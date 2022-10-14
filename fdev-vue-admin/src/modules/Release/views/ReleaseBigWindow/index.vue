<template>
  <Loading :visible="loading['releaseForm/queryBigReleaseNodes']">
    <fdev-table
      titleIcon="list_s_f"
      :data="tableData"
      :columns="columns"
      :pagination.sync="pagination"
      @request="init"
      title="投产大窗口列表"
      class="my-sticky-last-column-table"
    >
      <template v-slot:top-right>
        <span>
          <fdev-btn
            normal
            ficon="add"
            :disable="!releaseRole()"
            label="新增投产大窗口"
            @click="handleBigReleaseDialogOpen()"
          />
          <fdev-tooltip v-if="!releaseRole()">
            请联系投产管理员
          </fdev-tooltip>
        </span>
      </template>

      <template v-slot:top-bottom>
        <f-formitem
          class="col-4 q-pr-lg"
          label-style="width:80px"
          bottom-page
          label="起始日期"
        >
          <f-date
            :options="startOptions"
            v-model="releaseModel.start_date"
            mask="YYYY-MM-DD"
            @input="filterData"
          />
        </f-formitem>

        <f-formitem
          class="col-4 q-pr-lg"
          label-style="width:80px"
          bottom-page
          label="结束日期"
        >
          <f-date
            @input="filterData"
            :options="endOptions"
            v-model="releaseModel.end_date"
            mask="YYYY-MM-DD"
          />
        </f-formitem>

        <f-formitem
          class="col-4 q-pr-lg"
          label-style="width:80px"
          bottom-page
          label="小组"
        >
          <fdev-select
            :value="releaseModel.owner_groupId"
            :options="groupOptions"
            options-dense
            class="table-head-input q-mr-sm"
            option-label="fullName"
            option-value="id"
            map-options
            emit-value
            use-input
            @filter="filterGroup"
            @input="updateReleaseModelOwnerGroupId($event), filterData(null)"
          />
        </f-formitem>
      </template>

      <template v-slot:body-cell-release_date="props">
        <fdev-td class="text-ellipsis">
          <router-link
            :to="{
              path: `/release/BigReleaseDetail/${props.row.release_date}`
            }"
            class="link"
          >
            <span :title="props.row.release_date">{{
              props.row.release_date
            }}</span>
          </router-link>
        </fdev-td>
      </template>

      <template v-slot:body-cell-release_contact="props">
        <fdev-td
          :title="props.value.map(v => v.user_name_cn).join(',')"
          class="text-ellipsis"
        >
          <div
            class="inline-block"
            v-for="(item, index) in props.value"
            :key="index"
          >
            <router-link
              v-if="item.id"
              :to="{ path: `/user/list/${item.id}` }"
              class="link"
            >
              {{ item.user_name_cn }}
            </router-link>

            <span v-else>{{ item.user_name_en }}</span>
            &nbsp;
          </div>
        </fdev-td>
      </template>

      <template v-slot:body-cell-creater="props">
        <fdev-td class="text-ellipsis">
          <router-link
            v-if="props.value.id"
            :to="{ path: `/user/list/${props.value.id}` }"
            class="link"
            :title="props.value.user_name_cn"
          >
            {{ props.value.user_name_cn }}
          </router-link>

          <span v-else :title="props.value.user_name_en || '-'">
            {{ props.value.user_name_en || '-' }}</span
          >
        </fdev-td>
      </template>

      <template v-slot:body-cell-btn="props">
        <fdev-td>
          <div class="q-gutter-sm row items-center no-wrap">
            <span>
              <fdev-btn
                flat
                :disable="!isReleaseContact(props.row.release_contact)"
                label="编辑"
                @click="handleBigReleaseDialogOpen(props.row)"
              />
              <fdev-tooltip v-if="!isReleaseContact(props.row.release_contact)">
                请联系当前投产大窗口牵头联系人
              </fdev-tooltip>
            </span>

            <!-- 权限:本组的牵头联系人或卡点管理员 -->
            <span>
              <fdev-btn
                :disable="!(isDeleteRole(props.row) || isKaDianManager)"
                flat
                label="删除"
                @click="deleteBigRelease(props.row)"
              />
              <fdev-tooltip
                v-if="!(isDeleteRole(props.row) || isKaDianManager)"
              >
                请联系当前投产大窗口牵头联系人
              </fdev-tooltip>
            </span>
          </div>
        </fdev-td>
      </template>
    </fdev-table>

    <BigReleaseDialog
      @submit="handleBigReleaseDialog"
      v-model="bigReleaseDialogOpened"
      :title="`${editMessage.message}投产大窗口`"
      :data="editData"
      :loading="loading[editMessage.loading]"
    />
  </Loading>
</template>

<script>
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import { successNotify } from '@/utils/utils';
import { releaseDialogModel } from '../../utils/model';
import { relaseBigListColumns } from '../../utils/constants';
import Loading from '@/components/Loading';
import BigReleaseDialog from './components/BigReleaseDialog';
export default {
  name: 'BigRelease',
  components: { Loading, BigReleaseDialog },
  data() {
    return {
      groupOptions: [{ fullName: '全部', id: '' }],
      bigReleaseDialogOpened: false,
      isEdit: false,
      editData: releaseDialogModel(),
      tableData: [],
      columns: relaseBigListColumns,
      pagination: {
        rowsPerPage: 5
      }
    };
  },
  watch: {
    pagination(val) {
      this.updateBigReleaseCurrentPage({
        rowsPerPage: val.rowsPerPage
      });
    }
  },
  computed: {
    ...mapState('userActionSaveRelease/releaseList', [
      'releaseModel',
      'bigReleaseCurrentPage'
    ]),
    ...mapState('releaseForm', ['bigRelease']),
    ...mapState('global', ['loading']),
    ...mapState('userForm', ['groups']),
    ...mapState('user', ['currentUser']),
    ...mapGetters('user', ['isKaDianManager']),
    ...mapGetters('releaseForm', ['isReleaseContact', 'releaseRole']),
    editMessage() {
      if (this.isEdit) {
        return {
          loading: 'releaseForm/updateBigReleasenode',
          message: '修改',
          method: this.updateBigReleasenode
        };
      } else {
        return {
          loading: 'releaseForm/createBigReleaseNode',
          message: '新增',
          method: this.createBigReleaseNode
        };
      }
    }
  },
  methods: {
    ...mapMutations('userActionSaveRelease/releaseList', [
      'updateReleaseModelStartDate',
      'updateReleaseModelEndDate',
      'updateReleaseModelOwnerGroupId',
      'updateBigReleaseCurrentPage'
    ]),
    ...mapActions('releaseForm', [
      'createBigReleaseNode',
      'updateBigReleasenode',
      'queryBigReleaseNodes',
      'deleteReleaseNode'
    ]),
    ...mapActions('userForm', ['fetchGroup']),
    isDeleteRole(data) {
      return data.release_contact.some(user => {
        return user.user_name_en === this.currentUser.user_name_en;
      });
    },
    endOptions(date) {
      this.releaseModel.start_date = this.releaseModel.start_date
        ? this.releaseModel.start_date
        : '';
      return date > this.releaseModel.start_date.replace(/-/g, '/');
    },
    startOptions(date) {
      if (this.releaseModel.end_date) {
        return date < this.releaseModel.end_date.replace(/-/g, '/');
      }
      return true;
    },
    filterData(target) {
      this.pagination.page = 1;
      this.init();
    },
    filterGroup(val, update) {
      update(() => {
        this.groupOptions = this.groups.filter(
          tag => tag.label.indexOf(val) > -1
        );
      });
    },
    deleteBigRelease(data) {
      return this.$q
        .dialog({
          title: `删除确认`,
          message: `请确认是否删除${data.release_date}投产大窗口`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.deleteReleaseNode({
            release_node_name: data.release_date
          });
          successNotify('删除成功！');
          this.init();
        });
    },
    handleBigReleaseDialogOpen(data) {
      this.editData = data ? data : releaseDialogModel();
      this.isEdit = !!data;
      this.bigReleaseDialogOpened = true;
    },
    async handleBigReleaseDialog(data) {
      await this.editMessage.method(data);
      this.bigReleaseDialogOpened = false;
      successNotify(`${this.editMessage.message}成功！`);
      this.init();
    },
    async init() {
      await this.queryBigReleaseNodes(this.releaseModel);
      this.tableData = this.bigRelease;
    }
  },
  async created() {
    await this.fetchGroup();
    this.deepCloneGroups = this.groups.map(item => {
      return { fullName: item.fullName, id: item.id };
    });
    this.groupOptions = [{ fullName: '全部', id: '' }, ...this.deepCloneGroups];
    this.pagination = this.bigReleaseCurrentPage;
    this.init();
  }
};
</script>

<style lang="stylus" scoped></style>
