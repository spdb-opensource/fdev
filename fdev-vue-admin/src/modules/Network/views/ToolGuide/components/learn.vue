<template>
  <!-- 等待层 -->
  <Loading :visible="loading">
    <div class="fdevblock">
      <div class="row flex-center">
        <div class="col-md-9 col-xs ">
          <fdev-input
            v-model="searchTotalName"
            clearable
            @keyup.13="searchFunction"
            large
            placeholder="请输入名称搜索"
            ><template v-slot:append>
              <f-icon
                name="search"
                class="cursor-pointer"
                @click="searchFunction"
              />
            </template>
          </fdev-input>
        </div>
      </div>
      <div class="row martop">
        <div
          v-for="(link, index) in list"
          :key="index"
          class="cursor-pointer card-background"
          @click="goDetail(link.url)"
        >
          <div class="column item-style">
            <img :src="getSrc(link.iconEn)" />
            <div class="desc-style">{{ link.name }}</div>
            <fdev-tooltip position="top" v-if="link.tooltip">
              {{ link.tooltip }}
            </fdev-tooltip>
          </div>
        </div>
      </div>
      <div class="column items-center" v-if="dataFlag">
        <f-image name="no_data_1" class="q-mt-sm" />
        <span class="text-grey-3 q-mt-xs"
          >尚未提供该网址，如需要请联系fdev团队！</span
        >
      </div>
    </div>
  </Loading>
</template>
<script>
import Loading from '@/components/Loading';

export default {
  name: 'learnlist',
  props: {
    params: {
      type: Object
    }
  },
  components: { Loading },

  data() {
    return {
      loading: false,
      list: [],
      searchTotalName: '',
      dataFlag: false
    };
  },
  watch: {
    searchTotalName(val) {
      if (!val) {
        this.init();
        this.dataFlag = false;
      }
    }
  },
  methods: {
    //
    goDetail(path) {
      window.open(path);
    },
    getSrc(item) {
      return require(`@/modules/Network/assets/toolguide/${item}.svg`);
    },
    searchFunction() {
      this.list = this.list.filter(item =>
        item.name.toLowerCase().includes(this.searchTotalName.toLowerCase())
      );
      if (this.list.length === 0) {
        this.dataFlag = true;
      }
    },
    //数据初始化
    init() {
      this.list = this.params.learn;
    }
  },
  async created() {
    // this.loading = true;
    //初始化用户列表
    this.init();
  }
};
</script>
<style lang="stylus" scoped>
.fdevblock
  padding-top: 32px;
  padding-bottom: 16px;
  padding-left: 32px;
  padding-right: 32px;
  background: #fff !important;
.martop
  margin-top: 35px
  padding-left: 2px
.item-style
   display:table-cell
   text-align:center
   max-width:104px;
   padding: 10px 15px;
.desc-style
  font-family: PingFangSC-Regular;
  font-size: 12px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
  padding-top:4px;
  width: 72px;
.card-background
   border-radius:1px
   margin-right:16px
   margin-bottom: 12px;
.card-background:hover
  background: #FFFFFF;
  box-shadow: 0 2px 10px 0 rgba(21,101,192,0.20);
  border-radius: 8px 8px 8px 0 8px;
</style>
