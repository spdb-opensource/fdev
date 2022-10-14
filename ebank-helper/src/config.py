#!/usr/bin/python
# -*- coding: utf-8 -*-
import json
import os
import sys

import chardet
from pexpect import pxssh

from utils import Utils

reload(sys)
sys.setdefaultencoding('utf-8')


class Config:

    def __init__(self):
        self.utils = Utils()
        self.envs = os.environ
        self.init_env()
        self.CI_FDEV_ENV = os.environ.get('CI_FDEV_ENV', "")
        self.ENCODE_UTF8 = "utf-8"
        self.push_config_flag = False

    def init_env(self):
        # 获得部署的目录
        envs = os.environ
        self.CI_PROJECT_NAME = envs['CI_PROJECT_NAME']
        # 应用英文名去掉-parent
        self.name_en = self.utils.rename_project_name(self.CI_PROJECT_NAME)

        self.fdev_url = self.utils.get_fdev_url()
        self.DEPLOY_DIR = self.utils.get_file_dir()
        if envs.has_key("CI_COMMIT_TAG"):
            self.CI_COMMIT_TAG = envs["CI_COMMIT_TAG"]
        self.CI_PROJECT_ID = envs["CI_PROJECT_ID"]
        self.CI_PIPELINE_ID = envs["CI_PIPELINE_ID"]
        envs['CI_PROJECT_NAME'] = self.name_en

        if not (self.utils.CI_SCHEDULE and self.envs.get('CI_SCHEDULE') == "sit-ate"):
            if "1" == self.utils.deploy_platform['caas_status']:
                self.FDEV_CAAS_SERVICE_REGISTRY = self.utils.get_env("FDEV_CAAS_SERVICE_REGISTRY")
                self.FDEV_CAAS_REGISTRY_NAMESPACE = self.utils.get_env("FDEV_CAAS_REGISTRY_NAMESPACE")
                self.CI_REGISTRY_IMAGE = self.FDEV_CAAS_SERVICE_REGISTRY + "/" + self.FDEV_CAAS_REGISTRY_NAMESPACE + "/" + self.name_en
                os.environ['CI_REGISTRY_IMAGE'] = self.CI_REGISTRY_IMAGE

    def push_config(self):
        # 自定义部署设置了更新配置文件为否,不需要上传配置文件
        if self.utils.CONFIG_UPDATE_FLAG == "0":
            self.utils.info("======自定义部署设置了更新配置文件为否,不需要上传配置文件======")
            return
        envs = os.environ
        # 替换镜像tag
        if envs.has_key("CI_COMMIT_TAG"):
            image_tag = envs["CI_COMMIT_TAG"]
            envs['CI_PIPELINE_ID'] = image_tag
            os.environ['CI_PIPELINE_ID'] = image_tag
            self.utils.info("image_tag: ", image_tag)
        else:
            if "scc" in self.utils.CI_ENVIRONMENT_SLUG:
                os.environ['CI_PIPELINE_ID'] = self.CI_PIPELINE_ID
                image_tag = self.utils.get_scc_registry_tag(self.utils.CI_ENVIRONMENT_SLUG)
            else:
                image_tag = self.utils.get_registry_tag()
            envs['CI_PIPELINE_ID'] = image_tag
            os.environ['CI_PIPELINE_ID'] = image_tag
            self.utils.info("image_tag: ", image_tag)

        self.fdev_runtime_file = self.DEPLOY_DIR + "/fdev-application.properties"

        if os.path.exists(self.fdev_runtime_file) == False:
            self.utils.warn('获取配置模板文件失败，请检查应用配置模板！')
            return

        # 获取应用的配置模板文件内容
        with open(self.fdev_runtime_file, "r") as fr:
            originFileContent = fr.read()

        # 自动化测试环境需要判断是否替换esb的发送地址切换为挡板
        try:
            if self.utils.CI_ENVIRONMENT_SLUG == "scc-ate":
                url = "xxx/api/batchChange/queryIamsConfigByName"
                # url = "xxx/api/batchChange/queryIamsConfigByName"
                data = json.dumps({"appName": self.name_en})
                response_data = self.utils.post_request(url, data)
                if response_data["code"] == "AAAAAAA" and not response_data['data'] is None:
                    # esfConfig = response_data["data"]["esfConfig"]
                    esbSoapIp = response_data["data"]["esbSoapIp"]
                    esbSopIp = response_data["data"]["esbSopIp"]
                    esbSopPort = response_data["data"]["esbSopPort"]
                    if not esbSoapIp is None and len(esbSoapIp) > 0:
                        originFileContent = originFileContent.replace("$<spdbservice_esb_info.domain_retail>", esbSoapIp)
                    if not esbSopIp is None and len(esbSopIp) > 0:
                        originFileContent = originFileContent.replace("$<spdbservice_esb_info.domain_retail>", esbSopIp)
                    if esbSopPort is not None and len(esbSopPort) > 0:
                        originFileContent = originFileContent.replace("$<spdbservice_esb_info.port>", esbSopPort)
        except Exception as e:
            self.utils.error("发送挡板请求接口异常:", e.message)

        print "CI_ENVIRONMENT_SLUG=" + self.utils.CI_ENVIRONMENT_SLUG
        host_ip = self.utils.FDEV_CONFIG_HOST1_IP if self.utils.check_zero(
            self.utils.FDEV_CONFIG_HOST1_IP) else self.utils.FDEV_CONFIG_HOST2_IP
        # 获取配置文件信息 并上传至配置中心
        try:
            data = json.dumps({"env_name": self.utils.CI_ENVIRONMENT_SLUG, "content": str(originFileContent), "type": "1",
                               "project_id": str(self.CI_PROJECT_ID), "ci_project_name": self.name_en,
                               "fdev_config_dir": self.utils.FDEV_CONFIG_DIR, "fdev_config_host_ip": host_ip,
                               "fdev_config_user": self.utils.FDEV_CONFIG_USER,
                               "fdev_config_password": self.utils.FDEV_CONFIG_PASSWORD})
        except Exception as e:
            encoding = chardet.detect(originFileContent)['encoding']
            self.utils.info("encoding:" + encoding)
            if encoding != self.ENCODE_UTF8:
                self.utils.error('=========================================================================')
                self.utils.error('  外部配置文件（fdev-application.properties）编码必须为utf-8！  ')
                self.utils.error('=========================================================================')
            else:
                self.utils.error(e.message)
            sys.exit(-1)
        url = self.fdev_url + "/fenvconfig/api/v2/configfile/previewConfigFile"
        response_data = self.utils.post_request(url, data)
        if "AAAAAAA" != response_data["code"]:
            self.utils.warn('调环境配置模块接口previewConfigFile出错：', response_data['msg'])
            sys.exit(-1)

    # 获取项目英文名第一段
    def get_first_name(self):
        name_split = str(self.name_en).split("-")
        if len(name_split) <= 0:
            self.utils.warn("当前应用名称为：", self.name_en)
            self.utils.warn("应用名称不符合规范，xxx-xxx-xxx")
            sys.exit(-1)
        return name_split[0]

    # 若是 tag 打出的包 需要发送后台去保存配置文件和发邮件
    def save_pro_config(self):
        scc_yaml = ""
        caas_yaml = ""
        template_file = ""
        # 获取sccyaml
        if "1" == self.utils.deploy_platform['scc_status']:
            scc_yaml_path = self.DEPLOY_DIR + "/scc-deployment.yaml"
            if os.path.exists(scc_yaml_path):
                with (open(scc_yaml_path)) as fs:
                    template_file = fs.read()
                self.utils.info("当前scc_deployment.yaml从项目部署目录下获取%s" % scc_yaml_path)
            else:
                template_file = self.get_template_file()
                self.utils.info("当前scc_deployment.yaml从ci-template模版获取%s" % scc_yaml_path)
            # 替换系统环境变量
            if os.environ.has_key('CI_COMMIT_TAG') and "PRO" == os.environ['CI_COMMIT_TAG'].upper()[0:3]:
                os.environ['fdev_ci_image_tag'] = os.environ['CI_COMMIT_TAG']
            template_file_env = os.path.expandvars(template_file)
            scc_yaml = os.path.expandvars(template_file_env)
        # 获取caasyaml
        if not (self.utils.CI_SCHEDULE and self.envs.get('CI_SCHEDULE') == "sit-ate"):
            if "1" == self.utils.deploy_platform['caas_status']:
                self.utils.cmd(
                    "envsubst < " + self.DEPLOY_DIR + "/deployment.yaml > " + self.DEPLOY_DIR + "/temp-deployment-a.yaml")
                with(open(self.DEPLOY_DIR + "/temp-deployment-a.yaml")) as f:
                 caas_yaml = f.read()
        # 获取配置文件
        self.fdev_runtime_file = self.DEPLOY_DIR + "/fdev-application.properties"
        if not os.path.exists(self.fdev_runtime_file) or self.utils.CONFIG_UPDATE_FLAG == "0":
            originFileContent = "#"
        else:
            with open(self.fdev_runtime_file, "r") as fr:
                originFileContent = fr.read()
        data = json.dumps(
            {"branch": self.CI_COMMIT_TAG, "content": str(originFileContent), "scc_yaml_content": scc_yaml,
              "yaml_content": caas_yaml,"gitlab_project_id": int(self.CI_PROJECT_ID),"pipeline_id": int(self.CI_PIPELINE_ID)})
        url = self.fdev_url + "/fenvconfig/api/v2/configfile/saveConfigProperties"
        return_data = self.utils.post_request(url, data)
        if return_data['code'] != "AAAAAAA":
            self.utils.warn("发送后台交易失败：", return_data['msg'])
            sys.exit(-1)
        elif return_data['data']:
            self.utils.warn(return_data['data'])
            # 调投产模块发送邮件
        request_data = json.dumps({"gitlab_project_id": int(self.CI_PROJECT_ID), "product_tag": self.CI_COMMIT_TAG})
        request_url = self.fdev_url + "/frelease/api/releasenode/application/saveFdevConfigChanged"
        response_data = self.utils.post_request(request_url, request_data)
        if response_data['code'] != "AAAAAAA":
            self.utils.warn("发送后台交易失败：", response_data['msg'])
            sys.exit(-1)

    # 获取scc-yaml模板
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

    # ssh 到服务器 判断 目录是否存在，并创建
    def check_dir(self):
        name = self.get_first_name()
        command = "mkdir -p %s/%s/" % (self.utils.FDEV_CONFIG_DIR, name)
        print command
        s = pxssh.pxssh()
        if self.utils.FDEV_CONFIG_HOST1_IP:
            hostname = self.utils.FDEV_CONFIG_HOST1_IP
        else:
            if self.utils.FDEV_CONFIG_HOST2_IP:
                hostname = self.utils.FDEV_CONFIG_HOST2_IP
        print "hostname: %s, dirName:%s, host1:%s, host2:%s, user:%s, password:%s " % (
            hostname, name, self.utils.FDEV_CONFIG_HOST1_IP, self.utils.FDEV_CONFIG_HOST2_IP, self.utils.FDEV_CONFIG_USER,
            self.utils.FDEV_CONFIG_PASSWORD)
        s.login(server=hostname, username=self.utils.FDEV_CONFIG_USER, password=self.utils.FDEV_CONFIG_PASSWORD)
        print "login success"
        s.sendline(command)
        s.prompt()
        print s.before

    def main(self):
        schedule_flag = "CI_SCHEDULE" in os.environ
        print("CI_SCHEDULE:" + str(schedule_flag))
        if "sit" == self.utils.get_ref_env() and self.utils.is_retail and not self.utils.CI_SCHEDULE:
            print "sit分支零售组特殊应用非定时部署不做配置推送"
            sys.exit(0)

        # 检查环境部署开关
        if "1" == self.utils.deploy_platform['caas_status'] or "1" == self.utils.deploy_platform['scc_status']:
            if not (self.utils.CI_SCHEDULE and self.envs.get('CI_SCHEDULE') == "sit-ate"):
                if "1" == self.utils.deploy_platform['caas_status']:
                    self.utils.init_caas_var(self.utils.ci_environment_slug)
                    # 检查配置中心相关变量实体是否有值
                    if self.utils.check_env():
                        self.push_config()
                        self.push_config_flag = True
            if "1" == self.utils.deploy_platform['scc_status']:
                for scc_slug, scc_vars in self.utils.CI_SCC_VARS.items():
                    self.utils.init_scc_var(scc_slug)
                    if self.utils.check_env():
                        self.push_config()
                        self.push_config_flag = True
                    else:
                        self.push_config_flag = False
            #上传配置文件到gitlab
            envs = os.environ
            # 若是 tag 打出的包 需要发送后台去保存配置文件和发邮件
            if envs.has_key('CI_COMMIT_TAG') and self.push_config_flag:
                self.save_pro_config()
        else:
            self.utils.error("当前应用尚未打开环境部署开关，请联系应用负责人移步到环境配置管理下的部署信息打开开关！")
            sys.exit(-1)

if __name__ == '__main__':
    config = Config()
    vars = os.environ
    if vars.has_key("dce_fdev_caas_registry"):
        print "\033[1;31m 需要自己手动向配置中心放置文件.. \033[0m"
    else:
        config.main()
