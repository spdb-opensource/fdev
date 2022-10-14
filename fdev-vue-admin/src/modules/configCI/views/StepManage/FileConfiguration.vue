<template>
  <f-dialog
    :value="isShow"
    :title="`${isDisabled ? '查看' : '编辑'}${configInfoNew.configName}`"
    @before-close="clickCancel"
  >
    <f-formitem label="实体组" class="q-mb-lg">
      <fdev-select
        v-model="configId"
        :options="configTemplateIds"
        option-label="key"
        option-value="value"
        map-options
        emit-value
        :disable="isDisabled"
        @input="queryYamlConfig"
      />
    </f-formitem>
    <fdev-form
      class="scroll-v scroll-thin-y"
      style="height:400px"
      ref="configInfoFrom"
      :greedy="true"
      :no-error-focus="true"
      @submit.prevent="clickSure"
    >
      <f-formitem class="file-desc" label="文件说明">
        <div class="q-ml-sm text-grey">
          {{ configInfoNew.configDesc }}
        </div>
      </f-formitem>
      <div class="q-mt-md">
        <f-formitem class="no-required1" label="文件配置" />
        <div class="q-mb-md">
          <f-formitem
            required
            :label="`实体环境${configInfoNew.envsFlag ? '(可多选)' : ''}`"
            label-style="margin-left: 15px"
          />
          <fdev-select
            v-model="envModel"
            :options="envList1"
            :multiple="configInfoNew.envsFlag"
            :use-chips="configInfoNew.envsFlag"
            class="select-v q-ml-md"
            option-label="nameEn"
            option-value="nameEn"
            :disable="isDisabled"
            :rules="[
              val =>
                (configInfoNew.envsFlag ? val && val.length : !!val) ||
                '请选择实体环境'
            ]"
            use-input
            @filter="filterEnv"
          >
            <template v-slot:option="scope">
              <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                <fdev-item-section>
                  <fdev-item-label>
                    {{ scope.opt.nameEn }}
                  </fdev-item-label>
                  <fdev-item-label caption>
                    {{ scope.opt.type }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </template>
          </fdev-select>
        </div>
      </div>
      <div v-if="isHaveParams">
        <div v-for="(item, idx) in configInfoNew.params" :key="idx" class="col">
          <f-formitem
            class="text-center"
            :class="{ 'no-required': !item.required }"
            :required="item.required"
            :label="
              `
                ${item.entityTemplateCn}${item.chooseMore ? '(可多选)' : ''}
              `
            "
            :help="item.hint"
            :label-style="item.required ? 'margin-left: 15px' : ''"
          />
          <div
            class="q-pr-md q-mb-md"
            v-for="(el, index) in item.entityModelList"
            :key="index"
          >
            <div class="row no-wrap entity-model q-pl-md q-pr-md">
              <fdev-select
                v-model="el.entityModel"
                :disable="isDisabled || isReadonly"
                :rules="item.required ? [val => !!val || '请选择内容'] : []"
                :options="entityModelOptionsNew"
                outlined
                clearable
                lazy-rules
                option-label="nameEn"
                option-value="nameEn"
                @input="changeInput(el)"
                class="select-v"
                @focus="clickSelectEntityModel(item)"
                @filter="filterFn"
              >
                <template v-slot:option="scope">
                  <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                    <fdev-item-section>
                      <fdev-item-label>
                        {{ scope.opt.nameEn }}
                      </fdev-item-label>
                      <fdev-item-label caption>
                        {{ scope.opt.nameCn }}
                      </fdev-item-label>
                    </fdev-item-section>
                  </fdev-item>
                </template>
              </fdev-select>
              <div class="row justify-center q-ml-lg">
                <fdev-btn
                  flat
                  class="look-map"
                  label="查看映射"
                  size="sm"
                  :disabled="!isHadEnv || !el.entityModel"
                  @click.stop="openEneityDialog(el)"
                >
                  <!-- <fdev-tooltip v-if="!isHadEnv || !el.entityModel">
                    只有实体环境和当前下拉框都有值才能查看
                  </fdev-tooltip> -->
                </fdev-btn>
                <fdev-btn
                  v-if="item.chooseMore"
                  flat
                  ficon="delete_o"
                  class="delete-btn"
                  :disable="
                    item.entityModelList.length === 1 ||
                      isDisabled ||
                      isReadonly
                  "
                  @click.stop="deleteEnvModel(idx, index)"
                />
                <fdev-btn
                  v-if="item.chooseMore"
                  ficon="add"
                  flat
                  :disable="isDisabled || isReadonly"
                  @click.stop="addEnvModel(item)"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </fdev-form>
    <template v-slot:btnSlot>
      <fdev-btn label="取消" outline dialog @click="clickCancel" />
      <fdev-btn v-if="!isDisabled" label="确定" dialog @click="clickSure" />
    </template>
    <!-- 查看映射 -->
    <f-dialog
      v-if="isOpenEneityDialog"
      v-model="isOpenEneityDialog"
      title="实体参数映射"
    >
      <div
        class="table-wrapper bg-white scroll-thin-y"
        :class="{ 'q-pl-md': variablesData.length === 1 }"
        style="height:400px"
      >
        <table class="table">
          <div v-for="(el, idx) in variablesData" :key="idx">
            <div class="title-tr">
              {{ el.title }}
            </div>
            <tr>
              <th>参数</th>
              <th>
                映射值
              </th>
            </tr>
            <tr v-for="(item, index) of el.data" :key="index">
              <td>{{ index }}</td>
              <td>{{ item }}</td>
            </tr>
          </div>

          <div
            class="flex-center row no-data"
            v-if="variablesData.length === 0"
          >
            <f-image name="no_data"></f-image>
          </div>
        </table>
      </div>
    </f-dialog>
  </f-dialog>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import { successNotify, deepClone } from '@/utils/utils';
export default {
  name: 'Fileconfiguration',
  components: {},
  props: {
    isShow: {
      type: Boolean,
      default: true
    },
    type: {
      type: String,
      default: 'edit'
    },
    fileConfigInfo: Object,
    configTemplateIds: Array
  },
  data() {
    return {
      envModel: null,
      isHaveParams: false,
      isHadEnv: false,
      sl_entityTemplateId: '',
      entityModelOptions: null,
      entityModelOptionsNew: null,
      isOpenEneityDialog: false,
      envList1: [],
      variablesData: [], //映射值
      configInfoNew: {},
      configId: '', // 实体组选择框绑定id
      initConfigInfo: {}, // 初始化fileConfig信息
      isReadonly: false, // 使用提供模板的实体组时，除"实体环境"外，均不可编辑
      configTypeDefualt: '', // 保存自定义模板的实体组tamplatId
      configId_copy: '' // 保存自定义模板生成的configId，用于编辑自定义模板时接口传参
    };
  },
  computed: {
    ...mapState('configCIForm', [
      'modelList',
      'yamlConfigInfo',
      'envList',
      'entityModelDetail',
      'yamlConfigByIdInfo'
    ]),
    isDisabled() {
      return this.type === 'detail';
    }
  },
  watch: {
    configId(newVal, oldVal) {
      if (newVal && oldVal) {
        if (newVal !== this.configTypeDefualt) {
          this.isReadonly = true;
        } else {
          this.isReadonly = false;
        }
      }
    },
    isShow(newVal, oldVal) {
      if (newVal && Object.keys(this.initConfigInfo).length) {
        this.configInfoNew = deepClone(this.initConfigInfo);
      }
      if (!newVal) {
        // 关闭弹窗，实体组选择框默认选择自定义模板
        this.configId = this.configTypeDefualt;
        this.configId_copy = '';
        this.configInfoNew = {};
        this.envModel = null;
        this.entityModelOptions = null;
        this.entityModelOptionsNew = null;
      }
    },
    // 初始传入的fileConfig信息，需判断是否选择了实体组和选择了哪个实体组，同时对应实体组选择框的展示信息
    fileConfigInfo(val) {
      if (val) {
        // 选择了实体组
        if (val.configTemplateId) {
          // 判断选择的实体组是自定义模板or提供模板
          let currentId = this.configTemplateIds.find(item => {
            return item.value === val.configId;
          });
          // 若存在，当前configId即为提供模板的实体组id
          if (currentId) {
            this.configId = currentId;
            this.configTypeDefualt = val.configTemplateId;
            this.isReadonly = true;
          } else {
            // 若不存在，则选择框展示为自定义模板，但是编辑时接口上送的仍是该模板自己的configId
            this.configTypeDefualt = val.configTemplateId;
            this.configId = this.configTypeDefualt;
            this.configId_copy = val.configId;
            this.isReadonly = false;
          }
          // 未选择实体组时，当前configId为自定义模板的id
        } else {
          this.configTypeDefualt = val.configId;
          this.configId = this.configTypeDefualt;
          this.isReadonly = false;
        }
        this.initConfigInfo = deepClone(val);
      }
    },
    initConfigInfo(val) {
      if (val) {
        let info = deepClone(val);
        const { params } = val;
        //拼接数据
        if (params && params.length) {
          this.isHaveParams = true;
          info.params.forEach(item => {
            const { entity } = item;
            let arr = [{}];
            if (entity && entity.length) {
              arr = entity.map(item => {
                //实体有值才财富
                if (item && item.entityId) {
                  const obj = {
                    id: item.entityId,
                    nameEn: item.entityEn,
                    nameCn: item.entityCn
                  };
                  return { entityModel: obj };
                }
                return {};
              });
            }
            item.entityModelList = arr;
          });
        }
        //多选
        if (info.envsFlag) {
          if (info.envList && info.envList.length) {
            this.isHadEnv = true;
            this.envModel = info.envList.map(item => {
              return {
                nameEn: item
              };
            });
          }
          info.env = '';
          //单选
        } else {
          info.envList = [];
          if (info.env) {
            this.isHadEnv = true;
            this.envModel = {
              nameEn: info.env
            };
          }
        }
        this.configInfoNew = info;
      }
    },
    envModel(val) {
      if (val && Object.keys(val).length) {
        this.isHadEnv = true;
        return;
      }
      this.isHadEnv = false;
    }
  },
  methods: {
    ...mapActions('configCIForm', [
      'querySectionEntity',
      'addYamlConfig',
      'queryEnvList',
      'queryEntityModelDetail',
      'getYamlConfigById'
    ]),
    // 切换实体组时查询yamlConfig信息
    async queryYamlConfig(configId) {
      await this.getYamlConfigById({
        configId:
          this.configId_copy && configId === this.configTypeDefualt
            ? this.configId_copy
            : configId
      });
      this.initConfigInfo = deepClone(this.yamlConfigByIdInfo);
    },
    clickCancel() {
      this.$emit('update:isShow', false);
    },
    filterFn(val, update, abort) {
      setTimeout(() => {
        update(() => {
          if (val === '') {
            this.entityModelOptionsNew = this.entityModelOptions;
          } else {
            this.entityModelOptionsNew = this.entityModelOptions.filter(
              v => v.nameEn.toLowerCase().indexOf(val.toLowerCase()) > -1
            );
          }
        });
      }, 500);
      this.$forceUpdate();
    },
    clickSure() {
      this.$refs.configInfoFrom.validate().then(async res => {
        if (!res) return;
        let info = Object.assign({}, this.configInfoNew);
        const { configTemplateId } = info;
        if (!configTemplateId) {
          //存在则为编辑进来的
          info.configTemplateId = info.configId;
          info.configId = '';
        }
        //环境(多选)
        if (info.envsFlag) {
          info.envList = this.envModel.map(item => {
            return item.nameEn;
          });
          //单选环境
        } else {
          info.env = this.envModel.nameEn;
        }
        if (this.isHaveParams) {
          info.params.forEach(item => {
            let list = item.entityModelList;
            if (list.length) {
              item.entity = list.map(e => {
                if (e && Object.keys(e).length && e.entityModel) {
                  return {
                    entityId: e.entityModel.id,
                    entityEn: e.entityModel.nameEn,
                    entityCn: e.entityModel.nameCn
                  };
                }
              });
            }
            delete item.entityModelList;
          });
        }
        // console.log('传入参数：', info);
        await this.addYamlConfig(info);
        successNotify('新增成功');
        this.clickCancel();
        this.$emit('addYamlConfigSuccess', this.yamlConfigInfo);
      });
    },
    //查询实体
    async clickSelectEntityModel(item) {
      if (
        item.entityTemplateId === this.sl_entityTemplateId &&
        this.entityModelOptions &&
        this.entityModelOptions.length
      )
        return;
      this.sl_entityTemplateId = item.entityTemplateId;
      const params = {
        templateId: item.entityTemplateId
      };
      this.entityModelOptions = [];
      await this.querySectionEntity(params);
      //实体列表
      this.entityModelOptions = this.modelList.entityModelList || [];
    },
    //添加实体
    addEnvModel(item) {
      item.entityModelList.push({});
    },
    //删除实体
    deleteEnvModel(idx, childIdx) {
      this.configInfoNew.params[idx].entityModelList.splice(childIdx, 1);
    },
    // 查看映射
    async openEneityDialog(el, notLookMap) {
      if (!el || !el.entityModel) return;
      let mapData = [];
      // let hadData = false;
      await this.queryEntityModelDetail({ id: el.entityModel.id });
      let data = this.entityModelDetail.propertiesValue;
      if (data && Object.values(data).length) {
        const values = Object.values(data);
        //多选环境
        if (this.configInfoNew.envsFlag) {
          values.find(arr => {
            this.envModel.find(item => {
              const env = item.nameEn;
              let entityP = arr[env];
              if (entityP) {
                mapData.push({
                  title: env,
                  data: entityP
                });
              }
            });
          });
        } else {
          //选中的配置环境
          values.map(item => {
            const env = this.envModel.nameEn;
            const entityP = item[env];
            if (entityP) {
              mapData.push({
                title: env,
                data: entityP
              });
            }
          });
        }
      }
      if (!notLookMap) {
        this.isOpenEneityDialog = true;
        this.variablesData = mapData;
      }
    },
    //刷性数据
    changeInput(el) {
      // this.openEneityDialog(el, true); //查看是否有映射值
    },
    filterEnv(val, update) {
      if (val === '') {
        update(() => {
          this.envList1 = this.envList;
        });
        return;
      }
      update(() => {
        const needle = val.toLowerCase();
        this.envList1 = this.envList.filter(
          v => v.nameEn.toLowerCase().indexOf(needle) > -1
        );
      });
    }
  },
  async mounted() {
    //查询实体环境
    if (!this.envList || this.envList.length === 0) {
      await this.queryEnvList();
    }
  }
};
</script>
<style lang="stylus" scoped>
.file-config
  background red
.entity-model
  display: flex;
  justify-content flex-start
  align-items: center;
.select-v
  width 250px
/deep/ .q-field--with-bottom
  padding-bottom: 0
.scroll-v
  margin-top: -20px;
  padding-bottom 100px
.file-desc
  display block
  .text-grey
    color #999
    width 400px
.no-required1 >>> .required
  width 0px
.no-required >>>
  .label-with-help
    width 200px
.delete-btn
  width 30px
.table-wrapper
  width 100%
  // padding 25px
.table-wrapper .table
  width 100%
  border-radius 5px
  border-collapse collapse
  // border 1px solid #bdbdbd
  td, th
    height 40px
    text-align center
    border 1px solid #bdbdbd
    color #616161
    min-width 200px
  tr:nth-of-type(2n)
    td, th
      background #eee
  .title-tr
    width: 401px
    height 40px
    color #616161
    padding: 10px
    border 1px solid #bdbdbd
    border-bottom none
.no-data
  margin-top 30%
</style>
