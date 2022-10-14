<template>
  <f-dialog
    :value="value"
    @input="$emit('input', false)"
    right
    title="接口自动化测试"
    @before-close="clickCancel"
  >
    <div style="width:1250px">
      <fdev-table
        class="full-width"
        :data="infoDate.rtp_data"
        row-key="id"
        :columns="FirstColumns"
        :rows-per-page-options="[0]"
        hide-pagination
        title="列表1"
      >
      </fdev-table>
      <div class="row items-center justify-end full-width q-mt-md">
        <fdev-btn color="cyan-14" class="q-mr-md btn-style">
          <span class="q-mr-md">Passed</span>
          <span>{{ infoDate.rtp_data[0].pass_sum }}</span>
        </fdev-btn>
        <fdev-btn color="blue-10" class="q-mr-md btn-style">
          <span class="q-mr-md">Failed</span>
          <span>{{ infoDate.rtp_data[0].fail_sum }}</span>
        </fdev-btn>
        <fdev-btn color="indigo-4" class="q-mr-md btn-style">
          <span class="q-mr-md">Error</span>
          <span>{{ infoDate.rtp_data[0].error_sum }}</span>
        </fdev-btn>
        <fdev-btn color="amber-14" class="q-mr-md btn-style">
          <span class="q-mr-md">FPR</span>
          <span>{{ infoDate.rtp_data[0].fpr_sum }}</span>
        </fdev-btn>
        <fdev-btn normal class="btn-style">
          <span class="q-mr-md">Total</span>
          <span>{{ infoDate.rtp_data[0].case_sum }}</span>
        </fdev-btn>
      </div>
      <fdev-table
        class="full-width q-mt-lg"
        :data="infoDate.case_data"
        row-key="id"
        :columns="SecondColumns"
        title="列表2"
      >
        <template v-slot:body-cell-index="props">
          <fdev-td>
            {{ props.pageIndex + 1 }}
          </fdev-td>
        </template>
        <!-- <template v-slot:body-cell-btn="props">
          <fdev-td class="no-padding">
            <fdev-btn flat label="查看详情" @click="lookDetail"/>
          </fdev-td>
        </template> -->
      </fdev-table>
    </div>
  </f-dialog>
</template>

<script>
export default {
  name: 'AutoTest',
  components: {},
  props: {
    value: {
      type: Boolean,
      default: false
    },
    infoDate: Object
  },
  data() {
    return {
      FirstColumns: [
        {
          name: 'report_id',
          label: '编号',
          field: 'report_id',
          align: 'left'
        },
        {
          name: 'test_set',
          label: '测试集',
          field: 'test_set',
          align: 'left'
        },
        {
          name: 'env_name',
          label: '测试环境',
          field: 'env_name',
          align: 'left'
        },
        {
          name: 'tran_sum',
          label: '总交易数',
          field: 'tran_sum',
          align: 'left'
        },
        {
          name: 'case_sum',
          label: '总案例数',
          field: 'case_sum',
          align: 'left'
        },
        {
          name: 'success_perc',
          label: '通过率（%）',
          field: 'success_perc',
          align: 'left'
        },
        {
          name: 'create_time',
          label: '执行时间',
          field: 'create_time',
          align: 'left'
        },
        {
          name: 'cost_time',
          label: '耗时（s）',
          field: 'cost_time',
          align: 'left'
        }
      ],
      SecondColumns: [
        {
          name: 'index',
          label: '#',
          align: 'left'
        },
        {
          name: 'case_code',
          label: '交易码',
          field: 'case_code',
          align: 'left'
        },
        {
          name: 'case_kind',
          label: '案例分类',
          field: 'case_kind',
          align: 'left'
        },
        {
          name: 'case_id',
          label: '案例编号',
          field: 'case_id',
          align: 'left'
        },
        {
          name: 'case_desc',
          label: '案例描述',
          field: 'case_desc',
          align: 'left'
        },
        {
          name: 'case_rtcode',
          label: '返回码',
          field: 'case_rtcode',
          align: 'left'
        },
        {
          name: 'case_rtmsg',
          label: '返回信息',
          field: 'case_rtmsg',
          align: 'left'
        },
        {
          name: 'case_status',
          label: '结果',
          field: 'case_status',
          align: 'left'
        },
        {
          name: 'case_failed_kind',
          label: '失败归类',
          field: 'case_failed_kind',
          align: 'left'
        },
        {
          name: 'case_author',
          label: '负责人',
          field: 'case_author',
          align: 'left'
        },
        {
          name: 'timestamp',
          label: '执行开始时间',
          field: 'timestamp',
          align: 'left'
        },
        {
          name: 'cost_time',
          label: '耗时',
          field: 'cost_time',
          align: 'left'
        }
        // {
        //   name: 'btn',
        //   label: '操作',
        //   align: 'left'
        // }
      ]
    };
  },
  methods: {
    clickCancel() {
      this.$emit('update:isShow', false);
    }
  }
};
</script>
<style lang="stylus" scoped>
.btn-style
  width 148px
  height 42px
  border-radius 4px
.no-padding
  padding 0
</style>
