#!/bin/bash

PATH='/usr/local/bin:/usr/bin:/bin:/usr/games:/usr/lib/jvm/java-6-sun-1.6.0.07/bin'
dir='/home/unreal/wyldryde-logserver'

if [ `ps ux | grep -v 'grep' | grep -ci 'java logserver'` -eq 0 ] ; then
	cd $dir
	java LogServer 1>/dev/null 2>${dir}/err.log&
else
	echo "Already Running!"
	ps ux | grep -i 'java logserver' | grep -v 'grep'
fi
