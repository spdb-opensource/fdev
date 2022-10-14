<template>
  <f-block>
    <Loading :visible="loading">
      <div class="addMarginTop">
        <div
          v-for="(item, index) in proInfoCount"
          :key="index"
          class="q-mb-llg"
        >
          <div class="row ">
            <fdev-img :src="titleIcon" style="width:16px;height:16px" />
            <span class="title-style q-ml-sm">{{ item.type }}</span>
          </div>
          <div class="row justify-between cards">
            <fdev-card
              class="card-style"
              v-for="(detail, index) in item.platform"
              :key="index"
            >
              <div class="q-ml-llg q-mt-llg title-style">
                {{ detail.title }}
              </div>
              <count-charts
                :countOpt="detail.count"
                :countTotal="detail.total"
                :countTitle="detail.title"
              />
            </fdev-card>
          </div>
        </div>
      </div>
    </Loading>
  </f-block>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { proInfo } from '../../utils/constants';
import countCharts from './components/countCharts.vue';
import Loading from '@/components/Loading';
export default {
  components: { countCharts, Loading },
  name: 'colonyProductionInfo',
  data() {
    return {
      titleIcon: require('../../assets/title.svg'),
      proInfoCount: proInfo,
      loading: false
    };
  },
  computed: {
    ...mapState('environmentForm', ['proInfo'])
  },
  methods: {
    ...mapActions('environmentForm', ['queryProInfo']),
    initCount() {
      this.proInfoCount.forEach((item, index) => {
        if (item.type === 'Caas平台') {
          item.platform.forEach(val => {
            if (val.title === '应用') {
              Reflect.set(val, 'count', this.proInfo.caas_deploy_detail);
              Reflect.set(val, 'total', this.proInfo.caas_deploy_count);
            } else if (val.title === '容器') {
              Reflect.set(val, 'count', this.proInfo.caas_pod_detail);
              Reflect.set(val, 'total', this.proInfo.caas_pod_count);
            }
          });
        } else if (item.type === 'SCC平台') {
          item.platform.forEach(val => {
            if (val.title === '应用') {
              Reflect.set(val, 'count', this.proInfo.scc_deploy_detail);
              Reflect.set(val, 'total', this.proInfo.scc_deploy_count);
            } else if (val.title === '容器') {
              Reflect.set(val, 'count', this.proInfo.scc_pod_detail);
              Reflect.set(val, 'total', this.proInfo.scc_pod_count);
            }
          });
        }
      });
    }
  },
  async created() {
    this.loading = true;
    await this.queryProInfo();
    await this.initCount();
    this.loading = false;
  }
};
</script>

<style lang="stylus" scoped>
.addMarginTop
  margin-top: 10px;
.title-style
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 16px;
  font-weight: 600;
.cards
  margin-top 20px
.card-style
  background: #FFFFFF;
  box-shadow: 0 2px 10px 0 rgba(21,101,192,0.20);
  border-radius: 8px;
  width: 48.5%
  height: 296px
</style>
