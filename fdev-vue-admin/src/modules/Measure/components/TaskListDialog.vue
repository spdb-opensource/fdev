<template>
  <f-block>
    <f-dialog
      :value="value"
      @input="$emit('input', $event)"
      :title="`${title}任务`"
      right
    >
      <Loading :visible="showLoading">
        <fdev-table
          title="任务列表"
          titleIcon="list_s_f"
          :data="taskList"
          :columns="columns"
          row-key="id"
          class="table-max-wH"
          no-export
        >
          <!-- 任务名称 -->
          <template v-slot:body-cell-name="props">
            <fdev-td class="td-desc ellipsis">
              <router-link
                :to="{ path: `/job/list/${props.row.id}` }"
                class="link"
                v-if="props.row.id"
                :title="props.row.name"
              >
                <span>{{ props.row.name }}</span>
              </router-link>
              <span v-else :title="props.row.name">{{
                props.row.name || '-'
              }}</span>
            </fdev-td>
          </template>
          <!-- 研发单元编号 -->
          <template v-slot:body-cell-redmine_id="props">
            <fdev-td class="td-desc ellipsis" :title="props.row.redmine_id">
              {{ props.row.redmine_id }}
            </fdev-td>
          </template>
          <!-- 任务负责人 -->
          <template v-slot:body-cell-master="props">
            <fdev-td
              class="td-desc ellipsis"
              :title="showTitleNames(props.row.master)"
            >
              <span v-for="(user, index) in props.row.master" :key="index">
                <router-link
                  :to="{ path: `/user/list/${user.id}` }"
                  class="link"
                  v-if="user.id"
                >
                  <span
                    ><span v-if="index > 0"> ,</span>{{ user.user_name_cn }}
                  </span>
                </router-link>
              </span>
            </fdev-td>
          </template>
          <!-- 行内项目负责人 -->
          <template v-slot:body-cell-spdb_master="props">
            <fdev-td
              class="td-desc ellipsis"
              :title="showTitleNames(props.row.spdb_master)"
            >
              <span v-for="(user, index) in props.row.spdb_master" :key="index">
                <router-link
                  :to="{ path: `/user/list/${user.id}` }"
                  class="link"
                  v-if="user.id"
                >
                  <span
                    ><span v-if="index > 0"> ,</span
                    >{{ user.user_name_cn }}</span
                  >
                </router-link>
              </span>
            </fdev-td>
          </template>
          <!-- 所属应用 -->
          <template v-slot:body-cell-app="props">
            <fdev-td class="td-desc ellipsis">
              <span :title="props.row.project_name">{{
                props.row.project_name || '-'
              }}</span>
            </fdev-td>
          </template>
        </fdev-table>
      </Loading>
    </f-dialog>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
export default {
  components: { Loading },
  data() {
    return {
      visibleColumns: ['name', 'redmine_id', 'master', 'spdb_master', 'app'],
      columns: [
        {
          name: 'name',
          label: '任务名称',
          field: 'name',
          align: 'left'
        },
        {
          name: 'fdev_implement_unit_no',
          label: '研发单元编号',
          field: 'fdev_implement_unit_no',
          align: 'left'
        },
        {
          name: 'master',
          label: '任务负责人',
          field: 'master',
          align: 'left'
        },
        {
          name: 'spdb_master',
          label: '行内项目负责人',
          field: 'spdb_master',
          align: 'left'
        },
        {
          name: 'app',
          label: '所属应用',
          field: 'app',
          align: 'left'
        }
      ]
    };
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    dateTitle: String,
    showLoading: {
      type: Boolean,
      default: false
    },
    taskList: Array,
    stageName: String,
    groupName: String
  },
  computed: {
    title() {
      const groupName = this.groupName ? this.groupName : '';
      return `${groupName}${this.stageName}阶段`;
    }
  },
  methods: {
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
    updateVisibleColumns(columns) {
      this.visibleColumns = columns;
    }
  }
};
</script>

<style lang="stylus" scoped>
.dialog-Height
  min-width 1230px
.table-max-wH
  width 780px;
  max-height: 600px
</style>
