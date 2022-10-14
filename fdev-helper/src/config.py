#!/usr/bin/python
# -*- coding: utf-8 -*-
import json
import os
import sys
import pexpect

from utils import Utils

reload(sys)
sys.setdefaultencoding('utf-8')

class Config:

    def __init__(self):
        self.utils = Utils()

        # 获得部署的目录
        self.DEPLOY_DIR = self.utils.get_file_dir()
    def replace_yaml_variables(self):
        self.fdev_url = self.utils.get_fdev_url()

        envs = os.environ
        CI_PROJECT_DIR = envs['CI_PROJECT_DIR']
        CI_PROJECT_ID = envs['CI_PROJECT_ID']

        # 部署环境
        CI_ENVIRONMENT_SLUG = self.utils.get_devploy_slug()

        # 应用英文名,eureka 上服务名
        name_en = self.utils.get_name_en(CI_PROJECT_ID, self.fdev_url)
        self.fdev_runtime_file = self.DEPLOY_DIR + "/fdev-application.properties"
        # nbh-net-interent-sit.properties
        self.fdev_runtime_path = self.DEPLOY_DIR + "/" + name_en + "-" + str(CI_ENVIRONMENT_SLUG).lower() + ".properties"

        # 获取应用的配置模板文件内容
        with open(self.fdev_runtime_file, "r") as fr:
            originFileContent = fr.read()

        # 获取配置文件信息
        data = json.dumps({"env_name": str(CI_ENVIRONMENT_SLUG), "content": str(originFileContent), "type": "1", "project_id": str(CI_PROJECT_ID)})
        url = self.fdev_url + "/fenvconfig/api/v2/configfile/previewConfigFile"
        fileContent = self.utils.post_request(url, data)["data"]
        if str(fileContent) is None or len(str(fileContent)) == 0:
            self.utils.warn('当前应用没有 配置模板文件, 请通过任务去添加配置模板')
            sys.exit(-1)
        with open(self.fdev_runtime_path, "w") as fw:
            fw.write(fileContent)


    # 上传配置文件到配置中心
    def push_config(self):
        FDEV_CONFIG_HOST1_IP = self.utils.get_env('FDEV_CONFIG_HOST1_IP')
        FDEV_CONFIG_HOST2_IP = self.utils.get_env('FDEV_CONFIG_HOST2_IP')
        FDEV_CONFIG_PORT = self.utils.get_env('FDEV_CONFIG_PORT')
        FDEV_CONFIG_USER = self.utils.get_env('FDEV_CONFIG_USER')
        FDEV_CONFIG_PASSWORD = self.utils.get_env('FDEV_CONFIG_PASSWORD')
        FDEV_CONFIG_DIR = self.utils.get_env('FDEV_CONFIG_DIR')

        cmd = "scp %s %s@%s:%s" % (
            self.fdev_runtime_path,FDEV_CONFIG_USER,FDEV_CONFIG_HOST1_IP,FDEV_CONFIG_DIR)
        print cmd
        self.utils.push_config(cmd,FDEV_CONFIG_PASSWORD)

        envs = os.environ
        # 若是 tag 打出的包 需要发送后台去保存配置文件和发邮件
        if envs.has_key('CI_COMMIT_TAG') == True:
            CI_COMMIT_TAG = envs["CI_COMMIT_TAG"]
            CI_PROJECT_ID = envs["CI_PROJECT_ID"]
            with open(self.fdev_runtime_file, "r") as fr:
                originFileContent = fr.read()
            data = json.dumps({"branch": str(CI_COMMIT_TAG).lower(), "content": str(originFileContent),
                               "gitlab_project_id": int(CI_PROJECT_ID)})
            url = self.fdev_url + "/fenvconfig/api/v2/configfile/saveConfigProperties"
            self.utils.post_request(url, data)

    def main(self):
        # 生成对应环境配置文件
        self.replace_yaml_variables()
        self.push_config()


if __name__ == '__main__':
    config = Config()
    vars = os.environ
    if vars.has_key("dce_fdev_caas_registry"):
        print "\033[1;31m 需要自己手动向配置中心放置文件.. \033[0m"
    else:
        config.main()