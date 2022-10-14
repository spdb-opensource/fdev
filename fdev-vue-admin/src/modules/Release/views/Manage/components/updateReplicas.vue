<template>
  <div>
    <f-dialog
      :value="value"
      @input="$emit('input', $event)"
      right
      f-sc
      title="确认副本数"
    >
      <div v-if="Object.keys(replicasObj).length > 0">
        <!-- CAAS部署平台 -->
        <div v-if="deployPlatform.includes('CAAS')">
          <div class="bg-blue-1 full-width title-div q-mb-md row">
            <div class="title-icon row justify-center items-center">
              <f-icon name="version_s_f" class="text-blue-8" />
            </div>
            <div class="row justify-center items-center">
              CAAS部署平台
            </div>
          </div>
          <f-formitem
            v-for="(env, title) in replicasObj"
            :key="title"
            :label="`${title.split('/').reverse()[0]}副本数`"
            diaS
            required
          >
            <fdev-input
              v-model.number="env.replicas"
              :ref="`replicas.${title.split('/').reverse()[0]}`"
              type="number"
              :rules="[
                val => !!val || val === 0 || '请输入副本数',
                val =>
                  /^[0-9]$|^[1-9][0-9]$/g.test(Number(val)) || '只能输入0-99'
              ]"
            />
          </f-formitem>
        </div>

        <!-- SCC部署平台 -->
        <div v-if="deployPlatform.includes('SCC')">
          <div class="bg-blue-1 full-width title-div q-mb-md row">
            <div class="title-icon row justify-center items-center">
              <f-icon name="version_s_f" class="text-blue-8" />
            </div>
            <div class="row justify-center items-center">
              SCC部署平台（SCC平台副本数如需调整，请通过yaml方式变更）
            </div>
          </div>
          <f-formitem
            v-for="(env, title) in replicasObj"
            :key="title"
            :label="`${title.split('/').reverse()[0]}副本数`"
            diaS
          >
            <fdev-input
              v-model.number="env.scc_replicas"
              :ref="`sccReplicas.${title.split('/').reverse()[0]}`"
              type="number"
              disable
              :rules="[
                val => !!val || val === 0 || '请输入副本数',
                val =>
                  /^[0-9]$|^[1-9][0-9]$/g.test(Number(val)) || '只能输入0-99'
              ]"
            />
          </f-formitem>
        </div>
      </div>

      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          @click="addApp"
          label="确认"
          :loading="
            globalLoading['releaseForm/addChangeApplication'] ||
              globalLoading['releaseForm/updateReplicasnu']
          "
        />
      </template>
    </f-dialog>
  </div>
</template>

<script>
import { deepClone } from '@/utils/utils';
import { mapActions, mapState } from 'vuex';
export default {
  name: 'UpdateReplicas',
  props: {
    value: {
      type: Boolean,
      default: false
    },
    replicasObj: {
      type: Object,
      required: true
    },
    deployPlatform: {
      type: Array
    },
    // 弹窗来源
    source: String,
    applicationId: String
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    })
  },
  methods: {
    ...mapActions('releaseForm', ['updateReplicasnu']),

    // 点击确定按钮，添加变更应用
    addApp() {
      // 校验表单
      let keys = [];
      const replicasKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.startsWith('replicas');
      });
      const sccReplicasKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.startsWith('sccReplicas');
      });
      // 收集参与校验的表单（CAAS或SCC平台不显示的不参与校验）
      if (this.deployPlatform.includes('CAAS')) {
        keys = keys.concat(replicasKeys);
      }
      if (this.deployPlatform.includes('SCC')) {
        keys = keys.concat(sccReplicasKeys);
      }
      Promise.all(
        keys.map(ele => this.$refs[ele][0].validate() || Promise.reject(ele))
      ).then(
        async res => {
          let cloneReplicasObj = deepClone(this.replicasObj);
          for (let item in cloneReplicasObj) {
            // CAAS部署平台下的表单不显示，将其每个环境的副本数置空。
            if (!this.deployPlatform.includes('CAAS')) {
              cloneReplicasObj[item].replicas = '';
            }

            // SCC部署平台下的表单不显示，将其每个环境的副本数置空。
            if (!this.deployPlatform.includes('SCC')) {
              cloneReplicasObj[item].scc_replicas = '';
            }
          }
          if (this.source === 'restartBtn') {
            // 当是点击“重启副本数”开启的弹窗，发接口修改副本数
            await this.updateReplicasnu({
              prod_id: this.$route.params.id,
              application_id: this.applicationId,
              change: cloneReplicasObj
            });
            this.$emit('confirm');
          } else {
            this.$emit('confirm', cloneReplicasObj);
          }
        },
        rej => {
          this.$refs[rej][0].focus();
        }
      );
    }
  }
};
</script>

<style lang="stylus" scoped>
.title-div
  .title-icon
    width 32px
    height 54px
    margin-left 8px
  .title-text
    height 54px
    margin-left 16px
</style>
