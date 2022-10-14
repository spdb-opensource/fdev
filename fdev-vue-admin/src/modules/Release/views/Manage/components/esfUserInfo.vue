<template>
  <div>
    <f-dialog
      :value="value"
      @input="$emit('input', $event)"
      right
      f-sc
      :title="type === 'add' ? '添加ESF用户信息' : '编辑ESF用户信息'"
    >
      <div class="container">
        <!-- 基础信息 -->
        <div class="card q-mb-md">
          <div class="bg-blue-1 full-width title-div q-mb-md row">
            <div class="title-icon row justify-center items-center">
              <f-icon name="version_s_f" class="text-blue-8" />
            </div>
            <div class="row justify-center items-center">
              基础信息
            </div>
          </div>
          <div class="justify-between">
            <!-- 应用名 -->
            <f-formitem label="应用名称" diaS required>
              <fdev-select
                ref="esfUserInfo.appName"
                use-input
                emit-value
                map-options
                option-label="name_zh"
                :options="appOptions"
                v-model="appName"
                :disable="type === 'edit'"
                @filter="appFilter"
                :rules="[val => !!val || '请选择应用']"
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

            <!-- 部署平台 -->
            <f-formitem label="部署平台" diaS required>
              <div class="row items-center">
                <span>
                  <fdev-checkbox
                    v-model="deployPlatform"
                    class="q-mr-md"
                    style="margin-left:-8px"
                    val="CAAS"
                    label="CAAS"
                    :disable="
                      platformDisable &&
                        (!hasCaasDeployInfo ||
                          appIsNotReleased ||
                          (isAddToCard && !checkedCaas) ||
                          (isAddToCard && checkedCaas && hasNoCaasImage))
                    "
                  />
                  <fdev-tooltip
                    v-if="
                      platformDisable &&
                        (!hasCaasDeployInfo ||
                          appIsNotReleased ||
                          (isAddToCard && !checkedCaas) ||
                          (isAddToCard && checkedCaas && hasNoCaasImage))
                    "
                  >
                    <span v-if="platformDisable && !hasCaasDeployInfo">
                      该应用没有CAAS平台部署信息
                    </span>
                    <span
                      v-if="
                        platformDisable && hasCaasDeployInfo && appIsNotReleased
                      "
                    >
                      该应用未曾投产，请先添加变更应用
                    </span>
                    <span
                      v-if="
                        platformDisable &&
                          hasCaasDeployInfo &&
                          !appIsNotReleased &&
                          (isAddToCard && !checkedCaas)
                      "
                    >
                      该应用在应用卡片未勾选CAAS部署平台
                    </span>
                    <span
                      v-if="
                        platformDisable &&
                          hasCaasDeployInfo &&
                          !appIsNotReleased &&
                          (isAddToCard && checkedCaas) &&
                          (isAddToCard && checkedCaas && hasNoCaasImage)
                      "
                    >
                      该应用在应用卡片未选择CAAS镜像标签
                    </span>
                  </fdev-tooltip>
                </span>
                <span>
                  <fdev-checkbox
                    v-model="deployPlatform"
                    val="SCC"
                    label="SCC"
                    :disable="
                      platformDisable &&
                        (isFeApp ||
                          !hasSccDeployInfo ||
                          appIsNotReleased ||
                          (isAddToCard && !checkedScc) ||
                          (isAddToCard && checkedScc && hasNoSccImage))
                    "
                  />
                  <fdev-tooltip
                    v-if="
                      platformDisable &&
                        (isFeApp ||
                          !hasSccDeployInfo ||
                          appIsNotReleased ||
                          (isAddToCard && !checkedScc) ||
                          (isAddToCard && checkedScc && hasNoSccImage))
                    "
                  >
                    <span v-if="platformDisable && isFeApp">
                      前端应用不能部署SCC平台
                    </span>
                    <span
                      v-if="platformDisable && !isFeApp && !hasSccDeployInfo"
                    >
                      该应用没有SCC平台部署信息
                    </span>
                    <span
                      v-if="
                        platformDisable &&
                          !isFeApp &&
                          hasSccDeployInfo &&
                          appIsNotReleased
                      "
                    >
                      该应用未曾投产，请先添加变更应用
                    </span>
                    <span
                      v-if="
                        platformDisable &&
                          !isFeApp &&
                          hasSccDeployInfo &&
                          !appIsNotReleased &&
                          (isAddToCard && !checkedScc)
                      "
                    >
                      该应用在应用卡片未勾选SCC部署平台
                    </span>
                    <span
                      v-if="
                        platformDisable &&
                          !isFeApp &&
                          hasSccDeployInfo &&
                          !appIsNotReleased &&
                          (isAddToCard && checkedScc) &&
                          (isAddToCard && checkedScc && hasNoSccImage)
                      "
                    >
                      该应用在应用卡片未选择SCC镜像标签
                    </span>
                  </fdev-tooltip>
                </span>
                <span
                  class="text-red q-ml-md"
                  v-if="deployPlatform.length === 0"
                >
                  请选择部署平台
                </span>
              </div>
            </f-formitem>

            <!-- CAAS网络模式 -->
            <f-formitem
              class="q-my-md"
              v-if="deployPlatform.includes('CAAS')"
              label="CAAS网络模式"
              diaS
              required
            >
              <fdev-radio
                class="q-mr-md"
                v-model="caasNetModel"
                val="Underlay"
                label="Underlay"
              />
            </f-formitem>

            <!-- SCC网络模式 -->
            <f-formitem
              class="q-mt-md"
              v-if="deployPlatform.includes('SCC')"
              label="SCC网络模式"
              diaS
              required
            >
              <fdev-radio
                class="q-mr-md"
                v-model="sccNetModel"
                val="Underlay"
                label="Underlay"
              />
              <fdev-radio v-model="sccNetModel" val="Overlay" label="Overlay" />
            </f-formitem>

            <!-- sid -->
            <f-formitem class="q-mt-lg" label="应用sid" diaS required>
              <fdev-input
                ref="esfUserInfo.appSid"
                v-model="appSid"
                :rules="[val => !!val || '请输入应用sid']"
              />
            </f-formitem>
          </div>
        </div>

        <!-- DEV/TEST/PROCSH/PROCHF环境 -->
        <div
          class="card q-mb-md"
          v-for="(item, index) in ['DEV', 'TEST', 'PROCSH', 'PROCHF']"
          :key="index"
        >
          <div class="bg-blue-1 full-width title-div q-mb-md row">
            <div class="title-icon row justify-center items-center">
              <f-icon name="version_s_f" class="text-blue-8" />
            </div>
            <div class="row justify-center items-center">
              {{ item }}环境目录
            </div>
          </div>
          <div class="justify-between">
            <!-- 用户名 -->
            <f-formitem label="用户名" diaS required>
              <fdev-input
                :ref="'esfUserInfo.' + item + '.account'"
                v-model="esfUserInfo[item].account"
                :rules="[val => !!val || '请输入用户名']"
              />
            </f-formitem>

            <!-- 密码 -->
            <f-formitem label="密码" diaS required>
              <fdev-input
                :ref="'esfUserInfo.' + item + '.password'"
                v-model="esfUserInfo[item].password"
                :rules="[val => !!val || '请输入密码']"
              />
            </f-formitem>

            <!-- 配置中心ip(DEV和TEST) -->
            <f-formitem
              label="配置中心"
              diaS
              required
              v-if="
                !(
                  deployPlatform.includes('SCC') && deployPlatform.length == 1
                ) &&
                  (item === 'DEV' || item === 'TEST')
              "
            >
              <fdev-select
                :ref="'esfUserInfo.' + item + '.configip.k1'"
                use-input
                map-options
                emit-value
                v-model="esfUserInfo[item].config_area.k1"
                option-label="partition"
                :display-value="
                  esfUserInfo[item].config_area.k1
                    ? `${esfUserInfo[item].config_area.k1.partition}(${
                        esfUserInfo[item].config_area.k1.config_area
                      })`
                    : ''
                "
                :options="envConfigOptions[item]"
                @focus="curEnvConfig = item"
                @filter="filterConfigFn"
                :rules="[val => !!val || '请选择配置中心']"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.partition">{{
                        scope.opt.partition
                      }}</fdev-item-label>
                      <fdev-item-label caption :title="scope.opt.config_area">
                        {{ scope.opt.config_area }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>

            <!-- 配置中心ip(PROCSH和PROCHF) -->
            <f-formitem
              label="配置中心"
              diaS
              required
              v-if="
                !(
                  deployPlatform.includes('SCC') && deployPlatform.length == 1
                ) &&
                  (item === 'PROCSH' || item === 'PROCHF')
              "
            >
            </f-formitem>
            <f-formitem
              label="K1"
              diaS
              label-style="padding-left:16px"
              v-if="
                !(
                  deployPlatform.includes('SCC') && deployPlatform.length == 1
                ) &&
                  (item === 'PROCSH' || item === 'PROCHF')
              "
            >
              <fdev-select
                :ref="'esfUserInfo.' + item + '.configip.k1'"
                use-input
                map-options
                emit-value
                v-model="esfUserInfo[item].config_area.k1"
                option-label="partition"
                :display-value="
                  esfUserInfo[item].config_area.k1
                    ? `${esfUserInfo[item].config_area.k1.partition}(${
                        esfUserInfo[item].config_area.k1.config_area
                      })`
                    : ''
                "
                :options="envConfigOptions[item]"
                @focus="focusConfigCenter(item, 'K1')"
                @filter="filterConfigFn"
                :rules="[val => !!val || '请选择配置中心']"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.partition">{{
                        scope.opt.partition
                      }}</fdev-item-label>
                      <fdev-item-label caption :title="scope.opt.config_area">
                        {{ scope.opt.config_area }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
            <f-formitem
              label="K2"
              diaS
              label-style="padding-left:16px"
              v-if="
                !(
                  deployPlatform.includes('SCC') && deployPlatform.length == 1
                ) &&
                  (item === 'PROCSH' || item === 'PROCHF')
              "
            >
              <fdev-select
                :ref="'esfUserInfo.' + item + '.configip.k2'"
                use-input
                map-options
                emit-value
                v-model="esfUserInfo[item].config_area.k2"
                option-label="partition"
                :display-value="
                  esfUserInfo[item].config_area.k2
                    ? `${esfUserInfo[item].config_area.k2.partition}(${
                        esfUserInfo[item].config_area.k2.config_area
                      })`
                    : ''
                "
                :options="envConfigOptions[item]"
                @focus="focusConfigCenter(item, 'K2')"
                @filter="filterConfigFn"
                :rules="[val => !!val || '请选择配置中心']"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.partition">{{
                        scope.opt.partition
                      }}</fdev-item-label>
                      <fdev-item-label caption :title="scope.opt.config_area">
                        {{ scope.opt.config_area }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
          </div>
        </div>
      </div>

      <template v-slot:btnSlot>
        <span>
          <fdev-btn
            dialog
            label="确定"
            @click="submit"
            :disable="platformDisable && appIsNotReleased"
            :loading="
              globalLoading['releaseForm/addEsfRegistration'] ||
                globalLoading['releaseForm/updateEsf']
            "
          />
          <fdev-tooltip v-if="platformDisable && appIsNotReleased">
            该应用未曾投产，请先添加变更应用
          </fdev-tooltip>
        </span>
      </template>
    </f-dialog>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import { deepClone, errorNotify, successNotify } from '@/utils/utils';
import { initEsfUserInfoData } from '../../../utils/constants';
export default {
  name: 'esfUserInfo',
  props: {
    value: {
      type: Boolean,
      default: false
    },
    type: {
      type: String,
      required: true
    },
    curEditData: {
      type: Object
    }
  },
  data() {
    return {
      appOptions: [], // 应用下拉选项
      curEnvConfig: '', // 当前正在操作的环境配置中心
      configCenterKey: '', // 当前正在操作的环境配置中心key值（K1/K2）
      envConfigOptions: {
        DEV: null, // DEV环境配置中心下拉选项
        TEST: null, // TEST环境配置中心下拉选项
        PROCSH: null, // PROCSH环境配置中心下拉选项
        PROCHF: null // PROCHF环境配置中心下拉选项
      },
      appName: null, // 应用名称
      caasNetModel: 'Underlay', // CAAS网络模式
      sccNetModel: 'Underlay', // SCC网络模式
      deployPlatform: [], // 部署平台
      platformDisable: false, // 部署平台是否禁用
      appSid: '', // 应用sid
      esfUserInfo: initEsfUserInfoData(),
      settingOptions: {} //各环境的配置中心下拉选项
    };
  },
  watch: {
    value(val) {
      if (val) {
        if (this.type === 'edit') {
          // 打开弹窗编辑，给各字段赋初始值
          this.initEditData(this.curEditData);
        }
      } else {
        // 关闭弹窗时，各字段恢复初始值
        this.platformDisable = false;
        this.curEnvConfig = '';
        this.appName = null;
        this.appOptions = [];
        this.caasNetModel = 'Underlay';
        this.sccNetModel = 'Underlay';
        this.deployPlatform = [];
        this.appSid = '';
        this.envConfigOptions.DEV = null;
        this.envConfigOptions.TEST = null;
        this.envConfigOptions.PROCSH = null;
        this.envConfigOptions.PROCHF = null;
        this.esfUserInfo = initEsfUserInfoData();
      }
    },
    async appName(val) {
      // 应用改变时，清空配置中心的值及下拉选项（编辑应用不可改变）
      if (this.type === 'add') {
        this.deployPlatform = [];
        this.caasNetModel = 'Underlay';
        this.sccNetModel = 'Underlay';
        this.esfUserInfo.DEV.config_area = { k1: null };
        this.esfUserInfo.TEST.config_area = { k1: null };
        this.esfUserInfo.PROCSH.config_area = { k1: null, k2: null };
        this.esfUserInfo.PROCHF.config_area = { k1: null, k2: null };
        this.envConfigOptions.DEV = null;
        this.envConfigOptions.TEST = null;
        this.envConfigOptions.PROCSH = null;
        this.envConfigOptions.PROCHF = null;
      }

      if (val) {
        // 查询该应用是否投过产
        await this.queryAppStatus({
          application_id: this.appName.id ? this.appName.id : this.appName,
          prod_id: this.$route.params.id
        });

        // 查询该应用在CAAS和SCC平台的部署情况
        await this.queryAppDeployPlatformInfo({
          id: this.appName.id ? this.appName.id : this.appName
        });
        this.platformDisable = true;
        if (this.appPublishInfo.flag === 0) {
          // 如果该应用未投过产，errorNotify提示
          errorNotify('该应用未曾投产，请先添加变更应用');
        }
      }
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('releaseForm', [
      'esfConfigData',
      'appPublishInfo',
      'appDeployPlatformInfo',
      'appsByAddEsf'
    ]),

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

    // 应用是否是未投产(flag为0未投产)
    appIsNotReleased() {
      return this.appPublishInfo && this.appPublishInfo.flag === 0;
    },

    // 应用是否添加到应用卡片(flag为1：未投产的应用添加完应用卡片后变为1)
    isAddToCard() {
      return this.appPublishInfo && this.appPublishInfo.flag === 1;
    },

    // 应用在应用卡片上是否勾选了CAAS部署平台
    checkedCaas() {
      return this.appPublishInfo.deploy_type.includes('CAAS');
    },

    // 应用在应用卡片上勾选了CAAS部署平台但没有CAAS镜像标签
    hasNoCaasImage() {
      return !this.appPublishInfo.pro_image_uri;
    },

    // 应用在应用卡片上是否勾选了SCC部署平台
    checkedScc() {
      return this.appPublishInfo.deploy_type.includes('SCC');
    },

    // 应用在应用卡片上勾选了SCC部署平台但没有SCC镜像标签
    hasNoSccImage() {
      return !this.appPublishInfo.pro_scc_image_uri;
    },

    // 应用英文名是否是以mspmk-cli开头的前端应用
    isFeApp() {
      return this.appName && this.appName.name_en.startsWith('mspmk-cli');
    }
  },
  methods: {
    ...mapActions('releaseForm', [
      'addEsfRegistration',
      'updateEsf',
      'queryEsfConfiguration',
      'queryAppStatus',
      'queryAppDeployPlatformInfo',
      'queryAppsByAddEsf'
    ]),

    // 编辑时，编辑时初始化各字段值
    initEditData(data) {
      this.appName = {
        name_zh: data.application_cn,
        name_en: data.application_en,
        id: data.application_id
      };
      this.deployPlatform = data.platform;
      this.caasNetModel = data.caas_network_area
        ? data.caas_network_area
        : 'Underlay';
      this.sccNetModel = data.scc_network_area
        ? data.scc_network_area
        : 'Underlay';
      this.appSid = data.sid;
      this.esfUserInfo = initEsfUserInfoData();
      data.esfInfo.forEach(item => {
        this.esfUserInfo[item.runtime_env].account = item.account;
        this.esfUserInfo[item.runtime_env].password = item.password;
        if (item.config_area) {
          this.esfUserInfo[item.runtime_env].config_area = item.config_area;
        }
        this.envConfigOptions[item.runtime_env] = null;
      });
    },

    // 应用输入过滤
    async appFilter(val, update, abort) {
      const updateCb = () => {
        const needle = val.toLowerCase().trim();
        this.appOptions = this.appsByAddEsf.filter(
          v =>
            v.name_zh.toLowerCase().includes(needle) ||
            v.name_en.toLowerCase().includes(needle)
        );
      };
      if (this.appOptions.length > 0) {
        update(updateCb);
        return;
      }
      await this.queryAppsByAddEsf({
        prod_id: this.$route.params.id
      });
      this.appOptions = deepClone(this.appsByAddEsf);
      update(updateCb);
    },

    focusConfigCenter(env, key) {
      this.curEnvConfig = env;
      this.configCenterKey = key;
    },

    // 给各环境的配置中心下拉选项赋值
    async filterConfigFn(val, update, abort) {
      const updateCb = () => {
        this.envConfigOptions[this.curEnvConfig] = this.settingOptions[
          this.curEnvConfig
        ].filter(v => v.partition.toLowerCase().includes(val.toLowerCase()));
      };
      if (this.envConfigOptions[this.curEnvConfig]) {
        update(updateCb);
        return;
      }
      if (!this.appName) {
        errorNotify('请先选择应用');
        return;
      }
      await this.queryEsfConfiguration({
        application_id: this.appName.id ? this.appName.id : this.appName,
        env_name: this.curEnvConfig
      });
      this.envConfigOptions[this.curEnvConfig] = deepClone(this.esfConfigData);
      this.settingOptions[this.curEnvConfig] = deepClone(this.esfConfigData);
      update(updateCb);
    },

    // 点击确认按钮，提交表单
    submit() {
      // 校验表单
      let keys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.startsWith('esfUserInfo');
      });
      Promise.all(
        keys.map(ele => {
          if (Array.isArray(this.$refs[ele])) {
            if (this.$refs[ele][0]) {
              return this.$refs[ele][0].validate() || Promise.reject(ele);
            }
          } else {
            if (this.$refs[ele]) {
              return this.$refs[ele].validate() || Promise.reject(ele);
            }
          }
        })
      ).then(
        async res => {
          // 通过校验，部署平台必填，发接口
          if (this.deployPlatform.length > 0) {
            const env = ['DEV', 'TEST', 'PROCSH', 'PROCHF'];
            // 当部署平台仅勾选了SCC，各环境集群及配置中心字段的值置空。
            if (
              this.deployPlatform.includes('SCC') &&
              this.deployPlatform.length == 1
            ) {
              env.forEach(envName => {
                delete this.esfUserInfo[envName].config_area;
              });
            }

            const params = {
              prod_id: this.$route.params.id,
              application_id: this.appName.id ? this.appName.id : this.appName,
              sid: this.appSid,
              platform: this.deployPlatform,
              esf_info: this.esfUserInfo
            };
            if (this.deployPlatform.includes('CAAS')) {
              params.caas_network_area = 'Underlay';
            }
            if (this.deployPlatform.includes('SCC')) {
              params.scc_network_area = this.sccNetModel;
            }
            if (this.type === 'add') {
              await this.addEsfRegistration(params);
              successNotify('新增成功');
            } else if (this.type === 'edit') {
              await this.updateEsf({
                ...params,
                id: this.curEditData.esf_id
              });
              successNotify('修改成功');
            }
            this.$emit('confirm');
          } else {
            errorNotify('请选择部署平台');
          }
        },
        rej => {
          Array.isArray(this.$refs[rej])
            ? this.$refs[rej][0].focus()
            : this.$refs[rej].focus();
        }
      );
    }
  }
};
</script>

<style lang="stylus" scoped>
.card
  .title-div
    .title-icon
      width 32px
      height 54px
      margin-left 8px
    .title-text
      height 54px
      margin-left 16px
</style>
