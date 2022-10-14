<template>
  <fdev-table
    :grid="!$q.platform.is.desktop"
    :hide-header="noHeader"
    :data="table"
    noExport
    :columns="columns"
    row-key="index"
    class="bg-white"
    :pagination.sync="pagination"
    hide-bottom
    flat
  >
    <template v-slot:top-right> </template>
    <template v-slot:body="props">
      <fdev-tr :props="props">
        <fdev-td
          :props="props"
          v-for="col in columns"
          :key="col.name"
          class="td-width"
          :title="
            col.tooltip && props.row[col.name]
              ? dataFilter(props.row[col.name], col.name)
              : ''
          "
        >
          <span class="span-width">
            {{ dataFilter(props.row[col.name], col.name) }}
          </span>
          <fdev-icon
            size="12px"
            name="edit"
            v-ripple
            color="primary"
            @click.stop
            class="relative-position"
            v-if="
              canEdit &&
                (col.name === 'remark' ||
                  col.name === 'description' ||
                  col.name === 'note' ||
                  col.name === 'content')
            "
          >
            <fdev-popup-edit
              v-model="props.row[col.name]"
              buttons
              @save="handleUpdate"
            >
              <fdev-input
                v-model="props.row[col.name]"
                autofocus
                counter
                maxlength="50"
              />
            </fdev-popup-edit>
          </fdev-icon>

          <fdev-btn
            dense
            round
            v-if="col.name === 'name'"
            v-show="
              isTrans
                ? Array.isArray(props.row.item)
                : Array.isArray(props.row.paramList)
            "
            flat
            :icon="props.expand ? 'arrow_drop_up' : 'arrow_drop_down'"
            @click="props.expand = !props.expand"
          />

          <!-- <fdev-tooltip
            anchor="top middle"
            v-if="col.tooltip && props.row[col.name]"
          >
            {{ dataFilter(props.row[col.name], col.name) }}
          </fdev-tooltip> -->
        </fdev-td>
      </fdev-tr>
      <fdev-tr
        v-if="
          props.expand &&
            (isTrans
              ? Array.isArray(props.row.item)
              : Array.isArray(props.row.paramList))
        "
        :props="props"
      >
        <fdev-td colspan="100%">
          <div class="text-left">
            <NastedTable
              :tableData="isTrans ? props.row.item : props.row.paramList"
              :columns="columns"
              :isTrans="isTrans"
              :canEdit="canEdit"
              :noHeader="listHeader"
              @handleUpdate="
                data => {
                  handleChildData(data, props.row);
                }
              "
            />
          </div>
        </fdev-td>
      </fdev-tr>
    </template>
  </fdev-table>
</template>

<script>
import { dataFilter } from '../utils/constants';
export default {
  name: 'NastedTable',
  data() {
    return {
      table: this.createIndex(this.tableData),
      listHeader: true,
      pagination: {
        rowsNumber: 0
      }
    };
  },
  props: {
    tableData: {
      type: Array,
      default: () => []
    },
    columns: {
      type: Array,
      default: () => []
    },
    isTrans: {
      type: Boolean,
      default: false
    },
    noHeader: {
      type: Boolean,
      default: false
    },
    canEdit: {
      type: Boolean,
      default: false
    }
  },
  watch: {
    tableData: {
      deep: true,
      handler(val, oldVal) {
        this.table = this.createIndex(val);
      }
    }
  },
  methods: {
    createIndex(data = []) {
      data = data === '' ? [] : data;
      return data.map((item, index) => {
        const obj = {};
        for (let i in item) {
          obj[i] = Array.isArray(item[i]) ? this.createIndex(item[i]) : item[i];
        }
        return { ...obj, index: index };
      });
    },
    dataFilter(val, name) {
      if (name === 'required' || name === 'option') {
        return dataFilter[val];
      } else {
        return val ? val : '-';
      }
    },
    handleUpdate(value, initialValue) {
      this.$emit('handleUpdate', this.table);
    },
    handleChildData(data, target) {
      if (this.table[target.index].paramList) {
        this.table[target.index].paramList = data;
      } else {
        this.table[target.index].item = data;
      }
      this.handleUpdate();
    }
  },
  filters: {
    dataFilter(val) {
      return dataFilter[val];
    }
  }
};
</script>
<style lang="stylus" scoped>
.span-width
  max-width 187px
  overflow hidden
  text-overflow ellipsis
  display inline-block
  vertical-align middle
.td-width
  width 205px
</style>
