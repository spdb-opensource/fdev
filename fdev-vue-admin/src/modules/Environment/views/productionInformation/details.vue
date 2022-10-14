<template>
  <div>
    <f-block block class="mb-10">
      <div class="deploymaent-name mb-18 ">{{ deployment }}</div>
      <fdev-toolbar class="row justify-between">
        <fdev-tabs v-model="tab" shrink stretch>
          <fdev-tab name="Caas" label="Caas" class="pt-6"></fdev-tab>
          <fdev-tab name="SCC" label="SCC" class="pt-6"></fdev-tab>
        </fdev-tabs>
        <div class="row q-mb-sm no-wrap">
          <f-formitem
            :label="tab === 'Caas' ? '集群' : '租户'"
            label-auto
            label-style="margin-right:38px"
            value-style="width:200px"
          >
            <fdev-select
              v-model="selectParams.cluster"
              :options="caasCluster"
              option-label="label"
              option-value="value"
              emit-value
              map-options
              @input="queryCaasDetails()"
              v-if="tab === 'Caas'"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.label">
                      {{ scope.opt.label }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.value">
                      {{ scope.opt.value }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
            <fdev-select
              v-model="selectParams.namespace"
              :options="sccNamespaces"
              option-label="label"
              option-value="value"
              emit-value
              map-options
              @input="querySCCDetails()"
              v-else
            >
            </fdev-select>
          </f-formitem>
          <f-formitem
            label="部署网段"
            label-auto
            label-style="margin-right:10px"
            label-class="q-ml-lt"
            value-style="width:200px"
          >
            <fdev-select
              v-model="selectParams.CaasVlants"
              :options="caasVlans"
              option-label="label"
              option-value="value"
              emit-value
              map-options
              @input="queryCaasDetails()"
              v-if="tab === 'Caas'"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.label">
                      {{ scope.opt.label }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.value">
                      {{ scope.opt.value }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
            <fdev-select
              v-model="selectParams.SCCVlants"
              :options="sccVlans"
              option-label="label"
              option-value="value"
              emit-value
              map-options
              @input="querySCCDetails()"
              v-else
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.label">
                      {{ scope.opt.label }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.value">
                      {{ scope.opt.value }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <fdev-btn
            label="yaml视图"
            class="q-ml-lt"
            dialog
            @click="handleOpenYaml"
          ></fdev-btn>
        </div>
      </fdev-toolbar>
      <fdev-separator class="separator-style" />
    </f-block>
    <f-block block>
      <Loading :visible="loading">
        <fdev-tab-panels v-model="tab">
          <fdev-tab-panel :name="tab">
            <div v-if="tab === 'Caas' ? noCaasData : noSCCData">
              <fdev-icon class="icon-size" name="warning" />
              暂无数据
            </div>
            <div v-else>
              <div
                v-if="
                  (tab === 'Caas' && CaasDeploy) || (tab === 'SCC' && SCCDeploy)
                "
              >
                <div class="detailTitle">
                  <f-icon
                    name="list_s_f"
                    class="text-primary"
                    :width="16"
                    :height="16"
                  />
                  <span class="title-style q-ml-sm">部署信息</span>
                  <span class="updateTime-style q-mr-md"
                    >数据更新时间：{{
                      tab === 'Caas'
                        ? CaasDeploy.last_modified_date
                        : SCCDeploy.last_modified_date
                    }}</span
                  >
                </div>
                <div class="row border-bottom full-width detailTitle">
                  <div class="row full-width border-top">
                    <f-formitem
                      class="col-6"
                      label="租户"
                      profile
                      label-auto
                      bottom-page
                      label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                      label-style="height:100%;width:160px;"
                      value-style="line-height:43px;"
                      value-class="q-px-lg self-center"
                    >
                      <span>{{
                        tab === 'Caas'
                          ? CaasDeploy.namespace
                          : SCCDeploy.namespace
                      }}</span>
                    </f-formitem>
                    <f-formitem
                      class="col-6"
                      label="镜像标签"
                      profile
                      label-auto
                      bottom-page
                      label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                      label-style="height:100%;width:160px;"
                      value-class="q-px-lg"
                    >
                      <div class="config-info">
                        {{ tab === 'Caas' ? CaasDeploy.tag : SCCDeploy.tag }}
                      </div>
                    </f-formitem>
                  </div>
                  <div class="row full-width border-top">
                    <f-formitem
                      class="col-6"
                      label="cpu预留"
                      profile
                      label-auto
                      bottom-page
                      label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                      label-style="height:43px;width:160px;"
                      value-style="line-height:43px;"
                      value-class="q-px-lg"
                    >
                      <span>{{
                        tab === 'Caas'
                          ? CaasDeploy.cpu_requests
                          : SCCDeploy.cpu_requests
                      }}</span>
                    </f-formitem>
                    <f-formitem
                      class="col-6"
                      label="cpu限制"
                      profile
                      label-auto
                      bottom-page
                      label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                      label-style="height:43px;width:160px;"
                      value-style="line-height:43px;"
                      value-class="q-px-lg"
                    >
                      <span>{{
                        tab === 'Caas'
                          ? CaasDeploy.cpu_limits
                          : SCCDeploy.cpu_limits
                      }}</span>
                    </f-formitem>
                  </div>
                  <div class="row full-width border-top">
                    <f-formitem
                      class="col-6"
                      label="内存预留"
                      profile
                      label-auto
                      bottom-page
                      label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                      label-style="height:43px;width:160px;"
                      value-style="line-height:43px;"
                      value-class="q-px-lg"
                    >
                      <span>{{
                        tab === 'Caas'
                          ? CaasDeploy.memory_requests
                          : SCCDeploy.memory_requests
                      }}</span>
                    </f-formitem>
                    <f-formitem
                      class="col-6"
                      label="内存限制"
                      profile
                      label-auto
                      bottom-page
                      label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                      label-style="height:43px;width:160px;"
                      value-style="line-height:43px;"
                      value-class="q-px-lg"
                    >
                      <span>{{
                        tab === 'Caas'
                          ? CaasDeploy.memory_limits
                          : SCCDeploy.memory_limits
                      }}</span>
                    </f-formitem>
                  </div>
                </div>
                <div :class="tab === 'SCC' ? 'detailTitle' : ''">
                  <f-icon
                    name="list_s_f"
                    class="text-primary"
                    :width="16"
                    :height="16"
                  />
                  <span class="title-style q-ml-sm">运行信息</span>
                  <span class="replicas-style"
                    >容器组数：{{
                      tab === 'Caas' ? CaasDeploy.replicas : SCCDeploy.replicas
                    }}</span
                  >
                  <div class="row full-width q-my-md" v-if="tab === 'Caas'">
                    <span class="allocated_ip-style">预留ip地址段</span>
                    <div
                      class="row"
                      v-if="CaasConfig.allocated_ip_segment.length !== 0"
                    >
                      <div
                        class="div-position"
                        v-for="(allocated,
                        index) in CaasConfig.allocated_ip_segment"
                        :key="index"
                      >
                        <div class="div-border-right">{{ allocated }}</div>
                      </div>
                    </div>
                    <span v-else>—</span>
                  </div>
                </div>
                <fdev-table
                  :data="
                    tab === 'Caas'
                      ? caasDeploymentDetail.run
                      : sccDeploymentDetail.run
                  "
                  :columns="runTableColumns"
                  noExport
                  no-select-cols
                  class="mb-10"
                />
              </div>
              <div class="detailTitle">
                <f-icon
                  name="list_s_f"
                  class="text-primary"
                  :width="16"
                  :height="16"
                />
                <span class="title-style q-ml-sm">存储信息</span>
              </div>
              <fdev-table
                :data="
                  tab === 'Caas'
                    ? caasDeploymentDetail.storage
                    : sccDeploymentDetail.storage
                "
                :columns="storgeTableColumns"
                noExport
                no-select-cols
                hide-pagination
                class="detailTitle"
              />
              <div class="detailTitle">
                <f-icon
                  name="list_s_f"
                  class="text-primary"
                  :width="16"
                  :height="16"
                />
                <span class="title-style q-ml-sm">配置信息</span>
              </div>
              <div
                class="row border-bottom full-width"
                v-if="tab === 'Caas' && CaasConfig"
              >
                <div class="row full-width border-top">
                  <f-formitem
                    class="col-12"
                    label="注册中心"
                    profile
                    label-auto
                    bottom-page
                    label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                    label-style="height:100%;width:160px;"
                    value-style="padding-left:14px"
                    value-class="q-pr-lg"
                  >
                    <div class="row" v-if="CaasConfig.eureka.length !== 0">
                      <div
                        class="div-position"
                        v-for="(eureka, index) in CaasConfig.eureka"
                        :key="index"
                      >
                        <div class="div-border-right config-info">
                          {{ eureka }}
                        </div>
                      </div>
                    </div>
                    <span v-else class="no-data">{{ '—' }}</span>
                  </f-formitem>
                </div>
                <div class="row full-width border-top">
                  <f-formitem
                    class="col-12"
                    label="配置中心"
                    profile
                    label-auto
                    bottom-page
                    label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                    label-style="height:100%;width:160px;"
                    value-style="padding-left:14px"
                    value-class="q-pr-lg"
                  >
                    <div
                      class="row"
                      v-if="CaasConfig.config_center.length !== 0"
                    >
                      <div
                        class="div-position"
                        v-for="(config_center,
                        index) in CaasConfig.config_center"
                        :key="index"
                      >
                        <div class="div-border-right config-info">
                          {{ config_center }}
                        </div>
                      </div>
                    </div>
                    <span v-else class="no-data">{{ '—' }}</span>
                  </f-formitem>
                </div>
                <div class="row full-width border-top">
                  <f-formitem
                    class="col-12"
                    label="环境变量"
                    profile
                    label-auto
                    bottom-page
                    label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                    label-style="height:100%;width:160px;"
                    value-style="padding-left:14px;"
                    value-class="q-pr-lg"
                  >
                    <div class="row" v-if="CaasConfig.env.length !== 0">
                      <div
                        class="div-position"
                        v-for="(envItem, index) in CaasConfig.env"
                        :key="index"
                      >
                        <div class="div-border-right env-style">
                          <div class="margin-b">name：{{ envItem.name }}</div>
                          <div>value： {{ envItem.value }}</div>
                        </div>
                      </div>
                    </div>
                    <span v-else class="noEnv-style">{{ '—' }}</span>
                  </f-formitem>
                </div>
                <div class="row full-width border-top">
                  <f-formitem
                    class="col-4"
                    label="环境变量引用"
                    profile
                    label-auto
                    bottom-page
                    label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                    label-style="height:100%;width:160px;"
                    value-style="padding-left:14px"
                    value-class="q-pr-lg self-center"
                  >
                    <div class="row" v-if="CaasConfig.envfrom.length !== 0">
                      <div
                        class="div-position"
                        v-for="(envfrom, index) in CaasConfig.envfrom"
                        :key="index"
                      >
                        <div class="div-border-right config-info">
                          {{ getEnvfromName(envfrom) }}
                        </div>
                      </div>
                    </div>
                    <span v-else class="no-data">{{ '—' }}</span>
                  </f-formitem>
                  <f-formitem
                    class="col-4"
                    label="dns策略"
                    profile
                    label-auto
                    bottom-page
                    label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                    label-style="height:100%;width:160px;"
                    value-class="q-px-lg self-center"
                  >
                    <div class="config-info">
                      {{ CaasConfig.dnspolicy || '—' }}
                    </div>
                  </f-formitem>
                  <f-formitem
                    class="col-4"
                    label="dns服务器地址"
                    profile
                    label-auto
                    bottom-page
                    label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                    label-style="height:100%;width:160px;"
                    value-style="padding-left:14px "
                    value-class="self-center"
                  >
                    <div class="row" v-if="CaasConfig.dnsconfig.length !== 0">
                      <div
                        class="div-position"
                        v-for="(dnsconfig, index) in CaasConfig.dnsconfig"
                        :key="index"
                      >
                        <div class="div-border-right config-info">
                          {{ dnsconfig }}
                        </div>
                      </div>
                    </div>
                    <span v-else class="no-data">{{ '—' }}</span>
                  </f-formitem>
                </div>
                <div class="row full-width border-top">
                  <f-formitem
                    class="col-4"
                    label="预停止策略"
                    profile
                    label-auto
                    bottom-page
                    label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                    label-style="height:100%;width:160px;"
                    value-class="q-px-lg self-center"
                  >
                    <div class="config-info">
                      {{ CaasConfig.prestop || '—' }}
                    </div>
                  </f-formitem>
                  <f-formitem
                    class="col-4"
                    label="滚动发布参数"
                    profile
                    label-auto
                    bottom-page
                    label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                    label-style="height:100%;width:160px;"
                    value-class="q-px-lg self-center"
                  >
                    <span
                      v-if="CaasConfig.strategytype === ''"
                      class="noStrategytype-style"
                      >{{ '—' }}</span
                    >
                    <div
                      v-else-if="
                        CaasConfig.strategytype.type === 'RollingUpdate'
                      "
                      class="RollingUpdate-style"
                    >
                      <div class="margin-b">
                        type：{{ CaasConfig.strategytype.type }}
                      </div>
                      <div class="margin-b">
                        maxSurge：{{
                          CaasConfig.strategytype.rollingUpdate.maxSurge
                        }}
                      </div>
                      <div>
                        maxUnavailable：{{
                          CaasConfig.strategytype.rollingUpdate.maxUnavailable
                        }}
                      </div>
                    </div>
                    <div v-else class="config-info">
                      type：{{ CaasConfig.strategytype.type }}
                    </div>
                  </f-formitem>
                  <f-formitem
                    class="col-4"
                    label="镜像拉取密钥"
                    profile
                    label-auto
                    bottom-page
                    label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                    label-style="height:100%;width:160px;"
                    value-style="padding-left:14px"
                    value-class="q-pr-lg self-center"
                  >
                    <div
                      class="row"
                      v-if="CaasConfig.imagepullsecrets.length !== 0"
                    >
                      <div
                        class="div-position"
                        v-for="(imagepullsecrets,
                        index) in CaasConfig.imagepullsecrets"
                        :key="index"
                      >
                        <div class="div-border-right config-info">
                          {{ imagepullsecrets.name }}
                        </div>
                      </div>
                    </div>
                    <span v-else class="no-data">{{ '—' }}</span>
                  </f-formitem>
                </div>
                <div class="row full-width border-top">
                  <f-formitem
                    class="col-12"
                    label="hostalias"
                    profile
                    label-auto
                    bottom-page
                    label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                    label-style="height:100%;width:160px;"
                    value-style="padding-left:14px"
                    value-class="q-pr-lg"
                  >
                    <div class="row" v-if="CaasConfig.hostalias.length !== 0">
                      <div
                        class="div-position"
                        v-for="(host, index) in CaasConfig.hostalias"
                        :key="index"
                      >
                        <div class="div-border-right host-style">
                          <div class="margin-b">IP：{{ host.ip }}</div>
                          <div>hostName： {{ host.hostName }}</div>
                        </div>
                      </div>
                    </div>
                    <span v-else class="noHost-style">{{ '—' }}</span>
                  </f-formitem>
                </div>
              </div>
              <div
                class="row border-bottom full-width"
                v-if="tab === 'SCC' && SCCConfig"
              >
                <div class="row full-width border-top">
                  <f-formitem
                    class="col-4"
                    label="环境变量引用"
                    profile
                    label-auto
                    bottom-page
                    label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                    label-style="height:100%;width:160px;"
                    value-style="padding-left:14px"
                    value-class="q-pr-lg self-center"
                  >
                    <div class="row" v-if="SCCConfig.envfrom.length !== 0">
                      <div
                        class="div-position"
                        v-for="(envfrom, index) in SCCConfig.envfrom"
                        :key="index"
                      >
                        <div class="div-border-right config-info">
                          {{ getEnvfromName(envfrom) }}
                        </div>
                      </div>
                    </div>
                    <span v-else class="no-data">{{ '—' }}</span>
                  </f-formitem>
                  <f-formitem
                    class="col-4"
                    label="dns策略"
                    profile
                    label-auto
                    bottom-page
                    label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                    label-style="height:100%;width:160px;"
                    value-class="q-px-lg self-center"
                  >
                    <div class="config-info">
                      {{ SCCConfig.dnspolicy || '—' }}
                    </div>
                  </f-formitem>
                  <f-formitem
                    class="col-4"
                    label="滚动发布参数"
                    profile
                    label-auto
                    bottom-page
                    label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                    label-style="height:100%;width:160px;"
                    value-class="q-px-lg self-center"
                  >
                    <span
                      v-if="SCCConfig.strategytype === ''"
                      class="noStrategytype-style"
                      >{{ '—' }}</span
                    >
                    <div
                      v-else-if="
                        SCCConfig.strategytype.type === 'RollingUpdate'
                      "
                      class="RollingUpdate-style"
                    >
                      <div class="margin-b">
                        type：{{ SCCConfig.strategytype.type }}
                      </div>
                      <div class="margin-b">
                        maxSurge：{{
                          SCCConfig.strategytype.rollingUpdate.maxSurge.strVal
                        }}
                      </div>
                      <div>
                        maxUnavailable：{{
                          SCCConfig.strategytype.rollingUpdate.maxUnavailable
                            .strVal
                        }}
                      </div>
                    </div>
                    <div v-else class="config-info">
                      type：{{ SCCConfig.strategytype.type }}
                    </div>
                  </f-formitem>
                </div>
                <div class="row full-width border-top">
                  <f-formitem
                    class="col-12"
                    label="环境变量"
                    profile
                    label-auto
                    bottom-page
                    label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
                    label-style="height:100%;width:160px;"
                    value-style="padding-left:14px;"
                    value-class="q-pr-lg"
                  >
                    <div class="row" v-if="SCCConfig.env.length !== 0">
                      <div
                        class="div-position"
                        v-for="(envItem, index) in SCCConfig.env"
                        :key="index"
                      >
                        <div class="div-border-right env-style">
                          <div class="margin-b">name：{{ envItem.name }}</div>
                          <div>value： {{ envItem.value }}</div>
                        </div>
                      </div>
                    </div>
                    <span v-else class="noEnv-style">{{ '—' }}</span>
                  </f-formitem>
                </div>
              </div>
            </div>
          </fdev-tab-panel>
        </fdev-tab-panels>
      </Loading>
    </f-block>
    <f-dialog
      title="yaml视图"
      right
      v-model="yamlDialog"
      @before-show="beforeShow"
    >
      <div v-if="tab === 'Caas' ? noCaasData : noSCCData">
        <fdev-icon class="icon-size" name="warning" />
        暂无数据
      </div>
      <div v-else>
        <fdev-input type="textarea" ref="code" />
      </div>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="yamlDialog = false"/>
        <fdev-btn label="确定" dialog @click="yamlDialog = false"
      /></template>
    </f-dialog>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { perform } from '../../utils/constants';
import Loading from '@/components/Loading';
import CodeMirror from 'codemirror/lib/codemirror';
import 'codemirror/theme/lucario.css';
require('codemirror/mode/shell/shell');
export default {
  name: 'ProductionInfoDetails',
  components: { Loading },
  data() {
    return {
      tab: 'Caas',
      selectParams: {
        cluster: '', //集群
        namespace: '', //租户
        CaasVlants: '', //Caas部署网段
        SCCVlants: '' //SCC部署网段
      },
      caasCluster: [], //caas集群
      caasVlans: [], //caas部署网段
      sccNamespaces: [], //scc租户
      sccVlans: [], //scc部署网段
      noCaasData: false, //默认存在Caas信息
      noSCCData: false, //默认存在SCC信息
      deployment: '',
      ...perform,
      loading: false,
      yamlDialog: false,
      yaml: ''
    };
  },
  watch: {},
  computed: {
    ...mapState('environmentForm', [
      'platformClusters',
      'caasDeploymentDetail',
      'sccDeploymentDetail'
    ]),
    // Caas部署信息
    CaasDeploy() {
      return this.caasDeploymentDetail.deploy;
    },
    // Caas配置信息
    CaasConfig() {
      return this.caasDeploymentDetail.config;
    },
    // SCC部署信息
    SCCDeploy() {
      return this.sccDeploymentDetail.deploy;
    },
    // SCC配置信息
    SCCConfig() {
      return this.sccDeploymentDetail.config;
    }
  },

  async created() {
    // 从应用详情进入
    if (this.$route.params.appName) {
      this.deployment = this.$route.params.appName;
    }
    // 查询集群信息及详情
    try {
      await this.queryClustersDetails();
    } catch (e) {
      // 从应用详情进入，可能因各种情况出现接口返回报错信息，此时返回前一页
      this.$router.back();
    }
  },
  methods: {
    ...mapActions('environmentForm', [
      'queryClusters',
      'queryCaasInfo',
      'querySCCInfo'
    ]),

    // 查询集群信息
    async queryClustersDetails() {
      await this.queryClusters({ deploy_name: this.deployment });
      // 若caas集群存在，给集群、部署网段选择框选项赋值，并查询详情
      if (Object.keys(this.platformClusters.caas).length > 0) {
        this.caasCluster = this.platformClusters.caas.clusters;
        this.caasVlans = this.platformClusters.caas.vlans;
        // 选择框默认展示选项中的第一个
        this.selectParams.cluster = this.caasCluster[0].value;
        this.selectParams.CaasVlants = this.caasVlans[0].value;
        // 查询Caas信息详情
        await this.queryCaasDetails();
      } else {
        // 若不存在，则无caas信息，caas tab页面展示"暂无数据"
        this.noCaasData = true;
      }
      // 若SCC集群存在，给租户、部署网段选择框选项赋值，并查询详情
      if (Object.keys(this.platformClusters.scc).length > 0) {
        this.sccNamespaces = this.platformClusters.scc.namespaces;
        this.sccVlans = this.platformClusters.scc.vlans;
        // 选择框默认展示选项中的第一个
        this.selectParams.namespace = this.sccNamespaces[0].value;
        this.selectParams.SCCVlants = this.sccVlans[0].value;
        // 查询SCC信息详情
        await this.querySCCDetails();
      } else {
        // 若不存在，则无SCC信息，SCC tab页面展示"暂无数据"
        this.noSCCData = true;
      }
    },
    // 环境引用变量可能存在两个对象属性，在此判断后展示
    getEnvfromName(env) {
      if (env.hasOwnProperty('secretRef')) {
        return env.secretRef.name;
      } else if (env.hasOwnProperty('configMapRef')) {
        return env.configMapRef.name;
      }
    },
    // 查询Caas信息详情
    async queryCaasDetails() {
      this.loading = true;
      await this.queryCaasInfo({
        deploy_name: this.deployment,
        cluster: this.selectParams.cluster,
        vlan: this.selectParams.CaasVlants
      });
      // 若Caas信息详情不存在，caas tab页面展示"暂无数据"
      this.noCaasData = this.caasDeploymentDetail ? false : true;
      this.loading = false;
    },
    // 查询SCC信息详情
    async querySCCDetails() {
      this.loading = true;
      await this.querySCCInfo({
        deploy_name: this.deployment,
        namespace: this.selectParams.namespace,
        vlan: this.selectParams.SCCVlants
      });
      // 若SCC信息详情不存在，SCC tab页面展示"暂无数据"
      this.noSCCData = this.sccDeploymentDetail ? false : true;
      this.loading = false;
    },
    handleOpenYaml() {
      this.yamlDialog = true;
    },
    beforeShow() {
      // 若Caas或SCC无详情信息，点击yaml视图弹窗，不会初始化编辑器
      if (
        (this.tab === 'Caas' && this.noCaasData) ||
        (this.tab === 'SCC' && this.noSCCData)
      ) {
        return;
      } else {
        this.$nextTick(() => {
          //初始化编辑器
          this.yaml = CodeMirror.fromTextArea(this.$refs.code.$refs.input, {
            mode: 'text/x-sh',
            theme: 'lucario',
            lineNumbers: true,
            lineWrapping: true,
            tabSize: 4,
            line: true,
            matchBrackets: true,
            showCursorWhenSelecting: true,
            readOnly: true
          });
          this.yaml.setValue(
            this.tab === 'Caas'
              ? this.caasDeploymentDetail.yaml
              : this.sccDeploymentDetail.yaml
          );
        });
      }
    }
  }
};
</script>

<style lang="stylus" scoped>
.mb-10
  margin-bottom: 10px
.mb-18
  margin-bottom: 18px
.pt-6
  padding-top: 6px
.deploymaent-name
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 18px;
  font-weight: 600;
  margin-top: 10px;
.separator-style
  background: #ECEFF1
  height: 1px
.title-style
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 22px;
  font-weight: 600;
.updateTime-style
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 22px;
  font-weight: 400;
  float: right
.detailTitle
  margin-bottom: 20px
.replicas-style
  font-size: 14px;
  color: #999999;
  letter-spacing: 0;
  line-height: 22px;
  font-weight: 600;
  margin-left: 20px
.allocated_ip-style
  margin-right: 6px
.div-position
  flex-direction row
  display flex
.div-border-right
  border-right: 1px solid #ddd
  padding 0 10px
.div-position:last-child .div-border-right
  border-right: none
border(align='all')
  border-top 1px solid #ddd if align == 'top' || align == 'all'
  border-bottom 1px solid #ddd if align == 'bottom' || align == 'all'
.border-top
  border('top')
.border-bottom
  border('bottom')
.border
  border()

/deep/ .CodeMirror-sizer {
  border-right-width: 40px !important;
}

/deep/ .CodeMirror-scroll
  margin-bottom 0

/deep/ .CodeMirror
    width 100%
    height auto
    font-size 14px
.env-style
.host-style
  line-height:14px;
  padding-top:3px;
  padding-bottom:3px;
  margin-top:9px;
  margin-bottom:9px
.margin-b
  margin-bottom:5px
.noEnv-style
.noHost-style
  padding-left:10px
  line-height:57px;
.RollingUpdate-style
  line-height:14px;
  margin: 9px 0px
.config-info
  line-height:14px;
  padding-top:6px;
  padding-bottom:6px;
  margin-top:9px;
  margin-bottom:9px
.no-data
  line-height:43px;
  padding-left:10px
.noStrategytype-style
  line-height:43px;
</style>
