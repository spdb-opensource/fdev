module.exports = {
  wrap7A: function(obj) {
    return {
      code: 'AAAAAAA',
      msg: '成功',
      data: obj
    };
  },
  wrapError: function(obj) {
    return {
      code: obj.code || 'not6A',
      msg: obj.msg || 'error',
      data: obj
    };
  }
};
