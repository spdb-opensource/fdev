<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-table
        :columns="columns"
        :pagination.sync="pagination"
        :visible-columns="visibleColumns"
        :data="tableData"
        @request="init"
        :onSelectCols="updatevisibleColumns"
        noExport
        title="数据字典登记表"
        title-icon="list_s_f"
      >
        <template v-slot:top-bottom>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="所属系统">
            <fdev-select
              :options="systemNamesData"
              option-label="system_name_cn"
              option-value="sys_id"
              map-options
              @input="systemNameChange($event)"
              @filter="systemNamesFilter"
              :value="dictionaryModel.sys_id"
              use-input
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.system_name_cn">
                      {{ scope.opt.system_name_cn }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.database_name">
                      {{ scope.opt.database_name }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="数据库类型">
            <fdev-select
              @filter="filterGroup"
              :options="databaseType"
              option-label="appName_cn"
              :value="dictionaryModel.database_type"
              @input="updateDatabase_type($event)"
              use-input
            />
          </f-formitem>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="库名">
            <span>
              <fdev-tooltip v-if="canSelect">{{ canSelectTip }}</fdev-tooltip>
              <fdev-select
                :options="databaseNameOptions"
                option-label="database_name"
                @input="databaseNameChange($event)"
                :disable="canSelect"
                :value="dictionaryModel.database_name"
                use-input
              />
            </span>
          </f-formitem>
          <f-formitem class="col-4 q-pr-sm" bottom-page label="表名">
            <fdev-select
              use-input
              :options="tableOptions"
              @filter="tableFilter"
              :value="dictionaryModel.name"
              @input="updateName($event)"
            />
          </f-formitem>
        </template>
        <template v-slot:body-cell-system_name_cn="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.row.sysInfo.system_name_cn"
          >
            <span>{{ props.row.sysInfo.system_name_cn || '-' }}</span>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.sysInfo.system_name_cn || '-' }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>
        <template v-slot:body-cell-database_name="props">
          <fdev-td class="text-ellipsis" :title="props.row.database_name">
            {{ props.row.database_name || '-' }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-database_type="props">
          <fdev-td class="text-ellipsis" :title="props.row.database_type">
            {{ props.row.database_type || '-' }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-reviewers="props">
          <fdev-td class="text-ellipsis" :title="props.vlaue">
            {{ props.value || '-' }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-tableName="props">
          <fdev-td class="text-ellipsis" :title="props.value">
            {{ props.value || '-' }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-appName="props">
          <fdev-td class="text-ellipsis" :title="props.value">
            {{ props.value || '-' }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value || '-' }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>
        <template v-slot:body-cell-btn="props">
          <fdev-td class="justify-center">
            <fdev-btn
              label="详情"
              class="q-mr-sm"
              flat
              @click="openDetail(props.row)"
            />
            <fdev-btn label="编辑" flat @click="editDetail(props.row)" />
          </fdev-td>
        </template>
      </fdev-table>

      <!-- 详情弹窗 -->
      <f-dialog title="详情" v-model="handleShow" right>
        <div class="rdia-dc-w row justify-between">
          <f-formitem label="系统中文名">
            {{
              modelDataMessage.sysInfo
                ? modelDataMessage.sysInfo.system_name_cn
                : ''
            }}
          </f-formitem>
          <f-formitem label="数据库类型">
            {{ modelDataMessage.database_type }}
          </f-formitem>
          <f-formitem label="数据库名">
            {{ modelDataMessage.database_name }}
          </f-formitem>
          <f-formitem label="表名">
            {{ modelDataMessage.table_name }}
          </f-formitem>
          <f-formitem label="是否为新增表">
            {{ modelDataMessage.is_new_table === 'Y' ? '是' : '否' }}
          </f-formitem>
          <f-formitem label="备注">
            {{ modelDataMessage.remark }}
          </f-formitem>
          <f-formitem label="首次维护时间">
            {{ modelDataMessage.first_modi_time }}
          </f-formitem>
          <f-formitem label="首次维护人英文名">
            {{ modelDataMessage.first_modi_userNameEn }}
          </f-formitem>
          <f-formitem label="首次维护人中文名">
            {{ modelDataMessage.first_modi_userName }}
          </f-formitem>
          <f-formitem label="最后维护日期">
            {{ modelDataMessage.last_modi_time }}
          </f-formitem>
          <f-formitem label="最后维护人英文名">
            {{ modelDataMessage.last_modi_userNameEn }}
          </f-formitem>
          <f-formitem label="最后维护人中文名">
            {{ modelDataMessage.last_modi_userName }}
          </f-formitem>
        </div>
        <fdev-table
          :data="taskDetailsColumns"
          :columns="detailsColumns"
          title="表中所有字段及类型"
          title-icon="list_s_f"
          noExport
          class="q-mt-md"
          no-select-cols
        >
          <template v-slot:body-cell-field="props">
            <fdev-td class="text-ellipsis" :title="props.row.field">
              {{ props.row.field || '-' }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-field_cn="props">
            <fdev-td class="text-ellipsis" :title="props.row.field_cn">
              {{ props.row.field_cn || '-' }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-field_type="props">
            <fdev-td class="text-ellipsis" :title="props.row.field_type">
              {{ props.row.field_type || '-' }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-field_length="props">
            <fdev-td class="text-ellipsis" :title="props.row.field_length">
              {{ props.row.field_length || '-' }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-is_list_field="props">
            <fdev-td class="text-ellipsis" :title="props.value">
              {{ props.value || '-' }}
            </fdev-td>
          </template>
        </fdev-table>

        <fdev-table
          :data="newTaskDetailsColumns"
          :columns="detailsColumns"
          title="新增字段及类型"
          title-icon="list_s_f"
          noExport
          class="q-mt-md"
          no-select-cols
        >
          <template v-slot:body-cell-field="props">
            <fdev-td class="text-ellipsis" :title="props.row.field">
              {{ props.row.field || '-' }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.field || '-' }}
                </fdev-banner>
              </fdev-popup-proxy>
            </fdev-td>
          </template>
          <template v-slot:body-cell-field_cn="props">
            <fdev-td class="text-ellipsis" :title="props.row.field_cn">
              {{ props.row.field_cn || '-' }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.field_cn || '-' }}
                </fdev-banner>
              </fdev-popup-proxy>
            </fdev-td>
          </template>
          <template v-slot:body-cell-field_type="props">
            <fdev-td class="text-ellipsis" :title="props.row.field_type">
              {{ props.row.field_type || '-' }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-field_length="props">
            <fdev-td class="text-ellipsis" :title="props.row.field_length">
              {{ props.row.field_length || '-' }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-is_list_field="props">
            <fdev-td class="text-ellipsis" :title="props.value">
              {{ props.value || '-' }}
            </fdev-td>
          </template>
        </fdev-table>
      </f-dialog>

      <!-- 编辑页 -->
      <f-dialog
        right
        v-model="editShow"
        :title="detailTableName"
        childTitle="新增"
        @closeLeftDia="beforeClose"
        :leftDia="addDataModalOpened"
        @before-close="beforeClose"
      >
        <fdev-table
          :columns="keyColums"
          :data="modelEvn"
          noExport
          title="字段及类型表"
          title-icon="list_s_f"
          no-select-cols
          class="my-table-fst my-sticky-column-table"
        >
          <template v-slot:top-right>
            <fdev-btn
              normal
              label="新增"
              ficon="add"
              @click="handleDataAddModalOpened"
            />
          </template>
          <template v-slot:body-cell-field="props">
            <fdev-td class="text-ellipsis" :title="props.value">
              <span>{{ props.value || '-' }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value || '-' }}
                </fdev-banner>
              </fdev-popup-proxy>
            </fdev-td>
          </template>
          <template v-slot:body-cell-field_cn="props">
            <fdev-td auto-width :title="props.value">
              {{ props.value || '-' }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value || '-' }}
                </fdev-banner>
              </fdev-popup-proxy>
            </fdev-td>
          </template>
          <template v-slot:body-cell-field_length="props">
            <fdev-td class="text-ellipsis" :title="props.value">
              {{ props.value || '-' }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-field_type="props">
            <fdev-td class="text-ellipsis" :title="props.value">
              {{ props.value || '-' }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-is_list_field="props">
            <fdev-td class="text-ellipsis" :title="props.value">
              {{ props.value || '-' }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-btn="props">
            <fdev-td>
              <fdev-btn
                color="red"
                label="删除"
                flat
                @click="deleteDetail(props.row)"
              />
            </fdev-td>
          </template>
        </fdev-table>

        <template v-slot:leftDiaContent>
          <fdev-table
            :data="tableDataList"
            :columns="childrenColumns"
            :pagination.sync="addPagination"
            row-key="record_id"
            @request="changeAddPagination"
            selection="multiple"
            :selected.sync="selected"
            title="数据字典登记表"
            title-icon="list_s_f"
            noExport
            no-select-cols
            class="my-table-left"
          >
            <template v-slot:top-bottom>
              <f-formitem label="所属系统" page class="q-pr-sm">
                <fdev-select
                  :options="systemNamesData"
                  option-label="system_name_cn"
                  option-value="sys_id"
                  map-options
                  @filter="systemNamesFilter"
                  use-input
                  @input="submitAddData"
                  v-model="editModel.sys_id"
                >
                  <template v-slot:option="scope">
                    <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                      <fdev-item-section>
                        <fdev-item-label :title="scope.opt.system_name_cn">
                          {{ scope.opt.system_name_cn }}
                        </fdev-item-label>
                        <fdev-item-label
                          caption
                          :title="scope.opt.database_name"
                        >
                          {{ scope.opt.database_name }}
                        </fdev-item-label>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </f-formitem>
              <f-formitem label="数据库类型" page>
                <fdev-select
                  :options="databaseType"
                  option-label="appName_cn"
                  v-model="editModel.database_type"
                  @input="submitAddData"
                  use-input
                />
              </f-formitem>
              <f-formitem label="数据库名" page>
                <span>
                  <fdev-tooltip v-if="!editModel.sys_id">{{
                    canSelectTip
                  }}</fdev-tooltip>
                  <fdev-select
                    :options="databaseNameOptions"
                    option-label="database_name"
                    @filter="filterGroup"
                    :disable="!editModel.sys_id"
                    v-model="editModel.database_name"
                    @input="submitAddData"
                    use-input
                  />
                </span>
              </f-formitem>
            </template>
            <template v-slot:body-cell-system_name_cn="props">
              <fdev-td
                class="text-ellipsis"
                :title="props.row.sysInfo.system_name_cn"
              >
                {{ props.row.sysInfo.system_name_cn || '-' }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.sysInfo.system_name_cn || '-' }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </fdev-td>
            </template>
            <template v-slot:body-cell-database_type="props">
              <fdev-td
                class="text-ellipsis"
                :title="props.row.dataDict.database_type"
              >
                {{ props.row.dataDict.database_type || '-' }}
              </fdev-td>
            </template>
            <template v-slot:body-cell-database_name="props">
              <fdev-td
                class="text-ellipsis"
                :title="props.row.dataDict.database_name"
              >
                {{ props.row.dataDict.database_name || '-' }}
              </fdev-td>
            </template>
            <template v-slot:body-cell-field_en_name="props">
              <fdev-td
                class="text-ellipsis"
                :title="props.row.dataDict.field_en_name"
              >
                {{ props.row.dataDict.field_en_name || '-' }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.dataDict.field_en_name || '-' }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </fdev-td>
            </template>
            <template v-slot:body-cell-field_ch_name="props">
              <fdev-td
                class="text-ellipsis"
                :title="props.row.dataDict.field_ch_name"
              >
                {{ props.row.dataDict.field_ch_name || '-' }}
              </fdev-td>
            </template>
            <template v-slot:body-cell-field_type="props">
              <fdev-td
                class="text-ellipsis"
                :title="props.row.dataDict.field_type"
              >
                {{ props.row.dataDict.field_type || '-' }}
              </fdev-td>
            </template>
            <template v-slot:body-cell-field_length="props">
              <fdev-td
                class="text-ellipsis"
                :title="props.row.dataDict.field_length"
              >
                {{ props.row.dataDict.field_length || '-' }}
              </fdev-td>
            </template>
            <template v-slot:body-cell-list_field_desc="props">
              <fdev-td class="text-ellipsis" :title="props.row.value">
                {{ props.value || '-' }}
              </fdev-td>
            </template>
            <template v-slot:body-cell-remark="props">
              <fdev-td class="text-ellipsis" :title="props.row.value">
                {{ props.value || '-' }}
              </fdev-td>
            </template>
          </fdev-table>
        </template>
        <template v-slot:btnSlot>
          <fdev-btn label="取消" dialog outline @click="closeEditDialog" />
          <fdev-btn label="确认" dialog @click="confirmAddModal" />
        </template>

        <template v-slot:leftDiaBtnSlot>
          <fdev-btn label="取消" dialog outline @click="beforeClose" />
          <div>
            <fdev-btn
              :disable="selected.length === 0"
              label="确定新增"
              dialog
              @click="handleAddSelected"
            />
            <fdev-tooltip v-if="selected.length === 0"
              >请至少选择一条数据</fdev-tooltip
            >
          </div>
        </template>
      </f-dialog>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { formatOption, getGroupFullName, successNotify } from '@/utils/utils';
import { mapActions, mapState, mapGetters, mapMutations } from 'vuex';
import { reviewModel, filtersKey } from '@/modules/Release/utils/model';
import { errorNotify } from '@/utils/utils';
import {
  getPagination,
  setPagination,
  getAddPagination,
  setAddPagination
} from '@/modules/Database/utils/setting';
import {
  dataDictionaryRegColumns,
  dictionaryRegKeyColumns,
  dictRegChildColumns,
  dictRegDetailColumns
} from '@/modules/Database/utils/constants';
export default {
  name: 'DataDictionaryListRegister',
  components: { Loading },
  data() {
    return {
      tableData: [],
      tableDataList: [],
      userOptions: [],
      groupOptions: [],
      groupOptionsOriginal: [],
      detail: {},
      databaseNameOptions: [],
      canSelectTip: '请先选择系统',
      canSelect: true,
      selected: [],
      loading: false,
      filterModel: {},
      listModel:
        JSON.parse(sessionStorage.getItem('listModel')) || reviewModel(),
      filterListModel: {},
      pagination: {
        rowsPerPage: getPagination().rowsPerPage,
        rowsNumber: 10,
        page: 1
      },
      addPagination: {
        rowsPerPage: getAddPagination().rowsPerPage,
        rowsNumber: 10,
        page: 1
      },
      // refusedReason: '',
      tableOptions: [],
      columns: dataDictionaryRegColumns(),
      keyColums: dictionaryRegKeyColumns(),
      childrenColumns: dictRegChildColumns(),
      addDataModalOpened: false,
      systemNamesList: [],
      systemNamesData: [],
      modelDataMessage: {
        new_field: []
      }, // 详情页
      modelEvn: [],
      detailTableName: '',
      handleShow: false,
      editShow: false,

      taskDetailsColumns: [],
      newTaskDetailsColumns: [],
      detailsColumns: dictRegDetailColumns(),
      editModel: {
        sys_id: '',
        database_type: '',
        database_name: ''
      }
    };
  },
  watch: {
    'pagination.rowsPerPage': function(val) {
      setPagination({ rowsPerPage: val });
    },
    'addPagination.rowsPerPage': function(val) {
      setAddPagination({ rowsPerPage: val });
    },
    'dictionaryModel.sys_id': function(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.filterWithData();
      }
    },
    'dictionaryModel.database_type': function(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.filterWithData();
      }
    },
    'dictionaryModel.database_name': function(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.filterWithData();
      }
    },
    'dictionaryModel.name': function(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.filterWithData();
      }
    }
  },
  filters: {
    filtersKey(val) {
      return filtersKey[val];
    }
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', ['groups']),
    ...mapState('releaseForm', ['reviewRecord']),
    ...mapState('databaseForm', [
      'databaseType',
      'useRecordTable',
      'systemNames',
      'databaseNameById',
      'dictionaryList',
      'useRecord'
    ]),
    ...mapState('userActionSaveDatabase/DataDictionaryListRegister', {
      dictionaryModel: 'dictionaryListModel',
      visibleColumns: 'visibleColumns'
    }),
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    })
  },
  methods: {
    ...mapMutations('userActionSaveDatabase/DataDictionaryListRegister', [
      'updateSys_id',
      'updateDatabase_type',
      'updateDatabase_name',
      'updateName',
      'updatevisibleColumns'
    ]),
    ...mapActions('user', ['fetch']),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('releaseForm', ['queryReviewRecord']),
    ...mapActions('databaseForm', [
      'queryDbType',
      'querySystemNames',
      'queryUseRecordTable',
      'queryDatabaseNameById',
      'queryDictRecord',
      'queryUseRecord',

      'updateUseRecord'
    ]),
    tableFilter(val, update) {
      update(() => {
        this.tableOptions = this.useRecordTable.filter(tag => {
          return tag.toLowerCase().indexOf(val.toLowerCase()) > -1;
        });
      });
    },
    async systemNameChange($event) {
      this.updateSys_id($event);
      this.dictionaryModel.database_type = '';
      this.dictionaryModel.database_name = '';
      if (this.dictionaryModel.sys_id != null) {
        await this.queryDatabaseNameById({
          sys_id: this.dictionaryModel.sys_id.sys_id
        });
        this.canSelect = false;
        this.databaseNameOptions = this.databaseNameById;
      } else {
        this.canSelect = true;
        this.databaseNameOptions = [];
      }
    },
    filter() {},
    filterGroup(val, update, abort) {
      update(() => {
        this.groupOptions = this.groupOptionsOriginal.filter(
          group => group.newLabel.indexOf(val) > -1
        );
      });
    },
    async filterWithData() {
      const { applicant, group, key, reviewStatus } = this.listModel;
      this.filterListModel = {
        key,
        applicant: applicant ? applicant.id : null,
        group: group ? group.id : null,
        reviewStatus: reviewStatus === '全部' ? '' : reviewStatus
      };
      Object.keys(this.filterListModel).forEach(key => {
        if (!this.filterListModel[key]) delete this.filterListModel[key];
      });
      await this.init();
    },

    updateCurrentGroup(id) {
      this.groupOptionsOriginal = this.groups.map(group => {
        if (group.id === id) {
          this.listModel.group = {
            ...group,
            newLabel: getGroupFullName(this.groups, group.id)
          };
        }
        return {
          ...group,
          newLabel: getGroupFullName(this.groups, group.id)
        };
      });
    },
    systemNamesFilter(val, update) {
      update(() => {
        this.systemNamesData = this.systemNamesList.filter(tag => {
          return (
            tag.system_name_cn.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            tag.database_name.indexOf(val) > -1
          );
        });
      });
    },
    async databaseNameChange($event) {
      this.updateDatabase_name($event);
      if (this.dictionaryModel.database_name == null) {
        this.dictionaryModel.database_name = '';
        this.dictionaryModel.sys_id = this.dictionaryModel.sys_id;
      }
    },
    // 点击‘详情’按钮
    openDetail(row) {
      this.handleShow = true;
      this.modelDataMessage = row;
      if (this.modelDataMessage.all_field) {
        this.taskDetailsColumns = this.modelDataMessage.all_field;
      }
      if (this.modelDataMessage.new_field) {
        this.newTaskDetailsColumns = this.modelDataMessage.new_field;
      }
    },
    // 点击“编辑”按钮
    async editDetail(row) {
      await this.init();
      this.modelDataMessage = row;
      this.modelDataMessage.new_field = this.modelDataMessage.new_field
        ? this.modelDataMessage.new_field
        : [];
      this.modelEvn = row.all_field;
      this.detailTableName = row.table_name;
      this.editShow = true;
    },
    // changePagination(props) {
    //   let { page, rowsPerPage } = props.pagination;
    //   this.pagination.page = page;
    //   this.pagination.rowsPerPage = rowsPerPage;
    //   this.initAddData();
    // },
    async changeAddPagination(props) {
      let { page, rowsPerPage } = props.pagination;
      this.addPagination.page = page;
      this.addPagination.rowsPerPage = rowsPerPage;
      const { database_name } = this.filterModel;
      await this.queryDictRecord({
        page: this.addPagination.page,
        per_page: this.addPagination.rowsPerPage,
        ...this.filterModel,
        sys_id:
          this.filterModel.sys_id != null ? this.filterModel.sys_id.sys_id : '',
        database_name:
          typeof database_name === 'string'
            ? database_name
            : database_name.database_name
      });
      // "编辑"=>"新增"  当前页数据信息
      this.tableDataList = this.dictionaryList.dbs;
      // "编辑"=>"新增"  所有数据的条数
      this.addPagination.rowsNumber = this.dictionaryList.total;
    },
    // selection(selected) {
    //   this.selected = selected;
    //   // 如果复选框中有数据被选中
    //   if (selected.length === 0) {
    //     this.canClick = true;
    //     this.dataList = false;
    //     this.disabledTip = '未选择系统';
    //   } else {
    //     this.canClick = false;
    //     this.dataList = true;
    //   }
    // },

    async submitAddData() {
      this.filterModel = { ...this.dictionaryModel };
      this.filterModel.sys_id = this.editModel.sys_id
        ? this.editModel.sys_id
        : '';
      this.filterModel.database_name = this.editModel.database_name
        ? this.editModel.database_name
        : '';
      this.filterModel.database_type = this.editModel.database_type
        ? this.editModel.database_type
        : '';
      this.initAddData();
    },
    async initAddData() {
      this.showLoading = true;
      const { database_name } = this.filterModel;
      await this.queryDictRecord({
        page: this.addPagination.page,
        per_page: this.addPagination.rowsPerPage,
        ...this.filterModel,
        sys_id:
          this.filterModel.sys_id != null ? this.filterModel.sys_id.sys_id : '',
        database_name:
          typeof database_name === 'string'
            ? database_name
            : database_name.database_name
      });
      this.selected = [];
      // "编辑"=>"新增"  当前页数据信息
      this.tableDataList = this.dictionaryList.dbs;
      // "编辑"=>"新增"  所有数据的条数
      this.addPagination.rowsNumber = this.dictionaryList.total;
      this.showLoading = false;
    },
    // 点击‘编辑’=>‘新增’
    async handleDataAddModalOpened() {
      // 获取“所属系统”
      await this.querySystemNames({ sys_id: '' });
      // 获取“数据库”类型
      await this.queryDbType();
      // 查询数据
      await this.submitAddData();
      this.addDataModalOpened = true;
    },
    // 查询符合条件的“数据字典信息”并添加
    handleAddSelected() {
      this.addDataModalOpened = false;
      this.editModel.database_type = '';
      this.editModel.database_name = '';
      this.editModel.sys_id = '';
      let isRepeat = false;
      let repeatName = [];
      this.modelEvn.some(val => {
        this.selected.filter(data => {
          if (val.field === data['dataDict'].field_en_name) {
            isRepeat = true;
            repeatName.push(val.field);
          }
        });
      });
      if (!isRepeat) {
        let array = this.selected.map((val, index) => {
          return {
            field: val.dataDict.field_en_name,
            field_cn: val.dataDict.field_ch_name,
            field_length: val.dataDict.field_length,
            field_type: val.dataDict.field_type,
            is_list_field: this.selected[index]['is_list_field']
          };
        });
        this.modelEvn = [...this.modelEvn, ...array];
        this.modelDataMessage.all_field = [
          ...this.modelDataMessage.all_field,
          ...array
        ];
        this.modelDataMessage.new_field = [
          ...this.modelDataMessage.new_field,
          ...array
        ];
      } else {
        errorNotify('请勿重复添加' + repeatName.toString());
      }
    },
    deleteDetail(row, index) {
      this.modelEvn = this.modelEvn.filter(item => item.field != row.field);
      this.modelDataMessage.all_field = this.modelDataMessage.all_field.filter(
        item => item.field != row.field
      );
      this.modelDataMessage.new_field = this.modelDataMessage.new_field.filter(
        item => item.field != row.field
      );
    },
    // 确认添加
    async confirmAddModal() {
      let params = {
        useRecord_id: this.modelDataMessage.useRecord_id,
        sys_id: this.modelDataMessage.sys_id,
        database_type: this.modelDataMessage.database_type,
        database_name: this.modelDataMessage.database_name,
        table_name: this.modelDataMessage.table_name,
        all_field: this.modelDataMessage.all_field,
        new_field: this.modelDataMessage.new_field,
        is_new_table: this.modelDataMessage.is_new_table,
        remark: this.modelDataMessage.remark
      };
      await this.updateUseRecord(params);
      successNotify('添加成功');
      this.editShow = false;
      this.addDataModalOpened = false;
      // 添加成功后再次刷新
      this.editModel.database_type = '';
      this.editModel.database_name = '';
      this.editModel.sys_id = '';
      this.selected = [];
      await this.init();
    },
    beforeClose() {
      this.addDataModalOpened = false;
      this.selected = [];
      this.editModel.database_type = '';
      this.editModel.database_name = '';
      this.editModel.sys_id = '';
    },
    closeEditDialog() {
      this.beforeClose();
      this.editShow = false;
    },
    async init(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      this.loading = true;
      await this.queryUseRecord({
        sys_id:
          this.dictionaryModel.sys_id != null
            ? this.dictionaryModel.sys_id.sys_id
            : undefined,
        database_type: this.dictionaryModel.database_type,
        database_name: this.dictionaryModel.database_name.database_name,
        table_name: this.dictionaryModel.name,
        page: this.pagination.page,
        per_page: this.pagination.rowsPerPage
      });
      this.pagination.rowsNumber = this.useRecord.total;
      this.tableData = this.useRecord.dbs;
      this.loading = false;
    }
  },
  async created() {
    await this.querySystemNames({ sys_id: '' });
    await this.queryDbType();
    // 数据库名下拉框选项数据请求
    if (
      this.dictionaryModel.sys_id !== null &&
      this.dictionaryModel.sys_id !== ''
    ) {
      this.canSelect = false;
      await this.queryDatabaseNameById({
        sys_id: this.dictionaryModel.sys_id.sys_id
      });
      this.databaseNameOptions = this.databaseNameById;
    }
    // 查询表名
    await this.queryUseRecordTable({
      sys_id:
        this.dictionaryModel.sys_id != null
          ? this.dictionaryModel.sys_id.sys_id
          : undefined,
      database_type: this.dictionaryModel.database_type,
      database_name: this.dictionaryModel.database_name
    });
    await this.queryUseRecord({
      sys_id:
        this.dictionaryModel.sys_id != null
          ? this.dictionaryModel.sys_id.sys_id
          : undefined,
      database_type: this.dictionaryModel.database_type,
      database_name: this.dictionaryModel.database_name.database_name,
      table_name: this.dictionaryModel.name,
      page: this.pagination.page,
      per_page: this.pagination.rowsPerPage
    });
    this.systemNamesList = formatOption(this.systemNames, 'system_name_cn');
    await this.fetchGroup();
    //两种情况 1.带查询字符串--默认搜索查询字符串的内容
    // 2不带查询字符串--默认搜索查询当前用户组(原逻辑)
    const arr = this.$route.fullPath.split('=');
    await this.fetch();
    this.userOptions = this.userList;
    if (sessionStorage.getItem('listModel')) {
      const { group } = JSON.parse(sessionStorage.getItem('listModel'));
      if (group) {
        this.filterListModel.group = group.id;
        this.updateCurrentGroup(group.id);
      } else {
        this.updateCurrentGroup();
      }
      this.filterWithData();
    } else {
      if (!arr[1]) {
        this.filterListModel.group = this.currentUser.group.id;
        this.updateCurrentGroup(this.currentUser.group.id);
      } else {
        this.filterListModel.group = arr[1];
        this.updateCurrentGroup(arr[1]);
      }
      this.init();
    }
  }
};
</script>

<style lang="stylus" scoped>

.my-table-left
  width 750px
.my-table-fst
  width 450px
</style>
