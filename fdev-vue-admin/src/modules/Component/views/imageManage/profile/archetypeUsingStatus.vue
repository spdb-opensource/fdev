<template>
  <Loading :visible="loading">
    <fdev-table
      class="my-sticky-column-table"
      title="骨架使用现状列表"
      :data="tableData"
      :columns="columns"
      :pagination.sync="pagination"
      :filter="filterValue"
      :filter-method="filter"
      ref="table"
      :visible-columns="visibleColumns"
      no-export
      no-select-cols
      :onSelectCols="updatevisibleColumns"
      title-icon="list_s_f"
    >
      <template v-slot:top-bottom>
        <!-- 骨架中文/英文名 -->
        <f-formitem label="骨架中文/英文名" class="q-pr-sm">
          <fdev-select
            :value="selectValue"
            @input="updateSelectValue($event)"
            multiple
            use-input
            hide-dropdown-icon
            ref="select"
            @new-value="addSelect"
          >
            <template v-slot:append>
              <f-icon
                name="search"
                class="text-primary"
                @click="setSelect($refs.select)"
              />
            </template>
          </fdev-select>
        </f-formitem>

        <!-- 版本类型 -->
        <f-formitem label="版本类型">
          <fdev-select
            :value="statusType"
            :options="typeOptions"
            map-options
            @input="init($event)"
            emit-value
          />
        </f-formitem>
      </template>

      <!-- 骨架英文名列 -->
      <template v-slot:body-cell-name_en="props">
        <fdev-td class="text-ellipsis">
          <router-link
            :to="`/archetypeManage/server/archetype/${props.row.archetype_id}`"
            class="link"
            v-if="props.row.archetype_id"
          >
            <span :title="props.row.name_en">
              {{ props.row.name_en }}
            </span>
          </router-link>
          <span v-else :title="props.row.name_en">
            {{ props.row.name_en }}
          </span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.name_en }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
      </template>

      <!-- 骨架中文名 -->
      <template v-slot:body-cell-name_zh="props">
        <fdev-td class="text-ellipsis">
          <router-link
            :to="`/archetypeManage/server/archetype/${props.row.archetype_id}`"
            class="link"
            v-if="props.row.archetype_id"
          >
            <span :title="props.row.name_zh">
              {{ props.row.name_zh }}
            </span>
          </router-link>
          <span v-else :title="props.row.name_zh">
            {{ props.row.name_zh }}
          </span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.name_zh }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
      </template>

      <!-- 镜像版本类型 -->
      <template v-slot:body-cell-stage="props">
        <fdev-td class="text-ellipsis">
          <span :title="props.value ? imageTypeDict[props.value] : '-'">
            {{ props.value ? imageTypeDict[props.value] : '-' }}
          </span>
        </fdev-td>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import {
  imageTypeDict,
  imageManageProfileArchetypeUsingStatusColumns
} from '@/modules/Component/utils/constants.js';
import { mapState, mapActions, mapMutations } from 'vuex';
import {
  getImageArchetypeUsingStatus,
  setImageArchetypeUsingStatus
} from '@/modules/Component/utils/setting.js';

export default {
  name: 'ArchetypeUsingStatus',
  components: { Loading },
  data() {
    return {
      loading: false,
      tableData: [],
      pagination: getImageArchetypeUsingStatus(),
      filterValue: '',
      typeOptions: [
        { label: '全部', value: '0' },
        { label: '推荐版本', value: '1' }
      ],
      id: '',
      imageTypeDict: imageTypeDict,
      columns: imageManageProfileArchetypeUsingStatusColumns
    };
  },
  watch: {
    pagination(val) {
      setImageArchetypeUsingStatus({
        rowsPerPage: val.rowsPerPage
      });
    },
    selectValue(val) {
      this.filterValue = val.toString().toLowerCase();
      if (val.length === 0) {
        this.filterValue = ',';
      }
    },
    statusType(val) {
      this.filterValue += ',table';
    },
    '$route.params': {
      deep: true,
      handler(val) {
        this.component_id = val.id;
        this.init();
      }
    }
  },
  computed: {
    ...mapState('componentForm', ['frameByImage', 'baseImageDetail']),
    ...mapState(
      'userActionSaveComponent/imageManageProfile/imageManageProfileArche',
      ['selectValue', 'statusType', 'visibleColumns']
    )
  },
  methods: {
    ...mapMutations(
      'userActionSaveComponent/imageManageProfile/imageManageProfileArche',
      ['updateSelectValue', 'updateType', 'updatevisibleColumns']
    ),
    ...mapActions('componentForm', ['queryFrameByImage']),
    // 表格筛选，组件监听filterValue
    filter(rows, terms, cols, getCellValue) {
      const lowerTerms = terms.split(',').filter(item => {
        return item !== 'table' && item !== '';
      });
      return rows.filter(row => {
        let isExist = true;

        isExist = lowerTerms.every(item => {
          return cols.some(col => {
            return (
              (getCellValue(col, row) + '').toLowerCase().indexOf(item.trim()) >
              -1
            );
          });
        });
        return isExist;
      });
    },
    addSelect(val, done) {
      if (val.length > 0) {
        done(val);
      }
    }, // 点击搜索按钮
    setSelect(el) {
      if (el.inputValue.length) {
        el.add(el.inputValue);
        el.inputValue = '';
      }
    },
    async init(params) {
      this.loading = true;
      if (params) {
        this.updateType(params);
      }
      await this.queryFrameByImage({
        image_name: this.baseImageDetail.name || '',
        type: this.statusType === '1' ? this.statusType : ''
      });
      this.tableData = this.frameByImage;
      this.loading = false;
    }
  },
  async created() {
    this.id = this.$route.params.id;
    await this.init();
    this.filterValue = this.selectValue.toString().toLowerCase();
    if (this.selectValue.length === 0) {
      this.filterValue = ',';
    }
    this.filterValue += ',table';
  }
};
</script>
