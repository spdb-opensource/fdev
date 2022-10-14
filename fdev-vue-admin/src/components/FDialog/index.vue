<template>
  <fdev-dialog
    persistent
    v-model="value"
    :position="right ? 'right' : 'standard'"
    :full-height="right"
    @show="onShow"
    @hide="onHide"
    @before-show="beforeShow"
    @before-hide="beforeHide"
  >
    <div v-if="right" class="row no-wrap">
      <transition
        name="custom-classes-transition"
        enter-active-class="animate__animated animate__slideInRight"
        leave-active-class="animate__animated animate__slideOutRight"
      >
        <div class="column left-dia relative-position" v-if="leftDia">
          <div
            class="col column no-wrap bg-white items-center left-dialog-content"
          >
            <div
              class="drawer-title-line full-width text-black-0 text-subtitle3 text-thick q-px-llg row justify-between items-center"
              v-if="!hasLeftDiaTitleSlot"
            >
              <span>{{ childTitle }}</span
              ><f-icon
                name="close"
                class="cursor-pointer text-grey-3"
                @click="closeLeftDia"
                :width="24"
                :height="24"
              />
            </div>
            <slot name="leftDiaTitleSlot" />
            <fdev-separator class="full-width" />
            <f-scrollarea
              class="full-width q-mb-md content"
              :style="{ 'margin-top': !dense ? '32px' : '22px' }"
              @change-scroll="leftChangeScoll"
            >
              <slot name="leftDiaContent" />
            </f-scrollarea>
          </div>
          <div
            class="column bg-white full-width"
            :class="{
              'btn-slot': hasleftDiaBtnSlot,
              'scroll-bottom': leftInnerHeigherThanOuter
            }"
          >
            <div v-if="leftTip" class="text-negative text-right tip">
              {{ leftTip }}
            </div>
            <div
              v-if="hasleftDiaBtnSlot"
              class="row items-center justify-end full-width q-gutter-x-md"
              style="margin-top:20px"
            >
              <slot name="leftDiaBtnSlot" />
            </div>
          </div>
        </div>
      </transition>
      <div
        class="column dialog-right relative-position"
        :class="[fSlDc || fDc ? 'dia-dc-w' : 'dialog-s']"
      >
        <div class="col column no-wrap bg-white items-center dialog-content">
          <div
            class="drawer-title-line full-width text-black-0 text-subtitle3 text-thick q-px-llg row justify-between items-center"
            v-if="!hasTitleSlot"
          >
            <span>{{ title }}</span
            ><f-icon
              name="close"
              class="cursor-pointer text-grey-3"
              @click="closeDialog"
              :width="24"
              :height="24"
            />
          </div>
          <slot name="titleSlot" />
          <fdev-separator class="full-width" />
          <f-scrollarea
            class="full-width q-mb-md content"
            :style="{ 'margin-top': !dense ? '32px' : '22px' }"
            @change-scroll="changeScoll"
          >
            <slot />
          </f-scrollarea>
        </div>
        <div
          class="column bg-white full-width"
          :class="{
            'btn-slot': hasBtn,
            'scroll-bottom': innerHeigherThanOuter
          }"
        >
          <div v-if="tip" class="text-negative text-right tip">
            {{ tip }}
          </div>
          <div
            v-if="hasBtn"
            class="row items-center justify-end full-width q-gutter-x-md"
            style="margin-top:20px"
          >
            <slot name="btnSlot" />
          </div>
        </div>
      </div>
    </div>
    <div
      v-else
      class="column no-wrap bg-white dialog-standard"
      :class="[fSlDc || fDc ? 'dia-dc-w' : 'dialog-s']"
    >
      <div
        class="title-line full-width text-black-0 text-subtitle3 text-thick q-px-llg row justify-between items-center"
      >
        <span>{{ title }}</span
        ><f-icon
          name="close"
          class="cursor-pointer text-grey-3"
          @click="closeDialog"
          :width="24"
          :height="24"
        />
      </div>
      <slot name="titleSlot" />
      <fdev-separator class="full-width" />
      <div
        class="full-width content-standard"
        :class="[
          {
            'justify-start column': fSlSc,
            'q-gutter-y-diaLine column': fSc,
            'q-gutter-y-diaLine justify-between row': fDc,
            'items-end justify-between row': fSlDc
          }
        ]"
      >
        <slot />
      </div>
      <div
        class="row q-gutter-md items-center justify-end full-width"
        :class="{ 'btn-slot': hasBtn }"
      >
        <slot name="btnSlot" />
      </div>
    </div>
  </fdev-dialog>
</template>

<script>
export default {
  name: 'FDialog',
  data() {
    return {
      hasBtn: false,
      hasleftDiaBtnSlot: false,
      innerHeigherThanOuter: false,
      leftInnerHeigherThanOuter: false
    };
  },
  props: {
    value: Boolean,
    title: String,
    childTitle: String,
    right: { type: Boolean, default: false },
    leftDia: Boolean,
    'f-sl-sc': Boolean, //单列单行表单
    'f-sc': Boolean, //单列表单
    'f-sl-dc': Boolean, //单行双列表单
    'f-dc': Boolean, //双列表单
    dense: Boolean,
    leftDense: Boolean,
    tip: String,
    leftTip: String
  },
  computed: {
    hasTitleSlot() {
      return this.$slots['titleSlot'] !== void 0;
    },
    hasLeftDiaTitleSlot() {
      return this.$slots['leftDiaTitleSlot'] !== void 0;
    }
  },
  mounted() {
    this.hasBtn = this.$slots.btnSlot !== undefined;
    this.hasleftDiaBtnSlot = this.$slots.leftDiaBtnSlot !== undefined;
  },
  methods: {
    closeDialog() {
      this.$emit('before-close');
      this.$emit('input', false);
    },
    closeLeftDia() {
      this.$emit('closeLeftDia');
    },
    onShow() {
      this.$emit('show');
    },
    onHide() {
      this.$emit('hide');
    },
    beforeShow() {
      this.$emit('before-show');
    },
    beforeHide() {
      this.innerHeigherThanOuter = false;
      this.leftInnerHeigherThanOuter = false;
      this.$emit('before-hide');
    },
    changeScoll(status) {
      this.innerHeigherThanOuter = status;
    },
    leftChangeScoll(status) {
      this.leftInnerHeigherThanOuter = status;
    }
  }
};
</script>

<style lang="stylus" scoped>
.dialog-s
  min-width 500px
.dialog-standard
  border-radius 6px
.dialog-right
.left-dia
  box-shadow: 0 2px 10px 5px rgba(30,84,213,0.30)
.left-dia
  min-width 400px
.title-line
  min-height 64px
.drawer-title-line
  min-height 56px
.content
  padding-left 32px
  padding-right 22px
.content-standard
  padding 32px 32px 28px
  min-height 152px
.btn-slot
  padding 28px 32px 28px 0
.tip
  margin-top 20px
  font-size 12px
  line-height 12px
.scroll-bottom
  box-shadow 0 2px 10px 0 rgba(51, 51, 51, 0.25)
.dialog-content, .left-dialog-content
  >>>
    .scroll-thin-y::-webkit-scrollbar
      width 10px
    .scroll-thin-y::-webkit-scrollbar-thumb
      background-color rgba(51,51,51,0.25)
      border-right 2px solid transparent
      background-clip content-box
      border-radius 20px
      &:active
        background-color rgba(51, 51, 51, 0.65)
</style>
