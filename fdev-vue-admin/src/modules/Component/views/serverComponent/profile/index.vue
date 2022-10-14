<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-card flat square class="q-pa-md bg-white">
        <fdev-card-section class="row">
          <f-formitem label="组件英文名：" class="col-6">
            <div class="detail-item" :title="detail.name_en">
              {{ detail.name_en }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ detail.name_en }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </f-formitem>

          <f-formitem label="组件中文名：" class="col-6">
            <div class="detail-item" :title="detail.name_cn">
              {{ detail.name_cn }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ detail.name_cn }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </f-formitem>

          <f-formitem label="组件管理员：" class="col-6">
            <div class="q-gutter-x-sm detail-item" :title="managerTitle">
              <span v-for="user in detail.manager_id" :key="user.id">
                <router-link
                  :to="{ path: `/user/list/${user.id}` }"
                  class="link"
                  v-if="user.id"
                >
                  <span>{{ user.user_name_cn }}</span>
                </router-link>
                <span v-else>
                  {{ user.user_name_cn }}
                </span>
              </span>
            </div>
          </f-formitem>

          <f-formitem label="开发jdk版本：" class="col-6">
            <div
              class="detail-item"
              :title="detail.jdk_version ? detail.jdk_version : '未知'"
            >
              {{ detail.jdk_version ? detail.jdk_version : '未知' }}
            </div>
          </f-formitem>

          <f-formitem label="所属小组：" class="col-6">
            <div class="detail-item" :title="detail.groupFullName">
              {{ detail.groupFullName }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ detail.groupFullName }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </f-formitem>

          <f-formitem label="groupId：" class="col-6">
            <div class="detail-item" :title="detail.groupId">
              {{ detail.groupId }}
            </div>
          </f-formitem>

          <f-formitem label="artifactId：" class="col-6">
            <div class="detail-item" :title="detail.artifactId">
              {{ detail.artifactId }}
            </div>
          </f-formitem>

          <f-formitem label="组件类型：" class="col-6">
            <div class="detail-item" :title="detail.type | textFilter">
              {{ detail.type | textFilter }}
            </div>
          </f-formitem>

          <f-formitem label="父组件：" class="col-6">
            <div class="detail-item">
              <router-link
                :to="{
                  path: `/componentManage/server/list/${detail.parentId}`
                }"
                class="link"
                v-if="detail.parentId"
              >
                <span class="text-ellipsis" :title="detail.parent_nameen">
                  {{ detail.parent_nameen }}
                </span>
              </router-link>
              <span v-else>-</span>
            </div>
          </f-formitem>

          <f-formitem label="组件来源：" class="col-6">
            <div class="detail-item" :title="detail.source | sourceFilter">
              {{ detail.source | sourceFilter }}
            </div>
          </f-formitem>

          <f-formitem label="代码根路径：" class="col-6">
            <div class="detail-item" :title="detail.root_dir">
              {{ detail.root_dir || '-' }}
            </div>
          </f-formitem>

          <f-formitem label="Gitlab地址：" class="col-6">
            <a
              :href="detail.gitlab_url"
              target="_blank"
              class="link detail-item"
              :title="detail.gitlab_url"
            >
              {{ detail.gitlab_url || '-' }}
            </a>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ detail.gitlab_url }}
              </fdev-banner>
            </fdev-popup-proxy>
          </f-formitem>

          <f-formitem label="wiki地址：" class="col-6">
            <a
              :href="detail.wiki_url"
              target="_blank"
              class="link detail-item"
              v-if="detail.wiki_url"
              :title="detail.wiki_url"
            >
              {{ detail.wiki_url || '-' }}
            </a>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ detail.wiki_url }}
              </fdev-banner>
            </fdev-popup-proxy>
          </f-formitem>

          <f-formitem label="组件描述：" class="col-6">
            <div class="detail-item">
              <span v-html="desc" />
            </div>
          </f-formitem>

          <f-formitem label="sonar扫描卡点：" class="col-6">
            {{ detail.sonar_scan_switch === '1' ? '开' : '关' }}
          </f-formitem>
          <f-formitem label="是否涉及内测：" class="col-6">
            {{ detail.isTest === '1' ? '是' : '否' }}
          </f-formitem>
        </fdev-card-section>
        <div class="text-center q-gutter-md">
          <fdev-btn
            label="修改历史"
            @click="modifyVersionDialogOpen = true"
            v-if="detail.source === '1'"
          />
          <fdev-btn
            label="信息维护"
            @click="dialogOpen = true"
            v-if="isManger"
          />
          <fdev-btn label="返回" @click="goBack" />
        </div>
      </fdev-card>

      <fdev-card flat square class="q-mt-md bg-white">
        <fdev-tabs v-model="tab" class="q-mb-lg" align="left">
          <fdev-tab name="history" label="历史版本" />
          <fdev-tab name="appUsing" label="应用使用现状" />
          <fdev-tab name="archetypeUsing" label="骨架使用现状" />
          <fdev-tab
            name="optimize"
            label="优化需求"
            v-if="detail.source === '0' && detail.type !== '3'"
          />
          <fdev-tab
            name="optimizeRel"
            label="投产窗口"
            v-if="detail.type === '3'"
          />
        </fdev-tabs>

        <fdev-tab-panels v-model="tab" animated>
          <fdev-tab-panel name="history">
            <HistoryList
              :source="detail ? detail.source : '4'"
              ref="historyList"
              @input="init"
            />
          </fdev-tab-panel>

          <fdev-tab-panel name="appUsing">
            <AppUsingStatus />
          </fdev-tab-panel>

          <fdev-tab-panel name="archetypeUsing">
            <ArchetypeUsingStatus />
          </fdev-tab-panel>

          <fdev-tab-panel
            name="optimize"
            v-if="detail.source === '0' && detail.type !== '3'"
          >
            <OptimizeList />
          </fdev-tab-panel>
          <!-- 组件类型为多模块组件，使用投产窗口tab页 -->
          <fdev-tab-panel name="optimizeRel" v-if="detail.type === '3'">
            <OptimizeRel />
          </fdev-tab-panel>
        </fdev-tab-panels>
      </fdev-card>
    </Loading>

    <UpdateDialog
      v-model="dialogOpen"
      title="组件信息维护"
      :data="detail"
      @input="init"
      @history="refreshHistory"
    />

    <ModifyVersionDialog
      v-model="modifyVersionDialogOpen"
      title="推荐版本修改历史"
      :data="detail"
      input="false"
    />
  </f-block>
</template>

<script>
import { getGroupFullName } from '@/utils/utils';
import Loading from '@/components/Loading';
import { mapActions, mapState, mapMutations } from 'vuex';
import UpdateDialog from '@/modules/Component/views/serverComponent/components/addAndUpdate.vue';
import ModifyVersionDialog from '@/modules/Component/views/serverComponent/components/modifyVersionDialog.vue';
import HistoryList from './historyList';
import AppUsingStatus from './appUsingStatus';
import ArchetypeUsingStatus from './archetypeUsingStatus';
import OptimizeList from './optimizeList';
import { setTab, getTab } from '@/modules/Component/utils/setting.js';
import { jointNameCn } from '@/modules/Component/utils/utils.js';
import {
  typeOptions,
  sourceOptions
} from '@/modules/Component/utils/constants.js';
import OptimizeRel from './optimizeRel';

export default {
  name: 'ComponentProfile',
  components: {
    OptimizeList,
    AppUsingStatus,
    HistoryList,
    UpdateDialog,
    ModifyVersionDialog,
    Loading,
    ArchetypeUsingStatus,
    OptimizeRel
  },
  data() {
    return {
      loading: false,
      id: '',
      detail: {},
      dialogOpen: false,
      tab: getTab() || 'history',
      modifyVersionDialogOpen: false
    };
  },
  filters: {
    textFilter(val) {
      return typeOptions[val] ? typeOptions[val].label : '-';
    },
    sourceFilter(val) {
      return sourceOptions[val] ? sourceOptions[val].label : '-';
    }
  },
  watch: {
    tab(val) {
      setTab(val);
    },
    '$route.params': {
      deep: true,
      handler(val) {
        this.id = val.id;
        this.init();
      }
    }
  },
  computed: {
    ...mapState('componentForm', ['componentDetail']),
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', ['groups']),
    desc() {
      const desc = this.detail.desc ? this.detail.desc : '';
      return desc
        .replace(/</g, '&lt;')
        .replace(/</g, '&gt;')
        .replace(/\n/g, '<br/>');
    },
    isManger() {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      const isManger = this.componentDetail.manager_id
        ? this.componentDetail.manager_id.some(
            user => user.id === this.currentUser.id
          )
        : false;
      return isManger || haveRole;
    },
    managerTitle() {
      return jointNameCn(this.detail.manager_id);
    }
  },
  methods: {
    ...mapActions('componentForm', ['queryComponentDetail']),
    ...mapActions('userForm', ['fetchGroup']),
    ...mapMutations(
      'userActionSaveComponent/componentManage/componentList/ArcheUsing',
      ['updateSelectValue', 'updateType', 'updateComponentType']
    ),
    ...mapMutations(
      'userActionSaveComponent/componentManage/componentList/appUsingStatus',
      {
        updateSelectValue_com: 'updateSelectValue',
        updateType_com: 'updateType',
        updateUseVersion_com: 'updateUseVersion'
      }
    ),
    refreshHistory() {
      this.$refs['historyList'].init();
    },
    async init() {
      this.loading = true;
      await this.queryComponentDetail({
        id: this.id
      });

      if (this.componentDetail.source !== '0' && getTab() === 'optimize') {
        this.tab = 'history';
      }

      this.$q.sessionStorage.set('jdk', this.componentDetail.jdk_version);
      if (!this.componentDetail.group) {
        this.$set(this.componentDetail, 'group', '');
      }
      this.detail = {
        groupFullName: this.componentDetail.group
          ? getGroupFullName(this.groups, this.componentDetail.group) || ''
          : '',
        ...this.componentDetail
      };

      if (
        (this.detail.type === '3' && getTab() === 'optimize') ||
        (this.detail.type !== '3' && getTab() === 'optimizeRel')
      ) {
        this.tab = 'history';
      }
      this.loading = false;
    }
  },
  async created() {
    this.id = this.$route.params.id;
    await this.fetchGroup();
    this.init();
  },
  beforeRouteLeave(to, from, next) {
    // 重置骨架使用现状查询条件条件
    this.updateSelectValue([]);
    this.updateType('0');
    this.updateComponentType('0');
    // 重置骨架使用现状查询条件条件
    this.updateSelectValue_com([]);
    this.updateType_com('全部');
    this.updateUseVersion_com('全部');
    next();
  }
};
</script>

<style lang="stylus" scoped>
.detail-item
  display inline-block
  width 100%
  overflow hidden
  text-overflow ellipsis
  white-space nowrap
</style>
