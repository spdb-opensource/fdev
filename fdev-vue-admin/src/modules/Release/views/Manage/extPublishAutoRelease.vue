<template>
  <div>
    <div class="row q-mb-md q-pt-xs justify-end">
      <div v-if="isAutoPublishEntry">
        <fdev-btn
          ficon="add"
          :disable="appNoteOptions && appNoteOptions.length === 0"
          normal
          label="新增批量任务"
          @click="openAddDialog"
        />
        <fdev-tooltip v-if="appNoteOptions && appNoteOptions.length === 0">
          {{ appNotetipTitle }}
        </fdev-tooltip>
      </div>
      <div v-if="!isAutoPublishEntry">
        <fdev-btn
          ficon="version"
          v-if="!isAutoPublishEntry"
          :disable="
            !compareTime() ||
              !(releaseDetail.can_operation || isKaDianManager) ||
              (appOptions && appOptions.length === 0)
          "
          normal
          label="获取批量任务"
          @click="openGetDialog"
        />
        <fdev-tooltip
          v-if="
            !compareTime() ||
              !(releaseDetail.can_operation || isKaDianManager) ||
              (appOptions && appOptions.length === 0)
          "
        >
          <span v-if="!compareTime()">
            当前变更已过期
          </span>
          <span
            v-if="
              compareTime() && !(releaseDetail.can_operation || isKaDianManager)
            "
          >
            请联系当前变更所属组成员的第三层级组及其子组的投产管理员
          </span>
          <span
            v-if="
              compareTime() &&
                (releaseDetail.can_operation || isKaDianManager) &&
                (appOptions && appOptions.length === 0)
            "
          >
            当前变更下无应用
          </span>
        </fdev-tooltip>
      </div>
    </div>
    <div v-if="listData && listData.length > 0">
      <fdev-table
        titleIcon="list_s_f"
        v-for="(item, index) in listData"
        :key="index"
        :title="item.application_name_en | titleName"
        :data="item.list"
        class="q-mb-md"
        :columns="extPublishColumns"
        row-key="id"
        no-export
        no-select-cols
      >
        <template v-slot:body-cell-type="props">
          <fdev-td class="text-ellipsis" :title="props.row.type | taskName">
            <span
              class="text-blue-8 cursor-pointer"
              @click="openDetailDialog(props.row)"
              >{{ props.row.type | taskName }}</span
            >
          </fdev-td>
        </template>
        <template v-slot:body-cell-jobGroup="props">
          <fdev-td
            class="text-ellipsis"
            :title="props.row.jobGroup | jobGroupFiler"
          >
            <span>{{ props.row.jobGroup | jobGroupFiler }}</span>
          </fdev-td>
        </template>
        <template v-slot:body-cell-btn="props">
          <fdev-td auto-width>
            <div class="q-gutter-x-sm row no-wrap">
              <template v-if="!isAutoPublishEntry">
                <span v-if="props.row.batch_no && props.row.batch_no !== '1'">
                  <fdev-btn
                    label="上移"
                    @click="
                      move(listData[index].list, props.row.batch_no - 1, -1)
                    "
                    flat
                    :disable="
                      !compareTime() ||
                        !(releaseDetail.can_operation || isKaDianManager)
                    "
                  />
                  <fdev-tooltip
                    v-if="
                      !compareTime() ||
                        !(releaseDetail.can_operation || isKaDianManager)
                    "
                  >
                    <span v-if="!compareTime()">
                      当前变更已过期
                    </span>
                    <span
                      v-if="
                        compareTime() &&
                          !(releaseDetail.can_operation || isKaDianManager)
                      "
                    >
                      请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                    </span>
                  </fdev-tooltip>
                </span>
                <div
                  class="text-grey-1 sepreator"
                  v-if="props.row.batch_no && props.row.batch_no !== '1'"
                >
                  |
                </div>
                <span
                  v-if="
                    props.row.batch_no &&
                      item.list &&
                      Number(props.row.batch_no) !== listData[index].list.length
                  "
                >
                  <fdev-btn
                    label="下移"
                    :disable="
                      !compareTime() ||
                        !(releaseDetail.can_operation || isKaDianManager)
                    "
                    @click="
                      move(listData[index].list, props.row.batch_no - 1, 1)
                    "
                    flat
                  />
                  <fdev-tooltip
                    v-if="
                      !compareTime() ||
                        !(releaseDetail.can_operation || isKaDianManager)
                    "
                  >
                    <span v-if="!compareTime()">
                      当前变更已过期
                    </span>
                    <span
                      v-if="
                        compareTime() &&
                          !(releaseDetail.can_operation || isKaDianManager)
                      "
                    >
                      请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                    </span>
                  </fdev-tooltip>
                </span>
                <div
                  class="text-grey-1 sepreator"
                  v-if="
                    props.row.batch_no &&
                      item.list &&
                      Number(props.row.batch_no) !== listData[index].list.length
                  "
                >
                  |
                </div>
              </template>
              <span>
                <fdev-btn
                  label="编辑"
                  flat
                  :disable="
                    !compareTime() ||
                      !(releaseDetail.can_operation || isKaDianManager)
                  "
                  @click="edit(props.row)"
                />
                <fdev-tooltip
                  v-if="
                    !compareTime() ||
                      !(releaseDetail.can_operation || isKaDianManager)
                  "
                >
                  <span v-if="!compareTime()">
                    当前变更已过期
                  </span>
                  <span
                    v-if="
                      compareTime() &&
                        !(releaseDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                </fdev-tooltip>
              </span>
              <div class="text-grey-1 sepreator">|</div>
              <span>
                <fdev-btn
                  label="删除"
                  flat
                  :disable="
                    !compareTime() ||
                      !(releaseDetail.can_operation || isKaDianManager)
                  "
                  @click="deleteItem(props.row)"
                />
                <fdev-tooltip
                  v-if="
                    !compareTime() ||
                      !(releaseDetail.can_operation || isKaDianManager)
                  "
                >
                  <span v-if="!compareTime()">
                    当前变更已过期
                  </span>
                  <span
                    v-if="
                      compareTime() &&
                        !(releaseDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                </fdev-tooltip>
              </span>
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </div>
    <div class="text-center q-mt-md" v-else>
      <f-image name="no_data" />
    </div>
    <ExtPublishDialog
      v-if="extDialogModel"
      v-model="extDialogModel"
      :title="title"
      @click="handleExtPublishEvent"
      :options="appNoteOptions"
      :releaseNodeName="releaseNodeName"
      :noteId="this.note_id"
      :rowData="rowData"
    />
    <GetExtPublishListDialog
      v-if="getExtListDialogModel"
      v-model="getExtListDialogModel"
      @click="handleGetExtPublishListEvent"
      :releaseNodeName="releaseNodeName"
      :prodId="prodId"
      :options="appOptions"
      :listData="listData"
    />
    <ExtTaskDetailDialog
      v-if="extDetailDialogModel"
      v-model="extDetailDialogModel"
      @click="handleExtDetailEvent"
      :detail="detail"
    />
  </div>
</template>
<script>
import { mapActions, mapState, mapGetters } from 'vuex';
import { successNotify, isValidReleaseDate } from '@/utils/utils';
import {
  extPublishColumns,
  typeOptions,
  groupOptions
} from '../../utils/constants';
import ExtPublishDialog from './components/extPublishDialog';
import GetExtPublishListDialog from './components/getExtPublishListDialog';
import ExtTaskDetailDialog from './components/extTaskDetailDialog';
export default {
  name: 'ExtPublishAutoRelease',
  components: {
    ExtPublishDialog,
    GetExtPublishListDialog,
    ExtTaskDetailDialog
  },
  filters: {
    titleName(val) {
      return '应用：' + val;
    },
    taskName(val) {
      return typeOptions.find(v => v.value === val)
        ? typeOptions.find(v => v.value === val).label
        : '-';
    },
    jobGroupFiler(val) {
      return groupOptions.find(v => v.value === val)
        ? groupOptions.find(v => v.value === val).label
        : '-';
    }
  },
  computed: {
    ...mapState('releaseForm', {
      batchTaskList: 'batchTaskList',
      applyList: 'applyList',
      changeApllication: 'changeApllication',
      batchAppIdByNoteIdList: 'batchAppIdByNoteIdList',
      releaseDetail: 'releaseDetail'
    }),
    // 判断当前用户是否为卡点管理员角色
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),
    // 按钮悬浮提示语
    appNotetipTitle() {
      return !this.applyList || this.applyList.length === 0
        ? '当前投产窗口下无应用'
        : this.batchAppIdByNoteIdList &&
          this.batchAppIdByNoteIdList.length > 0 &&
          (!this.appNoteOptions || this.appNoteOptions.length === 0)
        ? '当前投产窗口下所有应用已添加批量任务'
        : '';
    }
  },
  data() {
    return {
      isAutoPublishEntry: true, // 是否从发布说明进入
      title: '新增批量任务',
      extDialogModel: false,
      getExtListDialogModel: false,
      extDetailDialogModel: false,
      detail: '',
      note_id: '',
      rowData: {},
      prodId: '',
      appOptions: [], // 变更应用列表
      appNoteOptions: [], // 发布说明应用列表
      releaseNodeName: '',
      extPublishColumns: extPublishColumns,
      listData: []
    };
  },
  methods: {
    ...mapActions('releaseForm', {
      queryBatchTaskList: 'queryBatchTaskList',
      queryApply: 'queryApply',
      queryReleaseNodeDetail: 'queryReleaseNodeDetail',
      getChangeApplications: 'getChangeApplications',
      deteleBatchTask: 'deteleBatchTask',
      updateNoteBatchNo: 'updateNoteBatchNo',
      queryBatchAppIdByNoteId: 'queryBatchAppIdByNoteId'
    }),
    openAddDialog() {
      this.title = '新增批量任务';
      this.extDialogModel = true;
    },
    handleExtPublishEvent(val) {
      this.extDialogModel = false;
      if (!val) {
        successNotify('操作成功');
        this.init();
      }
    },
    edit(val) {
      this.title = '编辑批量任务';
      this.rowData = val;
      this.extDialogModel = true;
    },
    deleteItem(val) {
      this.$q
        .dialog({
          title: '删除批量任务',
          message: '确定删除该批量任务吗?',
          cancel: true
        })
        .onOk(async () => {
          //todo 删除接口
          const params = {
            id: val.id,
            application_id: val.application_id
          };
          if (this.isAutoPublishEntry) {
            params.flag = '2';
          } else {
            params.flag = '1';
            params.prod_id = this.prodId;
          }
          await this.deteleBatchTask(params);
          successNotify('删除成功');
          this.init();
        });
    },
    openGetDialog() {
      this.getExtListDialogModel = true;
    },
    handleGetExtPublishListEvent(val) {
      this.getExtListDialogModel = false;
      if (!val) {
        successNotify('操作成功');
        this.init();
      }
    },
    openDetailDialog(val) {
      this.detail = val.batchInfo;
      this.extDetailDialogModel = true;
    },
    handleExtDetailEvent() {
      this.extDetailDialogModel = false;
      this.detail = '';
    },
    async move(data, index, act) {
      const params = [data[index + act].id, data[index].id];
      await this.updateNoteBatchNo({
        ids: params,
        application_id: data[index + act].application_id
      });
      successNotify('移动成功');
      this.init();
    },
    order(data) {
      const table = data.list;
      let exitBatchNoList = table.filter(data => {
        return !!data.batch_no;
      });
      exitBatchNoList = exitBatchNoList.sort((a, b) => {
        return a.batch_no - b.batch_no;
      });
      return { ...data, list: exitBatchNoList };
    },
    async init() {
      let params = {
        note_id: '',
        prod_id: ''
      };
      if (this.isAutoPublishEntry) {
        params.note_id = this.note_id;
      } else {
        params.prod_id = this.prodId;
      }
      await this.queryBatchTaskList(params);
      this.listData = [];
      if (this.isAutoPublishEntry) {
        this.listData = this.batchTaskList;
      } else {
        this.listData = this.batchTaskList.map(item => {
          return { ...this.order(item) };
        });
      }
    },
    compareTime() {
      return isValidReleaseDate(this.$q.sessionStorage.getItem('changeTime'));
    }
  },
  async created() {
    this.releaseNodeName = this.$q.sessionStorage.getItem('release_node_name');
    await this.queryReleaseNodeDetail({
      release_node_name: this.releaseNodeName
    });
    if (this.$route.path.split('/')[2] === 'autoReleaseNoteDetail') {
      this.extPublishColumns = extPublishColumns;
      this.note_id = this.$route.params.id;
      this.isAutoPublishEntry = true;
      this.init();
      let releaseApps = [];
      await this.queryApply(this.releaseNodeName);
      releaseApps = this.applyList.map(v => {
        v.application_name_en = v.app_name_en;
        v.application_name_cn = v.app_name_zh;
        return v;
      });
      await this.queryBatchAppIdByNoteId({
        release_node_name: this.releaseNodeName,
        note_id: this.note_id
      });
      if (
        this.batchAppIdByNoteIdList &&
        this.batchAppIdByNoteIdList.length > 0
      ) {
        this.appNoteOptions = releaseApps.filter(v => {
          return !this.batchAppIdByNoteIdList.find(
            item => v.application_id === item
          );
        });
      } else {
        this.appNoteOptions = releaseApps;
      }
    } else {
      this.extPublishColumns = [
        {
          name: 'batch_no',
          label: '序号',
          field: 'batch_no'
        },
        ...extPublishColumns
      ];
      this.isAutoPublishEntry = false;
      this.prodId = this.$route.params.id;
      this.init();
      await this.getChangeApplications({ prod_id: this.prodId });
      this.appOptions = this.changeApllication.map(v => {
        v.application_name_en = v.app_name_en;
        v.application_name_cn = v.app_name_zh;
        return v;
      });
    }
  }
};
</script>
<style lang="stylus" scoped>
.sepreator
  line-height 32px
</style>
