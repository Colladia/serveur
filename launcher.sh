#!/bin/bash
#set -xv

#------------------------------
# functions
#------------------------------
function launch () {
	java -cp lib/*:bin/: $@
}


#------------------------------
# main
#------------------------------
cd "$(dirname $0)"
make
launch "test.Test"
