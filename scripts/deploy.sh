#!/bin/bash
#Github Actions

dir="CDDAdb"
if [ ! -d $dir ]; then
  git clone git@github.com:innun/CDDAdb.git
  cd $dir
else
  cd $dir
  git pull origin master
fi
bash build.sh sbt
bash start.sh
