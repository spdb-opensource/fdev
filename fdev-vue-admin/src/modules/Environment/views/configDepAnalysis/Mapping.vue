<template>
  <Loading :visible="loading['environmentForm/queryModelEnvByValue']">
    <fdev-table
      :data="tableData"
      :columns="columns"
      class="q-py-md"
      noExport
      title="配置依赖列表"
      :on-search="init"
      titleIcon="list_s_f"
      :pagination.sync="pagination"
    >
      <template v-slot:top-bottom>
        <div class="row">
          <f-formitem
            class="col-4"
            bottom-page
            label-style="padding-left:60px"
            label="映射值"
          >
            <fdev-input
              clearable
              :value="terms.value"
              @input="saveValue($event)"
              ref="terms.value"
              :rules="[() => $v.terms.value.valueRequired || '请输入映射值']"
            />
          </f-formitem>
          <f-formitem
            class="col-4"
            bottom-page
            label-style="padding-left:30px"
            label="实体一级分类"
          >
            <fdev-select
              clearable
              :value="terms.type"
              @input="saveType($event)"
              :options="typeOptions"
              map-options
              emit-value
              option-label="label"
              option-value="value"
            />
          </f-formitem>
          <f-formitem
            class="col-4"
            bottom-page
            label-style="padding-left:50px"
            label="环境标签"
          >
            <fdev-select
              use-input
              :value="terms.labels"
              @input="saveLabels($event)"
              :options="labelOptions"
              map-options
              multiple
              emit-value
              option-label="label"
              option-value="value"
              @filter="labelFilter"
            />
          </f-formitem>
        </div>
      </template>
      <template v-slot:body-cell-model_name_cn="props">
        <fdev-td class="text-ellipsis">
          <router-link
            class="link"
            :to="`/envModel/modelList/${props.row.model_id}`"
            :title="props.value"
            v-if="props.value"
            >{{ props.value }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
          <div v-else title="-">-</div>
        </fdev-td>
      </template>

      <template v-slot:body-cell-model_name_en="props">
        <fdev-td class="text-ellipsis">
          <router-link
            class="link"
            :to="`/envModel/modelList/${props.row.model_id}`"
            :title="props.value"
            v-if="props.value"
            >{{ props.value }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
          <div v-else title="-">-</div>
        </fdev-td>
      </template>

      <template v-slot:body-cell-value="props">
        <fdev-td v-if="props.row.data_type === 'array'" class="array-td">
          <Chip
            v-for="(item, i) in props.value"
            :key="i"
            :data="item"
            :name="props.row.field_name_en + i"
          />
        </fdev-td>
        <fdev-td
          v-else-if="props.row.data_type === 'object'"
          class="text-ellipsis"
        >
          <Chip :data="props.value" :name="props.row.field_name_en" />
        </fdev-td>
        <fdev-td
          class="td-width"
          v-else
          :title="props.value ? props.value.split(';').join('<br/>') : '-'"
        >
          <!-- <fdev-tooltip
            anchor="top middle"
            self="bottom middle"
            :offest="[10, 10]"
          >
            <div v-html="props.value.split(';').join('<br/>')" />
          </fdev-tooltip> -->
          {{ props.value || '-' }}
        </fdev-td>
      </template>
      <template v-slot:body-cell-btn="props">
        <fdev-td class="text-center">
          <fdev-btn
            @click="handleConfigDepAnalysisOpen(props.row)"
            flat
            color="primary"
            label="应用依赖分析"
          />
        </fdev-td>
      </template>
    </fdev-table>

    <f-dialog v-model="configDepAnalysisOpened" right f-sc title="应用依赖分析">
      <!-- <Dialog
      v-model="configDepAnalysisOpened"
      title="应用依赖分析"
      :persistent="false"
    > -->
      <Loading :visible="loading['environmentForm/confDependList']">
        <fdev-table
          :data="configTableData"
          :columns="configDepAnalysisColumns"
          row-key="id"
          flat
          noExport
          style="width:1000px"
          class="table"
        >
          <template v-slot:body-cell-dev_managers="props">
            <fdev-td>
              <div
                :title="props.value.map(v => v.user_name_cn).join('，')"
                class="q-gutter-x-sm text-ellipsis"
              >
                <span v-for="(item, index) in props.value" :key="index">
                  <router-link
                    v-if="item.id"
                    :to="{ path: `/user/list/${item.id}` }"
                    class="link"
                    >{{ item.user_name_cn }}
                  </router-link>
                  <span v-else class="span-margin">{{
                    item.user_name_cn
                  }}</span>
                </span>
              </div>
            </fdev-td>
          </template>
          <template v-slot:body-cell-spdb_managers="props">
            <fdev-td>
              <div
                :title="props.value.map(v => v.user_name_cn).join('，')"
                class="q-gutter-x-sm text-ellipsis"
              >
                <span v-for="(item, index) in props.value" :key="index">
                  <router-link
                    v-if="item.id"
                    :to="{ path: `/user/list/${item.id}` }"
                    class="link"
                    >{{ item.user_name_cn }}
                  </router-link>
                  <span v-else class="span-margin">{{
                    item.user_name_cn
                  }}</span>
                </span>
              </div>
            </fdev-td>
          </template>
        </fdev-table>
      </Loading>
    </f-dialog>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState, mapGetters, mapMutations } from 'vuex';
import { mappingModel, configDepColumns } from '../../utils/constants';
import Chip from '../../components/Chip';
import { validate } from '@/utils/utils';

export default {
  name: 'Mapping',
  components: { Loading, Chip },
  data() {
    return {
      pagination: {},
      model: mappingModel(),
      typeOptions: [
        { label: 'CI', value: 'ci' },
        { label: '非CI', value: 'not_ci' }
      ],
      labelOptions: [],
      tableData: [],
      columns: configDepColumns().mappingColumns,
      configTableData: [],
      configDepAnalysisOpened: false,
      configDepAnalysisColumns: configDepColumns().configDepAnalysisColumns
    };
  },
  validations: {
    terms: {
      value: {
        valueRequired(value, terms) {
          if (!value) {
            return false;
          }
          return !!value.trim();
        }
      }
    }
  },
  watch: {
    pagination(val) {
      this.saveCurrentPage({
        rowsPerPage: val.rowsPerPage
      });
    }
  },
  computed: {
    ...mapState('global', ['loading']),
    ...mapState('environmentForm', ['modelEnvByValueList', 'confList']),
    ...mapGetters('environmentForm', ['envLablesOption']),
    ...mapState('userActionSaveEnv/mappingEnv', ['currentPage', 'terms'])
  },
  methods: {
    ...mapActions('environmentForm', [
      'queryModelEnvByValue',
      'queryAllLabels',
      'confDependList'
    ]),
    ...mapMutations('userActionSaveEnv/mappingEnv', [
      'saveCurrentPage',
      'saveValue',
      'saveType',
      'saveLabels'
    ]),
    async labelFilter(val, update, abort) {
      const needle = val.toLowerCase().trim();
      if (this.envLablesOption.length === 0) {
        await this.queryAllLabels();
      }
      update(() => {
        this.labelOptions = this.envLablesOption.filter(label =>
          label.value.includes(needle)
        );
      });
    },
    async handleConfigDepAnalysisOpen({
      model_name_en,
      field_name_en,
      labels
    }) {
      let range;
      if (
        field_name_en.startsWith('ci_') ||
        labels.includes('rel') ||
        labels.includes('pro')
      ) {
        range = ['master'];
      } else if (labels.includes('sit')) {
        range = ['sit'];
      } else if (labels.includes('uat')) {
        range = ['release'];
      } else {
        range = ['master', 'sit', 'release'];
      }
      this.configDepAnalysisOpened = true;
      await this.confDependList({
        model_name_en,
        field_name_en,
        range
      });
      this.configTableData = this.confList;
    },
    async init() {
      this.$v.terms.$touch();
      let Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('terms') > -1;
      });
      validate(
        Keys.map(key => {
          if (this.$refs[key] instanceof Array) {
            return this.$refs[key][0];
          }
          return this.$refs[key];
        })
      );
      if (this.$v.terms.$invalid) {
        return;
      }
      await this.queryModelEnvByValue(this.terms);
      this.tableData = this.modelEnvByValueList;
    }
  },
  mounted() {
    this.pagination = this.currentPage;
    if (this.terms.value) {
      this.init();
    }
  }
};
</script>

<style lang="stylus" scoped>
.input
  width 200px
  max-width 300px;
  display inline-block
  margin-right 8px
  vertical-align top
.td-width
	box-sizing border-box
	width 30%
	max-width 130px
	overflow hidden
	text-overflow ellipsis
.array-td
  max-width 400px
  white-space: pre;
.table
  width 1100px
</style>
