<template>
  <f-dialog :value="value" @input="$emit('input', $event)" :title="title">
    <f-formitem required label="投产大窗口日期" diaS>
      <f-date
        :options="releaseDateOptions"
        v-model="releaseDialogModel.release_date"
        mask="YYYY-MM-DD"
        :rules="[
          () =>
            $v.releaseDialogModel.release_date.required ||
            '请选择投产大窗口日期'
        ]"
      />
    </f-formitem>

    <f-formitem required label="牵头联系人" diaS>
      <fdev-select
        input-debounce="0"
        use-input
        multiple
        @filter="releaseContactFilter"
        :options="releaseContactOptions"
        option-label="user_name_cn"
        option-value="user_name_en"
        emit-value
        map-options
        ref="releaseDialogModel.release_contact"
        v-model="releaseDialogModel.release_contact"
        :rules="[
          () =>
            $v.releaseDialogModel.release_contact.required || '请选择牵头联系人'
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

    <f-formitem required label="牵头小组" diaS>
      <fdev-select
        use-input
        input-debounce="0"
        v-model="releaseDialogModel.owner_groupId"
        :options="groupOptions"
        @filter="groupFilter"
        option-value="id"
        option-label="name"
        map-options
        emit-value
        ref="releaseDialogModel.owner_groupId"
        :rules="[
          () => $v.releaseDialogModel.owner_groupId.required || '请选择牵头小组'
        ]"
      >
        <template v-slot:option="scope">
          <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
            <fdev-item-section>
              <fdev-item-label :title="scope.opt.name">{{
                scope.opt.name
              }}</fdev-item-label>
              <fdev-item-label caption :title="scope.opt.fullName">
                {{ scope.opt.fullName }}
              </fdev-item-label>
            </fdev-item-section>
          </fdev-item>
        </template>
      </fdev-select>
    </f-formitem>
    <template v-slot:btnSlot>
      <fdev-btn label="确定" :loading="loading" dialog @click="validateForm"
    /></template>
  </f-dialog>
</template>

<script>
import moment from 'moment';
import { releaseDialogModel } from '../../../utils/model';
import { mapState, mapActions, mapGetters } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import { validate } from '@/utils/utils';
export default {
  data() {
    return {
      releaseDialogModel: releaseDialogModel(),
      releaseContactOptions: [],
      groupOptions: [],
      selectedDate: []
    };
  },
  validations: {
    releaseDialogModel: {
      release_date: {
        required
      },
      release_contact: {
        required
      },
      owner_groupId: {
        required
      }
    }
  },
  props: {
    value: Boolean,
    title: String,
    data: Object,
    loading: Boolean
  },
  computed: {
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
    ...mapState('userForm', ['groups']),
    ...mapGetters('releaseForm', ['existingDate'])
  },
  watch: {
    value(newVal, oldVal) {
      if (newVal) {
        this.handleDialogOpen();
      }
    }
  },
  methods: {
    ...mapActions('user', ['fetch']),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('releaseForm', ['queryBigReleaseNodes']),
    releaseDateOptions(date) {
      const today = moment(new Date()).format('YYYY/MM/DD');
      return (
        !this.selectedDate.includes(date.replace(/\//g, '-')) && date >= today
      );
    },
    releaseContactFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.releaseContactOptions = this.userList.filter(
          v =>
            v.user_name_cn.includes(needle) ||
            v.user_name_en.toLowerCase().includes(needle)
        );
      });
    },
    groupFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.groupOptions = this.groups.filter(v => v =>
          v.fullName.includes(needle)
        );
      });
    },
    handleDialogOpen() {
      const { release_contact, release_date, owner_groupId } = this.data;
      this.releaseDialogModel = {
        release_date,
        old_release_date: release_date ? release_date : undefined,
        owner_groupId,
        release_contact: release_contact.map(user => {
          return user.user_name_en;
        })
      };
      this.fetchGroup().then(() => {
        this.groupOptions = this.groups;
      });
      this.fetch().then(() => {
        this.releaseContactOptions = this.userList;
      });
      this.querySelectedDate();
    },
    handleDialog() {
      this.$emit('submit', this.releaseDialogModel);
    },
    validateForm() {
      this.$v.releaseDialogModel.$touch();

      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('releaseDialogModel') > -1;
      });

      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );

      if (this.$v.releaseDialogModel.$invalid) {
        return;
      }

      this.handleDialog();
    },
    async querySelectedDate() {
      await this.queryBigReleaseNodes({
        start_date: moment(new Date()).format('YYYY-MM-DD')
      });
      this.selectedDate = [...this.existingDate];
      const index = this.selectedDate.indexOf(this.data.release_date);
      if (index > -1) {
        this.selectedDate.splice(index, 1);
      }
    }
  }
};
</script>

<style lang="stylus" scoped></style>
