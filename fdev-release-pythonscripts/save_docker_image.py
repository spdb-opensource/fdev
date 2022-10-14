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


def tag_image(logging, release_node_name, prod_id, application_id, prod_dir, release_type, image_url, app_name_en,
              prod_images, prod_type, automation_env_names, net_work, service_path, application_gitlab_id,
              yaml_service_path, docker_dirs, change, type_name):
    """
    直推投产验证仓库
    :param logging:
    :param release_node_name:投产窗口名称
    :param prod_id 变更id
    :param application_id: 应用编号
    :param prod_dir:介质根路径
    :param prod_images:
    :param prod_type: 变更类型
    :param release_type: 应用类型，用于区分根路径
    :param image_url: 镜像标签
    :param app_name_en: 应用英文名
    :param automation_env_names: 自动化发布环境集合
    :param net_work: 所属网段
    :param service_path: 自动化脚本目录
    :param new_add_sign: 1：新增应用
    :param application_gitlab_id: gitlab id
    :param yaml_service_path: yaml配置文件地址
    :param docker_dirs: 变更应用目录集合
    :param change: change
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
        contents = ""
        for content in docker_dirs:
            contents = contents + " " + contents
            if content == "docker_yaml":
                yaml_order_txt = "tag:" + image_url.split(':')[1] + "%obname:" + app_name_en \
                                 + "%port:8080%obyml:" + app_name_en + ".yaml"
                auto_release.write_docker_yaml_txt(logging, prod_dir, yaml_order_txt, application_gitlab_id,
                                                   image_url.split(':')[1], automation_env_names, prod_type,
                                                   net_work, app_name_en, yaml_service_path, change, type_name)
            else:
                order_txt = "tag:" + image_url.split(':')[1] + "%obname:" + app_name_en + "%port:8080"
                write_order_txt(prod_dir + "/" + content, order_txt, automation_env_names,
                                service_path + content + "/", change, content, logging, type_name)
        logging.info(app_name_en + contents)
    except (IOError, OSError, Exception) as e:
        logging.error(e)
        raise Exception("镜像获取失败")
    return product_image_url_list


# 选了新模板scc模板写各个环境目录的文件
def tag_image_all(logging, release_node_name, prod_id, application_id, prod_dir, release_type, image_url, app_name_en,
                  prod_images, prod_type, automation_env_names, net_work, service_path, application_gitlab_id,
                  yaml_service_path, docker_dirs, change, type_name, esf_flag, tag, caas_stop_env, scc_stop_env):
    """
    直推投产验证仓库
    :param logging:
    :param release_node_name:投产窗口名称
    :param prod_id 变更id
    :param application_id: 应用编号
    :param prod_dir:介质根路径
    :param prod_images:
    :param prod_type: 变更类型
    :param release_type: 应用类型，用于区分根路径
    :param image_url: 镜像标签
    :param app_name_en: 应用英文名
    :param automation_env_names: 自动化发布环境集合
    :param net_work: 所属网段
    :param service_path: 自动化脚本目录
    :param new_add_sign: 1：新增应用
    :param application_gitlab_id: gitlab id
    :param yaml_service_path: yaml配置文件地址
    :param docker_dirs: 变更应用目录集合（包含scc目录）
    :param change: change
    :param type_name: 应用类型
    :return:
    """
    logging.info("应用id:" + application_id)
    logging.info("变更类型:" + prod_type)
    logging.info("网段:" + net_work)
    logging.info("gitlabId:" + str(application_gitlab_id))
    logging.info("应用类型:" + type_name)
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
        # esf的标识
        tag_name = image_url.split(':')[1]
        if esf_flag == "1":
            tag_name = tag
        # 准备镜像脚本文件
        contents = ""
        for content in docker_dirs:
            contents = contents + " " + contents
            if content == "docker_yaml":
                yaml_order_txt = "obname:" + app_name_en + "%imagename:" + app_name_en + "%tag:" + \
                                 image_url.split(':')[1] + "%obyml:" + app_name_en + ".yaml"
                auto_release.write_docker_yaml_txt_new(logging, prod_dir, yaml_order_txt, application_gitlab_id,
                                                       tag_name, automation_env_names, prod_type,
                                                       net_work, app_name_en, yaml_service_path, change, type_name)
            elif content == "scc_yaml":
                yaml_order_txt = "obname:" + app_name_en + "%imagename:" + app_name_en + "%tag:" + \
                                 image_url.split(':')[1] + "%obyml:" + app_name_en + "-scc.yaml"
                auto_release.write_scc_yaml_txt(logging, prod_dir, yaml_order_txt, application_gitlab_id, tag_name,
                                                automation_env_names, prod_type, net_work, app_name_en,
                                                yaml_service_path, type_name)
            elif content == "docker_stop":
                order_txt = "obname:" + app_name_en
                write_docker_stop_txt(content, application_gitlab_id, prod_dir + "/" + content, order_txt,
                                      automation_env_names, service_path + content + "/", prod_type, net_work,
                                      type_name, caas_stop_env, logging)
            elif content == "scc_stop":
                order_txt = "obname:" + app_name_en
                write_scc_stop_txt(content, application_gitlab_id, prod_dir + "/" + content, order_txt,
                                   automation_env_names, service_path + content + "/", prod_type, net_work,
                                   scc_stop_env, logging)
            else:
                order_txt = "obname:" + app_name_en
                if content == "docker" or content.split("_")[0] == "docker":
                    write_docker_order_txt(content, application_gitlab_id, prod_dir + "/" + content, order_txt,
                                           automation_env_names, service_path + content + "/", prod_type, net_work,
                                           image_url.split(':')[1], app_name_en, type_name, change, logging)
                if content == "scc" or content.split("_")[0] == "scc":
                    write_scc_order_txt(content, application_gitlab_id, prod_dir + "/" + content, order_txt,
                                        automation_env_names, service_path + content + "/", prod_type, net_work,
                                        image_url.split(':')[1], app_name_en, type_name, change, logging)

    except (IOError, OSError, Exception) as e:
        logging.error(e)
        raise Exception("镜像获取失败")
    return product_image_url_list


# 非scc变更写order文件的方法
def write_order_txt(image_dir, order_txt, automation_env_names, origin_dir, change, content, logging, type_name):
    for env in automation_env_names:
        if type_name == "容器化项目":
            docker_order_txt = (order_txt + "%checkAndUp:0")
        else:
            if content == "docker_all" or content == "docker_stopall":
                docker_order_txt = (order_txt + "%checkAndUp:0")
            else:
                docker_order_txt = (order_txt + "%checkAndUp:1")
        if (content == "docker_startall") and (change is not None):
            env_content = []
            for item, value in change.items():
                env_content.append(item)
                if item == env["env_name"]:
                    for orderKey, orderValue in value.items():
                        if orderKey == "replicas":
                            docker_order_txt = docker_order_txt + "%replicasnu:" + str(orderValue)
                            break
            # 从蓝鲸获取不到的PROC/SHK1 等环境，副本数默认给0
            if env["env_name"] not in env_content:
                docker_order_txt = docker_order_txt + "%replicasnu:0"
        order_dir = image_dir + env["env_name"]
        docker_order = order_dir + '/order.txt'
        if not os.path.exists(order_dir):
            os.makedirs(order_dir)
        file_dir = origin_dir + env["env_name"] + "/"
        if not os.path.exists(file_dir):
            raise Exception("脚本文件目录错误：" + file_dir)
        script_file_list = os.listdir(file_dir)
        for script_file in script_file_list:
            if os.path.isfile(file_dir + script_file) and script_file != "order.txt":
                cmd_move_file = "cp " + file_dir + script_file + " " + order_dir
                logging.info(cmd_move_file)
                exception_resolver(commands.getstatusoutput(cmd_move_file))
        f = open(docker_order, mode='a')
        f.write(docker_order_txt + "\n")
        f.close()


# scc变更写docker开头目录下的order文件方法
def write_docker_order_txt(content, application_gitlab_id, image_dir, order_txt, automation_env_names, origin_dir,
                           prod_type, net_work, tag, app_name_en, type_name, change, logging):
    for env in automation_env_names:
        # fdev环境
        fdev_env = env["fdev_env_name"][prod_type][net_work]
        # 获取order中需从实体中获取到参数
        get_other_order_params = query_env_params("", str(application_gitlab_id), content, ''.join(fdev_env))
        # 拼以docker开头不同目录下order.txt内容
        if content == "docker":
            order_txt_new = (
                order_txt + "%namespace:" + get_other_order_params["fdev_caas_tenant"] + "%imagename:" +
                app_name_en + "%imagespacename:" + get_other_order_params["fdev_caas_registry_namespace"] +
                "%tag:" + tag + "%imageuser:" + get_other_order_params["fdev_caas_registry_user"])
            if type_name == "容器化项目":
                order_txt_all = (order_txt_new + "%checkAndUp:0")
            else:
                order_txt_all = (order_txt_new + "%port:8080%checkAndUp:1")
        if content == "docker_all":
            order_txt_new = (
                order_txt + "%namespace:" + get_other_order_params["fdev_caas_tenant"] + "%imagename:" +
                app_name_en + "%imagespacename:" + get_other_order_params["fdev_caas_registry_namespace"] +
                "%tag:" + tag + "%imageuser:" + get_other_order_params["fdev_caas_registry_user"])
            if type_name == "容器化项目":
                order_txt_all = (order_txt_new + "%checkAndUp:0")
            else:
                order_txt_all = (order_txt_new + "%port:8080%checkAndUp:0")
        if content == "docker_startall":
            order_txt_new = (order_txt + "%namespace:" + get_other_order_params["fdev_caas_tenant"])
            for item, value in change.items():
                if item == env["env_name"]:
                    for orderKey, orderValue in value.items():
                        if orderKey == "replicas":
                            order_txt_new = order_txt_new + "%replicasnu:" + str(orderValue)
                            break
            if type_name == "容器化项目":
                order_txt_all = (order_txt_new + "%checkAndUp:0")
            else:
                order_txt_all = (order_txt_new + "%port:8080%checkAndUp:1")
        if content == "docker_stopall":
            order_txt_new = (order_txt + "%namespace:" + get_other_order_params["fdev_caas_tenant"])
            if type_name == "容器化项目":
                order_txt_all = (order_txt_new + "%checkAndUp:0")
            else:
                order_txt_all = (order_txt_new + "%checkAndUp:0")
        if content == "docker_restart":
            order_txt_new = (order_txt + "%namespace:" + get_other_order_params["fdev_caas_tenant"])
            if type_name == "容器化项目":
                order_txt_all = (order_txt_new + "%checkAndUp:0")
            else:
                order_txt_all = (order_txt_new + "%port:8080%checkAndUp:1")
        order_dir = image_dir + env["env_name"]
        docker_order = order_dir + '/order.txt'
        if not os.path.exists(order_dir):
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
        f.write(order_txt_all + "\n")
        f.close()


# scc变更写scc开头目录下的order文件
def write_scc_order_txt(content, application_gitlab_id, image_dir, order_txt, automation_env_names, origin_dir,
                        prod_type, net_work, tag, app_name_en, type_name, change, logging):
    logging.info("==开始写scc开头的非yaml目录==")
    for env in automation_env_names:
        # fdev环境，类型是List<String>,可能是空，比如PROC/K1 灰度变更网银网段暂时是空
        fdev_env = env["scc_fdev_env_name"][prod_type][net_work]
        logging.info("fdev_env: " + fdev_env)
        # scc灰度环境暂时没有fdev环境，不报错跳过处理
        if fdev_env is None or fdev_env == "":
            logging.info("<strong style='color: red;'>" + env["env_name"] + "目录在" + prod_type + "变更" + net_work +
                         "网段SCC平台缺少fdev环境，跳过处理</strong>")
            continue
        order_dir = image_dir + env["env_name"]
        docker_order = order_dir + '/order.txt'
        if not os.path.exists(order_dir):
            os.makedirs(order_dir)
        file_dir = origin_dir + env["env_name"] + "/"
        if not os.path.exists(file_dir):
            raise Exception("脚本文件目录错误：" + file_dir)
        script_file_list = os.listdir(file_dir)
        for script_file in script_file_list:
            if os.path.isfile(file_dir + script_file) and script_file != "order.txt":
                cmd_move_file = "cp " + file_dir + script_file + " " + order_dir
                exception_resolver(commands.getstatusoutput(cmd_move_file))
        # 获取order中需从实体中获取到参数
        get_other_order_params = query_env_params("", str(application_gitlab_id), content, fdev_env)
        # order.txt公共部分 begin
        common_order_txt = order_txt + "%namespace:" + get_other_order_params["sccdeploy_namespace"]
        if content == "scc" or content == "scc_all":
            common_order_txt = common_order_txt + "%imagename:" + app_name_en + "%imagespacename:" + \
                               get_other_order_params["dockerservice_namespace"] + "%tag:" + tag
        if type_name == "容器化项目":
            common_order_txt = common_order_txt + "%checkAndUp:0"
        else:
            if content == "scc_all" or content == "scc_stopall":
                common_order_txt = common_order_txt + "%checkAndUp:0"
            else:
                common_order_txt = common_order_txt + "%checkAndUp:1"
            if content != "scc_stopall":
                common_order_txt = common_order_txt + "%port:8080"
        logging.info("common_order_txt: " + common_order_txt)
        # order.txt公共部分 begin
        scc_clusterlist = get_other_order_params["sccdeploy_clusterlist"]
        logging.info("scc_clusterlist: " + scc_clusterlist)
        # scc_startall目录的副本数从数据库prod_application表中的change字段获取
        clusterlist = scc_clusterlist.split(",")
        while '' in clusterlist:
            clusterlist.remove('')
        logging.info(len(clusterlist))
        # 写入集群
        all_order_txt = ""
        if clusterlist:
            flag = False
            cnt = 0
            for cluster in clusterlist:
                env_order_txt = common_order_txt + "%cluster:" + cluster
                if content == "scc_startall":
                    replicas = int()
                    for item, value in change.items():
                        if item == env["env_name"]:
                            for orderKey, orderValue in value.items():
                                if orderKey == "scc_replicas":
                                    replicas = int(orderValue)
                                    flag = True
                                    break
                            logging.info("scc_startall目录副本数：" + str(replicas))
                            # 将副本数replicas均分成len(clusterlist)等分
                            x = split_integer(replicas, len(clusterlist))
                if flag:
                    env_order_txt = env_order_txt + "%replicasnu:" + str(x[cnt])
                    cnt = cnt + 1
                all_order_txt = all_order_txt + env_order_txt + "\n"
        else:
            all_order_txt = common_order_txt + "%cluster:"
            if content == "scc_startall":
                for item, value in change.items():
                    if item == env["env_name"]:
                        for orderKey, orderValue in value.items():
                            if orderKey == "scc_replicas":
                                all_order_txt = all_order_txt + "%replicasnu:" + str(orderValue)
                                break
            all_order_txt = all_order_txt + "\n"
        f = open(docker_order, mode='a')
        f.write(all_order_txt)
        f.close()


# sccdce模板写docker_stop介质目录
def write_docker_stop_txt(content, application_gitlab_id, image_dir, order_txt, automation_env_names, origin_dir,
                          prod_type, net_work, type_name, caas_stop_env, logging):
    logging.info("=======write docker_stop start=========")
    for env in automation_env_names:
        # fdev环境
        fdev_env = env["fdev_env_name"][prod_type][net_work]
        # 获取order中需从实体中获取到参数
        get_other_order_params = query_env_params("", str(application_gitlab_id), content, ''.join(fdev_env))
        # 拼以docker开头不同目录下order.txt内容
        order_txt_new = (order_txt + "%namespace:" + get_other_order_params["fdev_caas_tenant"])
        if type_name == "容器化项目":
            order_txt_all = (order_txt_new + "%checkAndUp:0")
        else:
            order_txt_all = (order_txt_new + "%checkAndUp:0")
        order_dir = image_dir
        temp_env = env["env_name"].split("/")  # SHK1/SHK2/HFK1/HFK2
        if temp_env[1] == "PROC" and temp_env[2] not in caas_stop_env:
            continue
        # 创建介质目录并写order.txt
        order_dir = order_dir + env["env_name"]
        docker_order = order_dir + '/order.txt'
        if not os.path.exists(order_dir):
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
        f.write(order_txt_all + "\n")
        f.close()
    logging.info("=======write docker_stop end=========")


# sccdce模板写scc_stop介质目录
def write_scc_stop_txt(content, application_gitlab_id, image_dir, order_txt, automation_env_names, origin_dir,
                       prod_type, net_work, scc_stop_env, logging):
    logging.info("=======write scc_stop start=========")
    for env in automation_env_names:
        # PROC下的目录根据用户选择需要停止的集群
        env_array = env["env_name"].split("/")
        if env_array[1] == "PROC" and env_array[2] not in scc_stop_env:
            continue
        # fdev环境，类型是List<String>,可能是空，比如PROC/K1 灰度变更网银网段暂时是空
        fdev_env = env["scc_fdev_env_name"][prod_type][net_work]
        logging.info("fdev_env: " + fdev_env)
        # scc灰度环境暂时没有fdev环境，不报错跳过处理
        if fdev_env is None or fdev_env == "":
            logging.info("<strong style='color: red;'>" + env["env_name"] + "目录在" + prod_type + "变更" + net_work +
                         "网段SCC平台缺少fdev环境，跳过处理</strong>")
            continue
        order_dir = image_dir + env["env_name"]
        docker_order = order_dir + '/order.txt'
        if not os.path.exists(order_dir):
            os.makedirs(order_dir)
        file_dir = origin_dir + env["env_name"] + "/"
        if not os.path.exists(file_dir):
            raise Exception("脚本文件目录错误：" + file_dir)
        script_file_list = os.listdir(file_dir)
        for script_file in script_file_list:
            if os.path.isfile(file_dir + script_file) and script_file != "order.txt":
                cmd_move_file = "cp " + file_dir + script_file + " " + order_dir
                exception_resolver(commands.getstatusoutput(cmd_move_file))
        # 获取order中需从实体中获取到参数
        get_other_order_params = query_env_params("", str(application_gitlab_id), content, fdev_env)
        # order.txt公共部分 begin
        common_order_txt = order_txt + "%namespace:" + get_other_order_params[
            "sccdeploy_namespace"] + "%checkAndUp:0"
        logging.info("common_order_txt: " + common_order_txt)
        # order.txt公共部分 begin
        scc_clusterlist = get_other_order_params["sccdeploy_clusterlist"]
        logging.info("scc_clusterlist: " + scc_clusterlist)
        clusterlist = scc_clusterlist.split(",")
        while '' in clusterlist:
            clusterlist.remove('')
        logging.info(len(clusterlist))
        all_order_txt = ""
        # 写入集群
        if clusterlist:
            for cluster in clusterlist:
                order_txt_new = common_order_txt + "%cluster:" + cluster
                all_order_txt = all_order_txt + order_txt_new + "\n"
        else:
            all_order_txt = common_order_txt + "%cluster:" + "\n"
        f = open(docker_order, mode='a')
        f.write(all_order_txt)
        f.close()
    logging.info("=======write scc_stop end=========")


# 拆分整数
def split_integer(m, n):
    assert n > 0
    quotient = int(m / n)
    remainder = m % n
    if remainder > 0:
        return [quotient] * (n - remainder) + [quotient + 1] * remainder
    if remainder < 0:
        return [quotient - 1] * -remainder + [quotient] * (n + remainder)

    return [quotient] * n


def exception_resolver(cmd_result):
    if cmd_result[0] != 0:
        raise Exception(cmd_result[1])
