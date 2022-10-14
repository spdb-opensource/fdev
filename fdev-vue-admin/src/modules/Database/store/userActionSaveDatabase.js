export default {
  namespaced: true,
  modules: {
    //数据字典登记表
    DataDictionaryListRegister: {
      namespaced: true,
      state: {
        dictionaryListModel: {
          sys_id: '', //所属系统
          database_type: '', //数据库类型
          database_name: '', //数据库名
          field_en_name: '', //字段英文名
          system_name_cn: '', //系统中文名
          name: '', //表名
          database_tableName: '' //数据库表名
        },
        visibleColumns: [
          'system_name_cn',
          'database_type',
          'database_name',
          'reviewers',
          'tableName',
          'appName',
          'btn'
        ] //可视列筛选框
      },
      mutations: {
        updateSys_id(state, payload) {
          state.dictionaryListModel.sys_id = payload;
        },
        updateDatabase_type(state, payload) {
          state.dictionaryListModel.database_type = payload;
        },
        updateDatabase_name(state, payload) {
          state.dictionaryListModel.database_name = payload;
        },
        updateName(state, payload) {
          state.dictionaryListModel.name = payload;
        },
        updatevisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    //应用与库表
    DatabaseList: {
      namespaced: true,
      state: {
        searchListModel: {
          appid: '', //应用
          database_type: '', //数据库类型
          database_name: '', //库名
          status: '', //状态
          table_name: '', //表名
          spdb_manager: '' //行内应用负责人
        },
        visibleColumns: [
          'appName_cn',
          'appName_en',
          'database_type',
          'database_name',
          'table_name',
          'manager',
          'btn'
        ] //可视列筛选框
      },
      mutations: {
        updateAppid(state, payload) {
          state.searchListModel.appid = payload;
        },
        updateStatus(state, payload) {
          state.searchListModel.status = payload;
        },
        updateSpdb_manager(state, payload) {
          state.searchListModel.spdb_manager = payload;
        },
        updateDatabase_type(state, payload) {
          state.searchListModel.database_type = payload;
        },
        updateDatabase_name(state, payload) {
          state.searchListModel.database_name = payload;
        },
        updateTableName(state, payload) {
          state.searchListModel.table_name = payload;
        },
        updatevisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    },
    //数据字典
    DataDictionaryList: {
      namespaced: true,
      state: {
        dictionaryListModel: {
          sys_id: '', //所属系统
          database_type: '', //数据库类型
          database_name: '', //数据库名
          field_en_name: '' //字段英文名
        },
        visibleColumns: [
          'system_name_cn',
          'database_type',
          'database_name',
          'reviewers',
          'tableName',
          'appName',
          'btn'
        ] //可视列筛选框
      },
      mutations: {
        updateSys_id(state, payload) {
          state.dictionaryListModel.sys_id = payload;
        },
        updateDatabase_type(state, payload) {
          state.dictionaryListModel.database_type = payload;
        },
        updateDatabase_name(state, payload) {
          state.dictionaryListModel.database_name = payload;
        },
        updateName(state, payload) {
          state.dictionaryListModel.field_en_name = payload;
        },
        updatevisibleColumns(state, payload) {
          state.visibleColumns = payload;
        }
      }
    }
  }
};
