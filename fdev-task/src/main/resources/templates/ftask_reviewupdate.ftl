<div>
    <pre>
          任务负责人,DBA审核员,你好：
                任务: ${taskName} 数据库变更审核项 有了最新进展：
                需求编号：${rqrmntNum}
                数据库类型变更为：${db_type}
                相关库表变更是否涉及通知数据仓库等关联供数系统配套改造: ${system_remould}
                是否涉及在库表关联应用暂停服务期间实施数据库变更操作: ${impl_data}
                申请人：${applicant}
                <#if init_auditor ??>
                初审人：${init_auditor}
                </#if>
                计划投产日期： ${plan_fire_time}
                审核状态：${reviewStatus}
                <#if opinion ??>
                审核补充意见：${opinion}
                </#if>
                <#if reviewRecord ??>
                审核记录：
                ${reviewRecord}
                </#if>
                <#if reason ??>
                拒绝原因：${reason}
                </#if>
                审核项链接: ${href}
    <br><br>
    </pre>
</div>