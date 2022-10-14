<template>
  <f-block>
    <fdev-card flat square class="q-pa-md">
      <form @submit.prevent="handelModel(pathName)">
        <div class="padding-card">
          <fdev-card-section>
            <f-formitem
              diaS
              label="实体模板中文名"
              required
              bottom-page
              class="q-pr-sm"
            >
              <fdev-input
                ref="model.nameCn"
                type="text"
                class="input"
                v-model="$v.model.nameCn.$model"
                :rules="[
                  () => $v.model.nameCn.required || '请输入实体模板中文名',
                  () => $v.model.nameCn.includeChinese || '请至少输入一个中文',
                  () => $v.model.nameCn.isUnique || '中文名不能重复'
                ]"
              >
              </fdev-input>
            </f-formitem>
            <f-formitem
              diaS
              label="实体模板英文名"
              required
              bottom-page
              class="q-pr-sm"
            >
              <fdev-input
                ref="model.nameEn"
                type="text"
                v-model="$v.model.nameEn.$model"
                :rules="[
                  () => $v.model.nameEn.isUnique || '实体模板英文名重复',
                  () =>
                    $v.model.nameEn.alphaNumAndLine || '只能输入字母、数字和_',
                  () => $v.model.nameEn.required || '请输入实体模板英文名'
                ]"
              >
              </fdev-input>
            </f-formitem>
            <f-formitem diaS label="实体模板描述" bottom-page class="q-pr-sm">
              <fdev-input
                ref="model.desc"
                type="textarea"
                hint=""
                v-model="$v.model.desc.$model"
              >
              </fdev-input>
            </f-formitem>
          </fdev-card-section>
        </div>
        <fdev-separator />
        <div class="form-header q-my-sm">
          实体模板属性
          <fdev-btn color="primary" label="添加" flat @click="addEnvProperty" />
        </div>
        <fdev-markup-table flat v-if="model.envKey.length != 0">
          <thead class="thead">
            <tr class="trsty">
              <th>属性字段</th>
              <th>属性中文名</th>
              <th class="desc">描述信息</th>
              <th>属性类型</th>
              <!-- 高级属性待定，暂时保留 -->
              <!-- <th class="require">是否必填</th> -->
              <!-- <th class="require unique">是否应用独有</th> -->
              <th class="handel">操作</th>
            </tr>
          </thead>
        </fdev-markup-table>
        <div class="container q-pt-md">
          <div
            class="row row-margin"
            v-for="(item, index) in $v.model.envKey.$each.$iter"
            :key="index"
          >
            <fdev-input
              :ref="`model.${index}.propKey`"
              type="text"
              :readonly="model.envKey[index].id !== '' && path !== 'add'"
              placeholder="请输入属性字段"
              class="input q-pb-sm col-2"
              v-model="item.propKey.$model"
              :rules="[
                () => item.propKey.alphaNumAndLine || '只能输入字母、数字和_',
                () => item.propKey.required || '请输入属性字段',
                () =>
                  item.propKey.isUnique || '同一个实体模板的属性字段不能重复'
              ]"
            />
            <fdev-input
              :ref="`model.${index}.propNameCn`"
              type="text"
              placeholder="请输入属性中文名"
              class="input q-pb-sm col-2 input-padding"
              v-model="item.propNameCn.$model"
              :rules="[
                () => item.propNameCn.required || '请输入属性中文名',
                () => item.propNameCn.includeChinese || '请至少输入一个中文',
                () =>
                  item.propNameCn.isUnique ||
                  '同一个实体模板的属性中文名不能重复'
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
              <!-- 高级属性待定，暂时保留 -->
              <!-- <div v-for="radio in radioOption" :key="radio.value" class="span-display">
                <fdev-btn @click="selectKeyType(item, index, radio.value)" flat class="padding-btn" :disable="disableRadio(item, radio.value)">
                  <fdev-chip :outline="item.data_type.$model !== radio.value" square :color="
                      item.data_type.$model === radio.value ? 'teal' : 'green'
                    " dense text-color="white" size="xs">
                    {{ radio.value }}
                  </fdev-chip>
                </fdev-btn>
              </div> -->
              <div class="span-display">
                <fdev-btn
                  flat
                  class="padding-btn"
                  :disable="true"
                  style="text-transform:uppercase"
                >
                  <fdev-chip
                    :outline="false"
                    square
                    color="teal"
                    dense
                    text-color="white"
                    size="xs"
                  >
                    {{ 'string' }}
                  </fdev-chip>
                </fdev-btn>
              </div>
            </div>
            <!-- 是否必填，是否独有，暂时保留 -->
            <!-- <fdev-checkbox v-model="item.require.$model" true-value="1" false-value="0" class="q-pb-sm require-margin" @input="val => toggleRequire(val, item.$model)" :disable="
                (item.$model.disable && item.data_type.$model !== 'string') ||
                  (!Object.keys(model.env_key[index]).includes('flag') &&
                    path !== 'add' &&
                    item.data_type.$model !== 'string')
              " />
            <fdev-checkbox v-model="item.type.$model" true-value="1" false-value="0" class="q-pb-sm require-margin" /> -->
            <fdev-btn
              class="require-margin button"
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
            dialog
            :label="`${pathName}实体模板`"
            :loading="loading"
            class="q-mb-lg q-mr-lg"
          />
        </div>
      </form>
    </fdev-card>
    <!-- 高级属性待定，暂时保留此弹框，后续确定 start -->
    <!-- <fdev-dialog v-model="setGreatKeyDialog" transition-show="slide-up" transition-hide="slide-down" :persistent="path === 'add'" @shake="confirmToClose">
      <fdev-layout view="Lhh lpR fff" container class="bg-white">
        <fdev-header bordered class="bg-primary">
          <fdev-toolbar class="toolbar">
            <fdev-toolbar-title>
              高级属性设置
            </fdev-toolbar-title>
            <fdev-btn flat v-close-popup round dense icon="close" />
          </fdev-toolbar>
        </fdev-header>
        <fdev-page-container>
          <fdev-page padding>
            <field label="可选高级属性" :label-col="3" align="right" v-show="greatKey.json || path === 'add'">
              <fdev-select outlined use-input dense v-model="selectedKey" :options="json" option-label="description" option-value="title" clearable @input="selectTemp(selectedKey)">
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
            </field>
            <fdev-btn color="primary" label="添加" flat @click="addGreatKey" v-if="
                greatKey.template === '0' && (path === 'add' || greatKey.json)
              " />
            <fdev-markup-table flat v-if="greatKey.key.length !== 0">
              <thead>
                <tr>
                  <th>属性字段</th>
                  <th>描述信息</th>
                  <th :class="
                      greatKey.template === '0' &&
                      (path === 'add' || greatKey.json)
                        ? 'require-key'
                        : 'not-change'
                    ">
                    是否必填
                  </th>
                  <th class="handel" v-if="
                      greatKey.template === '0' &&
                        (path === 'add' || greatKey.json)
                    ">
                    操作
                  </th>
                </tr>
              </thead>
            </fdev-markup-table>
            <div>
              <div class="row" v-for="(item, index) in $v.greatKey.key.$each.$iter" :key="index">
                <fdev-input outlined :ref="`greatKey.${index}.type`" type="text" placeholder="请输入属性字段" class="input q-pb-sm col-4" dense :disable="
                    greatKey.template === '1' ||
                      (path !== 'add' && !greatKey.json)
                  " v-model="item.type.$model" :rules="[
                    () => item.type.alphaNumAndLine || '只能输入字母、数字和_',
                    () => item.type.required || '请输入属性字段',
                    () => item.type.isUnique || '高级属性字段不能重复'
                  ]" />
                <fdev-input outlined type="text" placeholder="请输入描述信息" class="input q-pb-sm col-4" dense :disable="
                    greatKey.template === '1' ||
                      (path !== 'add' && !greatKey.json)
                  " v-model="item.description.$model" />
                <fdev-checkbox v-model="item.required.$model" true-value="1" false-value="0" :disable="
                    greatKey.template === '1' ||
                      (path !== 'add' && !greatKey.json)
                  " :class="[
                    greatKey.template === '0' &&
                    (path === 'add' || greatKey.json)
                      ? 'col-3'
                      : 'col-4',
                    'q-pb-sm'
                  ]" />
                <fdev-btn v-if="
                    greatKey.template === '0' &&
                      (path === 'add' || greatKey.json)
                  " class="col-1 button" flat color="red" icon="delete" @click="handelDeleteKey(index)" />
              </div>
            </div>
            <div class="row justify-center btn-margin" v-if="greatKey.key.length !== 0">
              <fdev-btn @click="submitKey" label="确定" color="primary" text-color="white" class="q-mb-lg q-mr-lg" v-show="greatKey.json || path === 'add'" />
              <fdev-btn type="button" label="返回" @click="closeDialog" color="primary" text-color="white" class="q-mb-lg q-mr-lg" />
            </div>
          </fdev-page>
        </fdev-page-container>
      </fdev-layout>
    </fdev-dialog> -->
    <!-- end -->
  </f-block>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import { createModelTempItem, createGreatKey } from '../../utils/constants';
import { required } from 'vuelidate/lib/validators';
import { validate, successNotify, errorNotify } from '@/utils/utils';

export default {
  name: 'ModelTempEdit',
  data() {
    return {
      path: '',
      id: '',
      loading: false,
      model: createModelTempItem(),
      greatKey: createGreatKey(),
      entityListData: [],
      first_categorys: [],
      second_categorys: [],
      scope: [],
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
      envKey: {
        $each: {
          propKey: {
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
                return model.propKey === value;
              });
              return val.length <= 1;
            }
          },
          propNameCn: {
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
                let newVal = model.propNameCn.replace(/(^\s*)|(\s*$)/g, '');
                return newVal === newValue;
              });
              return val.length <= 1;
            }
          },
          require: {},
          type: {},
          dataType: {},
          desc: {}
        }
      },
      nameEn: {
        required,
        alphaNumAndLine(value) {
          if (!value) {
            return true;
          }
          let re = new RegExp(/^[a-zA-Z0-9_]*$/);
          return re.test(value);
        },
        isUnique(value) {
          let newValue = value.replace(/(^\s*)|(\s*$)/g, '');
          if (!newValue) {
            return true;
          }
          let val = this.filterData.filter(entity => {
            return entity.nameEn === newValue;
          });
          return val.length === 0;
        }
      },
      nameCn: {
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
            return entity.nameCn === newValue;
          });
          return val.length === 0;
        }
      },
      desc: {}
    }
    // 此处为高级属性弹框校验，暂时保留，后续确定后再决定删除与否
    /* greatKey: {
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
    } */
  },
  computed: {
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('environmentForm', {
      modelListData: 'modelTempListPaging',
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
      // 数据过滤，暂时除新增外无其它操作，后续待定
      /* if (this.path !== 'add') {
        let entityData = this.entityListData.filter(entity => {
          return entity.id !== this.model.id;
        });
        return entityData;
      } */
      return this.modelListData.list;
    },
    filterName() {
      return this.model.envKey;
    }
    /* filterGreatKey() {
      return this.greatKey.key;
    } */
  },
  watch: {
    // 高级属性设置，待定
    /* setGreatKeyDialog(val) {
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
    }, */
    /* selectedKey(val) {
      if (val) {
        this.selectTemp();
      }
    } */
  },
  methods: {
    ...mapActions('environmentForm', {
      getModelTempListPaging: 'getModelTempListPaging',
      addModelTemp: 'addModelTemp',
      updateModel: 'updateModel',
      queryModelConst: 'queryModelConst',
      getJsonSchema: 'getJsonSchema'
    }),
    addEnvProperty() {
      this.model.envKey.push({
        propKey: '',
        propNameCn: '',
        desc: '',
        type: '0',
        dataType: 'string',
        require: '0',
        id: '',
        flag: ''
      });
    },
    // -----------------------------高级属性暂定不设置，后续待定
    /* addGreatKey() {
      this.greatKey.key.push({
        type: '',
        description: '',
        required: '0'
      });
    }, */
    /* analisisData(data) {
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
    }, */
    /* selectTemp() {
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
    }, */
    /* async selectKeyType(item, index, radio) {
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
      // this.setGreatKeyDialog = true;
    }, */
    // --------------------------   end
    async getDetail() {
      this.id = this.$route.query.id;
      await this.getModelTempListPaging({
        page: 1,
        perPage: 0,
        nameEn: '',
        nameCn: '',
        envKey: ''
      });
      // await this.queryModelConst();
      // this.modelConstObj.category.filter(first => {
      //   for (let key in first) {
      //     this.first_categorys.push(key);
      //   }
      // });
      // this.scope = this.modelConstObj.scope;
      this.entityListData = this.modelListData.list;
      // 目前只有新增操作，不排除后续有高级属性或修改等操作，暂时保留，确定后再行删除
      // 复制或修改实体
      /* if (this.id) {
        this.model = this.entityListData.filter(entity => {
          return entity.id === this.id;
        })[0];
        // 实体属性和版本号回显 其他清空
        if (this.path === 'copy') {
          this.model.name_en = '';
          this.model.name_cn = '';
          this.model.first_category = '';
          this.model.second_category = '';
          this.model.suffix_name = '';
          this.model.scope = '';
          this.model.desc = '';
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
      } */
    },
    handleEnvModalOpened(todo) {
      let message = '';
      if (todo === '更新') {
        message = '做此操作前请先核对影响范围。';
      }
      return this.$q
        .dialog({
          title: `${todo}实体模板`,
          message: `确定${todo}该实体模板吗？${message}`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.selectHandle();
        });
    },
    async selectHandle() {
      this.loading = true;
      try {
        // flag 这个字段有无 标志高级属性按钮是否显示
        this.model.envKey.map(item => {
          delete item.flag;
          // item.dataType =
          //   item.dataType && item.dataType === 'string' ? '' : item.dataType;
          item.dataType =
            item.dataType && item.dataType === 'string'
              ? 'string'
              : item.dataType;
        });
        if (this.path === 'add' || this.path === 'copy') {
          await this.addModelTemp(this.model);
        } else {
          await this.updateModel(this.model);
        }
        successNotify(`${this.pathName}成功`);
        this.$router.push('/envModel/modelTempList');
      } finally {
        this.model.envKey.map(item => {
          item.flag = '';
          // item.dataType =
          //   !item.dataType || item.dataType === '' ? 'string' : item.dataType;
          item.dataType =
            !item.dataType || item.dataType === '' ? 'string' : item.dataType;
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
    // -----------------------------高级属性暂定不设置，此处数据处理及显隐切换等后续确定后决定删除与否   start
    /* submitKey() {
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
    }, */
    // 将数据拼凑成json_schema
    /* spellJsonSchema(schema, key) {
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
    }, */
    // 点击是否必填改变高级属性的必填项
    /* changeRequire(require, key) {
      let list = this.analisisData(key.json_schema);
      let schema = list.map(item => {
        item.required = require;
        return item;
      });
      return this.spellJsonSchema(schema, key);
    }, */
    // 切换高级属性的必填项
    /* toggleRequire(val, key) {
      // val === '1'必填
      if (key.json_schema && val === '1') {
        key.json_schema = this.changeRequire('1', key);
      } else if (key.json_schema && val === '0') {
        key.json_schema = this.changeRequire('0', key);
      }
    }, */
    // ----------------------------------  end
    handelDelete(index) {
      if (this.model.envKey.length === 1) {
        return this.$q.dialog({
          title: `预删除实体模板属性`,
          message: `实体模板属性至少有一个`,
          ok: '我知道了'
        });
      }
      return this.$q
        .dialog({
          title: `预删除实体模板属性`,
          message: `仅为预删除，点击'${this.pathName}实体模板'后生效？`,
          ok: '我知道了',
          cancel: '取消'
        })
        .onOk(async () => {
          this.model.envKey.splice(index, 1);
        });
    },
    // 高级属性删除，暂时保留，待定
    /* handelDeleteKey(index) {
      return this.$q
        .dialog({
          title: `预删除高级属性字段`,
          message: `仅为预删除，点击'${this.pathName}实体模板'后生效？`,
          ok: '我知道了',
          cancel: '取消'
        })
        .onOk(async () => {
          this.greatKey.key.splice(index, 1);
        });
    }, */
    goBack() {
      this.$router.push('/envModel/modelTempList');
    }
    // 高级属性操作，待定
    /* closeDialog() {
      this.greatKey.key = [];
      this.setGreatKeyDialog = false;
    }, */
    /* disableRadio(item, type) {
      if (
        item &&
        item.$model.dataType !== type &&
        item.$model.id !== '' &&
        this.path !== 'add'
      ) {
        return true;
      }
      return false;
    }, */
    /* confirmToClose() {
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
    } */
  },
  async created() {
    let role_names = this.currentUser.role.map(each => each.name);
    if (!role_names.includes('环境配置管理员')) {
      errorNotify('当前用户无权限进入实体模板操作页面');
      this.$router.push('/envModel/modelTempList');
      return;
    }
    this.path = this.$route.path.split('/')[
      this.$route.path.split('/').length - 1
    ];
    // this.path = this.$route.params.path;
    // this.$route.meta.name = this.pathName + '实体模板';
    this.getDetail();
  }
};
</script>

<style lang="stylus" scoped>


.padding-card {
  padding: 0 80px;
}

.form-header {
  font-size: 20px;
  font-weight: 700;
}

.button {
  height: 36px !important;
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

.q-pb-sm >>> .input-field span {
  min-width: 112px;
}
</style>
