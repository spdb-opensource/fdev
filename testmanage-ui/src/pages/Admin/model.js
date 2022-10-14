export function createAdminModel() {
  return {
    form: {
      user_en_name: '',
      user_name_cn: '',
      user_phone: '',
      email: '',
      role_en_name: '',
      group_id: ''
    }
  };
}

export function createFunctionMenu() {
  return {
    formAdd: {
      systemName: '',
      firstMenu: '',
      secondMenu: '',
      thirdMenu: '',
      fourthMenu: '',
      fifthMenu: '',
      sixthMenu: '',
      seventhMenu: ''
    }
  };
}
export const groupName = {
  1: '财富-金牛座',
  2: '微生活-白羊座',
  3: '互联-双子座',
  4: '转账-天平座',
  6: 'app-水瓶座',
  7: '账户-射手座',
  8: '生态-巨蟹座',
  9: '支付组-星纪',
  10: '公司组-北极星',
  12: '公共支持组',
  13: '个人金融领域',
  14: '公司金融领域',
  15: '资源池'
};
//测试阶段
export const testStage = {
  '0': '待分配',
  '1': '开发中',
  '2': 'SIT',
  '3': 'UAT',
  '4': '已投产',
  '6': 'UAT(含风险)',
  '8': '废弃',
  '9': '分包测试(内测完成)',
  '10': '分包测试(含风险)'
};
export const rules = {
  funcMenu: [
    { required: true, message: '请选择功能模块', trigger: ['blur', 'change'] }
  ],
  systemName: [
    { required: true, message: '请选择系统名称', trigger: ['blur', 'change'] }
  ]
};
