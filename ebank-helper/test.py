#!/usr/bin/python
# -*- coding: utf-8 -*-
import os
import sys
import json


class Scp:

    def man(self, parent_dir):
        # 获取当前文件夹下的.map文件(仅一级)
        mapFiles = []
        dir_or_files = os.listdir(parent_dir)
        for dir_or_file in dir_or_files:
            # 获取目录或者文件的全路径
            dir_file_path = os.path.join(parent_dir, dir_or_file)
            if os.path.isdir(dir_file_path):
                continue
            elif dir_or_file.endswith(".py"):
                mapFiles.append(dir_file_path)
        print mapFiles

if __name__ == '__main__':
    scp = Scp()
    scp.man("D:\\pyProject\\ebank-helper")
