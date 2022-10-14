/**
 *  permission中只配置 roles, authority, specialGroup, kadianManager
 *  module/page/btn 模块 页面 按钮
 */
// 用户模块
export const userPermissions = {
  // 用户列表
  listPage: {
    // 添加用户
    userAdd: {
      roles: ['行内项目负责人', '厂商项目负责人'],
      authority: ['admin', 'group_admin', 'user'] //超级管理员， 人员 ，小组管理员
    }
  }
};

// 任务模块
export const jobPermissions = {
  // 任务详情
  profilePage: {
    // 修改按钮
    modify: {
      kadianManager: true,
      specialGroup: true
    }
  }
};
