<template>
  <div class="bg-white groups-tree">
    <div
      class="q-mr-md group inline-block q-mb-sm"
      :class="[
        item.class,
        { 'group-hover': item._isExpand },
        { 'bg-primary text-white': item._isSelect }
      ]"
      v-for="(item, i) in treeData"
      :key="i"
    >
      <button
        @click.stop="add(item)"
        class="text-grey-7"
        :class="{ 'text-white': item._isSelect }"
      >
        {{ item.label }}
      </button>
      <f-icon
        class="text-grey-7"
        :class="{ 'text-white': item._isSelect }"
        :name="item._isExpand ? 'arrow_u_o' : 'arrow_d_o'"
        v-if="item.children && item.children.length > 0"
        @click="open(oldExpandItem, item)"
      />
    </div>
    <GroupsTree
      ref="child"
      :data="childrenData"
      v-if="childrenData.length > 0"
      @handleGroupsSelect="handleGroupsSelect"
      :value="value"
    />
  </div>
</template>

<script>
export default {
  name: 'GroupsTree',
  data() {
    return {
      selected: [],
      treeData: [],
      childrenData: [],
      oldExpandItem: null
    };
  },
  props: {
    data: Array,
    value: Array
  },
  watch: {
    data: {
      immediate: true,
      handler(val) {
        this.treeData = val;
        this.handleGroupExpand();
      }
    }
  },
  methods: {
    add(item) {
      item._isSelect = !item._isSelect;
      const groupIndex = this.selected.indexOf(item.id);
      if (groupIndex > -1) {
        this.selected.splice(groupIndex, 1);
      } else {
        this.selected.push(item.id);
      }
      this.$emit('handleGroupsSelect');
    },
    open(oldItem, newItem) {
      if (oldItem && oldItem !== newItem) {
        oldItem._isExpand = false;
        this.closed(this.treeData);
      }
      newItem._isExpand = !newItem._isExpand;
      this.handleGroupExpand();
      this.oldExpandItem = newItem;
    },
    handleGroupsSelect() {
      this.$emit('handleGroupsSelect');
    },
    closed(groups = this.treeData) {
      groups.forEach(group => {
        group._isExpand = false;
      });
      if (this.$refs.child) {
        this.$refs.child.closed();
      }
      this.childrenData = [];
    },
    handleGroupExpand() {
      const expandGroup = this.treeData.find(group => group._isExpand);
      this.childrenData = expandGroup ? expandGroup.children : [];
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
</style>
