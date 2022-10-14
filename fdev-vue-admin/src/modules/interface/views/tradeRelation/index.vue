<template>
  <f-block>
    <Loading :visible="loading" class="bg-white">
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
        class="bg-white q-mt-md"
        flat
        noExport
        :on-search="search"
        title="调用关系列表"
        titleIcon="list_s_f"
        :pagination.sync="pagination"
        @request="search"
        :visible-columns="visibleColumns"
      >
        <template v-slot:top-bottom>
          <form @keyup.enter="search">
            <div class="row">
              <div class="col-4">
                <f-formitem label="服务方" class="q-pr-sm">
                  <fdev-select
                    use-input
                    clearable
                    ref="invokeModel.serviceId"
                    :value="invokeModel.serviceId"
                    @input="queryServiceId($event)"
                    :options="filterProject"
                    @filter="projectFilter"
                    option-label="name_en"
                    option-value="name_en"
                    type="text"
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
                <f-formitem label="调用方" class="q-mt-sm q-pr-sm">
                  <fdev-select
                    use-input
                    clearable
                    ref="invokeModel.serviceCalling"
                    :value="invokeModel.serviceCalling"
                    @input="queryServiceCalling($event)"
                    :options="filterProject"
                    @filter="projectFilter"
                    option-label="name_en"
                    option-value="name_en"
                    type="text"
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
              </div>
              <div class="col-4">
                <f-formitem label="选择渠道" class="q-pr-sm">
                  <fdev-select
                    :value="invokeModel.channel"
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
                <f-formitem
                  label="分支名"
                  class="q-mt-sm q-pr-sm"
                  v-if="tab === 'other'"
                >
                  <fdev-input
                    ref="invokeModel.branch"
                    :value="invokeModel.branch"
                    @input="updateBranchOther($event)"
                    type="text"
                  />
                </f-formitem>
              </div>
              <f-formitem
                label="交易ID(可输入多个，以英文‘，’隔开)"
                class="col-4 q-pr-sm"
              >
                <fdev-input
                  ref="invokeModel.transId"
                  :value="invokeModel.transId"
                  @input="queryTransId($event)"
                  class="textarea"
                  type="textarea"
                />
              </f-formitem>
            </div>
            <div class="q-mr-sm q-mt-md">
              <div
                class="div-size bg-orange-2 text-deep-orange rounded-borders row"
              >
                <fdev-icon name="warning" class="q-ml-sm icon-warning" />
                <div class="inline-block q-my-sm detail">
                  调用关系的建立基于扫描调用方项目中的接口配置文件
                  <span
                    class="q-mb-xs q-pb-xs q-px-xs text-primary cursor-pointer"
                    @click="handlePDFDialog"
                    label="(扫描规范)"
                  >
                    (扫描规范)
                  </span>
                  ，为保证数据准确性，请及时更新维护接口配置
                </div>
              </div>
            </div>
          </form>
        </template>

        <template v-slot:body-cell-transId="props">
          <fdev-td class="text-ellipsis">
            <router-link
              v-if="props.row.transDetailId"
              :to="{
                path: `/interfaceAndRoute/interfaceTrading/list/${
                  props.row.transDetailId
                }`
              }"
              class="link"
              :title="props.row.transId"
              >{{ props.row.transId }}</router-link
            >
            <div v-else :title="props.row.transId || '-'">
              {{ props.row.transId || '-' }}
            </div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-serviceId="props">
          <fdev-td class="text-ellipsis">
            <router-link
              v-if="props.row.serviceProviderAppId"
              :to="{
                path: `/app/list/${props.row.serviceProviderAppId}-interface`
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

        <template v-slot:body-cell-serviceCalling="props">
          <fdev-td class="text-ellipsis">
            <router-link
              v-if="props.row.callingAppId"
              :to="{
                path: `/app/list/${props.row.callingAppId}-interface`
              }"
              class="link"
              :title="props.row.serviceCalling"
              >{{ props.row.serviceCalling }}</router-link
            >
            <div v-else :title="props.row.serviceCalling || '-'">
              {{ props.row.serviceCalling || '-' }}
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
    <PDFDialog v-model="PDFDialogOpen" />
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
import { channel, tradeRelationColumns } from '../../utils/constants';
import { setInvokePagination, getInvokePagination } from '../../utils/setting';
import PDFDialog from '../../components/PDFDialog';
export default {
  name: 'invokeRelationships',
  components: { Loading, PDFDialog },
  data() {
    return {
      loading: false,
      tab: 'master',
      tableData: [],
      PDFDialogOpen: false,
      channelOptions: [channel[0], channel[2], channel[4]],
      columns: tradeRelationColumns(),
      pagination: {
        rowsPerPage: getInvokePagination().rowsPerPage || 5,
        rowsNumber: 0,
        page: 1
      },
      filterProject: []
    };
  },
  watch: {
    'pagination.rowsPerPage': {
      handler(val) {
        setInvokePagination({
          rowsPerPage: val
        });
      },
      deep: true
    }
  },
  computed: {
    ...mapState('interfaceForm', {
      list: 'invokeRelationshipsList'
    }),
    ...mapState('appForm', ['vueAppData']),
    ...mapState(
      'userActionSaveInterface/interfaceTrading/invokeRelationships',
      [
        'invokeModelMaster',
        'invokeModelSit',
        'invokeModelOther',
        'visibleColumnsMaster',
        'visibleColumnsSit',
        'visibleColumnsOther'
      ]
    ),
    masterBranch() {
      return this.tab === 'master';
    },
    sitBranch() {
      return this.tab === 'SIT';
    },
    invokeModel() {
      if (this.masterBranch) {
        return this.invokeModelMaster;
      } else if (this.sitBranch) {
        return this.invokeModelSit;
      } else {
        return this.invokeModelOther;
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
    }
  },
  methods: {
    ...mapActions('interfaceForm', ['queryTransRelation']),
    ...mapActions('appForm', ['queryApps']),
    ...mapMutations(
      'userActionSaveInterface/interfaceTrading/invokeRelationships',
      [
        'updateServiceIdMaster',
        'updateServiceIdSit',
        'updateServiceIdOther',
        'updateCallingMaster',
        'updateCallingSit',
        'updateCallingOther',
        'updateChannelMaster',
        'updateChannelSit',
        'updateChannelOther',
        'updateBranchOther',
        'updateTransIdMaster',
        'updateTransIdSit',
        'updateTransIdOther',
        'updateColumnsMaster',
        'updateColumnsSit',
        'updateColumnsOther'
      ]
    ),
    queryServiceId(data) {
      if (this.masterBranch) {
        this.updateServiceIdMaster(data);
      } else if (this.sitBranch) {
        this.updateServiceIdSit(data);
      } else {
        this.updateServiceIdOther(data);
      }
    },
    queryServiceCalling(data) {
      if (this.masterBranch) {
        this.updateCallingMaster(data);
      } else if (this.sitBranch) {
        this.updateCallingSit(data);
      } else {
        this.updateCallingOther(data);
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
    changeTab() {
      this.tableData = [];
      this.pagination.page = 1;
    },
    handlePDFDialog() {
      this.PDFDialogOpen = true;
    },
    async search(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      this.loading = true;
      try {
        await this.queryTransRelation({
          ...this.invokeModel,
          serviceId: this.invokeModel.serviceId
            ? this.invokeModel.serviceId.name_en
            : '',
          serviceCalling: this.invokeModel.serviceCalling
            ? this.invokeModel.serviceCalling.name_en
            : '',
          ...this.pagination,
          branchDefault: this.tab
        });
        this.tableData = this.list.list;
        this.pagination.rowsNumber = this.list.total;
      } finally {
        this.loading = false;
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
    }
  },
  async created() {
    await this.queryApps();
    this.filterProject = this.vueAppData;
  }
};
</script>

<style lang="stylus" scoped>
form
  width 100%
  .row
    align-items self-start
input
  max-width 250px
// .textarea
//   width 300px
//   height 87px
.textarea >>> .q-field__control
  min-height 36px
  max-height 80px
.textarea >>> .q-field__native
  min-height 36px
  max-height 80px
.div-size
  // height 28px
  width calc(33.7% + 316px)
  font-size 10px
  .icon-warning
    margin-top 9px
    margin-right 4px
  .detail
    white-space normal
    flex 1
.w150
  width 150px
</style>
