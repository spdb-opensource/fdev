<template>
  <f-dialog
    right
    :value="extreConfigDialogOpened"
    title="环境配置参数（优先生效）"
    @input="$emit('update:extreConfigDialogOpened', false)"
  >
    <div class="row justify-between q-mb-md " v-if="!noWrite">
      <div class="float-left q-gutter-x-sm title-center">
        <f-icon name="list_s_f" class="text-primary" :width="14" :height="14" />
        <span>环境配置参数表</span>
      </div>
      <fdev-btn normal label="添加" ficon="add" @click="handleAddExtraConfig()">
        <fdev-tooltip position="left">
          优先参数对于分支的配置模板中已存在参数进行覆盖，且只能在测试环境使用
        </fdev-tooltip>
      </fdev-btn>
    </div>
    <fdev-markup-table bordered flat>
      <thead>
        <tr>
          <th class="width-30">环境</th>
          <th class="width-30">应用key</th>
          <th class="width-30">配置值</th>
          <th class="width-10">操作</th>
        </tr>
      </thead>
    </fdev-markup-table>
    <div class="q-mt-md">
      <div
        class="row"
        v-for="(item, index) in $v.extraConfigMsg.variables.$each.$iter"
        :key="index"
      >
        <div class="row width-30 justify-center">
          <fdev-select
            class="td-width-30"
            v-model="item.env_name.$model"
            :options="filterEnvList"
            option-label="name_en"
            option-value="name_en"
            emit-value
            use-input
            @filter="filterEnv"
            map-options
            :readonly="noWrite"
            :ref="`extraConfigMsg.variables.${index}.env_name`"
            :rules="[
              () => item.env_name.required || '请选择环境',
              () => item.env_name.isUniquePair || '应用key和环境配对重复配置'
            ]"
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
        </div>
        <div class="row width-30 justify-center">
          <fdev-input
            class="td-width-30"
            placeholder="请输入应用key"
            v-model="item.appkey.$model"
            :ref="`extraConfigMsg.variables.${index}.appkey`"
            :readonly="noWrite"
            :rules="[
              () => item.appkey.required || '请输入应用key',
              () => item.appkey.isUniquePair || '应用key和环境重复配置'
            ]"
          />
        </div>
        <div class="row width-30 justify-center">
          <fdev-input
            class="td-width-30"
            placeholder="请输入配置值"
            v-model="item.value.$model"
            :readonly="noWrite"
            :ref="`extraConfigMsg.variables.${index}.value`"
            :rules="[() => item.value.required || '请输入配置值']"
          />
        </div>
        <div v-if="!noWrite" class="row width-10 justify-end">
          <fdev-btn
            class="td-width-10"
            :disable="noWrite"
            flat
            color="red"
            ficon="delete"
            @click="handleDeleteExtraConfig(index)"
          />
          <fdev-tooltip v-if="noWrite"
            >只有应用/行内应用负责人可操作</fdev-tooltip
          >
        </div>
      </div>
    </div>
    <div class="div-msg" v-if="extraConfigMsg.variables.length === 0">
      <f-icon name="alert_t_f" class="warn"></f-icon>
      <span>没有可用数据</span>
    </div>
    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        label="保存"
        v-if="!noWrite"
        color="primary"
        @click="handleSaveExtraConfigParam()"
      />
    </template>
  </f-dialog>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { successNotify, validate } from '@/utils/utils';
import { createExtraConfigMsg } from '@/modules/App/utils/constants';
import { required } from 'vuelidate/lib/validators';

export default {
  name: 'ExtreConfigParam',
  data() {
    return {
      extraConfigMsg: createExtraConfigMsg(),
      filterEnvList: []
    };
  },
  validations: {
    extraConfigMsg: {
      variables: {
        $each: {
          appkey: {
            required,
            isUniquePair(val, obj) {
              let value = val.replace(/(^\s*)|(\s*$)/g, '');
              if (!value || !obj.env_name) {
                return true;
              }
              let pair = this.extraConfigMsg.variables.filter(item => {
                let appkey = item.appkey.replace(/(^\s*)|(\s*$)/g, '');
                return appkey === value && item.env_name === obj.env_name;
              });
              return pair.length <= 1;
            }
          },
          env_name: {
            required,
            isUniquePair(val, obj) {
              let appkey = obj.appkey.replace(/(^\s*)|(\s*$)/g, '');
              if (!val || !obj.appkey) {
                return true;
              }
              let pair = this.extraConfigMsg.variables.filter(item => {
                let secondAppkey = item.appkey.replace(/(^\s*)|(\s*$)/g, '');
                return item.env_name === val && secondAppkey === appkey;
              });
              return pair.length <= 1;
            }
          },
          value: {
            required
          }
        }
      }
    }
  },
  props: {
    extreConfigDialogOpened: {
      type: Boolean
    },
    noWrite: {
      type: Boolean
    },
    project_id: {
      type: String
    },
    extraConfig: {
      type: Object
    },
    branch: {
      type: String
    }
  },
  computed: {
    ...mapState('environmentForm', {
      envs: 'envList'
    }),
    envList() {
      return this.envs.filter(env => !env.labels.includes('pro'));
    }
  },
  watch: {
    async extreConfigDialogOpened(val) {
      if (val) {
        await this.getEnvList();
        this.filterEnvList = this.envList;
        if (this.extraConfig) {
          this.extraConfigMsg = this.extraConfig;
        }
      }
    }
  },
  methods: {
    ...mapActions('environmentForm', {
      getEnvList: 'getEnvList'
    }),
    ...mapActions('jobForm', {
      addExtraConfigParam: 'addExtraConfigParam'
    }),
    sendExtreConfigMsg() {
      this.$emit('listenExtreConfigEvent');
    },
    async handleSaveExtraConfigParam() {
      this.$v.extraConfigMsg.$touch();
      let Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('extraConfigMsg') > -1;
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.extraConfigMsg.$invalid) {
        return;
      }
      this.extraConfigMsg.project_id = this.project_id;
      this.extraConfigMsg.branch = this.branch;
      await this.addExtraConfigParam(this.extraConfigMsg);
      successNotify('保存成功');
      this.sendExtreConfigMsg();
    },
    handleAddExtraConfig() {
      this.extraConfigMsg.variables.push({
        appkey: '',
        env_name: '',
        value: ''
      });
    },
    handleDeleteExtraConfig(index) {
      return this.$q
        .dialog({
          title: `预删除`,
          message: `仅为预删除，点击'保存'后生效`,
          ok: '我知道了',
          cancel: '取消'
        })
        .onOk(async () => {
          this.extraConfigMsg.variables.splice(index, 1);
        });
    },
    filterEnv(val, update) {
      const needle = val.toLowerCase();
      update(() => {
        this.filterEnvList = this.envList.filter(
          v =>
            v.name_en.toLowerCase().includes(needle) ||
            v.name_cn.includes(needle)
        );
      });
    }
  }
};
</script>

<style lang="stylus" scoped>
.title-center
  align-self center
.width-30
  width 180px
.width-10
  width 60px
.td-width-30
  width 160px
.td-width-10
  width 40px
  padding 0 0 20px 20px
</style>
