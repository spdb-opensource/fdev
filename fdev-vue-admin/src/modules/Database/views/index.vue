<template>
  <f-block>
    <Loading :visible="loading['databaseForm/query'] || showLoading">
      <fdev-table
        title="应用与库表"
        title-icon="todo_list_s_f"
        class="my-sticky-column-table"
        :data="tableData"
        :columns="columns"
        :pagination.sync="pagination"
        row-key="id"
        :visible-columns="visibleColumns"
        @request="changePagination"
        selection="multiple"
        :selected.sync="selected"
        @update:selected="selection"
        :onSelectCols="updatevisibleColumns"
        :export-func="excelDownload"
      >
        <template v-slot:top-right>
          <!-- <fdev-btn
            label="导出"
            normal
            ficon="keep_file"
            @click="download(filterModel)"
            :loading="loading['databaseForm/download']"
          /> -->
          <Authorized
            :role-authority="['行内项目负责人', '卡点管理员']"
            class="q-gutter-md"
          >
            <fdev-btn
              normal
              ficon="upload"
              label="上传"
              @click="handleUploadDialogOpen"
            />
            <fdev-btn
              normal
              ficon="compile"
              label="录入"
              @click="handleDialogOpen()"
            />
          </Authorized>
          <span>
            <fdev-btn
              normal
              label="批量确认"
              @click="batchConfirm(selected)"
              :disable="disabled"
            >
            </fdev-btn>
            <fdev-tooltip v-if="disabled" position="bottom">{{
              disabledTip
            }}</fdev-tooltip>
          </span>
        </template>
        <template v-slot:top-bottom>
          <f-formitem label="应用" class="col-4 q-pr-sm" bottom-page>
            <fdev-select
              :options="appNameOptions"
              option-label="name_zh"
              option-value="id"
              map-options
              emit-value
              use-input
              :value="model.appid"
              @input="updateAppid($event)"
              @filter="appNameFilter"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name_zh">
                      {{ scope.opt.name_zh }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.name_en">
                      {{ scope.opt.name_en }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem label="数据库类型" class="col-4 q-pr-sm" bottom-page>
            <fdev-select
              :options="databaseType"
              option-label="appName_cn"
              :value="model.database_type"
              @input="updateDatabase_type($event)"
              use-input
            />
          </f-formitem>
          <f-formitem label="状态" class="col-4 q-pr-sm" bottom-page>
            <fdev-select
              :options="recordOptions"
              option-label="name"
              option-value="value"
              emit-value
              map-options
              :value="model.status"
              @input="updateStatus($event)"
            />
          </f-formitem>
          <f-formitem label="库名" class="col-4 q-pr-sm" bottom-page>
            <fdev-input
              :value="model.database_name"
              @input="updateDatabase_name($event)"
              @keyup.enter.native="submitData"
            >
              <template v-slot:append>
                <f-icon
                  name="search"
                  class="cursor-pointer"
                  @click="submitData"
                />
              </template>
            </fdev-input>
          </f-formitem>
          <f-formitem label="表名" class="col-4 q-pr-sm" bottom-page>
            <fdev-input
              :value="model.table_name"
              @input="updateTableName($event)"
              @keyup.enter.native="submitData"
            >
              <template v-slot:append>
                <f-icon
                  name="search"
                  class="cursor-pointer"
                  @click="submitData"
                />
              </template>
            </fdev-input>
          </f-formitem>
          <f-formitem label="行内应用负责人" class="col-4 q-pr-sm" bottom-page>
            <fdev-select
              use-input
              :value="model.spdb_manager"
              @input="updateSpdb_manager($event)"
              :options="users"
              option-label="user_name_cn"
              option-value="spdb_manager"
              @filter="managerFilter"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.user_name_cn">
                      {{ scope.opt.user_name_cn }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.user_name_en">
                      {{ scope.opt.user_name_en }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
        </template>
        <template v-slot:body-cell-appName_cn="props">
          <fdev-td :title="props.row.appinfo.appName_cn">
            <div class="text-ellipsis">
              <router-link
                class="link appNam"
                :to="`/app/list/${props.row.appinfo.id}`"
              >
                {{ props.row.appinfo.appName_cn }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.appinfo.appName_cn }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-appName_en="props">
          <fdev-td :title="props.row.appinfo.appName_en">
            <div class="text-ellipsis">
              <router-link
                class="link"
                :to="`/app/list/${props.row.appinfo.id}`"
              >
                {{ props.row.appinfo.appName_en }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.appinfo.appName_en }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-manager="props">
          <fdev-td class="text-ellipsis">
            <div :title="props.value[0]">
              <span
                v-for="(item, index) in props.value.slice(0, 5)"
                :key="index"
              >
                <router-link
                  v-if="item.id"
                  :to="{ path: `/user/list/${item.id}` }"
                  class="link"
                >
                  {{ item.user_name_cn }}
                </router-link>
                <span v-else class="span-margin">{{ item.user_name_cn }}</span>
              </span>
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-btn="props">
          <fdev-td>
            <div class="q-gutter-x-md">
              <!-- <fdev-btn
              label="编辑"
              color="primary"
              size="sm"
              padding="sm"
              v-if="
                isKadianManager || isAppManager(props.row.appinfo.spdb_managers)
              "
              flat
              @click="handleDialogOpen('编辑', props.row)"
            /> -->
              <fdev-btn label="详情" flat @click="batchDetails(props.row)" />
              <fdev-btn
                label="确认"
                v-if="
                  props.row.status === '0' &&
                    (isKadianManager ||
                      isAppManager(props.row.appinfo.spdb_managers))
                "
                flat
                @click="batchConfirm(props.row)"
              />
              <fdev-btn
                label="删除"
                v-if="
                  isKadianManager ||
                    isAppManager(props.row.appinfo.spdb_managers)
                "
                flat
                @click="deleted(props.row)"
              />
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
    <!-- 录入弹窗 -->
    <f-dialog :title="title" v-model="dialogOpened">
      <f-formitem label="应用" diaS required>
        <fdev-select
          :options="personAppNameOptions"
          option-label="appName_cn"
          option-value="id"
          @filter="personAppNameFilter"
          use-input
          v-model="$v.databaseModel.appid.$model"
          @input="
            () => {
              title === '编辑' ? queryDatabaseName() : null;
            }
          "
          ref="databaseModel.appid"
          map-options
          emit-value
          :rules="[() => $v.databaseModel.appid.required || '请选择应用']"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.appName_cn">
                  {{ scope.opt.appName_cn }}
                </fdev-item-label>
                <fdev-item-label caption :title="scope.opt.appName_en">
                  {{ scope.opt.appName_en }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <f-formitem diaS required label="数据库类型">
        <fdev-select
          :options="databaseType"
          option-label="appName_cn"
          ref="databaseModel.database_type"
          @input="queryDatabaseName"
          v-model="$v.databaseModel.database_type.$model"
          :rules="[
            () => $v.databaseModel.database_type.required || '请选择数据库类型'
          ]"
        />
      </f-formitem>
      <f-formitem diaS required label="数据库名">
        <fdev-select
          :options="databaseNameOptions"
          ref="databaseModel.database_name"
          @input="queryTableName"
          v-model="$v.databaseModel.database_name.$model"
          :rules="[
            () => $v.databaseModel.database_name.required || '请选择数据库名'
          ]"
        >
          <template v-slot:no-option>
            <fdev-item>
              <fdev-item-section class="text-grey" :title="databaseNameTip">
                {{ databaseNameTip }}
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <f-formitem diaS required label="表名">
        <fdev-select
          :options="tableNameOptions"
          ref="databaseModel.table_name"
          @filter="tableNameFilter"
          option-label="table_name"
          option-value="table_name"
          use-input
          map-options
          emit-value
          v-model="$v.databaseModel.table_name.$model"
          :rules="[() => $v.databaseModel.table_name.required || '请选择表名']"
        >
          <template v-slot:no-option>
            <fdev-item>
              <fdev-item-section class="text-grey">
                {{
                  !databaseModel.database_name
                    ? '数据库名尚未选择'
                    : '暂无可选项'
                }}
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>

      <template v-slot:btnSlot>
        <fdev-btn
          label="确定"
          dialog
          @click="handleDialog"
          :loading="loading[`databaseForm/${btnLoading}`]"
        />
        <fdev-btn label="取消" dialog outline @click="dialogOpened = false" />
      </template>
    </f-dialog>

    <!-- 文件上传弹窗 -->
    <f-dialog title="文件上传" v-model="uploadDialogOpened" f-sc>
      <f-formitem label="数据库类型" diaS>
        <fdev-select
          :options="databaseType"
          option-label="appName_cn"
          v-model="uploadModel.database_type"
        />
      </f-formitem>

      <f-formitem label="已选文件" diaS>
        <div class="text-right">
          <p class="text-grey-7" v-show="uploadModel.files.length === 0">
            暂未选择文件
          </p>
          <div
            v-for="file in uploadModel.files"
            :key="file.name"
            class="q-pb-sm"
          >
            <p class="file-wrapper">{{ file.name }}</p>
            <f-icon
              :width="14"
              :height="14"
              name="close"
              @click="deleteFiles(file)"
            />
          </div>
        </div>
      </f-formitem>

      <div class="notice">
        注意事项：
        <ol>
          <li>上传文件必须为schema或sql文件;</li>
          <li>文件名必须为库名;</li>
          <li>根据选择的库类型进行上传单个或多个该类型文件;</li>
        </ol>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          :label="uploadModel.files.length > 0 ? '重新选择' : '选择文件'"
          @click="openFiles"
          dialog
        />
        <fdev-btn
          dialog
          label="继续选择"
          @click="openFiles('goOn')"
          v-show="uploadModel.files.length > 0"
        />
        <span>
          <fdev-btn
            dialog
            label="确定"
            @click="uploadFiles"
            :disable="uploadModel.files.length === 0"
            :loading="loading[`databaseForm/upload`]"
          />
          <fdev-tooltip v-if="uploadModel.files.length === 0"
            >请选择文件</fdev-tooltip
          >
        </span>
      </template>
    </f-dialog>

    <!-- 详情弹窗 -->
    <f-dialog right title="详情" v-model="batchDetailsOpen">
      <div class="rdia-dc-w row justify-between q-mb-lg">
        <f-formitem label="应用名">
          <div>
            {{ appNameCn }}
          </div>
        </f-formitem>
        <f-formitem label="应用英文名">
          <div>
            {{ appNameEn }}
          </div>
        </f-formitem>
        <f-formitem label="数据库类型">
          <div>
            {{ databaseQueryDetailInfo.database_type }}
          </div>
        </f-formitem>
        <f-formitem label="库名">
          <div>
            {{ databaseQueryDetailInfo.database_name }}
          </div>
        </f-formitem>
        <f-formitem label="表名">
          <div>
            {{ databaseQueryDetailInfo.table_name }}
          </div>
        </f-formitem>
        <f-formitem label="主键">
          <div>
            {{ primaryKey }}
          </div>
        </f-formitem>
        <f-formitem label="应用负责人">
          <div class="q-gutter-x-sm">
            <span v-for="(each, index) in spdbManagers" :key="index">
              {{ each.user_name_cn }}
            </span>
          </div>
        </f-formitem>
      </div>
      <fdev-table
        :data="taskDetailsColumns"
        :columns="detailsColumns"
        noExport
        title="字段和类型"
        title-icon="todo_list_s_f"
        no-select-cols
      >
        <template v-slot:body-cell-column="props">
          <fdev-td :title="props.row.column" class="text-ellipsis">
            {{ props.row.column }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-columnType="props">
          <fdev-td :title="props.row.columnType" class="text-ellipsis">
            {{ props.row.columnType }}
          </fdev-td>
        </template>
      </fdev-table>
      <fdev-table
        :data="taskDetailsIndexs"
        :columns="detailsIndexs"
        title="普通索引"
        title-icon="todo_list_s_f"
        noExport
        no-select-cols
      >
        <template v-slot:body-cell-indexName="props">
          <fdev-td :title="props.row.indexName" class="text-ellipsis">
            {{ props.row.indexName }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-indexColumn="props">
          <fdev-td
            :title="props.row.indexColumn | arrJoin"
            class="text-ellipsis"
          >
            {{ props.row.indexColumn | arrJoin }}
          </fdev-td>
        </template>
      </fdev-table>
      <fdev-table
        :data="taskDetailsUniqueIndexs"
        :columns="detailsUniqueIndexs"
        title="唯一索引"
        title-icon="todo_list_s_f"
        noExport
        no-select-cols
      >
        <template v-slot:body-cell-indexName="props">
          <fdev-td :title="props.row.indexName" class="text-ellipsis">
            {{ props.row.indexName }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-indexColumn="props">
          <fdev-td
            :title="props.row.indexColumn | arrJoin"
            class="text-ellipsis"
          >
            {{ props.row.indexColumn | arrJoin }}
          </fdev-td>
        </template>
      </fdev-table>
    </f-dialog>
  </f-block>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import Authorized from '@/components/Authorized';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify, deepClone, errorNotify } from '@/utils/utils';
import {
  databaseListModel,
  uploadModel
} from '@/modules/Database/utils/constants';
import { getPagination, setPagination } from '@/modules/Database/utils/setting';
import {
  listColumns,
  dataDetailsColumns,
  detailsIndexsColumns,
  detailsUniqueIndexsColumns
} from '@/modules/Database/utils/constants';
export default {
  name: 'DatabaseList',
  components: {
    Loading,
    Authorized
  },
  data() {
    return {
      appNameOptions: [],
      personAppNameOptions: [],
      primaryKey: '',
      spdbManagers: [],
      appNameCnId: '',
      appNameCn: '',
      appNameEn: '',
      batchDetailsOpen: false,
      users: [],
      title: '',
      dialogOpened: false,
      uploadDialogOpened: false,
      pagination: {
        page: 1,
        rowsPerPage: getPagination().rowsPerPage,
        rowsNumber: 10
      },
      databaseModel: databaseListModel(),
      filterModel: {},
      tableData: [],
      databaseNameOptions: [],
      tableNameOptions: [],
      uploadModel: uploadModel(),
      selected: [],
      columns: listColumns(),
      taskDetailsColumns: [],
      detailsColumns: dataDetailsColumns(),
      taskDetailsIndexs: [],
      detailsIndexs: detailsIndexsColumns(),
      taskDetailsUniqueIndexs: [],
      detailsUniqueIndexs: detailsUniqueIndexsColumns(),
      disabled: true,
      disabledTip: '请至少选择一条应用',
      recordOptions: [
        { name: '全部', value: '' },
        { name: '已确认', value: '1' },
        { name: '未确认', value: '0' }
      ],
      showLoading: false
    };
  },
  validations: {
    databaseModel: {
      database_type: {
        required
      },
      database_name: {
        required
      },
      table_name: {
        required
      },
      appid: {
        required
      }
    }
  },
  computed: {
    ...mapState('userActionSaveDatabase/DatabaseList', {
      model: 'searchListModel',
      visibleColumns: 'visibleColumns'
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('global', ['loading']),
    ...mapState('databaseForm', [
      'databaseList',
      'databaseType',
      'appName',
      'databaseName',
      'tableName',
      'appManagerList',
      'databaseQueryDetailInfo',
      'personAppName'
    ]),
    databaseNameTip() {
      const { database_type, appid } = this.databaseModel;
      if (!appid) return '应用未选择!';
      if (!database_type) return '数据库类型未选择!';
      return '暂无可选项';
    },
    btnLoading() {
      if (this.title === '录入') return 'add';
      return 'update';
    },
    isManager() {
      const { role } = this.currentUser;
      return role.some(
        item => item.label === '行内项目负责人' || item.label === '卡点管理员'
      );
    },
    isKadianManager() {
      const { role } = this.currentUser;
      return role.some(item => item.label === '卡点管理员');
    }
    /* disable() {
      if (!this.selected[0]) return '未选择应用';
      return false;
    } */
  },
  filters: {
    arrJoin(val) {
      return val.join('，');
    }
  },
  watch: {
    'pagination.rowsPerPage': function(val) {
      setPagination({ rowsPerPage: val });
    },
    'model.appid': function(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.submitData();
      }
    },
    'model.database_type': function(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.submitData();
      }
    },
    'model.status': function(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.submitData();
      }
    },
    'model.database_name': function(val) {
      if (val === '') {
        this.submitData();
      }
    },
    'model.table_name': function(val) {
      if (val === '') {
        this.submitData();
      }
    },
    'model.spdb_manager': function(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.submitData();
      }
    }
  },
  methods: {
    ...mapMutations('userActionSaveDatabase/DatabaseList', [
      'updateAppid',
      'updateStatus',
      'updateSpdb_manager',
      'updateDatabase_type',
      'updateDatabase_name',
      'updateTableName',
      'updatevisibleColumns'
    ]),
    ...mapActions('databaseForm', [
      'query',
      'queryDbType',
      'queryAppName',
      'add',
      'update',
      'deleteDatabase',
      'queryDbName',
      'queryName',
      'download',
      'upload',
      'confirmAll',
      'queryManager',
      'databaseQueryDetail',
      'queryAppByUser'
    ]),
    async excelDownload() {
      await this.download(this.filterModel);
    },
    appNameFilter(val, update) {
      update(() => {
        this.appNameOptions = this.appName.filter(
          model =>
            model.name_zh.indexOf(val) > -1 || model.name_en.indexOf(val) > -1
        );
      });
    },
    managerFilter(val, update) {
      update(() => {
        this.users = this.appManagerList.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.indexOf(val) > -1
        );
      });
    },
    personAppNameFilter(val, update) {
      update(() => {
        this.personAppNameOptions = this.personAppName.filter(
          user =>
            user.appName_cn.indexOf(val) > -1 ||
            user.appName_en.indexOf(val) > -1
        );
      });
    },
    tableNameFilter(val, update) {
      update(() => {
        this.tableNameOptions = this.tableName.filter(
          user => user.table_name.indexOf(val) > -1
        );
      });
    },
    changePagination(props) {
      let { page, rowsPerPage } = props.pagination;
      this.pagination.page = page;
      this.pagination.rowsPerPage = rowsPerPage;
      this.init();
    },
    isAppManager(data) {
      const { id } = this.currentUser;
      return data.some(user => user.id === id);
    },
    submitData() {
      this.filterModel = { ...this.model };
      this.init();
    },
    async handleDialogOpen(title = '录入', data = databaseListModel()) {
      this.title = title;
      this.databaseModel = deepClone(data);
      this.databaseNameOptions = [];
      this.tableNameOptions = [];
      if (this.title === '录入') {
        await this.queryAppByUser({
          user_name_en: this.currentUser.user_name_en
        });
      }
      if (title === '编辑') {
        const { appid, database_name, database_type } = this.databaseModel;
        this.queryName({
          appid,
          database_type,
          database_name
        }).then(() => {
          this.tableNameOptions = this.tableName;
        });
        this.queryDbName({
          appid,
          database_type
        }).then(() => {
          this.databaseNameOptions = this.databaseName;
        });
      }
      this.dialogOpened = true;
    },
    async queryTableName() {
      const { appid, database_name, database_type } = this.databaseModel;
      const params = { appid, database_name, database_type };
      if (this.title !== '编辑' && this.title !== '录入') {
        //录入时传appid
        delete params.appid;
      }
      this.tableNameOptions = [];
      if (this.databaseModel.table_name) {
        this.databaseModel.table_name = '';
      }
      await this.queryName(params);
      this.tableNameOptions = this.tableName;
    },
    async queryDatabaseName() {
      this.showLoading = true;
      const { appid, database_type } = this.databaseModel;
      const params = { appid, database_type };

      if (!database_type) return;
      if (this.title === '录入') {
        delete params.appid;
      }
      if (this.databaseModel.database_name) {
        this.databaseModel.database_name = '';
      }
      if (this.databaseModel.table_name) {
        this.databaseModel.table_name = '';
      }
      this.databaseNameOptions = [];
      this.tableNameOptions = [];
      await this.queryDbName(params);
      this.databaseNameOptions = this.databaseName;
      this.showLoading = false;
    },
    validations() {
      this.$v.databaseModel.$touch();
      const keys = Object.keys(this.$refs).filter(item =>
        item.includes('databaseModel')
      );
      // validate(
      //   keys.map(key => {
      //     return this.$refs[key];
      //   })
      // );
      validate(keys.map(key => this.$refs[key]));
      if (this.$v.databaseModel.$invalid) {
        let _this = this;
        let validateRes = keys.every(item => {
          let itemArr = item.split('.');
          return _this.$v.databaseModel[itemArr[1]].$invalid == false;
        });
        if (!validateRes) {
          return;
        }
      }
      return true;
    },
    async handleDialog() {
      if (this.validations()) {
        if (this.title === '录入') {
          await this.add(this.databaseModel);
        } else {
          await this.update(this.databaseModel);
        }
        successNotify(`${this.title}成功`);
        this.init();
        this.dialogOpened = false;
      }
    },
    deleted({ id, appid }) {
      this.$q
        .dialog({
          title: '删除',
          message: '确定删除此数据吗？',
          cancel: true
        })
        .onOk(async () => {
          await this.deleteDatabase([{ id, appid }]);
          successNotify('删除成功！');
          this.init();
        });
    },
    handleUploadDialogOpen() {
      this.uploadModel = uploadModel();
      this.uploadDialogOpened = true;
    },
    openFiles(goOn) {
      const input = document.createElement('input');

      input.setAttribute('type', 'file');
      input.setAttribute('multiple', 'multiple');
      input.onchange = file => this.uploadFile(input, goOn);
      input.click();
    },
    uploadFile({ files }, goOn) {
      files = Array.from(files);
      if (goOn === 'goOn') {
        const modelFiles = [...this.uploadModel.files];

        files.forEach(file => {
          const notExist = this.uploadModel.files.every(
            item => item.name !== file.name
          );

          if (notExist) {
            modelFiles.push(file);
          }
        });

        this.uploadModel.files = modelFiles;
      } else {
        this.uploadModel.files = [...files];
      }
    },
    deleteFiles(file) {
      this.uploadModel.files = this.uploadModel.files.filter(
        item => item.name !== file.name
      );
    },
    async uploadFiles() {
      const formData = new FormData();
      this.uploadModel.files.forEach(file => {
        formData.append('file', file, file.name);
      });
      formData.append('database_type', this.uploadModel.database_type);
      await this.upload(formData);
      successNotify('上传成功！');
      this.uploadDialogOpened = false;
      this.init();
    },
    selection(selected) {
      const { id } = this.currentUser;
      selected = selected.filter(row => {
        let isConfirmed = row.status === '1';
        if (!this.isKadianManager) {
          let isMananger = row.appinfo.spdb_managers.some(
            user => user.id === id
          );
          if (isConfirmed || !isMananger) {
            errorNotify(
              '已确认的数据，不能再次选中去确认并且只能选择自己的应用'
            );
          }
          return !isConfirmed && isMananger;
        } else {
          if (isConfirmed) {
            errorNotify('已确认的数据，不能再次选中去确认');
          }
          return !isConfirmed;
        }
      });
      this.selected = selected;
      if (selected.length === 0) {
        this.disabled = true;
        this.disabledTip = '未选择应用';
      } else {
        this.disabled = false;
      }
    },
    async batchDetailsClose() {
      this.batchDetailsOpen = false;
    },
    async batchDetails(row) {
      this.batchDetailsOpen = true;
      await this.databaseQueryDetail({ id: row.id });
      if (this.databaseQueryDetailInfo) {
        this.appNameCn = this.databaseQueryDetailInfo.appinfo.appName_cn;
        this.appNameCnId = this.databaseQueryDetailInfo.appinfo.id;
        this.appNameEn = this.databaseQueryDetailInfo.appinfo.appName_en;
        this.spdbManagers = this.databaseQueryDetailInfo.appinfo.spdb_managers;
        this.primaryKey =
          this.databaseQueryDetailInfo.primaryKey.length > 0
            ? this.databaseQueryDetailInfo.primaryKey.join('，')
            : ' ';
        this.taskDetailsColumns = this.databaseQueryDetailInfo.columns;
        this.taskDetailsIndexs = this.databaseQueryDetailInfo.index;
        this.taskDetailsUniqueIndexs = this.databaseQueryDetailInfo.uniqueIndex;
      }
    },
    async batchConfirm(data) {
      let selected = this.selected,
        title = '批量确认提示',
        msg = '此操作将批量确认选中的数据，您确定吗？';
      if (!Array.isArray(data)) {
        selected = [data];
        title = '确认提示';
        msg = '此操作将确认该数据，您确定吗？';
      }
      this.$q
        .dialog({
          title: `${title}`,
          message: `${msg}`,
          ok: '确定',
          cancel: true
        })
        .onOk(async () => {
          await this.confirmAll(selected);
          this.selected = [];
          this.disabled = true;
          this.disabledTip = '未选择应用';
          successNotify('确认成功');
          this.init();
        });
    },
    async init() {
      await this.query({
        ...this.filterModel,
        page: this.pagination.page,
        per_page: this.pagination.rowsPerPage,
        spdb_manager: this.model.spdb_manager
          ? {
              user_name_cn: this.model.spdb_manager.user_name_cn,
              user_name_en: this.model.spdb_manager.user_name_en,
              id: this.model.spdb_manager.id
            }
          : ''
      });
      this.selected = [];
      this.tableData = this.databaseList.dbs;
      // table列表里应用负责人超过四个人用...表示，applicationManager为负责人提示框用到的字符串
      this.tableData.forEach(ele => {
        var applicationManager = ele.appinfo.spdb_managers
          .map(item => {
            return item.user_name_cn;
          })
          .join(',');
        ele.appinfo.spdb_managers.unshift(applicationManager);
        if (ele.appinfo.spdb_managers.length > 5) {
          ele.appinfo.spdb_managers[4].user_name_cn =
            ele.appinfo.spdb_managers[4].user_name_cn + ' ...';
        }
      });
      this.pagination.rowsNumber = this.databaseList.total;
    }
  },
  async created() {
    this.showLoading = true;
    await this.queryAppName();
    await this.queryDbType();
    await this.queryManager();

    this.users = this.appManagerList;
    this.showLoading = false;
    this.updateSpdb_manager('');
    // 如果“行内应用负责人”搜索项中有登录者的名字，则回显登陆者的名字，并展示其相对应的应用
    // if (
    //   this.model.spdb_manager === '' &&
    //   this.users.some(
    //     user => user.user_name_en === this.currentUser.user_name_en
    //   )
    // ) {
    //   this.updateSpdb_manager(this.currentUser);
    //   await this.query({
    //     ...this.filterModel,
    //     page: this.pagination.page,
    //     per_page: this.pagination.rowsPerPage,
    //     spdb_manager: {
    //       user_name_cn: this.currentUser.user_name_cn,
    //       user_name_en: this.currentUser.user_name_en,
    //       id: this.currentUser.id
    //     }
    //   });
    //   this.selected = [];
    //   this.tableData = this.databaseList.dbs;
    //   // table列表里应用负责人超过四个人用...表示，applicationManager为负责人提示框用到的字符串
    //   this.tableData.forEach(ele => {
    //     var applicationManager = ele.appinfo.spdb_managers
    //       .map(item => {
    //         return item.user_name_cn;
    //       })
    //       .join(',');
    //     ele.appinfo.spdb_managers.unshift(applicationManager);
    //     if (ele.appinfo.spdb_managers.length > 5) {
    //       ele.appinfo.spdb_managers[4].user_name_cn =
    //         ele.appinfo.spdb_managers[4].user_name_cn + ' ...';
    //     }
    //   });
    //   this.pagination.rowsNumber = this.databaseList.total;
    // }
    await this.submitData();
    this.appNameOptions = this.appName;
    this.personAppNameOptions = this.personAppName;
  },
  beforeRouteEnter(to, from, next) {
    const { params } = from;
    if (Object.keys(params).length === 0) {
      sessionStorage.removeItem('databaseModel');
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    const { params } = to;
    if (Object.keys(params).length) {
      sessionStorage.setItem('databaseModel', this.model);
    }
    next();
  }
};
</script>

<style lang="stylus" scoped>

.file-wrapper
  display inline-block
  width calc(100% - 20px)
  margin 0
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  vertical-align: bottom;

.notice
  color #757575
  ol
    margin 0
    padding-left 30px
    li
      line-height 20px
</style>
