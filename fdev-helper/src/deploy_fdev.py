#!/usr/bin/python
# -*- coding: utf-8 -*-

import json
import os
import sys

import yaml

from utils import Utils


class Deploy:

    def __init__(self):
        self.utils = Utils()
        # 获得部署的目录
        self.DEPLOY_DIR = self.utils.get_file_dir()
        envs = os.environ  # 系统环境变量
        self.CI_COMMIT_REF_NAME = envs['CI_COMMIT_REF_NAME']
        self.CI_REGISTRY_IMAGE = envs['CI_REGISTRY_IMAGE']
        self.CI_PROJECT_ID = envs['CI_PROJECT_ID']
        self.CI_PROJECT_NAME = envs['CI_PROJECT_NAME']

    # SIT
    def deploy7122(self):
        self.utils.cmd(
            "kubectl apply -f " + self.DEPLOY_DIR + "/temp-deployment.yaml  --kubeconfig=/etc/kubernetes/admin-71-22.conf")

    # REL
    def deploy101202(self):
        self.utils.cmd(
            "kubectl apply -f " + self.DEPLOY_DIR + "/temp-deployment.yaml  --kubeconfig=/etc/kubernetes/admin-101-202.conf")

    # PRO
    def deploy101199(self):
        self.utils.cmd(
            "kubectl apply -f " + self.DEPLOY_DIR + "/temp-deployment.yaml  --kubeconfig=/etc/kubernetes/admin-101-199.conf")

    def deploy2Replicas(self):
        deployment = "%s-%s" % (self.CI_PROJECT_NAME, self.CI_ENVIRONMENT_SLUG)
        self.utils.cmd(
            "kubectl scale --kubeconfig=/etc/kubernetes/admin-173.conf -n fdev --replicas=2 deployment %s" % deployment)

    def deploy68228(self):
        self.utils.cmd(
            "kubectl apply -f " + self.DEPLOY_DIR + "/temp-deployment.yaml  --kubeconfig=/etc/kubernetes/admin-68-228.conf")

    def deploy10764(self):
        self.utils.cmd(
            "kubectl apply -f " + self.DEPLOY_DIR + "/temp-deployment.yaml  --kubeconfig=/etc/kubernetes/admin-107-64.conf")

    # 判断 是否 挂载 /fdev/log
    def is_logs_nas(self):
        with open(self.DEPLOY_DIR + "/deployment.yaml", "r") as fr:
            file_content = json.dumps(fr.readlines())
            if file_content.find("mountPath: /fdev/log") < 0:
                return True
            else:
                return False

    # add fileBeat-sidecar
    def insert_file_beat(self):
        if self.is_logs_nas():
            return None
        yaml_str = ""
        tmp_list = []
        fileBeat = {
            "name": "filebeat-sidecar",
            "image": "********/mbpe-public-docker-local/common/filebeat:latest",
            "volumeMounts": [
                {
                    "mountPath": "/fdev/log",
                    "name": "log"
                }
            ],
            "env": [
                {
                    "name": "CI_PROJECT_NAME",
                    "value": "$CI_PROJECT_NAME"
                },
                {
                    "name": "CI_ENVIRONMENT_SLUG",
                    "value": "$CI_ENVIRONMENT_SLUG"
                },
                {
                    "name": "CI_LOG_FILE",
                    "value": "/fdev/log/*HOSTNAME.log"
                },
                {
                    "name": "FDEV_APP_GROUP",
                    "value": "devops"
                },
                {
                    "name": "KAFKA_TOPIC",
                    "value": "fdev-filebeat"
                }
            ]
        }
        with open(self.DEPLOY_DIR + "/deployment.yaml", "r") as fw:
            for line in fw.readlines():
                if line.startswith("---"):
                    tmp_list.append(yaml_str)
                    tmp_list.append(line)
                    yaml_str = ""
                    continue
                yaml_str = yaml_str + line
            tmp_list.append(yaml_str)
            tmp_list = filter(None, tmp_list)
            yaml_str = ""
            for element in tmp_list:
                element = str(element)
                if element.startswith("---"):
                    yaml_str = yaml_str + element
                    continue
                if element.find("containers:") > 0:
                    tmp = yaml.safe_load(element)
                    tmp['spec']['template']['spec']['containers'].append(fileBeat)
                    yaml_str = yaml_str + yaml.safe_dump(tmp)
                else:
                    yaml_str = yaml_str + element
        return yaml_str

    def main(self):
        self.utils.info("开始 deploy..")

        if os.environ.has_key('CI_COMMIT_TAG') == True:
            if os.environ.has_key('FDEV_DEPLOY_PROD') == True:
                self.CI_ENVIRONMENT_SLUG = "master"
                self.CI_REGISTRY_IMAGE = str(self.CI_REGISTRY_IMAGE).replace("library", "fdev-prod")
            else:
                self.CI_ENVIRONMENT_SLUG = "rel"
                self.CI_REGISTRY_IMAGE = str(self.CI_REGISTRY_IMAGE).replace("library", "fdev-rel")
        elif self.CI_COMMIT_REF_NAME[0:7].upper() == "RELEASE":
            self.CI_ENVIRONMENT_SLUG = "uat"
            self.CI_REGISTRY_IMAGE = str(self.CI_REGISTRY_IMAGE).replace("library", "fdev-uat")
        elif self.CI_COMMIT_REF_NAME[0:4].upper() == "TEST":
            self.CI_ENVIRONMENT_SLUG = "test"
            self.CI_REGISTRY_IMAGE = str(self.CI_REGISTRY_IMAGE).replace("library", "fdev-sit")
        elif "2831" == self.CI_PROJECT_ID and self.CI_COMMIT_REF_NAME == "master":
            self.CI_ENVIRONMENT_SLUG = "master"
            self.CI_REGISTRY_IMAGE = str(self.CI_REGISTRY_IMAGE).replace("library", "fdev-prod")
        else:
            self.CI_ENVIRONMENT_SLUG = "sit"
            self.CI_REGISTRY_IMAGE = str(self.CI_REGISTRY_IMAGE).replace("library", "fdev-sit")

        os.environ['CI_ENVIRONMENT_SLUG'] = self.CI_ENVIRONMENT_SLUG
        os.environ['CI_REGISTRY_IMAGE'] = self.CI_REGISTRY_IMAGE

        new_temp_deployment = self.insert_file_beat()
        if new_temp_deployment is not None:
            with open(self.DEPLOY_DIR + "/deployment.yaml", "w") as fw:
                fw.write(new_temp_deployment)
        # 替换系统环境变量
        self.utils.cmd(
            "envsubst < " + self.DEPLOY_DIR + "/deployment.yaml > " + self.DEPLOY_DIR + "/temp-deployment.yaml")
        # 替换 sky owp 地址
        self.utils.cmd(
            "sed -i 's/skywalking地址/g' " + self.DEPLOY_DIR + "/temp-deployment.yaml")
        self.utils.cmd("cat " + self.DEPLOY_DIR + "/temp-deployment.yaml")

        # 申请平台和svn 管理工具
        if "1263" == self.CI_PROJECT_ID or "2015" == self.CI_PROJECT_ID:
            self.deploy7122()
            sys.exit(0)
        # fdev 分类部署
        if str(self.CI_PROJECT_NAME).startswith("fdev") \
                or "899" == self.CI_PROJECT_ID or "11399" == self.CI_PROJECT_ID or "10921" == self.CI_PROJECT_ID \
                or str(self.CI_PROJECT_NAME).startswith("mpaas") \
                or str(self.CI_PROJECT_NAME).startswith("vue"):
            if self.CI_ENVIRONMENT_SLUG == "master":
                self.deploy101199()
            if self.CI_ENVIRONMENT_SLUG == "rel":
                self.deploy101202()
            if self.CI_ENVIRONMENT_SLUG == "test":
                self.deploy7122()
                self.deploy101202()
            if self.CI_ENVIRONMENT_SLUG == "sit":
                self.deploy7122()
                # fdev-vue-admin 触发 mock
                if "1498" == self.CI_PROJECT_ID and str(self.CI_COMMIT_REF_NAME).upper().startswith("SIT"):
                    self.CI_ENVIRONMENT_SLUG = "mock"
                    os.environ['CI_ENVIRONMENT_SLUG'] = self.CI_ENVIRONMENT_SLUG
                    self.utils.cmd(
                        "envsubst < " + self.DEPLOY_DIR + "/deployment.yaml > " + self.DEPLOY_DIR + "/temp-deployment.yaml")
                    self.utils.cmd("cat " + self.DEPLOY_DIR + "/temp-deployment.yaml")
                    self.deploy7122()
        # testmanage 分类部署
        elif str(self.CI_PROJECT_NAME).startswith("testmanage"):
            if self.CI_ENVIRONMENT_SLUG == "master":
                self.deploy101199()
            if self.CI_ENVIRONMENT_SLUG == "rel":
                self.deploy101202()
            if self.CI_ENVIRONMENT_SLUG == "sit":
                self.deploy7122()
        # eye 部署
        elif str(self.CI_PROJECT_NAME).startswith("eye"):
            if self.CI_ENVIRONMENT_SLUG == "master":
                self.deploy10764()
            if self.CI_ENVIRONMENT_SLUG == "sit":
                self.deploy68228()
        # yapi 单独部署
        elif str(self.CI_PROJECT_NAME).startswith("yapi"):
            if self.CI_ENVIRONMENT_SLUG == "master":
                self.deploy101199()

        # 不重构的应用需要部署到新的命名空间
        try:
            context_path = self.utils.project_list.get(self.CI_PROJECT_ID)
        except Exception:
            return
        if context_path:
            self.deploy_new(context_path)

    # 不重构的应用需要部署到新的命名空间
    def deploy_new(self, context_path):
        self.utils.info("========================== new deploy ==========================")

        # 重命名应用名
        os.environ['CI_PROJECT_NAME'] = self.CI_PROJECT_NAME + "-new"
        # 重命名镜像tag
        os.environ['CI_PIPELINE_ID'] = os.environ['CI_PIPELINE_ID'] + "-new"

        # 先替换上下文根，因为有的模块应用名和上下文根是一样的
        with open(self.DEPLOY_DIR + "/deployment.yaml", "r") as fr:
            deployment = fr.read()
            deployment = deployment.replace(context_path, context_path + "-new")
        with open(self.DEPLOY_DIR + "/deployment.yaml", "w") as fw:
            fw.write(deployment)

        # 替换环境变量
        self.utils.cmd(
            "envsubst < " + self.DEPLOY_DIR + "/deployment.yaml > " + self.DEPLOY_DIR + "/temp-deployment.yaml")

        with open(self.DEPLOY_DIR + "/temp-deployment.yaml", "r") as fr:
            new_temp_deployment = fr.read()
        # 替换命名空间
        if new_temp_deployment.__contains__("namespace: fdev"):
            new_temp_deployment = new_temp_deployment.replace("namespace: fdev", "namespace: fdev-new")
        elif new_temp_deployment.__contains__("namespace: testmanage"):
            new_temp_deployment = new_temp_deployment.replace("namespace: testmanage", "namespace: testmanage-new")
        with open(self.DEPLOY_DIR + "/temp-deployment.yaml", "w") as fw:
            fw.write(new_temp_deployment)

        # 替换 sky owp 地址
        self.utils.cmd(
            "sed -i 's/skywalking地址/g' " + self.DEPLOY_DIR + "/temp-deployment.yaml")

        # 打印
        self.utils.cmd("cat " + self.DEPLOY_DIR + "/temp-deployment.yaml")

        if self.CI_ENVIRONMENT_SLUG == "master":
            self.deploy101199()
        if self.CI_ENVIRONMENT_SLUG == "rel":
            self.deploy101202()
        if self.CI_ENVIRONMENT_SLUG == "sit":
            self.deploy7122()
        if self.CI_ENVIRONMENT_SLUG == "test":
            self.deploy7122()
            self.deploy101202()


if __name__ == '__main__':
    deploy = Deploy()
    deploy.main()
