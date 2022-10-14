<template>
  <div>
    <div>
      <!-- 编辑 -->
      <div v-if="type === 'edit'">
        <f-formitem label="任务名称" required>
          <fdev-input
            ref="jobName"
            style="width:240px"
            v-model="$v.job.name.$model"
            :placeholder="`请输入任务名称`"
            :rules="[() => $v.job.name.required || '请输入任务名称']"
          />
        </f-formitem>
        <f-formitem
          required
          label="构建集群"
          help="用户在流水线编排时，可以为任务设置不同的构建集群。默认使用Fdev提供的K8S集群，若有特殊的需求（如需使用Windows打包机），请联系本组小组管理员创建自有的构建集群"
        >
          <fdev-select
            ref="runner"
            use-input
            style="width:240px"
            v-model="job.runnerClusterId"
            option-label="runnerClusterName"
            option-value="runnerClusterId"
            map-options
            emit-value
            :options="allRunners"
            @filter="runnersOptionsFilter"
          >
            <template v-slot:option="scope">
              <div
                class="select-option"
                @click="selectRunner(scope.opt, scope.index)"
              >
                {{ scope.opt.runnerClusterName }}
              </div>
            </template>
          </fdev-select>
        </f-formitem>
        <div class="row q-mt-md" v-if="isLinuxPlat">
          <f-formitem
            required
            label="工作环境镜像"
            help="提供运行任务时所需环境，比如JDK和Maven。默认使用Fdev提供的流水线基础镜像，若有特殊的环境需求，请联系本组小组管理员创建自有的工作环境镜像。"
          >
            <fdev-select
              ref="image"
              use-input
              style="width:240px"
              v-model="job.image"
              :options="imageOptions"
              bottom-slots
              :display-value="
                `${job.image && job.image.name ? job.image.name : ''}`
              "
              @filter="imageOptionsFilter"
            >
              <template v-slot:option="scope">
                <div
                  @click="selectOption(scope.opt, scope.index)"
                  class="select-option"
                >
                  {{ scope.opt.name }}
                </div>
              </template>
              <template v-slot:hint>
                <span v-if="job.image">
                  {{
                    job.image.desc
                      ? '说明: ' + job.image.desc
                      : '该镜像暂无说明'
                  }}
                </span>
              </template>
            </fdev-select>
          </f-formitem>
        </div>
      </div>
      <!-- 详情 -->
      <div v-else>
        <div class="row">
          <div class="field">任务名称</div>
          <span class="break-word">
            {{ job.name.trim() ? job.name : '无' }}
          </span>
        </div>
        <div class="row q-mt-md">
          <div class="field">构建集群</div>
          <span class="break-word">
            {{ runnerName }}
          </span>
        </div>
        <div class="row q-mt-md" v-if="isLinuxPlat">
          <div class="field">工作环境镜像</div>
          <span class="break-word">
            {{ job.image && job.image.name ? job.image.name : '无' }}
          </span>
        </div>
      </div>
    </div>

    <div>
      <div class="card-header">
        任务步骤
      </div>
      <div class="scroll-thin-y card-body">
        <div
          v-for="(step, index) in job.steps"
          :key="index"
          class="row justify-between step-div"
        >
          <div
            class="row items-center name-left"
            :class="step.warning && !isToolBox ? 'text-red-4' : ''"
          >
            <div class="left" />
            <div
              class="row items-center right"
              :class="step.warning && !isToolBox ? 'text-red-4' : ''"
              @click="openEditDialog(step, index)"
            >
              {{ step.name }}
            </div>
          </div>
          <!-- isFromFixed,固定模式不允许新增和删除步骤 -->
          <div
            v-if="(type === 'edit' && step.deleteFlag) || isToolBox"
            class="row items-center"
          >
            <fdev-btn
              flat
              ficon="delete_o"
              v-if="!isFromFixed"
              @click="deleteStep(step, index)"
            />
          </div>
        </div>
      </div>
      <fdev-btn
        label="添加步骤"
        dialog
        v-if="type === 'edit' && !isFromFixed"
        roundash
        class="q-mt-md"
        @click="addPlugin"
      />
    </div>

    <div class="row justify-end">
      <fdev-btn dialog label="确定" @click="handleConfirm" />
    </div>

    <!-- 进入stepDetail -->
    <StepView
      v-if="stepViewDia"
      v-model="stepViewDia"
      :type="type"
      :dialog-data="stepViewData"
      @receive-step-info="getStepData"
    />
    <!-- 页面点击添加步骤,选完插件后进入PluginEdit页面 -->
    <!-- plugin只有code name信息，新增补全 -->
    <f-dialog
      v-if="pluginEditDia"
      v-model="pluginEditDia"
      right
      title="编辑步骤"
    >
      <PluginEdit
        type="edit"
        :pluginInfo="pluginInfo"
        @receive-step-info="getEditedPluginInfo"
        @close="pluginEditDia = false"
      />
    </f-dialog>

    <select-plugin
      v-if="selectPluginModalOpened"
      v-model="selectPluginModalOpened"
      :platform="platform"
      @getPlugin="getPlugin"
      @close="selectPluginModalOpened = false"
    />
  </div>
</template>

<script>
import { required } from 'vuelidate/lib/validators';
import { queryImageList } from '../../services/method';
import { deepClone, validate, errorNotify } from '@/utils/utils';
import StepView from '../StepManage/StepView.vue';
import PluginEdit from '../StepManage/PluginEdit';
import SelectPlugin from '../StepManage/SelectPlugin';
import { mapActions, mapState } from 'vuex';

export default {
  name: 'JobEdit',
  components: { SelectPlugin, StepView, PluginEdit },
  props: {
    jobInfo: {
      type: Object,
      default: () => ({})
    },
    type: {
      type: String,
      default: 'edit'
    }
  },
  data() {
    return {
      job: {},
      imageOptions: [],
      imageOptionsClone: [],
      stepViewDia: false,
      pluginEditDia: false,
      stepViewData: null,
      selectPluginModalOpened: false,
      isLinuxPlat: false, //是否是linux系统
      platform: '',
      pluginInfo: {},
      currentIndex: -1,
      allRunners: [],
      runnerName: ''
    };
  },
  validations: {
    job: {
      name: {
        required
      }
    }
  },
  computed: {
    ...mapState('configCIForm', ['runnerClusterInfo']),
    // toolBox页面不校验
    isToolBox() {
      return this.$route.name === 'toolbox';
    },
    // Fixed(固定模式)页面不可添加删除步骤
    isFromFixed() {
      return this.$route.params.fromType === 'fixed';
    }
  },
  methods: {
    ...mapActions('configCIForm', ['getRunnerClusterInfoByParam']),
    selectOption(opt, index) {
      this.job.image = deepClone(this.imageOptions[index]);
      this.$refs['image'].menu = false;
      this.$forceUpdate();
    },
    selectRunner(opt) {
      this.isLinuxPlat = opt.platform === 'linux' ? true : false;
      this.platform = opt.platform;
      this.job.runnerClusterId = opt.runnerClusterId;
      this.$refs['runner'].menu = false;
      this.$forceUpdate();
      if (this.isLinuxPlat && this.imageOptions.length === 0) {
        this.getImgOptions();
      }
    },
    //编辑任务确定
    handleConfirm() {
      if (this.$route.name === 'toolbox') {
        if (!this.job.name || !this.job.runnerClusterId || !this.job.image) {
          if (this.job.steps.length === 0) {
            errorNotify('存在必填字段为空、任务步骤为空，请检查并输入或选择');
          } else {
            errorNotify('存在必填字段为空，请检查并输入或选择');
          }
        } else if (this.job.steps.length === 0) {
          errorNotify('任务步骤为空，请选择');
        } else {
          // 均不为空
          this.$emit('receive-job', this.job);
        }
      } else if (this.type === 'edit') {
        this.checkForm();
        if (!this.job.name || !this.job.runnerClusterId || !this.job.image) {
          if (this.job.steps.length === 0) {
            errorNotify('存在必填字段为空、任务步骤为空，请检查并输入或选择');
          } else {
            errorNotify('存在必填字段为空，请检查并输入或选择');
          }
        } else if (this.job.steps.length === 0) {
          errorNotify('任务步骤为空，请选择');
        } else if (!this.job.warning) {
          this.$emit('receive-job', this.job);
        }
      } else {
        this.$emit('close');
      }
    },
    runnersOptionsFilter(val, update, abort) {
      update(() => {
        this.allRunners = this.runnerClusterInfo.filter(
          item =>
            item.runnerClusterName.indexOf(val) > -1 ||
            item.runnerClusterNameEn.indexOf(val) > -1
        );
      });
    },
    openEditDialog(data, index) {
      this.currentIndex = index;
      // 信息补足后修改，stepDetail组件去修改信息
      // data是stepDetail组件中传入的stepInfo值
      this.stepViewData = data;
      this.stepViewDia = true;
    },
    getStepData(info) {
      this.job.steps = this.job.steps.map((item, index) => {
        let stepInfo = {};
        if (this.currentIndex === index) {
          stepInfo = info;
        } else {
          stepInfo = item;
        }
        return stepInfo;
      });
    },
    // 新增插件，获取编辑后的返回stepInfo
    getEditedPluginInfo(info) {
      /* this.job.steps = this.job.steps.map((step, index) => {
        if (index === this.job.steps.length - 1) {
          step = {
            ...info,
            deleteFlag: true
          };
        }
        return step;
      }); */
      this.job.steps.push({
        ...info,
        deleteFlag: true
      });
      // pluginEdit页面新增完了之后,把值push到job.steps中,stepDetail的中从中取
      this.pluginEditDia = false;
    },
    imageOptionsFilter(val, update, abort) {
      update(() => {
        this.imageOptions = this.imageOptionsClone.filter(
          item => item.name.indexOf(val) > -1
        );
      });
    },
    deleteStep(step, index) {
      this.job.steps.splice(index, 1);
    },
    checkForm() {
      const keys = ['jobName'];
      this.$v.job.$touch();
      validate(
        keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      this.job.warning = this.$v.job.$invalid;
    },
    addPlugin() {
      this.selectPluginModalOpened = true;
    },
    // TODO 选择实体的逻辑，数据组装
    getPlugin(plugin) {
      // let pluginInfo = {
      //   pluginInfo: plugin,
      //   name: plugin.pluginName,
      //   deleteFlag: true
      // };
      // this.job.steps.push(pluginInfo);
      this.pluginInfo = plugin;
      this.pluginEditDia = true;
      this.selectPluginModalOpened = false;
    },
    //获取镜像列表
    async getImgOptions() {
      this.imageOptions = deepClone(await queryImageList());
      this.imageOptionsClone = deepClone(this.imageOptions);
      if (!this.job.image || !this.job.image.id) {
        this.job.image = this.imageOptions.find(
          item => item.name === 'centos基础镜像'
        );
      }
    }
  },
  async created() {
    this.job = deepClone(this.jobInfo);
    // 查runner 回显示runner
    await this.getRunnerClusterInfoByParam({ active: true });
    let runner = this.runnerClusterInfo.find(
      item => item.runnerClusterId === this.job.runnerClusterId
    );
    if (runner) {
      this.isLinuxPlat = runner.platform === 'linux' ? true : false;
      this.platform = runner.platform;
    }
    if (this.type !== 'edit') {
      this.runnerName = runner ? runner.runnerClusterName : '无';
    }
    if (this.type === 'edit') {
      if (this.isLinuxPlat) {
        this.getImgOptions();
      }
      this.allRunners = deepClone(this.runnerClusterInfo) || [];
      if (this.job.warning) {
        this.checkForm();
      }
    }
  }
};
</script>
<style scoped lang="stylus">

.break-word
  word-wrap break-word

.card-header
  height 40px
  line-height 40px
  font-size 18px
  margin-top 20px

.select-option
  height 36px
  padding 8px 16px 8px
  font-size 14px
  color #78909C

.select-option:hover
  color #1565C0
  cursor pointer

.card-body
  max-height 364px

.step-div
  height 52px
  border-top 1px solid #ECEFF1

.left
  width 4px
  height 14px
  line-height 14px
  margin 0 16px
  border 1px solid #1565C0
  border-radius 2px

.right
  font-size 14px
  color #1565C0
  cursor pointer

.field
  width 121px
  margin-right 20px
  font-size 14px

/deep/ .q-field__control-container
  height: 40px
</style>
