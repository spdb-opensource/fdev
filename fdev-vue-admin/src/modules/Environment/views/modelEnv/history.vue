<template>
  <f-block>
    <fdev-card flat square class="q-pa-md">
      <Loading :visible="loading">
        <fdev-table
          :data="list"
          :columns="columns"
          row-key="id"
          noExport
          title="历史信息"
          titleIcon="list_s_f"
          class="table"
          :pagination.sync="pagination"
          @request="init"
          flat
        >
          <template v-slot:body-cell-username="props">
            <fdev-td class="text-ellipsis">
              <router-link
                class="link"
                :to="`/user/list/${props.row.opno}`"
                :title="props.row.username"
                v-if="props.row.username"
                >{{ props.row.username }}</router-link
              >
              <div v-else title="-">-</div>
            </fdev-td>
          </template>
          <template v-slot:body-cell-btn="props">
            <fdev-td auto-width>
              <fdev-btn
                label="详情"
                flat
                size="sm"
                class="btn"
                color="primary"
                @click="openDetail(props.row.id)"
              />
            </fdev-td>
          </template>
        </fdev-table>
      </Loading>
      <f-dialog v-model="detailDialogOpened" f-sc title="历史信息详情">
        <!-- <Dialog v-model="detailDialogOpened" title="历史信息详情"> -->
        <div class="table-wrapper bg-white">
          <table class="table">
            <tr>
              <th>属性</th>
              <th>属性中文名</th>
              <th>变动前值</th>
              <th>变动后值</th>
            </tr>
            <tr v-for="item in detailData" :key="item.id">
              <td :title="item.name_en || '-'" class="text-ellipsis">
                {{ item.name_en }}
              </td>
              <td :title="item.name_cn || '-'" class="text-ellipsis">
                {{ item.name_cn }}
              </td>

              <td v-if="item.data_type === 'array'">
                <Chip
                  v-for="(old, i) in item.oldValue"
                  :key="i"
                  :data="old"
                  :name="item.name_en + i"
                />
              </td>
              <td v-else-if="item.data_type === 'object'">
                <Chip :data="item.oldValue" :name="item.name_en" />
              </td>
              <td v-else :title="item.oldValue || '-'" class="text-ellipsis">
                {{ item.oldValue }}
              </td>

              <td v-if="item.data_type === 'array'">
                <Chip
                  v-for="(old, i) in item.newValue"
                  :key="i"
                  :data="old"
                  :name="item.name_en + i"
                />
              </td>
              <td v-else-if="item.data_type === 'object'">
                <Chip :data="item.newValue" :name="item.name_en" />
              </td>
              <td v-else :title="item.newValue || '-'" class="text-ellipsis">
                {{ item.newValue }}
              </td>
            </tr>
          </table>
        </div>
        <template v-slot:btnSlot>
          <fdev-btn dialog label="确定" @click="detailDialogOpened = false" />
        </template>
      </f-dialog>
    </fdev-card>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState } from 'vuex';
import { modelEnvColumns } from '../../utils/constants';
import { setPagination, getPagination } from './setting';
import Chip from '../../components/Chip';

export default {
  name: 'modelEnvHistory',
  components: {
    Loading,
    Chip
  },
  data() {
    return {
      loading: false,
      list: [],
      columns: modelEnvColumns().historyColumns,
      pagination: {
        rowsPerPage: getPagination().rowsPerPage || 5,
        rowsNumber: 0,
        page: 1
      },
      detailDialogOpened: false,
      detailData: []
    };
  },
  computed: {
    ...mapState('environmentForm', ['HistoryMsgList', 'HistoryMsgDetail'])
  },
  watch: {
    'pagination.rowsPerPage': {
      handler(val) {
        setPagination({
          rowsPerPage: val
        });
      },
      deep: true
    }
  },
  methods: {
    ...mapActions('environmentForm', [
      'getMappingHistoryList',
      'getMappingHistoryDetail'
    ]),
    async openDetail(id) {
      await this.getMappingHistoryDetail({ id: id });
      this.detailData = this.HistoryMsgDetail;
      this.detailDialogOpened = true;
    },
    async init(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      this.loading = true;
      let params = {
        env_id: this.$route.params.id,
        model_id: this.$route.query.model_id,
        page: this.pagination.page,
        per_page: this.pagination.rowsPerPage
      };
      try {
        await this.getMappingHistoryList(params);
        this.pagination.rowsNumber = this.HistoryMsgList.total;
        this.list = this.HistoryMsgList.list;
      } finally {
        this.loading = false;
      }
    }
  },
  async created() {
    this.init();
  }
};
</script>

<style lang="stylus" scoped>

.table-wrapper
  width 100%
  padding 25px
  overflow-x auto
.table-wrapper .table
  width 100%
  border-radius 5px
  border-collapse collapse
  border 1px solid #bdbdbd
  td, th
    height 40px
    text-align center
    border 1px solid #bdbdbd
    color #616161
    min-width 80px
  tr:nth-of-type(2n)
    td, th
      background #999999
</style>
