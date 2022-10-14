<template>
  <f-block>
    <div class="row justify-between">
      <Loading :visible="loading">
        <fdev-table
          class="h-table"
          :data="currentCompanies"
          :columns="columns"
          row-key="name"
          binary-state-sort
          :pagination="{
            sortBy: 'count',
            descending: true,
            page: 1,
            rowsPerPage: 7
          }"
          titleIcon="list_s_f"
          title="公司"
          noExport
          no-select-cols
        >
          <template v-slot:top-right>
            <Authorized
              class="flex"
              :isUserModel="true"
              :trans-authority="['/api/company/add']"
            >
              <fdev-select
                :value="companyType"
                @input="updateCompanyType($event)"
                :options="companyTypeOptions"
                option-value="val"
                option-label="label"
                map-options
                emit-value
                options-dense
                class="q-pr-md"
              ></fdev-select>
              <fdev-btn
                ficon="add"
                label="新增"
                normal
                @click="() => handleAddDialogOpened('company')"
                v-if="companyType === '0'"
              ></fdev-btn>
            </Authorized>
          </template>
          <template v-slot:body="props">
            <fdev-tr :props="props">
              <fdev-td key="name" :props="props" :title="props.row.name">
                <Authorized
                  :isUserModel="true"
                  :trans-authority="['/api/company/update']"
                >
                  {{ props.row.name }}
                  <fdev-popup-edit
                    v-model="props.row.name"
                    buttons
                    v-if="companyType === '0'"
                    @save="
                      (value, initialValue) =>
                        handleUpdate(props.row, 'company', value, initialValue)
                    "
                    :validate="required"
                    @hide="required"
                    :color="
                      required(props.row.name) ? 'primary' : 'noop disabled'
                    "
                  >
                    <fdev-input
                      autofocus
                      type="text"
                      v-model="props.row.name"
                      color="primary"
                    ></fdev-input>
                  </fdev-popup-edit>
                  <template v-slot:exception>
                    {{ props.row.name }}
                  </template>
                </Authorized>
              </fdev-td>
              <fdev-td key="count" :props="props" :title="props.row.count">{{
                props.row.count
              }}</fdev-td>
              <fdev-td key="operation" :props="props">
                <Authorized
                  :isUserModel="true"
                  :trans-authority="['/api/company/delete']"
                >
                  <fdev-btn
                    flat
                    color="red"
                    ficon="delete"
                    :disable="companyType === '1'"
                    @click="() => handleRemove(props.row, 'company')"
                  ></fdev-btn>
                  <template v-slot:exception>
                    <fdev-btn flat color="red" ficon="delete" disabled />
                  </template>
                </Authorized>
              </fdev-td>
            </fdev-tr>
          </template>
        </fdev-table>
      </Loading>
      <Loading :visible="loading">
        <fdev-table
          class="h-table"
          :data="currentRoles"
          :columns="columns"
          row-key="name"
          :pagination="{
            sortBy: 'count',
            descending: true,
            page: 1,
            rowsPerPage: 7
          }"
          titleIcon="member_s_f"
          title="角色"
          noExport
          no-select-cols
        >
          <template v-slot:top-right>
            <Authorized
              class="flex"
              :isUserModel="true"
              :trans-authority="['/api/role/add']"
            >
              <fdev-select
                :value="roleType"
                @input="updateRoleType($event)"
                :options="roleTypeOptions"
                option-value="val"
                option-label="label"
                map-options
                emit-value
                options-dense
                class="q-pr-md"
              ></fdev-select>
              <fdev-btn
                ficon="add"
                label="新增"
                normal
                @click="() => handleAddDialogOpened('role')"
                v-if="roleType === '0'"
              ></fdev-btn>
            </Authorized>
          </template>
          <template v-slot:body="props">
            <fdev-tr :props="props">
              <fdev-td key="name" :props="props" :title="props.row.name">
                <Authorized
                  :isUserModel="true"
                  :trans-authority="['/api/role/update']"
                >
                  {{ props.row.name }}
                  <fdev-popup-edit
                    v-model="props.row.name"
                    buttons
                    v-if="roleType === '0'"
                    @save="
                      (value, initialValue) =>
                        handleUpdate(props.row, 'role', value, initialValue)
                    "
                    :validate="required"
                    :color="
                      required(props.row.name) ? 'primary' : 'noop disabled'
                    "
                  >
                    <fdev-input
                      autofocus
                      type="text"
                      v-model="props.row.name"
                    />
                  </fdev-popup-edit>
                  <template v-slot:exception>
                    {{ props.row.name }}
                  </template>
                </Authorized>
              </fdev-td>
              <fdev-td key="count" :props="props" :title="props.row.count">{{
                props.row.count
              }}</fdev-td>
              <fdev-td key="operation" :props="props">
                <Authorized
                  :isUserModel="true"
                  :trans-authority="['/api/role/delete']"
                >
                  <fdev-btn
                    flat
                    color="red"
                    ficon="delete"
                    :disable="roleType === '1'"
                    @click="() => handleRemove(props.row, 'role')"
                  />
                  <template v-slot:exception>
                    <fdev-btn flat color="red" ficon="delete" disabled />
                  </template>
                </Authorized>
              </fdev-td>
            </fdev-tr>
          </template>
        </fdev-table>
      </Loading>
    </div>
    <f-dialog v-model="addDialogOpened" :title="`新增${openedType.label}`">
      <div>
        <fdev-input
          autofocus
          type="text"
          v-model="$v.addModel.$model"
          ref="addModel"
          @keyup.enter="handleAdd"
        />
      </div>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="addDialogOpened = false"/>
        <fdev-btn
          dialog
          label="确定"
          @click="handleAdd"
          :disable="$v.addModel.$invalid"
      /></template>
    </f-dialog>
  </f-block>
</template>

<script>
import { mapState, mapActions, mapMutations } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import Loading from '@/components/Loading';
import Authorized from '@/components/Authorized';

const entityType = {
  company: {
    label: '公司',
    remove: 'removeCompany',
    add: 'addCompany',
    update: 'updateCompany'
  },
  role: {
    label: '角色',
    remove: 'removeRole',
    add: 'addRole',
    update: 'updateRole'
  }
};

export default {
  name: 'Other',
  components: { Authorized, Loading },
  data() {
    return {
      columns: [
        {
          name: 'name',
          label: '名字',
          align: 'left',
          field: 'name',
          style: 'width: 33.3333%'
        },
        {
          name: 'count',
          label: '人数',
          align: 'center',
          field: 'count',
          sortable: true,
          style: 'width: 33.3333%'
        },
        {
          name: 'operation',
          label: '操作',
          align: 'right',
          style: 'width: 33.3333%'
        }
      ],
      loading: true,
      addModel: '',
      openedType: '',
      addDialogOpened: false,
      companyTypeOptions: [
        { label: '使用', val: '0' },
        { label: '废弃', val: '1' }
      ],
      roleTypeOptions: [
        { label: '使用', val: '0' },
        { label: '废弃', val: '1' }
      ],
      currentCompanies: [],
      currentRoles: []
    };
  },
  validations: {
    addModel: {
      required
    }
  },
  watch: {
    companies() {
      this.currentCompanies = this.companies;
    },
    companyType(val) {
      this.currentCompanies =
        val === '0' ? this.companies : this.abandonCompanies;
    },
    roles() {
      this.currentRoles = this.roles;
    },
    roleType(val) {
      this.currentRoles = val === '0' ? this.roles : this.abandonRoles;
    }
  },
  computed: {
    ...mapState('userActionSaveUser/dimensionQuery/companiesAndRoles', [
      'companyType',
      'roleType'
    ]),
    ...mapState('userForm', [
      'companies',
      'abandonCompanies',
      'roles',
      'abandonRoles'
    ])
  },
  methods: {
    ...mapMutations('userActionSaveUser/dimensionQuery/companiesAndRoles', [
      'updateCompanyType',
      'updateRoleType'
    ]),
    ...mapActions('userForm', [
      'fetchCompany',
      'fetchRole',
      'removeCompany',
      'removeRole',
      'addCompany',
      'addRole',
      'updateCompany',
      'updateRole'
    ]),
    async handleUpdate(row, entity, value, initialValue) {
      let type = entityType[entity];
      try {
        await this[type.update](row);
      } catch (e) {
        row.name = initialValue;
      }
    },
    async handleRemove(row, entity) {
      let type = entityType[entity];
      if (row.count !== 0) {
        return this.$q
          .dialog({
            title: `删除${type.label}`,
            message: `有 ${row.count} 个人属于这个${type.label}, 确认删除么？`,
            ok: '删除',
            cancel: '再想想'
          })
          .onOk(async () => {
            await this[type.remove](row);
          });
      } else {
        return this.$q
          .dialog({
            title: `删除${type.label}`,
            message: `确认删除该${type.label}么？`,
            ok: '删除',
            cancel: '再想想'
          })
          .onOk(async () => {
            await this[type.remove](row);
          });
      }
    },
    handleAddDialogOpened(entity) {
      this.openedType = entityType[entity];
      this.addModel = '';
      this.$v.addModel.$reset();

      this.addDialogOpened = true;
    },
    async handleAdd() {
      if (this.$v.addModel.$invalid) {
        return;
      }
      await this[this.openedType.add]({
        name: this.addModel
      });
      this.addDialogOpened = false;
    },
    required(val) {
      return !!val;
    }
  },
  async created() {
    await this.fetchCompany();
    this.currentCompanies = this.companies;
    await this.fetchRole();
    this.currentRoles = this.roles;
    this.loading = false;
  },
  mounted() {}
};
</script>

<style lang="stylus" scoped>
.h-table{
 max-height: 38vw;
 width:32vw
}
</style>
