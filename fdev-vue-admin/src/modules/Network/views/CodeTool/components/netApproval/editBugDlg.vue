<template>
  <f-dialog
    :value="value"
    @input="$emit('input', $event)"
    persistent
    right
    dense
    title="编辑问题项"
  >
    <div>
      <f-formitem
        label="问题描述/评审建议"
        required
        bottom-page
        label-style="width:119px"
      >
        <fdev-input
          v-model="$v.createModel.problem.$model"
          ref="createModel.problem"
          type="textarea"
          :rules="[
            () => $v.createModel.problem.required || '问题描述/评审建议'
          ]"
        />
      </f-formitem>
      <f-formitem
        label="问题类型"
        bottom-page
        required
        label-style="width:119px"
      >
        <fdev-select
          ref="createModel.problemType"
          :options="problemTypeOptions"
          v-model="$v.createModel.problemType.$model"
          :rules="[
            () => $v.createModel.problemType.required || '请选择问题类型'
          ]"
        />
      </f-formitem>
      <f-formitem label="问题项" bottom-page label-style="width:119px">
        <fdev-select
          :options="itemTypeOptions"
          v-model="createModel.itemType"
          option-label="value"
          option-value="id"
          hint=""
        />
      </f-formitem>
      <f-formitem
        label="问题次数"
        bottom-page
        required
        label-style="width:119px"
      >
        <fdev-input
          v-model="$v.createModel.problemNum.$model"
          ref="createModel.problemNum"
          :rules="[
            () => $v.createModel.problemNum.required || '请输入问题次数',
            () => $v.createModel.problemNum.integer || '只能输入数字',
            v => v > 0 || '请输入大于0的数字',
            v => !fn(v) || '输入的数字不规范'
          ]"
        />
      </f-formitem>
      <f-formitem label="是否修复" bottom-page label-style="width:119px">
        <fdev-select
          ref="createModel.fixFlag"
          :options="fixFlagOptions"
          v-model="createModel.fixFlag"
          @input="handleFixDate"
          hint=""
        />
      </f-formitem>

      <f-formitem
        v-if="showFixData == true"
        label="修复日期"
        bottom-page
        label-style="width:119px"
      >
        <f-date
          ref="createModel.fixDate"
          mask="YYYY-MM-DD"
          v-model="createModel.fixDate"
          hint=""
        />
      </f-formitem>
      <f-formitem label="备注" bottom-page label-style="width:119px">
        <fdev-input
          ref="createModel.remark"
          type="textarea"
          hint=""
          v-model="createModel.remark"
        />
      </f-formitem>
    </div>
    <template v-slot:btnSlot>
      <fdev-btn outline dialog label="取消" @click="confirmToClose" />
      <fdev-btn dialog label="确定" :loading="loading" @click="submitForm" />
    </template>
  </f-dialog>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import { successNotify, validate } from '@/utils/utils';
import { required, integer } from 'vuelidate/lib/validators';
import {
  createProblemItemModel,
  priorityOptions
} from '@/modules/Network/utils/constants';
export default {
  name: 'updateEvaMgtDlg',
  data() {
    return {
      createModel: createProblemItemModel(),
      users: [], //牵头人员
      userOptions: [], //牵头人选项
      groups: [], //牵头小组,
      groupOptions: [], //牵头小组下拉选项
      priorityOptions: priorityOptions,
      detailObj: {},
      problemTypeOptions: [
        { label: '缺陷', value: 'issue' },
        { label: '风险', value: 'risk' },
        { label: '建议', value: 'advice' }
      ],
      fixFlagOptions: [
        { label: '已修复', value: 'fixed' },
        { label: '未修复', value: 'notFixed' }
      ],
      itemTypeOptions: [],
      loading: false,
      problemId: '',
      showFixData: false
    };
  },
  validations: {
    createModel: {
      problem: {
        required
      },
      problemType: {
        required
      },
      problemNum: {
        required,
        integer
      }
    }
  },
  props: {
    getParams: {
      type: Object
    },
    value: {
      type: Boolean
    },
    overdueCalOptions: {
      type: Array
    }
  },
  watch: {
    value(val) {
      if (val) {
        this.createModel = createProblemItemModel();
        this.getBugDetails();
      }
    }
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('networkForm', ['problemItem']),
    ...mapState('userForm', {
      groupsData: 'groups'
    })
  },
  methods: {
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('networkForm', ['queryProblemItem', 'updateProblem']),
    verifyModel() {
      try {
        let unitModuleKeys = Object.keys(this.$refs).filter(key => {
          return this.$refs[key] && key.indexOf('createModel') > -1;
        });
        validate(
          unitModuleKeys.map(key => {
            if (this.$refs[key] instanceof Array) {
              return this.$refs[key][0];
            }
            if (
              this.$refs[key].$children.length > 0 &&
              this.$refs[key].$children[0].$children.length > 0 &&
              this.$refs[key].$children[0].validate
            ) {
              return this.$refs[key].$children[0].validate();
            }
            return this.$refs[key].validate();
          })
        );
        const _this = this;
        if (this.$v.createModel.$invalid) {
          const validateRes = unitModuleKeys.every(item => {
            if (item.indexOf('.') === -1) {
              return true;
            }
            const itemArr = item.split('.');
            return !_this.$v.createModel[itemArr[1]].$invalid;
          });
          if (!validateRes) {
            return false;
          }
        }
      } catch (error) {
        return false;
      }
      return true;
    },
    handleFixDate($event) {
      if ($event && $event.value === 'fixed') {
        this.showFixData = true;
      } else {
        this.showFixData = false;
        this.createModel.fixDate = '';
      }
    },
    submitForm() {
      // if (!this.verifyModel()) {
      //   return;
      // }
      let formKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('createModel') > -1;
      });
      return Promise.all(
        formKeys.map(ele => {
          if (
            this.$refs[ele].$children.length > 0 &&
            this.$refs[ele].$children[0].$children.length > 0 &&
            this.$refs[ele].$children[0].validate
          ) {
            return (
              this.$refs[ele].$children[0].validate() || Promise.reject(ele)
            );
          }
          return this.$refs[ele].validate() || Promise.reject(ele);
        })
      ).then(
        async v => {
          this.saveEvaMgt();
        },
        reason => {
          if (
            this.$refs[reason].$children.length > 0 &&
            this.$refs[reason].$children[0].$children.length > 0 &&
            this.$refs[reason].$children[0].validate
          ) {
            this.$refs[reason].$children[0].focus();
          } else {
            this.$refs[reason].focus();
          }
        }
      );
      // this.saveEvaMgt();
    },
    fn(v) {
      // 不规范返回true
      return (
        String(v)
          .split('.')[0]
          .slice(0, 1) === '0' && String(v).split('.')[0].length > 1
      );
    },
    // 编辑
    async saveEvaMgt() {
      let params = {
        id: this.problemId, //问题id
        problemType: this.createModel.problemType.value, //问题类型
        problem: this.createModel.problem, //问题描述
        itemType: this.createModel.itemType && this.createModel.itemType.key, //问题项
        problemNum: Number(this.createModel.problemNum), //问题次数
        fixFlag: this.createModel.fixFlag ? this.createModel.fixFlag.value : '', //是否修复
        fixDate: this.createModel.fixDate, //修复日期
        remark: this.createModel.remark //备注
      };
      this.loading = true;
      await this.updateProblem(params);
      successNotify('修改问题项成功!');
      this.loading = false;
      this.$emit('close', true);
    },
    confirmToClose() {
      this.$emit('close', false);
    },
    getBugDetails() {
      this.problemId = this.getParams.id;
      this.createModel.problemType = this.getProblemTypeItem(
        this.getParams.problemType
      );
      this.createModel.problem = this.getParams.problem;
      this.createModel.problemNum = this.getParams.problemNum;
      this.createModel.fixDate = this.getParams.fixDate;
      if (this.getParams.fixDate) {
        this.showFixData = true;
      } else {
        this.showFixData = false;
      }
      this.createModel.remark = this.getParams.remark;
      //是否修复初始化赋值
      this.createModel.fixFlag = this.getFixFlagItem(this.getParams.fixFlag);
      //问题项初始化赋值
      this.createModel.itemType = this.getitemTypeItem(this.getParams.itemType);
    },
    getItemById(id, lists) {
      for (let i = 0; i < lists.length; i++) {
        if (id === lists[i].id) {
          return lists[i];
        }
      }
      return null;
    },
    getProblemTypeItem(val) {
      for (let i = 0; i < this.problemTypeOptions.length; i++) {
        if (val == this.problemTypeOptions[i].value) {
          return this.problemTypeOptions[i];
        }
      }
      return null;
    },
    getFixFlagItem(val) {
      for (let i = 0; i < this.fixFlagOptions.length; i++) {
        if (val == this.fixFlagOptions[i].value) {
          return this.fixFlagOptions[i];
        }
      }
      return null;
    },
    getitemTypeItem(val) {
      for (let i = 0; i < this.itemTypeOptions.length; i++) {
        if (val == this.itemTypeOptions[i].key) {
          return this.itemTypeOptions[i];
        }
      }
      return null;
    }
  },
  async created() {
    this.itemTypeOptions = this.problemItem;
    this.getBugDetails();
  }
};
</script>

<style lang="stylus" scoped>

.dialog-wrapper
  margin-top 50px
  box-sizing border-box
  max-height calc(100vh - 98px)
  overflow auto
.file-wrapper
  display inline-block
  width calc(100% - 20px)
  margin 0
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  vertical-align: bottom;
.icon
  cursor pointer
  padding 3px
  border-radius 50%
.icon:hover
  background #BBBBBB
.task-type{
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
}
</style>
