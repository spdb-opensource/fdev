<template>
  <f-block>
    <div class="bg-white">
      <Loading :visible="loading">
        <fdev-table
          :data="tableData"
          :columns="columns"
          class="my-table"
          row-key="id"
          :pagination.sync="pagination"
          noExport
          title="应用生产信息"
          titleIcon="list_s_f"
          @request="queryProInfo"
          :visible-columns="visibleColumns"
          ref="table"
          no-select-cols
        >
          <template v-slot:top-bottom-opt>
            <div class="row no-wrap self-center wrapper q-mb-md">
              <fdev-select
                @input="saveSearchType($event)"
                :value="params.type"
                style="width:149.5px"
                :options="paramsOptions"
                option-label="label"
                option-value="value"
                emit-value
                map-options
                clearable
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.label">
                        {{ scope.opt.label }}
                      </fdev-item-label>
                      <fdev-item-label caption :title="scope.opt.value">
                        {{ scope.opt.value }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
              <fdev-input
                :value="params.value"
                @input="saveSearchValue($event)"
                style="width:278.5px"
                @keyup.enter.native="handleSearch()"
                :disable="isDisable"
              >
                <template v-slot:append>
                  <f-icon
                    name="search"
                    class="cursor-pointer"
                    @click="handleSearch()"
                  />
                </template>
              </fdev-input>
            </div>
          </template>
          <!-- 应用名 -->
          <template v-slot:body-cell-deploy_name="props">
            <fdev-td class="text-ellipsis">
              <router-link
                :to="`/envModel/productionInfoDetails/${props.row.deploy_name}`"
                class="link"
                :title="props.row.deploy_name"
              >
                {{ props.row.deploy_name }}
                <fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ props.row.deploy_name }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </fdev-td>
          </template>
        </fdev-table>
      </Loading>
    </div>
  </f-block>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import { perform } from '../../utils/constants';

export default {
  name: 'ProductionInfoList',
  components: { Loading },
  data() {
    return {
      tableData: [],
      loading: false,
      ...perform,
      pagination: {}
    };
  },
  filters: {
    spdbManagers(val) {
      return val.length === 0
        ? '-'
        : val.map(item => item.user_name_cn).join(',');
    },
    devManagers(val) {
      return val.length === 0
        ? '-'
        : val.map(item => item.user_name_cn).join(',');
    }
  },
  computed: {
    ...mapState('userActionSaveEnv/productionInfoList', [
      'visibleColumns',
      'currentPage',
      'params'
    ]),
    ...mapState('environmentForm', ['productionInfoList']),

    // 1.初始进入页面，选择框未选择type，输入框不可点击,
    // 2.当清除选择框type，输入框不可点击
    isDisable() {
      return this.params.type ? false : true;
    }
  },
  watch: {
    pagination(val) {
      this.saveCurrentPage(val);
    },
    'params.type': {
      handler(newVal, oldVal) {
        // 若oldVal存在
        if (oldVal) {
          // 清除或切换type，清空value
          if (newVal !== oldVal) {
            this.params.value = '';
            // 若清除选择框type，并查全量数据
            if (newVal === null) {
              this.queryProInfo();
            }
          }
        }
      }
    }
  },
  async created() {
    this.pagination = this.currentPage;
    await this.queryProInfo();
  },
  methods: {
    ...mapMutations('userActionSaveEnv/productionInfoList', [
      'saveSearchType',
      'saveSearchValue',
      'saveCurrentPage',
      'saveVisibleColumns'
    ]),
    ...mapActions('environmentForm', ['queryDeployments']),
    // 点击查询
    handleSearch() {
      this.queryProInfo();
    },
    //查询生产信息
    async queryProInfo(props) {
      if (props && props.pagination) {
        const { page, rowsPerPage } = props.pagination;
        this.pagination.page = page;
        this.pagination.rowsPerPage = rowsPerPage;
      }
      let params = {
        type: this.params.type === null ? '' : this.params.type,
        value: this.params.value
      };
      this.loading = true;
      // 关于params查询情况：
      // { type: '', value: '' }、{ type: '有值', value: '' } 查全量
      // { type: '有值', value: '有值' } 指定条件查询
      await this.queryDeployments({
        params: params,
        page: this.pagination.page.toString(),
        pageSize: this.pagination.rowsPerPage.toString()
      });
      this.pagination.rowsNumber = this.productionInfoList.total;
      this.tableData = this.productionInfoList.deploy_list;
      this.loading = false;
    }
  }
};
</script>

<style lang="stylus" scoped>
.span-margin
  margin-right 25px
.my-sticky-column-table >>> .q-table__top>div:first-child
  margin-bottom 10px !important
.wrapper >>>
  .q-select.q-field--outlined .q-field__control
    border-top-right-radius 0
    border-bottom-right-radius 0
  .q-input.q-field--outlined .q-field__control
    position relative
    left -1px
    border-top-left-radius 0
    border-bottom-left-radius 0
    &:before, &:after
      border-left none
  .q-input.q-field--outlined.q-field--focused .q-field__control
    &:before, &:after
      border-left 1px solid
</style>
