#!/bin/bash
export LANG=zh_CN.gb18030

current_dir_path=$(dirname $(readlink -f "$0"))
current_date=`date '+%Y%m%d'`

if [ ! -e ./config.info ];
then
        echo '待迁移配置文件列表配置读取失败，当前目录配置文件：config.info不存在'
        exit 0
fi

mkdir -p ./log
echo "error log" > ./log/error.log
echo "success log" > ./log/success.log

cat ./config.info | while read line
do
        if [ ! -n "$line" ]
        then
                continue
        fi
        
        info_arr=($line)
        bucket_name=${info_arr[0]}
        app_id=${info_arr[1]}
		file_name=${info_arr[2]}
        cd /app/s3Config/
        msg=`sh uploadFile.sh $bucket_name ${current_dir_path}"/"${file_name}`
        OLD_IFS=${IFS}
        IFS='
		'
        temp_msg=($msg)
        for var in ${temp_msg[@]}
        do
                if [[ $msg =~ "上传成功" ]]
                then
                        echo -e "${var}" >> ${current_dir_path}/log/success.log
                else
                        echo -e "${var}" >> ${current_dir_path}/log/error.log
                fi
        done

        if [[ $msg =~ "上传失败" ]]
        then
                echo "微服务${app_id}配置文件上传失败，详情查看文件./log/error.log"
        fi
		IFS=$OLD_IFS

done
