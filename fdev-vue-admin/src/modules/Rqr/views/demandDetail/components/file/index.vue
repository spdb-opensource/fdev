<template>
  <f-block block>
    <fdev-table
      :loading="loading"
      :data="demandDocList"
      :columns="columns"
      row-key="id"
      class="my-sticky-column-table"
      no-export
      no-select-cols
      :pagination.sync="pagination"
      @request="onRequest"
    >
      <template #top-right>
        <div class="row no-wrap justify-between hidden">
          <!--文件-->
          <f-formitem
            label="请选择文件"
            v-if="fileModel.filetype.value !== 'confluenceFile'"
          >
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
          <fdev-btn
            class="btn-w"
            normal
            ficon="upload"
            label="上传文档"
            @click="uploadFile()"
          />
        </div>
      </template>
      <template v-slot:body-cell-doc_name="props">
        <fdev-td
          v-if="props.row.doc_type == 'confluenceFile'"
          class="td-desc"
          :title="props.row.doc_link"
        >
          {{ props.row.doc_link }}
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.doc_link }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
        <fdev-td
          v-if="props.row.doc_type != 'confluenceFile'"
          class="td-desc"
          :title="props.row.doc_name"
        >
          {{ props.row.doc_name }}
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.doc_name }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
      </template>
      <template v-slot:body-cell-upload_user_all="props">
        <fdev-td :title="props.value.user_name_cn" class="td-desc">
          <router-link
            v-if="props.value"
            :to="`/user/list/${props.value.id}`"
            class="link"
          >
            {{ props.value.user_name_cn }}
          </router-link>
        </fdev-td>
      </template>
      <template v-slot:body-cell-operation="props">
        <fdev-td
          :auto-width="true"
          class="td-padding "
          v-if="props.row.doc_type !== 'confluenceFile'"
        >
          <div class="q-gutter-x-sm row no-wrap items-center">
            <fdev-btn
              flat
              @click="exportDemandExcelData(props.row)"
              label="下载"
            />
            <div class="border"></div>
            <fdev-btn flat @click="deleteDoc(props.row)" label="删除" />
          </div>
        </fdev-td>
        <fdev-td
          :auto-width="true"
          class="td-padding"
          v-if="props.row.doc_type == 'confluenceFile'"
        >
          <div class="q-gutter-x-sm row no-wrap items-center">
            <fdev-btn flat @click="jumpConfluence(props.row)" label="查看" />
            <div class="border"></div>
            <fdev-btn flat @click="deleteDoc(props.row)" label="删除" />
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
          option-label="label"
          option-value="value"
          class="col-5"
        />
      </f-formitem>
      <!--confluence文档链接-->
      <f-formitem
        label="请选择文件"
        v-if="fileModel.filetype.value == 'confluenceFile'"
        label-style="width: 112px"
        required
      >
        <fdev-input
          ref="confluenceUrl"
          v-model="confluenceUrl"
          placeholder="请输入文件链接"
          :rules="[v => !!v || '请输入文件链接']"
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
import { errorNotify, baseUrl, successNotify } from '@/utils/utils';
import { createFileModel, fileTypes } from '@/modules/Rqr/model';
import axios from 'axios';

export default {
  name: 'FileManger',
  data() {
    return {
      loading: false,
      showDialogOpen: false,
      jsonCteFoleObjId: '',
      filedata: [],
      confluenceUrl: '',
      addComf: {},
      columns: [
        {
          name: 'doc_name',
          label: '文件名称',
          field: 'doc_name'
        },
        {
          name: 'doc_type',
          label: '文件类型',
          field: row => this.docTypeFilter(row.doc_type)
          // field: 'doc_type',
        },
        {
          name: 'upload_user_all',
          label: '上传人员',
          field: 'upload_user_all'
        },
        {
          name: 'create_time',
          label: '创建时间',
          field: 'create_time'
          // field: field => field.create_time.substr(0, 10),
        },
        {
          name: 'update_time',
          label: '修改时间',
          field: 'update_time'
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
      filetypeOptions: fileTypes,
      demand_id: '',
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
    isAdd: {
      type: Boolean
    },
    attachments: {
      type: Array
    },
    addFlag: {
      type: Boolean
    }
  },
  watch: {
    addFlag(val) {
      if (val && Object.keys(this.addComf).length > 0) {
        this.addComf = {};
        this.$emit('changeFlag');
      } else {
        this.addComf = {};
      }
    }
  },
  computed: {
    ...mapState('demandsForm', ['demandDocList', 'demandDocListData']),
    ...mapState('user', ['currentUser'])
  },
  methods: {
    ...mapActions('demandsForm', [
      'queryDemandDoc',
      'exportExcelData',
      'updateDemandDoc',
      'deleteDemandDoc'
    ]),
    ...mapActions('user', ['fetch']),
    sureFun() {
      if (
        this.fileModel.filetype.value == 'confluenceFile' &&
        !this.$refs.confluenceUrl.validate()
      )
        return;
      this.open = false;
      this.openFileSelectWindow();
    },
    // 上传按钮
    async uploadFile() {
      this.open = true;
    },
    // 打开资源管理器选择文件
    async openFileSelectWindow() {
      if (this.fileModel.filetype.value !== 'confluenceFile') {
        this.$refs.fileInput.pickFiles();
      } else {
        this.uploadFun();
      }
    },
    // 选择资源触发上传
    handleInput() {
      this.uploadFun();
    },
    uploadFun() {
      let formData = new FormData();
      formData.append('demand_id', this.demand_id);
      formData.append('user_group_id', this.currentUser.group.id);
      formData.append('user_group_cn', this.currentUser.group.fullName);
      formData.append('doc_type', this.fileModel.filetype.value);
      if (this.fileModel.filetype.value === 'confluenceFile') {
        formData.append('doc_link', this.confluenceUrl);
      } else {
        let fileName = this.mediaModel.name;
        let fileSize = this.mediaModel.size;
        let finalName = fileName.split('.')[1];
        if (
          finalName != 'exe' &&
          finalName != 'dll' &&
          finalName != 'js' &&
          fileSize > 0 &&
          fileSize <= 10485760
        ) {
          formData.append('files', this.mediaModel);
        } else {
          this.mediaModel = [];
          errorNotify('该文件不能被上传或文件大小不被允许');
          return;
        }
      }
      let config = {
        headers: {
          'Content-Type': 'multipart/form-data',
          Accept: 'application/json',
          Authorization: this.$q.localStorage.getItem('fdev-vue-admin-jwt')
        }
      };
      axios
        .post(`${baseUrl}fdemand/api/doc/updateDemandDoc`, formData, config)
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
    docTypeFilter(val) {
      if (val == 'demandInstruction') {
        return (val = '需求说明书');
      } else if (val == 'techPlan') {
        return (val = '技术方案');
      } else if (val == 'demandReviewResult') {
        return (val = '需求评审决议表');
      } else if (val == 'meetMinutes') {
        return (val = '会议纪要');
      } else if (val == 'otherRelatedFile') {
        return (val = '其他相关材料');
      } else if (val == 'confluenceFile') {
        return (val = 'confluence文档');
      } else if (val == 'demandPlanInstruction') {
        return (val = '需求规格说明书');
      } else if (val == 'deferEmail') {
        return (val = '暂缓邮件');
      } else if (val == 'businessTestReport') {
        return (val = '业测报告');
      } else if (val == 'launchConfirm') {
        return (val = '上线确认书');
      } else if (val == 'innerTestReport') {
        return (val = '内测报告');
      } else if (val == 'demandAssessReport') {
        return (val = '需求评估表');
      } else if (val == 'demandPlanConfirm') {
        return (val = '需归确认材料');
      }
    },
    //文件下载
    async exportDemandExcelData({ doc_path }) {
      //下载需规
      await this.exportExcelData({
        moduleName: 'fdev-demand',
        path: doc_path
      });
    },
    //文件删除
    deleteDoc(rowval) {
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
        await this.queryDemandDoc({
          demand_id: this.demand_id,
          doc_type: '',
          size: this.pagination.rowsPerPage,
          index: this.pagination.page
        });
        this.pagination.rowsNumber = this.demandDocListData.count || 0;
      } catch (e) {
        throw new Error(e);
      } finally {
        this.loading = false;
      }
    }
  },
  mounted() {
    this.demand_id = this.$route.params.id;
    this.init();
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
.fdev-block
  padding-top 4px
.border
  width 1px
  height 14px
  background #DDD
</style>
