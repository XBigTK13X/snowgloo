#! /bin/bash

docker rm -f snowgloo > /dev/null 2>&1

docker run --name snowgloo -d -v /media:/media -v /home/kretst/snowgloo/asset:/snowgloo -p 5050:5050 xbigtk13x/snowgloo
