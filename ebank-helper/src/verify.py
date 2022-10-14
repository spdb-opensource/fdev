#!/usr/bin/python
# -*- coding: utf-8 -*-

import base64
import json
import os
import ssl
import sys
import time
import urllib2
from urllib2 import URLError

from utils import Utils

reload(sys)
sys.setdefaultencoding("utf-8")
print sys.getdefaultencoding()


class Verify:

    def __init__(self):
        self.util = Utils()
        # self.init_env()

    def init_env(self):
        envs = os.environ  # 系统环境变量
        self.CI_PROJECT_ID = envs['CI_PROJECT_ID']  # 项目 gitlab io
        self.CI_COMMIT_REF_NAME = envs['CI_COMMIT_REF_NAME']  # 分支名
        self.CI_SCHEDULE = os.environ.has_key('CI_SCHEDULE')  # 如果有 CI_SCHEDULE 环境变量,就认为是 定时的

        print "CI_PROJECT_ID:" + str(self.CI_PROJECT_ID)
        print "CI_COMMIT_REF_NAME:" + self.CI_COMMIT_REF_NAME
        print "CI_SCHEDULE:" + str(self.CI_SCHEDULE)

        self.CI_PROJECT_NAME = self.util.rename_project_name(envs['CI_PROJECT_NAME'])
        self.FDEV_CAAS_IP = "https://" + self.util.get_env('FDEV_CAAS_IP')
        self.FDEV_CAAS_TENANT = self.util.get_env('FDEV_CAAS_TENANT')
        self.FDEV_CAAS_USERNAME = self.util.get_env('FDEV_CAAS_USER')
        self.FDEV_CAAS_PASSWORD = self.util.get_env('FDEV_CAAS_PWD')

    # 模拟DaoCloud登陆 返回access_token
    def login_daocloud(self):
        self.util.info("[+] 开始 login_daocloud...")
        url = self.FDEV_CAAS_IP + "/dce/sso/login"
        headers = {"Content-Type": "application/json",
                   "Authorization": "Basic " + base64.b64encode(
                       '%s:%s' % (self.FDEV_CAAS_USERNAME, self.FDEV_CAAS_PASSWORD))}

        response = self.util.request_caas(url=url, headers=headers, data='{}', method='POST')
        data = response.get("data")
        code = response.get("code")
        self.util.info("code:", code)
        result_data = json.loads(data)
        return result_data['access_token']

    # 获取应用在DaoCloud的部署状态 返回接口响应数据
    def get_app_deploy_status(self, ACCESS_TOKEN):
        self.util.info("[+] 开始 get_app_deploy_status...")
        try:
            url = self.FDEV_CAAS_IP + "/dce/apps/" + self.CI_PROJECT_NAME
            headers = {"Content-Type": "application/json", "X-DCE-TENANT": self.FDEV_CAAS_TENANT,
                       "X-DCE-Access-Token": ACCESS_TOKEN}
            self.util.info("app_url", url)
            self.util.info("headers:", headers)
            request = urllib2.Request(url=url, headers=headers)
            response = urllib2.urlopen(request, context=ssl._create_unverified_context(), timeout=10)
            code = response.getcode()
            return_date = response.read()
            self.util.info("code:", code)
            self.util.info("response:", (json.loads(return_date)))
            return json.loads(return_date)
        except Exception, e:
            self.util.error('获取应用在DaoCloud的部署状态失败!', e)

    # 获取应用所在ip和端口
    def get_app_ip(self, result):
        try:
            if result.has_key('status') & result['status'].has_key('link'):
                return result['status']['link']
        except Exception, e:
            self.util.error('获取应用所在ip和端口失败! ', e)
        return ''

    # HTTP 检查
    def check_remote_server(self, url):
        self.util.info("[+] 开始 HTTP 请求: ", url)
        request = urllib2.Request(url=url)
        try:
            response = urllib2.urlopen(request, timeout=10)
            code = response.getcode()
            return_date = response.read()
            self.util.info('code:', code)
            return True
        except urllib2.HTTPError as e:
            self.util.warn(e.code)
            return True
        except URLError as e:
            self.util.error('[-] http 请求无法连通该地址: ', url)
            self.util.error('[-] 错误信息: ', e.reason)
            return False

    # 获取部署异常的应用错误原因
    def get_app_faliure_msg(self, result):
        try:
            if result.has_key('status') and result['status'].has_key('warnings') and len(
                    result['status']['warnings']) > 0:
                if result['status']['warnings'][0].has_key('message'):
                    return result['status']['warnings'][0]['message']
        except Exception, e:
            self.util.error('获取部署异常的应用错误原因失败! ', e)
        return ''

    def main(self):
        if self.util.CI_SCHEDULE and self.util.envs.get('CI_SCHEDULE') == "sit-ate":
            self.util.info("scc-ate定时部署。caas部署停止")
            return
        if "0" == self.util.deploy_platform['caas_status']:
            self.util.info("----当前环境未选择CaaS平台----")
            return

        if "sit" == self.util.get_ref_env() and self.util.is_retail and not self.util.CI_SCHEDULE:
            print "sit分支零售组特殊应用非定时部署不做配置推送"
            sys.exit(0)

        self.init_env()
        ACCESS_TOKEN = self.login_daocloud()
        self.util.info("[+] ACCESS_TOKEN:", ACCESS_TOKEN)

        flag = False  # 是否部署成功标识
        result = ''
        count = 0

        for index in range(0, 6):
            count = count + 1
            result = self.get_app_deploy_status(ACCESS_TOKEN=ACCESS_TOKEN)
            # 获得 url ==> ip:port
            url = self.get_app_ip(result=result)
            if url == '':
                continue

            # http 请求检查
            self.util.info("[+] 从CaaS 上获得应用部署后的 IP PORT : ", url)
            flag = self.check_remote_server("http://" + url)
            if flag == True:
                break

            self.util.info("[+] 30 秒后将重新探测部署情况,剩下重试次数:", (6 - count))
            time.sleep(30)

        if flag == False:
            failure_msg = self.get_app_faliure_msg(result=result)
            self.util.warn('[-] 自动验证CaaS 返回的信息:', failure_msg)
            self.util.warn("[-] CaaS 部署成功后, 自动探测该应用不能对外提供服务, 请移步到 CaaS上 查看部署详情！！！")
            self.util.warn("[-] CaaS 部署成功后, 自动探测该应用不能对外提供服务, 请移步到 CaaS上 查看部署详情！！！")
            self.util.warn("[-] CaaS 部署成功后, 自动探测该应用不能对外提供服务, 请移步到 CaaS上 查看部署详情！！！")
            self.util.warn("[-] CaaS 地址: ", self.FDEV_CAAS_IP)
            self.util.warn("[-] CaaS 租户: ", self.FDEV_CAAS_TENANT)
        else:
            self.util.info("CaaS 部署成功 !")


if __name__ == '__main__':
    if 'CI_FDEV_ENV' not in os.environ:
        verify = Verify()
        verify.main()
