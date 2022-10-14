import { QCheckbox } from '@/components/quasar/src/components/checkbox/index.js';
import { QSeparator } from '@/components/quasar/src/components/separator/index.js';
import FIcon from '@/components/FIcon/index.vue';
import { stop } from '@/components/quasar/src/utils/event.js';

let count = 0;
export default {
  methods: {
    __getBody(h) {
      const staticClass = 'fdev-gstree__body column';

      const child = [];

      this.showData.forEach((data, index) => {
        const items = [];

        data.forEach(node => {
          const item = [];
          item.push(
            h(QCheckbox, {
              props: {
                value: this.selectedGroups,
                label: node.label,
                val: node.id
              },
              on: {
                input: () => {
                  node.selected = !node.selected;
                  const ind = this.selectedGroups.indexOf(node.id);
                  ind === -1
                    ? this.selectedGroups.push(node.id)
                    : this.selectedGroups.splice(ind, 1);
                  this.$emit('select-group', this.selectedGroups);
                  this.$emit('click-group', this.treeData);
                }
              }
            })
          );

          if (node.children !== void 0) {
            item.push(
              h(FIcon, {
                staticClass: `q-ml-xs cursor-pointer${
                  node.open ? ' text-primary' : ''
                }`,
                props: {
                  name: `arrow_${node.open ? 'u' : 'd'}_f`,
                  width: 16,
                  height: 16
                },
                on: {
                  click: e => {
                    stop(e);
                    !node.open ? (node.sort = ++count) : delete node.sort;
                    node.open = !node.open;
                    this.$emit('click-group', this.treeData);
                  }
                }
              })
            );
          }
          items.push(
            h(
              'div',
              {
                staticClass: 'row items-center',
                style: { 'margin-bottom': '20px' }
              },
              item
            )
          );
        });

        if (index !== 0) {
          child.push(
            h(
              'div',
              {
                staticClass: 'row no-wrap items-center'
              },
              [
                h(
                  'div',
                  {
                    staticClass: 'self-start q-mr-md',
                    style: {
                      width: '16px',
                      height: '16px',
                      'margin-left': `${(index - 1) * 32}px`
                    }
                  },
                  [
                    h(FIcon, {
                      staticClass: 'text-primary vertical-top',
                      props: {
                        name: 'arrow_r_f',
                        width: 16,
                        height: 16
                      }
                    })
                  ]
                ),
                h('div', { staticClass: 'row q-gutter-x-llg' }, items)
              ]
            )
          );
        } else {
          child.push(
            h('div', { staticClass: 'row items-center q-gutter-x-llg' }, items)
          );
        }

        if (index !== this.showData.length - 1) {
          child.push(
            h(QSeparator, {
              props: { color: 'grey-1' },
              style: { 'margin-bottom': '20px' }
            })
          );
        }
      });

      return h('div', { staticClass }, child);
    }
  }
};
