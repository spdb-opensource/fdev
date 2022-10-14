#!/usr/bin/python
# -*- coding: utf-8 -*-
import commands
import os
import sys
import urllib2
import json
import time
from utils import Utils

__version__ = "20190827_001"

class Build:

    def __init__(self):
        # 加载 docker 环境,不然无法使用 docker 命令
        os.putenv("DOCKER_HOST", "tcp://localhost:2375")
        envs = os.environ  # 系统环境变量
        self.utils = Utils()
        self.fdev_url = self.utils.get_fdev_url()
        self.envs = envs

        self.FDEV_CAAS_REGISTRY_USER = self.utils.get_env('FDEV_CAAS_REGISTRY_USER')
        self.FDEV_CAAS_REGISTRY_PASSWORD = self.utils.get_env('FDEV_CAAS_REGISTRY_PASSWORD')
        self.FDEV_CAAS_REGISTRY = self.utils.get_env("FDEV_CAAS_REGISTRY")
        self.FDEV_CAAS_REGISTRY_NAMESPACE = self.utils.get_env("FDEV_CAAS_REGISTRY_NAMESPACE")


        self.fdev_dockerfile_registry_namespace = self.utils.get_env("FDEV_DOCKERFILE_REGISTRY_NAMESPACE")
        self.fdev_dockerfile_ip = self.utils.get_env("FDEV_DOCKERFILE_IP")
        self.fdev_dockerfile_tag = self.utils.get_env("FDEV_DOCKERFILE_TAG")
        self.fdev_dockerfile_name = self.utils.get_env("FDEV_DOCKERFILE_NAME")

        self.CI_PROJECT_DIR = envs['CI_PROJECT_DIR']
        self.CI_PIPELINE_ID = envs['CI_PIPELINE_ID']
        self.CI_PROJECT_ID = envs['CI_PROJECT_ID']

        # 获得部署的目录
        self.DEPLOY_DIR = self.utils.get_file_dir()
        # 镜像坐标: ip/namepscae/project_name_en
        name_en = self.utils.get_name_en(self.CI_PROJECT_ID, self.fdev_url)
        self.CI_REGISTRY_IMAGE = self.FDEV_CAAS_REGISTRY + "/" + self.FDEV_CAAS_REGISTRY_NAMESPACE + "/" + name_en

    def man(self):
        result = self.utils.cmd("find " + self.CI_PROJECT_DIR + " -name '*.*ar' | wc -l")
        if int(result) != 0:
            # copy 打包产出物到 dockerfile 目录下
            self.utils.cmd("cp `find " + self.CI_PROJECT_DIR + " -name '*.*ar'` " + self.DEPLOY_DIR)

        self.utils.cmd("cd " + self.CI_PROJECT_DIR + "/gitlab-ci && envsubst < Dockerfile > Dockerfile.temp && mv Dockerfile.temp Dockerfile")
        self.utils.cmd("cd " + self.DEPLOY_DIR + " && pwd && echo "" && ls && echo "" && cat Dockerfile")

        # 代表打得 tag 包,也就是用来投产的包
        if os.environ.has_key('CI_COMMIT_TAG') == True:
            self.CI_COMMIT_TAG = os.environ['CI_COMMIT_TAG']
            if "PRO" == self.CI_COMMIT_TAG.upper()[0:3]:
                # tag 镜像
                image = self.CI_REGISTRY_IMAGE + ":" + self.CI_COMMIT_TAG
                self.utils.cmd("docker login -u %s -p %s %s" % (self.FDEV_CAAS_REGISTRY_USER, self.FDEV_CAAS_REGISTRY_PASSWORD, self.fdev_dockerfile_ip))
                self.utils.cmd("cd %s && docker build . -t %s" % (self.DEPLOY_DIR, image))
                self.utils.cmd("docker push %s" % image)
                data = json.dumps({"gitlab_project_id": int(self.CI_PROJECT_ID), "product_tag": self.CI_COMMIT_TAG,
                                   "pro_image_uri": str(image)})
                url = self.fdev_url + "/frelease/api/releasenode/application/saveProductImage"
                self.utils.post_request(url, data)
        else:
            # docker build 根据Dockerfile 文件中的镜像 登录
            registry_tag = self.utils.get_registry_tag()
            self.utils.cmd("docker login -u %s -p %s %s" % (self.FDEV_CAAS_REGISTRY_USER, self.FDEV_CAAS_REGISTRY_PASSWORD, self.fdev_dockerfile_ip))
            self.utils.cmd("cd %s && docker build . -t %s:%s" % (self.DEPLOY_DIR, self.CI_REGISTRY_IMAGE, registry_tag))
            # docker push 登录需要推送的镜像仓库地址
            self.utils.cmd("docker login -u %s -p %s %s" % (self.FDEV_CAAS_REGISTRY_USER, self.FDEV_CAAS_REGISTRY_PASSWORD, self.FDEV_CAAS_REGISTRY))
            self.utils.cmd("docker push %s:%s" % (self.CI_REGISTRY_IMAGE, registry_tag))

if __name__ == '__main__':
    print "[+] 镜像版本: ", __version__
    build = Build()
    build.man()