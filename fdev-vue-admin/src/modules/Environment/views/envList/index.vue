<template>
  <f-block>
    <div class="bg-white">
      <Loading :visible="loading">
        <fdev-table
          :data="envListData"
          :columns="columns"
          row-key="id"
          :filter="searchValue"
          :filter-method="envFilter"
          :pagination.sync="pagination"
          no-export
          no-select-cols
          title-icon="list_s_f"
          title="环境列表"
          :visible-columns="visibleCols"
        >
          <!-- 可配置列 visibleColumns -->
          <template v-slot:top-right>
            <fdev-btn
              ficon="add"
              normal
              v-if="isManagerRole"
              label="新增环境"
              @click="handleEnvAddModalOpened"
            />
          </template>
          <template v-slot:top-bottom>
            <div>
              <f-formitem label="查询条件" label-auto label-class="q-px-lg">
                <fdev-select
                  use-input
                  multiple
                  hide-dropdown-icon
                  :value="terms"
                  placeholder="请输入"
                  @input="saveTerms($event)"
                  @new-value="addEnvTerm"
                  ref="terms"
                >
                  <template v-slot:append>
                    <fdev-btn flat @click="addNewValue()">
                      <f-icon name="search" class="cursor-pointer" />
                    </fdev-btn>
                  </template>
                </fdev-select>
              </f-formitem>
            </div>
          </template>
          <template v-slot:body-cell-env_message="props">
            <fdev-td class="text-ellipsis" :title="props.row.desc || '-'">
              {{ props.row.desc || '-' }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-btn="props" v-if="isManagerRole">
            <fdev-td :auto-width="true">
              <fdev-btn
                label="更新环境"
                flat
                @click="handleEnvUpdateModalOpened(props.row)"
              />
            </fdev-td>
          </template>
        </fdev-table>
        <!-- 弹出 输入验证码删除对话框 -->
        <VerifycodeDialog
          v-model="deleteDialogOpened"
          @handleDelete="handleDelete"
          :description="description"
        />
        <!-- <div class="row justify-center" v-if="isManagerRole"> -->
        <!-- <fdev-btn
            label="更新环境"
            color="primary q-mr-lg"
            text-color="white"
            class="q-mb-lg q-mt-lg"
            :disable="selected.length != 1"
            @click="handleEnvUpdateModalOpened"
          /> -->
        <!-- 环境删除下周上 -->
        <!-- <fdev-btn
            label="删除环境"
            color="primary"
            text-color="white"
            class="q-mb-lg q-mt-lg"
            :disable="selected.length != 1"
            @click="handleDeleteDialogOpened()"
          /> -->
        <!-- </div> -->
        <f-dialog v-model="addEnvModalOpened" right f-sc title="新增环境">
          <!-- <div class="container"> -->
          <form @submit.prevent="handleAddEnv">
            <f-formitem diaS label="环境中文名" required>
              <fdev-input
                v-model="$v.envVarModel.env_name_cn.$model"
                ref="envVarModel.env_name_cn"
                type="text"
                :rules="[
                  () =>
                    $v.envVarModel.env_name_cn.required || '请输入环境中文名',
                  () =>
                    $v.envVarModel.env_name_cn.hasChinese ||
                    '环境中文名必须有汉字',
                  () =>
                    $v.envVarModel.env_name_cn.isUnique || '环境中文名已存在'
                ]"
              />
            </f-formitem>
            <f-formitem diaS label="环境英文名" required>
              <fdev-input
                v-model="$v.envVarModel.env_name_en.$model"
                ref="envVarModel.env_name_en"
                type="text"
                :rules="[
                  () =>
                    $v.envVarModel.env_name_en.required || '请输入环境英文名',
                  () =>
                    $v.envVarModel.env_name_en.alphaNumAndLine ||
                    '只能字母开头且只能是字母、数字、特殊字符(.-_)的结合',
                  () =>
                    $v.envVarModel.env_name_en.isUnique || '环境英文名已存在'
                ]"
              />
            </f-formitem>
            <f-formitem diaS label="环境标签">
              <fdev-select
                use-input
                multiple
                hint=""
                v-model="envVarModel.labels"
                @filter="queryLabel"
                stack-label
                :option-disable="
                  opt => (Object(opt) === opt ? opt.inactive === true : false)
                "
                :options="filterEnvLabels"
              />
            </f-formitem>
            <f-formitem diaS label="描述信息">
              <fdev-input
                hint=""
                v-model="envVarModel.env_message"
                type="textarea"
              />
            </f-formitem>
          </form>
          <!-- </div> -->
          <template v-slot:btnSlot>
            <fdev-btn
              label="取消"
              outline
              dialog
              @click="addEnvModalOpened = false"
            />
            <fdev-btn dialog label="确认" @click="handleAddEnv" />
          </template>
        </f-dialog>

        <f-dialog v-model="updateEnvModalOpen" right f-sc title="编辑环境">
          <div class="container">
            <form @submit.prevent="handleUpdateEnv" v-if="selected.length > 0">
              <f-formitem diaS label="环境中文名" required>
                <fdev-input
                  v-model="updateModel.name_cn"
                  ref="updateModel.name_cn"
                  type="text"
                  :rules="[
                    () => $v.updateModel.name_cn.required || '请输入环境中文名',
                    () =>
                      $v.updateModel.name_cn.hasChinese ||
                      '环境中文名必须有汉字',
                    () => $v.updateModel.name_cn.isUnique || '环境中文名已存在'
                  ]"
                />
              </f-formitem>
              <f-formitem diaS label="环境英文名" required>
                <fdev-input
                  v-model="$v.updateModel.name_en.$model"
                  ref="updateModel.name_en"
                  type="text"
                  readonly
                  :rules="[
                    () => $v.updateModel.name_en.required || '请输入环境英文名',
                    () =>
                      $v.updateModel.name_en.alphaNumAndLine ||
                      '只能字母开头且只能是字母、数字、特殊字符(.-_)的结合',
                    () => $v.updateModel.name_en.isUnique || '环境英文名已存在'
                  ]"
                />
              </f-formitem>
              <f-formitem diaS label="环境标签">
                <fdev-select
                  use-input
                  multiple
                  v-model="updateModel.labels"
                  @filter="queryLabel"
                  stack-label
                  hint=""
                  :option-disable="
                    opt => (Object(opt) === opt ? opt.inactive === true : false)
                  "
                  :options="filterEnvLabels"
                />
              </f-formitem>
              <f-formitem diaS label="描述信息">
                <fdev-input
                  v-model="updateModel.desc"
                  type="textarea"
                  hint=""
                />
              </f-formitem>
            </form>
          </div>
          <template v-slot:btnSlot>
            <fdev-btn
              label="取消"
              outline
              dialog
              @click="updateEnvModalOpen = false"
            />
            <fdev-btn dialog label="确定" @click="handleUpdateEnv" />
          </template>
        </f-dialog>
      </Loading>
    </div>
  </f-block>
</template>

<script>
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import { createEnvModel, envListColumns } from '../../utils/constants';
import { required } from 'vuelidate/lib/validators';
import VerifycodeDialog from '../../components/VerifycodeDialog';
import Loading from '@/components/Loading';
import { validate, successNotify, deepClone } from '@/utils/utils';

export default {
  name: 'envList',
  components: { Loading, VerifycodeDialog },
  data() {
    return {
      deleteDialogOpened: false, //对话框
      deleteId: '',
      deleteNameEn: '',
      loading: true, // 罩层
      columns: envListColumns(),
      data: [],
      selected: [],
      addEnvModalOpened: false,
      updateEnvModalOpen: false,
      pagination: {},
      envVarModel: createEnvModel(),
      updateModel: {},
      isManagerRole: false,
      singleOrNone: 'none',
      filterEnvLabels: [],
      labels: []
    };
  },
  validations: {
    updateModel: {
      name_en: {
        required,
        alphaNumAndLine(value) {
          let re = new RegExp(/^[a-zA-Z]{1}[a-zA-Z0-9.\-_]*$/g);
          return re.test(value);
        },
        isUnique(value) {
          let newValue = value.replace(/(^\s*)|(\s*$)/g, '');
          if (!newValue) {
            return true;
          }
          let val = this.updateFilterData.filter(app => {
            return app.name_en === newValue;
          });
          return val.length === 0;
        }
      },
      name_cn: {
        required,
        isUnique(value) {
          let newValue = value.replace(/(^\s*)|(\s*$)/g, '');
          if (!newValue) {
            return true;
          }
          let val = this.updateFilterData.filter(app => {
            return app.name_cn === newValue;
          });
          return val.length === 0;
        },
        hasChinese(value) {
          if (!value) {
            return true;
          }
          let reg = new RegExp(/[\u4e00-\u9fa5]/gm);
          let flag = reg.test(value);
          return flag;
        }
      }
    },
    envVarModel: {
      env_name_en: {
        required,
        alphaNumAndLine(value) {
          let re = new RegExp(/^[a-zA-Z]{1}[a-zA-Z0-9.\-_]*$/g);
          return re.test(value);
        },
        isUnique(value) {
          let newValue = value.replace(/(^\s*)|(\s*$)/g, '');
          if (!newValue) {
            return true;
          }
          let val = this.envListData.filter(app => {
            return app.name_en === newValue;
          });
          return val.length === 0;
        }
      },
      env_name_cn: {
        required,
        isUnique(value) {
          if (!value) {
            return true;
          }
          let val = this.envListData.filter(app => {
            return app.name_cn === value;
          });
          return val.length === 0;
        },
        hasChinese(value) {
          if (!value) {
            return true;
          }
          let reg = new RegExp(/[\u4e00-\u9fa5]/gm);
          let flag = reg.test(value);
          return flag;
        }
      }
    }
  },
  watch: {
    isManagerRole(val) {
      if (val) {
        this.columns.push({ name: 'btn', label: '操作' });
      }
    },
    pagination(val) {
      this.saveCurrentPage({
        rowsPerPage: val.rowsPerPage
      });
    },
    'envVarModel.labels': {
      handler(val) {
        this.separateOption.forEach(list => {
          list.some(label => {
            return this.changeLabelsOption(val, label, list);
          });
        });
      }
    },
    'updateModel.labels': {
      handler(val) {
        this.separateOption.forEach(list => {
          list.some(label => {
            return this.changeLabelsOption(val, label, list);
          });
        });
      }
    },
    addEnvModalOpened(val) {
      if (val) {
        this.envVarModel = createEnvModel();
      }
    }
  },
  computed: {
    ...mapState('user', {
      user: 'currentUser'
    }),
    ...mapState('environmentForm', {
      envListData: 'envList'
    }),
    ...mapGetters('environmentForm', {
      envLablesOption: 'envLablesOption',
      separateOption: 'separateOption'
    }),
    ...mapGetters('userActionSaveEnv/envList', ['searchValue']),
    ...mapState('userActionSaveEnv/envList', [
      'visibleCols',
      'currentPage',
      'terms'
    ]),
    description() {
      return `确认删除${this.deleteNameEn}环境吗?删除后不可恢复`;
    },
    updateFilterData() {
      let envData = this.envListData.filter(env => {
        return env.id !== this.selected[0].id;
      });
      return envData;
    }
  },
  methods: {
    ...mapActions('environmentForm', {
      getEnvList: 'getEnvList',
      deleteEnv: 'deleteEnv',
      updateEnvList: 'updateEnvList',
      addEnvList: 'addEnvList',
      queryAllLabels: 'queryAllLabels'
    }),
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent'
    }),
    ...mapMutations('userActionSaveEnv/envList', [
      'saveVisibleColumns',
      'saveCurrentPage',
      'saveTerms'
    ]),
    envFilter(rows, terms, cols, cellValue) {
      const lowerTerms = terms ? terms.toLowerCase().split(',') : [];
      return rows.filter(row => {
        return lowerTerms.every(term => {
          if (term.startsWith('__') || term === '') {
            return true;
          }
          let hasCol = cols.some(col => {
            return (
              (cellValue(col, row) + '').toLowerCase().indexOf(term.trim()) > -1
            );
          });
          return hasCol;
        });
      });
    },
    addNewValue() {
      if (this.$refs.terms.inputValue.length) {
        this.$refs.terms.add(this.$refs.terms.inputValue);
        this.$refs.terms.inputValue = '';
      }
    },
    addEnvTerm(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    // 显示输入验证码的对话框
    handleDeleteDialogOpened() {
      this.deleteNameEn = this.selected[0].name_en;
      this.deleteDialogOpened = true;
    },
    // 删除环境
    async handleDelete(inputVerifyCode) {
      await this.handleDeleteEnv(inputVerifyCode);
      this.deleteDialogOpened = false;
    },
    addTerm(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    async handleAddEnv() {
      this.$v.envVarModel.$touch();
      let envvarKeys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('envVarModel') > -1;
      });
      validate(envvarKeys.map(key => this.$refs[key]));
      if (this.$v.envVarModel.$invalid) {
        return;
      }
      this.handleEnvModalOpened('新增');
    },
    handleEnvAddModalOpened() {
      this.addEnvModalOpened = true;
    },
    //增删除操作前提示
    handleEnvModalOpened(todo) {
      let message = '';
      if (todo !== '新增') {
        message = '做此操作前请先核对影响范围。';
      }
      return this.$q
        .dialog({
          title: `${todo}环境`,
          message: `确定${todo}该环境吗？${message}`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.selectHandle(todo);
        });
    },
    async selectHandle(todo) {
      if (todo === '更新') {
        await this.handleUpdate();
      } else {
        await this.handleAdd();
      }
    },
    handleEnvUpdateModalOpened(row) {
      this.selected = [row];
      // let row = this.selected[0];
      this.updateModel = {
        ...row
      };
      this.$v.updateModel.$reset();
      this.updateEnvModalOpen = true;
    },
    async handleDeleteEnv(inputVerifyCode) {
      let params = {
        id: this.selected[0].id,
        verfityCode: inputVerifyCode
      };
      await this.deleteEnv(params);
      this.selected = [];
      successNotify('删除成功');
      this.getEnvList();
    },
    async handleUpdateEnv() {
      this.$v.updateModel.$touch();
      let envKeys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('updateModel') > -1;
      });
      validate(envKeys.map(key => this.$refs[key]));
      if (this.$v.updateModel.$invalid) {
        return;
      }
      this.handleEnvModalOpened('更新');
    },
    async handleUpdate() {
      this.updateModel.labels = this.updateModel.labels.map(item => {
        return item.value ? item.value : item;
      });
      await this.updateEnvList(this.updateModel);
      this.selected = [];
      successNotify('修改成功');
      this.updateEnvModalOpen = false;
      this.getEnvList();
    },
    async handleAdd() {
      this.envVarModel.labels = this.envVarModel.labels.map(item => {
        return item.value ? item.value : item;
      });
      await this.addEnvList(this.envVarModel);
      this.envVarModel = createEnvModel();
      successNotify('新增成功');
      this.addEnvModalOpened = false;
      this.getEnvList();
    },
    filterLabels(val) {
      if (val) {
        return val.join('，');
      }
    },
    async queryLabel(val, update, abort) {
      await this.queryAllLabels();
      update(() => {
        this.filterEnvLabels = this.envLablesOption.filter(label => {
          return label.value.toLowerCase().indexOf(val.toLowerCase()) > -1;
        });
        let currentLabels = {};
        if (this.updateEnvModalOpen) {
          currentLabels = this.updateModel.labels;
        } else {
          currentLabels = this.envVarModel.labels;
        }
        this.separateOption.forEach(list => {
          list.some(label => {
            return this.changeLabelsOption(currentLabels, label, list);
          });
        });
      });
    },
    // val 传入值，active 当前循环中的值，list 分组
    // 此方法用于在标签选择时，同一类别的标签不能重复选择
    changeLabelsOption(val, active, list) {
      if (!val) {
        return true;
      }
      val = val.map(item => {
        if (typeof item === 'string') {
          return {
            value: item,
            label: item
          };
        } else {
          return item;
        }
      });
      let labels = deepClone(this.filterEnvLabels);
      if (val.some(item => item.label === active)) {
        this.filterEnvLabels = labels.map(item => {
          if (item.label !== active && list.indexOf(item.label) > -1) {
            item.inactive = true;
          }
          return item;
        });
        return true;
      } else {
        this.filterEnvLabels = labels.map(item => {
          if (item.label !== active && list.indexOf(item.label) > -1) {
            item.inactive = false;
          }
          return item;
        });
      }
    },
    confirmToClose(key) {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this[key] = false;
        });
    }
  },
  async created() {
    this.loading = true;
    await this.getEnvList();
    await this.fetchCurrent();
    // 环境配置管理员才有权限增删改
    let managerUser = this.user.role.find(item => {
      return item.label === '环境配置管理员';
    });
    if (managerUser) {
      this.isManagerRole = true;
      this.singleOrNone = 'single';
    }
    this.loading = false;
  },
  mounted() {
    this.pagination = this.currentPage;
    if (!this.visibleCols.toString() || this.visibleCols.length < 2) {
      this.saveVisibleColumns([
        'env_name_cn',
        'env_name_en',
        'env_labels',
        'env_message',
        'btn'
      ]);
    }
    this.singleOrNone = this.visibleCols.length === 0 ? 'none' : 'single';
  }
};
</script>

<style lang="stylus" scoped>
.container
  position relative
  width 100%
  margin-right 4px
.input
  padding-bottom 20px
.td_desc
	box-sizing border-box
	width 35%
	max-width 130px
	overflow hidden
	text-overflow ellipsis
.select-bottom
  margin-bottom 20px
</style>
