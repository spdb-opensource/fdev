export function filterLabels(val) {
  if (val) {
    return val.join('，');
  }
}

export function chineseName(val) {
  return val
    .map(item => {
      return item.user_name_cn;
    })
    .join('，');
}
