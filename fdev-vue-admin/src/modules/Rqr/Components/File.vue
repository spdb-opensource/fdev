<template>
  <div>
    <Loading :visible="loading">
      <fdev-table
        :data="tableDoc"
        :columns="colums"
        row-key="id"
        titleIcon="list_s_f"
        title="需规列表"
        noExport
        no-select-cols
      >
        <template v-slot:body-cell-doc_name="props">
          <fdev-td
            v-if="props.row.doc_type == 'confluenceFile'"
            class="td-desc"
            :title="props.row.doc_link"
          >
            {{ props.row.doc_link }}
          </fdev-td>
          <fdev-td
            v-if="props.row.doc_type != 'confluenceFile'"
            class="td-desc"
            :title="props.row.doc_name"
          >
            {{ props.row.doc_name }}
          </fdev-td>
        </template>
        <template v-slot:body-cell-upload_user_all="props">
          <fdev-td class="td-desc" :title="props.value.user_name_cn">
            <router-link
              v-if="props.value"
              class="link"
              :to="`/user/list/${props.value.id}`"
            >
              {{ props.value.user_name_cn }}
            </router-link>
          </fdev-td>
        </template>
        <template v-slot:body-cell-btn="props">
          <fdev-td
            :auto-width="true"
            class="td-padding"
            v-if="props.row.doc_type !== 'confluenceFile'"
          >
            <fdev-btn
              flat
              size="sm"
              @click="exportDemandExcelData(props.row)"
              label="下载"
            />
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
    <FileManger
      @closeWpsFileHandler="closeWpsFileHandler"
      v-model="showDialogOpen"
      :demand_id="demand_id"
      :addFlag="addFlag"
      :isAdd="isAdd"
      @changeFlag="changeFlag"
    />
  </div>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions } from 'vuex';
import { createDemandModel } from '../model';
import FileManger from './FileManger';
// import PageHeaderWrapper from '@/components/PageHeaderWrapper';
// import FileManger from './FileManger';
import { successNotify } from '@/utils/utils';

export default {
  name: 'File',
  components: { Loading, FileManger },
  data() {
    return {
      demand_id: '',
      tableDoc: [],
      isAdd: false,
      addFlag: false,
      loading: false,
      showDialogOpen: false,
      demandModel: createDemandModel(),
      colums: [
        {
          name: 'doc_name',
          label: '需规名称',
          field: 'doc_name'
        },
        {
          name: 'upload_user_all',
          label: '上传人员',
          field: 'upload_user_all'
          // field: row => row.user_name_cn
        },
        {
          name: 'user_group_cn',
          label: '所属小组',
          field: 'user_group_cn'
        },
        {
          name: 'create_time',
          label: '上传日期',
          field: 'create_time'
          // field: field => field.create_time.substr(0, 10)
        },
        {
          name: 'btn',
          align: 'center',
          label: '操作',
          field: 'btn'
        }
      ]
    };
  },
  //上传文件的身份限制
  // props: {
  //   isDemandManager: {
  //     type: Boolean,
  //     default: false
  //   },
  //   isDemandLeader: {
  //     type: Boolean,
  //     default: false
  //   },
  //   isIncludeCurrentUser: {
  //     type: Boolean,
  //     default: false
  //   }
  // },
  computed: {
    ...mapState('demandsForm', ['demandDocList']),
    ...mapState('user', {
      userList: 'userList',
      currentUser: 'currentUser'
    })
  },
  methods: {
    ...mapActions('demandsForm', [
      'queryDemandDoc',
      'exportExcelData',
      'deleteDemandDoc'
    ]),
    changeFlag() {
      this.addFlag = false;
    },

    closeWpsFileHandler(type) {
      if (type === 'close') {
        this.showDialogOpen = false;
        this.init();
      }
    },

    openFileManger() {
      this.showDialogOpen = true;
    },
    //文件下载
    async exportDemandExcelData({ doc_path }) {
      //下载需规
      await this.exportExcelData({
        moduleName: 'fdev-demand',
        path: doc_path
      });
    },
    jumpConfluence(rowval) {
      let url = rowval.doc_link;
      window.open(url, '_blank');
    },
    //文件删除
    deleteDoc(rowval, type) {
      let param = {
        ids: [rowval.id],
        doc_link: [rowval.doc_link]
      };
      this.$q
        .dialog({
          title: `删除确认`,
          message: `确认要删除文件吗？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.deleteDemandDoc(param);
          await this.queryDemandDoc({
            demand_id: this.demand_id,
            doc_type: ''
          });
          successNotify('删除成功');
        });
    },
    async init() {
      //初始化数据
      this.loading = true;
      await this.queryDemandDoc({
        demand_id: this.$route.params.id,
        doc_type: 'demandPlanInstruction'
      });
      this.tableDoc = this.demandDocList;
      this.loading = false;
    }
  },
  mounted() {
    this.demand_id = this.$route.params.id;
    this.init();
  }
};
</script>
<style lang="stylus" scoped>
.td-desc {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
