<template>
  <f-block>
    <f-dialog
      right
      :value="value"
      transition-show="slide-up"
      transition-hide="slide-down"
      @input="$emit('input', $event)"
      id="scrollArea"
      :fullWidth="
        toggleFile ||
          (configAssets.length > 0 && !deployTypeArr.includes(false))
      "
      title="变更介质准备"
    >
      <div style="width:1200px">
        <div class="row">
          <fdev-toggle
            icon="mdi-set mdi-file-document-outline"
            v-model="toggleFile"
          />
          <div class="col text-right" v-if="data.length > 0 && !toggleFile">
            <span>
              <fdev-btn
                ficon="download"
                color="secondary"
                flat
                @click="exportFile"
              />
              <fdev-tooltip>导出发布说明</fdev-tooltip>
            </span>
          </div>
          <div
            class="col-5"
            v-if="configAssets.length > 0 && !deployTypeArr.includes(false)"
          ></div>
        </div>
        <div class="row q-mt-md" v-show="!toggleFile">
          <fdev-tree
            :nodes="data"
            label-key="asset_name"
            node-key="catalog_name"
            default-expand-all
            class="q-mb-lg col"
            no-nodes-label="暂无变更介质文件"
          >
            <template v-slot:default-header="prop">
              <div
                class="col-6 text-primary "
                v-if="prop.node.catalog_description"
              >
                <span>介质目录：{{ prop.node.catalog_name }}</span>
                <span
                  class="text-red"
                  v-if="
                    prop.node.catalog_type === '7' &&
                      prop.node.catalog_name === 'AWS_COMMON' &&
                      !awsCommonGroupIsRight
                  "
                >
                  （所属组不正确）
                </span>
                <span
                  class="text-red"
                  v-if="
                    prop.node.catalog_type === '7' &&
                      prop.node.catalog_name === 'AWS_STATIC' &&
                      !awsStaticGroupIsRight
                  "
                >
                  （所属组不正确）
                </span>
              </div>
              <div
                class="col-6 text-primary "
                v-if="prop.node.catalog_description"
              >
                <span>步骤：{{ prop.node.catalog_description }}</span>
              </div>
              <div v-else>
                <span v-if="prop.node.catalog_name || prop.node.asset_name">
                  {{ prop.node.catalog_name || prop.node.asset_name }}
                </span>
                <span
                  v-if="prop.node.write_flag && prop.node.write_flag === '0'"
                >
                  (写入order.txt文件)
                </span>
                <span
                  v-if="
                    (prop.node.pro_image_uri === null ||
                      prop.node.pro_image_uri === '' ||
                      prop.node.pro_image_uri) &&
                      autoType === '1'
                  "
                >
                  <!-- 无镜像标签，无部署平台 -->
                  <span
                    class="text-red"
                    v-if="
                      (prop.node.pro_image_uri === null ||
                        prop.node.pro_image_uri === '') &&
                        prop.node.deploy_type.length === 0
                    "
                  >
                    (未选择镜像标签和部署平台，
                    <router-link
                      class="link"
                      :to="`/release/updateDetail/${prod_id}/updateList`"
                      title="前往选择"
                    >
                      前往选择
                    </router-link>
                    )
                  </span>
                  <!-- 有镜像标签，无部署平台 -->
                  <span
                    class="text-red"
                    v-else-if="
                      prop.node.pro_image_uri !== null &&
                        prop.node.deploy_type.length === 0
                    "
                  >
                    (未选择部署平台，
                    <router-link
                      class="link"
                      :to="`/release/updateDetail/${prod_id}/updateList`"
                      title="前往选择"
                    >
                      前往选择
                    </router-link>
                    )
                  </span>
                  <!-- 无镜像标签，有部署平台 -->
                  <span
                    class="text-red"
                    v-else-if="
                      (prop.node.pro_image_uri === null ||
                        prop.node.pro_image_uri === '') &&
                        prop.node.deploy_type.length > 0
                    "
                  >
                    (未选择镜像标签，
                    <router-link
                      class="link"
                      :to="`/release/updateDetail/${prod_id}/updateList`"
                      title="前往选择"
                    >
                      前往选择
                    </router-link>
                    )
                  </span>
                  <span v-else>
                    <span
                      v-if="
                        prop.node.hasOwnProperty('pro_image_uri') &&
                          prop.node.hasOwnProperty('pro_scc_image_uri') &&
                          prop.node.deploy_type.length === 2
                      "
                    >
                      <span>(</span>
                      <span v-if="prop.node.pro_image_uri">
                        {{ prop.node.pro_image_uri }}
                      </span>
                      <span v-else class="text-red">
                        未选择CAAS镜像标签，
                        <router-link
                          class="link"
                          :to="`/release/updateDetail/${prod_id}/updateList`"
                          title="前往选择"
                        >
                          前往选择
                        </router-link>
                      </span>
                      <span>，</span>
                      <span v-if="prop.node.pro_scc_image_uri">
                        {{ prop.node.pro_scc_image_uri }}
                      </span>
                      <span v-else class="text-red">
                        未选择SCC镜像标签，
                        <router-link
                          class="link"
                          :to="`/release/updateDetail/${prod_id}/updateList`"
                          title="前往选择"
                        >
                          前往选择
                        </router-link>
                      </span>
                      <span>)</span>
                    </span>
                    <span v-else> ({{ prop.node.pro_image_uri }}) </span>
                  </span>
                </span>
              </div>
            </template>
          </fdev-tree>

          <fdev-separator
            vertical
            inset
            v-if="configAssets.length > 0 && !deployTypeArr.includes(false)"
          />

          <div
            class="col-5 configs"
            v-if="configAssets.length > 0 && !deployTypeArr.includes(false)"
          >
            <div class="q-ml-md">
              <p class="font">配置中心目录:</p>
              <fdev-input
                ref="catalog"
                class="q-ml-md"
                placeholder="生产环境目录"
                v-model="$v.catalog.$model"
                @blur="createConfigFile"
                :rules="[() => !$v.catalog.$error || '请输入生产环境目录']"
              />
            </div>
            <div class="q-ml-md">
              <p class="font">生成template.properties内容如下:</p>
              <fdev-input
                v-show="textareaShow"
                v-model="configFile"
                type="textarea"
                class="q-ml-md q-mt-md"
                placeholder="生产环境目录"
                autogrow
              />
            </div>
            <div class="q-ml-md q-mt-md text-grey-7">
              <p class="font q-pb-xs">template文件格式说明:</p>
              <div class="q-ml-md">#END</div>
              <div class="q-ml-md">
                #文件操作类型：模块类型&指定ip或all#介质目录投产文件相对位置及文件名#变更服务器全路径及文件名
              </div>
            </div>
            <div class="q-ml-md q-mt-md text-grey-7">
              <p class="font q-pb-xs">示例:</p>
              <div class="q-ml-md">#END</div>
              <div class="q-ml-md">
                #cover:cfg_nas&all#test1.sh#/ebank/test1.sh
              </div>
              <div class="q-ml-md">
                #add:cfg_core&all#test.png#/ebank/spdb/configs/test.png
              </div>
            </div>
            <div class="font q-ml-md q-mt-md  q-pb-xs text-grey-7">
              说明:
            </div>
            <ol class="text-grey-7">
              <li>
                文件操作类型默认为“cover”，若为新增配置文件，则操作类型需要修改为"add";若为更新配置文件，则操作类型为默认类型。
              </li>
              <li>
                配置文件变更需要生成template文件的模块类型有：cfg_nas(老NAS应用配置文件更新)、cfg_core(配置中心配置文件更新）。
              </li>
              <li>
                数据库类文件变更需要生成temlate文件的模块类型有：cfg*_*（数据库变更前中后配置文件更新）。
              </li>
            </ol>
            <div class="text-center">
              <fdev-btn
                type="submit"
                v-show="isConfirmTemplate"
                label="确认template.properties内容正确"
                @click="isConfirmTemplate = false"
              />
            </div>
          </div>
        </div>
        <transition name="fade">
          <div class="progress" v-show="toggleFile">
            <div class="step-position bg-white">
              <fdev-stepper
                v-model="stepNow"
                animated
                flat
                class="stepper"
                dense
                v-if="autoType === '1'"
              >
                <fdev-step
                  v-for="(item, index) in step"
                  class="step"
                  :key="index"
                  :name="index"
                  :title="item"
                  icon="assignment"
                  :done="stepNow > index"
                  :error="traceInfo.status === '4' && stepNow === index"
                />
              </fdev-stepper>
              <fdev-stepper
                v-model="stepNow"
                color="primary"
                animated
                flat
                class="stepper"
                dense
                v-if="autoType === '0'"
              >
                <fdev-step
                  v-for="(item, index) in deAutoStep"
                  class="step"
                  :key="index"
                  :name="index"
                  :title="item"
                  icon="assignment"
                  :done="stepNow > index"
                  :error="traceInfo.status === '4' && stepNow === index"
                />
              </fdev-stepper>
              <fdev-bar class="bar-border bg-grey-2">
                <fdev-space />
                <fdev-btn
                  flat
                  icon="arrow_upward"
                  color="black"
                  @click="scroll('top')"
                />
                <fdev-btn
                  flat
                  icon="arrow_downward"
                  color="black"
                  @click="scroll('bottom')"
                />
              </fdev-bar>
            </div>
            <fdev-scroll-area ref="scrollArea" class="scroll-wrapper">
              <pre class="code" id="code">
              <code v-html="traceInfo.auto_release_log"/>
            </pre>
            </fdev-scroll-area>
          </div>
        </transition>
      </div>
      <template v-slot:btnSlot>
        <div class="m-page-left full-width">
          <fdev-tooltip
            anchor="center middle"
            v-if="
              confirmBtnDisable ||
                !awsCommonGroupIsRight ||
                !awsStaticGroupIsRight ||
                isConfirmTemplate ||
                !canOperation ||
                !overdue
            "
          >
            <span v-if="!overdue">
              当前变更已过期
            </span>
            <span v-else-if="!canOperation">
              请联系投产管理员开始介质准备
            </span>
            <span
              v-else-if="
                pro_image_uri.includes(false) && deployTypeArr.includes(false)
              "
            >
              缺少镜像标签和部署平台
            </span>
            <span
              v-else-if="
                pro_image_uri.includes(false) && !deployTypeArr.includes(false)
              "
            >
              缺少镜像标签
            </span>
            <span
              v-else-if="
                !pro_image_uri.includes(false) && deployTypeArr.includes(false)
              "
            >
              缺少部署平台
            </span>
            <span v-else-if="!awsCommonGroupIsRight">
              AWS_COMMON介质目录对象存储用户所属组不正确，请前往配置文件更新页修改
            </span>
            <span v-else-if="!awsStaticGroupIsRight">
              AWS_STATIC介质目录对象存储用户所属组不正确，请前往配置文件更新页修改
            </span>
            <span v-else-if="isConfirmTemplate">
              请确认template.properties内容无误
            </span>
            <span v-else-if="traceInfo.status === '1'">
              变更介质准备中
            </span>
            <span v-else>
              暂无变更介质文件
            </span>
          </fdev-tooltip>
          <fdev-btn
            type="submit"
            class="full-width"
            @click="handleData"
            :disable="
              confirmBtnDisable ||
                !awsCommonGroupIsRight ||
                !awsStaticGroupIsRight ||
                isConfirmTemplate ||
                !canOperation ||
                !overdue
            "
            label="开始介质准备"
          />
        </div>
      </template>
    </f-dialog>
  </f-block>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import { validate, exportExcel } from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
export default {
  name: 'assetsReview',
  data() {
    return {
      toggleFile: false,
      timer: null,
      configFile: '',
      catalog: '/ebank/spdb/configs',
      textareaShow: false,
      isConfirmTemplate: true
    };
  },
  props: {
    value: {
      type: Boolean
    },
    data: null,
    prod_id: {
      type: String
    },
    autoType: {
      type: String
    },
    pro_image_uri: {
      type: Array
    },
    deployTypeArr: {
      type: Array
    },
    awsCommonGroupIsRight: {
      type: Boolean,
      default: true
    },
    awsStaticGroupIsRight: {
      type: Boolean,
      default: true
    },
    canOperation: Boolean,
    overdue: Boolean
  },
  validations() {
    if (this.configAssets.length > 0) {
      return {
        catalog: {
          required
        }
      };
    }
  },
  watch: {
    toggleFile(val) {
      if (val === false) {
        clearInterval(this.timer);
      }
    },
    catalog(val) {
      if (val) {
        this.isConfirmTemplate = true;
      }
    },
    configFile(val) {
      if (val) {
        this.isConfirmTemplate = true;
      }
    }
  },
  computed: {
    ...mapState('releaseForm', {
      traceInfo: 'traceInfo',
      step: 'step',
      deAutoStep: 'deAutoStep',
      prodDirection: 'prodDirection'
    }),
    stepNow() {
      if (
        this.traceInfo.status === '4' ||
        this.traceInfo.auto_release_stage === '7'
      ) {
        clearInterval(this.timer);
      }
      return this.traceInfo.auto_release_stage
        ? Number(this.traceInfo.auto_release_stage)
        : 0;
    },
    configAssets() {
      let data = [];
      for (let i = 0; i < this.data.length; i++) {
        const catalog = this.data[i];
        if (catalog.catalog_type === '4' && catalog.children.length > 0) {
          data = data.concat(
            catalog.children.map(item => {
              if (item.asset_templateContent) {
                let { asset_templateContent } = item;
                return asset_templateContent;
              } else {
                let children_assets = item.children;
                if (Array.isArray(children_assets)) {
                  let children_templateContent = children_assets.map(item => {
                    let { asset_templateContent } = item;
                    return asset_templateContent;
                  });
                  return children_templateContent;
                } else {
                  return [];
                }
              }
            })
          );
        }
      }
      let flatData = data.reduce(function(pre, cur) {
        return pre.concat(cur);
      }, []);
      return flatData;
    },
    needTemplateProp() {
      return this.configAssets.length > 0 && this.configFile.trim() === '';
    },
    confirmBtnDisable() {
      return (
        this.data.length === 0 ||
        this.pro_image_uri.includes(false) ||
        this.deployTypeArr.includes(false) ||
        this.traceInfo.status === '1' ||
        this.needTemplateProp
      );
    }
  },
  methods: {
    ...mapActions('releaseForm', {
      queryTrace: 'queryTrace',
      exportProdDirection: 'exportProdDirection'
    }),
    async handleData() {
      if (this.configAssets.length !== 0) {
        this.configFile = this.configFile.trim() + '\n';
        this.$v.catalog.$touch();
        validate([this.$refs.catalog]);
        if (this.$v.catalog.$invalid) {
          return;
        }
      }
      this.toggleFile = true;
      if (this.traceInfo.status === '1' || this.traceInfo.status === '3') {
        this.$q
          .dialog({
            title: '',
            message: '请确认是否重新发起介质准备？',
            cancel: true,
            persistent: true
          })
          .onOk(async () => {
            await this.$emit('click', '1', this.prod_id, this.configFile);
          });
      } else {
        await this.$emit('click', '1', this.prod_id, this.configFile);
      }
    },
    async start() {
      clearInterval(this.timer);
      await this.queryTrace({ prod_id: this.prod_id });
      if (this.traceInfo.status === '1') {
        this.timer = setInterval(() => {
          this.queryTrace({ prod_id: this.prod_id });
        }, 2000);
      }
    },
    createConfigFile() {
      if (this.configAssets.length === 0) {
        return;
      }
      this.textareaShow = true;
      let configFile = ['#END'].concat(this.configAssets);
      this.configFile = configFile.join('\n');
    },
    scroll(item) {
      const position =
        item === 'top' ? 0 : document.getElementById('code').clientHeight;
      this.$refs.scrollArea.setScrollPosition(position, 300);
    },
    async exportFile() {
      await this.exportProdDirection({ prod_id: this.prod_id });
      const exportData = {
        data: this.prodDirection
      };
      exportExcel(exportData, '发布说明', 'text/plain');
    }
  },
  destroyed() {
    clearInterval(this.timer);
  },
  async created() {
    await this.start();
    this.createConfigFile();
    if (this.traceInfo.status === '1') {
      this.toggleFile = true;
    }
    if (this.configAssets.length === 0) {
      this.isConfirmTemplate = false;
    }
  }
};
</script>

<style lang="stylus" scoped>
.progress
  pre
    width 100%
    overflow auto
    background black
    color white
.step-position
  width 100%
  border-radius 0
.code
  margin 0
.bar-border
  border 1px solid #e5e5e5
.scroll-wrapper
  height 500px
.stepper >>> .q-stepper__content
  display none!important
p
  margin 0
.font
  font-weight: 700
.m-page-left
  padding-left: 32px;
</style>
