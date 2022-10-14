<template>
  <Loading :visible="loading">
    <div class="row justify-between">
      <f-formitem page label="类型" v-if="isHistory">
        <fdev-select
          v-model="versionModel"
          :options="archetypeVersionOptions"
          options-dense
          map-options
          emit-value
          @input="init"
        />
      </f-formitem>
      <fdev-btn
        ficon="refresh_c_o"
        normal
        label="同步maven仓库"
        @click="scan"
        :loading="globalLoading['componentForm/scanArchetypeHistory']"
        v-if="isHistory"
      />
    </div>
    <fdev-timeline
      class="q-mt-lg"
      layout="comfortable"
      v-if="historyList.length > 0"
    >
      <fdev-timeline-entry
        v-for="(item, i) in historyList"
        :key="i"
        side="left"
      >
        <template v-slot:title>
          <div style="margin-bottom:12px">
            {{ isHistory ? item.version : item.tag }}
            <span>
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
              {{ version(item.type) }}
            </span>
          </div>
        </template>

        <div>
          <p class="text-grey-7">更新时间：{{ item.date }}</p>
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
              style="max-width:400px;margin-bottom:16px;word-break:break-all"
            >
              <span v-html="logFilter(item.release_log)"></span>
            </div>
          </div>
          <Authorized class="q-gutter-x-md" v-if="isHistory">
            <fdev-btn
              normal
              label="骨架关联"
              @click="
                $router.push({
                  name: 'ArchetypeIntergration',
                  params: {
                    archetype_id: item.archetype_id,
                    archetype_version: item.version
                  }
                })
              "
            />
            <fdev-btn
              normal
              label="骨架历史信息维护"
              @click="handleArchetypeHistoryDialogOpened(item)"
              v-if="isManger"
            />
          </Authorized>
        </div>
      </fdev-timeline-entry>
    </fdev-timeline>

    <div v-else class="q-py-lg">
      <f-icon name="alert_t_f" />
      暂无{{ versionModel }}历史版本
    </div>
    <f-dialog
      right
      v-model="archetypeHistoryDialogOpened"
      title="骨架历史信息维护"
    >
      <div class="q-gutter-md">
        <f-formitem label="更新人员" diaS>
          <fdev-input
            v-model="archetypeHistoryModel.name_cn"
            type="text"
            disable
          />
        </f-formitem>
        <f-formitem label="骨架版本" diaS>
          <fdev-input v-model="archetypeHistoryModel.version" type="text" />
        </f-formitem>
        <f-formitem label="骨架类型" diaS>
          <fdev-select
            option-label="label"
            option-value="value"
            map-options
            emit-value
            :disable="disable"
            :options="options"
            v-model="archetypeHistoryModel.type"
          />
        </f-formitem>
        <f-formitem label="更新内容" diaS>
          <fdev-input
            v-model="archetypeHistoryModel.release_log"
            type="textarea"
          />
        </f-formitem>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          @click="updateHistory"
          label="确定"
          :loading="globalLoading['componentForm/updateArchetypeHistory']"
        />
      </template>
    </f-dialog>
  </Loading>
</template>
<script>
import Loading from '@/components/Loading';
import { mapActions, mapState } from 'vuex';
import Authorized from '@/components/Authorized';
import {
  typeDict,
  archetypeVersionOptions,
  archetypeHistoryModel
} from '@/modules/Component/utils/constants.js';
import { deepClone, successNotify } from '@/utils/utils';

export default {
  name: 'ArchetypeHistoryList',
  components: { Loading, Authorized },
  data() {
    return {
      loading: false,
      archetype_id: '',
      archetypeHistoryDialogOpened: false,
      archetypeHistoryModel: archetypeHistoryModel(),
      disable: false,
      versionModel: 'RELEASE',
      archetypeVersionOptions: archetypeVersionOptions,
      historyList: [],
      isHistory: null // 前后端骨架历史版本判断
    };
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('componentForm', [
      'archetypeDetail',
      'archetypeHistory',
      'archetypeWebHistory'
    ]),
    ...mapState('user', ['currentUser']),
    options() {
      const optionsKey = this.disable ? ['3'] : ['0', '1', '2'];
      return optionsKey.map(v => {
        return { label: typeDict[v], value: v };
      });
    },
    isManger() {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      const isManager = this.archetypeDetail.manager_id
        ? this.archetypeDetail.manager_id.some(
            user => user.id === this.currentUser.id
          )
        : false;
      return isManager || haveRole;
    }
  },
  methods: {
    ...mapActions('componentForm', [
      'scanArchetypeHistory',
      'queryArchetypeHistory',
      'updateArchetypeHistory',
      'queryMpassArchetypeHistory'
    ]),
    async scan() {
      await this.scanArchetypeHistory({
        id: this.archetype_id,
        groupId: this.archetypeDetail.groupId,
        artifactId: this.archetypeDetail.artifactId
      });
      successNotify('发起同步成功，请耐心等待结果!');
    },
    async init() {
      this.loading = true;
      if (this.$route.name === 'ArchetypeProfile') {
        await this.queryArchetypeHistory({
          archetype_id: this.archetype_id,
          version: this.versionModel
        });
        this.historyList = this.archetypeHistory;
        this.isHistory = true;
      } else {
        await this.queryMpassArchetypeHistory({
          archetype_id: this.archetype_id,
          version: this.versionModel
        });
        this.historyList = this.archetypeWebHistory;
        this.isHistory = false;
      }
      this.loading = false;
    },
    copyMaven(version) {
      const content = `<dependency>\n  <groupId>${
        this.archetypeDetail.groupId
      }</groupId>\n  <artifactId>${
        this.archetypeDetail.artifactId
      }</artifactId>\n  <version>${version}</version>\n</dependency>`;
      const input = document.createElement('textarea');
      input.value = content;
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
      successNotify('复制成功');
    },
    version(index) {
      if (index === '3') {
        return '测试版本';
      }
      return typeDict[index];
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
    handleArchetypeHistoryDialogOpened(item) {
      this.archetypeHistoryDialogOpened = true;
      this.archetypeHistoryModel = deepClone(item);
      this.disable = item.version.includes('SNAPSHOT');
    },
    async updateHistory() {
      await this.updateArchetypeHistory(this.archetypeHistoryModel);
      successNotify('修改成功！');
      this.init();
      this.archetypeHistoryDialogOpened = false;
    },
    confirmToClose(e) {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this.archetypeHistoryDialogOpened = false;
        });
    }
  },

  async created() {
    this.archetype_id = this.$route.params.id;
    this.init();
  }
};
</script>
