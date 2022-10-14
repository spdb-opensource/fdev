<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-card flat square class="q-pa-md bg-white">
        <fdev-card-section class="row">
          <f-formitem class="col-6" label="骨架英文名：">
            <div class="detail-item" :title="detail.name_en">
              {{ detail.name_en }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ detail.name_en }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </f-formitem>

          <f-formitem class="col-6" label="骨架中文名：">
            <div class="detail-item" :title="detail.name_cn">
              {{ detail.name_cn }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ detail.name_cn }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </f-formitem>

          <f-formitem class="col-6" label="骨架管理员：">
            <div class="detail-item" :title="managerTitle">
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
                &nbsp;
              </span>
            </div>
          </f-formitem>

          <f-formitem class="col-6" label="所属组：">
            <div class="detail-item" :title="detail.groupFullName">
              {{ detail.groupFullName }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ detail.groupFullName }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </f-formitem>

          <f-formitem class="col-6" label="groupId：">
            <div class="detail-item" :title="detail.groupId">
              {{ detail.groupId }}
            </div>
          </f-formitem>

          <f-formitem class="col-6" label="artifactId：">
            <div class="detail-item" :title="detail.artifactId">
              {{ detail.artifactId }}
            </div>
          </f-formitem>

          <f-formitem class="col-6" label="骨架类型：">
            <div class="detail-item" :title="detail.type">
              {{ detail.type }}
            </div>
          </f-formitem>

          <f-formitem class="col-6" label="项目编码格式：">
            <div class="detail-item" :title="detail.encoding">
              {{ detail.encoding }}
            </div>
          </f-formitem>

          <f-formitem class="col-6" label="Gitlab地址：">
            <a
              :href="detail.gitlab_url"
              target="_blank"
              class="link detail-item"
              :title="detail.gitlab_url"
            >
              {{ detail.gitlab_url }}
            </a>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ detail.gitlab_url }}
              </fdev-banner>
            </fdev-popup-proxy>
          </f-formitem>

          <f-formitem class="col-6" label="环境配置文件路径：">
            <div class="detail-item" :title="detail.application_path">
              {{ detail.application_path }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ detail.application_path }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </f-formitem>

          <f-formitem class="col-6" label="描述：">
            <div class="detail-item" :title="detail.desc">
              {{ detail.desc }}
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
            label="信息维护"
            @click="archetypeDialogOpened = true"
            v-if="isManger"
          />
          <fdev-btn
            label="环境参数"
            v-show="archetypeDetail.type !== 'vue'"
            @click="modelDeployOpened = true"
          />
        </div>
      </fdev-card>

      <fdev-card flat square class="q-mt-md bg-white">
        <fdev-tabs v-model="tab" class="q-mb-md" align="left">
          <fdev-tab name="history" label="历史版本" />
          <fdev-tab name="optimize" label="优化需求" />
        </fdev-tabs>

        <fdev-tab-panels v-model="tab" animated>
          <fdev-tab-panel name="history">
            <ArchetypeHistoryList />
          </fdev-tab-panel>

          <fdev-tab-panel name="optimize">
            <ArchetypeOptimizeList />
          </fdev-tab-panel>
        </fdev-tab-panels>
      </fdev-card>
    </Loading>

    <ArchetypeDialog
      method="updateArchetype"
      :data="detail"
      v-model="archetypeDialogOpened"
      @click="init"
    />
    <EnvConfigDialog
      @click="init"
      :data="detail"
      :value="modelDeployOpened"
      v-model="modelDeployOpened"
      :isEditable="false"
    />
  </f-block>
</template>

<script>
import { getGroupFullName } from '@/utils/utils';
import Loading from '@/components/Loading';
import { mapActions, mapState } from 'vuex';
import ArchetypeHistoryList from './historyEdition';
import ArchetypeOptimizeList from './optimize';
import ArchetypeDialog from '@/modules/Component/views/serverArchetype/components/addAndUpdate';
import {
  setArchetypeTab,
  getArchetypeTab
} from '@/modules/Component/utils/setting.js';
import { jointNameCn } from '@/modules/Component/utils/utils.js';
import EnvConfigDialog from '@/modules/Component/views/serverArchetype/components/envConfigFile';
export default {
  name: 'ArchetypeProfile',
  components: {
    ArchetypeHistoryList,
    Loading,
    ArchetypeDialog,
    ArchetypeOptimizeList,
    EnvConfigDialog
  },
  data() {
    return {
      id: '',
      loading: false,
      detail: {},
      dialogOpen: false,
      tab: getArchetypeTab() || 'history',
      archetypeDialogOpened: false,
      modelDeployOpened: false
    };
  },
  computed: {
    ...mapState('componentForm', ['archetypeDetail']),
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', ['groups']),
    isManger() {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      const isManger = this.archetypeDetail.manager_id
        ? this.archetypeDetail.manager_id.some(
            user => user.id === this.currentUser.id
          )
        : false;
      return isManger || haveRole;
    },
    managerTitle() {
      return jointNameCn(this.detail.manager_id);
    }
  },
  watch: {
    tab(val) {
      setArchetypeTab(val);
    }
  },
  methods: {
    ...mapActions('componentForm', ['queryArchetypeDetail']),
    ...mapActions('userForm', ['fetchGroup']),

    async init() {
      this.loading = true;
      await this.queryArchetypeDetail({
        id: this.id
      });
      this.detail = {
        groupObj: this.archetypeDetail.group
          ? this.groups.find(
              group => this.archetypeDetail.group === group.id
            ) || ''
          : null,
        groupFullName: this.archetypeDetail.group
          ? getGroupFullName(this.groups, this.archetypeDetail.group) || ''
          : '',
        ...this.archetypeDetail
      };
      this.loading = false;
    }
  },
  async created() {
    await this.fetchGroup();
    this.id = this.$route.params.id;
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
