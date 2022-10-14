#!/usr/bin/python
# -*- coding: utf-8 -*-
import commands
import json
import sys
import urllib2

import os
from urllib2 import Request, urlopen, URLError

import pexpect
import datetime

__version__ = "20210730_001"


class Utils:
    def __init__(self):
        print "[+] 镜像版本: ", __version__
        self.envs = os.environ
        self.fdev_flag = True
        self.hands_flag = True
        self.flag = False
        self.CI_PROJECT_DIR = self.envs.get("CI_PROJECT_DIR")
        self.CI_PROJECT_ID = self.envs.get("CI_PROJECT_ID")
        self.CI_PIPELINE_ID = self.envs.get("CI_PIPELINE_ID")
        self.CI_PROJECT_NAME = self.envs.get("CI_PROJECT_NAME")
        # 定义不需要重构的应用列表
        self.project_list = {
            "fdocmanage的gitlabId": "fdocmanage",
            "fnotify的gitlabId": "fnotify",
            "fgitwork的gitlabId": "fgitwork",
            "tplan的gitlabId": "tplan",
            "tmantis的gitlabId": "tmantis",
            "tauto的gitlabId": "tauto",
            "tuser的gitlabId": "tuser",
            "torder的gitlabId": "torder",
            "tcase的gitlabId": "tcase",
            "tadmin的gitlabId": "tadmin"
        }

    CI_ENVIRONMENT_SLUG = ""

    # 封装日志输出格式及颜色
    def info(self, msg, date1="", date2=""):
        curtime = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        print "\033[1;32m %s [info] %s %s %s \033[0m" % (curtime, msg, date1, date2)

    def warn(self, msg, date1="", date2=""):
        curtime = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        print "\033[1;33m %s [info] %s %s %s \033[0m" % (curtime, msg, date1, date2)

    def error(self, msg, date1="", date2=""):
        curtime = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        print "\033[1;31m %s [info] %s %s %s \033[0m" % (curtime, msg, date1, date2)

    def debug(self, msg, date1="", date2=""):
        curtime = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        print "\033[37m %s [info] %s %s %s \033[0m" % (curtime, msg, date1, date2)

    # 封装执行命令
    def cmd(self, command):
        print("\033[1;32m $ " + command + " \033[0m")
        (status, output) = commands.getstatusoutput(command)
        print output
        if status != 0:
            print("\033[1;31m [err] 命令执行失败,程序退出!! \033[0m")
            sys.exit(1)
        return output

    # 重命名 CI_PROJECT_NAME 将 -parent 去掉
    def rename_project_name(self, project_name):
        if project_name[-7:] == "-parent":
            return project_name[:-7]
        return project_name

    # 查询项目英文名
    def get_name_en(self, CI_PROJECT_ID, fdev_url):
        if self.envs.has_key("dce_fdev_caas_registry"):
            return self.rename_project_name(self.CI_PROJECT_NAME)
        else:
            data = json.dumps(
                {"gitlab_project_id": int(CI_PROJECT_ID)})
            url = fdev_url + "/fapp/api/app/query"
            name_en = self.post_request(url, data)["data"][0]["name_en"]
        return name_en

    # 获得 fdev url
    def get_fdev_url(self):
        fdev_url = "xxx"
        if os.environ.has_key('CI_FDEV_ENV'):
            CI_FDEV_ENV = os.environ['CI_FDEV_ENV']
            self.info("CI_FDEV_ENV:", CI_FDEV_ENV)
            return self.get_fdev_slug(CI_FDEV_ENV)
        return fdev_url

    # 获得 fdev 属于哪个环境,sit,uat,pro
    def get_fdev_slug(self, CI_FDEV_ENV):
        fdev_url = "pro地址"
        # fdev pro 接口地址
        fdev_pro_url = "pro地址"
        # fdev uat 接口地址
        fdev_uat_url = "uat地址"
        # fdev sit 接口地址
        fdev_sit_url = "sit地址"
        # fdev rel 接口地址
        fdev_rel_url = "rel地址"
        if CI_FDEV_ENV.upper() == "SIT":
            fdev_url = fdev_sit_url
        elif CI_FDEV_ENV.upper() == "UAT":
            fdev_url = fdev_uat_url
        elif CI_FDEV_ENV.upper() == "REL":
            fdev_url = fdev_rel_url
        else:
            fdev_url = fdev_pro_url
        return fdev_url

    # 获得应用 所属系统
    def get_service_system(self, CI_PROJECT_NAME):
        index = CI_PROJECT_NAME.find('-')
        if index == -1:
            print "应用名命名不符合规范,请联系fdev."
            exit(0)
        app_name = CI_PROJECT_NAME[:index].lower()
        app_system = ""
        if app_name == "msper":
            app_system = "per"
        elif app_name == "mspmk":
            app_system = "pmk"
        elif app_name == "ims":
            app_system = "online"
        elif app_name == "nbh":
            app_system = "nbh"
        else:
            print "应用名命名不符合规范,请联系fdev."
            exit(1)
        return app_system

    # 通过手动设置环境变量部署
    def is_auto(self):
        print "通过手动设置环境变量部署"

    # 通过fdev设置环境变量部署
    def use_fdev(self):
        print "通过fdev设置环境变量部署"

    # 通过temp文件获取值
    def get_var_by_file(self, env_name):
        env_name_str = None
        with open(self.fdev_deploy_path) as f:
            for line in f.readlines():
                if line.find("=") > 0:
                    strs = line.split("=")
                    if env_name == strs[0]:
                        env_name_str = strs[1]

        if env_name_str == None:
            return None
        else:
            return env_name_str.strip('\n')

    # fdev部署自动替换文件
    def relplace_fdev_deploy(self, vars, env_name):
        yaml_str = ""
        with open(self.fdev_deploy_yaml) as f:
            for line in f.readlines():
                start_num = line.find("${")
                end_num = line.rfind("}")
                if start_num > 0 and end_num > 0:
                    old_str = line[start_num + 2:end_num]
                    if vars.has_key(old_str):
                        new_str = line.replace("${" + old_str + "}", vars[old_str])
                        yaml_str = yaml_str + new_str
                    else:
                        print "[Error] this key not fond :" + old_str + ", 请联系 fdev 管理员。"
                        sys.exit(1)

                else:
                    yaml_str = yaml_str + line
        with open(self.fdev_deploy_path, "w") as fw:
            fw.write(yaml_str)
        return self.get_var_by_file(env_name)

    # 通过手动部署 替换动作, 因为环境变量 不允许 出现 . 所有 以_代替 需要 替换第一个
    def relplace_hands_deploy(self, vars, env_name):
        self.info("通过手动获取环境变量如下: ")
        self.info("FDEV_CAAS_REGISTRY: ", vars['dce_fdev_caas_registry'])
        self.info("FDEV_CAAS_REGISTRY_USER: ", vars['dce_fdev_caas_registry_user'])
        self.info("FDEV_CAAS_REGISTRY_PASSWORD: ", vars['dce_fdev_caas_registry_password'])
        self.info("FDEV_CAAS_REGISTRY_NAMESPACE: ", vars['dce_fdev_caas_registry_namespace'])
        self.info("FDEV_CAAS_IP: ", vars['dce_fdev_caas_ip'])
        self.info("FDEV_CAAS_USER: ", vars['dce_fdev_caas_user'])
        self.info("FDEV_CAAS_PWD: ", vars['dce_fdev_caas_pwd'])
        self.info("FDEV_CAAS_TENANT: ", vars['dce_fdev_caas_tenant'])
        self.info("FDEV_CAAS_SECRET: ", vars['dce_fdev_caas_secret'])
        self.info("FDEV_CAAS_ACCESS: ", vars['dce_fdev_caas_access'])
        self.info("FDEV_DOCKERFILE_REGISTRY_NAMESPACE: ", vars['dockerfile_fdev_dockerfile_registry_namespace'])
        self.info("FDEV_DOCKERFILE_IP: ", vars['dockerfile_fdev_dockerfile_ip'])
        self.info("FDEV_DOCKERFILE_TAG: ", vars['dockerfile_fdev_dockerfile_tag'])
        self.info("FDEV_DOCKERFILE_NAME: ", vars['dockerfile_fdev_dockerfile_name'])

        self.info("FDEV_CONFIG_HOST1_IP: ", vars['config_fdev_config_host1_ip'])
        self.info("FDEV_CONFIG_HOST2_IP: ", vars['config_fdev_config_host2_ip'])
        self.info("FDEV_CONFIG_PORT: ", vars['config_fdev_config_port'])
        self.info("FDEV_CONFIG_USER: ", vars['config_fdev_config_user'])
        self.info("FDEV_CONFIG_PASSWORD: ", vars['config_fdev_config_password'])
        self.info("FDEV_CONFIG_DIR: ", vars['config_fdev_config_dir'])

        self.info("profileName: ", vars['yaml_profilename'])
        self.info("UNIONAPI_CONF_FILENAME: ", vars['yaml_unionapi_conf_filename'])
        self.info("SPRING_CLOUD_CONFIG_URI: ", vars['yaml_spring_cloud_config_uri'])
        self.info("EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: ", vars['yaml_eureka_client_serviceurl_defaultzone'])
        self.info("hostLogsPath: ", vars['yaml_hostlogspath'])
        self.info("configServerUri: ", vars['yaml_configserveruri'])
        self.info("eurekaServerUri: ", vars['yaml_eurekaserveruri'])
        self.info("eureka1ServerUri: ", vars['yaml_eureka1serveruri'])

        yaml_str = ""
        with open(self.fdev_deploy_yaml) as f:
            for line in f.readlines():
                start_num = line.find("${")
                end_num = line.rfind("}")
                if start_num > 0 and end_num > 0:
                    old_str1 = line[start_num + 2:end_num].replace(".", "_", 1)
                    old_str2 = line[start_num + 2:end_num]
                    if vars.has_key(old_str1):
                        new_str = line.replace("${" + old_str2 + "}", vars[old_str1])
                        yaml_str = yaml_str + new_str
                    else:
                        self.error("[Error] this key not fond :" + old_str1 + ", 请联系 fdev 管理员。")
                        sys.exit(-1)

                else:
                    yaml_str = yaml_str + line
        with open(self.fdev_deploy_path, "w") as fw:
            fw.write(yaml_str)
        return self.get_var_by_file(env_name)

    # 获得 fdev 环境变量
    def get_env(self, env_name):
        envs = os.environ
        CI_PROJECT_DIR = envs['CI_PROJECT_DIR']

        self.fdev_deploy_yaml = CI_PROJECT_DIR + "/gitlab-ci/fdev-deploy.properties"
        self.fdev_deploy_path = CI_PROJECT_DIR + "/gitlab-ci/fdev-deploy-temp.properties"

        if self.flag:
            # 初始化完毕,开始使用获得值
            return self.get_var_by_file(env_name)
        # 判断是 手动还是通过fdev部署
        if envs.has_key("dce_fdev_caas_registry"):
            if self.hands_flag:
                self.hands_flag = False
                self.flag = True
                self.info("通过手动设置环境变量部署")
            # 通过手动
            if os.path.exists(self.fdev_deploy_path) == False:
                # 该文件不存在, 通过手动部署 转换
                return self.relplace_hands_deploy(envs, env_name)

        else:
            if self.fdev_flag:
                self.fdev_flag = False
                self.flag = True
                self.info("通过fdev设置环境变量部署")
            # 通过fdev
            if "" is self.CI_ENVIRONMENT_SLUG:
                self.CI_ENVIRONMENT_SLUG = self.get_devploy_slug()
            if os.path.exists(self.fdev_deploy_path) == False:
                # 该文件不存在, 需要访问fdev 转换
                data = json.dumps({"env": self.CI_ENVIRONMENT_SLUG, "type": "deploy"})
                url = self.get_fdev_url() + "/fenvconfig/api/v2/var/queryEnvBySlug"

                vars = self.post_request(url, data)["data"]
                return self.relplace_fdev_deploy(vars, env_name)
        return self.get_var_by_file(env_name)

    # 获得这次持续集成 部署环境
    def get_devploy_slug(self):
        envs = os.environ
        if envs.has_key("dce_fdev_caas_registry"):
            return "通过手动部署"
        CI_PROJECT_ID = envs['CI_PROJECT_ID']
        CI_COMMIT_REF_NAME = envs['CI_COMMIT_REF_NAME']
        CI_SCHEDULE = os.environ.has_key('CI_SCHEDULE')  # 如果有 CI_SCHEDULE 环境变量,就认为是 定时的

        fdev_url = self.get_fdev_url()

        if CI_COMMIT_REF_NAME[0:7].upper() == "RELEASE":
            # 查找 release 分支环境
            print "in uat env search."
            data = json.dumps(
                {"gitlab_project_id": int(CI_PROJECT_ID), "release_branch": CI_COMMIT_REF_NAME, "application": ""})
            url = fdev_url + "/frelease/api/releasenode/queryUatEnv"
            slug = self.post_request(url, data)["data"]
            return slug

        elif CI_COMMIT_REF_NAME[0:3].upper() == "PRO":
            # 查找 release 分支环境
            print "in rel env search."
            data = json.dumps(
                {"gitlab_project_id": int(CI_PROJECT_ID), "release_branch": CI_COMMIT_REF_NAME, "application": ""})
            url = fdev_url + "/frelease/api/releasenode/queryRelEnv"
            slug = self.post_request(url, data)["data"]
            return slug

        else:
            # 查找 sit 分支环境
            print "in sit env search."
            data = json.dumps(
                {"CI_PROJECT_ID": int(CI_PROJECT_ID), "CI_COMMIT_REF_NAME": CI_COMMIT_REF_NAME,
                 "CI_SCHEDULE": CI_SCHEDULE})
            url = fdev_url + "/fapp/api/app/get_sit_slug"
            slug = self.post_request(url, data)["data"][0]["env_name"]
            return slug

    # 封装网络请求
    def post_request(self, url, data):

        headers = {"Content-Type": "application/json",
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
            response = urllib2.urlopen(request, timeout=10)
            code = response.getcode()
            return_date = response.read()
            self.info("code:", code)
            print "response:%s" % (json.loads(return_date))
            return json.loads(return_date)

        except URLError as e:
            if hasattr(e, 'reason'):
                self.warn('无法连接到服务器,请重试')
                print 'Reason: ', e.reason
                sys.exit(-1)
            elif hasattr(e, 'code'):
                self.error('服务器返回异常状态码')
                self.error('Error code: ', e.code)
                sys.exit(-1)

    # 封装网络请求
    def post_request_GET(self, url, headers):

        self.info("url:", url)
        print "headers:", headers
        print "method: GET"

        request = urllib2.Request(
            url=url,
            headers=headers,
        )
        request.get_method = lambda: 'GET'
        try:
            response = urllib2.urlopen(request, timeout=100)
            code = response.getcode()
            return_date = response.read()
            self.info("code:", code)
            print "response:%s" % (json.loads(return_date))
            return json.loads(return_date)

        except URLError as e:
            if hasattr(e, 'reason'):
                self.warn('无法连接到服务器,请重试')
                self.warn('Reason: ', e.reason)
                sys.exit(-1)
            elif hasattr(e, 'code'):
                self.error('服务器返回异常状态码')
                self.error('Error code: ', e.code)
                sys.exit(-1)

    def push_config(self, cmd, password):
        child = pexpect.spawn(cmd)
        child.logfile = sys.stdout
        index = child.expect(['yes', pexpect.TIMEOUT])

        if index == 0:
            child.sendline('yes')
            child.expect(['Password', pexpect.EOF])
            child.sendline(password)
            child.expect(pexpect.EOF)
        elif index == 1:
            child.sendline(password)
            child.expect(pexpect.EOF)
        elif index == 2:
            child.sendline(password)
            child.expect(pexpect.EOF)
        else:
            self.error("scp 配置文件到配置中心异常,请联系管理员!")
            self.error("cmd:" + cmd)
            self.error("password:" + password)
            sys.exit(-1)

    # 获取镜像标签
    def get_registry_tag(self):
        registry_tag = ""
        url = "http://xxx/api/v4/projects/" + self.CI_PROJECT_ID + "/pipelines/" + self.CI_PIPELINE_ID
        headers = {"PRIVATE-TOKEN": "KzdcV1psJCVsj5eAVv5n"}
        create_time = self.post_request_GET(url, headers)["created_at"]
        print "create_time: ", create_time
        pipline_create_time = str(create_time)[0: 16] \
            .replace("T", "_", 1) \
            .replace(":", "-", 1)
        print 'piplineCreateTime: ', pipline_create_time
        if self.CI_ENVIRONMENT_SLUG == "":
            registry_tag = pipline_create_time + "_" + self.CI_PIPELINE_ID
        else:
            registry_tag = self.CI_ENVIRONMENT_SLUG + "-" + pipline_create_time + "_" + self.CI_PIPELINE_ID
        return registry_tag

    # 根据部署的分支获取对应的文件夹
    def get_file_dir(self):
        envs = os.environ
        DEPLOY_DIR = ""
        if envs['CI_COMMIT_REF_NAME'][0:7].upper() == "RELEASE":
            self.info("选择 uat 部署模板")
            DEPLOY_DIR = "uat"
        elif envs.has_key('CI_COMMIT_TAG') and envs['CI_COMMIT_TAG'][0:3].upper() == "PRO":
            self.info("选择 pro 部署模板")
            DEPLOY_DIR = "pro"
        elif envs['CI_COMMIT_REF_NAME'].upper() == "SIT":
            self.info("选择 sit 部署模板")
            DEPLOY_DIR = "sit"

        if os.path.exists(envs['CI_PROJECT_DIR'] + "/gitlab-ci/" + DEPLOY_DIR):
            # 定制化文件存在
            DEPLOY_DIR = envs['CI_PROJECT_DIR'] + "/gitlab-ci/" + DEPLOY_DIR
        else:
            DEPLOY_DIR = envs['CI_PROJECT_DIR'] + "/gitlab-ci"

        self.info("DEPLOY_DIR:", DEPLOY_DIR)
        return DEPLOY_DIR
