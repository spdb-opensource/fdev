<template>
  <f-block>
    <Loading :visible="loading" class="bg-white">
      <fdev-table
        :data="archetypeList"
        :columns="columns"
        row-key="archetype_id"
        :pagination.sync="pagination"
        :filter="filterValue"
        :filter-method="filter"
        ref="table"
        title="后端骨架列表"
        titleIcon="list_s_f"
        :visible-columns="visibleColumns"
        class="my-sticky-column-table"
        :onSelectCols="updatevisibleColumns"
        :export-func="handleDownloadAll"
      >
        <template v-slot:top-right>
          <Authorized>
            <fdev-btn
              label="骨架录入"
              @click="handleArchetypeDialogOpened('addArchetype')"
              normal
              ficon="add"
              v-if="isManger(null)"
            />
          </Authorized>
        </template>
        <template v-slot:top-bottom>
          <f-formitem label="搜索条件">
            <fdev-select
              :value="selectValue"
              @input="updateSelectValue($event)"
              multiple
              use-input
              hide-dropdown-icon
              ref="select"
              @new-value="addSelect"
              placeholder="输入关键字，回车区分"
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
        </template>
        <template v-slot:body-cell-name_en="props">
          <fdev-td :title="props.row.name_en" class="text-ellipsis">
            <router-link
              :to="{
                path: `/archetypeManage/server/archetype/${props.row.id}`
              }"
              class="link"
            >
              <span>{{ props.row.name_en }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.name_en }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>

        <template v-slot:body-cell-name_cn="props">
          <fdev-td :title="props.row.name_cn" class="text-ellipsis">
            <router-link
              :to="{
                path: `/archetypeManage/server/archetype/${props.row.id}`
              }"
              class="link"
            >
              <span>{{ props.row.name_cn }}</span>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.name_cn }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </fdev-td>
        </template>

        <template v-slot:body-cell-manager_id="props">
          <fdev-td
            :title="props.value.map(v => v.user_name_cn).join(',')"
            class="text-ellipsis"
          >
            <span v-for="user in props.row.manager_id" :key="user.id">
              <router-link
                :to="`/user/list/${user.id}`"
                class="link"
                v-if="user.id"
              >
                <span>{{ user.user_name_cn }}</span>
              </router-link>
              <span v-else>{{ user.user_name_cn }}</span>
              &nbsp;
            </span>
          </fdev-td>
        </template>

        <template v-slot:body-cell-type="props">
          <fdev-td :title="props.value" class="text-ellipsis">
            <span>{{ props.value ? props.value : '-' }}</span>
          </fdev-td>
        </template>

        <template v-slot:body-cell-btn="props">
          <fdev-td>
            <div v-if="isManger(props.row.manager_id)">
              <fdev-btn
                label="信息维护"
                flat
                @click="
                  handleArchetypeDialogOpened('updateArchetype', props.row)
                "
                v-if="isManger"
              />
            </div>
          </fdev-td>
        </template>

        <template v-slot:body-cell-wiki_url="props">
          <fdev-td :title="props.row.wiki_url" class="text-ellipsis">
            <div class="td-width">
              <a
                :href="props.row.wiki_url"
                target="_blank"
                class="text-primary"
              >
                {{ props.row.wiki_url }}
              </a>
            </div>
          </fdev-td>
        </template>
      </fdev-table>
    </Loading>
    <ArchetypeDialog
      :method="method"
      :data="selectedData"
      v-model="archetypeDialogOpened"
      @click="init"
    />
  </f-block>
</template>
<script>
import Loading from '@/components/Loading';
import Authorized from '@/components/Authorized';
import { mapActions, mapState, mapMutations } from 'vuex';
import {
  getPagination,
  setPagination
} from '@/modules/Component/utils/setting.js';
import ArchetypeDialog from '@/modules/Component/views/serverArchetype/components/addAndUpdate';
import { serverArchetypeColums } from '@/modules/Component/utils/constants.js';
import { handleDownload } from '@/modules/Component/utils/utils.js';

export default {
  name: 'ComponentArchetype',
  components: { Loading, Authorized, ArchetypeDialog },
  data() {
    return {
      filterValue: '',
      pagination: getPagination(),
      loading: false,
      selectedData: {},
      method: 'method',
      archetypeDialogOpened: false
    };
  },
  computed: {
    ...mapState('userActionSaveComponent/archetypeManage/archetype', [
      'selectValue',
      'visibleColumns'
    ]),
    ...mapState('componentForm', ['archetypeList']),
    ...mapState('userForm', ['groups']),
    ...mapState('user', ['currentUser']),
    visibleOptions() {
      const arr = this.columns.slice(0);
      return arr.splice(0, arr.length - 1);
    },
    columns() {
      return serverArchetypeColums(this.groups);
    }
  },
  watch: {
    pagination(val) {
      setPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    selectValue(val) {
      this.filterValue = val.toString();
    }
  },
  methods: {
    ...mapMutations('userActionSaveComponent/archetypeManage/archetype', [
      'updateSelectValue',
      'updatevisibleColumns'
    ]),
    ...mapActions('componentForm', ['queryArchetypes']),
    ...mapActions('userForm', ['fetchGroup']),
    async init() {
      this.loading = true;
      await this.queryArchetypes();
      this.loading = false;
    },
    isManger(managerList) {
      const haveRole = this.currentUser.role.some(
        v => v.label === '基础架构管理员'
      );
      const isManger = managerList
        ? managerList.some(user => user.id === this.currentUser.id)
        : false;
      return isManger || haveRole;
    },
    async handleArchetypeDialogOpened(method, data) {
      this.method = method;
      this.archetypeDialogOpened = true;
      if (method === 'updateArchetype') {
        this.selectedData = {
          ...data,
          groupObj: data.group
            ? this.groups.find(group => group.id === data.group) || ''
            : {}
        };
      }
    },
    addSelect(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    setSelect(el) {
      if (el.inputValue.length) {
        el.add(el.inputValue);
        el.inputValue = '';
      }
    },
    filter(rows, terms, cols, getCellValue) {
      let lowerTerms = this.selectValue.map(item => item.toLowerCase());
      return rows.filter(row => {
        return lowerTerms.every(item => {
          return cols.some(col => {
            if (Array.isArray(getCellValue(col, row))) {
              return getCellValue(col, row).some(v => {
                return v.user_name_cn.toLowerCase().indexOf(item) > -1;
              });
            } else {
              return (
                (getCellValue(col, row) + '')
                  .toLowerCase()
                  .indexOf(item.trim()) > -1
              );
            }
          });
        });
      });
    },
    handleDownloadAll() {
      const dictObj = {
        sonar_scan_switch: [
          { label: '关', value: '0' },
          { label: '开', value: '1' }
        ],
        isTest: [{ label: '否', value: '0' }, { label: '是', value: '1' }]
      };
      handleDownload(
        this.columns,
        this.archetypeList,
        this.groups,
        dictObj,
        'manager_id',
        '后端骨架列表'
      );
    }
  },
  async created() {
    await this.fetchGroup();
    this.init();
    this.filterValue = this.selectValue.toString();
  }
};
</script>
<style lang="stylus" scoped>
.td-width
  max-width 300px
  overflow hidden
  text-overflow ellipsis
.w150
  width 150px
</style>
