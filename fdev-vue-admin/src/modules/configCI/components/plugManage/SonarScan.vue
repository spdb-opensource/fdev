<template>
  <f-dialog
    title="sonar扫描详情"
    right
    :value="isShow"
    @before-close="beforeClose"
  >
    <div class="content-v">
      <section>
        <div class="row justify-between">
          <span class="text-subtitle2">质量阈状态</span>
        </div>
        <div class="row flex content-center q-mt-sm">
          <f-image name="pass-icon" v-if="resultStr === '正常'" />
          <f-image name="warning_orange" v-else-if="resultStr === '警告'" />
          <f-image name="no-pass-icon" v-else />
          <span class="pass-text">
            {{ resultStr }}
          </span>
        </div>
      </section>
      <section class="illegal-other-sum row wrap">
        <div
          v-for="(el, index) in infoData"
          :key="index"
          class="column other-sum-item width-samll"
        >
          <span
            v-if="
              el.metric === 'coverage' ||
                el.metric === 'duplicated_lines_density'
            "
          >
            {{ el.value || 0 }}%
          </span>
          <span v-else>
            {{ el.value || 0 }}
          </span>
          <span>{{ el.title }}</span>
        </div>
      </section>
    </div>
  </f-dialog>
</template>

<script>
export default {
  name: 'CastSourceCodeScan',
  components: {},
  props: {
    isShow: {
      type: Boolean,
      default: false
    },
    dataSurce: Array
  },
  data() {
    return {
      infoData: [],
      resultStr: '',
      title: {
        alert_status: '',
        bugs: 'Bugs',
        vulnerabilities: '漏洞',
        security_hotspots: '安全热点',
        sqale_index: '债',
        code_smells: '异味',
        coverage: '覆盖率',
        duplicated_lines_density: '重复率',
        duplicated_blocks: '重复块'
      }
    };
  },
  computed: {},
  filters: {},
  watch: {
    dataSurce(val) {
      this.initData(val);
    }
  },
  methods: {
    beforeClose() {
      this.$emit('update:isShow', false);
    },
    initData(arr) {
      if (arr && arr.length) {
        //拼接数据
        let scanResult = {
          OK: '正常',
          WARN: '警告',
          ERROR: '错误'
        };
        let newArr = [];
        arr.forEach(item => {
          if (item.metric === 'alert_status') {
            this.resultStr = scanResult[item.value];
          } else {
            item.title = this.title[item.metric];
            newArr.push(item);
          }
        });
        this.infoData = newArr;
      }
    }
  },
  mounted() {
    this.initData(this.dataSurce);
  }
};
</script>
<style lang="stylus" scoped>
.content-v
  margin-top 20px
  width 500px
  color #333
.illegal-other-sum
  background: #F4F6FD;
  border-radius: 8px;
  padding 37px 16px 21px 16px
  margin-top 30px
  .other-sum-item
    border 1px solid #0378EA
    border-top 7px solid #0378EA
    margin-right 14px
    margin-bottom 16px
    text-align center
    display flex
    border-radius: 6px;
    span:first-child
      font-family: PingFangSC-Medium;
      font-size: 20px;
      margin-top 3px
    span:last-child
      font-family: PingFangSC-Regular;
      font-size: 12px;
      line-height: 22px;
    &:nth-child(3n)
      margin-right 0px
  .width-samll
    width 31%
    height 70px
.pass-text
  font-family: PingFangSC-Medium;
  font-size: 32px;
  margin-left 16px
</style>
