<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-card flat square class="q-pa-md bg-white">
        <fdev-card-section class="row">
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

          <f-formitem label="组件管理员：" class="col-6">
            <div class="q-gutter-x-sm detail-item" :title="managerTitle">
              <span v-for="user in detail.manager" :key="user.id">
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

          <f-formitem label="组件推荐版本：" class="col-6">
            <div class="detail-item" :title="detail.recommond_version">
              {{ detail.recommond_version }}
            </div>
          </f-formitem>

          <f-formitem label="组件来源：" class="col-6">
            <div class="detail-item" :title="detail.source | sourceFilter">
              {{ detail.source | sourceFilter }}
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

          <f-formitem label="组件类型：" class="col-6">
            <div class="detail-item" :title="detail.type | typeFilter">
              {{ detail.type | typeFilter }}
            </div>
          </f-formitem>

          <f-formitem label="npm坐标name：" class="col-6">
            <div class="detail-item" :title="detail.npm_name">
              {{ detail.npm_name }}
            </div>
          </f-formitem>

          <f-formitem label="npm坐标group：" class="col-6">
            <div class="detail-item" :title="detail.npm_group">
              {{ detail.npm_group }}
            </div>
          </f-formitem>

          <f-formitem label="技术栈：" class="col-6">
            <div class="detail-item" :title="detail.skillstack">
              {{ detail.skillstack }}
            </div>
          </f-formitem>

          <f-formitem label="业务领域：" class="col-6">
            <div class="detail-item" :title="detail.businessarea">
              {{ detail.businessarea }}
            </div>
          </f-formitem>

          <f-formitem label="Gitlab 地址：" class="col-6">
            <a
              :href="detail.gitlab_url"
              target="_blank"
              class="link detail-item"
              :title="detail.gitlab_url"
            >
              {{ detail.gitlab_url }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ detail.gitlab_url }}
                </fdev-banner>
              </fdev-popup-proxy>
            </a>
          </f-formitem>

          <f-formitem label="组件描述：" class="col-6">
            <div class="detail-item">
              <span v-html="detail.desc" />
            </div>
          </f-formitem>
          <f-formitem label="是否涉及内测：" class="col-6">
            {{ detail.isTest === '1' ? '是' : '否' }}
          </f-formitem>
        </fdev-card-section>
        <div class="text-center q-gutter-md" v-if="isManger()">
          <fdev-btn label="信息维护" dialog @click="dialogOpen = true" />
          <fdev-btn label="返回" dialog @click="goBack" />
        </div>
      </fdev-card>

      <fdev-card flat square class="q-mt-md bg-white">
        <fdev-tabs v-model="tab" class="q-mb-lg" align="left">
          <fdev-tab name="history" label="历史版本" />
          <fdev-tab
            v-if="!isBusinessCom"
            name="appUsing"
            label="应用使用现状"
          />
          <fdev-tab
            name="optimize"
            label="投产窗口"
            v-if="detail.source !== '1'"
          />
        </fdev-tabs>

        <fdev-tab-panels v-model="tab" animated>
          <fdev-tab-panel name="history">
            <WebHistory :managers="detail.manager" />
          </fdev-tab-panel>
          <fdev-tab-panel name="appUsing">
            <WebAppUsing />
          </fdev-tab-panel>
          <fdev-tab-panel name="optimize">
            <WebOptimize :componentType="detail.type" />
          </fdev-tab-panel>
        </fdev-tab-panels>
      </fdev-card>
    </Loading>

    <UpdateDialog
      v-model="dialogOpen"
      title="组件信息维护"
      :data="detail"
      @input="init"
    />
  </f-block>
</template>

<script>
import { getGroupFullName } from '@/utils/utils';
import Loading from '@/components/Loading';
import UpdateDialog from '@/modules/Component/views/webComponent/components/addAndUpdate';
import WebHistory from './historyList';
import WebAppUsing from './appUsingStatus';
import WebOptimize from './optimizeRel';
import { mapState, mapActions } from 'vuex';
import { setWebTab, getWebTab } from '@/modules/Component/utils/setting.js';
import {
  ComponentModelType,
  webTypeOptions,
  webSourceOptions
} from '@/modules/Component/utils/constants.js';
import { jointNameCn } from '@/modules/Component/utils/utils.js';

export default {
  components: {
    Loading,
    UpdateDialog,
    WebHistory,
    WebAppUsing,
    WebOptimize
  },
  data() {
    return {
      loading: false,
      dialogOpen: false,
      detail: {},
      tab: getWebTab() || 'history'
    };
  },
  filters: {
    typeFilter(val) {
      const typeItem = webTypeOptions.find(item => item.value === val);
      return typeItem ? typeItem.label : '-';
    },
    sourceFilter(val) {
      const sourceItem = webSourceOptions.find(item => item.value === val);
      return sourceItem ? sourceItem.label : '-';
    }
  },
  watch: {
    tab(val) {
      setWebTab(val);
    }
  },
  computed: {
    ...mapState('componentForm', ['mpassComDetail']),
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', ['groups']),
    isManger() {
      return function() {
        const haveRole = this.currentUser.role.some(
          v => v.label === '基础架构管理员'
        );
        const isManger = this.detail.manager
          ? this.detail.manager.some(user => user.id === this.currentUser.id)
          : false;
        return isManger || haveRole;
      };
    },
    isBusinessCom() {
      return this.detail.type === ComponentModelType.Business;
    },
    managerTitle() {
      return jointNameCn(this.detail.manager);
    }
  },
  methods: {
    ...mapActions('componentForm', ['queryMpassComponentDetail']),
    ...mapActions('userForm', ['fetchGroup']),
    async init() {
      const id = this.$route.params.id;
      await this.queryMpassComponentDetail({ id });
      this.detail = {
        groupFullName: this.mpassComDetail.group
          ? getGroupFullName(this.groups, this.mpassComDetail.group) || ''
          : '',
        ...this.mpassComDetail
      };

      if (this.detail.source === '1') {
        this.tab = 'history';
      }
    }
  },
  async created() {
    await this.fetchGroup();
    this.init();
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
