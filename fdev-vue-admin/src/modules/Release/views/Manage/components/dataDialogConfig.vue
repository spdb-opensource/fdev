<template>
  <f-dialog
    :value="value"
    right
    @input="$emit('input', $event)"
    title="获取配置文件列表"
  >
    <!-- <span v-if="!showRefreshBtn"
      >注：数据库更新文件仅展示通过审核的sh文件和sql文件</span
    > -->
    <fdev-table
      titleIcon="list_s_f"
      title="应用变更列表"
      :data="changeApllication"
      :columns="columns"
      noExport
      :selected.sync="selectedApp"
      row-key="application_id"
      selection="single"
      :pagination.sync="pagination"
      no-select-cols
    >
      <template v-slot:body-selection="props">
        <span>
          <fdev-checkbox
            v-model="props.selected"
            :disable="!props.row.pro_image_uri && !props.row.pro_scc_image_uri"
          />
          <fdev-tooltip
            v-if="!props.row.pro_image_uri && !props.row.pro_scc_image_uri"
          >
            tag不能为空，请前往应用卡片选择镜像版本
          </fdev-tooltip>
        </span>
      </template>
    </fdev-table>
    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        label="确定"
        @click="handleQueryProfile"
        :loading="globalLoading[`releaseForm/queryProfile`]"
        :disable="selectedApp.length === 0"
      />
    </template>
  </f-dialog>
</template>

<script>
import { mapActions, mapState } from 'vuex';
export default {
  name: 'dataDialogConfig',
  data() {
    return {
      loading: false,
      selectedApp: [],
      columns: [
        { name: 'app_name_zh', label: '应用名称', field: 'app_name_zh' },
        {
          name: 'pro_image_uri',
          label: 'caas镜像标签',
          field: 'pro_image_uri'
        },
        {
          name: 'pro_scc_image_uri',
          label: 'scc镜像标签',
          field: 'pro_scc_image_uri'
        },

        {
          name: 'compare_url',
          label: '最新的tag',
          field: row => this.getTagName(row)
        }
      ],
      pagination: {
        rowsPerPage: 5
      }
    };
  },
  props: {
    value: {
      type: Boolean
    },
    prod_id: {
      type: String
    }
  },
  computed: {
    ...mapState('releaseForm', {
      changeApllication: 'changeApllication'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    })
  },
  methods: {
    ...mapActions('releaseForm', {
      queryProfile: 'queryProfile' //获取配置文件
    }),
    // 获取tag名称
    getTagName(row) {
      const tag = row.pro_image_uri || row.pro_scc_image_uri || '';
      return tag ? tag.split(':')[tag.split(':').length - 1] : '';
    },

    handleQueryProfile() {
      const selectedAppObj = this.selectedApp[0];
      const tag =
        selectedAppObj.pro_image_uri || selectedAppObj.pro_scc_image_uri || '';
      const data = {
        application_id: selectedAppObj.application_id || '',
        pro_image_uri: selectedAppObj.pro_image_uri || '',
        pro_scc_image_uri: selectedAppObj.pro_scc_image_uri || '',
        tag: tag ? tag.split(':')[tag.split(':').length - 1] : ''
      };
      this.$emit('click', data);
    }

    // async init() {
    //   this.selectedApp = [];
    // }
  },
  watch: {
    value(val) {
      if (val === true) {
        this.selectedApp = [];
      }
    }
  }
};
</script>

<style lang="stylus" scoped></style>
