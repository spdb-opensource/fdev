<template>
  <div>
    <f-dialog v-model="dialogOpen" f-sc title="扫描">
      <!-- <fdev-dialog
      :value="value"
      transition-show="slide-up"
      transition-hide="slide-down"
      class="q-dialog-per"
      @input="$emit('input', $event)"
    > -->
      <form @submit.prevent="submit">
        <div class="q-gutter-y-diaLine">
          <f-formitem diaS label="应用名称">
            <p>{{ appName }}</p>
          </f-formitem>
          <f-formitem diaS label="扫描分支">
            <p v-if="branchName">{{ gitlab }}</p>
            <fdev-select
              v-else
              input-debounce="0"
              emit-value
              option-label="name"
              option-value="name"
              map-options
              @filter="filterFn"
              :options="gitlabBranch"
              v-model="gitlab"
              @input="changeGitlabBranch"
            />
          </f-formitem>
          <f-formitem diaS label="扫描类型">
            <fdev-select
              input-debounce="0"
              ref="scanInterfaceModel.type"
              emit-value
              map-options
              :options="scanType"
              option-label="label"
              option-value="value"
              v-model="scanInterfaceModel.type"
            />
          </f-formitem>
        </div>

        <!-- <fdev-btn
          color="primary"
          class="full-width q-mt-lg"
          :label="'扫描' + scanName"
          :loading="loading"
          @click="submit"
        /> -->
      </form>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          :label="'扫描' + scanName"
          :loading="loading"
          @click="submit"
        />
      </template>
    </f-dialog>
    <f-dialog v-model="sacnResOpen" title="扫描">
      <!-- <fdev-dialog
      v-model="sacnResOpen"
      transition-show="slide-up"
      transition-hide="slide-down"
      class="q-dialog-per bg-white"
    > -->
      <fdev-list class="bg-white">
        <fdev-item v-if="scanRes.success">
          <fdev-item-section avatar>
            <fdev-icon name="check_circle" color="teal" />
          </fdev-item-section>
          <fdev-item-section>
            <fdev-item-label class="text-h6">成功</fdev-item-label>
            <fdev-item-label v-for="item in successMsg" :key="item">
              {{ item }}!
            </fdev-item-label>
          </fdev-item-section>
        </fdev-item>

        <fdev-separator spaced inset v-if="scanRes.success && scanRes.error" />

        <fdev-item v-if="scanRes.error">
          <fdev-item-section avatar>
            <fdev-icon name="error" color="red" />
          </fdev-item-section>
          <fdev-item-section>
            <fdev-item-label class="text-h6">失败</fdev-item-label>
            <fdev-item-label
              class="q-mt-md"
              v-for="item in errorMsg"
              :key="item"
            >
              {{ item }}!
            </fdev-item-label>
          </fdev-item-section>
        </fdev-item>
      </fdev-list>
    </f-dialog>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import {
  scanInterfaceModel,
  scanWebList,
  scanCliList
} from '../utils/constants';
import { required } from 'vuelidate/lib/validators';
export default {
  name: 'ScanDialog',
  data() {
    return {
      loading: false,
      gitlab: this.branchName || 'master',
      scanInterfaceModel: scanInterfaceModel(),
      sacnResOpen: false
    };
  },
  validations: {
    scanInterfaceModel: {
      type: {
        required
      }
    }
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    appName: {
      type: String,
      default: ''
    },
    branchName: {
      type: String // 任务模块会传
    },
    appId: {
      type: String
    },
    id: {
      type: String
    }
  },
  watch: {
    branchName(val) {
      this.gitlab = val;
    }
  },
  computed: {
    ...mapState('interfaceForm', ['gitlabBranch', 'scanRes']),
    // 弹窗打开或关闭
    dialogOpen: {
      get() {
        return this.value;
      },
      set(val) {
        this.$emit('input', val);
      }
    },
    scanType() {
      /* 应用名包含-web-和-cli-，显示不同的扫描类型选项*/
      if (this.appName.includes('-cli-')) {
        return scanCliList;
      } else {
        return scanWebList;
      }
    },
    /* 如果应用名包含-web-,-cli-，显示扫描类型 */
    includesWeb() {
      if (this.appName) {
        return this.appName.includes('-web-') || this.appName.includes('-cli-');
      } else {
        return false;
      }
    },
    scanName() {
      return this.appName.includes('-cli-') ? '路由/交易' : '接口';
    },
    errorMsg() {
      if (!this.scanRes.error) {
        return [];
      } else {
        return this.scanRes.error.split('！').filter(item => {
          return item !== '';
        });
      }
    },
    successMsg() {
      if (!this.scanRes.success) {
        return [];
      } else {
        return this.scanRes.success.split('！').filter(item => {
          return item !== '';
        });
      }
    }
  },
  methods: {
    ...mapActions('interfaceForm', [
      'scanInterface',
      'getProjectBranchList',
      'taskScanInterface'
    ]),
    closeDialog() {
      this.dialogOpen = false;
    },
    async submit() {
      this.loading = true;
      /*只扫接口: '9' ,只扫交易: '0', 全部扫描: '90',应用名称不包含web:'124' */
      const params = {
        type: this.includesWeb ? this.scanInterfaceModel.type : '124',
        branch: this.gitlab,
        name_en: this.appName
      };
      try {
        if (this.branchName) {
          params.id = this.id;
          /* 任务模块要传任务id */
          await this.taskScanInterface(params);
          this.$emit('input', false);
        } else {
          /* 应用模块 */
          await this.scanInterface(params);
          this.$emit('click', this.appName.indexOf('web') > -1);
        }
        this.loading = false;
        this.sacnResOpen = true;
        /* 不是web的项目，应用扫描完刷新页面 */
      } catch (err) {
        this.loading = false;
      }
    },
    /* 应用：查询当前应用的分支 */
    async filterFn(val, update, abort) {
      await this.getProjectBranchList({ id: this.appId });
      update();
    },
    changeGitlabBranch(branchName) {
      this.$emit('change', branchName);
    }
  }
};
</script>

<style lang="stylus" scoped>
.dialog-height
  height 350px
  min-height 350px
  max-height 350px
.q-gutter-y-diaLine
  >>> p
    margin 0
</style>
