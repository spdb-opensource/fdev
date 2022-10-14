const clickoutside = {
  // 初始化指令
  bind(el, binding, vnode) {
    if (binding.value === 'noAction') return;
    function documentHandler(e) {
      // 这里判断点击的元素是否是本身，是本身，则返回

      let ignore = e.path.some(
        x => x.className && x.className.includes('ignore-click')
      );
      if (el.contains(e.target) || ignore) {
        return false;
      } // 判断指令中是否绑定了函数
      if (binding.expression) {
        // 如果绑定了函数 则调用那个函数，此处binding.value就是handleClose方法
        binding.value(e);
      }
    } // 给当前元素绑定个私有变量，方便在unbind中可以解除事件监听
    el.__vueClickOutside__ = documentHandler;
    document.addEventListener('click', documentHandler);
  },
  update() {},
  unbind(el, binding) {
    // 解除事件监听
    document.removeEventListener('click', el.__vueClickOutside__);
    delete el.__vueClickOutside__;
  }
};
export const ClickoutsideMixIn = {
  data: () => {
    return {
      show: true
    };
  },
  directives: { clickoutside },
  methods: {
    handleClose(e) {
      this.open = false;
    }
  }
};

export const InputMixIn = {
  props: {
    size: { type: String, default: 'md' },
    rules: {
      type: Array,
      default: () => {
        return [];
      }
    }
  },
  data: () => {
    return {
      inputSize: { sm: 'input-sm', md: 'input-md', lg: 'input-lg' },
      showAlert: true
    };
  },
  computed: {
    alertWord() {
      let ind = this.rules.findIndex(x => x !== true);
      return this.rules[ind];
    },
    alert() {
      return this.rules.some(x => x !== true);
    }
  }
};
