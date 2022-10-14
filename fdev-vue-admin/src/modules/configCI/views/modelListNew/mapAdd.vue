<!-- 新建/编辑映射值抽屉弹窗 -->
<template>
  <f-dialog
    childTitle="风险确认"
    v-model="dialogOpen"
    right
    f-sc
    :title="`${computedTitleType}${env.type}环境映射值`"
    :leftDia="leftDialogOpen"
    @before-show="beforeShow"
    @closeLeftDia="closeLeftDialog"
    @before-close="beforeClose"
  >
    <div class="container">
      <div class="card q-mb-md" v-if="generalProperties.length > 0">
        <div class="bg-blue-1 full-width title-div q-mb-md row">
          <div class="row justify-center items-center title-text">
            {{ entityInfo.templateId ? '普通属性值' : '自定义属性值' }}
          </div>
        </div>
        <div class="general">
          <div
            v-for="(property, index) in $v.generalProperties.$each.$iter"
            :key="property.$model.nameEn"
          >
            <f-formitem
              :label="property.$model.nameCn"
              diaS
              :required="property.$model.required"
              ><fdev-input
                :ref="`generalProperties${index}`"
                class="info-input"
                v-model="property.$model.value"
                :readonly="confirmRiskFirst"
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
      </div>

      <div v-if="advancedProperties.length > 0">
        <div
          v-for="(advancedProperty, ind) in $v.advancedProperties.$each.$iter"
          :key="advancedProperty.$model.nameEn"
          class="card q-mb-md"
        >
          <div class="bg-blue-1 full-width title-div q-mb-md row">
            <div class="row justify-center items-center title-text">
              {{ advancedProperty.$model.nameCn }}
            </div>
          </div>
          <div class="advanced-card">
            <div
              v-for="i in advancedProperty.$model.counts"
              :key="i"
              class="advanced-property q-mb-lg q-px-lg q-py-sm"
            >
              <div
                v-for="(property, index) in advancedProperty.$model.properties"
                :key="property.nameEn"
                class="row"
                :class="index === 0 ? 'q-mt-md' : ''"
              >
                <f-formitem :label="property.nameCn" full-width required
                  ><fdev-input
                    :ref="
                      `advancedProperties${ind}.${i}.advancedProperty${index}`
                    "
                    v-model="property['value' + i]"
                    :readonly="confirmRiskFirst"
                    placeholder="请输入"
                    :rules="[val => !!val || `请输入${property.nameCn}`]"
                /></f-formitem>
              </div>
            </div>
            <div class="q-my-md">
              <f-icon
                name="add_s_o"
                class="text-blue-8 cursor-pointer icon-size-md"
                @click="
                  confirmRiskFirst
                    ? () => {}
                    : addAdvancedProperty(advancedProperties, ind)
                "
              />
              <fdev-tooltip v-if="confirmRiskFirst" position="top">
                请先完成风险确认！
              </fdev-tooltip>
            </div>
          </div>
        </div>
      </div>

      <div class="card" v-if="type === 'add' || type === 'copy'">
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
          <f-formitem
            :label="this.type === 'copy' ? '所有环境' : `${env.type}环境`"
            diaS
            required
            class="q-mb-md"
            ><fdev-select
              ref="env"
              use-input
              @filter="filterEnvOptions"
              v-model="$v.environment.$model"
              :options="environmentList"
              map-options
              emit-value
              :multiple="type === 'copy'"
              :option-label="item => `${item.nameEn}环境`"
              :readonly="confirmRiskFirst"
              :rules="[val => $v.environment.required || '请选择环境']"
          /></f-formitem>
        </div>
      </div>
    </div>

    <template v-slot:btnSlot
      ><fdev-btn
        label="取消"
        :disable="confirmRiskFirst"
        dialog
        outline
        @click="closeDialog"
      />
      <fdev-btn
        label="确定"
        :disable="confirmRiskFirst"
        dialog
        @click="submit"
        :loading="
          type === 'edit'
            ? globalLoading['entityManageConfigForm/updateEntityClass']
            : globalLoading['entityManageConfigForm/addEntityClass']
        "
      /><fdev-tooltip v-if="confirmRiskFirst" position="top">
        请先完成风险确认！
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
          对该实体的改动可能会引起以下应用的流水线或配置文件不可用，请确认风险！
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

          <!-- 确认复选框 -->
          <div class="confirm">
            <fdev-checkbox
              v-model="confirm"
              label="我已仔细核对以上依赖，并知悉修改实体可能带来的影响，依然选择修改实体"
            />
          </div>
        </div>
      </div>
    </template>

    <template v-slot:leftDiaBtnSlot
      ><fdev-btn label="取消" dialog outline @click="closeLeftDialog"/>
      <fdev-btn label="确定" dialog @click="confirmRisk"
    /></template>
  </f-dialog>
</template>

<script>
import { validate, successNotify, deepClone } from '@/utils/utils';
import { deployColumns } from '../../utils/constants';
import { mapState, mapActions } from 'vuex';
import { required } from 'vuelidate/lib/validators';

export default {
  name: 'entityAdd',
  props: {
    value: {
      type: Boolean,
      default: () => false
    },
    type: {
      type: String,
      default: ''
    },
    entityInfo: {
      type: Object,
      default: () => ({})
    },
    env: {
      type: Object,
      default: () => ({
        type: '',
        nameEn: '',
        exist: []
      })
    }
    // envAll: {
    //   type: Array,
    //   default: () => []
    // }
  },
  data() {
    return {
      fields: [],
      propertiesVal: {},
      properties: [],
      originProps: [],
      environment: null,
      allEnvList: [], // 全量环境列表
      environmentList: [], // 环境下拉选项
      leftDialogOpen: false,
      confirmRiskFirst: false,
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
      },
      confirm: false
    };
  },
  validations: {
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
    environment: {
      required
    }
  },
  computed: {
    ...mapState('entityManageConfigForm', [
      'envList',
      'templateDetail',
      // 'configDependency',
      'deployDependency',
      'currentUser'
    ]),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    // 弹窗开关
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
          return '新建';
        case 'copy':
          return '复制';
        default:
          return '编辑';
      }
    },
    // 普通属性计算属性
    generalProperties() {
      const generalPros = this.properties.filter(
        item => item.type === 'string'
      );
      // 新增状态下，初始值为空字符串
      if (this.type === 'add') {
        generalPros.forEach(item => {
          this.$set(item, 'value', '');
        });
      } else {
        // 编辑状态下，初始值回显
        generalPros.forEach(item => {
          this.$set(item, 'value', this.propertiesVal[item.nameEn]);
        });
      }
      return generalPros;
    },
    // 高级属性计算属性
    advancedProperties() {
      const advancedPros = this.properties.filter(
        item => item.type === 'array'
      );
      if (this.type === 'add') {
        advancedPros.forEach(item => {
          this.$set(item, 'counts', 1);
        });
      } else {
        advancedPros.forEach(item => {
          // 找到 properties 中对应的值，记录下有几组数据
          const counts = this.propertiesVal[item.nameEn].length;
          Reflect.set(item, 'counts', counts);
          // 为 properties 塞值，value1、value2、value3......
          for (let i = 0; i < counts; i++) {
            item.properties.forEach(pro => {
              this.$set(
                pro,
                `value${i + 1}`,
                this.propertiesVal[item.nameEn][i][pro.nameEn]
              );
            });
          }
        });
      }
      return advancedPros;
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
    },
    // 配置文件依赖上传参数（不含分页信息）
    // paramsC() {
    //   return {
    //     entityNameEn: this.entityInfo.nameEn,
    //     fields: this.fields
    //   };
    // },
    // 部署依赖上传参数（不含分页信息）
    paramsD() {
      return {
        entityNameEn: this.entityInfo.nameEn,
        fields: this.fields
      };
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
    // 获取初始值
    generalProperties: {
      handler(newVal, oldVal) {
        this.originProps = Array.isArray(newVal) ? deepClone(newVal) : [];
      }
    }
  },
  methods: {
    ...mapActions('entityManageConfigForm', [
      'queryEnvList',
      'queryTemplateById',
      'addEntityClass',
      'updateEntityClass',
      // 'queryConfigDependency',
      'queryDeployDependency'
    ]),
    // 弹窗打开之前
    async beforeShow() {
      if (this.type === 'edit' || this.type === 'copy') {
        this.propertiesVal = this.entityInfo.propertiesValue[this.env.type][
          this.env.nameEn
        ];
      }
      this.properties = this.entityInfo.properties;
      // 查询全量环境
      await this.queryEnvList();
      this.initEnvList();
    },
    // 初始化环境列表，环境下拉选项赋值
    initEnvList() {
      if (this.type === 'add') {
        const arr = this.envList.filter(
          item =>
            item.type === this.env.type && !this.env.exist.includes(item.nameEn)
        );
        this.allEnvList = arr;
        this.environmentList = arr;
      } else if (this.type === 'copy') {
        // 复制情况特殊处理，获取所有环境下的映射值
        const arr = [];
        Object.values(this.entityInfo.propertiesValue).forEach(envTypeObj => {
          arr.push(...Object.keys(envTypeObj));
        });
        const envArr = this.envList.filter(env => !arr.includes(env.nameEn));
        this.allEnvList = envArr;
        this.environmentList = envArr;
      } else if (this.type === 'edit') {
        this.allEnvList = this.envList.slice();
        this.environmentList = this.envList.slice();
      } else {
        this.allEnvList = [];
        this.environmentList = [];
      }
    },
    // 模糊匹配环境列表，赋值环境下拉选项
    filterEnvOptions(val, update, abort) {
      update(() => {
        this.environmentList = this.allEnvList.filter(v =>
          `${v.nameEn}环境`.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    beforeClose() {
      if (this.leftDialogOpen) {
        this.closeLeftDialog();
      }
      this.propertiesVal = {};
      this.properties = [];
      this.fields = [];
      this.environment = null;
    },
    closeDialog() {
      this.beforeClose();
      this.dialogOpen = false;
    },
    // 添加一组高级属性
    addAdvancedProperty(advancedProperties, index) {
      const { counts } = advancedProperties[index];
      this.$set(advancedProperties[index], 'counts', counts + 1);
      this.$forceUpdate();
    },
    // 校验表单是否不合法
    formIsInvalid() {
      this.$v.generalProperties.$touch();
      this.$v.advancedProperties.$touch();
      const keys = Object.keys(this.$refs).filter(
        key =>
          this.$refs[key] &&
          (key.includes('generalProperties') ||
            key.includes('advancedProperties'))
      );
      if (this.type === 'add' || this.type === 'copy') {
        this.$v.environment.$touch();
        keys.push('env');
      }
      validate(
        keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (
        (this.type === 'add' || this.type === 'copy') &&
        this.$v.environment.$invalid
      ) {
        return true;
      }
      return (
        this.$v.generalProperties.$invalid ||
        this.$v.advancedProperties.$invalid
      );
    },
    // 提交表单
    submit() {
      // 校验表单是否不合法
      if (this.formIsInvalid()) {
        return;
      }
      // 当是更新状态时，排查下与原值相比，有无修改
      if (this.type === 'edit') {
        // 获取变化的记录
        const changedProps = this.generalProperties.filter(prop => {
          const propOrigin = this.originProps.find(
            pro => pro.nameEn === prop.nameEn
          );
          return prop.value !== propOrigin.value;
        });
        // 将变化的属性的英文名存入全局变量中
        this.fields = changedProps.map(prop => prop.nameEn);
        // 无变化时直接关闭右侧弹窗
        if (this.fields.length === 0) {
          this.closeDialog();
          return;
          // 有变化时，发起查询配置文件依赖的接口，并打开左侧弹窗
        } else {
          Promise.all([
            this.getDeployDependencyList()
            // this.getConfigFilesDeployList()
          ]).then(() => {
            if (this.deployDependency.count > 0) {
              this.confirmRiskFirst = true;
              this.leftDialogOpen = true;
            } else {
              this.submit2();
            }
          });
        }
        // 新增时，继续新增的操作
      } else {
        this.submit2();
      }
    },
    // 继续新增或修改操作
    async submit2() {
      // 整理上传参数
      const params = {};
      Reflect.set(params, 'id', this.entityInfo.id);
      Reflect.set(params, 'envType', this.env.type);
      if (this.type === 'add') {
        Reflect.set(params, 'envName', this.environment.nameEn);
      } else if (this.type === 'copy') {
        // 复制情况特殊处理
        let envList = [];
        this.environment.forEach(item => {
          envList.push({
            envType: item.type,
            envName: item.nameEn
          });
        });
        Reflect.set(params, 'envs', envList);
        delete params.envType;
      } else {
        Reflect.set(params, 'envName', this.env.nameEn);
      }
      // 对于复杂的属性值对象，拎出来专门写
      const propertiesValue = {};
      // 普通属性直接以键值对的形式放进 propertiesValue
      this.generalProperties.forEach(property => {
        this.$set(propertiesValue, property.nameEn, property.value);
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
            this.$set(arr[i], property.nameEn, property[`value${i + 1}`]);
          }
        });
        // 每处理完一种高级属性，放进 propertiesValue
        this.$set(propertiesValue, advancedProperty.nameEn, arr);
      });
      this.$set(params, 'propertiesValue', propertiesValue);
      if (this.type === 'add') {
        // 发新增实体的接口
        await this.addEntityClass(params);
      } else if (this.type === 'copy') {
        // 发复制实体的接口
        await this.addEntityClass(params);
      } else if (this.type === 'edit') {
        // 发编辑实体的接口
        await this.updateEntityClass(params);
      }
      successNotify(`${this.computedTitleType}映射值成功！`);
      // 刷新实体列表
      this.$emit('refresh');
      // 关闭此弹窗
      this.closeDialog();
    },
    // 关闭左侧弹窗
    closeLeftDialog() {
      this.confirmRiskFirst = false;
      this.confirm = false;
      this.leftDialogOpen = false;
    },
    // 分页查询配置文件依赖
    // getConfigFilesDeployList() {
    //   const params = {
    //     page: this.paginationC.page,
    //     perPage: this.paginationC.rowsPerPage,
    //     ...this.paramsC
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
        ...this.paramsD
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
    // 打开新页面查看应用详情
    async gotoAppDetail(row) {
      const url = this.$router.resolve({
        path: `/app/list/${row.id}`
      });
      window.open(url.href, '_blank');
    },
    // 确定风险后
    confirmRisk() {
      // 未确认时，提示用户必须勾选
      if (!this.confirm) {
        this.$q.dialog({
          title: '温馨提示',
          message: '请先勾选风险评估！'
        });
      } else {
        this.closeLeftDialog();
        this.submit2();
      }
    }
  }
};
</script>
<style lang="stylus" scoped>
@import '../../styles/common.styl';

.container
  position relative
  width 100%
  margin-right 4px
.card
  .title-div
    height 54px
    .title-icon
      width 32px
      height 54px
      margin-left 8px
    .title-text
      height 54px
      margin-left 16px
  .info-input
    width 300px
  .advanced-card
    .advanced-property
      background-color #F7F7F7
      border 1px dashed #BBBBBB
      border-radius 8px
.left-container
  width 580px
  .confirm
    position absolute
    bottom 64px
    right 32px
    z-index 100
.td-desc
  max-width 180px
  overflow hidden
  text-overflow ellipsis
</style>
