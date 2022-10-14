import { QSelect } from '../select';
import { QInput } from '../input';
import QBtn from '../btn/QBtn.js';
import FImage from '@/components/FImage';

import cache from '../../utils/cache.js';

const staticClass = 'q-table__bottom row items-center';

export default {
  props: {
    hideBottom: Boolean,
    hideSelectedBanner: Boolean,
    hideNoData: Boolean,
    hidePagination: Boolean
  },

  data() {
    return {
      toPage: ''
    };
  },

  computed: {
    navIcon() {
      const ico = [
        this.iconFirstPage || this.$q.iconSet.table.firstPage,
        this.iconPrevPage || this.$q.iconSet.table.prevPage,
        this.iconNextPage || this.$q.iconSet.table.nextPage,
        this.iconLastPage || this.$q.iconSet.table.lastPage
      ];
      return this.$q.lang.rtl === true ? ico.reverse() : ico;
    }
  },

  watch: {
    'computedPagination.page': {
      handler(v) {
        const totalPages = Math.ceil(
          this.computedRowsNumber / this.computedPagination.rowsPerPage
        );
        if (v > 0) {
          v < totalPages && (this.toPage = v + 1);
          v === totalPages && (this.toPage = v);
        }
      },
      immediate: true
    }
  },

  methods: {
    __getBottomDiv(h) {
      if (this.hideBottom === true) {
        return;
      }

      if (this.nothingToDisplay === true) {
        if (this.hideNoData === true) {
          return;
        }

        const message =
          this.loading === true
            ? this.loadingLabel || this.$q.lang.table.loading
            : this.filter
            ? this.noResultsLabel || this.$q.lang.table.noResults
            : this.noDataLabel || this.$q.lang.table.noData;

        const noData = this.$scopedSlots['no-data'];
        const children =
          noData !== void 0
            ? [
                noData({
                  message,
                  icon: this.$q.iconSet.table.warning,
                  filter: this.filter
                })
              ]
            : [
                h(
                  'div',
                  {
                    staticClass: 'full-width column items-center q-pt-llg',
                    style: { 'padding-bottom': '19px' }
                  },
                  [
                    h(FImage, {
                      staticClass: 'q-table__bottom justify-center',
                      props: { name: 'no_data_1' }
                    }),
                    h(
                      'div',
                      {
                        staticClass: 'self-center q-mt-xs',
                        style: {
                          color: '#999',
                          height: '28px',
                          'line-height': '28px'
                        }
                      },
                      '没有可用数据'
                    )
                  ]
                )
              ];

        return h(
          'div',
          {
            staticClass: staticClass + ' q-table__bottom--nodata'
          },
          children
        );
      }

      const { bottom } = this.$scopedSlots;

      if (bottom !== void 0) {
        return h('div', { staticClass }, [bottom(this.marginalsScope)]);
      }

      const child =
        this.hideSelectedBanner !== true &&
        this.hasSelectionMode === true &&
        this.rowsSelectedNumber > 0
          ? [
              h('div', { staticClass: 'q-table__control' }, [
                h('div', [
                  (this.selectedRowsLabel ||
                    this.$q.lang.table.selectedRecords)(this.rowsSelectedNumber)
                ])
              ])
            ]
          : [];

      if (this.hidePagination !== true) {
        return h(
          'div',
          {
            staticClass: staticClass + ' justify-end'
          },
          this.__getPaginationDiv(h, child)
        );
      }

      if (child.length > 0) {
        return h('div', { staticClass }, child);
      }
    },

    __getPaginationDiv(h, child) {
      let control;
      const { rowsPerPage } = this.computedPagination,
        c = this.computedPagination.page,
        max = Math.ceil(this.computedRowsNumber / rowsPerPage),
        paginationLabel = this.paginationLabel || this.$q.lang.table.pagination,
        paginationSlot = this.$scopedSlots.pagination,
        hasOpts = this.rowsPerPageOptions.length > 1;

      child.push(h('div', { staticClass: 'q-table__separator col' }));

      let opts = [];
      if (hasOpts === true) {
        opts.push(
          h(
            'div',
            {
              staticClass: 'q-table__control',
              style: { 'margin-right': '12px' }
            },
            [
              h(QSelect, {
                staticClass: 'q-table__select inline q-table__bottom-item',
                props: {
                  value: rowsPerPage,
                  flat: true,
                  outlined: true,
                  options: this.computedRowsPerPageOptions,
                  optionLabel: item => {
                    if (item.value === 0) {
                      return '全部';
                    }
                    return item.value + '行/页';
                  },
                  displayValue:
                    rowsPerPage === 0
                      ? this.$q.lang.table.allRows
                      : rowsPerPage,
                  optionsDense: true,
                  optionsCover: true,
                  tableBottomSelect: true
                },
                on: cache(this, 'pgSize', {
                  input: pag => {
                    this.setPagination({
                      page: 1,
                      rowsPerPage: pag.value
                    });
                  }
                })
              })
            ]
          )
        );
      }

      if (paginationSlot !== void 0) {
        control = paginationSlot(this.marginalsScope);
        child.push(h('div', { staticClass: 'q-table__control' }, control));
        child.push(...opts);
      } else {
        // this.computedPagination // 内含 page 和 rowsPerPage
        // this.computedRows // 每页对象数组
        // this.computedRowsNumber // 总数量
        control = [];
        if (rowsPerPage !== 0 && this.pagesNumber > 1) {
          const btnProps = {
            // dense: true
          };
          this.pagesNumber > 2 &&
            control.push(
              h(QBtn, {
                key: 'pgFirst',
                staticClass: 'btn-default btn-navi',
                props: {
                  ...btnProps,
                  icon: this.navIcon[0],
                  disable: this.isFirstPage
                },
                on: cache(this, 'pgFirst', { click: this.firstPage })
              })
            );

          control.push(
            h(QBtn, {
              key: 'pgPrev',
              staticClass: 'btn-default btn-navi',
              props: {
                ...btnProps,
                icon: this.navIcon[1],
                disable: this.isFirstPage
              },
              on: cache(this, 'pgPrev', { click: this.prevPage })
            })
          );

          const arr = [c];
          if (c === 2) {
            arr.unshift(1);
          } else if (c > 2) {
            arr.unshift(c - 2, c - 1);
          }
          if (c === max - 1) {
            arr.push(max);
          } else if (c < max - 1) {
            arr.push(c + 1, c + 2);
          }

          for (let i = 0; i < arr.length; i++) {
            control.push(
              h(QBtn, {
                key: `pg${arr[i]}`,
                props: {
                  label: `${arr[i]}`
                },
                staticClass:
                  'btn-default' +
                  ` ${
                    this.computedPagination.page === arr[i] ? 'current-' : ''
                  }num-btn`,
                on: cache(this, `pg${arr[i]}`, {
                  click: () => this.setPagination({ page: arr[i] })
                })
              })
            );
          }

          control.push(
            h(QBtn, {
              key: 'pgNext',
              staticClass: 'btn-default btn-navi',
              props: {
                ...btnProps,
                icon: this.navIcon[2],
                disable: this.isLastPage
              },
              on: cache(this, 'pgNext', { click: this.nextPage })
            })
          );

          this.pagesNumber > 2 &&
            control.push(
              h(QBtn, {
                key: 'pgLast',
                staticClass: 'btn-default btn-navi',
                props: {
                  ...btnProps,
                  icon: this.navIcon[3],
                  disable: this.isLastPage
                },
                on: cache(this, 'pgLast', { click: this.lastPage })
              })
            );
        }

        child.push(
          h('div', { staticClass: 'q-table__control q-gutter-x-xs q-mr-md' }, control)
        );

        child.push(...opts);

        // 跳转到 XXX 页
        if (
          this.computedPagination.rowsPerPage !== 0 &&
          Math.ceil(
            this.computedRowsNumber / this.computedPagination.rowsPerPage
          ) > 1
        ) {
          child.push(
            h('div', { staticClass: 'q-table__control', style: { 'margin-right': '12px' } }, [
              h('span', { staticClass: 'q-table__bottom-item-font' }, ['前往']),
              h(QInput, {
                staticClass: 'q-table__input inline q-mx-sm',
                props: {
                  value: this.toPage
                },
                attrs: {
                  placeholder: ''
                },
                ref: 'table-bottom-input',
                on: cache(this, 'toPage', {
                  input: value => {
                    this.toPage = value;
                  },
                  keyup: e => {
                    if (e.keyCode !== 13) {
                      return;
                    }
                    this.changePage(e.target);
                  }
                })
              }),
              h('span', { staticClass: 'q-table__bottom-item-font' }, ['页'])
            ])
          );
        }

        const label = rowsPerPage
          ? paginationLabel(
              this.firstRowIndex + 1,
              Math.min(this.lastRowIndex, this.computedRowsNumber),
              this.computedRowsNumber
            )
          : paginationLabel(
              1,
              this.filteredSortedRowsNumber,
              this.computedRowsNumber
            );

        const [span1, span2] = label.split('/');

        child.push(
          h(
            'div',
            rowsPerPage !== 0
              ? { staticClass: 'q-table__bottom-item-font2' }
              : {},
            [h('span', [span1]), h('span', [' / ' + span2])]
          )
        );
      }

      return child;
    },

    changePage(ele) {
      const toPage = Math.floor(this.toPage),
        totalPages = Math.ceil(
          this.computedRowsNumber / this.computedPagination.rowsPerPage
        );
      if (
        toPage < 1 ||
        toPage > totalPages ||
        toPage === this.computedPagination.page
      ) {
        ele.select();
        return;
      }
      this.setPagination({ page: toPage });
      ele.blur();
      if (toPage < totalPages) {
        this.$nextTick(() => {
          this.toPage = toPage + 1;
        });
      }
    }
  }
};
