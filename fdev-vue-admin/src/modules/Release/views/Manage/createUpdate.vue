<template>
  <div>
    <Loading :visible="loading">
      <fdev-table
        title="变更计划列表"
        title-icon="list_s_f"
        :data="tableData"
        :columns="columns"
        row-key="prod_assets_version"
        no-select-cols
        :filter="filterValue"
        :filter-method="filter"
        :pagination.sync="plansPagination"
        no-export
      >
        <template v-slot:top-bottom>
          <slot />
        </template>

        <template v-slot:body="props">
          <fdev-tr :props="props">
            <fdev-td auto-width>
              <fdev-btn
                flat
                :icon="props.expand ? 'expand_more' : 'keyboard_arrow_right'"
                @click="props.expand = !props.expand"
              />
            </fdev-td>
            <fdev-td class="text-ellipsis">
              <span :title="props.row.prod_assets_version">
                {{ props.row.prod_assets_version }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:400px">
                    {{ props.row.prod_assets_version }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </span>
            </fdev-td>
            <fdev-td class="text-ellipsis">
              <span :title="props.row.date">
                {{ props.row.date }}
              </span>
            </fdev-td>
            <fdev-td class="text-ellipsis">
              <span :title="props.row.prod_spdb_no">
                {{ props.row.prod_spdb_no }}
              </span>
            </fdev-td>
            <fdev-td class="text-ellipsis">
              <span :title="props.row.type | typeFilter">
                {{ props.row.type | typeFilter }}
              </span>
            </fdev-td>
            <fdev-td>
              <div class="q-gutter-x-md">
                <div class="inline-block" v-if="props.row.can_operation">
                  <fdev-tooltip v-if="props.row.selected.length === 0">
                    请选择变更版本
                  </fdev-tooltip>
                  <fdev-btn
                    :disable="props.row.selected.length === 0"
                    label="生成模版实例"
                    flat
                    @click="createdTemplate(props.row.selected)"
                  />
                </div>
                <fdev-btn
                  v-if="
                    addChangeTime(props.row.date) &&
                      (role || isKaDianManager) &&
                      !data
                  "
                  label="添加子变更"
                  flat
                  @click="
                    openCreateChanges(
                      props.row.date,
                      props.row.prod_assets_version
                    )
                  "
                />
              </div>
            </fdev-td>
          </fdev-tr>
          <fdev-tr v-show="props.expand" :props="props">
            <fdev-td colspan="100%">
              <fdev-table
                :data="props.row.prod_records"
                :columns="proRecordsColumns"
                :pagination.sync="pagination"
                hide-bottom
                row-key="prod_id"
                selection="multiple"
                class="my-sticky-column-table"
                :selected.sync="props.row.selected"
                @update:selected="selection"
                no-export
                no-select-cols
              >
                <template v-slot:body-cell-version="props">
                  <fdev-td class="text-ellipsis">
                    <router-link
                      :to="{
                        path: `/release/updateDetail/${props.row.prod_id}`
                      }"
                      class="link"
                    >
                      <span :title="props.row.version">
                        {{ props.row.version }}
                        <fdev-popup-proxy context-menu>
                          <fdev-banner style="max-width:400px">
                            {{ props.row.version }}
                          </fdev-banner>
                        </fdev-popup-proxy>
                      </span>
                      <span
                        v-if="props.row.isRisk === '1'"
                        class="text-orange-6"
                        >（含风险）</span
                      >
                    </router-link>
                  </fdev-td>
                </template>

                <template v-slot:body-cell-launcher="props">
                  <fdev-td class="text-ellipsis">
                    <router-link
                      v-if="props.row.launcher_name_cn"
                      :to="{ path: `/user/list/${props.row.launcher}` }"
                      class="link"
                    >
                      <span :title="props.row.launcher_name_cn">
                        {{ props.row.launcher_name_cn }}
                      </span>
                    </router-link>
                  </fdev-td>
                </template>

                <template v-slot:body-cell-status="props">
                  <fdev-td class="text-ellipsis">
                    <span :title="props.row.status | statusFilter">
                      {{ props.row.status | statusFilter }}
                    </span>
                  </fdev-td>
                </template>

                <template v-slot:body-cell-btn="props">
                  <fdev-td>
                    <div class="q-gutter-x-sm row no-wrap">
                      <span>
                        <fdev-btn
                          :disable="
                            props.row.status === '2' ||
                              !!props.row.applications.find(
                                item => item.deploy_type.length === 0
                              )
                          "
                          label="变更介质准备"
                          flat
                          @click="openAssetsDia(props.row)"
                        />
                        <fdev-tooltip
                          v-if="
                            props.row.status === '2' ||
                              props.row.applications.find(
                                item => item.deploy_type.length === 0
                              )
                          "
                        >
                          <span v-if="props.row.status === '2'">
                            介质准备中...
                          </span>
                          <span
                            v-if="
                              props.row.status !== '2' &&
                                props.row.applications.find(
                                  item => item.deploy_type.length === 0
                                )
                            "
                          >
                            应用卡片列表有应用未选择部署平台
                          </span>
                        </fdev-tooltip>
                      </span>
                      <fdev-btn
                        label="介质文件检查"
                        flat
                        @click="handleAssetsCatalogDialogOpen(props.row)"
                      />
                      <fdev-btn
                        label="删除"
                        flat
                        @click="deleted(props.row.prod_id)"
                        v-if="
                          compareTime(props.row.date) && props.row.can_operation
                        "
                      />
                    </div>
                  </fdev-td>
                </template>
              </fdev-table>
            </fdev-td>
          </fdev-tr>
        </template>
      </fdev-table>
    </Loading>
    <AssetsReview
      ref="dialog"
      v-model="assetsDia"
      :data="assetsData"
      :pro_image_uri="pro_image_uri"
      :deployTypeArr="deployTypeArr"
      :awsCommonGroupIsRight="awsCommonGroupIsRight"
      :awsStaticGroupIsRight="awsStaticGroupIsRight"
      v-if="assetsDia"
      @click="auditAChanges"
      :prod_id="prod_id"
      :autoType="autoType"
      :canOperation="canOperation"
      :overdue="overdue"
    />

    <f-dialog v-model="createUpdate" right f-sc title="添加子变更">
      <div>
        <!-- 变更类型 -->
        <f-formitem diaS label="变更类型">
          <fdev-input disable v-model="type" :rules="[() => true]" />
        </f-formitem>

        <!-- 变更日期 -->
        <f-formitem diaS label="变更日期">
          <f-date
            @input="findVersion($event, newChangeModel.plan_time)"
            mask="YYYY/MM/DD"
            :options="optionsFn"
            :disable="dateDisable"
            v-model="newChangeModel.date"
            :rules="[() => true]"
          />
        </f-formitem>

        <!-- 预期发布时间 -->
        <f-formitem diaS label="预期发布时间">
          <fdev-select
            mask="time"
            :hide-dropdown-icon="true"
            v-model="newChangeModel.plan_time"
            :rules="[() => true]"
          >
            <template v-slot:append>
              <fdev-icon name="schedule" class="cursor-pointer">
                <fdev-popup-proxy
                  ref="qTimeProxy"
                  transition-show="scale"
                  transition-hide="scale"
                >
                  <fdev-time
                    @input="findVersion(newChangeModel.date, $event)"
                    v-model="newChangeModel.plan_time"
                  >
                    <template>
                      当天24:00发布请选择第二天00:00
                    </template>
                  </fdev-time>
                </fdev-popup-proxy>
              </fdev-icon>
            </template>
          </fdev-select>
        </f-formitem>

        <!-- 变更版本 -->
        <f-formitem diaS label="变更版本">
          <fdev-input
            disable
            v-model="newChangeModel.version"
            :rules="[() => true]"
          />
        </f-formitem>

        <!-- 变更单号 -->
        <f-formitem diaS label="变更单号">
          <fdev-input
            disable
            v-model="newChangeModel.prod_spdb_no"
            :rules="[() => true]"
          />
        </f-formitem>

        <!-- 是否自动化发布 -->
        <f-formitem diaS label="是否自动化发布">
          <fdev-toggle
            false-value="0"
            true-value="1"
            v-model="newChangeModel.image_deliver_type"
          />
        </f-formitem>

        <!-- 应用系统 -->
        <f-formitem
          diaS
          required
          label="应用系统"
          v-if="newChangeModel.image_deliver_type === ImageDeliverType.auto"
        >
          <fdev-select
            ref="newChangeModel.owner_system_name"
            use-input
            @input="queryExcel"
            @blur="systemTouch(newChangeModel.owner_system_name)"
            :options="systemOptions"
            v-model="$v.newChangeModel.owner_system_name.$model"
            :rules="[
              () =>
                $v.newChangeModel.owner_system_name.required || '请选择应用系统'
            ]"
            @filter="systemFilter"
            option-label="name_cn"
            option-value="id"
          />
        </f-formitem>

        <!-- excel模版 -->
        <f-formitem
          diaS
          required
          label="excel模版"
          v-if="newChangeModel.image_deliver_type === ImageDeliverType.auto"
        >
          <fdev-select
            ref="newChangeModel.excel_template_name"
            use-input
            v-model="$v.newChangeModel.excel_template_name.$model"
            :options="templateOfChange"
            option-label="filename"
            option-value="url"
            @filter="templateFilter"
            :rules="[
              () =>
                $v.newChangeModel.excel_template_name.required ||
                '请选择excel模版'
            ]"
          />
        </f-formitem>

        <!-- 涉及变更应用 -->
        <f-formitem diaS label="涉及变更应用">
          <fdev-tree
            v-if="simple[0].children.length > 0"
            class="release-tree-node"
            :nodes="simple"
            node-key="application_id"
            tick-strategy="leaf"
            label-key="app_name_en"
            :selected.sync="selected"
            :ticked.sync="newChangeModel.applications"
            :expanded.sync="expanded"
            ref="newChangeModel.applications"
          />
          <span v-else>无</span>
        </f-formitem>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          @click="handleCreateUpdate"
          :loading="globalLoading['releaseForm/createAChanges']"
          label="确认"
        />
      </template>
    </f-dialog>

    <f-dialog v-model="confirmSeletedOpen" right f-sc title="确认变更执行顺序">
      <div>
        <Draggable
          :data="selectedData"
          v-model="draggableData"
          label="version"
          :draggable="false"
        />
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          :loading="globalLoading['releaseForm/generateProdExcel']"
          label="确认"
          @click="downloadExcel"
        />
      </template>
    </f-dialog>

    <f-dialog v-model="assetsCatalogOpened" right f-sc title="介质文件检查">
      <div>
        <fdev-tree
          :nodes="assetsCatalog"
          label-key="directory"
          node-key="id"
          default-expand-all
          class="q-mb-lg col"
        >
          <template v-slot:default-header="prop">
            <div class="col-6 text-primary " v-if="prop.node.directory_details">
              <span>{{ prop.node.directory }}</span>
            </div>
            <div v-else>
              {{ prop.node.directory }}
            </div>
          </template>
        </fdev-tree>
      </div>
    </f-dialog>
  </div>
</template>

<script>
import { mapActions, mapState, mapGetters } from 'vuex';
import {
  status,
  product_type,
  newChangeModel,
  batchArr,
  ImageDeliverType
} from '../../utils/model';
import Loading from '@/components/Loading';
import AssetsReview from './components/assetsReview';
import { required } from 'vuelidate/lib/validators';
import {
  isValidReleaseDate,
  successNotify,
  deepClone,
  getGroupFullName,
  errorNotify,
  copyValue,
  validate
} from '@/utils/utils';
import moment from 'moment';
import { changePlanListColumn } from '../../utils/constants';
import { getPlansPagination, setPlansPagination } from '../../utils/setting';
import Draggable from '../ChangesPlans/draggable';

export default {
  name: 'CreateUpdate',
  components: {
    Loading,
    AssetsReview,
    Draggable
  },
  data() {
    return {
      loading: false,
      release_node_name: '', // 投产窗口名
      assetsDia: false,
      assetsData: [],
      prod_id: '',
      autoType: '',
      selectedRowId: '',
      newChangeModel: newChangeModel(),
      pagination: {
        rowsPerPage: 0
      },
      plansPagination: getPlansPagination(),
      templateOfChange: [], // 变更模板列表
      createUpdate: false,
      ImageDeliverType,
      systemOptions: [],
      columns: changePlanListColumn,
      proRecordsColumns: [
        {
          name: 'version',
          label: '变更版本',
          field: 'version',
          sortable: true
        },
        {
          name: 'owner_group_name',
          label: '所属小组',
          field: row => getGroupFullName(this.groups, row.owner_groupId),
          sortable: true,
          copy: true
        },
        {
          name: 'launcher',
          label: '发起人',
          field: 'launcher',
          sortable: true
        },
        {
          name: 'create_time',
          label: '创建时间',
          field: 'create_time',
          sortable: true
        },
        {
          name: 'status',
          label: '状态',
          field: 'status',
          sortable: true
        },
        { name: 'btn', label: '操作' }
      ],
      selected: '应用列表',
      ticked: [], // selected data
      expanded: [],
      type: '',
      tableData: [],
      tooltip: false,
      canOperation: false,
      overdue: false,
      confirmSeletedOpen: false,
      selectedData: [],
      draggableData: [],
      date: '',
      assetsCatalogOpened: false
    };
  },
  validations: {
    newChangeModel: {
      owner_system_name: {
        required
      },
      excel_template_name: {
        required
      }
    }
  },
  props: {
    data: {
      type: Array
    },
    filterValue: {
      type: String
    },
    changes: {
      type: String
    }
  },
  watch: {
    createUpdate(val) {
      if (val === false) {
        this.newChangeModel = newChangeModel();
      } else {
        this.findVersion(
          this.newChangeModel.date,
          this.newChangeModel.plan_time
        );
      }
    },
    data(val) {
      if (val) {
        this.tableData = deepClone(val).map(item => {
          return { ...item, selected: [] };
        });
      } else {
        this.init();
      }
    },
    plansPagination(val) {
      setPlansPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    changesRecordSort(val) {
      this.tableData = val.map(item => {
        return { ...item, selected: [] };
      });
    }
  },
  computed: {
    ...mapState('releaseForm', {
      role: 'role',
      appWithOutSum: 'appWithOutSum',
      version: 'version',
      prodInfo: 'prodInfo',
      releaseDetail: 'releaseDetail',
      file: 'file',
      prodAssets: 'prodAssets',
      applicationTips: 'applicationTips',
      deAutoprodAssets: 'deAutoprodAssets',
      deAutoapplicationTips: 'deAutoapplicationTips',
      assetsCatalog: 'assetsCatalog',
      deleteReleaseInfo: 'deleteReleaseInfo',
      templateSystem: 'templateSystem',
      excelTemplate: 'excelTemplate'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),
    ...mapGetters('releaseForm', ['changesRecordSort']),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('userForm', ['groups']),
    simple() {
      const arr = batchArr().map(item => {
        return {
          application_id: item.batch,
          app_name_en: item.type,
          disabled: true,
          ...item
        };
      });
      const data = this.appWithOutSum.reduce((sum, i) => {
        const batch = i.batch_id ? Number(i.batch_id) : 4;
        sum[batch - 1].children.push(i);
        sum[batch - 1].disabled = false;
        return sum;
      }, arr);
      return [
        {
          app_name_en: '应用列表',
          children: data
        }
      ];
    },
    dateDisable() {
      const releaseDate = moment(this.releaseDetail.release_date)
        .add(3, 'day')
        .format('YYYY/MM/DD');
      return this.date === releaseDate;
    }
  },
  filters: {
    statusFilter(val) {
      return status[val];
    },
    typeFilter(val) {
      return product_type[val];
    }
  },
  methods: {
    ...mapActions('releaseForm', {
      queryAllProdAssets: 'queryAllProdAssets',
      queryDeAutoAllProdAssets: 'queryDeAutoAllProdAssets',
      getChangesRecord: 'getChangesRecord',
      auditChanges: 'auditChanges',
      queryProdInfo: 'queryProdInfo',
      queryVersion: 'queryVersion',
      createAChanges: 'createAChanges',
      queryAppWithOutSum: 'queryAppWithOutSum',
      deleteRelease: 'deleteRelease',
      generateProdExcel: 'generateProdExcel',
      findByProdId: 'findByProdId',
      queryTemplateSystem: 'queryTemplateSystem',
      queryExcelTemplate: 'queryExcelTemplate',
      queryReleaseNodeDetail: 'queryReleaseNodeDetail'
    }),
    // 获取变更记录
    async init() {
      await this.getChangesRecord({
        release_node_name: this.release_node_name
      });
    },
    async auditAChanges(type, id, configFile) {
      this.selectedRowId = id ? id : this.selectedRowId;
      let params = {
        prod_id: this.selectedRowId,
        audit_type: type,
        template_properties: configFile
      };
      await this.auditChanges(params);
      successNotify('开始准备介质，请等待');
      this.$refs.dialog.start();
      if (this.data) {
        this.$emit('click');
      } else {
        this.init();
      }
    },
    systemFilter(val, update) {
      const needle = val.toLowerCase().trim();
      update(() => {
        this.systemOptions = this.templateSystem.filter(item => {
          return (
            item.name_cn.includes(needle) ||
            item.name_en.toLowerCase().includes(needle)
          );
        });
      });
    },
    templateFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase().trim();
        this.templateOfChange = this.excelTemplate.filter(v =>
          v.filename.toLowerCase().includes(needle)
        );
      });
    },
    compareTime(date) {
      return isValidReleaseDate(date);
    },
    async openAssetsDia(item) {
      this.autoType = item.image_deliver_type;
      this.prod_id = item.prod_id;
      this.overdue = this.compareTime(item.date);
      this.canOperation = item.can_operation;
      if (item.image_deliver_type === '1') {
        await this.queryAllProdAssets({ prod_id: this.prod_id });
        if (this.applicationTips) {
          await this.messageBoxOpened();
        }
        this.prodAssets.forEach(item => {
          if (item.catalog_name.includes('scc') || item.catalog_type === '8') {
            if (item.children.length > 1) {
              for (let i = 1; i < item.children.length; i++) {
                if (item.catalog_name.includes('scc')) {
                  item.children[i].pro_image_uri =
                    item.children[i].pro_scc_image_uri;
                  delete item.children[i].pro_scc_image_uri;
                } else {
                  if (
                    item.children[i].deploy_type.includes('SCC') &&
                    item.children[i].deploy_type.length === 1
                  ) {
                    item.children[i].pro_image_uri =
                      item.children[i].pro_scc_image_uri;
                  }
                }
              }
            }
          }
        });
        this.pro_image_uri = [];
        this.deployTypeArr = [];
        this.awsCommonGroupIsRight = true;
        this.awsStaticGroupIsRight = true;
        this.prodAssets.forEach(item => {
          // 对象存储时，获取对象存储用户所属组是否正确
          if (item.catalog_type === '7') {
            if (item.catalog_name === 'AWS_COMMON') {
              this.awsCommonGroupIsRight = item.right_group;
            }
            if (item.catalog_name === 'AWS_STATIC') {
              this.awsStaticGroupIsRight = item.right_group;
            }
          }
          item.children.forEach(v => {
            // 收集应用是否选择部署平台
            if (v.deploy_type) {
              this.deployTypeArr.push(
                v.deploy_type.length === 0 ? false : true
              );
            }

            // 收集应用是否选择镜像标签
            if (v.pro_image_uri !== undefined) {
              this.pro_image_uri.push(
                v.pro_image_uri === null || v.pro_image_uri === ''
                  ? false
                  : v.pro_image_uri
              );
            }
          });
        });
        this.assetsData = this.prodAssets;
      } else {
        await this.queryDeAutoAllProdAssets({ prod_id: this.prod_id });
        if (this.deAutoapplicationTips) {
          await this.messageBoxOpened();
        }
        this.deAutoprodAssets.forEach(item => {
          if (item.catalog_name.includes('scc') || item.catalog_type === '8') {
            if (item.children.length > 1) {
              for (let i = 1; i < item.children.length; i++) {
                if (item.catalog_name.includes('scc')) {
                  item.children[i].pro_image_uri =
                    item.children[i].pro_scc_image_uri;
                  delete item.children[i].pro_scc_image_uri;
                } else {
                  if (
                    item.children[i].deploy_type.includes('SCC') &&
                    item.children[i].deploy_type.length === 1
                  ) {
                    item.children[i].pro_image_uri =
                      item.children[i].pro_scc_image_uri;
                  }
                }
              }
            }
          }
        });
        this.pro_image_uri = [];
        this.deployTypeArr = [];
        this.awsCommonGroupIsRight = true;
        this.awsStaticGroupIsRight = true;
        this.deAutoprodAssets.forEach(item => {
          // 对象存储时，获取对象存储用户所属组是否正确
          if (item.catalog_type === '7') {
            if (item.catalog_name === 'AWS_COMMON') {
              this.awsCommonGroupIsRight = item.right_group;
            }
            if (item.catalog_name === 'AWS_STATIC') {
              this.awsStaticGroupIsRight = item.right_group;
            }
          }
          item.children.forEach(v => {
            // 收集应用是否选择部署平台
            if (v.deploy_type) {
              this.deployTypeArr.push(
                v.deploy_type.length === 0 ? false : true
              );
            }

            // 收集应用是否选择镜像标签
            if (v.pro_image_uri !== undefined) {
              this.pro_image_uri.push(
                v.pro_image_uri === null || v.pro_image_uri === ''
                  ? false
                  : v.pro_image_uri
              );
            }
          });
        });
        this.assetsData = this.deAutoprodAssets;
      }
      this.assetsDia = true;
    },
    // 添加变更
    async handleCreateUpdate() {
      this.newChangeModel.owner_groupId = this.releaseDetail.owner_groupId;
      if (
        this.newChangeModel.image_deliver_type === this.ImageDeliverType.deAuto
      ) {
        this.newChangeModel.excel_template_name = null;
        this.newChangeModel.owner_system_name = null;
      } else {
        this.$v.newChangeModel.$touch();
        let Keys = Object.keys(this.$refs).filter(key => {
          return key.indexOf('newChangeModel') > -1;
        });
        validate(
          Keys.map(key => {
            return this.$refs[key];
          })
        );
        if (this.$v.newChangeModel.$invalid) {
          return;
        }
      }
      let params = {
        ...this.newChangeModel,
        excel_template_url: this.newChangeModel.excel_template_name
          ? this.newChangeModel.excel_template_name.url
          : '',
        owner_system: this.newChangeModel.owner_system_name
          ? this.newChangeModel.owner_system_name.id
          : ''
      };
      await this.createAChanges(params);
      successNotify('添加成功');
      this.createUpdate = false;
      this.newChangeModel = newChangeModel();
      this.init();
    },
    /* 打开添加变更弹窗 */
    async openCreateChanges(date, prod_assets_version) {
      this.createUpdate = true;
      /* 查询应用系统 */
      this.newChangeModel.release_node_name = this.release_node_name;
      this.date = this.newChangeModel.date = date;
      this.newChangeModel.prod_assets_version = prod_assets_version;
      /* 查询变更应用 */
      await this.queryAppWithOutSum({
        prod_assets_version: prod_assets_version,
        release_node_name: this.release_node_name
      });
      /* 根据总介质版本查询其他信息 */
      await this.queryProdInfo({ prod_assets_version: prod_assets_version });
      this.newChangeModel = {
        ...this.newChangeModel,
        ...this.prodInfo
      };
      this.type = product_type[this.prodInfo.type];

      // 查询应用系统
      await this.queryTemplateSystem({
        template_type: this.prodInfo.type,
        owner_group: this.releaseDetail.owner_groupId
      });
      this.systemOptions = this.templateSystem;

      this.templateSystem.filter(val => {
        if (val.id === this.prodInfo.owner_system) {
          //赋值--应用系统
          this.newChangeModel.owner_system_name = val;
        }
      });
      if (this.prodInfo.owner_system !== null) {
        await this.queryExcel();
      }
    },

    async queryExcel() {
      await this.queryExcelTemplate({
        sysname_cn: this.newChangeModel.owner_system_name.name_cn,
        template_type: this.newChangeModel.type
      });
      this.templateOfChange = this.excelTemplate;
      this.excelTemplate.filter(val => {
        if (val.filename === this.prodInfo.excel_template_name) {
          //赋值--excel模版
          this.newChangeModel.excel_template_name = val;
        }
      });
    },

    systemTouch(data) {
      this.newChangeModel.excel_template_name = null;
    },
    /* 删除变更 */
    deleted(id) {
      this.$q
        .dialog({
          title: '删除变更',
          message: `删除变更后，与之相关联的变更应用及变更文件都会统一删除，是否确认删除？`,
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteRelease({ prod_id: id });
          if (JSON.stringify(this.deleteReleaseInfo) !== '{}') {
            successNotify(`${this.deleteReleaseInfo}`);
          } else {
            successNotify('删除成功');
          }
          if (this.data) {
            this.$emit('click');
          } else {
            this.init();
          }
        });
    },
    // 查询变更版本
    async findVersion(date, time) {
      if (!date || !time) {
        return;
      }
      const params = {
        release_node_name: this.release_node_name,
        date: this.newChangeModel.date,
        group_id: this.currentUser.group_id,
        plan_time: this.newChangeModel.plan_time
      };
      await this.queryVersion(params);
      this.newChangeModel.version = this.version;
    },
    filter(rows, terms, cols, getCellValue) {
      let lowerTerms = terms
        .toLowerCase()
        .split(',')
        .filter(item => {
          return item !== 'table' && item !== '';
        });
      return rows.filter(row => {
        let isChanges = true;

        cols.some(col => {
          if (col.name === 'type' && this.changes !== '全部') {
            const changes = this.changes === '生产' ? 'proc' : 'gray';
            isChanges = row.type === changes;
          }
        });
        return (
          isChanges &&
          lowerTerms.every(term => {
            if (term === '') {
              return true;
            }
            let col = cols.some(col => {
              const test =
                (getCellValue(col, row) + '')
                  .toLowerCase()
                  .indexOf(term.trim()) > -1;
              return test;
            });
            return col;
          })
        );
      });
    },
    selection(selected) {
      selected = selected.filter(row => {
        let isDeAuto = row.image_deliver_type === '0';
        if (isDeAuto) {
          errorNotify('非自动化发布无模板生成');
        }
        return !isDeAuto;
      });
    },
    /* 生成变更模板 */
    createdTemplate(item) {
      if (item.length === 0) {
        return;
      }
      this.confirmSeletedOpen = true;
      this.draggableData = this.selectedData = item.sort((a, b) => {
        const aDate = a.version
          .split('_')
          .reverse()
          .slice(0, 2)
          .reverse()
          .join('');
        const bDate = b.version
          .split('_')
          .reverse()
          .slice(0, 2)
          .reverse()
          .join('');
        return aDate - bDate;
      });
    },
    async downloadExcel() {
      await this.generateProdExcel({
        prod_ids: this.draggableData.map(item => {
          return item.prod_id;
        })
      });
      this.confirmSeletedOpen = false;
      this.$q
        .dialog({
          title: '生成excel模板成功',
          message: `<span style="word-break: break-all">${this.file}</span>`,
          ok: '复制目录',
          cancel: true,
          persistent: true,
          html: true
        })
        .onOk(() => {
          const index = this.file.indexOf('为：') + 2;
          const path = this.file.substring(index);
          copyValue(path);
          successNotify('复制成功!');
        })
        .onDismiss(() => {
          this.draggableData = [];
        });
    },
    // 添加子变更时间控制
    addChangeTime(time) {
      time = moment(time);
      const today = moment(new Date());
      return time.diff(today, 'day') > -1;
    },
    /* 可选日期控制：当前日期-投产窗口日期+3 */
    optionsFn(date) {
      const nextDay = moment(this.date)
        .add(1, 'day')
        .format('YYYY/MM/DD');
      const preDay = this.date;
      return preDay <= date && date <= nextDay;
    },
    messageBoxOpened() {
      return new Promise((resolve, reject) => {
        this.$q
          .dialog({
            title: `<span style="font-size: 16px">
              ${
                this.autoType === '1'
                  ? this.applicationTips
                  : this.deAutoapplicationTips
              }</span>`,
            html: true,
            ok: '我知道了',
            cancle: true
          })
          .onOk(() => {
            resolve();
          });
      });
    },
    async handleAssetsCatalogDialogOpen({ prod_id }) {
      await this.findByProdId({
        prod_id
      });
      if (this.assetsCatalog.length === 0) {
        errorNotify('暂无介质文件');
        return;
      }
      this.assetsCatalogOpened = true;
    },
    confirmToClose() {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this.createUpdate = false;
        });
    }
  },
  async created() {
    this.release_node_name = this.$route.params.id;
    if (!this.data) {
      await this.queryReleaseNodeDetail({
        release_node_name: this.release_node_name
      });
      this.init();
    }
  }
};
</script>

<style lang="stylus" scoped></style>
