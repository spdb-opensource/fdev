<template>
  <f-block>
    <div class="bg-white q-pb-md">
      <fdev-card flat square class="q-pa-md">
        <fdev-card-section class="form">
          <div class="row q-col-gutter-x-md q-col-gutter-y-sm">
            <f-formitem class="col-4" label="实体模板英文名:">
              {{ model.nameEn }}
            </f-formitem>
            <f-formitem class="col-4" label="实体模板中文名:">
              {{ model.nameCn }}
            </f-formitem>
            <f-formitem class="col-4" label="描述:">
              {{ model.desc }}
            </f-formitem>
          </div>
        </fdev-card-section>
      </fdev-card>
      <fdev-table
        :data="model.envKey"
        :columns="columns"
        row-key="id"
        flat
        no-select-cols
        noExport
      >
        <template v-slot:body="props">
          <fdev-tr :props="props">
            <fdev-td
              :props="props"
              v-for="col in columns"
              :key="col.name"
              class="text-ellipsis"
            >
              <!-- 高级属性待定，暂时保留 -->
              <!-- <span v-if="col.name === 'require' || col.name === 'type'">
                {{ props.row[col.name] | filterRequire }}
              </span> -->
              <span
                v-if="col.name === 'data_type'"
                :title="props.row[col.name] | filterType"
              >
                {{ props.row[col.name] | filterType }}
              </span>
              <span v-else :title="props.row[col.name] || '-'">{{
                props.row[col.name] || '-'
              }}</span>
              <!-- 高级属性待定，暂时保留 -->
              <!-- <fdev-btn dense round v-if="col.name === 'name_en'" flat v-show="props.row.data_type && props.row.data_type !== ''" :icon="taggleIcon(props)" @click="props.expand = !props.expand" /> -->
            </fdev-td>
          </fdev-tr>
          <!-- 高级属性待定，暂时保留 -->
          <!-- <fdev-tr v-if="props.row.data_type && props.row.data_type !== ''" :props="props" v-show="showTable(props)">
            <fdev-td colspan="100%">
              <div class="text-left">
                <fdev-table :data="props.row.greatKey" :columns="keyColumns" row-key="id" flat hide-bottom :pagination.sync="pagination">
                </fdev-table>
              </div>
            </fdev-td>
          </fdev-tr> -->
        </template>
      </fdev-table>
      <div class="row justify-center">
        <!-- 高级属性待定，操作暂时保留 -->
        <!-- <fdev-btn v-if="isManagerRole && isSpecialGroup" label="修改" color="primary" text-color="white" class="q-mt-lg q-mr-lg" @click="update" /> -->
        <!-- <fdev-btn
          v-if="isManagerRole"
          label="删除"
          color="red"
          text-color="white"
          class="q-mt-lg q-mr-lg"
          @click="handelDelete()"
        /> -->
      </div>
    </div>
  </f-block>
</template>

<script>
import { mapActions, mapState, mapGetters } from 'vuex';
import { modelTempColumns } from '../../utils/constants';
import { deepClone } from '@/utils/utils';

export default {
  name: 'ModelTempEdit',
  data() {
    return {
      id: '',
      model: {},
      columns: modelTempColumns().detailColumns,
      keyColumns: modelTempColumns().keyColumns,
      isManagerRole: false,
      selected: [],
      pagination: {
        rowsPerPage: 0
      }
    };
  },
  computed: {
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('environmentForm', {
      modelListData: 'modelTempList'
    }),
    ...mapGetters('user', ['isSpecialGroup'])
  },
  filters: {
    filterRequire(val) {
      return val === '0' ? '否' : '是';
    },
    filterType(val) {
      return val === '' || !val ? 'string' : val;
    }
  },
  methods: {
    ...mapActions('environmentForm', {
      getModelTempDetail: 'getModelTempDetail',
      deleteModel: 'deleteModel'
    }),
    ...mapActions('user', {
      fetchCurrent: 'fetchCurrent'
    }),
    async getDetail() {
      // 环境配置管理员才有权限增删改
      await this.fetchCurrent();
      let managerUser = this.currentUser.role.find(item => {
        return item.label === '环境配置管理员';
      });
      if (managerUser) {
        this.isManagerRole = true;
      }
      this.id = this.$route.params.id;
      await this.getModelTempDetail({
        id: this.id
      });
      this.model = deepClone(this.modelListData);

      // -----------------------------------目前暂定实体模板无高级属性，后续待定
      /* let index = 1;
      this.model.env_key.map(item => {
        if (item.json_schema && item.data_type !== '') {
          // item.json_schema返回是字符串或对象需留意
          let json_schema =
            item.data_type === 'array'
              ? JSON.parse(JSON.stringify(item.json_schema)).items
              : JSON.parse(JSON.stringify(item.json_schema));
          // 以下是生成table的columns
          const columnsArr = Object.keys(json_schema.properties);
          item.greatKey = columnsArr.map(item => {
            let required = false;
            try {
              required = json_schema.required.includes(item);
            } finally {
              return {
                name: item,
                desc: json_schema.properties[item].description,
                required: required
              };
            }
          });
          item.index = index++;
        }
      }); */
    },
    // ---------------------  暂定无高级属性和修改删除操作，后续待定    start
    /* update() {
      this.$router.push({
        path: '/envModel/modelTempEdit/update',
        query: {
          id: this.id
        }
      });
    }, */
    /* handelDelete() {
      this.$q
        .dialog({
          title: '确认删除',
          message: '您确定删除该实体吗?',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteModel({ id: this.id });
          successNotify('删除成功');
          this.$router.push('/envModel/modelList');
        });
    }, */
    // ----------------------待定   end
    goBack() {
      window.history.back();
    },
    showTable(props) {
      if (props.row.index && props.row.index === 1) {
        return !props.expand;
      } else if (props.row.index && props.row.index !== 1) {
        return props.expand;
      }
    },
    taggleIcon(props) {
      if (props.row.index && props.row.index === 1) {
        return !props.expand ? 'arrow_drop_up' : 'arrow_drop_down';
      } else if (props.row.index && props.row.index !== 1) {
        return props.expand ? 'arrow_drop_up' : 'arrow_drop_down';
      }
    }
  },
  async created() {
    this.getDetail();
  }
};
</script>

<style lang="stylus" scoped></style>
