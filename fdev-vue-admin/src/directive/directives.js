import Vue from 'vue';

// v-forbidMultipleClick: 提交以后禁用按钮一段时间，防止重复提交
Vue.directive('forbidMultipleClick', {
  inserted(el) {
    el.v_click = () => {
      el.classList.add('is-disabled');
      el.disabled = true;
      setTimeout(() => {
        el.disabled = false;
        el.classList.remove('is-disabled');
      }, 1000);
    };
    el.addEventListener('click', el.v_click);
  },
  unbind(el) {
    el.removeEventListener('click', el.v_click);
  }
});

// 方便用户查看表格列表数据
// 鼠标滑轮滚动 表格即可 左右滚动
Vue.directive('tableEasyScrollX', {
  inserted(el) {
    let table = el.getElementsByClassName('q-table__middle')[0];
    table &&
      table.addEventListener(
        'wheel',
        function(e) {
          e.preventDefault(); //阻止浏览器滚动
          this.scrollLeft -= e.wheelDelta;
        },
        false
      );
  }
});
