#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import logging
from pexpect_cmd import *


if __name__ == '__main__':
    application_name_en = sys.argv[1]
    nas_host = sys.argv[2]
    nas_port = sys.argv[3]
    nas_user = sys.argv[4]
    nas_pwd = sys.argv[5]
    path = sys.argv[6]
    local_path = sys.argv[7]
    log_path = "/fdev/log/collapse/"
    logging_file = log_path + application_name_en + ".log"
    if not os.path.exists(log_path):
        os.makedirs(log_path)
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s : %(levelname)s : %(message)s',
        filename=logging_file
    )
    scp_tar_script = "scp -P " + nas_port + " " + nas_user + "@" + nas_host + ":" + path + " " + local_path
    logging.info(scp_tar_script)
    exec_cmd(scp_tar_script, nas_pwd, logging_file)
