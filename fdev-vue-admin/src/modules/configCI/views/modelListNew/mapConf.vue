<template>
  <div class="contatiner">
    <fdev-tabs v-model="tabs" align="left">
      <div class="row" v-for="(item, i) in envTypeListAll" :key="i">
        <fdev-tab :name="item.name" :label="item.label" />
      </div>
    </fdev-tabs>
    <fdev-separator class="separator" />
    <div class="content-box">
      <env-card
        v-if="tabs && entityInfo.id"
        :ref="`${tabs}-card`"
        :title="tabs"
        :entity-info="entityInfo"
        :env-all="envAll"
        @refresh="refresh"
        @risk-confirm="riskConfirm"
        :confirm-risk-first="confirmRiskFirst"
      />
    </div>
    <f-dialog
      v-model="riskDialogOpen"
      right
      f-sc
      title="风险确认"
      @before-close="closeDialog"
    >
      <div class="left-container">
        <div class="row items-center text-orange-7 q-mb-md">
          <f-icon
            name="alert_t_f"
            class="icon-size-md"
            style="margin-right:8px;padding:5px;color:#EF6C00"
          />
          对该实体的改动可能会引起以下应用的流水线或配置文件不可用，请确认风险！
        </div>
        <div class="content">
          <!-- 部署依赖列表 -->
          <div v-if="deployDependency.count > 0">
            <fdev-table
              title="部署依赖"
              style="width:100%"
              row-key="pipelineId"
              no-select-cols
              :data="deployTableData"
              :columns="deployColumns"
              :pagination.sync="paginationD"
              @request="onRequestD"
            >
              <!-- 列：应用中文名 -->
              <template v-slot:body-cell-name_zh="props">
                <fdev-td style="max-width:180px">
                  <div
                    class="link text-ellipsis"
                    :title="props.row.name_zh"
                    @click="gotoAppDetail(props.row)"
                    v-if="props.row.name_zh"
                  >
                    {{ props.row.name_zh }}
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ props.row.name_zh }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </div>
                  <div v-else title="-">-</div>
                </fdev-td>
              </template>

              <!-- 列：应用英文名 -->
              <template v-slot:body-cell-name_en="props">
                <fdev-td class="text-ellipsis">
                  <div
                    class="link"
                    :title="props.row.name_en"
                    @click="gotoAppDetail(props.row)"
                    v-if="props.row.name_en"
                  >
                    {{ props.row.name_en }}
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ props.row.name_en }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </div>
                  <div v-else title="-">-</div>
                </fdev-td>
              </template>

              <!-- 列：行内应用负责人 -->
              <template v-slot:body-cell-spdbManagers="props">
                <fdev-td class="text-ellipsis">
                  <div :title="props.row.spdbManagers | spdbManagers">
                    {{ props.row.spdbManagers | spdbManagers }}
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ props.row.spdbManagers | spdbManagers }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </div>
                </fdev-td>
              </template>

              <!-- 列：厂商应用负责人 -->
              <template v-slot:body-cell-devManagers="props">
                <fdev-td class="text-ellipsis">
                  <div :title="props.row.devManagers | devManagers">
                    {{ props.row.devManagers | devManagers }}
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ props.row.devManagers | devManagers }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </div>
                </fdev-td>
              </template>

              <!-- 列：流水线名称 -->
              <template v-slot:body-cell-pipelineName="props">
                <fdev-td class="td-desc">
                  <router-link
                    :to="{
                      path: `/configCI/pipelineDetail/${props.row.pipelineId}`
                    }"
                    class="link"
                    target="_blank"
                    :title="props.row.pipelineName"
                    v-if="props.row.pipelineName"
                    >{{ props.row.pipelineName }}
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ props.row.pipelineName }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </router-link>
                  <div v-else title="-">-</div>
                </fdev-td>
              </template>
            </fdev-table>
          </div>

          <!-- 配置文件依赖列表， 暂时不展示 -->
          <!-- <div v-if="configDependency.count > 0" style="margin-top:32px">
            <fdev-table
              title="配置文件依赖"
              class="my-sticky-column-table"
              style="width:100%"
              row-key="id"
              no-select-cols
              :data="configFilesdeployTableData"
              :columns="configFilesdeployColumns"
              :pagination.sync="paginationC"
              @request="onRequestC"
            >
              <template v-slot:body-cell-nameCN="props">
                <fdev-td style="max-width:180px">
                  <div
                    class="link text-ellipsis"
                    :title="props.row.nameCN"
                    @click="gotoAppDetail(props.row)"
                    v-if="props.row.nameCN"
                  >
                    {{ props.row.nameCN }}
                  </div>
                  <div v-else title="-">-</div>
                </fdev-td>
              </template>

              <template v-slot:body-cell-nameEN="props">
                <fdev-td class="text-ellipsis">
                  <div
                    class="link"
                    :title="props.row.nameEN"
                    @click="gotoAppDetail(props.row)"
                    v-if="props.row.nameEN"
                  >
                    {{ props.row.nameEN }}
                  </div>
                  <div v-else title="-">-</div>
                </fdev-td>
              </template>

              <template v-slot:body-cell-spdbManagers="props">
                <fdev-td class="text-ellipsis">
                  <div :title="props.row.spdbManagers | spdbManagers">
                    {{ props.row.spdbManagers | spdbManagers }}
                  </div>
                </fdev-td>
              </template>

              <template v-slot:body-cell-devManagers="props">
                <fdev-td class="text-ellipsis">
                  <div :title="props.row.devManagers | devManagers">
                    {{ props.row.devManagers | devManagers }}
                  </div>
                </fdev-td>
              </template>

              <template v-slot:body-cell-gitAddress="props">
                <fdev-td class="td-desc">
                  <a
                    :href="props.row.gitAddress"
                    target="_blank"
                    v-if="props.row.gitAddress"
                  >
                    <span :title="props.row.gitAddress">{{
                      props.row.gitAddress
                    }}</span>
                  </a>
                  <div v-else title="-">-</div>
                </fdev-td>
              </template>
            </fdev-table>
          </div> -->

          <!-- 确认复选框 -->
          <div class="confirm">
            <fdev-checkbox
              v-model="confirm"
              label="我已仔细核对以上依赖，并知悉修改实体可能带来的影响，依然选择修改实体"
            />
          </div>
        </div>
      </div>
      <template v-slot:btnSlot
        ><fdev-btn label="取消" dialog outline @click="closeDialog" />
        <fdev-btn label="确定" dialog @click="confirmRisk" />
      </template>
    </f-dialog>
  </div>
</template>

<script>
import envCard from './components/envCard';
import {
  deployColumns
  // configFilesdeployColumns
  // envTypeList
} from '../../utils/constants';
import { mapState, mapActions } from 'vuex';
// :selectObj="envTypeListAll.find(item => item.name === tabs)"
export default {
  name: 'mapConf',
  components: { envCard },
  props: {},
  data() {
    return {
      tabs: '',
      riskDialogOpen: false,
      // paginationC: {
      //   page: 1,
      //   rowsPerPage: 5,
      //   rowsNumber: 0
      // },
      paginationD: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      },
      deployColumns,
      // configFilesdeployColumns,
      confirmRiskFirst: false,
      confirm: false,
      envType: '',
      envTypeListAll: []
    };
  },
  computed: {
    ...mapState('entityManageConfigForm', [
      'entityModelDetail',
      'envList',
      // 'configDependency',
      'deployDependency',
      'currentUser',
      'envTypeList'
    ]),
    dialogOpen: {
      get() {
        return this.value;
      },
      set(val) {
        this.$emit('input', val);
      }
    },

    // 标题的计算值
    computedTitle() {
      return this.entityInfo.nameCn + ' — 映射值';
    },
    entityInfo() {
      return this.entityModelDetail;
    },
    // 全量环境数据
    envAll() {
      return this.envList;
    },
    // paramsC() {
    //   return {
    //     entityNameEn: this.entityInfo.nameEn
    //   };
    // },
    paramsD() {
      return {
        entityNameEn: this.entityInfo.nameEn
      };
    },
    // 配置文件依赖列表数据
    // configFilesdeployTableData() {
    //   return this.configDependency.serviceList
    //     ? this.configDependency.serviceList.slice()
    //     : [];
    // },
    // 部署依赖列表数据
    deployTableData() {
      return this.deployDependency.serviceList
        ? this.deployDependency.serviceList.slice()
        : [];
    }
  },
  filters: {
    spdbManagers(val) {
      return val.length === 0 ? '-' : val.map(item => item.nameCn).join(',');
    },
    devManagers(val) {
      return val.length === 0 ? '-' : val.map(item => item.nameCn).join(',');
    }
  },
  methods: {
    ...mapActions('entityManageConfigForm', [
      'queryEntityModelDetail',
      'deleteEntityClass',
      'queryEnvList',
      // 'queryConfigDependency',
      'queryDeployDependency',
      'queryEnvTypeList'
    ]),
    async beforeShow() {
      await this.queryEnvList();
    },
    refresh() {
      this.$emit('refresh');
    },
    // 删除映射值时，进行风险确认
    riskConfirm(envType) {
      Promise.all([
        this.getDeployDependencyList()
        // this.getConfigFilesDeployList()
      ]).then(() => {
        if (this.deployDependency.count > 0) {
          this.envType = envType;
          this.confirmRiskFirst = true;
          this.riskDialogOpen = true;
        } else {
          this.$refs[`${envType}-card`].deleteMapStep2('break');
        }
      });
    },
    // 分页查询配置文件依赖
    // getConfigFilesDeployList() {
    //   const params = {
    //     page: this.paginationC.page,
    //     perPage: this.paginationC.rowsPerPage,
    //     ...this.paramsC
    //   };
    //   return this.queryConfigDependency(params).then(() => {
    //     this.paginationC.rowsNumber = this.configDependency.count;
    //   });
    // },
    // 分页查询部署依赖
    getDeployDependencyList() {
      const params = {
        page: this.paginationD.page,
        perPage: this.paginationD.rowsPerPage,
        ...this.paramsD
      };
      return this.queryDeployDependency(params).then(() => {
        this.paginationD.rowsNumber = this.deployDependency.count;
      });
    },
    // 分页查询配置文件依赖
    // onRequestC(props) {
    //   const { page, rowsPerPage } = props.pagination;
    //   this.paginationC.page = page;
    //   this.paginationC.rowsPerPage = rowsPerPage;
    //   this.getConfigFilesDeployList();
    // },
    // 分页查询部署依赖
    onRequestD(props) {
      const { page, rowsPerPage } = props.pagination;
      this.paginationD.page = page;
      this.paginationD.rowsPerPage = rowsPerPage;
      this.getDeployDependencyList();
    },
    // 打开新页面查看应用详情
    async gotoAppDetail(row) {
      const url = this.$router.resolve({
        path: `/app/list/${row.id}`
      });
      window.open(url.href, '_blank');
    },
    // 关闭左侧弹窗
    closeDialog() {
      this.beforeClose();
      this.confirm = false;
      this.envType = '';
      this.confirmRiskFirst = false;
      this.riskDialogOpen = false;
    },
    // 确定风险后
    confirmRisk() {
      // 未确认时，提示用户必须勾选
      if (!this.confirm) {
        this.$q.dialog({
          title: '温馨提示',
          message: '请先勾选风险评估！'
        });
      } else {
        this.$refs[`${this.envType}-card`].deleteMapStep2('continue');
        this.closeDialog();
      }
    },
    beforeClose() {
      // this.paginationC = {
      //   page: 1,
      //   rowsPerPage: 5,
      //   rowsNumber: 0
      // };
      this.paginationD = {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      };
    }
  },
  async created() {
    // 查询环境类型列表
    await this.queryEnvTypeList();
    if (this.envTypeList) {
      this.envTypeListAll = this.envTypeList.map(envType => {
        return {
          name: envType.type,
          label: envType.type
        };
      });
      this.tabs = this.envTypeListAll[0].name;
    }
    // this.tabs = envTypeList[0].name;
    // this.envTypeListAll = envTypeList;
  }
};
</script>
<style lang="stylus" scoped>
@import '../../styles/common.styl';

.contatiner
  // position relative
  width 100%
  .content-box
    position relative
    margin-top 16px
.left-container
  width 580px
  .confirm
    position absolute
    bottom 64px
    right 32px
    z-index 100
.td-desc
  max-width 180px
  overflow hidden
  text-overflow ellipsis
.separator
  margin 0
  background-color alpha(#ECEFF1, 1)
</style>
