<template>
  <f-dialog :value="value" title="创建分支" @input="$emit('input', $event)">
    <f-formitem label="应用名称">
      <fdev-select
        use-input
        ref="jobModel.application"
        v-model="$v.jobModel.application.$model"
        :options="applications"
        @filter="applicationFilter"
        :rules="[val => $v.jobModel.application.required || '请选择应用名称']"
      >
        <template v-slot:option="scope">
          <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
            <fdev-item-section>
              <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
              <fdev-item-label>{{ scope.opt.name_zh }}</fdev-item-label>
            </fdev-item-section>
          </fdev-item>
        </template>
      </fdev-select>
    </f-formitem>

    <f-formitem label="feature分支" :label-col="4">
      <fdev-input
        ref="jobModel.feature"
        v-model="$v.jobModel.feature.$model"
        type="text"
        :rules="[
          val => $v.jobModel.feature.required || '请输入feature分支',
          val => $v.jobModel.feature.noWord || '不能输入中文/中文字符'
        ]"
      />
    </f-formitem>

    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        label="确认"
        @click="confirmAddBranch"
        :loading="globalLoading['jobForm/addBranch']"
      />
    </template>
  </f-dialog>
</template>

<script>
import { validate, formatOption, successNotify } from '@/utils/utils';
import { mapState, mapActions } from 'vuex';
import { required } from 'vuelidate/lib/validators';
export default {
  props: {
    id: String,
    value: Boolean
  },
  data() {
    return {
      jobModel: {
        application: null,
        feature: ''
      },
      addBranchOpen: true,
      applications: [],
      applicationOptions: []
    };
  },
  validations: {
    jobModel: {
      application: {
        required
      },
      feature: {
        required,
        noWord(val) {
          if (!val) {
            return true;
          }
          let reg = new RegExp(
            /[\u4e00-\u9fa5]|[\\，\\？\\；\\《\\》\\。\\（\\）\\；\\”\\“\\！\\‘\\’\\、\\【\\】\\－]/gm
          );
          return !reg.test(val);
        }
      }
    }
  },
  computed: {
    ...mapState('appForm', {
      appList: 'vueAppData'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    })
  },
  methods: {
    ...mapActions('appForm', {
      queryApps: 'queryApps'
    }),
    ...mapActions('jobForm', {
      addBranch: 'addBranch'
    }),
    async confirmAddBranch() {
      if (!this.validateJob()) {
        return;
      }
      let addBranchModel = {
        id: this.id,
        feature_branch: this.jobModel.feature,
        app: {
          id: this.jobModel.application.id
        }
      };
      await this.addBranch(addBranchModel);
      successNotify('分支创建成功');
      this.$emit('input', false);
      //刷新详情页面
      this.$emit('finished', 'addBranchFlag');
    },
    async applicationFilter(val, update, abort) {
      update(() => {
        this.applications = this.applicationOptions.filter(
          application =>
            application.label.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            application.name_zh.indexOf(val) > -1
        );
      });
    },
    validateJob() {
      this.$v.jobModel.$touch();
      let branchKeys = Object.keys(this.$refs).filter(
        key => key.indexOf('jobModel') > -1
      );
      validate(branchKeys.map(key => this.$refs[key]));
      if (this.$v.jobModel.$invalid) {
        return false;
      } else {
        return true;
      }
    }
  },
  async created() {
    await this.queryApps();
    this.applications = formatOption(this.appList, 'name_en');
    this.applicationOptions = this.applications.slice(0);
  }
};
</script>
