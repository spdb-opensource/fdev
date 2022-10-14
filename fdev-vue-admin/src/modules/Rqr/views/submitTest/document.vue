<template>
  <Loading>
    <fdev-table
      :loading="loading"
      class="my-sticky-column-table q-mt-md"
      :data="testDocList"
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
          v-if="
            testOrderDetail.file_flag.code === '0' ||
              testOrderDetail.file_flag.code === '2'
          "
          :disable="testOrderDetail.file_flag.code === '2'"
          ficon="upload"
          label="上传文档"
          @click="open = true"
        />
        <fdev-tooltip
          v-if="testOrderDetail.file_flag.code === '2'"
          position="top"
        >
          {{ testOrderDetail.file_flag.msg }}
        </fdev-tooltip>
      </template>
      <template v-slot:body-cell-file_name="props">
        <fdev-td :title="props.row.file_name" class="td-width ellipsis">
          {{ props.row.file_name }}
          <fdev-popup-proxy context-menu>
            <fdev-banner>
              {{ props.row.file_name }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
      </template>
      <!-- 上传人员 -->
      <template v-slot:body-cell-upload_user_info="props">
        <fdev-td
          :title="
            props.row.upload_user_info &&
              props.row.upload_user_info.user_name_cn
          "
          class="td-desc"
        >
          <div v-if="props.row.upload_user_info.id">
            <router-link
              :to="`/user/list/${props.row.upload_user_info.id}`"
              class="link"
            >
              {{
                props.row.upload_user_info &&
                  props.row.upload_user_info.user_name_cn
              }}
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
            <div>
              <fdev-btn
                flat
                @click="deleteDoc(props.row)"
                v-if="
                  testOrderDetail.file_flag.code === '0' ||
                    testOrderDetail.file_flag.code === '2'
                "
                :disable="testOrderDetail.file_flag.code === '2'"
                label="删除"
              />
              <fdev-tooltip
                v-if="testOrderDetail.file_flag.code === '2'"
                position="top"
              >
                {{ testOrderDetail.file_flag.msg }}
              </fdev-tooltip>
            </div>
          </div>
        </fdev-td>
      </template>
    </fdev-table>
    <f-dialog title="选择上传文件类型" v-model="open">
      <f-formitem label="文件类型" class="mt8" hint="">
        <fdev-select
          v-model="filesType"
          :options="fileOptions"
          option-label="label"
          option-value="value"
        >
        </fdev-select>
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="open = false"/>
        <fdev-btn label="确定" dialog @click="sureFun"
      /></template>
    </f-dialog>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { successNotify, errorNotify, baseUrl } from '@/utils/utils';
import { resolveResponseError, exportExcel } from '@/utils/utils';
import {
  uploadTestOrderFile,
  testOrderDownload,
  queryTestOrderFile,
  deleteTestOrderFile
} from '@/modules/Rqr/services/methods';
export default {
  name: 'document',
  props: ['testOrderDetail'],
  data() {
    return {
      mediaModel: [],
      open: false,
      isSonarManager: true,
      loading: false,
      testDocList: [],
      columns: [
        {
          name: 'file_name',
          label: '文件名称',
          field: 'file_name'
        },
        {
          name: 'file_type',
          label: '文件类型',
          field: 'file_type'
        },
        {
          name: 'upload_user_info',
          label: '上传人员',
          field: 'upload_user_info'
        },
        {
          name: 'upload_time',
          label: '上传时间',
          field: 'upload_time'
        },
        {
          name: 'operation',
          align: 'left',
          label: '操作'
        }
      ],
      filesType: {
        label: '需求说明书',
        value: 'demandInstruction'
      },
      fileOptions: [
        {
          label: '需求说明书',
          value: 'demandInstruction'
        },
        {
          label: '需求规格说明书',
          value: 'demandPlanInstruction'
        },
        // {
        //   label: '内测报告',
        //   value: 'innerTestReport'
        // },
        {
          label: '其他相关材料',
          value: 'otherRelatedFile'
        }
      ],
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 1
      },
      testOrderId: this.$route.params.id
    };
  },
  components: {
    Loading
  },
  methods: {
    sureFun() {
      this.open = false;
      this.openFileSelectWindow();
    },
    //文件下载
    async exportExcelData({ id, file_path, file_type }) {
      //下载
      let params = {
        moduleName: 'fdev-demand-testOrder',
        path: file_path
      };
      let res = await resolveResponseError(() => testOrderDownload(params));
      exportExcel(res);
    },
    //删除文件
    deleteDoc({ id }) {
      this.$q
        .dialog({
          title: '删除文件',
          message: `确认删除此文件？`,
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await resolveResponseError(() => deleteTestOrderFile({ ids: [id] }));
          successNotify('删除成功');
          this.init();
          this.mediaModel = [];
        });
    },
    onRequest(props) {
      const { page, rowsNumber, rowsPerPage } = props.pagination;
      this.pagination.page = page;
      this.pagination.rowsNumber = rowsNumber;
      this.pagination.rowsPerPage = rowsPerPage;
      this.init();
    },
    //文件上传
    handleInput() {
      this.uploadFun();
    },
    // 打开资源管理器选择文件
    openFileSelectWindow(refName) {
      this.$refs.fileInput.pickFiles();
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
        ? this.filesType.label
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
      if (fileSize > 0 && fileSize <= 20 * 1024 * 1024) {
        // this.beforeUpload(this.mediaModel, formData);
        formData.append('files', this.mediaModel);
      } else {
        this.mediaModel = [];
        errorNotify('上传的文件大小不能大于20MB');
        return;
      }
      let sameName = false;
      let dataSame = false;
      let messageTip = '';
      let fileType = (typeof this.filesType == 'object'
        ? this.filesType.value
        : this.filesType
      ).toString();
      let fileTypeNa = (typeof this.filesType == 'object'
        ? this.filesType.label
        : this.filesType
      ).toString();
      formData.append('fileType', fileType);
      formData.append('testOrderId', this.testOrderId);
      formData.append('name', fileName);
      if (this.testDocList && this.testDocList.length > 0) {
        this.testDocList.forEach(doc => {
          if (doc.file_type == fileTypeNa && fileName == doc.file_name) {
            sameName = true;
            messageTip = `该提测单下的${fileTypeNa}已存在同名文件，无法继续上传`;
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
            cancel: false,
            persistent: true
          })
          .onOk(() => {
            this.mediaModel = [];
          });
      } else {
        this.uploadFileSend(formData);
      }
    },

    async uploadFileSend(formData) {
      try {
        await resolveResponseError(() => uploadTestOrderFile(formData));
        successNotify('上传成功');
        this.init();
        this.mediaModel = [];
      } catch (error) {
        this.mediaModel = [];
        errorNotify('上传失败，请重试！');
      }
    },
    async handleDocTree() {
      let res = await resolveResponseError(() =>
        queryTestOrderFile({
          id: this.testOrderId
        })
      );
      //总数
      this.pagination.rowsNumber = res && res.length;
      this.testDocList = res;
    },
    init() {
      this.loading = true;
      this.handleDocTree();
      this.loading = false;
    }
  },
  mounted() {
    this.init();
  }
};
</script>
