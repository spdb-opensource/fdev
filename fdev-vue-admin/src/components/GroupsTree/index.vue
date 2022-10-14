<template>
  <GroupsTree
    ref="tree"
    :data="treeData"
    :value="value"
    @handleGroupsSelect="handleGroupsSelect"
  />
</template>

<script>
import GroupsTree from './GroupsTree';
export default {
  components: { GroupsTree },
  data() {
    return {
      treeData: this.data,
      selected: []
    };
  },
  props: {
    data: Array,
    value: Array
  },
  watch: {
    data: {
      deep: true,
      handler(val) {
        this.treeData = this._addAttribute(this.data);
      }
    },
    value: {
      deep: true,
      handler(val) {
        this.treeData = this._addAttribute(this.treeData);
      }
    }
  },
  methods: {
    _addAttribute(data, bool) {
      if (!Array.isArray(data)) {
        return data;
      }
      return data.map(v => {
        return {
          ...v,
          _isSelect: this.value.includes(v.id),
          _isExpand: v._isExpand === undefined ? false : v._isExpand,
          children: this._addAttribute(v.children)
        };
      });
    },
    handleGroupsSelect() {
      this.selected = [];
      this.handleDeepItem(this.treeData);
      this.$emit(
        'input',
        this.selected.map(item => {
          return item.id;
        })
      );
    },
    handleDeepItem(groups) {
      if (!Array.isArray(groups)) {
        return;
      }
      groups.forEach(group => {
        if (group._isSelect) {
          this.selected.push(group);
        }
        this.handleDeepItem(group.children);
      });
    },
    handleItem(groups = this.treeData, key) {
      if (!Array.isArray(groups)) {
        return;
      }
      groups.forEach(group => {
        group[key] = false;
        this.handleItem(group.children, key);
      });
    },
    closed() {
      this.$refs.tree.closed();
    }
  }
};
</script>
