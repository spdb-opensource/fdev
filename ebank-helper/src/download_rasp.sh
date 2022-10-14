#!/bin/bash

sys=$1
path=$2

current_dir_path=$(dirname $(readlink -f "$0"))
if [ -d $current_dir_path/tmp_rasp ];then
	cd $current_dir_path/tmp_rasp
	curl -O http://hfkfxms.spdbdev.com:8060/cloud-zh-israsp-kf-bucket-rasp-rel/rasp.tar.gz
else
	mkdir $current_dir_path/tmp_rasp
	cd $current_dir_path/tmp_rasp
	curl -O http://hfkfxms.spdbdev.com:8060/cloud-zh-israsp-kf-bucket-rasp-rel/rasp.tar.gz
fi

if [ -f $current_dir_path/tmp_rasp/rasp.tar.gz ];then
	tar -xvf rasp.tar.gz
fi

curl -O http://hfkfxms.spdbdev.com:8060/cloud-zh-israsp-kf-bucket-rasp-rel/$sys.yml
echo $current_dir_path/tmp_rasp/$sys.yml " size :"
ls -l $current_dir_path/tmp_rasp/$sys.yml | awk '{print $5}'

if [ -f $current_dir_path/tmp_rasp/$sys.yml ];then
	echo $current_dir_path/tmp_rasp/rasp/conf/openrasp.yml " size :"
	ls -l $current_dir_path/tmp_rasp/rasp/conf/openrasp.yml | awk '{print $5}'
	echo "move" $current_dir_path/tmp_rasp/$sys.yml "to " $current_dir_path"/tmp_rasp/rasp/conf/openrasp.yml" 
	mv $current_dir_path/tmp_rasp/$sys.yml $current_dir_path/tmp_rasp/rasp/conf/openrasp.yml
	echo $current_dir_path/tmp_rasp/rasp/conf/openrasp.yml " size :"
	ls -l $current_dir_path/tmp_rasp/rasp/conf/openrasp.yml | awk '{print $5}'
	echo "move successed"
fi

if [ -f $current_dir_path/tmp_rasp/rasp.tar.gz ];then
	echo "clean rasp.tar.gz"
	rm -rf $current_dir_path/tmp_rasp/rasp.tar.gz
fi

cp -r $current_dir_path/tmp_rasp/rasp/ $path
