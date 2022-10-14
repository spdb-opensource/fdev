<template>
  <div>
    <f-dialog
      right
      v-if="commitTabledata.length > 0"
      :value="value"
      @input="$emit('update:value', false)"
      title="为避免冲突，请尽快同步master分支"
    >
      <fdev-markup-table flat>
        <tbody>
          <fdev-tr class="trStyle">
            <fdev-td class="desc-td">
              落后master提交数：{{ commitTips.commits }}
            </fdev-td>
            <fdev-td class="desc-td" v-if="commitTips.conflict != 0">
              当前存在冲突个数：{{ commitTips.conflict }}
            </fdev-td>
          </fdev-tr>
        </tbody>
      </fdev-markup-table>
      <fdev-table
        v-if="commitTips.conflict != 0"
        :data="commitTabledata"
        :columns="commitCol"
        noExport
        no-select-cols
        titleIcon="list_s_f"
      ></fdev-table>
      <template v-slot:btnSlot>
        <fdev-btn @click="closeDia" dialog label="确定" />
      </template>
    </f-dialog>
    <!-- 无数据 不显示  table -->
    <f-dialog
      v-else
      :value="value"
      @input="$emit('update:value', false)"
      title="为避免冲突，请尽快同步master分支"
    >
      <fdev-markup-table flat>
        <tbody>
          <fdev-tr class="trStyle">
            <fdev-td class="desc-td no-border">
              落后master提交数：{{ commitTips.commits }}
            </fdev-td>
            <fdev-td class="desc-td no-border" v-if="commitTips.conflict != 0">
              当前存在冲突个数：{{ commitTips.conflict }}
            </fdev-td>
          </fdev-tr>
        </tbody>
      </fdev-markup-table>
      <template v-slot:btnSlot>
        <fdev-btn @click="closeDia" dialog label="确定" />
      </template>
    </f-dialog>
  </div>
</template>

<script>
import { mapState } from 'vuex';

export default {
  name: 'BehindMasterfdev-dialog',
  data() {
    return {
      commitCol: [
        {
          name: 'fileName',
          label: '冲突文件',
          field: 'fileName',
          align: 'left'
        }
      ]
    };
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    commitTabledata: {
      type: Array,
      default: function() {
        return [];
      }
    }
  },
  computed: {
    ...mapState('jobForm', ['commitTips'])
  },
  methods: {
    closeDia() {
      this.$emit('changeHandleShow', false);
    }
  }
};
</script>
<style lang="stylus" scoped>
.trStyle
  height 80px
.btnStyle
  display: flex;
  justify-content: center;
  padding-bottom: 20px;
.no-border
  border-bottom none !important
</style>
