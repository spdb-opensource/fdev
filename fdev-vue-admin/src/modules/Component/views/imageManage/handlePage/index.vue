<template>
  <f-block>
    <Loading :visible="loading">
      <div class="q-mt-md row justify-end">
        <fdev-btn
          normal
          label="废弃"
          ficon="delete"
          @click="deleteImageManage"
          v-if="imageIssueDetail.stage < 3"
        ></fdev-btn>
      </div>
      <div class="row justify-center q-mt-md">
        <f-formitem class="col-4" label="需求标题：">
          {{ imageIssueDetail.title }}
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ imageIssueDetail.title }}
            </fdev-banner>
          </fdev-popup-proxy>
        </f-formitem>
        <f-formitem class="col-4" label="开发人员：">
          <router-link
            :to="{ path: `/user/list/${imageIssueDetail.assignee}` }"
            class="link"
            v-if="imageIssueDetail.assignee"
          >
            <span>{{ imageIssueDetail.name_cn }}</span>
          </router-link>
        </f-formitem>
        <f-formitem class="col-4" label="开发分支：">
          {{ imageIssueDetail.branch }}
        </f-formitem>
        <f-formitem class="col-4" label="创建时间：">
          {{ imageIssueDetail.create_date }}
        </f-formitem>
        <f-formitem class="col-4" label="需求描述：">
          <span v-html="desc" />
        </f-formitem>
        <f-formitem class="col-4" label="计划完成日期：">
          {{ imageIssueDetail.due_date }}
        </f-formitem>
      </div>

      <!-- 正式发布（stage = 4）后，不能回退 -->
      <fdev-stepper
        v-model="step"
        ref="stepper"
        animated
        flat
        :header-nav="step !== 3"
        @input="findHistory"
      >
        <fdev-step
          :name="0"
          title="新增优化需求"
          icon="settings"
          :done="step > 0"
        />

        <fdev-step
          :name="1"
          title="开发测试"
          icon="settings"
          :done="step > 1"
          :header-nav="Number(imageIssueDetail.stage) > 0"
        />

        <fdev-step
          :name="2"
          title="试用发布"
          icon="setting"
          :done="step > 2"
          :header-nav="Number(imageIssueDetail.stage) > 1"
        />

        <fdev-step
          :name="3"
          title="正式发布（推广）"
          icon="setting"
          :done="step > 3"
          :header-nav="Number(imageIssueDetail.stage) > 2"
        />
        <template> </template>
      </fdev-stepper>

      <!-- 正式发布后(4),隐藏表单按钮，只显示记录 -->
      <div class="q-pb-md">
        <!-- 开发中(0)，隐藏表单，只显示’下一步‘按钮 -->
        <div v-show="step === 2" class="row">
          <f-formitem
            v-for="(item, index) in $v.metaDataList.$each.$iter"
            :key="index"
            :label="item.key.$model"
            required
            class="col-4"
            bottomPage
          >
            <fdev-input
              :ref="`trialPublishModel.meta_data-${index}`"
              :rules="[() => item.value.required || '请输入' + item.key.$model]"
              v-model="item.value.$model"
            />
          </f-formitem>

          <!-- 试用应用列表 -->
          <f-formitem label="试用应用列表" class="col-4" bottomPage>
            <fdev-select
              multiple
              use-input
              emit-value
              map-options
              :option-value="opt => opt"
              :options="filterProject"
              option-label="name_zh"
              ref="trialPublishModel.trial_apps"
              @filter="projectFilter"
              v-model="$v.trialPublishModel.trial_apps.$model"
              hint=""
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name_zh">
                      {{ scope.opt.name_zh }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.name_en">
                      {{ scope.opt.name_en }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>

          <!-- 发布日志 -->
          <f-formitem full-width label="发布日志" required>
            <fdev-input
              type="textarea"
              ref="trialPublishModel.release_log"
              v-model="$v.trialPublishModel.release_log.$model"
              :rules="[
                () =>
                  $v.trialPublishModel.release_log.required || '请输入发布日志'
              ]"
            />
          </f-formitem>
        </div>

        <div class="btn text-center q-mt-md">
          <!-- 当前组件管理员||当前需求开发人员，并且不处于开发（0）阶段 -->
          <div class="inline-block">
            <fdev-btn
              v-if="step === 2"
              label="试用发布"
              @click="onTrialPubilsh"
              :loading="globalLoading[`componentForm/pubilsh`]"
            />
          </div>
          <!-- 当前组件管理员，并且不处于正式发布（3） -->
          <div class="q-ml-md inline-block" v-if="step < 2">
            <!-- 历史记录为空时，并且不处于开发阶段（0），不能点击 -->

            <fdev-tooltip
              v-if="
                !queryImageIssueRecordList ||
                  (step !== 0 && queryImageIssueRecordList.length === 0)
              "
            >
              至少有一条历史版本记录
            </fdev-tooltip>

            <fdev-btn
              :disable="
                !queryImageIssueRecordList ||
                  (step !== 0 && queryImageIssueRecordList.length === 0)
              "
              :loading="globalLoading[`componentForm/${changeImageStage}`]"
              label="下一阶段"
              @click="next"
            />
          </div>
        </div>
      </div>
      <!-- 开发阶段（0）不展示 -->
      <fdev-table
        :data="queryImageIssueRecordList"
        v-if="step > 0"
        title="历史版本"
        :columns="columns"
        :pagination.sync="pagination"
        title-icon="list_s_f"
        no-export
        no-select-cols
      >
        <!-- 更新人员列 -->
        <template v-slot:body-cell-update_user="props">
          <fdev-td>
            <router-link
              :to="`/user/list/${props.row.update_user}`"
              class="link"
              v-if="props.row.update_user"
            >
              <span :title="props.row.name_cn">
                {{ props.row.name_cn }}
              </span>
            </router-link>
            <span v-else :title="props.row.name_cn">
              {{ props.row.name_cn }}
            </span>
          </fdev-td>
        </template>

        <!-- 版本列 -->
        <template v-slot:body-cell-image_tag="props">
          <fdev-td class="text-ellipsis">
            <fdev-tooltip>
              <span v-html="logFilter(props.value)">
                {{ props.row.image_tag }}
              </span>
            </fdev-tooltip>
            {{ props.row.image_tag }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.image_tag }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>

        <!-- 数据更新时间列 -->
        <template v-slot:body-cell-update_time="props">
          <fdev-td class="text-ellipsis">
            <fdev-tooltip>
              <span v-html="logFilter(props.value)">
                {{ props.row.update_time }}
              </span>
            </fdev-tooltip>
            {{ props.row.update_time }}
          </fdev-td>
        </template>

        <!-- 发布日志列 -->
        <template v-slot:body-cell-release_log="props">
          <fdev-td class="text-ellipsis">
            <fdev-tooltip v-if="props.queryImageIssueRecordList">
              <span v-html="logFilter(props.value)">
                {{ props.row.release_log }}
              </span>
            </fdev-tooltip>
            {{ props.row.release_log }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.release_log }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>

        <template v-slot:body-cell-pubilsh="props">
          <fdev-td>
            <fdev-btn
              flat
              v-show="queryImageIssueRecordList.indexOf(props.row) === 0"
              label="正式发布"
              @click="relase"
            />
          </fdev-td>
        </template>
      </fdev-table>
      <!-- 废弃镜像优化需求 -->
      <DestroyDialog
        v-model="imageDialogOpen"
        :issueBbranch="imageIssueDetail.branch"
        :issueStage="stageImage"
        :page="page"
        @deleteIssue="handleDelete"
      />
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify } from '@/utils/utils';
import {
  trialPublishModel,
  imageStageVersion,
  imageManageHandlePageColumns
} from '@/modules/Component/utils/constants.js';
import DestroyDialog from '@/modules/Component/views/serverComponent/components/destroyOptimize';
export default {
  name: 'HandlePage',
  components: { Loading, DestroyDialog },
  data() {
    return {
      filterProject: [],
      step: 0,
      loading: false,
      id: '',
      trialPublishModel: trialPublishModel(),
      columns: imageManageHandlePageColumns,
      pagination: {
        rowsPerPage: 0
      },
      metaDataList: [],
      deleteImageOpened: false,
      stageImage: '',
      imageDialogOpen: false,
      page: ''
    };
  },
  validations: {
    trialPublishModel: {
      release_log: {
        required
      },
      trial_apps: {},
      meta_data: {}
    },

    metaDataList: {
      $each: {
        key: {
          required
        },
        value: {
          required(val, item) {
            return !!val.trim();
          }
        }
      }
    }
  },

  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('componentForm', [
      'imageIssueDetail',
      'queryImageIssueRecordList',
      'baseImageDetail',
      'queryMetaDataRes'
    ]),
    ...mapState('appForm', ['vueAppData']),

    ...mapState('user', ['currentUser']),
    // 当前组件的管理员
    isManger() {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      const isManager = this.imageIssueDetail.manager
        ? this.imageIssueDetail.manager.some(
            user => user.id === this.currentUser.id
          )
        : false;
      return isManager || haveRole;
    },
    // 当前组件的开发人员
    desc() {
      const desc = this.imageIssueDetail.desc ? this.imageIssueDetail.desc : '';
      return desc
        .replace(/</g, '&lt;')
        .replace(/</g, '&gt;')
        .replace(/\n/g, '<br/>');
    }
  },
  methods: {
    ...mapActions('componentForm', [
      'queryBaseImageIssueDetail',
      'queryImageIssueRecord',
      'changeImageStage',
      'packageTag',
      'relasePackage',
      'queryMetaData',
      'destroyBaseImageIssue'
    ]),
    ...mapActions('appForm', ['queryApps']),
    async relase() {
      await this.relasePackage({
        name: this.imageIssueDetail.name,
        branch: this.imageIssueDetail.branch
      });
      await this.init();
    },
    projectFilter(val, update) {
      update(() => {
        this.filterProject = this.vueAppData.filter(
          tag => tag.name_zh.indexOf(val) > -1 || tag.name_en.indexOf(val) > -1
        );
      });
    },
    deleteImageManage() {
      this.imageDialogOpen = true;
      this.page = this.$route.path;
      if (this.imageIssueDetail.stage === '0')
        this.stageImage = '新增阶段 (create)';
      if (this.imageIssueDetail.stage === '1')
        this.stageImage = '开发阶段 (DEV)';
      if (this.imageIssueDetail.stage === '2')
        this.stageImage = '试用阶段 (trial)';
      this.deleteImageOpened = true;
    },
    async handleDelete() {
      await this.destroyBaseImageIssue({
        id: this.id
      });
      successNotify('废弃成功！');
      window.history.back();
    },
    async init() {
      this.loading = true;
      await this.queryApps();
      await this.queryBaseImageIssueDetail({
        id: this.id
      });
      this.metaDataList = [];
      await this.queryMetaData({
        name: this.imageIssueDetail.name
      });
      for (const key in this.queryMetaDataRes) {
        this.metaDataList.push({
          key: this.queryMetaDataRes[key],
          value: '',
          key_en: key
        });
      }
      this.filterProject = this.vueAppData;
      // 进入页面时，设置step
      this.step = Number(this.imageIssueDetail.stage);
      // 处于’开发中‘不查询历史记录
      if (this.imageIssueDetail.stage === '0') {
        this.loading = false;
        return;
      }
      this.findHistory();
      this.loading = false;
    },
    async onTrialPubilsh(isOk) {
      this.$v.trialPublishModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('trialPublishModel') > -1;
      });

      validate(
        Keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.trialPublishModel.$invalid || this.$v.metaDataList.$invalid) {
        return;
      }
      let gitlabIdArr = this.trialPublishModel.trial_apps.map(res => {
        return res.gitlab_project_id;
      });
      // /* 试用发布 */
      let gitlabIdObj =
        gitlabIdArr.length > 0 ? { trial_apps: [...gitlabIdArr] } : {};
      let metaDataObj = {};
      this.metaDataList.map(res => {
        metaDataObj[res.key_en] = res.value;
      });
      let params = {
        name: this.imageIssueDetail.name,
        meta_data: {
          ...metaDataObj
        },
        ...gitlabIdObj,
        update_user: this.currentUser.id,
        branch: this.imageIssueDetail.branch,
        stage: this.step.toString(),
        release_log: this.trialPublishModel.release_log
      };
      await this.packageTag(params);
      successNotify('已发起分支合并请求，请联系镜像管理员进行分支合并!');
    },
    // 下一步按钮
    async next() {
      // 如果回退了，不发接口更改状态，但要查回退stage的历史记录
      if (this.step.toString() !== this.imageIssueDetail.stage) {
        this.step = this.step + 1;
        this.findHistory();
        return;
      }
      await this.changeImageStage({
        stage: this.step.toString(),
        id: this.imageIssueDetail.id
      });
      await this.init();
    },
    logFilter(val) {
      val = val ? val : '';
      return val.replace(/<\/?[^>]*/g, '').replace(/\n/g, '<br/>');
    },
    async findHistory() {
      if (this.step === 0) {
        return;
      }
      // // 已完成（3）查不到历史记录，要改为2
      this.queryImageIssueRecord({
        name: this.imageIssueDetail.name,
        stage: imageStageVersion[this.step.toString()],
        branch: this.imageIssueDetail.branch
      });
    }
  },

  watch: {
    async step(newVal) {
      const publishCol = this.columns.find(item => item.name === 'pubilsh');
      if (newVal === 2 && !publishCol) {
        this.columns.push({
          name: 'pubilsh',
          label: '操作',
          field: 'pubilsh',
          align: 'left'
        });
      } else {
        this.columns = imageManageHandlePageColumns;
      }
    }
  },
  async created() {
    this.id = this.$route.params.id;

    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.q-stepper >>> .q-stepper__content
  display none!important
</style>
