<template>
  <f-block>
    <loading :visible="loading">
      <f-icon name="bell_s_f" class="titleimg" />
      <span class="text-subtitle3">组织结构管理</span>
      <fdev-splitter
        v-model="splitter"
        class="splitter splitter-padding"
        :horizontal="!$q.platform.is.desktop"
        :disable="!$q.platform.is.desktop"
      >
        <template v-slot:before>
          <fdev-splitter v-model="innerSplitter" disable horizontal>
            <template v-slot:before>
              <div class="q-pa-md tree-group">
                <fdev-tree
                  :nodes="nodes"
                  accordion
                  node-key="id"
                  :selected.sync="selected"
                  :expanded.sync="expanded"
                  children-key="childGroup"
                  selected-color="primary"
                  class="qtree"
                >
                  <template v-slot:default-header="prop">
                    <div class="row items-center">
                      <fdev-badge
                        v-if="!prop.node.new"
                        color="primary"
                        transparent
                        class="node-badge"
                      >
                        {{ prop.node.current_count }}
                      </fdev-badge>
                      <fdev-icon
                        v-else
                        size="21px"
                        color="positive"
                        name="fiber_new"
                      />
                      <span class="q-ml-xs">{{ prop.node.name }}</span>
                      <fdev-chip
                        outline
                        color="primary"
                        dense
                        size="xs"
                        text-color="white"
                        class="node-badge"
                      >
                        {{ prop.node.count }}
                      </fdev-chip>
                      <fdev-space />
                      <div class="q-gutter-x-md q-ml-sm">
                        <fdev-btn
                          icon="add"
                          flat
                          v-if="adminAuth || isAddOrg(prop.node)"
                          @click="addOrg(prop.node)"
                        />
                        <fdev-btn
                          icon="edit"
                          flat
                          v-if="adminAuth || isAddOrg(prop.node)"
                          @click="updateOrg(prop.node)"
                        />
                        <fdev-btn
                          icon="delete"
                          flat
                          v-if="
                            prop.node.count == 0 &&
                              !prop.node.childGroup.length &&
                              (adminAuth || isAddOrg(prop.node))
                          "
                          @click="deleteOrg(prop.node)"
                        />
                      </div>
                    </div>
                  </template>
                </fdev-tree>
              </div>
            </template>
            <template v-slot:after>
              <div class="q-pa-md">
                <!-- {{ group.name }} -->
                <div class="data-none row flex-center">
                  <!-- <fdev-icon name="sentiment_very_dissatisfied" /> -->
                </div>
              </div>
            </template>
          </fdev-splitter>
        </template>
        <template v-slot:after>
          <div class="row q-col-gutter-x-md">
            <div class="col-md-12 col-xs-12">
              <div class="q-px-md">
                <fdev-table
                  :data="tableUsers"
                  :columns="userColumns"
                  no-select-cols
                  noExport
                  titleIcon="list_s_f"
                  :title="tableTitle"
                  class="my-sticky-first-column-table"
                >
                  <template v-slot:top-right>
                    <span>
                      <fdev-btn
                        label="新增组员"
                        normal
                        class="q-ml-sm"
                        ficon="add"
                        v-if="!!tempId && isAddUserAuth"
                        @click="handleAddUser()"
                      >
                      </fdev-btn>
                    </span>
                    <span :class="showChildGroup ? 'active' : ''"
                      >展示子组</span
                    >
                    <fdev-toggle
                      :value="showChildGroup"
                      @input="updateShowChildGroup($event)"
                      label=""
                    ></fdev-toggle>
                  </template>
                  <template v-slot:body-cell-user_name_cn="props">
                    <fdev-td>
                      <router-link
                        :to="`/management/list/${props.row.id}`"
                        class="link"
                        :title="props.row.user_name_cn"
                      >
                        {{ props.row.user_name_cn }}
                      </router-link>
                    </fdev-td>
                  </template>
                  <!-- <template v-slot:body-cell-role="props">
                    <fdev-td
                      class="text-ellipsis"
                      :title="props.value | filterRole"
                    >
                      {{ props.value | filterRole }}
                    </fdev-td>
                  </template> -->
                </fdev-table>
              </div>
            </div>
          </div>
        </template>
      </fdev-splitter>
      <editOrg
        :isOpen="isEdit"
        :dataSource="GroupInfo"
        :parentNode="parentInfo"
        :dailogType="type"
        @close="isEdit = false"
        @edit="editOrg"
      />
      <f-dialog
        title="新增组员"
        right
        f-sc
        v-model="isAdd"
        @before-close="closeDialog"
      >
        <fdev-form
          @submit.prevent="edit"
          ref="addTeamUser"
          :greedy="true"
          :model="addTeamUser"
        >
          <f-formitem label="移入小组" diaS>
            <fdev-input v-model="tableTitle" hint="" />
          </f-formitem>
          <f-formitem label="选择成员" required diaS>
            <fdev-select
              multiple
              use-input
              ref="addTeamUser.teamUser"
              v-model="addTeamUser.teamUser"
              option-label="user_name_cn"
              option-value="id"
              map-options
              emit-value
              :options="userFilterOptions"
              @filter="userFilter"
              :rules="[val => val.length !== 0 || '请选择成员']"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.user_name_cn">
                      {{ scope.opt.user_name_cn }}
                    </fdev-item-label>
                    <fdev-item-label
                      :title="`${scope.opt.user_name_en}`"
                      caption
                    >
                      {{ scope.opt.user_name_en }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
        </fdev-form>
        <template #btnSlot>
          <fdev-btn label="取消" outline dialog @click="closeDialog"/>
          <fdev-btn label="确定" dialog @click="addGroupUser"
        /></template>
      </f-dialog>
    </loading>
  </f-block>
</template>
<script>
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import editOrg from './components/editOrg';
import { deepClone, successNotify, formatSelectDisplay } from '@/utils/utils';

import { setSplitter, getSplitter } from '@/modules/User/utils/setting';
import { findAuthority } from '@/modules/User/utils/model';
export default {
  name: 'organizationManagement',
  components: {
    editOrg,
    Loading
  },
  data() {
    return {
      loading: false,
      selected: '',
      nodes: [],
      // nodes: [
      //   {
      //     name: '浦发银行',
      //     id: 'root',
      //     header: 'root',
      //     selectable: false,
      //     childGroup: []
      //   }
      // ],
      userColumns: [
        {
          name: 'user_name_cn',
          label: '姓名',
          align: 'left',
          field: 'user_name_cn'
        },
        {
          name: 'email',
          label: '邮箱',
          field: 'email',
          sortable: true,
          align: 'left'
        },
        {
          name: 'name',
          label: '小组',
          field: 'name',
          sortable: true,
          align: 'left'
        },
        {
          name: 'telephone',
          label: '手机号',
          field: 'telephone',
          sortable: true,
          align: 'left'
        }
      ],
      expanded: [],
      currentGroups: [],
      org: [],
      GroupInfo: {},
      parentInfo: {},
      type: '',
      isEdit: false,
      tempId: '', //暂存的公司id，监视toogle发请求使用
      splitter: getSplitter() || 50,
      innerSplitter: 50,
      tableUsers: [],
      tableTitle: '小组',
      team: '',
      addTeamUser: { teamUser: [] },
      isAdd: false,
      userFilterOptions: [],
      formatSelectDisplay
    };
  },
  watch: {
    groups() {
      this.currentGroups = deepClone(this.groups);
    },
    showChildGroup() {
      if (this.tempId !== '') this.queryUsersByGroupId(this.tempId);
    },

    selected(val) {
      if (!val) {
        this.tableUsers = [];
        this.tableTitle = '小组';
        this.tempId = '';
      } else {
        this.tableTitle = this.getParent(this.allGroupList, val)[0].name;
        this.queryUsersByGroupId(val);
      }
    },
    splitter(val) {
      setSplitter(val);
    }
  },
  filters: {
    filterRole(val) {
      let roles = val.map(item => item.name);
      return roles.join(',');
    }
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', {
      groupPeople: 'groupPeople',
      abandonGroups: 'abandonGroups',
      groups: 'groupsAll',
      childGroup: 'childGroup'
    }),
    ...mapState('userActionSaveManagement/dimensionQuery/groupDimension', [
      'showChildGroup',
      'visibleColumns'
    ]),
    ...mapGetters('authorized', ['checkPermissions', 'checkTransPermissions']),
    ...mapState('managementForm', [
      'allGroupList',
      'addGroupUsersList',
      'userByGroupIdList',
      'addUserList'
    ]),

    adminAuth() {
      return (
        this.currentUser.user_name_en === 'admin' ||
        findAuthority(this.currentUser) === 'admin' ||
        this.currentUser.role.some(role => role.name === '用户管理员')
      );
    },
    isAddUserAuth() {
      return (
        this.currentUser.user_name_en === 'admin' ||
        findAuthority(this.currentUser) === 'admin' ||
        this.currentUser.role.some(role => role.name === '用户管理员') ||
        this.teamLeader() ||
        this.groupLeader()
      );
    }
  },
  methods: {
    // ...mapActions('user', [
    //   'fetchGroup',
    //   'addGroup',
    //   'updateGroup',
    //   'deleteGroup'
    // ]),
    ...mapMutations('userActionSaveManagement/dimensionQuery/groupDimension', [
      'updateShowChildGroup',
      'updateVisibleColumns'
    ]),
    ...mapActions('userForm', {
      removeGroup: 'removeGroup',
      addGroup: 'addGroup',
      updateGroup: 'updateGroup',
      fetchGroup: 'fetchGroupAll',
      queryChildGroupById: 'queryChildGroupById'
    }),
    ...mapActions('managementForm', [
      'addGroupUsers',
      'queryAllGroup',
      'queryUserByGroupId',
      'canAddUserList'
    ]),
    addOrg(val) {
      this.type = 'add';
      this.parentInfo = val;
      this.GroupInfo = {};
      this.isEdit = true;
    },
    updateOrg(val) {
      this.type = 'edit';
      this.parentInfo = this.getParent(this.allGroupList, val.parent_id)[0];
      this.GroupInfo = val;
      this.isEdit = true;
    },
    async deleteOrg(val) {
      return this.$q
        .dialog({
          title: `删除小组`,
          message: `确认删除该小组么？`,
          ok: '删除',
          cancel: '再想想'
        })
        .onOk(async () => {
          this.loading = true;
          await this.removeGroup(val);
          this.selected = '';
          this.update();
          successNotify('删除成功');
          this.loading = false;
        });
    },
    handleAddUser() {
      this.isAdd = true;
    },
    async addGroupUser() {
      this.$refs.addTeamUser.validate().then(res => {
        if (!res) return;
        this.addUser();
      });
    },
    async addUser() {
      await this.addGroupUsers({
        id: this.tempId,
        userIds: this.addTeamUser.teamUser,
        groupId: this.tempId
      });
      this.queryUsersByGroupId(this.tempId);
      successNotify('新增成功');
      this.addTeamUser.teamUser = [];
      this.queryGroup();
      this.isAdd = false;
    },
    closeDialog() {
      this.isAdd = false;
      this.addTeamUser.teamUser = [];
    },
    teamLeader() {
      if (this.currentUser.role.some(role => role.name === '团队负责人')) {
        return (
          this.childGroup.some(item => item.id === this.tempId) ||
          this.currentUser.group_id === this.tempId
        );
      } else {
        return false;
      }
    },
    groupLeader() {
      if (this.currentUser.role.some(role => role.name === '小组管理员')) {
        return (
          this.currentUser.group_id === this.tempId ||
          this.childGroup.some(item => item.id === this.tempId)
        );
      } else {
        return false;
      }
    },
    isAddOrg(row) {
      if (this.currentUser.role.some(role => role.name === '团队负责人')) {
        return (
          this.childGroup.some(item => item.id === row.id) ||
          this.currentUser.group_id === row.id
        );
      } else if (
        this.currentUser.role.some(role => role.name === '小组管理员')
      ) {
        return (
          this.currentUser.group_id === row.id ||
          this.childGroup.some(tag => tag.id === row.id)
        );
      } else {
        return false;
      }
    },

    async editOrg(val) {
      this.isEdit = false;
      this.loading = true;
      try {
        if (this.type === 'add') {
          await this.addGroup(val);
          this.update();
          successNotify('新增成功');
        } else {
          await this.updateGroup(val);
          this.update();
          successNotify('修改成功');
        }
      } finally {
        this.loading = false;
        this.org = deepClone(this.groups);
      }
    },
    async update() {
      this.queryGroup();
      await this.queryChildGroupById({ id: this.currentUser.group.id });
    },
    //树形结构遍历id
    getParent(data2, id) {
      let arrRes = [];
      // 如果非数组直接返回
      if (data2.length === 0) {
        if (id) {
          arrRes.unshift(data2);
        }
        return arrRes;
      }
      const rev = (data, nodeId) => {
        for (let i = 0; i < data.length; i += 1) {
          const node = data[i];
          // 顶层直接跳出
          if (!nodeId && nodeId !== 0) break;

          // 有则跳出循环再次递归
          if (node.id === nodeId) {
            arrRes.unshift(node);
            break;
          } else if (node.childGroup) {
            rev(node.childGroup, nodeId);
          }
        }

        return arrRes;
      };
      arrRes = rev(data2, id);
      return arrRes;
    },
    //树形结构遍历name
    getParentName(data2, id) {
      let arrRes = [];
      // 如果非数组直接返回
      if (data2.length === 0) {
        if (id) {
          arrRes.unshift(data2);
        }
        return arrRes;
      }
      const rev = (data, nodeId) => {
        for (let i = 0; i < data.length; i += 1) {
          const node = data[i];
          // 顶层直接跳出
          if (!nodeId && nodeId !== 0) break;

          // 有则跳出循环再次递归
          if (node.name === nodeId) {
            arrRes.unshift(node);
            break;
          } else if (node.childGroup) {
            rev(node.childGroup, nodeId);
          }
        }

        return arrRes;
      };
      arrRes = rev(data2, id);
      return arrRes;
    },
    async queryUsersByGroupId(id) {
      await this.queryUserByGroupId({
        groupId: id,
        showChild: this.showChildGroup
      });
      this.tempId = id;
      this.tableUsers = this.userByGroupIdList.data.slice(0);
    },

    userFilter(val, update) {
      update(() => {
        // 小组管理员新增只能新增自己小组人员
        this.userFilterOptions = this.deepCloneUserFilterOptions.filter(
          tag =>
            tag.user_name_cn.indexOf(val) > -1 ||
            tag.user_name_en.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
    },
    async queryGroup() {
      await this.queryAllGroup();
      this.nodes = this.allGroupList;
      this.expanded.push(
        this.nodes[0].id,
        this.getParentName(this.allGroupList, '互联网')[0] &&
          this.getParentName(this.allGroupList, '互联网')[0].id
      );
    }
  },
  async created() {
    this.loading = true;
    this.queryGroup();
    await this.queryChildGroupById({ id: this.currentUser.group.id });
    await this.canAddUserList();
    this.userFilterOptions = this.addUserList;
    this.deepCloneUserFilterOptions = deepClone(this.userFilterOptions);
    this.loading = false;
  },
  mounted() {
    if (sessionStorage.getItem('nodes')) {
      const { nodes, selected } = JSON.parse(sessionStorage.getItem('nodes'));
      this.nodes = nodes;
      this.selected = selected;
    }
  },
  beforeRouteEnter(to, from, next) {
    const { params } = from;
    if (Object.keys(params).length === 0) {
      sessionStorage.removeItem('nodes');
    }
    next();
  },
  beforeRouteLeave(to, from, next) {
    const { params } = to;
    if (Object.keys(params).length) {
      sessionStorage.setItem(
        'nodes',
        JSON.stringify({
          nodes: this.nodes,
          selected: this.selected
        })
      );
    }
    next();
  }
};
</script>
<style lang="stylus" scoped>
.titleimg{
  width: 16px;
  height: 16px;
  color: #0663be;
  margin-right: 8px;
  margin-bottom: -4px;
  border-radius: 4px;
  border-radius: 4px;
  }
.qtree >>> .q-tree__arrow
  color #0663BE
.text-subtitle3
  font-size: 1rem;
  font-weight: 600;
  line-height: 2rem;
.splitter
  min-height: calc(100vh - 299px);
</style>
