<template>
  <f-block>
    <fdev-card flat square class="q-pa-md">
      <form @submit.prevent="handelModel(pathName)">
        <div class="padding-card">
          <fdev-card-section>
            <f-formitem
              label="实体中文名"
              required
              bottom-page
              label-style="width:80px"
              class="q-pr-sm"
            >
              <fdev-input
                ref="model.name_cn"
                type="text"
                v-model="$v.model.name_cn.$model"
                :rules="[
                  () => $v.model.name_cn.required || '请输入实体中文名',
                  () => $v.model.name_cn.includeChinese || '请至少输入一个中文',
                  () => $v.model.name_cn.isUnique || '中文名不能重复'
                ]"
              >
              </fdev-input>
            </f-formitem>
            <f-formitem
              label="一级分类"
              required
              bottom-page
              label-style="width:80px"
              class="q-pr-sm"
            >
              <fdev-select
                ref="model.first_category"
                type="text"
                v-model="$v.model.first_category.$model"
                :readonly="path === 'update'"
                :options="first_categorys"
                :rules="[
                  () => $v.model.first_category.required || '请选择一级分类'
                ]"
              >
              </fdev-select>
            </f-formitem>
            <f-formitem
              label="二级分类"
              required
              bottom-page
              label-style="width:80px"
              class="q-pr-sm"
            >
              <fdev-select
                ref="model.second_category"
                type="text"
                v-model="$v.model.second_category.$model"
                :readonly="path === 'update'"
                :options="second_categorys"
                :rules="[
                  () => $v.model.second_category.required || '请选择二级分类'
                ]"
              >
              </fdev-select>
            </f-formitem>
            <f-formitem
              diaS
              label="后缀名"
              required
              bottom-page
              label-style="width:80px"
              class="q-pr-sm"
            >
              <fdev-input
                ref="model.suffix_name"
                type="text"
                v-model="$v.model.suffix_name.$model"
                :readonly="path === 'update'"
                :rules="[
                  () => $v.model.suffix_name.required || '请输入后缀名',
                  () =>
                    $v.model.suffix_name.onlyEnAndNum || '只能输入字母、数字和-'
                ]"
              >
              </fdev-input>
            </f-formitem>
            <f-formitem
              diaS
              label="实体英文名"
              required
              bottom-page
              label-style="width:80px"
              class="q-pr-sm"
            >
              <fdev-input
                ref="model.name_en"
                type="text"
                v-model="$v.model.name_en.$model"
                readonly
                :rules="[
                  () =>
                    $v.model.name_en.isUnique ||
                    '实体英文名重复，它由实体类型、分类和后缀名组成。'
                ]"
              >
              </fdev-input>
            </f-formitem>
            <f-formitem
              diaS
              label="实体模板"
              bottom-page
              label-style="width:80px"
              class="q-pr-sm"
            >
              <fdev-select
                :readonly="path === 'update' || path === 'copy'"
                use-input
                clearable
                v-model="modelTemplateId"
                :options="modelOptions"
                option-label="nameEn"
                hint=""
                @filter="modelFilterChoice"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label>{{ scope.opt.nameEn }}</fdev-item-label>
                      <fdev-item-label caption>
                        {{ scope.opt.nameCn }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
            </f-formitem>
            <f-formitem
              diaS
              label="作用域"
              required
              bottom-page
              label-style="width:80px"
              class="q-pr-sm"
            >
              <fdev-select
                ref="model.scope"
                type="text"
                v-model="$v.model.scope.$model"
                :options="scope"
                :rules="[() => $v.model.scope.required || '请输入作用域']"
              >
              </fdev-select>
            </f-formitem>
            <f-formitem
              diaS
              label="实体版本"
              required
              bottom-page
              label-style="width:80px"
              class="q-pr-sm"
            >
              <fdev-input
                ref="model.version"
                type="text"
                readonly
                v-model="$v.model.version.$model"
                :rules="[() => $v.model.version.required || '请输入实体版本']"
              >
              </fdev-input>
            </f-formitem>
            <f-formitem
              diaS
              label="实体描述"
              bottom-page
              label-style="width:80px"
              class="q-pr-sm"
            >
              <fdev-input
                ref="model.desc"
                type="textarea"
                hint=""
                v-model="$v.model.desc.$model"
              >
              </fdev-input>
            </f-formitem>
            <f-formitem
              diaS
              label="适用平台"
              required
              bottom-page
              label-style="width:80px"
              class="q-pr-sm"
              v-if="model.first_category === 'ci'"
            >
              <fdev-select
                ref="model.platform"
                type="text"
                multiple
                :readonly="path === 'update'"
                v-model="$v.model.platform.$model"
                :options="platform"
                :rules="[() => $v.model.platform.required || '请输入适用平台']"
              >
              </fdev-select>
            </f-formitem>
          </fdev-card-section>
        </div>
        <fdev-separator />
        <div class="form-header q-my-sm">
          实体属性
          <fdev-btn
            color="primary"
            label="添加"
            flat
            :disable="disab"
            @click="addEnvKey"
          />
        </div>
        <fdev-markup-table flat v-if="model.env_key.length != 0">
          <thead class="thead">
            <tr class="trsty">
              <th>属性字段</th>
              <th>属性中文名</th>
              <th class="desc">描述信息</th>
              <th>属性类型</th>
              <th class="require" v-if="!model.model_template_id">是否必填</th>
              <th class="require" v-if="!model.model_template_id">
                是否应用独有
              </th>
              <th class="handel">操作</th>
            </tr>
          </thead>
        </fdev-markup-table>
        <div class="container q-pt-md">
          <div
            class="row row-margin"
            v-for="(item, index) in $v.model.env_key.$each.$iter"
            :key="index"
          >
            <fdev-input
              :ref="`model.${index}.name_en`"
              type="text"
              :readonly="model.env_key[index].id !== '' && path !== 'add'"
              placeholder="请输入属性字段"
              class="input q-pb-sm col-2"
              v-model="item.name_en.$model"
              :rules="[
                () => item.name_en.alphaNumAndLine || '只能输入字母、数字和_',
                () => item.name_en.required || '请输入属性字段',
                () => item.name_en.isUnique || '同一实体属性字段不能重复'
              ]"
            />
            <fdev-input
              :ref="`model.${index}.name_cn`"
              type="text"
              placeholder="请输入属性中文名"
              class="input q-pb-sm col-2 input-padding"
              v-model="item.name_cn.$model"
              :rules="[
                () => item.name_cn.required || '请输入属性中文名',
                () => item.name_cn.includeChinese || '请至少输入一个中文',
                () => item.name_cn.isUnique || '同一实体属性中文名不能重复'
              ]"
            />
            <fdev-input
              :ref="`model.${index}.desc`"
              type="text"
              placeholder="请输入描述信息"
              class="input q-pb-sm col-3"
              v-model="item.desc.$model"
            >
            </fdev-input>
            <div class="col-chip">
              <div
                v-for="radio in radioOption"
                :key="radio.value"
                class="span-display"
              >
                <fdev-btn
                  @click="selectKeyType(item, index, radio.value)"
                  flat
                  class="padding-btn"
                  style="text-transform:uppercase"
                  :disable="disableRadio(item, radio.value) || disab"
                >
                  <fdev-chip
                    :outline="item.data_type.$model !== radio.value"
                    square
                    :color="
                      item.data_type.$model === radio.value ? 'teal' : 'green'
                    "
                    dense
                    text-color="white"
                    size="xs"
                  >
                    {{ radio.value }}
                  </fdev-chip>
                </fdev-btn>
              </div>
            </div>
            <fdev-checkbox
              v-if="!model.model_template_id"
              v-model="item.require.$model"
              true-value="1"
              false-value="0"
              class="q-pb-sm require-margin"
              @input="val => toggleRequire(val, item.$model)"
              :disable="
                (item.$model.disable && item.data_type.$model !== 'string') ||
                  (!Object.keys(model.env_key[index]).includes('flag') &&
                    path !== 'add' &&
                    item.data_type.$model !== 'string')
              "
            />
            <fdev-checkbox
              v-if="!model.model_template_id"
              v-model="item.type.$model"
              true-value="1"
              false-value="0"
              class="q-pb-sm require-margin"
            />
            <fdev-btn
              class="require-margin button q-pb-sm"
              :disable="disab"
              flat
              label="删除"
              color="red"
              @click="handelDelete(index)"
            />
          </div>
        </div>
        <div class="row justify-center btn-margin">
          <fdev-btn
            type="submit"
            :label="`${pathName}实体`"
            :loading="loading"
            dialog
            class="q-mb-lg q-mr-lg"
          />
        </div>
      </form>
    </fdev-card>
    <f-dialog v-model="setGreatKeyDialog" right f-sc title="高级属性设置">
      <div style="width:1000px">
        <f-formitem
          diaS
          label="可选高级属性"
          v-show="greatKey.json || path === 'add'"
        >
          <fdev-select
            use-input
            v-model="selectedKey"
            :options="json"
            option-label="description"
            option-value="title"
            clearable
            @input="selectTemp(selectedKey)"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label>{{ scope.opt.description }}</fdev-item-label>
                  <fdev-item-label caption>
                    {{ scope.opt.title }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </f-formitem>
        <fdev-btn
          color="primary"
          label="添加"
          flat
          @click="addGreatKey"
          v-if="greatKey.template === '0' && (path === 'add' || greatKey.json)"
        />
        <fdev-markup-table flat v-if="greatKey.key.length !== 0">
          <thead>
            <tr>
              <th class="tableWidth1">属性字段</th>
              <th class="tableWidth1">描述信息</th>
              <th
                :class="
                  greatKey.template === '0' && (path === 'add' || greatKey.json)
                    ? 'tableWidth3'
                    : 'tableWidth2'
                "
              >
                是否必填
              </th>
              <th
                class="tableWidth3"
                v-if="
                  greatKey.template === '0' && (path === 'add' || greatKey.json)
                "
              >
                操作
              </th>
            </tr>
          </thead>
        </fdev-markup-table>
        <div>
          <div
            class="row"
            style="margin-bottom:25px;"
            v-for="(item, index) in $v.greatKey.key.$each.$iter"
            :key="index"
          >
            <fdev-input
              outlined
              :ref="`greatKey.${index}.type`"
              type="text"
              placeholder="请输入属性字段"
              class="q-pb-sm tableWidth1"
              :disable="
                greatKey.template === '1' || (path !== 'add' && !greatKey.json)
              "
              v-model="item.type.$model"
              :rules="[
                () => item.type.alphaNumAndLine || '只能输入字母、数字和_',
                () => item.type.required || '请输入属性字段',
                () => item.type.isUnique || '高级属性字段不能重复'
              ]"
            />
            <fdev-input
              outlined
              type="text"
              placeholder="请输入描述信息"
              class="q-pb-sm tableWidth1"
              :disable="
                greatKey.template === '1' || (path !== 'add' && !greatKey.json)
              "
              v-model="item.description.$model"
            />
            <fdev-checkbox
              v-model="item.required.$model"
              true-value="1"
              false-value="0"
              :disable="
                greatKey.template === '1' || (path !== 'add' && !greatKey.json)
              "
              :class="[
                greatKey.template === '0' && (path === 'add' || greatKey.json)
                  ? 'tableWidth3'
                  : 'tableWidth2',
                'q-pb-sm'
              ]"
            />
            <fdev-btn
              v-if="
                greatKey.template === '0' && (path === 'add' || greatKey.json)
              "
              label="删除"
              class="tableWidth3 button q-pb-sm"
              flat
              color="red"
              @click="handelDeleteKey(index)"
            />
          </div>
        </div>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn label="返回" outline dialog @click="confirmToClose" />
        <fdev-btn
          dialog
          label="确定"
          @click="submitKey"
          v-if="greatKey.json || path === 'add'"
        />
      </template>
    </f-dialog>
  </f-block>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import { createModelItem, createGreatKey } from '../../utils/constants';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify, errorNotify } from '@/utils/utils';

export default {
  name: 'ModelEdit',
  data() {
    return {
      modelOptions: [],
      modelTemplateId: null,
      disab: false,
      tempArr: [],
      path: '',
      id: '',
      loading: false,
      model: createModelItem(),
      greatKey: createGreatKey(),
      entityListData: [],
      first_categorys: [],
      second_categorys: [],
      scope: [],
      platform: ['CaaS', 'SCC'],
      notices: [],
      setGreatKeyDialog: false,
      index: '',
      selectedKey: '',
      title: '',
      radioOption: [
        {
          value: 'string',
          label: 'String'
        },
        {
          value: 'array',
          label: 'Array'
        },
        {
          value: 'object',
          label: 'Object'
        }
      ]
    };
  },
  validations: {
    model: {
      env_key: {
        $each: {
          name_en: {
            required,
            alphaNumAndLine(value) {
              if (!value) {
                return true;
              }
              let re = new RegExp(/^[a-zA-Z0-9_]*$/);
              return re.test(value);
            },
            isUnique(value) {
              if (!value) {
                return true;
              }
              let val = this.filterName.filter(model => {
                return model.name_en === value;
              });
              return val.length <= 1;
            }
          },
          name_cn: {
            required,
            includeChinese(value) {
              if (!value) {
                return true;
              }
              let reg = new RegExp(/[\u4e00-\u9fa5]/gm);
              let flag = reg.test(value);
              return flag;
            },
            isUnique(value) {
              if (!value) {
                return true;
              }
              let newValue = value.replace(/(^\s*)|(\s*$)/g, '');
              let val = this.filterName.filter(model => {
                let newVal = model.name_cn.replace(/(^\s*)|(\s*$)/g, '');
                return newVal === newValue;
              });
              return val.length <= 1;
            }
          },
          require: {},
          type: {},
          data_type: {},
          desc: {}
        }
      },
      version: {
        required
      },
      model_template_id: {},
      name_en: {
        isUnique(value) {
          let newValue = value.replace(/(^\s*)|(\s*$)/g, '');
          if (!newValue) {
            return true;
          }
          let val = this.filterData.filter(entity => {
            return entity.name_en === newValue;
          });
          return val.length === 0;
        }
      },
      name_cn: {
        required,
        includeChinese(value) {
          if (!value) {
            return true;
          }
          let reg = new RegExp(/[\u4e00-\u9fa5]/gm);
          let flag = reg.test(value);
          return flag;
        },
        isUnique(value) {
          let newValue = value.replace(/(^\s*)|(\s*$)/g, '');
          if (!newValue) {
            return true;
          }
          let val = this.filterData.filter(entity => {
            return entity.name_cn === newValue;
          });
          return val.length === 0;
        }
      },
      desc: {},
      first_category: {
        required
      },
      second_category: {
        required
      },
      suffix_name: {
        required,
        onlyEnAndNum(value) {
          if (!value) {
            return true;
          }
          const reg = /^[a-zA-Z0-9-]*$/;
          return reg.test(value);
        }
      },
      scope: {
        required
      },
      platform: {
        required(value) {
          if (this.model.first_category === 'ci') {
            if (value && value.length === 0) {
              return false;
            }
            return !!value;
          }
          return true;
        }
      }
    },
    greatKey: {
      key: {
        $each: {
          type: {
            required,
            alphaNumAndLine(value) {
              if (!value) {
                return true;
              }
              let re = new RegExp(/^[a-zA-Z0-9_]*$/);
              return re.test(value);
            },
            isUnique(value) {
              if (!value || !Array.isArray(this.filterGreatKey)) {
                return true;
              }
              let val = this.filterGreatKey.filter(key => {
                return key.type === value;
              });
              return val.length <= 1;
            }
          },
          description: {},
          required: {}
        }
      }
    }
  },
  computed: {
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('environmentForm', {
      modelTempListPaging: 'modelTempListPaging',
      modelListData: 'modelList',
      modelTempData: 'modelTempList',
      modelConstObj: 'modelConstObj',
      json: 'jsonSchema'
    }),
    pathName() {
      return this.path === 'add'
        ? '新增'
        : this.path === 'update'
        ? '更新'
        : '复制';
    },
    filterData() {
      if (this.path !== 'add') {
        let entityData = this.entityListData.filter(entity => {
          return entity.id !== this.model.id;
        });
        return entityData;
      }
      return this.modelListData;
    },
    englishName() {
      if (this.model.first_category) {
        let name = '';
        name =
          this.model.first_category +
          this.changeName(this.model.second_category) +
          this.changeName(this.model.suffix_name);
        return name;
      } else {
        return '';
      }
    },
    filterName() {
      return this.model.env_key;
    },
    filterGreatKey() {
      return this.greatKey.key;
    }
  },
  watch: {
    modelTemplateId: {
      deep: true,
      handler(val) {
        if (this.path == 'add') {
          if (this.modelTemplateId) {
            this.model.model_template_id = this.modelTemplateId.id;
          } else {
            this.model.model_template_id = '';
          }
        }
      }
    },
    'model.first_category': {
      handler(val) {
        if (this.path !== 'update') {
          this.model.second_category = '';
          if (this.modelConstObj.category) {
            this.modelConstObj.category.filter(second => {
              for (let key in second) {
                if (val === key) {
                  this.second_categorys = second[key];
                }
              }
            });
          }
          if (val !== 'ci') {
            this.model.platform = [];
          }
        }
      }
    },
    'model.model_template_id': {
      handler(val) {
        // 新增状态时env_key由模板中实时筛选获取，复制/更新状态时，env_key值由初始化时查询获取；
        if (this.path == 'add') {
          this.getTempData(val);
        }
      },
      deep: true,
      immediate: true
    },
    englishName(val) {
      this.model.name_en = val;
    },
    setGreatKeyDialog(val) {
      if (!val) {
        this.greatKey.key = [
          {
            type: '',
            description: '',
            required: '1'
          }
        ];
        this.selectedKey = '';
        this.greatKey.template = '0';
        if (!this.model.env_key[this.index].dataType) {
          this.model.env_key[this.index].data_type = 'string';
        } else {
          this.model.env_key[this.index].data_type = this.model.env_key[
            this.index
          ].dataType;
        }
      }
    },
    selectedKey(val) {
      if (val) {
        this.selectTemp();
      }
    }
  },
  methods: {
    ...mapActions('environmentForm', {
      getModelTempDetail: 'getModelTempDetail',
      getModelTempListPaging: 'getModelTempListPaging',
      getModelList: 'getModelList',
      addModel: 'addModel',
      updateModel: 'updateModel',
      queryModelConst: 'queryModelConst',
      getJsonSchema: 'getJsonSchema'
    }),
    modelFilterChoice(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.modelOptions = this.modelTempListPaging.list.filter(v => {
          return (
            v.nameEn.toLowerCase().indexOf(needle) > -1 ||
            v.nameCn.indexOf(val) > -1
          );
        });
      });
    },
    async getTempData(id) {
      if (!id) {
        this.disab = false;
        this.model.env_key = createModelItem().env_key;
        this.radioOption = [
          {
            value: 'string',
            label: 'String'
          },
          {
            value: 'array',
            label: 'Array'
          },
          {
            value: 'object',
            label: 'Object'
          }
        ];
        return;
      }
      this.disab = true;
      this.radioOption = [{ value: 'string', label: 'String' }];
      await this.getModelTempDetail({
        id: id
      });
      let arr = this.modelTempData.envKey.map(item => {
        return {
          prop_key: item.prop_key,
          name_en: item.prop_key.toLowerCase(),
          name_cn: item.prop_name_cn,
          desc: item.desc,
          type: '0',
          data_type: 'string',
          require: '1',
          flag: ''
        };
      });
      this.model.env_key = arr;
    },
    addEnvKey() {
      this.model.env_key.push({
        name_en: '',
        name_cn: '',
        desc: '',
        type: '0',
        data_type: 'string',
        require: '0',
        id: '',
        flag: ''
      });
    },
    addGreatKey() {
      this.greatKey.key.push({
        type: '',
        description: '',
        required: '0'
      });
    },
    analisisData(data) {
      let template = JSON.parse(data).items
        ? JSON.parse(data).items
        : JSON.parse(data);
      Object.keys(template.properties).map((prop, index) => {
        this.$set(template.properties[prop], 'required', '0');
        template.properties[prop].type = prop;
      });
      template.required = template.required ? template.required : [];
      template.required.forEach(item => {
        Object.keys(template.properties).map((prop, index) => {
          if (prop === item) {
            this.$set(template.properties[prop], 'required', '1');
          }
        });
      });
      // 现在返回的是对象，需要返回数组
      let list = Object.keys(template.properties).map(
        item => template.properties[item]
      );
      return list;
    },
    selectTemp() {
      if (!this.selectedKey) {
        this.greatKey = {
          key: [],
          template: '0',
          json: '1'
        };
        return;
      }
      this.greatKey.key = this.analisisData(this.selectedKey.jsonSchema);
      this.$set(this.greatKey, 'json_schema_id', this.selectedKey.id);
      this.greatKey.template = '1';
    },
    async selectKeyType(item, index, radio) {
      // item.data_type.$model  item.require.$model
      let data_type;
      if (item) {
        item.data_type.$model = radio;
        data_type = item.data_type.$model;
      }
      if (data_type === 'string') {
        if (this.model.env_key[index].json_schema_id) {
          delete this.model.env_key[index].json_schema_id;
        }
        if (this.model.env_key[index].json_schema) {
          delete this.model.env_key[index].json_schema;
        }
        this.model.env_key[index].dataType = item.data_type.$model;
        this.index = index;
        return;
      }
      await this.getJsonSchema({ data_type: data_type });
      delete this.greatKey.json;
      let dataType = this.model.env_key[index].dataType;
      if (this.model.env_key[index].json_schema_id && dataType === data_type) {
        let key = this.json.find(
          item => item.id === this.model.env_key[index].json_schema_id
        );
        if (key) {
          this.selectedKey = key;
        } else {
          this.greatKey.key = this.analisisData(
            this.model.env_key[index].json_schema
          );
        }
      } else if (dataType !== data_type) {
        this.selectedKey = '';
        this.greatKey.key = [];
      } else if (this.model.env_key[index].json_schema) {
        this.greatKey.key = this.analisisData(
          this.model.env_key[index].json_schema
        );
      }
      if (Object.keys(this.model.env_key[index]).includes('flag')) {
        // 这个字段作为是否显示弹框操作按钮的根据
        this.$set(this.greatKey, 'json', '1');
      }
      this.title = data_type;
      this.index = index;
      this.setGreatKeyDialog = true;
    },
    async getDetail() {
      this.id = this.$route.query.id;
      await this.getModelList();
      await this.queryModelConst();
      this.modelConstObj.category.filter(first => {
        for (let key in first) {
          this.first_categorys.push(key);
        }
      });
      this.scope = this.modelConstObj.scope;
      this.entityListData = this.modelListData;
      // 复制或修改实体，env_key值由此处获取
      if (this.id) {
        this.model = this.entityListData.filter(entity => {
          return entity.id === this.id;
        })[0];
        this.model.platform = this.model.platform
          ? this.model.platform.split(',')
          : this.model.platform;
        // 实体属性和版本号回显 其他清空
        if (this.path === 'copy') {
          this.model.name_en = '';
          this.model.name_cn = '';
          this.model.first_category = '';
          this.model.second_category = '';
          this.model.suffix_name = '';
          this.model.scope = '';
          this.model.desc = '';
          this.model.platform = [];
        }
        // 返回值没有desc这个字段，就加上这个字段
        this.model.env_key = this.model.env_key.map(item => {
          let data_type = item.data_type
            ? item.data_type === ''
              ? 'string'
              : item.data_type
            : 'string';
          return {
            ...item,
            desc: item.desc ? item.desc : '',
            type: item.type ? item.type : '0',
            dataType: data_type,
            data_type: data_type
          };
        });
        await this.getJsonSchema({ data_type: '' });
        this.model.env_key = this.model.env_key.map(item => {
          let key = this.json.find(
            jsonItem => jsonItem.id === item.json_schema_id
          );
          item.disable = key ? true : false;
          return item;
        });
      } else {
        this.model.platform = [];
      }
      if (this.model.model_template_id) {
        // 实体模板选择是输入搜索下拉框，值为对象，此处需处理该赋值
        let newarr = this.modelTempListPaging.list.filter(item => {
          return item.id == this.model.model_template_id;
        });
        this.modelTemplateId = newarr[0];
        this.disab = true;
      } else {
        this.disab = false;
      }
    },
    handleEnvModalOpened(todo) {
      let message = '';
      if (todo === '更新') {
        message = '做此操作前请先核对影响范围。';
      }
      return this.$q
        .dialog({
          title: `${todo}实体`,
          message: `确定${todo}该实体吗？${message}`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.selectHandle();
        });
    },
    changeName(name) {
      if (name) {
        return '_' + name;
      }
      return '';
    },
    async selectHandle() {
      this.loading = true;
      try {
        // flag 这个字段有无 标志高级属性按钮是否显示
        this.model.env_key.map(item => {
          delete item.flag;
          item.dataType =
            item.dataType && item.dataType === 'string' ? '' : item.dataType;
          item.data_type =
            item.data_type && item.data_type === 'string' ? '' : item.data_type;
        });
        this.model.platform = this.model.platform.toString();
        if (this.path === 'add' || this.path === 'copy') {
          await this.addModel(this.model);
        } else {
          await this.updateModel(this.model);
        }
        successNotify(`${this.pathName}成功`);
        this.$router.push('/envModel/modelList');
      } finally {
        this.model.env_key.map(item => {
          item.flag = '';
          item.dataType =
            !item.dataType || item.dataType === '' ? 'string' : item.dataType;
          item.data_type =
            !item.data_type || item.data_type === ''
              ? 'string'
              : item.data_type;
        });
        this.loading = false;
      }
    },
    async handelModel(todo) {
      this.$v.model.$touch();
      let Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('model') > -1;
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.model.$invalid) {
        return;
      }
      this.handleEnvModalOpened(todo);
    },
    submitKey() {
      this.$v.greatKey.$touch();
      let Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('greatKey') > -1;
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.greatKey.$invalid) {
        return;
      }
      if (this.greatKey.template === '1') {
        this.$set(
          this.model.env_key[this.index],
          'json_schema_id',
          this.greatKey.json_schema_id
        );
        if (this.model.env_key[this.index].json_schema) {
          delete this.model.env_key[this.index].json_schema;
        }
        let key = this.json.find(
          item => item.id === this.greatKey.json_schema_id
        );
        let template = JSON.parse(key.jsonSchema).items
          ? JSON.parse(key.jsonSchema).items
          : JSON.parse(key.jsonSchema);
        if (template.required && template.required.length > 0) {
          this.model.env_key[this.index].require = '1';
        } else {
          this.model.env_key[this.index].require = '0';
        }
        this.$set(this.model.env_key[this.index], 'disable', true);
      } else {
        let json = {};
        if (!Array.isArray(this.greatKey.key)) {
          let key = Object.keys(this.greatKey.key).map(
            item => this.greatKey.key[item]
          );
          this.greatKey.key = key;
        }
        json = this.spellJsonSchema(this.greatKey.key, this.title);
        this.$set(this.model.env_key[this.index], 'json_schema', json);
        if (this.model.env_key[this.index].json_schema_id) {
          delete this.model.env_key[this.index].json_schema_id;
        }
        // 不是模板的高级属性，高级属性有必填，外层就必填，高级属性都非必填，外层就非必填
        let hasRequire = this.greatKey.key.some(item => item.required === '1');
        this.model.env_key[this.index].require = hasRequire ? '1' : '0';
        this.$set(this.model.env_key[this.index], 'disable', false);
      }
      // dataType为暂存的高级性类型，用于和变化中的data_type对比
      this.model.env_key[this.index].dataType = this.model.env_key[
        this.index
      ].data_type;
      this.setGreatKeyDialog = false;
    },
    // 将数据拼凑成json_schema
    spellJsonSchema(schema, key) {
      let required = [];
      let properties = {};
      schema.forEach(item => {
        if (item.required && item.required === '1') {
          required.push(item.type);
        }
        this.$set(properties, item.type, {
          type: 'string',
          description: item.description
        });
      });
      let json_schema = {};
      if (key.data_type === 'object' || key === 'object') {
        json_schema = {
          $schema: 'http://json-schema.org/draft-04/schema#',
          type: key.data_type ? key.data_type : key,
          properties,
          required
        };
      } else {
        json_schema = {
          $schema: 'http://json-schema.org/draft-04/schema#',
          type: key.data_type ? key.data_type : key,
          items: {
            properties,
            required,
            type: 'object'
          }
        };
      }
      if (json_schema.required && json_schema.required.length === 0) {
        delete json_schema.required;
      } else if (json_schema.items && json_schema.items.required.length === 0) {
        delete json_schema.items.required;
      }
      return JSON.stringify(json_schema);
    },
    // 点击是否必填改变高级属性的必填项
    changeRequire(require, key) {
      let list = this.analisisData(key.json_schema);
      let schema = list.map(item => {
        item.required = require;
        return item;
      });
      return this.spellJsonSchema(schema, key);
    },
    // 切换高级属性的必填项
    toggleRequire(val, key) {
      // val === '1'必填
      if (key.json_schema && val === '1') {
        key.json_schema = this.changeRequire('1', key);
      } else if (key.json_schema && val === '0') {
        key.json_schema = this.changeRequire('0', key);
      }
    },
    handelDelete(index) {
      if (this.model.env_key.length === 1) {
        return this.$q.dialog({
          title: `预删除实体属性`,
          message: `实体属性至少有一个`,
          ok: '我知道了'
        });
      }
      return this.$q
        .dialog({
          title: `预删除实体属性`,
          message: `仅为预删除，点击'${this.pathName}实体'后生效？`,
          ok: '我知道了',
          cancel: '取消'
        })
        .onOk(async () => {
          this.model.env_key.splice(index, 1);
        });
    },
    handelDeleteKey(index) {
      return this.$q
        .dialog({
          title: `预删除高级属性字段`,
          message: `仅为预删除，点击'${this.pathName}实体'后生效？`,
          ok: '我知道了',
          cancel: '取消'
        })
        .onOk(async () => {
          this.greatKey.key.splice(index, 1);
        });
    },
    goBack() {
      this.$router.push('/envModel/modelList');
    },
    closeDialog() {
      this.greatKey.key = [];
      this.setGreatKeyDialog = false;
    },
    disableRadio(item, type) {
      if (
        item &&
        item.$model.dataType !== type &&
        item.$model.id !== '' &&
        this.path !== 'add'
      ) {
        return true;
      }
      return false;
    },
    confirmToClose() {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this.setGreatKeyDialog = false;
        });
    }
  },
  async created() {
    let role_names = this.currentUser.role.map(each => each.name);
    if (!role_names.includes('环境配置管理员')) {
      errorNotify('当前用户无权限进入实体操作页面');
      this.$router.push('/envModel/modelList');
      return;
    }
    this.path = this.$route.path.split('/')[
      this.$route.path.split('/').length - 1
    ];
    // this.path = this.$route.params.path;
    // this.$route.meta.name = this.pathName + '实体';
    let paramsTemp = {
      page: 1,
      perPage: 0,
      nameEn: '',
      nameCn: '',
      envKey: ''
    };
    await this.getModelTempListPaging(paramsTemp);
    this.tempArr = this.modelTempListPaging.list.map(item => {
      return { value: item.id, label: item.nameCn };
    });
    this.modelOptions = this.modelTempListPaging.list;
    this.getDetail();
  }
};
</script>

<style lang="stylus" scoped>


.select-bottom {
  margin-bottom: 20px;
}

.padding-card {
  padding: 0 80px;
}

.form-header {
  font-size: 20px;
  font-weight: 700;
}

.button {
  height: 36px !important;
  line-height: 36px;
}

.desc {
  width: 25%;
}

.require {
  width: 7%;
}

.unique {
  padding: 0;
}

.require-key {
  width: 21%;
}

.not-change {
  width: 34%;
}

.handel {
  width: 8.3333%;
}

.q-pb-sm >>> .q-checkbox__inner {
  margin: 0 auto !important;
}

.tableWidth1 {
  width: 35%
}

.tableWidth2 {
  width: 30%
}

.tableWidth3 {
  width: 15%
}

.row >>> .q-field__inner {
  padding: 0 10px;
}

.row >>> .q-field__bottom--animated {
  padding: 0 10px;
}

.row-margin {
  margin-bottom: 8px;
}

// 布局调整
.row-margin label {
  flex: 1;
}

.row-margin button {
  flex: 0 0 8.33%;
}

.row-margin .col-chip {
  flex: 1;
}

.row-margin .q-checkbox {
  flex: 1;
}
.q-table thead {
  border: none;
}

.btn-margin {
  margin-top: 20px;
}

.container {
  max-height: 500px;
  overflow-y: scroll;
}

.container::-webkit-scrollbar {
  width: 5px;
}

.container::-webkit-scrollbar-track {
  background: #ededed;
}

.form .input-padding {
  padding-bottom: 20px;
}

.bottom-field >>> .input {
  padding-bottom: 20px;
}

.q-card__section > .row {
  width: 50%;
  display: inline-flex;
}

.q-card__section .row .col .q-field {
  min-width: 85%;
}

.relative {
  position: relative;
  right: -4em;
  top: 0;
}

.relative >>> .text-center {
  background: white !important;
}

.q-table thead, .q-table tr, .q-table th, .q-table td {
  border-color: rgba(0, 0, 0, 0);
}

// 样式调整
.trsty {
  display: flex;
}

.trsty th {
  flex: 1;
  line-height: 54px;
}

.trsty th:last-of-type {
  flex: 0 0 8.33%;
}

.require-margin {
  width: 7%;
  text-align: center;
}

.col-chip {
  width: 20%;
  display: flex;
  justify-content: center;
}

.span-display {
  display: inline-block;
}

.span-display >>> .q-anchor--skip {
  padding: 0;
}

.padding-btn {
  padding: 0;
  margin-top: 2px;
}
</style>
