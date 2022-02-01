#!/usr/bin/env bash
set -eu

function convert_package_info_adoc_to_package_info_java() {
  local _base_dir=$1
  local _rel_dir=$2
  local _i
  local _out="${_base_dir}/${_rel_dir}/package-info.java"
  rm -f ${_out}
  touch ${_out}
  echo "/**" >> ${_out}
  while IFS= read -r _i
  do
    echo " * ${_i}" >> ${_out}
  done < "${_base_dir}/${_rel_dir}/package-info.adoc"

  echo " */" >> ${_out}
  echo "package ${_rel_dir//\//.};" >> ${_out}
}

# find src/main/java -name 'package-info.adoc' -exec echo convert_package_info_adoc_to_package_info_java src/main/java
# convert_package_info_adoc_to_package_info_java /Users/hiroshi.ukai/Documents/scriptiveunit/src/main/java com/github/dakusui/scriptiveunit/utils

base=src/main/java
for i in $(find ${base} -name 'package-info.adoc'); do
  convert_package_info_adoc_to_package_info_java ${base} $(dirname ${i#${base}/})
done;