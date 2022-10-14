<template>
  <div>
    <fdev-table
      noExport
      title="配置文件列表"
      titleIcon="list_s_f"
      :data="configTreeData"
      row-key="module_name"
      :columns="configListColumns"
      :expanded.sync="expanded"
      no-select-cols
    >
      <template v-slot:no-data>
        <div class="full-width column items-center no-data-wrap">
          <f-image name="no_data_1" />
          <div class="no-data">没有可用数据</div>
        </div>
      </template>

      <template v-slot:top-right>
        <fdev-btn
          normal
          label="新增配置文件"
          ficon="add"
          @click="addDialogModel = true"
        >
        </fdev-btn>
      </template>

      <template v-slot:body="props">
        <fdev-tr :props="props">
          <fdev-td auto-width class="opt-td" key="opt">
            <f-icon
              v-if="
                props.row.note_configurations &&
                  props.row.note_configurations.length > 0
              "
              class="text-grey-3 cursor-pointer expand-icon"
              @click="expandFun(props)"
              :name="props.expand ? 'arrow_d_o' : 'arrow_r_o'"
            />
          </fdev-td>
          <fdev-td
            class="link text-left text-ellipsis cursor-pointer"
            key="module_name"
          >
            <span :title="props.row.module_name || '-'">{{
              props.row.module_name || '-'
            }}</span>
          </fdev-td>
          <fdev-td class="link text-left text-ellipsis cursor-pointer" key="ip">
            <span :title="props.row.ip || '-'">{{ props.row.ip || '-' }}</span>
          </fdev-td>
        </fdev-tr>
        <fdev-tr v-show="props.expand" :props="props">
          <fdev-td colspan="100%" class="table-first-level">
            <fdev-table
              no-select-cols
              noExport
              class="expand-table"
              v-if="props.row.note_configurations"
              :data="props.row.note_configurations"
              :columns="subConfigListColumns"
              hide-bottom
              row-key="id"
              :pagination.sync="pagination"
              :expanded.sync="expandedNoteConfigurations"
            >
              <template v-slot:no-data>
                <div class="full-width column items-center no-data-wrap">
                  <f-image name="no_data_1" />
                  <div class="no-data">没有可用数据</div>
                </div>
              </template>

              <template v-slot:body="props">
                <fdev-tr :props="props">
                  <fdev-td auto-width class="opt-td">
                    <f-icon
                      v-if="
                        props.row.diff_content &&
                          props.row.diff_content.length > 0
                      "
                      class="text-grey-3 cursor-pointer expand-icon"
                      :name="props.expand ? 'arrow_d_o' : 'arrow_r_o'"
                      @click="expandFun(props)"
                    />
                  </fdev-td>
                  <fdev-td class="text-ellipsis" key="fileName">
                    <a
                      v-if="props.row.file_url"
                      class="link cursor-pointer"
                      target="_blank"
                      :href="props.row.file_url"
                    >
                      {{ props.row.fileName }}
                    </a>
                    <span v-else :title="props.row.fileName || '-'">
                      {{ props.row.fileName || '-' }}
                    </span>
                    <fdev-popup-proxy context-menu>
                      <fdev-banner style="max-width:300px">
                        {{ props.row.fileName || '-' }}
                      </fdev-banner>
                    </fdev-popup-proxy>
                  </fdev-td>
                  <fdev-td class="text-ellipsis" key="file_principal">
                    <span :title="props.row.file_principal">
                      {{ props.row.file_principal }}
                    </span>
                  </fdev-td>
                  <fdev-td auto-width key="btn">
                    <div class="q-gutter-x-sm row no-wrap">
                      <fdev-btn label="编辑" flat @click="edit(props.row)" />
                      <div class="text-grey-1 sepreator">|</div>
                      <fdev-btn
                        label="删除"
                        flat
                        @click="deleteConfig(props.row)"
                      />
                    </div>
                  </fdev-td>
                </fdev-tr>
                <fdev-tr v-show="props.expand" :props="props">
                  <fdev-td
                    colspan="50%"
                    class="table-second-level sub my-sticky-column-table"
                  >
                    <div class=" diff-content">
                      <div class="row pb10">diff:</div>
                      <div
                        class="row"
                        v-for="(val, index) in props.row.diff_content"
                        :key="index"
                      >
                        <div
                          class="col-12 pd10"
                          v-if="props.row.module_name !== 'cfgbef_mbank'"
                        >
                          <f-icon
                            name="basic_msg_s_f"
                            class="text-blue-8 q-mr-sm"
                          /><span>{{ val.diffCity }}</span>
                        </div>
                        <div class="col-12" v-if="props.row.file_type === '2'">
                          <f-formitem
                            label-style="width:74px"
                            value-style="word-break:break-all;white-space:normal;"
                            full-width
                            label="修改前"
                          >
                            <div>
                              {{ val.before_content }}
                            </div>
                          </f-formitem>
                        </div>
                        <div class="col-12">
                          <f-formitem
                            full-width
                            label-style="width:74px"
                            value-style="word-break:break-all;white-space:normal;"
                            :label="
                              props.row.file_type === '2'
                                ? '修改后'
                                : '新增内容'
                            "
                          >
                            <div>
                              {{ val.after_content }}
                            </div>
                          </f-formitem>
                        </div>
                      </div>
                    </div>
                  </fdev-td>
                </fdev-tr>
              </template>
            </fdev-table>
          </fdev-td>
        </fdev-tr>
      </template>
    </fdev-table>

    <f-dialog right v-model="addDialogModel" :title="title" persistent>
      <f-formitem label="模块" diaS required>
        <fdev-select
          v-model="dialogModel.module_name"
          emit-value
          :options="moduleTypeOptions"
          ref="dialogModel.module_name"
          :disable="isEdit"
          @input="changeModuleName"
          :rules="[val => !!val || '请选择模块']"
        >
        </fdev-select>
      </f-formitem>

      <f-formitem label="IP地址" diaS required>
        <div
          class="row"
          style="margin-left: -7px;"
          v-if="dialogModel.module_name === 'cfg_core'"
        >
          <fdev-checkbox
            class="col-6"
            v-for="(item, index) in ipOptions"
            :key="index"
            v-model="cfgCoreSection"
            :label="item"
            :val="item"
            @input="changeCfgCoreSection"
          />
        </div>
        <div class="row" v-else>
          <span class="col-6" v-for="(item, index) in ipOptions" :key="index">
            {{ item }}
          </span>
        </div>
      </f-formitem>

      <f-formitem label="配置文件类型" class="q-mb-lg q-mt-md" diaS required>
        <fdev-radio
          class="q-mr-lg"
          v-model="dialogModel.file_type"
          val="1"
          label="文件新增"
        />
        <fdev-radio v-model="dialogModel.file_type" val="2" label="文件修改" />
      </f-formitem>

      <f-formitem label="文件名称" diaS required>
        <fdev-input
          v-model="dialogModel.fileName"
          ref="dialogModel.fileName"
          :rules="[val => !!val || '请输入文件名称']"
        />
      </f-formitem>

      <f-formitem label="文件路径" diaS>
        <fdev-input
          v-model="dialogModel.file_url"
          ref="dialogModel.file_url"
          hint=""
        />
      </f-formitem>

      <f-formitem
        required
        v-if="dialogModel.module_name === 'app_nas'"
        label="维护说明"
        value-style="width:300px"
      >
        <fdev-input
          type="textarea"
          v-model="dialogModel.safeguard_explain"
          ref="dialogModel.safeguard_explain"
          :rules="[val => !!val || '请输入维护说明']"
        />
      </f-formitem>

      <f-formitem
        required
        label="是否填写diff信息"
        :class="{ 'q-mb-lg': dialogModel.module_name === 'cfgbef_mbank' }"
      >
        <fdev-radio
          class="q-mr-lg"
          v-model="dialogModel.diff_flag"
          val="0"
          label="是"
          @input="changeDiffFlag"
        />
        <fdev-radio
          v-model="dialogModel.diff_flag"
          val="1"
          label="否"
          @input="changeDiffFlag"
        />
      </f-formitem>

      <div v-if="dialogModel.diff_flag !== '1'">
        <f-formitem
          label="diff新增"
          v-if="dialogModel.module_name !== 'cfgbef_mbank'"
          class="q-mb-lg q-mt-md"
          diaS
          required
        >
          <div style="margin-left: -7px;">
            <fdev-checkbox
              class="q-mr-lg"
              v-model="citySection"
              label="上海"
              val="sh"
              @input="val => changeCitySection(val, 'sh')"
            />
            <fdev-checkbox
              v-model="citySection"
              label="合肥"
              val="hf"
              @input="val => changeCitySection(val, 'hf')"
            />
          </div>
        </f-formitem>

        <div
          v-if="dialogModel.module_name === 'cfgbef_mbank'"
          class="file-content q-mb-md q-pt-md"
        >
          <f-formitem
            label-style="padding-left:20px;"
            value-style="margin-right:20px"
            v-if="dialogModel.file_type === '2'"
            label="修改前"
          >
            <fdev-input
              type="textarea"
              v-model="diffModelShanghai.before_content"
              ref="dialogModel.diffModelShanghai.before_content"
              :rules="[val => !!val || '请输入文件内容']"
            />
          </f-formitem>

          <f-formitem
            label-style="padding-left:20px;"
            value-style="margin-right:20px"
            :label="dialogModel.file_type === '2' ? '修改后' : '新增内容'"
          >
            <fdev-input
              type="textarea"
              v-model="diffModelShanghai.after_content"
              ref="dialogModel.diffModelShanghai.after_content"
              :rules="[val => !!val || '请输入文件内容']"
            />
          </f-formitem>
        </div>

        <div
          v-if="
            citySection.includes('sh') &&
              dialogModel.module_name !== 'cfgbef_mbank'
          "
          class="file-content q-mb-md q-pt-md"
        >
          <div class="row q-pb-sm" style="padding-left:20px">
            <f-icon name="basic_msg_s_f" class="text-blue-8 q-mr-sm" /><span
              >上海</span
            >
          </div>

          <f-formitem
            label-style="padding-left:20px;"
            value-style="margin-right:20px"
            v-if="dialogModel.file_type === '2'"
            label="修改前"
          >
            <fdev-input
              type="textarea"
              v-model="diffModelShanghai.before_content"
              ref="dialogModel.diffModelShanghai.before_content"
              :rules="[val => !!val || '请输入文件内容']"
            />
          </f-formitem>

          <f-formitem
            label-style="padding-left:20px;"
            value-style="margin-right:20px"
            :label="dialogModel.file_type === '2' ? '修改后' : '新增内容'"
          >
            <fdev-input
              type="textarea"
              v-model="diffModelShanghai.after_content"
              ref="dialogModel.diffModelShanghai.after_content"
              :rules="[val => !!val || '请输入文件内容']"
            />
          </f-formitem>
        </div>

        <div
          v-if="
            citySection.includes('hf') &&
              dialogModel.module_name !== 'cfgbef_mbank'
          "
          class="file-content q-pt-md"
        >
          <div class="row q-pb-sm" style="padding-left:20px">
            <f-icon name="basic_msg_s_f" class="text-blue-8 q-mr-sm" /><span
              >合肥</span
            >
          </div>
          <f-formitem
            v-if="dialogModel.file_type === '2'"
            label="修改前"
            label-style="padding-left:20px;"
            value-style="margin-right:20px"
          >
            <fdev-input
              type="textarea"
              v-model="diffModelHefei.before_content"
              ref="dialogModel.diffModelHefei.before_content"
              :rules="[val => !!val || '请输入文件内容']"
            />
          </f-formitem>

          <f-formitem
            label-style="padding-left:20px;"
            value-style="margin-right:20px"
            :label="dialogModel.file_type === '2' ? '修改后' : '新增内容'"
          >
            <fdev-input
              type="textarea"
              v-model="diffModelHefei.after_content"
              ref="dialogModel.diffModelHefei.after_content"
              :rules="[val => !!val || '请输入文件内容']"
            />
          </f-formitem>
        </div>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          label="确定"
          :loading="
            globalLoading['releaseForm/addNoteConfiguration'] ||
              globalLoading['releaseForm/updateNoteConfiguration']
          "
          @click="submit"
        />
      </template>
    </f-dialog>
  </div>
</template>
<script>
import { successNotify, errorNotify, deepClone } from '@/utils/utils';
import { mapActions, mapState } from 'vuex';
import {
  configListColumns,
  subConfigListColumns,
  moduleTypeOptions,
  mapIpList
} from '../../utils/constants';
import { releaseNoteConfigModel } from '../../utils/model';

export default {
  name: 'AutoReleaseNoteConfigFile',
  data() {
    return {
      module_name: '',
      type: '',
      owner_system: '',
      pagination: {
        rowsPerPage: 0
      },
      diffModelShanghai: {
        diffCity: 'SH',
        before_content: '',
        after_content: ''
      },
      diffModelHefei: {
        diffCity: 'HF',
        before_content: '',
        after_content: ''
      },
      dialogModel: releaseNoteConfigModel(),
      moduleTypeOptions: moduleTypeOptions, // 模块下拉选项
      ipOptions: [], // ip地址
      cfgCoreSection: [], // 模块为cfg_core时选中的ip
      citySection: [], // diff新增勾选的城市,
      lastModuleName: 'commonconfig', // 记录模块名更改前的最新值
      lastCitySection: ['sh', 'hf'], // 记录diff新增勾选的城市更改前的最新值
      addDialogModel: false, // 新建/编辑配置文件弹窗开关
      isEdit: false, // 是否是编辑配置文件
      note_id: '',
      expanded: [],
      expandedNoteConfigurations: [],
      configListColumns: configListColumns,
      subConfigListColumns: subConfigListColumns,
      configTreeData: []
    };
  },
  computed: {
    ...mapState('releaseForm', {
      noteConfiguration: 'noteConfiguration'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    title() {
      return this.isEdit ? '编辑配置文件' : '新建配置文件';
    },

    // 上海：'1'、合肥：'2'、上海&合肥：'3'
    cityFlag() {
      let result = '';
      const diffFlag = this.dialogModel.diff_flag;
      if (this.dialogModel.module_name === 'cfgbef_mbank') {
        result = '';
      } else {
        if (diffFlag === '0') {
          const cityArr = this.citySection;
          if (cityArr.length === 0) {
            result = '';
          } else if (cityArr.length === 1) {
            result = cityArr.includes('sh') ? '1' : '2';
          } else {
            result = '3';
          }
        }
      }
      return result;
    }
  },
  watch: {
    // 监听弹窗的关闭
    addDialogModel(val) {
      if (val) {
        // 打开弹窗
        if (!this.isEdit) {
          // 新增
          this.citySection = ['sh', 'hf'];
          this.ipOptions = this.getIpList('commonconfig', ['sh', 'hf']);
        }
      } else {
        // 关闭弹窗，恢复初始值
        this.isEdit = false;
        this.citySection = [];
        this.ipOptions = [];
        this.cfgCoreSection = [];
        this.lastModuleName = 'commonconfig';
        this.lastCitySection = ['sh', 'hf'];
        this.dialogModel = releaseNoteConfigModel();
        this.diffModelShanghai = {
          diffCity: 'SH',
          before_content: '',
          after_content: ''
        };
        this.diffModelHefei = {
          diffCity: 'HF',
          before_content: '',
          after_content: ''
        };
      }
    },

    'dialogModel.file_type': {
      deep: true,
      handler(val) {
        if (val && val === '1') {
          this.diffModelShanghai.before_content = '';
          this.diffModelHefei.before_content = '';
        }
      }
    }
  },

  methods: {
    ...mapActions('releaseForm', {
      updateNoteConfiguration: 'updateNoteConfiguration',
      deleteNoteConfiguration: 'deleteNoteConfiguration',
      addNoteConfiguration: 'addNoteConfiguration',
      queryNoteConfiguration: 'queryNoteConfiguration'
    }),
    expandFun(props) {
      if (props.row.module_name) {
        this.module_name = props.row.module_name;
      }
      props.expand = !props.expand;
    },

    // 根据模块名称和勾选的城市得到ip地址列表
    getIpList(moduleName, citySection) {
      let result = [];
      if (moduleName === 'cfgbef_mbank') {
        result = mapIpList['cfgbef_mbank'].slice();
      } else {
        const keyword = this.type + '-' + this.getSysType();
        if (citySection.includes('sh')) {
          result = result.concat(
            mapIpList[moduleName][keyword + '-sh'].slice()
          );
        }
        if (citySection.includes('hf')) {
          result = result.concat(
            mapIpList[moduleName][keyword + '-hf'].slice()
          );
        }
      }
      return result;
    },

    // 修改选择模块
    changeModuleName(newVal) {
      if (newVal) {
        this.lastModuleName = newVal;
      }
      // 按backspace不可删除
      this.dialogModel.module_name = newVal ? newVal : this.lastModuleName;

      // 给diff新增 城市都勾选上
      this.citySection = ['sh', 'hf'];

      // IP地址赋值
      if (this.dialogModel.module_name === 'cfgbef_mbank') {
        this.ipOptions = deepClone(mapIpList['cfgbef_mbank']);
      } else {
        this.ipOptions = this.getIpList(newVal, ['sh', 'hf']);
        // 模块选择为cfg_core，IP地址复选框勾选
        if (newVal === 'cfg_core') {
          this.cfgCoreSection = this.getIpList('cfg_core', ['sh', 'hf']);
        }
      }
    },

    // 修改diff新增城市勾选
    changeCitySection(newVal, handle) {
      // handle: 记录用户操作的是'sh'还是'hf'
      // handleType: 记录用户是增加勾选还是取消勾选
      let handleType;
      if (this.lastCitySection.length > newVal.length) {
        handleType = 'cancel';
      } else {
        handleType = 'add';
      }
      this.lastCitySection = this.citySection.slice();
      if (this.dialogModel.module_name === 'cfg_core') {
        this.ipOptions = this.getIpList(this.dialogModel.module_name, [
          'sh',
          'hf'
        ]);
        if (handleType === 'add') {
          this.cfgCoreSection = this.cfgCoreSection.concat(
            this.getIpList(this.dialogModel.module_name, [handle])
          );
        } else {
          const arr1 = this.cfgCoreSection.slice();
          const arr2 = this.getIpList(this.dialogModel.module_name, [handle]);
          this.cfgCoreSection = this.diffSet(arr1, arr2);
        }
      } else {
        this.ipOptions = this.getIpList(this.dialogModel.module_name, newVal);
      }
    },

    // 修改IP地址复选框勾选(此时module_name必定为'cfg_core')
    changeCfgCoreSection(newVal) {
      const keyword = this.type + '-' + this.getSysType();
      this.spliceCity(keyword, newVal, 'sh');
      this.spliceCity(keyword, newVal, 'hf');
      this.lastCitySection = this.citySection.slice();
    },

    // 修改 是否填写diff信息
    changeDiffFlag(newVal) {
      if (newVal === '1') {
        const moduleName = this.dialogModel.module_name;
        // IP地址赋值
        if (moduleName === 'cfgbef_mbank') {
          this.ipOptions = deepClone(mapIpList['cfgbef_mbank']);
        } else {
          this.ipOptions = this.getIpList(moduleName, ['sh', 'hf']);
          // 模块选择为cfg_core，IP地址复选框勾选
          if (moduleName === 'cfg_core') {
            this.cfgCoreSection = this.getIpList('cfg_core', ['sh', 'hf']);
          }
        }
      } else {
        const moduleName = this.dialogModel.module_name;
        if (moduleName === 'cfg_core') {
          this.changeCfgCoreSection(this.cfgCoreSection);
        } else {
          this.changeCfgCoreSection(this.ipOptions);
        }
      }
    },

    // 根据已勾选的ip地址来取消/增加勾选城市
    spliceCity(keyword, cfgCoreSection, city) {
      const targetIps = mapIpList['cfg_core'][`${keyword}-${city}`];
      let flagArr = [];
      cfgCoreSection.forEach(item => {
        targetIps.includes(item) ? flagArr.push(true) : flagArr.push(false);
      });
      const index = this.citySection.indexOf(`${city}`);
      if (index > -1) {
        if (!flagArr.includes(true)) {
          this.citySection.splice(index, 1);
        }
      } else {
        if (flagArr.includes(true)) {
          this.citySection.push(city);
        }
      }
    },

    getSysType() {
      if (this.owner_system.includes('个人手机')) {
        return 'phone';
      } else if (this.owner_system.includes('个人网银')) {
        return 'network';
      } else if (this.owner_system.includes('批量')) {
        return 'batch';
      } else if (this.owner_system.includes('柜面')) {
        return 'otc';
      } else {
        return 'phone';
      }
    },

    // 两数组求差集(arr1-arr2)
    diffSet(arr1, arr2) {
      const set1 = new Set(arr1);
      const set2 = new Set(arr2);
      let result = [];
      for (let item of set1) {
        if (!set2.has(item)) {
          result.push(item);
        }
      }
      return result;
    },

    // 点击编辑，打开编辑配置文件弹窗
    edit(val) {
      this.isEdit = true;
      this.dialogModel = deepClone(val);
      this.ipOptions = [];
      this.cfgCoreSection = [];
      this.citySection = [];
      if (val.diff_content && val.diff_content.length > 0) {
        const valDeep = deepClone(val);
        if (val.module_name === 'cfgbef_mbank') {
          this.diffModelShanghai = valDeep.diff_content[0];
        } else {
          const temp = valDeep.diff_content.find(v => v.diffCity === 'SH');
          if (temp) {
            this.diffModelShanghai = temp;
            this.citySection.push('sh');
          }
          const tempHf = valDeep.diff_content.find(v => v.diffCity === 'HF');
          if (tempHf) {
            this.diffModelHefei = tempHf;
            this.citySection.push('hf');
          }
        }
      }
      this.lastCitySection = this.citySection.slice();
      if (val.module_name === 'cfg_core') {
        this.ipOptions = this.getIpList('cfg_core', ['sh', 'hf']);
        this.cfgCoreSection = val.module_ip.split(',');
      } else {
        if (val.diff_flag === '1') {
          this.ipOptions = this.getIpList(val.module_name, ['sh', 'hf']);
        } else {
          this.ipOptions = this.getIpList(val.module_name, this.citySection);
        }
      }
      this.addDialogModel = true;
    },

    deleteConfig(val) {
      this.$q
        .dialog({
          title: '删除配置文件',
          message: `请确认是否删除${val.fileName}配置文件`,
          cancel: true
        })
        .onOk(async () => {
          await this.deleteNoteConfiguration({
            id: val.id
          });
          successNotify('删除成功');
          this.init();
        });
    },

    // 点击弹窗确认按钮，提交表单
    submit() {
      let formKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('dialogModel') > -1;
      });
      return Promise.all(
        formKeys.map(ele => this.$refs[ele].validate() || Promise.reject(ele))
      ).then(
        async v => {
          const moduleName = this.dialogModel.module_name;
          if (moduleName === 'cfg_core') {
            if (this.cfgCoreSection.length === 0) {
              errorNotify('请选择IP地址');
              return;
            } else {
              this.dialogModel.module_ip = this.cfgCoreSection.join(',');
            }
          } else {
            if (this.ipOptions.length === 0) {
              errorNotify('请选择diff新增');
              return;
            }
            this.dialogModel.module_ip = this.ipOptions.join(',');
          }
          if (
            this.dialogModel.diff_flag === '0' &&
            this.citySection.length === 0 &&
            moduleName !== 'cfgbef_mbank'
          ) {
            errorNotify('请选择diff新增');
            return;
          }
          this.dialogModel.diff_content = [];
          if (this.dialogModel.diff_flag !== '1') {
            if (this.citySection.includes('sh')) {
              this.dialogModel.diff_content.push(this.diffModelShanghai);
            }
            if (this.citySection.includes('hf')) {
              this.dialogModel.diff_content.push(this.diffModelHefei);
            }
            if (this.dialogModel.module_name === 'cfgbef_mbank') {
              this.diffModelShanghai.diffCity = '';
              this.dialogModel.diff_content.push(this.diffModelShanghai);
            }
          }
          const params = {
            ...this.dialogModel,
            catalog_type: '2',
            city: this.cityFlag,
            note_id: this.note_id
          };
          if (this.isEdit) {
            await this.updateNoteConfiguration(params);
            successNotify('编辑成功');
          } else {
            await this.addNoteConfiguration(params);
            successNotify('添加成功');
          }
          this.addDialogModel = false;
          this.init();
        },
        reason => {
          this.$refs[reason].focus();
        }
      );
    },

    async init() {
      await this.queryNoteConfiguration({
        note_id: this.note_id,
        catalog_type: 2
      });
      if (this.noteConfiguration && this.noteConfiguration.length > 0) {
        this.configTreeData = this.noteConfiguration.map(v => {
          let ips = [];
          if (v.note_configurations && v.note_configurations.length > 0) {
            v.note_configurations.map(val => {
              if (val.module_ip) {
                ips = ips.concat(val.module_ip.split(','));
              }
              return val;
            });
          }
          v.ip = Array.from(new Set(ips)).join(', ');
          return v;
        });
      } else {
        this.configTreeData = [];
      }
    }
  },
  created() {
    this.note_id = this.$route.params.id;
    this.init();
    this.type = this.$q.sessionStorage.getItem('type');
    this.owner_system = this.$q.sessionStorage.getItem('owner_system_name');
  }
};
</script>
<style lang="stylus" scoped>
.sepreator
  line-height 32px
.diff-content
  margin 16px
  padding 20px
  border 1px solid #DDDDDD
  border-radius 8px
.table-first-level
  padding 16px
.table-second-level
  margin 16px
  padding 0
.pb10
  padding-bottom 10px
.pd10
  padding-bottom 10px
  padding-top 10px
/deep/ .scroll-normal
  overflow auto
.expand-table
  border 1px solid #ddd
  border-bottom none
.file-content
  border 1px dashed #BBBBBB
  border-radius 8px
</style>
