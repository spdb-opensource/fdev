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
          <div class="flexRow">
            <f-formitem
              class="formItem"
              label="创建人"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div class="showData width150 ellipsis">
                {{ orderModel.createUserNameCn }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ orderModel.createUserNameCn }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="申请时间"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 ellipsis"
                :title="orderModel.applyTime"
              >
                {{ orderModel.applyTime }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ orderModel.applyTime }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="牵头人"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 ellipsis"
                :title="orderModel.leaderName"
              >
                {{ orderModel.leaderName }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ orderModel.leaderName }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="牵头小组"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 ellipsis"
                :title="orderModel.leaderGroupCn"
              >
                {{ orderModel.leaderGroupCn }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ orderModel.leaderGroupCn }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="计划投产日期"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 ellipsis"
                :title="orderModel.planProductDate"
              >
                {{ orderModel.planProductDate }}
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="实际投产日期"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 ellipsis"
                :title="orderModel.realProductDate"
              >
                {{ orderModel.realProductDate }}
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="期望审核日期"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 ellipsis"
                :title="orderModel.expectAuditDate"
              >
                {{ orderModel.expectAuditDate }}
              </div>
            </f-formitem>
            <f-formitem
              class="formItem"
              label="邮件通知人"
              profile
              :label-style="baseLabelStyle"
              value-style="width:130px;"
            >
              <div
                class="showData width150 ellipsis"
                :title="orderModel.emailToNameCn"
              >
                {{ orderModel.emailToNameCn }}
              </div>
            </f-formitem>
          </div>
        </div>
        <div class="row progressLine">
          <div class="lleftlabel">审核进度</div>
          <div v-if="orderModel.orderStatus == '8'" class="lrightContent">
            <!-- 拒绝 -->
            <div class="citem width42">
              <img
                class="imgSize"
                src="@/modules/Network/assets/orderstaus/orderstatus1.svg"
                alt=""
              />
              <div class="fontName corHui width42">待审核</div>
            </div>
            <div class="progressItem">
              <img
                class="imgPro"
                src="@/modules/Network/assets/orderstaus/progress_1.svg"
                alt=""
              />
            </div>
            <div class="citem width42">
              <img
                class="imgSize"
                src="@/modules/Network/assets/orderstaus/orderstatus8.svg"
                alt=""
              />
              <div class="fontName corhei width42">拒绝</div>
            </div>
          </div>
          <div v-else class="lrightContent">
            <div class="citem width42">
              <img
                class="imgSize"
                src="@/modules/Network/assets/orderstaus/orderstatus1.svg"
                alt=""
              />
              <div
                :class="[
                  'fontName width42',
                  {
                    corHui: orderModel.orderStatus > 1
                  },
                  {
                    corhei: orderModel.orderStatus == 1
                  }
                ]"
              >
                待审核
              </div>
            </div>
            <div class="progressItem">
              <img
                v-if="orderModel.orderStatus >= 2"
                class="imgPro"
                src="@/modules/Network/assets/orderstaus/progress_1.svg"
                alt=""
              />
              <img
                v-else
                class="imgPro"
                src="@/modules/Network/assets/orderstaus/progress_0.svg"
                alt=""
              />
            </div>
            <div class="citem width42">
              <img
                v-if="orderModel.orderStatus >= 2"
                class="imgSize"
                src="@/modules/Network/assets/orderstaus/orderstatus2.svg"
                alt=""
              />
              <img
                v-else
                class="imgSize"
                src="@/modules/Network/assets/orderstaus/orderstatus2_0.svg"
                alt=""
              />
              <div
                class="fontName corHui width42"
                :class="[
                  'fontName width42',
                  {
                    corHui:
                      orderModel.orderStatus < 2 || orderModel.orderStatus >= 5
                  },
                  {
                    corhei:
                      orderModel.orderStatus >= 2 && orderModel.orderStatus < 5
                  }
                ]"
              >
                审核中
              </div>
            </div>
            <div class="progressItem">
              <img
                v-if="orderModel.orderStatus >= 5"
                class="imgPro"
                src="@/modules/Network/assets/orderstaus/progress_1.svg"
                alt=""
              />
              <img
                v-else
                class="imgPro"
                src="@/modules/Network/assets/orderstaus/progress_0.svg"
                alt=""
              />
            </div>
            <div class="citem width56">
              <img
                v-if="orderModel.orderStatus >= 5"
                class="imgSize"
                src="@/modules/Network/assets/orderstaus/orderstatus5.svg"
                alt=""
              />
              <img
                v-else
                class="imgSize"
                src="@/modules/Network/assets/orderstaus/orderstatus5_0.svg"
                alt=""
              />
              <div
                :class="[
                  'fontName width56',
                  { corHui: orderModel.orderStatus < 5 },
                  { corhei: orderModel.orderStatus >= 5 }
                ]"
              >
                审核通过
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="demand">
        <div class="row items-center line-title" style="margin-bottom: 18px;">
          <f-icon name="basic_msg_s_f" class="titleimg" />
          <span class="titlename">需求及任务信息</span>
        </div>
        <div class="flexRow">
          <f-formitem
            class="t-item"
            label="对应需求"
            profile
            :label-style="taskLabelStyle"
            full-width
          >
            <div class=" showData " :title="orderModel.demandName">
              <router-link
                class="link"
                :to="`/rqrmn/rqrProfile/${orderModel.demandId}`"
              >
                {{ orderModel.demandName }}
              </router-link>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ orderModel.demandName }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </f-formitem>
          <f-formitem
            label="涉及系统"
            class="t-item"
            profile
            :label-style="taskLabelStyle"
            full-width
          >
            <div class="showData" :title="orderModel.systemNames">
              {{ orderModel.systemNames }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ orderModel.systemNames }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </f-formitem>
          <f-formitem
            label="涉及任务"
            class="t-item"
            profile
            :label-style="taskLabelStyle"
            value-style="overflow-y: auto; max-height: 140px;"
            full-width
          >
            <div
              style="margin-bottom: 8px;"
              class="showData "
              v-for="(item, index) in orderModel.tasksInfo"
              :key="index"
            >
              <fdev-chip
                square
                class="chipSize"
                :style="{
                  background: getStatusColor(item.taskStatus),
                  color: getStatusFontColor(item.taskStatus)
                }"
                >{{ getStatusName(item.taskStatus) }}</fdev-chip
              >
              <span :title="item.taskName">
                <router-link class="link" :to="`/job/list/${item.id}`">
                  {{ item.taskName }}
                </router-link>
              </span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ item.taskName }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </f-formitem>
        </div>
      </div>
    </div>
    <div class="evaluateOpinion">
      <div class="row items-center line-title mbtm ">
        <f-icon name="basic_msg_s_f" class="titleimg" />
        <span class="titlename">审核信息</span>
      </div>
      <div class="applyInfoTitle">申请信息</div>
      <div class="applyInfoTable">
        <fdev-table
          ref="table"
          :data="tableLists"
          :columns="columns"
          hide-bottom
          :pagination="{
            rowsPerPage: 0
          }"
          no-export
          no-select-cols
        >
          <template v-slot:body-cell-applyContent="props">
            <fdev-td class="text-ellipsis">
              <div class="text-ellipsis" :title="props.row.applyContent">
                {{ props.row.applyContent }}
              </div>
            </fdev-td>
          </template>
        </fdev-table>
      </div>
      <div class="row border-bottom full-width ">
        <div class="row full-width border-top">
          <!-- 审核完成时间 -->
          <f-formitem
            class="col-12"
            label="审核完成时间"
            profile
            bottom-page
            label-auto
            label-class="q-mr-md baseLabelStyle"
            label-style="width:102px; height:auto margin-bottom:5px ; color: #999999;"
            value-class="ellipsis-3-lines"
            value-style="margin-top:5px"
          >
            <div
              class="showData ellipsis-3-lines"
              :title="orderModel.auditFinishTime"
            >
              {{ orderModel.auditFinishTime }}
            </div>
          </f-formitem>
        </div>
        <!-- 角色 -->
        <!-- <div class="row full-width border-top">
          <f-formitem
            class="col-12"
            label="审核内容"
            profile
            bottom-page
            label-auto
            label-class="q-mr-md "
            label-style="width:102px; height:auto; margin-top: 5px; margin-bottom:5px; color: #999999"
            value-class=" ellipsis-3-lines"
          >
            <div
              class="showData ellipsis-3-lines"
              :title="orderModel.auditContent"
            >
              {{ orderModel.auditContent }}
            </div>
          </f-formitem>
        </div> -->
        <div class="row full-width border-top">
          <!-- 审核结论 -->
          <f-formitem
            class="col-12"
            label="审核结论"
            profile
            bottom-page
            label-auto
            label-class="q-mr-md"
            label-style="width:102px; height:auto;  margin-top: 5px;margin-bottom:5px; color: #999999"
            value-class="ellipsis-3-lines"
          >
            <div
              class="showData ellipsis-3-lines"
              :title="orderModel.auditResult"
            >
              {{ orderModel.auditResult }}
            </div>
          </f-formitem>
        </div>
        <div class="row full-width border-top">
          <!-- 投产问题描述 -->
          <f-formitem
            class="col-12"
            label="投产问题描述"
            profile
            bottom-page
            label-auto
            label-class="q-mr-md"
            label-style="width:102px; height:auto; margin-top:5px; margin-bottom:5px; color: #999999"
            value-class=" ellipsis-3-lines"
          >
            <div
              class="showData ellipsis-3-lines"
              :title="orderModel.productProblem"
            >
              {{ orderModel.productProblem }}
            </div>
          </f-formitem>
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
    params: {
      handler(newVal) {
        this.setDemandInfo(newVal);
      },
      deep: true
    }
  },
  data() {
    return {
      orderModel: {}, //需求详情
      labelDaily:
        'width:128px;font-family: PingFangSC-Regular;font-size: 14px;color: #999999;letter-spacing: 0;line-height: 22px;',
      labelbase:
        'width:112px;font-family: PingFangSC-Regular;font-size: 14px;color: #999999;letter-spacing: 0;line-height: 22px;',
      leaddailyStyle:
        'width:142px;font-family: PingFangSC-Regular;font-size: 14px;color: #999999;letter-spacing: 0;line-height: 22px;',
      basedailyStyle:
        'width:128px;font-family: PingFangSC-Regular;font-size: 14px;color: #999999;letter-spacing: 0;line-height: 22px;',
      taskLabelStyle:
        'width:76px;font-family: PingFangSC-Regular;font-size: 14px;color: #999999;letter-spacing: 0;line-height: 22px;',
      baseLabelStyle:
        'width:102px;font-family: PingFangSC-Regular;font-size: 14px;color: #999999;letter-spacing: 0;line-height: 22px;',
      demandLabelStyle:
        'width:145px;font-family: PingFangSC-Regular;font-size: 14px;color: #999999;letter-spacing: 0;line-height: 22px;',
      demandDailyStyle:
        'width:128px;font-family: PingFangSC-Regular;font-size: 14px;color: #999999;letter-spacing: 0;line-height: 22px;',
      tableLists: [],
      columns: [
        {
          name: 'applyUser',
          label: '申请人',
          field: 'applyUser',
          headerStyle: 'width:120px',
          required: true
        },
        {
          name: 'applyTime',
          label: '申请时间',
          field: 'applyTime',
          headerStyle: 'width: 200px',
          required: true
        },
        {
          name: 'applyContent',
          label: '申请内容',
          field: 'applyContent',
          required: true,
          copy: true
        }
      ]
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
      return designStatusMap[val] ? designStatusMap[val] : '';
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
    },
    isOtherType(key) {
      if (key && key.indexOf('其他') !== -1) {
        return true;
      } else return false;
    },
    getOrderStatusColor(val) {
      const color = {
        1: '#FEC400',
        2: '#02E01A',
        3: '#4DBB59',
        4: '#00830E',
        5: '#24C8F9',
        6: '#0378EA',
        7: '#FFFFFF',
        8: '#FF740B'
      };
      return color[val];
    },
    getOrderStatusName(val) {
      if (val === 1) return '待审核';
      else if (val === 2) return '审核中';
      else if (val === 3) return '需线下复审';
      else if (val === 4) return '需会议复审';
      else if (val === 5) return '初审通过';
      else if (val === 6) return '线下复审通过';
      else if (val === 7) return '会议复审通过';
      else if (val === 8) return '拒绝';
      else return '-';
    },

    getStatusName(val) {
      if (val === 'develop') return '开发中';
      else if (val === 'sit') return 'sit测试';
      else if (val === 'uat') return 'uat测试';
      else if (val === 'rel') return 'rel测试';
      else if (val === 'production') return '已投产';
      else if (val === 'file') return '已归档';
      else if (val === 'todo') return '待实施';
      else if (val === 'create-info') return '录入信息完成';
      else if (val === 'create-app') return '录入应用完成';
      else if (val === 'create-feature') return '录入分支完成';
      else if (val === 'abort') return '已删除';
      else if (val === 'discard') return '任务已废弃';
      else return '-';
    },
    getStatusColor(val) {
      //实施状态 notStart=未开始 going=进行中 done=已完成 delete=删除
      const color = {
        'create-info': 'rgba(226,156,70,0.20)',
        'create-app': 'rgba(175,111,2,0.20)',
        'create-feature': 'rgba(217,127,17,0.20)',
        todo: 'rgba(36,200,249,0.20)',
        develop: '#FFE7CA',
        sit: 'rgba(3,120,234,0.20)',
        uat: 'rgba(67,134,202,0.20)',
        rel: 'rgba(4,72,140,0.20)',
        production: 'rgba(77,187,89,0.20)',
        file: 'rgba(140,188,72,0.20)',
        defer: 'rgba(74,102,219,0.20)',
        abort: 'rgba(244,104,101,0.20)',
        discard: 'rgba(153,153,153,0.20)'
      };
      return color[val];
    },
    getStatusFontColor(val) {
      const color = {
        'create-info': '#E29C46',
        'create-app': '#AF6F02',
        'create-feature': '#D97F11',
        todo: '#24C8F9',
        develop: '#FD8D00',
        sit: '#0378EA',
        uat: '#4386CA',
        rel: '#04488C',
        production: '#4DBB59',
        file: '#8CBC48',
        defer: '#4A66DB',
        abort: '#F46865',
        discard: '#999999'
      };
      return color[val];
    },
    setDemandInfo(data) {
      this.orderModel = data || {};
      if (this.orderModel.applyInfo) {
        this.tableLists = this.orderModel.applyInfo;
      }
    }
  }
};
</script>
<style lang="stylus" scoped>
.baseLabelStyle {
  width:102px;
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #999999;
  letter-spacing: 0;
  line-height: 22px;
}
.t-item{
  margin-bottom: 14px;
}
>>> .t-item2 .input-height.row.no-wrap.items-center.q-mr-md.justify-start.label {
  height: 22px !important;
}>>> .t-item .input-height.row.no-wrap.items-center.q-mr-md.justify-start.label {
  height: 22px !important;
}
.chipSize{
  // width: 66px;
  height: 22px;
  border-radius: 2px;
}
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
      min-width: 594px;
      width: 594px;
      .formItem{
        align-items: baseline;
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
    .applyInfoTitle{
      font-family: PingFangSC-Regular;
      font-size: 14px;
      color: rgb(153, 153, 153);
      letter-spacing: 0;
      line-height: 22px;
      font-weight: 400;
      margin-top: 16px;
    }
    .applyInfoTable{
      margin-top: 14px;
    }
    .mbtm{
      margin-bottom: 10px;
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
.border-top{
  margin-top:10px
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
  letter-spacing: 0;
  line-height: 22px;
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
.progressLine{
  margin-top: 7px;
  height: 58px;
  .lleftlabel{
    font-family: PingFangSC-Regular;
    font-size: 14px;
    color: rgb(153, 153, 153);
    letter-spacing: 0;
    line-height: 22px;
    font-weight: 400;
    height: 22px;
    width: 86px;
  }
  .lrightContent{
    margin-left: 32px;
    display: flex;
    .citem{
      text-align: center;
      .imgSize{
         height: 32px;
         width: 32px;
         border-radius: 8px;
      }
      .corHui{
         color: rgb(153, 153, 153);
         font-weight: 400;
      }
      .corhei{
         color: #333333;
         font-weight: 600;
      }
      .width42{
        width: 42px;
      }
      .width56{
        width: 56px;
      }
      .fontName{
        margin-top: 4px;
        font-family: PingFangSC-Regular;
        font-size: 14px;
        letter-spacing: 0;
        line-height: 14px;
      }
    }
    .progressItem{
      height: 58px;
      .imgPro{
        margin: 12px 6px 0px 6px;
        height: 8px;
        width: 102px;
      }
    }

  }
}
</style>
