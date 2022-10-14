<template>
  <div ref="container" />
</template>

<script>
import { RingProgress } from '@antv/g2plot';
export default {
  name: 'ProgressMap',
  data() {
    return {
      pic: null
    };
  },
  props: { percent: String },
  computed: {
    successRate() {
      return Number(
        String(Math.round(this.percent.replace('%', ''))).split('.')[0]
      );
    },
    color() {
      return this.successRate < 70
        ? '#EF5350'
        : this.successRate < 90
        ? '#FEC400'
        : '#4DBB59';
    }
  },
  mounted() {
    this.pic = new RingProgress(this.$refs.container, {
      height: 32,
      width: 32,
      autoFit: false,
      percent: this.successRate / 100,
      color: [this.color, '#ECEFF1'],
      statistic: {
        content: {
          style: { fontSize: 8 },
          formatter: ({ percent }) => {
            let val = String(Math.round(percent * 100)).split('.')[0];
            return `${val}`;
          }
        }
      }
    });

    this.pic.render();
  }
};
</script>

<style></style>
