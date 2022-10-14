<div>
    <pre>
各位老师好：
	本周 <b>${startDate}</b> -- <b>${endDate}</b>，[<b>${groupName}</b>]有如下需求即将到期，还望尽快安排处理。
<#if initString?? && (initString?size > 0)>
计划启动提醒:
	<table border="1">
	<tr>
		<td>需求编号</td>
		<td>需求名称</td>
		<td>需求科技负责人</td>
		<td>计划启动日期</td>
		<td>还剩几天延期</td>
	</tr>
	<#list initString as rqrmnt>
	<tr>
		<td>${rqrmnt.demandNo}</td>
		<td>${rqrmnt.demandName}</td>
		<td>${rqrmnt.appendUserCn}</td>
		<td>${rqrmnt.date}</td>
		<td>${rqrmnt.countDay}</td>
	</tr>
	</#list>
	</table>
</#if>
<#if sitString?? && (sitString?size > 0)>
计划提测提醒：
	<table border="1">
	<tr>
		<td>需求编号</td>
		<td>需求名称</td>
		<td>需求科技负责人</td>
		<td>计划提测日期</td>
		<td>还剩几天延期</td>
	</tr>
	<#list sitString as rqrmnt>
	<tr>
		<td>${rqrmnt.demandNo}</td>
		<td>${rqrmnt.demandName}</td>
		<td>${rqrmnt.appendUserCn}</td>
		<td>${rqrmnt.date}</td>
		<td>${rqrmnt.countDay}</td>
	</tr>
	</#list>
	</table>
(注: 计划提测提醒:  需求已进入实际开发阶段,但未提测! )
</#if>
    <br><br>
	</pre>
</div>
