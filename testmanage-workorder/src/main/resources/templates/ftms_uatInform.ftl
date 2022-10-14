<div>
    <pre>
各位老师好：
    需求编号：${rqrmntNo}  需求名称：${rqrmntName}  工单名：${mainTaskName}。现提交业务测试，${jira}详情如下：
<div>

<table border="1" class="tablecss">
<tr class="tr-height">
    <td class="td-width">实施单元编号/需求名称</td>
    <td width="700">${unit} / ${rqrmntName}</td>
</tr>
<tr class="tr-height">
    <td class="td-width">工单名称</td>
    <td width="700">${mainTaskName}</td>
</tr>
<tr class="tr-height">
    <td class="td-width">工单涉及任务</td>
    <td width="700">${taskList}</td>
</tr>
<tr class="tr-height">
    <td class="td-width">计划投产日期</td>
    <td width="700">${planProDate}</td>
</tr>
<tr class="tr-height">
    <td class="td-width">测试内容</td>
    <td width="700">${repair_desc}</td>
</tr>
<tr class="tr-height">
    <td class="td-width">回归测试范围</td>
    <td width="700">${regressionTestScope}</td>
</tr>
<tr class="tr-height">
    <td class="td-width">是否涉及交易接口改动</td>
    <td width="700">${interfaceChange}</td>
</tr>
<tr class="tr-height">
    <td class="td-width">是否涉及数据库改动</td>
    <td width="700">${databaseChange}</td>
</tr>
<tr class="tr-height">
    <td class="td-width">是否涉及客户端更新</td>
    <td width="700">${clientVersion}</td>
</tr>
<tr class="tr-height">
    <td class="td-width">测试环境</td>
    <td width="700">${testEnv}</td>
</tr>
<tr class="tr-height">
    <td class="td-width">需求涉及的应用名称</td>
    <td width="700">${appName}</td>
</tr>
<tr class="tr-height">
    <td class="td-width">涉及关联系统同步改造</td>
    <td width="700">${otherSystemChange}</td>
</tr>
<tr class="tr-height">
    <td class="td-width">开发人员</td>
    <td width="700">${developer}</td>
</tr>
<tr class="tr-height">
    <td class="td-width">缺陷编写规则</td>
    <td width="700">1、概要：请以“需求联系单号 需求名_问题描述”的格式提交，如：dzqdUT-LSYW-2019-0036-N001汇理财页面部分字段优化需求_页面保证收益标签应由保本收益变为保本浮动收益。需求联系单号一般在提测邮件包含，如测试内容不涉及需求联系单号可用测试原由简述代替，如：全局流水号改造_提交跨行汇款报错。2、缺陷描述：1.操作步骤，预计结果，实际结果等。 3、附件：一般应有缺陷截图，如确实无法截图，应在备注里注明。</td>
</tr>
<tr class="tr-height">
    <td class="td-width">备注</td>
    <td width="700">${uatRemark}</td>
</tr>
</table>

${file}
</div>
</pre>
</div>
