<template>
  <fdev-dialog
    full-width
    :value="value"
    transition-show="slide-up"
    transition-hide="slide-down"
    persistent
    @input="$emit('input', $event)"
    @show="initQuery()"
    @shake="confirmToClose"
  >
    <fdev-layout view="Lhh lpR fff" container class="bg-white">
      <fdev-header bordered class="bg-primary">
        <fdev-toolbar>
          <fdev-toolbar-title>需求预评估</fdev-toolbar-title>
          <fdev-btn flat round dense icon="close" @click="confirmToClose" />
        </fdev-toolbar>
      </fdev-header>
      <fdev-page-container>
        <fdev-page padding>
          <fdev-form>
            <div class="q-pa-md items-start q-gutter-md">
              <fdev-card>
                <fdev-card-section>
                  <div class="row justify-between">
                    <div class="title">基础信息</div>
                    <div>
                      <fdev-btn
                        color="primary"
                        icon="cloud_download"
                        flat
                        size="lg"
                        dense
                        @click="handleExportModelExcel()"
                        ><span class="myspan">
                          评估表模板下载
                        </span>
                      </fdev-btn>
                      <fdev-btn
                        color="primary"
                        icon="cloud_upload"
                        flat
                        size="lg"
                        dense
                        @click="handleSelectFile"
                        ><span class="myspan">
                          导入需求评估表
                        </span>
                      </fdev-btn>
                      <input
                        ref="fileInput"
                        style="display:none"
                        filled
                        type="file"
                        @change="uploadFile()"
                      />
                    </div>
                  </div>
                  <div class="row">
                    <div class="col">
                      <div class="relative">
                        <fdev-btn
                          icon="live_help"
                          flat
                          round
                          size="sm"
                          color="primary"
                          class="mybtn"
                        >
                          <fdev-tooltip
                            anchor="top middle"
                            self="center middle"
                            :offest="[-20, 0]"
                          >
                            <div class="q-mt-sm q-mb-sm">
                              <span class="pre-style"
                                >需求编号规则应为以下三类：</span
                              >
                              <span class="pre-style"
                                >UT-WLaR-2020-0001：UT-部门简写-年份-序号 ->
                                UT-ZZZZ-YYYY-XXXX</span
                              >
                              <span class="pre-style"
                                >0001-2020-L-193-000015：0001-年份-L-类别-序号
                                -> 0001-YYYY-L-ZZZ-XXXXXX</span
                              >
                              <span class="pre-style"
                                >后面不能有M001、N001、001之类的序号。</span
                              >
                            </div>
                          </fdev-tooltip>
                        </fdev-btn>
                        <Field
                          label="需求编号(OA联系单编号)"
                          :label-col="4"
                          align="right"
                        >
                          <fdev-input
                            outlined
                            ref="assessmentModel.oa_real_no"
                            :readonly="isEditOaNoReadonly"
                            v-model="assessmentModel.oa_real_no"
                            type="text"
                            dense
                            hint=""
                            @blur="checkOANum"
                            :rules="[
                              () =>
                                $v.assessmentModel.oa_real_no.regRule ||
                                '输入格式不正确',
                              () =>
                                $v.assessmentModel.oa_real_no.useRule ||
                                '当前需求编号已被使用，请重新申请'
                            ]"
                          />
                        </Field>
                      </div>
                      <Field
                        label="OA联系单名称"
                        :label-col="4"
                        class="col-6"
                        align="right"
                      >
                        <fdev-input
                          outlined
                          readonly
                          v-model="assessmentModel.oa_contact_name"
                          type="text"
                          dense
                          hint=""
                        />
                      </Field>
                      <Field label="OA收件日期" :label-col="4" align="right">
                        <fdev-select
                          outlined
                          v-model="assessmentModel.oa_receive_date"
                          type="text"
                          dense
                          mask="date"
                          :clearable="true"
                          :hide-dropdown-icon="true"
                          hint=""
                        >
                          <template v-slot:append>
                            <fdev-icon name="event" class="cursor-pointer">
                              <fdev-popup-proxy
                                ref="qDateProxyOAReceive"
                                transition-show="scale"
                                transition-hide="scale"
                              >
                                <fdev-date
                                  @input="
                                    () => $refs.qDateProxyOAReceive.hide()
                                  "
                                  v-model="assessmentModel.oa_receive_date"
                                  mask="YYYY-MM-DD"
                                />
                              </fdev-popup-proxy>
                            </fdev-icon>
                          </template>
                        </fdev-select>
                      </Field>
                      <Field label="需求背景" :label-col="4" align="right">
                        <fdev-input
                          outlined
                          v-model="assessmentModel.demand_background"
                          type="textarea"
                          dense
                          hint=""
                        />
                      </Field>
                      <Field label="需求是否可行" :label-col="4" align="right">
                        <fdev-select
                          class="select"
                          dense
                          outlined
                          map-options
                          emit-value
                          v-model="assessmentModel.demand_available"
                          :options="demandAvailables"
                          hint=""
                        />
                      </Field>
                      <Field label="需求评估方式" :label-col="4" align="right">
                        <fdev-select
                          class="select"
                          dense
                          outlined
                          clearable
                          map-options
                          emit-value
                          v-model="assessmentModel.demand_assess_way"
                          :options="demandAssessWays"
                          hint=""
                        />
                      </Field>
                      <Field label="需求备案编号" :label-col="4" align="right">
                        <fdev-input
                          outlined
                          v-model="assessmentModel.demand_record_no"
                          type="text"
                          dense
                          hint=""
                        />
                      </Field>
                      <Field
                        label="需求可行性评估意见"
                        :label-col="4"
                        align="right"
                      >
                        <fdev-input
                          outlined
                          v-model="assessmentModel.available_assess_idea"
                          type="textarea"
                          dense
                          hint=""
                        />
                      </Field>
                    </div>
                    <div class="col">
                      <Field
                        label="需求说明书名称"
                        :label-col="4"
                        align="right"
                      >
                        <fdev-input
                          outlined
                          v-model="assessmentModel.demand_instruction"
                          type="text"
                          dense
                          hint=""
                        />
                      </Field>
                      <Field
                        label="对应需求计划编号"
                        :label-col="4"
                        align="right"
                      >
                        <fdev-input
                          outlined
                          ref="assessmentModel.demand_plan_no"
                          v-model="$v.assessmentModel.demand_plan_no.$model"
                          type="text"
                          dense
                          :rules="[
                            () =>
                              $v.assessmentModel.demand_plan_no.required ||
                              '请输入对应需求计划编号',
                            () =>
                              $v.assessmentModel.demand_plan_no.regRule ||
                              '输入格式不正确（样例：2020零售信贷038）'
                          ]"
                        />
                      </Field>
                      <Field label="需求计划名称" :label-col="4" align="right">
                        <fdev-input
                          outlined
                          v-model="assessmentModel.demand_plan_name"
                          type="text"
                          dense
                          hint=""
                        />
                      </Field>
                      <Field align="right" label="前期沟通情况" :label-col="4">
                        <fdev-input
                          outlined
                          v-model="assessmentModel.former_communication"
                          type="textarea"
                          dense
                          hint=""
                        />
                      </Field>
                      <Field
                        label="是否纳入后评估"
                        :label-col="4"
                        align="right"
                      >
                        <fdev-select
                          class="select"
                          dense
                          clearable
                          outlined
                          map-options
                          emit-value
                          option-value="value"
                          v-model="assessmentModel.future_assess"
                          :options="futureAssess"
                          hint=""
                        />
                      </Field>
                      <Field label="评审人" :label-col="4" align="right">
                        <fdev-input
                          outlined
                          v-model="assessmentModel.review_user"
                          type="text"
                          dense
                          hint=""
                        />
                      </Field>
                      <Field
                        label="需求期望投产日期"
                        :label-col="4"
                        align="right"
                      >
                        <fdev-select
                          outlined
                          v-model="assessmentModel.respect_product_date"
                          type="text"
                          dense
                          mask="date"
                          :clearable="true"
                          :hide-dropdown-icon="true"
                          hint=""
                        >
                          <template v-slot:append>
                            <fdev-icon name="event" class="cursor-pointer">
                              <fdev-popup-proxy
                                ref="qDateProxyRespectProduct"
                                transition-show="scale"
                                transition-hide="scale"
                              >
                                <fdev-date
                                  @input="
                                    () => $refs.qDateProxyRespectProduct.hide()
                                  "
                                  v-model="assessmentModel.respect_product_date"
                                  mask="YYYY-MM-DD"
                                />
                              </fdev-popup-proxy>
                            </fdev-icon>
                          </template>
                        </fdev-select>
                      </Field>
                    </div>
                  </div>
                </fdev-card-section>
              </fdev-card>
              <fdev-card>
                <fdev-card-section>
                  <div class="row">
                    <div class="title">安排与实施</div>
                  </div>
                  <div class="row">
                    <div class="col">
                      <Field label="优先级" :label-col="4" align="right">
                        <fdev-select
                          class="select"
                          dense
                          outlined
                          ref="assessmentModel.priority"
                          v-model="$v.assessmentModel.priority.$model"
                          :options="priorities"
                          emit-value
                          map-options
                          :rules="[
                            () =>
                              $v.assessmentModel.priority.required ||
                              '请选择优先级'
                          ]"
                        />
                      </Field>
                    </div>
                    <div class="col"></div>
                  </div>
                </fdev-card-section>
              </fdev-card>
              <fdev-card>
                <fdev-card-section>
                  <div class="row">
                    <div class="title">评估安排</div>
                  </div>
                  <div class="row">
                    <div class="col">
                      <Field label="涉及板块" :label-col="4" align="right">
                        <fdev-select
                          class="select"
                          multiple
                          use-input
                          use-chips
                          dense
                          outlined
                          new-value-mode="add-unique"
                          ref="assessmentModel.relate_part"
                          v-model="$v.assessmentModel.relate_part.$model"
                          :options="relatedFilterGroup"
                          @add="addRelatedGroups"
                          @filter="relatedGroupFilter"
                          @remove="removeRelatedGroup"
                          :rules="[
                            () =>
                              $v.assessmentModel.relate_part.required ||
                              '请选择涉及板块'
                          ]"
                        >
                          <template v-slot:hint>
                            <span v-if="isAssessName.length > 0" class="q-pr-sm"
                              >{{
                                isAssessName.toString()
                              }}为评估中/评估完成状态，不可删除</span
                            >
                          </template>
                          <!-- 涉及板块中如有评估中、评估完成状态，则不能去掉 -->
                          <template v-slot:selected-item="scope">
                            <fdev-chip
                              dense
                              :removable="!isAssessId.includes(scope.opt.id)"
                              @remove="scope.removeAtIndex(scope.index)"
                              :tabindex="scope.tabindex"
                            >
                              {{ scope.opt.name }}
                            </fdev-chip>
                          </template>
                          <template v-slot:option="scope">
                            <fdev-item
                              v-bind="scope.itemProps"
                              v-on="scope.itemEvents"
                            >
                              <fdev-item-section>
                                <fdev-item-label>{{
                                  scope.opt.name
                                }}</fdev-item-label>
                                <fdev-item-label caption>
                                  {{ scope.opt.fullName }}
                                </fdev-item-label>
                              </fdev-item-section>
                            </fdev-item>
                          </template>
                        </fdev-select>
                      </Field>
                      <Field
                        v-if="greatKey.length !== 0"
                        label="涉及板块评估人"
                        :label-col="4"
                        align="right"
                      >
                        <div class="row" v-if="greatKey.length !== 0">
                          <div
                            class="col-10"
                            v-for="(item, index) in $v.greatKey.$each.$iter"
                            :key="index"
                            style="margin-bottom:10px;"
                          >
                            <Field
                              :label="item.group.$model.label"
                              :label-col="4"
                              align="right"
                            >
                              <fdev-select
                                multiple
                                use-chips
                                use-input
                                dense
                                outlined
                                :ref="`assessmentModel${index}`"
                                v-model="item.users.$model"
                                :options="item.options.$model"
                                @focus="handleInputFocus(item, index)"
                                @filter="relatedUsersFilter"
                                :rules="[
                                  () => item.users.required || '请选择涉及人员'
                                ]"
                              >
                                <template v-slot:option="scope">
                                  <fdev-item
                                    v-bind="scope.itemProps"
                                    v-on="scope.itemEvents"
                                  >
                                    <fdev-item-section>
                                      <fdev-item-label>
                                        {{ scope.opt.user_name_cn }}
                                      </fdev-item-label>
                                      <fdev-item-label caption>
                                        {{ scope.opt.user_name_en }}--{{
                                          scope.opt.group
                                        }}
                                      </fdev-item-label>
                                    </fdev-item-section>
                                  </fdev-item>
                                </template>
                              </fdev-select>
                            </Field>
                          </div>
                        </div>
                      </Field>
                    </div>
                    <div class="col">
                      <Field
                        label="是否涉及UI审核"
                        :label-col="4"
                        align="right"
                      >
                        <fdev-select
                          class="select"
                          dense
                          outlined
                          map-options
                          emit-value
                          :readonly="!assessmentModel.ui_status"
                          ref="assessmentModel.ui_verify"
                          v-model="$v.assessmentModel.ui_verify.$model"
                          :options="uiList"
                          hint=""
                          :rules="[
                            () =>
                              $v.assessmentModel.ui_verify.required ||
                              '请选择是否涉及UI审核'
                          ]"
                        />
                      </Field>
                      <Field
                        label="实施团队可行性评估补充意见"
                        class="col-12 q-mb-md"
                        :label-col="4"
                        align="right"
                      >
                        <fdev-input
                          outlined
                          ref="assessmentModel.extra_idea"
                          v-model="$v.assessmentModel.extra_idea.$model"
                          type="textarea"
                          dense
                          :rules="[
                            () =>
                              $v.assessmentModel.extra_idea.required ||
                              '请输入实施团队可行性评估补充意见'
                          ]"
                        />
                      </Field>
                    </div>
                  </div>
                </fdev-card-section>
              </fdev-card>
            </div>
            <fdev-stepper-navigation>
              <div class="row justify-center">
                <fdev-btn
                  color="primary"
                  label="取消"
                  class="q-mb-md q-mr-lg"
                  @click="confirmToClose"
                />
                <fdev-btn
                  color="primary"
                  label="完成"
                  class="q-mb-md"
                  @click="sure"
                />
              </div>
            </fdev-stepper-navigation>
          </fdev-form>
        </fdev-page>
      </fdev-page-container>
    </fdev-layout>
  </fdev-dialog>
</template>
<script>
import { mapGetters, mapState, mapActions } from 'vuex';
import Field from '@/components/Field';
import {
  demandAvailables,
  futureAssess,
  demandAssessWays,
  priorities,
  uiList,
  queryUserOptionsParams
} from '../model';
import axios from 'axios';
import moment from 'moment';
import { required } from 'vuelidate/lib/validators';
import { formatUser } from '@/modules/User/utils/model';
import {
  formatOption,
  baseUrl,
  errorNotify,
  successNotify,
  exportExcel,
  validate
} from '@/utils/utils';
export default {
  props: {
    value: {
      type: Boolean,
      default: false
    },
    data: {
      type: Object,
      default: () => {}
    }
  },
  components: {
    Field
  },
  computed: {
    ...mapGetters('user', ['isLoginUserList', 'isDemandManager']),
    ...mapState('userForm', {
      groupsData: 'groups',
      userInPage: 'userInPage'
    }),
    ...mapState('demandsForm', ['oaNOStatus', 'modelExcel'])
  },
  data() {
    return {
      assessmentModel: {},
      groups: [],
      groupsClone: [],
      demandAvailables,
      futureAssess,
      demandAssessWays,
      priorities,
      uiList,
      relatedFilterGroup: [],
      focusInputIndex: '',
      greatKey: [],
      users: [],
      isAssessId: [],
      isAssessName: [],
      userOptions: [],
      fileContent: '',
      useRuled: true,
      isEditOaNoReadonly: false
    };
  },
  validations: {
    assessmentModel: {
      //oa联系单编号
      oa_real_no: {
        regRule(val) {
          if (!val) {
            return true;
          }
          const value = val.replace(/(^\s*)|(\s*$)/g, '');
          const arr = value.split('-');
          if (arr.length > 1) {
            const arr1 = arr.slice(1);
            const arr2 = ['M001', 'N001', '001'];
            const rule = arr1.some(item => {
              return arr2.indexOf(item) > 0;
            });
            if (rule) {
              return false;
            }
          }
          if (arr.length > 2 && arr.length < 6) {
            // UT-ZZZZ-YYYY-XXXX
            if (arr.length === 4) {
              if (arr[0] !== 'UT') {
                return false;
              } else {
                let re = new RegExp(/^\d+$/);
                let re1 = new RegExp(/^[a-zA-Z]+$/);
                if (!re.test(arr[2]) || !re.test(arr[3])) {
                  return false;
                } else {
                  return re1.test(arr[1]);
                }
              }
            } else if (arr.length === 5) {
              // 0001-YYYY-L-ZZZ-XXXXXX
              if (arr[0] !== '0001') {
                return false;
              } else {
                let re = new RegExp(/^\d+$/);
                if (!re.test(arr[1]) || !re.test(arr[3]) || !re.test(arr[4])) {
                  return false;
                } else {
                  return arr[2] === 'L';
                }
              }
            } else {
              return false;
            }
          } else {
            return false;
          }
        },
        useRule(val) {
          return val ? this.useRuled : true;
        }
      },
      ui_verify: {
        required
      },
      plan_user: {
        required,
        includeChinese(value) {
          if (!value) {
            return true;
          }
          const reg = new RegExp(/[\u4e00-\u9fa5]/gm);
          const flag = reg.test(value);
          return flag;
        }
      },
      priority: {
        required
      },
      demand_plan_no: {
        required,
        regRule(val) {
          const value = val.replace(/(^\s*)|(\s*$)/g, '');
          const reg = new RegExp(/^[0-9]{4}[\u4e00-\u9fa5]{2,6}[0-9]{3,4}$/);
          return reg.test(value);
        }
      },
      accept_date: {
        required
      },
      relate_part: {
        required
      },
      extra_idea: {
        required
      }
    },
    greatKey: {
      required,
      $each: {
        group: {
          required
        },
        users: {
          required
        },
        options: {}
      }
    }
  },
  methods: {
    ...mapActions('userForm', {
      queryGroup: 'fetchGroup',
      queryUserPagination: 'queryUserPagination'
    }),
    ...mapActions('demandsForm', [
      'queryDemandByOaContactNo',
      'exportModelExcel',
      'updateImpl'
    ]),
    handleSelectFile() {
      this.$refs.fileInput.click();
    },
    async handleExportModelExcel() {
      await this.exportModelExcel();
      exportExcel(this.modelExcel);
    },
    async uploadFile(event) {
      const formData = new FormData();
      const fileName = this.$refs.fileInput.files[0].name.split('.')[1];
      const fileSize = this.$refs.fileInput.files[0].size;
      const file = this.$refs.fileInput.files[0];
      if (fileName === 'xlsx' && fileSize > 0 && fileSize <= 5242880) {
        formData.append('file', file);
        formData.append('demandId', this.data.id);
        const config = {
          headers: {
            'Content-Type': 'multipart/form-data',
            Accept: 'application/json',
            Authorization: this.$q.localStorage.getItem('fdev-vue-admin-jwt')
          }
        };
        axios
          .post(
            `${baseUrl}fdemand/api/demand/importDemandExcel`,
            formData,
            config
          )
          .then(async res => {
            const { code, data } = res.data;
            if (code !== 'AAAAAAA') {
              this.$refs.fileInput.value = null;
              errorNotify(code);
              return;
            }
            this.fileContent = data;
            //对OA 收件日期进行转换
            if (typeof this.fileContent.oa_receive_date === 'number') {
              this.fileContent.oa_receive_date = moment(
                this.fileContent.oa_receive_date
              ).format('YYYY-MM-DD');
            } else {
              const date = moment(this.fileContent.oa_receive_date).format(
                'YYYY-MM-DD'
              );
              if (date === 'Invalid date') {
                this.fileContent.oa_receive_date = '';
              } else {
                this.fileContent.oa_receive_date = date;
              }
            }
            //对需求方期望投产日期进行转换
            if (typeof this.fileContent.respect_product_date === 'number') {
              this.fileContent.respect_product_date = moment(
                this.fileContent.respect_product_date
              ).format('YYYY-MM-DD');
            } else {
              const date = moment(this.fileContent.respect_product_date).format(
                'YYYY-MM-DD'
              );
              if (date === 'Invalid date') {
                this.fileContent.respect_product_date = '';
              } else {
                this.fileContent.respect_product_date = date;
              }
            }
            if (this.fileContent.demand_available !== '') {
              this.fileContent.demand_available = this.demandAvailables.find(
                item => item.label === this.fileContent.demand_available
              );
            }
            if (this.fileContent.demand_assess_way !== '') {
              this.fileContent.demand_assess_way = this.demandAssessWays.find(
                item => item.label === this.fileContent.demand_assess_way
              );
            }
            if (this.fileContent.future_assess !== '') {
              this.fileContent.future_assess = this.futureAssess.find(
                item => item.label === this.fileContent.future_assess
              );
            }
            for (let key in this.fileContent) {
              if (Reflect.has(this.assessmentModel, key)) {
                if (key == 'oa_contact_name') {
                  if (this.fileContent[key] != '') {
                    this.assessmentModel[key] = this.fileContent[key];
                  }
                } else {
                  this.assessmentModel[key] = this.fileContent[key];
                }
              }
            }
            successNotify('上传成功！');
            this.$refs.fileInput.value = null;
          })
          .catch(err => {
            this.$refs.fileInput.value = null;
            errorNotify('上传失败，请重试!');
          });
      } else {
        this.$refs.fileInput.value = null;
        errorNotify('该文件不能被上传或文件大小不被允许');
      }
    },
    handleInputFocus(item, index) {
      this.focusInputIndex = parseInt(index);
    },
    relatedGroupFilter(val, update, abort) {
      //涉及板块选择框输入过滤
      update(() => {
        this.relatedFilterGroup = this.groupsClone.filter(
          group => group.fullName.indexOf(val) > -1
        );
      });
    },
    //当前获取焦点的涉及板块人员选择框，以其所属板块id和输入，过滤选择框选项
    relatedUsersFilter(val, update, abort) {
      //如果为业务需求，则牵头人下拉为全量行内人员
      this.userOptions = this.userOptions.filter(item => {
        if (item.email.indexOf('@') > -1) {
          return item.email.split('@')[1] === 'spdb.com.cn';
        }
      });
      update(() => {
        this.greatKey[this.focusInputIndex].options = this.userOptions.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
      //遍历获取小组名
      for (let key in this.greatKey[this.focusInputIndex].options) {
        let userGroup = this.groups.filter(
          item =>
            this.greatKey[this.focusInputIndex].options[key].group_id == item.id
        );
        this.greatKey[this.focusInputIndex].options[key].group =
          userGroup[0].name;
      }
    },
    userOptionsFilter(group_id) {
      //传进来板块ID的话，以板块ID来过滤人员
      const myUser = this.userOptions.filter(item => {
        if (item.email.indexOf('@') > -1) {
          return item.email.split('@')[1] === 'spdb.com.cn';
        }
      });
      return myUser;
    },
    //增加涉及板块，添加节点，动态生成选择框，节点包含板块信息和用来绑定选择人员的users数组
    addRelatedGroups({ index, value }) {
      if (value.id === this.assessmentModel.demand_leader_group.id) {
        this.greatKey.unshift({
          group: value,
          users: [],
          options: this.userOptionsFilter(value.id),
          optionsClone: this.userOptionsFilter(value.id)
        });
      } else {
        this.greatKey.push({
          group: value,
          users: [],
          options: this.userOptionsFilter(value.id),
          optionsClone: this.userOptionsFilter(value.id)
        });
      }
    },
    removeRelatedGroup(detail) {
      //从涉及板块选择框中删除板块时，将相应的节点删除
      const index = this.greatKey.findIndex(item => {
        return item.group.id === detail.value.id;
      });
      this.greatKey.splice(index, 1);
    },
    async checkOANum() {
      if (!this.assessmentModel.oa_real_no.trim()) return;
      await this.queryDemandByOaContactNo({
        oa_contact_no: this.assessmentModel.oa_real_no
      });
      if (this.oaNOStatus) {
        this.useRuled = false;
      } else {
        this.useRuled = true;
      }
      this.$v.assessmentModel.oa_real_no.$touch();
      validate([this.$refs['assessmentModel.oa_real_no']]);
    },
    validateForm() {
      this.$v.assessmentModel.$touch();
      this.$v.greatKey.$touch();
      const keys = Object.keys(this.$refs).filter(key => {
        return (
          this.$refs[key] &&
          key.indexOf('assessmentModel') > -1 &&
          (Array.isArray(this.$refs[key]) ? this.$refs[key].length > 0 : true)
        );
      });
      validate(
        keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
    },
    sure() {
      let relatePartId = this.assessmentModel.relate_part.map(val => {
        return val.id;
      });
      if (
        !this.isAssessId.every(val => {
          return relatePartId.includes(val);
        })
      ) {
        errorNotify('请勿删除评估中/评估完成状态的涉及板块');
        return;
      }
      this.validateForm();
      const keys = Object.keys(this.$refs).filter(key => {
        return (
          this.$refs[key] &&
          key.indexOf('assessmentModel') > -1 &&
          (Array.isArray(this.$refs[key]) ? this.$refs[key].length > 0 : true)
        );
      });
      if (this.$v.greatKey.$invalid) {
        return;
      }
      if (this.$v.assessmentModel.$invalid) {
        const _this = this;
        const validateRes = keys.every(item => {
          if (item.indexOf('.') === -1) {
            return true;
          }
          const itemArr = item.split('.');
          return _this.$v.assessmentModel[itemArr[1]].$invalid == false;
        });
        if (!validateRes) {
          return;
        }
      }
      if (this.isEditOaNoReadonly) {
        this.submitAssessment();
      } else {
        if (this.assessmentModel.oa_real_no) {
          this.$q
            .dialog({
              title: '温馨提示',
              message: '需求编号同步调整为OA联系单编号',
              cancel: true,
              persistent: true
            })
            .onOk(() => {
              this.submitAssessment();
            });
        } else {
          this.submitAssessment();
        }
      }
    },
    async submitAssessment() {
      let relatePartId = this.assessmentModel.relate_part.map(val => {
        return val.id;
      });
      this.assessmentModel.relate_part_detail = this.greatKey.map(item => {
        return {
          part_id: item.group.id,
          part_name: item.group.label,
          assess_status: item.assess_status ? item.assess_status : '0',
          assess_user_all: item.users.map(user => {
            return {
              id: user.id,
              user_name_cn: user.user_name_cn,
              user_name_en: user.user_name_en
            };
          })
        };
      });
      let params = {
        ...this.assessmentModel,
        demand_leader_group: this.assessmentModel.demand_leader_group.id,
        relate_part: relatePartId,
        relate_part_detail: this.assessmentModel.relate_part_detail,
        demand_leader_group_cn: this.assessmentModel.demand_leader_group
          .fullName
      };
      //quasar的select组件没有校验的话，取的值有时候是取的value，有时候取的是整个对象。。暂时特殊处理
      if (
        params.demand_assess_way &&
        typeof params.demand_assess_way == 'object'
      ) {
        params.demand_assess_way = params.demand_assess_way.value;
      } else if (!params.demand_assess_way) {
        params.demand_assess_way = '';
      }
      if (
        params.demand_available &&
        typeof params.demand_available == 'object'
      ) {
        params.demand_available = params.demand_available.value;
      } else if (!params.demand_available) {
        params.demand_available = '';
      }

      if (params.future_assess && typeof params.future_assess == 'object') {
        params.future_assess = params.future_assess.value;
      } else if (!params.future_assess) {
        params.future_assess = '';
      }
      await this.updateImpl(params);
      successNotify('提交成功');
      // 评估 路由更换为 需求详情 下的 研发单元(developNo)tab
      this.$router.push({
        path: `/rqrmn/rqrProfile/${this.assessmentModel.id}`,
        query: { tab: 'developNo' }
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
          this.assessmentModel = JSON.parse(JSON.stringify(this.data));
          this.$emit('input', false);
        });
    },
    async initQuery() {
      await this.queryGroup();
      this.groups = formatOption(this.groupsData);
      await this.queryUserPagination(queryUserOptionsParams);
      this.users = this.userInPage.list.map(user =>
        formatOption(formatUser(user), 'name')
      );
      this.userOptions = this.users.slice(0);
      this.relatedFilterGroup = this.groups;
      this.assessmentModel = JSON.parse(JSON.stringify(this.data));
      // 组装keylist数组
      let keylist = this.assessmentModel.relate_part_detail.map(val => {
        let userArray = [];
        if (val.assess_user_all) {
          val.assess_user_all.map(item => {
            this.userOptions.filter(user => {
              if (user.id === item.id) userArray.push(user);
            });
          });
        }
        // 如果涉及板块评估状态为评估中或者评估完成，则不能修改相关板块。
        if (val.assess_status === '1' || val.assess_status === '2') {
          this.isAssessId.push(val.part_id);
          this.isAssessName.push(val.part_name);
        }
        const _this = this;
        return {
          group: { id: val.part_id, label: val.part_name },
          users: userArray,
          assess_status: val.assess_status,
          options: _this.userOptionsFilter(val.part_id),
          optionsClone: _this.userOptionsFilter(val.part_id)
        };
      });
      this.greatKey = keylist;

      this.groupsClone = JSON.parse(JSON.stringify(this.groups));
      this.relatedFilterGroup = this.groups.map(item => {
        if (this.isAssessId.includes(item.id)) {
          item.disable = true;
        }
        return item;
      });

      // 涉及板块赋值
      let relatePartList = [];
      this.assessmentModel.relate_part_detail.filter(item => {
        this.groups.forEach(val => {
          if (val.id === item.part_id) {
            relatePartList.push(val);
          }
        });
      });
      this.assessmentModel.relate_part = relatePartList;

      if (this.assessmentModel.oa_real_no) {
        this.isEditOaNoReadonly = true;
      } else {
        this.isEditOaNoReadonly = false;
      }

      this.validateForm();
    }
  }
};
</script>
<style lang="stylus" scoped>

.card {
  padding: 10px;
}
.title {
  font-size: 18px;
}
.myspan {
  font-size: 16px;
}
.pre-style
  display block
  line-height 1.8em
.mybtn {
  position: absolute;
  left: 0;
  top: 3px;
}
.relative {
  position: relative;
}
</style>
