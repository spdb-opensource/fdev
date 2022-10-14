<template>
  <Loading :visible="loading">
    <fdev-table
      class="my-sticky-column-table"
      title="骨架使用现状列表"
      titleIcon="list_s_f"
      :data="tableData"
      :columns="columns"
      :pagination.sync="pagination"
      :filter="filterValue"
      :filter-method="filter"
      ref="table"
      :visible-columns="visibleColumns"
      no-export
      no-select-cols
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
                class="cursor-pointer"
                @click="setSelect($refs.select)"
              />
            </template>
          </fdev-select>
        </f-formitem>

        <!-- 组件版本类型 -->
        <f-formitem label="骨架版本类型" class="q-pr-sm">
          <fdev-select
            :value="type"
            :options="typeOptions"
            map-options
            @input="updateTypeValue($event)"
            emit-value
          />
        </f-formitem>

        <!-- 组件版本类型 -->
        <f-formitem label="组件版本类型">
          <fdev-select
            :value="componentType"
            :options="typeOptions"
            map-options
            @input="updateComponentTypeValue($event)"
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
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.name_en }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
          <span v-else :title="props.row.name_en">{{ props.row.name_en }}</span>
        </fdev-td>
      </template>

      <!-- 骨架中文名列 -->
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
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.name_zh }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
          <span v-else :title="props.row.name_zh">{{ props.row.name_zh }}</span>
        </fdev-td>
      </template>

      <!-- 骨架版本类型列 -->
      <template v-slot:body-cell-type="props">
        <fdev-td class="text-ellipsis">
          <span
            :class="[
              { 'text-red': props.value === '2' },
              { 'text-yellow-9': props.value === '3' }
            ]"
            :title="props.value ? typeDict[props.value] : '-'"
          >
            {{ props.value ? typeDict[props.value] : '-' }}
          </span>
        </fdev-td>
      </template>
      <!-- 组件版本类型列 -->
      <template v-slot:body-cell-component_type="props">
        <fdev-td class="text-ellipsis">
          <span
            :class="[
              { 'text-red': props.value === '2' },
              { 'text-yellow-9': props.value === '3' }
            ]"
            :title="props.value ? typeDict[props.value] : '-'"
          >
            {{ props.value ? typeDict[props.value] : '-' }}
          </span>
        </fdev-td>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions, mapMutations } from 'vuex';
import {
  getArchetypeUsingStatus,
  setArchetypeUsingStatus
} from '@/modules/Component/utils/setting.js';
import {
  typeDict,
  serverComponentProfileArchetypeUsingStatusColums
} from '@/modules/Component/utils/constants.js';

export default {
  name: 'ArchetypeUsingStatus',
  components: { Loading },
  data() {
    return {
      loading: false,
      tableData: [],
      pagination: getArchetypeUsingStatus(),
      filterValue: '',
      typeOptions: [
        { label: '全部', value: '0' },
        { label: '推荐版本', value: '1' }
      ],
      component_id: '',
      typeDict: typeDict,
      columns: serverComponentProfileArchetypeUsingStatusColums
    };
  },
  watch: {
    pagination(val) {
      setArchetypeUsingStatus({
        rowsPerPage: val.rowsPerPage
      });
    },
    selectValue(val) {
      this.filterValue = val.toString().toLowerCase();
      if (val.length === 0) {
        this.filterValue = ',';
      }
    },
    type(val) {
      this.filterValue += ',table';
    },
    componentType(val) {
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
    ...mapState('componentForm', ['frameByComponent']),
    ...mapState(
      'userActionSaveComponent/componentManage/componentList/ArcheUsing',
      ['selectValue', 'visibleColumns', 'type', 'componentType']
    )
  },
  methods: {
    ...mapMutations(
      'userActionSaveComponent/componentManage/componentList/ArcheUsing',
      [
        'updateSelectValue',
        'updateType',
        'updateComponentType',
        'updatevisibleColumns'
      ]
    ),
    ...mapActions('componentForm', ['queryFrameByComponent', 'scanComponent']),
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
    updateTypeValue(params) {
      this.updateType(params);
      this.init();
    },
    updateComponentTypeValue(params) {
      this.updateComponentType(params);
      this.init();
    },
    async init() {
      this.loading = true;
      await this.queryFrameByComponent({
        component_id: this.component_id,
        type: this.type === '1' ? this.type : '',
        component_type: this.componentType === '1' ? this.componentType : ''
      });
      this.tableData = this.frameByComponent;
      this.loading = false;
    }
  },
  created() {
    this.component_id = this.$route.params.id;
    this.init();
    this.filterValue = this.selectValue.toString().toLowerCase();
    if (this.selectValue.length === 0) {
      this.filterValue = ',';
    }
  }
};
</script>
