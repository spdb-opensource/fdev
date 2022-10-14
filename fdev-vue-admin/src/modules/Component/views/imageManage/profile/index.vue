<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-card flat square class="q-pa-md bg-white">
        <fdev-card-section class="row">
          <f-formitem class="col-6" label="镜像英文名：">
            <div class="detail-item" :title="baseImageDetail.name">
              {{ baseImageDetail.name }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ baseImageDetail.name }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </f-formitem>

          <f-formitem class="col-6" label="镜像中文名：">
            <div class="detail-item" :title="baseImageDetail.name_cn">
              {{ baseImageDetail.name_cn }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ baseImageDetail.name_cn }}
                </fdev-banner>
              </fdev-popup-proxy>
            </div>
          </f-formitem>

          <f-formitem class="col-6" label="管理员：">
            <div class="q-gutter-x-sm detail-item" :title="managerTitle">
              <span v-for="user in baseImageDetail.manager" :key="user.id">
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
          <f-formitem class="col-6" label="目标环境：">
            <div class="detail-item" :title="baseImageDetail.target_env">
              {{ baseImageDetail.target_env }}
            </div>
          </f-formitem>

          <f-formitem class="col-6" label="镜像类型：">
            <div class="detail-item" :title="baseImageDetail.type">
              {{ baseImageDetail.type }}
            </div>
          </f-formitem>

          <f-formitem class="col-6" label="Gitlab 地址：">
            <a
              :href="baseImageDetail.gitlab_url"
              target="_blank"
              class="link detail-item"
              :title="baseImageDetail.gitlab_url"
            >
              {{ baseImageDetail.gitlab_url }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ baseImageDetail.gitlab_url }}
                </fdev-banner>
              </fdev-popup-proxy>
            </a>
          </f-formitem>

          <f-formitem class="col-6" label="wiki 地址：">
            <a
              :href="baseImageDetail.wiki"
              target="_blank"
              class="link detail-item"
              v-if="baseImageDetail.wiki"
              :title="baseImageDetail.wiki"
            >
              {{ baseImageDetail.wiki }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ baseImageDetail.wiki }}
                </fdev-banner>
              </fdev-popup-proxy>
            </a>
          </f-formitem>

          <f-formitem class="col-6" label="镜像描述：">
            <div class="detail-item" :title="baseImageDetail.description">
              {{ baseImageDetail.description }}
            </div>
          </f-formitem>

          <f-formitem label="是否涉及内测：" class="col-6">
            {{ baseImageDetail.isTest === '1' ? '是' : '否' }}
          </f-formitem>

          <f-formitem class="col-12" label="元数据：">
            <ul>
              <li
                v-for="(value, key) in baseImageDetail.meta_data_declare"
                :key="key"
              >
                <span>{{ key }}</span
                >:
                <span>{{ value }}</span>
              </li>
            </ul>
          </f-formitem>
        </fdev-card-section>

        <div class="text-center q-gutter-md">
          <span>
            <fdev-tooltip v-if="!isManger">
              需要镜像管理员或基础架构管理员权限
            </fdev-tooltip>
            <fdev-btn
              :disable="!isManger"
              label="信息维护"
              @click="dialogOpen = true"
            />
          </span>
        </div>
      </fdev-card>

      <fdev-card flat square class="q-mt-md bg-white">
        <fdev-tabs v-model="tab" class="q-mb-lg" align="left">
          <fdev-tab name="history" label="历史版本" />
          <fdev-tab name="appUsing" label="应用使用现状" />
          <fdev-tab name="archetypeUsing" label="骨架使用现状" />
          <fdev-tab name="optimize" label="优化需求" />
        </fdev-tabs>

        <fdev-tab-panels v-model="tab" animated>
          <fdev-tab-panel name="history">
            <HistoryList :id="id" :source="detail ? detail.source : '4'" />
          </fdev-tab-panel>
          <fdev-tab-panel name="appUsing">
            <AppUsingStatus
              v-if="nameImage"
              :isFrameWork="true"
              :nameImage="nameImage"
            />
          </fdev-tab-panel>
          <fdev-tab-panel name="archetypeUsing" v-if="showImageUsingStatus">
            <ImageArchetypeUsingStatus
              v-if="nameImage"
              :nameImage="nameImage"
            />
          </fdev-tab-panel>
          <fdev-tab-panel name="optimize"> <OptimizeList /> </fdev-tab-panel>
        </fdev-tab-panels>
      </fdev-card>
    </Loading>

    <AddImageDialog
      operation="update"
      v-model="dialogOpen"
      title="镜像信息维护"
      :data="detail"
      @input="init"
    />
  </f-block>
</template>

<script>
import { getGroupFullName } from '@/utils/utils';
import Loading from '@/components/Loading';
import { mapActions, mapState } from 'vuex';
import AddImageDialog from '@/modules/Component/views/imageManage/components/addAndUpdate';
import HistoryList from '@/modules/Component/views/imageManage/profile/historyEdition';
import OptimizeList from '@/modules/Component/views/imageManage/profile/optimize';
import {
  setImagetypeTab,
  getImageTypeTab
} from '@/modules/Component/utils/setting.js';
import { jointNameCn } from '@/modules/Component/utils/utils.js';
import AppUsingStatus from '@/modules/Component/views/serverComponent/profile/appUsingStatus';
import ImageArchetypeUsingStatus from './archetypeUsingStatus';

export default {
  name: 'ImageManageProfile',
  components: {
    OptimizeList,
    HistoryList,
    AddImageDialog,
    Loading,
    AppUsingStatus,
    ImageArchetypeUsingStatus
  },
  data() {
    return {
      loading: false,
      id: '',
      detail: {},
      dialogOpen: false,
      tab: getImageTypeTab() || 'history',
      showImageUsingStatus: false,
      showArcheUsingStatus: false,
      nameImage: ''
    };
  },
  watch: {
    tab(val) {
      setImagetypeTab(val);
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
    ...mapState('componentForm', ['baseImageDetail']),
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
        // v => v.label === '基础架构管理员'
        function(v) {
          return v.label === '基础架构管理员';
        }
      );
      const isManger = this.baseImageDetail.manager
        ? this.baseImageDetail.manager.some(
            user => user.id === this.currentUser.id
          )
        : false;
      return isManger || haveRole;
    },
    managerTitle() {
      return jointNameCn(this.baseImageDetail.manager);
    }
  },
  methods: {
    ...mapActions('componentForm', ['queryBaseImageDetail']),
    ...mapActions('userForm', ['fetchGroup']),
    async init() {
      this.loading = true;
      await this.queryBaseImageDetail({
        id: this.id
      });
      this.nameImage = this.baseImageDetail.name;
      this.showImageUsingStatus = true;
      this.detail = {
        groupObj: this.baseImageDetail.group
          ? this.groups.find(
              group => this.baseImageDetail.group === group.id
            ) || ''
          : null,
        groupFullName: this.baseImageDetail.group
          ? getGroupFullName(this.groups, this.baseImageDetail.group) || ''
          : '',
        ...this.baseImageDetail
      };
      this.loading = false;
    }
  },
  async created() {
    this.id = this.$route.params.id;
    await this.fetchGroup();
    await this.init();
  }
};
</script>
<style scoped lang="stylus">

li,ul
  list-style none
  padding  0
  margin  0
.detail-item
  display inline-block
  width 100%
  overflow hidden
  text-overflow ellipsis
  white-space nowrap
</style>
