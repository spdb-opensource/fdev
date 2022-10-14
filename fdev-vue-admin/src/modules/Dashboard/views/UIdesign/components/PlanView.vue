<template>
  <div id="mainDiv" style="position:relative">
    <div id="subDiv">
      <!-- 假日效果处理 -->
      <div
        v-for="(item, index) in cols"
        :style="{
          background: isHoliday(item) ? 'rgba(158,158,158,0.15)' : ''
        }"
        style="width: 64px;height: 100%;display: inline-block"
        :key="'col' + index"
      ></div>
    </div>
    <!-- 展示表头（名称和天数数组） -->
    <div class="div4">
      <div class="div5" v-show="showTask">
        项目小组
      </div>
      <div class="demandNo" v-show="showTask">
        需求编号
      </div>
      <div :class="[showTask ? 'div6' : 'divRqr']">{{ typeName }}</div>
      <div
        class="kdStatu"
        style="box-shadow: 5px 0px 5px -5px #ddd;"
        v-show="showTask"
      >
        卡点状态
      </div>
      <div class="flexDiv scroll-normal">
        <div
          v-for="(item, index) in cols"
          :key="'item' + index"
          class="col-item"
        >
          {{ item }}
        </div>
      </div>
    </div>
    <!-- 表格下面的数据 -->
    <!-- 任务（还原审核） -->
    <div v-if="showTask" class="divPosition">
      <div class="div7" v-for="(item, index) in reviewList" :key="index">
        <div class="div8s">
          <div
            style="writing-mode: vertical-lr;padding-bottom:20px;font-weight:600;line-height:60px"
          >
            {{ item.groupName }}
          </div>
          <div style="writing-mode: initial;text-align: center;">总数</div>
          <div style="writing-mode: initial;text-align: center;">
            {{ item.finishedList.length + item.unfinishedList.length }}
          </div>
        </div>
        <div class="div9">
          <div class="bgstyle1"></div>
          <div class="div10">
            <div style="background: #fff">
              <div class="divPass">
                <div>通过</div>
                <div class="div11">{{ item.finishedList.length }}</div>
              </div>
            </div>
            <div
              class="div12"
              :class="item.finishedList.length === 0 ? 'borderb' : ''"
            >
              <div
                style="background: #fff;min-height: 56px;width: 126px"
                v-if="item.finishedList.length === 0"
              ></div>
              <div
                class="divTask"
                v-for="(task, index1) in item.finishedList"
                :key="index1"
              >
                <div class="demandNo ellipsis">
                  <router-link
                    v-if="task.demandNo"
                    :title="task.demandNo"
                    :to="`/rqrmn/rqrProfile/${task.rqrmnt_no}`"
                    class="link"
                  >
                    {{ task.demandNo }}
                  </router-link>
                  <span v-else>-</span>
                </div>
                <div
                  :title="task.name"
                  class="taskName ellipsis"
                  @click="toDesignReview(task.id)"
                >
                  {{ task.name }}
                </div>
                <div class="kdStatu">
                  <div
                    class="flex justify-center items-center"
                    v-if="task.positionStatus === 'ok'"
                  >
                    <div class="succ"></div>
                    <div class="q-ml-sm" style="color:#4DBB59">正常</div>
                  </div>
                  <div class="flex justify-center items-center" v-else>
                    <div class="err"></div>
                    <div class="q-ml-sm" style="color:#EF5350">失败</div>
                  </div>
                </div>
                <div class="dayLength">
                  <div
                    class="div13"
                    :style="'width:' + cols.length * 64 + 'px'"
                  >
                    <div
                      class="square-allot divBox"
                      :id="task.id + '-' + item1.type + '-' + item1.keep"
                      v-for="(item1, index) in getItemData(task.id)"
                      :key="index"
                    ></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="div14">
            <div style="background: #fff">
              <div class="divNoPass">
                <div>未通过</div>
                <div class="div15">{{ item.unfinishedList.length }}</div>
              </div>
            </div>
            <div
              class="div12"
              :class="item.unfinishedList.length === 0 ? 'borderb' : ''"
            >
              <div
                style="background: #fff;min-height: 56px;width: 126px"
                v-if="item.unfinishedList.length === 0"
              ></div>
              <div
                class="divTask"
                v-for="(task, index2) in item.unfinishedList"
                :key="index2"
              >
                <div class="demandNo ellipsis">
                  <router-link
                    v-if="task.demandNo"
                    :title="task.demandNo"
                    :to="`/rqrmn/rqrProfile/${task.rqrmnt_no}`"
                    class="link"
                  >
                    {{ task.demandNo }}
                  </router-link>
                  <span v-else>-</span>
                </div>
                <div
                  :title="task.name"
                  class="taskName ellipsis"
                  @click="toDesignReview(task.id)"
                >
                  {{ task.name }}
                </div>
                <div class="kdStatu">
                  <div
                    class="flex justify-center items-center"
                    v-if="task.positionStatus === 'ok'"
                  >
                    <div class="succ"></div>
                    <div class="q-ml-sm" style="color:#4DBB59">正常</div>
                  </div>
                  <div class="flex justify-center items-center" v-else>
                    <div class="err"></div>
                    <div class="q-ml-sm" style="color:#EF5350">失败</div>
                  </div>
                </div>
                <div class="dayLength">
                  <div
                    class="div17"
                    :style="'width:' + cols.length * 64 + 'px'"
                  >
                    <div
                      class="square-allot divBox"
                      :id="task.id + '-' + item1.type + '-' + item1.keep"
                      v-for="(item1, index) in getItemData(task.id)"
                      :key="index"
                    ></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 需求（设计稿审核） -->
    <div v-else class="divPosition">
      <div class="div7" v-for="(item, index) in reviewList" :key="index">
        <div class="div8">
          {{ item.groupName }}
        </div>
        <div class="div9">
          <div class="bgstyle"></div>
          <div class="div10Rqr">
            <div
              class="div12"
              :class="item.finishedList.length === 0 ? 'borderb' : ''"
            >
              <div
                class="divTask"
                v-for="(task, index1) in item.finishedList"
                :key="index1"
              >
                <div class="taskNameRqr ellipsis">
                  <fdev-tooltip>
                    {{ task.oa_contact_name }}
                  </fdev-tooltip>
                  <router-link
                    class="link"
                    :to="`/rqrmn/designReviewRqr/${task.id}`"
                  >
                    {{ task.oa_contact_name }}
                  </router-link>
                </div>
                <div class="dayLength">
                  <div
                    class="div13"
                    :style="'width:' + cols.length * 64 + 'px'"
                  >
                    <div
                      class="square-allot divBox"
                      :id="task.id + '-' + item1.type + '-' + item1.keep"
                      v-for="(item1, index) in getItemData(task.id)"
                      :key="index"
                    ></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="div14">
            <div
              class="div12"
              :class="item.unfinishedList.length === 0 ? 'borderb' : ''"
            >
              <div
                class="divTask"
                v-for="(task, index2) in item.unfinishedList"
                :key="index2"
              >
                <div class="taskNameRqr ellipsis">
                  <fdev-tooltip>
                    {{ task.oa_contact_name }}
                  </fdev-tooltip>
                  <router-link
                    class="link"
                    :to="`/rqrmn/designReviewRqr/${task.id}`"
                  >
                    {{ task.oa_contact_name }}
                  </router-link>
                </div>
                <div class="dayLength">
                  <div
                    class="div17"
                    :style="'width:' + cols.length * 64 + 'px'"
                  >
                    <div
                      class="square-allot divBox"
                      :id="task.id + '-' + item1.type + '-' + item1.keep"
                      v-for="(item1, index) in getItemData(task.id)"
                      :key="index"
                    ></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import moment from 'moment';
export default {
  name: 'PlanView',
  data() {
    return {};
  },
  props: {
    cols: {
      default: () => [],
      type: Array
    },
    reviewList: {
      default: () => [],
      type: Array
    },
    mainData: {
      default: () => [],
      type: Array
    },
    searchObj: {
      default: () => {},
      type: Object
    },
    showTask: {
      default: false,
      type: Boolean
    }
  },
  // created() {
  //   console.log(this.reviewList, 'reviewList');
  // },
  computed: {
    typeName() {
      if (!this.showTask) {
        return '需求名称';
      } else {
        return '任务名称';
      }
    }
  },
  watch: {
    mainData(val) {
      if (val) {
        let header = document.querySelector('.flexDiv');
        let back = document.getElementById('subDiv');
        let body = document.querySelectorAll('.dayLength');
        header.addEventListener('scroll', function() {
          back.scrollLeft = header.scrollLeft;
          body.forEach(item => {
            item.scrollLeft = header.scrollLeft;
          });
        });
      }
    }
  },
  methods: {
    /**
     * @param {String} id  任务id
     * 通过任务id从计算得到的阶段数组中拿到该任务对应的那个数组
     */
    getItemData(id) {
      let res = [];
      this.mainData.forEach(item => {
        if (item && Array.isArray(item)) {
          item.forEach(val => {
            if (val && Array.isArray(val) && val.length > 0) {
              if (val[0].id && val[0].id === id) {
                res = val;
              }
            }
          });
        }
      });
      return res;
    },
    toDesignReview(id) {
      this.$router.push(`/job/list/${id}/design`);
    },
    // 计算这天是否是节假日
    isHoliday(day) {
      let dayDate = this.searchObj.time.from.slice(-2);
      let date = null;
      let year = null;
      let month = null;
      if (parseInt(day) < parseInt(dayDate)) {
        // 下个月的
        year = moment(this.searchObj.time.to).year();
        month = moment(this.searchObj.time.to).month();
      } else {
        year = moment(this.searchObj.time.from).year();
        month = moment(this.searchObj.time.from).month();
      }
      date = moment({
        years: year,
        months: month,
        date: day
      });
      return date.day() === 0 || date.day() === 6;
    }
  }
};
</script>

<style lang="stylus" scoped>
.square
  display inline-block
  width 16px
  height 16px
  line-height 16px
  margin-left 15px
  border-radius: 2px;
  border-radius: 2px;
.square-allot
  background: #F9816E;
.square-fixing
  background: #FFB64C;
.square-nopass
  background: #5FACFB;
.square-finish
  background: #5CC49E;
.col-item
  display: inline-block;
  height: 14px;
  // flex:1;
  width 64px
.divTask
  display flex
  overflow hidden
  min-height 57px
  align-items center
  border-bottom 1px solid #ddd
  border-right 1px solid #ddd
  flex 1
.divPass
  text-align:center;
  width: 50px;
  height 100%
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: rgba(77,187,89,0.20);
.divNoPass
  text-align:center;
  height 100%
  width: 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: rgba(239,83,80,0.20);
.box
  width: 30px;
  height: 15px;
.divBox
  width:0px;
  height: 16px;
  transition: all .5s;
.taskName
  width: 126px;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  color: var(--q-color-primary);
  padding: 18px 10px;
  background: #fff;
.taskNameRqr
  width 170px
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  color: var(--q-color-primary);
  padding: 18px 10px;
  background: #fff;
.link
  cursor: pointer;
  color: var(--q-color-primary);
.div4{
  height: 54px;
  display:flex;
  background: #F4F6FD;
  line-height: 54px;
  position: sticky;
  top: 64px;
  z-index 1000
  font-weight 600
}
.div5{
  width 110px
  text-align center
}
.kdStatu
  width 84px
  text-align center
.demandNo
  width 210px
  padding 0 10px
.div6{
  width: 126px;
  padding-left 10px
}
.divRqr{
  width: 230px;
  padding-left 70px
  box-shadow: 5px 0px 5px -5px #ddd;
}
.div7{
  display:flex;
  border-bottom: 10px solid #fff;
}
.divPosition {
  z-index: 999;
  position: relative;
}
.div8{
  background: #EFEFEF;
  writing-mode: vertical-lr;
  letter-spacing: 5px;
  width: 60px;
  line-height 60px
  text-align: center;
  font-weight 600
  padding 30px 0
}
.div8s {
  background #EFEFEF
  width 60px
  display flex
  flex-direction column
  justify-content center
  padding 30px 0
}
.div9{
  display: flex;
  flex-direction: column;    width: 96%;
  overflow: hidden;
  position relative
}
.div10{
  width:100%;
  display:flex;
}
.div10Rqr{
  width:100%;display:flex;
}
.div11{
  color: #1976d2;
}
.div12 {
  display: flex;
  flex-direction: column;
  width: 100%;
}
.borderb
  border-bottom 1px solid #ddd
  border-right 1px solid #ddd
.div13 {
  display:flex;
  flex:1;
}
.dayLength
  flex:1;
  overflow-x scroll
  white-space nowrap
  scrollbar-width none
.dayLength::-webkit-scrollbar
  display none
#subDiv::-webkit-scrollbar
  display none
.div14{
  display:flex;
  width:100%;
  flex 1
}
.div15{
  color: #1976d2;
}
.div16{
  flex:1;
  display: flex;
  flex-direction: column;
  width: 96%;
}
.div17{
  display:flex;
  flex:1;
}
.flexDiv{
  flex: 1;
  overflow-x scroll
  overflow-y hidden
  white-space nowrap
}
.q-field--auto-height >>> .q-field--dense >>> .q-field__native {
  min-height: 30px !important;
}
.btn-height {
  height: 30px !important;
}
.q-field--auto-height .q-field__native {
  min-height: 30px !important;
}
.overflow {
  word-break: break-all;
  -webkit-line-clamp: 1;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  display: -webkit-box !important;
  overflow: hidden;
  line-height: 16px;
  max-height: 16px;
}
.bgstyle
  height 100%;
  width 170px
  position absolute
  background #fff
  z-index -1
  left 0
  top 0
  box-shadow: 1px 1px 5px #ddd;
.bgstyle1
  height 100%;
  width 470px
  position absolute
  background #fff
  z-index -1
  left 0
  top 0
  box-shadow: 1px 1px 5px #ddd;
.succ
  background-image: linear-gradient(270deg, rgba(77,187,89,0.50) 0%, #4DBB59 100%);
  height 10px
  width 10px
  border-radius 50%
.err
  background-image: linear-gradient(270deg, rgba(239,83,80,0.50) 0%, #EF5350 100%);
  height 10px
  width 10px
  border-radius 50%
#subDiv
  height 0
</style>
