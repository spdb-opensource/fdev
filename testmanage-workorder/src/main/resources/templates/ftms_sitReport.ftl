<div>
    <pre>
各位老师好：
    需求编号：${rqrmntNo}  工单名：${mainTaskName}已完成内部测试。详情如下：
<div>
<table border="1">
<tr class="tr-height">
    <td class = "td-width2">工单名</td>
    <td colspan="3">${mainTaskName}</td>
</tr>
<tr class="tr-height">
    <td>需求编号/需求名称</td>
    <td colspan="3">${rqrmntNo} / ${rqrmntName}</td>
</tr>
<tr class="tr-height">
    <td>实施单元编号</td>
    <td>${unit}</td>
    <td>工单编号</td>
    <td>${workNo}</td>
</tr>
<tr class="tr-height">
    <td>工单涉及任务</td>
    <td colspan="3">${taskList}</td>
</tr>
<tr class="tr-height">
    <td>测试小组长</td>
    <td>${groupLeader}</td>
    <td>测试人员</td>
    <td>${testers}</td>
</tr>
<tr class="tr-height">
    <td>测试结果</td>
    <td colspan="3">${testResult}</td>
</tr>
<tr class="tr-height">
    <td>测试范围</td>
    <td colspan="3">${testRange}</td>
</tr>
<#list planData as data>
<tr class="tr-height">
    <td class = "td-width2">测试计划</td>
    <td colspan="3">${data.planName}</td>
</tr>
<tr class="tr-height">
    <td>测试案例总数</td>
    <td>${data.allCase!'0'}</td>
    <td>执行通过数</td>
    <td>${data.sumSucc!'0'}</td>
</tr>
<tr class="tr-height">
    <td>案例执行失败数</td>
    <td>${data.sumFail!'0'}</td>
    <td>案例执行阻碍数</td>
    <td>${data.sumBlock!'0'}</td>
</tr>
<tr class="tr-height">
    <td>有效缺陷数</td>
    <td>${data.validMantis!'0'}</td>
    <td>无效缺陷数</td>
    <td>${data.braceMantis!'0'}</td>
</tr>
<tr class="tr-height">
    <td>设备信息</td>
    <td>${data.deviceInfo}</td>
    <td>无效用例数</td>
    <td>${data.invalidCase!'0'}</td>
</tr>
<tr class="tr-height">
    <td>计划开始时间</td>
    <td>${data.planStartDate?string("yyyy年MM月dd日")}</td>
    <td>计划结束时间</td>
    <td>${data.planEndDate?string("yyyy年MM月dd日")}</td>
</tr>
</#list>

</table>
    案例截图链接地址为：${imageLink}
</div>
</pre>
</div>
