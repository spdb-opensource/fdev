#!/usr/bin/env python
# -*- coding: utf-8 -*-

import commands
from fdev_requests import *
import os

import auto_release
import cache_image


def save_image(logging, image_dir, image_url, app_name_en, prod_images, application_gitlab_id):
    """
    保存应用镜像
    :param logging:
    :param prod_images:
    :param image_dir: 保存镜像路径
    :param image_url: 镜像标签
    :param app_name_en: 应用名称
    :param application_gitlab_id:
    :return:
    """
    try:
        if prod_images is not None and prod_images.get(image_url) is not None:
            logging.info("此版本应用镜像已在变更版本" + prod_images.get(image_url) +
                         "上线，无需重新打包")
        else:
            if image_url.find(":") < 0:
                raise Exception("镜像标签地址格式不正确，请确认:" + image_url)
            image_info = image_url.split(":")
            image_tar_name = app_name_en + ".tar"
            image_tar_gz_name = app_name_en + ".tar.gz"
            tag_name = image_info[len(image_info) - 1]
            cache_dir = auto_release.CACHE_IMAGE_DIR + app_name_en + "/" + tag_name
            image_tar_path = cache_dir + "/" + image_tar_name
            image_tar_gz_path = cache_dir + "/" + image_tar_gz_name
            logging.info("==开始检测应用镜像是否存在缓存==" + image_tar_gz_path)
            if not os.path.exists(image_tar_gz_path):
                logging.info("==应用" + app_name_en + "缓存不存在,缓存镜像==")
                cache_image.cache_image(logging, image_url, tag_name, app_name_en, application_gitlab_id)
            if os.path.exists(image_tar_path):
                logging.info("==应用" + app_name_en + "镜像缓存不完整,重新缓存镜像==")
                cache_image.cache_image(logging, image_url, tag_name, app_name_en, application_gitlab_id)
            cache_image.get_cache_image(logging, image_dir, image_tar_gz_path, image_url, app_name_en, prod_images,
                                        application_gitlab_id)
        f = open(image_dir + '/order.txt', mode='a')
        f.write(image_url + " " + app_name_en + "\n")
        f.close()
    except (IOError, OSError, Exception) as e:
        logging.error(e)
        raise Exception("镜像获取失败")


def tag_image(logging, release_node_name, prod_id, application_id, prod_dir, release_type, image_url,
              app_name_en, prod_images, image_deliver_type, automation_env_names, container_num,
              service_path, new_add_sign):
    """
    直推投产验证仓库
    :param logging:
    :param release_node_name:投产窗口名称
    :param prod_id 变更id
    :param application_id: 应用编号
    :param prod_dir:介质根路径
    :param prod_images:
    :param image_deliver_type
    :param release_type: 应用类型，用于区分根路径
    :param image_url: 镜像标签
    :param app_name_en: 应用英文名
    :param automation_env_names: 自动化发布环境集合
    :param container_num: 容器数量
    :param service_path: 自动化脚本目录
    :param new_add_sign: 1：新增应用
    :return:
    """
    product_image_url_list = []
    try:
        docker_flag = True
        if release_type == "3":
            docker_flag = query_docker_directory(release_node_name, application_id, image_url, prod_id)
        if prod_images is not None and prod_images.get(image_url) is not None:
            logging.info("此版本应用镜像已在变更版本" + prod_images.get(image_url) +
                         "上线，无需再次推送镜像")
        elif not docker_flag:
            logging.info("停止服务后启动,镜像标签为历史版本,无需推送镜像")
        elif release_type == "4":
            logging.info("重启服务无需推送镜像")
        else:
            if image_url.find(":") < 0:
                raise Exception("镜像标签地址格式不正确，请确认:" + image_url)
            # 从环境配置接口获取镜像空间名称与用户名
            push_image_list = query_push_image_uri(release_node_name, prod_id, application_id)
            for push_image_map in push_image_list:
                product_image_url_list.append(push_image_map["image_uri"])
        # 准备镜像脚本文件
        if new_add_sign != "1":
            if (container_num is None) or (container_num == ""):
                container_num = "9999"
            if image_deliver_type == "1":
                order_txt = "tag:" + image_url.split(':')[1] + "%obname:" + app_name_en + "%port:8080%replicasnu:" \
                            + container_num
            else:
                order_txt = image_url.split(':')[1] + " " + app_name_en + " 8080 " + container_num + " " + app_name_en \
                            + ".yml deployment " + image_url.split(":")[0].split("/")[2]
            if (release_type is None) or (release_type == "") or (release_type == "1"):
                logging.info(app_name_en + "  docker")
                write_order_txt(prod_dir + "/docker", order_txt, automation_env_names, service_path + "docker/",
                                logging)
            elif release_type == "2":
                logging.info(app_name_en + "  docker_all")
                write_order_txt(prod_dir + "/docker_all", order_txt, automation_env_names,
                                service_path + "docker_all/", logging)
            elif release_type == "3":
                if docker_flag:
                    logging.info(app_name_en + "  docker_stopall  docker  docker_startall")
                    write_order_txt(prod_dir + "/docker", order_txt, automation_env_names, service_path + "docker/",
                                    logging)
                else:
                    logging.info(app_name_en + "  docker_stopall  docker_startall")
                write_order_txt(prod_dir + "/docker_stopall", order_txt, automation_env_names,
                                service_path + "docker_stopall/", logging)
                write_order_txt(prod_dir + "/docker_startall", order_txt, automation_env_names,
                                service_path + "docker_startall/", logging)
            elif release_type == "4":
                logging.info(app_name_en + "  docker_restart")
                write_order_txt(prod_dir + "/docker_restart", order_txt, automation_env_names,
                                service_path + "docker_restart/", logging)
            logging.info("order.txt:" + order_txt)
    except (IOError, OSError, Exception) as e:
        logging.error(e)
        raise Exception("镜像获取失败")
    return product_image_url_list


def write_order_txt(image_dir, order_txt, automation_env_names, origin_dir, logging):
    for env in automation_env_names:
        order_dir = image_dir + env["env_name"]
        docker_order = order_dir + '/order.txt'
        if not os.path.exists(order_dir):
            logging.info("创建目录:" + order_dir)
            os.makedirs(order_dir)
        file_dir = origin_dir + env["env_name"] + "/"
        if not os.path.exists(file_dir):
            raise Exception("脚本文件目录错误：" + file_dir)
        script_file_list = os.listdir(file_dir)
        for script_file in script_file_list:
            if os.path.isfile(file_dir + script_file) and script_file != "order.txt":
                cmd_move_file = "cp " + file_dir + script_file + " " + order_dir
                exception_resolver(commands.getstatusoutput(cmd_move_file))
        f = open(docker_order, mode='a')
        f.write(order_txt + "\n")
        f.close()


def exception_resolver(cmd_result):
    if cmd_result[0] != 0:
        raise Exception(cmd_result[1])

