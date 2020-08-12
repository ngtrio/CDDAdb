#!/bin/bash

sbtBuild() {
  echo "sbt building..."
  sbt clean
  sbt dist
  echo "sbt build done!"
}

npmBuild() {
  echo "npm building..."
  pushd view
  npm install
  popd
  echo "sbt build done!"
}

case $1 in
sbt)
  sbtBuild
  ;;
npm)
  npmBuild
  ;;
all)
  sbtBuild
  npmBuild
  ;;
*)
  echo args: sbt, npm, all
  ;;
esac
