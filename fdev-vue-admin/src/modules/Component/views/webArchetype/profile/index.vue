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
              <span
                v-for="user in detail.manager"
                :key="user.id"
                class="q-pr-xs"
              >
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

          <f-formitem class="col-6" label="描述：">
            <div class="detail-item" :title="detail.desc">
              {{ detail.desc }}
            </div>
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
        </div>
      </fdev-card>

      <fdev-card flat square class="q-mt-md bg-white">
        <fdev-tabs v-model="tab" align="left">
          <fdev-tab name="history" label="历史版本" />
          <fdev-tab name="optimize" label="优化需求" />
        </fdev-tabs>

        <fdev-tab-panels v-model="tab" animated>
          <fdev-tab-panel name="history">
            <ArchetypeHistoryList />
          </fdev-tab-panel>

          <fdev-tab-panel name="optimize" class="q-pt-md">
            <ArchetypeOptimizeList />
          </fdev-tab-panel>
        </fdev-tab-panels>
      </fdev-card>
    </Loading>

    <ArchetypeDialog
      method="updateMpassArchetype"
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
import ArchetypeHistoryList from '@/modules/Component/views/serverArchetype/profile/historyEdition';
import ArchetypeOptimizeList from './optimize';
import ArchetypeDialog from '@/modules/Component/views/webArchetype/components/update';
import EnvConfigDialog from '@/modules/Component/views/serverArchetype/components/envConfigFile';
import { jointNameCn } from '@/modules/Component/utils/utils.js';
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
      tab: 'history',
      archetypeDialogOpened: false,
      modelDeployOpened: false
    };
  },
  computed: {
    ...mapState('componentForm', ['mpassArchetypeDetail']),
    ...mapState('user', ['currentUser']),
    ...mapState('userForm', ['groups']),
    isManger() {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      const isManger = this.mpassArchetypeDetail.manager
        ? this.mpassArchetypeDetail.manager.some(
            user => user.id === this.currentUser.id
          )
        : false;
      return isManger || haveRole;
    },
    managerTitle() {
      return jointNameCn(this.detail.manager);
    }
  },
  methods: {
    ...mapActions('componentForm', ['queryMpassArchetypeDetail']),
    ...mapActions('userForm', ['fetchGroup']),

    async init() {
      this.loading = true;
      await this.queryMpassArchetypeDetail({
        id: this.id
      });
      this.detail = {
        groupObj: this.mpassArchetypeDetail.group
          ? this.groups.find(
              group => this.mpassArchetypeDetail.group === group.id
            ) || ''
          : null,
        groupFullName: this.mpassArchetypeDetail.group
          ? getGroupFullName(this.groups, this.mpassArchetypeDetail.group) || ''
          : '',
        ...this.mpassArchetypeDetail
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
