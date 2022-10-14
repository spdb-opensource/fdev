<template>
  <div>
    <f-block class="q-mb-md">
      <div class="row">
        <f-formitem
          class="col-4"
          label-style="width:120px;"
          bottom-page
          label="投产窗口："
        >
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
        <f-formitem
          class="col-4"
          label-style="width:120px;"
          bottom-page
          label="发布说明日期："
          >{{ detailData.date }}</f-formitem
        >
        <f-formitem
          class="col-4"
          label-style="width:120px;"
          bottom-page
          label="发布说明类型："
          >{{ detailData.type | getType }}</f-formitem
        >
        <f-formitem
          class="col-4"
          label-style="width:120px;"
          bottom-page
          label="创建人："
        >
          <router-link
            v-if="detailData.launcher_name_cn"
            :to="{ path: `/user/list/${detailData.launcher}` }"
            class="link"
          >
            {{ detailData.launcher_name_cn }}
          </router-link>
        </f-formitem>
        <f-formitem
          class="col-4"
          label-style="width:120px;"
          bottom-page
          label="创建时间："
          >{{ detailData.create_time }}</f-formitem
        >
        <f-formitem
          bottom-page
          label-style="width:120px;"
          class="col-4"
          label="应用系统："
          >{{ detailData.owner_system_name || '-' }}</f-formitem
        >
        <f-formitem
          bottom-page
          label-style="width:120px;"
          class="col-4"
          label="租户:"
          >{{ detailData.leaseholder || '-' }}</f-formitem
        >
        <f-formitem
          bottom-page
          label-style="width:120px;"
          class="col-4"
          label="网段:"
          >{{ detailData.namespace | namespaceFilter }}</f-formitem
        >
        <f-formitem
          bottom-page
          label-style="width:120px;"
          class="col-4"
          label="发布方式:"
          >{{
            detailData.image_deliver_type | imageDeliverTypeFilter
          }}</f-formitem
        >
        <f-formitem
          class="col-8"
          bottom-page
          label-style="width:120px;"
          label="发布说明名称："
        >
          <div
            class="ellipsis-3-lines full-width"
            :title="detailData.release_note_name | filterReleaseNoteName"
          >
            {{ detailData.release_note_name | filterReleaseNoteName }}
          </div>
        </f-formitem>
      </div>
      <div class="row justify-center q-gutter-md q-mt-md">
        <fdev-btn
          type="button"
          label="返回"
          @click="goReleaseList"
          class="text-primary"
        />
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
      <router-view v-if="showDetail" />
    </f-block>
  </div>
</template>

<script>
import { isValidReleaseDate } from '@/utils/utils';
import { mapActions, mapState, mapGetters } from 'vuex';

export default {
  name: 'updateDetail',
  data() {
    return {
      id: '',
      showDetail: false,
      UpdateTemplate: false, // 变更模板设置弹窗开关
      openNewChanges: false, // 新建变更模板弹窗开关
      detailData: {},
      routeFromPath: ''
    };
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
    ...mapState('releaseForm', ['releaseNoteDetail']),
    routerList() {
      const routerListTemp = [
        {
          label: '应用',
          path: `/release/autoReleaseNoteDetail/${this.id}/applist`
        },
        {
          label: '配置文件',
          path: `/release/autoReleaseNoteDetail/${this.id}/configFile`
        },
        {
          label: '数据库',
          path: `/release/autoReleaseNoteDetail/${this.id}/database`
        }
      ];
      if (this.detailData.type === 'gray') {
        return routerListTemp;
      } else {
        return routerListTemp.concat([
          {
            label: '批量任务',
            path: `/release/autoReleaseNoteDetail/${this.id}/extPublish`
          }
        ]);
      }
    }
  },
  filters: {
    imageDeliverTypeFilter(val) {
      return val === '1' ? '自动' : '手动';
    },
    namespaceFilter(val) {
      return val === '2' ? '网银' : '业务';
    },
    filterReleaseNoteName(val) {
      if (val) {
        const indexName = val.lastIndexOf('.txt');
        return val.substring(0, indexName);
      } else {
        return '-';
      }
    },
    getType(val) {
      return val === 'gray' ? '灰度' : '生产';
    }
  },
  methods: {
    ...mapActions('releaseForm', {
      queryNoteDetail: 'queryNoteDetail'
    }),
    async getDetail() {
      await this.queryNoteDetail({
        note_id: this.id
      });
      this.detailData = this.releaseNoteDetail;
      this.$q.sessionStorage.set('leaseholder', this.detailData.leaseholder);
      this.$q.sessionStorage.set('type', this.detailData.type);
      this.$q.sessionStorage.set('owner_system', this.detailData.owner_system);
      this.$q.sessionStorage.set(
        'owner_system_name',
        this.detailData.owner_system_name
      );
      this.$q.sessionStorage.set(
        'release_node_name',
        this.detailData.release_node_name
      );
      this.showDetail = true;
    },
    goReleaseList() {
      this.$router.push(
        '/release/list/' +
          this.detailData.release_node_name +
          '/autoReleaseNote'
      );
    },
    compareTime(date) {
      this.$q.sessionStorage.set('compareTime', date);
      return isValidReleaseDate(date);
    }
  },
  beforeRouteEnter(to, from, next) {
    next(vm => {
      vm.routeFromPath = from.path;
    });
  },
  created() {
    this.id = this.$route.params.id;
    this.getDetail();
  }
};
</script>

<style lang="stylus" scoped>
.font-wrap
  word-wrap: break-word;
</style>
