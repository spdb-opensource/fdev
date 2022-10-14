<template>
  <Loading :visible="loading" class="bg-white">
    <div v-if="databaseList && databaseList.length > 0">
      <div
        class="q-mt-xs q-mb-md"
        v-for="(table, index) in databaseList"
        :key="index"
      >
        <fdev-bar class="bg-blue-grey-13 text-white shadow-2 q-mb-md">
          <div class="q-table__title">
            <span class="q-mr-lg">介质目录：{{ table.catalog_name }}</span>
            <span>步骤：{{ table.description }}</span>
          </div>
        </fdev-bar>
        <fdev-table
          titleIcon="list_s_f"
          :data="table.assets"
          :columns="columnsWithIndex"
          :pagination.sync="pagination"
          hide-bottom
          :visible-columns="visibleColumns"
          title="有序执行文件"
          selection="multiple"
          :selected.sync="selected"
          no-export
          no-select-cols
        >
          <template v-slot:top-right>
            <span>
              <fdev-btn
                normal
                label="获取数据库文件"
                :disable="
                  !compareTime() ||
                    !(changesDetail.can_operation || isKaDianManager)
                "
                @click="openDialog(table.assets, index)"
              />
              <fdev-tooltip
                v-if="
                  !compareTime() ||
                    !(changesDetail.can_operation || isKaDianManager)
                "
              >
                <span v-if="!compareTime()">
                  当前变更已过期
                </span>
                <span
                  v-if="
                    compareTime() &&
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                </span>
              </fdev-tooltip>
            </span>
          </template>

          <template v-slot:body-selection="props">
            <span>
              <fdev-checkbox v-model="props.selected" />
              <fdev-tooltip v-if="props.selected">
                若取消勾选则不写入order.txt文件
              </fdev-tooltip>
            </span>
          </template>

          <template v-slot:body-cell-index="props">
            <fdev-td class="text-ellipsis" :title="props.row.seq_no || '-'">
              {{ props.row.seq_no || '-' }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-filename="props">
            <fdev-td class="text-ellipsis" :title="props.row.fileName">
              <a
                class="text-primary"
                target="_blank"
                @click.stop="download(props.row)"
                >{{ props.row.fileName }}</a
              >
            </fdev-td>
          </template>

          <template v-slot:body-cell-source="props">
            <fdev-td :title="props.row.source | source">
              {{ props.row.source | source }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-upload_user="props">
            <fdev-td class="text-ellipsis">
              <router-link
                v-if="props.row.upload_user"
                :title="props.row.upload_username_cn"
                :to="{ path: `/user/list/${props.row.upload_user}` }"
                class="link"
              >
                <span>{{ props.row.upload_username_cn }}</span>
              </router-link>
            </fdev-td>
          </template>

          <template v-slot:body-cell-btn="props">
            <fdev-td auto-width class="cursor-pointer text-primary">
              <div class="q-gutter-md">
                <span v-if="props.row.seq_no && props.row.seq_no !== '1'">
                  <fdev-btn
                    label="上移"
                    @click="move(table.assets, props.row.seq_no - 1, -1)"
                    flat
                    :disable="
                      !compareTime() ||
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  />
                  <fdev-tooltip
                    v-if="
                      !compareTime() ||
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    <span v-if="!compareTime()">
                      当前变更已过期
                    </span>
                    <span
                      v-if="
                        compareTime() &&
                          !(changesDetail.can_operation || isKaDianManager)
                      "
                    >
                      请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                    </span>
                  </fdev-tooltip>
                </span>
                <span
                  v-if="
                    props.row.seq_no &&
                      props.row.seq_no != table.exitSeqNoListLength
                  "
                >
                  <fdev-btn
                    label="下移"
                    @click="move(table.assets, props.row.seq_no - 1, 1)"
                    flat
                    :disable="
                      !compareTime() ||
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  />
                  <fdev-tooltip
                    v-if="
                      !compareTime() ||
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    <span v-if="!compareTime()">
                      当前变更已过期
                    </span>
                    <span
                      v-if="
                        compareTime() &&
                          !(changesDetail.can_operation || isKaDianManager)
                      "
                    >
                      请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                    </span>
                  </fdev-tooltip>
                </span>
                <span>
                  <fdev-btn
                    label="删除"
                    :disable="
                      !compareTime() ||
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                    @click="deleted(props.row.id)"
                    flat
                  />
                  <fdev-tooltip
                    v-if="
                      !compareTime() ||
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    <span v-if="!compareTime()">
                      当前变更已过期
                    </span>
                    <span
                      v-if="
                        compareTime() &&
                          !(changesDetail.can_operation || isKaDianManager)
                      "
                    >
                      请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                    </span>
                  </fdev-tooltip>
                </span>
              </div>
            </fdev-td>
          </template>
        </fdev-table>
        <fdev-separator />
      </div>
    </div>
    <div class="text-center q-mt-md" v-else>
      <f-image name="no_data" />
    </div>
    <SearchBranch
      title="获取数据库文件"
      :showProfileAddress="false"
      :showRefreshBtn="false"
      :showBranch="false"
      v-model="showDialog"
      :prod_id="id"
      @click="handleSelectedFiles"
    />
  </Loading>
</template>

<script>
import SearchBranch from './components/searchBranch';
import Loading from '@/components/Loading';
import { isValidReleaseDate, baseUrl, successNotify } from '@/utils/utils';
import { mapActions, mapState, mapGetters } from 'vuex';
import { databaseUpdateColumns } from '../../utils/constants';
import { getTableCol, setTableCol } from '../../utils/setting';
export default {
  name: 'DatabaseUpdate',
  components: { Loading, SearchBranch },
  data() {
    return {
      loading: false,
      id: '',
      url: baseUrl,
      databaseList: [],
      columnsWithIndex: databaseUpdateColumns,
      pagination: {
        rowsPerPage: 0
      },
      exitSeqNoListLength: 0,
      visibleColumns: [
        'index',
        'filename',
        'upload_user',
        'upload_time',
        'source_application',
        'source',
        'btn'
      ],
      assetCatalogName: '',
      showDialog: false,
      selected: []
    };
  },
  filters: {
    source(val) {
      if (val === '3') return 'fdev生成';
      return val === '1' ? '上传' : 'gitlab选择';
    }
  },
  watch: {
    visibleColumns(val) {
      setTableCol('orderlyList', val);
    },
    async selected(newVal, oldVal) {
      // 找到用户勾选或取消勾选的那条数据
      let targetItem;
      let writeFlag;
      if (newVal.length > oldVal.length) {
        // 勾选操作
        writeFlag = '0';
        targetItem = newVal.filter(
          newItem =>
            !oldVal.find(
              oldItem => JSON.stringify(oldItem) === JSON.stringify(newItem)
            )
        )[0];
      } else if (newVal.length < oldVal.length) {
        // 去除勾选操作
        writeFlag = '1';
        targetItem = oldVal.filter(
          oldItem =>
            !newVal.find(
              newItem => JSON.stringify(newItem) === JSON.stringify(oldItem)
            )
        )[0];
      }
      if (targetItem) {
        await this.whetherWriteOrder({
          write_flag: writeFlag,
          prod_id: this.id,
          filename: targetItem.fileName,
          asset_catalog_name: targetItem.asset_catalog_name
        });
        successNotify('操作成功');
      }
    }
  },
  computed: {
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),
    ...mapState('releaseForm', [
      'database',
      'dbPath',
      'changesDetail',
      'changeApllication'
    ]),
    role() {
      return this.$q.sessionStorage.getItem('releaseRole');
    },
    columnsOptions() {
      const columns = this.columnsWithIndex.slice();
      const length = columns.length - 2;
      return columns.splice(1).splice(0, length);
    }
  },
  methods: {
    ...mapActions('releaseForm', [
      'queryDBAssets',
      'moveOrder',
      'deleteFile',
      'queryDbPath',
      'uploadAssets',
      'queryChangesDetail',
      'getChangeApplications',
      'whetherWriteOrder'
    ]),
    compareTime() {
      return isValidReleaseDate(this.$q.sessionStorage.getItem('changeTime'));
    },
    ...mapActions('jobForm', ['downExcel']),
    async openDialog(assets, index) {
      // 获取审核通过的数据库文件路径
      let release_date = this.changesDetail.release_node_name;
      await this.queryDbPath({
        release_date: this.format(release_date),
        group_id: this.changesDetail.owner_groupId
      });
      this.assetCatalogName = this.databaseList[index].catalog_name;
      this.showDialog = true;
    },
    format(release_date) {
      if (!release_date) return '';
      return (
        release_date.substr(0, 4) +
        '/' +
        release_date.substr(4, 2) +
        '/' +
        release_date.substr(6, 2)
      );
    },
    async handleSelectedFiles(data) {
      // 弹框中点击“确认”，上传文件
      await this.uploadAssets({
        prod_id: this.id,
        asset_catalog_name: this.assetCatalogName,
        filePaths: data
      });
      await this.init();
      this.initSelected();
      this.showDialog = false;
    },
    async download(data) {
      let params = {
        path: data.file_giturl,
        moduleName: 'fdev-release'
      };
      await this.downExcel(params);
    },
    async move(data, index, act) {
      const params = [data[index + act].id, data[index].id];
      await this.moveOrder({ ids: params });
      successNotify('移动成功');
      this.init();
    },
    order(data) {
      const table = data.assets;
      let exitSeqNoList = table.filter(data => {
        return !!data.seq_no;
      });
      exitSeqNoList = exitSeqNoList.sort((a, b) => {
        return a.seq_no - b.seq_no;
      });
      data.exitSeqNoListLength = exitSeqNoList.length;
      let noExitSeqNoList = table.filter(data => {
        return !data.seq_no;
      });
      return { ...data, assets: exitSeqNoList.concat(noExitSeqNoList) };
    },
    deleted(params) {
      this.$q
        .dialog({
          title: '确认删除',
          message: '您确定删除该文件吗?',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteFile({ id: params });
          successNotify('删除成功');
          await this.init();
          this.initSelected();
        });
    },
    async init() {
      this.id = this.$route.params.id;
      this.loading = true;
      // 变更数据库文件列表查询
      await this.queryDBAssets({ prod_id: this.id });
      // queryDBAssets 接口返回的值
      this.databaseList = this.database.map(item => {
        return { ...this.order(item) };
      });
      await this.queryChangesDetail({
        prod_id: this.id
      });
      await this.getChangeApplications({ prod_id: this.id });
      this.loading = false;
    },

    // 根据接口返回的write_flag为0初始化选中
    initSelected() {
      this.databaseList.forEach(table => {
        table.assets.forEach(item => {
          if (item.write_flag && item.write_flag === '0') {
            this.selected.push(item);
          }
        });
      });
    }
  },
  async created() {
    await this.init();
    this.initSelected();
  },
  mounted() {
    const tempVisibleColumns = this.visibleColumns;
    this.visibleColumns = getTableCol('orderlyList');
    if (!this.visibleColumns || this.visibleColumns.length <= 3) {
      this.visibleColumns = tempVisibleColumns;
    }
  }
};
</script>

<style lang="stylus" scoped>
>>> .q-table thead tr th:first-child .q-checkbox
  visibility hidden
</style>
