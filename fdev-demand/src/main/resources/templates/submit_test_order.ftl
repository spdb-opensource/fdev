<div class="testContent">
    <div>
      各位老师好;<br />
      &nbsp;&nbsp;${oa_contact_no}_${oa_contact_name}，现提业测，如有问题请在jira上登记,详情如下：<br />
    </div>
    <div style="height: 2em;"></div>
    <table class="testTable" border="1" >
      <tr>
        <td>需求名称</td>
        <td>${oa_contact_name}</td>
      </tr>
      <tr>
        <td>需求编号</td>
        <td>${oa_contact_no}</td>
      </tr>
      <tr>
        <td>计划提交业测日期</td>
        <td>${plan_test_start_date}</td>
      </tr>
      <tr>
        <td>拟纳入版本日期</td>
        <td></td>
      </tr>
      <tr>
        <td>测试内容</td>
        <td>
          ${test_content}
      </td>
      </tr>
      <tr>
        <td>FDEV研发单元</td>
        <td>${fdev_implement_unit_no}</td>
      </tr>
      <tr>
        <td>IPMP实施单元</td>
        <td>${impl_unit_num}</td>
      </tr>
      <tr>
        <td>是否涉及客户端更新</td>
        <td>${client_change}</td>
      </tr>
      <tr>
        <td>是否涉及交易接口改动</td>
        <td>${trans_interface_change}</td>
      </tr>
      <tr>
        <td>是否涉及数据库改动</td>
        <td>${database_change}</td>
      </tr>
      <tr>
        <td>是否涉及回归测试</td>
        <td>${regress_test}</td>
        </tr>
      <tr>
        <td>具体回归测试范围</td>
        <td>${regress_test_range}</td>
      </tr>
      <tr>
        <td>涉及关联系统同步改造</td>
        <td>${system}</td>
      </tr>
      <tr>
        <td>单元测试情况</td>
        <td>${unit_test_result}</td>
      </tr>
      <tr>
        <td>测试日报抄送</td>
        <td>${daily_cc_user_name}</td>
      </tr>
      <tr>
        <td>开发人员</td>
        <td>${developer}</td>
      </tr>
      <tr>
        <td>需求涉及的应用名称</td>
        <td>${app_name}</td>
      </tr>
      <tr>
        <td>备注</td>
        <td>${remark}</td>
      </tr>
      <tr>
         <td>提测用户</td>
         <td>${submit_user}</td>
      </tr>
    </table>
    <div style="height: 2em;"></div>
        测试环境:<br />
        &nbsp;&nbsp;${test_environment}<br />
        <div style="height: 2em;"></div>
        客户端下载地址(明确具体的测试包):<br />
        &nbsp;&nbsp;${client_download}<br />

  </div>
