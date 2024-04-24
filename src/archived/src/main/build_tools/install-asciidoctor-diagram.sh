#!/usr/bin/env bash
set -eu

mkdir -p generated/build/gems
gem install concurrent-ruby -i generated/build/gems
gem install asciidoctor-diagram --version=1.5.10 -i generated/build/gems
