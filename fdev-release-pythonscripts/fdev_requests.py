#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import sys
import json
import urllib
import urllib2

reload(sys)
sys.setdefaultencoding("utf-8")
print sys.getdefaultencoding()

FDEV_HEADER = {"Content-Type": "application/json; charset=utf-8",
               "source": "back"}


def fdev_request(uri, data):
    return fdev_request_with_headers(uri, data, {})


def fdev_request_with_headers(uri, data, headers):
    """
    发送fdev请求
    :param headers: 自定义头
    :param uri: fdev交易路径
    :param data: 请求数据
    :return:
    """
    fdev_url = get_fdev_root_url() + uri
    # print "request>>" + fdev_url
    data = json.dumps(data, ensure_ascii=False).encode("utf-8")
    if len(headers) != 0:
        headers.update(FDEV_HEADER)
    else:
        headers = FDEV_HEADER.copy()
    # print data
    request = urllib2.Request(fdev_url, data, headers)
    try:
        response = urllib2.urlopen(request)
        response_data = json.loads(response.read().decode('utf-8'), encoding='utf-8')
        response_data = byteify(response_data)
        response.close()
        if "AAAAAAA" == response_data["code"]:
            return response_data["data"]
        else:
            print "response<<" + response_data["msg"]
            raise Exception(fdev_url + "交易异常:" + response_data["msg"])
    except urllib2.HTTPError, e:
        raise e


def get_fdev_root_url():
    """
    根据当前fdev环境,sit,uat,master获取fdev url
    :return: fdev url
    """
    FDEV_ENV = os.environ.get("CI_ENVIRONMENT_SLUG")
    if "master" == FDEV_ENV.lower():
        return "xxx"
    elif "rel" == FDEV_ENV.lower():
        return "xxx"
    elif "uat" == FDEV_ENV.lower():
        return "xxx"
    elif "sit" == FDEV_ENV.lower():
        return "xxx"
    else:
        return "http://localhost:8080"


def byteify(input, encoding='utf-8'):
    """
    UTF-8编码
    :param input:
    :param encoding:
    :return:
    """
    if isinstance(input, dict):
        return {byteify(key): byteify(value) for key, value in input.iteritems()}
    elif isinstance(input, list):
        return [byteify(element) for element in input]
    elif isinstance(input, unicode):
        return input.encode(encoding)
    else:
        return input


def query_prod_detail(prod_id):
    """
    查询变更详情
    :param prod_id: 变更id
    :return:
    """
    data = {"prod_id": prod_id}
    return fdev_request("/frelease/api/release/queryDetail", data)


def query_prod_applications(prod_id):
    """
    查询变更应用列表
    :param prod_id: 变更id
    :return:
    """
    data = {"prod_id": prod_id}
    return fdev_request("/frelease/api/release/queryAllApplications", data)


def query_prod_assets(prod_id):
    """
    查询变更文件列表
    :param prod_id:
    :return:
    """
    data = {"prod_id": prod_id}
    return fdev_request("/frelease/api/release/queryAssets", data)


def query_group_system_abbr(group_id):
    """
    查询系统缩写，如：006
    :param group_id:
    :return:
    """
    data = {"group_id": group_id}
    response = fdev_request("/frelease/api/release/queryGroupAbbr", data)
    return response["system_abbr"]


def update_autorelease_stage(prod_id, stage):
    """
    更新自动化发布阶段
    :param prod_id:
    :param stage:
    :param log:
    """
    data = {"prod_id": prod_id, "auto_release_stage": stage}
    fdev_request("/frelease/api/release/updateAutoReleaseStage", data)


def update_prod_status(prod_id, status):
    """
    修改准备变更介质状态
    :param prod_id:
    :param status:
    """
    data = {"prod_id": prod_id, "status": status}
    fdev_request("/frelease/api/release/updateProdStatus", data)


def update_autorelease_log(logging, prod_id, log):
    """
    更新自动化发布日志
    :param logging:
    :param prod_id:
    :param log:
    """
    data = {"prod_id": prod_id, "auto_release_log": log}
    try:
        fdev_request_with_headers("/frelease/api/release/updateAutoReleaseLog", data, {"Long-Message": "true"})
    except Exception, ex:
        logging.error(ex)
        logging.error("updateAutoReleaseLog failed! request data:"
                      + json.dumps(data, ensure_ascii=False).encode("utf-8"))


def update_push_image_log(logging, prod_id, log):
    """
    修改镜像推送错误日志
    :param logging:
    :param prod_id:
    :param log:
    """
    data = {"prod_id": prod_id, "push_image_log": log}
    try:
        fdev_request_with_headers("/frelease/api/release/updatePushImageLog", data, {"Long-Message": "true"})
    except Exception, ex:
        logging.error(ex)
        logging.error("updateAutoReleaseLog failed! request data:"
                      + json.dumps(data, ensure_ascii=False).encode("utf-8"))


def query_before_pord_images(release_node_name, version):
    """
    查询该投产窗口下该变更版本之前的所有镜像标签
    :param release_node_name:
    :param version:
    """
    data = {"release_node_name": release_node_name, "version": version}
    response = fdev_request("/frelease/api/release/queryBeforePordImages", data)
    return response


def query_app_scale(prod_id):
    """
    查询本次变更应用扩展
    :param prod_id:
    :return:
    """
    data = {"prod_id": prod_id}
    response = fdev_request("/frelease/api/appscale/query", data)
    return response


def query_gitlab_project_id_by_name_en(app_name_en):
    """
    根据应用英文名查询gitlab project id
    :param app_name_en:
    :return:
    """
    data = {"name_en": app_name_en}
    response = fdev_request("/fapp/api/app/query", data)
    return response[0]["gitlab_project_id"]


def update_application(prod_id, application_id, status):
    """
    更新变更应用介质准备状态
    :param prod_id
    :param application_id
    :param status
    :return:
    """
    data = {"prod_id": prod_id, "application_id": application_id, "status": status}
    fdev_request("/frelease/api/release/updateApplication", data)


def query_var_by_label_and_type(gitlab_id, label, deploy_type):
    """
    根据环境标签和实体类型查询实体与环境映射信息
    :param gitlab_id
    :param label
    :param deploy_type
    :return:
    """
    data = {"gitlabId": gitlab_id, "label": label, "type": deploy_type}
    response = fdev_request("/fenvconfig/api/v2/var/queryVarByLabelAndType", data)
    namespace_set = set()
    for env_key in response.keys():
        for env in response[env_key]:
            if env["key"] == "FDEV_CAAS_REGISTRY_NAMESPACE":
                if (env["value"] is not None) and (env["value"] != ""):
                    namespace_set.add(env["value"])
                break
    return namespace_set


def query_automation_param_map():
    """
    查询已转成map的自动化发布配置
    :return:
    """
    data = {}
    response = fdev_request("/frelease/api/automationparam/queryToMap", data)
    return response


def query_automation_env():
    """
    查询自动化发布环境
    :return:
    """
    data = {}
    response = fdev_request("/frelease/api/automationenv/queryByProdType", data)
    return response


def save_media_dir_data(prod_id, dir_array):
    """
    查询自动化发布环境
    :param prod_id 变更编号
    :param dir_array 变更目录集合
    """
    data = {"prod_id": prod_id, "directory_list": dir_array}
    fdev_request("/frelease/api/prodMediaFile/add", data)


def query_docker_directory(release_node_name, application_id, pro_image_uri, prod_id):
    """
    查询是否有docker目录
    :param release_node_name 投产窗口名称
    :param application_id 应用编号
    :param pro_image_uri 镜像标签
    :param prod_id 变更id
    """
    data = {"release_node_name": release_node_name, "application_id": application_id, "pro_image_uri": pro_image_uri,
            "prod_id": prod_id}
    response = fdev_request("/frelease/api/devopsrecord/query_docker_directory", data)
    return response


def query_rel_env(gitlab_project_id, tag_name):
    """
    查询应该所属rel环境名称
    :param gitlab_project_id:应用的gitlab对应id
    :param tag_name:分支名称
    :return:
    """
    data = {"gitlab_project_id": gitlab_project_id, "release_branch": tag_name}
    response = fdev_request("/frelease/api/releasenode/queryRelEnv", data)
    return response


def query_image_user_password(gitlab_project_id, env):
    """
    根据环境与应用查询caas平台用户名密码
    :param gitlab_project_id:应用的gitlab对应id
    :param env:环境
    :return:
    """
    data = {"gitlabId": gitlab_project_id, "env": env}
    response = fdev_request("/fenvconfig/api/v2/appDeploy/queryImageUserAndPwd", data)
    return response


def build_pro_dat_tar(logging, prod_id, prod_type, prod_version, prod_applications):
    """
    根据变更应用获取生成的tar包路径
    :param logging: 打印日志
    :param prod_id: 变更id
    :param prod_type: 变更类型
    :param prod_version: 变更版本号
    :param prod_applications: 变更应用详情
    :return:
    """
    application_list = []
    for application in prod_applications:
        if application["type_name"] == "Vue应用" and application["app_name_en"].startswith("mspmk-cli"):
            last_tag = query_last_tag_by_gitlab_id(application["gitlab_project_id"], prod_id, prod_type)
            detail = {"name_en": application["app_name_en"], "name_zh": application["app_name_zh"],
                      "branch": application["pro_image_uri"].split(":")[1], "last_tag": last_tag}
            application_list.append(detail)
    if len(application_list) == 0:
        logging.info("本次变更没有mspmk-cli*的vue应用")
        return ""
    env = "PRO"
    if "gray" == prod_type:
        env = "GRAY"
    data = {"env": env, "prod_id": prod_id, "prod_version": prod_version, "project_name": application_list}
    response = fdev_request("/finterface/api/interface/buildProDatTar", data)
    return response


def query_last_tag_by_gitlab_id(gitlab_project_id, prod_id, prod_type):
    """
    根据gitlab对应id查询上一次投产的tag
    :param gitlab_project_id: 应用id
    :param prod_id: 变更id
    :param prod_type: 变更类型
    :return:
    """
    data = {"gitlab_project_id": gitlab_project_id, "type": prod_type, "prod_id": prod_id}
    response = fdev_request("/frelease/api/release/queryLastTagByGitlabId", data)
    return response


def download_minio_file(file_path, minio_path, module_name):
    """
    下载minio文件到变更目录
    :param file_path: 文件本地路径
    :param minio_path: minio路径
    :param module_name: 模块名
    :return:
    """
    data = {"path": minio_path, "moduleName": module_name}
    data = urllib.urlencode(data)
    url = "/fdocmanage/api/file/filesDownload?" + data
    fdev_url = get_fdev_root_url() + url
    headers = FDEV_HEADER.copy()
    # print data
    request = urllib2.Request(fdev_url, headers=headers)
    try:
        response = urllib2.urlopen(request)
        resultdate = response.read()
        f = open(file_path, "wb")
        f.write(resultdate)
    except urllib2.HTTPError, e:
        raise e


def query_autoconfig_gitlab_url(gitlab_project_id, tag):
    """
    查询新增应用标识
    :param gitlab_project_id: 应用gitlab对应id
    :param tag: tag名称
    :return:
    """
    data = {"gitlab_project_id": gitlab_project_id, "tag": tag}
    response = fdev_request("/frelease/api/release/queryAutoConfigUrl", data)
    return response


def query_push_image_uri(release_node_name, prod_id, application_id):
    """
    查询推送镜像列表
    :param release_node_name: 投产窗口名称
    :param prod_id: 变更id
    :param application_id: 应用id
    :return:
    """
    data = {"release_node_name": release_node_name, "prod_id": prod_id, "application_id": application_id}
    response = fdev_request("/frelease/api/applicationImage/queryPushImageUri", data)
    return response


def update_push_image_status(logging, push_id, status, log, type):
    """
    根据id修改镜像推送状态以及推送日志
    :param logging: 当前日志打印
    :param push_id: 修改标识
    :param status: 状态
    :param log: 日志
    :return:
    """
    data = {"id": push_id, "status": status, "push_image_log": log, "deploy_type":type}
    try:
        fdev_request_with_headers("/frelease/api/applicationImage/updateStatusById", data, {"Long-Message": "true"})
    except Exception, ex:
        logging.error(ex)
        logging.error("updatePushImageLog failed! request data:"
                      + json.dumps(data, ensure_ascii=False).encode("utf-8"))


def check_push_image_and_auto_release(prod_id):
    """
    根据变更id查询推送镜像状态并更改准备介质状态
    :param prod_id: 变更id
    :return:
    """
    data = {"prod_id": prod_id}
    fdev_request("/frelease/api/applicationImage/checkPushImageAndAutoRelease", data)


def update_by_prod_application(prod, application, status, log,imageUri):
    """
    根据变更id与应用id批量修改推送镜像状态与日志
    :param prod:
    :param application:
    :param status:
    :param log:
    :return:
    """
    data = {"prod_id": prod, "application_id": application, "status": status, "push_image_log": log, "image_uri":imageUri}
    fdev_request("/frelease/api/applicationImage/updateByProdApplication", data)


def query_prod_assets_by_prod_id(prod_id):
    """
    查询变更文件列表
    :param prod_id:
    :return:
    """
    data = {"prod_id": prod_id}
    return fdev_request("/frelease/api/release/queryDeAutoAssets", data)


def check_de_auto_release(prod_id):
    """
    根据变更id查询推送镜像状态并更改准备介质状态
    :param prod_id: 变更id
    :return:
    """
    data = {"prod_id": prod_id}
    fdev_request("/frelease/api/release/checkDeAutoRelease", data)


def query_image_list_by_prod_assets_version(prod_assets_version):
    """
    根据总介质目录查询镜像推送记录
    :param prod_assets_version:
    :return:
    """
    data = {"prod_assets_version": prod_assets_version}
    return fdev_request("/frelease/api/applicationImage/queryImagelistByProdAssetVersion", data)


def query_env_params(applicationId, gitlabId, content, fdev_env):
    """
    查询实体中属性值
    :return:
    """
    data = {"application_id": applicationId, "gitlabId": gitlabId, "content": content, "env_name": fdev_env}
    response = fdev_request("/frelease/api/automationenv/queryEnvParams", data)
    return response


def query_aws_configure_assets(aws_group_id):
    """
    查询变更文件列表
    :param aws_group_id:
    :return:
    """
    data = {"group_id": aws_group_id}
    return fdev_request("/frelease/api/release/queryAwsConfigByGroupId", data)


def query_esf_userinfo(prod_id):
    """
        查询esf用户注册信息列表
        :param aws_group_id:
        :return:
        """
    data = {"prod_id": prod_id}
    response = fdev_request("/frelease/api/esf/queryEsfRegistration", data)
    return response

def query_batch_Info(prod_id):
    """
        查询批量任务列表列表
        :param prod_id:
        :return:
        """
    data = {"prod_id": prod_id}
    response = fdev_request("/frelease/api/batch/queryBatchTaskByProdId", data)
    return response