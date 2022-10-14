<template>
  <f-block>
    <Loading :visible="loading['releaseForm/queryBigReleaseNodeDetail']">
      <div class="row">
        <f-formitem label="投产日期：" class="col-4">
          {{ detail.release_date }}
        </f-formitem>

        <f-formitem label="牵头小组：" class="col-4">
          <div
            class="ellipsis-3-lines no-wrap"
            :title="detail.owner_group_name"
          >
            {{ detail.owner_group_name }}
          </div>
        </f-formitem>

        <f-formitem label="牵头联系人：" class="col-4">
          <div
            class="inline-block"
            v-for="(item, index) in detail.release_contact"
            :key="index"
          >
            <router-link
              v-if="item.id"
              :to="{ path: `/user/list/${item.id}` }"
              class="link"
            >
              {{ item.user_name_cn }}
            </router-link>

            <span v-else class="span-margin">{{ item.user_name_en }}</span>
            &nbsp;
          </div>
        </f-formitem>
      </div>

      <div
        v-show="
          this.$route.name !== 'Process' && this.$route.name !== 'Contact'
        "
      >
        <div class="group-fixed bg-white">
          <span>所属小组：</span>
          <span
            class="text-primary q-ml-md float-right cursor-pointer"
            @click="reset"
          >
            重置
          </span>

          <span class="text-primary float-right cursor-pointer" @click="close">
            {{ open ? '展开' : '收起' }}
          </span>
          <GroupsTree
            v-model="selectedList"
            :data="groupsData"
            ref="groupsTree"
            :firstData="[]"
          />
        </div>
      </div>
      <f-formitem label="筛选项" v-if="this.$route.name == 'Demand'" page>
        <fdev-select
          v-model="special"
          :options="specialOptions"
          options-dense
          emit-value
          map-options
          option-label="label"
          option-value="value"
        />
      </f-formitem>
      <div class="row justify-center q-mt-md q-gutter-md">
        <div>
          <fdev-btn
            label="查询"
            v-if="
              this.$route.name !== 'Process' && this.$route.name !== 'Contact'
            "
            :disable="!selectedList || selectedList.length === 0"
            @click="search()"
          />
          <fdev-tooltip v-if="!selectedList || selectedList.length === 0">
            请选择所属小组
          </fdev-tooltip>
        </div>
        <fdev-btn
          label="编辑投产大窗口"
          v-if="isReleaseContact()"
          @click="handleBigReleaseDialogOpen"
        />
        <fdev-btn label="返回" @click="goBack" />
      </div>

      <div class="bg-white q-mt-md">
        <fdev-tabs
          class="q-mb-md"
          active-color="primary"
          indicator-color="primary"
          align="left"
          to="/BigReleaseDetail"
        >
          <router-link
            v-for="item in tabList"
            :key="item.label"
            :label="item.label"
            :to="item.path"
            excat
            class="router-tab"
          >
            {{ item.label }}
          </router-link>
        </fdev-tabs>

        <div class="q-pa-md">
          <router-view />
        </div>
      </div>

      <BigReleaseDialog
        @submit="handleBigReleaseDialog"
        v-model="bigReleaseDialogOpened"
        title="编辑投产大窗口"
        :data="detail"
        :loading="loading['releaseForm/updateBigReleasenode']"
      />
    </Loading>
  </f-block>
</template>

<script>
import { specialOptions } from '../../utils/model';
import Loading from '@/components/Loading';
import GroupsTree from '@/modules/Dashboard/components/Chart/GroupsTree';
import BigReleaseDialog from './components/BigReleaseDialog';
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import { successNotify, deepClone } from '@/utils/utils';
export default {
  name: 'BigReleaseDetail',
  components: {
    Loading,
    GroupsTree,
    BigReleaseDialog
  },
  data() {
    return {
      detail: {},
      selectedList: [],
      open: false,
      special: '',
      specialOptions: specialOptions,
      groupsData: [],
      bigReleaseDialogOpened: false,
      tab: 'process',
      release_date: ''
    };
  },
  computed: {
    ...mapState('userForm', {
      groups: 'groups'
    }),
    ...mapState('jobForm', ['rqrmntFileUri']),
    ...mapState('global', ['loading']),
    ...mapState('user', ['currentUser']),
    ...mapState('releaseForm', ['bigReleaseDetail']),
    ...mapGetters('releaseForm', ['isReleaseContact']),
    nodes() {
      const root = this.groups.filter(group => !group.parent);
      const groupList = this.appendNode(
        root,
        this.groups.filter(group => group.id && group.parent)
      );
      return this.addAttribute(groupList);
    },
    disableType() {
      return sessionStorage.getItem('searchType');
    },
    tabList() {
      return [
        {
          label: '投产流程说明',
          path: `/release/BigReleaseDetail/${this.release_date}/process`
        },
        {
          label: '投产联系人/变更会同人员',
          path: `/release/BigReleaseDetail/${this.release_date}/contact`
        },
        {
          label: '需求列表',
          path: `/release/BigReleaseDetail/${this.release_date}/demand`
        },
        {
          label: '提测重点',
          path: `/release/BigReleaseDetail/${this.release_date}/test`
        },
        {
          label: '安全测试需求',
          path: `/release/BigReleaseDetail/${this.release_date}/security`
        }
      ];
    }
  },
  watch: {
    nodes(val) {
      this.groupsData = deepClone(val);
    },
    selectedList(val) {
      const list = val && val.length > 0 ? Array.from(new Set(val)) : [];
      if (list && list.length > 0) {
        this.saveSelectedGourpList(list);
      }
    },
    special(val) {
      this.saveDemandFilterData(val);
    }
  },
  methods: {
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('releaseForm', [
      'queryRqrmntInfoListByType',
      'queryRqrmntInfoList',
      'queryBigReleaseNodeDetail',
      'updateBigReleasenode'
    ]),
    ...mapActions('jobForm', ['queryRqrmntFileUri']),
    ...mapMutations('releaseForm', [
      'saveSelectedGourpList',
      'saveDemandFilterData'
    ]),
    search() {
      if (this.$route.name == 'Demand') {
        this.searchDemandList();
      } else if (this.$route.name == 'Security') {
        this.searchSecurityList();
      } else {
        this.searchTestList();
      }
    },
    async searchDemandList() {
      this.selectedList = Array.from(new Set(this.selectedList));
      await this.queryRqrmntInfoList({
        groupIds: this.selectedList,
        release_date: this.release_date,
        type: this.special,
        isParent: true
      });
    },
    async searchTestList() {
      this.selectedList = Array.from(new Set(this.selectedList));
      await this.queryRqrmntInfoListByType({
        groupIds: this.selectedList,
        release_date: this.release_date,
        type: '1',
        isParent: true
      });
    },
    async searchSecurityList() {
      this.selectedList = Array.from(new Set(this.selectedList));
      await this.queryRqrmntInfoListByType({
        groupIds: this.selectedList,
        release_date: this.release_date,
        type: '2',
        isParent: true
      });
    },
    appendNode(parent, set, depth = 2) {
      if (!Array.isArray(parent) || !Array.isArray(set)) {
        return [];
      }
      if (parent.length === 0 || set.length === 0) {
        return [];
      }
      if (depth === 0) {
        return parent;
      }
      const child = parent.reduce((pre, next) => {
        const nodes = set.filter(group => group.parent === next.id);
        nodes.forEach(node => (node.header = 'nodes'));

        next.children = nodes;
        return pre.concat(nodes);
      }, []);

      if (child.length > 0) {
        this.appendNode(child, set, --depth);
      }
      return parent;
    },
    addAttribute(data) {
      if (!Array.isArray(data)) {
        return data;
      }
      return data.map(item => {
        return {
          ...item,
          expand: false,
          selected: false,
          children: this.addAttribute(item.children)
        };
      });
    },
    reset() {
      this.selectedList = [];
      this.groupsData = deepClone(this.nodes);
      this.$refs.groupsTree.reset();
    },
    close() {
      this.open = this.$refs.groupsTree.open;
      this.$refs.groupsTree.closed();
    },
    handleBigReleaseDialogOpen() {
      this.bigReleaseDialogOpened = true;
    },
    async handleBigReleaseDialog(data) {
      await this.updateBigReleasenode(data);
      this.bigReleaseDialogOpened = false;
      successNotify('编辑成功！');

      if (this.release_date !== data.release_date) {
        const reg = /\d{1,}-\d{1,}-\d{1,}/g;
        let { path } = this.$route;
        path = path.replace(reg, data.release_date);
        this.release_date = data.release_date;
        this.$router.push(path);
      }

      this.init();
    },
    goBack() {
      this.$router.push('/release/list');
    },
    async init() {
      await this.queryBigReleaseNodeDetail({
        release_date: this.release_date
      });
      this.detail = this.bigReleaseDetail;
    }
  },
  async created() {
    await this.fetchGroup();
    this.release_date = this.$route.params.release_date;
    await this.queryRqrmntFileUri({ release_date: this.release_date });
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.router-tab
  cursor: pointer;
  padding 0 16px
  height 48px
  display inline-block
  line-height 48px
  box-sizing border-box
  border-bottom 2px solid white
  color #9e9e9e
  font-weight 700
  &.router-link-exact-active
    border-bottom 2px solid #0663BE
    color #0663BE
</style>
