<template>
  <div bothXY style="position: relative" class="scroll-normal">
    <div ref="container" />
  </div>
</template>

<script>
const JOB_NODE_CFG = {
  type: 'rect',
  size: [186, 48],
  style: {
    fill: '#0378EA',
    radius: 8,
    lineWidth: 0
  },
  labelCfg: {
    style: {
      fill: 'white',
      fontSize: 14,
      fontWeight: 500
    },
    position: 'center'
  }
};
const PLUGIN_NODE_CFG = {
  type: 'rect',
  size: [150, 40],
  style: {
    fill: '#F7F7F7',
    radius: 4,
    lineWidth: 1,
    stroke: '#BBBBBB'
  },
  labelCfg: {
    style: {
      fill: '#333333',
      fontSize: 14,
      fontWeight: 500
    },
    position: 'center'
  },
  anchorPoints: [[0, 0.5]]
};
import G6 from '@antv/g6';
import { calcStrLen } from '../../utils/constants';
export default {
  name: 'DetailPic',
  props: { graphData: Array },
  data() {
    return {
      graphCfg: null,
      graph: null
    };
  },
  methods: {
    calGraphSize(stages) {
      let JobPosiMap = this.calJobPositionMap(stages);
      let val = Object.values(JobPosiMap);
      let { x, y } = this.calPluginPosition(
        stages,
        `${stages.length - 1}_${val.length - 1}_${Math.max(...val) - 1}`
      );
      return { width: x + 93, height: y + 25 };
    },
    calJobPositionMap(stages) {
      let jobMax = Math.max(...stages.map(s => s.jobs.length));
      let JobPosiMap = {};
      for (let i = 0; i < jobMax; i++) {
        JobPosiMap[i] = Math.max(
          ...stages.map(stage => {
            let { jobs } = stage;
            return jobs[i] ? jobs[i].steps.length : 0;
          })
        );
      }
      return JobPosiMap;
    },
    calJobPosition(stages, id) {
      let [sInd, jInd] = id.split('_').map(x => Number(x));
      let JobPosiMap = this.calJobPositionMap(stages);
      let stageHeight = 0;
      if (jInd > 0) {
        for (let i = 0; i < jInd; i++) {
          let jobMax = JobPosiMap[i];
          stageHeight = stageHeight + jobMax * 48 + 32;
        }
      }
      let x = 93 + sInd * 296;
      let y = 24 + jInd * 78 + stageHeight;
      return { jx: x, jy: y };
    },
    calPluginPosition(stages, id) {
      let pInd = Number(id.split('_')[2]);
      let { jx, jy } = this.calJobPosition(stages, id);
      return {
        x: jx + 18,
        y: jy + 32 + (pInd + 1) * 48
      };
    }
  },

  mounted() {
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
    this.calJobPositionMap(this.graphData);
    let nodes = [];
    let edges = [];
    let maxSInd = this.graphData.length - 1;
    let baseY = this.calJobPosition(this.graphData, '0_0').jy;
    for (let sInd = 0; sInd < maxSInd; sInd++) {
      this.graphData[sInd].jobs.forEach((job, jInd) => {
        let jId = `${sInd}_${jInd}`;
        let { jx, jy } = this.calJobPosition(this.graphData, jId);
        edges.push({
          source: jId,
          target: `${sInd + 1}_0`,
          sourceAnchor: 2,
          targetAnchor: 0,
          controlPoints: [{ x: jx + 121, y: jy }, { x: jx + 121, y: baseY }]
        });
      });
    }
    for (let sInd = 1; sInd <= maxSInd; sInd++) {
      this.graphData[sInd].jobs.forEach((job, jInd) => {
        let jId = `${sInd}_${jInd}`;
        let { jx, jy } = this.calJobPosition(this.graphData, jId);
        edges.push({
          source: `${sInd - 1}_0`,
          target: jId,
          sourceAnchor: 2,
          targetAnchor: 0,
          controlPoints: [{ x: jx - 121, y: baseY }, { x: jx - 121, y: jy }]
        });
      });
    }
    this.graphData.forEach((stage, sInd) =>
      stage.jobs.forEach((job, jInd) => {
        let jId = `${sInd}_${jInd}`;
        let { jx, jy } = this.calJobPosition(this.graphData, jId);
        nodes.push({
          id: jId,
          x: jx,
          y: jy,
          ...JOB_NODE_CFG,
          label: fittingString(job.name, 150, 12),
          tooltip: job.name,
          anchorPoints: [[0, 0.5], [0.1, 1], [1, 0.5]]
        });
        job.steps.forEach((plugin, pInd) => {
          let pId = `${sInd}_${jInd}_${pInd}`;
          let { x, y } = this.calPluginPosition(this.graphData, pId);
          nodes.push({
            id: pId,
            x,
            y,
            ...PLUGIN_NODE_CFG,
            label: fittingString(plugin.name, 135, 12),
            tooltip: plugin.name
          });
          edges.push({
            source: jId,
            target: pId,
            sourceAnchor: 1,
            targetAnchor: 0,
            controlPoints: [{ x: jx - 85, y: jy + 24 }, { x: jx - 85, y }]
          });
        });
      })
    );
    // stage和job名字超长的提示
    const fontTooltip = new G6.Tooltip({
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
        outDiv.style.minWidth = '150px';
        outDiv.innerHTML = `
          ${e.item._cfg.model.tooltip}
         `;
        return outDiv;
      }
    });
    this.graphCfg = { nodes, edges };
    this.graph = new G6.Graph({
      container: this.$refs.container,
      ...this.calGraphSize(this.graphData),
      plugins: [fontTooltip],
      defaultEdge: {
        type: 'polyline',
        style: {
          strokeOpacity: 0.5,
          radius: 5,
          stroke: '#B0BEC5',
          lineWidth: 1,
          endArrow: true
        }
      }
    });
    this.graph.data(this.graphCfg);
    this.graph.render();
  }
};
</script>

<style></style>
