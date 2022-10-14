#!/usr/bin/python
# -*- coding: utf-8 -*-
import os
import sys
import json

from utils import Utils


class Scp:
    def __init__(self):
        self.utils = Utils()
        self.envs = os.environ  # 系统环境变量
        self.CI_PROJECT_DIR = self.envs['CI_PROJECT_DIR']
        self.CI_PROJECT_NAME = self.envs['CI_PROJECT_NAME']

    def man(self):
        if "0" == self.utils.deploy_platform['caas_status']:
            self.utils.info("------未绑定CaaS平台，该步骤跳过执行-------")
            return
        if self.utils.CI_SCHEDULE and self.envs.get('CI_SCHEDULE') == "sit-ate":
            self.utils.info("------SIT-ATE定时部署，该步骤跳过执行-------")
            return

        # 获取实体属性值
        self.FDEV_SCP_IP = self.utils.get_env('FDEV_SCP_IP')
        self.FDEV_SCP_USER = self.utils.get_env('FDEV_SCP_USER')
        self.FDEV_SCP_PWD = self.utils.get_env('FDEV_SCP_PWD')
        self.FDEV_SCP_LOCAL_DIR = self.utils.get_env('FDEV_SCP_LOCAL_DIR')
        self.FDEV_SCP_REMOTE_DIR = self.utils.get_env('FDEV_SCP_REMOTE_DIR')
        self.FDEV_SCP_PORT = self.utils.get_env('FDEV_SCP_PORT')
        self.CI_PROJECT_ID = self.envs['CI_PROJECT_ID']
        # 获取scp-ci实体中的值
        # 若本地的地址不为PROJECTNAME 便是不使用默认的路径
        if self.FDEV_SCP_LOCAL_DIR == "PROJECTNAME":
            package_name = self.CI_PROJECT_DIR + "/" + self.CI_PROJECT_NAME
        else:
            package_name = self.FDEV_SCP_LOCAL_DIR

        user = self.FDEV_SCP_USER
        ip = self.FDEV_SCP_IP
        remote_dir = self.FDEV_SCP_REMOTE_DIR
        pwd = self.FDEV_SCP_PWD
        port = self.FDEV_SCP_PORT

        self.utils.info("scp实体属性值：FDEV_SCP_IP：%s,FDEV_SCP_USER：%s,"
                        "FDEV_SCP_PWD：%s,FDEV_SCP_LOCAL_DIR：%s,FDEV_SCP_REMOTE_DIR：%s,FDEV_SCP_PORT: %s"
                        % (ip, user, pwd, package_name, remote_dir, port))

        # scp package
        scp_cmd = "scp -P %s -r %s %s@%s:%s" % (
            port, package_name, user, ip, remote_dir)
        self.utils.input_pwd(scp_cmd, pwd)

        # 如果是打得 tag 包,也就是用来投产的包
        if os.environ.has_key('CI_COMMIT_TAG') == True:
            self.CI_COMMIT_TAG = os.environ['CI_COMMIT_TAG']
            self.utils.info("CI_COMMIT_TAG : ", self.CI_COMMIT_TAG)
            if "PRO" == self.CI_COMMIT_TAG.upper()[0:3]:
                # 获取本地目录下sourceMap下的所有map文件
                mapFiles = self.utils.get_map(package_name, remote_dir + "/" + self.CI_PROJECT_NAME)
                # 获取环境的fdev_url
                fdev_url = self.utils.get_fdev_url()
                request_data = json.dumps(
                    {"path": list(mapFiles), "gitlab_project_id": int(self.CI_PROJECT_ID),
                     "product_tag": self.CI_COMMIT_TAG})
                request_url = fdev_url + "/frelease/api/releasenode/application/saveApplicationPath"
                response_data = self.utils.post_request(request_url, request_data)
                if response_data['code'] != "AAAAAAA":
                    self.utils.warn("发送投产模块后台交易失败：", response_data['msg'])
                    sys.exit(-1)


if __name__ == '__main__':
    scp = Scp()
    scp.man()
