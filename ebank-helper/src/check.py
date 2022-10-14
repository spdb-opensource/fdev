#!/usr/bin/python
# -*- coding: utf-8 -*-

import json
import os
import sys

import chardet

from utils import Utils


class Check:

    def __init__(self):
        # 系统环境变量
        self.envs = os.environ
        self.utils = Utils()
        # 获得fdev url
        self.fdev_url = self.utils.get_fdev_url()
        self.CI_PROJECT_NAME = self.envs['CI_PROJECT_NAME']
        self.CI_COMMIT_REF_NAME = self.envs['CI_COMMIT_REF_NAME']
        self.CI_PROJECT_ID = self.envs['CI_PROJECT_ID']
        # 应用英文名去掉-parent
        self.name_en = self.utils.rename_project_name(self.CI_PROJECT_NAME)
        self.success = "AAAAAAA"
        self.ENCODE_UTF8 = "utf-8"

    # 检查该应用是否涉及环境部署
    def checkAppLables(self):
        if "definedDeployId" in self.envs:
            return True
        url = self.fdev_url + "/fapp/api/app/getAppByGitId"
        data = json.dumps({"id": self.CI_PROJECT_ID})
        response = self.utils.post_request(url, data)
        if self.success == response['code']:
            flag = True
            app_info = response['data']
            if not app_info:
                self.utils.error("未获取到应用信息！")
                sys.exit(-1)
            if app_info['label']:
                for label in app_info['label']:
                    if label == "不涉及环境部署":
                        self.utils.warn("不涉及环境部署")
                        flag = False
                        break
            return flag
        else:
            self.utils.error("发送应用模块后台交易出错：", response['msg'])
            sys.exit(-1)

    # 检查该应用是否绑定过部署信息
    def checkAppDeploy(self):
        if "definedDeployId" in self.envs:
            return
        if "1" == self.utils.deploy_platform['scc_status'] or "1" == self.utils.deploy_platform['caas_status']:
            url = self.fdev_url + "/fenvconfig/api/v2/appDeploy/queryByGitlabId"
            data = json.dumps({"gitlabId": self.CI_PROJECT_ID})
            response = self.utils.post_request(url, data)

            if "1" == self.utils.deploy_platform['caas_status']:
                if self.success == response['code'] and not response['data']["variables"]:
                    self.utils.error("当前应用尚未绑定过caas部署信息，请联系应用负责人移步到环境配置管理下的部署信息进行绑定！")
                    sys.exit(-1)

            # if "1" == self.utils.deploy_platform['scc_status']:
            #     entityFlag = False
            #     for scc_slug, scc_vars in self.utils.CI_SCC_VARS.items():
            #         entityFlag = True
            #     if not entityFlag:
            #         self.utils.error("当前应用在尚未绑定scc在sit环境的部署信息，请联系应用负责人移步到环境配置管理下的部署信息进行绑定！" )
            #         sys.exit(-1)
            # elif self.success != response['code']:
            #     self.utils.error("发送环境配置模块后台交易出错：", response['msg'])
            #     sys.exit(-1)
        else:
            self.utils.error("当前应用尚未打开环境部署开关，请联系应用负责人移步到环境配置管理下的部署信息打开开关！")
            sys.exit(-1)

    # 检查该应用是否调用了未经审批通过的接口
    def checkInterface(self):
        url = self.fdev_url + "/finterface/api/interfaceApplication/queryIsNoApplyInterface"
        data = json.dumps({"serviceCalling": self.name_en, "branch": self.CI_COMMIT_REF_NAME})
        response = self.utils.post_request(url, data)
        if self.success == response['code']:
            response_data = response['data']
            flag = response_data['flag']
            if flag:
                self.utils.error("调用了以下未经审批通过的接口，请联系相关应用负责人申请及审批：", response_data['transId'])
                sys.exit(-1)
        else:
            self.utils.error("发送接口模块后台交易出错：", response['msg'])
            sys.exit(-1)

    # 检查合并到SIT或者release的最新feature分支所对应的任务是否涉及UI设计还原审核
    def getReviewStatus(self):
        if "SIT" != self.CI_COMMIT_REF_NAME and (not self.CI_COMMIT_REF_NAME.startswith("release")):
            return
        latest_feature_branch = self.utils.get_latest_feature_branch(self.CI_PROJECT_ID, self.CI_COMMIT_REF_NAME)
        if not latest_feature_branch:
            return
        url = self.fdev_url + "/ftask/api/task/getReviewStatus"
        data = json.dumps({"gitlabProjectId": self.CI_PROJECT_ID, "featureBranch": latest_feature_branch})
        response = self.utils.post_request(url, data)
        if self.success == response['code']:
            response_data = response['data']
            # SIT 和 release都需校验这两种状态
            if "wait_upload" == response_data or "uploaded" == response_data:
                self.utils.error("%s分支所对应的任务涉及UI设计稿还原审核，合并分支前，请先上传UI设计稿并点击下一步按钮!" % latest_feature_branch)
                sys.exit(-1)
            if self.CI_COMMIT_REF_NAME.startswith("release"):
                if "fixing" == response_data or "wait_allot" == response_data:
                    self.utils.error("%s分支所对应的任务涉及UI设计稿还原审核，请督办UI负责团队执行完成设计还原审核流程！" % latest_feature_branch)
                    sys.exit(-1)
                if "nopass" == response_data:
                    self.utils.error("%s分支所对应的任务UI设计稿还原审核未通过，请根据UI审核人员反馈意见，重新完成设计还原审核流程！" % latest_feature_branch)
                    sys.exit(-1)
        else:
            self.utils.error("发送任务模块后台交易出错：", response['msg'])
            sys.exit(-1)

    # 检查该应用是否绑定了生产环境
    def check_pro_env(self):
        if "definedDeployId" in self.envs:
            return
        if "1" == self.utils.deploy_platform['caas_status']:
            url = self.fdev_url + "/fenvconfig/api/v2/appEnv/queryProEnvByGitLabId"
            data = json.dumps({"gitlabId": int(self.CI_PROJECT_ID)})
            response = self.utils.post_request(url, data)
            if self.success == response['code']:
                response_data = response['data']
                if len(response_data) == 0:
                    self.utils.error("请联系应用负责人或行内应用负责人移步到环境配置管理下的部署信息确认应用的部署网段及生产环境信息")
                    sys.exit(-1)
            else:
                self.utils.error("发送环境配置模块后台交易出错：", response['msg'])
                sys.exit(-1)
        # if "1" == self.utils.deploy_platform['scc_status']:
        #     url = self.fdev_url + "/fenvconfig/api/v2/env/querySccEnvByAppId"
        #     data = json.dumps({"app_id": self.utils.app_id ,"deploy_env": "pro"})
        #     response = self.utils.post_request(url, data)
        #     if self.success == response['code']:
        #         response_data = response['data']
        #         if len(response_data) == 0:
        #             self.utils.error("请联系应用负责人或行内应用负责人移步到环境配置管理下的部署信息确认应用的部署网段及生产环境信息")
        #             sys.exit(-1)
        #     else:
        #         self.utils.error("发送环境配置模块后台交易出错：", response['msg'])
        #         sys.exit(-1)

    # 校验配置文件中必填实体属性是否有值
    def checkConfigFile(self, tag):
        DEPLOY_DIR = self.utils.get_file_dir()
        # 应用英文名,eureka 上服务名
        self.fdev_runtime_file = DEPLOY_DIR + "/fdev-application.properties"

        if os.path.exists(self.fdev_runtime_file) == False:
            self.utils.warn('获取配置模板文件失败，请检查应用配置模板！')
            sys.exit(0)
        # 获取应用的配置模板文件内容
        with open(self.fdev_runtime_file, "r") as fr:
            originFileContent = fr.read()
        url = self.fdev_url + "/fenvconfig/api/v2/configfile/checkConfigFile"
        try:
            data = json.dumps({"gitlab_project_id": self.CI_PROJECT_ID, "env_name": self.utils.CI_ENVIRONMENT_SLUG,
                               "content": originFileContent, "tag": tag})
        except Exception as e:
            encoding = chardet.detect(originFileContent)['encoding']
            self.utils.info("encoding:" + encoding)
            if encoding != self.ENCODE_UTF8:
                self.utils.error('=========================================================================')
                self.utils.error('  外部配置文件fdev-application.properties）编码必须为utf-8！  ')
                self.utils.error('=========================================================================')
            else:
                self.utils.error(e.message)
            sys.exit(-1)
        else:
            response = self.utils.post_request(url, data)
            if self.success == response['code']:
                response_data = response['data']
                # 当flag为False时，表示存在有必填字段为空
                if response_data['flag'] == False:
                    for format_error in response_data['formatError']:
                        self.utils.error(format_error)
                    for model_error in response_data['modelErrorList']:
                        self.utils.error(model_error)
                    for field_error in response_data['FiledErrorList']:
                        self.utils.error(field_error)
                    if len(response_data['model_env_amp']) != 0:
                        self.utils.error("应用引用的部分实体在以下环境未配置映射值，具体清单为:")
                        # 循环遍历获取参数
                        for model_env in response_data['model_env_amp']:
                            self.utils.error("实体%s ,属性字段%s ,环境%s" % (
                                model_env['model_name_en'], model_env['model_field_name_en'], model_env['env_name_en']))
                        self.utils.error("私有实体请应用负责人自行配置，公共实体请联系环境配置管理员进行配置")
                    sys.exit(-1)
            else:
                self.utils.error("发送环境配置模块后台交易出错: ", response['msg'])
                sys.exit(-1)

    # 检查该应用是否有额外挡板配置项
    def check_iam(self):
        url = self.fdev_url + "/ftask/api/manage/check/iam/properties"
        data = json.dumps({"gitlab_project_id": self.CI_PROJECT_ID, "branch_name": self.CI_COMMIT_REF_NAME})
        response = self.utils.post_request(url, data)
        if self.success == response['code']:
            response_data = response['data']
            if response_data == False:
                self.utils.error("检测到当前应用在使用挡板，请在挡板客户端点击停用挡板，以恢复正常状态，然后再重新run pipeline！")
                exit(-1)
        else:
            self.utils.error("发送任务模块后台交易出错：", response['msg'])
            sys.exit(-1)

    # 检查scc对应环境是否绑定部署信息
    def check_env(self,env):
        if "definedDeployId" in self.envs:
            return
        if "1" == self.utils.deploy_platform['scc_status']:
            entityFlag = False
            for scc_slug, scc_vars in self.utils.CI_SCC_VARS.items():
                if env in scc_slug.lower():
                    entityFlag = True
            if not entityFlag:
                self.utils.error("当前应用在尚未绑定scc在%s环境的部署信息，请联系应用负责人移步到环境配置管理下的部署信息进行绑定！"%env)
                sys.exit(-1)

    def main(self):
        print "开始 check..."
        flag = False
        if "SIT" == self.CI_COMMIT_REF_NAME:
            if self.checkAppLables():
                self.checkAppDeploy()
            self.checkInterface()
            # 检查该应用是否有额外挡板配置项
            self.check_iam()
        # elif self.utils.get_ref_env() == "uat":
        #     self.check_env('uat')
        # elif self.utils.get_ref_env() == "rel":
        #     self.check_env('rel')
        elif 'CI_COMMIT_TAG' in self.envs and self.checkAppLables():
            self.check_pro_env()
            flag = True
        self.getReviewStatus()
        if not (self.utils.CI_SCHEDULE and self.utils.envs.get('CI_SCHEDULE') == "sit-ate"):
            if "1" == self.utils.deploy_platform['caas_status']:
                self.utils.init_caas_var(self.utils.ci_environment_slug)
                if self.utils.check_env():
                    self.checkConfigFile(flag)
        # if "1" == self.utils.deploy_platform['scc_status']:
        #     for scc_slug, scc_vars in self.utils.CI_SCC_VARS.items():
        #         self.utils.init_scc_var(scc_slug)
        #         if self.utils.check_env():
        #             self.checkConfigFile(flag)


if __name__ == '__main__':
    check = Check()
    check.main()
