<template>
  <div class="q-mt-md">
    <fdev-tabs v-model="tab">
      <fdev-tab name="sit" label="内测自动化测试环境" />
      <fdev-tab name="uat" label="UAT自动化测试环境" />
      <fdev-tab name="rel" label="准生产自动化测试环境" />
    </fdev-tabs>

    <fdev-tab-panels v-model="tab">
      <fdev-tab-panel :name="tab">
        <div class="row justify-center q-mt-lg">
          <f-formitem :label="tabName[tab] + '环境'" class="form-width">
            <fdev-select
              use-input
              multiple
              v-model="autoTestModel"
              option-label="name_en"
              option-value="id"
              :options="filterEnvList"
              @filter="envListFilter"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name_en">
                      {{ scope.opt.name_en }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.name_cn">
                      {{ scope.opt.name_cn }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
        </div>
        <div class="row justify-center q-mt-lg">
          <f-formitem :label="tabName[tab] + '开关'" class="form-width">
            <div>
              <fdev-toggle
                v-model="projectDeploySwitchInfo"
                @input="handlePipelineScheduleOpen"
                left-label
                true-value="true"
                false-value="false"
                :disable="!autoTestModel || autoTestModel.length === 0"
              />
              <fdev-tooltip v-if="!autoTestModel || autoTestModel.length === 0"
                >请选择自动化测试环境</fdev-tooltip
              >
              <span class="text-grey-8">
                {{ projectDeploySwitchInfo === 'true' ? '开' : '关' }}
              </span>
            </div>
          </f-formitem>
        </div>
        <div class="row justify-center q-mt-lg">
          <fdev-btn
            label="提交修改"
            @click="handleUpdateAutoTest"
            :disable="!autoTestModel || autoTestModel.length === 0"
            :loading="globalLoading['appForm/updateAutoTest']"
          />
          <fdev-tooltip v-if="!autoTestModel || autoTestModel.length === 0"
            >请选择自动化测试环境</fdev-tooltip
          >
        </div>
      </fdev-tab-panel>
    </fdev-tab-panels>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { createModel } from './model.js';
import { successNotify } from '@/utils/utils';

export default {
  name: 'AuthmatedTest',
  props: {
    gitlab_project_id: {
      type: Number
    }
  },
  components: {},
  data() {
    return {
      tab: 'sit',
      tabName: {
        sit: '内测自动化测试',
        uat: 'UAT自动化测试',
        rel: '准生产自动化测试'
      },
      env: [],
      autoTestModel: null,
      projectDeploySwitchInfo: '',
      envSwitch: {
        '1': 'true',
        '0': 'false'
      },
      filterEnvList: [],
      copyAutoTestData: createModel()
    };
  },
  watch: {
    tab: {
      async handler() {
        await this.queryAutoEnv({ labels: [this.tab, 'auto'] });
        this.initObj(
          this.copyAutoTestData[this.tab + 'AutoTest'],
          this.copyAutoTestData[this.tab + 'ProjectDeploySwitchInfo']
        );
      },
      immediate: true
    }
  },
  computed: {
    ...mapState('appForm', ['autoTestData']),
    ...mapState('environmentForm', {
      autoEnvList: 'autoEnvList'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    })
  },
  methods: {
    ...mapActions('appForm', {
      queryAutoTest: 'queryAutoTest',
      updateAutoTest: 'updateAutoTest'
    }),
    ...mapActions('environmentForm', {
      queryAutoEnv: 'queryAutoEnv'
    }),
    handlePipelineScheduleOpen(val) {
      this.projectDeploySwitchInfo = val === 'true' ? 'true' : 'false';
    },
    initObj(autoTest, projectDeploySwitchInfo) {
      autoTest = autoTest ? autoTest : [];
      projectDeploySwitchInfo = projectDeploySwitchInfo
        ? projectDeploySwitchInfo
        : '0';
      this.autoTestModel = null;
      if (autoTest.length > 0) {
        this.autoTestModel = autoTest.map(item => {
          return {
            id: item[this.tab + 'AutoTestEnvId'],
            name_en: item[this.tab + 'AutoTestEnvName']
          };
        });
      }
      this.projectDeploySwitchInfo = this.envSwitch[projectDeploySwitchInfo];
    },
    async envListFilter(val, update) {
      update(() => {
        this.filterEnvList = this.autoEnvList.filter(tag => {
          return (
            tag.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            tag.name_cn.indexOf(val) > -1
          );
        });
      });
    },
    async handleUpdateAutoTest() {
      let switchInfo = this.projectDeploySwitchInfo === 'true' ? '1' : '0';
      this.copyAutoTestData[this.tab + 'AutoTest'] = this.autoTestModel.map(
        item => {
          return {
            [this.tab + 'AutoTestEnvId']: item.id,
            [this.tab + 'AutoTestEnvName']: item.name_en
          };
        }
      );
      this.copyAutoTestData[this.tab + 'ProjectDeploySwitchInfo'] = switchInfo;
      await this.updateAutoTest(this.copyAutoTestData);
      successNotify('修改成功！');
      await this.queryAutoTest({ gitlab_project_id: this.gitlab_project_id });
    }
  },
  async mounted() {
    await this.queryAutoTest({ gitlab_project_id: this.gitlab_project_id });
    if (Object.keys(this.autoTestData).length > 0) {
      this.copyAutoTestData = this.autoTestData;
    } else {
      this.copyAutoTestData.gitlab_project_id = this.gitlab_project_id;
    }
    this.initObj(
      this.copyAutoTestData.sitAutoTest,
      this.copyAutoTestData.sitProjectDeploySwitchInfo
    );
  }
};
</script>

<style lang="stylus" scoped>
/deep/ .form-width .label
  min-width 150px
</style>
