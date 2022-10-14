#!/usr/bin/python
# -*- coding: utf-8 -*-

import json
import os
import sys

from utils import Utils


class Build:

    def __init__(self):
        self.envs = os.environ  # 系统环境变量
        self.utils = Utils()
        self.fdev_url = self.utils.get_fdev_url()

        self.FDEV_DOCKER_USER = self.utils.get_env('FDEV_DOCKER_USER')
        self.FDEV_DOCKER_PWD = self.utils.get_env('FDEV_DOCKER_PWD')
        self.FDEV_DOCKER_BASE_REGISTRY = self.utils.get_env("FDEV_DOCKER_BASE_REGISTRY")
        self.FDEV_DOCKER_NAMESPACE = self.utils.get_env("FDEV_DOCKER_NAMESPACE")
        self.FDEV_DOCKER_BASE_NAMESPACE = self.utils.get_env("FDEV_DOCKER_BASE_NAMESPACE")
        self.FDEV_CAAS_BASE_IMAGE_VERSION = self.utils.get_env("FDEV_CAAS_BASE_IMAGE_VERSION")
        self.FDEV_DOCKER_IP = self.utils.get_env("FDEV_DOCKER_IP")

        self.CI_PROJECT_DIR = self.envs['CI_PROJECT_DIR']
        self.CI_PIPELINE_ID = self.envs['CI_PIPELINE_ID']
        self.CI_PROJECT_ID = self.envs['CI_PROJECT_ID']
        self.CI_PROJECT_NAME = self.envs['CI_PROJECT_NAME']
        self.CI_COMMIT_REF_NAME = self.envs['CI_COMMIT_REF_NAME']

        # 替换 Dockerfile 写入系统环境变量
        os.environ['FDEV_DOCKER_BASE_NAMESPACE'] = self.FDEV_DOCKER_BASE_NAMESPACE
        os.environ['FDEV_DOCKER_BASE_REGISTRY'] = self.FDEV_DOCKER_BASE_REGISTRY

        # 获得部署的目录
        self.DEPLOY_DIR = self.utils.get_file_dir()
        # 镜像坐标: ip/namepscae/project_name_en
        name_en = self.utils.rename_project_name(self.CI_PROJECT_NAME)

        self.CI_REGISTRY_IMAGE = self.FDEV_DOCKER_IP + "/" + self.FDEV_DOCKER_NAMESPACE + "/" + name_en
        self.success = "AAAAAAA"

        if 0 != int(self.CI_PIPELINE_ID):
            # 加载 docker 环境,不然无法使用 docker 命令
            os.putenv("DOCKER_HOST", "tcp://localhost:2375")

    def get_tar(self, parentDir):
        fileList = []
        file_name = ""
        for home, dirs, files in os.walk(parentDir):
            for fileName in files:
                if "gitlab-ci" in home:
                    continue
                if home.endswith("target") and (fileName.endswith(".tar.gz")):
                    fileList.append(os.path.join(home, fileName))
                    file_name = fileName
                    break
        return fileList, file_name

    def man(self):
        fileList, file_name = self.get_tar(self.CI_PROJECT_DIR + "/distribution")
        if len(fileList) != 0:
            for fileName in fileList:
                print fileName
                # 调用方法，将打出来的jar或war包上传到minio
                if os.environ.has_key('CI_COMMIT_TAG') == True:
                    self.utils.upload_jar_or_war_to_minio(fileName)
                self.utils.cmd("cp -r --backup=numbered %s %s" % (fileName, self.DEPLOY_DIR))
                self.utils.cmd("cd %s &&  tar -xzvf %s" % (self.DEPLOY_DIR, file_name))
        else:
            self.utils.warn("找到不到对应的介质包, 检测mvn-build 是否生成对应的介质包")
        # 替换 Dockerfile 中的内容
        envsubst = """envsubst "`printf '${%s} ' $(sh -c "env|cut -d'=' -f1")`" < Dockerfile > Dockerfile.tmp && mv Dockerfile.tmp Dockerfile"""
        self.utils.cmd("cd " + self.DEPLOY_DIR + "&&" + envsubst)
        self.utils.cmd("cd " + self.DEPLOY_DIR + " && pwd && ls && cat Dockerfile")
        self.is_pro_image = False
        self.image_tag = self.utils.get_registry_tag()
        # 如果是打得 tag 包,也就是用来投产的包
        if os.environ.has_key('CI_COMMIT_TAG') == True:
            self.CI_COMMIT_TAG = os.environ['CI_COMMIT_TAG']
            if "PRO" == self.CI_COMMIT_TAG.upper()[0:3]:
                self.is_pro_image = True
                # tag 镜像
                self.image_tag = self.CI_COMMIT_TAG
        # docker 登录到有基础镜像的ip去构建镜像
        self.utils.info("docker login " + self.FDEV_DOCKER_IP + " -u " + self.FDEV_DOCKER_USER)
        self.utils.cmdN("docker login -u %s -p %s %s" % (
            self.FDEV_DOCKER_USER, self.FDEV_DOCKER_PWD, self.FDEV_DOCKER_IP))
        # docker build . -t
        self.utils.cmd("cd %s && docker build . -t %s:%s" % (self.DEPLOY_DIR, self.CI_REGISTRY_IMAGE, self.image_tag))
        self.utils.cmd("docker push %s:%s" % (self.CI_REGISTRY_IMAGE, self.image_tag))

        if self.is_pro_image == True:
            data = json.dumps({"gitlab_project_id": int(self.CI_PROJECT_ID), "product_tag": self.CI_COMMIT_TAG,
                               "pro_image_uri": str(self.CI_REGISTRY_IMAGE + ":" + self.CI_COMMIT_TAG)})
            url = self.fdev_url + "/frelease/api/releasenode/application/saveProductImage"
            self.utils.post_request(url, data)
            self.push_war_md5()

    # 获取指定应用里target文件夹下的war包，将该应用的静态资源进行MD5并上传至GitLab
    def push_war_md5(self):
        war_name = None
        if self.CI_PROJECT_NAME.startswith(
                "mspmk-cli-") or self.CI_PROJECT_NAME == "spdb-cli-mobcli" or self.CI_PROJECT_NAME == "mspmk-web-pits":
            war_name = self.utils.get_war(self.CI_PROJECT_DIR)
            self.utils.info(war_name)
        if war_name:
            self.utils.cmd("cd %s && mkdir %s" % (self.DEPLOY_DIR, self.CI_PROJECT_NAME))
            md5_dir = self.DEPLOY_DIR + "/" + self.CI_PROJECT_NAME
            self.utils.cmd("unzip -oq %s -d %s" % (war_name, md5_dir))
            self.utils.cmd("genemd5.sh %s/WEB-INF/classes/static/ %s" % (md5_dir, self.CI_PROJECT_NAME))
            if not os.path.exists(self.CI_PROJECT_NAME + ".info"):
                self.utils.warn('获取MD5文件失败！')
                sys.exit(0)
            # 获取MD5文件内容
            with open(self.CI_PROJECT_NAME + ".info", "r") as fr:
                md5_content = fr.read()
            data = json.dumps({"gitlabId": int(self.CI_PROJECT_ID), "tag_name": self.CI_COMMIT_TAG,
                               "content": str(md5_content), "pipeline_id": self.CI_PIPELINE_ID})
            url = self.fdev_url + "/fenvconfig/api/v2/configfile/saveWarMd5ToGitlab"
            response_data = self.utils.post_request(url, data)
            if "AAAAAAA" != response_data["code"]:
                self.utils.warn('调环境配置模块接口上传war包的md5出错：', response_data['msg'])
                sys.exit(-1)


if __name__ == '__main__':
    build = Build()
    # 制作镜像
    build.man()
