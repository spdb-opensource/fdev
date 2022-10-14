<template>
  <div>
    <!-- 等待层 -->
    <Loading :visible="loadingStatus">
      <div class="items-start q-gutter-md">
        <f-block block>
          <!-- 业务需求 -->
          <common-title class="q-mt-md" icon-name="list_s_f" label="基础信息" />
          <div class="row justify-between q-ml-lg m-87" v-if="isBusinessFun()">
            <!-- 需求编号 -->
            <f-formitem label="需求编号" label-style="width: 112px">
              <!-- v => regRule(v) || addRule(v) || '输入格式不正确', -->
              <fdev-input
                @blur="checkOANum"
                :readonly="isEditReadonly"
                placeholder=" "
                v-model="editModel.oa_contact_no"
                ref="editModel.oa_contact_no"
                :rules="[v => useRule(v) || '当前需求编号已被使用，请重新申请']"
              />
            </f-formitem>
            <!-- 需求标题 -->
            <f-formitem label="需求标题" required label-style="width: 112px">
              <fdev-input
                ref="editModel.oa_contact_name"
                v-model="editModel.oa_contact_name"
                :readonly="
                  demandInfoDetail && demandInfoDetail.demand_flag === 1
                "
                :rules="[v => !!v || '请输入需求标题']"
              />
            </f-formitem>
            <!-- 需求书名称 -->
            <f-formitem label="需求书名称" label-style="width: 112px">
              <fdev-input
                v-model="editModel.demand_instruction"
                :readonly="isEditReadonly"
                placeholder=" "
                hint=""
              />
            </f-formitem>
            <!-- 需求提出部门 -->
            <f-formitem label="需求提出部门" label-style="width: 112px">
              <fdev-input
                v-model="editModel.propose_demand_dept"
                :readonly="isEditReadonly"
                placeholder=" "
                hint=""
              />
            </f-formitem>
            <!-- 需求计划名称 -->
            <f-formitem label="需求计划名称" label-style="width: 112px">
              <fdev-input
                :readonly="isEditReadonly"
                placeholder=" "
                v-model="editModel.demand_plan_name"
                hint=""
              />
            </f-formitem>
            <!-- 需求我部收件日期 -->
            <f-formitem label-style="width: 112px" label="需求我部收件日期">
              <f-date
                v-model="editModel.oa_receive_date"
                :disable="isEditReadonly"
                placeholder=" "
                hint=""
              />
            </f-formitem>
            <!-- 实施单元跟踪人 -->
            <f-formitem label-style="width: 112px" label="实施单元跟踪人">
              <fdev-input
                :readonly="isEditReadonly"
                placeholder=" "
                v-model="editModel.impl_track_user"
                hint=""
              />
            </f-formitem>
            <!-- 对应需求计划编号 -->
            <f-formitem label-style="width: 112px" label="对应需求计划编号">
              <fdev-input
                v-model="editModel.demand_plan_no"
                :readonly="isEditReadonly"
                placeholder=" "
                :rules="[
                  v =>
                    demandPlanNoRegRule(v) ||
                    '输入格式不正确（样例：2020零售信贷038）'
                ]"
              />
            </f-formitem>
            <!-- 是否涉及UI审核 -->
            <f-formitem label-style="width: 112px" label="是否涉及UI审核">
              <fdev-select
                :readonly="!editModel.ui_status"
                :placeholder="!editModel.ui_status ? ' ' : '请选择'"
                map-options
                emit-value
                v-model="editModel.ui_verify"
                :options="uiList"
                :hint="!editModel.ui_status ? '该状态无法选择' : ' '"
              />
            </f-formitem>
            <!-- 优先级 -->
            <f-formitem label-style="width: 112px" label="优先级">
              <fdev-select
                v-model="editModel.priority"
                :options="priorities"
                options-value="value"
                emit-value
                map-options
                hint=""
              />
            </f-formitem>
            <!-- 需求属性 -->
            <f-formitem label="需求属性" label-style="width: 112px">
              <fdev-select
                ref="editModel.demand_property"
                v-model="editModel.demand_property"
                :options="propertyOptions"
                hint=""
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.label">{{
                        scope.opt.label
                      }}</fdev-item-label>
                      <fdev-item-label :title="scope.opt.fullName" caption>
                        {{ scope.opt.fullName }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
            <!-- 需求说明 -->
            <f-formitem full-width label-style="width: 112px" label="需求说明">
              <fdev-input
                v-model="editModel.demand_desc"
                type="textarea"
                hint=""
              />
            </f-formitem>
          </div>
          <!-- 科技需求 -->
          <div class="row justify-between q-ml-lg m-87" v-if="isTechFun()">
            <!-- 需求说明书名称 -->
            <f-formitem
              label-style="width: 112px"
              label="需求说明书名称"
              required
            >
              <fdev-input
                ref="editModel.demand_instruction"
                v-model="editModel.demand_instruction"
                :rules="[v => !!v || '请输入需求说明书名称']"
              />
            </f-formitem>
            <!-- 是否涉及UI审核 -->
            <f-formitem label-style="width: 112px" label="是否涉及UI审核">
              <fdev-select
                :readonly="!editModel.ui_status"
                :placeholder="!editModel.ui_status ? ' ' : '请选择'"
                map-options
                emit-value
                v-model="editModel.ui_verify"
                :options="uiList"
                :hint="!editModel.ui_status ? '该状态无法选择' : ' '"
              />
            </f-formitem>
            <!-- 优先级 -->
            <f-formitem label-style="width: 112px" label="优先级">
              <fdev-select
                v-model="editModel.priority"
                :options="priorities"
                hint=""
              />
            </f-formitem>
            <!-- 科技类型 -->
            <f-formitem label-style="width: 112px" label="科技类型">
              <fdev-select
                hint=""
                v-model="editModel.tech_type"
                :options="techTypes"
                @input="onChangeTechType"
              />
            </f-formitem>
            <!-- 备注 -->
            <f-formitem
              required
              label-style="width: 112px"
              label="备注"
              v-if="ifShowTechTypeFun(editModel.tech_type)"
            >
              <fdev-input
                ref="editModel.tech_type_desc"
                v-model="editModel.tech_type_desc"
                :rules="[v => !!v || '请输入科技类型备注']"
              />
            </f-formitem>
            <!-- 需求属性 -->
            <f-formitem label="需求属性" label-style="width: 112px">
              <fdev-select
                ref="editModel.demand_property"
                v-model="editModel.demand_property"
                :options="propertyOptions"
                hint=""
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.label">{{
                        scope.opt.label
                      }}</fdev-item-label>
                      <fdev-item-label :title="scope.opt.fullName" caption>
                        {{ scope.opt.fullName }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
            <!-- 需求说明 -->
            <f-formitem full-width label-style="width: 112px" label="需求说明">
              <fdev-input
                v-model="editModel.demand_desc"
                type="textarea"
                hint=""
              />
            </f-formitem>
          </div>
          <!-- 日常需求 -->
          <div class="row justify-between q-ml-lg m-87" v-if="isDailyFun()">
            <!-- 需求名称 -->
            <f-formitem
              label-style="width: 112px"
              label="需求名称"
              required
              class="col-12"
            >
              <fdev-input
                ref="editModel.oa_contact_name"
                v-model="editModel.oa_contact_name"
                :readonly="
                  demandInfoDetail && demandInfoDetail.demand_flag === 1
                "
                :rules="[v => !!v || '请输入需求名称']"
              />
            </f-formitem>
            <!-- 需求属性 -->
            <f-formitem label="需求属性" label-style="width: 112px">
              <fdev-select
                ref="editModel.demand_property"
                v-model="editModel.demand_property"
                :options="propertyOptions"
                hint=""
              >
                <template v-slot:option="scope" v-if="propertyOptions.length">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.label">{{
                        scope.opt.label
                      }}</fdev-item-label>
                      <fdev-item-label :title="scope.opt.fullName" caption>
                        {{ scope.opt.fullName }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
            <!-- 需求说明 -->
            <f-formitem
              label-style="width: 112px"
              label="需求说明"
              class="col-12"
            >
              <fdev-input
                v-model="editModel.demand_desc"
                type="textarea"
                hint=""
              />
            </f-formitem>
          </div>
          <div class="q-ml-lg m-87">
            <f-formitem label="需求标签" full-width label-style="width: 112px">
              <div class="row q-gutter-sm">
                <div
                  v-for="(item, index) in editModel.demand_label_info"
                  :key="index"
                >
                  <fdev-checkbox
                    keep-color
                    v-model="item.flag"
                    :color="item.color"
                    ><span class="labelSty" :style="'background:' + item.color">
                      {{ item.label }}
                    </span></fdev-checkbox
                  >
                </div>
              </div>
            </f-formitem>
          </div>
          <!-- 安排与实施 -->
          <common-title
            width="18"
            height="18"
            x="2"
            y="2"
            class="q-mt-md"
            icon-name="bell_s_f"
            label="安排与实施"
          />
          <div class="row justify-between q-ml-lg m-87">
            <!-- 牵头小组 -->
            <f-formitem label-style="width: 112px" label="牵头小组" required>
              <fdev-select
                ref="editModel.demand_leader_group"
                v-model="editModel.demand_leader_group"
                :options="leaderGroupOptions"
                use-input
                hint=""
                @filter="leaderGroupFilter"
                option-label="name"
                option-value="id"
                :rules="[v => !leaderGroupIsEmpty(v) || '请选择牵头小组']"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.name">
                        {{ scope.opt.name }}
                      </fdev-item-label>
                      <fdev-item-label :title="scope.opt.fullName" caption>
                        {{ scope.opt.fullName }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
            <!-- 牵头人 -->
            <f-formitem label-style="width: 112px" label="牵头人" required>
              <fdev-select
                multiple
                option-value="id"
                ref="editModel.demand_leader"
                v-model="editModel.demand_leader"
                :options="userFilterOptions"
                use-input
                @filter="userFilter"
                :rules="[v => !demandLeaderIsEmpty(v) || '请选择牵头人']"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.user_name_cn">
                        {{ scope.opt.user_name_cn }}
                      </fdev-item-label>
                      <fdev-item-label
                        :title="`${scope.opt.user_name_en}--${scope.opt.group}`"
                        caption
                      >
                        {{ scope.opt.user_name_en }}--{{ scope.opt.group }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
            <!-- 优先级 -->
            <f-formitem
              label-style="width: 112px"
              label="优先级"
              v-if="isDailyFun()"
            >
              <fdev-select
                v-model="editModel.priority"
                :options="priorities"
                options-value="value"
                emit-value
                map-options
                hint=""
              />
            </f-formitem>

            <!-- 受理日期 -->
            <f-formitem
              label-style="width: 112px"
              label="受理日期"
              v-if="isDailyFun()"
            >
              <f-date
                v-model="editModel.accept_date"
                mask="YYYY-MM-DD"
                :clearable="true"
                :hide-dropdown-icon="true"
                hint=""
              >
              </f-date>
            </f-formitem>
          </div>
          <!-- 评估安排 -->
          <common-title
            width="18"
            height="18"
            x="2"
            y="2"
            class="q-mt-md"
            icon-name="bell_s_f"
            label="评估安排"
          />
          <!-- 涉及小组 -->
          <div class="row justify-between q-ml-lg m-87">
            <f-formitem label="涉及小组" required label-style="width: 112px">
              <fdev-select
                multiple
                use-input
                new-value-mode="add-unique"
                ref="editModel.relate_part"
                v-model="editModel.relate_part"
                :options="relatedFilterGroup"
                @add="addRelatedGroups"
                @filter="relatedGroupFilter"
                @remove="removeRelatedGroup"
                :rules="[v => !relatePartisEmpty(v) || '请选择涉及小组']"
              >
                <template v-slot:hint>
                  <span v-if="isAssessName.length > 0" class="q-pr-sm"
                    >{{
                      isAssessName.toString()
                    }}为评估中/评估完成状态，不可删除</span
                  >
                </template>
                <!-- 涉及板块中如有评估中、评估完成状态，则不能去掉 -->
                <template v-slot:selected-item="scope">
                  <fdev-chip
                    dense
                    :removable="!isAssessId.includes(scope.opt.id)"
                    @remove="scope.removeAtIndex(scope.index)"
                    :tabindex="scope.tabindex"
                  >
                    {{ scope.opt.name }}
                  </fdev-chip>
                </template>
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
          </div>
          <div v-if="greatKey.length !== 0">
            <!-- 评估人员 -->
            <common-title
              width="18"
              height="18"
              x="2"
              y="2"
              class="q-mt-md"
              icon-name="bell_s_f"
              label="评估人员"
            />
            <!-- 小组评估人员 -->
            <div class="row justify-between q-ml-lg m-87">
              <div v-for="(t, i) in greatKey" :key="i">
                <f-formitem
                  :label="t.group.label"
                  required
                  label-style="width: 112px"
                >
                  <fdev-select
                    multiple
                    use-chips
                    use-input
                    :ref="`editModel${i}`"
                    v-model="t.users"
                    :options="t.options"
                    @focus="handleInputFocus(t, i)"
                    @filter="relatedUsersFilter"
                    :rules="[v => !isExitEmpty(v) || '请选择评估人员']"
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
                            :title="
                              `${scope.opt.user_name_en}--${scope.opt.group}`
                            "
                            caption
                          >
                            {{ scope.opt.user_name_en }}--{{ scope.opt.group }}
                          </fdev-item-label>
                        </fdev-item-section>
                      </fdev-item>
                    </template>
                  </fdev-select>
                </f-formitem>
              </div>
            </div>
          </div>
          <div class="row justify-center mt-66">
            <fdev-btn
              outline
              label="返回"
              class="q-my-md mr-32"
              @click="goBack"
            />
            <fdev-btn
              label="提交"
              :loading="globalLoading['demandsForm/update']"
              class="q-my-md"
              @click="handleAllTip"
            />
          </div>
        </f-block>
      </div>
    </Loading>
  </div>
</template>
<script>
import {
  createDemandModel,
  demandAvailables,
  demandAssessWays,
  futureAssess,
  priorities,
  uiList,
  queryUserOptionsParams,
  propertyOptions
} from './model';
import { formatOption, successNotify, errorNotify } from '@/utils/utils';

import { mapState, mapGetters, mapActions } from 'vuex';
import Loading from '@/components/Loading';
import {
  queryTechType,
  queryUserCoreData
} from '@/modules/Rqr/services/methods';
import commonTitle from '@/modules/Rqr/Components/commonTitle';
import { formValidate } from '@/modules/Rqr/views/rqrEdit/validate';

export default {
  name: 'rqrEdit',
  components: { Loading, commonTitle },
  mixins: [formValidate],
  data() {
    return {
      loadingStatus: false,
      helpTip: `需求编号规则应为以下三类:
                UT-WLaR-2020-0001：UT-部门简写-年份-序号 ->
                UT-ZZZZ-YYYY-XXXX
                0001-2020-L-193-000015：0001-年份-L-类别-序号 ->
                0001-YYYY-L-ZZZ-XXXXXX
                后面不能有M001、N001、001之类的序号。`,
      demandAvailables,
      demandAssessWays,
      futureAssess,
      priorities,
      uiList,
      demandType: '',
      editModel: createDemandModel(),
      groups: [],
      groupsClone: [],
      leaderGroupOptions: [],
      relatedFilterGroup: [],
      users: [],
      userOptions: [],
      userFilterOptions: [],
      greatKey: [],
      focusInputIndex: '',
      isAssessId: [],
      isAssessName: [],
      isEditOaNoReadonly: true,
      isSyncIpmp: false, // 是否为同步实施单需求
      canNotEditOAName: true,
      techTypes: [],
      count: 0,
      propertyOptions: propertyOptions
    };
  },
  watch: {
    'editModel.demand_leader_group': {
      handler: function(newValue, oldValue) {
        // 更改牵头小组 涉及板块第一个元素跟着响应
        // this.editModel.relate_part.splice(0, 1, newValue);
        //不做级联
        //非初始化执行的时候
        if (
          this.editModel.demand_leader.length !== 0 &&
          oldValue &&
          oldValue.length !== 0
        ) {
          this.editModel.demand_leader = [];
        }
        this.userOptions = this.userOptionsFilter();
        this.userFilterOptions = this.userOptions;
      }
    },
    'editModel.relate_part': {
      deep: true,
      handler: function(newValue, oldValue) {
        if (!this.count) {
          this.count++;
          if (
            (!Array.isArray(newValue[0]) && newValue.length === 1) ||
            (Array.isArray(newValue[0]) && newValue.length === 2)
          ) {
            if (!Array.isArray(newValue[0]) && newValue.length === 1) {
              this.removeRelatedGroup(newValue[0]);
              this.addRelatedGroups({ index: 0, value: newValue[0] });
              this.getUserInfo();
            } else {
              this.removeRelatedGroup(newValue[1]);
              this.addRelatedGroups({ index: 0, value: newValue[1] });
              this.getUserInfo();
            }
          }
        }

        //通过length判断是否为删除操作
        if (newValue.length < oldValue.length) {
          // 当为删除操作时拿到删除项
          let deleteItem = null;
          oldValue.forEach(item => {
            let a = false;
            newValue.forEach(one => {
              if (item.id === one.id) {
                a = true;
              }
            });
            if (!a) {
              deleteItem = item;
            }
          });
          // 判断当前删除项是否为可删除项
          const cannotDelFlag = this.isAssessId.indexOf(deleteItem.id) > -1;
          if (cannotDelFlag) {
            const oldValueId = oldValue.map(part => part.id);
            const lastId = oldValueId[oldValueId.length - 1];
            const theLastGroup = this.editModel.relate_part_detail.find(
              item => {
                return item.part_id === lastId;
              }
            );
            if (this.isAssessId.indexOf(lastId) > -1) {
              this.groups.forEach(val => {
                if (val.id === lastId) {
                  this.editModel.relate_part.push(val);
                }
              });
              let lastUserArray = [];
              theLastGroup.assess_user_all.forEach(userInfo => {
                this.userOptions.filter(user => {
                  if (user.id === userInfo.id) lastUserArray.push(user);
                });
              });
              this.greatKey.push({
                group: {
                  id: theLastGroup.part_id,
                  label: theLastGroup.part_name
                },
                users: lastUserArray,
                options: this.userOptionsFilter(lastId),
                optionsClone: this.userOptionsFilter(lastId)
              });
            }
          }
        }
      }
    }
  },
  computed: {
    ...mapState('userForm', {
      groupsData: 'groups',
      userInPage: 'userInPage'
    }),
    ...mapState('global', ['loading']),
    ...mapState('user', ['currentUser']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('demandsForm', {
      demandInfoDetail: 'demandInfoDetail',
      oaNOStatus: 'oaNOStatus'
    }),
    ...mapGetters('user', ['isLoginUserList', 'isDemandManager']),
    isReadOnly() {
      return (
        !this.isDemandManager ||
        this.editModel.leader_group_status === '1' ||
        this.editModel.leader_group_status === '1'
      );
    },
    isReadOnlyLader() {
      return !this.isDemandManager && !this.isDemandLeader();
    },
    isEditReadonly() {
      return true;
    }
  },
  methods: {
    ...mapActions('userForm', {
      queryGroup: 'fetchGroup'
    }),
    ...mapActions('user', {
      queryUser: 'fetch'
    }),
    ...mapActions('demandsForm', {
      queryDemandInfoDetail: 'queryDemandInfoDetail',
      queryDemandByOaContactNo: 'queryDemandByOaContactNo',
      update: 'update'
    }),
    isBusinessFun() {
      return this.demandType === 'business';
    },
    isTechFun() {
      return this.demandType === 'tech';
    },
    isDailyFun() {
      return this.demandType === 'daily';
    },
    async handleAllTip() {
      let relatePartId = this.editModel.relate_part.map(val => {
        return val.id;
      });
      if (
        this.isAssessId.every(val => {
          return relatePartId.includes(val);
        })
      ) {
        await this.handelEdit();
      } else {
        errorNotify('请勿删除评估中/评估完成状态的涉及小组');
        setTimeout(() => {
          history.go(0);
        }, 2000);
      }
    },
    //判断用户集合中是否包含当前用户 ， 当前用户是否是需求牵头人,
    isDemandLeader() {
      if (
        this.editModel.demand_leader_all &&
        Array.isArray(this.editModel.demand_leader_all)
      ) {
        return this.editModel.demand_leader_all.some(user => {
          return user.id === this.currentUser.id;
        });
      }
    },
    isIncludeCurrentUser() {
      if (
        this.editModel.relate_part_detail &&
        Array.isArray(this.editModel.relate_part_detail)
      ) {
        return this.editModel.relate_part_detail.some(part => {
          return (
            part.assess_user &&
            part.assess_user.some(id => {
              return id === this.currentUser.id;
            })
          );
        });
      }
    },
    async handelEdit() {
      if (this.formValidateFun()) return;
      let relatePartId = this.editModel.relate_part.map(val => {
        return val.id;
      });
      let leaderId = [];
      this.editModel.demand_leader.map(val => {
        leaderId.push(val.id);
      });
      let demandLabel = [];
      this.editModel.demand_label_info.map(item => {
        if (item.flag) {
          demandLabel.push(item.code);
        }
      });
      let demand_leader_all = this.editModel.demand_leader.map(val => {
        return {
          id: val.id,
          user_name_cn: val.user_name_cn,
          user_name_en: val.user_name_en
        };
      });
      this.editModel.relate_part_detail = this.greatKey.map(item => {
        return {
          part_id: item.group.id,
          part_name: item.group.label,
          assess_status: item.assess_status ? item.assess_status : '0',
          assess_user_all: item.users.map(user => {
            return {
              id: user.id,
              user_name_cn: user.user_name_cn,
              user_name_en: user.user_name_en
            };
          })
        };
      });
      let params = {
        ...this.editModel,
        demand_leader_group:
          this.editModel.demand_leader_group &&
          this.editModel.demand_leader_group.id,
        demand_leader_all: demand_leader_all,
        relate_part: relatePartId,
        relate_part_detail: this.editModel.relate_part_detail,
        demand_leader: leaderId,
        demand_leader_group_cn:
          this.editModel.demand_leader_group &&
          this.editModel.demand_leader_group.name,
        demand_label: demandLabel
      };
      if (params.accept_date === null) params.accept_date = '';
      //quasar的select组件没有校验的话，取的值有时候是取的value，有时候取的是整个对象。。暂时特殊处理
      if (
        params.demand_assess_way &&
        typeof params.demand_assess_way == 'object'
      ) {
        params.demand_assess_way = params.demand_assess_way.value;
      } else if (!params.demand_assess_way) {
        params.demand_assess_way = '';
      }
      if (
        params.demand_available &&
        typeof params.demand_available == 'object'
      ) {
        params.demand_available = params.demand_available.value;
      } else if (!params.demand_available) {
        params.demand_available = '';
      }
      if (params.future_assess && typeof params.future_assess == 'object') {
        params.future_assess = params.future_assess.value;
      } else if (!params.future_assess) {
        params.future_assess = '';
      }

      let propertyObj = {
        预研: 'advancedResearch',
        重点: 'keyPoint',
        常规: 'routine'
      };
      let propertyValue;
      if (params.demand_property) {
        if (typeof params.demand_property === 'string') {
          propertyValue = params.demand_property;
        } else {
          propertyValue = params.demand_property.value;
        }
        for (let key in propertyObj) {
          if (key === params.demand_property) propertyValue = propertyObj[key];
        }
      } else {
        propertyValue = '';
      }
      params.demand_property = propertyValue;

      // 老代码优先级处理开始
      let priorityObj = {
        高: '0',
        中: '1',
        一般: '2',
        低: '3'
      };
      let value;
      if (params.priority) {
        if (typeof params.priority === 'string') {
          value = params.priority;
        } else {
          value = params.priority.value;
        }
        for (let key in priorityObj) {
          if (key === params.priority) value = priorityObj[key];
        }
      } else {
        value = '';
      }
      this.$set(params, 'priority', value);
      // 老代码优先级处理结束
      //若有值不允许修改
      if (this.isEditOaNoReadonly) {
        await this.update(params);
        successNotify('更新成功');
        this.$router.go(-1);
      } else {
        if (this.editModel.oa_contact_no) {
          //如果若OA联系单编号无值，允许用户修改且并弹框提示用户
          this.$q
            .dialog({
              title: '提示',
              message: '需求编号将同步调整为OA联系单编号',
              cancel: true,
              persistent: true
            })
            .onOk(async () => {
              await this.update(params);
              successNotify('更新成功');
              this.$router.go(-1);
            });
        } else {
          await this.update(params);
          successNotify('更新成功');
          this.$router.go(-1);
        }
      }
    },
    // 添加用户信息（当只有一个板块信息的时候添加数组信息）
    getUserInfo() {
      this.editModel.relate_part_detail.map(val => {
        let userArray = [];
        if (val.assess_user_all) {
          val.assess_user_all.map(item => {
            this.userOptions.filter(user => {
              if (user.id === item.id) userArray.push(user);
            });
          });
        }
        this.greatKey[0].users = userArray;
      });
    },
    async initQuery() {
      await this.queryGroup();
      //筛选出1 2 3级小组    新增编辑需求时 牵头小组和涉及小组不能选四级小组
      let threeGroups = [];
      threeGroups = this.groupsData.filter(item => {
        return item.level < 4;
      });
      this.groups = formatOption(threeGroups);
      this.leaderGroupOptions = this.groups.slice(0);
      let params = {
        company_id: queryUserOptionsParams.company_id,
        status: queryUserOptionsParams.status
      };
      const res = await queryUserCoreData(params);
      this.users = res.map(user => formatOption(user, 'user_name_cn'));
      this.userOptions = this.users.slice(0);
      this.userFilterOptions = this.userOptions;
      this.relatedFilterGroup = this.groups;
    },
    userOptionsFilter(group_id) {
      const myUser = this.userOptions.filter(item => {
        if (item.email.indexOf('@') > -1) {
          return item.email.split('@')[1] === 'spdb.com.cn';
        }
      });
      return myUser;
    },
    userFilter(val, update, abort) {
      this.userOptions = this.userOptions.filter(item => {
        if (item.email.indexOf('@') > -1) {
          return item.email.split('@')[1] === 'spdb.com.cn';
        }
      });

      // }
      //输入中文名和账号名过滤
      update(() => {
        this.userFilterOptions = this.userOptions.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
      //遍历获取小组名
      for (let key in this.userFilterOptions) {
        let userGroup = this.groups.filter(
          item => this.userFilterOptions[key].group_id == item.id
        );
        this.userFilterOptions[key].group =
          userGroup.length && userGroup[0].name;
      }
    },
    relatedGroupFilter(val, update, abort) {
      //涉及板块选择框输入过滤
      update(() => {
        this.relatedFilterGroup = this.groupsClone.filter(
          group => group.fullName.indexOf(val) > -1
        );
      });
    },
    //牵头板块选择框输入过滤
    leaderGroupFilter(val, update, abort) {
      update(() => {
        this.leaderGroupOptions = this.groups.filter(
          group => group.fullName.indexOf(val) > -1
        );
      });
    },
    //增加涉及板块，添加节点，动态生成选择框，节点包含板块信息和用来绑定选择人员的users数组
    addRelatedGroups({ index, value }) {
      if (value.id === this.editModel.demand_leader_group.id) {
        this.greatKey.unshift({
          group: value,
          users: [],
          options: this.userOptionsFilter(value.id),
          optionsClone: this.userOptionsFilter(value.id)
        });
      } else {
        this.greatKey.push({
          group: value,
          users: [],
          options: this.userOptionsFilter(value.id),
          optionsClone: this.userOptionsFilter(value.id)
        });
      }
    },
    removeRelatedGroup(detail) {
      //从涉及板块选择框中删除板块时，将相应的节点删除
      const index = this.greatKey.findIndex(item => {
        return item.group.id === detail.value.id;
      });
      this.greatKey.splice(index, 1);
    },
    //当前获取焦点的涉及板块人员选择框，以其所属板块id和输入，过滤选择框选项
    relatedUsersFilter(val, update, abort) {
      this.userOptions = this.userOptions.filter(item => {
        if (item.email.indexOf('@') > -1) {
          return item.email.split('@')[1] === 'spdb.com.cn';
        }
      });
      update(() => {
        this.greatKey[this.focusInputIndex].options = this.userOptions.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
      //遍历获取小组名
      for (let key in this.greatKey[this.focusInputIndex].options) {
        let userGroup = this.groups.filter(
          item =>
            this.greatKey[this.focusInputIndex].options[key].group_id == item.id
        );
        this.greatKey[this.focusInputIndex].options[key].group =
          userGroup.length && userGroup[0].name;
      }
    },
    handleInputFocus(item, index) {
      this.focusInputIndex = parseInt(index);
    },
    ifShowTechTypeFun(key) {
      if (key && key.indexOf('其他') !== -1) return true;
      return false;
    },
    onChangeTechType(data) {
      if (this.ifShowTechTypeFun(data)) {
        this.editModel.tech_type_desc = '';
      }
    },
    getPropertyItem(val) {
      for (let i = 0; i < this.propertyOptions.length; i++) {
        if (val == this.propertyOptions[i].value) {
          return this.propertyOptions[i];
        }
      }
      return null;
    }
  },
  async created() {
    this.loadingStatus = true;
    await this.queryDemandInfoDetail({ id: this.$route.params.id });
    await this.initQuery();
    this.loadingStatus = false;
    this.demandType = this.demandInfoDetail.demand_type;
    this.editModel = this.demandInfoDetail;
    let obj = {
      0: '高',
      1: '中',
      2: '一般',
      3: '低'
    };
    this.editModel.priority = obj[this.editModel.priority];

    this.editModel.demand_property = this.getPropertyItem(
      this.demandInfoDetail.demand_property
    );

    this.isSyncIpmp = this.demandInfoDetail.demand_flag;
    //判断是否同步了IPMP数据，控制修改字段
    if (this.demandInfoDetail.demand_flag) {
      this.canNotEditOAName =
        this.demandInfoDetail.demand_flag === 0 ? false : true; //0表示紧急需求未同步，1表示已同步不支持修改
    }

    // 组装keylist数组
    let keylist = this.editModel.relate_part_detail.map(val => {
      let userArray = [];
      if (val.assess_user_all) {
        val.assess_user_all.map(item => {
          this.userOptions.filter(user => {
            if (user.id === item.id) userArray.push(user);
          });
        });
      }
      // 如果涉及板块评估状态为评估中或者评估完成，则不能修改相关板块。
      if (val.assess_status === '1' || val.assess_status === '2') {
        this.isAssessId.push(val.part_id);
        this.isAssessName.push(val.part_name);
      }
      const _this = this;
      return {
        group: { id: val.part_id, label: val.part_name },
        users: userArray,
        assess_status: val.assess_status,
        options: _this.userOptionsFilter(val.part_id),
        optionsClone: _this.userOptionsFilter(val.part_id)
      };
    });
    this.greatKey = keylist;
    this.groupsClone = JSON.parse(JSON.stringify(this.groups));
    this.relatedFilterGroup = this.groupsClone.map(item => {
      if (this.isAssessId.includes(item.id)) {
        item.disable = true;
      }
      return item;
    });

    //牵头小组为数组类型，赋值
    if (this.editModel.demand_leader_group) {
      this.leaderGroupOptions.filter(val => {
        if (val.id === this.editModel.demand_leader_group) {
          this.editModel.demand_leader_group = val;
        }
      });
    } else {
      this.editModel.demand_leader_group = [];
    }
    // 牵头人赋值
    let userList = [];
    this.userOptions.forEach(val => {
      this.editModel.demand_leader.filter(item => {
        if (val.id === item) {
          userList.push(val);
        }
      });
    });
    this.editModel.demand_leader = userList;
    // 涉及板块赋值
    let relatePartList = [];
    this.editModel.relate_part_detail.filter(item => {
      this.groups.forEach(val => {
        if (val.id === item.part_id) {
          relatePartList.push(val);
        }
      });
    });
    this.editModel.relate_part = relatePartList;

    //判断OA联系单是否有值
    if (!this.editModel.oa_contact_no) {
      this.isEditOaNoReadonly = false;
    }
    const response = await queryTechType({});
    if (response && response.length > 0) {
      this.techTypes = [];
      response.forEach(item => {
        this.techTypes.push(item.techType);
      });
    }
  }
};
</script>
<style lang="stylus" scoped>

.title {
  font-size: 18px;
  padding: 10px;
}
.pre-style
  display block
  line-height 1.8em
.relative {
  position: relative;
}
.m-87
  margin-right 87px
.mr-32
  margin-right 32px
.mt-86
  margin-top 86px
>>> .block-type
  padding-bottom 16px
.mt-66
  margin-top 66px
.labelSty
  color #fff;
  padding 2px 5px;
  border-radius 3px;
</style>
