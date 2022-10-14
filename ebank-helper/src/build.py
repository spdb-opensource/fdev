#!/usr/bin/python
# -*- coding: utf-8 -*-

import collections
import json
import os
import sys
import time

from utils import Utils


class Build:

    def __init__(self):
        self.envs = os.environ  # 系统环境变量
        self.utils = Utils()
        self.fdev_url = self.utils.get_fdev_url()

        self.CI_PROJECT_DIR = self.envs['CI_PROJECT_DIR']
        self.CI_PIPELINE_ID = self.envs['CI_PIPELINE_ID']
        self.CI_PROJECT_ID = self.envs['CI_PROJECT_ID']
        self.CI_PROJECT_NAME = self.envs['CI_PROJECT_NAME']
        self.CI_COMMIT_REF_NAME = self.envs['CI_COMMIT_REF_NAME']

        # 获得部署的目录
        self.DEPLOY_DIR = self.utils.get_file_dir()
        # 镜像坐标: ip/namepscae/project_name_en
        self.name_en = self.utils.rename_project_name(self.CI_PROJECT_NAME)
        self.pro_scc_image_uri = None
        self.SEND_IMAGE_FLAG = False
        self.first_scc_env = None

        self.success = "AAAAAAA"
        if 0 != int(self.CI_PIPELINE_ID):
            # 加载 docker 环境,不然无法使用 docker 命令
            os.putenv("DOCKER_HOST", "tcp://localhost:2375")


    def man(self):
        fileList = self.utils.filter_jar_or_war(self.CI_PROJECT_DIR)
        if len(fileList) != 0:
            # copy 打包产出物到 dockerfile 目录下
            # self.utils.cmd("cp -r --backup=numbered `find " + self.CI_PROJECT_DIR + " -name '*.*ar'` " + self.DEPLOY_DIR)
            for fileName in fileList:
                print fileName
                # 调用方法，将打出来的jar或war包上传到minio
                if os.environ.has_key('CI_COMMIT_TAG') == True:
                    self.utils.upload_jar_or_war_to_minio(fileName)
                self.utils.cmd("cp -r --backup=numbered %s %s" % (fileName, self.DEPLOY_DIR))
            name_en = self.utils.rename_project_name(self.CI_PROJECT_NAME)
            self.utils.remove_version(self.DEPLOY_DIR, name_en)
        else:
            self.utils.warn("找到不到对应的介质包, 检测mvn-build 是否生成对应的介质包")

        self.is_pro_image = False
        # 如果是打得 tag 包,也就是用来投产的包
        if os.environ.has_key('CI_COMMIT_TAG'):
            self.CI_COMMIT_TAG = os.environ['CI_COMMIT_TAG']
            if "PRO" == self.CI_COMMIT_TAG.upper()[0:3]:
                self.is_pro_image = True

        # 输出环境变量信息 
        self.utils.cmd("echo self.CI_COMMIT_REF_NAME = %s " % self.CI_COMMIT_REF_NAME)
        self.utils.cmd("echo self.envs = %s " % self.envs)
        rasp_env = self.CI_COMMIT_REF_NAME
        rasp_release_branch = rasp_env[0:7]
        if rasp_env == 'SIT' or rasp_env == 'UAT' or rasp_env == 'release' or rasp_release_branch == 'release':
            # 判断是否为Java微服务
            app_info = self.utils.app_info()
            # 如果是SIT触发的持续集成，并且不是自定义部署的，则需要agent加固
            if "definedDeployId" not in self.envs \
                    and len(app_info) != 0 and app_info['type_name'] == "Java微服务".decode("utf-8") \
                    and os.path.exists(self.DEPLOY_DIR + "/setEnv.sh"):
                    # 从oss中获取rasp包、yml,替换rasp目录中yml
                    xt_flag = self.CI_PROJECT_NAME[0:self.CI_PROJECT_NAME.index("-")]
                    self.utils.cmd("echo xt_flag = %s " %xt_flag)
                    if xt_flag == "mspmk" or  xt_flag == "msper" or xt_flag == "msent" or xt_flag == "mstim" \
                    	or xt_flag == "overseas" or xt_flag == "mgmt" or xt_flag == "mgmtper" or xt_flag == "mgmtpay" \
                    	or xt_flag == "msemk" or xt_flag == "mspay" or xt_flag == "msfts" \
                    	or xt_flag == "shcs" or xt_flag == "octopus":
                    		with open("/opt/fdev-helper/rasp.sh", "r") as fr:
                    			rasp_sh = fr.read()
                    		# 修改setEnv.sh脚本
                    		self.utils.cmd("echo '\n%s' >> %s/setEnv.sh" % (rasp_sh, self.DEPLOY_DIR))
                    		self.utils.cmd("cat %s/setEnv.sh" % self.DEPLOY_DIR)
                    		rasp_sys = "wyxt-" + xt_flag
                    		self.utils.cmd("echo rasp_sys = %s " %rasp_sys)
                    		self.utils.cmd("cat /opt/fdev-helper/download_rasp.sh")
                    		self.utils.cmd("sh /opt/fdev-helper/download_rasp.sh %s %s " % (rasp_sys, self.DEPLOY_DIR))
                    else:
                    	with open("/opt/fdev-helper/rasp_back.sh", "r") as fr:
                    		rasp_sh = fr.read()
                    	self.utils.cmd("echo '\n%s' >> %s/setEnv.sh" % (rasp_sh, self.DEPLOY_DIR))
                    	self.utils.cmd("cat %s/setEnv.sh" % self.DEPLOY_DIR)
                    			

            # 如果是SIT触发的持续集成，服务类型是 JAVA微服务和容器化项目，则请求需要代理到挡板
            if len(app_info) != 0 \
                    and (app_info['type_name'] == "Java微服务".decode("utf-8")
                         or app_info['type_name'] == "容器化项目".decode("utf-8")) \
		    and 'CI_SCHEDULE' in os.environ \
                    and os.path.exists(self.DEPLOY_DIR + "/setEnv.sh"):
                    with open("/opt/fdev-helper/iams.sh", "r") as fr:
                        iams_sh = fr.read()
                    # 修改setEnv.sh脚本
                    self.utils.cmd("echo '\n%s' >> %s/setEnv.sh" % (iams_sh, self.DEPLOY_DIR))
                    self.utils.cmd("cat %s/setEnv.sh" % self.DEPLOY_DIR)


        if not (self.utils.CI_SCHEDULE and self.envs.get('CI_SCHEDULE') == "sit-ate"):
            if "1" == self.utils.deploy_platform['caas_status']:
                self.utils.info("镜像推送caas环境%s" % self.utils.ci_environment_slug)
                self.push_caas()
                # 生成路由介质
                self.build_dat(self.utils.ci_environment_slug)
                if self.is_pro_image:
                    data = json.dumps({"gitlab_project_id": int(self.CI_PROJECT_ID), "product_tag": self.CI_COMMIT_TAG,
                                       "env": self.utils.ci_environment_slug,
                                       "pro_image_uri": str(self.CI_REGISTRY_IMAGE + ":" + self.CI_COMMIT_TAG)})
                    url = self.fdev_url + "/frelease/api/releasenode/application/saveProductImage"
                    self.utils.post_request(url, data)
                    self.push_war_md5()
                    # self.SEND_IMAGE_FLAG = True
        if "1" == build.utils.deploy_platform['scc_status']:
            for scc_slug, scc_vars in build.utils.CI_SCC_VARS.items():
                self.utils.info("镜像推送scc环境%s" % scc_slug)
                self.push_scc(scc_slug)

            if self.SEND_IMAGE_FLAG:
                # 生成路由介质
                self.build_dat(self.first_scc_env)
                if self.is_pro_image:
                    data = json.dumps({"gitlab_project_id": int(self.CI_PROJECT_ID), "product_tag": self.CI_COMMIT_TAG,
                                       "env": self.first_scc_env,
                                       "pro_scc_image_uri": str(self.pro_scc_image_uri + ":" + self.CI_COMMIT_TAG)})
                    url = self.fdev_url + "/frelease/api/releasenode/application/saveProductImage"
                    self.utils.post_request(url, data)
                    self.push_war_md5()
        else:
            self.build_dat(None)



    # 获取指定应用里target文件夹下的war包，将该应用的静态资源进行MD5并上传至GitLab
    def push_war_md5(self):
        war_name = None
        if self.CI_PROJECT_NAME.startswith(
                "mspmk-cli-") or self.CI_PROJECT_NAME == "spdb-cli-mobcli" or self.CI_PROJECT_NAME == "mspmk-web-pits":
            war_name = self.utils.get_war(self.CI_PROJECT_DIR)
            self.utils.info(war_name)
        if war_name:
            self.utils.cmd("cd %s && mkdir -p %s" % (self.DEPLOY_DIR, self.CI_PROJECT_NAME))
            md5_dir = self.DEPLOY_DIR + "/" + self.CI_PROJECT_NAME
            self.utils.cmd("unzip -oq %s -d %s" % (war_name, md5_dir))
            self.utils.cmd("genemd5.sh %s/WEB-INF/classes/static/ %s" % (md5_dir, self.CI_PROJECT_NAME))
            if not os.path.exists(self.CI_PROJECT_NAME + ".info"):
                self.utils.warn('获取MD5文件失败！')
                # sys.exit(0)
                return
            # 获取MD5文件内容
            with open(self.CI_PROJECT_NAME + ".info", "r") as fr:
                md5_content = fr.read()
            data = json.dumps({"gitlabId": int(self.CI_PROJECT_ID), "tag_name": self.CI_COMMIT_TAG,
                               "content": str(md5_content), "pipeline_id": self.CI_PIPELINE_ID})
            url = self.fdev_url + "/fenvconfig/api/v2/configfile/saveWarMd5ToGitlab"
            response_data = self.utils.post_request(url, data)
            if "AAAAAAA" != response_data["code"]:
                self.utils.warn('调环境配置模块接口上传war包的md5出错：', response_data['msg'])
                sys.exit(-1)

    # 生成路由介质
    def build_dat(self, env):
        if self.CI_PROJECT_NAME.startswith("mspmk-cli-"):
            app_info = self.utils.app_info()
        else:
            return
        if len(app_info) != 0 and app_info['type_name'] == "Vue应用".decode("utf-8"):
            NEW_VUE_FRAMEWORK = self.CI_PROJECT_DIR + "/project.json"
            if os.path.exists(NEW_VUE_FRAMEWORK) is not True:
                self.utils.warn('应用未使用新版脚手架')
                return
            # 解析配置Json串
            with open(NEW_VUE_FRAMEWORK) as project_config:
                try:
                    config_str = project_config.read()
                    project_json = json.loads(config_str, object_pairs_hook=collections.OrderedDict)
                except Exception, err:
                    self.utils.error('解析应用路由配置文件project.json失败:' + err.message)
                    sys.exit(-1)
            # 生成路由介质
            tag_flag = 'CI_COMMIT_TAG' in self.envs
            if (env is not None and "scc" in env) or env is None:
                # 发送SCC环境的生成路由介质接口
                data = json.dumps(
                    {"gitlab_project_id": int(self.CI_PROJECT_ID), "branch": self.CI_COMMIT_REF_NAME,
                     "tag_flag": tag_flag, "project_json": project_json,
                     "env": self.utils.CI_SCC_SLUG if env is not None else ""})
                url = self.fdev_url + "/finterface/api/interface/buildSccDatTar"
            else:
                # 发送CaaS环境的生成路由介质接口
                data = json.dumps(
                    {"gitlab_project_id": int(self.CI_PROJECT_ID), "branch": self.CI_COMMIT_REF_NAME,
                     "tag_flag": tag_flag, "project_json": project_json})
                url = self.fdev_url + "/finterface/api/interface/buildTestDatTar"
            response = self.utils.post_request(url, data)
            if self.success == response['code']:
                self.utils.info(response['data'])
            else:
                self.utils.error("发送接口模块后台交易出错：", response['msg'])
                sys.exit(-1)

    def get_scc_image_tag(self, slug):
        image_tag = self.utils.get_scc_registry_tag(slug)
        # 如果是打得 tag 包,也就是用来投产的包
        if os.environ.has_key('CI_COMMIT_TAG') == True:
            self.CI_COMMIT_TAG = os.environ['CI_COMMIT_TAG']
            if "PRO" == self.CI_COMMIT_TAG.upper()[0:3]:
                self.is_pro_image = True
                # tag 镜像
                image_tag = self.CI_COMMIT_TAG
        return image_tag

    def push_scc(self,slug):
        # 替换 Dockerfile 写入系统环境变量(基础镜像实体从get_scc_env的接口获取)
        # os.environ['FDEV_CAAS_BASE_NAMESPACE'] = self.utils.get_scc_env("FDEV_CAAS_BASE_NAMESPACE", slug)
        # os.environ['FDEV_CAAS_REGISTRY'] = self.utils.get_scc_env("FDEV_CAAS_REGISTRY", slug)
        os.environ['FDEV_CAAS_BASE_NAMESPACE'] = self.utils.get_scc_env("fdev_caas_base_namespace", slug)
        os.environ['FDEV_CAAS_REGISTRY'] = self.utils.get_scc_env("fdev_caas_registry", slug)
        self.utils.info("SCC镜像构建基础实体IP：%s, namespaces：%s " % (os.environ['FDEV_CAAS_REGISTRY'], os.environ['FDEV_CAAS_BASE_NAMESPACE']) )

        fdev_scc_registry_user = build.utils.get_scc_env("dockerservice_user", slug)
        fdev_scc_registry_password = build.utils.get_scc_env("dockerservice_passwd", slug)
        fdev_scc_registry_namespace = build.utils.get_scc_env("dockerservice_namespace", slug)
        fdev_scc_service_registry = build.utils.get_scc_env("dockerservice_ip", slug)
        self.utils.info("============scc当前环境 %s 镜像仓库密码:%s" % (slug, fdev_scc_registry_password))
        scc_ci_registry_image = fdev_scc_service_registry + "/" + fdev_scc_registry_namespace + "/" + self.name_en
        scc_image_tag = self.get_scc_image_tag(slug)
        # 保存SCC镜像第一个tag名称
        if not self.SEND_IMAGE_FLAG:
            self.SEND_IMAGE_FLAG = True
            self.pro_scc_image_uri = scc_ci_registry_image
            self.first_scc_env = slug

        # 替换 Dockerfile 中的内容
        if os.path.exists(self.DEPLOY_DIR + "/Dockerfile.bak"):
            self.utils.cmd("cd " + self.DEPLOY_DIR + " && rm Dockerfile && cp Dockerfile.bak  Dockerfile")
        else:
            self.utils.cmd("cd " + self.DEPLOY_DIR + " && cp Dockerfile Dockerfile.bak")
        envsubst = """envsubst "`printf '${%s} ' $(sh -c "env|cut -d'=' -f1")`" < Dockerfile > Dockerfile.tmp && mv Dockerfile.tmp Dockerfile"""
        self.utils.cmd("cd " + self.DEPLOY_DIR + "&&" + envsubst)
        self.utils.cmd("cd " + self.DEPLOY_DIR + " && pwd && ls && cat Dockerfile")

        rasp_env = self.CI_COMMIT_REF_NAME
        rasp_release_branch = rasp_env[0:7]
        if rasp_env == 'SIT' or rasp_env == 'UAT' or rasp_env == 'release' or rasp_release_branch == 'release':
        	app_info = self.utils.app_info()
        	if "definedDeployId" not in self.envs \
        		and len(app_info) != 0 and app_info['type_name'] == "Java微服务".decode("utf-8"):
        			xt_flag = self.CI_PROJECT_NAME[0:self.CI_PROJECT_NAME.index("-")]
        			if xt_flag == "mspmk" or  xt_flag == "msper" or xt_flag == "msent" or xt_flag == "mstim" \
        				or xt_flag == "overseas" or xt_flag == "mgmt" or xt_flag == "mgmtper" or xt_flag == "mgmtpay" \
        				or xt_flag == "msemk" or xt_flag == "mspay" or xt_flag == "msfts" \
        				or xt_flag == "shcs" or xt_flag == "octopus":
        					self.utils.cmd("echo self.DEPLOY_DIR = %s " % self.DEPLOY_DIR)
        					wf = open(self.DEPLOY_DIR + "/tmp_Dockerfile", "w")
        					with open(self.DEPLOY_DIR + "/Dockerfile", "r") as rf:
        						line = rf.readlines()
        						for line_list in line:
        							line_context = line_list.replace('\n', '')
        							wf.write(line_context + '\n')
        							if line_context == "COPY setEnv.sh /ebank" or line_context.find("COPY setEnv.sh /ebank") != -1:
        								wf.write("COPY /rasp /ebank/rasp\n")
        								self.utils.cmd("echo COPY /rasp /ebank/rasp")
        					wf.close()
        					self.utils.cmd("cat %s/tmp_Dockerfile" % self.DEPLOY_DIR)
        					self.utils.cmd("mv %s/tmp_Dockerfile %s/Dockerfile " % (self.DEPLOY_DIR, self.DEPLOY_DIR))

        # self.utils.info("docker login " + fdev_scc_service_registry + " -u " + fdev_scc_registry_user)
        self.utils.info("docker login " + fdev_scc_service_registry + " -u " + fdev_scc_registry_user + " -p " + fdev_scc_registry_password)
        self.utils.cmdN("docker login -u %s -p %s %s" % (
            fdev_scc_registry_user, fdev_scc_registry_password, fdev_scc_service_registry))
        # 重新构建镜像
        self.utils.cmd("cd %s && docker build . -t %s:%s" % (self.DEPLOY_DIR, scc_ci_registry_image, scc_image_tag))
        self.utils.cmd("docker push %s:%s" % (scc_ci_registry_image, scc_image_tag))

        if self.CI_COMMIT_REF_NAME == "SIT" and self.utils.is_retail:
            # sit分支零售组特殊应用非定时部署的镜像信息保存
            data = json.dumps({"appName": self.utils.rename_project_name(self.utils.CI_PROJECT_NAME),
                               "deployEnv": "sit1",
                               "imagePushStatus": "1",
                               "imageCaasUrl": "",
                               "imageSccUrl": scc_ci_registry_image + ":" + scc_image_tag})
            url = self.utils.get_fdev_url() + "/ftask/api/deploy/saveAppDeployInfo"
            response_data = self.utils.post_request(url, data)
            if response_data['code'] != self.success:
                print "保存SCC镜像失败"
                sys.exit(-1)

    def push_caas(self):
        self.FDEV_CAAS_REGISTRY_USER = self.utils.get_env('FDEV_CAAS_REGISTRY_USER')
        self.FDEV_CAAS_REGISTRY_PASSWORD = self.utils.get_env('FDEV_CAAS_REGISTRY_PASSWORD')
        self.FDEV_CAAS_REGISTRY = self.utils.get_env("FDEV_CAAS_REGISTRY")
        self.FDEV_CAAS_REGISTRY_NAMESPACE = self.utils.get_env("FDEV_CAAS_REGISTRY_NAMESPACE")
        self.FDEV_CAAS_BASE_NAMESPACE = self.utils.get_env("FDEV_CAAS_BASE_NAMESPACE")
        self.FDEV_CAAS_BASE_IMAGE_VERSION = self.utils.get_env("FDEV_CAAS_BASE_IMAGE_VERSION")
        self.FDEV_CAAS_SERVICE_REGISTRY = self.utils.get_env("FDEV_CAAS_SERVICE_REGISTRY")
        self.utils.info("============caas当前环境镜像仓库密码:%s" % self.FDEV_CAAS_REGISTRY_PASSWORD)
        # 替换 Dockerfile 写入系统环境变量
        os.environ['FDEV_CAAS_BASE_NAMESPACE'] = self.FDEV_CAAS_BASE_NAMESPACE
        os.environ['FDEV_CAAS_REGISTRY'] = self.FDEV_CAAS_REGISTRY
        os.environ['FDEV_CAAS_BASE_IMAGE_VERSION'] = str(self.FDEV_CAAS_BASE_IMAGE_VERSION)

        self.CI_REGISTRY_IMAGE = self.FDEV_CAAS_SERVICE_REGISTRY \
                                 + "/" + self.FDEV_CAAS_REGISTRY_NAMESPACE \
                                 + "/" + self.name_en
        image_tag = self.utils.get_registry_tag()
        # 如果是打得 tag 包,也就是用来投产的包
        if os.environ.has_key('CI_COMMIT_TAG'):
            self.CI_COMMIT_TAG = os.environ['CI_COMMIT_TAG']
            if "PRO" == self.CI_COMMIT_TAG.upper()[0:3]:
                # tag 镜像
                image_tag = self.CI_COMMIT_TAG

        # 替换 Dockerfile 中的内容
        self.utils.cmd("cd " + self.DEPLOY_DIR + "&& cp Dockerfile Dockerfile.bak")
        envsubst = """envsubst "`printf '${%s} ' $(sh -c "env|cut -d'=' -f1")`" < Dockerfile > Dockerfile.tmp && mv Dockerfile.tmp Dockerfile"""
        self.utils.cmd("cd " + self.DEPLOY_DIR + "&&" + envsubst)
        self.utils.cmd("cd " + self.DEPLOY_DIR + " && pwd && ls && cat Dockerfile")

        rasp_env = self.CI_COMMIT_REF_NAME
        rasp_release_branch = rasp_env[0:7]
        if rasp_env == 'SIT' or rasp_env == 'UAT' or rasp_env == 'release' or rasp_release_branch == 'release':
        	app_info = self.utils.app_info()
        	if "definedDeployId" not in self.envs and len(app_info) != 0 and app_info['type_name'] == "Java微服务".decode("utf-8"):
        		xt_flag = self.CI_PROJECT_NAME[0:self.CI_PROJECT_NAME.index("-")]
        		if xt_flag == "mspmk" or  xt_flag == "msper" or xt_flag == "msent" or xt_flag == "mstim" \
        			or xt_flag == "overseas" or xt_flag == "mgmt" or xt_flag == "mgmtper" or xt_flag == "mgmtpay" \
        			or xt_flag == "msemk" or xt_flag == "mspay" or xt_flag == "msfts" \
        			or xt_flag == "shcs" or xt_flag == "octopus":
        				self.utils.cmd("echo self.DEPLOY_DIR = %s " % self.DEPLOY_DIR)
        				wf = open(self.DEPLOY_DIR + "/tmp_Dockerfile", "w")
        				with open(self.DEPLOY_DIR + "/Dockerfile", "r") as rf:
        					line = rf.readlines()
        					for line_list in line:
        						line_context = line_list.replace('\n', '')
        						wf.write(line_context + '\n')
        						if line_context == "COPY setEnv.sh /ebank" or line_context.find("COPY setEnv.sh /ebank") != -1:
        							wf.write("COPY /rasp /ebank/rasp\n")
        							self.utils.cmd("echo COPY /rasp /ebank/rasp")
        				wf.close()
        				self.utils.cmd("cat %s/tmp_Dockerfile" % self.DEPLOY_DIR)
        				self.utils.cmd("mv %s/tmp_Dockerfile %s/Dockerfile " % (self.DEPLOY_DIR, self.DEPLOY_DIR))

        # docker 登录到有基础镜像的ip去构建镜像
        # self.utils.info("docker login " + self.FDEV_CAAS_SERVICE_REGISTRY + " -u " + self.FDEV_CAAS_REGISTRY_USER)
        self.utils.info("docker login " + self.FDEV_CAAS_SERVICE_REGISTRY + " -u " + self.FDEV_CAAS_REGISTRY_USER + " -p " + self.FDEV_CAAS_REGISTRY_PASSWORD)
        self.utils.cmdN("docker login -u %s -p %s %s" % (
            self.FDEV_CAAS_REGISTRY_USER, self.FDEV_CAAS_REGISTRY_PASSWORD, self.FDEV_CAAS_SERVICE_REGISTRY))
        # docker build . -t
        self.utils.cmd("cd %s && docker build . -t %s:%s" % (self.DEPLOY_DIR, self.CI_REGISTRY_IMAGE, image_tag))
        self.utils.cmd("docker push %s:%s" % (self.CI_REGISTRY_IMAGE, image_tag))

        if self.CI_COMMIT_REF_NAME == "SIT" and self.utils.is_retail:
            # sit分支零售组特殊应用非定时部署的镜像信息保存
            data = json.dumps({"appName": self.utils.rename_project_name(self.utils.CI_PROJECT_NAME),
                               "deployEnv": "sit1",
                               "imagePushStatus": "0",
                               "imageCaasUrl": self.CI_REGISTRY_IMAGE + ":" + image_tag,
                               "imageSccUrl": ""})
            url = self.utils.get_fdev_url() + "/ftask/api/deploy/saveAppDeployInfo"
            response_data = self.utils.post_request(url, data)
            if response_data['code'] != self.success:
                print "保存CaaS镜像失败"
                sys.exit(-1)


if __name__ == '__main__':
    build = Build()
    # 制作镜像
    build.man()
