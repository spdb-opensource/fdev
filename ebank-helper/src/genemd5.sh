#!/bin/sh
EXCLUDE="ttf|woff|woff2|eot|map|gz|mp3|mp4|pdf|info|version"

function clear_cache() {
        if [ -f $1".info" ];then
                `rm -rf $1".info"`
        fi
}

function echo_md5 () {
        for file in `ls $1`
        do
                if [ -d $1"/"$file ]
                then
                        echo_md5 $1"/"$file $2 $3"/"$file;
                elif [ -f $1"/"$file ]
                then
                        fileExt=${file##*.}
                        result=$(echo $EXCLUDE | grep $fileExt)
                        if [ $result ] ; then
                           echo "filter "$3"/"$file;
                           continue;
                        fi;
                        filename=$2$3"/"$file;
                        echo "$filename=`md5sum $1"/"$file | cut -d " " -f1`" >> $2".info";
                fi
        done
}

clear_cache $2
echo_md5 $1 $2