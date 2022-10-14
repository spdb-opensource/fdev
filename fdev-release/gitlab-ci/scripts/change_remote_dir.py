#!/usr/bin/env python
# -*- coding: utf-8 -*-
import os

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
# 自动化发布服务器介质根目录
AUTORELEASE_SERVER_ROOT_DIR = automation_param["autorelease.server.root.dir"]


if __name__ == '__main__':
    system_abbr = sys.argv[1]
    old_prod_assets_version = sys.argv[2]
    new_prod_assets_version = sys.argv[3]
    prod_version = sys.argv[4]
    remote_root_path = AUTORELEASE_SERVER_ROOT_DIR \
                       + "/" + system_abbr + "/" + old_prod_assets_version
    new_remote_root_path = AUTORELEASE_SERVER_ROOT_DIR \
                       + "/" + system_abbr + "/" + new_prod_assets_version
    log_path = "/fdev/log/"
    logging_file = log_path + "chremotedir.log"
    if not os.path.exists(log_path):
        os.makedirs(log_path)
    if prod_version is None or prod_version == "":
        # 修改远程变更介质目录名
        ssh_changedir = "ssh " + AUTORELEASE_SERVER_USER \
                        + "@" + AUTORELEASE_SERVER_HOST \
                        + " \"mv " + remote_root_path + " " + new_remote_root_path + "\""
        print ssh_changedir
        exec_cmd(ssh_changedir, AUTORELEASE_SERVER_PASSWORD, logging_file)
    else:
        # 更换变更所在目录
        ssh_mkdir = "ssh " + AUTORELEASE_SERVER_USER \
                        + "@" + AUTORELEASE_SERVER_HOST \
                        + " \"mkdir -p " + new_remote_root_path + "/\""
        exec_cmd(ssh_mkdir, AUTORELEASE_SERVER_PASSWORD, logging_file)
        # 更换变更所在目录
        ssh_changedir = "ssh " + AUTORELEASE_SERVER_USER \
                        + "@" + AUTORELEASE_SERVER_HOST \
                        + " \"mv " + remote_root_path + "/" + prod_version + "* " + new_remote_root_path + "/\""
        print ssh_changedir
        exec_cmd(ssh_changedir, AUTORELEASE_SERVER_PASSWORD, logging_file)
    logfile = open(logging_file, 'a')
    logfile.write("\n=====================================\n")
