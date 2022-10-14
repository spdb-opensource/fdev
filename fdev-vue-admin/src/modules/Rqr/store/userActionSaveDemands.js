export default {
  namespaced: true,
  modules: {
    //需求列表
    requirementList: {
      namespaced: true,
      state: {
        userid: '', //牵头人/评估人/创建者
        keyword: '', //需求名称、需求编号（OA联系单编号）
        groupid: [], //小组
        groupQueryType: '', //本组/本组及子组
        groupState: '', //小组类型
        datetype: '', //延期选项
        isDateType: false,
        isDelayNum: false,
        delayNum: '', //超期天数
        featureType: '', //预进行选项
        featureNum: '',
        isfeatureNum: false,
        isFeatureType: false,
        designState: '', //设计稿审核状态
        isSubmitCodeReview: '', //是否提交了代码审核
        states: [], //状态
        isstateNum: false,
        stateNum: '',
        relDateType: [], //实际日期类型
        relStartDate: '',
        demandLabel: [], //需求标签
        relEndDate: '',
        isRealDate: false,
        demandType: { label: '全部', value: '' }, //类型
        priority: { label: '全部', value: '' }, //优先级
        relevant: { label: '查看所有', value: false }, //查看所有
        visibleColumns: [
          'demand_plan_name',
          'oa_contact_no',
          'demand_type',
          'demand_status_normal',
          'design_status',
          'priority',
          'propose_demand_dept',
          'propose_demand_user',
          'plan_user',
          'demand_leader_all',
          'demand_leader_group_cn',
          'plan_start_date',
          'plan_inner_test_date',
          'plan_test_date',
          'plan_test_finish_date',
          'plan_product_date',
          'real_start_date',
          'real_inner_test_date',
          'real_test_date',
          'real_test_finish_date',
          'real_product_date',
          'operation',
          'dept_workload',
          'company_workload'
        ], //选择列
        queryParam: {
          datetype: '', //延期类型
          delayNum: '', //延期天数
          designState: '', //设计稿审核状态
          isSubmitCodeReview: '',
          featureNum: '', //预进行天数
          featureType: '', //预进行类型
          groupid: [], //小组
          groupQueryType: '', //本组/本组及子组
          groupState: '', //小组类型
          keyword: '', //搜索框
          priority: '', //优先级
          relDateType: [], //实际日期类型
          relEndDate: '', //实际结束日期
          relStartDate: '', //实际开始日期
          relevant: false, //与我相关
          states: [], //状态
          stateNum: '', //状态超过天数
          demandType: '', //需求类型
          userid: '', //负责人/受理人
          index: 0, //页标
          size: 0,
          descending: false, //降序
          demand_label: [] //需求标签
        } //上传参数
      },

      mutations: {
        updateUserid(state, payload) {
          state.userid = payload;
        },
        updateKeyword(state, payload) {
          state.keyword = payload;
        },
        updateGroupid(state, payload) {
          state.groupid = payload;
        },
        updateGroupQueryType(state, payload) {
          state.groupQueryType = payload;
        },
        updateGroupState(state, payload) {
          state.groupState = payload;
        },
        updateDatetype(state, payload) {
          state.datetype = payload;
        },
        updateIsDateType(state, payload) {
          state.isDateType = payload;
        },
        updateDelayNum(state, payload) {
          state.delayNum = payload;
        },
        updateIsDelayNum(state, payload) {
          state.isDelayNum = payload;
        },
        updateFeatureType(state, payload) {
          state.featureType = payload;
        },
        updateFeatureNum(state, payload) {
          state.featureNum = payload;
        },
        updateIsfeatureNum(state, payload) {
          state.isfeatureNum = payload;
        },
        updateIsFeatureType(state, payload) {
          state.isFeatureType = payload;
        },
        updateDesignState(state, payload) {
          state.designState = payload;
        },
        updateIsSubmitCodeReview(state, payload) {
          state.isSubmitCodeReview = payload;
        },
        updateStates(state, payload) {
          state.states = payload;
        },
        updatestateNum(state, payload) {
          state.stateNum = payload;
        },
        updateIsstateNum(state, payload) {
          state.isstateNum = payload;
        },
        updateRelDateType(state, payload) {
          state.relDateType = payload;
        },
        updateIsRealDate(state, payload) {
          state.isRealDate = payload;
        },
        updateRelStartDate(state, payload) {
          state.relStartDate = payload;
        },
        updateRelEndDate(state, payload) {
          state.relEndDate = payload;
        },
        updateDemandType(state, payload) {
          state.demandType = payload;
        },
        updatePriority(state, payload) {
          state.priority = payload;
        },
        updateRelevant(state, payload) {
          state.relevant = payload;
        },
        updateDemandLabel(state, payload) {
          state.demandLabel = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        },
        // 查询条件
        updateQueryParamDatetype(state, payload) {
          state.queryParam.datetype = payload;
        },
        updateQueryParamDelayNum(state, payload) {
          state.queryParam.delayNum = payload;
        },
        updateQueryParamDesignState(state, payload) {
          state.queryParam.designState = payload;
        },
        updateQueryParamIsSubmitCodeReview(state, payload) {
          state.queryParam.isSubmitCodeReview = payload;
        },
        updateQueryParamFeatureNum(state, payload) {
          state.queryParam.featureNum = payload;
        },
        updateQueryParamFeatureType(state, payload) {
          state.queryParam.featureType = payload;
        },
        updateQueryParamGroupid(state, payload) {
          state.queryParam.groupid = payload;
        },
        updateQueryParamGroupQueryType(state, payload) {
          state.queryParam.groupQueryType = payload;
        },
        updateQueryParamGroupState(state, payload) {
          state.queryParam.groupState = payload;
        },
        updateQueryParamKeyword(state, payload) {
          state.queryParam.keyword = payload;
        },
        updateQueryParamPriority(state, payload) {
          state.queryParam.priority = payload;
        },
        updateQueryParamRelDateType(state, payload) {
          state.queryParam.relDateType = payload;
        },
        updateQueryParamRelEndDate(state, payload) {
          state.queryParam.relEndDate = payload;
        },
        updateQueryParamRelStartDate(state, payload) {
          state.queryParam.relStartDate = payload;
        },
        updateQueryParamRelevant(state, payload) {
          state.queryParam.relevant = payload;
        },
        updateQueryParamStates(state, payload) {
          state.queryParam.states = payload;
        },
        updateQueryParamStateNum(state, payload) {
          state.queryParam.stateNum = payload;
        },
        updateQueryParamDemandType(state, payload) {
          state.queryParam.demandType = payload;
        },
        updateQueryParamUserid(state, payload) {
          state.queryParam.userid = payload;
        },
        updateQueryParamIndex(state, payload) {
          state.queryParam.index = payload;
        },
        updateQueryParamSize(state, payload) {
          state.queryParam.size = payload;
        },
        updateQueryParamDescending(state, payload) {
          state.queryParam.descending = payload;
        },
        updateQueryParamDemandLable(state, payload) {
          state.queryParam.demand_label = payload;
        }
      }
    },
    // 需求评估列表
    evaluateList: {
      namespaced: true,
      state: {
        moreParamsData: {
          confState: null, //需求文档状态,
          goingDays: null, //需求进行中天数
          finalDays: null, //需求定稿后天数
          overdueType: null //超期分类
        } //高级搜索条件
      },
      mutations: {
        updateConfState(state, payload) {
          state.moreParamsData.confState = payload;
        },
        updateGoingDays(state, payload) {
          state.moreParamsData.goingDays = payload;
        },
        updateFinalDays(state, payload) {
          state.moreParamsData.finalDays = payload;
        },
        updateOverdueType(state, payload) {
          state.moreParamsData.overdueType = payload;
        }
      }
    },

    // 研发单元审批列表
    rdUnitApprovalList: {
      namespaced: true,
      state: {
        visibleColumns: [
          'fdevUnitNo',
          'fdevUnitName',
          'demandNo',
          'demandName',
          'fdevUnitLeaderName',
          'groupName',
          'overdueType',
          'overdueReason',
          'applicantName',
          'approveState',
          'approverName',
          'sectionName',
          'applyTime',
          'approveTime',
          'approveReason',
          'approveType'
        ] // 选择列
      },
      mutations: {
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    //定稿日期审核列表
    eveluateApprovalList: {
      namespaced: true,
      state: {
        approvalParams: {
          oaContactNameNo: '', //需求名称/编号
          demandLeaderGroups: [], //牵头小组
          apply_user: [], //申请人
          approveStates: [], //审批状态
          approverIds: [] //审批人
        },
        visibleColumns: [
          'oa_contact_no',
          'oa_contact_name',
          'demand_leader_group_cn',
          'apply_user',
          'apply_reason',
          'apply_update_time',
          'create_time',
          'operate_user',
          'operate_time',
          'state',
          'operate_status'
        ] // 选择列
      },
      mutations: {
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        },
        updatedemandKey(state, payload) {
          state.approvalParams.oaContactNameNo = payload;
        },
        updateGroupIds(state, payload) {
          state.approvalParams.demandLeaderGroups = payload;
        },
        updateApplyUser(state, payload) {
          state.approvalParams.apply_user = payload;
        },
        updateApproveStates(state, payload) {
          state.approvalParams.approveStates = payload;
        },
        updateApproverIds(state, payload) {
          state.approvalParams.approverIds = payload;
        }
      }
    }
  }
};
