export default {
  name: 'BtnCheckbox',
  data() {
    return {
      isTotal: false
    };
  },
  props: {
    value: {
      type: Array,
      required: true
    },
    // To avoid seeing the active raise shadow through the transparent button, give it a color (even white).
    color: String,
    textColor: String,
    toggleColor: {
      type: String,
      default: 'primary'
    },
    toggleTextColor: String,
    options: {
      type: Array,
      required: true,
      validator: v =>
        v.every(opt => ('label' in opt || 'icon' in opt) && 'value' in opt)
    },
    readonly: Boolean,
    disable: Boolean,
    noCaps: Boolean,
    noWrap: Boolean,
    outline: Boolean,
    flat: Boolean,
    rounded: Boolean,
    push: Boolean,
    size: String,
    glossy: Boolean,
    noRipple: Boolean,
    waitForRipple: Boolean,
    wrapTotal: Boolean,
    field: String
  },
  computed: {
    val() {
      return this.options.map(opt =>
        this.value.some(each => each === opt.value)
      );
    }
  },
  methods: {
    set(value, opt) {
      if (this.readonly) {
        return;
      }
      if (
        !this.value.some((val, ind) => {
          if (val === value) {
            this.isTotal = false;
            this.value.splice(ind, 1);
            return true;
          }
        })
      ) {
        this.value.push(value);
        if (this.value.length === this.options.length) {
          this.isTotal = true;
        }
      }
      this.$emit('input', this.value, opt);
      this.$nextTick(() => {
        this.$emit('change', this.value, opt);
      });
    },
    setTotal() {
      if (this.readonly) {
        return;
      }
      this.isTotal = !this.isTotal;
      this.value.splice(0, this.value.length);
      if (this.isTotal) {
        this.value.push(...this.options.map(opt => opt.value));
      }
      //
      this.$emit('input', this.value);
      this.$nextTick(() => {
        this.$emit('change', this.value);
      });
    }
  },
  render(h) {
    return h(
      'div',
      {
        staticClass: 'btn-checkbox row q-gutter-x-sm q-gutter-y-xs',
        props: {
          outline: this.outline,
          flat: this.flat,
          rounded: this.rounded,
          push: this.push
        }
      },
      [
        this.field
          ? h(
              'div',
              {
                staticClass: 'field',
                props: {
                  outline: this.outline,
                  flat: this.flat,
                  rounded: this.rounded,
                  push: this.push
                }
              },
              [this.field, '：']
            )
          : undefined,
        this.wrapTotal
          ? h('div', {}, [
              h('FdevBtn', {
                staticClass: 'btn-checkbox no-shadow',
                key: '__total',
                on: { click: () => this.setTotal() },
                props: {
                  label: '全部',
                  color: this.isTotal ? this.toggleColor : this.color,
                  textColor: this.isTotal
                    ? this.toggleTextColor
                    : this.textColor,
                  noCaps: this.noCaps,
                  noWrap: this.noWrap,
                  outline: this.outline,
                  flat: this.flat,
                  rounded: this.rounded,
                  push: this.push,
                  glossy: this.glossy,
                  size: this.size
                }
              })
            ])
          : undefined,
        ...this.options.map((opt, i) =>
          h('div', {}, [
            h('FdevBtn', {
              staticClass: 'btn-checkbox no-shadow',
              key: `${opt.label}${opt.icon}${opt.iconRight}`,
              on: { click: () => this.set(opt.value, opt) },
              props: {
                disable: this.disable || opt.disable,
                label: opt.label,
                // Colors come from the button specific options first, then from general props
                color: this.val[i]
                  ? opt.toggleColor || this.toggleColor
                  : opt.color || this.color,
                textColor: this.val[i]
                  ? opt.toggleTextColor || this.toggleTextColor
                  : opt.textColor || this.textColor,
                icon: opt.icon,
                iconRight: opt.iconRight,
                noCaps: this.noCaps || opt.noCaps,
                noWrap: this.noWrap || opt.noWrap,
                outline: this.outline,
                flat: this.flat || !this.value.includes(opt.value),
                rounded: this.rounded,
                push: this.push,
                glossy: this.glossy,
                size: this.size,
                noRipple: this.noRipple || opt.noRipple,
                waitForRipple: this.waitForRipple || opt.waitForRipple,
                tabindex: opt.tabindex
              }
            })
          ])
        )
      ]
    );
  }
};
