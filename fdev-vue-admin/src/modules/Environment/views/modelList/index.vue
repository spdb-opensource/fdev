<template>
  <f-block>
    <Loading :visible="loading">
      <div class="bg-white">
        <fdev-table
          noExport
          class="my-sticky-column-table"
          title="实体列表"
          titleIcon="list_s_f"
          :data="modelList"
          :columns="columns"
          row-key="id"
          :pagination.sync="pagination"
          :on-search="query"
          :visible-columns="visibleCols"
          @request="query"
        >
          <template v-slot:top-bottom>
            <f-formitem
              bottom-page
              label="实体英文名"
              class="col-4"
              label-style="padding-left:40px"
            >
              <fdev-input
                clearable
                :value="terms.modelNameEn"
                @input="saveModelNameEn($event)"
                @keydown.enter="query"
              />
            </f-formitem>
            <f-formitem
              bottom-page
              label="实体中文名"
              class="col-4"
              label-style="padding-left:40px"
            >
              <fdev-input
                clearable
                :value="terms.modelNameCn"
                @input="saveModelNameCn($event)"
                @keydown.enter="query"
                type="text"
              />
            </f-formitem>
            <f-formitem
              bottom-page
              label="实体模板"
              class="col-4"
              label-style="padding-left:40px"
            >
              <fdev-select
                use-input
                clearable
                :value="terms.modelTemplateId"
                @input="saveModelTemplateId($event)"
                :options="modelOptions"
                option-label="nameEn"
                @filter="modelFilterChoice"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.nameEn">{{
                        scope.opt.nameEn
                      }}</fdev-item-label>
                      <fdev-item-label caption :title="scope.opt.nameCn">
                        {{ scope.opt.nameCn }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
            <!-- <fdev-btn label="查询" color="primary" @click="query" /> -->
          </template>
          <template v-slot:top-right>
            <fdev-btn
              ficon="add"
              label="新增实体"
              normal
              v-if="isManagerRole"
              @click="goToManagePage('add')"
            />
          </template>
          <template v-slot:body-cell-name_cn="props">
            <fdev-td class="text-ellipsis">
              <router-link
                class="link"
                :to="`/envModel/modelList/${props.row.id}`"
                :title="props.row.name_cn"
                v-if="props.row.name_cn"
                >{{ props.row.name_cn }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.name_cn }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
              <div v-else title="-">-</div>
              <!-- <router-link
                class="text-primary"
                :to="`/envModel/modelList/${props.row.id}`"
              >
                {{ props.row.name_cn }}
              </router-link> -->
            </fdev-td>
          </template>

          <template v-slot:body-cell-desc="props">
            <fdev-td :title="props.row.desc || '-'" class="text-ellipsis">
              {{ props.row.desc || '-' }}
            </fdev-td>
            <!-- <fdev-td class="text-ellipsis">
              <fdev-tooltip
                anchor="top middle"
                self="center middle"
                :offest="[0, 0]"
                v-if="props.row.desc"
              >
                {{ props.row.desc }}
              </fdev-tooltip>
              {{ props.row.desc }}
            </fdev-td> -->
          </template>

          <template v-slot:body-cell-model_template_id="props">
            <fdev-td
              :title="
                props.row.model_template_id
                  ? props.row.model_template_name_cn
                  : '-'
              "
              class="text-ellipsis"
            >
              {{
                props.row.model_template_id
                  ? props.row.model_template_name_cn
                  : '-'
              }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-btn="props" v-if="isManagerRole">
            <fdev-td :auto-width="true">
              <div class="q-gutter-x-sm">
                <fdev-btn
                  label="复制"
                  flat
                  @click="goToManagePage('copy', props.row.id)"
                />
                <fdev-btn
                  label="修改"
                  flat
                  @click="goToManagePage('update', props.row.id)"
                />
                <fdev-btn
                  label="删除"
                  flat
                  @click="handleDeleteDialogOpened(props.row)"
                />
              </div>
            </fdev-td>
          </template>
        </fdev-table>
      </div>
      <!-- 弹出 输入验证码删除对话框 -->
      <VerifycodeDialog
        v-model="deleteDialogOpened"
        @handleDelete="handleDelete"
        :description="description"
        :loading="globalLoading['environmentForm/deleteModel']"
      />
      <!-- <fdev-dialog v-model="deleteDialogOpened">
        <VerifycodeDialog
          @handleDelete="handleDelete"
          :description="description"
        />
      </fdev-dialog> -->
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import VerifycodeDialog from '../../components/VerifycodeDialog';
import { mapActions, mapState, mapMutations } from 'vuex';
import { modelListColumns } from '../../utils/constants';
import { successNotify } from '@/utils/utils';

export default {
  name: 'ModelList',
  components: {
    Loading,
    VerifycodeDialog
  },
  data() {
    return {
      modelOptions: [],
      tempArr: [],
      deleteDialogOpened: false, //对话框
      deleteId: '',
      deleteNameEn: '',
      model: {},
      pagination: {},
      loading: false,
      rowSelected: [],
      columns: modelListColumns().columns,
      filter: '',
      isManagerRole: false,
      modelList: []
    };
  },
  computed: {
    ...mapState('environmentForm', {
      modelListPaging: 'modelListPaging',
      modelTempListPaging: 'modelTempListPaging'
    }),
    ...mapState('user', {
      user: 'currentUser'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('userActionSaveEnv/modelList', [
      'visibleCols',
      'currentPage',
      'terms'
    ]),
    description() {
      return `确认删除${this.deleteNameEn}实体吗?删除后不可恢复`;
    },
    visibaleOptions: {
      set(visibaleOptions) {
        return visibaleOptions;
      },
      get() {
        const arr = this.columns.slice(0);
        if (this.isManagerRole) {
          return arr.splice(0, arr.length - 1);
        } else {
          return arr;
        }
      }
    }
  },
  watch: {
    pagination: {
      handler(val) {
        this.saveCurrentPage(val);
      },
      deep: true
    },
    isManagerRole(val) {
      if (val) {
        this.columns.push({ name: 'btn', label: '操作' });
      }
      this.visibaleOptions = this.columns;
    },
    'terms.modelNameEn'(val) {
      if (!val) {
        this.query();
      }
    },
    'terms.modelNameCn'(val) {
      if (!val) {
        this.query();
      }
    },
    'terms.modelTemplateId'(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.query();
      }
    }
  },
  methods: {
    ...mapActions('environmentForm', {
      deleteModel: 'deleteModel', //删除实体
      getModelListPaging: 'getModelListPaging', //获取实体列表
      getModelTempListPaging: 'getModelTempListPaging'
    }),
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent'
    }),
    ...mapMutations('userActionSaveEnv/modelList', [
      'saveVisibleColumns',
      'saveCurrentPage',
      'saveModelNameEn',
      'saveModelNameCn',
      'saveModelTemplateId'
    ]),
    modelFilterChoice(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.modelOptions = this.modelTempListPaging.list.filter(v => {
          return (
            v.nameEn.toLowerCase().indexOf(needle) > -1 ||
            v.nameCn.indexOf(val) > -1
          );
        });
      });
    },
    // 显示输入验证码的对话框
    handleDeleteDialogOpened(row) {
      const { id, name_en } = row;
      this.deleteDialogOpened = true;
      this.deleteId = id;
      this.deleteNameEn = name_en;
    },
    // 删除实体
    async handleDelete(inputVerifyCode) {
      await this.deleteModel({
        id: this.deleteId,
        verfityCode: inputVerifyCode
      });
      successNotify('删除成功');
      this.deleteDialogOpened = false;
      this.init();
    },
    goToManagePage(path, id) {
      this.$router.push({
        path: `/envModel/modelEdit/${path}`,
        query: { id }
      });
    },
    //查询
    query(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      this.init();
    },
    async init() {
      this.loading = true;
      let params = {
        model_template_id: this.terms.modelTemplateId
          ? this.terms.modelTemplateId.id
          : '',
        page: this.pagination.page,
        per_page: this.pagination.rowsPerPage,
        name_en: this.terms.modelNameEn,
        name_cn: this.terms.modelNameCn
      };
      try {
        await this.getModelListPaging(params);
        this.modelList = this.modelListPaging.list;
        this.pagination.rowsNumber = this.modelListPaging.total;
      } finally {
        this.loading = false;
      }
    }
  },
  async created() {
    this.pagination = this.currentPage;
    this.loading = true;
    await this.init();
    let paramsTemp = {
      page: 1,
      perPage: 0,
      nameEn: '',
      nameCn: '',
      envKey: ''
    };
    await this.getModelTempListPaging(paramsTemp);
    this.tempArr = this.modelTempListPaging.list.map(item => {
      return { value: item.id, label: item.nameCn };
    });
    this.modelOptions = this.modelTempListPaging.list;
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
    this.filter = this.terms.toString();
    if (!this.visibleCols.toString() || this.visibleCols.length <= 2) {
      this.saveVisibleColumns([
        'name_cn',
        'name_en',
        'one_type',
        'two_type',
        'suffix_name',
        'action_scope',
        'desc',
        'model_template_id',
        'platform',
        'btn'
      ]);
    }
  }
};
</script>
<style lang="stylus" scoped>

.btn {
  width: 30px;
}

.td-desc {
  box-sizing: border-box;
  width: 20%;
  max-width: 130px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.q-table--col-auto-width {
  padding-right: 10px !important;
}

.width-search {
  width: 210px;
}

.select-input {
  word-break: break-all;
}

.select-width {
  width: 210px;
}
</style>
