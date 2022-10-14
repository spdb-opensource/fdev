<template>
  <div class="bg-white">
    <p class="text-h6 text-left">CaaS部署信息</p>
    <fdev-list class="rounded-borders">
      <fdev-expansion-item
        v-for="(item, key, i) in tableData"
        :key="key"
        :default-opened="i === 0"
      >
        <template v-slot:header>
          <fdev-item-section>
            <div class="section text-left">
              {{ key }}
            </div>
          </fdev-item-section>
        </template>
        <fdev-timeline
          color="secondary"
          class="q-pb-md q-px-md bg-white text-left"
        >
          <fdev-timeline-entry v-for="(table, index) in item" :key="index">
            <template v-slot:title>
              <span class="timeline-title">{{ table.name_cn }}</span>
            </template>
            <fdev-table
              class="my-sticky-column-table"
              :columns="columns"
              :data="table.env_key"
              row-key="id"
              :pagination.sync="pagination"
              no-select-cols
              noExport
              hide-bottom
            >
              <template v-slot:body="props">
                <fdev-tr :props="props">
                  <fdev-td
                    class="td-width text-ellipsis"
                    key="name_en"
                    :title="props.row.name_en || '-'"
                  >
                    {{ props.row.name_en || '-' }}
                  </fdev-td>

                  <fdev-td
                    class="td-width text-ellipsis"
                    key="name_cn"
                    :title="props.row.name_cn || '-'"
                  >
                    {{ props.row.name_cn || '-' }}
                  </fdev-td>

                  <fdev-td class="td-width text-ellipsis" key="value">
                    <span
                      class="text-red"
                      v-if="!props.row.value || props.row.value.length === 0"
                      >#error#</span
                    >
                    <!-- 有json_schema字段，则表示是高级属性 -->
                    <div
                      class="chip-wrapper"
                      v-if="
                        props.row.json_schema && Array.isArray(props.row.value)
                      "
                    >
                      <chip
                        v-for="(item, index) in props.row.value"
                        :key="item.id"
                        :data="item"
                        :name="props.row.name_en + index"
                      />
                    </div>

                    <div
                      v-else-if="
                        props.row.value &&
                          props.row.json_schema &&
                          !Array.isArray(props.row.value)
                      "
                    >
                      <chip :data="props.row.value" :name="props.row.name_en" />
                    </div>
                    <span v-else>
                      <span
                        v-if="props.row.value && !props.row.json_schema"
                        :title="props.row.value"
                        >{{ props.row.value }}</span
                      >
                      <span v-else>{{ props.row.value }}</span>
                    </span>
                  </fdev-td>

                  <fdev-td
                    :class="{
                      'td-width row no-wrap items-center': !props.row
                        .json_schema
                    }"
                    key="own_value"
                  >
                    <div v-if="props.row.json_schema">
                      <div
                        v-if="props.row.json_schema.jsonSchema.type === 'array'"
                        class="chip-wrapper"
                      >
                        <div
                          v-for="(item, i) in props.row.own_value"
                          :key="i"
                          class="inline-block"
                        >
                          <chip
                            :name="props.row.name_en + i"
                            :data="item"
                            v-if="item"
                            color="orange"
                          />
                        </div>
                        <span
                          v-if="
                            !props.row.own_value ||
                              props.row.own_value.length === 0
                          "
                          >-</span
                        >
                        <fdev-icon
                          size="12px"
                          name="edit"
                          v-ripple
                          color="primary"
                          class="icon cursor-pointer"
                          v-if="props.row.type === '1'"
                          @click.stop="
                            handleAppSelectedDialogOpen(
                              props.row,
                              index,
                              key,
                              'tableData'
                            )
                          "
                        />
                      </div>
                      <div v-else>
                        <chip
                          :name="props.row.name_en"
                          :data="props.row.own_value"
                          color="orange"
                          removable
                          v-if="props.row.own_value"
                          @remove="remove(props.row)"
                        />
                        <span v-else>-</span>
                        <fdev-icon
                          size="12px"
                          name="edit"
                          v-ripple
                          color="primary"
                          class="icon cursor-pointer"
                          v-if="props.row.type === '1'"
                          @click.stop="
                            handleObjEditDialogOpened(
                              props.row,
                              index,
                              key,
                              table,
                              'tableData'
                            )
                          "
                        />
                      </div>
                    </div>

                    <span v-else class="table-edit text-ellipsis">
                      <span
                        v-if="props.row.own_value && !props.row.json_schema"
                        :title="props.row.own_value"
                        >{{ props.row.own_value || '-' }}</span
                      >
                      <span v-else>{{ props.row.own_value || '-' }}</span>
                    </span>
                    <!-- type === '1'时可编辑 -->
                    <fdev-icon
                      size="12px"
                      name="edit"
                      v-ripple
                      color="primary"
                      class="cursor-pointer string-icon"
                      @click.stop
                      v-if="props.row.type === '1' && !props.row.json_schema"
                    >
                      <fdev-popup-edit v-model="props.row.own_value" buttons>
                        <fdev-input
                          v-model="props.row.own_value"
                          dense
                          autofocus
                        />
                      </fdev-popup-edit>
                    </fdev-icon>
                  </fdev-td>
                </fdev-tr>
              </template>
            </fdev-table>
          </fdev-timeline-entry>
        </fdev-timeline>
      </fdev-expansion-item>
    </fdev-list>
    <fdev-separator class="q-my-lg" />
    <p class="text-h6 text-left">SCC部署信息</p>
    <fdev-list class="rounded-borders">
      <fdev-expansion-item
        v-for="(item, key, i) in sccTableData"
        :key="key"
        :default-opened="i === 0"
      >
        <template v-slot:header>
          <fdev-item-section>
            <div class="section text-left">
              {{ key }}
            </div>
          </fdev-item-section>
        </template>
        <fdev-timeline
          color="secondary"
          class="q-pb-md q-px-md bg-white text-left"
        >
          <fdev-timeline-entry v-for="(table, index) in item" :key="index">
            <template v-slot:title>
              <span class="timeline-title">{{ table.name_cn }}</span>
            </template>
            <fdev-table
              class="my-sticky-column-table"
              :columns="columns"
              :data="table.env_key"
              row-key="id"
              :pagination.sync="pagination"
              no-select-cols
              noExport
              hide-bottom
            >
              <template v-slot:body="props">
                <fdev-tr :props="props">
                  <fdev-td
                    class="td-width text-ellipsis"
                    key="name_en"
                    :title="props.row.name_en || '-'"
                  >
                    {{ props.row.name_en || '-' }}
                  </fdev-td>

                  <fdev-td
                    class="td-width text-ellipsis"
                    key="name_cn"
                    :title="props.row.name_cn || '-'"
                  >
                    {{ props.row.name_cn || '-' }}
                  </fdev-td>

                  <fdev-td class="td-width text-ellipsis" key="value">
                    <span
                      class="text-red"
                      v-if="!props.row.value || props.row.value.length === 0"
                      >#error#</span
                    >
                    <!-- 有json_schema字段，则表示是高级属性 -->
                    <div
                      class="chip-wrapper"
                      v-if="
                        props.row.json_schema && Array.isArray(props.row.value)
                      "
                    >
                      <chip
                        v-for="(item, index) in props.row.value"
                        :key="item.id"
                        :data="item"
                        :name="props.row.name_en + index"
                      />
                    </div>

                    <div
                      v-else-if="
                        props.row.value &&
                          props.row.json_schema &&
                          !Array.isArray(props.row.value)
                      "
                    >
                      <chip :data="props.row.value" :name="props.row.name_en" />
                    </div>
                    <span v-else>
                      <span
                        v-if="props.row.value && !props.row.json_schema"
                        :title="props.row.value"
                        >{{ props.row.value }}</span
                      >
                      <span v-else>{{ props.row.value }}</span>
                    </span>
                  </fdev-td>

                  <fdev-td
                    :class="{
                      'td-width row no-wrap items-center': !props.row
                        .json_schema
                    }"
                    key="own_value"
                  >
                    <div v-if="props.row.json_schema">
                      <div
                        v-if="props.row.json_schema.jsonSchema.type === 'array'"
                        class="chip-wrapper"
                      >
                        <div
                          v-for="(item, i) in props.row.own_value"
                          :key="i"
                          class="inline-block"
                        >
                          <chip
                            :name="props.row.name_en + i"
                            :data="item"
                            v-if="item"
                            color="orange"
                          />
                        </div>
                        <span
                          v-if="
                            !props.row.own_value ||
                              props.row.own_value.length === 0
                          "
                          >-</span
                        >
                        <fdev-icon
                          size="12px"
                          name="edit"
                          v-ripple
                          color="primary"
                          class="icon cursor-pointer"
                          v-if="props.row.type === '1'"
                          @click.stop="
                            handleAppSelectedDialogOpen(
                              props.row,
                              index,
                              key,
                              'sccTableData'
                            )
                          "
                        />
                      </div>
                      <div v-else>
                        <chip
                          :name="props.row.name_en"
                          :data="props.row.own_value"
                          color="orange"
                          removable
                          v-if="props.row.own_value"
                          @remove="remove(props.row)"
                        />
                        <span v-else>-</span>
                        <fdev-icon
                          size="12px"
                          name="edit"
                          v-ripple
                          color="primary"
                          class="icon cursor-pointer"
                          v-if="props.row.type === '1'"
                          @click.stop="
                            handleObjEditDialogOpened(
                              props.row,
                              index,
                              key,
                              table,
                              'sccTableData'
                            )
                          "
                        />
                      </div>
                    </div>
                    <span v-else class="table-edit text-ellipsis">
                      <span
                        v-if="props.row.own_value && !props.row.json_schema"
                        :title="props.row.own_value"
                        >{{ props.row.own_value || '-' }}</span
                      >
                      <span v-else>{{ props.row.own_value || '-' }}</span>
                    </span>
                    <!-- type === '1'时可编辑 -->
                    <fdev-icon
                      size="12px"
                      name="edit"
                      v-ripple
                      color="primary"
                      class="cursor-pointer string-icon"
                      @click.stop
                      v-if="props.row.type === '1' && !props.row.json_schema"
                    >
                      <fdev-popup-edit v-model="props.row.own_value" buttons>
                        <fdev-input
                          v-model="props.row.own_value"
                          dense
                          autofocus
                        />
                      </fdev-popup-edit>
                    </fdev-icon>
                  </fdev-td>
                </fdev-tr>
              </template>
            </fdev-table>
          </fdev-timeline-entry>
        </fdev-timeline>
      </fdev-expansion-item>
    </fdev-list>
    <fdev-btn
      label="上一步"
      dialog
      class="q-my-md q-mr-md"
      @click="$emit('prev')"
    />
    <fdev-btn
      label="下一步"
      dialog
      class="q-my-md q-mr-md"
      @click="handleStep3"
      :loading="loading"
    />
    <f-dialog
      v-model="appSelectedDialogOpened"
      right
      f-sc
      :title="`编辑-${dialogTitle}-应用独有`"
    >
      <div style="width:800px">
        <fdev-table
          class="table"
          :columns="appColumns"
          :data="appTableData"
          row-key="id"
          :pagination.sync="pagination"
          no-select-cols
          noExport
          hide-bottom
          :selected.sync="selectedData"
          selection="multiple"
        >
          <template v-slot:body="props">
            <fdev-tr :props="props">
              <fdev-td>
                <fdev-checkbox v-model="props.selected" />
              </fdev-td>
              <fdev-td
                v-for="val in props.cols"
                :key="val.field"
                class="text-ellipsis"
              >
                <div
                  class="inline-block"
                  v-if="
                    have_path === '1' &&
                      (val.field === 'mount_path' || val.field === 'sub_path')
                  "
                >
                  <fdev-icon
                    size="12px"
                    name="edit"
                    v-ripple
                    color="primary"
                    @click.stop
                    v-if="props.selected"
                    class="cursor-pointer"
                  >
                    <fdev-popup-edit
                      v-model="props.row[val.field]"
                      buttons
                      :validate="proteinRangeValidation"
                      @hide="hide"
                    >
                      <fdev-input
                        v-model="props.row[val.field]"
                        dense
                        autofocus
                        :error="errorProtein"
                        :error-message="errorMessageProtein"
                      />
                    </fdev-popup-edit>
                  </fdev-icon>
                </div>
                {{ props.row[val.field] || '-' }}
              </fdev-td>
            </fdev-tr>
          </template>
        </fdev-table>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          label="取消"
          outline
          dialog
          @click="appSelectedDialogOpened = false"
        />
        <fdev-btn label="确定" dialog @click="handleAppSelectedDialogOpened" />
      </template>
    </f-dialog>

    <f-dialog
      v-model="objEditDialogOpened"
      right
      f-sc
      :title="`编辑-${dialogTitle}-应用独有`"
    >
      <fdev-form @submit="handleObjEditDialog" class="q-pa-md bg-white">
        <f-formitem
          diaS
          v-for="(item, i) in editObjData"
          :key="i"
          :label="item.key"
          :optional="!item.required"
        >
          <fdev-input
            v-model="item.value"
            :rules="[
              () => (item.required ? !!item.value : true) || `请输入${item.key}`
            ]"
          />
        </f-formitem>
        <!-- <fdev-btn
          type="submit"
          label="确定"
          color="primary"
          class="full-width"
        /> -->
      </fdev-form>
      <template v-slot:btnSlot>
        <fdev-btn
          label="取消"
          outline
          dialog
          @click="objEditDialogOpened = false"
        />
        <fdev-btn label="确定" dialog @click="handleObjEditDialog" />
      </template>
    </f-dialog>
  </div>
</template>

<script>
import { deployMessageColumns } from '../../utils/constants';
import { mapState, mapMutations } from 'vuex';
import { errorNotify, deepClone } from '@/utils/utils';
import Chip from '../../components/Chip';
export default {
  name: 'StepThree',
  components: { Chip },
  data() {
    return {
      columns: deployMessageColumns().detailColumns,
      have_path: '',
      tableIndex: 0,
      envKeyIndex: 0,
      env_key: 0,
      selectedData: [],
      tableData: {},
      sccTableData: {},
      appTableData: [],
      loading: false,
      appSelectedDialogOpened: false,
      objEditDialogOpened: false,
      errorProtein: false,
      errorMessageProtein: '',
      editObjData: {},
      dialogTitle: '',
      pagination: {
        rowsPerPage: 0,
        page: 1
      },
      appColumns: [],
      dataType: ''
    };
  },
  computed: {
    ...mapState('environmentForm', ['realTimeBindMsg', 'deployDetail'])
  },
  methods: {
    ...mapMutations('environmentForm', ['saveRealTimeBindMsg']),
    handleAppSelectedDialogOpened() {
      /* 保存勾选的pvc，替换表格里的应用独有 */
      if (this.have_path === '1') {
        const confirmBtnDisable = this.selectedData.some(
          item => !item.sub_path || !item.mount_path
        );
        if (confirmBtnDisable) {
          errorNotify('mount_path/sub_path不能为空！');
          return;
        }
      }
      this[this.dataType][this.env_key][this.tableIndex].env_key[
        this.envKeyIndex
      ].own_value = this.selectedData.map(item => {
        delete item.id;
        delete item.__index;
        return item;
      });

      this.selectedData.forEach(v => {
        const selectId = this.createId(v);
        this[this.dataType][this.env_key][this.tableIndex].env_key[
          this.envKeyIndex
        ].all_value.forEach(o => {
          if (selectId === this.createId(o)) {
            o.mount_path = v.mount_path;
            o.sub_path = v.sub_path;
          }
        });
      });
      this.appSelectedDialogOpened = false;
    },
    handleObjEditDialog() {
      this[this.dataType][this.env_key][this.tableIndex].env_key[
        this.envKeyIndex
      ].own_value = {};
      this.editObjData.forEach(item => {
        this.$set(
          this[this.dataType][this.env_key][this.tableIndex].env_key[
            this.envKeyIndex
          ].own_value,
          item.key,
          item.value
        );
      });
      this.objEditDialogOpened = false;
    },
    handleObjEditDialogOpened(data, i, key, table, dataType) {
      if (!data.own_value && !data.value) {
        errorNotify('暂无可选数据！');
        return;
      }
      const own_value = data.own_value ? data.own_value : data.value;
      this.dialogTitle = data.name_cn;
      this.dataType = dataType;
      this.objEditDialogOpened = true;
      this.editObjData = Object.keys(own_value).map(item => {
        return {
          key: item,
          value: data.own_value[item],
          required: data.json_schema.jsonSchema.required
            ? data.json_schema.jsonSchema.required.includes(item)
            : false
        };
      });
      this.env_key = key;
      this.tableIndex = i; // 保存当前实体的index；
      this.envKeyIndex = table.env_key.findIndex(item => item.id === data.id);
    },
    handleAppSelectedDialogOpen(data, i, key, dataType) {
      if (!data.all_value || data.all_value.length === 0) {
        errorNotify('暂无可选数据！');
        return;
      }
      this.dialogTitle = data.name_cn;
      let own_value = typeof data.own_value === 'object' ? data.own_value : [];
      this.env_key = key;
      this.tableIndex = i; // 保存当前实体的index；
      this.selectedData = [];
      this.envKeyIndex = this[dataType][key][i].env_key.findIndex(item => {
        return item.id === data.id;
      });
      // 显示当前属性所有可选打pvc，保存当前index值
      this.appTableData = deepClone(
        this[dataType][key][i].env_key[this.envKeyIndex].all_value
      );

      this.appTableData.forEach(v => {
        v.id = this.createId(v);
        own_value.forEach((o, ownIndex) => {
          if (this.createId(o) === v.id) {
            this.selectedData.push(v);
          }
        });
      }); //已选pvc打勾;
      let appColumnsKeyArr = Object.keys(this.appTableData[0]);
      appColumnsKeyArr.splice(appColumnsKeyArr.indexOf('id', 1));
      this.appColumns = appColumnsKeyArr.map(item => {
        return { name: item, label: item, field: item, align: 'left' };
      });
      this.have_path = data.have_path ? data.have_path : '';
      this.dataType = dataType;
      this.appSelectedDialogOpened = true;
    },
    handleStep3() {
      this.loading = true;
      this.saveRealTimeBindMsg({
        ...this.realTimeBindMsg,
        modelsInfo: this.tableData,
        scc_modelsInfo: this.sccTableData
      });
      this.$emit('next');
      this.loading = false;
    },
    proteinRangeValidation(val) {
      if (!val || !val.trim()) {
        this.errorProtein = true;
        this.errorMessageProtein = '不能为空';
        return false;
      }
      return this.hide();
    },
    hide() {
      this.errorProtein = false;
      this.errorMessageProtein = '';
      return true;
    },
    remove(data) {
      data.own_value = '';
    },
    createId(data) {
      const obj = { ...data };
      if (data.mount_path) {
        delete obj.mount_path;
        delete obj.sub_path;
      }
      return Object.values(obj).join('');
    },
    init() {
      this.tableData = this.realTimeBindMsg.caas_model_env_mapping;
      this.sccTableData = this.realTimeBindMsg.scc_model_env_mapping;
    }
  },
  activated() {
    this.init();
  }
};
</script>

<style lang="stylus" scoped>

.font
  font-weight: 700
.chip-wrapper
  max-width 500px
  display flex
  flex-wrap: wrap;
  align-items center
.icon
  vertical-align: middle;
.td-width
  max-width 500px!important
.table-edit
  max-width 420px!important;
  display inline-block!important;
.string-width
  max-width 200px;
  display inline-block
  overflow hidden
  text-overflow ellipsis
  text-align left
.timeline-title
  font-size 14px
  color #009688
.tooltip
  p
    margin 0
.string-icon
  vertical-align: super;
</style>
