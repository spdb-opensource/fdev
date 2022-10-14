<template>
  <div class="proContent">
    <Loading :visible="loading">
      <div class="bg-white">
        <fdev-table
          :data="datas"
          :columns="columns"
          class="my-sticky-column-table problemTable"
          hide-bottom
          :pagination.sync="pagination"
          no-export
          no-select-cols
        >
          <template v-slot:top-left>
            <div class="row items-center">
              <div class="Tabletitle">问题描述</div>
              <span>
                <fdev-tooltip
                  v-if="isDisableBtn(null, 1)"
                  anchor="top middle"
                  self="center middle"
                  :offest="[-20, 0]"
                >
                  <span>{{ getErrorMsg(null, 1) }}</span>
                </fdev-tooltip>
                <fdev-btn
                  :disable="isDisableBtn(null, 1)"
                  flat
                  dialog
                  ficon="add"
                  class="q-mr-llg"
                  label="新增"
                  @click="addProblem()"
                />
              </span>
            </div>
          </template>
          <template v-slot:body-cell-problemType="props">
            <fdev-td>
              <div
                v-if="props.row.problemType"
                :title="getProTypeName(props.row.problemType)"
              >
                {{ getProTypeName(props.row.problemType) }}
              </div>
              <div v-else>-</div>
            </fdev-td>
          </template>
          <template v-slot:body-cell-itemType="props">
            <fdev-td>
              <div
                class="ellipsis max-width"
                v-if="props.row.itemType"
                :title="props.row.itemTypeValue"
              >
                {{ props.row.itemTypeValue }}
              </div>
              <div v-else>-</div>
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ props.row.itemTypeValue }}
                </fdev-banner>
              </fdev-popup-proxy>
            </fdev-td>
          </template>
          <template v-slot:body-cell-fixFlag="props">
            <fdev-td>
              <div v-if="props.row.fixFlag" :title="props.row.fixFlag">
                {{ getfixFlagName(props.row.fixFlag) }}
              </div>
              <div v-else>-</div>
            </fdev-td>
          </template>
          <template v-slot:body-cell-operation="props">
            <fdev-td :auto-width="true" class="td-padding">
              <div class="opEdit ">
                <span>
                  <fdev-tooltip
                    v-if="isDisableBtn(props.row, 3)"
                    anchor="top middle"
                    self="center middle"
                    :offest="[-20, 0]"
                  >
                    <span>{{ getErrorMsg(props.row, 3) }}</span>
                  </fdev-tooltip>
                  <fdev-btn
                    flat
                    label="编辑"
                    :disable="isDisableBtn(props.row, 3)"
                    @click="openEdit(props.row)"
                  />
                </span>
                <span class="lflex q-mx-xs"> </span>
                <span>
                  <fdev-tooltip
                    v-if="isDisableBtn(props.row, 2)"
                    anchor="top middle"
                    self="center middle"
                    :offest="[-20, 0]"
                  >
                    <span>{{ getErrorMsg(props.row, 2) }}</span>
                  </fdev-tooltip>
                  <fdev-btn
                    flat
                    label="删除"
                    :disable="isDisableBtn(props.row, 2)"
                    @click="delProblem(props.row)"
                  />
                </span>
              </div>
            </fdev-td>
          </template>
        </fdev-table>
      </div>
    </Loading>
    <editBugDlg v-model="isEditBug" :getParams="params" @close="closeEdit" />
    <addBugDlg v-model="isAddBug" :meetingId="id" @close="handleBugClose" />
  </div>
</template>

<script>
import Loading from '@/components/Loading';
import editBugDlg from './editBugDlg';
import addBugDlg from './addBugDlg';
import { successNotify, resolveResponseError } from '@/utils/utils';
import { codeProblemsColumns } from '@/modules/Network/utils/constants';
import {
  deleteProblemById,
  queryProblemItem
} from '@/modules/Network/services/methods';

export default {
  name: 'NetApproval',
  props: {
    datas: {
      type: Array
    },
    addButton: {
      type: String
    },
    id: {
      //会议id
      type: String
    }
  },
  components: {
    Loading,
    editBugDlg,
    addBugDlg
  },
  data() {
    return {
      isEditBug: false,
      loading: false,
      columns: codeProblemsColumns(),
      params: {},
      proItemOptions: [],
      isAddBug: false,
      pagination: {
        rowsPerPage: 0
      }
    };
  },
  computed: {},
  watch: {},
  methods: {
    getErrorMsg(row, type) {
      if (type == '1') {
        //新增
        if (this.addButton == '1') {
          return '工单终态下不可新增问题';
        } else {
          return '';
        }
      } else if (type == 2) {
        if (row.deleteButton === '1') {
          return '工单终态下不可删除';
        } else {
          return '';
        }
      } else {
        if (row.updateButton === '1') {
          return '工单终态下不可删除';
        } else {
          return '';
        }
      }
    },
    isDisableBtn(row, type) {
      if (type == '1') {
        //新增
        if (this.addButton == '1') {
          return true;
        } else {
          return false;
        }
      } else if (type == '2') {
        if (row.deleteButton === '0') {
          return false;
        } else {
          return true;
        }
      } else {
        if (row.updateButton === '0') {
          return false;
        } else {
          return true;
        }
      }
    },
    addProblem() {
      this.isAddBug = true;
    },
    handleBugClose() {
      this.isAddBug = false;
      this.$emit('rebrash');
    },
    async getproItemOptions() {
      this.proItemOptions = await queryProblemItem({ type: 'problemItem' });
    },
    getProTypeName(val) {
      if (val === 'issue') {
        return '缺陷';
      } else if (val === 'advice') {
        return '建议';
      } else if (val === 'risk') {
        return '风险';
      } else {
        return '';
      }
    },
    getfixFlagName(val) {
      if (val === 'fixed') {
        return '已修复';
      } else if (val === 'notFixed') {
        return '未修复';
      } else {
        return '';
      }
    },
    openEdit(row) {
      this.isEditBug = true;
      this.params = row;
    },
    closeEdit() {
      this.isEditBug = false;
      this.$emit('rebrash');
    },
    async delProblem(row) {
      this.$q
        .dialog({
          title: `确认删除`,
          message: `是否确认删除此问题描述？`,
          ok: '是',
          cancel: '否'
        })
        .onOk(async () => {
          await resolveResponseError(() => deleteProblemById({ id: row.id }));
          successNotify('删除成功!');
          this.$emit('rebrash');
        });
    }
  },
  async created() {
    //await this.getproItemOptions();
  }
};
</script>
<style lang="stylus" scoped>
>>> .problemTable .q-table__top.relative-position.row.items-center.q-mb-lg{
  margin-bottom: 8px !important;
}
>>> .problemTable .q-table__control{
  margin-left: 32px;
}
.Tabletitle{
  margin-left: 32px;
  font-family: PingFangSC-Regular;
  font-weight: 600;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 14px;
}
.proContent{
  margin-top: 10px;
  background: #fff;
}
.lflex{
  border-left:1px solid #DDDDDD;
  height: 14px;
}
</style>
