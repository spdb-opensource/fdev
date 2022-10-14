<template>
  <Loading :visible="loading['releaseForm/queryRqrmntInfoList']">
    <div>
      <div class="shadow-1 bg-white q-my-md q-py-sm">
        <div class="row justify-between">
          <div class="q-ml-md">
            <fdev-btn
              :disable="tableData.length === 0"
              ficon="download"
              flat
              @click="exportExcelSelectOpen()"
            >
              <fdev-tooltip>导出需求列表</fdev-tooltip>
            </fdev-btn>
          </div>
          <div class="main-tip q-mr-md">
            <div>
              整包提测后，该大窗口需求规格说明书文档地址为 :
            </div>
            <div class="text-primary cursor-pointer" @click="routeToDoc">
              {{ rqrmntFileUri }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="tableData.length > 0">
      <fdev-list class="q-mx-sm">
        <fdev-separator />

        <template v-for="(item, i) in filterData">
          <fdev-expansion-item
            :id="item.group_name"
            v-model="item.model"
            :key="item.group_name"
          >
            <template v-slot:header>
              <fdev-item-section>
                <div class="header">
                  {{ item.group_name }}

                  <span v-if="item.release_node_name.length > 0">
                    ：
                  </span>
                  <router-link
                    v-for="node in item.release_node_name"
                    :key="node"
                    :to="{ path: `/release/list/${node}` }"
                    class="link text-teal"
                  >
                    {{ node }}
                  </router-link>
                </div>
              </fdev-item-section>
            </template>

            <fdev-table
              titleIcon="list_s_f"
              hide-bottom
              :data="item.rqrmnt_list"
              :columns="columns"
              title="需求列表"
              :pagination="{ rowsPerPage: 0 }"
            >
              <template v-slot:body-cell-tag="props">
                <fdev-td class="text-ellipsis">
                  <fdev-chip
                    :color="tagColor[props.value]"
                    v-if="props.value"
                    style="margin:0"
                  >
                    {{ props.value }}
                  </fdev-chip>
                </fdev-td>
              </template>

              <template v-slot:body-cell-rqrmnt_no="props">
                <fdev-td class="text-ellipsis">
                  <span
                    v-if="props.value"
                    class="text-blue link cursor-pointer"
                    @click="demandDetails(props.row.id)"
                  >
                    {{ props.value }}
                  </span>
                </fdev-td>
              </template>

              <template v-slot:body-cell-rqrmnt_spdb_manager="props">
                <fdev-td
                  :title="props.value.map(v => v.user_name_cn).join('，')"
                  class="text-ellipsis q-gutter-x-sm"
                >
                  <span v-for="(item, index) in props.value" :key="index">
                    <router-link
                      v-if="item.id"
                      :to="{ path: `/user/list/${item.id}` }"
                      class="link"
                    >
                      <span>{{ item.user_name_cn }}</span>
                    </router-link>
                    <span v-else>{{ item.user_name_en }}</span>
                  </span>
                </fdev-td>
              </template>

              <template v-slot:body-cell-confirmFileDateList="props">
                <fdev-td>
                  <div
                    v-if="props.row.confirmFileDateList"
                    class="q-gutter-x-sm text-ellipsis"
                    :title="props.row.confirmFileDateList.join('，')"
                  >
                    <span v-for="(item, index) in props.value" :key="index">
                      <span>{{ item }}</span>
                    </span>
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        <span v-for="(item, index) in props.value" :key="index">
                          <div>{{ item }}</div>
                        </span>
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </div>
                  <div v-else title="-">-</div>
                </fdev-td>
              </template>

              <template v-slot:body-cell-doc="props">
                <fdev-td
                  class="text-ellipsis"
                  :title="props.value.map(v => v.doc_name).join('，')"
                >
                  <span v-if="props.value.length > 0" class="q-gutter-x-sm">
                    <span
                      v-for="(item, index) in props.value"
                      :key="index"
                      @click="download(item.doc_path)"
                    >
                      <router-link
                        class="link"
                        v-if="item.doc_name"
                        :to="{ path: `#` }"
                      >
                        <span>{{ item.doc_name }}</span>
                      </router-link>
                    </span>
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        <span
                          v-for="(item, index) in props.value"
                          :key="index"
                          @click="download(item.doc_path)"
                        >
                          <router-link
                            class="link"
                            v-if="item.doc_name"
                            :to="{ path: `#` }"
                          >
                            <div>{{ item.doc_name }}</div>
                          </router-link>
                        </span>
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </span>
                  <span v-else>-</span>
                </fdev-td>
              </template>
            </fdev-table>
          </fdev-expansion-item>

          <fdev-separator :key="i" />
        </template>
      </fdev-list>

      <div class="text-center q-gutter-md q-mt-lg" v-if="isReleaseContact()">
        <div class="inline-block">
          <fdev-btn
            label="提交整包测试"
            @click="handleAllTest"
            :disable="!selectedGourpList || selectedGourpList.length === 0"
            :loading="allTestBtnLoading"
          />
          <fdev-tooltip
            v-if="!selectedGourpList || selectedGourpList.length === 0"
          >
            请选择所属小组
          </fdev-tooltip>
        </div>
        <div class="inline-block">
          <fdev-btn
            label="提交准生产测试"
            @click="handleRelTest"
            :disable="!selectedGourpList || selectedGourpList.length === 0"
            :loading="relTestBtnLoading"
          />
          <fdev-tooltip
            v-if="!selectedGourpList || selectedGourpList.length === 0"
          >
            请选择所属小组
          </fdev-tooltip>
        </div>
      </div>
    </div>

    <div class="text-center q-mt-md" v-show="filterData.length === 0">
      <f-image name="no_data" />
    </div>
    <f-dialog right title="详情" v-model="demandDetailsOpen">
      <div style="width:1200px">
        <fdev-table
          titleIcon="list_s_f"
          :data="demandDetailsColumns"
          :columns="detailsColumns"
          title="需求详情列表"
          ref="table"
        >
          <template v-slot:body-cell-task_name="props">
            <fdev-td class="text-ellipsis">
              <router-link
                :to="`/job/list/${props.row.task_id}`"
                class="link"
                :title="props.value"
              >
                {{ props.value }}
              </router-link>
            </fdev-td>
          </template>
          <template v-slot:body-cell-master="props">
            <fdev-td
              :title="props.value.map(v => v.user_name_cn).join(',')"
              class="text-ellipsis q-gutter-x-sm"
            >
              <span v-for="(item, index) in props.value" :key="index">
                <router-link
                  v-if="item.user_name_cn"
                  :to="`/user/list/${item.id}`"
                  class="link"
                  :title="item.user_name_cn"
                >
                  {{ item.user_name_cn }}
                </router-link>
              </span>
            </fdev-td>
          </template>
          <template v-slot:body-cell-project_name="props">
            <fdev-td class="text-ellipsis">
              <router-link
                :title="props.value"
                :to="`/app/list/${props.row.project_id}`"
                class="link"
              >
                {{ props.value }}
              </router-link>
            </fdev-td>
          </template>
          <template v-slot:body-cell-specialCase="props">
            <fdev-td class="text-ellipsis" :title="props.value">
              {{ props.value || '-' }}
            </fdev-td>
          </template>
          <template v-slot:body-cell-other_system="props">
            <fdev-td class="text-ellipsis" :title="props.value">
              {{ props.value || '-' }}
            </fdev-td>
          </template>
        </fdev-table>
      </div>
    </f-dialog>
    <f-dialog title="请选择导出类型" v-model="exportExcelSelect">
      <div class="q-gutter-md">
        <f-formitem required label="类型" diaS>
          <fdev-select
            v-model="fileType"
            :options="fileTypeOptions"
            option-value="value"
            option-label="label"
            map-options
            emit-value
            options-dense
          />
        </f-formitem>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          label="确定"
          :loading="loading[`releaseForm/${importLoading}`]"
          dialog
          @click="exportExcel()"
      /></template>
    </f-dialog>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapGetters } from 'vuex';
import { specialOptions, tagColor, fileTypeOptions } from '../../utils/model';
import { watchRouteParams } from '../../utils/mixin';
import {
  demandDetailListColumns,
  demandListColumns
} from '../../utils/constants';
import { deepClone } from '@/utils/utils';
export default {
  name: 'Demand',
  mixins: [watchRouteParams],
  components: { Loading },
  data() {
    return {
      fileType: '1',
      fileTypeOptions: fileTypeOptions,
      exportExcelSelect: false,
      demandDetailsColumns: [],
      demandDetailsOpen: false,
      isIncludeChildren: true, //是否包含子组
      groupsData: [],
      open: false,
      group: '小组',
      special: '',
      tableData: [],
      filterData: [],
      release_date: '',
      expanded: [],
      tagColor: tagColor,
      allTestBtnLoading: false,
      relTestBtnLoading: false,
      specialOptions: specialOptions,
      detailsColumns: demandDetailListColumns,
      columns: demandListColumns
    };
  },
  computed: {
    ...mapState('userForm', {
      groups: 'groups'
    }),
    ...mapState('global', ['loading']),
    ...mapState('user', ['currentUser']),
    ...mapState('jobForm', ['rqrmntFileUri']),
    ...mapState('releaseForm', [
      'rqrmntInfoList',
      'wpsUrl',
      'queryRqrmntInfo',
      'exportRqrmntInfoFiles',
      'selectedGourpList',
      'demandFilterData'
    ]),
    ...mapGetters('releaseForm', ['isReleaseContact']),
    groupOptions() {
      const groups = this.tableData.map(item => {
        return item.group_name;
      });
      return ['小组', ...groups];
    },
    nodes() {
      const root = this.groups.filter(group => !group.parent);
      const groupList = this.appendNode(
        root,
        this.groups.filter(group => group.id && group.parent)
      );
      return this.addAttribute(groupList);
    },
    importLoading() {
      if (this.fileType === '1') return 'exportSpecialRqrmntInfoList';
      return 'exportRqrmntInfoList';
    }
  },
  watch: {
    nodes(val) {
      this.groupsData = deepClone(val);
    },
    rqrmntInfoList() {
      this.rqrmntInfoList.forEach(item => {
        item.rqrmnt_list.forEach((ele, index) => {
          ele.number = `${index + 1}`;
        });
      });
      this.tableData = this.rqrmntInfoList.map(item => {
        return { ...item, model: true };
      });
      this.filterData = this.tableData;
    }
  },
  methods: {
    ...mapActions('jobForm', ['queryRqrmntFileUri', 'downExcel']),
    ...mapActions('releaseForm', [
      'queryRqrmntInfoList',
      'updateRqrmntInfo',
      'exportRqrmntInfoList',
      'preview',
      'queryRqrmntInfoTasks',
      'exportSpecialRqrmntInfoList'
    ]),
    appendNode(parent, set, depth = 2) {
      if (!Array.isArray(parent) || !Array.isArray(set)) {
        return [];
      }
      if (parent.length === 0 || set.length === 0) {
        return [];
      }
      if (depth === 0) {
        return parent;
      }
      const child = parent.reduce((pre, next) => {
        const nodes = set.filter(group => group.parent === next.id);
        nodes.forEach(node => (node.header = 'nodes'));

        next.children = nodes;
        return pre.concat(nodes);
      }, []);

      if (child.length > 0) {
        this.appendNode(child, set, --depth);
      }
      return parent;
    },
    routeToDoc() {
      window.open(`${this.rqrmntFileUri}`);
    },
    addAttribute(data) {
      if (!Array.isArray(data)) {
        return data;
      }
      return data.map(item => {
        return {
          ...item,
          expand: false,
          selected: false,
          children: this.addAttribute(item.children)
        };
      });
    },
    async demandDetails(rqrmntId) {
      this.demandDetailsOpen = true;
      await this.queryRqrmntInfoTasks({ id: rqrmntId });
      this.demandDetailsColumns = this.queryRqrmntInfo;
    },
    exportExcelSelectOpen() {
      this.exportExcelSelect = true;
    },
    async exportExcel() {
      if (this.fileType === '1') {
        await this.exportSpecialRqrmntInfoList({
          groupIds: this.selectedGourpList,
          release_date: this.release_date,
          type: this.demandFilterData,
          isParent: this.isIncludeChildren
        });
      } else {
        await this.exportRqrmntInfoList({
          groupIds: this.selectedGourpList,
          release_date: this.release_date,
          type: this.demandFilterData,
          isParent: this.isIncludeChildren
        });
      }
      this.exportExcelSelect = false;
    },
    // 下载需规
    async download(path) {
      let param = {
        path: path,
        moduleName: 'fdev-demand'
      };
      await this.downExcel(param);
    },
    async handleAllTest() {
      this.allTestBtnLoading = true;
      await this.handleUpdateRqrmntInfo('1');
      this.allTestBtnLoading = false;
    },
    async handleRelTest() {
      this.relTestBtnLoading = true;
      await this.handleUpdateRqrmntInfo('2');
      this.relTestBtnLoading = false;
    },
    async handleUpdateRqrmntInfo(type) {
      await this.updateRqrmntInfo({
        groupIds: this.selectedGourpList,
        release_date: this.release_date,
        type
      });
      this.init();
    },
    async linkToWps(id) {
      await this.preview({
        file: id
      });
      window.open(this.wpsUrl, '_blank');
    },
    async init() {
      await this.queryRqrmntInfoList({
        groupIds: this.selectedGourpList,
        release_date: this.release_date,
        type: this.demandFilterData,
        isParent: this.isIncludeChildren
      });
      // 后台没给序号，人为添加序号
      this.rqrmntInfoList.forEach(item => {
        item.rqrmnt_list.forEach((ele, index) => {
          ele.number = `${index + 1}`;
        });
      });
      this.tableData = this.rqrmntInfoList.map(item => {
        return { ...item, model: true };
      });
      this.filterData = this.tableData;
    }
  },
  async created() {
    this.release_date = this.$route.params.release_date;
    if (this.selectedGourpList && this.selectedGourpList.length > 0) {
      this.init();
    }
    await this.queryRqrmntFileUri({ release_date: this.release_date });
  }
};
</script>

<style lang="stylus" scoped>
.header
  font-weight 700
  color #616161
.main-tip
  font-size 8px
</style>
