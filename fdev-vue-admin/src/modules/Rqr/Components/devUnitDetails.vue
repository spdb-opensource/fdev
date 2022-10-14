<template>
  <div>
    <div class="q-gutter-md">
      <!-- 标题部分 -->
      <f-block block>
        <div class="row no-wrap">
          <div class="text-h5" v-if="detailtInfo">
            <span class="q-mr-md">{{
              detailtInfo.implement_unit_content
            }}</span>
            <fdev-badge>{{ devUnitStatus() }}</fdev-badge>
            <div class="text-subtitle1">
              {{ detailtInfo.fdev_implement_unit_no }}
            </div>
          </div>
        </div>
      </f-block>
      <f-block block v-if="$route.query.demandId">
        <div class="text-h6 text-bold q-mb-md">需求基本信息</div>
        <div class="row">
          <div class="col-4">
            <f-formitem page label="需求编号:">
              <router-link
                :to="`/rqrmn/rqrProfile/${demandModel.id}`"
                class="link"
              >
                {{ demandModel.oa_contact_no }}
              </router-link>
            </f-formitem>
          </div>
          <div class="col-4">
            <f-formitem page label="需求名称:">
              <router-link
                :to="`/rqrmn/rqrProfile/${demandModel.id}`"
                class="link"
              >
                {{ demandModel.oa_contact_name }}
              </router-link>
            </f-formitem>
          </div>
          <div class="col-4">
            <f-formitem page label="需求牵头人:"
              ><div class="text-ellipsis">
                <span
                  v-for="(item, index) in demandModel.demand_leader_all"
                  :key="index"
                >
                  <router-link :to="`/user/list/${item.id}`" class="link">
                    {{ item.user_name_cn }}
                  </router-link>
                </span>
              </div>
            </f-formitem>
          </div>
        </div>
      </f-block>
      <!-- 研发单元基础信息 -->
      <f-block block>
        <div class="text-h6 text-bold q-mb-md">研发单元基础信息</div>
        <div class="row q-gutter-diaLine justify-between">
          <f-formitem
            v-for="(item, index) in devUnitBaseInfo"
            :key="index"
            :label="item.label"
            label-style="width:140px"
          >
            <div class="q-gutter-x-sm" v-if="item.key === 'implement_leader'">
              <router-link
                v-for="(each, index) in detailtInfo &&
                  detailtInfo.implement_leader_all"
                :to="`/user/list/${each.id}`"
                :key="index"
                class="link"
              >
                <span class="text-primary cursor-pointer">
                  {{ each.user_name_cn }}
                </span>
              </router-link>
            </div>
            <div v-else>{{ item.value }}</div>
          </f-formitem>
          <template v-if="detailtInfo">
            <f-formitem label="超期类别：" label-style="width:140px">
              <div>{{ detailtInfo.overdueType }}</div>
            </f-formitem>
            <f-formitem label="申请原因：" label-style="width:140px">
              <div>{{ detailtInfo.overdueReason }}</div>
            </f-formitem>
            <f-formitem
              v-if="detailtInfo.approveType"
              label="审批类型："
              label-style="width:140px"
            >
              <div>
                {{ approveMap[detailtInfo.approveType] || '-' }}
              </div>
            </f-formitem>
            <f-formitem label="审批状态：" label-style="width:140px">
              <div>
                {{ detailtInfo.approveState | approveStateFilter }}
              </div>
            </f-formitem>
            <f-formitem
              label="审批说明："
              label-style="width:140px"
              v-if="
                detailtInfo.approveState === 'pass' ||
                  detailtInfo.approveState === 'reject'
              "
            >
              <div>
                {{ detailtInfo.approveReason }}
              </div>
            </f-formitem>
            <f-formitem
              label="审批人："
              label-style="width:140px"
              v-if="
                detailtInfo.approveState === 'pass' ||
                  detailtInfo.approveState === 'reject'
              "
            >
              <router-link
                :to="`/user/list/${detailtInfo.approverId}`"
                class="link"
              >
                <span class="text-primary cursor-pointer">
                  {{ detailtInfo.approverName }}
                </span>
              </router-link>
            </f-formitem>
          </template>
        </div>
      </f-block>
      <!-- 工作量 -->
      <f-block block>
        <div class="text-h6 text-bold q-mb-md">工作量</div>
        <div class="row q-gutter-diaLine justify-between">
          <f-formitem
            v-for="(item, index) in worker"
            :key="index"
            :label="item.label"
            label-style="width:180px"
          >
            <div>{{ item.value }}</div>
          </f-formitem>
        </div>
      </f-block>
      <!-- 实施安排及情况 -->
      <f-block block>
        <div class="text-h6 text-bold q-mb-md">实施安排及情况</div>
        <div class="row">
          <!-- detailtInfo.demand_type -->

          <template v-if="detailtInfo && detailtInfo.demand_type == 'daily'">
            <f-formitem
              class="col-4 q-mb-md"
              v-for="(item, index) in implSituationDaily"
              :key="index"
              :label="item.label"
            >
              <div>{{ item.value }}</div>
            </f-formitem>
          </template>
          <template v-else>
            <f-formitem
              class="col-4 q-mb-md"
              v-for="(item, index) in implSituation"
              :key="index"
              :label="item.label"
              label-style="width:180px"
            >
              <div>{{ item.value }}</div>
            </f-formitem>
          </template>
        </div>
      </f-block>
      <!-- 任务 -->
      <f-block block>
        <div class="text-h6 text-bold q-mb-md">任务</div>
        <Task
          v-if="detailtInfo"
          :demandDetail="detailtInfo"
          :devUnitNum="detailtInfo.fdev_implement_unit_no"
        />
      </f-block>
    </div>
  </div>
</template>
<script>
import Task from './Task';
import { queryFdevImplUnitDetail } from '@/services/demand.js';
import { mapState, mapActions } from 'vuex';
import { approveMap } from '@/modules/Rqr/model.js';
export default {
  name: 'devUnitDetails',
  components: {
    Task
  },
  data() {
    return {
      detailtInfo: null,
      approveMap,
      devUnitBaseInfo: [
        {
          key: 'implement_unit_content',
          label: '研发单元内容：',
          value: ''
        },
        {
          key: 'implement_leader',
          label: '研发单元牵头人：',
          value: ''
        },
        {
          key: 'group',
          label: '所属小组：',
          value: '',
          formate: v => this.detailtInfo.group_cn
        },
        {
          key: 'ui_verify',
          label: '是否涉及UI还原审核：',
          value: '',
          formate: v => (v ? '涉及' : '不涉及')
        }
      ],
      worker: [
        {
          key: 'dept_workload',
          label: '预期行内人员工作量(人月)：',
          value: ''
        },
        {
          key: 'company_workload',
          label: '预期公司人员工作量(人月)：',
          value: ''
        }
      ],
      implSituation: [
        {
          key: 'plan_start_date',
          label: '计划启动开发日期：',
          value: '2020-08-08'
        },
        {
          key: 'plan_inner_test_date',
          label: '计划提交内测日期：',
          value: '2020-08-08'
        },
        {
          key: 'plan_test_date',
          label: '计划提交用户测试日期：',
          value: '2020-08-08'
        },
        {
          key: 'plan_test_finish_date',
          label: '计划用户测试完成日期：',
          value: '2020-08-08'
        },
        {
          key: 'plan_product_date',
          label: '计划投产日期：',
          value: '2020-08-08'
        },
        {
          key: '',
          label: '',
          value: ''
        },
        {
          key: 'real_start_date',
          label: '实际启动开发日期：',
          value: '2020-08-08'
        },
        {
          key: 'real_inner_test_date',
          label: '实际提交内测日期：',
          value: '2020-08-08'
        },

        {
          key: 'real_test_date',
          label: '实际提交用户测试日期：',
          value: '2020-08-08'
        },
        {
          key: 'real_test_finish_date',
          label: '实际用户测试完成日期：',
          value: '2020-08-08'
        },
        {
          key: 'real_product_date',
          label: '实际投产日期：',
          value: '2020-08-08'
        }
      ],
      implSituationDaily: [
        {
          key: 'plan_start_date',
          label: '计划启动日期：',
          value: '2020-08-08'
        },
        {
          key: 'plan_product_date',
          label: '计划完成日期：',
          value: '2020-08-08'
        },
        {
          key: 'real_start_date',
          label: '实际启动日期：',
          value: '2020-08-08'
        },
        {
          key: 'real_product_date',
          label: '实际完成日期：',
          value: '2020-08-08'
        }
      ],
      demandModel: {}
    };
  },
  async created() {
    this.getDetailsInfo();
    // 只有从研发单元列表 跳转到 研发单元详情才展示 需求信息
    if (this.$route.query.demandId) {
      await this.queryDemandInfoDetail({ id: this.$route.query.demandId });
      this.demandModel = this.demandInfoDetail;
    }
  },
  filters: {
    approveStateFilter(val) {
      const obj2 = {
        noSubmit: '未提交',
        wait: '待审批',
        pass: '通过',
        reject: '拒绝'
      };
      return obj2[val];
    }
  },
  computed: {
    ...mapState('demandsForm', ['demandInfoDetail'])
  },
  methods: {
    ...mapActions('demandsForm', {
      queryDemandInfoDetail: 'queryDemandInfoDetail'
    }),
    async getDetailsInfo() {
      const { id, dev_unit_no } = this.$route.query;
      try {
        this.detailtInfo = await queryFdevImplUnitDetail({
          id,
          fdev_implement_unit_no: dev_unit_no
        });
      } catch (e) {
        throw new Error(e);
      }
      let type = this.detailtInfo.demand_type;
      if (type !== 'business' && type !== 'tech') {
        this.devUnitBaseInfo = this.devUnitBaseInfo.filter(
          item => item.key != 'ui_verify'
        );
      }

      this.matchedFun(this.detailtInfo, this.devUnitBaseInfo);
      this.matchedFun(this.detailtInfo, this.worker);
      if (this.detailtInfo && this.detailtInfo.demand_type == 'daily') {
        this.matchedFun(this.detailtInfo, this.implSituationDaily);
      } else {
        this.matchedFun(this.detailtInfo, this.implSituation);
      }
    },
    // 匹配赋值
    matchedFun(obj, arr) {
      for (let k in obj) {
        let index = arr.findIndex(item => item.key === k);
        if (index > -1) {
          arr[index].formate
            ? (arr[index].value = arr[index].formate(obj[k]))
            : (arr[index].value = obj[k]);
        }
      }
    },
    devUnitStatus() {
      let type = this.detailtInfo.demand_type;

      const obj1 = {
        1: '评估中',
        2: type == 'daily' ? '未开始' : '待实施',
        3: type == 'daily' ? '进行中' : '开发中',
        4: 'sit',
        5: 'uat',
        6: 'rel',
        7: type == 'daily' ? '已完成' : '已投产',
        8: '已归档',
        9: '已撤销'
      };
      let obj2 = {
        1: '暂缓中',
        2: '恢复中',
        3: '恢复完成'
      };
      let special = this.detailtInfo.implement_unit_status_special;
      if (special && special !== 3) return obj2[special];
      if (this.detailtInfo.implement_unit_status_normal)
        return obj1[this.detailtInfo.implement_unit_status_normal];
    }
  }
};
</script>
