export default {
  fuser: {
    queryApprovalList: '/fuser/api/approval/queryApprovalList',
    updateApprovalStatus: '/fuser/api/approval/updateApprovalStatus',
    userQuery: '/fuser/api/user/query',
    companyQuery: '/fuser/api/company/query'
  },
  fapprove: {
    openKF: '/fapprove/api/network/openKF',
    closeKF: '/fapprove/api/network/closeKF'
  },
  fprocesstool: {
    queryOrdersList: '/fprocesstool/api/order/queryListSimple', //查工单列表
    exportIssueList: '/fprocesstool/api/problem/exportAll', //导出问题列表的数据
    queryCodeIssue: '/fprocesstool/api/problem/queryAll', //代码审核问题列表
    queryLogs: '/fprocesstool/api/log/queryLogs', //日志查询
    queryMeetings: '/fprocesstool/api/meeting/queryMeetings', //查询会议记录
    deleteMeetingById: '/fprocesstool/api/meeting/deleteMeetingById', //删除会议记录
    addMeeting: '/fprocesstool/api/meeting/add', //新增会议记录
    updateMeeting: '/fprocesstool/api/meeting/update', //更新会议记录
    downloadDoc: '/fdocmanage/api/file/filesDownload', //文件下载
    uploadDoc: '/fprocesstool/api/file/upload', //文件上传
    deleteDoc: '/fprocesstool/api/file/delete', //文件删除
    queryFiles: '/fprocesstool/api/file/queryFiles', //文件查询
    queryProblems: '/fprocesstool/api/problem/queryProblems', //查询问题描述列表
    queryOrders: '/fprocesstool/api/order/queryOrders', //获取工单列表
    queryOrderById: '/fprocesstool/api/order/queryOrderById', //工单详情
    exportOrderExcel: '/fprocesstool/api/order/exportOrderExcel', //导出工单
    addOrders: '/fprocesstool/api/order/add', //新增工单
    deleteOrderById: '/fprocesstool/api/order/deleteOrderById', //删除工单
    updateOrders: '/fprocesstool/api/order/update', //编辑工单
    upload: '/fprocesstool/api/order/upload', //上传文件(代码审核表，需求规格说明书)
    queryProblemItem: '/fprocesstool/api/dict/query', //查询字典（新增问题问题项下拉框选项）
    addProblem: '/fprocesstool/api/problem/add', //新增问题项
    updateProblem: '/fprocesstool/api/problem/update', //修改问题项
    exportProblemExcel: '/fprocesstool/api/problem/exportProblemExcel', //导出问题列表
    deleteProblemById: '/fprocesstool/api/problem/deleteProblemById', //删除问题项
    downloadAll: '/fprocesstool/api/file/downloadAll', //文件批量下载
    recheckRemind: '/fprocesstool/api/order/recheckRemind', //复审提醒
    applyRecheck: '/fprocesstool/api/order/applyRecheck' //申请复审
  }
};
