<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-table
        :data="rqrDelayData"
        :columns="columns"
        row-key="job.id"
        :loading="loading"
        :visible-columns="visibleColumns"
        :onSelectCols="updatevisibleColumns"
        :pagination.sync="pagination"
        titleIcon="list_s_f"
        title="需求延期任务情况列表"
        class="my-sticky-column-table"
        :onSearch="queryDelayTask"
      >
        <!-- 筛选条件 -->
        <template v-slot:top-bottom>
          <f-formitem
            label="维度"
            class="col-4 q-pr-md"
            label-style="width:60px"
            bottom-page
          >
            <fdev-select
              use-input
              :value="rqrDelay.name"
              @input="updateRqrDelayName($event)"
              :options="nameOptions"
              option-label="label"
              option-value="value"
            >
            </fdev-select
          ></f-formitem>

          <f-formitem
            label="名称"
            class="col-4 q-pr-md"
            label-style="width:60px"
            bottom-page
          >
            <fdev-select
              use-input
              @filter="filterGroups"
              option-label="name_en"
              option-value="id"
              :options="comNameList"
              :value="rqrDelay.groupObj"
              @input="updateRqrDelayGroupObj($event)"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name_en">
                      {{ scope.opt.name_en }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.name_cn">
                      {{ scope.opt.name_cn }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select></f-formitem
          >
          <f-formitem
            label="参与人"
            class="col-4 q-pr-md"
            label-style="width:60px"
            bottom-page
          >
            <fdev-select
              use-input
              option-label="user_name_cn"
              option-value="id"
              @filter="filterUser"
              :options="userList"
              :value="rqrDelay.memberObj"
              @input="updateRqrDelayMember($event)"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.user_name_cn">
                      {{ scope.opt.user_name_cn }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="scope.opt.user_name_en">
                      {{ scope.opt.user_name_en }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select></f-formitem
          >
        </template>
        <!-- 组件、骨架、镜像名称 -->
        <template v-slot:body-cell-name="props">
          <fdev-td auto-width :title="props.value">
            <router-link
              :to="`/${nameType[props.row.type]}/${props.row.id}`"
              class="link"
            >
              <span class="overflow">{{ props.value }}</span
              ><fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.value }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>
        <!-- 类型 -->
        <template v-slot:body-cell-type="props">
          <fdev-td>
            <span>{{ comType[props.value] }} </span>
          </fdev-td>
        </template>
        <!-- 管理员 -->
        <template v-slot:body-cell-rqrmnts_admin="props">
          <fdev-td
            class="td-desc ellipsis"
            :title="showTitleNames(props.value)"
          >
            <span v-for="(item, index) in props.value" :key="index">
              <router-link
                v-if="item.user_name_cn"
                :to="`/user/list/${item.id}`"
                class="link "
              >
                {{ item.user_name_cn
                }}<fdev-popup-proxy context-menu>
                  <fdev-banner style="max-width:300px">
                    {{ showTitleNames(props.value) }}
                  </fdev-banner>
                </fdev-popup-proxy>
              </router-link>
            </span>
          </fdev-td>
        </template>
        <!-- 开发人员 -->
        <template v-slot:body-cell-develop="props">
          <fdev-td :title="props.value.user_name_cn">
            <span v-if="props.value.user_name_cn">
              <router-link
                :to="`/user/list/${props.row.develop.id}`"
                class="link "
              >
                {{ props.value.user_name_cn }}
              </router-link>
            </span>
            <span v-else>-</span>
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapGetters, mapMutations } from 'vuex';
import {
  setRqrPagination,
  getRqrPagination,
  getRqrTableCol,
  setRqrTableCol
} from '@/modules/Dashboard/utils/setting';
import { deepClone } from '@/utils/utils';
import {
  delayNameOptions,
  nameType,
  comType
} from '@/modules/Dashboard/utils/constants';

export default {
  name: 'RqrDelay',
  components: { Loading },
  data() {
    return {
      pagination: getRqrPagination(),
      terms: [],

      rqrDelayData: [],
      loading: false,
      cloneNameList: [],
      columns: [
        {
          name: 'name',
          label: '组件、骨架、镜像名称',
          field: 'name',
          align: 'left',
          copy: true
        },
        {
          name: 'type',
          label: '类型',
          field: 'type',
          align: 'left'
        },
        {
          name: 'rqrmnts_admin',
          label: '管理员',
          field: 'rqrmnts_admin',
          align: 'left',
          copy: true
        },
        {
          name: 'rqrmnts_name',
          label: '优化需求标题',
          field: 'rqrmnts_name',
          align: 'left',
          copy: true
        },
        {
          name: 'develop',
          label: '开发人员',
          field: 'develop',
          align: 'left'
        },
        {
          name: 'feature_branch',
          label: '开发分支',
          field: 'feature_branch',
          align: 'left',
          copy: true
        },
        {
          name: 'stage',
          label: '当前阶段',
          field: 'stage',
          align: 'left'
        },
        {
          name: 'due_date',
          label: '计划完成日期',
          field: 'due_date',
          align: 'left'
        },
        {
          name: 'delay_date',
          label: '延期天数',
          field: 'delay_date',
          align: 'left'
        }
      ],

      comNameList: [],
      userList: [],
      nameOptions: delayNameOptions,
      nameType,
      comType
    };
  },
  watch: {
    'rqrDelay.name': {
      deep: true,
      handler(val) {
        this.getConName();
      }
    },
    visibleColumns(val) {
      setRqrTableCol(val);
    },
    pagination(val) {
      setRqrPagination({
        rowsPerPage: val.rowsPerPage
      });
    }
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapState('userActionSaveDashboard/basicFrame/rqrDelay', {
      rqrDelay: 'createrqrDelayModel',
      visibleColumns: 'visibleColumns'
    }),

    ...mapState('dashboard', ['rqrDelayList', 'allComNameList']),
    ...mapGetters('user', ['isLoginUserList'])
  },
  methods: {
    ...mapActions('dashboard', ['queryIssueDelay', 'queryallComName']),
    ...mapActions('user', ['fetch']),
    ...mapMutations('userActionSaveDashboard/basicFrame/rqrDelay', [
      'updateRqrDelayName',
      'updateRqrDelayGroupObj',
      'updateRqrDelayMember',
      'updatevisibleColumns'
    ]),
    showTitleNames(names) {
      let sName = '';
      if (names.length > 0) {
        for (let i = 0; i < names.length; i++) {
          sName += `${names[i].user_name_cn} `;
        }
        sName = sName.substring(0, sName.length - 1);
      }
      return sName;
    },
    async queryDelayTask() {
      let params = {
        name: this.rqrDelay.name ? this.rqrDelay.name.value : '',
        id: this.rqrDelay.groupObj ? this.rqrDelay.groupObj.id : '',
        Member: this.rqrDelay.memberObj ? this.rqrDelay.memberObj.id : ''
      };
      this.loading = true;
      await this.queryIssueDelay(params);
      this.rqrDelayData = this.rqrDelayList;
      this.loading = false;
    },
    filterGroups(val, update) {
      update(() => {
        this.comNameList = this.cloneNameList.filter(v => {
          return (
            v.name_cn.indexOf(val.toLowerCase()) > -1 ||
            v.name_en.indexOf(val.toLowerCase()) > -1
          );
        });
      });
    },
    filterUser(val, update) {
      update(() => {
        this.userList = this.isLoginUserList.filter(v => {
          return (
            v.user_name_cn.indexOf(val.toLowerCase()) > -1 ||
            v.user_name_en.toLowerCase().indexOf(val.toLowerCase()) > -1
          );
        });
      });
    },
    getRqrmntName(id) {
      let rqrmnts = this.rqrmntIdArr.find(rqrmnts => rqrmnts._id === id);
      return rqrmnts ? rqrmnts.rqrmntNum : '';
    },
    async getConName() {
      await this.queryallComName({
        name: this.rqrDelay.name ? this.rqrDelay.name.value : ''
      });
      this.cloneNameList = deepClone(this.allComNameList);
      this.comNameList = this.cloneNameList;
    }
  },
  async created() {
    this.getConName();
    await this.fetch();
    this.userList = this.isLoginUserList;
    this.queryDelayTask();
  },
  mounted() {
    const tempVisibleColumns = this.visibleColumns;
    let Columns = getRqrTableCol() ? getRqrTableCol() : tempVisibleColumns;
    this.updatevisibleColumns(Columns);
    let examColumns =
      this.visibleColumns.length === 0 || this.visibleColumns.length === 1;
    if (examColumns) {
      this.updatevisibleColumns(tempVisibleColumns);
    }
  }
};
</script>

<style lang="stylus" scoped>

/* >>>.q-table td
    max-width 100% !important */
.overflow
  overflow hidden
  white-space nowrap
  text-overflow ellipsis
  max-width 250px
  display inline-block
</style>
