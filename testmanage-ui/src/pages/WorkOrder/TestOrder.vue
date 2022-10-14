<template>
  <div class="page-container">
    <div>
      <orderMenuTab />
      <el-form :inline="true" :model="formInline">
        <el-form-item>
          <el-input
            v-model="formInline.taskName"
            class="input-style input-order-name"
            placeholder="需求名称/需求编号/实施单元/任务名/工单名 "
            maxlength="90"
            clearable
            @keyup.enter.native="queryOrder()"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="queryOrder">查询</el-button>
          <el-button type="primary" @click="addOrder" v-if="userRole >= 40">
            新增需求
          </el-button>
        </el-form-item>
      </el-form>

      <div class="card-container" v-loading="loading" v-if="userRole >= 40">
        <div class="title">可抢工单</div>
        <el-row :gutter="20">
          <el-col
            :span="6"
            v-for="(item, index) in assignOrderImpl"
            :key="index"
          >
            <OrderCard
              clickable
              divider
              shadow="always"
              class="card"
              @click="orderDetail(item, false)"
            >
              <template v-slot:header>
                <!-- 类型标签 -->
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
                      :class="'point-' + orderStage(item.stage).type"
                      class="pointMl16"
                    ></div>
                    <span
                      class="fontsize-12 weight pointMl4"
                      :class="'text-' + orderStage(item.stage).type"
                    >
                      {{ orderStage(item.stage).label }}
                    </span>
                  </div>
                </div>
              </template>

              <div class="ellipsis">
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
                    @click.stop="orderDetail(item, false)"
                    >{{ item.mainTaskName }}</el-link
                  >
                </el-tooltip>
              </div>

              <description label="需求名称" class="desc-margin">
                <el-tooltip
                  effect="dark"
                  :content="item.demandName"
                  placement="top-start"
                >
                  <el-col :span="12" class="ellipsis">{{
                    item.demandName
                  }}</el-col>
                </el-tooltip>
              </description>

              <template v-slot:footer>
                <div class="text-right full-width">
                  <el-button
                    type="primary"
                    size="small"
                    @click.stop="iWantToAssign(item)"
                    >我来分配</el-button
                  >
                </div>
              </template>
            </OrderCard>
          </el-col>
        </el-row>

        <div class="pagination">
          <el-pagination
            :hide-on-single-page="true"
            background
            @current-change="handleCurrentChange"
            :current-page="pagination.currentPage"
            :page-size="pagination.pageSize"
            :pager-count="5"
            layout="prev, pager, next"
            :total="pagination.total"
          />
        </div>
      </div>

      <el-form :inline="true" :model="selectMyOrders">
        <t-select
          prop="orderType"
          :options="orderTypeList"
          v-model="selectMyOrders.orderType"
          placeholder="工单类型"
        >
        </t-select>
        <t-select
          prop="label"
          :options="groupList"
          v-model="selectMyOrders.userEnName"
          filterable
          placeholder="请选择人员名称"
          clearable
          option-label="user_name_cn"
          option-value="user_name_en"
        >
          <template v-slot:options="item">
            <span class="option-left">{{ item.user_name_cn }}</span>
            <span class="option-right">{{ item.user_name_en }}</span>
          </template>
        </t-select>

        <t-select
          prop="stage"
          v-model="selectMyOrders.stage"
          placeholder="工单状态"
          clearable
          :options="stageOptions"
        />

        <t-select
          prop="sitFlag"
          v-model="selectMyOrders.sitFlag"
          placeholder="fdev是否提内测"
          clearable
          :options="sitFlagOptions"
        />

        <el-form-item>
          <el-button type="primary" @click="queryMyOrders()">查询</el-button>
          <el-button type="primary" @click="downloadOrder">下载</el-button>
          <el-button-group class="btn-group">
            <el-button
              type="primary"
              round
              @click="switchTable"
              :class="switchCard"
              >卡片</el-button
            >
            <el-button
              type="primary"
              round
              @click="switchTable"
              :class="switchChart"
              >表格</el-button
            >
          </el-button-group>
        </el-form-item>
      </el-form>

      <div class="card-container" v-loading="myOrderLoading">
        <div class="title">我的工单</div>
        <el-row :gutter="20" v-if="!tableShow">
          <el-col
            :span="6"
            v-for="(item, index) in myOrderImpl"
            :key="index"
            style="height: 280px;"
          >
            <OrderCard class="card" divider shadow="always">
              <template v-slot:header>
                <!-- 类型标签 -->
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
                      :class="'point-' + orderStage(item.stage).type"
                      class="pointMl16"
                    ></div>
                    <span
                      class="fontsize-12 weight pointMl4"
                      :class="'text-' + orderStage(item.stage).type"
                    >
                      {{ orderStage(item.stage).label }}
                    </span>
                  </div>
                </div>
                <!-- 按钮操作 -->
                <div class="buttonMr10">
                  <el-button
                    v-if="isAuthUser(item)"
                    size="mini"
                    type="text"
                    icon=""
                    @click.stop="dialogRevertDialog(item)"
                  >
                    打回
                  </el-button>
                  <el-button
                    size="mini"
                    type="text"
                    icon=""
                    v-if="item.stage == '0' && item.workOrderFlag == '1'"
                    @click.stop="backOrder(item)"
                  >
                    撤回
                  </el-button>
                  <el-button
                    size="mini"
                    type="text"
                    v-if="item.mantisFlag != null"
                    @click.stop="goMantisList(item)"
                  >
                    缺陷
                  </el-button>
                </div>
              </template>

              <div class="ellipsis">
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
                    @click.stop="orderDetail(item, true)"
                  >
                    {{ item.mainTaskName }}
                  </el-link>
                </el-tooltip>
              </div>

              <description label="需求名称" class="desc-margin">
                <el-tooltip
                  effect="dark"
                  :content="item.demandName"
                  placement="top-start"
                >
                  <el-col :span="12" class="ellipsis">{{
                    item.demandName
                  }}</el-col>
                </el-tooltip>
              </description>
              <description label="工单负责人" class="desc-margin">
                <el-tooltip
                  effect="dark"
                  :content="
                    item.workManager ? item.workManager.user_name_cn : '-'
                  "
                  placement="top-start"
                >
                  <el-col :span="12" class="ellipsis">{{
                    item.workManager ? item.workManager.user_name_cn : '-'
                  }}</el-col>
                </el-tooltip>
              </description>

              <description label="工单组长" class="desc-margin">
                <el-tooltip
                  effect="dark"
                  :content="getContent(item.groupLeader)"
                  placement="top-start"
                >
                  <el-col :span="12" class="ellipsis">
                    <span
                      v-for="(user, index) in item.groupLeader"
                      :key="index"
                    >
                      <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
                    </span>
                  </el-col>
                </el-tooltip>
              </description>

              <description label="测试人员" class="desc-margin">
                <el-tooltip
                  effect="dark"
                  :content="getContent(item.testers)"
                  placement="top-start"
                >
                  <el-col :span="12" class="ellipsis">
                    <span v-for="(user, index) in item.testers" :key="index">
                      <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
                    </span>
                  </el-col>
                </el-tooltip>
              </description>

              <template v-slot:footer>
                <div class="footer-tag">
                  <el-tag size="mini" v-if="item.sitFlag == '1'" type="success">
                    已提内测
                  </el-tag>
                  <el-tag size="mini" v-if="item.sitFlag == '2'" type="success">
                    已提业测
                  </el-tag>
                </div>

                <div class="footer-btn">
                  <el-button-group v-if="item.stage === '0'">
                    <el-button
                      type="primary"
                      size="small"
                      :disabled="isallot"
                      @click.stop="assign(item)"
                    >
                      分配
                    </el-button>
                    <el-button
                      size="small"
                      type="primary"
                      class="task-btn"
                      @click.stop="openTaskList(item)"
                      v-if="item.workOrderFlag != '0'"
                      >任务列表
                      <span v-if="item.hasTaskFlag === '1'" class="icon">
                        ★
                      </span>
                    </el-button>
                  </el-button-group>
                  <el-button-group v-if="btnShowArr.includes(item.stage)">
                    <el-button
                      type="primary"
                      size="small"
                      @click.stop="exePlan(item)"
                    >
                      执行计划
                      <span v-if="item.hasCaseFlag === '1'" class="icon-plan">
                        ★
                      </span>
                    </el-button>
                    <el-dropdown
                      trigger="click"
                      v-if="item.workOrderFlag != '0'"
                    >
                      <el-button size="small" type="primary"
                        >工单管理
                        <i class="el-icon-arrow-down"></i>
                        <span v-if="item.hasTaskFlag === '1'" class="icon-plan">
                          ★
                        </span>
                      </el-button>
                      <el-dropdown-menu slot="dropdown">
                        <el-dropdown-item
                          v-if="item.workOrderFlag != '0'"
                          class="clearfix"
                        >
                          <span @click.stop="openTaskList(item)"
                            >任务列表
                          </span>
                        </el-dropdown-item>

                        <el-dropdown-item
                          class="clearfix"
                          v-if="item.orderType == 'function'"
                        >
                          <span @click.stop="workOrderSplit(item)"
                            >工单拆分
                          </span>
                        </el-dropdown-item>

                        <el-dropdown-item
                          class="clearfix"
                          v-if="item.orderType == 'function'"
                        >
                          <span @click.stop="workOrderConcat(item)"
                            >工单合并
                          </span>
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </el-dropdown>
                  </el-button-group>
                </div>
              </template>
            </OrderCard>
          </el-col>
        </el-row>
        <div class="pagination" v-if="!tableShow">
          <el-pagination
            :hide-on-single-page="true"
            background
            @current-change="handleCurrentChange2"
            :current-page="myTestOrderPagination.currentPage"
            :page-size="myTestOrderPagination.pageSize"
            :pager-count="5"
            layout="prev, pager, next"
            :total="myTestOrderPagination.total"
          />
        </div>
        <el-table
          stripe
          :data="myOrderImpl"
          tooltip-effect="dark"
          style="width: 100%;color:black"
          :header-cell-style="handleTheadStyle"
          @sort-change="changeTableSort"
          v-if="tableShow"
        >
          <el-table-column
            prop="mainTaskName"
            label="工单名称"
            fixed
            width="140"
          >
            <template slot-scope="scope">
              <el-tooltip
                effect="dark"
                :content="scope.row.mainTaskName"
                placement="top-start"
                class="tooltip"
              >
                <el-link
                  :underline="false"
                  type="primary"
                  @click.stop="orderDetail(scope.row, true)"
                >
                  {{ scope.row.mainTaskName }}
                </el-link>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="demandNo" label="需求名称" width="140">
            <template slot-scope="scope">
              <el-tooltip
                effect="dark"
                :content="scope.row.demandName"
                placement="top-start"
                class="tooltip"
              >
                <div>
                  <span>{{ scope.row.demandName }}</span>
                </div>
              </el-tooltip>
            </template>
          </el-table-column>
          <!-- 工单类型占位 -->
          <el-table-column prop="orderType" label="工单类型" width="110">
            <template slot-scope="scope">
              <div>
                <span>{{ orderTypeText(scope.row.orderType) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="workOrderNo" label="工单编号" width="120">
            <template slot-scope="scope">
              <el-tooltip
                effect="dark"
                :content="scope.row.workOrderNo"
                placement="top-start"
                class="tooltip"
              >
                <div>
                  <span>{{ scope.row.workOrderNo }}</span>
                </div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column
            prop="groupName"
            label="所属小组"
            sortable="custom"
            width="140"
          >
            <template slot-scope="scope">
              <el-tooltip
                effect="dark"
                :content="scope.row.groupName"
                placement="top-start"
                class="tooltip"
              >
                <div>
                  <span>{{ scope.row.groupName }}</span>
                </div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column
            prop="workManager.user_name_cn"
            label="工单负责人"
            width="120"
          >
          </el-table-column>
          <el-table-column prop="groupLeader" label="测试组长" width="120">
            <template slot-scope="scope">
              <el-tooltip
                effect="dark"
                :content="getContent(scope.row.groupLeader)"
                placement="top-start"
                class="tooltip"
              >
                <div>
                  <span
                    v-for="(user, index) in scope.row.groupLeader"
                    :key="index"
                  >
                    <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
                  </span>
                </div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="testers" label="测试人员" width="80">
            <template slot-scope="scope">
              <el-tooltip
                effect="dark"
                :content="getContent(scope.row.testers)"
                placement="top-start"
                class="tooltip"
              >
                <div>
                  <span v-for="(user, index) in scope.row.testers" :key="index">
                    <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
                  </span>
                </div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column
            prop="stage"
            label="测试阶段"
            sortable="custom"
            width="120"
          >
            <template slot-scope="scope">
              <el-tooltip
                effect="dark"
                :content="orderStage(scope.row.stage).label"
                placement="top-start"
                class="tooltip"
              >
                <span>
                  {{ orderStage(scope.row.stage).label }}
                </span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column
            prop="planSitDate"
            label="计划SIT开始时间"
            width="130"
          ></el-table-column>
          <el-table-column
            prop="planUatDate"
            label="计划UAT开始时间"
            width="140"
          ></el-table-column>
          <el-table-column
            prop="planProDate"
            label="计划投产开始时间"
            width="140"
          ></el-table-column>
        </el-table>

        <div class="pagination" v-if="tableShow">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange2"
            :current-page="myTestOrderPagination.currentPage"
            :page-sizes="[8, 15, 20, 50, 100, 200, 500]"
            :page-size="myTestOrderPagination.pageSize"
            layout="sizes, total, prev, pager, next"
            :total="myTestOrderPagination.total"
          ></el-pagination>
        </div>
      </div>
      <!-- 分配弹窗 129Line事件 -->
      <Dialog
        title="工单分配"
        :visible.sync="assignOrderDialogShow"
        :before-close="closeAssignDia"
      >
        <el-form
          ref="assignOrderModel"
          :rules="assignOrderModelRules"
          label-position="right"
          :model="assignOrderModel"
        >
          <t-select
            label="测试小组长"
            label-width="40%"
            prop="groupLeader"
            :options="groupLeaders"
            v-model="assignOrderModel.groupLeader"
            filterable
            multiple
            placeholder="请选择"
            option-label="user_name_cn"
            option-value="user_name_en"
          >
            <template v-slot:options="item">
              <span class="option-left">{{ item.user_name_cn }}</span>
              <span class="option-right">{{ item.user_name_en }}</span>
            </template>
          </t-select>

          <t-select
            label="测试人员"
            label-width="40%"
            prop="testers"
            :options="testers"
            v-model="assignOrderModel.testers"
            filterable
            multiple
            placeholder="请选择"
            option-label="user_name_cn"
            option-value="user_name_en"
          >
            <template v-slot:options="item">
              <span class="option-left">{{ item.user_name_cn }}</span>
              <span class="option-right">{{ item.user_name_en }}</span>
            </template>
          </t-select>
          <t-select
            label="所属小组"
            label-width="40%"
            prop="groupName"
            :options="groupNameList"
            v-model="assignOrderModel.groupName"
            filterable
            placeholder="请选择"
            option-label="name"
            option-value="id"
          >
          </t-select>
        </el-form>
        <el-button
          slot="footer"
          class="full-width"
          type="primary"
          @click="submitOrderForm('assignOrderModel')"
        >
          确定
        </el-button>
      </Dialog>

      <!-- 新增工单 22Line事件 -->
      <Dialog title="新增工单" :visible.sync="addOrderDialogShow">
        <el-form
          label-position="right"
          :model="addOrderModel"
          :rules="addOrderModelRules"
          ref="addOrderModel"
        >
          <el-form-item label="工单名称:" label-width="40%" prop="mainTaskName">
            <el-input
              @blur="verifyTaskName"
              v-model="addOrderModel.mainTaskName"
              placeholder="工单名称"
              maxlength="90"
            />
          </el-form-item>

          <el-form-item label="需求名称:" label-width="40%" prop="demandName">
            <el-input
              v-model="addOrderModel.demandName"
              placeholder="需求名称"
              maxlength="25"
            />
          </el-form-item>

          <t-select
            label="所属小组"
            label-width="40%"
            prop="groupId"
            :options="groupNameList"
            v-model="addOrderModel.groupId"
            filterable
            placeholder="请选择所属小组"
            option-label="name"
            option-value="id"
          >
          </t-select>

          <t-select
            v-model="addOrderModel.workManager"
            filterable
            label="工单负责人"
            prop="workManager"
            label-width="40%"
            placeholder="请选择负责人"
            :options="managers"
            option-label="user_name_cn"
            option-value="user_name_en"
          >
            <template v-slot:options="item">
              <span class="option-left">{{ item.user_name_cn }}</span>
              <span class="option-right">{{ item.user_name_en }}</span>
            </template>
          </t-select>

          <t-select
            v-model="addOrderModel.groupLeader"
            filterable
            label="测试小组长"
            prop="groupLeader"
            label-width="40%"
            multiple
            placeholder="请选择测试小组长"
            :options="groupLeaders"
            option-label="user_name_cn"
            option-value="user_name_en"
          >
            <template v-slot:options="item">
              <span class="option-left">{{ item.user_name_cn }}</span>
              <span class="option-right">{{ item.user_name_en }}</span>
            </template>
          </t-select>

          <t-select
            v-model="addOrderModel.testers"
            filterable
            label="测试人员"
            prop="testers"
            label-width="40%"
            multiple
            placeholder="请选择测试人员"
            :options="testers"
            option-label="user_name_cn"
            option-value="user_name_en"
          >
            <template v-slot:options="item">
              <span class="option-left">{{ item.user_name_cn }}</span>
              <span class="option-right">{{ item.user_name_en }}</span>
            </template>
          </t-select>

          <el-form-item
            label-width="40%"
            label="计划SIT开始时间:"
            prop="planSitDate"
          >
            <el-date-picker
              v-model="addOrderModel.planSitDate"
              value-format="yyyy/MM/dd"
              format="yyyy/MM/dd"
              type="date"
              placeholder="选择日期"
              :picker-options="sitDatePickerMethod(addOrderModel.uatDate)"
            />
          </el-form-item>

          <el-form-item
            label-width="40%"
            label="计划UAT开始时间:"
            prop="planUatDate"
          >
            <el-date-picker
              v-model="addOrderModel.planUatDate"
              value-format="yyyy/MM/dd"
              format="yyyy/MM/dd"
              type="date"
              placeholder="选择日期"
              :picker-options="
                uatDatePickerMethod(
                  addOrderModel.sitDate,
                  addOrderModel.proDate
                )
              "
            />
          </el-form-item>

          <el-form-item
            label-width="40%"
            label="计划投产开始时间:"
            prop="planProDate"
          >
            <el-date-picker
              v-model="addOrderModel.planProDate"
              value-format="yyyy/MM/dd"
              format="yyyy/MM/dd"
              type="date"
              placeholder="选择日期"
              :picker-options="proDatePickerMethod(addOrderModel.uatDate)"
            />
          </el-form-item>

          <el-form-item label-width="30%" label="备注:" prop="remark">
            <el-input
              type="textarea"
              :autosize="{ minRows: 3 }"
              maxlength="200"
              show-word-limit
              placeholder="请输入备注"
              v-model="addOrderModel.remark"
            />
          </el-form-item>
        </el-form>
        <el-button
          slot="footer"
          class="full-width"
          type="primary"
          @click="submitAddOrderForm('addOrderModel')"
        >
          确定
        </el-button>
      </Dialog>
      <!-- 工单详情 -->
      <Dialog
        title="工单详情"
        :visible.sync="detailOrderDialogShow"
        close-on-click-modal
        width="45%"
      >
        <div v-loading="showLoading">
          <description align="center" :span="9" label="工单名称">
            {{ detailOrder.mainTaskName }}
          </description>
          <description
            align="center"
            :span="9"
            label="工单编号"
            class="q-mt-md"
            >{{ detailOrder.workOrderNo }}</description
          >

          <description
            align="center"
            :span="9"
            label="需求名称"
            class="q-mt-md"
          >
            {{ detailOrder.demandName }}
          </description>

          <description
            align="center"
            :span="9"
            label="需求编号"
            class="q-mt-md"
          >
            {{ detailOrder.demandNo }}
          </description>

          <description
            align="center"
            :span="9"
            label="实施单元"
            class="q-mt-md"
          >
            {{ detailOrder.unit }}
          </description>

          <description
            align="center"
            :span="9"
            label="所属小组"
            class="q-mt-md"
            >{{ detailOrder.groupName }}</description
          >
          <description
            align="center"
            :span="9"
            label="工单负责人"
            class="q-mt-md"
          >
            {{
              detailOrder.workManager
                ? detailOrder.workManager.user_name_cn
                : '-'
            }}
          </description>

          <description
            align="center"
            :span="9"
            label="测试小组长"
            class="q-mt-md"
          >
            <span v-for="(user, index) in detailOrder.groupLeader" :key="index">
              <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
            </span>
          </description>

          <description
            align="center"
            :span="9"
            label="测试人员"
            class="q-mt-md"
          >
            <span
              :span="22"
              v-for="(user, index) in detailOrder.testers"
              :key="index"
            >
              <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
            </span>
          </description>

          <description
            align="center"
            :span="9"
            label="计划SIT开始时间"
            class="q-mt-md"
          >
            {{ detailOrder.planSitDate }}
          </description>

          <description
            align="center"
            :span="9"
            label="计划UAT开始时间"
            class="q-mt-md"
          >
            {{ detailOrder.planUatDate }}
          </description>

          <description
            align="center"
            :span="9"
            label="计划投产开始时间"
            class="q-mt-md"
          >
            {{ detailOrder.planProDate | filterDate }}
          </description>

          <description
            align="center"
            :span="9"
            label="内部测试完成时间"
            class="q-mt-md"
            v-if="
              detailOrder.stage === '3' ||
                detailOrder.stage === '6' ||
                detailOrder.stage === '9' ||
                detailOrder.stage === '10'
            "
          >
            {{ detailOrder.uatSubmitDate }}
          </description>

          <description
            align="center"
            :span="9"
            label="风险描述"
            class="q-mt-md"
            v-if="
              detailOrder.stage === '6' ||
                detailOrder.stage === '10' ||
                detailOrder.stage === '13'
            "
          >
            {{ detailOrder.riskDescription }}
          </description>

          <description
            align="center"
            :span="9"
            label="截图路径"
            v-if="!detailOrder.stage == '14'"
            class="q-mt-md"
          >
            {{ detailOrder.imageLink }}
          </description>

          <description align="center" :span="9" label="备注" class="q-mt-md">
            {{ detailOrder.remark }}
          </description>

          <description
            align="center"
            :span="9"
            label="审批人员"
            class="q-mt-md"
            v-if="detailOrder.stage == 5"
          >
            <span v-for="(user, index) in detailOrder.approver" :key="index">
              <span v-if="user">{{ user.user_name_cn + ' ' }}</span>
            </span>
          </description>

          <description
            align="center"
            :span="9"
            label="测试阶段"
            class="q-mt-md"
          >
            {{
              orderStage(detailOrder.stage)
                ? orderStage(detailOrder.stage).label
                : ''
            }}
          </description>

          <description
            align="center"
            :span="9"
            label="任务关联文档"
            class="q-mt-md"
          >
            <el-col>
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
            </el-col>
          </description>

          <description
            align="center"
            :span="9"
            label="需求关联文档"
            class="q-mt-md bottomItem"
          >
            <div>
              <el-tree
                :default-expand-all="true"
                class="tree tree-style"
                :data="docDataRqr"
                ref="tree"
                highlight-current
                :props="propsRqr"
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
            </div>
          </description>
        </div>
        <el-button
          class="full-width"
          v-if="userRole >= 30 && canEdit"
          slot="footer"
          type="primary"
          @click="updateBegin"
        >
          修改
        </el-button>

        <el-button
          class="full-width"
          slot="footer"
          v-else
          type="primary"
          @click="closeDetailDia"
        >
          确定
        </el-button>
      </Dialog>
      <!-- 工单修改 -->
      <Dialog
        title="工单修改"
        :visible.sync="updateOrderDialogShow"
        :before-close="closeUpdateDia"
      >
        <el-form
          :model="updateOrderModel"
          :rules="updateOrderModelRules"
          ref="updateOrderModel"
          label-width="40%"
          label-position="right"
        >
          <el-form-item label="工单名称:" prop="mainTaskName">
            <el-input v-model="updateOrderModel.mainTaskName" disabled />
          </el-form-item>

          <el-form-item label="工单编号:" prop="workOrderNo">
            <el-input v-model="updateOrderModel.workOrderNo" disabled />
          </el-form-item>

          <el-form-item label="需求名称:">
            <el-input v-model="updateOrderModel.demandName" disabled />
          </el-form-item>

          <el-form-item label="需求编号:">
            <el-input v-model="updateOrderModel.demandNo" disabled />
          </el-form-item>

          <el-form-item label="所属小组：" prop="groupId">
            <t-select
              v-model="updateOrderModel.groupId"
              filterable
              placeholder="请选择所属小组"
              :options="groupNameList"
              option-label="name"
              option-value="id"
              :disabled="groupDisabled"
            >
            </t-select>
          </el-form-item>

          <el-form-item label="工单负责人:" prop="workManager">
            <t-select
              v-model="updateOrderModel.workManager"
              filterable
              placeholder="请选择负责人"
              :options="managers"
              option-label="user_name_cn"
              option-value="user_name_en"
              :disabled="userRole < 40"
            >
              <template v-slot:options="item">
                <span class="option-left">{{ item.user_name_cn }}</span>
                <span class="option-right">{{ item.user_name_en }}</span>
              </template>
            </t-select>
          </el-form-item>

          <el-form-item label="测试小组长:" prop="groupLeader">
            <t-select
              v-model="updateOrderModel.groupLeader"
              filterable
              placeholder="请选择测试小组长"
              :options="groupLeaders"
              multiple
              option-label="user_name_cn"
              option-value="user_name_en"
            >
              <template v-slot:options="item">
                <span class="option-left">{{ item.user_name_cn }}</span>
                <span class="option-right">{{ item.user_name_en }}</span>
              </template>
            </t-select>
          </el-form-item>

          <el-form-item label="测试人员:" prop="testers">
            <t-select
              :disabled="userRole < 30"
              v-model="updateOrderModel.testers"
              filterable
              placeholder="请选择测试人员"
              :options="testers"
              multiple
              option-label="user_name_cn"
              option-value="user_name_en"
            >
              <template v-slot:options="item">
                <span class="option-left">{{ item.user_name_cn }}</span>
                <span class="option-right">{{ item.user_name_en }}</span>
              </template>
            </t-select>
          </el-form-item>

          <el-form-item label="计划SIT开始时间:" prop="planSitDate">
            <el-date-picker
              v-model="updateOrderModel.planSitDate"
              value-format="yyyy/MM/dd"
              format="yyyy/MM/dd"
              type="date"
              placeholder="选择日期"
              :picker-options="
                sitDatePickerMethod(updateOrderModel.planUatDate)
              "
              disabled
            />
          </el-form-item>

          <el-form-item label="计划UAT开始时间:" prop="planUatDate">
            <el-date-picker
              v-model="updateOrderModel.planUatDate"
              value-format="yyyy/MM/dd"
              format="yyyy/MM/dd"
              type="date"
              placeholder="选择日期"
              :picker-options="
                uatDatePickerMethod(
                  updateOrderModel.planSitDate,
                  updateOrderModel.planProDate
                )
              "
              disabled
            />
          </el-form-item>

          <el-form-item label="计划投产开始时间 ：">
            <el-date-picker
              v-model="updateOrderModel.planProDate"
              value-format="yyyy/MM/dd"
              format="yyyy/MM/dd"
              type="date"
              placeholder="选择日期"
              :picker-options="
                proDatePickerMethod(updateOrderModel.planUatDate)
              "
            />
          </el-form-item>

          <el-form-item
            label="内部测试完成时间:"
            v-if="
              updateOrderModel.stage === '3' ||
                updateOrderModel.stage === '6' ||
                updateOrderModel.stage === '9' ||
                updateOrderModel.stage === '10'
            "
            prop="uatSubmitDate"
          >
            <el-date-picker
              v-model="updateOrderModel.uatSubmitDate"
              value-format="yyyy/MM/dd"
              format="yyyy/MM/dd"
              type="date"
              placeholder="选择日期"
            />
          </el-form-item>

          <el-form-item
            label="含风险原因:"
            v-if="
              updateOrderModel.stage === '6' ||
                updateOrderModel.stage === '10' ||
                updateOrderModel.stage === '13'
            "
            prop="riskDescription"
          >
            <el-checkbox-group v-model="updateOrderModel.riskDescription">
              <el-checkbox label="SIT/UAT并行" name="riskDescription" />
              <el-checkbox label="缺陷未修复" name="riskDescription" />
            </el-checkbox-group>
          </el-form-item>

          <t-select
            v-model="updateOrderModel.approver"
            filterable
            placeholder="请选择测试小组长"
            :options="assessorList"
            v-if="
              updateOrderModel.stage == 6 ||
                updateOrderModel.stage == 10 ||
                updateOrderModel.stage == 13
            "
            multiple
            prop="approver"
            label="审批人员"
            option-value="user_name_en"
            option-label="user_name_cn"
          >
            <template v-slot:options="item">
              <span class="option-left">{{ item.user_name_cn }}</span>
              <span class="option-right">{{ item.user_name_en }}</span>
            </template>
          </t-select>

          <t-select
            @input="isUatShowNowDate"
            v-model="updateOrderModel.stage"
            filterable
            placeholder="请选择测试阶段"
            :options="wordOrderStage"
            prop="stage"
            label="测试阶段"
            v-if="detailOrder.stage > 0"
            option-label="stageCnName"
            option-value="stage"
          />

          <el-form-item
            label="截图路径:"
            prop="imageLink"
            v-if="!detailOrder.stage == '14'"
          >
            <el-input
              type="textarea"
              :autosize="{ minRows: 3 }"
              placeholder="请输入截图路径"
              show-word-limit
              v-model="updateOrderModel.imageLink"
            />
          </el-form-item>

          <el-form-item label="备注:" prop="remark">
            <el-input
              type="textarea"
              :autosize="{ minRows: 3 }"
              placeholder="请输入备注"
              maxlength="200"
              show-word-limit
              v-model="updateOrderModel.remark"
              :disabled="userRole < 30"
            />
          </el-form-item>
        </el-form>
        <div slot="footer" v-if="userRole >= 30">
          <el-button @click="closeUpdateDia">返回</el-button>
          <el-button type="primary" @click="updateOrder('updateOrderModel')">
            确定
          </el-button>
        </div>
      </Dialog>
    </div>

    <!-- 右下角通知 -->
    <div class="card-notice" v-if="showNotice">
      <OrderCard class="card" shadow="always">
        <template v-slot:header>
          <span>通知</span>
          <i class="el-icon-close" @click="closeNotice" />
        </template>

        <div class="notice-body">您好：您有新的通知需要处理</div>

        <template v-slot:footer>
          <div class="text-right full-width">
            <el-button size="small" @click="closeNotice">关闭</el-button>
            <el-button
              size="small"
              type="primary"
              @click="dialogCardVisible = true"
            >
              处理
            </el-button>
          </div>
        </template>
      </OrderCard>
    </div>

    <!-- 通知列表弹框 -->
    <Dialog
      title="通知列表"
      close-on-click-modal
      :visible.sync="dialogCardVisible"
      width="90%"
    >
      <el-table
        stripe
        :data="dialogNoticeTable"
        tooltip-effect="dark"
        class="full-width"
      >
        <el-table-column prop="workNo" label="工单编号" width="190" />

        <el-table-column prop="taskName" label="任务名称">
          <template slot-scope="scope">
            <el-link
              type="primary"
              :underline="false"
              @click="taskNameDialog(scope.row)"
            >
              {{ scope.row.taskName }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column prop="workStage" label="测试阶段" width="100" />

        <el-table-column
          prop="taskReason"
          label="测试原因"
          :formatter="({ taskReason }) => formatterTaskName(taskReason)"
          width="180"
        />

        <el-table-column prop="rqrNo" label="需求编号" width="160" />

        <el-table-column prop="taskDesc" label="描述" show-overflow-tooltip />

        <el-table-column label="操作" width="160">
          <template slot-scope="scope">
            <el-button
              v-if="!scope.row.isEgdit"
              type="text"
              size="small"
              @click="handleAddPlan(scope.row)"
            >
              新增计划
            </el-button>
            <el-button
              @click="handleIgnore(scope.$index, scope.row)"
              type="text"
              size="small"
            >
              忽略
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleAllIgnore()">
          忽略全部
        </el-button>
      </div>
      <div class="pagination">
        <el-pagination
          @size-change="handleSizeChange3"
          @current-change="handleCurrentChange3"
          :current-page="noticePagination.currentPage"
          :page-sizes="[5, 10, 50, 100, 200, 500]"
          :page-size="noticePagination.pageSize"
          layout="sizes, prev, pager, next, jumper"
          :total="noticePagination.total"
        />
      </div>
    </Dialog>

    <!-- 通知详情弹框 -->
    <Dialog
      title="通知详情"
      :visible.sync="dialogNotifyDetail"
      close-on-click-modal
    >
      <el-form label-position="left" :model="detailNotify" label-width="18%">
        <el-form-item label="工单编号:">
          <el-input v-model="detailNotify.workNo" readonly />
        </el-form-item>
        <el-form-item label="任务编号:">
          <el-input v-model="detailNotify.taskNo" readonly />
        </el-form-item>
        <el-form-item label="任务名称:">
          <el-input v-model="detailNotify.taskName" readonly />
        </el-form-item>
        <el-form-item label="需求编号:">
          <el-input v-model="detailNotify.rqrNo" readonly />
        </el-form-item>
        <el-form-item label="工单状态:">
          <el-input v-model="detailNotify.workStage" readonly />
        </el-form-item>
        <el-form-item label="测试原因:">
          <el-input
            :value="formatterTaskName(detailNotify.taskReason)"
            readonly
          />
        </el-form-item>
        <el-form-item label="任务描述:">
          <el-input
            type="textarea"
            :rows="4"
            v-model="detailNotify.taskDesc"
            readonly
          />
        </el-form-item>
      </el-form>
      <el-button
        slot="footer"
        class="full-width"
        type="primary"
        @click="dialogNotifyDetail = false"
      >
        关闭
      </el-button>
    </Dialog>

    <!-- 新增执行计划  -->
    <Dialog title="新增测试计划" :visible.sync="dialogAdd">
      <el-form
        :rules="addTestPlanModelRules"
        ref="addTestPlanModel"
        :model="addTestPlanModel"
        label-position="right"
        label-width="18%"
      >
        <el-form-item prop="planName" label="计划名称:">
          <el-input v-model="addTestPlanModel.planName" maxlength="90" />
        </el-form-item>
        <el-form-item label="开始时间:" prop="planStartDate">
          <el-date-picker
            v-model="addTestPlanModel.planStartDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="选择开始日期"
            :picker-options="startDatePicker"
          />
        </el-form-item>
        <el-form-item label="结束时间:" prop="planEndDate">
          <el-date-picker
            v-model="addTestPlanModel.planEndDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="选择结束日期"
            :picker-options="endDatePicker"
          />
        </el-form-item>
        <el-form-item prop="deviceInfo" label="设备信息">
          <el-input v-model="addTestPlanModel.deviceInfo" maxlength="90" />
        </el-form-item>
      </el-form>
      <el-button
        type="primary"
        slot="footer"
        class="full-width"
        @click="inputAddPlan('addTestPlanModel')"
        >确 定</el-button
      >
    </Dialog>

    <!-- 打回工单弹框 -->
    <Dialog title="提测打回页面" :visible.sync="dialogRevert">
      <el-form
        label-position="left"
        label-width="50%"
        :model="revertOrderModel"
        :rules="revertOrderModelRules"
        ref="revertOrderModel"
      >
        <t-select
          label="任务"
          :options="taskOptions"
          v-model="revertOrderModel.taskNo"
          placeholder="请选择任务"
          prop="taskNo"
        />
        <t-select
          label="打回原因"
          :options="revertOptions"
          v-model="revertOrderModel.reason"
          placeholder="请选择打回原因"
          prop="reason"
        />
        <el-form-item label="详细说明">
          <el-input
            type="textarea"
            :autosize="{ minRows: 3 }"
            placeholder="请输入内容"
            v-model="revertOrderModel.detailInfo"
          />
        </el-form-item>
      </el-form>

      <el-button
        type="primary"
        slot="footer"
        class="full-width"
        :loading="loadingBtn"
        @click="revertOrder"
      >
        确定
      </el-button>
    </Dialog>
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
              v-for="(item, index) in groupList"
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
            multiple
            :remote-method="remoteMethod"
            :loading="loading"
            remote
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
  dateFormat,
  validate,
  deepClone,
  getUserListByRole,
  handleDocDataRqr
} from '@/common/utlis';
import { mapState, mapActions } from 'vuex';
import orderMenuTab from './orderMenuTab';
import { queryAllGroup } from '@/services/useradmin';
import OrderCard from '@/components/OrderCard';
import moment from 'moment';
import {
  orderStage,
  stageOptions,
  sitFlagOptions,
  revertOptions,
  selectMyOrders,
  addOrderModel,
  updateOrderModel,
  detailOrder,
  assignOrderModel,
  addTestPlanModel,
  rules,
  testReasonStage,
  revertOrderModel,
  orderTypeList
} from './model';
export default {
  name: 'TestOrder',
  components: { orderMenuTab, OrderCard, TaskListDialog },
  data() {
    return {
      orderTypeList: orderTypeList,
      isOtherGroup: '',
      showCallBack: false,
      callBackworkNo: '',
      taskListDialogDetail: [],
      taskListDialog: false,
      loadingBtn: false,
      dialogConcat: false,
      OrderSplitNo: '',
      OrderConcatNo: '',
      showTaskName: [],
      workOrderSplitModel: {
        taskNo: [],
        testerNames: [],
        splitTaskName: '',
        selectedWorkNo: ''
      },
      workOrderConcatModel: {
        worksNo: [],
        concatTaskName: ''
      },
      dialogSplit: false,
      taskOptionsSplit: [],
      taskOptionsConcat: [],
      taskOptions: [],
      pagination: {
        pageSize: 4,
        total: 0,
        currentPage: sessionStorage.getItem('currentPage')
          ? Number(sessionStorage.getItem('currentPage'))
          : 1
      },
      myTestOrderPagination: {
        pageSize: 8,
        total: 0,
        currentPage: JSON.parse(sessionStorage.getItem('cachePage')) || 1
      },
      noticePagination: {
        pageSize: 8,
        total: 0,
        currentPage: 1
      },
      stageOptions: stageOptions,
      sitFlagOptions: sitFlagOptions,
      props: {
        children: 'children',
        label: 'label'
      },
      propsRqr: {
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
      revertWorkOrderNo: '',
      dialogRevert: false,
      revertOptions: revertOptions,
      planPower: true,
      userEnName: '' + sessionStorage.getItem('user_en_name'),
      showNotice: false,
      dialogCardVisible: false,
      selectMyOrders: selectMyOrders(),
      groupNameList: [],
      dialogNoticeTable: {
        taskName: ''
      },
      userRole: sessionStorage.getItem('Trole'),
      formInline: {
        taskName: ''
      },
      isallot: true, //分配按钮是否可用
      addOrderDialogShow: false,
      addOrderModel: addOrderModel(),

      updateOrderDialogShow: false,
      updateOrderModel: updateOrderModel(),

      detailOrderDialogShow: false,
      //工单详情
      detailOrder: detailOrder(),
      detailNotify: {},
      dialogNotifyDetail: false,
      assignOrderDialogShow: false,
      assignOrderModel: assignOrderModel(),
      userManager: [
        {
          role_en_name: 'admin',
          user_name_en: sessionStorage.getItem('user_en_name'),
          user_name_cn: sessionStorage.getItem('TuserName')
        }
      ],
      addTestPlanModel: addTestPlanModel(),
      //弹框
      dialogAdd: false,
      startDatePicker: this.beginDate(),
      endDatePicker: this.processDate(),
      btnShowArr: ['1', '2', '3', '6', '9', '10', '12', '13'],
      loading: false,
      myOrderLoading: false,
      canEdit: true,
      revertOrderModel: revertOrderModel(),
      tableShow: false,
      showLoading: false,
      groupSort: '',
      stageSort: '',
      curThead: [],
      managers: [],
      groupLeaders: [],
      testers: []
    };
  },
  methods: {
    ...mapActions('workOrderForm', [
      'queryLastTransFilePath',
      'oauthLogin',
      'queryAssignOrderImpl',
      'queryMyOrderImpl',
      'iWantToassignImpl',
      'queryAllUserImpl',
      'submitAddOrderFormImpl',
      'submitOrderFormImpl',
      'updateOrderImpl',
      'queryMessageImpl',
      'queryAssessorList',
      'selectNameImpl',
      'ignoreMessageImpl',
      'queryMessageCount',
      'queryTaskDoc',
      'ignoreAllMessageImpl',
      'rollBackWorkOrder',
      'workOrderRollback',
      'queryTotalImpl',
      'addPlan',
      'queryWorkOrderStageList',
      'queryRqrFilesByOrderNo',
      'preview',
      'exportUserAllOrder',
      'downExcel',
      'queryTaskList',
      'verifyOrderName',
      'queryTasks',
      'queryMergeOrderList',
      'mergeWorkOrder',
      'splitWorkOrder',
      'queryFdevTaskByWorkNo',
      'queryWorkOrderName',
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
        await this.queryMyOrders();
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
    ...mapActions('adminForm', ['queryAllUserName']),
    // 卡片表格转化时,默认每页显示8条
    switchTable() {
      this.tableShow = !this.tableShow;
      this.myTestOrderPagination.pageSize = 8;
      this.queryMyOrders();
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
    orderStage(stage) {
      return orderStage[stage.trim()];
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
    // 通知列表弹框 测试原因转译
    formatterTaskName(taskReason) {
      return testReasonStage[taskReason] || '-';
    },
    //查询所有组,去除"资源池"这一条
    queryGroupList() {
      queryAllGroup({ status: 1 }).then(res => {
        this.groupNameList = res;
      });
    },
    closeNotice() {
      this.showNotice = false;
    },
    handleSizeChange3(size) {
      this.noticePagination.pageSize = size;
      this.queryMessageList();
    },
    // 通知分页
    handleCurrentChange3(val) {
      this.noticePagination.currentPage = val;
      this.queryMessageList();
    },
    //

    // 任务名称
    taskNameDialog(taskName) {
      this.dialogNotifyDetail = true;
      this.detailNotify = taskName;
    },
    //工单详情orderDetail
    async orderDetail(row, canEdit) {
      this.init();
      let param = {
        currentPage: 1,
        pageSize: 50,
        taskName: '',
        workNo: row.workOrderNo
      };
      await this.queryTaskList(param);
      this.canEdit = canEdit;
      this.detailOrderDialogShow = true;
      this.detailOrder = {
        ...row,
        approver: row.field2
      };
      await this.getDoc();
      await this.getDocRqr();
    },
    async getDoc() {
      this.showLoading = true;
      let docList = [];
      this.docData = [];
      if (this.detailOrder.mainTaskNo || this.detailOrder.workOrderNo) {
        try {
          await this.queryTaskDoc({
            id: this.detailOrder.mainTaskNo
              ? this.detailOrder.mainTaskNo
              : this.detailOrder.workOrderNo
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
            label: this.detailOrder.mainTaskName,
            children: this.taskDoc
          });
          docList.push(this.taskDoc);
        }
        if (this.detailOrder.orderType == 'security') {
          try {
            await this.queryLastTransFilePath({
              workNo: this.detailOrder.workOrderNo
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
              label: this.detailOrder.mainTaskName,
              children: securityDoc
            });
            docList.push(securityDoc);
          }
        }
        //一工单多任务
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
          workNo: this.detailOrder.workOrderNo
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
    init() {
      this.rqrDocData = [];
    },
    closeDetailDia() {
      this.detailOrderDialogShow = false;
    },
    closeUpdateDia() {
      this.detailOrderDialogShow = true;
      this.updateOrderDialogShow = false;
    },
    //工单详情-修改页面 打开按钮事件
    async updateBegin() {
      this.queryWorkOrderStageList({
        workNo: this.detailOrder.workOrderNo,
        groupId: this.detailOrder.fdevGroupId
      });
      this.updateOrderModel = {
        ...deepClone(this.detailOrder),
        planProDate: this.detailOrder.planProDate
          ? new Date(this.detailOrder.planProDate)
          : '',
        workManager: this.detailOrder.workManager.user_name_en,
        groupLeader: this.detailOrder.groupLeader
          ? this.detailOrder.groupLeader.map(user => user.user_name_en)
          : [],
        testers: this.detailOrder.testers
          ? this.detailOrder.testers.map(user => user.user_name_en)
          : [],
        approver: this.detailOrder.approver
          ? this.detailOrder.approver.map(user => user.user_name_en)
          : [],
        riskDescription: this.detailOrder.riskDescription
          ? this.detailOrder.riskDescription.split(',')
          : [],
        groupId: this.detailOrder.fdevGroupId
      };
      this.updateOrderDialogShow = true;
      this.detailOrderDialogShow = false;
    },
    //已分配工单-关闭分配页面 事件
    closeAssignDia() {
      this.assignOrderModel.groupLeader = [];
      this.assignOrderModel.testers = [];
      this.assignOrderModel.groupId = '';
      this.assignOrderDialogShow = false;
    },

    //我想分配 按钮事件
    async iWantToAssign(item) {
      await this.iWantToassignImpl({
        orderId: item.workOrderNo
      });
      this.queryAssignOrder();
      this.myTestOrderPagination.currentPage = 1;
      this.queryMyOrder();
      this.queryTotal(this.formInline.taskName);
    },
    //查询测试人员及小组长
    queryAllUser() {
      this.queryAllUserName();
    },

    //第三方获取用户信息
    async oauthGetUser(token) {
      await this.oauthLogin({
        token
      });
      localStorage.setItem('userToken', this.oauth.userToken);
      localStorage.setItem('Trole', this.oauth.userRole);
    },
    //验证任务名是否重复
    async verifyTaskName() {
      const param = {
        mainTaskName: this.addOrderModel.mainTaskName
      };
      await this.verifyOrderName(param);
      if (
        this.verifyOrderNameBoolean === false &&
        this.addOrderModel.mainTaskName != ''
      ) {
        this.$message({
          showClose: true,
          message: '测试任务名已存在！',
          type: 'error'
        });
        return;
      }
    },
    //新增工单表单提交
    async submitAddOrderForm(addOrderModels) {
      await validate(this.$refs[addOrderModels]);
      const params = {
        ...this.addOrderModel,
        testers: this.addOrderModel.testers.join(','),
        groupLeader: this.addOrderModel.groupLeader.join(','),
        remark: this.addOrderModel.remark.trim()
      };
      await this.submitAddOrderFormImpl(params);
      this.$message({
        showClose: true,
        message: '新增成功',
        type: 'success'
      });
      //需要更新数据，重新获取界面数据
      this.queryMyOrder();
      this.queryTotal(this.formInline.taskName);
      this.addOrderDialogShow = false;
      this.addOrderModel = addOrderModel();
    },
    //分配-确定按钮事件
    async submitOrderForm(assignOrderModel) {
      await validate(this.$refs[assignOrderModel]);
      const {
        testers,
        groupLeader,
        groupName,
        mainTaskNo,
        workOrderNo
      } = this.assignOrderModel;
      const params = {
        workOrderNo: workOrderNo,
        mainTaskNo: mainTaskNo,
        testers: testers.join(','),
        groupLeader: groupLeader.join(','),
        groupId: groupName
      };
      await this.submitOrderFormImpl(params);
      //需要更新数据，重新获取界面数据
      this.queryMyOrder();
      this.$message({
        showClose: true,
        message: '分配成功',
        type: 'success'
      });
      this.assignOrderDialogShow = false;
    },
    //新增工单
    addOrder() {
      this.addOrderDialogShow = true;
    },
    //工单修改状态为：UAT状态时，内部测试完成时间，默认回显当日，支持修改
    isUatShowNowDate() {
      this.updateOrderModel.uatSubmitDate = '';
      if (this.updateOrderModel.stage === '3') {
        this.updateOrderModel.uatSubmitDate = new Date();
      }
    },
    //修改工单
    async updateOrder(updateOrderModel) {
      await validate(this.$refs[updateOrderModel]);
      const params = {
        ...this.updateOrderModel,
        workManager: this.updateOrderModel.workManager,
        aduits: this.updateOrderModel.approver,
        groupId: this.updateOrderModel.groupId
      };
      if (
        params.stage !== '6' &&
        params.stage !== '10' &&
        params.stage !== '13'
      ) {
        delete params.riskDescription;
      }
      if (this.updateOrderModel.stage === '3') {
        params.uatSubmitDate = dateFormat(
          this.updateOrderModel.uatSubmitDate,
          '/'
        );
      }
      await this.updateOrderImpl(params);
      //需要更新数据，重新获取界面数据
      this.queryMyOrder();
      //数据传递
      this.updateOrderDialogShow = false;

      this.$message({
        showClose: true,
        message: '修改成功',
        type: 'success'
      });
    },
    //分配-按钮事件
    assign({ groupLeader, testers, fdevGroupId, mainTaskNo, workOrderNo }) {
      this.assignOrderModel = {
        groupLeader: groupLeader.map(user => user.user_name_en),
        testers: testers.map(user => user.user_name_en),
        groupName: fdevGroupId,
        mainTaskNo: mainTaskNo,
        workOrderNo: workOrderNo
      };
      this.assignOrderDialogShow = true;
    },
    /**
     * 跳转缺陷列表
     * */
    goMantisList(item) {
      var workOrderInfo = [
        { workOrderNo: item.workOrderNo, mainTaskName: item.mainTaskName }
      ];
      sessionStorage.setItem('workOrderInfo', JSON.stringify(workOrderInfo));
      this.$router.push({
        name: 'MantisIssue',
        query: { workOrderInfo: workOrderInfo }
      });
    },
    /**
     * 跳转任务列表
     * */
    goTaskList(item) {
      window.sessionStorage.setItem('childrenItem', JSON.stringify(item));
      var task = { workOrderNo: item.workOrderNo };
      this.$router.push({ name: 'TaskList', query: { item: task } });
    },

    /**
     * 跳转执行计划
     * */
    exePlan(item) {
      const testersArr = item.testers.map(user => user.user_name_cn);
      const testersStr = testersArr.toString();
      this.$router.push({
        name: 'Plan',
        query: { workOrderNo: item.workOrderNo }
      });
      const workOrderNoData = {
        stage: item.stage,
        workOrderNo: item.workOrderNo,
        testers: testersStr,
        mainTaskName: item.mainTaskName,
        unitNo: item.unit,
        planPower: this.planPower,
        workNo: item.workOrderNo
      };
      sessionStorage.setItem(
        'planWorkOrderNo',
        JSON.stringify(workOrderNoData)
      );
    },

    //查询工单（模糊查询）
    queryOrder() {
      this.pagination.currentPage = 1;
      this.myTestOrderPagination.currentPage = 1;
      this.queryAllUser();
      this.queryTotal(this.formInline.taskName);
      this.queryAssignOrder();
      this.queryMyOrder();
    },
    //查询总数为了分页--1、待分配工单的分页；2、已分配工单的分页
    async queryTotal(taskName) {
      await this.queryTotalImpl({
        stage: this.selectMyOrders.stage,
        person: this.selectMyOrders.userEnName,
        sitFlag: this.selectMyOrders.sitFlag, // 查询fdev已提内测的工单总页数不同
        taskName: taskName,
        userRole: this.userRole,
        orderType: this.selectMyOrders.orderType
      });
      this.pagination.total = this.count.total1;
      this.myTestOrderPagination.total = this.count.total2;
    },
    //分配工单下一页
    handleCurrentChange(val) {
      this.pagination.currentPage = val;
      this.queryAssignOrder();
    },
    //查询待分配的工单（模糊查询）
    async queryAssignOrder() {
      this.loading = true;
      await this.queryAssignOrderImpl({
        taskName: this.formInline.taskName,
        pageSize: this.pagination.pageSize,
        currentPage: this.pagination.currentPage
      });
      this.loading = false;
    },
    handleSizeChange: function(size) {
      this.myTestOrderPagination.pageSize = size;
      this.queryMyOrders();
    },
    //你的工单下一页
    handleCurrentChange2(val) {
      this.myTestOrderPagination.currentPage = val;
      this.queryMyOrder();
    },

    //查询通知列表表单
    async queryMessageList() {
      await this.queryMessageImpl({
        user_en_name: this.userEnName,
        ...this.noticePagination
      });
      this.dialogNoticeTable = this.messageImpl;
    },
    async handleIgnore(index, row) {
      await this.$confirm('此操作将忽略该文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.ignoreMessageImpl({
        messageId: row.messageId
      });
      this.dialogNoticeTable.splice(index, 1);
      this.$message({
        type: 'success',
        message: '忽略成功!'
      });
      await this.queryMessageCount({
        user_en_name: this.userEnName
        // (!this.userEnName)?this.userEnName:''
      });
      this.noticePagination.total = this.messageCount;
      if (this.noticePagination.total == null) {
        this.showNotice = false;
      }
      this.queryMessageList();
    },
    async handleAllIgnore() {
      await this.$confirm('此操作将忽略全部文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await this.ignoreAllMessageImpl({
        user_en_name: this.userEnName
      });
      this.dialogNoticeTable = [];
      this.$message({
        type: 'success',
        message: '忽略成功!'
      });
      this.queryMessageList();
      this.showNotice = false;
    },

    //新增计划(四)
    async inputAddPlan(ref) {
      await validate(this.$refs[ref]);
      await this.addPlan(this.addTestPlanModel);
      this.dialogAdd = false;
      this.$message({
        showClose: true,
        message: '新增成功',
        type: 'success'
      });
    },
    // 新增计划弹框开始结束时间控制
    beginDate() {
      const self = this;
      return {
        disabledDate(time) {
          if (self.inputEndTime) {
            //如果结束时间不为空，则小于结束时间
            return new Date(self.inputEndTime).getTime() < time.getTime();
          } else {
            // return time.getTime() > Date.now()//开始时间不选时，结束时间最大值小于等于当天
          }
        }
      };
    },
    //审批人员下拉选项
    approvalSelect() {
      this.queryAssessorList();
    },
    //回退工单
    async backOrder(item) {
      try {
        await this.$confirm('此操作将撤回工单, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        await this.rollBackWorkOrder({
          workOrderNo: item.workOrderNo
        });
        this.$message({
          showClose: true,
          message: '撤回成功',
          type: 'success'
        });
        this.queryOrder();
      } catch (e) {}
    },
    processDate() {
      const self = this;
      return {
        disabledDate(time) {
          if (self.inputStartTime) {
            //如果开始时间不为空，则结束时间大于开始时间
            return new Date(self.inputStartTime).getTime() > time.getTime();
          } else {
          }
        }
      };
    },
    handleAddPlan(row) {
      this.dialogAdd = true;
      this.addTestPlanModel = addTestPlanModel();
      this.addTestPlanModel.workNo = row.workNo;
    },
    //我的工单查询
    queryMyOrders() {
      this.myTestOrderPagination.currentPage = 1;
      this.queryMyOrder();
      this.queryTotal(this.formInline.taskName);
    },
    async downloadOrder() {
      let params = {
        ...this.selectMyOrders,
        taskName: this.formInline.taskName,
        userRole: this.userRole,
        person: this.selectMyOrders.userEnName,
        orderType: this.selectMyOrders.orderType
      };
      await this.exportUserAllOrder(params);
      let fileName = this.allOrder.headers['content-disposition'];
      const fileType = this.allOrder.headers['content-type'];
      fileName = fileName.substring(fileName.indexOf('=') + 1);
      const blob = new Blob([this.allOrder.data], { type: fileType });
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
    },
    //初始化下拉选项
    selectNameData() {
      this.selectNameImpl();
    },
    isAuthUser(item) {
      const { stage, groupLeader, workManager, workOrderFlag, sitFlag } = item;
      if (
        (stage !== '1' && stage !== '2') ||
        workOrderFlag === '0' ||
        sitFlag !== '1'
      )
        return false;
      // 工单状态为‘开发中’和‘sit’阶段且用户是工单的测试组长或者工单负责人可执行打回工单操作
      // 判断当前用户是否是工单负责人或者测试组长
      const manager = [...groupLeader, workManager];
      const username = localStorage.getItem('user_en_name');

      return manager.some(user => user.user_name_en === username);
    },
    async dialogRevertDialog(item) {
      this.dialogRevert = true;
      this.revertOrderModel = revertOrderModel();
      this.revertOrderModel.workNo = item.workOrderNo;
      //请求任务接口
      await this.queryTasks({
        workNo: item.workOrderNo
      });
      this.taskOptions = this.taskAllData.taskList.map(item => {
        return { label: item.name, value: item.id };
      });
    },
    // 打回工单
    async revertOrder(item) {
      await validate(this.$refs['revertOrderModel']);
      this.loadingBtn = true;
      try {
        // 打回原因
        await this.workOrderRollback(this.revertOrderModel);
        this.loadingBtn = false;
        this.$message({
          showClose: true,
          message: '打回成功',
          type: 'success'
        });
        this.dialogRevert = false;
        // 注意打回成功之后工单变为开发中，已提内测标志取消 发通知给fdev任务相关人员
        this.queryMyOrder();
      } catch (error) {
        this.loadingBtn = false;
        throw error;
      }
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
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
    async queryMyOrder() {
      this.myOrderLoading = true;
      let param = {};
      if (this.groupSort) {
        param.groupSort = this.groupSort;
      }
      if (this.stageSort) {
        param.stageSort = this.stageSort;
      }
      await this.queryMyOrderImpl({
        person: this.selectMyOrders.userEnName,
        stage: this.selectMyOrders.stage,
        sitFlag: this.selectMyOrders.sitFlag,
        taskName: this.formInline.taskName,
        orderType: this.selectMyOrders.orderType,
        userRole: this.userRole,
        ...this.myTestOrderPagination,
        ...param
      });
      this.myOrderLoading = false;
    },
    async changeTableSort(row) {
      let index = this.curThead.findIndex(item => {
        return item.prop == row.prop;
      });
      if (index > -1) {
        this.curThead[index].order = row.order;
      } else {
        this.curThead.push({ prop: row.prop, order: row.order });
      }
      let param = {
        person: this.selectMyOrders.userEnName,
        stage: this.selectMyOrders.stage,
        sitFlag: this.selectMyOrders.sitFlag,
        taskName: this.formInline.taskName,
        userRole: this.userRole,
        ...this.myTestOrderPagination
      };
      this.myOrderLoading = true;
      // 如果是按组名排序
      if (row.prop === 'groupName') {
        if (row.order === 'descending') {
          this.groupSort = 'desc';
        } else if (row.order === 'ascending') {
          this.groupSort = 'asc';
        } else {
          this.groupSort = null;
        }
      }

      if (row.prop === 'stage') {
        // 如果是按状态排序
        if (row.order === 'descending') {
          this.stageSort = 'desc';
        } else if (row.order === 'ascending') {
          this.stageSort = 'asc';
        } else {
          this.stageSort = null;
        }
      }
      if (this.groupSort) {
        param.groupSort = this.groupSort;
      }
      if (this.stageSort) {
        param.stageSort = this.stageSort;
      }
      await this.queryMyOrderImpl(param);
      this.myOrderLoading = false;
    },
    handleTheadStyle({ row, column, rowIndex, columnIndex }) {
      let index = this.curThead.findIndex(item => {
        return item.prop == column.property;
      });
      if (index > -1) {
        column.order = this.curThead[index].order;
      }
    },
    getContent(val) {
      let res = [];
      val.forEach(item => {
        if (item) {
          res.push(item.user_name_cn);
        }
      });
      return res.join(' ');
    }
  },
  filters: {
    filterDate(val) {
      if (val) {
        return moment(val).format('YYYY-MM-DD');
      } else {
        return '';
      }
    },
    testerFilter(users) {
      if (!users || typeof user === 'string') {
        return users;
      }
      return users.join(',');
    }
  },
  async mounted() {
    await this.queryAllUserName();
    this.managers = await getUserListByRole(
      ['玉衡-测试管理员', '玉衡超级管理员'],
      this.userList
    );
    this.groupLeaders = await getUserListByRole(
      [
        '玉衡-测试组长',
        '玉衡-测试管理员',
        '玉衡超级管理员',
        '玉衡-安全测试组长'
      ],
      this.userList
    );
    this.testers = await getUserListByRole(
      [
        '测试人员',
        '玉衡-测试管理员',
        '玉衡超级管理员',
        '玉衡-测试组长',
        '玉衡-安全测试组长'
      ],
      this.userList
    );
    this.isallot = !(this.userRole >= 30);
    //如果权限比组员20还小，则自动进入查询页面
    if (this.userRole < 20) {
      this.$router.push({ path: '/QueryOrder' }); //工单查询
    }
    if (JSON.parse(sessionStorage.getItem('pageInfoTest'))) {
      this.selectMyOrders = JSON.parse(sessionStorage.getItem('pageInfoTest'));
    }
    //初始化选中功能测试
    if (!this.selectMyOrders.orderType) {
      this.selectMyOrders.orderType = 'function';
    }
    this.formInline.taskName = sessionStorage.getItem('taskName')
      ? sessionStorage.getItem('taskName')
      : '';
    this.myTestOrderPagination.currentPage = sessionStorage.getItem('cachePage')
      ? Number(sessionStorage.getItem('cachePage'))
      : 1;
    this.pagination.currentPage = sessionStorage.getItem('currentPage')
      ? Number(sessionStorage.getItem('currentPage'))
      : 1;

    if (
      JSON.parse(sessionStorage.getItem('pageInfoTest')) ||
      sessionStorage.getItem('taskName') ||
      sessionStorage.getItem('cachePage') ||
      sessionStorage.getItem('currentPage')
    ) {
      this.queryMyOrder();
      this.queryTotal(this.formInline.taskName);
      this.queryAssignOrder();
    } else {
      this.queryOrder(); //初始化加载所有工单
    }
    this.queryGroupList();
    this.selectNameData();
    this.approvalSelect();
    this.queryMessageList();
    this.queryMessageCount({
      user_en_name: this.userEnName
    }).then(() => {
      this.noticePagination.total = this.messageCount;
      if (this.noticePagination.total > 0) {
        this.showNotice = true;
      }
    });
  },

  computed: {
    ...mapState('workOrderForm', [
      'filePath',
      'oauth',
      'assignOrderImpl',
      'myOrderImpl',
      'messageImpl',
      'assessorList',
      'groupList',
      'messageCount',
      'count',
      'taskDoc',
      'wordOrderStage',
      'rqrFiles',
      'previewUrl',
      'allOrder',
      'childTaskList',
      'verifyOrderNameBoolean',
      'taskDoc',
      'rqrFilesByOrderNo',
      'taskAllData',
      'mergeOrderList',
      'fdevTaskByWorkNo',
      'defaultWorkOrderName',
      'splitWorkOrderList'
    ]),
    ...mapState('adminForm', ['userList']),
    switchCard() {
      if (this.tableShow) {
        return 'card-style';
      } else {
        return false;
      }
    },
    switchChart() {
      if (!this.tableShow) {
        return 'card-style';
      } else {
        return false;
      }
    },
    // 所属小组修改权限:工单负责人或者超级管理员
    groupDisabled() {
      if (!this.detailOrder.workManager) {
        return true;
      } else if (
        this.userRole > 40 ||
        this.userManager[0].user_name_en ===
          this.detailOrder.workManager.user_name_en
      ) {
        return false;
      } else {
        return true;
      }
    },
    workOrderSplitModelRules() {
      return this.rules(this.workOrderSplitModel);
    },
    workOrderConcatModelRules() {
      return this.rules(this.workOrderConcatModel);
    },
    addOrderModelRules() {
      return this.rules(this.addOrderModel);
    },
    updateOrderModelRules() {
      return this.rules(
        this.updateOrderModel,
        this.needCheckDateTime ? [] : ['uatSubmitDate']
      );
    },
    addTestPlanModelRules() {
      return this.rules(this.addTestPlanModel);
    },
    assignOrderModelRules() {
      return this.rules(this.assignOrderModel);
    },
    revertOrderModelRules() {
      return this.rules(this.revertOrderModel);
    },
    tables() {
      const search = this.search;
      if (search) {
        return this.dialogNoticeTable.filter(dataNews => {
          return Object.keys(dataNews).some(key => {
            return (
              String(dataNews[key])
                .toLowerCase()
                .indexOf(search) > -1
            );
          });
        });
      }
      return this.dialogNoticeTable;
    },
    needCheckDateTime() {
      if (
        this.updateOrderModel.stage == 3 ||
        this.updateOrderModel.stage == 9
      ) {
        return true;
      } else {
        return false;
      }
    }
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
    // 检测表格数据过滤变化，自动跳到第一页
    tables() {
      this.currentPage = 1;
    },
    // 当工单修改弹窗关闭时，所属小组设为空，目的使groupId的oldVal为空
    updateOrderDialogShow(val) {
      if (!val) {
        this.updateOrderModel.groupId = '';
      }
    },
    'updateOrderModel.groupId': {
      async handler(val, oldVal) {
        // 第一次打开工单修改弹窗时不发送接口查测试阶段的值，不清空测试阶段
        if ((oldVal && val) || oldVal === null) {
          await this.queryWorkOrderStageList({
            workNo: this.detailOrder.workOrderNo,
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
    if (from.path === '/') {
      sessionStorage.removeItem('cachePage');
      sessionStorage.removeItem('currentPage');
      sessionStorage.removeItem('pageInfoTest');
      sessionStorage.removeItem('taskName');
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    sessionStorage.setItem('cachePage', this.myTestOrderPagination.currentPage);
    sessionStorage.setItem('currentPage', this.pagination.currentPage);
    sessionStorage.setItem('pageInfoTest', JSON.stringify(this.selectMyOrders));
    sessionStorage.setItem('taskName', this.formInline.taskName);
    next();
  }
};
</script>

<style scoped>
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
.title {
  font-size: 20px;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 10px;
  text-align: left;
}
.card-container {
  margin-bottom: 20px;
}
.card-notice {
  width: 260px;
  position: fixed;
  right: 0;
  bottom: 0;
}
.card-notice >>> .el-card box-card is-always-shadow {
  height: 260px;
}
.card-notice >>> .el-card__header {
  padding: 15px 20px;
  background: #f8f7f7;
}
.card-notice >>> .el-icon-close:before {
  font-size: 20px;
  color: gray;
  cursor: pointer;
}

.pagination {
  margin-top: 3%;
  margin-bottom: 8%;
}
.pagination >>> .el-pagination {
  float: right !important;
}
.icon {
  position: relative;
  top: -4px;
  color: yellow;
}
.icon-plan {
  position: absolute;
  top: 4px;
  color: yellow;
}
.tree-style >>> .is-leaf + .el-tree-node__label {
  color: #2196f3 !important;
  display: inline-block;
  width: 100%;
  text-overflow: ellipsis !important;
  overflow: hidden !important;
}
.el-link {
  display: inline !important;
}
.card {
  margin-top: 15px;
}
.desc-margin {
  margin-top: 5px;
}
.el-date-editor.el-input {
  width: 100%;
}
.notice-body {
  padding: 20px 0;
}
.rqr-list {
  color: #2196f3 !important;
  cursor: pointer;
}
.norqr-list {
  margin-left: 23px;
}
.weight {
  font-weight: 700;
}
.btn-group {
  padding: 0 15px 0 15px;
}
.card-style {
  background: white;
  color: #2196f3;
}
.switch {
  padding: 11px;
}
.bottomItem {
  margin-bottom: 0 !important;
}
.el-table >>> .cell {
  white-space: nowrap !important;
}
.tooltip {
  width: 100% !important;
  overflow: hidden;
  text-overflow: ellipsis;
}
.tooltip >>> .el-link--inner {
  width: 100% !important;
  white-space: nowrap !important;
  overflow: hidden;
  text-overflow: ellipsis;
}
.select-width {
  width: 240px;
}
.task-btn {
  width: 80px;
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
.textStyle {
  font-family: PingFangSC-Semibold;
  font-size: 12px;
  color: #ffffff;
  letter-spacing: 0;
  line-height: 22px;
  font-weight: 600;
}
.fontweight {
  font-weight: 600;
}
</style>
