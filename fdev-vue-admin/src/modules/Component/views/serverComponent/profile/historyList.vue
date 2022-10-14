<template>
  <Loading :visible="loading">
    <div class="scan-wrapper">
      <fdev-btn
        ficon="refresh_c_o"
        label="同步maven仓库"
        @click="scan"
        :loading="globalLoading['componentForm/scanHistory']"
      />
      <fdev-space />
      <fdev-select
        v-model="versionModel"
        :options="versionOptions"
        map-options
        emit-value
        @input="init"
        page
      />
    </div>

    <fdev-timeline layout="comfortable" v-if="history.length > 0">
      <fdev-timeline-entry
        v-for="(item, i) in history"
        :key="i"
        side="left"
        :subtitle="item.version"
        :title="item.type"
      >
        <template v-slot:subtitle> </template>

        <template v-slot:title>
          <div style="margin-bottom:12px">
            {{ item.version }}
            <span v-if="componentDetail.type !== '1'">
              <f-icon
                name="copy"
                class="cursor-pointer text-blue-8"
                @click="copyMaven(item.version)"
              />
              <fdev-tooltip>复制Maven依赖</fdev-tooltip>
            </span>
          </div>
          <div>
            <span :class="`text-${type_color(item.type)}`">
              {{ item.type ? version(item.type) : '-' }}
            </span>
          </div>
        </template>
        <div>
          <div class="text-grey-7 row">
            <p class="col-6">更新时间：{{ item.date }}</p>
            <div class="col">
              <f-icon
                name="edit"
                @click="handleDialogOpened(item)"
                v-if="isManger"
                class="text-blue-8 cursor-pointer"
              />
            </div>
          </div>
          <p class="text-grey-7">
            组件版本：
            <span>{{ item.version }}</span>
          </p>
          <p class="text-grey-7">
            <span class="jdk q-mr-sm">编译jdk版本：</span
            >{{ item.jdk_version ? item.jdk_version : '未知' }}
          </p>
          <p class="text-grey-7">
            更新人员：
            <router-link
              :to="`/user/list/${item.update_user}`"
              class="link"
              v-if="item.update_user"
            >
              {{ item.name_cn }}
            </router-link>
            <span v-else>{{ item.name_cn }}</span>
          </p>
          <div class="text-grey-7">
            <p class="inline-block">更新内容：</p>
            <div
              class="inline-block"
              style="max-width:400px;margin-bottom:16px;word-break:break-all"
            >
              <span v-html="logFilter(item.release_log)"></span>
            </div>
          </div>
          <fdev-btn
            label="Maven依赖树"
            dialog
            normal
            @click="handleDependencyTree(item)"
          />
          <fdev-btn
            style="margin-left:16px"
            label="复制Maven依赖"
            dialog
            normal
            v-if="componentDetail.type !== '1'"
            @click="handleMavenCopy(item)"
          />
        </div>
      </fdev-timeline-entry>
    </fdev-timeline>

    <div v-else class="q-py-lg">
      <fdev-icon name="warning" size="200%" class="warning q-mr-sm" />
      暂无{{ versionModel }}历史版本
    </div>

    <f-dialog
      title="编辑版本信息"
      right
      f-sc
      v-model="historyModelDialogOpen"
      @shake="confirmToClose('historyModelDialogOpen')"
    >
      <div>
        <!-- 编译jdk版本 -->
        <f-formitem diaS label="jdk版本">
          <fdev-select
            emit-value
            map-options
            :options="jdkVersionOptions"
            option-value="value"
            option-label="label"
            v-model="historyModel.jdk_version"
            hint=""
          />
        </f-formitem>

        <!-- 组件版本 -->
        <f-formitem diaS label="组件版本">
          <fdev-input v-model="historyModel.version" disable hint="" />
        </f-formitem>

        <!-- 更新人员 -->
        <f-formitem diaS label="更新人员">
          <fdev-input v-model="historyModel.name_cn" disable hint="" />
        </f-formitem>

        <!-- 组件类型 -->
        <f-formitem diaS label="版本类型">
          <fdev-select
            option-label="label"
            option-value="value"
            map-options
            emit-value
            :disable="disable"
            :options="options"
            v-model="historyModel.type"
            hint=""
          />
        </f-formitem>

        <!-- 更新内容 -->
        <f-formitem diaS label="更新内容">
          <fdev-input
            v-model="historyModel.release_log"
            type="textarea"
            hint=""
          />
        </f-formitem>
      </div>

      <template v-slot:btnSlot>
        <fdev-btn
          label="确定"
          dialog
          @click="updateHistory"
          :loading="globalLoading['componentForm/updateComponentHistary']"
        />
      </template>
    </f-dialog>

    <f-dialog right v-model="dependencyTreeDialogOpen" title="Maven依赖树">
      <div class="scroll-thin-x">
        <div class="q-px-lg" style="width:1000px">
          <pre>{{ this.dependencyTree }}</pre>
        </div>
      </div>
    </f-dialog>

    <f-dialog right v-model="mavenCopyDialogOpen" title="Maven复制">
      <div class="q-px-lg">
        <pre><code>&lt;dependency&quot;&gt;
    &lt;groupId&gt;{{ componentDetail.groupId }}&lt;/groupId&gt;
    &lt;artifactId&gt;{{ componentDetail.artifactId }}&lt;/artifactId&gt;
    &lt;version&gt;{{ currentVersion }}&lt;/version&gt;
&lt;/dependency&gt;</code></pre>
      </div>
    </f-dialog>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState } from 'vuex';
import {
  historyModel,
  versionOptions
} from '@/modules/Component/utils/constants.js';
import { deepClone, successNotify, copyValue } from '@/utils/utils';
import {
  typeDict,
  jdkVersionOptions
} from '@/modules/Component/utils/constants.js';

export default {
  name: 'HistoryList',
  components: { Loading },
  data() {
    return {
      historyModel: historyModel(),
      loading: false,
      component_id: '',
      historyModelDialogOpen: false,
      versionModel: 'release',
      versionOptions: versionOptions,
      disable: false,
      jdkVersionOptions: jdkVersionOptions,
      dependencyTreeDialogOpen: false,
      mavenCopyDialogOpen: false,
      historyData: {}, // 当前历史版本的历史值
      currentVersion: '' // 当前版本
    };
  },
  props: {
    source: {
      type: String,
      default: '4'
    }
  },
  watch: {
    '$route.params': {
      deep: true,
      handler(val) {
        this.component_id = val.id;
        this.init();
      }
    },
    source(val) {
      if (val === '1') {
        this.versionModel = '';
      }
      this.init();
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('componentForm', [
      'history',
      'componentDetail',
      'dependencyTree'
    ]),
    ...mapState('user', ['currentUser']),
    // 当前组件管理员或基础架构管理员
    isManger() {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      const isManager = this.componentDetail.manager_id
        ? this.componentDetail.manager_id.some(
            user => user.id === this.currentUser.id
          )
        : false;
      return isManager || haveRole;
    },
    options() {
      const optionsKey = this.disable ? ['3'] : ['0', '1', '2'];
      return optionsKey.map(v => {
        return { label: typeDict[v], value: v };
      });
    }
  },
  methods: {
    ...mapActions('componentForm', [
      'queryComponentHistory',
      'updateComponentHistary',
      'scanHistory',
      'queryDependencyTree'
    ]),
    async init() {
      this.loading = true;
      await this.queryComponentHistory({
        component_id: this.component_id,
        version: this.versionModel
      });

      this.loading = false;
    },
    version(index) {
      if (index === '3') {
        return '测试版本';
      }
      return typeDict[index];
    },
    async updateHistory() {
      await this.updateComponentHistary(this.historyModel);
      if (this.historyData.type !== this.historyModel.type) {
        this.$emit('input');
      }
      successNotify('修改成功！');
      this.init();
      this.historyModelDialogOpen = false;
    },
    handleDialogOpened(item) {
      this.historyModelDialogOpen = true;
      this.historyData = item;
      this.historyModel = deepClone(item);
      this.disable =
        item.version.includes('SNAPSHOT') || item.version.includes('RC');
    },
    type_color(type) {
      if (!type) {
        return 'primary';
      }
      if (type === '0') {
        return 'secondary';
      } else if (type === '1') {
        return 'orange';
      } else if (type === '2') {
        return 'red';
      }
    },
    logFilter(val) {
      val = val ? val : '';
      return val
        .replace(/</g, '&lt;')
        .replace(/</g, '&gt;')
        .replace(/\n/g, '<br/>');
    },
    copyMaven(version) {
      const content = `<dependency>\n  <groupId>${
        this.componentDetail.groupId
      }</groupId>\n  <artifactId>${
        this.componentDetail.artifactId
      }</artifactId>\n  <version>${version}</version>\n</dependency>`;
      copyValue(content);
      successNotify('复制成功');
    },
    async scan() {
      await this.scanHistory({
        id: this.component_id,
        groupId: this.componentDetail.groupId,
        artifactId: this.componentDetail.artifactId
      });
      successNotify('发起同步成功，请耐心等待结果!');
    },
    async handleDependencyTree(item) {
      await this.queryDependencyTree({
        version: item.version,
        groupId: this.componentDetail.groupId,
        artifactId: this.componentDetail.artifactId
      });
      this.dependencyTreeDialogOpen = true;
    },
    async handleMavenCopy(item) {
      this.currentVersion = item.version;
      this.mavenCopyDialogOpen = true;
    },
    confirmToClose(key) {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this[key] = false;
        });
    }
  },
  async created() {
    this.component_id = this.$route.params.id;
    if (this.source === '1') {
      this.versionModel = '';
    }
    if (this.source !== '4') {
      this.init();
    }
  }
};
</script>

<style lang="stylus" scoped>

.dialog-height
  height 600px
.inline-block
  vertical-align top
div
  &.inline-block
    width: calc(100% - 70px);
    word-break: break-all;
// .select-width
//   width 180px
.scan-wrapper
  display flex
  justify-content space-around
.q-timeline >>> .q-timeline__entry
  .q-timeline__subtitle
    width: 29%;
    text-transform none
// pre {
//   background-color: #f8f8f8;
//   border: 1px solid #cccccc;
//   font-size: 13px;
//   line-height: 19px;
//   overflow: auto;
//   padding: 6px 10px;
//   border-radius: 3px;
// }
</style>
