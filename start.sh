#!/bin/bash

build_path="target/universal"
files=$(ls $build_path)
for file in $files; do
  if [ ${file##*.} = "zip" ]; then
    full_name=${file%.*}
    # version=${full_name#*-}
    pushd $build_path || exit
    unzip -q $file
    mv $full_name "cddadb"
    popd || exit
    # echo "CDDADB_VERSION=$version" >.env
    docker-compose down
    docker rmi cddadb:latest
    docker-compose up -d
  fi
done
