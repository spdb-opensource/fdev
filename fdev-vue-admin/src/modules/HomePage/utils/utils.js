export function sort(a, b) {
  let order = [
    '录入信息完成',
    '录入应用完成',
    '录入分支完成',
    '进行中',
    '开发中',
    'SIT测试',
    'UAT测试',
    'REL测试',
    '已投产',
    '已完成',
    '已归档'
  ];
  return order.indexOf(a) - order.indexOf(b);
}

export function setOperation(stage) {
  let num = 0;
  if (stage === 'production') {
    num = 2;
  } else if (
    stage === 'sit' ||
    stage === 'uat' ||
    stage === 'rel' ||
    stage === 'develop'
  ) {
    num = 1;
  }
  return num;
}
