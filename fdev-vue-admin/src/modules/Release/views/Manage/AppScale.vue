<template>
  <Loading :visible="loading">
    <fdev-table
      :data="tableData"
      :columns="tableColumns"
      row-key="id"
      :pagination.sync="pagination"
      title="弹性扩展列表"
      titleIcon="list_s_f"
      no-select-cols
      class="my-sticky-column-table"
    >
      <template v-slot:top-right>
        <span>
          <fdev-btn
            normal
            label="新增弹性扩展"
            ficon="add"
            :disable="
              !compareTime || !(changesDetail.can_operation || isKaDianManager)
            "
            @click="handleUpdateUserModalOpen('addAppScale')"
          />
          <fdev-tooltip
            v-if="
              !compareTime || !(changesDetail.can_operation || isKaDianManager)
            "
          >
            <span v-if="!compareTime">
              当前变更已过期
            </span>
            <span
              v-if="
                compareTime && !(changesDetail.can_operation || isKaDianManager)
              "
            >
              请联系当前变更所属组成员的第三层级组及其子组的投产管理员
            </span>
          </fdev-tooltip>
        </span>
      </template>
      <template v-slot:body="props">
        <fdev-tr :props="props">
          <fdev-td class="text-ellipsis" :title="props.row.application_name_cn">
            <router-link
              :to="{ path: `/app/list/${props.row.application_id}` }"
              class="link"
            >
              <span>{{ props.row.application_name_cn }}</span>
            </router-link>
          </fdev-td>
          <fdev-td class="text-ellipsis" :title="props.row.application_name_en">
            <router-link
              :to="{ path: `/app/list/${props.row.application_id}` }"
              class="link"
            >
              <span>{{ props.row.application_name_en }}</span>
            </router-link>
          </fdev-td>
          <fdev-td
            class="text-ellipsis"
            :title="
              props.row.deploy_type.length > 0
                ? props.row.deploy_type.join(',')
                : '-'
            "
          >
            <span v-if="props.row.deploy_type.includes('CAAS')">CAAS</span>
            <span v-if="props.row.deploy_type.length > 1">/</span>
            <span v-if="props.row.deploy_type.includes('SCC')">SCC</span>
          </fdev-td>
          <fdev-td v-for="env in filteredAutomationEnv" :key="env.id">
            <span v-if="props.row.deploy_type.includes('CAAS')">{{
              getEnvScales(props.row.env_scales, env.env_name)
            }}</span>
            <span v-if="props.row.deploy_type.length > 1">/</span>
            <span v-if="props.row.deploy_type.includes('SCC')">{{
              getSccEnvScales(props.row.env_scales, env.env_name)
            }}</span>
          </fdev-td>
          <fdev-td auto-width>
            <div class="q-gutter-md">
              <span>
                <fdev-btn
                  label="修改"
                  flat
                  :disable="
                    !compareTime ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                  @click="
                    handleUpdateUserModalOpen('updateAppScale', props.row)
                  "
                />
                <fdev-tooltip
                  v-if="
                    !compareTime ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <span v-if="!compareTime">
                    当前变更已过期
                  </span>
                  <span
                    v-if="
                      compareTime &&
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                </fdev-tooltip>
              </span>
              <span>
                <fdev-btn
                  label="删除"
                  :disable="
                    !compareTime ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                  @click="deleteElasticExpan(props.row)"
                  flat
                />
                <fdev-tooltip
                  v-if="
                    !compareTime ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <span v-if="!compareTime">
                    当前变更已过期
                  </span>
                  <span
                    v-if="
                      compareTime &&
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                </fdev-tooltip>
              </span>
            </div>
          </fdev-td>
        </fdev-tr>
      </template>
    </fdev-table>

    <f-dialog
      right
      v-model="appScaleModalOpened"
      persistent
      :title="`${this.handle === 'addAppScale' ? '新增' : '修改'}弹性扩展`"
    >
      <f-formitem label="应用名称" diaS>
        <fdev-select
          input-debounce="0"
          :disable="disable"
          use-input
          ref="appScaleModal.application"
          @input="uniqueList"
          option-label="name_zh"
          :options="appOptions"
          @filter="appFilter"
          v-model="$v.appScaleModal.application.$model"
          :rules="[() => $v.appScaleModal.application.required || '请选择应用']"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.name_zh">{{
                  scope.opt.name_zh
                }}</fdev-item-label>
                <fdev-item-label caption :title="scope.opt.name_en">
                  {{ scope.opt.name_en }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>

      <!-- CAAS部署平台 -->
      <div>
        <f-formitem class="q-mb-md" diaS label="CAAS部署平台">
          <fdev-toggle
            v-model="deployPlatform"
            val="CAAS"
            :disable="
              !appScaleModal.application || !prodOnCaas || !hasCaasDeployInfo
            "
          >
            <fdev-tooltip
              v-if="
                !appScaleModal.application || !prodOnCaas || !hasCaasDeployInfo
              "
            >
              <span v-if="!appScaleModal.application">
                请选择应用
              </span>
              <span v-if="appScaleModal.application && !prodOnCaas">
                该应用未曾在CAAS平台投产
              </span>
              <span
                v-if="
                  appScaleModal.application && prodOnCaas && !hasCaasDeployInfo
                "
              >
                该应用没有CAAS平台部署信息
              </span>
            </fdev-tooltip>
          </fdev-toggle>
        </f-formitem>

        <div v-if="deployPlatform.includes('CAAS')">
          <f-formitem
            label-class="q-pl-md"
            :label="env.name.$model"
            v-for="(env, i) in $v.appScaleModal.env_scales.$each.$iter"
            :key="i"
            diaS
          >
            <fdev-input
              :key="i"
              input-debounce="0"
              :ref="`appScaleModal.caas.${i}`"
              v-model="env.replicas.$model"
              @input="
                inputEnvReplicas(env.name.$model, env.replicas.$model, 'CAAS')
              "
              :rules="[
                () => env.replicas.required || `请输入${env.name.$model}数量`,
                () => env.replicas.maxlength || `只能输入0-99`
              ]"
            />
          </f-formitem>
        </div>
      </div>

      <!-- SCC部署平台 -->
      <div>
        <f-formitem class="q-mb-md" diaS label="SCC部署平台">
          <fdev-toggle v-model="deployPlatform" val="SCC" disable>
            <fdev-tooltip
              v-if="
                !isSccNewExcelTemp ||
                  !appScaleModal.application ||
                  isFeApp ||
                  !prodOnScc ||
                  !hasSccDeployInfo
              "
            >
              <span v-if="!isSccNewExcelTemp">
                新建变更时未选择SCC新版excel模板
              </span>
              <span v-if="isSccNewExcelTemp && !appScaleModal.application">
                请选择应用
              </span>
              <span
                v-if="isSccNewExcelTemp && appScaleModal.application && isFeApp"
              >
                前端应用不能部署SCC平台
              </span>
              <span
                v-if="
                  isSccNewExcelTemp &&
                    appScaleModal.application &&
                    !isFeApp &&
                    !prodOnScc
                "
              >
                该应用未曾在SCC平台投产
              </span>
              <span
                v-if="
                  isSccNewExcelTemp &&
                    appScaleModal.application &&
                    !isFeApp &&
                    prodOnScc &&
                    !hasSccDeployInfo
                "
              >
                该应用没有SCC平台部署信息
              </span>
            </fdev-tooltip>
            <fdev-tooltip v-else
              >SCC平台副本数如需调整，请通过yaml方式变更</fdev-tooltip
            >
          </fdev-toggle>
        </f-formitem>

        <div v-if="deployPlatform.includes('SCC')">
          <f-formitem
            label-class="q-pl-md"
            :label="env.name.$model"
            v-for="(env, i) in $v.appScaleModal.env_scales.$each.$iter"
            :key="i"
            diaS
          >
            <fdev-input
              :key="i"
              input-debounce="0"
              :ref="`appScaleModal.scc.${i}`"
              v-model="env.scc_replicas.$model"
              @input="
                inputEnvReplicas(
                  env.name.$model,
                  env.scc_replicas.$model,
                  'SCC'
                )
              "
              :rules="[
                () =>
                  env.scc_replicas.required || `请输入${env.name.$model}数量`,
                () => env.scc_replicas.maxlength || `只能输入0-99`
              ]"
            />
          </f-formitem>
        </div>
      </div>

      <template v-slot:btnSlot>
        <fdev-btn
          :loading="globalLoading[`releaseForm/${handle}`]"
          dialog
          label="确认"
          @click="handleAppScaleModal"
        />
      </template>
    </f-dialog>
  </Loading>
</template>

<script>
import { mapState, mapActions, mapGetters } from 'vuex';
import Loading from '@/components/Loading';
import { required } from 'vuelidate/lib/validators';
import {
  validate,
  successNotify,
  errorNotify,
  deepClone,
  isValidReleaseDate
} from '@/utils/utils';

export default {
  name: 'AppScale',
  components: { Loading },
  data() {
    return {
      loading: false,
      tableColumns: [], // 列表表头列
      tableData: [], // 列表数据
      pagination: {
        rowsPerPage: 0
      },
      envList: [], // 环境列表
      appScaleModalOpened: false, // 新增/修改弹性扩展弹窗是否开启
      appScaleModal: [],
      appOptions: [],
      prodSequenceList: [],
      type: '',
      handle: 'addAppScale',
      disable: false,
      deployPlatform: [],
      envScales: [],
      filteredAutomationEnv: []
    };
  },
  validations: {
    appScaleModal: {
      application: {
        required
      },
      env_scales: {
        $each: {
          replicas: {
            required,
            maxlength(val) {
              const reg = /^[0-9]$|^[1-9][0-9]$/;
              return reg.test(val);
            }
          },
          scc_replicas: {
            required,
            maxlength(val) {
              const reg = /^[0-9]$|^[1-9][0-9]$/;
              return reg.test(val);
            }
          },
          name: {}
        }
      }
    }
  },
  watch: {
    appScaleModalOpened(val) {
      if (val === false) {
        this.appScaleModal = this.getAppScaleModal();
        this.deployPlatform = [];
      }
    },
    'changesDetail.image_deliver_type': {
      handler(val) {
        this.init();
      },
      deep: true
    }
  },
  filters: {
    handleFilter(val) {
      return val === 'updateAppScale' ? '修改' : '新增';
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('releaseForm', [
      'appScaleList',
      'automationEnv',
      'changesDetail',
      'deployTypeByAppId',
      'appDeployPlatformInfo'
    ]),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),
    ...mapState('appForm', ['vueAppData']),
    compareTime() {
      return isValidReleaseDate(this.$q.sessionStorage.getItem('changeTime'));
    },
    columns() {
      const env = this.filteredAutomationEnv.map(env => {
        const envName = env.env_name.split('/').reverse()[0];
        return {
          label: envName,
          name: envName,
          field: env.id
        };
      });
      return [
        {
          name: 'application_name_cn',
          label: '应用中文名',
          field: 'application_name_cn'
        },
        {
          name: 'application_name_en',
          label: '应用英文名',
          field: 'application_name_en',
          sortable: true
        },
        {
          name: 'deploy_type',
          label: '部署平台',
          field: 'deploy_type'
        },
        ...env,
        {
          name: 'btn',
          label: '操作',
          field: 'btn'
        }
      ];
    },
    // 判断当前变更是否选择SCC新版excel模板
    isSccNewExcelTemp() {
      return this.changesDetail.scc_prod === '1';
    },
    // 判断选择的应用是否在CAAS平台投产
    prodOnCaas() {
      return (
        this.deployTypeByAppId.deploy_type &&
        this.deployTypeByAppId.deploy_type.includes('CAAS')
      );
    },
    // 判断选择的应用是否在SCC平台投产
    prodOnScc() {
      return (
        this.deployTypeByAppId.deploy_type &&
        this.deployTypeByAppId.deploy_type.includes('SCC')
      );
    },
    // 判断选择的应用是否在CAAS平台有部署信息
    hasCaasDeployInfo() {
      return (
        this.appDeployPlatformInfo &&
        this.appDeployPlatformInfo.caas_status === '1'
      );
    },
    // 判断选择的应用是否在SCC平台有部署信息
    hasSccDeployInfo() {
      return (
        this.appDeployPlatformInfo &&
        this.appDeployPlatformInfo.scc_status === '1'
      );
    },
    // 应用英文名是否是以mspmk-cli开头的前端应用
    isFeApp() {
      return (
        this.appScaleModal.application &&
        this.appScaleModal.application.name_en.startsWith('mspmk-cli')
      );
    }
  },
  methods: {
    ...mapActions('releaseForm', [
      'queryAppScale',
      'addAppScale',
      'deleteAppScale',
      'updateAppScale',
      'queryAutomationEnv',
      'queryDeployTypeByAppId',
      'queryAppDeployPlatformInfo'
    ]),
    ...mapActions('appForm', ['queryApps']),

    // 初始化列表
    async init() {
      this.loading = true;
      // this.type = this.changesDetail.image_deliver_type === '0' ? 'scales' : '';
      /* 不区分image_deliver_type */
      await this.queryAutomationEnv({
        type: ''
      });
      await this.queryAppScale({
        prod_id: this.prod_id
      });
      this.tableData = this.appScaleList;
      this.filterAutomationEnv();
      this.tableColumns = this.columns;
      this.loading = false;
    },

    // 获取当前所选应用的批次
    async uniqueList(prod) {
      const list = [];
      this.appScaleList.forEach(v => {
        if (v.application_id === prod.id) {
          list.push(v.prod_sequence);
        }
      });
      this.prodSequenceList = list;
      if (this.appScaleModal.prod_sequence) {
        this.$v.appScaleModal.prod_sequence.$touch();
        validate([this.$refs['appScaleModal.prod_sequence']]);
      }
      const prodId = this.$route.params.id;
      // 根据应用id查询该应用在CAAS和SCC部署平台的投产情况
      await this.queryDeployTypeByAppId({
        application_id: prod.id,
        prod_id: prodId
      });
      // 根据应用id查询该应用在CAAS和SCC部署平台的部署信息
      await this.queryAppDeployPlatformInfo({ id: prod.id });
      let tempDeployPlatform = [];
      if (
        this.deployTypeByAppId.deploy_type.includes('CAAS') &&
        this.appDeployPlatformInfo.caas_status === '1'
      ) {
        tempDeployPlatform.push('CAAS');
      }
      //因scc平台副本数不支持在弹性拓展里修改 故隐藏该段代码
      // if (
      //   this.deployTypeByAppId.deploy_type.includes('SCC') &&
      //   this.appDeployPlatformInfo.scc_status === '1' &&
      //   !prod.name_en.startsWith('mspmk-cli')
      // ) {
      //   tempDeployPlatform.push('SCC');
      // }
      this.deployPlatform = tempDeployPlatform;
      if (this.deployTypeByAppId.deploy_type.length === 0) {
        errorNotify('该应用未曾投产');
      }
    },

    // 点击新增/修改弹性扩展弹窗按钮，校验提交表单
    async handleAppScaleModal() {
      // 校验
      let keys = [];
      if (this.deployPlatform.includes('CAAS')) {
        let caasKeys = Object.keys(this.$refs).filter(key => {
          return this.$refs[key] && key.startsWith('appScaleModal.caas');
        });
        keys = keys.concat(caasKeys);
      }
      if (this.deployPlatform.includes('SCC')) {
        let sccKeys = Object.keys(this.$refs).filter(key => {
          return this.$refs[key] && key.startsWith('appScaleModal.scc');
        });
        keys = keys.concat(sccKeys);
      }
      Promise.all(
        keys.map(ele => this.$refs[ele][0].validate() || Promise.reject(ele))
      ).then(
        async res => {
          const applicationId = this.appScaleModal.application.id;
          const envScales = deepClone(this.appScaleModal.env_scales);
          if (this.deployPlatform.length > 0) {
            envScales.forEach(item => {
              if (!this.deployPlatform.includes('CAAS')) {
                item.replicas = '';
              }
              if (!this.deployPlatform.includes('SCC')) {
                item.scc_replicas = '';
              }
            });
            await this[this.handle]({
              application_id: applicationId,
              env_scales: envScales,
              prod_id: this.prod_id,
              deploy_type: this.deployPlatform
            });
            successNotify(
              `${this.handle === 'addAppScale' ? '新增' : '修改'}成功！`
            );
            this.appScaleModalOpened = false;
            this.init();
          } else {
            errorNotify('请至少打开一个部署平台');
          }
        },
        rej => this.$refs[rej][0].focus()
      );
    },

    // 删除弹性扩展列表的某一条数据
    async deleteElasticExpan(data) {
      this.$q
        .dialog({
          title: '确认删除',
          message: '确定删除吗?',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteAppScale({
            ...data,
            prod_id: this.prod_id
          });
          successNotify('删除成功！');
          this.init();
        });
    },

    // 打开弹窗新增/修改弹性扩展
    async handleUpdateUserModalOpen(handle, data = this.getAppScaleModal()) {
      this.appOptions = this.vueAppData;
      this.handle = handle;
      this.appScaleModal = deepClone(data);
      this.envScales = deepClone(data.env_scales);
      this.disable = false;
      if (handle !== 'addAppScale') {
        this.disable = true;
        this.appScaleModal.application = {
          name_zh: data.application_name_cn,
          name_en: data.application_name_en,
          id: data.application_id
        };
        await this.queryDeployTypeByAppId({
          application_id: data.application_id,
          prod_id: this.$route.params.id
        });
        await this.queryAppDeployPlatformInfo({ id: data.application_id });
        this.deployPlatform = data.deploy_type;
      }
      this.appScaleModalOpened = true;
    },

    // 过滤应用的下拉列表
    appFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase().trim();
        this.appOptions = this.vueAppData.filter(
          v =>
            v.name_zh.toLowerCase().indexOf(needle) > -1 ||
            v.name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    },

    // 获取环境，转换成表单格式
    getAppScaleModal() {
      const ednvForm = this.automationEnv.map(env => {
        return {
          name: env.env_name.split('/').reverse()[0],
          env_name: env.env_name,
          description: env.description,
          replicas: '',
          scc_replicas: ''
        };
      });
      return {
        prod_id: '',
        application: null,
        prod_sequence: '',
        env_scales: ednvForm
      };
    },
    getEnvScales(itemEnv, envName) {
      const env = itemEnv.find(env => env.env_name === envName);
      return env ? env.replicas : '0';
    },
    getSccEnvScales(itemEnv, envName) {
      const env = itemEnv.find(env => env.env_name === envName);
      return env ? env.scc_replicas : '0';
    },
    confirmToClose(e) {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this.appScaleModalOpened = false;
        });
    },
    filterAutomationEnv() {
      // 如果未选择SCC新版excel模板，去除scc部署平台的automationEnv
      if (this.changesDetail.scc_prod !== '1') {
        this.filteredAutomationEnv = this.automationEnv.filter(
          item => `${item.platform}` === 'CAAS' || item.platform.length === 2
        );
      } else {
        this.filteredAutomationEnv = deepClone(this.automationEnv);
      }
    },
    inputEnvReplicas(env, envReplicas, platform) {
      this.envScales.find(item => {
        if (item.name === env) {
          if (platform === 'CAAS') {
            item.replicas = envReplicas;
          } else if (platform === 'SCC') {
            item.scc_replicas = envReplicas;
          }
        }
      });
    }
  },
  created() {
    this.prod_id = this.$route.params.id;
    this.queryApps();
    if (this.changesDetail.image_deliver_type) {
      this.init();
    }
  }
};
</script>

<style lang="stylus" scoped></style>
