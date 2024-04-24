#!/usr/bin/env bash

set -eu

# shellcheck disable=SC1090
source "$(dirname "${0}")/lib/mvn-utils.rc"
_project_name="$(project_name)"

rm -fr target/site-staging/.git
cd target/site-staging
git init
git remote add origin "https://github.com/dakusui/${_project_name}.git"
git push origin :gh-pages || :
git checkout -b gh-pages
git add --all
git commit -m "Update Documentation"
git push origin gh-pages