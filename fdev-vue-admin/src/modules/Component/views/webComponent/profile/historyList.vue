<template>
  <Loading :visible="loading">
    <div class="row justify-between">
      <fdev-btn
        ficon="refresh_c_o"
        label="同步npm仓库"
        @click="scan"
        :loading="globalLoading['componentForm/scanMpassComHistory']"
      />

      <fdev-space />

      <fdev-select
        v-model="versionModel"
        :options="webVersionOptions"
        map-options
        emit-value
        @input="init"
        class="select-width"
      />
    </div>

    <fdev-timeline layout="comfortable" v-if="mpassHistory.length > 0">
      <fdev-timeline-entry
        v-for="(item, i) in mpassHistory"
        :key="i"
        side="left"
        :subtitle="item.version"
        :title="item.type"
      >
        <template v-slot:subtitle>
          {{ item.version }}
        </template>

        <template v-slot:title>
          <span :class="`text-${type_color(item.type)}`">
            {{ item.type ? version(item.type) : '-' }}
          </span>
        </template>

        <div>
          <div class="text-grey-7 row">
            <p class="col-6">更新时间：{{ item.date }}</p>
            <div class="col">
              <f-icon
                name="edit"
                @click="handleDialogOpened(item)"
                class="text-blue-8 cursor-pointer"
              />
            </div>
          </div>
          <p class="text-grey-7">
            当前版本：
            <span>{{ item.version }}</span>
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
          <div class="text-grey-7 row">
            <div class="inline-block">更新内容：</div>
            <div
              class="inline-block"
              style="max-width:400px;word-break:break-all"
            >
              <span v-html="logFilter(item.release_log)"></span>
            </div>
          </div>
        </div>
      </fdev-timeline-entry>
    </fdev-timeline>

    <f-dialog
      title="编辑版本信息"
      right
      f-sc
      v-model="historyModelOpen"
      @shake="confirmToClose('historyModelDialogOpen')"
    >
      <div>
        <!-- 当前版本 -->
        <f-formitem diaS label="当前版本">
          <fdev-input
            v-model="mpassHisModel.version"
            autofocus
            disable
            hint=""
          />
        </f-formitem>

        <!-- 更新人员 -->
        <f-formitem diaS label="更新人员">
          <fdev-input
            v-model="mpassHisModel.name_cn"
            autofocus
            disable
            hint=""
          />
        </f-formitem>

        <!-- 当前版本类型 -->
        <f-formitem diaS label="当前版本类型">
          <fdev-select
            emit-value
            map-options
            :options="weTypeOptions"
            :readonly="!isManager"
            option-value="value"
            option-label="label"
            v-model="mpassHisModel.type"
            hint=""
          />
        </f-formitem>

        <!-- 更新内容 -->
        <f-formitem diaS label="更新内容">
          <fdev-input
            v-model="mpassHisModel.release_log"
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
          :loading="globalLoading['componentForm/updateMpassComHistory']"
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
  mpassHisModel,
  webTypeDict,
  webVersionOptions
} from '@/modules/Component/utils/constants.js';
import { successNotify, deepClone } from '@/utils/utils';

export default {
  name: 'WebHistoryList',
  components: { Loading },
  data() {
    return {
      loading: false,
      webVersionOptions,
      versionModel: 'release',
      historyModelOpen: false,
      mpassHisModel: mpassHisModel(),
      webTypeDict
    };
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('componentForm', ['mpassHistory', 'mpassComDetail']),
    ...mapState('user', ['currentUser']),
    weTypeOptions() {
      return Object.keys(this.webTypeDict).map(item => {
        return {
          label: this.webTypeDict[item],
          value: item
        };
      });
    },
    isManager() {
      return this.managers.some(user => {
        return user.id === this.currentUser.id;
      });
    }
  },
  props: {
    managers: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    ...mapActions('componentForm', [
      'queryMpassComHistory',
      'scanMpassComHistory',
      'updateMpassComHistory'
    ]),
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
    version(index) {
      return webTypeDict[index];
    },
    logFilter(val) {
      val = val ? val : '';
      return val
        .replace(/</g, '&lt;')
        .replace(/</g, '&gt;')
        .replace(/\n/g, '<br/>');
    },
    handleDialogOpened(item) {
      this.historyModelOpen = true;
      this.mpassHisModel = deepClone(item);
    },
    async updateHistory() {
      await this.updateMpassComHistory(this.mpassHisModel);
      this.mpassHisModel = mpassHisModel();
      this.init();
      this.historyModelOpen = false;
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
    },
    async scan() {
      const { id, npm_name } = this.mpassComDetail;
      await this.scanMpassComHistory({
        id,
        npm_name
      });
      successNotify('发起同步成功，请耐心等待结果!');
    },
    async init() {
      this.loading = true;
      const component_id = this.$route.params.id;
      await this.queryMpassComHistory({
        component_id,
        version: this.versionModel
      });
      this.loading = false;
    }
  },
  created() {
    this.init();
  }
};
</script>

<style></style>
