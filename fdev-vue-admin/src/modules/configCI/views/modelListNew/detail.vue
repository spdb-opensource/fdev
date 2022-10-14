<template>
  <div>
    <div class="base">
      <div class="row justify-between base-info">
        <div class="base-title">{{ entityInfo.nameCn }}</div>
        <div class="row q-gutter-x-md" v-if="isManagerRole">
          <fdev-btn ficon="copy" label="复制" dialog @click="copyEntity" />
          <fdev-btn ficon="edit" label="编辑" dialog @click="editEntity" />
          <fdev-btn ficon="delete_o" label="删除" dialog @click="delEntity" />
        </div>
      </div>
      <div class="row">
        <div>
          <f-formitem
            class="item-padding"
            label="实体英文名"
            profile
            :label-style="baseLabelStyle"
            :value-style="valueLabelStyle"
          >
            {{ entityInfo.nameEn }}
          </f-formitem>
          <f-formitem
            class="col-6 item-padding"
            label="创建人"
            profile
            :label-style="baseLabelStyle"
            :value-style="valueLabelStyle"
          >
            {{ entityInfo.createUser ? entityInfo.createUser.nameCn : '' }}
          </f-formitem>
          <f-formitem
            class="col-6 item-padding"
            label="最近更新人"
            profile
            :label-style="baseLabelStyle"
            :value-style="valueLabelStyle"
          >
            {{ entityInfo.updateUser ? entityInfo.updateUser.nameCn : '' }}
          </f-formitem>
        </div>
        <div style="flex:1"></div>
        <div>
          <f-formitem
            class="item-padding"
            label="实体中文名"
            profile
            :label-style="baseLabelStyle"
            :value-style="valueLabelStyle"
          >
            {{ entityInfo.nameCn }}
          </f-formitem>
          <f-formitem
            class="col-6 item-padding"
            label="创建时间"
            profile
            :label-style="baseLabelStyle"
            :value-style="valueLabelStyle"
          >
            {{ entityInfo.createTime }}
          </f-formitem>
          <f-formitem
            class="col-6 item-padding"
            label="最近更新时间"
            profile
            :label-style="baseLabelStyle"
            :value-style="valueLabelStyle"
          >
            {{ entityInfo.updateTime }}
          </f-formitem>
        </div>
        <div style="flex:1"></div>
        <div>
          <f-formitem
            label="实体类型"
            profile
            :label-style="baseLabelStyle"
            :value-style="valueLabelStyle"
          >
            {{ entityInfo.templateName }}
          </f-formitem>
        </div>
      </div>
    </div>
    <div class="flexbetween">
      <div class="property-content scroll-thin-y">
        <fdev-table
          noExport
          title="实体属性"
          titleIcon="basic_msg_s_f"
          :data="propertyList"
          :columns="propertyColumns"
          row-key="id"
          no-export
          no-select-cols
          :rows-per-page-options="[5, 7, 10]"
          :pagination.sync="pagination"
        >
          <template v-slot:top>
            <div class="row items-center line-title">
              <f-icon name="basic_msg_s_f" class="titleimg" />
              <span class="titlename">实体属性</span>
            </div>
          </template>
          <template v-slot:body-cell-required="props">
            <fdev-td :title="props.row.required | required">
              {{ props.row.required | required }}
            </fdev-td>
          </template>
        </fdev-table>
      </div>
      <div class="record-dependency scroll-thin-y">
        <map-log :entity-info="entityInfo" v-if="entityInfo.id" ref="mapLog" />
      </div>
    </div>
    <div class="mapping-content">
      <div class="row items-center line-title line-title-mapping">
        <f-icon name="basic_msg_s_f" class="titleimg" />
        <span class="titlename">映射值</span>
      </div>
      <map-conf
        :entity-info="entityInfo"
        v-if="entityInfo.id"
        @refresh="refresh"
      />
    </div>
    <entity-add
      v-model="entityAddDialogOpen"
      @refresh="refresh"
      :type="entityType"
      :entity-info="entityInfo"
    />
    <f-dialog
      v-model="riskDialogOpen"
      right
      f-sc
      title="风险确认"
      @before-close="beforeClose"
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
              no-select-cols
              row-key="pipelineId"
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
                    target="_blank"
                    :title="props.row.pipelineName"
                    v-if="props.row.pipelineName"
                    class="link"
                    >{{ props.row.pipelineName }}</router-link
                  >
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
                <fdev-td class="td-desc">
                  <div :title="props.row.devManagers | devManagers">
                    {{ props.row.devManagers | devManagers }}
                  </div>
                </fdev-td>
              </template>

              <template v-slot:body-cell-gitAddress="props">
                <fdev-td class="text-ellipsis">
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

      <template v-slot:btnSlot>
        <fdev-btn label="确定" dialog @click="closeDialog" />
      </template>
    </f-dialog>
  </div>
</template>

<script>
import { propertyColumns, tabsList } from '../../utils/constants';
import { mapState, mapActions } from 'vuex';
import { successNotify } from '@/utils/utils';
import { deployColumns } from '../../utils/constants';
import mapLog from './mapLog';
import mapConf from './mapConf';
import entityAdd from './entityAdd';

export default {
  name: 'ModelDetail',
  components: { mapLog, mapConf, entityAdd },
  data() {
    return {
      isManagerRole: false,
      baseLabelStyle:
        'width:144px;font-size: 14px;color: #666666;letter-spacing: 0;line-height: 22px;margin-right:0',
      valueLabelStyle: 'width:161px;',
      entityAddDialogOpen: false, // 新增实体弹窗是否显示
      entityType: 'add', // 默认实体类型为新增
      entityInfo: {},
      propertyColumns, // 实体属性表头
      propertyList: [], // 实体属性表头
      pagination: {
        rowsPerPage: 5
      },
      tabs: 'entityRecord', // 默认tabs
      tabsList,
      riskDialogOpen: false, // 依赖弹窗
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
      }
    };
  },
  computed: {
    ...mapState('entityManageConfigForm', [
      'entityModelDetail',
      // 'configDependency',
      'deployDependency'
    ]),
    ...mapState('user', {
      user: 'currentUser'
    }),
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
    required(val) {
      return val ? '是' : '否';
    }
  },
  methods: {
    ...mapActions('entityManageConfigForm', [
      'queryEntityModelDetail',
      // 'queryConfigDependency',
      'queryDeployDependency',
      'deleteEntity'
    ]),
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent'
    }),
    async getDetail() {
      this.id = this.$route.params.id;
      await this.queryEntityModelDetail({ id: this.id });
      this.entityInfo = this.entityModelDetail;
      this.propertyList = this.entityModelDetail.properties;
      this.$refs['mapLog'] && this.$refs['mapLog'].getEntityLogList();
      this.$refs['mapLog'] && this.$refs['mapLog'].getMapLogList();
    },
    // 复制实体
    copyEntity() {
      this.entityType = 'copy';
      this.entityAddDialogOpen = true;
    },
    // 编辑实体
    editEntity() {
      this.entityType = 'edit';
      this.entityAddDialogOpen = true;
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
    },
    closeDialog() {
      this.beforeClose();
      this.riskDialogOpen = false;
    },
    // 打开新页面查看应用详情
    async gotoAppDetail(row) {
      const url = this.$router.resolve({
        path: `/app/list/${row.id}`
      });
      window.open(url.href, '_blank');
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
    // 删除实体
    delEntity() {
      Promise.all([
        this.getDeployDependencyList()
        // this.getConfigFilesDeployList()
      ]).then(() => {
        if (this.deployDependency.count === 0) {
          this.$q
            .dialog({
              title: '温馨提示',
              message: '您所选择的实体没有应用在使用，可以删除，确认要删除吗？',
              cancel: true
            })
            .onOk(async () => {
              await this.deleteEntity({ id: this.entityInfo.id });
              successNotify('删除实体成功！');
              this.$router.push('/configCI/modelListNew');
            });
          return;
        } else {
          this.riskDialogOpen = true;
        }
      });
    },
    refresh() {
      this.getDetail();
    },
    // 切换tab修改对应组件
    switchFun(v) {
      // this.currentTabComponent = this.tabsList.filter(
      //   e => e.name === v
      // )[0].components;
    }
  },
  async created() {
    await this.fetchCurrent();
    // 环境配置管理员才有权限增删改
    let managerUser = this.user.role.find(item => {
      return item.label === '环境配置管理员';
    });
    if (managerUser) {
      this.isManagerRole = true;
    }
    this.getDetail();
  }
};
</script>

<style lang="stylus" scoped>
@import '../../styles/common.styl';
.base{
  background: #fff;
  padding: 20px 32px 10px 32px;
  border-radius: 8px;
  width: 100%;
  .base-info{
    margin-bottom: 10px;
    .base-title{
      font-size: 20px;
      color: #333333;
      letter-spacing: 0;
      line-height: 20px;
      font-weight: 600;
    }
  }
  .item-padding{
    padding-right: 0px;
  }
}
.flexbetween{
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  .property-content{
    max-height: 553px
    width: 50%;
    background: #fff;
    padding: 20px 32px 11px 32px;
    border-radius: 8px;
    display: inline-block;
    margin-right: 10px;
    >>>.q-table__top{
      margin-bottom: 20px;
      height: 24px;
    }
  }
  .record-dependency{
    max-height: 553px
    width: 50%;
    background: #fff;
    padding: 19px 32px 16px 32px;
    border-radius: 8px;
    display: inline-block;
    >>>.q-tabs__content--align-justify .q-tab{
      flex 0;
    }
  }
}
.line-title
  margin-bottom: 20px;
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
.mapping-content
  width: 100%;
  margin-top: 10px;
  background: #fff;
  padding: 20px 32px 12px 32px;
  border-radius: 8px;
  display: inline-block;
.line-title-mapping
  margin-bottom: 19px;
.left-container
  width 580px
.td-desc
  max-width 180px
  overflow hidden
  text-overflow ellipsis
</style>
