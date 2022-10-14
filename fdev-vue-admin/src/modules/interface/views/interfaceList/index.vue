<template>
  <f-block>
    <Loading :visible="loading">
      <div class="bg-white">
        <fdev-tabs v-model="defaultBranch" align="left" @input="changeTab">
          <fdev-tab name="master" label="master" />
          <fdev-tab name="SIT" label="SIT" />
          <fdev-tab name="other" label="其他分支" />
        </fdev-tabs>
        <fdev-separator />
        <fdev-table
          :data="tableData"
          :columns="columns"
          row-key="id"
          :loading="loading"
          :filter="filter"
          class="q-mt-md my-sticky-column-table"
          noExport
          :on-search="searchByTermS"
          title="接口列表"
          titleIcon="list_s_f"
          no-data-label="没有可用数据，请输入查询条件"
          @request="query"
          :pagination.sync="pagination"
          :visible-columns="visibleColumns"
        >
          <template v-slot:top-right>
            <fdev-btn normal dialog @click="handlePDFDialog" label="显示规范" />
          </template>
          <template v-slot:top-bottom>
            <form @keyup.enter="searchByTermS">
              <div class="row">
                <div :class="defaultBranch === 'other' ? 'col-4' : ''">
                  <f-formitem label="提供报文类型" class="q-pr-sm">
                    <fdev-select
                      input-debounce="0"
                      :options="typeOptions"
                      emit-value
                      map-options
                      option-label="label"
                      option-value="value"
                      :value="interfaceType"
                      @input="queryInterfaceType($event)"
                    />
                  </f-formitem>
                  <f-formitem label="提供方应用" class="q-mt-sm q-pr-sm">
                    <fdev-select
                      use-input
                      clearable
                      ref="interfaceModel.serviceProvider"
                      :value="interfaceModel.serviceProvider"
                      @input="queryServiceProvider($event)"
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
                <div v-show="defaultBranch === 'other'" class="col-4">
                  <f-formitem label="分支名" class="q-pr-sm">
                    <fdev-input
                      ref="interfaceModel.branchName"
                      :value="interfaceModel.branchName"
                      @input="updateBranchNameOther($event)"
                      type="text"
                      class="input"
                    />
                  </f-formitem>
                </div>
                <f-formitem
                  :label="label"
                  :label-style="defaultBranch !== 'other' ? 'width:180px' : ''"
                  :class="defaultBranch === 'other' ? 'col-4' : ''"
                >
                  <fdev-input
                    ref="interfaceModel.transactionCode"
                    :value="interfaceModel.transactionCode"
                    @input="queryTransactionCode($event)"
                    class="textarea"
                    type="textarea"
                  />
                </f-formitem>
              </div>
            </form>
          </template>

          <template v-slot:body-cell-transId="props">
            <fdev-td class="text-left text-ellipsis">
              <router-link
                :to="{
                  path: `/interfaceAndRoute/interface/interfaceProfile/${
                    props.row.id
                  }`,
                  query: { interfaceType: props.row.interfaceType }
                }"
                class="link"
                v-if="props.row.id"
                :title="props.row.transId"
                >{{ props.row.transId }}</router-link
              >
              <div v-else :title="props.row.transId || '-'">
                {{ props.row.transId || '-' }}
              </div>
            </fdev-td>
          </template>

          <template v-slot:body-cell-interfaceName="props">
            <fdev-td
              class="text-ellipsis"
              :title="props.row.interfaceName || '-'"
            >
              <span>{{ props.row.interfaceName || '-' }}</span>
            </fdev-td>
          </template>

          <template v-slot:body-cell-serviceId="props">
            <fdev-td class="text-left text-ellipsis">
              <router-link
                v-if="props.row.appId"
                :to="{
                  path: `/app/list/${props.row.appId}-interface_${
                    props.row.serviceId
                  }`
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
          <template v-slot:body-cell-description="props">
            <fdev-td
              class="text-ellipsis"
              :title="props.row.description || '-'"
            >
              <span>{{ props.row.description || '-' }}</span>
            </fdev-td>
          </template>
          <template v-slot:body-cell-operation="props" v-if="limit">
            <fdev-td :auto-width="true" class="td-width">
              <fdev-btn
                flat
                label="申请调用"
                @click="handleApplyCallDialog(props.row)"
              />
            </fdev-td>
          </template>
        </fdev-table>
      </div>
    </Loading>
    <PDFDialog v-model="PDFDialogOpen" />
    <ApplyCallDialog v-model="applyCallDialogOpen" :selector="selector" />
  </f-block>
</template>

<script>
import { required } from 'vuelidate/lib/validators';
import Loading from '@/components/Loading';
import { typeOptions, interfaceColumns } from '../../utils/constants';
import { mapActions, mapState, mapMutations } from 'vuex';
import PDFDialog from '../../components/PDFDialog';
import ApplyCallDialog from '../../components/applyCallDialog';
import { errorNotify } from '@/utils/utils';

export default {
  name: 'masterList',
  components: { Loading, PDFDialog, ApplyCallDialog },
  data() {
    return {
      PDFDialogOpen: false,
      filter: '',
      typeOptions: typeOptions,
      loading: false,
      searchByTerm: false,
      defaultBranch: 'master',
      tableData: [],
      tab: 'schema',
      columns: interfaceColumns(),
      pagination: {
        rowsPerPage: 5,
        rowsNumber: 0,
        page: 1
      },
      applyCallDialogOpen: false,
      selector: {},
      filterProject: []
    };
  },
  validations: {
    interfaceScanModel: {
      cloneUrl: {
        required
      },
      branchName: {
        required
      }
    }
  },
  // watch: {
  //   interfaceType(newVal, oldVal) {
  //     if (newVal !== oldVal) {
  //       this.searchByTermS();
  //     }
  //   },
  //   'interfaceModel.serviceProvider'(newVal, oldVal) {
  //     if (newVal !== oldVal) {
  //       this.searchByTermS();
  //     }
  //   },
  //   'interfaceModel.branchName'(val) {
  //     if (!val) {
  //       this.searchByTermS();
  //     }
  //   },
  //   'interfaceModel.transactionCode'(val) {
  //     if (!val) {
  //       this.searchByTermS();
  //     }
  //   }
  // },
  computed: {
    ...mapState('interfaceForm', ['interfaceList', 'limit']),
    ...mapState('appForm', ['vueAppData']),
    ...mapState('userActionSaveInterface/interFace/interfaceList', [
      'interfaceTypeMaster',
      'interfaceTypeSit',
      'interfaceTypeOther',
      'interfaceModelMaster',
      'interfaceModelSit',
      'interfaceModelOther',
      'visibleColumnsMaster',
      'visibleColumnsSit',
      'visibleColumnsOther'
    ]),
    masterBranch() {
      return this.defaultBranch === 'master';
    },
    sitBranch() {
      return this.defaultBranch === 'SIT';
    },
    interfaceType() {
      if (this.masterBranch) {
        return this.interfaceTypeMaster;
      } else if (this.sitBranch) {
        return this.interfaceTypeSit;
      } else {
        return this.interfaceTypeOther;
      }
    },
    interfaceModel() {
      if (this.masterBranch) {
        return this.interfaceModelMaster;
      } else if (this.sitBranch) {
        return this.interfaceModelSit;
      } else {
        return this.interfaceModelOther;
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

    label() {
      return this.interfaceType === 'SOAP'
        ? "交易码,服务ID,操作ID(支持批量查询,以英文','隔开)"
        : '交易码(可输入多个，以英文‘，’隔开)';
    },
    visibleOptions() {
      const arr = this.columns.slice(0);
      return arr.splice(0, arr.length - 1);
    }
  },
  methods: {
    ...mapActions('interfaceForm', ['queryInterfaceList', 'isManagers']),
    ...mapMutations('interfaceForm', ['saveInterfaceList']),
    ...mapActions('appForm', ['queryApps']),
    ...mapMutations('userActionSaveInterface/interFace/interfaceList', [
      'updateInterfaceTypeMaster',
      'updateInterfaceTypeSit',
      'updateInterfaceTypeOther',
      'updateServiceProviderMaster',
      'updateServiceProviderSit',
      'updateServiceProviderOther',
      'updateBranchNameOther',
      'updateCoderMaster',
      'updateCodeSit',
      'updateCodeOther',
      'updateBranchName',
      'updateColumnsMaster',
      'updateColumnsSit',
      'updateColumnsOther'
    ]),
    queryInterfaceType(data) {
      if (this.masterBranch) {
        this.updateInterfaceTypeMaster(data);
      } else if (this.sitBranch) {
        this.updateInterfaceTypeSit(data);
      } else {
        this.updateInterfaceTypeOther(data);
      }
    },
    queryServiceProvider(data) {
      if (this.masterBranch) {
        this.updateServiceProviderMaster(data);
      } else if (this.sitBranch) {
        this.updateServiceProviderSit(data);
      } else {
        this.updateServiceProviderOther(data);
      }
    },
    queryTransactionCode(data) {
      if (this.masterBranch) {
        this.updateCoderMaster(data);
      } else if (this.sitBranch) {
        this.updateCodeSit(data);
      } else {
        this.updateCodeOther(data);
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
    async searchByTermS(props) {
      this.searchByTerm = true;
      await this.isManagers();
      this.query(props);
      this.searchByTerm = false;
    },
    async query(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      const baseColumns = [...interfaceColumns()];
      this.columns = baseColumns;
      if (
        this.limit &&
        this.columns[this.columns.length - 1].name !== 'operation' &&
        this.interfaceType === 'REST'
      ) {
        this.columns.push({
          name: 'operation',
          label: '操作'
        });
        if (this.visibleColumns.indexOf('operation') < 0) {
          this.visibleColumns.push('operation');
        }
      }
      let params = {
        page: this.pagination.page,
        pageNum: this.pagination.rowsPerPage,
        interfaceName: this.interfaceModel.interfaceName,
        serviceId: this.interfaceModel.serviceProvider
          ? this.interfaceModel.serviceProvider.name_en
          : '',
        transId: this.interfaceModel.transactionCode
          ? this.interfaceModel.transactionCode.split(',')
          : [],
        branchDefault: this.defaultBranch,
        interfaceType: this.interfaceType,
        branch: this.interfaceModel.branchName // 以后其他分支使用
      };
      const canOperation =
        Object.values(params).filter(v => v !== '' && v !== undefined).length >
        2;
      if (!canOperation) {
        errorNotify('请至少再输入一项其他查询条件');
        return;
      }
      this.loading = true;
      try {
        await this.queryInterfaceList(params);
        this.tableData = this.interfaceList.list;
        this.pagination.rowsNumber = this.interfaceList.total;
      } finally {
        this.loading = false;
      }
    },
    changeTab() {
      this.tableData = [];
      this.pagination.page = 1;
      this.interfaceModel.defaultBranch = this.tab;
      this.saveInterfaceList([]);
    },
    handlePDFDialog() {
      this.PDFDialogOpen = true;
    },
    handleApplyCallDialog(row) {
      this.selector = row;
      this.applyCallDialogOpen = true;
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
    }
  },
  async created() {
    // this.searchByTermS();
    await this.queryApps();
    this.filterProject = this.vueAppData;
  },
  destroyed() {
    this.saveInterfaceList([]);
  }
};
</script>

<style lang="stylus" scoped>

form
  width 100%
.td-width
	box-sizing border-box
	max-width 130px
	overflow hidden
	text-overflow ellipsis
.dialog-width
  width 700px
  max-width 700px
.btn-height
  height 40px
.input
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
.my-sticky-column-table >>>
  th:first-child,td:first-child
    // background-color #fff
    // opacity 1
  th:first-child
    // color #9c9a9a
  th:first-child,td:first-child
    position sticky
    left 0
    z-index 1
  .td-desc
    max-width 300px
    overflow hidden
    text-overflow ellipsis
.w150
  width 150px
</style>
