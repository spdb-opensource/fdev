<template>
  <Loading :visible="gridFlag">
    <f-block class="column items-end fit measure-dashboard" id="measureBlock">
      <div class="row full-width  justify-between q-mb-sm">
        <!-- 标题 -->
        <div class="row items-center q-pl-md">
          <fdev-img :src="titleIcon" style="width:32px;height:32px" />
          <span class="text-h6 q-mr-sm">仪表盘</span>
          <f-icon name="help_c_o" class="cursor-pointer text-primary " />
          <fdev-tooltip
            >周期<br />每周：当前周到往前六周数据;<br />每月：当前月到往前六个月数据</fdev-tooltip
          >
        </div>
        <!-- 右上角操作按钮 -->
        <div>
          <!-- 点击恢复到上一次保存的数据 -->
          <fdev-btn
            normal
            ficon="refresh_c_o"
            label="恢复默认状态"
            @click="init"
          />
          <fdev-btn
            normal
            label="新增"
            class="q-ml-md"
            ficon="add"
            @click="compMarketDia = true"
          />
          <fdev-btn
            label="保存"
            ficon="check"
            :loading="loading"
            class="q-mx-md"
            dialog
            @click="saveDash"
            v-forbidMultipleClick
          />
        </div>
      </div>
      <!--卡片展示面板 -->
      <div ref="grid" class="fit">
        <GridLayout
          style="width:100%; touch-action: none"
          :is-draggable="true"
          :is-resizable="true"
          :layout.sync="layout"
          :col-num="colNum"
          :row-height="rowHeight"
          :margin="[16, 16]"
          :use-css-transforms="true"
          :autoSize="true"
        >
          <grid-item
            v-for="(item, ind) in layout"
            :x="item.x"
            :y="item.y"
            :w="item.w"
            :h="item.h"
            :i="item.i"
            :key="ind"
            :minH="3"
            :minW="3"
          >
            <!-- 每个指标卡片 -->
            <div class="box-style q-pa-md">
              <div class="row full-width items-center justify-between">
                <div>
                  <span class="title-style">{{ item.nameCn }}</span>
                  <!-- 提示图标 -->
                  <f-icon
                    name="help_c_o"
                    width="14"
                    height="14"
                    class="cursor-pointer text-primary q-ml-sm"
                  ></f-icon>
                  <fdev-tooltip>{{ item.tips }}</fdev-tooltip>
                </div>
                <div class="row cursor-pointer items-center no-wrap">
                  <!-- 编辑图标 -->
                  <f-icon
                    name="edit"
                    @click="changeParams(item)"
                    class="q-mr-md text-primary"
                  />
                  <!-- 删除图标 -->
                  <f-icon
                    name="close"
                    class="text-grey-3"
                    @click="deleteComp(item.i)"
                  />
                </div>
              </div>
              <!-- 图表 -->
              <components
                :render="render"
                :is="item.params.graphType.value"
                :dataSource="item.compData"
                :w="item.w"
                :h="item.h"
                :currentBlock="currentBlock"
                class="text-center"
              />
            </div>
          </grid-item>
        </GridLayout>
      </div>
      <!-- 编辑卡片弹窗 -->
      <f-dialog
        title="选择参数"
        v-if="managingComp"
        f-sc
        v-model="paramCfgDia"
        @before-close="initParam"
      >
        <Loading :visible="updateFlag">
          <!-- 筛选条件 -->
          <f-formitem
            diaS
            :label="customParamVal(param, 'label', paramCfg)"
            v-for="(param, i) in managingComp.paramCfg"
            :key="i"
            class="q-mt-md"
            label-style="width:80px"
          >
            <components
              :is="customParamVal(param, 'compName', paramCfg)"
              :options="getOpts(param)"
              v-model="managingParam[customParamVal(param, 'key')]"
              @input="selectDate($event, param)"
            /> </f-formitem
        ></Loading>

        <template v-slot:btnSlot>
          <fdev-btn label="取消" outline dialog @click="initParam" />
          <fdev-btn label="确定" dialog @click="updateParams" />
        </template>
      </f-dialog>
      <!-- 添加指标弹窗 -->
      <f-dialog right title="添加指标" v-model="compMarketDia">
        <div class="row no-wrap q-mx-md dia-content">
          <fdev-tabs v-model="tab" vertical>
            <fdev-tab
              v-for="type in typeKeys"
              :key="type"
              :name="type"
              :label="compType[type]"
            />
          </fdev-tabs>
          <fdev-separator vertical class="q-mx-lt" />
          <fdev-tab-panels
            v-model="tab"
            animated
            swipeable
            vertical
            transition-prev="jump-up"
            transition-next="jump-up"
            class="tab-panels"
          >
            <fdev-tab-panel v-for="type in typeKeys" :key="type" :name="type">
              <!-- 有数据时 -->
              <f-scrollarea v-if="compDevidedByType[type]" class="full-height">
                <div
                  v-for="comp in compCfg.filter(comp => comp.type === type)"
                  :key="comp.nameEn"
                  class="row no-wrap q-mb-sm full-width items-start tab-card"
                >
                  <fdev-img
                    :src="compMarketIcon[comp.paramInit.graphType.value]"
                    style="width:28px;height:28px"
                    class="q-mx-sm"
                  />
                  <div class="column">
                    <span class="itemTitleStyle q-mb-md">{{
                      comp.nameCn
                    }}</span>
                    <div class="row items-center no-wrap">
                      <span class="text-body2 comp-desc">{{ comp.desc }}</span>
                      <div>
                        <fdev-btn
                          outline
                          dialog
                          label="添加"
                          v-forbidMultipleClick
                          :disable="!comp.canAdd"
                          @click="addComp(comp)"
                        />
                        <fdev-tooltip v-if="!comp.canAdd">{{
                          comp.disableTip || '暂无数据!'
                        }}</fdev-tooltip>
                      </div>
                    </div>
                  </div>
                </div>
              </f-scrollarea>
              <!-- 无数据时 -->
              <div v-else class="column items-center">
                <f-image name="no_data_1" class="q-mt-xl" />
                <span class="text-grey-3 q-mt-xs"
                  >尚未提供该类型指标，敬请期待！</span
                >
              </div>
            </fdev-tab-panel>
          </fdev-tab-panels>
        </div>
      </f-dialog>
    </f-block>
  </Loading>
</template>

<script>
import VueGridLayout from 'vue-grid-layout';
import { COMP_MARKET_ICON, COL_NUM, MIN_SIZE } from '../../utils/constants';
import { successNotify, resolveResponseError } from '@/utils/utils';
import { initCompCfg } from '../../utils/compCfg';
import { UI_TEMPS } from '../../components/UITemps';
import * as apis from '../../services/methods.js';
import Loading from '@/components/Loading';
import { mapState } from 'vuex';
export default {
  components: {
    Loading,
    ...UI_TEMPS,
    GridLayout: VueGridLayout.GridLayout,
    GridItem: VueGridLayout.GridItem
  },
  data() {
    return {
      titleIcon: require('../../assets/title.svg'),
      compMarketIcon: COMP_MARKET_ICON,
      colNum: COL_NUM,
      tab: null,
      compCfg: [],
      typeKeys: [],
      compType: [],
      compCfgObj: [],
      compDevidedByType: {},
      paramCfgDia: false,
      compMarketDia: false,
      rowHeight: 82,
      layout: [],
      paramCfg: [],
      managingComp: null,
      managingParam: null,
      render: false,
      loading: false,
      updateFlag: false,
      gridFlag: false,
      currentBlock: null //当前block面板的宽度
    };
  },
  computed: {
    ...mapState('menu', {
      miniState: 'miniState'
    })
  },
  watch: {
    miniState(val) {
      //侧边栏折叠或打开重新获取当前block的宽度
      setTimeout(() => {
        this.currentBlock = document.getElementById('measureBlock').clientWidth;
      }, 200);
    }
  },
  async mounted() {
    await this.getCfg();
    this.init();
    setTimeout(async () => {
      this.currentBlock = document.getElementById('measureBlock')
        ? document.getElementById('measureBlock').clientWidth
        : this.currentBlock; //赋初值
    }, 500);
    //浏览器缩放时触发
    window.onresize = () => {
      //获取当前block的宽度
      this.currentBlock = document.getElementById('measureBlock')
        ? document.getElementById('measureBlock').clientWidth
        : null;
    };
  },
  methods: {
    // 获取配置
    async getCfg() {
      let {
        COMP_CFG_OBJ,
        COMP_CFG,
        COMP_TYPE,
        PARAM_CFG,
        COMP_TYPES
      } = await initCompCfg();
      this.compCfg = COMP_CFG;
      this.compType = COMP_TYPE;
      this.paramCfg = PARAM_CFG;
      this.typeKeys = COMP_TYPES;
      this.tab = COMP_TYPES[0];
      this.compCfgObj = COMP_CFG_OBJ;
      this.typeKeys.forEach(type => {
        let comps = this.compCfg.filter(comp => comp.type === type);
        this.compDevidedByType[type] = comps.length > 0 ? comps : null;
      });
    },
    // 获取数据
    async init() {
      this.gridFlag = true;
      try {
        let { configs } = await resolveResponseError(() =>
          apis.queryUserConfig()
        );
        this.layout = [];
        configs &&
          configs.forEach(async cfg => {
            let { nameEn, params } = cfg;
            let {
              api,
              apiParam,
              dataProcess,
              nameCn,
              paramCfg
            } = this.compCfgObj[nameEn];
            let data = await resolveResponseError(() =>
              apis[api](apiParam(params))
            );

            let compData = dataProcess(data, params);
            this.layout.push({
              ...cfg,
              api, //接口名称
              apiParam, //函数 apiParam 函数
              dataProcess, //dataProcess 函数
              nameCn, //指标中文名
              tips: data.tips, //指标公式
              paramCfg, //编辑弹框里的筛选条件
              compData // 接口返回的  data  单个图表数据源
            });
          });
        this.gridFlag = false;
      } catch (e) {
        this.gridFlag = false;
      }
    },
    //获取编辑卡片弹窗对应的选择项
    customParamVal(params, name, cfg) {
      return params.type === 'custom'
        ? params[name]
        : cfg
        ? cfg[params.type][name]
        : params.type;
    },
    //获取筛选条件的opts
    getOpts(params) {
      let { type, options } = params;
      let paramCfg = type === 'custom' ? params : this.paramCfg[type];
      let { depends, dependFunc } = paramCfg;
      if (depends) {
        let opts = dependFunc(this.managingParam[depends]);
        this.managingParam[params.type] = opts[0];
        return opts;
      } else return options;
    },
    //保存dashboard配置信息
    async saveDash() {
      let configs = this.layout.map(comp => {
        let { w, h, x, y, i, params, nameEn } = comp;
        return { w, h, x, y, i, params, nameEn };
      });
      this.loading = true;
      try {
        await resolveResponseError(() => apis.addUserConfig({ configs }));
        this.loading = false;
        successNotify('保存成功！');
      } catch (e) {
        this.loading = false;
      }
    },
    //添加组件
    async addComp(comp) {
      let i = Date.now();
      let {
        paramInit,
        paramCfg,
        nameCn,
        nameEn,
        apiParam,
        api,
        dataProcess
      } = comp;
      let data = await resolveResponseError(() =>
        apis[api](apiParam(paramInit))
      );
      let compData = dataProcess(data, paramInit);
      this.layout.push({
        ...MIN_SIZE,
        api,
        apiParam,
        dataProcess,
        nameCn,
        nameEn,
        tips: data.tips,
        paramCfg,
        compData,
        params: JSON.parse(JSON.stringify(paramInit)),
        x: (this.layout.length * MIN_SIZE.w) % COL_NUM,
        y: this.layout.length + COL_NUM,
        i
      });
      this.$q.notify({
        type: 'positive',
        message: '添加成功',
        position: 'top'
      });
    },
    //唤起修改参数的弹窗
    changeParams(comp) {
      this.managingComp = comp;
      this.managingParam = JSON.parse(JSON.stringify(comp.params));
      this.paramCfgDia = true;
    },
    //修改参数
    async updateParams() {
      this.updateFlag = true;
      let { api, dataProcess, apiParam } = this.managingComp;
      try {
        let data = await resolveResponseError(() =>
          apis[api](apiParam(this.managingParam))
        );
        this.managingComp.compData = dataProcess(data, this.managingParam);
        this.managingComp.params = JSON.parse(
          JSON.stringify(this.managingParam)
        );
        this.paramCfgDia = false;
        this.updateFlag = false;
      } catch (e) {
        this.updateFlag = false;
      }
    },
    //关闭修改参数弹窗，初始化参数
    initParam() {
      this.paramCfgDia = false;
    },
    //删除组件
    deleteComp(ind) {
      let index = this.layout.map(item => item.i).indexOf(ind);
      this.layout.splice(index, 1);
    },
    selectDate(val, param) {
      // 开始日期
      if (param.type === 'startDate') {
        const endDate = this.managingParam[
          this.customParamVal({ type: 'endDate' }, 'key')
        ];
        if (!endDate) return;
        const [start, end] = [
          val.split('-').join(''),
          endDate.split('-').join('')
        ];
        if (start > end) {
          this.managingParam[this.customParamVal({ type: 'endDate' }, 'key')] =
            '';
        }
      } else if (param.type === 'endDate') {
        const startDate = this.managingParam[
          this.customParamVal({ type: 'startDate' }, 'key')
        ];
        if (!startDate) return;
        const [start, end] = [
          startDate.split('-').join(''),
          val.split('-').join('')
        ];
        if (start > end) {
          this.managingParam[
            this.customParamVal({ type: 'startDate' }, 'key')
          ] = '';
        }
      }
    }
  }
};
</script>

<style lang="stylus" scoped>


.vue-grid-item.vue-grid-placeholder {
  background: green !important;
}

.title-style {
  font-size: 14px;
  LineG-height: 28px;
  font-weight: 600;
  color: #333333;
}

.box-style {
  width:100%;
  height:100%;
  box-shadow: 0 2px 10px 0 rgba(30, 84, 213, 0.4);
}

.dia-content {
  min-height: 550px;
}

.tab-panels {
  width: 620px;
}

.comp-desc {
  width: 435px;
  LineG-height: 22px;
  margin-right: 32px;
  color: #333333;
  font-family: PingFangSC-Regular;
  font-size: 14px;
  letter-spacing: 0;
  line-height: 22px;
}

.tab-card {
  margin-bottom: 4px;
  width: auto;
  height: 120px;
  padding: 16px 0;
  border-radius: 4px;
  background-size: 800px 120px;
  background-repeat: no-repeat;
  background-position: -32px 0px;
  background-image: url('../../assets/appBack.png');
}

.measure-dashboard {
  padding-left: 0;
  padding-right: 0;
  min-height :600px
}

.itemTitleStyle{
  font-family: PingFangSC-Medium;
  font-size: 14px;
  color: #333333;
  letter-spacing: 0;
  line-height: 22px;
  font-weight:600
}
</style>
