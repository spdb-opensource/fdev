//模块内部接口调用定义
// import request from '@/utils/request.js';
export default {
  fcipipeline: {
    queryCollectionPipelineList:
      '/fcipipeline/api/pipeline/queryCollectionPipelineList', //查询我收藏的流水线列表
    queryMinePipelineList: '/fcipipeline/api/pipeline/queryMinePipelineList', //查询我的流水线列表
    queryAllPipelineList: '/fcipipeline/api/pipeline/queryAllPipelineList', //查询全部流水线列表
    deletePipeline: '/fcipipeline/api/pipeline/delete', //流水线删除
    queryFdevciLogList: '/fcipipeline/api/pipelineLog/queryFdevciLogList', //查询应用fdev-ci日志列表
    queryMinePipelineTemplateList:
      '/fcipipeline/api/pipelineTemplate/queryMinePipelineTemplateList', //查询我的流水线模板列表
    queryAppPipelineList: '/fcipipeline/api/pipeline/queryAppPipelineList', //查询应用所属流水线列表
    delTemplate: '/fcipipeline/api/pipelineTemplate/delTemplate', //模板删除
    updateFollowStatus: '/fcipipeline/api/pipeline/updateFollowStatus', //流水线关注/取消关注
    queryPlugin: '/fcipipeline/api/plugin/queryPluginList',
    delPlugin: '/fcipipeline/api/plugin/delPlugin',
    addPlugin: '/fcipipeline/api/plugin/addPlugin',
    editPlugin: '/fcipipeline/api/plugin/editPlugin',
    queryPluginHistory: '/fcipipeline/api/plugin/queryPluginHistory',
    queryReadDraft: '/fcipipeline/api/pipeline/readDraft', //查询草稿
    queryPipelineDetailById: '/fcipipeline/api/pipeline/queryById', //查询流水线详情
    queryPipelineTempDetailById: '/fcipipeline/api/pipelineTemplate/queryById', //查询流水线模板详情
    pipelineTemplateAdd: '/fcipipeline/api/pipelineTemplate/add', //新增流水线模板
    pipelineAdd: '/fcipipeline/api/pipeline/add', //新增流水线
    pipelineTemplateUpdate: '/fcipipeline/api/pipelineTemplate/update', //升级流水线模板
    pipelineUpdate: '/fcipipeline/api/pipeline/update', //升级流水线
    pipelineLogDetail: '/fcipipeline/api/pipelineLog/queryPipelineDetail', //流水线日志全景图
    CIdraftSave: '/fcipipeline/api/pipeline/saveDraft', //草稿存储
    triggerPipeline: 'fcipipeline/api/pipeline/triggerPipeline', //手动触发流水线
    queryImageList: 'fcipipeline/api/pipeline/queryImageList', //查询流水线中镜像列表
    queryToolImageList: '/fcipipeline/api/images/queryToolImageList', //查询工具箱镜像列表
    updateImage: '/fcipipeline/api/images/updateImage', //镜像更新
    addImage: '/fcipipeline/api/images/addImage', //镜像新增
    queryLogDetailById: '/fcipipeline/api/pipelineLog/queryLogDetailById', //流水线job日志
    retryPipeline: 'fcipipeline/api/pipeline/retryPipeline', //重跑pipeline
    queryBranchesByPipelineId:
      '/fcipipeline/api/pipeline/queryBranchesByPipelineId', //根据流水线Id查分支列表
    queryCategory: '/fcipipeline/api/category/queryCategory', //查询插件分类,
    queryPluginDetail: '/fcipipeline/api/plugin/queryPluginDetail', //查询插件详情
    copyPipeline: '/fcipipeline/api/pipeline/copy', //复制流水线
    retryJob: '/fcipipeline/api/pipeline/retryJob', //重试单个job
    saveAsPipelineTemplate: '/fcipipeline/api/pipeline/saveAsPipelineTemplate', //流水线另存为模板
    queryMarkDown: '/fcipipeline/api/plugin/queryMarkDown', //查询markDown地址
    stopPipeline: '/fcipipeline/api/pipeline/stopPipeline', //停止整条流水线
    stopJob: '/fcipipeline/api/pipeline/stopJob', //停止单个job
    copyPipelineTemplate: '/fcipipeline/api/pipelineTemplate/copy',
    queryModelTemplateByContent:
      '/fcipipeline/api/plugin/queryEntityTemplateByContent',
    queryTypeAndContent: '/fcipipeline/api/pipeline/queryTypeAndContent', //queryContentByPathFormGit
    getMinioInfo: '/fcipipeline/api/pipeline/getMinioInfo', //queryContentByPathFormMinio
    saveToMinio: '/fcipipeline/api/pipeline/saveToMinio',
    getAllJobs: '/fcipipeline/api/jobs/getAllJobs',
    getFullJobsByIds: '/fcipipeline/api/jobs/getJobTemplateInfo',
    getAllRunnerCluster: '/fcipipeline/api/v4/getAllRunnerCluster',
    queryTriggerRules: '/fcipipeline/api/pipeline/queryTriggerRules',
    updateTriggerRules: '/fcipipeline/api/pipeline/updateTriggerRules',
    getYamlConfigById: '/fcipipeline/api/plugin/getYamlConfigById',
    addYamlConfig: '/fcipipeline/api/plugin/addYamlConfig',
    updateVisibleRange: '/fcipipeline/api/pipelineTemplate/updateVisibleRange',
    queryPluginResultData:
      '/fcipipeline/api/pluginResult/queryPluginResultData', //请求插件结果
    jobsUpdate: '/fcipipeline/api/jobs/update',
    delJobsTemplate: '/fcipipeline/api/jobs/delTemplate', // 删除任务组模板
    updateJobsVisibleRange: '/fcipipeline/api/jobs/updateVisibleRange',
    addJobsTemplate: '/fcipipeline/api/jobs/add', //新增任务组模板
    getRunnerClusterInfoByParam:
      '/fcipipeline/api/v4/getRunnerClusterInfoByParam', //获取当前用户runner集群
    getAllRunnerInfo: '/fcipipeline/api/v4/getAllRunnerInfo', //获取所有的runner
    addRunnerCluster: '/fcipipeline/api/v4/addRunnerCluster', //新增runner集群构建集群
    updateRunnerCluster: '/fcipipeline/api/v4/updateRunnerCluster', //删除，更新runnerCluster集群
    getPipelineHistoryVersion:
      'fcipipeline/api/pipeline/getPipelineHistoryVersion', //获取流水线历史版本信息
    setPipelineRollBack: '/fcipipeline/api/pipeline/setPipelineRollBack',
    downLoadArtifacts: '/fcipipeline/api/pipeline/downLoadArtifacts' //下载制品
  },
  fuser: {
    queryUserCoreData: '/fuser/api/user/queryUserCoreData', // 查询用户列表
    currentUser: '/fuser/api/user/currentUser' // 查询当前用户信息
  },
  fapp: {
    queryPagination: '/fapp/api/app/queryPagination',
    findById: '/fapp/api/app/findbyid',
    queryDutyAppBaseInfo: '/fapp/api/app/queryDutyAppBaseInfo'
  },
  // fentity
  fentity: {
    // 查询所有环境
    queryEnvList: '/fentity/api/env/queryList',

    // 查询实体列表
    // 查询当前应用有权限的所有实体列表,将configCi中的queryEntityModel接口全部换位querySectionEntity
    querySectionEntity: '/fentity/api/entity/querySectionEntity',

    // 查询实体模板
    queryTemplate: '/fentity/api/template/queryTemplate',

    //查询实体详情
    queryEntityModelDetail: '/fentity/api/entity/queryEntityModelDetail',

    //根据id查实体模板详情
    modelTempQuery: '/fentity/api/template/queryById',
    // queryServiceStatus: '/fapp/api/app/queryServiceStatus' //查询应用状态 使用中或废弃

    queryEnv: '/fentity/api/entity/queryEnv',
    addEntityModelClass: '/fentity/api/entity/addEntityModelClass',
    // queryTemplate: '/fentity/api/template/queryTemplate', // 查询实体模板
    // queryEntityModelDetail: '/fentity/api/entity/queryEntityModelDetail', // 查询实体模板详情
    // queryEnvList: '/fentity/api/env/queryList', // 查询全量环境列表
    queryUserGroup: '/fentity/api/entity/queryUserGroup', // 查用户所属条线
    addEntity: '/fentity/api/entity/addEntity', // 新建实体
    copyEntity: '/fentity/api/entity/copyEntity', // 新建实体
    checkEntity: '/fentity/api/entity/checkEntity', // 校验实体是否重复
    queryEntityModel: '/fentity/api/entity/queryEntityModel', // 查询实体模型列表
    queryTemplateById: '/fentity/api/template/queryById', // 根据id查实体模板详情
    addEntityClass: '/fentity/api/entity/addEntityClass', // 新增实体映射
    updateEntityClass: '/fentity/api/entity/updateEntityClass', // 编辑实体映射
    updateEntity: '/fentity/api/entity/updateEntity', //编辑实体(删除)
    deleteEntityClass: '/fentity/api/entity/deleteEntityClass', // 删除实体映射
    queryEntityLog: '/fentity/api/entity/queryEntityLog', // 查看实体操作日志信息
    queryMapLogList: '/fentity/api/entity/getMappingHistoryList', // 查看实体操作日志信息（映射值）
    queryConfigDependency: '/fentity/api/entity/queryConfigDependency', // 获取依赖分析
    queryDeployDependency: '/fentity/api/entity/queryDeployDependency', // 获取部署依赖
    deleteEntity: '/fentity/api/entity/deleteEntity', // 删除实体
    queryEnvTypeList: '/fentity/api/env/queryEnvTypeList' // 查询环境列表
  }
};
