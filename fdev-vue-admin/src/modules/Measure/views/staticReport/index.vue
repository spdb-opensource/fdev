<template>
  <Loading :visible="loading">
    <!-- 分类 -->
    <f-block
      v-for="(type, index) in reportObj"
      :key="index"
      class="block-style"
      page
    >
      <!-- 类名 -->
      <div class="row">
        <div class="typeName">{{ type.nameCn }}</div>
        <fdev-space />
        <!-- 收起/展开按钮 -->
        <div>
          <span
            class="text-primary float-right cursor-pointer folder-style"
            @click="close(index)"
          >
            {{ type.showBlock ? '收起' : '展开'
            }}<f-icon
              :name="type.showBlock ? 'arrow_u_o' : 'arrow_d_o'"
              class="q-ml-xs"
            />
          </span>
        </div>
      </div>

      <!-- 类下的指标 -->
      <div v-if="type.showBlock">
        <div class="row q-gutter-xl" v-if="compDevidedByType[type.nameEn]">
          <div
            v-for="report in reportCfg.filter(
              report => report.type === type.nameEn
            )"
            :key="report.nameEn"
            class="cursor-pointer"
            @click="goDetail(report.path)"
          >
            <div class="column item-style">
              <img :src="report.img" style="width:60px;height:60px" />
              <div class="desc-style">{{ report.desc }}</div>
            </div>
          </div>
        </div>
        <!-- 无数据时 -->
        <div v-else class="column items-center">
          <f-image name="no_data_1" class="q-mt-sm" />
          <span class="text-grey-3 q-mt-xs"
            >尚未提供该类型指标，敬请期待！</span
          >
        </div>
      </div>
      <!-- 有数据时 -->
    </f-block>
  </Loading>
</template>
<script>
import Loading from '@/components/Loading';
import { initCompCfg } from '../../utils/reportCfg';
export default {
  components: { Loading },
  data() {
    return {
      reportObj: [],
      reportCfg: [],
      compDevidedByType: {},
      loading: false
    };
  },
  methods: {
    async init() {
      this.loading = true;
      let { REPORT_CFG, REPORT_OBJ } = await initCompCfg();
      this.reportCfg = REPORT_CFG;
      this.reportObj = REPORT_OBJ;
      this.reportObj.forEach(item => {
        let comps = this.reportCfg.filter(comp => comp.type === item.nameEn);
        this.compDevidedByType[item.nameEn] = comps.length > 0 ? comps : null;
      });
      this.loading = false;
    },
    // 进入指标详情
    goDetail(path) {
      this.$router.push({ path: path });
    },
    // 收起/展开按钮
    close(index) {
      this.reportObj[index].showBlock = !this.reportObj[index].showBlock;
    }
  },
  mounted() {
    this.init();
  }
};
</script>
<style scoped lang="stylus">
.block-style
   padding-top:32px;
   padding-bottom:32px
   margin-bottom:20px
.typeName
   font-family: PingFangSC-Semibold;
   font-size: 18px;
   color: #0663BE;
   letter-spacing: 0;
   line-height: 18px;
   font-weight:600;
   margin-bottom:30px
.item-style
   display:table-cell
   text-align:center
   max-width:120px;
   padding: 10px 14px;
.desc-style
  font-family: PingFangSC-Regular;
  font-size: 12px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
  padding-top:4px;
  width:90px
  max-width :100px
.item-style:hover
   border-radius:1px
   background-color:#F4F7FF
.folder-style
   font-family: PingFangSC-Regular;
   font-size: 14px;
   letter-spacing: 0;
   line-height:18px;
</style>
