export default {
  namespaced: true,
  modules: {
    //公告
    noticeAnnounce: {
      namespaced: true,
      state: {
        filter: '', //公告搜索的内容
        terms: [], //搜索栏绑定的数组
        announceFilter: 'all' //公告类型筛选框
      },
      mutations: {
        updateFilter(state, payload) {
          state.filter = payload;
        },
        updateAnnounceFilter(state, payload) {
          state.announceFilter = payload;
        },
        updateTerms(state, payload) {
          state.terms = payload;
        }
      }
    },
    //消息
    noticeMessage: {
      namespaced: true,
      state: {
        typeMsg: '', //未读通知消息类型
        tab: 'newNotices',
        oldVisibleColumns: ['content', 'create_time', 'type'] //已读消息选择列
      },
      mutations: {
        updateTypeMsg(state, payload) {
          state.typeMsg = payload;
        },
        updateTab(state, payload) {
          state.tab = payload;
        },
        updateOldVisibleColumns(state, payload) {
          state.oldVisibleColumns = payload;
        }
      }
    }
  }
};
