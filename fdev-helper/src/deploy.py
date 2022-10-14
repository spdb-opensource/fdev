#!/usr/bin/python
# -*- coding: utf-8 -*-

import urllib2
import json
import yaml
import os
import ssl
import base64
from utils import Utils

__version__ = "20190827_001"

class Deploy:

    def __init__(self):
        self.utils = Utils()

        # 获得部署的目录
        self.DEPLOY_DIR = self.utils.get_file_dir()
    # 替换模板变量
    def replace_yaml_variables(self, yaml_file):
        self.utils.info("yaml_file:", yaml_file)
        yaml_str = ""
        with open(yaml_file) as f:
            for line in f.readlines():
                # 判断当前行 是否存在{{  }}这样的字符串
                start_num = line.find("{{")
                end_num = line.rfind("}}")
                if start_num > 0 and end_num > 0:
                    # 这一列存在{{}}
                    old_str = line[start_num:end_num]
                    key = old_str.split(" ")[1]
                    new_str = line.replace("{{ " + key + " }}", str(self.utils.get_env(key)))
                    yaml_str = yaml_str + "\n" + new_str
                else:
                    yaml_str = yaml_str + "\n" + line

        return yaml_str


    # 往 caas 部署
    def deploy_caas(self,envs, deployment_json, selector_json):
        FDEV_CAAS_ACCESS = self.utils.get_env('FDEV_CAAS_ACCESS')
        FDEV_CAAS_SECRET = self.utils.get_env('FDEV_CAAS_SECRET')
        FDEV_CAAS_IP = self.utils.get_env('FDEV_CAAS_IP')
        FDEV_CAAS_USER = self.utils.get_env('FDEV_CAAS_USER')
        FDEV_CAAS_PWD = self.utils.get_env('FDEV_CAAS_PWD')
        FDEV_CAAS_TENANT = self.utils.get_env('FDEV_CAAS_TENANT')

        CI_PROJECT_NAME = envs['CI_PROJECT_NAME']

        base64string = base64.b64encode('%s:%s' % (FDEV_CAAS_ACCESS, FDEV_CAAS_SECRET))
        headers = {"Content-Type": "application/json",
                   "Authorization": "Basic " + base64string}

        url = "https://" + FDEV_CAAS_IP + "/apis/apps/v1/namespaces/" + FDEV_CAAS_TENANT + "/deployments/" + CI_PROJECT_NAME
        print 'query url:', url
        request = urllib2.Request(url=url, headers=headers)
        try:
            response = urllib2.urlopen(request, context=ssl._create_unverified_context())
            if response.getcode() == 200:
                # 该应用已存在，执行更新操作
                request = urllib2.Request(
                    url=url,
                    headers=headers,
                    data=deployment_json,
                )
                request.get_method = lambda: 'PUT'
                response = urllib2.urlopen(request, context=ssl._create_unverified_context())
                print response.read()
        except urllib2.HTTPError, e:
            print 'code: ', e.code
            # 该应用不存在，执行创建操作
            url = "https://" + FDEV_CAAS_IP + "/apis/apps/v1/namespaces/" + FDEV_CAAS_TENANT + "/deployments"
            print 'create url:', url
            print 'headers:', headers
            print 'data:',deployment_json
            request = urllib2.Request(
                url=url,
                headers=headers,
                data=deployment_json,
            )
            request.get_method = lambda: 'POST'
            response = urllib2.urlopen(request, context=ssl._create_unverified_context())
            print response.read()

            base64string = base64.b64encode('%s:%s' % (FDEV_CAAS_USER, FDEV_CAAS_PWD))
            headers = {"Content-Type": "application/json",
                       "x-dce-tenant": FDEV_CAAS_TENANT,
                       "Authorization": "Basic " + base64string}

            url = "https://" + FDEV_CAAS_IP + "/dce/apps"
            print 'create app url:', url
            print 'headers:', headers
            print 'data:', selector_json
            request = urllib2.Request(
                url=url,
                headers=headers,
                data=selector_json,
            )
            request.get_method = lambda: 'POST'
            response = urllib2.urlopen(request, context=ssl._create_unverified_context())
            print response.read()

    def main(self):
        fdev_url = self.utils.get_fdev_url()
        print "fdev_url:" + fdev_url
        DEVOPS_DEPLOY_SLUG = self.utils.get_devploy_slug()
        self.utils.info("DEVOPS_DEPLOY_SLUG:" + DEVOPS_DEPLOY_SLUG)# 系统部署环境
        envs = os.environ  # 系统环境变量

        FDEV_CAAS_REGISTRY = self.utils.get_env("FDEV_CAAS_REGISTRY")
        FDEV_CAAS_REGISTRY_NAMESPACE = self.utils.get_env("FDEV_CAAS_REGISTRY_NAMESPACE")

        CI_PROJECT_ID = envs['CI_PROJECT_ID']
        CI_PROJECT_NAME = envs['CI_PROJECT_NAME']  # 应用名
        CI_PROJECT_DIR = envs['CI_PROJECT_DIR']


        # 镜像坐标: ip/namepscae/project_name_en
        name_en = self.utils.get_name_en(CI_PROJECT_ID, fdev_url)
        CI_REGISTRY_IMAGE = FDEV_CAAS_REGISTRY + "/" + FDEV_CAAS_REGISTRY_NAMESPACE + "/" + name_en
        os.environ['CI_REGISTRY_IMAGE'] = CI_REGISTRY_IMAGE
        self.utils.info("CI_REGISTRY_IMAGE:" + CI_REGISTRY_IMAGE)
        # 替换镜像tag
        if envs.has_key("CI_COMMIT_TAG"):
            image_tag = envs["CI_COMMIT_TAG"]
            envs['CI_PIPELINE_ID'] = image_tag
            os.environ['CI_PIPELINE_ID'] = image_tag
            self.utils.info("image_tag: ", image_tag)
        else:
            image_tag = self.utils.get_registry_tag()
            envs['CI_PIPELINE_ID'] = image_tag
            os.environ['CI_PIPELINE_ID'] = image_tag
            self.utils.info("image_tag: ", image_tag)
        # 替换系统环境变量
        self.utils.cmd("envsubst < " + self.DEPLOY_DIR + "/deployment.yaml > " + self.DEPLOY_DIR + "/temp-deployment.yaml")
        self.utils.cmd("envsubst < " + self.DEPLOY_DIR + "/selector.yaml > " + self.DEPLOY_DIR + "/temp-selector.yaml")

        # 替换模板变量
        deployment_str = self.replace_yaml_variables(yaml_file=self.DEPLOY_DIR + "/temp-deployment.yaml")
        deployment_json = json.dumps(yaml.load(deployment_str), indent=4)

        selector_str = self.replace_yaml_variables(yaml_file=self.DEPLOY_DIR +"/temp-selector.yaml")
        selector_json = json.dumps(yaml.load(selector_str), indent=4)

        # caas 部署
        self.deploy_caas(envs=envs, deployment_json=deployment_json, selector_json=selector_json)

if __name__ == '__main__':
    print "[+] 镜像版本: ", __version__
    deploy = Deploy()
    deploy.main()
