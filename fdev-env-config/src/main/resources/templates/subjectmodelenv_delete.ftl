<div>
    <pre>
各位老师好：
	<span>${env_name_en}${"("+env_name_cn+")"}环境与${model_name_en}(${model_name_cn})实体的映射信息已被环境配置管理员删除，请相关负责人关注，如涉及生产环境配置映射值变化，请安排排期投产，详细内容请移步fdev进行操作。</span>
    <#assign model_name_en = model_name_en />
    <#assign model_name_cn = model_name_cn />
    <#assign env_name_en = env_name_en />
    删除的属性映射值有：
        属性字段--属性字段中文名--环境英文--字段映射值：
     <#list updated as data>
        ${data.name_en}--${data.name_cn}--${env_name_en}--${data.value}
     </#list>

    当前版本：${version}

    <#if (app?size>sendEmailAppSize) >
    涉及到应用见附件:
    <#else>
    涉及到应用如下：
    <#list app as entity>
        应用英文名：${entity.name_en}
        应用中文名：${entity.name_zh}
        行内应用负责人及应用负责人：${entity.user_name_cn}
        分支所涉及的实体属性如下：
                 分支--实体属性--中文名：
        <#list entity.branch  as branch>
           <#assign branchMap = branch/>
           <#assign keys = branchMap ? keys/>
           <#list keys as key>
             <#list branchMap[key]  as env_key>
                 ${key}--${model_name_en}.${env_key.name_en}--${env_key.name_cn};
             </#list>
            </#list>
        </#list>
    </#list>
    </#if>
    <br><br>
</pre>
</div>
