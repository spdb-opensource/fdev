<template>
  <Loading :visible="loading['releaseForm/queryBatchInfoByTaskId']">
    <f-dialog
      right
      :value="value"
      @input="$emit('input', $event)"
      :title="title"
    >
      <f-formitem diaS label="应用名称">
        <fdev-select
          disable
          use-input
          input-debounce="0"
          ref="changeBatchDialogModel.application_id"
          v-model="$v.changeBatchDialogModel.application_id.$model"
          :options="optionsList.optionsData"
          map-options
          emit-value
          :option-label="optionsList.name"
          option-value="id"
          :loading="loading[optionsList.loading]"
          :rules="[
            () =>
              $v.changeBatchDialogModel.application_id.required ||
              '请选择应用名称'
          ]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.name_en">{{
                  scope.opt.name_en
                }}</fdev-item-label>
                <fdev-item-label :title="scope.opt.name_zh">{{
                  scope.opt.name_zh
                }}</fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <f-formitem diaS label="投产窗口">
        <fdev-input
          disable
          ref="changeBatchDialogModel.release_node_name"
          v-model="$v.changeBatchDialogModel.release_node_name.$model"
          :rules="[
            () =>
              $v.changeBatchDialogModel.release_node_name.required ||
              '请输入投产窗口'
          ]"
        />
      </f-formitem>
      <f-formitem diaS label="选择批次">
        <fdev-select
          use-input
          input-debounce="0"
          ref="changeBatchDialogModel.batch_id"
          v-model="$v.changeBatchDialogModel.batch_id.$model"
          :options="batchOptions"
          map-options
          emit-value
          option-label="label"
          option-value="value"
          :rules="[
            () => $v.changeBatchDialogModel.batch_id.required || '请选择批次'
          ]"
        />
      </f-formitem>
      <f-formitem diaS label="已绑定应用" v-if="selectedApp.length > 0">
        <fdev-chip
          class="inline-block chip-line-hight"
          square
          dense
          outline
          color="teal"
          text-color="white"
          v-for="app in selectedApp"
          :key="app.application_id"
        >
          {{ app.application_name_en }}
        </fdev-chip>
      </f-formitem>

      <f-formitem diaS label="已绑定应用" v-else>
        <span class="text-grey-7">无</span>
      </f-formitem>

      <f-formitem
        diaS
        label="可绑应用"
        v-if="
          appBatchInfo.could_bind_app && appBatchInfo.could_bind_app.length > 0
        "
      >
        <fdev-tree
          class="release-tree-node"
          :nodes="treeData"
          default-expand-all
          node-key="application_id"
          tick-strategy="leaf"
          label-key="application_name_en"
          ref="newChangeModel.applications"
          :ticked.sync="$v.changeBatchDialogModel.applications.$model"
        />
      </f-formitem>

      <f-formitem diaS label="可绑应用" v-else>
        <span class="text-grey-7">无</span>
      </f-formitem>

      <f-formitem label="修改原因" v-if="isEdit">
        <fdev-input
          type="textarea"
          ref="changeBatchDialogModel.modify_reason"
          v-model="$v.changeBatchDialogModel.modify_reason.$model"
          :rules="[
            () =>
              $v.changeBatchDialogModel.modify_reason.required ||
              '请输入修改原因'
          ]"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          label="确定"
          :loading="loading['releaseForm/addBatch']"
          @click="validateForm"
        />
      </template>
    </f-dialog>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import { validate } from '@/utils/utils';
import { batchArr } from '../utils/model';
import { changeBatchDialogModel, batchOptions } from '../utils/model';
export default {
  components: { Loading },
  data() {
    return {
      changeBatchDialogModel: changeBatchDialogModel(),
      batchOptions: batchOptions,
      optionsList: {}
    };
  },
  props: {
    title: String,
    value: Boolean,
    data: Object,
    isEdit: Boolean,
    type: String
  },
  validations: {
    changeBatchDialogModel: {
      application_id: {
        required
      },
      batch_id: {
        required
      },
      applications: {},
      modify_reason: {
        required(val) {
          if (this.isEdit) {
            return !!val.trim();
          }
          return true;
        }
      },
      release_node_name: {
        required
      }
    }
  },
  computed: {
    ...mapState('appForm', ['vueAppData']),
    ...mapState('componentForm', [
      'allComponentList',
      'allArchetypeList',
      'imageManageList'
    ]),
    ...mapState('releaseForm', ['appBatchInfo']),
    ...mapState('global', ['loading']),
    treeData() {
      const { could_bind_app } = this.appBatchInfo;
      if (!could_bind_app || could_bind_app.length === 0) {
        return [];
      }

      const arr = batchArr().map(item => {
        return {
          application_id: item.batch,
          application_name_en: item.type,
          disabled: true,
          ...item
        };
      });

      const data = could_bind_app.reduce((sum, i) => {
        const data = {
          ...i,
          label: i.app_name_en
        };
        const batch = i.batch_id ? Number(i.batch_id) : 4;
        sum[batch - 1].children.push(data);
        sum[batch - 1].disabled = false;
        return sum;
      }, arr);

      return [
        {
          application_name_en: '可绑应用',
          children: data
        }
      ];
    },
    selectedApp() {
      const { applications } = this.changeBatchDialogModel;
      if (applications.length === 0) return [];

      const { could_bind_app } = this.appBatchInfo;
      if (!could_bind_app) return this.appBatchInfo.bind_app;
      return could_bind_app.filter(app => {
        return applications.includes(app.application_id);
      });
    }
  },
  watch: {
    value(val, old) {
      if (val) {
        this.handleDialogOpen();
      }
    }
  },
  methods: {
    ...mapActions('appForm', ['queryApps']),
    ...mapActions('componentForm', [
      'queryAllComponents',
      'queryAllArchetypeTypes',
      'queryBaseImage'
    ]),
    ...mapActions('releaseForm', ['queryBatchInfoByTaskId']),
    async handleDialogOpen() {
      if (this.data.type === '4') {
        // 请求组件列表
        await this.queryAllComponents();
        this.optionsList = {
          optionsData: this.allComponentList,
          name: 'name_en',
          loading: 'componentForm/queryAllComponents'
        };
      } else if (this.data.type === '5') {
        // 请求骨架列表
        await this.queryAllArchetypeTypes();
        this.optionsList = {
          optionsData: this.allArchetypeList,
          name: 'name_en',
          loading: 'componentForm/queryAllArchetypeTypes'
        };
      } else if (this.data.type === '6') {
        // 请求镜像列表
        await this.queryBaseImage();
        this.optionsList = {
          optionsData: this.imageManageList,
          name: 'name',
          loading: 'componentForm/queryBaseImage'
        };
      } else {
        // 应用列表
        await this.queryApps();
        this.optionsList = {
          optionsData: this.vueAppData,
          name: 'name_en',
          loading: 'appForm/queryApps'
        };
      }
      this.changeBatchDialogModel = {
        ...changeBatchDialogModel(),
        ...this.data
      };
      await this.queryBatchInfoByTaskId({
        release_node_name: this.data.release_node_name,
        task_id: this.$route.params.id,
        application_id: this.data.application_id
      });
      const { bind_app, modify_reason } = this.appBatchInfo;
      this.changeBatchDialogModel.applications = bind_app.map(app => {
        return app.application_id;
      });
      this.changeBatchDialogModel.modify_reason = modify_reason;
    },
    validateForm() {
      this.$v.changeBatchDialogModel.$touch();

      let keys = Object.keys(this.$refs).filter(key =>
        key.includes('changeBatchDialogModel')
      );
      validate(
        keys.map(key => {
          return this.$refs[key];
        })
      );

      if (this.$v.changeBatchDialogModel.$invalid) {
        return;
      }

      this.handleDialog();
    },
    handleDialog() {
      const {
        application_id,
        batch_id,
        applications,
        modify_reason,
        release_node_name
      } = this.changeBatchDialogModel;
      this.$emit('submit', {
        application_id,
        batch_id,
        applications,
        modify_reason,
        release_node_name
      });
    }
  }
};
</script>

<style lang="stylus" scoped>
.dialog
  width 560px
.chip-line-hight
  line-height 14px
</style>
<style>
.release-tree-node .q-checkbox__inner {
  font-size: 30px;
}
</style>
