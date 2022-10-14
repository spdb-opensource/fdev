<template>
  <f-block page>
    <Loading class="orderPage" :visible="loading">
      <div class="row items-center titleLine">
        <f-icon name="list_s_f" class="titleimg" />
        <span class="titlename">{{
          type === '4' ? '提测单修改' : '创建提测单'
        }}</span>
      </div>
      <div class="row justify-between ml-28 mr-102">
        <!-- 小组 -->
        <f-formitem
          label="小组"
          required
          bottom-page
          label-style="width: 112px"
        >
          <fdev-select
            use-input
            ref="createModel.group"
            v-model="createModel.group"
            :options="leaderGroupOptions"
            @filter="leaderGroupFilter"
            option-label="name"
            :rules="[() => $v.createModel.group.required || '请选择小组']"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name">{{
                    scope.opt.name
                  }}</fdev-item-label>
                  <fdev-item-label :title="scope.opt.fullName" caption>
                    {{ scope.opt.fullName }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem
          label="需求名称"
          bottom-page
          required
          label-style="width:112px"
        >
          <fdev-select
            :readonly="type === '1' ? true : false"
            use-input
            ref="createModel.demand_id"
            v-model="$v.createModel.demand_id.$model"
            :options="demandOptions"
            @filter="rqrmntInputFilter"
            @input="demandInput($event)"
            option-label="oa_contact_name"
            option-value="oa_contact_no"
            :rules="[() => $v.createModel.demand_id.required || '请选择需求']"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.oa_contact_name">
                    {{ scope.opt.oa_contact_name }}
                  </fdev-item-label>
                  <fdev-item-label
                    :title="`${scope.opt.oa_contact_no}`"
                    caption
                  >
                    {{ scope.opt.oa_contact_no }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem label="需求编号" bottom-page label-style="width:112px">
          <fdev-input
            placeholder="请选择需求名称"
            v-model="createModel.oa_contact_no"
            readonly
            :title="createModel.oa_contact_no"
            hint=""
          >
          </fdev-input>
        </f-formitem>
        <f-formitem
          label="研发单元编号"
          bottom-page
          required
          label-style="width:112px"
        >
          <fdev-select
            multiple
            use-input
            ref="createModel.fdev_implement_unit_no"
            v-model="$v.createModel.fdev_implement_unit_no.$model"
            :options="implementOptions"
            @filter="implementInputFilter"
            @input="implementInput($event)"
            option-label="fdev_implement_unit_no"
            option-value="fdev_implement_unit_no"
            :rules="[
              () =>
                $v.createModel.fdev_implement_unit_no.required ||
                '请选择研发单元编号'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-tooltip v-if="isFdevIpmpShow(scope.opt)">
                    {{ fdevIpmpTip(scope.opt) }}
                  </fdev-tooltip>
                  <fdev-item-label :title="scope.opt.fdev_implement_unit_no">
                    {{ scope.opt.fdev_implement_unit_no }}
                  </fdev-item-label>
                  <fdev-item-label
                    :title="`${scope.opt.ipmp_implement_unit_no}`"
                    caption
                  >
                    {{ scope.opt.ipmp_implement_unit_no }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem label="IPMP实施单元" bottom-page label-style="width:112px">
          <fdev-input
            hint=""
            placeholder="请选择研发单元"
            readonly
            :title="createModel.impl_unit_num_show"
            v-model="createModel.impl_unit_num_show"
          />
        </f-formitem>
        <f-formitem
          label="业务人员"
          bottom-page
          required
          label-style="width:128px"
          help="输入多个业务人员邮箱时，以英文分号分隔"
        >
          <fdev-input
            placeholder="请输入业务人员的邮箱"
            ref="createModel.business_email"
            v-model="$v.createModel.business_email.$model"
            :rules="[
              () =>
                $v.createModel.business_email.required ||
                '请输入业务人员的邮箱',
              () =>
                $v.createModel.business_email.isOk ||
                '不能输入中文或中文符号且多个邮箱以英文分号分隔'
            ]"
          >
          </fdev-input>
        </f-formitem>
        <f-formitem
          label="测试经理"
          bottom-page
          required
          label-style="width:112px"
        >
          <fdev-select
            multiple
            use-input
            ref="createModel.test_manager_info"
            v-model="$v.createModel.test_manager_info.$model"
            :options="testManagesOptions"
            @filter="testManagerFilter"
            option-label="user_name_cn"
            option-value="user_name_en"
            :rules="[
              () => $v.createModel.test_manager_info.IsLen || '请选择测试经理'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label
                    :title="
                      `${scope.opt.user_name_en}--${scope.opt.user_email}`
                    "
                    caption
                  >
                    {{ scope.opt.user_name_en }}--{{ scope.opt.user_email }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem
          label="提测邮件通知抄送人员"
          bottom-page
          required
          label-style="width:112px"
        >
          <fdev-select
            multiple
            use-input
            ref="createModel.test_cc_user_ids"
            v-model="$v.createModel.test_cc_user_ids.$model"
            :options="testUserOptions"
            @filter="testUserFilter"
            option-label="user_name_cn"
            option-value="id"
            :rules="[
              () =>
                $v.createModel.test_cc_user_ids.IsLen ||
                '请选择提测邮件通知抄送人员'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label
                    :title="`${scope.opt.user_name_en}--${scope.opt.groupName}`"
                    caption
                  >
                    {{ scope.opt.user_name_en }}--{{ scope.opt.groupName }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem
          label="开发人员"
          bottom-page
          required
          label-style="width:128px"
          help="示例： 售前:XXX   售中：XXX   定投：XXX   注：各开发负责的模块需描述清楚（负责的功能模块：开发人员）"
        >
          <fdev-input
            type="textarea"
            ref="createModel.developer"
            v-model="$v.createModel.developer.$model"
            :rules="[
              () => $v.createModel.developer.required || '请输入开发人员'
            ]"
          />
        </f-formitem>
        <f-formitem
          label="测试内容"
          bottom-page
          required
          label-style="width:128px"
          help="注：如提测内容仅为需规部分功能点，请注明功能点。测试内容对应的测试版本不同的，也请注明。"
        >
          <fdev-input
            ref="createModel.test_content"
            type="textarea"
            v-model="$v.createModel.test_content.$model"
            :rules="[
              () => $v.createModel.test_content.required || '请输入测试内容'
            ]"
          />
        </f-formitem>
        <f-formitem label="单元测试情况" bottom-page label-style="width:112px">
          <fdev-input v-model="createModel.unit_test_result" />
        </f-formitem>
        <f-formitem
          label="内测通过情况"
          bottom-page
          required
          label-style="width:112px"
        >
          <fdev-select
            ref="createModel.inner_test_result"
            v-model="$v.createModel.inner_test_result.$model"
            :options="testResOptions"
            option-label="innerTestTab"
            option-value="innerTestTab"
            :rules="[
              () =>
                $v.createModel.inner_test_result.required ||
                '请选择内测通过情况'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-tooltip v-if="isTestShow(scope.opt)">
                    {{ testTip(scope.opt) }}
                  </fdev-tooltip>
                  <fdev-item-label :title="scope.opt.innerTestTab">
                    {{ scope.opt.innerTestTab }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem
          label="是否涉及交易接口改动"
          bottom-page
          required
          label-style="width:112px"
        >
          <fdev-select
            ref="createModel.trans_interface_change"
            v-model="$v.createModel.trans_interface_change.$model"
            :options="YesOrNo"
            :rules="[
              () =>
                $v.createModel.trans_interface_change.required ||
                '请选择是否涉及交易接口改动'
            ]"
          />
        </f-formitem>
        <f-formitem
          label="是否涉及数据库改动"
          bottom-page
          required
          label-style="width:112px"
        >
          <fdev-select
            ref="createModel.database_change"
            v-model="$v.createModel.database_change.$model"
            :options="YesOrNo"
            :rules="[
              () =>
                $v.createModel.database_change.required ||
                '请选择是否涉及数据库改动'
            ]"
          />
        </f-formitem>
        <f-formitem
          label="是否涉及回归测试"
          bottom-page
          required
          label-style="width:112px"
        >
          <fdev-select
            ref="createModel.regress_test"
            v-model="$v.createModel.regress_test.$model"
            :options="YesOrNo"
            @input="verifyModel('createModel.regress_test_range')"
            :rules="[
              () =>
                $v.createModel.regress_test.required || '请选择是否涉及回归测试'
            ]"
          />
        </f-formitem>
        <f-formitem
          label="是否涉及客户端更新"
          bottom-page
          required
          label-style="width:112px"
        >
          <fdev-select
            ref="createModel.client_change"
            v-model="$v.createModel.client_change.$model"
            :options="YesOrNo"
            @input="verifyModel('createModel.client_download')"
            :rules="[
              () =>
                $v.createModel.client_change.required ||
                '请选择是否涉及客户端更新'
            ]"
          />
        </f-formitem>
        <f-formitem
          label="具体回归测试范围"
          bottom-page
          :required="createModel.regress_test.value === 'yes' ? true : false"
          label-style="width:112px"
        >
          <fdev-input
            use-input
            type="textarea"
            ref="createModel.regress_test_range"
            v-model="$v.createModel.regress_test_range.$model"
            :rules="[
              () =>
                $v.createModel.regress_test_range.isOk ||
                '请选择具体回归测试范围',
              () =>
                $v.createModel.regress_test_range.maxLen ||
                '最大输入长度不能超过500'
            ]"
          />
        </f-formitem>
        <f-formitem
          label="客户端下载地址(明确具体测试包)"
          bottom-page
          required
          label-style="width:112px"
        >
          <fdev-input
            type="textarea"
            ref="createModel.client_download"
            v-model="$v.createModel.client_download.$model"
            :rules="[
              () =>
                $v.createModel.client_download.required ||
                '请输入客户端下载地址'
            ]"
          />
        </f-formitem>
        <f-formitem
          label="涉及关联系统同步改造"
          bottom-page
          label-style="width:112px"
        >
          <fdev-input hint="" use-input v-model="createModel.system" />
        </f-formitem>
        <f-formitem
          label="测试日报抄送人员"
          bottom-page
          label-style="width:112px"
        >
          <fdev-select
            multiple
            use-input
            v-model="createModel.daily_cc_user_ids"
            :options="dailyUserOptions"
            @filter="dailyUserFilter"
            option-label="user_name_cn"
            option-value="id"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label
                    :title="`${scope.opt.user_name_en}--${scope.opt.groupName}`"
                    caption
                  >
                    {{ scope.opt.user_name_en }}--{{ scope.opt.groupName }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem
          label="测试环境"
          bottom-page
          required
          label-style="width:112px"
        >
          <fdev-input
            ref="createModel.test_environment"
            type="textarea"
            v-model="$v.createModel.test_environment.$model"
            :rules="[
              () => $v.createModel.test_environment.required || '请输入测试环境'
            ]"
          />
        </f-formitem>
        <f-formitem label="备注" bottom-page label-style="width:112px">
          <fdev-input type="textarea" hint="" v-model="createModel.remark" />
        </f-formitem>
      </div>
      <div class="row justify-between ml-28 mr-88" v-if="type !== '4'">
        <f-formitem
          label="需求说明书"
          bottom-page
          required
          label-style="width:112px"
        >
          <fdev-btn
            dash
            ficon="upload"
            :label="
              createModel.deInstruction_files.length > 0
                ? '继续选择'
                : '选择文件'
            "
            @click="openFiles('1')"
          />
          <span class="q-ml-sm">
            <span
              class="text-grey-7"
              v-show="createModel.deInstruction_files.length === 0"
            >
              暂未选择文件
            </span>
            <div
              v-for="file in createModel.deInstruction_files"
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
                  name="close"
                  class="text-primary q-ml-sm"
                  @click="deleteInstFiles(file)"
                />
              </div>
            </div>
          </span>
          <fdev-input
            class="hideInput"
            v-model="$v.createModel.deInstruction_files.$model"
            ref="createModel.deInstruction_files"
            :rules="[
              () => $v.createModel.deInstruction_files.IsLen || '请选择文件'
            ]"
          />
        </f-formitem>
        <f-formitem
          label="需求规格说明书"
          bottom-page
          required
          label-style="width:112px"
          value-style="width:294px"
        >
          <fdev-btn
            dash
            ficon="upload"
            :label="createModel.req_files.length > 0 ? '继续选择' : '选择文件'"
            @click="openFiles('2')"
          />
          <span class="q-ml-sm">
            <span
              class="text-grey-7"
              v-show="createModel.req_files.length === 0"
            >
              暂未选择文件
            </span>
            <div
              v-for="file in createModel.req_files"
              :key="file.name"
              style="width:100px height:10px"
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
                  name="close"
                  class="text-primary q-ml-sm"
                  @click="deleteReq_Files(file)"
                />
              </div>
            </div>
          </span>
          <fdev-input
            class="hideInput"
            v-model="$v.createModel.req_files.$model"
            ref="createModel.req_files"
            :rules="[() => $v.createModel.req_files.IsLen || '请选择文件']"
          />
        </f-formitem>
        <f-formitem
          label="其他相关材料"
          bottom-page
          label-style="width:112px"
          value-style="width:294px"
        >
          <fdev-btn
            dash
            ficon="upload"
            :label="
              createModel.other_files.length > 0 ? '继续选择' : '选择文件'
            "
            @click="openFiles('3')"
          />
          <span class="q-ml-sm">
            <span
              class="text-grey-7"
              v-show="createModel.other_files.length === 0"
            >
              暂未选择文件
            </span>
            <div
              v-for="file in createModel.other_files"
              :key="file.name"
              style="width:100px height:10px"
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
                  name="close"
                  class="text-primary q-ml-sm"
                  @click="deleteOther_Files(file)"
                />
              </div>
            </div>
          </span>
        </f-formitem>
      </div>
      <div class="op-btn">
        <fdev-btn
          outline
          class="q-mr-lg"
          label="取消"
          @click="confirmToCancle"
        />
        <fdev-btn label="确定" :loading="loading" @click="submitForm" />
      </div>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState } from 'vuex';
import {
  resolveResponseError,
  formatOption,
  deepClone,
  successNotify,
  validate,
  errorNotify
} from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import {
  createSubmitTestModel,
  queryUserOptionsParams,
  YesOrNo
} from '@/modules/Rqr/utils/constants';
import {
  queryUserCoreData,
  queryTestOrderDetail,
  queryFdevUnitListByDemandId,
  queryInnerTestTab,
  addTestOrder,
  updateTestOrder,
  getTestManagerInfo,
  getThreeLevelGroup,
  queryTechBusinessForSelect
} from '@/modules/Rqr/services/methods';
import { queryGroup } from '@/modules/User/services/methods';

export default {
  name: 'addTestOrders',
  components: { Loading },
  data() {
    return {
      loading: false,
      type: '2', // type 1为研发单元入口进入新增（需要传入研发单元id），2为提交列表页进入新增，type 3为复制进入新增（需要传入被复制单id）4为进入编辑页
      submitId: '', //提测单id type为4时为当前提交测试工单id,type为3时为被复制的提交测试工单id
      detailObj: null, //提测工单详情
      demandObj: {},
      createModel: createSubmitTestModel,
      groups: [],
      leaderGroupOptions: [],
      users: [], //牵头人员
      userOptions: [], //牵头人选项
      testManagerOptions: [], //测试经理选项
      testUserOptions: [], //测试邮件抄送人员选项
      emailUsersOptions: [], //邮件通知人选项
      dailyUserOptions: [], //测试日报抄送人员选项
      demandOptions: [], //需求选项
      implement: [], //研发单元列表
      implementOptions: [],
      testResOptions: [],
      workNoObj: {}, //工单编号
      YesOrNo: YesOrNo,
      testManages: [], //测试经理
      testManagesOptions: [] //测试经理选项
    };
  },
  validations: {
    createModel: {
      group: {
        required
      },
      demand_id: {
        required
      },
      fdev_implement_unit_no: {
        required
      },
      business_email: {
        required,
        isOk(val) {
          const reg1 = /[\u4E00-\u9FA5\uF900-\uFA2D,，；]/;
          const reg = /[\uFF00-\uFFEF]/;
          return !reg1.test(val) && !reg.test(val);
        }
      },
      test_manager_info: {
        IsLen(val) {
          if (val) {
            return val.length > 0;
          } else {
            return false;
          }
        }
      },
      test_cc_user_ids: {
        IsLen(val) {
          if (val) {
            return val.length > 0;
          } else {
            return false;
          }
        }
      },
      developer: {
        required
      },
      test_content: {
        required
      },
      inner_test_result: {
        required
      },
      trans_interface_change: {
        required
      },
      database_change: {
        required
      },
      regress_test: {
        required
      },
      regress_test_range: {
        isOk(val) {
          if (
            this.createModel.regress_test &&
            this.createModel.regress_test.value === 'yes'
          ) {
            return val ? true : false;
          } else return true;
        },
        maxLen(val) {
          if (val) {
            return val.length <= 500;
          } else return true;
        }
      },
      client_change: {
        required
      },
      client_download: {
        required
      },
      test_environment: {
        required
      },
      deInstruction_files: {
        IsLen(val) {
          if (this.type === '4') return true;
          return val.length > 0;
        }
      },
      req_files: {
        IsLen(val) {
          if (this.type === '4') return true;
          return val.length > 0;
        }
      }
    }
  },
  computed: {
    ...mapState('user', ['currentUser'])
  },
  methods: {
    async rqrmntInputFilter(val, update, abort) {
      update(() => {
        this.demandOptions = this.rqrmntsList.filter(tag => {
          return (
            tag.demand_status_special !== 1 &&
            tag.demand_status_normal !== 9 &&
            (tag.oa_contact_name.toLowerCase().indexOf(val.toLowerCase()) >
              -1 ||
              tag.oa_contact_no.toLowerCase().indexOf(val.toLowerCase()) > -1)
          );
        });
      });
    },
    async getCurUserInfo() {
      let res = await getThreeLevelGroup({ id: this.currentUser.group_id });
      if (res && res.level == 3) {
        this.createModel.group = { name: res.name, id: res.id };
      }
    },
    async implementInputFilter(val, update, abort) {
      update(() => {
        // 如果搜索值为空 直接返回整个列表数据
        if (!val) return (this.implementOptions = this.implement);
        this.implementOptions = this.implement.filter(
          tag => tag.fdev_implement_unit_no.indexOf(val) > -1
        );
      });
    },
    //小组过滤
    leaderGroupFilter(val, update, abort) {
      update(() => {
        this.leaderGroupOptions = this.groups.filter(
          group => group.fullName.indexOf(val) > -1
        );
      });
    },
    demandInput($event) {
      if (!$event) return;
      this.createModel.fdev_implement_unit_no = [];
      this.createModel.impl_unit_num = [];
      this.createModel.impl_unit_num_show = '';
      this.createModel.oa_contact_no = $event.oa_contact_no;
      this.demandObj.id = $event.id;
      this.demandObj.oa_contact_no = $event.oa_contact_no;
      this.demandObj.oa_contact_name = $event.oa_contact_name;
      this.demandObj.demand_type = $event.demand_type;
      this.getIpmpUnit();
    },
    addImpUnit(lists) {
      let tobj = {};
      for (let i = 0; i < lists.length; i++) {
        let key = lists[i].ipmp_implement_unit_no;
        if (key) tobj[key] = 1;
      }
      this.createModel.impl_unit_num = [];
      this.createModel.impl_unit_num_show = '';
      for (let key in tobj) {
        this.createModel.impl_unit_num.push(key);
      }
      this.createModel.impl_unit_num_show = this.createModel.impl_unit_num.toString();
    },
    implementInput($event) {
      if ($event) {
        this.addImpUnit($event);
        let ipmpLists = [];
        for (let i = 0; i < $event.length; i++) {
          let key = $event[i].fdev_implement_unit_no;
          ipmpLists.push(key);
        }
        this.getInnerTestTab(ipmpLists);
      } else {
        this.createModel.impl_unit_num = [];
        this.createModel.impl_unit_num_show = '';
      }
    },
    async testManagerFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.testManagesOptions = this.testManages.filter(
          v =>
            v.user_name_en.indexOf(val) > -1 ||
            v.user_name_cn.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    async testUserFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.testUserOptions = this.users.filter(
          v =>
            v.user_name_en.indexOf(val) > -1 ||
            v.user_name_cn.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    async dailyUserFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.dailyUserOptions = this.users.filter(
          v =>
            v.user_name_en.indexOf(val) > -1 ||
            v.user_name_cn.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    submitForm() {
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
          this.submitOrders();
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
    },
    arToStr(datas, separator) {
      let str = '';
      for (let i = 0; i < datas.length; i++) {
        str += `${datas[i]}${separator}`;
      }
      if (str) {
        str = str.slice(0, str.length - 1);
      }
      return str;
    },
    // 新增or编辑
    async submitOrders() {
      let sendDate = {};
      if (this.type === '4') {
        sendDate = { ...this.detailObj };
      }
      //小组
      if (this.createModel.group) {
        sendDate.group = this.createModel.group.id;
      }
      //对应需求
      sendDate.demand_id = this.demandObj.id;
      sendDate.demand_type = this.demandObj.demand_type || '';
      //IPMP实施单元
      if (this.createModel.impl_unit_num.length > 0) {
        sendDate.impl_unit_num = this.arToStr(
          this.createModel.impl_unit_num,
          ';'
        );
      } else {
        sendDate.impl_unit_num = '';
      }
      //研发单元编号
      let temList = this.createModel.fdev_implement_unit_no;
      if (temList && temList.length > 0) {
        let ipmpLists = [];
        for (let i = 0; i < temList.length; i++) {
          ipmpLists.push(temList[i].fdev_implement_unit_no);
        }
        sendDate.fdev_implement_unit_no = this.arToStr(ipmpLists, ';');
      }
      //业务人员邮箱
      sendDate.business_email = this.createModel.business_email;
      //测试经理用户信息类
      if (this.createModel.test_manager_info) {
        sendDate.test_manager_info = this.createModel.test_manager_info;
      }
      //提测邮件通知抄送人员id
      if (this.createModel.test_cc_user_ids) {
        let test_cc_user_ids = [];
        for (let i = 0; i < this.createModel.test_cc_user_ids.length; i++) {
          test_cc_user_ids.push(this.createModel.test_cc_user_ids[i].id);
        }
        sendDate.test_cc_user_ids = test_cc_user_ids;
      }
      //开发人员
      sendDate.developer = this.createModel.developer;
      //测试内容
      sendDate.test_content = this.createModel.test_content;
      //单元测试情况
      sendDate.unit_test_result = this.createModel.unit_test_result;
      //内测通过情况
      if (this.createModel.inner_test_result) {
        sendDate.inner_test_result = this.createModel.inner_test_result.innerTestTab;
        //工单编号
        const tab = this.createModel.inner_test_result.innerTestTab;
        if (tab === '部分内测通过' || tab === '内测通过') {
          sendDate.workNo = this.workNoObj[tab] || '';
        }
      }
      //是否涉及交易接口改动
      if (this.createModel.trans_interface_change) {
        sendDate.trans_interface_change = this.createModel.trans_interface_change.value;
      }
      //是否涉及数据库改动
      if (this.createModel.database_change) {
        sendDate.database_change = this.createModel.database_change.value;
      }
      //是否涉及回归测试
      if (this.createModel.regress_test) {
        sendDate.regress_test = this.createModel.regress_test.value;
      } //具体回归测试范围
      sendDate.regress_test_range = this.createModel.regress_test_range;
      //涉及关联系统同步改造
      sendDate.system = this.createModel.system;
      //是否涉及客户端更新
      if (this.createModel.client_change) {
        sendDate.client_change = this.createModel.client_change.value;
      }
      //客户端下载地址
      sendDate.client_download = this.createModel.client_download;
      //测试环境
      sendDate.test_environment = this.createModel.test_environment;
      //测试日报抄送人员id
      if (
        this.createModel.daily_cc_user_ids &&
        this.createModel.daily_cc_user_ids.length > 0
      ) {
        let daily_cc_user_ids = [];
        for (let i = 0; i < this.createModel.daily_cc_user_ids.length; i++) {
          daily_cc_user_ids.push(this.createModel.daily_cc_user_ids[i].id);
        }
        sendDate.daily_cc_user_ids = daily_cc_user_ids;
      }
      //备注
      sendDate.remark = this.createModel.remark;
      try {
        this.loading = true;
        if (this.type === '4') {
          //编辑提测单
          sendDate.id = this.submitId;
          sendDate.oa_contact_name = this.demandObj.oa_contact_name;
          sendDate.oa_contact_no = this.demandObj.oa_contact_no;
          await resolveResponseError(() => updateTestOrder(sendDate));
          successNotify('修改成功！');
        } else {
          let formData = new FormData();
          formData.append('testOrder', JSON.stringify(sendDate));
          //需求说明书
          this.createModel.deInstruction_files.forEach(file => {
            formData.append('deInstruction_files', file, file.name);
          });
          //需求规格说明书
          this.createModel.req_files.forEach(file => {
            formData.append('req_files', file, file.name);
          });
          //其他相关材料
          if (this.createModel.other_files.length > 0) {
            this.createModel.other_files.forEach(file => {
              formData.append('other_files', file, file.name);
            });
          }
          await resolveResponseError(() => addTestOrder(formData));
          this.createModel = deepClone(createSubmitTestModel);
          if (this.type === '1') {
            this.createModel.demand_id = {
              oa_contact_name: this.demandObj.oa_contact_name,
              oa_contact_no: this.demandObj.oa_contact_no
            };
            this.createModel.oa_contact_no = this.demandObj.oa_contact_no;
            this.getIpmpUnit();
          }
          successNotify('新增成功！');
        }
        this.loading = false;
        this.$router.go(-1);
      } catch (e) {
        this.loading = false;
      }
    },
    confirmToCancle() {
      this.$router.go(-1);
    },
    async getUsers() {
      let params = {
        status: queryUserOptionsParams.status
      };
      const res = await queryUserCoreData(params);
      this.users = res.map(user => formatOption(user, 'user_name_cn'));
    },
    async getGroups() {
      let res = await queryGroup({ level: 3, status: '1' });
      if (res) {
        this.groups = res;
        this.leaderGroupOptions = this.groups.filter(item => {
          return item;
        });
      }
      await this.getCurUserInfo();
    },
    async getInnerTestTab(fdev_implement_unit_no) {
      let strNo = this.arToStr(fdev_implement_unit_no, ',');
      let res = await queryInnerTestTab({
        fdevUnitNos: strNo
      });
      if (res) {
        this.testResOptions = res.filter(item => {
          if (item.isOption) {
            item.disable = true;
          }
          this.workNoObj[item.innerTestTab] = item.workNo;
          return item;
        });
      }
    },
    async getSubmitDetail(id) {
      const res = await queryTestOrderDetail({ id });
      if (res) {
        this.detailObj = res;
        //更新createModel字段
        if (this.type === '4') {
          //只有编辑才可以赋值
          this.createModel.group = { id: res.group, name: res.group_cn };
          this.createModel.demand_id = {
            oa_contact_name: res.oa_contact_name,
            oa_contact_no: res.oa_contact_no
          };
          this.demandObj.id = res.demand_id;
          this.demandObj.demand_type = res.demand_type;
          this.demandObj.oa_contact_name = res.oa_contact_name;
          this.demandObj.oa_contact_no = res.oa_contact_no;
          await this.getIpmpUnit();
          this.createModel.oa_contact_no = res.oa_contact_no;
          this.createModel.impl_unit_num = res.impl_unit_num.split(',');
          this.createModel.impl_unit_num_show = res.impl_unit_num;
          this.createModel.fdev_implement_unit_no = [];
          let temL = res.fdev_implement_unit_no.split(';');
          for (let i = 0; i < temL.length; i++) {
            this.createModel.fdev_implement_unit_no.push({
              fdev_implement_unit_no: temL[i]
            });
          }
          await this.getInnerTestTab(temL);
          this.createModel.test_content = res.test_content;
          this.createModel.inner_test_result = {
            innerTestTab: res.inner_test_result,
            isOption: ''
          };
          let isHas = false;
          for (let i = 0; i < this.testResOptions.length; i++) {
            if (this.testResOptions[i].innerTestTab === res.inner_test_result) {
              isHas = true;
              break;
            }
          }
          if (!isHas) {
            this.testResOptions.push({
              innerTestTab: res.inner_test_result,
              isOption: ''
            });
          }

          this.createModel.trans_interface_change = {
            label: this.getOptionName(res.trans_interface_change),
            value: res.trans_interface_change
          };
          this.createModel.database_change = {
            label: this.getOptionName(res.database_change),
            value: res.database_change
          };
          this.createModel.regress_test = {
            label: this.getOptionName(res.regress_test),
            value: res.regress_test
          };
          this.createModel.regress_test_range = res.regress_test_range;
          this.createModel.remark = res.remark;
        }
        this.createModel.business_email = res.business_email; //可复制
        this.createModel.developer = res.developer; //可复制
        this.createModel.test_manager_info = res.test_manager_info || []; //可复制
        this.createModel.test_cc_user_ids = res.test_cc_user_info || []; //可复制
        this.createModel.unit_test_result = res.unit_test_result; //可复制
        this.createModel.client_change = {
          label: this.getOptionName(res.client_change),
          value: res.client_change
        }; //可复制
        this.createModel.system = res.system; //可复制
        this.createModel.client_download = res.client_download; //可复制
        this.createModel.test_environment = res.test_environment; //可复制
        this.createModel.daily_cc_user_ids = res.daily_cc_user_info || []; //可复制
      }
    },
    getOptionName(val) {
      if (val === 'yes') {
        return '是';
      } else if (val === 'no') {
        return '否';
      } else return '';
    },
    fdevIpmpTip(item) {
      if (item.testOrderFlag) {
        return item.testOrderFlag;
      } else return '';
    },
    isFdevIpmpShow(item) {
      //提测单新增选择时话术 为空可选 不为空展示话术
      if (item.testOrderFlag) {
        return true;
      } else return false;
    },
    testTip(item) {
      if (item.isOption) {
        return item.isOption;
      } else return '';
    },
    isTestShow(item) {
      //提测单新增选择时话术 为空可选 不为空展示话术
      if (item.isOption) {
        return true;
      } else return false;
    },
    //查看研发单元详情
    async getIpmpUnit() {
      const res = await queryFdevUnitListByDemandId({
        demandId: this.demandObj.id
      });
      if (res) {
        res.forEach(item => {
          if (item.testOrderFlag) {
            item.disable = true;
          }
        });
        this.implement = formatOption(res);
        this.implementOptions = formatOption(res);
      }
    },
    async getParams() {
      this.type = this.$route.params.type || '2';
      if (this.type === '1') {
        //研发单元入口进入新增
        this.demandObj.id = this.$route.query.id;
        this.demandObj.oa_contact_no = this.$route.query.oa_contact_no;
        this.demandObj.oa_contact_name = this.$route.query.oa_contact_name;
        this.demandObj.demand_type = this.$route.query.demand_type;
        this.createModel.demand_id = this.$route.query.oa_contact_name;
        this.createModel.oa_contact_no = this.$route.query.oa_contact_no;
        await this.getIpmpUnit();
      } else if (this.type === '3') {
        //为复制进入新增
        this.submitId = this.$route.query.id;
      } else if (this.type === '4') {
        //提交测试编辑入口
        this.submitId = this.$route.params.id;
      }
    },
    async getTestManager() {
      const res = await getTestManagerInfo();
      if (res) {
        this.testManages = res;
        this.testManagesOptions = this.testManages.filter(item => {
          return item;
        });
      }
    },
    async getRqrmnts() {
      // 需求列表
      const res = await queryTechBusinessForSelect();
      //撤销和暂缓的都去除掉
      if (res) {
        this.rqrmntsList = res;
        this.demandOptions = this.rqrmntsList.filter(item => {
          return (
            item.demand_status_special !== 1 && item.demand_status_normal !== 9
          );
        });
      }
    },
    async init() {
      this.createModel = deepClone(createSubmitTestModel);
      this.loading = true;
      const func1 = new Promise(async resolve => {
        await this.getGroups();
        resolve();
      });
      const func2 = new Promise(async resolve => {
        await this.getTestManager();
        resolve();
      });
      const func3 = new Promise(async resolve => {
        await this.getRqrmnts();
        resolve();
      });
      const func4 = new Promise(async resolve => {
        await this.getUsers();
        resolve();
      });
      await this.getParams();
      return Promise.all([func1, func2, func3, func4])
        .then(async res => {
          if (this.type === '3' || this.type === '4') {
            await this.getSubmitDetail(this.submitId);
          }
          this.loading = false;
        })
        .catch(err => {
          this.loading = false;
        });
    },
    openFiles(type) {
      const input = document.createElement('input');
      input.setAttribute('type', 'file');
      //input.setAttribute('accept', '*.vue,*.js');
      input.setAttribute('multiple', 'multiple');
      input.onchange = file => this.uploadFile(input, type);
      input.click();
    },
    uploadFile({ files }, type) {
      files = Array.from(files);
      if (type === '1') {
        const modelFiles = [...this.createModel.deInstruction_files];
        files.forEach(file => {
          const notExist = this.createModel.deInstruction_files.every(
            item => item.name !== file.name
          );
          let fileName = file.name;
          let fileSize = file.size;
          if (fileSize > 0 && fileSize <= 20 * 1024 * 1024) {
            if (notExist) {
              modelFiles.push(file);
            }
          } else {
            errorNotify(`上传的${fileName}文件大小不能大于20MB,请重新上传`);
          }
        });
        this.createModel.deInstruction_files = modelFiles;
      } else if (type === '2') {
        const modelFiles = [...this.createModel.req_files];
        files.forEach(file => {
          const notExist = this.createModel.req_files.every(
            item => item.name !== file.name
          );
          let fileName = file.name;
          let fileSize = file.size;
          if (fileSize > 0 && fileSize <= 20 * 1024 * 1024) {
            if (notExist) {
              modelFiles.push(file);
            }
          } else {
            errorNotify(`上传的${fileName}文件大小不能大于20MB,请重新上传`);
          }
        });
        this.createModel.req_files = modelFiles;
      } else if (type === '3') {
        const modelFiles = [...this.createModel.other_files];
        files.forEach(file => {
          const notExist = this.createModel.other_files.every(
            item => item.name !== file.name
          );
          let fileName = file.name;
          let fileSize = file.size;
          if (fileSize > 0 && fileSize <= 20 * 1024 * 1024) {
            if (notExist) {
              modelFiles.push(file);
            }
          } else {
            errorNotify(`上传的${fileName}文件大小不能大于20MB,请重新上传`);
          }
        });
        this.createModel.other_files = modelFiles;
      }
    },
    deleteReq_Files(file) {
      this.createModel.req_files = this.createModel.req_files.filter(
        item => item.name !== file.name
      );
    },
    deleteInstFiles(file) {
      this.createModel.deInstruction_files = this.createModel.deInstruction_files.filter(
        item => item.name !== file.name
      );
    },
    deleteOther_Files(file) {
      this.createModel.other_files = this.createModel.other_files.filter(
        item => item.name !== file.name
      );
    },
    verifyModel(nameKey) {
      try {
        let unitModuleKeys = Object.keys(this.$refs).filter(key => {
          return this.$refs[key] && key.indexOf(nameKey) > -1;
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
    }
  },
  created() {
    this.init();
  },
  mounted() {}
};
</script>

<style lang="stylus" scoped>
.orderPage {
  padding-top: 10px;
  padding-bottom: 121px;
}
.titleLine{
  margin-bottom: 30px;
  .titleimg{
    width: 16px;
    height: 16px;
    color: #0663BE;
    margin-bottom: 0px !important;
  }
  .titlename{
    margin-left: 12px;
    font-family: PingFangSC-Semibold;
    font-size: 16px;
    color: #333333;
    letter-spacing: 0;
    line-height: 16px;
    font-weight: 600;
  }
}
.ml-28 {
  margin-left: 28px;
}

.mr-102 {
  margin-right: 102px;
}

.mr-88 {
  margin-right: 88px;
}

.mt-16 {
  margin-top: 16px;
}

.mb-16 {
  margin-bottom: 16px;
}

.op-btn {
  margin-top: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.taskBox {
  .itemleft {
    width: 40px;
  }

  .itemright {
    padding: 11px 16px 11px 16px;
    border: 1px solid #BBBBBB;
    border-radius: 2px;
    border-radius: 2px;
    width: 760px;
    height: 36px;
    font-family: PingFangSC-Regular;
    font-size: 14px;
    color: #333333;
    letter-spacing: 0;
    line-height: 14px;
  }
}

>>>.hideInput .q-field__control.relative-position.row.no-wrap.text-negative,
>>>.hideInput input,
>>>.hideInput .q-field__control,
>>>.hideInput .q-field__control:before,
>>>.hideInput .q-field__control:after {
  display: none;
  content: none;
  height: 0px;
}
</style>
