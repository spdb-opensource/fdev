<template>
  <f-block block>
    <fdev-table
      :loading="loading"
      :data="orderDocList"
      :columns="columns"
      row-key="id"
      class="my-sticky-column-table"
      no-export
      no-select-cols
      :pagination="{
        sortBy: 'count',
        descending: true,
        page: 1,
        rowsPerPage: 5
      }"
      @request="onRequest"
    >
      <template #top-right>
        <div class="row no-wrap justify-between hidden">
          <!--文件-->
          <f-formitem label="请选择文件">
            <fdev-file
              ref="fileInput"
              v-model="mediaModel"
              square
              outlined
              dense
              @input="handleInput"
            >
              <template v-slot:append>
                <f-icon
                  name="upload"
                  @click="openFileSelectWindow('fileInput')"
                  size="md"
                />
              </template>
            </fdev-file>
          </f-formitem>
        </div>
        <div class="row justify-center pd-none">
          <span class="q-mr-md">
            <fdev-btn
              v-if="orderDocList && orderDocList.length > 0"
              ficon="download"
              label="全部下载"
              normal
              @click="handleExportExcel()"
            >
            </fdev-btn>
          </span>
          <span>
            <fdev-tooltip
              v-if="addButtonFlag === '0' ? false : true"
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
            >
              <span>工单终态下不可上传文件</span>
            </fdev-tooltip>
            <fdev-btn
              class="btn-w"
              normal
              ficon="upload"
              label="上传文档"
              :disable="addButtonFlag === '0' ? false : true"
              @click="uploadFile()"
          /></span>
        </div>
      </template>
      <template v-slot:body-cell-fileName="props">
        <fdev-td class="td-desc" :title="props.row.fileName">
          {{ props.row.fileName }}
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.fileName }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
      </template>
      <template v-slot:body-cell-uploadUserName="props">
        <fdev-td :title="props.row.uploadUserName" class="td-desc">
          <router-link
            v-if="props.value"
            :to="`/user/list/${props.row.uploadUser}`"
            class="link"
          >
            {{ props.row.uploadUserName }}
          </router-link>
        </fdev-td>
      </template>
      <template v-slot:body-cell-operation="props">
        <fdev-td :auto-width="true" class="td-padding ">
          <div class="q-gutter-x-sm row no-wrap items-center">
            <fdev-btn
              flat
              @click="exportDemandExcelData(props.row)"
              label="下载"
            />
            <div class="border"></div>
            <span>
              <fdev-tooltip
                v-if="props.row.deleteButton === '1'"
                anchor="top middle"
                self="center middle"
                :offest="[-20, 0]"
              >
                <span>{{ getErrorMsg(props.row.deleteButton) }}</span>
              </fdev-tooltip>
              <fdev-btn
                flat
                :disable="props.row.deleteButton === '0' ? false : true"
                @click="deleteOrderDoc(props.row)"
                label="删除"
              />
            </span>
          </div>
        </fdev-td>
      </template>
    </fdev-table>

    <f-dialog title="选择上传文件类型" v-model="open">
      <f-formitem
        label="文件类型"
        label-style="width: 112px"
        class="q-mt-lg"
        hint=""
      >
        <fdev-select
          use-input
          v-model="fileModel.filetype"
          :options="filetypeOptions"
          option-label="value"
          option-value="id"
          class="col-5"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="open = false"/>
        <fdev-btn label="确定" dialog @click="sureFun"
      /></template>
    </f-dialog>
  </f-block>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import {
  errorNotify,
  baseUrl,
  successNotify,
  resolveResponseError,
  exportExcel
} from '@/utils/utils';
import { createFileModel } from '@/modules/Network/utils/constants';
import { downloadAll } from '@/modules/Network/services/methods';
import axios from 'axios';

export default {
  name: 'FileManger',
  data() {
    return {
      loading: false,
      showDialogOpen: false,
      jsonCteFoleObjId: '',
      filedata: [],
      orderDocList: [],
      addButtonFlag: true,
      confluenceUrl: '',
      addComf: {},
      columns: [
        {
          name: 'fileName',
          label: '文件名称',
          field: 'fileName'
        },
        {
          name: 'fileTypeName',
          label: '文件类型',
          field: 'fileTypeName'
          // field: 'doc_type',
        },
        {
          name: 'uploadUserName',
          label: '上传人员',
          field: 'uploadUserName'
        },
        {
          name: 'uploadTime',
          label: '上传时间',
          field: 'uploadTime'
          // field: field => field.create_time.substr(0, 10),
        },
        {
          name: 'updateTime',
          label: '修改时间',
          field: 'updateTime'
          // field: field => field.update_time.substr(0, 10),
        },
        {
          name: 'operation',
          align: 'center',
          label: '操作',
          field: 'operation'
        }
      ],
      fileModel: createFileModel(),
      filetypeOptions: [],
      order_id: '',
      mediaModel: [],
      open: false,
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 1
      }
    };
  },
  props: {
    params: {
      type: Object
    }
  },
  watch: {},
  computed: {
    ...mapState('networkForm', ['fileList', 'problemItem']),
    ...mapState('user', ['currentUser'])
  },
  methods: {
    ...mapActions('networkForm', [
      'queryFiles',
      'downloadDoc',
      'uploadDoc',
      'deleteDoc',
      'queryProblemItem'
    ]),
    ...mapActions('user', ['fetch']),
    sureFun() {
      this.open = false;
      this.openFileSelectWindow();
    },
    // 上传按钮
    async uploadFile() {
      this.open = true;
    },
    // 打开资源管理器选择文件
    async openFileSelectWindow() {
      this.$refs.fileInput.pickFiles();
    },
    //删除按钮
    getErrorMsg(row) {
      if (row === '1') {
        return '工单终态下不可删除文件';
      }
    },
    // 选择资源触发上传
    handleInput() {
      this.uploadFun();
    },
    uploadFun() {
      let formData = new FormData();
      formData.append('orderId', this.order_id);
      formData.append('fileType', this.fileModel.filetype.key);
      let fileName = this.mediaModel.name;
      let fileSize = this.mediaModel.size;
      if (fileSize > 0 && fileSize <= 50 * 1024 * 1024) {
        formData.append('files', this.mediaModel);
      } else {
        this.mediaModel = [];
        errorNotify(`上传的${fileName}文件大小不能大于50MB,请重新上传`);
        return;
      }
      let config = {
        headers: {
          'Content-Type': 'multipart/form-data',
          Accept: 'application/json',
          Authorization: this.$q.localStorage.getItem('fdev-vue-admin-jwt')
        }
      };
      axios
        .post(`${baseUrl}fprocesstool/api/file/upload`, formData, config)
        .then(res => {
          this.init();
          if (res.data.code !== 'AAAAAAA') {
            errorNotify('上传失败，请重试！');
          } else {
            successNotify('上传成功！');
          }
          this.mediaModel = [];
        })
        .catch(err => {
          errorNotify('上传失败，请重试！');
          this.mediaModel = [];
        });
    },
    async handleExportExcel() {
      let queryParam = { orderId: this.order_id };
      let res = await resolveResponseError(() => downloadAll(queryParam));
      exportExcel(res);
    },
    //文件下载
    async exportDemandExcelData({ filePath }) {
      //下载需规
      await this.downloadDoc({
        moduleName: 'fdev-process-tool',
        path: filePath
      });
    },
    //文件删除
    deleteOrderDoc(row) {
      let param = {
        id: row.id
      };
      this.$q
        .dialog({
          title: `删除确认`,
          message: `确认要删除文件吗？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.deleteDoc(param);
          this.init();
          successNotify('删除成功');
        });
    },
    jumpConfluence(rowval) {
      let url = rowval.doc_link;
      window.open(url, '_blank');
    },
    onRequest(props) {
      const { page, rowsNumber, rowsPerPage } = props.pagination;
      this.pagination.page = page;
      this.pagination.rowsNumber = rowsNumber;
      this.pagination.rowsPerPage = rowsPerPage;
      this.init();
    },
    async init() {
      try {
        this.loading = true;
        await this.queryFiles({
          orderId: this.order_id
        });

        this.orderDocList = this.fileList.fileList;
        this.addButtonFlag = this.fileList.addButton;
      } catch (e) {
        throw new Error(e);
      } finally {
        this.loading = false;
      }
    }
  },
  async mounted() {
    this.order_id = this.params.id;
    this.init();
    await this.queryProblemItem({ type: 'fileType' });
    this.filetypeOptions = this.problemItem;
  }
};
</script>

<style lang="stylus" scoped>
@import '~#/css/variables.styl';
.row
  padding-bottom 5px
.form
  max-width: 820px;
  margin: 0 auto;
  @media screen and (max-width: $sizes.sm)
    margin-left: -24px;
    margin-right: -24px;
.filetable
  // width 98%
  margin: 0 auto;
.td-desc {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
}
>>> .scroll-normal, .scroll-thin
  overflow auto
.btn-w
  width 108px
.pd-none
  padding 0!important
.f-block
  padding-top 4px
.border
  width 1px
  height 14px
  background #DDD
</style>
