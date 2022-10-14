<template>
  <f-dialog
    :value="value"
    right
    @input="$emit('input', $event)"
    title="添加gitlab配置文件"
  >
    <fdev-table
      titleIcon="list_s_f"
      :data="files"
      title="文件列表"
      :columns="columns"
      row-key="__index"
      :filter="filterValue"
      :filter-method="filter"
      ref="table"
      hide-bottom
      :pagination.sync="pagination"
      no-select-cols
      no-export
    >
      <template v-slot:top-right>
        <fdev-toggle v-model="toggleFile" />
        <fdev-btn
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
        <div class="column" style="width:1200px">
          <div class="row">
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
                    class="text-primary cursor-pointer"
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
          </div>
          <div
            class="q-mt-md"
            v-if="catalogType === '5' && catalogName === 'commonconfig'"
          >
            <f-formitem label="部署平台" class="col-3" label-style="width:75px">
              <div class="row items-center">
                <fdev-radio
                  v-model="commonconfigPlatform"
                  class="q-mr-lg"
                  val="CAAS"
                  label="CAAS"
                />
                <fdev-radio
                  v-model="commonconfigPlatform"
                  val="SCC"
                  label="SCC"
                />
              </div>
            </f-formitem>
          </div>
        </div>
      </template>

      <template #header="props">
        <fdev-tr :props="props">
          <template v-for="col in props.cols">
            <fdev-th :key="col.name" v-if="col.label" :props="props">
              {{ col.label }}
            </fdev-th>
          </template>
        </fdev-tr>
      </template>

      <template v-slot:body="props">
        <fdev-tr :props="props">
          <fdev-td
            class="text-ellipsis"
            key="value"
            :props="props"
            :class="{ max: toggleFile }"
            :title="props.row.value"
          >
            {{ props.row.value }}
          </fdev-td>
          <fdev-td
            class="text-ellipsis"
            v-for="(box, index) in runtime_env"
            :key="`${index}`"
            :props="props"
          >
            <input
              :disable="isSelect(props.row.value, box)"
              type="checkbox"
              class="test"
              :value="box"
              v-model="props.row.runtime_env"
            />
          </fdev-td>
        </fdev-tr>
      </template>
    </fdev-table>
    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        label="确定"
        @click="handleUpdateTemplate"
        :loading="btnLoading"
      />
    </template>

    <f-dialog right v-model="toggleFile" title="已选文件">
      <div class="col" style="width:600px">
        <p class="selected">已选文件：</p>
        <div
          class="row"
          :class="{ error: item.length !== runtime_env.length }"
          v-for="(item, key) in addFiles"
          :key="key"
        >
          <p class="col-4 overflow ellipsis" :title="key">
            {{ key }}
          </p>
          <div class="col-8" style="text-align: right">
            <fdev-badge
              class="q-mr-xs"
              v-for="(badge, index) in item"
              :key="index"
            >
              {{ badge.runtime_env }}
            </fdev-badge>
          </div>
        </div>
        <p class="error-text" v-show="message.length !== 0">
          <f-icon name="alert_t_f" />
          <span>
            请确认所选文件已覆盖所有环境。
          </span>
        </p>
      </div>
    </f-dialog>
  </f-dialog>
</template>

<script>
import { mapActions, mapState } from 'vuex';
export default {
  name: 'selectForRuntime',
  data() {
    return {
      loading: false,
      brancheSelected: '',
      selectValue: [], // 搜索框
      filterValue: '',
      toggleFile: false,
      pagination: {
        rowsPerPage: 0
      },
      flat: true,
      clickRefresh: false,
      commonconfigPlatform: 'CAAS'
      // runtime_env: ['DEV', 'TEST', 'PROCSH', 'PROCHF']
    };
  },
  props: {
    value: {
      type: Boolean
    },
    prod_id: {
      type: String
    },
    source_application: {
      type: String
    },
    btnLoading: {},
    prodType: {
      type: String
    },
    catalogType: {
      type: String
    },
    catalogName: {
      type: String
    }
  },
  computed: {
    ...mapState('releaseForm', {
      branches: 'branches',
      files: 'filesWithRuntime',
      resourceProjects: 'resourceProjects'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    runtime_env() {
      let result;
      if (this.catalogType === '9') {
        if (this.prodType === 'gray') {
          result = ['DEV', 'TEST', 'shanghaiHd', 'hefeiHd'];
        } else {
          result = [
            'DEV',
            'TEST',
            'shanghaiK1',
            'shanghaiK2',
            'hefeiK1',
            'hefeiK2'
          ];
        }
      } else if (
        this.catalogType === '5' &&
        this.catalogName === 'commonconfig'
      ) {
        if (this.commonconfigPlatform === 'CAAS') {
          result = ['DEV', 'TEST', 'PROCSH', 'PROCHF'];
        } else if (this.commonconfigPlatform === 'SCC') {
          if (this.prodType === 'gray') {
            result = ['DEVSCC', 'TESTSCC', 'PROCSHSCCHD', 'PROCHFSCCHD'];
          } else {
            result = [
              'DEVSCC',
              'TESTSCC',
              'PROCSHSCCK1',
              'PROCSHSCCK2',
              'PROCHFSCCK1',
              'PROCHFSCCK2'
            ];
          }
        }
      } else {
        result = ['DEV', 'TEST', 'PROCSH', 'PROCHF'];
      }
      return result;
    },
    columns() {
      const items = this.runtime_env.map((item, index) => ({
        name: index + '',
        label: item
      }));
      if (6 - items.length > 0) {
        for (let i = items.length; i < 6; i++) {
          items.push({ name: `${i}` });
        }
      }
      return [{ name: 'value', label: '文件名', field: 'value' }, ...items];
    },
    addFiles() {
      let uploadFiles = {};
      this.files.forEach(item => {
        if (item.runtime_env.length > 0) {
          const itemArr = item.value.split('/');
          const key = itemArr[itemArr.length - 1];
          if (!uploadFiles[key]) {
            uploadFiles[key] = [];
          }
          const runtimeArr = item.runtime_env.map(run => {
            return { runtime_env: run, file: item.value };
          });
          uploadFiles[key] = uploadFiles[key].concat(runtimeArr);
        }
      });
      return uploadFiles;
    },
    message() {
      let arr = [];
      const keys = Object.keys(this.addFiles);
      keys.forEach(key => {
        if (this.addFiles[key].length !== this.runtime_env.length) {
          arr.push(key);
        }
      });
      return arr;
    }
  },
  methods: {
    ...mapActions('releaseForm', {
      queryResourceBranches: 'queryResourceBranches',
      queryResourceFiles: 'queryResourceFiles'
    }),
    async handleUpdateTemplate() {
      let arr = [];
      const keys = Object.keys(this.addFiles);
      keys.forEach(key => {
        arr = arr.concat(this.addFiles[key]);
      });
      const data = {
        files: arr,
        prod_id: this.prod_id,
        branch: this.brancheSelected,
        source_application: this.source_application,
        resource_giturl: this.branches.resource_url
      };
      if (this.message.length !== 0 && this.catalogType !== '10') {
        this.toggleFile = true;
        return '';
      }
      this.$emit('click', data);
    },
    addSelect(val, done) {
      if (val.length > 0) {
        done(val);
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
    selection(detail) {
      detail.rows.forEach(item => {
        if (detail.added === false) {
          item.runtime_env = [];
        }
        item.show = detail.added;
      });
    },
    // 点击搜索按钮
    setSelect(el) {
      if (el.inputValue.length) {
        el.add(el.inputValue);
        el.inputValue = '';
      }
    },
    isSelect(file, run) {
      if (this.flat === true) {
        return;
      }
      const nameArr = file.split('/');
      const name = nameArr[nameArr.length - 1];
      if (this.addFiles[name]) {
        const disable = this.addFiles[name].find(item => {
          return item.runtime_env === run && item.file !== file;
        });
        return Boolean(disable);
      }
      return false;
    },
    async init(clear_cache) {
      clear_cache === '1'
        ? (this.clickRefresh = true)
        : (this.clickRefresh = false);
      this.flat = true;
      await this.queryResourceBranches({
        resource_giturl: this.branches.resource_url
      });
      this.flat = false;
      if (this.branches.branchList.length !== 0) {
        this.brancheSelected = 'master';
        this.selectBranch(this.brancheSelected, clear_cache);
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
        this.commonconfigPlatform = 'CAAS';
        this.files.forEach(item => {
          item.runtime_env = [];
        });
      }
    },
    selectValue(val) {
      this.filterValue = val.toString();
      if (val == '') {
        this.filterValue = ',';
      }
    },
    runtime_env() {
      this.files.forEach(item => {
        item.runtime_env = [];
      });
    }
  }
};
</script>

<style lang="stylus" scoped>
.error
  color red
.selected
  font-size 18px
  font-weight 700
.error-text
  height 30px
  background #fef0f0
  color #f56c6c
  display inline-block
  line-height 30px
  padding 0 10px
  border 1px solid #fde2e2
  border-radius 5px
  .icon
    font-size 18px
    vertical-align: text-bottom;
</style>
