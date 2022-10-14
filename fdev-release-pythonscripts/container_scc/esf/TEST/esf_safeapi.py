import json
import sys
import datetime
import argparse
import urllib.request
import urllib.parse
import yaml


ORDER_FILE = r'order.txt'


def showError(flag1, content1):
    dict1 = {'I': 'INFO', 'W': 'WARN', 'E': 'ERROR'}
    time1 = datetime.datetime.now().strftime("%FT%T")
    if flag1 in dict1.keys():
        flag1 = dict1[flag1]
    print(time1 + ". " + flag1 + ". " + str(content1))
    if flag1 == 'ERROR':
        sys.exit(1)

def Read_Param(line_str):
    param_dict ={}
    for k_v in line_str.strip().split('%'):
        tmp = k_v.split(':')
        key = tmp[0]
        val = tmp[1]
        param_dict[key] = val
    return param_dict

# def Merge(dict1, ):
#     return(dict2.udict2pdate(dict1))

def catch_args():
    parser1 = argparse.ArgumentParser()
    parser1.add_argument('-u', '--url')
    parser1.add_argument('-g', '--gkname')
    parser1.add_argument('-i', '--user')
    parser1.add_argument('-p', '--password')
    parser1.add_argument('-c', '--check')
    parser1.add_argument('-s', '--sids')
    return vars(parser1.parse_args())


def Check_Param(param_dict):
    flag=True
    list1=['sid','filename']
    for name in list1:
        if name  not in param_dict.keys():
            flag=False
            showError('E','missing must factor {} in order.txt'.format(name))
    return flag



def Find_gk(dict1):
    filename=dict1['filename']
    f= open(filename, 'r',encoding='utf-8')
    data = yaml.load(f,Loader=yaml.FullLoader)
    f.close()
    env_list=data['spec']['template']['spec']['containers'][0]['env']
    env=len(env_list)
    dict2={}
    for i in range(env):
        dict2[env_list[i]['name']]=env_list[i]['value']
    if dict1['gkname'] not in dict2.keys():
       showError('E','missing must value gkvalue')
    else:
        dict1['gkvalue']=dict2[dict1['gkname']]

def safeapi_requests(dict1):
    head={}
    head['Content-Type']='application/json'
    head['sid']=dict1['sid']
    head['cipherGk']=dict1['gkvalue']
    head['account']=dict1['account']
    head['password']=dict1['password']
    url_request(dict1['url'],head)

def url_request(url,head):
    response = urllib.request.Request(url,headers=head,method='POST')
    try:
        resp = urllib.request.urlopen(response)
        result = resp.read().decode('utf-8')
        listaa=result.split(' ')
        if listaa[0]=='0':
            showError('I', 'sent successfully'+result)
        else:
            showError('E', 'sent failture'+result)
    except urllib.request.HTTPError as e:
        a = json.loads(e.read().decode('utf-8'),encoding='utf-8')
        showError('E',a)

def main(args):
    with open(ORDER_FILE, "r") as f:
        for line in f.read().strip().splitlines():
            param_dict = Read_Param(line)
            showError('I','begin to start execut {}'.format(param_dict['sid']))
            if Check_Param(param_dict):
                param_dict['url']=args['url']
                param_dict['gkname'] = args['gkname']
                showError('I','begin to find gkvalue in {}'.format(param_dict['filename']))
                Find_gk(param_dict)
                check=args['check']
                if check !=None:
                    if int(check) == 0:
                        if 'account' not in param_dict.keys():
                            param_dict['account'] = args['user']
                            param_dict['password'] = args['password']
                    elif int(check) == 1:
                        param_dict['account'] = args['user']
                        param_dict['password'] = args['password']
                systemsids=args['sids']
                if systemsids !=None:
                    sid=param_dict['sid']
                    systemid=sid[0:4]
                    if systemid  not in systemsids:
                        showError('E','systemid is not in systemsids'+ systemid)
                showError('I','begin to send')
                safeapi_requests(param_dict)
        showError('I','All finished ')
if __name__ == '__main__':
    args = catch_args()
    main(args)

