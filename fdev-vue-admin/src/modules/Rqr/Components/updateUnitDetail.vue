<template>
  <f-dialog
    :value="showFlag"
    @input="$emit('input', $event)"
    @show="initQuery()"
    @before-close="confirmToClose"
    right
  >
    <template v-slot:titleSlot>
      <div
        class="tipStyle1 bg-blue-0 text-subtitle1 q-px-llg row justify-between q-py-md items-center"
      >
        <div
          class="ellipsis-2-lines"
          :title="assessmentModel.implContent"
          style="flex: 1;font-weight: 600"
        >
          {{ assessmentModel.implContent }}
        </div>
        <f-icon name="close" class="cursor-pointer" @click="confirmToClose" />
      </div>
    </template>
    <Loading class="orderPage" :visible="loading">
      <div v-if="isStockDemand" class="tipStyle">
        <span class="pre-style1">
          该实施单元由IPMP维护；仅支持修改<span class="red"
            >牵头小组、计划提交内测日期</span
          >，暂不支持修改预期工作量、实施牵头人、项目编号、实际日期等IPMP相关信息。如需修改请前往IPMP平台！
        </span>
      </div>
      <div v-else class="tipStyle">
        <span class="pre-style1 red">
          实施牵头人、实施牵头团队、项目编号、预期行内人员工作量、预期公司人员工作量
        </span>
        <span class="pre-style1">
          1.以上数据仅支持向IPMP更新一次数据，如需再改，请前往IPMP平台更改。
        </span>
        <span class="pre-style1">
          2.若未勾选“是否向IPMP同步数据”选择框，则以上数据不会同步到IPMP且不会更新IPMP里程碑信息。
        </span>
      </div>
      <div class="rdia-dc-w row justify-between q-mt-lg">
        <f-formitem label="牵头小组">
          <fdev-select
            use-input
            ref="assessmentModel.leaderGroup"
            v-model="assessmentModel.leaderGroup"
            :disable="
              assessmentModel.syncFlag == '1' &&
                assessmentModel.implStatusName === '已投产'
            "
            :options="relatedFilterGroup"
            @filter="relatedGroupFilter"
            :rules="[
              val => $v.assessmentModel.leaderGroup.required || '请选择牵头小组'
            ]"
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
        <!-- <f-formitem label="测试牵头人">
          <fdev-select
            multiple
            use-input
            hint=""
            :disable="isStockDemand"
            ref="assessmentModel.testLeaderName"
            v-model="assessmentModel.testLeaderName"
            :options="testFilterLeader"
            @filter="testLeaderFilter"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">{{
                    scope.opt.user_name_cn
                  }}</fdev-item-label>
                  <fdev-item-label :title="scope.opt.user_name_en" caption>
                    {{ scope.opt.user_name_en }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem> -->
        <f-formitem label="行内人员预期工作量">
          <fdev-input
            v-model="assessmentModel.expectOwnWorkload"
            :disable="assessmentModel.syncFlag === '1' || isStockDemand"
            type="text"
            ref="assessmentModel.expectOwnWorkload"
            maxlength="6"
            suffix="人/月"
            :rules="[
              val =>
                assessmentModel.syncFlag !== '1' &&
                (toIPMP || data.expectOwnWorkload) &&
                !isStockDemand
                  ? !!val || '行内人员预期工作量不能为空'
                  : true,
              () =>
                $v.assessmentModel.expectOwnWorkload.decimal || '只能输入数字',
              () =>
                $v.assessmentModel.expectOwnWorkload.regValue ||
                '请输入大于0的数字且最多两位小数',
              () =>
                $v.assessmentModel.expectOwnWorkload.maxL ||
                '非法数字且输入最大长度为6',

              () =>
                $v.assessmentModel.expectOwnWorkload.minNum ||
                `输入值不能小于传入预期工作量${data.expectOwnWorkload}`
            ]"
          >
          </fdev-input>
        </f-formitem>
        <f-formitem label="实施牵头人">
          <fdev-select
            multiple
            use-input
            option-value="id"
            ref="assessmentModel.implLeaderInfo"
            v-model="assessmentModel.implLeaderInfo"
            :options="userFilterOptions"
            :disable="assessmentModel.syncFlag === '1' || isStockDemand"
            @filter="userFilter"
            @remove="removeRelatedGroup"
            :rules="[
              val =>
                assessmentModel.syncFlag !== '1' && toIPMP && !isStockDemand
                  ? $v.assessmentModel.implLeaderInfo.required ||
                    '请选择实施牵头人'
                  : true
            ]"
          >
            <!-- 涉及板块中如有评估中、评估完成状态，则不能去掉 -->
            <template v-slot:selected-item="scope">
              <fdev-chip
                dense
                class="q-chip1 text-blue-9 q-chip--square bg-blue-grey-0 outline"
                :removable="!!scope.opt.id"
                @remove="scope.removeAtIndex(scope.index)"
                :tabindex="scope.tabindex"
              >
                {{ scope.opt.user_name_cn }}
              </fdev-chip>
            </template>
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label
                    :title="
                      scope.opt.user_name_en + '--' + scope.opt.group.name
                    "
                    caption
                  >
                    {{ scope.opt.user_name_en }}--{{ scope.opt.group.name }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem label="公司人员预期工作量">
          <fdev-input
            v-model="assessmentModel.expectOutWorkload"
            :disable="assessmentModel.syncFlag === '1' || isStockDemand"
            type="text"
            ref="assessmentModel.expectOutWorkload"
            maxlength="6"
            suffix="人/月"
            :rules="[
              val =>
                assessmentModel.syncFlag !== '1' &&
                (toIPMP || data.expectOutWorkload) &&
                !isStockDemand
                  ? $v.assessmentModel.expectOutWorkload.required ||
                    '公司人员预期工作量不能为空'
                  : true,
              () =>
                $v.assessmentModel.expectOutWorkload.decimal || '只能输入数字',
              () =>
                $v.assessmentModel.expectOutWorkload.regValue ||
                '请输入大于等于0的数字且最多两位小数',
              () =>
                $v.assessmentModel.expectOutWorkload.maxL ||
                '非法数字且输入最大长度为6',

              () =>
                $v.assessmentModel.expectOutWorkload.minNum ||
                `输入值不能小于传入预期工作量${data.expectOutWorkload}`
            ]"
          >
          </fdev-input>
        </f-formitem>
        <f-formitem label="实施牵头团队">
          <fdev-select
            use-input
            :disable="assessmentModel.syncFlag === '1' || isStockDemand"
            ref="assessmentModel.headerTeamName"
            v-model="assessmentModel.headerTeamName"
            :options="ipmpFilterTeam"
            @filter="ipmpTeamFilter"
            :rules="[
              val =>
                assessmentModel.syncFlag !== '1' && toIPMP && !isStockDemand
                  ? $v.assessmentModel.headerTeamName.required ||
                    '请选择实施牵头团队'
                  : true
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.team_name">{{
                    scope.opt.team_name
                  }}</fdev-item-label>
                  <fdev-item-label :title="scope.opt.team_id" caption>
                    {{ scope.opt.team_id }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem label="项目编号">
          <fdev-select
            use-input
            :disable="assessmentModel.syncFlag === '1' || isStockDemand"
            ref="assessmentModel.prjNum"
            v-model="assessmentModel.prjNum"
            :options="ipmpFilterProject"
            @filter="ipmpProjectFilter"
            :rules="[
              val => $v.assessmentModel.prjNum.required || '请选择项目编号'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.project_no">{{
                    scope.opt.project_no
                  }}</fdev-item-label>
                  <fdev-item-label :title="scope.opt.project_name" caption>
                    {{ scope.opt.project_name }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
          <div class="msgStyle" v-show="assessmentModel.prjNum && !showUnitNo">
            请选择所属系统下的项目编号
          </div>
        </f-formitem>
        <f-formitem label="计划启动开发日期">
          <f-date
            v-model="assessmentModel.planDevelopDate"
            mask="YYYY-MM-DD"
            hint=""
            disable
          />
        </f-formitem>
        <f-formitem label="实际启动开发日期">
          <f-date
            v-model="assessmentModel.acturalDevelopDate"
            mask="YYYY-MM-DD"
            disable
            hint=""
          />
        </f-formitem>
        <f-formitem label="计划提交内测日期">
          <f-date
            v-model="assessmentModel.planInnerTestDate"
            :default-year-month="defaultYearMonth"
            ref="assessmentModel.planInnerTestDate"
            :disable="
              assessmentModel.syncFlag == '1' &&
                assessmentModel.implStatusName === '已投产'
            "
            :options="optionsFn"
            :rules="[val => !!val || '计划提交内测日期不能为空']"
          />
        </f-formitem>
        <f-formitem label="实际提交内测日期">
          <f-date
            v-model="assessmentModel.actualInnerTestDate"
            mask="YYYY-MM-DD"
            hint=""
            disable
          />
        </f-formitem>
        <f-formitem label="计划提交用户测试日期">
          <f-date
            v-model="assessmentModel.planTestStartDate"
            mask="YYYY-MM-DD"
            hint=""
            disable
          />
        </f-formitem>
        <f-formitem label="实际提交用户测试日期">
          <f-date
            v-model="assessmentModel.acturalTestStartDate"
            mask="YYYY-MM-DD"
            disable
            hint=""
          />
        </f-formitem>
        <f-formitem label="计划用户测试完成日期">
          <f-date
            v-model="assessmentModel.planTestFinishDate"
            mask="YYYY-MM-DD"
            disable
            hint=""
          />
        </f-formitem>
        <f-formitem label="实际用户测试完成日期">
          <f-date
            v-model="assessmentModel.acturalTestFinishDate"
            mask="YYYY-MM-DD"
            disable
            hint=""
          />
        </f-formitem>
        <f-formitem label="计划投产日期">
          <f-date
            disable
            v-model="assessmentModel.planProductDate"
            mask="YYYY-MM-DD"
            hint=""
          />
        </f-formitem>
        <f-formitem label="实际投产日期">
          <f-date
            disable
            v-model="assessmentModel.acturalProductDate"
            mask="YYYY-MM-DD"
            hint=""
          />
        </f-formitem>
        <!-- 显示 标志位关联的是:任务集  -->
        <f-formitem
          help="实施单元向ipmp同步过且实施单元牵头人才可编辑"
          label="是否上云"
          v-if="
            assessmentModel.prjNum && assessmentModel.prjNum.project_type == '2'
          "
        >
          <!-- 置灰 未向ipmp同步过 || 不是实施单元牵头人  
          || 回显值是: 是  || 回显值是: 否 || || 回显值是: 审核中  -->
          <fdev-select
            @input="showTip($event)"
            ref="ipmpUnitUpToCloud.cloudFlag"
            :disable="
              assessmentModel.syncFlag != '1' ||
                !isIpmpLeader ||
                data.cloudFlag == 'implunit.cloud.flag.01' ||
                data.cloudFlag == 'implunit.cloud.flag.02' ||
                data.cloudFlag == 'implunit.cloud.flag.04'
            "
            options-value="value"
            v-model="ipmpUnitUpToCloud.cloudFlag"
            :rules="[val => isCloudFlagRequired(val) || '请选择是否上云']"
            :options="
              assessmentModel.syncFlag == '1' &&
              assessmentModel.implStatusName === '已投产'
                ? cloudOption1
                : cloudOption
            "
            clearable
          />
        </f-formitem>
        <!-- 显示  标志位的值是:( 是 或者 审核中) -->
        <f-formitem
          label="技术方案编号"
          v-if="
            (ipmpUnitUpToCloud.cloudFlag &&
              ipmpUnitUpToCloud.cloudFlag.value == 'implunit.cloud.flag.01') ||
              (ipmpUnitUpToCloud.cloudFlag &&
                ipmpUnitUpToCloud.cloudFlag.value == 'implunit.cloud.flag.04')
          "
        >
          <!-- 置灰  标志位回显值是:审核中 || 标志位回显值是:是 || 未向ipmp同步过 || 不是实施单元牵头人 -->
          <fdev-select
            use-input
            :disable="
              data.cloudFlag == 'implunit.cloud.flag.04' ||
                data.cloudFlag == 'implunit.cloud.flag.01' ||
                assessmentModel.syncFlag != '1' ||
                !isIpmpLeader
            "
            :options="techSchemeNoOption"
            ref="ipmpUnitUpToCloud.techSchemeNo"
            v-model="ipmpUnitUpToCloud.techSchemeNo"
            option-value="code"
            option-label="codename"
            @filter="filterTechSchemeNoOption"
            :rules="[
              val =>
                isTechSchemeNoOrCheckerUserNamesRequired(val, 'techSchemeNo') ||
                '请选择技术方案编号'
            ]"
            clearable
          >
          </fdev-select>
        </f-formitem>
        <f-formitem
          label="审核人"
          v-if="
            (ipmpUnitUpToCloud.cloudFlag &&
              ipmpUnitUpToCloud.cloudFlag.value == 'implunit.cloud.flag.01') ||
              (ipmpUnitUpToCloud.cloudFlag &&
                ipmpUnitUpToCloud.cloudFlag.value == 'implunit.cloud.flag.04')
          "
        >
          <fdev-select
            multiple
            use-input
            :disable="
              data.cloudFlag == 'implunit.cloud.flag.04' ||
                data.cloudFlag == 'implunit.cloud.flag.01' ||
                assessmentModel.syncFlag != '1' ||
                !isIpmpLeader
            "
            :options="checkerUserNamesOption"
            ref="ipmpUnitUpToCloud.checkerUserNames"
            v-model="ipmpUnitUpToCloud.checkerUserNames"
            option-value="code"
            option-label="codename"
            @filter="filterCheckerUserNamesOption"
            :rules="[
              val =>
                isTechSchemeNoOrCheckerUserNamesRequired(
                  val,
                  'checkerUserNames'
                ) || '请选择审核人'
            ]"
            clearable
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.codename">{{
                    scope.opt.codename
                  }}</fdev-item-label>
                  <fdev-item-label :title="scope.opt.code" caption>{{
                    scope.opt.code
                  }}</fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem label="实施延期原因分类">
          <fdev-select
            multiple
            use-input
            map-options
            emit-value
            v-model="assessmentModel.implDelayType"
            :options="delayAvailables"
            :disable="!checkDate || isStockDemand"
            ref="assessmentModel.implDelayType"
            :rules="[
              val =>
                !checkDate || isStockDemand
                  ? true
                  : $v.assessmentModel.implDelayType.required ||
                    '实施延期原因分类不能为空'
            ]"
          />
        </f-formitem>
        <f-formitem full-width label="实施延期原因">
          <fdev-input
            :disable="!checkDate || isStockDemand"
            v-model="assessmentModel.implDelayReason"
            type="textarea"
            ref="assessmentModel.implDelayReason"
            :rules="[
              val =>
                !checkDate || isStockDemand
                  ? true
                  : !!val || '实施延期原因不能为空'
            ]"
          />
        </f-formitem>
      </div>
    </Loading>
    <template v-slot:btnSlot>
      <div
        class="q-gutter-sm"
        v-if="assessmentModel.syncFlag !== '1' && !isStockDemand"
      >
        <fdev-checkbox v-model="toIPMP" label="是否向IPMP同步数据?" />
      </div>
      <!-- <div>
          <f-icon name="help_c_o" class="text-primary"> </f-icon>
          <fdev-tooltip
            anchor="top middle"
            self="center middle"
            :offest="[-20, 0]"
          >
            <div class="q-mt-sm q-mb-sm">
              <span class="pre-style"
                >以下数据仅支持向IPMP更新一次数据，如需再改，请前往IPMP平台更改。</span
              >
              <span class="pre-style">
                实施牵头人、实施牵头团队、测试牵头人、项目编号、预期行内人员工作量、预期公司人员工作量
              </span>
            </div>
          </fdev-tooltip>
        </div> -->
      <fdev-btn dialog label="取消" @click="confirmToClose" />
      <fdev-btn dialog label="确认" :loading="updateFlag" @click="sure" />
    </template>
    <f-dialog v-model="tipFlag" title="请注意">
      <div class="red flex" style="font-size:16px">
        <f-icon name="alert_t_f" style="color:red;margin-top: 6px" />
        <div style="padding-left:20px">
          请确认所有小组是否评估完成
          <div>
            向IPMP更新后，如需再改，需处长审批后在IPMP平台更改！
          </div>
        </div>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="tipFlag = false"/>
        <fdev-btn label="确定" dialog @click="onSubmit"
      /></template>
    </f-dialog>
    <f-dialog v-model="unitCheck" title="实施单元核算">
      <div>
        请前往<a href="#" @click="goIpmp">IPMP</a>发起【{{
          checkImplUnitNum
        }}】核算
      </div>
    </f-dialog>
  </f-dialog>
</template>
<script>
import { mapGetters, mapState, mapActions } from 'vuex';
import Loading from '@/components/Loading';
import {
  delayAvailables,
  queryUserOptionsParams,
  delayTypeSpecial
} from '../model';
import moment from 'moment';
import { formatUser } from '@/modules/User/utils/model';
import {
  formatOptionAdd,
  formatOption,
  successNotify,
  validate,
  deepClone
} from '@/utils/utils';
import { required, decimal } from 'vuelidate/lib/validators';
import {
  getSchemeReview,
  getCloudCheckers,
  getImplUnitRelatSpFlag
} from '@/modules/Rqr/services/methods.js';

export default {
  name: 'UpdateUnitDetail',
  props: {
    showFlag: {
      type: Boolean,
      default: false
    },
    data: {
      type: Object,
      default: () => {}
    }
  },
  created() {
    window.this_ = this;
  },
  components: { Loading },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapGetters('user', ['isLoginUserList', 'isDemandManager']),
    ...mapState('userForm', {
      groupsData: 'groups',
      userInPage: 'userInPage'
    }),
    ...mapState('demandsForm', [
      'oaNOStatus',
      'modelExcel',
      'ipmpUser',
      'ipmpProject',
      'ipmpLeadTeam',
      'updateIpmpUnits'
    ]),
    // true 是实施单元牵头人
    isIpmpLeader() {
      return (
        this.data.implLeaderInfo &&
        this.data.implLeaderInfo
          .map(item => item.user_name_en)
          .includes(this.currentUser.eName)
      );
    },
    defaultYearMonth() {
      let {
        planDevelopDate,
        planTestStartDate,
        planTestFinishDate,
        planProductDate
      } = this.data;
      let time =
        planDevelopDate ||
        planTestStartDate ||
        planTestFinishDate ||
        planProductDate;
      if (time) {
        let arr = time.split('-');
        return arr[0] + '/' + arr[1];
      } else {
        return '';
      }
    },
    //是否是存量需求
    isStockDemand() {
      return this.data.usedSysCode === 'stockUnit';
    }
  },
  watch: {
    //计划提交内测时间不能为空
    // 'assessmentModel.planInnerTestDate': {
    //   deep: true,
    //   handler(val) {
    //     // if (!val) {
    //     //   this.assessmentModel.planInnerTestDate = this.data.planInnerTestDate;
    //     // }
    //   }
    // },
    'assessmentModel.implLeaderInfo': {
      handler(newVal, oldVal) {
        let len = oldVal && oldVal.length;
        let len1 = newVal && newVal.length;
        if (len1 < len) {
          if (!oldVal[this.deleteIndex].id) {
            this.assessmentModel.implLeaderInfo = oldVal;
          }
        }
      }
    },
    'assessmentModel.prjNum': {
      async handler(newVal, oldVal) {
        this.showUnitNo = true;
        //无法编辑项目编号时无须发检验交易
        if (
          newVal &&
          newVal.value &&
          !(this.assessmentModel.syncFlag === '1' || this.isStockDemand)
        ) {
          let params = {
            implUnitNum: this.data.implUnitNum,
            prjNum: newVal.value
          };
          this.showUnitNo = await getImplUnitRelatSpFlag(params);
        }
      }
    }
  },
  data() {
    return {
      ipmpUnitUpToCloud: {
        //是否上云
        cloudFlag: null,
        //技术编号
        techSchemeNo: null,
        //审核人
        checkerUserNames: null
      },
      // 是否上云
      cloudOption: [
        { value: 'implunit.cloud.flag.01', label: '是' },
        { value: 'implunit.cloud.flag.02', label: '否' },
        { value: 'implunit.cloud.flag.03', label: '待定' },
        { value: 'implunit.cloud.flag.04', label: '审核中', disable: true }
      ],
      //已投产 已同步IPMP的实施单元可以编辑上云标识  但只能选择否
      cloudOption1: [{ value: 'implunit.cloud.flag.02', label: '否' }],
      // 技术方案编号
      techSchemeNoOption: [],
      techSchemeNoOptionClone: [],
      // 审核人
      checkerUserNamesOption: [],
      checkerUserNamesOptionClone: [],
      delayAvailables,
      assessmentModel: {},
      testLeaderClone: [], //ipmp人员信息
      testFilterLeader: [],
      testLeader: [],
      groupsClone: [], //小组
      groups: [],
      relatedFilterGroup: [],
      userFilterOptions: [],
      userOptions: [],
      ipmpProjectOption: [],
      ipmpFilterProject: [],
      ipmpProjectClone: [],
      ipmpTeamOption: [],
      ipmpFilterTeam: [],
      ipmpTeamClone: [],
      toIPMP: false, //同步IPMP标识
      deleteIndex: 0, //删除下标
      checkDate: false,
      tipFlag: false,
      unitCheck: false,
      checkImplUnitNum: '',
      checkUrl: '',
      updateFlag: false,
      showUnitNo: true,
      loading: false
    };
  },
  validations: {
    assessmentModel: {
      headerTeamName: {
        required
      },
      implDelayType: {
        required
      },
      implLeaderInfo: {
        required
      },
      prjNum: {
        required
      },
      leaderGroup: {
        required
      },
      planInnerTestDate: {
        required
      },
      expectOwnWorkload: {
        decimal,
        maxL(val) {
          if (val === 0 || val === '' || !val) {
            return true;
          }
          let str = val.toString();
          if (
            str.length > 6 ||
            (str.length >= 2 && str[0] === '0' && str[1] !== '.') ||
            (str.length >= 2 && str[0] === '-' && str[1] === '0')
          ) {
            return false;
          } else {
            return true;
          }
        },
        regValue(val) {
          if (!this.toIPMP && (val === 0 || val === '' || !val)) {
            return true;
          }
          if (val > 0 && /^(-?\d+)(\.\d{1,2})?$/.test(val)) {
            return true;
          } else {
            return false;
          }
        },
        minNum(val) {
          if (val === 0 || val === '' || !val) {
            return true;
          }
          if (val >= this.data.expectOwnWorkload) {
            return true;
          } else {
            return false;
          }
        }
      },
      expectOutWorkload: {
        required,
        decimal,
        maxL(val) {
          if (val === 0 || val === '' || !val) {
            return true;
          }
          let str = val.toString();
          if (
            str.length > 6 ||
            (str.length >= 2 && str[0] === '0' && str[1] !== '.') ||
            (str.length >= 2 && str[0] === '-' && str[1] === '0')
          ) {
            return false;
          } else {
            return true;
          }
        },
        regValue(val) {
          if (val === 0 || val === '' || !val) {
            return true;
          }
          if (val >= 0 && /^(-?\d+)(\.\d{1,2})?$/.test(val)) {
            return true;
          } else {
            return false;
          }
        },
        minNum(val) {
          if (val === 0 || val === '' || !val) {
            return true;
          }
          if (val >= this.data.expectOutWorkload) {
            return true;
          } else {
            return false;
          }
        }
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
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent'
    }),
    ...mapActions('userForm', {
      queryGroup: 'fetchGroup',
      queryUserPagination: 'queryUserPagination'
    }),
    ...mapActions('demandsForm', [
      'queryDemandByOaContactNo',
      'updateImpl',
      'queryIpmpUser',
      'queryIpmpProject',
      'updateIpmpUnit',
      'queryIpmpLeadTeam'
    ]),
    showTip(data) {
      if (
        data.value == 'implunit.cloud.flag.01' ||
        data.value == 'implunit.cloud.flag.02'
      ) {
        this.$q.dialog({
          title: '关闭弹窗',
          message: '上云标志位选择为是或者否后不允许修改，请谨慎选择!',
          persistent: true,
          ok: '我知道了'
        });
      }
      // 上云标志位选择后不允许修改，请谨慎选择!
    },
    //是否上云是否必填
    isCloudFlagRequired(val) {
      // 是否上云必填条件
      // 同步过  是实施单元牵头人 关联的任务集
      // assessmentModel.syncFlag == '1' && isIpmpLeader &&  assessmentModel.prjNum && assessmentModel.prjNum.project_type == '2'
      if (
        this.assessmentModel.syncFlag == '1' &&
        this.isIpmpLeader &&
        this.assessmentModel.prjNum &&
        this.assessmentModel.prjNum.project_type == '2'
      ) {
        // 校验 是否上云是否填写
        return !!(val && val.value);
      } else {
        // (比如 同步过 是实施单元牵头人 但是关联的不是任务集) 就返回false 不校验
        return true;
      }
    },
    // 技术编号或者审核人是否必填(必须有值)
    isTechSchemeNoOrCheckerUserNamesRequired(val, type) {
      // 技术编号 必填 条件
      // (标志位显示值是 :'是'  || 标志位显示值是 :'审核中' ) && 同步过  && 是实施单元牵头人 && 关联了任务集
      if (
        (this.ipmpUnitUpToCloud.cloudFlag.value == 'implunit.cloud.flag.01' ||
          this.ipmpUnitUpToCloud.cloudFlag.value == 'implunit.cloud.flag.04') &&
        this.assessmentModel.syncFlag == '1' &&
        this.isIpmpLeader &&
        this.assessmentModel.prjNum &&
        this.assessmentModel.prjNum.project_type == '2'
      ) {
        //必填校验
        if (type == 'techSchemeNo') {
          // 技术编号单选 检验 code值
          return !!(val && val.code);
        } else {
          // 审核人多选 检验长度
          return val && val.length;
        }
      } else {
        //非必填 不检验
        return true;
      }
    },
    validateForm() {
      this.$v.assessmentModel.$touch();
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
          if (this.$refs[key].$refs.date) return this.$refs[key].$children[0];
          return this.$refs[key];
        })
      );
      if (this.$v.assessmentModel.$invalid) {
        return false;
      }
      return true;
    },
    singleValidate(arr) {
      arr.forEach(itm => {
        this.$refs[itm].validate();
      });
      return arr.every(item => {
        return this.$refs[item].validate();
      });
    },
    confirmToClose() {
      // this.assessmentModel = JSON.parse(JSON.stringify(this.data));
      this.toIPMP = false;
      this.$emit('updateSuccess');
    },
    dateChang(val) {
      if (val) {
        return moment(val).format('YYYYMMDD');
      } else {
        return '';
      }
    },
    getToday() {
      return moment(new Date()).format('YYYYMMDD');
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
    async initQuery() {
      this.assessmentModel = JSON.parse(JSON.stringify(this.data));
      this.loading = true;
      this.checkDateType();
      this.validateForm();
      this.groupList();
      this.useList();
      await this.ipmpUserList();
      this.projectList();
      this.teamList();
      // 是否上云回显
      this.findCloudFlag();
      // 查询技术编号
      this.getSchemeReview();
      //审核人
      this.getCloudCheckers();
      this.assessmentModel.implDelayType = this.assessmentModel.implDelayType
        ? this.assessmentModel.implDelayType.split(',')
        : [];
      // 获取currentUser
      this.fetchCurrent();
      this.loading = false;
    },
    async filterTechSchemeNoOption(val, update, abort) {
      //审核人员过滤
      update(() => {
        this.techSchemeNoOption = this.techSchemeNoOptionClone.filter(
          item =>
            (item.code && item.code.indexOf(val) > -1) ||
            (item.codename && item.codename.indexOf(val) > -1)
        );
      });
    },
    findCloudFlag() {
      this.ipmpUnitUpToCloud.cloudFlag = {
        label: this.data.cloudFlagName,
        value: this.data.cloudFlag
      };
    },
    async getSchemeReview() {
      let res = await getSchemeReview();
      this.techSchemeNoOption = res;
      this.techSchemeNoOptionClone = deepClone(res);
      // 技术方案回显示
      // techSchemeKey: "schemeReview_a4d5032be8ce472c"
      // techSchemeNo: "2021-1-018"
      this.ipmpUnitUpToCloud.techSchemeNo = {
        code: this.data.techSchemeKey,
        codename: this.data.techSchemeNo
      };
    },
    async filterCheckerUserNamesOption(val, update, abort) {
      //审核人员过滤
      update(() => {
        this.checkerUserNamesOption = this.checkerUserNamesOptionClone.filter(
          item =>
            (item.code && item.code.indexOf(val) > -1) ||
            (item.codename && item.codename.indexOf(val) > -1) ||
            (item.displayName && item.displayName.indexOf(val) > -1)
        );
      });
    },
    async getCloudCheckers() {
      let res = await getCloudCheckers();
      this.checkerUserNamesOption = res;
      this.checkerUserNamesOptionClone = deepClone(res);
      //审核人回显
      // checkerUserIds: 'chengye2,chengj3';
      // checkerUserNames: '程烨,成洁';
      let code =
        this.data.checkerUserIds && this.data.checkerUserIds.split(',');
      let codename =
        this.data.checkerUserNames && this.data.checkerUserNames.split(',');
      this.ipmpUnitUpToCloud.checkerUserNames =
        (code &&
          code.length &&
          code.map((item, idx) => ({
            code: item,
            codename: codename[idx]
          }))) ||
        null;
    },
    //牵头小组
    async groupList() {
      await this.queryGroup();
      this.groups = formatOption(this.groupsData);
      this.relatedFilterGroup = this.groups;
      this.groupsClone = JSON.parse(JSON.stringify(this.groups));
      let groupList = [];
      this.groups.filter(val => {
        if (val.id === this.assessmentModel.leaderGroup) {
          groupList = val;
        }
      });
      this.assessmentModel.leaderGroup = groupList;
    },
    //实施牵头人列表
    async useList() {
      await this.queryUserPagination(queryUserOptionsParams);
      this.users = this.userInPage.list.map(user =>
        formatOption(formatUser(user), 'name')
      );
      this.userOptions = this.users.slice(0);
      this.userFilterOptions = this.userOptions;
    },
    //测试牵头人列表
    async ipmpUserList() {
      await this.queryIpmpUser();
      this.testLeader = formatOptionAdd(
        this.ipmpUser,
        'user_name_cn',
        'user_name_en'
      );
      this.testFilterLeader = this.testLeader;
      this.testLeaderClone = JSON.parse(JSON.stringify(this.testLeader));
      let leadList = [];
      let testLeaderARR = this.assessmentModel.testLeader
        ? this.assessmentModel.testLeader.split(',')
        : [];
      this.testLeader.map(val => {
        if (testLeaderARR.indexOf(val.user_name_en) > -1) {
          leadList.push(val);
        }
      });
      this.assessmentModel.testLeaderName = leadList;
    },
    //项目编号列表
    async projectList() {
      // project_type	string
      // 必须
      // 项目类型 1-项目 2-任务集
      // 项目集 选择 是否上运否则不选择
      await this.queryIpmpProject();
      this.ipmpProjectOption = formatOptionAdd(
        this.ipmpProject,
        'project_no',
        'project_no'
      );
      this.ipmpFilterProject = this.ipmpProjectOption;
      this.ipmpProjectClone = JSON.parse(
        JSON.stringify(this.ipmpProjectOption)
      );
      let projectList = '';
      this.ipmpProjectOption.filter(val => {
        if (val.project_no === this.assessmentModel.prjNum) {
          projectList = val;
        }
      });
      if (projectList) {
        this.assessmentModel.prjNum = projectList;
      } else if (this.data.prjNum) {
        this.assessmentModel.prjNum = {
          project_no: this.data.prjNum,
          project_name: this.data.planPrjName,
          label: this.data.prjNum,
          value: this.data.prjNum
        };
      }
    },
    //牵头团队
    async teamList() {
      await this.queryIpmpLeadTeam({ dept_name: this.data.headerUnitName });
      this.ipmpTeamOption = formatOptionAdd(
        this.ipmpLeadTeam,
        'team_name',
        'team_name'
      );
      this.ipmpFilterTeam = this.ipmpTeamOption;
      this.ipmpTeamClone = JSON.parse(JSON.stringify(this.ipmpTeamOption));
      let TeamList = [];
      this.ipmpTeamOption.filter(val => {
        if (val.team_name === this.assessmentModel.headerTeamName) {
          TeamList = val;
        }
      });
      this.assessmentModel.headerTeamName = TeamList;
    },
    optionsFn(date) {
      let time =
        this.assessmentModel.planTestStartDate ||
        this.assessmentModel.planTestFinishDate ||
        this.assessmentModel.planProductDate;
      if (time) {
        return (
          this.assessmentModel.planDevelopDate.split('-').join('/') <= date &&
          date <= time.split('-').join('/')
        );
      } else {
        return (
          this.assessmentModel.planDevelopDate.split('-').join('/') <= date
        );
      }
    },
    removeRelatedGroup(date) {
      this.deleteIndex = date.index;
    },
    sure() {
      if (this.isStockDemand) {
        const arr = [
          'assessmentModel.leaderGroup',
          'assessmentModel.planInnerTestDate'
        ];
        if (this.singleValidate(arr)) {
          this.onSubmitDialg();
        } else {
          this.validateForm();
        }
      } else {
        if (!this.toIPMP && !this.checkDate) {
          const arr = [
            'assessmentModel.leaderGroup',
            'assessmentModel.prjNum',
            'assessmentModel.planInnerTestDate',
            'assessmentModel.expectOwnWorkload',
            'assessmentModel.expectOutWorkload'
          ];
          if (this.singleValidate(arr)) {
            this.onSubmitDialg();
          } else {
            this.validateForm();
          }
        } else if (this.toIPMP && !this.checkDate) {
          const arr = [
            'assessmentModel.leaderGroup',
            'assessmentModel.implLeaderInfo',
            'assessmentModel.planInnerTestDate',
            'assessmentModel.headerTeamName',
            'assessmentModel.prjNum',
            'assessmentModel.expectOwnWorkload',
            'assessmentModel.expectOutWorkload'
          ];
          if (this.singleValidate(arr)) {
            this.onSubmitDialg();
          } else {
            this.validateForm();
          }
        } else if (!this.toIPMP && this.checkDate) {
          const arr = [
            'assessmentModel.leaderGroup',
            'assessmentModel.prjNum',
            'assessmentModel.planInnerTestDate',
            'assessmentModel.implDelayType',
            'assessmentModel.implDelayReason',
            'assessmentModel.expectOwnWorkload',
            'assessmentModel.expectOutWorkload'
          ];
          if (this.singleValidate(arr)) {
            this.onSubmitDialg();
          } else {
            this.validateForm();
          }
        } else {
          if (this.validateForm()) {
            this.onSubmitDialg();
          }
        }
      }
    },
    onSubmitDialg() {
      if (this.toIPMP) {
        this.tipFlag = true;
      } else {
        this.onSubmit();
      }
    },
    async onSubmit() {
      //项目编号校验不通过  直接返回
      if (!this.showUnitNo) return;
      // ipmpUnitUpToCloud
      // 上云校验 开始
      const CloudKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('ipmpUnitUpToCloud') > -1;
      });
      const resultArray = CloudKeys.map(
        item => this.$refs[item] && this.$refs[item].validate()
      );
      if (resultArray.includes(false)) return;
      // 上云校验 结束
      this.updateFlag = true;
      this.tipFlag = false;
      const {
        implLeaderInfo,
        headerTeamName,
        testLeaderName,
        prjNum,
        expectOwnWorkload,
        expectOutWorkload,
        implDelayType,
        implDelayReason,
        implUnitNum,
        leaderGroup,
        planInnerTestDate
      } = this.assessmentModel;
      let leader = [];
      let leaderCn = [];
      if (implLeaderInfo) {
        implLeaderInfo.map(item => {
          leader.push(item.user_name_en);
          leaderCn.push(item.user_name_cn);
        });
      }
      let delayName = [];
      if (implDelayType) {
        implDelayType.map(item => {
          delayName.push(delayTypeSpecial[item]);
        });
      }
      let leaderN = [];
      let leaderE = [];
      let leaderM = [];
      if (testLeaderName) {
        testLeaderName.map(item => {
          leaderN.push(item.user_name_cn);
          leaderE.push(item.user_name_en);
          leaderM.push(item.email);
        });
      }
      let cloudParams = {};
      // 项目类型 1-项目 2-任务集
      let type =
        this.assessmentModel.prjNum && this.assessmentModel.prjNum.project_type; //项目编号类型
      // 选择的是 任务集  才会显示 是否上云 技术方案编号 审核人
      if (type == '2') {
        // 是否上云选择是
        if (
          this.ipmpUnitUpToCloud.cloudFlag.value == 'implunit.cloud.flag.01'
        ) {
          // 必填参数
          // cloudFlag
          // cloudFlagName
          // techSchemeKey
          // techSchemeNo
          // checkerUserIds
          // checkerUserNames
          // projectType
          cloudParams.cloudFlag = 'implunit.cloud.flag.01';
          cloudParams.cloudFlagName = '是';
          cloudParams.techSchemeKey = this.ipmpUnitUpToCloud.techSchemeNo.code;
          cloudParams.techSchemeNo = this.ipmpUnitUpToCloud.techSchemeNo.codename;
          cloudParams.checkerUserIds = this.ipmpUnitUpToCloud.checkerUserNames
            .map(item => item.code)
            .join(',');
          cloudParams.checkerUserNames = this.ipmpUnitUpToCloud.checkerUserNames
            .map(item => item.codename)
            .join(',');
          cloudParams.projectType = '2';
        } else {
          // 否 待定
          cloudParams.cloudFlag = this.ipmpUnitUpToCloud.cloudFlag.value;
          cloudParams.cloudFlagName = this.ipmpUnitUpToCloud.cloudFlag.label;
          cloudParams.techSchemeKey = '';
          cloudParams.techSchemeNo = '';
          cloudParams.checkerUserIds = '';
          cloudParams.checkerUserNames = '';
          cloudParams.projectType = '2';
        }
      } else {
        // 项目集
        cloudParams = {};
      }
      let params = {
        implLeader: leader.join(','),
        implLeaderName: leaderCn.join(','),
        headerTeamName: headerTeamName ? headerTeamName.team_name : '',
        headerUnitName: headerTeamName ? headerTeamName.dept_name : '',
        testLeaderName: leaderN.join(','),
        testLeader: leaderE.join(','),
        testLeaderEmail: leaderM.join(','),
        prjNum: prjNum ? prjNum.project_no : '',
        planPrjName: prjNum ? prjNum.project_name : '',
        expectOwnWorkload: expectOwnWorkload
          ? Number(expectOwnWorkload)
          : expectOwnWorkload,
        expectOutWorkload: expectOutWorkload
          ? Number(expectOutWorkload)
          : expectOutWorkload,
        implDelayType: implDelayType ? implDelayType.join(',') : '',
        implDelayTypeName: delayName.join(','),
        implDelayReason,
        implUnitNum,
        leaderGroup: leaderGroup ? leaderGroup.id : '',
        planInnerTestDate,
        syncFlag: this.toIPMP ? '1' : '',
        // 是否上云相关
        ...cloudParams
      };

      await this.updateIpmpUnit(params);
      this.updateFlag = false;
      if (this.updateIpmpUnits.errorCode === '30') {
        this.unitCheck = true;
        this.checkImplUnitNum = this.updateIpmpUnits.implUnitNum;
        this.checkUrl = this.updateIpmpUnits.url;
      } else {
        successNotify('修改实施单元成功！');
        this.toIPMP = false;
        this.$emit('updateSuccess');
      }
    },
    goIpmp() {
      window.open(this.checkUrl);
    },
    userFilter(val, update, abort) {
      this.userOptions = this.userOptions.filter(item => {
        if (item.email.indexOf('@') > -1) {
          return item.email.split('@')[1] === 'spdb.com.cn';
        }
      });

      // }
      //输入中文名和账号名过滤
      update(() => {
        this.userFilterOptions = this.userOptions.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
      //遍历获取小组名
      for (let key in this.userFilterOptions) {
        let userGroup = this.groups.filter(
          item => this.userFilterOptions[key].group_id == item.id
        );
        this.userFilterOptions[key].group = userGroup[0].name;
      }
    },
    relatedGroupFilter(val, update, abort) {
      //涉及板块选择框输入过滤
      update(() => {
        this.relatedFilterGroup = this.groupsClone.filter(
          group => group.fullName.indexOf(val) > -1
        );
      });
    },
    testLeaderFilter(val, update, abort) {
      update(() => {
        this.testFilterLeader = this.testLeaderClone.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    ipmpProjectFilter(val, update, abort) {
      update(() => {
        this.ipmpFilterProject = this.ipmpProjectClone.filter(
          user =>
            user.project_name.indexOf(val) > -1 ||
            user.project_no.indexOf(val) > -1
        );
      });
    },
    ipmpTeamFilter(val, update, abort) {
      update(() => {
        this.ipmpFilterTeam = this.ipmpTeamClone.filter(
          user => user.team_name.indexOf(val) > -1
        );
      });
    },
    checkDateType() {
      this.checkDate =
        this.compareDate(
          this.assessmentModel.planDevelopDate,
          this.assessmentModel.acturalDevelopDate
        ) ||
        this.compareDate(
          this.assessmentModel.planInnerTestDate,
          this.assessmentModel.actualInnerTestDate
        ) ||
        this.compareDate(
          this.assessmentModel.planTestStartDate,
          this.assessmentModel.acturalTestStartDate
        ) ||
        this.compareDate(
          this.assessmentModel.planTestFinishDate,
          this.assessmentModel.acturalTestFinishDate
        ) ||
        this.compareDate(
          this.assessmentModel.planProductDate,
          this.assessmentModel.acturalProductDate
        );
    },
    compareDate(develpe, actural) {
      if (!develpe) return false;
      let dDate = this.dateChang(develpe);
      let aDate = '';
      if (!actural) {
        aDate = this.getToday();
      } else {
        aDate = this.dateChang(actural);
      }
      return dDate < aDate;
    }
  }
};
</script>
<style lang="stylus" scoped>
.card {
  padding: 10px;
}
.msgStyle {
  font-size: 12px;
  color: #ef5350;
  line-height: 12px;
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
.pre-style1
  display block
  line-height 1.8em
  padding 0 30px
.red
  color red
.relative {
  position: relative;
}
.quest
  margin 0 15px 0 5px
.pdl16
  padding-right 16px
.tipStyle1
  width 968px
.tipStyle
  width 900px
  background rgba(255,243,224,0.5)
  padding 10px 0
.q-chip1 {
  vertical-align: middle;
  outline: 0;
  position: relative;
  height: 26px;
  margin: 3px;
  background: #e0e0e0;
  font-size: 12px;
  line-height: 20px;
  padding: 6px 10px;
}
.q-chip1
  >>> .q-chip__icon{
  color: inherit;
  font-family: 'Material Icons Outlined';
  font-weight: normal;
  font-style: normal;
  display: inline-block;
  line-height: 1;
  text-transform: none;
  letter-spacing: normal;
  word-wrap: normal;
  white-space: nowrap;
  direction: ltr;
  -webkit-font-smoothing: antialiased;
  text-rendering: optimizeLegibility;
  -moz-osx-font-smoothing: grayscale;
  font-feature-settings: 'liga';
}
</style>
