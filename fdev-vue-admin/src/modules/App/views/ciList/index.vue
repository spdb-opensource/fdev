<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-table
        :data="appContinuousTableData"
        title="持续集成模板列表"
        title-icon="list_s_f"
        :columns="columns"
        row-key="id"
        :pagination.sync="pagination"
        class="q-mt-md"
        noExport
        no-select-cols
      >
        <template v-slot:top-right>
          <div class="row q-gutter-md">
            <!-- 新增按钮 -->
            <Authorized :role-authority="['环境配置管理员']">
              <fdev-btn
                ficon="add"
                label="新增"
                normal
                @click="handleAddCIModalOpen('新建')"
              />
            </Authorized>
          </div>
        </template>
        <template v-slot:body-cell-opt="props">
          <fdev-td>
            <Authorized :role-authority="['环境配置管理员']">
              <fdev-btn
                flat
                label="编辑"
                @click="handleAddCIModalOpen('编辑', props.row)"
              />
            </Authorized>
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>

    <!-- 新增弹框 -->
    <f-dialog v-model="addCIModalOpened" f-sc :title="`${title}持续集成模板`">
      <f-formitem label="持续集成模板名" diaS required>
        <fdev-input
          ref="ciTemplateModel.name"
          v-model="$v.ciTemplateModel.name.$model"
          :rules="[
            () => !$v.ciTemplateModel.name.$error || '请输入持续集成模板名'
          ]"
        />
      </f-formitem>
      <f-formitem label="yaml文件名" diaS required>
        <fdev-input
          ref="ciTemplateModel.yaml_name"
          v-model="$v.ciTemplateModel.yaml_name.$model"
          :rules="[
            () => !$v.ciTemplateModel.yaml_name.$error || '请输入yaml文件名'
          ]"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn
          label="提交"
          dialog
          @click="handleAddCIModel"
          :loading="globalLoading[`appForm/${submitLoading}`]"
      /></template>
    </f-dialog>
  </f-block>
</template>

<script>
import { setCIPagination, getCIPagination } from '@/modules/App/utils/setting';
import { createCIModel, ciListColumns } from '@/modules/App/utils/constants'; // 表单字段存放
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify } from '@/utils/utils';
import Loading from '@/components/Loading';
import Authorized from '@/components/Authorized';
import { mapState, mapActions } from 'vuex';

export default {
  name: 'AppCIList',
  components: { Loading, Authorized },
  data() {
    return {
      pagination: getCIPagination(),
      title: '新建', // 弹窗标题
      loading: false, // 罩层
      addCIModalOpened: false, // 弹窗显隐
      columns: ciListColumns(), // 表格头
      ciTemplateModel: createCIModel() // 新增/编辑字段
    };
  },
  validations: {
    ciTemplateModel: {
      name: {
        required
      },
      yaml_name: {
        required
      }
    }
  },
  computed: {
    ...mapState('global', { globalLoading: 'loading' }),
    ...mapState('appForm', ['appContinuousTableData', 'appContinuous']),
    ...mapState('user', ['currentUser']),
    submitLoading() {
      if (this.title === '编辑') {
        return 'queryAppContinuousUpdate';
      } else {
        return 'queryAppContinuousSave';
      }
    }
  },
  watch: {
    pagination(val) {
      setCIPagination({
        rowsPerPage: val.rowsPerPage
      });
    }
  },
  methods: {
    ...mapActions('appForm', {
      queryAppContinuous: 'queryAppContinuous',
      queryAppContinuousUpdate: 'queryAppContinuousUpdate',
      queryAppContinuousSave: 'queryAppContinuousSave',
      queryAppContinuousFind: 'queryAppContinuousFind'
    }),
    // 初始化数据
    async getTableData() {
      this.loading = true;
      await this.queryAppContinuous();
      this.loading = false;
    },
    /* 打开新增/编辑页面并初始化数据 */
    async handleAddCIModalOpen(title, row) {
      this.title = title;
      if (this.title === '编辑') {
        await this.queryAppContinuousFind(row.id);
        this.ciTemplateModel = this.appContinuous;
      } else {
        this.ciTemplateModel = createCIModel();
      }
      this.addCIModalOpened = true;
    },
    // 提交弹窗
    async handleAddCIModel() {
      this.$v.ciTemplateModel.$touch();
      let ciTemplateKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('ciTemplateModel') > -1;
      });
      validate(ciTemplateKeys.map(key => this.$refs[key]));
      if (this.$v.ciTemplateModel.$invalid) {
        return;
      }
      if (this.title === '编辑') {
        await this.queryAppContinuousUpdate(this.ciTemplateModel);
      } else {
        await this.queryAppContinuousSave(this.ciTemplateModel);
      }
      successNotify(`${this.title}成功`);
      this.addCIModalOpened = false;
      await this.getTableData();
    }
  },
  async created() {
    await this.getTableData();
  }
};
</script>
