<template>
  <f-block>
    <fdev-tabs :value="tab" @input="updateTab($event)" align="left">
      <fdev-tab name="newNotices" label="未读通知" />
      <fdev-tab name="oldNotices" label="已读通知" />
    </fdev-tabs>
    <fdev-separator class="q-mb-lg" />
    <Loading :visible="loading">
      <fdev-tab-panels
        :value="tab"
        @input="updateTab($event)"
        animated
        class="panel-border"
      >
        <fdev-tab-panel name="newNotices">
          <fdev-table
            no-export
            title="未读通知列表"
            titleIcon="list_s_f"
            :data="newNoticesList"
            :columns="columns"
            noSelectCols
          >
            <template v-slot:top-bottom>
              <f-formitem label="条件筛选">
                <fdev-select
                  :value="typeMsg"
                  @input="updateTypeMsg($event)"
                  :options="typeArr"
                />
              </f-formitem>
            </template>
            <template v-slot:top-right>
              <fdev-btn
                type="button"
                @click="readAll"
                label="一键已读"
                ficon="eye"
                normal
                :disable="typeArr.length === 0"
              />
            </template>
            <template v-slot:body-cell-content="props">
              <fdev-td>
                <div
                  @click="lookMessage(props.row)"
                  class="ellipsis text-blue-8 cursor-pointer"
                  :title="props.value"
                  v-if="props.value !== '-'"
                >
                  {{ props.value }}
                </div>
                <div v-else>-</div>
              </fdev-td>
            </template>

            <template v-slot:body-cell-create_time="props">
              <fdev-td>
                <div class="ellipsis" :title="props.row.create_time">
                  {{ props.row.create_time }}
                </div>
              </fdev-td>
            </template>
            <template v-slot:body-cell-type="props">
              <fdev-td>
                <div class="ellipsis" :title="props.row.desc">
                  {{ props.row.desc }}
                </div>
              </fdev-td>
            </template>
          </fdev-table>
        </fdev-tab-panel>
        <fdev-tab-panel name="oldNotices">
          <fdev-table
            no-export
            title="已读通知列表"
            titleIcon="list_s_f"
            :data="oldNotices"
            :columns="columns"
            noSelectCols
          >
            <template v-slot:body-cell-content="props">
              <fdev-td>
                <div
                  class="ellipsis text-blue-8 cursor-pointer"
                  :title="props.value"
                  v-if="props.value !== '-'"
                  @click="lookMessage(props.row)"
                >
                  {{ props.value }}
                </div>
                <div v-else>-</div>
              </fdev-td>
            </template>
          </fdev-table>
        </fdev-tab-panel>
      </fdev-tab-panels>
    </Loading>
  </f-block>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import { successNotify } from '@/utils/utils';
import Loading from '@/components/Loading';

export default {
  name: 'Notices',
  components: { Loading },
  data() {
    return {
      cloneVisibleColumns: ['content', 'create_time', 'type'],
      columns: [
        {
          name: 'content',
          label: '消息内容',
          field: 'content',
          align: 'left'
        },
        {
          name: 'create_time',
          label: '创建时间',
          field: 'create_time',
          align: 'left'
        },
        { name: 'type', label: '消息描述', field: 'desc', align: 'left' }
      ],
      // tab: 'newNotices',
      typeArr: [],
      newNoticesList: [],
      loading: false
    };
  },
  computed: {
    ...mapState('global', {
      newNotices: 'newNotices',
      oldNotices: 'oldNotices'
    }),
    ...mapState('user', ['currentUser']),
    ...mapState('noticesForm', ['unreadMsgList']),
    ...mapState('userActionSaveNotice/noticeMessage', [
      'typeMsg',
      'tab',

      'oldVisibleColumns'
    ])
  },
  watch: {
    async typeMsg(val) {
      await this.queryMessage(val);
    },
    async newNotices(val) {
      this.newNoticesList = this.newNotices;
      let typeList = val.map(item => item.desc);
      this.typeArr = this.unique(typeList);
      await this.queryMessage(this.typeMsg);
    }
  },
  methods: {
    ...mapActions('global', ['updateNotifyStatus', 'fetchNotices']),
    ...mapActions('noticesForm', ['queryMessageByType']),
    ...mapMutations('userActionSaveNotice/noticeMessage', {
      updateTypeMsg: 'updateTypeMsg',
      updateTab: 'updateTab',
      updateOldVisibleColumns: 'updateOldVisibleColumns'
    }),
    unique(arr) {
      return Array.from(new Set(arr));
    },
    async queryMessage(val) {
      let params = { target: this.currentUser.user_name_en, desc: val };
      await this.queryMessageByType(params);
      this.newNoticesList = this.unreadMsgList;
    },
    async readAll() {
      let idList = [];
      this.newNoticesList.map(item => idList.push(item.id));
      await this.updateNotifyStatus({ id: idList });
      successNotify('一键处理成功！');
      this.typeMsg = '';
    },
    async lookMessage(notice) {
      if (notice.state === '0') {
        await this.updateNotifyStatus({ id: notice.id.split(',') });
      }
      //为2代表无链接，为3代表需要刷新，为1代表外链接，为0代表内链接
      if (notice.type !== '2' && notice.type !== '3') {
        if (notice.type === '0') {
          window.open(notice.hyperlink, '_self');
        } else {
          window.open(notice.hyperlink);
        }
      }
    },
    async init() {
      await this.fetchNotices({ target: this.currentUser.user_name_en });
      this.newNoticesList = this.newNotices;
      let typeList = this.newNotices.map(item => item.desc);
      this.typeArr = this.unique(typeList);
    }
  },
  async created() {
    this.loading = true;
    await this.init();
    this.loading = false;
  }
};
</script>
