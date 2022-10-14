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
          :pagination.sync="pagination"
          class="table-max-wH"
          no-export
        >
          <template v-slot:top-right> </template>
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
          <template v-slot:body-cell-redmine_id="props">
            <fdev-td class="td-desc ellipsis" :title="props.row.redmine_id">
              {{ props.row.redmine_id }}
            </fdev-td>
          </template>
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
                  <span>{{ user.user_name_cn }} </span>
                </router-link>
              </span>
            </fdev-td>
          </template>
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
                  <span>{{ user.user_name_cn }}</span>
                </router-link>
              </span>
            </fdev-td>
          </template>
          <template v-slot:body-cell-app="props">
            <fdev-td class="td-desc ellipsis">
              <router-link
                :to="{ path: `/app/list/${props.row.project_id}` }"
                class="link"
                v-if="props.row.project_id"
                :title="props.row.project_name"
              >
                <span>{{ props.row.project_name }}</span>
              </router-link>
              <span v-else :title="props.row.project_name">{{
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
import { mapState } from 'vuex';
import { getGroupFullName } from '@/utils/utils';
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
          name: 'redmine_id',
          label: '实施单元编号',
          field: 'redmine_id',
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
      ],
      pagination: {
        rowsPerPage: 0
      }
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
    }
  },
  computed: {
    ...mapState('userForm', {
      groups: 'groups'
    }),
    ...mapState('dashboard', {
      taskList: 'taskList'
    }),
    title() {
      if (this.dateTitle) return this.dateTitle;
      const task = this.taskList[0] ? this.taskList[0] : {};
      const stage = task.stage;
      const groupId = task.group ? task.group.id : '';
      const groupFullName = this.getGroupFullName(groupId);
      return `${groupFullName}-${stage}阶段`;
    }
  },
  methods: {
    getGroupFullName(val) {
      return getGroupFullName(this.groups, val);
    },
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
