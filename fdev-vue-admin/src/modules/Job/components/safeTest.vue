<template>
  <div class="test-box-wrap">
    <div class="row q-mt-16 items-center line-title">
      <f-icon name="basic_msg_s_f" class="titleimg" />
      <span class="titlename">提测</span>
    </div>
    <fdev-tabs align="left" class="tabs-border-bottom" v-model="testType">
      <fdev-tab name="function" label="功能测试"> </fdev-tab>
      <fdev-tab name="safe" label="安全测试"> </fdev-tab>
    </fdev-tabs>
    <div class="height-10"></div>
    <!-- 功能测试 -->
    <div
      class="radioStyle"
      :class="testData.reason == 1 && 'row'"
      v-show="testType === 'function'"
    >
      <f-formitem full-width bottom-page label="测试原因">
        <fdev-option-group
          v-model="testData.reason"
          :options="testReason"
          color="primary"
          inline
        />
      </f-formitem>
      <template>
        <div class="row no-wrap  width-100" v-if="testData.reason === '1'">
          <div class="col-6 q-mr-md">
            <f-formitem label="回归测试范围" class="q-mt-14">
              <fdev-option-group
                v-model="testData.regressionTestScope"
                :options="radioOptions"
                color="primary"
                inline
              />
            </f-formitem>
            <f-formitem
              label="涉及的回归测试范围"
              v-show="testData.regressionTestScope"
              required
              class="q-mt-14 pb4 "
            >
              <fdev-input
                v-model="testData.regressionText"
                type="textarea"
                ref="$v.testData.regressionText.$model"
                :rules="[
                  () =>
                    $v.testData.regressionText.required ||
                    '请输入涉及的回归测试范围'
                ]"
              />
            </f-formitem>
          </div>
          <div class="col-6 q-mr-md">
            <f-formitem label="客户端版本" class="q-mt-14">
              <fdev-option-group
                v-model="testData.clientVersion"
                :options="radioOptions"
                color="primary"
                inline
              />
            </f-formitem>
            <f-formitem
              label="涉及的客户端版本"
              v-show="testData.clientVersion"
              required
              class="q-mt-14 pb4 "
            >
              <fdev-input
                v-model="testData.clientVersionText"
                type="textarea"
                ref="$v.testData.clientVersionText.$model"
                :rules="[
                  () =>
                    $v.testData.clientVersionText.required ||
                    '请输入涉及的客户端版本'
                ]"
              />
            </f-formitem>
          </div>
        </div>
        <div class="row no-wrap width-100">
          <div class="col-6 q-mr-md">
            <!-- <f-formitem label="测试环境" class="q-mt-14">
              <fdev-option-group
                v-model="testData.testEnv"
                :options="radioOptions"
                color="primary"
                inline
              />
            </f-formitem> -->
            <f-formitem label="涉及的测试环境" required class="q-mt-14 pb4 ">
              <fdev-input
                v-model="testData.testEnvText"
                type="textarea"
                ref="$v.testData.testEnvText.$model"
                :rules="[
                  () =>
                    $v.testData.testEnvText.required || '请输入涉及的测试环境'
                ]"
              />
            </f-formitem>
          </div>
          <div class="col-6 q-mr-md" v-if="testData.reason === '1'">
            <f-formitem label="交易接口改动" class="q-mt-14 col-6">
              <fdev-option-group
                v-model="testData.interfaceChange"
                :options="radioOptions"
                color="primary"
                inline
              />
            </f-formitem>
            <f-formitem
              label="涉及的交易接口改动"
              v-show="testData.interfaceChange"
              required
              class="q-mt-14 pb4 "
            >
              <fdev-input
                v-model="testData.interfaceChangeText"
                type="textarea"
                ref="$v.testData.interfaceChangeText.$model"
                :rules="[
                  () =>
                    $v.testData.interfaceChangeText.required ||
                    '请输入涉及的交易接口改动'
                ]"
              />
            </f-formitem>
          </div>
        </div>
      </template>
      <div class="row no-wrap width-100">
        <f-formitem
          label="抄送人员"
          optional
          class="q-mt-14 col-6 q-mr-md"
          v-show="testData.reason === '1'"
        >
          <fdev-select
            use-input
            multiple
            input-debounce="0"
            ref="testData.copyTo"
            v-model="testData.copyTo"
            :options="copyToOptions"
            option-label="user_name_cn"
            @filter="copyToFilter"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label :title="scope.opt.user_name_en" caption>
                    {{ scope.opt.user_name_en }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem label="功能描述" required class="q-mt-14 col-6 q-mr-md">
          <fdev-input
            v-model="testData.desc"
            type="textarea"
            ref="$v.testData.desc.$model"
            :rules="[
              () => $v.testData.desc.required || '请输入本次提交所做修改描述'
            ]"
            hint=""
          />
        </f-formitem>
      </div>
      <div class="col-12 row justify-center mt20">
        <div>
          <!-- 涉及sit2部署的团队的逻辑 -->
          <fdev-btn
            :loading="globalLoading['jobForm/toNoticeTest']"
            label="提交功能测试"
            v-if="appGroup"
            :disable="
              job.developer.length === 0 ||
                (deployStatus != null && deployStatus != '2')
            "
            @click="noticeTest"
          />
          <!-- 其他逻辑 -->
          <fdev-btn
            :loading="globalLoading['jobForm/toNoticeTest']"
            label="提交功能测试"
            v-if="!appGroup"
            :disable="job.developer.length === 0 || !mergerData.merged"
            @click="noticeTest"
          />
          <fdev-tooltip
            anchor="top middle"
            self="bottom middle"
            :offest="[-70, 0]"
            v-if="
              job.developer.length === 0 ||
                !mergerData.merged ||
                (deployStatus != null && deployStatus != 2)
            "
          >
            <span v-if="job.developer.length === 0">
              该任务无开发人员，不能提测 ，请点击 [ 修改 ] 按钮，补全开发人员
            </span>
            <span v-if="!mergerData.merged">
              该任务未成功合并过SIT,请点击[分支合并]
            </span>
            <span v-if="deployStatus == '0'">
              该任务部署申请未审批，请联系部署申请审批人员进行申请
            </span>
            <span v-if="deployStatus == '1'">
              该任务sit2部署失败，请联系部署申请审批人员在已完成审批列表中，进行重新部署
            </span>
            <span v-if="deployStatus == '3'">
              该任务还在部署中，请稍后再试....
            </span>
          </fdev-tooltip>
        </div>
        <!-- 如果是简单任务类型则跳过功能测试 -->
        <div v-if="jobProfile.simpleDemand === '0'" class="q-ml-sm">
          <!-- 涉及sit2部署的 -->
          <fdev-btn
            label="跳过功能测试"
            @click="jumpOverTest"
            v-if="appGroup"
            :disable="
              job.developer.length === 0 ||
                (deployStatus != null && deployStatus != '2')
            "
          />
          <!-- 其他 -->
          <fdev-btn
            label="跳过功能测试"
            @click="jumpOverTest"
            v-if="!appGroup"
            :disable="job.developer.length === 0 || !mergerData.merged"
          />
          <fdev-tooltip
            anchor="top middle"
            self="bottom middle"
            :offest="[-70, 0]"
            v-if="
              job.developer.length === 0 ||
                !mergerData.merged ||
                (deployStatus != null && deployStatus != 2)
            "
          >
            <span v-if="job.developer.length === 0">
              该任务无开发人员，不能提测 ，请点击 [ 修改 ] 按钮，补全开发人员
            </span>
            <span v-if="!mergerData.merged">
              该任务未成功合并过SIT,请点击[分支合并]
            </span>
            <span v-if="deployStatus == '0'">
              该任务部署申请未审批，请联系部署申请审批人员进行申请
            </span>
            <span v-if="deployStatus == '1'">
              该任务sit2部署失败，请联系部署申请审批人员在已完成审批列表中，进行重新部署
            </span>
            <span v-if="deployStatus == '3'">
              该任务还在部署中，请稍后再试....
            </span>
          </fdev-tooltip>
        </div>
      </div>
    </div>
    <!-- 安全测试 -->
    <div class="safe-test-box" v-show="testType === 'safe'">
      <div class="row">
        <div class="col-6">
          <f-formitem label="涉及第三方系统" class="">
            <fdev-input v-model="testData.tripartiteSystem" />
          </f-formitem>
          <f-formitem
            label="接口说明"
            help="如涉及后端系统，提供esb接口说明文档"
            class="mt20 interface-desc"
          >
            <div class="row items-center">
              <fdev-btn
                ficon="upload"
                flat
                class="borderLine q-px-md"
                @click="uploadInterface('interface')"
                label="上传"
              />
              <span
                class="ellipsis q-ml-10  row items-center"
                :title="interfaceFile.name ? interfaceFile.name : ''"
                >{{
                  interfaceFile.name
                    ? interfaceFile.name
                    : '支持后缀名7z,zip,rar'
                }}</span
              >
              <input
                @change="selFile('interface')"
                class="none"
                type="file"
                value="上传接口说明文档"
                id="upload"
              />
            </div>
          </f-formitem>
        </div>
        <div class="col-6">
          <f-formitem label="涉及第三方系统接口">
            <fdev-input v-model="testData.systemInterface" />
          </f-formitem>
          <f-formitem label="安全测试描述" class="mt20 ">
            <fdev-input type="textarea" v-model="testData.safeDescribe" />
          </f-formitem>
        </div>
        <div class="row">
          <f-formitem
            label="安全测试交易清单"
            required
            class="mt20"
            value-style="flex:1"
            full-width
          >
            <div class="flex">
              <fdev-btn
                ficon="download"
                class="borderLine q-px-md"
                flat
                @click="downloadTrade"
                label="下载模板文件"
              />
              <fdev-btn
                ficon="upload"
                class="q-ml-llg borderLine q-px-md"
                flat
                @click="uploadInterface('trans')"
                label="上传"
              />
              <span
                class="uploadMsg ellipsis flex1"
                :title="tradeFile.name ? tradeFile.name : ''"
                >{{ tradeFile.name ? tradeFile.name : '支持后缀名xlsx' }}</span
              >
              <input
                @change="selFile('trans')"
                class="none"
                dash
                type="file"
                value="上传安全测试交易清单"
                id="upload1"
              />
            </div>
          </f-formitem>
        </div>
        <div class="col-12" v-if="safeTestList && safeTestList.length > 0">
          <div
            class="row mt20 q-px-10 justify-between"
            style="background:#F4F6FD;"
          >
            <div class="width-50">
              序号
            </div>
            <div class="col-2">
              <span class="q-pr-xs" style="color:#EF5350">*</span>交易名称
            </div>
            <div class="col-2">
              <span class="q-pr-xs" style="color:#EF5350">*</span>交易描述
            </div>
            <div class="col-2">
              <span class="q-pr-xs" style="color:#EF5350">*</span>功能入口
            </div>
            <div class="col-2 q-pl-md">操作</div>
          </div>
          <transition-group name="add">
            <div
              v-for="(item, index) in safeTestList"
              :key="item.index"
              class="row items-center q-px-10 justify-between"
            >
              <div class="width-50">
                {{ index + 1 }}
              </div>
              <div class="col-2 pd0">
                <fdev-input
                  v-model="item.transName"
                  ref="notEmpty"
                  :rules="[val => !!val || '不能为空']"
                />
              </div>
              <div class="col-2 pd0">
                <fdev-input
                  v-model="item.transDesc"
                  ref="notEmpty"
                  :rules="[val => !!val || '不能为空']"
                />
              </div>
              <div class="col-2 pd0">
                <fdev-input
                  v-model="item.functionMenu"
                  ref="notEmpty"
                  :rules="[val => !!val || '不能为空']"
                />
              </div>
              <div class="col-2 q-pl-md row items-center">
                <fdev-btn flat label="新增" @click="add(index)" />
                <span class="line"></span>
                <fdev-btn flat label="删除" @click="del(index)" />
              </div>
            </div>
          </transition-group>
        </div>
      </div>
      <div class="row justify-center mt54">
        <div>
          <fdev-tooltip
            anchor="top middle"
            self="bottom middle"
            :offest="[-70, 0]"
            v-if="safeFlag"
          >
            <span v-if="jobProfile.stage === 'develop'">请先提交功能测试</span>
            <span v-else-if="safeFlag">请上传安全测试交易清单</span>
          </fdev-tooltip>
          <fdev-btn
            :disable="safeFlag"
            label="提交安全测试"
            :loading="loading"
            @click="submitSafe"
          />
        </div>
      </div>
    </div>
    <f-dialog
      v-model="couldSubmit"
      transition-show="slide-up"
      transition-hide="slide-down"
      title="提示"
    >
      <div style="max-width:500px">
        缺少文件{{
          checkList.missType == 0
            ? '需求说明书'
            : checkList.missType == 1
            ? '需求规格说明书'
            : '需求说明书和需求规格说明书'
        }}
        ,请先前往需求<span
          class="normal-link1"
          @click="goRqr(checkList.rqrNo)"
          >{{ job.demand.oa_contact_no }}</span
        >
        上传文件至{{
          checkList.missType == 0
            ? '需求说明书'
            : checkList.missType == 1
            ? '需求规格说明书'
            : '需求说明书和需求规格说明书'
        }}目录下
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          flat
          label="确定"
          color="primary"
          v-close-popup
          @click="couldSubmit = false"
        />
      </template>
    </f-dialog>
    <!-- 未挂载实施单元弹窗 -->
    <f-dialog
      v-model="mountUnitFlagQ"
      transition-show="slide-up"
      transition-hide="slide-down"
      title="挂载研发单元"
    >
      <span>
        任务所属的研发单元还未挂载实施单元，请进入【<fdev-btn
          flat
          :label="job.demand.oa_contact_no"
          @click="go2Rqr"
        ></fdev-btn
        >】详情页进行挂载
      </span>
      <template v-slot:btnSlot>
        <fdev-btn flat label="去挂载" @click="go2Rqr()" />
      </template>
    </f-dialog>
    <!-- 测试通知 进行SIT测试 -->
    <f-dialog
      v-model="noticeTestSuccess"
      transition-show="slide-up"
      transition-hide="slide-down"
      title="提示"
    >
      <fdev-card-section class="q-mb-md q-ml-xs">
        测试通知发送成功，还请联系对应的内测人员
        {{ nameFilter(job.tester) }}进行SIT测试！
      </fdev-card-section>
      <template v-slot:btnSlot>
        <fdev-btn dialog label="确定" @click="noticeTestSuccess = false" />
      </template>
    </f-dialog>
    <!-- 确认提测信息 弹窗 -->
    <f-dialog
      right
      :persistent="false"
      v-model="testConfirmDialogOpened"
      title="确认提测信息"
    >
      <table class="bg-white table">
        <tr>
          <td>需求编号/需求名称</td>
          <td>{{ testConfirmDialogData.rqrNo }}</td>
        </tr>
        <tr>
          <td>任务名称</td>
          <td>{{ job.name }}</td>
        </tr>
        <tr>
          <td>计划开始uat测试日期</td>
          <td>{{ job.plan_uat_test_start_time }}</td>
        </tr>
        <tr>
          <td>计划投产日期</td>
          <td>{{ job.plan_fire_time }}</td>
        </tr>
        <tr>
          <td>功能描述</td>
          <td v-html="logFilter(testConfirmDialogData.desc)" />
        </tr>
        <tr>
          <td>回归测试范围</td>
          <td v-html="logFilter(testConfirmDialogData.regressionTestScope)" />
        </tr>
        <tr>
          <td>是否涉及数据库改动</td>
          <td>{{ isInvolveInterface.data_base_alter }}</td>
        </tr>
        <tr>
          <td>客户端版本</td>
          <td v-html="logFilter(testConfirmDialogData.clientVersion)" />
        </tr>
        <tr>
          <td>测试环境</td>
          <td v-html="logFilter(testConfirmDialogData.testEnv)" />
        </tr>
        <tr>
          <td>交易接口改动</td>
          <td v-html="logFilter(testConfirmDialogData.interfaceChange)" />
        </tr>
        <tr>
          <td>应用名称</td>
          <td>{{ job.project_name }}</td>
        </tr>
        <tr>
          <td>涉及关联系统同步改造</td>
          <td>{{ isInvolveInterface.external }}</td>
        </tr>
        <tr>
          <td>开发人员</td>
          <td>
            {{ job.developer.map(user => user.user_name_cn).join(',') }}
          </td>
        </tr>
        <tr>
          <td>抄送人员</td>
          <td>{{ testConfirmDialogData.copyToNameCn }}</td>
        </tr>
      </table>

      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          label="确定"
          @click="handleTestConfirmDialogOpened"
          :loading="globalLoading['jobForm/toNoticeTest']"
        />
      </template>
    </f-dialog>
  </div>
</template>
<script>
import { mapState, mapActions } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import {
  validate,
  errorNotify,
  successNotify,
  resolveResponseError,
  exportExcel
} from '@/utils/utils';
import { testData } from '../utils/constants';
import { uploadSecurityTestDoc, skipInnerTest } from '@/services/job';
export default {
  name: 'safeTest',
  props: {
    testData: {
      type: Object
    },
    job: {
      type: Object
    },
    checkList: {
      type: Object
    },
    mergerData: {
      type: Object
    },
    jobProfile: {
      type: Object
    },
    isLoginUserList: {
      type: Array
    },
    deployStatus: {
      type: String
    },
    appGroup: {
      type: Boolean
    }
  },
  validations: {
    testData: {
      reason: {
        required
      },
      desc: {
        required
      },
      regressionText: {
        required(val) {
          if (this.testData.regressionTestScope) {
            return !!val.trim();
          }
          return true;
        }
      },
      clientVersionText: {
        required(val) {
          if (this.testData.clientVersion) {
            return !!val.trim();
          }
          return true;
        }
      },
      testEnvText: {
        required(val) {
          if (val) {
            return !!val.trim();
          }
          return true;
        }
      },
      interfaceChangeText: {
        required(val) {
          if (this.testData.interfaceChange) {
            return !!val.trim();
          }
          return true;
        }
      }
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('jobForm', ['codeQuality', 'mountUnitFlag', 'templateFile']),
    safeFlag() {
      let disabled = true;
      let valueArr = [];
      if (
        this.safeTestList &&
        this.safeTestList.length > 0 &&
        this.jobProfile.stage !== 'develop'
      ) {
        this.safeTestList.map(item => {
          valueArr.push(...Object.values(item));
        });
        disabled = !valueArr.every(item => !!item);
      }
      return disabled;
    },
    isInvolveInterface() {
      let { data_base_alter, external } = this.job.review;
      data_base_alter =
        data_base_alter && data_base_alter.length > 0
          ? data_base_alter.map(item => item.name).join(',')
          : '否';
      external =
        external && external.length > 0
          ? external.map(item => item.name).join(',')
          : '否';
      return { data_base_alter, external };
    }
  },
  data() {
    return {
      testType: 'function',
      copyToOptions: [],
      safeTestList: [],
      testLength: 0,
      mountUnitFlagQ: false,
      couldSubmit: false,
      interfaceFiles: {},
      noticeTestSuccess: false,
      testConfirmDialogOpened: false,
      testConfirmDialogData: testData(),
      radioOptions: [
        { label: '不涉及', value: false },
        { label: '涉及', value: true }
      ],
      testReason: [
        { label: '正常', value: '1' },
        { label: '缺陷', value: '2' },
        { label: '需求变更', value: '3' }
      ],
      interfaceFile: { name: '', size: '' },
      tradeFile: { name: '', size: '' },
      interfaceDate: {},
      safeListDate: {},
      loading: false,
      simpleFlag: false
    };
  },
  methods: {
    ...mapActions('jobForm', [
      'checkMountUnit',
      'queryRqrDocInfo',
      'getCodeQuality',
      'toNoticeTest',
      'downloadTemplateFile',
      'putSecurityTest',
      'testReportCreate'
    ]),
    copyToFilter(val, update, abort) {
      const needle = val.toLowerCase().trim();
      update(() => {
        this.copyToOptions = this.isLoginUserList.filter(user => {
          return (
            user.user_name_cn.includes(needle) ||
            user.user_name_en.includes(needle)
          );
        });
      });
    },
    //上传第三方接口文档
    uploadInterface(type) {
      let fileDome;
      if (type === 'interface') {
        fileDome = document.querySelector('#upload');
      } else if (type === 'trans') {
        fileDome = document.querySelector('#upload1');
      }
      fileDome.value = '';
      fileDome.click();
    },
    async selFile(type) {
      let file;
      if (type === 'interface') {
        file = document.querySelector('#upload').files[0];
      } else if (type === 'trans') {
        file = document.querySelector('#upload1').files[0];
      }
      file = file ? file : { name: '', size: '' };
      let length = file.name.split('.').length;
      let fileTypeName = file.name.split('.')[length - 1];
      if (
        type === 'interface' &&
        fileTypeName !== '7z' &&
        fileTypeName !== 'zip' &&
        fileTypeName !== 'rar'
      ) {
        errorNotify('上传的文件只能为压缩文件7z,zip,rar格式!');
        return;
      } else if (type === 'trans' && fileTypeName !== 'xlsx') {
        errorNotify('上传的文件只能为xlsx格式!');
        return;
      }
      let uploaderParam = new FormData();
      uploaderParam.append('fileName', file.name);
      uploaderParam.append('fileType', type);
      uploaderParam.append('taskId', this.jobProfile.id);
      uploaderParam.append('file', file);
      //上传接口文档
      let res = await resolveResponseError(() =>
        uploadSecurityTestDoc(uploaderParam)
      );
      if (type === 'interface') {
        this.interfaceFile = file;
        this.interfaceDate = res;
      } else if (type === 'trans') {
        this.tradeFile = file;
        this.safeListDate = res;
        this.safeTestList = res.transList;
        this.testLength = res.transList.length;
      }
      successNotify('上传成功！');
    },
    //下载测试清单
    async downloadTrade() {
      let param = {
        fileType: 'trans'
      };
      await this.downloadTemplateFile(param);
      exportExcel(this.templateFile);
    },
    //新增安全测试交易清单列表
    async add(index) {
      this.testLength++;
      this.safeTestList.splice(index + 1, 0, {
        index: this.testLength,
        transName: null,
        transDesc: null,
        functionMenu: null
      });
      await this.$nextTick();
      this.$refs.notEmpty.map(item => {
        item.focus();
      });
      this.$refs.notEmpty.slice(-1)[0].blur();
    },
    del(index) {
      this.safeTestList.splice(index, 1);
    },
    async noticeTest(send) {
      // 如果任务的研发单元未挂载实施单元，弹窗提示挂载
      await this.checkMountUnit({ id: this.$route.params.id });
      this.mountUnitFlagQ = !this.mountUnitFlag.mountUnitFlag;
      if (this.mountUnitFlagQ) {
        return;
      }
      await this.queryRqrDocInfo({ taskId: this.$route.params.id });
      this.couldSubmit = !this.checkList.result;
      if (!this.checkList.result) {
        return;
      }
      //跳过内测时不检查表单字段
      if (send !== 'jump') {
        this.$v.testData.$touch();
        let uatKeys = Object.keys(this.$refs).filter(
          key => key.indexOf('testData') > -1
        );
        validate(uatKeys.map(key => this.$refs[key]));
        if (this.$v.testData.$invalid) {
          return;
        }
      }
      // sonar卡点判断
      await this.getCodeQuality({ task_id: this.job.id });
      // tips 不为空 都提示 然后开关状态 开 卡点  tips为空  直接过 正常流程
      if (this.codeQuality.tips) {
        this.$q
          .dialog({
            title: 'sonar描述提示',
            message: `${this.codeQuality.tips}`,
            ok: '知道啦'
          })
          .onOk(() => {
            if (this.codeQuality.qube_switch === '1') {
              return;
            } else {
              this.relTest();
            }
          });
      } else {
        this.relTest();
      }
    },
    relTest() {
      //简单任务跳过
      if (this.simpleFlag) {
        return;
      }
      if (this.testData.reason !== '1') {
        this.submitTestData();
      } else {
        this.testConfirmDialogOpened = true;
        this.testConfirmDialogData = {
          desc: this.testData.desc,
          regressionTestScope: this.testData.regressionTestScope
            ? this.testData.regressionText
            : '不涉及',
          clientVersion: this.testData.clientVersion
            ? this.testData.clientVersionText
            : '不涉及',
          interfaceChange: this.testData.interfaceChange
            ? this.testData.interfaceChangeText
            : '不涉及',
          testEnv: this.testData.testEnvText,
          copyToNameCn: this.testData.copyTo
            .map(user => user.user_name_cn)
            .join(','),
          copyTo: this.testData.copyTo.map(user => user.user_name_en),
          rqrNo:
            this.job.demand.oa_contact_no +
            '/' +
            this.job.demand.oa_contact_name
        };
      }
    },
    async handleTestConfirmDialogOpened() {
      await this.submitTestData();
      this.testConfirmDialogOpened = false;
    },
    goRqr(id) {
      this.$router.push({
        path: `/rqrmn/rqrProfile/${id}`,
        query: { tab: 'file' }
      });
    },
    async submitTestData() {
      let params;
      if (this.testData.reason === '1') {
        params = { ...this.testConfirmDialogData };
        delete params.copyToNameCn;
        delete params.desc;
      }
      await this.toNoticeTest({
        id: this.job.id,
        test_reason: this.testData.reason,
        repair_desc: this.testData.desc,
        rqrNo:
          this.job.demand.oa_contact_no + '/' + this.job.demand.oa_contact_name,
        ...params
      });
      this.testReportCreate({
        id: this.job.id,
        testEnv: this.testData.testEnvText
      });
      this.noticeTestSuccess = true; //提交内侧成功  刷新头部信息
      this.$root.$emit('updateTaskStage'); //更新阶段 状态
      this.$root.$emit('updateTopTaskStage'); //更新头部状态
      // this.$emit('updateDetail');
    },
    //跳过功能测试
    async jumpOverTest() {
      this.$q
        .dialog({
          title: '跳过功能测试',
          message: '您确定要跳过功能测试？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this.simpleFlag = true;
          this.handleJump();
        });
    },
    async handleJump() {
      await this.noticeTest('jump');
      //修改任务状态
      await resolveResponseError(() => skipInnerTest({ id: this.job.id }));
      successNotify('操作成功!');
      this.simpleFlag = false;
      //跳过
      this.$root.$emit('updateTaskStage'); //更新阶段 状态
      this.$root.$emit('updateTopTaskStage'); //更新头部状态
    },
    //提交安全测试
    async submitSafe() {
      this.loading = true;
      this.safeTestList.map((item, index) => {
        item.index = (index + 1).toString();
      });
      let params = {
        taskId: this.jobProfile.id,
        correlationSystem: this.testData.tripartiteSystem,
        correlationInterface: this.testData.systemInterface,
        interfaceFilePath: this.interfaceDate
          ? this.interfaceDate.filePath
          : '',
        transFilePath: this.safeListDate ? this.safeListDate.filePath : '',
        transList: this.safeTestList,
        remark: this.testData.safeDescribe
      };
      try {
        await this.putSecurityTest(params);
        this.loading = false;
        successNotify('提交安全测试成功');
      } catch (error) {
        this.loading = false;
        throw error;
      }
    },
    // 需求详情页挂载
    go2Rqr() {
      let rqrmnId = this.jobProfile.rqrmnt_no;
      this.mountUnitFlagQ = false;
      this.$router.push({
        path: `/rqrmn/rqrProfile/${rqrmnId}`,
        query: { tab: 'developNo' }
      });
    },
    logFilter(val) {
      val = val ? val : '';
      return val
        .replace(/</g, '&lt;')
        .replace(/</g, '&gt;')
        .replace(/\n/g, '<br/>');
    },
    nameFilter(nameList) {
      if (Array.isArray(nameList)) {
        let names = [];
        nameList.forEach(item => {
          if (item.user_name_cn) {
            names.push(item.user_name_cn);
          }
        });
        return names.join('，') ? ` ( ${names.join('，')} ) ` : '';
      } else {
        return ` ( ${nameList} ) `;
      }
    }
  }
};
</script>
<style lang="stylus" scoped>
.none
  display none
.pd0
  padding-left 0
  >>> .q-field--with-bottom
    padding-bottom 0
.pb4
  >>> .q-field--with-bottom
    padding-bottom 4px
.line
  height 14px
  width 1px
  display inline-block
  border-right 1px solid #ddd
  margin 0 4px
.add-enter-active, .add-leave-active
  transition all .5s ease
.add-enter
  opacity 0
  transform translateY(100%)
.add-leave-to
  opacity 0
  transform translateY(-100%)
.mt60
  margin-top 60px
.uploadMsg
  color #666
  margin-left 10px
.mt54
  margin-top 54px
.borderTop
  border-top 1px solid #eceff1
.table
  width 650px
  border-radius 5px
  border-collapse collapse
  border 1px solid $grey-5
  td
    padding 10px 10px
    text-align left
    border 1px solid $grey-5
    color $grey-8
    word-break: break-all;
  tr
    td:nth-of-type(2n-1)
      width 30%
      font-weight 600
      color black
      background $grey-2
.q-textarea >>> textarea
  word-break break-all;
.flex1
  flex 1
.borderLine
  border 1px dashed currentColor
.visbility
  visibility hidden
.normal-link1
  cursor: pointer;
  color: #2196f3;
  word-break: break-all;
  padding: 0 5px;
.mt20
  margin-top 20px
.q-mt-10
  margin-top 10px
.q-px-10
  padding 10px 10px
.width-50
  width 50px
.q-mt-14
  margin-top 14px
.q-ml-10
      margin-left 10px
.width-100
  width 100%
>>> .q-option-group>div
  margin-right 16px
>>> .q-radio__label.q-anchor--skip
  min-width 42px
>>> .q-gutter-x-sm > *, .q-gutter-sm > *
  margin-left 0
>>> .q-gutter-x-sm, .q-gutter-sm
  margin-left 0
.height-10
  height 10px
.test-box-wrap >>>
  .input-md
    width 220px
  .label
    width 90px
  .interface-desc .label-with-help
    width 90px
    margin-right 16px
@media (max-width: 1439px)
  .test-box-wrap >>>
    .input-md
      width 180px
    .label
      width 80px
    .interface-desc .label-with-help
      width 80px
      margin-right 16px
    .q-ml-10
      margin-left 0
</style>
