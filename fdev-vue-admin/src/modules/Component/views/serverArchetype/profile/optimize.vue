<template>
  <Loading :visible="loading">
    <fdev-table
      :data="tableData"
      :columns="columns"
      :pagination.sync="pagination"
      title="优化需求列表"
      titleIcon="list_s_f"
      no-export
      :visible-columns="visibleColumns"
      :onSelectCols="updatevisibleColumns"
    >
      <template v-slot:top-right>
        <fdev-btn
          label="新增优化需求"
          normal
          ficon="add"
          @click="handleOptimizeDialogOpened"
          v-if="isManger && archetypeDetail.type !== 'vue'"
        />
      </template>
      <template v-slot:body-cell-name_cn="props">
        <fdev-td :title="props.value" class="text-ellipsis">
          <router-link
            :to="`/user/list/${props.row.assignee}`"
            class="link"
            v-if="props.row.assignee"
          >
            {{ props.value }}
          </router-link>
          <span v-else>{{ props.value || '-' }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-stage="props">
        <fdev-td :title="props.row.stage | textFilters" class="text-ellipsis">
          {{ props.row.stage | textFilters }}
        </fdev-td>
      </template>

      <template v-slot:body-cell-btn="props">
        <fdev-td>
          <fdev-btn
            label="处理"
            flat
            v-if="isManger || props.row.assignee === currentUser.id"
            @click="
              $router.push({
                name: 'ArchetypeHandlePage',
                params: {
                  id: props.row.id,
                  archetype_id: props.row.archetype_id
                }
              })
            "
          />
        </fdev-td>
      </template>
    </fdev-table>

    <f-dialog right v-model="optimizeDialogOpen" title="新增优化需求">
      <f-formitem diaS label="骨架名称">
        <fdev-input
          ref="optimizeModel.archetype"
          type="text"
          disable
          :rules="[() => true]"
          v-model="optimizeModel.archetype"
        />
      </f-formitem>
      <f-formitem diaS label="标题" required>
        <fdev-input
          ref="optimizeModel.title"
          type="text"
          v-model="$v.optimizeModel.title.$model"
          :rules="[() => $v.optimizeModel.title.required || '请输入标题']"
        />
      </f-formitem>
      <f-formitem
        diaS
        label="目标版本"
        required
        help="第一段不限制长度，第二、三段最多两位"
      >
        <fdev-input
          ref="optimizeModel.target_version"
          type="text"
          v-model="$v.optimizeModel.target_version.$model"
          :rules="[
            () => $v.optimizeModel.target_version.required || '请输入目标版本',
            () =>
              $v.optimizeModel.target_version.examine ||
              '请输入正确的格式:*.x.x, 只能输入数字并不能以0开头'
          ]"
        />
      </f-formitem>
      <f-formitem diaS label="开发分支名" required>
        <fdev-input
          ref="feature_branch"
          type="text"
          disable=""
          v-model="$v.optimizeModel.feature_branch.$model"
          :rules="[
            () =>
              $v.optimizeModel.feature_branch.required || '请输入拉取的分支名',
            () => $v.optimizeModel.feature_branch.examine || '不能输入中文'
          ]"
        />
      </f-formitem>
      <f-formitem diaS label="开发人员" required>
        <fdev-select
          use-input
          emit-value
          map-options
          option-value="id"
          :options="developerList"
          option-label="user_name_cn"
          ref="optimizeModel.assignee"
          v-model="$v.optimizeModel.assignee.$model"
          @filter="userFilter"
          :rules="[
            () => $v.optimizeModel.assignee.required || '请选择开发人员'
          ]"
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
        </fdev-select>
      </f-formitem>
      <f-formitem diaS label="计划完成日期" required>
        <f-date
          mask="YYYY/MM/DD"
          v-model="$v.optimizeModel.due_date.$model"
          :options="dateOptions"
          :rules="[
            () => $v.optimizeModel.due_date.required || '请输入计划完成日期'
          ]"
        />
      </f-formitem>
      <f-formitem diaS label="需求描述" required>
        <fdev-input
          type="textarea"
          ref="optimizeModel.desc"
          v-model="$v.optimizeModel.desc.$model"
          :rules="[() => $v.optimizeModel.desc.required || '请填写需求描述']"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn
          label="确定"
          :loading="globalLoading['componentForm/optimizeArchetype']"
          dialog
          @click="handleOptimizeFormAllTip"
        />
      </template>
    </f-dialog>
  </Loading>
</template>

<script>
import { mapActions, mapState, mapGetters, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify } from '@/utils/utils';
import moment from 'moment';
import {
  getPagination,
  setPagination
} from '@/modules/Component/utils/setting.js';
import {
  archetypeOptimizeModel,
  archetypeStage,
  serverArchetypeProfileOptimizeColums
} from '@/modules/Component/utils/constants.js';

export default {
  name: 'ArchetypeOptimizeList',
  components: { Loading },
  data() {
    return {
      loading: false,
      archetype_id: '',
      optimizeDialogOpen: false,
      tableData: [],
      optimizeModel: archetypeOptimizeModel(),
      developerList: [],
      pagination: getPagination(),
      columns: serverArchetypeProfileOptimizeColums
    };
  },
  validations: {
    optimizeModel: {
      feature_branch: {
        required,
        examine(val) {
          if (!val) {
            return true;
          }
          const reg = /[\u4E00-\u9FA5]/;
          return !reg.test(val);
        }
      },
      archetype_id: {
        required
      },
      assignee: {
        required
      },
      component: {},
      title: {
        required
      },
      desc: {
        required
      },
      target_version: {
        required,
        examine(val) {
          if (!val) {
            return true;
          }
          const reg = /^(?!0)(\d{1,})\.((?!0)\d{2}|\d{1})\.((?!0)\d{2}|\d{1})$/;
          return reg.test(val);
        }
      },
      due_date: {
        required
      }
    }
  },
  watch: {
    'optimizeModel.target_version': {
      handler(val) {
        if (!val) {
          return;
        }
        this.optimizeModel.feature_branch = `${val}-DEV`;
      }
    },
    pagination(val) {
      setPagination({
        rowsPerPage: val.rowsPerPage
      });
    }
  },
  computed: {
    ...mapState(
      'userActionSaveComponent/archetypeManage/archetype/archetypeOptimize',
      ['visibleColumns']
    ),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('componentForm', ['archetypeIssues', 'archetypeDetail']),
    ...mapState('user', ['currentUser']),
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
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
    },
    developerOptions() {
      return this.userList.filter(user => {
        return user.role.some(role => {
          return role.name === '开发人员';
        });
      });
    },
    columnsOptions() {
      const arr = this.columns.slice(0);
      return arr.splice(0, arr.length - 1);
    }
  },
  filters: {
    textFilters(val) {
      return archetypeStage[val];
    }
  },
  methods: {
    ...mapMutations(
      'userActionSaveComponent/archetypeManage/archetype/archetypeOptimize',
      ['updatevisibleColumns']
    ),
    ...mapActions('componentForm', [
      'optimizeArchetype',
      'queryArchetypeIssues'
    ]),
    userFilter(val, update, abort) {
      const needle = val.toLowerCase();
      update(() => {
        this.developerList = this.developerOptions.filter(
          user =>
            user.user_name_cn.indexOf(needle) > -1 ||
            user.user_name_en.toLowerCase().indexOf(needle) > -1
        );
      });
      if (this.developerList.length === 0) {
        this.developerList = this.developerOptions;
      }
    },
    dateOptions(date) {
      const today = moment(new Date()).format('YYYY/MM/DD');
      return date >= today;
    },
    handleOptimizeDialogOpened() {
      this.optimizeModel = archetypeOptimizeModel();
      this.optimizeModel.archetype_id = this.archetype_id;
      this.optimizeModel.archetype = this.archetypeDetail.name_en;
      this.optimizeDialogOpen = true;
    } /* 提交优化 */,
    handleOptimizeFormAllTip() {
      this.$v.optimizeModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('optimizeModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.optimizeModel.$invalid) {
        return;
      }
      this.handleOptimize();
    },
    async handleOptimize() {
      await this.optimizeArchetype(this.optimizeModel);
      successNotify('新增优化需求成功');
      this.optimizeDialogOpen = false;
      this.init();
    },
    confirmToClose() {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this.optimizeDialogOpen = false;
        });
    },
    async init() {
      this.loading = true;
      await this.queryArchetypeIssues({
        archetype_id: this.archetype_id
      });
      this.tableData = this.archetypeIssues;
      this.loading = false;
    }
  },
  created() {
    this.archetype_id = this.$route.params.id;
    this.init();
  }
};
</script>
