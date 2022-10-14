<template>
  <f-dialog
    :value="value"
    @input="$emit('input', $event)"
    persistent
    :title="title"
    right
  >
    <div class="row justify-between" style="width:720px">
      <f-formitem
        required
        diaS
        label="小组名称"
        label-style="width:100px"
        value-style="width:220px"
      >
        <fdev-select
          ref="templateData.group"
          v-model="group"
          use-input
          emit-value
          map-options
          option-label="owner_group_name"
          :options="groupOptions"
          @filter="filterGroup"
          :rules="[val => !!val || '请选择所属小组']"
        />
      </f-formitem>

      <f-formitem
        required
        diaS
        label="小组标识"
        label-style="width:100px"
        value-style="width:220px"
      >
        <div class="row">
          <fdev-input
            readonly
            :title="groupAbbrInfo.group_abbr"
            class="col-10"
            v-model="groupAbbrInfo.group_abbr"
            :rules="[() => !!groupAbbrInfo.group_abbr || '请输入小组标识']"
          />
          <div
            class="row col-2 items-center justify-end"
            style="padding-bottom:20px"
          >
            <fdev-btn
              flat
              v-ripple
              @click.stop="group_abbr = groupAbbrInfo.group_abbr"
              :label="groupAbbrInfo.group_abbr ? '修改' : '新增'"
            >
              <fdev-popup-edit
                v-model="group_abbr"
                buttons
                @save="() => handleAdd(group_abbr)"
                :validate="required"
                @hide="required"
                :color="required(group_abbr) ? 'primary' : 'noop disabled'"
              >
                <fdev-input
                  autofocus
                  type="text"
                  v-model="group_abbr"
                  color="primary"
                  ref="group_abbr"
                  :rules="[() => $v.group_abbr.examine || '只能输入数字和字母']"
                />
              </fdev-popup-edit>
            </fdev-btn>
          </div>
        </div>
      </f-formitem>

      <f-formitem
        required
        diaS
        label="变更类型"
        label-style="width:100px"
        value-style="width:220px"
      >
        <fdev-select
          input-debounce="0"
          :options="template"
          ref="templateData.template_type"
          emit-value
          map-options
          option-label="name"
          :readonly="url === 'update'"
          option-value="template_type"
          v-model="$v.templateData.template_type.$model"
          :rules="[
            () => $v.templateData.template_type.required || '请选择变更类型'
          ]"
        />
      </f-formitem>

      <f-formitem
        required
        diaS
        label="小组系统缩写"
        label-style="width:100px"
        value-style="width:220px"
        v-if="groupAbbrInfo.group_abbr"
      >
        <div class="row">
          <fdev-input
            readonly
            :title="templateData.system_abbr"
            class="col-10"
            v-model="templateData.system_abbr"
            :rules="[() => !!templateData.system_abbr || '请输入小组系统缩写']"
          />
          <div
            class="row col-2 items-center justify-end"
            style="padding-bottom:20px"
          >
            <fdev-btn
              flat
              v-ripple
              :label="groupAbbrInfo.system_abbr ? '修改' : '新增'"
              @click.stop="system_abbr = groupAbbrInfo.system_abbr"
              color="primary"
            >
              <fdev-popup-edit
                v-model="system_abbr"
                buttons
                @save="() => handleGroupAbbrModel(system_abbr)"
                :validate="sysRequired"
                @hide="sysRequired"
                :color="sysRequired(system_abbr) ? 'primary' : 'noop disabled'"
              >
                <fdev-input
                  ref="system_abbr"
                  v-model="$v.system_abbr.$model"
                  :rules="[
                    () => $v.system_abbr.onlyNumber || '只能输入3位数字'
                  ]"
                />
              </fdev-popup-edit>
            </fdev-btn>
          </div>
        </div>
      </f-formitem>

      <f-formitem
        required
        diaS
        label="系统名称"
        label-style="width:100px"
        value-style="width:220px"
      >
        <fdev-select
          input-debounce="0"
          :readonly="url === 'update'"
          ref="templateData.owner_system"
          use-input
          @filter="systemFilter"
          :options="systemOptions"
          option-value="id"
          option-label="sysname_cn"
          v-model="$v.templateData.owner_system.$model"
          :rules="[
            () => $v.templateData.owner_system.required || '请选择系统名称'
          ]"
        />
      </f-formitem>

      <f-formitem
        diaS
        label="应用名称"
        label-style="width:100px"
        value-style="width:220px"
      >
        <fdev-select
          input-debounce="0"
          ref="templateData.owner_app"
          :readonly="url === 'update'"
          :options="typeNameOptions"
          option-label="name_en"
          v-model="$v.templateData.owner_app.$model"
          use-input
          :rules="[() => true]"
          @filter="userFilterChoice"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.name_en">
                  {{ scope.opt.name_en }}
                </fdev-item-label>
                <fdev-item-label caption :title="scope.opt.name_zh">
                  {{ scope.opt.name_zh }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>

      <f-formitem
        required
        full-width
        label="配置文件gitlab地址"
        v-if="templateData.owner_system"
        style="margin-bottom:20px"
      >
        <div class="row">
          <div
            class="row items-center text-grey-8 text-ellipsis"
            style="max-width:calc(100% - 50px)"
            :title="templateData.resource_giturl[0]"
            v-if="templateData.resource_giturl[0]"
          >
            <span class="imageUrl q-mr-sm">
              {{ templateData.resource_giturl[0] }}
            </span>
            <f-icon name="arrow_d_f" />
            <fdev-popup-proxy>
              <div v-for="data in templateData.resource_giturl" :key="data">
                <p class="q-mx-md q-my-sm">{{ data }}</p>
                <fdev-separator />
              </div>
            </fdev-popup-proxy>
          </div>
          <div class="text-center q-ml-md">
            <fdev-btn
              :label="templateData.resource_giturl[0] ? '修改' : '新增'"
              flat
              @click="handleGitlabModelOpen"
            />
          </div>
        </div>
      </f-formitem>
    </div>

    <fdev-separator />

    <div class="form-header q-my-sm">
      <fdev-btn-dropdown flat label="添加变更目录">
        <fdev-list>
          <fdev-item
            v-for="(item, index) in catalog"
            :key="index"
            clickable
            v-close-popup
            @click="selectCatalog(item)"
          >
            <fdev-item-section>
              <fdev-item-label>{{ item.catalog_name }}</fdev-item-label>
            </fdev-item-section>
          </fdev-item>
        </fdev-list>
      </fdev-btn-dropdown>
    </div>

    <div
      class="row items-center justify-between q-ml-md q-mb-sm"
      v-for="(item, index) in templateData.catalogs"
      :key="index"
    >
      <div class="row col-11">
        <div class="col-4">
          <span class="text-grey-3">目录：</span>
          <span>{{ item.catalog_name }}</span>
        </div>
        <div class="col-4">
          <span class="text-grey-3">步骤：</span>
          <span>{{ item.description }}</span>
        </div>
        <div class="col-4">
          <span class="text-grey-3">类型：</span>
          <span>{{ item.catalog_type | catalogsType }}</span>
        </div>
      </div>
      <fdev-btn ficon="delete" flat color="red" @click="deleted(index)" />
    </div>

    <f-dialog
      right
      v-if="templateData.owner_system"
      v-model="groupGitlabModelOpened"
      title="修改"
    >
      <div class="row q-mb-md">
        <span>配置文件gitlab地址</span
        ><f-icon
          name="add"
          class="right-icon text-primary"
          @click="addNewGiturl"
        ></f-icon>
      </div>
      <fdev-input
        input-debounce="0"
        type="text"
        v-for="(item, index) in $v.resource_giturl.$each.$iter"
        :key="index"
        class="col-10"
        :ref="`resource_giturl${index}`"
        v-model="item.value.$model"
        :rules="[
          () => item.value.required || '请输入gitlab项目地址',
          () => item.value.format || '格式不正确',
          () => item.value.isUnique || '地址已存在'
        ]"
      >
        <template>
          <fdev-btn
            ficon="delete"
            color="red"
            flat
            @click="removeGitlab(index)"
          />
        </template>
      </fdev-input>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          @click="handleGitlabModel"
          :loading="globalLoading['releaseForm/updateSysRlsInfo']"
          label="确认"
      /></template>
    </f-dialog>

    <template v-slot:btnSlot>
      <fdev-btn
        label="提交"
        :loading="globalLoading[`releaseForm/${loading}`]"
        dialog
        @click="handleUpdateTemplateAllTip"
      />
    </template>
  </f-dialog>
</template>

<script>
import { validate, successNotify } from '@/utils/utils';
import { mapState, mapActions } from 'vuex';
import { createTemplate, type } from '../../../utils/model';
import { required } from 'vuelidate/lib/validators';
export default {
  name: 'EditDialog',
  data() {
    return {
      templateData: createTemplate(),
      uploadData: {},
      systemDis: false,
      typeNameOptions: this.appData,
      selected: {},
      groupGitlabModelOpened: false,
      template: [
        {
          name: '灰度',
          template_type: 'gray'
        },
        {
          name: '生产',
          template_type: 'proc'
        }
      ],
      system_abbr: '',
      resource_giturl: '',
      systemOptions: [],
      group_abbr: '',
      groupAbbrInfo: {},
      group: null,
      groupOptions: [] // 小组下拉选项
    };
  },
  validations: {
    group: {
      required
    },
    system_abbr: {
      required,
      onlyNumber(val) {
        if (!val) {
          return true;
        }
        const reg = /[^0-9]/g;
        return !reg.test(val) && val.length === 3;
      }
    },
    group_abbr: {
      required,
      examine(val) {
        if (!val) {
          return true;
        }
        const reg = /^\w*$/;
        return reg.test(val);
      }
    },
    resource_giturl: {
      $each: {
        value: {
          required,
          format(val) {
            if (!val) {
              return true;
            }
            const reg = /^http:\/\/([\w\-.:/]*)([\w-])$/;
            return reg.test(val);
          },
          isUnique(val) {
            if (!val) {
              return true;
            }
            const data = this.resource_giturl.filter(
              item => item.value === val
            );
            return data.length === 1;
          }
        }
      }
    },
    templateData: {
      owner_system: {
        required
      },
      template_type: {
        required
      },
      owner_app: {},
      resource_giturl: {},
      system_abbr: { required }
    }
  },
  props: {
    value: {
      type: Boolean, // 控制弹窗显隐
      default: false
    },
    data: {}, // 点击编辑时，父组件传递的详情
    title: {
      type: String // 控制弹窗名
    },
    url: {},
    thirdLevelGroups: {
      type: Array
    }
  },
  watch: {
    data(val) {
      // 有时候父组件传递的详情值不能及时显示
      this.templateData = val;
      if (this.title === '编辑') {
        let systemObj = {
          sysname_cn: this.templateData.owner_system_name,
          id: this.templateData.owner_system
        };
        let obj = this.appData.find(
          app => app.id.indexOf(this.templateData.owner_app) > -1
        );
        let appObj = {
          name_en: obj ? obj.name_en : '',
          name_zh: this.templateData.owner_app_name,
          id: this.templateData.owner_app
        };
        this.$set(this.templateData, 'owner_system', systemObj);
        this.$set(this.templateData, 'owner_app', appObj);
      }
    },
    async value(val) {
      if (val === false) {
        this.templateData = createTemplate();
      } else {
        // 给小组赋值
        if (this.title === '编辑') {
          this.group = this.thirdLevelGroups.find(
            item => item.owner_groupId === this.data.owner_group
          );
        } else {
          // 新增时，对第三层级组及其子组按层级排序找到顶层组给小组赋值
          this.group = this.sortedGroups[0];
        }

        // 给小组下拉选项赋值
        this.groupOptions = this.thirdLevelGroups.slice();
        this.typeNameOptions = this.appData;
        // 获取变更目录列表
        this.getCatalog();
        this.queryGroupSystem();
        this.systemOptions = this.system;
      }
    },
    'templateData.owner_system': {
      handler(val, oldVal) {
        if (val) {
          const data = this.system.find(item => {
            return item.id === val.id;
          });
          this.templateData.resource_giturl =
            data && data.resource_giturl ? data.resource_giturl : [];
        }
      }
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('userForm', ['groups']),
    ...mapState('releaseForm', ['groupAbbr', 'catalog']),
    ...mapState('release', ['system', 'appData']),
    loading() {
      return this.title === '编辑' ? 'updateChangeTemplate' : 'createATemplate';
    },

    // 对第三层级组及子组按层级level升序排序
    sortedGroups() {
      const thirdLevelGroups = this.thirdLevelGroups.slice();
      return thirdLevelGroups.sort((a, b) => a.level - b.level);
    }
  },
  filters: {
    catalogsType(val) {
      return type[val];
    }
  },
  methods: {
    ...mapActions('release', ['getSystem', 'getAppData']),
    ...mapActions('releaseForm', [
      'updateChangeTemplate',
      'getCatalog',
      'updateSysRlsInfo',
      'queryGroupAbbr',
      'updateGroupSysAbbr',
      'updateGroupAbbr',
      'createATemplate'
    ]),
    handleUpdateTemplateAllTip() {
      this.$v.group.$touch();
      this.$v.templateData.$touch();

      let Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('templateData') > -1;
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.group.$invalid || this.$v.templateData.$invalid) {
        return;
      } else {
        this.handleUpdateTemplate();
      }
    },
    // 点击保存时，向父组件提交数据
    async handleUpdateTemplate() {
      const params = {
        ...this.templateData,
        owner_app: this.templateData.owner_app
          ? this.templateData.owner_app.id
          : '',
        owner_system: this.templateData.owner_system
          ? this.templateData.owner_system.id
          : '',
        catalogs:
          this.templateData.catalogs.length > 0
            ? JSON.stringify(this.templateData.catalogs)
            : '',
        owner_group: this.group
      };
      delete params.file;
      if (this.title === '编辑') {
        await this.updateChangeTemplate(params);
      } else {
        await this.createATemplate(params);
      }
      successNotify(`${this.title}成功`);
      this.$emit('submit', false);
    },
    // 删除变更目录
    deleted(index) {
      this.templateData.catalogs.splice(index, 1);
    },
    userFilterChoice(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.typeNameOptions = this.appData.filter(
          v =>
            v.name_zh.toLowerCase().indexOf(needle) > -1 ||
            v.name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    selectCatalog(val) {
      if (
        !this.templateData.catalogs.some(item => {
          return item.catalog_name === val.catalog_name;
        })
      ) {
        this.templateData.catalogs.push(val);
      }
    },
    async handleGitlabModel() {
      this.$v.resource_giturl.$touch();
      let Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('resource_giturl') > -1;
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.resource_giturl.$invalid) {
        return;
      }
      await this.updateSysRlsInfo({
        resource_giturl: this.resource_giturl.map(item => {
          return item.value;
        }),
        system_id: this.templateData.owner_system.id
      });
      successNotify('修改成功！');
      this.getSystem();
      this.groupGitlabModelOpened = false;
      this.templateData.resource_giturl = this.resource_giturl.map(item => {
        return item.value;
      });
    },
    handleGitlabModelOpen() {
      const { resource_giturl } = this.templateData;
      if (resource_giturl.length === 0) {
        this.resource_giturl = [];
      } else {
        this.resource_giturl = resource_giturl.map(item => {
          return { value: item };
        });
      }
      this.groupGitlabModelOpened = true;
    },
    systemFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase().trim();
        this.systemOptions = this.system.filter(
          v => v.sysname_cn.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    /* 添加gitlba地址 */
    addNewGiturl() {
      this.resource_giturl.push({ value: '' });
    },
    removeGitlab(index) {
      /* 删除gitlab地址 */
      this.resource_giturl.splice(index, 1);
    },
    async handleGroupAbbrModel() {
      this.$v.system_abbr.$touch();
      let Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('system_abbr') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.system_abbr.$invalid) {
        return;
      }
      await this.updateGroupSysAbbr({
        system_abbr: this.system_abbr,
        group_id: this.sortedGroups[0].owner_groupId
      });
      successNotify('小组系统缩写修改成功！');
      this.queryGroupSystem();
    },
    async queryGroupSystem() {
      await this.queryGroupAbbr({
        group_id: this.sortedGroups[0].owner_groupId
      });
      const { system_abbr, group_abbr } = (this.groupAbbrInfo = this.groupAbbr);
      this.templateData.system_abbr = system_abbr;
      this.templateData.group_abbr = group_abbr;
      this.group_abbr = group_abbr;
      this.system_abbr = system_abbr;
    },
    required(val) {
      if (!val) {
        return false;
      }
      const reg = /^\w*$/;
      return reg.test(val);
    },
    sysRequired(val) {
      if (!val) {
        return false;
      }
      const reg = /[^0-9]/g;
      return !reg.test(val) && val.length === 3;
    },
    async handleAdd(group) {
      this.$v.group_abbr.$touch();
      if (this.$v.group_abbr.$invalid) {
        return;
      }
      await this.updateGroupAbbr({
        group_id: this.sortedGroups[0].owner_groupId,
        group_abbr: group
      });
      this.queryGroupSystem();
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
          this.$emit('input', false);
        });
    },
    // 小组输入过滤
    filterGroup(val, update, abort) {
      update(() => {
        this.groupOptions = this.thirdLevelGroups.filter(v =>
          v.owner_group_name.toLowerCase().includes(val.toLowerCase().trim())
        );
      });
    }
  },
  created() {
    this.getSystem();
    this.getAppData();
  }
};
</script>

<style lang="stylus" scoped>
.form-header
  font-size 20px
  font-weight 700
.imageUrl
  max-width calc(100% - 30px)
  overflow: hidden
  text-overflow: ellipsis
  white-space: nowrap
  display: inline-block
  vertical-align top
.right-icon
  position absolute
  right 40px
</style>
