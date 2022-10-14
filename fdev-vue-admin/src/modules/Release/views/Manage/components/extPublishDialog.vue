<template>
  <f-dialog
    :value="value"
    :title="title"
    right
    @input="$emit('input', $event)"
    @before-close="clearForm"
  >
    <f-formitem label="应用" required bottom-page label-style="width:94px;">
      <fdev-select
        input-debounce="0"
        option-label="application_name_cn"
        v-model="dialogModel.selectApp"
        ref="dialogModel.selectApp"
        :options="appOptions"
        use-input
        map-options
        emit-value
        :disable="isEdit"
        @filter="appFilter"
        :rules="[val => !!val || '请选择应用']"
      >
        <template v-slot:option="scope">
          <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
            <fdev-item-section>
              <fdev-item-label :title="scope.opt.application_name_cn">{{
                scope.opt.application_name_cn
              }}</fdev-item-label>
              <fdev-item-label caption :title="scope.opt.application_name_en">
                {{ scope.opt.application_name_en }}
              </fdev-item-label>
            </fdev-item-section>
          </fdev-item>
        </template>
      </fdev-select>
    </f-formitem>
    <f-formitem label="组别" required bottom-page label-style="width:94px;">
      <fdev-select
        v-model="dialogModel.jobGroup"
        emit-value
        map-options
        :disable="isEdit"
        ref="dialogModel.jobGroup"
        :options="groupOptions"
        :rules="[val => !!val || '请选择组别']"
      >
      </fdev-select>
    </f-formitem>
    <f-formitem label="类型" required bottom-page label-style="width:94px;">
      <fdev-select
        v-model="dialogModel.type"
        emit-value
        map-options
        :disable="isEdit"
        ref="dialogModel.type"
        :options="typeOptions"
        :rules="[val => !!val || '请选择类型']"
      >
      </fdev-select>
    </f-formitem>
    <f-formitem
      required
      label="服务注册中心注册的名称"
      bottom-page
      label-style="width:94px;"
    >
      <fdev-input
        v-model="dialogModel.executorId"
        ref="dialogModel.executorId"
        :rules="[val => !!val || '请输入服务注册中心注册的名称']"
      />
    </f-formitem>
    <f-formitem required label="交易名" bottom-page label-style="width:94px;">
      <fdev-input
        v-model="dialogModel.transName"
        ref="dialogModel.transName"
        :rules="[val => !!val || '请输入交易名']"
      />
    </f-formitem>
    <f-formitem
      required
      label="交易中文名"
      v-if="
        dialogModel.type === 'httpAddJob' ||
          dialogModel.type === 'httpUpdateJob'
      "
      bottom-page
      label-style="width:94px;"
    >
      <fdev-input
        v-model="dialogModel.description"
        ref="dialogModel.description"
        :rules="[val => !!val || '请输入交易中文名']"
      />
    </f-formitem>
    <f-formitem
      required
      label="触发规则"
      v-if="
        dialogModel.type === 'httpAddJob' ||
          dialogModel.type === 'httpUpdateJob'
      "
      bottom-page
      label-style="width:94px;"
    >
      <fdev-input
        v-model="dialogModel.cronExpression"
        ref="dialogModel.cronExpression"
        :rules="[val => !!val || '请输入触发规则']"
      />
    </f-formitem>
    <f-formitem
      bottom-page
      label-style="width:94px;"
      v-if="dialogModel.type === 'httpTriggerJob'"
      :required="!!date || !!time"
      label="一次性任务触发日期"
    >
      <f-date
        ref="dialogModel.date"
        :rules="[val => validateDate(val) || '请输选择一次性任务触发日期']"
        v-model="date"
      />
    </f-formitem>

    <f-formitem
      bottom-page
      label-style="width:94px;"
      :required="!!date || !!time"
      label="一次性任务触发时间"
      v-if="dialogModel.type === 'httpTriggerJob'"
    >
      <el-time-picker
        ref="time"
        v-model="time"
        format="HH:mm:ss"
        value-format="HH:mm:ss"
        class="release-pre-publish-time"
      >
      </el-time-picker>
      <div v-if="!validTime && !time && date" class="error-area">
        请选择一次性任务触发时间
      </div>
      <div v-else class="error-area"></div>
    </f-formitem>
    <f-formitem
      label="miss补跑策略"
      v-if="
        dialogModel.type === 'httpAddJob' ||
          dialogModel.type === 'httpUpdateJob'
      "
      required
      bottom-page
      label-style="width:94px;"
    >
      <fdev-select
        v-model="dialogModel.misfireInstr"
        emit-value
        map-options
        ref="dialogModel.misfireInstr"
        :options="missOptions"
        :rules="[val => !!val || '请选择miss补跑策略']"
      >
      </fdev-select>
    </f-formitem>
    <template v-slot:btnSlot>
      <fdev-btn @click="handleCancel" outline dialog label="取消" />
      <fdev-btn @click="submit" dialog label="确定" />
    </template>
  </f-dialog>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { deepClone } from '@/utils/utils';
import { extPublishDialogModel } from '../../../utils/model';
import {
  missOptions,
  typeOptions,
  groupOptions
} from '../../../utils/constants';
export default {
  name: 'SelectPro',
  data() {
    return {
      validTime: true,
      date: '',
      time: '',
      dialogModel: extPublishDialogModel(),
      groupOptions: groupOptions,
      typeOptions: typeOptions,
      missOptions: missOptions
    };
  },
  props: {
    value: {
      type: Boolean
    },
    title: {
      type: String
    },
    options: {
      type: Array
    },
    releaseNodeName: {
      type: String
    },
    noteId: {
      type: String
    },
    rowData: {
      type: Object
    }
  },
  computed: {
    ...mapState('releaseForm', {
      image_tags: 'image_tags',
      imageTagResult: 'imageTagResult'
    }),
    isEdit() {
      return this.title === '编辑批量任务';
    }
  },
  methods: {
    ...mapActions('releaseForm', ['createBatchTask', 'updateBatchTask']),
    handleCancel() {
      this.clearForm();
      this.$emit('click', true);
    },
    clearForm() {
      this.time = '';
      this.date = '';
      this.dialogModel = extPublishDialogModel();
    },
    appFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase().trim();
        this.appOptions = this.options.filter(
          v =>
            v.application_name_cn.toLowerCase().indexOf(needle) > -1 ||
            v.application_name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    async submit() {
      let formKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('dialogModel') > -1;
      });
      return Promise.all(
        formKeys.map(ele => this.$refs[ele].validate() || Promise.reject(ele))
      ).then(
        async v => {
          this.validTime = false;
          if (
            !this.time &&
            this.date &&
            this.dialogModel.type === 'httpTriggerJob'
          ) {
            return;
          }
          if (
            this.dialogModel.type === 'httpTriggerJob' &&
            this.date &&
            this.time
          ) {
            this.dialogModel.fireTime = this.date + ' ' + this.time;
          } else if (
            (this.dialogModel.type === 'httpTriggerJob' && !this.date) ||
            !this.time
          ) {
            this.dialogModel.fireTime = '';
          }
          const params = { ...this.dialogModel };
          params.release_node_name = this.releaseNodeName;
          params.application_name_cn = this.dialogModel.selectApp.application_name_cn;
          params.application_name_en = this.dialogModel.selectApp.application_name_en;
          params.application_id = this.dialogModel.selectApp.application_id;

          if (this.isEdit) {
            params.id = this.id;
            await this.updateBatchTask(params);
          } else {
            params.note_id = this.noteId;
            delete params.batchInfo;
            await this.createBatchTask(params);
          }
          this.$emit('click');
          this.validTime = true;
        },
        reason => {
          if (reason === 'dialogModel.date') {
            this.$refs[reason].$children[0].focus();
          } else {
            this.$refs[reason].focus();
          }
        }
      );
    },
    validateDate(val) {
      if (!this.time && !val) {
        return true;
      } else {
        return !!val;
      }
    },
    handlerData(val) {
      const tempVal = deepClone(val);
      this.id = tempVal.id;
      this.dialogModel.type = tempVal.type;
      this.dialogModel.executorId = tempVal.executorId;
      this.dialogModel.transName = tempVal.transName;
      this.dialogModel.jobGroup = tempVal.jobGroup;
      this.dialogModel.description = tempVal.description;
      this.dialogModel.misfireInstr = tempVal.misfireInstr;
      this.dialogModel.cronExpression = tempVal.cronExpression;
      this.dialogModel.fireTime = tempVal.fireTime;
      this.dialogModel.selectApp = {
        application_id: tempVal.application_id,
        application_name_en: tempVal.application_name_en,
        application_name_cn: tempVal.application_name_cn
      };
      if (tempVal.type === 'httpTriggerJob') {
        const dateTimeArray = tempVal.fireTime.split(' ');
        this.date = dateTimeArray[0];
        this.time = dateTimeArray[1];
      }
    }
  },

  created() {
    this.appOptions = this.options;
    if (this.isEdit) {
      this.handlerData(this.rowData);
    }
  },
  watch: {
    options(val) {
      if (val) {
        this.appOptions = val;
      } else {
        this.appOptions = [];
      }
    },
    'dialogModel.type': {
      handler(val) {
        if (!this.isEdit) {
          this.dialogModel.cronExpression = '';
          this.dialogModel.description = '';
          this.dialogModel.fireTime = '';
          this.dialogModel.transName = '';
          this.dialogModel.misfireInstr = '';
          this.dialogModel.executorId = '';
          this.date = '';
          this.time = '';
          this.id = '';
        }
      }
    }
  }
};
</script>

<style lang="stylus" scoped>
.error-area
  color red
  font-size 10px
  margin-top -24px
/deep/ .el-input__inner
  height 36px
  margin-bottom 20px
  width 326px
  border 1px solid #bbb
/deep/ .el-icon-circle-close
  position absolute
  right -154px
  top -8px
</style>
<style>
.el-time-panel {
  z-index: 9999 !important;
}
.el-icon-time {
  height: 36px;
  line-height: normal;
}
</style>
