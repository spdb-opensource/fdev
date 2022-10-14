# -*- coding: utf-8 -*-
"""
Created on Wed Jun 23 15:05:18 2021

@author: zhangjy19
"""
import json
import datetime
import sys
import argparse
import os

BASE_CONFIG=r'template.json'
ORDER_FILE = r'order.txt'
SORT_FILE = r'sortcfg.txt'
FINISH_FILE = r'finish'
DE_BUG = False


def catch_args():
    parser1 = argparse.ArgumentParser()
    parser1.add_argument('-m','--module')
    return  vars(parser1.parse_args())


def showError(flag1,content1):
    dict1 = {'I': 'INFO', 'W': 'WARN', 'E': 'ERROR'}
    time1 =datetime.datetime.now().strftime("%FT%T")
    if flag1 in dict1.keys():
        flag1 = dict1[flag1]
    print(time1 + ". " + flag1 + ". " + str(content1))
    if flag1 == 'ERROR':
        sys.exit(1)


def Set_Config(module,param_dict):
    f = open(BASE_CONFIG,'r')
    text = f.read()
    data = json.loads(text)
    f.close()
    data1 = Reset_Procedure(module,data)
    data2 = Set_Param(data1,param_dict)
#    print(data2)
    ##写到json文件里                
#    print(type(param_dict['obname']))
    target_file = str(data2['metadata']['name']) + "-" + str(data2['metadata']['namespace']) + "-" + str(data2['kind']) + "-" + str(data2['procedure']['checkAndDown']) + "-" + str(data2['procedure']['zeroReplica']) + "-" + str(data2['procedure']['applyYAML']) + "-" + str(data2['procedure']['updateImage']) + "-" + str(data2['procedure']['scaleReplicas']) + "-" + str(data2['procedure']['checkAndUp']) + "-" + str(data2['procedure']['deletePo']) + ".json"
    data3 = json.dumps(data2)
    with open(target_file,'w+') as f:
        f.write(data3)
    if not os.path.isfile(FINISH_FILE):
        with open(SORT_FILE,'a+') as s:
            s.write(target_file)
            s.write('\n')
#    
        
        
        
PARAM_CHECK_MAP={'docker':('tag','obname','imagename','imagespacename','namespace','imageuser'),
                 'docker_all':('tag','obname','imagename','imagespacename','namespace','imageuser'),
                 'docker_stopall':('obname','namespace'),
                 'docker_startall':('replicasnu','obname','namespace'),
                 'docker_scale':('replicasnu','obname','namespace'),
                 'docker_yaml':('tag','obyml','obname','imagename','imagespacename','namespace','dceuser','imageuser'),
                 'docker_restart':('obname','namespace'),
                 'docker_stop':('obname','namespace')}
def Check_Param(module,param_dict):
    if module not in PARAM_CHECK_MAP.keys():
        showError("E","{} not in docker docker_all docker_stopall docker_startall docker_scale docker_yaml docker_restart docker_stop".format(module))
    param_input = param_dict.keys()
    for param in PARAM_CHECK_MAP[module]:
        if param not in param_input:
            showError("E","Current module is {} Please input {} in order.txt".format(module,param))
            return False
#            showError("E","Please input {} in order.txt".format(param))
    if 'containername' in param_input:
        n = len(param_dict['containername'].split(','))
        for k in ('tag','imagespacename','imagename','port'):
            if k not in param_input:
                showError("E","params include containername Please input {} in order.txt".format(k))
                return False
            if len(param_dict[k].split(',')) != n:
                showError("E","numbers of {} should equal to containername".format(k))
                return False
    return True





def Set_Param(data,param_dict):
    #初始化container列表
    containers_list = data['spec']['template']['spec']['containers']
    container_template = containers_list[0].copy()
    #order参数赋值到json文件
    if 'containername' in param_dict.keys():
        containers_list.clear()
        for value in param_dict['containername'].split(','):
            container = container_template.copy()
            container['name'] = value
            temp = dict()
            temp['mSvcStatus'] = container_template['lifecycle']['mSvcStatus'].copy()
            temp['mSvcDown'] = container_template['lifecycle']['mSvcDown'].copy()
            temp['mSvcUp'] = container_template['lifecycle']['mSvcUp'].copy()
            container['lifecycle'] = temp
            containers_list.append(container)
    for k,v in param_dict.items():
        if k == 'kind':
            data['kind'] = v
        elif k == 'obname':
            data['metadata']['name'] = v
        elif k == 'dceuser':
            data['metadata']['dceuser'] = v
        elif k == 'dcepwd':
            data['metadata']['dcepwd'] = v
        elif k == 'imageuser':
            data['metadata']['imageuser'] = v
        elif k == 'imagepwd':
            data['metadata']['imagepwd'] = v
        elif k == 'imagespaceip':
            data['metadata']['imagespaceip'] = v
        elif k == 'namespace':
            data['metadata']['namespace'] = v
        elif k == 'obyml':
            data['spec']['yamlPath'] = v
        elif k == 'stime':
            data['spec']['template']['spec']['scalingTime'] = int(v)
        elif k == 'replicasnu':
            data['spec']['replicas'] = int(v)
        elif k in ('checkAndDown','zeroReplica','applyYAML','updateImage','scaleReplicas','checkAndUp','deletePo','check_namespace','check_deploymentname','check_metadatalabels','check_matchlabels','check_templatemetadatalabels','check_replicas','check_volumes','check_hostaliases','check_imagepullsecrets','check_dnspolicy','check_nameservers','check_strategy','check_matchexpressionskey','check_matchexpressionsvalues','check_containersname','check_limitscpu','check_limitsmemory','check_requestscpu','check_requestsmemory','check_ports','check_envfrom','check_env','check_prestop','check_image','check_volumemounts'):
            data['procedure'][k] = int(v)
        elif k in ('tag','imagespacename','imagename','port'):
            value_list = v.split(',')
            for i in range(len(value_list)):
                container = containers_list[i]
                if k == 'tag':
                    container['image'] = value_list[i]
                elif k == 'port':
                    container['lifecycle']['mSvcStatus']['exec'] = "curl --fail --silent http://xxx:" + value_list[i] + "/getstatus"
                    container['lifecycle']['mSvcDown']['exec'] = "curl --fail --silent http://xxx:" + value_list[i] + "/down"
                    container['lifecycle']['mSvcUp']['exec'] = "curl --fail --silent http://xxx:" + value_list[i] + "/up"
                else:
                    container[k] = value_list[i]
                    
    return data


PROCEDURE_SWITCH_MAP={'docker':{'checkAndDown':0,'checkAndUp':0,'updateImage':1},
                 'docker_all':{'updateImage':1},
                 'docker_stopall':{'zeroReplica':1},
                 'docker_startall':{'checkAndDown':0,'scaleReplicas':1,'checkAndUp':0},
                 'docker_scale':{'checkAndDown':0,'scaleReplicas':1,'checkAndUp':0},
                 'docker_yaml':{'checkAndDown':0,'applyYAML':1,'checkAndUp':0,'check_namespace':1,'check_deploymentname':1,'check_metadatalabels':1,'check_matchlabels':1,'check_templatemetadatalabels':1,'check_replicas':1,'check_volumes':1,'check_hostaliases':1,'check_imagepullsecrets':1,'check_dnspolicy':1,'check_nameservers':1,'check_strategy':1,'check_matchexpressionskey':1,'check_matchexpressionsvalues':1,'check_containersname':1,'check_limitscpu':1,'check_limitsmemory':1,'check_requestscpu':1,'check_requestsmemory':1,'check_ports':1,'check_envfrom':1,'check_env':1,'check_prestop':1,'check_image':1,'check_volumemounts':1},
                 'docker_restart':{'checkAndDown':0,'deletePo':1,'checkAndUp':0},
                 'docker_stop':{'zeroReplica':1}}
def Reset_Procedure(module,data):
    if module not in PROCEDURE_SWITCH_MAP.keys():
        showError("E","{} not in docker docker_all docker_stopall docker_startall docker_scale docker_yaml docker_restart docker_stop".format(module))
    for k,v in PROCEDURE_SWITCH_MAP[module].items():
        data['procedure'][k] = v
    return data
    


def Read_Param(line_str):
    param_dict={}
    for k_v in line_str.strip().split('%'):
        tmp = k_v.split(':')
        key = tmp[0]
        val = tmp[1]
        param_dict[key]=val
    return param_dict


# =============================================================================
# param
#   1
# =============================================================================
def main(args):
    module=args['module']
#    module ='scc'
    with open(ORDER_FILE,"r") as f:
        for line in f.read().strip().splitlines():
            param_dict = Read_Param(line)
            if Check_Param(module,param_dict):
                Set_Config(module,param_dict)
    with open(FINISH_FILE,'w+') as t:
        t.write('\n')
            
if __name__ == '__main__':
    args = catch_args()
    main(args)