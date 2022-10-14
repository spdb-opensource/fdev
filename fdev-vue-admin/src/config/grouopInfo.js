function ObjectId(id) {
  return id;
}
const groupInfo = [
  /* 1 */
  {
    _id: ObjectId('5e142479e5ae8e000c28bd04'),
    id: '5e142479e5ae8e000c28bd04',
    name: 'mpaas',
    parent_id: '5c81c56cd3e2a1126ce3004b',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-4-1'
  },

  /* 2 */
  {
    _id: ObjectId('5d3e9405606eeb000a22d324'),
    id: '5d3e9405606eeb000a22d324',
    name: '板块5-公共组',
    parent_id: '5d3ebdc2606eeb000a22d335',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-1-5'
  },

  /* 3 */
  {
    _id: ObjectId('5c9dc899d1785ec6a04bf430'),
    id: '5c81c56cd3e2a1126ce301c4',
    name: '公司组',
    count: 0,
    parent_id: '5c81c4d0d3e2a1126ce30049',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '0',
    sortNum: '1-6'
  },

  /* 4 */
  {
    _id: ObjectId('6efc7e46b12e394a457ea013'),
    id: '5c81c4d0d3e2a1126ce30049',
    name: '互联网应用',
    count: '0',
    parent_id: '',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1'
  },

  /* 5 */
  {
    _id: ObjectId('5d3e9411606eeb000a22d325'),
    id: '5d3e9411606eeb000a22d325',
    name: '板块6-APP组',
    parent_id: '5d3ebdc2606eeb000a22d335',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-1-10'
  },

  /* 6 */
  {
    _id: ObjectId('5d3e9418606eeb000a22d326'),
    id: '5d3e9418606eeb000a22d326',
    name: '板块7-账户组',
    parent_id: '5d3ebdc2606eeb000a22d335',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-1-7'
  },

  /* 7 */
  {
    _id: ObjectId('5e142494e5ae8e000c28bd06'),
    id: '5e142494e5ae8e000c28bd06',
    name: '集成架构',
    parent_id: '5c81c56cd3e2a1126ce3004b',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-4-2'
  },

  /* 8 */
  {
    _id: ObjectId('5e1424c8e5ae8e000c28bd09'),
    id: '5e1424c8e5ae8e000c28bd09',
    name: '安全',
    parent_id: '5c81c56cd3e2a1126ce3004b',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-4-3'
  },

  /* 9 */
  {
    _id: ObjectId('5d3e93f5606eeb000a22d322'),
    id: '5d3e93f5606eeb000a22d322',
    name: '板块3-互联组',
    parent_id: '5d3ebdc2606eeb000a22d335',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-1-3'
  },

  /* 10 */
  {
    _id: ObjectId('5d42bd09e6907b000ac8800b'),
    id: '5d42bd09e6907b000ac8800b',
    name: '测试服务团队',
    parent_id: '5c81c4d0d3e2a1126ce30049',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '0',
    sortNum: '1-5'
  },

  /* 11 */
  {
    _id: ObjectId('5e1424c3e5ae8e000c28bd08'),
    id: '5e1424c3e5ae8e000c28bd08',
    name: '基础应用',
    parent_id: '5c81c56cd3e2a1126ce3004b',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-4-4'
  },

  /* 12 */
  {
    _id: ObjectId('5c9dc899d1785ec6a04bf42f'),
    id: '5c81c56cd3e2a1126ce3004b',
    name: '公共组',
    count: 0,
    parent_id: '5c81c4d0d3e2a1126ce30049',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-4'
  },

  /* 13 */
  {
    _id: ObjectId('5c9dc899d1785ec6a04bf431'),
    id: '5c81c56cd3e2a1986ce30aw1',
    name: '移动组',
    count: 0,
    parent_id: '5c81c4d0d3e2a1126ce30049',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '0',
    sortNum: '1-7'
  },

  /* 14 */
  {
    _id: ObjectId('5e1532466403f8210fac08f5'),
    id: '4cf6c56cd31za1126ce303l5',
    name: '个人组',
    count: 0,
    parent_id: '5c81c4d0d3e2a1126ce30049',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '0',
    sortNum: '1-8'
  },

  /* 15 */
  {
    _id: ObjectId('5d3e93ce606eeb000a22d320'),
    id: '5d3e93ce606eeb000a22d320',
    name: '板块1-财富组',
    parent_id: '5d3ebdc2606eeb000a22d335',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-1-1'
  },

  /* 16 */
  {
    _id: ObjectId('5e14248be5ae8e000c28bd05'),
    id: '5e14248be5ae8e000c28bd05',
    name: '应用架构',
    parent_id: '5c81c56cd3e2a1126ce3004b',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-4-5'
  },

  /* 17 */
  {
    _id: ObjectId('5e5885cd13149c000c4a2777'),
    id: '5e5885cd13149c000c4a2777',
    name: '公司金融组',
    count: 0,
    parent_id: '5c81c4d0d3e2a1126ce30049',
    status: '1',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    sortNum: '1-2'
  },

  /* 18 */
  {
    _id: ObjectId('5c9dc899d1785ec6a04bf434'),
    id: '5nsi28a123adash23742as21',
    name: '移动营销组',
    count: 0,
    parent_id: '5c81c4d0d3e2a1126ce30049',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '0',
    sortNum: '1-9'
  },

  /* 19 */
  {
    _id: ObjectId('5d42be1be6907b000ac8800d'),
    id: '5d42be1be6907b000ac8800d',
    name: '公共服务领域',
    parent_id: '5d42bd09e6907b000ac8800b',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '0',
    sortNum: '1-5-8'
  },

  /* 20 */
  {
    _id: ObjectId('5c9dc899d1785ec6a04bf433'),
    id: '123asdasb3241ad13adada13',
    name: '支付组',
    count: 0,
    parent_id: '5c81c4d0d3e2a1126ce30049',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-3'
  },

  /* 21 */
  {
    _id: ObjectId('5cb97dc13358ba00077075fe'),
    id: '5cb97dc13358ba00077075fe',
    name: '登录微服务小组',
    parent_id: '5c81c56cd3e2a1986ce30aw1',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '0',
    sortNum: '1-7-1'
  },

  /* 22 */
  {
    _id: ObjectId('5d42bed6e6907b000ac88010'),
    id: '5d42bed6e6907b000ac88010',
    name: '个人金融领域',
    parent_id: '5d42bd09e6907b000ac8800b',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '0',
    sortNum: '1-5-9'
  },

  /* 23 */
  {
    _id: ObjectId('5d3e93fe606eeb000a22d323'),
    id: '5d3e93fe606eeb000a22d323',
    name: '板块4-转账组',
    parent_id: '5d3ebdc2606eeb000a22d335',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-1-4'
  },

  /* 24 */
  {
    _id: ObjectId('5d42be58e6907b000ac8800e'),
    id: '5d42be58e6907b000ac8800e',
    name: '支付平台领域',
    parent_id: '5d42bd09e6907b000ac8800b',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '0',
    sortNum: '1-5-10'
  },

  /* 25 */
  {
    _id: ObjectId('5cb97dee3358ba00077075ff'),
    id: '5cb97dee3358ba00077075ff',
    name: '统一收银台小组',
    parent_id: '5c81c56cd3e2a1986ce30aw1',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '0',
    sortNum: '1-7-2'
  },

  /* 26 */
  {
    _id: ObjectId('5d3e93ed606eeb000a22d321'),
    id: '5d3e93ed606eeb000a22d321',
    name: '板块2-微生活组',
    parent_id: '5d3ebdc2606eeb000a22d335',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-1-2'
  },

  /* 27 */
  {
    _id: ObjectId('5e1d7fc8a10549000c50cbcd'),
    id: '5e1d7fc8a10549000c50cbcd',
    name: '效能',
    parent_id: '5c81c56cd3e2a1126ce3004b',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-4-6'
  },

  /* 28 */
  {
    _id: ObjectId('5d3ebdc2606eeb000a22d335'),
    id: '5d3ebdc2606eeb000a22d335',
    name: '零售金融组',
    parent_id: '5c81c4d0d3e2a1126ce30049',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-1'
  },

  /* 29 */
  {
    _id: ObjectId('5d42be6ce6907b000ac8800f'),
    id: '5d42be6ce6907b000ac8800f',
    name: '公司金融领域',
    parent_id: '5d42bd09e6907b000ac8800b',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '0',
    sortNum: '1-5-11'
  },

  /* 30 */
  {
    _id: ObjectId('5d42bef3e6907b000ac88012'),
    id: '5d42bef3e6907b000ac88012',
    name: '移动pad领域',
    parent_id: '5d42bd09e6907b000ac8800b',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '0',
    sortNum: '1-5-12'
  },

  /* 31 */
  {
    _id: ObjectId('5d6c84ce054583000aa4e9af'),
    id: '5d6c84ce054583000aa4e9af',
    name: '板块8-生态组',
    parent_id: '5d3ebdc2606eeb000a22d335',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    status: '1',
    sortNum: '1-1-8'
  },

  /* 32 */
  {
    _id: ObjectId('5ec219daddbdbe000d6e0c84'),
    id: '5ec219daddbdbe000d6e0c84',
    name: '测试中心',
    count: 0,
    status: '1',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    sortNum: '2'
  },

  /* 33 */
  {
    _id: ObjectId('5ec219efddbdbe000d6e0c85'),
    id: '5ec219efddbdbe000d6e0c85',
    name: '业务部门',
    count: 0,
    status: '1',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    sortNum: '3'
  },

  /* 34 */
  {
    _id: ObjectId('5ec219f7ddbdbe000d6e0c86'),
    id: '5ec219f7ddbdbe000d6e0c86',
    name: '规划部门',
    count: 0,
    status: '1',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    sortNum: '4'
  },

  /* 35 */
  {
    _id: ObjectId('5ec2332bddbdbe000d6e0cc7'),
    id: '5ec2332bddbdbe000d6e0cc7',
    name: '一组',
    count: 0,
    parent_id: '5d3e93ce606eeb000a22d320',
    status: '0',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    sortNum: '1-1-1-1'
  },

  /* 36 */
  {
    _id: ObjectId('5ec2332fddbdbe000d6e0cc8'),
    id: '5ec2332fddbdbe000d6e0cc8',
    name: '二组',
    count: 0,
    parent_id: '5d3e93ce606eeb000a22d320',
    status: '0',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    sortNum: '1-1-1-2'
  },

  /* 37 */
  {
    _id: ObjectId('5ec23333ddbdbe000d6e0cc9'),
    id: '5ec23333ddbdbe000d6e0cc9',
    name: '三组',
    count: 0,
    parent_id: '5d3e93ce606eeb000a22d320',
    status: '0',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    sortNum: '1-1-1-3'
  },

  /* 38 */
  {
    _id: ObjectId('5ec23337ddbdbe000d6e0cca'),
    id: '5ec23337ddbdbe000d6e0cca',
    name: '四组',
    count: 0,
    parent_id: '5d3e93ce606eeb000a22d320',
    status: '0',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    sortNum: '1-1-1-4'
  },

  /* 39 */
  {
    _id: ObjectId('5ec2333bddbdbe000d6e0ccb'),
    id: '5ec2333bddbdbe000d6e0ccb',
    name: '五组',
    count: 0,
    parent_id: '5d3e93ce606eeb000a22d320',
    status: '0',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    sortNum: '1-1-1-5'
  },

  /* 40 */
  {
    _id: ObjectId('5ec23341ddbdbe000d6e0ccc'),
    id: '5ec23341ddbdbe000d6e0ccc',
    name: '六组',
    count: 0,
    parent_id: '5d3e93ce606eeb000a22d320',
    status: '0',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    sortNum: '1-1-1-6'
  },

  /* 41 */
  {
    _id: ObjectId('5ec23347ddbdbe000d6e0ccd'),
    id: '5ec23347ddbdbe000d6e0ccd',
    name: '七组',
    count: 0,
    parent_id: '5d3e93ce606eeb000a22d320',
    status: '0',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    sortNum: '1-1-1-7'
  },

  /* 42 */
  {
    _id: ObjectId('5ec23351ddbdbe000d6e0cce'),
    id: '5ec23351ddbdbe000d6e0cce',
    name: '内测',
    count: 0,
    parent_id: '5d3e93ce606eeb000a22d320',
    status: '0',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    sortNum: '1-1-1-8'
  },

  /* 43 */
  {
    _id: ObjectId('5efc1b78e74f58000c28aa3d'),
    id: '5efc1b78e74f58000c28aa3d',
    name: '内测',
    count: 0,
    parent_id: '5c81c56cd3e2a1126ce3004b',
    status: '1',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group',
    sortNum: '1-4-7'
  },

  /* 44 */
  {
    _id: ObjectId('5f27de7a9970bc000cd62dd0'),
    id: '5f27de7a9970bc000cd62dd0',
    name: '武汉公共研发',
    count: 0,
    parent_id: '5c81c56cd3e2a1126ce3004b',
    status: '1',
    sortNum: '1-4-8',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 45 */
  {
    _id: ObjectId('5f3a5c2f8b67a7000cd5f6ee'),
    id: '5f3a5c2f8b67a7000cd5f6ee',
    name: '公司网银领域',
    count: 0,
    parent_id: '5e5885cd13149c000c4a2777',
    status: '1',
    sortNum: '1-2-1',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 46 */
  {
    _id: ObjectId('5f3a5c3a8b67a7000cd5f6ef'),
    id: '5f3a5c3a8b67a7000cd5f6ef',
    name: '公司手机微信领域',
    count: 0,
    parent_id: '5e5885cd13149c000c4a2777',
    status: '1',
    sortNum: '1-2-2',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 47 */
  {
    _id: ObjectId('5f3a5c4c8b67a7000cd5f6f1'),
    id: '5f3a5c4c8b67a7000cd5f6f1',
    name: '海外托管业务场景领域',
    count: 0,
    parent_id: '5e5885cd13149c000c4a2777',
    status: '1',
    sortNum: '1-2-3',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 48 */
  {
    _id: ObjectId('5f3a5c578b67a7000cd5f6f2'),
    id: '5f3a5c578b67a7000cd5f6f2',
    name: '同业互动营销领域',
    count: 0,
    parent_id: '5e5885cd13149c000c4a2777',
    status: '1',
    sortNum: '1-2-4',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 49 */
  {
    _id: ObjectId('5f3a5c608b67a7000cd5f6f3'),
    id: '5f3a5c608b67a7000cd5f6f3',
    name: '托管领域',
    count: 0,
    parent_id: '5e5885cd13149c000c4a2777',
    status: '1',
    sortNum: '1-2-5',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 50 */
  {
    _id: ObjectId('5f3a5c698b67a7000cd5f6f4'),
    id: '5f3a5c698b67a7000cd5f6f4',
    name: '公司金融微服务',
    count: 0,
    parent_id: '5e5885cd13149c000c4a2777',
    status: '1',
    sortNum: '1-2-6',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 51 */
  {
    _id: ObjectId('5f3a5c778b67a7000cd5f6f5'),
    id: '5f3a5c778b67a7000cd5f6f5',
    name: '公共安全服务',
    count: 0,
    parent_id: '5e5885cd13149c000c4a2777',
    status: '1',
    sortNum: '1-2-7',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 52 */
  {
    _id: ObjectId('5f3a5c7e8b67a7000cd5f6f6'),
    id: '5f3a5c7e8b67a7000cd5f6f6',
    name: '移动营销',
    count: 0,
    parent_id: '5e5885cd13149c000c4a2777',
    status: '1',
    sortNum: '1-2-8',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 53 */
  {
    _id: ObjectId('5f3a5c828b67a7000cd5f6f7'),
    id: '5f3a5c828b67a7000cd5f6f7',
    name: '内测',
    count: 0,
    parent_id: '5e5885cd13149c000c4a2777',
    status: '1',
    sortNum: '1-2-9',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 54 */
  {
    _id: ObjectId('5f470b2b952d82000ccd7863'),
    id: '5f470b2b952d82000ccd7863',
    name: '自动化测试组',
    count: 0,
    parent_id: '5d3ebdc2606eeb000a22d335',
    status: '1',
    sortNum: '1-1-11',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 55 */
  {
    _id: ObjectId('5f52016f38d931000cb9c285'),
    id: '5f52016f38d931000cb9c285',
    name: '数据中心',
    count: 0,
    status: '1',
    sortNum: '5',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 56 */
  {
    _id: ObjectId('5f895f6f38ff4f000c50f1ec'),
    id: '5f895f6f38ff4f000c50f1ec',
    name: '成都公司金融领域',
    count: 0,
    parent_id: '5e5885cd13149c000c4a2777',
    status: '1',
    sortNum: '1-2-10',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 57 */
  {
    _id: ObjectId('5f9938979513dd000c0a19a4'),
    id: '5f9938979513dd000c0a19a4',
    name: '其他',
    count: 0,
    status: '1',
    sortNum: '6',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 58 */
  {
    _id: ObjectId('5f9a60899513dd000c0a1b4c'),
    id: '5f9a60899513dd000c0a1b4c',
    name: '板块9-蓉亿组',
    count: 0,
    parent_id: '5d3ebdc2606eeb000a22d335',
    status: '1',
    sortNum: '1-1-9',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 59 */
  {
    _id: ObjectId('5f9bc3479513dd000c0a1e68'),
    id: '5f9bc3479513dd000c0a1e68',
    name: '板块6-投资组',
    count: 0,
    parent_id: '5d3ebdc2606eeb000a22d335',
    status: '1',
    sortNum: '1-1-6',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 60 */
  {
    _id: ObjectId('5fa8e0f278f923000c72e2ff'),
    id: '5fa8e0f278f923000c72e2ff',
    name: '中间条线',
    count: 0,
    status: '1',
    sortNum: '7',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  },

  /* 61 */
  {
    _id: ObjectId('5fa8efda78f923000c72e3a4'),
    id: '5fa8efda78f923000c72e3a4',
    name: '临时组',
    count: 0,
    parent_id: '5c81c4d0d3e2a1126ce30049',
    status: '1',
    sortNum: '1-10',
    _class: 'com.spdb.fdev.fuser.spdb.entity.user.Group'
  }
];

export default groupInfo;
