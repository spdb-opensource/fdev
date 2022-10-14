<template>
  <!-- 模板配置 -->
  <fdev-dialog
    :value="modelSettingOpened"
    @input="$emit('input', $event)"
    persistent
    :maximized="maximizedToggle"
  >
    <fdev-layout view="Lhh lpR fff" container class="bg-white">
      <fdev-header class="q-pt-sm q-pb-sm bg-cyan-6">
        <fdev-bar class="bg-cyan-6 text-white">
          <fdev-btn-group>
            <fdev-btn
              class="q-btn--dialog-width"
              v-if="hasPermissions"
              icon="build"
              color="cyan-6"
              :label="`${toggleTool ? '隐藏' : '打开'}模板工具`"
              size="md"
              @click="toggleTool = !toggleTool"
            />
            <fdev-btn
              class="q-btn--dialog-width"
              v-if="hasPermissions"
              icon="save"
              color="cyan-6"
              label="保存配置模板"
              size="md"
              @click="handleSubmitModel"
            />
            <fdev-btn
              class="q-btn--dialog-width"
              icon="priority_high"
              color="cyan-6"
              label="优先生效配置参数"
              size="md"
              @click="handleExtreConfigDialogOpen"
            >
              <div>
                <f-icon name="help" style="font-size: 12px"> </f-icon>
                <fdev-tooltip
                  anchor="top middle"
                  self="center middle"
                  :offest="[0, 0]"
                >
                  只是临时测试使用，长期使用需要配置实体参数。
                </fdev-tooltip>
              </div>
            </fdev-btn>
            <fdev-btn
              class="q-btn--dialog-width"
              icon="autorenew"
              color="cyan-6"
              label="预览各环境配置文件"
              size="md"
              @click="getPreviewFile"
            />
            <!-- 保存到配置中心按钮 -->
            <div>
              <fdev-btn
                class="q-btn--dialog-width"
                icon="save"
                color="cyan-6"
                size="md"
                @click="saveConfig"
                :disable="!hasEditPermissions || !isDefaultDevEnv"
                label="上传到开发配置中心"
              >
              </fdev-btn>
              <fdev-tooltip
                anchor="bottom middle"
                self="top middle"
                :offset="[10, 10]"
                v-if="hasEditPermissions && isDefaultDevEnv"
              >
                开发环境实体映射值与SIT环境相等，开发阶段可使用常量值调试
              </fdev-tooltip>
              <fdev-tooltip
                anchor="bottom middle"
                self="top middle"
                :offset="[10, 10]"
                v-else
              >
                该功能只在预览选择开发环境后可用，开发环境实体映射值与SIT环境相等，开发阶段可使用常量值调试
              </fdev-tooltip>
            </div>
          </fdev-btn-group>
          <fdev-space />
          <fdev-btn
            class="q-btn--dialog-width"
            flat
            icon="close"
            @click="closeDialog"
          />
        </fdev-bar>
      </fdev-header>
      <div class="property-editor">
        <div class="row editor-title">
          <div class="col-3" v-if="toggleTool">
            <span class="q-pl-lg">模板工具</span>
          </div>
          <div class="col">
            编辑外部配置模板
            <span>
              <fdev-tooltip
                anchor="top middle"
                self="center middle"
                :offest="[0, 0]"
              >
                <span>配置项左侧为应用key,右侧为fdev实体key</span>
              </fdev-tooltip>
              <f-icon name="help" />
            </span>
          </div>
          <div class="col row">
            <div class="col-4">预览各环境配置文件</div>
            <div class="row input form-item">
              <span class="form-label">环境</span>
              <fdev-select
                use-input
                placeholder="环境"
                ref="envModel.env"
                class="input select-width inline-block"
                v-model="envModel.env"
                @filter="envFilter"
                :options="filterEnvList"
                option-label="name_en"
                option-value="name_en"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.name_en">
                        {{ scope.opt.name_en }}
                      </fdev-item-label>
                      <fdev-item-label caption :title="scope.opt.name_cn">
                        {{ scope.opt.name_cn }}
                      </fdev-item-label>
                    </fdev-item-section>
                    <fdev-item-section
                      side
                      v-if="
                        scope.opt.labels.includes('biz') ||
                          scope.opt.labels.includes('dmz')
                      "
                    >
                      <fdev-chip
                        flat
                        square
                        class="text-white"
                        :color="
                          scope.opt.labels.includes('biz')
                            ? 'green-5'
                            : 'orange-5'
                        "
                      >
                        {{ scope.opt.labels.includes('biz') ? '业务' : '网银' }}
                      </fdev-chip>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </div>
          </div>
        </div>
        <div class="row editor-warpper">
          <div class="col-3" v-if="toggleTool">
            <form class="form">
              <div class="job-top-select">
                <fdev-select
                  placeholder="环境实体名称"
                  use-input
                  clearable
                  ref="envModel.term"
                  v-model="envModel.term"
                  @filter="filterFn"
                  :options="nameEnList"
                  :option-value="opt => opt"
                  option-label="name_en"
                >
                  <template v-slot:option="scope">
                    <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                      <fdev-item-section>
                        <fdev-item-label :title="scope.opt.name_en">
                          {{ scope.opt.name_en }}
                        </fdev-item-label>
                        <fdev-item-label :title="scope.opt.name_cn" caption>
                          {{ scope.opt.name_cn }}
                        </fdev-item-label>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </div>
              <!-- 导入原始配置文件 -->
              <div class="row justify-center q-pt-md">
                <fdev-btn-group>
                  <fdev-btn
                    style="height:auto"
                    class="q-btn--dialog-width"
                    icon="call_missed_outgoing"
                    color="primary"
                    label="选择该实体配置模板"
                    size="md"
                    v-if="hasPermissions"
                    @click="appendModelHtml"
                  />
                  <fdev-btn
                    style="height:auto"
                    class="q-btn--dialog-width"
                    icon="cloud_upload"
                    color="primary"
                    label="导入外部配置文档"
                    size="md"
                    v-if="hasPermissions"
                    @click="showUploader = !showUploader"
                  />
                </fdev-btn-group>
                <fdev-uploader
                  label="限properties文件"
                  field-name="file"
                  :multiple="false"
                  ref="upload"
                  v-show="showUploader"
                  @added="startRead"
                  accept=".properties"
                  class="uploader"
                  hide-upload-btn
                />
              </div>
              <!-- 实体 -->
              <div class="row">
                <pre class="scroll-x">
                    <code class="text-left block" v-html="modelHtml"/>
                  </pre>
              </div>
            </form>
          </div>
          <div class="col" :style="{ width: toggleTool ? '74.5%' : '100%' }">
            <PropertyEditor
              v-model="editor"
              ref="propertyEdit"
              :properties="modelProperties"
              :disable="permission"
              @noPermission="changeStatus"
              @error="handleError"
            />
          </div>
        </div>
      </div>
      <!-- 保存失败 -->
      <f-dialog v-model="saveResultDialogOpened" title="保存失败">
        <div v-for="(arr, title) in result" :key="title">
          <fdev-chip
            icon="warning"
            square
            color="red"
            text-color="white"
            :label="title"
          />
          <p class="result-wrapper" v-for="res in arr" :key="res">
            <f-icon name="alert_t_f" style="color:red" />{{ res }}
          </p>
        </div>
      </f-dialog>
      <!-- 环境配置弹框 -->
      <ExtreConfigParam
        :extreConfigDialogOpened.sync="extreConfigDialogOpened"
        :extraConfig="extraConfigParam[0]"
        @listenExtreConfigEvent="changeExtreConfigEvent"
        :noWrite="noWrite"
        :project_id="job.project_id"
        :branch="job.feature_branch"
      />
    </fdev-layout>
  </fdev-dialog>
</template>
<script>
import PropertyEditor from '@/components/PropertyEditor';
import LocalStorage from '#/plugins/LocalStorage';
import { mapState, mapActions, mapGetters } from 'vuex';
import {
  getIdsFormList,
  errorNotify,
  successNotify,
  deepClone
} from '@/utils/utils';
import { createEnvModel } from '@/modules/Job/utils/constants';
import ExtreConfigParam from '@/components/ExtraConfigParam';
export default {
  name: 'envSetting',
  props: ['job'],
  data() {
    return {
      PropertyEditor: false,
      hasEditPermissions: false,
      isOnceComing: true,
      editor: '',
      managerIds: [],
      envModel: createEnvModel(),
      filterEnvList: [],
      typeList: [],
      firstCategories: [],
      secondCategories: [],
      cloneSecCategories: [],
      filterModelList: [],
      orignalModelProperties: [],
      modelProperties: [],
      nameEnList: [],
      maximizedToggle: true,
      toggleTool: true,
      saveResultDialogOpened: false,
      modelSettingOpened: false,
      extreConfigDialogOpened: false,
      showUploader: false,
      hasPermissions: false,
      noWrite: true,
      modelHtml: '',
      permission: false
    };
  },
  watch: {
    modelSettingOpened(val) {
      this.permission = !val ? false : this.permission;
    },
    'envModel.first_category'(val) {
      this.modelConstant.category.forEach(cate => {
        for (let key in cate) {
          if (key === val) {
            this.secondCategories = cate[key];
          }
        }
      });
    },
    'envModel.term'(val) {
      if (val === null) {
        val = '';
        this.modelHtml = '';
      }
      if (typeof val === 'object') {
        this.modelHtml = '';
        let envNameCn = val.name_cn;
        let envNameEn = val.name_en;
        let envKeys = val.env_key;
        let modelHtmlStart = '## ' + envNameCn + '\n';
        envKeys.forEach(key => {
          let modelDesc = key.name_cn;
          // let modelKey = `${envNameEn}.${key.name_en}`;
          let modelValue = '$&lt;' + `${envNameEn}.${key.name_en}` + '&gt;';
          this.modelHtml += '# ' + envNameCn + '.' + modelDesc + '\n';
          this.modelHtml += 'appKey' + '=' + modelValue + '\n';
        });
        let modelHtmlEnd = '';
        this.modelHtml = modelHtmlStart + this.modelHtml + modelHtmlEnd;
      }
    },
    'envModel.env'(val) {
      this.getPreviewFile();
    }
  },
  components: { PropertyEditor, ExtreConfigParam },
  computed: {
    ...mapState('appForm', ['appData']),
    ...mapGetters('user', ['isKaDianManager']),
    ...mapState('user', ['currentUser']),
    ...mapState('jobForm', [
      'modelConstant',
      'pirvateModelList',
      'configTemplate',
      'envListByAppId',
      'extraConfigParam',
      'previewFile',
      'configProperties',
      'result'
    ]),
    // 计算值控制配置按钮的显示
    isDefaultDevEnv() {
      if (!this.envModel.env) {
        return false;
      }
      const { labels } = this.envModel.env;
      if (labels) {
        return labels.indexOf('dev') > -1 && labels.indexOf('default') > -1;
      } else {
        return false;
      }
    }
  },
  methods: {
    ...mapActions('appForm', ['queryApplication']),
    ...mapActions('jobForm', [
      'getModelConstant',
      'queryExcludePirvateModelList',
      'queryConfigTemplate',
      'saveConfigTemplate',
      'queryExtraConfigParam',
      'previewConfigFile',
      'saveDevConfigProperties',
      'getEnvListByAppId'
    ]),
    changeStatus() {
      this.permission = !this.hasEditPermissions;
    },
    async handleModelSettingOpen() {
      this.isOnceComing = true;
      this.modelSettingOpened = true;
      //获取有权限的用户id
      await this.queryApplication({ id: this.job.project_id });
      this.managerIds = getIdsFormList([
        this.job.spdb_master,
        this.job.master,
        this.appData[0].dev_managers,
        this.appData[0].spdb_managers
      ]);
      let managerEditIds = getIdsFormList([
        this.job.spdb_master,
        this.job.master,
        this.job.developer,
        this.appData[0].dev_managers,
        this.appData[0].spdb_managers
      ]);
      if (
        managerEditIds.indexOf(this.currentUser.id) > -1 ||
        this.isKaDianManager
      ) {
        this.hasEditPermissions = true;
      }
      if (
        this.managerIds.indexOf(this.currentUser.id) > -1 ||
        this.isKaDianManager
      ) {
        this.hasPermissions = true;
      }
      // 查询环境列表
      //await this.getEnvList();
      // 根据应用查询环境列表
      await this.getEnvListByAppId({ app_id: this.job.project_id });
      this.filterEnvList = deepClone(this.envListByAppId);
      //this.filterEnvList = deepClone(this.envList);
      // 查询常量
      await this.getModelConstant();
      this.firstCategories = [];
      this.secondCategories = [];
      this.typeList = this.modelConstant.type;
      let categories = this.modelConstant.category
        ? this.modelConstant.category
        : [];
      categories.forEach(item => {
        for (let key in item) {
          this.firstCategories.push(key);
          this.secondCategories = this.secondCategories.concat(item[key]);
        }
      });
      this.cloneSecCategories = deepClone(this.secondCategories);
      // 查询自己应用的私有实体用作关联项
      await this.queryExcludePirvateModelList({
        name_en: this.job.project_name
      });
      // 过滤掉所有的含ci的数据
      this.filterModelList = this.pirvateModelList.filter(item => {
        return item.name_en.split('_')[0].indexOf('ci') < 0;
      });
      let list = this.filterModelList; // 外部编辑器过滤掉ci环境实体
      this.filterModelList.sort(this.compare('name_en'));
      let modelProperties = [];
      list.forEach(model => {
        let modelNameEn = model.name_en;
        let modelNameCn = model.name_cn;
        let envKeys = model.env_key;
        envKeys.forEach(key => {
          let modelKey = `${modelNameEn}.${key.name_en}`;
          let modelProNameCn = key.name_cn;
          modelProperties.push({
            isVarivale: true,
            name: modelKey,
            value: modelKey,
            label: modelKey,
            desc: modelNameCn + '.' + modelProNameCn
          });
        });
      });
      // 查询已有配置模板
      let params = {
        project_id: this.job.project_id,
        feature_branch: this.job.feature_branch
      };
      // 已有参数配置模板Property
      let configTemplateProperties = [];
      try {
        await this.queryConfigTemplate(params);
        this.editor = this.configTemplate;
        this.isOnceComing = false;
      } catch (e) {
        throw e;
      } finally {
        this.orignalModelProperties = this.modelProperties = modelProperties.concat(
          configTemplateProperties
        );
        this.$refs.propertyEdit.runCmd('selectAll');
        this.$refs.propertyEdit.runCmd('insertText', this.editor, 'initEditor');
      }
      this.isOnceComing = false;
    },
    //预览配置文件
    async getPreviewFile() {
      if (!this.isOnceComing) {
        if (
          this.editor.trim() &&
          this.envModel.env &&
          this.envModel.env.name_en
        ) {
          let params = {
            type: '0',
            env_name: this.envModel.env.name_en,
            content: this.editor,
            project_id: this.job.project_id
          };
          await this.previewConfigFile(params);
          this.modelProperties = deepClone(this.orignalModelProperties);

          const { outSideParam, modelParam } = this.previewFile;
          const modelParamKeys = Object.keys(modelParam);
          const outSideParamKeys = Object.keys(outSideParam);

          modelParamKeys.forEach(key => {
            const index = this.modelProperties.findIndex(
              item => item.label === key
            );
            if (index > -1) {
              this.modelProperties[index].value = modelParam[key];
            } else {
              this.modelProperties.push({
                label: key,
                name: key,
                isVarivale: true,
                value: modelParam[key]
              });
            }
          });

          outSideParamKeys.forEach(key => {
            this.modelProperties.push({
              label: key,
              name: key,
              value: outSideParam[key]
            });
          });
        } else {
          errorNotify('外部配置模板或者环境不能为空');
        }
      }
    },
    // 提交实体模板
    async handleSubmitModel() {
      return this.$q
        .dialog({
          title: `保存配置模板`,
          message: `确定保存此配置模板吗？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.submitModel();
        });
    },
    async submitModel() {
      const content = this.editor.replace(/\n{2,}/g, $1 => {
        const nArr = $1.split('\n');
        const res = nArr.slice(0, nArr.length / 2 + 1).join('\n');
        return res;
      });
      let params = {
        project_id: this.job.project_id,
        feature_branch: this.job.feature_branch,
        content: content
      };

      await this.saveConfigTemplate(params);
      if (this.result) {
        this.saveResultDialogOpened = true;
        return;
      }
      successNotify('保存模板成功');
      this.modelSettingOpened = false;
    },
    closeDialog() {
      let initEditor = LocalStorage.getItem('initEditor');
      if (this.editor !== initEditor) {
        return this.$q
          .dialog({
            message: `配置模板有修改但未保存，是否继续关闭？`,
            ok: '取消',
            cancel: '确定'
          })
          .onCancel(() => {
            this.modelSettingOpened = false;
          });
      } else {
        this.modelSettingOpened = false;
      }
    },
    // 保存到开发环境配置中心
    async saveConfig() {
      if (this.editor.trim() && this.envModel.env.name_en) {
        let params = {
          labels: this.envModel.env.labels,
          content: this.editor,
          project_id: this.job.project_id,
          feature_branch: this.job.feature_branch
        };
        await this.saveDevConfigProperties(params);
        successNotify('配置文件保存成功,上传路径:' + this.configProperties);
      } else {
        errorNotify('配置文件保存失败');
      }
    },
    //筛选环境列表
    envFilter(val, update) {
      update(() => {
        this.filterEnvList = this.envListByAppId.filter(tag => {
          return (
            tag.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            tag.name_cn.indexOf(val) > -1
          );
        });
      });
    },
    // 实体选择过滤
    async filterFn(val, update, abort) {
      update(() => {
        this.nameEnList = this.filterModelList.filter(item => {
          return (
            item.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            item.name_cn.toLowerCase().indexOf(val.toLowerCase()) > -1
          );
        });
      });
    },
    handleError() {
      errorNotify(
        `部分实体在当前环境${
          this.envModel.env.name_en
        }未配置映射值，详见下方值为#ERROR#的字段`
      );
    },
    compare(property) {
      return (one, two) => {
        let before = one[property];
        let after = two[property];
        return before >= after ? 1 : -1;
      };
    },
    appendModelHtml() {
      let reg1 = new RegExp(/&lt;/g);
      let reg2 = new RegExp(/&gt;/g);
      let modelHtml = this.modelHtml.replace(reg1, '<').replace(reg2, '>');
      this.$refs.propertyEdit.runCmd('insertText', modelHtml);
    },
    startRead(files) {
      const reader = new FileReader();
      reader.readAsText(files[0]);
      reader.onload = $event => {
        this.$refs.propertyEdit.runCmd('insertText', $event.target.result);
      };
    },
    async handleExtreConfigDialogOpen() {
      await this.queryExtraConfigParam({
        project_id: this.job.project_id
      });
      this.extreConfigDialogOpened = true;
    },
    changeExtreConfigEvent() {
      this.extreConfigDialogOpened = false;
    }
  }
};
</script>

<style lang="stylus" scoped>
.editor-warpper
  height: 100%;
  width 100%
  .col-3, .col
    display inline-block;
    height: 100%;
    overflow auto
    vertical-align: top;
    box-sizing border-box
  .col-3
    padding-bottom 20px
    width 25.5%;
  .col
    overflow auto
    padding-bottom 60px
    position relative
.editor-title
  height: 40px;
  line-height: 40px;
  // position: fixed;
  top: 48px;
  width: 100%;
  z-index: 2;
  background: #FFF;
.property-editor
  margin-top: 50px;
  height calc(100vh - 50px)
  overflow-y hidden
.form-item
  align-items center
  .form-label
    margin-right 16px
    position relative
  .select-width
    min-width 200px
</style>
