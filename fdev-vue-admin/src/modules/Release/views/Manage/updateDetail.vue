<template>
  <div>
    <f-block class="q-mb-md">
      <div class="row">
        <f-formitem class="col-4" label="投产窗口：">
          <router-link
            :title="detailData.release_node_name"
            v-if="detailData.launcher_name_cn"
            :to="{
              path: `/release/list/${detailData.release_node_name}/createUpdate`
            }"
            class="link ellipsis-3-lines no-wrap"
          >
            {{ detailData.release_node_name }}
          </router-link>
        </f-formitem>
        <f-formitem class="col-4" label="变更日期：">{{
          detailData.date
        }}</f-formitem>
        <f-formitem class="col-4" label="变更单号：">{{
          detailData.prod_spdb_no
        }}</f-formitem>
        <f-formitem class="col-4" label="变更类型：">{{
          detailData.type | getType
        }}</f-formitem>
        <f-formitem class="col-4" label="发起人：">
          <router-link
            v-if="detailData.launcher_name_cn"
            :to="{ path: `/user/list/${detailData.launcher}` }"
            class="link"
          >
            {{ detailData.launcher_name_cn }}
          </router-link>
        </f-formitem>
        <f-formitem class="col-4" label="创建时间：">{{
          detailData.create_time
        }}</f-formitem>
        <f-formitem class="col-4" label="变更版本：">{{
          detailData.version
        }}</f-formitem>
        <f-formitem class="col-4" label="应用系统：">{{
          detailData.owner_system_name || '-'
        }}</f-formitem>
        <f-formitem
          class="col-4"
          label="模板名称"
          v-if="detailData.image_deliver_type === '1'"
        >
          <router-link
            v-if="detailData.template_id"
            :to="{
              path: `/release/templateDetail/${detailData.template_id}`
            }"
            class="link"
          >
            模版详情
          </router-link>
          <span v-else>-</span>
        </f-formitem>
      </div>
      <div class="row justify-center q-mb-md q-mt-md q-gutter-md">
        <span>
          <fdev-btn
            label="编辑"
            @click="UpdateTemplate = true"
            :disable="
              !compareTime(detailData.date) ||
                !(detailData.can_operation || isKaDianManager)
            "
          />
          <fdev-tooltip
            v-if="
              !compareTime(detailData.date) ||
                !(detailData.can_operation || isKaDianManager)
            "
          >
            <span v-if="!compareTime(detailData.date)">
              当前变更已过期
            </span>
            <span
              v-if="
                compareTime(detailData.date) &&
                  !(detailData.can_operation || isKaDianManager)
              "
            >
              请联系当前变更所属组成员的第三层级组及其子组的投产管理员
            </span>
          </fdev-tooltip>
        </span>
        <span>
          <fdev-btn
            label="添加变更应用"
            @click="openNewChanges = true"
            :disable="
              !compareTime(detailData.date) ||
                !(detailData.can_operation || isKaDianManager)
            "
          />
          <fdev-tooltip
            v-if="
              !compareTime(detailData.date) ||
                !(detailData.can_operation || isKaDianManager)
            "
          >
            <span v-if="!compareTime(detailData.date)">
              当前变更已过期
            </span>
            <span
              v-if="
                compareTime(detailData.date) &&
                  !(detailData.can_operation || isKaDianManager)
              "
            >
              请联系当前变更所属组成员的第三层级组及其子组的投产管理员
            </span>
          </fdev-tooltip>
        </span>
        <fdev-btn label="返回" @click="goUpdateList" />
      </div>
    </f-block>
    <f-block>
      <fdev-tabs
        class="q-ml-md"
        active-color="primary"
        indicator-color="primary"
        align="left"
      >
        <fdev-route-tab
          v-for="(route, index) in routerList"
          :key="index"
          :to="route.path"
          exact
          :label="route.label"
        />
      </fdev-tabs>
      <fdev-separator class="q-mb-md" />
      <router-view :application_type="detailData.type" />

      <UpdateTemplateDialog
        @confirm="confirm"
        v-model="UpdateTemplate"
        :detail="detailData"
        :prod_id="id"
        :loading="globalLoading['releaseForm/changesUpdate']"
      />
      <NewChanges
        :image_deliver_type="detailData.image_deliver_type"
        v-model="openNewChanges"
        :application_type="detailData.type"
        @confirm="submitNewChanges"
        :release_node_name="detailData.release_node_name"
        :prod_assets_version="detailData.prod_assets_version"
      />
    </f-block>
  </div>
</template>

<script>
import UpdateTemplateDialog from './components/updateTemplateDialog';
import NewChanges from './components/newChanges';
import { isValidReleaseDate, successNotify } from '@/utils/utils';
import { mapActions, mapState, mapGetters } from 'vuex';

export default {
  name: 'updateDetail',
  data() {
    return {
      id: '',
      UpdateTemplate: false, // 变更模板设置弹窗开关
      openNewChanges: false, // 新建变更模板弹窗开关
      detailData: {},
      routeFromPath: ''
    };
  },
  components: {
    NewChanges,
    UpdateTemplateDialog // 变更模板设置弹窗
  },
  computed: {
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),
    ...mapState('releaseForm', ['changesDetail']),
    routerList() {
      //自动化发布
      if (this.detailData.image_deliver_type === '1') {
        const routerListTemp = [
          {
            label: '应用更新',
            path: `/release/updateDetail/${this.id}/updateList`
          },
          {
            label: '弹性扩展',
            path: `/release/updateDetail/${this.id}/AppScale`
          },
          {
            label: '配置文件更新',
            path: {
              path: `/release/updateDetail/${this.id}/FileUpload`,
              query: { template_id: this.detailData.template_id }
            }
          },
          {
            label: '数据库更新',
            path: `/release/updateDetail/${this.id}/DatabaseUpdate`
          }
        ];
        if (this.detailData.type !== 'gray') {
          return routerListTemp.concat([
            {
              label: '批量任务',
              path: `/release/updateDetail/${this.id}/extPublish`
            }
          ]);
        } else {
          return routerListTemp;
        }
      } else {
        //非自动化发布
        return [
          {
            label: '应用更新',
            path: `/release/updateDetail/${this.id}/updateList`
          },
          {
            label: '投产文件更新',
            path: {
              path: `/release/updateDetail/${this.id}/FileUpload`,
              query: { template_id: this.detailData.template_id }
            }
          }
        ];
      }
    }
  },
  methods: {
    ...mapActions('releaseForm', {
      getChangeApplications: 'getChangeApplications',
      changeName: 'changeName',
      queryChangesDetail: 'queryChangesDetail',
      addChangeApplication: 'addChangeApplication'
    }),
    async getDetail() {
      await this.queryChangesDetail({
        prod_id: this.id,
        source_application: ''
      });
      this.detailData = this.changesDetail;
      this.$q.sessionStorage.set('changeTime', this.detailData.date);
    },
    async submitNewChanges(data) {
      const params = {
        application_id: data.application_id,
        release_type: data.release_type,
        prod_id: this.id,
        deploy_type: data.deploy_type,
        change: data.change,
        caas_stop_env: data.caas_stop_env,
        scc_stop_env: data.scc_stop_env
      };
      await this.addChangeApplication(params);
      successNotify('添加应用成功');

      this.getChangeApplications({ prod_id: this.id });
      this.openNewChanges = false;
    },
    async confirm(data) {
      successNotify('修改成功');
      await this.getDetail();
      if (this.$route.meta.name === '变更文件列表') {
        this.$router.push({
          path: `/release/updateDetail/${this.id}/FileUpload`,
          query: { template_id: this.detailData.template_id }
        });
      } else {
        await this.getChangeApplications({ prod_id: this.id });
      }
      this.UpdateTemplate = false;
    },
    compareTime(date) {
      this.$q.sessionStorage.set('compareTime', date);
      return isValidReleaseDate(date);
    },
    goUpdateList() {
      if (this.routeFromPath === '/') {
        this.$router.push('/release/changesPlans');
      } else {
        this.$router.push(this.routeFromPath);
      }
    }
  },
  beforeRouteEnter(to, from, next) {
    next(vm => {
      vm.routeFromPath = from.path;
    });
  },
  async created() {
    this.id = this.$route.params.id;
    await this.getDetail();
    this.$q.sessionStorage.set(
      'release_node_name',
      this.detailData.release_node_name
    );
    this.$q.sessionStorage.set('type', this.detailData.type);
    this.$q.sessionStorage.set('releaseRole', this.detailData.can_operation);
    this.$store.commit('releaseForm/saveType', this.detailData.type);
  },
  filters: {
    getType(val) {
      return val === 'gray' ? '灰度' : '生产';
    }
  }
};
</script>

<style lang="stylus" scoped></style>
