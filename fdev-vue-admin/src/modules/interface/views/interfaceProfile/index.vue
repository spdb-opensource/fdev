<template>
  <f-block>
    <Page>
      <Loading :visible="loading">
        <div>
          <fdev-card-section>
            <div class="row q-col-gutter-x-md q-col-gutter-y-sm q-ml-md">
              <f-formitem class="col-6" label="交易码:">
                {{ version.transId }}
              </f-formitem>
              <f-formitem class="col-6" label="接口名称:">
                <span v-if="isForeign">
                  {{ version.interfaceName }}
                </span>
                <span v-else>
                  {{ version.interfaceName }}
                </span>
              </f-formitem>
              <f-formitem
                class="col-6"
                label="接口别名："
                v-if="interfaceType !== 'REST'"
              >
                {{ version.interfaceAlias }}
              </f-formitem>
              <f-formitem class="col-6" label="提供方:">
                {{ version.serviceId }}
              </f-formitem>
              <f-formitem
                class="col-6"
                label="服务Id:"
                v-if="interfaceType !== 'REST'"
              >
                {{ version.esbServiceId }}
              </f-formitem>
              <f-formitem
                class="col-6"
                label="服务名称:"
                v-if="interfaceType !== 'REST'"
              >
                {{ version.esbServiceName }}
              </f-formitem>
              <f-formitem
                class="col-6"
                label="操作Id:"
                v-if="interfaceType !== 'REST'"
              >
                {{ version.esbOperationId }}
              </f-formitem>
              <f-formitem
                class="col-6"
                label="操作名称:"
                v-if="interfaceType !== 'REST'"
              >
                {{ version.esbOperationName }}
              </f-formitem>
              <f-formitem
                class="col-6"
                label="调用报文类型:"
                v-if="interfaceType !== 'REST'"
              >
                {{ version.interfaceType }}
              </f-formitem>
              <f-formitem class="col-6" label="URL:">
                <p class="wrap">{{ version.uri }}</p>
              </f-formitem>
              <f-formitem class="col-6" label="分支名:">
                {{ version.branch }}
              </f-formitem>
              <f-formitem class="col-6" label="版本号:">
                {{ version.ver }}
              </f-formitem>
              <f-formitem
                class="col-6"
                label="状态:"
                v-if="interfaceType !== 'REST'"
              >
                {{ version.esbState }}
              </f-formitem>
              <f-formitem class="col-6" label="请求类型:">
                {{ version.requestType }}
              </f-formitem>
              <f-formitem class="col-6" label="请求协议:">
                {{ version.requestProtocol }}
              </f-formitem>
              <f-formitem
                class="col-6"
                label="是否注册:"
                v-if="version.register"
              >
                {{ version.register | dataFilter }}
              </f-formitem>
              <f-formitem class="col-6" label="接口描述:">
                <span class="wrap">{{ version.description }}</span>
              </f-formitem>
            </div>
          </fdev-card-section>
        </div>
        <div class>
          <fdev-separator inset />
          <fdev-card-section>
            <div class="row q-ml-xs">
              <f-formitem
                page
                label="选择版本"
                v-if="!isForeign && interfaceVersions.length > 0"
              >
                <fdev-select
                  input-debounce="0"
                  ref="version"
                  v-model="version"
                  :options="interfaceVersions"
                  map-options
                  emit-value
                  :option-value="opt => opt"
                  :option-label="opt => opt.branch + '分支-' + opt.ver + '版本'"
                />
              </f-formitem>
            </div>
          </fdev-card-section>
        </div>

        <div class>
          <fdev-card-section>
            <div>
              <fdev-chip color="primary" text-color="white" icon="directions">
                请求参数
              </fdev-chip>
            </div>
            <NastedTable
              :tableData="version.request"
              :columns="columns"
              @handleUpdate="
                data => {
                  handleUpdate(data, 'request');
                }
              "
              :canEdit="!isForeign && currentUser.status === '0'"
            />
          </fdev-card-section>
        </div>
        <div class>
          <fdev-card-section>
            <div>
              <fdev-chip color="primary" text-color="white" icon="directions">
                响应参数
              </fdev-chip>
            </div>
            <NastedTable
              :tableData="version.response"
              :columns="columns"
              @handleUpdate="
                data => {
                  handleUpdate(data, 'response');
                }
              "
              :canEdit="!isForeign && currentUser.status === '0'"
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
                @click="handleRequestHeaderPanelOpen"
              >
                请求头信息
              </fdev-chip>
            </div>
            <NastedTable
              :tableData="version.reqHeader"
              :columns="headerColumns"
              v-show="requestHeaderPanelOpened"
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
                @click="handleResponseHeaderPanelOpen"
              >
                响应头信息
              </fdev-chip>
            </div>
            <NastedTable
              :tableData="version.rspHeader"
              :columns="headerColumns"
              v-show="responseHeaderPanelOpened"
            />
          </fdev-card-section>
        </div>
        <div class="text-center">
          <fdev-btn
            type="button"
            label="返回"
            @click="goBack"
            color="primary"
            text-color="white"
            class="q-mb-lg"
          />
        </div>
      </Loading>
    </Page>
  </f-block>
</template>

<script>
import Page from '@/components/Page';
import Loading from '@/components/Loading';
import { dataFilter, interfaceProfileColumns } from '../../utils/constants';
import { deepClone, successNotify } from '@/utils/utils';
import { mapActions, mapState } from 'vuex';
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
      columns: interfaceProfileColumns().columns,
      headerColumns: interfaceProfileColumns().headerColumns,
      expandColumns: [],
      requestHeaderPanelOpened: false,
      responseHeaderPanelOpened: false,
      version: {},
      loading: false,
      interfaceType: ''
    };
  },
  computed: {
    ...mapState('interfaceForm', [
      'interfaceModel',
      'interfaceDetail',
      'interfaceVersions',
      'tagRole'
    ]),
    ...mapState('user', ['currentUser'])
  },
  filters: {
    dataFilter(val) {
      return dataFilter[val];
    }
  },
  methods: {
    ...mapActions('interfaceForm', [
      'queryInterfaceDetailById',
      'getInterfaceDetailById',
      'queryInterfaceVersions',
      'updateParamDescription',
      'queryTagsRole'
    ]),
    handleRequestHeaderPanelOpen() {
      this.requestHeaderPanelOpened = !this.requestHeaderPanelOpened;
    },
    handleResponseHeaderPanelOpen() {
      this.responseHeaderPanelOpened = !this.responseHeaderPanelOpened;
    },
    dataFilter(val) {
      return dataFilter[val];
    },
    async handel(data, key) {
      let params = {
        transId: this.version.relTransId,
        interfaceType: this.version.interfaceType,
        serviceId: this.version.serviceId
      };
      params[key] = data;
      try {
        await this.updateParamDescription(params);
        successNotify('修改成功！');
      } finally {
        await this.init();
      }
    },
    handleEmptyArr(data) {
      data.forEach(item => {
        if (Array.isArray(item.paramList)) {
          this.handleEmptyArr(item.paramList);
        } else {
          item.paramList = [];
        }
      });
      return data;
    },
    handleUpdate(data, key) {
      let handleData = deepClone(data);
      handleData = this.handleEmptyArr(handleData);
      this.handel(handleData, key);
    },
    async init() {
      this.loading = true;
      let params = {
        id: this.$route.params.id,
        interfaceType: this.interfaceType
      };
      if (this.isForeign) {
        /* 外部链接，不需要token */
        await this.getInterfaceDetailById(params);
        this.version = this.interfaceDetail;
        this.loading = false;
      } else {
        /* 需要token */
        await this.queryInterfaceDetailById(params);
        await this.queryInterfaceVersions(params);
        const version = this.interfaceVersions.find(
          ele => ele.ver == this.interfaceModel.ver
        );
        this.version = version || this.interfaceModel;
        this.loading = false;
      }
    }
  },
  async created() {
    this.interfaceType = this.$route.query.interfaceType;
    this.expandColumns = deepClone(this.columns);
    this.expandColumns.splice(this.columns.length - 1, 1);
    this.isForeign = this.$route.name === 'interfaceProfile';
    await this.init();
    if (this.interfaceType === 'REST') {
      this.columns.splice(3, 1);
    }
  }
};
</script>

<style lang="stylus" scoped>

.wrap
  margin 0
  word-wrap break-word
</style>
