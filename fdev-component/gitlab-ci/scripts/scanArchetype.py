#!/usr/bin/env python
# -*- coding: utf-8 -*-

import subprocess
import io
import logging
import os
import sys
import json

#log_dir = 'D:/kelan/scan_log'


log_dir = '/fcomponent/scan_log'


class dependency:
    def __init__(self, groupId, artifactId, version):
        self.groupId = groupId
        self.artifactId = artifactId
        self.version = version


def get_archetype(dir):
    command = 'find ' + dir + ' -type f -name .archetype'
    child = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE)
    result = child.stdout.read().decode('utf-8')
    archetype_path = []
    for i in result.split('\n'):
        if '' != i:
            archetype_path.append(i)
    return archetype_path


def get_dependency(dir):
    if not os.path.exists(log_dir):
        os.makedirs(log_dir)
    logging_file = log_dir + "/" + "archetype.log"
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s : %(levelname)s : %(message)s',
        filename=logging_file
    )

    try:
        archetype_path = get_archetype(dir)
        if len(archetype_path) == 0:
            return None
        else:
            dict = {}
            path = archetype_path[0]
            file = io.open(path, mode='r', encoding='utf-8')
            for line in file:
                # 按行读取，去除每一行中的换行符
                line = line.strip('\n')
                result = line.split(':')
                if len(result) > 1:
                    dict[result[0]] = result[1]
            relist = [
                dependency(dict['archetype.groupId'], dict['archetype.artifactId'], dict['archetype.version']).__dict__]
            data = json.dumps(relist, ensure_ascii=False).encode('utf-8')
            return data
    except Exception:
        logging.error(dir + "获取骨架信息失败")
        return None


if __name__ == '__main__':
    dir = sys.argv[1]
    print(get_dependency(dir))
