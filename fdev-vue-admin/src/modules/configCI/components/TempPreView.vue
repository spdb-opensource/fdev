<template>
  <div
    class="rounded-borders column items-start"
    :class="page ? ['w995', 'temp-page'] : 'temp'"
  >
    <div
      class="head-style row items-center q-mb-lg card-width full-width"
      :class="page ? 'bg-indigogrey-0' : 'bg-primary'"
    >
      <img
        :src="page ? newBag : bag"
        class="cursor-pointer q-ml-lg q-mr-md"
        style="width:24px"
      />
      <span class="text-subtitle2" :class="page ? '' : 'text-white'">
        {{ CIData.name }}
      </span>
      <div class="row align-right items-center">
        <span
          class="text-subtitle2 q-mr-md"
          :class="page ? 'text-grey-7' : 'text-white'"
        >
          {{ CIData.fixedModeFlag | filterFlag }}模板
        </span>
        <div v-if="CIData.canEdit === '1' && isTool" class="row q-gutter-x-sm">
          <!-- 空模板id 603f1ca8e18171d090b1c012,空模板不准编辑 -->
          <fdev-btn
            flat
            ficon="edit"
            @click="editTemp"
            v-if="CIData.id !== '603f1ca8e18171d090b1c012'"
          >
            <fdev-tooltip>
              <span>编辑</span>
            </fdev-tooltip>
          </fdev-btn>
          <fdev-btn
            flat
            ficon="copy"
            @click="copyTemp"
            v-if="CIData.id !== '603f1ca8e18171d090b1c012'"
          >
            <fdev-tooltip>
              <span>复制</span>
            </fdev-tooltip>
          </fdev-btn>
          <fdev-btn
            flat
            ficon="delete_o"
            @click="deleteTemp"
            v-if="CIData.id !== '603f1ca8e18171d090b1c012'"
          >
            <fdev-tooltip>
              <span>删除</span>
            </fdev-tooltip>
          </fdev-btn>
        </div>
      </div>
    </div>
    <div class="full-width row">
      <div
        class="q-ml-lg col-10 "
        :class="isScroll ? 'scroll-thin-x-auto' : ''"
        ref="canvas"
      />
      <div class="col container">
        <fdev-select class="select-style" style="width: 130px; height:100px"
        v-if="CIData.canEdit === '1' && CIData.author.nameCn !== 'system' &&
        isTool" color: white :options="options" emit-value map-options
        v-model="CIData.visibleRange" @input="selectRange(CIData.visibleRange)"
        />
        <span class="text-body2">
          <span class="q-mr-sm"> {{ CIData.author.nameCn }} 提供 </span>
          <span>
            {{ CIData.updateTime }}
          </span>
        </span>
      </div>
    </div>
  </div>
</template>
<script>
const ratio = window.innerWidth / 1920;
const [nodeWidth, nodeHeight, spaceX, spaceY, fontSize, radius, lineWidth] = [
  180,
  50,
  90,
  16,
  14,
  24,
  1
].map(x => (Number.isInteger(x) ? x * ratio : x));
import { CI_COLORS, calcStrLen } from '../utils/constants';
import G6 from '@antv/g6';
import bag from '../assets/bag.svg';
import newBag from '../assets/new_bag.svg';
export default {
  name: 'tempPreView',
  data() {
    return {
      canvas: null,
      options: [
        { label: '全部可见', value: 'public' },
        { label: '个人可见', value: 'private' }
      ],
      bag,
      newBag,
      isScroll: false
    };
  },
  props: {
    CIData: Object,
    page: {
      type: Boolean,
      default: false
    }
  },
  filters: {
    filterFlag(val) {
      return val ? '固定' : '自由';
    }
  },
  computed: {
    isTool() {
      return this.$route.name === 'toolbox';
    },
    //模板预览
    canvasData() {
      let nodes = this.CIData.stages
        .map((stage, indS) => {
          return stage.jobNames.map((job, indJ) => {
            return {
              id: `${indS}-${indJ}`,
              label: job,
              x: (indS + 1 / 2) * nodeWidth + indS * spaceX + 1,
              y: (indJ + 1 / 2) * nodeHeight + indJ * spaceY + 1,
              size: [nodeWidth, nodeHeight],
              style: {
                lineWidth: 1,
                // stroke: CI_COLORS.process,
                stroke: CI_COLORS.running,
                radius: radius,
                fillOpacity: 0
              },
              labelCfg: {
                // style: { fontSize: fontSize, fill: CI_COLORS.process }
                style: { fontSize: fontSize, fill: CI_COLORS.running }
              },
              tooltipLabel: job
            };
          });
        })
        .flat();
      let stageInds = Array.from(this.CIData.stages.keys());
      stageInds.pop();
      nodes = nodes.concat(
        stageInds.map(indS => {
          return {
            id: `stage-${indS}`,
            x: (indS + 1) * nodeWidth + (indS + 1 / 2) * spaceX + 1,
            y: (1 / 2) * nodeHeight + 1,
            size: [1, 1],
            style: {
              lineWidth: 0,
              fill: CI_COLORS.line
            }
          };
        })
      );
      //文字超长处理
      nodes.forEach(node => {
        if (node.label) {
          return (node.label = this.fittingString(node.label, 160, 12));
        }
      });
      let edges = stageInds
        .map(indS => {
          let indSNext = indS + 1;
          return [
            Array.from(this.CIData.stages[indSNext].jobNames.keys()).map(
              indJ => {
                return {
                  source: `stage-${indS}`,
                  target: `${indSNext}-${indJ}`
                };
              }
            ),
            Array.from(this.CIData.stages[indS].jobNames.keys()).map(indJ => {
              return {
                source: `${indS}-${indJ}`,
                target: `stage-${indS}`
              };
            })
          ];
        })
        .flat()
        .flat();
      return { nodes, edges };
    },
    //画布尺寸
    canvasize() {
      let width = this.isTool ? 2124 * ratio : 824 * ratio;
      let height =
        Math.max(...this.CIData.stages.map(stage => stage.jobNames.length)) *
        (nodeHeight + spaceY);
      // eslint-disable-next-line vue/no-side-effects-in-computed-properties
      this.isScroll = this.CIData.stages.length > 4 ? true : false;
      return { width, height };
    }
  },
  methods: {
    deleteTemp() {
      return this.$q
        .dialog({
          title: '删除流水线模板',
          message: '请确认是否删除该流水线模板',
          ok: '删除',
          cancel: '再想想'
        })
        .onOk(async () => {
          await this.$emit('deleteTemp', this.CIData.id);
        });
    },

    editTemp() {
      this.$emit('editTemp', this.CIData.id);
    },
    copyTemp() {
      this.$emit('copyTemp', this.CIData.id);
    },
    selectRange(data) {
      this.$emit('selectRange', data, this.CIData.id);
    },
    // 计算显示的字符串
    fittingString(str, maxWidth, fontSize) {
      let fontWidth = fontSize * 1.3;
      maxWidth = maxWidth * 2;
      let width = calcStrLen(str) * fontWidth;
      let ellipsis = '...';
      if (width > maxWidth) {
        let actualLen = Math.floor((maxWidth - 200) / fontWidth);
        let result = str.substring(0, actualLen) + ellipsis;
        return result;
      }
      return str;
    }
  },
  mounted() {
    const tooltip = new G6.Tooltip({
      offsetX: 10,
      offsetY: 10,
      itemTypes: ['node'],
      shouldBegin(e) {
        if (e.item._cfg.model.label.includes('...')) {
          return true;
        }
        return false;
      },

      getContent: e => {
        const outDiv = document.createElement('div');
        outDiv.style.maxWidth = '300px';
        outDiv.innerHTML = `
          ${e.item._cfg.model.tooltipLabel}
         `;
        return outDiv;
      }
    });
    this.canvas = new G6.Graph({
      container: this.$refs.canvas,
      plugins: [tooltip],
      modes:
        (nodeWidth + spaceX) * this.CIData.stages.length + 1 >
        this.canvasize.width
          ? {
              default: [
                {
                  type: 'drag-canvas',
                  allowDragOnItem: true,
                  direction: 'x'
                }
              ]
            }
          : { default: [] },
      ...this.canvasize,
      defaultNode: {
        type: 'rect',
        anchorPoints: [[0, 0.5], [1, 0.5]]
      },
      defaultEdge: {
        type: 'cubic-horizontal',
        sourceAnchor: 1,
        targetAnchor: 0,
        style: {
          stroke: CI_COLORS.line,
          lineWidth: lineWidth
        }
      }
    });
    this.canvas.read(this.canvasData);
  }
};
</script>
<style scoped lang="stylus">

.temp
  border-radius 6px
  border #0378EA   1px solid
.temp-page
  border #b0bec5 1px solid
.w995
  width 995px
.head-style
  height 42px
  border-top-left-radius: 4px
  border-top-right-radius: 4px
.btn
  width 30px
.align-right
  position absolute
  right 10px
.text-body2
  position: absolute
  right: 8px
  bottom: 8px
  width: 360%
  text-align: right
  color: #9e9e9e
.container
  position relative
  height: 100px
// .bg-primary-page
//   background-color $indigogrey-0
</style>
