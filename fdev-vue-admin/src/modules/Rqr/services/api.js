export default {
  fdemand: {
    queryDemandFile: '/fdemand/api/demand/queryDemandFile', //需求归档检查文件存在情况
    //根据fdev实施单元编号、需求id查实施单元信息及需求信息
    queryByFdevNoAndDemandId:
      '/fdemand/api/implementUnit/queryByFdevNoAndDemandId',
    queryImpingDemandDashboard:
      '/fdemand/api/dashboard/queryImpingDemandDashboard',
    queryEndDemandDashboard: '/fdemand/api/dashboard/queryEndDemandDashboard',
    queryIntGroupId: '/fdemand/api/dashboard/queryIntGroupId',
    save: '/fdemand/api/demand/save',
    query: '/fdemand/api/demand/query', //查询全量需求
    queryStatis: '/fdemand/api/dashboard/queryDemandStatis', //需求报表统计
    queryImplUnitStatis: '/fdemand/api/dashboard/queryImplUnit', //实施单元报表统计
    queryGroupRqrmnt: '/fdemand/api/dashboard/queryGroupDemand', //各组对应阶段实施需求数量需求报表统计
    queryDemandList: '/fdemand/api/demand/queryDemandList',
    update: '/fdemand/api/demand/update',
    deleteRqr: '/fdemand/api/demand/repeal',
    fileRqr: '/fdemand/api/demand/placeonfile',
    queryPaginationByDemandId:
      '/fdemand/api/implementUnit/queryPaginationByDemandId', //查看需求下的研发单元
    queryDemandInfoDetail: '/fdemand/api/demand/queryDemandInfoDetail',
    deleteUnitById: 'fdemand/api/ipmptask/deleteUnitById',
    deleteById: '/fdemand/api/implementUnit/deleteById',
    addImplementUnit: '/fdemand/api/implementUnit/add',
    supplyImplementUnit: '/fdemand/api/implementUnit/supply',
    updateImplementUnit: '/fdemand/api/implementUnit/update',
    search: '/fdemand/api/ipmptask/search',
    queryByGroupId: '/fdemand/api/ipmptask/queryByGroupId',
    queryUnitByIpmpTaskId: '/fdemand/api/ipmptask/queryUnitByIpmpTaskId',
    addIpmpTask: '/fdemand/api/ipmptask/addIpmpTask',
    addUnit: '/fdemand/api/ipmptask/addUnit',
    queryDemandDoc: '/fdemand/api/doc/queryDemandDoc',
    updateDemandDoc: '/fdemand/api/doc/updateDemandDoc',
    exportAssessExcel: '/fdemand/api/demand/exportAssessExcel',
    getDesignInfo: '/fdemand/api/design/getDesignInfo',
    uploadDesignDoc: '/fdemand/api/design/uploadDesignDoc',
    updateDesignStage: '/fdemand/api/design/updateDesignStage',
    updateDesignRemark: '/fdemand/api/design/updateDesignRemark',
    assess: '/fdemand/api/implementUnit/assess',
    queryImplByGroupAndDemandId:
      '/fdemand/api/implementUnit/queryImplByGroupAndDemandId',
    queryAvailableIpmpUnit: '/fdemand/api/implementUnit/queryAvailableIpmpUnit',
    queryAvailableIpmpUnitNew:
      '/fdemand/api/implementUnit/queryAvailableIpmpUnitNew',
    defer: '/fdemand/api/demand/defer',
    recover: '/fdemand/api/demand/recover',
    exportDemandsExcel: '/fdemand/api/demand/exportDemandsExcel',
    deleteDemandDoc: '/fdemand/api/doc/deleteDemandDoc',
    updateByRecover: '/fdemand/api/implementUnit/updateByRecover',
    importDemandExcel: '/fdemand/api/demand/importDemandExcel',
    queryDemandByOaContactNo: '/fdemand/api/demand/queryDemandByOaContactNo',
    queryReviewList: '/fdemand/api/design/queryReviewList',
    exportModelExcel: '/fdemand/api/demand/exportModelExcel',
    updateImpl: '/fdemand/api/demand/updateImpl',
    queryPartInfo: '/fdemand/api/demand/queryPartInfo',
    queryIpmpUnitByDemandId: '/fdemand/api/ipmpUnit/queryIpmpUnitByDemandId', // 根据需求id查看实施单元列表
    queryDetailByUnitNo: '/ftask/api/task/queryTaskByUnitNos', // 通过研发单元编号查询任务
    queryIpmpUnitById: '/fdemand/api/ipmpUnit/queryIpmpUnitById', //根据实施单元id查看实施单元详情
    updateIpmpUnit: '/fdemand/api/ipmpUnit/updateIpmpUnit', //修改实施单元
    queryIpmpProject: '/fdemand/api/project/queryIpmpProject', //查询项目和任务集信息
    queryFdevImplUnitDetail:
      '/fdemand/api/implementUnit/queryFdevImplUnitDetail', // 查询研发单元详情
    queryTaskByIpmpUnitNo: '/fdemand/api/ipmpUnit/queryTaskByIpmpUnitNo', //通过实施单元编号查询任务
    mount: '/fdemand/api/implementUnit/mount', //挂载
    queryPaginationByIpmpUnitNo:
      '/fdemand/api/implementUnit/queryPaginationByIpmpUnitNo', //查看实施单元下的研发单元
    assessButton: '/fdemand/api/implementUnit/assessButton', //判断评估完成按钮是否置灰
    getWorker: '/fdemand/api/implementUnit/checkWork', // 获取研发单元剩余工作量
    exportExcelData: '/fdocmanage/api/file/filesDownload',
    queryIpmpLeadTeam: '/fuser/api/ipmpTeam/queryIpmpLeadTeam', //查询牵头单位和团队信息
    queryIpmpUser: '/fuser/api/ipmpUser/queryIpmpUser', //查询ipmp人员信息
    queryTechType: '/fdemand/api/demand/queryTechType', //查询科技类型
    queryIpmpUnitIsCheck: '/fdemand/api/ipmpUnit/queryIpmpUnitIsCheck', //查询需求下当前登录人是否有牵头的实施单元未核算
    syncIpmpUnit: '/fdemand/api/ipmpUnit/syncIpmpUnit', //查询科技类型
    queryOtherDemandTaskList: '/fdemand/api/other/queryOtherDemandTaskList', //查询其他需求任务
    addOtherDemandTask: '/fdemand/api/other/addOtherDemandTask', //新增其他需求任务
    updateDemandTask: '/fdemand/api/other/updateOtherDemandTask', //修其他需求任务
    deleteOtherDemandTask: '/fdemand/api/other/deleteOtherDemandTask', //删除其他需求任务
    queryOtherDemandTask: '/fdemand/api/other/queryOtherDemandTask', //查询其他需求任务详情
    isShowAdd: '/fdemand/api/implementUnit/isShowAdd', //判断当前用户是否展示新增研发单元按钮
    getOverdueTypeSelect: '/fdemand/api/dict/query', //查询超期分类列表
    queryEvaMgtList: '/fdemand/api/demandAssess/query', //查询评估需求列表
    saveEvaMgt: '/fdemand/api/demandAssess/save', //添加评估需求
    updateEvaMgt: '/fdemand/api/demandAssess/update', //修改评估需求
    exportEvaMgtList: '/fdemand/api/demand/exportEvaMgtList', //导出评估需求列表
    queryById: '/fdemand/api/demandAssess/queryById', //需求评估详情
    delete: '/fdemand/api/demandAssess/delete', //撤销
    export: '/fdemand/api/demandAssess/export', //导出
    queryUserCoreData: '/fuser/api/user/queryUserCoreData', //查询基础用户信息（不关联公司、小组、角色、标签等详细信息）
    queryIpmpUnitList: '/fdemand/api/ipmpUnit/queryIpmpUnitList',
    exportIpmpUnitList: '/fdemand/api/ipmpUnit/exportIpmpUnitList', //导出实施单元
    queryFdevUnitList: '/fdemand/api/implementUnit/queryFdevUnitList', //研发单元列表
    exportFdevUnitList: '/fdemand/api/implementUnit/exportFdevUnitList ', //导出研发单元列表
    getSchemeReview: '/fdemand/api/ipmpUnit/getSchemeReview', //查询技术方案编号
    getCloudCheckers: '/fdemand/api/ipmpUnit/getCloudCheckers', //查询审核人
    queryApproveList: '/fdemand/api/approve/queryApproveList', //查询研发单元审批列表
    exportApproveList: '/fdemand/api/approve/exportApproveList', //导出研发单元审批列表
    approvePass: '/fdemand/api/approve/approvePass', //研发单元审批通过
    approveReject: '/fdemand/api/approve/approveReject', //审批拒绝
    queryDemandAssessDate: '/fdemand/api/approve/queryDemandAssessDate', //查询评估完成时间和XY值
    applyApprove: '/fdemand/api/approve/applyApprove', //申请超期审批
    queryMyApproveList: '/fdemand/api/approve/queryMyApproveList', //查询我的审批
    queryByTypes: '/fdemand/api/dict/queryByTypes', //根据字典类型查询字典
    confirmDelay: '/fdemand/api/ipmpUnit/confirmDelay', //确认延期
    adjustDate: '/fdemand/api/ipmpUnit/adjustDate', //调整排期
    deleteEmailFile: '/fdemand/api/ipmpUnit/deleteEmailFile', //删除文件
    addTestOrder: '/fdemand/api/test/addTestOrder', //创建提测单
    updateTestOrder: '/fdemand/api/test/updateTestOrder', //修改提测单
    submitTestOrder: '/fdemand/api/test/submitTestOrder', //确认提交提测单
    deleteTestOrder: '/fdemand/api/test/deleteTestOrder', //删除提测单
    queryTestOrderDetail: '/fdemand/api/test/queryTestOrder', //查询提测单详情
    queryCopyFlag: '/fdemand/api/test/queryCopyFlag', //查询提测单复制权限
    queryTestOrderList: '/fdemand/api/test/queryTestOrderList', //查询提测单列表
    uploadTestOrderFile: '/fdemand/api/test/uploadTestOrderFile', //提测单附件上传
    deleteTestOrderFile: '/fdemand/api/test/deleteTestOrderFile', //提测单附件删除
    queryTestOrderFile: '/fdemand/api/test/queryTestOrderFile', //提测单列表
    queryInnerTestTab: '/fdemand/api/test/queryInnerTestTab', //内测选项列表
    queryFdevUnitListByDemandId:
      '/fdemand/api/test/queryFdevUnitListByDemandId', //提测单研发单元列表
    getTestManagerInfo: '/fuser/api/test/getTestManagerInfo', //测试经理
    getThreeLevelGroup: '/fuser/api/group/getThreeLevelGroup', //获取小组的第三级小组
    queryTechBusinessForSelect:
      '/fdemand/api/demand/queryTechBusinessForSelect', //获取需求列表
    confirmFinish: '/fdemand/api/demandAssess/confirmFinish', //手动确认需求评估完成
    cancelDefer: '/fdemand/api/demandAssess/cancelDefer', //取消暂缓接口
    updateFinalDate: '/fdemand/api/demandAssess/updateFinalDate', //修改定稿日期
    queryRqrApproveList: '/fdemand/api/fdevFinalDateApprove/queryApproveList', //列表展示定稿日期修改申请
    queryMyList: '/fdemand/api/fdevFinalDateApprove/queryMyList', //查看我的需求评估定稿审批列表
    refuse: '/fdemand/api/fdevFinalDateApprove/refuse', //拒绝审批
    agree: '/fdemand/api/fdevFinalDateApprove/agree', //同意审批
    getImplUnitRelatSpFlag: '/fdemand/api/ipmpUnit/getImplUnitRelatSpFlag', // 实施单元项目编号修改
    queryCount: '/fdemand/api/fdevFinalDateApprove/queryCount' //查询待审批及已完成数据
  },
  fdocmanage: {
    testOrderDownload: '/fdocmanage/api/file/filesDownload' //提测单附件下载
  }
};
