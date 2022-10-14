<template>
  <div v-if="btnLabel" class="text-center q-my-md">
    <f-image name="no_data" />
  </div>
  <div v-else>
    <p class="text-h6 text-left">CaaS部署信息</p>
    <fdev-list class="rounded-borders">
      <fdev-expansion-item
        v-for="(item, key, i) in tableData"
        :key="i"
        :default-opened="i === 0"
      >
        <template v-slot:header>
          <fdev-item-section>
            <div class="section text-left">
              {{ key }}
            </div>
          </fdev-item-section>
        </template>
        <fdev-timeline
          color="secondary"
          class="q-pb-md q-px-md bg-white text-left"
        >
          <fdev-timeline-entry v-for="(table, index) in item" :key="index">
            <template v-slot:title>
              <span class="timeline-title">{{ table.name_cn }}</span>
            </template>
            <fdev-table
              class="my-sticky-column-table"
              :columns="columns"
              :data="table.env_key"
              row-key="id"
              :pagination.sync="pagination"
              flat
              no-select-cols
              noExport
              hide-bottom
            >
              <template v-slot:body-cell-name_en="props">
                <fdev-td
                  class="td-width text-ellipsis"
                  :title="props.row.name_en || '-'"
                >
                  {{ props.row.name_en || '-' }}
                </fdev-td>
              </template>

              <template v-slot:body-cell-name_cn="props">
                <fdev-td
                  class="td-width text-ellipsis"
                  :title="props.row.name_cn || '-'"
                >
                  {{ props.row.name_cn || '-' }}
                </fdev-td>
              </template>
              <template v-slot:body-cell-value="props">
                <!-- 如果own_value有值，则显示own_value，没有则显示value，都没有则显示’#error#‘ -->
                <fdev-td
                  class="td-width text-ellipsis"
                  v-if="
                    Array.isArray(props.row.own_value)
                      ? props.row.own_value.length > 0
                      : props.row.own_value
                  "
                >
                  <div
                    class="chip-wrapper"
                    v-if="Array.isArray(props.row.own_value)"
                  >
                    <Chip
                      v-for="(item, index) in props.row.own_value"
                      :key="index"
                      :data="item"
                      :name="props.row.name_en + index"
                    />
                  </div>
                  <div
                    v-else-if="
                      props.row.own_value &&
                        props.row.json_schema &&
                        !Array.isArray(props.row.own_value)
                    "
                  >
                    <fdev-chip
                      outline
                      square
                      color="teal"
                      dense
                      text-color="white"
                    >
                      {{ props.row.name_en }}
                      <fdev-tooltip>
                        <div class="tooltip">
                          <p
                            v-for="(val, key, i) in props.row.own_value"
                            :key="i"
                          >
                            {{ key }}：{{ val }}
                          </p>
                        </div>
                      </fdev-tooltip>
                    </fdev-chip>
                  </div>
                  <span v-else>
                    <span
                      v-if="props.row.own_value && !props.row.json_schema"
                      :title="props.row.own_value"
                      >{{ props.row.own_value }}</span
                    >
                    <span v-else>{{ props.row.own_value }}</span>
                  </span>
                </fdev-td>

                <fdev-td class="td-width text-ellipsis" v-else>
                  <span
                    class="text-red"
                    v-if="!props.row.value || props.row.value.length === 0"
                    >#error#</span
                  >
                  <div
                    class="chip-wrapper"
                    v-if="Array.isArray(props.row.value)"
                  >
                    <chip
                      v-for="(item, index) in props.row.value"
                      :key="index"
                      :data="item"
                      :name="props.row.name_en + index"
                    />
                  </div>
                  <div
                    v-else-if="
                      props.row.value &&
                        props.row.json_schema &&
                        !Array.isArray(props.row.value)
                    "
                  >
                    <fdev-chip
                      outline
                      square
                      color="teal"
                      dense
                      text-color="white"
                    >
                      {{ props.row.name_en }}
                      <fdev-tooltip>
                        <div class="tooltip">
                          <p v-for="(val, key, i) in props.row.value" :key="i">
                            {{ key }}：{{ val }}
                          </p>
                        </div>
                      </fdev-tooltip>
                    </fdev-chip>
                  </div>
                  <span v-else>
                    <span
                      v-if="props.row.value && !props.row.json_schema"
                      :title="props.row.value"
                      >{{ props.row.value }}</span
                    >
                    <span v-else>{{ props.row.value }}</span>
                  </span>
                </fdev-td>
              </template>
            </fdev-table>
          </fdev-timeline-entry>
        </fdev-timeline>
      </fdev-expansion-item>
    </fdev-list>
    <fdev-separator class="q-my-lg" />
    <p class="text-h6 text-left">SCC部署信息</p>
    <fdev-list class="rounded-borders">
      <fdev-expansion-item
        v-for="(item, key, i) in sccTableData"
        :key="i"
        :default-opened="i === 0"
      >
        <template v-slot:header>
          <fdev-item-section>
            <div class="section text-left">
              {{ key }}
            </div>
          </fdev-item-section>
        </template>
        <fdev-timeline
          color="secondary"
          class="q-pb-md q-px-md bg-white text-left"
        >
          <fdev-timeline-entry v-for="(table, index) in item" :key="index">
            <template v-slot:title>
              <span class="timeline-title">{{ table.name_cn }}</span>
            </template>
            <fdev-table
              class="my-sticky-column-table"
              :columns="columns"
              :data="table.env_key"
              row-key="id"
              :pagination.sync="pagination"
              flat
              no-select-cols
              noExport
              hide-bottom
            >
              <template v-slot:body-cell-name_en="props">
                <fdev-td
                  class="td-width text-ellipsis"
                  :title="props.row.name_en || '-'"
                >
                  {{ props.row.name_en || '-' }}
                </fdev-td>
              </template>

              <template v-slot:body-cell-name_cn="props">
                <fdev-td
                  class="td-width text-ellipsis"
                  :title="props.row.name_cn || '-'"
                >
                  {{ props.row.name_cn || '-' }}
                </fdev-td>
              </template>
              <template v-slot:body-cell-value="props">
                <!-- 如果own_value有值，则显示own_value，没有则显示value，都没有则显示’#error#‘ -->
                <fdev-td
                  class="td-width text-ellipsis"
                  v-if="
                    Array.isArray(props.row.own_value)
                      ? props.row.own_value.length > 0
                      : props.row.own_value
                  "
                >
                  <div
                    class="chip-wrapper"
                    v-if="Array.isArray(props.row.own_value)"
                  >
                    <Chip
                      v-for="(item, index) in props.row.own_value"
                      :key="index"
                      :data="item"
                      :name="props.row.name_en + index"
                    />
                  </div>
                  <div
                    v-else-if="
                      props.row.own_value &&
                        props.row.json_schema &&
                        !Array.isArray(props.row.own_value)
                    "
                  >
                    <fdev-chip
                      outline
                      square
                      color="teal"
                      dense
                      text-color="white"
                    >
                      {{ props.row.name_en }}
                      <fdev-tooltip>
                        <div class="tooltip">
                          <p
                            v-for="(val, key, i) in props.row.own_value"
                            :key="i"
                          >
                            {{ key }}：{{ val }}
                          </p>
                        </div>
                      </fdev-tooltip>
                    </fdev-chip>
                  </div>
                  <span v-else>
                    <span
                      v-if="props.row.own_value && !props.row.json_schema"
                      :title="props.row.own_value"
                      >{{ props.row.own_value }}</span
                    >
                    <span v-else>{{ props.row.own_value }}</span>
                  </span>
                </fdev-td>

                <fdev-td class="td-width text-ellipsis" v-else>
                  <span
                    class="text-red"
                    v-if="!props.row.value || props.row.value.length === 0"
                    >#error#</span
                  >
                  <div
                    class="chip-wrapper"
                    v-if="Array.isArray(props.row.value)"
                  >
                    <chip
                      v-for="(item, index) in props.row.value"
                      :key="index"
                      :data="item"
                      :name="props.row.name_en + index"
                    />
                  </div>
                  <div
                    v-else-if="
                      props.row.value &&
                        props.row.json_schema &&
                        !Array.isArray(props.row.value)
                    "
                  >
                    <fdev-chip
                      outline
                      square
                      color="teal"
                      dense
                      text-color="white"
                    >
                      {{ props.row.name_en }}
                      <fdev-tooltip>
                        <div class="tooltip">
                          <p v-for="(val, key, i) in props.row.value" :key="i">
                            {{ key }}：{{ val }}
                          </p>
                        </div>
                      </fdev-tooltip>
                    </fdev-chip>
                  </div>
                  <span v-else>
                    <span
                      v-if="props.row.value && !props.row.json_schema"
                      :title="props.row.value"
                      >{{ props.row.value }}</span
                    >
                    <span v-else>{{ props.row.value }}</span>
                  </span>
                </fdev-td>
              </template>
            </fdev-table>
          </fdev-timeline-entry>
        </fdev-timeline>
      </fdev-expansion-item>
    </fdev-list>
    <fdev-btn
      label="上一步"
      dialog
      class="q-my-md q-mr-md"
      @click="$emit('prev')"
      v-if="!isAppProfile"
    />

    <fdev-btn
      v-if="!isAppProfile"
      label="确定"
      dialog
      class="q-my-md q-mr-md"
      @click="handleStep4"
      :loading="globalLoading['environmentForm/bindAppInfo']"
    />
  </div>
</template>

<script>
import { deployMessageColumns } from '../../utils/constants';
import { mapState, mapActions } from 'vuex';
import Chip from '../../components/Chip';
export default {
  name: 'StepFour',
  components: { Chip },
  data() {
    return {
      tableData: {},
      sccTableData: {},
      columns: deployMessageColumns().detailColumns.slice(0, 3),
      pagination: {
        rowsPerPage: 0,
        page: 1
      }
    };
  },
  props: {
    isAppProfile: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('environmentForm', ['deployDetail', 'realTimeBindMsg']),
    btnLabel() {
      return (
        JSON.stringify(this.deployDetail.caas_model_env_mapping) === '{}' &&
        JSON.stringify(this.deployDetail.scc_model_env_mapping) === '{}'
      );
    }
  },
  watch: {
    tableData(val) {
      if (val && val.length > 0) {
        this.$emit('hasData');
      }
    },
    sccTableData(val) {
      if (val && val.length > 0) {
        this.$emit('hasData');
      }
    }
  },
  methods: {
    ...mapActions('environmentForm', [
      'bindAppInfo',
      'saveSitConfigProperties'
    ]),

    getEnvTestConfigList(modelsInfo) {
      let envTestConfigList = [];
      let configIndex = -1;
      for (const key in modelsInfo) {
        let envTestConfig = {
          name_en: '',
          env_key: []
        };
        envTestConfig.name_en = key;
        // 如果没有"ci_config",则不发saveSitConfigProperties接口
        modelsInfo[key].filter((val, index) => {
          if (val.name_en.startsWith('ci_config')) {
            // 记住ci_config_此时位于数组的顺序
            configIndex = index;
          }
        });
        if (configIndex > -1) {
          for (const item of modelsInfo[key][configIndex].env_key) {
            envTestConfig.env_key.push({
              [item.name_en]: item.value
            });
          }
          envTestConfigList.push(envTestConfig);
        }
      }
      return {
        configIndex,
        envTestConfigList
      };
    },

    async handleStep4() {
      let modelsInfo = [];
      Object.keys(this.tableData).forEach(item => {
        modelsInfo = modelsInfo.concat(this.tableData[item]);
      });
      Object.keys(this.sccTableData).forEach(item => {
        modelsInfo = modelsInfo.concat(this.sccTableData[item]);
      });
      const detail = {
        app_id: this.realTimeBindMsg.appInfo.app_id,
        envProList: this.realTimeBindMsg.envProList,
        envTestList: this.deployDetail.envTestList,
        sccSitList: this.deployDetail.sccSitList,
        sccUatList: this.deployDetail.sccUatList,
        sccRelList: this.deployDetail.sccRelList,
        sccProList: this.deployDetail.sccProList,
        modelsInfo: modelsInfo,
        caas_status: this.deployDetail.caas_status,
        scc_status: this.deployDetail.scc_status,
        scc_modelSet: this.deployDetail.sccModelSetMsg
          ? this.deployDetail.sccModelSetMsg.id
          : '',
        modelSet: this.deployDetail.modelSetMsg
          ? this.deployDetail.modelSetMsg.id
          : '',
        network: this.deployDetail.network.toString()
      };

      const caasConfigList = this.getEnvTestConfigList(
        this.realTimeBindMsg.modelsInfo
      );
      const sccConfigList = this.getEnvTestConfigList(
        this.realTimeBindMsg.scc_modelsInfo
      );
      if (caasConfigList.configIndex > -1 || sccConfigList.configIndex > -1) {
        const envSccTestList = this.realTimeBindMsg.envSccList.filter(
          v => v.labels.indexOf('pro') === -1
        );
        const configFileData = {
          app_id: this.realTimeBindMsg.appInfo.app_id,
          envTestList: this.realTimeBindMsg.envTestList.concat(envSccTestList),
          envTestConfig: caasConfigList.envTestConfigList.concat(
            sccConfigList.envTestConfigList
          )
        };
        await this.saveSitConfigProperties(configFileData);
      }
      await this.bindAppInfo(detail);
      this.$emit('next');
    },
    init() {
      this.tableData = this.realTimeBindMsg.modelsInfo;
      this.sccTableData = this.realTimeBindMsg.scc_modelsInfo;
    },
    appProfileinit() {
      this.tableData = this.deployDetail.caas_model_env_mapping;
      this.sccTableData = this.deployDetail.scc_model_env_mapping;
    }
  },
  activated() {
    this.init();
  }
};
</script>

<style lang="stylus" scoped>

.font
  font-weight: 700
.chip-wrapper
  max-width 500px
  display flex
  flex-wrap: wrap;
.td-width
  max-width 500px!important
  overflow hidden
  text-overflow ellipsis
  text-align left
.timeline-title
  font-size 14px
  color #009688
.tooltip
  p
    margin 0
</style>
