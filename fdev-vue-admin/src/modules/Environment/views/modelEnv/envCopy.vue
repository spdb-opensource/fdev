<template>
  <PageHeaderWrapper>
    <fdev-card flat square class="q-pa-md">
      <fdev-form @submit.prevent="handelEnvModel">
        <fdev-card-section class="form" v-if="modelList">
          <Field
            label="环境中文名"
            :label-col="4"
            align="right"
            class="q-pb-sm"
          >
            <fdev-select
              outlined
              ref="modelList.name_cn"
              dense
              v-model="$v.modelList.env.name_cn.$model"
              :options="option"
              option-label="name_cn"
              option-value="id"
              :rules="[
                () => $v.modelList.env.name_cn.update || '请重新选择环境中文名'
              ]"
            >
            </fdev-select>
          </Field>
          <Field
            label="环境英文名"
            :label-col="4"
            align="right"
            class="q-pb-sm"
          >
            <fdev-select
              outlined
              ref="modelList.name_en"
              dense
              v-model="$v.modelList.env.name_en.$model"
              :options="option"
              option-label="name_en"
              option-value="id"
            >
            </fdev-select>
          </Field>
          <Field
            label="描述"
            optional
            :label-col="4"
            align="right"
            class="q-pb-sm"
          >
            <fdev-input
              outlined
              ref="modelList.desc"
              type="textarea"
              class="input"
              dense
              v-model="$v.modelList.env.desc.$model"
            >
            </fdev-input>
          </Field>
        </fdev-card-section>
        <fdev-separator />
        <div v-for="(item, index) in $v.model.$each.$iter" :key="index">
          <div class="q-my-sm">
            <span>实体中文名：{{ item.name_cn.$model }}</span>
          </div>
          <fdev-markup-table flat>
            <thead>
              <tr>
                <th class="head">属性字段</th>
                <th>属性中文名</th>
                <th class="head">属性值</th>
              </tr>
            </thead>
          </fdev-markup-table>
          <fdev-card-section>
            <div class="container">
              <div
                class="row"
                v-for="(key, i) in item.env_key.$each.$iter"
                :key="i"
              >
                <fdev-input
                  outlined
                  type="text"
                  class="input col-4  q-pb-sm key-section"
                  v-model="key.name_en.$model"
                  dense
                  readonly
                />
                <fdev-input
                  outlined
                  type="text"
                  class="input col-4  q-pb-sm key-section"
                  v-model="key.name_cn.$model"
                  dense
                  readonly
                />
                <fdev-input
                  outlined
                  :ref="`modelList.${index}.${i}.name_cn`"
                  type="text"
                  :class="[
                    key.$model.require === '1' ? 'require' : '',
                    'input col-4  q-pb-sm key-section'
                  ]"
                  placeholder="请输入属性值"
                  :optional="key.$model.require !== '1'"
                  v-model="key.$model.value"
                  dense
                  :rules="[
                    () => key.value.need || `请输入${key.$model.name_cn}`
                  ]"
                />
              </div>
            </div>
          </fdev-card-section>
        </div>
        <div class="row justify-center">
          <fdev-btn
            label="复制实体与环境映射"
            :loading="loading"
            color="primary"
            text-color="white"
            class="q-mb-lg q-mr-lg"
            @click="handelEnvModel"
          />
          <fdev-btn
            type="button"
            label="返回"
            @click="goBack"
            color="primary"
            text-color="white"
            class="q-mb-lg q-mr-lg"
          />
        </div>
      </fdev-form>
    </fdev-card>
  </PageHeaderWrapper>
</template>

<script>
import PageHeaderWrapper from '@/components/PageHeaderWrapper';
import Field from '@/components/Field';
import { mapActions, mapState } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import { validate, deepClone } from '@/utils/utils';

export default {
  name: 'envCopy',
  components: {
    PageHeaderWrapper,
    Field
  },
  data() {
    return {
      path: '',
      id: '',
      modelList: {},
      loading: false,
      models: [],
      envs: [],
      cloneModel: [],
      hasCloneModel: false,
      model: [],
      option: []
    };
  },
  validations: {
    model: {
      $each: {
        env_key: {
          $each: {
            name_cn: {},
            name_en: {},
            value: {
              need(val, item) {
                if (item.require === '1') {
                  return val;
                } else {
                  return true;
                }
              }
            }
          }
        },
        name_cn: {}
      }
    },
    modelList: {
      env: {
        name_en: {
          required
        },
        name_cn: {
          required,
          update(value) {
            let reg = new RegExp(/.*(copy)$/g);
            let flag = reg.test(value);
            return !flag;
          }
        },
        desc: {}
      }
    }
  },
  computed: {
    ...mapState('environmentForm', {
      EnvModelList: 'EnvModelList', // 某环境下的一套实体
      envList: 'envList' //环境列表
    })
  },
  methods: {
    ...mapActions('environmentForm', {
      getEnvList: 'getEnvList', //查询环境列表
      getEnvWithModel: 'getEnvWithModel', //查询某个环境下的所有实体
      copyEnvWithModel: 'copyEnvWithModel' //拷贝环境与实体映射
    }),
    goBack() {
      this.$router.push('/envModel/modelEvnList');
    },
    async handelEnvModel() {
      this.$v.modelList.$touch();
      this.$v.model.$touch();
      let Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('modelList') > -1;
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.modelList.$invalid || this.$v.model.$invalid) {
        return;
      }
      this.handelEnvModelOpened();
    },
    handelEnvModelOpened() {
      return this.$q
        .dialog({
          title: `复制环境实体映射`,
          message: `您确定复制该环境与该实体的映射吗？`,
          ok: '确定',
          cancel: '取消'
        })
        .onOk(async () => {
          await this.copyEnvWithModel(this.modelList);
        });
    }
  },
  async created() {
    let params = {
      env: this.$route.query.env,
      model: this.$route.query.model
    };
    await this.getEnvWithModel(params);
    this.modelList = deepClone(this.EnvModelList);
    this.modelList.env.name_cn = this.modelList.env.name_cn + '-copy';
    this.modelList.env.name_en = this.modelList.env.name_en + '-copy';
    this.model = this.modelList.model;
    await this.getEnvList();
    this.option = this.envList.filter(env => {
      return env.id !== this.modelList.env.id;
    });
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
.key-section
  padding 1% 2%
.require:after
  content '*'
  color red
.q-field
  align-items center
.head
  width 33.3333%
.container
  max-height 1000px
  overflow auto
.container::-webkit-scrollbar
  width 5px
.container::-webkit-scrollbar-track
  background #ededed
</style>
