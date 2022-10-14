<template>
  <f-dialog
    :value="value"
    @hide="hideDialog"
    title="需求文档管理"
    right
    @before-close="hideDialog"
  >
    <div class="filetable rdia-dc-w">
      <div class="text-h6 q-mb-md">文件上传</div>
      <div class="row no-wrap justify-between">
        <f-formitem label="文件类型">
          <fdev-select
            use-input
            v-model="fileModel.filetype"
            :options="filetypeOptions"
            option-label="label"
            option-value="value"
            class="col-5"
          />
        </f-formitem>
        <!--文件-->
        <f-formitem
          label="请选择文件"
          v-if="fileModel.filetype.value !== 'confluenceFile'"
        >
          <fdev-file ref="fileInput" v-model="mediaModel" square outlined dense>
            <template v-slot:append>
              <f-icon
                name="upload"
                @click="openFileSelectWindow('fileInput')"
                size="md"
              />
            </template>
          </fdev-file>
        </f-formitem>
        <!--confluence文档链接-->
        <f-formitem
          label="请选择文件"
          v-if="fileModel.filetype.value == 'confluenceFile'"
        >
          <fdev-input v-model="confluenceUrl" placeholder="请输入文件链接" />
        </f-formitem>
      </div>
      <div class="row justify-center q-mt-md q-mb-sm">
        <fdev-btn
          :label="
            fileModel.filetype.value === 'confluenceFile' ? '提交' : '上传'
          "
          @click="uploadFile()"
        />
      </div>

      <fdev-table
        :data="demandDocList"
        :columns="columns"
        row-key="id"
        class="my-sticky-column-table"
        titleIcon="list_s_f"
        title="需求文档列表"
        no-export
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
            <div class="q-gutter-x-sm row no-wrap">
              <fdev-btn
                flat
                @click="exportDemandExcelData(props.row)"
                label="下载"
              />
              <fdev-btn
                class="text-negative"
                flat
                @click="deleteDoc(props.row)"
                label="删除"
              />
            </div>
          </fdev-td>
          <fdev-td
            :auto-width="true"
            class="td-padding"
            v-if="props.row.doc_type == 'confluenceFile'"
          >
            <div class="q-gutter-x-sm row no-wrap">
              <fdev-btn flat @click="jumpConfluence(props.row)" label="查看" />
              <fdev-btn
                flat
                @click="deleteDoc(props.row)"
                class="text-negative"
                label="删除"
              />
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </div>
  </f-dialog>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { errorNotify, baseUrl, successNotify } from '@/utils/utils';
import { createFileModel, fileTypes } from '../model';
import axios from 'axios';

// import {
//   createFolder,
//   queryFileList,
//   queryDownloadLink,
//   queryPreviewLink,
//   queryEditLink,
//   deleteFile
// } from '@/services/wpsfile';
// import LocalStorage from '#/plugins/LocalStorage';

export default {
  name: 'FileManger',
  data() {
    return {
      loading: 'false',
      showDialogOpen: false,
      jsonCteFoleObjId: '',
      filedata: [],
      confluenceUrl: '',
      addComf: {},
      columns: [
        {
          name: 'doc_name',
          align: 'left',
          label: '文件名称',
          field: 'doc_name',
          sortable: true
        },
        {
          name: 'doc_type',
          align: 'left',
          label: '文件类型',
          field: row => this.docTypeFilter(row.doc_type),
          // field: 'doc_type',
          sortable: true
        },
        {
          name: 'upload_user_all',
          align: 'left',
          label: '上传人员',
          field: 'upload_user_all',
          sortable: true
        },
        // {
        //   name: 'number',
        //   align: 'left',
        //   label: '文件版本',
        //   field: 'number',
        //   sortable: true
        // },
        {
          name: 'create_time',
          align: 'left',
          label: '创建时间',
          field: 'create_time',
          // field: field => field.create_time.substr(0, 10),
          sortable: true
        },
        {
          name: 'update_time',
          align: 'left',
          label: '修改时间',
          field: 'update_time',
          // field: field => field.update_time.substr(0, 10),
          sortable: true
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
      mediaModel: []
    };
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
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
    value(val) {
      if (val) {
        this.init();
      }
    },
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
    ...mapState('demandsForm', ['demandDocList']),
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
    //上传文件
    async uploadFile(event) {
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
        .then(async res => {
          await this.queryDemandDoc({
            demand_id: this.demand_id,
            doc_type: ''
          });
          if (res.data.code !== 'AAAAAAA') {
            errorNotify('上传失败，请重试！');
          } else {
            successNotify('上传成功！');
          }
        })
        .catch(err => {
          errorNotify('上传失败，请重试！');
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
      } else if (val == 'demandAssessReport') {
        return (val = '需求评估表');
      } else if (val == 'demandPlanConfirm') {
        return (val = '需归确认材料');
      } else if (val == 'businessTestReport') {
        return (val = '业测报告');
      } else if (val == 'launchConfirm') {
        return (val = '上线确认书');
      } else if (val == 'innerTestReport') {
        return (val = '内测报告');
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
    // 打开资源管理器选择文件
    openFileSelectWindow(refName) {
      this.$refs[refName].pickFiles();
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
          await this.queryDemandDoc({
            demand_id: this.demand_id,
            doc_type: ''
          });
          successNotify('删除成功');
        });
    },
    hideDialog() {
      this.fileModel = createFileModel();
      this.$emit('closeWpsFileHandler', 'close');
    },

    jumpConfluence(rowval) {
      let url = rowval.doc_link;
      window.open(url, '_blank');
    },
    async init() {
      this.demand_id = this.$route.params.id;
      this.loading = true;
      //文件创建时间和修改时间的日期格式化
      await this.queryDemandDoc({
        demand_id: this.demand_id,
        doc_type: ''
      });
      await this.fetch();
      this.loading = false;
    }
  },
  async mounted() {
    this.demand_id = this.$route.params.id;
    await this.queryDemandDoc({
      demand_id: this.demand_id,
      doc_type: ''
    });
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
</style>
