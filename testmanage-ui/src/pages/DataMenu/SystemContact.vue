<template>
  <div class="page-container">
    <el-container>
      <el-header style="margin-left:20px;">
        <el-form :inline="true" class="demo-form-inline">
          <el-col :span="8">
            <el-form-item label="搜索：">
              <el-input
                v-model="search"
                placeholder="请输入搜索内容"
                clearable
              ></el-input>
            </el-form-item>
          </el-col>
        </el-form>
      </el-header>
      <el-main style="padding:20px" class="systemContactTable">
        <el-table
          stripe
          :data="tables"
          tooltip-effect="dark"
          style="width: 100%;color:black"
          :header-cell-style="{ color: '#545c64' }"
        >
          <el-table-column
            type="index"
            label="序号"
            width="120"
          ></el-table-column>
          <el-table-column prop="stripLine" label="处室/条线"></el-table-column>
          <el-table-column prop="systemName" label="系统名称"></el-table-column>
          <el-table-column
            prop="contacts"
            label="联系人（A/B)"
          ></el-table-column>
        </el-table>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'SystemContact',

  data() {
    return {
      SystemContactData: [
        {
          stripLine: '个金条线',
          systemName: '网贷、零售信贷',
          contacts: 'xxx'
        },
        {
          stripLine: '个金条线',
          systemName: '黄金交易管理系统',
          contacts: 'xxx'
        },
        {
          stripLine: '个金条线',
          systemName: '财富E站',
          contacts: 'xxx'
        },
        { stripLine: '公共渠道', systemName: 'ESB', contacts: '刘明辉/胡彭勇' },
        {
          stripLine: '公司业务条线',
          systemName: '商业汇票系统',
          contacts: 'xxx'
        },
        {
          stripLine: '公司业务条线',
          systemName: '本币支付清算',
          contacts: 'xxx'
        },
        {
          stripLine: '公司业务条线',
          systemName: '外币支付清算',
          contacts: 'xxx'
        },
        {
          stripLine: '管理信息条线',
          systemName: '企业在线融资',
          contacts: 'xxx'
        },
        {
          stripLine: '管理信息条线',
          systemName: '企业信贷系统',
          contacts: 'xxx'
        },
        {
          stripLine: '合肥开发',
          systemName: 'ECIF企业级客户信息管理系统',
          contacts: 'xxx'
        },
        {
          stripLine: '核心平台处\n公司条线\n个金条线',
          systemName: '核心产品化系统',
          contacts: 'xxx'
        },
        {
          stripLine: '信用卡业务',
          systemName: '信用卡发卡系统',
          contacts: 'xxx'
        },
        {
          stripLine: '中间业务条线',
          systemName: '理财业务销售系统',
          contacts: 'xxx'
        },
        {
          stripLine: '中间业务条线',
          systemName: '银基通系统',
          contacts: 'xxx'
        },
        {
          stripLine: '互联网条线',
          systemName: '个人手机',
          contacts: 'xxx'
        },
        {
          stripLine: '互联网条线',
          systemName: '个人网银',
          contacts: 'xxx'
        },
        {
          stripLine: '互联网条线',
          systemName: '公司网银',
          contacts: 'xxx'
        },
        {
          stripLine: '互联网条线',
          systemName: '电子商务支付',
          contacts: 'xxx'
        }
      ],
      search: ''
    };
  },
  methods: {},
  computed: {
    tables() {
      const search = this.search;
      if (search) {
        return this.SystemContactData.filter(data => {
          return Object.keys(data).some(key => {
            if (
              String(data[key])
                .toLowerCase()
                .indexOf(search) > -1
            ) {
              return (
                String(data[key])
                  .toLowerCase()
                  .indexOf(search) > -1
              );
            } else {
              return String(data[key]).indexOf(search) > -1;
            }
          });
        });
      }
      return this.SystemContactData;
    }
  }
};
</script>

<style>
.systemContactTable .el-table .cell {
  white-space: pre-line !important;
}
</style>
