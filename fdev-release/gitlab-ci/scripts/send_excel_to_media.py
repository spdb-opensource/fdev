#!/usr/bin/env python
# -*- coding: utf-8 -*-
import os
import shutil
import time
from pexpect_cmd import *
from fdev_requests import query_automation_param_map

# 通过自动化配置表获取配置
automation_param = query_automation_param_map()
# 自动化发布服务器ip
AUTORELEASE_SERVER_HOST = automation_param["autorelease.server.host"]
# 自动化发布服务器用户
AUTORELEASE_SERVER_USER = automation_param["autorelease.server.user"]
# 自动化发布服务器密码
AUTORELEASE_SERVER_PASSWORD = automation_param["autorelease.server.password"]

if __name__ == '__main__':
    log_path = "/fdev/log/"
    logging_file = log_path + "send_excel_to_media.log"
    if not os.path.exists(log_path):
        os.makedirs(log_path)
    logfile = open(logging_file, 'a')
    begin_time = time.strftime("%Y-%m-%d %H:%M:%S")
    logfile.write("\nbegin===============================" + begin_time + "=================================begin\n")
    local_path = sys.argv[1]
    excel_file = sys.argv[2]
    media_path = sys.argv[3]
    logfile.write("\n本地文件路径：" + excel_file)
    logfile.write("\n介质服务器目录：" + media_path)
    # 创建文件夹
    ssh_mkdir = "ssh " + AUTORELEASE_SERVER_USER + "@" + AUTORELEASE_SERVER_HOST + " \"mkdir -p " + media_path + "\""
    exec_cmd(ssh_mkdir, AUTORELEASE_SERVER_PASSWORD, logging_file)

    # 传送excel文件
    scp_excel = "scp " + excel_file + " " + AUTORELEASE_SERVER_USER + "@" + AUTORELEASE_SERVER_HOST + ":" + media_path
    exec_cmd(scp_excel, AUTORELEASE_SERVER_PASSWORD, logging_file)
    shutil.rmtree(local_path)
    logfile.write("删除本地文件所在文件夹：" + local_path)
    end_time = time.strftime("%Y-%m-%d %H:%M:%S")
    logfile.write("\nend===============================" + end_time + "=================================end\n")
    logfile.close()
