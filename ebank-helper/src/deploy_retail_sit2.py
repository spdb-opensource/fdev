#!/usr/bin/python
# -*- coding: utf-8
import base64
import commands
import datetime
import json
import os
import ssl
import sys
import time
import urllib2

import chardet
import re
import yaml

__version__ = "20220401_001"


class Deploy:
    def __init__(self):
        self.info("[+] 镜像版本: " + __version__)
        self.envs = os.environ
        self.flag = False

        self.token = "xxx"
        self.sit_token = "xxx"
        self.success = "AAAAAAA"
        self.file_beat_name = "filebeat-sidecar"
        self.CI_PROJECT_DIR = self.envs.get("CI_PROJECT_DIR")
        self.CI_PROJECT_ID = self.envs.get("CI_PROJECT_ID")
        self.CI_PROJECT_NAME = self.rename_project_name(os.environ.get("CI_PROJECT_NAME"))
        # # http://xxx/api/v4/projects/26475/repository/
        self.CI_API_V4_URL = self.envs.get("CI_API_V4_URL")
        # self.CI_COMMIT_REF_NAME = self.envs.get('CI_COMMIT_REF_NAME')

        self.ci_environment_slug = "sit2-dmz"
        self.CI_VARS = None
        self.CI_SCC_VARS = {}
        self.CI_SCC_SLUG = ["scc-sit2"]
        self.deploy_platform = {"caas_status": "0", "scc_status": "0"}
        self.CONFIG_UPDATE_FLAG = "1"
        self.IMAGE_UPDATE_FLAG = "1"

        # 获得部署的目录
        self.DEPLOY_DIR = self.CI_PROJECT_DIR + "/gitlab-ci"
        if not os.path.exists(self.DEPLOY_DIR):
            self.DEPLOY_DIR = self.CI_PROJECT_DIR + "/ci"
        print("DEPLOY_DIR: %s", self.DEPLOY_DIR)
        # init variables
        self.init_var()

    # 封装日志输出格式及颜色
    def info(self, msg, date1="", date2=""):
        curtime = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        print "\033[1;32m %s [info] %s %s %s \033[0m" % (curtime, msg, date1, date2)

    def warn(self, msg, date1="", date2=""):
        curtime = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        print "\033[1;33m %s [warn] %s %s %s \033[0m" % (curtime, msg, date1, date2)

    def error(self, msg, date1="", date2=""):
        curtime = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        print "\033[1;31m %s [error] %s %s %s \033[0m" % (curtime, msg, date1, date2)

    def init_var(self):
        self.get_deploy_platform()
        if '1' == self.deploy_platform['caas_status']:
            # 初始化 CI_VARS
            self.init_caas_env()
        if '1' == self.deploy_platform['scc_status']:
            # 初始化 CI_SCC_VARS
            self.init_scc_env()

    # 查询应用详情绑定的部署平台
    def get_deploy_platform(self):
        data = json.dumps({"id": int(self.CI_PROJECT_ID)})
        url = self.get_fdev_url() + "/fapp/api/app/getAppByGitId"
        result = self.post_request(url, data)
        code = result['code']
        if code != self.success:
            self.warn("获取持续集成绑定平台信息出错，请联系管理员。")
            self.update_deploy_status("1")
            sys.exit(1)

        self.deploy_platform['caas_status'] = result['data']['caas_status']
        self.deploy_platform['scc_status'] = result['data']['scc_status']

    # 解密 pwd
    def decrypt(self, pwd):
        url = "%s/fenvconfig/api/v2/var/ciDecrypt" % (self.get_fdev_url())
        data = json.dumps({"fdev_caas_pwd": pwd})
        return self.post_request(url, data)

    # 替换 秘钥
    def replace_decrypt(self, data):
        for key, value in data.items():
            if key == "FDEV_CAAS_PWD" or key == "FDEV_CAAS_REGISTRY_PASSWORD":
                data[key] = self.decrypt(value)['data']
        return data

    # 获得 fdev 属于哪个环境,sit,uat,pro
    def get_fdev_slug(self, CI_FDEV_ENV):
        fdev_url = "xxx"
        # fdev uat 接口地址
        fdev_uat_url = "xxx"
        # fdev sit 接口地址
        fdev_sit_url = "xxx"
        # fdev rel 接口地址
        fdev_rel_url = "xxx"
        if CI_FDEV_ENV.upper() == "SIT":
            fdev_url = fdev_sit_url
        elif CI_FDEV_ENV.upper() == "UAT":
            fdev_url = fdev_uat_url
        elif CI_FDEV_ENV.upper() == "REL":
            fdev_url = fdev_rel_url
        return fdev_url

    # 获得 fdev url
    def get_fdev_url(self):
        # 暂时测试环境使用赋值SIT
        # os.environ["CI_FDEV_ENV"] = "REL"
        fdev_url = "xxx"
        if "CI_FDEV_ENV" in os.environ:
            CI_FDEV_ENV = os.environ['CI_FDEV_ENV']
            self.info("CI_FDEV_ENV:", CI_FDEV_ENV)
            return self.get_fdev_slug(CI_FDEV_ENV)
        return fdev_url

    # remove value's -
    def remove_value(self, CI_VARS):
        for key, val in CI_VARS.items():
            if val is "-" or val == "-":
                CI_VARS[key] = ""
        return CI_VARS

    # 初始化查询CaaS实体环境变量
    def init_caas_env(self):
        data = json.dumps(
            {"env": self.ci_environment_slug, "gitlabId": int(self.CI_PROJECT_ID), "image": ""})
        url = self.get_fdev_url() + "/fenvconfig/api/v2/appDeploy/queryVariablesMapping"
        result = self.post_request(url, data)
        code = result['code']
        if code != self.success:
            self.warn("获取持续集成环境变量出错，请联系管理员。")
            sys.exit(1)
        self.CI_VARS = self.replace_decrypt(self.remove_value(result['data']))

    # 重命名 CI_PROJECT_NAME 将 -parent 去掉
    def rename_project_name(self, project_name):
        if project_name[-7:] == "-parent":
            return project_name[:-7]
        return project_name

    # string 不为0、""、None、"0"，返回True
    def check_zero(self, string):
        if string and "0" != string:
            return True
        else:
            return False

    # 初始化查询SCC环境实体变量
    def init_scc_env(self):
        for slug in self.CI_SCC_SLUG:
            data = json.dumps(
                {"env": slug, "gitlabId": int(self.CI_PROJECT_ID), "image": ""})
            url = self.get_fdev_url() + "/fenvconfig/api/v2/appDeploy/querySccVariablesMapping"
            result = self.post_request(url, data)
            code = result['code']
            if code != self.success:
                self.warn("获取持续集成环境SCC变量出错，请联系管理员。")
                self.update_deploy_status("1")
                sys.exit(1)
            # 添加到数组CI_SCC_VARS中
            self.CI_SCC_VARS[slug] = result['data']

    # 获得 caas 环境变量
    def get_env(self, env_name):
        if env_name.startswith("FDEV_CAAS_PVC") and "FDEV_CAAS_PVC" in self.CI_VARS:
            index = int(env_name.split("-")[1:][0].split(".")[0])
            name_en = env_name.split("-")[1:][0].split(".")[1]
            return json.loads(self.CI_VARS["FDEV_CAAS_PVC"])[index][name_en]
        if env_name in self.CI_VARS:
            return self.CI_VARS[env_name] == "" if self.CI_VARS[env_name] == "-" else self.CI_VARS[env_name]
        else:
            self.warn("CaaS环境sit2-dmz的%s变量为空" % env_name)
            return ""

    # 获得 scc 环境变量
    def get_scc_env(self, env_name, slug=""):
        if not slug:
            self.error("查询SCC环境实体信息时环境参数为空!")
            self.update_deploy_status("1")
            sys.exit(1)
        if env_name in self.CI_SCC_VARS[slug]:
            return self.CI_SCC_VARS[slug][env_name] == "" if self.CI_SCC_VARS[slug][env_name] == "-" else \
                self.CI_SCC_VARS[slug][env_name]
        else:
            self.warn("scc环境%s的%s变量为空" % (slug, env_name))
            return ""

    # 更新 的时候只更新镜像
    def update_caas_image(self):
        headers = {"Content-Type": "application/strategic-merge-patch+json",
                   "Authorization": "Basic " + self.base64stringAccess}

        request_date = {
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
                                "image": self.caas_image_path
                            }]
                    }
                }
            }
        }
        url = "https://%s/apis/apps/v1/namespaces/%s/deployments/%s" % (
            self.FDEV_CAAS_IP, self.FDEV_CAAS_TENANT, self.CI_PROJECT_NAME)
        return self.request_caas(url=url, headers=headers, data=json.dumps(request_date), method='PATCH')

    def init_caas_env_var(self):
        self.FDEV_CAAS_ACCESS = self.get_env('FDEV_CAAS_ACCESS')
        self.FDEV_CAAS_SECRET = self.get_env('FDEV_CAAS_SECRET')
        self.FDEV_CAAS_IP = self.get_env('FDEV_CAAS_IP')
        self.FDEV_CAAS_USER = self.get_env('FDEV_CAAS_USER')
        self.FDEV_CAAS_PWD = self.get_env('FDEV_CAAS_PWD')
        self.FDEV_CAAS_TENANT = self.get_env('FDEV_CAAS_TENANT')
        self.CI_CAAS_REGISTRY = self.get_env('FDEV_CAAS_SERVICE_REGISTRY')
        self.FDEV_CAAS_REGISTRY = self.get_env("FDEV_CAAS_REGISTRY")
        self.FDEV_CAAS_SERVICE_REGISTRY = self.get_env("FDEV_CAAS_SERVICE_REGISTRY")
        self.FDEV_CAAS_REGISTRY_NAMESPACE = self.get_env("FDEV_CAAS_REGISTRY_NAMESPACE")

        self.base64stringAccess = base64.b64encode('%s:%s' % (self.FDEV_CAAS_ACCESS, self.FDEV_CAAS_SECRET))
        self.base64stringUser = base64.b64encode('%s:%s' % (self.FDEV_CAAS_USER, self.FDEV_CAAS_PWD))
        print("FDEV_CAAS_USER: %s, FDEV_CAAS_PWD: %s" % (self.FDEV_CAAS_USER, self.FDEV_CAAS_PWD))

    def init_scc_env_var(self, env_slug):
        self.FDEV_SCC_URL = "xxx"
        self.scc_pvc_nastemp = self.get_scc_env('scc_pvc_nastemp', env_slug)
        self.FDEV_SCC_USER_GROUP_CODE = self.get_scc_env('sccdeploy_usergroup', env_slug)
        # self.FDEV_SCC_USER_GROUP_CODE = 'spdb'
        self.FDEV_SCC_NAMESPACE_CODE = self.get_scc_env('sccdeploy_usercode', env_slug)
        # self.FDEV_SCC_NAMESPACE_CODE = 'mbper-sit'
        self.FDEV_SCC_NAMESPACES = self.get_scc_env('sccdeploy_namespace', env_slug)
        # self.FDEV_SCC_NAMESPACES = 'mbper-sit'
        self.FDEV_SCC_CLUSTER_CODES = self.get_scc_env('sccdeploy_clusterlist', env_slug).split(",")
        # self.FDEV_SCC_CLUSTER_CODES = 'k8-phy-b01'.split(",")
        self.HARBOR_ADDRESS = self.get_scc_env('dockerservice_ip', env_slug)
        # self.HARBOR_ADDRESS = 'xxx'
        self.REGION = self.get_scc_env('sccdeploy_region', env_slug)
        # self.REGION = 'sh'
        self.FDEV_SCC_USER_ID = "fdev"
        self.dockerservice_namespace = self.get_scc_env("dockerservice_namespace", env_slug)
        self.image_path = '/' + self.dockerservice_namespace + '/' + self.CI_PROJECT_NAME

    def cmd(self, command):
        print(command)
        (status, output) = commands.getstatusoutput(command)
        # 若没有输出，则不打印
        if output:
            print(output)
        return status, output

    def send_file(self, url, file_path, userGroupCode, namespaces):
        curl_cmd = "curl -X POST -H \"Content-Type: multipart/form-data\" %s " \
                   "-F 'file=@%s' " \
                   "-F 'userId=%s' " \
                   "-F 'userGroupCode=%s' " \
                   "-F 'namespaces=%s' " % (
                       url,
                       file_path,
                       "fdev",
                       userGroupCode,
                       namespaces
                   )
        print("curlCmd: ", curl_cmd)
        return self.cmd(curl_cmd)

    def get_tsccclusterconfig(self):
        headers = {"Content-Type": "application/json"}
        url = "%s/scc/tsccclusterconfig?userGroupCode=%s&resourceKind=Deployment&namespaceCode=%s&namespaces=%s" \
              "&resourceCode=%s" % (
                  self.FDEV_SCC_URL,
                  self.FDEV_SCC_USER_GROUP_CODE,
                  self.FDEV_SCC_NAMESPACE_CODE,
                  self.FDEV_SCC_NAMESPACES,
                  self.rename_project_name(self.CI_PROJECT_NAME)
              )
        return self.request_scc(url=url, headers=headers)

    def post_tsccclusterconfig(self):
        # 资源配置
        clusterCodeList = []
        for code in self.FDEV_SCC_CLUSTER_CODES:
            clusterCodeList.append(code)
        headers = {"Content-Type": "application/json;charset=UTF-8"}
        url = "%s/scc/tsccclusterconfig" % self.FDEV_SCC_URL
        request_data = {
            "resourceCode": self.rename_project_name(self.CI_PROJECT_NAME),
            "resourceKind": "Deployment",
            "userGroupCode": self.FDEV_SCC_USER_GROUP_CODE,
            "namespaceCode": self.FDEV_SCC_NAMESPACE_CODE,
            "clusterCodeList": clusterCodeList,
            "createUid": self.FDEV_SCC_USER_ID
        }
        return self.request_scc(url=url, headers=headers, data=json.dumps(request_data), method="POST")

    def post_tsccmirrorrespository(self):
        headers = {"Content-Type": "application/json;charset=UTF-8"}
        url = "%s/scc/tsccmirrorrespository/batch" % self.FDEV_SCC_URL
        request_data = {
            "containerCode": self.rename_project_name(self.CI_PROJECT_NAME),
            "userGroupCode": self.FDEV_SCC_USER_GROUP_CODE,
            "namespaceCode": self.FDEV_SCC_NAMESPACE_CODE,
            "resourceKind": "Deployment",
            "harborAddressList": [self.HARBOR_ADDRESS],
            "resourceCode": self.rename_project_name(self.CI_PROJECT_NAME),
            "region": self.REGION,
            "path": self.image_path,
            "createUid": self.FDEV_SCC_USER_ID
        }
        return self.request_scc(url=url, headers=headers, data=json.dumps(request_data), method="POST")

    def put_tsccmirrorrespository(self):
        headers = {"Content-Type": "application/json;charset=UTF-8"}
        url = "%s/scc/tsccmirrorrespository/updatebatch" % self.FDEV_SCC_URL
        request_data = {
            "containerCode": self.CI_PROJECT_NAME,
            "userGroupCode": self.FDEV_SCC_USER_GROUP_CODE,
            "namespaceCode": self.FDEV_SCC_NAMESPACE_CODE,
            "resourceKind": "Deployment",
            "harborAddressList": [self.HARBOR_ADDRESS],
            "resourceCode": self.CI_PROJECT_NAME,
            "region": self.REGION,
            "path": self.image_path,
            "createUid": self.FDEV_SCC_USER_ID
        }
        return self.request_scc(url=url, headers=headers, data=json.dumps(request_data), method="PUT")

    def upload_yaml(self):
        url_api_yamlupload = "%s/scc/yaml/upload" % self.FDEV_SCC_URL
        # 替换变量后写入的scc部署文件
        scc_deployment_file = self.DEPLOY_DIR + "/deployment-scc.yaml"
        return self.send_file(url=url_api_yamlupload,
                              file_path=scc_deployment_file,
                              userGroupCode=self.FDEV_SCC_USER_GROUP_CODE,
                              namespaces=self.FDEV_SCC_NAMESPACES)

    # SCC Api调用结果验证
    def scc_api_verify(self, response):
        result_json = json.loads(response.get("data"))
        # 发送api失败直接失败结束
        if response.get("code") != 200 or result_json.get("respCode") != "000":
            return -1
        return 0

    # 将值为 0 的所属的块剔除
    def remove_no_variables(self, deployment_json):
        parsed_json = json.loads(deployment_json)
        result = []
        if parsed_json.has_key("spec") and parsed_json['spec'].has_key("template") and parsed_json['spec'][
            'template'].has_key("spec") and parsed_json['spec']['template']['spec'].has_key("hostAliases"):
            list_hosts = parsed_json['spec']['template']['spec']['hostAliases']
            for host in list_hosts:
                if host['ip']:
                    result.append(host)

            parsed_json['spec']['template']['spec']['hostAliases'] = result
        return parsed_json

    # 替换模板变量
    def replace_yaml_variables(self, s, slug):
        newstring = ''
        start = 0
        match = re.finditer(r"\{\{(.*?)\}\}", s)
        for m in match:
            end, newstart = m.span()
            newstring += s[start:end]
            env_name = m.group(1).strip()
            rep = ""
            if "sit2-dmz" == slug:
                rep = str(self.get_env(env_name))
            else:
                rep = str(self.get_scc_env(env_name, slug))
            if len(rep) == 0:
                self.error("映射值%s未找到: " % env_name)
                self.update_deploy_status("1")
                sys.exit(1)
            newstring += rep
            start = newstart
        newstring += s[start:]
        return newstring

    def get_pods(self):
        headers = {"Content-Type": "application/json;charset=UTF-8"}
        url = "%s/scc/podsmanager?&ownerCode=%s&namespaces=%s&userGroupCode=%s&pageSize=10000" % (
            self.FDEV_SCC_URL,
            self.CI_PROJECT_NAME,
            self.FDEV_SCC_NAMESPACES,
            self.FDEV_SCC_USER_GROUP_CODE
        )
        response = self.request_scc(url=url, headers=headers, data='', method='GET')
        result_json = json.loads(response.get("data"))
        pods_json = result_json["data"]
        before_pods = []
        if pods_json:
            for pod in pods_json:
                before_pods.append(pod.get("podCode"))
        return before_pods

    def get_template_file(self):
        # url = self.CI_API_V4_URL + "projects/13/repository/files/yaml-template%2Fscc%2Eyaml/raw?ref=sit"
        url = self.CI_API_V4_URL + "projects/1659/repository/files/yaml-template%2Fscc%2Eyaml/raw?ref=master"
        # 获取deployment.yaml文件
        if "CI_FDEV_ENV" in os.environ and os.environ.get("CI_FDEV_ENV").upper() == "SIT":
            headers = {"PRIVATE-TOKEN": self.sit_token}
        else:
            headers = {"PRIVATE-TOKEN": self.token}
            
        response = self.request_scc(url=url, headers=headers)
        if response["code"] != 200:
            self.error("从yaml-template获取yaml模板文件失败；url:" + url)
            self.update_deploy_status("1")
            sys.exit(-1)
        return response.get("data")

    def insert_pvc(self, deployment_str):
        if len(self.scc_pvc_nastemp) != 0 and self.scc_pvc_nastemp != 'xxxxx':
            deploy = yaml.load(deployment_str)
            spec_ = deploy['spec']['template']['spec']
            if spec_['volumes'] is None:
                spec_['volumes'] = []
            spec_['volumes'].append(
                {
                    "name": "storage-nastemp-pvc",
                    "persistentVolumeClaim": {
                        "claimName": self.scc_pvc_nastemp
                    }
                }
            )
            container = spec_['containers'][0]
            if container['volumeMounts'] is None:
                container['volumeMounts'] = []
            container['volumeMounts'].append(
                {
                    "mountPath": "/ebank/spdb/temp/",
                    "name": "storage-nastemp-pvc",
                    "subPath": self.CI_PROJECT_NAME
                }
            )
            return yaml.safe_dump(deploy, encoding='utf-8', allow_unicode=True)
        return deployment_str

    # caaas sececret 检查
    def deploy_cass_checksecret(self):
        self.info("进行 sececret 校验 \n")
        url = "https://" + self.FDEV_CAAS_IP + "/api/v1/namespaces/" + self.FDEV_CAAS_TENANT + "/secrets/" + self.CI_PROJECT_NAME + "-" + self.CI_CAAS_REGISTRY
        headers = {"Content-Type": "application/json",
                   "Authorization": "Basic " + self.base64stringAccess}
        return_data = self.request_caas(url=url, headers=headers)
        code = return_data.get('code')
        data = return_data.get('data')
        if code == 404:
            self.info(self.CI_PROJECT_NAME + " 的secret不存在, 则执行创建操作")
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
            code = self.request_caas(url=url, headers=headers, data=json.dumps(secretsData),
                                     method="POST").get("code")
            self.info("创建secret返回的状态码 ", code)
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

                if self.CI_CAAS_REGISTRY == str(old_ip) and self.FDEV_CAAS_USER == str(
                        username) and self.FDEV_CAAS_PWD == str(password):
                    print "原有的 secret 与预期符合"
                    return
                else:
                    # 删除原来的 secret
                    print "原有的 secret 与预期不符合，执行移除操作"
                    code = self.request_caas(url=url, headers=headers, method="DELETE").get("code")
                    self.info("移除原有的secret 返回的状态： ", code)
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
                    code = self.request_caas(url=url, headers=headers, data=json.dumps(secretsData),
                                                   method="POST").get("code")
                    self.info("创建secret返回的状态码 ", code)
                    return
            self.info(self.CI_PROJECT_NAME + " 的secret查询状态为: ", code)

    # 拼接 pullSecret
    def get_pull_secrets(self, deployment_json):
        name = str(self.CI_PROJECT_NAME) + "-" + self.FDEV_CAAS_SERVICE_REGISTRY
        image_pull_secrets = [{"name": name}]
        self.info('imagePullSecrets: ', image_pull_secrets)
        deployment_json = json.loads(deployment_json)
        deployment_json["spec"]["template"]["spec"]["imagePullSecrets"] = image_pull_secrets
        return json.dumps(deployment_json)

    # 查询 应用
    def get_app(self):
        headers = {"Content-Type": "application/strategic-merge-patch+json",
                   "Authorization": "Basic " + self.base64stringAccess}
        url = "https://%s/apis/apps/v1/namespaces/%s/deployments/%s" % (
            self.FDEV_CAAS_IP, self.FDEV_CAAS_TENANT, self.CI_PROJECT_NAME)
        return self.request_caas(url=url, headers=headers)

    # 创建 应用
    def create_app(self, deployment_json, selector_json):
        url = "https://%s/apis/apps/v1/namespaces/%s/deployments" % (self.FDEV_CAAS_IP, self.FDEV_CAAS_TENANT)
        headers = {"Content-Type": "application/json",
                   "Authorization": "Basic " + self.base64stringAccess}
        deployment_json = self.get_pull_secrets(deployment_json)
        self.request_caas(url=url, headers=headers, data=deployment_json, method='POST')

        # 创建 app ui
        self.info("创建 app ui")
        url = "https://%s/dce/apps" % self.FDEV_CAAS_IP
        headers = {"Content-Type": "application/json",
                   "x-dce-tenant": self.FDEV_CAAS_TENANT,
                   "Authorization": "Basic " + self.base64stringUser}
        self.request_caas(url=url, headers=headers, data=selector_json, method='POST')

    # 往 scc 部署
    def deploy_scc(self):
        self.init_scc_env_var("scc-sit2")
        os.environ["fdev_ci_image_tag"] = self.scc_image_path
        template_file = ""
        scc_yaml_path = self.DEPLOY_DIR + "/scc-deployment.yaml"
        if os.path.exists(scc_yaml_path):
            with (open(scc_yaml_path)) as fs:
                template_file = fs.read()
            self.info("当前scc_deployment.yaml从项目部署目录下获取%s" % scc_yaml_path)
        else:
            template_file = self.get_template_file()
            self.info("当前scc_deployment.yaml从ci-template模版获取%s" % scc_yaml_path)
        # 替换系统环境变量
        template_file_env = os.path.expandvars(template_file)
        deployment_str = os.path.expandvars(template_file_env)
        deployment_str = self.replace_yaml_variables(deployment_str, "scc-sit2")
        deployment_str = self.insert_pvc(deployment_str)
        self.info("================= 部署环境%s deployment-scc.yaml =================")
        print deployment_str
        with open(self.DEPLOY_DIR + "/deployment-scc.yaml", 'w') as f:
            f.write(deployment_str)

        # step 1. 检查应用是否已经部署
        self.info("================= 检查应用是否首次部署 =================")
        response = self.get_tsccclusterconfig()
        result_json = json.loads(response.get("data"))
        print(response.get("data"))
        if response.get("code") != 200:
            self.error("接口发送失败")
            print(response.get("data"))
            return "1"
        else:
            if result_json["total"] == 0:
                self.info("================= 应用首次部署，进行资源配置 =================")
                # 资源配置
                response = self.post_tsccclusterconfig()
                if self.scc_api_verify(response) == -1:
                    self.error(response.get("data"))
                    return "1"
                print(response.get("data"))
                # 镜像配置
                self.info("================= 进行镜像配置 =================")
                response = self.post_tsccmirrorrespository()
                if self.scc_api_verify(response) == -1:
                    self.error(response.get("data"))
                    return "1"
                print(response.get("data"))
            else:
                # 更新镜像路径
                self.info("================= 非首次部署，更新镜像路径 =================")
                response = self.put_tsccmirrorrespository()
                if self.scc_api_verify(response) == -1:
                    self.error(response.get("data"))
                    return "1"
                print(response.get("data"))
        # Step 2. 上传 y-yaml
        # if result_json["total"] == 0:
        if True:
            self.info("================= 上传部署yaml文件 =================")
            status, response = self.upload_yaml()
            if status != 0:
                self.error("上传命令执行失败")
                return "1"
            if "上传失败" in response:
                self.error("文件上传失败")
                return "1"
            # Step 3. y-yaml 转换 c-yaml
            self.info("================= yaml文件转换 =================")
            response = self.post_deploy()
            if self.scc_api_verify(response) == -1:
                self.error(response.get("data"))
                return "1"
            print(response.get("data"))
        else:
            self.info("================= yaml文件无变化，deployment换版 =================")
            self.version_change()

        time.sleep(30)
        # step 4. 下发前该资源所有的pod状态
        # before_pods = self.get_pods()
        # Step 5. 下发
        self.info("================= 部署下发 =================")
        response = self.post_cluster()
        if self.scc_api_verify(response) == -1:
            self.error(response.get("data"))
            return "1"
        print(response.get("data"))

        return "0"

    # 往 caas 部署
    def deploy_caas(self):
        self.init_caas_env_var()
        os.environ["fdev_ci_image"] = self.caas_image_path
        os.environ["CI_REGISTRY_IMAGE"] = self.caas_image_path.split(":")[0]
        os.environ["CI_PIPELINE_ID"] = self.caas_image_path.split(":")[1]
        # 替换系统环境变量
        self.cmd(
            "envsubst < " + self.DEPLOY_DIR + "/deployment.yaml > " + self.DEPLOY_DIR + "/temp-deployment.yaml")
        self.cmd("envsubst < " + self.DEPLOY_DIR + "/selector.yaml > " + self.DEPLOY_DIR + "/temp-selector.yaml")
        # 替换模板变量
        template_file = ""
        caas_yaml_path = self.DEPLOY_DIR + "/deployment.yaml"
        if os.path.exists(caas_yaml_path):
            with (open(caas_yaml_path)) as fs:
                template_file = fs.read()
            self.info("当前deployment.yaml从项目部署目录下获取%s" % caas_yaml_path)
        # 替换系统环境变量
        template_file_env = os.path.expandvars(template_file)
        # 替换模板变量
        deployment_str = self.replace_yaml_variables(template_file_env, "sit2-dmz")
        # 添加边车
        deployment_json = json.dumps(yaml.load(deployment_str), indent=4)
        # 将值为 0 的所属的块剔除
        deployment_json = json.dumps(self.remove_no_variables(deployment_json))
        selector_template_file = ""
        caas_selector_path = self.DEPLOY_DIR + "/selector.yaml"
        if os.path.exists(caas_selector_path):
            with (open(caas_selector_path)) as fs:
                selector_template_file = fs.read()
            self.info("当前deployment.yaml从项目部署目录下获取%s" % caas_selector_path)
        # 替换系统环境变量
        template_selector_env = os.path.expandvars(selector_template_file)
        # 替换模板变量
        selector_str = self.replace_yaml_variables(template_selector_env, "sit2-dmz")
        selector_json = json.dumps(yaml.load(selector_str), indent=4)

        self.deploy_cass_checksecret()
        response = self.get_app()
        if response.get("code") == 200:
            self.info('该应用已存在，执行更新操作')
            code = self.update_caas_image()["code"]
            print "put 更新返回 状态码: ", code
            if code == 422:
                self.warn("方案一")
                self.warn("请手动删除在 CaaS 上该应用,然后重试。")
                self.warn("方案二")
                self.warn("请在CaaS手动更新镜像")
                self.update_deploy_status("1")
                sys.exit(1)

        else:
            # 创建 app
            self.info('该应用不存在, 创建 app')
            self.create_app(deployment_json=deployment_json, selector_json=selector_json)

        return "0"

    def version_change(self):
        headers = {"Content-Type": "application/json;charset=UTF-8"}
        name_en = self.rename_project_name(self.CI_PROJECT_NAME)
        url = "%s/scc/deployment/updateimage" % self.FDEV_SCC_URL
        request_data = {
            "containerName": name_en,
            "userGroupCode": self.FDEV_SCC_USER_GROUP_CODE,
            "namespaceCode": self.FDEV_SCC_NAMESPACE_CODE,
            "resourceKind": "Deployment",
            "resourceCode": name_en,
            "newTag": self.caas_image_path,
            "userId": "fdev"
        }
        return self.request_scc(url=url, headers=headers, data=json.dumps(request_data), method="POST")

    def push_config(self, slug):
        fdev_runtime_file = self.DEPLOY_DIR + "/fdev-application.properties"
        if not os.path.exists(fdev_runtime_file):
            self.warn('获取配置模板文件失败，请检查应用配置模板！')
            return
        # step1: 获取项目中的fdev-application.properties文件调用接口上传
        with open(fdev_runtime_file, "r") as fr:
            origin_file_content = fr.read()

        host_ip = self.FDEV_CONFIG_HOST1_IP if self.check_zero(
            self.FDEV_CONFIG_HOST1_IP) else self.FDEV_CONFIG_HOST2_IP
        # 获取配置文件信息 并上传至配置中心
        try:
            data = json.dumps(
                {"env_name": slug, "content": str(origin_file_content), "type": "1",
                 "project_id": str(self.CI_PROJECT_ID), "ci_project_name": self.CI_PROJECT_NAME,
                 "fdev_config_dir": self.FDEV_CONFIG_DIR, "fdev_config_host_ip": host_ip,
                 "fdev_config_user": self.FDEV_CONFIG_USER,
                 "fdev_config_password": self.FDEV_CONFIG_PASSWORD})
        except Exception as e:
            encoding = chardet.detect(origin_file_content)['encoding']
            self.info("encoding:" + encoding)
            if encoding != self.ENCODE_UTF8:
                self.error('=========================================================================')
                self.error('  外部配置文件（fdev-application.properties）编码必须为utf-8！  ')
                self.error('=========================================================================')
            else:
                self.error(e.message)
                self.update_deploy_status("1")
            sys.exit(-1)
        url = self.get_fdev_url() + "/fenvconfig/api/v2/configfile/previewConfigFile"
        response_data = self.post_request(url, data)
        if self.success != response_data["code"]:
            self.error('调环境配置模块接口previewConfigFile出错：', response_data['msg'])
            self.update_deploy_status("1")
            sys.exit(-1)

    # 封装网络请求
    def post_request(self, url, data):

        headers = {"Content-Type": "application/json;charset=UTF-8",
                   "source": "back"}

        self.info("url:", url)
        print "headers:", headers
        print "data:", data
        print "method: POST"

        request = urllib2.Request(
            url=url,
            headers=headers,
            data=data,
        )
        request.get_method = lambda: 'POST'
        try:
            response = urllib2.urlopen(request, timeout=180)
            code = response.getcode()
            return_date = response.read()
            self.info("code:", code)
            print "response:%s" % (json.loads(return_date))
            return json.loads(return_date)

        except urllib2.URLError as e:
            if hasattr(e, 'reason'):
                self.warn('无法连接到服务器,请重试')
                print 'Reason: ', e.reason
                self.update_deploy_status("1")
                sys.exit(-1)
            elif hasattr(e, 'code'):
                self.error('服务器返回异常状态码')
                self.error('Error code: ', e.code)
                self.update_deploy_status("1")
                sys.exit(-1)

    # 封装 scc 网络请求
    def request_scc(self, url, headers, data='', method='GET'):
        return_data = {}
        request = urllib2.Request(
            url=url,
            headers=headers,
            data=data,
        )
        request.get_method = lambda: method
        self.info(url)
        print("method:" + method)
        print("headers:" + str(headers))
        print("request_data:" + str(data))

        try:
            response = urllib2.urlopen(request, timeout=60)
            return_data['data'] = response.read()
            return_data['code'] = response.getcode()
            print(return_data)
            return return_data
        except urllib2.HTTPError as e:
            return_data['code'] = e.code
            self.error('服务器返回异常状态码')
            self.error("errorReason: ", e.reason)
            return return_data
        except urllib2.URLError as e:
            return_data['code'] = 600
            self.error('无法连接到服务器,请重试')
            self.error("errorReason: ", e.reason)
            return return_data

    # 封装 caas 网络请求
    def request_caas(self, url, headers, data='', method='GET'):
        self.info("url:", url)
        self.info("headers:", headers)
        self.info("data:", data)
        self.info("method:", method)

        return_data = {}
        request = urllib2.Request(
            url=url,
            headers=headers,
            data=data,
        )
        request.get_method = lambda: method
        try:
            response = urllib2.urlopen(request, context=ssl._create_unverified_context(), timeout=60)
            return_data['data'] = response.read()
            return_data['code'] = response.getcode()
            self.info("code:", return_data.get('code'))
            self.info(return_data)
            return return_data
        except urllib2.HTTPError as e:
            return_data['code'] = e.code
            self.warn('Error code: ', return_data.get('code'))
            self.warn(e)
            return return_data
        except urllib2.URLError as e:
            self.error('无法连接到服务器,请重试')
            self.error('Reason: ', e.reason)
            self.update_deploy_status("1")
            sys.exit(1)

    def update_deploy_status(self, status):
        # 部署完成更新部署状态
        send_url = self.get_fdev_url() + "/ftask/api/deploy/modifyDeployStatus"
        data = json.dumps({"appName": self.rename_project_name(self.CI_PROJECT_NAME), "deployStatus": status})
        response_data = self.post_request(send_url, data)
        if self.success != response_data["code"]:
            self.warn('调应用模块接口updateAppDeloyStatus出错：', response_data['msg'])
            self.update_deploy_status("1")
            sys.exit(-1)

    def post_deploy(self):
        headers = {"Content-Type": "application/json;charset=UTF-8"}
        url = "%s/scc/yaml/deploy" % self.FDEV_SCC_URL
        request_data = {
            "resourceCode": self.CI_PROJECT_NAME,
            "resourceKind": "Deployment",
            "userGroupCode": self.FDEV_SCC_USER_GROUP_CODE,
            "namespaceCode": self.FDEV_SCC_NAMESPACE_CODE,
            "yamlName": self.CI_PROJECT_NAME + "_" + self.FDEV_SCC_NAMESPACE_CODE + "_Deployment.yaml",
            "userId": self.FDEV_SCC_USER_ID
        }
        return self.request_scc(url=url, headers=headers, data=json.dumps(request_data), method="POST")

    def post_cluster(self):
        headers = {"Content-Type": "application/json;charset=UTF-8"}
        url = "%s/scc/deployment/cluster" % (self.FDEV_SCC_URL)
        resourceClusterList = []
        for code in self.FDEV_SCC_CLUSTER_CODES:
            resourceCluster = {
                "userGroupCode": self.FDEV_SCC_USER_GROUP_CODE,
                "namespaceCode": self.FDEV_SCC_NAMESPACE_CODE,
                "resourceCode": self.CI_PROJECT_NAME,
                "resourceKind": "Deployment",
                "clusterCode": code
            }
            resourceClusterList.append(resourceCluster)
        request_data = {
            "resourceClusterList": resourceClusterList,
            "action": "commit",
            "userId": self.FDEV_SCC_USER_ID
        }
        return self.request_scc(url=url, headers=headers, data=json.dumps(request_data), method="POST")

    def main(self):
        # 检查环境部署开关
        if "0" == self.deploy_platform['caas_status'] and "0" == self.deploy_platform['scc_status']:
            self.error("当前应用尚未打开环境部署开关，请联系应用负责人移步到环境配置管理下的部署信息打开开关！")
            self.update_deploy_status("1")
            sys.exit(-1)

        if "1" == self.deploy_platform['caas_status']:
            self.FDEV_CONFIG_HOST1_IP = self.get_env('FDEV_CONFIG_HOST1_IP')
            self.FDEV_CONFIG_HOST2_IP = self.get_env('FDEV_CONFIG_HOST2_IP')
            self.FDEV_CONFIG_USER = self.get_env('FDEV_CONFIG_USER')
            self.FDEV_CONFIG_PASSWORD = self.get_env('FDEV_CONFIG_PASSWORD')
            self.FDEV_CONFIG_DIR = self.get_env('FDEV_CONFIG_DIR')
            self.FDEV_CONFIG_PORT = self.get_env('FDEV_CONFIG_PORT')
            self.push_config("sit2-dmz")
            # step2: 获取项目中的sit1环境最新的镜像名称更新镜像部署项目
            send_url = self.get_fdev_url() + "/ftask/api/deploy/queryAppImage"
            data = json.dumps({"appName": self.rename_project_name(self.CI_PROJECT_NAME)})
            response_data = self.post_request(send_url, data)
            if self.success != response_data["code"]:
                self.error('调应用模块接口queryAppImage出错：', response_data['msg'])
                self.update_deploy_status("1")
                sys.exit(-1)
            if response_data["data"]["imageCaasUrl"]:
                self.caas_image_path = response_data["data"]["imageCaasUrl"]
            else:
                self.error("接口queryAppImage查询到的镜像名称imageCaasUrl为空")
                self.update_deploy_status("1")
                sys.exit(-1)
            try:
                status = self.deploy_caas()
            except Exception as e:
                self.update_deploy_status("1")
                self.error(e.message)
                sys.exit(-1)
            if status != "0":
                self.error('调应用CaaS接口更新镜像出错：', response_data['msg'])
                self.update_deploy_status("1")
                sys.exit(-1)
        if "1" == self.deploy_platform['scc_status']:
            self.FDEV_CONFIG_HOST1_IP = self.get_scc_env('FDEV_CONFIG_HOST1_IP', "scc-sit2")
            self.FDEV_CONFIG_HOST2_IP = self.get_scc_env('FDEV_CONFIG_HOST2_IP', "scc-sit2")
            self.FDEV_CONFIG_USER = self.get_scc_env('FDEV_CONFIG_USER', "scc-sit2")
            self.FDEV_CONFIG_PASSWORD = self.get_scc_env('FDEV_CONFIG_PASSWORD', "scc-sit2")
            self.FDEV_CONFIG_DIR = self.get_scc_env('FDEV_CONFIG_DIR', "scc-sit2")
            self.FDEV_CONFIG_PORT = self.get_scc_env('FDEV_CONFIG_PORT', "scc-sit2")
            self.push_config("scc-sit2")

            # step2: 获取项目中的sit1环境最新的镜像名称更新镜像部署项目
            send_url = self.get_fdev_url() + "/ftask/api/deploy/queryAppImage"
            data = json.dumps({"appName": self.rename_project_name(self.CI_PROJECT_NAME)})
            response_data = self.post_request(send_url, data)
            print(response_data["data"]["imageSccUrl"])
            if self.success != response_data["code"]:
                self.error('调接口queryAppImage出错：', response_data['msg'])
                self.update_deploy_status("1")
                sys.exit(-1)
            if response_data["data"]["imageSccUrl"]:
                self.scc_image_path = response_data["data"]["imageSccUrl"].split(":")[1]
            else:
                self.error("接口queryAppImage查询到的镜像名称imageSccUrl为空")
                self.update_deploy_status("1")
                sys.exit(-1)
            try:
                status = self.deploy_scc()
            except Exception as e:
                self.update_deploy_status("1")
                self.error(e.message)
                sys.exit(-1)
            if status != "0":
                self.error('调应用SCC接口更新镜像出错：', response_data['msg'])
                self.update_deploy_status("1")
                sys.exit(-1)
        # 部署完成修改状态成功
        self.update_deploy_status("2")


if __name__ == '__main__':
    if 'CI_FDEV_ENV' not in os.environ:
        deploy = Deploy()
        cur_time = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        print "\033[1;32m %s [info] =================开始部署SIT2环境================= \033[0m" % cur_time
        deploy.main()
