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
import random

import urllib2

FDEV_ENV = os.environ.get("CI_ENVIRONMENT_SLUG")
SCRIPTS_PATH = os.path.dirname(__file__) + "/"

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


def step2_config_files(prod_dir, prod_detail, prod_assets):
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
    for catalog_assets in prod_assets:
        catalog_name = catalog_assets["catalog_name"]
        catalog_type = catalog_assets["catalog_type"]
        ordered_assets = {}
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
                if catalog_type == "5":
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
                if not os.path.exists(file_target_dir):
                    os.makedirs(file_target_dir)
                logging.info("拷贝文件 " + file_source_path + "  -->  " + file_target_path)
                output = commands.getoutput("cp " + file_source_path + " " + file_target_path)
                if output != "":
                    logging.info(output)
            elif prod_asset["source"] == "1":
                download_path = prod_dir + prod_asset["file_giturl"].split(prod_detail["version"])[1]
                download_dir = download_path[:download_path.index(file_name)]
                if not os.path.exists(download_dir):
                    os.makedirs(download_dir)
                download_minio_file(download_path, prod_asset["file_giturl"], "fdev-release")
            seq_no = prod_asset["seq_no"]
            if (seq_no is not None) and (seq_no != ""):
                ordered_assets[seq_no] = file_name
        # 生成order.txt文件
        ordered_size = len(ordered_assets)
        if ordered_size != 0:
            order_txt_path = prod_dir + "/" + catalog_name + "/" + "order.txt"
            logging.info("开始生成order.txt文件:" + order_txt_path)
            f = open(order_txt_path, mode="a")
            for num in range(1, ordered_size + 1):
                ordered_file = ordered_assets.get(str(num))
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
            exception_msg = "[" + db_file_key + "]数据库存在，准备介质中不存在"
            logging.info(exception_msg)
            raise Exception(exception_msg)
    for local_file_key in local_files.keys():
        if local_file_key not in db_files.keys():
            exception_msg = "[" + local_file_key + "]准备介质中存在,数据库不存在"
            logging.info(exception_msg)
            raise Exception(exception_msg)
    logging.info("==检查各环境公共配置文件是否一致==")
    commonconfig_file = commonconfig_files(prod_assets)
    for commonconfig_items in commonconfig_file.items():
        logging.info("检查目录：" + commonconfig_items[0])
        commonconfigs = commonconfig_items[1]
        size = 0
        tmp_set = set()
        for env_files in commonconfigs.items():
            env = env_files[0]
            files = env_files[1]
            logging.info(env_files[0] + ": " + str(files))
            if env == "PROCHF":
                continue
            if (size != 0) and (size != len(files)):
                raise Exception(commonconfig_items[0] + "/" + env + "目录文件数与其他环境不一致,请检查")
            size = len(files)
            if (len(tmp_set) != 0) and (tmp_set != set(files)):
                raise Exception(commonconfig_items[0] + "/" + env + "目录文件内容与其他环境不一致,请检查")
            tmp_set = set(files)
    logging.info("==各环境公共配置文件一致==")
    logging.info("==已记录变更文件与准备变更文件一致==")
    end_step()


def step4_app_images(prod_dir, prod_applications, prod_images, image_deliver_type, prod_id, prod_type, prod_detail,
                     automatic_production_root_path):
    logging.info("[+] 步骤四： 准备镜像脚本")
    if len(prod_applications) == 0:
        logging.info("==本次变更无应用投产==")
        return
    logging.info("==开始应用打包==")
    release_node_name = prod_detail["release_node_name"]
    prod_version = prod_detail["version"]
    env_tar_path = build_pro_dat_tar(logging, prod_id, prod_type, release_node_name + "_" + prod_version,
                                     prod_applications)
    # 返回的tar包名字不为空
    if env_tar_path is not None:
        #遍历Map集合
       for applicationName_en , applicationUrl in dict.items(env_tar_path):
        # tar包真实地址不存在
        if not os.path.isfile(applicationUrl):
            raise Exception(applicationUrl + "路由介质未生成")
        dat_tar_dir = prod_dir + "/config/"+applicationName_en
        if not os.path.exists(dat_tar_dir):
            os.makedirs(dat_tar_dir)
        logging.info("生成路由介质目录:" + applicationUrl)
        cmd_move_tar = "cp " + applicationUrl + " " + dat_tar_dir
        logging.info(cmd_move_tar)
        exception_resolver(commands.getstatusoutput(cmd_move_tar))
        #修改文件名
        repoFiledir,repoFileName = os.path.split(applicationUrl)
        old_name_dat_tar_dir = os.path.join(dat_tar_dir,repoFileName)
        new_name_dat_tar_dir = os.path.join(dat_tar_dir,"repo.json")
        os.rename(old_name_dat_tar_dir , new_name_dat_tar_dir)
    else:
        logging.info("本次变更不生成路由介质")
    automation_env_names = query_automation_env(prod_type)
    i = 0
    product_image_url_all_list = []
    for prod_application in prod_applications:
        i = i + 1
        application_id = prod_application["application_id"]
        application_gitlab_id = prod_application["gitlab_project_id"]
        release_type = prod_application["release_type"]
        container_num = prod_application["container_num"]
        app_name_en = prod_application["app_name_en"]
        app_name_zh = prod_application["app_name_zh"]
        pro_image_uri = prod_application["pro_image_uri"]
        new_add_sign = prod_application["new_add_sign"]
        net_work = prod_application["network"]
        fdev_config_changed = prod_application["fdev_config_changed"]
        fdev_config_confirm = prod_application["fdev_config_confirm"]
        logging.info(str(i) + ". " + app_name_en + "(" + app_name_zh + ")")
        if (pro_image_uri is None) or (pro_image_uri == ""):
            logging.info("应用[" + app_name_en + "]尚未选择镜像版本")
            raise Exception("应用[" + app_name_en + "]尚未选择镜像版本")
        yaml_service_path = "/fdev/scripts/service/"
        if image_deliver_type == "1":
            automatic_production_service_path = "/fdev/scripts/service/"
            logging.info("应用类型：" + prod_application["type_name"])
            if prod_application["type_name"] == "容器化项目":
                automatic_production_service_path = "/fdev/scripts/container/"
                yaml_service_path = "/fdev/scripts/container/"
        else:
            automatic_production_service_path = automatic_production_root_path + "/k8s_sercvice/"
            logging.info("应用类型：" + prod_application["type_name"])
            if prod_application["type_name"] == "容器化项目":
                automatic_production_service_path = automatic_production_root_path + "/k8s_container/"
        if fdev_config_changed is not None and fdev_config_changed and fdev_config_confirm != "1":
            logging.info("应用[" + app_name_en + "]配置文件有变化，请审核")
            raise Exception("应用[" + app_name_en + "]配置文件有变化，请审核")
        logging.info("脚本目录为：" + automatic_production_service_path)
        if image_deliver_type == "1":
            # 新增应用准备docker_yaml目录
            if new_add_sign == "1":
                logging.info("应用[" + app_name_en + "]为新增应用或从未准备过介质，生成docker_yaml目录")
                yaml_order_txt = "tag:" + pro_image_uri.split(':')[1] + "%obname:" + app_name_en \
                                 + "%port:8080%replicasnu:" + container_num + "%obyml:" + app_name_en \
                                 + ".yaml%checkandup:0"
                write_docker_yaml_txt(logging, prod_dir, yaml_order_txt, application_gitlab_id,
                                      pro_image_uri.split(':')[1], automation_env_names, prod_type, net_work,
                                      app_name_en, yaml_service_path)
        product_image_url_list = tag_image(logging, release_node_name, prod_id, application_id, prod_dir, release_type,
                                           pro_image_uri, app_name_en, prod_images, image_deliver_type,
                                           automation_env_names, container_num, automatic_production_service_path,
                                           new_add_sign)
        # product_image_url_all_list = list(set(product_image_url_all_list).union(set(product_image_url_list)))
        if len(product_image_url_list) != 0:
            for product_image_url in product_image_url_list:
                product_image_url_all_list.append(product_image_url)
        update_application(prod_id, application_id, '1')
    if len(product_image_url_all_list) != 0:
        prod_assets_version = get_prod_assets_version(prod_detail)
        imagelist_txt = TEMP_DIR + prod_assets_version + "/" + prod_detail["version"] + ".txt"
        f = open(imagelist_txt, mode='w')
        # f.truncate()
        for img_url in product_image_url_all_list:
            f.write(img_url + "\n")
        f.close()
        if os.path.isfile(imagelist_txt) and os.path.getsize(imagelist_txt) > 0:
            logging.info("本次变更镜像列表文件已生成：" + imagelist_txt)
            logging.info('直推投产验证仓库镜像地址：')
            f = io.open(imagelist_txt, mode="r", encoding="utf-8", errors='ignore')
            log_content = f.read()
            logging.info("\n" + log_content)
            f.close()
        else:
            raise Exception("imagelist文件生成异常, 请重新发起介质准备")
    logging.info("==应用打包完毕==")
    end_step()


def write_docker_yaml_txt(logging, prod_dir, yaml_order_txt, application_gitlab_id, tag_name, automation_env_names,
                          prod_type, net_work, app_name_en, yaml_service_path):
    gitlab_url = query_autoconfig_gitlab_url(application_gitlab_id, tag_name)
    if gitlab_url is not None and gitlab_url != "":
        local_path = gitlab_url.replace(GITLAB_URL, TEMP_DIR)
        # 拉取配置文件项目
        checkout_resource(gitlab_url, local_path, tag_name, False)
        for env in automation_env_names:
            order_dir = prod_dir + "/docker_yaml" + env["env_name"]
            docker_order = order_dir + '/order.txt'
            fdev_env_name = env["fdev_env_name"][prod_type][net_work]
            app_name_head = app_name_en.split("-")[0]
            yaml_file_dir = local_path + "/" + fdev_env_name + "/" + app_name_head + "/"
            yaml_dir_create = False
            if os.path.exists(yaml_file_dir):
                yaml_file_list = os.listdir(yaml_file_dir)
                for yaml_file in yaml_file_list:
                    if os.path.isfile(yaml_file_dir + yaml_file) and yaml_file.endswith(".yaml"):
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
                f.write(yaml_order_txt + "\n")
                f.close()
            else:
                logging.info("<strong style='color: red;'>docker_yaml缺少" + env["env_name"] + "目录</strong>")


def step5_app_check(prod_applications):
    logging.info("[+] 步骤五： 应用镜像检查 ")
    if len(prod_applications) == 0:
        logging.info("==本次变更无应用投产==")
        return
    logging.info("==应用直推镜像，无需检查包==")
    end_step()


def generate_app_scale(prod_id, prod_dir, image_deliver_type, automatic_production_root_path):
    """
    新增应用扩展order.txt
    :param image_deliver_type: 新版自动化发布判断
    :param prod_id:
    :param prod_dir:
    :param automatic_production_root_path:
    """
    app_scales = query_app_scale(prod_id)
    if len(app_scales) == 0:
        return
    logging.info("[+] 生成应用扩展order.txt ")
    scale_order_set = set()
    for app_scale in app_scales:
        if image_deliver_type == "1":
            automatic_production_service_path = "/fdev/scripts/service/"
            if app_scale["type_name"] == "容器化项目":
                automatic_production_service_path = "/fdev/scripts/container/"
        else:
            automatic_production_service_path = automatic_production_root_path + "/k8s_sercvice/"
            if app_scale["type_name"] == "容器化项目":
                automatic_production_service_path = automatic_production_root_path + "/k8s_container/"
        logging.info("弹性扩展应用类型：" + app_scale["type_name"])
        logging.info("脚本目录为：" + automatic_production_service_path)
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
            f = open(order_file_path, mode='a')
            f.write(order_txt + "\n")
            f.close()
            scale_order_set.add(order_file_path)
    logging.info("==应用扩展order.txt生成完毕,内容如下==")
    for file_path in scale_order_set:
        logging.info(file_path + ":")
        logging.info(get_log(file_path))
    end_step()


def generate_docker_delete(prod_dir):
    """
    新增清理投产文件order.txt
    :param prod_dir:变更根目录
    """
    logging.info("[+] 生成清理投产文件order.txt ")
    order_file_dir = prod_dir + "/docker_delete"
    order_file_path = order_file_dir + "/order.txt"
    if not os.path.exists(order_file_dir):
        os.makedirs(order_file_dir)
    f = open(order_file_path, mode='a')
    f.close()
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
        raise Exception("当前变更变更说明已修改为" + newest_prod_detail["prod_assets_version"] + ",请重新准备介质！" )
    # 传送介质
    scp_cmd = "scp -r " \
              + prod_dir + " " + AUTORELEASE_SERVER_USER + "@" + AUTORELEASE_SERVER_HOST + ":" + remote_root_path
    logging.info(scp_cmd)
    exec_cmd(scp_cmd, AUTORELEASE_SERVER_PASSWORD, logging_file)
    logging.info("==传送介质完毕==")
    logging.info("==开始打压缩包==")

    # 遍历本地介质目录下所有文件
    prod_root_dir = TEMP_DIR + prod_assets_version + "/"
    image_list_dir = os.listdir(prod_root_dir)
    if len(image_list_dir) != 0:
        image_list_url = prod_root_dir + str(random.randint(0, 999999)) + "/"
        # 随机生成文件夹名称,必须创建
        os.makedirs(image_list_url)
        flag = False
        f = open(image_list_url + "imagelist.txt", 'a')
        # 读取各文件中的直推镜像写入imagelist文件
        for image_file_url in image_list_dir:
            if os.path.isfile(prod_root_dir + image_file_url):
                flag = True
                with open(prod_root_dir + image_file_url, 'r') as prod_imagelist:
                    f.write(prod_imagelist.read())
        f.close()
        if flag:
            # 传送imagelist.txt
            scp_imagelist = "scp " + image_list_url + "imagelist.txt " + AUTORELEASE_SERVER_USER + "@" \
                            + AUTORELEASE_SERVER_HOST + ":" + remote_root_path
            logging.info(scp_imagelist)
            exec_cmd(scp_imagelist, AUTORELEASE_SERVER_PASSWORD, logging_file)
        # 删除imagelist所在文件夹
        shutil.rmtree(image_list_url)

    # 传送tar.sh
    scp_tar_script = "scp " + SCRIPTS_PATH + "tar.sh " \
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


def auto_release(prod_id):
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
        step2_config_files(prod_dir, prod_detail, prod_assets)
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
        # 脚本本地根目录
        automatic_production_root_path = SCRIPTS_PATH + "automatic_production"
        # 检查本地文件，没有则clone，存在则拉取
        checkout_resource(AUTOMATIC_PRODUCTION_URL, automatic_production_root_path, "master", True)
        step4_app_images(prod_dir, prod_applications, prod_images, image_deliver_type, prod_id, prod_type, prod_detail,
                         automatic_production_root_path)
        # 步骤五： 应用镜像检查
        # update_autorelease_stage(prod_id, "5")
        # step5_app_check(prod_applications)
        # 应用扩展文件 docker stop目录
        generate_app_scale(prod_id, prod_dir, image_deliver_type, automatic_production_root_path)
        # 清理投产文件 docker_delete目录
        generate_docker_delete(prod_dir)
        # 介质目录保存
        save_media_dirs(prod_id, prod_dir)
        # 步骤五： 传送介质
        update_autorelease_stage(prod_id, "5")
        step6_transfer(prod_detail, query_group_system_abbr(prod_detail["owner_groupId"]), logging_file)
        update_autorelease_stage(prod_id, "6")
        # 步骤六：检查镜像推送
        step7_check_image(prod_detail)
    except Exception, e:
        logging.error(e)
        logging.exception(e)
        raise e
    finally:
        update_autorelease_log(logging, prod_id, get_log(logging_file))


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
    auto_release(prod_id)
