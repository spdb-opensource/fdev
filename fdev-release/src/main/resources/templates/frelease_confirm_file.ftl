<div>
    <pre>
各位相关负责老师好：
    当前${release_date!''}投产大窗口有以下需求的任务尚未收到上线确认书，请尽快确认业务已发出上线确认书并通过相关审批流程！
    如已到达，请及时在任务详情页面打开”上线确认书情况“开关，并上传上线确认书至 关联文档> 投产类> 变更材料类！
         <table border="1">
         <tr class="tr-height">
         <td>需求编号</td>
         <td>需求名称</td>
         <td>任务名称</td>
         <td>任务负责人</td>
         <td>行内负责人</td>
         <td>投产联系人</td>
         </tr>
        <#list ftask as data>
        <tr>
             <td>${data.rqrmntNo!''}</td>
             <td>${data.rqrmntName!''}</td>
             <td>${data.task_name!''}</td>
             <td>${data.dev_managers!''}</td>
             <td>${data.spdb_managers!''}</td>
             <td>${data.release_contact!''}</td>
         </tr>
        </#list>
        </table>
        <br><br>
    </pre>
</div>
