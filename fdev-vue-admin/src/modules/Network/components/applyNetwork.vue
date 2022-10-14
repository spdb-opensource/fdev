<template>
  <f-dialog
    persistent
    title="KF网络开通申请"
    :value="isOpen"
    @input="closeDialog"
  >
    <f-formitem diaS required label="使用人">
      <fdev-select
        ref="netWorkModel.applyUser"
        use-input
        @filter="filterUser"
        :options="userOptions"
        option-label="user_name_cn"
        option-value="id"
        v-model="netWorkModel.applyUser"
        :rules="[val => !!val || '请选择使用人']"
      >
        <template v-slot:option="scope">
          <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
            <fdev-item-section>
              <fdev-item-label>{{ scope.opt.user_name_cn }}</fdev-item-label>
              <fdev-item-label caption>
                {{ scope.opt.user_name_en }}
              </fdev-item-label>
            </fdev-item-section>
          </fdev-item>
        </template>
      </fdev-select>
    </f-formitem>
    <f-formitem label="手机型号:" required diaS>
      <fdev-input
        ref="netWorkModel.phone_type"
        v-model="netWorkModel.phone_type"
        :rules="[val => !!val || '请输入手机型号']"
      />
    </f-formitem>
    <f-formitem label="手机mac地址:" required diaS>
      <fdev-input
        ref="netWorkModel.phone_mac"
        v-model="netWorkModel.phone_mac"
        :rules="[val => !!val || '请输入手机mac地址']"
      />
    </f-formitem>
    <f-formitem label="是否为行内测试设备:" required diaS>
      <fdev-select
        ref="netWorkModel.is_spdb_mac"
        v-model="netWorkModel.is_spdb_mac"
        :options="isSpdbMacOptions"
        option-label="label"
        option-value="value"
        map-options
        emit-value
        :rules="[val => !!val || '请选择是否为行内测试设备']"
    /></f-formitem>
    <template v-slot:btnSlot>
      <fdev-btn label="取消" outline dialog @click="closeDialog"/>
      <fdev-btn label="确定" dialog @click="applySure"
    /></template>
  </f-dialog>
</template>

<script>
import { mapGetters, mapActions } from 'vuex';
import { isSpdbMacOptions } from '@/modules/User/utils/model';
import { successNotify } from '@/utils/utils';
export default {
  name: 'applyNetwork',
  props: {
    isOpen: {
      default: false,
      type: Boolean
    },
    dailogType: {
      default: '',
      type: String
    }
  },
  data() {
    return {
      netWorkModel: {
        applyUser: '',
        phone_type: '',
        phone_mac: '',
        is_spdb_mac: ''
      },
      isSpdbMacOptions: isSpdbMacOptions
    };
  },
  computed: {
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    })
  },
  methods: {
    ...mapActions('networkForm', ['openKF', 'closeKF']),
    closeDialog() {
      this.netWorkModel.applyUser = '';
      this.netWorkModel.phone_type = '';
      this.netWorkModel.phone_mac = '';
      this.netWorkModel.is_spdb_mac = '';
      this.$emit('close');
    },
    applySure() {
      let formKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('netWorkModel') > -1;
      });
      return Promise.all(
        formKeys.map(ele => this.$refs[ele].validate() || Promise.reject(ele))
      ).then(
        async v => {
          await this.openKF({
            user_id: this.netWorkModel.applyUser,
            phone_type: this.netWorkModel.phone_type,
            phone_mac: this.netWorkModel.phone_mac,
            is_spdb_mac: this.netWorkModel.is_spdb_mac
          });
          successNotify('KF网络申请成功，请等待网络审核员审核!');
        },
        reason => {
          this.$refs[reason].focus();
        }
      );
    },

    filterUser(val, update, abort) {
      if (val === '') {
        update(() => {
          this.userOptions = this.userList;
        });
      }
      update(() => {
        const needle = val.toLowerCase();
        this.userOptions = this.userList.filter(
          v =>
            v.user_name_cn.indexOf(needle) > -1 ||
            v.user_name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    }
  },
  created() {
    this.userOptions = this.userList;
  }
};
</script>

<style></style>
