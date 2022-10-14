<template>
  <div class="content-v">
    <section>
      <div class="row justify-between">
        <span class="text-subtitle2">扫描结果</span>
        <fdev-btn
          dash
          rounded
          class="q-ml-lg"
          label="扫描详情"
          v-if="infoData.scanResult === '不通过'"
          @click="gotoScanDetail"
        />
      </div>
      <div class="row flex content-center q-mt-sm">
        <f-image name="pass-icon" v-if="infoData.scanResult === '通过'" />
        <img v-else src="../../assets/notPass.svg" alt="" />
        <span class="pass-text">{{ infoData.scanResult }}</span>
      </div>
      <div class="progress-v">
        <div
          class="q-mt-md q-mb-md"
          v-for="(item, idx) in infoData.illegalSum"
          :key="idx"
        >
          <div class="row justify-between q-mb-sm">
            <span class="text-subtitle2">{{ item.title }}</span>
            <span class="text-h5 line-height22">
              {{ item.num || 0 }}{{ idx === 1 ? '/4' : '' }}
            </span>
          </div>
          <fdev-linear-progress
            rounded
            size="10px"
            :value="getProgressNum(item.num, idx)"
            :color="item.num > 0 ? item.color : 'blue-grey-1'"
            class="q-mt-sm"
          />
        </div>
      </div>
    </section>
    <section class="illegal-other-sum row wrap">
      <div
        v-for="(el, index) in infoData.otherIllagalSum"
        :key="index"
        class="column other-sum-item width-samll"
      >
        <span>
          {{ el.num || 0 }}
        </span>
        <span>{{ el.title }}</span>
      </div>
      <div v-if="infoData.reportUrl" class="column other-sum-item url-v">
        <div class="full-width text-left">
          <span>{{ infoData.reportUrl }}</span>
        </div>
        <span class="q-mb-sm">源代码扫描报告URL</span>
      </div>
    </section>
    <section class="performace-v">
      <div class="relative" style="width:116px">
        <span class="absolute">
          <span>{{ infoData.rbstNum }}/4</span>
          <p>健壮性</p>
        </span>
        <canvas id="rbstNum" width="100" height="100"></canvas>
      </div>
      <div class="relative" style="width:116px">
        <span class="absolute">
          {{ infoData.efcyNum }}/4
          <p>效率性</p>
        </span>
        <canvas id="efcyNum" width="100" height="100"> </canvas>
      </div>
      <div class="relative" style="width:116px">
        <span class="absolute">
          {{ infoData.secuNum }}/4<br />
          <p>安全性</p>
        </span>
        <canvas id="secuNum" width="100" height="100"> </canvas>
      </div>
      <div class="relative" style="width:100px;">
        <span class="absolute">
          {{ infoData.chngNum }}/4<br />
          <p>变更性</p>
        </span>
        <canvas id="chngNum" width="100" height="100"> </canvas>
      </div>
      <div class="relative" style="width:116px;display:block;margin-top:10px">
        <span class="absolute">
          {{ infoData.trsfNum }}/4<br />
          <p>迁移性</p>
        </span>
        <canvas id="trsfNum" width="100" height="100"> </canvas>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'CastSourceCodeScan',
  components: {},
  props: {
    dataSurce: Object
  },
  data() {
    return {
      infoData: {}
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
    //结果页
    gotoScanDetail() {
      let url = this.infoData.redRulesUrl;
      if (url) {
        window.open(url);
      }
    },
    getProgressNum(num, idx) {
      num = parseFloat(num || 0);
      let sum = parseFloat(this.infoData.actNum);
      if (idx === 1) {
        sum = 4; //固定满分为4分
      }
      return num / sum;
    },
    getRbstNum(id, color, percent) {
      let c = document.getElementById(id);
      let ctx = c.getContext('2d');
      let start = 2 - percent * Math.PI;
      let fixed = (3 / 2) * Math.PI;
      // 绿色;
      ctx.beginPath();
      // 圆心x坐标，圆心y坐标，圆心半径，画笔开始弧度，画笔结束弧度
      // arc(x,y,r,start,stop)
      ctx.arc(50, 50, 45, start, fixed);
      ctx.strokeStyle = '#' + color;
      ctx.lineWidth = '10';
      ctx.stroke();
      // 灰色
      ctx.beginPath();
      ctx.arc(50, 50, 45, fixed, 2 * Math.PI + start);
      ctx.strokeStyle = '#ECEFF1';
      ctx.lineWidth = '10';
      ctx.stroke();
      // 圆
      ctx.beginPath();
      ctx.arc(50, 50, 35, 0, 2 * Math.PI);
      ctx.strokeStyle = '#EDF0F1';
      ctx.lineWidth = '2';
      ctx.stroke();
    },
    initData(val) {
      if (val) {
        //拼接数据
        val.illegalSum = [
          {
            title: '红线违规项总数',
            num: val.redNum,
            color: 'red-7'
          },
          {
            title: '整体质量总分',
            num: val.tqiNum,
            color: 'blue-5'
          }
        ];
        val.otherIllagalSum = [
          {
            title: '违规项总数',
            num: val.atcNum
          },
          {
            title: '黄线违规项数量',
            num: val.yellowNum
          },
          {
            title: '白线违规项数量',
            num: val.whiteNum
          },
          {
            title: '功能点数量',
            num: val.afpNum
          },
          {
            title: '代码行数量',
            num: val.locsNum
          }
        ];
        //做两种代码扫描布局
        if (!val.reportUrl) {
          const obj = {
            title: '源代码扫描报告URL',
            num: '无'
          };
          val.otherIllagalSum.push(obj);
        }
        this.infoData = Object.assign({}, val);
      }
    }
  },
  mounted() {
    let info = Object.assign({}, this.dataSurce);
    this.initData(info);
    this.getRbstNum('rbstNum', 'BBD69C', info.rbstNum / 4); //健壮性
    this.getRbstNum('efcyNum', '77B3EE', info.efcyNum / 4); //效率性
    this.getRbstNum('secuNum', 'F0ABAB', info.secuNum / 4); //安全性
    this.getRbstNum('chngNum', 'F5DA78', info.chngNum / 4); //变更性
    this.getRbstNum('trsfNum', '9AAAE6', info.trsfNum / 4); //迁移性
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
.line-height22
  line-height 22px
.progress-v
  margin 24px auto
.q-linear-progress
  border-radius 8px
  margin 16px auto 20px
.performace-v
  margin-top: 24px
.relative
  display: inline-block;
  position: relative;
.absolute
  top: 30px;
  left: 25%;
  font-family: PingFangSC-Medium;
  font-size: 18px;
  text-align center
  p
    font-family: PingFangSC-Regular;
    font-size: 12px;
.url-v
  width calc(93% + 28px)
  padding 0 5px
</style>
