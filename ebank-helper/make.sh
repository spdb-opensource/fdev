#!/bin/bash
tag=$1

rm -rf fdev-helper*.tar.gz
docker build . -txxx:9999/library/fdev-helper:$tag
docker savexxx:9999/library/fdev-helper:$tag -o fdev-helper-$tag.tar
gzip fdev-helper-$tag.tar
git add .
git commit -m ""$tag
git push origin master