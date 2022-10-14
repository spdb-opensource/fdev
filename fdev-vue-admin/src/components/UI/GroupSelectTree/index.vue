<template>
  <div>
    <div class="row no-wrap">
      <f-icon name="member" class="text-primary" style="margin-right:8px" />
      <label class="label-style">所属小组：</label>
      <fdev-space />
      <span
        class="text-primary q-ml-md float-right cursor-pointer"
        @click="reset()"
      >
        重置
      </span>
    </div>
    <div v-for="(tree, i) in showData" :key="i" class="row justify-start">
      <fdev-btn-group
        v-for="node in tree"
        :key="node.id"
        unelevated
        class="q-mr-md group inline-block q-mb-sm"
        :class="[
          { 'group-hover': node.selected },
          { 'bg-primary text-white': node.selected }
        ]"
      >
        <button
          @click.stop="selectGroup(node)"
          class="text-grey-7"
          :class="{ 'text-white': node.selected }"
        >
          {{ node.label }}
        </button>
        <f-icon
          v-if="node.children"
          class="text-grey-7"
          :class="{ 'text-white': node.selected }"
          :name="node.open ? 'arrow_u_o' : 'arrow_d_o'"
          @click="changeOpenStatus(node.id)"
        />
      </fdev-btn-group>
    </div>
  </div>
</template>
<script>
export default {
  name: 'GroupSelectTree',
  props: {
    dataSource: Array
  },
  data() {
    return {
      groupTree: this.deepCloneData(this.dataSource)
    };
  },
  computed: {
    //展示的数据
    showData() {
      return this.createShowData([this.groupTree]);
    },
    //被选中的组
    selectedGroups() {
      return this.showData
        .map(y => y.filter(x => x.selected).map(x => x.id))
        .reduce((acc, curr) => acc.concat(curr));
    }
  },
  methods: {
    //重置整棵树
    reset() {
      this.groupTree = this.deepCloneData(this.dataSource);
      this.resetNode(this.groupTree);
      this.$emit('resetTree', this.groupTree);
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
    //点击选择组
    selectGroup(node) {
      node.selected = !node.selected;
      this.$emit('selectGroup', this.selectedGroups);
      this.$emit('clickGroup', this.groupTree);
    },
    //把dataSource给深拷贝了
    deepCloneData(tree) {
      let newTree = JSON.parse(JSON.stringify(tree));
      return newTree;
    },

    //生成showData
    createShowData(array) {
      let lastTree = array[array.length - 1];
      let openInd = lastTree.findIndex(x => x.open);
      if (openInd != -1) {
        array.push(lastTree[openInd].children);
        this.createShowData(array);
      }
      return array;
    },
    //点击组的展开收起按钮触发的函数
    changeOpenStatus(id) {
      let ind = this.showData.findIndex(
        x => x.findIndex(y => y.id === id) != -1
      );
      let tree = this.showData[ind];
      let node = tree[tree.findIndex(y => y.id === id)];
      node.open = !node.open;
      node.open
        ? tree.forEach(node => (node.id === id ? null : (node.open = false)))
        : null;
      this.$emit('clickGroup', this.groupTree);
    }
  }
};
</script>

<style lang="stylus" scoped>
.group
  vertical-align top
  border-radius 3px
  overflow hidden
  &:hover
    background #ebeef5
.group-hover
  background #ebeef5
button
  border none
  outline none
  background none
  cursor pointer
.q-icon
  font-size: 20px;
  vertical-align: text-top;
.groups-tree
  width 100%
button:focus
  outline none
.text-white
  color white!important
.label-style
  font-family: PingFangSC-Regular;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
  font-weight:600
</style>
