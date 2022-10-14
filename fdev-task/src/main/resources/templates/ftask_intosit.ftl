<div text-align:center>
    <pre>
  各位老师,你好:
     以下内容为任务 ${task.name} 进入SIT阶段的环境信息:
     sit环境:
     环境信息:
         profileName:${auto.profileName}
         config 配置中心全路径:${auto.SPRING_CLOUD_CONFIG_URI}
         宿主机日志映射目录:${auto.hostLogsPath}
         eureka 1 地址:${auto.eurekaServerUri}
         config 配置中心:${auto.CI_CAAS_REGISTRY}
         CaaS 镜像仓库IP:${auto.configServerUri}
         CaaS DCE IP:${auto.CI_CAAS_IP}
         租户名:${auto.CI_CAAS_TENANT}
         eureka 2 地址:${auto.eureka1ServerUri}
         eureka 配置中心全路径:${auto.EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
     环境信息:
         profileName:${schedule.profileName}
         config 配置中心全路径:${schedule.SPRING_CLOUD_CONFIG_URI}
         宿主机日志映射目录:${schedule.hostLogsPath}
         eureka 1 地址:${schedule.eurekaServerUri}
         config 配置中心:${schedule.CI_CAAS_REGISTRY}
         CaaS 镜像仓库IP:${schedule.configServerUri}
         CaaS DCE IP:${schedule.CI_CAAS_IP}
         租户名:${schedule.CI_CAAS_TENANT}
         eureka 2 地址:${schedule.eureka1ServerUri}
         eureka 配置中心全路径:${schedule.EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}

    <br><br>
</pre>
</div>
