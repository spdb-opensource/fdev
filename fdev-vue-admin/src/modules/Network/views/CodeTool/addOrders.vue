<template>
  <f-block page>
    <Loading class="orderPage" :visible="loading">
      <div class="tileName">新建工单</div>
      <div class="row justify-between q-ml-lg mr-102">
        <f-formitem
          label="对应需求"
          bottom-page
          required
          label-style="width:86px"
        >
          <fdev-select
            use-input
            ref="createModel.demandId"
            v-model="$v.createModel.demandId.$model"
            :options="demandOptions"
            @filter="rqrmntInputFilter"
            @input="demandInput"
            option-label="oa_contact_name"
            option-value="oa_contact_no"
            :rules="[() => $v.createModel.demandId.required || '请选择需求']"
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
        <f-formitem
          label="牵头人"
          bottom-page
          required
          label-style="width:86px"
        >
          <fdev-select
            use-input
            ref="createModel.leader"
            v-model="$v.createModel.leader.$model"
            :options="userOptions"
            @filter="userFilter"
            :rules="[() => $v.createModel.leader.required || '请选择牵头人员']"
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
          label="计划投产日期"
          bottom-page
          required
          label-style="width:86px"
        >
          <f-date
            ref="createModel.planProductDate"
            mask="YYYY-MM-DD"
            v-model="$v.createModel.planProductDate.$model"
            :rules="[
              () =>
                $v.createModel.planProductDate.required || '请选择计划投产日期'
            ]"
          />
        </f-formitem>
        <f-formitem label="期望审核日期" bottom-page label-style="width:86px">
          <f-date
            mask="YYYY-MM-DD"
            hint=""
            v-model="createModel.expectAuditDate"
            :options="expectAuditDateOptions"
          />
        </f-formitem>
      </div>
      <div class="row q-ml-lg">
        <f-formitem
          label="审核内容"
          bottom-page
          required
          label-style="width:86px"
          value-style="width:792px"
        >
          <fdev-input
            ref="createModel.auditContent"
            type="textarea"
            v-model="$v.createModel.auditContent.$model"
            :rules="[
              () => $v.createModel.auditContent.required || '请输入审核内容',
              () =>
                $v.createModel.auditContent.maxLen || '输入内容不能超过400字'
            ]"
          />
        </f-formitem>
      </div>
      <div :class="[taskLists.length == 0 ? 'mb-16' : '', 'row q-ml-lg']">
        <f-formitem
          label="涉及任务"
          bottom-page
          required
          label-style="width:77px"
          value-style="width:800px"
        >
          <div
            :class="[index > 0 ? 'mt-16' : '', 'row taskBox ellipsis']"
            v-for="(item, index) in taskLists"
            :key="item.id"
            :title="item.name"
          >
            <fdev-checkbox
              class="itemleft"
              v-model="item.isTask"
              @input="changeSel()"
            />
            <div class="itemright ellipsis">{{ item.name }}</div>
          </div>
          <fdev-input
            class="hideInput margTask"
            v-model="$v.createModel.taskIds.$model"
            ref="createModel.taskIds"
            :rules="[() => $v.createModel.taskIds.IsSel || '请选择涉及任务']"
          />
        </f-formitem>
      </div>
      <div class="row justify-between q-ml-lg mr-88">
        <f-formitem
          label="代码审核表"
          bottom-page
          required
          label-style="width:86px"
        >
          <!-- <div style="display: flex;"> -->
          <fdev-btn
            dash
            ficon="upload"
            :label="createModel.code_files.length > 0 ? '继续选择' : '选择文件'"
            @click="openFiles('1')"
          />
          <span class="q-ml-sm">
            <span
              class="text-grey-7"
              v-show="createModel.code_files.length === 0"
            >
              暂未选择文件
            </span>
            <div
              v-for="file in createModel.code_files"
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
                  @click="deleteFiles(file)"
                />
              </div>
            </div>
          </span>
          <!-- </div> -->
          <fdev-input
            class="hideInput"
            v-model="$v.createModel.code_files.$model"
            ref="createModel.code_files"
            :rules="[() => $v.createModel.code_files.IsLen || '请选择文件']"
          />
        </f-formitem>
        <f-formitem
          label="需求规格说明书"
          bottom-page
          required
          label-style="width:100px"
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
      </div>
      <div class="row justify-between q-ml-lg mr-88">
        <f-formitem label="原型图" bottom-page label-style="width:86px">
          <fdev-btn
            dash
            ficon="upload"
            :label="
              createModel.prototype_files.length > 0 ? '继续选择' : '选择文件'
            "
            @click="openFiles('3')"
          />
          <span class="q-ml-sm">
            <span
              class="text-grey-7"
              v-show="createModel.prototype_files.length === 0"
            >
              暂未选择文件
            </span>
            <div v-for="file in createModel.prototype_files" :key="file.name">
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
            hint=""
            v-model="createModel.prototype_files"
          />
        </f-formitem>
        <f-formitem label="需求说明书" bottom-page label-style="width:100px">
          <fdev-btn
            dash
            ficon="upload"
            :label="
              createModel.deInstruction_files.length > 0
                ? '继续选择'
                : '选择文件'
            "
            @click="openFiles('4')"
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
            hint=""
            v-model="createModel.deInstruction_files"
          />
        </f-formitem>
      </div>
      <div class="row q-ml-lg">
        <!-- 邮件通知人必填 -->
        <f-formitem
          label="邮件通知人"
          bottom-page
          required
          label-style="width:100px"
          value-style="width:792px"
          help="新建工单时邮件通知人将会收到邮件通知"
        >
          <fdev-select
            multiple
            use-input
            ref="createModel.emailTo"
            v-model="$v.createModel.emailTo.$model"
            :options="emailUsersOptions"
            @filter="emailUsersFilter"
            :rules="[
              () => $v.createModel.emailTo.required || '请选择邮件通知人'
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
import { mapState, mapActions } from 'vuex';
import {
  resolveResponseError,
  formatOption,
  // validate,
  successNotify,
  errorNotify
} from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import {
  createOrdersModel,
  queryUserOptionsParams
} from '@/modules/Network/utils/constants';
import {
  queryProblemItem,
  addOrders
} from '@/modules/Network/services/methods';
import { queryUserCoreData } from '@/modules/Rqr/services/methods';
import { queryTaskByDemandId } from '@/services/job';
import moment from 'moment';

export default {
  name: 'addOrders',
  components: { Loading },
  data() {
    return {
      loading: false,
      createModel: createOrdersModel(),
      users: [], //牵头人员
      userOptions: [], //牵头人选项
      emailUsersOptions: [], //邮件通知人
      demandOptions: [],
      taskLists: [],
      codeFileName: '', //上传代码名
      reqFileName: '', //请求文档名
      mapProItem: {} //文件类型
    };
  },
  validations: {
    createModel: {
      demandId: {
        required
      },
      leader: {
        required
      },
      planProductDate: {
        required
      },
      emailTo: {
        required
      },
      taskIds: {
        IsSel(val) {
          for (let i = 0; i < this.createModel.taskIds.length; i++) {
            if (this.createModel.taskIds[i].isTask) {
              return true;
            }
          }
          return false;
        }
      },
      code_files: {
        IsLen(val) {
          return this.createModel.code_files.length > 0;
        }
      },
      req_files: {
        IsLen(val) {
          return this.createModel.req_files.length > 0;
        }
      },
      auditContent: {
        required,
        maxLen(val) {
          return val.length <= 400;
        }
      }
    }
  },
  props: {
    value: {
      type: Boolean
    }
  },
  watch: {},
  computed: {
    ...mapState('jobForm', ['rqrmntsList'])
  },
  methods: {
    ...mapActions('jobForm', ['queryRqrmnts']),
    expectAuditDateOptions(date) {
      return (
        date >=
        moment(new Date())
          .format('YYYY-MM-DD')
          .replace(/-/g, '/')
      );
    },
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
    async getTaskLists(demandId) {
      //新增和编辑工单时，根据需求选任务的时候，过滤一下，把无代码任务和日常任务过滤掉。
      //用taskType过滤，taskType=1或2的任务过滤掉
      let params = { demandId };
      let res = await queryTaskByDemandId(params);
      if (res) {
        this.taskLists = res.filter(item => {
          item.isTask = false;
          return item;
          //放开无代码任务和日常任务过滤掉。
          //return item.taskType != '1' && item.taskType != '2';
        });
      }
    },
    demandInput($event) {
      if (!$event) return;
      if ($event.demand_leader && $event.demand_leader.length > 0) {
        let userId = $event.demand_leader[0];
        for (let i = 0; i < this.users.length; i++) {
          if (userId === this.users[i].id) {
            let uObj = this.users[i];
            uObj.label = uObj.user_name_cn;
            uObj.value = uObj.id;
            this.createModel.leader = uObj;
            break;
          }
        }
      }
      this.getTaskLists($event.id);
    },
    async emailUsersFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.emailUsersOptions = this.users.filter(
          v =>
            v.user_name_en.indexOf(val) > -1 ||
            v.user_name_cn.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    async userFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.userOptions = this.users.filter(
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
          this.saveOrders();
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
    // 新增
    async saveOrders() {
      let formData = new FormData();
      formData.append('demandId', this.createModel.demandId.id); //对应需求
      formData.append('leader', this.createModel.leader.value); //牵头人
      formData.append('planProductDate', this.createModel.planProductDate); //预计投产日期
      formData.append('auditContent', this.createModel.auditContent); //审核内容
      formData.append('expectAuditDate', this.createModel.expectAuditDate); //审核内容
      //涉及任务
      let taskIds = [];
      for (let i = 0; i < this.createModel.taskIds.length; i++) {
        taskIds.push(this.createModel.taskIds[i].id);
      }
      formData.append('taskIds', taskIds);
      //邮件通知人
      if (this.createModel.emailTo) {
        let emailTo = [];
        for (let i = 0; i < this.createModel.emailTo.length; i++) {
          emailTo.push(this.createModel.emailTo[i].value);
        }
        formData.append('emailTo', emailTo);
      }
      //需求规格说明书
      this.createModel.req_files.forEach(file => {
        formData.append('requirementSpecification', file, file.name);
      });

      //代码审核表
      this.createModel.code_files.forEach(file => {
        formData.append('codeReviewTable', file, file.name);
      });

      //原型图
      this.createModel.prototype_files.forEach(file => {
        formData.append('prototypeFigure', file, file.name);
      });
      //需求说明书
      this.createModel.deInstruction_files.forEach(file => {
        formData.append('demandInstructionBook', file, file.name);
      });
      try {
        this.loading = true;
        await resolveResponseError(() => addOrders(formData));
        successNotify('新增成功！');
        this.$router.push('/aAndA/codeTool');
      } catch (e) {
        this.loading = false;
      }
    },
    confirmToCancle() {
      this.$router.push('/aAndA/codeTool');
    },
    async getUsers() {
      let params = {
        status: queryUserOptionsParams.status
      };
      const res = await queryUserCoreData(params);
      this.users = res.map(user => formatOption(user, 'user_name_cn'));
    },
    async getproItemOptions() {
      let res = await queryProblemItem();
      if (res) {
        let self = this;
        res.forEach(element => {
          self.mapProItem[element.key] = element.id;
        });
      }
    },
    async init() {
      this.loading = true;
      this.createModel = createOrdersModel();
      // 需求列表
      await this.queryRqrmnts();
      //撤销和暂缓的都去除掉
      this.demandOptions = this.rqrmntsList.filter(item => {
        return (
          item.demand_status_special !== 1 && item.demand_status_normal !== 9
        );
      });
      await this.getUsers();
      this.getproItemOptions();
      this.loading = false;
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
      function push_file(flist, mFiles) {
        const modelFiles = [...mFiles];
        flist.forEach(file => {
          const notExist = mFiles.every(item => item.name !== file.name);
          let fileName = file.name;
          let fileSize = file.size;
          if (fileSize > 0 && fileSize <= 50 * 1024 * 1024) {
            if (notExist) {
              modelFiles.push(file);
            }
          } else {
            errorNotify(`上传的${fileName}文件大小不能大于50MB,请重新上传`);
          }
        });
        return modelFiles;
      }
      files = Array.from(files);
      if (type === '1') {
        this.createModel.code_files = push_file(
          files,
          this.createModel.code_files
        );
      } else if (type === '2') {
        this.createModel.req_files = push_file(
          files,
          this.createModel.req_files
        );
      } else if (type === '3') {
        this.createModel.prototype_files = push_file(
          files,
          this.createModel.prototype_files
        );
      } else if (type === '4') {
        this.createModel.deInstruction_files = push_file(
          files,
          this.createModel.deInstruction_files
        );
      }
    },
    deleteFiles(file) {
      this.createModel.code_files = this.createModel.code_files.filter(
        item => item.name !== file.name
      );
    },
    deleteReq_Files(file) {
      this.createModel.req_files = this.createModel.req_files.filter(
        item => item.name !== file.name
      );
    },
    deleteProFiles(file) {
      this.createModel.prototype_files = this.createModel.prototype_files.filter(
        item => item.name !== file.name
      );
    },
    deleteInstFiles(file) {
      this.createModel.deInstruction_files = this.createModel.deInstruction_files.filter(
        item => item.name !== file.name
      );
    },
    changeSel() {
      this.createModel.taskIds = [];
      for (let i = 0; i < this.taskLists.length; i++) {
        if (this.taskLists[i].isTask === true) {
          this.createModel.taskIds.push(this.taskLists[i]);
        }
      }
    }
  },
  created() {
    this.createModel.planProductDate = moment(new Date()).format('YYYY-MM-DD');
    this.init();
  },
  mounted() {}
};
</script>

<style lang="stylus" scoped>
.orderPage{
  padding-top: 10px ;
  padding-bottom: 121px;
}
.tileName{
  font-family: PingFangSC-Semibold;
  font-size: 18px;
  color: #333333;
  letter-spacing: 0;
  line-height: 18px;
  font-weight: 600;
  margin-bottom: 30px;
}
.mr-102{
  margin-right: 102px;
}
.mr-88{
  margin-right: 88px;
}
.mt-16{
  margin-top: 16px;
}
.mb-16{
  margin-bottom: 16px;
}
.op-btn{
  margin-top: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.taskBox{
  .itemleft{
    width: 40px;
  }
  .itemright{
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
>>> .hideInput .q-field__control.relative-position.row.no-wrap.text-negative,
>>> .hideInput input,
>>> .hideInput .q-field__control,
>>> .hideInput .q-field__control:before,
>>> .hideInput .q-field__control:after {
  display: none;
  content: none;
  height: 0px;
}
>>> .margTask .q-field__bottom.row.items-start.q-field__bottom--animated{
  padding-left: 9px;
  padding-top: 6px;
}
</style>
