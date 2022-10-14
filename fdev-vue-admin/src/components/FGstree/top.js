import { QBtn } from '@/components/quasar/src/components/btn/index.js';

export default {
  props: {
    title: String,
    titleIcon: String
  },
  computed: {
    marginalsScope() {
      return {};
    }
  },
  methods: {
    __getTopDiv(h) {
      const { top, topLeft, topRight } = this.$scopedSlots,
        titleSlot = this.$scopedSlots['title'],
        titleIconSlot = this.$scopedSlots['titleIcon'],
        staticClass = 'fdev-gstree-top row items-center q-mb-lg';

      if (top !== void 0) {
        return h('div', { staticClass }, [top(this.marginalsScope)]);
      }

      const child = [],
        t = [];

      if (this.titleIcon) {
        t.push(
          h('fIcon', {
            props: { name: this.titleIcon, width: 16, height: 16 },
            staticClass: 'text-primary fdev-gstree__icon'
          })
        );
      } else if (titleIconSlot !== void 0) {
        t.push('div', {}, [titleIconSlot(this.marginalsScope)]);
      }

      if (titleSlot !== void 0) {
        t.push(
          h('div', { staticClass: 'fdev-gstree__control' }, [
            titleSlot(this.marginalsScope)
          ])
        );
      } else if (this.title) {
        t.push(
          h('div', { staticClass: 'fdev-gstree__control' }, [
            h(
              'div',
              {
                staticClass: 'text-black-0 text-thick text-lh-lg text-md',
                class: this.titleClass
              },
              this.title
            )
          ])
        );
      }

      child.push(h('div', { staticClass: 'row no-wrap items-center' }, t));

      child.push(h('div', { staticClass: 'fdev-gstree-separator col' }));

      if (topLeft !== void 0) {
        child.push(
          h('div', { staticClass: 'fdev-gstree__control q-mr-llg' }, [
            topLeft(this.marginalsScope)
          ])
        );
      }

      child.push(
        h(QBtn, {
          props: { normal: true, ficon: 'refresh_c_o', label: '重置' },
          on: { click: this.reset }
        })
      );

      if (topRight !== void 0) {
        child.push(
          h('div', { staticClass: 'fdev-gstree__control q-ml-md' }, [
            topRight(this.marginalsScope)
          ])
        );
      }

      return h('div', { staticClass }, child);
    }
  }
};
