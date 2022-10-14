<template>
  <div>
    <div class="row mt20">
      <f-formitem
        v-for="(item, index) in messageList"
        :class="
          !item.value ||
          item.value === 'inner_test_result' ||
          item.value === 'systemName'
            ? 'border-bottom'
            : ''
        "
        :key="index"
        class="col-4 border-top"
        :label="item.label"
        profile
        label-auto
        bottom-page
        label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
        label-style="height:42px;width:160px;"
        value-style="line-height:42px;"
        value-class="ellipsis q-px-lg"
      >
        <div
          v-if="
            item.value === 'oa_contact_no' || item.value === 'oa_contact_name'
          "
        >
          <div :title="testOrderDetail[item.value]" class="ellipsis">
            <router-link
              v-if="testOrderDetail[item.value]"
              :to="`/rqrmn/rqrProfile/${testOrderDetail.demand_id}`"
              class="link"
            >
              {{ testOrderDetail[item.value] }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ testOrderDetail[item.value] }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else>-</span>
          </div>
        </div>
        <div class="ellipsis" v-else :title="testOrderDetail[item.value]">
          {{
            item.value
              ? testOrderDetail[item.value]
                ? testOrderDetail[item.value]
                : '-'
              : ''
          }}
          <fdev-popup-proxy
            context-menu
            v-if="
              item.value === 'impl_unit_num' ||
                item.value === 'fdev_implement_unit_no'
            "
          >
            <fdev-banner style="max-width:300px">
              {{ testOrderDetail[item.value] }}
            </fdev-banner>
          </fdev-popup-proxy>
        </div>
      </f-formitem>
    </div>
    <div class="mt20">
      <div class="mb10 row items-center">
        <f-icon
          name="bell_s_f"
          class="text-primary mr10"
          :width="16"
          :height="16"
        ></f-icon>
        <span class="infoStyle">测试内容</span>
      </div>
      <div class="row">
        <f-formitem
          v-for="(item, index) in testList"
          :class="[
            item.value === 'remark' ? 'border-bottom' : '',
            item.value === 'remark' ||
            item.value === 'test_content' ||
            item.value === 'test_environment'
              ? 'col-12'
              : 'col-4'
          ]"
          :key="index"
          class="border-top"
          :label="item.label"
          profile
          label-auto
          bottom-page
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:100%;width:160px;"
          value-style="line-height:42px;"
          value-class="ellipsis q-px-lg"
        >
          <div
            v-if="
              item.value === 'trans_interface_change' ||
                item.value === 'database_change' ||
                item.value === 'regress_test' ||
                item.value === 'client_change'
            "
          >
            {{
              testOrderDetail[item.value] === 'yes'
                ? '涉及'
                : testOrderDetail[item.value] === 'no'
                ? '不涉及'
                : '-'
            }}
          </div>
          <div
            v-else-if="
              item.value === 'test_content' || item.value === 'test_environment'
            "
            v-html="testOrderDetail[item.value]"
            style="white-space:pre-line;line-height:22px;padding: 10px 0;display:flex;align-items:center"
          ></div>
          <div v-else class="ellipsis" :title="testOrderDetail[item.value]">
            <div v-if="item.value">
              {{
                testOrderDetail[item.value] ? testOrderDetail[item.value] : '-'
              }}
              <fdev-popup-proxy
                context-menu
                v-if="
                  item.value === 'app_name' || item.value === 'test_environment'
                "
              >
                <fdev-banner style="max-width:300px">
                  {{ testOrderDetail[item.value] }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </div>
        </f-formitem>
      </div>
    </div>
    <div class="mt20">
      <div class="mb10 row items-center">
        <f-icon
          name="bell_s_f"
          class="text-primary mr10"
          :width="16"
          :height="16"
        ></f-icon>
        <span class="infoStyle">人员信息</span>
      </div>
      <div class="row">
        <f-formitem
          v-for="(item, index) in roleList"
          :class="[
            item.value === 'test_user_info' || item.value === 'submit_time'
              ? 'border-bottom'
              : '',
            item.col
          ]"
          :key="index"
          class="border-top"
          :label="item.label"
          profile
          label-auto
          bottom-page
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:100%;width:160px;"
          value-style="line-height:42px;"
          value-class="ellipsis q-px-lg"
        >
          <div
            class="row"
            v-if="
              (item.value === 'test_manager_info' ||
                item.value === 'test_cc_user_info' ||
                item.value === 'daily_cc_user_info') &&
                Array.isArray(testOrderDetail[item.value])
            "
          >
            <div
              :title="
                testOrderDetail[item.value]
                  .map(val => val.user_name_cn)
                  .join(',')
              "
              class="ellipsis"
              v-if="testOrderDetail[item.value].length > 0"
            >
              <span
                v-for="(itm, ind) in testOrderDetail[item.value]"
                :key="ind"
                :class="ind !== 0 ? 'q-ml-sm' : ''"
              >
                <span v-if="item.value === 'test_manager_info'">
                  {{ itm.user_name_cn }}
                </span>
                <router-link v-else :to="`/user/list/${itm.id}`" class="link">
                  {{ itm && itm.user_name_cn }}
                </router-link>
              </span>
            </div>
            <div v-else>-</div>
          </div>
          <div
            class="ellipsis row"
            v-else-if="
              item.value === 'test_user_info' ||
                item.value === 'create_user_info'
            "
          >
            <!-- <span>
              {{
                testOrderDetail[item.value]
                  ? testOrderDetail[item.value].user_name_cn
                  : '-'
              }}
            </span> -->
            <router-link
              v-if="testOrderDetail[item.value]"
              :to="`/user/list/${testOrderDetail[item.value].id}`"
              class="link"
            >
              {{
                testOrderDetail[item.value] &&
                  testOrderDetail[item.value].user_name_cn
              }}
            </router-link>
            <div v-else>-</div>
          </div>
          <div v-else class="ellipsis" :title="testOrderDetail[item.value]">
            {{
              testOrderDetail[item.value] ? testOrderDetail[item.value] : '-'
            }}
          </div>
        </f-formitem>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'message',
  props: ['testOrderDetail'],
  data() {
    return {
      messageList: [
        {
          label: '需求名称',
          value: 'oa_contact_name'
        },
        {
          label: '需求编号',
          value: 'oa_contact_no'
        },
        {
          label: 'IPMP实施单元',
          value: 'impl_unit_num'
        },
        {
          label: '研发单元编号',
          value: 'fdev_implement_unit_no'
        },
        {
          label: '计划提交业测日期',
          value: 'plan_test_start_date'
        },
        {
          label: '单元测试情况',
          value: 'unit_test_result'
        },
        {
          label: '内测通过情况',
          value: 'inner_test_result'
        },
        {
          label: '系统名称',
          value: 'systemName'
        },
        {
          label: '',
          value: ''
        }
      ],
      testList: [
        {
          label: '是否涉及交易接口改动',
          value: 'trans_interface_change'
        },
        {
          label: '是否涉及数据库改动',
          value: 'database_change'
        },
        {
          label: '是否涉及回归测试',
          value: 'regress_test'
        },
        {
          label: '是否涉及客户端更新',
          value: 'client_change'
        },
        {
          label: '需求涉及的应用名称',
          value: 'app_name'
        },
        {
          label: '具体回归测试范围',
          value: 'regress_test_range'
        },
        {
          label: '客户端下载地址',
          value: 'client_download'
        },
        {
          label: '涉及关联系统同步改造',
          value: 'system'
        },
        {
          label: '',
          value: ''
        },
        {
          label: '测试环境',
          value: 'test_environment'
        },
        {
          label: '测试内容',
          value: 'test_content'
        },
        {
          label: '备注',
          value: 'remark'
        }
      ],
      roleList: [
        {
          label: '测试经理',
          col: 'col-12',
          value: 'test_manager_info'
        },
        {
          label: '提测邮件通知抄送人员',
          col: 'col-12',
          value: 'test_cc_user_info'
        },
        {
          label: '测试日报抄送人员',
          col: 'col-12',
          value: 'daily_cc_user_info'
        },
        {
          label: '业务人员邮箱',
          col: 'col-12',
          value: 'business_email'
        },
        {
          label: '开发人员',
          col: 'col-12',
          value: 'developer'
        },
        {
          label: '创建人',
          col: 'col-4',
          value: 'create_user_info'
        },
        {
          label: '创建时间',
          col: 'col-8',
          value: 'create_time'
        },
        {
          label: '提交人',
          col: 'col-4',
          value: 'test_user_info'
        },
        {
          label: '提交时间',
          col: 'col-8',
          value: 'submit_time'
        }
      ]
    };
  }
};
</script>

<style lang="stylus" scoped>
border(align='all')
  border-top 1px solid #ddd if align == 'top' || align == 'all'
  border-bottom 1px solid #ddd if align == 'bottom' || align == 'all'
.border-top
  border('top')
.border-bottom
  border('bottom')
.border
  border()
.mt20
  margin-top 20px
.mr10
  margin-right 10px
.mb10
  margin-bottom 10px
.infoStyle
  font-size 14px
  font-weight 600
</style>
