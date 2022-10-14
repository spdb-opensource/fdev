<template>
  <div class="page-container">
    <orderMenuTab />
    <el-form :inline="true" :model="formInline" class="demo-form-inline">
      <el-form-item>
        <el-input
          v-model="formInline.taskName"
          class="input-style input-order-name"
          placeholder="需求名称/需求编号/实施单元/任务名/工单名"
          clearable
          @keyup.enter.native="onSubmit()"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-select v-model="formInline.orderType" placeholder="工单类型">
          <el-option
            v-for="item in orderTypeList"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-select
          v-model="formInline.userEnName"
          class="input-style1"
          filterable
          placeholder="请选择人员名称"
          clearable
        >
          <el-option
            v-for="item in selectNames"
            :key="item.index"
            :value="item.user_name_en"
            :label="item.user_name_cn"
          >
            <span style="float: left">{{ item.user_name_cn }}</span>
            <span style="float: right">{{ item.user_name_en }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-select
          v-model="formInline.groupId"
          class="input-style1"
          filterable
          placeholder="请选择所属小组"
          clearable
        >
          <el-option
            v-for="(item, index) in groupNameList"
            :key="index"
            :label="item.fullName"
            :value="item.id"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit()">查询</el-button>
      </el-form-item>
    </el-form>

    <div class="card-container">
      <div class="title">
        工单查询
      </div>
      <el-row :gutter="20">
        <el-col
          style="height: 280px"
          :span="6"
          v-for="(item, index) in queryList"
          :key="index"
        >
          <el-card class="box-card" :body-style="{ padding: '12px 0 0' }">
            <div>
              <el-row class="text-grey-2 marginbt8">
                <el-col :span="19">
                  <!-- stage值以后不会取5，7 -->
                  <div class="headerStyle">
                    <div
                      :class="'chip' + item.orderType"
                      style="text-align: center;"
                    >
                      <div class="textStyle">
                        {{ orderTypeText(item.orderType) }}
                      </div>
                    </div>
                    <!-- 状态（加小圆点） -->
                    <div class="headerStyle buttonMr10">
                      <div
                        :class="'point-' + orderStageOther(item.stage).type"
                        class="pointMl16"
                      ></div>
                      <span
                        class="fontsize-12 weight pointMl4"
                        :class="'text-' + orderStageOther(item.stage).type"
                      >
                        {{ orderStageOther(item.stage).label }}
                      </span>
                    </div>
                  </div>
                </el-col>
                <el-col :span="5">
                  <el-button
                    size="mini"
                    type="primary"
                    v-if="item.mantisFlag != null"
                    @click.stop="goMantisList(item)"
                  >
                    缺陷
                  </el-button>
                </el-col>
              </el-row>
              <div class="divider"></div>
              <div class="ellipsis paddingWay">
                <el-tooltip
                  effect="dark"
                  placement="top-start"
                  :visible-arrow="false"
                >
                  <div slot="content" class="elTooltip">
                    {{ item.mainTaskName }}
                  </div>
                  <el-link
                    :underline="false"
                    type="primary"
                    class="fontweight"
                    @click.stop="queryDetail(item)"
                  >
                    {{ item.mainTaskName }}
                  </el-link>
                </el-tooltip>
              </div>
              <div class="padding-10">
                <el-row class="text-grey-2 fontsize-14">
                  <el-col :span="9">需求名称：</el-col>
                  <el-tooltip
                    effect="dark"
                    :content="item.demandName"
                    placement="top-start"
                  >
                    <el-col :span="12" class="cardEllipsis">{{
                      item.demandName
                    }}</el-col>
                  </el-tooltip>
                </el-row>
                <el-row class="text-grey-2 fontsize-14">
                  <el-col :span="9">工单负责人：</el-col>
                  <el-col :span="12" v-if="item.workManager">{{
                    item.workManager.user_name_cn
                  }}</el-col>
                </el-row>
                <el-row class="text-grey-2 fontsize-14">
                  <el-col :span="9">工单组长：</el-col>
                  <el-tooltip
                    effect="dark"
                    :content="getContent(item.groupLeader)"
                    placement="top-start"
                  >
                    <el-col :span="12" class="cardEllipsis">
                      <span
                        v-for="(user, index) in item.groupLeader"
                        :key="index"
                      >
                        <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
                      </span>
                    </el-col>
                  </el-tooltip>
                </el-row>
                <el-row class="text-grey-2 fontsize-14">
                  <el-col :span="9">测试人员：</el-col>
                  <el-tooltip
                    effect="dark"
                    :content="getContent(item.testers)"
                    placement="top-start"
                  >
                    <el-col :span="12" class="cardEllipsis">
                      <span v-for="(user, index) in item.testers" :key="index">
                        <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
                      </span>
                    </el-col>
                  </el-tooltip>
                </el-row>
              </div>
            </div>
            <div class="divider"></div>
            <el-row class="card-footer">
              <el-col :span="6" class="text-left margin-top-5">
                <el-tag size="mini" v-if="item.sitFlag == '1'" type="success"
                  >已提内测</el-tag
                >
                <el-tag size="mini" v-if="item.sitFlag == '2'" type="success"
                  >内测完成</el-tag
                >
              </el-col>
              <el-col
                :span="18"
                class="text-right height-32"
                v-if="item.stage === '0'"
              >
                <el-button
                  size="small"
                  type="primary"
                  @click.stop="openTaskList(item)"
                  v-if="item.workOrderFlag != '0'"
                  >任务列表
                  <span
                    v-if="item.hasTaskFlag === '1'"
                    class="icon special_icon"
                  >
                    ★
                  </span>
                </el-button>
              </el-col>
              <el-col
                :span="18"
                class="text-right height-32"
                v-if="
                  item.stage == '1' ||
                    item.stage == '2' ||
                    item.stage == '3' ||
                    item.stage == '6' ||
                    item.stage == '8' ||
                    item.stage == '9' ||
                    item.stage == '10' ||
                    item.stage == '11' ||
                    item.stage == '12' ||
                    item.stage == '13'
                "
              >
                <el-button-group>
                  <el-button
                    type="primary"
                    size="small"
                    @click.stop="exePlan(item)"
                  >
                    执行计划
                    <span v-if="item.hasCaseFlag === '1'" class="icon">
                      ★
                    </span>
                  </el-button>
                  <el-dropdown trigger="click" v-if="item.workOrderFlag != '0'">
                    <el-button size="small" type="primary"
                      >工单管理
                      <i class="el-icon-arrow-down"></i>
                      <span v-if="item.hasTaskFlag === '1'" class="icon">
                        ★
                      </span>
                    </el-button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item
                        v-if="item.workOrderFlag != '0'"
                        class="clearfix"
                      >
                        <span @click.stop="openTaskList(item)">任务列表 </span>
                      </el-dropdown-item>

                      <el-dropdown-item class="clearfix">
                        <span @click.stop="workOrderSplit(item)"
                          >工单拆分
                        </span>
                      </el-dropdown-item>

                      <el-dropdown-item class="clearfix">
                        <span @click.stop="workOrderConcat(item)"
                          >工单合并
                        </span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </el-button-group>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>
      <el-row class="el-row1 page pagination">
        <el-col :offset="17" :span="7">
          <el-pagination
            :hide-on-single-page="true"
            background
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-size="pageSize"
            layout="prev, pager, next"
            :total="total"
          >
          </el-pagination>
        </el-col>
      </el-row>
    </div>

    <!-- 工单合并弹窗 -->
    <Dialog title="工单合并" :visible.sync="dialogConcat">
      <el-form
        label-position="left"
        label-width="50%"
        :model="workOrderConcatModel"
        :rules="workOrderConcatModelRules"
        ref="workOrderConcatModel"
      >
        <el-form-item label="新工单名" prop="concatTaskName">
          <el-input
            v-model="workOrderConcatModel.concatTaskName"
            class="input-style"
            placeholder="请输入新工单名"
            clearable
          >
          </el-input>
        </el-form-item>
        <el-form-item label="合并工单" prop="worksNo">
          <el-select
            @focus="checkTaskOptionsConcat"
            filterable
            :remote-method="remoteMethod"
            :loading="loading"
            remote
            multiple
            class="select-width"
            v-model="workOrderConcatModel.worksNo"
            placeholder="请选择待合并工单"
          >
            <el-option
              v-for="(item, index) in taskOptionsConcat"
              :key="index"
              :value="item.workOrderNo"
              :label="
                item.mainTaskName.length > 10
                  ? item.mainTaskName.substring(0, 10) + '...'
                  : item.mainTaskName
              "
            >
              <span style="float: left">{{ item.mainTaskName }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <div
          v-if="
            taskOptionsConcat.length > 0 &&
              workOrderConcatModel.worksNo.length > 0
          "
        >
          选中的工单所含的任务：
          <br />
          <div v-for="(item, index) in showTaskName" :key="index">
            {{ item }}
          </div>
        </div>
        <p v-if="taskOptionsConcat.length > 0">
          温馨提示：被选中的工单将同本工单组合成新工单
        </p>
      </el-form>

      <el-button
        type="primary"
        slot="footer"
        class="full-width"
        :loading="loadingBtn"
        @click="concatOrder('workOrderConcatModel')"
      >
        确定
      </el-button>
    </Dialog>

    <!-- 工单详情 -->
    <el-dialog
      title="工 单 详 情"
      :visible.sync="queryDetailOrderDialogShow"
      width="45%"
      class="abow_dialog"
      center
    >
      <el-form
        :model="queryDetailOrder"
        label-width="40%"
        size="mini"
        v-loading="showLoading"
      >
        <el-form-item label="工单名称：" class="text-grey-1">
          <el-col :span="22">{{ queryDetailOrder.mainTaskName }}</el-col>
        </el-form-item>
        <el-form-item label="工单编号：" class="text-grey-1">
          <el-col :span="22">{{ queryDetailOrder.workOrderNo }}</el-col>
        </el-form-item>
        <el-form-item label="需求名称：" class="text-grey-1">
          <el-col :span="22">{{ queryDetailOrder.demandName }}</el-col>
        </el-form-item>
        <el-form-item label="需求编号：" class="text-grey-1">
          <el-col :span="22">{{ queryDetailOrder.demandNo }}</el-col>
        </el-form-item>
        <el-form-item label="实施单元：" class="text-grey-1">
          <el-col :span="22">{{ queryDetailOrder.unit }}</el-col>
        </el-form-item>
        <el-form-item label="所属小组 ：" class="text-grey-1">
          <el-col :span="22">{{ queryDetailOrder.groupName }}</el-col>
        </el-form-item>
        <el-form-item label="工单负责人：">
          <el-col :span="22" v-if="queryDetailOrder.workManager">{{
            queryDetailOrder.workManager.user_name_cn
          }}</el-col>
        </el-form-item>
        <el-form-item label="测试小组长：">
          <span
            v-for="(user, index) in queryDetailOrder.groupLeader"
            :key="index"
          >
            <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
          </span>
        </el-form-item>
        <el-form-item label="测试人员：">
          <span
            :span="22"
            v-for="(user, index) in queryDetailOrder.testers"
            :key="index"
          >
            <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
          </span>
        </el-form-item>
        <el-form-item label="计划SIT开始时间：">
          <el-col :span="22">{{ queryDetailOrder.planSitDate }}</el-col>
        </el-form-item>
        <el-form-item label="计划UAT开始时间：">
          <el-col :span="22">{{ queryDetailOrder.planUatDate }}</el-col>
        </el-form-item>
        <el-form-item label="计划投产开始时间：">
          <el-col :span="22">{{
            formatDate(queryDetailOrder.planProDate)
          }}</el-col>
        </el-form-item>
        <el-form-item
          label="内部测试完成时间："
          v-if="
            queryDetailOrder.stage === '3' ||
              queryDetailOrder.stage === '6' ||
              queryDetailOrder.stage === '9' ||
              queryDetailOrder.stage === '10'
          "
        >
          <el-col :span="22">{{ queryDetailOrder.uatSubmitDate }}</el-col>
        </el-form-item>
        <el-form-item
          label="风险描述 ："
          v-if="
            queryDetailOrder.stage === '6' ||
              queryDetailOrder.stage === '10' ||
              queryDetailOrder.stage === '12' ||
              queryDetailOrder.stage === '13'
          "
        >
          <el-col :span="22">{{ queryDetailOrder.riskDescription }}</el-col>
        </el-form-item>
        <el-form-item label="备注：" class="text-grey-1">
          <el-col :span="22">{{ queryDetailOrder.remark }}</el-col>
        </el-form-item>
        <el-form-item label="测试阶段：" class="text-grey-1">
          <el-col :span="22" v-if="queryDetailOrder.stage == '0'"
            >待分配</el-col
          >
          <el-col :span="22" v-if="queryDetailOrder.stage == '1'"
            >开发中</el-col
          >
          <el-col :span="22" v-if="queryDetailOrder.stage == '2'">SIT</el-col>
          <el-col :span="22" v-if="queryDetailOrder.stage == '3'">UAT</el-col>
          <el-col :span="22" v-if="queryDetailOrder.stage == '4'"
            >已投产</el-col
          >
          <el-col :span="22" v-if="queryDetailOrder.stage == '6'"
            >UAT(含风险)</el-col
          >
          <el-col :span="22" v-if="queryDetailOrder.stage == '8'">撤销</el-col>
          <el-col :span="22" v-if="queryDetailOrder.stage == '9'"
            >分包测试（内测完成）</el-col
          >
          <el-col :span="22" v-if="queryDetailOrder.stage == '10'"
            >分包测试（含风险）</el-col
          >
          <el-col :span="22" v-if="queryDetailOrder.stage == '12'"
            >安全测试(内测完成)</el-col
          >
          <el-col :span="22" v-if="queryDetailOrder.stage == '13'"
            >安全测试（含风险）</el-col
          >
        </el-form-item>
        <el-form-item label="任务关联文档：">
          <el-tree
            :default-expand-all="true"
            class="tree tree-style"
            :data="docData"
            ref="tree"
            highlight-current
            :props="props"
            @node-click="handNodeClick"
            :filter-node-method="filterNode"
          >
            <div slot-scope="{ node, data }" class="ellipsis">
              <el-tooltip
                effect="dark"
                :content="node.label"
                placement="top-start"
                class="tooltip"
                v-if="data.path"
              >
                <el-link :underline="false" type="primary">
                  <span>{{ node.label }}</span>
                </el-link>
              </el-tooltip>
              <span v-else>{{ node.label }}</span>
            </div>
          </el-tree>
        </el-form-item>
        <el-form-item label="需求关联文档：">
          <el-tree
            :default-expand-all="true"
            class="tree tree-style"
            :data="docDataRqr"
            ref="tree"
            highlight-current
            :props="props"
            @node-click="downloadRqrDoc"
            :filter-node-method="filterNode"
          >
            <div slot-scope="{ node, data }" class="ellipsis">
              <el-tooltip
                effect="dark"
                :content="node.label"
                placement="top-start"
                class="tooltip"
                v-if="data.id"
              >
                <el-link :underline="false" type="primary">
                  <span>{{ node.label }}</span>
                </el-link>
              </el-tooltip>
              <span v-else>{{ node.label }}</span>
            </div>
          </el-tree>
        </el-form-item>
      </el-form>
      <div
        slot="footer"
        class="dialog-footer"
        v-if="
          userRole >= 30 &&
            (queryDetailOrder.stage == '0' ||
              queryDetailOrder.stage == '1' ||
              queryDetailOrder.stage == '2' ||
              queryDetailOrder.stage == '3' ||
              queryDetailOrder.stage == '6' ||
              queryDetailOrder.stage == '8' ||
              queryDetailOrder.stage == '9' ||
              queryDetailOrder.stage == '10' ||
              queryDetailOrder.stage == '12' ||
              queryDetailOrder.stage == '13')
        "
      >
        <el-button type="primary" @click="updateBegin" class="modifyBtn"
          >修 改</el-button
        >
      </div>
      <div slot="footer" class="dialog-footer" v-if="userRole < 30">
        <el-button type="primary" @click="closeDetailDia">确 定</el-button>
      </div>
      <div
        slot="footer"
        class="dialog-footer"
        v-if="
          userRole >= 30 &&
            (queryDetailOrder.stage == '4' || queryDetailOrder.stage == '11')
        "
      >
        <el-button type="primary" @click="closeDetailDia">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 工单修改 -->
    <el-dialog
      title="工 单 修 改"
      :visible.sync="updateOrderDialogShow"
      :before-close="closeUpdateDia"
      class="abow_dialog"
      width="45%"
    >
      <el-form
        :model="updateOrderModel"
        :rules="uatSubmitDateRule ? updateRules1 : updateRules"
        ref="updateOrderModel"
        label-width="30%"
        size="mini"
      >
        <el-form-item
          label="工单名称 ："
          class="text-grey-1"
          prop="mainTaskName"
        >
          <el-col :span="20">{{ updateOrderModel.mainTaskName }}</el-col>
        </el-form-item>
        <el-form-item
          label="需求名称 ："
          class="text-grey-1"
          prop="demandNo"
          label-width="190px"
        >
          <el-input
            class="el-select-temp"
            placeholder="需求名称 "
            v-model="updateOrderModel.demandName"
            disabled
          />
        </el-form-item>
        <el-form-item
          label="需求编号 ："
          class="text-grey-1"
          prop="demandNo"
          label-width="190px"
        >
          <el-input
            class="el-select-temp"
            placeholder="需求编号 "
            v-model="updateOrderModel.demandNo"
            disabled
          />
        </el-form-item>
        <el-form-item
          label="工单编号 ："
          class="text-grey-1"
          prop="workOrderNo"
        >
          <el-col :span="20">{{ updateOrderModel.workOrderNo }}</el-col>
        </el-form-item>
        <!-- 所属小组修改权限:工单负责人或者超级管理员 -->
        <el-form-item label="所属小组 ：" class="text-grey-1" prop="groupName">
          <el-col
            :span="20"
            v-if="
              userRole < 50 &&
                userManager[0].user_en_name !==
                  queryDetailOrder.workManager.user_name_en
            "
            >{{ updateOrderModel.groupName }}</el-col
          >
          <el-select
            v-if="
              userRole >= 50 ||
                userManager[0].user_en_name ===
                  queryDetailOrder.workManager.user_name_en
            "
            v-model="updateOrderModel.fdevGroupId"
            filterable
            placeholder="请选择所属小组"
            class="el-select-temp"
          >
            <el-option
              v-for="(item, index) in groupNameList"
              :key="index"
              :label="item.name"
              :value="item.id"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="工单负责人 ：" prop="workManager">
          <el-col
            :span="20"
            v-if="userRole < 40 && updateOrderModel.workManager"
            >{{ updateOrderModel.workManager.user_name_cn }}</el-col
          >
          <el-select
            v-if="userRole >= 40"
            v-model="updateOrderModel.workManager.user_name_en"
            filterable
            placeholder="请选择负责人"
            class="el-select-temp"
          >
            <el-option
              v-for="(item, index) in managers"
              :key="index"
              :label="item.user_name_cn"
              :value="item.user_name_en"
            >
              <span style="float: left">{{ item.user_name_cn }}</span>
              <span style="float: right">{{ item.user_name_en }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="测试小组长 ：" prop="groupLeader">
          <el-col :span="20" v-if="userRole < 30">{{
            updateOrderModel.groupLeader
          }}</el-col>
          <el-select
            v-if="userRole >= 30"
            v-model="updateOrderModel.groupLeader"
            multiple
            filterable
            placeholder="请选择测试小组长"
            class="el-select-temp"
          >
            <el-option
              v-for="(item, index) in groupLeaders"
              :key="index"
              :label="item.user_name_cn"
              :value="item.user_name_en"
            >
              <span style="float: left">{{ item.user_name_cn }}</span>
              <span style="float: right">{{ item.user_name_en }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="测试人员 ：" prop="testers">
          <el-col :span="20" v-if="userRole < 30">{{
            updateOrderModel.testers
          }}</el-col>
          <el-select
            v-if="userRole >= 30"
            v-model="updateOrderModel.testers"
            multiple
            filterable
            placeholder="请选择测试人员"
            class="el-select-temp"
          >
            <el-option
              v-for="(item, index) in testers"
              :key="index"
              :label="item.user_name_cn"
              :value="item.user_name_en"
            >
              <span style="float: left">{{ item.user_name_cn }}</span>
              <span style="float: right">{{ item.user_name_en }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="计划SIT开始时间 ：" prop="planSitDate">
          <div class="block">
            <el-col :span="20" v-if="userRole < 30">{{
              updateOrderModel.planSitDate
            }}</el-col>
            <span class="demonstration"></span>
            <el-date-picker
              class="el-select-temp"
              v-model="updateOrderModel.planSitDate"
              value-format="yyyy-MM-dd"
              type="date"
              placeholder="选择日期"
              v-if="userRole >= 30"
              :picker-options="
                sitDatePickerMethod(updateOrderModel.planUatDate)
              "
              disabled
            ></el-date-picker>
          </div>
        </el-form-item>
        <el-form-item label="计划UAT开始时间 ：" prop="planUatDate">
          <div class="block">
            <el-col :span="20" v-if="userRole < 30">{{
              updateOrderModel.planUatDate
            }}</el-col>
            <span class="demonstration"></span>
            <el-date-picker
              class="el-select-temp"
              v-model="updateOrderModel.planUatDate"
              value-format="yyyy-MM-dd"
              type="date"
              placeholder="选择日期"
              v-if="userRole >= 30"
              :picker-options="
                uatDatePickerMethod(
                  updateOrderModel.planSitDate,
                  updateOrderModel.planProDate
                )
              "
              disabled
            ></el-date-picker>
          </div>
        </el-form-item>
        <el-form-item label="计划投产开始时间 ：">
          <div class="block">
            <el-col :span="20" v-if="userRole < 30">{{
              formatDate(updateOrderModel.planProDate)
            }}</el-col>
            <span class="demonstration"></span>
            <el-date-picker
              class="el-select-temp"
              v-model="updateOrderModel.planProDate"
              value-format="yyyy-MM-dd"
              type="date"
              placeholder="选择日期"
              v-if="userRole >= 30"
              :picker-options="
                proDatePickerMethod(updateOrderModel.planUatDate)
              "
            ></el-date-picker>
          </div>
        </el-form-item>
        <el-form-item
          label="内部测试完成时间 ："
          v-if="
            updateOrderModel.stage === '3' ||
              updateOrderModel.stage === '6' ||
              updateOrderModel.stage === '9' ||
              updateOrderModel.stage === '10'
          "
          prop="uatSubmitDate"
        >
          <div class="block">
            <el-col :span="20" v-if="userRole < 30">{{
              updateOrderModel.uatSubmitDate
            }}</el-col>
            <span class="demonstration"></span>
            <el-date-picker
              class="el-select-temp"
              v-model="updateOrderModel.uatSubmitDate"
              value-format="yyyy-MM-dd"
              type="date"
              placeholder="选择日期"
              v-if="userRole >= 30"
            ></el-date-picker>
          </div>
        </el-form-item>
        <el-form-item
          label="含风险原因 ："
          v-if="
            updateOrderModel.stage === '6' ||
              updateOrderModel.stage === '10' ||
              updateOrderModel.stage === '13'
          "
          prop="riskDescription"
        >
          <el-checkbox-group v-model="updateOrderModel.riskDescription">
            <el-checkbox
              label="SIT/UAT并行"
              name="riskDescription"
            ></el-checkbox>
            <el-checkbox
              label="缺陷未修复"
              name="riskDescription"
            ></el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="备注 ：" class="text-grey-1" prop="remark">
          <el-col :span="20" v-if="userRole < 30">{{
            updateOrderModel.remark
          }}</el-col>
          <el-input
            class="el-select-temp"
            type="textarea"
            :autosize="{ minRows: 3 }"
            placeholder="请输入备注"
            maxlength="200"
            show-word-limit
            v-model="updateOrderModel.remark"
            v-if="userRole >= 30"
          ></el-input>
        </el-form-item>
        <el-form-item
          label="审批人员 ："
          class="text-grey-1"
          v-if="
            updateOrderModel.stage == 6 ||
              updateOrderModel.stage == 10 ||
              updateOrderModel.stage == 13
          "
          prop="approver"
        >
          <el-select
            v-model="updateOrderModel.approver"
            placeholder="请选择"
            multiple
            filterable
            class="el-select-temp"
          >
            <el-option
              v-for="item in this.approvalLists"
              :key="item.id"
              :label="item.user_name_cn"
              :value="item.user_name_en"
            >
              <span style="float: left">{{ item.user_name_cn }}</span>
              <span style="float: right">{{ item.user_name_en }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          label="测试阶段 ："
          class="text-grey-1"
          v-if="queryDetailOrder.stage > 0"
          prop="stage"
        >
          <el-select
            v-model="updateOrderModel.stage"
            placeholder="请选择"
            class="el-select-temp"
            @change="uatSubmitDateChange"
          >
            <el-option
              v-for="(item, index) in wordOrderStage"
              :key="index"
              :label="item.stageCnName"
              :value="item.stage"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer" v-if="userRole >= 30">
        <el-button @click="closeUpdateDia">返 回</el-button>
        <el-button type="primary" @click="updateOrder('updateOrderModel')"
          >确 定</el-button
        >
      </div>
    </el-dialog>
    <Dialog title="工单拆分" :visible.sync="dialogSplit">
      <el-form
        label-position="left"
        label-width="50%"
        :model="workOrderSplitModel"
        :rules="workOrderSplitModelRules"
        ref="workOrderSplitModel"
      >
        <el-form-item label="已有工单">
          <el-select
            class="select-width"
            clearable
            v-model="workOrderSplitModel.selectedWorkNo"
            placeholder="请选要拆入的工单名"
          >
            <el-option
              v-for="(item, index) in splitWorkOrderList"
              :key="index"
              :value="item.workOrderNo"
              :label="
                item.mainTaskName.length > 10
                  ? item.mainTaskName.substring(0, 10) + '...'
                  : item.mainTaskName
              "
            >
              <span style="float: left">{{ item.mainTaskName }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          label="新工单名"
          prop="splitTaskName"
          v-if="workOrderSplitModel.selectedWorkNo == ''"
        >
          <el-input
            @blur="splitTaskNameChange"
            v-model="workOrderSplitModel.splitTaskName"
            class="input-style"
            placeholder="请输入新工单名"
            clearable
          >
          </el-input>
        </el-form-item>
        <el-form-item label="任务" prop="taskNo">
          <el-select
            clearable
            multiple
            class="select-width"
            v-model="workOrderSplitModel.taskNo"
            placeholder="请选择任务"
          >
            <el-option
              v-for="(item, index) in taskOptionsSplit"
              :key="index"
              :value="item.value"
              :label="
                item.label.length > 10
                  ? item.label.substring(0, 10) + '...'
                  : item.label
              "
            >
              <span style="float: left">{{ item.label }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          label="测试人员"
          prop="testerNames"
          v-if="workOrderSplitModel.selectedWorkNo == ''"
        >
          <el-select
            class="select-width"
            multiple
            v-model="workOrderSplitModel.testerNames"
            placeholder="请选择"
            filterable
            clearable
          >
            <el-option
              v-for="(item, index) in selectNames"
              :key="index"
              :label="item.user_name_cn"
              :value="item.user_name_en"
            >
              <span style="float: left">{{ item.user_name_cn }}</span>
              <span style="float: right">{{ item.user_name_en }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <p>温馨提示：被选中的任务将组合成新工单</p>
      </el-form>

      <el-button
        type="primary"
        slot="footer"
        class="full-width"
        :loading="loadingBtn"
        @click="splitOrder('workOrderSplitModel')"
      >
        确定
      </el-button>
    </Dialog>
    <!-- 任务列表窗口 -->
    <TaskListDialog
      :openDialog="taskListDialog"
      :dialogModel="taskListDialogDetail"
      @taskListClose="closeDialogTaskList"
      @callBack="showCallBackDialog"
      :showCallBack="showCallBack"
      :isOtherGroup="isOtherGroup"
    />
  </div>
</template>
<script>
import TaskListDialog from './Compoments/TaskListDialog';
import {
  queryQueryOrderImpl,
  updateOrderImpl,
  queryQueryCountImpl
} from '@/services/order';
import { mapState, mapActions } from 'vuex';
import { queryAllGroup } from '@/services/useradmin';
import {
  handleDocDataRqr,
  dateFormat,
  deepClone,
  getUserListByRole,
  validate
} from '@/common/utlis';
import moment from 'moment';
import orderMenuTab from './orderMenuTab';
import { rules, orderTypeList, orderStageOther } from './model';
export default {
  name: 'QueryOrder',
  components: {
    orderMenuTab,
    TaskListDialog
  },
  data() {
    return {
      orderTypeList: orderTypeList,
      isOtherGroup: '',
      showCallBack: false,
      callBackworkNo: '',
      taskListDialogDetail: [],
      taskListDialog: false,
      loadingBtn: false,
      OrderSplitNo: '',
      dialogConcat: false,
      workOrderConcatModel: {
        worksNo: [],
        concatTaskName: ''
      },
      taskOptionsConcat: [],
      workOrderSplitModel: {
        taskNo: [],
        testerNames: [],
        splitTaskName: '',
        selectedWorkNo: ''
      },
      dialogSplit: false,
      taskOptionsSplit: [],
      userManager: [
        {
          role_en_name: 'admin',
          user_en_name: sessionStorage.getItem('user_en_name'),
          user_name_cn: sessionStorage.getItem('TuserName')
        }
      ],
      props: {
        children: 'children',
        label: 'label'
      },
      docData: [],
      docDataRqr: [
        { label: '需求说明书', children: [] },
        { label: '需求规格说明书', children: [] },
        { label: '其它相关材料', children: [] }
      ],
      rqrDocData: [],
      uatCopyDate: '',
      uatSubmitDateRule: false,
      userRole: sessionStorage.getItem('Trole'),
      isAssessor: sessionStorage.getItem('isAssessor'),
      planPower: false,
      activeName: 'third',
      queryDetailOrderDialogShow: false,
      queryDetailOrder: {
        mainTaskName: '',
        mainTaskNo: '',
        demandNo: '',
        demandName: '',
        workOrderNo: '',
        stage: '',
        workManager: '',
        groupLeader: '',
        testers: '',
        planSitDate: '',
        planUatDate: '',
        planProDate: '',
        remark: '',
        approver: '',
        groupName: '',
        fdevGroupId: '',
        orderType: ''
      },
      value1: [],
      updateOrderDialogShow: false,
      managers: [],
      groupLeaders: [],
      groupNameList: [],
      testers: [],
      approvalLists: [],
      formInline: {
        orderType: '',
        taskName: '',
        userEnName: '',
        groupId: ''
      },
      user_en_name: sessionStorage.getItem('user_en_name'),
      queryList: [],
      currentPage: 1,
      pageSize: 8,
      total: 0,
      groupNames: [],
      groupList: [],
      loading: false,
      selectNames: [],
      updateOrderModel: {
        mainTaskName: '',
        mainTaskNo: '',
        demandNo: '',
        demandName: '',
        workOrderNo: '',
        stage: '',
        workManager: '',
        groupLeader: [],
        testers: [],
        planSitDate: '',
        planUatDate: '',
        planProDate: '',
        uatSubmitDate: '',
        riskDescription: [],
        remark: '',
        approver: [],
        groupName: '',
        orderType: ''
      },
      stages: [],
      updateRules: {
        mainTaskName: [
          { required: true, message: '请输入工单名称', trigger: 'blur' }
        ],
        groupLeader: [
          { required: true, message: '请选择小组负责人', trigger: 'blur' }
        ],
        planUatDate: [
          { required: true, message: '请选择计划UAT开始时间', trigger: 'blur' }
        ],
        planSitDate: [
          { required: true, message: '请选择计划SIT开始时间', trigger: 'blur' }
        ],
        planProDate: [
          { required: true, message: '请选择计划投产时间', trigger: 'blur' }
        ],
        testers: [
          { required: true, message: '请选择测试人员', trigger: 'blur' }
        ],
        approver: [
          { required: true, message: '请选择审批人员', trigger: 'blur' }
        ],
        riskDescription: [
          {
            type: 'array',
            required: true,
            message: '请至少选择一个含风险的原因',
            trigger: 'change'
          }
        ],
        fdevGroupId: [
          { required: true, message: '请选择所属小组', trigger: 'blur' }
        ],
        stage: [{ required: true, message: '请选择测试阶段', trigger: 'blur' }]
      },
      updateRules1: {
        mainTaskName: [
          { required: true, message: '请输入工单名称', trigger: 'blur' }
        ],
        groupLeader: [
          { required: true, message: '请选择小组负责人', trigger: 'blur' }
        ],
        planUatDate: [
          { required: true, message: '请选择计划UAT开始时间', trigger: 'blur' }
        ],
        planSitDate: [
          { required: true, message: '请选择计划SIT开始时间', trigger: 'blur' }
        ],
        planProDate: [
          { required: true, message: '请选择计划投产时间', trigger: 'blur' }
        ],
        testers: [
          { required: true, message: '请选择测试人员', trigger: 'blur' }
        ],
        approver: [
          { required: true, message: '请选择审批人员', trigger: 'blur' }
        ],
        uatSubmitDate: [
          { required: true, message: '请选择内部测试完成时间', trigger: 'blur' }
        ],
        workManager: [
          { required: true, message: '请选择工单负责人', trigger: 'blur' }
        ],
        fdevGroupId: [
          { required: true, message: '请选择所属小组', trigger: 'blur' }
        ],
        stage: [{ required: true, message: '请选择测试阶段', trigger: 'blur' }]
      },
      showLoading: false
    };
  },
  filters: {
    testerFilter(users) {
      if (!users) {
        return;
      }
      return users.join(',');
    }
  },
  computed: {
    ...mapState('workOrderForm', [
      'filePath',
      'wordOrderStage',
      'childTaskList',
      'taskDoc',
      'rqrFilesByOrderNo',
      'taskAllData',
      'fdevTaskByWorkNo',
      'defaultWorkOrderName',
      'mergeOrderList',
      'verifyOrderNameBoolean',
      'splitWorkOrderList'
    ]),
    workOrderSplitModelRules() {
      return this.rules(this.workOrderSplitModel);
    },
    workOrderConcatModelRules() {
      return this.rules(this.workOrderConcatModel);
    }
  },
  created() {
    if (JSON.parse(sessionStorage.getItem('pageInfoQuery'))) {
      this.formInline = JSON.parse(sessionStorage.getItem('pageInfoQuery'));
    }
    if (!this.formInline.orderType) {
      this.formInline.orderType = 'function';
    }
    this.currentPage = sessionStorage.getItem('cacheQueryPage')
      ? Number(sessionStorage.getItem('cacheQueryPage'))
      : 1;
    if (this.$router.currentRoute.query.mainTaskName) {
      this.formInline.taskName =
        this.$router.currentRoute.query.mainTaskName || '';
    }
    if (
      JSON.parse(sessionStorage.getItem('pageInfoQuery')) ||
      sessionStorage.getItem('cacheQueryPage')
    ) {
      this.queryQueryOrder(
        this.formInline.taskName,
        this.formInline.userEnName,
        this.formInline.groupId,
        this.pageSize,
        this.currentPage,
        this.formInline.orderType
      );
    } else {
      this.queryGroupList();
      this.queryQueryOrder(
        this.formInline.taskName,
        this.formInline.userEnName,
        this.formInline.groupId,
        this.pageSize,
        this.currentPage,
        this.formInline.orderType
      );
      this.queryQueryCount();
    }
    if (this.$router.currentRoute.query.mainTaskName) {
      this.queryQueryOrder(
        this.formInline.taskName,
        this.formInline.userEnName,
        this.formInline.groupId,
        this.pageSize,
        this.currentPage,
        this.formInline.orderType
      );
    }
  },
  methods: {
    ...mapActions('workOrderForm', [
      'queryLastTransFilePath',
      'queryWorkOrderStageList',
      'downExcel',
      'queryTaskList',
      'queryRqrFilesByOrderNo',
      'queryTaskDoc',
      'queryQueryCountImpl',
      'queryTasks',
      'splitWorkOrder',
      'queryFdevTaskByWorkNo',
      'queryWorkOrderName',
      'verifyOrderName',
      'queryMergeOrderList',
      'mergeWorkOrder',
      'refuseTask',
      'querySplitOrderList'
    ]),
    remoteMethod(query) {
      if (query !== '') {
        this.loading = true;
        setTimeout(() => {
          this.loading = false;
          this.taskOptionsConcat = this.mergeOrderList.filter(item => {
            if (item.mainTaskName) {
              return (
                item.mainTaskName.toLowerCase().indexOf(query.toLowerCase()) >
                -1
              );
            }
          });
        }, 200);
      } else {
        this.taskOptionsConcat = this.mergeOrderList;
      }
    },
    formatDate(val) {
      if (val) {
        return val;
      } else {
        return '';
      }
    },
    orderStageOther(stage) {
      return orderStageOther[stage.trim()];
    },
    orderTypeText(type) {
      if (type == 'function') {
        return '功能测试';
      } else if (type == 'security') {
        return '安全测试';
      } else {
        return '';
      }
    },
    async showCallBackDialog(data) {
      this.$confirm('是否线上通知fdev相关人员变更实施单元?', '发送通知', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.refuseTask({
          workNo: this.callBackworkNo,
          taskIds: [data.row.id]
        });
        this.$message({
          type: 'success',
          message: '发送通知成功!'
        });
      });
    },
    async splitTaskNameChange() {
      const param = {
        mainTaskName: this.workOrderSplitModel.splitTaskName
      };
      await this.verifyOrderName(param);
      if (
        this.verifyOrderNameBoolean === false &&
        this.workOrderSplitModel.splitTaskName != ''
      ) {
        this.$message({
          showClose: true,
          message: '工单名已存在！',
          type: 'error'
        });
        return;
      }
    },
    async workOrderConcat(item) {
      this.OrderConcatNo = item.workOrderNo;
      this.taskOptionsConcat = [];
      this.workOrderConcatModel.worksNo = [];
      this.dialogConcat = true;
      // 查询可选择的工单列表
      await this.queryMergeOrderList({
        workNo: item.workOrderNo,
        unitNo: item.unit
      });
      this.taskOptionsConcat = this.mergeOrderList;
      await this.queryWorkOrderName({
        unit: item.unit
      });
      this.workOrderConcatModel.concatTaskName = this.defaultWorkOrderName;
    },
    async openTaskList(item) {
      this.taskListDialog = true;
      await this.queryFdevTaskByWorkNo({ workNo: item.workOrderNo });
      this.callBackworkNo = item.workOrderNo;
      this.taskListDialogDetail = this.fdevTaskByWorkNo;
      let isWorkManager = false;
      if (
        `${sessionStorage.getItem('TuserName')}` ===
        item.workManager.user_name_cn
      ) {
        isWorkManager = true;
      }
      let isrGoupLeader = item.groupLeader.some(
        value => value.user_name_cn === `${sessionStorage.getItem('TuserName')}`
      );
      this.showCallBack = isWorkManager || isrGoupLeader;
      this.isOtherGroup = item.fdevGroupId;
    },
    closeDialogTaskList() {
      this.taskListDialog = false;
    },
    rules(model, notNecessary = []) {
      const keys = Object.keys(model);
      const rulesObj = {};
      keys.forEach(key => {
        if (rules[key] && !notNecessary.includes(key)) {
          rulesObj[key] = rules[key];
        }
      });
      return rulesObj;
    },
    checkTaskOptionsConcat() {
      if (this.taskOptionsConcat.length === 0) {
        this.$message({
          type: 'warning',
          message: '只有一个工单不能进行合并!'
        });
      }
    },
    async concatOrder(workOrderConcatModel) {
      await validate(this.$refs[workOrderConcatModel]);
      this.loadingBtn = true;
      await this.mergeWorkOrder({
        workNo: this.OrderConcatNo,
        workNos: this.workOrderConcatModel.worksNo,
        name: this.workOrderConcatModel.concatTaskName
      });
      await this.queryMyOrders();
      this.$message({
        type: 'success',
        message: '合并成功!'
      });
      this.loadingBtn = false;
      this.dialogConcat = false;
    },
    async splitOrder(workOrderSplitModel) {
      await validate(this.$refs[workOrderSplitModel]);
      this.loadingBtn = true;
      let params = {};
      if (this.workOrderSplitModel.selectedWorkNo !== '') {
        params = {
          workNo: this.OrderSplitNo,
          taskIds: this.workOrderSplitModel.taskNo,
          selectedWorkNo: this.workOrderSplitModel.selectedWorkNo
        };
      } else {
        params = {
          workNo: this.OrderSplitNo,
          taskIds: this.workOrderSplitModel.taskNo,
          name: this.workOrderSplitModel.splitTaskName,
          testers: this.workOrderSplitModel.testerNames
        };
      }
      try {
        await this.splitWorkOrder(params);
        this.onSubmit();
        this.$message({
          type: 'success',
          message: '拆分成功!'
        });
      } catch (e) {
        this.loadingBtn = false;
      }
      this.loadingBtn = false;
      this.dialogSplit = false;
    },
    async workOrderSplit(item) {
      this.OrderSplitNo = item.workOrderNo;
      this.workOrderSplitModel.taskNo = '';
      this.taskOptionsSplit = [];
      this.workOrderSplitModel.testerNames = [];
      this.workOrderSplitModel.selectedWorkNo = '';
      this.dialogSplit = true;
      //窗口打开先通过工单id查任务
      await this.queryTasks({
        workNo: item.workOrderNo
      });
      this.taskOptionsSplit = this.taskAllData.taskList.map(item => {
        return { label: item.name, value: item.id };
      });
      await this.queryWorkOrderName({
        unit: item.unit
      });
      this.workOrderSplitModel.splitTaskName = this.defaultWorkOrderName;
      //拆分工单查询该实施单元下符合要求的工单
      await this.querySplitOrderList({
        workNo: item.workOrderNo,
        unitNo: item.unit
      });
    },
    async handNodeClick(data) {
      if (data.path) {
        let param = {
          path: data.path,
          moduleName: 'fdev-task'
        };
        await this.downExcel(param);
        this.$message({
          showClose: true,
          message: '下载成功!',
          type: 'success'
        });
      }
    },
    async downloadRqrDoc(data) {
      if (data.id) {
        let param = {
          path: data.id,
          moduleName: 'fdev-demand'
        };
        await this.downExcel(param);
        this.$message({
          showClose: true,
          message: '下载成功!',
          type: 'success'
        });
      }
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    onSubmit() {
      this.queryQueryOrder(
        this.formInline.taskName,
        this.formInline.userEnName,
        this.formInline.groupId,
        this.pageSize,
        1,
        this.formInline.orderType
      );
      this.queryQueryCount();
    },
    //换页
    handleCurrentChange(val) {
      this.currentPage = val;
      this.queryQueryOrder(
        this.formInline.taskName,
        this.formInline.userEnName,
        this.formInline.groupId,
        this.pageSize,
        val,
        this.formInline.orderType
      );
    },
    async getDoc() {
      this.showLoading = true;
      let docList = [];
      this.docData = [];
      if (
        this.queryDetailOrder.mainTaskNo ||
        this.queryDetailOrder.workOrderNo
      ) {
        try {
          await this.queryTaskDoc({
            id: this.queryDetailOrder.mainTaskNo
              ? this.queryDetailOrder.mainTaskNo
              : this.queryDetailOrder.workOrderNo
          });
        } catch (e) {
          this.showLoading = false;
        }
        if (
          this.taskDoc &&
          Array.isArray(this.taskDoc) &&
          this.taskDoc.length > 0
        ) {
          this.docData.push({
            label: this.queryDetailOrder.mainTaskName,
            children: this.taskDoc
          });
          docList.push(this.taskDoc);
        }
        if (this.queryDetailOrder.orderType == 'security') {
          try {
            await this.queryLastTransFilePath({
              workNo: this.queryDetailOrder.workOrderNo
            });
          } catch (e) {
            this.showLoading = false;
          }
          if (this.filePath) {
            let securityDoc = [
              {
                name: this.filePath.substring(
                  this.filePath.lastIndexOf('/') + 1
                ),
                path: this.filePath
              }
            ];
            this.docData.push({
              label: this.queryDetailOrder.mainTaskName,
              children: securityDoc
            });
            docList.push(securityDoc);
          }
        }
        for (let i = 0; i < this.childTaskList.length; i++) {
          try {
            await this.queryTaskDoc({
              id: this.childTaskList[i].taskno
            });
          } catch (e) {
            this.showLoading = false;
          }
          if (
            this.taskDoc &&
            Array.isArray(this.taskDoc) &&
            this.taskDoc.length > 0
          ) {
            docList.push(this.taskDoc);
            this.docData.push({
              label: this.childTaskList[i].taskname,
              children: this.taskDoc
            });
          }
        }
        handleDocDataRqr(this.docData, docList, false);
      }
      this.showLoading = false;
    },
    async getDocRqr() {
      this.showLoading = true;
      try {
        await this.queryRqrFilesByOrderNo({
          workNo: this.queryDetailOrder.workOrderNo
        });
      } catch (e) {
        this.showLoading = false;
      }
      handleDocDataRqr(this.docDataRqr, [
        this.rqrFilesByOrderNo.rqrmntInstruction,
        this.rqrFilesByOrderNo.rqrmntRule,
        this.rqrFilesByOrderNo.otherDoc
      ]);
      this.showLoading = false;
    },
    //查询所有组,去除"资源池"这一条
    queryGroupList() {
      queryAllGroup({ status: 1 }).then(res => {
        this.groupNameList = res;
      });
    },
    //历史工单详情
    async queryDetail(row) {
      this.init();
      this.queryDetailOrderDialogShow = true;
      this.queryDetailOrder.mainTaskName = row.mainTaskName;
      this.queryDetailOrder.mainTaskNo = row.mainTaskNo;
      this.queryDetailOrder.demandNo = row.demandNo;
      this.queryDetailOrder.unit = row.unit;
      this.queryDetailOrder.demandName = row.demandName;
      this.queryDetailOrder.workOrderNo = row.workOrderNo;
      this.queryDetailOrder.stage = row.stage;
      this.queryDetailOrder.workManager = row.workManager;
      this.queryDetailOrder.groupLeader = row.groupLeader;
      this.queryDetailOrder.testers = row.testers;
      this.queryDetailOrder.planSitDate = row.planSitDate;
      this.queryDetailOrder.planUatDate = row.planUatDate;
      this.queryDetailOrder.planProDate = row.planProDate;
      this.queryDetailOrder.remark = row.remark;
      this.queryDetailOrder.approver = row.field2;
      this.queryDetailOrder.uatSubmitDate = row.uatSubmitDate;
      this.queryDetailOrder.riskDescription = row.riskDescription;
      this.queryDetailOrder.groupName = row.groupName;
      this.queryDetailOrder.fdevGroupId = row.fdevGroupId;
      this.queryDetailOrder.orderType = row.orderType;
      let param = {
        currentPage: 1,
        pageSize: 50,
        taskName: '',
        workNo: this.queryDetailOrder.workOrderNo
      };
      await this.queryTaskList(param);
      await this.getDoc();
      await this.getDocRqr();
    },
    init() {
      this.rqrDocData = [];
    },
    //工单详情-修改页面 打开按钮事件
    async updateBegin() {
      //数据传递到修改页面默认值
      this.updateOrderModel = {
        ...deepClone(this.queryDetailOrder)
      };
      let riskDes = this.queryDetailOrder.riskDescription;
      if (riskDes) {
        this.updateOrderModel.riskDescription = riskDes.split(',');
      } else {
        this.updateOrderModel.riskDescription = [];
      }
      let i = '';
      let groupLeaderArr = [];
      let groupLeaderStr = '';
      let testersArr = [];
      let testersStr = '';
      let approverArr = [];
      let approverStr = '';
      const groupLeader = this.queryDetailOrder.groupLeader;
      const testers = this.queryDetailOrder.testers;
      const approver = this.queryDetailOrder.approver;
      if (groupLeader && groupLeader != '') {
        for (i = 0; i < groupLeader.length; i++) {
          groupLeaderArr.push(groupLeader[i].user_name_en);
        }
        groupLeaderStr = groupLeaderArr.toString();
        this.updateOrderModel.groupLeader = groupLeaderStr.split(',');
      } else {
        this.updateOrderModel.groupLeader = '';
      }
      if (testers && testers != '') {
        for (i = 0; i < testers.length; i++) {
          testersArr.push(testers[i].user_name_en);
        }
        testersStr = testersArr.toString();
        this.updateOrderModel.testers = testersStr.split(',');
      } else {
        this.updateOrderModel.testers = '';
      }
      if (approver && approver != '') {
        for (i = 0; i < approver.length; i++) {
          approverArr.push(approver[i].user_name_en);
        }
        approverStr = approverArr.toString();
        this.updateOrderModel.approver = approverStr.split(',');
      } else {
        this.updateOrderModel.approver = '';
      }
      this.uatCopyDate = this.updateOrderModel.uatSubmitDate;
      if (
        this.updateOrderModel.stage == 3 ||
        this.updateOrderModel.stage == 9
      ) {
        this.uatSubmitDateRule = true;
        this.updateOrderModel.uatSubmitDate = new Date(
          moment().format('YYYY-MM-DD')
        );
      } else {
        this.uatSubmitDateRule = false;
      }
      this.updateOrderModel.planSitDate = this.updateOrderModel.planSitDate
        ? new Date(this.updateOrderModel.planSitDate)
        : '';
      this.updateOrderModel.planUatDate = this.updateOrderModel.planUatDate
        ? new Date(this.updateOrderModel.planUatDate)
        : '';
      this.updateOrderModel.planProDate = this.updateOrderModel.planProDate
        ? new Date(this.updateOrderModel.planProDate)
        : '';
      if (
        this.updateOrderModel.uatSubmitDate != '' &&
        this.updateOrderModel.uatSubmitDate != null
      ) {
        this.updateOrderModel.uatSubmitDate = new Date(
          this.updateOrderModel.uatSubmitDate
        );
      }
      this.queryAllUser();
      this.stages = await this.queryWorkOrderStageList({
        workNo: this.queryDetailOrder.workOrderNo,
        groupId: this.updateOrderModel.fdevGroupId
      });
      this.updateOrderDialogShow = true;
      this.detailOrderDialogShow = false;
    },
    //查询测试人员及小组长
    async queryAllUser() {
      let userList = await getUserListByRole();
      this.managers = await getUserListByRole(
        ['玉衡-测试管理员', '玉衡-超级管理员'],
        userList
      );
      this.groupLeaders = await getUserListByRole(
        [
          '玉衡-测试组长',
          '玉衡-测试管理员',
          '玉衡超级管理员',
          '玉衡-安全测试组长'
        ],
        userList
      );
      this.testers = await getUserListByRole(
        [
          '测试人员',
          '玉衡-测试管理员',
          '玉衡超级管理员',
          '玉衡-测试组长',
          '玉衡-安全测试组长'
        ],
        userList
      );
    },
    // 时间控制
    sitDatePickerMethod(uatDate) {
      return {
        disabledDate(time) {
          if (uatDate) {
            //如果结束时间不为空，则小于结束时间
            return new Date(uatDate).getTime() < time.getTime();
          } else {
            // return time.getTime() > Date.now()//开始时间不选时，结束时间最大值小于等于当天
          }
        }
      };
    },
    uatDatePickerMethod(sitDate, proDate) {
      return {
        disabledDate(time) {
          if (sitDate) {
            //如果开始时间不为空，则结束时间大于开始时间
            var sitDates = new Date(sitDate).getTime();
            if (proDate) {
              var proDates = new Date(proDate).getTime();
              return sitDates > time.getTime() || proDates < time.getTime();
            }
            return sitDates > time.getTime();
          } else {
            // return time.getTime() > Date.now()//开始时间不选时，结束时间最大值小于等于当天
          }
        }
      };
    },
    proDatePickerMethod(uadDate) {
      return {
        disabledDate(time) {
          if (uadDate) {
            //如果开始时间不为空，则结束时间大于开始时间
            return new Date(uadDate).getTime() > time.getTime();
          } else {
            // return time.getTime() > Date.now()//开始时间不选时，结束时间最大值小于等于当天
          }
        }
      };
    },
    uatTestDatePickerMethod() {
      return {
        disabledDate(time) {
          return time.getTime() <= Date.now() - 3600 * 1000 * 24; //开始时间不选时，结束时间最大值大于等于当天
        }
      };
    },

    closeUpdateDia() {
      this.detailOrderDialogShow = true;
      this.updateOrderDialogShow = false;
    },
    //修改工单
    updateOrder(updateOrderModel) {
      this.$refs[updateOrderModel].validate(valid => {
        if (valid) {
          this.$confirm('是否确认修改?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(res => {
            var te = this.$options.filters['testerFilter'];
            //调用本地过滤器
            const testers = te(this.updateOrderModel.testers);
            const groupLeader = te(this.updateOrderModel.groupLeader);
            const approver = te(this.updateOrderModel.approver);
            let params = {
              workOrderNo: this.updateOrderModel.workOrderNo,
              mainTaskName: this.updateOrderModel.mainTaskName,
              mainTaskNo: this.updateOrderModel.mainTaskNo,
              planSitDate: dateFormat(this.updateOrderModel.planSitDate),
              planUatDate: dateFormat(this.updateOrderModel.planUatDate),
              planProDate: dateFormat(this.updateOrderModel.planProDate),
              testers: this.updateOrderModel.testers,
              workManager: this.updateOrderModel.workManager.user_name_en,
              groupLeader: this.updateOrderModel.groupLeader,
              remark: this.updateOrderModel.remark,
              stage: this.updateOrderModel.stage,
              aduits: this.updateOrderModel.approver,
              demandNo: this.updateOrderModel.demandNo,
              groupId: this.updateOrderModel.fdevGroupId,
              orderType: this.updateOrderModel.orderType
            };
            if (
              this.updateOrderModel.stage === '3' ||
              this.updateOrderModel.stage === '6' ||
              this.updateOrderModel.stage === '9' ||
              this.updateOrderModel.stage === '10'
            ) {
              params.uatSubmitDate = dateFormat(
                this.updateOrderModel.uatSubmitDate,
                '/'
              );
            }
            if (
              this.updateOrderModel.stage === '6' ||
              this.updateOrderModel.stage === '10'
            ) {
              params.riskDescription = this.updateOrderModel.riskDescription;
            }
            updateOrderImpl(params).then(res => {
              //需要更新数据，重新获取界面数据
              this.queryQueryOrder(
                this.formInline.taskName,
                this.formInline.userEnName,
                this.formInline.groupId,
                this.pageSize,
                this.currentPage,
                this.formInline.orderType
              );
              //数据传递
              this.queryDetailOrder.mainTaskName = this.updateOrderModel.mainTaskName;
              this.queryDetailOrder.mainTaskNo = this.updateOrderModel.mainTaskNo;
              this.queryDetailOrder.demandNo = this.updateOrderModel.demandNo;
              this.queryDetailOrder.workOrderNo = this.updateOrderModel.workOrderNo;
              this.queryDetailOrder.stage = this.updateOrderModel.stage;
              this.queryDetailOrder.workManager = this.updateOrderModel.workManager;
              this.queryDetailOrder.planSitDate = this.updateOrderModel.planSitDate;
              this.queryDetailOrder.planUatDate = this.updateOrderModel.planUatDate;
              this.queryDetailOrder.planProDate = this.updateOrderModel.planProDate;
              this.queryDetailOrder.uatSubmitDate = this.updateOrderModel.uatSubmitDate;
              this.queryDetailOrder.remark = this.updateOrderModel.remark;
              this.queryDetailOrder.approver = approver;
              this.queryDetailOrder.groupLeader = groupLeader;
              this.queryDetailOrder.testers = testers;

              //关闭修改弹窗，打开详情弹窗
              this.updateOrderDialogShow = false;
              this.queryDetailOrderDialogShow = false;

              this.$message({
                showClose: true,
                message: '修改成功',
                type: 'success'
              });
            });
          });
        } else {
          return false;
        }
      });
    },

    //查询工单
    queryQueryOrder(
      taskName,
      userEnName,
      groupId,
      pageSize,
      currentPage,
      orderType
    ) {
      queryQueryOrderImpl({
        taskName: taskName,
        pageSize: pageSize,
        currentPage: currentPage,
        user_en_name: userEnName,
        groupId: groupId,
        orderType: orderType
      }).then(res => {
        this.queryList = res;
      });
    },
    isAuthUser(item) {
      // 判断当前用户是否是工单负责人或者测试组长或者测试组员
      let authFlag = false;
      const username = localStorage.getItem('user_en_name');
      if (
        item.workManager &&
        item.workManager.user_name_en.includes(username)
      ) {
        authFlag = true;
      }
      for (let obj of item.groupLeader) {
        if (obj && obj.user_name_en.includes(username)) {
          authFlag = true;
          break;
        }
      }
      for (let obj of item.testers) {
        if (obj && obj.user_name_en.includes(username)) {
          authFlag = true;
          break;
        }
      }
      return authFlag;
    },
    /**
     * 跳转缺陷列表
     * */
    goMantisList(item) {
      var workOrderInfo = [
        { workOrderNo: item.workOrderNo, mainTaskName: item.mainTaskName }
      ];
      this.$router.push({
        name: 'MantisIssue',
        query: { workOrderInfo: workOrderInfo }
      });
    },
    /**
     * 跳转执行计划
     * */
    exePlan(item) {
      let i = '';
      let testersArr = [];
      let testersStr = '';
      const testers = item.testers;

      // 跳转到查询工单页面 判断是否是权限用户（工单负责人，测试组长，测试组员）
      const isAuthUser = this.isAuthUser(item);
      for (i = 0; i < testers.length; i++) {
        testersArr.push(testers[i].user_name_cn);
      }
      testersStr = testersArr.toString();
      // 已废弃工单 isWaste=true item.stage === '11'
      let queryParams = {};
      if (item.stage === '11') {
        queryParams = {
          workOrderNo: item.workOrderNo,
          isSelf: isAuthUser,
          isWaste: true
        };
      } else {
        queryParams = {
          workOrderNo: item.workOrderNo,
          isAuthUser
        };
      }
      this.$router.push({
        name: 'Plan',
        query: queryParams
      });
      const workOrderNoData = {
        stage: item.stage,
        workOrderNo: item.workOrderNo,
        testers: testersStr,
        mainTaskName: item.mainTaskName,
        planPower: this.planPower,
        workNo: item.workOrderNo,
        unitNo: item.unit
      };
      sessionStorage.setItem(
        'planWorkOrderNo',
        JSON.stringify(workOrderNoData)
      );
    },
    /**
     * 跳转任务列表
     * */
    goTaskList(item) {
      window.sessionStorage.setItem('childrenItem', JSON.stringify(item));
      var task = { workOrderNo: item.workOrderNo };
      this.$router.push({ name: 'TaskList', query: { item: task } });
    },
    //查询总数
    queryQueryCount() {
      queryQueryCountImpl({
        taskName: this.formInline.taskName,
        user_en_name: this.formInline.userEnName,
        groupId: this.formInline.groupId,
        orderType: this.formInline.orderType
      }).then(res => {
        this.total = res;
      });
    },
    //关闭历史详情页面
    closeQueryDia() {
      this.queryDetailOrderDialogShow = false;
    },
    closeDetailDia() {
      this.detailOrderDialogShow = false;
      this.queryDetailOrderDialogShow = false;
    },
    uatSubmitDateChange() {
      if (
        this.updateOrderModel.stage === '6' ||
        this.updateOrderModel.stage === '10'
      ) {
        this.updateOrderModel.uatSubmitDate = '';
      }
      if (
        this.updateOrderModel.stage === '3' ||
        this.updateOrderModel.stage === '9'
      ) {
        this.uatSubmitDateRule = true;
        this.updateOrderModel.uatSubmitDate = new Date(
          moment().format('YYYY-MM-DD')
        );
        if (this.uatCopyDate === '' || this.uatCopyDate === null) {
          this.updateOrderModel.uatSubmitDate = new Date();
        }
      } else {
        this.uatSubmitDateRule = false;
      }
    },
    getContent(val) {
      let res = [];
      val.forEach(item => {
        res.push(item.user_name_cn);
      });
      return res.join(' ');
    }
  },
  async mounted() {
    // 查询内测人员
    getUserListByRole().then(res => {
      this.selectNames = res;
      this.approvalLists = res;
    });
    this.queryQueryCount();
    this.queryGroupList();
  },
  watch: {
    //检测选中的工单，展示出他们包含的任务名
    'workOrderConcatModel.worksNo'(val) {
      this.showTaskName = [];
      val.forEach(item => {
        this.mergeOrderList.forEach(ele => {
          if (ele.workOrderNo == item) {
            ele.taskList.forEach(element => {
              if (element.name !== null) {
                this.showTaskName.push(element.name);
              }
            });
          }
        });
      });
    },
    updateOrderDialogShow(val) {
      if (!val) {
        this.updateOrderModel.groupName = '';
      }
    },
    'updateOrderModel.groupName': {
      handler(val, oldVal) {
        if ((oldVal && val) || oldVal === null) {
          this.queryWorkOrderStageList({
            workNo: this.queryDetailOrder.workOrderNo,
            groupId: val
          });
          this.updateOrderModel.stage = '';
        }
      },
      deep: true
    }
  },
  // 缓存页码
  beforeRouteEnter(to, from, next) {
    if (from.path === '/' || from.path.includes('/sitMsg/')) {
      sessionStorage.removeItem('cacheQueryPage');
      sessionStorage.removeItem('pageInfoQuery');
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    sessionStorage.setItem('cacheQueryPage', this.currentPage);
    sessionStorage.setItem('pageInfoQuery', JSON.stringify(this.formInline));
    next();
  }
};
</script>
<style scoped>
.tree-style >>> .is-leaf + .el-tree-node__label {
  color: #2196f3 !important;
  text-overflow: ellipsis !important;
  overflow: hidden !important;
}
.el-link {
  display: inline !important;
}
.title {
  font-size: 20px;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 10px;
  text-align: left;
}
.box-card {
  margin-top: 20px;
}
.card-footer {
  padding: 10px 10px;
}
.card-container {
  margin-bottom: 20px;
}
.cardEllipsis {
  width: 50%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.abow_dialog >>> .el-dialog__title {
  color: white;
  font-size: 20px;
  font-weight: 500;
}
.abow_dialog >>> .el-icon-close:before {
  color: white;
  font-size: 20px;
  font-weight: 600;
}
.abow_dialog >>> .el-dialog__header {
  background: #409eff;
}
.abow_dialog {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}
.abow_dialog >>> .el-dialog {
  margin: 0 auto !important;
  height: 90%;
  overflow: hidden;
}
.abow_dialog >>> .el-dialog__body {
  position: absolute;
  left: 10px;
  top: 70px;
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
.pagination {
  margin-top: 20px;
  padding: 20px 0;
  margin-right: 50px;
  text-align: right;
}
.modifyBtn {
  width: 100%;
}
.el-select-temp {
  width: 85%;
  font-size: 13px;
}
.el-select-temp >>> .el-input__inner {
  font-family: 'Helvetica Neue', 'Helvetica', 'PingFang SC';
}
.el-select-temp >>> .el-textarea__inner {
  font-family: 'Helvetica Neue', 'Helvetica', 'PingFang SC';
}
.height-32 {
  height: 32px;
}
.input-style {
  width: 230px;
}
.input-style >>> input {
  padding-right: 15px !important;
}
.rqr-list {
  color: #2196f3 !important;
  cursor: pointer;
}
.norqr-list {
  margin-left: 23px;
}
.input-style1 {
  width: 230px;
}
.icon {
  position: absolute;
  color: yellow;
  top: 2px;
}
.special_icon {
  margin-top: 8px;
  right: 20px;
}
.bottomItem {
  margin-bottom: 0 !important;
}
.select-width {
  width: 230px;
}
.input-order-name {
  width: 315px;
}
.elTooltip {
  max-width: 300px;
}
.pointMl16 {
  margin-left: 16px;
}
.pointMl4 {
  margin-left: 4px;
}
.headerStyle {
  display: flex;
  align-items: center;
}
.buttonMr10 {
  margin-right: 10px;
}
.point-success {
  background-image: linear-gradient(
    270deg,
    rgba(140, 188, 72, 0.5) 0%,
    #8cbc48 100%
  );
  border-radius: 12px;
  border-radius: 12px;
  width: 10px;
  height: 10px;
}
.point-primary {
  background-image: linear-gradient(
    270deg,
    rgba(64, 158, 255, 0.5) 0%,
    #409eff 100%
  );
  border-radius: 12px;
  border-radius: 12px;
  width: 10px;
  height: 10px;
}
.point-warning {
  background-image: linear-gradient(
    270deg,
    rgba(230, 162, 60, 0.5) 0%,
    #e6a23c 100%
  );
  border-radius: 12px;
  border-radius: 12px;
  width: 10px;
  height: 10px;
}
.point-danger {
  background-image: linear-gradient(
    270deg,
    rgba(245, 108, 108, 0.5) 0%,
    #f56c6c 100%
  );
  border-radius: 12px;
  border-radius: 12px;
  width: 10px;
  height: 10px;
}
.chipsecurity {
  background: #fd8d00;
  border-radius: 0 11px 11px 0;
  border-radius: 0px 11px 11px 0px;
  width: 92px;
  height: 22px;
}
.chipfunction {
  background: #4dbb59;
  border-radius: 0 11px 11px 0;
  border-radius: 0px 11px 11px 0px;
  width: 92px;
  height: 22px;
}
.textStyle {
  font-family: PingFangSC-Semibold;
  font-size: 12px;
  color: #ffffff;
  letter-spacing: 0;
  line-height: 22px;
  font-weight: 600;
}
.marginbt8 {
  margin-bottom: 8px;
}
.padding-10 {
  padding: 8px 10px 10px 10px;
}
.paddingWay {
  padding-left: 10px;
  padding-top: 7px;
}
.fontweight {
  font-weight: 600;
}
</style>
