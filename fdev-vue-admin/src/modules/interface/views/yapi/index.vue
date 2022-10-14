<template>
  <f-block>
    <Loading class="bg-white" :visible="loading['interfaceForm/queryYapiList']">
      <fdev-table
        :data="tableData"
        class="q-px-sm q-pb-sm"
        :columns="columns"
        grid
        noExport
        :on-search="search"
        title="项目导入列表"
        titleIcon="list_s_f"
        :pagination.sync="pagination"
        @request="init"
        no-select-cols
      >
        <template v-slot:top-right>
          <fdev-btn
            normal
            label="Yapi 项目导入"
            dialog
            @click="handleYapiImportDialogOpen"
          />
          <fdev-btn normal label="schema3 -> schema4转换" @click="jump" />
        </template>
        <template v-slot:top-bottom>
          <f-formitem label="项目名、Yapi ProjectId" label-style="width:180px">
            <fdev-input
              placeholder="请输入项目名、Yapi ProjectId"
              clearable
              @keydown.enter="search"
              :value="filterStr"
              @input="updateFilterStr($event)"
            />
          </f-formitem>
        </template>
        <template v-slot:item="props">
          <div class="col-4 q-pa-sm">
            <fdev-card square>
              <fdev-card-section>
                <div class="text-h6 q-mb-sm row">
                  <router-link
                    :to="
                      `/interfaceAndRoute/interface/yapi/${
                        props.row.yapi_token
                      }`
                    "
                    class="link"
                  >
                    {{ props.row.project_name }}
                  </router-link>
                </div>

                <Description label-width="103px" class="q-mb-sm" label="录入人">
                  <router-link
                    :to="`/user/list/${props.row.import_user.import_user_id}`"
                    class="link"
                    v-if="props.row.import_user.import_user_id"
                  >
                    {{ props.row.import_user.import_name_cn }}
                  </router-link>
                  <span v-else>{{ props.row.import_name_en }}</span>
                </Description>

                <Description
                  label-width="103px"
                  class="q-mb-sm"
                  label="Yapi ProjectId"
                >
                  {{ props.row.project_id }}
                </Description>

                <Description ellipsis label-width="103px" label="Yapi Token">
                  <fdev-tooltip
                    v-if="props.row.yapi_token"
                    anchor="top middle"
                    self="center middle"
                  >
                    {{ props.row.yapi_token }}
                  </fdev-tooltip>
                  {{ props.row.yapi_token }}
                  <fdev-popup-proxy context-menu>
                    <fdev-banner style="max-width:300px">
                      {{ props.row.yapi_token }}
                    </fdev-banner>
                  </fdev-popup-proxy>
                </Description>
              </fdev-card-section>

              <fdev-separator />

              <div class="q-pa-sm">
                <fdev-btn
                  dialog
                  label="刷新"
                  color="primary"
                  @click="handleYapiImportDialog(props.row)"
                />
                <fdev-btn
                  dialog
                  label="删除"
                  color="red"
                  class="q-ml-sm"
                  @click="deleteProject(props.row)"
                />
              </div>
            </fdev-card>
          </div>
        </template>
      </fdev-table>

      <f-dialog v-model="yapiImportDialogOpened" title="导入Yapi项目下所有接口">
        <fdev-form @submit="handleYapiImportDialog">
          <f-formitem page label="Yapi项目Token">
            <fdev-input
              v-model="yapi_token"
              :rules="[() => !!yapi_token.trim() || '请输入Yapi项目Token']"
            />
          </f-formitem>
          <div
            class="row q-gutter-md items-center justify-end full-width btn-slot"
          >
            <fdev-btn
              type="submit"
              label="确定"
              dialog
              :loading="loading['interfaceForm/importYapiProject']"
            />
          </div>
        </fdev-form>
      </f-dialog>
    </Loading>
  </f-block>
</template>

<script>
import { mapActions, mapState, mapMutations } from 'vuex';
import { successNotify } from '@/utils/utils';
import { getYapiPagination, setYapiPagination } from '../../utils/setting';
import Loading from '@/components/Loading';
import { yapiColumns } from '../../utils/constants';
import Description from '@/components/Description';
export default {
  name: 'Yapi',
  components: { Loading, Description },

  data() {
    return {
      tableData: [],
      str: '',
      yapi_token: '',
      yapiImportDialogOpened: false,
      pagination: {
        sortBy: '',
        descending: false,
        page: 1,
        rowsPerPage: getYapiPagination().rowsPerPage || 5,
        rowsNumber: 0
      },
      columns: yapiColumns().columns
    };
  },

  computed: {
    ...mapState('userActionSaveInterface/interFace/YapiProjectImport', [
      'filterStr'
    ]),
    ...mapState('global', ['loading']),
    ...mapState('interfaceForm', ['yapiList', 'importMessage'])
  },

  watch: {
    'pagination.rowsPerPage'(val) {
      setYapiPagination({
        rowsPerPage: val
      });
    },
    filterStr(val) {
      if (!val) {
        this.search();
      }
    }
  },

  methods: {
    ...mapMutations('userActionSaveInterface/interFace/YapiProjectImport', [
      'updateFilterStr'
    ]),
    ...mapActions('interfaceForm', [
      'queryYapiList',
      'importYapiProject',
      'deleteYapiProject'
    ]),
    jump() {
      this.$router.push({ name: 'SchemaConcersion' });
    },
    handleYapiImportDialogOpen() {
      this.yapi_token = '';
      this.yapiImportDialogOpened = true;
    },
    async handleYapiImportDialog({ yapi_token }) {
      await this.importYapiProject({
        yapi_token: yapi_token ? yapi_token : this.yapi_token
      });
      this.yapiImportDialogOpened = false;

      if (this.importMessage) {
        this.$q.dialog({
          title: yapi_token ? '刷新成功' : '导入成功',
          message: this.importMessage
        });
      } else {
        successNotify(yapi_token ? '刷新成功' : '导入成功');
      }

      this.init();
    },
    deleteProject({ yapi_token }) {
      this.$q
        .dialog({
          title: '删除yapi项目',
          message: '确认删除此yapi项目？',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteYapiProject({
            yapi_token: yapi_token
          });
          successNotify('删除成功!');
          this.init();
        });
    },
    search() {
      this.pagination.page = 1;
      this.str = this.filterStr ? this.filterStr.trim() : '';
      this.init();
    },
    async init(props) {
      if (props && props.pagination) {
        let { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      await this.queryYapiList({
        page: this.pagination.page,
        pageNum: this.pagination.rowsPerPage,
        str: this.str
      });
      this.tableData = this.yapiList.list;
      this.pagination.rowsNumber = this.yapiList.total;
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
.search
  span
    line-height: 40px;
.input
  width 220px
  display inline-block
.btn
  border 1px solid red
>>> .q-btn
  text-transform none
.btn-slot
  padding 16px 0 24px 0
.yapi-import-dialog
  min-width 300px
</style>
