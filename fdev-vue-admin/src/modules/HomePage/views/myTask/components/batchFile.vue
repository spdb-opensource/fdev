<template>
  <load-status :visible="loading">
    <f-dialog
      @before-close="handleCancleFun"
      v-model="open"
      right
      dense
      title="任务归档"
      tip="任务归档后, 相关操作无法执行且无法重新打开！归档后的任务只允许查看"
    >
      <fdev-table
        title="归档列表"
        titleIcon="list_s_f"
        noExport
        row-key="id"
        :columns="columns"
        :data="data"
        selection="multiple"
        :selected.sync="selected"
        :rows-per-page-options="[0]"
        hide-bottom
        noSelectCols
      >
        <template v-slot:header-selection="props">
          <fdev-checkbox
            @input="handleSelectFun(props)"
            v-model="props.selected"
          />
        </template>
        <!-- 任务名称 -->
        <template v-slot:body-selection="props">
          <fdev-tooltip v-if="props.row.disable">
            已暂缓的任务无法归档
          </fdev-tooltip>
          <fdev-checkbox
            @input="handleSingleFun(props)"
            :disable="props.row.disable"
            v-model="props.row.selected"
          />
        </template>
        <template v-slot:body-cell-name="props">
          <fdev-td>
            <div
              class="text-ellipsis"
              v-if="props.row.name"
              :title="props.row.name"
            >
              <router-link :to="`/job/list/${props.row.id}`" class="link">
                {{ props.row.name }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.name }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-app_name_en="props">
          <fdev-td>
            <div
              class="text-ellipsis"
              v-if="props.row.app_name_en"
              :title="props.row.app_name_en"
            >
              <router-link
                :to="`/app/list/${props.row.project_id}`"
                class="link"
              >
                {{ props.row.app_name_en }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.app_name_en }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>
      </fdev-table>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" dialog outline @click="handleCancleFun" />
        <fdev-btn
          label="归档"
          dialog
          @click="submitFileFun"
          :loading="loadingBtn"
        />
      </template>
    </f-dialog>
  </load-status>
</template>
<script>
import loadStatus from '@/components/Loading';
import { batchFileCol } from '../../../utils/constants';
import {
  batchFileTask,
  queryFileList
} from '@/modules/HomePage/services/methods';
import { successNotify, errorNotify } from '@/utils/utils';
export default {
  components: { loadStatus },
  data() {
    return {
      loading: false, // 加载loading
      loadingBtn: false,
      open: true, // 该属性不做任何操作 弹窗通过外层v-if控制（防止生命周期混乱）
      data: [], // 列表数据项
      columns: batchFileCol, // 列字段配置项
      selected: [] // 添加插槽 手动控制selected 数组
    };
  },
  created() {
    this.init();
  },
  methods: {
    // 初始化数据 添加selected disable属性
    async init() {
      this.loading = true;
      const res = await queryFileList();
      if (res.code) return errorNotify(res.msg);
      res.forEach(item => {
        item.selected = false;
        if (item.taskSpectialStatus === 1) item.disable = true;
        if (item.taskSpectialStatus !== 1) item.disable = false;
      });
      this.data = res;
      this.loading = false;
    },
    // 关闭弹窗
    handleCancleFun() {
      this.$emit('exposureOperationFun');
    },
    validateFun() {
      this.$q.dialog({
        title: `提示`,
        message: `请先勾选需要归档的数据`,
        ok: '确定',
        cancel: '取消'
      });
    },
    // 确认归档
    async submitFileFun() {
      let ids = this.selected.map(item => item.id);
      if (!ids.length) this.validateFun();
      if (ids.length) {
        this.loadingBtn = true;
        const res = await batchFileTask({ ids });
        this.loadingBtn = false;
        if (res.code) return errorNotify(res.msg);
        successNotify('归档成功');
        this.handleCancleFun();
        this.$emit('exposureOperationFun', 'refresh');
      }
    },
    // 全选
    handleSelectFun(props) {
      if (props.selected === false) {
        this.data.forEach(item => {
          if (item.taskSpectialStatus === 1) item.selected = false;
        });
        let arr = this.selected.filter(item => item.disable === false);
        arr.forEach(item => (item.selected = true));
        this.selected = arr;
      } else {
        this.data.forEach(item => {
          if (item.taskSpectialStatus === 1) item.selected = false;
          if (item.taskSpectialStatus !== 1) {
            this.selected = [];
            item.selected = false;
          }
        });
      }
    },
    // 单选
    handleSingleFun(props) {
      if (props.row.selected) this.selected.push(props.row);
      if (!props.row.selected)
        this.selected.splice(this.selected.length - 1, 1);
    }
  }
};
</script>
<style lang="stylus" scoped>
.font {
  font-size: 12px;
  color: #ef5350;
}
.my-sticky-column-table {
  height: 500px;
}
.my-sticky-column-table  >>> .q-table > thead > tr > th, {
  position: sticky;
  z-index: 999;
  top: 0;
  background-color: #f4f6fd;
}
.my-sticky-column-table th:first-child, .my-sticky-column-table td:first-child {
  padding: 0;
}
.my-sticky-column-table td:first-child {
  background: transparent;
  border: 1px solid transparent;
}
.my-sticky-column-table >>> .q-table tbody td:after {
  background: transparent;
  border: 1px solid transparent;
}
</style>
