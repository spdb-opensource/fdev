#!/usr/bin/env python3
# -*- coding: utf-8 -*-


import os


def line_separator_convert(file_path, direction):
    """
    读取文件内容，并将行分隔符在DOS与LINUX之间转换
    direction: 0->DOS to LINUX; 1->LINUX to DOS
    :param file_path:
    :param direction:
    :return:
    """
    # If file does not exist, then exit
    if not os.path.exists(file_path):
        print('File [' + file_path + '] is not exist!')
        return None
    if os.path.isfile(file_path):
        convert_separator(file_path, direction)
        return
    files = os.listdir(file_path)
    # log current files
    print('Files in ' + file_path + ' is ' + str(files))

    for f in files:
        if not os.path.isdir(file_path + '/' + f):
            # convert line separator
            convert_separator(file_path + '/' + f, direction)
        else:
            # recursive invocation
            line_separator_convert(file_path + '/' + f, direction)


def convert_separator(target_file, direction):
    """
    Converting line separator.
    :param target_file:
    :param direction:
    :return:
    """
    print('Converting file [' + target_file + ']......')
    try:
        new_content = []
        with open(target_file, 'rb') as in_stream:
            content = in_stream.readlines()
            for c in content:
                if 0 == direction:
                    new_character = c.replace(b'\r\n', b'\n')
                elif 1 == direction:
                    new_character = c.replace(b'\n', b'\r\n')
                new_content.append(new_character)
        with open(target_file, 'wb') as out_stream:
            out_stream.writelines(new_content)

    except IOError as err:
        print('Operation on file has encountered error:' + str(err))


def dos2unix(file_path):
    line_separator_convert(file_path, 0)


if __name__ == '__main__':
    dos2unix("/Users/lettuce/Downloads/test.sh")
