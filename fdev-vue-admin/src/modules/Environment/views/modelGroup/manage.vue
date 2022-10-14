<template>
  <f-block>
    <Page>
      <fdev-stepper
        v-model="step"
        ref="stepper"
        flat
        class="stepper"
        animated
        transition-prev="fade"
        transition-next="fade"
        v-if="step < 3"
      >
        <fdev-step
          :name="1"
          title="选择实体组模块"
          icon="settings"
          :done="step > 1"
        >
          <fdev-card-section class="row q-mt-lg form">
            <div class="col-sm-9 col-xs-12">
              <f-formitem diaS label="实体组模板" required>
                <fdev-select
                  use-input
                  input-debounce="0"
                  ref="template"
                  v-model="$v.template.$model"
                  :options="filterTemplate"
                  :rules="[() => !$v.template.$error || '请选择实体组模板']"
                  @input="next"
                />
              </f-formitem>
            </div>
          </fdev-card-section>
        </fdev-step>
        <fdev-step
          :name="2"
          title="新增实体组"
          icon="assignment"
          :done="step > 2"
        >
          <fdev-card-section class="row q-mt-lg form">
            <div class="col-sm-9 col-xs-12">
              <f-formitem label="实体组名称" required>
                <fdev-input
                  ref="modelGroup.name"
                  v-model="$v.modelGroup.name.$model"
                  type="text"
                  :disable="path === 'update'"
                  :rules="[
                    () => !$v.modelGroup.name.$error || '请输入实体组名称'
                  ]"
                />
              </f-formitem>
            </div>
          </fdev-card-section>
          <fdev-card-section class="row">
            <ModelTable
              :models="models"
              :modelsList="modelsList"
              :terms="terms"
              :path="path"
              @click="addModels"
              @goBack="goStepBack"
              @filter="modelsFilter"
            ></ModelTable>
          </fdev-card-section>
        </fdev-step>
      </fdev-stepper>
    </Page>
  </f-block>
</template>

<script>
import ModelTable from './components/modelTable';
import { mapState, mapActions } from 'vuex';
import { errorNotify, validate, successNotify } from '@/utils/utils';
import Page from '@/components/Page';
import { createModelGroup } from '../../utils/constants';
import { required } from 'vuelidate/lib/validators';
import { deepClone } from '@/utils/utils';

export default {
  name: 'ModelGroupManage',
  components: {
    Page,
    ModelTable
  },
  data() {
    return {
      path: '',
      loading: false,
      step: 1,
      filterTemplate: [],
      modelGroup: createModelGroup(),
      template: '',
      modelsList: [],
      terms: [],
      copyModels: []
    };
  },
  validations: {
    template: {
      required
    },
    modelGroup: {
      name: {
        require(val) {
          if (!val) {
            return false;
          } else {
            return val.trim().length > 0;
          }
        }
      }
    }
  },
  computed: {
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('environmentForm', {
      modelType: 'modelType',
      models: 'models'
    }),
    pathName() {
      return this.path === 'add' ? '新增' : '编辑';
    }
  },
  watch: {},
  methods: {
    ...mapActions('environmentForm', {
      getType: 'getType',
      getModles: 'getModles',
      saveModel: 'saveModel',
      updateModels: 'updateModels'
    }),
    async next() {
      await this.getModles({ template: this.template });
      this.copyModels = deepClone(this.models);
      this.models.forEach((item, index) => {
        this.terms.push({ value: [] });
      });
      this.step = 2;
    },
    async addModels(ids) {
      this.$v.modelGroup.$touch();
      let Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('modelGroup') > -1;
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.modelGroup.$invalid) {
        return;
      }
      if (this.path === 'add') {
        let params = {
          name_cn: this.modelGroup.name,
          template: this.template,
          models: ids
        };
        await this.saveModel(params);
        successNotify('新增实体组成功');
      } else {
        let params = {
          models: ids,
          id: this.modelGroup.id
        };
        await this.updateModels(params);
        successNotify('编辑实体组成功');
      }
      this.$router.push('/envModel/modelGroupList');
    },
    goStepBack() {
      this.step = 1;
      this.template = '';
      this.terms = [];
      this.modelGroup = createModelGroup();
    },
    modelsFilter(value) {
      let firstList = [];
      value.forEach((val, index) => {
        val.value.filter((v, i) => {
          firstList = this.copyModels[index].modelsInfo.filter(item => {
            return (
              item.desc.toLowerCase().includes(v.toLowerCase()) ||
              item.name_en.toLowerCase().includes(v.toLowerCase()) ||
              item.name_cn.toLowerCase().includes(v.toLowerCase())
            );
          });
          if (i === 0) {
            this.models[index].modelsInfo = firstList;
          } else {
            let otherList = [];
            this.models[index].modelsInfo.forEach(info => {
              firstList.filter(key => {
                if (info.name_cn === key.name_cn) {
                  otherList.push(key);
                }
              });
            });
            this.models[index].modelsInfo = otherList;
          }
        });
        if (val.value.length === 0) {
          this.models[index].modelsInfo = this.copyModels[index].modelsInfo;
        }
      });
    }
  },
  async created() {
    let role_names = this.currentUser.role.map(each => each.name);
    if (!role_names.includes('环境配置管理员')) {
      errorNotify('当前用户无权限进入实体操作页面');
      this.$router.push('/envModel/modelGroupList');
      return;
    }
    this.path = this.$route.path.split('/')[
      this.$route.path.split('/').length - 1
    ];
    // this.path = this.$route.params.path;
    if (this.path === 'update') {
      this.step = 2;
      this.modelGroup.name = this.$route.query.name;
      this.modelGroup.id = this.$route.query.id;
      this.template = this.$route.query.temp;
      this.modelsList = this.$route.query.models;
      await this.getModles({ template: this.template });
      this.copyModels = deepClone(this.models);
      this.models.forEach((item, index) => {
        this.terms.push({ value: [] });
      });
    }
    // this.$route.meta.name = this.pathName + '实体组';
    if (this.path === 'add') {
      await this.getType();
      this.filterTemplate = this.modelType.map(item => item.template);
    }
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
.q-card__section
  padding-bottom 0
</style>
