import {
  queryEnv,
  addEntityModelClass,
  queryTemplate,
  queryEntityModelDetail,
  queryEnvList,
  querySystem,
  queryAppList,
  queryUserGroup,
  addEntity,
  copyEntity,
  checkEntity,
  queryApps,
  queryUserCoreData,
  queryEntityModel,
  queryTemplateById,
  addEntityClass,
  updateEntityClass,
  updateEntity,
  deleteEntityClass,
  queryEntityLog,
  queryMapLogList,
  queryConfigDependency,
  queryIsShow,
  queryCurrent,
  queryDeployDependency,
  deleteEntity,
  queryEnvTypeList
} from '../services/method';

export default {
  namespaced: true,
  state: {
    envObj: null, // 环境对象
    templateList: [], // 实体模板列表
    entityModelDetail: {}, // 实体详情对象
    envList: [], // 环境列表
    systemList: [], // 全量系统列表
    appList: [], // 全量应用列表
    userGroups: [], // 用户所属条线
    checkEntityFlag: {}, // 实体英文名校验
    entityList: [], // 实体列表
    userList: [], // 用户列表
    serviceList: [], // 应用列表
    templateDetail: null, // 实体模板详情对象
    mapLogList: {}, // 实体操作日志分页查询结果（映射值）
    entitiesLogList: {}, // 实体操作日志分页查询结果
    configDependency: {}, // 配置依赖分析
    deployDependency: {}, // 部署依赖
    isShow: '0',
    currentUser: null, // 当前用户信息
    copyEntityId: null
  },
  getters: {},
  actions: {
    // 查询环境
    async queryEnv({ commit }, payload) {
      const data = await queryEnv(payload);
      commit('saveEnvObj', data);
    },
    async addEntityModelClass({ commit }, payload) {
      await addEntityModelClass(payload);
    },
    // 查询全量实体模板
    async queryTemplate({ commit }, payload) {
      const data = await queryTemplate(payload);
      commit('saveTemplateList', data);
    },
    // 查询实体详情
    async queryEntityModelDetail({ commit }, payload) {
      const data = await queryEntityModelDetail(payload);
      commit('saveentityModelDetail', data);
    },
    // 查询全量环境列表
    async queryEnvList({ commit }, payload) {
      const data = await queryEnvList(payload);
      commit('saveEnvList', data);
    },
    // 查询所属系统
    async querySystem({ commit }, payload) {
      const data = await querySystem(payload);
      commit('saveSystemList', data);
    },
    // 应用列表分页查询
    async queryAppList({ commit }, payload) {
      const data = await queryAppList(payload);
      commit('saveAppList', data);
    },
    // 查用户所属条线
    async queryUserGroup({ commit }, payload) {
      const data = await queryUserGroup(payload);
      commit('saveUserGroup', data);
    },
    // 新建实体
    async addEntity({ commit }, payload) {
      await addEntity(payload);
    },
    // 复制实体
    async copyEntity({ commit }, payload) {
      const data = await copyEntity(payload);
      commit('saveCopyEntityId', data);
    },
    // 校验实体是否重复
    async checkEntity({ commit }, payload) {
      const data = await checkEntity(payload);
      commit('saveCheckEntityFlag', data);
    },
    // 查询全量应用列表
    async queryApps({ commit }, payload) {
      const data = await queryApps(payload);
      commit('saveServiceList', data);
    },
    // 查询全量用户列表
    async queryUserCoreData({ commit }, payload) {
      const data = await queryUserCoreData(payload);
      commit('saveUserList', data);
    },
    // 查询实体列表
    async queryEntityModel({ commit }, payload) {
      const data = await queryEntityModel(payload);
      commit('saveEntityList', data);
    },
    // 根据id查实体模板详情
    async queryTemplateById({ commit }, payload) {
      const data = await queryTemplateById(payload);
      commit('saveTemplateDetail', data);
    },
    // 新增实体映射
    async addEntityClass({ commit }, payload) {
      await addEntityClass(payload);
    },
    // 编辑实体映射
    async updateEntityClass({ commit }, payload) {
      await updateEntityClass(payload);
    },
    // 编辑实体
    async updateEntity({ commit }, payload) {
      await updateEntity(payload);
    },
    // 删除实体映射
    async deleteEntityClass({ commit }, payload) {
      await deleteEntityClass(payload);
    },
    // 查看实体操作日志信息
    async queryEntityLog({ commit }, payload) {
      const data = await queryEntityLog(payload);
      commit('saveEntityLogList', data);
    },
    // 查看实体操作日志信息（映射值）
    async queryMapLogList({ commit }, payload) {
      const data = await queryMapLogList(payload);
      commit('saveMapLogList', data);
    },
    // 获取依赖分析
    async queryConfigDependency({ commit }, payload) {
      const data = await queryConfigDependency(payload);
      commit('saveConfigDependency', data);
    },
    // 查询当前用户是否可查看应用详情
    async queryIsShow({ commit }, payload) {
      const response = await queryIsShow(payload);
      commit('saveIsShow', response);
    },
    // 查询当前用户信息
    async queryCurrent({ commit }, payload) {
      const response = await queryCurrent(payload);
      commit('saveCurrentUser', response);
    },
    // 获取部署依赖
    async queryDeployDependency({ commit }, payload) {
      const response = await queryDeployDependency(payload);
      commit('saveDeployDependency', response);
    },
    // 删除实体
    async deleteEntity({ commit }, payload) {
      await deleteEntity(payload);
    },
    // 查询环境列表
    async queryEnvTypeList({ commit }, payload) {
      const response = await queryEnvTypeList(payload);
      commit('saveEnvTypeList', response);
    }
  },
  mutations: {
    saveEnvObj(state, data) {
      state.envObj = data;
    },
    saveTemplateList(state, data) {
      state.templateList = data;
    },
    saveentityModelDetail(state, data) {
      state.entityModelDetail = data;
    },
    saveEnvList(state, data) {
      state.envList = data;
    },
    saveSystemList(state, data) {
      state.systemList = data;
    },
    saveAppList(state, data) {
      state.appList = data.serviceAppList;
    },
    saveUserGroup(state, data) {
      state.userGroups = data;
    },
    saveCopyEntityId(state, data) {
      state.copyEntityId = data;
    },
    saveCheckEntityFlag(state, data) {
      state.checkEntityFlag = data;
    },
    saveEntityList(state, data) {
      state.entityList = data;
    },
    saveUserList(state, data) {
      state.userList = data;
    },
    saveServiceList(state, data) {
      state.serviceList = data;
    },
    saveTemplateDetail(state, data) {
      state.templateDetail = data;
    },
    saveMapLogList(state, data) {
      state.mapLogList = data;
    },
    saveEntityLogList(state, data) {
      state.entitiesLogList = data;
    },
    saveConfigDependency(state, data) {
      state.configDependency = data;
    },
    saveIsShow(state, payload) {
      state.isShow = payload.isShow;
    },
    saveCurrentUser(state, data) {
      state.currentUser = data;
    },
    saveDeployDependency(state, data) {
      state.deployDependency = data;
    },
    saveEnvTypeList(state, data) {
      state.envTypeList = data;
    }
  }
};
