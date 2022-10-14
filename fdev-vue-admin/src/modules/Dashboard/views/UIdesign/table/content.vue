<template>
  <div class="wrap">
    <table>
      <thead>
        <tr>
          <th
            v-for="(item, index) in col"
            :key="index"
            :colspan="item.val === 'group' ? 2 : ''"
          >
            {{ item.label }}
          </th>
        </tr>
      </thead>
      <tbody @click="goDetailFun($event)" class="contain"></tbody>
    </table>
  </div>
</template>
<script>
// import { mockData } from '@/modules/Dashboard/utils/constants.js';
import createTable from './table.js';
let deepCloneFun = list => {
  return JSON.parse(JSON.stringify(list));
};
export default {
  props: {
    type: {
      type: String
    },
    tableData: {
      type: Array
    },
    selectedColList: {
      type: Array
    }
  },
  data() {
    return {
      // 设计还原审核
      colList: [
        // 固定的选择列
        { label: '项目小组', val: 'group' },
        { label: '需求编号', val: 'demandNo' },
        { label: '任务名称', val: 'taskName' },
        { label: '卡点状态', val: 'states' }
      ],
      options: [
        { label: '完成情况', val: 'finshFlag', key: 0 },
        { label: '当前阶段', val: 'currentStage', key: 1 },
        { label: '当前阶段开始时间', val: 'currentStageTime', key: 2 },
        { label: '审核次数', val: 'checkCount', key: 3 },
        { label: '开发人员', val: 'uiVerifyReporter', key: 4 },
        { label: '计划提交内测日期', val: 'plan_inner_test_time', key: 5 },
        { label: '计划提交业测日期', val: 'plan_uat_test_start_time', key: 6 },
        { label: '实际提交内测日期', val: 'start_inner_test_time', key: 7 },
        { label: '实际提交业测日期', val: 'start_uat_test_time', key: 8 },
        { label: '研发单元牵头人', val: 'implement_leader', key: 9 }
      ],
      count: 0, // 初次进入状态
      // 设计稿审核 固定列
      demandCol: [
        { label: '项目小组', val: 'group' },
        { label: '需求名称', val: 'oa_contact_name' }
      ],
      demandOptions: [
        { label: '完成情况', val: 'finshFlag', key: 0 },
        { label: '当前阶段', val: 'currentStage', key: 1 },
        { label: '当前阶段开始时间', val: 'currentStageTime', key: 2 },
        { label: '审核次数', val: 'checkCount', key: 3 },
        { label: '开发人员', val: 'uiVerifyReporter', key: 4 },
        { label: '计划提交内测日期', val: 'plan_inner_test_time', key: 5 },
        { label: '计划提交业测日期', val: 'plan_uat_test_start_time', key: 6 },
        { label: '实际提交内测日期', val: 'start_inner_test_time', key: 7 },
        { label: '实际提交业测日期', val: 'start_uat_test_time', key: 8 }
      ],
      abstractCol: [], // 抽象的选择固定列
      abstractOptions: [],
      listType: ''
    };
  },
  computed: {
    col() {
      return this.colList.concat(this.selectedColList);
    }
  },
  watch: {
    // 是设计稿审核列表还是涉及还原审核
    type(n) {
      this.count = 0;
      this.listType = n;
      if (n === 'task') {
        this.abstractCol = this.colList;
        this.abstractOptions = this.options;
      }
      if (n === 'demand') {
        this.abstractCol = this.demandCol;
        this.abstractOptions = this.demandOptions;
      }
    },
    col(n, o) {
      // 勾选选择列执行
      this.initColFun(n, o);
    },
    count(n) {
      // 页面初次加载执行，其他情况不执行 确保在tableData函数后执行
      if (n === 1)
        this.initColFun(this.colList.concat(this.options.slice(0, 4)));
    },
    async tableData(n) {
      this.listType = this.type; // 初次进来需要对类型进行初始化赋值
      if (this.count === 0) this.initTable(deepCloneFun(n), this.listType);
      // 等待dom数加载完毕 count++ 触发 count wact函数（解决样式通过js加上，dom树还未渲染完成问题）
      if (document.readyState === 'complete') this.count++;
    }
  },
  methods: {
    // 初始化渲染选择列
    initColFun(n, o) {
      if (this.count === 1) {
        this.hideColFun(n, this.colList.concat(this.options));
        this.count++; // 只关注 0 1 2 的值，后续无需在++
      } else {
        this.hideColFun(n, o);
      }
    },
    // 初始化渲染列表
    initTable(tableData, type) {
      this.clearBodyFun();
      let res = this.handleData(tableData);
      let groupArr = this.getGroupArrFun(tableData);
      let el = '.contain';
      createTable.createDomByData(groupArr, res, el, type);
    },
    // 组装生成新的数据是一个新的数组（不改变原有数据）
    handleData(tableData) {
      let newData = [];
      let groupArr = this.getGroupArrFun(tableData);
      for (let i = 0; i < groupArr.length; i++) {
        newData.push(
          this.getDataByGroupIdFun(
            tableData.filter(ct => ct.group === groupArr[i].group)
          )
        );
      }
      return newData.flat(Infinity);
    },
    // 返回所有小组Id 和name 的新的数组
    getGroupArrFun(list) {
      let arr = [];
      list.forEach(it => {
        let obj = {
          group: it.group,
          groupName: it.groupName
        };
        arr.push(obj);
      });
      return arr;
    },
    // 返回一个将通过和未通过的数据整合后的某个小组数据的新数组
    getDataByGroupIdFun(list) {
      let arr = [];
      list.forEach(
        it => (arr = deepCloneFun(it.finishedList.concat(it.unfinishedList)))
      );
      return arr;
    },
    async hideColFun(n, o) {
      await this.$nextTick();
      let map = this.diffFun(n, o);
      if (map.add) {
        map.add.forEach(it => {
          if (it === 'finshFlag') it = 'ifFinsh';
          if (it === 'implement_leader') it = 'implementLeaderNameCN';
          let el = document.querySelectorAll(`.${it}`);
          el.forEach(ct => (ct.style.display = 'table-cell'));
        });
      }
      if (map.del) {
        map.del.forEach(it => {
          if (it === 'finshFlag') it = 'ifFinsh';
          if (it === 'implement_leader') it = 'implementLeaderNameCN';
          let el = document.querySelectorAll(`.${it}`);
          el.forEach(ct => {
            ct.style.display = 'none';
          });
        });
      }
    },
    // 对比选择列差异
    diffFun(newArr, oldArr) {
      let diffMap = {},
        diff = [],
        map = {};
      if (newArr.length > oldArr.length) {
        // 添加
        oldArr.forEach(it => (diffMap[it.val] = true));
        newArr.forEach(nt => {
          if (!diffMap[nt.val]) {
            diff.push(nt.val);
            map['add'] = diff;
          }
        });
      } else {
        // 删除
        newArr.forEach(it => (diffMap[it.val] = true));
        oldArr.forEach(nt => {
          if (!diffMap[nt.val]) {
            diff.push(nt.val);
            map['del'] = diff;
          }
        });
      }
      return map;
    },
    // 去往详情页（事件代理）
    goDetailFun(e) {
      if (e.target.title) {
        let rowInfo = JSON.parse(e.target.rowInfo);
        // 需求编号
        if (e.target.className === 'redmine_id') {
          this.$router.push(`/rqrmn/rqrProfile/${rowInfo.redmine_id}`);
        }
        // 任务名称
        if (e.target.className === 'name') {
          this.$router.push(`/job/list/${rowInfo.id}/design`);
        }
      }
    },
    clearBodyFun() {
      let tbody = document.querySelector('.contain');
      tbody.innerHTML = '';
    }
  }
};
</script>
<style lang="stylus" scoped>
.wrap {
    overflow-x: scroll;
}
table {
  border-collapse: collapse;
  margin: 0 auto;
  width: 1800px;
}

table thead th {
  background-color: #F4F6FD;
  height: 54px;
  padding: 0 5px;
  box-sizing: border-box;
  text-align: left;
}
table thead th:first-child{
    text-align: center;
}
// table > thead > tr > th:nth-child(-n + 4) {
//   position: sticky;
//   z-index: 99;
//   left: 0;
//   background-color: #f4f6fd;
//   box-shadow: 2px 0 4px 0 rgba(51,51,51,.15);
// }
</style>
<style>
table td,
table th {
  height: 54px;
  padding: 0 5px;
  box-sizing: border-box;
}

.group,
.pass1,
.pass0 {
  text-align: center;
}

/* .group,
.pass1,
.pass0,
.positionStatus {
  position: sticky;
  z-index: 99;
  background-color: #f4f6fd;
  box-shadow: 2px 0 4px 0 rgba(51, 51, 51, 0.15);
} */
.group {
  left: 0;
}
.pass1 {
  left: 159;
}

table tr:nth-child(n) {
  background-color: #fff;
}

table tr td:nth-child(n-2) {
  box-sizing: border-box;
  border-bottom: 1px solid #ccc;
}

.redmine_id,
.name {
  color: #0663be;
  cursor: pointer;
}

.ok,
.pass,
.alloting {
  color: #4dbb59;
}
.fail,
.noPass {
  color: #ef5350;
}
.ok::before,
.fail::before,
.pass::before,
.noPass::before,
.alloting::before,
.checking::before,
.updateing::before {
  content: '';
  display: inline-block;
  width: 10px;
  height: 10px;
  margin-right: 8px;
  border-radius: 12px;
}
.ok::before,
.pass::before,
.alloting::before {
  background-image: linear-gradient(
    270deg,
    rgba(77, 187, 89, 0.5) 0%,
    #4dbb59 100%
  );
}
.fail::before,
.noPass::before {
  background-image: linear-gradient(
    270deg,
    rgba(239, 83, 80, 0.5) 0%,
    #ef5350 100%
  );
}
.checking {
  color: #fd8d00;
}
.checking::before {
  background-image: linear-gradient(
    270deg,
    rgba(253, 141, 0, 0.5) 0%,
    #fd8d00 100%
  );
}
.updateing {
  color: #42a5f5;
}
.updateing::before {
  background-image: linear-gradient(
    270deg,
    rgba(66, 165, 245, 0.5) 0%,
    #42a5f5 100%
  );
}
</style>
