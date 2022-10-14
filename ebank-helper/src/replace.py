#!/usr/bin/python
# -*- coding: utf-8 -*-
import datetime
import json
import os
import re
import sys
import urllib2
from urllib2 import URLError

reload(sys)
sys.setdefaultencoding("utf-8")
print sys.getdefaultencoding()

__version__ = "20200924_001"

# node后端应用接入环境配置模块的脚本
class Replace:
    def __init__(self):
        self.info("[+] 镜像版本: " + __version__)
        envs = os.environ
        # 获取fdev对应环境的接口地址
        self.fdev_url = self.get_fdev_url()
        self.envs = envs
        # 应用ID
        self.CI_PROJECT_ID = envs['CI_PROJECT_ID']
        self.CI_PROJECT_DIR = envs['CI_PROJECT_DIR']
        # 环境 sit,uat,release
        self.CI_ENVIRONMENT_SLUG = self.get_deploy_slug()
        # 分支
        self.CI_COMMIT_REF_NAME = envs['CI_COMMIT_REF_NAME']

    '''
        file_list:列表文件，里面记录了所有需要更改的文件名,file_list记录了file_list.txt的绝对地址，里面存储的所有需要修改的
        配置文件的相对位置，相对路径均是相对于file_list.txt
        data:该应用所需要的实体，并使用实体中的内容将其进行替换
    '''

    def change(self, file_list, data):
        msg = ""
        msg_error = ""
        with open(file_list) as f:
            for file in f.readlines():
                # 循环读取文件中的每一个文件
                new_content = ""
                un_paired_key = []
                # 只有键没有值
                no_value = []
                # 记录每个文件中的$<>的情况
                msg_sub_error = ""
                with open(self.CI_PROJECT_DIR + '/' + file.strip()) as ff:
                    for line in ff.readlines():
                        # 循环读取每行数据，检测其是否存在$<>，存在则将其替换
                        res = re.findall(r'[$][<](.*?)[>]', line)
                        # 该行存在$<>
                        if len(res) > 0:
                            for s in res:
                                if s and data.has_key(s):
                                    line = line.replace('$<' + s + '>', data[s])
                                    if len(re.findall(r'[$][<](.*?)[>]', line)):
                                        continue
                                    new_content = new_content + line
                                    if not data[s]:
                                        un_paired_key.append(s)
                                elif s:
                                    un_paired_key.append(s)
                                    new_content = new_content + line
                                else:
                                    msg_sub_error = file.strip() + "文件中存在$<>的错误配置格式，$<>中不可为空，请检查!\n"
                                    new_content = new_content + line
                        # 该行不存在$<>
                        else:
                            new_content = new_content + line

                    if len(un_paired_key) > 0:
                        # 存在不匹配的key
                        for i in un_paired_key:
                            if un_paired_key.index(i) == 0:
                                msg = msg + file.strip() + "文件中的" + i
                                if len(un_paired_key) == 1:
                                    msg = msg + '在' + self.CI_ENVIRONMENT_SLUG + '环境中没有值！\n'
                            elif un_paired_key.index(i) == len(un_paired_key) - 1:
                                msg = msg + "," + i + '在' + self.CI_ENVIRONMENT_SLUG + '环境中没有值！\n'
                            else:
                                msg = msg + "," + i
                    msg_error = msg_error + msg_sub_error
                # 将新内容写入原来的文件
                with open(self.CI_PROJECT_DIR + '/' + file.strip(), "w") as ff:
                    ff.write(new_content)

        if msg != "" or msg_error != "":
            if msg != "":
                self.error(msg)
                self.error("请联系fdev环境配置管理员添加实体环境映射值！")
            if msg_error != "":
                self.error(msg_error)
            sys.exit(1)

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
        else:
            fdev_url = fdev_pro_url
        return fdev_url

    # 获取fdev_url
    def get_fdev_url(self):
        fdev_url = "xxx"
        if os.environ.has_key('CI_FDEV_ENV'):
            CI_FDEV_ENV = os.environ['CI_FDEV_ENV']
            print "CI_FDEV_ENV:", CI_FDEV_ENV
            return self.get_fdev_slug(CI_FDEV_ENV)
        return fdev_url

    # 获得这次持续集成 部署环境
    def get_deploy_slug(self):
        envs = os.environ
        if envs.has_key("dce_fdev_caas_registry"):
            return "通过手动部署"
        if envs.has_key("DEPLOY_PROD_ENV"):
            return envs.get("DEPLOY_PROD_ENV")
        CI_PROJECT_ID = envs['CI_PROJECT_ID']
        CI_COMMIT_REF_NAME = envs['CI_COMMIT_REF_NAME']
        CI_SCHEDULE = os.environ.has_key('CI_SCHEDULE')  # 如果有 CI_SCHEDULE 环境变量,就认为是 定时的

        if CI_COMMIT_REF_NAME[0:7].upper() == "RELEASE":
            # 查找 release 分支环境
            print "in uat env search."
            data = json.dumps(
                {"gitlab_project_id": int(CI_PROJECT_ID), "release_branch": CI_COMMIT_REF_NAME, "application": ""})
            url = self.get_fdev_url() + "/frelease/api/releasenode/queryUatEnv"
            slug = self.post_request(url, data)["data"]
            return slug

        elif CI_COMMIT_REF_NAME[0:3].upper() == "PRO":
            # 查找 release 分支环境
            print "in rel env search."
            data = json.dumps(
                {"gitlab_project_id": int(CI_PROJECT_ID), "release_branch": CI_COMMIT_REF_NAME, "application": ""})
            url = self.get_fdev_url() + "/frelease/api/releasenode/queryRelEnv"
            slug = self.post_request(url, data)["data"]
            return slug

        else:
            # 查找 sit 分支环境
            print "in sit env search."
            data = json.dumps(
                {"CI_PROJECT_ID": int(CI_PROJECT_ID), "CI_COMMIT_REF_NAME": CI_COMMIT_REF_NAME,
                 "CI_SCHEDULE": CI_SCHEDULE})
            url = self.get_fdev_url() + "/fapp/api/app/get_sit_slug"
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
                self.error('无法连接到服务器,请重试')
                print 'Reason: ', e.reason
                sys.exit(-1)
            elif hasattr(e, 'code'):
                self.error('服务器返回异常状态码')
                self.error('Error code: ', e.code)
                sys.exit(-1)

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


if __name__ == '__main__':

    replace = Replace()
    path = replace.CI_PROJECT_DIR + "/gitlab-ci/file_list.txt"
    # 判断是否存在指定的文件列表
    if os.path.exists(path):
        data = {}
        post_data = json.dumps(
            {"gitlabId": int(replace.CI_PROJECT_ID), "branch": replace.CI_COMMIT_REF_NAME,
             "env_name": replace.CI_ENVIRONMENT_SLUG, "node": "1"})
        url = replace.fdev_url + "/fenvconfig/api/v2/appConfig/queryConfigVariables"
        entity_data = replace.post_request(url, post_data)["data"]
        for key, value in zip(entity_data.keys(), entity_data.values()):
            data[key] = value
        replace.change(path, data)
    else:
        print "不存在file_list.txt"
