<template>
  <fdev-input
    @click="bindEvent"
    ref="date-input"
    :placeholder="range ? '请选择日期区间' : '请选择日期'"
    v-model="inputDate"
    :mask="range ? 'dateRenge' : 'date'"
    :disable="disable"
    :class="{ 'no-pointer-events': disable }"
    :rules="rules"
    readonly
    class="good-time"
    :hint="hint"
  >
    <fdev-popup-proxy
      ref="date"
      transition-show="scale"
      transition-hide="scale"
      @hide="hide"
      no-padding
    >
      <fdev-date
        :value="value"
        :mask="mask"
        @input="selectDate"
        minimal
        :range="range"
        :default-year-month="defaultYearMonth"
        :options="options"
      >
        <div v-if="range" class="full-width row justify-end">
          <fdev-btn flat v-close-popup label="关闭" />
        </div>
        <div class="date-tips" v-if="isDisable">
          {{ disableTips }}
        </div>
      </fdev-date>
    </fdev-popup-proxy>
    <template v-slot:append>
      <f-icon
        :name="appendIcon"
        :width="16"
        :height="16"
        class="cursor-pointer"
        @click="clearDate"
      />
    </template>
  </fdev-input>
</template>

<script>
import ValidateMixin from '../quasar/src/mixins/validate';
let getCurrentDate = () => {
  let current = new Date();
  let Y = current.getFullYear();
  let M = current.getMonth() + 1;
  M = M >= 10 ? M : '0' + M;
  return `${Y}/${M}`;
};
export default {
  name: 'FDate',
  mixins: [ValidateMixin],
  props: {
    disableTips: String,
    value: { default: false },
    range: Boolean,
    options: [Array, Function],
    disable: Boolean,
    mask: {
      type: String,
      default: 'YYYY-MM-DD',
      validator: value => ['YYYY-MM-DD', 'YYYY/MM/DD'].includes(value)
    },
    rules: {
      type: Array,
      default: () => []
    },
    defaultYearMonth: {
      type: String,
      default: getCurrentDate()
    },
    hint: String
  },
  data() {
    return {
      isDisable: false,
      inputDate: ''
    };
  },
  computed: {
    appendIcon() {
      return !this.inputDate ? 'calendar' : 'close';
    }
  },
  methods: {
    selectDate(val) {
      !this.range && this.$refs.date.hide();
      if (this.range && typeof val !== 'object') {
        this.$emit('input', null);
        return;
      }
      this.$emit('input', val);
    },
    bindEvent() {
      setTimeout(() => {
        // 拿到日期选择器元素 使用setTimeout是因为自带的动画在0.几秒后才将元素渲染出来
        let el = document.querySelector('.q-date--portrait-minimal');
        if (el) {
          // 给日期选择器绑定mouseover事件，判断当鼠标移入带有q-date__calendar-item--out也就是无法选择日期时，展示tip
          el.addEventListener('mouseover', $event => {
            if (
              $event.fromElement &&
              $event.fromElement.className.indexOf(
                'q-date__calendar-item--out'
              ) !== -1
            ) {
              this.disableTips && (this.isDisable = true);
            } else {
              this.isDisable = false;
            }

            if (
              $event.fromElement &&
              $event.fromElement.className.indexOf(
                'q-date__calendar-item--in'
              ) !== -1
            ) {
              this.isDisable = false;
            }
          });
        }
      }, 500);
    },
    hide() {
      if (!this.range && this.value === '') {
        this.$refs['date-input'].validate();
        return;
      }
      if (this.range) {
        this.$refs['date-input'].validate();
      }
    },
    clearDate(e) {
      if (this.appendIcon !== 'close') return;
      e.cancelable !== false && e.preventDefault();
      e.stopPropagation();
      this.$emit('input', null);
    }
  },
  watch: {
    value: {
      immediate: true,
      handler(val) {
        if (this.range) {
          if (
            this.inputDate === '' &&
            !!val &&
            val.from === '' &&
            val.to === ''
          ) {
            this.inputDate = null;
            return;
          }
          if (typeof val === 'object' && val) {
            this.inputDate =
              this.mask === 'YYYY/MM/DD'
                ? val.from.replace(/\//g, '-') +
                  '~' +
                  val.to.replace(/\//g, '-')
                : val.from + '~' + val.to;
          } else {
            this.inputDate = null;
          }
          return;
        }
        this.inputDate =
          this.mask === 'YYYY/MM/DD' && val ? val.replace(/\//g, '-') : val;
      }
    }
  }
};
</script>

<style scoped lang="stylus">
.q-date--portrait-minimal {
  position: relative;
  box-sizing: border-box;
}
>>> .q-date__actions {
  padding-top: 0;
  padding-bottom: 0;
}
/deep/ .q-date__actions .date-tips {
  top: 0;
  position: absolute;
  background: black;
  opacity: 0.8;
  color: #fff;
  width: 100%;
  left: 0;
  padding: 16px 0 16px 22px;
}
</style>

<style lang="stylus">
.good-time, .q-field--readonly .q-field__inner
  background-color #fff
</style>
