<template>
  <div>
    <f-dialog
      :value="value"
      title="添加变更应用"
      @input="$emit('input', $event)"
      @hide="deployPlatform = []"
    >
      <f-formitem label="变更类型" diaS v-if="image_deliver_type === '1'">
        <fdev-select
          input-debounce="0"
          ref="releaseType"
          option-label="label"
          option-value="value"
          @input="changeReleaseType(releaseType)"
          map-options
          emit-value
          :options="typeOptions"
          v-model="$v.releaseType.$model"
          :rules="[() => $v.releaseType.required || '请选择变更类型']"
        />
      </f-formitem>
      <f-formitem label="选择应用" diaS v-if="options.length > 0">
        <fdev-select
          input-debounce="0"
          ref="application"
          option-label="name_zh"
          option-value="id"
          :options="appOptions"
          use-input
          v-model="$v.application.$model"
          @filter="appFilter"
          @input="getDeployType"
          :rules="[() => $v.application.required || '请选择应用']"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.name_zh">
                  {{ scope.opt.name_zh }}
                </fdev-item-label>
                <fdev-item-label :title="scope.opt.name_en" caption>
                  {{ scope.opt.name_en }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <span v-else class="text-grey-7">
        暂无可添加的变更应用！
      </span>
      <f-formitem label="部署平台" diaS v-if="options.length > 0">
        <div class="row">
          <fdev-checkbox
            v-model="deployPlatform"
            class="q-mr-md"
            val="CAAS"
            label="CAAS"
            :disable="
              !application ||
                (platformDisable &&
                  (!hasCaasDeployInfo || hasCaasDeployaviable))
            "
          >
            <fdev-tooltip
              v-if="
                platformDisable && (!hasCaasDeployInfo || hasCaasDeployaviable)
              "
            >
              <span v-if="platformDisable && !hasCaasDeployInfo">
                该应用没有CAAS平台部署信息
              </span>
              <span
                v-if="
                  platformDisable && hasCaasDeployInfo && hasCaasDeployaviable
                "
              >
                该平台未投过产
              </span>
            </fdev-tooltip>
          </fdev-checkbox>
          <fdev-checkbox
            v-model="deployPlatform"
            val="SCC"
            label="SCC"
            :disable="
              !application ||
                (platformDisable &&
                  (!isSccExcelTemp ||
                    !hasSccDeployInfo ||
                    isFeApp ||
                    hasSccDeployaviable))
            "
          >
            <fdev-tooltip
              v-if="
                platformDisable &&
                  (!isSccExcelTemp ||
                    !hasSccDeployInfo ||
                    isFeApp ||
                    hasSccDeployaviable)
              "
            >
              <span v-if="platformDisable && !isSccExcelTemp">
                新建变更时未选择SCC新版excel模板
              </span>
              <span
                v-if="platformDisable && isSccExcelTemp && hasSccDeployaviable"
              >
                该平台未投过产
              </span>
              <span
                v-if="platformDisable && isSccExcelTemp && !hasSccDeployInfo"
              >
                该应用没有SCC平台部署信息
              </span>
              <span
                v-if="
                  platformDisable &&
                    isSccExcelTemp &&
                    hasSccDeployInfo &&
                    isFeApp
                "
              >
                前端应用不能部署SCC平台
              </span>
            </fdev-tooltip>
          </fdev-checkbox>
        </div>
      </f-formitem>

      <f-formitem
        label="CASS停止的集群"
        diaS
        v-if="isCAAS && releaseType === '2'"
      >
        <div class="row">
          <fdev-checkbox
            class="q-mr-md"
            v-model="CAASlist"
            val="SHK1"
            label="SHK1"
          >
          </fdev-checkbox>
          <fdev-checkbox
            v-if="application_type !== 'gray'"
            v-model="CAASlist"
            val="SHK2"
            label="SHK2"
            class="q-mr-md"
          >
          </fdev-checkbox>
          <fdev-checkbox
            class="q-mr-md"
            v-model="CAASlist"
            val="HFK1"
            label="HFK1"
          >
          </fdev-checkbox>
          <fdev-checkbox
            v-if="application_type !== 'gray'"
            v-model="CAASlist"
            val="HFK2"
            label="HFK2"
          >
          </fdev-checkbox>
        </div>
      </f-formitem>

      <f-formitem
        label="SCC停止的集群"
        diaS
        v-if="isSCC && releaseType === '2'"
      >
        <div class="row">
          <fdev-checkbox
            class="q-mr-md"
            v-model="SCClist"
            val="SHK1"
            label="SHK1"
          >
          </fdev-checkbox>
          <fdev-checkbox
            v-if="application_type !== 'gray'"
            v-model="SCClist"
            val="SHK2"
            label="SHK2"
            class="q-mr-md"
          >
          </fdev-checkbox>
          <fdev-checkbox
            class="q-mr-md"
            v-model="SCClist"
            val="HFK1"
            label="HFK1"
          >
          </fdev-checkbox>
          <fdev-checkbox
            v-if="application_type !== 'gray'"
            v-model="SCClist"
            val="HFK2"
            label="HFK2"
          >
          </fdev-checkbox>
        </div>
      </f-formitem>

      <template v-slot:btnSlot>
        <span>
          <fdev-btn
            dialog
            @click="handleUpdate"
            label="确认"
            :disable="!application || deployPlatform.length === 0"
            :loading="
              globalLoading['releaseForm/addChangeApplication'] ||
                globalLoading['releaseForm/queryReplicasnu']
            "
          />
          <fdev-tooltip v-if="!application || deployPlatform.length === 0">
            <span v-if="!application">
              请选择应用
            </span>
            <span v-if="application && deployPlatform.length === 0">
              请选择部署平台
            </span>
          </fdev-tooltip>
        </span>
      </template>
    </f-dialog>

    <UpdateReplicas
      v-model="replicasDialog"
      :replicasObj="replicasObj"
      :deployPlatform="deployPlatform"
      @confirm="addApp"
    />
  </div>
</template>

<script>
import { validate, errorNotify, deepClone } from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import { mapActions, mapState } from 'vuex';
import { typeOptions } from '../../../utils/model';
import UpdateReplicas from './updateReplicas';

export default {
  name: 'NewChanges',
  components: { UpdateReplicas },
  data() {
    return {
      openDialog: false, // 打开弹窗
      application: null,
      appOptions: [],
      typeOptions: typeOptions,
      releaseType: '',
      deployPlatform: [],
      replicasDialog: false, // 修改副本数弹窗开关
      replicasObj: {}, // 各发布环境对应的副本数
      queryReplicasnuCode: '',
      platformDisable: false,
      CAASlist: [], //caas停止集群集合
      SCClist: [] //scc停止集群集合
    };
  },
  props: {
    image_deliver_type: String,
    value: {
      type: Boolean,
      default: false
    },
    release_node_name: {
      type: String
    },
    prod_assets_version: {
      type: String
    },
    application_type: {
      type: String
    }
  },
  validations: {
    application: {
      required
    },
    releaseType: {
      required
    }
  },
  computed: {
    ...mapState('appForm', ['vueAppData']),
    ...mapState('releaseForm', [
      'appWithOutSum',
      'changesDetail',
      'changeApllication',
      'deployTypeByAppId',
      'replicasNum',
      'appDeployPlatformInfo'
    ]),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    options() {
      let appList = [];
      if (
        this.releaseType !== '3' &&
        this.releaseType !== '4' &&
        this.releaseType !== '2'
      ) {
        appList = this.appWithOutSum;
      } else {
        appList = this.vueAppData;
      }
      return appList.filter(item => {
        return !this.changeApllication.some(app => {
          return app.application_id === item.id;
        });
      });
    },

    // 新建变更时是否选择SCC新版excel模板
    isSccExcelTemp() {
      return this.changesDetail && this.changesDetail.scc_prod === '1';
    },

    hasCaasDeployaviable() {
      return (
        !this.deployTypeByAppId.deploy_type.includes('CAAS') &&
        (this.releaseType === '2' ||
          this.releaseType === '3' ||
          this.releaseType === '4')
      );
    },
    hasSccDeployaviable() {
      return (
        !this.deployTypeByAppId.deploy_type.includes('SCC') &&
        (this.releaseType === '2' ||
          this.releaseType === '3' ||
          this.releaseType === '4')
      );
    },
    // 应用是否有CAAS平台的部署信息
    hasCaasDeployInfo() {
      return (
        this.appDeployPlatformInfo &&
        this.appDeployPlatformInfo.caas_status === '1'
      );
    },

    // 应用是否有SCC平台的部署信息
    hasSccDeployInfo() {
      return (
        this.appDeployPlatformInfo &&
        this.appDeployPlatformInfo.scc_status === '1'
      );
    },

    // 应用英文名是否是以mspmk-cli开头的前端应用
    isFeApp() {
      return (
        this.application && this.application.name_en.startsWith('mspmk-cli')
      );
    },
    //是否选择的CASS平台
    isCAAS() {
      return this.deployPlatform.includes('CAAS');
    },
    //是否选择的SCC平台
    isSCC() {
      return this.deployPlatform.includes('SCC');
    }
  },
  watch: {
    async value(val) {
      if (val === false) {
        this.application = null;
        this.releaseType = '';
        this.queryReplicasnuCode = '';
        this.platformDisable = false;
      } else {
        if (this.image_deliver_type === '0') {
          this.releaseType = '1';
        } else {
          this.queryApps();
        }
        await this.queryAppWithOutSum({
          prod_assets_version: this.prod_assets_version,
          release_node_name: this.release_node_name
        });
      }
    },
    releaseType(val, oldVal) {
      this.deployPlatform = [];
      this.application = null;
      this.SCClist = [];
      this.CAASlist = [];
    },
    deployPlatform(val) {
      if (!val.includes('CAAS')) this.CAASlist = [];
      if (!val.includes('SCC')) this.SCClist = [];
    }
  },
  methods: {
    ...mapActions('appForm', ['queryApps']),
    ...mapActions('releaseForm', [
      'queryAppWithOutSum',
      'queryDeployTypeByAppId',
      'queryReplicasnu',
      'updateReplicasnu',
      'queryAppDeployPlatformInfo'
    ]),

    // 点击添加变更应用弹窗的确定按钮
    async handleUpdate() {
      //非自动化不校验变更类型
      if (this.image_deliver_type === '1') {
        this.$v.releaseType.$touch();
        validate([this.$refs.releaseType]);
        if (this.$v.releaseType.$invalid) {
          return;
        }
      }

      this.$v.application.$touch();
      validate([this.$refs.application]);
      if (this.$v.application.$invalid) {
        return;
      }
      if (
        this.application.fdev_config_changed === true &&
        this.application.fdev_config_confirm !== '1'
      ) {
        errorNotify(
          `请应用${
            this.application.app_name_en
          }的应用负责人完成配置文件更新审核`
        );
        return;
      }
      if (this.queryReplicasnuCode !== 'error') {
        // 当变更类型为“停止应用后重启”发接口获取副本数并打开弹窗
        if (this.releaseType === '3') {
          const prodId = this.$route.params.id;
          try {
            await this.queryReplicasnu({
              prod_id: prodId,
              application_id: this.application.id,
              release_type: this.releaseType,
              deploy_type: this.deployPlatform
            });
            this.replicasObj = deepClone(this.replicasNum.change);
            this.replicasDialog = true;
          } catch (e) {
            this.queryReplicasnuCode = 'error';
          }
        } else {
          // 发接口添加变更应用
          this.$emit('confirm', {
            application_id: this.application.id,
            ...this.application,
            release_type: this.releaseType,
            deploy_type: this.deployPlatform,
            caas_stop_env: this.releaseType === '2' ? this.CAASlist : [],
            scc_stop_env: this.releaseType === '2' ? this.SCClist : []
          });
        }
      }
    },

    // 输入过滤选择应用的下拉选项
    appFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase().trim();
        this.appOptions = this.options.filter(
          v =>
            v.name_zh.toLowerCase().indexOf(needle) > -1 ||
            v.name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    },

    // 选择应用后，获取该应用曾部署过的平台
    async getDeployType(app) {
      this.queryReplicasnuCode = '';
      const prodId = this.$route.params.id;
      if (app) {
        this.deployPlatform = [];
        this.SCClist = [];
        this.CAASlist = [];
        // 发接口查询应用在CAAS和SCC平台的投产情况
        await this.queryDeployTypeByAppId({
          application_id: app.id,
          prod_id: prodId
        });

        // 发接口查询应用在CAAS和SCC平台的部署情况（应用详情-部署信息）
        await this.queryAppDeployPlatformInfo({ id: app.id });
        this.platformDisable = true;
        if (
          // 应用有在CAAS平台投产过且在CAAS平台有部署信息
          this.deployTypeByAppId.deploy_type.includes('CAAS') &&
          this.appDeployPlatformInfo.caas_status === '1'
        ) {
          this.deployPlatform.push('CAAS');
        }
        if (
          // 应用有在SCC平台投产过且在SCC平台有部署信息
          // 且已选应用英文名不是以mspmk-cli开头的前端应用
          this.deployTypeByAppId.deploy_type.includes('SCC') &&
          this.appDeployPlatformInfo.scc_status === '1' &&
          !app.name_en.startsWith('mspmk-cli')
        ) {
          this.deployPlatform.push('SCC');
        }
      }
    },

    // 修改变更类型
    async changeReleaseType(releaseType) {
      this.queryReplicasnuCode = '';
    },

    // 点击“确认副本数”弹窗的“确认”按钮，添加变更应用
    async addApp(replicasObj) {
      this.replicasObj = deepClone(replicasObj);
      // 发接口添加变更应用
      await this.$emit('confirm', {
        application_id: this.application.id,
        ...this.application,
        release_type: this.releaseType,
        deploy_type: this.deployPlatform,
        change: this.replicasObj
      });
      this.replicasDialog = false;
    }
  }
};
</script>

<style lang="stylus" scoped></style>
