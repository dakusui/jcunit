#!/bin/bash -eu

# Component versions to install
COMPONENT_VERSIONS="$(cat <<'EOF'
JDK:         8.0.442-amzn
MAVEN:       3.9.6
GOLANG:      1.21.6
EOF
)"

function sed() {
  /usr/bin/sed "${@}"
}

# A function to ensure macOS default grep is used.
function grep() {
  /usr/bin/grep "${@}"
}

function projectbrew() {
  "${brewdir}/bin/brew" "${@}"
}

function version_for() {
  local _component_name="${1}"
  echo "${COMPONENT_VERSIONS}" | grep "^${_component_name}" | cut -f 2 -d ':' | sed -E 's/^ +//g'
}

function jdk_version() {
  version_for "JDK"
}
function maven_version() {
  version_for "MAVEN"
}
function golang_version() {
  version_for "GOLANG"
}

# Reads standard streams (stdout and stderr) and assign the content of them to variables specified by the first and
# second parameters respectively.
# Note that this function doesn't declare variables specified by the parameters.
# They need to be defined in the caller side if necessary.
# Also note that the behavior is undefined, when a variable name which starts with `__read_std_` is specified.
# 1:  A variable name to which stdout from the parameter 3- will be assigned.
# 2:  A variable name to which stderr from the parameter 3- will be assigned.
# 3-: A command line to be executed by this function.
function read_std() {
  local __read_std_stdout_varname __read_std_stderr_varname
  local __read_std_tmpfile_stderr __read_std_stdout_content __read_std_stderr_content __read_std_exit_code=0
  __read_std_stdout_varname="$(printf '%q' "${1}")"
  __read_std_stderr_varname="$(printf '%q' "${2}")"
  shift; shift
  __read_std_tmpfile_stderr="$(mktemp)"
  __read_std_stdout_content="$("${@}" 2> "${__read_std_tmpfile_stderr}")" || {
    __read_std_exit_code=$?
  }
  __read_std_stdout_content="$(printf '%q' "${__read_std_stdout_content}")"
  __read_std_stderr_content="$(cat "${__read_std_tmpfile_stderr}")"
  __read_std_stderr_content="$(printf '%q' "${__read_std_stderr_content}")"

  eval "${__read_std_stdout_varname}=${__read_std_stdout_content}"
  eval "${__read_std_stderr_varname}=${__read_std_stderr_content}"

  rm -f "${__read_std_tmpfile_stderr}"
  return "${__read_std_exit_code}"
}

function __bootstrap__perform_checks() {
  local _installation_reportdir="${1}" _session_name="${2}"
  local _failed=0
  local _stdout _stderr _session_dir="${1}/${_session_name}"
  shift
  shift
  mkdir -p "${_session_dir}"
  for _i in "${@}"; do
    mkdir -p "${_session_dir}/${_i}"
    read_std _stdout _stderr "${_i}" || {
      echo "${_stdout}" > "${_session_dir}/${_i}/stdout"
      echo "${_stderr}" > "${_session_dir}/${_i}/stderr"
      echo "FAIL: <${_i}>" >&2
      _failed=$((_failed + 1))
      continue
    }
    echo "${_stdout}" > "${_session_dir}/${_i}/stdout"
    echo "${_stderr}" > "${_session_dir}/${_i}/stderr"
    echo "pass: <${_i}>" >&2
  done
  echo "----"
  echo "FAILED CHECKS: ${_failed}" >&2
  [[ 0 == "${_failed}" ]] || return 1
}

function __bootstrap__checkenv() {
  local _installation_reportdir="${1}"
  local _checks=()
  function is_curl_installed() {
    which curl
  }
  _checks+=("is_curl_installed")
  function is_git_installed() {
    which git
  }
  _checks+=("is_git_installed")
  function is_ruby_installed() {
    which ruby
  }
  _checks+=("is_ruby_installed")
  function is_gem_installed() {
    which gem
  }
  _checks+=("is_gem_installed")
  __bootstrap__perform_checks \
    "${_installation_reportdir}" \
    "pre-check" \
    "${_checks[@]}"
}

function install_project_homebrew() {
  local _homebrew_dir="${1}"
  mkdir -p "${_homebrew_dir}"
  git init "${_homebrew_dir}"
  git -C "${_homebrew_dir}" remote add "origin" https://github.com/Homebrew/brew
  # git checkout -b main -C "${_homebrew_dir}"
  curl -L https://github.com/Homebrew/brew/tarball/master | tar xz --strip 1 -C "${_homebrew_dir}"
  # rm -fr "$("${_homebrew_dir}/bin/brew" --repo homebrew/core)"

  git -C "${_homebrew_dir}" add --all .
  git -C "${_homebrew_dir}" commit -a -m 'DUMMY COMMIT' >& /dev/null
}

function bootstrap_homebrew() {
  local _homebrew_dir="${1}"
  install_project_homebrew "${_homebrew_dir}" | progress "BOOTSTRAP" 2>&1 /dev/null
}

function install_brew_package() {
  local _homebrew_dir="${1}" _package="${2}"
  projectbrew install "${2}" | tee >(progress "${_package}")
}

function sdk_install() {
  local _lang="${1}" _ver="${2}"
  bash -c 'source '"${SDKMAN_DIR}"'/bin/sdkman-init.sh
           yes | sdk install '"${_lang}"' '"${_ver}"
}

function progress() {
  local _item_name="${1}"
  echo "BEGIN: ${_item_name}" >&2
  sed -E 's/^(.*)$/  ['"${_item_name}"'] \1/g' >&2
  echo "END:   ${_item_name}" >&2
}


function caveats() {
  # LIMITATION: Only PATH environment variable mangling will be considered.
  sed -n '/==> Caveats/,/END/p' | grep 'PATH=' || :
}

function reset_caveats_rc() {
  local _fname="${1}"
  [[ -e "${_fname}" ]] && rm "${_fname}"
  touch "${_fname}"
}

function compose_goenv_rc() {
  local _project_godir="${1}" _go_version="${2}"
  echo "
  export GOENV_ROOT=${_project_godir}/env
  export GOENV_SHELL=bash
  export GOPATH=${_project_godir}/${_go_version}
  export GOROOT=${_project_godir}/env/versions/${_go_version}

  export PATH=${_project_godir}/${_go_version}/bin:"'${PATH}'"
  "
}

function compose_sdk_rc() {
  local _jdk_name="${1}"
  echo "
  export SDK_JDK_NAME=${_jdk_name}
  export SDKMAN_DIR=$(pwd)/.dependencies/sdkman
  export JAVA_HOME=$(pwd)/.dependencies/sdkman/candidates/java/current
  "
}

function message() {
  echo "${@}" >&2
}

function main() {
  local _projectdir _project_dependencies_dir _project_brewdir  _project_rcdir _caveats_file
  local _project_godir _goenv_file
  local _project_sdkman_dir
  _projectdir="$(dirname "${1}")"
  _projectdir="$(realpath "${_projectdir}")"
  _project_dependencies_dir="${_projectdir}/.dependencies"

  _project_brewdir="${_project_dependencies_dir}/homebrew"

  _project_rcdir="${_project_dependencies_dir}/rc"
  _caveats_file="${_project_dependencies_dir}/rc/caveats.rc"

  _goenv_file="${_project_dependencies_dir}/rc/goenv.rc"
  _project_godir="${_project_dependencies_dir}/go"

  _sdkenv_file="${_project_dependencies_dir}/rc/sdk.rc"
  _project_sdkman_dir="${_project_dependencies_dir}/sdkman"
  shift

  local _precheck_reportdir=".dependencies/bootstrap"
  mkdir -p "${_precheck_reportdir}"

  # Erase all the downloaded files.
  message "We are erasing the old .dependencies"
  sudo rm -fr .dependencies
  # Performs precheck
  __bootstrap__checkenv "${_precheck_reportdir}"

  mkdir -p "${_project_rcdir}"
  touch "${_project_rcdir}/.bash_profile" # Ensure the presence of .bash_profile read by env.rc
  bootstrap_homebrew "${_project_brewdir}"
  reset_caveats_rc "${_caveats_file}"

  export HOMEBREW_NO_AUTO_UPDATE=1
  # Disable this behaviour by setting HOMEBREW_NO_INSTALL_CLEANUP.
  # Hide these hints with HOMEBREW_NO_ENV_HINTS (see `man brew`).
  export HOMEBREW_NO_INSTALL_CLEANUP=0
  export HOMEBREW_NO_ENV_HINTS=0

  # shellcheck disable=SC2129
  install_brew_package "${_project_brewdir}" make       | caveats >> "${_caveats_file}"
  install_brew_package "${_project_brewdir}" gnu-sed    | caveats >> "${_caveats_file}"
  install_brew_package "${_project_brewdir}" findutils  | caveats >> "${_caveats_file}"
  install_brew_package "${_project_brewdir}" xmlstarlet | caveats >> "${_caveats_file}"

  # golang
  mkdir -p "${_project_godir}/env"
  install_brew_package "${_project_brewdir}" goenv   > /dev/null
  compose_goenv_rc "${_project_godir}" "$(golang_version)" > "${_goenv_file}"
  # shellcheck disable=SC1090
  source "${_goenv_file}"
  "${_project_brewdir}/bin/goenv" install -q "$(golang_version)" 2>&1 | progress "goenv:$(golang_version)"
  "${_project_godir}/env/versions/$(golang_version)/bin/go" install github.com/Songmu/make2help/cmd/make2help@latest 2>&1 | progress "go:make2help"

  # sdkman & Java
  export HOME="${_project_rcdir}" # To avoid .bashrc / .bash_profile / .zsh_profile being updated
  export SDKMAN_DIR="${_project_sdkman_dir}"
  # install sdkman
  curl -s "https://get.sdkman.io"    | /bin/bash 2>&1 | progress "sdkman"

  sdk_install java "$(jdk_version)"  | progress "sdkman:java"
  sdk_install maven "$(maven_version)" | progress "sdkman:maven"

  compose_sdk_rc "$(jdk_version)" > "${_sdkenv_file}"
}

projectdir="$(dirname "${BASH_SOURCE[0]}")"
projectdir="$(realpath "${projectdir}")"
brewdir="${projectdir}/.dependencies/homebrew"

main "${0}" "$@"
