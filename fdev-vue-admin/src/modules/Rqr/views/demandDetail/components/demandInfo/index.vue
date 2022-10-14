<template>
  <div class="content">
    <!-- 科技需求、业务需求 基础信息 -->
    <div class="flexbetween">
      <div class="base">
        <div class="row items-center line-title">
          <f-icon name="basic_msg_s_f" class="titleimg" />
          <span class="titlename">基础信息</span>
        </div>
        <div>
          <div class="flexRow" v-if="demand_type === 'business'">
            <f-formitem
              class="formItem"
              label="需求提出部门"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 two-line"
                :title="demandModel.propose_demand_dept"
              >
                {{ demandModel.propose_demand_dept }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ demandModel.propose_demand_dept }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="需求计划名称"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 two-line"
                :title="demandModel.demand_plan_name"
              >
                {{ demandModel.demand_plan_name }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ demandModel.demand_plan_name }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="实施单元跟踪人"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div class="showData width150 two-line">
                {{
                  demandModel.impl_track_user &&
                    demandModel.impl_track_user.join(',')
                }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{
                      demandModel.impl_track_user &&
                        demandModel.impl_track_user.join(',')
                    }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="对应需求计划编号"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 two-line"
                :title="demandModel.demand_plan_no"
              >
                {{ demandModel.demand_plan_no }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ demandModel.demand_plan_no }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="需求信息单我部接收日期"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 two-line"
                :title="demandModel.oa_receive_date"
              >
                {{ demandModel.oa_receive_date }}
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="UI设计稿审核状态"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div class="showData width150 two-line">
                <!-- 异常关闭状态和不涉及 -->
                <span
                  v-if="
                    !designStatus ||
                      designStatus === 'abnormalShutdown' ||
                      designStatus === 'noRelate'
                  "
                >
                  {{ demandModel.design_status | designStatusFilter }}
                </span>
                <!-- 其他正常状态 -->
                <router-link
                  v-else
                  :to="`/rqrmn/designReviewRqr/${demandModel.id}`"
                  class="link"
                >
                  {{ demandModel.design_status | designStatusFilter }}
                </router-link>
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="需求书名称"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 two-line"
                :title="demandModel.demand_instruction"
              >
                {{ demandModel.demand_instruction }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ demandModel.demand_instruction }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="行内人员预期工作量（人月）"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 two-line"
                :title="demandModel.dept_workload"
              >
                {{ demandModel.dept_workload }}
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="公司人员预期工作量（人月）"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 two-line"
                :title="demandModel.company_workload"
              >
                {{ demandModel.company_workload }}
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="需求属性"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 two-line"
                :title="demandModel.demand_property | demand_propertyFilter"
              >
                {{ demandModel.demand_property | demand_propertyFilter }}
              </div>
            </f-formitem>
          </div>
          <div class="flexRow" v-if="demand_type === 'tech'">
            <f-formitem
              class="formItem"
              label="受理日期"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 two-line"
                :title="demandModel.accept_date"
              >
                {{ demandModel.accept_date }}
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="需求创建人"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div class="showData width150 two-line">
                <router-link
                  v-if="demandModel.demand_create_user_all"
                  :to="`/user/list/${demandModel.demand_create_user_all.id}`"
                  class="link"
                >
                  {{ demandModel.demand_create_user_all.user_name_cn }}
                </router-link>
                <div v-else>-</div>
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="科技类型"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div class="showData width150 two-line">
                {{ demandModel.tech_type }}
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="UI设计稿审核状态"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div class="showData width150 two-line">
                <!-- 异常关闭状态和不涉及 -->
                <span
                  v-if="
                    !designStatus ||
                      designStatus === 'abnormalShutdown' ||
                      designStatus === 'noRelate'
                  "
                >
                  {{ demandModel.design_status | designStatusFilter }}
                </span>
                <!-- 其他正常状态 -->
                <router-link
                  v-else
                  tag="span"
                  :to="`/rqrmn/designReviewRqr/${demandModel.id}`"
                  class="normal-link text-primary cursor-pointer"
                >
                  {{ demandModel.design_status | designStatusFilter }}
                </router-link>
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="备注"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 two-line"
                :title="demandModel.tech_type_desc"
              >
                {{ demandModel.tech_type_desc }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ demandModel.tech_type_desc }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="行内人员预期工作量（人月）"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 two-line"
                :title="demandModel.dept_workload"
              >
                {{ demandModel.dept_workload }}
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="公司人员预期工作量（人月）"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 two-line"
                :title="demandModel.company_workload"
              >
                {{ demandModel.company_workload }}
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="需求属性"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 two-line"
                :title="demandModel.demand_property | demand_propertyFilter"
              >
                {{ demandModel.demand_property | demand_propertyFilter }}
              </div>
            </f-formitem>
          </div>
          <div class="flexRow" v-if="demand_type === 'daily'">
            <f-formitem
              class="formItem"
              label="受理日期"
              profile
              :label-style="basedailyStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 two-line"
                :title="demandModel.accept_date"
              >
                {{ demandModel.accept_date }}
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="需求属性"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 two-line"
                :title="demandModel.demand_property | demand_propertyFilter"
              >
                {{ demandModel.demand_property | demand_propertyFilter }}
              </div>
            </f-formitem>
          </div>
          <div class="row">
            <f-formitem
              label="需求标签"
              profile
              v-if="
                demandModel.demand_label_info &&
                  demandModel.demand_label_info.length > 0
              "
              :label-style="
                demandModel.demand_type === 'daily' ? labelDaily : labelbase
              "
              value-style="width:402px;min-height: 36px"
            >
              <div class="width400 row">
                <div
                  v-for="(item, index) in demandModel.demand_label_info"
                  :key="index"
                >
                  <div
                    v-if="item.flag"
                    class="q-mr-sm labelSty q-my-xs"
                    :style="'background:' + item.color"
                  >
                    {{ item.label }}
                  </div>
                </div>
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="需求说明"
              profile
              :label-style="
                demandModel.demand_type === 'daily' ? labelDaily : labelbase
              "
              value-style="width:402px;"
            >
              <div
                class="showData width400 two-line"
                :title="demandModel.demand_desc"
              >
                {{ demandModel.demand_desc }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ demandModel.demand_desc }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </div>
            </f-formitem>
          </div>
        </div>
      </div>
      <div class="demand">
        <div class="row items-center line-title">
          <div class="dback">
            <f-icon name="time_r" class="dimg" />
          </div>
          <span class="titlename">需求排期</span>
        </div>
        <div class="flexRow" v-if="demand_type === 'daily'">
          <f-formitem
            label="计划启动日期"
            profile
            :label-style="demandDailyStyle"
            value-style="width:82px;"
          >
            <div
              class="width80  showData ellipsis"
              :title="demandModel.plan_start_date"
            >
              {{ demandModel.plan_start_date }}
            </div>
          </f-formitem>
          <f-formitem
            label="计划完成日期"
            profile
            :label-style="demandDailyStyle"
            value-style="width:82px;"
          >
            <div
              class="width80  showData ellipsis"
              :title="demandModel.plan_product_date"
            >
              {{ demandModel.plan_product_date }}
            </div>
          </f-formitem>
          <f-formitem
            label="实际启动日期"
            profile
            :label-style="demandDailyStyle"
            value-style="width:82px;"
          >
            <div
              class="width80  showData ellipsis"
              :title="demandModel.real_start_date"
            >
              {{ demandModel.real_start_date }}
            </div>
          </f-formitem>

          <f-formitem
            label="实际完成日期"
            profile
            :label-style="demandDailyStyle"
            value-style="width:82px;"
          >
            <div
              class="width80  showData ellipsis"
              :title="demandModel.real_product_date"
            >
              {{ demandModel.real_product_date }}
            </div>
          </f-formitem>
        </div>
        <div class="flexRow" v-else>
          <f-formitem
            label="计划启动开发日期"
            profile
            :label-style="demandLabelStyle"
            value-style="width:83px;"
          >
            <div
              class="width80  showData ellipsis"
              :title="demandModel.plan_start_date"
            >
              {{ demandModel.plan_start_date }}
            </div>
          </f-formitem>
          <f-formitem
            label="实际启动开发日期"
            profile
            :label-style="demandLabelStyle"
            value-style="width:83px;"
          >
            <div
              class="width80  showData ellipsis"
              :title="demandModel.real_start_date"
            >
              {{ demandModel.real_start_date }}
            </div>
          </f-formitem>
          <f-formitem
            label="计划提交内测日期"
            profile
            :label-style="demandLabelStyle"
            value-style="width:83px;"
          >
            <div
              class="width80  showData ellipsis"
              :title="demandModel.plan_inner_test_date"
            >
              {{ demandModel.plan_inner_test_date }}
            </div>
          </f-formitem>
          <f-formitem
            label="实际提交内测日期"
            profile
            :label-style="demandLabelStyle"
            value-style="width:83px;"
          >
            <div
              class="width80  showData ellipsis"
              :title="demandModel.real_inner_test_date"
            >
              {{ demandModel.real_inner_test_date }}
            </div>
          </f-formitem>
          <f-formitem
            label="计划提交用户测试日期"
            profile
            :label-style="demandLabelStyle"
            value-style="width:83px;"
          >
            <div
              class="width80  showData ellipsis"
              :title="demandModel.plan_test_date"
            >
              {{ demandModel.plan_test_date }}
            </div>
          </f-formitem>
          <f-formitem
            label="实际提交用户测试日期"
            profile
            :label-style="demandLabelStyle"
            value-style="width:83px;"
          >
            <div
              class="width80  showData ellipsis"
              :title="demandModel.real_test_date"
            >
              {{ demandModel.real_test_date }}
            </div>
          </f-formitem>
          <f-formitem
            label="计划用户测试完成日期"
            profile
            :label-style="demandLabelStyle"
            value-style="width:83px;"
          >
            <div
              class="width80  showData ellipsis"
              :title="demandModel.plan_test_finish_date"
            >
              {{ demandModel.plan_test_finish_date }}
            </div>
          </f-formitem>
          <f-formitem
            label="实际用户测试完成日期"
            profile
            :label-style="demandLabelStyle"
            value-style="width:83px;"
          >
            <div
              class="width80  showData ellipsis"
              :title="demandModel.real_test_finish_date"
            >
              {{ demandModel.real_test_finish_date }}
            </div>
          </f-formitem>
          <f-formitem
            label="计划投产日期"
            profile
            :label-style="demandLabelStyle"
            value-style="width:83px;"
          >
            <div
              class="width80  showData ellipsis"
              :title="demandModel.plan_product_date"
            >
              {{ demandModel.plan_product_date }}
            </div>
          </f-formitem>
          <f-formitem
            label="实际投产日期"
            profile
            :label-style="demandLabelStyle"
            value-style="width:83px;"
          >
            <div
              class="width80  showData ellipsis"
              :title="demandModel.real_product_date"
            >
              {{ demandModel.real_product_date }}
            </div>
          </f-formitem>
        </div>
      </div>
    </div>
    <div class="evaluateOpinion">
      <div class="row items-center line-title mbtm">
        <f-icon name="bell_s_f" class="titleimg" />
        <span class="titlename">评估安排</span>
      </div>
      <div class="tbRow">
        <div class="tbflex full-width">
          <div class="tbItem">
            <div class="tblabel showLabel">牵头小组</div>
            <div class="tbline">
              <div class="tbgroup showGroupText ellipsis">
                {{ demandModel.demand_leader_group_cn }}
              </div>
            </div>
          </div>
          <f-formitem
            label="牵头负责人"
            profile
            :label-style="leaddailyStyle"
            value-style="width:150px;"
          >
            <div
              v-if="demandModel.demand_leader_all"
              class="showData width150 ellipsis"
              :title="
                demandModel.demand_leader_all
                  .map(v => v.user_name_cn)
                  .join('，')
              "
            >
              <router-link
                v-for="(each, index) in demandModel.demand_leader_all"
                :to="`/user/list/${each.id}`"
                :key="index"
                class="link q-mr-xs"
              >
                {{ each.user_name_cn }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{
                      demandModel.demand_leader_all
                        .map(v => v.user_name_cn)
                        .join(' ')
                    }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </div>
          </f-formitem>
        </div>
      </div>
      <div class="tbRow2">
        <div class="tblabel showLabel">涉及小组</div>
        <div class="tbline">
          <div
            class="tbgroup showGroupText ellipsis"
            v-for="(item, index) in demandModel.relate_part_detail"
            :key="index"
          >
            <span>
              {{ `${item.part_name} — ` }}
            </span>
            <span>
              {{ item.assess_user_all | assessUserFilter }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
// 引入组件
import { priValue, designStatusMap } from '@/modules/Rqr/model';
export default {
  name: 'demandInfo',
  props: {
    params: {
      type: Object
    }
  },
  components: {},
  watch: {
    // params: {
    //   handler(newVal) {
    //     this.setDemandInfo(newVal);
    //   },
    //   deep: true
    // }
  },
  data() {
    return {
      demand_type: null, // 需求类型 tech--科技需求；business--业务需求；daily--日常需求；
      demandModel: {}, //需求详情
      labelDaily:
        'width:128px;font-family: PingFangSC-Regular;font-size: 14px;color: #999999;letter-spacing: 0;line-height: 22px;',
      labelbase:
        'width:112px;font-family: PingFangSC-Regular;font-size: 14px;color: #999999;letter-spacing: 0;line-height: 22px;',
      leaddailyStyle:
        'width:142px;font-family: PingFangSC-Regular;font-size: 14px;color: #999999;letter-spacing: 0;line-height: 22px;',
      basedailyStyle:
        'width:128px;font-family: PingFangSC-Regular;font-size: 14px;color: #999999;letter-spacing: 0;line-height: 22px;',
      baseLabelStyle:
        'width:112px;font-family: PingFangSC-Regular;font-size: 14px;color: #999999;letter-spacing: 0;line-height: 22px;',
      demandLabelStyle:
        'width:145px;font-family: PingFangSC-Regular;font-size: 14px;color: #999999;letter-spacing: 0;line-height: 22px;',
      demandDailyStyle:
        'width:128px;font-family: PingFangSC-Regular;font-size: 14px;color: #999999;letter-spacing: 0;line-height: 22px;'
    };
  },
  filters: {
    priorityFilter(val) {
      return priValue[val] ? priValue[val] : '';
    },
    assessUserFilter(val) {
      if (val) {
        let assessUserAll = val;
        let assessUserList = [];
        assessUserAll.forEach(item => {
          assessUserList.push(item.user_name_cn);
        });
        return (val = assessUserList.join(','));
      }
    },
    designStatusFilter(val) {
      if (!val) val = 'noRelate';
      return designStatusMap[val] ? designStatusMap[val] : '';
    },
    demand_propertyFilter(val) {
      let obj1 = {
        advancedResearch: '预研',
        keyPoint: '重点',
        routine: '常规'
      };
      return obj1[val] ? obj1[val] : '';
    }
  },
  created() {
    this.init();
  },
  methods: {
    // 初始化操作总入口
    init() {
      this.setDemandInfo(this.params);
      //获取设计稿状态
      this.designStatus = this.demandModel.design_status;
    },
    isOtherType(key) {
      if (key && key.indexOf('其他') !== -1) {
        return true;
      } else return false;
    },
    setDemandInfo(data) {
      this.demandModel = data || {};
      this.demand_type = this.demandModel.demand_type || '';
    }
  }
};
</script>
<style lang="stylus" scoped>

.content{
  background: #f4f6fd;
  padding-top: 10px;
  .line-title{
    margin-bottom: 11px;
    line-height: 16px;
    .dback{
        width: 24px;
        height: 24px;
        background: #0378EA;
        border-radius: 4px;
        border-radius: 4px;
        justify-content: center;
        align-items: center;
        display: -webkit-box;
        .dimg{
          width: 15px;
          height: 15px;
          color: #fff;
        }
    }
    .titleimg{
        width: 24px;
        height: 24px;
        color: #0378EA;
        border-radius: 4px;
        border-radius: 4px;
      }
    .titlename{
        margin-left: 16px;
        font-size: 14px;
        font-weight: 600;
        color: #333333;
        letter-spacing: 0;
        line-height: 22px;
      }
  }
  .flexbetween{
    display: flex;
    justify-content: space-between;
    .base{
      background: #fff;
      padding: 20px 32px 20px 32px;
      border-radius: 8px;
      display: inline-block;
      margin-right: 10px;
      min-width: 600px;
      width: 600px;
      .formItem{
        align-items: baseline;
        margin-bottom: 8px;
      }
    }
    .demand{
      background: #fff;
      padding: 20px 32px 20px 32px;
      border-radius: 8px;
      display: inline-block;
      width:100%;
    }
  }
  .evaluateOpinion{
    .mbtm{
      margin-bottom: 15px;
    }
    background: #fff;
    margin-top: 10px;
    padding: 20px 32px 20px 32px;
    border-radius: 8px;
    .tbRow{
        .tbflex{
          display: flex;
        }
        .tbItem{
          display: flex;
          align-items: center;
          height: 26px;
          width: 610px;
          margin-bottom: 10px;
          margin-top: 4px;
          padding-right: 30px;
          .tblabel{
            width: 126px;
          }
          .tbline{
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            .tbgroup{
              background: #F1F1F1;
              border-radius: 2px;
              margin-right: 8px;
              padding: 0 8px;
              text-align: center;
              line-height:26px;
            }
          }
        }
    }
    .tbRow2{
        display: flex;
        align-items: flex-start;
        .tblabel{
          font-family: PingFangSC-Regular;
          font-size: 14px;
          color: #999999;
          letter-spacing: 0;
          width: 126px;
        }
        .tbline{
          max-width: 948px;
          display: flex;
          align-items: center;
          flex-wrap: wrap;
          .tbgroup{
            background: #F1F1F1;
            border-radius: 2px;
            margin-right: 8px;
            margin-bottom: 10px;
            padding: 0 8px;
            text-align: center;
            line-height:26px;
          }
        }
    }
  }
}
.flexRow{
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}
.width150{
  width: 130px;
}
.width400{
  width: 400px;
}
.two-line{
  overflow: hidden;
  -webkit-line-clamp: 2;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  text-overflow: ellipsis;
  line-height: 16px;
}
.showData{
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
}
.showGroupText{
  font-size: 14px;
  color: #333333;
}
.showLabel{
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #999999;
}
.width80{
  width: 80px;
}
.labelSty
  color #fff;
  padding 2px 5px;
  border-radius 3px;
  line-height 24px
</style>
