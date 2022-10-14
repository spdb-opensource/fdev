<template>
  <div>
    <f-dialog
      :value="value"
      @input="$emit('input', $event)"
      right
      f-sc
      :title="type === 'add' ? '添加' : '修改'"
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
                ref="esfCommonConfig.appName"
                use-input
                emit-value
                map-options
                option-label="name_zh"
                option-value="id"
                v-model="esfCommonConfig.appName"
                :options="appOptions"
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

            <!-- 应用sid -->
            <f-formitem label="应用sid" diaS required>
              <fdev-input
                :ref="esfCommonConfig.appSid"
                v-model="esfCommonConfig.appSid"
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
            <div v-if="item === 'PROCSH' || item === 'PROCHF'">
              <!-- K1桶名称-->
              <f-formitem diaS required label="K1桶名称">
                <fdev-input
                  :ref="'esfCommonConfig.bucketName' + item + 'K1'"
                  v-model="esfCommonConfig[item].bucketNameK1"
                  :rules="[val => !!val || '请输入桶名称']"
                />
              </f-formitem>

              <!-- K2桶名称-->
              <f-formitem diaS required label="K2桶名称">
                <fdev-input
                  :ref="'esfCommonConfig.bucketName' + item + 'K2'"
                  v-model="esfCommonConfig[item].bucketNameK2"
                  :rules="[val => !!val || '请输入桶名称']"
                />
              </f-formitem>

              <!-- 上传文件 -->
              <f-formitem
                diaS
                required
                label="选择文件"
                help="可按住ctrl键选择多个文件，不同环境目录需选择同名文件。"
              >
                <fdev-file
                  :ref="'esfCommonConfig.files' + item"
                  outlined
                  use-chips
                  multiple
                  clearable
                  v-model="esfCommonConfig[item].files"
                  :rules="[
                    val => !!val || '请选择文件',
                    val => val.length > 0 || '请选择文件'
                  ]"
                />
              </f-formitem>
            </div>
            <div v-else>
              <!-- 桶名称 -->
              <f-formitem label="桶名称" diaS required>
                <fdev-input
                  :ref="'esfCommonConfig.bucketName' + item"
                  v-model="esfCommonConfig[item].bucketName"
                  :rules="[val => !!val || '请输入桶名称']"
                />
              </f-formitem>

              <!-- 上传文件 -->
              <f-formitem
                label="选择文件"
                help="可按住ctrl键选择多个文件，不同环境目录需选择同名文件。"
                diaS
                required
              >
                <fdev-file
                  :ref="'esfCommonConfig.files' + item"
                  outlined
                  use-chips
                  multiple
                  clearable
                  v-model="esfCommonConfig[item].files"
                  :rules="[
                    val => !!val || '请选择文件',
                    val => val.length > 0 || '请选择文件'
                  ]"
                />
              </f-formitem>
            </div>
          </div>
        </div>
      </div>

      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          label="确定"
          @click="submit"
          :loading="
            globalLoading['releaseForm/addEsfCommonConfig'] ||
              globalLoading['releaseForm/updateEsfcommonconfigAssets']
          "
        />
      </template>
    </f-dialog>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import { deepClone, errorNotify, successNotify } from '@/utils/utils';
import { initEsfCommonConfigData } from '../../../utils/constants';
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
      appOptions: null,
      esfCommonConfig: initEsfCommonConfigData()
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
        this.esfCommonConfig = initEsfCommonConfigData();
      }
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('appForm', ['vueAppData'])
  },
  methods: {
    ...mapActions('appForm', ['queryApps']),
    ...mapActions('releaseForm', [
      'addEsfCommonConfig',
      'updateEsfcommonconfigAssets'
    ]),

    // 编辑时，编辑时初始化各字段值
    initEditData(data) {
      this.esfCommonConfig = initEsfCommonConfigData();
      this.esfCommonConfig.appName = {
        name_zh: data.source_application_name,
        id: data.source_application
      };
      this.esfCommonConfig.appSid = data.sid;
      data.assets.forEach(item => {
        this.esfCommonConfig[item.runtime_env].bucketName = item.bucket_name;
      });
    },

    // 比较两个相同长度的字符串数组内容是否相同（顺序可以不同）
    isSameContent(arr1, arr2) {
      let flagArr = [];
      arr1.forEach(item => {
        arr2.includes(item) ? flagArr.push(true) : flagArr.push(false);
      });
      return !flagArr.includes(false);
    },

    // 应用输入过滤
    async appFilter(val, update, abort) {
      const updateCb = () => {
        const needle = val.toLowerCase().trim();
        this.appOptions = this.vueAppData.filter(
          v =>
            v.name_zh.toLowerCase().includes(needle) ||
            v.name_en.toLowerCase().includes(needle)
        );
      };
      if (this.appOptions) {
        update(updateCb);
        return;
      }
      await this.queryApps();
      this.appOptions = deepClone(this.vueAppData);
      update(updateCb);
    },

    // 点击确认，提交表单
    submit() {
      // 校验表单
      let keys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.startsWith('esfCommonConfig');
      });
      Promise.all(
        keys.map(ele => {
          if (Array.isArray(this.$refs[ele])) {
            return this.$refs[ele][0].validate() || Promise.reject(ele);
          } else {
            return this.$refs[ele].validate() || Promise.reject(ele);
          }
        })
      ).then(
        async res => {
          // 校验上传的文件文件名相同
          // 去除应用名
          let { appName, appSid, ...envConfig } = this.esfCommonConfig;
          // 校验各环境上传的文件数量相同
          const filesCount = envConfig.DEV.files.length;
          for (let env in envConfig) {
            if (envConfig[env].files.length !== filesCount) {
              // 各环境上传文件的文件数量不同
              errorNotify('请上传同名文件');
              return;
            }
          }

          // 如果各环境上传的文件数量相同，收集各环境上传的文件名
          let filesName = {};
          for (let env in envConfig) {
            filesName[env] = [];
            for (let i = 0; i < filesCount; i++) {
              filesName[env][i] = envConfig[env].files[i].name;
            }
          }

          // 校验各环境上传的文件名相同-字符串数组内容相同
          const filesNameArr = filesName.DEV;
          let sameFileName = false;
          for (let env in filesName) {
            if (!this.isSameContent(filesNameArr, filesName[env])) {
              sameFileName = false;
              errorNotify('请上传同名文件');
              return;
            } else {
              sameFileName = true;
            }
          }
          if (sameFileName) {
            const formData = new FormData();
            formData.append('asset_catalog_name', 'esfcommonconfig');
            formData.append('prod_id', this.$route.params.id);
            formData.append(
              'source_application',
              this.esfCommonConfig.appName.id
                ? this.esfCommonConfig.appName.id
                : this.esfCommonConfig.appName
            );
            const envArr = ['DEV', 'TEST', 'PROCSH', 'PROCHF'];
            envArr.forEach(envItem => {
              for (let fileItem = 0; fileItem < filesCount; fileItem++) {
                if (envItem === 'DEV' || envItem === 'TEST') {
                  formData.append(
                    'file',
                    this.esfCommonConfig[envItem].files[fileItem],
                    filesName[envItem][fileItem] +
                      `/${envItem}` +
                      `/${this.esfCommonConfig[envItem].bucketName}` +
                      `/${this.esfCommonConfig.appSid}`
                  );
                }
                if (envItem === 'PROCSH' || envItem === 'PROCHF') {
                  formData.append(
                    'file',
                    this.esfCommonConfig[envItem].files[fileItem],
                    filesName[envItem][fileItem] +
                      `/${envItem}` +
                      `/${this.esfCommonConfig[envItem].bucketNameK1}` +
                      `,${this.esfCommonConfig[envItem].bucketNameK2}` +
                      `/${this.esfCommonConfig.appSid}`
                  );
                }
              }
            });
            if (this.type === 'add') {
              await this.addEsfCommonConfig(formData);
              successNotify('新增成功');
            } else if (this.type === 'edit') {
              await this.updateEsfcommonconfigAssets(formData);
              successNotify('修改成功');
            }
            this.$emit('confirm');
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
