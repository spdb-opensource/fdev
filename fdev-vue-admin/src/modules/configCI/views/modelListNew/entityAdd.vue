<!-- 新建实体抽屉弹窗 -->
<template>
  <f-dialog
    childTitle="风险提示"
    v-model="dialogOpen"
    right
    f-sc
    :title="`${computedTitleType}实体`"
    @before-close="beforeClose"
    @before-show="beforeShow"
    @closeLeftDia="closeLeftDialog"
    :leftDia="leftDialogOpen"
  >
    <div class="container">
      <!-- 基础信息 -->
      <div class="card q-mb-md">
        <div class="bg-blue-1 full-width title-div q-mb-md row">
          <div class="title-icon row justify-center items-center">
            <f-icon
              name="version_s_f"
              class="text-blue-8 icon-size-md"
              style="padding:6px"
            />
          </div>
          <div class="row justify-center items-center">
            基础信息
          </div>
        </div>
        <div class="justify-between">
          <!-- 实体英文名 -->
          <f-formitem
            label="实体英文名"
            diaS
            required
            class="front-prefix"
            :help="dependencyDisabled ? '该实体正在被使用，不支持编辑！' : ''"
            ><fdev-input
              ref="entityModel.nameEn"
              v-model="$v.entityModel.nameEn.$model"
              placeholder="请输入"
              @blur="checkNameEn"
              :readonly="confirmRiskFirst || dependencyDisabled"
              :rules="[
                () => $v.entityModel.nameEn.required || '请输入实体英文名',
                () =>
                  $v.entityModel.nameEn.isValid ||
                  '只能输入数字、字母、下划线、横线',
                () =>
                  $v.entityModel.nameEn.includesEnglish ||
                  '必须包含至少一个英文字母',
                () => $v.entityModel.nameEn.noRepeat || `实体英文名重复`
              ]"
            >
            </fdev-input
          ></f-formitem>

          <!-- 实体中文名 -->
          <f-formitem label="实体中文名" diaS required
            ><fdev-input
              ref="entityModel.nameCn"
              v-model="$v.entityModel.nameCn.$model"
              placeholder="请输入"
              @blur="checkNameCn"
              :readonly="confirmRiskFirst"
              :rules="[
                () => $v.entityModel.nameCn.required || '请输入实体中文名',
                () =>
                  $v.entityModel.nameCn.isValid ||
                  '只能输入中英文、数字、下划线',
                () =>
                  $v.entityModel.nameCn.includesChinese || '请至少输入一个中文',
                () => $v.entityModel.nameCn.validLength || '长度不得大于30位',
                () => $v.entityModel.nameCn.noRepeat || `实体中文名重复`
              ]"
          /></f-formitem>

          <!-- 实体类型 -->
          <f-formitem label="实体类型" diaS required
            ><fdev-select
              ref="entityModel.entityTemplate"
              v-model="$v.entityModel.entityTemplate.$model"
              use-input
              :options="entityTempOptions"
              @filter="filterEntityTemp"
              map-options
              emit-value
              :readonly="type !== 'add'"
              option-label="nameCn"
              :rules="[
                val =>
                  $v.entityModel.entityTemplate.required || '请选择实体类型'
              ]"
          /></f-formitem>

          <!-- 属性卡片区域 -->
          <div
            class="properties-card"
            v-if="$v.entityModel.entityTemplate.$model"
          >
            <!-- 普通属性 -->
            <div
              class="general-properties"
              v-if="type === 'add' && generalProperties.length > 0"
            >
              <div class="bg-blue-1 full-width title-div q-mb-md row">
                <div class="row justify-center items-center title-text">
                  普通属性值
                </div>
              </div>
              <div
                v-for="(property, index) in $v.generalProperties.$each.$iter"
                :key="property.$model.nameEn"
                class="general-property row"
              >
                <f-formitem
                  :label="property.$model.nameCn"
                  diaS
                  :required="property.$model.required"
                  ><fdev-input
                    :ref="`generalProperties${index}`"
                    v-model="property.$model.value"
                    placeholder="请输入"
                    :rules="[
                      () =>
                        property.value.myRequired ||
                        `请输入${property.$model.nameCn}`
                    ]"
                    hint=""
                /></f-formitem>
              </div>
            </div>

            <!-- 复制或者编辑时展示卡片 -->
            <div v-if="type !== 'add'">
              <div class="bg-blue-1 full-width title-div q-mb-md row">
                <div class="row justify-center items-center title-text">
                  属性列表
                </div>
              </div>

              <div
                class="customer-property q-py-sm q-px-lg q-mb-lg q-pt-lg"
                v-for="(item, index) in $v.customerProperties.$each.$iter"
                :key="index"
                style="position:relative"
              >
                <!-- 属性英文名 -->
                <f-formitem
                  label="属性英文名"
                  full-width
                  required
                  :help="
                    propertiesfields.includes(item.$model.nameEn)
                      ? '该属性正在被应用的配置文件使用，不支持编辑英文名！'
                      : ''
                  "
                  :class="customerProperties.length > 1 ? 'q-mt-sm' : ''"
                >
                  <fdev-input
                    :ref="`customerProperties.${index}.nameEn`"
                    v-model="item.$model.nameEn"
                    placeholder="请输入"
                    :readonly="
                      !!entityModel.entityTemplate.id ||
                        confirmRiskFirst ||
                        propertiesfields.includes(item.$model.nameEn)
                    "
                    @input="checkOthersName('nameEn', index)"
                    :rules="[
                      () => item.nameEn.required || '属性英文名不能为空',
                      () => item.nameEn.isValid || '只能输入数字、字母、下划线',
                      () =>
                        item.nameEn.includesEnglish ||
                        '必须包含至少一个英文字母',
                      () => item.nameEn.noRepeat || '与已有英文名重复'
                    ]"
                  />
                </f-formitem>

                <!-- 属性中文名 -->
                <f-formitem label="属性中文名" full-width required>
                  <fdev-input
                    :ref="`customerProperties.${index}.nameCn`"
                    v-model="item.$model.nameCn"
                    placeholder="请输入"
                    :readonly="
                      !!entityModel.entityTemplate.id || confirmRiskFirst
                    "
                    @input="checkOthersName('nameCn', index)"
                    :rules="[
                      () => item.nameCn.required || '属性中文名不能为空',
                      () =>
                        item.nameCn.isValid || '只能输入中英文、数字、下划线',
                      () => item.nameCn.includesChinese || '请至少输入一个中文',
                      () => item.nameCn.noRepeat || '与已有中文名重复'
                    ]"
                  />
                </f-formitem>

                <!-- 是否必输 -->
                <f-formitem label="是否必输" full-width required>
                  <fdev-select
                    v-model="item.$model.required"
                    :options="trueOrFalseOptions"
                    placeholder="请输入"
                    :readonly="
                      !!entityModel.entityTemplate.id || confirmRiskFirst
                    "
                    class="select-required"
                    map-options
                    emit-value
                    hint=""
                  />
                </f-formitem>
              </div>
            </div>

            <!-- 高级属性 -->
            <div
              class="advanced-properties"
              v-if="advancedProperties.length > 0"
            >
              <div
                v-for="(advancedProperty, ind) in $v.advancedProperties.$each
                  .$iter"
                :key="advancedProperty.$model.nameEn"
              >
                <div class="bg-blue-1 full-width title-div q-mb-md row">
                  <div class="row justify-center items-center title-text">
                    {{ advancedProperty.$model.nameCn }}
                  </div>
                </div>
                <div
                  class="advanced-property q-py-sm q-px-lg q-mb-lg"
                  v-for="i in advancedProperty.$model.counts"
                  :key="i"
                >
                  <div
                    v-for="(property, index) in advancedProperty.$model
                      .properties"
                    :key="property.nameEn"
                    class="row"
                    :class="index === 0 ? 'q-mt-md' : ''"
                  >
                    <f-formitem :label="property.nameCn" full-width required>
                      <fdev-input
                        :ref="
                          `advancedProperties${ind}.${i}.advancedProperty${index}`
                        "
                        v-model="property['value' + i]"
                        placeholder="请输入"
                        :rules="[val => !!val || `请输入${property.nameCn}`]"
                      />
                    </f-formitem>
                  </div>
                </div>
                <div class="q-my-lg">
                  <f-icon
                    name="add_s_o"
                    class="text-blue-8 cursor-pointer icon-size-md"
                    @click="addAdvancedProperty(advancedProperties, ind)"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 环境 -->
      <div class="card" v-if="type === 'add'">
        <div class="bg-blue-1 full-width title-div q-mb-md row">
          <div class="title-icon row justify-center items-center">
            <f-icon
              name="drawer_s_f"
              class="text-blue-8 icon-size-md"
              style="padding:6px"
            />
          </div>
          <div class="row justify-center items-center">
            选择环境
          </div>
        </div>
        <div class="justify-between">
          <f-formitem label="环境" diaS required class="q-mb-md"
            ><fdev-select
              ref="env"
              use-input
              v-model="$v.env.$model"
              :options="environmentList"
              map-options
              emit-value
              :option-label="item => `${item.nameEn}环境`"
              :rules="[() => $v.env.required || '请选择环境']"
              @filter="filterEnvList"
            >
              <template v-slot:option="props">
                <fdev-item v-bind="props.itemProps" v-on="props.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="props.opt.nameEn + '环境'">
                      {{ props.opt.nameEn + '环境' }}
                    </fdev-item-label>
                    <fdev-item-label caption :title="props.opt.nameCn">
                      {{ props.opt.nameCn }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
        </div>
      </div>
    </div>

    <!-- 底部按钮 -->
    <template v-slot:btnSlot>
      <fdev-btn
        label="依赖分析"
        dialog
        outline
        @click="dependencyAnalyse"
        :disable="confirmRiskFirst"
        v-if="type === 'edit' && deployDependency.count > 0"
      />
      <fdev-btn label="取消" dialog outline @click="closeDialog" />
      <fdev-btn
        label="确定"
        dialog
        :disable="confirmRiskFirst"
        @click="submit"
        :loading="
          globalLoading['entityManageConfigForm/addEntity'] ||
            globalLoading['entityManageConfigForm/copyEntity'] ||
            globalLoading['entityManageConfigForm/updateEntity']
        "
      >
      </fdev-btn>
      <fdev-tooltip v-if="confirmRiskFirst" position="top">
        对该实体的改动可能会引起以下应用的流水线或配置文件不可用，不支持编辑！
      </fdev-tooltip>
    </template>

    <template v-slot:leftDiaContent>
      <div class="left-container">
        <div class="row items-center text-orange-7 q-mb-md">
          <f-icon
            name="alert_t_f"
            class="icon-size-md"
            style="margin-right:8px;padding:5px;color:#EF6C00"
          />
          对该实体的改动可能会引起以下应用的流水线或配置文件不可用，不支持编辑！
        </div>
        <div class="content">
          <!-- 部署依赖列表 -->
          <div v-if="deployDependency.count > 0">
            <fdev-table
              title="部署依赖"
              style="width:100%"
              row-key="pipelineId"
              no-select-cols
              :data="deployTableData"
              :columns="deployColumns"
              :pagination.sync="paginationD"
              @request="onRequestD"
            >
              <!-- 列：应用中文名 -->
              <template v-slot:body-cell-name_zh="props">
                <fdev-td style="max-width:180px">
                  <div
                    class="link text-ellipsis"
                    :title="props.row.name_zh"
                    @click="gotoAppDetail(props.row)"
                    v-if="props.row.name_zh"
                  >
                    {{ props.row.name_zh }}
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ props.row.name_zh }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </div>
                  <div v-else title="-">-</div>
                </fdev-td>
              </template>

              <!-- 列：应用英文名 -->
              <template v-slot:body-cell-name_en="props">
                <fdev-td class="text-ellipsis">
                  <div
                    class="link"
                    :title="props.row.name_en"
                    @click="gotoAppDetail(props.row)"
                    v-if="props.row.name_en"
                  >
                    {{ props.row.name_en }}
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ props.row.name_en }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </div>
                  <div v-else title="-">-</div>
                </fdev-td>
              </template>

              <!-- 列：行内应用负责人 -->
              <template v-slot:body-cell-spdbManagers="props">
                <fdev-td style="white-space:nowrap" class="text-ellipsis">
                  <div :title="props.row.spdbManagers | spdbManagers">
                    {{ props.row.spdbManagers | spdbManagers }}
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ props.row.spdbManagers | spdbManagers }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </div>
                </fdev-td>
              </template>

              <!-- 列：厂商应用负责人 -->
              <template v-slot:body-cell-devManagers="props">
                <fdev-td class="text-ellipsis">
                  <div :title="props.row.devManagers | devManagers">
                    {{ props.row.devManagers | devManagers }}
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ props.row.devManagers | devManagers }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </div>
                </fdev-td>
              </template>

              <!-- 列：流水线名称 -->
              <template v-slot:body-cell-pipelineName="props">
                <fdev-td class="td-desc">
                  <router-link
                    :to="{
                      path: `/configCI/pipelineDetail/${props.row.pipelineId}`
                    }"
                    class="link"
                    target="_blank"
                    :title="props.row.pipelineName"
                    v-if="props.row.pipelineName"
                    >{{ props.row.pipelineName }}
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ props.row.pipelineName }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </router-link>
                  <div v-else title="-">-</div>
                </fdev-td>
              </template>
            </fdev-table>
          </div>

          <!-- 配置文件依赖列表， 暂时不展示 -->
          <!-- <div v-if="configDependency.count > 0" style="margin-top:32px">
            <fdev-table
              title="配置文件依赖"
              class="my-sticky-column-table"
              style="width:100%"
              row-key="id"
              no-select-cols
              :data="configFilesdeployTableData"
              :columns="configFilesdeployColumns"
              :pagination.sync="paginationC"
              @request="onRequestC"
            >
              <template v-slot:body-cell-nameCN="props">
                <fdev-td style="max-width:180px">
                  <div
                    class="link text-ellipsis"
                    :title="props.row.nameCN"
                    @click="gotoAppDetail(props.row)"
                    v-if="props.row.nameCN"
                  >
                    {{ props.row.nameCN }}
                  </div>
                  <div v-else title="-">-</div>
                </fdev-td>
              </template>

              <template v-slot:body-cell-nameEN="props">
                <fdev-td class="text-ellipsis">
                  <div
                    class="link"
                    :title="props.row.nameEN"
                    @click="gotoAppDetail(props.row)"
                    v-if="props.row.nameEN"
                  >
                    {{ props.row.nameEN }}
                  </div>
                  <div v-else title="-">-</div>
                </fdev-td>
              </template>

              <template v-slot:body-cell-spdbManagers="props">
                <fdev-td style="white-space:nowrap" class="text-ellipsis">
                  <div :title="props.row.spdbManagers | spdbManagers">
                    {{ props.row.spdbManagers | spdbManagers }}
                  </div>
                </fdev-td>
              </template>

              <template v-slot:body-cell-devManagers="props">
                <fdev-td class="text-ellipsis">
                  <div :title="props.row.devManagers | devManagers">
                    {{ props.row.devManagers | devManagers }}
                  </div>
                </fdev-td>
              </template>

              <template v-slot:body-cell-gitAddress="props">
                <fdev-td class="td-desc">
                  <a
                    :href="props.row.gitAddress"
                    target="_blank"
                    v-if="props.row.gitAddress"
                  >
                    <span :title="props.row.gitAddress">{{
                      props.row.gitAddress
                    }}</span>
                  </a>
                  <div v-else title="-">-</div>
                </fdev-td>
              </template>
            </fdev-table>
          </div> -->
        </div>
      </div>
    </template>

    <template v-slot:leftDiaBtnSlot>
      <fdev-btn label="确定" dialog @click="closeLeftDialog"
    /></template>
  </f-dialog>
</template>

<script>
import { validate, successNotify, deepClone } from '@/utils/utils';
import {
  createEntityModel,
  trueOrFalseOptions,
  deployColumns
  // configFilesdeployColumns
} from '../../utils/constants';
import { mapState, mapActions } from 'vuex';
import { required } from 'vuelidate/lib/validators';

export default {
  name: 'entityAdd',
  props: {
    type: {
      type: String,
      default: ''
    },
    entityInfo: {
      type: Object,
      default: () => ({})
    },
    value: {
      type: Boolean,
      default: () => false
    }
  },
  data() {
    return {
      entityModel: createEntityModel(),
      entityTempOptions: [], // 实体类型下拉选项
      entityTempList: [], // 全量实体类型
      trueOrFalseOptions, // 是否下拉选项集合
      isNameEnRepeat: false, // 校验具体的条线/系统/应用下，英文名是否重复
      isNameCnRepeat: false, // 校验具体的条线/系统/应用下，中文名是否重复
      env: null, // 环境 model
      systemListData: [], // 系统全量数据
      appListData: [], // 应用全量数据
      envListData: [], // 全局环境变量
      customerProperties: [], // 用户自定义实体 model
      init: false,
      // fields: [], // 删除和编辑时候传入属性旧值
      propertiesfields: [],
      dependencyDisabled: false,
      dependencyDesc: '',
      confirmRiskFirst: false,
      leftDialogOpen: false,
      deployColumns,
      // configFilesdeployColumns,
      // paginationC: {
      //   page: 1,
      //   rowsPerPage: 5,
      //   rowsNumber: 0
      // },
      paginationD: {
        page: 1,
        rowsPerPage: 5,
        rowsNumber: 0
      }
    };
  },
  validations: {
    entityModel: {
      nameEn: {
        required,
        // 校验具体的条线/系统/应用下，英文名是否重复
        noRepeat(val) {
          return val ? !this.isNameEnRepeat : true;
        },
        // 是不是全都是合法字符
        isValid(val) {
          return /^\w[\w-]*$/.test(val);
        },
        includesEnglish(val) {
          return /[a-zA-Z]+/.test(val);
        }
      },
      nameCn: {
        required,
        // 校验具体的条线/系统/应用下，中文名是否重复
        noRepeat(val) {
          return val ? !this.isNameCnRepeat : true;
        },
        // 是不是全都是合法字符
        isValid(val) {
          return /^[\u4e00-\u9fa5\w]+$/.test(val);
        },
        // 包含中文
        includesChinese(val) {
          return /[\u4e00-\u9fa5]/.test(val);
        },
        // 长度是否不大于 30
        validLength(val) {
          return val.length <= 30;
        }
      },
      entityTemplate: {
        required
      }
    },
    env: {
      required
    },
    advancedProperties: {
      $each: {
        properties: {
          $each: {}
        }
      }
    },
    generalProperties: {
      $each: {
        value: {
          myRequired(val, property) {
            return property.required ? required(val) : true;
          }
        }
      }
    },
    customerProperties: {
      $each: {
        nameEn: {
          required,
          // 是不是全都是合法字符
          isValid(val) {
            return /^\w+$/.test(val);
          },
          noRepeat(val) {
            const [index, lastIndex] = [
              this.customerPropertiesNameEns.indexOf(val),
              this.customerPropertiesNameEns.lastIndexOf(val)
            ];
            return index === lastIndex;
          },
          includesEnglish(val) {
            return /[a-zA-Z]+/.test(val);
          }
        },
        nameCn: {
          required,
          // 是不是全都是合法字符
          isValid(val) {
            return /^[\u4e00-\u9fa5\w]+$/.test(val);
          },
          // 包含中文
          includesChinese(val) {
            return /[\u4e00-\u9fa5]/.test(val);
          },
          noRepeat(val) {
            const [index, lastIndex] = [
              this.customerPropertiesNameCns.indexOf(val),
              this.customerPropertiesNameCns.lastIndexOf(val)
            ];
            return index === lastIndex;
          }
        }
      }
    }
  },
  computed: {
    ...mapState('entityManageConfigForm', [
      'templateList',
      'envList',
      'systemList',
      'appList',
      'userGroups',
      'copyEntityId',
      'checkEntityFlag',
      // 'configDependency',
      'deployDependency',
      'currentUser'
    ]),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    // 弹窗打开或关闭
    dialogOpen: {
      get() {
        return this.value;
      },
      set(val) {
        this.$emit('input', val);
      }
    },
    // 操作类型
    computedTitleType() {
      switch (this.type) {
        case 'add':
          return '新增';
        case 'copy':
          return '复制';
        default:
          return '编辑';
      }
    },
    // 环境全量数据
    environmentList() {
      // return this.envList;
      return this.envListData;
    },
    // 普通属性值数组
    generalProperties() {
      if (
        !this.entityModel.entityTemplate ||
        Object.keys(this.entityModel.entityTemplate).length === 0
      ) {
        return [];
      }
      return this.entityModel.entityTemplate.properties.filter(
        item => item.type === 'string'
      );
    },
    // 高级属性值数组
    advancedProperties() {
      if (
        !this.entityModel.entityTemplate ||
        Object.keys(this.entityModel.entityTemplate).length === 0
      ) {
        return [];
      }
      const advancedPros = this.entityModel.entityTemplate.properties.filter(
        item => item.type === 'array'
      );
      advancedPros.forEach(item => {
        Reflect.set(item, 'counts', 1);
      });
      return advancedPros;
    },
    // 自定义实体属性的全量英文名，用来做本地重复校验
    customerPropertiesNameEns() {
      return this.customerProperties.map(item => item.nameEn);
    },
    // 自定义实体属性的全量中文名，用来做本地重复校验
    customerPropertiesNameCns() {
      return this.customerProperties.map(item => item.nameCn);
    },
    // 配置文件依赖列表数据
    // configFilesdeployTableData() {
    //   return this.configDependency.serviceList
    //     ? this.configDependency.serviceList.slice()
    //     : [];
    // },
    // 部署依赖列表数据
    deployTableData() {
      return this.deployDependency.serviceList
        ? this.deployDependency.serviceList.slice()
        : [];
    }
  },
  filters: {
    spdbManagers(val) {
      return val.length === 0 ? '-' : val.map(item => item.nameCn).join(',');
    },
    devManagers(val) {
      return val.length === 0 ? '-' : val.map(item => item.nameCn).join(',');
    }
  },
  watch: {
    'entityModel.entityTemplate': {
      handler: function(val) {
        if (this.type !== 'add') {
          // 复制回填卡片属性数组，只要是复制，empty => false
          this.customerProperties =
            this.entityModel.entityTemplate &&
            this.entityModel.entityTemplate.properties;
        } else {
          this.customerProperties = [
            { nameEn: '', nameCn: '', type: 'string', required: true }
          ];
        }
      }
    },
    dialogOpen(newVal, oldVal) {
      if (newVal && !oldVal) {
        this.init = true;
      }
    }
  },
  methods: {
    ...mapActions('entityManageConfigForm', [
      'queryTemplate',
      'queryEnvList',
      'addEntity',
      'copyEntity',
      'updateEntity',
      'checkEntity',
      // 'queryConfigDependency',
      'queryDeployDependency'
    ]),
    // 系统/应用模糊搜索环境
    filterEnvList(val, update, abort) {
      update(() => {
        this.envListData = this.envList.filter(
          item =>
            item.nameCn.includes(val) ||
            item.nameEn.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    // 模糊匹配实体类型，赋值实体类型下拉选项
    filterEntityTemp(val, update, abort) {
      update(() => {
        this.entityTempOptions = this.entityTempList.filter(
          v =>
            v.nameCn.includes(val) ||
            v.nameCn.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    // 初始化实体类型列表，实体类型下拉选项赋初始值
    initEntityTempList() {
      if (this.templateList.length === 0) {
        this.entityTempOptions = [];
        this.entityList = [];
      } else {
        this.entityTempOptions = this.templateList.slice();
        this.entityTempList = this.templateList.slice();
      }
    },
    // 弹窗打开之前
    async beforeShow() {
      this.customerProperties = [
        { nameEn: '', nameCn: '', type: 'string', required: true }
      ];
      if (this.type === 'add') {
        this.entityModel = createEntityModel();
      }
      // 查询全量实体模板
      await this.queryTemplate({
        page: 0,
        pageSize: 0
      });
      this.initEntityTempList();
      // 查询全量环境
      this.queryEnvList().then(
        () => (this.envListData = deepClone(this.envList))
      );
      if (this.type !== 'add') {
        this.entityModel.nameCn =
          this.entityInfo.nameCn + (this.type === 'copy' ? '_copy' : '');
        this.entityModel.nameEn =
          this.entityInfo.nameEn + (this.type === 'copy' ? '_copy' : '');
        this.entityModel.entityTemplate = {
          nameCn: this.entityInfo.templateName,
          id: this.entityInfo.templateId,
          // nameEn: this.entityInfo.templateName,
          properties: deepClone(this.entityInfo.properties)
        };
        if (this.type === 'edit') {
          Promise.all([
            this.getDeployDependencyList()
            // this.getConfigFilesDeployList1()
          ]).then(() => {
            if (this.entityInfo.templateId) {
              //非自定义实体判断逻辑
              //判断部署依赖不为空则不能修改 范围  实体英文名  自定义实体则所有属性属性英文名都不可编辑
              //判断配置依赖不为空则不能修改范围实体英文名 自定义实体 则判断响应报文里面的 fields 所包含的属性英文名不可编辑;
              if (this.deployDependency.count > 0) {
                this.dependencyDisabled = true;
                return;
              }
              // if (this.configDependency.count > 0) {
              //   this.dependencyDisabled = true;
              // }
            }
          });
        }
      }
      // 中英文名校验
      if (this.entityModel.nameEn) {
        this.checkNameEn();
      }
      if (this.entityModel.nameCn) {
        this.checkNameCn();
      }
    },
    // 弹窗关闭之前的回调
    beforeClose() {
      if (this.leftDialogOpen) {
        this.closeLeftDialog();
      }
      this.entityModel = createEntityModel();
      this.isNameEnRepeat = false;
      this.isNameCnRepeat = false;
      this.init = false;
      this.env = null;
      this.dependencyDisabled = false;
      this.propertiesfields = [];
    },
    // 关闭弹窗
    closeDialog() {
      this.beforeClose();
      this.dialogOpen = false;
    },
    // 关闭左侧弹窗
    closeLeftDialog() {
      this.confirmRiskFirst = false;
      this.confirm = false;
      this.leftDialogOpen = false;
    },
    dependencyAnalyse() {
      Promise.all([
        this.getDeployDependencyList()
        // this.getConfigFilesDeployList()
      ]).then(() => {
        this.leftDialogOpen = true;
        this.confirmRiskFirst = true;
      });
    },
    // 自定义实体属性中的中/英文名的本地重复校验
    checkOthersName(type, index) {
      this.$v.customerProperties.$touch();
      const keys = Object.keys(this.$refs).filter(
        key =>
          key.includes('customerProperties') &&
          this.$refs[key].length > 0 &&
          key.includes(type) &&
          key !== `customerProperties.${index}.${type}` &&
          this.$refs[key][0].value // 对于其他的输入框，只有当不为空的时候才校验
      );
      validate(
        keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
    },
    // 添加一组高级属性
    addAdvancedProperty(advancedProperties, index) {
      advancedProperties[index].counts++;
      this.$forceUpdate();
    },
    // 在选择了具体的条线/系统/应用后，校验英文名是否唯一
    async checkNameEn() {
      const { nameEn } = this.$v.entityModel;
      if (
        this.type === 'edit' &&
        this.entityModel.nameEn === this.entityInfo.nameEn
      ) {
        // 编辑操作时，修改值等于原值则不校验
        this.isNameEnRepeat = false;
        this.$v.entityModel.$touch();
        this.$refs['entityModel.nameEn'] &&
          this.$refs['entityModel.nameEn'].validate();
        return;
      }
      if (!nameEn.required || !nameEn.includesEnglish || !nameEn.isValid)
        return;
      const params = {
        nameEn: this.entityModel.nameEn // 前缀参与校验
      };
      await this.checkEntity(params);
      this.isNameEnRepeat = this.checkEntityFlag.nameEnFlag;
      this.$v.entityModel.$touch();
      this.$refs['entityModel.nameEn'] &&
        this.$refs['entityModel.nameEn'].validate();
    },
    // 在选择了具体的条线/系统/应用后，校验中文名是否唯一
    async checkNameCn() {
      const { nameCn } = this.$v.entityModel;
      if (
        this.type === 'edit' &&
        this.entityModel.nameCn === this.entityInfo.nameCn
      ) {
        // 编辑操作时，修改值等于原值则不校验
        this.isNameCnRepeat = false;
        this.$v.entityModel.$touch();
        this.$refs['entityModel.nameCn'] &&
          this.$refs['entityModel.nameCn'].validate();
        return;
      }
      if (
        !nameCn.required ||
        !nameCn.includesChinese ||
        !nameCn.isValid ||
        !nameCn.validLength
      )
        return;
      const params = {
        nameCn: this.entityModel.nameCn
      };
      await this.checkEntity(params);
      this.isNameCnRepeat = this.checkEntityFlag.nameCnFlag;
      this.$v.entityModel.$touch();
      this.$refs['entityModel.nameCn'] &&
        this.$refs['entityModel.nameCn'].validate();
    },
    // 查询配置文件依赖
    // getConfigFilesDeployList1() {
    //   const params = {
    //     entityNameEn: this.entityInfo.nameEn,
    //     fields: []
    //   };
    //   return this.queryConfigDependency(params).then(() => {
    //     this.paginationC.rowsNumber = this.configDependency.count;
    //   });
    // },
    // 分页查询配置文件依赖
    // getConfigFilesDeployList() {
    //   const params = {
    //     page: this.paginationC.page,
    //     perPage: this.paginationC.rowsPerPage,
    //     entityNameEn: this.entityInfo.nameEn,
    //     fields: []
    //     // ...this.paramsC
    //   };
    //   return this.queryConfigDependency(params).then(() => {
    //     this.paginationC.rowsNumber = this.configDependency.count;
    //   });
    // },
    // 分页查询部署依赖
    getDeployDependencyList() {
      const params = {
        page: this.paginationD.page,
        perPage: this.paginationD.rowsPerPage,
        entityNameEn: this.entityInfo.nameEn,
        fields: []
        // ...this.paramsD
      };
      return this.queryDeployDependency(params).then(() => {
        this.paginationD.rowsNumber = this.deployDependency.count;
      });
    },
    // 分页查询配置文件依赖
    // onRequestC(props) {
    //   const { page, rowsPerPage } = props.pagination;
    //   this.paginationC.page = page;
    //   this.paginationC.rowsPerPage = rowsPerPage;
    //   this.getConfigFilesDeployList();
    // },
    // 分页查询部署依赖
    onRequestD(props) {
      const { page, rowsPerPage } = props.pagination;
      this.paginationD.page = page;
      this.paginationD.rowsPerPage = rowsPerPage;
      this.getDeployDependencyList();
    },
    async gotoAppDetail(row) {
      const url = this.$router.resolve({
        path: `/app/list/${row.id}`
      });
      window.open(url.href, '_blank');
    },
    // 校验表单是否不合法
    async formIsInvalid() {
      let keys = Object.keys(this.$refs).filter(
        key => this.$refs[key] && key.includes('entityModel')
      );
      this.$v.entityModel.$touch();
      if (this.entityModel.nameEn) {
        await this.checkNameEn();
      }
      if (this.entityModel.nameCn) {
        await this.checkNameCn();
      }
      if (this.type === 'add') {
        if (this.generalProperties.length > 0) {
          // 普通属性值
          this.$v.generalProperties.$touch();
          this.$v.env.$touch();
          const ks = Object.keys(this.$refs).filter(
            key => this.$refs[key] && key.includes('generalProperties')
          );
          ks.push('env');
          keys = keys.concat(ks);
        } else {
          // 高级属性值
          this.$v.advancedProperties.$touch();
          this.$v.env.$touch();
          const ks = Object.keys(this.$refs).filter(
            key => this.$refs[key] && key.includes('advancedProperties')
          );
          ks.push('env');
          keys = keys.concat(ks);
        }
      }
      // 新增情况时才校验自定义实体，普通属性值，高级属性值
      if (this.type !== 'add' && !this.entityInfo.templateId) {
        this.$v.customerProperties.$touch();
        const ks = Object.keys(this.$refs).filter(
          key => this.$refs[key] && key.includes('customerProperties')
        );
        keys = keys.concat(ks);
      }
      validate(
        keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.entityModel.$invalid) return true;
      // 新增情况时才校验自定义实体，普通属性值，高级属性值
      if (this.type === 'add') {
        if (this.generalProperties.length > 0) {
          return this.$v.generalProperties.$invalid || this.$v.env.$invalid;
        } else {
          return this.$v.advancedProperties.$invalid || this.$v.env.$invalid;
        }
      }
      if (this.type !== 'add' && !this.entityInfo.templateId) {
        return this.$v.customerProperties.$invalid;
      }
      return false;
    },
    // 提交表单
    async submit() {
      // 校验表单是否不合法
      if (await this.formIsInvalid()) {
        return;
      }
      // 整理上传参数
      const params = {};
      Reflect.set(params, 'nameEn', this.entityModel.nameEn);
      Reflect.set(params, 'nameCn', this.entityModel.nameCn);
      if (this.type === 'add') {
        // 不是自定义实体的时候，才上传环境
        Reflect.set(params, 'envType', this.env.type);
        Reflect.set(params, 'envName', this.env.nameEn);
        Reflect.set(params, 'templateId', this.entityModel.entityTemplate.id);
        // 对于复杂的属性值对象，拎出来专门写
        const propertiesValue = {};
        // 普通属性直接以键值对的形式放进 propertiesValue
        this.generalProperties.forEach(property => {
          Reflect.set(propertiesValue, property.nameEn, property.value);
        });
        // 放高级属性
        this.advancedProperties.forEach(advancedProperty => {
          // 获取用户对同一个高级属性填写了几套
          const length = Object.keys(advancedProperty.properties[0]).length - 2;
          // 有几套数据就建相应长度的数组，里面初始都是空对象
          const arr = new Array(length);
          arr.fill({});
          // 遍历每种高级属性，把里面存在的多套值按照顺序依次放入 arr
          advancedProperty.properties.forEach((property, index) => {
            for (let i = 0; i < length; i++) {
              Reflect.set(arr[i], property.nameEn, property[`value${i + 1}`]);
            }
          });
          // 每处理完一种高级属性，放进 propertiesValue
          Reflect.set(propertiesValue, advancedProperty.nameEn, arr);
        });
        Reflect.set(params, 'propertiesValue', propertiesValue);
        await this.addEntity(params);
        successNotify('新增实体成功！');
        // 刷新实体列表
        this.$emit('refresh');
        this.closeDialog();
      } else {
        let propertiesList = [];
        this.customerProperties.forEach(property => {
          propertiesList.push({
            oldNameEn: property.nameEn,
            oldNameCn: property.nameCn,
            oldRequired: property.required,
            newNameEn: property.nameEn,
            newNameCn: property.nameCn,
            newRequired: property.required
          });
        });
        if (this.type === 'copy') {
          // 复制操作
          Reflect.set(params, 'properties', propertiesList);
          Reflect.set(params, 'copyId', this.entityInfo.id);
          await this.copyEntity(params);
          // this.$emit('showMapConfig', { id: this.copyEntityId.id });
          successNotify('复制实体成功！');
          this.$emit('refresh');
          this.closeDialog();
        } else if (this.type === 'edit') {
          // 编辑操作
          Reflect.set(params, 'propertiesList', propertiesList);
          Reflect.set(params, 'id', this.entityInfo.id);
          await this.updateEntity(params);
          successNotify('编辑实体成功！');
          this.$emit('refresh');
          this.closeDialog();
        }
      }
    }
  }
};
</script>
<style lang="stylus" scoped>
@import '../../styles/common.styl';

.container
  width 100%
  padding-right 4px
.card
  .title-div
    .title-icon
      width 32px
      height 54px
      margin-left 8px
    .title-text
      height 54px
      margin-left 16px
  .properties-card
    .advanced-properties
      .advanced-property
        width 378px
        background-color #F7F7F7
        border 1px dashed #BBBBBB
        border-radius 8px
  .customer-property
    width 378px
    background-color #F7F7F7
    border 1px dashed #BBBBBB
    border-radius 8px
    .customer-card-top
      position absolute
      right 2px
      top 4px
      width 24px
      height 24px
.select-option-width
  max-width 263px
  overflow hidden
  text-overflow ellipsis
.left-container
  width 580px
  .confirm
    position absolute
    bottom 64px
    right 32px
    z-index 100
.td-desc
  max-width 100px
  white-space nowrap
  overflow hidden
  text-overflow ellipsis
.front-prefix .q-field__before, .q-field__prepend
  padding-right 0
</style>
