<!-- 实体管理主页面 -->
<template>
  <f-block>
    <loading :visible="loading">
      <div class="column full-width">
        <fdev-table
          class="my-sticky-column-table"
          style="width:100%"
          title="实体列表"
          titleIcon="list_s_f"
          :data="tableData"
          :columns="entityListColumns"
          row-key="name"
          :pagination.sync="pagination"
          no-select-cols
          @request="onRequest"
          :visible-columns="visibleCols"
        >
          <template v-slot:top-right>
            <div class="q-gutter-md">
              <fdev-btn normal @click="addEntity()" v-if="isManagerRole">
                <f-icon name="add" class="icon-size-md icon-padding" />
                <span>新增实体</span>
              </fdev-btn>
              <fdev-btn dialog outline label="重置" @click="reset" />
              <fdev-btn dialog label="查询" @click="query" />
            </div>
          </template>

          <template v-slot:top-bottom>
            <f-formitem label="实体英文名" class="col-4 q-pr-sm" bottom-page>
              <fdev-input
                v-model="nameEn"
                placeholder="请输入"
                @keydown.enter="query"
              >
                <template v-slot:append>
                  <f-icon name="search" class="cursor-pointer" @click="query" />
                </template>
              </fdev-input>
            </f-formitem>
            <f-formitem label="实体中文名" class="col-4 q-pr-sm" bottom-page>
              <fdev-input
                v-model="nameCn"
                placeholder="请输入"
                @keydown.enter="query"
              >
                <template v-slot:append>
                  <f-icon name="search" class="cursor-pointer" @click="query" />
                </template>
              </fdev-input>
            </f-formitem>
            <f-formitem label="实体类型" class="col-4 q-pr-sm" bottom-page>
              <fdev-select
                use-input
                v-model="entityTemp"
                :options="entityTempOptions"
                option-label="nameCn"
                @input="query"
                @filter="filterEntityTemp"
              />
            </f-formitem>
            <f-formitem label="创建人" class="col-4 q-pr-sm" bottom-page>
              <fdev-select
                use-input
                v-model="creater"
                :options="createrOptions"
                option-label="user_name_cn"
                @input="query"
                @filter="filterUserList"
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
          <template v-slot:body-cell-nameEn="props">
            <fdev-td class="text-ellipsis">
              <router-link
                class="link"
                :to="`/configCI/modelListNew/${props.row.id}`"
                :title="props.row.nameEn"
                v-if="props.row.nameEn"
                >{{ props.row.nameEn }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.nameEn }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
              <div v-else title="-">-</div>
            </fdev-td>
          </template>
          <template v-slot:body-cell-nameCn="props">
            <fdev-td key="nameCn" class="text-ellipsis">
              <router-link
                class="link"
                :to="`/configCI/modelListNew/${props.row.id}`"
                :title="props.row.nameCn"
                v-if="props.row.nameCn"
                >{{ props.row.nameCn }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.nameCn }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
              <div v-else title="-">-</div>
            </fdev-td>
          </template>
          <template v-slot:body-cell-handle="props">
            <fdev-td key="handle" v-if="isManagerRole">
              <div class="q-gutter-x-sm row no-wrap">
                <fdev-btn label="编辑" flat @click="editEntity(props.row)" />
                <fdev-btn label="复制" flat @click="copyEntity(props.row)" />
                <fdev-btn label="删除" flat @click="delEntity(props.row)" />
              </div>
            </fdev-td>
          </template>
        </fdev-table>
      </div>

      <f-dialog
        v-model="riskDialogOpen"
        right
        f-sc
        title="风险确认"
        @before-close="beforeClose"
      >
        <div class="left-container">
          <div class="row items-center text-orange-7 q-mb-md">
            <f-icon
              name="alert_t_f"
              class="icon-size-md"
              style="margin-right:8px;padding:5px;color:#EF6C00"
            />
            对该实体的改动可能会引起以下应用的流水线或配置文件不可用，请确认风险！
          </div>
          <div class="content">
            <!-- 部署依赖列表 -->
            <div v-if="deployDependency.count > 0">
              <fdev-table
                title="部署依赖"
                style="width:100%"
                row-key="pipelineId"
                no-select-cols
                :data="deployTableData"
                :columns="deployColumns"
                :pagination.sync="paginationD"
                @request="onRequestD"
              >
                <!-- 列：应用中文名 -->
                <template v-slot:body-cell-name_zh="props">
                  <fdev-td style="max-width:180px">
                    <div
                      class="link text-ellipsis"
                      :title="props.row.name_zh"
                      @click="gotoAppDetail(props.row)"
                      v-if="props.row.name_zh"
                    >
                      {{ props.row.name_zh }}
                      <fdev-popup-proxy context-menu>
                        <fdev-banner style="max-width:300px">
                          {{ props.row.name_zh }}
                        </fdev-banner>
                      </fdev-popup-proxy>
                    </div>
                    <div v-else title="-">-</div>
                  </fdev-td>
                </template>

                <!-- 列：应用英文名 -->
                <template v-slot:body-cell-name_en="props">
                  <fdev-td class="text-ellipsis">
                    <div
                      class="link"
                      :title="props.row.name_en"
                      @click="gotoAppDetail(props.row)"
                      v-if="props.row.name_en"
                    >
                      {{ props.row.name_en }}
                      <fdev-popup-proxy context-menu>
                        <fdev-banner style="max-width:300px">
                          {{ props.row.name_en }}
                        </fdev-banner>
                      </fdev-popup-proxy>
                    </div>
                    <div v-else title="-">-</div>
                  </fdev-td>
                </template>

                <!-- 列：行内应用负责人 -->
                <template v-slot:body-cell-spdbManagers="props">
                  <fdev-td style="white-space:nowrap" class="text-ellipsis">
                    <div :title="props.row.spdbManagers | spdbManagers">
                      {{ props.row.spdbManagers | spdbManagers }}
                      <fdev-popup-proxy context-menu>
                        <fdev-banner style="max-width:300px">
                          {{ props.row.spdbManagers | spdbManagers }}
                        </fdev-banner>
                      </fdev-popup-proxy>
                    </div>
                  </fdev-td>
                </template>

                <!-- 列：厂商应用负责人 -->
                <template v-slot:body-cell-devManagers="props">
                  <fdev-td class="text-ellipsis">
                    <div :title="props.row.devManagers | devManagers">
                      {{ props.row.devManagers | devManagers }}
                      <fdev-popup-proxy context-menu>
                        <fdev-banner style="max-width:300px">
                          {{ props.row.devManagers | devManagers }}
                        </fdev-banner>
                      </fdev-popup-proxy>
                    </div>
                  </fdev-td>
                </template>

                <!-- 列：流水线名称 -->
                <template v-slot:body-cell-pipelineName="props">
                  <fdev-td class="td-desc">
                    <router-link
                      :to="{
                        path: `/modelListNew/pipelineDetail/${
                          props.row.pipelineId
                        }`
                      }"
                      class="link"
                      target="_blank"
                      :title="props.row.pipelineName"
                      v-if="props.row.pipelineName"
                      >{{ props.row.pipelineName }}
                      <fdev-popup-proxy context-menu>
                        <fdev-banner style="max-width:300px">
                          {{ props.row.pipelineName }}
                        </fdev-banner>
                      </fdev-popup-proxy>
                    </router-link>
                    <div v-else title="-">-</div>
                  </fdev-td>
                </template>
              </fdev-table>
            </div>

            <!-- 配置文件依赖列表， 暂时不展示 -->
            <!-- <div v-if="configDependency.count > 0" style="margin-top:32px">
              <fdev-table
                title="配置文件依赖"
                style="width:100%"
                row-key="id"
                no-select-cols
                :data="configFilesdeployTableData"
                :columns="configFilesdeployColumns"
                :pagination.sync="paginationC"
                @request="onRequestC"
              >
                <template v-slot:body-cell-nameCN="props">
                  <fdev-td style="max-width:180px">
                    <div
                      class="link text-ellipsis"
                      :title="props.row.nameCN"
                      @click="gotoAppDetail(props.row)"
                      v-if="props.row.nameCN"
                    >
                      {{ props.row.nameCN }}
                    </div>
                    <div v-else title="-">-</div>
                  </fdev-td>
                </template>

                <template v-slot:body-cell-nameEN="props">
                  <fdev-td class="text-ellipsis">
                    <div
                      class="link"
                      :title="props.row.nameEN"
                      @click="gotoAppDetail(props.row)"
                      v-if="props.row.nameEN"
                    >
                      {{ props.row.nameEN }}
                    </div>
                    <div v-else title="-">-</div>
                  </fdev-td>
                </template>

                <template v-slot:body-cell-spdbManagers="props">
                  <fdev-td class="text-ellipsis">
                    <div :title="props.row.spdbManagers | spdbManagers">
                      {{ props.row.spdbManagers | spdbManagers }}
                    </div>
                  </fdev-td>
                </template>

                <template v-slot:body-cell-devManagers="props">
                  <fdev-td class="td-desc">
                    <div :title="props.row.devManagers | devManagers">
                      {{ props.row.devManagers | devManagers }}
                    </div>
                  </fdev-td>
                </template>

                <template v-slot:body-cell-gitAddress="props">
                  <fdev-td class="text-ellipsis">
                    <a
                      :href="props.row.gitAddress"
                      target="_blank"
                      v-if="props.row.gitAddress"
                    >
                      <span :title="props.row.gitAddress">{{
                        props.row.gitAddress
                      }}</span>
                    </a>
                    <div v-else title="-">-</div>
                  </fdev-td>
                </template>
              </fdev-table>
            </div> -->
          </div>
        </div>

        <template v-slot:btnSlot>
          <fdev-btn label="确定" dialog @click="closeDialog" />
        </template>
      </f-dialog>

      <entity-add
        v-model="entityAddDialogOpen"
        @refresh="refresh"
        @showMapConfig="showMapConfig"
        :type="entityType"
        :entity-info="entityInfo"
      />
    </loading>
  </f-block>
</template>

<script>
import { SAVE_USER_CFG_MIXIN } from '@/modules/configCI/utils/mixin';
import Loading from '@/components/Loading';
import { successNotify } from '@/utils/utils';
import {
  entityColumns,
  deployColumns
  // configFilesdeployColumns
} from '../../utils/constants';
import { mapActions, mapState, mapMutations } from 'vuex';
import entityAdd from './entityAdd';

export default {
  name: 'entityManagement',
  mixins: [SAVE_USER_CFG_MIXIN],
  components: { entityAdd, Loading },
  data() {
    return {
      isManagerRole: false,
      saveUserCfg_: ['creater', 'nameEn', 'nameCn', 'entityTemp', 'pagination'],
      loading: true,
      creater: null, // 实体创建人
      createrList: [], // 实体创建人（用户）列表
      createrOptions: [], // 实体创建人下拉选项
      nameEn: '', // 实体英文名
      nameCn: '', // 实体中文名
      entityTemp: null, // 实体类型
      entityTempOptions: [], // 实体类型下拉选项
      entityTempList: [], // 实体类型全量列表
      tableData: [], // 实体列表数据
      entityListColumns: entityColumns().columns, // 表头
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      },
      entityId: '', // 点击“映射值”,传给子组件的实体id
      entityInfo: {},
      entityType: 'add', // 默认实体类型为新增
      mapConfigDialogOpen: false, // 映射值弹窗是否显示
      entityAddDialogOpen: false, // 新增实体弹窗是否显示
      deployColumns,
      // configFilesdeployColumns,
      // paginationC: {
      //   page: 1,
      //   rowsPerPage: 5,
      //   rowsNumber: 0
      // },
      paginationD: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      },
      riskDialogOpen: false
    };
  },
  computed: {
    ...mapState('entityManageConfigForm', [
      'entityList',
      'userList',
      'templateList',
      // 'configDependency',
      'deployDependency',
      'currentUser'
    ]),
    ...mapState('user', {
      user: 'currentUser'
    }),
    ...mapState('userActionSaveConfig/modelList', ['visibleCols']),
    // 查询实体列表的参数
    params() {
      return {
        createUserId: this.creater ? this.creater.id : '', // 实体创建人id
        nameEn: this.nameEn, // 实体英文名
        nameCn: this.nameCn, // 实体中文名
        templateId: this.entityTemp ? this.entityTemp.id : '' // 实体模板id
      };
    },
    // 选择列下拉选项
    columnOptions() {
      const columns = this.entityListColumns.slice();
      return columns.splice(1, columns.length - 2);
    },
    paramsC() {
      return {
        entityNameEn: this.entityInfo.nameEn
      };
    },
    paramsD() {
      return {
        entityNameEn: this.entityInfo.nameEn
      };
    },
    // 配置文件依赖列表数据
    // configFilesdeployTableData() {
    //   return this.configDependency.serviceList
    //     ? this.configDependency.serviceList.slice()
    //     : [];
    // },
    // 部署依赖列表数据
    deployTableData() {
      return this.deployDependency.serviceList
        ? this.deployDependency.serviceList.slice()
        : [];
    }
  },
  watch: {
    isManagerRole: {
      immediate: true,
      handler(val) {
        if (val) {
          this.entityListColumns.push({
            name: 'handle',
            label: '操作',
            field: 'handle'
          });
        }
      }
    }
  },
  filters: {
    spdbManagers(val) {
      return val.length === 0 ? '-' : val.map(item => item.nameCn).join(',');
    },
    devManagers(val) {
      return val.length === 0 ? '-' : val.map(item => item.nameCn).join(',');
    }
  },
  //  fuser  接口
  // queryUserCoreData
  methods: {
    ...mapActions('entityManageConfigForm', [
      'queryEntityModel',
      'queryUserCoreData',
      'queryTemplate',
      'deleteEntity',
      // 'queryConfigDependency',
      'queryDeployDependency'
    ]),
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent'
    }),
    ...mapMutations('userActionSaveConfig/modelList', ['saveVisibleColumns']),
    // visibaleOptions: {
    //   set(visibaleOptions) {
    //     return visibaleOptions;
    //   },
    //   get() {
    //     const arr = this.columns.slice(0);
    //     if (this.isManagerRole) {
    //       return arr.splice(0, arr.length - 1);
    //     } else {
    //       return arr;
    //     }
    //   }
    // },
    // 获取实体列表
    async getList() {
      let params = {
        page: this.pagination.page,
        perPage: this.pagination.rowsPerPage
      };
      params = Object.assign(params, this.params);
      this.loading = true;
      await this.queryEntityModel(params);
      this.pagination.rowsNumber = this.entityList.count;
      this.tableData = this.entityList.entityModelList;
      this.loading = false;
    },
    // 点击“重置”按钮, 重置查询条件,并查询列表
    reset() {
      this.creater = null;
      this.nameEn = '';
      this.nameCn = '';
      this.entityTemp = null;
      this.pagination = {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      };
      this.getList();
    },
    // 点击“查询”，查询实体列表
    query() {
      this.pagination.page = 1;
      this.getList();
    },
    // 实体列表换页时查询
    onRequest(props) {
      const { page, rowsPerPage } = props.pagination;
      this.pagination.page = page;
      this.pagination.rowsPerPage = rowsPerPage;
      this.getList();
    },
    // 点击“映射值”按钮，弹出映射值弹窗
    showMapConfig(row) {
      this.entityId = row.id;
      this.mapConfigDialogOpen = true;
    },
    // 模糊匹配用户，赋值用户下拉选项
    filterUserList(val, update, abort) {
      update(() => {
        this.createrOptions = this.createrList.filter(
          v =>
            v.user_name_cn.includes(val) ||
            v.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    // 初始化实体类型列表，实体类型下拉选项赋初始值
    initEntityTempList() {
      if (this.templateList.length === 0) {
        this.entityTempOptions = [];
        this.entityList = [];
      } else {
        // let arr = this.templateList.slice();
        // arr.push({
        //   id: 'empty',
        //   nameCn: '自定义实体',
        //   nameEn: 'empty',
        //   properties: [
        //     {
        //       nameEn: '',
        //       nameCn: '',
        //       type: 'string',
        //       required: true,
        //       empty: true
        //     }
        //   ]
        // });
        // this.entityTempOptions = arr.slice();
        // this.entityTempList = arr.slice();
        this.entityTempOptions = this.templateList.slice();
        this.entityTempList = this.templateList.slice();
      }
    },
    // 模糊匹配实体类型，赋值实体类型下拉选项
    filterEntityTemp(val, update, abort) {
      update(() => {
        this.entityTempOptions = this.entityTempList.filter(
          v =>
            v.nameCn.includes(val) ||
            v.nameCn.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    // 重新请求实体列表
    refresh() {
      this.getList();
    },
    // 分页查询配置文件依赖
    // getConfigFilesDeployList() {
    //   const params = {
    //     page: this.paginationC.page,
    //     perPage: this.paginationC.rowsPerPage,
    //     ...this.paramsC
    //   };
    //   return this.queryConfigDependency(params).then(() => {
    //     this.paginationC.rowsNumber = this.configDependency.count;
    //   });
    // },
    // 分页查询部署依赖
    getDeployDependencyList() {
      const params = {
        page: this.paginationD.page,
        perPage: this.paginationD.rowsPerPage,
        ...this.paramsD
      };
      return this.queryDeployDependency(params).then(() => {
        this.paginationD.rowsNumber = this.deployDependency.count;
      });
    },
    // 删除实体
    delEntity(row) {
      //
      this.entityInfo = row;
      Promise.all([
        this.getDeployDependencyList()
        // this.getConfigFilesDeployList()
      ]).then(() => {
        if (this.deployDependency.count === 0) {
          this.$q
            .dialog({
              title: '温馨提示',
              message: '您所选择的实体没有应用在使用，可以删除，确认要删除吗？',
              cancel: true
            })
            .onOk(async () => {
              await this.deleteEntity({ id: this.entityInfo.id });
              this.entityInfo = {};
              this.refresh();
              successNotify('删除实体成功！');
            });
          return;
        } else {
          this.riskDialogOpen = true;
        }
      });
    },
    // 新增实体
    addEntity() {
      this.entityType = 'add';
      this.entityAddDialogOpen = true;
    },
    // 复制实体
    copyEntity(rows) {
      this.entityInfo = rows;
      this.entityType = 'copy';
      this.entityAddDialogOpen = true;
    },
    // 编辑实体
    editEntity(rows) {
      this.entityInfo = rows;
      this.entityType = 'edit';
      this.entityAddDialogOpen = true;
    },
    // 分页查询配置文件依赖
    // onRequestC(props) {
    //   const { page, rowsPerPage } = props.pagination;
    //   this.paginationC.page = page;
    //   this.paginationC.rowsPerPage = rowsPerPage;
    //   this.getConfigFilesDeployList();
    // },
    // 分页查询部署依赖
    onRequestD(props) {
      const { page, rowsPerPage } = props.pagination;
      this.paginationD.page = page;
      this.paginationD.rowsPerPage = rowsPerPage;
      this.getDeployDependencyList();
    },
    beforeClose() {
      // this.paginationC = {
      //   page: 1,
      //   rowsPerPage: 5,
      //   rowsNumber: 0
      // };
      this.paginationD = {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      };
    },
    closeDialog() {
      this.beforeClose();
      this.riskDialogOpen = false;
    },
    // 打开新页面查看应用详情
    async gotoAppDetail(row) {
      const url = this.$router.resolve({
        path: `/app/list/${row.id}`
      });
      window.open(url.href, '_blank');
    }
  },
  async created() {
    this.queryUserCoreData().then(() => {
      this.createrList = this.userList.filter(item =>
        item.hasOwnProperty('user_name_cn')
      );
      this.createrOptions = this.userList.filter(item =>
        item.hasOwnProperty('user_name_cn')
      );
    });
    await this.queryTemplate({ page: 0, pageSize: 0 });
    this.initEntityTempList();
    this.getList();

    await this.fetchCurrent();
    // 环境配置管理员才有权限增删改
    let managerUser = this.user.role.find(item => {
      return item.label === '环境配置管理员';
    });
    if (managerUser) {
      this.isManagerRole = true;
    }
    this.loading = false;
  },
  mounted() {
    if (!this.visibleCols.toString() || this.visibleCols.length <= 2) {
      this.saveVisibleColumns([
        'nameEn',
        'nameCn',
        'templateName',
        'createUserName',
        'createTime',
        'updateUserName',
        'updateTime',
        'handle'
      ]);
    }
  }
};
</script>
<style lang="stylus" scoped>
@import '../../styles/common.styl';

.select-option-width
  max-width 164px
  overflow hidden
  text-overflow ellipsis
.left-container
  width 580px
.td-desc
  max-width 180px
  overflow hidden
  text-overflow ellipsis
.btnDrop
  >>> i
    margin-left 0
</style>
