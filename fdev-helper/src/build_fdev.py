#!/usr/bin/python
# -*- coding: utf-8 -*-
import os

from utils import Utils


class Build:

    def __init__(self):
        # 加载 docker 环境,不然无法使用 docker 命令
        os.putenv("DOCKER_HOST", "tcp://localhost:2375")
        envs = os.environ  # 系统环境变量
        self.utils = Utils()
        self.envs = envs

        self.CI_PROJECT_DIR = envs['CI_PROJECT_DIR']
        self.CI_PIPELINE_ID = envs['CI_PIPELINE_ID']
        self.CI_PROJECT_ID = envs['CI_PROJECT_ID']
        self.CI_REGISTRY_USER = envs['CI_REGISTRY_USER']
        self.CI_REGISTRY = envs['CI_REGISTRY']
        self.CI_REGISTRY_PASSWORD = envs['CI_REGISTRY_PASSWORD']
        self.CI_REGISTRY_IMAGE = envs['CI_REGISTRY_IMAGE']
        self.CI_PIPELINE_ID = envs['CI_PIPELINE_ID']
        self.CI_COMMIT_REF_NAME = envs['CI_COMMIT_REF_NAME']
        # 获得部署的目录
        self.DEPLOY_DIR = self.utils.get_file_dir()

    def main(self):
        self.utils.info("开始 build..")
        result = self.utils.cmd("find " + self.CI_PROJECT_DIR + " -name '*.*ar' | wc -l")
        if int(result) != 0:
            # copy 打包产出物到 dockerfile 目录下
            self.utils.cmd("cp `find " + self.CI_PROJECT_DIR + " -name '*.*ar'` " + self.DEPLOY_DIR)

        self.utils.cmd(
            "cd " + self.CI_PROJECT_DIR + "/gitlab-ci && envsubst < Dockerfile > Dockerfile.temp && mv Dockerfile.temp Dockerfile")
        self.utils.cmd("cd " + self.DEPLOY_DIR + " && pwd && echo "" && ls && echo "" && cat Dockerfile")
        # docker login 登录
        self.utils.cmd(
            "docker login -u %s -p %s %s" % (self.CI_REGISTRY_USER, self.CI_REGISTRY_PASSWORD, self.CI_REGISTRY))

        # CI_REGISTRY_IMAGE = xxx/library/fdev-app

        # SIT 分支 -> 镜像打到 xxx/fdev-sit/fdevapp:piplineid
        # release 分支 -> 镜像打到 xxx/fdev-uat/fdevapp:piplineid
        # pro tag 分支 -> 镜像打到 xxx/fdev-rel/fdevapp:piplineid
        # pro tag 分支 并且是投产 -> 镜像打到 xxx/fdev-prod/fdevapp:piplineid

        # 代表打得 tag 包
        if os.environ.has_key('CI_COMMIT_TAG') == True:
            if os.environ.has_key('FDEV_DEPLOY_PROD') == True:
                # 代表打 prod 包,只替换 CI_REGISTRY_IMAGE 中的 library 为 fdev-prod
                self.CI_REGISTRY_IMAGE = str(self.CI_REGISTRY_IMAGE).replace("library", "fdev-prod")
            else:
                # 代表打rel包,只替换 CI_REGISTRY_IMAGE 中的 library为 fdev-rel
                self.CI_REGISTRY_IMAGE = str(self.CI_REGISTRY_IMAGE).replace("library", "fdev-rel")
        elif self.CI_COMMIT_REF_NAME[0:7].upper() == "RELEASE":
            # 代表打UAT包,只替换 CI_REGISTRY_IMAGE 中的 library为 fdev-uat
            self.CI_REGISTRY_IMAGE = str(self.CI_REGISTRY_IMAGE).replace("library", "fdev-uat")
        elif "2831" == self.CI_PROJECT_ID and self.CI_COMMIT_REF_NAME == "master":
            self.CI_REGISTRY_IMAGE = str(self.CI_REGISTRY_IMAGE).replace("library", "fdev-prod")
        else:
            # default 打 SIT 包
            self.CI_REGISTRY_IMAGE = str(self.CI_REGISTRY_IMAGE).replace("library", "fdev-sit")

        self.utils.cmd(
            "cd %s && docker build . -t %s:%s" % (self.DEPLOY_DIR, self.CI_REGISTRY_IMAGE, self.CI_PIPELINE_ID))
        self.utils.cmd("docker push %s:%s" % (self.CI_REGISTRY_IMAGE, self.CI_PIPELINE_ID))

        # 不重构的应用需要重新打包，因为启动脚本放在包里的
        try:
            context_path = self.utils.project_list.get(self.CI_PROJECT_ID)
        except Exception:
            return
        if context_path:
            # 替换启动脚本，然后重新打包
            self.utils.cmd("sed -i 's/=pro/=pro-new/g' " + self.DEPLOY_DIR + "/start.sh")
            self.utils.cmd("sed -i 's/=sit/=sit-new/g' " + self.DEPLOY_DIR + "/start.sh")
            self.utils.cmd("sed -i 's/=rel/=rel-new/g' " + self.DEPLOY_DIR + "/start.sh")
            self.utils.cmd("sed -i 's/=uat/=uat-new/g' " + self.DEPLOY_DIR + "/start.sh")
            self.utils.cmd("cat " + self.DEPLOY_DIR + "/start.sh")
            self.utils.cmd(
                "cd %s && docker build . -t %s:%s" % (
                    self.DEPLOY_DIR, self.CI_REGISTRY_IMAGE, self.CI_PIPELINE_ID + "-new"))
            self.utils.cmd("docker push %s:%s" % (self.CI_REGISTRY_IMAGE, self.CI_PIPELINE_ID + "-new"))


if __name__ == '__main__':
    build = Build()
    build.main()
