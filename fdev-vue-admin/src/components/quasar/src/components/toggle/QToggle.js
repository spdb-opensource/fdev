import Vue from 'vue';

import QIcon from '../icon/QIcon.js';
import CheckboxMixin from '../../mixins/checkbox.js';

export default Vue.extend({
  name: 'QToggle',

  mixins: [CheckboxMixin],

  props: {
    icon: String,
    checkedIcon: String,
    uncheckedIcon: String,
    indeterminateIcon: String,

    iconColor: String
  },

  computed: {
    computedIcon() {
      return (
        (this.isTrue === true
          ? this.checkedIcon
          : this.isIndeterminate === true
          ? this.indeterminateIcon
          : this.uncheckedIcon) || this.icon
      );
    },

    computedIconColor() {
      if (this.isTrue === true) {
        return this.iconColor;
      } else return null;
    }
  },

  methods: {
    __getInner(h) {
      return [
        h('div', {
          staticClass: 'q-toggle__track',
          style: {
            height: this.size === 'lg' ? '24px' : '20px'
          }
        }),

        h(
          'div',
          {
            staticClass: 'q-toggle__thumb absolute flex flex-center no-wrap',
            style: {
              width: this.size === 'lg' ? '20px' : '16px',
              height: this.size === 'lg' ? '20px' : '16px'
            }
          },
          this.computedIcon !== void 0
            ? [
                h(QIcon, {
                  props: {
                    name: this.computedIcon,
                    color: this.computedIconColor
                  }
                })
              ]
            : void 0
        )
      ];
    }
  },

  created() {
    this.type = 'toggle';
  }
});
