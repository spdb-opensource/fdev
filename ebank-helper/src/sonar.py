#!/usr/bin/python
# -*- coding: utf-8 -*-
import datetime
import json
import os
import sys
import urllib2


# sonar扫描，更新任务模块 sonar id 的脚本
class Sonar:
    def __init__(self):
        self.success = "AAAAAAA"

    # 获得 fdev url
    def get_fdev_url(self, fdev_env):
        # fdev 接口地址
        fdev_url = "xxx"
        fdev_sit_url = "xxx"
        fdev_rel_url = "xxx"
        if fdev_env == "SIT":
            fdev_url = fdev_sit_url
        elif fdev_env == "REL":
            fdev_url = fdev_rel_url
        return fdev_url

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
            response = urllib2.urlopen(request, timeout=100)
            code = response.getcode()
            return_date = response.read()
            self.info("code:", code)
            print "response:%s" % (json.loads(return_date))
            return json.loads(return_date)
        except urllib2.URLError as e:
            if hasattr(e, 'reason'):
                self.warn('无法连接到服务器,请重试')
                print 'Reason: ', e.reason
                sys.exit(-1)
            elif hasattr(e, 'code'):
                self.error('服务器返回异常状态码')
                self.error('Error code: ', e.code)
                sys.exit(-1)

    def get_sonar_task_id(self, report_path, log_path):
        if os.path.exists(report_path) is True:
            with open(report_path) as report_task:
                try:
                    sonar_report = report_task.read()
                    sonar_report_splits = sonar_report.split("\n")
                    for line in sonar_report_splits:
                        if line.startswith("ceTaskId"):
                            sonar_task_id = line.split("=")[1]
                except Exception, err:
                    print('解析文件失败' + err.message)
                    sys.exit(-1)
                else:
                    # 扫描成功，先打印日志，再将nas上的日志删除
                    self.print_log(log_path)
                    os.remove(log_path)
                    return sonar_task_id
        else:
            # 打印日志
            self.print_log(log_path)
            return log_path

    def print_log(self, log_path):
        if os.path.exists(log_path) is True:
            with open(log_path) as log_content:
                print log_content.read()

    def main(self, fdev_env, app_name_en, app_branch, report_path, log_path):
        sonar_task_id = self.get_sonar_task_id(report_path, log_path)
        url = self.get_fdev_url(fdev_env) + "/ftask/api/sonarqube/updateTaskSonarId"
        data = json.dumps({"feature_id": app_branch, "web_name_en": app_name_en, "sonar_id": sonar_task_id})
        response = self.post_request(url, data)
        if self.success == response['code']:
            response_data = response['data']
            self.info(response_data)
        else:
            self.error("发送任务模块后台交易出错：", response['msg'])
            sys.exit(-1)

    def save_sonar_info(self, fdev_env, project_id):
        """再发一个交易 请求新写的接口（该接口读取sonar的扫描信息 存入数据库）"""
        url = self.get_fdev_url(fdev_env) + "/fsonar/api/sonar/saveBugReffer"  # 填写对应新接口的路径
        pakg = {
            "id": project_id
        }
        data = json.dumps(pakg)
        response = self.post_request(url, data)
        if self.success == response['code']:
            response_data = response['data']
            self.info(response_data)
        else:
            self.error("发送sonar模块后台交易出错：", response['msg'])
            sys.exit(-1)


if __name__ == '__main__':
    fdev_env = sys.argv[1]
    project_id = sys.argv[2]
    app_name_en = sys.argv[3]
    app_branch = sys.argv[4]
    report_path = sys.argv[5]
    log_path = sys.argv[6]
    sonar = Sonar()
    if app_branch.upper() == "SIT":
        sonar.save_sonar_info(fdev_env, project_id)
    else:
        sonar.main(fdev_env, app_name_en, app_branch, report_path, log_path)
