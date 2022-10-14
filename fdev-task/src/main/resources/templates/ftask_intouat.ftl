<div>
    <pre>
  各位老师,你好:
    以下为任务 ${task.name} 进入UAT阶段相关信息:
    投产点:${release_node_name}
    release分支:${release_branch}
    UAT环境:${uat_evn_name}
    环境信息:
    profileName:${profileName}
    config 配置中心全路径:${SPRING_CLOUD_CONFIG_URI}
    宿主机日志映射目录:${hostLogsPath}
    eureka 1 地址:${eurekaServerUri}
    config 配置中心:${CI_CAAS_REGISTRY}
    CaaS 镜像仓库IP:${configServerUri}
    CaaS DCE IP:${CI_CAAS_IP}
    租户名:${CI_CAAS_TENANT}
    eureka 2 地址:${eureka1ServerUri}
    eureka 配置中心全路径:${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
    <br><br>
</pre>
</div>