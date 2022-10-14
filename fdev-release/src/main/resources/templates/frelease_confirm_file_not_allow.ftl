<div>
    <pre>
各位相关负责老师好：
    当前${release_date!''}投产大窗口有以下需求的任务仍未收到上线确认书，将不予投产，请及时修改任务的投产意向窗口日期和更换投产窗口，取消投产！
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
