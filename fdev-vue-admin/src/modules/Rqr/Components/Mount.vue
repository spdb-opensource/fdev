<template>
  <div>
    <f-dialog
      v-model="open"
      right
      persistent
      title="挂载实施单元"
      @hide="confirmToClose"
    >
      <div class="q-gutter-y-diaLine rdia-dc-w row justify-between">
        <fdev-table
          :data="tableData"
          :columns="columns"
          class="row-item"
          :pagination.sync="pagination"
          title="实施单元列表"
          titleIcon="list_s_f"
          :rows-per-page-options="perPageOptions"
          @request="pageUnitList"
          row-key="name"
          selection="single"
          :selected.sync="selectedRow"
          noExport
        >
          <template class="row" v-slot:top-bottom>
            <f-formitem class="col-6" label="实施单元内容">
              <fdev-input v-model="ipmpContent" @keyup.enter="findUnitList">
              </fdev-input>
            </f-formitem>
            <f-formitem class="col-6" label="项目编号">
              <fdev-input v-model="projectNo" @keyup.enter="findUnitList">
              </fdev-input>
            </f-formitem>
            <f-formitem class="col-6" label="实施单元编号">
              <fdev-input v-model="ipmpNo" @keyup.enter="findUnitList">
              </fdev-input>
            </f-formitem>
            <f-formitem class="col-6" label="实施单元牵头人">
              <fdev-select
                use-input
                v-model="Ipmpleader.user_name_cn"
                :options="userFilterOptions"
                @input="findUnitList($event)"
                @keyup.native="findUnitList(500)"
                @filter="userFilter"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.user_name_cn">
                        {{ scope.opt.user_name_cn }}
                      </fdev-item-label>
                      <fdev-item-label
                        :title="
                          scope.opt.user_name_en + '--' + scope.opt.group.name
                        "
                        caption
                      >
                        {{ scope.opt.user_name_en }}--{{ scope.opt.group.name }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
          </template>
          <template v-slot:body-selection="scope">
            <fdev-tooltip v-if="isCheckbox(scope.row)">
              {{ scope.row.isMountTip }}
            </fdev-tooltip>
            <!-- :disable="isCheckbox(scope.row)" -->
            <fdev-checkbox
              :disable="isCheckbox(scope.row)"
              v-model="scope.row.selected"
              @input="selectionFun(scope.row)"
            >
            </fdev-checkbox>
          </template>
        </fdev-table>
      </div>

      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="confirmToClose"/>
        <fdev-btn :label="label" dialog @click="mountAdd"
      /></template>
    </f-dialog>
    <f-dialog
      v-model="nextDialogOpen"
      title="修改研发单元时间"
      @before-show="initQuery()"
      hide="confirmNextClose"
    >
      <!--计划启动日期-->
      <f-formitem required label="计划启动日期" diaS>
        <f-date
          v-model="$v.editUnitModel.plan_start_date.$model"
          ref="editUnitModel.plan_start_date"
          :options="planStartDateOptions"
          mask="YYYY-MM-DD"
          :rules="[
            () =>
              $v.editUnitModel.plan_start_date.required || '请输入计划启动日期',
            () =>
              $v.editUnitModel.plan_start_date.regRule ||
              '不能晚于实施单元计划启动开发日期'
          ]"
        >
        </f-date>
      </f-formitem>

      <!--计划提交内测日期-->
      <f-formitem required label="计划提交内测日期" diaS>
        <f-date
          v-model="$v.editUnitModel.plan_inner_test_date.$model"
          ref="editUnitModel.plan_inner_test_date"
          :options="planInnerTestDateOptions"
          mask="YYYY-MM-DD"
          :rules="[
            () =>
              $v.editUnitModel.plan_inner_test_date.required ||
              '请输入计划提交内测日期',
            () =>
              $v.editUnitModel.plan_inner_test_date.regRule ||
              '不能晚于实施单元计划提交内测日期'
          ]"
        >
        </f-date>
      </f-formitem>

      <!--计划提交用户测试日期-->
      <f-formitem required label="计划提交用户测试日期" diaS>
        <f-date
          v-model="$v.editUnitModel.plan_test_date.$model"
          ref="editUnitModel.plan_test_date"
          :options="planTestDateOptions"
          mask="YYYY-MM-DD"
          :rules="[
            () =>
              $v.editUnitModel.plan_test_date.required ||
              '请输入计划提交用户测试日期',
            () =>
              $v.editUnitModel.plan_test_date.regRule ||
              '不能晚于实施单元计划提交用户测试日期'
          ]"
        >
        </f-date>
      </f-formitem>

      <!--计划用户测试完成日期-->
      <f-formitem label="计划用户测试完成日期" diaS>
        <f-date
          v-model="editUnitModel.plan_test_finish_date"
          :options="planTestFinishDateOptions"
          mask="YYYY-MM-DD"
          hint=""
        >
        </f-date>
      </f-formitem>

      <!--计划投产日期-->
      <f-formitem label="计划投产日期" diaS>
        <f-date
          v-model="editUnitModel.plan_product_date"
          :options="planProductDateOptions"
          mask="YYYY-MM-DD"
        >
        </f-date>
      </f-formitem>

      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="confirmNextClose"/>
        <fdev-btn label="挂载" dialog @click="mountImplUnit"
      /></template>
    </f-dialog>
  </div>
</template>

<script>
import {
  queryUserOptionsParams,
  ipmpUnitColumns,
  unitVisibleColumns
} from '../model';
import { mapState, mapActions } from 'vuex';
import {
  formatOption,
  successNotify,
  validate,
  errorNotify
} from '@/utils/utils';
import { formatUser } from '@/modules/User/utils/model';
import moment from 'moment';
import { required } from 'vuelidate/lib/validators';
export default {
  name: 'Mount',

  props: {
    mountIpmp: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      timer: null, // '计时器'
      open: true,
      Ipmpleader: {},
      ipmpContent: '', //实施单元
      ipmpNo: '', //实施单元编号
      projectNo: '', //项目编号
      userFilterOptions: [],
      selectedRow: [],
      selectedDetail: null,
      tableData: [],
      columns: ipmpUnitColumns,
      loading: false,
      perPageOptions: [5, 10, 15, 20, 30, 50, 100],
      pagination: {
        sortBy: '', //排序
        page: 1, //页码
        rowsPerPage: 5, //每页数据大小
        rowsNumber: 0 //数据库数据总条数
      },

      isMountTip: '',
      nextDialogOpen: false,
      editUnitModel: {
        plan_start_date: '',
        plan_inner_test_date: '',
        plan_test_date: '',
        plan_test_finish_date: '',
        plan_product_date: ''
      },
      isDisable: false,
      visibleColumns: unitVisibleColumns
    };
  },
  validations: {
    editUnitModel: {
      plan_start_date: {
        required,
        regRule(val) {
          if (
            this.selectedRow.length > 0 &&
            this.selectedRow[0].planDevelopDate &&
            this.dateFormat(val) >
              this.dateFormat(this.selectedRow[0].planDevelopDate)
          ) {
            return false;
          } else {
            return true;
          }
        }
      },
      plan_inner_test_date: {
        required,
        regRule(val) {
          if (
            this.selectedRow.length > 0 &&
            this.selectedRow[0].planInnerTestDate &&
            this.dateFormat(val) >
              this.dateFormat(this.selectedRow[0].planInnerTestDate)
          ) {
            return false;
          } else {
            return true;
          }
        }
      },
      plan_test_date: {
        required,
        regRule(val) {
          if (
            this.selectedRow.length > 0 &&
            this.selectedRow[0].planTestStartDate &&
            this.dateFormat(val) >
              this.dateFormat(this.selectedRow[0].planTestStartDate)
          ) {
            return false;
          } else {
            return true;
          }
        }
      }
      // plan_test_finish_date: {
      //   required
      // },
      // plan_product_date: {
      //   required
      // }
    }
  },
  // watch: {
  //   editUnitModel: {
  //     handler: function(newValue, oldValue) {
  //       this.clearDateFun(newValue);
  //     },
  //     deep: true
  //   }
  // },
  computed: {
    ...mapState('userForm', {
      userInPage: 'userInPage'
    }),
    ...mapState('demandsForm', {
      ipmpUnitListTable: 'ipmpUnitListTable'
    }),
    label() {
      // if (this.mountIpmp.implement_unit_status_normal !== 1) {
      //   return '挂载';
      // } else {
      //   return '下一步';
      // }
      return '下一步';
    },
    columnsOptions() {
      const columns = this.columns.slice();
      return columns.splice(1, columns.length - 1);
    }
  },
  created() {
    this.initDialogData();
  },
  methods: {
    ...mapActions('userForm', {
      queryGroup: 'fetchGroup',
      queryUserPagination: 'queryUserPagination'
    }),
    ...mapActions('demandsForm', ['queryIpmpUnitByDemandId', 'mount']),
    async initDialogData() {
      await this.queryUserPagination(queryUserOptionsParams);
      this.users = this.userInPage.list.map(user =>
        formatOption(formatUser(user), 'name')
      );
      this.userOptions = this.users;
      this.userFilterOptions = this.userOptions;
      await this.queryIpmpUnitByDemandId({
        demandId: this.mountIpmp.demand_id,
        size: this.pagination.rowsPerPage,
        index: this.pagination.page
      });
      this.tableData = this.ipmpUnitListTable.data;
      this.initSelectedFun();
      this.loading = true;
      this.pagination.rowsNumber = this.ipmpUnitListTable.count;
    },

    async pageUnitList(props) {
      let { page, rowsPerPage, rowsNumber } = props.pagination;
      this.loading = true;
      this.pagination.page = page; //页码
      this.pagination.rowsPerPage = rowsPerPage; //每页数据大小
      this.pagination.rowsNumber = rowsNumber; //数据库数据总条数

      await this.queryIpmpUnitByDemandId({
        demandId: this.mountIpmp.demand_id,
        size: this.pagination.rowsPerPage,
        index: this.pagination.page
      });
      this.tableData = this.ipmpUnitListTable.data;
      this.pagination.rowsNumber = this.ipmpUnitListTable.count;
      this.initSelectedFun();
    },
    userFilter(val, update, abort) {
      // if (this.demandType === '1') {
      //如果为业务需求，则牵头人下拉为全量行内人员
      this.userOptions = this.userOptions.filter(item => {
        if (item.email.indexOf('@') > -1) {
          return item.email.split('@')[1] === 'spdb.com.cn';
        }
      });
      // }
      //输入中文名和账号名过滤
      update(() => {
        this.userFilterOptions = this.userOptions.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
      //遍历获取小组名
      // for (let key in this.userFilterOptions) {
      //   let userGroup = this.groups.filter(
      //     item => this.userFilterOptions[key].group_id == item.id
      //   );
      //   this.userFilterOptions[key].group = userGroup[0].name;
      // }
    },
    isCheckbox(row) {
      if (this.mountIpmp.demand_type == 'business' && row.leaderFlag == '3') {
        row.isMountTip = '实施牵头人不为fdev用户，不允许挂载';
        return true;
      }
      if (
        this.mountIpmp.demand_type == 'business' &&
        !!row.usedSysCode &&
        row.usedSysCode !== 'ZH-0748' &&
        row.usedSysCode !== 'stockUnit'
      ) {
        row.isMountTip = '非fdev平台实施单元，不允许挂载';
        return true;
      }
      if (
        this.mountIpmp.demand_type == 'business' &&
        (row.implStatusName == '已投产' ||
          row.implStatusName == '已撤销' ||
          row.implStatusName == '暂缓' ||
          row.implStatusName == '暂存')
      ) {
        row.isMountTip = '实施单元已投产、撤销、暂缓、暂存状态时不可挂载';
        return true;
      }
      if (
        this.mountIpmp.demand_type == 'business' &&
        (!row.leaderGroup || !row.planInnerTestDate)
      ) {
        row.isMountTip =
          '至实施单元编辑页面补全牵头小组和计划提交内测日期才可挂载';
        return true;
      }
      return false;
    },
    dateFormat(date) {
      if (!date || typeof date === 'object') {
        return;
      }
      return moment(date).format('YYYYMMDD');
    },
    formatDate(date) {
      if (!date || typeof date === 'object') {
        return;
      }
      return moment(date).format('YYYY/MM/DD');
    },
    //计划启动日期
    planStartDateOptions(date) {
      const afterDate = this.formatDate(
        this.editUnitModel.plan_test_date ||
          this.editUnitModel.plan_inner_test_date ||
          this.editUnitModel.plan_test_finish_date ||
          this.editUnitModel.plan_product_date
      );
      if (!this.editUnitModel.plan_start_date) {
        this.editUnitModel.plan_start_date = afterDate ? afterDate : '';
      }
      if (this.selectedRow.length > 0 && this.selectedRow[0].planDevelopDate) {
        const afterDate = this.formatDate(this.selectedRow[0].planDevelopDate);
        return date <= afterDate;
      }
      return afterDate ? date <= afterDate : true;
    },
    //计划内测日期
    planInnerTestDateOptions(date) {
      const beforeDate = this.editUnitModel.plan_start_date;
      const afterDate = this.formatDate(
        this.editUnitModel.plan_test_date ||
          this.editUnitModel.plan_test_finish_date ||
          this.editUnitModel.plan_product_date
      );
      const smallerThanAfterDate = afterDate ? date <= afterDate : true;
      if (!this.editUnitModel.plan_inner_test_date) {
        this.editUnitModel.plan_inner_test_date = beforeDate ? beforeDate : '';
      }
      if (
        this.selectedRow.length > 0 &&
        this.selectedRow[0].planInnerTestDate
      ) {
        const unitDate = this.formatDate(this.selectedRow[0].planInnerTestDate);
        return (
          unitDate >= date &&
          this.formatDate(beforeDate) <= date &&
          smallerThanAfterDate
        );
      }
      return this.formatDate(beforeDate) <= date && smallerThanAfterDate;
    },
    //计划提交用户测试日期
    planTestDateOptions(date) {
      const beforeDate =
        this.editUnitModel.plan_inner_test_date ||
        this.editUnitModel.plan_start_date;
      const afterDate = this.formatDate(
        this.editUnitModel.plan_test_finish_date ||
          this.editUnitModel.plan_product_date
      );
      const smallerThanAfterDate = afterDate ? date <= afterDate : true;
      if (!this.editUnitModel.plan_test_date) {
        this.editUnitModel.plan_test_date = beforeDate ? beforeDate : '';
      }
      if (
        this.selectedRow.length > 0 &&
        this.selectedRow[0].planTestStartDate
      ) {
        const unitDate = this.formatDate(this.selectedRow[0].planTestStartDate);
        return (
          unitDate >= date &&
          date >= this.formatDate(beforeDate) &&
          smallerThanAfterDate
        );
      }
      return this.formatDate(beforeDate) <= date && smallerThanAfterDate;
    },
    //计划提交用户测试完成日期
    planTestFinishDateOptions(date) {
      const beforeDate =
        this.editUnitModel.plan_test_date ||
        this.editUnitModel.plan_inner_test_date ||
        this.editUnitModel.plan_start_date;
      const afterDate = this.formatDate(this.editUnitModel.plan_product_date);
      const smallerThanAfterDate = afterDate ? date <= afterDate : true;
      if (!this.editUnitModel.plan_test_finish_date) {
        this.editUnitModel.plan_test_finish_date = beforeDate ? beforeDate : '';
      }
      return this.formatDate(beforeDate) <= date && smallerThanAfterDate;
    },
    //计划投产日期
    planProductDateOptions(date) {
      const beforeDate =
        this.editUnitModel.plan_test_finish_date ||
        this.editUnitModel.plan_test_date ||
        this.editUnitModel.plan_inner_test_date ||
        this.editUnitModel.plan_start_date;
      if (!this.editUnitModel.plan_product_date) {
        this.editUnitModel.plan_product_date = beforeDate ? beforeDate : '';
      }
      return this.formatDate(beforeDate) <= date;
    },
    bindEvent() {
      setTimeout(() => {
        // 拿到日期选择器元素 使用setTimeout是因为自带的动画在0.几秒后才将元素渲染出来
        let el = document.querySelector('.q-position-engine');
        if (el) {
          // 给日期选择器绑定mouseover事件，判断当鼠标移入带有q-date__calendar-item--out也就是无法选择日期时，展示tip
          el.addEventListener('mouseover', $event => {
            if (
              $event.fromElement &&
              $event.fromElement.className.indexOf(
                'q-date__calendar-item--out'
              ) !== -1
            ) {
              this.isDisable = true;
            } else {
              this.isDisable = false;
            }
          });
        }
      }, 500);
    },
    showTip() {
      return this.isDisable;
    },
    initQuery() {
      //this.$v.editUnitModel.plan_start_date.$touch();
      const keys = Object.keys(this.$refs).filter(
        key => this.$refs[key] && key.indexOf('editUnitModel') > -1
      );
      validate(
        keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.editUnitModel.$invalid) {
        const _this = this;
        const validateRes = keys.every(item => {
          if (item.indexOf('.') === -1) {
            return true;
          }
          const itemArr = item.split('.');
          return _this.$v.editUnitModel[itemArr[1]].$invalid == false;
        });
        if (!validateRes) {
          return;
        }
      }
    },
    async findUnitList(time) {
      if (this.timer) {
        clearTimeout(this.timer);
        this.timer = null;
        this.timer = setTimeout(() => {
          this.commitFun();
        }, time);
      } else {
        this.timer = setTimeout(() => {
          this.commitFun();
        }, time);
      }
    },
    async commitFun() {
      await this.queryIpmpUnitByDemandId({
        demandId: this.mountIpmp.demand_id,
        size: this.pagination.rowsPerPage,
        index: this.pagination.page,
        implContent: this.ipmpContent,
        implUnitNum: this.ipmpNo,
        prjNum: this.projectNo,
        implLeader: this.Ipmpleader.user_name_cn
          ? this.Ipmpleader.user_name_cn.user_name_en
          : ''
      });
      this.tableData = this.ipmpUnitListTable.data;
      this.loading = true;
      this.pagination.rowsNumber = this.ipmpUnitListTable.count;
      this.initSelectedFun();
    },
    confirmToClose() {
      (this.ipmpContent = ''),
        (this.ipmpNo = ''),
        (this.projectNo = ''),
        (this.Ipmpleader = {}),
        this.$emit('refreshMount');
    },
    selectionFun(val) {
      this.selectedRow = [];
      let cache = val.selected;
      if (
        this.selectedDetail &&
        val.implUnitNum === this.selectedDetail.implUnitNum
      ) {
        this.initSelectedFun();
        this.tableData.forEach(item => {
          if (item.implUnitNum === val.implUnitNum)
            this.$set(item, 'selected', cache);
        });
      } else {
        this.initSelectedFun();
        this.tableData.forEach(item => {
          if (item.implUnitNum === val.implUnitNum)
            this.$set(item, 'selected', cache);
        });
      }
      this.selectedRow.push(val);
      this.selectedDetail = val;
    },
    initSelectedFun() {
      this.tableData.forEach(item => this.$set(item, 'selected', false));
    },
    async mountAdd() {
      if (this.selectedRow.length !== 0) {
        // if (this.mountIpmp.implement_unit_status_normal !== 1) {
        //   await this.mount({
        //     id: this.mountIpmp.id,
        //     demand_id: this.mountIpmp.demand_id,
        //     ipmp_implement_unit_no: this.selectedRow[0].implUnitNum,
        //     plan_start_date: this.mountIpmp.plan_start_date,
        //     plan_inner_test_date: this.mountIpmp.plan_inner_test_date,
        //     plan_test_date: this.mountIpmp.plan_test_date,
        //     plan_test_finish_date: this.mountIpmp.plan_test_finish_date,
        //     plan_product_date: this.mountIpmp.plan_product_date
        //   });
        //   successNotify('挂载成功');
        //   this.$emit('refreshMount', true);
        // } else {
        this.editUnitModel.plan_start_date =
          this.formatDate(this.selectedRow[0].planDevelopDate) >=
          this.formatDate(this.mountIpmp.plan_start_date)
            ? this.mountIpmp.plan_start_date
            : '';
        this.editUnitModel.plan_inner_test_date =
          this.formatDate(this.selectedRow[0].planInnerTestDate) >=
          this.formatDate(this.mountIpmp.plan_inner_test_date)
            ? this.mountIpmp.plan_inner_test_date
            : '';
        this.editUnitModel.plan_test_date =
          this.formatDate(this.selectedRow[0].planTestStartDate) >=
          this.formatDate(this.mountIpmp.plan_test_date)
            ? this.mountIpmp.plan_test_date
            : '';
        this.editUnitModel.plan_test_finish_date = this.mountIpmp.plan_test_finish_date;
        this.editUnitModel.plan_product_date = this.mountIpmp.plan_product_date;
        this.nextDialogOpen = true;
        // }
      } else {
        errorNotify('请选择实施单元');
      }
    },
    //修改时间后挂载
    async mountImplUnit() {
      this.$v.editUnitModel.$touch();
      const keys = Object.keys(this.$refs).filter(
        key => this.$refs[key] && key.indexOf('editUnitModel') > -1
      );
      validate(
        keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          //时间日期组件校验
          if (this.$refs[key].$refs.date) {
            return this.$refs[key].$children[0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.editUnitModel.$invalid) {
        const _this = this;
        const validateRes = keys.every(item => {
          if (item.indexOf('.') === -1) {
            return true;
          }
          const itemArr = item.split('.');
          return _this.$v.editUnitModel[itemArr[1]].$invalid == false;
        });
        if (!validateRes) {
          return;
        }
      }
      await this.mount({
        id: this.mountIpmp.id,
        demand_id: this.mountIpmp.demand_id,
        ipmp_implement_unit_no: this.selectedRow[0].implUnitNum,
        plan_start_date: this.editUnitModel.plan_start_date,
        plan_inner_test_date: this.editUnitModel.plan_inner_test_date,
        plan_test_date: this.editUnitModel.plan_test_date,
        plan_test_finish_date: this.editUnitModel.plan_test_finish_date,
        plan_product_date: this.editUnitModel.plan_product_date
      });
      successNotify('挂载成功');
      this.$emit('refreshMount', true);
    },
    confirmNextClose() {
      this.nextDialogOpen = false;
    }
  }
};
</script>

<style lang="stylus" scoped>
.box {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: space-between;
}

.td-width
  max-width 300px
  overflow hidden
  text-overflow ellipsis


 .row-item {
  width: 100%;
}
</style>
