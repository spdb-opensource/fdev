<div>
    <pre>
各位老师好：
     申请以下设备从spdb-kf白名单上移除，相应信息如下，烦请老师协助处理，谢谢！
    <br><br>
</pre>
<table cellpadding="10" cellspacing="0" border="1">
    <thead>
        <tr>
            <th>源服务器名称</th>
            <th>申请类型</th>
            <th>MAC地址</th>
            <th>使用人</th>
            <th>使用人员类别</th>
            <th>使用人公司</th>
            <th>使用人域用户</th>
            <th>申请日期</th>
        </tr>
    </thead>
    <#list approvalList as approval>
        <tr>
            <td>${approval.phone_type!}</td>
            <td>关闭spdb-kf网络</td>
            <td>${approval.phone_mac!}</td>
            <td>${approval.user.user_name_cn!}</td>
            <td>
                <#if approval.user.is_spdb>
                                                        行内
                <#else>
                                                        厂商 
                </#if>
            </td>
            <td>${approval.user.company.name!}</td>
            <td>
                <#if approval.user.is_spdb>
                   hdqsmsg01\\${approval.user.vm_user_name!}
                <#else>
                   dev\\${approval.user.email!}
                </#if>
            </td>
            <td>${approval.create_time!}</td>
        </tr>
    </#list>
</table>
</div>
