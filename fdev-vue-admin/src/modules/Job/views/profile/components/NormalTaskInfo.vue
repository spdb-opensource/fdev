<template>
  <div class="full-width">
    <div class="icontitle row items-center">
      <f-icon
        name="basic_msg_s_f"
        class="text-primary mr12"
        :width="16"
        :height="16"
      ></f-icon>
      <span class="infoStyle">任务基础信息</span>
    </div>
    <div class="row border-bottom full-width">
      <!-- 基础信息按钮图 -->
      <div class="row full-width border-top">
        <f-formitem
          class="col-4"
          label="需求名称"
          profile
          label-auto
          bottom-page
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:43px;width:160px;"
          value-style="line-height:43px;"
          value-class="ellipsis q-px-lg"
        >
          <router-link
            :to="{ path: `/rqrmn/rqrProfile/${job.rqrmnt_no}` }"
            class="link"
            :title="job.demand && job.demand.oa_contact_name"
          >
            {{ job.demand && job.demand.oa_contact_name }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ job.demand && job.demand.oa_contact_name }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
        </f-formitem>
        <f-formitem
          class="col-4"
          label="需求编号"
          profile
          label-auto
          bottom-page
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:43px;width:160px;"
          value-style="line-height:43px;"
          value-class="ellipsis q-px-lg"
        >
          <span
            v-if="job.demand"
            class="normal-link"
            @click="routeTo(job.rqrmnt_no)"
            :title="job.demand.oa_contact_no"
          >
            {{ job.demand && job.demand.oa_contact_no }}
          </span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ job.demand && job.demand.oa_contact_no }}
            </fdev-banner>
          </fdev-popup-proxy>
        </f-formitem>
        <f-formitem
          class="col-4"
          label="研发单元"
          profile
          label-auto
          bottom-page
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:43px;width:160px;"
          value-style="line-height:43px;"
          value-class="ellipsis q-px-lg"
        >
          <span :title="job.unitNo">{{ job.unitNo }}</span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ job.unitNo }}
            </fdev-banner>
          </fdev-popup-proxy>
        </f-formitem>
      </div>
      <div class="row full-width border-top">
        <f-formitem
          class="col-4"
          label="所属小组"
          profile
          label-auto
          bottom-page
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:43px;width:160px;"
          value-style="line-height:43px;"
          value-class="ellipsis q-px-lg"
        >
          <span :title="job.groupFullName">{{ job.groupFullName }}</span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ job.groupFullName }}
            </fdev-banner>
          </fdev-popup-proxy>
        </f-formitem>
        <f-formitem
          class="col-4"
          label="任务创建人"
          profile
          label-auto
          bottom-page
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:43px;width:160px;"
          value-style="line-height:43px;"
          value-class="ellipsis q-px-lg"
        >
          <div v-if="job.creator">
            <router-link
              :to="`/user/list/${job.creator.id}`"
              class="link"
              :title="job.creator.user_name_cn"
            >
              {{ job.creator.user_name_cn }}
            </router-link>
          </div>
        </f-formitem>
        <f-formitem
          class="col-4"
          label="任务负责人"
          profile
          label-auto
          bottom-page
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:43px;width:160px;"
          value-style="line-height:43px;"
          value-class="ellipsis q-px-lg"
        >
          <router-link
            :to="`/user/list/${each.id}`"
            v-for="(each, index) in job.partyB"
            :key="index"
            class="link"
            :title="each.user_name_cn"
          >
            {{ each.user_name_cn }}
          </router-link>
        </f-formitem>
      </div>
      <div class="row full-width border-top">
        <f-formitem
          class="col-4"
          label="行内项目负责人"
          profile
          label-auto
          bottom-page
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:43px;width:160px;"
          value-style="line-height:43px;"
          value-class="ellipsis q-px-lg"
        >
          <router-link
            :to="`/user/list/${each.id}`"
            v-for="(each, index) in job.partyA"
            :key="index"
            class="link"
            :title="each.user_name_cn"
          >
            {{ each.user_name_cn }}
          </router-link>
        </f-formitem>
        <f-formitem
          class="col-4"
          label="开发人员"
          profile
          bottom-page
          label-auto
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:43px;width:160px;"
          value-style="line-height:43px;"
          value-class="ellipsis q-px-lg"
        >
          <router-link
            :to="`/user/list/${each.id}`"
            v-for="(each, index) in job.developer"
            :key="index"
            class="link"
            :title="each.user_name_cn"
          >
            {{ each.user_name_cn }}
          </router-link>
        </f-formitem>
        <f-formitem
          class="col-4"
          label="任务描述"
          profile
          label-auto
          bottom-page
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:43px;width:160px;"
          value-style="line-height:43px;"
          value-class="ellipsis q-px-lg"
        >
          <span :title="job.desc">{{ job.desc }}</span>
        </f-formitem>
      </div>
      <div class="row full-width border-top">
        <f-formitem
          v-if="job.project_name"
          class="col-4"
          label="所属应用"
          profile
          label-auto
          bottom-page
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:43px;width:160px;"
          value-style="line-height:43px;"
          value-class="ellipsis q-px-lg"
        >
          <router-link
            :to="`/app/list/${job.project_id}`"
            class="link"
            :title="job.project_name"
          >
            {{ job.project_name }}
          </router-link>
        </f-formitem>
        <f-formitem
          class="col-4"
          label="分支"
          v-if="job.feature_branch"
          bottom-page
          profile
          label-auto
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:43px;width:160px;"
          value-style="line-height:43px;"
          value-class="ellipsis q-px-lg"
        >
          <span :title="job.feature_branch">{{ job.feature_branch }}</span>
        </f-formitem>
      </div>
    </div>

    <div class="icontitle">
      <f-icon
        name="schedule_s_f"
        class="text-primary mr12"
        :width="16"
        :height="16"
      ></f-icon>
      <span class="infoStyle">任务实施安排及情况</span>
    </div>
    <div class="row border-bottom full-width">
      <div class="row full-width border-top">
        <f-formitem
          class="col-4"
          label="计划启动日期"
          profile
          label-auto
          bottom-page
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:43px;width:160px;"
          value-style="line-height:43px;"
          value-class="q-px-lg"
        >
          <span :title="job.plan_start_time">{{ job.plan_start_time }}</span>
        </f-formitem>
        <f-formitem
          class="col-4"
          label="计划完成日期"
          profile
          label-auto
          bottom-page
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:43px;width:160px;"
          value-style="line-height:43px;"
          value-class="q-px-lg"
        >
          <span :title="job.plan_fire_time">{{ job.plan_fire_time }}</span>
        </f-formitem>
      </div>
      <div class="row full-width border-top">
        <f-formitem
          class="col-4"
          label="实际启动日期"
          profile
          label-auto
          bottom-page
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:43px;width:160px;"
          value-style="line-height:43px;"
          value-class="q-px-lg"
        >
          <span :title="job.start_time">{{ job.start_time }}</span>
        </f-formitem>
        <f-formitem
          class="col-4"
          label="实际完成日期"
          profile
          label-auto
          bottom-page
          label-class="bg-indigogrey-0 q-px-lg q-py-sm self-stretch"
          label-style="height:43px;width:160px;"
          value-style="line-height:43px;"
          value-class="q-px-lg"
        >
          <span :title="job.fire_time">{{ job.fire_time }}</span>
        </f-formitem>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    job: {
      default: () => {},
      type: Object
    }
  },
  methods: {
    routeTo(id) {
      this.$router.push({ name: 'rqrProfile', params: { id: id } });
    }
  }
};
</script>

<style lang="stylus" scoped>

.infoStyle
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 16px;
  font-weight: 600;
.icontitle
  margin-top:33px;
  margin-bottom:20px;
.normal-link
  cursor: pointer;
  color: #0663BE;
.normal-link:hover
  cursor: pointer;
  color: #2196f3;
.mr12
  margin-right:12px;
border(align='all')
  border-top 1px solid #ddd if align == 'top' || align == 'all'
  border-bottom 1px solid #ddd if align == 'bottom' || align == 'all'
.border-top
  border('top')
.border-bottom
  border('bottom')
.border
  border()
</style>
