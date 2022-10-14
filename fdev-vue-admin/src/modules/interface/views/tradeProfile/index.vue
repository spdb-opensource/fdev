<template>
  <f-block class="wrapper">
    <Page>
      <Loading :visible="loading">
        <div>
          <fdev-card-section>
            <div class="row q-col-gutter-x-md q-col-gutter-y-sm q-ml-md">
              <f-formitem label="交易ID:" class="col-6">
                {{ version.transId }}
              </f-formitem>
              <f-formitem label="交易名称:" class="col-6">
                {{ version.transName }}
              </f-formitem>
              <f-formitem label="服务ID:" class="col-6">
                {{ version.serviceId }}
              </f-formitem>
              <f-formitem label="分支:" class="col-6">
                {{ version.branch }}
              </f-formitem>
              <f-formitem label="渠道:" class="col-6">
                {{ version.channel }}
              </f-formitem>
              <f-formitem label="标签:" class="col-6">
                <fdev-chip
                  v-for="(tag, index) in version.tags"
                  :key="index"
                  :removable="tagRole"
                  @remove="removeTag(tag)"
                  color="orange"
                  text-color="white"
                  :label="tag"
                  dense
                  square
                />
                <fdev-badge
                  color="blue"
                  v-if="tagRole"
                  style="vertical-align:middle"
                >
                  <f-icon name="add" style="color:white" />
                  <fdev-popup-proxy @hide="addTag">
                    <fdev-input v-model="tag" autofocus class="q-ma-sm" />
                  </fdev-popup-proxy>
                </fdev-badge>
              </f-formitem>
            </div>
          </fdev-card-section>
        </div>

        <fdev-separator inset />

        <div class>
          <fdev-card-section>
            <div>
              <fdev-chip
                color="primary"
                text-color="white"
                icon="directions"
                @click="requestOpen = !requestOpen"
                clickable
              >
                请求参数
              </fdev-chip>
            </div>
            <NastedTable
              :tableData="version.request"
              :columns="columns"
              v-show="requestOpen"
              :isTrans="isTrans"
              @handleUpdate="
                data => {
                  handleUpdate(data, 'request');
                }
              "
              :canEdit="currentUser.status === '0'"
            />
          </fdev-card-section>
        </div>
        <div class>
          <fdev-card-section>
            <div>
              <fdev-chip
                color="primary"
                text-color="white"
                icon="directions"
                clickable
                @click="responseOpen = !responseOpen"
              >
                响应参数
              </fdev-chip>
            </div>
            <NastedTable
              :tableData="version.response"
              :columns="columns"
              v-show="responseOpen"
              :isTrans="isTrans"
              @handleUpdate="
                data => {
                  handleUpdate(data, 'response');
                }
              "
              :canEdit="currentUser.status === '0'"
            />
          </fdev-card-section>
        </div>
        <div class>
          <fdev-card-section>
            <div>
              <fdev-chip
                color="primary"
                text-color="white"
                icon="directions"
                clickable
                @click="reqHeader = !reqHeader"
              >
                请求头信息
              </fdev-chip>
            </div>
            <NastedTable
              :tableData="version.reqHeader"
              :columns="headerColumns"
              v-show="reqHeader"
              :isTrans="isTrans"
            />
          </fdev-card-section>
        </div>
        <div class>
          <fdev-card-section>
            <div>
              <fdev-chip
                color="primary"
                text-color="white"
                icon="directions"
                clickable
                @click="repHeaderOpen = !repHeaderOpen"
              >
                响应头信息
              </fdev-chip>
            </div>
            <NastedTable
              :tableData="version.rspHeader"
              :columns="headerColumns"
              v-show="repHeaderOpen"
              :isTrans="isTrans"
            />
          </fdev-card-section>
        </div>
      </Loading>
    </Page>
  </f-block>
</template>

<script>
import Page from '@/components/Page';
import Loading from '@/components/Loading';
import { deepClone, successNotify } from '@/utils/utils';
import { mapActions, mapState } from 'vuex';
import { tradeProfileColumns } from '../../utils/constants';
import NastedTable from '../../components/NastedTable';

export default {
  name: 'interfaceProfile',
  components: {
    Page,
    Loading,
    NastedTable
  },
  data() {
    return {
      masterList: [],
      columns: tradeProfileColumns().columns,
      headerColumns: tradeProfileColumns().headerColumns,
      loading: false,
      tag: '',
      id: '',
      version: {},
      repHeaderOpen: false,
      requestOpen: true,
      responseOpen: true,
      reqHeader: false,
      isTrans: true
    };
  },
  computed: {
    // 应用负责人权限
    ...mapState('interfaceForm', ['transDetail', 'tagRole']),
    ...mapState('user', ['currentUser'])
  },
  methods: {
    ...mapActions('interfaceForm', [
      'transTags',
      'queryTransDetail',
      'queryTagsRole',
      'modifyParamDescription'
    ]),
    /* 增加、删除标签 */
    removeTag(tag) {
      this.$q
        .dialog({
          title: '删除标签',
          message: '确认删除此标签吗？',
          cancel: true
        })
        .onOk(async () => {
          const id = this.$route.params.id;
          let tags = this.version.tags.filter(item => {
            return item !== tag;
          });
          await this.transTags({ tags, id });
          this.init();
        });
    },
    async addTag() {
      const id = this.$route.params.id;
      if (!this.tag) return;
      this.version.tags = this.version.tags === '' ? [] : this.version.tags;
      let tags = deepClone(this.version.tags).concat(this.tag);
      await this.transTags({ tags, id });
      this.init();
      this.tag = '';
    },
    async init() {
      this.loading = true;
      await this.queryTransDetail({ id: this.id });
      this.version = deepClone(this.transDetail);
      this.loading = false;
    },
    async handleUpdate(data, key) {
      let params = {
        transId: this.version.transId,
        serviceId: this.version.serviceId,
        channel: this.version.channel
      };
      params[key] = data;
      try {
        await this.modifyParamDescription(params);
        successNotify('修改成功！');
      } finally {
        await this.init();
      }
    }
  },
  async created() {
    this.id = this.$route.params.id;
    await this.init();
    this.queryTagsRole({ serviceId: this.transDetail.serviceId });
  }
};
</script>
