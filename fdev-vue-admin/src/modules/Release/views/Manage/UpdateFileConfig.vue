<template>
  <div>
    <loading :visible="loading" class="q-pa-sm">
      <fdev-table
        class="my-sticky-column-table"
        titleIcon="list_s_f"
        title="配置依赖查询"
        selection="multiple"
        :selected.sync="selected"
        :data="queryList"
        :columns="queryColumns"
        row-key="name_en"
        :visible-columns="visibleQueryColumns"
        no-data-label="没有可用数据，请输入查询条件"
        :pagination.sync="paginationConfigQuery"
        :on-search="searchList"
      >
        <template v-slot:top-bottom>
          <f-formitem
            class="col-4 q-pr-md"
            label-style="width:60px"
            bottom-page
            label="映射值"
          >
            <fdev-input v-model="model.map" @blur="queryMapList" type="text" />
          </f-formitem>

          <f-formitem
            class="col-4 q-pr-md"
            label-style="width:60px"
            bottom-page
            label="实体"
          >
            <fdev-select
              v-model="$v.model.model.$model"
              ref="model.model"
              use-input
              :options="modelOptions"
              @filter="modelOptionsFilter"
              option-label="name_en"
              :rules="[() => $v.model.model.required || '请选择实体']"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name_en">{{
                      scope.opt.name_en
                    }}</fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.name_cn">
                      {{ scope.opt.name_cn }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>

          <f-formitem
            class="col-4 q-pr-md"
            label-style="width:60px"
            bottom-page
            label="属性字段"
          >
            <fdev-select
              v-model="$v.model.field.$model"
              ref="model.field"
              :options="fieldOptions"
              use-input
              option-label="nameEn"
              @filter="fieldOptionsFilter"
              :rules="[() => $v.model.field.required || '请选择属性']"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.nameAll">{{
                      scope.opt.nameAll
                    }}</fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.nameEn">
                      {{ scope.opt.nameEn }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
        </template>

        <template v-slot:body-cell-gitlab="props">
          <fdev-td class="text-ellipsis" :title="props.row.git || '-'">
            {{ props.row.git || '-' }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.git || '-' }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>
      </fdev-table>
      <div class="row justify-center btn-margin">
        <div>
          <fdev-tooltip
            anchor="top middle"
            self="center middle"
            :offest="[0, 0]"
            v-if="!isReleaseManager || selected.length === 0"
          >
            {{
              !isReleaseManager
                ? '仅投产管理员可添加配置变更应用'
                : '请至少选择一个应用'
            }}
          </fdev-tooltip>
          <fdev-btn
            :disable="!isReleaseManager || selected.length === 0"
            label="添加至配置变更应用列表"
            :loading="globalLoading['releaseForm/addConfigApplication']"
            @click="addToConfigApplicationList"
          />
        </div>
      </div>
      <div class="row items-center q-pa-sm q-mt-md">
        <span class="text-grey-9">已选实体属性：</span>
        <fdev-chip
          class="q-mt-md"
          color="primary"
          text-color="white"
          v-for="(item, index) in fieldSelected"
          :key="index"
        >
          {{ item }}
        </fdev-chip>
      </div>

      <fdev-table
        class="my-sticky-column-table"
        :data="addList"
        :columns="addColumns"
        row-key="id"
        title="配置变更应用列表"
        titleIcon="list_s_f"
        :visible-columns="visibleAddColumns"
        :pagination.sync="paginationConfigAdd"
      >
        <template v-slot:body-cell-config_type="props">
          <fdev-td class="text-ellipsis">
            <div>
              <fdev-tooltip
                anchor="top middle"
                self="center middle"
                :offest="[0, 0]"
              >
                {{ props.row.config_type.join('，') }}
              </fdev-tooltip>
              <span>{{ props.row.config_type.join('，') }}</span>
            </div>
          </fdev-td>
        </template>
        <template v-slot:body-cell-release_node="props">
          <fdev-td class="text-ellipsis">
            <router-link
              v-if="props.row.release_node"
              :to="{
                path: `/release/list/${props.row.release_node}`
              }"
              class="link"
            >
              <span>{{ props.row.release_node }}</span>
            </router-link>
            <span v-else>/</span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-asset_name="props">
          <fdev-td class="text-ellipsis">
            <div v-if="props.row.asset_name">
              <fdev-tooltip
                anchor="top middle"
                self="center middle"
                :offest="[0, 0]"
              >
                {{ props.row.asset_name }}
              </fdev-tooltip>
              <span>{{ props.row.asset_name }}</span>
            </div>
            <span v-else>{{ '/' }}</span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-operate="props">
          <fdev-td :auto-width="true" class="td-padding">
            <div style="display: inline-block">
              <fdev-tooltip
                v-if="props.row.ischeck === '0' || !isReleaseManager"
                anchor="top left"
                self="top left"
                :offest="[-20, 0]"
              >
                <span>{{
                  props.row.ischeck === '0' ? '已审核' : '仅投产管理员可以审核'
                }}</span>
              </fdev-tooltip>
              <fdev-btn
                label="审核"
                :disable="props.row.ischeck === '0' || !isReleaseManager"
                :loading="globalLoading['releaseForm/checkConfigApplication']"
                @click="handleCheckClick(props.row)"
              />
            </div>
            <div style="display: inline-block">
              <fdev-tooltip
                v-if="!isReleaseManager"
                anchor="top left"
                self="top left"
                :offest="[-20, 0]"
              >
                <span>仅投产管理员可以删除</span>
              </fdev-tooltip>
              <fdev-btn
                flat
                label="删除"
                :loading="globalLoading['releaseForm/deleteConfig']"
                @click="handleDeleteClick(props.row)"
              />
            </div>
          </fdev-td>
        </template>
      </fdev-table>
      <div class="row justify-center btn-margin">
        <div>
          <fdev-tooltip
            v-if="!isReleaseManager"
            anchor="top left"
            self="top left"
            :offest="[-20, 0]"
          >
            <span>仅投产管理员可以生成配置文件</span>
          </fdev-tooltip>
          <fdev-btn
            :disable="!isReleaseManager"
            label="生成配置文件"
            :loading="globalLoading['releaseForm/createConfig']"
            @click="handleCreateClick"
          />
        </div>
      </div>
      <f-dialog
        right
        v-model="errorConfigDialogOpened"
        title="配置生成失败应用列表"
      >
        <div class="table-wrapper bg-white">
          <fdev-list boardered padding class="q-mb-md text-center">
            <fdev-item
              v-ripple
              v-for="(item, index) in errorConfigList"
              :key="index"
            >
              <fdev-item-section>
                {{
                  `应用 ${item.application_name} 生成配置文件失败，失败原因：${
                    item.errorInfo
                  }`
                }}
              </fdev-item-section>
            </fdev-item>
          </fdev-list>
        </div>
      </f-dialog>
    </loading>
  </div>
</template>

<script>
import Loading from '@/components/Loading';
import {
  updateFileConfigQueryColumn,
  updateFileConfigAddColumn
} from '../../utils/model';
import { required } from 'vuelidate/lib/validators';
import { deepClone, successNotify, errorNotify } from '@/utils/utils';
import { mapState, mapActions, mapMutations } from 'vuex';

export default {
  name: 'UodateFileConfig',
  components: { Loading },
  data() {
    return {
      paginationConfigQuery: {
        rowsPerPage: 5
      },
      paginationConfigAdd: {
        rowsPerPage: 5
      },
      loading: false,
      queryColumns: updateFileConfigQueryColumn,
      addColumns: updateFileConfigAddColumn,
      selected: [],
      queryList: [],
      addList: [],
      modelOptions: [],
      fields: [],
      fieldOptions: [],
      fieldOptionsClone: [],
      fieldSelected: [],
      errorConfigDialogOpened: false
    };
  },
  validations: {
    model: {
      model: {
        required
      },
      field: {
        required
      }
    }
  },
  watch: {
    paginationConfigQuery(val) {
      this.updateQueryCurrentPage(val);
    },
    paginationConfigAdd(val) {
      this.updateAddCurrentPage(val);
    },
    'model.map'(val) {
      this.updateTermsMap(val);
    },
    'model.model': {
      handler: function(newValue, oldValue) {
        //若是被清空，则将属性下拉框置为全量，然后进行一次以映射值为依据的属性下拉框过滤
        if (!newValue) {
          this.model.field = null;
          if (!this.model.map || this.model.map.trim()) {
            this.filtFieldByMap();
          } else {
            this.fieldOptions = this.fields.slice();
            this.fieldOptionsClone = this.fieldOptions.slice();
          }
          //原来未选，现在新选时，过滤属性下拉框
        } else if (!oldValue) {
          this.filtFieldByModel();
        } else {
          //重新选择实体，若属性框已选，则将其清空，fdev-select设为单选时清空规则为null
          if (this.model.field) {
            this.model.field = null;
          }
          //过滤属性下拉框
          this.fieldOptions = this.fields.slice();
          this.fieldOptionsClone = this.fieldOptions.slice();
          this.filtFieldByModel();
        }
        this.updateTermsModel(this.model.model);
      }
    },
    'model.field': {
      handler: function(newValue) {
        if (newValue) {
          //属性框中选中值时，判断实体有没有选择，没有的话填充相应实体
          if (!this.model.model) {
            this.model.model = this.modelOptions.find(
              model => model.name_en === newValue.modelNameEn
            );
          }
        }
        this.updateTermsField(this.model.field);
      }
    }
  },
  computed: {
    ...mapState('userActionSaveRelease/updateFileConfig', [
      'visibleQueryColumns',
      'visibleAddColumns',
      'queryCurrentPage',
      'addCurrentPage',
      'terms'
    ]),
    ...mapState('user', ['currentUser']),
    ...mapState('environmentForm', [
      'configModelList',
      'modelEnvByValueList',
      'confList',
      'envKeyList'
    ]),
    ...mapState('releaseForm', ['configApplicationList', 'errorConfigList']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    isReleaseManager() {
      return this.currentUser.role.some(role => role.name === '投产管理员');
    }
  },
  methods: {
    ...mapMutations('userActionSaveRelease/updateFileConfig', [
      'updateFileConfigQueryVisibleColumn',
      'updateFileConfigAddVisibleColumn',
      'updateQueryCurrentPage',
      'updateAddCurrentPage',
      'updateTermsMap',
      'updateTermsModel',
      'updateTermsField'
    ]),
    ...mapActions('user', {
      queryCurrent: 'fetchCurrent'
    }),
    ...mapActions('environmentForm', [
      'queryConfigModel',
      'queryModelEnvByValue',
      'confDependList',
      'queryEnvKeyList'
    ]),
    ...mapActions('releaseForm', [
      'queryConfigApplication',
      'addConfigApplication',
      'checkConfigApplication',
      'createConfig',
      'deleteConfig'
    ]),
    async queryMapList() {
      if (!this.model.map || !this.model.map.trim()) {
        this.filtFieldByMap();
        return;
      }
      await this.queryModelEnvByValue({
        value: this.model.map
      });
      this.filtFieldByMap();
    },
    filtFieldByModel() {
      const { model, map } = this.model;
      //映射值没值的情况下，属性下拉框先置为全量再依据实体进行过滤
      if (!map || !map.trim()) {
        this.fieldOptions = this.fields.slice();
        this.fieldOptionsClone = this.fieldOptions.slice();
        /*映射值有值的情况下，先通过（配合实体的监听方法）映射值进行过滤，然后再对筛选过的属性，通过实体再进行过滤
         *这里映射值做了一遍重复的过滤工作
         */
      } else {
        this.filtFieldByMap();
      }
      this.fieldOptions = this.fieldOptionsClone.filter(field => {
        return (
          field.modelId === model.id &&
          model.env_key.some(env_key => env_key.name_en === field.nameEn)
        );
      });
      this.fieldOptionsClone = this.fieldOptions.slice();
    },
    filtFieldByMap() {
      const { model, map } = this.model;
      //若映射值输入为空，则根据实体值来判断
      if (!map || !map.trim()) {
        //实体值已选择的情况下，根据实体值过滤属性下拉框
        if (model) {
          this.filtFieldByModel();
          //实体值未选择的情况下，将属性下拉框置为全量
        } else {
          this.fieldOptions = this.fields.slice();
          this.fieldOptionsClone = this.fieldOptions.slice();
        }
      } else {
        //若实体值未选择，则先将属性下拉框设为全量
        if (!model) {
          this.fieldOptions = this.fields.slice();
          this.fieldOptionsClone = this.fieldOptions.slice();
        }
        //根据映射值对当前的属性下拉框过滤
        this.fieldOptions = this.fieldOptionsClone.filter(field => {
          return this.modelEnvByValueList.some(
            item =>
              item.model_id === field.modelId &&
              item.field_name_en === field.nameEn
          );
        });
        this.fieldOptionsClone = this.fieldOptions.slice();
      }
    },
    modelOptionsFilter(val, update, abort) {
      update(() => {
        this.modelOptions = this.configModelList.filter(
          model =>
            model.name_cn.indexOf(val) > -1 ||
            model.name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    fieldOptionsFilter(val, update, abort) {
      update(() => {
        this.fieldOptions = this.fieldOptionsClone.filter(
          field =>
            field.nameEn.toLowerCase().includes(val.toLowerCase()) ||
            field.modelNameEn.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    async searchList() {
      this.selected = [];
      if (this.model.model && this.model.field) {
        this.loading = true;
        await this.confDependList({
          model_name_en: this.model.model.name_en,
          field_name_en: this.model.field.nameEn,
          range: ['master']
        });
        this.queryList = deepClone(this.confList);
        this.loading = false;
      } else {
        let keys = Object.keys(this.$refs).filter(key => {
          return this.$refs[key] && key.startsWith('model');
        });
        keys.forEach(key => {
          this.$refs[key].validate();
        });
      }
    },
    addToConfigApplicationList() {
      this.$q
        .dialog({
          title: '添加确认',
          message: '确认要将选中的应用添加至配置变更应用列表吗？',
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          const applications = this.selected.map(app => ({
            release_node_name: this.release_node_name,
            application_id: app.app_id,
            application_name: app.name_en,
            config_type: this.model.field.nameAll
          }));
          await this.addConfigApplication({ applications });
          successNotify('添加成功');
          await this.queryConfigApp();
          this.selected = [];
        });
    },
    handleDeleteClick(row) {
      this.$q
        .dialog({
          title: '删除确认',
          message: '确认要删除这条应用吗？',
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.deleteConfig({
            application_id: row.application_id,
            release_node_name: this.release_node_name
          });
          successNotify('删除成功');
          //刷新列表
          await this.queryConfigApp();
        });
    },
    handleCheckClick(row) {
      this.$q
        .dialog({
          title: '审核确认',
          message: '确认要审核通过吗？',
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.checkConfigApplication({
            release_node_name: this.release_node_name,
            application_id: row.application_id
          });
          successNotify('审核成功');
          await this.queryConfigApp();
        });
    },
    handleCreateClick() {
      //存在有风险且未审核的应用不能审核
      const flag = this.addList.every(
        item => item.isrisk === '0' || item.ischeck === '0'
      );
      if (!flag) {
        errorNotify('列表中存在有风险且未审核的应用，请先审核！');
        return;
      }
      this.$q
        .dialog({
          title: '生成配置文件确认',
          message: '确认要生成配置文件吗？',
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          const appIds = this.addList.map(app => app.application_id);
          await this.createConfig({
            release_node_name: this.release_node_name,
            application_id: appIds
          });
          if (this.errorConfigList.length !== 0) {
            this.errorConfigDialogOpened = true;
          } else {
            successNotify('生成配置文件成功！');
          }
          await this.queryConfigApp();
        });
    },
    async queryConfigApp() {
      await this.queryConfigApplication({
        release_node_name: this.release_node_name
      });
      this.addList = deepClone(this.configApplicationList);

      const set = new Set();
      this.addList.forEach(data =>
        data.config_type.reduce((a, b) => set.add(b), '')
      );
      this.fieldSelected = [...set];
    }
  },
  async created() {
    this.loading = true;
    this.model = this.terms;
    this.paginationConfigQuery = this.queryCurrentPage;
    this.paginationConfigAdd = this.addCurrentPage;
    await this.queryCurrent();
    this.release_node_name = this.$route.params.id;
    await this.queryConfigModel();
    this.modelOptions = this.configModelList;

    await this.queryEnvKeyList();
    this.fields = this.envKeyList;
    this.fieldOptions = this.fields.slice();
    this.fieldOptionsClone = this.fieldOptions.slice();

    await this.queryConfigApp();
    this.loading = false;
  }
};
</script>
<style lang="stylus" scoped></style>
