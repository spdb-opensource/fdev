<template>
  <PageHeaderWrapper>
    <Loading :visible="loading">
      <fdev-table
        title="接口统计"
        class="my-sticky-column-table"
        :data="interfaceStatistics"
        :columns="columns"
        :pagination.sync="pagination"
        flat
        @request="onTabelRequest"
      >
        <template v-slot:top-left>
          <div class="q-mr-sm col-2">
            <fdev-input
              dense
              label="接口名称"
              debounce="800"
              v-model="interfaceName"
              class="top-left interfaceName"
              outlined
            />
            <fdev-select
              dense
              use-input
              outlined
              input-debounce="0"
              label="调用方应用名"
              v-model="targetServiceName"
              :options="serviceName"
              class="top-left target"
              clearable
            />
            <fdev-select
              dense
              use-input
              outlined
              input-debounce="0"
              label="提供方应用名"
              v-model="sourceServiceName"
              :options="serviceName"
              class="top-left"
              clearable
            />
          </div>
        </template>
        <template v-slot:top-right>
          <fdev-btn color="primary" @click="queryList">
            查询
          </fdev-btn>
        </template>
      </fdev-table>
    </Loading>
  </PageHeaderWrapper>
</template>

<script>
import PageHeaderWrapper from '@/components/PageHeaderWrapper';
import Loading from '@/components/Loading';
import { mapState, mapActions } from 'vuex';
import {
  getStatisticsPagination,
  setStatisticsPagination
} from '../../utils/setting.js';
import { errorNotify } from '@/utils/utils';

export default {
  components: {
    PageHeaderWrapper,
    Loading
  },
  data() {
    return {
      loading: false,
      appData: [],
      pagination: {
        page: 1,
        rowsPerPage: getStatisticsPagination().rowsPerPage || 5,
        rowsNumber: 10
      },
      interfaceName: '',
      targetServiceName: '',
      sourceServiceName: '',
      columns: [
        {
          name: 'targetServiceName',
          label: '调用方应用名',
          field: 'targetServiceName',
          align: 'left'
        },
        {
          name: 'sourceServiceName',
          label: '提供方应用名',
          field: 'sourceServiceName',
          align: 'left'
        },
        {
          name: 'name',
          label: '接口名称',
          field: 'name',
          align: 'left'
        },
        {
          name: 'url',
          label: '接口URL',
          field: 'url',
          align: 'left'
        }
      ]
    };
  },
  computed: {
    ...mapState('interfaceForm', [
      'interfaceStatistics',
      'statisticsRowsNumber',
      'serviceName'
    ]),
    ...mapState('user', ['currentUser']),
    selectField() {
      return {
        name: this.interfaceName,
        targetServiceName: this.targetServiceName,
        sourceServiceName: this.sourceServiceName
      };
    }
  },
  watch: {
    'pagination.rowsPerPage'(rowsPerPage) {
      setStatisticsPagination({ rowsPerPage });
    }
  },
  methods: {
    ...mapActions('interfaceForm', [
      'queryInterfaceStatistics',
      'queryServiceName'
    ]),
    async queryList() {
      if (
        !this.interfaceName &&
        !this.targetServiceName &&
        !this.sourceServiceName
      )
        return;
      const { page, rowsPerPage: pageNum } = this.pagination;
      const params = {
        ...this.selectField,
        page,
        pageNum
      };
      await this.queryInterfaceStatistics(params);
      this.pagination.rowsNumber = this.statisticsRowsNumber;
    },
    async onTabelRequest(props) {
      const {
        pagination: { page, rowsPerPage }
      } = props;
      const params = {
        ...this.selectField,
        page,
        pageNum: rowsPerPage
      };
      await this.queryInterfaceStatistics(params);
      //动态修改表格页码
      this.pagination = {
        ...this.pagination,
        page,
        rowsPerPage
      };
    }
  },
  async created() {
    if (this.currentUser.name !== 'admin') {
      errorNotify('当前用户无权限查看接口统计页面');
      this.$router.push('/interfaceAndRoute/interface');
    }
    const params = {
      size: 0,
      index: 1,
      keyword: 'fdev|testmanage',
      status: ''
    };
    await this.queryServiceName(params);
  }
};
</script>

<style lang="stylus" scoped>
.top-left
  width 240px
  height 40px
  float left
  margin-left 5px

.interfaceName
 width 210px

.my-sticky-column-table >>>
  th:first-child,td:first-child
    background-color #fff
    opacity 1
  th:first-child
    color #9c9a9a
  th:first-child,td:first-child
    position sticky
    left 0
    z-index 1
  .td-desc
    max-width 300px
    overflow hidden
    text-overflow ellipsis
</style>
