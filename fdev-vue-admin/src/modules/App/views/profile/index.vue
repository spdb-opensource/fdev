<template>
  <f-block>
    <fdev-tabs v-model="tab">
      <fdev-tab name="mails" label="应用概述" />
      <fdev-tab name="alarms" label="关联事项" v-if="!appinfo.status" />
      <fdev-tab name="sonar" label="代码分析" v-if="isSonarShow" />
      <fdev-tab name="deployMessage" label="部署信息" v-if="!examLabel" />
      <fdev-tab
        name="interfaceList"
        @click="queryInterface"
        label="接口列表"
        v-if="!appinfo.status"
      />
      <Authorized :include-me="managerArr" v-if="!appinfo.status">
        <fdev-tab name="automatedTest" label="自动化测试" />
      </Authorized>
      <fdev-tab name="pipelines" label="持续集成" v-if="!appinfo.status" />
    </fdev-tabs>

    <fdev-separator />

    <fdev-tab-panels v-model="tab">
      <fdev-tab-panel name="mails">
        <div class="text-h6 q-mt-md">应用概述</div>
        <div class="row justify-center q-mt-md">
          <f-formitem class="col-6 mails-width" diaS label="中文名称：">
            {{ appinfo.name_zh }}
          </f-formitem>
          <f-formitem class="col-6 mails-width" diaS label="英文名称：">
            {{ appinfo.name_en }}
          </f-formitem>
          <f-formitem class="col-6 mails-width" diaS label="行内应用负责人：">
            <div class="q-gutter-x-sm">
              <span v-for="(item, index) in appinfo.spdb_managers" :key="index">
                <router-link
                  v-if="item.id"
                  :to="{ path: `/user/list/${item.id}` }"
                  class="link"
                >
                  {{ item.user_name_cn }}
                </router-link>
                <span v-else>{{ item.user_name_cn }}</span>
              </span>
            </div>
          </f-formitem>
          <f-formitem class="col-6 mails-width" diaS label="应用负责人：">
            <div class="q-gutter-x-sm">
              <span v-for="(item, index) in appinfo.dev_managers" :key="index">
                <router-link
                  v-if="item.id"
                  :to="{ path: `/user/list/${item.id}` }"
                  class="link"
                >
                  {{ item.user_name_cn }}
                </router-link>
                <span v-else>{{ item.user_name_cn }}</span>
              </span>
            </div>
          </f-formitem>
          <f-formitem class="col-6 mails-width" diaS label="应用类型：">
            {{ appinfo.type_name }}
          </f-formitem>
          <f-formitem class="col-6 mails-width" diaS label="应用所属系统：">
            <span v-if="appinfo.system">
              {{ appinfo.system.name }}
            </span>
          </f-formitem>
          <f-formitem class="col-6 mails-width" diaS label="创建时间：">
            {{ appinfo.createtime }}
          </f-formitem>
          <f-formitem class="col-6 mails-width" diaS label="所属小组：">
            {{ appinfo.group.name }}
          </f-formitem>
          <f-formitem
            class="col-6 mails-width"
            diaS
            label="部署网段："
            v-if="!examLabel"
          >
            {{ appinfo.network }}
          </f-formitem>
          <f-formitem class="col-6 mails-width" diaS label="gitlab仓库：">
            <a
              target="_blank"
              :href="appinfo.git"
              v-if="appinfo.git !== 'devops'"
            >
              {{ appinfo.git }}
            </a>
            <span v-else>无</span>
          </f-formitem>
          <f-formitem
            class="col-6 mails-width"
            diaS
            label="SIT部署环境："
            v-if="!examLabel"
          >
            <span v-if="appinfo.sit">
              {{ appinfo.sit[0].auto_env_name }}
            </span>
          </f-formitem>
          <f-formitem
            class="col-6 mails-width"
            diaS
            label="SIT定时部署环境："
            v-if="!examLabel"
          >
            <span v-if="appinfo.sit">
              {{ appinfo.sit[0].schedule_env_name }}
            </span>
          </f-formitem>
          <f-formitem class="col-6 mails-width" diaS label="标签：">
            <fdev-chip
              v-for="(label, index) in appinfo.labels"
              :key="index"
              square
              color="teal"
              text-color="white"
              class="q-mt-none"
            >
              {{ label }}
            </fdev-chip>
          </f-formitem>
          <f-formitem
            class="col-6 mails-width"
            diaS
            label="定时部署："
            v-if="appinfo.appCiType !== 'fdev-ci'"
          >
            <span class="text-grey-8">
              {{
                createPipelineScheduleModel.autoSwitch === 'true' ? '开' : ' 关'
              }}
            </span>
            <Authorized
              :include-me="managerArr"
              v-if="!appinfo.status"
              class="inline-block"
            >
              <fdev-toggle
                v-model="createPipelineScheduleModel.autoSwitch"
                @input="handlePipelineScheduleOpen"
                left-label
                true-value="true"
                false-value="false"
              />
            </Authorized>
          </f-formitem>
          <f-formitem class="col-6 mails-width" diaS label="sonar扫描卡点：">
            {{ appinfo.sonar_scan_switch === '1' ? '开' : ' 关' }}
          </f-formitem>
          <f-formitem class="col-6 mails-width" diaS label="持续集成方式：">
            {{ appinfo.appCiType === 'fdev-ci' ? 'fdev-ci' : 'git-ci' }}
          </f-formitem>
          <f-formitem class="col-6 mails-width" diaS label="是否涉及内测：">
            {{ appinfo.isTest === '1' ? '是' : '否' }}
          </f-formitem>
          <f-formitem class="col-6 mails-width" diaS label="应用描述：">
            <span v-html="descFilter(appinfo.desc)" />
          </f-formitem>
          <f-formitem class="col-6 mails-width" diaS label="">
            <!-- 占位置 -->
          </f-formitem>
        </div>

        <div class="row justify-center q-gutter-x-sm q-mt-md">
          <Authorized :include-me="managerArr" v-if="!appinfo.status">
            <fdev-btn
              ficon="edit"
              label="编辑"
              @click="handleSummary('应用概述')"
            />
          </Authorized>
          <fdev-btn
            label="环境配置参数"
            v-if="!appinfo.status"
            @click="handleModelSettingOpen"
          />
          <Authorized :include-me="managerArr" v-if="!appinfo.status">
            <fdev-btn label="删除" @click="handleDeleteApp" ficon="delete" />
          </Authorized>
          <fdev-btn
            label="查询生产信息"
            @click="goProInfoDetails"
            color="primary"
          />
        </div>
      </fdev-tab-panel>

      <fdev-tab-panel name="alarms">
        <div class="text-h6 q-mt-md">SIT测试缺陷汇总：</div>
        <Count :defectList="mantisList" :fromApp="true" />
        <fdev-table
          title="任务列表"
          title-icon="todo_list_s_f"
          :data="data"
          :columns="columns"
          row-key="id"
          :filter="filter"
          noExport
          no-select-cols
        >
          <template v-slot:top-bottom>
            <f-formitem label="搜索条件">
              <fdev-input :value="filter" @input="updateFilter($event)">
                <template v-slot:append>
                  <f-icon name="search" class="cursor-pointer" />
                </template>
              </fdev-input>
            </f-formitem>
          </template>
          <template v-slot:body-cell-name="props">
            <fdev-td class="text-ellipsis" :title="props.value">
              <router-link :to="`/job/list/${props.row.id}`" class="link">
                {{ props.value }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.value }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </fdev-td>
          </template>
          <template v-slot:body-cell-master="props">
            <fdev-td class="text-ellipsis">
              <span v-for="(item, index) in props.value" :key="index">
                <router-link
                  v-if="item.user_name_cn"
                  :to="{ path: `/user/list/${item.id}` }"
                  class="link"
                >
                  {{ item.user_name_cn }}
                </router-link>
              </span>
            </fdev-td>
          </template>
        </fdev-table>
      </fdev-tab-panel>

      <fdev-tab-panel name="automatedTest">
        <AuthmatedTest :gitlab_project_id="appinfo.gitlab_project_id" />
      </fdev-tab-panel>

      <fdev-tab-panel name="sonar" class="q-mt-md">
        <Sonar :gitlab_id="appinfo.gitlab_project_id" />
      </fdev-tab-panel>

      <fdev-tab-panel name="interfaceList">
        <Loading :visible="loading">
          <fdev-table
            title="接口列表"
            title-icon="list_s_f"
            :data="tableData"
            :columns="interfaceColumns"
            row-key="id"
            :selected.sync="interfaceArr"
            selection="multiple"
            :loading="loading"
            @request="queryInterface"
            :visible-columns="visibleColumns"
            :onSelectCols="updateVisibleColumns"
            :pagination.sync="pagination"
            noExport
            class="q-mt-md"
          >
            <template v-slot:top-right>
              <fdev-btn
                normal
                ficon="eye"
                @click="handleScanDialogOpen"
                label="扫描"
                v-if="appManagerLimit.limit"
              />
              <div>
                <fdev-btn
                  normal
                  @click="createUrlOpen"
                  label="生成链接"
                  ficon="iteration"
                  :loading="globalLoading['interfaceForm/getInterfacesUrl']"
                  :disable="interfaceArr.length === 0"
                />
                <fdev-tooltip v-if="interfaceArr.length === 0"
                  >请至少选择一条数据</fdev-tooltip
                >
              </div>
            </template>
            <template v-slot:top-bottom>
              <f-formitem class="col-4" bottom-page label="提供报文类型">
                <fdev-select
                  :options="typeOptions"
                  emit-value
                  map-options
                  option-label="label"
                  option-value="value"
                  :value="interfaceType"
                  @input="updateInterfaceType($event)"
                />
              </f-formitem>
              <f-formitem class="col-4" bottom-page label="接口名称">
                <fdev-input
                  ref="interfaceName"
                  :value="interfaceName"
                  @input="updateInterfaceName($event)"
                  @keyup.enter.native="queryInterface"
                >
                  <template v-slot:append>
                    <f-icon
                      name="search"
                      class="cursor-pointer"
                      @click="queryInterface"
                    />
                  </template>
                </fdev-input>
              </f-formitem>
              <f-formitem class="col-4" bottom-page label="分支名">
                <fdev-select
                  use-input
                  emit-value
                  option-label="name"
                  option-value="name"
                  map-options
                  @filter="filterFn"
                  :options="filterGitlabBranch"
                  :value="branchName"
                  @input="updateBranchName($event)"
                />
              </f-formitem>
              <f-formitem class="col-4" bottom-page :label="label">
                <fdev-input
                  placeholder="可输入多个，以英文‘,’隔开"
                  ref="transactionCode"
                  :value="transactionCode"
                  @input="updateTransactionCode($event)"
                  @keyup.enter.native="queryInterface"
                >
                  <template v-slot:append>
                    <f-icon
                      name="search"
                      class="cursor-pointer"
                      @click="queryInterface"
                    />
                  </template>
                </fdev-input>
              </f-formitem>
            </template>
            <template v-slot:body-cell-transId="props">
              <fdev-td :title="props.row.transId">
                <router-link
                  :to="{
                    path: `/interfaceAndRoute/interface/interfaceProfile/${
                      props.row.id
                    }`,
                    query: { interfaceType: props.row.interfaceType }
                  }"
                  class="link"
                >
                  <span>{{ props.row.transId }}</span>
                  <fdev-popup-proxy context-menu>
                    <fdev-banner style="max-width:300px">
                      {{ props.row.transId }}
                    </fdev-banner>
                  </fdev-popup-proxy>
                </router-link>
              </fdev-td>
            </template>
          </fdev-table>
        </Loading>
      </fdev-tab-panel>

      <fdev-tab-panel name="deployMessage">
        <Loading :visible="loading" class="q-mt-lg">
          <div class="row justify-end ">
            <Authorized
              :include-me="managerArr"
              or
              :role-authority="['环境配置管理员']"
            >
              <fdev-btn
                normal
                @click="
                  $router.push({
                    name: 'DeployMessageHandlePage',
                    query: { appId: appId }
                  })
                "
                :ficon="!btnLabel ? 'edit' : 'add'"
                :label="!btnLabel ? '编辑' : '新增部署信息'"
              />
            </Authorized>
          </div>
          <StepFour isAppProfile ref="deployMessage" />
        </Loading>
      </fdev-tab-panel>

      <fdev-tab-panel name="pipelines">
        <Loading :visible="loading">
          <configCI
            ref="configCI"
            :applicationId="appId"
            :appInfo="applicationInfo"
          />
        </Loading>
      </fdev-tab-panel>
    </fdev-tab-panels>
    <!-- 以下是弹窗，根据点击判断显示 -->
    <f-dialog
      v-model="openSummary"
      @shake="confirmToClose('openSummary')"
      @before-show="initDialog"
      :title="`编辑${title}`"
      right
    >
      <div v-if="title === '应用概述'">
        <f-formitem diaS required label="中文名称">
          <fdev-input
            ref="summaryModel.name_zh"
            v-model="$v.summaryModel.name_zh.$model"
            :rules="[
              () => $v.summaryModel.name_zh.required || '中文名称不能为空'
            ]"
          />
        </f-formitem>
        <f-formitem diaS class="edit-form-cells" label="英文名称">
          <fdev-input
            readonly
            ref="summaryModel.name_en"
            v-model="$v.summaryModel.name_en.$model"
          />
        </f-formitem>
        <f-formitem diaS required label="行内应用负责人">
          <fdev-select
            multiple
            use-input
            map-options
            emit-value
            :option-value="opt => opt"
            option-label="user_name_cn"
            ref="summaryModel.spdb_managers"
            v-model="$v.summaryModel.spdb_managers.$model"
            :options="options"
            @filter="spdbManagersFilter"
            :rules="[
              () =>
                $v.summaryModel.spdb_managers.required ||
                '行内应用负责人不能为空'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label caption :title="scope.opt.user_name_en">
                    {{ scope.opt.user_name_en }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem diaS required label="应用负责人">
          <fdev-select
            multiple
            use-input
            map-options
            emit-value
            :option-value="opt => opt"
            option-label="user_name_cn"
            ref="summaryModel.dev_managers"
            v-model="$v.summaryModel.dev_managers.$model"
            :options="options"
            @filter="spdbManagersFilter"
            :rules="[
              () =>
                $v.summaryModel.dev_managers.required || '应用负责人不能为空'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">
                    {{ scope.opt.user_name_cn }}
                  </fdev-item-label>
                  <fdev-item-label caption :title="scope.opt.user_name_en">
                    {{ scope.opt.user_name_en }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem diaS required label="应用类型">
          <fdev-tooltip position="top" v-if="jobs.length > 0">
            该应用有关联事项时，不允许修改应用类型！
          </fdev-tooltip>
          <fdev-select
            option-label="label"
            option-value="value"
            map-options
            ref="summaryModel.appType"
            v-model="summaryModel.appType"
            :options="appTypeData"
            :disable="jobs.length > 0"
            :rules="[
              () => $v.summaryModel.appType.required || '请选择应用类型'
            ]"
          />
        </f-formitem>
        <f-formitem diaS required label="应用所属系统">
          <fdev-select
            ref="summaryModel.system"
            v-model="$v.summaryModel.system.$model"
            :options="filterappSystem"
            @filter="appsystemInputFilter"
            option-label="name"
            option-value="id"
            use-input
            :rules="[
              () => !$v.summaryModel.system.$error || '应用所属系统不能为空'
            ]"
          />
        </f-formitem>
        <f-formitem diaS label="创建时间" class="edit-form-cells">
          <fdev-input
            readonly
            ref="summaryModel.createtime"
            v-model="$v.summaryModel.createtime.$model"
          />
        </f-formitem>
        <f-formitem diaS required label="所属小组">
          <fdev-select
            ref="summaryModel.group"
            v-model="$v.summaryModel.group.$model"
            option-label="fullName"
            option-value="name"
            use-input
            :options="groupsFilterList"
            @filter="filterGroups"
            :rules="[
              () => $v.summaryModel.group.required || '所属小组不能为空'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name">{{
                    scope.opt.name
                  }}</fdev-item-label>
                  <fdev-item-label caption :title="scope.opt.fullName">
                    {{ scope.opt.fullName }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem
          diaS
          label="部署网段"
          class="edit-form-cells"
          v-if="appinfo.network"
        >
          <fdev-option-group
            name="accepted_genres"
            v-model="$v.summaryModel.network.$model"
            :options="networkOptions"
            type="checkbox"
            color="primary"
            inline
            disable
          />
        </f-formitem>
        <f-formitem diaS label="gitlab仓库">
          <fdev-input
            :title="summaryModel.git"
            ref="summaryModel.git"
            readonly
            v-model="$v.summaryModel.git.$model"
            :rules="[
              () => $v.summaryModel.git.required || 'gitlab仓库不能为空'
            ]"
          />
        </f-formitem>
        <f-formitem
          diaS
          required
          label="SIT部署环境"
          v-if="appinfo.network && summaryModel.appCiType !== 'fdev-ci'"
        >
          <fdev-select
            use-input
            option-label="name_en"
            option-value="id"
            ref="summaryModel.auto"
            v-model="$v.summaryModel.auto.$model"
            @filter="sitEnvFilter"
            :options="eitEnvOptions"
            :rules="[() => $v.summaryModel.auto.required || '应用环境不能为空']"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name_en">
                    {{ scope.opt.name_en }}
                  </fdev-item-label>
                  <fdev-item-label caption :title="scope.opt.name_cn">
                    {{ scope.opt.name_cn }}
                  </fdev-item-label>
                </fdev-item-section>
                <fdev-item-section side>
                  <fdev-chip
                    dense
                    flat
                    square
                    class="text-white"
                    :color="
                      scope.opt.labels.includes('biz') ? 'green-5' : 'orange-5'
                    "
                  >
                    {{ scope.opt.labels.includes('biz') ? '业务' : '网银' }}
                  </fdev-chip>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem
          diaS
          required
          label="SIT定时部署环境"
          v-if="appinfo.network && summaryModel.appCiType !== 'fdev-ci'"
        >
          <fdev-select
            use-input
            option-label="name_en"
            option-value="id"
            ref="summaryModel.schedule"
            v-model="$v.summaryModel.schedule.$model"
            @filter="sitEnvFilter"
            :options="eitEnvOptions"
            :rules="[
              () => $v.summaryModel.schedule.required || '请选择SIT定时部署环境'
            ]"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name_en">
                    {{ scope.opt.name_en }}
                  </fdev-item-label>
                  <fdev-item-label caption :title="scope.opt.name_cn">
                    {{ scope.opt.name_cn }}
                  </fdev-item-label>
                </fdev-item-section>
                <fdev-item-section side>
                  <fdev-chip
                    dense
                    flat
                    square
                    class="text-white"
                    :color="
                      scope.opt.labels.includes('biz') ? 'green-5' : 'orange-5'
                    "
                  >
                    {{ scope.opt.labels.includes('biz') ? '业务' : '网银' }}
                  </fdev-chip>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem diaS label="重要度(选填)" class="edit-form-cells">
          <fdev-select
            ref="summaryModel.importance"
            v-model="$v.summaryModel.importance.$model"
            :options="importanceList"
          />
        </f-formitem>
        <f-formitem diaS required label="持续集成方式">
          <fdev-select
            :options="appCiTypeList"
            v-model="$v.summaryModel.appCiType.$model"
            :rules="[
              () => $v.summaryModel.appCiType.required || '请选择持续集成方式'
            ]"
          />
        </f-formitem>
        <f-formitem
          diaS
          label="是否涉及环境部署"
          v-if="summaryModel.appCiType === 'git-ci'"
          class="edit-form-cells"
        >
          <fdev-radio
            val="涉及环境部署"
            v-model="$v.summaryModel.env_deploy.$model"
            label="是"
            @input="handleEnvDeploy('是')"
          />
          <fdev-radio
            val="不涉及环境部署"
            v-model="$v.summaryModel.env_deploy.$model"
            label="否"
            class="q-ml-lg"
            @input="handleEnvDeploy('否')"
          />
        </f-formitem>
        <f-formitem diaS label="sonar扫描卡点" class="edit-form-cells">
          <fdev-radio
            val="1"
            v-model="$v.summaryModel.sonar_scan_switch.$model"
            label="开"
          />
          <fdev-radio
            val="0"
            v-model="$v.summaryModel.sonar_scan_switch.$model"
            label="关"
            class="q-ml-lg"
          />
        </f-formitem>
        <f-formitem diaS label="是否涉及内测" class="edit-form-cells">
          <fdev-radio
            val="1"
            v-model="$v.summaryModel.isTest.$model"
            label="是"
          />
          <fdev-radio
            val="0"
            v-model="$v.summaryModel.isTest.$model"
            label="否"
            class="q-ml-lg"
          />
        </f-formitem>
        <f-formitem diaS label="标签(选填)" class="edit-form-cells">
          <fdev-select
            use-input
            multiple
            ref="summaryModel.labels"
            v-model="$v.summaryModel.labels.$model"
            hide-dropdown-icon
            @new-value="addSelect"
            @remove="removeEnvDep"
          >
            <template v-slot:selected-item="scope">
              <fdev-chip
                :removable="
                  scope.opt !== '不涉及环境部署' ||
                    summaryModel.appCiType === 'git-ci'
                "
                @remove="scope.removeAtIndex(scope.index)"
                :tabindex="scope.tabindex"
              >
                {{ scope.opt }}
              </fdev-chip>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem diaS label="应用描述(选填)" class="edit-form-cells">
          <fdev-input
            ref="summaryModel.desc"
            v-model="$v.summaryModel.desc.$model"
            type="textarea"
            rows="2"
          />
        </f-formitem>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="openSummary = false"/>
        <fdev-btn
          label="确定"
          dialog
          @click="handleSummaryModel"
          :loading="globalLoading['appForm/appUpdate']"
      /></template>
    </f-dialog>
    <!-- 查看外部配置参数 -->
    <fdev-dialog
      v-model="modelSettingOpened"
      persistent
      :maximized="maximizedToggle"
    >
      <fdev-layout view="Lhh lpR fff" container class="bg-white">
        <fdev-header class="q-pt-sm q-pb-sm bg-cyan-6">
          <fdev-bar class="bg-cyan-6 text-white">
            <div>
              <fdev-btn
                ficon="alert_c_o"
                label="优先生效配置参数"
                @click="handleExtreConfigDialogOpen"
                normal
              >
              </fdev-btn>
              <fdev-tooltip position="top">
                只是临时测试使用，长期使用需要配置实体参数。
              </fdev-tooltip>
            </div>

            <fdev-space />
            <fdev-btn dense flat icon="close" v-close-popup />
          </fdev-bar>
        </fdev-header>
        <div class="property-editor">
          <div class="row editor-title">
            <div class="col-6" diaS>
              <span class="q-pl-lg">编辑外部配置模板</span>
            </div>
            <div class="col-6" diaS>
              预览各环境配置文件
              <fdev-select
                use-input
                @filter="envFilter"
                ref="env"
                type="text"
                class="float-right"
                placeholder="环境"
                v-model="env"
                :options="filterEnvList"
                option-label="name_en"
                option-value="name_en"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.name_en">
                        {{ scope.opt.name_en }}
                      </fdev-item-label>
                      <fdev-item-label caption :title="scope.opt.name_cn">
                        {{ scope.opt.name_cn }}
                      </fdev-item-label>
                    </fdev-item-section>
                    <fdev-item-section
                      side
                      v-if="
                        scope.opt.labels.includes('biz') ||
                          scope.opt.labels.includes('dmz')
                      "
                    >
                      <fdev-chip
                        flat
                        square
                        class="text-white"
                        :color="
                          scope.opt.labels.includes('biz')
                            ? 'green-5'
                            : 'orange-5'
                        "
                      >
                        {{ scope.opt.labels.includes('biz') ? '业务' : '网银' }}
                      </fdev-chip>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </div>
          </div>
          <div class="row editor-warpper config">
            <PropertyEditor
              v-model="editor"
              ref="propertyEdit"
              :properties="modelProperties"
              :disable="permission"
              @noPermission="changeStatus"
              @error="handleError"
            />
          </div>
        </div>
      </fdev-layout>
    </fdev-dialog>

    <f-dialog v-model="confirmEnv" title="提示">
      <div class="row justify-center">
        <div>
          您的应用尚未绑定过部署信息，请点击
          <a class="span-link" @click="goCheckEnv" target="_blank"> 去绑定 </a>
        </div>
      </div>
    </f-dialog>

    <f-dialog v-model="confirmCI" title="提示">
      <div class="row justify-center">
        <div>
          您的应用尚未配置流水线，请点击
          <a class="span-link" @click="goCheckCI" target="_blank"> 去配置 </a>
        </div>
      </div>
    </f-dialog>
    <!-- 生成链接弹窗 -->
    <f-dialog v-model="createUrlDialogOpen" title="生成链接">
      <div class="row justify-center ">
        <div>{{ createdUrl }}</div>
      </div>
      <template v-slot:btnSlot
        ><fdev-btn
          label="取消"
          dialog
          outline
          @click="createUrlDialogOpen = false"/>
        <fdev-btn label="复制" dialog @click="createUrl"
      /></template>
    </f-dialog>

    <ExtreConfigParam
      :extreConfigDialogOpened.sync="extreConfigDialogOpened"
      :extraConfig="extraConfigParam[0]"
      @listenExtreConfigEvent="changeExtreConfigEvent"
      :noWrite="noWrite"
      :project_id="appId"
      :branch="appBranch"
    >
    </ExtreConfigParam>
    <ScanDialog
      v-model="scanDialogOpen"
      :appId="appinfo.id"
      :appName="appinfo.name_en"
      @click="handleScanDialog"
      @change="handleChangeBranchName"
    />
  </f-block>
</template>

<script>
import { mapState, mapGetters, mapActions, mapMutations } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import {
  validate,
  successNotify,
  deepClone,
  errorNotify,
  getIdsFormList,
  baseUrl
} from '@/utils/utils';
import configCI from '@/modules/App/views/pipelines/configCI';
import ExtreConfigParam from '@/components/ExtraConfigParam';
import PropertyEditor from '@/components/PropertyEditor';
import Loading from '@/components/Loading';
import AuthmatedTest from '@/components/AutomatedTest';
import {
  createAppModel,
  createSummaryModel,
  createPipelineScheduleModel,
  interfaceColumns,
  profileTaskColumns
} from '@/modules/App/utils/constants';
import {
  createInterfaceModel,
  typeOptions
} from '@/modules/interface/utils/constants';
import ScanDialog from '@/modules/interface/components/scanDialog';
import Sonar from '@/modules/App/components/Sonar/Sonar';
import Count from '@/components/Count';
import Authorized from '@/components/Authorized';
import StepFour from '@/modules/Environment/views/deployMessage/StepFour';
import { networkOptions } from '@/modules/Environment/utils/constants';
export default {
  name: 'AppProfile',
  components: {
    configCI,
    ExtreConfigParam,
    PropertyEditor,
    ScanDialog,
    Loading,
    AuthmatedTest,
    Count,
    Sonar,
    StepFour,
    Authorized
  },
  data() {
    return {
      createUrlDialogOpen: false,
      createdUrl: '',
      createPipelineScheduleModel: createPipelineScheduleModel(),
      tab: 'mails',
      title: '',
      isTest: '',
      appinfo: createAppModel(),
      scanDialogOpen: false,
      typeOptions: typeOptions,
      filterappSystem: [],
      cloneAlarmColumns: ['name', 'redmine_id', 'stage', 'group', 'master'],

      interfaceColumns: interfaceColumns(),
      data: [],
      groupsJob: [],
      groupNamesJob: '',
      openSummary: false,
      summaryModel: createSummaryModel(),
      archetypes: [],
      options: [],
      interfaceModel: createInterfaceModel(),
      importanceList: ['关键', '一般', '重要'],
      appCiTypeList: ['git-ci', 'fdev-ci'],
      deepCloneGroups: [],
      extreConfigDialogOpened: false,
      noWrite: true,
      modelSettingOpened: false,
      maximizedToggle: true,
      editor: '',
      modelProperties: [],
      env: null,
      managerIds: [],
      interfaceArr: [],
      loading: false,
      filterEnvList: [],
      filterGitlabBranch: [],
      groupsFilterList: [],
      modelHtml: '',
      nameEnList: [],
      toggleTool: true,
      modelNameEn: null,
      appId: '',
      appBranch: 'master',
      isSonarShow: '', //是否显示sonar
      pagination: {
        rowsPerPage: 5,
        rowsNumber: 0,
        page: 1
      },
      tableData: [],
      permission: false,
      networkOptions: networkOptions,
      eitEnvOptions: [],
      confirmEnv: false,
      confirmCI: false,
      originModelProperties: [],
      filterModelList: [],
      applicationInfo: {},
      goPipelines: false,
      pipelinesReady: false
    };
  },
  validations: {
    summaryModel: {
      auto: {
        required(val) {
          if (this.appinfo.network) {
            return !!val;
          }
          return true;
        }
      },
      schedule: {
        required(val) {
          if (this.appinfo.network) {
            return !!val;
          }
          return true;
        }
      },
      importance: {},
      appCiType: {
        required
      },
      labels: {},
      name_zh: {
        required
      },
      name_en: {},
      desc: {},
      spdb_managers: {
        required
      },
      isTest: {
        required
      },
      dev_managers: {
        required
      },
      createtime: {},
      group: {
        required
      },
      network: {},
      git: {
        required
      },
      appType: {
        required
      },
      env_deploy: {},
      sonar_scan_switch: {},
      system: {
        required
      }
    }
  },
  watch: {
    'summaryModel.importance': {
      handler(newVal, oldVal) {
        let labels = this.summaryModel.labels ? this.summaryModel.labels : [];
        let newLabels = [];
        labels.forEach(label => {
          if (this.importanceList.indexOf(label) === -1) newLabels.push(label);
        });
        labels = newLabels;
        if (newVal) labels.push(newVal);
        this.summaryModel.labels = labels;
      },
      deep: true
    },
    'summaryModel.appCiType': {
      handler: function(val, oldVal) {
        if (val === 'git-ci') {
          this.summaryModel.env_deploy = '涉及环境部署';
          const index = this.summaryModel.labels.indexOf('不涉及环境部署');
          this.summaryModel.labels.splice(index, 1);
          return;
        }
        if (!val || val === 'fdev-ci') {
          if (!this.summaryModel.labels.includes('不涉及环境部署')) {
            this.summaryModel.labels.push('不涉及环境部署');
          }
        }
      }
    },
    'summaryModel.labels': {
      handler(newVal, oldVal) {
        if (
          this.summaryModel.appCiType !== 'git-ci' &&
          !newVal.includes('不涉及环境部署') &&
          oldVal.includes('不涉及环境部署')
        ) {
          this.summaryModel.labels.push('不涉及环境部署');
        }
        let isExistImportance = newVal.some(label => {
          return this.importanceList.includes(label);
        });
        if (!isExistImportance) {
          this.summaryModel.importance = '';
        }
      },
      deep: true
    },
    async tab(val) {
      this.loading = true;
      // 任务列表
      if (this.tab === 'alarms') {
        await this.fetchGroup();
        await this.queryMantis({ id: this.appId });
      } else if (this.tab === 'deployMessage') {
        await this.queryDeploy({
          app_id: this.appId
        });
        if (this.$refs.deployMessage) {
          this.$refs.deployMessage.appProfileinit();
        }
      }
      this.loading = false;
    },
    modelSettingOpened(val) {
      this.permission = !val ? false : this.permission;
    },
    env() {
      this.getPreviewFile();
    },
    modelNameEn(val) {
      if (val === null) {
        val = '';
        this.modelHtml = '';
      }
      if (typeof val === 'object') {
        if (!val.name_cn) {
          return;
        }
        this.modelHtml = '';
        let envNameCn = val.name_cn;
        let envNameEn = val.name_en;
        let envKeys = val.env_key;
        let modelHtmlStart = '## ' + envNameCn + '\n';
        envKeys.forEach(key => {
          let modelDesc = key.name_cn;
          let modelKey = `${envNameEn}.${key.name_en}`;
          this.modelHtml += '# ' + modelDesc + '\n';
          this.modelHtml += modelKey + '\n';
        });
        let modelHtmlEnd = '';
        this.modelHtml = modelHtmlStart + this.modelHtml + modelHtmlEnd;
      }
    },

    pipelinesReady(val) {
      if (val && this.goPipelines) {
        //是应用负责人的话跳转到持续集成tab页的项目流水线子tab页
        if (!this.managerArr.includes(this.currentUser.id)) {
          this.tab = 'pipelines';
        } else {
          this.tab = 'pipelines';
          this.$nextTick(() => {
            this.$refs.configCI.tabpanel = 'proPipelines';
          });
        }
        this.goPipelines = false;
        this.pipelinesReady = false;
      }
    },
    interfaceType(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.queryInterface();
      }
    },
    interfaceName(val) {
      if (val === '') {
        this.queryInterface();
      }
    },
    branchName(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.queryInterface();
      }
    },
    transactionCode(val) {
      if (val === '') {
        this.queryInterface();
      }
    }
  },
  computed: {
    ...mapState('userActionSaveApp/appProfile', [
      'transactionCode',
      'branchName',
      'interfaceName',
      'interfaceType',
      'filter',
      'visibleColumns'
    ]),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('userForm', ['groups']),
    ...mapState('appForm', {
      testFlag: 'testFlag',
      interfaceData: 'interfaceData',
      appData: 'appData',
      mantisList: 'mantisList',
      sonarChart: 'sonarChart',
      appTypeData: 'appTypeData',
      appSystemList: 'appSystemList'
    }),
    ...mapState('environmentForm', {
      envListById: 'envList',
      defaultDeployTemplate: 'defaultDeployTemplate',
      deployDetail: 'deployDetail',
      envFilterByLabels: 'envFilterByLabels'
    }),
    ...mapState('user', {
      userList: 'list',
      currentUser: 'currentUser'
    }),
    ...mapState('jobForm', [
      'configTemplate',
      'envList',
      'envListByAppId',
      'previewFile',
      'extraConfigParam',
      'jobs',
      'modelList',
      'pirvateModelList'
    ]),
    ...mapState('interfaceForm', [
      'url',
      'interfaceList',
      'gitlabBranch',
      'appManagerLimit'
    ]),
    ...mapState('configCIForm', ['AppPipelineList']),
    ...mapGetters('user', ['isLoginUserList']),
    label() {
      return this.interfaceType === 'SOAP' ? '服务ID+操作ID' : '交易码';
    },
    managerArr() {
      return [
        ...this.appinfo.dev_managers.map(user => user.id),
        ...this.appinfo.spdb_managers.map(user => user.id)
      ];
    },
    btnLabel() {
      return (
        JSON.stringify(this.deployDetail.caas_model_env_mapping) === '{}' &&
        JSON.stringify(this.deployDetail.scc_model_env_mapping) === '{}'
      );
    },
    examLabel() {
      return this.appinfo.labels.indexOf('不涉及环境部署') > -1;
    },
    columns() {
      return profileTaskColumns(this.groups);
    }
  },
  methods: {
    ...mapMutations('userActionSaveApp/appProfile', [
      'updateTransactionCode',
      'updateBranchName',
      'updateInterfaceName',
      'updateInterfaceType',
      'updateFilter',
      'updateVisibleColumns'
    ]),
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent',
      fetchUser: 'fetch'
    }),
    ...mapActions('userForm', {
      fetchGroup: 'fetchGroup'
    }),
    ...mapActions('appForm', {
      getTestFlag: 'getTestFlag',
      appUpdate: 'appUpdate',
      queryDetail: 'queryDetail',
      createPipelineSchedule: 'createPipelineSchedule',
      deleteApp: 'deleteApp',
      queryMantis: 'queryMantis',
      querySonarChart: 'getAnalysesHistory',
      queryAppType: 'queryAppType',
      queryAppSystem: 'queryAppSystem'
    }),
    ...mapActions('environmentForm', {
      queryEnv: 'getEnvList',
      queryDeploy: 'queryDeploy',
      queryByLabelsFuzzy: 'queryByLabelsFuzzy'
    }),
    ...mapActions('jobForm', [
      'queryConfigTemplate',
      'getEnvList',
      'getEnvListByAppId',
      'previewConfigFile',
      'queryExtraConfigParam',
      'fetchJob',
      'queryExcludePirvateModelList'
    ]),
    ...mapActions('interfaceForm', [
      'getInterfacesUrl',
      'queryInterfaceList',
      'getProjectBranchList',
      'isAppManager'
    ]),
    ...mapActions('configCIForm', ['queryAppPipelineList']),
    //跳转生产信息查询详情
    goProInfoDetails() {
      this.$router.push({
        path: `/envModel/productionInfoDetails/${this.appinfo.name_en}`
      });
    },
    changeStatus() {
      this.permission = true;
    },
    initDialog() {
      if (!this.summaryModel.appCiType) {
        this.summaryModel.appCiType = 'git-ci';
      }
    },
    // 打开外部配置参数弹窗
    async handleModelSettingOpen() {
      this.modelSettingOpened = true;
      this.managerIds = getIdsFormList([
        this.appinfo.dev_managers,
        this.appinfo.spdb_managers
      ]);
      await this.getEnvListByAppId({ app_id: this.appId });
      await this.queryExcludePirvateModelList({
        name_en: this.appinfo.name_en
      });
      // 过滤掉所有的含ci的数据
      this.filterModelList = this.pirvateModelList.filter(item => {
        return item.name_en.split('_')[0].indexOf('ci') < 0;
      });
      let list = this.filterModelList;
      let modelProperties = [];
      list.forEach(model => {
        let modelNameEn = model.name_en;
        let modelNameCn = model.name_cn;
        let envKeys = model.env_key;
        envKeys.forEach(key => {
          let modelKey = `${modelNameEn}.${key.name_en}`;
          let modelProNameCn = key.name_cn;
          modelProperties.push({
            isVarivale: true,
            name: modelKey,
            value: modelKey,
            label: modelKey,
            desc: modelNameCn + '.' + modelProNameCn
          });
        });
      });
      let configTemplateProperties = [];
      try {
        let params = {
          project_id: this.appinfo.id,
          feature_branch: 'master'
        };
        await this.queryConfigTemplate(params);
        this.editor = this.configTemplate;
        this.filterEnvList = deepClone(this.envListByAppId);
      } catch (e) {
        throw e;
      } finally {
        this.originModelProperties = this.modelProperties = modelProperties.concat(
          configTemplateProperties
        );
        this.$refs.propertyEdit.runCmd('selectAll');
        this.$refs.propertyEdit.runCmd('insertText', this.editor);
      }
    },
    // 预览配置文件
    async getPreviewFile() {
      if (this.editor.trim() && this.env.name_en) {
        let params = {
          type: '0',
          env_name: this.env.name_en,
          content: this.editor,
          project_id: this.appinfo.id
        };
        await this.previewConfigFile(params);
        this.modelProperties = deepClone(this.originModelProperties);

        const { outSideParam, modelParam } = this.previewFile;
        const modelParamKeys = Object.keys(modelParam);
        const outSideParamKeys = Object.keys(outSideParam);

        modelParamKeys.forEach(key => {
          const index = this.modelProperties.findIndex(
            item => item.label === key
          );
          if (index > -1) {
            this.modelProperties[index].value = modelParam[key];
          } else {
            this.modelProperties.push({
              label: key,
              name: key,
              value: modelParam[key],
              isVarivale: true
            });
          }
        });

        outSideParamKeys.forEach(key => {
          this.modelProperties.push({
            label: key,
            name: key,
            value: outSideParam[key]
          });
        });
      } else {
        errorNotify('外部配置模板或者环境不能为空');
      }
    },
    handleError() {
      errorNotify(
        `部分实体在当前环境${
          this.env.name_en
        }未配置映射值，详见下方值为#ERROR#的字段`
      );
    },
    async spdbManagersFilter(val, update, abort) {
      if (this.isLoginUserList.length === 0) {
        await this.fetchUser();
      }
      let userOptions = this.userOptionsFilter(
        '行内项目负责人',
        '厂商项目负责人'
      );
      update(() => {
        this.options = userOptions.filter(
          user =>
            user.name.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    },
    appsystemInputFilter(val, update) {
      update(() => {
        this.filterappSystem = this.appSystemList.filter(
          tag => tag.name.indexOf(val) > -1
        );
      });
    },
    async queryInterface(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      const baseColumns = interfaceColumns();
      if (this.interfaceType !== 'REST') {
        baseColumns.splice(2, 0, {
          name: 'esbServiceId',
          label: '服务ID',
          field: 'esbServiceId',
          align: 'left'
        });
        baseColumns.splice(3, 0, {
          name: 'esbOperationId',
          label: '操作ID',
          field: 'esbOperationId',
          align: 'left'
        });
      }
      this.interfaceColumns = baseColumns;
      this.loading = true;
      let branchDefault = '';
      if (this.branchName === 'master') {
        branchDefault = 'master';
      } else if (this.branchName === 'SIT') {
        branchDefault = 'SIT';
      } else {
        branchDefault = 'other';
      }
      let params = {
        page: this.pagination.page,
        pageNum: this.pagination.rowsPerPage,
        interfaceName: this.interfaceName,
        serviceId: this.appinfo.name_en,
        transId: this.transactionCode ? this.transactionCode.split(',') : [],
        branch: this.branchName,
        branchDefault: branchDefault,
        interfaceType: this.interfaceType
      };
      try {
        await this.queryInterfaceList(params);
        this.tableData = this.interfaceList.list;
        this.pagination.rowsNumber = this.interfaceList.total;
        this.loading = false;
      } catch (e) {
        this.loading = false;
        throw e;
      }
    },
    async getData() {
      await this.queryDetail({
        id: this.appId
      });
      this.appinfo = this.appData;
      this.applicationInfo = this.appinfo[0];
      this.pipelinesReady = true;
      this.isAppManager({ serviceId: this.appinfo[0].name_en });
      this.createPipelineScheduleModel.autoSwitch =
        this.appData[0].pipeline_schedule_switch === '1' ? 'true' : 'false';

      if (this.appinfo.length !== 0) {
        this.appinfo = this.appinfo[0];
        if (!this.appinfo.status) {
          this.queryInterface();
        }
        this.summaryModel = {
          ...deepClone(this.appinfo),
          network: this.appinfo.network ? this.appinfo.network.split(',') : [],
          sit: this.appinfo.sit ? this.appinfo.sit : [{}],
          auto: this.appinfo.sit ? this.appinfo.sit.auto : null,
          schedule: this.appinfo.sit ? this.appinfo.sit.schedule : null
        };
        this.appinfo.labels = this.appinfo.label ? this.appinfo.label : [];
      } else {
        this.appinfo = createAppModel();
      }
      sessionStorage.setItem(
        'typeAndproId',
        JSON.stringify({
          project_id: this.appinfo.gitlab_project_id,
          appCiType: this.appinfo.appCiType
        })
      );
      this.$set(this.appinfo, 'importance', '');
      let labelValue = this.appinfo.label ? this.appinfo.label : [];
      this.importanceList.forEach(importanceValue => {
        if (labelValue.includes(importanceValue)) {
          this.appinfo.importance = importanceValue;
        }
      });
    },
    descFilter(input) {
      if (!input) {
        return input;
      }
      let reg = new RegExp(/\n/g);
      let reg2 = new RegExp(/</g);
      let reg3 = new RegExp(/>/g);
      return input
        .replace(reg2, '&lt;')
        .replace(reg3, '&gt;')
        .replace(reg, '</br>');
    },
    addSelect(val, done) {
      if (this.importanceList.includes(val)) {
        this.summaryModel.importance = val;
      }
      let newVal = val.replace(/(^\s*)|(\s*$)/g, '');
      let notOnly = this.summaryModel.labels.some(item => {
        return item.replace(/(^\s*)|(\s*$)/g, '') === newVal;
      });
      if (newVal.length > 0 && !notOnly) {
        done(newVal);
      }
    },
    removeEnvDep(params) {
      if (params.value === '不涉及环境部署') {
        this.summaryModel.env_deploy = '涉及环境部署';
      }
    },
    async handleSummary(title) {
      this.title = title;
      this.summaryModel = {
        ...deepClone(this.appinfo),
        network: this.appinfo.network ? this.appinfo.network.split(',') : [],
        sit: this.appinfo.sit ? this.appinfo.sit : [{}],
        auto: this.appinfo.sit ? this.appinfo.sit.auto : null,
        schedule: this.appinfo.sit ? this.appinfo.sit.schedule : null,
        sonar_scan_switch: this.appinfo.sonar_scan_switch === '1' ? '1' : '0',
        isTest: this.appinfo.isTest === '1' ? '1' : '0'
      };
      if (this.appinfo.labels.indexOf('不涉及环境部署') > -1) {
        this.summaryModel.env_deploy = '不涉及环境部署';
      } else {
        this.summaryModel.env_deploy = '涉及环境部署';
      }
      if (!this.summaryModel.sit) {
        this.summaryModel.sit = [{}];
      }
      if (this.appinfo.type_name && this.appinfo.type_id) {
        let appTypeObj = {
          label: this.appinfo.type_name,
          value: this.appinfo.type_id
        };
        this.$set(this.summaryModel, 'appType', appTypeObj);
      }
      this.summaryModel.group.fullName = this.summaryModel.group.name;
      this.openSummary = true;
      await this.fetchGroup();
      await this.queryAppSystem();
      let deepCloneGroups = deepClone(this.groups);
      this.deepCloneGroups = deepCloneGroups;
      this.groupsFilterList = deepCloneGroups;
      let autoObj = {
        id: this.summaryModel.sit[0] ? this.summaryModel.sit[0].auto : null,
        name_en: this.summaryModel.sit[0]
          ? this.summaryModel.sit[0].auto_env_name
          : null
      };
      this.$set(this.summaryModel, 'auto', this.appinfo.sit ? autoObj : null);
      let scheduleObj = {
        id: this.summaryModel.sit[0].schedule,
        name_en: this.summaryModel.sit[0].schedule_env_name
      };
      this.$set(
        this.summaryModel,
        'schedule',
        this.appinfo.sit ? scheduleObj : null
      );
      if (this.isLoginUserList.length === 0) {
        await this.fetchUser();
      } else {
        await this.fetchUser();
      }
      this.summaryModel.spdb_managers = this.summaryModel.spdb_managers.filter(
        user => {
          return this.isLoginUserList.some(item => item.id === user.id);
        }
      );
      this.summaryModel.dev_managers = this.summaryModel.dev_managers.filter(
        user => {
          return this.isLoginUserList.some(item => item.id === user.id);
        }
      );
    },
    async handleDeleteApp() {
      this.$q
        .dialog({
          title: '确认删除',
          message: '您确定删除该应用吗？',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteApp({
            id: this.appId
          });
          successNotify('删除成功');
          this.$router.push('/app/list');
        });
    },
    userOptionsFilter(...param) {
      let myuser = this.isLoginUserList.filter(item => {
        let flag = false;
        let roleLabels = [];
        item.role.forEach(ele => {
          roleLabels.push(ele.name);
        });
        param.forEach(roleLabel => {
          if (roleLabels.includes(roleLabel)) {
            flag = true;
          }
        });
        let list = [
          ...this.appinfo.spdb_managers,
          ...this.appinfo.dev_managers
        ];
        list.forEach(manager => {
          if (manager.id === item.id) {
            flag = true;
          }
        });
        return flag;
      });
      return myuser;
    },
    async handleSummaryModel() {
      this.$v.summaryModel.$touch();
      let summaryModelKeys = Object.keys(this.$refs).filter(
        key => key.indexOf('summaryModel') > -1
      );
      validate(summaryModelKeys.map(key => this.$refs[key]));
      if (this.$v.summaryModel.$invalid) {
        return;
      }
      let params = deepClone(this.summaryModel);
      params.type_id = this.summaryModel.appType.value;
      params.type_name = this.summaryModel.appType.label;
      params.network = this.summaryModel.network.toString();
      params.sonar_scan_switch = this.summaryModel.sonar_scan_switch;
      params.isTest = this.summaryModel.isTest;
      delete params.appType;
      if (this.appinfo.sit) {
        params.sit[0].auto = params.auto.id;
        params.sit[0].auto_env_name = params.auto.name_en;
        params.sit[0].schedule = params.schedule.id;
        params.sit[0].schedule_env_name = params.schedule.name_en;
      } else {
        params.sit = null;
      }
      params.label = params.labels;
      params.system = params.system.id;
      await this.appUpdate(params);
      successNotify(`编辑${this.title}成功`);
      this.openSummary = false;
      if (params.label.indexOf('不涉及环境部署') < 0 && !params.network) {
        this.confirmEnv = true;
      }
      await this.getData();
      if (this.applicationInfo.appCiType === 'fdev-ci') {
        await this.queryAppPipelineList({
          applicationId: this.appinfo.id,
          pageSize: 0,
          pageNum: 1
        });
        if (this.AppPipelineList.total === 0) {
          this.confirmCI = true;
        }
      }
    },
    async getJobList() {
      await this.fetchJob({ project_id: this.appId });
      this.data = this.jobs;
      await this.queryMantis({
        id: this.appId
      });
    },
    async handleExtreConfigDialogOpen() {
      await this.queryExtraConfigParam({
        project_id: this.appId
      });
      await this.fetchCurrent();
      if (this.managerIds.indexOf(this.currentUser.id) > -1) {
        this.noWrite = false;
      }
      this.extreConfigDialogOpened = true;
    },
    //关闭优先生效对话框
    changeExtreConfigEvent() {
      this.extreConfigDialogOpened = false;
    },
    handleScanDialogOpen() {
      this.scanDialogOpen = true;
    },
    // 打开生成链接弹窗
    async createUrlOpen() {
      const params = {
        branch: this.interfaceModel.branch || 'master',
        serviceId: this.appinfo.name_en,
        ids: this.interfaceArr.map(item => {
          const data = {};
          data[item.id] = item.interfaceType;
          return data;
        })
      };
      await this.getInterfacesUrl(params);
      this.createdUrl = `${baseUrl}fdev/#/queryInterfacesList/${this.url}`;
      this.createUrlDialogOpen = true;
    },
    /* 生成链接 */
    createUrl() {
      const input = document.createElement('input');
      input.value = this.createUrlurl;
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
      successNotify('复制成功');
      this.createUrlDialogOpen = false;
    },
    handleScanDialog(includeWeb) {
      this.scanDialogOpen = false;
      /* 不是web的项目，应用扫描完刷新页面 */
      if (!includeWeb) {
        this.queryInterface();
      }
    },
    handleChangeBranchName(branchName) {
      this.updateBranchName(branchName);
    },
    async filterFn(val, update, abort) {
      await this.getProjectBranchList({ id: this.appinfo.id });
      update(() => {
        this.filterGitlabBranch = this.gitlabBranch.filter(
          tag => tag.name.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    },
    /* 定时部署 */
    async handlePipelineScheduleOpen(val) {
      await this.createPipelineSchedule({
        ...this.createPipelineScheduleModel,
        id: this.appId
      });
      if (val === 'false') {
        successNotify('定时部署已关闭');
      } else {
        successNotify('定时部署已开启');
      }
      this.getData();
    },
    envFilter(val, update) {
      update(() => {
        this.filterEnvList = this.envListByAppId.filter(tag => {
          return (
            tag.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            tag.name_cn.indexOf(val) > -1
          );
        });
      });
    },
    sitEnvFilter(val, update) {
      update(() => {
        this.eitEnvOptions = this.envFilterByLabels.filter(
          env =>
            env.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            env.name_cn.indexOf(val) > -1
        );
      });
    },
    // 小组筛选
    filterGroups(val, update) {
      const needle = val.toLowerCase();
      update(() => {
        this.groupsFilterList = this.deepCloneGroups.filter(v => {
          return v.fullName.indexOf(needle) > -1;
        });
      });
    },
    async querySitEnvByNetwork() {
      if (!this.appinfo.network) return;
      await this.queryByLabelsFuzzy({
        labels: this.summaryModel.network.concat(['sit'])
      });
      this.eitEnvOptions = this.envFilterByLabels;
    },
    goCheckEnv() {
      this.$router.push({
        name: 'DeployMessageHandlePage',
        query: { appId: this.$route.params.id }
      });
    },
    goCheckCI() {
      this.tab = 'pipelines';
      if (this.managerArr.includes(this.currentUser.id)) {
        this.$nextTick(() => {
          this.$refs.configCI.tabpanel = 'proPipelines';
        });
      }
      this.confirmCI = false;
    },
    handleEnvDeploy(params) {
      if (params === '否') {
        this.summaryModel.labels.push('不涉及环境部署');
      } else {
        const index = this.summaryModel.labels.indexOf('不涉及环境部署');
        if (index > -1) {
          this.summaryModel.labels.splice(index, 1);
        }
      }
    },
    confirmToClose(key) {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this[key] = false;
        });
    }
  },
  async created() {
    let id = this.$route.params.id;
    const idIncludesInterface = id.indexOf('-interface');
    this.appId =
      idIncludesInterface > -1 ? id.substring(0, idIncludesInterface) : id;
    this.type = this.$route.query.type ? this.$route.query.type : 'using';
    await this.getTestFlag({
      id: this.appId
    });
    this.isTest = this.testFlag.isTest;
    this.queryAppType();
    this.getJobList(); // 查询是否有关联任务，若是，则应用编辑--应用类型不可修改
    await this.getData();

    if (!this.appinfo.status) {
      if (idIncludesInterface > -1) {
        //截取到需要的服务英文名
        this.tab = 'interfaceList';
      }
      this.queryEnv();
      this.querySitEnvByNetwork();
    }
    await this.querySonarChart({
      gitlab_id: this.appinfo.gitlab_project_id.toString()
    });
    this.isSonarShow = Boolean(this.sonarChart);
    if (this.$route.query.tab && this.isSonarShow) {
      this.tab = this.$route.query.tab;
    }
    this.loading = false;
  },
  mounted() {
    if (sessionStorage.getItem('interfaceSearchModel')) {
      const { tab } = JSON.parse(
        sessionStorage.getItem('interfaceSearchModel')
      );
      this.tab = tab;
    }
  },
  beforeRouteEnter(to, from, next) {
    const { params } = from;
    if (Object.keys(params).length === 0) {
      sessionStorage.removeItem('interfaceSearchModel');
    }
    next(vm => {
      if (
        from.path.indexOf('/app/applicationAdd') > -1 ||
        (params && (params.toCI === 'pipeline' || params.fromCI === 'pipeline'))
      ) {
        vm.goPipelines = true;
      }
    });
  },
  beforeRouteLeave(to, from, next) {
    const { params } = to;
    if (Object.keys(params).length) {
      sessionStorage.setItem(
        'interfaceSearchModel',
        JSON.stringify({
          tab: this.tab
        })
      );
    }
    next();
  }
};
</script>

<style lang="stylus" scoped>
/deep/ .mails-width .input-lg {
  width: 450px;
}

.edit-form-cells {
  padding-bottom: 20px;
}

.property-editor {
  height: calc(100vh - 50px);
  margin-top: 50px;
  overflow-y: hidden;
  min-height: 500px;
}

.editor-title {
  height: 40px;
  line-height: 40px;
  top: 48px;
  width: 100%;
  z-index: 2;
  background: #FFF;
}

.form {
  padding: 0 20px;
}

.editor-warpper {
  height: 100%;
  width: 100%;

  &.config {
    overflow: auto;
    padding-bottom: 60px;
    box-sizing: border-box;
  }

  .col-3, .col {
    display: inline-block;
    height: 100%;
    overflow: auto;
    vertical-align: top;
    box-sizing: border-box;
  }

  .col-3 {
    padding-bottom: 20px;
    width: 25%;
  }

  .col {
    overflow: auto;
    padding-bottom: 60px;
  }
}
</style>
