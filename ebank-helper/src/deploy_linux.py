#!/usr/bin/python
# -*- coding: utf-8 -*-
import datetime
import os
import sys

from utils import Utils


# 部署到指定的宿主机上
class DeployLinux:
    def __init__(self):
        # 系统环境变量
        self.envs = os.environ
        self.utils = Utils()
        self.CI_PROJECT_NAME = self.envs['CI_PROJECT_NAME']
        self.CI_PROJECT_DIR = self.envs['CI_PROJECT_DIR']
        self.FDEV_LINUX_USER = self.utils.get_env('FDEV_LINUX_USER')
        self.FDEV_LINUX_PWD = self.utils.get_env('FDEV_LINUX_PWD')
        self.FDEV_LINUX_IP = self.utils.get_env('FDEV_LINUX_IP')
        self.FDEV_LINUX_DIR = self.utils.get_env('FDEV_LINUX_DIR')
        self.FDEV_PACKAGE_NAME = self.utils.get_env('FDEV_PACKAGE_NAME')
        self.FDEV_LINUX_START_SH = self.utils.get_env('FDEV_LINUX_START_SH')
        self.FDEV_LINUX_STOP_SH = self.utils.get_env('FDEV_LINUX_STOP_SH')

    def get_jar_or_war(self, parent_dir):
        for home, dirs, files in os.walk(parent_dir):
            for fileName in files:
                if home.endswith("target") and (fileName == self.FDEV_PACKAGE_NAME):
                    return os.path.join(home, fileName)

    def man(self):
        # 获取target文件夹下的介质包
        package_name = self.get_jar_or_war(self.CI_PROJECT_DIR)
        self.utils.info("package_name:", package_name)
        if not package_name:
            self.utils.error("请检查maven-build是否生成对应的介质包!")
            sys.exit(-1)
        ssh_cmd = "ssh %s@%s " % (self.FDEV_LINUX_USER, self.FDEV_LINUX_IP)
        # 备份原有的包
        date_time = str(datetime.datetime.now()).split(".")[0].replace(" ", "_").replace(":", "-")
        file_name = self.FDEV_LINUX_DIR + self.FDEV_PACKAGE_NAME
        name_split = self.FDEV_PACKAGE_NAME.split(".")
        if len(name_split) != 2:
            self.utils.error("请检查包名是否填写正确!")
            sys.exit(-1)
        bak_file_name = self.FDEV_LINUX_DIR + name_split[0] + "_" + date_time + "." + name_split[1]
        bak_cmd = "%s 'cp %s %s'" % (ssh_cmd, file_name, bak_file_name)
        if self.FDEV_LINUX_PWD:
            self.utils.input_pwd(bak_cmd, self.FDEV_LINUX_PWD)
        else:
            self.utils.cmd(bak_cmd)
        # scp package
        scp_cmd = "scp %s %s@%s:%s" % (
            package_name, self.FDEV_LINUX_USER, self.FDEV_LINUX_IP, self.FDEV_LINUX_DIR)
        if self.FDEV_LINUX_PWD:
            self.utils.input_pwd(scp_cmd, self.FDEV_LINUX_PWD)
        # 根据开关执行启停命令，默认重启，FDEV_LINUX_SH:strat,stop
        start_cmd = ""
        if "FDEV_LINUX_SH" in self.envs:
            fdev_linux_sh = self.envs['FDEV_LINUX_SH']
            if "start" == fdev_linux_sh:
                start_cmd = ssh_cmd + self.FDEV_LINUX_START_SH
            elif "stop" == fdev_linux_sh:
                start_cmd = ssh_cmd + self.FDEV_LINUX_STOP_SH
        else:
            start_cmd = "%s '%s && %s'" % (ssh_cmd, self.FDEV_LINUX_STOP_SH, self.FDEV_LINUX_START_SH)
        if self.FDEV_LINUX_PWD:
            self.utils.input_pwd(start_cmd, self.FDEV_LINUX_PWD)
        else:
            self.utils.cmd(start_cmd)


if __name__ == '__main__':
    deploy_linux = DeployLinux()
    deploy_linux.man()
