<template>
  <div :id="id" :class="className" :style="{ height: height, width: width }" />
</template>

<script>
import echarts from 'echarts/lib/echarts';
import 'echarts/lib/chart/bar';
import 'echarts/lib/component/legend';
import 'echarts/lib/component/title';
import 'echarts/lib/component/grid';
import 'echarts/lib/component/tooltip';
require('echarts/theme/macarons');
import { mapState } from 'vuex';
export default {
  name: 'Chart',
  data() {
    return {
      chart: null
    };
  },
  props: {
    name: String,
    className: {
      type: String,
      default: 'chart'
    },
    id: {
      type: String,
      default: 'chart'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '550px'
    },
    title: {
      type: String
    }
  },
  watch: {
    chartData(val) {
      if (val) {
        this.draw();
        this.chartResize();
      }
    },
    isMini(val) {
      this.chartResize();
    }
  },
  computed: {
    ...mapState('interfaceForm', ['serviceLink']),
    ...mapState('dashboard', {
      isMini: 'isMini'
    })
  },
  methods: {
    draw() {
      this.chart = echarts.init(document.getElementById(this.id), 'macarons');

      const data = {
        name: this.name,
        children: this.handleChildren(this.name)
      };

      this.chart.clear();
      this.chart.resize();

      const option = {
        tooltip: {
          trigger: 'item',
          position: (pos, params, dom, rect, size) => {
            var obj = {};
            const x = ['left', 'right'][+(pos[0] < size.viewSize[0] / 2)];
            const y = ['top', 'bottom'][+(pos[1] < size.viewSize[1] / 2)];
            if (x === 'left') {
              obj[x] = pos[0] - size.contentSize[0];
            } else {
              obj['left'] = pos[0];
            }

            if (y === 'top') {
              obj[y] = pos[1] - size.contentSize[1];
            } else {
              obj['top'] =
                size.viewSize[1] - size.contentSize[1] - pos[1] > 0
                  ? pos[1]
                  : pos[1] - size.contentSize[1] / 2;
            }
            return obj;
          },
          textStyle: {
            fontSize: 12
          },
          axisPointer: {
            type: 'cross'
          }
        },
        series: [
          {
            type: 'tree',
            name: 'tree1',
            data: [data],
            top: '5%',
            bottom: '2%',
            right: '25%',
            symbolSize: 7,

            label: {
              position: 'left',
              verticalAlign: 'middle',
              align: 'right'
            },

            leaves: {
              label: {
                position: 'right',
                verticalAlign: 'middle',
                align: 'left'
              }
            },

            expandAndCollapse: true,

            animationDuration: 550,
            animationDurationUpdate: 750
          }
        ]
      };

      const tooltip = {
        formatter: params => {
          if (params.data && params.data.relations) {
            const servicesList = params.data.relations;
            if (servicesList.length >= 20) {
              let services1 = '';
              let services2 = '';
              servicesList.forEach((item, i) => {
                if (i % 2 === 0) {
                  services1 += item.trans_id + '<br/>';
                } else {
                  services2 += item.trans_id + '<br/>';
                }
              });
              return `<div style="display: flex;"><div style="flex: 1; margin-right: 5px;">${services1}</div><div style="flex: 1;">${services2}</div></div>`;
            }
            return servicesList
              .map(item => {
                return item.trans_id;
              })
              .join('<br/>');
          }
        }
      };
      this.chart.setOption(Object.assign(option, tooltip), true);
    },
    chartResize() {
      if (!this.chart) {
        return;
      }
      const timer = setInterval(() => {
        this.chart.resize();
        clearInterval(timer);
      }, 100);
    },
    handleChildren(name) {
      if (this.serviceLink[name]) {
        return this.serviceLink[name].map(item => {
          return {
            ...item,
            name: item.trans_id || item.serviceId,
            value: 0,
            children: this.handleChildren(item.trans_id || item.serviceId)
          };
        });
      }
      return null;
    }
  }
};
</script>
