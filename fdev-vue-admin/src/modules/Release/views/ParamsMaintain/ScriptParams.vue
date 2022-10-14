<template>
  <Loading :visible="loading">
    <fdev-table
      titleIcon="list_s_f"
      :data="tableData"
      :columns="columnsDisplay"
      row-key="id"
      :visible-columns="visibleColumns"
      :pagination.sync="pagination"
      title="脚本参数列表"
      no-select-cols
    >
      <template v-slot:top-right>
        <span>
          <fdev-btn
            normal
            ficon="add"
            label="新增脚本参数"
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

    <f-dialog v-model="dialogOpened" :title="title" persistent>
      <f-formitem required label="参数key" diaS>
        <fdev-input
          type="text"
          ref="scriptParamsModel.key"
          v-model="$v.scriptParamsModel.key.$model"
          :rules="[() => $v.scriptParamsModel.key.required || '请输入参数key']"
        />
      </f-formitem>
      <f-formitem required label="参数value" diaS>
        <fdev-input
          type="text"
          ref="scriptParamsModel.value"
          v-model="$v.scriptParamsModel.value.$model"
          :rules="[
            () => $v.scriptParamsModel.value.required || '请输入参数value'
          ]"
        />
      </f-formitem>
      <f-formitem required label="描述" diaS>
        <fdev-input
          type="textarea"
          ref="scriptParamsModel.description"
          v-model="$v.scriptParamsModel.description.$model"
          :rules="[
            () => $v.scriptParamsModel.description.required || '请输入描述'
          ]"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn
          label="提交"
          :loading="globalLoading[`releaseForm/${type}Script`]"
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
import { scriptParamsModel } from '../../utils/model';
import { validate, successNotify, deepClone } from '@/utils/utils';
import { scriptParamsListColumns } from '../../utils/constants';

export default {
  name: 'ScriptParams',
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
      scriptParamsModel: scriptParamsModel(),
      columns: scriptParamsListColumns
    };
  },
  validations: {
    scriptParamsModel: {
      key: {
        required
      },
      value: {
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
    ...mapState('userActionSaveRelease/paramsScriptParams', [
      'visibleColumns',
      'currentPage'
    ]),
    ...mapState('releaseForm', {
      list: 'scriptParams'
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
    ...mapMutations('userActionSaveRelease/paramsScriptParams', [
      'updateVisibleColumns',
      'updateCurrentPage'
    ]),
    ...mapActions('releaseForm', {
      query: 'queryScript',
      deleted: 'deleteScript',
      add: 'addScript',
      update: 'updateScript'
    }),
    handleDialogOpened(type, data = scriptParamsModel()) {
      this.scriptParamsModel = deepClone(data);
      this.type = type;
      this.dialogOpened = true;
    },
    async submit() {
      this.$v.scriptParamsModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('scriptParamsModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.scriptParamsModel.$invalid) {
        return;
      }
      if (this.scriptParamsModel._id) {
        delete this.scriptParamsModel._id;
      }
      await this[this.type](this.scriptParamsModel);
      successNotify(`${this.title}成功！`);
      this.dialogOpened = false;
      this.init();
    },
    deleteModule(id) {
      this.$q
        .dialog({
          title: '确认删除',
          message: '确认删除此参数？',
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
