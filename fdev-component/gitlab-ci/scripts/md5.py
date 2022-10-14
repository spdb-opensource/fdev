#!/usr/bin/env python
# -*- coding: utf-8 -*-

import io
import os
import re
import subprocess

root_dir = '/fcomponent/scan_results'


# 对pom.xml做md5操作
def md5(pomdir):
    command = 'md5sum ' + pomdir
    child = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE)
    result = child.stdout.read().decode('utf-8')
    return result


# 对pom.xml进行匹配packaging类型操作
def get_package(pomdir):
    package = re.compile('<packaging>(.+?)</packaging>')
    try:
        f = io.open(pomdir, mode='r', encoding='utf-8')
        content = f.read()
    except UnicodeDecodeError:
        try:
            f = io.open(pomdir, mode='r', encoding='gbk')
            content = f.read()
        except UnicodeDecodeError:
            return ''
    types = re.findall(package, content)
    if len(types) > 0:
        return types[0]
    return ''


# 获取当前项目的所有pom文件，并对空数据和target中的pom做过滤
def getpoms(dir):
    command = 'find ' + dir + ' -type f -name pom.xml'
    child = subprocess.Popen(command, shell=True,
                             stdout=subprocess.PIPE)
    result = child.stdout.read().decode('utf-8')
    poms = []
    for i in result.split('\n'):
        if '' != i and 'target' not in i:
            poms.append(i)
    return poms


# 获取所有pom的md5值，封装为字典
def get_poms_md5_packaging(poms):
    dict_md5 = {}
    dict_package = {}
    if len(poms) > 0:
        for i in poms:
            md5_value = md5(i)
            dict_md5[i] = md5_value
            package = get_package(i)
            dict_package[package] = i
    return dict_md5, dict_package


# 将pom的md5存入到目标文件中
def writepom(dict_poms_md5, targetpath):
    if not os.path.exists(targetpath):
        os.makedirs(targetpath)
    f = open(targetpath + '/md5.txt', mode='w')
    f.write(str(dict_poms_md5))
    f.close()


# 将项目依赖存入到文件中
def write_dependency(dependency, target_path):
    if not os.path.exists(target_path):
        os.makedirs(target_path)
    f = open(target_path + '/dependency.txt', mode='w')
    f.write(dependency)
    f.close()


def get_main_name(pomdir):
    artifactId = re.compile('<artifactId>(.+?)</artifactId>')
    parent = re.compile('<parent>(.|\n)*</parent>')
    try:
        f = io.open(pomdir, mode='r', encoding='utf-8')
        content = f.read()
    except UnicodeDecodeError:
        f = io.open(pomdir, mode='r', encoding='gbk')
        content = f.read()
    content = parent.sub('', content)
    artifactIds = re.findall(artifactId, content)
    if len(artifactIds) > 0:
        return artifactIds[0]


if __name__ == '__main__':
    dir = root_dir + '/mspmk-web-finance'
    poms = getpoms(dir)
    target_path = root_dir + '/' + dir.split('/')[-1]
    writepom(poms, target_path)
