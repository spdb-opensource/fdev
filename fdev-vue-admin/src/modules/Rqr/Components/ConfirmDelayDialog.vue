<template>
  <f-dialog
    right
    persistent
    v-model="open"
    title="确认延期"
    @before-show="initDialogData"
    @hide="handleConfirmDelayClose"
  >
    <fdev-form
      ref="confirmDelayForm"
      greedy
      @submit.prevent="commitConfirmDelay"
    >
      <div style="margin-bottom: 4px; margin-top:-8px;margin-left:-8px;">
        <span v-if="developDelayShow">
          <fdev-tooltip position="top" v-if="developDelayFlag">
            延期阶段确认后不可取消！
          </fdev-tooltip>
          <fdev-checkbox
            v-model="selection"
            label="启动延期"
            val="developDelay"
            :disable="developDelayFlag"
            style="margin-right: 137px;"
          />
        </span>
        <span v-if="testStartDelayShow">
          <fdev-tooltip position="top" v-if="testStartDelayFlag">
            延期阶段确认后不可取消！
          </fdev-tooltip>
          <fdev-checkbox
            v-model="selection"
            label="提交用户测试延期"
            val="testStartDelay"
            :disable="testStartDelayFlag"
          />
        </span>
      </div>
      <div style="margin-bottom: 12px; margin-left:-8px;">
        <span v-if="testFinishDelayShow">
          <fdev-tooltip position="top" v-if="testFinishDelayFlag">
            延期阶段确认后不可取消！
          </fdev-tooltip>
          <fdev-checkbox
            v-model="selection"
            label="提交测试完成延期"
            val="testFinishDelay"
            :disable="testFinishDelayFlag"
            style=" margin-right: 81px;"
          />
        </span>
        <span v-if="productDelayShow">
          <fdev-tooltip position="top" v-if="productDelayFlag">
            延期阶段确认后不可取消！
          </fdev-tooltip>
          <fdev-checkbox
            v-model="selection"
            label="投产延期"
            val="productDelay"
            :disable="productDelayFlag"
          />
        </span>
      </div>
      <f-formitem
        label="ipmp实施延期原因分类"
        required
        label-style="width:120px"
        diaS
      >
        <fdev-select
          multiple
          use-input
          map-options
          emit-value
          v-model="implDelayType"
          :options="delayAvailables"
          ref="implDelayType"
          :rules="[
            val => (!!val && val.length > 0) || 'ipmp实施延期原因分类不能为空'
          ]"
        />
      </f-formitem>
      <f-formitem
        label="ipmp实施延期原因"
        required
        label-style="width:120px"
        diaS
      >
        <fdev-input
          v-model="implDelayReason"
          type="textarea"
          ref="implDelayReason"
          :rules="[val => !!val || '实施延期原因不能为空']"
        />
      </f-formitem>
    </fdev-form>

    <template v-slot:btnSlot>
      <fdev-btn
        v-if="!dialogLoading"
        label="取消"
        outline
        dialog
        @click="handleConfirmDelayClose"
      />
      <fdev-btn
        dialog
        v-if="!dialogLoading"
        :loading="loading"
        label="确定"
        @click="commitConfirmDelay"
      />
    </template>
  </f-dialog>
</template>

<script>
import {
  queryIpmpUnitById,
  confirmDelay
} from '@/modules/Rqr/services/methods';
import { successNotify, resolveResponseError } from '@/utils/utils';
import { delayAvailables, delayTypeSpecial } from '../model';
export default {
  name: 'confirmDelayDialog',

  props: {
    value: {
      type: Boolean,
      default: false
    },
    implUnitNum: {
      type: String
    },
    implUnitId: {
      type: String
    }
  },
  data() {
    return {
      dialogLoading: false,
      loading: false,
      selection: [],
      delayAvailables: delayAvailables,
      implDelayType: [],
      implDelayReason: '',
      developDelayFlag: false,
      testStartDelayFlag: false,
      testFinishDelayFlag: false,
      productDelayFlag: false,
      confirmDelayItem: []
    };
  },

  computed: {
    open: {
      get() {
        return this.value;
      },
      set() {
        this.$emit('input', false);
      }
    },
    developDelayShow() {
      return this.confirmDelayItem.indexOf('developDelay') > -1;
    },
    testStartDelayShow() {
      return this.confirmDelayItem.indexOf('testStartDelay') > -1;
    },
    testFinishDelayShow() {
      return this.confirmDelayItem.indexOf('testFinishDelay') > -1;
    },
    productDelayShow() {
      return this.confirmDelayItem.indexOf('productDelay') > -1;
    }
  },
  methods: {
    async initDialogData() {
      let res = await resolveResponseError(() =>
        queryIpmpUnitById({ implUnitNum: this.implUnitNum })
      );
      this.implDelayReason = res.implDelayReason;
      res.confirmDelayStage && res.confirmDelayStage.length > 0
        ? (this.selection = this.selection.concat(res.confirmDelayStage))
        : [];
      this.confirmDelayItem = res.confirmDelayItem ? res.confirmDelayItem : [];
      res.confirmDelayItem.forEach(item => {
        if (this.selection.indexOf(item) < 0) {
          this.selection.push(item);
        }
      });
      // ipmp实施延期原因分类赋值
      this.implDelayType = res.implDelayType
        ? res.implDelayType.split(',')
        : [];
      if (
        res.confirmDelayStage &&
        res.confirmDelayStage.indexOf('developDelay') > -1
      ) {
        this.developDelayFlag = true;
      }
      if (
        res.confirmDelayStage &&
        res.confirmDelayStage.indexOf('testStartDelay') > -1
      ) {
        this.testStartDelayFlag = true;
      }
      if (
        res.confirmDelayStage &&
        res.confirmDelayStage.indexOf('testFinishDelay') > -1
      ) {
        this.testFinishDelayFlag = true;
      }
      if (
        res.confirmDelayStage &&
        res.confirmDelayStage.indexOf('productDelay') > -1
      ) {
        this.productDelayFlag = true;
      }
    },
    handleConfirmDelayClose() {
      this.$emit('input', false);
    },
    async commitConfirmDelay() {
      this.$refs.confirmDelayForm.validate().then(res => {
        if (!res) return;
        this.saveConfirmDelay();
      });
    },
    //确认延期
    async saveConfirmDelay() {
      let params = {
        confirmDelayStage: this.selection, //确认延期阶段：
        implDelayReason: this.implDelayReason, //ipmp实施延期原因
        id: this.implUnitId,
        implDelayTypeName: '',
        implDelayType: ''
      };

      //ipmp实施延期原因分类
      let delayName = [];
      if (this.implDelayType) {
        this.implDelayType.map(item => {
          delayName.push(delayTypeSpecial[item]);
        });
      }
      params.implDelayTypeName = delayName.join(',');
      params.implDelayType = this.implDelayType
        ? this.implDelayType.join(',')
        : '';
      try {
        this.loading = true;
        await resolveResponseError(() => confirmDelay(params));
        successNotify('确认延期成功');
        this.$emit('close', true);
      } catch (er) {
        this.loading = false;
      }
    }
  }
};
</script>

<style lang="stylus" scoped></style>
