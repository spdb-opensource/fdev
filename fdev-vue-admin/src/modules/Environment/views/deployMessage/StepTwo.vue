<template>
  <!-- 
    1、进入第二步，根据第一步选择/路由传入的应用id，查询部署信息详情；
    2、查到应用详情，则此应用已绑定，实体组信息不能选择；
    3、查不到应用详情，则实体组信息可选择，根据实体组信息切换表格内容
  -->
  <Loading :visible="loading" class="bg-white">
    <fdev-form @submit="handleStep2">
      <!-- <p class="text-h6 text-left">基本信息</p> -->
      <div class="model-wrapper">
        <f-formitem
          fullWidth
          label="应用英文名"
          label-style="width:160px"
          class="normal-font"
          required
        >
          <fdev-input
            v-model="deployProfileModel.app_name_en"
            disable
            hint=""
          />
        </f-formitem>
        <f-formitem
          fullWidth
          label="部署网段"
          class="text-left normal-font"
          label-style="width:160px"
          required
        >
          <fdev-option-group
            name="accepted_genres"
            v-model="$v.deployProfileModel.network.$model"
            :options="networkOptions"
            type="checkbox"
            inline
            style="padding-bottom:20px"
          />
          <p
            class="text-negative error"
            v-show="$v.deployProfileModel.network.$error"
          >
            {{ '请选择网段' }}
          </p>
        </f-formitem>

        <f-formitem
          fullWidth
          label="CaaS平台"
          class="text-left normal-font"
          label-style="width:160px"
          style="padding-bottom:20px"
        >
          <fdev-toggle
            v-model="$v.deployProfileModel.caas_status.$model"
            true-value="1"
            false-value="0"
            left-label
            :disable="deployProfileModel.network.length ? false : true"
          />
        </f-formitem>
      </div>
      <div
        class="model-wrapper deploy-content q-px-lg q-py-lg"
        v-if="$v.deployProfileModel.caas_status.$model === '1'"
      >
        <f-formitem
          fullWidth
          label="CaaS业务测试部署环境"
          label-style="width:160px"
          v-if="deployProfileModel.network.includes('biz')"
        >
          <fdev-select
            v-model="$v.deployProfileModel.bizEnvTestList.$model"
            :options="envOptions"
            use-input
            option-label="name_en"
            option-value="id"
            multiple
            hint=""
            disable
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                  <fdev-item-label caption>
                    {{ scope.opt.name_cn }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem
          fullWidth
          label="CaaS业务生产部署环境"
          label-style="width:160px"
          v-if="deployProfileModel.network.includes('biz')"
        >
          <fdev-select
            v-model="$v.deployProfileModel.bizEnvProList.$model"
            :options="
              envOptions.filter(
                env =>
                  env.labels.includes('pro') &&
                  env.labels.includes('biz') &&
                  env.labels.includes('caas')
              )
            "
            use-input
            option-label="name_en"
            option-value="id"
            multiple
            ref="deployProfileModel.bizEnvProList"
            @input="touch"
            @filter="
              (val, update, abort) => filterEnv(val, update, abort, 'biz')
            "
            :rules="[
              () =>
                !$v.deployProfileModel.bizEnvProList.$error ||
                '请选择CaaS业务生产部署环境'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                  <fdev-item-label caption>
                    {{ scope.opt.name_cn }}
                  </fdev-item-label>
                </fdev-item-section>
                <fdev-item-section side>
                  <fdev-chip
                    dense
                    flat
                    square
                    class="text-white"
                    color="green-5"
                  >
                    业务
                  </fdev-chip>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem
          fullWidth
          label="CaaS网银测试部署环境"
          label-style="width:160px"
          v-if="deployProfileModel.network.includes('dmz')"
        >
          <fdev-select
            v-model="$v.deployProfileModel.dmzEnvTestList.$model"
            :options="envOptions"
            use-input
            option-label="name_en"
            option-value="id"
            multiple
            disable
            hint=""
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                  <fdev-item-label caption>
                    {{ scope.opt.name_cn }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem
          fullWidth
          label="CaaS网银生产部署环境"
          label-style="width:160px"
          v-if="deployProfileModel.network.includes('dmz')"
        >
          <fdev-select
            v-model="$v.deployProfileModel.dmzEnvProList.$model"
            :options="
              envOptions.filter(
                env =>
                  env.labels.includes('pro') &&
                  env.labels.includes('dmz') &&
                  env.labels.includes('caas')
              )
            "
            use-input
            option-label="name_en"
            option-value="id"
            multiple
            use-chips
            ref="deployProfileModel.dmzEnvProList"
            @input="touch"
            @filter="
              (val, update, abort) => filterEnv(val, update, abort, 'dmz')
            "
            :rules="[
              () =>
                !$v.deployProfileModel.dmzEnvProList.$error ||
                '请选择CaaS网银生产部署环境'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                  <fdev-item-label caption>
                    {{ scope.opt.name_cn }}
                  </fdev-item-label>
                </fdev-item-section>
                <fdev-item-section side>
                  <fdev-chip
                    dense
                    flat
                    square
                    class="text-white"
                    color="orange-5"
                  >
                    网银
                  </fdev-chip>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <fdev-separator class="q-my-lg" />
        <f-formitem fullWidth label="CaaS实体组信息" label-style="width:160px">
          <fdev-select
            v-model="$v.deployProfileModel.modelSetMsg.$model"
            :options="modelSetListOptions"
            ref="deployProfileModel.modelSetMsg"
            use-input
            hint=""
            option-label="nameCn"
            option-value="id"
            @filter="modlesFilter"
            @input="modlesSelected($event, 'tabArrData')"
            :disable="modlesDisable"
          />
        </f-formitem>

        <div class="text-left q-mb-sm row">
          <!-- <span class="font" style="margin-right:4px">选择CaaS部署实体</span> -->
          <!-- <div class="typeSty"> -->
          <f-formitem label="CaaS部署实体" label-style="width:160px">
            <fdev-select
              use-input
              multiple
              clearable
              v-model="selectCategory"
              :options="typesArr"
              option-label="name_en"
              @filter="modlesFilter2"
              class="q-mr-sm select-input input select-width"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                    <fdev-item-label caption>
                      {{ scope.opt.name_cn }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <div>
            <fdev-btn
              flat
              label="添加"
              @click="add1"
              :disable="!selectCategory || selectCategory.length === 0"
            />
            <fdev-tooltip
              anchor="top middle"
              self="bottom middle"
              :offest="[10, 10]"
              v-if="!selectCategory || selectCategory.length === 0"
            >
              请选择实体
            </fdev-tooltip>
          </div>
          <!-- </div> -->
        </div>
        <fdev-list class="rounded-borders">
          <fdev-expansion-item
            v-for="(table, index) in tabArrData"
            :key="index"
            :default-opened="index === 0"
          >
            <template v-slot:header>
              <fdev-item-section>
                <div class="section text-left text-teal">
                  <span class="text-black"
                    >{{ table.name_en }} ({{ table.name_cn }})</span
                  >
                </div>
              </fdev-item-section>
              <fdev-item-section side>
                <div>
                  <fdev-btn
                    label="移除"
                    color="red"
                    flat
                    dense
                    @click.stop="deleteSecondCategory(index, 'caas')"
                  />
                </div>
              </fdev-item-section>
            </template>
            <div class="q-mx-lg">
              <fdev-table
                class="my-sticky-column-table"
                :columns="columns"
                :data="table.env_key"
                row-key="id"
                :pagination.sync="pagination"
                flat
                no-select-cols
                noExport
                hide-bottom
              >
                <template v-slot:body="props">
                  <fdev-tr :props="props">
                    <fdev-td
                      key="name_en"
                      class="td-width"
                      :title="props.row.name_en || '-'"
                    >
                      {{ props.row.name_en || '-' }}
                    </fdev-td>
                    <fdev-td
                      key="name_cn"
                      class="td-width"
                      :title="props.row.name_cn || '-'"
                    >
                      {{ props.row.name_cn || '-' }}
                    </fdev-td>
                  </fdev-tr>
                </template>
              </fdev-table>
            </div>
          </fdev-expansion-item>
        </fdev-list>
      </div>

      <div class="model-wrapper">
        <f-formitem
          fullWidth
          label="SCC平台"
          class="text-left normal-font"
          label-style="width:160px"
          style="padding-bottom:20px"
        >
          <fdev-toggle
            v-model="$v.deployProfileModel.scc_status.$model"
            true-value="1"
            false-value="0"
            left-label
            :disable="deployProfileModel.network.length ? false : true"
          />
        </f-formitem>
      </div>
      <div
        class="model-wrapper deploy-content q-px-lg q-py-lg"
        v-if="$v.deployProfileModel.scc_status.$model === '1'"
      >
        <f-formitem fullWidth label="SCC SIT部署环境" label-style="width:160px">
          <fdev-select
            v-model="$v.deployProfileModel.sccSitList.$model"
            :options="sitOptions"
            @filter="sitFilter"
            use-input
            option-label="name_en"
            option-value="id"
            multiple
            :rules="[
              () =>
                !$v.deployProfileModel.sccSitList.$error ||
                'SIT、UAT、REL、PRO环境需全选/不选'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                  <fdev-item-label caption>
                    {{ scope.opt.name_cn }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem fullWidth label="SCC UAT部署环境" label-style="width:160px">
          <fdev-select
            v-model="$v.deployProfileModel.sccUatList.$model"
            :options="uatOptions"
            @filter="uatFilter"
            use-input
            option-label="name_en"
            option-value="name_cn"
            multiple
            :rules="[
              () =>
                !$v.deployProfileModel.sccUatList.$error ||
                'SIT、UAT、REL、PRO环境需全选/不选'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                  <fdev-item-label caption>
                    {{ scope.opt.name_cn }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem fullWidth label="SCC REL部署环境" label-style="width:160px">
          <fdev-select
            v-model="$v.deployProfileModel.sccRelList.$model"
            :options="relOptions"
            @filter="relFilter"
            use-input
            option-label="name_en"
            option-value="name_cn"
            multiple
            :rules="[
              () =>
                !$v.deployProfileModel.sccRelList.$error ||
                'SIT、UAT、REL、PRO环境需全选/不选'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                  <fdev-item-label caption>
                    {{ scope.opt.name_cn }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem fullWidth label="SCC PRO部署环境" label-style="width:160px">
          <fdev-select
            v-model="$v.deployProfileModel.sccProList.$model"
            :options="proOptions"
            @filter="proFilter"
            use-input
            option-label="name_en"
            option-value="name_cn"
            multiple
            :rules="[
              () =>
                !$v.deployProfileModel.sccProList.$error ||
                'SIT、UAT、REL、PRO环境需全选/不选'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                  <fdev-item-label caption>
                    {{ scope.opt.name_cn }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <fdev-separator class="q-my-lg" />
        <f-formitem fullWidth label="SCC实体组信息" label-style="width:160px">
          <fdev-select
            v-model="$v.deployProfileModel.sccModelSetMsg.$model"
            :options="sccModelSetListOptions"
            ref="deployProfileModel.sccModelSetMsg"
            use-input
            hint=""
            option-label="nameCn"
            option-value="id"
            @filter="sccModlesFilter"
            @input="modlesSelected($event, 'sccTabArrData')"
            :disable="sccModlesDisable"
          />
        </f-formitem>
        <div class="text-left q-mb-sm row">
          <!-- <span class="font text-h6" style="margin-right:4px"
            >选择SCC部署实体</span> -->
          <f-formitem label="选择SCC部署实体" label-style="width:160px">
            <fdev-select
              use-input
              multiple
              clearable
              v-model="sccSelectCategory"
              :options="sccTypesArr"
              option-label="name_en"
              @filter="modlesFilter3"
              class="q-mr-sm select-input input select-width"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                    <fdev-item-label caption>
                      {{ scope.opt.name_cn }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <div>
            <fdev-btn
              flat
              label="添加"
              @click="add2"
              :disable="!sccSelectCategory || sccSelectCategory.length === 0"
            />
            <fdev-tooltip
              anchor="top middle"
              self="bottom middle"
              :offest="[10, 10]"
              v-if="!sccSelectCategory || sccSelectCategory.length === 0"
            >
              请选择实体
            </fdev-tooltip>
          </div>
        </div>
        <fdev-list class="rounded-borders">
          <fdev-expansion-item
            v-for="(table, index) in sccTabArrData"
            :key="index"
            :default-opened="index === 0"
          >
            <template v-slot:header>
              <fdev-item-section>
                <div class="section text-left text-teal">
                  <span class="text-black"
                    >{{ table.name_en }} ({{ table.name_cn }})</span
                  >
                </div>
              </fdev-item-section>
              <fdev-item-section side>
                <div>
                  <fdev-btn
                    label="移除"
                    color="red"
                    flat
                    dense
                    @click.stop="deleteSecondCategory(index, 'scc')"
                  />
                </div>
              </fdev-item-section>
            </template>
            <div class="q-mx-lg">
              <fdev-table
                class="my-sticky-column-table"
                :columns="columns"
                :data="table.env_key"
                row-key="id"
                :pagination.sync="pagination"
                flat
                no-select-cols
                noExport
                hide-bottom
              >
                <template v-slot:body="props">
                  <fdev-tr :props="props">
                    <fdev-td
                      key="name_en"
                      class="td-width"
                      :title="props.row.name_en || '-'"
                    >
                      {{ props.row.name_en || '-' }}
                    </fdev-td>
                    <fdev-td
                      key="name_cn"
                      class="td-width"
                      :title="props.row.name_cn || '-'"
                    >
                      {{ props.row.name_cn || '-' }}
                    </fdev-td>
                  </fdev-tr>
                </template>
              </fdev-table>
            </div>
          </fdev-expansion-item>
        </fdev-list>
      </div>

      <fdev-btn
        label="上一步"
        dialog
        class="q-my-md q-mr-md"
        @click="$emit('prev')"
        v-if="!$route.query.appId"
      />
      <fdev-btn
        label="下一步"
        dialog
        class="q-my-md q-mr-md"
        :class="{
          gray:
            (tabArrData.length === 0 &&
              deployProfileModel.caas_status === '1') ||
            (sccTabArrData.length === 0 &&
              deployProfileModel.scc_status === '1')
        }"
        type="submit"
        :loading="
          globalLoading['environmentForm/queryRealTimeBindMsg'] || showLoading
        "
      >
        <fdev-tooltip
          position="top"
          v-if="
            (tabArrData.length == 0 &&
              deployProfileModel.caas_status === '1') ||
              (sccTabArrData.length === 0 &&
                deployProfileModel.scc_status === '1')
          "
        >
          {{
            tabArrData.length === 0 && deployProfileModel.caas_status === '1'
              ? `请选择CaaS部署实体`
              : `请选择SCC部署实体`
          }}
        </fdev-tooltip>
      </fdev-btn>
    </fdev-form>
  </Loading>
</template>

<script>
import { validate, errorNotify, deepClone } from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import {
  deployProfileModel,
  deployMessageColumns,
  networkOptions
} from '../../utils/constants';
import { mapState, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
export default {
  name: 'StepTwo',
  components: { Loading },
  data() {
    return {
      tabArrData: [],
      sccTabArrData: [],
      selectData: [],
      sccSelectData: [],
      selectCategory: [],
      sccSelectCategory: [],
      typesArr: [],
      sccTypesArr: [],
      deployProfileModel: deployProfileModel(),
      modelSetListOptions: [],
      sccModelSetListOptions: [],
      envOptions: [],
      tableData: {},
      networkOptions: networkOptions,
      modlesOptions: {},
      canAddSecondCategory: [],
      sccCanAddSecondCategory: [],
      columns: deployMessageColumns().detailColumns.slice(0, 2),
      modlesDisable: false,
      sccModlesDisable: false,
      caasModelSetList: [],
      sccModelSetList: [],
      loading: false,
      pagination: {
        rowsPerPage: 0,
        page: 1
      },
      showLoading: false,
      sitOptions: [],
      uatOptions: [],
      relOptions: [],
      proOptions: [],
      networkCopy: ''
    };
  },
  validations: {
    // biz 业务 ，dmz 网银
    deployProfileModel: {
      bizEnvProList: {
        required(val) {
          if (this.deployProfileModel.caas_status === '1') {
            if (
              this.deployProfileModel.network.includes('biz') &&
              this.deployProfileModel.dmzEnvProList.length === 0
            ) {
              return val.length > 0;
            } else if (
              this.deployProfileModel.network.includes('biz') &&
              this.deployProfileModel.network.length === 1
            ) {
              return val.length > 0;
            } else {
              return true;
            }
          } else {
            return true;
          }
        }
      },
      dmzEnvProList: {
        required(val) {
          if (this.deployProfileModel.caas_status === '1') {
            if (
              this.deployProfileModel.network.includes('dmz') &&
              this.deployProfileModel.bizEnvProList.length === 0
            ) {
              return val.length > 0;
            } else if (
              this.deployProfileModel.network.includes('dmz') &&
              this.deployProfileModel.network.length === 1
            ) {
              return val.length > 0;
            } else {
              return true;
            }
          } else {
            return true;
          }
        }
      },
      caas_status: {},
      scc_status: {},
      bizEnvTestList: {},
      dmzEnvTestList: {},
      sccSitList: {},
      sccUatList: {},
      sccRelList: {},
      sccProList: {},
      modelSetMsg: {},
      sccModelSetMsg: {},
      app_name_en: {
        required
      },
      network: {
        required
      }
    }
  },
  props: {
    appId: String,
    app_name_en: String
  },
  watch: {
    selectCategory: {
      deep: true,
      handler(val) {
        this.selectData = val;
      }
    },
    sccSelectCategory: {
      deep: true,
      handler(val) {
        this.sccSelectData = val;
      }
    },
    // caas平台实体组
    'deployDetail.modelSetMsg': {
      immediate: true,
      deep: true,
      handler(val) {
        if (val) {
          this.modelSetListOptions = [
            {
              id: this.deployDetail.modelSetMsg.id,
              nameCn: this.deployDetail.modelSetMsg.name_cn
            }
          ];
        } else {
          this.modelSetListOptions = this.caasModelSetList;
        }
      }
    },
    // scc平台实体组
    'deployDetail.sccModelSetMsg': {
      immediate: true,
      deep: true,
      handler(val) {
        if (val) {
          this.sccModelSetListOptions = [
            {
              id: this.deployDetail.sccModelSetMsg.id,
              nameCn: this.deployDetail.sccModelSetMsg.name_cn
            }
          ];
        } else {
          this.sccModelSetListOptions = this.sccModelSetList;
        }
      }
    },
    // 部署网络
    'deployProfileModel.network': {
      immediate: true,
      deep: true,
      handler(val) {
        if (val && val.length === 1) {
          if (val[0] !== this.networkCopy[0] || this.networkCopy.length === 2) {
            this.deployProfileModel.sccSitList = [];
            this.deployProfileModel.sccUatList = [];
            this.deployProfileModel.sccRelList = [];
            this.deployProfileModel.sccProList = [];
            this.networkCopy = val;
          }
        }
      }
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('environmentForm', [
      'modelSetList',
      'envList',
      'deployDetail',
      'modelList'
    ]),
    ...mapState('releaseForm', ['envType'])
  },
  methods: {
    ...mapMutations('environmentForm', ['saveDeployDetail']),
    ...mapActions('environmentForm', [
      'queryModelSetList',
      'getEnvList',
      'queryDeployDetail',
      'getModelList',
      'getModles',
      'queryRealTimeBindMsg'
    ]),
    ...mapActions('releaseForm', ['queryEnv']),
    filterEnv(val, update, abort, network) {
      update(() => {
        this.envOptions = this.envList.filter(
          env =>
            env.labels.includes('pro') &&
            env.labels.includes(network) &&
            env.labels.includes('caas') &&
            (env.name_cn.includes(val) || env.name_en.includes(val))
        );
      });
    },
    sitFilter(val, update, abort) {
      update(() => {
        this.sitOptions = this.envType.filter(env => {
          let bool = false;
          this.deployProfileModel.network.forEach(item => {
            if (env.labels.includes(item)) {
              bool = true;
            }
          });
          return (
            bool &&
            env.labels.includes('sit') &&
            (env.name_cn.includes(val) || env.name_en.includes(val))
          );
        });
      });
    },
    uatFilter(val, update, abort) {
      update(() => {
        this.uatOptions = this.envType.filter(env => {
          let bool = false;
          this.deployProfileModel.network.forEach(item => {
            if (env.labels.includes(item)) {
              bool = true;
            }
          });
          return (
            bool &&
            env.labels.includes('uat') &&
            (env.name_cn.includes(val) || env.name_en.includes(val))
          );
        });
      });
    },
    relFilter(val, update, abort) {
      update(() => {
        this.relOptions = this.envType.filter(env => {
          let bool = false;
          this.deployProfileModel.network.forEach(item => {
            if (env.labels.includes(item)) {
              bool = true;
            }
          });
          return (
            bool &&
            env.labels.includes('rel') &&
            (env.name_cn.includes(val) || env.name_en.includes(val))
          );
        });
      });
    },
    proFilter(val, update, abort) {
      update(() => {
        this.proOptions = this.envType.filter(env => {
          let bool = false;
          this.deployProfileModel.network.forEach(item => {
            if (env.labels.includes(item)) {
              bool = true;
            }
          });
          return (
            bool &&
            env.labels.includes('pro') &&
            (env.name_cn.includes(val) || env.name_en.includes(val))
          );
        });
      });
    },
    modlesFilter(val, update, abort) {
      update(() => {
        this.modelSetListOptions = this.caasModelSetList.filter(env =>
          env.nameCn.includes(val)
        );
      });
    },
    sccModlesFilter(val, update, abort) {
      update(() => {
        this.sccModelSetListOptions = this.sccModelSetList.filter(env =>
          env.nameCn.includes(val)
        );
      });
    },
    add1() {
      if (this.selectData && this.selectData.length > 0) {
        this.selectData.forEach(item => {
          if (this.tabArrData.length === 0) {
            this.tabArrData.push(item);
          } else {
            let bool = false;
            this.tabArrData.forEach(v => {
              if (v.name_en === item.name_en) {
                bool = true;
              }
            });
            if (!bool) {
              this.tabArrData.push(item);
            }
          }
          // if (this.tabArrData.indexOf(item) == -1) {
          //   this.tabArrData.push(item);
          // }
        });
      }
      this.selectCategory = [];
    },
    add2() {
      if (this.sccSelectData && this.sccSelectData.length > 0) {
        this.sccSelectData.forEach(item => {
          if (this.sccTabArrData.length === 0) {
            this.sccTabArrData.push(item);
          } else {
            let bool = false;
            this.sccTabArrData.forEach(v => {
              if (v.name_en === item.name_en) {
                bool = true;
              }
            });
            if (!bool) {
              this.sccTabArrData.push(item);
            }
          }
          // if (this.sccTabArrData.indexOf(item) == -1) {
          //   this.sccTabArrData.push(item);
          // }
        });
      }
      this.sccSelectCategory = [];
    },
    modlesFilter2(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.typesArr = this.modelList.filter(v => {
          let platformCopy = v.platform ? v.platform.split(',') : [];
          let bool = false;
          platformCopy.forEach(item => {
            if (item.includes('CaaS')) {
              bool = true;
            }
          });
          return (
            bool &&
            (v.name_en.toLowerCase().indexOf(needle) > -1 ||
              v.name_cn.indexOf(val) > -1)
          );
        });
      });
    },
    modlesFilter3(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.sccTypesArr = this.modelList.filter(v => {
          let platformCopy = v.platform ? v.platform.split(',') : [];
          let bool = false;
          platformCopy.forEach(item => {
            if (item.includes('SCC')) {
              bool = true;
            }
          });
          return (
            bool &&
            (v.name_en.toLowerCase().indexOf(needle) > -1 ||
              v.name_cn.indexOf(val) > -1)
          );
        });
      });
    },
    async handleStep2() {
      this.touch();

      if (
        this.deployProfileModel.scc_status === '0' &&
        this.deployProfileModel.caas_status === '0' &&
        this.deployProfileModel.network.length
      ) {
        errorNotify('CaaS平台、SCC平台至少选择一个');
        document.body.scrollTop = document.documentElement.scrollTop = 200;
        return false;
      }
      if (this.$v.deployProfileModel.$invalid) {
        return;
      }
      let scc = this.deployProfileModel;

      if (
        !scc.sccProList.length &&
        !scc.sccSitList.length &&
        !scc.sccUatList.length &&
        !scc.sccRelList.length &&
        scc.scc_status === '1'
      ) {
        errorNotify('SIT、UAT、REL、PRO部署环境至少选择一个');
        document.body.scrollTop = document.documentElement.scrollTop = 200;
        return false;
      }

      if (this.tabArrData.length === 0 && scc.caas_status === '1') {
        errorNotify('请添加CaaS部署实体');
        document.body.scrollTop = document.documentElement.scrollTop = 200;
        return false;
      }
      if (this.sccTabArrData.length === 0 && scc.scc_status === '1') {
        errorNotify('请添加CSS部署实体');
        document.body.scrollTop = document.documentElement.scrollTop = 200;
        return false;
      }
      this.showLoading = true;

      // CaaS平台和SCC平台不使用时存储已填写的数据
      let deployProfileModelCopy = deepClone(this.deployProfileModel);
      // let modelsInfoCopy = [];
      if (scc.caas_status === '0') {
        deployProfileModelCopy.bizEnvTestList = [];
        deployProfileModelCopy.dmzEnvTestList = [];
        deployProfileModelCopy.dmzEnvProList = [];
        deployProfileModelCopy.bizEnvProList = [];
        deployProfileModelCopy.modelSetMsg = null;
        deployProfileModelCopy.modelsInfo = [];
        deployProfileModelCopy.envTestList = [];
        deployProfileModelCopy.envProList = [];
        // modelsInfoCopy = this.sccTabArrData;
      }
      if (scc.scc_status === '0') {
        deployProfileModelCopy.sccSitList = [];
        deployProfileModelCopy.sccUatList = [];
        deployProfileModelCopy.sccRelList = [];
        deployProfileModelCopy.sccProList = [];
        deployProfileModelCopy.sccModelSetMsg = null;
        deployProfileModelCopy.sccModelsInfo = [];
        // modelsInfoCopy = this.tabArrData;
      }
      // if (scc.caas_status === '1' && scc.scc_status === '1') {
      //   modelsInfoCopy = this.tabArrData.concat(this.sccTabArrData);
      // }
      const model = {
        app_id: this.appId,
        modelsInfo: scc.caas_status === '0' ? [] : this.tabArrData,
        sccModelsInfo: scc.scc_status === '0' ? [] : this.sccTabArrData,
        // modelsInfo: modelsInfoCopy,
        // sccModeslInfo: this.sccTabArrData,
        envTestList: this.filterEnvByNetwork([
          ...deployProfileModelCopy.bizEnvTestList,
          ...deployProfileModelCopy.dmzEnvTestList
        ]),
        envProList: this.filterEnvByNetwork([
          ...deployProfileModelCopy.dmzEnvProList,
          ...deployProfileModelCopy.bizEnvProList
        ]),
        envSccList: [
          ...deployProfileModelCopy.sccSitList,
          ...deployProfileModelCopy.sccUatList,
          ...deployProfileModelCopy.sccRelList,
          ...deployProfileModelCopy.sccProList
        ]
      };
      this.saveDeployDetail({
        ...deployProfileModelCopy,
        ...model
      });
      /* 选择完模版信息后，更新deployDetail，下一步使用*/
      await this.queryRealTimeBindMsg(model);
      this.showLoading = false;
      this.$emit('next');
    },
    async findModelsBySecondCategory(second_category, i) {
      if (this.modlesOptions[second_category]) return;
      await this.getModelList({ second_category: second_category });
      this.$set(this.modlesOptions, second_category, this.modelList);
    },
    replaceTable(data, key) {
      this.$set(this.tableData, key, data);
      let index = this.canAddSecondCategory.indexOf(key);
      if (index === -1) return;
      this.canAddSecondCategory.splice(index, 1);
    },
    modlesSelected(val, type) {
      const obj = {};
      val &&
        val.modelsInfo.forEach(item => {
          if (!obj[item.second_category]) {
            obj[item.second_category] = {};
          }
          obj[item.second_category] = item;
        });
      let arr = Object.keys(obj).map(item => {
        return obj[item];
      });
      this[type] = arr;
    },
    deleteSecondCategory(secondCategory, type) {
      this.$q
        .dialog({
          title: '移除实体',
          message: '确认移除该实体',
          ok: '我知道了',
          cancel: '取消'
        })
        .onOk(() => {
          if (type === 'caas') {
            this.$delete(this.tabArrData, secondCategory);
            this.canAddSecondCategory.push(secondCategory);
          } else {
            this.$delete(this.sccTabArrData, secondCategory);
            this.sccCanAddSecondCategory.push(secondCategory);
          }
        });
    },
    touch() {
      this.$v.deployProfileModel.$touch();

      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('deployProfileModel') > -1;
      });

      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
    },
    filterEnvByNetwork(envlist) {
      return envlist.filter(env => {
        let bool = false;
        this.deployProfileModel.network.forEach(item => {
          if (env.labels.includes(item)) {
            bool = true;
          }
        });
        return bool;
      });
    },

    operateTable(tableData, type) {
      // 搜索框数据筛选
      let tabKeys = Object.keys(tableData);
      let newTypeArr = [];
      let indexs = [];
      this.modelList.forEach(item => {
        tabKeys.forEach(val => {
          if (tableData[val].name_en == item.name_en) {
            newTypeArr.push(item);
          }
        });
      });
      newTypeArr.forEach(item => {
        let i = this[type].indexOf(item);
        indexs.push(i);
      });
      indexs.sort((a, b) => {
        return b - a;
      });
      indexs.forEach(i => {
        this[type].splice(i, 1);
      });
    },

    async init() {
      this.loading = true;
      await this.queryDeployDetail({
        app_id: this.appId
      });
      await this.queryEnv({
        labels: ['scc']
      });
      this.loading = false;

      /* 环境根据网段区分显示 */
      this.deployProfileModel = {
        ...this.deployDetail,
        caas_status: !this.deployDetail.caas_status
          ? '0'
          : this.deployDetail.caas_status,
        scc_status: !this.deployDetail.scc_status
          ? '0'
          : this.deployDetail.scc_status,
        bizEnvTestList: this.deployDetail.envTestList.filter(
          env => env.labels.includes('biz') && env.labels.includes('caas')
        ),
        dmzEnvTestList: this.deployDetail.envTestList.filter(
          env => env.labels.includes('dmz') && env.labels.includes('caas')
        ),
        bizEnvProList: this.deployDetail.envProList.filter(
          env => env.labels.includes('biz') && env.labels.includes('caas')
        ),
        dmzEnvProList: this.deployDetail.envProList.filter(
          env => env.labels.includes('dmz') && env.labels.includes('caas')
        ),
        sccSitList: this.deployDetail.sccSitList || [],
        sccUatList: this.deployDetail.sccUatList || [],
        sccRelList: this.deployDetail.sccRelList || [],
        sccProList: this.deployDetail.sccProList || [],
        network: this.deployDetail.network
          ? this.deployDetail.network.split(',')
          : []
      };
      this.networkCopy = this.deployProfileModel.network;
      this.modlesDisable = !!this.deployDetail.modelSetMsg;
      this.sccModlesDisable = !!this.deployDetail.sccModelSetMsg;
      this.tableData = { ...this.deployDetail.modelsInfo };
      this.sccTableData = { ...this.deployDetail.sccModelsInfo };
      /* 
      处理二级分类：
      1、删除tableData,sccTableData中没有没有value的二级分类
      2、将删除的二级分类放到新增选项里
       */
      this.canAddSecondCategory = [];
      Object.keys(this.tableData).forEach(item => {
        if (!this.tableData[item]) {
          delete this.tableData[item];
          this.canAddSecondCategory.push(item);
        }
      });
      this.sccCanAddSecondCategory = [];
      Object.keys(this.sccTableData).forEach(item => {
        if (!this.sccTableData[item]) {
          delete this.sccTableData[item];
          this.sccCanAddSecondCategory.push(item);
        }
      });
      this.typesArr = [];
      this.sccTypesArr = [];
      this.tabArrData = Object.keys(this.tableData).map(item => {
        return this.tableData[item];
      });
      this.sccTabArrData = Object.keys(this.sccTableData).map(item => {
        return this.sccTableData[item];
      });

      await this.getModelList({ first_category: 'ci', state: '1' });
      // 过滤caas实体和scc实体
      this.typesArr = this.modelList.filter(v => v.platform === 'CaaS');
      this.sccTypesArr = this.modelList.filter(v => v.platform === 'SCC');
      this.operateTable(this.tableData, 'typesArr');
      this.operateTable(this.sccTableData, 'sccTypesArr');
    }
  },
  async created() {
    this.init();
    this.getEnvList().then(() => {
      this.envOptions = this.envList;
    });
    await this.queryModelSetList({
      // template: 'deploy',
      template: '',
      page: 1,
      per_page: 0
    });
    // 过滤caas实体组和scc实体组
    this.caasModelSetList = this.modelSetList.list.filter(
      v => v.template === 'deploy' || v.template === 'linux-deploy'
    );
    this.sccModelSetList = this.modelSetList.list.filter(
      v => v.template === 'scc-deploy'
    );
  }
};
</script>

<style lang="stylus" scoped>
.model-wrapper {
  max-width: 70%;
  margin: 0 auto;
}
// .model-wrapper >>> .q-field--with-bottom {
//   padding-bottom: 0px;
// }
.deploy-content {
  border: 1px dashed #bbb;
  border-radius: 8px;
  margin-bottom: 24px;
}
.normal-font >>> .label-font {
  font-weight: 700;
}
.font {
  font-weight: 700;
  margin: 0;
  display: inline-block;
  vertical-align: middle;
}

.chip-wrapper {
  max-width: 300px;
  display: flex;
  flex-wrap: wrap;
}

.td-wrapper {
  width: 300px;
}

.category-span {
  display: inline-block;
  line-height: 56px;
}

.td-width {
  width: 50%;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: left;
}

.error {
  font-size: 12px;
  padding: 0 12px;
}

.typeSty {
  display: inline-block;
  width: 400px;
  margin-left: 20px;
}

.typeSty label {
  display: inline-block;
  margin-right: 20px;
  width: 250px;
}

.gray >>> .q-btn__wrapper {
  background-color: #ccc;
}
</style>
