<template>
  <fdev-table
    titleIcon="list_s_f"
    :data="data"
    :columns="columns"
    row-key="id"
    :title="title"
    class="modifiedTable"
    :expanded.sync="expanded"
    ref="table"
    no-select-cols
  >
    <template v-slot:header="props">
      <fdev-tr :props="props">
        <fdev-th auto-width>
          <fdev-checkbox
            color="grey-8"
            indeterminate-value="maybe"
            v-model="selectAll"
            @input="handleSelectAll(data)"
          />
        </fdev-th>

        <fdev-th class="text-left">需求编号</fdev-th>

        <fdev-th class="text-left">需求名</fdev-th>

        <fdev-th class="text-left">需求阶段</fdev-th>

        <fdev-th class="text-left">上线确认书到达时间</fdev-th>
      </fdev-tr>
    </template>

    <template v-slot:body="props">
      <fdev-tr :props="props">
        <!-- 需求维度 -->
        <fdev-td class="text-ellipsis">
          <fdev-btn
            flat
            @click="props.expand = !props.expand"
            :icon="props.expand ? 'expand_more' : 'keyboard_arrow_right'"
          />
        </fdev-td>

        <fdev-td class="text-ellipsis">
          <span
            v-if="props.row.rqrmnt_id"
            class="link"
            @click="routeTo(props.row.rqrmnt_id)"
          >
            {{ props.row.rqrmnt_num }}
          </span>

          <span v-else>{{ props.row.rqrmnt_num || '-' }}</span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.rqrmnt_num || '-' }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>

        <fdev-td class="text-ellipsis">
          {{ props.row.rqrmnt_name }}
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.rqrmnt_name || '-' }}
            </fdev-banner>
          </fdev-popup-proxy>
          <span class="hasFile" v-if="!existFile(props.row)">
            ----该需求下的投产任务未上传文件
          </span>
        </fdev-td>

        <fdev-td class="text-ellipsis" :title="props.row.stage">
          {{ props.row.stage || '-' }}
        </fdev-td>

        <fdev-td class="text-ellipsis" :title="props.row.operate_time">
          {{ props.row.operate_time || '-' }}
        </fdev-td>
      </fdev-tr>

      <fdev-tr
        v-show="props.row.release_node_name && props.expand"
        :props="props"
      >
        <fdev-td colspan="100%">
          <fdev-table
            :data="props.row.task_list"
            :columns="expandColumsTask"
            row-key="task_id"
            :expanded.sync="expanded"
            no-select-cols
            hide-bottom
            :pagination="{
              rowsPerPage: 0
            }"
            noExport
          >
            <!-- 第二层table -->
            <template v-slot:header="props">
              <fdev-tr :props="props">
                <fdev-th auto-width />
                <fdev-th class="text-left">任务名</fdev-th>
                <fdev-th class="text-left">任务负责人</fdev-th>
                <fdev-th class="text-left">任务阶段</fdev-th>
              </fdev-tr>
            </template>

            <template v-slot:body="props">
              <fdev-tr :props="props">
                <fdev-td class="text-ellipsis">
                  <fdev-btn
                    flat
                    v-if="props.row.task_file.length > 0"
                    @click="props.expand = !props.expand"
                    :icon="
                      props.expand ? 'expand_more' : 'keyboard_arrow_right'
                    "
                  />
                </fdev-td>

                <fdev-td class="text-ellipsis">
                  <router-link
                    :to="{ path: `/job/list/${props.row.task_id}` }"
                    class="link"
                  >
                    <span>{{ props.row.task_name }}</span>
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ props.row.task_name || '-' }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </router-link>
                  <span class="hasFile" v-if="props.row.task_file.length < 1">
                    ----该任务下投产类文档未上传
                  </span>
                </fdev-td>

                <fdev-td class="text-ellipsis">
                  <router-link
                    :to="{
                      path: `/user/list/${props.row.master[0].id}`
                    }"
                    class="link"
                  >
                    {{ props.row.master[0].user_name_cn }}
                  </router-link>
                </fdev-td>

                <fdev-td class="text-left" :title="props.row.stage">
                  {{ props.row.stage }}
                </fdev-td>
              </fdev-tr>

              <fdev-tr :props="props" v-show="props.expand">
                <fdev-td colspan="100%" class="text-ellipsis">
                  <fdev-table
                    :data="props.row.task_file"
                    :columns="expandColumsFiles"
                    hide-bottom
                    hide-header
                    row-key="name"
                    no-select-cols
                    noExport
                    :pagination="{
                      rowsPerPage: 0
                    }"
                  >
                    <template v-slot:body="props">
                      <fdev-tr>
                        <fdev-td>
                          <fdev-checkbox
                            v-model="choosenFiles"
                            :val="props.row"
                            color="grey-8"
                          />
                          <a
                            class="link"
                            @click="
                              $emit('download', {
                                moduleName: 'fdev-task',
                                path: props.row.path
                              })
                            "
                          >
                            {{ props.row.name }}
                            <fdev-popup-proxy context-menu>
                              <fdev-banner style="max-width:300px">
                                {{ props.row.name || '-' }}
                              </fdev-banner>
                            </fdev-popup-proxy>
                          </a>
                        </fdev-td>
                      </fdev-tr>
                    </template>
                  </fdev-table>
                </fdev-td>
              </fdev-tr>
            </template>
          </fdev-table>
        </fdev-td>
      </fdev-tr>
    </template>
  </fdev-table>
</template>
<script>
import { taskStage } from '@/utils/utils';
export default {
  data() {
    return {
      columns: [
        { name: 'rqrmnt_num', label: '需求编号', field: 'rqrmnt_num' },
        { name: 'rqrmnt_name', label: '需求名', field: 'rqrmnt_name' },
        { name: 'stage', label: '需求阶段', field: 'stage' },
        {
          name: 'operate_time',
          label: '上线确认书到达时间',
          field: 'operate_time'
        }
      ],
      expandColumsFiles: [{ name: 'name', label: '文件名', field: 'name' }],
      expandColumsTask: [
        {
          name: 'name',
          label: '任务名',
          field: 'name'
        }
      ],
      selectAll: false,
      expanded: [],
      choosenFiles: [],
      taskStage: taskStage
    };
  },

  props: {
    title: {
      type: String
    },
    data: {
      type: Array,
      default: () => []
    },
    selected: {
      type: Array,
      default: () => []
    }
  },

  watch: {
    choosenFiles(newVal, oldVal) {
      if (newVal.length === this.allFiles.length) {
        this.selectAll = true;
      } else if (newVal.length === 0) {
        this.selectAll = false;
      } else {
        this.selectAll = 'maybe';
      }
      this.$emit('update:selected', newVal);
    }
  },

  computed: {
    allFiles() {
      let files = [];
      this.data.forEach(item => {
        if (item.task_list.length > 0) {
          item.task_list.forEach(i => {
            if (i.task_file.length > 0) {
              files = [...files, ...i.task_file];
            }
          });
        }
      });
      return files;
    }
  },

  methods: {
    existFile({ task_list }) {
      return task_list.every(item => item.task_file.length > 0);
    },
    handleSelectAll(val) {
      if (this.selectAll) {
        this.choosenFiles = this.allFiles;
      } else {
        this.choosenFiles = [];
      }
    },
    routeTo(id) {
      this.$router.push({ name: 'rqrProfile', params: { id: id } });
    }
  }
};
</script>

<style lang="stylus" scoped>
.hasFile
  color red
.link
  cursor: pointer;
  color: #0663BE;
</style>
