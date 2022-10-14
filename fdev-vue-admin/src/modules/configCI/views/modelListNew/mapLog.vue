<!-- 映射值更新日志  -->
<template>
  <div>
    <fdev-tabs v-model="tab" align="left">
      <fdev-tab name="entity" label="实体记录" />
      <fdev-tab name="entityClass" label="映射值记录" />
      <fdev-tab name="dependency" label="依赖分析" />
    </fdev-tabs>
    <fdev-separator class="separator" />
    <div class="container" v-show="tab == 'entity'">
      <fdev-table
        style="width:100%"
        row-key="id"
        :data="tableEntityData"
        :columns="entityColumns"
        :pagination.sync="paginationEntity"
        @request="entityOnRequest"
        noExport
        no-select-cols
      >
        <template v-slot:header="props">
          <fdev-tr :props="props">
            <fdev-th :auto-width="false" style="padding:0" />
            <fdev-th v-for="col in props.cols" :key="col.name" :props="props">
              {{ col.label }}
            </fdev-th>
          </fdev-tr>
        </template>

        <template v-slot:body="props">
          <fdev-tr :props="props">
            <fdev-td :auto-width="true" class="opt-td">
              <f-icon
                :class="{
                  animation1: props.expand ? true : false,
                  animation2: props.expand ? false : true
                }"
                name="arrow_u_o"
                class="icon-size-ld cursor-pointer text-blue-8"
                style="padding:7px"
                @click="showLogDetail(props)"
                v-if="
                  Array.isArray(
                    detailTableEntityData[
                      props.rowIndex -
                        paginationEntity.rowsPerPage *
                          (paginationEntity.page - 1)
                    ]
                  ) &&
                    detailTableEntityData[
                      props.rowIndex -
                        paginationEntity.rowsPerPage *
                          (paginationEntity.page - 1)
                    ].length > 0
                "
              />
            </fdev-td>
            <fdev-td key="updateType" class="text-ellipsis">
              <span :title="props.row.updateType || '-'">{{
                props.row.updateType || '-'
              }}</span>
            </fdev-td>

            <fdev-td key="updateUserName" class="text-ellipsis">
              <span :title="props.row.updateUserName || '-'">{{
                props.row.updateUserName || '-'
              }}</span>
            </fdev-td>

            <fdev-td key="updateTime" class="text-ellipsis">
              <span :title="props.row.updateTime || '-'">{{
                props.row.updateTime || '-'
              }}</span>
            </fdev-td>
          </fdev-tr>

          <fdev-tr v-show="props.expand" :props="props">
            <fdev-td colspan="100%" style="padding:0">
              <fdev-table
                :data="
                  detailTableEntityData[
                    props.rowIndex -
                      paginationEntity.rowsPerPage * (paginationEntity.page - 1)
                  ]
                "
                :columns="entityDetailColumns"
                style="width:100%"
                hide-bottom
                :pagination.sync="paginationEntityEdit"
                noExport
                no-select-cols
              >
                <template v-slot:header="props">
                  <fdev-tr :props="props" style="display:none">
                    <fdev-th
                      :class="{ 'pr-0': !col.label }"
                      v-for="col in props.cols"
                      :key="col.name"
                      :props="props"
                    >
                      {{ col.label }}
                    </fdev-th>
                  </fdev-tr>
                </template>

                <template v-slot:body-cell-opt="">
                  <fdev-td :auto-width="true" class="opt-td"> </fdev-td>
                </template>

                <template v-slot:body-cell-desc="scopes">
                  <fdev-td class="text-ellipsis">
                    <div class="row">
                      <div class="radio" />
                      <div :title="scopes.row.desc || '-'" class="detail">
                        {{ scopes.row.desc || '-' }}
                        <fdev-popup-proxy context-menu>
                          <fdev-banner style="max-width:300px">
                            {{ scopes.row.desc || '-' }}
                          </fdev-banner>
                        </fdev-popup-proxy>
                      </div>
                    </div>
                  </fdev-td>
                </template>
              </fdev-table>
            </fdev-td>
          </fdev-tr>
        </template>
      </fdev-table>
    </div>
    <div class="container" v-show="tab == 'entityClass'">
      <fdev-table
        style="width:100%"
        row-key="id"
        :data="tableData"
        :columns="columns"
        :pagination.sync="pagination"
        @request="onRequest"
        noExport
        no-select-cols
      >
        <template v-slot:top-bottom>
          <f-formitem page label="环境">
            <fdev-select
              v-model="envs"
              :options="environmentList"
              map-options
              emit-value
              option-label="nameEn"
              option-value="nameEn"
              use-input
              multiple
              @filter="envFilter"
              @input="envSelectInput"
            >
              <template v-slot:option="props">
                <fdev-item v-bind="props.itemProps" v-on="props.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label
                      class="select-option-width"
                      :title="props.opt.nameEn"
                    >
                      {{ props.opt.nameEn }}
                    </fdev-item-label>
                    <fdev-item-label
                      caption
                      class="select-option-width"
                      :title="props.opt.nameCn"
                    >
                      {{ props.opt.nameCn }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
        </template>

        <template v-slot:header="props">
          <fdev-tr :props="props">
            <fdev-th :auto-width="false" style="padding:0" />
            <fdev-th v-for="col in props.cols" :key="col.name" :props="props">
              {{ col.label }}
            </fdev-th>
          </fdev-tr>
        </template>

        <template v-slot:body="props">
          <fdev-tr :props="props">
            <fdev-td :auto-width="true" class="opt-td">
              <f-icon
                :class="{
                  animation1: props.expand ? true : false,
                  animation2: props.expand ? false : true
                }"
                name="arrow_u_o"
                class="icon-size-ld cursor-pointer text-blue-8"
                style="padding:7px"
                @click="showLogDetail(props)"
                v-if="
                  Array.isArray(
                    detailTableData[
                      props.rowIndex -
                        pagination.rowsPerPage * (pagination.page - 1)
                    ]
                  ) &&
                    detailTableData[
                      props.rowIndex -
                        pagination.rowsPerPage * (pagination.page - 1)
                    ].length > 0
                "
              />
            </fdev-td>
            <fdev-td key="envName" class="text-ellipsis">
              <span :title="props.row.envName || '-'">{{
                props.row.envName || '-'
              }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.envName || '-' }}
                </fdev-banner>
              </fdev-popup-proxy>
            </fdev-td>

            <fdev-td key="operateType" class="text-ellipsis">
              <span :title="operateTypeMap[props.row.operateType] || '-'">{{
                operateTypeMap[props.row.operateType] || '-'
              }}</span>
            </fdev-td>

            <fdev-td key="updateUserName" class="text-ellipsis">
              <span :title="props.row.updateUserName || '-'">{{
                props.row.updateUserName || '-'
              }}</span>
            </fdev-td>

            <fdev-td key="updateTime" class="text-ellipsis">
              <span :title="props.row.updateTime || '-'">{{
                props.row.updateTime || '-'
              }}</span>
            </fdev-td>
          </fdev-tr>

          <fdev-tr v-show="props.expand" :props="props">
            <fdev-td colspan="100%" style="padding:0">
              <fdev-table
                :data="
                  detailTableData[
                    props.rowIndex -
                      pagination.rowsPerPage * (pagination.page - 1)
                  ]
                "
                :columns="detailColumns"
                class="expand-row"
                style="width:100%"
                hide-bottom
                noExport
                no-select-cols
                :pagination.sync="paginationMapEdit"
              >
                <template v-slot:header="props">
                  <fdev-tr :props="props">
                    <fdev-th
                      :class="{ 'pr-0': !col.label }"
                      v-for="col in props.cols"
                      :key="col.name"
                      :props="props"
                    >
                      {{ col.label }}
                    </fdev-th>
                  </fdev-tr>
                </template>

                <template v-slot:body-cell-opt="">
                  <fdev-td :auto-width="true" class="opt-td"> </fdev-td>
                </template>

                <template v-slot:body-cell-nameEn="scopes">
                  <fdev-td
                    :class="
                      scopes.row.before !== scopes.row.after ? 'text-red' : ''
                    "
                    class="text-ellipsis"
                  >
                    <span :title="scopes.row.nameEn || '-'">{{
                      scopes.row.nameEn || '-'
                    }}</span>
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ scopes.row.nameEn || '-' }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </fdev-td>
                </template>

                <template v-slot:body-cell-nameCn="scopes">
                  <fdev-td
                    :class="
                      scopes.row.before !== scopes.row.after ? 'text-red' : ''
                    "
                    class="text-ellipsis"
                  >
                    <span :title="scopes.row.nameCn || '-'">{{
                      scopes.row.nameCn || '-'
                    }}</span>
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ scopes.row.nameCn || '-' }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </fdev-td>
                </template>

                <template v-slot:body-cell-before="scopes">
                  <fdev-td
                    :class="
                      scopes.row.before !== scopes.row.after ? 'text-red' : ''
                    "
                    class="text-ellipsis"
                  >
                    <span :title="scopes.row.before || '-'">
                      {{ scopes.row.before || '-' }}
                    </span>
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ scopes.row.before || '-' }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </fdev-td>
                </template>

                <template v-slot:body-cell-after="scopes">
                  <fdev-td
                    :class="
                      scopes.row.before !== scopes.row.after ? 'text-red' : ''
                    "
                    class="text-ellipsis"
                  >
                    <span :title="scopes.row.after || '-'">
                      {{ scopes.row.after || '-' }}
                    </span>
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ scopes.row.after || '-' }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </fdev-td>
                </template>
              </fdev-table>
            </fdev-td>
          </fdev-tr>
        </template>
      </fdev-table>
    </div>

    <div class="container dependency-content" v-show="tab == 'dependency'">
      <!-- 部署依赖列表 -->
      <div>
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
          <template v-slot:top>
            <div class="row items-center line-title">
              <f-icon name="basic_msg_s_f" class="titleimg" />
              <span class="titlename">部署依赖</span>
            </div>
          </template>
          <!-- 列：应用中文名 -->
          <template v-slot:body-cell-name_zh="props">
            <fdev-td style="max-width:150px">
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
            <fdev-td>
              <div
                class="link text-ellipsis"
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
            <fdev-td style="white-space:nowrap" class="text-ellipsis">
              <div :title="props.row.spdbManagers | spdbManagers">
                {{ props.row.spdbManagers | spdbManagers }}
              </div>
            </fdev-td>
          </template>

          <!-- 列：厂商应用负责人 -->
          <template v-slot:body-cell-devManagers="props">
            <fdev-td class="text-ellipsis">
              <div :title="props.row.devManagers | devManagers">
                {{ props.row.devManagers | devManagers }}
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
      <!-- <div style="margin-top:16px">
        <fdev-table
          title="配置文件依赖"
          style="width:100%"
          row-key="id"
          no-select-cols
          :data="configFilesdeployTableData"
          :columns="configFilesdeployColumns"
          :pagination.sync="paginationC"
          @request="onRequestC"
        >
          <template v-slot:top>
            <div class="row items-center line-title">
              <f-icon name="basic_msg_s_f" class="titleimg" />
              <span class="titlename">配置文件依赖</span>
            </div>
          </template>

          <template v-slot:body-cell-nameCN="props">
            <fdev-td style="max-width:150px">
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
            <fdev-td style="white-space:nowrap" class="text-ellipsis">
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
    </div>
  </div>
</template>

<script>
import {
  mapLogListColumns,
  mapLogDetailColumns,
  createOperateTypeMap,
  entityLogListColumns,
  entityLogDetailColumns,
  deployColumns
  // configFilesdeployColumns
} from '../../utils/constants';
// import { deepClone } from '@/utils/utils';
import { mapState, mapActions } from 'vuex';

export default {
  name: 'mapLog',
  props: {
    entityInfo: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      envs: [],
      environmentList: [],
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      },
      columns: mapLogListColumns,
      detailColumns: mapLogDetailColumns,
      operateTypeMap: createOperateTypeMap(),
      tab: 'entity',
      paginationEntity: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      },
      paginationEntityEdit: {
        page: 1,
        rowsPerPage: 0
      },
      paginationMapEdit: {
        page: 1,
        rowsPerPage: 0
      },
      entityColumns: entityLogListColumns,
      entityDetailColumns: entityLogDetailColumns,
      deployColumns,
      // configFilesdeployColumns,
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
      copyDeployDependency: []
    };
  },
  computed: {
    ...mapState('entityManageConfigForm', [
      'envList',
      'mapLogList',
      'entitiesLogList',
      // 'configDependency',
      'deployDependency'
    ]),
    dialogOpen: {
      get() {
        return this.value;
      },
      set(val) {
        this.$emit('input', val);
      }
    },
    computedTitle() {
      if (this.entityInfo.nameCn) {
        if (this.tab === 'entity') {
          return this.entityInfo.nameCn + ' —— 实体历史记录';
        } else {
          return this.entityInfo.nameCn + ' —— 映射值历史记录';
        }
      } else {
        return '';
      }
    },
    tableData() {
      if (Object.keys(this.mapLogList).length === 0) {
        return [];
      } else {
        return this.mapLogList.historyList.slice();
      }
    },
    detailTableData() {
      if (this.tableData.length === 0) return [];
      return this.tableData.map(item => {
        const { before, after } = item;
        return item.fields.map(item => {
          const [valueBefore, valueAfter] = [
            before[item.nameEn],
            after[item.nameEn]
          ];
          return {
            ...item,
            before: valueBefore,
            after: valueAfter
          };
        });
      });
    },
    // 实体操作日志数据
    tableEntityData() {
      if (Object.keys(this.entitiesLogList).length === 0) {
        return [];
      } else {
        this.entitiesLogList.entityLogList.forEach((item, index) => {
          item['id'] = new Date(item.updateTime).valueOf();
        });
        return this.entitiesLogList.entityLogList.slice();
      }
    },
    detailTableEntityData() {
      if (this.tableEntityData.length === 0) return [];
      return this.tableEntityData.map(item => {
        const { content } = item;
        return content.map(item => {
          return {
            desc: item
          };
        });
      });
    },
    paramsEntity() {
      return {
        entityId: this.entityInfo.id
      };
    },
    params() {
      return {
        entityId: this.entityInfo.id,
        envNames: this.envs
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
      return this.copyDeployDependency.serviceList
        ? this.copyDeployDependency.serviceList.slice()
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
      'queryEnvList',
      'queryMapLogList',
      'queryEntityLog',
      // 'queryConfigDependency',
      'queryDeployDependency'
    ]),
    closeDialog() {
      this.beforeClose();
      this.dialogOpen = false;
    },
    async getEntityLogList() {
      const params = {
        page: this.paginationEntity.page,
        perPage: this.paginationEntity.rowsPerPage,
        ...this.paramsEntity
      };
      await this.queryEntityLog(params);
      this.paginationEntity.rowsNumber = this.entitiesLogList.count;
    },
    async getMapLogList() {
      const params = {
        page: this.pagination.page,
        perPage: this.pagination.rowsPerPage,
        ...this.params
      };
      await this.queryMapLogList(params);
      this.pagination.rowsNumber = this.mapLogList.count;
    },
    // 环境筛选
    envFilter(val, update, abort) {
      update(() => {
        this.environmentList = this.envList.filter(
          env =>
            env.nameEn.toLowerCase().includes(val.toLowerCase()) ||
            env.nameCn.includes(val)
        );
      });
    },
    onRequest(props) {
      const { page, rowsPerPage } = props.pagination;
      this.pagination.page = page;
      this.pagination.rowsPerPage = rowsPerPage;
      this.getMapLogList();
    },
    entityOnRequest(props) {
      const { page, rowsPerPage } = props.pagination;
      this.paginationEntity.page = page;
      this.paginationEntity.rowsPerPage = rowsPerPage;
      this.getEntityLogList();
    },
    envSelectInput(val) {
      this.getMapLogList();
    },
    showLogDetail(props) {
      props.expand = !props.expand;
    },
    // 分页查询配置文件依赖
    // getConfigFilesDeployList() {
    //   const params = {
    //     page: this.paginationC.page,
    //     perPage: this.paginationC.rowsPerPage,
    //     entityNameEn: this.entityInfo.nameEn,
    //     fields: []
    //     // ...this.paramsC
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
        entityNameEn: this.entityInfo.nameEn,
        fields: []
        // ...this.paramsD
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
    async onRequestD(props) {
      const { page, rowsPerPage } = props.pagination;
      this.paginationD.page = page;
      this.paginationD.rowsPerPage = rowsPerPage;
      await this.getDeployDependencyList();
      this.copyDeployDependency = this.deployDependency;
    },
    // 打开新页面查看应用详情
    async gotoAppDetail(row) {
      const url = this.$router.resolve({
        path: `/app/list/${row.id}`
      });
      window.open(url.href, '_blank');
    }
  },
  async created() {
    // 查询全量环境
    this.getEntityLogList();
    this.queryEnvList();
    this.getMapLogList();
    await this.getDeployDependencyList();
    this.copyDeployDependency = this.deployDependency;
    // this.getConfigFilesDeployList();
  }
};
</script>
<style lang="stylus" scoped>
@import '../../styles/common.styl';
.container
  margin-top 18px
.dependency-content
  >>>.q-table__top
    height: 24px;
    margin-bottom: 20px;
    .line-title
      margin-bottom: 24px;
      height: 24px;
      align-items: center;
      .titleimg
        width: 24px;
        height: 24px;
        color: #0378EA;
        border-radius: 4px;
        border-radius: 4px;
      .titlename
        margin-left: 16px;
        font-size: 14px;
        font-weight: 600;
        color: #333333;
        letter-spacing: 0;
        line-height: 22px;
.select-option-width
  max-width 164px
  overflow hidden
  text-overflow ellipsis
.opt-td
  min-width 80px
  padding-left 10px
  padding-right 10px
.expand-row
  border 1px solid #0378ea
  margin-top 10px
  margin-bottom 10px
  .q-table td
    max-width 170px
.animation1
  vertical-align middle;
  transform-origin center center
  transform rotate(180deg)
  transition transform 0.2s
.animation2
  vertical-align middle;
  transform-origin center center
  transform rotate(90deg)
  transition transform 0.2s
.pr-0
  padding-right 0
.radio {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #1565c0;
  margin-right: 15px;
  margin-top: 7px;
}
.detail
  white-space normal
  flex 1
.top-tabs
  margin-bottom 15px;
.td-desc
  max-width 100px
  white-space nowrap
  overflow hidden
  text-overflow ellipsis
.separator
  margin 0
  background-color alpha(#ECEFF1, 1)
</style>
