#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import os
import subprocess
import logging
import traceback
import re
import io


# maven仓库路径
repository_dir = '/root/.m2/repository'
# pom文件临时存储路径
temp_dir = '/fcomponent/maven_temp'
# 日志路径
log_dir = '/fcomponent/scan_log'
# 扫描依赖树存储路径
dependency_tree_dir = '/fcomponent/dependency_tree'

# repository_dir = 'D:/Mavenbox/repository'
# temp_dir = 'D:/fcomponent/maven_temp'
# log_dir = 'D:/fcomponent/scan_log'
# dependency_tree_dir = 'D:/fcomponent/dependency_tree'


def get_pom_path(group_id, artifact_id, version):
    group = str.split(group_id, '.')
    path = repository_dir
    if os.path.exists(path):
        for i in group:
            path = os.path.join(path, i)
        # 拼接jar包路径
        path = os.path.join(path, artifact_id, version)
        # 拼接jar包名称
        file_name = artifact_id + "-" + version + '.pom'
        return path, file_name
    return '', ''


def copy_file(path, file_name):
    if not os.path.exists(temp_dir):
        os.makedirs(temp_dir)
    command = 'cp ' + path + '/' + file_name + ' ' + temp_dir + '/' + 'pom.xml'
    if path and os.path.exists(path + '/' + file_name):
        subprocess.Popen(command, shell=True, cwd=path,
                         stdout=subprocess.PIPE)
    else:
        raise Exception("cp file failure")


def get_dependency_tree(artifact_id):
    command = 'mvn dependency:tree'
    subparent = subprocess.Popen(command, shell=True, cwd=temp_dir,
                                 stdout=subprocess.PIPE)
    try:
        result = subparent.stdout.read().decode('utf-8')
    except UnicodeDecodeError:
        result = subparent.stdout.read().decode('gbk')
    dependency = re.compile('\\(default-cli\\) @ ' + artifact_id + ' ---((.|\n)*?)\\[INFO\\] ---------------')
    logging.info("--扫描结果匹配正则" + artifact_id)
    child_dependency = re.findall(dependency, result)
    if len(child_dependency) > 0:
        regex_dependency = child_dependency[0]
        if len(regex_dependency) > 0:
            return regex_dependency[0]
    else:
        logging.info("--匹配正则为空,打印扫描结果")
        logging.info(result)
        return ''


def get_jar_dependency(group_id, artifact_id, version):
    if not os.path.exists(log_dir):
        os.makedirs(log_dir)
    logging_file = log_dir + "/" + "scan_jar_tree.log"
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s : %(levelname)s : %(message)s',
        filename=logging_file
    )
    try:
        logging.info('开始扫描依赖树' + group_id + ':' + artifact_id + ':' + version)
        # 判断是否已存在扫描结果
        tree_dependency_file = dependency_tree_dir + '/' + artifact_id + '/' + version + '.txt'
        if os.path.exists(tree_dependency_file):
            f = io.open(tree_dependency_file, mode='r', encoding='utf-8')
            dependency_data = f.read()
            f.close()
            return dependency_data
        else:
            # 获取maven仓库jar包所在文件路径和项目依赖文件
            path, file_name = get_pom_path(group_id, artifact_id, version)
            # 将依赖文件拷贝至空项目目录
            copy_file(path, file_name)
            # 执行mvn dependency:tree获取依赖树
            tree_data = get_dependency_tree(artifact_id)
            if not os.path.exists(dependency_tree_dir + '/' + artifact_id):
                os.makedirs(dependency_tree_dir + '/' + artifact_id)
            f = open(tree_dependency_file, mode='w')
            data_new = tree_data.strip().replace('\r\n', '\n').replace('[INFO] ', '')
            f.write(data_new)
            f.flush()
            f.close()
            return data_new
    except Exception, ex:
        # except Exception as ex:
        # traceback.print_exc()
        logging.error("扫描失败")
        logging.error(ex)
        return ''
    finally:
        if os.path.exists(temp_dir + '/' + 'pom.xml'):
            os.remove(temp_dir + '/' + 'pom.xml')


if __name__ == '__main__':
    group_id = sys.argv[1]
    artifact_id = sys.argv[2]
    version = sys.argv[3]
    mvn = 'mvn dependency:tree'
    data = get_jar_dependency(group_id, artifact_id, version)
    print(data)