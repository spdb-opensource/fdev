export default {
  fuser: {
    currentUser: '/fuser/api/user/currentUser',
    userQuery: '/fuser/api/user/query',
    groupQuery: '/fuser/api/group/query',
    userUpdate: '/fuser/api/user/update',
    labelQuery: '/fuser/api/label/query',
    labelAdd: '/fuser/api/label/add',
    labelDelete: '/fuser/api/label/delete',
    queryArea: '/fuser/api/user/queryArea',
    queryFunction: '/fuser/api/user/queryfunction',
    queryRank: '/fuser/api/user/queryrank'
  },
  fapp: {
    myAppQuery: '/fapp/api/app/queryMyApps'
  },
  fcomponent: {
    queryMyArchetypes: '/fcomponent/api/archetype/queryMyArchetypes',
    queryMyComponent: '/fcomponent/api/component/queryMyComponents',
    queryMyMpassArchetypes:
      '/fcomponent/api/mpassarchetype/queryMyMpassArchetypes',
    queryMyMpassComponents:
      '/fcomponent/api/mpasscomponent/queryMyMpassComponents'
  },
  tmantis: {
    queryFuserMantis: '/tmantis/mantisFdev/queryFuserMantisAll',
    updateFdevMantis: '/tmantis/mantisFdev/updateFdevMantis',
    updateAssignUser: '/tmantis/mantisFdev/updateFdevMantis',
    queryUserProIssues: '/tmantis/proIssue/queryUserProIssues',
    queryTaskProIssues: '/tmantis/proIssue/queryTaskProIssues',
    deleteProIssue: '/tmantis/proIssue/delete'
  },
  torder: {
    queryOrderInfoByNo: '/torder/order/queryOrderInfoByNo' //据工单编号查询工单信息
  },
  ftask: {
    taskUpdate: '/ftask/api/task/update',
    queryCommitTips: '/ftask/api/git/queryCommitTips',
    queryUserTask: '/ftask/api/task/queryUserTask'
  },
  fmine: {
    queryWaitFileTask: '/ftask/api/task/queryWaitFileTask', // 查询用户可操作的已投产待归档任务列表
    updateTaskStateToFile: '/ftask/api/task/updateTaskStateToFile' // 批量归档任务
  }
};
