<template>
  <f-block>
    <Loading :visible="globalLoading['jobForm/queryDocDetail']">
      <fdev-table
        :loading="loading"
        class="my-sticky-column-table"
        :data="taskDocList"
        :columns="columns"
        no-export
        no-select-cols
        :pagination.sync="pagination"
        @request="onRequest"
      >
        <template #top-right>
          <div class="no-wrap hidden">
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
          <fdev-btn
            class="btn-w"
            normal
            v-if="isSonarManager"
            ficon="upload"
            label="上传文档"
            @click="uploadFile()"
          />
        </template>
        <template v-slot:body-cell-name="props">
          <fdev-td :title="props.row.name" class="td-width">
            {{ props.row.name }}
            <fdev-popup-proxy context-menu>
              <fdev-banner>
                {{ props.row.name }}
              </fdev-banner>
            </fdev-popup-proxy>
          </fdev-td>
        </template>
        <!-- 上传人员 -->
        <template v-slot:body-cell-user_name_cn="props">
          <fdev-td :title="props.row.user_name_cn" class="td-desc">
            <div v-if="props.row.user_name_cn">
              <router-link :to="`/user/list/${props.row.user_id}`" class="link">
                {{ props.row.user_name_cn }}
              </router-link>
            </div>
            <div v-else>-</div>
          </fdev-td>
        </template>

        <!-- 操作，下载和删除 -->
        <template v-slot:body-cell-operation="props">
          <fdev-td :auto-width="true" class="td-padding ">
            <div class="q-gutter-x-sm row no-wrap items-center">
              <fdev-btn flat @click="exportExcelData(props.row)" label="下载" />
              <div class="border"></div>
              <fdev-btn
                flat
                @click="deleteDoc(props.row)"
                v-if="examDelete(props.row)"
                label="删除"
              />
            </div>
          </fdev-td>
        </template>
      </fdev-table>

      <f-dialog title="选择上传文件类型" v-model="open">
        <f-formitem label="文件类型" class="mt8" hint="">
          <!-- 级联选择器 -->
          <el-cascader
            :options="fileTypeOptions"
            v-model="filesType"
            class="wd300"
          ></el-cascader>
        </f-formitem>
        <template v-slot:btnSlot>
          <fdev-btn label="取消" outline dialog @click="open = false"/>
          <fdev-btn label="确定" dialog @click="sureFun"
        /></template>
      </f-dialog>
    </Loading>
  </f-block>
</template>
<script>
import {
  successNotify,
  errorNotify,
  baseUrl,
  getIdsFormList
} from '@/utils/utils';
import { mapActions, mapState } from 'vuex';
import Loading from '@/components/Loading';
import { fileTypeOptions } from '@/modules/Job/utils/constants';
export default {
  name: 'translation',
  props: {
    taskId: String,
    taskName: String,
    groupName: String
  },
  components: { Loading },
  computed: {
    ...mapState('jobForm', ['docDetail', 'jobProfile']),
    ...mapState('user', ['currentUser']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    isSonarManager() {
      let managerEditIds = getIdsFormList([
        this.jobProfile.spdb_master,
        this.jobProfile.master,
        this.jobProfile.developer,
        this.jobProfile.creator,
        this.jobProfile.tester
      ]);
      return managerEditIds.indexOf(this.currentUser.id) > -1;
    }
  },
  data() {
    return {
      mediaModel: [],
      filesType: '设计类',
      reviewRecordStatus: '',
      fileTypeOptions: fileTypeOptions,
      selectedOptions: [],
      loading: false,
      taskDocList: [],
      open: false,
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 1
      },
      columns: [
        {
          name: 'name',
          label: '文件名称',
          field: 'name'
        },
        {
          name: 'type',
          label: '文件类型',
          field: 'type'
        },
        {
          name: 'user_name_cn',
          label: '上传人员',
          field: 'user_name_cn'
        },
        {
          name: 'updateTime',
          label: '上传时间',
          field: 'updateTime'
        },
        {
          name: 'operation',
          align: 'left',
          label: '操作'
        }
      ]
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    ...mapActions('jobForm', [
      'queryDocDetail',
      'updateTaskDoc',
      'deleteTaskDoc',
      'downExcel'
    ]),
    onRequest(props) {
      const { page, rowsNumber, rowsPerPage } = props.pagination;
      this.pagination.page = page;
      this.pagination.rowsNumber = rowsNumber;
      this.pagination.rowsPerPage = rowsPerPage;
      this.init();
    },
    sureFun() {
      this.open = false;
      this.openFileSelectWindow();
    },
    handleInput() {
      this.uploadFun();
    },
    async uploadFile() {
      this.open = true;
    },
    beforeUpload(file, formData) {
      const port = baseUrl.substr(baseUrl.length - 5, 4);
      const val =
        port === '8080' && process.env.NODE_ENV === 'production'
          ? 'fdev-resources'
          : 'fdev-resources-test';
      let name = encodeURI(this.taskName)
        .substring(0, 10)
        .replace(/%/g, 'a');
      let fileType = (typeof this.filesType == 'object'
        ? this.filesType.slice(-1)
        : this.filesType
      ).toString();
      let files = [];
      files.push(file);
      const datas = files.map(file => {
        const path = `${val}/${this.groupName}/${name}-${
          this.taskId
        }/${fileType}/${file.name}`;
        return {
          path,
          name: file.name,
          taskId: this.taskId,
          type: fileType
        };
      });
      formData.append('datas', JSON.stringify(datas));
      formData.append('taskId', this.taskId);
    },
    //待办文件查询，文件类型控制（重复的覆盖等）
    async uploadFun() {
      let formData = new FormData();
      let fileName = this.mediaModel.name;
      let fileSize = this.mediaModel.size;
      let finalName = fileName.split('.')[1];
      if (fileSize > 0 && fileSize <= 10485760) {
        this.beforeUpload(this.mediaModel, formData);
        formData.append('files', this.mediaModel);
      } else {
        this.mediaModel = [];
        errorNotify('该文件不能被上传或文件大小不被允许');
        return;
      }
      let sameName = false;
      let dataSame = false;
      let messageTip = '';
      let fileType = (typeof this.filesType == 'object'
        ? this.filesType.slice(-1)
        : this.filesType
      ).toString();
      if (this.taskDocList && this.taskDocList.length > 0) {
        this.taskDocList.forEach(doc => {
          if (doc.type == fileType && fileName == doc.name) {
            sameName = true;
            messageTip = '该任务下已存在同名文件，选择继续上传将为您覆盖该文件';
          }
          if (doc.type == fileType && fileType == '审核类-数据库审核材料') {
            sameName = true;
            messageTip =
              '该任务下该类型文件已存在，选择继续上传将为您覆盖该文件';
          }
          if (fileType == '审核类-数据库审核材料' && finalName != 'zip') {
            dataSame = true;
          }
        });
      } else if (fileType == '审核类-数据库审核材料' && finalName != 'zip') {
        dataSame = true;
      }
      if (dataSame) {
        this.$q
          .dialog({
            title: '文件类型错误',
            message: `数据库审核材料请上传"zip"格式文件`,
            cancel: false,
            persistent: true
          })
          .onOk(() => {
            this.mediaModel = [];
          });
      } else if (sameName) {
        this.$q
          .dialog({
            title: '文件已存在',
            message: messageTip,
            cancel: true,
            persistent: true
          })
          .onOk(async () => {
            this.uploadFileSend(formData);
            this.init();
          });
      } else {
        this.uploadFileSend(formData);
      }
    },

    async uploadFileSend(formData) {
      try {
        await this.updateTaskDoc(formData);
        successNotify('上传成功');
        this.init();
        this.mediaModel = [];
      } catch (error) {
        this.mediaModel = [];
        errorNotify('上传失败，请重试！');
      }
    },

    //不能删除的
    examDelete: function() {
      return function(params) {
        if (
          params.type === '审核类-数据库审核材料' &&
          (this.reviewRecordStatus === '初审中' ||
            this.reviewRecordStatus === '复审中')
        ) {
          return false;
        }
        return true;
      };
    },

    //文件下载
    async exportExcelData({ path }) {
      //下载
      await this.downExcel({
        moduleName: 'fdev-task',
        path: path
      });
    },
    // 打开资源管理器选择文件
    openFileSelectWindow(refName) {
      this.$refs.fileInput.pickFiles();
    },
    deleteDoc({ path }) {
      let delFileParam = {
        path: path,
        taskId: this.taskId
      };
      this.$q
        .dialog({
          title: '删除文件',
          message: `确认删除此文件？`,
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteTaskDoc(delFileParam);
          successNotify('删除成功');
          this.init();
          this.mediaModel = [];
        });
    },
    async handleDocTree() {
      await this.queryDocDetail({
        id: this.taskId,
        pageSize: this.pagination.rowsPerPage,
        pageNum: this.pagination.page
      });
      //总数
      this.pagination.rowsNumber = this.docDetail.count;
      this.reviewRecordStatus = this.docDetail.status;
      this.taskDocList = this.docDetail.doc;
    },
    init() {
      this.loading = true;
      this.handleDocTree();
      this.loading = false;
    }
  }
};
</script>
<style lang="stylus" scoped>
.td-width
  max-width 300px
  overflow hidden
  text-overflow ellipsis
.wd300
  width: 300px;
.mt8
  margin-top:8px;
</style>
