<template>
  <f-block>
    <fdev-card flat square class="q-pa-md">
      <fdev-form @submit.prevent="handelModel(pathName)">
        <fdev-card-section class="form">
          <f-formitem diaS label="环境" required>
            <fdev-select
              use-input
              @filter="envFilter"
              ref="modelEnv.env"
              :readonly="path === 'update'"
              v-model="$v.modelEnv.env.$model"
              :options="envOptions"
              option-label="name_en"
              option-value="id"
              :rules="[() => $v.modelEnv.env.required || '请选择环境']"
              :disable="envs.length === 0"
            >
              <template>
                <span class="span-tip" v-show="envs.length === 0">
                  提示：所选实体已和所有环境映射，请重新选择实体！
                </span>
              </template>
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label>
                      {{ scope.opt.name_en }}
                    </fdev-item-label>
                    <fdev-item-label caption>
                      {{ scope.opt.name_cn }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem diaS label="实体" required>
            <fdev-select
              use-input
              @filter="modelFilter"
              :readonly="path !== 'add'"
              ref="modelEnv.model"
              v-model="$v.modelEnv.model.$model"
              :options="modelOptions"
              option-label="name_en"
              option-value="name_cn"
              :rules="[() => $v.modelEnv.model.required || '请选择实体']"
              :disable="models.length === 0"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                    <fdev-item-label caption>
                      {{ scope.opt.name_cn }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
              <template>
                <span class="span-tip" v-show="models.length === 0">
                  提示：所选环境已和所有实体映射，请重新选择环境！
                </span>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem diaS label="描述">
            <fdev-input
              ref="modelEnv.desc"
              type="textarea"
              class="input"
              hint=""
              v-model="$v.modelEnv.desc.$model"
            >
            </fdev-input>
          </f-formitem>
        </fdev-card-section>
        <fdev-separator />
        <div class="q-my-sm">
          <span class="form-header">实体属性</span>
        </div>
        <fdev-markup-table flat v-if="modelEnv.variables.length != 0">
          <thead>
            <tr>
              <th class="head">属性字段</th>
              <th class="head">属性中文名</th>
              <th>属性类型</th>
              <th class="head-last">属性值</th>
            </tr>
          </thead>
        </fdev-markup-table>
        <div class="container q-pt-md">
          <div
            class="row row-margin"
            v-for="(item, index) in $v.modelEnv.variables.$each.$iter"
            :key="index"
          >
            <fdev-input
              type="text"
              class="col-3 q-pb-sm"
              v-model="item.$model.name_en"
              readonly
            />
            <fdev-input
              type="text"
              class="col-3 q-pb-sm"
              v-model="item.$model.name_cn"
              readonly
            />
            <fdev-input
              type="text"
              class="col-2 q-pb-sm"
              :value="
                item.$model.data_type && item.$model.data_type !== ''
                  ? item.$model.data_type
                  : 'string'
              "
              readonly
            />
            <fdev-input
              :ref="`modelEnv.${index}.name_cn`"
              type="text"
              :class="[
                item.$model.require == '1' ? 'require' : 'not-require',
                'input col-key q-pb-sm hidden-input'
              ]"
              :placeholder="placehold(item)"
              :optional="item.$model.require !== '1'"
              v-model="item.value.$model"
              :readonly="
                item.$model.data_type ? item.$model.data_type !== '' : false
              "
              @input="contrast"
              v-if="
                item.$model.data_type
                  ? item.$model.data_type !== 'string'
                  : false
              "
              :rules="[() => item.value.need || `请输入${item.$model.name_cn}`]"
            >
              <div
                v-if="
                  Array.isArray(item.$model.value) &&
                    item.$model.value.length > 0
                "
              >
                <fdev-chip
                  square
                  dense
                  color="gray"
                  class="relative-chip"
                  text-color="black"
                >
                  {{ item.$model.value[0] }}
                  <fdev-tooltip>
                    <div class="tooltip">
                      <p
                        v-for="(val, valkey, i) in item.$model.newValue[0]"
                        :key="i"
                        class="q-ma-xs"
                      >
                        <span v-if="valkey !== 'index' && valkey !== '__index'">
                          {{ valkey }}：{{ val }}
                        </span>
                      </p>
                    </div>
                  </fdev-tooltip>
                </fdev-chip>
                <span v-if="item.$model.value.length > 1" class="relative-chip">
                  ...
                </span>
              </div>
              <fdev-chip
                square
                v-if="
                  !Array.isArray(item.$model.value) && item.$model.value !== ''
                "
                dense
                color="gray"
                class="relative-chip"
                text-color="black"
              >
                {{ item.$model.value }}
                <fdev-tooltip anchor="top middle" self="center middle">
                  <div class="tooltip">
                    <p
                      v-for="(val, valkey, i) in item.$model.newValue"
                      :key="i"
                      class="q-ma-xs"
                    >
                      <span v-if="valkey !== 'id'">
                        {{ valkey }}：{{ val }}
                      </span>
                    </p>
                  </div>
                </fdev-tooltip>
              </fdev-chip>
            </fdev-input>
            <fdev-input
              outlined
              :ref="`modelEnv.${index}.name_cn`"
              type="text"
              :class="[
                item.$model.require == '1' ? 'require' : 'not-require',
                'input col-key q-pb-sm'
              ]"
              :placeholder="placehold(item)"
              :optional="item.$model.require !== '1'"
              v-model="item.value.$model"
              :readonly="
                item.$model.data_type ? item.$model.data_type !== '' : false
              "
              @input="contrast"
              v-else
              :rules="[() => item.value.need || `请输入${item.$model.name_cn}`]"
            />
            <fdev-btn
              flat
              round
              icon="edit"
              class="edit"
              v-if="
                item.$model.data_type ? item.$model.data_type !== '' : false
              "
              @click="selectKeyType(item.$model.data_type, index)"
            >
              <fdev-tooltip anchor="top middle" self="center middle">
                编辑属性映射值
              </fdev-tooltip>
            </fdev-btn>
          </div>
        </div>
        <div class="row justify-center">
          <div class="block">
            <fdev-btn
              dialog
              v-if="isShowExamBtn"
              :disable="disbaleExamBtn"
              type="button"
              label="检测镜像仓库"
              @click="examStore"
              class="q-mb-lg q-mr-lg"
            />
            <fdev-tooltip
              anchor="top middle"
              self="center middle"
              :offest="[-20, 0]"
              v-if="disbaleExamBtn"
            >
              {{ examTips.join(',') }}字段值为空
            </fdev-tooltip>
          </div>
          <div>
            <fdev-tooltip position="top" v-if="examAddbtn">
              <span
                >属性字段，属性中文名，属性类型，属性值必填，且上述字段修改</span
              >
            </fdev-tooltip>
            <fdev-btn
              dialog
              type="submit"
              :label="`${pathName}实体与环境映射`"
              :loading="loading"
              class="q-mb-lg q-mr-lg"
              :disable="examAddbtn"
            />
          </div>
        </div>
      </fdev-form>
    </fdev-card>

    <f-dialog v-model="applyMessageOpened" f-sc title="申请人信息">
      <!-- <Dialog
      v-model="applyMessageOpened"
      title="申请人信息"
      @shake="confirmToClose('applyMessageOpened')"
    > -->
      <div class="q-pa-md bg-white">
        <form @submit.prevent="handleApplyMessage">
          <div class="row">
            <f-formitem diaS label="邮箱">
              <fdev-input
                ref="applyMessage.email_pre"
                v-model="$v.applyMessage.email_pre.$model"
                :rules="[
                  () => $v.applyMessage.email_pre.required || '请输入邮箱地址',
                  () =>
                    $v.applyMessage.email_pre.notFind ||
                    '找不到对应用户,请检查邮箱是否正确',
                  () => $v.applyMessage.email_pre.isLeave || '该用户已离职'
                ]"
                class="col-8"
                @blur="findUser"
              />
              <fdev-select
                ref="applyMessage.email_append"
                v-model="$v.applyMessage.email_append.$model"
                :options="emailAppend"
                :rules="[() => true]"
                class="col-4"
                @input="findUser"
              />
            </f-formitem>
          </div>
          <f-formitem diaS label="中文名">
            <fdev-input
              v-model="$v.applyMessage.apply_username.$model"
              disable
              hint=""
            />
          </f-formitem>
          <f-formitem diaS label="应用">
            <fdev-select
              input-debounce="0"
              use-input
              @filter="appFilter"
              option-label="name_en"
              option-value="id"
              v-model="$v.applyMessage.app_name_en.$model"
              :options="appOptions"
              hint=""
              :rules="[]"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label>{{ scope.opt.name_en }}</fdev-item-label>
                    <fdev-item-label caption>
                      {{ scope.opt.name_zh }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <div v-if="pathName === '更新'">
            <f-formitem diaS label="修改原因">
              <fdev-input
                ref="applyMessage.modify_reason"
                type="textarea"
                v-model="$v.applyMessage.modify_reason.$model"
                :rules="[
                  () =>
                    $v.applyMessage.modify_reason.required || '请输入修改原因'
                ]"
              >
              </fdev-input>
            </f-formitem>
          </div>
        </form>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          label="取消"
          outline
          dialog
          @click="confirmToClose('applyMessageOpened')"
        />
        <fdev-btn
          dialog
          label="确认"
          @click="handleApplyMessage"
          :disable="globalLoading['user/fetch']"
        />
      </template>
    </f-dialog>
    <f-dialog
      v-model="arrayKeyDialogOpened"
      right
      f-sc
      :title="`新增${headerTitle}属性映射值`"
    >
      <fdev-table
        :columns="columns"
        :data="attrMapKey"
        noExport
        no-select-cols
        style="width:1000px"
      >
        <template v-slot:header-cell="props">
          <fdev-th :props="props">
            {{ props.col | filterName }}
            <span class="text-red" v-if="props.col.required">*</span>
            <fdev-icon
              size="18px"
              name="add"
              v-ripple
              color="primary"
              v-if="props.col.name === 'operate'"
              @click="addPVCKey"
              class="cursor"
            />
          </fdev-th>
        </template>
        <template v-slot:body="props">
          <fdev-tr :props="props">
            <fdev-td v-for="td in columns" :key="td.name" class="text-ellipsis">
              <fdev-icon
                size="12px"
                name="edit"
                v-ripple
                color="primary"
                @click.stop
                v-if="td.name !== 'operate'"
                class="cursor"
              >
                <fdev-popup-edit
                  v-model="props.row[td.name]"
                  buttons
                  :validate="val => inputValidation(td, val)"
                  @hide="inputValidationClose"
                  label-set="Save"
                  label-cancel="Close"
                  @before-show="
                    props.row[td.name] = props.row[td.name].replace(/^ /g, '')
                  "
                >
                  <fdev-input
                    v-model="props.row[td.name]"
                    dense
                    autofocus
                    :error="errorProtein"
                    :error-message="errorMessageProtein"
                  />
                </fdev-popup-edit>
              </fdev-icon>
              <span v-else>
                <fdev-icon
                  size="18px"
                  name="delete"
                  v-ripple
                  color="red"
                  @click="handelDelete(props.row.index)"
                  class="cursor"
                />
                <fdev-icon
                  size="18px"
                  name="add"
                  v-ripple
                  color="primary"
                  @click="addPVCKey"
                  class="cursor"
                />
              </span>
              {{ props.row[td.name] }}
            </fdev-td>
          </fdev-tr>
        </template>
      </fdev-table>
      <template v-slot:btnSlot>
        <fdev-btn
          label="返回"
          outline
          dialog
          @click="arrayKeyDialogOpened = false"
        />
        <fdev-btn dialog label="确定" @click="savePVC" />
      </template>
    </f-dialog>
    <f-dialog
      v-model="objKeyDialogOpened"
      right
      f-sc
      :title="`新增${headerTitle}属性映射值`"
    >
      <div v-for="item in mapList" :key="item.label">
        <f-formitem
          bottom-page
          :label="getLabel(item)"
          :required="item.required == '1' ? true : false"
        >
          <fdev-input
            :ref="`attrMapList.${item.key}`"
            v-model="item.value"
            :rules="[
              val => {
                if (item.required && !val) {
                  return `请输入${item.label}`;
                }
              }
            ]"
          />
        </f-formitem>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          label="返回"
          outline
          dialog
          @click="confirmToClose('objKeyDialogOpened')"
        />
        <fdev-btn dialog label="确定" @click="saveMap" />
      </template>
    </f-dialog>
  </f-block>
</template>

<script>
import { mapActions, mapState, mapMutations } from 'vuex';
import { createModelEnv, applyMessage } from '../../utils/constants';
import { required } from 'vuelidate/lib/validators';
import {
  validate,
  deepClone,
  successNotify,
  errorNotify,
  getIdsFormList
} from '@/utils/utils';

export default {
  name: 'modelEnvEdit',
  data() {
    return {
      path: '',
      id: '',
      mapId: '',
      mapList: [],
      modelEnv: createModelEnv(),
      loading: false,
      models: [],
      envs: [],
      envOptions: [],
      modelOptions: [],
      applyMessageOpened: false,
      applyMessage: applyMessage(),
      emailAppend: ['xxx', 'xxx'],
      appOptions: [],
      startFindUser: true,
      isModelValueChange: false,
      isMapValueChange: false,
      isStringValueChange: false,
      isDescChange: false,
      descOldValue: '',
      tips: false,
      timer: null,
      arrayKeyDialogOpened: false,
      objKeyDialogOpened: false,
      json_schema: {},
      attrMapKey: [],
      columns: [],
      errorProtein: false,
      errorMessageProtein: '',
      attrMapList: [],
      changeNum: 0,
      length: 0,
      headerTitle: '',
      examTips: [],
      disbaleExamBtn: false,
      examFlag: false
    };
  },
  validations() {
    return {
      modelEnv: {
        env: {
          required
        },
        model: {
          required
        },
        desc: {},
        variables: {
          required,
          $each: {
            value: {
              need(val, item) {
                if (typeof val === 'undefined') {
                  return true;
                }
                if (item.require === '1') {
                  if (item.data_type && item.data_type === 'array') {
                    return item.keysLength > 0;
                  } else if (item.data_type && item.data_type === 'object') {
                    return Object.keys(item.newValue).every(val => {
                      if (item.required && item.required.includes(val)) {
                        return item.newValue[val];
                      } else {
                        return true;
                      }
                    });
                  } else {
                    if (!val) {
                      return false;
                    }
                    return val.replace(/(^\s*)|(\s*$)/g, '');
                  }
                } else {
                  return true;
                }
              }
            }
          }
        }
      },
      applyMessage: {
        email_pre: {
          required,
          notFind(val) {
            if (!val) return true;
            return this.startFindUser || !!this.applyMessage.apply_username;
          },
          isLeave(val) {
            if (!val || !this.users[0]) return true;
            return this.users[0].status === '0';
          }
        },
        apply_username: {
          required
        },
        modify_reason: {
          required
        },
        app_name_en: {},
        email_append: {
          required
        }
      }
    };
  },
  computed: {
    ...mapState('appForm', ['appData', 'vueAppData']),
    ...mapState('user', {
      currentUser: 'currentUser',
      users: 'list'
    }),
    ...mapState('environmentForm', {
      modelListData: 'modelList', // 实体列表
      envList: 'envList', // 环境列表
      modelEvnList: 'modelEvnList',
      modelEnvDetail: 'modelEnvDetail',
      connectionInfo: 'connectionInfo'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    pathName() {
      return this.path === 'add'
        ? '新增'
        : this.path === 'update'
        ? '更新'
        : '复制';
    },
    examAddbtn() {
      return (
        this.path === 'update' &&
        !this.isModelValueChange &&
        !this.isDescChange &&
        !this.isStringValueChange &&
        !this.isMapValueChange
      );
    },
    isShowExamBtn() {
      let flag = false;
      if (this.modelEnv.variables) {
        const hasReg = this.modelEnv.variables.some(
          item => item.name_en === 'fdev_caas_service_registry'
        );
        const hasUser = this.modelEnv.variables.some(
          item => item.name_en === 'fdev_caas_user'
        );
        const hasPwd = this.modelEnv.variables.some(
          item => item.name_en === 'fdev_caas_pwd'
        );
        if (hasReg && hasUser && hasPwd) {
          flag = true;
        }
      }
      return this.isDce && !this.judgeString && flag;
    },
    judgeString() {
      let flag = false;
      this.modelEnv.variables.forEach(item => {
        let regFlag = item.name_en === 'fdev_caas_service_registry',
          userFlag = item.name_en === 'fdev_caas_user',
          pwdFlag = item.name_en === 'fdev_caas_pwd';
        if (regFlag || userFlag || pwdFlag) {
          if (item.data_type === 'array' || item.data_type === 'object') {
            flag = true;
          }
        }
      });
      return flag;
    },
    isDce() {
      if (
        this.modelEnv.model &&
        this.modelEnv.model.name_en.startsWith('ci_dce_')
      ) {
        return true;
      }
      return false;
    }
  },
  watch: {
    'modelEnv.variables': {
      deep: true,
      handler(val) {
        this.examFlag = false;
        this.modelEnv.variables.forEach(item => {
          let regFlag = item.name_en === 'fdev_caas_service_registry',
            userFlag = item.name_en === 'fdev_caas_user',
            pwdFlag = item.name_en === 'fdev_caas_pwd';
          if (regFlag || userFlag || pwdFlag) {
            this.handleExam(item);
          }
        });
        this.disbaleExamBtn = this.examFlag;
      }
    },
    'modelEnv.model': {
      async handler(val) {
        if (this.path !== 'update') {
          // 不是更新操作的时候，过滤掉已经有的映射
          this.envs = deepClone(this.envList);
          if (!this.modelEnv.model) return;
          await this.getModelEvn({
            model_id: this.modelEnv.model.id
          });
          let env = [];
          let copyVal = {};
          copyVal = this.modelListData.find(item => {
            return item.id === val.id;
          });
          env = this.envs.filter((env, index) => {
            let modelEvn = this.modelEvnList.find(item => {
              return item.env_id === env.id;
            });
            // 当选择 CaaS实体，只展示 CaaS环境；当选择 SCC实体时，只展示SCC环境；当不为CaaS实体也不为SCC实体时，展示所有的环境。
            if (copyVal.platform === 'CaaS') {
              // 选择CaaS实体
              return !modelEvn && env.labels.indexOf('caas') > -1;
            } else if (copyVal.platform === 'SCC') {
              // 选择SCC实体
              return !modelEvn && env.labels.indexOf('scc') > -1;
            } else {
              return !modelEvn;
            }
          });
          this.envs = env;
        }
        if (this.path === 'add') {
          if (this.modelEnv.env) {
            this.queryModelEnvDetail();
          }
        }
      }
    },
    'modelEnv.env': {
      async handler(val) {
        if (this.path === 'add') {
          // 新增操作的时候，过滤掉已经有的映射
          this.models = deepClone(this.modelListData);
          if (!this.modelEnv.env) return;
          await this.getModelEvn({
            env_id: this.modelEnv.env.id
          });
          let model = [];
          model = this.models.filter((model, index) => {
            let modelEvn = this.modelEvnList.find(item => {
              return item.model_id === model.id;
            });
            // 当选择CaaS环境， 展示CaaS实体和一级分类不为“ci”的其他实体；选择SCC环境时，展示SCC实体和一级分类不为“ci”的其他实体。
            let platformCopy = model.platform ? model.platform.split(',') : [];
            let bool = false;
            if (val.labels.indexOf('caas') > -1) {
              platformCopy.forEach(item => {
                if (item.includes('CaaS')) {
                  bool = true;
                }
              });
              return !modelEvn && (model.first_category !== 'ci' || bool);
            } else if (val.labels.indexOf('scc') > -1) {
              platformCopy.forEach(item => {
                if (item.includes('SCC')) {
                  bool = true;
                }
              });
              return !modelEvn && (model.first_category !== 'ci' || bool);
            } else {
              return !modelEvn;
            }
          });
          this.models = model;
          if (this.modelEnv.model) {
            this.queryModelEnvDetail();
          }
        }
      }
    },
    'modelEnv.desc': {
      handler(val) {
        if (this.path !== 'update') return;
        const descOldValue = this.modelEvnList[0].desc
          ? this.modelEvnList[0].desc
          : '';
        this.isDescChange = val !== descOldValue;
      }
    }
  },
  filters: {
    filterName(val) {
      let label = val.label ? '(' + val.label + ')' : '';
      return val.name + label;
    }
  },
  methods: {
    ...mapActions('environmentForm', {
      saveModelEnv: 'saveModelEnv',
      getModelList: 'getModelList', // 查询实体
      getEnvList: 'getEnvList', // 查询环境
      getModelEvn: 'getModelEvn', // 查询实体环境详情
      queryModleEnvDetail: 'queryModleEnvDetail', // 通过环境id和实体id 获取相应的映射属性
      checkConnectionDocker: 'checkConnectionDocker'
    }),
    ...mapActions('user', {
      fetchUser: 'fetch'
    }),
    ...mapActions('appForm', ['queryApplication', 'queryApps']),
    ...mapMutations('userActionSaveEnv/modelEnv', [
      'saveSelectedModel',
      'saveSelectedEnv'
    ]),
    placehold(item) {
      return item.$model.data_type && item.$model.data_type !== ''
        ? ''
        : '请输入属性值';
    },
    async queryModelEnvDetail() {
      let params = {
        env_id: this.modelEnv.env.id,
        model_id: this.modelEnv.model.id
      };
      await this.queryModleEnvDetail(params);
      if (this.modelEnvDetail) {
        this.modelEnv = this.modelEnvDetail;
        this.modelEnv.variables = this.modelEnv.variables.map(item => {
          return { ...item, value: item.value ? item.value : '' };
        });
      } else {
        let model = this.models.find(item => {
          return item.id === this.modelEnv.model.id;
        });
        this.modelEnv.variables = model.env_key.map(item => {
          return { ...item, value: item.value ? item.value : '' };
        });
        this.sortModelKey();
        this.modelEnv.variables.forEach((key, index) => {
          if (key.data_type === 'array') {
            key.newValue = key.value !== '' ? key.value : [];
            if (Array.isArray(key.value)) {
              key.keysLength = key.value.length;
            }
            key.value = '';
          } else if (key.data_type === 'object') {
            // 如果是对象形式，重新组装数据，用数组存起来
            const items = JSON.parse(key.json_schema);
            this.mapId = key.value.id;
            delete key.value.id;
            key.newValue = key.value !== '' ? key.value : {};
            key.required = items.required;
            if (Object.keys(key.newValue).length === 0) {
              Object.keys(items.properties).forEach(item => {
                this.$set(key.newValue, item, '');
              });
            }
            // 将newValue拼凑成需要的形式
            this.attrMapList[index] = Object.keys(key.newValue).map(item => {
              let obj = {
                key: item,
                value: key.newValue[item],
                required: false
              };
              // 默认不必填，以下确定是否必填
              if (item.required && items.required.includes(item)) {
                obj.required = true;
              }
              // 将返回值的key作为label存起来
              Object.keys(items.properties).forEach(prop => {
                if (prop === item) {
                  obj.label = items.properties[prop].description;
                }
              });
              return obj;
            });
            // value属性被删除掉了的话，得加上
            if (!key.value) {
              key.value = [];
            }
          }
        });
      }
    },
    async getDetail() {
      await this.getEnvList();
      if (this.path !== 'copy') {
        this.envs = deepClone(this.envList);
        this.envOptions = deepClone(this.envList);
      }
      await this.getModelList();
      this.models = deepClone(this.modelListData);
      this.modelOptions = deepClone(this.modelListData);
    },
    // 新增、复制 更新 实体与环境映射
    async selectHandle() {
      this.loading = true;
      let cloneModelEnv = deepClone(this.modelEnv);
      cloneModelEnv.variables.map(item => {
        if (Object.keys(item).includes('arrayChanged')) {
          delete item.arrayChanged;
        }
        if (Object.keys(item).includes('changed')) {
          delete item.changed;
        }
        if (item.newValue !== undefined) {
          item.value = item.newValue;
        }
        // value是数组类型的，给后端传值的时候删除index和__index字段
        if (item.value && Array.isArray(item.value) && item.value.length > 0) {
          item.value.forEach(val => {
            delete val.index;
            delete val.__index;
          });
        }
      });
      const params = {
        ...cloneModelEnv,
        apply_user_id: this.users[0] ? this.users[0].id : '',
        apply_username: this.applyMessage.apply_username,
        app_name_en: this.applyMessage.app_name_en
          ? this.applyMessage.app_name_en.name_en
          : '',
        apply_email: this.applyMessage.email_pre
          ? this.applyMessage.email_pre + this.applyMessage.email_append
          : '',
        modify_reason: this.applyMessage.modify_reason,
        type: this.path === 'update' ? 'update' : 'insert',
        model_env_id: this.$route.query.id
      };
      try {
        await this.saveModelEnv(params);
        if (this.tips) {
          successNotify(
            `已录入相关变动申请，需申请人${
              this.applyMessage.apply_username
            }在5天内完成核对后生效`
          );
        } else {
          successNotify('实体与环境映射修改成功');
        }
        this.saveSelectedModel(this.modelEnv.model);
        this.$router.push({
          path: '/envModel/modelEvnList',
          query: {
            env_id: this.modelEnv.env.id,
            model_id: this.modelEnv.model.id
          }
        });
      } finally {
        this.loading = false;
      }
    },
    async handelModel(todo) {
      this.loading = true;
      this.$v.modelEnv.$touch();
      let Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('modelEnv') > -1;
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.modelEnv.$invalid) {
        return;
      }
      //如果修改的是私有实体映射，不走审核流程
      if (
        this.path === 'update' &&
        this.modelEnv &&
        this.modelEnv.first_category === 'private'
      ) {
        this.selectHandle();
      } else {
        this.handleApplyMessageOpened();
      }
      this.loading = false;
    },
    async envFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.envOptions = this.envs.filter(
          v =>
            v.name_cn.toLowerCase().indexOf(needle) > -1 ||
            v.name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    async modelFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.modelOptions = this.models.filter(
          v =>
            v.name_cn.toLowerCase().indexOf(needle) > -1 ||
            v.name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    goBack() {
      this.$router.push('/envModel/modelEvnList');
    },
    async findUser() {
      if (!this.applyMessage.email_pre) return;
      try {
        this.startFindUser = false;
        await this.fetchUser({
          email: this.applyMessage.email_pre + this.applyMessage.email_append
        });
        this.applyMessage.apply_username = this.users[0]
          ? this.users[0].user_name_cn
          : '';
      } catch (e) {
        this.applyMessage.apply_username = '';
      }
      this.$v.applyMessage.email_pre.$touch();
      validate([this.$refs['applyMessage.email_pre']]);
    },
    appFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.appOptions = this.vueAppData.filter(
          v =>
            v.name_en.toLowerCase().includes(needle) ||
            v.name_zh.includes(needle)
        );
      });
    },
    async handleApplyMessageOpened() {
      await this.queryApps();
      this.appOptions = this.vueAppData;
      this.applyMessage = applyMessage();
      this.applyMessageOpened = true;
      this.startFindUser = true;
      this.tips = true;
    },
    handleApplyMessage() {
      this.$v.applyMessage.$touch();
      let applyMessageKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('applyMessage') > -1;
      });
      validate(applyMessageKeys.map(key => this.$refs[key]));
      if (this.$v.applyMessage.$invalid) {
        let _this = this;
        let validateRes = applyMessageKeys.every(item => {
          let itemArr = item.split('.');
          return _this.$v.applyMessage[itemArr[1]].$invalid == false;
        });
        if (!validateRes) {
          return;
        }
      }
      this.applyMessageOpened = false;
      this.selectHandle();
    },
    contrast() {
      if (this.path !== 'update') return;
      if (this.timer) clearTimeout(this.timer);
      this.timer = setTimeout(() => {
        const newValue = this.modelEnv.variables.filter(item => {
          if (!item.data_type || item.data_type === '') {
            return { ...item, value: item.value ? item.value : '' };
          }
        });
        let variables = this.modelEvnList[0].variables.filter(item => {
          if (!item.data_type || item.data_type === '') {
            return { ...item, value: item.value ? item.value : '' };
          }
        });
        const oldValue = variables;
        const resule = newValue.every(item => {
          if (typeof item.value === 'string' && !item.json_schema_id) {
            let obj = oldValue.find(val => val.name_en === item.name_en);
            obj.value = obj.value ? obj.value : '';
            return item.value === obj.value;
          }
          return true;
        });
        this.isStringValueChange = !resule;
      }, 500);
    },
    async selectKeyType(title, index) {
      // 如果当前高级属性是array类型的，将数据处理成table需要的格式
      if (title === 'array') {
        // 传入table的数据
        let newValue = this.modelEnv.variables[index].newValue;
        this.attrMapKey = deepClone(newValue ? newValue : []);
        this.length = this.modelEnv.variables[index].length;
        this.json_schema = JSON.parse(
          this.modelEnv.variables[index].json_schema
        ).items;
        // 以下是生成table的columns
        const columnsArr = Object.keys(this.json_schema.properties);
        this.columns = columnsArr.map(item => {
          let required = false;
          if (this.json_schema.required) {
            required = this.json_schema.required.includes(item);
          }
          return {
            name: item,
            label: this.json_schema.properties[item].description,
            field: item,
            required: required,
            align: 'left'
          };
        });
        this.columns.push({ name: 'operate', label: '操作', align: 'left' });
        this.arrayKeyDialogOpened = true;
        this.headerTitle = this.modelEnv.variables[index].name_en;
        this.index = index;
      } else if (title === 'object') {
        // 如果当前高级属性是object类型的
        // 数据修改了未保存，newValue未改变，但是this.attrMapList[index]是改变的，需要重置数据
        this.modelEnv.variables[index].value = this.modelEnv.variables[
          index
        ].newValue;
        this.getMapList(this.modelEnv.variables[index], index);
        this.mapList = this.attrMapList[index];
        this.headerTitle = this.modelEnv.variables[index].name_en;
        this.objKeyDialogOpened = true;
        this.index = index;
      }
    },
    // 新增一行属性映射值
    addPVCKey() {
      let data = {};
      Object.keys(this.json_schema.properties).forEach(item => {
        if (
          this.json_schema.required &&
          this.json_schema.required.includes(item)
        ) {
          data[item] = ' ';
        } else {
          data[item] = '';
        }
      });
      this.attrMapKey.push(data);
      this.attrMapKey.map((item, index) => (item.index = index));
    },
    handelDelete(index) {
      this.attrMapKey.splice(index, 1);
      // 每删除一行就需要重新赋值一下index
      this.attrMapKey.map((item, index) => (item.index = index));
    },
    saveMap() {
      // 校验表单
      let keys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.startsWith('attrMapList');
      });
      let empty = [];
      keys.map(ele => {
        if (
          Array.isArray(this.$refs[ele]) &&
          this.$refs[ele].length &&
          !this.$refs[ele][0].validate()
        ) {
          empty.push('error');
        }
      });
      if (empty.some(c => c === 'error')) return;
      let value = {};
      this.attrMapList[this.index].forEach(item => {
        value[item.key] = item.value;
      });
      value.id = this.mapId;
      let model = this.modelEnv.variables[this.index];
      model.newValue = value;
      const env_key = this.modelEvnList[0]
        ? this.modelEvnList[0].variables
        : this.models[0].env_key;
      model.value = '';
      let hasValue = Object.keys(model.newValue).some(val => {
        if (model.newValue[val]) {
          return model.newValue[val] !== '';
        }
      });
      if (hasValue) {
        model.value = model.name_en;
      }
      let variables = env_key.map(item => {
        return { ...item, value: item.value ? item.value : '' };
      });
      const obj = variables.find(item => item.name_en === model.name_en);
      if (this.path === 'update') {
        this.compareModel(model.newValue, obj.value, model);
      }
      this.objKeyDialogOpened = false;
    },

    savePVC() {
      // 判断是否有必填项未填，有一个就弹框提示
      // 如果必填项现在的值是' '，也不能通过，还是需要重新修改内容
      let required = this.columns.some(col => {
        if (col.required) {
          return this.attrMapKey.some(item => {
            return Object.keys(item).some(key => {
              if (key === col.name) {
                return !item[key].trim();
              }
            });
          });
        }
      });
      if (required) {
        errorNotify('属性映射值有必填项未填写');
        return;
      }
      let model = this.modelEnv.variables[this.index];
      model.keysLength = this.attrMapKey.length;
      model.newValue = this.attrMapKey;
      if (Array.isArray(model.newValue)) {
        model.value = [];
        model.newValue.forEach((val, index) => {
          model.value.push(model.name_en + index);
        });
      }
      const env_key = this.modelEvnList[0]
        ? this.modelEvnList[0].variables
        : this.models[0].env_key;
      let variables = env_key.map(item => {
        return { ...item, value: item.value ? item.value : '' };
      });
      const obj = variables.find(item => item.name_en === model.name_en);
      if (this.path === 'update') {
        this.compareList(model.newValue, obj, model);
      }
      this.arrayKeyDialogOpened = false;
    },
    compareList(newValue, obj, currentModel) {
      let value = obj.value ? obj.value : [];
      let greatKeyChanged = false;
      if (!obj.value && newValue.length > 0) {
        greatKeyChanged = true;
      } else if (newValue.length !== value.length) {
        greatKeyChanged = true;
      } else if (obj.value) {
        newValue.some((newval, index) => {
          greatKeyChanged = Object.keys(newval).some(item => {
            if (Object.keys(value[index]).includes(item)) {
              return newval[item] !== value[index][item];
            }
          });
          return greatKeyChanged;
        });
      }
      this.modelEnv.variables.find(model => {
        if (greatKeyChanged && currentModel.name_en === model.name_en) {
          model.arrayChanged = true;
        } else if (currentModel.name_en === model.name_en) {
          model.arrayChanged = false;
        }
      });
      this.isModelValueChange = this.modelEnv.variables.some(model => {
        if (model.arrayChanged) {
          return model.arrayChanged === true;
        }
        return false;
      });
    },
    compareModel(newValue, value, currentModel) {
      let greatKeyChanged = false;
      greatKeyChanged = Object.keys(newValue).some(item => {
        if (!value) {
          if (item !== 'id') {
            return newValue[item] !== '';
          }
          return false;
        } else if (Object.keys(value).includes(item)) {
          return newValue[item] !== value[item];
        }
      });
      this.modelEnv.variables.find(model => {
        if (greatKeyChanged && currentModel.name_en === model.name_en) {
          model.changed = true;
        } else if (currentModel.name_en === model.name_en) {
          model.changed = false;
        }
      });
      this.isMapValueChange = this.modelEnv.variables.some(model => {
        if (model.changed) {
          return model.changed === true;
        }
        return false;
      });
    },
    inputValidation(td, val) {
      if (td.required === true && val.trim().length < 1) {
        this.errorProtein = true;
        this.errorMessageProtein = `请输入${td.label}`;
        return false;
      }
      return this.inputValidationClose();
    },
    inputValidationClose() {
      this.errorProtein = false;
      this.errorMessageProtein = '';
      return true;
    },
    getMapList(key, index) {
      let items = JSON.parse(key.json_schema);
      if (key.value) {
        this.mapId = key.value.id;
        delete key.value.id;
        key.newValue = key.value;
        this.attrMapList[index] = Object.keys(key.newValue).map(item => {
          let obj = {
            key: item,
            value: key.newValue[item],
            required: false
          };
          key.value = '';
          let hasValue = Object.keys(key.newValue).some(val => {
            return key.newValue[val] !== '';
          });
          if (hasValue) {
            key.value = key.name_en;
          }
          key.required = items.required;
          // 默认不必填，以下确定是否必填
          if (items.required && items.required.includes(item)) {
            obj.required = true;
          }
          // 将返回值的key作为label存起来
          Object.keys(items.properties).forEach(prop => {
            if (prop === item) {
              obj.label = items.properties[prop].description;
            }
          });
          return obj;
        });
      } else {
        let value = {};
        this.attrMapList[index] = Object.keys(items.properties).map(prop => {
          let obj = {
            key: prop,
            value: '',
            required: false,
            label: items.properties[prop].description
          };
          value[prop] = '';
          key.required = items.required;
          if (items.required && items.required.includes(prop)) {
            obj.required = true;
          }
          return obj;
        });
        key.newValue = value;
        key.value = '';
        let hasValue = Object.keys(key.newValue).some(val => {
          return key.newValue[val] !== '';
        });
        if (hasValue) {
          key.value = key.name_en;
        }
      }
    },
    getArray(key) {
      key.newValue = key.value;
      if (Array.isArray(key.value)) {
        key.keysLength = key.value.length;
        key.length = key.value.length;
        key.value = [];
        key.newValue.forEach((val, index) => {
          key.value.push(key.name_en + index);
        });
      } else {
        key.value = '';
      }
    },
    sortKey(a, b) {
      let key = this.modelEnv.variables;
      // 高级属性排序
      let order = ['array', 'object', ''];
      return order.indexOf(key[a].data_type) - order.indexOf(key[b].data_type);
    },
    sortKeyByRequire(a, b, flag) {
      let key = this.modelEnv.variables;
      // 是否必填排序
      let orderByRequire = ['1', '0'];
      if (flag && key[a].data_type && key[b].data_type) {
        return (
          orderByRequire.indexOf(key[a].require) -
          orderByRequire.indexOf(key[b].require)
        );
      } else if (
        !flag &&
        (!key[a].data_type || key[a].data_type === '') &&
        (!key[b].data_type || key[b].data_type === '')
      ) {
        return (
          orderByRequire.indexOf(key[a].require) -
          orderByRequire.indexOf(key[b].require)
        );
      } else {
        return 0;
      }
    },
    sortModelKey() {
      // 按照data_type排序，将高级属性项列在前面
      let sort = Object.keys(this.modelEnv.variables).sort((a, b) => {
        return this.sortKey(a, b);
      });
      this.modelEnv.variables = sort.map(item => this.modelEnv.variables[item]);
      // 含高级属性的按照是否必填排序，将必填项列在前面 第三个参数true表示是高级属性
      let sortDataTypeByRequire = Object.keys(this.modelEnv.variables).sort(
        (a, b) => {
          return this.sortKeyByRequire(a, b, true);
        }
      );
      this.modelEnv.variables = sortDataTypeByRequire.map(
        item => this.modelEnv.variables[item]
      );
      // 不含高级属性的按照是否必填排序，将必填项列在前面
      let sortByRequire = Object.keys(this.modelEnv.variables).sort((a, b) => {
        return this.sortKeyByRequire(a, b, false);
      });
      this.modelEnv.variables = sortByRequire.map(
        item => this.modelEnv.variables[item]
      );
    },
    getLabel(item) {
      let label = item.label ? '(' + item.label + ')' : '';
      return item.key + label;
    },
    addExamTip(tip) {
      this.examTips.push(tip);
    },
    deletExamTip(tip) {
      const index = this.examTips.indexOf(tip);
      this.examTips.splice(index, 1);
    },
    //examTip去重
    formatExamTips() {
      const set = new Set(this.examTips);
      this.examTips = Array.from(set);
    },
    handleExam(field) {
      if (!field.value) {
        this.examFlag = true;
        this.addExamTip(field.name_cn);
        this.formatExamTips();
      } else if (field.value) {
        if (this.examTips.indexOf(field.name_cn) > -1) {
          this.deletExamTip(field.name_cn);
        }
      }
    },
    async examStore() {
      let fdev_caas_service_registry = '',
        fdev_caas_user = '',
        fdev_caas_pwd = '',
        model_id = this.modelEnv.model.id,
        model_env_id = this.$route.query.id;
      this.modelEnv.variables.forEach(item => {
        if (item.name_en === 'fdev_caas_service_registry') {
          fdev_caas_service_registry = item.value;
        }
        if (item.name_en === 'fdev_caas_user') {
          fdev_caas_user = item.value;
        }
        if (item.name_en === 'fdev_caas_pwd') {
          fdev_caas_pwd = item.value;
        }
      });
      await this.checkConnectionDocker({
        fdev_caas_service_registry,
        fdev_caas_user,
        fdev_caas_pwd,
        model_id,
        model_env_id
      });
      if (this.connectionInfo.indexOf('Login Succeeded') > -1) {
        this.$q.dialog({
          title: '检测成功',
          message: `<span style="color:green">${this.connectionInfo}<span>`,
          html: true,
          cancel: true
        });
      } else {
        this.$q.dialog({
          title: '检测失败',
          message: `<span style="color:red">${this.connectionInfo}<span>`,
          html: true,
          cancel: true
        });
      }
    },
    confirmToClose(key) {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this[key] = false;
        });
    }
  },
  async created() {
    let role_names = this.currentUser.role.map(each => each.name);
    this.path = this.$route.path.split('/')[
      this.$route.path.split('/').length - 1
    ];
    // this.path = this.$route.params.path;
    this.id = this.$route.query.id;
    // bugfix 修复面包屑导航 复制与更新显示混乱
    // this.$route.meta.name = this.pathName + '实体与环境映射';
    if (this.id) {
      // 如果有id，先获取环境和实体列表
      await this.getModelEvn({
        id: this.id
      });
      let data = deepClone(this.modelEvnList[0]);
      // 私有实体修改权限放给应用负责人
      if (data.first_category === 'private' && this.path === 'update') {
        let appEnName = data.model_name_en.split('_')[2];
        await this.queryApplication({ name_en: appEnName });
        let appDetail = this.appData[0];
        if (appDetail) {
          let userIds = getIdsFormList(
            appDetail.dev_managers.concat(appDetail.spdb_managers)
          );
          let isManager = userIds.indexOf(this.currentUser.id) > -1;
          if (!role_names.includes('环境配置管理员') && !isManager) {
            errorNotify('当前用户无权限进入实体环境映射的操作页面');
            this.$router.push('/envModel/modelEvnList');
            return;
          }
        }
      }
      data.model = {
        env_key: data.variables,
        name_cn: data.model,
        name_en: data.model_name_en,
        id: data.model_id
      };
      if (this.path === 'update') {
        data.env = {
          name_cn: data.env,
          name_en: data.env_name_en,
          id: data.env_id
        };
      } else {
        data.env = null;
      }
      this.modelEnv = data;
      //在复制页面，如果实体一级分类为"ci"，二级分类为"dce"，同时后端返回的name_en字段为fdev_caas_pwd或者dev_caas_registry_password，则不显示对应的value值
      if (
        this.path === 'copy' &&
        this.modelEnv.model_name_en.includes('ci_dce_')
      ) {
        this.modelEnv.variables.map((item, key) => {
          if (
            item.name_en === 'fdev_caas_pwd' ||
            item.name_en === 'fdev_caas_registry_password'
          )
            item.value = '';
        });
      }
    } else {
      if (!role_names.includes('环境配置管理员')) {
        errorNotify('当前用户无权限进入实体环境映射的操作页面');
        this.$router.push('/envModel/modelEvnList');
        return;
      }
    }
    this.sortModelKey();
    if (this.path !== 'add') {
      this.modelEnv.variables.forEach((key, index) => {
        if (key.data_type === 'array') {
          key.arrayChanged = false;
          this.getArray(key);
        } else if (key.data_type === 'object') {
          // 如果是对象形式，重新组装数据，用数组存起来
          key.changed = false;
          this.getMapList(key, index);
        }
      });
    }
    this.getDetail();
  }
};
</script>

<style lang="stylus" scoped>
@import '~#/css/variables.styl';
.form
  text-align center
  max-width 645px
  margin 0 auto
  @media screen and (max-width: $sizes.sm)
    margin-left: -24px;
    margin-right: -24px;
.form-header
  font-size 20px
  font-weight 700
  padding-right 50px
// .key-section
//   padding 1% 2%
.require:after
  content '*'
  color red
.q-field
  align-items center
.container >>> .q-field__inner {
  padding: 0 10px;
}
.container >>> .q-field__bottom--animated {
  padding: 0 10px;
}
.head
  width 26%
.head-last
  width 35%
.container
  max-height 1000px
  overflow-y scroll
.container::-webkit-scrollbar
  width 5px
.container::-webkit-scrollbar-track
  background #ededed
.row-margin
  margin-bottom 8px
.not-require:after
  content ''
  padding-right 8px
.span-tip
  transform translate3d(0, 100%, 0)
  position absolute
  right -25px
  bottom 0
  font-size 12px
  color rgba(0,0,0,.54)
  padding-top 5px
.dialog-height
  height 350px
.q-table thead, .q-table tr, .q-table th, .q-table td
  border-color rgba(0,0,0,0)
.radio-div
  width 40px
  height 40px
  position absolute
  top 8px
  left 16px
.col-key
  width 29%
.dialog-width
  min-width 560px
  max-width 100%
.cursor
  cursor pointer
.btn-margin
  margin-top 20px
.edit >>> .text-center
  background white
.edit >>> .q-ripple
  opacity 0
.hidden-input >>> input
  display none
.relative-chip
  position relative
  top 5px
.block
  display inline-block
</style>
