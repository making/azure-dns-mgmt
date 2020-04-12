#!/bin/bash
set -ex
mkdir -p /tmp/bin
curl -sL https://github.com/go-acme/lego/releases/download/v3.5.0/lego_v3.5.0_linux_amd64.tar.gz | tar -C /tmp/bin -xzf -
export PATH=${PATH}:/tmp/bin