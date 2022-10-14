<div>
    <pre>
各位老师好：
	[<b>${groupName}</b>]有如下需求评估时间超过14天，还望尽快安排处理。
<#if initString?? && (initString?size > 0)>
需求评估超期：
	<table border="1">
	<tr>
		<td>需求编号</td>
		<td>需求名称</td>
		<td>需求牵头人</td>
		<td>评估日期</td>
		<td>评估超期天数</td>
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

</pre>
</div>
