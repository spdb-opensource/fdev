#!/usr/bin/env python
# -*- coding: utf-8 -*-

import commands
from fdev_requests import *
from save_docker_image import exception_resolver
import io
import os
import logging


# docker镜像仓库用户名
DOCKER_REPOSITORY_USER = 'ebank'
# docker镜像仓库密码
DOCKER_REPOSITORY_PASSWORD = 'xxx'
# 通过自动化配置表获取配置
automation_param = query_automation_param_map()
# 直推投产环境ip
CAAS_REGISTRY = automation_param["autorelease.docker.push.registry"]
# faketime镜像推送空间名称
FAKE_CASS_REGISTRY_NAMESPACE = automation_param["fake.cass.registry.namespace"]


def push_image(app_gitlab_id, app_name_en, image_url, fake_image_url, release_node_name, prod_id, application_id):
    # 从环境配置接口获取镜像空间名称与用户名
    push_image_list = query_push_image_uri(release_node_name, prod_id, application_id)
    # 从配置文件获取直推镜像ip
    fdev_caas_registry = CAAS_REGISTRY
    docker_reposity_user = DOCKER_REPOSITORY_USER
    docker_reposity_passwd = DOCKER_REPOSITORY_PASSWORD
    env = query_rel_env(app_gitlab_id, image_url.split(":")[1])
    auto_param_user_pawd = query_image_user_password(app_gitlab_id, env)
    # 返回值不为空且包含的用户名密码不为空
    if auto_param_user_pawd is not None and auto_param_user_pawd != "" \
            and auto_param_user_pawd["FDEV_CAAS_REGISTRY_USER"] is not None \
            and auto_param_user_pawd["FDEV_CAAS_REGISTRY_USER"] != "" \
            and auto_param_user_pawd["FDEV_CAAS_REGISTRY_PASSWORD"] is not None \
            and auto_param_user_pawd["FDEV_CAAS_REGISTRY_PASSWORD"] != "":
        docker_reposity_user = auto_param_user_pawd["FDEV_CAAS_REGISTRY_USER"]
        docker_reposity_passwd = auto_param_user_pawd["FDEV_CAAS_REGISTRY_PASSWORD"]
    else:
        logging.info("gitlab_id:" + str(app_gitlab_id) + " env:" + env + "对应的镜像空间用户名密码获取失败，使用默认值")
    cmd_login = 'docker login ' + image_url.split('/')[0] + " -u " + docker_reposity_user
    try:
        logging.info(cmd_login)
        exception_resolver(commands.getstatusoutput(cmd_login + " -p " + docker_reposity_passwd))
    except (IOError, OSError, Exception):
        raise Exception("镜像获取失败，请对应用【" + app_name_en + "】以" + image_url.split(':')[1] + "重新打包，更新"
                        + image_url.split('/')[0] + "/" + image_url.split('/')[1] + "对应的用户名密码")
    cmd_pull = 'docker pull ' + image_url
    logging.info(cmd_pull)
    try:
        exception_resolver(commands.getstatusoutput(cmd_pull))
    except(IOError, OSError, Exception) as pull_image:
        logging.info(image_url + "镜像获取失败")
        raise pull_image
    for image_map in push_image_list:
        update_push_image_status(logging, image_map["id"], "1", image_map["image_uri"] + "镜像推送中")
        try:
            fdev_caas_registry_namespace = image_map["namaspace"]
            fdev_caas_registry_user = image_map["namaspace"]
            # 镜像空间密码为用户名+'@spdb'
            fdev_caas_registry_password = image_map["namaspace"] + '@spdb'
            logging.info("镜像空间名称：" + fdev_caas_registry_namespace)
            product_image_url = image_map["image_uri"]
            cmd_tag = 'docker tag ' + image_url + ' ' + product_image_url
            cmd_product_login = 'docker login ' + fdev_caas_registry + " -u " + fdev_caas_registry_user
            cmd_push = 'docker push ' + product_image_url
            logging.info(cmd_tag)
            exception_resolver(commands.getstatusoutput(cmd_tag))
            logging.info(cmd_product_login)
            exception_resolver(commands.getstatusoutput(cmd_product_login + " -p " + fdev_caas_registry_password))
            logging.info(cmd_push)
            exception_resolver(commands.getstatusoutput(cmd_push))
            update_push_image_status(logging, image_map["id"], "2", image_map["image_uri"] + "镜像推送成功")
        except(IOError, OSError, Exception) as image_exception:
            logging.info(image_map["image_uri"] + "推送失败")
            raise Exception(image_exception)
    # 拉取并推送faketime镜像
    if fake_image_url is not None and fake_image_url != "":
        logging.info("推送faketime镜像")
        fake_caas_registry_namespace = FAKE_CASS_REGISTRY_NAMESPACE
        fake_caas_registry_user = fake_caas_registry_namespace
        fake_caas_registry_password = fake_caas_registry_user + "@spdb"
        fake_product_image_url = fdev_caas_registry + '/' + fake_caas_registry_namespace + "/" \
            + fake_image_url.split('/')[2]
        fake_cmd_login = 'docker login ' + fake_image_url.split('/')[0] + " -u " + docker_reposity_user
        try:
            logging.info(fake_cmd_login)
            exception_resolver(commands.getstatusoutput(fake_cmd_login + " -p " + docker_reposity_passwd))
        except (IOError, OSError, Exception):
            raise Exception("镜像获取失败，请对应用【" + app_name_en + "】以" + image_url.split(':')[1] + "重新打包，更新"
                            + image_url.split('/')[0] + "/" + image_url.split('/')[1] + "对应的用户名密码")
        fake_cmd_pull = 'docker pull ' + fake_image_url
        logging.info(fake_cmd_pull)
        exception_resolver(commands.getstatusoutput(fake_cmd_pull))
        fake_cmd_tag = 'docker tag ' + fake_image_url + ' ' + fake_product_image_url
        fake_cmd_product_login = 'docker login ' + fdev_caas_registry + " -u " + fake_caas_registry_user
        fake_cmd_push = 'docker push ' + fake_product_image_url

        logging.info(fake_cmd_tag)
        exception_resolver(commands.getstatusoutput(fake_cmd_tag))
        logging.info(fake_cmd_product_login)
        exception_resolver(commands.getstatusoutput(fake_cmd_product_login + " -p " + fake_caas_registry_password))
        logging.info(fake_cmd_push)
        exception_resolver(commands.getstatusoutput(fake_cmd_push))


def get_log(log_file):
    f = io.open(log_file, mode="r", encoding="utf-8", errors='ignore')
    log_content = f.read()
    return log_content


if __name__ == '__main__':
    gitlab_id = int(sys.argv[1])
    name_en = sys.argv[2]
    image_uri = sys.argv[3]
    fake_image_uri = sys.argv[4]
    prod = sys.argv[5]
    application = sys.argv[6]
    log_path = sys.argv[7]
    log_file_name = sys.argv[8]
    logging_file = log_path + log_file_name
    if not os.path.exists(log_path):
        os.makedirs(log_path)
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s : %(levelname)s : %(message)s',
        filename=logging_file
    )

    prod_detail = query_prod_detail(prod)
    if (prod_detail["status"] == "4" and prod_detail["auto_release_stage"] == "6") or \
            (prod_detail["status"] == "3" and prod_detail["auto_release_stage"] == "7"):
        # 异步更新查询镜像推送的日志
        update_autorelease_stage(prod, "6")
        update_prod_status(prod, "1")
    try:
        push_image(gitlab_id, name_en, image_uri, fake_image_uri, prod_detail["release_node_name"], prod, application)
    except(IOError, OSError, Exception) as e:
        logging.info(e)
        update_by_prod_application(prod, application, "3", get_log(logging_file))
        raise Exception("镜像推送失败")
    finally:
        check_push_image_and_auto_release(prod)
