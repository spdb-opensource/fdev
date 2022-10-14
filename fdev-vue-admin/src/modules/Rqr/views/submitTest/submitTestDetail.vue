<template>
  <Loading :visible="loading">
    <div class="fblock">
      <f-block>
        <div class="row justify-between h90 items-center">
          <div>
            <div class="flex items-center">
              <div class="title">
                提测单编号：{{ testOrderDetail.test_order }}
              </div>
              <div
                class="statusStyle"
                :class="colors(filterStatus(testOrderDetail.status))"
              >
                {{ filterStatus(testOrderDetail.status) }}
              </div>
            </div>
            <div class="group">提测小组：{{ testOrderDetail.group_cn }}</div>
          </div>
          <div class="row">
            <div>
              <fdev-btn
                dialog
                ficon="confirm"
                label="确认提交"
                v-if="
                  testOrderDetail.submit_flag.code === '0' ||
                    testOrderDetail.submit_flag.code === '2'
                "
                :disable="testOrderDetail.submit_flag.code === '2'"
                @click="comfireTest"
              />
              <fdev-tooltip
                v-if="testOrderDetail.submit_flag.code === '2'"
                position="top"
              >
                {{ testOrderDetail.submit_flag.msg }}
              </fdev-tooltip>
            </div>
            <div>
              <fdev-btn
                class="q-ml-md"
                v-if="
                  testOrderDetail.update_flag.code === '0' ||
                    testOrderDetail.update_flag.code === '2'
                "
                :disable="testOrderDetail.update_flag.code === '2'"
                dialog
                ficon="edit"
                label="编辑"
                @click="handleEditSubmitTestOpen"
              />
              <fdev-tooltip
                v-if="testOrderDetail.update_flag.code === '2'"
                position="top"
              >
                {{ testOrderDetail.update_flag.msg }}
              </fdev-tooltip>
            </div>
            <div>
              <fdev-btn
                dialog
                ficon="delete_o"
                label="删除"
                class="q-ml-md"
                v-if="
                  testOrderDetail.delete_flag.code === '0' ||
                    testOrderDetail.delete_flag.code === '2'
                "
                :disable="testOrderDetail.delete_flag.code === '2'"
                @click="deleteTest"
              />
              <fdev-tooltip
                v-if="testOrderDetail.delete_flag.code === '2'"
                position="top"
              >
                {{ testOrderDetail.delete_flag.msg }}
              </fdev-tooltip>
            </div>
            <div>
              <fdev-btn
                v-if="flagInfo.code === '0' || flagInfo.code === '2'"
                :disable="flagInfo.code === '2'"
                dialog
                ficon="copy"
                label="复制"
                class="q-ml-md"
                @click="handleCopySubmitTestOpen()"
              />
              <fdev-tooltip v-if="flagInfo.code === '2'" position="top">
                {{ flagInfo.msg }}
              </fdev-tooltip>
            </div>
          </div>
        </div>
      </f-block>
      <f-block class="mt10">
        <fdev-tabs v-model="tab" align="left">
          <fdev-tab name="message" label="基本信息" />
          <fdev-tab name="document" label="关联文档" />
        </fdev-tabs>
        <div class="line"></div>
        <fdev-tab-panels v-model="tab">
          <fdev-tab-panel :name="tab">
            <component :is="tab" :testOrderDetail="testOrderDetail" />
          </fdev-tab-panel>
        </fdev-tab-panels>
      </f-block>
    </div>
  </Loading>
</template>

<script>
import message from '@/modules/Rqr/views/submitTest/message.vue';
import document from '@/modules/Rqr/views/submitTest/document';
import { resolveResponseError, successNotify } from '@/utils/utils';
import { mapState, mapActions } from 'vuex';
import Loading from '@/components/Loading';
import {
  submitTestOrder,
  deleteTestOrder,
  queryCopyFlag
} from '@/modules/Rqr/services/methods';
import { testOrderModel } from '@/modules/Rqr/model.js';
export default {
  name: 'submitTestDetail',
  data() {
    return {
      status: '',
      tab: 'message',
      id: this.$route.params.id,
      testOrderDetail: testOrderModel(),
      flagInfo: {
        code: '1', //0 可编辑 1不展示  2 不可编辑
        msg: ''
      },
      loading: false
    };
  },
  components: { message, document, Loading },
  computed: {
    ...mapState('demandsForm', ['testOrderDetails'])
  },
  methods: {
    ...mapActions('demandsForm', ['queryTestOrderDetail']),
    colors(col) {
      let arrs = {
        已创建: 'creatC',
        已提测: 'testC',
        已归档: 'fileC',
        已撤销: 'deletedC'
      };
      return arrs[col];
    },
    //确认提交
    comfireTest(val) {
      this.$q
        .dialog({
          title: `确认提示`,
          message: `确认提交提测单编号是
          ${this.testOrderDetail.test_order}的测试单至云测试平台？`,
          ok: true,
          cancel: true
        })
        .onOk(async () => {
          await resolveResponseError(() =>
            submitTestOrder({
              id: this.testOrderDetail.id
            })
          );
          successNotify('确认提交成功');
          this.queryTestDetail();
        });
    },
    //删除提测单
    deleteTest() {
      this.$q
        .dialog({
          title: `删除提示`,
          message: `确认删除提测单编号是
          ${this.testOrderDetail.test_order}的测试单？`,
          ok: true,
          cancel: true
        })
        .onOk(async () => {
          await resolveResponseError(() =>
            deleteTestOrder({
              id: this.testOrderDetail.id
            })
          );
          successNotify('删除成功');
          this.queryTestDetail();
        });
    },
    async queryTestDetail() {
      this.loading = true;
      try {
        await this.queryTestOrderDetail({ id: this.id });
        this.testOrderDetail = this.testOrderDetails;
        this.loading = false;
      } catch (error) {
        this.loading = false;
        throw error;
      }
    },
    //查询是否有复制权限
    async queryCopyFlag() {
      let res = await resolveResponseError(() => queryCopyFlag());
      this.flagInfo = res;
    },
    filterStatus(val) {
      let options = {
        create: '已创建',
        test: '已提测',
        file: '已归档',
        deleted: '已撤销'
      };
      return options[val];
    },
    //编辑提测单
    handleEditSubmitTestOpen() {
      this.$router.push({
        path: `/rqrmn/submitTestOrder/4/${this.id}`
      });
    },
    //复制提测单
    handleCopySubmitTestOpen() {
      this.$router.push({
        path: `/rqrmn/submitTestOrder/3`,
        query: { id: this.id }
      });
    },
    init() {
      this.queryTestDetail();
      this.queryCopyFlag();
    }
  },
  mounted() {
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.title
  font-size 20px
  font-weight 600
  line-height 30px
.group
  font-size 12px
  line-height 20px
  color #666
.statusStyle
  width 65px
  height 22px
  border-radius 2px
  font-size 12px
  line-height 22px
  text-align center
  margin-left 12px
.mt10
  margin-top 10px
.line
  height 1px
  background #DDDDDD
.creatC
  color #24C8F9
  background rgba(36,200,249,0.20)
.testC
  color #04488C
  background rgba(4,72,140,0.20)
.fileC
  color #8CBC48
  background rgba(140,188,72,0.20)
.deletedC
  color #B0BEC5
  background rgba(176,190,197,0.20)
.fblock
  >>> .no-block
    padding-top 20px
    padding-bottom 20px
</style>
