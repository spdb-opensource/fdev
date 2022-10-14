<template>
  <f-dialog
    v-model="dialogOpen"
    right
    f-sc
    title="接口调用申请"
    :maximized="!$q.platform.is.desktop"
  >
    <div :class="$q.platform.is.desktop ? 'q-px-lg' : ''">
      <form>
        <f-formitem diaS label="调用方">
          <fdev-select
            use-input
            input-debounce="0"
            ref="applyCallModel.serviceCalling"
            v-model="applyCallModel.serviceCalling"
            :options="applications"
            @filter="applicationFilter"
            option-label="name_en"
            option-value="name_en"
            emit-value
            map-options
            hint=""
            :rules="[
              () => !$v.applyCallModel.serviceCalling.$error || '请选择调用方'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                  <fdev-item-label caption>{{
                    scope.opt.name_zh
                  }}</fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem diaS label="申请理由">
          <fdev-input
            ref="applyCallModel.reason"
            v-model="$v.applyCallModel.reason.$model"
            type="textarea"
            class="input"
            :rules="[
              () => !$v.applyCallModel.reason.$error || '申请理由不能为空'
            ]"
          />
        </f-formitem>
      </form>
    </div>
    <template v-slot:btnSlot>
      <fdev-btn label="取消" outline dialog @click="closeDialog" />
      <fdev-btn
        dialog
        label="申请"
        @click="handleApplyCallModel"
        :loading="globalLoading['interfaceForm/interfaceCallRequest']"
      />
    </template>
  </f-dialog>
</template>

<script>
import { createApplyCallModel } from '../utils/constants';
import { required } from 'vuelidate/lib/validators';
import { mapState, mapActions } from 'vuex';
import { formatOption, validate, successNotify } from '@/utils/utils';
export default {
  name: 'ApplyCallDialog',
  props: ['value', 'selector'],
  data() {
    return {
      applyCallModel: createApplyCallModel(),
      applications: [],
      applicationOptions: []
    };
  },
  validations: {
    applyCallModel: {
      serviceCalling: {
        required
      },
      reason: {
        require(val) {
          if (!val) {
            return false;
          } else {
            return val.trim().length > 0;
          }
        }
      }
    }
  },
  computed: {
    ...mapState('appForm', {
      appList: 'vueAppData'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('interfaceForm', {
      provideManagers: 'provideManagers'
    }),
    // 弹窗打开或关闭
    dialogOpen: {
      get() {
        return this.value;
      },
      set(val) {
        this.$emit('input', val);
      }
    }
  },
  watch: {
    async value(val) {
      if (val) {
        await this.queryApps();

        this.applications = formatOption(this.appList, 'name_en');
        this.applicationOptions = this.applications.slice(0);
      } else {
        this.applyCallModel = createApplyCallModel();
      }
    }
  },
  methods: {
    ...mapActions('appForm', {
      queryApps: 'queryApps'
    }),
    ...mapActions('interfaceForm', {
      interfaceCallRequest: 'interfaceCallRequest'
    }),
    async applicationFilter(val, update, abort) {
      update(() => {
        this.applications = this.applicationOptions.filter(
          application =>
            application.label.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            application.name_zh.indexOf(val) > -1
        );
      });
    },
    async handleApplyCallModel() {
      this.$v.applyCallModel.$touch();
      let applyCallModelKeys = Object.keys(this.$refs).filter(
        key => key.indexOf('applyCallModel') > -1
      );
      validate(applyCallModelKeys.map(key => this.$refs[key]));
      if (this.$v.applyCallModel.$invalid) {
        return;
      }
      this.applyCallModel.transId = this.selector.transId;
      this.applyCallModel.applicant = this.currentUser.user_name_cn;
      this.applyCallModel.serviceId = this.selector.serviceId;
      await this.interfaceCallRequest(this.applyCallModel);
      successNotify(`调用申请发送成功`);
      this.$emit('input', false);
      this.$q.dialog({
        title: `联系人信息`,
        message: `接口调用申请已发出，您可联系以下负责人进行审批：${
          this.provideManagers.provideManagers
        }`,
        ok: '我知道了',
        cancel: true
      });
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
    },
    // 关闭弹窗
    closeDialog() {
      this.dialogOpen = false;
    }
  }
};
</script>
<style lang="stylus" scoped>
.q-layout-padding
  padding 16px 26px 16px 20px
.dialog-height
  height 350px !important
</style>
