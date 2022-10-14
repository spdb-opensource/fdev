#!/usr/bin/env python
# -*- coding: utf-8 -*-
import threading

import io

from save_docker_image import *
from cache_image import *
from pexpect_cmd import *
import logging
import time
import sched


FDEV_ENV = os.environ.get("CI_ENVIRONMENT_SLUG")
DIR = os.environ.get("CI_PROJECT_DIR")
SCRIPTS_PATH = os.path.dirname(__file__) + "/"

# 通过自动化配置表获取配置
automation_param = query_automation_param_map()
# 本地准备介质临时目录
TEMP_DIR = automation_param["autorelease.tmp.dir"]
# 自动化发布服务器ip
AUTORELEASE_SERVER_HOST = automation_param["autorelease.server.host"]
# 自动化发布服务器用户
AUTORELEASE_SERVER_USER = automation_param["autorelease.server.user"]
# 自动化发布服务器密码
AUTORELEASE_SERVER_PASSWORD = automation_param["autorelease.server.password"]
# 自动化发布服务器介质根目录
AUTORELEASE_SERVER_ROOT_DIR = automation_param["autorelease.server.root.dir"]


def step1_makedir(prod_dir):
    logging.info("[+] 步骤一： 新建变更目录 ")
    logging.info("本次变更目录: " + prod_dir)
    if os.path.exists(prod_dir):
        logging.info("重新准备介质，删除原目录: " + prod_dir)
        shutil.rmtree(prod_dir)
    else:
        os.makedirs(prod_dir)
    end_step()


def step2_config_files(prod_dir, prod_assets):
    """
    准备配置文件
    :param prod_dir: 变更目录
    :param prod_assets:  变更介质列表
    :return:
    """
    logging.info("[+] 步骤二： 检出配置文件 ")
    if len(prod_assets) == 0:
        logging.info("==本次变更无配置文件投产==")
        return
    logging.info("==开始准备配置文件==")
    for prod_asset in prod_assets:
        for asset in prod_asset["assets"]:
            list = asset["file_giturl"].split("/")
            catalog_name = ("/" + asset["asset_catalog_name"]) if (asset["asset_catalog_name"] is not None and asset["asset_catalog_name"]!="") else ""
            local = prod_dir + catalog_name + "/" + list[len(list) - 1]
            local_dir = prod_dir + catalog_name + "/"
            logging.info("  获取文件至本地： " + local)
            if not os.path.exists(local_dir):
                os.makedirs(local_dir)
            download_minio_file(local, asset["file_giturl"], "fdev-release")
    end_step()


def step3_config_check():
    logging.info("==非自动化发布无需检查配置文件==")
    end_step()


def save_media_dirs(prod_id, prod_dir):
    dir_array = []
    for root, dirs, files in os.walk(prod_dir):
        file_path = root.split(prod_dir)
        for file_dir in files:
            directory = os.path.join(file_path[1], file_dir).decode('utf-8')
            dir_array.append(directory)
    save_media_dir_data(prod_id, dir_array)


def step6_transfer(prod_detail, logging_file):
    logging.info("[+] 步骤五： 传送介质并打tar包 ")
    logging.info("==开始传送介质==")
    prod_assets_version = get_prod_assets_version(prod_detail)
    prod_dir = TEMP_DIR + prod_assets_version + "/" + prod_detail["version"]
    remote_root_path = AUTORELEASE_SERVER_ROOT_DIR + "/fdev/" + prod_detail["release_node_name"] + "/" + prod_assets_version
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
        raise Exception("当前变更变更说明已修改为" + newest_prod_detail["prod_assets_version"] + ",请重新准备介质！" )
    # 传送介质
    scp_cmd = "scp -r " \
              + prod_dir + " " + AUTORELEASE_SERVER_USER + "@" + AUTORELEASE_SERVER_HOST + ":" + remote_root_path
    logging.info(scp_cmd)
    exec_cmd(scp_cmd, AUTORELEASE_SERVER_PASSWORD, logging_file)
    logging.info("==传送介质完毕==")
    logging.info("==开始打压缩包==")

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


# 从MinIO获取
def step4_app_package(prod_dir, prod_applications):
    for prod_application in prod_applications:
        if prod_application["pro_package_uri"] is not None:
            list = prod_application["pro_package_uri"].split("/")
            local = prod_dir + '/' + list[len(list)-1]
            logging.info("  获取文件至本地： " + local)
            download_minio_file(local, prod_application["pro_package_uri"], "fdev-release")
        else:
            logging.info(" 不涉及应用包 : " + prod_application["application_id"])


def de_auto_release(prod_id):
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
    logging.info("******************************************************************")

    prod_applications = query_prod_applications(prod_id)
    prod_assets = query_prod_assets_by_prod_id(prod_id)
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
        step2_config_files(prod_dir, prod_assets)
        # 步骤三： 检查配置
        update_autorelease_stage(prod_id, "3")
        step3_config_check()
        # 步骤四： 准备镜像脚本
        update_autorelease_stage(prod_id, "4")
        # 拉取应用包
        step4_app_package(prod_dir, prod_applications)
        # 介质目录保存
        save_media_dirs(prod_id, prod_dir)
        # 步骤五： 传送介质
        update_autorelease_stage(prod_id, "5")
        step6_transfer(prod_detail, logging_file)
        # update_autorelease_stage(prod_id, "6")
        # 步骤六：修改变更状态
        check_de_auto_release(prod_id)
    except Exception, e:
        update_prod_status(prod_id, "4")
        logging.error(e)
        logging.exception(e)
        raise e
    finally:
        update_autorelease_log(logging, prod_id, get_log(logging_file))


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


def get_prod_assets_version(prod_detail):
    prod_assets_version = prod_detail["prod_assets_version"]
    if (prod_assets_version is None) or (prod_assets_version == ""):
        prod_assets_version = prod_detail["version"] + "_" + prod_detail["prod_spdb_no"]
    return prod_assets_version


if __name__ == '__main__':
    prod_id = sys.argv[1]
    de_auto_release(prod_id)
