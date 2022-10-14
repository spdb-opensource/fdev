<template>
  <!-- 等待层 -->
  <Loading :visible="loading">
    <f-block page>
      <fdev-table
        ref="table"
        :data="tableLists"
        :columns="columns"
        :pagination="{
          sortBy: 'count',
          descending: true,
          page: 1,
          rowsPerPage: 5
        }"
        no-export
        no-select-cols
      >
        <template v-slot:body-cell-fieldList="props">
          <fdev-td class="text-ellipsis">
            <div v-if="Array.isArray(props.row.fieldList)">
              <p
                class="pmar text-ellipsis"
                v-for="(item, index) in props.row.fieldList"
                :key="index"
                :title="props.row.fieldList[index] | filterFieldList"
              >
                {{ item | filterFieldList }}
              </p>
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-newValueList="props">
          <fdev-td class="text-ellipsis">
            <div v-if="Array.isArray(props.row.newValueList)">
              <p
                class="pmar text-ellipsis"
                v-for="(item, index) in props.row.newValueList"
                :key="index"
                :title="props.row.newValueList[index] | filterNewValueList"
              >
                {{ item | filterNewValueList }}
              </p>
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-oldValueList="props">
          <fdev-td class="text-ellipsis">
            <div v-if="Array.isArray(props.row.oldValueList)">
              <p
                class="pmar text-ellipsis"
                v-for="(item, index) in props.row.oldValueList"
                :key="index"
                :title="props.row.oldValueList[index] | filterNewValueList"
              >
                {{ item | filterNewValueList }}
              </p>
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </f-block>
  </Loading>
</template>
<script>
import Loading from '@/components/Loading';
import { mapState, mapActions } from 'vuex';
import { perform } from '@/modules/Network/utils/constants';

export default {
  name: 'logsManger',
  props: {
    params: {
      type: Object
    }
  },
  components: { Loading },

  data() {
    return {
      loading: false,
      ...perform,
      columns: [
        {
          name: 'operateUserName',
          label: '操作人',
          field: 'operateUserName',
          required: true
        },
        {
          name: 'operateTime',
          label: '操作时间',
          field: 'operateTime'
        },

        {
          name: 'fieldList',
          label: '字段',
          field: 'fieldList',
          required: true,
          align: 'left'
        },
        {
          name: 'newValueList',
          label: '新值',
          field: 'newValueList',
          required: true,
          align: 'left'
        },
        {
          name: 'oldValueList',
          label: '旧值',
          field: 'oldValueList',
          required: true,
          align: 'left'
        }
      ],
      tableLists: []
    };
  },
  filters: {
    filterFieldList(val) {
      if (val === 'order_status') {
        return '工单状态';
      } else return '审核结论';
    },
    filterNewValueList(val) {
      if (val === 1) return '待审核';
      else if (val === 2) return '审核中';
      else if (val === 3) return '需线下复审';
      else if (val === 4) return '需会议复审';
      else if (val === 5) return '初审通过';
      else if (val === 6) return '线下复审通过';
      else if (val === 7) return '会议复审通过';
      else if (val === 8) return '拒绝';
      else if (val === null) return '-';
      else return val;
    }
  },
  computed: {
    ...mapState('networkForm', ['logList'])
  },
  methods: {
    ...mapActions('networkForm', ['queryLogs'])
  },
  async created() {
    // this.loading = true;
    //初始化用户列表
    await this.queryLogs({ orderId: this.params.id });
    this.tableLists = this.logList;
  }
};
</script>
<style lang="stylus" scoped>
.pmar
  margin-top: 14px;
  margin-bottom: 14px;
.a-link {
  color: #0663BE;
  cursor: pointer;
}
.opEdit {
  display: flex;
  align-items: center;
  vertical-align: center;
}
.lflex{
  border-left:1px solid #DDDDDD;
  height: 14px;
}
</style>
