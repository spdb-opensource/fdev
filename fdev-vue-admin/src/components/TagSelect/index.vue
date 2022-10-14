<template>
  <div
    class="tagSelect row"
    :class="`${expandable ? 'hasExpandTag' : ''} ${expand ? 'expanded' : ''} `"
    :style="
      `font-size: ${sizes[size]}px;
             ${expandable ? (expand ? '' : `maxHeight: ${maxHeight}`) : ''}`
    "
  >
    <div class="label" :style="{ width: labelWidth || '' }" v-if="label">
      {{ label }}：
    </div>
    <transition-group tag="div" class="col row q-gutter-x-sm q-gutter-y-xs">
      <fdev-btn
        key="total"
        v-if="wrapTotal"
        class="q-px-sm tag-label"
        @click="setTotal"
        :disable="disable"
        :color="isTotal ? toggleColor : color"
        :textColor="isTotal ? toggleTextColor : textColor"
        :noCaps="noCaps"
        :noWrap="noWrap"
        :outline="outline"
        :flat="flat"
        :rounded="rounded"
        :size="size"
        :unelevated="unelevated"
        >全部</fdev-btn
      >
      <template v-for="(opt, i) in formattedOptions">
        <fdev-btn
          :key="opt.value"
          class="q-px-sm tag-label"
          @click="set(opt.value, opt)"
          :disable="disable"
          :label="opt.label"
          :color="val[i] ? opt.toggleColor || toggleColor : opt.color || color"
          :textColor="
            val[i]
              ? opt.toggleTextColor || toggleTextColor
              : opt.textColor || textColor
          "
          :icon="opt.icon"
          :iconRight="opt.iconRight"
          :noCaps="noCaps || opt.noCaps"
          :noWrap="noWrap || opt.noWrap"
          :outline="outline"
          :flat="flat"
          :rounded="rounded"
          :size="size"
          :noRipple="noRipple || opt.noRipple"
          :waitForRipple="waitForRipple || opt.waitForRipple"
          :tabindex="opt.tabindex"
          :unelevated="unelevated || opt.unelevated"
          v-if="!!$slots.option"
        />
        <slot
          name="option"
          :opt="opt"
          :selected="val[i]"
          :item-events="itemEvents[i]"
          v-else
        />
      </template>
    </transition-group>

    <a
      class="trigger"
      :class="color ? `text-${color}` : ''"
      v-if="expandable"
      @click="handleExpand"
    >
      <f-image
        name="mdi-set mdi-chevron-right"
        :class="expand ? 'rotate-90' : ''"
      />
    </a>
  </div>
</template>
<script>
export default {
  name: 'TagSelect',
  data() {
    return {
      isTotal: false,
      expand: false,
      sizes: {
        xs: 8,
        sm: 10,
        md: 14,
        lg: 20,
        xl: 24
      }
    };
  },
  props: {
    value: {
      type: Array,
      required: true
    },
    color: String,
    textColor: String,
    toggleColor: {
      type: String,
      default: 'primary'
    },
    toggleTextColor: String,
    options: {
      type: Array,
      required: true
    },
    readonly: Boolean,
    disable: Boolean,
    noCaps: Boolean,
    noWrap: Boolean,
    outline: Boolean,
    flat: Boolean,
    rounded: Boolean,
    size: String,
    noRipple: Boolean,
    waitForRipple: Boolean,
    wrapTotal: Boolean,
    label: String,
    unelevated: Boolean,
    labelWidth: String,
    expandable: {
      type: Boolean,
      default: false
    },
    emitOpt: {
      type: Boolean,
      default: false
    },
    sort: {
      type: Boolean,
      default: false
    },
    maxHeight: {
      type: String,
      default: ''
    }
  },
  computed: {
    val() {
      return this.formattedOptions.map(opt =>
        this.value.some(
          each => (this.emitOpt ? each.value : each) === opt.value
        )
      );
    },
    itemEvents() {
      return this.formattedOptions.map(opt => ({
        click: () => {
          this.set(opt.value, opt);
        }
      }));
    },
    formattedOptions() {
      if (this.sort) {
        return this.options.slice(0).sort((pre, next) => {
          const preSelected = this.value.some(
            each => (this.emitOpt ? each.value : each) === pre.value
          );
          const nextSelected = this.value.some(
            each => (this.emitOpt ? each.value : each) === next.value
          );
          if (preSelected) {
            if (nextSelected) {
              return 0;
            }
            return -1;
          }
          if (nextSelected) {
            return 1;
          }
          return 0;
        });
      }
      return this.options.slice(0);
    }
  },
  methods: {
    set(value, opt) {
      if (this.readonly) {
        return;
      }
      if (
        !this.value.some((val, ind) => {
          if (this.emitOpt) {
            if (val.value === opt.value) {
              this.isTotal = false;
              this.value.splice(ind, 1);
              return true;
            }
          }
          if (val === value) {
            this.isTotal = false;
            this.value.splice(ind, 1);
            return true;
          }
        })
      ) {
        if (this.emitOpt) {
          this.value.push(opt);
        } else {
          this.value.push(value);
        }
        if (this.value.length === this.formattedOptions.length) {
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
        this.value.push(
          ...(this.emitOpt
            ? this.formattedOptions
            : this.formattedOptions.map(opt => opt.value))
        );
      }

      this.$emit('input', this.value);
      this.$nextTick(() => {
        this.$emit('change', this.value);
      });
    },
    handleExpand() {
      this.expand = !this.expand;
    }
  }
};
</script>

<style lang="stylus" scoped>
.tagSelect
  user-select: none;
  position: relative;
  overflow: hidden;
  transition: all .3s;
  color: $grey-7;
  &.hasExpandTag
    max-height: 1.6em;
  &.expanded
    max-height: 50vh;
  .label
    font-size: 1em;
    color: $grey-7;
  .trigger
    position: absolute;
    top: 0;
    right: 0;
    i
      font-size: 1.6em;
      transition: .3s;
  &.hasExpandTag
    padding-right: 50px;
</style>
