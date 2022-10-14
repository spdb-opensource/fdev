import Vue from 'vue';

import Top from './top.js';
import Body from './body.js';

import { deepClone } from '@/utils/utils.js';

export default Vue.extend({
  name: 'FGstree',

  mixins: [Top, Body],

  props: {
    dataSource: Array,
    titleClass: [String, Array, Object]
  },

  data() {
    return {
      treeData: deepClone(this.dataSource),
      selectedGroups: []
    };
  },

  computed: {
    showData() {
      return this.createShowData([this.treeData]);
    },
    classes() {
      return {
        staticClass: 'fdev-gstree'
      };
    }
  },

  methods: {
    //重置整棵树
    reset() {
      this.treeData = deepClone(this.dataSource);
      this.selectedGroups = [];
      this.resetNode(this.treeData);
      this.$emit('reset-tree', this.treeData);
    },
    //将整棵树设为未选中和未打开
    resetNode(tree) {
      tree.forEach(node => {
        node.selected = false;
        if (node.children) {
          node.open = false;
          this.resetNode(node.children);
        }
      });
    },
    createShowData(array) {
      const lastTree = array[array.length - 1];
      let [max, currentNode] = [-Infinity, null];
      lastTree.forEach(node => {
        if (node.open === true) {
          if (node.sort > max) {
            max = node.sort;
            currentNode = node;
          } else {
            !currentNode && (currentNode = node);
          }
        }
      });
      if (currentNode) {
        array.push(currentNode.children);
        this.createShowData(array);
      }
      return array;
    }
  },

  render(h) {
    const child = [this.__getTopDiv(h)];
    child.push(this.__getBody(h));

    return h('div', this.classes, child);
  }
});
