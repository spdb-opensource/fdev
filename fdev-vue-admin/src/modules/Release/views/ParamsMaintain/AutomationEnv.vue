<template>
  <Loading :visible="loading">
    <fdev-table
      :data="tableData"
      :columns="columnsDisplay"
      row-key="id"
      title="自动化发布环境列表"
      titleIcon="list_s_f"
      :visible-columns="visibleColumns"
      :pagination.sync="pagination"
      class="my-sticky-column-table"
    >
      <template v-slot:top-right>
        <span>
          <fdev-btn
            normal
            ficon="add"
            label="新增发布环境"
            :disable="!isManager"
            @click="handleDialogOpened('add')"
          />
          <fdev-tooltip v-if="!isManager">
            请联系投产管理员或卡点管理员
          </fdev-tooltip>
        </span>
      </template>

      <!-- 部署平台列 -->
      <template v-slot:body-cell-platform="props">
        <fdev-td>
          {{ props.row.platform.join('，') }}
        </fdev-td>
      </template>

      <template v-slot:body-cell-btn="props">
        <fdev-td auto-width>
          <div class="q-gutter-sm row items-center no-wrap">
            <fdev-btn
              label="修改"
              flat
              @click="handleDialogOpened('update', props.row)"
            />
            <fdev-btn label="删除" flat @click="deleteModule(props.row.id)" />
          </div>
        </fdev-td>
      </template>
    </fdev-table>

    <f-dialog
      right
      v-model="dialogOpened"
      :title="title"
      persistent
      @hide="closeDialog"
    >
      <!-- 自动化发布环境 -->
      <f-formitem diaS label="自动化发布环境">
        <fdev-input
          type="text"
          ref="automationEnvModel.env_name"
          v-model="$v.automationEnvModel.env_name.$model"
          :rules="[
            () =>
              $v.automationEnvModel.env_name.required || '请输入自动化发布环境'
          ]"
        />
      </f-formitem>

      <!-- 部署平台 -->
      <f-formitem required class="q-mb-md" diaS label="部署平台">
        <div class="row">
          <fdev-checkbox
            v-model="deployPlatform"
            class="q-mr-md"
            val="CAAS"
            label="CAAS"
          />
          <fdev-checkbox
            v-model="deployPlatform"
            class="q-mr-md"
            val="SCC"
            label="SCC"
          />
          <span class="text-red q-ml-md" v-if="deployPlatform.length === 0">
            请选择
          </span>
        </div>
      </f-formitem>

      <div>
        <div
          v-for="(item, index) in deployPlatform"
          :key="index"
          class="env-item"
        >
          <div class="text-grey-7">对应{{ item }}环境</div>
          <div class="q-gutter-md">
            <f-formitem
              :label="env.label"
              diaS
              v-for="(env, envIndex) in envType"
              :key="envIndex"
            >
              <fdev-select
                input-debounce="0"
                use-input
                option-label="name_cn"
                option-value="id"
                v-model="envObj[env.value + item]"
                :options="envObjOptions[env.value + item + 'Options']"
                @filter="envFilter"
                @focus="getCurEnv(env.value + item)"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.name_cn">
                        {{ scope.opt.name_cn }}
                      </fdev-item-label>
                      <fdev-item-label caption :title="scope.opt.name_en">
                        {{ scope.opt.name_en }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
          </div>
        </div>
      </div>
      <f-formitem required class="q-mt-md" label="描述" diaS>
        <fdev-input
          type="textarea"
          ref="automationEnvModel.description"
          v-model="$v.automationEnvModel.description.$model"
          :rules="[
            () => $v.automationEnvModel.description.required || '请输入描述'
          ]"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          label="提交"
          :loading="globalLoading[`releaseForm/${type}AutomationEnv`]"
          @click="submit"
        />
      </template>
    </f-dialog>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { required } from 'vuelidate/lib/validators';
import { mapActions, mapState, mapMutations } from 'vuex';
import { automationEnvModel } from '../../utils/model';
import { automationEnvListColumns, envTypeList } from '../../utils/constants';
import { validate, successNotify, errorNotify, deepClone } from '@/utils/utils';

export default {
  name: 'AutomationEnv',
  components: { Loading },
  data() {
    return {
      pagination: {
        rowsPerPage: 5
      },
      loading: false,
      dialogOpened: false,
      type: '',
      tableData: [],
      automationEnvModel: automationEnvModel(),
      curEnvOption: '',
      columns: automationEnvListColumns,
      envType: envTypeList,
      envObj: {
        proDmzCAAS: null, // CAAS生产网银网段
        proBizCAAS: null, // CAAS生产业务网段
        grayDmzCAAS: null, // CAAS灰度网银网段
        grayBizCAAS: null, // CAAS灰度业务网段
        proDmzSCC: null, // SCC生产网银网段
        proBizSCC: null, // SCC生产业务网段
        grayDmzSCC: null, // SCC灰度网银网段
        grayBizSCC: null // SCC灰度业务网段
      },
      envObjOptions: {
        proDmzCAASOptions: [], // CAAS生产网银网段下拉选项
        proBizCAASOptions: [], // CAAS生产业务网段下拉选项
        grayDmzCAASOptions: [], // CAAS灰度网银网段下拉选项
        grayBizCAASOptions: [], // CAAS灰度业务网段下拉选项
        proDmzSCCOptions: [], // SCC生产网银网段下拉选项
        proBizSCCOptions: [], // SCC生产业务网段下拉选项
        grayDmzSCCOptions: [], // SCC灰度网银网段下拉选项
        grayBizSCCOptions: [] // SCC灰度业务网段下拉选项
      },
      envFilterObjOptions: {},
      deployPlatform: [] // 部署平台
    };
  },
  validations: {
    automationEnvModel: {
      env_name: {
        required
      },
      description: {
        required
      }
    }
  },
  props: {
    isManager: Boolean
  },
  watch: {
    pagination(val) {
      this.updateCurrentPage({
        rowsPerPage: val.rowsPerPage
      });
    },
    dialogOpened(val) {
      if (val) {
        this.dialogInit();
      }
    }
  },
  computed: {
    ...mapState('userActionSaveRelease/paramsAutomationEnv', [
      'visibleColumns',
      'currentPage'
    ]),
    ...mapState('releaseForm', {
      list: 'automationEnv',
      envList: 'envList'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    title() {
      return this.type === 'add' ? '新增发布环境' : '修改发布环境';
    },
    columnsDisplay() {
      if (this.isManager) {
        return this.columns.concat({
          name: 'btn',
          field: 'btn',
          label: '操作'
        });
      } else {
        return this.columns;
      }
    }
  },
  methods: {
    ...mapMutations('userActionSaveRelease/paramsAutomationEnv', [
      'updateVisibleColumns',
      'updateCurrentPage'
    ]),
    ...mapActions('releaseForm', {
      query: 'queryAutomationEnv',
      deleted: 'deleteAutomationEnv',
      add: 'addAutomationEnv',
      update: 'updateAutomationEnv',
      getEnvList: 'queryEnvList'
    }),
    handleDialogOpened(type, data = automationEnvModel()) {
      this.dialogInit();
      this.automationEnvModel = deepClone(data);
      this.type = type;
      if (data.id) {
        // 修改，给各环境赋值
        const envArr = [
          'proDmzCAAS',
          'proBizCAAS',
          'grayDmzCAAS',
          'grayBizCAAS',
          'proDmzSCC',
          'proBizSCC',
          'grayDmzSCC',
          'grayBizSCC'
        ];
        envArr.forEach(item => {
          this.envObjOptions[item + 'Options'].forEach(env => {
            if (env.name_en === data[this.handleStr(item)]) {
              this.envObj[item] = env;
            }
          });
        });

        // 给部署平台赋值
        this.deployPlatform = data.platform;
      }
      this.dialogOpened = true;
    },
    async submit() {
      this.$v.automationEnvModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('automationEnvModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.automationEnvModel.$invalid) {
        return;
      }
      const commonParams = {
        id: this.automationEnvModel.id,
        env_name: this.automationEnvModel.env_name,
        description: this.automationEnvModel.description,
        platform: this.deployPlatform
      };
      let caasParams = {
        gray_dmz_caas: this.envObj.grayDmzCAAS
          ? this.envObj.grayDmzCAAS.name_en
          : '',
        gray_biz_caas: this.envObj.grayBizCAAS
          ? this.envObj.grayBizCAAS.name_en
          : '',
        pro_dmz_caas: this.envObj.proDmzCAAS
          ? this.envObj.proDmzCAAS.name_en
          : '',
        pro_biz_caas: this.envObj.proBizCAAS
          ? this.envObj.proBizCAAS.name_en
          : ''
      };
      if (!this.deployPlatform.includes('CAAS')) {
        caasParams = {
          gray_dmz_caas: '',
          gray_biz_caas: '',
          pro_dmz_caas: '',
          pro_biz_caas: ''
        };
      }
      let sccParams = {
        gray_dmz_scc: this.envObj.grayDmzSCC
          ? this.envObj.grayDmzSCC.name_en
          : '',
        gray_biz_scc: this.envObj.grayBizSCC
          ? this.envObj.grayBizSCC.name_en
          : '',
        pro_dmz_scc: this.envObj.proDmzSCC ? this.envObj.proDmzSCC.name_en : '',
        pro_biz_scc: this.envObj.proBizSCC ? this.envObj.proBizSCC.name_en : ''
      };
      if (!this.deployPlatform.includes('SCC')) {
        sccParams = {
          gray_dmz_scc: '',
          gray_biz_scc: '',
          pro_dmz_scc: '',
          pro_biz_scc: ''
        };
      }
      let params = {
        ...commonParams,
        ...caasParams,
        ...sccParams
      };
      const caasEnvArr = [
        'proDmzCAAS',
        'proBizCAAS',
        'grayDmzCAAS',
        'grayBizCAAS'
      ];
      const sccEnvArr = ['proDmzSCC', 'proBizSCC', 'grayDmzSCC', 'grayBizSCC'];
      const caasFlagArr = [];
      const sccFlagArr = [];
      for (let i = 0; i < caasEnvArr.length; i++) {
        this.envObj[caasEnvArr[i]]
          ? caasFlagArr.push(true)
          : caasFlagArr.push(false);
      }
      for (let i = 0; i < sccEnvArr.length; i++) {
        this.envObj[sccEnvArr[i]]
          ? sccFlagArr.push(true)
          : sccFlagArr.push(false);
      }
      if (
        (caasFlagArr.includes(true) && `${this.deployPlatform}` === 'CAAS') ||
        (sccFlagArr.includes(true) && `${this.deployPlatform}` === 'SCC') ||
        (caasFlagArr.includes(true) &&
          sccFlagArr.includes(true) &&
          this.deployPlatform.length === 2)
      ) {
        await this[this.type](params);
        successNotify(`${this.title}成功！`);
        this.dialogOpened = false;
        this.init();
      } else {
        if (
          !caasFlagArr.includes(true) &&
          this.deployPlatform.includes('CAAS')
        ) {
          errorNotify('请至少选择一项CAAS环境配置');
        }
        if (!sccFlagArr.includes(true) && this.deployPlatform.includes('SCC')) {
          errorNotify('请至少选择一项SCC环境配置');
        }
      }
    },
    deleteModule(id) {
      this.$q
        .dialog({
          title: '确认删除',
          message: '确认删除此自动化发布环境？',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleted({
            id: id
          });
          successNotify('删除成功！');
          this.init();
        });
    },
    // 关闭弹窗时，各环境值归空
    closeDialog() {
      this.envObj.proDmzCAAS = null;
      this.envObj.proBizCAAS = null;
      this.envObj.grayDmzCAAS = null;
      this.envObj.grayBizCAAS = null;
      this.envObj.proDmzSCC = null;
      this.envObj.proBizSCC = null;
      this.envObj.grayDmzSCC = null;
      this.envObj.grayBizSCC = null;
      this.deployPlatform = [];
    },
    async init() {
      this.loading = true;
      await this.query();
      this.tableData = this.list;
      this.loading = false;
    },
    // 弹窗开启时，给各环境下拉选项赋值
    dialogInit() {
      const envList = this.envList.slice();
      const envType = ['CAAS', 'SCC'];
      const publishType = ['pro', 'gray'];
      const type = ['Biz', 'Dmz'];
      // 给下拉选项赋值
      for (let i = 0; i < envType.length; i++) {
        for (let j = 0; j < type.length; j++) {
          for (let k = 0; k < publishType.length; k++) {
            this.envObjOptions[
              `${publishType[k]}${type[j]}${envType[i]}Options`
            ] = envList.filter(env =>
              this.isIncludeArr(env.labels, [
                // publishType[k].toLowerCase(),
                type[j].toLowerCase(),
                envType[i].toLowerCase()
              ])
            );
            this.envFilterObjOptions[
              `${publishType[k]}${type[j]}${envType[i]}Options`
            ] = this.envObjOptions[
              `${publishType[k]}${type[j]}${envType[i]}Options`
            ].slice();
          }
        }
      }
    },
    // 工具函数：判断一个数组是否完全包含另一个数组
    isIncludeArr(lArr, sArr) {
      let flag = [];
      for (let i = 0; i < sArr.length; i++) {
        lArr.includes(sArr[i]) ? flag.push(true) : flag.push(false);
      }
      return !flag.includes(false);
    },
    // 工具函数，处理字符串：如将‘proDmzCAAS’处理为‘pro_dmz_caas’
    handleStr(str) {
      const strArr = str.split('');
      let upperCaseCount = 0;
      let resultStr = '';
      for (let i = 0; i < strArr.length; i++) {
        if (/[A-Z]/.test(strArr[i])) {
          upperCaseCount++;
          if (upperCaseCount < 3) {
            strArr[i] = '_' + strArr[i].toLowerCase();
          } else {
            strArr[i] = strArr[i].toLowerCase();
          }
        }
        resultStr = resultStr + strArr[i];
      }
      return resultStr;
    },
    envFilter(val, update, abort) {
      const curEnv = this.curEnvOption;
      const envListTemp = this.envFilterObjOptions[curEnv + 'Options'];
      update(() => {
        this.envObjOptions[curEnv + 'Options'] = envListTemp.filter(
          v =>
            v.name_cn.toLowerCase().includes(val.toLowerCase()) ||
            v.name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    getCurEnv(str) {
      this.curEnvOption = str;
    }
  },
  async created() {
    this.pagination = this.currentPage;
    this.init();
    await this.getEnvList();
  }
};
</script>

<style lang="stylus" scoped>
.env-item:not(:first-child)
  margin-top 16px
</style>
