#!/usr/bin/python
# -*- coding: utf-8 -*-
import commands
import datetime
import json
import os
import shutil
import ssl
import sys
import time
import urllib2
from urllib2 import URLError

import pexpect

reload(sys)
sys.setdefaultencoding("utf-8")
print sys.getdefaultencoding()

__version__ = "20220324_004"


class Utils:
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
        self.CI_PIPELINE_ID = self.envs.get("CI_PIPELINE_ID")
        self.CI_PROJECT_NAME = self.envs.get("CI_PROJECT_NAME")
        self.CI_API_V4_URL = self.envs.get("CI_API_V4_URL")
        self.CI_COMMIT_SHORT_SHA = self.envs.get("CI_COMMIT_SHORT_SHA")
        self.CI_COMMIT_REF_NAME = self.envs.get('CI_COMMIT_REF_NAME')

        self.is_retail = False
        self.req_retail_flag = False
        self.CI_SCHEDULE = "CI_SCHEDULE" in self.envs
        self.is_micro_flag = False
        self.is_call_micro = False
        self.ci_environment_slug = None
        self.CI_VARS = None
        self.CI_SCC_VARS = {}
        self.CI_SCC_SLUG = []
        self.deploy_platform = {"caas_status": "0", "scc_status": "0"}
        self.CONFIG_UPDATE_FLAG = "1"
        self.IMAGE_UPDATE_FLAG = "1"

        self.init_slug_and_vars()


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

    def debug(self, msg, date1="", date2=""):
        curtime = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        print "\033[37m %s [debug] %s %s %s \033[0m" % (curtime, msg, date1, date2)

    # 封装执行命令
    def cmd(self, command):
        print("\033[1;32m $ " + command + " \033[0m")
        try:
            (status, output) = commands.getstatusoutput(command)
            print output
            if status != 0:
                print("\033[1;31m [err] 命令执行失败,程序退出!! \033[0m")
                sys.exit(1)
            return output
        except IOError as e:
            self.error('执行命令异常,请重试:', e.message)
            return ""

    # 封装 不打印执行命令
    def cmdN(self, command):
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
        fdev_url = "xxx"
        # fdev pro 接口地址
        fdev_pro_url = "xxx"
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
            return env_name_str.strip()

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
                        if old_str.find("dockerfile") == 0:
                            continue
                        print "[Error] this key not fond :" + old_str + ", 请联系 fdev 管理员。"
                        sys.exit(1)
                else:
                    yaml_str = yaml_str + line
        with open(self.fdev_deploy_path, "w") as fw:
            fw.write(yaml_str)
        return self.get_var_by_file(env_name)

    # 获取基础镜像版本
    def get_image_version(self, image_name):
        # 查询基础镜像版本
        url = self.get_fdev_url() + "/fcomponent/api/baseImage/queryBaseImageVersion"
        data = json.dumps({"gitlabId": self.CI_PROJECT_ID, "image": image_name})
        response = self.post_request(url, data)
        if self.success == response['code']:
            response_data = response['data']
            return response_data['FDEV_CAAS_BASE_IMAGE_VERSION']
        elif self.success != response['code']:
            self.error("发送组件模块后台交易出错：", response['msg'])
            sys.exit(-1)

    # remove value's -
    def remove_value(self, CI_VARS):
        for key, val in CI_VARS.items():
            if val is "-" or val == "-":
                CI_VARS[key] = ""
        return CI_VARS

    def get_ref_env(self):
        if os.environ['CI_COMMIT_REF_NAME'][0:7].upper() == "RELEASE":
            return "uat"
        elif os.environ['CI_COMMIT_REF_NAME'][0:3].upper() == "PRO":
            return "rel"
        else:
            return "sit"

    # 封装 caas 网络请求
    def request_caas(self, url, headers, data='', method='GET'):
        self.info("url:", url)
        self.info("headers:", headers)
        self.debug("data:", data)
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
            self.debug(return_data)
            return return_data
        except urllib2.HTTPError as e:
            return_data['code'] = e.code
            self.warn('服务器返回异常状态码')
            self.warn('Error code: ', return_data.get('code'))
            self.warn(e)
            return return_data
        except URLError as e:
            self.error('无法连接到服务器,请重试')
            self.error('Reason: ', e.reason)
            sys.exit(1)

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
            response = urllib2.urlopen(request, timeout=180)
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
            # print "response:%s" % (json.loads(return_date))
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

    def input_pwd(self, cmd, password):
        self.info("cmd:", cmd)
        child = pexpect.spawn(cmd)
        child.logfile = sys.stdout
        index = child.expect(['yes', 'password', 'Password', pexpect.TIMEOUT], timeout=300)

        if index == 0:
            child.sendline('yes')
            child.expect(['password', 'Password', pexpect.EOF], timeout=300)
            child.sendline(password)
            child.expect(pexpect.EOF, timeout=300)
        elif index == 1:
            child.sendline(password)
            child.expect(pexpect.EOF, timeout=300)
        elif index == 2:
            child.sendline(password)
            child.expect(pexpect.EOF, timeout=300)
        else:
            self.error("cmd:" + cmd)
            self.error("password:" + password)
            sys.exit(-1)

    # 获取镜像标签
    def get_registry_tag(self):
        if 0 == int(self.CI_PIPELINE_ID):
            return self.ci_environment_slug + "-" + \
                   str(datetime.datetime.now()).split(".")[0].replace(" ", "_").replace(":", "-") + \
                   "_" + self.CI_PIPELINE_ID
        url = "%s/projects/%s/pipelines/%s" % (self.CI_API_V4_URL, self.CI_PROJECT_ID, self.CI_PIPELINE_ID)
        headers = {"PRIVATE-TOKEN": self.is_SIT_or_master()}
        create_time = self.post_request_GET(url, headers)["created_at"]
        print "create_time: ", create_time
        pipline_create_time = str(create_time)[0: 16] \
            .replace("T", "_", 1) \
            .replace(":", "-", 1)

        print 'piplineCreateTime: ', pipline_create_time
        if self.ci_environment_slug == "":
            registry_tag = pipline_create_time + "_" + self.CI_PIPELINE_ID
        else:
            registry_tag = self.ci_environment_slug + "-" + pipline_create_time + "_" + self.CI_PIPELINE_ID
        return registry_tag

    # 获取SCC镜像标签
    def get_scc_registry_tag(self, slug):
        if 'GITLAB_CI_PIPELINE_ID' in os.environ:
            GITLAB_CI_PIPELINE_ID = os.environ['GITLAB_CI_PIPELINE_ID']
        else:
            # GITLAB_CI_PIPELINE_ID = os.environ['CI_PIPELINE_ID']
            GITLAB_CI_PIPELINE_ID = self.CI_PIPELINE_ID
        if 0 == int(GITLAB_CI_PIPELINE_ID):
            return slug + "-" + \
                   str(datetime.datetime.now()).split(".")[0].replace(" ", "_").replace(":", "-") + \
                   "_" + GITLAB_CI_PIPELINE_ID
        url = "%s/projects/%s/pipelines/%s" % (self.CI_API_V4_URL, self.CI_PROJECT_ID, GITLAB_CI_PIPELINE_ID)
        headers = {"PRIVATE-TOKEN": self.is_SIT_or_master()}
        create_time = self.post_request_GET(url, headers)["created_at"]
        print "create_time: ", create_time
        pipline_create_time = str(create_time)[0: 16] \
            .replace("T", "_", 1) \
            .replace(":", "-", 1)

        print 'piplineCreateTime: ', pipline_create_time
        if slug == "":
            registry_tag = pipline_create_time + "_" + GITLAB_CI_PIPELINE_ID
        else:
            registry_tag = slug + "-" + pipline_create_time + "_" + GITLAB_CI_PIPELINE_ID
        return registry_tag

    # 判断 是 SIT 环境还是 其他
    def is_SIT_or_master(self):
        if self.envs.has_key("CI_FDEV_ENV"):
            CI_FDEV_ENV = self.envs["CI_FDEV_ENV"]
            if CI_FDEV_ENV.upper() == "SIT":
                return self.sit_token
            elif CI_FDEV_ENV.upper() == "UAT":
                return self.token
            else:
                return self.token
        else:
            return self.token

    # 根据部署的分支获取对应的文件夹
    def get_file_dir(self):
        envs = os.environ
        DEPLOY_DIR = "sit"
        if envs.has_key("definedDeployId") and self.ci_environment_slug:
            # 第一优先级使用执行环境
            self.info("系统变量包含 ci_environment_slug:" + self.ci_environment_slug)
            DEPLOY_DIR = self.ci_environment_slug
        else:
            # 第二优先级使用分支名判断
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
        elif os.path.exists(envs['CI_PROJECT_DIR'] + "/gitlab-ci"):
            DEPLOY_DIR = envs['CI_PROJECT_DIR'] + "/gitlab-ci"
        else:
            DEPLOY_DIR = envs['CI_PROJECT_DIR'] + "/ci"
        self.info("DEPLOY_DIR:", DEPLOY_DIR)
        return DEPLOY_DIR

    # 去除版本号
    def remove_version(self, source_dir, project_name):
        self.info("统一去掉版本号")
        try:
            for root, sub_dirs, files in os.walk(source_dir):
                for special_file in files:
                    if (special_file.find("SNAPSHOT") != -1) or (special_file.find("RELEASE") != -1):
                        (shotname, extension) = os.path.splitext(special_file)
                        source_file = os.path.join(source_dir, special_file)
                        target_file = os.path.join(source_dir, project_name + extension)
                        if special_file.find(project_name) != -1:
                            self.info("old", source_file)
                            self.info("new", target_file)
                            shutil.copy(source_file, target_file)
        except Exception as e:
            self.warn("统一去掉版本号出错")
            self.warn(e)

    # 过滤文件夹下的 jar 和 war 文件 gitlab-ci 文件夹除外
    def filter_jar_or_war(self, parentDir):
        fileList = []
        for home, dirs, files in os.walk(parentDir):
            for fileName in files:
                if "gitlab-ci" in home or "/ci/" in home:
                    continue
                if home.endswith("target") and (fileName.endswith(".jar") or fileName.endswith(".war")):
                    fileList.append(os.path.join(home, fileName))
        return fileList

    # 获取target文件夹下的war包
    def get_war(self, parent_dir):
        for home, dirs, files in os.walk(parent_dir):
            for fileName in files:
                if "gitlab-ci" in home or "/ci/" in home:
                    continue
                if home.endswith("target") and fileName.endswith(".war"):
                    return os.path.join(home, fileName)

    # 获取目标文件夹下的map文件
    def get_map(self, parent_dir, remote_dir):
        # 获取当前文件夹下的.map文件(仅一级)
        mapFiles = []
        dir_or_files = os.listdir(parent_dir)
        for dir_or_file in dir_or_files:
            # 获取目录或者文件的全路径
            dir_file_path = os.path.join(parent_dir, dir_or_file)
            if os.path.isdir(dir_file_path):
                continue
            elif dir_or_file.endswith(".map"):
                remote_path = os.path.join(remote_dir, dir_or_file)
                mapFiles.append(remote_path)
        return mapFiles

    def replace_placeHold(self, yaml_file):
        with open(yaml_file) as f:
            for line in f.readlines():
                # 判断当前行 是否存在{{  }}这样的字符串
                start_num = line.find("{{")
                end_num = line.rfind("}}")
                if start_num > 0 and end_num > 0:
                    # 这一列存在{{}}
                    old_str = line[start_num:end_num]
                    key = old_str.split(" ")[1]
                    new_str = line.replace("{{ " + key + " }}", str(self.get_env(key)))
                    yaml_str = yaml_str + "\n" + new_str
                else:
                    yaml_str = yaml_str + "\n" + line

        return yaml_str

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

    # 判断 这次提交是否包含 deployment.yaml 是则 先删除再创建 不是 则只更新镜像
    def is_commit_deployment(self):
        url = "%s/projects/%s/repository/commits/%s/diff" % (
            self.CI_API_V4_URL, self.CI_PROJECT_ID, self.CI_COMMIT_SHORT_SHA)
        headers = {"PRIVATE-TOKEN": self.is_SIT_or_master()}
        commit_data = self.post_request_GET(url=url, headers=headers)
        yaml_path = "gitlab-ci/deployment.yaml"
        DEPLOY_DIR = self.get_file_dir()
        if not str(DEPLOY_DIR).__contains__("gitlab-ci"):
            yaml_path = "ci/deployment.yaml"
        for commit in commit_data:
            if commit.has_key("old_path") and yaml_path == commit.get("old_path"):
                return True
        return False

    def is_commit_scc_deployment(self):
        url = "%s/projects/%s/repository/commits/%s/diff" % (
            self.CI_API_V4_URL, self.CI_PROJECT_ID, self.CI_COMMIT_SHORT_SHA)
        headers = {"PRIVATE-TOKEN": self.is_SIT_or_master()}
        commit_data = self.post_request_GET(url=url, headers=headers)
        yaml_path = "gitlab-ci/scc-deployment.yaml"
        DEPLOY_DIR = self.get_file_dir()
        if not str(DEPLOY_DIR).__contains__("gitlab-ci"):
            yaml_path = "ci/scc-deployment.yaml"
        for commit in commit_data:
            if commit.has_key("old_path") and yaml_path == commit.get("old_path"):
                return True
        return False

    # 判断 应用是否 有添加过边车
    def is_have_file_beat(self, response):
        response = json.loads(response)
        if response.has_key("spec") \
                and response['spec'].has_key("template") \
                and response['spec']['template'].has_key("spec") \
                and response['spec']['template']['spec'].has_key("containers"):
            for item in response['spec']['template']['spec']['containers']:
                if self.file_beat_name == item['name']:
                    return False
            return True

    # 是Java 微服务的才插入边车
    def is_micro_service(self, app_info):
        if self.is_call_micro == True:
            return self.is_micro_flag
        self.is_call_micro = True
        if len(app_info) != 0 and app_info['type_name'] == "Java微服务".decode("utf-8"):
            self.is_micro_flag = True
        return self.is_micro_flag

    # 环境 包含 sit uat rel 的才插入边车
    def is_allow_env(self):
        env = self.ci_environment_slug.upper()
        self.info("插入边车判断当前环境为 ", env)
        if env.find("SIT") >= 0 or env.find("UAT") >= 0 or env.find("REL") >= 0:
            return True
        return False

    # 获取 Dockerfile 的镜像名字
    def get_image_name(self):
        file_dir = self.get_file_dir()
        dockerfile_dir = file_dir + "/Dockerfile"
        if os.path.exists(file_dir):
            with open(dockerfile_dir, "r") as fr:
                first_line = fr.readline().strip()
                if first_line.startswith("FROM") and first_line.endswith("{FDEV_CAAS_BASE_IMAGE_VERSION}"):
                    line_split = first_line.split("/")
                    image_name = line_split[len(line_split) - 1].split(":")[0]
                    return image_name

    # 获取最新合并到目标分支的feature分支
    def get_latest_feature_branch(self, project_id, branch):
        url = "%s/projects/%s/merge_requests?state=merged&target_branch=%s" % (self.CI_API_V4_URL, project_id, branch)
        headers = {"PRIVATE-TOKEN": self.is_SIT_or_master()}
        response = self.post_request_GET(url, headers)
        if len(response) != 0:
            return response[0]['source_branch']

    # 解密 pwd
    def decrypt(self, pwd):
        url = "%s/fenvconfig/api/v2/var/ciDecrypt" % (self.get_fdev_url())
        data = json.dumps({"fdev_caas_pwd": pwd})
        return self.post_request_without_log(url, data)

    def post_request_without_log(self, url, data):
        headers = {"Content-Type": "application/json",
                   "source": "back"}
        request = urllib2.Request(
            url=url,
            headers=headers,
            data=data,
        )
        request.get_method = lambda: 'POST'
        try:
            response = urllib2.urlopen(request, timeout=60)
            return_date = json.loads(response.read())
            code = return_date['code']
            if code != "AAAAAAA":
                self.warn("解密失败，秘钥为: %s", data)
                sys.exit(1)
            return return_date['data']

        except URLError as e:
            if hasattr(e, 'reason'):
                self.warn('无法连接到服务器,请重试')
                print 'Reason: ', e.reason
                sys.exit(-1)
            elif hasattr(e, 'code'):
                self.error('服务器返回异常状态码')
                self.error('Error code: ', e.code)
                sys.exit(-1)

    # 替换 秘钥
    def replace_decrypt(self, data):
        for key, value in data.items():
            if key == "FDEV_CAAS_PWD" or key == "FDEV_CAAS_REGISTRY_PASSWORD":
                data[key] = self.decrypt(value)
        return data

    # 判断 应用是否为微服务
    def app_info(self):
        result = {}
        url = "%s/fapp/api/app/query" % (self.get_fdev_url())
        data = json.dumps({"gitlab_project_id": self.CI_PROJECT_ID})
        response = self.post_request(url, data)
        if self.success == response['code'] and len(response['data']):
            result['type_name'] = response['data'][0]['type_name']
            result['FDEV_APP_GROUP'] = response['data'][0]['group']['name']
            result['id'] = response['data'][0]['id']
        return result

    # string 不为0、""、None、"0"，返回True
    def check_zero(self, string):
        if string and "0" != string:
            return True
        else:
            return False

    def get_ci_key(self):
        url = self.get_fdev_url() + "/fenvconfig/api/v2/appDeploy/queryByGitlabId"
        data = json.dumps({"gitlabId": self.CI_PROJECT_ID})
        response = self.post_request(url, data)
        if self.success == response['code']:
            response_data = response['data']
            if response_data:
                return response_data['variables']
        elif self.success != response['code']:
            self.error("发送环境配置模块后台交易出错：", response['msg'])
            sys.exit(-1)

    # 发送minio暂存mvn打出来jar或war包
    def upload_jar_or_war_to_minio(self, file_name):
        envs = os.environ
        # /builds/ebank/devops/testLsl/msper-web-testapps/target/msper-web-testapps.jar
        # 获取应用名
        list = file_name.split('/')
        i = 0
        # 应用名
        app_name = ''
        # 应用打出来的包的后缀名
        suffix = ''
        for item in list:
            i = i + 1
            if item == 'target':
                # 截取target前一文件夹为应用名（msper-web-testapps），后一个文件后缀（jar）
                print("item:" + item)
                app_name = list[i - 2]
                print("app_name:" + app_name)
                suffix = list[i].split('.')[len(list[i].split('.')) - 1]
                print("suffix:" + suffix)
                break
        if app_name == '' or suffix == '':
            print("截取应用名/后缀名出错，请检查介质包名称是否符合.../xxx/target/xxx.jar")
            exit(0)
        # pro-20210608_001-001
        CI_COMMIT_REF_NAME = envs['CI_COMMIT_TAG']
        print ("CI_COMMIT_REF_NAME:" + CI_COMMIT_REF_NAME)
        # 20210608_001
        pro_win_name = CI_COMMIT_REF_NAME[4:16]
        print ("pro_win_name:" + pro_win_name)
        # 20210608_001-001
        tag_name = CI_COMMIT_REF_NAME[4:len(CI_COMMIT_REF_NAME)]
        print ("tag_name:" + tag_name)
        # 调用投产模块接口上传到minio
        # frelease/api/release/uploadMinioFile
        try:
            # 桶名称fdev/fdev-release/war/窗口名/应用名
            # 对象 应用名-tag名.后缀名
            app_file_name = app_name + "-" + tag_name + '.' + suffix
            print ("app_file_name:" + app_file_name)
            # fdev-release/war/20210608_001/msper-web-testapps/msper-web-testapps-20210608_001-001.jar
            dir_name = "war/" + pro_win_name + "/" + app_name + "/" + app_file_name
            print("dir_name:" + dir_name)
            print ("file_name:" + file_name)
            url = self.get_fdev_url() + '/frelease/api/release/uploadMinioFile'
            boundary = '----------%s' % hex(int(time.time() * 10000))
            data = []
            data.append('--%s' % boundary)
            fr = open(file_name, 'rb')
            data.append('Content-Disposition:form-data; name="file";filename="' + dir_name + '"')
            data.append('Content-Type: %s\r\n' % app_file_name)
            data.append(fr.read())
            data.append('--%s--\r\n' % boundary)
            fr.close()
            body = '\r\n'.join(data)
            try:
                req = urllib2.Request(url, data=body)
                req.add_header('Content-Type', 'multipart/form-data; boundary=%s' % boundary)
                req.add_header('source', 'back')
                resp = urllib2.urlopen(req, timeout=100)
                result = resp.read()
                print result
            except Exception, e:
                print e
        except ReferenceError as err:
            print(err)
        dir_name = '/' + dir_name
        # 发送投产模块，将包的路径返回给release
        url = self.get_fdev_url() + '/frelease/api/releasenode/application/saveProductWar'
        data = json.dumps({"gitlab_project_id": str(self.CI_PROJECT_ID), "product_tag": envs['CI_COMMIT_TAG'],
                           "pro_package_uri": str(dir_name)})
        self.post_request(url, data)

    # 获得这次CAAS持续集成 部署环境
    def get_deploy_slug(self):
        # envs = os.environ
        # CI_PROJECT_ID = envs['CI_PROJECT_ID']
        # CI_SCHEDULE = os.environ.has_key('CI_SCHEDULE')  # 如果有 CI_SCHEDULE 环境变量,就认为是 定时的

        fdev_url = self.get_fdev_url()
        if self.CI_COMMIT_REF_NAME[0:7].upper() == "RELEASE":
            # 查找 release 分支环境
            print "in uat env search."
            data = json.dumps(
                {"gitlab_project_id": int(self.CI_PROJECT_ID), "release_branch": self.CI_COMMIT_REF_NAME, "application": ""})
            url = fdev_url + "/frelease/api/releasenode/queryUatEnv"
            slug = self.post_request(url, data)["data"]
            return slug

        elif self.CI_COMMIT_REF_NAME[0:3].upper() == "PRO":
            # 查找 release 分支环境
            print "in rel env search."
            data = json.dumps(
                {"gitlab_project_id": int(self.CI_PROJECT_ID), "release_branch": self.CI_COMMIT_REF_NAME, "application": ""})
            url = fdev_url + "/frelease/api/releasenode/queryRelEnv"
            slug = self.post_request(url, data)["data"]
            return slug

        else:
            # 查找 sit 分支环境
            print "in sit env search."
            if not self.req_retail_flag:
                # 发送请求查询应用是否是零售组特殊部署应用
                send_url = fdev_url + "/fapp/api/app/queryAppAscriptionGroup"
                data = json.dumps({"name": self.rename_project_name(self.CI_PROJECT_NAME)})
                self.is_retail = self.post_request(send_url, data)["data"]
                self.req_retail_flag = True
            if self.is_retail and self.CI_SCHEDULE:
                print "零售组特殊应用定时部署发往【sit1-dmz】环境"
                return "sit1-dmz"

            data = json.dumps({"CI_PROJECT_ID": int(self.CI_PROJECT_ID), "CI_COMMIT_REF_NAME": self.CI_COMMIT_REF_NAME,
                               "CI_SCHEDULE": self.CI_SCHEDULE})
            url = fdev_url + "/fapp/api/app/get_sit_slug"
            slug = self.post_request(url, data)["data"]
            if slug:
                return slug[0]["env_name"]
            else:
                return ""

    # 查詢SCC部署环境
    def get_scc_slug(self):
        fdev_url = self.get_fdev_url()
        app = self.app_info()
        data = json.dumps({"app_id": app["id"], "deploy_env": self.get_ref_env()})
        url = fdev_url + "/fenvconfig/api/v2/env/querySccEnvByAppId"
        envs = self.post_request(url, data)["data"]
        return map(lambda env: env["name_en"], envs)

    # 查询应用详情绑定的部署平台
    def get_deploy_platform(self):
        data = json.dumps({"id": int(self.CI_PROJECT_ID)})
        url = self.get_fdev_url() + "/fapp/api/app/getAppByGitId"
        result = self.post_request(url, data)
        # --------------------------------------------------------------
        # data = json.dumps(
        #     {"code": "AAAAAAA", "data": {"caas_status": "1", "scc_status": "1", "gitlab_project_id": 123}})
        # result = json.loads(data)
        # --------------------------------------------------------------
        code = result['code']
        if code != "AAAAAAA":
            self.warn("获取持续集成绑定平台信息出错，请联系管理员。")
            sys.exit(1)

        self.deploy_platform['caas_status'] = result['data']['caas_status']
        self.deploy_platform['scc_status'] = result['data']['scc_status']
        self.app_id = result['data']['id']

    # 调用环境配置模块接口查询自定义部署实体信息
    def get_defined_deploy_info(self, definedDeployId):
        data = json.dumps({"definedDeployId": definedDeployId, "gitlabId": int(self.CI_PROJECT_ID)})
        url = self.get_fdev_url() + "/fenvconfig/api/v2/appEnv/queryDefinedInfo"
        result = self.post_request(url, data)
        code = result['code']
        if code != "AAAAAAA":
            self.warn("获取持续集成环境自定义部署变量出错，请联系管理员。")
            sys.exit(1)
        return result['data']

    # 初始化查询CaaS环境变量
    def init_caas_env(self):
        # scc-ate自动化测试环境部署
        if self.CI_SCHEDULE and self.envs.get('CI_SCHEDULE') == "sit-ate":
            return
        self.ci_environment_slug = self.get_deploy_slug()
        image_name = self.get_image_name()
        data = json.dumps(
            {"env": self.ci_environment_slug, "gitlabId": int(self.CI_PROJECT_ID),
             "image": image_name if image_name else ""})
        url = self.get_fdev_url() + "/fenvconfig/api/v2/appDeploy/queryVariablesMapping"
        result = self.post_request(url, data)
        code = result['code']
        if code != "AAAAAAA":
            self.warn("获取持续集成环境变量出错，请联系管理员。")
            sys.exit(1)
        self.CI_VARS = self.replace_decrypt(self.remove_value(result['data']))

    # 初始化查询SCC环境实体
    def init_scc_env(self):
        slug = self.get_ref_env()
        if "sit" == slug:
            if self.CI_SCHEDULE and self.envs.get('CI_SCHEDULE') == "sit-ate":
                self.info("SCC-ATE自动化测试环境定时部署。")
                self.CI_SCC_SLUG = ["scc-ate"]
            else:
                if not self.req_retail_flag:
                    # 发送请求查询应用是否是零售组特殊部署应用
                    send_url = self.get_fdev_url() + "/fapp/api/app/queryAppAscriptionGroup"
                    data = json.dumps({"name": self.rename_project_name(self.CI_PROJECT_NAME)})
                    self.is_retail = self.post_request(send_url, data)["data"]
                    self.req_retail_flag = True
                if self.is_retail and self.CI_SCHEDULE:
                    print "零售组特殊应用定时部署指定往scc-sit1环境部署"
                    self.CI_SCC_SLUG = ["scc-sit1"]
                else:
                    self.CI_SCC_SLUG = self.get_scc_slug()
        else:
            self.CI_SCC_SLUG = self.get_scc_slug()

        for slug in self.CI_SCC_SLUG:
            image_name = self.get_image_name()
            data = json.dumps(
                {"env": slug, "gitlabId": int(self.CI_PROJECT_ID),
                 "image": image_name if image_name else ""})
            url = self.get_fdev_url() + "/fenvconfig/api/v2/appDeploy/querySccVariablesMapping"
            result = self.post_request(url, data)
            code = result['code']
            if code != "AAAAAAA":
                self.warn("获取持续集成环境SCC变量出错，请联系管理员。")
                sys.exit(1)
            # self.scc[slug] = result['data']
            # 添加到数组CI_SCC_VARS中
            self.CI_SCC_VARS[slug] = result['data']

    # 获得 caas 环境变量
    def get_env(self, env_name):
        if self.CI_VARS.has_key(env_name):
            return self.CI_VARS[env_name] == "" if self.CI_VARS[env_name] == "-" else self.CI_VARS[env_name]
        else:
            return 0

    # 获得 scc 环境变量
    def get_scc_env(self, env_name, slug=""):
        if not slug:
            self.error("查询SCC环境实体信息时环境参数为空!")
            sys.exit(1)
        if self.CI_SCC_VARS[slug].has_key(env_name):
            return self.CI_SCC_VARS[slug][env_name] == "" if self.CI_SCC_VARS[slug][env_name] == "-" else \
                self.CI_SCC_VARS[slug][env_name]
        else:
            self.warn("scc环境%s的%s变量为空" % (slug, env_name))
            return ""

    # 初始化环境和变量信息
    def init_slug_and_vars(self):
        envs = os.environ
        if "definedDeployId" in envs:
            self.info("自定义部署触发流水线，definedDeployId:%s" % envs['definedDeployId'])
            response_data = self.get_defined_deploy_info(envs['definedDeployId'])
            env_data = response_data['variables']
            for key, val in env_data.items():
                if "scc" in key:
                    # 从自定义部署信息中获取scc的环境实体
                    self.CI_SCC_VARS[key] = val
                    self.CI_SCC_SLUG.append(key)
                    self.deploy_platform['scc_status'] = '1'
                else:
                    # 从自定义部署信息中获取caas的环境实体，CaaS镜像仓库的密码需要解密
                    self.CI_VARS = self.replace_decrypt(self.remove_value(val))
                    self.ci_environment_slug = key
                    self.deploy_platform['caas_status'] = '1'

            # 是否更新配置文件、更新镜像开关
            if response_data['configUpdateFlag']:
                self.CONFIG_UPDATE_FLAG = response_data['configUpdateFlag']
            if response_data['reDeployFlag']:
                self.IMAGE_UPDATE_FLAG = response_data['reDeployFlag']
            return

        self.get_deploy_platform()
        if '1' == self.deploy_platform['caas_status']:
            # 初始化 CI_VARS
            self.init_caas_env()

        if '1' == self.deploy_platform['scc_status']:
            # 初始化 CI_SCC_VARS
            self.init_scc_env()

    def init_caas_var(self,env_slug):
        # 部署环境
        self.CI_ENVIRONMENT_SLUG = env_slug
        self.FDEV_CONFIG_HOST1_IP = self.get_env('FDEV_CONFIG_HOST1_IP')
        self.FDEV_CONFIG_HOST2_IP = self.get_env('FDEV_CONFIG_HOST2_IP')
        self.FDEV_CONFIG_USER = self.get_env('FDEV_CONFIG_USER')
        self.FDEV_CONFIG_PASSWORD = self.get_env('FDEV_CONFIG_PASSWORD')
        self.FDEV_CONFIG_DIR = self.get_env('FDEV_CONFIG_DIR')
        self.FDEV_CONFIG_PORT = self.get_env('FDEV_CONFIG_PORT')

    def init_scc_var(self,env_slug):
        # 部署环境
        self.CI_ENVIRONMENT_SLUG = env_slug
        self.FDEV_CONFIG_HOST1_IP = self.get_scc_env('FDEV_CONFIG_HOST1_IP', env_slug)
        self.FDEV_CONFIG_HOST2_IP = self.get_scc_env('FDEV_CONFIG_HOST2_IP', env_slug)
        self.FDEV_CONFIG_USER = self.get_scc_env('FDEV_CONFIG_USER', env_slug)
        self.FDEV_CONFIG_PASSWORD = self.get_scc_env('FDEV_CONFIG_PASSWORD', env_slug)
        self.FDEV_CONFIG_DIR = self.get_scc_env('FDEV_CONFIG_DIR', env_slug)
        self.FDEV_CONFIG_PORT = self.get_scc_env('FDEV_CONFIG_PORT', env_slug)

    def check_env(self):
        # 只要其中一个为0、""、None、"0"，就返回False
        if (self.check_zero(self.FDEV_CONFIG_HOST1_IP) or self.check_zero(self.FDEV_CONFIG_HOST2_IP)) \
                and self.check_zero(self.FDEV_CONFIG_PORT) and self.check_zero(self.FDEV_CONFIG_USER) \
                and self.check_zero(self.FDEV_CONFIG_PASSWORD) and self.check_zero(self.FDEV_CONFIG_DIR):
            print 'check_env: True'
            return True
        else:
            print 'check_env: False'
            return False

    # 封装 scc 网络请求
    def request_get_pod(self, url, headers='', data='', method='GET'):
        return_data = {}
        request = urllib2.Request(
            url=url,
            headers=headers,
            data=data,
        )
        request.get_method = lambda: method

        try:
            response = urllib2.urlopen(request, timeout=60)
            return_data['data'] = response.read()
            return_data['code'] = response.getcode()
            return return_data, 0
        except urllib2.HTTPError as e:
            return_data['code'] = e.code
            self.error('服务器返回异常状态码')
            self.error("errorReason: ", e.reason)
            return return_data, 1
        except urllib2.URLError as e:
            return_data['code'] = 600
            self.error('无法连接到服务器,请重试')
            self.error("errorReason: ", e.reason)
            return return_data, 1

# if __name__ == '__main__':
#     util = Utils()
    # if "definedDeployId" in os.environ:
#         caas_val = util.get_env("OBJ_TEST")
#         scc_val = util.get_scc_env("OBJ_TEST", "test2-scc-sit2")
#     else:
#         caas_val = util.get_env("FDEV_CAAS_IP")
#         scc_val = util.get_scc_env("dockerservice_ip", "scc-sit1")
#
#     # 打印获取的实体信息
#     print caas_val
#     print scc_val
