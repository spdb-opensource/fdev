<template>
  <div class="bg-white groups-tree">
    <div
      class="q-mr-md group inline-block q-mb-sm"
      :class="[
        item.class,
        { 'group-hover': item.expand },
        { 'bg-primary text-white': item.selected || selected.includes(item.id) }
      ]"
      v-for="(item, i) in nodes"
      :key="i"
    >
      <button
        @click.stop="select([], item)"
        class="text-grey-7"
        :class="{ 'text-white': item.selected || selected.includes(item.id) }"
      >
        {{ item.label }}
      </button>
      <f-icon
        class="text-grey-7"
        :class="{ 'text-white': item.selected || selected.includes(item.id) }"
        :name="item.expand === false ? 'arrow_d_o' : 'arrow_u_o'"
        v-if="item.children ? item.children.length > 0 : false"
        @click="openChild(i, item.expand)"
      />
    </div>
    <GroupsTree
      ref="child"
      @click="changeSelectType"
      @done="handleDone"
      @input="handleDone"
      @deleted="deleted"
      v-show="nodes[index] && nodes[index].expand && open"
      :data="nodes[index] && nodes[index].children"
      v-if="nodes[index] && nodes[index].children"
    />
  </div>
</template>

<script>
import { deepClone } from '@/utils/utils';

export default {
  name: 'GroupsTree',
  data() {
    return {
      selected: Array.isArray(this.firstData)
        ? deepClone(this.firstData)
        : [this.firstData],
      nodes: this.data,
      index: 0,
      selectList: [],
      show: false,
      ids: [],
      final: [],
      open: false
    };
  },
  props: {
    data: {
      type: Array,
      default: () => []
    },
    firstData: {}
  },
  watch: {
    data(val) {
      this.nodes = val;
    }
  },
  methods: {
    openChild(index, bool) {
      this.nodes.forEach(item => {
        item.expand = false;
      });
      this.nodes[index].expand = !bool;
      this.index = index;
      this.open = true;
    },
    select(id, item) {
      if (item) {
        item.selected =
          item.selected === true || this.selected.includes(item.id)
            ? false
            : true;
        if (item.selected === false) {
          this.deleted(item.id);
        }
      }
      this.changeSelectType();
      this.$emit('click');
      this.selected = this.ids.concat(
        this.$refs.child ? this.$refs.child.ids : [null]
      );
      this.selected = Array.from(new Set(this.selected));
      this.$emit('done', this.selected);
      if (this.firstData && this.$refs.child) {
        this.$refs.child.select();
      } else if (this.firstData) {
        this.handleDone();
      }
    },
    changeSelectType() {
      const selected = this.nodes.filter(item => {
        return item.selected === true;
      });
      this.ids = Array.from(
        new Set(selected.map(item => item.id).concat(this.selected))
      );
    },
    handleDone(ids = []) {
      this.changeSelectType();
      this.final = ids.concat(this.ids).filter(item => {
        return item !== null && item !== undefined;
      });
      this.final = Array.from(new Set(this.final));
      this.$emit('input', this.final);
    },
    deleted(id) {
      if (this.selected.includes(id)) {
        this.selected.splice(this.selected.indexOf(id), 1);
      }
      if (this.ids.indexOf(id) > -1) {
        this.ids.splice(this.ids.indexOf(id), 0);
      }
      this.$emit('deleted', id);
    },
    reset() {
      this.selected = Array.isArray(this.firstData)
        ? [...this.firstData]
        : [this.firstData];
      this.$refs.child.selected = [];
      this.$refs.child.ids = [];
      this.childReset();
      this.nodes = deepClone(this.data);
      this.$emit('input', Array.from(new Set(this.selected)));
    },
    childReset() {
      if (this.$refs.child) {
        this.$refs.child.selected = [];
        this.$refs.child.ids = [];
        this.$refs.child.childReset();
      }
    },
    closed() {
      this.open = !this.open;
      this.$refs.child.open = !this.$refs.child.open;
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
