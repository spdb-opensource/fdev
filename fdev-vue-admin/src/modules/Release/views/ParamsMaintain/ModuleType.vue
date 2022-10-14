<template>
  <Loading :visible="loading">
    <fdev-table
      :data="tableData"
      :columns="columnsDisplay"
      row-key="id"
      titleIcon="list_s_f"
      title="模版类型列表"
      no-select-cols
      :visible-columns="visibleColumns"
      :pagination.sync="pagination"
    >
      <template v-slot:top-right>
        <span>
          <fdev-btn
            normal
            ficon="add"
            label="新增模版类型"
            :disable="!isManager"
            @click="handleDialogOpened('add')"
          />
          <fdev-tooltip v-if="!isManager">
            请联系投产管理员或卡点管理员
          </fdev-tooltip>
        </span>
      </template>

      <template v-slot:body-cell-btn="props">
        <fdev-td auto-width>
          <div class="q-gutter-sm row items-center no-wrap">
            <fdev-btn
              label="修改"
              flat
              @click="handleDialogOpened('update', props.row)"
            />
            <fdev-btn label="删除" flat @click="deleteModule(props.row.id)" />
          </div>
        </fdev-td>
      </template>
    </fdev-table>

    <f-dialog v-model="dialogOpened" persistent :title="title">
      <f-formitem required label="目录" diaS>
        <fdev-input
          type="text"
          ref="moduleTypeModel.catalog_name"
          v-model="$v.moduleTypeModel.catalog_name.$model"
          :rules="[
            () => $v.moduleTypeModel.catalog_name.required || '请输入目录'
          ]"
        />
      </f-formitem>
      <f-formitem required label="类型" diaS>
        <fdev-select
          input-debounce="0"
          option-label="label"
          option-value="value"
          emit-value
          fill-input
          map-options
          :options="catalogTypeOptions"
          ref="moduleTypeModel.catalog_type"
          v-model="$v.moduleTypeModel.catalog_type.$model"
          :rules="[
            () => !$v.moduleTypeModel.catalog_type.$error || '请选择类型'
          ]"
        />
      </f-formitem>
      <f-formitem required label="描述" diaS>
        <fdev-input
          type="textarea"
          ref="moduleTypeModel.description"
          v-model="$v.moduleTypeModel.description.$model"
          :rules="[
            () => $v.moduleTypeModel.description.required || '请输入描述'
          ]"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn
          label="提交"
          :loading="globalLoading[`releaseForm/${type}ModuleType`]"
          dialog
          @click="submit"
      /></template>
    </f-dialog>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { required } from 'vuelidate/lib/validators';
import { mapActions, mapState, mapMutations } from 'vuex';
import { moduleTypeModel, catalogTypeOptions } from '../../utils/model';
import { moduleTypeListColumns } from '../../utils/constants';

import { validate, successNotify, deepClone } from '@/utils/utils';
export default {
  name: 'ModuleType',
  components: { Loading },
  data() {
    return {
      pagination: {
        rowsPerPage: 5
      },
      loading: false,
      dialogOpened: false,
      type: '',
      tableData: [],
      moduleTypeModel: moduleTypeModel(),
      catalogTypeOptions: catalogTypeOptions,
      columns: moduleTypeListColumns
    };
  },
  validations: {
    moduleTypeModel: {
      catalog_name: {
        required
      },
      catalog_type: {
        required
      },
      description: {
        required
      }
    }
  },
  props: {
    isManager: Boolean
  },
  watch: {
    pagination(val) {
      this.updateCurrentPage({
        rowsPerPage: val.rowsPerPage
      });
    }
  },
  computed: {
    ...mapState('userActionSaveRelease/paramsModuleType', [
      'visibleColumns',
      'currentPage'
    ]),
    ...mapState('releaseForm', {
      list: 'moduleType'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    title() {
      return this.type === 'add' ? '新增' : '修改';
    },
    columnsDisplay() {
      if (this.isManager) {
        return this.columns.concat({
          name: 'btn',
          field: 'btn',
          label: '操作'
        });
      } else {
        return this.columns;
      }
    }
  },
  methods: {
    ...mapMutations('userActionSaveRelease/paramsModuleType', [
      'updateVisibleColumns',
      'updateCurrentPage'
    ]),
    ...mapActions('releaseForm', {
      query: 'queryModuleType',
      deleted: 'deleteModuleType',
      add: 'addModuleType',
      update: 'updateModuleType'
    }),
    handleDialogOpened(type, data = moduleTypeModel()) {
      this.moduleTypeModel = deepClone(data);
      this.type = type;
      this.dialogOpened = true;
    },
    async submit() {
      this.$v.moduleTypeModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('moduleTypeModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.moduleTypeModel.$invalid) {
        return;
      }
      if (this.moduleTypeModel._id) {
        delete this.moduleTypeModel._id;
      }

      await this[this.type](this.moduleTypeModel);
      successNotify(`${this.title}成功！`);
      this.dialogOpened = false;
      this.init();
    },
    deleteModule(id) {
      this.$q
        .dialog({
          title: '确认删除',
          message: '确认删除此目录？',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleted({
            id: id
          });
          successNotify('删除成功！');
          this.init();
        });
    },
    confirmToClose() {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this.dialogOpened = false;
        });
    },
    async init() {
      this.loading = true;
      await this.query();
      this.tableData = this.list;
      this.loading = false;
    }
  },
  created() {
    this.pagination = this.currentPage;
    this.init();
  }
};
</script>

<style lang="stylus" scoped></style>
