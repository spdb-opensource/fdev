#!/usr/bin/python
# -*- coding: utf-8 -*-
import base64
import json


# 封装 caas 网络请求
import ssl
import urllib2
from urllib2 import URLError

import yaml


def request_caas(url,data='', method='POST'):
        return_data = {}
        headers = {"source": "back", "Content-Type": "application/json"}
        request = urllib2.Request(
            url=url,
            headers=headers,
            data=data,
        )
        request.get_method = lambda: method
        try:
            response = urllib2.urlopen(request, context=ssl._create_unverified_context(), timeout=60)
            return_data['data'] = response.read()
            return_data['code'] = response.getcode()
            print("code:", return_data.get('code'))
            print(return_data)
            return return_data
        except URLError as e:
            return_data['code'] = e.code
            print('服务器返回异常状态码')
            print('Error code: ', return_data.get('code'))
            print(e)
            return return_data


# 封装网络请求
def post_request(url, data):
    headers = {"Content-Type": "application/json",
               "source": "back"}

    print "headers:", headers
    print "data:", data
    print "method: POST"

    request = urllib2.Request(
        url=url,
        headers=headers,
        data=data,
    )
    request.get_method = lambda: 'POST'
    try:
        response = urllib2.urlopen(request, timeout=180)
        code = response.getcode()
        return_date = response.read()
        print "response:%s" % (json.loads(return_date))
        return json.loads(return_date)

    except URLError as e:
        print 1112

if __name__ == '__main__':
    url = "xxx/api/batchChange/queryIamsConfigByName"
    data = json.dumps({"appName": "mspmk-web-loan"})
    response_data = post_request(url, data)
    print (not response_data['data'] is None)
    if response_data["code"] == "AAAAAAA" and not response_data['data'] is None and len(response_data["data"]["esfConfig"]) > 0:
        print 111

    absolute_file_path = "scc-deployment.yaml"
    f = open(absolute_file_path)
    yaml_content = yaml.load(f)
    new_envs = []
    # envsList = yaml_content["spec"]["template"]["spec"]["containers"][0]['env']
    if "env" in yaml_content["spec"]["template"]["spec"]["containers"][0]:
        env_list = yaml_content["spec"]["template"]["spec"]["containers"][0]['env']
        for item in env_list:
            if item['name'] == "CFG_SVR_URL":
                print item['value']
            else:
                new_envs.append(item)

    print new_envs
    new_config = {}
    new_config["name"] = "CFG_SVR_URL"
    new_config["value"] = response_data["data"]["esfConfig"]
    new_envs.append(new_config)
    yaml_content["spec"]["template"]["spec"]["containers"][0]['env'] = new_envs
    print yaml_content
    # content = "{'respMsg': 'sas', 'respCode': '000', 'ad': '$<spdbservice_esb_info.domain_retail>', cc='dd' , esb.domain=$<spdbservice_esb_info.domain_retail>}"
    # a = content.replace("$<spdbservice_esb_info.domain_retail>", "xxx")
    # print a
    # url = "xxx/scc/deployment/curYaml?namespaceCode=effapp-sit&userGroupCode=spdb&resourceCode=eff-web-iams&resourceKind=Deployment&clusterCode=k8-phy-b01&userId=fdev"
    # response_data = request_caas(url)
    # print response_data["data"]
    # # yaml.load(json.loads(response_data['data'])["data"][50:])["spec"]["template"]["spec"]["containers"][0]['env']
    # # yaml_content = yaml.load(json.loads(response_data['data'])["data"][50:])
    # if json.loads(response_data['data'])["data"].startswith("apiVersion"):
    #     yaml_content = yaml.load(json.loads(response_data['data'])["data"])
    # else:
    #     yaml_content = yaml.load(json.loads(response_data['data'])["data"][50:])
    # envList = yaml_content["spec"]["template"]["spec"]["containers"][0]["env"]
    # print envList
    # newConfig = {}
    # newEnvs = []
    # for key, val in envList:
    #     print key
    # for item in envList:
    #     if item['name'] == "TEST_ESF_CONFIG":
    #         print item['value']
    #         newConfig["name"] = item['name']
    #         newConfig["value"] = "http://xxx"
    #     else:
    #         newEnvs.append(item)
    # newEnvs.append(newConfig)
    # yaml_content["spec"]["template"]["spec"]["containers"][0]["env"] = newEnvs
    # print yaml.dump(yaml_content)
    # FDEV_CAAS_SECRET = "33st3bwxizlanx5o6dl4bbhcduoywr4a3hn25q5w"
    # FDEV_CAAS_ACCESS = "q4yhjm6u"
    #
    # base64stringAccess = base64.b64encode('%s:%s' % (FDEV_CAAS_ACCESS, FDEV_CAAS_SECRET))
    # print base64stringAccess
    #
    # response = {'respMsg': 'sas', 'respCode': '000'}
    #
    # result_json = json.loads(response.get("data"))
    # # 发送api失败直接失败结束
    # if response.get("code") != 200 or result_json.get("respCode") != "000":
    #     print 123