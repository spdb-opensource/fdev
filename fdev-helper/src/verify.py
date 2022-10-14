#!/usr/bin/python
# -*- coding: utf-8 -*-

import urllib2
import json
import time
import ssl
import base64
import sys

import os


from urllib2 import Request, urlopen, URLError
import datetime

class Verify:

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

    # 封装 caas 网络请求
    def request_caas(self, url, headers, data='{}', method='GET'):
        print "url:", url
        print "headers:", headers
        print "data:", data
        print "method:", method

        request = urllib2.Request(
            url=url,
            headers=headers,
            data=data,
        )
        request.get_method = lambda: method
        try:
            response = urllib2.urlopen(request, context=ssl._create_unverified_context(), timeout=10)
            code = response.getcode()
            return_date = response.read()
            print "code:", code
            print "response:%s" % (json.loads(return_date))
            return json.loads(return_date)
        except urllib2.HTTPError as e:
            print '服务器返回异常状态码'
            print 'Error code: ', e.code
            sys.exit(1)
        except URLError as e:
            print '无法连接到服务器,请重试'
            print 'Reason: ', e.reason
            sys.exit(1)




    # 模拟DaoCloud登陆 返回access_token
    def login_daocloud(self, FDEV_CAAS_IP, FDEV_CASS_USERNAME, FDEV_CASS_PASSWORD):
        print "[+] 开始 login_daocloud..."
        url = FDEV_CAAS_IP + "/dce/sso/login"
        headers = {"Content-Type": "application/json",
                   "Authorization": "Basic " + base64.b64encode('%s:%s' % (FDEV_CASS_USERNAME, FDEV_CASS_PASSWORD))}
        return self.request_caas(url=url,headers=headers,method='POST')['access_token']


    # 获取应用在DaoCloud的部署状态 返回接口响应数据
    def get_app_deploy_status(self, FDEV_CAAS_IP, FDEV_CAAS_TENANT, CI_PROJECT_NAME, ACCESS_TOKEN):
        print "[+] 开始 get_app_deploy_status..."
        try:
            url = FDEV_CAAS_IP + "/dce/apps/" + CI_PROJECT_NAME
            headers = {"Content-Type": "application/json", "X-DCE-TENANT": FDEV_CAAS_TENANT,
                       "X-DCE-Access-Token": ACCESS_TOKEN}
            print "app_url", url
            print "headers:", headers
            request = urllib2.Request(url=url, headers=headers)
            response = urllib2.urlopen(request, context=ssl._create_unverified_context(),timeout=10)
            code = response.getcode()
            return_date = response.read()
            print "code:", code
            print "response:%s" % (json.loads(return_date))
            return json.loads(return_date)
        except Exception, e:
            print '获取应用在DaoCloud的部署状态失败!', e

    # 获取应用所在ip和端口
    def get_app_ip(self, result):
        try:
            if result.has_key('status') & result['status'].has_key('link'):
                return result['status']['link']
        except Exception, e:
            print '获取应用所在ip和端口失败! ', e
        return ''

    # HTTP 检查
    def check_remote_server(self, url):
        print "[+] 开始 HTTP 请求: " ,url
        request = urllib2.Request(url=url)
        try:
            response = urllib2.urlopen(request, timeout=10)
            code = response.getcode()
            return_date = response.read()
            print "code:", code
            return True
        except urllib2.HTTPError as e:
            print e.code
            return True
        except URLError as e:
            print '[-] http 请求无法连通该地址: ' , url
            print '[-] 错误信息: ', e.reason
            return False


    # 获取部署异常的应用错误原因
    def get_app_faliure_msg(self, result):
        try:
            if result.has_key('status') and result['status'].has_key('warnings') and len(
                    result['status']['warnings']) > 0:
                if result['status']['warnings'][0].has_key('message'):
                    return result['status']['warnings'][0]['message']
        except Exception, e:
            print '获取部署异常的应用错误原因失败! ', e
        return ''

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

    # 封装网络请求f dev
    def post_fdev(self, url, data):
        headers = {"Content-Type": "application/json",
                   "source": "back"}
        request = urllib2.Request(url, data, headers)
        # 超时设置为10秒
        f = urllib2.urlopen(request, timeout=10)
        response = f.read()
        f.close()

        print "data:" + data
        print "url:" + url
        print "response:%s" % (json.loads(response))
        return json.loads(response)

    def get_devploy_slug(self, CI_PROJECT_ID, CI_COMMIT_REF_NAME, CI_SCHEDULE, fdev_url):
        if CI_COMMIT_REF_NAME[0:7].upper() == "RELEASE":
            # 查找 release 分支环境
            print "in uat env search."
            data = json.dumps(
                {"gitlab_project_id": int(CI_PROJECT_ID), "release_branch": CI_COMMIT_REF_NAME, "application": ""})


            url = fdev_url + "/frelease/api/releasenode/queryUatEnv"
            slug = self.post_fdev(url=url,data=data)["data"]
            return slug
        elif CI_COMMIT_REF_NAME[0:3].upper() == "PRO":
            # 查找 release 分支环境
            print "in rel env search."
            data = json.dumps(
                {"gitlab_project_id": int(CI_PROJECT_ID), "release_branch": CI_COMMIT_REF_NAME, "application": ""})
            url = fdev_url + "/frelease/api/releasenode/queryRelEnv"
            slug = self.post_fdev(url=url, data=data)["data"]
            return slug

        else:
            # 查找 sit 分支环境
            print "in sit env search."
            data = json.dumps(
                {"CI_PROJECT_ID": int(CI_PROJECT_ID), "CI_COMMIT_REF_NAME": CI_COMMIT_REF_NAME,
                 "CI_SCHEDULE": CI_SCHEDULE})
            url = fdev_url + "/fapp/api/app/get_sit_slug"

            slug = self.post_fdev(url=url, data=data)["data"][0]["env_name"]
            return slug

    # 获得指定环境中所有环境变量
    def get_variables(self, DEVOPS_DEPLOY_SLUG, fdev_url):
        data = json.dumps({"env_name": DEVOPS_DEPLOY_SLUG})
        url = fdev_url + "/fapp/api/environment/query"
        return self.post_fdev(url, data)["data"][0]["env_variables"]

    # 重命名 CI_PROJECT_NAME 将 -parent 去掉
    def rename_project_name(self, CI_PROJECT_NAME):
        if CI_PROJECT_NAME[-7:] == "-parent":
            return CI_PROJECT_NAME[:-7]
        return CI_PROJECT_NAME

    # 通过fdev部署
    def use_fdev(self):
        envs = os.environ  # 系统环境变量
        self.info("通过fdev设置环境变量部署")
        CI_PROJECT_ID = envs['CI_PROJECT_ID']  # 项目 gitlab io
        CI_COMMIT_REF_NAME = envs['CI_COMMIT_REF_NAME']  # 分支名
        CI_SCHEDULE = os.environ.has_key('CI_SCHEDULE')  # 如果有 CI_SCHEDULE 环境变量,就认为是 定时的
        fdev_url = "pro地址"
        if os.environ.has_key('CI_FDEV_ENV'):
            CI_FDEV_ENV = envs['CI_FDEV_ENV']
            print "CI_FDEV_ENV:" + CI_FDEV_ENV
            fdev_url = self.get_fdev_slug(CI_FDEV_ENV)

        print "fdev_url:" + fdev_url
        DEVOPS_DEPLOY_SLUG = self.get_devploy_slug(CI_PROJECT_ID, CI_COMMIT_REF_NAME, CI_SCHEDULE, fdev_url)
        print "DEVOPS_DEPLOY_SLUG:" + DEVOPS_DEPLOY_SLUG  # 系统部署环境
        vars = self.get_variables(DEVOPS_DEPLOY_SLUG, fdev_url)  # 环境变量
        print "vars:"
        print vars
        return vars

    def is_auto(self):
        envs = os.environ  # 系统环境变量
        # 通过手动设置环境变量部署
        self.info("通过手动设置环境变量部署")
        self.info("CI_CAAS_IP: ", envs['CI_CAAS_IP'])
        self.info("profileName: ", envs['profileName'])
        self.info("hostLogsPath: ", envs['hostLogsPath'])
        self.info("subnet: ", envs['subnet'])
        self.info("hostLogsPath: ", envs['hostLogsPath'])
        self.info("CI_CAAS_USER: ", envs['CI_CAAS_USER'])
        self.info("eurekaServerUri: ", envs['eurekaServerUri'])
        self.info("CI_CAAS_REGISTRY_PASSWORD: ", envs['CI_CAAS_REGISTRY_PASSWORD'])
        self.info("configServerUri: ", envs['configServerUri'])
        self.info("CI_CAAS_REGISTRY_NAMESPACE: ", envs['CI_CAAS_REGISTRY_NAMESPACE'])
        self.info("CI_CAAS_REGISTRY: ", envs['CI_CAAS_REGISTRY'])
        self.info("CI_CAAS_REGISTRY_USER: ", envs['CI_CAAS_REGISTRY_USER'])
        self.info("UNIONAPI_CONF_FILENAME: ", envs['UNIONAPI_CONF_FILENAME'])
        self.info("CI_CAAS_IP: ", envs['CI_CAAS_IP'])
        self.info("CI_CAAS_TENANT: ", envs['CI_CAAS_TENANT'])
        self.info("CI_CAAS_SECRET: ", envs['CI_CAAS_SECRET'])
        self.info("CI_CAAS_PWD: ", envs['CI_CAAS_PWD'])
        self.info("CI_CAAS_ACCESS: ", envs['CI_CAAS_ACCESS'])
        self.info("eureka1ServerUri: ", envs['eureka1ServerUri'])
        self.info("EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: ", envs['EUREKA_CLIENT_SERVICEURL_DEFAULTZONE'])
        self.info("SPRING_CLOUD_CONFIG_URI: ", envs['SPRING_CLOUD_CONFIG_URI'])
        return envs

    def main(self):
        envs = os.environ  # 系统环境变量
        CI_PROJECT_ID = envs['CI_PROJECT_ID']  # 项目 gitlab io
        CI_COMMIT_REF_NAME = envs['CI_COMMIT_REF_NAME']  # 分支名
        CI_SCHEDULE = os.environ.has_key('CI_SCHEDULE')  # 如果有 CI_SCHEDULE 环境变量,就认为是 定时的
        print "CI_PROJECT_ID:" + str(CI_PROJECT_ID)
        print "CI_COMMIT_REF_NAME:" + CI_COMMIT_REF_NAME
        print "CI_SCHEDULE:" + str(CI_SCHEDULE)

        vars = {}
        if envs.has_key("CI_CAAS_IP"):
            vars = self.is_auto()
        else:
            vars = self.use_fdev()

        # 项目名 去掉 -parent
        CI_PROJECT_NAME = self.rename_project_name(envs['CI_PROJECT_NAME'])
        FDEV_CAAS_IP = "https://" + vars['CI_CAAS_IP']
        FDEV_CAAS_TENANT = vars['CI_CAAS_TENANT']
        FDEV_CASS_USERNAME = vars['CI_CAAS_USER']
        FDEV_CASS_PASSWORD = vars['CI_CAAS_PWD']

        # 一下参数为本地调试运行
        # FDEV_CAAS_IP = 'https://xxx'
        # FDEV_CAAS_TENANT = 'ebank-auto-tenant'
        # FDEV_CASS_USERNAME = 'ebank'
        # FDEV_CASS_PASSWORD = 'xxx'
        # CI_PROJECT_NAME = 'mspmk-web-insurance'  # mspmk-web-insurance

        ACCESS_TOKEN = self.login_daocloud(FDEV_CAAS_IP=FDEV_CAAS_IP,
                                           FDEV_CASS_username=xxx_CASS_USERNAME,
                                           FDEV_CASS_PASSWORD=FDEV_CASS_PASSWORD)

        print "[+] ACCESS_TOKEN:", ACCESS_TOKEN
        flag = False  # 是否部署成功标识
        result = ''
        count = 0
        for index in range(0, 6):
            count = count + 1
            result = self.get_app_deploy_status(FDEV_CAAS_IP=FDEV_CAAS_IP,
                                                FDEV_CAAS_TENANT=FDEV_CAAS_TENANT,
                                                CI_PROJECT_NAME=CI_PROJECT_NAME,
                                                ACCESS_TOKEN=ACCESS_TOKEN)
            # 获得 url ==> ip:port
            url = self.get_app_ip(result=result)
            if url == '':
                continue

            # http 请求检查
            print "[+] 从CaaS 上获得应用部署后的 IP PORT : ",  url
            flag = self.check_remote_server("http://" + url)
            if flag == True:
                break

            print "[+] 30 秒后将重新探测部署情况,剩下重试次数:", (6 - count)
            time.sleep(30)

        if flag == False:
            failure_msg = self.get_app_faliure_msg(result=result)
            print '[-] 自动验证CaaS 返回的信息:', failure_msg
            print "[-] CaaS 部署成功后, 自动探测该应用不能对外提供服务, 请移步到 CaaS上 查看部署详情！！！"
            print "[-] CaaS 部署成功后, 自动探测该应用不能对外提供服务, 请移步到 CaaS上 查看部署详情！！！"
            print "[-] CaaS 部署成功后, 自动探测该应用不能对外提供服务, 请移步到 CaaS上 查看部署详情！！！"
            print "[-] CaaS 地址: ", FDEV_CAAS_IP
            print "[-] CaaS 租户: ", FDEV_CAAS_TENANT
        else:
            print "CaaS 部署成功 !"
    def test(self):
        print self.check_remote_server(url="xxx:8080")

if __name__ == '__main__':
    verify = Verify()
    verify.main()