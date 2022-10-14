<template>
  <f-block>
    <Loading :visible="loading" class="bg-white">
      <div>
        <fdev-table
          noExport
          title="实体与环境映射列表"
          titleIcon="list_s_f"
          :data="list"
          :columns="columns"
          row-key="id"
          class="my-sticky-column-table"
          :pagination.sync="pagination"
          @request="query"
          :on-search="query"
          :visible-columns="visibleCols"
        >
          <template v-slot:top-bottom>
            <f-formitem
              class="col-4"
              bottom-page
              label-style="padding-left:80px"
              label="实体"
            >
              <fdev-select
                use-input
                clearable
                @input="saveSelectedModel($event)"
                v-model="terms.selectedModel"
                :options="modelOptions"
                option-label="name_en"
                @filter="modelFilterChoice"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.name_en">{{
                        scope.opt.name_en
                      }}</fdev-item-label>
                      <fdev-item-label :title="scope.opt.name_cn" caption>
                        {{ scope.opt.name_cn }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
            <f-formitem
              class="col-4"
              bottom-page
              label-style="padding-left:80px"
              label="环境"
            >
              <fdev-select
                use-input
                clearable
                @input="saveSelectedEnv($event)"
                v-model="terms.selectedEnv"
                :options="envOptions"
                option-label="name_en"
                @filter="envFilterChoice"
                :disable="terms.labels && terms.labels.length > 0"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.name_en">{{
                        scope.opt.name_en
                      }}</fdev-item-label>
                      <fdev-item-label :title="scope.opt.name_cn" caption>
                        {{ scope.opt.name_cn }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
            <f-formitem
              class="col-4"
              bottom-page
              label-style="padding-left:60px"
              label="环境标签"
            >
              <fdev-select
                use-input
                multiple
                :value="terms.labels"
                @input="saveLabels($event)"
                :disable="!!terms.selectedEnv"
                :options="labelOptions"
                @filter="labelFilterChoice"
              />
            </f-formitem>
          </template>

          <template v-slot:top-right>
            <fdev-btn
              ficon="add"
              label="新增实体与环境映射"
              normal
              @click="goToManagePage('add', '')"
            />
          </template>

          <template v-slot:body-cell-model="props">
            <fdev-td class="text-ellipsis">
              <router-link
                class="link"
                :to="`/envModel/modelList/${props.row.model_id}`"
                :title="props.row.model"
                v-if="props.row.model"
                >{{ props.row.model }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.model }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
              <div v-else title="-">-</div>
            </fdev-td>
          </template>
          <template v-slot:body-cell-model_name_en="props">
            <fdev-td class="text-ellipsis">
              <router-link
                class="link"
                :to="`/envModel/modelList/${props.row.model_id}`"
                :title="props.row.model_name_en"
                v-if="props.row.model_name_en"
                >{{ props.row.model_name_en }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.model_name_en }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
              <div v-else title="-">-</div>
            </fdev-td>
          </template>
          <template v-slot:body-cell-labels="props">
            <fdev-td
              class="text-ellipsis"
              :title="props.value.join('，') || '-'"
            >
              {{ props.value.join('，') || '-' }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-desc="props">
            <fdev-td class="text-ellipsis" :title="props.value || '-'">
              {{ props.value || '-' }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-btn="props">
            <fdev-td auto-width>
              <div class="q-gutter-x-sm">
                <fdev-btn label="详情" flat @click="openDetail(props.row)" />
                <fdev-btn
                  label="复制"
                  flat
                  @click="goToManagePage('copy', props.row.id)"
                  v-if="isManagerRole"
                />
                <fdev-btn
                  v-if="isManagerRole || isManager"
                  label="修改"
                  flat
                  @click="goToManagePage('update', props.row.id)"
                />
                <fdev-btn
                  label="历史信息"
                  flat
                  @click="goToHistoryMsg(props.row.env_id, props.row.model_id)"
                />
                <fdev-btn
                  v-if="isManagerRole"
                  label="删除"
                  flat
                  @click="handleDeleteDialogOpened(props.row)"
                />
              </div>
            </fdev-td>
          </template>
        </fdev-table>
        <!-- 详情页开始 -->
        <f-dialog v-model="handleShow" right f-sc title="实体与环境映射详情">
          <div style="width:1000px">
            <fdev-markup-table flat>
              <tbody>
                <fdev-tr class="hover-tr">
                  <fdev-td>环境中文名：{{ modelEvnMessage.env }}</fdev-td>
                  <fdev-td
                    >环境英文名：{{ modelEvnMessage.env_name_en }}</fdev-td
                  >
                  <fdev-td class="desc-td">
                    环境描述信息：{{ modelEvnMessage.env_desc }}
                  </fdev-td>
                </fdev-tr>
                <fdev-tr class="hover-tr">
                  <fdev-td>实体中文名：{{ modelEvnMessage.model }}</fdev-td>
                  <fdev-td
                    >实体英文名：{{ modelEvnMessage.model_name_en }}</fdev-td
                  >
                  <fdev-td class="desc-td">
                    实体描述信息：{{ modelEvnMessage.model_desc }}
                  </fdev-td>
                </fdev-tr>
                <fdev-tr class="hover-tr">
                  <fdev-td>实体作用域：{{ modelEvnMessage.scope }}</fdev-td>
                  <fdev-td
                    >实体版本：{{ modelEvnMessage.model_version }}</fdev-td
                  >
                </fdev-tr>
              </tbody>
            </fdev-markup-table>
            <fdev-table
              :data="modelEvn"
              :columns="keyColums"
              hide-bottom
              noExport
              :pagination.sync="childPagination"
            >
              <template v-slot:body="props">
                <fdev-tr :props="props">
                  <fdev-td
                    :props="props"
                    v-for="col in keyColums"
                    :key="col.name"
                    :class="[
                      col.name === 'value' || col.name === 'desc' ? 'desc' : '',
                      'q-td text-left'
                    ]"
                  >
                    <span
                      v-if="col.name === 'require' || col.name === 'type'"
                      :title="props.row[col.name] === '1' ? '是' : '否'"
                    >
                      {{ props.row[col.name] === '1' ? '是' : '否' }}
                    </span>
                    <span
                      v-else-if="col.name === 'data_type'"
                      :title="props.row[col.name] | filterType"
                    >
                      {{ props.row[col.name] | filterType }}
                    </span>
                    <span
                      v-else-if="
                        col.name === 'value' &&
                          props.row.data_type &&
                          props.row.data_type !== ''
                      "
                    ></span>
                    <span
                      v-else-if="
                        col.name === 'value' &&
                          (!props.row.data_type || props.row.data_type === '')
                      "
                      :title="props.row[col.name] || '-'"
                      class="text-ellipsis"
                    >
                      {{ props.row[col.name] || '-' }}
                    </span>
                    <span
                      v-else
                      class="text-ellipsis"
                      :title="props.row[col.name] || '-'"
                      >{{ props.row[col.name] || '-' }}</span
                    >
                    <fdev-btn
                      round
                      v-if="col.name === 'name_en'"
                      flat
                      v-show="props.row.data_type && props.row.data_type !== ''"
                      :icon="taggleIcon(props)"
                      @click="props.expand = !props.expand"
                    />
                  </fdev-td>
                </fdev-tr>
                <fdev-tr
                  v-if="props.row.data_type && props.row.data_type !== ''"
                  :props="props"
                  v-show="showTable(props)"
                >
                  <fdev-td colspan="100%" v-if="Array.isArray(props.row.value)">
                    <div
                      class="text-left"
                      v-for="(key, index) in props.row.value"
                      :key="index"
                    >
                      <!-- <fdev-btn
                        dense
                        round
                        flat
                        :icon="key.expand ? 'arrow_drop_down' : 'arrow_drop_up' "
                        @click="toggleBtn(props.row.id, index)"
                      /> -->
                      <fdev-table
                        :data="key.greatKey"
                        :columns="grandChildColumns"
                        row-key="id"
                        flat
                        noExport
                        hide-bottom
                        :pagination.sync="grandChildPagination"
                      >
                        <template v-slot:body-cell-desc="propsChild">
                          <fdev-td
                            auto-width
                            class="td_desc"
                            :title="propsChild.value || '-'"
                          >
                            {{ propsChild.value || '-' }}
                          </fdev-td>
                        </template>
                        <template v-slot:body-cell-value="propsChild">
                          <fdev-td
                            auto-width
                            class="td_desc"
                            :title="propsChild.value || '-'"
                          >
                            {{ propsChild.value || '-' }}
                          </fdev-td>
                        </template>
                      </fdev-table>
                    </div>
                  </fdev-td>
                  <fdev-td colspan="100%" v-else>
                    <div class="text-left">
                      <fdev-table
                        :data="props.row.greatKey"
                        :columns="grandChildColumns"
                        row-key="id"
                        flat
                        noExport
                        hide-bottom
                        :pagination.sync="grandChildPagination"
                      >
                        <template v-slot:body-cell-desc="propsChild">
                          <fdev-td
                            auto-width
                            class="td_desc"
                            :title="propsChild.value || '-'"
                          >
                            {{ propsChild.value || '-' }}
                          </fdev-td>
                        </template>
                        <template v-slot:body-cell-value="propsChild">
                          <fdev-td
                            auto-width
                            class="td_desc"
                            :title="propsChild.value || '-'"
                          >
                            {{ propsChild.value || '-' }}
                          </fdev-td>
                        </template>
                      </fdev-table>
                    </div>
                  </fdev-td>
                </fdev-tr>
              </template>
            </fdev-table>
          </div>
          <template v-slot:btnSlot>
            <fdev-btn dialog label="确定" @click="handleShow = false" />
          </template>
        </f-dialog>
        <!-- 弹出 输入验证码删除对话框 -->
        <VerifycodeDialog
          v-model="deleteDialogOpened"
          @handleDelete="handleDelete"
          :description="description"
          :loading="globalLoading['environmentForm/deleteModelEvn']"
        />
      </div>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState, mapGetters, mapMutations } from 'vuex';
import { successNotify, getIdsFormList } from '@/utils/utils';
import { modelEnvColumns } from '../../utils/constants';
import VerifycodeDialog from '../../components/VerifycodeDialog';

export default {
  name: 'ModelEnv',
  components: {
    VerifycodeDialog,
    Loading
  },
  data() {
    return {
      deleteDialogOpened: false,
      ...modelEnvColumns(),

      pagination: {},
      childPagination: {
        rowsPerPage: 0
      },
      grandChildPagination: {
        rowsPerPage: 0
      },
      modelOptions: [],
      envOptions: [],
      labelOptions: [],
      loading: false,
      isManagerRole: false,
      isManager: false,
      modelEvnMessage: {}, // 详情页
      handleShow: false,
      modelEvn: [],
      list: [],
      deleteEnvName: '',
      deleteModelName: ''
    };
  },
  computed: {
    ...mapState('environmentForm', {
      modelList: 'modelList', //实体列表
      modelEnvMap: 'modelEnvMap', // 实体环境映射列表
      envList: 'envList' //环境列表
    }),
    ...mapGetters('environmentForm', {
      envLablesOption: 'envLablesOption' // 对labels处理后的list
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('appForm', ['appData']), //看是否是私有应用时需要
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('userActionSaveEnv/modelEnv', [
      'visibleCols',
      'currentPage',
      'terms'
    ]),
    description() {
      return `确认删除${this.deleteModelName}实体下的${
        this.deleteEnvName
      }环境吗?删除后不可恢复`;
    },
    visibleOptions() {
      const arr = this.columns.slice(0);
      return arr.splice(0, arr.length - 1);
    }
  },
  watch: {
    pagination: {
      handler(val) {
        this.saveCurrentPage(val);
      },
      deep: true
    },
    'terms.selectedEnv'(newVal, oldVal) {
      if (newVal && oldVal) {
        if (newVal.id !== oldVal.id) {
          this.query();
        }
      } else {
        if (newVal !== oldVal) {
          this.query();
        }
      }
      // this.saveSelectedEnv(val);
      // this.query();
    },
    'terms.selectedModel': {
      deep: true,
      handler: async function(val, oldVal) {
        if (val && oldVal) {
          if (val.id !== oldVal.id) {
            this.query();
            this.managerAuth(val);
          }
        } else {
          if (val !== oldVal) {
            this.query();
            this.managerAuth(val);
          }
        }
        // this.saveSelectedModel(val);
        // this.query();
      }
    },
    'terms.labels'(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.query();
      }
    }
  },
  filters: {
    filterType(val) {
      return val === '' || !val ? 'string' : val;
    }
  },
  methods: {
    ...mapActions('environmentForm', {
      getModelList: 'getModelList', // 获取实体列表
      pageQuery: 'pageQuery',
      queryAllLabels: 'queryAllLabels',
      deleteModelEvn: 'deleteModelEvn', //删除实体映射
      getEnvList: 'getEnvList'
    }),
    ...mapActions('appForm', {
      queryAppDetail: 'queryApplication'
    }),
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent'
    }),
    ...mapMutations('userActionSaveEnv/modelEnv', [
      'saveVisibleColumns',
      'saveCurrentPage',
      'saveSelectedModel',
      'saveSelectedEnv',
      'saveLabels'
    ]),
    modelFilterChoice(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.modelOptions = this.modelList.filter(v => {
          return (
            v.name_en.toLowerCase().indexOf(needle) > -1 ||
            v.name_cn.indexOf(val) > -1
          );
        });
      });
    },
    envFilterChoice(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.envOptions = this.envList.filter(v => {
          return (
            v.name_en.toLowerCase().indexOf(needle) > -1 ||
            v.name_cn.indexOf(val) > -1
          );
        });
      });
    },
    labelFilterChoice(val, update, abort) {
      update(() => {
        this.labelOptions = this.envLablesOption.filter(label => {
          return label.value.toLowerCase().indexOf(val.toLowerCase()) > -1;
        });
      });
    },
    async managerAuth(val) {
      // 当前登录用户不是环境配置管理员，但是他的应用所绑定的私有环境也能做修改操作
      this.isManager = false;
      if (val && val.name_en.includes('private')) {
        let appEnName = val.name_en.split('_')[2];
        await this.queryAppDetail({ name_en: appEnName });
        let appDetail = this.appData[0];
        if (appDetail) {
          let userIds = getIdsFormList(
            appDetail.dev_managers.concat(appDetail.spdb_managers)
          );
          this.isManager = userIds.indexOf(this.currentUser.id) > -1;
        }
      }
    },
    async query(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      this.loading = true;
      let labels = this.terms.labels
        ? this.terms.labels.map(label => label.value)
        : null;
      let params = {
        model_id: this.terms.selectedModel ? this.terms.selectedModel.id : null,
        env_id: this.terms.selectedEnv ? this.terms.selectedEnv.id : null,
        labels,
        page: this.pagination.page,
        per_page: this.pagination.rowsPerPage
      };
      try {
        await this.pageQuery(params);
        this.pagination.rowsNumber = this.modelEnvMap.total;
        this.list = this.modelEnvMap.list;
      } finally {
        this.loading = false;
      }
    },
    openDetail(row) {
      this.modelEvnMessage = row;
      this.modelEvn = row.variables;
      let index = 1;
      this.modelEvn.map(item => {
        if (item.json_schema && item.data_type !== '') {
          let json_schema =
            item.data_type === 'array'
              ? JSON.parse(item.json_schema).items
              : JSON.parse(item.json_schema);
          // 以下是生成table的columns
          const columnsArr = Object.keys(json_schema.properties);
          if (Array.isArray(item.value)) {
            item.value.map(val => {
              val.greatKey = columnsArr.map(col => {
                let required = false;
                try {
                  required = json_schema.required.includes(col);
                } finally {
                  return {
                    name: col,
                    desc: json_schema.properties[col].description,
                    value: val ? val[col] : '',
                    required: required
                  };
                }
              });
              val.expand = false;
            });
          } else {
            item.greatKey = columnsArr.map(col => {
              let required = false;
              try {
                required = json_schema.required.includes(col);
              } finally {
                return {
                  name: col,
                  desc: json_schema.properties[col].description,
                  value: item.value ? item.value[col] : '',
                  required: required
                };
              }
            });
          }
          item.index = index++;
        }
      });
      this.handleShow = true;
    },
    // 页面跳转
    goToManagePage(path, id) {
      this.$router.push({
        path: `/envModel/modelEvnList/${path}`,
        query: {
          id: id
        }
      });
    },
    goToHistoryMsg(env_id, model_id) {
      this.$router.push({
        path: `/envModel/modelEvnList/historyMsg/${env_id}`,
        query: {
          model_id: model_id
        }
      });
    },
    // 显示输入验证码的对话框
    handleDeleteDialogOpened(row) {
      const { id, env_name_en, model_name_en } = row;
      this.deleteDialogOpened = true;
      this.deleteId = id;
      this.deleteEnvName = env_name_en;
      this.deleteModelName = model_name_en;
    },
    // 删除实体映射
    async handleDelete(inputVerifyCode) {
      await this.deleteModelEvn({
        id: this.deleteId,
        verfityCode: inputVerifyCode
      });
      successNotify('删除成功');
      this.deleteDialogOpened = false;
      this.query();
    },
    showTable(props) {
      if (props.row.index && props.row.index === 1) {
        return !props.expand;
      } else if (props.row.index && props.row.index !== 1) {
        return props.expand;
      }
    },
    taggleIcon(props) {
      if (props.row.index && props.row.index === 1) {
        return !props.expand ? 'arrow_drop_up' : 'arrow_drop_down';
      } else if (props.row.index && props.row.index !== 1) {
        return props.expand ? 'arrow_drop_up' : 'arrow_drop_down';
      }
    }
  },
  async created() {
    // 环境配置管理员才有权限增删改
    await this.fetchCurrent();
    let managerUser = this.currentUser.role.find(item => {
      return item.label === '环境配置管理员';
    });
    if (managerUser) {
      this.isManagerRole = true;
    }
    // 获取实体下拉列表,环境下拉列表
    await this.getModelList();
    await this.getEnvList();
    this.modelOptions = this.modelList;
    this.envOptions = this.envList;
    if (Object.keys(this.$route.query).length > 0) {
      // 新增和复制成功 显示当前新增和复制的搜索列表(不包括环境)
      this.terms.selectedModel = this.modelList.find(
        item => item.id === this.$route.query.model_id
      );
    } else {
      // 查询记录保留
      // if (sessionStorage.getItem('modelName')) {
      //   const { selectedModel, labels } = JSON.parse(
      //     sessionStorage.getItem('modelName')
      //   );
      //   this.selectedModel = selectedModel;
      //   this.labels = labels;
      // }
    }

    // 获取所有标签列表
    await this.queryAllLabels();
    this.labelOptions = this.envLablesOption;
  },
  mounted() {
    this.pagination = this.currentPage;
    this.query();
    this.managerAuth(this.terms.selectedModel);
    if (!this.visibleCols.toString() || this.visibleCols.length <= 2) {
      this.saveVisibleColumns([
        'model',
        'model_name_en',
        'env',
        'env_name_en',
        'labels',
        'desc',
        'btn'
      ]);
    }
  }
};
</script>
<style lang="stylus" scoped>
.btn
  width 30px
.history
  width 55px
.select
  width 250px
.td_desc
	box-sizing border-box
	width 20%
	max-width 130px
	overflow hidden
	text-overflow ellipsis
.table
  max-width 100vw
.tr_bac td
  background #fff
  border none!important
.tr_bac:hover
  background #fff
.desc
  box-sizing border-box
  width 30%
  max-width 130px
  overflow hidden
  text-overflow ellipsis
.q-dialog__inner--minimized > div
  max-width 1200px
.q-layout-container
  min-height 600px
  height 0
.span-msg
  padding 0 30px
  overflow hidden
  display inline-block
  vertical-align  text-bottom!important
.span-desc
  white-space nowrap
  max-width 400px
  text-overflow ellipsis
.hover-tr
  background #fff
.hover-tr:hover
  background #fff
.hover-tr td
  border none
.desc-td
  max-width 320px
  overflow hidden
  text-overflow ellipsis
.q-table--col-auto-width
  padding-right 10px!important
.select-width
  width 210px
.clearfix:after
  content ''
  display block
  clear:both
  visibility hidden
.width100
  width 100%
.select-input
 word-break break-all
</style>
