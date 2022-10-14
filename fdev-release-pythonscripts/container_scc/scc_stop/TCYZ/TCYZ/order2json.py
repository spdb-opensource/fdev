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
    parser1.add_argument('-g','--group')
    return  vars(parser1.parse_args())


def showError(flag1,content1):
    dict1 = {'I': 'INFO', 'W': 'WARN', 'E': 'ERROR'}
    time1 =datetime.datetime.now().strftime("%FT%T")
    if flag1 in dict1.keys():
        flag1 = dict1[flag1]
    print(time1 + ". " + flag1 + ". " + str(content1))
    if flag1 == 'ERROR':
        sys.exit(1)


def Set_Config(module,param_dict,group):
    f = open(BASE_CONFIG,'r')
    text = f.read()
    data = json.loads(text)
    f.close()
    data1 = Reset_Procedure(module,data)
    data2 = Set_Param(data1,param_dict)
    if not data2['metadata'].__contains__('group'):
        data2['metadata']['group'] = group
    if data2['metadata']['group'] == 'group':
        data2['metadata']['group'] = group
    if str(group) == 'None':
        if str(data2['metadata']['group']) == 'None':
            data2['metadata']['group'] = 'group'
#    print(data2)
    ##写到json文件里                
#    print(type(param_dict['obname']))
    target_file = str(data2['metadata']['name']) + "-" + str(data2['metadata']['namespace']) + "-" + str(data2['metadata']['cluster']) + "-" + str(data2['metadata']['group']) + "-" + str(data2['kind']) + "-" + str(data2['procedure']['checkAndDown']) + "-" + str(data2['procedure']['stopWorkload']) + "-" + str(data2['procedure']['applyYAML']) + "-" + str(data2['procedure']['updateImage']) + "-" + str(data2['procedure']['scaleReplicas']) + "-" + str(data2['procedure']['checkAndUp']) + ".json"
#    target_file = param_dict['obname'] + "-" + param_dict['namespace'] + "-" + param_dict['cluster']+ ".json"
    data3 = json.dumps(data2)
    with open(target_file,'w+') as f:
        f.write(data3)
    if not os.path.isfile(FINISH_FILE):
        with open(SORT_FILE,'a+') as s:
            s.write(target_file)
            s.write('\n')
#    
        
        
        
PARAM_CHECK_MAP={'scc':('tag','obname','imagename','imagespacename','cluster','namespace'),
                 'scc_all':('tag','obname','imagename','imagespacename','cluster','namespace'),
                 'scc_stopall':('obname','cluster','namespace'),
                 'scc_startall':('replicasnu','obname','cluster','namespace'),
                 'scc_scale':('replicasnu','obname','cluster','namespace'),
                 'scc_yaml':('tag','imagename','imagespacename','obyml','obname','cluster','namespace'),
                 'scc_restart':('obname','cluster','namespace'),
                 'scc_stop':('obname','cluster','namespace')}
def Check_Param(module,param_dict):
    if module not in PARAM_CHECK_MAP.keys():
        showError("E","{} not in scc scc_all scc_stopall scc_startall scc_scale scc_yaml scc_restart scc_stop".format(module))
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
        elif k == 'cluster':
            data['metadata']['cluster'] = v
        elif k == 'harboruri':
            data['metadata']['harboruri'] = v
        elif k == 'harboruser':
            data['metadata']['harboruser'] = v
        elif k == 'harborpwd':
            data['metadata']['harborpwd'] = v
        elif k == 'group':
            data['metadata']['group'] = v
        elif k == 'namespace':
            data['metadata']['namespace'] = v
        elif k == 'obyml':
            data['spec']['yamlPath'] = v
        elif k == 'stime':
            data['spec']['template']['spec']['scalingTime'] = int(v)
        elif k == 'replicasnu':
            data['spec']['replicas'] = int(v)
        elif k in ('checkAndDown','stopWorkload','applyYAML','updateImage','scaleReplicas','checkAndUp'):
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


PROCEDURE_SWITCH_MAP={'scc':{'checkAndDown':0,'checkAndUp':0,'updateImage':1},
                 'scc_all':{'updateImage':1},
                 'scc_stopall':{'stopWorkload':2},
                 'scc_startall':{'checkAndDown':0,'scaleReplicas':2,'checkAndUp':0},
                 'scc_scale':{'checkAndDown':0,'scaleReplicas':1,'checkAndUp':0},
                 'scc_yaml':{'checkAndDown':0,'applyYAML':1,'checkAndUp':0},
                 'scc_restart':{'checkAndDown':0,'scaleReplicas':3,'checkAndUp':0},
                 'scc_stop':{'stopWorkload':2}}
def Reset_Procedure(module,data):
    if module not in PROCEDURE_SWITCH_MAP.keys():
        showError("E","{} not in scc scc_all scc_stopall scc_startall scc_scale scc_yaml scc_restart scc_stop".format(module))
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
    group=args['group']
#    module ='scc'
    with open(ORDER_FILE,"r") as f:
        for line in f.read().strip().splitlines():
            param_dict = Read_Param(line)
            if Check_Param(module,param_dict):
                Set_Config(module,param_dict,group)
    with open(FINISH_FILE,'w+') as t:
        t.write('\n')
            
if __name__ == '__main__':
    args = catch_args()
    main(args)