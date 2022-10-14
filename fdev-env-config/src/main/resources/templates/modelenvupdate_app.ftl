<div>
    <pre>
各位老师好：
    <span>"${model_name_en}(${model_name_cn})"实体在${env_name_cn}环境的映射值已按照申请人${apply_username}将变动预录入，请在5天内点击该链接"${url}"进入fdev 环境配置模块核对确认录入值，核对未完成则实体变动无法生效，超过5天状态自动变成超期未核对。</span>
    <#assign model_name_cn = model_name_cn />
    <#assign env_name_cn = env_name_cn />

 申请的新增实体-环境映射的录入值：
    实体-属性字段--环境--变动前映射值--变动后映射值
    <#list updated as del>
        ${model_name_cn}--${del.name_en}--${env_name_cn}--${((del.oldValue!'')?length>0)?string((del.oldValue!''),"空白")}--${del.newValue}
     </#list>
 截止当前该变动的影响范围
    <#if (app?size>sendEmailAppSize)>
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
