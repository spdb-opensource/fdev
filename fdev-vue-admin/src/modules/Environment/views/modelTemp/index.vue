<template>
  <f-block>
    <Loading :visible="loading">
      <div class="bg-white">
        <fdev-table
          :data="modelList"
          :columns="columns"
          row-key="id"
          noExport
          titleIcon="list_s_f"
          title="实体模版列表"
          :pagination.sync="pagination"
          no-select-cols
          :on-search="query"
          :visible-columns="visibleCols"
          @request="query"
        >
          <template v-slot:top-bottom>
            <f-formitem
              class="col-4"
              bottom-page
              label="实体模板英文名"
              label-style="padding-left:16px"
            >
              <fdev-input
                clearable
                @keydown.enter="query"
                :value="terms.modelNameEn"
                @input="saveModelNameEn($event)"
              />
            </f-formitem>
            <f-formitem
              class="col-4"
              bottom-page
              label="实体模板中文名"
              label-style="padding-left:16px"
            >
              <fdev-input
                clearable
                @keydown.enter="query"
                :value="terms.modelNameCn"
                @input="saveModelNameCn($event)"
                type="text"
              />
            </f-formitem>
            <f-formitem
              class="col-4"
              bottom-page
              label="实体模板属性英文名"
              label-style="padding-left:16px"
            >
              <fdev-input
                clearable
                @keydown.enter="query"
                :value="terms.modelEnvKey"
                @input="saveModelEnvKey($event)"
                type="text"
              />
            </f-formitem>
          </template>

          <template v-slot:top-right>
            <fdev-btn
              ficon="add"
              normal
              label="新增实体模板"
              @click="goToManagePage('add')"
            />
          </template>

          <template v-slot:body-cell-nameCn="props">
            <fdev-td class="text-ellipsis">
              <router-link
                :to="`/envModel/modelTempList/${props.row.id}`"
                :title="props.row.nameCn"
                class="link"
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

          <template v-slot:body-cell-desc="props">
            <fdev-td class="text-ellipsis" :title="props.row.desc || '-'">
              {{ props.row.desc || '-' }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.desc || '-' }}
                </fdev-banner>
              </fdev-popup-proxy>
            </fdev-td>
          </template>

          <!-- 高级属性待定，后续操作暂时保留 -->
          <!-- <template v-slot:body-cell-btn="props" v-if="isManagerRole && isSpecialGroup">
            <fdev-td :auto-width="true">
              <fdev-btn label="复制" flat size="sm" class="btn" color="primary" @click="goToManagePage('copy', props.row.id)" />
              <fdev-btn label="修改" flat size="sm" class="btn" color="primary" @click="goToManagePage('update', props.row.id)" />
              <fdev-btn label="删除" flat size="sm" class="btn" color="red" @click="handleDeleteDialogOpened(props.row)" />
            </fdev-td>
          </template> -->
        </fdev-table>
        <!-- <div class="row justify-center" v-if="isManagerRole"></div> -->
      </div>
      <!-- 弹出 输入验证码删除对话框 -->
      <!-- <fdev-dialog v-model="deleteDialogOpened">
        <VerifycodeDialog @handleDelete="handleDelete" :description="description" />
      </fdev-dialog> -->
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { modelTempColumns } from '../../utils/constants';
import { mapActions, mapState, mapGetters, mapMutations } from 'vuex';

export default {
  name: 'ModelTempList',
  components: {
    Loading
    // VerifycodeDialog
  },
  data() {
    return {
      deleteDialogOpened: false, //对话框
      deleteId: '',
      deleteNameEn: '',
      pagination: {},
      loading: false,
      rowSelected: [],
      columns: modelTempColumns().columns,
      filter: '',
      isManagerRole: false,
      modelNameEn: '',
      modelNameCn: '',
      modelEnvKey: '',
      modelList: []
    };
  },
  computed: {
    ...mapState('environmentForm', {
      modelTempListPaging: 'modelTempListPaging'
    }),
    ...mapState('user', {
      user: 'currentUser'
    }),
    ...mapGetters('user', ['isSpecialGroup']),
    ...mapState('userActionSaveEnv/modelTemplate', [
      'visibleCols',
      'currentPage',
      'terms'
    ]),
    /* description() {
      return `确认删除${this.deleteNameEn}实体吗?删除后不可恢复`;
    }, */
    visibaleOptions: {
      set(visibaleOptions) {
        return visibaleOptions;
      },
      get() {
        const arr = this.columns.slice(0);
        return arr;
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
      // 按钮添加暂时保留
      // if (val) {
      //   this.columns.push({ name: 'btn', label: '操作', align: 'center' });
      // }
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
    'terms.modelEnvKey'(val) {
      if (!val) {
        this.query();
      }
    }
  },
  methods: {
    ...mapActions('environmentForm', {
      deleteModel: 'deleteModel', //删除实体
      getModelTempListPaging: 'getModelTempListPaging' //获取实体列表
    }),
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent'
    }),
    ...mapMutations('userActionSaveEnv/modelTemplate', [
      'saveVisibleColumns',
      'saveCurrentPage',
      'saveModelNameEn',
      'saveModelNameCn',
      'saveModelEnvKey'
    ]),
    // 显示输入验证码的对话框
    /* handleDeleteDialogOpened(row) {
      const { id, name_en } = row;
      this.deleteDialogOpened = true;
      this.deleteId = id;
      this.deleteNameEn = name_en;
    }, */
    // 删除实体
    /* async handleDelete(inputVerifyCode) {
      await this.deleteModel({
        id: this.deleteId,
        verfityCode: inputVerifyCode
      });
      successNotify('删除成功');
      this.deleteDialogOpened = false;
      this.init();
    }, */
    goToManagePage(path, id) {
      this.$router.push({
        path: `/envModel/modelTempEdit/${path}`,
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
        page: this.pagination.page,
        perPage: this.pagination.rowsPerPage,
        nameEn: this.terms.modelNameEn,
        nameCn: this.terms.modelNameCn,
        envKey: this.terms.modelEnvKey
      };
      try {
        await this.getModelTempListPaging(params);
        this.modelList = this.modelTempListPaging.list;
        this.pagination.rowsNumber = this.modelTempListPaging.total;
      } finally {
        this.loading = false;
      }
    }
  },
  async mounted() {
    this.pagination = this.currentPage;
    if (!this.visibleCols.toString() || this.visibleCols.length < 2) {
      this.saveVisibleColumns([
        'nameCn',
        'nameEn',
        'desc'
        // 'btn'
      ]);
    }
    this.loading = true;
    await this.init();
    await this.fetchCurrent();
    // 环境配置管理员才有权限增删改
    let managerUser = this.user.role.find(item => {
      return item.label === '环境配置管理员';
    });
    if (managerUser) {
      this.isManagerRole = true;
    }
    this.loading = false;
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
</style>
