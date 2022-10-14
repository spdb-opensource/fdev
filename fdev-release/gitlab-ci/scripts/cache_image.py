#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import commands
import logging
import sys
import shutil

import datetime

import auto_release
import save_docker_image
from fdev_requests import query_rel_env
from fdev_requests import query_image_user_password


def cache_image(logging, image_url, prod_tag, application_name, application_gitlab_id):
    """
    缓存应用镜像
    :param logging:
    :param image_url: 镜像标签
    :param prod_tag:
    :param application_name: 应用名称
    :param application_gitlab_id:gitlab
    :return:
    """
    logger_dir = auto_release.CACHE_IMAGE_DIR + "log/"
    if not os.path.exists(logger_dir):
        os.makedirs(logger_dir)
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s : %(levelname)s : %(message)s',
        filename=logger_dir + "cache-image." + str(datetime.date.today()) + ".log"
    )
    logging.info("应用名称：" + application_name)
    logging.info("镜像标签：" + image_url)
    retry_times = 0
    while retry_times < 3:
        try:
            full_dir = auto_release.CACHE_IMAGE_DIR + application_name
            if os.path.exists(full_dir):
                logging.info("清空缓存目录：" + full_dir)
                shutil.rmtree(full_dir)
            save_dir = full_dir + "/" + prod_tag
            print(save_dir)
            os.makedirs(save_dir)
            tar_gz_name = application_name + ".tar.gz"
            save_image_to(logging, save_dir, image_url, application_name, application_gitlab_id)
            logging.info("镜像包md5：" + get_md5(save_dir + "/" + tar_gz_name))
            logging.info("镜像缓存完毕:" + image_url)
            break
        except (IOError, OSError, Exception) as e:
            logging.error("缓存镜像失败:" + image_url)
            logging.exception(e)
            retry_times = retry_times + 1
            logging.info("第" + str(retry_times) + "次尝试缓存失败:" + image_url)
            logging.info("------------------------------")
            if retry_times == 3:
                raise Exception("缓存镜像失败:" + image_url)
            continue
    logging.info("==================================")


def get_cache_image(logging, image_dir, image_tar_gz_path, image_url, application_name, prod_images,
                    application_gitlab_id):
    """
    取出缓存应用镜像
    :param image_dir: 镜像目录
    :param application_name: 应用名称
    :param image_tar_gz_path: 缓存镜像路径
    :param application_gitlab_id:gitlab
    :return:
    """

    try:
        if prod_images is not None and prod_images.get(image_url) is not None:
            logging.info("此版本应用镜像已在变更版本" + prod_images.get(image_url) +
                         "上线，无需重新打包")
        else:
            logging.info("应用名称：" + application_name)
            logging.info("镜像标签：" + image_url)
            logging.info("镜像目录：" + image_dir)
            logging.info("==应用" + application_name + "缓存存在开始进行复制==")
            logging.info("cp " + image_tar_gz_path + " " + image_dir)
            os.system("cp " + image_tar_gz_path + " " + image_dir)
            logging.info("==应用" + application_name + "缓存复制完毕==")
            target_file_path = image_dir + "/" + application_name + ".tar.gz"
            if not check_md5(logging, image_tar_gz_path, target_file_path):
                logging.info("镜像md5校验失败,重新从仓库拉取镜像!")
                os.remove(target_file_path)
                save_docker_image.save_image(logging, image_dir, image_url, application_name, prod_images,
                                             application_gitlab_id)
    except (IOError, OSError, Exception) as e:
        logging.error(e)
        raise Exception("获取缓存镜像失败")


def save_image_to(logging, save_dir, image_url, application_name, application_gitlab_id):
    docker_reposity_user = auto_release.DOCKER_REPOSITORY_USER
    docker_reposity_passwd = auto_release.DOCKER_REPOSITORY_PASSWORD
    env = query_rel_env(application_gitlab_id, image_url.split(":")[1])
    auto_param_user_pawd = query_image_user_password(application_gitlab_id, env)
    if auto_param_user_pawd is not None and auto_param_user_pawd != "":
        docker_reposity_user = auto_param_user_pawd["FDEV_CAAS_REGISTRY_USER"]
        docker_reposity_passwd = auto_param_user_pawd["FDEV_CAAS_REGISTRY_PASSWORD"]
    else:
        logging.info("gitlab_id:" + str(application_gitlab_id) + " env:" + env + "对应的镜像空间用户名密码获取失败，使用默认值")
    cmd_login = 'docker login ' + image_url.split('/')[0] \
                + " -u " + docker_reposity_user
    cmd_pull = 'docker pull ' + image_url
    tar_name = application_name + '.tar'
    cmd_save = 'docker save ' + image_url + " -o " + save_dir + "/" + tar_name
    cmd_gzip = 'gzip ' + save_dir + "/" + tar_name
    tar_gz_name = application_name + '.tar.gz'
    cmd_load = 'docker load -i ' + save_dir + "/" + tar_gz_name
    logging.info(cmd_login)
    exception_resolver(commands.getstatusoutput(cmd_login + " -p " + docker_reposity_passwd))
    if image_url.find(":") < 0:
        raise Exception("镜像标签地址格式不正确，请确认:" + image_url)
    image_info = image_url.split(":")
    cmd_find_image = "docker images | grep '" + image_info[0] + " ' | grep '" + image_info[1] + "'"
    logging.info(cmd_find_image)
    find_result = commands.getoutput(cmd_find_image)
    if (find_result is None) or (find_result == ""):
        logging.info("本地不存在此镜像，从镜像仓库拉取镜像:" + image_url)
        logging.info(cmd_pull)
        status = commands.getstatusoutput(cmd_pull)
        exception_resolver(status)
    else:
        logging.info("本地已存在此镜像，直接保存压缩")
    logging.info(cmd_save)
    exception_resolver(commands.getstatusoutput(cmd_save))
    logging.info(cmd_gzip)
    exception_resolver(commands.getstatusoutput(cmd_gzip))
    logging.info(cmd_load)
    exception_resolver(commands.getstatusoutput(cmd_load))


def check_md5(logging, src_file, target_file):
    if os.path.isfile(src_file) and os.path.isfile(target_file):
        src_md5 = get_md5(src_file)
        target_md5 = get_md5(target_file)
        if src_md5 == target_md5:
            logging.info("md5校验一致:" + src_md5)
            return True
    return False


def get_md5(target_file_path):
    return commands.getoutput("md5sum " + target_file_path + " | cut -d ' ' -f1")


def exception_resolver(cmd_result):
    if cmd_result[0] != 0:
        raise Exception(cmd_result[1])


if __name__ == '__main__':
    image_url = sys.argv[1]
    prod_tag = sys.argv[2]
    application_name = sys.argv[3]
