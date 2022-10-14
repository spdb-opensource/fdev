<template>
  <div>
    <!-- 上传文档弹窗 -->
    <f-dialog
      :title="isDirect ? '请选择文件' : '请上传业务暂缓确认邮件'"
      v-model="open"
      @before-close="handleClose"
    >
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
          :readonly="!isDirect"
        />
      </f-formitem>
      <!--confluence文档链接-->
      <f-formitem
        label="请选择文件"
        v-if="fileModel.filetype.value == 'confluenceFile'"
        label-style="width: 112px"
        required
        class="q-mt-md"
      >
        <fdev-input
          ref="confluenceUrl"
          v-model="confluenceUrl"
          placeholder="请输入文件链接"
          :rules="[v => !!v || '请输入文件链接']"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="handleClose"/>
        <fdev-btn label="确定" dialog @click="handleUploader"
      /></template>
    </f-dialog>
    <!-- 文件选择（隐藏） -->
    <div class="hidden">
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
          @input="uploadMethod"
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
  </div>
</template>
<script>
import { mapState } from 'vuex';
import { createFileModel, fileTypes } from '@/modules/Rqr/model';
import { errorNotify, baseUrl, successNotify } from '@/utils/utils';
import axios from 'axios';
export default {
  computed: {
    ...mapState('user', ['currentUser'])
  },
  props: {
    openLoader: {
      type: Boolean
    },
    demandId: {
      type: String
    },
    isDirect: {
      type: Boolean
    }
  },
  data() {
    return {
      open: false,
      filetypeOptions: fileTypes,
      fileModel: createFileModel(),
      mediaModel: [],
      confluenceUrl: ''
    };
  },
  methods: {
    //打开弹窗
    openDialog() {
      this.init();
      this.open = true;
    },
    //关闭弹窗
    handleClose() {
      this.open = false;
      if (this.isDirect) {
        this.$emit('resetDirect');
      }
    },
    // 点击确认
    handleUploader() {
      if (
        this.fileModel.filetype.value == 'confluenceFile' &&
        !this.$refs.confluenceUrl.validate()
      )
        return;
      this.open = false;
      this.openFileSelectWindow();
    },
    // 打开资源管理器选择文件
    async openFileSelectWindow() {
      if (this.fileModel.filetype.value !== 'confluenceFile') {
        this.$refs.fileInput.pickFiles();
      } else {
        this.uploadDirect();
      }
    },
    uploadMethod() {
      this.isDirect ? this.uploadDirect() : this.uploadFun();
    },
    // 上传并暂缓
    uploadFun() {
      let formData = new FormData();
      formData.append('id', this.demandId);
      formData.append('demandStatus', '3');
      formData.append('fileType', this.fileModel.filetype.value);
      formData.append('user_group_id', this.currentUser.group.id);
      formData.append('user_group_cn', this.currentUser.group.fullName);
      if (!this.mediaModel) {
        return;
      }
      let fileName = this.mediaModel.name;
      let fileSize = this.mediaModel.size;
      let finalName = fileName.split('.')[1];
      if (
        finalName != 'exe' &&
        finalName != 'dll' &&
        finalName != 'js' &&
        fileSize > 0 &&
        fileSize <= 10485760 * 2
      ) {
        formData.append('files', this.mediaModel);
      } else {
        this.mediaModel = [];
        errorNotify('该文件不能被上传或文件大小超过20M不被允许');
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
        .post(
          `${baseUrl}/fdemand/api/demandAssess/deferOperate`,
          formData,
          config
        )
        .then(res => {
          if (res.data.code !== 'AAAAAAA') {
            errorNotify('上传失败，请重试！');
          } else {
            // 刷新列表
            this.$emit('reFresh');
            successNotify('上传成功！');
          }
          this.mediaModel = [];
        })
        .catch(err => {
          errorNotify('上传失败，请重试！');
          this.mediaModel = [];
        });
    },
    //直接上传
    uploadDirect() {
      let formData = new FormData();
      formData.append('demand_id', this.demandId);
      formData.append('user_group_id', this.currentUser.group.id);
      formData.append('user_group_cn', this.currentUser.group.fullName);
      formData.append('doc_type', this.fileModel.filetype.value);
      formData.append('demand_kind', 'demandAccess');
      if (!this.mediaModel) {
        return;
      }
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
          fileSize <= 10485760 * 2
        ) {
          formData.append('files', this.mediaModel);
        } else {
          this.mediaModel = [];
          errorNotify('该文件不能被上传或文件大小超过20M不被允许');
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
          if (res.data.code !== 'AAAAAAA') {
            errorNotify('上传失败，请重试！');
          } else {
            // 刷新列表
            this.$emit('reFresh');
            successNotify('上传成功！');
          }
          this.mediaModel = [];
        })
        .catch(err => {
          errorNotify('上传失败，请重试！');
          this.mediaModel = [];
        });
    },
    init() {
      // 如果通过暂缓按钮上传只能选择暂缓邮件类型，如果从详情里直接上传可以选择其他类型
      if (!this.isDirect) {
        this.fileModel.filetype = { label: '暂缓邮件', value: 'deferEmail' };
      }
    }
  },
  created() {
    this.init();
  }
};
</script>
<style scoped lang="stylus">
.td-desc {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
