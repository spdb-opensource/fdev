#!/usr/bin/python
# -*- coding: utf-8 -*-
import base64
import json
import os
import sys

from utils import Utils

reload(sys)
sys.setdefaultencoding("utf-8")
print sys.getdefaultencoding()


class CassApi:

    def __init__(self):
        self.envs = os.environ
        self.utils = Utils()
        self.init_fdev_env()
        self.init_gitlab_env()

        self.fdev_flag = True
        self.hands_flag = True
        self.flag = False
        self.base64stringAccess = base64.b64encode('%s:%s' % (self.FDEV_CAAS_ACCESS, self.FDEV_CAAS_SECRET))
        self.base64stringUser = base64.b64encode('%s:%s' % (self.FDEV_CAAS_USER, self.FDEV_CAAS_PWD))
        self.CI_HANDS_VARS = None
        self.CI_VARS = None

    # 初始化 fdev 环境变量
    def init_fdev_env(self):
        self.fdev_url = self.utils.get_fdev_url()
        self.FDEV_CAAS_REGISTRY = self.utils.get_env("FDEV_CAAS_REGISTRY")
        self.FDEV_CAAS_SERVICE_REGISTRY = self.utils.get_env("FDEV_CAAS_SERVICE_REGISTRY")
        self.FDEV_CAAS_REGISTRY_NAMESPACE = self.utils.get_env("FDEV_CAAS_REGISTRY_NAMESPACE")
        self.FDEV_CAAS_ACCESS = self.utils.get_env('FDEV_CAAS_ACCESS')
        self.FDEV_CAAS_SECRET = self.utils.get_env('FDEV_CAAS_SECRET')
        self.FDEV_CAAS_IP = self.utils.get_env('FDEV_CAAS_IP')
        self.FDEV_CAAS_USER = self.utils.get_env('FDEV_CAAS_USER')
        self.FDEV_CAAS_PWD = self.utils.get_env('FDEV_CAAS_PWD')
        self.FDEV_CAAS_TENANT = self.utils.get_env('FDEV_CAAS_TENANT')
        self.CI_CAAS_REGISTRY = self.utils.get_env('FDEV_CAAS_SERVICE_REGISTRY')
        print("FDEV_CAAS_USER: %s, FDEV_CAAS_PWD: %s" % (self.FDEV_CAAS_USER, self.FDEV_CAAS_PWD))

    # 初始化系统环境变量
    def init_gitlab_env(self):
        envs = os.environ  # 系统环境变量
        self.CI_PROJECT_DIR = self.envs.get("CI_PROJECT_DIR")
        self.CI_PROJECT_ID = self.envs.get("CI_PROJECT_ID")
        self.CI_PIPELINE_ID = self.envs.get("CI_PIPELINE_ID")
        self.CI_PROJECT_NAME = self.envs.get("CI_PROJECT_NAME")
        self.CI_API_V4_URL = self.envs.get("CI_API_V4_URL")
        self.CI_COMMIT_SHORT_SHA = self.envs.get("CI_COMMIT_SHORT_SHA")
        # 替换镜像tag
        if envs.has_key("CI_COMMIT_TAG"):
            image_tag = envs["CI_COMMIT_TAG"]
            envs['CI_PIPELINE_ID'] = image_tag
            os.environ['CI_PIPELINE_ID'] = image_tag
            self.utils.info("image_tag: ", image_tag)
        else:
            image_tag = self.utils.get_registry_tag()
            envs['CI_PIPELINE_ID'] = image_tag
            os.environ['CI_PIPELINE_ID'] = image_tag
            self.utils.info("image_tag: ", image_tag)

        # 从环境变量中获得的变量
        envs = os.environ  # 系统环境变量
        self.DEVOPS_DEPLOY_SLUG = self.utils.get_deploy_slug()
        # 将应用名的 -parent 去掉
        self.CI_PROJECT_NAME = self.utils.rename_project_name(envs['CI_PROJECT_NAME'])
        envs['CI_PROJECT_NAME'] = self.CI_PROJECT_NAME
        # 镜像坐标: ip/namepscae/project_name_en
        self.CI_REGISTRY_IMAGE = self.FDEV_CAAS_SERVICE_REGISTRY + "/" + self.FDEV_CAAS_REGISTRY_NAMESPACE + "/" + self.CI_PROJECT_NAME
        os.environ['CI_REGISTRY_IMAGE'] = self.CI_REGISTRY_IMAGE
        self.CI_PIPELINE_ID = envs['CI_PIPELINE_ID']

    # 查询 应用
    def get_app(self):
        headers = {"Content-Type": "application/strategic-merge-patch+json",
                   "Authorization": "Basic " + self.base64stringAccess}
        url = "https://%s/apis/apps/v1/namespaces/%s/deployments/%s" % (
            self.FDEV_CAAS_IP, self.FDEV_CAAS_TENANT, self.CI_PROJECT_NAME)

        return self.utils.request_caas(url=url, headers=headers)

    # 删除 应用
    def delete_app(self):
        url = "https://%s/apis/apps/v1/namespaces/%s/deployments/%s" % (
            self.FDEV_CAAS_IP, self.FDEV_CAAS_TENANT, self.CI_PROJECT_NAME)
        header = {"Content-Type": "application/json",
                  "Authorization": "Basic " + self.base64stringAccess}
        return_data = self.utils.request_caas(url=url, headers=header, method='DELETE')
        self.utils.debug("删除 deployment: ", return_data)

        url = "https://%s/dce/v1/apps/%s" % (self.FDEV_CAAS_IP, self.CI_PROJECT_NAME)
        header = {"Content-Type": "application/json",
                  "x-dce-tenant": self.FDEV_CAAS_TENANT,
                  "Authorization": "Basic " + self.base64stringAccess}
        return_data = self.utils.request_caas(url=url, headers=header, method='DELETE')
        self.utils.debug("删除 app: ", return_data)

    # 创建 应用
    def create_app(self, deployment_json, selector_json):
        url = "https://%s/apis/apps/v1/namespaces/%s/deployments" % (self.FDEV_CAAS_IP, self.FDEV_CAAS_TENANT)
        headers = {"Content-Type": "application/json",
                   "Authorization": "Basic " + self.base64stringAccess}
        deployment_json = self.get_pullSecrets(deployment_json)
        self.utils.request_caas(url=url, headers=headers, data=deployment_json, method='POST')

        # 创建 app ui
        self.utils.info("创建 app ui")
        url = "https://%s/dce/apps" % self.FDEV_CAAS_IP
        headers = {"Content-Type": "application/json",
                   "x-dce-tenant": self.FDEV_CAAS_TENANT,
                   "Authorization": "Basic " + self.base64stringUser}
        self.utils.request_caas(url=url, headers=headers, data=selector_json, method='POST')

    # 更新 的时候只更新镜像
    def updateImage(self):
        image = self.CI_REGISTRY_IMAGE + ":" + self.CI_PIPELINE_ID
        headers = {"Content-Type": "application/strategic-merge-patch+json",
                   "Authorization": "Basic " + self.base64stringAccess}

        requestDate = {
            "metadata": {
                "annotations":
                    {
                        "kubernetes.io/change-cause": "update image"
                    }
            },
            "spec": {
                "template": {
                    "spec": {
                        "containers": [
                            {
                                "name": self.CI_PROJECT_NAME,
                                "image": image
                            }]
                    }
                }
            }
        }
        url = "https://%s/apis/apps/v1/namespaces/%s/deployments/%s" % (
            self.FDEV_CAAS_IP, self.FDEV_CAAS_TENANT, self.CI_PROJECT_NAME)
        return self.utils.request_caas(url=url, headers=headers, data=json.dumps(requestDate), method='PATCH')

    # CaaS重启服务
    def restart_service(self, deployment_json):
        deployment_json = json.loads(deployment_json)
        deployment_json["spec"]["template"]
        headers = {"Content-Type": "application/strategic-merge-patch+json",
                   "Authorization": "Basic " + self.base64stringAccess}
        stopRequestData = {
                            "spec": {
                                "replicas": 0
                            }
                          }
        startRequestData = {
                             "spec": {
                                "replicas": deployment_json["spec"]["replicas"]
                             }
                           }
        url = "https://%s/apis/apps/v1/namespaces/%s/deployments/%s" % (
            self.FDEV_CAAS_IP, self.FDEV_CAAS_TENANT, self.CI_PROJECT_NAME)
        return_data = self.utils.request_caas(url=url, headers=headers, data=json.dumps(stopRequestData), method='PATCH')
        if return_data.get('code') != 200:
            self.utils.error("停止服务 %s 请求失败" %  self.CI_PROJECT_NAME)
            sys.exit(1)
        return_data = self.utils.request_caas(url=url, headers=headers, data=json.dumps(startRequestData), method='PATCH')
        if return_data.get('code') != 200:
            self.utils.error("启动服务 %s 请求失败" %  self.CI_PROJECT_NAME)
            sys.exit(1)

    # 拼接 pullSecret
    def get_pullSecrets(self, deployment_json):
        name = str(self.CI_PROJECT_NAME) + "-" + self.FDEV_CAAS_SERVICE_REGISTRY
        imagePullSecrets = [{"name": name}]
        self.utils.info('imagePullSecrets: ', imagePullSecrets)
        deployment_json = json.loads(deployment_json)
        deployment_json["spec"]["template"]["spec"]["imagePullSecrets"] = imagePullSecrets
        return json.dumps(deployment_json)

    # caaas sececret 检查
    def deploy_cass_checksecret(self):
        self.utils.info("进行 sececret 校验 \n")
        url = "https://" + self.FDEV_CAAS_IP + "/api/v1/namespaces/" + self.FDEV_CAAS_TENANT + "/secrets/" + self.CI_PROJECT_NAME + "-" + self.CI_CAAS_REGISTRY
        headers = {"Content-Type": "application/json",
                   "Authorization": "Basic " + self.base64stringAccess}
        return_data = self.utils.request_caas(url=url, headers=headers)
        code = return_data.get('code')
        data = return_data.get('data')
        if code == 404:
            self.utils.info(self.CI_PROJECT_NAME + " 的secret不存在, 则执行创建操作")
            url = "https://" + self.FDEV_CAAS_IP + "/api/v1/namespaces/" + self.FDEV_CAAS_TENANT + "/secrets"
            auth = base64.b64encode('%s:%s' % (self.FDEV_CAAS_USER, self.FDEV_CAAS_PWD))
            dockerconfigjson = {
                "auths": {
                    str(self.CI_CAAS_REGISTRY):
                        {
                            "auth": auth,
                            "username": self.FDEV_CAAS_USER,
                            "password": self.FDEV_CAAS_PWD
                        }
                }
            }
            # print "dockerconfigjson: ", dockerconfigjson
            secretsData = {
                "apiVersion": "v1",
                "data": {
                    ".dockerconfigjson": base64.b64encode(json.dumps(dockerconfigjson))
                },
                "kind": "Secret",
                "metadata": {
                    "name": self.CI_PROJECT_NAME + "-" + self.CI_CAAS_REGISTRY,
                    "namespace": self.FDEV_CAAS_TENANT
                },
                "type": "kubernetes.io/dockerconfigjson"
            }
            code = self.utils.request_caas(url=url, headers=headers, data=json.dumps(secretsData),
                                           method="POST").get(
                "code")
            self.utils.info("创建secret返回的状态码 ", code)
        else:
            re_data = json.loads(data)
            if re_data != "":
                dockerconfigjson = re_data['data']['.dockerconfigjson']
                doc = json.loads(base64.b64decode(dockerconfigjson))['auths']
                old_ip = dict(doc).keys().pop(0)
                username = dict(doc).get(old_ip).get('username')
                password = dict(doc).get(old_ip).get('password')
                print "old_ip", old_ip
                print "old_username", username
                # print "old_password", password

                if self.CI_CAAS_REGISTRY == str(old_ip) and self.FDEV_CAAS_USER == str(
                        username) and self.FDEV_CAAS_PWD == str(
                    password):
                    print "原有的 secret 与预期符合"
                    return
                else:
                    # 删除原来的 secret
                    print "原有的 secret 与预期不符合，执行移除操作"
                    code = self.utils.request_caas(url=url, headers=headers, method="DELETE").get("code")
                    self.utils.info("移除原有的secret 返回的状态： ", code)
                    url = "https://" + self.FDEV_CAAS_IP + "/api/v1/namespaces/" + self.FDEV_CAAS_TENANT + "/secrets"
                    auth = base64.b64encode('%s:%s' % (self.FDEV_CAAS_USER, self.FDEV_CAAS_PWD))
                    dockerconfigjson = {
                        "auths": {
                            str(self.CI_CAAS_REGISTRY):
                                {
                                    "auth": auth,
                                    "username": self.FDEV_CAAS_USER,
                                    "password": self.FDEV_CAAS_PWD
                                }
                        }
                    }
                    # print "dockerconfigjson: ", dockerconfigjson
                    secretsData = {
                        "apiVersion": "v1",
                        "data": {
                            ".dockerconfigjson": base64.b64encode(json.dumps(dockerconfigjson))
                        },
                        "kind": "Secret",
                        "metadata": {
                            "name": self.CI_PROJECT_NAME + "-" + self.CI_CAAS_REGISTRY,
                            "namespace": self.FDEV_CAAS_TENANT
                        },
                        "type": "kubernetes.io/dockerconfigjson"
                    }
                    code = self.utils.request_caas(url=url, headers=headers, data=json.dumps(secretsData),
                                                   method="POST").get(
                        "code")
                    self.utils.info("创建secret返回的状态码 ", code)
                    return
            self.utils.info(self.CI_PROJECT_NAME + " 的secret查询状态为: ", code)
