<template>
  <Loading :visible="loading">
    <fdev-table
      title="配置依赖列表"
      titleIcon="list_s_f"
      :data="copiedConfigList"
      :columns="columns"
      :filter="filter"
      row-key="id"
      class="q-py-md"
      :on-search="handelAppList"
      :export-func="exportDependency"
      :visible-columns="visibleCols"
      :pagination.sync="pagination"
    >
      <template v-slot:top-bottom>
        <div class="row">
          <f-formitem
            :class="model.name_en ? 'col-4' : ''"
            bottom-page
            label-style="padding-left:40px"
            label="实体英文名"
          >
            <fdev-select
              use-input
              ref="model.name_en"
              clearable
              v-model="$v.model.name_en.$model"
              :options="options"
              option-label="name_en"
              option-value="name_en"
              @filter="nameFilter"
              :rules="[() => $v.model.name_en.required || '请输入实体英文名']"
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
            label="实体属性"
            label-style="padding-left:50px"
            v-show="model.name_en"
          >
            <fdev-select
              use-input
              ref="model.field_name_en"
              :value="termsApp.field_name_en"
              @input="saveFieldNameEn($event)"
              :options="fieldFilterOptions"
              option-label="name_en"
              option-value="name_en"
              @filter="fieldFilter"
              clearable
              :rules="[() => true]"
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
            bottom-page
            label-style="padding-left:30px"
            :class="model.name_en ? 'col-4' : ''"
            label="选择搜索范围"
          >
            <fdev-select
              multiple
              ref="model.range"
              clearable
              :value="termsApp.range"
              @input="saveRange($event)"
              :options="branchs"
              :option-disable="item => item === 'master'"
              :rules="[() => $v.model.range.required || '请选择搜索范围']"
            />
          </f-formitem>
        </div>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { createModel, downUrl, configDepColumns } from '../../utils/constants';
import { mapActions, mapState, mapMutations } from 'vuex';
import { validate, errorNotify } from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';

export default {
  name: 'aloneConfFile',
  components: {
    Loading
  },
  data() {
    return {
      loading: false,
      model: createModel(),
      branchs: ['master', 'sit', 'release'],
      pagination: {},
      columns: configDepColumns().propertyColumns,
      filter: '',
      options: [],
      fieldOptions: [],
      fieldFilterOptions: [],
      copiedConfigList: [], //搜索到的配置信息列表
      flag: true
    };
  },
  validations: {
    model: {
      name_en: {
        required
      },
      field_name_en: {},
      range: {
        required
      }
    }
  },
  watch: {
    'model.name_en'(model) {
      if (!this.flag) this.model.field_name_en = '';
      this.fieldOptions =
        model &&
        model.env_key.filter(key => {
          return key.name_en;
        });
      this.saveNameEn(model);
    },
    pagination(val) {
      this.saveCurrentPage({
        rowsPerPage: val.rowsPerPage
      });
    }
  },
  computed: {
    ...mapState('environmentForm', {
      confList: 'confList',
      modelList: 'modelList',
      exportData: 'exportData'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('userActionSaveEnv/configDepAnalysis', [
      'visibleCols',
      'currentPage',
      'termsApp'
    ])
  },
  methods: {
    ...mapActions('environmentForm', ['confDependList', 'getModelList']),
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent'
    }),
    ...mapMutations('userActionSaveEnv/configDepAnalysis', [
      'saveVisibleColumns',
      'saveCurrentPage',
      'saveNameEn',
      'saveFieldNameEn',
      'saveRange'
    ]),
    chineseName(val) {
      return val
        .map(item => {
          return item.user_name_cn;
        })
        .join('，');
    },
    async exportDependency() {
      this.$v.model.$touch();
      let Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('model') > -1;
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.model.$invalid) {
        return;
      }
      if (!this.model.field_name_en) {
        this.model.field_name_en = '';
      }
      this.loading = true;
      try {
        window.open(
          `${downUrl}?field_name_en=${this.model.field_name_en.name_en ||
            ''}&model_name_en=${this.model.name_en.name_en}&range=${
            this.model.range
          }`
        );
        this.loading = false;
      } catch (error) {
        this.loading = false;
      }
    },
    async handelAppList() {
      this.$v.model.$touch();
      let Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('model') > -1;
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.model.$invalid) {
        return;
      }
      this.loading = true;
      try {
        if (!this.model.field_name_en) {
          this.model.field_name_en = '';
        }
        await this.confDependList({
          ...this.model,
          range: this.model.range,
          model_name_en: this.model.name_en.name_en,
          field_name_en: this.model.field_name_en.name_en
        });
        this.copiedConfigList = this.confList;
        this.loading = false;
      } catch (error) {
        this.loading = false;
      }
    },
    async nameFilter(val, update, abort) {
      update(() => {
        let filter = this.modelList.filter(item => {
          return (
            item.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            item.name_cn.indexOf(val) > -1
          );
        });
        this.options = filter;
      });
    },
    async fieldFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.fieldFilterOptions = this.fieldOptions.filter(
          v =>
            v.name_en.toLowerCase().indexOf(needle) > -1 ||
            v.name_cn.indexOf(needle) > -1
        );
      });
    }
  },
  async created() {
    await this.fetchCurrent();
    let role_names = this.currentUser.role.map(each => each.name);
    if (
      !role_names.includes('环境配置管理员') &&
      !role_names.includes('厂商项目负责人') &&
      !role_names.includes('行内项目负责人') &&
      !role_names.includes('开发人员')
    ) {
      errorNotify('当前用户无权限进行环境配置操作');
      this.$router.push('/envModel/envlist');
      return;
    }
    this.copiedConfigList = []; //每次进来清空配置信息列表
    await this.getModelList();
    this.options = this.modelList;
  },
  async mounted() {
    this.pagination = this.currentPage;
    this.model = this.termsApp;
    await this.$nextTick();
    this.flag = false;
    if (this.model.name_en) {
      this.handelAppList();
    }
    if (!this.visibleCols.toString() || this.visibleCols.length <= 1) {
      this.saveVisibleColumns([
        'name_cn',
        'name_en',
        'app_manager',
        'spdb_manager',
        'group',
        'gitlab',
        'branch'
      ]);
    }
  }
};
</script>
<style lang="stylus" scoped>
.bg-white
  padding-top 20px
.input
  min-width 200px
  max-width 350px
  padding-bottom 10px
.input-bottom >>> .q-placeholder
  width 0!important
.container
  display flex
  margin-bottom 20px
.container .col
  flex 1
  display flex
  align-items center
  justify-content center
.container >>> .q-field__native
  max-height 24px!important
.input-relate
  display inline-block
  flex 1.3!important
.width210
  width 210px
.width150
  width 150px
.positionRight
  position absolute
  right 5px
  top 20px
.btn-height
  height 40px
</style>
