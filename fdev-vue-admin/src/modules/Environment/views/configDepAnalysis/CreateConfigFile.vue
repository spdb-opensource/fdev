<template>
  <Loading :visible="loading">
    <fdev-table
      class="my-sticky-column-table q-py-md"
      :data="tableDatas"
      :columns="columns"
      selection="multiple"
      row-key="id"
      noExport
      title="配置依赖列表"
      titleIcon="list_s_f"
      :selected.sync="selected"
      :on-search="search"
      :visible-columns="visibleColumns"
    >
      <template v-slot:top-right>
        <Authorized
          :role-authority="['环境配置管理员']"
          class="row justify-center"
        >
          <div>
            <fdev-btn
              normal
              v-show="tableDatas.length"
              :disable="!selected.length"
              label="生成配置文件"
              @click="createConfigFile"
            />
            <fdev-tooltip
              v-if="!selected.length"
              anchor="top middle"
              self="bottom middle"
              :offset="[10, 10]"
              >请选择需要生成配置文件的应用</fdev-tooltip
            >
          </div>
        </Authorized>
      </template>
      <template v-slot:top-bottom>
        <div class="row">
          <f-formitem class="col-4" bottom-page label="映射值">
            <fdev-input
              clearable
              v-model="model.mappingValue"
              @keyup.enter="search"
              @input="mappingValueChange"
            />
          </f-formitem>
          <f-formitem
            class="col-4"
            bottom-page
            label="实体英文名"
            label-style="padding-left:50px"
          >
            <fdev-select
              clearable
              use-input
              v-model="model.entityObj"
              :options="entityOptions"
              option-label="name_en"
              @filter="nameFilter"
              @input="entityChange"
              ref="model.entityObj"
              :rules="[() => $v.model.entityObj.required || '请输入实体英文名']"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.name_en">{{
                      scope.opt.name_en
                    }}</fdev-item-label>
                    <fdev-item-label :title="scope.opt.name_cn" caption>
                      {{ scope.opt.name_cn }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem
            class="col-4"
            bottom-page
            label="属性"
            label-style="padding-left:80px"
          >
            <fdev-select
              clearable
              use-input
              v-model="model.attributeObj"
              :options="attributeOptions"
              option-label="nameEn"
              @filter="attributeFilter"
              @input="attributeChange"
              ref="model.attributeObj"
              :rules="[() => $v.model.attributeObj.required || '请输入属性']"
            >
              <template v-slot:option="scope">
                <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
                  <fdev-item-section>
                    <fdev-item-label :title="scope.opt.nameAll">{{
                      scope.opt.nameAll
                    }}</fdev-item-label>
                    <fdev-item-label :title="scope.opt.nameCn" caption>
                      {{ scope.opt.nameCn }}
                    </fdev-item-label>
                  </fdev-item-section>
                </fdev-item>
              </template>
            </fdev-select>
          </f-formitem>
          <f-formitem class="col-4" bottom-page label="选择搜索范围">
            <fdev-select
              clearable
              multiple
              v-model="model.range"
              :options="branchs"
              ref="model.range"
              :rules="[() => $v.model.range.required || '请选择搜索范围']"
            />
          </f-formitem>
        </div>
      </template>
      <template v-slot:body-cell-gitlab="props">
        <fdev-td class="text-ellipsis" :title="props.value || '-'">
          <!-- <fdev-tooltip
            anchor="top middle"
            self="center middle"
            :offest="[0, 0]"
            v-if="props.value"
          >
            {{ props.value }}
          </fdev-tooltip> -->
          {{ props.value || '-' }}
        </fdev-td>
      </template>
    </fdev-table>
    <f-dialog
      v-model="resultDialogShow"
      right
      f-sc
      title="配置生成失败应用列表"
    >
      <!-- <div class="table-wrapper bg-white"> -->
      <fdev-list boardered padding class="q-mb-md text-center">
        <fdev-item
          v-ripple
          v-for="(item, index) in errorConfigList"
          :key="index"
        >
          <fdev-item-section>{{
            ` ${item.branch} 分支下的 ${
              item.appName
            } 应用生成配置文件失败，失败原因：${item.errorInfo}`
          }}</fdev-item-section>
        </fdev-item>
      </fdev-list>
      <template v-slot:btnSlot>
        <fdev-btn dialog label="确定" @click="resultDialogShow = false" />
      </template>
      <!-- </div> -->
    </f-dialog>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import Authorized from '@/components/Authorized';
import { mapActions, mapState } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import { createConfigModel, configDepColumns } from '../../utils/constants';
import { validate, successNotify } from '@/utils/utils';

export default {
  name: 'CreateConfigFile',
  components: { Loading, Authorized },
  data() {
    return {
      loading: false,
      model: createConfigModel(),
      attributeOptions: [],
      attributeOptionsClone: [],
      entityOptions: [],
      entityOptionsClone: [],
      branchs: ['master', 'sit', 'release'],
      visibleColumns: [
        'name_cn',
        'name_en',
        'app_manager',
        'spdb_manager',
        'group',
        'gitlab',
        'branch'
      ],
      columns: configDepColumns().createConfigFilecolumns,
      tableDatas: [],
      selected: [],
      resultDialogShow: false,
      errorConfigList: []
    };
  },
  validations: {
    model: {
      entityObj: {
        required
      },
      attributeObj: {
        required
      },
      range: {
        required
      }
    }
  },
  computed: {
    ...mapState('environmentForm', [
      'configModelList',
      'confList',
      'envKeyList',
      'batchPreviewConfigFile',
      'modelEnvByValueList'
    ])
  },
  watch: {
    attributeOptionsClone: function(val) {
      if (val.length == 0) {
        this.model.attributeObj = '';
      }
    }
  },
  async created() {
    await this.queryConfigModel();
    await this.queryEnvKeyList();
    this.entityOptionsClone = this.configModelList.slice();
    this.attributeOptionsClone = this.envKeyList.slice();
  },
  methods: {
    ...mapActions('environmentForm', [
      'queryModelEnvByValue',
      'queryEnvKeyList',
      'queryConfigModel',
      'confDependList',
      'getBatchPreviewConfigFile'
    ]),
    chineseName(val) {
      return val
        .map(item => {
          return item.user_name_cn;
        })
        .join('，');
    },
    attributeFilter(val, update, abort) {
      update(() => {
        this.attributeOptions = this.attributeOptionsClone.filter(item => {
          return (
            item.nameEn.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            item.nameCn.indexOf(val) > -1
          );
        });
      });
    },
    nameFilter(val, update, abort) {
      update(() => {
        this.entityOptions = this.entityOptionsClone.filter(item => {
          return (
            item.name_en.toLowerCase().indexOf(val.toLowerCase()) > -1 ||
            item.name_cn.indexOf(val) > -1
          );
        });
      });
    },
    updateAttributeOptions(entityObj) {
      let res = [];
      this.envKeyList.map(item => {
        if (
          item.modelNameEn == entityObj.name_en &&
          item.modelNameCn == entityObj.name_cn
        ) {
          res.push(item);
        }
      });
      this.attributeOptionsClone = res;
    },
    attributeChange(val) {
      if (val) {
        let arr = this.configModelList;
        for (let i = 0, len = arr.length; i < len; i++) {
          if (arr[i].name_en == val.modelNameEn) {
            this.model.entityObj = arr[i];
            break;
          }
        }
      }
      if (!this.model.mappingValue) {
        this.updateAttributeOptions(this.model.entityObj);
      }
    },
    entityChange(val) {
      this.model.attributeObj = '';
      this.mappingValueChange();
    },
    async mappingValueChange() {
      if (!this.model.mappingValue && this.model.entityObj) {
        this.updateAttributeOptions(this.model.entityObj);
      } else if (!this.model.mappingValue && !this.model.entityObj) {
        this.attributeOptionsClone = this.envKeyList.slice();
      } else if (this.model.mappingValue) {
        let clone = this.envKeyList.slice();
        await this.queryModelEnvByValue({
          value: this.model.mappingValue,
          type: 'not_ci',
          labels: []
        });
        this.attributeOptionsClone = clone.filter(item => {
          let flag = false;
          this.modelEnvByValueList.map(item1 => {
            if (
              item1.field_name_en == item.nameEn &&
              item1.model_name_en == item.modelNameEn
            ) {
              flag = true;
            }
          });
          return flag;
        });
      }
    },
    async search() {
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
      this.loading = true;
      try {
        this.selected = [];
        await this.confDependList({
          model_name_en: this.model.entityObj.name_en,
          field_name_en: this.model.attributeObj.nameEn,
          range: this.model.range
        });
        this.tableDatas = this.confList;
        this.tableDatas.map((item, index) => {
          this.$set(item, 'id', index);
        });
        this.loading = false;
      } catch {
        this.loading = false;
      }
    },
    createConfigFile() {
      if (!this.selected.length) {
        return;
      } else {
        this.$q
          .dialog({
            title: '生成配置文件',
            message: '确认生成勾选应用的配置文件？',
            cancel: true,
            persistent: true
          })
          .onOk(async () => {
            this.loading = true;
            // this.resultDialogShow = true;
            try {
              let reqObj = [];
              this.selected.map(item => {
                reqObj.push({
                  gitlabId: item.gitlab_id,
                  appName: item.name_en,
                  appId: item.app_id,
                  branch: item.branch
                });
              });
              await this.getBatchPreviewConfigFile({
                list: reqObj
              });
              this.errorConfigList = [];
              this.batchPreviewConfigFile.map(item => {
                if (item.flag == '0') {
                  this.errorConfigList.push(item);
                }
              });
              if (this.errorConfigList.length) {
                this.resultDialogShow = true;
              } else {
                successNotify('生成配置文件成功！');
              }
              this.loading = false;
              this.selected = [];
            } catch {
              this.loading = false;
            }
          });
      }
    }
  }
};
</script>

<style lang="stylus" scoped>
.input
  min-width 200px
  max-width 300px
  padding-bottom 20px
.container
  display flex
  margin-bottom 20px
.container .col
  flex 1
  display flex
  align-items center
  justify-content center
.width150
  width 150px
.btn-height
  height 40px
// .my-sticky-column-table >>>
//   th:first-child,td:first-child,td:last-child,th:last-child
//     background-color #fff
//     opacity 1
  th:first-child,th:last-child
    color #9c9a9a
  th:first-child,td:first-child
    position sticky
    left 0
    z-index 1
  td:last-child,th:last-child
    position sticky
    right 0
    z-index 1
  .td-width
    max-width 300px
    overflow hidden
    text-overflow ellipsis
</style>
