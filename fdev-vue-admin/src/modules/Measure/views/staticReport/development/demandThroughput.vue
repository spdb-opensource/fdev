<template>
  <div>
    <f-block class="to-bottom q-pt-llg">
      <div class="row no-wrap">
        <!-- 开始日期 -->
        <f-formitem
          profile
          label="选择日期"
          label-style="width:56px;margin-right:32px"
          value-style="width:144px"
          class="q-mr-sm"
          ><f-date
            v-model="time.start_date"
            :options="startTimeOptions"
            ref="time.start_date"
            :rules="[val => !!val || '请选择开始日期']"
        /></f-formitem>
        <!-- 结束日期 -->
        <f-formitem
          profile
          label="至"
          label-style="width:14px;margin-right:8px"
          value-style="width:144px"
          ><f-date
            v-model="time.end_date"
            :options="endTimeOptions"
            ref="time.end_date"
            :rules="[val => !!val || '请选择结束日期']"
        /></f-formitem>
        <fdev-space class="q-pl-md" />
        <!-- 团队 -->
        <f-formitem
          profile
          label="团队"
          label-style="width:28px;margin-right:32px"
          value-style="max-width:280px"
          ><fdev-select
            use-input
            @filter="filterGroups"
            option-label="name"
            option-value="id"
            v-model="$v.groupObj.$model"
            multiple
            ref="groupObj"
            :rules="[val => !!val.length || '请选择团队']"
            :options="groupList"
            placeholder="请选择"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name">
                    {{ scope.opt.name }}</fdev-item-label
                  >
                  <fdev-item-label :title="scope.opt.fullName" caption>
                    {{ scope.opt.fullName }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template></fdev-select
          ></f-formitem
        >
        <fdev-space class="q-pl-sm" />
        <!-- 查询/导出按钮 -->
        <div class="row no-wrap">
          <fdev-btn
            ficon="search"
            dialog
            label="查询"
            @click="searchFunc"
            v-forbidMultipleClick
          />
          <fdev-btn
            ficon="exit"
            class="q-ml-md"
            dialog
            label="导出"
            :loading="btnLoading"
            @click="exportFunc('demandTable', '牵头投产需求')"
          />
        </div>
      </div>
      <!-- 是否展示日常需求数据 -->
      <f-formitem
        profile
        label="展示日常需求数据"
        bottom-page
        label-style="width:112px"
      >
        <fdev-toggle v-model="showDaily" left-label size="lg"
      /></f-formitem>
    </f-block>
    <f-block class="data-block q-pt-llg">
      <!-- 标题 -->
      <div>
        <f-icon
          name="list_s_f"
          class="text-primary"
          width="16px"
          height="16px"
        />
        <span class="title-font">牵头投产需求</span>
      </div>
      <Loading :visible="dataLoading">
        <!-- 有数据时 -->
        <div v-if="demandData && demandData.length" class="table-top">
          <table
            border="1"
            class="table-style"
            cellpadding="0"
            cellspacing="0"
            width="100%"
            id="demandTable"
          >
            <!-- 表体 -->
            <tr v-for="(item, index) in demandData" :key="index">
              <td class="td-style" v-for="(i, index) in item" :key="index">
                {{ i }}
              </td>
            </tr>
          </table>
        </div>
        <!-- 无数据时 -->
        <div v-else class="column items-center">
          <f-image name="no_data" class="q-mt-xl" /></div
      ></Loading>
    </f-block>
  </div>
</template>
<script>
import moment from 'moment';
import { mapActions, mapState } from 'vuex';
import { validate, deepClone, resolveResponseError } from '@/utils/utils';
import Loading from '@/components/Loading';
import { required } from 'vuelidate/lib/validators';
import { queryDemandThroughputStatistics } from '@/modules/Measure/services/methods.js';
export default {
  name: 'demandThroughput',
  components: { Loading },
  data() {
    return {
      time: {
        start_date: moment(new Date() - 24 * 60 * 60 * 30000).format(
          'YYYY-MM-DD'
        ), //开始时间
        end_date: moment(new Date()).format('YYYY-MM-DD') //结束时间
      }, //默认展示当前日期~往前30天数据
      cloneGroups: [],
      groupObj: [], //团队
      groupList: [], //下拉团队选项
      showDaily: false, //是否展示日常需求数据
      demandData: null, //返回的需求信息数据
      btnLoading: false, //下载按钮缓冲
      dataLoading: false //加载数据缓冲
    };
  },
  validations: {
    groupObj: {
      required
    },
    time: {
      end_date: {
        required
      },
      start_date: { required }
    }
  },
  watch: {
    showDaily(val) {
      this.searchFunc();
    }
  },
  computed: {
    ...mapState('userForm', ['groups']),
    ...mapState('user', ['currentUser'])
  },
  methods: {
    ...mapActions('userForm', ['fetchGroup']),
    async init() {
      //获取小组信息
      await this.fetchGroup();
      this.cloneGroups = deepClone(this.groups);
      this.groupList = this.cloneGroups;
      if (this.groupObj.length) {
        this.searchFunc();
      }
    },
    // 查询
    async searchFunc() {
      // 查询牵头投产需求
      let params = {
        startDate: this.time.start_date,
        endDate: this.time.end_date,
        groupIds: this.groupObj.map(item => item.id),
        showDaily: this.showDaily
      };
      if (this.checkGroupEmpty()) {
        this.dataLoading = true;
        try {
          this.demandData = await resolveResponseError(() =>
            queryDemandThroughputStatistics(params)
          );
          this.dataLoading = false;
        } catch (e) {
          this.dataLoading = false;
        }
      }
    },
    // 导出
    exportFunc(id, name) {
      this.btnLoading = true;
      import('@/utils/exportExcel').then(excel => {
        excel.export_html_table(id, name);
      });
      this.btnLoading = false;
    },
    // 开始日期范围控制
    startTimeOptions(date) {
      if (this.time.end_date) {
        return date < this.time.end_date.replace(/-/g, '/');
      }
      return true;
    },
    // 结束日期范围控制
    endTimeOptions(date) {
      this.time.start_date = this.time.start_date ? this.time.start_date : '';
      return date > this.time.start_date.replace(/-/g, '/');
    },
    filterGroups(val, update) {
      update(() => {
        this.groupList = this.cloneGroups.filter(v => {
          return v.fullName.includes(val) || v.name.includes(val);
        });
      });
    },
    // 校验团队是否输入
    checkGroupEmpty() {
      this.$v.$touch();
      validate(this.$refs);
      if (this.$v.$invalid) {
        return false;
      }
      return true;
    }
  },
  created() {
    this.dataLoading = true;
    // 默认查询用户所在组数据
    this.groupObj = [
      { id: this.currentUser.group_id, name: this.currentUser.group.name }
    ];
    this.init();
  }
};
</script>
<style lang="stylus" scoped>
.to-bottom
  margin-bottom:10px
  padding-bottom:28px
.data-block
  min-height:300px
.title-font
  font-family: PingFangSC-Medium;
  font-weight:600
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 16px;
  padding-left:12px
.table-top
  margin-top:20px
  margin-bottom:32px
table,tr,td{
  border:1px solid #ddd
  border-collapse:collapse !important
}
table tr td:first-child{
  width:127px
}
.table-style
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
  padding-left:32px;
  font-family: PingFangSC-Regular;
.td-style
  padding:20px;
</style>
