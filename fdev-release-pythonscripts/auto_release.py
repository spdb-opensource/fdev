#!/usr/bin/env python
# -*- coding: utf-8 -*-
import threading

import io

from dos_unix import dos2unix
from fdev_requests import *
import git
from save_docker_image import *
from cache_image import *
from pexpect_cmd import *
import logging
import time
import sched
import tarfile
import zipfile
import yaml
import codecs

FDEV_ENV = os.environ.get("CI_ENVIRONMENT_SLUG")
DIR = os.environ.get("CI_PROJECT_DIR")

# 通过自动化配置表获取配置
automation_param = query_automation_param_map()
# 本地准备介质临时目录
TEMP_DIR = automation_param["autorelease.tmp.dir"]
# gitlab地址
GITLAB_URL = automation_param["gitlab.url"]
# gitlab管理员Tokenp:
GITLAB_TOKEN = automation_param["gitlab.manager.token"]
# 自动化发布服务器ip
AUTORELEASE_SERVER_HOST = automation_param["autorelease.server.host"]
# 自动化发布服务器用户
AUTORELEASE_SERVER_USER = automation_param["autorelease.server.user"]
# 自动化发布服务器密码
AUTORELEASE_SERVER_PASSWORD = automation_param["autorelease.server.password"]
# 自动化发布服务器介质根目录
AUTORELEASE_SERVER_ROOT_DIR = automation_param["autorelease.server.root.dir"]
# 路由介质获取目录
AUTORELEASE_INTERFACE_ROUT_DIR = automation_param["autorelease.interface.rout.dir"]
# 需dos转unix的文件类型
DOS_UNIX_FILE_TYPE = str(automation_param["dos2unix.filetypes"]).split(",")
# 从minio下载aoms.sh地址
MINIO_DYNAMICCONFIG_URL = automation_param["minio.dynamicconfig.url"]

# 保存缓存镜像路径
CACHE_IMAGE_DIR = automation_param["cache.image.dir"]
if not str(CACHE_IMAGE_DIR).endswith("/"):
    CACHE_IMAGE_DIR = CACHE_IMAGE_DIR + "/"
# docker镜像仓库用户名
DOCKER_REPOSITORY_USER = 'ebank'
# docker镜像仓库密码
DOCKER_REPOSITORY_PASSWORD = 'xxx'
# 直推投产环境ip
CAAS_REGISTRY = automation_param["autorelease.docker.push.registry"]
# 自动化脚本地址
AUTOMATIC_PRODUCTION_URL = automation_param["automatic.production.git.url"]
# faketime镜像推送空间名称
FAKE_CASS_REGISTRY_NAMESPACE = automation_param["fake.cass.registry.namespace"]


def step1_makedir(prod_dir):
    logging.info("[+] 步骤一： 新建变更目录 ")
    logging.info("本次变更目录: " + prod_dir)
    if os.path.exists(prod_dir):
        logging.info("重新准备介质，删除原目录: " + prod_dir)
        shutil.rmtree(prod_dir)
    else:
        os.makedirs(prod_dir)
    end_step()


def handle_zip_file(bucket_path):
    bucket_file_list = os.listdir(bucket_path)
    for bucket_file in bucket_file_list:
        zfile_path = bucket_path + "/" + bucket_file
        logging.info("zfile_path:" + zfile_path)
        # 桶目录下是文件且是压缩包
        if os.path.isfile(zfile_path) and zfile_path[-4:] == '.zip':
            if bucket_file.find("_") != -1 and bucket_file.split("_")[1] == "folder.zip":
                zfile = zipfile.ZipFile(zfile_path, "r")
                for zip_file in zfile.namelist():
                    zfile.extract(zip_file, bucket_path + "/" + bucket_file.split("_")[0])
                zfile.close()
                # 解压成功后删除zip包
                os.remove(zfile_path)
        # 桶目录下级仍是文件夹/fdev/imagecache/autorelease/DOCKER_G08_20211203_1800_11302046/DOCKER_G08_20211203_1800/AWS/PROC/mobper-staticresource/prd/mspmk-cli-showrouterthree_folder.zip
        elif os.path.isdir(zfile_path):
            bucket_path_list = os.listdir(zfile_path)
            for bucket_zip_file in bucket_path_list:
                bucket_path_dir = zfile_path + "/" + bucket_zip_file
                logging.info("bucket_path_dir: " + bucket_path_dir)
                if os.path.isfile(bucket_path_dir) and bucket_path_dir[-4:] == '.zip':
                    if bucket_zip_file.find("_") != -1 and bucket_zip_file.split("_")[1] == "folder.zip":
                        zfile = zipfile.ZipFile(bucket_path_dir, "r")
                        for zip_file in zfile.namelist():
                            zfile.extract(zip_file, zfile_path + "/" + bucket_zip_file.split("_")[0])
                        zfile.close()
                        # 解压成功后删除zip包
                        os.remove(bucket_path_dir)


def write_aws(prod_detail, prod_dir, catalog_name):
    aws_configure_assets = query_aws_configure_assets(prod_detail["aws_group"])
    aws_env = ["DEV", "PROC"]
    for env in aws_env:
        file_dir = prod_dir + "/" + catalog_name + "/" + env + "/"
        if not os.path.exists(file_dir):
            raise Exception("脚本文件目录错误：" + file_dir)
        script_file_list = os.listdir(file_dir)
        for bucket_name in script_file_list:
            bucket_dir = file_dir + bucket_name
            logging.info("桶目录：" + bucket_dir)
            if os.path.exists(bucket_dir):
                # 对通过文件夹上传的zip压缩包进行解压处理，然后删除zip包
                if os.path.isdir(bucket_dir):
                    handle_zip_file(bucket_dir)
                # 对桶目录打tar包
                tar = tarfile.open(bucket_dir + ".tar", "w")
                for root, dir, files in os.walk(bucket_dir):
                    for file in files:
                        fullpath = os.path.join(root, file)
                        alias = fullpath.split(env + "/")[1]
                        tar.add(fullpath, arcname=alias)
                tar.close()
                # 打成tar包后删除文件夹目录
                shutil.rmtree(bucket_dir)
        # 对象存储在对应的目录生成order.txt文件
        aws_order = file_dir + "/" + "order.txt"
        bucket_file_list = os.listdir(file_dir)
        order_content = ""
        for bucket_file in bucket_file_list:
            order_content = order_content + bucket_file + "\n"
        f = open(aws_order, mode='a')
        f.write(order_content + "\n")
        f.close()

        # 对象存储在对应的目录生成aws_configure.txt文件
        aws_configure = file_dir + "/" + "aws_configure.txt"
        envInfo = aws_configure_assets[0]["env_config_info"][env]
        aws_configure_content = (
            "accessKeyID@:::::@" + envInfo["access_key"] + "\n" + "secretAccessKey@:::::@" +
            envInfo["secret_key"] + "\n" + "endPoint@:::::@" + envInfo[
                "end_point"])
        f = open(aws_configure, mode='a')
        f.write(aws_configure_content + "\n")
        f.close()


def write_esfcommonconfig(prod_asset, prod_detail, service_sid, prod_dir, service_path):
    # 拼接esfcommonconfig介质目录的最后一层目录: 应用sid_变更日期目录
    last_content = prod_asset["sid"] + "_" + prod_detail["date"].replace("/", "")
    service_sid.add(last_content)
    config_info_dir = prod_dir + "/" + prod_asset["asset_catalog_name"] + "/" + prod_asset[
        "runtime_env"] + "/" + last_content
    config_info = config_info_dir + '/config.info'
    file_dir = service_path + "esfcommonconfig/" + prod_asset["runtime_env"] + "/"
    if not os.path.exists(file_dir):
        raise Exception("脚本文件目录错误：" + file_dir)
    script_file_list = os.listdir(file_dir)
    # 将预设模板中sh文件copy到临时目录下
    for script_file in script_file_list:
        if os.path.isfile(file_dir + script_file) and script_file != "config.info":
            cmd_move_file = "cp " + file_dir + script_file + " " + config_info_dir
            logging.info("cmd_move_file:" + cmd_move_file)
            exception_resolver(commands.getstatusoutput(cmd_move_file))
    # 修改config.info文件内容 桶名 sid 上传的文件名
    # procsh,prochf分别有两个桶名，同一个文件名，需写两行
    if prod_asset["runtime_env"] == "PROCSH" or prod_asset["runtime_env"] == "PROCHF":
        bucketlist = prod_asset["bucket_name"].split(",")
        config_info_content = ""
        for bucketname in set(bucketlist):
            config_info_content = config_info_content + bucketname + " " + prod_asset["sid"] + " " + prod_asset[
                "fileName"] + "\n"
    else:
        config_info_content = prod_asset["bucket_name"] + " " + prod_asset["sid"] + " " + prod_asset[
            "fileName"] + "\n"
    f = open(config_info, mode='a')
    f.write(config_info_content)
    f.close()


def write_app_esf(app_esf_dir, service_sid):
    app_esf_sh = app_esf_dir + "/uncompress.sh"
    sh_head_cmd = "#!/bin/bash\nexport LANG=zh_CN.gb18030\nfailnumber=0\n"
    app_esf_sh_content = sh_head_cmd
    sh_cmd = "a=`cat ./log/success.log | grep 成功 | wc -l`\n" \
             "b=`cat ./config.info | wc -l`\n" \
             "echo `cat ./log/error.log`\n" \
             "echo `cat ./log/success.log`\n" \
             "if [ $a != $b ]\n" \
             "then\n" \
             "  failnumber=1\n" \
             "fi\n"
    for sid in service_sid:
        # app_esf目录下sh脚本里需要进入的服务器目录,一个应用四个环境目录下sid相同，执行的py脚本也相同
        cd_dir = "cd /app/s3Config/config/" + sid
        # 写sh文件内容
        app_esf_sh_content = app_esf_sh_content + cd_dir + "\n" + "sh  upload-process.sh" + "\n" + sh_cmd + "\n"
    sh_end_cmd = "if [ $failnumber -gt 0 ]\n" \
                 "then\n" \
                 "  echo \"fail,详情查看文件./log/error.log\"\n" \
                 "  echo 1\n" \
                 "  exit 1\n" \
                 "else\n" \
                 "  echo \"success\"\n" \
                 "  echo 0\n" \
                 "  exit 0\n" \
                 "fi\n"
    app_esf_sh_content = app_esf_sh_content + "\n" + sh_end_cmd
    logging.info(app_esf_sh_content)
    f = codecs.open(app_esf_sh, mode='a', encoding="gb2312")
    f.write(app_esf_sh_content)
    f.close()
    # app_esf介质目录写order文件，文件内容为uncompress.sh名称
    app_esf_order_txt = "uncompress.sh"
    app_esf_order = app_esf_dir + "/order.txt"
    f = open(app_esf_order, mode='a')
    f.write(app_esf_order_txt)
    f.close()


def write_esf(prod_dir, service_path, catalog_name):
    esf_env = ["DEV", "TEST", "PROCSH", "PROCHF"]
    # 发交易获取用户注册信息
    esf_user_list = query_esf_userinfo(prod_id)
    if len(esf_user_list) != 0:
        for esf in esf_user_list:
            order_dir = ""
            for env in esf_env:
                order_dir = prod_dir + "/" + catalog_name + "/" + env + "/"
                if not os.path.exists(order_dir):
                    os.makedirs(order_dir)
                esf_order = order_dir + "order.txt"
                # 开始生成order内容
                esf_env_info = esf["esf_info"][env]
                logging.info(esf_env_info)
                esf_order_txt = ("account:" + esf_env_info["account"] + "%password:" + esf_env_info["password"] +
                                 "%sid:" + esf["sid"] + "%filename:" + esf["application_en"] + ".yaml")
                file_dir = service_path + catalog_name + "/" + env + "/"
                if not os.path.exists(file_dir):
                    raise Exception("脚本文件目录错误：" + file_dir)
                script_file_list = os.listdir(file_dir)
                # copy python脚本、yaml文件到临时目录下
                for script_file in script_file_list:
                    if os.path.isfile(file_dir + script_file) and script_file != "order.txt":
                        cmd_move_file = "cp " + file_dir + script_file + " " + order_dir
                        logging.info("cmd:" + cmd_move_file)
                        exception_resolver(commands.getstatusoutput(cmd_move_file))
                # 对yaml文件根据应用名重命名
                temp_file_list = os.listdir(order_dir)
                for yaml_file in temp_file_list:
                    if os.path.isfile(order_dir + yaml_file) and yaml_file == "template.yaml":
                        os.rename(order_dir + yaml_file, order_dir + esf["application_en"] + ".yaml")
                        # 更新yaml文件内容SDK_GK的值
                        with open(order_dir + esf["application_en"] + ".yaml") as f:
                            doc = yaml.safe_load(f)
                        logging.info(doc)
                        for env in doc['spec']['template']['spec']['containers'][0]['env']:
                            env['value'] = esf_env_info["sdk_gk"]
                        with open(order_dir + esf["application_en"] + ".yaml", 'w') as f:
                            yaml.safe_dump(doc, f, default_flow_style=False)
                f = open(esf_order, mode='a')
                f.write(esf_order_txt + "\n")
                f.close()


def write_dynamic(prod_dir, catalog_name):
    download_dynamic_path = prod_dir + "/" + catalog_name + "/aoms.sh"
    logging.info(download_dynamic_path)
    logging.info(MINIO_DYNAMICCONFIG_URL)
    if not os.path.exists(download_dynamic_path):
        logging.info("aoms.sh文件不存在，从minio中获取aoms.sh")
        download_minio_file(download_dynamic_path, MINIO_DYNAMICCONFIG_URL, "fdev-release")
        logging.info("aoms.sh文件从minio下载完成,minio地址：" + MINIO_DYNAMICCONFIG_URL)
    # dynamicconfig介质目录写order文件，文件内容为aoms.sh
    dynamicconfig_order_content = "aoms.sh"
    dynamicconfig_order = prod_dir + "/" + catalog_name + "/order.txt"
    f = open(dynamicconfig_order, mode='a')
    f.write(dynamicconfig_order_content)
    f.close()


def handle_nofilecontent_commonconfig(prod_detail, prod_dir, catalog_name):
    proc_envlist = ["DEV", "TEST", "PROCHF", "PROCSH", "DEVSCC", "TESTSCC", "PROCHFSCCK1", "PROCHFSCCK2", "PROCSHSCCK1",
                    "PROCSHSCCK2"]
    gray_envlist = ["DEV", "TEST", "PROCHF", "PROCSH", "DEVSCC", "TESTSCC", "PROCHFSCCHD", "PROCSHSCCHD"]
    if prod_detail["type"] == "proc":
        envlist = proc_envlist
    else:
        envlist = gray_envlist
    for env in envlist:
        file_dir = prod_dir + "/" + catalog_name + "/" + env + "/"
        if not os.path.exists(file_dir):
            os.makedirs(file_dir)
        file_list = os.listdir(file_dir)
        if file_list:
            logging.info("commonconfig/" + env + "目录下的文件：" + ','.join(file_list))
            continue
        else:
            # 生成一个空的order.txt文件
            order_path = file_dir + "/" + "order.txt"
            f = open(order_path, mode='a')
            f.close()


def handle_sourcemaptar_file(prod_dir, catalog_name):
    envlist = ["DEV", "TEST", "PROCSH"]
    for env in envlist:
        tar_path = prod_dir + "/" + catalog_name + "/" + env + "/mupload/spdb-footprint-processor/"
        logging.info("tar_path: " + tar_path)
        if os.path.isdir(tar_path):
            sourcemap_tar_list = os.listdir(tar_path)
            for tar_file in sourcemap_tar_list:
                tar_path_file = tar_path + tar_file
                if os.path.isfile(tar_path_file) and tar_path_file[-4:] == ".tar":
                    tar = tarfile.open(tar_path_file, mode="r:")
                    tar.extractall(path=tar_path)
                    tar.close()
                    # 解压成功后删除zip包
                    os.remove(tar_path_file)


def step2_config_files(prod_dir, prod_detail, prod_assets, logging_file):
    """
    准备配置文件
    :param prod_dir: 变更目录
    :param prod_detail:  变更详情
    :param prod_assets:  变更介质列表
    :return:
    """
    logging.info("[+] 步骤二： 检出配置文件 ")
    if len(prod_assets) == 0:
        logging.info("==本次变更无配置文件投产==")
        return
    logging.info("==开始准备配置文件==")
    # 生成批量任务介质目录
    batchtaskinfo = query_batch_Info(prod_detail["prod_id"])
    if (batchtaskinfo is not None) and (batchtaskinfo != ""):
        for batchTask in batchtaskinfo:
            batch_dir = prod_dir + "/batch"
            if not os.path.exists(batch_dir):
                os.makedirs(batch_dir)
            logging.info("batch介质目录：" + batch_dir)
            batch_order_txt = batchTask["batchInfo"]
            batch_order = batch_dir + "/order.txt"
            f = open(batch_order, mode='a')
            f.write(batch_order_txt + "\n")
            f.close()
    for catalog_assets in prod_assets:
        catalog_name = catalog_assets["catalog_name"]
        catalog_type = catalog_assets["catalog_type"]
        ordered_assets = {}
        # 是否有esfcommonconfig介质目录
        has_esfcommonconfig_flag = False
        if catalog_name == "esfcommonconfig":
            has_esfcommonconfig_flag = True
            app_esf_dir = prod_dir + "/app_esf"
            if not os.path.exists(app_esf_dir):
                os.makedirs(app_esf_dir)
        # 项目目录，为找到预设模板copy到临时目录
        service_path = DIR + "/container_scc/"
        service_sid = set()
        for prod_asset in catalog_assets["assets"]:
            file_name = prod_asset["fileName"]
            if prod_asset["source"] == "2" or prod_asset["source"] == "3":
                resource_giturl = prod_asset["file_giturl"].split("/blob/")[0]
                file_fullpath = prod_asset["file_giturl"].split("/blob/")[1]
                branch = file_fullpath[0:file_fullpath.index("/")]
                file_fullpath = file_fullpath[file_fullpath.index("/"):]
                repo_local_path = resource_giturl.replace(GITLAB_URL, TEMP_DIR)
                if prod_asset["source"] == "3":
                    checkout_resource(resource_giturl, repo_local_path, branch, False)
                else:
                    checkout_resource(resource_giturl, repo_local_path, branch, True)
                file_source_path = repo_local_path + file_fullpath
                file_target_dir = prod_dir + "/" + catalog_name + "/"
                if catalog_type == "5" or catalog_type == "9" or catalog_type == "10":
                    # 选择的文件目录必须包含对应的环境名
                    runtime_env = prod_asset["runtime_env"]
                    file_target_dir = prod_dir + "/" + catalog_name + "/" + prod_asset["runtime_env"] + "/"
                    # fdev生成的配置文件
                    if prod_asset["source"] == "3":
                        fdev_file_path = file_fullpath[file_fullpath.index("/") + 1:]
                        md5_file_head = fdev_file_path[:fdev_file_path.index("/")]
                        if md5_file_head == "cli-md5":
                            file_target_dir = prod_dir + "/" + catalog_name + "/" + runtime_env + "/" + "cli-md5" + "/"
                        else:
                            file_target_dir = prod_dir + "/" + catalog_name + "/" + runtime_env + "/" + \
                                              fdev_file_path[fdev_file_path.index("/") + 1
                                              :fdev_file_path.index(file_name)]
                    # 文件名包含环境时子目录需copy至对应环境
                    elif file_fullpath.find(runtime_env) >= 0:
                        file_target_dir = prod_dir + "/" + catalog_name + "/" + \
                                          file_fullpath[file_fullpath.index(runtime_env):file_fullpath.index(file_name)]
                file_target_path = file_target_dir + file_name
                logging.info("拷贝文件地址为：" + file_target_path)
                if not os.path.exists(file_target_dir):
                    os.makedirs(file_target_dir)
                logging.info("拷贝文件 " + file_source_path + "  -->  " + file_target_path)
                output = commands.getoutput("cp " + file_source_path + " " + file_target_path)
                if output != "":
                    logging.info(output)
            elif prod_asset["source"] == "1":
                if prod_detail["version"] in prod_asset["file_giturl"]:
                    logging.info(prod_asset["file_giturl"].split(prod_detail["version"])[1].split("/")[1])
                    if prod_asset["file_giturl"].split(prod_detail["version"])[1].split("/")[1] == "dynamicconfig":
                        url_tail = prod_asset["file_giturl"].split(prod_detail["version"])[1].split(catalog_name)[1]
                        download_path = prod_dir + "/" + catalog_name + "/data" + url_tail
                        logging.info("动态配置文件从minio下载至本地地址为：" + download_path)
                    else:
                        download_path = prod_dir + prod_asset["file_giturl"].split(prod_detail["version"])[1]
                        logging.info("文件从minio下载至本地地址为：" + download_path)
                else:
                    if prod_asset["asset_catalog_name"] == "AWS_STATIC":
                        # 持续集成传过来的zip包
                        file_name = file_name[:file_name.index(".")] + "_folder" + file_name[file_name.index("."):]
                        download_path = prod_dir + "/" + prod_asset["asset_catalog_name"] + "/" + prod_asset[
                            "runtime_env"] + "/" + prod_asset["bucket_name"] + "/" + prod_asset[
                                            "bucket_path"] + "/" + file_name
                    else:
                        download_path = prod_dir + "/" + prod_asset["asset_catalog_name"] + "/" + file_name
                download_dir = download_path[:download_path.index(file_name)]
                if not os.path.exists(download_dir):
                    os.makedirs(download_dir)
                if "/" in file_name:
                    app_dir = download_dir + "/" + file_name.split("/")[0]
                    if not os.path.exists(app_dir):
                        os.makedirs(app_dir)
                logging.info("文件下载至本地地址为:" + download_path)
                download_minio_file(download_path, prod_asset["file_giturl"], "fdev-release")
            seq_no = prod_asset["seq_no"]
            if (seq_no is not None) and (seq_no != ""):
                if (catalog_type == "3") and (prod_asset["write_flag"] == "0"):
                    ordered_assets[seq_no] = file_name
                elif catalog_type != "3":
                    ordered_assets[seq_no] = file_name
            # 开始生成esfcommonconfig介质目录
            if prod_asset["asset_catalog_name"] == "esfcommonconfig":
                write_esfcommonconfig(prod_asset, prod_detail, service_sid, prod_dir, service_path)
        # 开始生成app_esf介质目录,前置条件是包含esfcommonconfig目录
        if has_esfcommonconfig_flag:
            write_app_esf(app_esf_dir, service_sid)
        # 开始处理commonconfig目录下各个环境目录下是否有文件夹或文件，若都无则生成一个空的order.txt文件
        if catalog_name == "commonconfig":
            handle_nofilecontent_commonconfig(prod_detail, prod_dir, catalog_name)
        # 开始生成AWS对象存储介质包目录
        if catalog_type == "7":
            write_aws(prod_detail, prod_dir, catalog_name)
        # 开始生成esf介质目录
        if catalog_type == "8":
            write_esf(prod_dir, service_path, catalog_name)
        if catalog_type == "9":
            write_dynamic(prod_dir, catalog_name)
        # 解压bastioncommonconfig目录下各环境目录下的tar包
        if catalog_type == "10":
            handle_sourcemaptar_file(prod_dir, catalog_name)
        # 生成order.txt文件
        ordered_size = len(ordered_assets)
        ordered_sizes = len(catalog_assets["assets"])
        if catalog_type == "3":
            if ordered_size != 0:
                order_txt_path = prod_dir + "/" + catalog_name + "/" + "order.txt"
                logging.info("开始生成order.txt文件:" + order_txt_path)
                f = open(order_txt_path, mode="a")
                for num in range(1, ordered_sizes + 1):
                    ordered_file = ordered_assets.get(str(num))
                    if (ordered_file is None) or (ordered_file == ""):
                        continue
                    logging.info(str(num) + " " + ordered_file)
                    order_string = ordered_file + "\n"
                    f.write(order_string)
            else:
                order_txt_path = prod_dir + "/" + catalog_name + "/" + "order.txt"
                logging.info("开始生成order.txt文件:" + order_txt_path)
                f = open(order_txt_path, mode="a")
                f.write("")
            f.close()
        if catalog_type != "3":
            if ordered_size != 0:
                order_txt_path = prod_dir + "/" + catalog_name + "/" + "order.txt"
                logging.info("开始生成order.txt文件:" + order_txt_path)
                f = open(order_txt_path, mode="a")
                for num in range(1, ordered_size + 1):
                    ordered_file = ordered_assets.get(str(num))
                    logging.info(str(num))
                    if (ordered_file is None) or (ordered_file == ""):
                        raise Exception("有序文件序号错误")
                    logging.info(str(num) + " " + ordered_file)
                    order_string = ordered_file + "\n"
                    f.write(order_string)
                f.close()
    # 生成template.properties 文件
    template_properties = prod_detail["template_properties"]
    if (template_properties is not None) and (len(template_properties) > 0):
        logging.info("==准备template.properties==")
        logging.info("内容如下:\n" + template_properties)
        tpf = open(prod_dir + "/templete.properties", mode="a")
        tpf.write(template_properties)
        tpf.close()
        logging.info("==template.properties准备完毕==")
    logging.info("==配置文件dos转unix==")
    for root, dirs, files in os.walk(prod_dir):
        for tmp_file in files:
            if tmp_file.find(".") < 0:
                continue
            file_type = "." + tmp_file.split(".")[1]
            file_path = os.path.join(root, tmp_file)
            if (DOS_UNIX_FILE_TYPE is not None) and (file_type in DOS_UNIX_FILE_TYPE):
                logging.info("dos2unix-->" + file_path)
                dos2unix(file_path)
    logging.info("==配置文件dos转unix完毕==")
    logging.info("==配置文件准备完毕==")
    end_step()


def step3_config_check(prod_dir, prod_assets, prod_detail):
    logging.info("[+] 步骤三： 检查变更文件 ")
    if len(prod_assets) == 0:
        logging.info("==本次变更无变更文件投产==")
    template_properties = prod_detail["template_properties"]
    logging.info("==开始检查template.properties==")
    for catalog_assets in prod_assets:
        catalog_name = catalog_assets["catalog_name"]
        catalog_type = catalog_assets["catalog_type"]
        if catalog_type == "4":
            for prod_asset in catalog_assets["assets"]:
                file_name = prod_asset["fileName"]
                if (template_properties is None) or (template_properties.strip() == ""):
                    raise Exception("缺少template.properties文件！")
                if template_properties.find(file_name) < 0:
                    raise Exception(catalog_name + "目录下" + file_name + "未在template.properties中定义")
    logging.info("==template.properties检查完毕==")
    logging.info("==开始检查变更文件==")
    logging.info("介质目录：" + prod_dir)

    db_files = get_db_assets(prod_assets)
    local_files = {}
    for root, dirs, files in os.walk(prod_dir):
        for tmp_file in files:
            # 过滤 order.txt 和 template.properties
            if (tmp_file.find("order.txt") < 0) and (tmp_file.find("templete.properties") < 0):
                # if (root.find("db_") >= 0) and (tmp_file.find(".sql") > 0) and (not tmp_file.startswith("ebank_")):
                #     raise Exception(root + "/" + tmp_file + "命名不规范，需以ebank_开头！")
                key = os.path.join(root, tmp_file).replace(prod_dir + "/", "")
                local_files[key] = ""
    logging.info("数据库已记录文件列表:")
    for db_file in db_files:
        logging.info("    " + db_file)
    logging.info("临时已准备文件列表:")
    for local_file in local_files:
        logging.info("    " + local_file)
    for db_file_key in db_files.keys():
        if db_file_key not in local_files.keys():
            # 对象存储文件跳过校验
            if (db_file_key.split("/")[0] in "AWS_STATIC") or (db_file_key.split("/")[0] in "AWS_COMMON") or (
                        db_file_key.split("/")[0] in "esfcommonconfig") or (db_file_key.split("/")[0] in "esf") or (
                        db_file_key.split("/")[0] in "app_esf") or (db_file_key.split("/")[0] in "dynamicconfig") or (
                        db_file_key.split("/")[0] in "bastioncommonconfig"):
                continue
            exception_msg = "[" + db_file_key + "]数据库存在，准备介质中不存在"
            logging.info(exception_msg)
            raise Exception(exception_msg)
    for local_file_key in local_files.keys():
        if local_file_key not in db_files.keys():
            # 对象存储文件跳过校验
            if (local_file_key.split("/")[0] in "AWS_STATIC") or (local_file_key.split("/")[0] in "AWS_COMMON") or (
                        local_file_key.split("/")[0] in "esfcommonconfig") or (
                        local_file_key.split("/")[0] in "esf") or (local_file_key.split("/")[0] in "app_esf") or (
                        local_file_key.split("/")[0] in "dynamicconfig") or (
                        local_file_key.split("/")[0] in "bastioncommonconfig") or (
                        local_file_key.split("/")[1] in "aoms.sh"):
                continue
            exception_msg = "[" + local_file_key + "]准备介质中存在,数据库不存在"
            logging.info(exception_msg)
            raise Exception(exception_msg)
    logging.info("==检查各环境公共配置文件是否一致==")
    commonconfig_file = commonconfig_files(prod_assets)
    for commonconfig_items in commonconfig_file.items():
        logging.info("检查目录：" + commonconfig_items[0])
        commonconfigs = commonconfig_items[1]
        caas_tmp_set = set()
        scc_tmp_set = set()
        caas_file_count = 0
        scc_file_count = 0
        for env_files in commonconfigs.items():
            env = env_files[0]
            files = env_files[1]
            logging.info(env_files[0] + ": " + str(files))
            if env == "PROCHF":
                continue
            if "SCC" in env:
                if (scc_file_count != 0) and (scc_file_count != len(files)):
                    raise Exception(commonconfig_items[0] + "/" + env + "目录文件数与其他环境不一致,请检查")
                scc_file_count = len(files)
                if (len(scc_tmp_set) != 0) and (scc_tmp_set != set(files)):
                    raise Exception(commonconfig_items[0] + "/" + env + "目录文件内容与其他环境不一致,请检查")
                scc_tmp_set = set(files)
            else:
                if (caas_file_count != 0) and (caas_file_count != len(files)):
                    raise Exception(commonconfig_items[0] + "/" + env + "目录文件数与其他环境不一致,请检查")
                caas_file_count = len(files)
                if (len(caas_tmp_set) != 0) and (caas_tmp_set != set(files)):
                    raise Exception(commonconfig_items[0] + "/" + env + "目录文件内容与其他环境不一致,请检查")
                caas_tmp_set = set(files)
    logging.info("==各环境公共配置文件一致==")
    logging.info("==已记录变更文件与准备变更文件一致==")
    end_step()


def step4_app_images(prod_dir, prod_applications, prod_images, image_deliver_type,
                     prod_id, prod_type, prod_detail, scc_flag):
    logging.info("[+] 步骤四： 准备镜像脚本")
    if len(prod_applications) == 0:
        logging.info("==本次变更无应用投产==")
        return
    logging.info("==开始应用打包==")
    release_node_name = prod_detail["release_node_name"]
    automation_env_names = query_automation_env()
    i = 0
    product_image_url_all_list = []
    for prod_application in prod_applications:
        i = i + 1
        application_id = prod_application["application_id"]
        application_gitlab_id = prod_application["gitlab_project_id"]
        release_type = prod_application["release_type"]
        app_name_en = prod_application["app_name_en"]
        app_name_zh = prod_application["app_name_zh"]
        pro_image_uri = prod_application["pro_image_uri"]
        pro_scc_image_uri = prod_application["pro_scc_image_uri"]
        if (pro_image_uri is None) or (pro_image_uri == ""):
            pro_image_uri = pro_scc_image_uri
        net_work = prod_application["network"]
        fdev_config_changed = prod_application["fdev_config_changed"]
        fdev_config_confirm = prod_application["fdev_config_confirm"]
        docker_dirs = prod_application["prod_dir"]
        change = prod_application["change"]
        type_name = prod_application["type_name"]
        esf_flag = prod_application["esf_flag"]
        tag = prod_application["tag"]
        caas_stop_env = prod_application["caas_stop_env"]
        scc_stop_env = prod_application["scc_stop_env"]
        logging.info(str(i) + ". " + app_name_en + "(" + app_name_zh + ")")
        if (pro_image_uri is None) or (pro_image_uri == ""):
            logging.info("应用[" + app_name_en + "]尚未选择镜像版本")
            raise Exception("应用[" + app_name_en + "]尚未选择镜像版本")
        logging.info("应用类型：" + prod_application["type_name"])
        if scc_flag == "0":
            automatic_production_service_path = DIR + "/service/"
            yaml_service_path = DIR + "/service/"
            if type_name == "容器化项目":
                automatic_production_service_path = DIR + "/container/"
                yaml_service_path = DIR + "/container/"
        else:
            # 支持scc变更，容器化和微服务都使用容器化order2py.py
            automatic_production_service_path = DIR + "/container_scc/"
            yaml_service_path = DIR + "/container_scc/"
        if fdev_config_changed is not None and fdev_config_changed and fdev_config_confirm != "1":
            logging.info("应用[" + app_name_en + "]配置文件有变化，请审核")
            raise Exception("应用[" + app_name_en + "]配置文件有变化，请审核")
        if scc_flag == "1":
            product_image_url_list = tag_image_all(logging, release_node_name, prod_id, application_id, prod_dir,
                                                   release_type, pro_image_uri, app_name_en, prod_images, prod_type,
                                                   automation_env_names, net_work, automatic_production_service_path,
                                                   application_gitlab_id, yaml_service_path, docker_dirs,
                                                   change, type_name, esf_flag, tag, caas_stop_env, scc_stop_env)
        else:
            product_image_url_list = tag_image(logging, release_node_name, prod_id, application_id, prod_dir,
                                               release_type, pro_image_uri, app_name_en, prod_images, prod_type,
                                               automation_env_names, net_work, automatic_production_service_path,
                                               application_gitlab_id, yaml_service_path, docker_dirs,
                                               change, type_name)
        if len(product_image_url_list) != 0:
            for product_image_url in product_image_url_list:
                product_image_url_all_list.append(product_image_url)
        update_application(prod_id, application_id, '1')
    if len(product_image_url_all_list) != 0:
        logging.info(product_image_url_all_list)
        for img_url in product_image_url_all_list:
            logging.info("镜像推送地址：" + img_url)
    logging.info("==应用打包完毕==")
    end_step()


def write_docker_yaml_txt(logging, prod_dir, yaml_order_txt, application_gitlab_id, tag_name, automation_env_names,
                          prod_type, net_work, app_name_en, yaml_service_path, change, type_name):
    gitlab_url = query_autoconfig_gitlab_url(application_gitlab_id, tag_name)
    if gitlab_url is not None and gitlab_url != "":
        local_path = gitlab_url.replace(GITLAB_URL, TEMP_DIR)
        # 拉取配置文件项目
        checkout_resource(gitlab_url, local_path, tag_name, False)
        for env in automation_env_names:
            if type_name == "容器化项目":
                env_order_txt = (yaml_order_txt + "%checkAndUp:0")
            else:
                env_order_txt = (yaml_order_txt + "%checkAndUp:1")
            if change is not None:
                for item, value in change.items():
                    if item == env["env_name"]:
                        for orderKey, orderValue in value.items():
                            if orderKey == "replicas" or orderKey == "scc_replicas":
                                continue
                            env_order_txt = env_order_txt + "%" + orderKey + ":" + str(orderValue)
            order_dir = prod_dir + "/docker_yaml" + env["env_name"]
            docker_order = order_dir + '/order.txt'
            fdev_env_name = env["fdev_env_name"][prod_type][net_work]
            app_name_head = app_name_en.split("-")[0]
            yaml_file_dir = local_path + "/" + ''.join(fdev_env_name) + "/" + app_name_head + "/"
            yaml_dir_create = False
            if os.path.exists(yaml_file_dir):
                yaml_file_list = os.listdir(yaml_file_dir)
                for yaml_file in yaml_file_list:
                    if os.path.isfile(yaml_file_dir + yaml_file) and yaml_file.endswith(".yaml") \
                            and not yaml_file.endswith("-scc.yaml"):
                        yaml_dir_create = True
                        if not os.path.exists(order_dir):
                            os.makedirs(order_dir)
                        cmd_move_yaml_file = "cp " + yaml_file_dir + yaml_file + " " + order_dir
                        logging.info("拷贝文件 " + yaml_file_dir + yaml_file + "  -->  " + order_dir + "/" + yaml_file)
                        exception_resolver(commands.getstatusoutput(cmd_move_yaml_file))
            if yaml_dir_create:
                file_dir = yaml_service_path + "docker_yaml" + env["env_name"] + "/"
                if not os.path.exists(file_dir):
                    raise Exception("脚本文件目录错误：" + file_dir)
                script_file_list = os.listdir(file_dir)
                for script_file in script_file_list:
                    if os.path.isfile(file_dir + script_file) and script_file != "order.txt":
                        cmd_move_file = "cp " + file_dir + script_file + " " + order_dir
                        exception_resolver(commands.getstatusoutput(cmd_move_file))
                f = open(docker_order, mode='a')
                f.write(env_order_txt + "\n")
                f.close()
            else:
                logging.info("<strong style='color: red;'>docker_yaml缺少" + env["env_name"] + "目录</strong>")
        if not os.path.exists(prod_dir + "/docker_yaml"):
            raise Exception("yaml文件未生成，请对当前应用【" + app_name_en + "】对应tag：" + tag_name + "重新发起pipeline")


def write_docker_yaml_txt_new(logging, prod_dir, yaml_order_txt, application_gitlab_id, tag_name, automation_env_names,
                              prod_type, net_work, app_name_en, yaml_service_path, change, type_name):
    logging.info("====进入写docker_yaml目录====")
    gitlab_url = query_autoconfig_gitlab_url(application_gitlab_id, tag_name)
    if gitlab_url is not None and gitlab_url != "":
        local_path = gitlab_url.replace(GITLAB_URL, TEMP_DIR)
        # 拉取配置文件项目
        checkout_resource(gitlab_url, local_path, tag_name, False)
        for env in automation_env_names:
            fdev_env = env["fdev_env_name"][prod_type][net_work]
            # 获取order中需从实体中获取到参数
            get_other_order_params = query_env_params("", str(application_gitlab_id), "docker_yaml", ''.join(fdev_env))
            yaml_order_txt_new = (yaml_order_txt + "%namespace:" + get_other_order_params["fdev_caas_tenant"] +
                                  "%imagespacename:" + get_other_order_params["fdev_caas_registry_namespace"] +
                                  "%dceuser:" + get_other_order_params["fdev_caas_user"] + "%imageuser:" +
                                  get_other_order_params["fdev_caas_registry_user"])
            if type_name == "容器化项目":
                yaml_order_txt_all = (yaml_order_txt_new + "%checkAndUp:0")
            else:
                yaml_order_txt_all = (yaml_order_txt_new + "%port:8080%checkAndUp:1")
            env_order_txt = yaml_order_txt_all
            if change is not None:
                for item, value in change.items():
                    if item == env["env_name"]:
                        for orderKey, orderValue in value.items():
                            if orderKey == "replicas" or orderKey == "scc_replicas":
                                continue
                            env_order_txt = env_order_txt + "%" + orderKey + ":" + str(orderValue)
            order_dir = prod_dir + "/docker_yaml" + env["env_name"]
            docker_order = order_dir + '/order.txt'
            fdev_env_name = env["fdev_env_name"][prod_type][net_work]
            fdev_env = ''.join(fdev_env_name)
            app_name_head = app_name_en.split("-")[0]
            yaml_file_dir = local_path + "/" + fdev_env + "/" + app_name_head + "/"
            yaml_dir_create = False
            if os.path.exists(yaml_file_dir):
                yaml_file_list = os.listdir(yaml_file_dir)
                for yaml_file in yaml_file_list:
                    if os.path.isfile(yaml_file_dir + yaml_file) and yaml_file.endswith(".yaml") \
                            and not yaml_file.endswith("-scc.yaml"):
                        yaml_dir_create = True
                        if not os.path.exists(order_dir):
                            os.makedirs(order_dir)
                        cmd_move_yaml_file = "cp " + yaml_file_dir + yaml_file + " " + order_dir
                        logging.info("拷贝文件 " + yaml_file_dir + yaml_file + "  -->  " + order_dir + "/" + yaml_file)
                        exception_resolver(commands.getstatusoutput(cmd_move_yaml_file))
            if yaml_dir_create:
                file_dir = yaml_service_path + "docker_yaml" + env["env_name"] + "/"
                if not os.path.exists(file_dir):
                    raise Exception("脚本文件目录错误：" + file_dir)
                script_file_list = os.listdir(file_dir)
                for script_file in script_file_list:
                    if os.path.isfile(file_dir + script_file) and script_file != "order.txt":
                        cmd_move_file = "cp " + file_dir + script_file + " " + order_dir
                        exception_resolver(commands.getstatusoutput(cmd_move_file))
                logging.info("order.txt:" + env_order_txt)
                f = open(docker_order, mode='a')
                f.write(env_order_txt + "\n")
                f.close()
            else:
                logging.info("<strong style='color: red;'>docker_yaml缺少" + env["env_name"] + "目录</strong>")
        if not os.path.exists(prod_dir + "/docker_yaml"):
            raise Exception("yaml文件未生成，请对当前应用【" + app_name_en + "】对应tag：" + tag_name + "重新发起pipeline")


def write_scc_yaml_txt(logging, prod_dir, yaml_order_txt, application_gitlab_id, tag_name, automation_env_names,
                       prod_type, net_work, app_name_en, yaml_service_path, type_name):
    logging.info("==开始写scc_yaml目录==")
    gitlab_url = query_autoconfig_gitlab_url(application_gitlab_id, tag_name)
    if gitlab_url is not None and gitlab_url != "":
        local_path = gitlab_url.replace(GITLAB_URL, TEMP_DIR)
        # 拉取配置文件项目
        checkout_resource(gitlab_url, local_path, tag_name, False)
        # 遍历环境表，在scc_yaml目录下生成各个环境目录并写入order文件
        for env in automation_env_names:
            # fdev环境,可能为空
            scc_fdev_env = env["scc_fdev_env_name"][prod_type][net_work]
            if len(scc_fdev_env) == 0:
                logging.info("<strong style='color: red;'>" + env["env_name"] + "目录在" + prod_type + "变更" + net_work +
                             "网段SCC平台缺少fdev环境，跳过处理</strong>")
                continue
            # 拼接order内容
            # 获取order中需从实体中获取到参数
            get_other_order_params = query_env_params("", str(application_gitlab_id), "scc_yaml", scc_fdev_env)
            # scc的集群有两个的时候order里写两行
            scc_clusterlist = get_other_order_params["sccdeploy_clusterlist"]
            env_order_txt = ""
            if scc_clusterlist is None or scc_clusterlist == "":
                yaml_order_txt_new = yaml_order_txt + "%namespace:" + get_other_order_params[
                    "sccdeploy_namespace"] + "%cluster:%imagespacename:" + get_other_order_params[
                                         "dockerservice_namespace"]
                if type_name == "容器化项目":
                    yaml_order_txt_all = (yaml_order_txt_new + "%checkAndUp:0")
                else:
                    yaml_order_txt_all = (yaml_order_txt_new + "%port:8080%checkAndUp:1")
                env_order_txt = env_order_txt + yaml_order_txt_all + "\n"
            else:
                clusters = scc_clusterlist.split(",")
                for cluster in clusters:
                    yaml_order_txt_new = yaml_order_txt + "%namespace:" + get_other_order_params[
                        "sccdeploy_namespace"] + "%cluster:" + cluster + "%imagespacename:" + get_other_order_params[
                                             "dockerservice_namespace"]
                    if type_name == "容器化项目":
                        yaml_order_txt_all = (yaml_order_txt_new + "%checkAndUp:0")
                    else:
                        yaml_order_txt_all = (yaml_order_txt_new + "%port:8080%checkAndUp:1")
                    env_order_txt = env_order_txt + yaml_order_txt_all + "\n"
            order_dir = prod_dir + "/scc_yaml" + env["env_name"]
            scc_order = order_dir + '/order.txt'
            app_name_head = app_name_en.split("-")[0]
            # 拷贝下载到本地的auto-config目录下项目对应环境yaml文件 -> 临时目录的对应环境目录里
            yaml_file_dir = local_path + "/" + scc_fdev_env + "/" + app_name_head + "/"
            yaml_dir_create = False
            if os.path.exists(yaml_file_dir):
                yaml_file_list = os.listdir(yaml_file_dir)
                for yaml_file in yaml_file_list:
                    if os.path.isfile(yaml_file_dir + yaml_file) and yaml_file.endswith("-scc.yaml"):
                        yaml_dir_create = True
                        if not os.path.exists(order_dir):
                            os.makedirs(order_dir)
                        cmd_move_yaml_file = "cp " + yaml_file_dir + yaml_file + " " + order_dir
                        logging.info("拷贝文件 " + yaml_file_dir + yaml_file + "  -->  " + order_dir + "/" + yaml_file)
                        exception_resolver(commands.getstatusoutput(cmd_move_yaml_file))
            if yaml_dir_create:
                file_dir = yaml_service_path + "scc_yaml" + env["env_name"] + "/"
                if not os.path.exists(file_dir):
                    raise Exception("脚本文件目录错误：" + file_dir)
                script_file_list = os.listdir(file_dir)
                for script_file in script_file_list:
                    if os.path.isfile(file_dir + script_file) and script_file != "order.txt":
                        cmd_move_file = "cp " + file_dir + script_file + " " + order_dir
                        exception_resolver(commands.getstatusoutput(cmd_move_file))
                logging.info("order.txt:" + env_order_txt)
                f = open(scc_order, mode='a')
                f.write(env_order_txt)
                f.close()
            else:
                logging.info("<strong style='color: red;'>scc_yaml缺少" + env["env_name"] + "目录</strong>")
    if not os.path.exists(prod_dir + "/scc_yaml"):
        raise Exception("yaml文件未生成，请对当前应用【" + app_name_en + "】对应tag：" + tag_name + "重新发起pipeline")


def step5_app_check(prod_applications):
    logging.info("[+] 步骤五： 应用镜像检查 ")
    if len(prod_applications) == 0:
        logging.info("==本次变更无应用投产==")
        return
    logging.info("==应用直推镜像，无需检查包==")
    end_step()


def generate_app_scale(prod_id, prod_dir, image_deliver_type):
    """
    新增应用扩展order.txt
    :param image_deliver_type: 新版自动化发布判断
    :param prod_id:
    :param prod_dir:
    """
    app_scales = query_app_scale(prod_id)
    if len(app_scales) == 0:
        return
    logging.info("[+] 生成应用扩展order.txt ")
    scale_order_set = set()
    for app_scale in app_scales:
        automatic_production_service_path = ""
        if image_deliver_type == "1":
            automatic_production_service_path = DIR + "/service/"
            if app_scale["type_name"] == "容器化项目":
                automatic_production_service_path = DIR + "/container/"
        for env_scale in app_scale["env_scales"]:
            order_file_dir = prod_dir + "/docker_scale" + env_scale["env_name"]
            order_file_path = order_file_dir + "/order.txt"
            if (app_scale["pro_image_uri"] is None) or (app_scale["pro_image_uri"] == ""):
                raise Exception("弹性扩展参数异常，请将本变更下所有弹性扩展删除后重新添加！")
            if not os.path.exists(order_file_dir):
                os.makedirs(order_file_dir)
            script_file_dir = automatic_production_service_path + "docker_scale" + env_scale["env_name"] + "/"
            if not os.path.exists(script_file_dir):
                raise Exception("脚本文件目录错误：" + script_file_dir)
            script_file_list = os.listdir(script_file_dir)
            for script_file in script_file_list:
                if os.path.isfile(script_file_dir + script_file) and script_file != "order.txt":
                    cmd_move_file = "cp " + script_file_dir + script_file + " " + order_file_dir
                    exception_resolver(commands.getstatusoutput(cmd_move_file))
            if image_deliver_type == "1":
                order_txt = "tag:" + app_scale["tag_name"] + "%obname:" + app_scale["application_name_en"] \
                            + "%port:8080%replicasnu:" + env_scale["replicas"]
            else:
                order_txt = app_scale["tag_name"] + " " + app_scale["application_name_en"] + " 8080 " \
                            + env_scale["replicas"] + " " + app_scale["application_name_en"] + ".yml deployment " \
                            + app_scale["pro_image_uri"].split(":")[0].split("/")[2]
            if app_scale["type_name"] == "容器化项目":
                order_txt_new = (order_txt + "%checkAndUp:0")
            else:
                order_txt_new = (order_txt + "%checkAndUp:1")
            f = open(order_file_path, mode='a')
            f.write(order_txt_new + "\n")
            f.close()
            scale_order_set.add(order_file_path)
    logging.info("==应用扩展order.txt生成完毕,内容如下==")
    for file_path in scale_order_set:
        logging.info(file_path + ":")
        logging.info(get_log(file_path))
    end_step()


def write_docker_delete_txt(prod_dir):
    order_file_dir = prod_dir + "/docker_delete"
    order_file_path = order_file_dir + "/order.txt"
    if not os.path.exists(order_file_dir):
        os.makedirs(order_file_dir)
    f = open(order_file_path, mode='a')
    f.close()


def write_scc_delete_txt(prod_dir):
    order_file_dir = prod_dir + "/scc_delete"
    order_file_path = order_file_dir + "/order.txt"
    if not os.path.exists(order_file_dir):
        os.makedirs(order_file_dir)
    f = open(order_file_path, mode='a')
    f.close()


def generate_docker_delete(prod_dir, prod_applications):
    """
    新增清理投产文件order.txt
    :param prod_dir:变更根目录
    """
    logging.info("[+] 生成清理投产文件order.txt ")
    deploys = []
    for prod_application in prod_applications:
        deploys = deploys + prod_application["deploy_type"]
    deploy_type = set(deploys)
    if "CAAS" in deploy_type:
        write_docker_delete_txt(prod_dir)
    if "SCC" in deploy_type:
        write_scc_delete_txt(prod_dir)
    logging.info("==清理投产order.txt生成完毕==")
    end_step()


def save_media_dirs(prod_id, prod_dir):
    dir_array = []
    for root, dirs, files in os.walk(prod_dir):
        file_path = root.split(prod_dir)
        for file_dir in files:
            directory = os.path.join(file_path[1], file_dir).decode('utf-8')
            dir_array.append(directory)
    save_media_dir_data(prod_id, dir_array)


def step6_transfer(prod_detail, system_abbr, logging_file):
    logging.info("[+] 步骤五： 传送介质并打tar包 ")
    logging.info("==开始传送介质==")
    prod_assets_version = get_prod_assets_version(prod_detail)
    # prod_root_dir = TEMP_DIR + prod_assets_version
    prod_dir = TEMP_DIR + prod_assets_version + "/" + prod_detail["version"]
    remote_root_path = AUTORELEASE_SERVER_ROOT_DIR + "/" + system_abbr + "/" + prod_assets_version
    remote_prod_dir = remote_root_path + "/" + prod_detail["version"]
    # 保证远程系统缩写目录存在
    ssh_mkdir = "ssh " + AUTORELEASE_SERVER_USER + "@" + AUTORELEASE_SERVER_HOST + " \"mkdir -p " \
                + remote_root_path + "\""
    logging.info(ssh_mkdir)
    exec_cmd(ssh_mkdir, AUTORELEASE_SERVER_PASSWORD, logging_file)
    # 删除远程投产目录
    ssh_removedir = "ssh " + AUTORELEASE_SERVER_USER \
                    + "@" + AUTORELEASE_SERVER_HOST \
                    + " \"rm -rf " + remote_prod_dir + "\""
    logging.info(ssh_removedir)
    exec_cmd(ssh_removedir, AUTORELEASE_SERVER_PASSWORD, logging_file)

    newest_prod_detail = query_prod_detail(prod_id)
    if prod_detail["prod_assets_version"] != newest_prod_detail["prod_assets_version"]:
        raise Exception("当前变更变更说明已修改为" + newest_prod_detail["prod_assets_version"] + ",请重新准备介质！")
    # 传送介质
    scp_cmd = "scp -r " \
              + prod_dir + " " + AUTORELEASE_SERVER_USER + "@" + AUTORELEASE_SERVER_HOST + ":" + remote_root_path
    logging.info(scp_cmd)
    exec_cmd(scp_cmd, AUTORELEASE_SERVER_PASSWORD, logging_file)
    logging.info("==传送介质完毕==")
    logging.info("==开始打压缩包==")

    # 遍历本地介质目录下所有文件
    prod_root_dir = TEMP_DIR + prod_assets_version + "/"
    print "prod_assets_version"
    print prod_assets_version
    prod_assets_version_image_list = query_image_list_by_prod_assets_version(prod_assets_version)
    # 取自动化参数表中的准生产镜像仓库地址
    automation_param = query_automation_param_map()
    caas_ip = automation_param["autorelease.docker.push.registry"]
    if prod_assets_version_image_list is not None and len(prod_assets_version_image_list) != 0:
        image_list_url = prod_root_dir + "imagelist.txt"
        path_txt_url = prod_root_dir + "path.txt"
        path_list_url = prod_root_dir + "path.list"
        f = open(image_list_url, 'a')
        # 如果存在path.list文件就重命名为path.txt
        if os.path.isfile(path_list_url):
            os.rename(path_list_url, path_txt_url)
        p = open(path_txt_url, 'a')
        # 读取各文件中的直推镜像写入imagelist文件
        for image_url in prod_assets_version_image_list:
            if caas_ip in image_url:
                print "imagelist"
                print image_url
                f.write(image_url + "\n")
            else:
                print "pathlist"
                print image_url
                p.write(image_url.split("/", 1)[1] + "\n")
        f.close()
        p.close()
        # 将path.txt文件重命名为path.list
        os.rename(path_txt_url, path_list_url)
        # 打印imagelist内容
        if os.path.isfile(image_list_url) and os.path.getsize(image_list_url) > 0:
            logging.info("本次变更镜像列表文件已生成：" + image_list_url)
            logging.info('投产介质总目录生成imagelist内容：')
            f = io.open(image_list_url, mode="r", encoding="utf-8", errors='ignore')
            image_content = f.read()
            logging.info("\n" + image_content)
            f.close()
        # 传送imagelist.txt
        scp_imagelist = "scp " + image_list_url + " " + AUTORELEASE_SERVER_USER + "@" \
                        + AUTORELEASE_SERVER_HOST + ":" + remote_root_path
        logging.info(scp_imagelist)
        exec_cmd(scp_imagelist, AUTORELEASE_SERVER_PASSWORD, logging_file)
        # 传送path.list
        scp_pathlist = "scp " + path_list_url + " " + AUTORELEASE_SERVER_USER + "@" \
                       + AUTORELEASE_SERVER_HOST + ":" + remote_root_path
        exec_cmd(scp_pathlist, AUTORELEASE_SERVER_PASSWORD, logging_file)
    # 传送tar.sh
    scp_tar_script = "scp " + DIR + "/tar.sh " \
                     + AUTORELEASE_SERVER_USER + "@" + AUTORELEASE_SERVER_HOST + ":" + remote_root_path
    logging.info(scp_tar_script)
    exec_cmd(scp_tar_script, AUTORELEASE_SERVER_PASSWORD, logging_file)

    # 执行tar.sh
    ssh_tar = "ssh " + AUTORELEASE_SERVER_USER \
              + "@" + AUTORELEASE_SERVER_HOST \
              + " \"chmod +x " + remote_root_path + "/tar.sh && cd " + remote_root_path + " && " \
              + remote_root_path + "/" + "tar.sh " \
              + prod_detail["version"] \
              + " " + AUTORELEASE_SERVER_USER + "\""
    logging.info(ssh_tar)
    exec_cmd(ssh_tar, AUTORELEASE_SERVER_PASSWORD, logging_file)
    logging.info("删除临时目录：" + prod_dir)
    shutil.rmtree(prod_dir)
    logging.info("==压缩包准备完毕==")
    end_step()


def step7_check_image(prod):
    check_push_image_and_auto_release(prod)


def generate_check(prod_dir, scc_flag):
    if scc_flag == '1':
        order_file_dir = prod_dir + "/check"
        if not os.path.exists(order_file_dir):
            os.makedirs(order_file_dir)


def auto_release(prod_id, scc_flag):
    prod_detail = query_prod_detail(prod_id)
    if prod_detail is None:
        raise Exception("变更不存在:" + prod_id)
    release_node_name = prod_detail["release_node_name"]
    prod_assets_version = get_prod_assets_version(prod_detail)

    log_path = "/fdev/log/frelease/" + FDEV_ENV + "/autorelease/" + release_node_name + "/"
    if not os.path.exists(log_path):
        os.makedirs(log_path)
    logging_file = log_path + prod_detail["version"] + "-autorelease-" + \
                   time.strftime("%Y%m%d-%H:%M:%S", time.localtime()) + ".log"
    print "日志路径：" + logging_file
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s : %(levelname)s : %(message)s',
        filename=logging_file
    )

    logging.info("******************************************************************")
    logging.info("  开始准备介质：变更id： " + prod_id)
    logging.info("  投产窗口：" + release_node_name)
    logging.info("  总介质版本：" + prod_assets_version)
    logging.info("  变更版本：" + prod_detail["version"])
    logging.info("  变更单号：" + prod_detail["prod_spdb_no"])
    logging.info("  Fdev当前环境：" + FDEV_ENV)
    logging.info("  自动化发布服务器：" + AUTORELEASE_SERVER_HOST)
    logging.info("  本地日志路径：" + logging_file)
    tip = ""
    if scc_flag == "1":
        tip = "支持scc变更"
    else:
        tip = "不支持scc变更"
    logging.info("  scc变更标识:" + tip)
    logging.info("******************************************************************")

    prod_applications = query_prod_applications(prod_id)
    prod_assets = query_prod_assets(prod_id)
    prod_dir = TEMP_DIR + prod_assets_version + "/" + prod_detail["version"]

    update_thread = threading.Thread(
        target=update_log, kwargs={"logging": logging, "prod_id": prod_id, "logging_file": logging_file})
    update_thread.setDaemon(True)
    update_thread.start()

    try:
        update_autorelease_stage(prod_id, "0")
        # 第一步：新建变更目录
        update_autorelease_stage(prod_id, "1")
        step1_makedir(prod_dir)
        # 步骤二： 检出配置文件
        update_autorelease_stage(prod_id, "2")
        step2_config_files(prod_dir, prod_detail, prod_assets, logging_file)
        # 步骤三： 检查配置
        update_autorelease_stage(prod_id, "3")
        step3_config_check(prod_dir, prod_assets, prod_detail)
        # 步骤四： 准备镜像脚本
        update_autorelease_stage(prod_id, "4")
        release_node_name = prod_detail["release_node_name"]
        version = prod_detail["version"]
        # #  查询在此版本前所有镜像标签
        prod_images = query_before_pord_images(release_node_name, version)
        image_deliver_type = prod_detail["image_deliver_type"]
        prod_type = prod_detail["type"]
        step4_app_images(prod_dir, prod_applications, prod_images, image_deliver_type, prod_id, prod_type, prod_detail,
                         scc_flag)
        # 步骤五： 应用镜像检查
        # update_autorelease_stage(prod_id, "5")
        # step5_app_check(prod_applications)
        # 应用扩展文件 docker stop目录
        if scc_flag == "1":
            generate_app_scale_all(prod_id, prod_dir, image_deliver_type, prod_type)
        else:
            generate_app_scale(prod_id, prod_dir, image_deliver_type)
        # 清理投产文件 docker_delete目录
        generate_docker_delete(prod_dir, prod_applications)
        # sccdce模板出介质加check目录
        # 在AOMS自动化变更时会触发检查变更的内容是否在当前允许范围内
        generate_check(prod_dir, scc_flag)
        # 介质目录保存
        save_media_dirs(prod_id, prod_dir)
        # 步骤五： 传送介质
        update_autorelease_stage(prod_id, "5")
        step6_transfer(prod_detail, query_group_system_abbr(prod_detail["owner_groupId"]), logging_file)
        update_autorelease_stage(prod_id, "6")
        # 步骤六：检查镜像推送
        step7_check_image(prod_detail)
    except Exception, e:
        update_prod_status(prod_id, "4")
        logging.error(e)
        logging.exception(e)
        raise e
    finally:
        update_autorelease_log(logging, prod_id, get_log(logging_file))


def generate_app_scale_all(prod_id, prod_dir, image_deliver_type, prod_type):
    """
    新增应用扩展order.txt
    :param image_deliver_type: 新版自动化发布判断
    :param prod_id:
    :param prod_dir:
    :param prod_type: 变更类型
    :param scc_flag: 变更标识 1-scc变更 0-非scc
    """

    app_scales = query_app_scale(prod_id)
    if len(app_scales) == 0:
        return
    logging.info("[+] 生成应用扩展order.txt ")
    scale_order_set = set()
    automation_env_names = query_automation_env()
    for app_scale in app_scales:
        if "CAAS" in app_scale["deploy_type"]:
            write_docker_scale_order_txt(image_deliver_type, app_scale, prod_dir, scale_order_set, prod_type,
                                         automation_env_names)
        if "SCC" in app_scale["deploy_type"]:
            write_scc_scale_order_txt(image_deliver_type, app_scale, prod_dir, scale_order_set, prod_type,
                                      automation_env_names)
    logging.info("==应用扩展order.txt生成完毕,内容如下==")
    for file_path in scale_order_set:
        logging.info(file_path + ":")
        logging.info(get_log(file_path))
    end_step()


def write_docker_scale_order_txt(image_deliver_type, app_scale, prod_dir, scale_order_set, prod_type,
                                 automation_env_names):
    automatic_production_service_path = ""
    if image_deliver_type == "1":
        automatic_production_service_path = DIR + "/container_scc/"
    for env_scale in app_scale["env_scales"]:
        # 根据环境目录去环境表获取fdev环境
        for auto_env in automation_env_names:
            if auto_env["env_name"] == env_scale["env_name"]:
                fdev_env = auto_env["fdev_env_name"][prod_type][app_scale["network"]]
                break
        # 获取order中需从实体中获取到参数
        get_other_order_params = query_env_params(app_scale["application_id"], "", "docker_scale", ''.join(fdev_env))
        if image_deliver_type == "1":
            order_txt = "obname:" + app_scale["application_name_en"] \
                        + "%replicasnu:" + env_scale["replicas"] \
                        + "%namespace:" + get_other_order_params["fdev_caas_tenant"]
            if app_scale["type_name"] == "容器化项目":
                order_txt_new = (order_txt + "%checkAndUp:0")
            else:
                order_txt_new = (order_txt + "%port:8080%checkAndUp:1")
        order_file_dir = prod_dir + "/docker_scale" + env_scale["env_name"]
        order_file_path = order_file_dir + "/order.txt"
        if (app_scale["pro_image_uri"] is None) or (app_scale["pro_image_uri"] == ""):
            raise Exception("弹性扩展参数异常，请将本变更下所有弹性扩展删除后重新添加！")
        if not os.path.exists(order_file_dir):
            os.makedirs(order_file_dir)
        script_file_dir = automatic_production_service_path + "docker_scale" + env_scale["env_name"] + "/"
        if not os.path.exists(script_file_dir):
            raise Exception("脚本文件目录错误：" + script_file_dir)
        script_file_list = os.listdir(script_file_dir)
        for script_file in script_file_list:
            if os.path.isfile(script_file_dir + script_file) and script_file != "order.txt":
                cmd_move_file = "cp " + script_file_dir + script_file + " " + order_file_dir
                exception_resolver(commands.getstatusoutput(cmd_move_file))
        f = open(order_file_path, mode='a')
        f.write(order_txt_new + "\n")
        f.close()
        scale_order_set.add(order_file_path)


def write_scc_scale_order_txt(image_deliver_type, app_scale, prod_dir, scale_order_set, prod_type,
                              automation_env_names):
    logging.info("==开始写scc弹性扩展==")
    automatic_production_service_path = ""
    if image_deliver_type == "1":
        automatic_production_service_path = DIR + "/container_scc/"
    for env_scale in app_scale["env_scales"]:
        scc_replicas = int(env_scale["scc_replicas"])
        logging.info(env_scale["name"] + "目录副本数为：" + str(scc_replicas))
        order_file_dir = prod_dir + "/scc_scale" + env_scale["env_name"]
        order_file_path = order_file_dir + "/order.txt"
        if (app_scale["pro_scc_image_uri"] is None) or (app_scale["pro_scc_image_uri"] == ""):
            raise Exception("弹性扩展参数异常，请将本变更下所有弹性扩展删除后重新添加！")
        if not os.path.exists(order_file_dir):
            os.makedirs(order_file_dir)
        script_file_dir = automatic_production_service_path + "scc_scale" + env_scale["env_name"] + "/"
        if not os.path.exists(script_file_dir):
            raise Exception("脚本文件目录错误：" + script_file_dir)
        script_file_list = os.listdir(script_file_dir)
        for script_file in script_file_list:
            if os.path.isfile(script_file_dir + script_file) and script_file != "order.txt":
                cmd_move_file = "cp " + script_file_dir + script_file + " " + order_file_dir
                exception_resolver(commands.getstatusoutput(cmd_move_file))
        if image_deliver_type == "1":
            common_order_txt = "obname:" + app_scale["application_name_en"]
            if app_scale["type_name"] == "容器化项目":
                common_order_txt = (common_order_txt + "%checkAndUp:0")
            else:
                common_order_txt = (common_order_txt + "%port:8080%checkAndUp:1")
            # 根据弹性扩展的环境目录去环境表获取fdev环境
            namespace = ""
            cluster = ""
            for auto_env in automation_env_names:
                if auto_env["env_name"] == env_scale["env_name"]:
                    # scc 环境PROC/K1、PROC/K2目录对应的fdev环境是List<String>，灰度环境可能为空
                    fdev_env = auto_env["scc_fdev_env_name"][prod_type][app_scale["network"]]
                    break
            if fdev_env is not None:
                # 获取order中需从实体中获取到参数
                get_other_order_params = query_env_params(app_scale["application_id"], "", "scc_scale", fdev_env)
                namespace = get_other_order_params["sccdeploy_namespace"]
                cluster = get_other_order_params["sccdeploy_clusterlist"]
            common_order_txt = common_order_txt + "%namespace:" + namespace
            # 副本数不为0，看集群是否是双集群，若是，则需均分
            clusterlist = cluster.split(",")
            while '' in clusterlist:
                clusterlist.remove('')
            if clusterlist:
                x = split_integer(scc_replicas, len(clusterlist))
                cnt = 0
                for cluster in clusterlist:
                    order_txt = common_order_txt + "%cluster:" + cluster + "%replicasnu:" + str(x[cnt])
                    cnt = cnt + 1
                    f = open(order_file_path, mode='a')
                    f.write(order_txt + "\n")
                    f.close()

            else:
                order_txt = common_order_txt + "%cluster:" + cluster + "%replicasnu:" + str(scc_replicas)
                f = open(order_file_path, mode='a')
                f.write(order_txt + "\n")
                f.close()
        scale_order_set.add(order_file_path)


def checkout_resource(resource_giturl, repo_local_path, branch, normal_branch):
    """
    检出对应系统的配置文件gitlab存放项目
    :param normal_branch: 是否为正常分支，非tag分支
    :param branch: 介质所在分支
    :param resource_giturl:
    :param repo_local_path:
    """
    if os.path.exists(repo_local_path + "/.git"):
        # logging.info("已检出本系统配置文件gitlab项目，开始pull：" + repo_local_path)
        try:
            git_repo = git.Repo.init(repo_local_path)
            if normal_branch:
                git_remote = git_repo.remote()
                git_remote.pull()
                if branch != str(git_repo.active_branch):
                    logging.info("当前分支:" + str(git_repo.active_branch) + ",切换到分支:" + branch)
                    git_repo.git.checkout(branch, force=True)
            else:
                # tag分支删除原有目录重新clone并切换分支
                if os.path.exists(repo_local_path):
                    shutil.rmtree(repo_local_path)
                logging.info("分支为tag，删除目录，重新clone")
                git_repo = git.Repo.clone_from(url=add_git_token(resource_giturl) + ".git", to_path=repo_local_path)
                if branch != "master":
                    git_repo.git.checkout(branch, force=True)
        except Exception as e:
            logging.error(e.message)
            logging.info("更新失败，重新clone项目")
            shutil.rmtree(repo_local_path)
            git_repo = git.Repo.clone_from(url=add_git_token(resource_giturl) + ".git", to_path=repo_local_path)
            if branch != "master":
                logging.info("当前分支:" + str(git_repo.active_branch) + ",切换到分支:" + branch)
                git_repo.git.checkout(branch, force=True)
                # logging.info("已检出本系统配置文件gitlab项目，pull完毕：" + repo_local_path)
    else:
        logging.info("尚未检出本系统配置文件gitlab项目，开始检出：" + repo_local_path)
        git_repo = git.Repo.clone_from(url=add_git_token(resource_giturl) + ".git", to_path=repo_local_path)
        if branch != "master":
            git_repo.git.checkout(branch, force=True)
        logging.info("本系统配置文件gitlab项目检出完毕：" + repo_local_path)


def add_git_token(origin_url):
    return origin_url.replace("http://", "http://Authorization:" + GITLAB_TOKEN + "@")


def end_step():
    logging.info("============================================================")


def get_log(logging_file):
    f = io.open(logging_file, mode="r", encoding="utf-8", errors='ignore')
    # f = open(logging_file)
    log_content = f.read()
    return log_content


def update_log(logging, prod_id, logging_file):
    """
    定时更新自动化发布日志
    :param prod_id:变更id
    :param logging_file:
    """
    print "update log:" + logging_file
    update_autorelease_log(logging, prod_id, get_log(logging_file))
    schedule = sched.scheduler(time.time, time.sleep)
    schedule.enter(5, 0, update_log, (logging, prod_id, logging_file))
    schedule.run()


def get_db_assets(prod_assets):
    """
    将数据库中文件列表转为"目录名/环境名/文件名列表"格式
    :param prod_assets:
    :return:
    """
    catalog_files = {}
    for catalog_assets in prod_assets:
        catalog_name = catalog_assets["catalog_name"]
        for tmp_file in catalog_assets["assets"]:
            key = catalog_name + "/" + tmp_file["fileName"]
            file_giturl = tmp_file["file_giturl"]
            runtime_env = tmp_file["runtime_env"]
            if (runtime_env is not None) and (runtime_env != ""):
                file_name = tmp_file["fileName"]
                key = catalog_name + "/" + tmp_file["runtime_env"] + "/" + file_name
                # 文件名包含环境名时，文件地址需截取环境名后全路径
                if file_giturl.find(runtime_env) >= 0:
                    key = catalog_name + "/" + file_giturl[file_giturl.index(runtime_env):]
                if tmp_file["source"] == "3":
                    file_fullpath = file_giturl.split("/blob/")[1]
                    fdev_file_path = file_fullpath[file_fullpath.index("/") + 1:]
                    md5_file_head = fdev_file_path[:fdev_file_path.index("/")]
                    if md5_file_head == "cli-md5":
                        key = catalog_name + "/" + runtime_env + "/" + fdev_file_path
                    else:
                        key = catalog_name + "/" + runtime_env + "/" + \
                              fdev_file_path[fdev_file_path.index("/") + 1:]

            catalog_files[key] = ""
    return catalog_files


def commonconfig_files(prod_assets):
    """
    获取数据中所有公共配置文件列表
    :param prod_assets:
    :return:
    """
    common_configs = {}
    for catalog_assets in prod_assets:
        catalog_type = catalog_assets["catalog_type"]
        if catalog_type == "5":
            catalog_name = catalog_assets["catalog_name"]
            env_files = {}
            for tmp_file in catalog_assets["assets"]:
                files = env_files.get(tmp_file["runtime_env"])
                if files is None:
                    files = []
                files.append(tmp_file["fileName"])
                env_files[tmp_file["runtime_env"]] = files
            common_configs[catalog_name] = env_files
    return common_configs


def get_prod_assets_version(prod_detail):
    prod_assets_version = prod_detail["prod_assets_version"]
    if (prod_assets_version is None) or (prod_assets_version == ""):
        prod_assets_version = prod_detail["version"] + "_" + prod_detail["prod_spdb_no"]
    return prod_assets_version


if __name__ == '__main__':
    prod_id = sys.argv[1]
    scc_flag = sys.argv[2]
    auto_release(prod_id, scc_flag)
