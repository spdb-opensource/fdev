#!/usr/bin/env python
# -*- coding: utf-8 -*-
import random
import shutil
from pexpect_cmd import *
from fdev_requests import *

# 通过自动化配置表获取配置
automation_param = query_automation_param_map()
# 自动化发布服务器ip
AUTORELEASE_SERVER_HOST = automation_param["autorelease.server.host"]
# 自动化发布服务器用户
AUTORELEASE_SERVER_USER = automation_param["autorelease.server.user"]
# 自动化发布服务器密码
AUTORELEASE_SERVER_PASSWORD = automation_param["autorelease.server.password"]
# 自动化发布服务器介质根目录
AUTORELEASE_SERVER_ROOT_DIR = automation_param["autorelease.server.root.dir"]
# 本地准备介质临时目录
TEMP_DIR = automation_param["autorelease.tmp.dir"]


def check_tar_md5(old_dir, new_dir, version, logs, num):
    # 复制tar包
    ssh_copytar = "ssh " + AUTORELEASE_SERVER_USER \
                  + "@" + AUTORELEASE_SERVER_HOST \
                  + " \"\\cp  -rf " + old_dir + "/" + version + ".tar " + new_dir + "/\""
    exec_cmd(ssh_copytar, AUTORELEASE_SERVER_PASSWORD, logs)

    ssh_tarmd5 = "ssh " + AUTORELEASE_SERVER_USER \
                 + "@" + AUTORELEASE_SERVER_HOST \
                 + " \"md5sum " + new_dir + "/" + version + ".tar\""
    child_tarmd5 = exec_cmd(ssh_tarmd5, AUTORELEASE_SERVER_PASSWORD, logs)
    tarmd5 = str(child_tarmd5.before.strip()).split(new_dir + "/"
                                                    + version + ".tar")[0].split("\n")[1].strip()

    ssh_catmd5 = "ssh " + AUTORELEASE_SERVER_USER \
                 + "@" + AUTORELEASE_SERVER_HOST \
                 + " \"cat " + new_dir + "/" + version + ".tar.md5\""
    child_catmd5 = exec_cmd(ssh_catmd5, AUTORELEASE_SERVER_PASSWORD, logs)
    catmd5 = str(child_catmd5.before.strip()).split("{=")[1].strip("}")

    log_file = open(logs, 'a')
    log_file.write("tarmd5:" + tarmd5 + "\n")
    log_file.write("catmd5:" + catmd5 + "\n")

    flag = False
    if tarmd5 == catmd5:
        log_file.write("文件md5校验正确\n")
        flag = True
    elif num < 3:
        check_tar_md5(old_dir, new_dir, version, logs, num + 1)
    return flag


if __name__ == '__main__':
    log_path = "/fdev/log/"
    logging_file = log_path + "copy_media.log"
    if not os.path.exists(log_path):
        os.makedirs(log_path)
    prod_list = sys.argv[1]
    prod_new_dir = sys.argv[2]
    old_prod_detail = query_prod_detail(prod_list.split(",")[0])
    prod_old_dir = old_prod_detail["prod_assets_version"]
    # 转移介质 必定为同一系统标识,默认取第一个,优先找到这个文件夹删除后新建
    prod_abbr = query_group_system_abbr(old_prod_detail["owner_groupId"])
    new_remote_root_path = AUTORELEASE_SERVER_ROOT_DIR + "/" + prod_abbr + "/" + prod_new_dir
    # 删除文件夹
    ssh_removedir = "ssh " + AUTORELEASE_SERVER_USER \
                    + "@" + AUTORELEASE_SERVER_HOST \
                    + " \"rm -rf " + new_remote_root_path + "\""
    exec_cmd(ssh_removedir, AUTORELEASE_SERVER_PASSWORD, logging_file)
    # 创建文件夹
    ssh_mkdir = "ssh " + AUTORELEASE_SERVER_USER \
                + "@" + AUTORELEASE_SERVER_HOST \
                + " \"mkdir -p " + new_remote_root_path + "/\""
    exec_cmd(ssh_mkdir, AUTORELEASE_SERVER_PASSWORD, logging_file)
    prod_root_dir = TEMP_DIR + prod_old_dir + "/"
    imagelist_url = prod_root_dir + str(random.randint(0, 999999)) + "/"
    trans_imagelist = False
    for prod_id in prod_list.split(','):
        try:
            prod_detail = query_prod_detail(prod_id)
            # 介质准备完成则执行
            if prod_detail["auto_release_stage"] == "7":
                prod_version = prod_detail["version"]
                remote_root_path = AUTORELEASE_SERVER_ROOT_DIR + "/" + prod_abbr + "/" + prod_old_dir

                # 复制文件夹
                ssh_copydir = "ssh " + AUTORELEASE_SERVER_USER \
                    + "@" + AUTORELEASE_SERVER_HOST \
                    + " \"cp -r " + remote_root_path + "/" + prod_version + " " + new_remote_root_path + "/\""
                exec_cmd(ssh_copydir, AUTORELEASE_SERVER_PASSWORD, logging_file)

                # 复制md5文件
                ssh_copymd5file = "ssh " + AUTORELEASE_SERVER_USER \
                    + "@" + AUTORELEASE_SERVER_HOST \
                    + " \"cp " + remote_root_path + "/" + prod_version + ".tar.md5 " + new_remote_root_path + "/\""
                exec_cmd(ssh_copymd5file, AUTORELEASE_SERVER_PASSWORD, logging_file)
                check_result = check_tar_md5(remote_root_path, new_remote_root_path, prod_version, logging_file, 1)
                if not check_result:
                    # 文件复制校验MD5三次不对,删除tar包 放弃执行
                    ssh_deletetar = "ssh " + AUTORELEASE_SERVER_USER \
                                  + "@" + AUTORELEASE_SERVER_HOST \
                                  + " \"rm -f " + new_remote_root_path + "/" + prod_version + ".tar\""
                    exec_cmd(ssh_copydir, AUTORELEASE_SERVER_PASSWORD, logging_file)

                imagelist_file = prod_root_dir + prod_version + ".txt"
                if os.path.exists(imagelist_file):
                    trans_imagelist = True
                    if not os.path.exists(imagelist_url):
                        os.makedirs(imagelist_url)
                    f = open(imagelist_url + "imagelist.txt", 'a')
                    with open(imagelist_file, 'r') as prod_imagelist:
                        f.write(prod_imagelist.read())
                    f.close()
        except (IOError, OSError, Exception):
            logfile = open(logging_file, 'a')
            logfile.write("变更id:" + prod_id + "复制失败")
            logfile.close()
    # 若imagelist文件存在
    if trans_imagelist:
        # 传送imagelist.txt
        scp_imagelist = "scp " + imagelist_url + "imagelist.txt " + AUTORELEASE_SERVER_USER \
                        + "@" + AUTORELEASE_SERVER_HOST + ":" + new_remote_root_path
        exec_cmd(scp_imagelist, AUTORELEASE_SERVER_PASSWORD, logging_file)
        shutil.rmtree(imagelist_url)
    logfile = open(logging_file, 'a')
    logfile.write("\n=================================================================================\n")
    logfile.close()
