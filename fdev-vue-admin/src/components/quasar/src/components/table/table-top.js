import exportFile from '../../utils/export-file';
import Notify from '../../plugins/Notify';
import { QBtn } from '../btn/index';
import { QSelect } from '../select/index';
import fIcon from '../../../../FIcon';

export default {
  props: {
    title: String,
    titleIcon: String,
    noExport: Boolean, //不显示导出按钮
    exportFunc: Function, //导出的执行函数
    onSelectCols: Function, //选择列时触发函数
    onSearch: Function,
    noSelectCols: {
      type: Boolean,
      default: false
    },
    noTopBottom: Boolean
  },
  computed: {
    marginalsScope() {
      return {
        pagination: this.computedPagination,
        pagesNumber: this.pagesNumber,
        isFirstPage: this.isFirstPage,
        isLastPage: this.isLastPage,
        firstPage: this.firstPage,
        prevPage: this.prevPage,
        nextPage: this.nextPage,
        lastPage: this.lastPage,
        inFullscreen: this.inFullscreen,
        toggleFullscreen: this.toggleFullscreen
      };
    }
  },
  emits: ['update:visibleColumns'],
  methods: {
    wrapCsvValue(val, formatFn) {
      let formatted =
        formatFn !== void 0
          ? formatFn(val)
          : typeof val === 'object'
          ? JSON.stringify(val)
          : val;
      formatted =
        formatted === void 0 || formatted === null ? '' : String(formatted);
      formatted = formatted.split('"').join('""');
      return `"${formatted}"`;
    },

    exportTable() {
      // naive encoding to csv format
      const content = [this.columns.map(col => this.wrapCsvValue(col.label))]
        .concat(
          this.data.map(row =>
            this.columns
              .map(col =>
                this.wrapCsvValue(
                  typeof col.field === 'function'
                    ? col.field(row)
                    : row[col.field === void 0 ? col.name : col.field],
                  col.format
                )
              )
              .join(',')
          )
        )
        .join('\r\n');
      let title = this.title || '表格';
      const status = exportFile(`${title}.csv`, content, 'text/csv');

      if (status !== true) {
        Notify.create({
          message: 'Browser denied file download...',
          color: 'negative',
          icon: 'warning'
        });
      }
    },
    __getTopDiv(h) {
      const { top } = this.$scopedSlots,
        topLeft = this.$scopedSlots['top-left'],
        topRight = this.$scopedSlots['top-right'],
        topSelection = this.$scopedSlots['top-selection'],
        topBottom = this.$scopedSlots['top-bottom'],
        topBottomOpt = this.$scopedSlots['top-bottom-opt'],
        titleIcon = this.$scopedSlots['title-icon'],
        hasSelection =
          this.hasSelectionMode === true &&
          topSelection !== void 0 &&
          this.rowsSelectedNumber > 0,
        staticClass = `q-table__top relative-position row items-center${
          this.noTopBottom === true ? '' : ' q-mb-md'
        }`,
        rowClass = 'row full-width q-mb-md items-center',
        colClass = 'q-table__top q-mb-md relative-position column items-end';

      if (top !== void 0) {
        // 1 有 TopSLot
        return h('div', { staticClass }, [top(this.marginalsScope)]);
      }

      let child;

      if (hasSelection === true) {
        child = topSelection(this.marginalsScope).slice();
      } else {
        child = [];

        if (topLeft !== void 0) {
          child.push(
            h('div', { staticClass: 'q-table-control' }, [
              topLeft(this.marginalsScope)
            ])
          );
        } else if (this.title) {
          let title = [
            h('div', { staticClass: 'q-table__control' }, [
              h(
                'div',
                {
                  staticClass: 'text-black-0 text-thick',
                  class: this.titleClass,
                  style: { 'line-height': '36px', 'font-size': '16px' }
                },
                this.title
              )
            ])
          ];

          if (this.titleIcon) {
            title.unshift(
              h(fIcon, {
                props: {
                  name: this.titleIcon,
                  width: 16,
                  height: 16
                },
                staticClass: 'text-primary q-mr-sm q-table__title-icon'
              })
            );
          } else if (titleIcon !== void 0) {
            title.unshift(
              h(
                'div',
                {
                  style: { width: '32px' },
                  staticClass: 'q-table__title-icon'
                },
                [titleIcon(this.marginalsScope)]
              )
            );
          }
          child.push(
            h('div', { staticClass: 'no-wrap row items-center' }, title)
          );
        }
      }
      if (
        !(
          topRight === void 0 &&
          this.noExport &&
          this.noSelectCols &&
          !this.onSearch
        )
      ) {
        child.push(h('div', { staticClass: 'q-table__separator col' }));
      }

      if (topRight !== void 0) {
        child.push(
          h('div', { staticClass: 'q-table__control row q-gutter-x-md' }, [
            topRight(this.marginalsScope)
          ])
        );
      }
      let thisTable = this;

      if (!this.noSelectCols) {
        child.push(
          h(QSelect, {
            staticClass: 'q-ml-md',
            props: {
              value: this.visibleCols,
              multiple: true,
              displayValue: '选择列',
              emitValue: true,
              mapOptions: true,
              options: this.colList.filter(col => !col.required),
              optionValue: 'name',
              optionsCover: true,
              flat: true
            },
            on: {
              input: function(val) {
                thisTable.onSelectCols
                  ? thisTable.onSelectCols(val)
                  : (thisTable.visibleCols = val);
              }
            }
          })
        );
      }

      if (this.onSearch !== void 0) {
        child.push(
          h(QBtn, {
            staticClass: 'q-ml-md',
            props: {
              label: '查询',
              dialog: true,
              normal: true,
              ficon: 'search'
            },
            on: {
              click: this.onSearch
            }
          })
        );
      }
      if (!this.noExport) {
        child.push(
          h(QBtn, {
            staticClass: 'q-ml-md',
            props: { label: '导出', dialog: true, normal: true, ficon: 'exit' },
            on: {
              click: this.exportFunc || this.exportTable
            }
          })
        );
      }
      if (topBottom !== void 0 || topBottomOpt !== void 0) {
        let tb =
          topBottom !== void 0
            ? h(
                'div',
                {
                  staticClass: 'row full-width'
                },
                [
                  h(
                    'div',
                    {
                      staticClass: 'row q-gutter-y-sm justify-start full-width'
                    },
                    [topBottom(this.marginalsScope)]
                  )
                ]
              )
            : null;
        let tbo =
          topBottomOpt !== void 0
            ? ('div',
              {
                staticClass: 'full-width row'
              },
              [topBottomOpt(this.marginalsScope)])
            : null;
        // 2 没有 TopSLot，有TopBottomSlot
        return h('div', { staticClass: colClass }, [
          h('div', { staticClass: rowClass }, child),
          tb,
          tbo
        ]);
      } else if (child.length === 0) {
        // 3 啥都没有
        return;
        // 4 top-left（title、titleIcon）、top-right（功能按钮、选择列、查询、导出）
      } else {
        return h('div', { staticClass }, child)
      }
    }
  }
}; 
