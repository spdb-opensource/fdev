<template>
  <f-dialog :value="value" @input="$emit('input', $event)" title="编辑">
    <f-formitem label="excel模板" diaS v-if="detail.image_deliver_type === '1'">
      <fdev-select
        input-debounce="0"
        ref="updateChanges.template"
        :options="templateOfChange"
        option-label="filename"
        use-input
        @filter="templateFilter"
        v-model="$v.updateChanges.template.$model"
        :rules="[() => !$v.updateChanges.template.$error || '请选择变更模板']"
      />
    </f-formitem>
    <f-formitem label="预期变更时间" diaS>
      <el-time-picker
        ref="newChangeModel.plan_time"
        v-model="$v.updateChanges.plan_time.$model"
        format="HH:mm"
        value-format="HH:mm"
      >
      </el-time-picker>
    </f-formitem>
    <f-formitem diaS label="变更单号">
      <fdev-input
        input-debounce="0"
        ref="updateChanges.prod_spdb_no"
        type="text"
        v-model="$v.updateChanges.prod_spdb_no.$model"
        :rules="[
          () => $v.updateChanges.prod_spdb_no.required || '请输入变更编号',
          () =>
            $v.updateChanges.prod_spdb_no.onlyNumOrWord ||
            '只能由英文字母或数字组成'
        ]"
      />
    </f-formitem>
    <template v-slot:btnSlot>
      <fdev-btn @click="handleUpdate" dialog label="确认" :loading="loading" />
    </template>
  </f-dialog>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { updateChanges, ImageDeliverType } from '../../../utils/model';
import { required } from 'vuelidate/lib/validators';
import { validate, deepClone } from '@/utils/utils';

export default {
  name: 'UpdateTemplateDialog',
  data() {
    return {
      openDialog: false, // 打开弹窗
      updateChanges: updateChanges(),
      templateOfChange: [],
      ImageDeliverType
    };
  },
  validations: {
    updateChanges: {
      plan_time: {
        required
      },
      prod_spdb_no: {
        required,
        onlyNumOrWord(value) {
          let re = new RegExp(/^[a-zA-Z0-9]*$/);
          return re.test(value);
        }
      },
      template: {
        required
      }
    }
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    detail: Object,
    loading: Boolean,
    prod_id: String
  },
  computed: {
    ...mapState('releaseForm', {
      excelTemplate: 'excelTemplate'
    })
  },
  watch: {
    async value(val) {
      if (val === true) {
        this.updateChanges = deepClone(this.detail);
        let templateObj = {
          filename: this.detail.excel_template_name,
          url: this.detail.excel_template_url
        };
        this.$set(this.updateChanges, 'template', templateObj);
        if (this.detail.image_deliver_type === this.ImageDeliverType.auto) {
          await this.queryExcelTemplate({
            template_type: this.detail.type,
            sysname_cn: this.detail.owner_system_name
          });
          this.templateOfChange = this.excelTemplate;
        }
      }
    },
    detail(val) {
      this.updateChanges = val;
    }
  },
  methods: {
    ...mapActions('releaseForm', ['queryExcelTemplate', 'changesUpdate']),
    async handleUpdate() {
      this.$v.updateChanges.$touch();
      let Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('updateChanges') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.updateChanges.$invalid) {
        return;
      }
      const params = {
        plan_time: this.updateChanges.plan_time,
        excel_template_url: this.updateChanges.template.url,
        excel_template_name: this.updateChanges.template.filename,
        prod_spdb_no: this.updateChanges.prod_spdb_no,
        prod_id: this.prod_id
      };
      await this.changesUpdate(params);
      this.$emit('confirm', params);
    },
    templateFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase().trim();
        this.templateOfChange = this.excelTemplate.filter(v =>
          v.filename.toLowerCase().includes(needle)
        );
      });
    }
  }
};
</script>

<style lang="stylus" scoped>
/deep/ .el-input__inner
  width 300px
  height 36px
  margin-bottom 16px
  border 1px solid #bbb

/deep/ .el-input__icon
  height 80%

/deep/ .el-icon-circle-close
  position absolute
  right -100px
</style>
<style>
.el-time-panel {
  z-index: 9999 !important;
}
.el-time-spinner__item {
  height: 36px;
  line-height: 30px;
}
</style>
