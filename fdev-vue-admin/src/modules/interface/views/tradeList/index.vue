<template>
  <f-block class="wrapper">
    <Loading :visible="loading">
      <div class="bg-white">
        <fdev-tabs v-model="tab" align="left" @input="changeTab">
          <fdev-tab name="master" label="master" />
          <fdev-tab name="SIT" label="SIT" />
          <fdev-tab name="other" label="其他分支" />
        </fdev-tabs>
        <fdev-separator />
        <fdev-table
          :data="tableData"
          :columns="columns"
          row-key="index"
          :loading="loading"
          class="q-mt-md my-sticky-column-table"
          :on-search="onSearch"
          noExport
          title="交易列表"
          titleIcon="list_s_f"
          :pagination.sync="pagination"
          @request="onTableRequest"
          :visible-columns="visibleColumns"
        >
          <template v-slot:top-right>
            <span>
              <fdev-btn
                ficon="exit"
                label="全量导出"
                normal
                :disable="dowmloadLoading"
                @click="handleDownloadAll()"
              >
              </fdev-btn>
              <fdev-tooltip position="top" v-if="dowmloadLoading">
                正在导出，请稍后
              </fdev-tooltip>
            </span>
          </template>

          <template v-slot:top-bottom>
            <div class="row q-my-md form justify-between">
              <form class="row" @keyup.enter="init">
                <div class="col-4">
                  <f-formitem label="应用名" class="q-pr-sm">
                    <fdev-select
                      use-input
                      clearable
                      ref="transModel.serviceId"
                      :value="transModel.serviceId"
                      @input="queryServiceId($event)"
                      :options="filterProject"
                      @filter="projectFilter"
                      option-label="name_en"
                      option-value="name_en"
                      type="text"
                      class="input"
                    >
                      <template v-slot:option="scope">
                        <fdev-item
                          v-bind="scope.itemProps"
                          v-on="scope.itemEvents"
                        >
                          <fdev-item-section>
                            <fdev-item-label :title="scope.opt.name_en">{{
                              scope.opt.name_en
                            }}</fdev-item-label>
                            <fdev-item-label :title="scope.opt.name_zh" caption>
                              {{ scope.opt.name_zh }}
                            </fdev-item-label>
                          </fdev-item-section>
                        </fdev-item>
                      </template>
                    </fdev-select>
                  </f-formitem>
                  <f-formitem label="选择渠道" class="q-mt-sm q-pr-sm">
                    <fdev-select
                      :value="transModel.channel"
                      @input="queryChannel($event)"
                      type="text"
                      class="input"
                      option-label="label"
                      option-value="value"
                      map-options
                      emit-value
                      :options="channelOptions"
                    />
                  </f-formitem>
                  <f-formitem label="图片验证码" class="q-mt-sm q-pr-sm">
                    <fdev-select
                      :value="transModel.verCodeType"
                      @input="queryVerCode($event)"
                      type="text"
                      class="input"
                      option-label="label"
                      option-value="value"
                      map-options
                      :options="verCodeTypeOptions"
                      emit-value
                    />
                  </f-formitem>
                  <f-formitem
                    label="分支"
                    v-if="tab === 'other'"
                    class="q-mt-sm q-pr-sm"
                  >
                    <fdev-input
                      outlined
                      :value="transModel.branch"
                      @input="updateBranchOther($event)"
                      type="text"
                      class="input"
                    />
                  </f-formitem>
                </div>
                <div class="col-4">
                  <f-formitem label="是否记流水" class="q-pr-sm">
                    <fdev-select
                      :value="transModel.writeJnl"
                      @input="queryWriteJnl($event)"
                      type="text"
                      :options="options"
                      option-label="label"
                      option-value="value"
                      map-options
                      emit-value
                      class="input"
                    />
                  </f-formitem>
                  <f-formitem label="是否登录" class="q-mt-sm q-pr-sm">
                    <fdev-select
                      :value="transModel.needLogin"
                      @input="queryNeedLogin($event)"
                      option-label="label"
                      option-value="value"
                      map-options
                      emit-value
                      type="text"
                      :options="options"
                      class="input"
                    />
                  </f-formitem>
                  <f-formitem label="标签" class="q-mt-sm q-pr-sm">
                    <fdev-input
                      :value="transModel.tags"
                      @input="queryTags($event)"
                      type="text"
                      class="input"
                    />
                  </f-formitem>
                </div>

                <f-formitem
                  label="交易ID(可输入多个，以英文‘，’隔开)"
                  class="col-4"
                >
                  <fdev-input
                    ref="transModel.transId"
                    :value="transModel.transId"
                    @input="queryTransId($event)"
                    class="textarea"
                    type="textarea"
                  />
                </f-formitem>
              </form>
            </div>
          </template>

          <template v-slot:body-cell-transId="props">
            <fdev-td :title="props.row.transId || '-'">
              {{ props.row.transId || '-' }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-serviceId="props">
            <fdev-td class="text-ellipsis">
              <router-link
                v-if="props.row.appId"
                :to="{
                  path: `/app/list/${props.row.appId}-interface`
                }"
                class="link"
                :title="props.row.serviceId"
                >{{ props.row.serviceId }}</router-link
              >
              <div v-else :title="props.row.serviceId || '-'">
                {{ props.row.serviceId || '-' }}
              </div>
            </fdev-td>
          </template>

          <template v-slot:body-cell-channelIdMap="props">
            <fdev-td class="text-ellipsis">
              <span
                v-for="(channel, key, index) in props.row.channelIdMap"
                :key="index"
                class="linkTo"
              >
                <router-link
                  v-if="channel"
                  :to="{
                    path: `/interfaceAndRoute/interfaceTrading/list/${channel}`
                  }"
                  class="link"
                  :title="key"
                  >{{ key }}</router-link
                >
                <div v-else :title="key">
                  {{ key }}
                </div>
              </span>
            </fdev-td>
          </template>

          <template v-slot:body-cell-tag="props">
            <fdev-td auto-width auto-height>
              <div class="chip-height" v-if="props.row.tags.length > 0">
                <fdev-chip
                  v-for="tag in props.row.tags"
                  :key="tag"
                  color="orange"
                  text-color="white"
                  :label="tag"
                  dense
                  square
                />
              </div>
              <span v-else title="-">-</span>
            </fdev-td>
          </template>
        </fdev-table>
      </div>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
import {
  channel,
  verCodeTypeOptions,
  tradeListColumns
} from '../../utils/constants';
export default {
  name: 'InterfaceTrading',
  components: { Loading },
  data() {
    return {
      tableData: [],
      tab: 'master',
      dowmloadLoading: false,
      loading: false,
      columns: tradeListColumns(),
      options: [
        { label: '全部', value: '' },
        { label: '否', value: '0' },
        { label: '是', value: '1' }
      ],
      channelOptions: channel,
      verCodeTypeOptions: verCodeTypeOptions,
      filterProject: [],
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 5
      }
    };
  },
  watch: {
    pagination: {
      handler({ page, rowsPerPage }, old) {
        if (page !== old.page || rowsPerPage !== old.rowsPerPage) {
          this.pagination.page = page;
          this.pagination.rowsPerPage = rowsPerPage;
          this.onTableRequest({ pagination: this.pagination });
        }
      },
      deep: true
    }
  },
  computed: {
    ...mapState('interfaceForm', ['transList']),
    ...mapState('appForm', ['vueAppData']),
    ...mapState(
      'userActionSaveInterface/interfaceTrading/interfaceTradingList',
      [
        'transModelMaster',
        'transModelSit',
        'transModelOther',
        'visibleColumnsMaster',
        'visibleColumnsSit',
        'visibleColumnsOther'
      ]
    ),
    transModel() {
      if (this.masterBranch) {
        return this.transModelMaster;
      } else if (this.sitBranch) {
        return this.transModelSit;
      } else {
        return this.transModelOther;
      }
    },
    visibleColumns() {
      if (this.masterBranch) {
        return this.visibleColumnsMaster;
      } else if (this.sitBranch) {
        return this.visibleColumnsSit;
      } else {
        return this.visibleColumnsOther;
      }
    },
    masterBranch() {
      return this.tab === 'master';
    },
    sitBranch() {
      return this.tab === 'SIT';
    },
    dataFilter() {
      return {
        '0': '否',
        '1': '是'
      };
    }
  },
  methods: {
    ...mapActions('interfaceForm', ['queryTransList', 'exportTransList']),
    ...mapActions('appForm', ['queryApps']),
    ...mapMutations(
      'userActionSaveInterface/interfaceTrading/interfaceTradingList',
      [
        'updateServiceIdMaster',
        'updateServiceIdSit',
        'updateServiceIdOther',
        'updateChannelMaster',
        'updateChannelSit',
        'updateChannelOther',
        'updateWriteJnlMaster',
        'updateWriteJnlSit',
        'updateWriteJnlOther',
        'updateLoginMaster',
        'updateLoginSit',
        'updateLoginOther',
        'updateVerCodeMaster',
        'updateVerCodeSit',
        'updateVerCodeOther',
        'updateTagsMaster',
        'updateTagsSit',
        'updateTagsOther',
        'updateTransIdMaster',
        'updateTransIdSit',
        'updateTransIdOther',
        'updateBranchOther',
        'updateColumnsMaster',
        'updateColumnsSit',
        'updateColumnsOther'
      ]
    ),
    onSearch() {
      this.onTableRequest({ pagination: this.pagination });
    },
    queryServiceId(data) {
      if (this.masterBranch) {
        this.updateServiceIdMaster(data);
      } else if (this.sitBranch) {
        this.updateServiceIdSit(data);
      } else {
        this.updateServiceIdOther(data);
      }
    },
    queryChannel(data) {
      if (this.masterBranch) {
        this.updateChannelMaster(data);
      } else if (this.sitBranch) {
        this.updateChannelSit(data);
      } else {
        this.updateChannelOther(data);
      }
    },
    queryWriteJnl(data) {
      if (this.masterBranch) {
        this.updateWriteJnlMaster(data);
      } else if (this.sitBranch) {
        this.updateWriteJnlSit(data);
      } else {
        this.updateWriteJnlOther(data);
      }
    },
    queryNeedLogin(data) {
      if (this.masterBranch) {
        this.updateLoginMaster(data);
      } else if (this.sitBranch) {
        this.updateLoginSit(data);
      } else {
        this.updateLoginOther(data);
      }
    },
    queryVerCode(data) {
      if (this.masterBranch) {
        this.updateVerCodeMaster(data);
      } else if (this.sitBranch) {
        this.updateVerCodeSit(data);
      } else {
        this.updateVerCodeOther(data);
      }
    },
    queryTags(data) {
      if (this.masterBranch) {
        this.updateTagsMaster(data);
      } else if (this.sitBranch) {
        this.updateTagsSit(data);
      } else {
        this.updateTagsOther(data);
      }
    },
    queryTransId(data) {
      if (this.masterBranch) {
        this.updateTransIdMaster(data);
      } else if (this.sitBranch) {
        this.updateTransIdSit(data);
      } else {
        this.updateTransIdOther(data);
      }
    },
    selectVisibleColumns(data) {
      if (this.masterBranch) {
        this.updateColumnsMaster(data);
      } else if (this.sitBranch) {
        this.updateColumnsSit(data);
      } else {
        this.updateColumnsOther(data);
      }
    },
    async init(pagination) {
      this.loading = true;
      const { page, rowsPerPage } = pagination;
      const params = {
        ...this.transModel,
        transId: this.transModel.transId
          ? this.transModel.transId.split(',')
          : [],
        serviceId: this.transModel.serviceId
          ? this.transModel.serviceId.name_en
          : '',
        page,
        pageNum: rowsPerPage
      };
      try {
        await this.queryTransList(params);
        //拿数据
        this.tableData = this.transList.list;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
        this.pagination.rowsNumber = this.transList.total;
      } finally {
        this.loading = false;
      }
    },
    async onTableRequest({ pagination }) {
      const { page, rowsPerPage } = pagination;
      this.loading = true;
      const params = {
        ...this.transModel,
        transId: this.transModel.transId
          ? this.transModel.transId.split(',')
          : [],
        serviceId: this.transModel.serviceId
          ? this.transModel.serviceId.name_en
          : '',
        page,
        pageNum: rowsPerPage
      };
      try {
        await this.queryTransList(params);
        this.tableData = this.transList.list;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
        this.pagination.rowsNumber = this.transList.total;
      } finally {
        this.loading = false;
      }
    },
    changeTab() {
      this.tableData = [];
      this.transModel.branchDefault = this.tab;
    },
    addSelect(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    projectFilter(val, update) {
      update(() => {
        this.filterProject = this.vueAppData.filter(
          tag =>
            tag.name_zh.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            tag.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    },
    // 全量导出
    async handleDownloadAll() {
      this.dowmloadLoading = true;
      const params = {
        transId: [],
        transName: '',
        serviceId: '',
        branch: '',
        channel: '',
        writeJnl: '',
        needLogin: '',
        verCodeType: 'all',
        tags: '',
        branchDefault: this.tab
      };
      await this.exportTransList(params);
      this.dowmloadLoading = false;
    }
  },
  async created() {
    await this.queryApps();
    this.filterProject = this.vueAppData;
  }
};
</script>

<style lang="stylus" scoped>
.form
  width 100%
  .row
    align-items self-start
.chip-height
  width 150px
  white-space: initial;
.input
  &.col
    min-width 50px
.btn-height
  height 40px
// .textarea
//   width 220px
//   height 87px
.textarea >>> .q-field__control
  min-height 36px
  max-height 124px
.textarea >>> .q-field__native
  min-height 36px
  max-height 124px
.linkTo
  &:nth-of-type(n)::before
    content ','
  &:nth-of-type(1)::before
    content ''
.w150
 width: 150px
</style>
