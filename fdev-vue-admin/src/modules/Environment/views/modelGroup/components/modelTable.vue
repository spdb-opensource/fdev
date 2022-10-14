<template>
  <div class="row-width">
    <div v-for="(item, index) in models" :key="index">
      <fdev-table
        noExport
        :title="item.second_category + '实体'"
        titleIcon="list_s_f"
        :data="item.modelsInfo"
        selection="single"
        :selected.sync="rowSelected[index]"
        :columns="columns"
        class="q-py-md"
        :auto-width="false"
        no-select-cols
      >
        <template v-slot:top-right>
          <fdev-select
            use-input
            multiple
            hide-dropdown-icon
            v-model="terms[index].value"
            @new-value="addTerm"
            class="table-head-input"
            :ref="'terms' + index"
            v-if="terms[index]"
          >
            <template v-slot:append>
              <fdev-btn flat @click="addNewValue(index)">
                <f-icon name="search" class="cursor-pointer" />
              </fdev-btn>
            </template>
          </fdev-select>
        </template>
      </fdev-table>
    </div>
    <div class="row justify-center q-gutter-x-md">
      <div>
        <fdev-btn
          dialog
          label="确定"
          @click="addModels"
          :disable="ids.length !== models.length"
        />
        <fdev-tooltip anchor="top middle" v-if="ids.length !== models.length">
          每类实体必须选择一个
        </fdev-tooltip>
      </div>
      <fdev-btn dialog label="上一步" @click="stepBack" v-if="path === 'add'" />
      <!-- <fdev-btn dialog label="返回" @click="goBack" /> -->
    </div>
  </div>
</template>

<script>
import { modelGroupColumns } from '../../../utils/constants';

export default {
  name: 'ModelTable',
  data() {
    return {
      rowSelected: [],
      columns: modelGroupColumns().detailColumns,
      ids: []
    };
  },
  props: {
    models: {
      type: Array
    },
    modelsList: {
      type: Array
    },
    path: {},
    terms: {}
  },
  watch: {
    rowSelected(val) {
      this.ids = [];
      val.filter(item => {
        if (item[0]) {
          this.ids.push(item[0].id);
        }
      });
    },
    models() {
      this.init();
    },
    terms: {
      handler(val) {
        this.$emit('filter', val);
      },
      deep: true
    }
  },
  methods: {
    stepBack() {
      this.$emit('goBack');
    },
    addModels() {
      this.$emit('click', this.ids);
    },
    init() {
      this.models.forEach((item, index) => {
        item.modelsInfo.some(model => {
          this.modelsList.some(name => {
            if (model.name_en === name) {
              this.rowSelected[index] = [model];
            }
          });
        });
      });
      this.rowSelected.filter(item => {
        if (item[0]) {
          this.ids.push(item[0].id);
        }
      });
    },
    addNewValue(index) {
      const curremtDom = this.$refs['terms' + index][0];
      if (curremtDom.inputValue.length) {
        curremtDom.add(curremtDom.inputValue);
        curremtDom.inputValue = '';
      }
    },
    addTerm(val, done) {
      if (val.length > 0) {
        done(val);
      }
    }
  }
};
</script>
<style lang="stylus" scoped>
.row-width
  width 100%
</style>
