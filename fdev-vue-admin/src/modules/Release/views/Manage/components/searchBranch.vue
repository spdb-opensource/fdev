<template>
  <f-dialog :value="value" right @input="$emit('input', $event)" :title="title">
    <span v-if="!showRefreshBtn"
      >注：数据库更新文件仅展示通过审核的sh文件和sql文件</span
    >
    <fdev-table
      titleIcon="list_s_f"
      title="文件列表"
      :data="filterList"
      :columns="columns"
      selection="multiple"
      row-key="index"
      :selected.sync="file"
      :filter="filterValue"
      :filter-method="filter"
      ref="table"
      hide-bottom
      :pagination.sync="pagination"
      noExport
      no-select-cols
    >
      <template v-slot:top-right>
        <fdev-btn
          v-if="showRefreshBtn"
          ficon="refresh_c_o"
          @click="init('1')"
          label="刷新"
          normal
          :loading="
            clickRefresh && globalLoading['releaseForm/queryResourceBranches']
          "
        />
      </template>
      <template v-slot:top-bottom>
        <f-formitem
          class="col-3 q-pr-md"
          label-style="width:50px"
          bottom-page
          label="名称"
        >
          <fdev-select
            v-model="selectValue"
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
        <f-formitem
          class="col-6 q-pr-md"
          label-style="width:100px"
          bottom-page
          label="配置文件地址"
        >
          <fdev-select
            input-debounce="0"
            emit-value
            map-options
            @input="init"
            :options="resourceProjects"
            v-model="branches.resource_url"
          />
        </f-formitem>
        <f-formitem
          class="col-3"
          label-style="width:50px"
          bottom-page
          label="分支名"
        >
          <fdev-select
            input-debounce="0"
            :options="branches.branchList"
            v-model="brancheSelected"
            emit-value
            @input="selectBranch($event)"
            map-options
            :option-value="opt => (opt === null ? '-Null-' : opt)"
            :option-label="opt => (opt === null ? '-Null-' : opt)"
          />
        </f-formitem>
      </template>
    </fdev-table>
    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        label="确定"
        @click="handleUpdateTemplate"
        :loading="btnLoading"
        :disable="file.length === 0"
      />
    </template>
  </f-dialog>
</template>

<script>
import { mapActions, mapState } from 'vuex';
import { required } from 'vuelidate/lib/validators';
export default {
  name: 'searchBranch',
  data() {
    return {
      loading: false,
      brancheSelected: '',
      selectValue: [], // 搜索框
      filterValue: '',
      file: [],
      columns: [{ name: 'value', label: '文件名', field: 'value' }],
      pagination: {
        rowsPerPage: 0
      },
      clickRefresh: false
    };
  },
  validations: {
    file: {
      required
    }
  },
  props: {
    // 展示文件配置地址
    showProfileAddress: {
      type: Boolean,
      default: true
    },
    // 展示“刷新”按钮
    showRefreshBtn: {
      type: Boolean,
      default: true
    },
    // 展示分支搜索项
    showBranch: {
      type: Boolean,
      default: true
    },
    title: {
      type: String
    },
    value: {
      type: Boolean
    },
    prod_id: {
      type: String
    },
    source_application: {
      type: String
    },
    btnLoading: {}
  },
  computed: {
    ...mapState('releaseForm', {
      branches: 'branches',
      files: 'files',
      resourceProjects: 'resourceProjects',
      dbPath: 'dbPath'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    filterList() {
      if (this.showProfileAddress) {
        return this.files.map((item, index) => {
          return { index: index, value: item };
        });
      } else {
        return this.dbPath.map((item, index) => {
          return { index: index, value: item };
        });
      }
    }
  },
  methods: {
    ...mapActions('releaseForm', {
      queryResourceBranches: 'queryResourceBranches', // 获取“配置文件更新”分支名
      queryResourceFiles: 'queryResourceFiles' // 获取“配置文件更新”文件名
    }),
    async handleUpdateTemplate() {
      if (this.showProfileAddress) {
        let selectedFiles = this.file.map(item => {
          return { file: item.value };
        });
        const data = {
          files: selectedFiles,
          prod_id: this.prod_id,
          branch: this.brancheSelected,
          source_application: this.source_application,
          resource_giturl: this.branches.resource_url
        };
        this.$emit('click', data);
      } else {
        // 获取数据库文件路径名
        const data = this.file.map(item => item.value);
        this.$emit('click', data);
      }
    },
    addSelect(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    // 点击搜索按钮
    setSelect(el) {
      if (el.inputValue.length) {
        el.add(el.inputValue);
        el.inputValue = '';
      }
    },
    filter(rows, terms, cols, getCellValue) {
      let lowerTerms = terms
        .toLowerCase()
        .split(',')
        .filter(item => {
          return item !== 'tabel' && item !== '';
        });
      this.filterData = rows.filter(row => {
        return lowerTerms.every(item => {
          if (item === '') {
            return true;
          }
          let col = cols.some(col => {
            return (
              (getCellValue(col, row) + '').toLowerCase().indexOf(item) > -1
            );
          });
          return col;
        });
      });
      return this.filterData;
    },
    async selectBranch(branch, clear_cache) {
      this.clickRefresh = false;
      this.loading = true;
      await this.queryResourceFiles({
        prod_id: this.prod_id,
        branch: this.brancheSelected,
        clear_cache: clear_cache,
        resource_giturl: this.branches.resource_url
      });
      this.loading = false;
    },
    async init(clear_cache) {
      clear_cache === '1'
        ? (this.clickRefresh = true)
        : (this.clickRefresh = false);
      this.file = [];
      if (this.showBranch) {
        await this.queryResourceBranches({
          resource_giturl: this.branches.resource_url
        });
        if (this.branches.branchList.length !== 0) {
          this.brancheSelected = 'master';
          this.selectBranch(this.brancheSelected, clear_cache);
        }
      }
    }
  },
  watch: {
    value(val) {
      if (val === true) {
        this.branches.resource_url = this.resourceProjects[0];
        this.init();
      } else {
        this.clickRefresh = false;
      }
    },
    selectValue(val) {
      this.filterValue = val.toString();
      if (val == '') {
        this.filterValue = ',';
      }
    }
  }
};
</script>

<style lang="stylus" scoped></style>
