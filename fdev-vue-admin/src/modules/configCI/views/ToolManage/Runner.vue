<template>
  <div>
    <fdev-table
      :data="runnerTableData"
      :columns="columns"
      noExport
      row-key="name"
      title="runner集群列表"
      :pagination.sync="pagination"
    >
      <template v-slot:top-right
        ><fdev-btn
          class="q-ml-md btn-height"
          normal
          ficon="add"
          label="新增runner集群"
          @click="addRunner"
      /></template>
      <template v-slot:body-cell-active="props">
        <fdev-td>
          <fdev-toggle
            v-model="props.row.active"
            :disable="props.row.canEdit !== '1'"
            @input="toggleRunner(props.row)"
          />
        </fdev-td>
      </template>
      <template v-slot:body-cell-btn="props">
        <fdev-td>
          <template v-if="props.row.canEdit === '1'">
            <fdev-btn
              flat
              label="编辑"
              class="q-mr-sm"
              @click="editRunner(props.row)"
            />
            <fdev-btn flat label="删除" @click="delRunner(props.row)" />
          </template>
          <template v-else
            ><fdev-btn flat label="无权限" disable
          /></template>
        </fdev-td>
      </template>
    </fdev-table>
    <f-dialog v-model="addDialog" title="runner集群设置">
      <form @submit.prevent="handleAddRunner">
        <f-formitem label="runner 集群名称">
          <fdev-input
            v-model="updateModel.runnerClusterName"
            ref="updateModel.runnerClusterName"
            :rules="[
              () =>
                $v.updateModel.runnerClusterName.required ||
                '请输入runner 集群名称'
            ]"
          />
        </f-formitem>
        <f-formitem label="运行平台">
          <fdev-select
            v-model="updateModel.platform"
            :options="platformOptions"
            ref="updateModel.platform"
            @input="changePlatform"
            :rules="[
              () => $v.updateModel.platform.required || '请选择运行平台'
            ]"
          />
        </f-formitem>
        <f-formitem label="配置runner（多选）">
          <fdev-select
            v-model="updateModel.runnerList"
            ref="updateModel.runnerList"
            :options="runnerListOptions"
            option-label="runnerName"
            option-value="runnerId"
            map-options
            use-input
            multiple
            use-chips
            @filter="runListFilter"
            :rules="[val => isHasPlatForm(val) || runnerTip]"
          />
        </f-formitem>
        <div class="float-right q-pa-md">
          <fdev-btn label="取消" outline dialog @click="addDialog = false" />
          <fdev-btn label="确定" dialog class="q-ml-md" type="submit" />
        </div>
      </form>
    </f-dialog>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { validate, successNotify } from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import { createUpdateModel } from '../../utils/constants.js';
import { deepClone } from '@/utils/utils.js';

export default {
  name: 'Runner',
  data() {
    return {
      runnerData: [],
      columns: [
        {
          name: 'runnerClusterName',
          field: 'runnerClusterName',
          label: '集群名称',
          copy: true
        },
        {
          name: 'platform',
          field: 'platform',
          label: '运行平台'
        },
        {
          name: 'runnerNum',
          field: 'runnerNum',
          label: 'runner数量'
        },
        {
          name: 'createTime',
          field: 'createTime',
          label: '创建时间'
        },
        { name: 'active', field: 'active', label: '状态' },
        { name: 'btn', field: 'btn', label: '操作' }
      ],
      pagination: {
        rowsPerPage: 5
      },
      runnerTableData: [],
      addDialog: false,
      updateModel: createUpdateModel(),
      platformOptions: ['linux', 'windows', 'darwin'],
      runnerListOptions: [],
      type: '',
      runnerTip: '',
      allRunnerInfos: []
    };
  },
  validations: {
    updateModel: {
      runnerClusterName: { required },
      platform: { required },
      runnerList: { required }
    }
  },

  computed: {
    ...mapState('global', ['loading']),
    ...mapState('configCIForm', ['runnerClusterInfo', 'allRunnerInfo'])
  },
  watch: {
    async 'updateModel.platform'(val) {
      this.allRunnerInfos = []; //先清空
      if (!val) return;
      await this.getAllRunnerInfo({ platform: val });
      this.allRunnerInfos = deepClone(this.allRunnerInfo);
      this.allRunnerInfos.map(run => {
        run.runnerName = run.name;
      });
    }
  },
  methods: {
    ...mapActions('configCIForm', [
      'getRunnerClusterInfoByParam',
      'addRunnerCluster',
      'getAllRunnerInfo',
      'updateRunnerCluster'
    ]),
    isHasPlatForm(val) {
      if (this.updateModel.platform) {
        if (!val || val.length === 0) {
          this.runnerTip = '请选择配置runner';
          return false;
        }
        return true;
      }
      this.runnerTip = '请先选择运行平台';
      return false;
    },
    runListFilter(val, update) {
      update(() => {
        this.runnerListOptions = this.allRunnerInfos.filter(run => {
          return run.runnerName.toLowerCase().indexOf(val.toLowerCase()) > -1;
        });
      });
    },
    addRunner() {
      this.updateModel = createUpdateModel();
      this.type = 'add';
      this.addDialog = true;
    },
    changePlatform() {
      this.updateModel.runnerList = []; //切换时清除已选择的内容
    },
    async getTableData() {
      await this.getRunnerClusterInfoByParam();
      this.runnerTableData = this.runnerClusterInfo;
      this.pagination.rowsNumber = this.runnerClusterInfo.length;
    },
    async handleAddRunner() {
      this.$v.updateModel.$touch();
      let envvarKeys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('updateModel') > -1;
      });
      validate(envvarKeys.map(key => this.$refs[key]));
      if (this.$v.updateModel.$invalid) {
        return;
      }

      let params = {
        ...this.updateModel
      };
      if (this.type === 'add') {
        await this.addRunnerCluster(params);
        successNotify('新增成功');
      } else {
        await this.updateRunnerCluster(params);
        successNotify('更新成功');
      }
      this.addDialog = false;
      this.getTableData();
    },
    delRunner(data) {
      return this.$q
        .dialog({
          title: '删除提示',
          message: '确定删除该条runner吗？',
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.updateRunnerCluster({
            runnerClusterId: data.runnerClusterId,
            status: '0'
          });
          successNotify('删除成功');
          this.getTableData();
        });
    },
    editRunner(data) {
      this.updateModel = { ...data };
      this.type = 'edit';
      this.addDialog = true;
    },
    async toggleRunner(data) {
      await this.updateRunnerCluster({
        runnerClusterId: data.runnerClusterId,
        active: data.active
      });
      successNotify('更新状态成功');
      this.getTableData();
    }
  },
  created() {
    this.getTableData();
  }
};
</script>

<style lang="stylus" scoped>
.align-right
  position: absolute;
  right: 1%;
</style>
