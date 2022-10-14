<template>
  <f-block>
    <Loading :visible="loading" class="bg-white">
      <fdev-table
        :data="tableData"
        :columns="columns"
        :pagination.sync="pagination"
        noExport
        title="实体信息变动管理列表"
        titleIcon="list_s_f"
        ref="table"
        :on-search="filterWithData"
        @request="init"
        :visible-columns="visibleCols"
        class="my-sticky-column-table"
      >
        <template v-slot:top-bottom>
          <f-formitem
            class="col-4"
            bottom-page
            label-style="padding-left:80px"
            label="实体"
          >
            <fdev-select
              use-input
              clearable
              :value="terms.model"
              @input="saveModel($event)"
              :options="modelOptions"
              option-label="name_en"
              option-value="name_en"
              @filter="filterModel"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name_en">{{
                      scope.opt.name_en
                    }}</fdev-item-label>
                    <fdev-item-label :title="scope.opt.name_cn" caption>
                      {{ scope.opt.name_cn }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem
            class="col-4"
            bottom-page
            label-style="padding-left:70px"
            label="申请人"
          >
            <fdev-select
              use-input
              clearable
              @filter="filterUser"
              :options="userOptions"
              option-label="user_name_cn"
              option-value="user_name_en"
              ref="terms.appllyUser"
              :value="terms.appllyUser"
              @input="saveAppllyUser($event)"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.user_name_cn">
                      {{ scope.opt.user_name_cn }}
                    </fdev-item-label>
                    <fdev-item-label :title="scope.opt.user_name_en" caption>
                      {{ scope.opt.user_name_en }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem
            class="col-4"
            bottom-page
            label-style="padding-left:80px"
            label="状态"
          >
            <fdev-select
              use-input
              clearable
              :value="terms.status"
              @input="saveStatus($event)"
              :options="statusOptions"
              option-label="label"
              option-value="value"
              map-options
              emit-value
            />
          </f-formitem>
        </template>
        <template v-slot:body-cell-modelNameCn="props">
          <fdev-td class="text-ellipsis">
            <router-link
              class="link"
              :to="`/envModel/modelList/${props.row.modelId}`"
              :title="props.value"
              v-if="props.value"
              >{{ props.value }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <div v-else title="-">-</div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-modelNameEn="props">
          <fdev-td class="text-ellipsis">
            <router-link
              class="link"
              :to="`/envModel/modelList/${props.row.modelId}`"
              :title="props.value"
              v-if="props.value"
              >{{ props.value }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <div v-else title="-">-</div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-applyUsername="props">
          <fdev-td class="text-ellipsis">
            <router-link
              v-if="props.row.applyUserId"
              :to="{
                path: `/user/list/${props.row.applyUserId}`
              }"
              class="link"
              :title="props.row.applyUsername"
              >{{ props.value }}
            </router-link>
            <span v-else :title="props.row.applyUsername || '-'">
              {{ props.row.applyUsername || '-' }}
            </span>
          </fdev-td>
        </template>

        <template v-slot:body-cell-status="props">
          <fdev-td class="text-ellipsis" :title="props.value | statusFilter">
            {{ props.value | statusFilter }}
          </fdev-td>
        </template>

        <template v-slot:body-cell-type="props">
          <fdev-td
            auto-width
            :title="props.value === 'insert' ? '新增' : '更新'"
          >
            {{ props.value === 'insert' ? '新增' : '更新' }}
          </fdev-td>
        </template>

        <template v-slot:body-cell-btn="props">
          <fdev-td auto-width>
            <div class="q-gutter-x-sm">
              <fdev-btn
                :color="
                  props.row.applyUserId === currentUser.id &&
                  props.row.status === 'checking'
                    ? 'primary'
                    : 'grey'
                "
                :disable="
                  !(
                    props.row.applyUserId === currentUser.id &&
                    props.row.status === 'checking'
                  )
                "
                flat
                label="核对"
                @click="handleCheckDialogOpen(props.row)"
              />
              <!-- 仅申请人可点，状态(status)为checking -->
              <fdev-btn
                :color="
                  isManager && props.row.status === 'overtime' ? 'teal' : 'grey'
                "
                :disable="!(isManager && props.row.status === 'overtime')"
                flat
                label="恢复"
                @click="recover(props.row)"
              />
              <!-- 恢复：仅环境配置管理员可点，状态（status）为overtime -->
              <fdev-btn
                :color="
                  isManager && props.row.status === 'checking' ? 'red' : 'grey'
                "
                :disable="
                  !(
                    (isManager || props.row.applyUserId === currentUser.id) &&
                    props.row.status === 'checking'
                  )
                "
                flat
                label="撤销"
                @click="cancel(props.row)"
              />
              <!-- 取消：环境配置管理员、申请人,类型（type）为更新(update), 状态(status)为checking-->
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>

    <f-dialog v-model="checkDialogOpened" right f-sc title="核对实体信息">
      <!-- <Dialog v-model="checkDialogOpened" title="核对实体信息"> -->
      <fdev-stepper v-model="step" class="dialog-width" vertical animated>
        <div class="q-px-md row">
          <f-formitem class="col-6" label="实体:">
            {{ selected.modelNameCn }}
          </f-formitem>
          <f-formitem class="col-6" label="环境:">
            {{ selected.envNameCn }}
          </f-formitem>
        </div>
        <fdev-step :name="1" title="核对实体环境映射值变动" :done="step > 1">
          <fdev-table
            :columns="modelEnvChangeColumns"
            :data="compareData.variables"
            titleIcon="list_s_f"
            noExport
            row-key="id"
            no-select-cols
          >
            <template v-slot:body-cell-oldValue="props">
              <fdev-td>
                <div v-if="props.row.data_type === 'array'" class="row">
                  <Chip
                    v-for="(old, i) in props.row.oldValue"
                    :key="i"
                    :data="old"
                    :name="props.row.name_en + i"
                  />
                </div>
                <div v-else-if="props.row.data_type === 'object'">
                  <Chip :data="props.row.oldValue" :name="props.row.name_en" />
                </div>
                <div
                  v-else
                  :title="props.row.oldValue || '-'"
                  class="text-ellipsis"
                >
                  {{ props.row.oldValue || '-' }}
                </div>
              </fdev-td>
            </template>
            <template v-slot:body-cell-newValue="props">
              <fdev-td>
                <div v-if="props.row.data_type === 'array'" class="row">
                  <Chip
                    v-for="(old, i) in props.row.newValue"
                    :key="i"
                    :data="old"
                    :name="props.row.name_en + i"
                  />
                </div>
                <div v-else-if="props.row.data_type === 'object'">
                  <Chip :data="props.row.newValue" :name="props.row.name_en" />
                </div>
                <div
                  v-else
                  :title="props.row.newValue || '-'"
                  class="text-ellipsis"
                >
                  {{ props.row.newValue || '-' }}
                </div>
              </fdev-td>
            </template>
          </fdev-table>
          <fdev-stepper-navigation>
            <fdev-btn
              @click="step = 2"
              color="primary"
              label="核对无误，下一步"
            />
          </fdev-stepper-navigation>
        </fdev-step>
        <fdev-step :name="2" title="核对变动影响范围" :done="step > 2">
          <f-formitem fullWidth label="影响范围清单">
            <fdev-btn label="下载" flat color="primary" @click="download" />
          </f-formitem>
          <fdev-stepper-navigation>
            <fdev-btn
              @click="step = 3"
              class="q-mr-md"
              color="primary"
              label="完成核对"
            />
            <fdev-btn @click="step = 1" color="primary" label="上一步" flat />
          </fdev-stepper-navigation>
        </fdev-step>
        <fdev-step :name="3" title="确认" :done="step > 3">
          <fdev-stepper-navigation>
            <fdev-btn
              @click="handleCheckDialogModel"
              color="primary"
              label="确认无误"
              :loading="globalLoading['environmentForm/finish']"
            />
            <fdev-btn @click="step = 2" color="primary" label="上一步" flat />
          </fdev-stepper-navigation>
        </fdev-step>
      </fdev-stepper>
    </f-dialog>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import {
  modelMessageStatus,
  statusOptions,
  modelMessageColumns,
  modelEnvChangeColumns
} from '../../utils/constants';
import { successNotify } from '@/utils/utils';
import Chip from '../../components/Chip';
export default {
  name: 'ModelMessage',
  components: { Loading, Chip },
  data() {
    return {
      modelEnvChangeColumns,
      step: 1,
      loading: false,
      checkDialogOpened: false,
      tableData: [],
      userOptions: [],
      modelOptions: [],
      selected: {},
      statusOptions: statusOptions,
      filterListModel: {},
      pagination: {},
      columns: modelMessageColumns()
    };
  },
  watch: {
    pagination(val) {
      this.saveCurrentPage(val);
    },
    'terms.model'(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.filterWithData();
      }
    },
    'terms.appllyUser'(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.filterWithData();
      }
    },
    'terms.status'(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.filterWithData();
      }
    }
  },
  computed: {
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('environmentForm', [
      'compareData',
      'appInfo',
      'modelList',
      'modelMessageList'
    ]),
    isManager() {
      return this.currentUser.role.some(role => role.name === '环境配置管理员');
    },
    visibleOptions() {
      const arr = this.columns.slice(0);
      return arr.splice(0, arr.length - 1);
    },
    ...mapState('userActionSaveEnv/modelMessage', [
      'visibleCols',
      'currentPage',
      'terms'
    ])
  },
  filters: {
    statusFilter(val) {
      return modelMessageStatus[val] || '-';
    }
  },
  methods: {
    ...mapActions('user', ['fetch']),
    ...mapActions('environmentForm', [
      'cancelModelMessage',
      'updateModelMessage',
      'finish',
      'downloadAppInfo',
      'compare',
      'getModelList',
      'queryModelMessage'
    ]),
    ...mapMutations('userActionSaveEnv/modelMessage', [
      'saveVisibleColumns',
      'saveCurrentPage',
      'saveModel',
      'saveAppllyUser',
      'saveStatus'
    ]),
    filterUser(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.userOptions = this.userList.filter(
          v =>
            v.user_name_cn.indexOf(needle) > -1 ||
            v.user_name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    filterModel(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.modelOptions = this.modelList.filter(v => {
          return (
            v.name_en.toLowerCase().indexOf(needle) > -1 ||
            v.name_cn.indexOf(val) > -1
          );
        });
      });
    },
    /* 
      处理核对弹窗
      1、保存点击的数据；
      2、发送compare接口查询变动的内容；
    */
    async handleCheckDialogOpen(data) {
      this.selected = data;
      await this.compare({
        variables: data.variables,
        id: data.id,
        type: data.type,
        model_env_id: data.modelEnvId,
        desc: data.desc
      });
      this.step = 1;
      if (this.compareData && this.compareData.variables) {
        this.checkDialogOpened = true;
      } else if (this.compareData.code != 'AAAAAAA') {
        return;
      }
    },
    /* 点击查询时，保存查询的参数，分页时携带查询的参数；没有点击查询则不保存 */
    async filterWithData() {
      const { appllyUser, status, model } = this.terms;
      this.filterListModel = {
        status,
        apply_user_id: appllyUser ? appllyUser.id : '',
        model_id: model ? model.id : ''
      };
      await this.init();
    },
    async handleCheckDialogModel() {
      await this.finish({
        id: this.selected.id,
        type: this.selected.type,
        model_env_id: this.selected.modelEnvId,
        model_id: this.selected.modelId,
        env_id: this.selected.envId,
        env_name_en: this.selected.envNameEn,
        env_name_cn: this.selected.envNameCn,
        desc: this.selected.desc,
        apply_user_id: this.selected.applyUserId,
        variables: this.selected.variables
      });
      this.checkDialogOpened = false;
      successNotify('成功');
      this.init();
    },
    async download() {
      await this.downloadAppInfo({
        model_name_en: this.selected.modelNameEn,
        model_name_cn: this.selected.modelNameCn,
        env_name_en: this.selected.envNameEn,
        env_name_cn: this.selected.envNameCn,
        variables: this.compareData.variables,
        apply_username: this.selected.applyUsername
      });
      window.open(this.appInfo);
    },
    recover({ id, modelId, applyEmail, envId }) {
      this.$q
        .dialog({
          title: `恢复实体信息变动申请`,
          message: `您确定恢复实体信息变动申请吗？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.updateModelMessage({
            id: id,
            model_id: modelId,
            apply_email: applyEmail,
            env_id: envId
          });
          successNotify('已恢复');
          this.init();
        });
    },
    cancel({ id }) {
      this.$q
        .dialog({
          title: `取消实体信息变动申请`,
          message: `您确定取消实体信息变动申请吗？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.cancelModelMessage({
            id: id
          });
          successNotify('已取消');
          this.init();
        });
    },
    async init(props) {
      if (props && props.pagination) {
        const { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }

      this.loading = true;
      await this.queryModelMessage({
        page: this.pagination.page,
        per_page: this.pagination.rowsPerPage,
        ...this.filterListModel
      });
      this.pagination.rowsNumber = this.modelMessageList.total;
      this.tableData = this.modelMessageList.list;
      this.loading = false;
    }
  },
  async created() {
    /* 
      1、页面初始化默认查询当前用户的数据；
     */
    this.pagination = this.currentPage;
    this.getModelList().then(() => {
      this.modelOptions = this.modelList;
    });
    await this.fetch();
    this.userOptions = this.userList;
    if (this.terms.appllyUser === '') {
      let item = this.userList.find(user => user.id === this.currentUser.id);
      this.saveAppllyUser(item);
    }
    this.filterWithData();
  },
  mounted() {
    if (!this.visibleCols.toString() || this.visibleCols.length <= 2) {
      this.saveVisibleColumns([
        'modelNameCn',
        'modelNameEn',
        'envNameCn',
        'envNameEn',
        'status',
        'type',
        'applyUsername',
        'createTime',
        'id',
        'btn'
      ]);
    }
  }
};
</script>

<style lang="stylus" scoped>

.input
  max-width 330px
  min-width 230px
.table-wrapper
  width 100%
  overflow-x auto
.table
  width 100%
  border-radius 5px
  border-collapse collapse
  border 1px solid #bdbdbd
  td, th
    height 40px
    text-align center
    border 1px solid #bdbdbd
    color #616161
    min-width 80px
  tr:nth-of-type(2n)
    td, th
      background #999999
.align-center
  align-items center
.dialog-width
  width 1000px
  >>> td
    max-width 229px
// .my-sticky-column-table >>>
//   th:first-child,td:first-child,td:last-child,th:last-child
//     background-color #fff
//     opacity 1
  th:first-child,th:last-child
    color #9c9a9a
  th:first-child,td:first-child
    position sticky
    left 0
    z-index 1
  td:last-child,th:last-child
    position sticky
    right 0
    z-index 1
</style>
