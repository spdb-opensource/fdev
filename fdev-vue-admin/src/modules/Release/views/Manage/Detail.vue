<template>
  <div>
    <f-block class="q-mb-md">
      <div class="row">
        <f-formitem class="col-4" :label="desc + '日期：'">
          {{ releaseNodeDetail.release_date }}
        </f-formitem>
        <f-formitem class="col-4" :label="desc + '窗口名称：'">
          <div
            class="ellipsis-3-lines no-wrap"
            :title="releaseNodeDetail.release_node_name"
          >
            {{ releaseNodeDetail.release_node_name }}
          </div>
        </f-formitem>
        <f-formitem class="col-4" label="窗口类型：">
          {{ typeFilter[releaseNodeDetail.type] || '微服务窗口' }}
        </f-formitem>
        <f-formitem class="col-4" label="科技负责人：">
          <router-link
            :to="`/user/list/${releaseNodeDetail.release_spdb_manager_id}`"
            class="link"
            v-if="releaseNodeDetail.release_spdb_manager_id"
          >
            {{ releaseNodeDetail.release_spdb_manager_name_cn }}
          </router-link>
          <span v-else>{{
            releaseNodeDetail.release_spdb_manager_name_cn
          }}</span>
        </f-formitem>
        <f-formitem class="col-4" :label="desc + '负责人：'">
          <router-link
            :to="`/user/list/${releaseNodeDetail.release_manager_id}`"
            class="link"
            v-if="releaseNodeDetail.release_manager_id"
          >
            {{ releaseNodeDetail.release_manager_name_cn }}
          </router-link>
          <span v-else>{{ releaseNodeDetail.release_manager_name_cn }}</span>
        </f-formitem>

        <f-formitem class="col-4" label="创建人：">
          <router-link
            :to="`/user/list/${releaseNodeDetail.create_user}`"
            class="link"
            v-if="releaseNodeDetail.create_user"
          >
            {{ releaseNodeDetail.create_user_name_cn }}
          </router-link>
          <span v-else>{{ releaseNodeDetail.create_user_name_cn }}</span>
        </f-formitem>

        <f-formitem class="col-4" :label="desc + '小组：'">
          <div
            class="ellipsis-3-lines no-wrap"
            :title="releaseNodeDetail.groupFullName"
          >
            {{ releaseNodeDetail.groupFullName }}
          </div>
        </f-formitem>
      </div>
      <div class="row justify-center q-gutter-md q-mt-md">
        <span>
          <fdev-btn
            label="编辑"
            @click="handleupdateRelease"
            :disable="
              !compareTime ||
                !(releaseNodeDetail.can_operation || isKaDianManager)
            "
          />
          <fdev-tooltip
            v-if="
              !compareTime ||
                !(releaseNodeDetail.can_operation || isKaDianManager)
            "
          >
            <span v-if="!compareTime">
              投产窗口已过期
            </span>
            <span
              v-if="
                compareTime &&
                  !(releaseNodeDetail.can_operation || isKaDianManager)
              "
            >
              请联系当前投产窗口所属组成员的第三层级组及其子组的投产管理员
            </span>
          </fdev-tooltip>
        </span>

        <span>
          <fdev-btn
            label="新建变更"
            @click="openCreateChanges"
            :disable="
              !compareTime ||
                releaseNodeDetail.type === '3' ||
                Boolean(componentReleaseType) ||
                !(releaseNodeDetail.can_operation || isKaDianManager)
            "
          />
          <fdev-tooltip
            v-if="
              !compareTime ||
                releaseNodeDetail.type === '3' ||
                Boolean(componentReleaseType) ||
                !(releaseNodeDetail.can_operation || isKaDianManager)
            "
          >
            <span v-if="!compareTime">
              投产窗口已过期
            </span>
            <span
              v-if="
                compareTime &&
                  (releaseNodeDetail.type === '3' ||
                    Boolean(componentReleaseType))
              "
            >
              试运行或非应用投产窗口不能新建变更
            </span>
            <span
              v-if="
                compareTime &&
                  !(
                    releaseNodeDetail.type === '3' ||
                    Boolean(componentReleaseType)
                  ) &&
                  !(releaseNodeDetail.can_operation || isKaDianManager)
              "
            >
              请联系当前投产窗口所属组成员的第三层级组及其子组的投产管理员
            </span>
          </fdev-tooltip>
        </span>

        <span>
          <fdev-btn
            :disable="
              !compareTime ||
                releaseNodeDetail.type === '3' ||
                Boolean(componentReleaseType) ||
                !(releaseNodeDetail.can_operation || isKaDianManager)
            "
            label="新建发布说明"
            @click="openAutoReleaseNoteDialog"
          />
          <fdev-tooltip
            v-if="
              !compareTime ||
                releaseNodeDetail.type === '3' ||
                Boolean(componentReleaseType) ||
                !(releaseNodeDetail.can_operation || isKaDianManager)
            "
          >
            <span v-if="!compareTime">
              投产窗口已过期
            </span>
            <span
              v-if="
                compareTime &&
                  (releaseNodeDetail.type === '3' ||
                    Boolean(componentReleaseType))
              "
            >
              试运行或非应用投产窗口不能新建发布说明
            </span>
            <span
              v-if="
                compareTime &&
                  !(
                    releaseNodeDetail.type === '3' ||
                    Boolean(componentReleaseType)
                  ) &&
                  !(releaseNodeDetail.can_operation || isKaDianManager)
              "
            >
              请联系当前投产窗口所属组成员的第三层级组及其子组的投产管理员
            </span>
          </fdev-tooltip>
        </span>

        <span>
          <fdev-btn
            label="生成投产材料"
            :disable="
              !compareTime ||
                releaseNodeDetail.type === '3' ||
                Boolean(componentReleaseType) ||
                !(releaseNodeDetail.can_operation || isKaDianManager)
            "
            @click="clickRealeseFile"
          />
          <fdev-tooltip
            v-if="
              !compareTime ||
                releaseNodeDetail.type === '3' ||
                Boolean(componentReleaseType) ||
                !(releaseNodeDetail.can_operation || isKaDianManager)
            "
          >
            <span v-if="!compareTime">
              投产窗口已过期
            </span>
            <span
              v-if="
                compareTime &&
                  (releaseNodeDetail.type === '3' ||
                    Boolean(componentReleaseType))
              "
            >
              试运行或非应用投产窗口不能生成投产材料
            </span>
            <span
              v-if="
                compareTime &&
                  !(
                    releaseNodeDetail.type === '3' ||
                    Boolean(componentReleaseType)
                  ) &&
                  !(releaseNodeDetail.can_operation || isKaDianManager)
              "
            >
              请联系当前投产窗口所属组成员的第三层级组及其子组的投产管理员
            </span>
          </fdev-tooltip>
        </span>

        <fdev-btn
          type="button"
          label="返回"
          @click="goReleaseList"
          color="primary"
          text-color="white"
        />
      </div>

      <f-dialog v-model="realeseFileDialog" right title="生成投产材料">
        <f-formitem diaS label="材料类型">
          <fdev-select
            input-debounce="0"
            :options="releaseFileType"
            option-value="releasefile_type"
            option-label="name"
            emit-value
            map-options
            v-model="releaseFileModel.type"
            :rules="[() => true]"
          />
        </f-formitem>
        <f-formitem
          diaS
          label="应用所属系统"
          required
          v-if="releaseFileModel.type !== 'sourceCodeScanInfo'"
        >
          <fdev-select
            ref="releaseFileModel.system"
            input-debounce="0"
            use-input
            multiple
            @filter="systemInputFilter"
            v-model="$v.releaseFileModel.system.$model"
            :options="filterSystem"
            option-label="name"
            option-value="id"
            map-options
            emit-value
            :rules="[
              () => $v.releaseFileModel.system.require || '请选择应用所属系统'
            ]"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem diaS label="问题单号" required v-if="examFileType">
          <fdev-input
            ref="releaseFileModel.questionNumber"
            input-debounce="0"
            type="text"
            prefix="SPDB"
            v-model="$v.releaseFileModel.questionNumber.$model"
            :rules="[
              () =>
                $v.releaseFileModel.questionNumber.required || '请输入问题单号',
              () =>
                $v.releaseFileModel.questionNumber.examine ||
                '请输入11位数字单号'
            ]"
          />
        </f-formitem>
        <Loading
          :visible="queryByReleaseNodeNameLoading"
          v-if="releaseFileModel.type === 'source'"
        >
          <fdev-table
            titleIcon="list_s_f"
            title="业务需求"
            no-select-cols
            :data="releaseFiles.business_rqrmnt_list"
            :columns="rqrmnt_columns"
            row-key="id"
            selection="multiple"
            :selected.sync="selectedBusinessRqrmnt"
            noExport
          >
            <template v-slot:body-selection="props">
              <fdev-checkbox v-model="props.selected" />
            </template>
          </fdev-table>
          <fdev-table
            titleIcon="list_s_f"
            title="科技需求"
            no-select-cols
            :data="releaseFiles.tech_rqrmnt_list"
            :columns="rqrmnt_columns"
            row-key="id"
            selection="multiple"
            :selected.sync="selectedTechRqrmnt"
            noExport
          >
            <template v-slot:body-selection="props">
              <fdev-checkbox v-model="props.selected" />
            </template>
          </fdev-table>
        </Loading>
        <template v-slot:btnSlot>
          <fdev-btn
            v-if="releaseFileModel.type !== 'source'"
            @click="handleCreateRelease"
            dialog
            label="确认"
            :loading="globalLoading['releaseForm/addRedLineScanReport']"
          />
          <span v-else>
            <fdev-btn
              @click="handleCreateRelease"
              dialog
              label="确认"
              :loading="
                globalLoading['releaseForm/querySourceReviewDetail'] ||
                  queryByReleaseNodeNameLoading
              "
              :disable="
                queryByReleaseNodeNameLoading ||
                  (selectedBusinessRqrmnt.length === 0 &&
                    selectedTechRqrmnt.length === 0)
              "
            />
            <fdev-tooltip
              v-if="
                selectedBusinessRqrmnt.length === 0 &&
                  selectedTechRqrmnt.length === 0
              "
            >
              请勾选科技需求或业务需求
            </fdev-tooltip>
          </span>
        </template>
      </f-dialog>

      <f-dialog v-model="sourceOpened" right title="源代码审查登记表">
        <div style="width:1100px">
          <div class="row">
            <f-formitem diaS label="系统名称" class="col-6">
              <fdev-input
                disable
                :title="sourceDetail.sysname_cn"
                v-model="sourceDetail.sysname_cn"
                :rules="[() => true]"
              />
            </f-formitem>
            <f-formitem diaS label="投产日期" class="col-6">
              <fdev-input
                disable
                :title="sourceDetail.release_date"
                v-model="sourceDetail.release_date"
                :rules="[() => true]"
              />
            </f-formitem>
            <f-formitem diaS label="代码审查人" class="col-6">
              <fdev-select
                input-debounce="0"
                use-input
                @filter="filterSpdbMaster"
                :options="spdbMaster"
                option-label="user_name_cn"
                option-value="user_name_en"
                ref="sourceDetail.spdb_managers"
                v-model="$v.sourceDetail.spdb_managers.$model"
                :rules="[
                  () =>
                    $v.sourceDetail.spdb_managers.required || '请输入代码审查人'
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
            <f-formitem diaS label="代码审查负责人" class="col-6">
              <fdev-select
                input-debounce="0"
                use-input
                option-label="user_name_cn"
                option-value="user_name_en"
                ref="sourceDetail.spdb_master"
                @filter="managerFilter"
                :options="managerOptions"
                v-model="$v.sourceDetail.spdb_master.$model"
                :rules="[
                  () =>
                    $v.sourceDetail.spdb_master.required ||
                    '请输入代码审查负责人'
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
            <f-formitem diaS label="审查日期" class="col-6">
              <fdev-input
                disable
                :rules="[() => true]"
                :title="sourceDetail.date"
                v-model="sourceDetail.date"
              />
            </f-formitem>
          </div>

          <fdev-table
            titleIcon="list_s_f"
            title="源代码审查登记表"
            no-select-cols
            class="my-sticky-column-table"
            :data="sourceDetail.list"
            :columns="source_columns"
            :selected.sync="selectedBusinessRqrmnt"
            noExport
          >
            <template v-slot:header="props">
              <fdev-tr :props="props">
                <fdev-th class="text-left">序号</fdev-th>
                <fdev-th class="text-left">问题单号</fdev-th>
                <fdev-th class="text-left">申请人</fdev-th>
                <fdev-th class="text-left">程序名称</fdev-th>
                <fdev-th
                  class="text-left text-ellipsis"
                  style="max-width:300px"
                  title="审查结果（通过，但需按审查意见整改，否决）"
                >
                  审查结果（通过，但需按审查意见整改，否决）
                </fdev-th>
                <fdev-th class="text-left">备注</fdev-th>
              </fdev-tr>
            </template>
            <template v-slot:body-cell-name="props">
              <fdev-td class="text-ellipsis">
                {{ props.row.name ? props.row.name.name_cn : '' }}
                <fdev-icon
                  size="12px"
                  name="edit"
                  v-ripple
                  color="primary"
                  @click="() => saveValue(props.row.name.name_en)"
                  class="cursor"
                >
                  <fdev-popup-edit
                    v-model="props.row.name"
                    buttons
                    :validate="val => inputValidation(val)"
                    @hide="hideInput"
                  >
                    <fdev-select
                      use-input
                      v-model="props.row.name"
                      @filter="managerFilter"
                      :options="managerOptions"
                      :error="errorProtein"
                      :rules="[() => !errorProtein || errorMessageProtein]"
                      :error-message="errorMessageProtein"
                      option-label="name_cn"
                      option-value="name_en"
                    >
                      <template v-slot:option="scope">
                        <fdev-item
                          v-bind="scope.itemProps"
                          v-on="scope.itemEvents"
                        >
                          <fdev-item-section>
                            <fdev-item-label :title="scope.opt.user_name_cn">
                              {{ scope.opt.user_name_cn }}
                            </fdev-item-label>
                            <fdev-item-label
                              caption
                              :title="scope.opt.user_name_en"
                            >
                              {{ scope.opt.user_name_en }}
                            </fdev-item-label>
                          </fdev-item-section>
                        </fdev-item>
                      </template>
                    </fdev-select>
                    <!-- <fdev-input
                    v-model="props.row.name"
                    dense
                    autofocus
                    :error="errorProtein"
                    :error-message="errorMessageProtein"
                  /> -->
                  </fdev-popup-edit>
                </fdev-icon>
              </fdev-td>
            </template>
          </fdev-table>
        </div>

        <template v-slot:btnSlot>
          <fdev-btn
            @click="handleCreateFile"
            dialog
            label="确认"
            :loading="globalLoading['releaseForm/addSourceReview']"
          />
        </template>
      </f-dialog>
      <ReleaseDialog
        :releaseDetail="releaseNodeDetail"
        :thirdLevelGroups="thirdLevelGroups && thirdLevelGroups.groups"
        type="update"
        v-model="updateReleaseModalOpened"
        @click="updateReleaseNode"
      />

      <f-dialog v-model="groupAbbrModelOpened" title="修改小组标识">
        <div class="q-gutter-md">
          <f-formitem diaS label="小组名称">
            <fdev-input
              input-debounce="0"
              type="text"
              :title="group"
              readonly
              :value="group"
            />
          </f-formitem>
          <f-formitem diaS label="小组标识">
            <fdev-select
              input-debounce="0"
              use-input
              @filter="groupAbbrFilter"
              :options="groupAbbrOptions"
              option-value="group_abbr"
              option-label="group_abbr"
              emit-value
              map-options
              v-model="$v.groupAbbrModel.$model"
              :rules="[() => $v.groupAbbrModel.required || '请输入小组标识']"
            />
            <!-- <fdev-input
              input-debounce="0"
              type="text"
              v-model="$v.groupAbbrModel.$model"
              :rules="[
                () => $v.groupAbbrModel.required || '请输入小组标识',
                () => $v.groupAbbrModel.examine || '只能输入数字和字母'
              ]"
            /> -->
          </f-formitem>
        </div>
        <template v-slot:btnSlot>
          <fdev-btn
            dialog
            @click="handleGroupAbbrModel"
            :loading="globalLoading['releaseForm/updateGroupAbbr']"
            label="确认"
            :disable="!groupAbbrModel"
          />
        </template>
      </f-dialog>
      <f-dialog v-model="createUpdate" persistent title="新建变更" right>
        <div style="width:520px">
          <f-formitem full-width label="小组标识">
            <div class="row">
              <div class="col col-10">
                <fdev-input
                  v-model="group_abbr"
                  readonly
                  :rules="[() => true]"
                />
              </div>
              <div class="col col-2 text-center">
                <fdev-btn
                  :label="group_abbr ? '修改' : '新增'"
                  flat
                  @click="handleGroupAbbrModelOpen"
                />
              </div>
            </div>
          </f-formitem>

          <f-formitem full-width label="小组名称">
            <fdev-input
              input-debounce="0"
              :rules="[() => true]"
              type="text"
              readonly
              :title="group"
              :value="group"
            />
          </f-formitem>

          <f-formitem full-width label="变更类型">
            <fdev-select
              input-debounce="0"
              :options="templateType"
              ref="newChangeModel.type"
              emit-value
              option-value="template_type"
              option-label="name"
              map-options
              v-model="$v.newChangeModel.type.$model"
              :rules="[
                () => $v.newChangeModel.type.required || '请选择变更类型'
              ]"
            />
          </f-formitem>

          <f-formitem full-width label="变更日期">
            <f-date
              mask="YYYY/MM/DD"
              :rules="[
                () => $v.newChangeModel.date.required || '请选择变更日期'
              ]"
              @input="findVersion($event, newChangeModel.plan_time)"
              v-model="$v.newChangeModel.date.$model"
            />
          </f-formitem>

          <f-formitem full-width label="预期发布时间">
            <el-time-picker
              ref="newChangeModel.plan_time"
              v-model="$v.newChangeModel.plan_time.$model"
              format="HH:mm"
              value-format="HH:mm"
              class="release-pre-publish-time"
              @change="findVersion(newChangeModel.date, $event)"
            >
            </el-time-picker>
          </f-formitem>

          <f-formitem full-width label="变更版本">
            <div class="row" :class="{ 'q-mb-md': cls }">
              <fdev-input
                ref="newChangeModel.vContainer"
                v-model="$v.newChangeModel.vContainer.$model"
                type="text"
                class="time-special-input col q-mt-xs"
                :rules="[
                  () => $v.newChangeModel.vContainer.myRequired || '请输入容器',
                  () =>
                    $v.newChangeModel.vContainer.separator ||
                    '不能输入“_”和空格'
                ]"
              />
              <span class="line-link q-mt-md">-</span>
              <fdev-input
                ref="newChangeModel.vGroup"
                v-model="$v.newChangeModel.vGroup.$model"
                type="text"
                class="time-special-input col q-mt-xs"
                :rules="[
                  () => $v.newChangeModel.vGroup.myRequired || '请输入小组标识',
                  () =>
                    $v.newChangeModel.vGroup.separator || '不能输入“_”和空格'
                ]"
              />
              <span class="line-link">-</span>
              <fdev-input
                ref="newChangeModel.vDate"
                v-model="$v.newChangeModel.vDate.$model"
                type="text"
                class="time-special-input col-2 q-mt-xs"
                disable
                :rules="[]"
              >
              </fdev-input>
              <span class="line-link">-</span>
              <fdev-input
                ref="newChangeModel.Vtime"
                v-model="$v.newChangeModel.Vtime.$model"
                type="text"
                class="time-special-input col-2 q-mt-xs"
                disable
                :rules="[]"
              />
              <span class="line-link">-</span>
              <fdev-input
                ref="newChangeModel.vEnd"
                v-model="$v.newChangeModel.vEnd.$model"
                type="text"
                placeholder="选填"
                class="time-special-input col-2 q-mt-xs"
                :rules="[]"
              />
            </div>
          </f-formitem>
          <f-formitem full-width label="变更单号">
            <fdev-input
              input-debounce="0"
              ref="newChangeModel.prod_spdb_no"
              v-model="$v.newChangeModel.prod_spdb_no.$model"
              :rules="[
                () =>
                  $v.newChangeModel.prod_spdb_no.required || '请填写变更编号',
                () =>
                  $v.newChangeModel.prod_spdb_no.onlyNumOrWord ||
                  '只能由英文字母或数字组成'
              ]"
            />
          </f-formitem>
          <f-formitem full-width class="q-mb-md" label="是否自动化发布">
            <fdev-toggle
              false-value="0"
              true-value="1"
              v-model="newChangeModel.image_deliver_type"
            />
          </f-formitem>
          <f-formitem
            full-width
            label="应用系统"
            v-if="newChangeModel.image_deliver_type === '1'"
          >
            <fdev-select
              ref="newChangeModel.system"
              input-debounce="0"
              use-input
              @input="queryExcel"
              :options="systemOptions"
              v-model="$v.newChangeModel.system.$model"
              :rules="[
                () => $v.newChangeModel.system.required || '请选择应用系统'
              ]"
              @filter="systemFilter"
              option-label="name_cn"
              option-value="name_cn"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name_cn">{{
                      scope.opt.name_cn
                    }}</fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.name_en">
                      {{ scope.opt.name_en }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <div
            class="text-right"
            v-if="
              systemOptions.length === 0 &&
                newChangeModel.image_deliver_type === '1'
            "
          >
            <span>该变更类型下暂无可选变更模板，前往新建变更模板。</span>
            <router-link to="/release/changeTemplate" class="link">
              点击前往
            </router-link>
          </div>
          <f-formitem
            label="excel模版"
            label-style="width: 120px;"
            value-style="width: 384px"
            v-if="!!newChangeModel.system"
          >
            <fdev-select
              ref="newChangeModel.excel_template_url"
              input-debounce="0"
              use-input
              :options="templateOfChange"
              option-label="filename"
              v-model="$v.newChangeModel.excel_template_url.$model"
              :rules="[
                () =>
                  $v.newChangeModel.excel_template_url.required ||
                  '请选择变更模版'
              ]"
              @filter="templateFilter"
            />
          </f-formitem>

          <f-formitem full-width label="涉及变更应用">
            <fdev-tree
              v-if="
                simple[0].children[0].children.length > 0 ||
                  isConfigSum === '0' ||
                  isConfigSum === '1'
              "
              class="release-tree-node"
              :nodes="simple"
              node-key="application_id"
              tick-strategy="leaf"
              :selected.sync="selected"
              :ticked.sync="$v.newChangeModel.applications.$model"
              :expanded.sync="expanded"
              ref="newChangeModel.applications"
              @update:ticked="tickNode"
            >
              <template v-slot:default-body="prop">
                <div v-if="prop.node.disableLabel">
                  {{ prop.node.disableLabel }}
                </div>
              </template>
            </fdev-tree>
            <span v-else>无</span>
          </f-formitem>
        </div>
        <template v-slot:btnSlot>
          <fdev-btn
            @click="handleAllTip"
            :loading="globalLoading['releaseForm/createAChanges']"
            dialog
            label="确认"
          />
        </template>
      </f-dialog>
      <f-dialog
        v-model="autoReleaseNoteDialogModel"
        persistent
        dense
        title="新建发布说明"
        right
      >
        <f-formitem diaS class="q-mb-md" required label="类型">
          <fdev-radio
            class="q-mr-md"
            v-model="newReleaseNoteModel.image_deliver_type"
            val="1"
            label="自动"
          />
          <fdev-radio
            v-model="newReleaseNoteModel.image_deliver_type"
            val="2"
            label="手动"
          />
        </f-formitem>

        <f-formitem full-width required label="小组标识">
          <div class="row">
            <div class="col col-10">
              <fdev-input v-model="group_abbr" readonly :rules="[() => true]" />
            </div>
            <div class="col col-2 text-center">
              <fdev-btn
                :label="group_abbr ? '修改' : '新增'"
                flat
                @click="handleGroupAbbrModelOpen"
              />
            </div>
          </div>
        </f-formitem>

        <f-formitem diaS required label="小组名称">
          <fdev-input
            :title="group"
            input-debounce="0"
            :rules="[() => true]"
            type="text"
            readonly
            :value="group"
          />
        </f-formitem>

        <f-formitem diaS required label="发布说明类型">
          <fdev-select
            input-debounce="0"
            :options="templateType"
            ref="newReleaseNoteModel.type"
            emit-value
            option-value="template_type"
            option-label="name"
            map-options
            v-model="$v.newReleaseNoteModel.type.$model"
            :rules="[
              () => $v.newReleaseNoteModel.type.required || '请选择发布说明类型'
            ]"
          />
        </f-formitem>

        <f-formitem
          diaS
          required
          label="网段"
          v-if="newReleaseNoteModel.image_deliver_type === '1'"
        >
          <fdev-select
            input-debounce="0"
            :options="namespaceOptions"
            ref="newReleaseNoteModel.namespace"
            emit-value
            map-options
            :disable="
              newReleaseNoteModel.type === 'gray' || namespaceDisableFlag
            "
            v-model="$v.newReleaseNoteModel.namespace.$model"
            :rules="[
              () => $v.newReleaseNoteModel.namespace.myRequired || '请选择网段'
            ]"
          />
        </f-formitem>

        <f-formitem diaS required label="发布说明日期">
          <f-date
            mask="YYYY/MM/DD"
            :rules="[
              () => $v.newReleaseNoteModel.date.required || '请选择发布说明日期'
            ]"
            @input="findReleaseVersion($event, newReleaseNoteModel.plan_time)"
            v-model="$v.newReleaseNoteModel.date.$model"
          />
        </f-formitem>

        <f-formitem
          v-if="newReleaseNoteModel.image_deliver_type === '1'"
          diaS
          required
          help="生产：一批次(20:00-21:59),二批次(22:00-23:59),三批次(00:00-07:00)
            灰度：一批次(13:00-15:59),二批次(16:00-17:59),三批次(18:00-23:59)"
          label="预期发布时间"
        >
          <el-time-picker
            ref="newReleaseNoteModel.plan_time"
            v-model="$v.newReleaseNoteModel.plan_time.$model"
            format="HH:mm"
            value-format="HH:mm"
            class="release-note-pre-publish-time"
            @change="findReleaseVersion(newReleaseNoteModel.date, $event)"
          >
          </el-time-picker>
        </f-formitem>
        <f-formitem
          diaS
          required
          label="批次"
          v-if="newReleaseNoteModel.image_deliver_type === '2'"
        >
          <fdev-select
            input-debounce="0"
            :options="batchOptions"
            ref="newReleaseNoteModel.note_batch"
            emit-value
            map-options
            v-model="newReleaseNoteModel.note_batch"
            :rules="[() => true]"
          />
        </f-formitem>

        <f-formitem diaS required label="发布说明名称">
          <fdev-input
            :title="newReleaseNoteModel.release_note_name"
            ref="newReleaseNoteModel.release_note_name"
            v-model="$v.newReleaseNoteModel.release_note_name.$model"
            type="text"
            :rules="[
              () =>
                $v.newReleaseNoteModel.release_note_name.required ||
                '请填写发布说明名称'
            ]"
          />
        </f-formitem>
        <f-formitem diaS required label="应用系统">
          <fdev-select
            ref="newReleaseNoteModel.system"
            input-debounce="0"
            use-input
            :options="releaseNodeSystemOptions"
            v-model="$v.newReleaseNoteModel.system.$model"
            :rules="[
              () => $v.newReleaseNoteModel.system.required || '请选择应用系统'
            ]"
            @filter="systemReleaseFilter"
            option-label="name"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem
          diaS
          required
          label="租户"
          v-if="newReleaseNoteModel.image_deliver_type === '1'"
        >
          <fdev-select
            input-debounce="0"
            :options="leaseholderOptions"
            ref="newReleaseNoteModel.leaseholder"
            emit-value
            map-options
            :disable="leaseholderOptions.length === 1"
            v-model="$v.newReleaseNoteModel.leaseholder.$model"
            :rules="[
              () =>
                $v.newReleaseNoteModel.leaseholder.myRequired || '请选择租户'
            ]"
          />
        </f-formitem>
        <template v-slot:btnSlot>
          <fdev-btn
            @click="handleReleaseTip()"
            :loading="globalLoading['releaseForm/createNote']"
            dialog
            label="确认"
          />
        </template>
      </f-dialog>
    </f-block>
    <f-block>
      <fdev-tabs
        class="q-ml-md"
        v-if="dimensions.length >= 1"
        align="left"
        to="/release/joblist"
      >
        <fdev-route-tab
          v-for="each in dimensions"
          :key="each.path"
          :to="each.path"
          exact
          :label="each.name"
        />
      </fdev-tabs>
      <fdev-separator class="q-mb-md" />
      <router-view v-if="releaseNodeDetail && releaseNodeDetail.type" />
    </f-block>
  </div>
</template>

<script>
import { required } from 'vuelidate/lib/validators';
import {
  releaseNodeDetail,
  newReleaseNoteModel,
  newChangeModel,
  batchArr
} from '../../utils/model.js';

import {
  leaseholderOptions,
  batchOptions,
  namespaceOptions
} from '../../utils/constants.js';
import Loading from '@/components/Loading';
import ReleaseDialog from './components/releaseDialog';
import moment from 'moment';
import { mapState, mapMutations, mapActions, mapGetters } from 'vuex';
import {
  isValidReleaseDate,
  getGroupFullName,
  successNotify,
  errorNotify,
  validate,
  perform,
  deepClone
} from '@/utils/utils';
export default {
  name: 'Detail',
  components: { ReleaseDialog, Loading },
  data() {
    return {
      namespaceOptions: namespaceOptions,
      batchOptions: batchOptions,
      leaseholderOptions: [],
      isReleaseNote: false,
      typeFilter: perform.typeFilter,
      roleData: {},
      releaseNodeDetail: releaseNodeDetail(),
      id: '',
      dimensions: [],
      updateReleaseModalOpened: false,
      compareTime: false,
      namespaceDisableFlag: false,
      user: [],
      isCurrentUserGroup: false,
      isMangers: false,
      createUpdate: false, // 新建变更
      autoReleaseNoteDialogModel: false, // 新建发布说明
      simple: [
        {
          label: '涉及变更应用',
          application_id: '',
          noTick: true,
          children: [
            {
              label: '应用列表',
              noTick: false,
              children: []
            },
            {
              label: '配置文件变更应用',
              application_id: '配置文件变更应用',
              children: [],
              noTick: false,
              disabled: false
            }
          ]
        }
      ], // tree's data
      ticked: [], // selected data
      expanded: [], //
      selected: '应用列表',
      newReleaseNoteModel: newReleaseNoteModel(),
      newChangeModel: newChangeModel(),
      templateType: [
        {
          name: '灰度',
          template_type: 'gray'
        },
        {
          name: '生产',
          template_type: 'proc'
        }
      ],
      templateOfChange: [], // 变更模板列表
      temp_name: '',
      group_abbr: '', // 小组标识
      groupAbbrModel: '',
      groupAbbrModelOpened: false,
      realeseFileDialog: false,
      releaseFileType: [
        {
          name: '源代码审查表',
          releasefile_type: 'source'
        },
        {
          name: '系统测试报告',
          releasefile_type: 'systemTest'
        },
        {
          name: '源代码扫描信息',
          releasefile_type: 'sourceCodeScanInfo'
        }
      ],
      userListCopy: [],
      managerOptions: [],
      groupAbbrOptions: [],
      spdbMaster: [],
      currentEditValue: '',
      queryByReleaseNodeNameLoading: false,
      rqrmnt_columns: [
        { name: 'rqrmnt_num', label: '需求编号', field: 'rqrmnt_num' },
        {
          name: 'rqrmnt_name',
          label: '需求名',
          field: 'rqrmnt_name'
        }
      ], // 需求列名
      selectedBusinessRqrmnt: [],
      selectedTechRqrmnt: [],
      sourceOpened: false,
      sourceDetail: {
        sysname_cn: '',
        release_date: '',
        spdb_managers: '',
        spdb_master: '',
        date: '',
        list: []
      },
      source_columns: [
        {
          name: 'index',
          label: '序号',
          field: 'index'
        },
        { name: 'question_no', label: '问题单号', field: 'question_no' },
        {
          name: 'name',
          label: '申请人',
          field: 'name'
        },
        {
          name: 'path',
          label: '程序名称',
          field: 'path'
        },
        {
          name: 'result',
          label: '审查结果（通过，但需按审查意见整改，否决）',
          field: row => '通过'
        },
        {
          name: 'memo',
          label: '备注',
          field: 'memo'
        }
      ],
      errorProtein: false,
      errorMessageProtein: '',
      cls: false,
      releaseFileModel: {
        type: 'source',
        system: [],
        questionNumber: ''
      },
      filterSystem: [],
      desc: '',
      systemOptions: [],
      releaseNodeSystemOptions: [],
      deepCloneTemplateSystem: [],
      componentReleaseType: '' //是否为组件/骨架/镜像投产窗口名称
    };
  },
  validations: {
    groupAbbrModel: {
      required,
      examine(val) {
        if (!val) {
          return true;
        }
        const reg = /^\w*$/;
        return reg.test(val);
      }
    },
    newReleaseNoteModel: {
      release_node_name: {
        required
      },
      release_note_name: {
        required
      },
      namespace: {
        myRequired(val) {
          if (this.newReleaseNoteModel.image_deliver_type === '1') {
            return required(val);
          } else {
            return true;
          }
        }
      },
      leaseholder: {
        myRequired(val) {
          if (this.newReleaseNoteModel.image_deliver_type === '1') {
            return required(val);
          } else {
            return true;
          }
        }
      },
      date: {
        required
      },
      vContainer: {},
      vGroup: {},
      vDate: {},
      Vtime: {},
      vEnd: {},
      type: {
        required
      },
      applications: {},
      plan_time: {
        myRequired(val) {
          if (this.newReleaseNoteModel.image_deliver_type === '1') {
            return required(val);
          } else {
            return true;
          }
        }
      },
      system: {
        required
      }
    },
    newChangeModel: {
      date: {
        required
      },
      vContainer: {
        myRequired(val) {
          this.cls = !required(val);
          return required(val);
        },
        required,
        separator(val) {
          if (!val) {
            return true;
          }
          let reg = new RegExp(/^[^_\s]*$/);
          const flag = reg.test(val);
          this.cls = !flag;
          return flag;
        }
      },
      vGroup: {
        myRequired(val) {
          this.cls = !required(val);
          return required(val);
        },
        required,
        separator(val) {
          if (!val) {
            return true;
          }
          let reg = new RegExp(/^[^_\s]*$/);
          const flag = reg.test(val);
          this.cls = !flag;
          return flag;
        }
      },
      vDate: {},
      Vtime: {},
      vEnd: {},
      prod_spdb_no: {
        required,
        onlyNumOrWord(value) {
          let re = new RegExp(/^[a-zA-Z0-9]*$/);
          return re.test(value);
        }
      },
      type: {
        required
      },
      excel_template_url: {
        required
      },
      applications: {},
      plan_time: {
        required
      },
      system: {
        required
      }
    },
    releaseFileModel: {
      system: {
        require(val) {
          if (this.releaseFileModel.type !== 'sourceCodeScanInfo') {
            return val.length > 0;
          }
          return true;
        }
      },
      questionNumber: {
        required(val) {
          if (this.examFileType) {
            return val.trim();
          }
          return true;
        },
        examine(val) {
          if (this.examFileType) {
            const reg = /^\d{11}$/;
            return reg.test(val);
          }
          return true;
        }
      }
    },
    sourceDetail: {
      spdb_managers: { required },
      spdb_master: { required }
    }
  },
  watch: {
    id() {
      this.changeTab();
    },
    releaseNodeDetail(val) {
      // 是否为组件/骨架/镜像投产窗口
      switch (val.type) {
        case '4':
          this.componentReleaseType = '组件';
          break;
        case '5':
          this.componentReleaseType = '骨架';
          break;
        case '6':
          this.componentReleaseType = '镜像';
          break;
        default:
          this.componentReleaseType = '';
      }
      this.changeTab();
      this.desc = val.type && val.type === '3' ? '试运行' : '投产';
    },
    'newChangeModel.type': {
      handler(val) {
        this.searchTemplate();
        this.newChangeModel.excel_template_url = '';
      }
    },
    'newChangeModel.image_deliver_type': {
      handler(val) {
        this.newChangeModel.system = '';
        this.newChangeModel.excel_template_url = {};
      }
    },
    'newReleaseNoteModel.image_deliver_type': {
      handler(val) {
        this.newReleaseNoteModel.leaseholder = '';
        this.newReleaseNoteModel.release_note_name = '';
        this.newReleaseNoteModel.plan_time = '';
        this.date = '';
        if (val && val === '1') {
          this.newReleaseNoteModel.note_batch = '';
        } else {
          this.newReleaseNoteModel.note_batch = '1';
        }
      }
    },
    'newReleaseNoteModel.note_batch': {
      handler(val) {
        this.newReleaseNoteModel.release_note_name =
          this.cutGourpName(this.group) +
          '_手动_' +
          this.batchOptions[val - 1].label;
      }
    },
    'newReleaseNoteModel.namespace': {
      handler(val) {
        if (val) {
          this._getleaseHolder(this.newReleaseNoteModel.type, val);
        }
      }
    },
    'newReleaseNoteModel.type': {
      handler(val) {
        this.searchReleaseNoteTemplate();
        if (val && this.newReleaseNoteModel.image_deliver_type === '1') {
          let level = '';
          this.newReleaseNoteModel.leaseholder = '';
          if (val === 'gray') {
            this.newReleaseNoteModel.namespace = '2';
            this.leaseholderOptions = leaseholderOptions.gray2;
            if (
              this.newReleaseNoteModel.Vtime >= 1300 &&
              this.newReleaseNoteModel.Vtime < 1600
            ) {
              level = '一批次';
            } else if (
              this.newReleaseNoteModel.Vtime >= 1600 &&
              this.newReleaseNoteModel.Vtime < 1800
            ) {
              level = '二批次';
            } else if (this.newReleaseNoteModel.Vtime >= 1800) {
              level = '三批次';
            }
          } else {
            this.leaseholderOptions =
              leaseholderOptions['proc' + this.newReleaseNoteModel.namespace];
            if (
              this.newReleaseNoteModel.Vtime >= 2000 &&
              this.newReleaseNoteModel.Vtime < 2200
            ) {
              level = '一批次';
            } else if (this.newReleaseNoteModel.Vtime >= 2200) {
              level = '二批次';
            } else if (
              this.newReleaseNoteModel.Vtime >= 0 &&
              this.newReleaseNoteModel.Vtime < 700
            ) {
              level = '三批次';
            }
          }

          this.newReleaseNoteModel.release_note_name =
            this.cutGourpName(this.group) +
            '_自动化_' +
            level +
            '【' +
            this.newReleaseNoteModel.vContainer +
            '_' +
            this.newReleaseNoteModel.vGroup +
            '_' +
            this.newReleaseNoteModel.vDate +
            '_' +
            this.newReleaseNoteModel.Vtime +
            '】';
        }
      }
    },
    'newReleaseNoteModel.system': {
      handler(val) {
        if (val && val.name_cn.includes('批量')) {
          this.newReleaseNoteModel.leaseholder = '';
          this.newReleaseNoteModel.namespace = '1';
          this.namespaceDisableFlag = true;
          this.setLeaseholder(leaseholderOptions['batch']);
          this.$nextTick(() => {
            this.leaseholderOptions = leaseholderOptions['batch'];
          });
        } else if (val && val.name_cn.includes('柜面')) {
          this.newReleaseNoteModel.leaseholder = '';
          this.newReleaseNoteModel.namespace = '1';
          this.namespaceDisableFlag = true;
          this.setLeaseholder(leaseholderOptions['otc']);
          this.$nextTick(() => {
            this.leaseholderOptions = leaseholderOptions['otc'];
          });
        } else {
          this.namespaceDisableFlag = false;
          this._getleaseHolder(
            this.newReleaseNoteModel.type,
            this.newReleaseNoteModel.namespace
          );
        }
      }
    },
    createUpdate(val) {
      if (val === false) {
        this.newChangeModel = newChangeModel();
        this.temp_name = '';
      }
    },
    /* 用于匹配角色被更改的科技负责人下拉选项 */
    spdbManagerOptions(val) {
      this.spdbMaster = val;
    }
  },
  computed: {
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager',
      userList: 'isLoginUserList'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('userForm', ['groups']),
    ...mapState('releaseForm', [
      'assetsVersion',
      'applyList',
      'version',
      'releaseDetail',
      'groupAbbr',
      'templateSystem',
      'excelTemplate',
      'releaseSystemList',
      'isConfigSum',
      'thirdLevelGroups',
      'releaseFiles',
      'allGroupAbbrList',
      'sourceReviewDetail'
    ]),
    prodVersion() {
      if (this.newChangeModel.prod_spdb_no && this.newChangeModel.date) {
        return (
          this.newChangeModel.date.split('/').join('') +
          '_' +
          this.newChangeModel.prod_spdb_no
        );
      } else {
        return '';
      }
    },
    group() {
      return getGroupFullName(this.groups, this.releaseDetail.owner_groupId);
    },
    examFileType() {
      return this.releaseFileModel.type === 'source';
    },
    spdbManagerOptions() {
      return this.userListCopy.filter(item => {
        return (
          item.role.some(role => {
            return role.name === '行内项目负责人';
          }) || item.user_name_cn === this.sourceDetail.user_name_cn
        );
      });
    }
  },
  methods: {
    ...mapActions('userForm', {
      fetchGroup: 'fetchGroup'
    }),
    ...mapActions('user', ['fetch']),
    ...mapActions('releaseForm', {
      createManualNote: 'createManualNote',
      createAChanges: 'createAChanges',
      createNote: 'createNote',
      getChangesRecord: 'getChangesRecord',
      queryApply: 'queryApply',
      queryVersion: 'queryVersion',
      queryReleaseNodeDetail: 'queryReleaseNodeDetail',
      queryGroupAbbr: 'queryGroupAbbr',
      updateGroupAbbr: 'updateGroupAbbr',
      queryAllGroupAbbr: 'queryAllGroupAbbr',
      addSystemTestFile: 'addSystemTestFile',
      addSourceReview: 'addSourceReview',
      queryTemplateSystem: 'queryTemplateSystem',
      queryExcelTemplate: 'queryExcelTemplate',
      queryReleaseSystem: 'queryReleaseSystem',
      queryConfigSum: 'queryConfigSum',
      addRedLineScanReport: 'addRedLineScanReport',
      queryReleaseNote: 'queryReleaseNote',
      queryByReleaseNodeName: 'queryByReleaseNodeName',
      queryThreeLevelGroups: 'queryThreeLevelGroups',
      querySourceReviewDetail: 'querySourceReviewDetail'
    }),
    ...mapMutations('releaseForm', {
      saveRole: 'saveRole',
      saveCompareTime: 'saveCompareTime'
    }),
    // 租户只有一个时默认回显
    setLeaseholder(value) {
      if (value.length === 1) {
        this.$nextTick(() => {
          this.newReleaseNoteModel.leaseholder = value[0].value;
        });
      }
    },
    async clickRealeseFile() {
      this.clearReleaseFile();
      this.realeseFileDialog = true;
      await this.queryReleaseSystem({
        release_node_name: this.$route.params.id
      });
      this.queryByReleaseNodeNameLoading = true;
      await this.queryByReleaseNodeName({
        release_node_name: this.$route.params.id
      });
      this.queryByReleaseNodeNameLoading = false;
      this.selectedBusinessRqrmnt = [];
      this.selectedTechRqrmnt = [];
    },
    _getleaseHolder(type, namespace) {
      this.newReleaseNoteModel.leaseholder = '';
      this.leaseholderOptions = leaseholderOptions[type + namespace];
      this.setLeaseholder(leaseholderOptions[type + namespace]);
    },
    //编辑时修改tab
    changeTab() {
      if (this.releaseNodeDetail.type && this.releaseNodeDetail.type === '3') {
        this.dimensions = [
          { name: '试运行任务列表', path: `/release/list/${this.id}/joblist` },
          {
            name: '试运行应用列表',
            path: `/release/list/${this.id}/testRunAppList`
          }
        ];
      } else if (this.releaseNodeDetail.type && this.componentReleaseType) {
        let componentMenu = '';
        switch (this.componentReleaseType) {
          case '组件':
            componentMenu = 'componentlist';
            break;
          case '骨架':
            componentMenu = 'archetypeList';
            break;
          case '镜像':
            componentMenu = 'imageList';
            break;
        }
        this.dimensions = [
          { name: '投产任务列表', path: `/release/list/${this.id}/joblist` },
          {
            name: `投产${this.componentReleaseType}列表`,
            path: `/release/list/${this.id}/${componentMenu}`
          }
        ];
      } else if (
        this.releaseNodeDetail.type &&
        this.releaseNodeDetail.type === '1'
      ) {
        this.dimensions = [
          { name: '投产任务列表', path: `/release/list/${this.id}/joblist` },
          {
            name: '投产应用列表',
            path: `/release/list/${this.id}/applist`
          },
          { name: '变更列表', path: `/release/list/${this.id}/createUpdate` },
          {
            name: '发布说明列表',
            path: `/release/list/${this.id}/autoReleaseNote`
          },
          {
            name: '投产文档汇总',
            path: `/release/list/${this.id}/modifiedFiles`
          },
          {
            name: '配置文件变更',
            path: `/release/list/${this.id}/updateFileConfig`
          }
        ];
      } else {
        this.dimensions = [
          { name: '投产任务列表', path: `/release/list/${this.id}/joblist` },
          { name: '投产应用列表', path: `/release/list/${this.id}/applist` },
          { name: '变更列表', path: `/release/list/${this.id}/createUpdate` },
          {
            name: '发布说明列表',
            path: `/release/list/${this.id}/autoReleaseNote`
          },
          {
            name: '投产文档汇总',
            path: `/release/list/${this.id}/modifiedFiles`
          }
        ];
      }
    },
    /* 显示修改弹框 */
    async handleupdateRelease() {
      this.newChangeModel.release_node_name = this.id;
      /* 查询小组标识 */
      this.queryGroupAbbr({ group_id: this.releaseDetail.owner_groupId }).then(
        res => {
          this.group_abbr = this.groupAbbr.group_abbr;
        }
      );
      /* 查询模板 */
      this.searchTemplate();
      this.getTreeData();
      this.updateReleaseModalOpened = true;
      this.isReleaseNote = true;
      this.newChangeModel = newChangeModel();
      this.newChangeModel.release_node_name = this.id;
      /* 查询涉及配置文件变更 */
      await this.queryConfigSum({
        release_node_name: this.id
      });
      if (this.isConfigSum === '1') {
        this.simple[0].children[1].disabled = true;
        this.simple[0].children[1].disableLabel = '有数据未生成，不可选择';
      } else if (this.isConfigSum === '2') {
        this.simple[0].children.splice(1, 1);
      }
    },
    /* 修改完成更改字段值 */
    async updateReleaseNode(oldRelease) {
      this.updateReleaseModalOpened = false;
      if (oldRelease) {
        this.$router.push(`/release/list/${oldRelease}/joblist`);
      }
      await this.queryReleaseNodeDetail({
        release_node_name: this.$route.params.id
      });
      this.releaseNodeDetail = this.releaseDetail;
      this.id = this.$route.params.id;
      this.releaseNodeDetail.groupFullName = getGroupFullName(
        this.groups,
        this.releaseNodeDetail.owner_groupId
      );
    },
    findName(val) {
      if (this.users) {
        let name = this.users.find(user => user.user_name_en === val);
        return name ? name.user_name_cn : val;
      }
    },
    findManager(val) {
      if (this.users) {
        let name = this.users.find(
          user => user.user_name_en === val || user.user_name_cn === val
        );
        return name ? name.id : val;
      }
    },
    async handleReleaseTip() {
      this.$v.newReleaseNoteModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('newReleaseNoteModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.newReleaseNoteModel.$invalid) {
        return;
      }
      this.handleAutoReleaseNote();
    },
    handleAllTip() {
      this.$v.newChangeModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('newChangeModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.newChangeModel.$invalid) {
        return;
      }
      this.handleCreateUpdate();
    },
    // 新建发布说明
    async handleAutoReleaseNote() {
      const {
        vContainer,
        vGroup,
        vDate,
        Vtime,
        vEnd
      } = this.newReleaseNoteModel;

      const payload = {
        ...this.newReleaseNoteModel,
        release_note_name: this.newReleaseNoteModel.release_note_name + '.txt',
        owner_groupId: this.releaseNodeDetail.owner_groupId,
        owner_system: this.newReleaseNoteModel.system.id,
        systemType: this.newReleaseNoteModel.system.name.includes('批量')
          ? '1'
          : ''
      };

      if (this.newReleaseNoteModel.image_deliver_type === '1') {
        if (this.newReleaseNoteModel.type === 'gray') {
          if (Vtime >= 0 && Vtime < 1300) {
            errorNotify('选择的预期发布时间为非投产时间，请重新选择');
            return;
          }
        } else {
          if (Vtime >= 700 && Vtime < 2000) {
            errorNotify('选择的预期发布时间为非投产时间，请重新选择');
            return;
          }
        }
        let version = vEnd
          ? vContainer + '_' + vGroup + '_' + vDate + '_' + Vtime + '_' + vEnd
          : vContainer + '_' + vGroup + '_' + vDate + '_' + Vtime;
        payload.version = version;
      }

      if (this.newReleaseNoteModel.image_deliver_type === '1') {
        await this.createNote(payload);
      } else {
        await this.createManualNote(payload);
      }
      successNotify('新建成功');
      this.autoReleaseNoteDialogModel = false;
      this.newReleaseNoteModel = newReleaseNoteModel();
      this.queryReleaseNote({ release_node_name: this.id });
      this.$router.push(`/release/list/${this.id}/autoReleaseNote`);
    },

    // 新建变更
    async handleCreateUpdate() {
      let app = [];
      this.newChangeModel.applications.forEach(item => {
        this.simple[0].children[0].children.forEach(batch => {
          batch.children.forEach(child => {
            if (
              child.application_id === item &&
              child.fdev_config_changed === true &&
              child.fdev_config_confirm !== '1'
            ) {
              app.push(child);
            }
          });
        });
      });
      if (app.length > 0) {
        let list = app.map(item => item.app_name_en);
        let name = list.join(',');
        errorNotify(`请应用${name}的应用负责人完成配置文件更新审核`);
        return;
      }
      const { vContainer, vGroup, vDate, Vtime, vEnd } = this.newChangeModel;
      let version = vEnd
        ? vContainer + '_' + vGroup + '_' + vDate + '_' + Vtime + '_' + vEnd
        : vContainer + '_' + vGroup + '_' + vDate + '_' + Vtime;
      const payload = {
        ...this.newChangeModel,
        owner_groupId: this.releaseNodeDetail.owner_groupId,
        excel_template_url: this.newChangeModel.excel_template_url.url,
        excel_template_name: this.newChangeModel.excel_template_url.filename,
        owner_system: this.newChangeModel.system.id,
        version: version
      };
      //勾选了配置文件变更应用的话，上传字段里带上isConfigSum: '0'
      if (
        Array.isArray(this.newChangeModel.applications) &&
        this.newChangeModel.applications.includes('配置文件变更应用')
      ) {
        payload.applications = [];
        Reflect.set(payload, 'isConfigSum', '0');
      }
      await this.createAChanges(payload);
      successNotify('新建成功');
      this.createUpdate = false;
      this.newChangeModel = newChangeModel();
      this.getChangesRecord({ release_node_name: this.id });
      this.$router.push(`/release/list/${this.id}/createUpdate`);
    },
    tickNode(target) {
      if (target.length === 0) {
        this.simple[0].children[0].disabled = false;
        this.isConfigSum !== '2' &&
          (this.simple[0].children[1].disabled = this.isConfigSum === '1');
      } else if (target.includes('配置文件变更应用')) {
        this.simple[0].children[0].disabled = true;
      } else {
        this.isConfigSum !== '2' &&
          (this.simple[0].children[1].disabled = true);
      }
    },
    cutGourpName(val) {
      const temp = val.split('-');
      return temp[temp.length - 1];
    },
    async getTreeData() {
      if (this.releaseNodeDetail.type <= 3) {
        // 窗口类型为微服务，原生，试运行时，请求应用接口
        await this.queryApply(this.$route.params.id);
      }

      const arr = batchArr().map(item => {
        return {
          application_id: item.batch,
          label: item.type,
          disabled: true,
          ...item
        };
      });

      const data = this.applyList.reduce((sum, i) => {
        const data = {
          ...i,
          label: i.app_name_en
        };
        const batch = i.batch_id ? Number(i.batch_id) : 4;
        sum[batch - 1].children.push(data);
        sum[batch - 1].disabled = false;
        return sum;
      }, arr);
      this.simple[0].children[0].children = data;
    },
    // 查询变更版本
    async findVersion(date, time) {
      if (!date || !time) {
        return;
      }
      const params = {
        release_node_name: this.id,
        date: this.newChangeModel.date,
        group_id: this.releaseDetail.owner_groupId,
        plan_time: this.newChangeModel.plan_time
      };
      await this.queryVersion(params);
      this.$set(this.newChangeModel, 'vContainer', this.version.split('_')[0]);
      this.$set(this.newChangeModel, 'vGroup', this.version.split('_')[1]);
      this.$set(this.newChangeModel, 'vDate', this.version.split('_')[2]);
      this.$set(this.newChangeModel, 'Vtime', this.version.split('_')[3]);
    },
    async findReleaseVersion(date, time) {
      if (!date || !time) {
        return;
      }
      const params = {
        release_node_name: this.id,
        date: this.newReleaseNoteModel.date,
        group_id: this.releaseDetail.owner_groupId,
        plan_time: this.newReleaseNoteModel.plan_time
      };
      await this.queryVersion(params);
      this.$set(
        this.newReleaseNoteModel,
        'vContainer',
        this.version.split('_')[0]
      );
      this.$set(this.newReleaseNoteModel, 'vGroup', this.version.split('_')[1]);
      this.$set(this.newReleaseNoteModel, 'vDate', this.version.split('_')[2]);
      this.$set(this.newReleaseNoteModel, 'Vtime', this.version.split('_')[3]);
      if (this.newReleaseNoteModel.image_deliver_type === '1') {
        let level = '';
        if (this.newReleaseNoteModel.type === 'gray') {
          if (
            this.newReleaseNoteModel.Vtime >= 1300 &&
            this.newReleaseNoteModel.Vtime < 1600
          ) {
            level = '一批次';
          } else if (
            this.newReleaseNoteModel.Vtime >= 1600 &&
            this.newReleaseNoteModel.Vtime < 1800
          ) {
            level = '二批次';
          } else if (this.newReleaseNoteModel.Vtime >= 1800) {
            level = '三批次';
          }
        } else {
          if (
            this.newReleaseNoteModel.Vtime >= 2000 &&
            this.newReleaseNoteModel.Vtime < 2200
          ) {
            level = '一批次';
          } else if (this.newReleaseNoteModel.Vtime >= 2200) {
            level = '二批次';
          } else if (
            this.newReleaseNoteModel.Vtime >= 0 &&
            this.newReleaseNoteModel.Vtime < 700
          ) {
            level = '三批次';
          }
        }

        this.newReleaseNoteModel.release_note_name =
          this.cutGourpName(this.group) +
          '_自动化_' +
          level +
          '【' +
          this.newReleaseNoteModel.vContainer +
          '_' +
          this.newReleaseNoteModel.vGroup +
          '_' +
          this.newReleaseNoteModel.vDate +
          '_' +
          this.newReleaseNoteModel.Vtime +
          '】';
      }
    },
    /* 打开新建变更 */
    async openCreateChanges() {
      this.newChangeModel.release_node_name = this.id;
      /* 查询小组标识 */
      this.queryGroupAbbr({ group_id: this.releaseDetail.owner_groupId }).then(
        res => {
          this.group_abbr = this.groupAbbr.group_abbr;
        }
      );
      /* 查询模板 */
      this.searchTemplate();
      this.getTreeData();
      this.createUpdate = true;
      this.isReleaseNote = false;
      this.newChangeModel = newChangeModel();
      this.newChangeModel.release_node_name = this.id;
      /* 查询涉及配置文件变更 */
      await this.queryConfigSum({
        release_node_name: this.id
      });
      if (this.isConfigSum === '1') {
        this.simple[0].children[1].disabled = true;
        this.simple[0].children[1].disableLabel = '有数据未生成，不可选择';
      } else if (this.isConfigSum === '2') {
        this.simple[0].children.splice(1, 1);
      }
    },
    // 打开新建发布说明弹窗
    async openAutoReleaseNoteDialog() {
      this.newReleaseNoteModel.release_node_name = this.id;
      /* 查询小组标识 */
      this.queryGroupAbbr({ group_id: this.releaseDetail.owner_groupId }).then(
        res => {
          this.group_abbr = this.groupAbbr.group_abbr;
        }
      );
      /* 查询模板 */
      this.searchReleaseNoteTemplate();
      this.getTreeData();
      this.autoReleaseNoteDialogModel = true;
      this.newReleaseNoteModel = newReleaseNoteModel();
      this.newReleaseNoteModel.release_node_name = this.id;
      /* 查询涉及配置文件变更 */
      await this.queryConfigSum({
        release_node_name: this.id
      });
      if (this.isConfigSum === '1') {
        this.simple[0].children[1].disabled = true;
        this.simple[0].children[1].disableLabel = '有数据未生成，不可选择';
      } else if (this.isConfigSum === '2') {
        this.simple[0].children.splice(1, 1);
      }
    },
    async searchTemplate() {
      this.newChangeModel.system = '';
      await this.queryTemplateSystem({
        template_type: this.newChangeModel.type,
        owner_group: this.releaseNodeDetail.owner_groupId
      });
      this.systemOptions = this.templateSystem;
    },
    async searchReleaseNoteTemplate() {
      this.newReleaseNoteModel.system = '';
      await this.queryTemplateSystem({
        template_type: this.newReleaseNoteModel.type,
        owner_group: this.releaseNodeDetail.owner_groupId
      });
      this.deepCloneTemplateSystem = deepClone(this.templateSystem);
      const batch = this.deepCloneTemplateSystem.find(v =>
        v.name.includes('个人手机银行系统')
      );
      if (batch && this.newReleaseNoteModel.type === 'proc') {
        this.deepCloneTemplateSystem = this.deepCloneTemplateSystem.concat([
          {
            id: batch.id,
            name: '网银系统群_批量',
            name_cn: '网银系统群_批量'
          },
          {
            id: batch.id,
            name: '柜面及消息系统',
            name_cn: '柜面及消息系统'
          }
        ]);
      }
      this.releaseNodeSystemOptions = this.deepCloneTemplateSystem;
    },
    /* 可选日期控制：当前日期-投产窗口日期+3 */
    optionsFn(date) {
      const today = moment(new Date()).format('YYYY/MM/DD');
      const releaseDate = moment(this.releaseNodeDetail.release_date)
        .add(3, 'days')
        .format('YYYY/MM/DD');
      return today <= date && date <= releaseDate;
    },
    // 判断是否是小组投产管理员、当前投产负责人、科技负责人
    judgeRole() {
      this.saveRole(this.releaseNodeDetail.can_operation);
      this.$q.sessionStorage.set(
        'releaseRole',
        this.releaseNodeDetail.can_operation
      );
    },
    async handleGroupAbbrModelOpen() {
      await this.queryAllGroupAbbr({});
      this.groupAbbrModelOpened = true;
      this.groupAbbrModel = this.group_abbr;
    },
    /* 提交小组标识修改 */
    async handleGroupAbbrModel() {
      await this.updateGroupAbbr({
        group_id: this.releaseDetail.owner_groupId,
        group_abbr: this.groupAbbrModel
      });
      if (this.isReleaseNote) {
        this.findReleaseVersion(
          this.newChangeModel.date,
          this.newChangeModel.plan_time
        );
      } else {
        this.findVersion(
          this.newChangeModel.date,
          this.newChangeModel.plan_time
        );
      }
      successNotify('成功');
      this.group_abbr = this.groupAbbrModel;
      this.groupAbbrModel = '';
      this.groupAbbrModelOpened = false;
    },
    templateFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase().trim();
        this.templateOfChange = this.excelTemplate.filter(v =>
          v.filename.toLowerCase().includes(needle)
        );
      });
    },
    filterSpdbMaster(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.spdbMaster = this.spdbManagerOptions.filter(
          v =>
            v.user_name_cn.indexOf(needle) > -1 ||
            v.user_name_en.toLowerCase().indexOf(needle) > -1
        );
        if (this.spdbMaster.length === 0) {
          this.spdbMaster = this.spdbManagerOptions;
        }
      });
    },
    managerFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.managerOptions = this.userListCopy.filter(
          v =>
            v.user_name_cn.indexOf(needle) > -1 ||
            v.user_name_en.toLowerCase().indexOf(needle) > -1
        );
        if (this.managerOptions.length === 0) {
          this.managerOptions = this.userListCopy;
        }
      });
    },
    groupAbbrFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.groupAbbrOptions = this.allGroupAbbrList.filter(
          v =>
            v.group_abbr.indexOf(needle) > -1 ||
            v.group_abbr.toLowerCase().indexOf(needle) > -1
        );
        if (this.groupAbbrOptions.length === 0) {
          this.groupAbbrOptions = this.allGroupAbbrList;
        }
      });
    },
    saveValue(value) {
      this.currentEditValue = value;
    },
    // 申请人为空时校验
    inputValidation(val) {
      if (!val || val.name_en.length < 1) {
        this.errorProtein = true;
        this.errorMessageProtein = '请输入申请人名称';
        return;
      }
      this.sourceDetail.list = this.sourceDetail.list.map(item => {
        if (item.name.name_en === this.currentEditValue) {
          return {
            ...item,
            name: {
              name_cn: val ? val.name_cn : '',
              name_en: val ? val.name_en : ''
            }
          };
        }
        return {
          ...item,
          name: {
            name_cn: item.name.name_cn,
            name_en: item.name.name_en
          }
        };
      });
      return this.hideInput();
    },
    hideInput() {
      this.errorProtein = false;
      this.errorMessageProtein = '';
      return true;
    },
    // 生成源代码登记表文档
    async handleCreateFile() {
      this.$v.sourceDetail.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('sourceDetail') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.sourceDetail.$invalid) {
        return;
      }
      let applicationName = Array.from(
        new Set(
          this.sourceDetail.list.map(item => {
            return item.name.name_en;
          })
        )
      );

      if (
        this.sourceDetail.spdb_managers.user_name_en ===
        this.sourceDetail.spdb_master.user_name_en
      ) {
        this.handleSameNameDialog(
          '审查人与代码审查负责人同名，确认继续生成投产材料吗？'
        );
      } else if (
        applicationName.indexOf(this.sourceDetail.spdb_managers.user_name_en) >
          -1 ||
        applicationName.indexOf(this.sourceDetail.spdb_master.user_name_en) > -1
      ) {
        let startItem = this.sourceDetail.list.find(
          item =>
            item.name.name_en ===
              this.sourceDetail.spdb_managers.user_name_en ||
            item.name.name_en === this.sourceDetail.spdb_master.user_name_en
        );
        let message = `审查人与申请人同名或代码审查负责人与申请人同名，相同项第${
          startItem.index
        }项，名称${startItem.name.name_cn}，确认继续生成投产材料吗？`;
        this.handleSameNameDialog(message);
      } else {
        this.handleAddSourceReview();
      }
    },
    // 任务名相同时给出提示
    handleSameNameDialog(message) {
      this.$q
        .dialog({
          title: '风险提示',
          message,
          cancel: '不生成，去修改',
          ok: '确认生成'
        })
        .onOk(() => {
          this.handleAddSourceReview();
        });
    },
    // 源代码审查登记表确认
    async handleAddSourceReview() {
      const release_node_name = this.$route.params.id;
      const spdb_managers = {
        user_name_cn: this.sourceDetail.spdb_managers.user_name_cn,
        user_name_en: this.sourceDetail.spdb_managers.user_name_en
      };
      const spdb_master = {
        user_name_cn: this.sourceDetail.spdb_master.user_name_cn,
        user_name_en: this.sourceDetail.spdb_master.user_name_en
      };
      const { questionNumber } = this.releaseFileModel;
      let list = this.sourceDetail.list.map(item => {
        return {
          path: item.path,
          question_no: item.question_no,
          name: {
            name_cn: item.name.name_cn,
            name_en: item.name.name_en
          }
        };
      });
      const params = {
        release_node_name,
        ...this.sourceDetail,
        list,
        question_no: `SPDB${questionNumber}`,
        spdb_managers,
        spdb_master
      };
      await this.addSourceReview(params);
      this.sourceOpened = false;
      this.realeseFileDialog = false;
      successNotify('操作成功!');
      await this.queryByReleaseNodeName({
        release_node_name: this.releaseNodeDetail.release_node_name
      });
    },
    clearReleaseFile() {
      this.releaseFileModel = {
        type: 'source',
        system: [],
        questionNumber: ''
      };
    },
    // 生成投产材料窗口
    async handleCreateRelease() {
      this.$v.releaseFileModel.$touch();
      let Keys = [];
      if (this.examFileType) {
        Keys = Object.keys(this.$refs).filter(key => {
          return key.indexOf('releaseFileModel') > -1;
        });
      }
      if (this.releaseFileModel.type == 'systemTest') {
        Keys = Object.keys(this.$refs).filter(key => {
          return key.indexOf('releaseFileModel.system') > -1;
        });
      }
      Promise.all(
        Keys.map(ele => this.$refs[ele].validate() || Promise.reject(ele))
      ).then(
        async res => {
          const { questionNumber, type } = this.releaseFileModel;
          const release_node_name = this.$route.params.id;
          const ids = this.releaseFileModel.system;
          if (type === 'source') {
            let question_no = `SPDB${questionNumber}`;
            let rqrmnt_ids = this.selectedBusinessRqrmnt
              .concat(this.selectedTechRqrmnt)
              .map(item => {
                return item.rqrmnt_id;
              });
            await this.querySourceReviewDetail({
              release_node_name,
              question_no,
              ids,
              rqrmnt_ids
            });
            this.sourceDetail = this.sourceReviewDetail;
            this.sourceDetail.spdb_managers =
              Object.keys(this.sourceDetail.spdb_managers).length === 0
                ? null
                : this.sourceDetail.spdb_managers;
            this.sourceDetail.spdb_master =
              Object.keys(this.sourceDetail.spdb_master).length === 0
                ? null
                : this.sourceDetail.spdb_master;
            this.sourceDetail.list = this.sourceDetail.list.map(
              (item, index) => {
                return {
                  ...item,
                  index: index + 1
                };
              }
            );
            await this.fetch();
            this.userListCopy = this.userList.map(item => {
              return {
                ...item,
                name_cn: item.user_name_cn,
                name_en: item.user_name_en
              };
            });
            this.sourceOpened = true;
            return;
          } else if (type === 'systemTest') {
            await this.addSystemTestFile({ release_node_name, ids });
          } else if (type === 'sourceCodeScanInfo') {
            await this.addRedLineScanReport({
              release_node_name: this.releaseNodeDetail.release_node_name,
              group_id: this.releaseNodeDetail.owner_groupId
            });
          }
          await this.queryByReleaseNodeName({
            release_node_name: this.releaseNodeDetail.release_node_name
          });
          successNotify('操作成功!');
          this.realeseFileDialog = false;
        },
        rej => {
          this.$refs[rej].focus();
        }
      );
    },
    systemInputFilter(val, update) {
      update(() => {
        this.filterSystem = this.releaseSystemList.filter(
          tag => tag.name.indexOf(val) > -1
        );
      });
    },
    goReleaseList() {
      this.$router.push('/release/list');
    },
    systemFilter(val, update) {
      const needle = val.toLowerCase().trim();
      update(() => {
        this.systemOptions = this.templateSystem.filter(item => {
          return (
            item.name_cn.includes(needle) ||
            item.name_en.toLowerCase().includes(needle)
          );
        });
      });
    },
    systemReleaseFilter(val, update) {
      const needle = val.toLowerCase().trim();
      update(() => {
        this.releaseNodeSystemOptions = this.deepCloneTemplateSystem.filter(
          item => {
            return (
              item.name.includes(needle) ||
              item.name_cn.toLowerCase().includes(needle)
            );
          }
        );
      });
    },
    async queryExcel() {
      this.newChangeModel.excel_template_url = '';
      if (this.newChangeModel.system) {
        await this.queryExcelTemplate({
          sysname_cn: this.newChangeModel.system.name_cn,
          template_type: this.newChangeModel.type
        });
        this.templateOfChange = this.excelTemplate;
      }
    },
    async queryReleaseNoteExcel() {
      if (this.newReleaseNoteModel.system) {
        await this.queryExcelTemplate({
          sysname_cn: this.newReleaseNoteModel.system.name_cn,
          template_type: this.newReleaseNoteModel.type
        });
        this.templateOfChange = this.excelTemplate;
      }
    },
    // 获取当前用户的第三层级组及其子组
    getThirdLevelGroups() {
      const thirdLevelGroups = this.thirdLevelGroups;
      if (
        !thirdLevelGroups ||
        thirdLevelGroups.user_id !== this.currentUser.id
      ) {
        this.queryThreeLevelGroups();
      }
    }
  },
  async created() {
    await this.fetchGroup();
    await this.getThirdLevelGroups();
    this.getTreeData();
    this.leaseholderOptions = leaseholderOptions.gray2;
    let params = {
      release_node_name: this.$route.params.id
    };
    await this.queryReleaseNodeDetail(params);
    this.releaseNodeDetail = this.releaseDetail;
    this.getTreeData();
    this.id = this.$route.params.id;
    this.roleData = JSON.parse(JSON.stringify(this.releaseDetail));
    this.releaseNodeDetail.groupFullName = getGroupFullName(
      this.groups,
      this.releaseDetail.owner_groupId
    );
    this.compareTime = isValidReleaseDate(this.releaseNodeDetail.release_date);
    this.saveCompareTime(this.compareTime);
    this.judgeRole();
  }
};
</script>

<style lang="stylus" scoped>
.release-note-pre-publish-time >>> .el-input__inner
  width 300px
  height 36px
  margin-bottom 16px
  border 1px solid #bbb

.release-pre-publish-time >>> .el-input__inner
  width 384px
  height 36px
  margin-bottom 16px
  border 1px solid #bbb

/deep/ .el-input__icon
  height 80%

.release-note-pre-publish-time >>> .el-icon-circle-close
  position absolute
  right -130px

.release-pre-publish-time >>> .el-icon-circle-close
  position absolute
  right -210px
</style>
<style>
.el-time-panel {
  z-index: 9999 !important;
}
.line-link {
  text-align: center;
  display: inline-block;
  margin: auto;
  height: 40px;
}
.time-special-input .q-field__control,
.time-special-input .q-field__native {
  padding: 0;
}
.time-special-input .q-field__control:before,
.time-special-input .q-field__control:after {
  border-top: none;
  border-left: none;
  border-right: none;
}
.time-special-input .self-stretch {
  align-self: auto;
}
.el-time-spinner__item {
  height: 36px;
  line-height: 30px;
}
</style>
