<template>
  <f-block>
    <div>
      <div class="text-h6 text-title">模板详情</div>
      <div class="row q-pl-lg q-my-md">
        <f-formitem class="col-6" label="小组名称">
          {{ group }}
        </f-formitem>
        <f-formitem class="col-6" label="小组系统缩写">
          {{ system_abbr }}
        </f-formitem>
        <f-formitem class="col-6" label="所属系统">
          {{ data.owner_system_name }}
        </f-formitem>
        <f-formitem class="col-6" label="所属应用">
          {{ data.owner_app_name }}
        </f-formitem>
        <f-formitem class="col-6" label="变更类型">
          {{ data.template_type | getType }}
        </f-formitem>
        <f-formitem class="col-6" label="上传人">
          <router-link
            v-if="data.update_username_cn"
            :to="{ path: `/user/list/${data.update_user}` }"
            class="link"
          >
            {{ data.update_username_cn }}
          </router-link>
        </f-formitem>
        <f-formitem class="col-6" label="更新时间">
          {{ data.update_time }}
        </f-formitem>
        <f-formitem class="col-6" label="配置文件地址">
          <div
            class="row text-grey-8 items-center"
            v-if="data.resource_giturl && data.resource_giturl[0]"
          >
            <a
              class="ellipsis overflow inline-block"
              style="max-width:calc(100% - 30px)"
              target="_blank"
              :title="data.resource_giturl[0]"
              :href="data.resource_giturl[0]"
            >
              {{ data.resource_giturl[0] }}
            </a>
            <f-icon name="arrow_d_f" class="cursor-pointer" />
            <fdev-popup-proxy>
              <div v-for="data in data.resource_giturl" :key="data">
                <a target="_blank" :href="data">{{ data }}</a>
                <fdev-separator />
              </div>
            </fdev-popup-proxy>
          </div>
        </f-formitem>
      </div>
    </div>
    <fdev-separator />
    <div class="q-mt-lg">
      <div class="text-h6 text-title">变更目录</div>
      <div
        class="row q-ml-lg q-mt-md"
        v-for="(item, index) in data.catalogs"
        :key="index"
      >
        <div class="col-4">
          <span class="text-grey-3">目录：</span>
          <span>{{ item.catalog_name }}</span>
        </div>
        <div class="col-4">
          <span class="text-grey-3">步骤：</span>
          <span>{{ item.description }}</span>
        </div>
        <div class="col-4">
          <span class="text-grey-3">类型：</span>
          <span>{{ item.catalog_type | typeName }}</span>
        </div>
      </div>
      <div class="q-gutter-md text-center q-mt-lg q-mb-md">
        <span>
          <fdev-btn
            label="修改"
            @click="openDialog"
            :disable="!((role && isthirdLevelGroup) || isKaDianManager)"
          />
          <fdev-tooltip
            v-if="!((role && isthirdLevelGroup) || isKaDianManager)"
          >
            请联系当前变更模板所属小组成员的第三层级组及其子组的投产管理员
          </fdev-tooltip>
        </span>
        <span>
          <fdev-btn
            label="删除"
            color="red"
            @click="deleted(data.id)"
            :disable="!((role && isthirdLevelGroup) || isKaDianManager)"
          />
          <fdev-tooltip
            v-if="!((role && isthirdLevelGroup) || isKaDianManager)"
          >
            请联系当前变更模板所属小组成员的第三层级组及其子组的投产管理员
          </fdev-tooltip>
        </span>
        <span>
          <fdev-btn label="返回" @click="goBack" />
        </span>
      </div>
    </div>

    <EditDialog
      v-model="editTemplate"
      url="update"
      :thirdLevelGroups="thirdLevelGroups && thirdLevelGroups.groups"
      :data="dialogData"
      title="编辑"
      @submit="handleTemplate"
    />
  </f-block>
</template>
<script>
import { successNotify, getGroupFullName } from '@/utils/utils';
import EditDialog from './componetns/EditDialog';
import { mapState, mapActions, mapGetters } from 'vuex';
import { type } from '../../utils/model';

export default {
  data() {
    return {
      editTemplate: false,
      id: '', // 从路由获取id
      data: {},
      dialogData: {},
      system_abbr: ''
    };
  },
  components: {
    EditDialog
  },
  computed: {
    ...mapState('releaseForm', [
      'groupSysAbbr',
      'templateDetail',
      'thirdLevelGroups'
    ]),
    ...mapState('user', ['currentUser']),
    ...mapState('release', ['system']),
    ...mapState('userForm', ['groups']),
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),
    role() {
      const roler = this.currentUser.role.find(role => {
        return role.name === '投产管理员';
      });
      return Boolean(roler);
    },
    group() {
      return getGroupFullName(this.groups, this.templateDetail.owner_group);
    },
    // 判断当前变更模板所属小组是否属于当前用户的第三层级组及子组
    isthirdLevelGroup() {
      return (
        this.thirdLevelGroups &&
        !!this.thirdLevelGroups.groups.find(
          item => item.owner_groupId === this.data.owner_group
        )
      );
    }
  },
  methods: {
    ...mapActions('release', ['getSystem']),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapActions('releaseForm', {
      deleteTemplate: 'deleteTemplate',
      queryGroupSysAbbr: 'queryGroupSysAbbr',
      queryTemplateDetail: 'getTemplateDetail',
      queryThreeLevelGroups: 'queryThreeLevelGroups'
    }),
    // 打开编辑弹窗
    openDialog() {
      let data = JSON.parse(JSON.stringify(this.data));
      this.dialogData = data;
      this.editTemplate = true;
    },
    // 弹窗点击确定，组件传数据
    handleTemplate(data) {
      this.editTemplate = data;
      this.getTemplateDetail();
      this.getSystem();
    },
    // 获取变更模板详情
    async getTemplateDetail() {
      await this.queryTemplateDetail({ id: this.id });
      this.data = { ...this.templateDetail };
      /* 如果当前模版没有小组，默认当前小组 */
      if (!this.data.owner_group) {
        this.data.owner_group = this.currentUser.group_id;
        this.data.owner_group_name = this.currentUser.group.name;
      }
      if (this.system.length === 0) {
        await this.getSystem();
      }
      const item = this.system.find(item => {
        return item.id === this.data.owner_system;
      });
      this.data.resource_giturl =
        item && item.resource_giturl ? item.resource_giturl : [];
      /* 根据小组查询小组系统缩写 */
      await this.queryGroupSysAbbr({
        group_id: this.data.owner_group
      });
      this.system_abbr = this.groupSysAbbr ? this.groupSysAbbr : '';
    },
    async deleted(id) {
      this.$q
        .dialog({
          title: '确认删除',
          message: '您确定删除该模板吗?',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteTemplate({ id: id });
          successNotify('删除成功');
          history.back();
        });
    },
    // 获取当前用户的第三层级组及其子组
    getThirdLevelGroups() {
      const thirdLevelGroups = this.thirdLevelGroups;
      if (
        !thirdLevelGroups ||
        thirdLevelGroups.user_id !== this.currentUser.id
      ) {
        this.queryThreeLevelGroups();
      }
    }
  },
  async created() {
    this.id = this.$route.params.id;
    this.fetchGroup();
    this.getTemplateDetail();
    this.getThirdLevelGroups();
  },
  filters: {
    getType(val) {
      return val === 'gray' ? '灰度' : '生产';
    },
    typeName(val) {
      return type[val];
    }
  }
};
</script>

<style lang="stylus" scoped>
a
  color #027be3
.text-title
  font-weight 700
</style>
