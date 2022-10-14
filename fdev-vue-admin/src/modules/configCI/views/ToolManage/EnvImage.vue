<template>
  <div>
    <fdev-table
      :data="imageTableData"
      :columns="columns"
      row-key="name"
      title="工作环境镜像列表"
      :pagination.sync="pagination"
      @request="onRequest"
      noExport
    >
      <template v-slot:top-right>
        <fdev-btn normal ficon="add" label="新增镜像" @click="handelAddImage" />
      </template>
      <template v-slot:body-cell-active="props">
        <fdev-td>
          <fdev-toggle
            v-model="props.row.active"
            :disable="props.row.canEdit !== '1'"
            @input="toggleImage(props.row)"
          />
        </fdev-td>
      </template>
      <template v-slot:body-cell-btn="props">
        <fdev-td>
          <template v-if="props.row.canEdit === '1'">
            <fdev-btn
              flat
              label="编辑"
              class="q-mr-sm"
              @click="editImage(props.row)"
            />
            <fdev-btn flat label="删除" @click="delImage(props.row)" />
          </template>
          <template v-else
            ><fdev-btn flat label="无权限" disable
          /></template>
        </fdev-td>
      </template>
    </fdev-table>
    <f-dialog v-model="addDialog" title="新增镜像">
      <fdev-form
        :greedy="true"
        :no-error-focus="true"
        ref="updateModel"
        @submit.prevent="handleAddImage"
      >
        <f-formitem required label="镜像名称">
          <fdev-input
            v-model="updateModel.name"
            clearable
            maxlength="20"
            :rules="[
              val => !!(val && val.trim()) || '请输入镜像名称，20字以内'
            ]"
          />
        </f-formitem>
        <f-formitem required label="镜像路径">
          <fdev-input
            v-model="updateModel.path"
            clearable
            :rules="[val => !!(val && val.trim()) || '请输入镜像路径']"
          />
        </f-formitem>
        <!-- <f-formitem label="镜像可见范围">
          <fdev-select
            v-model="updateModel.visibleRange"
            :options="visibleRanges"
            :rules="[val => (val && val.length) || '请输入镜像路径']"
          />
        </f-formitem> -->
        <f-formitem label="镜像说明">
          <fdev-input
            v-model="updateModel.desc"
            class="scroll-thin"
            placeholder="请简要说明该镜像提供了什么环境，50字以内"
            type="textarea"
            rows="3"
            maxlength="50"
            clearable
          />
        </f-formitem>
        <div class="float-right q-pa-md">
          <fdev-btn label="取消" outline dialog @click="addDialog = false" />
          <fdev-btn label="确定" dialog class="q-ml-md" type="submit" />
        </div>
      </fdev-form>
    </f-dialog>
  </div>
</template>

<script>
import { successNotify, resolveResponseError } from '@/utils/utils';
import {
  queryToolImageList,
  addImage,
  updateImage
} from '../../services/method';
export default {
  name: 'EnvImage',
  data() {
    return {
      columns: [
        {
          name: 'name',
          field: 'name',
          label: '镜像名称',
          copy: true
        },
        {
          name: 'path',
          field: 'path',
          label: '路径',
          copy: true
        },
        {
          name: 'desc',
          field: 'desc',
          label: '镜像描述',
          copy: true
        },
        {
          name: 'active',
          field: 'active',
          label: '是否公开'
        },
        { name: 'btn', field: 'btn', label: '操作' }
      ],
      pagination: {
        rowsPerPage: 5,
        rowsNumber: 0,
        page: 1
      },
      imageTableData: [],
      addDialog: false,
      updateModel: {
        name: '',
        path: '',
        desc: ''
        // visibleRange: null // 镜像可见范围
      },
      // visibleRanges: ['public', 'group', 'private'],
      type: ''
    };
  },

  computed: {},
  methods: {
    onRequest(props) {
      this.pagination = props.pagination;
      this.getTableData();
    },
    async getTableData() {
      const { rowsPerPage, page } = this.pagination;
      const res = await queryToolImageList({
        pageNum: page,
        pageSize: rowsPerPage
      });
      this.imageTableData = res.content;
      this.pagination.rowsNumber = res.totalElements;
    },
    handelAddImage() {
      this.updateModel = {
        name: '',
        path: '',
        desc: ''
        // visibleRange: null
      };
      this.type = 'add';
      this.addDialog = true;
    },
    handleAddImage() {
      this.$refs.updateModel.validate().then(async res => {
        if (!res) return;
        let params = {
          ...this.updateModel,
          desc: this.updateModel.desc || '' //避免null时后端不处理此字段
        };
        if (this.type === 'add') {
          await resolveResponseError(() => addImage(params));
          successNotify('新增成功');
        } else {
          await resolveResponseError(() => updateImage(params));
          successNotify('更新成功');
        }
        this.addDialog = false;
        this.getTableData();
      });
    },
    delImage(data) {
      return this.$q
        .dialog({
          title: '删除提示',
          message: '确定删除该条工作环境镜像吗？',
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await resolveResponseError(() =>
            updateImage({
              id: data.id,
              status: '0'
            })
          );
          successNotify('删除成功');
          this.getTableData();
        });
    },
    editImage(data) {
      this.updateModel = { ...data };
      this.type = 'edit';
      this.addDialog = true;
    },
    //更新状态
    async toggleImage(data) {
      let { active, id } = data;
      await resolveResponseError(() =>
        updateImage({
          id,
          active
        })
      );
      successNotify('更新状态成功');
    }
  },
  created() {
    this.getTableData();
  }
};
</script>

<style lang="stylus" scoped></style>
