#!/usr/bin/python
# -*- coding: utf-8
import datetime
import json
import os
import sys
import time

import yaml

from cassApi import CassApi
from scc import Scc
from utils import Utils


class Deploy:
    def __init__(self):
        self.utils = Utils()
        # 获得部署的目录
        self.DEPLOY_DIR = self.utils.get_file_dir()

        self.app_info = self.utils.app_info()

    # 微服务应用类型 插入 边车
    def insert_file_beat(self, deployment_json):
        if self.utils.is_micro_service(self.app_info) == False:
            return deployment_json
        if self.utils.is_allow_env() == False:
            return deployment_json
        fileBeat = {
            "name": self.utils.file_beat_name,
            "image": "xxx/mbpe-public-docker-local/common/filebeat:latest",
            "volumeMounts": [
                {
                    "mountPath": "/logs/",
                    "name": "logs-storage"
                }
            ],
            "env": [
                {
                    "name": "CI_PROJECT_NAME",
                    "value": self.utils.CI_PROJECT_NAME
                },
                {
                    "name": "CI_ENVIRONMENT_SLUG",
                    "value": self.utils.ci_environment_slug
                },
                {
                    "name": "CI_LOG_FILE",
                    "value": "/logs/*HOSTNAME-trans.log.crn"
                },
                {
                    "name": "FDEV_APP_GROUP",
                    "value": self.app_info['FDEV_APP_GROUP'].encode("utf-8")
                },
                {
                    "name": "KAFKA_TOPIC",
                    "value": "ebank-filebeat"
                }
            ]
        }
        if deployment_json.has_key("spec") \
                and deployment_json['spec'].has_key("template") \
                and deployment_json['spec']['template'].has_key("spec") \
                and deployment_json['spec']['template']['spec'].has_key("containers"):
            deployment_json['spec']['template']['spec']['containers'].append(fileBeat)
            self.utils.info("insert filebeat sidecar yaml: ", fileBeat)
        return deployment_json

    # 替换模板变量
    def replace_yaml_variables(self, yaml_file):
        self.utils.info("yaml_file:", yaml_file)
        yaml_str = ""
        with open(yaml_file) as f:
            for line in f.readlines():
                # 判断当前行 是否存在{{  }}这样的字符串
                start_num = line.find("{{ ")
                end_num = line.rfind(" }}")
                if start_num > 0 and end_num > 0:
                    key = line[start_num + 3:end_num]
                    first = key.find("-")
                    last = key.rfind(".")
                    value = str(self.utils.get_env(key))
                    # 若包括"-"和"."则表示为数组，"-"和"."之间的为数组下标
                    if first > 0 and last > 0:
                        name = key.split(".")[1]
                        index = int(key[first + 1:last])
                        field = key[0:first]
                        self.utils.info("field:", field)
                        self.utils.info("value:", self.utils.get_env(field))
                        self.utils.info("index:", index)
                        self.utils.info("name:", name)
                        if not self.utils.get_env(field):
                            value = ""
                        else:
                            if type(self.utils.get_env(field)) == str or type(self.utils.get_env(field)) == unicode:
                                value = json.loads(self.utils.get_env(field))
                            else:
                                self.utils.info(type(self.utils.get_env(field)))
                                value = self.utils.get_env(field)

                            if not type(index) == int:
                                self.utils.info(type(index))
                                index = int(index)
                            value = str(value[index][name])
                    new_str = line.replace("{{ " + key + " }}", value)
                    yaml_str = yaml_str + "\n" + new_str
                else:
                    yaml_str = yaml_str + "\n" + line
        return yaml_str

    # 往 caas 部署
    def deploy_caas(self, deployment_json, selector_json):
        self.cassApi.deploy_cass_checksecret()
        response = self.cassApi.get_app()
        if response.get("code") == 200:
            self.utils.info('该应用已存在，执行更新操作')
            # 判断 这次提交是否包含 deployment.yaml 是则 先删除再创建 不是 则只更新镜像
            # if self.utils.is_commit_deployment() or \
            #         (self.utils.is_micro_service(self.app_info)
            #          and self.utils.is_have_file_beat(response.get("data"))
            #          and self.utils.is_allow_env()):
            if self.utils.is_commit_deployment():
                self.cassApi.delete_app()
                self.cassApi.create_app(deployment_json=deployment_json, selector_json=selector_json)
            else:
                code = self.cassApi.updateImage()["code"]
                print "put 更新返回 状态码: ", code
                if code == 422:
                    self.utils.warn("方案一")
                    self.utils.warn("请手动删除在 CaaS 上该应用,然后重试。")
                    self.utils.warn("方案二")
                    self.utils.warn("请在CaaS手动更新镜像")
                    sys.exit(1)
        else:
            # 创建 app
            self.utils.info('该应用不存在, 创建 app')
            self.cassApi.create_app(deployment_json=deployment_json, selector_json=selector_json)

    def main(self):
        if self.utils.CI_SCHEDULE and self.utils.envs.get('CI_SCHEDULE') == "sit-ate":
            self.utils.info("scc-ate定时部署。caas部署停止")
            return
        self.cassApi = CassApi()
        # 替换系统环境变量
        self.utils.cmd(
            "envsubst < " + self.DEPLOY_DIR + "/deployment.yaml > " + self.DEPLOY_DIR + "/temp-deployment.yaml")
        self.utils.cmd("envsubst < " + self.DEPLOY_DIR + "/selector.yaml > " + self.DEPLOY_DIR + "/temp-selector.yaml")

        # 替换模板变量
        deployment_str = self.replace_yaml_variables(yaml_file=self.DEPLOY_DIR + "/temp-deployment.yaml")
        # 添加边车
        # deployment_json = json.dumps(self.insert_file_beat(yaml.load(deployment_str)), indent=4)
        deployment_json = json.dumps(yaml.load(deployment_str), indent=4)

        # 将值为 0 的所属的块剔除
        deployment_json = json.dumps(self.utils.remove_no_variables(deployment_json))
        selector_str = self.replace_yaml_variables(yaml_file=self.DEPLOY_DIR + "/temp-selector.yaml")
        selector_json = json.dumps(yaml.load(selector_str), indent=4)

        if self.utils.IMAGE_UPDATE_FLAG == "0":
            self.utils.warn("===gitci自定义部署方式，镜像不需要CaaS部署===")
            return
        # caas 部署
        self.deploy_caas(deployment_json=deployment_json, selector_json=selector_json)


if __name__ == '__main__':
    if 'CI_PIPELINE_ID' in os.environ:
        os.environ.setdefault("GITLAB_CI_PIPELINE_ID", os.environ['CI_PIPELINE_ID'])

    deploy = Deploy()
    if 'CI_FDEV_ENV' not in os.environ:
        if "sit" == deploy.utils.get_ref_env() and deploy.utils.is_retail and not deploy.utils.CI_SCHEDULE:
            print "sit分支零售组特殊应用非定时部署不做应用部署"
            sys.exit(0)

        curtime = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        if "1" == deploy.utils.deploy_platform['caas_status']:
            print "\033[1;32m %s [info] =================开始部署caas================= \033[0m" % (curtime)
            deploy.main()

        if "1" == deploy.utils.deploy_platform['scc_status']:
            scc = Scc()
            scc.main()
