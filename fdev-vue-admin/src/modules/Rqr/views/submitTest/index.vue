<template>
  <f-block>
    <fdev-table
      :data="tableData"
      :columns="columns"
      row-key="id"
      :pagination.sync="pagination"
      @request="pageTableRequest"
      :loading="loading"
      titleIcon="list_s_f"
      title="提测单列表"
      class="my-sticky-column-table"
      :on-search="queryTestList"
      no-select-cols
      noExport
    >
      <template v-slot:top-right>
        <fdev-btn
          normal
          label="高级搜索"
          @click="moreSearch = true"
          ficon="search"
        />
        <span>
          <fdev-tooltip v-if="flagInfo.code === '2'" position="top">
            {{ flagInfo.msg }}
          </fdev-tooltip>
          <fdev-btn
            v-if="flagInfo.code === '0' || flagInfo.code === '2'"
            :disable="flagInfo.code === '2' ? true : false"
            normal
            label="新建提测单"
            ficon="add"
            @click="handleSubmitTestOpen"
          />
        </span>
      </template>
      <template v-slot:top-bottom>
        <f-formitem
          class="col-4 q-pr-md"
          label-style="width:110px"
          bottom-page
          label="提测单编号"
        >
          <fdev-input
            v-model="params.testOrder"
            @keyup.enter="queryTestList()"
            clearable
            @clear="queryTestList()"
          >
          </fdev-input>
        </f-formitem>
        <f-formitem
          label="研发单元编号/内容"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:110px"
        >
          <fdev-input
            v-model="params.implKey"
            @keyup.enter="queryTestList()"
            clearable
            @clear="queryTestList()"
          >
          </fdev-input>
        </f-formitem>
        <f-formitem
          label="需求名称/编号"
          class="col-4"
          bottom-page
          label-style="width:110px"
        >
          <fdev-input
            v-model="params.demandKey"
            @keyup.enter="queryTestList()"
            clearable
            @clear="queryTestList()"
          >
          </fdev-input>
        </f-formitem>
        <f-formitem
          label="小组"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:110px"
        >
          <fdev-select
            use-input
            ref="group"
            v-model="params.group"
            @input="queryTestList"
            :options="groupOptions"
            @filter="groupInputFilter"
            option-label="name"
            option-value="label"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.name">{{
                    scope.opt.name
                  }}</fdev-item-label>
                  <fdev-item-label :title="scope.opt.label" caption>
                    {{ scope.opt.label }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem
          label="状态"
          class="col-4 q-pr-md"
          bottom-page
          label-style="width:110px"
        >
          <fdev-select
            ref="status"
            multiple
            v-model="params.status"
            @input="queryTestList()"
            :options="testStatusOptions"
          >
          </fdev-select>
        </f-formitem>
        <f-formitem
          class="col-4"
          label-style="width:110px"
          bottom-page
          label="创建人"
        >
          <fdev-select
            use-input
            @filter="createIdsFilterUser"
            :options="createIdsOptions"
            option-label="user_name_cn"
            option-value="id"
            v-model="params.creatorId"
            @input="queryTestList()"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label :title="scope.opt.user_name_cn">{{
                    scope.opt.user_name_cn
                  }}</fdev-item-label>
                  <fdev-item-label
                    caption
                    :title="`${scope.opt.user_name_en}--${scope.opt.groupName}`"
                  >
                    {{ scope.opt.user_name_en }}--{{ scope.opt.groupName }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
      </template>
      <template v-slot:body-cell-test_order="props">
        <fdev-td :title="props.row.test_order">
          <div class="text-ellipsis">
            <router-link
              v-if="props.row.id && props.row.test_order"
              class="link"
              :to="`/rqrmn/submitTestDetail/${props.row.id}`"
            >
              {{ props.row.test_order }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.test_order }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else>-</span>
          </div>
        </fdev-td>
      </template>
      <!-- 需求编号跳转 -->
      <template v-slot:body-cell-oa_contact_no="props">
        <fdev-td :title="props.row.oa_contact_no">
          <div class="text-ellipsis">
            <router-link
              v-if="props.row.oa_contact_no"
              :to="`/rqrmn/rqrProfile/${props.row.demand_id}`"
              class="link"
            >
              {{ props.row.oa_contact_no }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.oa_contact_no }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else>-</span>
          </div>
        </fdev-td>
      </template>
      <!-- 需求名称跳转 -->
      <template v-slot:body-cell-oa_contact_name="props">
        <fdev-td :title="props.row.oa_contact_name">
          <div class="text-ellipsis">
            <router-link
              v-if="props.row.oa_contact_name"
              :to="`/rqrmn/rqrProfile/${props.row.demand_id}`"
              class="link"
            >
              {{ props.row.oa_contact_name }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.oa_contact_name }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
            <span v-else>{{ props.row.oa_contact_name || '-' }}</span>
          </div>
        </fdev-td>
      </template>
      <template v-slot:body-cell-create_user_info="props">
        <fdev-td>
          <div class="text-ellipsis" :title="props.value.user_name_cn">
            <span>
              <router-link
                v-if="props.row.create_user_info.id"
                :to="{ path: `/user/list/${props.row.create_user_info.id}` }"
                class="link"
              >
                {{ props.value.user_name_cn }}
              </router-link>
              <span v-else class="span-margin">{{
                props.value.user_name_cn || '-'
              }}</span>
            </span>
          </div>
        </fdev-td>
      </template>
      <template v-slot:body-cell-status="props">
        <fdev-td class="td-desc">
          <span :class="roundC(props.value)" class="roundS"></span>
          {{ statusFilter(props.value) }}
        </fdev-td>
      </template>
      <!-- 操作 -->
      <template v-slot:body-cell-operate="props">
        <fdev-td :auto-width="true" class="td-padding">
          <div class="q-gutter-sm row no-wrap items-center">
            <div>
              <fdev-btn
                v-if="flagInfo.code === '0' || flagInfo.code === '2'"
                :disable="flagInfo.code === '2'"
                flat
                label="复制"
                @click="handleCopySubmitTestOpen(props.row)"
              />
              <fdev-tooltip v-if="flagInfo.code === '2'" position="top">
                {{ flagInfo.msg }}
              </fdev-tooltip>
            </div>
            <div
              v-if="
                props.row.submit_flag.code === '0' ||
                  props.row.submit_flag.code === '2'
              "
            >
              <fdev-btn
                flat
                label="确认提交"
                :disable="props.row.submit_flag.code === '2'"
                @click="comfireTest(props.row)"
              />
              <fdev-tooltip
                v-if="props.row.submit_flag.code === '2'"
                position="top"
              >
                {{ props.row.submit_flag.msg }}
              </fdev-tooltip>
            </div>
            <div>
              <fdev-btn
                flat
                v-if="
                  props.row.update_flag.code === '0' ||
                    props.row.update_flag.code === '2'
                "
                :disable="props.row.update_flag.code === '2'"
                label="编辑"
                @click="handleEditSubmitTestOpen(props.row)"
              />
              <fdev-tooltip
                v-if="props.row.update_flag.code === '2'"
                position="top"
              >
                {{ props.row.update_flag.msg }}
              </fdev-tooltip>
            </div>
            <div>
              <fdev-btn
                flat
                label="删除"
                v-if="
                  props.row.delete_flag.code === '0' ||
                    props.row.delete_flag.code === '2'
                "
                :disable="props.row.delete_flag.code === '2'"
                @click="deleteTest(props.row)"
              />
              <fdev-tooltip
                v-if="props.row.delete_flag.code === '2'"
                position="top"
              >
                {{ props.row.delete_flag.msg }}
              </fdev-tooltip>
            </div>
          </div>
        </fdev-td>
      </template>
    </fdev-table>
    <f-dialog title="更多查询条件" v-model="moreSearch">
      <div class="q-gutter-y-lg">
        <f-formitem label="实施单元内容/编号">
          <fdev-input
            v-model="params.ipmpKey"
            @keyup.enter="
              queryTestList();
              moreSearch = false;
            "
            clearable
          >
          </fdev-input>
        </f-formitem>
      </div>
    </f-dialog>
  </f-block>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import {
  submitTestColums,
  testStatusOptions
} from '@/modules/Rqr/utils/constants';
import SessionStorage from '#/plugins/SessionStorage';
import {
  queryTestOrderList,
  exportApproveList,
  submitTestOrder,
  deleteTestOrder,
  queryCopyFlag
} from '@/modules/Rqr/services/methods';
import {
  resolveResponseError,
  successNotify,
  exportExcel,
  deepClone,
  formatOption
} from '@/utils/utils';
import { formatUser } from '@/modules/User/utils/model';
export default {
  name: 'submitTestList',
  data() {
    return {
      tableData: [],
      columns: submitTestColums,
      pagination: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      },
      params: {
        implKey: '',
        demandKey: '',
        group: '',
        ipmpKey: '',
        testOrder: '',
        status: [],
        creatorId: ''
      },
      loading: false,
      testStatusOptions,
      saveUserSearchData: ['params', 'pagination'],
      createIdsOptions: [],
      groupOptions: [],
      deepCloneGroups: [],
      users: [],
      flagInfo: {
        code: '0', //0 可编辑 1不展示  2 不可编辑
        msg: ''
      },
      moreSearch: false
    };
  },
  computed: {
    ...mapState('dashboard', ['userList']),
    ...mapState('userForm', {
      groupsData: 'groups'
    })
  },
  methods: {
    ...mapActions('dashboard', ['queryUserCoreData']),
    ...mapActions('userForm', ['fetchGroup']),
    getCacheData() {
      this.saveUserSearchData.forEach(x => {
        let val = SessionStorage.getItem(this.$route.fullPath + '_' + x);
        val && (this[x] = val);
      });
    },
    //翻页
    pageTableRequest(props) {
      let { page, rowsPerPage, rowsNumber } = props.pagination;
      this.pagination.page = page; //页码
      this.pagination.rowsPerPage = rowsPerPage; //每页数据大小
      this.pagination.rowsNumber = rowsNumber; //数据库数据总条数
      this.queryTestList();
    },
    //后端导出
    async handleDownload() {
      let param = {
        ...this.params,
        size: 0
      };
      let res = await resolveResponseError(() => exportApproveList(param));
      exportExcel(res);
    },
    //创建人过滤
    createIdsFilterUser(val, update, abort) {
      update(() => {
        this.createIdsOptions = this.users.filter(
          user =>
            user.user_name_cn.indexOf(val) > -1 ||
            user.user_name_en.indexOf(val) > -1
        );
      });
    },
    //小组
    groupInputFilter(val, update) {
      update(() => {
        this.groupOptions = this.deepCloneGroups.filter(
          tag => tag.label.indexOf(val) > -1
        );
      });
    },
    //查询提测单列表
    async queryTestList() {
      this.loading = true;
      try {
        let params = {
          ipmpKey: this.params.ipmpKey,
          demandKey: this.params.demandKey,
          implKey: this.params.implKey,
          testOrder: this.params.testOrder,
          group:
            this.params.group &&
            Object.prototype.toString.call(this.params.group) ===
              '[object Object]'
              ? this.params.group.id
              : '',
          status:
            this.params.status && this.params.status.map(item => item.value),
          creatorId:
            this.params.creatorId &&
            Object.prototype.toString.call(this.params.creatorId) ===
              '[object Object]'
              ? this.params.creatorId.id
              : '',
          pageNum: this.pagination.page,
          pageSize: this.pagination.rowsPerPage
        };
        let response = await resolveResponseError(() =>
          queryTestOrderList(params)
        );
        this.tableData = response.data;
        this.pagination.rowsNumber = response.total;
        this.loading = false;
      } catch (error) {
        this.loading = false;
        throw error;
      }
    },
    //确认提交
    comfireTest(val) {
      this.$q
        .dialog({
          title: `确认提示`,
          message: `确认提交提测单编号是
          ${val.test_order}的测试单至云测试平台？`,
          ok: true,
          cancel: true
        })
        .onOk(async () => {
          await resolveResponseError(() =>
            submitTestOrder({
              id: val.id
            })
          );
          successNotify('确认提交成功');
          this.queryTestList();
        });
    },
    //删除提测单
    deleteTest(val) {
      this.$q
        .dialog({
          title: `删除提示`,
          message: `确认删除提测单编号是
          ${val.test_order}的测试单？`,
          ok: true,
          cancel: true
        })
        .onOk(async () => {
          await resolveResponseError(() =>
            deleteTestOrder({
              id: val.id
            })
          );
          successNotify('删除成功');
          this.queryTestList();
        });
    },
    //查询是否有复制权限
    async queryCopyFlag() {
      let res = await resolveResponseError(() => queryCopyFlag());
      this.flagInfo = res;
    },
    handleSubmitTestOpen() {
      this.$router.push({
        path: `/rqrmn/submitTestOrder/2`,
        query: {}
      });
    },
    handleEditSubmitTestOpen($event) {
      this.$router.push({
        path: `/rqrmn/submitTestOrder/4/${$event.id}`
      });
    },
    handleCopySubmitTestOpen($event) {
      this.$router.push({
        path: `/rqrmn/submitTestOrder/3`,
        query: { id: $event.id }
      });
    },
    roundC(col) {
      let arrs = {
        create: 'creatC',
        test: 'testC',
        file: 'fileC',
        deleted: 'deletedC'
      };
      return arrs[col];
    },
    statusFilter(val) {
      let options = {
        create: '已创建',
        test: '已提测',
        file: '已归档',
        deleted: '已撤销'
      };
      return options[val];
    }
  },
  async mounted() {
    this.getCacheData();
    //隐藏查询 新建提测单和复制提测单权限接口
    // this.queryCopyFlag();
    let params = {
      level: '3',
      status: '1'
    };
    this.queryTestList();
    await this.fetchGroup(params);
    this.deepCloneGroups = deepClone(this.groupsData);
    this.deepCloneGroups.forEach((group, index) => {
      this.$set(this.deepCloneGroups[index], 'label', group.fullName);
    });
    this.groupOptions = this.deepCloneGroups;
    await this.queryUserCoreData();
    this.users = this.userList.map(user =>
      formatOption(formatUser(user), 'name')
    );
    this.createIdsOptions = this.users.slice(0);
  },
  beforeRouteLeave(to, from, next) {
    this.saveUserSearchData.forEach(x => {
      let val = [undefined, null].includes(this[x]) ? '' : this[x];
      SessionStorage.set(from.fullPath + '_' + x, val);
      next();
    });
  }
};
</script>

<style lang="stylus" scoped>
.space
  width 1px
  height 20px
  background #ccc
  margin 0 10px
.creatC
  background #24C8F9
.testC
  background #04488C
.fileC
  background #8CBC48
.deletedC
  background #B0BEC5
.roundS
  display inline-block
  height 8px
  width 8px
  margin-right 5px
  border-radius 4px
</style>
