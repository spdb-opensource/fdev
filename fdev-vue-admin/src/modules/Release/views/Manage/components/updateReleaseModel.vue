<template>
  <div>
    <f-dialog right v-model="updateReleaseModalOpened" title="编辑投产窗口">
      <f-formitem label="投产日期" dense diaS>
        {{ updateReleaseNodeModel.releaseDate }}
      </f-formitem>
      <f-formitem label="投产窗口名称" dense diaS>
        {{ updateReleaseNodeModel.releaseNodeName }}
      </f-formitem>
      <f-formitem label="所属小组" dense diaS>
        {{ updateReleaseNodeModel.groupFullName }}
      </f-formitem>

      <f-formitem label="科技负责人" diaS>
        <fdev-select
          input-debounce="0"
          use-input
          fill-input
          ref="updateReleaseNodeModel.releaseSpdbManager"
          v-model="$v.updateReleaseNodeModel.releaseSpdbManager.$model"
          :options="userOptionsFilter('行内项目负责人')"
          @filter="userFilterChoice"
          :rules="[
            () =>
              !$v.updateReleaseNodeModel.releaseSpdbManager.$error ||
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

      <f-formitem label="投产负责人" diaS>
        <fdev-select
          input-debounce="0"
          use-input
          fill-input
          ref="updateReleaseNodeModel.releaseManager"
          v-model="$v.updateReleaseNodeModel.releaseManager.$model"
          :options="releasUser"
          @filter="releaseUserFilterChoice"
          :rules="[
            () =>
              !$v.updateReleaseNodeModel.releaseManager.$error ||
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
      <f-formitem label="UAT测试环境" diaS>
        <fdev-select
          input-debounce="0"
          ref="updateReleaseNodeModel.uatEnvName"
          v-model="$v.updateReleaseNodeModel.uatEnvName.$model"
          :options="envType"
          option-value="name_en"
          option-label="name_en"
          emit-value
          map-options
          :rules="[
            () =>
              !$v.updateReleaseNodeModel.uatEnvName.$error ||
              '请选择UAT测试环境'
          ]"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn @click="handleUpdateRelease" dialog label="提交" />
      </template>
    </f-dialog>
  </div>
</template>
<script>
import { formatUser } from '@/modules/User/utils/model';
import { mapState } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import { queryEnv, update } from '@/services/release';
import { query, queryGroupPeople } from '@/services/user';
import { formatReleaseDetail } from '../../../utils/model';
import {
  formatOption,
  resolveResponseError,
  validate,
  successNotify,
  getGroupFullName
} from '@/utils/utils';

export default {
  data() {
    return {
      updateReleaseNodeModel: {},
      updateReleaseModalOpened: this.showUpdateModal,
      releasUser: [],
      envType: [],
      userFilterList: [],
      releaseOptions: [],
      users: [],
      userOptions: []
    };
  },
  watch: {
    updateReleaseModalOpened(val) {
      this.$emit('updateReleaseNode', 'hideModal');
      this.updateReleaseModalOpened = true;
    }
  },
  props: {
    updateData: {
      type: Object,
      default: () => {}
    },
    showUpdateModal: {
      type: Boolean,
      default: () => true
    }
  },
  computed: {
    ...mapState('user', {
      currentUser: 'currentUser',
      userList: 'list'
    }),
    ...mapState('userForm', ['groups'])
  },
  validations: {
    updateReleaseNodeModel: {
      releaseDate: {
        required
      },
      releaseManager: {
        required
      },
      releaseSpdbManager: {
        required
      },
      uatEnvName: {
        required
      }
    }
  },
  methods: {
    /* 提交修改 */
    async handleUpdateRelease() {
      this.$v.updateReleaseNodeModel.$touch();
      let jobKeys = Object.keys(this.$refs).filter(
        key => key.indexOf('updateReleaseNodeModel') > -1
      );
      validate(jobKeys.map(key => this.$refs[key]));
      if (this.$v.updateReleaseNodeModel.$invalid) {
        return;
      }
      let params = {
        release_node_name: this.updateReleaseNodeModel.releaseNodeName,
        release_manager: this.updateReleaseNodeModel.releaseManager
          .user_name_en,
        release_spdb_manager: this.updateReleaseNodeModel.releaseSpdbManager
          .user_name_en,
        uat_env_name: this.updateReleaseNodeModel.uatEnvName
      };
      let res = await resolveResponseError(() => update(params));
      successNotify('修改成功');
      this.$emit('updateReleaseNode', res);
    },
    async userFilterChoice(val, update, abort) {
      update(() => {
        this.users = this.userOptions.filter(
          user => user.name.indexOf(val) > -1
        );
      });
    },
    async releaseUserFilterChoice(val, update, abort) {
      update(() => {
        this.releasUser = this.releaseOptions.filter(
          user => user.name.indexOf(val) > -1
        );
      });
    },
    userOptionsFilter(...param) {
      let myuser = this.users.filter(item => {
        let flag = false;
        let role_labels = [];
        item.role.forEach(ele => {
          role_labels.push(ele.name);
        });
        param.forEach(r => {
          if (role_labels.includes(r)) {
            flag = true;
          }
        });
        return flag;
      });
      return myuser;
    }
  },
  async created() {
    this.updateReleaseNodeModel = formatReleaseDetail(this.$props.updateData);
    let users = await query();
    this.users = users.map(user => formatOption(formatUser(user), 'name'));
    this.userOptions = this.users.slice(0);

    let releasUser = await resolveResponseError(() =>
      queryGroupPeople({
        id: this.currentUser.group.id
      })
    );
    this.releasUser = releasUser.map(user =>
      formatOption(formatUser(user), 'name')
    );
    this.releaseOptions = this.releasUser.slice(0);

    let env = await resolveResponseError(() => queryEnv({ labels: ['uat'] }));
    this.envType = env;
    this.updateReleaseNodeModel.groupFullName = getGroupFullName(
      this.groups,
      this.updateReleaseNodeModel.owner_groupId
    );
  }
};
</script>
<style scoped></style>
