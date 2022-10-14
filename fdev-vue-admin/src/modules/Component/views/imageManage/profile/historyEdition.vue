<template>
  <Loading :visible="loading">
    <f-formitem label="类型" page>
      <fdev-select
        v-model="versionModel"
        :options="imageversionOptions"
        map-options
        emit-value
        @input="init"
      />
    </f-formitem>
    <fdev-timeline
      layout="comfortable"
      v-if="imageRecordList.length > 0"
      class="q-mt-lg"
    >
      <fdev-timeline-entry
        v-for="(item, i) in imageRecordList"
        :key="i"
        side="left"
        :subtitle="item.image_tag"
        :title="item.stage"
      >
        <template v-slot:subtitle>
          <div class="row justify-end q-mr-lg text-grey-7">
            {{ item.image_tag }}
          </div>
        </template>

        <template v-slot:title>
          <span :class="`text-${type_color(item.stage)}`">
            {{ version(item.stage) }}
          </span>
        </template>

        <div>
          <div class="text-grey-7 row">
            <p class="col-6">更新时间：{{ item.update_time }}</p>
            <div class="col">
              <fdev-tooltip v-if="!isManger">
                需要镜像管理员或基础架构管理员权限
              </fdev-tooltip>
              <f-icon
                class="text-blue-8 cursor-pointer"
                name="edit"
                :disable="!isManger"
                @click="handleDialogOpened(item)"
              />
            </div>
          </div>
          <p class="text-grey-7">
            镜像版本号：
            <span>{{ item.image_tag }}</span>
          </p>
          <div class="text-grey-7 row">
            <p class="q-mr-sm">试用应用列表:</p>
            <div class="app-wrapper">
              <p v-for="app in item.trial_apps_names" :key="app">
                {{ app }}
              </p>
            </div>
          </div>
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
          <div class="text-grey-7 row">
            <p class="inline-block">更新内容：</p>
            <div
              class="inline-block"
              style="max-width:400px;margin-bottom:16px;word-break:break-all"
            >
              <span v-html="logFilter(item.release_log)"></span>
            </div>
          </div>
          <div class="text-grey-7">
            <p class="inline-block">元数据：</p>
            <div class="inline-block">
              <ul v-if="item.meta_data">
                <li v-for="(value, key) in item.meta_data" :key="key">
                  <span>{{ key }}</span
                  >:
                  <span>{{ value }}</span>
                </li>
              </ul>
            </div>
          </div>
          <Authorized class="text-left q-pb-md">
            <fdev-tooltip v-if="!isManger && item.stage === 'invalid'">
              需要镜像管理员或基础架构管理员权限
            </fdev-tooltip>
            <fdev-btn
              class="q-mt-md"
              normal
              v-if="item.stage === 'invalid'"
              :disable="!isManger && item.stage === 'invalid'"
              label="恢复为正式版本"
              @click="handleDependencyTree(item)"
            />
          </Authorized>
        </div>
      </fdev-timeline-entry>
    </fdev-timeline>

    <div v-else class="q-py-lg">
      <f-icon name="alert_t_f" class="q-mr-sm" />
      暂无{{ versionModel }}历史版本
    </div>

    <f-dialog
      right
      f-sc
      v-model="imageRecordHistoryModelDialogOpen"
      title="修改历史版本"
    >
      <div>
        <!-- 更新时间 -->
        <f-formitem diaS label="更新时间">
          <fdev-input
            v-model="imageRecordHistoryModel.update_time"
            disable
            hint=""
          />
        </f-formitem>

        <!-- 镜像版本号 -->
        <f-formitem diaS label="镜像版本号">
          <fdev-input
            v-model="imageRecordHistoryModel.image_tag"
            autofocus
            disable
            hint=""
          />
        </f-formitem>

        <!-- 试用应用列表 -->
        <f-formitem diaS label="试用应用列表">
          <fdev-input
            v-model="imageRecordHistoryModel.trial_apps"
            disable
            hint=""
          />
        </f-formitem>

        <!-- 更新人员 -->
        <f-formitem diaS label="更新人员">
          <fdev-input
            v-model="imageRecordHistoryModel.name_cn"
            autofocus
            disable
            hint=""
          />
        </f-formitem>

        <!-- 镜像类型 -->
        <f-formitem diaS label="镜像类型">
          <fdev-select
            option-label="label"
            option-value="value"
            map-options
            emit-value
            :options="options"
            v-model="imageRecordHistoryModel.stage"
            hint=""
          />
        </f-formitem>

        <!-- 更新内容 -->
        <f-formitem diaS label="更新内容">
          <fdev-input
            v-model="imageRecordHistoryModel.release_log"
            type="textarea"
            autofocus
            hint=""
          />
        </f-formitem>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          label="确定"
          :loading="globalLoading['componentForm/updateBaseImageRecord']"
          @click="updateHistory"
        />
      </template>
    </f-dialog>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState } from 'vuex';
import {
  imageRecordHistoryModel,
  imageTypeDict,
  jdkVersionOptions,
  imageversionOptions
} from '@/modules/Component/utils/constants.js';
import { deepClone, successNotify } from '@/utils/utils';
import Authorized from '@/components/Authorized';

export default {
  name: 'HistoryList',
  components: { Loading, Authorized },
  data() {
    return {
      id: '',
      imageRecordHistoryModel: imageRecordHistoryModel(),
      loading: false,
      imageRecordHistoryModelDialogOpen: false,
      versionModel: 'release',
      imageversionOptions: imageversionOptions,
      jdkVersionOptions: jdkVersionOptions,
      dependencyTreeDialogOpen: false
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
      'imageRecordList',
      'baseImageDetail',
      'componentDetail',
      'dependencyTree'
    ]),
    ...mapState('user', ['currentUser']),
    // 当前组件管理员或基础架构管理员
    isManger() {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );

      const isManager = this.baseImageDetail.manager
        ? this.baseImageDetail.manager.some(
            user => user.id === this.currentUser.id
          )
        : false;
      return haveRole || isManager;
    },
    options() {
      const keys = Object.keys(imageTypeDict);

      return keys.map(key => {
        return { label: imageTypeDict[key], value: key };
      });
    }
  },
  methods: {
    ...mapActions('componentForm', [
      'queryBaseImageDetail',
      'queryBaseImageRecord',
      'queryDependencyTree',
      'updateBaseImageRecord',
      'recoverInvalidRecord'
    ]),
    async init() {
      this.loading = true;
      await this.queryBaseImageDetail({
        id: this.id
      });
      await this.queryBaseImageRecord({
        name: this.baseImageDetail.name,
        stage: this.versionModel
      });

      this.loading = false;
    },
    version(index) {
      return imageTypeDict[index];
    },
    async updateHistory() {
      await this.updateBaseImageRecord(this.imageRecordHistoryModel);
      successNotify('修改历史版本成功！');
      this.init();
      this.imageRecordHistoryModelDialogOpen = false;
    },
    handleDialogOpened(item) {
      this.imageRecordHistoryModelDialogOpen = true;
      this.imageRecordHistoryModel = deepClone(item);
    },
    type_color(type) {
      if (!type) {
        return 'primary';
      }
      if (type === 'release') {
        return 'secondary';
      } else if (type === 'trial') {
        return 'orange';
      } else if (type === 'invalid') {
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
    async handleDependencyTree(item) {
      await this.recoverInvalidRecord({
        id: item.id
      });
      successNotify('恢复成正式版本成功！');
      this.init();
    },
    confirmToClose() {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this.imageRecordHistoryModelDialogOpen = false;
        });
    }
  },
  async created() {
    this.id = this.$route.params.id;
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.inline-block
  vertical-align top
div
  &.inline-block
    width calc(100% - 70px)
    word-break break-all
li,ul
  list-style none
  padding 0
</style>
