<template>
  <div>
    <f-dialog
      title="选择插件"
      right
      :value="value"
      @input="$emit('input', false)"
    >
      <f-formitem label="搜索" class="q-mb-md" v-if="allPlugins.length">
        <fdev-input
          clearable
          placeholder="请输入任务名称"
          v-model="pluginName"
          @keyup.enter.native="pluginFilter"
        >
          <template v-slot:append>
            <f-icon
              name="search"
              class="cursor-pointer"
              @click="pluginFilter"
            />
          </template>
        </fdev-input>
      </f-formitem>
      <fdev-splitter v-model="splitterModel" unit="px" class="w750">
        <template v-slot:before>
          <fdev-tabs v-model="tab" dense vertical>
            <fdev-tab
              v-for="(job, index) in allPlugins"
              :key="index"
              :name="job.category.categoryName"
              :label="job.category.categoryName"
            />
          </fdev-tabs>
        </template>
        <template v-slot:after>
          <fdev-tab-panels v-model="tab" vertical>
            <fdev-tab-panel
              v-for="(jobInfo, index) in allPlugins"
              :key="index"
              :name="jobInfo.category.categoryName"
              class="row"
            >
              <div
                class="col-6"
                v-for="(item, index) in jobInfo.pluginList"
                :key="index"
                @click.stop="selectPluginInfo(item)"
              >
                <fdev-card class="q-ma-sm hand">
                  <div
                    class="text-subtitle2 text-grey-9 q-px-md q-pt-xs ellipsis"
                  >
                    {{ item.pluginName }}
                    <fdev-tooltip>
                      {{ item.pluginName }}
                    </fdev-tooltip>
                  </div>
                  <div class="row q-px-md q-py-sm">
                    <div class="col-5 text-center">
                      <span
                        class="hand detail-link text-light-blue-9"
                        @click.stop="showDetail(item)"
                      >
                        详情说明
                      </span>
                      <!-- <fdev-btn
                        label="详情说明"
                        outline
                        rounded
                        size="xs"
                        @click.stop="showDetail(item)"
                        color="light-blue-9"
                      /> -->
                    </div>
                    <div class="col q-pl-md">
                      <span>评分:</span
                      ><span class="q-pl-sm text-orange-13">{{
                        item.average
                      }}</span>
                    </div>
                  </div>
                </fdev-card>
              </div>
            </fdev-tab-panel>
          </fdev-tab-panels>
        </template>
      </fdev-splitter>
      <div class="flex-center row no-data" v-if="allPlugins.length === 0">
        <f-image name="no_data"></f-image>
      </div>
    </f-dialog>
    <md v-model="mdModalOpened" :pluginInfo="pluginInfo" @closeMd="closeMd" />
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import md from './mdToHtml';
export default {
  name: 'selectPlugin',
  components: { md },
  data() {
    return {
      splitterModel: 140,
      tab: '',
      pluginName: '',
      allPlugins: [],
      mdModalOpened: false,
      pluginInfo: {}
    };
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    platform: String
  },
  watch: {
    pluginName(val) {
      this.allPlugins = this.plugins.slice();
    }
  },
  computed: {
    ...mapState('configCIForm', {
      plugins: 'plugins',
      pluginDetailInfo: 'pluginDetailInfo'
    })
  },
  methods: {
    ...mapActions('configCIForm', {
      queryPlugin: 'queryPlugin',
      queryPluginDetail: 'queryPluginDetail'
    }),
    closeDialog() {
      this.$emit('close');
    },
    async selectPluginInfo(info) {
      await this.queryPluginDetail({
        pluginCode: info.pluginCode
      });
      this.pluginInfo = this.pluginDetailInfo;
      this.$emit('getPlugin', this.pluginInfo);
    },
    pluginFilter() {
      let _this = this;
      _this.allPlugins = [];
      this.plugins.forEach(item => {
        let pluginList = item.pluginList.filter(job => {
          return (
            job.pluginName
              .toLocaleLowerCase()
              .indexOf(_this.pluginName.toLocaleLowerCase()) > -1
          );
        });
        if (pluginList.length > 0) {
          _this.allPlugins.push({
            category: item.category,
            pluginList: pluginList
          });
        }
      });
      this.tab = this.allPlugins.length
        ? this.allPlugins[0].category.categoryName
        : '';
    },
    clearInput() {
      this.pluginName = '';
      this.allPlugins = this.plugins.slice();
    },
    showDetail(info) {
      this.pluginInfo = info;
      this.mdModalOpened = true;
    },
    closeMd() {
      this.mdModalOpened = false;
    }
  },
  async mounted() {
    let { platform } = this;
    if (!this.value) return;
    let pramas = {
      pluginType: '0',
      key: []
    };
    //未选集群则查询所有的插件
    if (platform) {
      pramas.platform = [platform];
    }
    await this.queryPlugin(pramas);
    if (this.plugins.length) {
      this.tab = this.plugins[0].category.categoryName;
    }
    this.allPlugins = this.plugins.slice();
  }
};
</script>

<style lang="stylus" scoped>
.w750
 width 750px
.hand
  cursor pointer
  width 250px
.detail-link
  border: 1px solid #00aef0;
  border-radius: 12px;
  display: inline-block;
  width: 100%;
.ellipsis
  word-break break-all
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
  width: 100%;
.no-data
  margin-top 25%
</style>
