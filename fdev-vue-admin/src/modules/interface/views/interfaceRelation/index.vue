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
          class="q-mt-md"
          noExport
          :on-search="searchByTermS"
          title="调用关系列表"
          titleIcon="list_s_f"
          no-data-label="没有可用数据，请输入查询条件"
          :pagination.sync="pagination"
          :visible-columns="visibleColumns"
        >
          <template v-slot:top-right>
            <fdev-btn normal @click="downloadRelation" label="下载调用关系" />
            <fdev-select
              input-debounce="0"
              options-dense
              :options="typeOptions"
              emit-value
              map-options
              option-label="label"
              option-value="value"
              class="table-head-input w150"
              :value="interfaceType"
              @input="selectInterfaceType($event)"
            />
          </template>
          <template v-slot:top-bottom>
            <form @keyup.enter="searchByTermS">
              <div class="row">
                <div :class="defaultBranch === 'other' ? 'col-4' : ''">
                  <f-formitem label="提供方应用">
                    <fdev-select
                      use-input
                      clearable
                      ref="interfaceModel.serviceId"
                      :value="interfaceModel.serviceId"
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
                  <f-formitem label="调用方应用" class="q-mt-sm q-pr-sm">
                    <fdev-select
                      use-input
                      clearable
                      ref="interfaceModel.serviceCalling"
                      :value="interfaceModel.serviceCalling"
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
                <div v-show="defaultBranch === 'other'" class="col-4">
                  <f-formitem label="分支名" class="q-pr-sm">
                    <fdev-input
                      ref="interfaceModel.branchName"
                      :value="interfaceModel.branchName"
                      @input="updateBranchNameOther($event)"
                      type="text"
                    />
                  </f-formitem>
                </div>
                <f-formitem
                  label="交易码,服务ID,操作ID(支持批量查询,以英文','隔开)"
                  :label-style="defaultBranch !== 'other' ? 'width:180px' : ''"
                  :class="defaultBranch === 'other' ? 'col-4' : ''"
                >
                  <fdev-input
                    ref="interfaceModel.transOrServiceOrOperation"
                    :value="interfaceModel.transOrServiceOrOperation"
                    @input="queryInputValue($event)"
                    class="textarea"
                    type="textarea"
                  />
                </f-formitem>
              </div>
              <div class="q-mr-sm q-mt-md row justify-between">
                <div>
                  <div
                    class="waring-row bg-orange-2  text-deep-orange rounded-borders q-pr-lg inline-block"
                  >
                    <f-icon
                      name="alert_t_f"
                      style="color:#EF6C00"
                      class="q-ml-sm q-pt-xs"
                    />
                    <div class="inline-block q-my-sm div-size">
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
              </div>
            </form>
          </template>

          <template v-slot:body-cell-transId="props">
            <fdev-td class="text-left text-ellipsis">
              <router-link
                :to="{
                  path: `/interfaceAndRoute/interface/interfaceProfile/${
                    props.row.interfaceId
                  }`,
                  query: { interfaceType: props.row.interfaceType }
                }"
                class="link"
                v-if="props.row.interfaceId"
                :title="props.row.transId"
                >{{ props.row.transId }}</router-link
              >
              <div v-else :title="props.row.transId || '-'">
                {{ props.row.transId || '-' }}
              </div>
            </fdev-td>
          </template>

          <template v-slot:body-cell-serviceId="props">
            <fdev-td class="text-left text-ellipsis">
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
            <fdev-td class="text-left text-ellipsis">
              <router-link
                v-if="props.row.callingAppId"
                :to="{
                  path: `/app/list/${props.row.callingAppId}-interface_${
                    props.row.serviceCalling
                  }`
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
          <template v-slot:body-cell-description="props">
            <fdev-td
              class="text-ellipsis"
              :title="props.row.description || '-'"
            >
              <span>{{ props.row.description || '-' }}</span>
            </fdev-td>
          </template>
          <template v-slot:body-cell-uri="props">
            <fdev-td class="text-ellipsis" :title="props.row.uri || '-'">
              <a
                v-if="props.row.uri"
                :href="props.row.uri"
                target="_blank"
                class="text-primary"
                >{{ props.row.uri }}</a
              >
              <div v-else title="-">-</div>
            </fdev-td>
          </template>
        </fdev-table>
      </div>
    </Loading>
    <PDFDialog v-model="PDFDialogOpen" />
  </f-block>
</template>

<script>
import { required } from 'vuelidate/lib/validators';
import Loading from '@/components/Loading';
import { errorNotify } from '@/utils/utils';
import { relationTypeOptions, columns } from '../../utils/constants';
import { mapActions, mapState, mapMutations } from 'vuex';
import PDFDialog from '../../components/PDFDialog';
import { setApplyPagination, getApplyPagination } from '../../utils/setting';

export default {
  name: 'masterList',
  components: { Loading, PDFDialog },
  data() {
    return {
      filter: '',
      PDFDialogOpen: false,
      typeOptions: relationTypeOptions,
      loading: false,
      searchByTerm: false,
      defaultBranch: 'master',
      columns: columns(),
      tableData: [],
      tab: 'schema',
      pagination: getApplyPagination(),
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
  computed: {
    ...mapState('interfaceForm', ['interfaceRelation']),
    ...mapState('interfaceForm', ['exportData']),
    ...mapState('appForm', ['vueAppData']),
    ...mapState('userActionSaveInterface/interFace/interfaceRelation', [
      'interfaceModelMaster',
      'interfaceModelSit',
      'interfaceModelOther',
      'interfaceTypeMaster',
      'interfaceTypeSit',
      'interfaceTypeOther',
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
    interfaceModel() {
      if (this.masterBranch) {
        return this.interfaceModelMaster;
      } else if (this.sitBranch) {
        return this.interfaceModelSit;
      } else {
        return this.interfaceModelOther;
      }
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
  watch: {
    interfaceType(val) {
      if (val === '调用报文类型') {
        this.tableData = this.interfaceRelation.list;
      } else {
        if (this.interfaceRelation.list) {
          this.tableData = this.interfaceRelation.list.filter(item => {
            return item.interfaceType === val;
          });
        }
      }
    },
    //  interfaceType(newVal, oldVal) {
    //   if (newVal !== oldVal) {
    //     this.searchByTermS();
    //   }
    // },
    // 'interfaceModel.serviceId'(newVal, oldVal) {
    //   if (newVal !== oldVal) {
    //     this.searchByTermS();
    //   }
    // },
    // 'interfaceModel.serviceCalling'(newVal, oldVal) {
    //   if (newVal !== oldVal) {
    //     this.searchByTermS();
    //   }
    // },
    // 'interfaceModel.branchName'(val) {
    //   if (!val) {
    //     this.searchByTermS();
    //   }
    // },
    // 'interfaceModel.transOrServiceOrOperation'(val) {
    //   if (!val) {
    //     this.searchByTermS();
    //   }
    // },
    pagination(val) {
      setApplyPagination({
        rowsPerPage: val.rowsPerPage
      });
    }
  },
  methods: {
    ...mapMutations('interfaceForm', ['saveInterfaceRelation']),
    ...mapActions('interfaceForm', ['queryInterfaceRelation']),
    ...mapActions('interfaceForm', ['downloadRestRelationExcel']),
    ...mapActions('appForm', ['queryApps']),
    ...mapMutations('userActionSaveInterface/interFace/interfaceRelation', [
      'updateServiceIdMaster',
      'updateServiceIdSit',
      'updateServiceIdOther',
      'updateCallingMaster',
      'updateCallingSit',
      'updateCallingOther',
      'updateBranchNameOther',
      'updateInputMaster',
      'updateInputSit',
      'updateInputOther',
      'updateTypeMaster',
      'updateTypeSit',
      'updateTypeOther',
      'updateColumnsMaster',
      'updateColumnsSit',
      'updateColumnsOther'
    ]),
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
    queryInputValue(data) {
      if (this.masterBranch) {
        this.updateInputMaster(data);
      } else if (this.sitBranch) {
        this.updateInputSit(data);
      } else {
        this.updateInputOther(data);
      }
    },
    selectInterfaceType(data) {
      if (this.masterBranch) {
        this.updateTypeMaster(data);
      } else if (this.sitBranch) {
        this.updateTypeSit(data);
      } else {
        this.updateTypeOther(data);
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

    searchByTermS(props) {
      this.searchByTerm = true;
      this.query(props);
      this.searchByTerm = false;
    },
    handlePDFDialog() {
      this.PDFDialogOpen = true;
    },
    async downloadRelation() {
      this.loading = true;
      let interfaceType = '';
      if (this.interfaceType !== '调用报文类型') {
        interfaceType = this.interfaceType;
      }
      const params = {
        transOrServiceOrOperation: this.interfaceModel.transOrServiceOrOperation
          ? this.interfaceModel.transOrServiceOrOperation.split(',')
          : [],
        serviceCalling: this.interfaceModel.serviceCalling
          ? this.interfaceModel.serviceCalling.name_en
          : '',
        serviceId: this.interfaceModel.serviceId.name_en,
        branchDefault: this.defaultBranch,
        interfaceType: interfaceType,
        branch: this.interfaceModel.branchName // 以后其他分支使用
      };
      try {
        await this.downloadRestRelationExcel(params);
      } finally {
        this.loading = false;
      }
      this.loading = false;
      window.open(this.exportData);
    },
    async query(props) {
      const baseColumns = columns();
      if (this.interfaceType !== 'REST') {
        baseColumns.splice(2, 0, {
          name: 'esbServiceId',
          label: '服务ID',
          field: 'esbServiceId',
          align: 'left'
        });
        baseColumns.splice(3, 0, {
          name: 'esbOperationId',
          label: '操作ID',
          field: 'esbOperationId',
          align: 'left'
        });
      }
      let interfaceType = '';
      if (this.interfaceType !== '调用报文类型') {
        interfaceType = this.interfaceType;
      }
      this.columns = baseColumns;
      let params = {
        transOrServiceOrOperation: this.interfaceModel.transOrServiceOrOperation
          ? this.interfaceModel.transOrServiceOrOperation.split(',')
          : [],
        serviceCalling: this.interfaceModel.serviceCalling
          ? this.interfaceModel.serviceCalling.name_en
          : '',
        serviceId: this.interfaceModel.serviceId
          ? this.interfaceModel.serviceId.name_en
          : '',
        branchDefault: this.defaultBranch,
        interfaceType: interfaceType,
        branch: this.interfaceModel.branchName // 以后其他分支使用
      };

      const canOperation =
        Object.values(params).filter(v => v !== '' && v !== undefined).length >
        0;
      if (!canOperation) {
        errorNotify('请至少再输入一项其他查询条件');
        return;
      }
      this.loading = true;
      try {
        await this.queryInterfaceRelation(params);
        this.tableData = this.interfaceRelation.list;
      } finally {
        this.loading = false;
      }
    },
    changeTab() {
      this.tableData = [];
      this.pagination.page = 1;
      this.interfaceModel.defaultBranch = this.tab;
      this.saveInterfaceRelation([]);
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
    this.saveInterfaceRelation([]);
  }
};
</script>

<style lang="stylus" scoped>

form
  width 100%
  .row
    align-items self-start
.td-width
	box-sizing border-box
	max-width 130px
	overflow hidden
	text-overflow ellipsis
.btn-height
  height 40px
.input
  max-width 250px
// .textarea
//   height 87px
.textarea >>> .q-field__control
  min-height 36px
  max-height 80px
.textarea >>> .q-field__native
  min-height 36px
  max-height 80px
.div-size
  margin-top: 0px
  margin-bottom: 0px
  font-size 10px
.my-requst
  width 130px
  margin-top -20px
  margin-right 10px
.my-textarea
  width 100%
.div-right
  align right
.my-requst-right
  display: flex
  justify-content: flex-end
.waring-row
 margin-top: 10px
 margin-right: 8px
.w150
  width 150px
</style>
