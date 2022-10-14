<template>
  <f-block>
    <Loading
      class="bg-white"
      :visible="loading['interfaceForm/queryYapiDetail']"
    >
      <div class="q-pa-md text-h6 row">
        {{ yapiDetail.project_name }}

        <fdev-space />

        <fdev-btn-group unelevated>
          <fdev-btn
            :loading="loading['interfaceForm/importYapiProject']"
            dialog
            label="刷新项目"
            @click="refresh"
          />
          <fdev-btn dialog label="返回" @click="goBack" />
        </fdev-btn-group>
      </div>

      <div class="q-ml-md">
        <p class="font">详情</p>
        <div class="row q-ml-md">
          <f-formitem class="col-6" label="录入人:">
            <router-link
              :to="`/user/list/${yapiDetail.import_user.import_user_id}`"
              class="link"
              v-if="yapiDetail.import_user.import_user_id"
            >
              {{ yapiDetail.import_user.import_name_cn }}
            </router-link>
            <span v-else>{{ yapiDetail.import_user.import_name_en }}</span>
          </f-formitem>

          <f-formitem class="col-6" label="Yapi Project_id:">
            {{ yapiDetail.project_id }}
          </f-formitem>

          <f-formitem fullWidth label="Yapi Token:">
            {{ yapiDetail.yapi_token }}
          </f-formitem>
        </div>
      </div>

      <div class="q-ml-md q-mt-md">
        <fdev-table
          :data="tableData"
          class="q-px-sm q-pb-sm"
          :columns="columns"
          flat
          title="接口列表"
          titleIcon="list_s_f"
          noExport
          no-select-cols
          :pagination.sync="pagination"
          :on-search="search"
          @request="init"
        >
          <template v-slot:top-bottom>
            <f-formitem label="接口名称、接口路径" label-style="width:180px">
              <fdev-input
                placeholder="请输入接口名称、接口路径"
                clearable
                :value="filterStr"
                @input="updateFilterStr($event)"
              />
            </f-formitem>
          </template>
          <template v-slot:body-cell-btn="props">
            <fdev-td auto-width>
              <fdev-btn
                label="Json Schema下载"
                flat
                color="primary"
                @click="download(props.row)"
              />
              <fdev-btn
                class="q-mx-sm"
                label="刷新"
                flat
                color="primary"
                @click="interfaceRefresh(props.row)"
              />
              <fdev-btn
                label="删除"
                flat
                color="red"
                @click="deleteInterface(props.row)"
              />
            </fdev-td>
          </template>
        </fdev-table>
      </div>
    </Loading>
  </f-block>
</template>

<script>
import { mapActions, mapState, mapMutations } from 'vuex';
import { successNotify } from '@/utils/utils';
import {
  getYapiInterfacePagination,
  setYapiInterfacePagination
} from '../../utils/setting';
import { yapiColumns } from '../../utils/constants';
import Loading from '@/components/Loading';
export default {
  name: 'YapiProfile',
  components: { Loading },
  props: ['token'],
  data() {
    return {
      tableData: [],
      str: '',
      pagination: {
        sortBy: '',
        descending: false,
        page: 1,
        rowsPerPage: getYapiInterfacePagination().rowsPerPage || 5,
        rowsNumber: 0
      },
      columns: yapiColumns().profileColumns
    };
  },
  computed: {
    ...mapState('userActionSaveInterface/interFace/yapiProfile', ['filterStr']),
    ...mapState('global', ['loading']),
    ...mapState('interfaceForm', ['yapiDetail', 'importMessage'])
  },

  watch: {
    'pagination.rowsPerPage'(val) {
      setYapiInterfacePagination({
        rowsPerPage: val
      });
    }
  },

  methods: {
    ...mapMutations('userActionSaveInterface/interFace/yapiProfile', [
      'updateFilterStr'
    ]),
    ...mapActions('interfaceForm', [
      'queryYapiDetail',
      'importYapiProject',
      'importYapiInterface',
      'deleteYapiInterface'
    ]),
    download({ interface_name, json_schema }) {
      const link = document.createElement('a');
      const file = new Blob([json_schema], {
        type: 'data:text/plain;charset=utf-8'
      });
      const URL = window.URL.createObjectURL(file);

      link.href = URL;
      link.download = `${interface_name}.json`;
      document.body.appendChild(link);

      link.click();

      window.URL.revokeObjectURL(link.href);
      document.body.removeChild(link);
      successNotify('下载成功！');
    },
    async refresh() {
      await this.importYapiProject({
        yapi_token: this.token
      });
      if (this.importMessage) {
        this.$q.dialog({
          title: '刷新成功',
          message: this.importMessage
        });
      } else {
        successNotify('刷新成功');
      }
      this.init();
    },
    async interfaceRefresh({ interface_id }) {
      await this.importYapiInterface({
        yapi_token: this.token,
        interface_id
      });
      successNotify('刷新成功!');
      this.init();
    },
    async deleteInterface({ interface_id }) {
      this.$q
        .dialog({
          title: '删除接口',
          message: '确认删除此接口？',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteYapiInterface({
            yapi_token: this.token,
            interface_id
          });
          successNotify('删除成功!');
          this.init();
        });
    },
    search() {
      this.pagination.page = 1;
      this.str = this.filterStr;
      this.init();
    },
    async init(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      await this.queryYapiDetail({
        page: this.pagination.page,
        pageNum: this.pagination.rowsPerPage,
        yapi_token: this.token,
        str: this.str
      });
      this.tableData = this.yapiDetail.interfaces;
      this.pagination.rowsNumber = this.yapiDetail.total;
    }
  },
  created() {
    if (this.filterStr) {
      this.search();
    } else {
      this.init();
    }
  }
};
</script>

<style lang="stylus" scoped>
.font
  font-weight: 700;
>>> .q-btn
  text-transform none
.input
  width 210px
</style>
