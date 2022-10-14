#!/usr/bin/env python
# -*- coding: utf-8 -*-

import json
import logging
import shutil
import sys
import traceback

from md5 import *

root_dir = '/fcomponent/scan_results'
log_dir = '/fcomponent/scan_log'


class dependency:
    def __init__(self, groupId, artifactId, version):
        self.groupId = groupId
        self.artifactId = artifactId
        self.version = version


# 执行mvn命令，获取项目依赖
def executeMvn(mvn, url, root_pom=''):
    if root_pom:
        cwd_dir = root_pom
    else:
        cwd_dir = url
    child = subprocess.Popen(mvn, shell=True, cwd=cwd_dir,
                             stdout=subprocess.PIPE)
    result = child.stdout.read().decode('utf-8')
    # 如果项目执行mvn depenency:list失败，执行mvn clean install
    if 'BUILD FAILURE' in result:
        logging.error(cwd_dir + '执行mvn dependency:list失败')
        # 执行mvn clean install命令
        logging.error(cwd_dir + '执行mvn clean install')
        sub_install = subprocess.Popen('mvn clean install -Dmaven.test.skip=true', shell=True, cwd=cwd_dir,
                                       stdout=subprocess.PIPE)
        try:
            install_result = sub_install.stdout.read().decode('utf-8')
        except UnicodeDecodeError:
            install_result = sub_install.stdout.read().decode('gbk')
        if 'FAILURE' in install_result and 'ERROR' in install_result:
            logging.error(cwd_dir + '执行mvn clean install失败')
            logging.error(install_result)
        # 执行install后重新执行扫描命令
        logging.error(cwd_dir + '执行install后重新执行扫描命令')
        sub_child = subprocess.Popen(mvn, shell=True, cwd=cwd_dir,
                                     stdout=subprocess.PIPE)
        try:
            result = sub_child.stdout.read().decode('utf-8')
        except UnicodeDecodeError:
            result = sub_child.stdout.read().decode('gbk')
    # 如果是父子项目，进行正则匹配，获取子项目的依赖
    if root_pom:
        main_name = get_main_name(url + 'pom.xml')
        if main_name:
            pattern = re.compile('\\(default-cli\\) @ ' + main_name + ' ---((.|\n)*?)-------')
            logging.info("--扫描结果匹配正则")
            logging.info(main_name)
            child_dependency = re.findall(pattern, result)
            if len(child_dependency) > 0:
                regex_dependency = child_dependency[0]
                if len(regex_dependency) > 0:
                    return regex_dependency[0]
            else:
                logging.info("--扫描结果匹配正则为空，匹配父项目依赖")
                return result
    logging.info("--非父子项目扫描")
    return result


# 获取第一个pom.xml文件
def getFirstPom(url):
    blacklist = [".gitignore", ".gitlab-ci.yml", ".git", ".com", "com", ".gitlab-ci", "gitlab-ci", "gitlab-ci", "doc",
                 "src", "static", "config", "build"]
    # 判断是否文件夹
    if os.path.isdir(url):
        pompath = url + "/" + "pom.xml"
        # 判断当前文件夹下是否存在pom文件
        if not os.path.exists(pompath):
            # 遍历当前文件夹，获取所有文件，递归获取
            files = os.listdir(url)
            for filename in files:
                # blacklist中的文件或者文件夹跳过
                if filename not in blacklist and "-testng" not in filename:
                    tmp = getFirstPom(url + "/" + filename)
                    if tmp is not None:
                        return tmp
        else:
            return url


# 比较项目是否存在md5和dependency，新旧pom的md5是否相等
def compare_md5(url, dict_new):
    md5path = root_dir + '/' + url.split('/')[-1] + '/' + 'md5.txt'
    dependencypath = root_dir + '/' + url.split('/')[-1] + '/' + 'dependency.txt'
    if not os.path.exists(md5path):
        return False
    if not os.path.exists(dependencypath):
        return False
    f = io.open(md5path, mode='r', encoding='utf-8')
    try:
        dict_old = eval(f.read())
        f.close()
        for pompath in dict_new:
            if dict_old[pompath] != dict_new[pompath]:
                return False
    except Exception:
        return False
    return True


# 获取项目依赖列表
def getList(mvn, url):
    if not os.path.exists(log_dir):
        os.makedirs(log_dir)
    logging_file = log_dir + "/" + "scan.log"
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s : %(levelname)s : %(message)s',
        filename=logging_file
    )
    try:
        # 获取所有pom.xml的列表
        poms = getpoms(url)
        if len(poms) < 1:
            return None
        # 比较pom.xml文件是否有改变
        dict_poms_md5, dict_poms_package = get_poms_md5_packaging(poms)
        flag = compare_md5(url, dict_poms_md5)
        # 如果md5.txt和dependenc.txt存在，且md5未变化，直接返回dependenc.txt内容
        if flag:
            dependencypath = root_dir + '/' + url.split('/')[-1] + '/' + 'dependency.txt'
            f = io.open(dependencypath, mode='r', encoding='utf-8')
            data = f.read()
            f.close()
            return data
        # 进行mvn命令扫描，存入新的md5和dependency
        else:
            # 将新的md5值写入到文件中
            target_path = root_dir + '/' + url.split('/')[-1]
            writepom(dict_poms_md5, target_path)
            # 获取packaging为pom和war类型的pom.xml路径
            pom_path = ''
            war_path = ''
            keys = dict_poms_package.keys()
            if len(keys) > 0:
                if 'pom' in keys:
                    pom_path = str(dict_poms_package['pom']).replace('pom.xml', '').rstrip()
                if 'war' in keys:
                    war_path = str(dict_poms_package['war']).replace('pom.xml', '').rstrip()
            # 如果有war类型的项目，进入到war路径下执行扫描脚本
            result = ''
            if war_path is not None and war_path.strip() != '':
                result = executeMvn(mvn, war_path, pom_path)
            else:
                url = getFirstPom(url)
                if url is not None:
                    result = executeMvn(mvn, url)
            # 正则匹配，获取所有依赖
            pattern = re.compile(
                "(([\\w\\-\\.\\d]+):([\\w\\-\\.\\d]+):([\\w\\-\\.\\d]+):([\\w\\-\\.\\d]+):([\\w\\-\\.\\d]+))")
            list = re.findall(pattern, result)
            relist = []
            for iteritem in list:
                relist.append(dependency(iteritem[1], iteritem[2], iteritem[4]).__dict__)
            data = json.dumps(relist, ensure_ascii=False).encode('utf-8')
            # 写入dependency
            write_dependency(str(data), target_path)
            return data
    except Exception, ex:
        # traceback.print_exc()
        logging.error(url + "扫描失败")
        logging.error(ex)
        # 清除相关缓存数据
        shutil.rmtree(root_dir + '/' + url.split('/')[-1], ignore_errors=True)
        return None


if __name__ == '__main__':
    # strs = "D:/ideaproject/fdev-app"
    strs = sys.argv[1]
    mvn = "mvn dependency:list"
    if strs is not None and strs.strip() != "":
        jsondata = getList(mvn, strs)
        print(jsondata)
