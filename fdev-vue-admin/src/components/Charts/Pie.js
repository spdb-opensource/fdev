import ECharts from 'vue-echarts';

export default {
  name: 'Pie',
  data() {
    return {};
  },
  props: {
    animate: Boolean,
    color: String,
    colors: Array,
    hasLegend: Boolean,
    padding: String,
    percent: Number,
    data: [],
    total: Number,
    tooltip: Boolean,
    valueFormat: Function
  },
  computed: {},
  methods: {},
  render(h) {
    return h(
      ECharts,
      {
        staticClass: ''
      },
      []
    );
  }
};
