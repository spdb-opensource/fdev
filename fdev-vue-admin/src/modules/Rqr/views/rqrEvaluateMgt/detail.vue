<template>
  <div>
    <!-- head -->
    <div class="rqrHeader">
      <div
        class="rqrBack"
        :style="{
          backgroundImage: getPrioritySrc(demandInfoDetail.demand_status)
        }"
      >
        <div class="rqrleft">
          <div>
            <div class="maxWidth ellipsis-2-lines">
              <span class="rqrName">
                需求名称：
                <span class="rqrName" :title="demandInfoDetail.oa_contact_name"
                  >{{ demandInfoDetail.oa_contact_name }}
                  <fdev-popup-proxy context-menu>
                    <fdev-banner style="max-width:300px">
                      {{ demandInfoDetail.oa_contact_name }}
                    </fdev-banner>
                  </fdev-popup-proxy>
                </span>
              </span>
            </div>
            <div class="maxWidth ellipsis-2-lines">
              <span class="rqrNum row items-center">
                <span>需求编号：</span>
                <span class="rqrNum " :title="demandInfoDetail.oa_contact_no">{{
                  demandInfoDetail.oa_contact_no
                }}</span>
              </span>
            </div>
          </div>
        </div>
        <div class="rqrRight">
          <img
            class="statusLogo"
            v-if="demandInfoDetail.demand_status"
            :src="getStatusSrc(demandInfoDetail.demand_status)"
            alt=""
          />
          <div class="">
            <div class="demand-status-normal">
              {{ filterdemandStatus(demandInfoDetail.demand_status) }}
            </div>
            <span class="demand-status-normal-caption">分析状态</span>
          </div>
          <div class="statusType">
            <div class="up">
              {{ filterDemandType(demandInfoDetail.demand_type) }}
            </div>
            <span class="down">需求类型</span>
          </div>
          <f-image
            v-if="demandInfoDetail.priority == '0'"
            name="priority_high_font"
            class="statusPriorityImg"
          />
          <f-image
            v-else-if="demandInfoDetail.priority == '1'"
            name="priority_medium_font"
            class="statusPriorityImg"
          />
          <f-image
            v-else-if="demandInfoDetail.priority == '2'"
            name="priority_normal_font"
            class="statusPriorityImg"
          />
          <f-image
            v-else-if="demandInfoDetail.priority == '3'"
            name="priority_low_font"
            class="statusPriorityImg"
          />
        </div>
      </div>
    </div>
    <!-- 基础信息 -->
    <f-block class="detail-middle">
      <div style="position:relative">
        <div class="row justify-between items-center">
          <!-- 标题 -->
          <div class="row justify-between items-center line-title">
            <f-icon name="basic_msg_s_f" class="titleimg" />
            <span class="titlename">基础信息</span>
          </div>
          <fdev-space />
          <!-- 操作按钮 -->
          <div class="row">
            <!-- 编辑、撤销，
          已完成评估需求不可编辑、撤销。操作权限为需求管理员、需求牵头人。 -->
            <!-- 编辑 -->
            <div v-if="demandInfoDetail.operate_flag == 'show'">
              <fdev-btn dialog ficon="edit" label="编辑" @click="edit" />
            </div>
            <!-- 撤销 -->
            <div v-if="demandInfoDetail.operate_flag == 'show'">
              <fdev-btn
                dialog
                class="q-ml-md"
                ficon="repeal"
                label="撤销"
                @click="delEvaMgt"
              />
            </div>
            <!-- 暂缓/取消暂缓、分析完成 ，操作权限与列表一致-->
            <!-- 暂缓/取消暂缓 -->
            <div v-if="demandInfoDetail.operate_flag == 'show'">
              <!-- 取消暂缓 -->
              <fdev-btn
                v-if="demandInfoDetail.demand_status === 3"
                dialog
                ficon="refresh_c_o"
                label="取消暂缓"
                @click="recover()"
                class="q-ml-md"
              />
              <!-- 暂缓 -->
              <div v-else>
                <fdev-btn
                  dialog
                  ficon="cancel_c_o"
                  label="暂缓"
                  @click="handleDefer()"
                  :disable="isAfterToday()"
                  class="q-ml-md"
                />
                <fdev-tooltip v-if="isAfterToday()">
                  <span>还未到起始评估日期不能暂缓</span>
                </fdev-tooltip>
              </div>
            </div>
            <!-- 分析完成 -->
            <div
              v-if="
                demandInfoDetail.confirmStatus &&
                  demandInfoDetail.demand_status === 1
              "
            >
              <fdev-btn
                dialog
                class="q-ml-md"
                ficon="check"
                label="分析完成"
                @click="complete()"
              />
            </div>
            <!-- 修改定稿日期,权限:后端配置文件配置的指定人员，只要未从conf同步任何阶段都能修改 -->
            <div v-if="demandInfoDetail.operate_flag !== 'noshow'">
              <fdev-btn
                dialog
                class="q-ml-md"
                ficon="lock"
                label="定稿"
                :disable="demandInfoDetail.final_date_status === 2"
                @click="Finalize()"
              />
              <fdev-tooltip v-if="demandInfoDetail.final_date_status === 2">
                <span>已从conflunce同步不能手动定稿</span>
              </fdev-tooltip>
            </div>
          </div>
        </div>
        <!-- 提示 -->
        <div>
          <f-icon
            name="alert_r_f"
            class="text-green"
            style="width:12px;height:12px"
          />
          <span class="tips-desc">
            此功能用于跟踪需求评估时长，点击需求编号可跳转到需求开发详情！
          </span>
        </div>
        <!-- 需求编号、需求创建人 -->
        <div class="row q-mt-21">
          <f-formitem
            profile
            label="需求编号"
            label-style="margin-right:32px;width:112px;font-size: 14px;color: #666666;line-height: 22px;height:22px"
            value-style="width:161px;font-size: 14px;line-height: 22px;height:22px"
          >
            <div class="ellipsis" :title="demandInfoDetail.oa_contact_no">
              <router-link
                class="link"
                v-if="demandInfoDetail.demand_id"
                :to="`/rqrmn/rqrProfile/${demandInfoDetail.demand_id}`"
                >{{ demandInfoDetail.oa_contact_no || '-' }}</router-link
              >
              <span v-else> {{ demandInfoDetail.oa_contact_no || '-' }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ demandInfoDetail.oa_contact_no }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </f-formitem>
          <f-formitem
            profile
            label="需求创建人"
            label-style="margin-right:32px;width:112px;margin-left:80px;font-size: 14px;color: #666666;line-height: 22px;height:22px"
            value-style="width:auto;font-size: 14px;color: #333333;line-height: 22px;height:22px"
          >
            <router-link
              v-if="
                demandInfoDetail.create_user_info &&
                  demandInfoDetail.create_user_info.id
              "
              :to="`/user/list/${demandInfoDetail.create_user_info.id}`"
              class="link"
            >
              {{
                (demandInfoDetail.create_user_info &&
                  demandInfoDetail.create_user_info.user_name_cn) ||
                  '-'
              }}</router-link
            >
            <span v-else>{{
              (demandInfoDetail.create_user_info &&
                demandInfoDetail.create_user_info.user_name_cn) ||
                '-'
            }}</span>
          </f-formitem>
        </div>
      </div></f-block
    >
    <!-- 评估安排 -->
    <f-block class="q-mt-10 row no-wrap">
      <div>
        <div class="row items-center line-title">
          <f-icon name="log_s_f" class="titleimg" />
          <span class="titlename">评估安排</span>
        </div>
        <div class="row q-mt-21">
          <f-formitem
            profile
            label="起始评估日期"
            label-style="margin-right:32px;width:112px;font-size: 14px;color: #666666;line-height: 22px;height:22px"
            value-style="width:161px;cursor:pointer;font-size: 14px;color: #333333;line-height: 22px;height:22px"
          >
            {{ demandInfoDetail.start_assess_date || '-' }}
          </f-formitem>
          <f-formitem
            profile
            label="完成评估日期"
            label-style="margin-right:32px;width:112px;margin-left:80px;font-size: 14px;color: #666666;line-height: 22px;height:22px"
            value-style="width:auto;font-size: 14px;color: #333333;line-height: 22px;height:22px"
          >
            {{ demandInfoDetail.end_assess_date || '-' }}
          </f-formitem>
        </div>
        <div class="row q-mt-14">
          <f-formitem
            profile
            label="牵头小组"
            label-style="margin-right:32px;width:112px;font-size: 14px;color: #666666;line-height: 22px;height:22px"
            value-style="width:161px;cursor:pointer;font-size: 14px;color: #333333;line-height: 22px;height:22px"
          >
            {{ demandInfoDetail.demand_leader_group_cn }}
          </f-formitem>
          <f-formitem
            profile
            label="牵头人员"
            label-style="margin-right:32px;width:112px;margin-left:80px;font-size: 14px;color: #666666;line-height: 22px;height:22px"
            value-style="width:102px;font-size: 14px;color: #333333;line-height: 22px;"
          >
            <router-link
              :to="`/user/list/${item.id}`"
              :title="item.user_name_cn"
              v-for="item in demandInfoDetail.demand_leader_info"
              :key="item.id"
              class="link q-mr-xs"
              >{{ item.user_name_cn }}
            </router-link>
          </f-formitem>
        </div>
        <div class="row q-mt-14">
          <f-formitem
            profile
            label="评估天数"
            label-style="margin-right:32px;width:112px;font-size: 14px;color: #666666;line-height: 22px;height:22px"
            value-style="width:161px;cursor:pointer;font-size: 14px;color: #333333;line-height: 22px;height:22px"
          >
            {{
              demandInfoDetail.assess_days == null
                ? '-'
                : demandInfoDetail.assess_days
            }}
          </f-formitem>
          <f-formitem
            profile
            label="超期分类"
            label-style="margin-right:32px;width:112px;margin-left:80px;font-size: 14px;color: #666666;line-height: 22px;height:22px"
            value-style="width:auto;font-size: 14px;color: #333333;line-height: 22px;height:22px"
          >
            <div v-if="overdueOptions.length">
              {{ type }}
            </div>
          </f-formitem>
        </div>
        <!-- 需求文档状态、定稿日期 -->
        <div class="row q-mt-14">
          <f-formitem
            profile
            label="需求文档状态"
            label-style="margin-right:32px;width:112px;font-size: 14px;color: #666666;line-height: 22px;height:22px"
            value-style="width:161px;cursor:pointer;font-size: 14px;color: #333333;line-height: 22px;height:22px"
          >
            {{
              demandInfoDetail.conf_state_cn
                ? demandInfoDetail.conf_state_cn
                : '-'
            }}
          </f-formitem>
          <f-formitem
            profile
            label="定稿日期"
            label-style="margin-right:32px;width:112px;margin-left:80px;font-size: 14px;color: #666666;line-height: 22px;height:22px"
            value-style="width:102px;font-size: 14px;color: #333333;line-height: 22px;"
          >
            {{
              demandInfoDetail.final_date ? demandInfoDetail.final_date : '-'
            }}
          </f-formitem>
        </div>
        <!-- conflunce的url -->
        <div class="row q-mt-14">
          <f-formitem
            profile
            full-width
            label="Conflunce地址"
            label-style="margin-right:32px;width:112px;font-size: 14px;color: #666666;line-height: 22px;height:22px"
            value-style="cursor:pointer;font-size: 14px;color: #333333;line-height: 22px"
          >
            <a
              :href="demandInfoDetail.conf_url"
              class="text-primary"
              target="_blank"
              v-if="demandInfoDetail.conf_url"
              >{{ demandInfoDetail.conf_url }}</a
            >
            <div v-else>{{ '-' }}</div>
          </f-formitem>
        </div>
        <div class="row q-mt-14">
          <f-formitem
            profile
            label="评估现状"
            label-style="margin-right:32px;width:112px;font-size: 14px;color: #666666;line-height: 22px;height:22px"
            value-style="width:calc(100% - 112px - 32px);cursor:pointer;font-size: 14px;color: #333333;line-height: 22px;"
          >
            <div
              class="ellipsis-3-lines"
              :title="demandInfoDetail.assess_present"
            >
              {{ demandInfoDetail.assess_present }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ demandInfoDetail.assess_present }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </f-formitem>
        </div>
        <!-- 暂缓记录 -->
        <span class="row q-mt-14">
          <f-formitem
            profile
            full-width
            label="评估记录"
            label-style="margin-right:32px;width:112px;font-size: 14px;color: #666666;line-height: 22px;height:22px"
            value-style="cursor:pointer;font-size: 14px;color: #333333;line-height: 22px"
          >
            <div
              v-for="(item, index) in demandInfoDetail.access_times"
              :key="index"
              class="q-pb-xs"
            >
              {{ item }}
            </div>
          </f-formitem>
        </span>
        <!-- 定稿日期修改记录 -->
        <span class="row q-mt-14">
          <f-formitem
            profile
            full-width
            label="定稿日期修改记录"
            label-style="margin-right:32px;width:112px;font-size: 14px;color: #666666;line-height: 22px;height:22px"
            value-style="cursor:pointer;font-size: 14px;color: #333333;line-height: 22px"
          >
            <span
              v-for="(item, index) in demandInfoDetail.approve_time_list"
              :key="index"
              class="q-pr-md"
            >
              {{ item }}
            </span>
          </f-formitem>
        </span>
      </div>
    </f-block>
    <!-- 文档列表 -->
    <f-block class="q-mt-10">
      <!-- 标题 -->
      <div class="row no-wrap">
        <div class="row  items-center line-title">
          <f-icon name="list_s_f" class="titleimg" />
          <span class="titlename">文件列表</span>
        </div>
        <fdev-space />
        <!-- 上传文件按钮：权限在需求管理员、需求牵头人，且撤销的需求不能再上传文档 -->
        <fdev-btn
          label="上传文档"
          ficon="upload"
          normal
          @click="uploadDirect()"
          v-if="demandInfoDetail.operate_flag !== 'noshow'"
          :disable="demandInfoDetail.demand_status === 9"
        />
      </div>
      <fdev-table
        :columns="docColumns"
        :data="demandDocList"
        noExport
        class="q-mt-md"
        noSelectCols
      >
        <template v-slot:body-cell-doc_name="props">
          <fdev-td
            v-if="props.row.doc_type == 'confluenceFile'"
            class="td-desc"
            :title="props.row.doc_link"
          >
            {{ props.row.doc_link }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.doc_link }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
          <fdev-td
            v-if="props.row.doc_type != 'confluenceFile'"
            :title="props.row.doc_name"
          >
            <div class="ellipsis">{{ props.row.doc_name }}</div>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.doc_name }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td></template
        >
        <template v-slot:body-cell-upload_user_all="props">
          <fdev-td :title="props.value.user_name_cn" class="ellipsis">
            <router-link
              v-if="props.value"
              :to="`/user/list/${props.value.id}`"
              class="link"
            >
              {{ props.value.user_name_cn }}
            </router-link>
          </fdev-td>
        </template>
        <template v-slot:body-cell-operation="props">
          <fdev-td
            :auto-width="true"
            class="td-padding "
            v-if="props.row.doc_type !== 'confluenceFile'"
          >
            <div class="q-gutter-x-sm row no-wrap items-center">
              <fdev-btn
                flat
                @click="exportDemandExcelData(props.row)"
                label="下载"
              />
              <div class="border"></div>
              <fdev-btn
                flat
                @click="deleteDoc(props.row)"
                label="删除"
                :disable="demandInfoDetail.operate_flag === 'noshow'"
              />
            </div>
          </fdev-td>
          <fdev-td
            :auto-width="true"
            class="td-padding"
            v-if="props.row.doc_type == 'confluenceFile'"
          >
            <div class="q-gutter-x-sm row no-wrap items-center">
              <fdev-btn flat @click="jumpConfluence(props.row)" label="查看" />
              <div class="border"></div>
              <fdev-btn
                flat
                @click="deleteDoc(props.row)"
                label="删除"
                :disable="demandInfoDetail.operate_flag === 'noshow'"
              />
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </f-block>
    <!-- 编辑 -->
    <updateDialog
      :id="id"
      :overdueCalOptions="overdueOptions"
      v-model="isShowUpdateDlg"
      @close="
        init();
        isShowUpdateDlg = !isShowUpdateDlg;
      "
    ></updateDialog>
    <!-- 上传文件弹窗 -->
    <uploadFile
      ref="uploadFile"
      @reFresh="reFresh"
      :demandId="demandInfoDetail.id"
      :isDirect="isDirect"
      @resetDirect="resetDirect"
    ></uploadFile>
    <!-- 完成评估弹窗 -->
    <complete
      ref="complete"
      @reFresh="reFresh"
      :id="demandInfoDetail.id"
      :startTime="demandInfoDetail.start_assess_date"
    />
    <!-- 修改定稿日期 -->
    <finalize
      ref="finalize"
      :id="demandInfoDetail.id"
      :status="demandInfoDetail.final_date_status"
      @reFresh="reFresh"
    ></finalize>
  </div>
</template>
<script>
import {
  queryById,
  deleteData,
  getOverdueTypeSelect,
  cancelDefer //取消暂缓
} from '@/modules/Rqr/services/methods.js';
import {
  successNotify,
  errorNotify,
  resolveResponseError
} from '@/utils/utils.js';
import { mapState, mapActions } from 'vuex';
import moment from 'moment';
export default {
  computed: {
    ...mapState('demandsForm', ['demandDocList'])
  },
  data() {
    return {
      id: this.$route.params.id,
      demandInfoDetail: {},
      isShowUpdateDlg: false,
      overdueOptions: [],
      type: '-',
      docColumns: [
        {
          name: 'doc_name',
          label: '文件名称',
          field: 'doc_name'
        },
        {
          name: 'doc_type',
          label: '文件类型',
          field: row => this.docTypeFilter(row.doc_type)
        },
        {
          name: 'upload_user_all',
          label: '上传人员',
          field: 'upload_user_all'
        },
        {
          name: 'create_time',
          label: '创建时间',
          field: 'create_time'
        },
        {
          name: 'operation',
          align: 'center',
          label: '操作',
          field: 'operation'
        }
      ],
      isDirect: null
    };
  },
  components: {
    updateDialog: () =>
      import('@/modules/Rqr/views/rqrEvaluateMgt/components/updateDialog.vue'),
    uploadFile: () =>
      import('@/modules/Rqr/views/rqrEvaluateMgt/components/uploadFile.vue'),
    complete: () =>
      import('@/modules/Rqr/views/rqrEvaluateMgt/components/complete.vue'),
    finalize: () =>
      import('@/modules/Rqr/views/rqrEvaluateMgt/components/finalize.vue')
  },
  created() {
    this.init();
    this.queryEmailList();
  },
  watch: {
    demandInfoDetail: {
      handler: function(a, b) {
        this.type = a.overdue_type
          ? this.overdueOptions.find(item => item.code == a.overdue_type).value
          : '-';
      },
      deep: true
    }
  },
  methods: {
    ...mapActions('demandsForm', [
      'exportExcelData',
      'deleteDemandDoc',
      'queryDemandDoc'
    ]),
    async delEvaMgt() {
      this.$q
        .dialog({
          title: `确认撤销`,
          message: `是否确认撤销本需求？`,
          ok: '是',
          cancel: '否'
        })
        .onOk(async () => {
          let res = await deleteData({ id: this.id });
          if (res && res.code && res.code != 'AAAAAAA') {
            // 失败
            errorNotify(res.msg);
          } else {
            // 成功
            successNotify('撤销成功!');
            this.init();
          }
        });
    },
    edit() {
      this.isShowUpdateDlg = !this.isShowUpdateDlg;
    },
    async init() {
      // Promise.all([getOverdueTypeSelect(), queryById({ id: this.id })]).then(
      //   res => {
      //     this.overdueOptions = res[0];
      //     this.demandInfoDetail = res[1];
      //   }
      // );
      Promise.all([
        resolveResponseError(() => getOverdueTypeSelect()),
        resolveResponseError(() => queryById({ id: this.id }))
      ]).then(res => {
        this.overdueOptions = res[0];
        this.demandInfoDetail = res[1];
      });
    },
    getPrioritySrc(status) {
      // 1 评估中  2完成  3 暂缓 9 撤销
      const bgObject = {
        '1': `url(${require('@/modules/Rqr/assets/status12bj.svg')})`, //评估中
        '2': `url(${require('@/modules/Rqr/assets/status7bj.svg')})`, //评估完成
        '3': `url(${require('@/modules/Rqr/assets/status3bj.svg')})`, //暂缓
        '9': `url(${require('@/modules/Rqr/assets/status9bj.svg')})` //撤销
      };
      return bgObject[status];
    },
    getStatusSrc(status) {
      const bgObject = {
        '1': `${require('@/modules/Rqr/assets/status12.svg')}`, //评估中
        '2': `${require('@/modules/Rqr/assets/status7.svg')}`, //评估完成
        '3': `${require('@/modules/Rqr/assets/status3.svg')}`, //暂缓
        '9': `${require('@/modules/Rqr/assets/status9.svg')}` //撤销
      };
      return bgObject[status];
    },
    filterdemandStatus(val) {
      let obj = {
        '1': '分析中',
        '2': '分析完成',
        '3': '暂缓中',
        '9': '撤销'
      };

      return obj[val];
    },
    filterDemandType(val) {
      const obj = {
        business: '业务需求',
        tech: '科技需求',
        daily: '日常需求'
      };
      return obj[val];
    },
    //文件下载
    async exportDemandExcelData({ doc_path }) {
      //下载暂缓邮件
      await this.exportExcelData({
        moduleName: 'fdev-demand',
        path: doc_path
      });
    },
    //文件删除
    deleteDoc(rowval) {
      let param = {
        ids: [rowval.id],
        doc_link: [rowval.doc_link],
        demand_kind: 'demandAccess'
      };
      this.$q
        .dialog({
          title: `删除确认`,
          message: `确认要删除文件吗？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.deleteDemandDoc(param);
          // 更新文件列表
          this.queryEmailList();
          successNotify('删除成功');
        });
    },
    docTypeFilter(val) {
      if (val == 'demandInstruction') {
        return (val = '需求说明书');
      } else if (val == 'techPlan') {
        return (val = '技术方案');
      } else if (val == 'demandReviewResult') {
        return (val = '需求评审决议表');
      } else if (val == 'meetMinutes') {
        return (val = '会议纪要');
      } else if (val == 'otherRelatedFile') {
        return (val = '其他相关材料');
      } else if (val == 'confluenceFile') {
        return (val = 'confluence文档');
      } else if (val == 'demandPlanInstruction') {
        return (val = '需求规格说明书');
      } else if (val === 'deferEmail') {
        return (val = '暂缓邮件');
      }
    },
    // 暂缓打开上传文件弹窗
    handleDefer() {
      this.$refs.uploadFile.openDialog();
    },
    // 直接上传文件
    uploadDirect() {
      this.isDirect = true;
      this.handleDefer();
    },
    // 刷新详情
    async reFresh() {
      Promise.all([
        queryById({ id: this.demandInfoDetail.id }),
        this.queryEmailList()
      ]).then(res => {
        this.demandInfoDetail = res[0];
        this.isDirect = false;
      });
    },
    //取消暂缓
    recover() {
      this.$q
        .dialog({
          title: `确认取消暂缓`,
          message: `是否取消暂缓？`,
          ok: '是',
          cancel: '否'
        })
        .onOk(async () => {
          await resolveResponseError(() =>
            cancelDefer({ id: this.demandInfoDetail.id })
          );
          successNotify('恢复成功!');
          //刷新详情
          this.reFresh();
        });
    },
    //分析完成
    complete() {
      this.$refs.complete.openDilaog();
    },
    //修改定稿日期
    Finalize() {
      this.$refs.finalize.openDilaog();
    },
    //查询邮件文件列表
    queryEmailList() {
      this.queryDemandDoc({
        demand_id: this.id,
        demand_kind: 'demandAccess'
      });
    },
    resetDirect() {
      this.isDirect = false;
    },
    //跳转conflunce链接
    jumpConfluence(rowval) {
      let url = rowval.doc_link;
      window.open(url, '_blank');
    },
    //如果起始评估日期大于今天则不能进行暂缓、分析完成操作
    isAfterToday() {
      const today = moment(new Date()).format('YYYY/MM/DD');
      return (
        moment(this.demandInfoDetail.start_assess_date).format('YYYY/MM/DD') >
        today
      );
    }
  }
};
</script>
<style lang="stylus" scoped>
.rqrHeader
  background: #FFFFFF;
  border-radius: 8px;
  margin-bottom:10px;
.rqrHeader
  .rqrBack
    padding: 0  20px 0 33px;
    min-height: 90px;
    width: 100%;
    border-radius: 8px;
    background-size: cover;
    background-repeat: no-repeat;
    background-position: left center;
    display:flex;
    justify-content: space-between;
  .rqrleft
    display: flex;
    align-items: center;
    .rqrName
      font-size: 20px;
      color: #333333;
      letter-spacing: 0;
      line-height: 30px;
      font-weight: 600;
    .rqrNum
      font-family: PingFangSC-Regular;
      font-size: 12px;
      color: #666;
      letter-spacing: 0;
      line-height: 20px;
      font-weight: 400;
    .maxWidth
      max-width: 600px;
  .rqrRight
    display:flex;
    align-items: center;
    width: 381px;
    justify-content: flex-end;
    .statusLogo
      width: 32px;
      height: 32px;
      margin-right: 16px;
    .statusName
      font-size: 18px;
      font-weight: 600;
      color: #333333;
      letter-spacing: 0;
      line-height: 28px;
    .statusType
      margin-left: 49px;
      height: 48px;
      .up
        font-size: 18px;
        font-weight: 600;
        color: #333333;
        letter-spacing: 0;
        line-height: 28px;
      .down
        font-family: PingFangSC-Regular;
        font-size: 12px;
        color: #666;
        letter-spacing: 0;
        line-height: 20px;
    .statusPriorityImg
        margin-left: 30px;
        width: 92px;
        height: 24px;
.demand-status-normal
  font-size: 18px;
  font-weight: 600;
  color: #333333;
  letter-spacing: 0;
  line-height: 28px;
.demand-status-normal-caption
  font-family: PingFangSC-Regular;
  font-size: 12px;
  color: #999;
  letter-spacing: 0;
  line-height: 20px;
.detail-middle
  min-height 107px
.border-radius
  border-radius: 8px;
.titleimg
    width: 24px;
    height: 24px;
    color: #0378ea;
    border-radius: 4px;
.titlename
    margin-left: 16px;
    font-size: 14px;
    font-weight: 600;
    color: #333;
.q-mt-21
  margin-top 20px
.q-mt-10
  margin-top 10px
.q-mt-14
  margin-top 14px
.Tips
  font-size: 14px;
  color: #666666;
  text-align: center;
  line-height: 12px;
  font-weight 400
  margin-top 11px
.tips-desc
  font-size: 12px;
  color: #666666;
  line-height: 22px;
.line-title
  height 24px
.td-desc
  max-width 180px
  overflow hidden
  text-overflow ellipsis
.border
  width 1px
  height 14px
  background #DDD
</style>
