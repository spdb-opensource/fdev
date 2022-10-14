<template>
  <div>
    <div v-if="databaseList && databaseList.length > 0">
      <div
        class="q-mt-xs q-mb-md"
        v-for="(table, index) in databaseList"
        :key="index"
      >
        <fdev-bar class="table-title q-mb-md">
          <div class="q-table__title">
            <span class="q-mr-lg">介质目录：{{ table.catalog_name }}</span>
            <span>步骤：{{ table.description }}</span>
          </div>
        </fdev-bar>
        <fdev-table
          titleIcon="list_s_f"
          :data="table.implementSql"
          :columns="noteDatabaseSeqNoColumns"
          :pagination.sync="pagination"
          hide-bottom
          class="q-mb-md"
          title="顺序执行脚本"
          no-export
          no-select-cols
        >
          <template v-slot:top-right>
            <fdev-btn
              normal
              ficon="add"
              label="获取顺序执行脚本"
              @click="openDialog(table, '2')"
            />
            <fdev-btn
              normal
              ficon="add"
              label="添加顺序执行脚本"
              @click="openNoTaskDialog(table, '2')"
            />
          </template>

          <template v-slot:body-cell-index="props">
            <fdev-td class="text-ellipsis" :title="props.row.seq_no || '-'">
              {{ props.row.seq_no || '-' }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-fileName="props">
            <fdev-td class="text-ellipsis" :title="props.row.fileName">
              <a
                v-if="props.row.file_type === '1'"
                class="text-primary cursor-pointer"
                target="_blank"
                @click="download(props.row)"
                >{{ props.row.fileName }}</a
              >
              <span v-else>{{ props.row.fileName || '-' }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.fileName || '-' }}
                </fdev-banner>
              </fdev-popup-proxy>
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
              <div class="q-gutter-x-sm">
                <fdev-btn
                  label="上移"
                  @click="move(table.implementSql, props.row.seq_no - 1, -1)"
                  flat
                  v-if="props.row.seq_no && props.row.seq_no !== '1'"
                />
                <div
                  class="text-grey-1 sepreator"
                  v-if="props.row.seq_no && props.row.seq_no !== '1'"
                >
                  |
                </div>
                <fdev-btn
                  label="下移"
                  v-if="
                    props.row.seq_no &&
                      props.row.seq_no != table.implementSql.length
                  "
                  @click="move(table.implementSql, props.row.seq_no - 1, 1)"
                  flat
                />
                <div
                  class="text-grey-1 sepreator"
                  v-if="
                    props.row.seq_no &&
                      props.row.seq_no != table.implementSql.length
                  "
                >
                  |
                </div>
                <fdev-btn label="删除" @click="deleted(props.row.id)" flat />
              </div>
            </fdev-td>
          </template>
        </fdev-table>
        <fdev-table
          titleIcon="list_s_f"
          :data="table.goBackSql"
          :columns="columnsWithIndex"
          :pagination.sync="pagination"
          hide-bottom
          title="回退脚本"
          no-export
          no-select-cols
        >
          <template v-slot:top-right>
            <fdev-btn
              normal
              ficon="add"
              label="获取回退脚本"
              @click="openDialog(table, '3')"
            />
            <fdev-btn
              normal
              ficon="add"
              label="添加回退脚本"
              @click="openNoTaskDialog(table, '3')"
            />
          </template>

          <template v-slot:body-cell-index="props">
            <fdev-td class="text-ellipsis" :title="props.row.seq_no || '-'">
              {{ props.row.seq_no || '-' }}
            </fdev-td>
          </template>

          <template v-slot:body-cell-fileName="props">
            <fdev-td class="text-ellipsis" :title="props.row.fileName">
              <a
                v-if="props.row.file_type === '1'"
                class="text-primary cursor-pointer"
                target="_blank"
                @click="download(props.row)"
                >{{ props.row.fileName }}</a
              >
              <span v-else>{{ props.row.fileName || '-' }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.fileName || '-' }}
                </fdev-banner>
              </fdev-popup-proxy>
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
                <fdev-btn label="删除" @click="deleted(props.row.id)" flat />
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
      :seq_no="seq_no"
      title="获取数据库文件"
      :showProfileAddress="false"
      :showRefreshBtn="false"
      :showBranch="false"
      v-model="showDialog"
      :prod_id="id"
      @click="handleSelectedFiles"
      :release_node_name="releaseNoteDetail.release_node_name"
      :note_id="id"
      :asset_catalog_name="asset_catalog_name"
      :script_type="script_type"
    />
    <f-dialog
      @before-close="clearForm"
      v-model="addfileDialogModel"
      right
      :title="dialogTitle"
    >
      <f-formitem label="脚本名称" required diaS>
        <fdev-input
          v-model="dialogModel.fileName"
          ref="dialogModel.fileName"
          :rules="[val => !!val || '请输入文件名称']"
        />
      </f-formitem>
      <f-formitem label="责任人" required diaS>
        <fdev-select
          v-model="selectUser"
          use-input
          ref="dialogModel.dev_managers_info"
          :options="userOptions"
          option-label="user_name_cn"
          :rules="[val => !!val || '请选择责任人']"
          @filter="filterUser"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label>{{ scope.opt.user_name_en }}</fdev-item-label>
                <fdev-item-label caption>
                  {{ scope.opt.user_name_cn }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn dialog label="取消" outline @click="clearForm" />
        <fdev-btn dialog label="确定" @click="addFile" />
      </template>
    </f-dialog>
  </div>
</template>

<script>
import SearchBranch from './components/searchReleaseNoteBranch';
import { isValidReleaseDate, baseUrl, successNotify } from '@/utils/utils';
import { mapActions, mapState, mapGetters } from 'vuex';
import {
  noteDatabaseColumns,
  noteDatabaseSeqNoColumns
} from '../../utils/constants';
import { releaseNoteNoTaskSqlModel } from '../../utils/model';
export default {
  name: 'AutoReleaseNoteDatabase',
  components: { SearchBranch },
  data() {
    return {
      selectUser: '',
      userOptions: [],
      dialogModel: releaseNoteNoTaskSqlModel(),
      addfileDialogModel: false,
      asset_catalog_name: '',
      script_type: '1',
      id: '',
      seq_no: '',
      url: baseUrl,
      databaseList: [],
      columnsWithIndex: noteDatabaseColumns,
      noteDatabaseSeqNoColumns: noteDatabaseSeqNoColumns,
      pagination: {
        rowsPerPage: 0
      },
      exitSeqNoListLength: 0,
      assetCatalogName: '',
      showDialog: false
    };
  },
  filters: {
    source(val) {
      if (val === '3') return 'fdev生成';
      return val === '1' ? '上传' : 'gitlab选择';
    }
  },
  computed: {
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager',
      userList: 'isLoginUserList'
    }),
    ...mapState('releaseForm', {
      noteSqlList: 'noteSqlList',
      releaseNoteDetail: 'releaseNoteDetail'
    }),
    role() {
      return this.$q.sessionStorage.getItem('releaseRole');
    },
    dialogTitle() {
      return this.script_type === '2' ? '添加顺序执行脚本' : '添加回退脚本';
    },
    columnsOptions() {
      const columns = this.columnsWithIndex.slice();
      const length = columns.length - 2;
      return columns.splice(1).splice(0, length);
    }
  },
  methods: {
    ...mapActions('user', ['fetch']),
    ...mapActions('releaseForm', {
      addNoteSql: 'addNoteSql',
      queryNoteSql: 'queryNoteSql',
      queryDbPath: 'queryDbPath',
      deleteNoteSql: 'deleteNoteSql',
      updateNoteSeqNo: 'updateNoteSeqNo',
      queryNoteDetail: 'queryNoteDetail'
    }),
    compareTime() {
      return isValidReleaseDate(this.$q.sessionStorage.getItem('changeTime'));
    },
    ...mapActions('jobForm', ['downExcel']),
    async openDialog(assets, index) {
      // 获取审核通过的数据库文件路径
      this.asset_catalog_name = assets.catalog_name;
      this.script_type = index;
      if (index === '2') {
        this.seq_no = assets.implementSql.length;
      }
      let release_date = this.releaseNoteDetail.release_node_name;
      await this.queryDbPath({
        release_date: this.format(release_date),
        group_id: this.releaseNoteDetail.owner_groupId
      });
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
    filterUser(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.userOptions = this.userList.filter(
          v =>
            v.user_name_cn.toLowerCase().indexOf(needle) > -1 ||
            v.user_name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    fileNameValidate(val) {
      if (val) {
        const item = val.split('.');
        if (item.length < 2 || item[0] === '') {
          return false;
        }
        if (item[item.length - 1] === 'sh' || item[item.length - 1] === 'sql') {
          return true;
        } else {
          return false;
        }
      }
    },
    clearForm() {
      this.selectUser = '';
      this.dialogModel = releaseNoteNoTaskSqlModel();
      this.addfileDialogModel = false;
    },
    async openNoTaskDialog(assets, index) {
      this.selectUser = this.currentUser;
      this.asset_catalog_name = assets.catalog_name;
      this.script_type = index;
      if (index === '2') {
        this.seq_no = assets.implementSql.length;
      }
      let release_date = this.releaseNoteDetail.release_node_name;
      await this.queryDbPath({
        release_date: this.format(release_date),
        group_id: this.releaseNoteDetail.owner_groupId
      });
      this.addfileDialogModel = true;
    },
    addFile() {
      let formKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('dialogModel') > -1;
      });
      return Promise.all(
        formKeys.map(ele => this.$refs[ele].validate() || Promise.reject(ele))
      ).then(
        async v => {
          const res = {
            release_node_name: this.releaseNoteDetail.release_node_name,
            note_id: this.id,
            asset_catalog_name: this.asset_catalog_name,
            file_type: '2',
            file_url: this.dialogModel.fileName,
            script_type: this.script_type,
            file_principal: this.selectUser.user_name_cn,
            principal_phone: this.selectUser.telephone
          };
          if (this.script_type === '2') {
            res.seq_no = this.seq_no + 1 + '';
          } else {
            res.seq_no = '0';
          }
          const data = {
            list: [res]
          };
          await this.addNoteSql(data);
          successNotify('添加成功');
          this.addDialogModel = false;
          this.clearForm();
          this.init();
        },
        reason => {
          this.$refs[reason].focus();
        }
      );
    },
    async handleSelectedFiles(data) {
      // 弹框中点击“确认”，上传文件
      await this.addNoteSql(data);
      successNotify('添加成功');
      await this.init();
      this.showDialog = false;
    },
    async download(data) {
      let params = {
        path: data.file_url,
        moduleName: 'fdev-release'
      };
      await this.downExcel(params);
    },
    async move(data, index, act) {
      const params = [data[index + act].id, data[index].id];
      await this.updateNoteSeqNo({ ids: params });
      successNotify('移动成功');
      this.init();
    },
    order(data) {
      const table = data.implementSql;
      let exitSeqNoList = table.filter(data => {
        return !!data.seq_no;
      });
      exitSeqNoList = exitSeqNoList.sort((a, b) => {
        return a.seq_no - b.seq_no;
      });
      return { ...data, implementSql: exitSeqNoList };
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
          await this.deleteNoteSql({ id: params });
          successNotify('删除成功');
          await this.init();
        });
    },
    async init() {
      this.id = this.$route.params.id;
      // 变更数据库文件列表查询
      await this.queryNoteSql({
        note_id: this.id,
        catalog_type: 3
      });
      // queryDBAssets 接口返回的值
      // this.databaseList = this.noteSqlList;
      this.databaseList = this.noteSqlList.map(item => {
        return { ...this.order(item) };
      });
      this.queryNoteDetail({ note_id: this.id });
      await this.fetch();
      this.userOptions = this.userList;
    }
  },
  created() {
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.width-select
  min-width 150px
>>> .q-table thead tr th:first-child .q-checkbox
  visibility hidden
.sepreator
  line-height 32px
  display inline-block
.select-gap
  padding-top 16px
  padding-bottom 26px
.table-title
  background #F7F7F7
  height 54px
  font-size 16px
  color #333333
  letter-spacing 0
  line-height 28px
</style>
