/*
本文件公用的注释如下：
visibleCols  用户可选的列
currentPage  页面分页信息
**/
export default {
  namespaced: true,
  modules: {
    modelList: {
      namespaced: true,
      state: {
        visibleCols: [
          'nameEn',
          'nameCn',
          'templateName',
          'createUserName',
          'createTime',
          'updateUserName',
          'updateTime',
          'handle'
        ]
        // currentPage: {
        //   rowsPerPage: 5,
        //   page: 1,
        //   rowsNumber: 0
        // },
      },
      mutations: {
        saveVisibleColumns(state, payload) {
          state.visibleCols = payload;
        }
      }
    }
  }
};
