<template>
  <f-dialog
    :value="value"
    right
    persistent
    @input="$emit('input', $event)"
    :maximized="maximizedToggle"
    title="环境配置文件"
  >
    <div class="container" style="width:1080px">
      <!-- 顶部操作按钮 -->
      <div class="opt-btn q-gutter-md row">
        <fdev-btn
          v-if="isEditable"
          :label="`${toggleTool ? '隐藏' : '打开'}模板工具`"
          @click="toggleTool = !toggleTool"
          normal
        />
        <fdev-btn
          v-if="isEditable"
          label="保存部署模板"
          @click="handleSubmitModel"
          normal
        />
        <fdev-btn label="预览各环境配置文件" @click="getPreviewFile" normal />
      </div>

      <!-- 模板工具 -->
      <div class="card q-mt-md temp-tool" v-if="toggleTool && isEditable">
        <div class="bg-blue-1 full-width title-div q-mb-md row">
          <div class="title-icon row justify-center items-center">
            <f-icon name="version_s_f" class="text-blue-8" />
          </div>
          <div class="row justify-center items-center">
            模板工具
          </div>
        </div>
        <div class="row justify-between">
          <div>
            <f-formitem label="实体名称">
              <fdev-select
                use-input
                ref="envModel.term"
                v-model="envModel.term"
                @filter="modelfilterFn"
                :options="nameEnList"
                :option-value="opt => opt"
                option-label="name_cn"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label :title="scope.opt.name_cn">
                        {{ scope.opt.name_cn }}
                      </fdev-item-label>
                      <fdev-item-label caption :title="scope.opt.name_en">
                        {{ scope.opt.name_en }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
          </div>
          <div class="row q-gutter-x-md">
            <fdev-btn
              label="选择该实体配置模板"
              v-if="hasPermissions"
              @click="appendModelHtml"
              normal
            />
            <fdev-btn
              label="导入外部配置文档"
              :ficon="showUploader ? 'close' : 'add'"
              v-if="hasPermissions"
              @click="showUploader = !showUploader"
              normal
            />
          </div>
        </div>
        <div class="col-12 q-pl-lg">
          <pre class="scroll-x">
            <code class="text-left block" v-html="modelHtml"/>
          </pre>
        </div>
        <div class="uploader">
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
      </div>

      <!-- 编辑&预览配置文件 -->
      <div class="card" :class="toggleTool && isEditable ? '' : 'q-mt-md'">
        <div class="bg-blue-1 full-width title-div q-mb-md row">
          <!-- 编辑fdev配置模板文件 -->
          <div class="col-6 row items-center edit-preview">
            <div class="title-icon row justify-center items-center">
              <f-icon name="version_s_f" class="text-blue-8" />
            </div>
            <span class="q-mr-md">编辑fdev配置模板文件</span>
            <span class="row items-center">
              <fdev-tooltip
                anchor="top middle"
                self="center middle"
                :offest="[0, 0]"
              >
                <span>部署项左侧为应用key,右侧为fdev实体key</span>
              </fdev-tooltip>
              <f-icon name="help_c_o" />
            </span>
          </div>

          <!-- 预览各环境配置文件 -->
          <div class="col-6 row justify-between items-center edit-preview">
            <div>
              预览各环境配置文件
            </div>
            <div class="q-pr-md">
              <f-formitem label="环境选择：" page>
                <fdev-select
                  use-input
                  ref="envModel.env"
                  v-model="envModel.env"
                  @filter="envFilter"
                  :options="filterEnvList"
                  option-label="name_cn"
                  option-value="name_en"
                  class="special-select"
                >
                  <template v-slot:option="scope">
                    <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                      <fdev-item-section>
                        <fdev-item-label :title="scope.opt.name_cn">
                          {{ scope.opt.name_cn }}
                        </fdev-item-label>
                        <fdev-item-label caption :title="scope.opt.name_en">
                          {{ scope.opt.name_en }}
                        </fdev-item-label>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </f-formitem>
            </div>
          </div>
        </div>

        <div>
          <PropertyEditor
            v-model="editor"
            ref="propertyEdit"
            :properties="modelProperties"
            :disabled="!isEditable"
            value="propertyEdit"
            @error="handleError"
          />
        </div>
      </div>
    </div>

    <f-dialog
      :persistent="false"
      v-model="saveResultDialogOpened"
      right
      title="保存失败"
    >
      <div v-for="(arr, title) in result" :key="title">
        <fdev-chip
          icon="warning"
          square
          color="red"
          text-color="white"
          :label="title"
        />
        <p class="result-wrapper" v-for="res in arr" :key="res">
          <f-icon name="alert_t_f" class="text-red q-mr-md" />{{ res }}
        </p>
      </div>
    </f-dialog>
  </f-dialog>
</template>
<script>
import { mapState, mapActions } from 'vuex';
import PropertyEditor from '@/components/PropertyEditor';
import { deepClone, errorNotify, successNotify } from '@/utils/utils';
import {
  createEnvModel,
  createJobModel
} from '@/modules/Component/utils/constants.js';

export default {
  name: 'EnvConfig',
  components: {
    PropertyEditor
  },
  data() {
    return {
      id: '',
      toggleTool: true,
      maximizedToggle: true,
      envModel: createEnvModel(),
      filterEnvList: [],
      modelHtml: '',
      nameEnList: [],
      filterModelList: [],
      editor: '',
      modelProperties: [],
      showUploader: false,
      hasPermissions: true,
      isOnceComing: true,
      firstCategories: [],
      secondCategories: [],
      typeList: [],
      cloneSecCategories: [],
      job: createJobModel(),
      jobModel: createJobModel(),
      showLoading: false,
      orignalModelProperties: [],
      saveResultDialogOpened: false
    };
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    isEditable: {
      type: Boolean
    }
  },
  watch: {
    async value(newVal) {
      if (newVal) {
        this.showLoading = true;
        await this.handeleModelappDeployOpen();
        this.showLoading = false;
      }
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
    },

    '$route.params'(val) {
      if (val.id) {
        this.id = val.id;
      }
    }
  },
  computed: {
    ...mapState('jobForm', [
      'envList',
      'modelList',
      'modelConstant',
      'pirvateModelList',
      'previewFile'
    ]),
    ...mapState('componentForm', [
      'archetypeDetail',
      'archetypeIssueDetail',
      'result',
      'configTemplate'
    ])
  },
  methods: {
    ...mapActions('jobForm', [
      'queryModelList',
      'getEnvList',
      'getModelConstant',
      'queryExcludePirvateModelList',
      'previewConfigFile'
    ]),
    ...mapActions('componentForm', [
      'queryArchetypeIssueDetail',
      'queryArchetypeDetail',
      'saveConfigTemplate',
      'queryConfigTemplate',
      'queryArchetypeTypes'
    ]),
    async getPreviewFile() {
      if (!this.isOnceComing) {
        if (this.editor.trim() && this.envModel.env.name_en) {
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
                value: modelParam[key],
                isVarivale: true
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
    startRead(files) {
      const reader = new FileReader();
      reader.readAsText(files[0]);
      reader.onload = $event => {
        this.$refs.propertyEdit.runCmd('insertText', $event.target.result);
      };
    },
    appendModelHtml() {
      let reg1 = new RegExp(/&lt;/g);
      let reg2 = new RegExp(/&gt;/g);
      let modelHtml = this.modelHtml.replace(reg1, '<').replace(reg2, '>');
      this.$refs.propertyEdit.runCmd('insertText', modelHtml);
    },
    async handeleModelappDeployOpen() {
      this.isOnceComing = true;
      // 查询环境列表
      await this.getEnvList();
      this.filterEnvList = deepClone(this.envList);
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
      let params;
      // 查询已有配置模版
      if (this.isEditable) {
        await this.queryArchetypeIssueDetail({
          id: this.id
        });
        await this.queryArchetypeDetail({
          id: this.archetypeIssueDetail.archetype_id
        });
        params = {
          archetype_id: this.archetypeIssueDetail.archetype_id,
          branch: this.archetypeIssueDetail.feature_branch
        };
        await this.queryExcludePirvateModelList({
          name_en: this.archetypeDetail.artifactId
        });
      } else {
        await this.queryArchetypeDetail({
          id: this.id
        });
        params = {
          archetype_id: this.archetypeDetail.id,
          branch: 'master'
        };
      }

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
    // 实体选择过滤
    async modelfilterFn(val, update, abort) {
      update(() => {
        this.nameEnList = this.filterModelList.filter(item => {
          return (
            item.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            item.name_cn.toLowerCase().indexOf(val.toLowerCase()) > -1
          );
        });
      });
    },
    envFilter(val, update) {
      update(() => {
        this.filterEnvList = this.envList.filter(tag => {
          return (
            tag.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            tag.name_cn.indexOf(val) > -1
          );
        });
      });
    },
    async handleSubmitModel() {
      return this.$q
        .dialog({
          title: `保存持续集成部署模板`,
          message: `确定保存此部署模板吗？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.handleSaveTemplate();
        });
    },
    async handleSaveTemplate() {
      await this.queryArchetypeIssueDetail({
        id: this.id
      });
      const content = this.editor.replace(/\n{2,}/g, $1 => {
        const nArr = $1.split('\n');
        const res = nArr.slice(0, nArr.length / 2 + 1).join('\n');
        return res;
      });
      let params = {
        archetype_id: this.archetypeIssueDetail.archetype_id,
        branch: this.archetypeIssueDetail.feature_branch,
        content: content
      };
      await this.saveConfigTemplate(params);
      if (this.result) {
        this.saveResultDialogOpened = true;
        return;
      }
      successNotify('保存模板成功');
      this.$emit('input', false);
    }
  },
  created() {
    this.id = this.$route.params.id;
  }
};
</script>
<style lang="stylus" scoped>
.card
  .title-div
    .title-icon
      width 32px
      height 54px
      margin-left 8px
    .title-text
      height 54px
      margin-left 16px
.edit-preview
  height 54px
.temp-tool
  position relative
  .uploader
    position absolute
    right 0px
    top 60px
    z-index 999
.result-wrapper
  margin 0
  margin-left 16px
  margin-top 10px
  color $grey-7
</style>
