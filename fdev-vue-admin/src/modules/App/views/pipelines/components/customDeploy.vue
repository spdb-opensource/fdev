<template>
  <div class="q-mt-md" v-if="isMobileType">
    <div class="row justify-center">
      <f-formitem label="分支" required>
        <fdev-select
          use-input
          ref="mobileModel.branch"
          @filter="branchFilter"
          v-model="$v.mobileModel.branch.$model"
          :options="filterBranchList"
          :rules="[() => !$v.mobileModel.branch.$error || '分支不能为空']"
        />
      </f-formitem>
    </div>
    <div class="row justify-center">
      <f-formitem label="包类型" required>
        <fdev-select
          map-options
          emit-value
          ref="mobileModel.evns"
          v-model="$v.mobileModel.evns.$model"
          :options="evnsOptions"
          :rules="[() => !$v.mobileModel.evns.$error || '包类型不能为空']"
        />
      </f-formitem>
    </div>
    <div class="row justify-center">
      <f-formitem label="是否为特殊测试包" v-if="isEvns">
        <fdev-select
          map-options
          emit-value
          ref="mobileModel.specialEvns"
          v-model="mobileModel.specialEvns"
          :options="specialOptions"
        />
      </f-formitem>
    </div>
    <div class="row justify-center q-mb-md">
      <f-formitem label="是否加固">
        <div class="q-gutter-x-sm">
          <fdev-radio val="1" v-model="mobileModel.reinforce" label="加固" />
          <fdev-radio val="0" v-model="mobileModel.reinforce" label="不加固" />
        </div>
      </f-formitem>
    </div>
    <div class="row justify-center">
      <f-formitem label="应用描述" required>
        <fdev-input
          ref="mobileModel.desc"
          v-model="$v.mobileModel.desc.$model"
          type="textarea"
          :rules="[() => !$v.mobileModel.desc.$error || '应用描述不能为空']"
        />
      </f-formitem>
    </div>
    <div class="row justify-center q-mt-md">
      <fdev-btn
        label="部署"
        @click="handleAllTip"
        :loading="globalLoading['appForm/createPipeline']"
      />
    </div>
  </div>
  <div class="q-mt-md" v-else>
    <div class="row justify-center">
      <f-formitem label="分支" required>
        <fdev-select
          use-input
          ref="customDeploy.branch"
          @filter="branchFilter"
          v-model="$v.serchModel.branch.$model"
          :options="filterBranchList"
          :rules="[() => !$v.serchModel.branch.$error || '分支不能为空']"
        />
      </f-formitem>
    </div>
    <div class="row justify-center">
      <f-formitem label="SCC部署环境">
        <fdev-select
          use-input
          multiple
          emit-value
          v-model="serchModel.scc"
          :options="filterSccList"
          @filter="sccListFilter"
          option-label="name_en"
          option-value="name_en"
          @input="getSCCEnv"
          hint=""
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.name_en">
                  {{ scope.opt.name_en }}
                </fdev-item-label>
                <fdev-item-label caption :title="scope.opt.name_cn">
                  {{ scope.opt.name_cn }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
    </div>
    <div class="row justify-center">
      <f-formitem label="CaaS部署环境">
        <fdev-select
          use-input
          v-model="serchModel.env"
          :options="filterEnvList"
          @filter="envListFilter"
          option-label="env_name"
          :option-value="opt => opt"
          @input="getCaaSEnv"
          hint=""
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.name_en">
                  {{ scope.opt.name_en }}
                </fdev-item-label>
                <fdev-item-label caption :title="scope.opt.name_cn">
                  {{ scope.opt.name_cn }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
    </div>
    <div class="row justify-center">
      <f-formitem label="更新配置文件">
        <fdev-radio val="1" v-model="isUpdateSettingFile" label="是" />
        <fdev-radio
          val="0"
          v-model="isUpdateSettingFile"
          label="否"
          class="q-ml-lg"
        />
      </f-formitem>
    </div>
    <div class="row justify-center">
      <f-formitem label="触发应用部署">
        <fdev-radio val="1" v-model="isUpdateImage" label="是" />
        <fdev-radio
          val="0"
          v-model="isUpdateImage"
          label="否"
          class="q-ml-lg"
        />
      </f-formitem>
    </div>
    <div class="row justify-center q-my-md">
      <f-icon name="alert_t_f" />
      <p class="q-ml-md font">
        当部署压测环境时，为避免压测中断，建议选择不触发应用部署
      </p>
    </div>
    <fdev-separator />
    <div class="full-width row justify-center">
      <fdev-tabs class="q-mt-md" v-model="tab" style="width:80%">
        <fdev-tab
          v-for="(env, index) in tabs"
          :key="index"
          :name="env.env_name"
          :label="env.env_name"
        />
      </fdev-tabs>
    </div>
    <fdev-tab-panels v-model="tab">
      <fdev-tab-panel
        v-for="(env, index) in tabs"
        :key="index"
        :name="env.env_name"
        :label="env.env_name"
      >
        <div class="row q-mt-md justify-center" v-if="noValue">
          <f-icon name="alert_t_f" />
          当前环境未绑定实体
        </div>
        <div v-else class="row q-mt-md justify-center">
          <div class="col">
            <f-formitem
              v-for="(item, index) in $v.customDeployModel.$each.$iter"
              :label="item.name.$model"
              :key="index"
              class="hidden-input"
              style="justify-content: center;"
              :required="item.$model.require === '1'"
            >
              <fdev-input
                v-model="item.value.$model"
                :ref="`customDeploy${index}`"
                class="input"
                :rules="[() => item.value.need || `请输入${item.name.$model}`]"
                :disable="isReadonly(item.$model.key)"
                v-if="
                  item.$model.data_type
                    ? item.$model.data_type !== 'string'
                    : false
                "
              >
                <template v-slot:append>
                  <fdev-btn
                    flat
                    :width="14"
                    :height="14"
                    ficon="edit"
                    v-if="
                      (item.$model.data_type
                        ? item.$model.data_type !== ''
                        : false) && !isReadonly(item.$model.key)
                    "
                    @click="selectKeyType(item.$model.data_type, index)"
                  >
                    <fdev-tooltip position="top">
                      编辑属性映射值
                    </fdev-tooltip>
                  </fdev-btn>
                </template>
                <div
                  v-if="
                    item.$model.data_type === 'array' &&
                      item.$model.value !== ''
                  "
                >
                  <fdev-chip square color="gray" text-color="black">
                    {{
                      JSON.parse(item.$model.value).length > 1
                        ? item.$model.key.toLowerCase() + '0'
                        : item.$model.key.toLowerCase()
                    }}
                    <fdev-tooltip>
                      <div>
                        <p
                          v-for="(val, valkey, i) in JSON.parse(
                            item.$model.value
                          )[0]"
                          :key="i"
                          class="q-ma-xs"
                        >
                          <span
                            v-if="valkey !== 'index' && valkey !== '__index'"
                          >
                            {{ valkey }}：{{ val }}
                          </span>
                        </p>
                      </div>
                    </fdev-tooltip>
                  </fdev-chip>
                  <span v-if="JSON.parse(item.$model.value).length > 1">
                    ...
                  </span>
                </div>
                <fdev-chip
                  square
                  v-if="
                    item.$model.data_type === 'object' &&
                      item.$model.value !== ''
                  "
                  color="gray"
                  text-color="black"
                >
                  {{ item.$model.key.toLowerCase() }}
                  <fdev-tooltip position="top">
                    <div>
                      <p
                        v-for="(val, valkey, i) in JSON.parse(
                          item.$model.value
                        )"
                        :key="i"
                        class="q-ma-xs"
                      >
                        <span> {{ valkey }}：{{ val }} </span>
                      </p>
                    </div>
                  </fdev-tooltip>
                </fdev-chip>
              </fdev-input>
              <fdev-input
                v-model="item.value.$model"
                :ref="`customDeploy${index}`"
                :rules="[() => item.value.need || `请输入${item.name.$model}`]"
                :disable="isReadonly(item.$model.key)"
                v-else
                :title="item.value.$model"
              >
              </fdev-input>
            </f-formitem>
          </div>
        </div>
      </fdev-tab-panel>
    </fdev-tab-panels>

    <div class="row justify-center q-mt-md">
      <fdev-btn
        v-if="hasEnvs && serchModel.branch"
        label="部署"
        @click="handleCustomDeploy"
        :loading="globalLoading['appForm/definedDeploy']"
      />
    </div>
    <tableDialog
      v-model="tableDialogOpen"
      :title="headerTitle"
      :columns="columns"
      :attrMapKey="attrMapKey"
      @getTableDate="saveTableDate"
      :json_schema="json"
      :customDeployData="dialogDate"
    />
    <listDialog
      v-model="listDialogOpen"
      :title="headerTitle"
      :mapList="mapList"
      @getListDate="saveListDate"
    />
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import { validate, deepClone, successNotify } from '@/utils/utils';
import {
  createDeployModel,
  evnsOptions,
  specialOptions
} from '@/modules/App/utils/constants';
import tableDialog from '@/modules/App/components/tableDialog';
import listDialog from '@/modules/App/components/listDialog';

export default {
  name: 'customDeploy',
  components: { tableDialog, listDialog },
  data() {
    return {
      tab: '',
      tabs: [],
      deleteEnvs: [],
      updateEnvs: [],
      hasEnvs: false,
      deployTip: [],
      allEnvs: [],
      noValue: false,
      selectedSCCEnvs: [],
      selectedCaasEnvs: [],
      allCustomDepModels: [],
      serchModel: {
        env: null,
        scc: [],
        branch: ''
      },
      customDeployModel: null,
      mobileModel: createDeployModel(),
      filterEnvList: [],
      filterSccList: [],
      filterBranchList: [],
      mapList: [],
      dialogDate: [],
      tableDialogOpen: false,
      listDialogOpen: false,
      headerTitle: '',
      columns: [],
      length: 0,
      json: {},
      json_schema: {},
      attrMapList: [],
      attrMapKey: [],
      evnsOptions,
      specialOptions,
      isUpdateSettingFile: '1', //是否触发配置文件更新推送，默认为是
      isUpdateImage: '1' //是否触发应用部署，默认为是
    };
  },
  validations: {
    serchModel: {
      branch: {
        required
      }
    },
    customDeployModel: {
      $each: {
        value: {
          need(val, item) {
            if (
              item.require === '1' &&
              item.key !== 'FDEV_CAAS_PWD' &&
              item.key !== 'FDEV_CAAS_REGISTRY_PASSWORD'
            ) {
              return val.replace(/(^\s*)|(\s*$)/g, '');
            } else {
              return true;
            }
          }
        },
        name: {}
      }
    },
    mobileModel: {
      branch: {
        required
      },
      evns: {
        required
      },
      desc: {
        required
      }
    }
  },
  props: ['gitlab_project_id', 'id', 'type'],
  watch: {
    // 'serchModel.env': {
    //   handler(value, oldValue) {
    //     const selectValue = deepClone(value.env_variables);
    //     this.customDeployModel = Object.entries(selectValue).map(item => {
    //       return { key: item[0], value: item[1], name: item[0] };
    //     });
    //   }
    // },
    // 监听tab，记录并保存每个环境的实体
    tab(newVal, oldVal) {
      const modelTemp = {
        temp: null,
        newInd: '',
        oldInd: ''
      };
      // 当所选环境只有一个，删除该环境，清空allCustomDepModels
      if (oldVal && !newVal) {
        this.allCustomDepModels = [];
      }
      // 当所选有多个环境：1.用户点击切换tab;2.新增、删除会造成tab切换，默认跳至tabs[0]
      if (oldVal && newVal && oldVal !== newVal) {
        // 此时customDeployModel为oldVal对应的实体
        modelTemp.temp = deepClone(this.customDeployModel);
        // 获取oldVal和newVal在tabs数组中的位置
        this.tabs.forEach((item, index) => {
          if (oldVal === item.env_name) {
            modelTemp.oldInd = index;
          }
        });
        this.tabs.forEach((item, index) => {
          if (newVal === item.env_name) {
            modelTemp.newInd = index;
          }
        });
        // 新增、删除会造成tab切换，默认跳至tabs[0]，新增无影响，删除需考虑删除的环境在tabs数组的位置
        // 若删除了第一个tab页对应的环境，则oldVal在tabs数组中的位置为''，此时只需更新customDeployModel为newVal的实体即可
        // 若删除了非第一个tab页对应的环境或点击切换tab，则需要将oldVal的实体重新存入allCustomDepModel，并更新customDeployModel为newVal的实体
        if (modelTemp.oldInd) {
          this.allCustomDepModels[modelTemp.oldInd].value = deepClone(
            modelTemp.temp
          );
        }
        this.customDeployModel = this.allCustomDepModels[
          modelTemp.newInd
        ].value;
        this.dialogDate = this.customDeployModel;
        this.noValue = this.customDeployModel.length > 0 ? false : true;
      }
    }
  },
  computed: {
    ...mapState('appForm', ['branchList']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('environmentForm', {
      newEnvList: 'envList',
      envModelVar: 'envModelVar'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('releaseForm', ['envType']),
    isMobileType() {
      let typeArray = ['IOS应用', 'ANDROID应用'];
      return typeArray.includes(this.type.toUpperCase()) ? true : false;
    },
    // 是否为特殊测试包
    isEvns() {
      let envsList = ['test', 'testrun', 'release'];
      return envsList.includes(this.mobileModel.evns) ? true : false;
    }
  },
  methods: {
    ...mapActions('appForm', {
      queryAllBranch: 'queryAllBranch',
      createPipeline: 'createPipeline',
      definedDeploy: 'definedDeploy'
    }),
    ...mapActions('environmentForm', {
      getEnvList: 'getEnvList',
      queryVarByEnvAndType: 'queryVarByEnvAndType'
    }),
    ...mapActions('releaseForm', ['queryEnv']),
    handleAllTip() {
      this.$v.mobileModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('mobileModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.mobileModel.$invalid) {
        return;
      }
      this.handleMobileDeploy();
    },
    async handleMobileDeploy() {
      let array = ['gray', 'prod'];
      let value;
      //是否为特殊测试包,选否，传包类型字段，选其他，则传输入的特殊测试包值
      if (this.mobileModel.specialEvns) {
        value = array.includes(this.mobileModel.evns)
          ? this.mobileModel.evns
          : this.mobileModel.specialEvns;
      } else {
        value = this.mobileModel.evns;
      }
      let params = {
        id: this.id,
        ref: this.mobileModel.branch,
        variables: [
          {
            value: this.mobileModel.branch,
            key: 'FDEV_BRANCH_NAME'
          },
          {
            value: value,
            key: 'FDEV_ENVS'
          },
          {
            value: this.mobileModel.reinforce,
            key: 'FDEV_IS_REINFORCE'
          },
          {
            value: this.mobileModel.desc,
            key: 'FDEV_DESC'
          },
          {
            value: this.currentUser.git_user,
            key: 'FDEV_USERNAME'
          }
        ]
      };
      await this.createPipeline(params);
      successNotify('部署成功!');
    },
    // 个别属性只读不可编辑
    isReadonly(val) {
      if (val === 'FDEV_CAAS_REGISTRY_PASSWORD' || val === 'FDEV_CAAS_PWD') {
        return true;
      } else {
        return false;
      }
    },
    // 个别属性在弹窗中编辑
    async selectKeyType(title, index) {
      // 如果当前高级属性是array类型的，将数据处理成table需要的格式
      if (title === 'array') {
        // 传入table的数据
        let newValue = this.customDeployModel[index].value
          ? JSON.parse(this.customDeployModel[index].value)
          : [];
        this.attrMapKey = deepClone(newValue ? newValue : []);
        this.length = this.customDeployModel[index].value.length;
        this.json_schema = JSON.parse(
          this.customDeployModel[index].json_schema
        ).items;
        this.json = this.json_schema;
        // // 以下是生成table的columns
        const columnsArr = Object.keys(this.json_schema.properties);
        this.columns = columnsArr.map(item => {
          let required = false;
          if (this.json_schema.required) {
            required = this.json_schema.required.includes(item);
          }
          return {
            name: item,
            label: this.json_schema.properties[item].description,
            field: item,
            required: required,
            align: 'left'
          };
        });
        this.columns.push({ name: 'operate', label: '操作', align: 'left' });
        this.tableDialogOpen = true;
        this.headerTitle = this.customDeployModel[index].name;
        this.index = index;
      } else if (title === 'object') {
        let requiredArray = JSON.parse(
          this.customDeployModel[index].json_schema
        ).required;
        this.headerTitle = this.customDeployModel[index].name;
        // 如果当前高级属性是object类型的
        // 数据修改了未保存，newValue未改变，但是this.attrMapList[index]是改变的，需要重置数据
        // 对象形式，重新组装数据，用数组存起来
        this.json_schema = JSON.parse(
          this.customDeployModel[index].json_schema
        ).properties;

        this.attrMapList[index] = Object.keys(this.json_schema).map(item => {
          let getValue = this.customDeployModel[index].value
            ? JSON.parse(this.customDeployModel[index].value)[item]
            : '';
          let obj = {
            key: item,
            value: getValue,
            required: false
          };
          // 默认不必填，以下确定是否必填
          if (requiredArray && requiredArray.includes(item)) {
            obj.required = true;
          }
          // 将返回值的key作为label存起来
          Object.keys(this.json_schema).forEach(prop => {
            if (prop === item) {
              obj.label = this.json_schema[prop].description;
            }
          });
          return obj;
        });
        this.mapList = this.attrMapList[index];
        this.listDialogOpen = true;
        this.index = index;
      }
    },
    // 保存table弹窗编辑值
    saveTableDate(data) {
      this.tableDialogOpen = false;
      if (data.length === 0) {
        this.customDeployModel[this.index].value = '';
      } else {
        this.customDeployModel[this.index].value = JSON.stringify(data);
      }
    },
    // 保存list弹窗编辑值
    saveListDate(data) {
      this.listDialogOpen = false;
      this.customDeployModel[this.index].value = JSON.stringify(data);
    },
    // Cass部署环境选择框过滤
    async envListFilter(val, update, abort) {
      await this.getEnvList();
      const changedEnvList = this.formatEnv(this.newEnvList);
      update(() => {
        this.filterEnvList = changedEnvList.filter(
          tag =>
            !tag.labels.includes('scc') &&
            (tag.env_name.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
              tag.name_cn.indexOf(val) > -1)
        );
      });
    },
    // SCC环境选择框过滤
    async sccListFilter(val, update, abort) {
      await this.queryEnv({
        labels: ['scc']
      });
      update(() => {
        this.filterSccList = this.envType.filter(env => {
          return (
            env.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            env.name_cn.indexOf(val) > -1
          );
        });
      });
    },
    // 分支选择框过滤
    async branchFilter(val, update, abort) {
      await this.queryAllBranch({
        gitlab_project_id: this.gitlab_project_id.toString()
      });
      update(() => {
        this.filterBranchList = this.branchList.filter(tag => {
          return tag.toLowerCase().indexOf(val.toLowerCase()) > -1;
        });
      });
    },
    // 点击部署
    async handleCustomDeploy() {
      await this.comfirmTabsValue();
      this.$v.customDeployModel.$touch();
      this.$v.serchModel.$touch();
      let Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('customDeploy') > -1;
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.customDeployModel.$invalid || this.$v.serchModel.$invalid) {
        return;
      }
      let model = {};
      for (let i = 0; i < this.allCustomDepModels.length; i++) {
        Reflect.set(model, this.allCustomDepModels[i].env_name, {});
      }
      this.allCustomDepModels.forEach((arr, index) => {
        arr.value.forEach((item, ind) => {
          if (item.key) {
            Reflect.set(model[arr.env_name], item.key, item.value);
          }
        });
      });
      const params = {
        ref: this.serchModel.branch,
        variables: model,
        appId: this.id,
        config_update_flag: this.isUpdateSettingFile,
        re_deploy_flag: this.isUpdateImage
      };
      await this.definedDeploy(params);
      successNotify('部署成功!');
    },
    // 校验每一个tab中的必填属性是否已填
    comfirmTabsValue() {
      const valiFlag = [];
      // 遍历每一个环境的实体，若非加密属性的值为''，记录所有“必填且非加密属性的值为''”的环境
      this.allCustomDepModels.forEach((model, index) => {
        if (
          model.value.some(val =>
            val.value === '' &&
            val.key !== 'FDEV_CAAS_PWD' &&
            val.key !== 'FDEV_CAAS_REGISTRY_PASSWORD'
              ? true
              : false
          )
        ) {
          valiFlag.push(this.allCustomDepModels[index]);
        }
      });
      this.deployTip = valiFlag;
      // tab跳至第一个存在“必填且非加密属性的值为''”的环境
      if (this.deployTip.length !== 0) {
        this.tab = this.deployTip[0].env_name;
      }
    },
    // 加一个env_name的属性
    formatEnv(envList) {
      return envList.map(env => {
        return { ...env, env_name: env.name_en };
      });
    },
    // 处理环境实体值
    formatEnvModelVar(envModelVar) {
      return envModelVar.map(item => {
        return {
          name: item.name_zh,
          value: item.value,
          key: item.key,
          require: item.require,
          data_type: item.data_type,
          json_schema: item.json_schema
        };
      });
    },
    // 查询环境实体
    async queryEnvModel(newEnv, oldEnv) {
      // 页面初始，第一次选择环境，oldEnv为[]，此时newEnv中只有一个环境
      if (oldEnv.length === 0) {
        const allModels = [];
        await this.queryVarByEnvAndType({
          type: 'deploy',
          env: newEnv[0].env_name,
          gitlabId: this.gitlab_project_id
        });
        allModels.push({
          env_name: newEnv[0].env_name,
          value: this.formatEnvModelVar(this.envModelVar)
        });
        // 将查询的实体存入allCustomDepModels
        this.allCustomDepModels = deepClone(allModels);
        // 更新tabs
        this.tabs = this.allCustomDepModels;
        // 默认展示第一个tab页
        this.tab = this.tabs[0].env_name;
      }
      // Cass部署环境单选切换
      if (oldEnv.length !== 0 && oldEnv.length === newEnv.length) {
        this.updateEnvs = deepClone(this.allCustomDepModels);
        this.allCustomDepModels = [];
        // 找到切换后的新环境
        const nEnv = newEnv.filter(o =>
          oldEnv.every(p => !['env_name'].some(k => o[k] === p[k]))
        );
        const allModels = [];
        // 单选切换，查询切换后新环境实体
        await this.queryVarByEnvAndType({
          type: 'deploy',
          env: nEnv[0].env_name,
          gitlabId: this.gitlab_project_id
        });
        allModels.push({
          env_name: nEnv[0].env_name,
          value: this.formatEnvModelVar(this.envModelVar)
        });
        // 找到切换前的环境
        const oEnv = oldEnv.filter(o =>
          newEnv.every(p => !['env_name'].some(k => o[k] === p[k]))
        );
        // 更新allCustomDepModels
        this.updateEnvs.forEach((item, index) => {
          if (item.env_name === oEnv[0].env_name) {
            this.allCustomDepModels.push(allModels[0]);
          } else {
            this.allCustomDepModels.push(item);
          }
        });
        // 更新tabs
        this.tabs = this.allCustomDepModels;
        // 默认展示第一个tab页
        this.tab = this.tabs[0].env_name;
        this.updateEnvs = [];
      }
      // 选择框新增一个环境
      if (oldEnv.length !== 0 && oldEnv.length < newEnv.length) {
        // 找到新增环境的环境名
        const env = newEnv.filter(o =>
          oldEnv.every(p => !['env_name'].some(k => o[k] === p[k]))
        );
        const allModels = [];
        // 新增一个就会发一次接口，查询环境实体
        await this.queryVarByEnvAndType({
          type: 'deploy',
          env: env[0].env_name,
          gitlabId: this.gitlab_project_id
        });
        allModels.push({
          env_name: env[0].env_name,
          value: this.formatEnvModelVar(this.envModelVar)
        });
        // 更新allCustomDepModels，将新增环境的实体拼接进去
        this.allCustomDepModels = this.allCustomDepModels.concat(allModels);
        // 更新tabs
        this.tabs = this.allCustomDepModels;
        // 默认展示第一个tab页
        this.tab = this.tabs[0].env_name;
      }
      // 选择框删除一个环境
      if (newEnv.length !== 0 && oldEnv.length > newEnv.length) {
        this.deleteEnvs = deepClone(this.allCustomDepModels);
        this.allCustomDepModels = [];
        // 找到被删除环境的环境名
        const env = oldEnv.filter(o =>
          newEnv.every(p => !['env_name'].some(k => o[k] === p[k]))
        );
        // 更新allCustomDepModels，删掉env的实体
        this.deleteEnvs.forEach((item, index) => {
          if (item.env_name !== env[0].env_name) {
            this.allCustomDepModels.push(item);
          }
        });
        this.tabs =
          this.allCustomDepModels.length === 0 ? [] : this.allCustomDepModels;
        this.tab = this.tabs.length === 0 ? '' : this.tabs[0].env_name;
        this.deleteEnvs = [];
      }
    },
    // 拼接两个环境数组
    async initEnvTabs() {
      // 任一环境数组有值
      if (
        this.selectedSCCEnvs.length !== 0 ||
        this.selectedCaasEnvs.length !== 0
      ) {
        // 拼接两个数组组成新数组，用来保存已选环境
        this.allEnvs = this.selectedSCCEnvs.concat(this.selectedCaasEnvs);
        // 查询环境实体
        await this.queryEnvModel(this.allEnvs, this.tabs);
        this.hasEnvs = true;
        // 默认展示第一个tab页，customDeployModel用来存当前tab页环境的实体
        this.customDeployModel =
          this.allCustomDepModels.length === 0
            ? null
            : this.allCustomDepModels[0].value;
        this.dialogDate = this.customDeployModel;
        // 若当前tab页的环境无实体，则展示提示话术
        this.noValue = this.customDeployModel.length > 0 ? false : true;
      } else {
        // 没有选择环境时，tabs、tab都清空
        this.tabs = [];
        this.tab = '';
        this.hasEnvs = false;
      }
    },
    // 获取SCC环境
    getSCCEnv(env) {
      const SCCEnv = [];
      env.forEach((item, index) => {
        SCCEnv.push({
          env_name: item
        });
      });
      this.selectedSCCEnvs = deepClone(SCCEnv);
      this.initEnvTabs();
    },
    // 获取Caas环境
    getCaaSEnv(env) {
      const CassEnv = [];
      if (env) {
        CassEnv.push({
          env_name: env.env_name
        });
        this.selectedCaasEnvs = deepClone(CassEnv);
      } else {
        this.selectedCaasEnvs = [];
      }
      this.initEnvTabs();
    }
  }
};
</script>

<style lang="stylus" scoped>
.font
  color #757575
  font-size 14px
.hidden-input .input >>> input
  display none
</style>
