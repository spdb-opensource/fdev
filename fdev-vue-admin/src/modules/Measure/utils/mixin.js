import * as echarts from 'echarts';
export const UI_TEMP_MIXIN = {
  props: {
    dataSource: Object,
    w: Number,
    h: Number,
    render: Boolean,
    currentBlock: Number
  },
  data() {
    return { chart: null };
  },
  computed: {
    containerSize() {
      return {
        width: ((this.currentBlock - 64) / 12) * this.w * 0.9,
        height: screen.width <= 1280 ? 85 * this.h : 82 * this.h
      };
    }
  },
  mounted() {
    this.chart = echarts.init(this.$refs.container, null, this.containerSize);
  },
  watch: {
    containerSize(val) {
      this.chart.resize(val);
    },
    dataSource(val) {
      this.chart.clear();
      this.draw();
    }
  }
};
