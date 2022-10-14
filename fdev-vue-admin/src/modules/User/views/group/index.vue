<template>
  <f-block>
    <Loading :visible="loading">
      <f-icon name="bell_s_f" class="titleimg" />
      <span class="text-subtitle3">组织结构查询</span>
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
                  node-key="id"
                  :expanded.sync="expanded"
                  ref="tree"
                  accordion
                  :selected.sync="selected"
                  children-key="childGroup"
                  selected-color="primary"
                  class="qtree"
                >
                  <template v-slot:default-header="prop">
                    <div class="node col row items-center">
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
                    </div>
                  </template>
                  <template v-slot:header-root="prop">
                    {{ prop.node.name }}
                  </template>
                  <template v-slot:header-add="prop">
                    <fdev-icon
                      size="20px"
                      flat
                      dense
                      v-ripple
                      name="add"
                      color="primary"
                      class="relative-position"
                    >
                      <fdev-popup-edit
                        v-model="addModel"
                        buttons
                        @save="() => handleAdd(prop.node)"
                        :validate="required"
                        @hide="required"
                        :color="
                          required(addModel) ? 'primary' : 'noop disabled'
                        "
                      >
                        <fdev-input autofocus v-model="addModel"></fdev-input>
                      </fdev-popup-edit>
                    </fdev-icon>
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
                        :to="`/user/list/${props.row.id}`"
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
    </Loading>
  </f-block>
</template>

<script>
import { mapState, mapGetters, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import { setSplitter, getSplitter } from '@/modules/User/utils/setting';
import { deepClone } from '@/utils/utils';
import { findAuthority } from '@/modules/User/utils/model';

export default {
  name: 'Group',
  components: { Loading },
  data() {
    return {
      loading: true,
      tempId: '', //暂存的公司id，监视toogle发请求使用
      tableUsers: [],
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
      nodes: [],
      // nodes: [
      //   {
      //     label: '浦发银行',
      //     id: 'root',
      //     header: 'root',
      //     selectable: false,
      //     children: []
      //   }
      // ],
      expanded: ['root'],
      selected: '',

      splitter: getSplitter() || 50,
      innerSplitter: 50,

      addModel: '',
      tableTitle: '',
      currentGroups: []
    };
  },
  filters: {
    filterRole(val) {
      let roles = val.map(item => item.name);
      return roles.join(',');
    }
  },
  watch: {
    groups() {
      this.currentGroups = deepClone(this.groups);
    },
    showChildGroup() {
      if (this.tempId !== '') this.queryUsersByGroupId(this.tempId);
    },
    async currentGroups() {
      let root = [];
      let group = [];
      group = this.groups;
      root = this.currentGroups.filter(group => !group.parent);
      this.nodes[0].children = this.appendNode(
        root,
        group.filter(group => group.id && group.parent)
      );
      if (this.checkTransPermissions('/api/group/add')) {
        if (findAuthority(this.currentUser) === 'group_admin') {
          this.nodes[0].children.push({
            id: 'node-add-root',
            header: 'add',
            selectable: false,
            parent: null,
            name: ''
          });
        } else if (
          findAuthority(this.currentUser) === 'group_admin' &&
          this.currentUser.group.name == this.nodes[0].label
        ) {
          this.nodes[0].children.push({
            id: 'node-add-root',
            header: 'add',
            selectable: false,
            parent: null,
            name: ''
          });
        }
      }
    },
    selected(val) {
      if (!val) {
        this.tableUsers = [];
      } else {
        this.tableTitle = this.getParent(this.allGroupList, val)[0].name;
        this.queryUsersByGroupId(val);
      }
    },
    splitter(val) {
      setSplitter(val);
    }
  },
  computed: {
    ...mapState('userActionSaveUser/dimensionQuery/groupDimension', [
      'showChildGroup',
      'visibleColumns'
    ]),
    ...mapState('userForm', {
      groupPeople: 'groupPeople',
      abandonGroups: 'abandonGroups',
      groups: 'groupsAll',
      childGroup: 'childGroup'
    }),
    ...mapState('managementForm', [
      'allGroupList',
      'addGroupUsersList',
      'userByGroupIdList'
    ]),
    ...mapState('user', { currentUser: 'currentUser' }),
    ...mapGetters('authorized', ['checkPermissions', 'checkTransPermissions']),
    group() {
      let group = this.currentGroups.find(group => group.id === this.selected);
      return group || {};
    }
  },
  methods: {
    ...mapMutations('userActionSaveUser/dimensionQuery/groupDimension', [
      'updateShowChildGroup',
      'updateVisibleColumns'
    ]),
    ...mapActions('userForm', {
      removeGroup: 'removeGroup',
      addGroup: 'addGroup',
      updateGroup: 'updateGroup',
      queryGroupPeople: 'queryGroupPeople',
      fetchGroup: 'fetchGroupAll',
      queryChildGroupById: 'queryChildGroupById'
    }),
    ...mapActions('managementForm', [
      'addGroupUsers',
      'queryAllGroup',
      'queryUserByGroupId'
    ]),

    // isNotLastChild(group) {
    //   let children = group.children;
    //   if (children && children.length !== 0) {
    //     // 有添加小组的权限时 children 恒为真 length>0
    //     let moveAddGroup = children.filter(childGroup => {
    //       return childGroup.header !== 'add';
    //     });
    //     return moveAddGroup.length > 0;
    //   }
    // },

    async handleAdd(group) {
      await this.addGroup({
        name: this.addModel,
        parent: group.parent
      });
      await this.queryChildGroupById({ id: this.currentUser.group.id });
      if (findAuthority(this.currentUser) === 'group_admin') {
        this.childGroup.forEach(item => {
          this.groups.forEach(ele => {
            if (ele.id === item.id) {
              ele.showButton = true;
            }
          });
        });
      } else {
        this.groups.forEach(ele => {
          ele.showButton = true;
        });
      }
      this.currentGroups = deepClone(this.groups);
      this.addModel = '';
    },
    required(val) {
      return !!val;
    },
    //树形结构遍历
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
    async queryGroup() {
      await this.queryAllGroup();
      this.nodes = this.allGroupList;
      this.expanded.push(
        this.nodes[0].id,
        this.getParentName(this.allGroupList, '互联网')[0].id
      );
    }
  },
  async created() {
    this.loading = true;
    this.queryGroup();
    await this.queryChildGroupById({ id: this.currentUser.group.id });
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
.card-add
  min-width: 280px;
.link
  i
    visibility: hidden;
  &:hover
    i
      visibility: visible;
.node
  .group-operation
    position: relative;
    opacity: 0.1;
  &:hover
    .group-operation
      opacity: 0.9;
.total
  margin-left: 5px;
.splitter
  min-height: calc(100vh - 299px);
.select-group
  position absolute
  right 0
.width-select
  min-width 150px
.active
  color #1976d2
</style>
