<template>
  <div>
    <table
      border="1"
      class="table-style"
      bordercolor="#DDDDDD"
      width="100%"
      id="taskTeamDimensionTable"
    >
      <tbody>
        <tr>
          <td class="first-th-style">板块</td>
          <td class="first-th-style">小组</td>
          <td class="th-style" v-for="(i, index) in headerList" :key="index">
            {{ i }}
          </td>
        </tr>
        <tr v-for="item in sjList" :key="item.id" class="td-style">
          <td :rowspan="item.groupspan" :class="{ hidden: item.groupdis }">
            {{ item.group }}
          </td>
          <td>{{ item.team }}</td>
          <td>{{ item.kcdm }}</td>
          <td>{{ item.kcmc }}</td>
          <td>{{ item.xf }}</td>
          <td>{{ item.kcdm }}</td>
          <td>{{ item.kcmc }}</td>
          <td>{{ item.xf }}</td>
          <td>{{ item.kcdm }}</td>
          <td>{{ item.kcmc }}</td>
          <td>{{ item.xf }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
<script>
import { taskThroughputHeader } from '@/modules/Measure/utils/constants';
export default {
  props: {
    dataSource: Array
  },
  data() {
    return { headerList: taskThroughputHeader };
  },
  methods: {
    // 数据处理相同的放在一起
    checkSameData(sjList) {
      let cache = {}; //存储的是键是kclx 的值，值是kclx 在indeces中数组的下标
      let indices = []; //数组中每一个值是一个数组，数组中的每一个元素是原数组中相同kclx的下标
      sjList.map((item, index) => {
        let group = item.group;
        let _index = cache[group];
        if (_index !== undefined) {
          indices[_index].push(index);
        } else {
          cache[group] = indices.length;
          indices.push([index]);
        }
      });
      let result = [];
      indices.map(item => {
        item.map(index => {
          result.push(sjList[index]);
        });
      });
      this.sjList = result;
    },
    // 合并
    combineCell() {
      let list = this.sjList;
      for (let field in list[0]) {
        var k = 0;
        while (k < list.length) {
          list[k][field + 'span'] = 1;
          list[k][field + 'dis'] = false;
          for (var i = k + 1; i <= list.length - 1; i++) {
            if (list[k][field] == list[i][field] && list[k][field] != '') {
              list[k][field + 'span']++;
              list[k][field + 'dis'] = false;
              list[i][field + 'span'] = 1;
              list[i][field + 'dis'] = true;
            } else {
              break;
            }
          }
          k = i;
        }
      }
      return list;
    },
    // 导出方法
    childExport() {
      import('@/utils/exportExcel').then(excel => {
        excel.export_html_table('taskTeamDimensionTable', '小组维度任务吞吐量');
      });
    }
  },
  created() {
    // 处理数据
    this.checkSameData(this.dataSource);
    this.combineCell();
  }
};
</script>
<style lang="stylus" scoped>
.table-style
  border-collapse:collapse !important
  margin-top:26px;
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
.first-th-style
  min-width:102px
  width:102px;
  padding:20px
.th-style
  padding:8px 20px
.td-style td
  padding:20px
</style>
