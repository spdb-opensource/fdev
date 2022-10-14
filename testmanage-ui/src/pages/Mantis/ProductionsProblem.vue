<template>
  <div class="page-container">
    <div class="form">
      <el-select
        v-model="listModel.module"
        placeholder="所属板块"
        @keyup.enter.native="init"
        filterable
        clearable
      >
        <el-option
          v-for="(group, i) in moduleOptions"
          :key="i"
          :label="group.fullName"
          :value="group.id"
        />
      </el-select>
      <el-select
        v-model="listModel.responsible_name_en"
        placeholder="请选择负责人"
        @keyup.enter.native="init"
        filterable
        clearable
      >
        <el-option
          v-for="user in userOptions"
          :key="user.id"
          :value="user.user_name_en"
          :label="user.user_name_cn"
        >
          <span style="float: left">{{ user.user_name_cn }}</span>
          <span style="float: right">{{ user.user_name_en }}</span>
        </el-option>
      </el-select>
      <el-select
        v-model="listModel.deal_status"
        placeholder="请选择处理状态"
        @keyup.enter.native="init"
        clearable
      >
        <el-option
          v-for="item in dealStatusOptions"
          :key="item"
          :value="item"
          :label="item"
        />
      </el-select>
      <el-select
        v-model="listModel.issue_level"
        label="生产问题级别"
        placeholder="请选择生产问题级别"
        @keyup.enter.native="init"
        clearable
      >
        <el-option
          v-for="item in issueLevelOptions"
          :key="item.value"
          :value="item.label"
          :label="item.label"
        />
      </el-select>
      <el-date-picker
        v-model="date"
        value-format="yyyy-MM-dd"
        format="yyyy-MM-dd"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        :picker-options="pickerOptions"
      />
      <el-button type="primary" @click="filterData">
        查询
      </el-button>
    </div>

    <el-table class="table" :data="tableData">
      <el-table-column prop="id" label="缺陷编号"></el-table-column>
      <el-table-column prop="work_no" label="工单号"></el-table-column>
      <el-table-column prop="module" label="所属板块"></el-table-column>
      <el-table-column prop="issue_type" label="问题类型"></el-table-column>
      <el-table-column prop="discover_stage" label="发现阶段"></el-table-column>
      <el-table-column
        prop="test_responsible"
        label="内测责任人"
      ></el-table-column>
      <el-table-column
        prop="issue_level"
        label="生产问题级别"
      ></el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current_page"
        :page-sizes="[5, 10, 50, 100, 200, 500]"
        :page-size="pagination.page_size"
        layout="sizes, prev, pager, next, jumper"
        :total="pagination.total"
      />
    </div>
  </div>
</template>

<script>
import { listModel, dealStatusOptions, issueLevelOptions } from './model';
import { queryProIssues, countProIssues } from '@/services/mantis';
import { mapActions, mapState } from 'vuex';
export default {
  name: 'ProductionsProblem',
  data() {
    return {
      tableData: [],
      date: [],
      moduleOptions: [],
      userOptions: [],
      issueLevelOptions: issueLevelOptions,
      dealStatusOptions: dealStatusOptions,
      filterModel: {
        ...listModel(),
        module: []
      },
      listModel: listModel(),
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        }
      },
      pagination: {
        current_page: 1,
        page_size: 10,
        total: 0
      }
    };
  },
  watch: {
    date(val) {
      if (val) {
        this.listModel.start_time = val[0];
        this.listModel.end_time = val[1];
      } else {
        this.listModel.start_time = '';
        this.listModel.end_time = '';
      }
    }
  },
  computed: {
    ...mapState('mantisForm', ['userList', 'fdevGroup'])
  },
  methods: {
    ...mapActions('mantisForm', ['queryUser', 'queryFdevGroup']),
    handleSizeChange(size) {
      this.pagination.page_size = size;
      this.init();
    },
    handleCurrentChange(size) {
      this.pagination.current_page = size;
      this.init();
    },
    filterData() {
      this.filterModel = {
        ...this.listModel,
        module: this.listModel.module ? [this.listModel.module] : [],
        isIncludeChildren: false
      };
      this.pagination.current_page = 1;
      this.init();
    },
    async init() {
      const params = {
        ...this.filterModel,
        ...this.pagination
      };
      Promise.all([countProIssues(params), queryProIssues(params)]).then(
        res => {
          this.pagination.total = res[0] / 1;
          this.tableData = res[1];
        }
      );
    }
  },
  async created() {
    this.init();
    await this.queryUser();
    this.userOptions = this.userList;
    await this.queryFdevGroup();
    this.moduleOptions = this.fdevGroup.filter(item => item.status === '1');
  }
};
</script>

<style scoped>
.form .el-select {
  margin-right: 5px;
  margin-bottom: 10px;
  width: 20%;
}
.form .el-date-editor {
  width: calc(40% + 10px);
  margin-right: 5px;
}
.table {
  width: 100%;
}
.pagination {
  text-align: right;
  margin-top: 15px;
}
</style>
