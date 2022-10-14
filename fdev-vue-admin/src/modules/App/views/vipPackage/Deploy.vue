<template>
  <f-block>
    <div class="row justify-center">
      <f-formitem label="应用">
        <fdev-select
          use-input
          ref="customDeploy.name_zh"
          v-model="$v.serchModel.name_en.$model"
          :options="appOptions"
          @filter="appListFilter"
          option-label="name_zh"
          option-value="name_en"
          :rules="[() => !$v.serchModel.name_en.$error || '应用名不能为空']"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.name_zh">
                  {{ scope.opt.name_zh }}
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
    <div class="row justify-center">
      <f-formitem label="分支" v-show="serchModel.name_en">
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
      <f-formitem label="环境">
        <fdev-select
          use-input
          ref="customDeploy.env"
          v-model="$v.serchModel.env.$model"
          :options="filterEnvList"
          @filter="envListFilter"
          option-label="env_name"
          :option-value="opt => opt"
          :rules="[() => !$v.serchModel.env.$error || '环境不能为空']"
          @input="queryEnvModel"
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
    <fdev-separator />
    <div class="row q-mt-md justify-center">
      <div class="col">
        <f-formitem
          v-for="(item, index) in $v.customDeployModel.$each.$iter"
          :label="item.name.$model"
          :key="index"
          class="hidden-input"
          style="justify-content: center;"
        >
          <fdev-input
            v-model="item.value.$model"
            :ref="`customDeploy${index}`"
            class="input"
            :rules="[() => item.value.need || `请输入${item.name.$model}`]"
            :readonly="isReadonly(item.$model.key)"
            v-if="
              item.$model.data_type ? item.$model.data_type !== 'string' : false
            "
          >
            <template v-slot:append>
              <fdev-btn
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
                item.$model.data_type === 'array' && item.$model.value !== ''
              "
            >
              <fdev-chip
                square
                color="gray"
                class="relative-chip"
                text-color="black"
              >
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
                      <span v-if="valkey !== 'index' && valkey !== '__index'">
                        {{ valkey }}：{{ val }}
                      </span>
                    </p>
                  </div>
                </fdev-tooltip>
              </fdev-chip>
              <span
                v-if="JSON.parse(item.$model.value).length > 1"
                class="relative-chip"
              >
                ...
              </span>
            </div>
            <fdev-chip
              square
              v-if="
                item.$model.data_type === 'object' && item.$model.value !== ''
              "
              color="gray"
              class="relative-chip"
              text-color="black"
            >
              {{ item.$model.key.toLowerCase() }}
              <fdev-tooltip anchor="top middle" self="center middle">
                <div class="tooltip">
                  <p
                    v-for="(val, valkey, i) in JSON.parse(item.$model.value)"
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
            :readonly="isReadonly(item.$model.key)"
            v-else
          >
          </fdev-input>
        </f-formitem>
      </div>
    </div>
    <div class="row justify-center q-gutter-x-lg q-mt-md">
      <fdev-btn
        label="部署"
        :loading="globalLoading['appForm/runPipeline']"
        @click="handleCustomDeploy"
      />
      <fdev-btn label="返回" @click="goBack" dialog />
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
  </f-block>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import { validate, deepClone, successNotify } from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import tableDialog from '@/modules/App/components/tableDialog';
import listDialog from '@/modules/App/components/listDialog';

export default {
  name: 'AppDeploy',
  components: {
    tableDialog,
    listDialog
  },
  data() {
    return {
      serchModel: {
        name_en: null,
        env: null,
        branch: ''
      },
      gitlab_project_id: '',
      id: '',
      customDeployModel: null,
      appOptions: [],
      filterEnvList: [],
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
      attrMapKey: []
    };
  },
  validations: {
    serchModel: {
      name_en: {
        required
      },
      env: {
        required
      },
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
    }
  },
  watch: {
    'serchModel.env': {
      handler(value, oldValue) {
        const selectValue = deepClone(value.env_variables);
        this.customDeployModel = Object.entries(selectValue).map(item => {
          return { key: item[0], value: item[1], name: item[0] };
        });
      }
    },
    async 'serchModel.name_en'(val) {
      if (val) {
        this.serchModel.branch = '';
        this.serchModel.env = null;
        this.customDeployModel = false;
        await this.queryAllBranch({
          gitlab_project_id: val.gitlab_project_id.toString()
        });
        this.filterEnvList = this.branchList;
      }
    }
  },
  computed: {
    ...mapState('appForm', ['branchList', 'withEnvAppData', 'runPipelineInfo']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('environmentForm', {
      newEnvList: 'envList',
      envModelVar: 'envModelVar'
    })
  },
  methods: {
    ...mapActions('appForm', {
      queryAllBranch: 'queryAllBranch',
      createPipeline: 'createPipeline',
      queryWithEnv: 'queryWithEnv',
      runPipeline: 'runPipeline'
    }),
    ...mapActions('environmentForm', {
      getEnvList: 'getEnvList',
      queryVarByEnvAndType: 'queryVarByEnvAndType'
    }),
    isReadonly(val) {
      if (val === 'FDEV_CAAS_REGISTRY_PASSWORD' || val === 'FDEV_CAAS_PWD') {
        return true;
      } else {
        return false;
      }
    },
    async envListFilter(val, update, abort) {
      await this.getEnvList();
      const changedEnvList = this.formatEnv(this.newEnvList);
      update(() => {
        this.filterEnvList = changedEnvList.filter(tag => {
          return (
            tag.env_name.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            tag.name_cn.indexOf(val) > -1
          );
        });
      });
    },
    //应用过滤
    async appListFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.appOptions = this.withEnvAppData.filter(v => {
          //请求回来的应用列表
          return (
            v.name_en.toLowerCase().indexOf(needle) > -1 ||
            v.name_zh.indexOf(val) > -1
          );
        });
      });
    },
    async branchFilter(val, update, abort) {
      update(() => {
        this.filterBranchList = this.branchList.filter(tag => {
          return tag.toLowerCase().indexOf(val.toLowerCase()) > -1;
        });
      });
    },
    async handleCustomDeploy() {
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
      let model = [];
      model = this.customDeployModel.map(item => {
        return { value: item.value, key: item.key };
      });
      let newModel = [
        ...model,
        {
          value: this.serchModel.env.env_name,
          key: 'CI_ENVIRONMENT_SLUG'
        }
      ];
      const params = {
        ref: this.serchModel.branch,
        variables: newModel,
        id: this.serchModel.name_en.id
      };
      await this.runPipeline(params);
      successNotify('部署成功!');
      const param = {
        nameEn: this.serchModel.name_en,
        id: this.serchModel.name_en.id,
        nameZh: this.serchModel.name_en.name_zh
      };
      sessionStorage.setItem('deployInfo', JSON.stringify(param));
      this.$router.push('/app/appVipList');
    },
    formatEnv(envList) {
      return envList.map(env => {
        return { ...env, env_name: env.name_en };
      });
    },
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
    async queryEnvModel(env) {
      await this.queryVarByEnvAndType({
        type: 'deploy',
        env: env.name_en,
        gitlabId: this.serchModel.name_en.gitlab_project_id
      });
      this.customDeployModel = this.formatEnvModelVar(this.envModelVar);
      this.dialogDate = this.customDeployModel;
    },
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
    saveTableDate(data) {
      this.tableDialogOpen = false;
      if (data.length === 0) {
        this.customDeployModel[this.index].value = '';
      } else {
        this.customDeployModel[this.index].value = JSON.stringify(data);
      }
    },
    saveListDate(data) {
      this.listDialogOpen = false;
      this.customDeployModel[this.index].value = JSON.stringify(data);
    }
  },
  async created() {
    await this.queryWithEnv();
    this.appOptions = this.withEnvAppData;
  },
  beforeRouteEnter(to, from, next) {
    const { params } = from;
    if (Object.keys(params).length === 0) {
      if (params) {
        sessionStorage.removeItem('deployInfo');
      }
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    const { params } = to;
    if (Object.keys(params).length) {
      sessionStorage.setItem(
        'deployInfo',
        JSON.stringify({
          nameEn: this.serchModel.name_en.name_en,
          gitlab_project_id: this.serchModel.name_en.gitlab_project_id,
          nameZh: this.serchModel.name_en.name_zh
        })
      );
    }
    next();
  }
};
</script>

<style lang="stylus" scoped>
.hidden-input .input >>> input
  display none
.relative-chip
  position relative
  top 5px
</style>
