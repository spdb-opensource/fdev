<template>
  <Loading :visible="loading">
    <fdev-select
      outlined
      use-input
      dense
      v-model="selectedModel"
      :options="option"
      option-label="name_en"
      option-value="name_en"
      @filter="userFilterChoice"
      @input="getTableData(selectedModel)"
      class="select q-pa-sm"
    >
      <template v-slot:option="scope">
        <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
          <fdev-item-section>
            <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
            <fdev-item-label caption>
              {{ scope.opt.name_cn }}
            </fdev-item-label>
          </fdev-item-section>
        </fdev-item>
      </template>
    </fdev-select>
    <span v-if="selectedModel.name_cn">
      <span class="span-msg">实体中文名：{{ selectedModel.name_cn }}</span>
      <span class="span-msg span-desc">描述信息：{{ selectedModel.desc }}</span>
    </span>
    <fdev-table
      :data="modelEvnList"
      :columns="columns"
      row-key="id"
      class="table"
      :pagination.sync="pagination"
      flat
    >
      <template v-slot:body="props">
        <fdev-tr :props="props">
          <fdev-td key="env" :props="props">
            {{ props.row.env }}
          </fdev-td>
          <fdev-td key="env_name_en" :props="props">
            {{ props.row.env_name_en }}
          </fdev-td>
          <fdev-td key="model" :props="props">
            {{ props.row.model }}
          </fdev-td>
          <fdev-td key="desc" :props="props" class="td_desc">
            {{ props.row.desc }}
          </fdev-td>
          <fdev-td key="btn" :props="props" :auto-width="true">
            <fdev-btn
              label="详情"
              flat
              size="sm"
              class="btn"
              color="primary"
              @click="openDetail(props.row)"
            />
            <fdev-btn
              label="复制"
              flat
              size="sm"
              class="btn"
              color="primary"
              @click="goToManagePage('copy', props.row.id, 'model')"
              v-if="role"
            />
            <fdev-btn
              v-if="role || isManager"
              label="修改"
              flat
              size="sm"
              class="btn"
              color="primary"
              @click="goToManagePage('update', props.row.id, 'model')"
            />
            <fdev-btn
              v-if="role"
              label="删除"
              flat
              size="sm"
              class="btn"
              color="red"
              @click="handleDeleteDialogOpened(props.row)"
            />
          </fdev-td>
        </fdev-tr>
      </template>
    </fdev-table>
    <!-- 弹出 输入验证码删除对话框 -->
    <fdev-dialog v-model="deleteDialogOpened">
      <VerifycodeDialog
        @handleDelete="handleDelete"
        :description="description"
      />
    </fdev-dialog>
    <div class="row justify-center" v-if="role">
      <fdev-btn
        label="新增实体与环境映射"
        color="primary"
        text-color="white"
        class="q-mb-lg q-mr-lg"
        @click="goToManagePage('add', '', 'model')"
      />
    </div>
    <!-- 详情页开始 -->
    <fdev-dialog
      v-model="handleShow"
      transition-show="slide-up"
      transition-hide="slide-down"
    >
      <fdev-layout view="Lhh lpR fff" container class="bg-white">
        <fdev-header bordered class="bg-primary">
          <fdev-toolbar class="toolbar">
            <fdev-toolbar-title>实体与环境映射详情</fdev-toolbar-title>
            <fdev-btn flat v-close-popup round dense icon="close" />
          </fdev-toolbar>
        </fdev-header>
        <fdev-page-container>
          <fdev-page padding>
            <div class="q-px-lg">
              <fdev-markup-table flat>
                <tbody>
                  <fdev-tr class="hover-tr">
                    <fdev-td>环境中文名：{{ modelEvnMessage.env }}</fdev-td>
                    <fdev-td
                      >环境英文名：{{ modelEvnMessage.env_name_en }}</fdev-td
                    >
                    <fdev-td class="desc-td">
                      环境描述信息：{{ modelEvnMessage.env_desc }}
                    </fdev-td>
                  </fdev-tr>
                  <fdev-tr class="hover-tr">
                    <fdev-td>实体中文名：{{ modelEvnMessage.model }}</fdev-td>
                    <fdev-td
                      >实体英文名：{{ modelEvnMessage.model_name_en }}</fdev-td
                    >
                    <fdev-td class="desc-td">
                      实体描述信息：{{ modelEvnMessage.model_desc }}
                    </fdev-td>
                  </fdev-tr>
                  <fdev-tr class="hover-tr">
                    <fdev-td>实体作用域：{{ modelEvnMessage.scope }}</fdev-td>
                    <fdev-td
                      >实体版本：{{ modelEvnMessage.model_version }}</fdev-td
                    >
                  </fdev-tr>
                </tbody>
              </fdev-markup-table>
              <fdev-table
                :data="modelEvn"
                :columns="keyColums"
                hide-bottom
                :pagination.sync="childPagination"
              >
                <template v-slot:body="props">
                  <fdev-tr :props="props">
                    <fdev-td
                      :props="props"
                      v-for="col in keyColums"
                      :key="col.name"
                      :class="[
                        col.name === 'value' ? 'desc' : '',
                        'q-td text-left'
                      ]"
                    >
                      <span
                        v-if="col.name === 'require' || col.name === 'type'"
                      >
                        {{ props.row[col.name] === '1' ? '是' : '否' }}
                      </span>
                      <span v-else-if="col.name === 'data_type'">
                        {{ props.row[col.name] | filterType }}
                      </span>
                      <span
                        v-else-if="
                          col.name === 'value' &&
                            props.row.data_type &&
                            props.row.data_type !== ''
                        "
                      ></span>
                      <span v-else>{{ props.row[col.name] }}</span>
                      <fdev-btn
                        dense
                        round
                        v-if="col.name === 'name_en'"
                        flat
                        v-show="
                          props.row.data_type && props.row.data_type !== ''
                        "
                        :icon="taggleIcon(props)"
                        @click="props.expand = !props.expand"
                      />
                    </fdev-td>
                  </fdev-tr>
                  <fdev-tr
                    v-if="props.row.data_type && props.row.data_type !== ''"
                    :props="props"
                    v-show="showTable(props)"
                  >
                    <fdev-td
                      colspan="100%"
                      v-if="Array.isArray(props.row.value)"
                    >
                      <div
                        class="text-left"
                        v-for="(key, index) in props.row.value"
                        :key="index"
                      >
                        <!-- <fdev-btn
                          dense
                          round
                          flat
                          :icon="key.expand ? 'arrow_drop_down' : 'arrow_drop_up' "
                          @click="toggleBtn(props.row.id, index)"
                        /> -->
                        <fdev-table
                          :data="key.greatKey"
                          :columns="grandChildColumns"
                          row-key="id"
                          flat
                          hide-bottom
                          :pagination.sync="grandChildPagination"
                        />
                      </div>
                    </fdev-td>
                    <fdev-td colspan="100%" v-else>
                      <div class="text-left">
                        <fdev-table
                          :data="props.row.greatKey"
                          :columns="grandChildColumns"
                          row-key="id"
                          flat
                          hide-bottom
                          :pagination.sync="grandChildPagination"
                        />
                      </div>
                    </fdev-td>
                  </fdev-tr>
                </template>
              </fdev-table>
            </div>
          </fdev-page>
        </fdev-page-container>
      </fdev-layout>
    </fdev-dialog>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import VerifycodeDialog from '../components/VerifycodeDialog';
import { mapActions, mapState } from 'vuex';
import { successNotify, getIdsFormList } from '@/utils/utils';
import { setPagination, getPagination } from './setting';
export default {
  name: 'ModelTab',
  components: {
    Loading,
    VerifycodeDialog
  },
  props: {
    role: {
      type: Boolean
    },
    id: {
      type: String
    }
  },
  data() {
    return {
      deleteDialogOpened: false, //对话框
      deleteId: '',
      deleteEnvName: '',
      loading: false,
      temporaryModel: {}, // 每次selected选择以后 暂存的实体
      selectedModel: {
        name_en: '',
        name_cn: '',
        desc: '',
        id: ''
      }, // 每次选中的实体
      option: [],
      columns: [
        { name: 'env', label: '环境中文名', field: 'env', align: 'left' },
        {
          name: 'env_name_en',
          label: '环境英文名',
          field: 'env_name_en',
          align: 'left'
        },
        { name: 'model', label: '实体', field: 'model', align: 'left' },
        { name: 'desc', label: '描述', field: 'desc', align: 'left' },
        { name: 'btn', label: '操作', align: 'center' }
      ],
      keyColums: [
        { name: 'name_en', label: '属性字段', field: 'name_en', align: 'left' },
        {
          name: 'name_cn',
          label: '属性中文名',
          field: 'name_cn',
          align: 'left'
        },
        { name: 'value', label: '属性值', field: 'value', align: 'left' },
        { name: 'require', label: '是否必填', field: 'require', align: 'left' },
        { name: 'desc', label: '描述', field: 'desc', align: 'left' },
        {
          name: 'data_type',
          label: '属性类型',
          field: 'data_type',
          align: 'left'
        },
        {
          name: 'type',
          label: '是否应用独有',
          field: 'type',
          align: 'left'
        }
      ],
      grandChildColumns: [
        { name: 'name', label: '属性字段', field: 'name', align: 'left' },
        { name: 'desc', label: '描述信息', field: 'desc', align: 'left' },
        { name: 'value', label: '属性值', field: 'value', align: 'left' },
        {
          name: 'required',
          label: '是否必填',
          field: row => (row.required ? '是' : '否'),
          align: 'left'
        }
      ],
      handleShow: false,
      modelEvnMessage: {},
      modelEvn: [],
      pagination: getPagination(),
      childPagination: {
        rowsPerPage: 0
      },
      grandChildPagination: {
        rowsPerPage: 0
      },
      isManager: false
    };
  },
  computed: {
    ...mapState('environmentForm', {
      modelList: 'modelList', //实体列表
      modelEvnList: 'modelEvnList' // 某个实体下的一套环境
    }),
    ...mapState('appForm', ['appData']),
    ...mapState('user', ['currentUser']),
    description() {
      return `确认删除${this.selectedModel.name_en}实体下的${
        this.deleteEnvName
      }环境吗?删除后不可恢复`;
    }
  },
  filters: {
    filterType(val) {
      return val === '' || !val ? 'string' : val;
    }
  },
  watch: {
    pagination(val) {
      setPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    selectedModel(val) {
      sessionStorage.setItem(
        'selectedModel',
        JSON.stringify(this.selectedModel)
      );
    },
    'selectedModel.name_en': {
      handler: async function(val) {
        this.isManager = false;
        if (val && val.includes('private')) {
          let appEnName = val.split('_')[2];
          await this.queryAppDetail({ name_en: appEnName });
          let appDetail = this.appData[0];
          if (appDetail) {
            let userIds = getIdsFormList(
              appDetail.dev_managers.concat(appDetail.spdb_managers)
            );
            this.isManager = userIds.indexOf(this.currentUser.id) > -1;
          }
        }
      },
      deep: true
    }
  },
  methods: {
    ...mapActions('environmentForm', {
      getModelList: 'getModelList', // 获取实体列表
      getModelEvn: 'getModelEvn',
      deleteModelEvn: 'deleteModelEvn' //删除实体映射
    }),
    ...mapActions('appForm', {
      queryAppDetail: 'queryApplication'
    }),
    async getTableData(selectedModel) {
      this.temporaryModel = selectedModel;
      await this.getModelEvn({ model_id: selectedModel.id }); // 获取某个实体下的一套环境
      this.loading = false;
    },
    // 显示输入验证码的对话框
    handleDeleteDialogOpened(row) {
      const { id, env_name_en } = row;
      this.deleteDialogOpened = true;
      this.deleteId = id;
      this.deleteEnvName = env_name_en;
    },
    // 删除实体映射
    async handleDelete(inputVerifyCode) {
      await this.deleteModelEvn({
        id: this.deleteId,
        verfityCode: inputVerifyCode
      });
      successNotify('删除成功');
      this.deleteDialogOpened = false;
      this.getTableData(this.temporaryModel);
    },
    // 页面跳转
    goToManagePage(path, id, tab) {
      this.$router.push({
        path: `/envModel/modelEvnList/${path}`,
        query: {
          id: id,
          tab: tab
        }
      });
    },
    openDetail(row) {
      this.modelEvnMessage = row;
      this.modelEvn = row.variables;
      let index = 1;
      this.modelEvn.map(item => {
        if (item.json_schema && item.data_type !== '') {
          let json_schema =
            item.data_type === 'array'
              ? JSON.parse(item.json_schema).items
              : JSON.parse(item.json_schema);
          // 以下是生成table的columns
          const columnsArr = Object.keys(json_schema.properties);
          if (Array.isArray(item.value)) {
            item.value.map(val => {
              val.greatKey = columnsArr.map(col => {
                let required = false;
                try {
                  required = json_schema.required.includes(col);
                } finally {
                  return {
                    name: col,
                    desc: json_schema.properties[col].description,
                    value: val ? val[col] : '',
                    required: required
                  };
                }
              });
              val.expand = false;
            });
          } else {
            item.greatKey = columnsArr.map(col => {
              let required = false;
              try {
                required = json_schema.required.includes(col);
              } finally {
                return {
                  name: col,
                  desc: json_schema.properties[col].description,
                  value: item.value ? item.value[col] : '',
                  required: required
                };
              }
            });
          }
          item.index = index++;
        }
      });
      this.handleShow = true;
    },
    showTable(props) {
      if (props.row.index && props.row.index === 1) {
        return !props.expand;
      } else if (props.row.index && props.row.index !== 1) {
        return props.expand;
      }
    },
    taggleIcon(props) {
      if (props.row.index && props.row.index === 1) {
        return !props.expand ? 'arrow_drop_up' : 'arrow_drop_down';
      } else if (props.row.index && props.row.index !== 1) {
        return props.expand ? 'arrow_drop_up' : 'arrow_drop_down';
      }
    },
    handelDelete(id) {
      this.$q
        .dialog({
          title: '确认删除',
          message:
            '您确定删除该实体与该环境的映射吗?做此操作前请先核对影响范围。',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteModelEvn({ id: id });
          successNotify('删除成功');
          this.getTableData(this.temporaryModel);
        });
    },
    userFilterChoice(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.option = this.modelList.filter(v => {
          return (
            v.name_en.toLowerCase().indexOf(needle) > -1 ||
            v.name_cn.indexOf(val) > -1
          );
        });
      });
    },
    toggleBtn(id, index) {
      this.modelEvn.map(item => {
        if (item.id === id) {
          this.$set(item.value[index], 'expand', !item.value[index].expand);
        }
      });
    }
  },
  async created() {
    this.loading = true;
    await this.getModelList();
    if (this.id) {
      this.selectedModel = this.modelList.find(item => item.id === this.id);
    } else {
      this.selectedModel = this.modelList[0];
      if (sessionStorage.getItem('selectedModel')) {
        this.selectedModel = JSON.parse(
          sessionStorage.getItem('selectedModel')
        );
      }
    }
    this.getTableData(this.selectedModel);
    this.option = this.modelList;
  }
};
</script>

<style lang="stylus" scoped>
.btn
  width 30px
.select
  width 250px
.td_desc
  box-sizing border-box
  width 30%
  max-width 130px
  overflow hidden
  text-overflow ellipsis
.table
  max-width 100vw
.tr_bac td
  background #fff
  border none!important
.tr_bac:hover
  background #fff
.desc
  box-sizing border-box
  width 30%
  max-width 130px
  overflow hidden
  text-overflow ellipsis
.q-dialog__inner--minimized > div
  max-width 1200px
.q-layout-container
  min-height 600px
  height 0
.q-field
  display inline-block
.span-msg
  padding 0 30px
  overflow hidden
  display inline-block
  vertical-align  text-bottom!important
.span-desc
  white-space nowrap
  max-width 400px
  text-overflow ellipsis
.hover-tr
  background #fff
.hover-tr:hover
  background #fff
.hover-tr td
  border none
.desc-td
  max-width 320px
  overflow hidden
  text-overflow ellipsis
.q-table--col-auto-width
  padding-right 10px!important
</style>
