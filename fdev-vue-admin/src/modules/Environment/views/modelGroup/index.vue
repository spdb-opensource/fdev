<template>
  <f-block>
    <Loading :visible="loading">
      <div class="bg-white">
        <fdev-table
          :data="list"
          :columns="columns"
          :filter="filter"
          noExport
          title="实体组列表"
          titleIcon="list_s_f"
          :filter-method="tableFilter"
          row-key="id"
          no-select-cols
          :on-search="init"
          @request="init"
          :pagination.sync="pagination"
          :visible-columns="visibleCols"
        >
          <template v-slot:top-bottom>
            <f-formitem
              class="col-4"
              bottom-page
              label-style="padding-left:40px"
              label="实体组模板"
            >
              <fdev-select
                use-input
                ref="template"
                clearable
                :value="terms.template"
                @input="saveTemplate($event)"
                :options="options"
                option-label="label"
                option-value="value"
              />
            </f-formitem>
            <f-formitem
              class="col-4"
              bottom-page
              label-style="padding-left:40px"
              label="实体组名称"
            >
              <fdev-input
                clearable
                @keyup.enter="init"
                :value="terms.modelGroupCn"
                @input="saveModelGroupCn($event)"
                type="text"
              />
            </f-formitem>
            <f-formitem
              class="col-4"
              bottom-page
              label-style="padding-left:80px"
              label="实体"
            >
              <fdev-select
                use-input
                clearable
                :value="terms.selectedModel"
                @input="saveSelectedModel($event)"
                :options="modelOptions"
                option-label="name_en"
                option-value="id"
                @filter="modelFilterChoice"
                class="input"
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
          </template>
          <template v-slot:top-right>
            <fdev-btn
              icon="add"
              normal
              label="新增实体组"
              @click="goToAddPage('add')"
            />
          </template>
          <template v-slot:body-cell-modelsInfo="props">
            <fdev-td>
              <div
                :title="props.row.modelsInfo.map(v => v.name_en).join('，')"
                class="q-gutter-x-sm text-ellipsis"
              >
                <span
                  v-for="item in props.row.modelsInfo"
                  :key="item.id"
                  class="q-pr-sm"
                >
                  {{ item.name_en }}
                </span>
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.modelsInfo.map(v => v.name_en).join('，') }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </div>
            </fdev-td>
          </template>
          <template v-slot:body-cell-btn="props" v-if="isManagerRole">
            <fdev-td :auto-width="true">
              <div class="q-gutter-x-sm">
                <fdev-btn
                  label="编辑"
                  flat
                  @click="goToUpdatePage('update', props.row)"
                />
                <fdev-btn
                  label="删除"
                  flat
                  @click="handelDelete(props.row.id)"
                />
              </div>
            </fdev-td>
          </template>
        </fdev-table>
        <div class="row justify-center" v-if="isManagerRole"></div>
      </div>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState, mapMutations } from 'vuex';
import { modelGroupColumns } from '../../utils/constants';
import { successNotify } from '@/utils/utils';
export default {
  name: 'ModelGroupList',
  components: {
    Loading
  },
  data() {
    return {
      pagination: {},
      template: [],
      loading: false,
      rowSelected: [],
      list: [],
      options: [],
      columns: modelGroupColumns().columns,
      filter: '',
      isManagerRole: false,
      modelGroupCn: '',
      modelOptions: [],
      selectedModel: null
    };
  },
  computed: {
    ...mapState('environmentForm', {
      modelSetList: 'modelSetList', //实体组列表
      modelType: 'modelType',
      templateModelList: 'templateModelList' //实体选项列表
    }),
    ...mapState('user', {
      user: 'currentUser'
    }),
    ...mapState('userActionSaveEnv/modelGroupList', [
      'visibleCols',
      'currentPage',
      'terms'
    ]),
    visibleOptions() {
      const arr = this.columns.slice(0);
      let managerUser = this.user.role.find(item => {
        return item.label === '环境配置管理员';
      });
      if (managerUser) {
        return arr.splice(0, arr.length - 1);
      } else {
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
      if (val) {
        this.columns.push({ name: 'btn', label: '操作' });
      }
    },
    // template(val) {
    //   this.init();
    // },
    'terms.template'(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    },
    'terms.modelGroupCn'(val) {
      if (!val) {
        this.init();
      }
    },
    'terms.selectedModel'(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    }
  },
  methods: {
    ...mapActions('environmentForm', {
      queryModelSetList: 'queryModelSetList', //获取实体组列表
      deleteModelSet: 'deleteModelSet', //删除实体组
      getType: 'getType',
      queryTemplateContainsModel: 'queryTemplateContainsModel' //获取实体列表
    }),
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent'
    }),
    ...mapMutations('userActionSaveEnv/modelGroupList', [
      'saveVisibleColumns',
      'saveCurrentPage',
      'saveTemplate',
      'saveModelGroupCn',
      'saveSelectedModel'
    ]),
    modelFilterChoice(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.modelOptions = this.templateModelList.filter(v => {
          return (
            v.name_en.toLowerCase().indexOf(needle) > -1 ||
            v.name_cn.indexOf(val) > -1
          );
        });
      });
    },
    handelDelete(id) {
      this.$q
        .dialog({
          title: '确认删除',
          message: '您确定删除该实体组吗?删除不可恢复！',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteModelSet({ id: id });
          successNotify('删除成功');
          this.init();
        });
    },
    goToUpdatePage(path, row) {
      let models = row.modelsInfo.map(item => item.name_en);
      this.$router.push({
        path: `/envModel/modelGroupManage/${path}`,
        query: {
          temp: row.template,
          name: row.nameCn,
          id: row.id,
          models
        }
      });
    },
    goToAddPage(path, row) {
      this.$router.push({
        path: `/envModel/modelGroupManage/${path}`
      });
    },
    addTerm(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    // 表格 输入搜索功能
    tableFilter(rows, terms, cols, cellValue) {
      const lowerTerms = terms ? terms.toLowerCase().split(',') : [];
      return rows.filter(row => {
        return lowerTerms.every(term => {
          if (term.startsWith('__') || term === '') {
            return true;
          }
          let hasModel =
            row.modelsInfo
              .map(model => model.name_en)
              .toString()
              .toLowerCase()
              .indexOf(term) > -1;
          let hasCol = cols.some(col => {
            return (cellValue(col, row) + '').toLowerCase().indexOf(term) > -1;
          });
          return hasCol || hasModel;
        });
      });
    },
    async init(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      let params = {
        name_cn: this.terms.modelGroupCn,
        template: this.terms.template ? this.terms.template.value : null,
        id: this.terms.selectedModel ? this.terms.selectedModel.id : null,
        page: this.pagination.page,
        per_page: this.pagination.rowsPerPage
      };
      await this.queryModelSetList(params);
      this.list = this.modelSetList.list;
      this.pagination.rowsNumber = this.modelSetList.total;
    }
  },
  async created() {
    this.loading = true;
    await this.getType();
    this.options = this.modelType.map(item => {
      return {
        label: item.template,
        value: item.template
      };
    });
    await this.queryTemplateContainsModel();
    this.modelOptions = this.templateModelList;
    this.init();
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
    this.pagination = this.currentPage;
    if (!this.visibleCols.toString() || this.visibleCols.length <= 2) {
      this.saveVisibleColumns(['name_cn', 'modelsInfo', 'template', 'btn']);
    }
  }
};
</script>
<style lang="stylus" scoped>
.btn
  width 30px
.q-table--col-auto-width
  padding-right 10px!important
.width-search
  width 210px
</style>
