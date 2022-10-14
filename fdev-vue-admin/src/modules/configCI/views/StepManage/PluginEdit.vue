<template>
  <div>
    <div class="row">
      <f-formitem row label="步骤名称:">
        <fdev-input
          ref="stepName"
          v-model="$v.stepName.$model"
          :rules="[() => $v.stepName.required || '请输入步骤名称']"
        />
      </f-formitem>
    </div>

    <div class="m-b-40px">
      <div class="card-header">
        插件信息
      </div>
      <div class="card-content">
        <div class="row">
          <div class="field">插件名称</div>
          <div class="font-14px row item-center">
            <span
              @click="mdModalOpened = true"
              class="hand text-light-blue-9"
              >{{ pluginInfo.pluginName }}</span
            >
            <fdev-btn padding="none" flat ficon="information" class="tip-icon">
              <fdev-tooltip position="top">
                {{ pluginInfo.pluginDesc }}
              </fdev-tooltip>
            </fdev-btn>
          </div>
        </div>
      </div>
    </div>

    <!--插件有脚本信息的话，展示脚本输入框-->
    <div v-if="hasScript">
      <div class="card-header q-mb-md">执行命令</div>
      <div style="height: 230px">
        <fdev-input
          style="max-width:96%"
          class="q-ml-md"
          type="textarea"
          ref="code"
          @blur="scriptBlur()"
          :rules="[() => $v.script.scriptRequired || '请输入脚本']"
        />
      </div>
    </div>

    <!--输入参数卡片，实体模板数组不为空 或者 插件有 params 参数时才展示-->
    <div class="inputCard2" v-if="hasModelTemplate || hasParams">
      <div class="card-header">
        输入参数
      </div>

      <div class="card-content">
        <!--实体模板的卡片信息-->
        <div v-if="hasModelTemplate">
          <div
            v-for="(item, index) in $v.modelTemplates.$each.$iter"
            :key="item.id"
          >
            <div class="row m-b-16px">
              <div class="row items-center break-word m-r-20px">
                {{ item.$model.nameCn }}
              </div>
              <div class="font-14px break-word">
                <fdev-btn
                  class="q-mr-lg"
                  label="选择实体"
                  @click="selectEntity(index, item.$model.id)"
                />
                <fdev-chip
                  class="chip-clear"
                  removable
                  clickable
                  dense
                  square
                  v-model="item.$model.entityChip"
                  @remove="removeEntity(index)"
                >
                  {{ item.$model.entityInfo.nameCn }}
                  <img
                    src="../../assets/icon-chip-close.png"
                    @click="closeChip(index)"
                  />
                </fdev-chip>
              </div>
            </div>

            <!--选了实体后，展示映射值选择框-->
            <div
              class="row map-div font-16px m-b-20px"
              v-if="item.$model.entityChip"
            >
              <f-formitem row label="选择环境下的映射值">
                <fdev-select
                  ref="map-select"
                  v-model="item.$model.environment"
                  :options="item.$model.environmentOptions"
                  :display-value="
                    `${
                      item.$model.environment
                        ? item.$model.environment.nameEn
                        : '选择环境'
                    }`
                  "
                  @input="queryMappingValue"
                >
                  <template v-slot:option="scope">
                    <div
                      @click="selectOption(scope.opt, index, scope.index)"
                      class="select-option"
                    >
                      {{ scope.opt.nameEn }}
                    </div>
                  </template>
                </fdev-select>
              </f-formitem>
            </div>

            <!--选中映射值的情况下，展示回显的变量数据-->
            <div
              v-if="item.$model.hasMappingValue"
              class="font-16px font-color-default m-b-24px"
            >
              <div class="variable-card">
                <div
                  v-for="v in item.$model.variables"
                  :key="v.id"
                  class="row m-b-24px"
                >
                  <div class="row items-center variable-card-field break-word">
                    {{ v.nameCn }}
                  </div>
                  <div
                    class="row items-center width-360px min-height-40px break-word"
                  >
                    {{ v.value }}
                  </div>
                </div>
              </div>
            </div>

            <!--没选中映射值的情况下，展示输入框让用户输入变量-->
            <div
              v-if="!item.$model.hasMappingValue"
              class="border-solid q-mb-md"
            >
              <div
                v-for="(e, i) in item.envKeys.$each.$iter"
                :key="e.$model.prop_key"
              >
                <f-formitem :label="e.$model.nameCn" required>
                  <fdev-input
                    :ref="`prop_value-${index}-${i}`"
                    v-model="e.prop_value.$model"
                    :placeholder="`请输入${e.$model.nameCn}`"
                    :rules="[
                      () => e.prop_value.required || `请输入${e.$model.nameCn}`
                    ]"
                  />
                </f-formitem>
              </div>
            </div>
          </div>
        </div>

        <!--插件有 params 的话展示参数卡片-->
        <div class="font-16px m-b-24px input-card" v-if="hasParams">
          <div
            v-for="(item, index) in $v.pluginData.params.$each.$iter"
            :key="index"
          >
            <div v-if="!item.$model.hidden">
              <!-- 参数一共有9种类型 实体类型不显示label-->
              <f-formitem
                v-if="item.$model.type === 'input'"
                :help="item.$model.hint"
                :label="item.$model.label"
                :required="item.$model.required"
              >
                <fdev-input
                  :ref="`params${index}`"
                  outlined
                  v-model="item.value.$model"
                  placeholder="请输入"
                  :rules="[() => item.value.valueRequired || '请输入参数']"
                />
              </f-formitem>
              <f-formitem
                v-if="item.$model.type === 'multipleInput'"
                :help="item.$model.hint"
                :label="item.$model.label"
                :required="item.$model.required"
              >
                <fdev-input
                  :ref="`params${index}`"
                  outlined
                  type="textarea"
                  v-model="item.value.$model"
                  placeholder="请输入"
                  :rules="[() => item.value.valueRequired || '请输入参数']"
                />
              </f-formitem>
              <f-formitem
                v-if="item.$model.type === 'password'"
                :help="item.$model.hint"
                :label="item.$model.label"
                :required="item.$model.required"
              >
                <fdev-input
                  :ref="`params${index}`"
                  outlined
                  autocomplete="new-password"
                  type="password"
                  v-model="item.value.$model"
                  placeholder="请输入"
                  :rules="[() => item.value.valueRequired || '请输入参数']"
                />
              </f-formitem>
              <f-formitem
                v-if="item.$model.type === 'select'"
                :help="item.$model.hint"
                :label="item.$model.label"
                :required="item.$model.required"
              >
                <fdev-select
                  class="q-mb-md"
                  :ref="`params${index}`"
                  outlined
                  option-label="value"
                  option-value="key"
                  map-options
                  emit-value
                  clearable
                  v-model="item.value.$model"
                  placeholder="请选择"
                  :options="item.$model.itemValue"
                  :rules="[() => item.value.valueRequired || '请选择']"
                />
              </f-formitem>
              <f-formitem
                v-if="item.$model.type === 'http-select'"
                :help="item.$model.hint"
                :label="item.$model.label"
                :required="item.$model.required"
              >
                <fdev-select
                  class="q-mb-md"
                  :ref="`params${index}`"
                  option-label="valueUp"
                  option-value="key"
                  use-input
                  map-options
                  emit-value
                  clearable
                  v-model="item.value.$model"
                  placeholder="请选择"
                  @filter="
                    (val, update, abort) =>
                      filterHttp(val, update, abort, item.$model.itemValue)
                  "
                  :options="httpselOptions"
                  :rules="[() => item.value.valueRequired || '请选择']"
                >
                  <template v-slot:option="scope">
                    <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                      <fdev-item-section>
                        <fdev-item-label>{{
                          scope.opt.valueUp
                        }}</fdev-item-label>
                        <fdev-item-label caption>
                          {{ scope.opt.valueDown }}
                        </fdev-item-label>
                      </fdev-item-section>
                    </fdev-item>
                  </template>
                </fdev-select>
              </f-formitem>
              <!-- 多选 -->
              <f-formitem
                v-if="item.$model.type === 'multipleSelect'"
                :help="item.$model.hint"
                :label="item.$model.label"
                :required="item.$model.required"
              >
                <fdev-select
                  :ref="`params${index}`"
                  multiple
                  use-chips
                  outlined
                  map-options
                  emit-value
                  v-model="item.valueArray.$model"
                  placeholder="请选择"
                  option-label="value"
                  option-value="key"
                  :options="item.$model.itemValue"
                  :rules="[() => item.valueArray.valueRequired1 || '请选择']"
                />
              </f-formitem>
              <f-formitem
                v-if="item.$model.type === 'fileEdit'"
                :help="item.$model.hint"
                diaS
                class="q-mb-lg"
                :label="item.$model.label"
                :required="item.$model.required"
              >
                <fdev-btn
                  :label="item.$model.label"
                  @click="clickFileEdit(item.$model)"
                />
              </f-formitem>
              <!-- 上传文件 -->
              <f-formitem
                v-if="item.$model.type === 'fileUpload'"
                :help="item.$model.hint"
                diaS
                class="q-mb-lg"
                :label="item.$model.label"
                :required="item.$model.required"
              >
                <fdev-btn @click="uploadFile(index)" :label="item.$model.label">
                </fdev-btn>
                <input
                  type="file"
                  @change="selectFile(item.$model)"
                  class="none"
                  id="fileUpload"
                  style="display:none"
                />
                <div class="file-path" v-if="item.$model.value">
                  filePath: {{ item.$model.value }}
                </div>
              </f-formitem>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 构建参数 -->
    <!-- addParamsFlag为true时显示 -->
    <div v-if="pluginData.addParamsFlag">
      <div class="card-header">
        构建参数
        <fdev-btn
          class="q-ml-md"
          outline
          ficon="add"
          label="添加参数"
          @click="openAddParams"
        />
      </div>
      <div>
        <fdev-markup-table flat>
          <thead class="bg-blue-grey-1">
            <tr>
              <th>变量类型</th>
              <th>参数</th>
              <th>映射</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in addParams" :key="index">
              <td>{{ item.type | getType }}</td>
              <td v-if="item.type === 'customVariableType'">
                {{ item.customParams.key }}/{{ item.customParams.value }}
              </td>
              <td v-if="item.type === 'entityTemplateType'">
                {{ item.entityTemplate.nameEn }}/{{ item.entity.nameEn }}/{{
                  item.env.nameEn
                }}
              </td>
              <td v-if="item.type === 'customVariableType'"></td>
              <td v-else>
                <fdev-btn
                  flat
                  label="查看映射"
                  @click="openEneityDialog(item)"
                />
              </td>
              <td>
                <fdev-btn
                  flat
                  color="grey-9"
                  ficon="delete_o"
                  @click="deleteParam(index)"
                />
              </td>
            </tr>
          </tbody>
        </fdev-markup-table>
      </div>
    </div>

    <div v-if="pluginData.artifacts">
      <div class="card-header">
        构建物上传
      </div>
      <div class="card-content">
        <div class="artifacts-div">
          <fdev-select
            ref="artifacts"
            class="artifacts-input chip-clear"
            outlined
            use-input
            use-chips
            multiple
            hide-dropdown-icon
            v-model="artifacts"
            placeholder="请输入构建物（每输完一个请回车）"
            :hint="pluginData.artifacts.hint"
            dense
            type="search"
            @new-value="createNewValue"
          />
        </div>
      </div>
    </div>

    <entity-select
      v-model="entitySelectDialogOpen"
      :template-id="templateId"
      @receive-entity="getEntity"
    />

    <div class="row items-center justify-end btns q-mt-lg">
      <fdev-btn
        class="q-mr-md"
        dialog
        outline
        label="取消"
        @click="handleCancel"
      />

      <fdev-btn class="q-ml-md" dialog label="确定" @click="handleConfirm" />
    </div>
    <f-dialog v-model="addParamsOpened" title="添加构建参数">
      <div class="div-wrapper bg-white">
        <fdev-form ref="addParaModel">
          <f-formitem label="变量类型" diaS>
            <fdev-select
              option-label="label"
              option-value="value"
              map-options
              emit-value
              v-model="$v.addParaModel.type.$model"
              placeholder="请选择"
              @input="inputType($event)"
              :options="paramsTypeOptions"
              :rules="[() => $v.addParaModel.type.required || '请选择变量类型']"
            />
          </f-formitem>
          <div v-if="addParaModel.type === 'customVariableType'">
            <f-formitem label="参数名" diaS>
              <fdev-input
                v-model="$v.addParaModel.customParams.key.$model"
                :placeholder="`输入参数`"
                :rules="[
                  () =>
                    $v.addParaModel.customParams.key.required || '请输入参数'
                ]"
              />
            </f-formitem>
            <f-formitem label="参数值" diaS>
              <fdev-input
                v-model="$v.addParaModel.customParams.value.$model"
                :placeholder="`输入值`"
                :rules="[
                  () =>
                    $v.addParaModel.customParams.value.required ||
                    '请输入参数值'
                ]"
              />
            </f-formitem>
          </div>
          <!-- 实体类型 -->
          <div v-show="addParaModel.type === 'entityTemplateType'">
            <f-formitem label="实体类型" diaS>
              <fdev-select
                option-label="nameCn"
                option-value="nameEn"
                use-input
                v-model="$v.addParaModel.entityTemplate.$model"
                @input="entityTemplateInput"
                @filter="filterEntityTemplate"
                :options="allEntityTemplateList"
                :rules="[
                  () =>
                    $v.addParaModel.entityTemplate.required || '请选择实体类型'
                ]"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label>{{ scope.opt.nameCn }}</fdev-item-label>
                      <fdev-item-label caption>
                        {{ scope.opt.nameEn }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
            <f-formitem label="实体" diaS>
              <fdev-select
                option-label="nameCn"
                option-value="nameEn"
                use-input
                :disable="!addParaModel.entityTemplate"
                @input="entityInput"
                @focus="entityFocus(addParaModel.entityTemplate)"
                v-model="$v.addParaModel.entity.$model"
                :options="modelListOptions"
                @filter="filterEntity"
                :rules="[() => $v.addParaModel.entity.required || '请选择实体']"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label>{{ scope.opt.nameCn }}</fdev-item-label>
                      <fdev-item-label caption>
                        {{ scope.opt.nameEn }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
              <fdev-tooltip v-if="!addParaModel.entityTemplate" position="top">
                请先选择实体模板
              </fdev-tooltip>
            </f-formitem>

            <f-formitem label="环境" diaS>
              <fdev-select
                :disable="!addParaModel.entity"
                @focus="envFocus(addParaModel.entity)"
                v-model="$v.addParaModel.env.$model"
                :options="envOptions"
                @filter="filterEnv"
                option-label="nameEn"
                option-value="nameEn"
                :rules="[() => $v.addParaModel.env.required || '请选择环境']"
              >
              </fdev-select>
              <fdev-tooltip v-if="!addParaModel.entity" position="top">
                请先选择实体
              </fdev-tooltip>
            </f-formitem>
          </div>
        </fdev-form>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          label="确定"
          dialog
          :disable="addParamsSubmit"
          @click="handelAddParams"
        />
      </template>
    </f-dialog>
    <md
      v-model="mdModalOpened"
      :pluginInfo="pluginInfo"
      @closeMd="mdModalOpened = false"
    />
    <f-dialog v-model="variablesOpened" title="实体参数映射">
      <div class="table-wrapper bg-white">
        <table class="table">
          <tr>
            <th>参数</th>
            <th>
              描述
            </th>
          </tr>
          <tr v-for="(item, index) of variablesData" :key="index">
            <td>{{ index }}</td>
            <td>{{ item }}</td>
          </tr>
        </table>
      </div>
    </f-dialog>
    <Fileconfiguration
      :isShow.sync="isShowFileConfig"
      :fileConfigInfo="fileConfigInfo"
      :configTemplateIds="configTemplateIds"
      @addYamlConfigSuccess="addYamlConfigSuccess"
    />
  </div>
</template>

<script>
import EntitySelect from './EntitySelect';
import { required } from 'vuelidate/lib/validators';
import { mapState, mapActions } from 'vuex';
import { deepClone, validate, uploadOrDownloadFile } from '../../utils/utils';
import CodeMirror from 'codemirror/lib/codemirror';
import 'codemirror/theme/lucario.css';
require('codemirror/mode/shell/shell');
import { setTimeout } from 'timers';
import md from './mdToHtml';
import Fileconfiguration from './FileConfiguration';
export default {
  name: 'PluginEdit',
  components: {
    EntitySelect,
    md,
    Fileconfiguration
  },
  props: {
    pluginInfo: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      entitySelectDialogOpen: false,
      hasParams: false,
      stepName: '',
      hasArtifacts: false,
      hasScript: false,
      artifacts: [],
      pluginData: {},
      hasModelTemplate: false,
      modelTemplates: [],
      currentIndex: 0,
      script: '',
      warning: false,
      entity: {},
      modelTemplateId: '',
      addParaModel: {
        type: 'customVariableType',
        customParams: { key: '', value: '' },
        entityTemplate: null,
        entity: null,
        env: null
      },
      addParams: [],
      paramsTypeOptions: [
        { label: '自定义变量', value: 'customVariableType' },
        { label: '实体类型', value: 'entityTemplateType' }
      ],
      allEntityTemplateList: [],
      modelListOptions: [],
      envOptions: [],
      newEnvOptions: [],
      addParamsOpened: false,
      variablesOpened: false,
      variablesData: [],
      addPramsBtn: '',
      mdModalOpened: false,
      isShowFileConfig: false, //展示文件配置弹框
      configTypeDefualt: '', //type=fileEdt时配置文件id
      fileConfigInfo: {},
      configTemplateIds: [],
      selectParamsIndex: null, //选中parmas下第几个的index
      httpselOptions: []
    };
  },
  validations: {
    stepName: {
      required
    },
    script: {
      //校验脚本框架，配合 blur 使用
      scriptRequired(cm) {
        return !!cm.getValue();
      }
    },

    pluginData: {
      params: {
        $each: {
          // 当类型为multipleSelect时，做特殊判断
          value: {
            valueRequired(val, param) {
              if (param.required && param.type !== 'multipleSelect') {
                return required(val);
              } else {
                return true;
              }
            }
          },
          valueArray: {
            valueRequired1(val, param) {
              if (param.required && param.type === 'multipleSelect') {
                return required(val);
              } else {
                return true;
              }
            }
          }
        }
      }
    },

    modelTemplates: {
      $each: {
        envKeys: {
          $each: {
            prop_value: {
              required
            }
          }
        }
      }
    },
    addParaModel: {
      type: { required },
      customParams: {
        key: { required },
        value: { required }
      },
      entityTemplate: { required },
      entity: { required },
      env: { required }
    }
  },
  watch: {
    pluginInfo: {
      handler: async function(newVal, oldVal) {
        //更换插件的情况下，需要先把脚本输入框转换为 textarea ，防止脚本输入框累加
        this.selectParamsIndex = null; //清空
        if (oldVal && oldVal.scriptCmd) {
          this.script.toTextArea();
        }
        this.init();
        this.pluginData = deepClone(newVal);
        //addParamsFlag为true时,发接口查询实体相关数据
        if (this.pluginData.addParamsFlag) {
          this.getEntityData();
        }
        if (this.pluginData.scriptCmd) {
          this.hasScript = true;
        }
        if (this.pluginData.artifacts) {
          this.hasArtifacts = true;
          const default_ = this.pluginData.artifacts.default.slice();
          default_.forEach((item, index, self) => {
            if (!item.trim()) {
              self.splice(index, 1);
            }
          });
          this.artifacts = default_.slice();
        }
        await this.transferModelTemplates();
        if (
          Array.isArray(this.pluginData.params) &&
          this.pluginData.params.length > 0
        ) {
          // 给value valueArray赋值，是因为该字段最终校验的时候有非空校验
          this.pluginData.params = this.pluginData.params.map(param => {
            if (!param.hidden) {
              if (param.type === 'entityType') {
                this.$set(param, 'value', '可通过校验的值');
              } else if (param.type === 'multipleSelect') {
                this.$set(param, 'valueArray', param.defaultArr);
              } else {
                this.$set(param, 'value', param.default);
              }
            }
            return param;
          });
          this.hasParams = true;
        }
        if (this.hasScript) {
          this.$nextTick(() => {
            //初始化编辑器
            this.script = CodeMirror.fromTextArea(
              this.$refs['code'].$refs.input,
              {
                mode: 'text/x-sh',
                theme: 'lucario',
                lineNumbers: true,
                lineWrapping: false,
                tabSize: 4,
                line: true,
                matchBrackets: true,
                showCursorWhenSelecting: true
              }
            );
            this.script.setValue(this.pluginData.scriptCmd);
          });
        }
      },
      immediate: true
    },
    addParaModel(val) {
      if (val.type === 'customVariableType') {
        this.addPramsBtn = 'variableBtn';
      } else {
        this.addPramsBtn = 'templateBtn';
      }
    }
  },
  filters: {
    getType(val) {
      let typeDate = {
        customVariableType: '自定义变量',
        entityTemplateType: '实体类型',
        entityType: '老实体'
      };
      return typeDate[val];
    }
  },
  computed: {
    ...mapState('configCIForm', {
      modelTempDetail: 'modelTempList', // 查询到的实体详情信息
      modelTempListPaging: 'modelTempListPaging',
      modelList: 'modelList',
      entityModelDetail: 'entityModelDetail',
      yamlConfigByIdInfo: 'yamlConfigByIdInfo'
    }),
    templateId() {
      return this.modelTemplateId;
    },
    addParamsSubmit() {
      let model = this.addParaModel;
      if (this.addPramsBtn === 'variableBtn') {
        return !(model.customParams.key && model.customParams.value);
      } else {
        return !(model.entityTemplate && model.entity && model.env);
      }
    }
  },
  methods: {
    ...mapActions('configCIForm', [
      'getModelTempDetail', // 查询实体模板详情
      'queryTemplate',
      'querySectionEntity',
      'queryEntityModelDetail',
      'getYamlConfigById'
    ]),
    filterHttp(val, update, abort, data) {
      update(() => {
        this.httpselOptions = data.filter(
          env => env.valueDown.includes(val) || env.valueDown.includes(val)
        );
      });
    },
    openAddParams() {
      (this.addParaModel = {
        type: 'customVariableType',
        customParams: { key: '', value: '' },
        entityTemplate: null,
        entity: null,
        env: null
      }),
        (this.addParamsOpened = true);
    },
    handelAddParams() {
      this.addParams.push(this.addParaModel);
      this.addParamsOpened = false;
    },
    filterEntityTemplate(val, update) {
      update(() => {
        let data = this.modelTempListPaging;
        this.allEntityTemplateList = data.filter(
          v =>
            v.nameEn.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            v.nameCn.toLowerCase().indexOf(val.toLowerCase()) > -1
        );
      });
      this.$forceUpdate();
    },
    filterEntity(val, update) {
      setTimeout(() => {
        update(() => {
          if (val === '') {
            this.modelListOptions = this.modelList.entityModelList;
          } else {
            this.modelListOptions = this.modelList.entityModelList.filter(
              v =>
                v.nameEn.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
                v.nameCn.toLowerCase().indexOf(val.toLowerCase()) > -1
            );
          }
        });
      }, 500);
      this.$forceUpdate();
    },
    filterEnv(val, update) {
      setTimeout(() => {
        update(() => {
          if (val === '') {
            this.envOptions = this.newEnvOptions;
          } else {
            this.envOptions = this.newEnvOptions.filter(
              v => v.nameEn.toLowerCase().indexOf(val.toLowerCase()) > -1
            );
          }
        });
      }, 500);
      this.$forceUpdate();
    },
    handleAddParams(type) {
      let array = deepClone(this.newAddParams[0]);
      if (type === 'customVariableType') {
        this.customVariableTypeParams.push(array);
      } else if (type === 'entityTemplateType') {
        this.entityTemplateTypeParams.push(array);
      } else {
        this.entityTypeParams.push(array);
      }
      // this.addParams.push(array);
    },
    deleteParam(index) {
      this.addParams.splice(index, 1);
    },
    async inputType(data) {
      //改条选择类型发生变化时，清空除了类型的其他值
      if (data) {
        let cloneAddParams = {
          type: data,
          customParams: { key: '', value: '' },
          entityTemplate: null,
          entity: null,
          env: null
        };
        this.addParaModel = cloneAddParams;
      }
      await this.$forceUpdate();
    },
    openEneityDialog(data) {
      delete data.env.nameEn;
      this.variablesData = data.env;
      this.variablesOpened = true;
    },
    // 实体类型--实体模板发生变化时,实体,环境清空
    entityTemplateInput(index) {
      this.addParaModel.entity = null;
      this.addParaModel.env = null;
    },
    // 实体类型--实体发生变化时,环境清空
    entityInput(index) {
      this.addParaModel.env = null;
    },
    //实体类型--实体根据选择的实体模板查询值
    async entityFocus(data) {
      await this.querySectionEntity({
        templateId: data.id
      });
      this.modelListOptions = deepClone(this.modelList.entityModelList);
    },

    //实体类型--环境根据选择的实体查询值
    async envFocus(data) {
      let optionsArray = [];
      let obj = {};
      await this.queryEntityModelDetail({
        id: data.id
      });
      let valueDate =
        Object.values(this.entityModelDetail.propertiesValue) || [];
      valueDate.forEach(item => {
        obj = Object.values(item)[0];
        this.$set(obj, 'nameEn', Object.keys(item)[0]);
        optionsArray.push(obj);
      });

      this.newEnvOptions = optionsArray;
      this.$forceUpdate();
    },

    // 失去焦点时手动触发校验
    scriptBlur() {
      this.$v.script.$touch();
      const ref = this.$refs['code'];
      for (let key in ref) {
        if (ref.hasOwnProperty(key)) {
          if (ref[key] && ref[key].validate) {
            ref[key].validate();
          }
        }
      }
    },
    // 请求全量实体模板对象,针对老实体，查全量实体
    async getEntityData() {
      await this.queryTemplate({
        page: 1,
        perPage: 0
      });
      this.allEntityTemplateList = deepClone(this.modelTempListPaging);
    },
    init() {
      this.hasParams = false;
      this.hasModelTemplate = false;
      this.modelTemplates = [];
      this.hasArtifacts = false;
      this.warning = false;
      this.hasScript = false;
      this.artifacts = [];
      this.pluginData = {};
      this.stepName = this.pluginInfo.pluginName;
    },
    async transferModelTemplates() {
      // const en_t = this.pluginData.entityTemplateList;
      /*
        在原有逻辑（entityTemplateList）基础上修改，获取params里面的实体字段信息，
        最终提交的时候，再把提取出的entityList数据组装回去(这样能尽可能少的修改原有代码逻辑)
     */
      let en_t = this.pluginData.params.filter(param => {
        return param.type === 'entityType';
      });
      if (en_t.length > 0) {
        en_t = en_t.map(param => {
          return {
            ...param,
            // ...param.entityTemplate
            ...param.entityTemplateParams[0] //id都一样
          };
        });
      }
      if (Array.isArray(en_t) && en_t.length > 0) {
        //该插件有实体模板
        this.hasModelTemplate = true;
        this.modelTemplates = deepClone(en_t);
        this.modelTemplates.forEach(m_t => {
          Reflect.set(m_t, 'entityChip', false);
          Reflect.set(m_t, 'hasMappingValue', false);
          Reflect.set(m_t, 'variables', []);
          Reflect.set(m_t, 'entityInfo', {});
          Reflect.set(m_t, 'environment', '');
          Reflect.set(m_t, 'environmentOptions', []);
        });
        for (let m_t of this.modelTemplates) {
          await this.getModelTempDetail({
            id: m_t.id
          });
          const envKeys = deepClone(this.modelTempDetail.properties) || [];
          // 若插件为镜像部署，则手动输入时给部署镜像仓库的实体模板增加“部署环境”
          if (envKeys.length !== 0 && m_t.nameCn === '部署镜像仓库') {
            envKeys.push({
              nameCn: '部署环境',
              nameEn: 'image_env',
              required: true,
              type: 'string'
            });
          }
          envKeys.forEach((item, index, self) => {
            this.$set(self[index], 'prop_value', '');
          });
          this.$set(m_t, 'envKeys', envKeys);
        }
      }
    },
    selectEntity(index, id) {
      this.currentIndex = index;
      this.modelTemplateId = id;
      this.entitySelectDialogOpen = true;
    },
    async getEntity(entity) {
      this.modelTemplates[this.currentIndex].entityInfo = entity;
      this.modelTemplates[this.currentIndex].entityChip = true;
      await this.queryModelEnvList();
      this.$forceUpdate();
    },
    async queryModelEnvList() {
      await this.queryEntityModelDetail({
        id: this.modelTemplates[this.currentIndex].entityInfo.id
      });
      //组装历史的页面显示数据结构
      let entityVariables = this.entityModelDetail.properties;
      let optionsArray = [];
      Object.values(this.entityModelDetail.propertiesValue).forEach(item => {
        Object.keys(item).forEach(nameEn => {
          let envWithVariables = {};
          envWithVariables['nameEn'] = nameEn;
          let variables = deepClone(entityVariables);
          variables.forEach(properties => {
            properties['value'] = item[nameEn][properties['nameEn']];
          });
          envWithVariables['variables'] = variables;
          optionsArray.push(envWithVariables);
        });
      });
      this.modelTemplates[this.currentIndex].environmentOptions = deepClone(
        optionsArray
      );
      this.modelTemplates[this.currentIndex].environment = null;
      this.modelTemplates[this.currentIndex].hasMappingValue = false;
    },
    removeEntity(index) {
      this.modelTemplates[index].environment = '';
      this.modelTemplates[index].environmentOptions = [];
      this.modelTemplates[index].entityInfo = {};
      this.modelTemplates[index].variables = [];
      this.modelTemplates[index].hasMappingValue = false;
    },
    async queryMappingValue(val) {
      if (!val) {
        return;
      }
      await this.queryEntityModelDetail({
        id: this.modelTemplates[this.currentIndex].entityInfo.id
      });
      this.$set(
        this.modelTemplates[this.currentIndex],
        'variables',
        val.variables
      );
      this.$set(
        this.modelTemplates[this.currentIndex],
        'hasMappingValue',
        true
      );
    },
    //增加唯一值
    createNewValue(val, done) {
      done(val, 'add-unique');
    },
    checkForm() {
      let keys = ['stepName'];
      this.$v.stepName.$touch();

      if (this.hasScript) {
        this.$v.script.$touch();
        keys.push('code');
      }
      let other6TypeParams = false;
      let isNeedVerifyBtn = false; //需要验证的按钮
      if (this.hasParams) {
        const paramsKeys = Object.keys(this.$refs).filter(
          key => this.$refs[key] && key.indexOf('params') > -1
        );
        // 其他六种类型的校验
        other6TypeParams = paramsKeys.length > 0;
        if (other6TypeParams) {
          this.$v.pluginData.params.$touch();
          validate(paramsKeys.map(key => this.$refs[key]));
        }
        //验证类型为fileEdit，fileUpload且required为必输条件
        const findIdx = this.pluginData.params.findIndex(item => {
          if (
            (item.type === 'fileEdit' || item.type === 'fileUpload') &&
            item.required
          ) {
            return !item.value || item.default === item.value;
          }
        });
        if (findIdx !== -1) {
          isNeedVerifyBtn = true;
        }
      }
      let flag = false,
        exitNoMappingValueFlag = false;
      if (this.hasModelTemplate) {
        flag = this.modelTemplates.some(m_t => !m_t.hasMappingValue);
        exitNoMappingValueFlag = this.modelTemplates.some(
          m_t => m_t.hasMappingValue
        );
      }
      if (flag) {
        if (exitNoMappingValueFlag) {
          this.modelTemplates = this.modelTemplates.map((item, index) => {
            if (item.hasMappingValue) {
              item.envKeys = item.envKeys.map(envKey => {
                return {
                  ...envKey,
                  prop_value: '校验必须有值才添加的'
                };
              });
            }
            return item;
          });
        }
        this.$v.modelTemplates.$touch();
        const modelTemplatesKeys = Object.keys(this.$refs).filter(key => {
          let keyId =
            this.$refs[key] &&
            this.$refs[key].length > 0 &&
            key.indexOf('prop_value') > -1;
          return keyId;
        });
        validate(modelTemplatesKeys.map(key => this.$refs[key][0]));
      }
      if (
        this.$v.stepName.$invalid ||
        (this.hasScript && this.$v.script.$invalid) ||
        (flag && this.$v.modelTemplates.$invalid) ||
        (this.hasParams &&
          ((other6TypeParams && this.$v.pluginData.params.$invalid) ||
            isNeedVerifyBtn))
      ) {
        this.warning = true;
      }
    },
    async selectOption(opt, index, index_) {
      this.$set(
        this.modelTemplates[index],
        'environment',
        this.modelTemplates[index].environmentOptions[index_]
      );
      this.$refs['map-select'][0].menu = false;
      await this.queryMappingValue(opt);
      this.$forceUpdate();
    },
    handleConfirm() {
      this.selectParamsIndex = null; //清空
      const pluginModel = {};
      this.checkForm();
      Reflect.set(pluginModel, 'pluginCode', this.pluginData.pluginCode);
      Reflect.set(pluginModel, 'script', this.pluginData.script);
      Reflect.set(pluginModel, 'artifacts', {
        value: this.artifacts,
        hint: this.pluginData.artifacts.hint
      });
      if (this.hasScript) {
        const script = this.script.getValue();
        Reflect.set(pluginModel, 'scriptCmd', script);
        if (this.pluginInfo.scriptCmd === script) {
          Reflect.set(pluginModel, 'scriptUpdateFlag', false);
        } else {
          Reflect.set(pluginModel, 'scriptUpdateFlag', true);
        }
      }

      // 组装addparams数据
      if (this.addParams.length) {
        this.addParams.map((item, index) => {
          if (item.entity) {
            let entity = {
              id: item.entity.id,
              nameCn: item.entity.nameCn,
              nameEn: item.entity.nameEn
            };
            this.$set(item, 'entity', entity);
          }
          if (item.env) {
            let env = {
              nameEn: item.env.nameEn
            };
            this.$set(item, 'env', env);
          }
          if (item.entityTemplate) {
            let entityTemplate = {
              id: item.entityTemplate.id,
              nameCn: item.entityTemplate.nameCn,
              nameEn: item.entityTemplate.nameEn
            };
            this.$set(item, 'entityTemplate', entityTemplate);
          }
        });
        Reflect.set(pluginModel, 'addParams', this.addParams);
      }

      if (this.hasParams) {
        let params = this.pluginData.params.slice();
        Reflect.set(pluginModel, 'params', params);
      }
      // params里面有实体类型的字段 需要重新组装数据
      if (this.hasModelTemplate) {
        if (this.modelTemplates.length > 1) {
          this.modelTemplates.map((m_t, index) => {
            const entityTemplateParams = { ...m_t };
            Reflect.set(entityTemplateParams, 'id', m_t.id);
            Reflect.set(entityTemplateParams, 'nameCn', m_t.nameCn);
            Reflect.set(entityTemplateParams, 'nameEn', m_t.nameEn);
            if (m_t.hasMappingValue) {
              const entity = {
                id: m_t.entityInfo.id,
                nameEn: m_t.entityInfo.nameEn,
                nameCn: m_t.entityInfo.nameCn
              };
              Reflect.set(entityTemplateParams, 'entity', entity);
              const env = {
                id: m_t.environment.id,
                nameEn: m_t.environment.nameEn,
                nameCn: m_t.environment.env
              };
              Reflect.set(entityTemplateParams, 'env', env);
            } else {
              const entityParams = m_t.envKeys.map(e_v => ({
                key: e_v.nameEn,
                value: e_v.prop_value,
                label: e_v.nameCn
              }));
              Reflect.set(entityTemplateParams, 'entityParams', entityParams);
            }
            let newParams = {
              id: entityTemplateParams.id,
              nameEn: entityTemplateParams.nameEn,
              nameCn: entityTemplateParams.nameCn,
              entity: entityTemplateParams.entity,
              env: entityTemplateParams.env,
              entityParams: entityTemplateParams.entityParams
            };
            pluginModel.params.map((item, i) => {
              if (item.type === 'entityType' && item.key === m_t.key) {
                Reflect.set(pluginModel.params[i], 'entityTemplateParams', [
                  newParams
                ]);
              }
            });
            return entityTemplateParams;
          });
        } else {
          this.modelTemplates.map((m_t, index) => {
            const entityTemplateParams = { ...m_t };
            Reflect.set(entityTemplateParams, 'id', m_t.id);
            Reflect.set(entityTemplateParams, 'nameCn', m_t.nameCn);
            Reflect.set(entityTemplateParams, 'nameEn', m_t.nameEn);
            if (m_t.hasMappingValue) {
              const entity = {
                id: m_t.entityInfo.id,
                nameEn: m_t.entityInfo.nameEn,
                nameCn: m_t.entityInfo.nameCn
              };
              Reflect.set(entityTemplateParams, 'entity', entity);
              const env = {
                id: m_t.environment.id,
                nameEn: m_t.environment.nameEn,
                nameCn: m_t.environment.env
              };
              Reflect.set(entityTemplateParams, 'env', env);
            } else {
              const entityParams = m_t.envKeys.map(e_v => ({
                key: e_v.nameEn,
                value: e_v.prop_value,
                label: e_v.nameCn
              }));
              Reflect.set(entityTemplateParams, 'entityParams', entityParams);
            }
            let newParams = {
              id: entityTemplateParams.id,
              nameEn: entityTemplateParams.nameEn,
              nameCn: entityTemplateParams.nameCn,
              entity: entityTemplateParams.entity,
              env: entityTemplateParams.env,
              entityParams: entityTemplateParams.entityParams
            };
            pluginModel.params.map((item, i) => {
              if (item.type === 'entityType') {
                Reflect.set(pluginModel.params[i], 'entityTemplateParams', [
                  newParams
                ]);
              }
            });
            return entityTemplateParams;
          });
        }
      }
      const stepModel = {};
      const name = !this.stepName.trim()
        ? this.pluginInfo.pluginName
        : this.stepName;
      Reflect.set(stepModel, 'name', name);
      Reflect.set(stepModel, 'warning', this.warning);
      Reflect.set(stepModel, 'pluginInfo', pluginModel);
      this.$emit('receive-step-info', stepModel);
      this.$emit('close');
    },
    handleCancel() {
      this.$emit('close', false);
    },
    closeChip(index) {
      this.modelTemplates[index].entityChip = false;
      this.removeEntity(index);
      this.$forceUpdate();
    },
    //编辑文件配置
    async clickFileEdit(item) {
      this.isShowFileConfig = true;
      this.configTypeDefualt = item.default; //记录default
      const { value } = item; //已编辑状态存在
      await this.getYamlConfigById({ configId: value ? value : item.default });
      this.configTemplateIds = item.itemValue;
      this.fileConfigInfo = this.yamlConfigByIdInfo;
      this.$forceUpdate();
    },
    //添加配置文件成功返回id
    addYamlConfigSuccess(objId) {
      let { params } = this.pluginData;
      const idx = params.findIndex(item => {
        return item.default === this.configTypeDefualt;
      });
      if (idx !== -1) {
        this.pluginData.params[idx].value = objId;
      }
    },
    //上传文件
    uploadFile(idx) {
      this.selectParamsIndex = idx;
      let file = document.querySelector('#fileUpload');
      file.value = '';
      file.click();
    },
    async selectFile(item) {
      let file = document.querySelector('#fileUpload').files[0];
      file = file ? file : { size: '', name: '' };
      let uploadParam = new FormData();
      uploadParam.append('file', file); //参数
      let param = {
        type: 'upload',
        url: 'fcipipeline/api/pipeline/fileUpload2Nas',
        params: uploadParam
      };
      const res = await uploadOrDownloadFile(param);
      const index = this.selectParamsIndex;
      this.pluginData.params[index].value = res.data.filePath;
      // item.value = data.data.filePath;
      this.$forceUpdate();
    }
  }
};
</script>
<style scoped lang="stylus">
.break-word
  word-wrap break-word

.field
  width 121px
  margin-left 10px
  margin-right 20px
  font-size 14px

.m-b-16px
  margin-bottom 16px

.m-b-20px
  margin-bottom 20px

.m-b-24px
  margin-bottom 24px

.m-b-40px
  margin-bottom 40px

.m-r-20px
  margin-right 20px

.font-14px
  font-size 14px

.font-16px
  font-size 16px

.value
  font-size 14px

.card-header
  height 40px
  line-height 40px
  font-size 16px
  font-weight 500
  color #37474F
  margin-bottom 12px

.card-content
  padding-top 9px
.chip-clear
   >>> .q-chip__icon
    display none

.btns
  position sticky
  bottom 0

.width-360px
  width 360px

.min-height-40px
  min-height 40px

.border-solid
  width 646px
  padding 24px 0 24px 24px
  border 1px solid #ECEFF1
  border-radius 16px

.variable-card
  width 646px
  padding 24px 0 24px 24px
  border 1px solid #ECEFF1
  border-radius 16px
  >>> .q-field__bottom
    font-size 14px
    min-height 12px
    height 24px
    padding-top 6px

.variable-card-field
  width 200px
  margin-right 10px
  padding-top 5px

.inputCard2
  margin-top 20px

.artifacts-input
  width 485px
  >>> .q-field__bottom
    padding-top 16px

.artifacts-div
  >>> .q-chip--dense
    height 20px
    margin 2px 4px

/deep/ .CodeMirror
    width 100%
    height 215px
    font-size 14px


.font-14px
  >>> .q-chip
    font-size 14px
    background #F1F5F8
    color #1A76D2
    min-width 100px
    height 36px
    margin 0

.font-14px
  >>> .q-chip__icon
    color #1A76D2

.font-color-default
  color #37374F

.tip-icon
  font-size 20px
  color #1565C0
  margin-left 14px

/deep/ .CodeMirror-scroll
  margin-bottom 0


.select-option
  height 36px
  padding 8px 16px 8px
  font-size 14px
  color #78909C

.select-option:hover
  color #1565C0
  cursor pointer

/deep/.scroll
  &::-webkit-scrollbar
    width 4px
  &::-webkit-scrollbar-thumb
    border-radius 4px
    background-color #1565C0
  &::-webkit-scrollbar-track
    border-radius 4px
    opacity 0.3
    background-color #E3F2FD
/deep/.CodeMirror-hscrollbar, /deep/.CodeMirror-vscrollbar
  &::-webkit-scrollbar
    height 10px
    width 10px
  &::-webkit-scrollbar-thumb
    border-radius 10px
    background-color #E3F2FD
  &::-webkit-scrollbar-track
    border-radius 10px
    opacity 0.3
    background-color #2b3e50
/deep/.CodeMirror-scrollbar-filler
  height 0!important
  width 0!important

.table-wrapper
  width 100%
  padding 25px
  overflow-x auto
.table-wrapper .table
  width 100%
  border-radius 5px
  border-collapse collapse
  border 1px solid #bdbdbd
  td, th
    height 40px
    text-align center
    border 1px solid #bdbdbd
    color #616161
    min-width 80px
  tr:nth-of-type(2n)
    td, th
      background #eee
.div-wrapper
  width 100%
  padding 25px 50px 25px 50px
.hand
  cursor: pointer;
.upload-icon
  width 25px
.file-path
  margin-top 8px
  font-size 12px
  line-height 20px
.input-card >>> .label-font
  line-height normal
</style>
