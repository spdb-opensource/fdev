<template>
  <div>
    <div class="row item-start container-div">
      <div
        class="section-div"
        @click="handelOpenDefectDetail(10, 'newDefect')"
        :class="fromApp ? 'div-cursor' : ''"
      >
        <div class="card-div">{{ defectCount.newDefect }}</div>
        <fdev-badge class="newCreated textBade">新建</fdev-badge>
      </div>
      <fdev-separator vertical />
      <div
        class="section-div"
        :class="fromApp ? 'div-cursor' : ''"
        @click="handelOpenDefectDetail(50, 'open')"
      >
        <div class="card-div">{{ defectCount.open }}</div>
        <fdev-badge class="opened textBade">打开</fdev-badge>
      </div>
      <fdev-separator vertical />
      <div
        class="section-div"
        @click="handelOpenDefectDetail(40, 'delay')"
        :class="fromApp ? 'div-cursor' : ''"
      >
        <div class="card-div">{{ defectCount.delay }}</div>
        <fdev-badge class="delayedRep textBade">延迟修复</fdev-badge>
      </div>
      <fdev-separator vertical />
      <div class="section-div count-div" :class="fromApp ? 'div-cursor' : ''">
        <div
          class="left-div"
          @click="handelOpenDefectDetail(60, 'waitComfirm')"
        >
          <div class="card-div">{{ defectCount.waitComfirm }}</div>
          <fdev-badge class="waitConfirm textBade">待确认</fdev-badge>
        </div>
        <fdev-separator vertical class="slantSep" />
        <div class="right-div">
          <div
            class="right-item color-reject mrb"
            @click="handelOpenDefectDetail(20, 'reject')"
          >
            拒绝 <span class="card-span">{{ defectCount.reject }}</span>
          </div>
          <div
            class="right-item color-success"
            @click="handelOpenDefectDetail(80, 'repaired')"
          >
            已修复 <span class="card-span">{{ defectCount.repaired }}</span>
          </div>
        </div>
      </div>
      <fdev-separator vertical />
      <div class="section-div count-div" :class="fromApp ? 'div-cursor' : ''">
        <div class="left-div" @click="handelOpenDefectDetail(70, 'closed')">
          <div class="card-div">{{ defectCount.closed }}</div>
          <fdev-badge class="closed textBade">已关闭</fdev-badge>
        </div>
        <fdev-separator vertical class="slantSep" />
        <div class="right-div">
          <div
            class="right-item color-close mrb"
            @click="handelOpenDefectDetail(90, 'close')"
          >
            关闭 <span class="card-span">{{ defectCount.close }}</span>
          </div>
          <div
            class="right-item color-close"
            @click="handelOpenDefectDetail(30, 'comfirmReject')"
          >
            确认拒绝
            <span class="card-span">{{ defectCount.comfirmReject }}</span>
          </div>
        </div>
      </div>
    </div>
    <!-- 应用下每个任务的缺陷详情统计 -->
    <f-dialog
      right
      v-model="defectDetailDialog"
      :title="`${statusName}状态对应的任务缺陷统计`"
    >
      <fdev-table
        :data="defectListByStatus"
        :columns="keyColums"
        :pagination.sync="pagination"
        noExport
        :title="`${statusName}状态对应的任务缺陷`"
        title-icon="list_s_f"
        no-select-cols
      >
        <template v-slot:body-cell-name="props">
          <fdev-td class="text-ellipsis" :title="props.value">
            <router-link :to="`/job/list/${props.row.id}`" class="link">
              {{ props.value }}
            </router-link>
          </fdev-td>
        </template>
      </fdev-table>
    </f-dialog>
  </div>
</template>

<script>
import { setPagination, getPagination } from '@/modules/App/utils/setting';
import { createDefectModel } from './model.js';
import { perform } from '@/utils/utils';

export default {
  name: 'Count',
  components: {},
  data() {
    return {
      ...perform,
      defectDetailDialog: false,
      defectListByStatus: [],
      statusName: [],
      keyColums: [
        {
          name: 'name',
          label: '任务名称',
          align: 'left',
          field: 'name'
        },
        {
          name: 'status',
          label: '缺陷状态',
          align: 'left',
          field: 'status'
        },
        {
          name: 'count',
          label: '缺陷数量',
          align: 'center',
          sortable: true,
          field: 'count'
        }
      ],
      pagination: getPagination(),
      defectCount: createDefectModel(), //任务缺陷统计总和
      defectListByJobId: [] //每个任务下的缺陷统计
    };
  },
  props: {
    fromApp: {
      type: Boolean
    },
    defectList: {
      type: Array
    }
  },
  watch: {
    pagination(val) {
      setPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    defectList(value) {
      if (!this.fromApp) {
        //任务模块的缺陷统计
        this.defectCount = this.addUpcDefect(value);
      } else {
        //应用模块的缺陷统计
        this.defectCount = createDefectModel();
        value.forEach((defect, index) => {
          this.defectListByJobId.push(this.addUpcDefect(defect.mantis, defect));
          this.defectListByJobId = Array.from(new Set(this.defectListByJobId));
        });
        this.defectListByJobId.forEach(defectItem => {
          Object.keys(defectItem).map(key => {
            return (this.defectCount[key] += defectItem[key]);
          });
        });
      }
    }
  },
  methods: {
    handelOpenDefectDetail(num, status) {
      if (this.fromApp && this.defectCount[status] !== 0) {
        this.defectListByStatus = [];
        this.defectListByJobId.map(item => {
          let defectObj = {};
          defectObj.count = item[status];
          defectObj.name = item.name;
          defectObj.id = item.id;
          defectObj.status = this.status[num];
          if (defectObj.count) {
            this.defectListByStatus.push(defectObj);
          }
        });
        this.statusName = this.status[num];
        this.defectDetailDialog = this.fromApp;
      }
    },
    addUpcDefect(value, defect) {
      let defectCount = createDefectModel();
      if (defect) {
        defectCount.name = defect.name;
        defectCount.id = defect.id;
      }
      value.forEach(val => {
        switch (val.status) {
          case '10':
            ++defectCount.newDefect;
            break;
          case '20':
            ++defectCount.reject;
            break;
          case '30':
            ++defectCount.comfirmReject;
            break;
          case '40':
            ++defectCount.delay;
            break;
          case '50':
            ++defectCount.open;
            break;
          case '80':
            ++defectCount.repaired;
            break;
          case '90':
            ++defectCount.close;
            break;
        }
      });
      defectCount.closed = defectCount.close + defectCount.comfirmReject;
      defectCount.waitComfirm = defectCount.reject + defectCount.repaired;
      return defectCount;
    }
  }
};
</script>
<style lang="stylus" scoped>
.card-div {
  font-weight: 600;
  font-size: 20px;
  color: #333333;
  letter-spacing: 0;
  text-align: center;
  line-height: 22px;
  margin-bottom: 8px;
}

.newCreated {
  background: rgba(77, 187, 89, 0.2);
  border-radius: 2px;
  border-radius: 2px;
  width: 40px;
  height: 22px;
  font-weight: 600;
  font-size: 12px;
  color: #4DBB59;
  letter-spacing: 0;
  line-height: 12px;
}

.opened {
  background: rgba(3, 120, 234, 0.2);
  border-radius: 2px;
  border-radius: 2px;
  width: 40px;
  height: 22px;
  font-weight: 600;
  font-size: 12px;
  color: #0378EA;
  letter-spacing: 0;
  line-height: 12px;
}

.delayedRep {
  background: rgba(253, 141, 0, 0.2);
  border-radius: 2px;
  border-radius: 2px;
  width: 64px;
  height: 22px;
  font-weight: 600;
  font-size: 12px;
  color: #FD8D00;
  letter-spacing: 0;
  line-height: 12px;
}

.waitConfirm {
  background: rgba(244, 104, 101, 0.2);
  border-radius: 2px;
  border-radius: 2px;
  width: 52px;
  height: 22px;
  font-weight: 600;
  font-size: 12px;
  color: #F46865;
  letter-spacing: 0;
  line-height: 12px;
}

.closed {
  background: #A8AFBE;
  border-radius: 2px;
  border-radius: 2px;
  width: 52px;
  height: 22px;
  font-weight: 600;
  font-size: 12px;
  color: #FFFFFF;
  letter-spacing: 0;
  line-height: 12px;
}

.q-separator--vertical {
  height: 52px;
}

.slantSep {
  height: 52px;
}

.slantSep.q-separator--vertical {
  background: #DDDDDD;
  transform: rotate(25deg);
  width: 1px;
}

.container-div {
  margin-left: 0;
  margin-top: 32px;
}

.section-div {
  flex: 1;
  text-align: center;
}

.div-cursor {
  cursor: pointer;
}

.mrb {
  margin-bottom: 9px;
}

.card-span {
  font-weight: 600;
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  text-align: center;
  line-height: 22px;
  margin-left: 8px;
}

.count-div {
  display: flex;
}

.count-div > div {
  flex: 1;
}

.right-div {
  margin-right: 20px;
}

.right-div > .right-item {
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0;
  line-height: 12px;
  width: 80px;
  text-align: right;
}

.left-div {
  margin-left: 20px;
  align-self: flex-start;
}

.color-close {
  color: #A8AFBE;
}

.color-success {
  color: #4DBB59;
}

.color-reject {
  color: #F46865;
}
.textBade {
  display: inline-grid;
}
.textBade.q-badge {
  padding: 0px 0px;
}
</style>
