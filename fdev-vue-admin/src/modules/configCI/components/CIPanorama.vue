<template>
  <div
    :style="{ height: height }"
    class="q-pt-sm scroll-normal"
    style="position: relative"
  >
    <div ref="graph" />
  </div>
</template>
<script>
import {
  CI_COLORS,
  CI_ICON,
  THUMB_STYLE,
  calcStrLen
} from '../utils/constants';
import G6 from '@antv/g6';
export default {
  name: 'CIPanorama',
  props: {
    CIData: Object,
    canvasSize: Object,
    height: { type: String, default: '75vh' },
    RATIO: {
      type: Number,
      default: window.innerWidth / 1920
    },
    fontSize: {
      type: Number,
      default: (window.innerWidth / 1920) * 16
    }
  },
  data() {
    return { ...THUMB_STYLE, graph: null };
  },
  watch: {
    CIData(newVal) {
      // this.graph.changeData(newVal);
      this.graph.data(newVal);
      this.graph.render();
    },
    canvasSize(newVal) {
      this.graph.changeSize(newVal.width, newVal.height);
    }
  },
  mounted() {
    const CI_PANORAMA_SIZE = {
      nodeWidth: 260 * this.RATIO,
      nodeHeight: 48 * this.RATIO,
      // fontSize: 16 * this.RATIO,
      stageNameSize: 18 * this.RATIO,
      jobXSpace: 140 * this.RATIO,
      jobYSpace: 60 * this.RATIO,
      spaceInJob: 30 * this.RATIO,
      radius: 25 * this.RATIO,
      iconSize: 20 * this.RATIO
    };
    function fittingString(str, maxWidth, fontSize) {
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
    G6.registerNode('dot-node', {
      drawShape: function drawShape(cfg, group) {
        const shape = group.addShape('rect', {
          attrs: {
            fill: cfg.color,
            width: 1,
            height: 1,
            y: -0.5
          }
        });
        return shape;
      },
      getAnchorPoints() {
        return [
          [0, 0.5], // 左侧中间
          [1, 0.5], // 右侧中间
          [0.5, 0], //上中
          [0.5, 1] //下中
        ];
      }
    });
    G6.registerNode('add-stage', {
      drawShape: function drawShape(cfg, group) {
        const shape = group.addShape('image', {
          attrs: {
            x: 0,
            y: 0,
            width: CI_PANORAMA_SIZE.iconSize,
            height: CI_PANORAMA_SIZE.iconSize,
            img: CI_ICON.add,
            cursor: 'pointer'
          },
          name: 'add-stage'
        });

        return shape;
      },
      getAnchorPoints() {
        return [
          [0, 0.5], // 左侧中间
          [1, 0.5], // 右侧中间
          [0.5, 0], //上中
          [0.5, 1] //下中
        ];
      }
    });
    G6.registerNode('add-job', {
      drawShape: function drawShape(cfg, group) {
        const color = CI_COLORS.running;
        const shape = group.addShape('rect', {
          attrs: {
            x: 0,
            y: 0,
            width: CI_PANORAMA_SIZE.nodeWidth,
            height: CI_PANORAMA_SIZE.nodeHeight,
            stroke: color,
            radius: CI_PANORAMA_SIZE.radius,
            lineWidth: 1,
            fill: 'white',
            lineDash: [5, 5],
            cursor: 'pointer'
          },
          name: 'add-job'
        });
        group.addShape('text', {
          attrs: {
            text: '并行任务',
            textAlign: 'center',
            textBaseline: 'middle',
            fontSize: this.fontSize,
            fill: color,
            x: CI_PANORAMA_SIZE.nodeWidth / 2 + CI_PANORAMA_SIZE.iconSize / 2,
            y: CI_PANORAMA_SIZE.nodeHeight / 2,
            cursor: 'pointer'
          },
          name: 'add-job'
        });
        group.addShape('image', {
          attrs: {
            x: CI_PANORAMA_SIZE.nodeWidth / 4 + CI_PANORAMA_SIZE.iconSize,
            y: CI_PANORAMA_SIZE.nodeHeight / 2 - CI_PANORAMA_SIZE.iconSize / 2,
            width: CI_PANORAMA_SIZE.iconSize,
            height: CI_PANORAMA_SIZE.iconSize,
            img: CI_ICON.addNormal,
            cursor: 'pointer'
          },
          name: 'add-img'
        });
        return shape;
      }
    });
    G6.registerNode('stage-name', {
      drawShape: function drawShape(cfg, group) {
        const { stageName, basicFunc, editImg } = cfg;
        const shape = group.addShape('rect', {
          attrs: {
            x: CI_PANORAMA_SIZE.jobYSpace - CI_PANORAMA_SIZE.jobXSpace / 2,
            width:
              CI_PANORAMA_SIZE.nodeWidth +
              CI_PANORAMA_SIZE.jobXSpace -
              CI_PANORAMA_SIZE.jobYSpace * 2,
            height: CI_PANORAMA_SIZE.nodeHeight
          }
        });
        group.addShape('text', {
          attrs: {
            text: fittingString(stageName, 150, 12),
            textAlign: 'center',
            textBaseline: 'middle',
            fontSize: CI_PANORAMA_SIZE.stageNameSize,
            fill: '#37474F',
            x: CI_PANORAMA_SIZE.nodeWidth / 2,
            y: CI_PANORAMA_SIZE.nodeHeight / 2
          }
        });
        if (basicFunc) {
          group.addShape('image', {
            attrs: {
              x: CI_PANORAMA_SIZE.nodeWidth - CI_PANORAMA_SIZE.iconSize / 2,
              y:
                CI_PANORAMA_SIZE.nodeHeight / 2 - CI_PANORAMA_SIZE.iconSize / 2,
              width: CI_PANORAMA_SIZE.iconSize,
              height: CI_PANORAMA_SIZE.iconSize,
              img: editImg,
              cursor: 'pointer'
            },
            name: 'stage-name'
          });
        }
        return shape;
      }
    });
    G6.registerNode('ci-node', {
      drawShape: function drawShape(cfg, group) {
        const {
          isFill,
          bottomImg,
          leftImg,
          rightImg,
          color,
          label,
          basicFunc
        } = cfg;
        const cursor = basicFunc ? 'pointer' : 'default';
        const shape = group.addShape('rect', {
          attrs: {
            x: 0,
            y: 0,
            width: CI_PANORAMA_SIZE.nodeWidth,
            height: CI_PANORAMA_SIZE.nodeHeight,
            stroke: color,
            lineWidth: 1,
            fill: isFill ? color : 'white',
            radius: CI_PANORAMA_SIZE.radius,
            cursor: cursor
          },
          name: 'ci-node-basic'
        });
        group.addShape('text', {
          attrs: {
            text: fittingString(label, 200, 12),
            textAlign: 'center',
            textBaseline: 'middle',
            fontWeight: isFill ? 'bold' : 'normal',
            fontSize: this.fontSize,
            fill: isFill ? ' #f0f2f5' : color,
            x: CI_PANORAMA_SIZE.nodeWidth / 2,
            y: CI_PANORAMA_SIZE.nodeHeight / 2,
            zIndex: 1,
            cursor: cursor
          },
          name: 'ci-node-basic'
        });
        if (bottomImg) {
          group.addShape('image', {
            attrs: {
              x: CI_PANORAMA_SIZE.nodeWidth - CI_PANORAMA_SIZE.iconSize * 1.5,
              y:
                CI_PANORAMA_SIZE.nodeHeight / 2 - CI_PANORAMA_SIZE.iconSize / 2,
              width: CI_PANORAMA_SIZE.iconSize,
              height: CI_PANORAMA_SIZE.iconSize,
              img: bottomImg
              // cursor: 'pointer'
            },
            name: 'bottom-btn'
          });
          group.find(function(item) {
            return item.cfg.name === 'bottom-btn';
          });
        }
        if (rightImg) {
          group.addShape('image', {
            attrs: {
              x: CI_PANORAMA_SIZE.nodeWidth - CI_PANORAMA_SIZE.iconSize * 1.5,
              y:
                CI_PANORAMA_SIZE.nodeHeight / 2 - CI_PANORAMA_SIZE.iconSize / 2,
              width: CI_PANORAMA_SIZE.iconSize,
              height: CI_PANORAMA_SIZE.iconSize,
              img: rightImg,
              cursor: 'pointer'
            },
            name: 'right-btn'
          });
          group
            .find(function(item) {
              return item.cfg.name === 'right-btn';
            })
            .hide();
        }
        if (leftImg) {
          group.addShape('image', {
            attrs: {
              x: CI_PANORAMA_SIZE.iconSize / 2,
              y:
                CI_PANORAMA_SIZE.nodeHeight / 2 - CI_PANORAMA_SIZE.iconSize / 2,
              width: CI_PANORAMA_SIZE.iconSize,
              height: CI_PANORAMA_SIZE.iconSize,
              img: leftImg
            },
            name: 'left-img'
          });
        }
        return shape;
      },
      getAnchorPoints() {
        return [
          [0, 0.5], // 左侧中间
          [1, 0.5], // 右侧中间
          [0.5, 0], //上中
          [0.5, 1] //下中
        ];
      }
    });
    // 插件是否有效的提示
    const validTooltip = new G6.Tooltip({
      offsetX: 10,
      offsetY: 10,
      itemTypes: ['node'],
      shouldBegin(e) {
        if (e.item._cfg.model.bottomImg) {
          return true;
        }
        return false;
      },

      getContent: e => {
        const outDiv = document.createElement('div');
        outDiv.style.width = '200px';
        outDiv.innerHTML = `
        <div style="font-size:16px; font-weight: 600">提示</div>
          <span>插件有升级或已废弃，建议重新选择!</span>
         `;
        return outDiv;
      }
    });
    // stage和job名字超长的提示
    const fontTooltip = new G6.Tooltip({
      offsetX: 10,
      offsetY: 10,
      itemTypes: ['node'],
      shouldBegin(e) {
        let { stageName } = e.item._cfg.model;
        let { label } = e.item._cfg.model;
        let labelTip = '';
        let stageTip = '';
        if (label) labelTip = fittingString(label, 200, 12);
        if (stageName) stageTip = fittingString(stageName, 150, 12);
        if (labelTip.includes('...') || stageTip.includes('...')) {
          return true;
        }
        return false;
      },

      getContent: e => {
        const outDiv = document.createElement('div');
        outDiv.style.maxWidth = '300px';
        outDiv.style.minWidth = '150px';
        let tooltip = e.item._cfg.model.label || e.item._cfg.model.stageName;
        outDiv.innerHTML = tooltip;
        return outDiv;
      }
    });
    this.graph = new G6.Graph({
      container: this.$refs.graph,
      autoPaint: true,
      ...this.canvasSize,
      plugins: [validTooltip, fontTooltip],
      defaultEdge: {
        sourceAnchor: 1,
        targetAnchor: 0,
        style: {
          lineWidth: 1,
          stroke: CI_COLORS.line
        }
      }
    });
    this.graph.read(this.CIData);
    //单击节点主体触发回调函数
    this.graph.on('ci-node-basic:click', evt => {
      const { id, basicFunc } = evt.item._cfg.model;
      basicFunc ? basicFunc(id) : null;
    });
    //点击节点底部按钮触发回调函数
    this.graph.on('bottom-btn:click', evt => {
      const { id, bottomBtnFunc, funcType } = evt.item._cfg.model;
      bottomBtnFunc(id, funcType);
    });
    //点击节点右侧按钮触发函数
    this.graph.on('right-btn:click', evt => {
      const { id, rightBtnFunc } = evt.item._cfg.model;
      rightBtnFunc(id);
    });
    //添加stage
    this.graph.on('add-stage:click', evt => {
      const { id, onClick, funcType } = evt.item._cfg.model;
      onClick(id, funcType);
    });
    //stage内添加Job
    this.graph.on('add-job:click', evt => {
      const { id, onClick, funcType } = evt.item._cfg.model;
      onClick(id, funcType);
    });
    //修改stage-name
    this.graph.on('stage-name:click', evt => {
      const { item } = evt;
      const model = item.get('model');
      const { basicFunc, id } = model;
      let sInd = id.split('-')[1];
      const { x, y } = item.calculateBBox();
      const graph = evt.currentTarget;
      const realPosition = evt.currentTarget.getClientByPoint(x, y);
      const el = document.createElement('div');
      el.style.fontSize = '14px';
      el.style.position = 'fixed';
      el.style.top = realPosition.y + this.fontSize / 2 + 'px';
      el.style.left = realPosition.x + 'px';
      el.style.paddingLeft = '12px';
      el.style.transformOrigin = 'top left';
      el.style.transform = `scale(${evt.currentTarget.getZoom()})`;
      const input = document.createElement('input');
      input.value = model.stageName;
      input.style.color = '#37474F';
      input.style.border = '1px solid #1565C0';
      input.style.textAlign = 'center';
      input.style.width = CI_PANORAMA_SIZE.nodeWidth + 20 + 'px';
      input.className = 'dice-input';
      el.className = 'dice-input';
      el.appendChild(input);
      document.body.appendChild(el);
      const destroyEl = () => {
        document.body.removeChild(el);
      };
      const clickEvt = event => {
        if (!event.target['className'].includes('dice-input')) {
          window.removeEventListener('mousedown', clickEvt);
          window.removeEventListener('scroll', clickEvt);
          basicFunc(sInd, input.value);
          destroyEl();
        }
      };
      graph.on('wheelZoom', clickEvt);
      window.addEventListener('mousedown', clickEvt);
      window.addEventListener('scroll', clickEvt);
      input.addEventListener('keyup', event => {
        if (event.key === 'Enter') {
          clickEvt({
            target: {}
          });
        }
      });
    });
    this.graph.on('node:mouseenter', evt => {
      const { group, currentShape } = evt.item._cfg;
      switch (currentShape) {
        case 'ci-node': {
          ['right-btn', 'bottom-btn'].forEach(x => {
            let item = group.find(function(item) {
              return item.cfg.name === x;
            });
            item ? item.show() : null;
          });
          break;
        }
        default: {
          break;
        }
      }
    });
    this.graph.on('node:mouseleave', evt => {
      const { group, currentShape } = evt.item._cfg;
      switch (currentShape) {
        case 'ci-node': {
          ['right-btn'].forEach(x => {
            let item = group.find(function(item) {
              return item.cfg.name === x;
            });
            item ? item.hide() : null;
          });
          break;
        }
        default: {
          break;
        }
      }
    });
  }
};
</script>
