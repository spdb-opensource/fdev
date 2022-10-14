#!/usr/bin/python
# -*- coding: utf-8
import commands
import json
import os
import re
import sys
import time
import urllib2

import chardet
import yaml

from utils import Utils


def name_en_contains(*args):
    def env_con(env):
        return all(x in env["name_en"] for x in args)

    return env_con


class Scc:

    def __init__(self):
        self.CI_FDEV_ENV = os.environ.get('CI_FDEV_ENV', "")
        self.utils = Utils()
        # 获得部署的目录
        self.DEPLOY_DIR = self.utils.get_file_dir()
        self.name_en = self.utils.rename_project_name(self.utils.CI_PROJECT_NAME)
        os.environ['CI_PROJECT_NAME'] = self.name_en

    def init_scc_env_var(self, env_slug):
        self.FDEV_SCC_URL = "xxx"
        self.scc_pvc_nastemp = self.get_env('scc_pvc_nastemp', env_slug)
        self.FDEV_SCC_USER_GROUP_CODE = self.get_env('sccdeploy_usergroup', env_slug)
        # self.FDEV_SCC_USER_GROUP_CODE = 'spdb'
        self.FDEV_SCC_NAMESPACE_CODE = self.get_env('sccdeploy_usercode', env_slug)
        # self.FDEV_SCC_NAMESPACE_CODE = 'mbper-sit'
        self.FDEV_SCC_NAMESPACES = self.get_env('sccdeploy_namespace', env_slug)
        # self.FDEV_SCC_NAMESPACES = 'mbper-sit'
        self.FDEV_SCC_CLUSTER_CODES = self.get_env('sccdeploy_clusterlist', env_slug).split(",")
        # self.FDEV_SCC_CLUSTER_CODES = 'k8-phy-b01'.split(",")
        self.HARBOR_ADDRESS = self.get_env('dockerservice_ip', env_slug)
        # self.HARBOR_ADDRESS = 'xxx'
        self.REGION = self.get_env('sccdeploy_region', env_slug)
        # self.REGION = 'sh'
        self.FDEV_SCC_USER_ID = "fdev"
        self.dockerservice_namespace = self.get_env("dockerservice_namespace", env_slug)
        self.image_path = '/' + self.dockerservice_namespace + '/' + self.name_en

    # 判断 应用是否为微服务
    def app_info(self):
        url = "%s/fapp/api/app/query" % (self.utils.get_fdev_url())
        data = json.dumps({"gitlab_project_id": self.utils.CI_PROJECT_ID})
        response = self.utils.post_request(url, data)
        if "AAAAAAA" == response['code'] and len(response['data']):
            return response['data'][0]
        else:
            self.utils.warn("获取应用信息出错。")
            sys.exit(1)

    # 封装执行命令
    def cmd(self, command):
        (status, output) = commands.getstatusoutput(command)
        # 若没有输出，则不打印
        if output:
            print(output)
        return status, output

    def get_env(self, env_name, env_slug):
        return self.utils.get_scc_env(env_name, env_slug)

    def send_file(self, url, file_path, userId, userGroupCode, namespaces):
        curlCmd = "curl -X POST -H \"Content-Type: multipart/form-data\" %s " \
                  "-F 'file=@%s' " \
                  "-F 'userId=%s' " \
                  "-F 'userGroupCode=%s' " \
                  "-F 'namespaces=%s' " % (
                      url,
                      file_path,
                      userId,
                      userGroupCode,
                      namespaces
                  )
        print("curlCmd: ", curlCmd)
        return self.cmd(curlCmd)

    def get_tsccclusterconfig(self):
        headers = {"Content-Type": "application/json"}
        url = "%s/scc/tsccclusterconfig?userGroupCode=%s&resourceKind=Deployment&namespaceCode=%s&namespaces=%s" \
              "&resourceCode=%s" % (
                  self.FDEV_SCC_URL,
                  self.FDEV_SCC_USER_GROUP_CODE,
                  self.FDEV_SCC_NAMESPACE_CODE,
                  self.FDEV_SCC_NAMESPACES,
                  self.name_en
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
            "resourceCode": self.name_en,
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
            "containerCode": self.name_en,
            "userGroupCode": self.FDEV_SCC_USER_GROUP_CODE,
            "namespaceCode": self.FDEV_SCC_NAMESPACE_CODE,
            "resourceKind": "Deployment",
            "harborAddressList": [self.HARBOR_ADDRESS],
            "resourceCode": self.name_en,
            "region": self.REGION,
            "path": self.image_path,
            "createUid": self.FDEV_SCC_USER_ID
        }
        return self.request_scc(url=url, headers=headers, data=json.dumps(request_data), method="POST")

    def put_tsccmirrorrespository(self):
        headers = {"Content-Type": "application/json;charset=UTF-8"}
        url = "%s/scc/tsccmirrorrespository/updatebatch" % self.FDEV_SCC_URL
        request_data = {
            "containerCode": self.name_en,
            "userGroupCode": self.FDEV_SCC_USER_GROUP_CODE,
            "namespaceCode": self.FDEV_SCC_NAMESPACE_CODE,
            "resourceKind": "Deployment",
            "harborAddressList": [self.HARBOR_ADDRESS],
            "resourceCode": self.name_en,
            "region": self.REGION,
            "path": self.image_path,
            "createUid": self.FDEV_SCC_USER_ID
        }
        return self.request_scc(url=url, headers=headers, data=json.dumps(request_data), method="PUT")

    def upload_yaml(self):
        url_api_yamlupload = "%s/scc/yaml/upload" % self.FDEV_SCC_URL
        scc_deployment_file = self.DEPLOY_DIR + "/deployment-scc.yaml"
        return self.send_file(url=url_api_yamlupload,
                              file_path=scc_deployment_file,
                              userId=self.FDEV_SCC_USER_ID,
                              userGroupCode=self.FDEV_SCC_USER_GROUP_CODE,
                              namespaces=self.FDEV_SCC_NAMESPACES)

    def version_change(self):
        url = "%s/scc/deployment/updateimage" % self.FDEV_SCC_URL
        headers = {"Content-Type": "application/json;charset=UTF-8"}
        request_data = {
            "containerName": self.name_en,
            "userGroupCode": self.FDEV_SCC_USER_GROUP_CODE,
            "namespaceCode": self.FDEV_SCC_NAMESPACE_CODE,
            "resourceKind": "Deployment",
            "resourceCode": self.name_en,
            "newTag": os.environ['fdev_ci_image_tag'],
            "userId": self.FDEV_SCC_USER_ID
        }
        return self.request_scc(url=url, headers=headers, data=json.dumps(request_data), method="POST")

    # 封装 scc 网络请求
    def request_scc(self, url, headers, data='', method='GET'):
        return_data = {}
        request = urllib2.Request(
            url=url,
            headers=headers,
            data=data,
        )
        request.get_method = lambda: method
        self.utils.info(url)
        print("method:" + method)
        print("headers:" + str(headers))
        print("request_data:" + str(data))

        try:
            response = urllib2.urlopen(request, timeout=60)
            return_data['data'] = response.read()
            return_data['code'] = response.getcode()
            return return_data
        except urllib2.HTTPError as e:
            return_data['code'] = e.code
            self.utils.error('服务器返回异常状态码')
            self.utils.error("errorReason: ", e.reason)
            return return_data
        except urllib2.URLError as e:
            return_data['code'] = 600
            self.utils.error('无法连接到服务器,请重试')
            self.utils.error("errorReason: ", e.reason)
            return return_data

    # SCC Api调用结果验证
    def scc_api_verify(self, response):
        result_json = json.loads(response.get("data"))
        # 发送api失败直接失败结束
        if response.get("code") != 200 or result_json.get("respCode") != "000":
            return -1
        return 0

    # 替换模板变量
    def replace_yaml_variables(self, s, slug):
        newstring = ''
        start = 0
        match = re.finditer(r"\{\{(.*?)\}\}", s)
        for m in match:
            end, newstart = m.span()
            newstring += s[start:end]
            env_name = m.group(1).strip()
            rep = str(self.get_env(env_name, slug))
            if len(rep) == 0:
                self.utils.error("映射值%s未找到: " % env_name)
                sys.exit(1)
            newstring += rep
            start = newstart
        newstring += s[start:]
        return newstring

    # 往 scc 部署
    def deploy_scc(self):
        # step 1. 检查应用是否已经部署
        self.utils.info("================= 检查应用是否首次部署 =================")
        response = self.get_tsccclusterconfig()
        result_json = json.loads(response.get("data"))
        print(response.get("data"))
        if self.scc_api_verify(response) == -1:
            self.utils.error("接口发送失败")
            sys.exit(1)
        else:
            if result_json["total"] == 0:
                self.utils.info("================= 应用首次部署，进行资源配置 =================")
                # 资源配置
                response = self.post_tsccclusterconfig()
                if self.scc_api_verify(response) == -1:
                    self.utils.error(response.get("data"))
                    sys.exit(1)
                print(response.get("data"))
                # 镜像配置
                self.utils.info("================= 进行镜像配置 =================")
                response = self.post_tsccmirrorrespository()
                if self.scc_api_verify(response) == -1:
                    self.utils.error(response.get("data"))
                    sys.exit(1)
                print(response.get("data"))
            else:
                # 更新镜像路径
                self.utils.info("================= 非首次部署，更新镜像路径 =================")
                response = self.put_tsccmirrorrespository()
                if self.scc_api_verify(response) == -1:
                    self.utils.error(response.get("data"))
                    sys.exit(1)
                print(response.get("data"))
        # Step 2. 上传 y-yaml
        if self.utils.is_commit_scc_deployment() or result_json["total"] == 0 or "scc-ate" == self.utils.CI_ENVIRONMENT_SLUG:
            self.utils.info("================= 上传部署yaml文件 =================")
            status, response = self.upload_yaml()
            if status != 0:
                self.utils.error("上传命令执行失败")
                sys.exit(1)
            if "上传失败" in response:
                self.utils.error("文件上传失败")
                sys.exit(1)
            # Step 3. y-yaml 转换 c-yaml
            self.utils.info("================= yaml文件转换 =================")
            response = self.post_deploy()
            if self.scc_api_verify(response) == -1:
                self.utils.error(response.get("data"))
                sys.exit(1)
            print(response.get("data"))
        else:
            self.utils.info("================= yaml文件无变化，deployment换版 =================")
            self.version_change()

        time.sleep(30)
        # step 4. 下发前该资源所有的pod状态
        before_pods = self.get_pods()
        # Step 5. 下发
        self.utils.info("================= 部署下发 =================")
        response = self.post_cluster()
        if self.scc_api_verify(response) == -1:
            self.utils.error(response.get("data"))
            sys.exit(1)
        print(response.get("data"))

        # step 6 查看下发后对应集群的pod状态
        self.utils.info("================= 查询pod启动状态 =================")
        headers = {"Content-Type": "application/json;charset=UTF-8"}
        url = "%s/scc/podsmanager?&ownerCode=%s&namespaces=%s&userGroupCode=%s&pageSize=10000" % (
            self.FDEV_SCC_URL,
            self.name_en,
            self.FDEV_SCC_NAMESPACES,
            self.FDEV_SCC_USER_GROUP_CODE
        )
        self.utils.info("url:", url)
        success_num = 0
        times = 0
        # step 6.1 获取副本数
        absolute_file_path = self.DEPLOY_DIR + "/deployment-scc.yaml"
        f = open(absolute_file_path)
        yaml_content = yaml.load(f)
        replicas = yaml_content.get("spec").get("replicas")
        while True:
            time.sleep(30)
            response, status = self.utils.request_get_pod(url=url, headers=headers)
            result_json = json.loads(response.get("data"))
            pods_json = result_json["data"]
            if pods_json:
                for pod in pods_json:
                    for cluster in self.FDEV_SCC_CLUSTER_CODES:
                        if cluster == pod.get("clusterCode") and pod.get("status") == "Running":
                            if not pod.get("podCode") in before_pods and pod.get("status") == "Running":
                                success_num += 1
                                before_pods.append(pod.get("podCode"))
                                self.utils.debug(self.name_en + "在租户：" + self.FDEV_SCC_NAMESPACE_CODE + \
                                                 " 集群：" + cluster + " 节点：" + pod.get("nodeName") + "部署成功")
            if times >= 5:
                self.utils.warn("查询部署情况超时，请移至SCC平台自行查看")
                break
            elif replicas == success_num:
                self.utils.debug("部署已按预期完成")
                break
            times += 1

        return 0

    def post_cluster(self):
        headers = {"Content-Type": "application/json;charset=UTF-8"}
        url = "%s/scc/deployment/cluster" % (self.FDEV_SCC_URL)
        resourceClusterList = []
        for code in self.FDEV_SCC_CLUSTER_CODES:
            resourceCluster = {
                "userGroupCode": self.FDEV_SCC_USER_GROUP_CODE,
                "namespaceCode": self.FDEV_SCC_NAMESPACE_CODE,
                "resourceCode": self.name_en,
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

    def restart_service(self):
        headers = {"Content-Type": "application/json;charset=UTF-8"}
        url = "%s/scc/deployment/cluster" % (self.FDEV_SCC_URL)
        resourceClusterList = []
        for code in self.FDEV_SCC_CLUSTER_CODES:
            resourceCluster = {
                "userGroupCode": self.FDEV_SCC_USER_GROUP_CODE,
                "namespaceCode": self.FDEV_SCC_NAMESPACE_CODE,
                "resourceCode": self.name_en,
                "resourceKind": "Deployment",
                "clusterCode": code
            }
            resourceClusterList.append(resourceCluster)
        request_data = {
                            "resourceClusterList": resourceClusterList,
                            "action": "restart",
                            "userId": self.FDEV_SCC_USER_ID
                        }
        return self.request_scc(url=url, headers=headers, data=json.dumps(request_data), method="POST")

    def get_pods(self):
        headers = {"Content-Type": "application/json;charset=UTF-8"}
        url = "%s/scc/podsmanager?&ownerCode=%s&namespaces=%s&userGroupCode=%s&pageSize=10000" % (
            self.FDEV_SCC_URL,
            self.name_en,
            self.FDEV_SCC_NAMESPACES,
            self.FDEV_SCC_USER_GROUP_CODE
        )
        response = self.request_scc(url=url, headers=headers)
        result_json = json.loads(response.get("data"))
        pods_json = result_json["data"]
        before_pods = []
        if pods_json:
            for pod in pods_json:
                before_pods.append(pod.get("podCode"))
        return before_pods

    def post_deploy(self):
        headers = {"Content-Type": "application/json;charset=UTF-8"}
        url = "%s/scc/yaml/deploy" % self.FDEV_SCC_URL
        request_data = {
            "resourceCode": self.name_en,
            "resourceKind": "Deployment",
            "userGroupCode": self.FDEV_SCC_USER_GROUP_CODE,
            "namespaceCode": self.FDEV_SCC_NAMESPACE_CODE,
            "yamlName": self.name_en + "_" + self.FDEV_SCC_NAMESPACE_CODE + "_Deployment.yaml",
            "userId": self.FDEV_SCC_USER_ID
        }
        return self.request_scc(url=url, headers=headers, data=json.dumps(request_data), method="POST")

    def get_template_file(self):
        if self.CI_FDEV_ENV.upper() == "SIT":
            url = self.utils.CI_API_V4_URL + "/projects/13/repository/files/yaml-template%2Fscc%2Eyaml/raw?ref=sit"
        elif self.CI_FDEV_ENV.upper() == "REL":
            url = self.utils.CI_API_V4_URL + "/projects/1659/repository/files/yaml-template%2Fscc%2Eyaml/raw?ref=rel"
        else:
            url = self.utils.CI_API_V4_URL + "/projects/1659/repository/files/yaml-template%2Fscc%2Eyaml/raw?ref=master"
        # feature = "sit"
        # 获取deployment.yaml文件
        headers = {"PRIVATE-TOKEN": self.utils.is_SIT_or_master()}
        response, status = self.utils.request_get_pod(url=url, headers=headers)
        if status != 0:
            self.utils.debug("================= 获取SCC部署文件模板 =================")
            print("method:" + "GET")
            self.utils.info("url:" + url)
            return status
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
                    "subPath": self.name_en
                }
            )
            return yaml.safe_dump(deploy, encoding='utf-8', allow_unicode=True)
        return deployment_str

    # 查询正在运行的yaml文件中env变量
    def query_yaml_envs(self):
        cur_envs = []
        # url = "xxx/scc/deployment/curYaml?namespaceCode=effapp-sit&userGroupCode=spdb&resourceCode=eff-web-iams&resourceKind=Deployment&clusterCode=k8-phy-b01&userId=fdev"
        url = "xxx/scc/deployment/curYaml?namespaceCode=%s&userGroupCode=%s&resourceCode=%s&resourceKind=Deployment&clusterCode=%s&userId=%s"
        url = url % (self.FDEV_SCC_NAMESPACE_CODE, self.FDEV_SCC_USER_GROUP_CODE, self.name_en, self.FDEV_SCC_CLUSTER_CODES[0], self.FDEV_SCC_USER_ID)
        headers = {"source": "back"}
        response_data = self.utils.request_caas(url, headers)
        print response_data["data"]
        remote_data_json = json.loads(response_data['data'])
        if not response_data['code'] == 200 or not remote_data_json["respCode"] == "000":
            return cur_envs
        if json.loads(response_data['data'])["data"].startswith("apiVersion"):
            yaml_content = yaml.load(json.loads(response_data['data'])["data"])
        else:
            yaml_content = yaml.load(json.loads(response_data['data'])["data"][50:])

        if "env" in yaml_content["spec"]["template"]["spec"]["containers"][0]:
            cur_envs = yaml_content["spec"]["template"]["spec"]["containers"][0]["env"]

        return cur_envs

    def replace_env(self, deployment_str):
        deploy_yaml = yaml.load(deployment_str)
        new_envs = []
        if "env" in deploy_yaml["spec"]["template"]["spec"]["containers"][0]:
            env_list = deploy_yaml["spec"]["template"]["spec"]["containers"][0]['env']
            for item in env_list:
                if item['name'] == "CFG_SVR_URL":
                    print item['value']
                else:
                    new_envs.append(item)

        esf_config = None
        try:
            url = "xxx/api/batchChange/queryIamsConfigByName"
            data = json.dumps({"appName": self.name_en})
            response_data = self.utils.post_request(url, data)
            if response_data["code"] == "AAAAAAA" and not response_data['data'] is None and len(response_data["data"]["esfConfig"]) > 0:
                esf_config = response_data["data"]["esfConfig"]
        except Exception as e:
            self.utils.error("发送挡板请求接口异常:", e.message)
            return deployment_str

        if esf_config is not None:
            new_config = {}
            new_config["name"] = "CFG_SVR_URL"
            new_config["value"] = esf_config
            new_envs.append(new_config)

        remote_envs = self.query_yaml_envs()
        for param in remote_envs:
            flag = True
            if esf_config is None and param["name"] == "CFG_SVR_URL":
                flag = False
            for env in new_envs:
                if param["name"] == env["name"]:
                    flag = False
            if flag:
                new_envs.append(param)

        deploy_yaml["spec"]["template"]["spec"]["containers"][0]['env'] = new_envs

        return yaml.safe_dump(deploy_yaml, encoding='utf-8', allow_unicode=True)

    def push_config(self, deployment_str):
        # 调后台接口生成对应环境配置文件，并上传至配置中心
        # self.previewConfigFile()
        self.fdev_runtime_file = self.DEPLOY_DIR + "/fdev-application.properties"
        envs = os.environ
        # 若是 tag 打出的包 需要发送后台去保存配置文件和发邮件
        if envs.has_key('CI_COMMIT_TAG'):
            # 上传到gitlab
            if not os.path.exists(self.fdev_runtime_file):
                originFileContent = "#"
            else:
                with open(self.fdev_runtime_file, "r") as fr:
                    originFileContent = fr.read()
            data = json.dumps(
                {"branch": envs['CI_COMMIT_TAG'], "content": str(originFileContent), "scc_yaml_content": deployment_str,
                 "gitlab_project_id": int(self.utils.CI_PROJECT_ID), "pipeline_id": int(os.environ["GITLAB_CI_PIPELINE_ID"])})
            url = self.utils.get_fdev_url() + "/fenvconfig/api/v2/configfile/saveConfigProperties"
            return_data = self.utils.post_request(url, data)
            if return_data['code'] != "AAAAAAA":
                self.utils.warn("发送后台交易失败：", return_data['msg'])
                sys.exit(-1)
            elif return_data['data']:
                self.utils.warn(return_data['data'])
                # 调投产模块发送邮件
            request_data = json.dumps(
                {"gitlab_project_id": int(self.utils.CI_PROJECT_ID), "product_tag": envs['CI_COMMIT_TAG']})
            request_url = self.utils.get_fdev_url() + "/frelease/api/releasenode/application/saveFdevConfigChanged"
            response_data = self.utils.post_request(request_url, request_data)
            if response_data['code'] != "AAAAAAA":
                self.utils.warn("发送后台交易失败：", response_data['msg'])
                sys.exit(-1)

    def main(self):
        if len(self.utils.CI_SCC_SLUG) != 0:
            template_file = ""
            scc_yaml_path = self.DEPLOY_DIR + "/scc-deployment.yaml"
            if os.path.exists(scc_yaml_path):
                with (open(scc_yaml_path)) as fs:
                    template_file = fs.read()
                self.utils.info("当前scc_deployment.yaml从项目部署目录下获取%s" % scc_yaml_path)
            else:
                template_file = self.get_template_file()
                self.utils.info("当前scc_deployment.yaml从ci-template模版获取%s" % scc_yaml_path)
            # 替换系统环境变量
            template_file_env = os.path.expandvars(template_file)
            # 如果是打得 tag 包,也就是用来投产的包
            if os.environ.has_key('CI_COMMIT_TAG') and "PRO" == os.environ['CI_COMMIT_TAG'].upper()[0:3]:
                os.environ['fdev_ci_image_tag'] = os.environ['CI_COMMIT_TAG']
                # 上传配置文件移动到config中去了(2021-09-13)
                # self.push_config(os.path.expandvars(template_file_env))
            # if 'CI_FDEV_ENV' in os.environ:
            #     return
            if self.utils.IMAGE_UPDATE_FLAG == "0":
                self.utils.warn("===gitci自定义部署方式，镜像不需要SCC部署===")
                return
            self.utils.info("=================开始部署scc=================")
            for slug in self.utils.CI_SCC_SLUG:
                self.utils.info("================= 部署环境%s =================" % slug)
                deployment_str = template_file_env
                self.utils.CI_ENVIRONMENT_SLUG = slug
                if not os.environ.has_key('CI_COMMIT_TAG') or "PRO" != os.environ['CI_COMMIT_TAG'].upper()[0:3]:
                    os.environ['fdev_ci_image_tag'] = self.utils.get_scc_registry_tag(slug)
                deployment_str = os.path.expandvars(deployment_str)
                self.init_scc_env_var(slug)
                # 替换模板变量
                deployment_str = self.replace_yaml_variables(deployment_str, slug)
                deployment_str = self.insert_pvc(deployment_str)
                if slug == "scc-ate":
                    try:
                        deployment_str = self.replace_env(deployment_str)
                    except Exception as e:
                        self.utils.error("替换ESF变量异常:", e.message)
                self.utils.info("================= 部署环境%s deployment-scc.yaml =================" % slug)
                print deployment_str
                with open(self.DEPLOY_DIR + "/deployment-scc.yaml", 'w') as f:
                    f.write(deployment_str)
                # scc 部署
                self.deploy_scc()
