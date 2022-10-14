<template>
  <f-dialog
    :value="value"
    right
    @input="$emit('input', $event)"
    :title="`${type === 'update' ? '编辑' : '新建'}投产窗口`"
  >
    <f-formitem required label="投产日期" diaS>
      <f-date
        @input="getNodeName($event)"
        :options="sitOptions"
        v-model="$v.updateReleaseNodeModel.release_date.$model"
        mask="YYYY-MM-DD"
        :rules="[
          () =>
            $v.updateReleaseNodeModel.release_date.required || '请选择投产日期'
        ]"
      />
    </f-formitem>

    <f-formitem required label="投产窗口名称" diaS>
      <fdev-input
        input-debounce="0"
        type="text"
        readonly
        v-model="updateReleaseNodeModel.release_node_name"
        :rules="[() => true]"
      />
    </f-formitem>
    <f-formitem required label="所属小组" diaS>
      <fdev-select
        ref="updateReleaseNodeModel.owner_groupId"
        v-model="updateReleaseNodeModel.owner_groupId"
        use-input
        emit-value
        map-options
        option-label="owner_group_name"
        :options="groupOptions"
        @filter="filterGroup"
        :rules="[val => !!val || '请选择所属小组']"
      />
    </f-formitem>
    <f-formitem required label="科技负责人" diaS>
      <fdev-select
        input-debounce="0"
        use-input
        @filter="filterSpdbFn"
        :options="spdbFn"
        option-label="user_name_cn"
        option-value="user_name_en"
        ref="updateReleaseNodeModel.release_spdb_manager"
        v-model="$v.updateReleaseNodeModel.release_spdb_manager.$model"
        :rules="[
          () =>
            $v.updateReleaseNodeModel.release_spdb_manager.required ||
            '请选择科技负责人'
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

    <f-formitem required label="投产负责人" diaS>
      <fdev-select
        input-debounce="0"
        use-input
        option-label="user_name_cn"
        option-value="user_name_en"
        ref="updateReleaseNodeModel.release_manager"
        @filter="managerFilter"
        v-model="$v.updateReleaseNodeModel.release_manager.$model"
        :options="managerOptions"
        :rules="[
          () =>
            $v.updateReleaseNodeModel.release_manager.required ||
            '请选择投产负责人'
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
    <f-formitem required label="窗口类型" diaS>
      <div v-for="item in typeOptions" :key="item.value">
        <fdev-radio
          :val="item.value"
          v-model="updateReleaseNodeModel.type"
          :label="item.label"
          :disable="canChangeType"
        />
      </div>
    </f-formitem>
    <template v-slot:btnSlot>
      <fdev-btn
        label="提交"
        :loading="globalLoading[loading]"
        dialog
        @click="handleUpdateRelease"
    /></template>
  </f-dialog>
</template>

<script>
import { mapState, mapActions, mapGetters } from 'vuex';
import { createReleaseNodeModel } from '../../../utils/model';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify, deepClone, perform } from '@/utils/utils';
export default {
  name: 'ReleaseDialog',
  data() {
    return {
      typeOptions: perform.typeOptions,
      updateReleaseNodeModel: createReleaseNodeModel(),
      managerOptions: [],
      spdbFn: [],
      today: '',
      originalReleaseDate: '',
      thisRealeaseManager: '',
      canChangeType: false,
      groupOptions: [] // 小组下拉选项
    };
  },
  validations: {
    updateReleaseNodeModel: {
      release_date: {
        required
      },
      release_spdb_manager: {
        required
      },
      release_manager: {
        required
      },
      owner_groupId: {
        required
      }
    }
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    release_node_name: {
      type: String
    },
    type: {
      type: String
    },
    releaseDetail: {
      type: Object
    },
    thirdLevelGroups: {
      type: Array
    }
  },
  watch: {
    async value(val) {
      this.groupOptions = this.thirdLevelGroups.slice();
      this.thisRealeaseManager = '';
      this.today = this.updateReleaseNodeModel.releaseDate;
      if (val && this.type === 'update') {
        this.updateReleaseNodeModel = deepClone(this.releaseDetail);
        this.updateReleaseNodeModel.release_manager = {
          user_name_cn: this.updateReleaseNodeModel.release_manager_name_cn,
          user_name_en: this.updateReleaseNodeModel.release_manager
        };
        this.updateReleaseNodeModel.release_spdb_manager = {
          user_name_cn: this.updateReleaseNodeModel
            .release_spdb_manager_name_cn,
          user_name_en: this.updateReleaseNodeModel.release_spdb_manager
        };
        this.updateReleaseNodeModel.owner_groupId = this.thirdLevelGroups.find(
          item =>
            item.owner_groupId === this.updateReleaseNodeModel.owner_groupId
        );
        await this.fetch();
        this.managerOptions = this.userList;
        this.thisRealeaseManager = this.releaseDetail.release_spdb_manager;
        this.originalReleaseDate = this.updateReleaseNodeModel.release_node_name;
        // 存量数据没有窗口类型字段，默认是微服务窗口
        if (!this.updateReleaseNodeModel.type) {
          this.$set(this.updateReleaseNodeModel, 'type', '1');
        }
      } else if (val && this.type === 'create') {
        this.fetch();

        // 对第三层级组及其子组按层级排序找到顶层组给小组赋值
        const sortedGroups = this.thirdLevelGroups.sort(
          (a, b) => a.level - b.level
        );
        this.updateReleaseNodeModel.owner_groupId = sortedGroups[0];
        await this.queryNodeName(this.updateReleaseNodeModel.releaseDate);
        this.updateReleaseNodeModel.release_node_name = this.releaseNodeName;
        if (!this.updateReleaseNodeModel.type) {
          this.$set(this.updateReleaseNodeModel, 'type', '1');
        }
      } else {
        this.updateReleaseNodeModel = createReleaseNodeModel();
      }
    },
    /* 用于匹配角色被更改的科技负责人下拉选项 */
    spdbManagerOptions(val) {
      this.spdbFn = val;
    },
    taskList(val) {
      this.canChangeType = val.length > 0 ? true : false;
    }
  },
  computed: {
    ...mapState('releaseForm', ['releaseNodeName', 'taskList']),
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    spdbManagerOptions() {
      return this.userList.filter(item => {
        return (
          item.role.some(role => {
            return role.name === '行内项目负责人';
          }) || item.user_name_en === this.thisRealeaseManager.user_name_en
        );
      });
    },
    loading() {
      if (this.type === 'update') {
        return 'releaseForm/update';
      }
      return 'releaseForm/create';
    }
  },
  methods: {
    ...mapActions('releaseForm', [
      'queryReleaseNodeDetail',
      'queryNodeName',
      'create',
      'update'
    ]),
    ...mapActions('user', ['fetch']),
    async handleUpdateRelease() {
      this.$v.updateReleaseNodeModel.$touch();

      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('updateReleaseNodeModel') > -1;
      });

      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );

      if (this.$v.updateReleaseNodeModel.$invalid) {
        return;
      }
      const params = {
        release_manager: this.updateReleaseNodeModel.release_manager
          .user_name_en,
        release_node_name: this.updateReleaseNodeModel.release_node_name,
        release_spdb_manager: this.updateReleaseNodeModel.release_spdb_manager
          .user_name_en,
        release_date: this.updateReleaseNodeModel.release_date,
        owner_group: this.updateReleaseNodeModel.owner_groupId,
        type: this.updateReleaseNodeModel.type
      };
      if (this.type === 'update') {
        if (params.release_node_name !== this.originalReleaseDate) {
          params.old_release_node_name = this.originalReleaseDate;
          this.$q
            .dialog({
              title: '风险提示！！',
              message:
                '<p>请谨慎修改投产窗口时间，可能会发生无法预期的问题！</p><p>包括但不限于：</p><p>1.master分支已包含此窗口的开发代码。</p><p>2.近期投产窗口TAG分支已包含此窗口的开发代码。</p><p>请确认是否修改投产窗口时间</p>',
              cancel: true,
              persistent: true,
              html: true
            })
            .onOk(async () => {
              await this.update(params);
              successNotify('编辑成功');
              this.$emit('click', params.release_node_name);
            });
        } else {
          await this.update(params);
          successNotify('编辑成功');
          this.$emit('click');
        }
      } else {
        await this.create(params);
        successNotify('创建成功');
        this.$emit('click');
      }
    },
    filterSpdbFn(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.spdbFn = this.spdbManagerOptions.filter(
          v =>
            v.user_name_cn.indexOf(needle) > -1 ||
            v.user_name_en.toLowerCase().indexOf(needle) > -1
        );
        if (this.spdbFn.length === 0) {
          this.spdbFn = this.spdbManagerOptions;
        }
      });
    },
    managerFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.managerOptions = this.userList.filter(
          v =>
            v.user_name_cn.indexOf(needle) > -1 ||
            v.user_name_en.toLowerCase().indexOf(needle) > -1
        );
        if (this.managerOptions.length === 0) {
          this.managerOptions = this.userList;
        }
      });
    },
    sitOptions(date) {
      return date >= this.today.replace(/-/g, '/');
    },
    async getNodeName(val) {
      await this.queryNodeName(val);
      this.updateReleaseNodeModel.release_node_name = this.releaseNodeName;
    },
    // 小组输入过滤
    filterGroup(val, update, abort) {
      update(() => {
        this.groupOptions = this.thirdLevelGroups.filter(v =>
          v.owner_group_name.toLowerCase().includes(val.toLowerCase().trim())
        );
      });
    }
  }
};
</script>

<style scoped></style>
