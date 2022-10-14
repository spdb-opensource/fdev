<div>
    <pre>
  各位老师,你好:
    以下为任务 ${task.name} 进入REL阶段相关信息:
    production tag:
    <#list product_tag as tag>
        ${tag}
    </#list>
    production镜像:
    <#list pro_image_uri as image>
            ${image}
    </#list>
    REL环境:${rel_evn_name}
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
    其他系统变更:${Other_system}
    数据库变更:${Data_base_alter}
    公共配置文件更新:${commonProfile}
    安全测试:${securityTest}
    是否涉及特殊情况:${specialCase}
    <br><br>
</pre>
</div>
