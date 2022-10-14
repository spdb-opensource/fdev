<template>
  <el-container class="page-container">
    <el-scrollbar style="height:120vh">
      <el-aside>
        <div class="aside">
          <el-row>
            <el-col :span="24"><b>工单号</b></el-col>
          </el-row>
          <el-row>
            <el-col class="PlanworkNo" :span="24">{{ PlanworkNo }}</el-col>
          </el-row>
          <el-row class="m-tb-10">
            <el-col :span="24">
              <el-tag>案例汇总</el-tag>
            </el-col>
          </el-row>
          <el-row class="title">
            <div class="exePlan">执行计划</div>
            <div class="exePlan">
              <el-button
                type="primary"
                @click="dialogAdd = true"
                v-if="
                  stage !== '8' &&
                    !isHistory &&
                    !isWaste &&
                    (planPower == true || userRole == '50' || isAuthUser)
                "
                >新增计划</el-button
              >
            </div>

            <div>
              <el-button
                type="primary"
                v-if="
                  isWaste &&
                    (planPower == true ||
                      userRole == '50' ||
                      isSelf ||
                      isAuthUser)
                "
                @click="dialogAddMigrationOrder = true"
                >工单迁移</el-button
              >
            </div>
          </el-row>
          <el-row>
            <el-col>
              <ul>
                <li v-for="(item, index) in planList" :key="index">
                  <el-button
                    @click="
                      showPlanTestStatus(
                        item.planId,
                        item.planName,
                        item.planStartDate,
                        item.planEndDate,
                        item.deviceInfo,
                        index
                      )
                    "
                    :class="{ buttonActive: index === active }"
                    type="primary"
                    plain
                    >{{ item.planName }}
                  </el-button>
                </li>
              </ul>
            </el-col>
          </el-row>
        </div>
      </el-aside>
    </el-scrollbar>
    <el-main>
      <div class="main-header">
        <el-card>
          <div class="card-body">
            <el-row class="text-grey-1">
              <el-col :span="20"
                ><span class="mar-20">工单名称</span>{{ mainTaskName }}</el-col
              >
              <el-col :span="4">
                <el-tooltip
                  v-if="stage !== '11' && this.planList.length == 0"
                  content="该工单下暂无可发送的计划"
                  placement="top"
                >
                  <div>
                    <el-button
                      type="primary"
                      v-if="stage !== '11' && this.planList.length == 0"
                      icon="el-icon-message"
                      :disabled="disabledSendBtn || this.planList.length == 0"
                      @click="sendTestcaseMail"
                      >发送案例评审</el-button
                    >
                  </div>
                </el-tooltip>
                <el-button
                  type="primary"
                  v-if="stage !== '11' && this.planList.length !== 0"
                  icon="el-icon-message"
                  :disabled="disabledSendBtn || this.planList.length == 0"
                  @click="sendTestcaseMail"
                  >发送案例评审</el-button
                >
              </el-col>
            </el-row>
            <el-row class="text-grey-2">
              <el-col :span="24"
                ><span class="mar-35">测试人员</span>{{ testers }}</el-col
              >
            </el-row>
            <el-row class="text-grey-2">
              <el-col :span="24"
                ><span class="mar-35">计划名称</span
                >{{ planInfo.planName }}</el-col
              >
            </el-row>
            <el-row class="text-grey-2">
              <el-col :span="24"
                ><span class="mar-35">设备信息</span
                >{{ planInfo.deviceInfo }}</el-col
              >
            </el-row>
            <el-row class="text-grey-2">
              <el-col :span="8"
                ><span class="mar-35">开始时间</span
                >{{ planInfo.startTime }}</el-col
              >
              <el-col :span="8"
                ><span class="mar-35">结束时间</span
                >{{ planInfo.endTime }}</el-col
              >
            </el-row>
            <el-row>
              <el-col :span="24" class="state-container">
                <el-button-group>
                  <el-button
                    size="small"
                    class="executeCountSum"
                    @click="queryTestcaseSingle('')"
                    >总数
                    <el-badge
                      :value="planInfo.allCase"
                      class="item"
                      type="primary"
                    ></el-badge>
                  </el-button>
                  <el-button
                    size="small"
                    class="executeCount"
                    @click="queryTestcaseSingle('allExe')"
                    >执行<el-badge
                      :value="planInfo.exeNum"
                      class="item"
                      type="primary"
                    ></el-badge>
                  </el-button>
                  <el-button
                    size="small"
                    class="executeCount"
                    @click="queryTestcaseSingle('0')"
                    >未执行<el-badge
                      :value="planInfo.sumUnExe"
                      class="item"
                      type="primary"
                    ></el-badge>
                  </el-button>
                  <el-button
                    size="small"
                    class="executeCount"
                    @click="queryTestcaseSingle('1')"
                    >成功
                    <el-badge
                      :value="planInfo.successNum"
                      class="item"
                      type="success"
                    ></el-badge>
                  </el-button>
                  <el-button
                    size="small"
                    class="executeCount"
                    @click="queryTestcaseSingle('2')"
                    >阻塞<el-badge
                      :value="planInfo.zuaiNum"
                      class="item"
                      type="warning"
                    ></el-badge>
                  </el-button>
                  <el-button
                    size="small"
                    class="executeCount"
                    @click="queryTestcaseSingle('3')"
                    >失败
                    <el-badge
                      :value="planInfo.failedNum"
                      class="item"
                    ></el-badge>
                  </el-button>
                </el-button-group>
              </el-col>
            </el-row>
          </div>
          <div class="divider"></div>
          <el-row class="card-footer">
            <el-col
              :span="12"
              class="text-left pd-4"
              v-if="
                !isWaste &&
                  !isHistory &&
                  (planPower == true || userRole == '50' || isAuthUser)
              "
            >
              <el-link
                class="fontSize"
                icon="el-icon-edit"
                @click="updatePlan = true"
                >修改计划</el-link
              >
              <el-link
                class="fontSize mal-10"
                icon="el-icon-delete"
                @click="deletePlan"
                >删除计划</el-link
              >
            </el-col>

            <el-col
              :span="12"
              class="text-right"
              v-if="
                !isWaste &&
                  !isHistory &&
                  (planPower == true || userRole == '50' || isAuthUser)
              "
            >
              <el-link class="fontSize" icon="el-icon-document" @click="Reuse"
                >复用案例</el-link
              >
              <el-link
                class="fontSize mal-10"
                icon="el-icon-plus"
                @click="querySystem('add')"
                >新增案例</el-link
              >
              <el-button-group class="sort-style">
                <el-button
                  size="mini"
                  type="primary"
                  :disabled="total < 2 || !notShowCaseSort"
                  round
                  @click="startSort()"
                  >开始排序</el-button
                >
                <el-button
                  size="mini"
                  type="primary"
                  :disabled="notShowCaseSort"
                  round
                  @click="endSort()"
                  >结束排序</el-button
                >
              </el-button-group>
            </el-col>
          </el-row>
        </el-card>
      </div>

      <div v-show="notShowCaseSort">
        <!--  STATRT多条件查询  -->
        <div class="main-body">
          <el-form :inline="true" :model="formInline" class="demo-form-inline">
            <el-row>
              <el-col :span="3" :offset="1">
                <el-form-item>
                  <el-input
                    v-model="testcaseName"
                    placeholder="案例名称"
                    maxlength="90"
                    clearable
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="3">
                <el-form-item>
                  <el-input
                    v-model="mantisPlanIdTestcaseId"
                    placeholder="案例关系编号"
                    maxlength="90"
                    clearable
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="3">
                <el-form-item>
                  <t-select
                    placeholder="审核状态"
                    filterable
                    :options="auditList"
                    v-model="testcaseStatus"
                    option-label="label"
                    option-value="value"
                    :full-width="false"
                    clearable
                  >
                    <template v-slot:options="item">
                      <span>{{ item.label }}</span>
                    </template>
                  </t-select>
                </el-form-item>
              </el-col>
              <el-col :span="3">
                <el-form-item>
                  <t-select
                    placeholder="执行结果"
                    filterable
                    :options="stateList"
                    v-model="testcaseExecuteResult"
                    option-label="label"
                    option-value="value"
                    :full-width="false"
                    clearable
                  >
                    <template v-slot:options="item">
                      <span>{{ item.label }}</span>
                    </template>
                  </t-select>
                </el-form-item>
              </el-col>
              <el-col :span="3">
                <el-form-item>
                  <el-select
                    v-model="testcaseType"
                    placeholder="案例类型"
                    filterable
                    clearable
                  >
                    <el-option
                      v-for="(item, index) in auditList1"
                      :key="index"
                      :label="item.label"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="3">
                <el-form-item>
                  <el-select
                    v-model="testcaseNature"
                    placeholder="案例性质"
                    filterable
                    clearable
                  >
                    <el-option
                      v-for="(item, index) in booList"
                      :key="index"
                      :label="item.label"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="2" :offset="1">
                <el-form-item>
                  <el-button type="primary" @click="ClickASPQueryTestCase()"
                    >查 询
                  </el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <!--  表格内容  -->
        <div class="card-container">
          <el-table
            ref="multipleTables"
            :data="testcaseData"
            tooltip-effect="dark"
            style="width: 100%;color:black"
            @selection-change="handleSelectionChange"
            :header-cell-style="{ color: '#545c64' }"
          >
            <el-table-column type="selection" width="40" />
            <el-table-column
              prop="testcaseExecuteResult"
              :render-header="renderHeader"
              width="60"
              align="center"
            >
              <template slot-scope="scope">
                <span v-if="scope.row.testcaseExecuteResult === '0'"></span>
                <span v-if="scope.row.testcaseExecuteResult === '1'"
                  ><i class="el-icon-circle-check resultSuccess"></i
                ></span>
                <span v-if="scope.row.testcaseExecuteResult === '2'"
                  ><i class="el-icon-remove-outline resultBlock"></i
                ></span>
                <span v-if="scope.row.testcaseExecuteResult === '3'"
                  ><i class="el-icon-circle-close resultFail"></i
                ></span>
                <span v-if="scope.row.testcaseExecuteResult === '4'"
                  ><i class="el-icon-circle-plus-outline resultLoseEfficacy"></i
                ></span>
              </template>
            </el-table-column>
            <el-table-column
              prop="testcaseExecuteDate"
              label="执行时间"
              width="100"
            />
            <el-table-column
              prop="planlistTestcaseId"
              label="案例关系编号"
              width="110"
            />
            <el-table-column prop="testcaseNo" label="案例编号" width="170" />
            <el-table-column label="案例名称">
              <template slot-scope="scope">
                <el-tooltip
                  class="item"
                  effect="dark"
                  :content="scope.row.testcaseName"
                  placement="top"
                >
                  <el-link
                    type="primary"
                    :underline="false"
                    @click="
                      (caseDetails = true),
                        queryDetailByTestcaseNoMethod(
                          scope.$index,
                          testcaseData
                        )
                    "
                  >
                    {{ scope.row.testcaseName }}
                  </el-link>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column
              prop="expectedResult"
              label="预期结果"
              show-overflow-tooltip
            ></el-table-column>
            <el-table-column
              prop="funcationPoint"
              label="功能点"
            ></el-table-column>
            <el-table-column
              prop="testcaseStatus"
              label="审核状态"
              :formatter="formatStatus"
              width="100"
            />
            <el-table-column label="操作" width="110" align="center">
              <template slot-scope="scope">
                <el-row class>
                  <el-col :span="24" class="text-right">
                    <el-tooltip
                      class="item"
                      effect="dark"
                      content="编辑案例"
                      v-if="
                        !isWaste &&
                          !isHistory &&
                          (planPower == true || userRole == '50' || isAuthUser)
                      "
                      placement="top"
                    >
                      <i
                        class="el-icon-edit tabel-icon"
                        :style="{
                          cursor:
                            scope.row.testcaseExecuteResult == '0'
                              ? ''
                              : 'not-allowed'
                        }"
                        @click="
                          queryDetailMethod(
                            scope.$index,
                            testcaseData,
                            scope.row
                          )
                        "
                      ></i>
                    </el-tooltip>
                    <!-- 隐藏掉部分功能 -->
                    <el-tooltip
                      class="item"
                      v-if="
                        !isHistory &&
                          !isWaste &&
                          (planPower == true || userRole == '50' || isAuthUser)
                      "
                      effect="dark"
                      content="执行"
                      placement="top"
                    >
                      <i
                        class="el-icon-thumb tabel-icon"
                        @click="executeMethod(scope.$index, testcaseData)"
                      ></i>
                    </el-tooltip>
                    <el-tooltip
                      class="item"
                      effect="dark"
                      content="缺陷"
                      v-if="!isHistory"
                      placement="top"
                    >
                      <i
                        class="el-icon-warning-outline tabel-icon"
                        @click="issueMethod(scope.$index, testcaseData)"
                      ></i>
                    </el-tooltip>
                  </el-col>
                </el-row>
              </template>
            </el-table-column>
          </el-table>
          <el-row class="pagination">
            <el-col :offset="17" :span="7">
              <el-pagination
                background
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="currentPage"
                :page-sizes="[5, 10, 50, 100, 200, 500]"
                :page-size="pageSize"
                layout="sizes, prev, pager, next, jumper"
                :total="total"
              ></el-pagination>
            </el-col>
          </el-row>
          <el-row class="mt-lg">
            <el-col :span="24" class="text-center">
              <el-button-group>
                <el-button
                  v-if="
                    !isWaste &&
                      !isHistory &&
                      (planPower == true || userRole == '50' || isAuthUser)
                  "
                  type="primary"
                  @click="batchCommitAudit()"
                  >批量提交审批</el-button
                >
                <el-button
                  v-if="
                    !isWaste &&
                      (planPower == true ||
                        userRole == '50' ||
                        isSelf ||
                        isAuthUser)
                  "
                  type="primary"
                  @click="batchEffectAudit()"
                  >批量生效</el-button
                >
                <el-button
                  v-if="
                    !isWaste &&
                      (planPower == true ||
                        userRole == '50' ||
                        isSelf ||
                        isAuthUser)
                  "
                  type="primary"
                  @click="batchExecuteTestCase()"
                  >批量执行</el-button
                >
                <el-button
                  v-if="
                    !isWaste &&
                      !isHistory &&
                      (planPower == true || userRole == '50' || isAuthUser)
                  "
                  type="primary"
                  @click="batchCopy = true"
                  >批量复制</el-button
                >
                <el-button
                  v-if="
                    !isWaste &&
                      !isHistory &&
                      (planPower == true || userRole == '50' || isAuthUser)
                  "
                  type="primary"
                  :loading="loading"
                  :disabled="loading"
                  @click="ClickDelTestCase()"
                  >批量删除</el-button
                >
              </el-button-group>
              <el-button-group class="exportBtnGroup">
                <el-button
                  v-if="
                    planPower == true ||
                      userRole == '50' ||
                      isSelf ||
                      isAuthUser
                  "
                  type="primary"
                  @click="addExportDialog()"
                  icon="el-icon-upload2"
                  >全部导出</el-button
                >
                <el-button
                  v-if="
                    !isWaste &&
                      !isHistory &&
                      (planPower == true || userRole == '50' || isAuthUser)
                  "
                  type="primary"
                  @click="templateDownload()"
                  icon="el-icon-caret-bottom"
                  >下载模版</el-button
                >
                <el-upload
                  class="importTestcaseBtn"
                  action=""
                  multiple
                  accept=".xlsx"
                  ref="upload"
                  :http-request="importTestcase"
                  :on-change="saveFile"
                  :show-file-list="false"
                >
                  <el-button
                    v-if="
                      !isWaste &&
                        !isHistory &&
                        (planPower == true || userRole == '50' || isAuthUser)
                    "
                    type="primary"
                    icon="el-icon-download"
                    >导入案例</el-button
                  >
                </el-upload>
              </el-button-group>
            </el-col>
          </el-row>
        </div>
      </div>
      <!-- 开始案例行拖拽排序 -->
      <div v-show="!notShowCaseSort" class="sort">
        <el-table
          class="sortTable-style"
          row-key="testcaseNo"
          ref="multipleTable"
          :data="testcaseSortData"
          tooltip-effect="dark"
          style="width: 100%;color:black"
          :header-cell-style="{ color: '#545c64' }"
        >
          <el-table-column
            prop="testcaseExecuteDate"
            label="执行时间"
            width="100"
          />
          <el-table-column
            prop="planlistTestcaseId"
            label="案例关系编号"
            width="110"
          />
          <el-table-column prop="testcaseNo" label="案例编号" width="170" />
          <el-table-column prop="testcaseName" label="案例名称" />
          <el-table-column
            prop="expectedResult"
            label="预期结果"
            show-overflow-tooltip
          ></el-table-column>
          <el-table-column
            prop="funcationPoint"
            label="功能点"
          ></el-table-column>
          <el-table-column
            prop="testcaseStatus"
            label="审核状态"
            :formatter="formatStatus"
            width="100"
          />
        </el-table>
      </div>
    </el-main>

    <!-- 案例详情弹窗 -->
    <el-dialog
      title="案例详情页"
      :visible.sync="caseDetails"
      width="60%"
      class="abow_dialog"
    >
      <div>
        <el-form
          :inline="true"
          :model="caseDetailsModel"
          :label-width="labelWidth"
          size="mini"
        >
          <el-row>
            <el-col :span="12">
              <el-row>
                <el-form-item label="案例编号 ：">
                  <el-input
                    v-model="caseDetailsModel.testcaseNo"
                    autocomplete="off"
                    suffix-icon="xxx"
                    class="detailsDialog"
                    disabled
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例名称 ：">
                  <el-input
                    v-model="caseDetailsModel.testcaseName"
                    autocomplete="off"
                    suffix-icon="xxx"
                    class="detailsDialog"
                    disabled
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例类型 ：">
                  <el-select
                    v-model="caseDetailsModel.testcaseType"
                    placeholder="请选择"
                    class="detailsDialog"
                    disabled
                  >
                    <el-option label="页面" value="1"></el-option>
                    <el-option label="功能" value="2"></el-option>
                    <el-option label="流程" value="3"></el-option>
                    <el-option label="链接" value="4"></el-option>
                    <el-option label="接口" value="5"></el-option>
                    <el-option label="批量" value="6"></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="优先级 ：">
                  <el-select
                    v-model="caseDetailsModel.testcasePriority"
                    placeholder="请选择"
                    class="detailsDialog"
                    disabled
                  >
                    <el-option label="高" value="1"></el-option>
                    <el-option label="中" value="2"></el-option>
                    <el-option label="低" value="3"></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例性质 ：">
                  <el-select
                    v-model="caseDetailsModel.testcaseNature"
                    placeholder="请选择"
                    class="detailsDialog"
                    disabled
                  >
                    <el-option label="正案例" value="1"></el-option>
                    <el-option label="反案例" value="2"></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
            </el-col>

            <el-col :span="12">
              <el-row>
                <el-form-item label="系统名称 ：">
                  <el-input
                    v-model="caseDetailsModel.systemName"
                    autocomplete="off"
                    suffix-icon="xxx"
                    class="detailsDialog"
                    disabled
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="功能模块 ：">
                  <el-select
                    v-model="caseDetailsModel.testcaseFuncName"
                    class="detailsDialog"
                    disabled
                  >
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="功能点 ：">
                  <el-input
                    type="textarea"
                    v-model="caseDetailsModel.funcationPoint"
                    class="detailsDialogFunction"
                    :rows="6"
                    disabled
                  ></el-input>
                </el-form-item>
              </el-row>
            </el-col>
            <el-col :span="24">
              <el-row>
                <el-form-item label="前置条件 ：">
                  <el-input
                    type="textarea"
                    :rows="2"
                    class="detailsDialogInput"
                    v-model="caseDetailsModel.testcasePre"
                    disabled
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例描述 ：">
                  <el-input
                    type="textarea"
                    :rows="6"
                    class="detailsDialogInput"
                    v-model="caseDetailsModel.testcaseDescribe"
                    disabled
                  />
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="预期结果 ：">
                  <el-input
                    type="textarea"
                    :rows="6"
                    class="detailsDialogInput"
                    v-model="caseDetailsModel.expectedResult"
                    disabled
                  />
                </el-form-item>
              </el-row>
              <el-form-item label="备注 ：">
                <el-input
                  type="textarea"
                  :rows="6"
                  class="detailsDialogInput"
                  v-model="caseDetailsModel.remark"
                  disabled
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="caseDetails = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 新增案例弹窗 -->
    <Dialog
      title="新增案例页面"
      :visible.sync="caseDetailsAdd"
      width="60%"
      class="abow_dialog"
      :before-close="handleClose"
    >
      <div>
        <el-form
          :inline="true"
          :model="caseDetailsModelAdd"
          :rules="dialogRules"
          ref="caseDetailsModelAdd"
          :label-width="labelWidth"
          size="mini"
        >
          <el-row>
            <el-col :span="12">
              <el-row>
                <el-form-item
                  label="系统名称 ："
                  v-if="systemName"
                  prop="systemName"
                >
                  <el-select
                    v-model="caseDetailsModelAdd.systemName"
                    placeholder="请选择"
                    filterable
                    @change="getFirstValue('add')"
                  >
                    <el-option
                      :label="item.sys_module_name"
                      :value="item.sys_id"
                      v-for="(item, index) in systemName"
                      :key="index"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例名称 ：" prop="testcaseName">
                  <el-input
                    v-model="caseDetailsModelAdd.testcaseName"
                    autocomplete="off"
                    suffix-icon="xxx"
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例类型 ：" prop="testcaseType">
                  <el-select
                    v-model="caseDetailsModelAdd.testcaseType"
                    placeholder="请选择"
                  >
                    <el-option label="页面" value="1"></el-option>
                    <el-option label="功能" value="2"></el-option>
                    <el-option label="流程" value="3"></el-option>
                    <el-option label="链接" value="4"></el-option>
                    <el-option label="接口" value="5"></el-option>
                    <el-option label="批量" value="6"></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="优先级 ：" prop="testcasePriority">
                  <el-select
                    v-model="caseDetailsModelAdd.testcasePriority"
                    placeholder="请选择"
                  >
                    <el-option label="高" value="1"></el-option>
                    <el-option label="中" value="2"></el-option>
                    <el-option label="低" value="3"></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例性质 ：" prop="testcaseNature">
                  <el-select
                    v-model="caseDetailsModelAdd.testcaseNature"
                    placeholder="请选择"
                  >
                    <el-option label="正案例" value="1"></el-option>
                    <el-option label="反案例" value="2"></el-option>
                  </el-select>
                </el-form-item>
                <el-row>
                  <el-form-item label="功能点 ：" prop="funcationPoint">
                    <el-input
                      type="textarea"
                      v-model="caseDetailsModelAdd.funcationPoint"
                      class="dialogFunction"
                      :rows="3"
                    ></el-input>
                  </el-form-item>
                </el-row>
              </el-row>
            </el-col>

            <el-col :span="12">
              <el-tree
                class="add-tree meunTree"
                :data="addMeunTreeList"
                show-checkbox
                :check-strictly="true"
                node-key="id"
                ref="tree"
                highlight-current
                :check-on-click-node="false"
                @check="checkMenu"
              />
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-row>
                <el-form-item label="前置条件 ：">
                  <el-input
                    type="textarea"
                    :rows="2"
                    class="inputWidth"
                    v-model="caseDetailsModelAdd.testcasePre"
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例描述 ：" prop="testcaseDescribe">
                  <el-input
                    type="textarea"
                    :rows="5"
                    class="inputWidth"
                    v-model="caseDetailsModelAdd.testcaseDescribe"
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="预期结果 ：" prop="expectedResult">
                  <el-input
                    type="textarea"
                    :rows="4"
                    class="inputWidth"
                    v-model="caseDetailsModelAdd.expectedResult"
                  ></el-input>
                </el-form-item>
              </el-row>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button-group>
          <el-button
            type="primary"
            size="medium"
            @click="addTestcaseMethod('caseDetailsModelAdd')"
            :disabled="disabledAddBtn"
            >提交</el-button
          >
          <el-button
            type="primary"
            size="medium"
            @click="nextTestcaseMethod('caseDetailsModelAdd')"
            :disabled="disabledNextBtn"
            >下一条</el-button
          >
          <el-button
            type="primary"
            size="medium"
            @click="closeTestcaseMethod('caseDetailsModelAdd')"
            >取消</el-button
          >
        </el-button-group>
      </div>
    </Dialog>

    <!-- 案例编辑弹窗 -->
    <Dialog
      title="编辑案例页面"
      :visible.sync="editDialogOpened"
      width="60%"
      class="abow_dialog"
    >
      <div>
        <el-form
          :inline="true"
          :model="caseEditModel"
          :rules="dialogRules"
          ref="caseEditModel"
          :label-width="labelWidth"
          size="mini"
        >
          <el-row>
            <el-col :span="12">
              <el-row>
                <el-form-item label="案例编号 ：" prop="testcaseNo">
                  <el-input
                    v-model="caseEditModel.testcaseNo"
                    autocomplete="off"
                    suffix-icon="xxx"
                    disabled
                  ></el-input>
                </el-form-item>
                <el-form-item
                  label="系统名称 ："
                  v-if="systemName"
                  prop="systemId"
                >
                  <el-select
                    v-model="caseEditModel.systemId"
                    placeholder="请选择"
                    filterable
                    @change="getFirstValue('edit')"
                  >
                    <el-option
                      :label="item.sys_module_name"
                      :value="item.sys_id"
                      v-for="(item, index) in systemName"
                      :key="index"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例名称 ：" prop="testcaseName">
                  <el-input
                    v-model="caseEditModel.testcaseName"
                    autocomplete="off"
                    suffix-icon="xxx"
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例类型 ：" prop="testcaseType">
                  <el-select
                    v-model="caseEditModel.testcaseType"
                    placeholder="请选择"
                  >
                    <el-option label="页面" value="1"></el-option>
                    <el-option label="功能" value="2"></el-option>
                    <el-option label="流程" value="3"></el-option>
                    <el-option label="链接" value="4"></el-option>
                    <el-option label="接口" value="5"></el-option>
                    <el-option label="批量" value="6"></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="优先级 ：" prop="testcasePriority">
                  <el-select
                    v-model="caseEditModel.testcasePriority"
                    placeholder="请选择"
                  >
                    <el-option label="高" value="1"></el-option>
                    <el-option label="中" value="2"></el-option>
                    <el-option label="低" value="3"></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例性质 ：" prop="testcaseNature">
                  <el-select
                    v-model="caseEditModel.testcaseNature"
                    placeholder="请选择"
                  >
                    <el-option label="正案例" value="1"></el-option>
                    <el-option label="反案例" value="2"></el-option>
                  </el-select>
                </el-form-item>
                <el-row>
                  <el-form-item label="功能点 ：" prop="funcationPoint">
                    <el-input
                      type="textarea"
                      v-model="caseEditModel.funcationPoint"
                      class="dialogFunction"
                      :rows="2"
                    ></el-input>
                  </el-form-item>
                </el-row>
              </el-row>
            </el-col>

            <el-col :span="12">
              <el-tree
                class="edit meunTree"
                :data="editMeunTreeList"
                show-checkbox
                default-esxpand-all
                :check-strictly="true"
                node-key="id"
                ref="editTree"
                highlight-current
                :check-on-click-node="false"
                @check="editCheckMenu"
              />
            </el-col>

            <el-col :span="24">
              <el-row>
                <el-form-item label="前置条件 ：">
                  <el-input
                    type="textarea"
                    :rows="2"
                    class="inputWidth"
                    v-model="caseEditModel.testcasePre"
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例描述 ：" prop="testcaseDescribe">
                  <el-input
                    type="textarea"
                    :rows="6"
                    class="inputWidth"
                    v-model="caseEditModel.testcaseDescribe"
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="预期结果 ：" prop="expectedResult">
                  <el-input
                    type="textarea"
                    :rows="6"
                    class="inputWidth"
                    v-model="caseEditModel.expectedResult"
                  />
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="备注 ：">
                  <el-input
                    type="textarea"
                    :rows="6"
                    class="inputWidth"
                    v-model="caseEditModel.remark"
                  />
                </el-form-item>
              </el-row>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="editTestcaseMethod('caseEditModel')"
          >提交</el-button
        >
      </div>
    </Dialog>

    <!-- 复制案例弹窗 -->
    <el-dialog
      title="复制案例页面"
      :visible.sync="copyDetailOpened"
      width="60%"
      class="abow_dialog"
    >
      <div>
        <el-form
          :inline="true"
          :model="copyDetails"
          :rules="dialogRules"
          ref="copyDetails"
          :label-width="labelWidth"
          size="mini"
        >
          <el-row>
            <el-col :span="12">
              <el-row>
                <el-form-item label="计划名称 ：" prop="selectValue">
                  <el-select
                    v-model="copyDetails.selectValue"
                    placeholder="请选择"
                    filterable
                  >
                    <el-option
                      v-for="(item, index) in options"
                      :key="index"
                      :label="item.planName"
                      :value="item.planId"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例编号 ：">
                  <el-input
                    v-model="copyDetails.testcaseNo"
                    autocomplete="off"
                    suffix-icon="xxx"
                    class="detailsDialog"
                    disabled
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例名称 ：">
                  <el-input
                    v-model="copyDetails.testcaseName"
                    autocomplete="off"
                    suffix-icon="xxx"
                    class="detailsDialog"
                    v-bind:disabled="
                      this.overallPlanId != copyDetails.selectValue
                    "
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例类型 ：">
                  <el-select
                    v-model="copyDetails.testcaseType"
                    placeholder="请选择"
                    class="detailsDialog"
                    v-bind:disabled="
                      this.overallPlanId != copyDetails.selectValue
                    "
                  >
                    <el-option label="页面" value="1"></el-option>
                    <el-option label="功能" value="2"></el-option>
                    <el-option label="流程" value="3"></el-option>
                    <el-option label="链接" value="4"></el-option>
                    <el-option label="接口" value="5"></el-option>
                    <el-option label="批量" value="6"></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="优先级 ：">
                  <el-select
                    v-model="copyDetails.testcasePriority"
                    placeholder="请选择"
                    class="detailsDialog"
                    v-bind:disabled="
                      this.overallPlanId != copyDetails.selectValue
                    "
                  >
                    <el-option label="高" value="1"></el-option>
                    <el-option label="中" value="2"></el-option>
                    <el-option label="低" value="3"></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例性质 ：">
                  <el-select
                    v-model="copyDetails.testcaseNature"
                    placeholder="请选择"
                    class="detailsDialog"
                    v-bind:disabled="
                      this.overallPlanId != copyDetails.selectValue
                    "
                  >
                    <el-option label="正案例" value="1"></el-option>
                    <el-option label="反案例" value="2"></el-option>
                  </el-select>
                </el-form-item>
              </el-row>
            </el-col>

            <el-col :span="12">
              <el-row>
                <el-form-item label="系统名称 ：">
                  <el-input
                    v-model="copyDetails.systemName"
                    autocomplete="off"
                    suffix-icon="xxx"
                    class="detailsDialog"
                    v-bind:disabled="
                      this.overallPlanId != copyDetails.selectValue
                    "
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="功能模块 ：">
                  <el-select
                    v-model="copyDetails.testcaseFuncName"
                    class="detailsDialog"
                    v-bind:disabled="
                      this.overallPlanId != copyDetails.selectValue
                    "
                  >
                  </el-select>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="功能点 ：">
                  <el-input
                    type="textarea"
                    v-model="copyDetails.funcationPoint"
                    class="detailsDialogFunction"
                    :rows="8"
                    v-bind:disabled="
                      this.overallPlanId != copyDetails.selectValue
                    "
                  ></el-input>
                </el-form-item>
              </el-row>
            </el-col>
            <el-col :span="24">
              <el-row>
                <el-form-item label="前置条件 ：">
                  <el-input
                    type="textarea"
                    :rows="2"
                    class="detailsDialogInput"
                    v-model="copyDetails.testcasePre"
                    v-bind:disabled="
                      this.overallPlanId != copyDetails.selectValue
                    "
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="案例描述 ：">
                  <el-input
                    type="textarea"
                    :rows="7"
                    class="detailsDialogInput"
                    v-model="copyDetails.testcaseDescribe"
                    v-bind:disabled="
                      this.overallPlanId != copyDetails.selectValue
                    "
                  ></el-input>
                </el-form-item>
              </el-row>
              <el-row>
                <el-form-item label="预期结果 ：">
                  <el-input
                    type="textarea"
                    :rows="2"
                    class="detailsDialogInput"
                    v-model="copyDetails.expectedResult"
                    v-bind:disabled="
                      this.overallPlanId != copyDetails.selectValue
                    "
                  ></el-input>
                </el-form-item>
              </el-row>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button
          type="primary"
          @click="
            copyTestcaseToOtherPlanMethod(
              copyDetails.selectValue,
              'copyDetails'
            )
          "
          >提交</el-button
        >
      </div>
    </el-dialog>

    <!-- 新增测试计划  -->
    <Dialog
      title="新增测试计划"
      :visible.sync="dialogAdd"
      width="35%"
      class="small_dialog"
    >
      <el-form
        :model="form"
        :label-width="formLabelWidth"
        :rules="dialogRules"
        ref="form"
      >
        <el-form-item label="计划名称" prop="inputPlanName">
          <el-input
            v-model="form.inputPlanName"
            autocomplete="off"
            style="width:80%"
            :maxlength="31"
            show-word-limit
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item label="开始时间" prop="inputStartTime">
          <div class="block">
            <el-date-picker
              v-model="form.inputStartTime"
              type="date"
              placeholder="选择开始日期"
              value-format="yyyy-MM-dd"
              style="width:80%"
              :picker-options="inputStartTimePicker"
            ></el-date-picker>
          </div>
        </el-form-item>
        <el-form-item label="结束时间" prop="inputEndTime">
          <div class="block">
            <el-date-picker
              v-model="form.inputEndTime"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="选择结束日期"
              style="width:80%"
              :picker-options="inputEndTimePicker"
            ></el-date-picker>
          </div>
        </el-form-item>
        <el-form-item label="设备信息" prop="deviceInfo">
          <el-input
            v-model="form.deviceInfo"
            autocomplete="off"
            style="width:80%"
            clearable
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogAdd = false">取 消</el-button>
        <el-button
          type="primary"
          @click="
            inputAddPlan(
              form.inputPlanName,
              form.inputStartTime,
              form.inputEndTime,
              form.deviceInfo,
              'form'
            )
          "
          >确 定
        </el-button>
      </div>
    </Dialog>

    <!-- 修改测试计划  -->
    <Dialog
      title="修改测试计划"
      :visible.sync="updatePlan"
      width="35%"
      class="small_dialog"
    >
      <el-form
        :model="planInfo"
        :label-width="formLabelWidth"
        :rules="dialogRules"
        ref="planInfo"
      >
        <el-form-item label="计划名称" prop="planName">
          <el-input
            v-model="planInfo.planName"
            autocomplete="off"
            style="width:80%"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <div class="block">
            <el-date-picker
              v-model="planInfo.startTime"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="请选择开始日期"
              style="width:80%"
              :picker-options="startTimePicker"
            ></el-date-picker>
          </div>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <div class="block">
            <el-date-picker
              v-model="planInfo.endTime"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="请选择结束日期"
              style="width:80%"
              :picker-options="endTimePicker"
            ></el-date-picker>
          </div>
        </el-form-item>
        <el-form-item label="设备信息" prop="deviceInfo">
          <el-input
            v-model="planInfo.deviceInfo"
            autocomplete="off"
            style="width:80%"
            clearable
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="updatePlan = false">取 消</el-button>
        <el-button type="primary" @click="ClickUpdatePlan(planInfo)"
          >确 定</el-button
        >
      </div>
    </Dialog>

    <!-- 批量复制弹窗 -->
    <el-dialog
      title="批量复制页面"
      :visible.sync="batchCopy"
      width="35%"
      class="batchCopy_dialog"
      center
    >
      <el-row>
        <el-form :inline="true" label-width="120px">
          <el-form-item label="计划名称 ：">
            <el-select
              v-model="selectValue"
              placeholder="请选择"
              filterable
              :model="batchCopyModel"
            >
              <el-option
                v-for="(item, index) in options"
                :key="index"
                :label="item.planName"
                :value="item.planId"
              >
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button
          type="primary"
          @click="
            (batchCopy = false), batchCopyTestcaseToOtherPlan(selectValue)
          "
          >提 交</el-button
        >
        <el-button type="primary" @click="batchCopy = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 案例批量执行弹窗 -->
    <el-dialog
      title="案例批量执行页"
      :visible.sync="batchCaseExecute"
      width="30%"
      class="email_dialog"
    >
      <div class="batch_btn_group">
        <el-button-group>
          <el-button
            type="success"
            size="medium"
            :disabled="multipleSelection.length == 0"
            @click="batchExecuteCase('1')"
            >成 功</el-button
          >
          <el-button
            type="warning"
            size="medium"
            :disabled="multipleSelection.length == 0"
            @click="batchExecuteCase('2')"
            >阻 塞</el-button
          >
          <el-button
            type="danger"
            size="medium"
            :disabled="multipleSelection.length == 0"
            @click="batchExecuteCase('3')"
            >失 败</el-button
          >
          <el-button
            type="info"
            size="medium"
            :disabled="multipleSelection.length == 0"
            @click="batchExecuteCase('4')"
            >无 效</el-button
          >
        </el-button-group>
      </div>
      <div v-if="multipleSelection.length == 0" class="tooltip-case" style="">
        未存在可执行状态的案例
      </div>
      <div class="batch_btn">
        <el-button-group class="executeBtn">
          <el-button
            type="primary"
            size="medium"
            @click="batchCaseExecute = false"
            >关 闭</el-button
          >
        </el-button-group>
      </div>
    </el-dialog>

    <!-- 案例执行弹窗 -->
    <el-dialog
      title="案例执行页"
      :visible.sync="caseExecute"
      width="60%"
      class="abow_dialog"
    >
      <div>
        <el-form
          :inline="true"
          :model="caseExecuteModel"
          :label-width="labelWidth"
          size="mini"
        >
          <el-col :span="12">
            <el-form-item label="案例编号 ：">
              <el-input
                v-model="caseExecuteModel.testcaseNo"
                autocomplete="off"
                suffix-icon="xxx"
                class="detailsDialog"
                disabled
              ></el-input>
            </el-form-item>
            <el-form-item label="案例名称 ：">
              <el-input
                v-model="caseExecuteModel.testcaseName"
                autocomplete="off"
                suffix-icon="xxx"
                class="detailsDialog"
                disabled
              ></el-input>
            </el-form-item>
            <el-form-item label="案例类型 ：">
              <el-select
                v-model="caseExecuteModel.testcaseType"
                placeholder="请选择"
                class="detailsDialog"
                disabled
              >
                <el-option label="页面" value="1"></el-option>
                <el-option label="功能" value="2"></el-option>
                <el-option label="流程" value="3"></el-option>
                <el-option label="链接" value="4"></el-option>
                <el-option label="接口" value="5"></el-option>
                <el-option label="批量" value="6"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="优先级 ：">
              <el-select
                v-model="caseExecuteModel.testcasePriority"
                placeholder="请选择"
                class="detailsDialog"
                disabled
              >
                <el-option label="高" value="1"></el-option>
                <el-option label="中" value="2"></el-option>
                <el-option label="低" value="3"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="案例性质 ：">
              <el-select
                v-model="caseExecuteModel.testcaseNature"
                placeholder="请选择"
                class="detailsDialog"
                disabled
              >
                <el-option label="正案例" value="1"></el-option>
                <el-option label="反案例" value="2"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="系统名称 ：">
              <el-input
                v-model="caseExecuteModel.systemName"
                autocomplete="off"
                suffix-icon="xxx"
                class="detailsDialog"
                disabled
              ></el-input>
            </el-form-item>
            <el-form-item label="功能模块 ：">
              <el-select
                v-model="caseExecuteModel.testcaseFuncName"
                class="detailsDialog"
                disabled
              >
              </el-select>
            </el-form-item>
            <el-form-item label="功能点 ：">
              <el-input
                type="textarea"
                v-model="caseExecuteModel.funcationPoint"
                class="detailsDialogFunction"
                :rows="6"
                disabled
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-row>
              <el-form-item label="前置条件 ：">
                <el-input
                  type="textarea"
                  :rows="2"
                  class="detailsDialogInput"
                  v-model="caseExecuteModel.testcasePre"
                  disabled
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="案例描述 ：">
                <el-input
                  type="textarea"
                  :rows="7"
                  class="detailsDialogInput"
                  v-model="caseExecuteModel.testcaseDescribe"
                  disabled
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="预期结果 ：">
                <el-input
                  type="textarea"
                  :rows="6"
                  class="detailsDialogInput"
                  v-model="caseExecuteModel.expectedResult"
                  disabled
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="备注 ：">
                <el-input
                  type="textarea"
                  :rows="2"
                  class="detailsDialogInput"
                  v-model="caseExecuteModel.remark"
                ></el-input>
              </el-form-item>
            </el-row>
          </el-col>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button-group>
          <el-button
            type="success"
            size="medium"
            :disabled="executeBtn"
            @click="executeCase(caseExecuteModel, 1)"
            >成 功</el-button
          >
          <el-button
            type="warning"
            size="medium"
            :disabled="executeBtn"
            @click="executeCase(caseExecuteModel, 2)"
            >阻 塞</el-button
          >
          <el-button
            type="danger"
            size="medium"
            :disabled="executeBtn"
            @click="executeCase(caseExecuteModel, 3)"
            >失 败</el-button
          >
          <el-button
            type="info"
            size="medium"
            :disabled="executeBtn"
            @click="executeCase(caseExecuteModel, 4)"
            >无 效</el-button
          >
          <el-button
            type="primary"
            size="medium"
            @click="mantisMethod(caseExecuteModel)"
            >提交mantis</el-button
          >
        </el-button-group>
        <el-button-group class="executeBtn">
          <el-button
            type="primary"
            size="medium"
            @click="lastExecute(testcaseData)"
            >上一条</el-button
          >
          <el-button
            type="primary"
            size="medium"
            @click="nextExecute(testcaseData, '1')"
            >下一条</el-button
          >
          <el-button type="primary" size="medium" @click="caseExecute = false"
            >关 闭</el-button
          >
        </el-button-group>
      </div>
    </el-dialog>

    <!-- mantis提交弹窗 -->
    <Dialog
      title="新增缺陷页面"
      :visible.sync="mantisDialog"
      width="60%"
      class="abow_dialog"
      :before-close="handleCloseMantis"
    >
      <el-form
        :inline="true"
        :model="mantisDialogAdd"
        :label-width="labelWidth"
        :rules="mantisDialogRules"
        ref="mantisDialogAdd"
        size="mini"
      >
        <el-row>
          <el-col :span="12">
            <el-row>
              <el-form-item label="项目名称" prop="project">
                <el-select
                  v-model="mantisDialogAdd.project"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in projectList"
                    :key="index"
                    :label="item.name"
                    :value="item.id"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item
                label="任务"
                prop="taskNo"
                v-if="showTask == '1' && taskLength > 0"
              >
                <el-select
                  v-model="mantisDialogAdd.taskNo"
                  placeholder="请选择任务"
                  filterable
                  clearable
                  @input="taskChange"
                >
                  <el-option
                    v-for="(item, index) in taskOptions"
                    :key="index"
                    :value="item.value"
                    :label="item.label"
                  >
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item
                prop="app_name"
                label="应用英文名"
                v-if="workNoAppLists.length > 0"
              >
                <el-select
                  v-model="mantisDialogAdd.app_name"
                  class="dialogMantis"
                  placeholder
                  collapse-tags
                  multiple
                >
                  <el-option
                    v-for="(item, index) in workNoAppLists"
                    :key="index"
                    :label="item.label"
                    :value="item.label"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="应用英文名" v-else>
                <el-input
                  v-model="mantisDialogAdd.app_name"
                  class="dialogMantis"
                  placeholder
                  filterable
                  clearable
                >
                </el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="系统名称">
                <el-input
                  v-model="mantisDialogAdd.system_name"
                  autocomplete="off"
                  class="detailsDialog"
                  suffix-icon="xxx"
                  disabled
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="功能模块">
                <el-select
                  v-model="mantisDialogAdd.function_module"
                  class="detailsDialog"
                  disabled
                ></el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="需求编号/实施单元">
                <el-input
                  v-model="mantisDialogAdd.redmine_id"
                  autocomplete="off"
                  class="detailsDialog"
                  suffix-icon="xxx"
                  disabled
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="工单号">
                <el-input
                  v-model="mantisDialogAdd.workNo"
                  autocomplete="off"
                  class="detailsDialog"
                  suffix-icon="xxx"
                  disabled
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="系统版本" prop="system_version">
                <el-select
                  v-model="mantisDialogAdd.system_version"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in optionSV"
                    :key="index"
                    :label="item.lalel"
                    :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="归属阶段" prop="stage">
                <el-select
                  v-model="mantisDialogAdd.stage"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in optionBS"
                    :key="index"
                    :label="item.lalel"
                    :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="开发责任人" prop="developer">
                <el-select
                  v-model="mantisDialogAdd.developer"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in developerList"
                    :key="index"
                    :label="item.label"
                    :value="item.value"
                  >
                    <span style="float: left">{{ item.label }}</span>
                    <span style="float: right">{{ item.value }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="分派给" prop="handler">
                <el-select
                  v-model="mantisDialogAdd.handler"
                  placeholder="请选择"
                  filterable
                  clearable
                  @change="getHandlerCn"
                >
                  <el-option
                    v-for="(item, index) in handlerList"
                    :key="index"
                    :value="item.user_name_en"
                    :label="item.user_name_cn"
                  >
                    <span style="float: left">{{ item.user_name_cn }}</span>
                    <span style="float: right">{{ item.user_name_en }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="额外抄送人员" prop="copy_to_list">
                <el-select
                  v-model="mantisDialogAdd.copy_to_list"
                  multiple
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in handlerList"
                    :key="index"
                    :value="item.user_name_en"
                    :label="item.user_name_cn"
                  >
                    <span style="float: left">{{ item.user_name_cn }}</span>
                    <span style="float: right">{{ item.user_name_en }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-row>
          </el-col>

          <el-col :span="12">
            <el-row>
              <el-form-item label="优先级" prop="priority">
                <el-select
                  v-model="mantisDialogAdd.priority"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in optionPriority"
                    :key="index"
                    :label="item.value"
                    :value="item.label"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="严重性" prop="severity">
                <el-select
                  v-model="mantisDialogAdd.severity"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in optionSeverity"
                    :key="index"
                    :label="item.value"
                    :value="item.label"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="缺陷来源" prop="flaw_source">
                <el-select
                  v-model="mantisDialogAdd.flaw_source"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in optionDS"
                    :key="index"
                    :label="item.lalel"
                    :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="预计修复时间" prop="plan_fix_date">
                <el-date-picker
                  v-model="mantisDialogAdd.plan_fix_date"
                  type="date"
                  value-format="yyyy-MM-dd"
                  placeholder="请选择"
                  :picker-options="expireTimeOption"
                  style="width:90%"
                ></el-date-picker>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="缺陷类型" prop="flaw_type">
                <el-select
                  v-model="mantisDialogAdd.flaw_type"
                  placeholder="请选择"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="(item, index) in optionTDT"
                    :key="index"
                    :label="item.lalel"
                    :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="上传文件 " class="filesEditCss">
                <el-upload
                  :action="actionUrl"
                  multiple
                  accept=".txt, .jpg, .png"
                  ref="upload"
                  :before-remove="beforeRemove"
                  :on-remove="handleRemove"
                  :auto-upload="false"
                  :file-list="fileList"
                  :on-change="test"
                >
                  <el-button size="small" type="primary">点击上传</el-button>
                  <div slot="tip" class="el-upload__tip">
                    只能上传txt/jpg/png文件
                  </div>
                </el-upload>
              </el-form-item>
            </el-row>
          </el-col>
          <el-col :span="24">
            <el-row>
              <el-form-item label="摘要" prop="summary">
                <el-input
                  type="textarea"
                  :rows="2"
                  show-word-limit
                  maxlength="100"
                  class="inputWidth"
                  v-model="mantisDialogAdd.summary"
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="描述" prop="description">
                <el-input
                  type="textarea"
                  :rows="4"
                  maxlength="500"
                  show-word-limit
                  class="inputWidth"
                  v-model="mantisDialogAdd.description"
                ></el-input>
              </el-form-item>
            </el-row>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelMantisAdd('mantisDialogAdd')">取 消</el-button>
        <el-button
          type="primary"
          :loading="loadingBtn"
          @click="submitMantisAdd('mantisDialogAdd')"
          >提 交</el-button
        >
      </div>
    </Dialog>
    <!--缺陷详情-->
    <el-dialog
      title="缺陷详情页面"
      :visible.sync="mantisListDialog"
      width="60%"
      class="abow_dialog"
      :before-close="handleCloseMantisDialog"
    >
      <el-table
        stripe
        :data="mantisTableData"
        tooltip-effect="dark"
        style="width: 100%;color:black"
        :header-cell-style="{ color: '#545c64' }"
      >
        <el-table-column
          prop="planlist_testcase_id"
          label="案例关系编号"
          width="130"
        ></el-table-column>
        <el-table-column
          prop="summary"
          label="缺陷摘要"
          width="200"
        ></el-table-column>
        <el-table-column
          prop="flaw_source"
          label="缺陷来源"
          width="150"
        ></el-table-column>
        <el-table-column
          prop="developer_cn"
          label="开发责任人"
        ></el-table-column>
        <el-table-column prop="reporter" label="缺陷提出人员"></el-table-column>
        <el-table-column prop="handler" label="分派给"></el-table-column>
        <el-table-column
          prop="status"
          label="状态"
          :formatter="formatterStatus"
        ></el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-tooltip
              class="item"
              effect="dark"
              content="详情"
              placement="top"
            >
              <i
                class="el-icon-document tabel-icon"
                @click="detailData(scope.$index, scope.row)"
              ></i>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 缺陷详情弹窗 -->
    <MantisDialog
      :mantisTitle="detailTitle"
      :openDialog="mantisDetailDialog"
      :task_no="task_name"
      :dialogModel="mantisDialogDetail"
      @mantisClose="closeDialogDetail"
      :filesContent="filesContent"
    />
    <!-- 发送评审案例弹窗 -->
    <el-dialog
      :visible.sync="caseEmailDialog"
      title="发送案例评审"
      class="email_dialog"
    >
      <el-form
        :inline="true"
        :model="sendEmailsModel"
        :rules="emailRules"
        ref="sendEmailsModel"
        :label-width="labelWidth"
        size="medium"
      >
        <el-row>
          <el-form-item label="测试关系人 ：" prop="ftmsRelativePeople">
            <el-select
              v-model="sendEmailsModel.ftmsRelativePeople"
              placeholder="请选择"
              filterable
              multiple
              @change="testSend"
            >
              <el-option
                :label="item.user_name_cn"
                :value="item.email"
                v-for="(item, index) in relativePeoples.ftms"
                :key="index"
              >
                <span style="float: left">{{ item.user_name_cn }}</span>
                <span style="float: right">{{ item.user_name_en }}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="任务关系人 ：" prop="fdevRelativePeople">
            <el-select
              v-model="sendEmailsModel.fdevRelativePeople"
              placeholder="请选择"
              filterable
              multiple
              @change="testSend"
            >
              <el-option
                :label="item.user_name_cn"
                :value="item.email"
                v-for="item in relativePeoples.fdev"
                :key="item.email"
              >
                <span style="float: left">{{ item.user_name_cn }}</span>
                <span style="float: right">{{ item.user_name_en }}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="额外抄送人 ：" prop="otherPeople">
            <el-select
              v-model="sendEmailsModel.otherPeople"
              placeholder="请选择"
              filterable
              multiple
              @change="testSend"
            >
              <el-option
                :label="item.user_name_cn"
                :value="item.email"
                v-for="(item, index) in allPeopleOption"
                :key="index"
              >
                <span style="float: left">{{ item.user_name_cn }}</span>
                <span style="float: right">{{ item.user_name_en }}</span>
              </el-option>
            </el-select>
          </el-form-item>
        </el-row>
        <el-row>
          <el-form-item
            v-if="options.length > 1"
            label="选择计划 ："
            prop="plans"
          >
            <el-select
              v-model="sendEmailsModel.plans"
              placeholder="请选择"
              filterable
              multiple
              clearable
              @change="testSend"
            >
              <el-option
                v-for="(item, index) in relativePeoples.plan"
                :key="index"
                :label="item.planName"
                :value="item.planId"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item v-else label="选择计划 ：" prop="planzz">
            <el-select
              v-model="sendEmailsModel.planzz"
              placeholder="请选择"
              filterable
              multiple
              clearable
              @change="testSend"
            >
              <el-option
                v-for="(item, index) in relativePeoples.plan"
                :key="index"
                :label="item.planName"
                :value="item.planId"
              >
              </el-option>
            </el-select>
          </el-form-item>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button
          type="primary"
          @click="sendEmails('sendEmailsModel')"
          :disabled="disabledSendEmails"
          >发送</el-button
        >
      </div>
    </el-dialog>
    <!-- 文件预览弹框 -->
    <el-dialog :visible.sync="filePreviewDialog" class="filePreviewDialog">
      <div v-html="filePreview">{{ filePreview }}</div>
    </el-dialog>
    <!-- 全部导出弹窗 -->
    <el-dialog
      title="全部导出页面"
      :visible.sync="allExport"
      width="35%"
      class="batchCopy_dialog"
      center
    >
      <el-row>
        <el-form :inline="true" label-width="120px">
          <el-form-item label="计划名称 ：">
            <el-select
              v-model="selectExportValue"
              multiple
              placeholder="请选择多个计划"
              filterable
            >
              <el-option
                v-for="(item, index) in options"
                :key="index"
                :label="item.planName"
                :value="item.planId"
              >
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button
          type="primary"
          @click="(allExport = false), exportExcelTestcase()"
          >导 出</el-button
        >
        <el-button type="primary" @click="allExport = false">关 闭</el-button>
      </div>
    </el-dialog>
    <!-- 工单废弃弹框 -->
    <el-dialog
      :visible.sync="dialogAddMigrationOrder"
      title="工单迁移页面"
      width="40%"
      center
      class="migration-style"
    >
      <el-tabs v-model="activeName" @tab-click="handleClick" type="border-card">
        <el-tab-pane label="执行计划迁移" name="first">
          <el-form label-width="120px" class="form-margin">
            <el-form-item label="迁移计划 ：">
              <el-select
                v-model="selectPlanId"
                class="testcase-style"
                multiple
                collapse-tags
                clearable
                placeholder="请选择计划"
                filterable
              >
                <el-option
                  v-for="(item, index) in options"
                  :key="index"
                  :label="item.planName"
                  :value="item.planId"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="目标工单：">
              <el-select
                v-model="targetWorkNo"
                placeholder="请选择工单名称"
                clearable
                filterable
              >
                <el-option
                  v-for="(item, index) in targetWorkNoOptions"
                  :key="index"
                  :label="item.mainTaskName"
                  :value="item.workNo"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button
                center
                type="primary"
                :disabled="migraPlanDisable"
                :loading="loadMigrationPlan"
                @click="asyncmigrationPlan()"
                >确认计划迁移</el-button
              >
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="案例迁移" name="second">
          <el-form label-width="120px" class="form-margin">
            <el-form-item label="迁移计划 ：">
              <el-select
                v-model="selectTestcasePlanId"
                placeholder="请选择迁移计划名称"
                filterable
                clearable
              >
                <el-option
                  v-for="(item, index) in options"
                  :key="index"
                  :label="item.planName"
                  :value="item.planId"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="迁移案例 ：">
              <el-select
                v-model="selectTestcase"
                class="testcase-style"
                collapse-tags
                :loading="loadTestcase"
                multiple
                placeholder="请选择迁移案例名称"
                filterable
                clearable
              >
                <el-option
                  v-for="(item, index) in testcaseOptions"
                  :key="index"
                  :label="item.testcaseName"
                  :value="item.planlistTestcaseId"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="目标工单：">
              <el-select
                v-model="targetWorkNo_"
                placeholder="请选择目标工单名称"
                filterable
                clearable
              >
                <el-option
                  v-for="(item, index) in targetWorkNoOptions"
                  :key="index"
                  :label="item.mainTaskName"
                  :value="item.workNo"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="目标计划：">
              <el-select
                v-model="targetPlanId_"
                :loading="loadTargetPlan"
                placeholder="请选择目标计划名称"
                filterable
                clearable
              >
                <el-option
                  v-for="(item, index) in targetPlanOptions"
                  :key="index"
                  :label="item.planName"
                  :value="item.planId"
                >
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="loadMigrationTestcase"
                :disabled="migraTestcaseDisable"
                @click="migrationTestcase()"
                >确认案例迁移</el-button
              >
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </el-container>
</template>
<script>
import moment from 'moment';
import Sortable from 'sortablejs';
import { queryOrderInfoByNo } from '@/services/order.js';
import {
  createPlanAddModel,
  createMantisModel,
  stateList,
  auditList,
  auditList1,
  dialogRules,
  mantisDialogRules,
  testcaseStatusList
} from './model';
import { exportExcel, getUserListByRole } from '@/common/utlis';
import { mapActions, mapState } from 'vuex';
import {
  optionSV,
  optionBS,
  optionPriority,
  optionSeverity,
  optionDS,
  optionDT,
  optionDTSA,
  optionDSA,
  optionStatusEdit
} from '../Mantis/model.js';
import MantisDialog from '../Mantis/MantisDialog';

export default {
  name: 'JobList',
  components: {
    MantisDialog
  },
  data() {
    return {
      orderType: '', //工单类型 function-功能测试工单，security-安全测试工单
      taskLength: '',
      appNameArrayTmp: [],
      handlerTmp: '',
      handlerCnTmp: '',
      task_name: '',
      showTask: '',
      workNoAppLists: [],
      taskOptions: [],
      loadTestcase: false, // 加载迁移案例
      loadTargetPlan: false, // 加载目标计划
      loadMigrationPlan: false, // 确认迁移加载
      loadMigrationTestcase: false, // 确认迁移加载
      targetPlanId_: '',
      targetWorkNo_: '',
      allPeopleOption: '',
      targetPlanOptions: [],
      selectTestcasePlanId: '',
      activeName: 'first',
      testcaseOptions: [],
      selectTestcase: [],
      selectPlanId: '',
      targetWorkNo: '',
      targetWorkNoOptions: [],
      dialogAddMigrationOrder: false, // 工单废弃弹框
      caseEmailDialog: false, //发送案例评审弹框
      filePreview: null,
      filePreviewDialog: false, // 文件预览弹框
      selectExportValue: [],
      allExport: false,
      notShowCaseSort: true, // 默认不展示案例排序
      allSortCase: [],
      routerPlanId: null,
      mantisPlanIdTestcaseId: '',
      stage: '',
      userRole: sessionStorage.getItem('Trole'),
      currentPage: 1,
      pageSize: 5,
      total: 0,
      value1: [],
      formInline: {
        taskName: '',
        region: '',
        region1: '',
        region2: '',
        region3: ''
      },
      disabledSendBtn: false,
      disabledAddBtn: false,
      disabledNextBtn: true,
      batchCopy: false,
      batchCopyModel: [],
      copyDetailOpened: false,
      editDialogOpened: false,
      active: Boolean,
      systemName: [],
      //页面权限
      planPower: '',
      //任务名称 测试人员
      mainTaskName: '',
      testers: '',
      //案例复制下拉框选择值
      selectValue: '',

      //案例多条件查询
      updatePlan: false,
      testcaseExecuteResult: '',
      testcaseType: '',
      testcaseNature: '',
      testcaseStatus: '',
      testcaseName: '',
      copyDetails: {},

      //全局planId
      overallPlanId: 0,

      //新增计划
      inputStartTime: '',
      inputEndTime: '',
      PlanworkNo: 'q',
      inputPlanName: '',

      //弹框
      dialogAdd: false,
      form: {
        inputPlanName: '',
        inputStartTime: '',
        inputEndTime: '',
        deviceInfo: ''
      },
      caseDetails: false,
      caseDetailsAdd: false,
      formLabelWidth: '24%',
      labelWidth: '110px',
      caseDetailsModel: {},
      sendEmailsModel: {
        relativePeople: [],
        ftmsRelativePeople: [],
        fdevRelativePeople: [],
        otherPeople: [],
        plans: [],
        planzz: []
      }, //发送案例邮件表单
      caseDetailsModelAdd: createPlanAddModel(),
      firstCommit: {
        testcaseName: '',
        testcaseType: '',
        testcaseNature: '',
        expectedResult: ''
      },
      caseEditModel: {},
      planInfo: {
        taskName: '',
        tester: [{ name: '' }, { name: '' }],
        planName: '',
        startTime: '',
        endTime: '',
        allCase: 0,
        exeNum: 0,
        sumUnExe: 0,
        successNum: 0,
        zuaiNum: 0,
        failedNum: 0,
        getPlanId: '',
        deviceInfo: ''
      },
      auditList,
      stateList,
      auditList1,
      booList: [
        { label: '正', value: '1' },
        { label: '反', value: '2' }
      ],
      multipleSelection: [],
      //计划
      planList: [],
      testcaseData: [],
      testcaseSortData: [],
      index: -1,
      //案例复制计划展示下拉框
      options: [],
      inputStartTimePicker: this.beginDate(),
      inputEndTimePicker: this.processDate(),
      startTimePicker: this.alterBeginDate(),
      endTimePicker: this.alterProcessDate(),
      menuList: [],
      addMeunTreeList: [],
      copyMeunTreeList: [],
      editMeunTreeList: [],
      checkedId: '',
      planNameStatusCheck: true,
      planName: '',
      dialogRules,
      mantisDialogRules,
      // 案例执行相关
      caseExecuteModel: {},
      caseExecute: false,
      // mantis相关
      userMantisToken: sessionStorage.getItem('mantisToken'),
      expireTimeOption: {
        disabledDate(time) {
          return time.getTime() <= Date.now() - 24 * 60 * 60 * 1000;
        }
      },
      mantisDialog: false,
      mantisDetailDialog: false,
      mantisListDialog: false,
      mantisTableData: [],
      mantisDialogDetail: {},
      filesContent: [],
      mantisDialogAdd: createMantisModel(),
      projectList: [],
      developerList: [],
      handlerList: [],
      optionSV,
      optionBS,
      optionPriority,
      optionSeverity,
      optionStatusEdit,
      unitNo: '',
      workNo: '',
      actionUrl: '',
      fileList: [],
      getFileData: [],
      detailTitle: '缺陷详情',
      handlerCnName: '',
      //发送案例评审校验
      emailRules: {
        plans: [
          {
            type: 'array',
            required: true,
            message: '请选择计划',
            trigger: 'change'
          }
        ]
      },
      executeBtn: true,
      indexData: '',
      runIndex: '',
      getId: '',
      loadingBtn: false,
      fileTypeArray: [],
      disabledSendEmails: false,
      judgeFileType: true,
      // 案例导入
      file: null,
      caseImportDialog: false,
      caseImport: {},
      importSystemName: '',
      batchErrorMsg: '',
      isHistory: false,
      isAuthUser: false,
      isWaste: false,
      isSelf: false,
      loading: false,
      batchCaseExecute: false,
      indexCase: Number,
      testcaseStatusList
    };
  },
  watch: {
    async selectTestcasePlanId(val) {
      // 清空不请求
      if (val) {
        // 加载案例
        this.loadTestcase = true;
        await this.QueryTestcaseByPlanId({
          currentPage: 1,
          pageSize: 5000,
          planId: val
        });
        this.selectTestcase = [];
        this.testcaseOptions = this.testCaseData.map(item => {
          return {
            planlistTestcaseId: item.planlistTestcaseId,
            testcaseName: item.testcaseName
          };
        });
        this.loadTestcase = false;
      }
    },
    async targetWorkNo_(val) {
      // 清空不请求
      if (val) {
        this.loadTargetPlan = true;
        await this.queryByworkNo({
          workNo: val
        });
        this.targetPlanId_ = '';
        this.targetPlanOptions = this.planListArr.map(item => {
          return {
            planId: item.planId,
            planName: item.planName
          };
        });
        this.loadTargetPlan = false;
      }
    },
    workNoAppList: {
      deep: true,
      handler(val) {
        if (val.length > 0) {
          this.mantisDialogRules.app_name = [
            { required: true, message: '请选择应用英文名', trigger: 'blur' }
          ];
        } else {
          delete this.mantisDialogRules.app_name;
        }
      }
    },
    workNoAppLists: {
      deep: true,
      handler(val) {
        if (val.length > 0) {
          this.mantisDialogRules.app_name = [
            { required: true, message: '请选择应用英文名', trigger: 'blur' }
          ];
        } else {
          delete this.mantisDialogRules.app_name;
        }
      }
    }
  },
  computed: {
    ...mapState('workOrderForm', ['taskAllData']),
    migraPlanDisable() {
      if (this.targetWorkNo !== '' && this.selectPlanId.length > 0) {
        return false;
      } else {
        return true;
      }
    },
    optionTDT() {
      if (this.orderType == 'security') {
        return optionDTSA;
      } else {
        return optionDT;
      }
    },
    optionDS() {
      if (this.orderType == 'security') {
        return optionDSA;
      } else {
        return optionDS;
      }
    },
    migraTestcaseDisable() {
      if (
        this.selectTestcasePlanId !== '' &&
        this.selectTestcase.length > 0 &&
        this.targetWorkNo_ !== '' &&
        this.targetPlanId_ !== ''
      ) {
        return false;
      } else {
        return true;
      }
    },
    ...mapState('testPlanForm', [
      'workOrderNoData',
      'systemNameString',
      'caseDetail',
      'testCaseData',
      'planInfoList',
      'totalCount',
      'fileObj',
      'fileTemlate',
      'allPlan',
      'isTestCase',
      'targetWork',
      'tableData',
      'planListArr',
      'menus',
      'relativePeoples',
      'allPeoples'
    ]),
    ...mapState('testCaseForm', ['updatePlanArr']),
    ...mapState('mantisForm', [
      'mantisProjects',
      'developerArr',
      'fdevDevelopList',
      'issueDetailList',
      'workNoAppList'
    ])
  },
  methods: {
    ...mapActions('workOrderForm', ['queryTasks']),
    ...mapActions('menu', ['getUserApprovalList']),
    ...mapActions('testPlanForm', [
      'queryAllPeople',
      'queryTaskNameTestersByNo',
      'UpdateTestcaseByTestcaseNo',
      'AddTestcase',
      'QueryAllSystem',
      'CopyTestcaseToOtherPlan',
      'QueryDetailByTestcaseNo',
      'UpdateTestcaseByStatusWaitEffect',
      'UpdateTestcaseByStatusWaitPass',
      'DeleteTestcaseByTestcaseNo',
      'QueryTestcaseByPlanId',
      'queryPlanAllStatus',
      'delBatchRelationCase',
      'QueryTestCount',
      'batchCommitAuditFun',
      'batchEffectAuditFun',
      'batchExecuteTestcaseFun', //批量执行
      'batchCopyTestcaseToOtherPlanFun',
      'exportExcelTestcaseFun',
      'downloadTemplate',
      'batchAdd',
      'queryByPlanIdAll',
      'isTestcaseAddIssue',
      'sendCaseEmail',
      'delPlan',
      'testCaseCustomSort',
      'queryUserValidOrder',
      'movePlanOrCase',
      'queryIssueByPlanResultId',
      'addPlan',
      'queryByworkNo',
      'updateByPrimaryKey',
      'queryFuncMenuBySysId',
      'queryRelativePeople'
    ]),
    ...mapActions('mantisForm', [
      'queryMantisProjects',
      'queryIssueDetail',
      'queryAppByWorkNo'
    ]),
    ...mapActions('testCaseForm', ['add', 'updatePlanlistTestcaseRelation']),
    //案例审核状态
    formatStatus(row, column) {
      return this.testcaseStatusList[row.testcaseStatus];
    },

    //分页
    handleSizeChange: function(size) {
      this.pageSize = size;
      this.initTestCase(this.overallPlanId, this.pageSize, this.currentPage);
    },

    handleCurrentChange(val) {
      this.currentPage = val;
      this.initTestCase(this.overallPlanId, this.pageSize, val);
    },
    handleClick(tab, event) {},
    // 迁移计划
    async asyncmigrationPlan() {
      this.loadMigrationPlan = true;
      await this.movePlanOrCase({
        moveType: '1',
        fromPlanId: this.selectPlanId,
        toWorkNo: this.targetWorkNo
      });
      this.loadMigrationPlan = false;
      this.$message({
        showClose: true,
        message: '迁移计划成功',
        type: 'success'
      });
      this.dialogAddMigrationOrder = false;
      this.selectPlanId = [];
      this.targetWorkNo = '';
      // 更新计划
      this.initqueryByworkNo(this.PlanworkNo);
      this.loadMigrationPlan = false;
    },
    // 迁移案例
    async migrationTestcase() {
      this.loadMigrationTestcase = true;
      await this.movePlanOrCase({
        moveType: '0',
        planlist_testcase_id: this.selectTestcase,
        toWorkNo: this.targetWorkNo_,
        toPlanId: this.targetPlanId_
      });
      this.loadMigrationTestcase = false;
      this.$message({
        showClose: true,
        message: '迁移案例成功',
        type: 'success'
      });
      this.dialogAddMigrationOrder = false;
      this.selectTestcasePlanId = '';
      this.selectTestcase = [];
      this.targetWorkNo_ = '';
      this.targetPlanId_ = '';
      // 更新计划下的案例
      this.initqueryPlanStatus(this.getPlanId);
      this.queryTaskCount(this.getPlanId);
      this.initTestCase(this.getPlanId, this.pageSize, this.currentPage);
      this.loadMigrationTestcase = false;
    },
    // 发送案例评审邮件
    async sendTestcaseMail() {
      const workOrderNoDataSession = JSON.parse(
        sessionStorage.getItem('planWorkOrderNo')
      );
      await this.queryRelativePeople({
        workNo: this.routerPlanId
          ? this.$route.query.workOrderNo
          : workOrderNoDataSession.workNo
      });
      this.sendEmailsModel.fdevRelativePeople = this.relativePeoples.fdev.map(
        item => {
          return item.email;
        }
      );
      this.sendEmailsModel.ftmsRelativePeople = this.relativePeoples.ftms.map(
        item => {
          return item.email;
        }
      );
      this.sendEmailsModel.planzz = this.relativePeoples.plan.map(item => {
        return item.planId;
      });
      this.disabledSendBtn = true;
      this.caseEmailDialog = true;
      this.disabledSendBtn = false;
    },
    // 发送评审邮件弹框里的发送事件
    async sendEmails(sendEmailsModel) {
      this.disabledSendEmails = true;
      let plansList = [];
      this.sendEmailsModel.plans.forEach((item, index) => {
        this.relativePeoples.plan.forEach(ele => {
          if (ele.planId == item) {
            plansList[index] = ele;
          }
        });
      });
      this.sendEmailsModel.relativePeople = Array.from(
        new Set(
          this.sendEmailsModel.fdevRelativePeople.concat(
            this.sendEmailsModel.ftmsRelativePeople.concat(
              this.sendEmailsModel.otherPeople
            )
          )
        )
      );
      if (this.sendEmailsModel.relativePeople.length == 0) {
        this.$message({
          message: '至少选择一个要发送的测试或者任务关系人',
          type: 'warning'
        });
      } else {
        this.$refs[sendEmailsModel].validate(async valid => {
          if (valid) {
            await this.sendCaseEmail({
              workNo: this.PlanworkNo,
              email: this.sendEmailsModel.relativePeople,
              plan: this.relativePeoples.plan.length > 1 ? plansList : undefined
            });
            this.$message({
              showClose: true,
              message: '发送成功',
              type: 'success'
            });
            this.caseEmailDialog = false;
          }
        });
      }
      this.disabledSendEmails = false;
    },
    testSend() {},
    // 删除计划
    deletePlan() {
      this.$confirm('此操作将删除选中的计划, 是否继续?', '提示', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await this.delPlan({
          planId: this.overallPlanId,
          workNo: this.PlanworkNo
        });
        this.$message({
          showClose: true,
          message: '删除成功',
          type: 'success'
        });
        // 更新计划
        this.initqueryByworkNo(this.PlanworkNo);
      });
    },

    // table header中加图标展示
    renderHeader(h, { column }) {
      return h('div', [
        h('span', column.label),
        h('i', {
          class: 'el-icon-thumb',
          style: 'margin-top:18px;font-size:18px;font-wight:600'
        })
      ]);
    },

    // 新增计划弹框开始结束时间控制
    beginDate() {
      const self = this;
      return {
        disabledDate(time) {
          if (self.form.inputEndTime) {
            //如果结束时间不为空，则小于结束时间
            return new Date(self.form.inputEndTime).getTime() <= time.getTime();
          } else {
            // return time.getTime() > Date.now()//开始时间不选时，结束时间最大值小于等于当天
          }
        }
      };
    },
    processDate() {
      const self = this;
      return {
        disabledDate(time) {
          if (self.form.inputStartTime) {
            //如果开始时间不为空，则结束时间大于开始时间
            return (
              new Date(self.form.inputStartTime).getTime() - 86400000 >
              time.getTime()
            );
          } else {
            // return time.getTime() > Date.now()//开始时间不选时，结束时间最大值小于等于当天
          }
        }
      };
    },

    // 修改计划弹框开始结束时间控制
    alterBeginDate() {
      const self = this;
      return {
        disabledDate(time) {
          if (self.planInfo.endTime) {
            //如果结束时间不为空，则小于结束时间
            return new Date(self.planInfo.endTime).getTime() < time.getTime();
          } else {
            // return time.getTime() > Date.now()//开始时间不选时，结束时间最大值小于等于当天
          }
        }
      };
    },
    alterProcessDate() {
      const self = this;
      return {
        disabledDate(time) {
          if (self.planInfo.startTime) {
            //如果开始时间不为空，则结束时间大于开始时间
            return (
              new Date(self.planInfo.startTime).getTime() - 86400000 >
              time.getTime()
            );
          } else {
            // return time.getTime() > Date.now()//开始时间不选时，结束时间最大值小于等于当天
          }
        }
      };
    },

    Reuse() {
      if (this.planList.length > 0) {
        // let planIdList = [];
        // this.planList.forEach(item => {
        //   planIdList.push(item.planId)
        // })
        const planIdData = { planId: this.overallPlanId };
        sessionStorage.setItem('planId', JSON.stringify(planIdData));
        if (Object.keys(this.$route.query).length) {
          sessionStorage.setItem(
            'workNoAndTestcaseIdAndPlanId',
            JSON.stringify({
              workOrderNo: this.$route.query.workOrderNo,
              planlist_testcase_id: this.$route.query.planlist_testcase_id,
              planId: this.$route.query.planId
            })
          );
        }
        this.$router.push('/reuse');
      } else {
        this.$message({
          showClose: true,
          message: '暂无执行计划',
          type: 'warning'
        });
        return;
      }
    },
    //计划修改
    async ClickUpdatePlan(planInfo) {
      await this.updateByPrimaryKey({
        planId: this.overallPlanId,
        planName: planInfo.planName,
        planStartDate: planInfo.startTime,
        planEndDate: planInfo.endTime,
        deviceInfo: planInfo.deviceInfo
      });
      this.updatePlan = false;
      this.initqueryByworkNo(this.PlanworkNo);
      this.$message({
        showClose: true,
        message: '修改成功',
        type: 'success'
      });
    },

    //计划展示 ( 一 )
    async initqueryByworkNo(workNo) {
      let res;
      await this.queryByworkNo({
        workNo: workNo
      });
      res = this.planListArr;
      if (res.length == 0) {
        this.planList = [];
        return;
      }
      this.planList = res;
      //案例展示
      this.options = res;
      this.overallPlanId = this.planList[0].planId;
      this.showPlanTestStatus(
        this.planList[0].planId,
        this.planList[0].planName,
        this.planList[0].planStartDate,
        this.planList[0].planEndDate,
        this.planList[0].deviceInfo
      );
      return res;
    },
    //整合信息展示计划状态案例状态等信息(二)
    async showPlanTestStatus(
      planId,
      planName,
      startDate,
      endDate,
      deviceInfo,
      index
    ) {
      this.planName = planName;
      this.active = index;
      this.overallPlanId = planId;
      this.planInfo.startTime = startDate.split('T')[0];
      this.planInfo.endTime = endDate.split('T')[0];
      this.planInfo.planName = planName;
      this.planInfo.deviceInfo = deviceInfo;
      this.initqueryPlanStatus(planId);
      this.currentPage = 1;
      this.queryTaskCount(planId);
      // 如果在排序，则查出该计划下的所有案例
      if (this.notShowCaseSort === false) {
        // 开始排序，查该计划下的所有案例
        await this.QueryTestcaseByPlanId({
          planId,
          pageSize: 0,
          currentPage: 1
        });
        this.testcaseSortData = this.testCaseData;
      } else {
        this.initTestCase(planId, this.pageSize, this.currentPage);
      }
    },
    //点击之后 根据计划Id查询当前计划下所有的案例状态 (三)
    async initqueryPlanStatus(planId) {
      // 展示工单信息
      await this.queryPlanAllStatus({
        planId: planId
      });
      this.planInfo.allCase = this.planInfoList.allCase;
      this.planInfo.exeNum = this.planInfoList.exeNum;
      this.planInfo.sumUnExe = this.planInfoList.sumUnExe;
      this.planInfo.successNum = this.planInfoList.sumSucc;
      this.planInfo.zuaiNum = this.planInfoList.sumBlock;
      this.planInfo.failedNum = this.planInfoList.sumFail;
      this.getPlanId = this.planInfoList.PLAN_ID;
      //加载时间
      return planId;
    },

    //新增计划(四)
    async inputAddPlan(
      planName,
      inputStartTime,
      inputEndTime,
      deviceInfo,
      form
    ) {
      this.$refs[form].validate(async valid => {
        if (valid) {
          this.planNameStatusCheck = true;
          //本页面判断
          this.planList.forEach(item => {
            if (item.planName === planName) {
              this.planNameStatusCheck = false;
              this.$message({
                showClose: true,
                message: '已经有相同的计划名称,请重新输入',
                type: 'error'
              });
            }
          });

          //新增加
          if (this.planNameStatusCheck) {
            await this.addPlan({
              planName: planName,
              workNo: this.PlanworkNo,
              planStartDate: inputStartTime,
              planEndDate: inputEndTime,
              deviceInfo: deviceInfo
            });
            this.$message({
              showClose: true,
              message: '添加成功',
              type: 'success'
            });
            this.dialogAdd = false;
            this.initqueryByworkNo(this.PlanworkNo);
            this.$refs[form].resetFields();
            //加载时间
          }
        } else {
          return false;
        }
      });
    },

    //动态多条件查询 (5)
    async ClickASPQueryTestCase() {
      this.currentPage = 1;
      await this.QueryTestcaseByPlanId({
        testcaseName: this.testcaseName ? this.testcaseName : '',
        testcaseStatus: this.testcaseStatus ? this.testcaseStatus : '',
        testcaseExecuteResult: this.testcaseExecuteResult
          ? this.testcaseExecuteResult
          : '',
        testcaseType: this.testcaseType ? this.testcaseType : '',
        testcaseNature: this.testcaseNature ? this.testcaseNature : '',
        planId: this.overallPlanId,
        pageSize: this.pageSize,
        currentPage: this.currentPage,
        planlistTestcaseId: this.mantisPlanIdTestcaseId
      });
      this.testcaseData = this.testCaseData;
      this.queryTaskCount(this.overallPlanId);
    },
    //获取选中的row multipleTable
    handleSelectionChange(rows) {
      this.multipleSelection = rows;
    },
    //批量删除 (6)
    async ClickDelTestCase() {
      if (this.multipleSelection.length <= 0) {
        this.$confirm('请选择要操作的案例', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        return;
      }
      this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(async () => {
          this.loading = true;
          try {
            await this.delBatchRelationCase({
              testcaseNos: this.multipleSelection,
              planId: this.overallPlanId
            });
            this.queryTaskCount(this.overallPlanId);
            this.currentPage = 1;
            this.initTestCase(
              this.overallPlanId,
              this.pageSize,
              this.currentPage
            );
            this.initqueryPlanStatus(this.getPlanId);
            this.$message({
              showClose: true,
              message: '删除成功',
              type: 'success'
            });
            this.loading = false;
          } catch (e) {
            this.loading = false;
            this.ClickASPQueryTestCase();
            throw err;
          }
        })
        .catch(err => {
          this.$message({
            showClose: true,
            type: 'info',
            message: '已取消删除'
          });
        });
    },

    //加载系统数据
    async querySystem(type) {
      if (this.planList.length > 0) {
        if (type === 'add') {
          this.caseDetailsAdd = true;
          this.disabledAddBtn = false;
          this.disabledNextBtn = true;
        }
      } else {
        this.$message({
          showClose: true,
          message: '暂无执行计划',
          type: 'warning'
        });
        return;
      }
      this.editMeunTreeList = [];
      this.addMeunTreeList = [];
      this.copyMeunTreeList = [];
      await this.QueryAllSystem({});
      this.systemName = this.systemNameString;
    },
    appendNode(parent, set, depth = 6) {
      if (!Array.isArray(parent) || !Array.isArray(set)) {
        return [];
      }

      // if (parent.length === 0 || set.length === 0) {
      //   return [];
      // }
      if (parent.length === 0) {
        return [];
      }
      if (depth === 0) {
        return parent;
      }
      const child = parent.reduce((pre, next, index) => {
        const nodes = set.filter(menu => {
          return menu.parent_id === next.id;
        });

        next.children = nodes;

        // next.header = 'nodes';
        return pre.concat(nodes);
      }, []);

      if (child.length > 0) {
        this.appendNode(child, set, --depth);
      }
      return parent;
    },
    getMenuList(type) {
      const root = this.menuList.filter(meun => meun.parent_id === 0);
      let meunTreeList = this.appendNode(
        root,
        this.menuList.filter(meun => meun.func_id && meun.parent_id !== 0)
      );
      if (type === 'add') {
        this.addMeunTreeList = meunTreeList;
      } else if (type === 'copy') {
        this.copyMeunTreeList = meunTreeList;
      } else if (type === 'edit') {
        this.editMeunTreeList = meunTreeList;
      }
    },

    checkMenu(obj, node) {
      if (node.checkedKeys.length === 1) {
        this.checkedId = node.checkedKeys[0];
      } else {
        this.checkedId = node.checkedKeys.filter(id => {
          return id != this.checkedId;
        });
      }
      this.$refs.tree.setCheckedKeys([]);
      this.$refs.tree.setCheckedKeys([this.checkedId]);
    },
    editCheckMenu(obj, node) {
      if (node.checkedKeys.length === 1) {
        this.checkedId = node.checkedKeys[0];
      } else {
        this.checkedId = node.checkedKeys.filter(id => {
          return id != this.checkedId;
        });
      }
      this.$refs.editTree.setCheckedKeys([]);
      this.$refs.editTree.setCheckedKeys([this.checkedId]);
    },
    // 根据选择的系统名称获得一级菜单级别  hf
    async getFirstValue(type) {
      let id = '';
      if (type === 'add') {
        id = this.caseDetailsModelAdd.systemName;
      } else if (type === 'copy') {
        id = this.copyDetails.systemName;
      } else if (type === 'edit') {
        id = this.caseEditModel.systemId;
      }
      await this.queryFuncMenuBySysId({
        sys_id: id
      });
      this.menuList = this.menus.map(menu => {
        return {
          ...menu,
          label: menu.func_model_name,
          id: menu.func_id
        };
      });
      this.getMenuList(type);
    },

    pushObjs(obj) {
      if (obj) {
        this.caseDetailsModelAdd.testcaseFuncObj.push({
          id: obj.func_id,
          name: obj.func_model_name
        });
      }
    },

    //新增案例页面  提交
    addTestcaseMethod(caseDetailsModelAdd) {
      if (
        this.firstCommit.testcaseName !=
          this.caseDetailsModelAdd.testcaseName ||
        this.firstCommit.testcaseType !=
          this.caseDetailsModelAdd.testcaseType ||
        this.firstCommit.testcaseNature !=
          this.caseDetailsModelAdd.testcaseNature ||
        this.firstCommit.expectedResult !=
          this.caseDetailsModelAdd.expectedResult ||
        !this.firstCommit.testcaseName
      ) {
        this.$refs[caseDetailsModelAdd].validate(async valid => {
          if (valid) {
            // 选择的菜单id  this.$refs.tree.getCheckedKeys()
            let ids = this.$refs.tree.getCheckedKeys();
            let fnId = ids[0];
            if (fnId) {
              let obj = this.caseDetailsModelAdd;
              this.pushObjs(obj.firstMenu);
              this.pushObjs(obj.secondMenu);
              this.pushObjs(obj.thirdMenu);
              this.pushObjs(obj.fourthMenu);
              this.pushObjs(obj.fifthMenu);
              this.pushObjs(obj.sixthMenu);
              this.pushObjs(obj.seventhMenu);
              await this.AddTestcase({
                testcase: {
                  testcaseName: this.caseDetailsModelAdd.testcaseName.trim(),
                  testcaseStatus: this.caseDetailsModelAdd.testcaseStatus,
                  testcaseType: this.caseDetailsModelAdd.testcaseType,
                  testcasePriority: this.caseDetailsModelAdd.testcasePriority,
                  testcasePre: this.caseDetailsModelAdd.testcasePre.trim(),
                  testcaseNature: this.caseDetailsModelAdd.testcaseNature,
                  funcationPoint: this.caseDetailsModelAdd.funcationPoint.trim(),
                  testcaseDescribe: this.caseDetailsModelAdd.testcaseDescribe.trim(),
                  expectedResult: this.caseDetailsModelAdd.expectedResult.trim(),
                  remark: this.caseDetailsModelAdd.remark,
                  testcaseVersion: this.caseDetailsModelAdd.testcaseVersion,
                  testcasePeople: this.caseDetailsModelAdd.testcasePeople,
                  testcaseDate: this.caseDetailsModelAdd.testcaseDate,
                  testcaseFuncId: fnId
                },
                sysId: this.caseDetailsModelAdd.systemName,
                planId: this.overallPlanId,
                workNo: this.PlanworkNo
              });
              this.$message({
                showClose: true,
                type: 'success',
                message: '新增案例成功!'
              });
              this.disabledAddBtn = true;
              this.disabledNextBtn = false;
              this.queryTaskCount(this.overallPlanId);
              this.initTestCase(
                this.overallPlanId,
                this.pageSize,
                this.currentPage
              );
              this.initqueryPlanStatus(this.getPlanId);
            } else {
              this.$message({
                showClose: true,
                message: '请选择功能模块',
                type: 'error'
              });
              return false;
            }
          } else {
            return false;
          }
        });
      } else {
        this.$message({
          showClose: true,
          message:
            '案例名称、案例类型、案例性质、预期结果与上一条内容一致，请修改其中任意一项内容后提交',
          type: 'error'
        });
      }
    },

    //新增案例页面  下一条
    nextTestcaseMethod(nextTestcaseMethod) {
      this.disabledAddBtn = false;
      this.disabledNextBtn = true;
      this.firstCommit.testcaseName = this.caseDetailsModelAdd.testcaseName;
      this.firstCommit.testcaseType = this.caseDetailsModelAdd.testcaseType;
      this.firstCommit.testcaseNature = this.caseDetailsModelAdd.testcaseNature;
      this.firstCommit.expectedResult = this.caseDetailsModelAdd.expectedResult;
    },

    //新增案例页面  取消
    closeTestcaseMethod(caseDetailsModelAdd) {
      this.caseDetailsAdd = false;
      this.$refs[caseDetailsModelAdd].resetFields();
      this.caseDetailsModelAdd = createPlanAddModel();
    },

    //新增案例页面  关闭
    handleClose(caseDetailsModelAdd) {
      this.caseDetailsAdd = false;
      this.caseDetailsModelAdd = createPlanAddModel();
    },

    //查询案例详情页面
    async queryDetailByTestcaseNoMethod(index, row) {
      await this.QueryDetailByTestcaseNo({
        testcaseNo: this.testcaseData[index].testcaseNo
      });
      this.caseDetailsModel = this.caseDetail === null ? {} : this.caseDetail;
    },

    //查询案例详情页面 编辑
    async queryDetailMethod(index, row, data) {
      if (data.testcaseExecuteResult === '0') {
        this.editDialogOpened = true;
        await this.QueryDetailByTestcaseNo({
          testcaseNo: this.testcaseData[index].testcaseNo
        });
        this.caseEditModel = this.caseDetail === null ? {} : this.caseDetail;
        this.getFirstValue('edit');
        this.$refs.editTree.setCheckedKeys([this.caseEditModel.testcaseFuncId]);
        this.index = index;
        await this.querySystem();
      } else {
        return;
      }
    },

    //编辑案例信息
    editTestcaseMethod(caseEditModel) {
      this.$refs[caseEditModel].validate(async valid => {
        if (valid) {
          let ids = this.$refs.editTree.getCheckedKeys();
          let fnId = ids[0];
          await this.UpdateTestcaseByTestcaseNo({
            planlistTestcaseId: this.testcaseData[this.index]
              .planlistTestcaseId,
            planId: this.testcaseData[this.index].planId,
            workNo: this.PlanworkNo,
            sysId: 1,
            testcase: {
              testcaseNo: this.caseEditModel.testcaseNo,
              testcaseName: this.caseEditModel.testcaseName,
              testcaseStatus: this.caseEditModel.testcaseStatus,
              testcaseType: this.caseEditModel.testcaseType,
              testcasePriority: this.caseEditModel.testcasePriority,
              testcasePre: this.caseEditModel.testcasePre,
              testcaseNature: this.caseEditModel.testcaseNature,
              funcationPoint: this.caseEditModel.funcationPoint,
              testcaseDescribe: this.caseEditModel.testcaseDescribe,
              expectedResult: this.caseEditModel.expectedResult,
              remark: this.caseEditModel.remark,
              testcaseVersion: this.caseEditModel.testcaseVersion,
              testcasePeople: this.caseEditModel.testcasePeople,
              testcaseDate: this.caseEditModel.testcaseDate,
              testcaseFuncId: fnId,
              field1: null,
              field2: null,
              field3: null,
              field4: null,
              field5: null
            }
          });
          this.$message({
            showClose: true,
            type: 'success',
            message: '编辑成功!'
          });
          this.editDialogOpened = false;
          this.$refs[caseEditModel].resetFields();
          this.initTestCase(
            this.overallPlanId,
            this.pageSize,
            this.currentPage
          );
        } else {
          return false;
        }
      });
    },
    //案例复制到当前计划下
    copyTestcaseToOtherPlanMethod(copyTestcaseToOtherPlanMethod, copyDetails) {
      this.$refs[copyDetails].validate(async valid => {
        if (valid) {
          this.copyDetailOpened = false;
          await this.CopyTestcaseToOtherPlan({
            // planId: this.overallPlanId,
            orderPlanId: this.overallPlanId,
            targetPlanId: copyTestcaseToOtherPlanMethod,
            sysId: this.copyDetails.systemId,
            testcase: this.copyDetails,
            workNo: this.PlanworkNo
          });
          this.$message({
            showClose: true,
            type: 'success',
            message: '复制成功!'
          });
          this.initTestCase(
            this.overallPlanId,
            this.pageSize,
            this.currentPage
          );
          this.queryTaskCount(this.overallPlanId);
          this.initqueryPlanStatus(this.getPlanId);
        } else {
          return false;
        }
      });
    },

    //批量提交审批
    async batchCommitAudit() {
      if (this.multipleSelection.length <= 0) {
        this.$confirm('请选择要操作的案例', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        return;
      }
      this.$confirm('此操作将批量审核文件, 是否继续?', '提示', {
        confirmButtonText: '批量审批',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await this.batchCommitAuditFun({
          testcaseNo: this.multipleSelection,
          testcaseStatus: '10'
        });
        this.$message({
          showClose: true,
          type: 'success',
          message: '批量审批提交成功!'
        });
        this.queryTaskCount(this.overallPlanId);
        this.initTestCase(this.overallPlanId, this.pageSize, this.currentPage);
      });
    },
    //批量执行
    async batchExecuteTestCase() {
      if (this.multipleSelection.length <= 0) {
        this.$confirm('请选择要操作的案例', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        return;
      }
      this.batchCaseExecute = true;
      this.multipleSelection = this.multipleSelection.filter(item => {
        return (
          item.testcaseExecuteResult == 0 &&
          item.testcaseStatus > 10 &&
          item.testcaseStatus < 31 &&
          item.testcaseStatus !== '12'
        ); //testcaseExecuteResult为0表示未执行过的案例
      });
      let ids = this.multipleSelection;
      this.$refs.multipleTables.clearSelection();
      ids.forEach(row => {
        this.$refs.multipleTables.toggleRowSelection(row, true);
      });
    },
    //批量执行成功,堵塞按钮
    async batchExecuteCase(testcaseExecuteResult) {
      this.$confirm('此操作将批量执行案例, 是否继续?', '提示', {
        confirmButtonText: '批量执行',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        let ids = this.multipleSelection.map(item => {
          return `${item.planlistTestcaseId}`;
        });
        await this.batchExecuteTestcaseFun({
          ids,
          testcaseExecuteResult
        });
        this.$message({
          showClose: true,
          type: 'success',
          message: '批量执行成功!'
        });
        this.batchCaseExecute = false;
        await this.queryTaskCount(this.overallPlanId);
        await this.initTestCase(
          this.overallPlanId,
          this.pageSize,
          this.currentPage
        );
        await this.initqueryPlanStatus(this.getPlanId);
      });
    },
    //批量生效
    async batchEffectAudit() {
      if (this.multipleSelection.length <= 0) {
        this.$confirm('请选择要操作的案例', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        return;
      }
      this.$confirm('此操作将批量生效文件, 是否继续?', '提示', {
        confirmButtonText: '批量生效',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await this.batchEffectAuditFun({
          testcaseNo: this.multipleSelection,
          testcaseStatus: '20'
        });
        this.$message({
          showClose: true,
          type: 'success',
          message: '批量生效成功!'
        });
        this.queryTaskCount(this.overallPlanId);
        this.initTestCase(this.overallPlanId, this.pageSize, this.currentPage);
      });
    },
    //批量复制
    async batchCopyTestcaseToOtherPlan(param) {
      if (this.multipleSelection.length <= 0) {
        this.$confirm('请选择要操作的案例', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        return;
      }
      this.$confirm('此操作将批量复制文件, 是否继续?', '提示', {
        confirmButtonText: '批量复制',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await this.batchCopyTestcaseToOtherPlanFun({
          testcaseNo: this.multipleSelection,
          planId: param,
          workNo: this.PlanworkNo
        });
        this.$message({
          showClose: true,
          type: 'success',
          message: '批量复制成功!'
        });
        this.queryTaskCount(this.overallPlanId);
        this.initTestCase(this.overallPlanId, this.pageSize, this.currentPage);
        this.initqueryPlanStatus(this.getPlanId);
      });
    },
    addExportDialog() {
      this.allExport = true;
      if (this.options.length === 1 && this.selectExportValue.length === 0) {
        this.selectExportValue.push(this.options[0].planId);
      }
    },
    //全部导出
    async exportExcelTestcase() {
      let exportValue = this.options.filter(item =>
        this.selectExportValue.includes(item.planId)
      );
      let planIdAndName = exportValue.map(item => {
        return {
          planId: item.planId,
          planName: item.planName
        };
      });
      await this.exportExcelTestcaseFun({
        planIds: planIdAndName,
        testcaseStatus: this.testcaseStatus ? this.testcaseStatus : null,
        testcaseExecuteResult: this.testcaseExecuteResult
          ? this.testcaseExecuteResult
          : null,
        testcaseType: this.testcaseType ? this.testcaseType : null,
        testcaseNature: this.testcaseNature ? this.testcaseNature : null
      });
      let res = this.fileObj;
      let fileName = res.headers['content-disposition'];
      const fileType = res.headers['content-type'];
      fileName = fileName.substring(fileName.indexOf('=') + 1);
      const blob = new Blob([res.data], { type: fileType });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');

      link.href = url;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      URL.revokeObjectURL(url);
      document.body.removeChild(link);
      this.$message({
        showClose: true,
        type: 'success',
        message: '全部导出成功!'
      });
      this.selectExportValue = [];
    },

    // 下载模版
    async templateDownload() {
      await this.downloadTemplate();
      let res = this.fileTemlate;
      let fileName = res.headers['content-disposition'];
      const fileType = res.headers['content-type'];
      fileName = fileName.substring(fileName.indexOf('=') + 1);
      const blob = new Blob([res.data], { type: fileType });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      URL.revokeObjectURL(url);
      document.body.removeChild(link);
      this.$message({
        showClose: true,
        type: 'success',
        message: '下载成功!'
      });
    },

    //查询总数
    async queryTaskCount(planId) {
      await this.QueryTestCount({
        planId: planId,
        testcaseName: this.testcaseName ? this.testcaseName : '',
        testcaseStatus: this.testcaseStatus ? this.testcaseStatus : '',
        testcaseExecuteResult: this.testcaseExecuteResult
          ? this.testcaseExecuteResult
          : '',
        testcaseType: this.testcaseType ? this.testcaseType : '',
        testcaseNature: this.testcaseNature ? this.testcaseNature : '',
        planlistTestcaseId: this.mantisPlanIdTestcaseId
      });
      this.total = this.totalCount;
    },
    // 点击【总数】【执行数】【未执行数】【成功】【阻塞】【失败】按钮，分别执行查询
    queryTestcaseSingle(param) {
      this.testcaseExecuteResult = param;
      this.testcaseName = '';
      this.testcaseStatus = '';
      this.testcaseType = '';
      this.testcaseNature = '';
      this.mantisPlanIdTestcaseId = '';
      this.ClickASPQueryTestCase();
      this.indexCase = 10;
    },
    //页面加载初始化案例
    async initTestCase(planId, pageSize, currentPage) {
      await this.QueryTestcaseByPlanId({
        planId: planId,
        pageSize: pageSize ? pageSize : this.pageSize,
        currentPage: currentPage ? currentPage : this.currentPage,
        testcaseName: this.testcaseName ? this.testcaseName : '',
        testcaseStatus: this.testcaseStatus ? this.testcaseStatus : '',
        testcaseExecuteResult: this.testcaseExecuteResult
          ? this.testcaseExecuteResult
          : '',
        testcaseType: this.testcaseType ? this.testcaseType : '',
        testcaseNature: this.testcaseNature ? this.testcaseNature : '',
        planlistTestcaseId: this.mantisPlanIdTestcaseId
      });
      this.testcaseData = this.testCaseData;
    },

    //  案例执行
    async executeMethod(index, row) {
      this.getId = this.testcaseData[index].planlistTestcaseId;
      this.caseExecute = true;
      await this.QueryDetailByTestcaseNo({
        testcaseNo: this.testcaseData[index].testcaseNo
      });
      this.caseExecuteModel = this.caseDetail === null ? {} : this.caseDetail;
      if (
        row[index].testcaseStatus > 10 &&
        row[index].testcaseStatus < 31 &&
        row[index].testcaseStatus !== '12'
      ) {
        this.executeBtn = false;
      } else {
        this.executeBtn = true;
      }
      this.indexData = index;
      this.runIndex = index;
    },

    async issueMethod(index, row) {
      this.mantisListDialog = true;
      // this.getId = (this.testcaseData[index].planlistTestcaseId);
      await this.queryIssueByPlanResultId({
        id: row[index].planlistTestcaseId
      });
      this.mantisTableData = this.tableData;
    },

    async detailData(index, row) {
      this.mantisDetailDialog = true;
      await this.queryIssueDetail({
        id: row.id
      });
      this.mantisDialogDetail = this.issueDetailList;
      this.filesContent = this.issueDetailList.files;
      this.task_name = this.issueDetailList.task_name;
    },

    // 执行弹框 上一条
    async lastExecute(testcaseData) {
      let allDate = testcaseData;
      this.indexData -= 1;
      this.runIndex -= 1;
      if (allDate[this.indexData] === undefined) {
        let totalPageSize = Math.ceil(this.total / this.pageSize);
        if (totalPageSize >= this.currentPage && this.currentPage !== 1) {
          // 查询上一页的数据
          this.currentPage -= 1;
          await this.initTestCase(this.overallPlanId);
          this.indexData = this.pageSize;
          allDate = this.testcaseData;
        } else {
          this.$message({
            showClose: true,
            type: 'warning',
            message: '该条案例已经是第一条案例'
          });
          return;
        }
        await this.QueryDetailByTestcaseNo({
          testcaseNo: allDate[this.pageSize - 1].testcaseNo
        });
        this.caseExecuteModel = this.caseDetail === null ? {} : this.caseDetail;
        if (
          this.caseDetail.testcaseStatus > 10 &&
          this.caseDetail.testcaseStatus < 31 &&
          this.caseDetail.testcaseStatus !== '12'
        ) {
          this.executeBtn = false;
        } else {
          this.executeBtn = true;
        }
        this.getId = allDate[this.pageSize - 1].planlistTestcaseId;
      } else {
        await this.QueryDetailByTestcaseNo({
          testcaseNo: allDate[this.indexData].testcaseNo
        });
        this.caseExecuteModel = this.caseDetail === null ? {} : this.caseDetail;
        if (
          this.caseDetail.testcaseStatus > 10 &&
          this.caseDetail.testcaseStatus < 31 &&
          this.caseDetail.testcaseStatus !== '12'
        ) {
          this.executeBtn = false;
        } else {
          this.executeBtn = true;
        }
        this.getId = allDate[this.indexData].planlistTestcaseId;
      }
    },

    // 下一条
    async nextExecute(testcaseData, flag) {
      let allDate = testcaseData;
      if (
        this.indexCase === 3 &&
        (this.testcaseExecuteResult === '1' ||
          this.testcaseExecuteResult === '2' ||
          this.testcaseExecuteResult === '0')
      ) {
        if (allDate[this.runIndex] === undefined) {
          let totalPageSize = Math.ceil(this.total / this.pageSize);
          if (totalPageSize > this.currentPage) {
            (this.currentPage += 1),
              await this.initTestCase(this.overallPlanId);
            this.indexData = 0;
          } else {
            this.$message({
              showClose: true,
              type: 'warning',
              message: '该条案例已经是最后一条案例'
            });
            return;
          }
        }
        await this.QueryDetailByTestcaseNo({
          testcaseNo: allDate[this.runIndex].testcaseNo
        });
        this.getId = allDate[this.runIndex].planlistTestcaseId;
      } else if (
        flag === '1' ||
        this.testcaseExecuteResult === '' ||
        this.testcaseExecuteResult === 'allExe'
      ) {
        this.indexData += 1;
        this.runIndex += 1;
        if (allDate[this.indexData] === undefined) {
          let totalPageSize = Math.ceil(this.total / this.pageSize);
          if (totalPageSize > this.currentPage) {
            this.currentPage += 1;
            await this.initTestCase(this.overallPlanId);
            this.indexData = 0;
            allDate = this.testcaseData;
          } else {
            this.$message({
              showClose: true,
              type: 'warning',
              message: '该条案例已经是最后一条案例'
            });
            this.indexData -= 1;
            return;
          }
        }
        await this.QueryDetailByTestcaseNo({
          testcaseNo: allDate[this.indexData].testcaseNo
        });
        this.getId = allDate[this.indexData].planlistTestcaseId;
      } else {
        if (allDate[this.runIndex] === undefined) {
          let totalPageSize = Math.ceil(this.total / this.pageSize);
          if (totalPageSize > this.currentPage) {
            // 查询下一页的数据
            (this.currentPage += 1),
              await this.initTestCase(this.overallPlanId);
            this.indexData = 0;
          } else {
            this.$message({
              showClose: true,
              type: 'warning',
              message: '该条案例已经是最后一条案例'
            });
            this.runIndex -= 1;
            return;
          }
        }
        // 点击功能按钮（失败按钮除外）
        await this.QueryDetailByTestcaseNo({
          testcaseNo: allDate[this.runIndex].testcaseNo
        });
        this.getId = allDate[this.runIndex].planlistTestcaseId;
      }
      this.caseExecuteModel = this.caseDetail === null ? {} : this.caseDetail;
      if (
        this.caseDetail.testcaseStatus > 10 &&
        this.caseDetail.testcaseStatus < 31 &&
        this.caseDetail.testcaseStatus !== '12'
      ) {
        this.executeBtn = false;
      } else {
        this.executeBtn = true;
      }
      this.indexCase = 10;
      this.queryTaskCount(this.overallPlanId);
    },
    //选择任务重新拿回应用和分派给人员
    //若选择任务则分派给为当前任务的开发人员，应用为当前任务对应的应用
    //若不选择任务，则分派给为当前工单对应的实施单元的负责人，应用为该工单对应的所有任务对应的应用
    async taskChange() {
      await this.queryTasks({
        workNo: this.$route.query.workOrderNo,
        taskNo: this.mantisDialogAdd.taskNo
      });
      let workNoAppLists = this.taskAllData.taskList.map(item => {
        return { label: item.project_name, value: item.project_id };
      });
      var result = {};
      var workNoAppListsTmp = [];
      for (var i = 0; i < workNoAppLists.length; i++) {
        if (
          !result[workNoAppLists[i].value] &&
          workNoAppLists[i].value != null &&
          !result[workNoAppLists[i].label] &&
          workNoAppLists[i].label != null
        ) {
          workNoAppListsTmp.push(workNoAppLists[i]);
          result[workNoAppLists[i].value] = true;
        }
      }
      this.workNoAppLists = workNoAppListsTmp;
      let app_name = workNoAppListsTmp.map(item => {
        return item.label;
      });
      const set = new Set(app_name);
      let appNameArray = Array.from(set);
      if (appNameArray.length === 0) {
        appNameArray = '';
      }
      if (this.mantisDialogAdd.taskNo == '') {
        this.mantisDialogAdd.app_name = this.appNameArrayTmp;
        this.mantisDialogAdd.handler = this.handlerTmp;
        this.mantisDialogAdd.handlerCn = this.handlerCnTmp;
      } else {
        this.mantisDialogAdd.app_name = appNameArray;
        this.mantisDialogAdd.handler = this.taskAllData.taskList[0].developer[0].user_name_en;
        this.mantisDialogAdd.handlerCn = this.taskAllData.taskList[0].developer[0].user_name_cn;
      }
    },
    // mantis 相关方法
    async mantisMethod(caseExecuteModel) {
      this.loadingBtn = false;
      let res = await queryOrderInfoByNo({
        workNo: this.$route.query.workOrderNo
      });
      this.orderType = res.orderType;
      await this.isTestcaseAddIssue({
        planlist_testcase_id: this.getId
      });
      if (this.isTestCase == false) {
        this.$message({
          showClose: true,
          type: 'warning',
          message: '只有案例执行失败后,才可提交mantis'
        });
      } else if (this.isTestCase == true) {
        //查询mantis项目
        await this.queryMantisProjects();
        this.projectList = this.mantisProjects;
        //查任务列表
        await this.queryTasks({
          workNo: this.$route.query.workOrderNo
        });
        this.taskLength = this.taskAllData.taskList.length;
        if (this.taskAllData.taskList.length > 0) {
          this.mantisDialogAdd.redmine_id = this.taskAllData.taskList[0].demand.oa_contact_no;
        } else {
          this.mantisDialogAdd.redmine_id = this.unitNo;
        }
        let workNoAppLists = this.taskAllData.taskList.map(item => {
          return { label: item.project_name, value: item.project_id };
        });
        var result = {};
        var workNoAppListsTmp = [];
        for (var i = 0; i < workNoAppLists.length; i++) {
          if (
            !result[workNoAppLists[i].value] &&
            workNoAppLists[i].value != null &&
            !result[workNoAppLists[i].label] &&
            workNoAppLists[i].label != null
          ) {
            workNoAppListsTmp.push(workNoAppLists[i]);
            result[workNoAppLists[i].value] = true;
          }
        }
        this.workNoAppLists = workNoAppListsTmp;
        let app_name = workNoAppListsTmp.map(item => {
          return item.label;
        });
        const set = new Set(app_name);
        let appNameArray = Array.from(set);
        if (appNameArray.length === 0) {
          appNameArray = '';
        }
        this.appNameArrayTmp = appNameArray;
        this.mantisDialogAdd.app_name = appNameArray;
        this.mantisDialogAdd.handler = this.taskAllData.unitLeaderEn;
        this.mantisDialogAdd.handlerCn = this.taskAllData.unitLeaderCn;
        this.handlerTmp = this.taskAllData.unitLeaderEn;
        this.handlerCnTmp = this.taskAllData.unitLeaderCn;
        this.taskOptions = this.taskAllData.taskList.map(item => {
          return { label: item.name, value: item.id };
        });
        // 查询开发人责任人
        let userList = await getUserListByRole();
        this.developerList = userList.map(item => {
          return { value: item.user_name_en, label: item.user_name_cn };
        });
        // 查询分派给
        this.handlerList = userList;
        // 查询系统名称/功能模块,回显
        await this.QueryDetailByTestcaseNo({
          testcaseNo: caseExecuteModel.testcaseNo
        });
        this.mantisDialogAdd.system_name = this.caseDetail.systemName
          ? this.caseDetail.systemName
          : '';
        this.mantisDialogAdd.function_module = this.caseDetail.testcaseFuncName
          ? this.caseDetail.testcaseFuncName
          : '';
        this.mantisDialogAdd.workNo = this.workNo;
        await this.queryAppByWorkNo({ workNo: this.workNo });
        this.mantisDialog = true;
      }
    },
    // 获取分派给handler选择框的值
    getHandlerCn(val) {
      var obj = {};
      obj = this.handlerList.find(item => {
        return item.user_name_en === val;
      });
      this.mantisDialogAdd.handlerCn = obj.user_name_cn;
    },

    handleCloseMantis() {
      this.mantisDialog = false;
    },
    handleCloseMantisDialog() {
      this.mantisListDialog = false;
    },

    // mantis编辑弹框 提交
    async submitMantisAdd(mantisDialogAdd) {
      this.fileTypeArray = [];
      this.judgeFileType = true;
      this.fileList.forEach(item => {
        this.fileTypeArray.push(item.raw.type);
      });
      let typeList = ['text/plain', 'image/jpeg', 'image/jpg', 'image/png'];
      for (var i = 0; i < this.fileTypeArray.length; i++) {
        if (typeList.indexOf(this.fileTypeArray[i]) == -1) {
          this.judgeFileType = false;
        }
      }
      let res = [];
      if (this.fileList.length !== 0) {
        res = await this.getFile();
      }
      if (this.judgeFileType) {
        this.$refs[mantisDialogAdd].validate(async valid => {
          if (valid) {
            this.loadingBtn = true;
            let i = '';
            let len = '';
            let developerName;
            for (i = 0, len = this.developerList.length; i < len; i++) {
              if (
                this.mantisDialogAdd.developer == this.developerList[i].value
              ) {
                developerName = this.developerList[i].label;
              }
            }

            await this.add({
              taskNo: this.mantisDialogAdd.taskNo,
              project: this.mantisDialogAdd.project,
              system_name: this.mantisDialogAdd.system_name,
              function_module: this.mantisDialogAdd.function_module,
              redmine_id: this.mantisDialogAdd.redmine_id,
              workNo: this.mantisDialogAdd.workNo,
              system_version: this.mantisDialogAdd.system_version,
              stage: this.mantisDialogAdd.stage,
              developer: this.mantisDialogAdd.developer,
              developerCh: developerName,
              handler: this.mantisDialogAdd.handler,
              handlerCh: this.mantisDialogAdd.handlerCn,
              priority: this.mantisDialogAdd.priority,
              severity: this.mantisDialogAdd.severity,
              flaw_source: this.mantisDialogAdd.flaw_source,
              plan_fix_date: this.mantisDialogAdd.plan_fix_date,
              flaw_type: this.mantisDialogAdd.flaw_type,
              summary: this.mantisDialogAdd.summary,
              description: this.mantisDialogAdd.description,
              reason: this.mantisDialogAdd.reason
                ? this.mantisDialogAdd.reason
                : '',
              files: res,
              mantis_token: this.userMantisToken,
              planlist_testcase_id: this.getId,
              copy_to_list: this.mantisDialogAdd.copy_to_list
                ? this.mantisDialogAdd.copy_to_list
                : [],
              appName_en: this.mantisDialogAdd.app_name
            }).catch(err => {
              this.loadingBtn = false;
              throw err;
            });
            this.$message({
              showClose: true,
              message: '交易执行成功',
              type: 'success'
            });
            this.mantisDialog = false;
            this.$refs[mantisDialogAdd].resetFields();
            this.mantisDialogAdd = createMantisModel();
            this.mantisDialogAddDisplay();
            this.$refs.upload.clearFiles();
            this.loadingBtn = false;
          } else {
            return false;
          }
        });
      } else {
        this.$message({
          showClose: true,
          type: 'error',
          message: '文件上传失败！只能上传txt/jpg/png文件!'
        });
      }
    },
    closeDialogDetail() {
      this.mantisDetailDialog = false;
    },
    cancelMantisAdd(mantisDialogAdd) {
      this.$refs[mantisDialogAdd].resetFields();
      this.mantisDialog = false;
      this.mantisDialogAdd = createMantisModel();
      this.mantisDialogAddDisplay();
      this.$refs.upload.clearFiles();
    },

    //成功 通过 阻塞 失败按钮
    async executeCase(caseExecuteModel, testcaseExecuteResult) {
      await this.updatePlanlistTestcaseRelation({
        testcaseNo: caseExecuteModel.testcaseNo,
        planlistTestcaseId: this.getId,
        testcaseExecuteResult: testcaseExecuteResult,
        remark: caseExecuteModel.remark
      });
      await this.initTestCase(
        this.overallPlanId,
        this.pageSize,
        this.currentPage
      );
      await this.initqueryPlanStatus(this.getPlanId);
      await this.$message({
        showClose: true,
        message: '案例执行成功',
        type: 'success'
      });
      this.indexCase = testcaseExecuteResult;
      // 自动跳到下一条案例  失败不跳到下一条
      if (testcaseExecuteResult !== 3) {
        await this.nextExecute(this.testcaseData);
      }
    },

    //文件上传相关操作
    beforeRemove(file, fileList) {
      return this.$confirm(`确定移除 ${file.name}？`);
    },
    handleRemove(file, fileList) {
      this.fileList = fileList;
    },
    getBase64(file) {
      //把图片转成base64编码
      return new Promise(function(resolve, reject) {
        let reader = new FileReader();
        let imgResult = '';
        reader.readAsDataURL(file);
        reader.onload = function() {
          imgResult = reader.result;
        };
        reader.onerror = function(error) {
          reject(error);
        };
        reader.onloadend = function() {
          resolve(imgResult);
        };
      });
    },
    getFile() {
      return new Promise((resolve, reject) => {
        this.getFileData = [];
        this.fileList.forEach(async (file, index) => {
          const res = await this.getBase64(file.raw);
          this.getFileData.push({ name: file.name, content: res });
          if (index === this.fileList.length - 1) {
            resolve(this.getFileData);
          }
        });
      });
    },
    test(file, files) {
      this.fileList = files;
      if (file.raw.type.length === 0) {
        alert('文件类型不合法，请移除该文件');
        return false;
      }
    },

    // 案例导入
    async importTestcase(file) {
      let param = new FormData();
      param.append('planId', this.getPlanId ? this.getPlanId : '');
      param.append('workNo', `${this.PlanworkNo}`);
      param.append('file', file.file);
      if (file.file.size > 10000) {
        await this.batchAdd(param)
          .then(res => {
            this.$message({
              showClose: true,
              message: '案例导入成功',
              type: 'success'
            });
            this.queryTaskCount(this.overallPlanId);
            this.initTestCase(
              this.overallPlanId,
              this.pageSize,
              this.currentPage
            );
            this.initqueryPlanStatus(this.getPlanId);
          })
          .catch(err => {
            this.batchErrorMsg = sessionStorage.getItem('batchErrorMsg');
            if (this.batchErrorMsg !== ' ') {
              const fileContent = this.batchErrorMsg; //文件内容
              const type = 'text/plain; charset=us-ascii';
              const fileName = '详细错误信息.txt';
              let blob = new Blob([fileContent], { type: type }); // text格式,直接转blob

              const res = {
                data: blob
              };
              exportExcel(res, fileName, type);
              sessionStorage.setItem('batchErrorMsg', ' ');
            }
            sessionStorage.setItem('batchErrorMsg', ' ');
          });
      } else {
        this.$message({
          showClose: true,
          message: '该文件内容未按模板填写，请下载模板填写',
          type: 'warning'
        });
      }
    },

    saveFile(file) {
      this.file = file;
    },
    //状态转译
    formatterStatus(row, column) {
      if (row.status === '10') {
        return '新建';
      } else if (row.status === '20') {
        return '拒绝';
      } else if (row.status === '30') {
        return '确认拒绝';
      } else if (row.status === '40') {
        return '延迟修复';
      } else if (row.status === '50') {
        return '打开';
      } else if (row.status === '80') {
        return '已修复';
      } else if (row.status === '90') {
        return '关闭';
      } else {
        return '';
      }
    },
    // 编辑缺陷页面提交mantis弹框 反显阶段，版本，优先级，日期
    mantisDialogAddDisplay() {
      this.mantisDialogAdd.stage = '集成阶段';
      this.mantisDialogAdd.system_version = '内测版本';
      this.mantisDialogAdd.priority = '中';
      this.mantisDialogAdd.plan_fix_date = moment().format('YYYY-MM-DD');
    },
    // 案例行拖拽排序
    rowDrop() {
      const tbody = document.querySelector(
        '.sort .el-table__body-wrapper tbody'
      );
      const _this = this;
      Sortable.create(tbody, {
        onEnd({ newIndex, oldIndex }) {
          const currRow = _this.testcaseSortData.splice(oldIndex, 1)[0];
          _this.testcaseSortData.splice(newIndex, 0, currRow);
          let testcaseSortArr = [];
          let order = 0;
          let testNo = '';
          _this.testcaseSortData.forEach(item => {
            testNo = item.testcaseNo;
            testcaseSortArr.push({
              [testNo]: order
            });
            order++;
          });
          _this.allSortCase = testcaseSortArr;
        }
      });
    },
    async startSort() {
      this.notShowCaseSort = false;
      // 开始排序，查该计划下的所有案例
      await this.QueryTestcaseByPlanId({
        planId: this.overallPlanId,
        pageSize: 0,
        currentPage: 1
      });
      this.testcaseSortData = this.testCaseData;
    },
    async endSort() {
      // 判断用户是否拖拽排序了
      if (this.allSortCase.length > 1) {
        await this.testCaseCustomSort({
          planId: this.overallPlanId,
          allCase: this.allSortCase
        });
        await this.initTestCase(
          this.overallPlanId,
          this.pageSize,
          this.currentPage
        );
      }
      this.notShowCaseSort = true;
    }
  },
  async created() {
    (this.mantisPlanIdTestcaseId = this.$route.query.planlist_testcase_id
      ? this.$route.query.planlist_testcase_id
      : ''),
      (this.routerPlanId = Number(this.$route.query.planId));
    await this.queryAllPeople();
    this.allPeopleOption = this.allPeoples.filter(item => {
      return item.email.length > 1;
    });
  },
  //初始化构造方法
  async mounted() {
    this.rowDrop(); // 案例行拖拽
    this.mantisDialogAddDisplay();
    // 工单查询页面跳转过来-权限判断
    this.isAuthUser = this.$route.query.isAuthUser || false;
    this.isHistory = this.$route.query.isHistory || false;
    this.isWaste = this.$route.query.isWaste || false;
    this.isSelf = this.$route.query.isSelf || false;
    sessionStorage.setItem('batchErrorMsg', ' ');
    //从session中获取工单编号、测试人员、任务名称
    const workOrderNoDataSession = JSON.parse(
      sessionStorage.getItem('planWorkOrderNo')
    );
    // 工单编号
    this.PlanworkNo = this.$route.query.workOrderNo
      ? this.$route.query.workOrderNo
      : workOrderNoDataSession.workNo;
    // 存储工单编号供批量复用
    sessionStorage.setItem('repeatWorkNo', this.PlanworkNo);
    // 从缺陷页面跳转过来 发queryOrderByNo 获取主任务名，测试人员，归属阶段，实施单元编号
    await this.queryTaskNameTestersByNo({
      workNo: this.PlanworkNo
    });
    const workOrderNoData = this.workOrderNoData;
    //任务名称
    this.mainTaskName = workOrderNoData.workOrder.mainTaskName;
    //测试人员
    this.testers = workOrderNoData.testers;
    this.stage = workOrderNoData.workOrder.stage;
    this.showTask = workOrderNoData.workOrder.workOrderFlag;
    //权限判断
    if (this.routerPlanId) {
      // 从缺陷页面跳转过来  planPower就设为true
      this.planPower = true;
    } else {
      this.planPower = workOrderNoDataSession.planPower;
    }
    //获取当前工单下计划
    await this.initqueryByworkNo(this.PlanworkNo);
    if (this.routerPlanId) {
      this.overallPlanId = this.routerPlanId; // 路由跳转的计划id赋值给全局id
      await this.initTestCase(
        this.routerPlanId,
        this.pageSize,
        this.currentPage
      );
      await this.initqueryPlanStatus(this.routerPlanId);
      await this.queryTaskCount(this.routerPlanId);
    }
    const index = this.planList.findIndex(
      item => item.planId == this.routerPlanId
    );
    this.active = index == -1 ? 0 : index;

    //  拿实施单元编号，工单号
    if (this.routerPlanId) {
      this.unitNo = workOrderNoData.workOrder.unit;
      this.workNo = this.$route.query.workOrderNo;
    } else {
      this.unitNo = workOrderNoDataSession.unitNo;
      this.workNo = workOrderNoDataSession.workNo;
    }
    // 废弃工单跳到执行计划页面
    if (this.isWaste) {
      await this.queryUserValidOrder({
        userRole: sessionStorage.getItem('Trole'),
        orderType: this.$route.query.orderType
      });
      this.targetWorkNoOptions = this.targetWork;
    }
  }
};
</script>
<style scoped>
/* 样式显示-版本问题 */
.tooltip-case {
  margin-top: 10px;
  text-align: center;
  color: red;
}
.testcase-style >>> span {
  max-width: 100%;
}
.testcase-style >>> .el-tag {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.aside {
  padding: 20px;
}

.title {
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 10px;
  text-align: left;
  padding-top: 4px;
  font-size: 22px;
}

.dialogFunction {
  width: 110%;
}

.dialogFunction >>> .el-textarea__inner {
  font-family: 'Helvetica Neue', 'Helvetica', 'PingFang SC';
}

.detailsDialogFunction {
  width: 110%;
}
.detailsDialogFunction >>> .el-textarea__inner {
  font-family: 'Helvetica Neue', 'Helvetica', 'PingFang SC';
  color: #000 !important;
}
.el-date-editor.el-input {
  width: 250px;
}

.state {
  margin-left: 5px;
  border-right: 2px solid #999;
  height: 30px;
  display: inline-block;
  vertical-align: bottom;
  padding-top: 10px;
  padding-right: 5px;
}

.el-card >>> .el-card__body {
  padding: 20px 20px 0 20px;
}

.card-body {
  padding: 0 20px 20px 20px;
}

.card-footer {
  padding: 16px 20px;
}

.fontSize {
  font-size: 15px;
}

.card-container {
  margin-bottom: 20px;
}

.m-tb-10 {
  margin: 10px 0;
}

.state-container {
  text-align: right;
  margin-top: 30px;
}

.icon {
  margin: 10px;
}

.tabel-icon {
  margin: 0 2px;
  font-size: 20px;
}

.main-body {
  margin-top: 20px;
}

.small_dialog >>> .el-dialog__title {
  color: white;
  font-size: 18px;
  font-weight: 500;
}

.small_dialog >>> .el-dialog__header {
  background: #409eff;
}

.small_dialog >>> .el-icon-close:before {
  color: white;
  font-size: 20px;
  font-weight: 600;
}
.email_dialog >>> .el-dialog {
  width: 400px;
}
.email_dialog >>> .el-dialog__title {
  color: white;
  font-size: 18px;
  font-weight: 500;
}
.email_dialog >>> .el-dialog__header {
  background: #409eff;
  padding: 15px 20px 10px;
}
.email_dialog >>> .el-icon-close:before {
  color: white;
  font-size: 20px;
  font-weight: 600;
}
.email_dialog >>> .el-form-item__label {
  width: 150px;
}
.abow_dialog >>> .el-dialog__title {
  color: white;
  font-size: 18px;
  font-weight: 500;
}

.abow_dialog >>> .el-dialog__header {
  background: #409eff;
  padding: 15px 20px 10px;
}

.abow_dialog >>> .el-icon-close:before {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.abow_dialog {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.abow_dialog >>> .el-dialog {
  margin: 0 auto !important;
  height: 95%;
  overflow: hidden;
}

.abow_dialog >>> .el-dialog__body {
  position: absolute;
  left: 10px;
  top: 60px;
  bottom: 60px;
  right: 0;
  padding: 0;
  z-index: 1;
  overflow: hidden;
  overflow-y: auto;
}

.abow_dialog >>> .el-dialog__footer {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
}

.batchCopy_dialog >>> .el-dialog__title {
  color: white;
  font-size: 20px;
  font-weight: 500;
}

.batchCopy_dialog >>> .el-icon-close:before {
  color: white;
  font-size: 20px;
  font-weight: 600;
}

.batchCopy_dialog >>> .el-dialog__header {
  background: #409eff;
}

.batchCopy_dialog >>> .el-row {
  margin-top: 30px;
}

.inputWidth {
  width: 380%;
}

.inputWidth >>> .el-textarea__inner {
  font-family: 'Helvetica Neue', 'Helvetica', 'PingFang SC';
}

.detailsDialog >>> .el-input__inner {
  color: #000 !important;
}

.detailsDialogInput {
  width: 380%;
}

.detailsDialogInput >>> .el-textarea__inner {
  font-family: 'Helvetica Neue', 'Helvetica', 'PingFang SC';
  color: #000 !important;
}

.buttonActive {
  background: #409eff !important;
  color: #fff !important;
}

.pagination {
  margin-top: 3%;
  margin-bottom: 3%;
}

.pagination >>> .el-pagination {
  float: right !important;
}

.meunTree {
  position: absolute;
  width: 50%;
  overflow: auto;
}
.add {
  height: 50%;
}
.edit {
  height: 40%;
}
.executeBtn {
  margin-left: 15px;
}
.filesEditCss >>> .el-icon-close:before {
  color: gray;
  font-size: 16px;
  font-weight: 500;
}

.resultSuccess {
  color: green;
  font-size: 16px;
  font-weight: 600;
}
.resultBlock {
  color: #e6a23c;
  font-size: 16px;
  font-weight: 600;
}
.resultFail {
  color: red;
  font-size: 16px;
  font-weight: 600;
}
.resultLoseEfficacy {
  color: #b3b4b5;
  font-size: 16px;
  font-weight: 600;
}

.executeCountSum {
  border: 0px;
  font-size: 13px;
}
.executeCount {
  border-top: 0px;
  border-right: 0px;
  border-bottom: 0px;
  font-size: 13px;
}
.exportBtnGroup {
  margin-left: 20px;
}
.importTestcaseBtn {
  display: inline-block;
}
.page-container {
  padding: 0 10px;
}
.sort-style {
  margin-left: 5px;
}
.sort-style >>> .el-button--mini {
  padding: 7px 10px;
}
.pd-4 {
  padding-top: 4px;
}
.sortTable-style {
  margin-top: 32px;
}
.migration-style {
  height: auto;
  color: white;
}
.migration-style >>> .el-dialog__header {
  background: #409eff;
}
.migration-style >>> .el-dialog__title {
  color: white;
  font-size: 20px;
  font-weight: 500;
}

.migration-style >>> .el-icon-close:before {
  color: white;
  font-size: 20px;
  font-weight: 600;
}

.form-margin {
  margin-top: 15px;
}
.PlanworkNo {
  padding-top: 10px;
}
.mar-20 {
  margin-right: 20px;
}
.mar-35 {
  margin-right: 35px;
}
.mal-10 {
  margin-left: 10px;
}
.add-tree {
  height: 90%;
}
.batch_btn_group {
  text-align: center;
}
.batch_btn {
  text-align: center;
  margin-top: 30px;
}
.exePlan {
  margin-bottom: 10px;
}
</style>
