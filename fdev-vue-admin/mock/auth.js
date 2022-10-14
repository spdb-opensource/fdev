const mockjs = require('mockjs');

const auth = mockjs.mock([
  {
    id: '@id',
    name_en: 'admin',
    name_cn: '超级管理员',
    permissions: [
      '/api/user/add',
      /*   新增用户   */
      '/api/user/update',
      /*   更新用户信息   */
      '/api/user/delete',
      /*   删除用户   */
      '/api/role/add',
      /*   新增角色   */
      '/api/role/update',
      /*   更新角色   */
      '/api/role/delete',
      /*   删除角色    */
      '/api/label/add',
      /*   新增标签  */
      '/api/label/update',
      /*   更新标签  */
      '/api/label/delete',
      /*    删除标签    */
      '/api/permission/add',
      /*   新增权限  */
      '/api/permission/update',
      /*   更新权限   */
      '/api/permission/delete',
      /*   删除权限     */
      '/api/company/add',
      /*   新增公司实体   */
      '/api/company/update',
      /*   更新公司实体   */
      '/api/company/delete',
      /*    删除公司实体   */
      '/api/group/add',
      /*   新增小组   */
      '/api/group/update',
      /*   更新小组   */
      '/api/group/delete'
      /*    删除小组    */
    ]
  },
  {
    id: '@id',
    name_en: 'group_admin',
    name_cn: '小组管理员',
    permissions: [
      '/api/user/add',
      /*   新增用户   */
      '/api/user/update',
      /*   更新用户信息   */
      '/api/user/delete',
      /*   删除用户   */
      '/api/label/add',
      /*   新增标签  */
      '/api/label/update',
      /*   更新标签  */
      '/api/label/delete'
      /*    删除标签    */
    ]
  },
  {
    id: '@id',
    name_en: 'user',
    name_cn: '普通人员',
    permissions: [
      '/api/user/update',
      /*   更新用户信息   */
      '/api/label/add',
      /*   新增标签  */
      '/api/label/update',
      /*   更新标签  */
      '/api/label/delete'
      /*    删除标签    */
    ]
  }
]);

module.exports = {
  'POST /fuser/api/permission/query': auth,
  'POST /fuser/api/permission/update': auth[0]
};
