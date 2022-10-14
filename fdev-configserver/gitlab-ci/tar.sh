#!/bin/sh
dir=$1
user=$2
tar -cvf ${dir}.tar ${dir}
echo "{="`md5sum ${dir}.tar | awk -F " " '{print $1}' `"}" > ${dir}.tar.md5
chown ${user}:${user} ${dir}.tar
chown ${user}:${user} ${dir}.tar.md5
chmod 777 *
chmod 777 ../*