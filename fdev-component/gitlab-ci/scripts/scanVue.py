#!/usr/bin/env python
# -*- coding: utf-8 -*-

import logging
import sys
import traceback
import json

from md5 import *

root_dir = '/fcomponent/scan_results'
log_dir = '/fcomponent/scan_log'


class Dependency:
    def __init__(self, name, version):
        self.name = name
        self.version = version


# 比较项目是否存在md5和dependency，新旧pom的md5是否相等
def compare_md5(dir, dict_new):
    md5_path = root_dir + '/' + dir.split('/')[-1] + '/' + 'md5.txt'
    dependency_path = root_dir + '/' + dir.split('/')[-1] + '/' + 'dependency.txt'
    if not os.path.exists(md5_path):
        return False
    if not os.path.exists(dependency_path):
        return False
    f = io.open(md5_path, mode='r', encoding='utf-8')
    try:
        dict_old = eval(f.read())
        f.close()
        for path in dict_new:
            if dict_old[path] != dict_new[path]:
                return False
    except Exception, e:
        logging.error(e)
        return False
    return True


# 对package.json文件做md5操作
def get_package_md5(file_path):
    dict_md5 = {file_path: md5(file_path)}
    return dict_md5


# 获取前端应用依赖组件
def get_data(dir):
    if not os.path.exists(log_dir):
        os.makedirs(log_dir)
    logging_file = log_dir + "/" + "scanVue.log"
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s : %(levelname)s : %(message)s',
        filename=logging_file
    )
    package_json_file = dir + "/package-lock.json"
    if not os.path.exists(package_json_file):
        logging.error("项目package.json文件不存在:" + dir)
        return None
    # 比较package文件是否有改变
    dict_json_md5 = get_package_md5(package_json_file)
    flag = compare_md5(dir, dict_json_md5)
    try:
        if flag:
            dependency_path = root_dir + '/' + dir.split('/')[-1] + '/' + 'dependency.txt'
            f = io.open(dependency_path, mode='r', encoding='utf-8')
            data = f.read()
            f.close()
            return data
        else:
            # 获取md5和dependency缓存存放路径
            target_cache_path = root_dir + '/' + dir.split('/')[-1]
            # 将新的md5值写入到文件中
            writepom(dict_json_md5, target_cache_path)
            # 读取vue项目的依赖描述文件，获取所有dependencies依赖的名称和版本
            package_json = io.open(package_json_file, mode="r", encoding="utf-8")
            dict_package_json = package_json.read()
            data = json.loads(dict_package_json, encoding='utf-8')
            dependencies = data["dependencies"]
            # 遍历所有依赖，存入到依赖列表中
            result_dependency = []
            if dependencies is not None and len(dependencies) > 0:
                for key, value in dependencies.iteritems():
                    result_dependency.append(Dependency(key, value["version"]).__dict__)
                data = json.dumps(result_dependency, ensure_ascii=False).encode('utf-8')
            # 写入dependency
            write_dependency(str(data), target_cache_path)
            return data
    except Exception, ex:
        traceback.print_exc()
        logging.error(dir + "扫描失败")
        logging.error(ex)
        return None


if __name__ == '__main__':
    dir = sys.argv[1]
    if dir is not None and dir.strip() != "":
        jsondata = get_data(dir)
        print(jsondata)
