<template>
  <f-dialog
    right
    persistent
    v-model="open"
    title="调整排期"
    @before-show="initDialogData"
    @hide="handleAdjustDataClose"
  >
    <Loading :visible="dialogLoading">
      <fdev-form ref="adjustDateModel" greedy @submit="commitAdjustData">
        <div class="row  justify-between" style="max-width: 677px;">
          <f-formitem
            label="计划启动开发日期"
            label-style="width:112px"
            value-style="width:78px;margin-right: 59px;"
          >
            {{ adjustDateModel.planDevelopDate }}
          </f-formitem>
          <!--计划启动开发日期(调)-->
          <f-formitem label="计划启动开发日期(调)" label-style="width:112px">
            <fdev-tooltip position="top" v-if="planDevelopDateAdjustFlag">
              此阶段已确认延期，无法再次调整！
            </fdev-tooltip>
            <f-date
              v-model="adjustDateModel.planDevelopDateAdjust"
              :options="planDevelopDateAdjustOptions"
              ref="adjustDateModel.planDevelopDateAdjust"
              :disable="planDevelopDateAdjustFlag"
              hint=""
            />
          </f-formitem>
          <f-formitem
            label="计划提交用户测试日期"
            label-style="width:112px"
            value-style="width:78px;margin-right: 59px; line-height: 20px;"
          >
            {{ adjustDateModel.planTestStartDate }}
          </f-formitem>
          <!--计划提交用户测试日期（调）-->
          <f-formitem
            label="计划提交用户测试日期(调)"
            label-style="width:112px"
          >
            <fdev-tooltip position="top" v-if="planTestStartDateAdjustFlag">
              此阶段已确认延期，无法再次调整！
            </fdev-tooltip>
            <f-date
              v-model="adjustDateModel.planTestStartDateAdjust"
              :options="planTestStartDateAdjustOptions"
              :disable="planTestStartDateAdjustFlag"
              ref="adjustDateModel.planTestStartDateAdjust"
              hint=""
            />
          </f-formitem>
          <f-formitem
            label="计划用户测试完成日期"
            label-style="width:112px"
            value-style="width:78px;margin-right: 59px; line-height: 20px;"
          >
            {{ adjustDateModel.planTestFinishDate }}
          </f-formitem>
          <!--计划用户测试完成日期（调）-->
          <f-formitem
            label="计划用户测试完成日期(调)"
            label-style="width:112px"
          >
            <fdev-tooltip position="top" v-if="planTestFinishDateAdjustFlag">
              此阶段已确认延期，无法再次调整！
            </fdev-tooltip>
            <f-date
              v-model="adjustDateModel.planTestFinishDateAdjust"
              :options="planTestFinishDateAdjustOptions"
              :disable="planTestFinishDateAdjustFlag"
              ref="adjustDateModel.planTestFinishDateAdjust"
              hint=""
            />
          </f-formitem>
          <f-formitem
            label="计划投产日期"
            label-style="width:112px"
            value-style="width:78px;margin-right: 59px;"
          >
            {{ adjustDateModel.planProductDate }}
          </f-formitem>
          <!--计划投产日期（调）-->
          <f-formitem
            label="计划投产日期(调)"
            :required="planProductDateAdjustFlag"
            label-style="width:112px"
          >
            <fdev-tooltip
              position="top"
              v-if="planProductDateAdjustDisableFlag"
            >
              此阶段已确认延期，无法再次调整！
            </fdev-tooltip>
            <f-date
              v-model="adjustDateModel.planProductDateAdjust"
              :options="planProductDateAdjustOptions"
              ref="adjustDateModel.planProductDateAdjust"
              :disable="planProductDateAdjustDisableFlag"
              :rules="[
                val =>
                  !planProductDateAdjustFlag
                    ? true
                    : !!val || '请选择计划投产日期(调)'
              ]"
            />
          </f-formitem>
          <!-- fdev实施计划变更原因分类 -->
          <f-formitem
            label="fdev实施计划变更原因分类"
            required
            full-width
            label-style="width:112px"
            value-style="width:545px"
          >
            <fdev-select
              option-label="value"
              option-value="code"
              emit-value
              map-options
              v-model="adjustDateModel.implChangeType"
              ref="adjustDateModel.implChangeType"
              :options="implChangeTypeOptions"
              :rules="[val => !!val || '请选择fdev实施计划变更原因分类']"
            />
          </f-formitem>
          <!-- fdev实施计划变更原因 -->
          <f-formitem
            label="fdev实施计划变更原因"
            required
            full-width
            label-style="width:112px"
            value-style="width:545px"
          >
            <fdev-input
              type="textarea"
              v-model="adjustDateModel.implChangeReason"
              ref="adjustDateModel.implChangeReason"
              :rules="[val => !!val || '请填写fdev实施计划变更原因']"
            />
          </f-formitem>
          <!-- 业务确认邮件 -->
          <f-formitem
            label="业务确认邮件"
            full-width
            required
            label-style="width:112px"
            value-style="width:545px"
          >
            <div>
              <fdev-btn
                dash
                ficon="upload"
                :label="
                  adjustDateModel.businessEmail &&
                  adjustDateModel.businessEmail.length > 0
                    ? '继续选择'
                    : '选择文件'
                "
                @click="openFiles()"
              />
              <span class="q-ml-sm">
                <span
                  class="text-grey-7"
                  v-show="
                    adjustDateModel.businessEmail &&
                      adjustDateModel.businessEmail.length === 0
                  "
                >
                  暂未选择文件
                </span>
                <div
                  v-for="file in adjustDateModel.businessEmail"
                  :key="file.name"
                >
                  <div
                    class="file-wrapper"
                    style="display: flex;align-items: center"
                  >
                    <div
                      class="ellipsis"
                      style="width:250px height:10px"
                      :title="file.name"
                    >
                      {{ file.name }}
                    </div>
                    <f-icon
                      :width="14"
                      :height="14"
                      class="text-primary q-ml-sm"
                      name="close"
                      @click="deleteProFiles(file)"
                    />
                  </div>
                </div>
              </span>
              <fdev-input
                class="hideInput"
                :rules="[val => (!!val && val.length > 0) || '请选择文件']"
                v-model="adjustDateModel.businessEmail"
              />
            </div>
          </f-formitem>
        </div>
      </fdev-form>
    </Loading>
    <template v-slot:btnSlot>
      <fdev-btn
        v-if="!dialogLoading"
        label="取消"
        outline
        dialog
        @click="handleAdjustDataClose"
      />
      <fdev-btn
        dialog
        v-if="!dialogLoading"
        :loading="loading"
        label="确定"
        @click="$refs['adjustDateModel'].submit()"
      />
    </template>
  </f-dialog>
</template>

<script>
import Loading from '@/components/Loading';
import { createAdjustDateModel } from '../model';
import {
  queryIpmpUnitById,
  queryByTypes,
  adjustDate,
  deleteEmailFile
} from '@/modules/Rqr/services/methods';
import { successNotify, resolveResponseError } from '@/utils/utils';
import moment from 'moment';
export default {
  name: 'adjustDateDialog',
  components: { Loading },
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
      adjustDateModel: createAdjustDateModel(),
      implChangeTypeOptions: [],
      planProductDateAdjustFlag: false,
      planDevelopDateAdjustFlag: false,
      planTestStartDateAdjustFlag: false,
      planTestFinishDateAdjustFlag: false,
      planProductDateAdjustDisableFlag: false,
      implUnitInfoData: {}
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
    }
  },
  methods: {
    formatDate(date) {
      if (!date || typeof date === 'object') {
        return;
      }
      return moment(date).format('YYYY/MM/DD');
    },
    //计划启动日期（调）
    planDevelopDateAdjustOptions(date) {
      if (this.adjustDateModel.planTestStartDateAdjust) {
        //计划启动日期（调）大于实际启动日期
        //当计划提交用户测试日期（调）存在时，计划启动日期调要小于等于计划提交用户测试日期（调）。
        return (
          date >= this.formatDate(this.implUnitInfoData.acturalDevelopDate) &&
          date <= this.formatDate(this.adjustDateModel.planTestStartDateAdjust)
        );
      } else {
        //计划启动日期调要小于等于计划提交用户测试日期
        //计划启动日期（调）大于实际启动日期
        const beforeDate = this.formatDate(
          this.implUnitInfoData.acturalDevelopDate
        );
        let arry1 = [];
        if (this.adjustDateModel.planTestFinishDateAdjust) {
          arry1.push(this.adjustDateModel.planTestFinishDateAdjust);
        } else {
          arry1.push(this.implUnitInfoData.planTestFinishDate);
        }
        if (this.adjustDateModel.planProductDateAdjust) {
          arry1.push(this.adjustDateModel.planProductDateAdjust);
        } else {
          arry1.push(this.implUnitInfoData.planProductDate);
        }
        arry1.push(this.implUnitInfoData.planTestStartDate);
        //将数组按照降序排序,由小到大
        // arry1.sort(function(a, b) {
        //   return a - b;
        // });
        this.sortArray(arry1);
        //将其与实际计划启动日期比较，找出比实际启动日期大的值
        let afterDate = '';
        for (let i = 0; i <= arry1.length - 1; i++) {
          if (arry1[i] >= this.implUnitInfoData.acturalDevelopDate) {
            afterDate = arry1[i];
            break;
          }
        }
        if (beforeDate && afterDate) {
          return date >= beforeDate && date <= this.formatDate(afterDate);
        } else if (afterDate) {
          return date <= this.formatDate(afterDate);
        } else {
          return date >= beforeDate;
        }
      }
    },
    //  计划提交用户测试日期（调）
    planTestStartDateAdjustOptions(date) {
      if (
        this.adjustDateModel.planDevelopDateAdjust &&
        this.adjustDateModel.planTestFinishDateAdjust
      ) {
        //当计划启动日期（调），计划用户完成测试日期（调）都有值时。
        //1。大于等于实际提交用户测试日期，大于等于计划启动日期（调）
        // 2.小于等于计划用户完成测试日期（调）
        const beforeDate1 = this.adjustDateModel.planDevelopDateAdjust;
        const beforeDate2 = this.implUnitInfoData.acturalTestStartDate;
        const beforeDate =
          beforeDate1 >= beforeDate2 ? beforeDate1 : beforeDate2;
        const afterDate = this.formatDate(
          this.adjustDateModel.planTestFinishDateAdjust
        );
        if (beforeDate && afterDate) {
          return date >= this.formatDate(beforeDate) && date <= afterDate;
        } else if (afterDate) {
          return date <= afterDate;
        } else {
          return date >= this.formatDate(beforeDate);
        }
      } else if (
        !this.adjustDateModel.planDevelopDateAdjust &&
        this.adjustDateModel.planTestFinishDateAdjust
      ) {
        //当计划启动日期（调）无值，计划用户完成测试日期（调）有值时。
        // 大于等于实际提交用户测试日期，大于等于当计划启动日期。
        // 小于计划用户完成测试日期（调）
        const beforeDate1 = this.adjustDateModel.planDevelopDate;
        const beforeDate2 = this.implUnitInfoData.acturalTestStartDate;
        const beforeDate =
          beforeDate1 >= beforeDate2 ? beforeDate1 : beforeDate2;
        const afterDate = this.formatDate(
          this.adjustDateModel.planTestFinishDateAdjust
        );
        if (beforeDate && afterDate) {
          return date >= this.formatDate(beforeDate) && date <= afterDate;
        } else if (afterDate) {
          return date <= afterDate;
        } else {
          return date >= this.formatDate(beforeDate);
        }
      } else if (
        this.adjustDateModel.planDevelopDateAdjust &&
        !this.adjustDateModel.planTestFinishDateAdjust
      ) {
        //当计划启动日期（调）有值，计划用户完成测试日期（调）无值时。
        //大于等于实际提交用户测试日期，大于等于计划启动日期（调）
        // 小于等于计划完成测试日期，小于等于计划投产日期
        const beforeDate1 = this.adjustDateModel.planDevelopDateAdjust;
        const beforeDate2 = this.implUnitInfoData.acturalTestStartDate;
        const beforeDate =
          beforeDate1 >= beforeDate2 ? beforeDate1 : beforeDate2;
        let arry2 = [];
        if (this.adjustDateModel.planProductDateAdjust) {
          arry2.push(this.adjustDateModel.planProductDateAdjust);
        } else {
          arry2.push(this.implUnitInfoData.planProductDate);
        }
        arry2.push(this.implUnitInfoData.planTestFinishDate);
        this.sortArray(arry2);
        let afterDate = '';
        for (let i = 0; i <= arry2.length - 1; i++) {
          if (arry2[i] >= beforeDate) {
            afterDate = arry2[i];
            break;
          }
        }
        if (beforeDate && afterDate) {
          return (
            date >= this.formatDate(beforeDate) &&
            date <= this.formatDate(afterDate)
          );
        } else if (afterDate) {
          return date <= this.formatDate(afterDate);
        } else {
          return date >= this.formatDate(beforeDate);
        }
      } else if (
        !this.adjustDateModel.planDevelopDateAdjust &&
        !this.adjustDateModel.planTestFinishDateAdjust
      ) {
        //当计划启动日期（调）无值，计划用户完成测试日期（调）无值时。
        //大于等于实际提交用户测试日期，大于等于当计划启动日期。
        // 小于等于计划完成测试日期，小于等于计划投产日期
        const beforeDate1 = this.adjustDateModel.planDevelopDate;
        const beforeDate2 = this.implUnitInfoData.acturalTestStartDate;
        const beforeDate =
          beforeDate1 >= beforeDate2 ? beforeDate1 : beforeDate2;

        let arry2 = [];
        if (this.adjustDateModel.planProductDateAdjust) {
          arry2.push(this.adjustDateModel.planProductDateAdjust);
        } else {
          arry2.push(this.implUnitInfoData.planProductDate);
        }
        arry2.push(this.implUnitInfoData.planTestFinishDate);
        this.sortArray(arry2);

        let afterDate;
        for (let i = 0; i <= arry2.length - 1; i++) {
          if (arry2[i] >= beforeDate) {
            afterDate = arry2[i];
            break;
          }
        }
        if (beforeDate && afterDate) {
          return (
            date >= this.formatDate(beforeDate) &&
            date <= this.formatDate(afterDate)
          );
        } else if (afterDate) {
          return date <= this.formatDate(afterDate);
        } else {
          return date >= this.formatDate(beforeDate);
        }
      }
    },

    //计划用户测试完成日期（调）
    planTestFinishDateAdjustOptions(date) {
      if (
        this.adjustDateModel.planTestStartDateAdjust &&
        this.adjustDateModel.planProductDateAdjust
      ) {
        //当计划提交测试日期（调）有值，当计划投产日期（调）有值
        // 大于等于实际用户测试完成日期，大于等于计划提交测试日期(调)
        //小于等于计划投产日期（调）

        const beforeDate1 = this.adjustDateModel.planTestStartDateAdjust;
        const beforeDate2 = this.implUnitInfoData.acturalTestFinishDate;
        const beforeDate =
          beforeDate1 >= beforeDate2 ? beforeDate1 : beforeDate2;
        const afterDate = this.formatDate(
          this.adjustDateModel.planProductDateAdjust
        );
        if (beforeDate && afterDate) {
          return date >= this.formatDate(beforeDate) && date <= afterDate;
        } else if (afterDate) {
          return date <= afterDate;
        } else {
          return date >= this.formatDate(beforeDate);
        }
      } else if (
        !this.adjustDateModel.planTestStartDateAdjust &&
        this.adjustDateModel.planProductDateAdjust
      ) {
        //当计划提交测试日期（调）无值，当计划投产日期（调）有值
        //大于等于实际用户测试完成日期，大于等于计划提交测试日期,大于等于计划启动日期调，大于计划启动日期,大于等于计划启动日期调
        // 小于等于计划投产日期（调）
        const beforeArray = [];
        beforeArray.push(
          this.adjustDateModel.planTestStartDate,
          this.adjustDateModel.planDevelopDate,
          this.implUnitInfoData.acturalTestFinishDate,
          this.adjustDateModel.planDevelopDateAdjust
            ? this.adjustDateModel.planDevelopDateAdjust
            : ''
        );
        this.sortArray(beforeArray);

        const beforeDate = beforeArray[beforeArray.length - 1];
        const afterDate = this.formatDate(
          this.adjustDateModel.planProductDateAdjust
        );
        if (beforeDate && afterDate) {
          return date >= this.formatDate(beforeDate) && date <= afterDate;
        } else if (afterDate) {
          return date <= afterDate;
        } else {
          return date >= this.formatDate(beforeDate);
        }
      } else if (
        this.adjustDateModel.planTestStartDateAdjust &&
        !this.adjustDateModel.planProductDateAdjust
      ) {
        //当计划提交测试日期（投）有值，当计划投产日期（调）无值
        // 大于等于实际用户测试完成日期，大于等于计划提交测试日期(调)
        //小于等于计划投产日期
        const beforeDate1 = this.adjustDateModel.planTestStartDateAdjust;
        const beforeDate2 = this.implUnitInfoData.acturalTestFinishDate;
        const beforeDate =
          beforeDate1 >= beforeDate2 ? beforeDate1 : beforeDate2;
        let afterDate;
        if (beforeDate >= this.adjustDateModel.planProductDate) {
          afterDate = '';
        } else {
          afterDate = this.formatDate(this.adjustDateModel.planProductDate);
        }
        if (beforeDate && afterDate) {
          return date >= this.formatDate(beforeDate) && date <= afterDate;
        } else if (afterDate) {
          return date <= afterDate;
        } else {
          return date >= this.formatDate(beforeDate);
        }
      } else if (
        !this.adjustDateModel.planTestStartDateAdjust &&
        !this.adjustDateModel.planProductDateAdjust
      ) {
        //当计划提交测试日期（调）无值，当计划投产日期（调）无值
        //大于等于实际用户测试完成日期，大于等于计划提交测试日期,大于等于计划启动日期调，大于计划启动日期
        //小于计划投产日期
        const beforeArray = [];
        beforeArray.push(
          this.adjustDateModel.planTestStartDate,
          this.adjustDateModel.planDevelopDate,
          this.implUnitInfoData.acturalTestFinishDate,
          this.adjustDateModel.planDevelopDateAdjust
            ? this.adjustDateModel.planDevelopDateAdjust
            : ''
        );
        this.sortArray(beforeArray);
        const beforeDate = beforeArray[beforeArray.length - 1];
        let afterDate;
        if (beforeDate >= this.adjustDateModel.planProductDate) {
          afterDate = '';
        } else {
          afterDate = this.formatDate(this.adjustDateModel.planProductDate);
        }
        if (beforeDate && afterDate) {
          return date >= this.formatDate(beforeDate) && date <= afterDate;
        } else if (afterDate) {
          return date <= afterDate;
        } else {
          return date >= this.formatDate(beforeDate);
        }
      }
    },
    //计划投产日期（调）
    planProductDateAdjustOptions(date) {
      if (this.adjustDateModel.planTestFinishDateAdjust) {
        //当计划用户测试完成日期（调） 有值时比较。
        // 1.计划投产日期（调）大于等于实际投产日期
        //2.计划投产日期（调）大于等于当计划用户测试完成日期（调）
        const beforeDate = this.formatDate(
          this.implUnitInfoData.acturalProductDate
        );
        if (beforeDate) {
          return (
            date >= beforeDate &&
            date >=
              this.formatDate(this.adjustDateModel.planTestFinishDateAdjust)
          );
        } else {
          return (
            date >=
            this.formatDate(this.adjustDateModel.planTestFinishDateAdjust)
          );
        }
      } else {
        //当计划用户测试完成日期（调） 无值时比较。
        //大于实际开发日期，大于计划启动、大于计划测试、大于计划测试完成、大于计划启动调、大于计划测试调、
        const beforeArray = [];
        beforeArray.push(
          this.implUnitInfoData.planTestFinishDate,
          this.implUnitInfoData.planTestStartDate,
          this.implUnitInfoData.planDevelopDate,
          this.implUnitInfoData.acturalProductDate,
          this.adjustDateModel.planTestFinishDateAdjust
            ? this.adjustDateModel.planTestFinishDateAdjust
            : '',
          this.adjustDateModel.planTestStartDateAdjust
            ? this.adjustDateModel.planTestStartDateAdjust
            : '',
          this.adjustDateModel.planDevelopDateAdjust
            ? this.adjustDateModel.planDevelopDateAdjust
            : ''
        );
        this.sortArray(beforeArray);
        const beforeDate = beforeArray[beforeArray.length - 1];
        if (beforeDate) {
          return date >= this.formatDate(beforeDate);
        } else {
          return true;
        }
      }
    },
    // 数组排序
    sortArray(item) {
      for (var i = 0; i < item.length; i++) {
        for (var k = 0; k < item.length; k++) {
          if (item[k] > item[k + 1]) {
            var temp = item[k];
            item[k] = item[k + 1];
            item[k + 1] = temp;
          }
        }
      }
    },

    //数据初始化
    async initDialogData() {
      let parms = ['implChangeType'];
      let resData = await resolveResponseError(() =>
        queryByTypes({ types: parms })
      );
      this.implChangeTypeOptions = resData;
      let res = await resolveResponseError(() =>
        queryIpmpUnitById({ implUnitNum: this.implUnitNum })
      );
      this.implUnitInfoData = res;

      this.adjustDateModel.planDevelopDate = res.planDevelopDate
        ? res.planDevelopDate
        : '';
      this.adjustDateModel.planDevelopDateAdjust = res.planDevelopDateAdjust
        ? res.planDevelopDateAdjust
        : '';
      this.adjustDateModel.planTestStartDate = res.planTestStartDate
        ? res.planTestStartDate
        : '';
      this.adjustDateModel.planTestStartDateAdjust = res.planTestStartDateAdjust
        ? res.planTestStartDateAdjust
        : '';
      this.adjustDateModel.planTestFinishDate = res.planTestFinishDate
        ? res.planTestFinishDate
        : '';
      this.adjustDateModel.planTestFinishDateAdjust = res.planTestFinishDateAdjust
        ? res.planTestFinishDateAdjust
        : '';
      this.adjustDateModel.planProductDate = res.planProductDate
        ? res.planProductDate
        : '';
      this.adjustDateModel.planProductDateAdjust = res.planProductDateAdjust
        ? res.planProductDateAdjust
        : '';

      // fdev实施计划变更原因分类
      this.adjustDateModel.implChangeType = this.getImplChangeTypeItem(
        res.implChangeType
      );
      this.adjustDateModel.implChangeReason = res.implChangeReason;
      this.adjustDateModel.businessEmail = res.businessEmail
        ? res.businessEmail
        : [];
      this.adjustDateModel.businessEmail.forEach((item, index) => {
        this.$set(
          this.adjustDateModel.businessEmail[index],
          'name',
          item.businessEmailName
        );
      });

      if (
        res.planProductDate < res.acturalProductDate &&
        (res.confirmDelayStage
          ? res.confirmDelayStage.indexOf('productDelay') < 0
          : true)
      ) {
        this.planProductDateAdjustFlag = true;
      }
      if (
        res.confirmDelayStage &&
        res.confirmDelayStage.indexOf('developDelay') > -1
      ) {
        this.planDevelopDateAdjustFlag = true;
      }
      if (
        res.confirmDelayStage &&
        res.confirmDelayStage.indexOf('testStartDelay') > -1
      ) {
        this.planTestStartDateAdjustFlag = true;
      }
      if (
        res.confirmDelayStage &&
        res.confirmDelayStage.indexOf('testFinishDelay') > -1
      ) {
        this.planTestFinishDateAdjustFlag = true;
      }
      if (
        res.confirmDelayStage &&
        res.confirmDelayStage.indexOf('productDelay') > -1
      ) {
        this.planProductDateAdjustDisableFlag = true;
      }
    },
    //弹窗关闭
    handleAdjustDataClose() {
      this.$emit('input', false);
    },
    //删除文件
    async deleteProFiles(file) {
      if (!file.businessEmailPath) {
        this.adjustDateModel.businessEmail = this.adjustDateModel.businessEmail.filter(
          item => item.name !== file.name
        );
      } else {
        await resolveResponseError(() =>
          deleteEmailFile({
            id: this.implUnitId,
            businessEmailPath: file.businessEmailPath,
            businessEmailName: file.businessEmailName
          })
        );
        successNotify('删除文件成功');
        this.adjustDateModel.businessEmail = this.adjustDateModel.businessEmail.filter(
          item => item.name !== file.name
        );
      }
    },
    //提交
    commitAdjustData() {
      const keys = Object.keys(this.$refs).filter(
        key => this.$refs[key] && key.indexOf('adjustDateModel') > -1
      );
      let res = keys.map(key => {
        if (this.$refs[key] instanceof Array) {
          if (!this.$refs[key][0]) return 'error';
        }
        if (
          this.$refs[key].$children.length > 0 &&
          this.$refs[key].$children[0].$children.length > 0 &&
          this.$refs[key].$children[0].validate
        ) {
          if (!this.$refs[key].$children[0].validate()) return 'error';
        }
        if (!this.$refs[key].validate()) return 'error';
      });
      if (res.includes('error')) return;
      this.saveAdjustData();
    },
    //提交数据
    async saveAdjustData() {
      let formData = new FormData();
      formData.append(
        'planDevelopDateAdjust',
        this.adjustDateModel.planDevelopDateAdjust
      );

      formData.append(
        'planTestStartDateAdjust',
        this.adjustDateModel.planTestStartDateAdjust
      );
      formData.append(
        'planTestFinishDateAdjust',
        this.adjustDateModel.planTestFinishDateAdjust
      );
      formData.append(
        'planProductDateAdjust',
        this.adjustDateModel.planProductDateAdjust
      );

      if (typeof this.adjustDateModel.implChangeType == 'string') {
        formData.append('implChangeType', this.adjustDateModel.implChangeType);
      } else {
        formData.append(
          'implChangeType',
          this.adjustDateModel.implChangeType.code
        );
      }

      formData.append(
        'implChangeReason',
        this.adjustDateModel.implChangeReason
      );
      formData.append('id', this.implUnitId);
      //邮件
      this.adjustDateModel.businessEmail.forEach(file => {
        if (!file.businessEmailPath) {
          formData.append('businessEmail', file, file.name);
        }
      });
      try {
        this.loading = true;
        await resolveResponseError(() => adjustDate(formData));
        successNotify('调整排期成功');
        this.$emit('close', true);
      } catch (e) {
        this.loading = false;
      }
    },
    getImplChangeTypeItem(val) {
      for (let i = 0; i < this.implChangeTypeOptions.length; i++) {
        if (val && val[0] == this.implChangeTypeOptions[i].code) {
          return this.implChangeTypeOptions[i];
        }
      }
      return null;
    },
    openFiles() {
      const input = document.createElement('input');
      input.setAttribute('type', 'file');
      //input.setAttribute('accept', '*.vue,*.js');
      input.setAttribute('multiple', 'multiple');

      input.onchange = file => this.uploadFile(input);
      input.click();
    },
    uploadFile({ files }) {
      files = Array.from(files);
      const modelFiles = [...this.adjustDateModel.businessEmail];
      files.forEach(file => {
        const notExist = this.adjustDateModel.businessEmail.every(
          item => item.name !== file.name
        );

        if (notExist) {
          modelFiles.push(file);
        }
      });
      this.adjustDateModel.businessEmail = modelFiles;
    }
  }
};
</script>

<style lang="stylus" scoped>
>>> .hideInput .q-field__control.relative-position.row.no-wrap.text-negative,
>>> .hideInput input,
>>> .hideInput .q-field__control,
>>> .hideInput .q-field__control:before,
>>> .hideInput .q-field__control:after {
  display: none;
  content: none;
  height: 0px;
}
</style>
