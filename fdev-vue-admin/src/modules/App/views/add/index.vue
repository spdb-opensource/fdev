<template>
  <f-block>
    <fdev-stepper
      v-model="step"
      header-nav
      ref="stepper"
      flat
      class="stepper"
      animated
      transition-prev="fade"
      transition-next="fade"
      v-if="step < 4"
    >
      <fdev-step
        :name="1"
        title="选择要进行的操作"
        icon="settings"
        :done="step > 1"
        :header-nav="step > 1"
      >
        <div class="row justify-center">
          <div class="col-12">
            <div class="step1-form">
              <fdev-select
                v-model="operation"
                :options="operationList"
                @input="handleStep1"
                @focus="handleFocus"
              >
              </fdev-select>
            </div>
          </div>
        </div>
      </fdev-step>

      <fdev-step
        :name="2"
        :title="title"
        icon="assignment"
        :done="step > 2"
        :header-nav="step > 2"
      >
        <div class="row">
          <div class="col">
            <f-formitem label="应用中文名" required>
              <fdev-input
                ref="applicationModel.name_zh"
                v-model="$v.applicationModel.name_zh.$model"
                :rules="[
                  () =>
                    $v.applicationModel.name_zh.required ||
                    '请输入应用中文名称',
                  () => $v.applicationModel.name_zh.isUnique || '该名称已存在',
                  () =>
                    $v.applicationModel.name_zh.includeChinese ||
                    '请至少输入一个中文'
                ]"
              >
              </fdev-input>
            </f-formitem>
            <f-formitem label="所属小组" required>
              <fdev-select
                use-input
                ref="applicationModel.group"
                v-model="$v.applicationModel.group.$model"
                :options="filterGroup"
                option-label="fullName"
                @filter="groupInputFilter"
                :rules="[
                  () => !$v.applicationModel.group.$error || '小组不能为空'
                ]"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.name">{{
                        scope.opt.name
                      }}</fdev-item-label>
                      <fdev-item-label :title="scope.opt.fullName" caption>
                        {{ scope.opt.fullName }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
            <f-formitem label="应用类型" required>
              <fdev-select
                ref="applicationModel.type"
                v-model="applicationModel.type"
                :options="appTypes"
                @blur="appTypeTouch"
                :rules="[
                  () => $v.applicationModel.type.required || '请选择应用类型'
                ]"
              />
            </f-formitem>
            <f-formitem label="应用编码集" required>
              <fdev-select
                ref="applicationModel.coding"
                v-model="applicationModel.coding"
                :options="codingGroup"
                :rules="[
                  () =>
                    $v.applicationModel.coding.required || '请选择应用编码集'
                ]"
              />
            </f-formitem>
            <f-formitem
              v-if="operateType === '1'"
              label="gitlab project Id"
              required
            >
              <span>
                <fdev-input
                  :disable="applicationModel.type ? false : true"
                  ref="applicationModel.gitlab_project_id"
                  v-model="$v.applicationModel.gitlab_project_id.$model"
                  @blur="delayTouch($v.applicationModel.gitlab_project_id)"
                  :rules="[
                    () =>
                      $v.applicationModel.gitlab_project_id.required ||
                      'gitlab Id 不能为空',
                    () =>
                      $v.applicationModel.gitlab_project_id.isUnique ||
                      'gitlab Id 不存在',
                    () =>
                      $v.applicationModel.gitlab_project_id.numeric ||
                      '只能输入数字'
                  ]"
                >
                </fdev-input>
                <fdev-tooltip v-if="!applicationModel.type"
                  >请先选择应用类型</fdev-tooltip
                >
              </span>
            </f-formitem>
            <!-- 互联网条线-‘录入已有应用’页面 -->
            <f-formitem
              label="应用英文名"
              v-if="isInternetApp && operateType === '1'"
              required
            >
              <span>
                <fdev-input
                  ref="applicationModel.name_en"
                  v-model="$v.applicationModel.name_en.$model"
                  disable
                  :rules="[
                    () =>
                      $v.applicationModel.name_en.required ||
                      '请输入正确的project ID',
                    () =>
                      $v.applicationModel.name_en.isCorrect ||
                      '应用英文名格式有误'
                  ]"
                >
                </fdev-input>
                <fdev-tooltip v-if="!applicationModel.gitlab_project_id"
                  >请输入正确的应用类型和project ID</fdev-tooltip
                >
              </span>
            </f-formitem>
            <!-- 非互联网条线-‘录入已有应用’页面 -->
            <f-formitem
              label="应用英文名"
              v-if="!isInternetApp && operateType === '1'"
              required
            >
              <span>
                <fdev-input
                  ref="applicationModel.name_en"
                  v-model="$v.applicationModel.name_en.$model"
                  disable
                  :rules="[
                    () =>
                      $v.applicationModel.name_en.required ||
                      '请输入正确的project ID'
                  ]"
                >
                </fdev-input>
                <fdev-tooltip v-if="!applicationModel.gitlab_project_id"
                  >请输入正确的应用类型和project ID</fdev-tooltip
                >
              </span>
            </f-formitem>
            <f-formitem label="CI集成模板" v-if="operateType === '1'" required>
              <fdev-select
                use-input
                @filter="CITemplatesInputFliter"
                ref="applicationModel.gitlabci_id"
                v-model="$v.applicationModel.gitlabci_id.$model"
                :options="filterCITemplates"
                :rules="[
                  () => !$v.applicationModel.gitlabci_id.$error || 'CI集成模板'
                ]"
              >
              </fdev-select>
            </f-formitem>
            <!-- 只在 非互联网条线应用的‘新增应用’页面，展示此按钮  -->
            <f-formitem
              label="是否自定义英文名"
              v-if="!isInternetApp && operateType === '0'"
              class="formCells"
            >
              <fdev-radio
                val="0"
                v-model="isVerify"
                label="是"
                @input="handleIsVerify('是')"
              />
              <fdev-radio
                val="1"
                v-model="isVerify"
                label="否"
                class="q-ml-lg"
                @input="handleIsVerify('否')"
              />
            </f-formitem>
            <!--operateType === '0' && isVerify === '0'  表示新增应用页面——自定义英文名 start-->
            <f-formitem
              v-if="operateType === '0' && isVerify === '0'"
              label="应用英文名"
              required
            >
              <fdev-input
                ref="applicationModel.name_en_diy"
                v-model="$v.applicationModel.name_en_diy.$model"
                :rules="[
                  () =>
                    $v.applicationModel.name_en_diy.required ||
                    '应用英文名不能为空',
                  () =>
                    $v.applicationModel.name_en_diy.isUnique || '该名称已存在',
                  () => $v.applicationModel.name_en_diy.onlyEnglish || ''
                ]"
              />
              <span v-show="showWarningDiy" class="warning">
                只能输入数字、字母，并只能以小写字母开头结尾
              </span>
            </f-formitem>
            <!-- 新增应用页面——自定义英文名  end -->
            <!--operateType === '0' && isVerify === '1'  表示新增应用页面——不自定义英文名 start-->
            <f-formitem
              v-if="operateType === '0' && isVerify === '1'"
              label="应用所属业务域"
              class="formCells"
            >
              <fdev-select
                use-input
                @filter="systemInputFilter"
                ref="applicationModel.service_system"
                v-model="applicationModel.service_system"
                :options="filterSystem"
              />
            </f-formitem>
            <f-formitem
              v-if="operateType === '0' && isVerify === '1'"
              label="应用所属域"
              class="formCells"
            >
              <fdev-select
                use-input
                @filter="domainInputFilter"
                ref="applicationModel.domain"
                v-model="applicationModel.domain"
                :options="filterDomain"
              />
            </f-formitem>
            <f-formitem
              v-if="operateType === '0' && isVerify === '1'"
              label="应用英文名"
              required
            >
              <div class="row">
                <fdev-input
                  ref="applicationModel.service_system.name_en"
                  v-model="$v.applicationModel.service_system.name_en.$model"
                  class="input col-3"
                  placeholder="系统"
                  :disable="
                    applicationModel.service_system.label !== '自定义系统'
                  "
                  :rules="[
                    () =>
                      $v.applicationModel.service_system.name_en.need ||
                      '系统不能为空',
                    () =>
                      $v.applicationModel.service_system.name_en.onlyEnglish ||
                      ''
                  ]"
                />
                <span class="col line-link">-</span>
                <fdev-input
                  ref="applicationModel.domain.name_en"
                  v-model="$v.applicationModel.domain.name_en.$model"
                  placeholder="域"
                  class="input col-3"
                  :disable="applicationModel.domain.label !== '自定义域'"
                  :rules="[
                    () =>
                      $v.applicationModel.domain.name_en.need || '域名不能为空',
                    () => $v.applicationModel.domain.name_en.onlyEnglish || ''
                  ]"
                />
                <span class="col line-link">-</span>
                <fdev-input
                  ref="applicationModel.name_en"
                  v-model="$v.applicationModel.name_en.$model"
                  class="input col-5"
                  :rules="[
                    () =>
                      $v.applicationModel.name_en.required ||
                      '应用英文名不能为空',
                    () =>
                      $v.applicationModel.name_en.isUnique || '该名称已存在',
                    () => $v.applicationModel.name_en.onlyEnglish || ''
                  ]"
                />
              </div>
              <span v-show="showWarning" class="warning">
                只能输入数字、字母，并只能以小写字母开头结尾
              </span>
            </f-formitem>
            <!-- 新增应用页面——不自定义英文名 end -->
            <f-formitem label="应用负责人" required>
              <fdev-select
                use-input
                multiple
                ref="applicationModel.dev_managers"
                v-model="$v.applicationModel.dev_managers.$model"
                :options="userOptionsFilter('行内项目负责人', '厂商项目负责人')"
                @filter="userFilter"
                :rules="[
                  () =>
                    !$v.applicationModel.dev_managers.$error ||
                    '应用负责人不能为空'
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
            <f-formitem label="应用描述" v-if="operateType === '0'">
              <fdev-input
                ref="applicationModel.desc"
                v-model="$v.applicationModel.desc.$model"
                type="textarea"
                rows="2"
              />
            </f-formitem>
          </div>
          <div class="col">
            <f-formitem label="行内应用负责人" required>
              <fdev-select
                use-input
                multiple
                ref="applicationModel.spdb_managers"
                v-model="$v.applicationModel.spdb_managers.$model"
                :options="userOptionsFilter('行内项目负责人')"
                @filter="userFilter"
                :rules="[
                  () =>
                    !$v.applicationModel.spdb_managers.$error ||
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

            <!-- 新增应用 start -->
            <f-formitem label="应用所属系统" required>
              <fdev-select
                use-input
                ref="applicationModel.system"
                v-model="$v.applicationModel.system.$model"
                :options="filterappSystem"
                @filter="appsystemInputFilter"
                option-label="name"
                option-value="id"
                :rules="[
                  () =>
                    !$v.applicationModel.system.$error || '应用所属系统不能为空'
                ]"
              />
            </f-formitem>
            <f-formitem v-if="operateType === '0'" label="gitlab组" required>
              <fdev-select
                use-input
                ref="applicationModel.gitlab_group"
                v-model="$v.applicationModel.gitlab_group.$model"
                :options="filtergroupGit"
                @filter="groupGitInputFilter"
                option-label="full_path"
                option-value="id"
                :rules="[
                  () =>
                    !$v.applicationModel.gitlab_group.$error ||
                    'gitlab组不能为空'
                ]"
              />
            </f-formitem>

            <f-formitem v-if="operateType === '0'" label="git代码库" required>
              <fdev-input
                ref="applicationModel.git"
                v-model="$v.applicationModel.git.$model"
                @blur="toGetGroup('0')"
                :rules="[
                  () => $v.applicationModel.git.required || '请输入仓库',
                  () => $v.applicationModel.git.grounpExistence || '仓库不存在',
                  () =>
                    $v.applicationModel.git.isUnique || '请输入正确的仓库地址'
                ]"
              >
                <template v-slot:prepend v-if="gitPrefix">
                  <span class="prefix">/{{ gitPrefix }}/</span>
                </template>
              </fdev-input>
            </f-formitem>
            <!-- 新增应用 end -->
            <f-formitem v-if="operateType === '0'" label="应用骨架" required>
              <fdev-select
                use-input
                @filter="archetypesInputFliter"
                ref="applicationModel.archetype"
                v-model="$v.applicationModel.archetype.$model"
                option-label="name_cn"
                option-value="id"
                :options="filterArchetypes"
                :rules="[
                  () =>
                    !$v.applicationModel.archetype.$error || '应用骨架不能为空'
                ]"
              />
            </f-formitem>

            <f-formitem label="重要度" class="formCells">
              <fdev-select
                ref="applicationModel.importance"
                v-model="$v.applicationModel.importance.$model"
                :options="importanceList"
              />
            </f-formitem>
            <f-formitem label="持续集成方式" required>
              <fdev-select
                ref="applicationModel.appCiType"
                :options="appCiTypeList"
                v-model="$v.applicationModel.appCiType.$model"
                :rules="[
                  () =>
                    $v.applicationModel.appCiType.required ||
                    '请选择持续集成方式'
                ]"
              />
            </f-formitem>
            <f-formitem
              v-if="applicationModel.appCiType === 'git-ci'"
              label="是否涉及环境部署"
              class="formCells"
            >
              <fdev-radio
                val="涉及环境部署"
                v-model="$v.applicationModel.env_deploy.$model"
                label="是"
                @input="handleEnvDeploy('是')"
              />
              <fdev-radio
                val="不涉及环境部署"
                v-model="$v.applicationModel.env_deploy.$model"
                label="否"
                class="q-ml-lg"
                @input="handleEnvDeploy('否')"
              />
            </f-formitem>
            <f-formitem label="sonar扫描卡点" class="formCells">
              <fdev-radio
                val="1"
                v-model="applicationModel.sonar_scan_switch"
                label="开"
              />
              <fdev-radio
                val="0"
                v-model="applicationModel.sonar_scan_switch"
                label="关"
                class="q-ml-lg"
              />
            </f-formitem>
            <f-formitem label="是否涉及内测" class="formCells">
              <fdev-radio
                val="1"
                v-model="$v.applicationModel.isTest.$model"
                label="是"
              />
              <fdev-radio
                val="0"
                v-model="$v.applicationModel.isTest.$model"
                label="否"
                class="q-ml-lg"
              />
            </f-formitem>
            <f-formitem label="标签" class="formCells">
              <fdev-select
                placeholder="请输入 "
                use-input
                multiple
                ref="applicationModel.labels"
                v-model="$v.applicationModel.labels.$model"
                hide-dropdown-icon
                @new-value="addSelect"
                @remove="removeEnvDep"
              >
                <template v-slot:selected-item="scope">
                  <fdev-chip
                    :removable="
                      scope.opt !== '不涉及环境部署' ||
                        applicationModel.appCiType === 'git-ci'
                    "
                    @remove="scope.removeAtIndex(scope.index)"
                    :tabindex="scope.tabindex"
                  >
                    {{ scope.opt }}
                  </fdev-chip>
                </template>
              </fdev-select>
            </f-formitem>
            <f-formitem label="应用描述" v-if="operateType !== '0'">
              <fdev-input
                ref="applicationModel.desc"
                v-model="$v.applicationModel.desc.$model"
                type="textarea"
                rows="2"
              />
            </f-formitem>
          </div>
        </div>

        <fdev-stepper-navigation>
          <div class="row justify-center q-gutter-x-lg">
            <fdev-btn outline label="上一步" @click="handleStepPrevious" />
            <fdev-btn label="下一步" @click="toConfirm" />
          </div>
        </fdev-stepper-navigation>
      </fdev-step>

      <fdev-step
        :name="3"
        title="确认信息"
        icon="mdi-set mdi-application"
        :done="step > 3"
        :header-nav="step > 3"
      >
        <div class="col-sm-12 col-xs-12 row">
          <div class="col-sm-4 col-xs-6">
            <f-formitem label="应用中文名：">
              {{ applicationModel.name_zh }}
            </f-formitem>
          </div>
          <div class="col-sm-4 col-xs-6">
            <f-formitem label="所属组：">
              {{ applicationModel.group.name }}
            </f-formitem>
          </div>
          <div v-if="operateType === '1'" class="col-sm-4 col-xs-6">
            <f-formitem label="gitlab Id：">
              {{ applicationModel.gitlab_project_id }}
            </f-formitem>
          </div>
          <div class="col-sm-4 col-xs-6">
            <f-formitem label="应用负责人：">
              {{ applicationModel.dev_managers | managersFilter }}
            </f-formitem>
          </div>
          <div class="col-sm-4 col-xs-6">
            <f-formitem label="行内应用负责人：">
              {{ applicationModel.spdb_managers | managersFilter }}
            </f-formitem>
          </div>
          <!-- 展示 ‘录入已有应用’页面—应用英文名 -->
          <div class="col-sm-4 col-xs-6" v-if="operateType === '1'">
            <f-formitem label="应用英文名：">
              {{ applicationModel.name_en }}
            </f-formitem>
          </div>
          <!-- 展示 ‘新增应用’页面—不自定义应用英文名-->
          <div
            class="col-sm-4 col-xs-6"
            v-if="
              !(operateType === '0' && isVerify === '0') &&
                !(operateType === '1')
            "
          >
            <f-formitem label="应用英文名：">
              {{ applicationModel.service_system.name_en }}-{{
                applicationModel.domain.name_en
              }}-{{ applicationModel.name_en }}
            </f-formitem>
          </div>
          <!-- 展示‘新增应用’页面—自定义应用英文名 -->
          <div
            class="col-sm-4 col-xs-6"
            v-if="operateType === '0' && isVerify === '0'"
          >
            <f-formitem label="应用英文名：">
              {{ applicationModel.name_en_diy }}
            </f-formitem>
          </div>
          <div class="col-sm-4 col-xs-6">
            <f-formitem label="应用类型：" v-if="applicationModel.type">
              {{ applicationModel.type.label }}
            </f-formitem>
          </div>
          <div class="col-sm-4 col-xs-6">
            <f-formitem label="应用编码集：" v-if="applicationModel.coding">
              {{ applicationModel.coding.label }}
            </f-formitem>
          </div>
          <div class="col-sm-4 col-xs-6">
            <f-formitem label="应用所属系统：">
              {{ applicationModel.system.name }}
            </f-formitem>
          </div>
          <!-- 用户在‘新增应用’页面，选择了‘不自定义应用英文名’时，展示相应的‘应用所属业务域’和‘应用所属域’-->
          <div
            v-if="
              !(operateType === '0' && isVerify === '0') &&
                !(operateType === '1')
            "
            class="col-sm-4 col-xs-6"
          >
            <f-formitem label="应用所属业务域：">
              {{ applicationModel.service_system.label }}
            </f-formitem>
          </div>
          <div
            v-if="
              !(operateType === '0' && isVerify === '0') &&
                !(operateType === '1')
            "
            class="col-sm-4 col-xs-6"
          >
            <f-formitem label="应用所属域：">
              {{ applicationModel.domain.label }}
            </f-formitem>
          </div>
          <!-- 用户在‘新增应用’页面，选择了‘自定义应用英文名’时，展示相应的‘应用所属业务域’和‘应用所属域’ -->
          <div
            v-if="operateType === '0' && isVerify === '0'"
            class="col-sm-4 col-xs-6"
          >
            <f-formitem label="应用所属业务域：">
              {{ '' }}
            </f-formitem>
          </div>
          <div
            v-if="operateType === '0' && isVerify === '0'"
            class="col-sm-4 col-xs-6"
          >
            <f-formitem label="应用所属域：">
              {{ '' }}
            </f-formitem>
          </div>
          <div class="col-sm-4 col-xs-6" v-if="operation.value === '0'">
            <f-formitem label="应用骨架：">
              {{
                applicationModel.archetype
                  ? applicationModel.archetype.name_cn
                  : ''
              }}
            </f-formitem>
          </div>
          <div v-if="operateType === '1'" class="col-sm-4 col-xs-6">
            <f-formitem label="CI集成模板：">
              {{ applicationModel.gitlabci_id.name }}
            </f-formitem>
          </div>
          <div v-if="operateType === '0'" class="col-sm-4 col-xs-6">
            <f-formitem label="git代码库：">
              /{{ gitPrefix }}/{{ applicationModel.git }}
            </f-formitem>
          </div>
          <div class="col-sm-4 col-xs-6">
            <f-formitem label="持续集成方式：">
              {{ applicationModel.appCiType }}
            </f-formitem>
          </div>
          <div class="col-sm-4 col-xs-6">
            <f-formitem label="标签：">
              <fdev-chip
                v-for="(label, index) in applicationModel.labels"
                :key="index"
                square
                color="teal"
                text-color="white"
                class="q-mt-none"
              >
                {{ label }}
              </fdev-chip>
            </f-formitem>
          </div>
          <div class="col-sm-4 col-xs-6">
            <f-formitem label="sonar扫描卡点：">
              {{ applicationModel.sonar_scan_switch === '1' ? '开' : ' 关' }}
            </f-formitem>
          </div>
          <div class="col-sm-4 col-xs-6">
            <f-formitem label="是否涉及内测：">
              {{ applicationModel.isTest === '1' ? '是' : ' 否' }}
            </f-formitem>
          </div>
          <div class="col-sm-4 col-xs-6">
            <f-formitem label="应用描述：">
              <span
                class="desc-style"
                v-html="descFilter(applicationModel.desc)"
              ></span>
            </f-formitem>
          </div>
        </div>
        <fdev-stepper-navigation>
          <div class="row justify-center q-gutter-x-lg">
            <fdev-btn outline label="上一步" @click="handleStepPrevious" />
            <fdev-btn
              @click="handleStep3"
              label="确认"
              :loading="globalLoading['appForm/addApp']"
            />
          </div>
        </fdev-stepper-navigation>
      </fdev-step>
    </fdev-stepper>
    <div v-show="step > 3" class="col row items-center">
      <Polling
        v-show="operation.value === '0'"
        ref="polling"
        :label="applicationModel.labels"
        :app-data="applicationModel"
        :app-id="appId"
      />
      <Result
        v-show="
          operation.value === '1' && applicationModel.appCiType === 'git-ci'
        "
        type="success"
        class="col q-my-xl"
      >
        <template v-slot:title>
          新增成功
        </template>
        <template v-slot:description v-if="!examLabel">
          请先绑定部署信息，否则不能进行持续集成
        </template>
        <template v-slot:actions>
          <div class="row justify-center q-gutter-x-md">
            <fdev-btn
              @click="toDeployMessage(result.id)"
              label="去绑定"
              v-if="!examLabel"
            />
            <fdev-btn
              dialog
              label="返回列表"
              v-if="examLabel"
              @click="backToAppList"
            />
            <fdev-btn
              label="查看应用"
              v-if="examLabel"
              @click="viewAppDetail(`/app/list/${result.id}`)"
            />
          </div>
        </template>
      </Result>

      <Result
        v-show="
          operation.value === '1' && applicationModel.appCiType === 'fdev-ci'
        "
        type="success"
        class="col q-my-xl"
      >
        <template v-slot:title>
          录入成功
        </template>
        <template v-slot:description>
          请先配置流水线，否则不能进行持续集成，点击去配置按钮
        </template>
        <template v-slot:actions>
          <div class="row justify-center q-gutter-x-sm">
            <fdev-btn @click="toAddPipeline(result.id)" label="去配置" />
          </div>
        </template>
      </Result>
    </div>
  </f-block>
</template>

<script>
import { mapState, mapActions, mapGetters } from 'vuex';
import { createAppModel } from '@/modules/App/utils/constants';
import { formatUser } from '@/modules/User/utils/model';
import { required, numeric } from 'vuelidate/lib/validators';
import Polling from '@/modules/App/views/add/components/polling';
import Result from '@/components/Result';
import { formatOption, validate, errorNotify } from '@/utils/utils';

export default {
  name: 'applicationAdd',
  components: { Polling, Result },
  data() {
    return {
      step: 1,
      operationList: [
        { label: '录入已有应用', value: '1' },
        { label: '新增应用', value: '0' }
      ],
      importanceList: ['关键', '一般', '重要'],
      appCiTypeList: ['git-ci', 'fdev-ci'],
      operateType: '',
      isVerify: '1', //是否校验英文名   '1' 表示校验（即不自定义英文名），'0' 表示不校验（即自定义英文名）
      operation: '',
      title: '录入基本信息',
      applicationModel: createAppModel(),
      loading: false,
      users: [],
      userOptions: [],
      applications: [],
      systems: [],
      domains: [],
      domainsOptions: [],
      CITemplates: [],
      filterGroup: [],
      archetype: {},
      result: {},
      existence: true,
      gitlabId: true,
      isOnceComming: false, //是否第一次进来，第一次进来数据变化之前不做rules规则验证。
      filterSystem: [],
      filterappSystem: [],
      filtergroupGit: [],
      filterDomain: [],
      filterArchetypes: [],
      filterCITemplates: [],
      appTypes: [],
      codingGroup: [
        { label: 'GBK', value: 'GBK' },
        { label: 'UTF-8', value: 'UTF-8' }
      ],
      appId: {}
    };
  },
  validations: {
    applicationModel: {
      name_zh: {
        required,
        isUnique(value) {
          if (!value) {
            return true;
          }
          let val = this.applications.filter(app => app.name_zh == value);
          return val.length == 0;
        },
        includeChinese(value) {
          if (!value) {
            return true;
          }
          let reg = new RegExp(/[\u4e00-\u9fa5]/gm);
          let flag = reg.test(value);
          return flag;
        }
      },
      git: {
        required,
        grounpExistence(val) {
          if (!val) {
            return true;
          }
          return this.existence;
        },
        isUnique(val) {
          if (!val) {
            return true;
          }
          const reg = /^(?!-)([a-zA-Z0-9_./-]*)([a-zA-Z0-9_-])$/;
          return reg.test(val);
        }
      },
      system: {
        required
      },
      gitlab_group: {
        required
      },
      isTest: {
        required
      },
      service_system: {
        name_en: {
          need(value) {
            if (this.isOnceComming) {
              return true;
            } else {
              return !!value;
            }
          },
          onlyEnglish(value) {
            if (!value) {
              return true;
            }
            const reg = /^(?![0-9])([a-z0-9]*)([a-z0-9])$/;
            return reg.test(value);
          }
        }
      },
      domain: {
        name_en: {
          need(value) {
            if (this.isOnceComming) {
              return true;
            } else {
              return !!value;
            }
          },
          onlyEnglish(value) {
            if (!value) {
              return true;
            }
            const reg = /^([a-z0-9]*)([a-z0-9])$/;
            return reg.test(value);
          }
        }
      },
      gitlabci_id: {
        required
      },
      importance: {},
      appCiType: {
        required
      },
      gitlab_project_id: {
        required,
        numeric,
        isUnique() {
          return this.gitlabId;
        }
      },
      archetype: {
        required
      },
      group: {
        required
      },
      dev_managers: {
        required
      },
      spdb_managers: {
        required
      },
      coding: {
        required
      },
      desc: {},
      name_en: {
        required,
        isUnique(value) {
          if (!value) {
            return true;
          }
          if (this.operateType === '1') {
            return true;
          } else {
            const { service_system, domain, name_en } = this.applicationModel;
            value = `${service_system.name_en}-${domain.name_en}-${name_en}`;
            let val = this.applications.filter(app => app.name_en == value);
            return val.length === 0;
          }
        },
        isCorrect(value) {
          if (this.operateType !== '1' || !value) {
            return true;
          } else {
            if (
              this.isInternetApp &&
              this.applicationModel.type &&
              (this.applicationModel.type.label === 'Vue应用' ||
                this.applicationModel.type.label === 'Java微服务')
            ) {
              const reg = /^(?![0-9])([a-z0-9]*)([a-z0-9])-([a-z0-9]+)-([a-z0-9]*)([a-z])$/;
              return reg.test(value);
            } else {
              return true;
            }
          }
        },
        onlyEnglish(value) {
          if (!value) {
            return true;
          }
          // 录入已有应用 '1'
          if (this.operateType !== '0') {
            return true;
          } else {
            const reg = /^([a-z0-9]*)([a-z])$/;
            return reg.test(value);
          }
        }
      },
      name_en_diy: {
        required,
        isUnique(value) {
          if (!value) {
            return true;
          }
          let val = this.applications.filter(app => app.name_en == value);
          return val.length === 0;
        },
        onlyEnglish(value) {
          if (!value) {
            return true;
          }
          // 录入已有应用 '1'
          if (this.operateType !== '0') {
            return true;
          } else {
            const reg = /^(?![0-9])([a-z0-9]*)([a-z])$/;
            return reg.test(value);
          }
        }
      },
      labels: {},
      type: {
        required
      },
      env_deploy: {},
      sonar_scan_switch: {}
    }
  },
  watch: {
    'applicationModel.gitlab_group': {
      handler: function(val) {
        if (val) {
          this.applicationModel.git = '';
        }
      },
      deep: true
    },
    isVerify: {
      handler: function(val) {
        if (val) {
          // 用户在‘是否自定义英文名’的‘是’‘否’中来回切换时，重置各项的值
          if (this.$refs.Model) this.$refs.Model.reset();
        }
      }
    },
    'applicationModel.importance': {
      handler(newVal, oldVal) {
        let labels = this.applicationModel.labels
          ? this.applicationModel.labels
          : [];
        let newLabels = [];
        labels.forEach(label => {
          if (this.importanceList.indexOf(label) === -1) newLabels.push(label);
        });
        labels = newLabels;
        if (newVal) labels.push(newVal);
        this.applicationModel.labels = labels;
      },
      deep: true
    },
    'applicationModel.labels': {
      handler(newVal, oldVal) {
        if (
          this.applicationModel.appCiType !== 'git-ci' &&
          !newVal.includes('不涉及环境部署') &&
          oldVal.includes('不涉及环境部署')
        ) {
          this.applicationModel.labels.push('不涉及环境部署');
        }
        let isExistImportance = newVal.some(label => {
          return this.importanceList.includes(label);
        });
        if (!isExistImportance) {
          this.applicationModel.importance = '';
        }
      },
      deep: true
    },
    'applicationModel.archetype': {
      deep: true,
      handler: function(val) {
        if (!val) {
          this.applicationModel.archetyp = null;
        }
      }
    },
    'applicationModel.appCiType': {
      handler: function(val) {
        if (val === 'git-ci') {
          const index = this.applicationModel.labels.indexOf('不涉及环境部署');
          this.applicationModel.labels.splice(index, 1);
        }
        if (val === 'fdev-ci') {
          if (!this.applicationModel.labels.includes('不涉及环境部署')) {
            this.applicationModel.labels.push('不涉及环境部署');
          }
        }
      }
    },
    step(val) {
      if (val === 1) {
        this.operation = '';
      }
    },
    'applicationModel.git': {
      deep: true,
      handler(val) {
        this.existence = true;
      }
    },
    async operateType(val) {
      // 持续集成模板list 原来是写在focus中，不合理,第一次focus时，类型为选择
      if (this.operateType === '1') {
        await this.queryCITemplate();
        this.CITemplates = formatOption(this.CITemplateData);
      }
    },
    operation(val) {
      if (val.value === '0') {
        this.appTypes = this.appTypes.filter(type => {
          return (
            type.label === 'Java微服务' ||
            type.label === 'Vue应用' ||
            type.label.toLowerCase() === 'reactnative应用'
          );
        });
      } else {
        this.appTypes = this.appTypeData;
      }
    }
  },
  computed: {
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapGetters('user', ['isLoginUserList']),
    ...mapState('userForm', {
      groups: 'groups',
      CITemplateData: 'CITemplateData',
      systemData: 'systemData',
      domainData: 'domainData',
      childGroup: 'childGroup'
    }),
    ...mapState('appForm', {
      vueAppData: 'vueAppData',
      gitlabData: 'gitlabData',
      groupData: 'groupData',
      existedAppData: 'existedAppData',
      newAppData: 'newAppData',
      addAppStep: 'addAppStep',
      applicationName: 'applicationName',
      appTypeData: 'appTypeData',
      appSystemList: 'appSystemList',
      groupGitList: 'groupGitList'
    }),
    ...mapState('componentForm', ['archetypeList']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    // 判断是否为互联网条线应用，即判断登录用户所属小组或其父组是否为‘互联网’
    isInternetApp() {
      return this.childGroup.find(item => item.id === this.currentUser.group.id)
        ? true
        : false;
    },
    showWarning() {
      return (
        !this.$v.applicationModel.service_system.name_en.onlyEnglish ||
        !this.$v.applicationModel.domain.name_en.onlyEnglish ||
        !this.$v.applicationModel.name_en.onlyEnglish
      );
    },
    showWarningDiy() {
      return !this.$v.applicationModel.name_en_diy.onlyEnglish;
    },
    examLabel() {
      return this.result.label
        ? this.result.label.indexOf('不涉及环境部署') > -1
        : false;
    },
    gitPrefix() {
      if (this.applicationModel.gitlab_group) {
        return this.applicationModel.gitlab_group.full_path;
      } else {
        return '';
      }
    }
  },
  filters: {
    managersFilter(managers) {
      let arrays = [];
      managers.forEach(manager => {
        arrays.push(manager.name);
      });
      return arrays.join(', ');
    }
  },
  methods: {
    ...mapActions('userForm', [
      'fetchGroup',
      'queryCITemplate',
      'queryDomain',
      'querySystem',
      'queryChildGroupById'
    ]),
    ...mapActions('componentForm', ['queryArchetypes']),
    ...mapActions('user', ['fetch']),
    ...mapActions('appForm', {
      queryApps: 'queryApps',
      queryProject: 'queryProject',
      getGroup: 'getGroup',
      addApp: 'addApp',
      addNewApp: 'addNewApp',
      saveByAsync: 'saveByAsync',
      addExistedApp: 'addExistedApp',
      queryApplicationName: 'queryApplicationName',
      queryAppType: 'queryAppType',
      queryAppSystem: 'queryAppSystem',
      getGroupGit: 'getGroupGit'
    }),
    toAppProfile(appId) {
      this.$router.push({
        path: `/app/list/${appId}`
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
    handleStepPrevious() {
      if (this.step === 2) {
        this.title = '录入基本信息';
        this.operation = '';
      }
      this.$refs.stepper.previous();
    },
    async userFilter(val, update, abort) {
      update(() => {
        this.users = this.userOptions.filter(
          user =>
            user.name.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    },
    addSelect(val, done) {
      if (this.importanceList.includes(val)) {
        this.applicationModel.importance = val;
      }
      let newVal = val.replace(/(^\s*)|(\s*$)/g, '');
      let notOnly = this.applicationModel.labels.some(item => {
        return item.replace(/(^\s*)|(\s*$)/g, '') === newVal;
      });
      if (newVal.length > 0 && !notOnly) {
        done(newVal);
      }
    },
    handleStep1(val) {
      this.operateType = val.value;
      this.title =
        val.value === '1' ? '录入已有应用基本信息' : '录入新应用基本信息';
      this.step = 2;
    },
    async handleFocus() {
      this.isOnceComming = true;
      this.applicationModel = createAppModel();
      // 获取小组list
      await this.fetchGroup();
      // 获取应用list
      await this.queryApps();
      this.applications = this.vueAppData;
      // 获取所有的负责人
      await this.fetch();
      let users = this.isLoginUserList;
      this.users = users.map(user => formatOption(formatUser(user), 'name'));
      this.userOptions = this.users.slice(0);
      // 应用所属业务域list
      await this.querySystem();
      this.systems = formatOption(this.systemData, 'name_cn');
      // 用户选择‘不自定义英文名’时，‘所属业务域’默认为‘自定义系统’
      this.applicationModel.service_system = this.systems.find(
        item => item.name_cn === '自定义系统'
      );
      // 应用所属域list
      await this.queryDomain();
      this.domains = formatOption(this.domainData, 'name_cn');
      // 用户选择‘不自定义英文名’时，‘应用所属域’默认为‘自定义域’
      this.applicationModel.domain = this.domains.find(
        item => item.name_cn === '自定义域'
      );
      //应用所属系统
      await this.queryAppSystem();
      // 获取骨架list
      await this.queryArchetypes();
      // 获取gitlab组信息
      await this.getGroupGit();
      this.isOnceComming = false;
      this.filterGroup = this.groups.slice(0);
    },
    handleStep2() {
      this.$v.applicationModel.$touch();
      let applicationKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('applicationModel') > -1;
      });
      validate(applicationKeys.map(key => this.$refs[key]));
      if (this.$v.applicationModel.$invalid) {
        let _this = this;
        let validateRes = applicationKeys.every(item => {
          let itemArr = item.split('.');
          return _this.$v.applicationModel[itemArr[1]].$invalid == false;
        });
        if (!validateRes) {
          return;
        }
      }
      return true;
    },
    async handleStep3() {
      this.loading = true;
      let params = {
        name_en: this.applicationModel.name_en,
        name_zh: this.applicationModel.name_zh,
        group: this.applicationModel.group.id,
        spdb_managers: this.getUserList(this.applicationModel.spdb_managers),
        dev_managers: this.getUserList(this.applicationModel.dev_managers),
        desc: this.applicationModel.desc,
        type_id: this.applicationModel.type.value,
        label: this.applicationModel.labels,
        sit: [{ name: 'SIT' }],
        sonar_scan_switch: this.applicationModel.sonar_scan_switch,
        fileEncoding: this.applicationModel.coding.value,
        system: this.applicationModel.system.id,
        isTest: this.applicationModel.isTest,
        appCiType: this.applicationModel.appCiType
      };
      if (this.isInternetApp && this.operateType === '1') {
        // 互联网条线-‘录入已有应用’页面
        params.isInternetSystem = '1'; // 校验应用英文名
        params.gitlabci_id = this.applicationModel.gitlabci_id.id;
        params.gitlab_project_id = this.applicationModel.gitlab_project_id;
        await this.addApp(params);
        this.result = this.existedAppData;
        this.step = 4;
      } else if (!this.isInternetApp && this.operateType === '1') {
        // 非互联网条线-‘录入已有应用’页面
        params.isInternetSystem = '0'; // 不校验应用英文名
        params.gitlabci_id = this.applicationModel.gitlabci_id.id;
        params.gitlab_project_id = this.applicationModel.gitlab_project_id;
        await this.addApp(params);
        this.result = this.existedAppData;
        this.step = 4;
      } else if (this.operateType === '0' && this.isVerify === '1') {
        // 用户在‘新增应用’页面，选择‘不自定义英文名’时，传递的参数
        params.isInternetSystem = '1'; // 校验应用英文名
        params.service_system = this.applicationModel.service_system.id;
        params.archetype_id = this.applicationModel.archetype.id;
        params.domain = this.applicationModel.domain.id;
        params.name_en = `${this.applicationModel.service_system.name_en}-${
          this.applicationModel.domain.name_en
        }-${this.applicationModel.name_en}`;
        params.git = this.gitPrefix + '/' + this.applicationModel.git;
        await this.saveByAsync(params);
        this.result = this.newAppData;
        //触发polling组件的start方法
        this.$refs.polling.start();
        this.appId = this.result;
        this.step = 4;
      } else if (this.operateType === '0' && this.isVerify === '0') {
        // 用户在‘新增应用’页面，选择‘自定义英文名’时，传递的参数
        params.isInternetSystem = '0'; // 不校验应用英文名
        params.service_system = '';
        params.archetype_id = this.applicationModel.archetype.id;
        params.domain = '';
        params.name_en = this.applicationModel.name_en_diy;
        params.git = this.gitPrefix + '/' + this.applicationModel.git;
        await this.saveByAsync(params);
        this.result = this.newAppData;
        //触发polling组件的start方法
        this.$refs.polling.start();
        this.appId = this.result;
        this.step = 4;
      }
    },
    getUserList(users) {
      let userList = [];
      users.forEach(item => {
        userList.push({
          user_name_en: item.user_name_en,
          user_name_cn: item.user_name_cn,
          id: item.id
        });
      });
      return userList;
    },
    toAddPipeline(appId) {
      this.$router.push({
        path: `/app/list/${appId}`
      });
    },
    viewAppDetail(path) {
      this.$router.push(path);
    },
    backToAppList() {
      this.$router.push('/app/list');
    },
    userOptionsFilter(...param) {
      let myuser = this.users.filter(item => {
        let flag = false;
        let role_labels = [];
        item.role.forEach(ele => {
          role_labels.push(ele.name);
        });
        param.forEach(r => {
          if (role_labels.includes(r)) {
            flag = true;
          }
        });
        return flag;
      });
      return myuser;
    },
    async delayTouch($v) {
      if (!$v.$model) {
        return;
      }
      if (!$v.numeric) {
        return;
      }
      try {
        await this.queryProject({
          id: this.applicationModel.gitlab_project_id
        });
        this.gitlabId = true;
      } catch (err) {
        this.gitlabId = false;
      }
      $v.$touch();
      validate([this.$refs['applicationModel.gitlab_project_id']]);
      if (!this.gitlabId) {
        this.applicationModel.name_en = '';
        return;
      }
      if (!this.isInternetApp) {
        try {
          await this.queryApplicationName({
            gitlab_project_id: this.applicationModel.gitlab_project_id,
            type_name: this.applicationModel.type.label,
            isInternetSystem: '0' // 非互联网条线应用‘录入已有应用’页面，不校验‘应用英文名’
          });
          this.applicationModel.name_en = this.applicationName;
        } catch (err) {
          this.applicationModel.name_en = '';
        }
      } else {
        try {
          await this.queryApplicationName({
            gitlab_project_id: this.applicationModel.gitlab_project_id,
            type_name: this.applicationModel.type.label,
            isInternetSystem: '1' // 互联网条线应用‘录入已有应用’页面，校验‘应用英文名’
          });
          this.applicationModel.name_en = this.applicationName;
        } catch (err) {
          this.applicationModel.name_en = '';
        }
      }
    },
    async appTypeTouch() {
      if (
        this.applicationModel.type &&
        this.applicationModel.gitlab_project_id &&
        !this.isInternetApp
      ) {
        try {
          await this.queryApplicationName({
            gitlab_project_id: this.applicationModel.gitlab_project_id,
            type_name: this.applicationModel.type.label,
            isInternetSystem: '0' // 非互联网条线应用‘录入已有应用’页面，不校验‘应用英文名’
          });
          this.applicationModel.name_en = this.applicationName;
        } catch (err) {
          this.applicationModel.name_en = '';
        }
      } else if (
        this.applicationModel.type &&
        this.applicationModel.gitlab_project_id &&
        this.isInternetApp
      ) {
        try {
          await this.queryApplicationName({
            gitlab_project_id: this.applicationModel.gitlab_project_id,
            type_name: this.applicationModel.type.label,
            isInternetSystem: '1' // 互联网条线应用‘录入已有应用’页面，校验‘应用英文名’
          });
          this.applicationModel.name_en = this.applicationName;
        } catch (err) {
          this.applicationModel.name_en = '';
        }
      }
    },
    toConfirm() {
      if (this.handleStep2()) {
        if (this.operateType === '0') {
          this.$q
            .dialog({
              title: `git代码库确认提示`,
              message: `请确认git代码库地址：
                ${this.gitPrefix}/${this.applicationModel.git}`,
              ok: true,
              cancel: true
            })
            .onOk(() => {
              this.step = 3;
            });
        } else {
          this.step = 3;
        }
      }
    },
    // 查询group是否存在,点击确认后新建
    async toGetGroup(status) {
      if (!this.$v.applicationModel.git.isUnique) {
        return false; // 存在
      } else {
        await this.getGroup({
          group: `/${this.gitPrefix}/${this.applicationModel.git}`,
          status: status
        });
        this.existence = this.groupData;
        this.$v.applicationModel.git.$reset();
        this.$v.applicationModel.git.$touch();
        validate([this.$refs['applicationModel.git']]);
        if (this.gitPrefix && this.applicationModel.git) {
          if (!this.existence) {
            this.$q
              .dialog({
                title: `新建${this.applicationModel.git}`,
                message: `${this.gitPrefix}的${
                  this.applicationModel.git
                }不存在，是否新建该${this.applicationModel.git}`,
                ok: true,
                cancel: true
              })
              .onOk(() => {
                this.toGetGroup('1'); //不存在 --> 新建
              });
          }
        }
        return true;
      }
    },
    groupInputFilter(val, update) {
      update(() => {
        this.filterGroup = this.groups.filter(
          tag => tag.fullName.indexOf(val) > -1
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
    groupGitInputFilter(val, update) {
      update(() => {
        this.filtergroupGit = this.groupGitList.filter(
          tag => tag.full_path.indexOf(val) > -1
        );
      });
    },
    systemInputFilter(val, update) {
      update(() => {
        this.filterSystem = this.systems.filter(
          tag => tag.name_cn.indexOf(val) > -1
        );
      });
    },
    domainInputFilter(val, update) {
      update(() => {
        this.filterDomain = this.domains.filter(
          tag => tag.name_cn.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    },
    archetypesInputFliter(val, update) {
      update(() => {
        this.filterArchetypes = this.archetypeList.filter(
          tag => tag.name_cn.indexOf(val) > -1
        );
      });
    },
    CITemplatesInputFliter(val, update) {
      update(() => {
        this.filterCITemplates = this.CITemplates.filter(
          tag => tag.name.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    },
    toDeployMessage(appId) {
      this.$router.push({
        path: '/envModel/handle',
        query: {
          appId
        }
      });
    },
    handleEnvDeploy(params) {
      if (params === '否') {
        this.applicationModel.labels.push('不涉及环境部署');
      } else {
        const index = this.applicationModel.labels.indexOf('不涉及环境部署');
        this.applicationModel.labels.splice(index, 1);
      }
    },
    handleIsVerify(params) {
      if (params === '是') {
        this.isVerify = '0';
        this.applicationModel.name_en_diy = '';
      } else {
        this.isVerify = '1';
        if (this.applicationModel.domain) {
          this.applicationModel.domain = this.domains.find(
            item => item.name_cn === '自定义域'
          );
        }
        if (this.applicationModel.service_system) {
          this.applicationModel.service_system = this.systems.find(
            item => item.name_cn === '自定义系统'
          );
        }
        this.applicationModel.name_en = '';
      }
      this.applicationModel.service_system.name_en = '';
      this.applicationModel.domain.name_en = '';
      this.$v.applicationModel.$reset();
    },
    removeEnvDep(params) {
      if (params.value === '不涉及环境部署') {
        this.applicationModel.env_deploy = '涉及环境部署';
      }
    }
  },
  async created() {
    if (
      !this.currentUser.role.some(
        role =>
          role.name === '厂商项目负责人' ||
          role.name === '行内项目负责人' ||
          role.name === '卡点管理员'
      )
    ) {
      errorNotify(
        '当前用户无权限新建应用，只有行内/厂商项目负责人才可执行此操作!'
      );
      this.$router.push('/app/list');
    }
    // 查询互联网及其子组
    await this.queryChildGroupById({ id: '5c81c4d0d3e2a1126ce30049' });
    await this.queryAppType();
    this.appTypes = this.appTypeData.slice(0);
  }
};
</script>

<style lang="stylus" scoped>

/deep/.q-select .q-field__input--padding
  padding-left 0
.step1-form
  max-width: 500px;
  margin: 32px auto 0;
.prefix
  color: #607d8b;
  font-size: 14px;
.line-link
  text-align center
  vertical middle
  display inline-block
.warning
  font-size 1px;
  color rgb(193, 0, 21);
.formCells
  padding-bottom 20px
.desc-style
  word-break break-all
</style>
