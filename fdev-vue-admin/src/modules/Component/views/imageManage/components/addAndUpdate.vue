<template>
  <f-dialog
    right
    f-sc
    :title="operation === 'update' ? '修改镜像' : '镜像录入'"
    :value="value"
    @input="$emit('input', $event)"
    @shake="confirmToClose"
  >
    <div>
      <!-- 镜像英文名 -->
      <f-formitem required diaS label="镜像英文名">
        <fdev-input
          ref="baseImageModel.name"
          :readonly="operation === 'update'"
          v-model="$v.baseImageModel.name.$model"
          :rules="[
            () => $v.baseImageModel.name.required || '请输入镜像英文名',
            () => $v.baseImageModel.name.examine || '只能输入英文、数字、.-_'
          ]"
        />
      </f-formitem>

      <!-- 镜像中文名 -->
      <f-formitem required diaS label="镜像中文名">
        <fdev-input
          ref="baseImageModel.name_cn"
          v-model="$v.baseImageModel.name_cn.$model"
          :rules="[
            () => $v.baseImageModel.name_cn.required || '请输入镜像中文名',
            () => $v.baseImageModel.name_cn.examine || '至少包含一个中文'
          ]"
        />
      </f-formitem>

      <!-- 镜像管理员 -->
      <f-formitem required diaS label="镜像管理员">
        <fdev-select
          multiple
          use-input
          emit-value
          map-options
          :option-value="opt => opt"
          :options="managerOptions"
          option-label="user_name_cn"
          ref="baseImageModel.manager"
          @filter="managerFilter"
          v-model="$v.baseImageModel.manager.$model"
          :rules="[
            () => $v.baseImageModel.manager.required || '请选择镜像管理员'
          ]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.user_name_cn">
                  {{ scope.opt.user_name_cn }}
                </fdev-item-label>
                <fdev-item-label caption :title="scope.opt.user_name_en">
                  {{ scope.opt.user_name_en }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>

      <!-- 所属小组 -->
      <f-formitem label="所属小组" diaS required>
        <fdev-select
          use-input
          :option-value="opt => opt"
          :options="groupOptions"
          option-label="fullName"
          ref="baseImageModel.groupObj"
          @filter="groupsFilter"
          v-model="$v.baseImageModel.groupObj.$model"
          :rules="[
            () => $v.baseImageModel.groupObj.required || '请选择所属小组'
          ]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.name">
                  {{ scope.opt.name }}
                </fdev-item-label>
                <fdev-item-label caption :title="scope.opt.fullName">
                  {{ scope.opt.fullName }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>

      <!-- 镜像类型 -->
      <f-formitem required diaS label="镜像类型">
        <fdev-select
          emit-value
          map-options
          :options="imageTypeOptions"
          option-label="label"
          option-value="value"
          ref="baseImageModel.type"
          v-model="$v.baseImageModel.type.$model"
          :rules="[() => $v.baseImageModel.type.required || '请选择镜像类型']"
        />
      </f-formitem>

      <!-- 目标环境 -->
      <f-formitem required diaS label="目标环境">
        <fdev-select
          emit-value
          map-options
          :options="targetEnvOptions"
          option-label="label"
          option-value="value"
          ref="baseImageModel.target_env"
          v-model="$v.baseImageModel.target_env.$model"
          :rules="[
            () => $v.baseImageModel.target_env.required || '请选择目标环境'
          ]"
        />
      </f-formitem>

      <!-- gitlab地址 -->
      <f-formitem required diaS label="gitlab地址">
        <fdev-input
          ref="baseImageModel.gitlab_url"
          :readonly="operation === 'update'"
          v-model="$v.baseImageModel.gitlab_url.$model"
          :rules="[
            () => $v.baseImageModel.gitlab_url.required || '请输入gitlab地址',
            () => $v.baseImageModel.gitlab_url.examine || 'gitlab地址格式不正确'
          ]"
        />
      </f-formitem>

      <!-- 是否涉及内测 -->
      <f-formitem diaS label="是否涉及内测" class="edit-form-cells">
        <fdev-radio
          val="1"
          v-model="$v.baseImageModel.isTest.$model"
          label="是"
        />
        <fdev-radio
          val="0"
          v-model="$v.baseImageModel.isTest.$model"
          label="否"
          class="q-ml-lg"
        />
      </f-formitem>

      <!-- wiki地址 -->
      <f-formitem diaS label="wiki地址">
        <fdev-input
          ref="baseImageModel.wiki"
          v-model="$v.baseImageModel.wiki.$model"
        />
      </f-formitem>

      <!-- 添加元数据 -->
      <div class="addmetadata q-gutter-md row items-center">
        <span>添加元数据</span>
        <fdev-btn dialog flat @click="addNew" ficon="add_c_o" />
        <fdev-btn
          dialog
          flat
          @click="delMetadata"
          :disable="Object.keys(this.updateMetaList).length <= 0"
          ficon="substract_r_o"
        />
      </div>

      <div
        class="meta-dev"
        v-for="(item, index) in $v.updateMetaList.$each.$iter"
        :key="index"
      >
        <f-formitem diaS label="key">
          <fdev-input
            v-model="item.key.$model"
            :ref="`updateMetaList-${index}-key`"
            :rules="[
              () => item.key.repeat || 'key值不能重复',
              () => item.key.required || 'value值不为空时,key值也不能为空'
            ]"
          />
        </f-formitem>
        <f-formitem diaS label="value">
          <fdev-input
            v-model="item.value.$model"
            :ref="`updateMetaList-${index}-value`"
            :rules="[
              () => item.value.required || 'key值不为空时,value值也不能为空'
            ]"
          />
        </f-formitem>
      </div>

      <!-- 镜像描述 -->
      <f-formitem required diaS label="镜像描述">
        <fdev-input
          type="textarea"
          ref="baseImageModel.description"
          v-model="$v.baseImageModel.description.$model"
          :rules="[
            () => $v.baseImageModel.description.required || '请输入镜像描述'
          ]"
        />
      </f-formitem>
    </div>

    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        label="确定"
        :loading="
          globalLoading[
            'componentForm/' +
              (operation === 'update' ? 'updateBaseImage' : 'addBaseImage')
          ]
        "
        @click="handleOptimizeAllTip"
      />
    </template>
  </f-dialog>
</template>

<script>
import {
  baseImageModel,
  imageTypeOptions,
  targetEnvOptions
} from '@/modules/Component/utils/constants.js';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify, deepClone } from '@/utils/utils';
import { mapGetters, mapState, mapActions } from 'vuex';
export default {
  name: 'AddImageDialog',
  data() {
    return {
      id: '',
      baseImageModel: baseImageModel(),
      imageTypeOptions: imageTypeOptions,
      managerOptions: [],
      groupOptions: [],
      targetEnvOptions: targetEnvOptions,
      updateMetaList: [],
      showLoading: false
    };
  },
  validations: {
    baseImageModel: {
      name: {
        required,
        examine(val) {
          if (!val) {
            return true;
          }
          const reg = /^[\w.-]+$/;
          return reg.test(val);
        }
      },
      name_cn: {
        required,
        examine(val) {
          if (!val) {
            return true;
          }
          const reg = /[\u4E00-\u9FA5]/gm;
          return reg.test(val);
        }
      },
      gitlab_url: {
        required,
        examine(val) {
          if (!val) {
            return true;
          }
          const reg = /^http:\/\/([\w\-.:/%]*)([\w-])$/;
          return reg.test(val);
        }
      },
      wiki: {},
      isTest: {},
      manager: {
        required
      },
      type: {
        required
      },
      groupObj: {
        required
      },
      target_env: {
        required
      },
      description: {
        required
      }
    },
    updateMetaList: {
      $each: {
        value: {
          required(val, item) {
            if (item.key) {
              return !!val.trim();
            }
            return true;
          }
        },
        key: {
          repeat(val) {
            let arr = this.updateMetaList.filter(res => {
              return val.trim() && res.key === val;
            });
            return arr.length <= 1;
          },
          required(val, item) {
            if (item.value) {
              return !!val.trim();
            }

            return true;
          }
        }
      }
    }
  },

  props: {
    value: Boolean,
    operation: {
      type: String
    },
    data: Object
  },
  watch: {
    async value(val) {
      this.showLoading = true;
      this.groupOptions = this.groups;
      this.baseImageModel = baseImageModel();
      if (this.operation === 'update') {
        this.baseImageModel = deepClone(this.data);
      }
      await this.fetch();
      this.getMetaData();
      this.showLoading = false;
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState({
      groups(state) {
        return state.userForm.groups.map(group => {
          return {
            ...group
          };
        });
      }
    }),
    ...mapState('componentForm', ['baseImageDetail']),
    ...mapGetters('user', ['isLoginUserList']),
    userList() {
      return this.isLoginUserList.map(user => {
        return {
          user_name_cn: user.user_name_cn,
          id: user.id,
          user_name_en: user.user_name_en
        };
      });
    }
  },
  methods: {
    ...mapActions('componentForm', [
      'addBaseImage',
      'updateBaseImage',
      'queryBaseImageDetail'
    ]),
    ...mapActions('user', ['fetch']),
    /* 提交优化 */
    async getMetaData() {
      if (this.operation === 'update') {
        await this.init();
        this.updateMetaList = Object.keys(
          this.baseImageDetail.meta_data_declare
        ).map(item => {
          return {
            key: item,
            value: this.baseImageDetail.meta_data_declare[item]
          };
        });
      }
    },
    async init() {
      await this.queryBaseImageDetail({
        id: this.$route.params.id
      });
    },
    handleOptimizeAllTip() {
      this.$v.baseImageModel.$touch();
      this.$v.updateMetaList.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return (
          key.indexOf('baseImageModel') > -1 ||
          key.indexOf('updateMetaList') > -1
        );
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.baseImageModel.$invalid || this.$v.updateMetaList.$invalid) {
        return;
      }
      this.handleOptimize();
    },
    async handleOptimize() {
      let metaDataObj = {};
      this.updateMetaList.map((res, index) => {
        if (res.key.trim()) {
          metaDataObj[res.key] = res.value;
        }
      });
      let formObj = {
        ...this.baseImageModel,
        group: this.baseImageModel.groupObj.id,
        meta_data_declare: {
          ...metaDataObj
        }
      };
      if (this.baseImageModel.wiki.trim() === '') {
        delete formObj.wiki;
      }
      if (Object.keys(metaDataObj).length === 0) {
        delete formObj.meta_data_declare;
      }
      if (this.operation === 'update') {
        await this.updateBaseImage(formObj);
        successNotify('修改基础镜像成功');
        await this.init();
      } else {
        delete formObj.id;
        await this.addBaseImage(formObj);
        successNotify('新增基础镜像成功');
      }
      this.$emit('refresh', this.value);
      this.$emit('input', false);
    },
    delMetadata(key) {
      this.updateMetaList.pop();
    },
    managerFilter(val, update) {
      const needle = val.toLowerCase();
      update(() => {
        this.managerOptions = this.userList.filter(
          user =>
            user.user_name_cn.indexOf(needle) > -1 ||
            user.user_name_en.toLowerCase().indexOf(needle) > -1
        );
        if (this.managerOptions.length === 0) {
          this.managerOptions = this.userList;
        }
      });
    },
    groupsFilter(val, update) {
      const needle = val.toLowerCase();
      update(() => {
        this.groupOptions = this.groups.filter(
          group => group.fullName.indexOf(needle) > -1
        );
      });
    },
    addNew() {
      this.updateMetaList.push({ key: '', value: '' });
    },
    confirmToClose() {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this.$emit('input', false);
        });
    }
  }
};
</script>
<style lang="stylus" scoped>
.edit-form-cells
  padding-bottom 20px
.submit-div
  margin-top 20px !important
.addmetadata
  margin: 8px 0
.addmetadata-btn
  margin: 0 8px
.meta-dev
  margin-bottom 6px
</style>
