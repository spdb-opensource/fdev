<template>
  <f-block>
    <!-- <template v-slot:header> -->
    <Loading class="bg-white" v-if="!hasAuthority">
      <f-block>
        <div class="row">
          <f-formitem label="所属小组:" page>
            <span>{{ jobProfile.group && jobProfile.group.name }}</span>
          </f-formitem>
          <f-formitem label="需求编号:" page>
            <span>{{
              jobProfile.demand && jobProfile.demand.oa_contact_no
            }}</span>
          </f-formitem>
          <f-formitem label="需求名称:" page>
            <span>{{
              jobProfile.demand && jobProfile.demand.oa_contact_name
            }}</span>
          </f-formitem>
        </div>
      </f-block>

      <div class="div3 mainDiv">
        <fdev-stepper
          v-model="stepChild"
          animated
          flat
          vertical
          ref="stepper"
          class="text-center leftItem"
        >
          <fdev-step
            :name="1"
            title="上传设计稿"
            header-nav
            :done="reviewerObj.uploaded && reviewerObj.uploaded.length > 0"
            class="step-position"
            prefix="1"
          >
            请上传完整的标注文件
          </fdev-step>
          <fdev-step
            :name="2"
            title="审核分配"
            :done="reviewerObj.wait_allot && reviewerObj.wait_allot.length > 0"
            header-nav
            class="step-position"
            prefix="2"
          >
            <div>
              UI团队负责人指定设计师审核
            </div>
          </fdev-step>
          <fdev-step
            :name="3"
            title="审核反馈"
            :done="reviewerObj.fixing && reviewerObj.fixing.length > 0"
            header-nav
            class="step-position"
            prefix="3"
          >
            <div>
              如有不符规范处，请设计师上传审核后文件
            </div>
          </fdev-step>

          <fdev-step
            :name="reviewerObj.nopass ? 4 : null"
            title="上传新设计稿"
            header-nav
            :done="reviewerObj.nopass && reviewerObj.nopass.length > 0"
            class="step-position"
            prefix="reviewerObj.nopass ? 4 : null"
            v-if="
              reviewerObj.nopass &&
                reviewerObj.nopass[0].stage === 'load_nopass'
            "
          >
          </fdev-step>

          <fdev-step
            :name="reviewerObj.nopass && reviewerObj.nopass.length > 0 ? 5 : 4"
            title="审核完成"
            :done="reviewerObj.finished && reviewerObj.finished.length > 0"
            header-nav
            class="step-position"
            :prefix="
              reviewerObj.nopass && reviewerObj.nopass.length > 0 ? 5 : 4
            "
          >
          </fdev-step>
        </fdev-stepper>
        <div class="div4">
          <div class="step">
            <div class="name">
              {{ reviewerObj.uploaded ? reviewerObj.uploaded[0].name : '' }}
            </div>
            <div class="time">
              {{ reviewerObj.uploaded ? reviewerObj.uploaded[0].time : '' }}
            </div>
          </div>
          <div class="step">
            <div class="name">
              {{ reviewerObj.fixing ? reviewerObj.fixing[0].name : '' }}
            </div>
            <div class="time">
              {{ reviewerObj.fixing ? reviewerObj.fixing[0].time : '' }}
            </div>
          </div>
          <div class="step">
            <div
              class="name"
              v-if="
                (reviewerObj.finished && reviewerObj.finished.length > 0) ||
                  (reviewerObj.nopass && reviewerObj.nopass.length > 0)
              "
            >
              {{
                jobProfile
                  ? jobProfile.reviewer
                    ? jobProfile.reviewer.user_name_cn
                    : ''
                  : ''
              }}
            </div>
            <div
              class="time"
              v-if="
                reviewerObj.finished &&
                  reviewerObj.finished.length > 0 &&
                  !reviewerObj.nopass
              "
            >
              {{ reviewerObj.finished[0].time }}
            </div>
            <div
              class="time"
              v-if="reviewerObj.nopass && reviewerObj.nopass.length > 0"
            >
              {{ reviewerObj.nopass[0].time }}
            </div>
          </div>
          <div
            class="step"
            v-if="reviewerObj.nopass && reviewerObj.nopass.length > 0"
          >
            <div
              class="name"
              v-if="reviewerObj.nopass && reviewerObj.nopass.length > 1"
            >
              {{ reviewerObj.nopass ? reviewerObj.nopass[1].name : '' }}
            </div>
            <div
              class="time"
              v-if="reviewerObj.nopass && reviewerObj.nopass.length > 1"
            >
              {{ reviewerObj.nopass[1].time }}
            </div>
          </div>
          <div class="step">
            <div
              class="name"
              v-if="reviewerObj.finished && reviewerObj.finished.length > 0"
            >
              {{ reviewerObj.finished ? reviewerObj.finished[0].name : '' }}
            </div>
            <div
              class="time"
              v-if="reviewerObj.finished && reviewerObj.finished.length > 0"
            >
              {{ reviewerObj.finished[0].time }}
            </div>
          </div>
        </div>
        <div class="div2 div6 rightItem">
          <div class="step">
            <div class="row">
              <div class="div3 div-1">
                <div class="div-2">
                  <div class="div3">
                    <div>
                      <fdev-tooltip
                        v-if="demandFile"
                        anchor="top middle"
                        self="center middle"
                        :offest="[-20, 0]"
                      >
                        {{ demandFile ? '点击下载' : '' }}
                      </fdev-tooltip>
                      <a
                        class="link div-3 cursor-pointer"
                        @click="download(demandFile)"
                        v-if="demandFile"
                        >{{ demandFile ? demandFile.fileName : '' }}
                      </a>
                    </div>
                    <div v-if="demandFile" style="flex: 1"></div>
                    <fdev-btn
                      v-if="!demandFile"
                      @click="selBtn(0)"
                      label="上传设计稿"
                    />
                    <div>
                      <fdev-tooltip
                        v-if="disBtn && demandFile"
                        anchor="top middle"
                        self="center middle"
                        :offest="[-20, 0]"
                      >
                        设计稿已审核，无法重新上传
                      </fdev-tooltip>
                      <fdev-btn
                        :disable="disBtn"
                        v-if="demandFile"
                        outline
                        @click="selBtn(0)"
                        label="重新上传"
                      ></fdev-btn>
                    </div>
                  </div>
                </div>
                <input
                  @change="selFile(0)"
                  class="none"
                  type="file"
                  value="上传IOS截图"
                  id="demand0"
                />
              </div>
            </div>
          </div>
          <div class="step">
            <div class="row">
              <div v-if="showUserSelect">
                <fdev-select
                  use-input
                  v-model="reviewer"
                  :options="users"
                  class="wd200"
                  option-label="user_name_cn"
                  option-value="user_name_en"
                  @filter="userFilter"
                  :disable="disableUserSelect"
                >
                  <template v-slot:option="scope">
                    <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                      <fdev-item-section>
                        <fdev-item-label :title="scope.opt.user_name_cn">
                          {{ scope.opt.user_name_cn }}
                        </fdev-item-label>
                        <fdev-item-label
                          :title="scope.opt.user_name_en"
                          caption
                        >
                          {{ scope.opt.user_name_en }}
                        </fdev-item-label>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </div>
            </div>
          </div>
          <div
            class="step"
            v-if="(isManager && stepChild == 3) || stepChild > 3"
          >
            <div class="div-9">
              <fdev-btn
                :disable="disBtn || !isManager"
                :class="[
                  (reviewerObj.nopass && reviewerObj.nopass.length > 0) ||
                  changeOptVal === 'false'
                    ? 'check'
                    : ''
                ]"
                @click="changeOpt('false')"
                outline
                label="审核未通过"
              />
              <fdev-btn
                :disable="disBtn || !isManager"
                :class="[
                  (!reviewerObj.nopass &&
                    reviewerObj.finished &&
                    reviewerObj.finished.length !== 0) ||
                  changeOptVal === 'true'
                    ? 'check'
                    : ''
                ]"
                @click="changeOpt('true')"
                outline
                label="审核通过"
              />
            </div>
          </div>

          <div
            class="step"
            v-if="reviewerObj.nopass && reviewerObj.nopass.length !== 0"
          >
            <div class="row">
              <div class="div3 div-1">
                <div class="div-2">
                  <div class="div3">
                    <div>
                      <fdev-tooltip
                        v-if="newDemandFile"
                        anchor="top middle"
                        self="center middle"
                        :offest="[-20, 0]"
                      >
                        {{ newDemandFile ? '点击下载' : '' }}
                      </fdev-tooltip>
                      <a
                        class="link div-3 cursor-pointer"
                        @click="download(newDemandFile)"
                        v-if="newDemandFile"
                        >{{ newDemandFile ? newDemandFile.fileName : '' }}
                      </a>
                    </div>
                    <div v-if="newDemandFile" style="flex: 1"></div>
                    <div>
                      <fdev-tooltip
                        v-if="!isManager && !newDemandFile"
                        anchor="top middle"
                        self="center middle"
                        :offest="[-10, -20]"
                      >
                        只有UI团队设计师角色可以上传
                      </fdev-tooltip>
                      <fdev-btn
                        v-if="
                          !newDemandFile &&
                            reviewerObj.nopass &&
                            reviewerObj.nopass.length > 0
                        "
                        outline
                        @click="selBtn(1)"
                        color="primary"
                        class="div-4 ios"
                        label="上传新设计稿"
                        :disable="!isManager"
                      />
                    </div>
                    <div>
                      <fdev-tooltip
                        v-if="(isFinished || !isManager) && newDemandFile"
                        anchor="top middle"
                        self="center middle"
                        :offest="[-10, -20]"
                      >
                        <div v-if="isFinished">
                          审核已通过，无法重新上传
                        </div>
                        <div v-else-if="!isManager">
                          只有UI团队设计师角色可以上传
                        </div>
                      </fdev-tooltip>
                      <fdev-btn
                        :disable="isFinished || !isManager"
                        v-if="newDemandFile"
                        outline
                        @click="selBtn(1)"
                        label="重新上传"
                        class="div-5"
                      ></fdev-btn>
                    </div>
                  </div>
                </div>
                <input
                  @change="selFile(1)"
                  class="none"
                  type="file"
                  value="上传IOS截图"
                  id="demand1"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
      <div>
        <div>
          <f-formitem label="备注" full-width>
            <fdev-input v-model="text" clearable type="textarea"> </fdev-input>
          </f-formitem>
        </div>

        <div class="div5 btn-margin flex justify-center">
          <div>
            <fdev-btn
              :label="
                (reviewerObj.finished && reviewerObj.finished.length > 0) ||
                changeOptVal === 'true'
                  ? '完成'
                  : btnText
              "
              :disable="nextDisable"
              @click="nextStep"
              :loading="loadingBtn"
            />
            <fdev-tooltip v-if="nextDisable">
              {{ getTip }}
            </fdev-tooltip>
          </div>
        </div>
      </div>
    </Loading>
    <Exception
      type="403"
      desc="抱歉，你无权访问该页面"
      backText="返回首页"
      v-if="hasAuthority"
    />
    <!-- 异常关闭弹窗，触发弹框-->
    <f-dialog v-model="showDialog" title="异常关闭">
      <div class="q-pt-none">
        该审核稿状态异常，无法进行修改.
      </div>
      <template v-slot:btnSlot>
        <fdev-btn label="确定" dialog v-close-popup @click="goBack"
      /></template>
    </f-dialog>
    <!-- </template> -->
  </f-block>
</template>

<script>
import { mapState, mapActions, mapGetters } from 'vuex';
import Loading from '@/components/Loading';
import Exception from '@/components/Exception';
import date from '#/utils/date.js';
import {
  errorNotify,
  deepClone,
  getIdsFormList,
  successNotify,
  baseUrl
} from '@/utils/utils';
import axios from 'axios';

export default {
  name: 'DesignReview',
  components: {
    Loading,
    Exception
  },
  data() {
    return {
      loadingBtn: false,
      reviewer: null, //项目进度信息
      id: '',
      users: [],
      hasAuthority: false,
      text: '',
      files: [],
      stepChild: 1,
      reviewerObj: {},
      changeOptVal: null,
      flag: false,
      choose: null,
      demandFile: '',
      newDemandFile: '',
      btnText: '下一步',
      showDialog: false, //是否展示异常关闭弹窗。
      userList: []
    };
  },
  watch: {
    jobProfile: {
      deep: true,
      async handler(val) {
        this.reviewerObj = this.jobProfile.demand.designMap
          ? deepClone(this.jobProfile.demand.designMap)
          : {};
        this.files = val.demand.designDoc || [];
        if (this.reviewerObj) {
          if (this.reviewerObj.nopass && !this.reviewerObj.finished) {
            this.text = this.reviewerObj.nopass.slice(-1)[0].remark;
          } else if (!this.reviewerObj.nopass && this.reviewerObj.fixing) {
            this.text = this.reviewerObj.fixing.slice(-1)[0].remark;
          } else if (!this.reviewerObj.fixing && this.reviewerObj.wait_allot) {
            this.text = this.reviewerObj.wait_allot.slice(-1)[0].remark;
          } else if (
            !this.reviewerObj.wait_allot &&
            this.reviewerObj.uploaded
          ) {
            this.text = this.reviewerObj.uploaded.slice(-1)[0].remark;
          } else if (this.reviewerObj.finished) {
            this.text = this.reviewerObj.finished[0].remark;
          }
        }
        if (
          this.reviewerObj.finished &&
          this.reviewerObj.finished.length != 0
        ) {
          this.stepChild = Object.keys(this.reviewerObj).length + 1;
        } else {
          this.stepChild = Object.keys(this.reviewerObj).length;
        }
        if (this.reviewerObj.nopass && this.reviewerObj.nopass.length > 1) {
          this.btnText = '完成';
        }
      }
    },
    async stepChild(val) {
      if (val === 2) {
        await this.fetchRole({ name: 'UI团队设计师' });
        await this.fetchUser({
          role_id: [this.roles[0].id],
          status: '0'
        });
        this.users = deepClone(this.list);
      }
    },
    files(val) {
      if (val && Array.isArray(val)) {
        this.demandFile = val.find(val => {
          return val.uploadStage === '0' && val.docType === 'demand';
        });
        this.newDemandFile = val.find(val => {
          return val.uploadStage === '1' && val.docType === 'demand';
        });
      }
    }
  },
  computed: {
    ...mapState('demandsForm', ['designState', 'jobProfile']),
    ...mapState('user', ['currentUser', 'list']),
    ...mapGetters('user', ['isKaDianManager']),
    ...mapState('userForm', ['roles']),
    isManager() {
      return (
        (this.jobProfile.reviewer &&
          this.jobProfile.reviewer.id === this.currentUser.id) ||
        this.isKaDianManager
      );
    },
    nextDisable() {
      return (
        !this.demandFile ||
        this.files.some(item => {
          typeof item === 'undefined' || item == null || !item;
        }) ||
        (this.stepChild > 1 && !this.reviewer) ||
        (this.stepChild === 3 && !this.changeOptVal) ||
        (this.stepChild === 4 &&
          this.reviewerObj.nopass &&
          this.reviewerObj.nopass.length === 1 &&
          this.files &&
          this.files.length !== 2) ||
        (this.stepChild === 4 &&
          this.isManager &&
          this.reviewerObj.finished &&
          this.reviewerObj.finished.length > 0) ||
        (this.stepChild >= 5 &&
          this.reviewerObj.finished &&
          this.reviewerObj.finished.length > 0) ||
        (this.stepChild > 2 && !this.isManager)
      );
    },
    getTip() {
      let tip = '';
      if (
        this.stepChild >= 5 &&
        this.reviewerObj.finished &&
        this.reviewerObj.finished.length > 0
      ) {
        tip = '审核已完成';
      }
      if (
        this.stepChild === 4 &&
        this.isManager &&
        this.reviewerObj.finished &&
        this.reviewerObj.finished.length > 0
      ) {
        tip = '审核已通过';
      }
      if (
        this.stepChild === 4 &&
        this.reviewerObj.nopass &&
        this.reviewerObj.nopass.length === 1 &&
        this.files &&
        this.files.length !== 2
      ) {
        tip = '请再次上传设计稿';
      }
      if (this.stepChild === 3 && !this.changeOptVal) {
        tip = '审核未通过';
      }
      if (this.stepChild > 1 && !this.reviewer) {
        tip = '请分配审核人';
      }
      if (
        this.files.some(item => {
          typeof item === 'undefined' || item == null || !item;
        })
      ) {
        tip = '设计稿异常';
      }
      if (!this.demandFile) {
        tip = '请上传设计稿';
      }
      if (this.stepChild > 2 && !this.isManager) {
        tip = '只有卡点管理员/分配审核员才能操作';
      }
      return tip;
    },
    showUserSelect() {
      return (
        (this.stepChild > 1 &&
          this.currentUser.role.some(item => item.name === 'UI团队负责人') &&
          this.demandFile) ||
        this.stepChild > 2
      );
    },
    disableUserSelect() {
      return this.stepChild !== 2;
    },
    disBtn() {
      if (this.reviewerObj.nopass && this.reviewerObj.nopass.length !== 0) {
        return true;
      } else if (
        this.reviewerObj.finished &&
        this.reviewerObj.finished.length > 0
      ) {
        return true;
      }
      return false;
    },
    isFinished() {
      if (this.reviewerObj.finished && this.reviewerObj.finished.length > 0) {
        return true;
      } else {
        return false;
      }
    }
  },
  methods: {
    ...mapActions('demandsForm', {
      updateDesignStage: 'updateDesignStage',
      getDesignInfo: 'getDesignInfo'
    }),
    ...mapActions('jobForm', ['downExcel']),
    ...mapActions('user', {
      fetchUser: 'fetch'
    }),
    ...mapActions('userForm', {
      fetchRole: 'fetchRole'
    }),
    async download({ minioPath }) {
      let param = {
        path: minioPath,
        moduleName: 'fdev-design'
      };
      await this.downExcel(param);
    },
    async init() {
      this.id = this.$route.params.id;
      await this.getDesignInfo({ demand_id: this.id });
      //获取设计稿状态
      if (this.jobProfile.demand.design_status === 'abnormalShutdown') {
        //弹出弹框
        this.showDialog = true;
      }

      this.reviewer = this.jobProfile.reviewer
        ? this.jobProfile.reviewer.user_name_cn
        : '';
      this.jobProfile.reviewer = this.jobProfile.reviewer
        ? this.jobProfile.reviewer
        : {};
      if (this.reviewerObj.finished && this.demandFile === {}) {
        this.demandFile = this.files[0] ? this.files[0] : {};
      }
      this.userList = deepClone(this.list);
    },
    async nextStep() {
      this.loadingBtn = true;
      if (this.stepChild === 1) {
        await this.updateDesignState('auditWait');
      } else if (this.stepChild === 2) {
        await this.updateDesignState('auditIn');
      } else if (this.stepChild === 3) {
        if (this.changeOptVal === 'true') {
          await this.updateDesignState('completedAudit');
        } else if (this.changeOptVal === 'false') {
          await this.updateDesignState('auditPassNot');
        }
      } else if (
        this.stepChild === 4 &&
        this.files.length === 2 &&
        this.reviewerObj.nopass &&
        this.reviewerObj.nopass.length === 1
      ) {
        await this.updateDesignState('auditPassNot');
      } else {
        await this.updateDesignState('completedAudit');
      }
      this.loadingBtn = false;
    },
    async updateDesignState(state, fileId) {
      let params = {
        demand_id: this.id,
        newStatus: state,
        remark: this.text,
        name: this.currentUser.name,
        time: date.formatDate(Date.now(), 'YYYY-MM-DD HH:mm:ss')
      };
      if (state === 'auditIn') {
        params = { ...params, uiDesignReporter: this.reviewer.id };
      } else if (state === 'auditPassNot') {
        if (this.files && this.files.length === 1) {
          this.flag = false;
        } else {
          this.flag = true;
        }
        params = {
          ...params,
          stage: this.flag ? 'load_upload' : 'load_nopass'
        };
      }
      await this.updateDesignStage(params);
      await this.getDesignInfo({ demand_id: this.id });
    },
    userFilter(val, update, abort) {
      update(() => {
        if (
          this.list &&
          Array.isArray(this.list) &&
          this.list.length > 0 &&
          val
        ) {
          this.users = this.userList.filter(user => {
            return (
              user.user_name_cn.indexOf(val) > -1 ||
              (user.user_name_en &&
                user.user_name_en.toLowerCase().includes(val.toLowerCase()))
            );
          });
        } else {
          this.users = this.list;
        }
      });
    },
    selBtn(num) {
      let file0;
      file0 = document.querySelector(`#demand${num}`);
      file0.value = '';
      file0.click();
    },
    async selFile(nums) {
      let file;
      file = document.querySelector(`#demand${nums}`).files[0];
      file = file ? file : { size: '', name: '' };
      let length = file.name.split('.').length;
      let finalName = file.name.split('.')[length - 1];
      if (finalName !== 'zip') {
        errorNotify('上传的文件只能为压缩文件zip格式!');
        return;
      }
      let uploadParam = new FormData();
      uploadParam.append('fileType', 'demand');
      uploadParam.append('demand_id', this.id);
      uploadParam.append('fileName', file.name);
      uploadParam.append('uploadStage', nums);
      uploadParam.append('remark', this.text);
      uploadParam.append('file', file);
      let config = {
        headers: {
          'Content-Type': 'multipart/form-data',
          Accept: 'application/json',
          Authorization: this.$q.localStorage.getItem('fdev-vue-admin-jwt')
        }
      };
      axios
        .post(
          `${baseUrl}fdemand/api/design/uploadDesignDoc`,
          uploadParam,
          config
        )
        .then(async res => {
          if (res.data.code !== 'AAAAAAA') {
            errorNotify('上传失败，请重试!');
            return;
          }
          await this.getDesignInfo({ demand_id: this.id });
          this.files = res.data.data.designDoc || [];
          successNotify('上传成功！');
        })
        .catch(err => {
          errorNotify('上传失败，请重试!');
        });
    },
    changeOpt(val) {
      this.changeOptVal = val;
    },
    isRelevant() {
      let role = getIdsFormList([this.jobProfile.reviewer]);
      if (
        role.indexOf(this.currentUser.id) !== -1 ||
        this.currentUser.role.some(item => item.name === 'UI团队负责人') ||
        this.isKaDianManager
      ) {
        return true;
      }
      return false;
    }
  },
  async created() {
    await this.init();
  }
};
</script>

<style lang="stylus" scoped>
.card-style{
   width:300px
}
.btn-margin
  margin-top 40px

.q-stepper--vertical .q-stepper__step
  min-height 130px

.upload-align >>> .q-uploader__file-header-content .q-uploader__title
  text-align left

.div1
  display flex
  border-style solid
  width calc(100% + 64px)
  margin-left -32px
  border-width 10px 0
  border-color rgb(245,247,253)
  height 60px
  align-items center
  padding 0 5% 0 32px
.div2
  flex 1
.page-header
  padding 0
.div3
  display flex
.div4
  flex 2.5
.span2
  font-size 16px
  color #000
.div5
  text-align center
  margin-bottom 50px
.div6
  padding 16px 0
  margin-top 10px
.step
  min-height 130px
.check
  background-color: var(--q-color-primary) !important;
  color: #fff !important;
.name
  padding 32px 0 6px
.time
  color: #9e9e9e;
  font-size: .8em;
div >>>.q-stepper__step-inner
  text-align: left !important;
  color: #9e9e9e !important;
.a-link
  float: right;
  margin-right: 10px;
.bottomDiv
  padding-bottom: 5px;
.leftDiv
  flex 2.5
.div-1
  width: 100%
.div-2
  display: flex;
  flex-direction: column;
  width: 100%;
.div-3
  flex:2;
  display:flex;
.div-4
  margin-bottom:10px;
.div-5
  margin-bottom:10px;
  flex:1;
.div-6
  display: flex;
  padding-top: 10px;
.div-7
  flex:2;
.div-8
  flex:1;
.none
  display: none;
.div-9
  display: flex;
  flex-direction: column;
  width: 150px;
.div-10
  margin-bottom:5px;
a {
  word-break: break-all;
  -webkit-line-clamp: 2;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  display: -webkit-box !important;
  overflow: hidden;
  line-height: 25px;
  max-height: 50px;
}
.rightItem {
  margin-left: -20px;
  flex: 3;
}
.mainDiv
  padding-right 5%
.leftItem
  flex 3
.wd200
  width 200px
</style>
