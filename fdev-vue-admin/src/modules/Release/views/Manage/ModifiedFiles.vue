<template>
  <Loading
    :visible="loading['releaseForm/queryByReleaseNodeName']"
    class="q-pa-sm"
  >
    <ModifiedFilesTable
      title="业务需求"
      class="q-mb-md"
      :data="releaseFiles.business_rqrmnt_list"
      @download="downExcel"
      :selected.sync="rqrmntListSelect"
    />

    <ModifiedFilesTable
      title="科技需求"
      class="q-mb-md"
      :data="releaseFiles.tech_rqrmnt_list"
      @download="downExcel"
      :selected.sync="techRqrmntListSelect"
    />
    <fdev-table
      class="q-mb-md"
      titleIcon="list_s_f"
      title="投产窗口上传材料"
      no-select-cols
      :data="releaseFiles.rqrmnt_file_list"
      :columns="columns"
      row-key="id"
      selection="multiple"
      :selected.sync="rqrmntFileListSelected"
      @selection="selection"
      hide-bottom
      :pagination="{
        rowsPerPage: 0
      }"
    >
      <template v-slot:body-cell-name="props">
        <fdev-td class="text-ellipsis">
          <a
            class="link"
            @click="
              downExcel({
                moduleName: 'fdev-release',
                path: props.row.path
              })
            "
            :title="
              `${props.row.name}${
                props.row.risk_flag === '1'
                  ? '（文件内容风险提示，请复核）'
                  : ''
              }`
            "
          >
            {{ props.row.name }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                <span>{{ props.row.path || '-' }}</span>
                <span class="hasRisk" v-if="props.row.risk_flag === '1'">
                  （文件内容风险提示，请复核）
                </span>
              </fdev-banner>
            </fdev-popup-proxy>
            <span class="hasRisk" v-if="props.row.risk_flag === '1'">
              （文件内容风险提示，请复核）
            </span>
          </a>
        </fdev-td>
      </template>

      <template v-slot:body-cell-btn="props">
        <fdev-td>
          <fdev-btn
            color="red"
            icon="delete"
            flat
            @click="deleteFile(props.row)"
          />
        </fdev-td>
      </template>
    </fdev-table>

    <fdev-fab
      direction="left"
      class="uploader"
      icon="cloud_upload"
      color="primary"
    >
      <fdev-uploader
        :factory="beforeUpdate"
        fileName="file"
        style="max-width: 300px"
        :multiple="false"
        @uploaded="fileUploaded"
        @failed="failed"
        @added="added"
        ref="picker"
      />
    </fdev-fab>

    <fdev-btn
      icon="ion-cloud-download"
      round
      color="blue-5"
      padding="md"
      class="download"
      @click="handleTreeDialogOpen"
      :disable="selected.length === 0"
    />

    <f-dialog right v-model="treeDialogOpened" title="文档清单">
      <fdev-tree
        :nodes="tree"
        node-key="id"
        label-key="name"
        default-expand-all
      />
      <template v-slot:btnSlot>
        <fdev-btn
          :loading="loading['releaseForm/downloadFiles']"
          label="下载"
          dialog
          @click="handleDownloadFiles"
        />
      </template>
    </f-dialog>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions } from 'vuex';
import { baseUrl } from '@/utils/utils';
import { successNotify, exportExcel, errorNotify } from '@/utils/utils';
import ModifiedFilesTable from '../../Components/ModifiedFilesTable';

export default {
  components: {
    Loading,
    ModifiedFilesTable
  },
  data() {
    return {
      tableData: [],
      url: baseUrl,
      release_node_name: '',
      treeDialogOpened: false,
      rqrmntFileListSelected: [],
      columns: [
        { name: 'name', label: '文件名', field: 'name' },
        { name: 'btn', field: 'name' }
      ],
      rqrmntListSelect: [],
      techRqrmntListSelect: [],
      tree: []
    };
  },
  computed: {
    ...mapState('releaseForm', ['releaseFiles', 'downFiles']),
    ...mapState('global', ['loading']),
    selected() {
      return [
        ...this.rqrmntListSelect,
        ...this.rqrmntFileListSelected,
        ...this.techRqrmntListSelect
      ];
    }
  },
  methods: {
    ...mapActions('releaseForm', [
      'queryByReleaseNodeName',
      'deleteRequireFile',
      'downloadFiles'
    ]),
    ...mapActions('jobForm', ['downExcel']),
    beforeUpdate(file) {
      return {
        url: `${baseUrl}frelease/api/rqrmnt/upload`,
        method: 'POST',
        headers: [
          {
            name: 'Authorization',
            value: this.$q.localStorage.getItem('fdev-vue-admin-jwt')
          }
        ],
        fieldName: 'files',
        formFields: [
          { name: 'release_node_name', value: this.release_node_name }
        ]
      };
    },
    fileUploaded({ files, xhr }) {
      const resp = JSON.parse(xhr.responseText);
      if (!resp.code || resp.code !== 'AAAAAAA') {
        errorNotify(resp.msg);
        return;
      }
      this.init();
      successNotify('上传成功!');
    },
    failed(info) {
      const response = JSON.parse(info.xhr.response);
      if (response.status === 401) {
        this.$router.push('/login');
      }
      errorNotify(response.message);
    },
    deleteFile({ id }) {
      this.$q
        .dialog({
          title: '删除文件',
          message: `确认删除此文件？`,
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteRequireFile({
            release_node_name: this.release_node_name,
            id
          });
          successNotify('删除成功！');
          this.init();
        });
    },
    init() {
      this.queryByReleaseNodeName({
        release_node_name: this.release_node_name
      });
    },
    async handleDownloadFiles() {
      await this.downloadFiles({
        release_node_name: this.release_node_name,
        files: this.selected
      });
      exportExcel(this.downFiles);
      this.treeDialogOpened = false;
    },
    added(files) {
      if (files.length > 0) {
        let existFile = this.tableData.some(file => {
          if (file.type === '2') {
            return file.name === files[0].name;
          }
        });
        if (existFile) {
          this.$q
            .dialog({
              title: '是否替换同名文件',
              message: `该任务下已存在同名文件,是否替换?`,
              cancel: true
            })
            .onCancel(() => {
              this.$refs.picker.removeFile(files[0]);
            });
        }
      }
    },
    selection(details) {
      return details.rows;
    },
    handleTreeData(data, selected, name) {
      const children = data.reduce((total, item) => {
        const obj = {
          name: item.rqrmnt_name,
          children: [],
          id: item.id
        };
        item.task_list.forEach(task => {
          const taskObj = {
            name: task.task_name,
            children: [],
            id: task.task_id
          };
          task.task_file.forEach(taskFile => {
            selected.forEach(i => {
              if (i.name === taskFile.name) {
                taskObj.children.push({
                  ...taskFile,
                  id: Math.random()
                });
              }
            });
          });
          if (taskObj.children.length > 0) {
            obj.children.push(taskObj);
          }
        });
        if (obj.children.length > 0) {
          total.push(obj);
        }
        return total;
      }, []);

      return [
        {
          name,
          children
        }
      ];
    },
    handleTreeDialogOpen() {
      const yewu =
        this.rqrmntListSelect.length > 0
          ? this.handleTreeData(
              this.releaseFiles.business_rqrmnt_list,
              this.rqrmntListSelect,
              '业务需求'
            )
          : [];
      const keji =
        this.techRqrmntListSelect.length > 0
          ? this.handleTreeData(
              this.releaseFiles.tech_rqrmnt_list,
              this.techRqrmntListSelect,
              '科技需求'
            )
          : [];
      const release =
        this.rqrmntFileListSelected.length > 0
          ? [
              {
                name: '投产窗口上传材料',
                children: this.rqrmntFileListSelected
              }
            ]
          : [];
      this.tree = [...yewu, ...keji, ...release];
      this.treeDialogOpened = true;
    }
  },
  async created() {
    this.release_node_name = this.$route.params.id;
    await this.init();
  }
};
</script>

<style lang="stylus" scoped>
.uploader, .download
  position fixed
  bottom 50px
  right 130px
  z-index 2001
  min-width 56px
  width 56px
  height 56px
  min-height 56px
/deep/ .q-fab > .q-btn
  width 56px
  min-width 56px
  height 56px
  min-height 56px
.download
  right 50px
.hasRisk
  color red
</style>
