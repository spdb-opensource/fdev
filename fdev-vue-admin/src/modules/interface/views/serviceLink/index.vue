<template>
  <f-block>
    <Loading :visible="loading" class="bg-white q-pa-md">
      <fdev-form class="row" @submit="filterWithData">
        <div class="row q-gutter-y-sm justify-start q-gutter-x-lg">
          <f-formitem label-style="width:40px" label="应用">
            <fdev-select
              use-input
              :value="app"
              @input="updateApp($event)"
              :options="appOptions"
              option-label="name_en"
              option-value="name_en"
              @filter="filterApp"
              clearable
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name_en">{{
                      scope.opt.name_en
                    }}</fdev-item-label>
                    <fdev-item-label :title="scope.opt.name_zh" caption>
                      {{ scope.opt.name_zh }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem label-style="width:40px" label="分支">
            <fdev-select
              :value="branch"
              @input="updateBranch($event)"
              :options="['master', 'sit']"
            />
          </f-formitem>
          <div>
            <fdev-tooltip position="top" v-if="!app">
              <span>请选择一个应用</span>
            </fdev-tooltip>
            <fdev-btn :disable="!app" label="查询" dialog type="submit">
            </fdev-btn>
          </div>

          <!-- <fdev-btn :disable="!app" label="查询" dialog type="submit" /> -->
        </div>
      </fdev-form>
      <Chart
        v-show="serviceLink[filterDataModel.serviceId]"
        :name="filterDataModel.serviceId"
        ref="chart"
      />
      <div v-show="!serviceLink[filterDataModel.serviceId]" class="q-mt-md">
        <fdev-icon name="warning" />
        没有可用数据
      </div>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
import Chart from './components/Chart';
export default {
  name: 'ServiceLink',
  components: { Loading, Chart },
  data() {
    return {
      loading: false,
      appOptions: [],
      filterDataModel: {}
    };
  },
  computed: {
    ...mapState('userActionSaveInterface/ServiceLink', ['app', 'branch']),
    ...mapState('appForm', ['appData']),
    ...mapState('interfaceForm', ['serviceLink'])
  },
  methods: {
    ...mapMutations('userActionSaveInterface/ServiceLink', [
      'updateApp',
      'updateBranch'
    ]),
    ...mapActions('appForm', ['queryApplication']),
    ...mapActions('interfaceForm', ['getServiceChainInfo']),
    filterApp(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase().trim();
        this.appOptions = this.appData.filter(v => {
          return (
            v.name_en.toLowerCase().includes(needle) || v.name_zh.includes(val)
          );
        });
      });
    },
    filterWithData() {
      this.filterDataModel = {
        serviceId: this.app.name_en,
        branch: this.branch
      };
      this.init();
    },
    async init() {
      this.loading = true;
      await this.getServiceChainInfo(this.filterDataModel);
      this.$refs.chart.draw();
      this.loading = false;
    }
  },
  created() {
    this.queryApplication();
    if (this.app && this.branch) {
      this.filterWithData();
    }
  }
};
</script>

<style lang="stylus" scoped>
.chart
  .roots
    text-align center
.input
  min-width 200px
</style>
