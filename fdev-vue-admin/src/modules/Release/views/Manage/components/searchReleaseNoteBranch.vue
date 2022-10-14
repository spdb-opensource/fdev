<template>
  <f-dialog :value="value" right @input="$emit('input', $event)" :title="title">
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
      class="table-max-width"
      no-select-cols
    >
      <template v-slot:top-right>
        <fdev-btn
          v-if="showRefreshBtn"
          ficon="refresh_c_o"
          @click="init('1')"
          label="刷新"
          normal
        />
        <f-formitem label-style="width:84px" bottom-page label="名称">
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
      </template>
      <template v-slot:top-bottom>
        <span class="text-red-4" v-if="!showRefreshBtn"
          >注：数据库更新文件仅展示通过审核的sh文件和sql文件</span
        >
      </template>
    </fdev-table>
    <div class="row">
      <f-formitem class="col-6" label="责任人" required diaS>
        <fdev-select
          v-model="selectUser"
          use-input
          ref="dialogModel.dev_managers_info"
          :options="userOptions"
          option-label="user_name_cn"
          :rules="[val => !!val || '请选择责任人']"
          @filter="filterUser"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label>{{ scope.opt.user_name_en }}</fdev-item-label>
                <fdev-item-label caption>
                  {{ scope.opt.user_name_cn }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
    </div>
    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        label="确定"
        @click="handleUpdateTemplate"
        :loading="btnLoading"
        :disable="file.length === 0 || !selectUser"
      />
    </template>
  </f-dialog>
</template>

<script>
import { mapActions, mapState, mapGetters } from 'vuex';
import { required } from 'vuelidate/lib/validators';
export default {
  name: 'searchBranch',
  data() {
    return {
      userOptions: [],
      selectUser: '',
      loading: false,
      brancheSelected: '',
      selectValue: [], // 搜索框
      filterValue: '',
      file: [],
      columns: [{ name: 'value', label: '文件名', field: 'value' }],
      pagination: {
        rowsPerPage: 0
      }
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
    release_node_name: {
      type: String
    },
    note_id: {
      type: String
    },
    asset_catalog_name: {
      type: String
    },
    script_type: {
      type: String
    },
    seq_no: {},
    btnLoading: {}
  },
  computed: {
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
    ...mapState('releaseForm', {
      branches: 'branches',
      files: 'files',
      resourceProjects: 'resourceProjects',
      dbPath: 'dbPath'
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
    ...mapActions('user', ['fetch']),
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
        const data = {
          list: this.file.map((item, index) => {
            const res = {
              release_node_name: this.release_node_name,
              note_id: this.note_id,
              asset_catalog_name: this.asset_catalog_name,
              file_type: '1',
              file_url: item.value,
              script_type: this.script_type,
              file_principal: this.selectUser.user_name_cn,
              principal_phone: this.selectUser.telephone
            };
            if (this.script_type === '2') {
              res.seq_no = this.seq_no + index + 1 + '';
            } else {
              res.seq_no = '0';
            }
            return res;
          })
        };
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
    filterUser(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.userOptions = this.userList.filter(
          v =>
            v.user_name_cn.toLowerCase().indexOf(needle) > -1 ||
            v.user_name_en.toLowerCase().indexOf(needle) > -1
        );
      });
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
      this.file = [];
      this.selectUser = '';
      if (this.showBranch) {
        await this.queryResourceBranches({
          resource_giturl: this.branches.resource_url
        });
        if (this.branches.branchList.length !== 0) {
          this.brancheSelected = 'master';
          this.selectBranch(this.brancheSelected, clear_cache);
        }
      }
      await this.fetch();
      this.userOptions = this.userList;
    }
  },
  watch: {
    value(val) {
      if (val === true) {
        this.branches.resource_url = this.resourceProjects[0];
        this.init();
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

<style lang="stylus" scoped>
.table-max-width
  width 965px
  margin-bottom 20px
</style>
