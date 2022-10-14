<template>
  <f-dialog
    :value="value"
    @input="$emit('input', false)"
    :title="`编辑${title}属性映射值`"
    f-sc
    right
  >
    <fdev-table
      :columns="columns"
      :data="attrMapKey"
      noExport
      no-select-cols
      :title="`${title}属性映射值表`"
      title-icon="list_s_f"
      style="width:1000px"
      class="my-sticky-column-table scroll-thin-x"
    >
      <template v-slot:top-right>
        <fdev-btn ficon="add" label="添加" normal @click="addPVCKey" />
      </template>
      <template v-slot:header-cell="props">
        <fdev-th :props="props" :title="props.col.name">
          <div class="row no-wrap">
            <span class="text-red" v-if="props.col.required">*</span>
            <div>
              {{ props.col.label }}
            </div>
          </div>
        </fdev-th>
      </template>
      <template v-slot:body="props">
        <fdev-tr :props="props">
          <fdev-td
            v-for="td in columns"
            :key="td.name"
            :title="props.row[td.name]"
          >
            <div class="row no-wrap">
              <div v-if="td.name !== 'operate'">
                <fdev-icon name="edit" color="primary" @click.stop>
                  <fdev-popup-edit
                    v-model="props.row[td.name]"
                    buttons
                    :validate="val => inputValidation(td, val)"
                    @hide="inputValidationClose"
                    label-set="Save"
                    label-cancel="Close"
                    @before-show="
                      props.row[td.name] = props.row[td.name].replace(/^ /g, '')
                    "
                  >
                    <fdev-input
                      v-model="props.row[td.name]"
                      autofocus
                      :error="errorProtein"
                      :error-message="errorMessageProtein"
                    />
                  </fdev-popup-edit>
                </fdev-icon>
              </div>
              <div v-else>
                <f-icon
                  name="delete_o"
                  color="red"
                  @click="handelDelete(props['pageIndex'])"
                  :width="14"
                  :height="14"
                  class="q-mr-md"
                />
              </div>
              <div class="td-desc">
                {{ props.row[td.name] }}
              </div>
            </div>
          </fdev-td>
        </fdev-tr>
      </template>
    </fdev-table>
    <template v-slot:btnSlot>
      <fdev-btn label="确定" dialog @click="savePVC"
    /></template>
  </f-dialog>
</template>

<script>
import { errorNotify } from '@/utils/utils';
export default {
  name: 'tableDialog',
  data() {
    return {
      errorProtein: false,
      errorMessageProtein: ''
    };
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    customDeployData: {
      type: Array,
      required: true
    },
    title: {
      type: String,
      required: true
    },
    columns: {
      type: Array,
      required: true
    },
    attrMapKey: {
      type: Array,
      required: true
    },
    json_schema: {
      type: Object,
      required: true
    }
  },
  filters: {
    // filterName(val) {
    //   let label = '';
    //   if (val.name === 'note' || val.name === 'operate') {
    //     label = val.label;
    //   } else {
    //     label = val.name;
    //   }
    //   return label;
    // }
  },
  methods: {
    handelDelete(index) {
      this.attrMapKey.splice(index, 1);
      this.attrMapKey.map((item, index) => (item.indexValue = index));
    },
    inputValidation(td, val) {
      if (td.required === true && val.trim().length < 1) {
        this.errorProtein = true;
        this.errorMessageProtein = `请输入${td.label}`;
        return false;
      }
      return this.inputValidationClose();
    },
    inputValidationClose() {
      this.errorProtein = false;
      this.errorMessageProtein = '';
      return true;
    },
    // 新增一行属性映射值
    addPVCKey() {
      let data = {};
      Object.keys(this.json_schema.properties).forEach(item => {
        if (
          this.json_schema.required &&
          this.json_schema.required.includes(item)
        ) {
          data[item] = ' ';
        } else {
          data[item] = '';
        }
      });
      this.attrMapKey.push(data);
      this.attrMapKey.map((item, index) => (item.indexValue = index));
    },
    savePVC() {
      // 判断是否有必填项未填，有一个就弹框提示
      // 如果必填项现在的值是' '，也不能通过，还是需要重新修改内容
      let required = this.columns.some(col => {
        if (col.required) {
          return this.attrMapKey.some(item => {
            return Object.keys(item).some(key => {
              if (key === col.name) {
                return !item[key].trim();
              }
            });
          });
        }
      });

      if (required) {
        errorNotify('属性映射值有必填项未填写');
        return;
      }
      this.attrMapKey.map(val => delete val.indexValue);
      this.$emit('getTableDate', this.attrMapKey);
    }
  }
};
</script>

<style lang="stylus" scoped>
.td-desc
  max-width 180px
  overflow hidden
  text-overflow ellipsis
</style>
