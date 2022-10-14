import AlignMixin from './align.js';
import RippleMixin from './ripple.js';
import ListenersMixin from './listeners.js';
import { getSizeMixin } from './size.js';

const padding = {
  none: 0,
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32
};

export default {
  mixins: [
    ListenersMixin,
    RippleMixin,
    AlignMixin,
    getSizeMixin({
      xs: 8,
      sm: 10,
      md: 14,
      lg: 20,
      xl: 24
    })
  ],

  props: {
    type: String,

    to: [Object, String],
    replace: Boolean,
    append: Boolean,

    label: [Number, String],
    icon: String,
    iconRight: String,
    ficon: String,

    round: Boolean,
    outline: Boolean,
    flat: Boolean,
    unelevated: Boolean,
    rounded: Boolean,
    push: Boolean,
    glossy: Boolean,
    menu: Boolean,
    dropdown: Boolean,
    dialog: Boolean,

    size: String,
    fab: Boolean,
    fabMini: Boolean,
    padding: String,
    dash: Boolean,
    roundash: Boolean,
    normal: Boolean,

    color: { type: String, default: 'primary' },
    textColor: String,
    noCaps: { type: Boolean, default: true },
    noWrap: Boolean,
    dense: Boolean,

    tabindex: [Number, String],

    align: { default: 'center' },
    stack: Boolean,
    stretch: Boolean,
    loading: {
      type: Boolean,
      default: null
    },
    disable: Boolean,
    plain: Boolean,
    flatColorless: Boolean
  },
  data() {
    return {
      isDropdown: this.menu ? false : this.dropdown
    };
  },

  computed: {
    style() {
      if (this.fab === false && this.fabMini === false) {
        return this.sizeStyle;
      }
    },

    isRounded() {
      return (
        this.rounded === true || this.fab === true || this.fabMini === true
      );
    },

    isActionable() {
      return this.disable !== true && this.loading !== true;
    },

    computedTabIndex() {
      return this.isActionable === true ? this.tabindex || 0 : -1;
    },

    hasRouterLink() {
      return (
        this.disable !== true &&
        this.to !== void 0 &&
        this.to !== null &&
        this.to !== ''
      );
    },

    isLink() {
      return this.type === 'a' || this.hasRouterLink === true;
    },

    design() {
      if (this.plain === true) return 'plain';
      if (this.flat === true) return 'flat';
      if (this.outline === true) return 'outline';
      if (this.push === true) return 'push';
      if (this.unelevated === true) return 'unelevated';
      if (this.roundash === true) return 'roundash';
      if (this.dash === true) return 'dash';
      if (this.normal === true) return 'normal';
      if (this.menu === true) return 'menu';
      if (this.isDropdown === true) return 'dropdown';
      return 'standard';
    },

    currentLocation() {
      if (this.hasRouterLink === true) {
        // we protect from accessing this.$route without
        // actually needing it so that we won't trigger
        // unnecessary updates
        return this.append === true
          ? this.$router.resolve(this.to, this.$route, true)
          : this.$router.resolve(this.to);
      }
    },

    attrs() {
      const attrs = { tabindex: this.computedTabIndex };

      if (this.type !== 'a') {
        attrs.type = this.type || 'button';
      }

      if (this.hasRouterLink === true) {
        attrs.href = this.currentLocation.href;
        attrs.role = 'link';
      } else {
        attrs.role = this.type === 'a' ? 'link' : 'button';
      }

      if (this.loading === true && this.percentage !== void 0) {
        attrs.role = 'progressbar';
        attrs['aria-valuemin'] = 0;
        attrs['aria-valuemax'] = 100;
        attrs['aria-valuenow'] = this.percentage;
      }

      if (this.disable === true) {
        attrs.disabled = '';
        attrs['aria-disabled'] = 'true';
      }

      return attrs;
    },

    classes() {
      let colors;
      if (this.disable) {
        colors =
          this.flat || this.plain
            ? 'text-grey-3'
            : this.outline
            ? 'text-grey-3 bg-grey-0 q-btn--outline--disable'
            : this.dash || this.roundash || this.normal
            ? 'text-grey-3 bg-grey-0'
            : 'text-white bg-grey-2';
      } else if (this.color !== void 0) {
        if (
          this.outline === true ||
          this.roundash === true ||
          this.dash === true
        ) {
          colors = `text-${this.textColor || this.color} bg-white`;
        } else if (this.flat === true || this.plain === true) {
          colors =
            this.flatColorless === true
              ? 'text-black-0'
              : `text-${this.textColor || this.color}`;
        } else if (this.normal === true || this.isDropdown === true) {
          colors = 'text-black-0';
        } else if (this.menu === true) {
          colors = 'text-white';
        } else {
          colors = `bg-${this.color} text-${this.textColor || 'white'}`;
        }
      } else if (this.textColor) {
        colors = `text-${this.textColor}`;
      }

      return (
        `q-btn--${this.design} ` +
        `q-btn--${
          this.round === true
            ? 'round'
            : `rectangle${this.isRounded === true ? ' q-btn--rounded' : ''}`
        }` +
        (colors !== void 0 ? ' ' + colors : '') +
        (this.isActionable === true
          ? this.plain && this.flatColorless
            ? ' q-btn--actionable'
            : ' q-btn--actionable q-focusable'
          : this.disable === true
          ? ' disabled'
          : '') +
        (this.fab === true
          ? ' q-btn--fab'
          : this.fabMini === true
          ? ' q-btn--fab-mini'
          : '') +
        (this.noCaps === true ? ' q-btn--no-uppercase' : '') +
        (this.noWrap === true ? '' : ' q-btn--wrap') + // this is for IE11
        (this.dense === true ? ' q-btn--dense' : '') +
        (this.stretch === true ? ' no-border-radius self-stretch' : '') +
        (this.glossy === true ? ' glossy' : '') +
        (!this.disable &&
        (this.outline ||
          this.roundash ||
          this.dash ||
          (this.flat && !this.flatColorless) ||
          this.plain)
          ? ' q-btn--hoverClass'
          : '') +
        (!this.disable && this.design === 'standard'
          ? ' q-btn--standard-hoverClass'
          : '') +
        (!this.disable && this.normal ? ' q-btn--normal-hoverClass' : '') +
        (!this.disable && this.isDropdown ? ' q-btn--dropdown-hover' : '') +
        (this.dialog === true ? ' q-btn--dialog-width' : '')
      );
    },

    innerClasses() {
      return (
        this.alignClass +
        (this.stack === true ? ' column' : ' row') +
        (this.noWrap === true ? ' no-wrap text-no-wrap' : '') +
        (this.loading === true ? ' q-btn__content--hidden' : '')
      );
    },

    wrapperStyle() {
      if (this.padding !== void 0) {
        return {
          padding: this.padding
            .split(/\s+/)
            .map(v => (v in padding ? padding[v] + 'px' : v))
            .join(' '),
          minWidth: '0',
          minHeight: '0'
        };
      }
    }
  }
};
