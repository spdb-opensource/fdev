<template>
  <div class="side-list">
    <div v-if="title" class="title text-center text-h6 text-blue-grey-9">
      {{ title }}
    </div>
    <fdev-input
      clearable
      @clear="onSearch"
      v-if="canSearch"
      class="q-mb-md"
      v-model="searchWord"
      @keyup.13="onSearch"
      ><template v-slot:append>
        <f-icon name="search" class="cursor-pointer" @click="onSearch" />
      </template>
    </fdev-input>
    <div v-if="hasData" class="scroll-thin list">
      <div
        :title="item[label]"
        v-for="(item, ind) in listData"
        :key="ind"
        @click="clickItem(ind)"
        class="text-subtitle1 hover-bg justify-start q-px-sm item cursor-pointer row items-center"
        :class="
          selectInd === ind
            ? 'select text-blue-9 bg-blue-0'
            : 'unselect text-blue-grey-9'
        "
      >
        {{ item[label] }}
      </div>
    </div>
    <f-image v-else class="q-mt-xl full-width" name="no_data" />
  </div>
</template>

<script>
export default {
  name: 'FSidelist',
  props: {
    title: String, //标题
    dataSource: Array, //数据源
    label: {
      type: String,
      default: 'label'
    },
    searchLabel: {
      type: String
    },
    search: Boolean,
    init: Boolean,
    tip: String //鼠标悬浮到item上的显示的字段,不展示悬浮则不添加
  },
  data() {
    return {
      selectInd: 0,
      noDataPic: require('@/assets/fImage/no_data.svg'),
      searchWord: '',
      listData: this.dataSource
    };
  },
  filters: {},
  computed: {
    canSearch() {
      return this.search || this.searchLabel;
    },
    hasData() {
      return this.listData && this.listData.length > 0;
    },
    searchKey() {
      return this.searchLabel || this.label;
    }
  },
  created() {
    this.init && this.clickItem(0);
  },
  watch: {
    dataSource(val) {
      this.listData = val;
    }
  },
  methods: {
    onSearch() {
      if (!this.searchWord) this.listData = this.dataSource;
      else {
        this.listData = this.dataSource.filter(
          item => item[this.searchKey].indexOf(this.searchWord) !== -1
        );
      }
    },
    //选中列表
    clickItem(ind) {
      this.selectInd = ind;
      this.$emit('clickItem', { item: this.listData[ind], index: ind });
    }
  }
};
</script>

<style scoped lang="stylus">
.side-list
  width 213px
  height 737px
.unselect
.side-list
  border-right solid 1px #ECEFF1
.title
  height 46px
.list
  width 213px
  height 691px
.item
  min-height 48px
  margin-bottom 8px
.select
  border-right solid 4px #1565C0
</style>
