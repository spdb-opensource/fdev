<template>
  <f-block>
    <f-icon name="bell_s_f" class="titleimg" />
    <span class="text-subtitle1 q-pa-sm q-my-md">菜单管理</span>
    <fdev-tree
      :nodes="nodes"
      accordion
      no-connectors
      node-key="menuId"
      children-key="childrenList"
      :expanded.sync="expanded"
      class="qtree"
    >
      <template v-slot:header-root="prop">
        <div class="row items-center q-gutter-x-lg">
          <span>{{ prop.node.nameCn }}</span>
          <fdev-btn
            icon="add"
            flat
            label="新增"
            @click="addMenus(prop.node)"
            v-if="adminAuth"
          />
        </div>
      </template>
      <template v-slot:default-header="prop">
        <div class="row q-gutter-x-md items-center">
          <fdev-chip
            square
            class="q-pa-sm"
            color="primary"
            text-color="white"
            v-if="prop.node.level !== '0'"
          >
            {{ prop.node.level | filterLevel(prop.node.level) }}
          </fdev-chip>
          <span>{{ prop.node.nameCn }}</span>
          <div v-if="adminAuth" class="row q-gutter-x-md q-ml-lg">
            <fdev-btn
              icon="add"
              flat
              label="新增"
              v-if="prop.node.level < 4"
              @click="addMenus(prop.node)"
            />
            <fdev-btn
              icon="edit"
              flat
              label="编辑"
              @click="updateMenus(prop.node)"
            />
            <fdev-btn
              icon="delete"
              flat
              label="删除"
              v-if="!prop.node.childrenList.length"
              @click="removeMenu(prop.node)"
            />
          </div>
        </div>
      </template>
    </fdev-tree>
    <editMenu
      :isOpen="isEdit"
      :dataSource="menuInfo"
      :parentNode="parentInfo"
      :dailogType="type"
      @close="isEdit = false"
      @edit="editMenu"
    />
  </f-block>
</template>
<script>
import { filterLevel } from '../../utils/constants';
import { mapState, mapActions } from 'vuex';
import editMenu from './components/editMenu';
import { successNotify } from '@/utils/utils';
import { findAuthority } from '@/modules/User/utils/model';

export default {
  name: 'menuManagement',
  components: {
    editMenu
  },
  data() {
    return {
      nodes: [
        {
          menuId: 'menu',
          header: 'root',
          level: '0',
          nameCn: '根节点',
          childrenList: []
        }
      ],
      selected: '',
      expanded: [],
      isSelect: false,
      isEdit: false,
      type: '',
      menuInfo: {},
      parentInfo: {},
      dismiss: null
    };
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('managementForm', ['menuTableList', 'roleMemu']),
    adminAuth() {
      return (
        this.currentUser.user_name_en === 'admin' ||
        findAuthority(this.currentUser) === 'admin'
      );
      // return this.currentUser.user_name_en === 'c-chengp1';
    }
  },
  filters: {
    filterLevel,
    filterMenuType(val) {
      let obj = {
        '1': '非敏',
        '2': '敏捷'
      };
      return obj[val];
    }
  },
  methods: {
    ...mapActions('managementForm', [
      'queryMenu',
      'updateMenu',
      'addMenu',
      'deleteMenu',
      'queryRoleByMenuId'
    ]),
    addMenus(val) {
      this.type = 'add';
      this.menuInfo = {};
      this.parentInfo = val;
      this.isEdit = true;
    },
    updateMenus(val) {
      this.type = 'edit';
      this.menuInfo = val;
      const { level, parentId } = val;

      if (level !== '1' && level < '3') {
        this.parentInfo = this.menus.find(item => item.menuId === parentId);
      } else if (level === '3') {
        const menusLevel = this.menus.find(item =>
          item.childrenList.find(val => val.menuId === parentId)
        );
        this.parentInfo = menusLevel.childrenList.find(
          item => (item = item.menuId === parentId)
        );
      } else if (level === '4') {
        this.parentInfo = this.getParent(this.menus, parentId)[0];
      } else {
        this.parentInfo = this.nodes[0];
      }
      this.isEdit = true;
    },

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
          if (node.menuId === nodeId) {
            arrRes.unshift(node);

            //rev(data2, node.menuId);
            break;
          } else if (node.childrenList) {
            rev(node.childrenList, nodeId);
          }
        }
        return arrRes;
      };
      arrRes = rev(data2, id);
      return arrRes;
    },
    async editMenu(val) {
      if (this.type === 'add') {
        await this.addMenu(val);
        successNotify('新增成功');
        this.isEdit = false;
      } else {
        await this.updateMenu(val);
        successNotify('修改成功');
        this.isEdit = false;
      }
      this.queryMenus();
      this.$root.$emit('updateMenu');
    },
    async removeMenu(val) {
      const { menuId } = val;
      await this.queryRoleByMenuId({ menuId });
      let res = this.roleMemu;
      if (res.length > 0) {
        this.$q
          .dialog({
            title: `温馨提示`,
            message: `该菜单有角色正在被使用,不可删除！`,
            persistent: true,
            ok: '返回'
          })
          .onOk(() => {});
      } else {
        this.$q
          .dialog({
            title: `温馨提示`,
            message: `确认删除该菜单项么？`,
            persistent: true,
            ok: '删除',
            cancel: '再想想'
          })
          .onOk(async () => {
            await this.deleteMenu({ menuId });
            this.queryMenus();
            this.$root.$emit('updateMenu');
            successNotify('删除成功');
          });
      }
    },
    async queryMenus() {
      await this.queryMenu();
      let response = this.menuTableList;
      this.menus = response;
      this.nodes[0].childrenList = this.menus;
      this.expanded.push('menu');
    }
  },
  created() {
    this.queryMenus();
  }
};
</script>
<style lang="stylus" scoped>
.titleimg{
  width: 16px;
  height: 16px;
  color: #0663be;
  margin-bottom: -4px;
  border-radius: 4px;
  border-radius: 4px;
  }
.qtree >>> .q-tree__arrow
  color #0663BE
.text-subtitle1
  font-size: 1rem;
  font-weight: 600;
  line-height: 2rem;
</style>
