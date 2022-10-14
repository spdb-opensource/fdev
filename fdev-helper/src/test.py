# fdev_runtime_path = "/home/luotao/test.properties"
# FDEV_CONFIG_HOST1_IP = "xxx"
# FDEV_CONFIG_HOST2_IP = "xxx"
# FDEV_CONFIG_PORT = "22"
# FDEV_CONFIG_USER = "root"
# FDEV_CONFIG_PASSWORD = "12345678"
# FDEV_CONFIG_DIR = "/ebank/spdb/configs_sit"
#
# cmd = "scp %s %s@%s:%s -p %s" % (
#     fdev_runtime_path,FDEV_CONFIG_USER,FDEV_CONFIG_HOST1_IP,FDEV_CONFIG_DIR,FDEV_CONFIG_PORT)
#
# print cmd

# child = pexpect.spawn('scp /test.txt root@xxx:/root')
# child.logfile = sys.stdoutw
# index = child.expect(['yes', 'password', pexpect.TIMEOUT])
#
# if index == 0:
#     child.sendline('yes')
#     child.sendline('12345678')
#     child.expect(pexpect.EOF)
# elif index == 1:
#     child.sendline('12345678')
#     child.expect(pexpect.EOF)
# else:
#     print('SCP ������ʧ��,��������')
#     sys.exit(1)