#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import pexpect


def exec_cmd(cmd, password, log_file):
    child = pexpect.spawn(cmd)
    child.logfile = open(log_file, 'a')
    index = child.expect(['yes', pexpect.TIMEOUT, 'password'])
    child.logfile.write("cmd:" + cmd)
    if index == 0:
        child.sendline('yes')
        child.expect(['password', pexpect.EOF])
        child.sendline(password)
        child.expect(pexpect.EOF, timeout=1800)
        child.logfile.write('success')
    elif index == 1:
        # child.sendline(password)
        child.expect(pexpect.EOF)
        child.logfile.write('timeout')
    elif index == 2:
        child.sendline(password)
        child.expect(pexpect.EOF, timeout=1800)
        child.logfile.write('success')
    else:
        child.logfile.write('cmd execute error!')
        child.logfile.write("password:" + password)
        sys.exit(1)
    child.logfile.write("\n")
    return child
