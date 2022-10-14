<template>
  <f-dialog
    :value="value"
    title="获取批量任务"
    right
    dense
    @input="$emit('input', $event)"
    @before-close="clearForm"
  >
    <fdev-table
      titleIcon="list_s_f"
      title="批量任务列表"
      :data="tableData"
      :columns="columns"
      class="table-width"
      row-key="id"
      no-export
      :selected.sync="rowSelected"
      selection="multiple"
      no-select-cols
    >
      <template v-slot:top-right>
        <f-formitem
          label="应用"
          label-style="width:40px;"
          value-style="width:200px;"
          bottom-page
        >
          <fdev-select
            input-debounce="0"
            option-label="app_name_zh"
            v-model="selectApp"
            ref="dialogModel.selectApp"
            :options="appOptions"
            use-input
            @filter="appFilter"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.application_name_cn">{{
                    scope.opt.application_name_cn
                  }}</fdev-item-label>
                  <fdev-item-label
                    caption
                    :title="scope.opt.application_name_en"
                  >
                    {{ scope.opt.application_name_en }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
      </template>
      <template v-slot:body-cell-type="props">
        <fdev-td class="text-ellipsis" :title="props.row.type">
          <span class="linl">{{ props.row.type | taskName }}</span>
        </fdev-td>
      </template>
      <template v-slot:body-cell-jobGroup="props">
        <fdev-td :title="props.row.jobGroup | jobGroupFiler">
          <span>{{ props.row.jobGroup | jobGroupFiler }}</span>
        </fdev-td>
      </template>
      jobGroup
    </fdev-table>
    <template v-slot:btnSlot>
      <fdev-btn @click="handleCancel" outline dialog label="取消" />
      <fdev-btn
        @click="submit"
        :disable="rowSelected && rowSelected.length === 0"
        dialog
        label="确定"
      />
    </template>
  </f-dialog>
</template>

<script>
import {
  typeOptions,
  extPublishNoOptColumns,
  groupOptions
} from '../../../utils/constants';
import { mapActions, mapState } from 'vuex';
export default {
  name: 'GetExtPublishListDialog',
  data() {
    return {
      batchNo: 0,
      tableData: [],
      selectApp: '',
      appOptions: [],
      columns: extPublishNoOptColumns,
      rowSelected: []
    };
  },
  computed: {
    ...mapState('releaseForm', ['batchTask'])
  },
  filters: {
    taskName(val) {
      return typeOptions.find(v => v.value === val)
        ? typeOptions.find(v => v.value === val).label
        : '-';
    },
    jobGroupFiler(val) {
      return groupOptions.find(v => v.value === val)
        ? groupOptions.find(v => v.value === val).label
        : '-';
    }
  },
  props: {
    value: {
      type: Boolean
    },
    title: {
      type: String
    },
    releaseNodeName: {
      type: String
    },
    options: {
      type: Array
    },
    listData: {
      type: Array
    },
    prodId: {
      type: String
    }
  },
  methods: {
    ...mapActions('releaseForm', ['queryBatchTask', 'addBatchTask']),
    clearForm() {
      this.tableData = [];
      this.selectApp = '';
      this.appOptions = [];
    },
    handleCancel() {
      this.clearForm();
      this.$emit('click', true);
    },
    appFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase().trim();
        this.appOptions = this.options.filter(
          v =>
            v.application_name_cn.toLowerCase().indexOf(needle) > -1 ||
            v.application_name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    async handleBatchTask(val) {
      if (val) {
        const params = {
          release_node_name: this.releaseNodeName,
          application_id: val.application_id
        };
        await this.queryBatchTask(params);
        const appObj = this.listData.find(
          v => v.application_name_en === val.application_name_en
        );
        this.batchNo = 0;
        if (appObj) {
          this.tableData = this.composeListData(this.batchTask, appObj.list);
          this.rowSelected = this.tableData;
        } else {
          this.tableData = this.batchTask;
          this.rowSelected = this.tableData;
        }
      } else {
        this.tableData = [];
        this.rowSelected = [];
      }
    },
    handleData(val) {
      if (val && val.length > 0) {
        this.appOptions = val;
        this.selectApp = val[0];
      } else {
        this.selectApp = '';
        this.appOptions = [];
      }
    },
    async submit() {
      const params = {
        prod_id: this.prodId,
        ids: this.rowSelected.map(v => {
          v.batch_no = String(++this.batchNo);
          return v;
        })
      };
      await this.addBatchTask(params);
      this.tableData = [];
      this.rowSelected = [];
      this.$emit('click');
    },
    composeListData(originList, composeList) {
      let temp = [];
      if (composeList) {
        this.batchNo = composeList.length;
      } else {
        this.batchNo = 0;
      }

      if (originList && originList.length > 0) {
        temp = originList.filter(v => {
          return !composeList.find(val => val.id === v.id);
        });
      }
      return temp;
    }
  },

  created() {
    this.handleData(this.options);
  },
  watch: {
    selectApp: {
      deep: true,
      async handler(val) {
        if (val) {
          this.handleBatchTask(val);
        }
      }
    }
  }
};
</script>
