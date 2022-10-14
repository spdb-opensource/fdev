export default {
  namespaced: true,
  modules: {
    // KF网络开通审核
    networkOpen: {
      namespaced: true,
      state: {
        applicant_id: null, // 申请人
        company: '', // 使用人公司
        status: { label: '待审核', value: 'wait_approve' }, // 申请状态
        startTime: '', // 申请日期区间-开始
        endTime: '', // 申请日期区间-结束
        visibleColumns: [
          'applicant',
          'user',
          'group',
          'company',
          'vm_user_name',
          'phone_type',
          'phone_mac',
          'status',
          'create_time'
        ] // 选择列
      },
      mutations: {
        updateApplicationId(state, payload) {
          state.applicant_id = payload;
        },
        updateCompany(state, payload) {
          state.company = payload;
        },
        updateStatus(state, payload) {
          state.status = payload;
        },
        updateStartTime(state, payload) {
          state.startTime = payload;
        },
        updateEndTime(state, payload) {
          state.endTime = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    // KF网络关闭审核
    networkClose: {
      namespaced: true,
      state: {
        applicant_id: null, // 申请人
        company: '', // 使用人公司
        status: { label: '待审核', value: 'wait_approve' }, // 申请状态
        startTime: '', // 申请日期区间-开始
        endTime: '', // 申请日期区间-结束
        visibleColumns: [
          'applicant',
          'user',
          'group',
          'company',
          'vm_user_name',
          'phone_type',
          'phone_mac',
          'status',
          'create_time'
        ] // 选择列
      },
      mutations: {
        updateApplicationId(state, payload) {
          state.applicant_id = payload;
        },
        updateCompany(state, payload) {
          state.company = payload;
        },
        updateStatus(state, payload) {
          state.status = payload;
        },
        updateStartTime(state, payload) {
          state.startTime = payload;
        },
        updateEndTime(state, payload) {
          state.endTime = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    // 网络关闭提醒
    networkRemind: {
      namespaced: true,
      state: {
        applicant_id: null, // 申请人
        company: '', // 使用人公司
        status: { label: '待审核', value: 'wait_approve' }, // 申请状态
        startTime: '', // 申请日期区间-开始
        endTime: '', // 申请日期区间-结束
        visibleColumns: [
          'applicant',
          'user',
          'group',
          'company',
          'vm_user_name',
          'phone_type',
          'phone_mac',
          'status',
          'create_time'
        ] // 选择列
      },
      mutations: {
        updateApplicationId(state, payload) {
          state.applicant_id = payload;
        },
        updateCompany(state, payload) {
          state.company = payload;
        },
        updateStatus(state, payload) {
          state.status = payload;
        },
        updateStartTime(state, payload) {
          state.startTime = payload;
        },
        updateEndTime(state, payload) {
          state.endTime = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    // VDI网段迁移
    vdiApproval: {
      namespaced: true,
      state: {
        user_id: null, // 使用人
        vm_user_name: '', // 用户名
        status: { label: '待审核', value: 'wait_approve' }, // 申请状态
        startTime: '', // 申请日期区间-开始
        endTime: '', // 申请日期区间-结束
        visibleColumns: [
          'user',
          'vm_user_name',
          'type',
          'vm_ip',
          'status',
          'netSegment',
          'applicant',
          'projectGroup',
          'vm_name',
          'create_time'
        ] // 选择列
      },
      mutations: {
        updateUserId(state, payload) {
          state.user_id = payload;
        },
        updateVmUserName(state, payload) {
          state.vm_user_name = payload;
        },
        updateStatus(state, payload) {
          state.status = payload;
        },
        updateStartTime(state, payload) {
          state.startTime = payload;
        },
        updateEndTime(state, payload) {
          state.endTime = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    // 虚拟机网段迁移
    vmApproval: {
      namespaced: true,
      state: {
        user_id: null, // 使用人
        vm_user_name: '', // 用户名
        status: { label: '待审核', value: 'wait_approve' }, // 申请状态
        startTime: '', // 申请日期区间-开始
        endTime: '', // 申请日期区间-结束
        visibleColumns: [
          'user',
          'vm_user_name',
          'type',
          'vm_ip',
          'status',
          'netSegment',
          'applicant',
          'projectGroup',
          'vm_name',
          'create_time'
        ] // 选择列
      },
      mutations: {
        updateUserId(state, payload) {
          state.user_id = payload;
        },
        updateVmUserName(state, payload) {
          state.vm_user_name = payload;
        },
        updateStatus(state, payload) {
          state.status = payload;
        },
        updateStartTime(state, payload) {
          state.startTime = payload;
        },
        updateEndTime(state, payload) {
          state.endTime = payload;
        },
        updateVisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },

    // 代码审核
    codeTool: {
      namespaced: true,
      state: {
        dateField: [], //日期类型
        startDate: '', // 开始日期
        endDate: '', // 结束日期，
        isRealDate: false,
        aboutMe: true
      },
      mutations: {
        updateStartDate(state, payload) {
          state.startDate = payload;
        },
        updateEndDate(state, payload) {
          state.endDate = payload;
        },
        updateDateType(state, payload) {
          state.dateField = payload;
        },
        updateIsRealDate(state, payload) {
          state.isRealDate = payload;
        },
        showAboutMe(state, payload) {
          state.aboutMe = payload;
        }
      }
    },
    //代码问题列表
    codeIssueList: {
      namespaced: true,
      state: {
        isShowDate: false,
        dateField: [], //日期类型
        startDate: '', // 开始日期
        endDate: '', // 结束日期，
        issueDesc: '', //问题描述
        orderNo: '', //工单编号
        itemType: '', //问题项
        visibleCols: [
          'problem',
          'fixFlag',
          'fixDate',
          'orderNo',
          'demandName',
          'remark'
        ],
        currentPage: { rowsPerPage: 5, page: 1, rowsNumber: 0 }
      },
      getters: {
        issueDesc(state) {
          return state.issueDesc.toString().trim();
        }
      },
      mutations: {
        saveisShowDate(state, payload) {
          state.isShowDate = payload;
        },
        saveStartDate(state, payload) {
          state.startDate = payload;
        },
        saveEndDate(state, payload) {
          state.endDate = payload;
        },
        saveDateType(state, payload) {
          state.dateField = payload;
        },
        saveVisibleCols(state, payload) {
          state.visibleCols = payload;
        },
        saveCurrentPage(state, payload) {
          state.currentPage = payload;
        },
        saveIssueDesc(state, payload) {
          state.issueDesc = payload;
        },
        saveOrderNo(state, payload) {
          state.orderNo = payload;
        },
        saveItemType(state, payload) {
          state.itemType = payload;
        }
      }
    }
  }
};
