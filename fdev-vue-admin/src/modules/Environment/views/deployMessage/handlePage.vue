<template>
  <f-block>
    <Loading :visible="loading" class="bg-white">
      <fdev-stepper v-model="step" animated flat class="text-center" keep-alive>
        <fdev-step :name="1" title="第一步" icon="settings" :done="step > 1">
          <div class="form-wrapper q-pa-lg">
            <f-formitem diaS label="模糊查询应用" required>
              <fdev-select
                use-input
                hide-dropdown-icon
                input-debounce="500"
                transition-show="jump-down"
                transition-hide="jump-down"
                v-model="app"
                option-label="name_en"
                option-value="id"
                :options="appsOptions"
                @filter="filterApp"
              >
                <template v-slot:prepend>
                  <f-icon name="search" class="cursor-pointer" />
                </template>

                <template v-slot:option="scope">
                  <fdev-item
                    @click="handleSelectedApp(scope.opt)"
                    v-bind="scope.itemProps"
                    v-on="scope.itemEvents"
                  >
                    <fdev-item-section>
                      <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                      <fdev-item-label caption>
                        {{ scope.opt.name_zh }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
          </div>
        </fdev-step>

        <fdev-step
          :name="2"
          title="第二步"
          icon="settings"
          :done="step > 2"
          header-nav
        >
          <StepTwo
            :app_name_en="app_name_en"
            :appId="appId"
            @next="handleStep(3)"
            @prev="handleStep(1)"
          />
        </fdev-step>

        <fdev-step
          :name="3"
          title="第三步"
          icon="setting"
          :done="step > 3"
          header-nav
        >
          <stepThree @next="handleStep(4)" @prev="handleStep(2)" />
        </fdev-step>

        <fdev-step
          :name="4"
          title="第四步"
          icon="setting"
          :done="step > 4"
          header-nav
        >
          <StepFour @next="handleStep(5)" @prev="handleStep(3)" />
        </fdev-step>
      </fdev-stepper>
      <div class="form-wrapper q-pb-lg" v-if="step === 5">
        <Result type="success">
          <template v-slot:title>
            {{ $route.query.appId ? '编辑' : '新增' }}成功
          </template>
        </Result>
      </div>
    </Loading>
  </f-block>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import Loading from '@/components/Loading';
import Result from '@/components/Result';
import StepTwo from './StepTwo';
import StepThree from './StepThree';
import StepFour from './StepFour';
export default {
  name: 'DeployMessageHandlePage',
  components: {
    Loading,
    Result,
    StepTwo,
    StepThree,
    StepFour
  },
  data() {
    return {
      appId: '',
      step: 1,
      loading: false,
      app: null,
      app_name_en: '',
      appsOptions: []
    };
  },
  computed: {
    ...mapState('appForm', ['appData'])
  },
  methods: {
    ...mapActions('appForm', ['queryApplication']),
    filterApp(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.appsOptions = this.appData.filter(v => {
          return (
            v.name_en.toLowerCase().includes(needle) || v.name_zh.includes(val)
          );
        });
      });
    },
    async handleStep(step) {
      this.step = step;
    },
    handleSelectedApp(app) {
      const { id, name_en } = app;
      this.app = app;
      this.appId = id;
      this.app_name_en = name_en;
      this.handleStep(2);
    },
    goEnvModelList() {
      this.$router.push('/envModel/DeployMessage');
    }
  },
  created() {
    this.appId = this.$route.query.appId;
    if (this.appId) {
      this.step = 2;
      this.handleStep(2);
    }
    this.queryApplication();
  }
};
</script>

<style lang="stylus" scoped>
.form-wrapper
  width 60%
  margin 70px auto 150px auto
.model-wrapper
  width 60%
  margin 0 auto
.font
  font-weight: 700
.chip-wrapper
  max-width 300px
  display flex
  flex-wrap: wrap;
.td-wrapper
  width 300px
</style>
