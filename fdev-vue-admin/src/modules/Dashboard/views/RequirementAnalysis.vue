<template>
  <f-block page>
    <div class="q-pa-sm bg-white">
      <span class="text-grey-9 q-pl-sm">选择小组：</span>
      <div class="float-right l-flex">
        <span class="text-primarycursor-pointer">是否包含子组：</span>
        <fdev-toggle v-model="isParent" left-label />
        <span class="text-primary cursor-pointer" @click="close">
          {{ open ? '展开' : '收起' }}
        </span>
        <span class="text-primary q-ml-md cursor-pointer" @click="reset">
          重置
        </span>
      </div>
      <GroupsTree
        v-model="selectedList"
        class="bg-white q-mt-sm"
        :data="groupsData"
        ref="groupsTree"
        :firstData="firstData"
      />
    </div>
    <Loading :visible="loading">
      <fdev-card flat square class="q-pa-md">
        <div class="q-pb-sm">
          <div class="column items-center">
            <div class="text-h5 text-title q-ma-md">需求统计</div>
          </div>
        </div>
        <fdev-select
          v-model="groupType"
          :options="groupTypeOptions"
          borderless
          option-value="val"
          option-label="label"
          map-options
          emit-value
          options-dense
          class="select-group"
        />
        <fdev-separator inset />
        <div class="bg-white scroll">
          <table
            width="100%"
            border="1"
            bordercolor="black"
            cellspacing="0"
            cellpadding="0"
            :class="rqrStatisData.length !== 0 ? 'table-width' : ''"
          >
            <thead>
              <tr>
                <th rowspan="2">序号</th>
                <th rowspan="2">分组</th>
                <th colspan="2">需求阶段</th>
                <th rowspan="2">待实施(排队)</th>
                <th rowspan="2">开发阶段</th>
                <th colspan="3">测试阶段</th>
                <th colspan="3">超期需求</th>
                <th colspan="2">非本组需求</th>
                <th rowspan="2">开发人均负荷</th>
                <th rowspan="2">已投产</th>
                <th rowspan="2">合计</th>
              </tr>
              <tr>
                <th>需求预评估</th>
                <th>需求评估</th>
                <th>SIT</th>
                <th>UAT</th>
                <th>REL</th>
                <th>开发启动超期</th>
                <th>提测超期</th>
                <th>投产超期</th>
                <th>开发阶段</th>
                <th>测试阶段</th>
              </tr>
            </thead>
            <!--具体的数据值-->
            <tr v-for="(item, index) in rqrStatisData" :key="index">
              <td class="text-center">{{ item.num }}</td>
              <td class="text-center">{{ item.groupName }}</td>
              <td class="text-center">
                {{ item.ypgyw }} + {{ item.ypgkj }} =
                {{ item.ypgyw + item.ypgkj }}
              </td>
              <td class="text-center">
                {{ item.pgyw }} + {{ item.pgkj }} = {{ item.pgyw + item.pgkj }}
              </td>
              <td class="text-center">
                {{ item.dssyw }} ({{ item.dsspdyw }}) + {{ item.dsskj }} ({{
                  item.dsspdkj
                }}) = {{ item.dssyw + item.dsskj }} ({{
                  item.dsspdyw + item.dsspdkj
                }})
              </td>
              <td class="text-center">
                {{ item.kaifayw }} + {{ item.kaifakj }} =
                {{ item.kaifayw + item.kaifakj }}
              </td>
              <td class="text-center">
                {{ item.cssityw }} + {{ item.cssitkj }} =
                {{ item.cssityw + item.cssitkj }}
              </td>
              <td class="text-center">
                {{ item.csuatyw }} + {{ item.csuatkj }} =
                {{ item.csuatyw + item.csuatkj }}
              </td>
              <td class="text-center">
                {{ item.csrelyw }} + {{ item.csrelkj }} =
                {{ item.csrelyw + item.csrelkj }}
              </td>
              <td class="text-center">
                {{ item.kfqdcqyw }} + {{ item.kfqdcqkj }} =
                {{ item.kfqdcqyw + item.kfqdcqkj }}
              </td>
              <td class="text-center">
                {{ item.tccqyw }} + {{ item.tccqkj }} =
                {{ item.tccqyw + item.tccqkj }}
              </td>
              <td class="text-center">
                {{ item.procqyw }} + {{ item.procqkj }} =
                {{ item.procqyw + item.procqkj }}
              </td>
              <td class="text-center">
                {{ item.nogdevelopyw }} + {{ item.nogdevelopkj }} =
                {{ item.nogdevelopkj + item.nogdevelopyw }}
              </td>
              <td class="text-center">
                {{ item.nogtestyw }} + {{ item.nogtestkj }} =
                {{ item.nogtestkj + item.nogtestyw }}
              </td>
              <td class="text-center">{{ item.kaifarjfh }}</td>
              <td class="text-center">
                {{ item.csproyw }} + {{ item.csprokj }} =
                {{ item.csproyw + item.csprokj }}
              </td>
              <td class="text-center">
                {{ item.totalyw }} + {{ item.totalkj }} =
                {{ item.totalyw + item.totalkj }}
              </td>
            </tr>
          </table>

          <div class="div-msg" v-if="rqrStatisData.length === 0">
            <fdev-icon name="warning" class="warn"></fdev-icon>
            <span>没有可用数据</span>
          </div>
        </div>
        <div class="q-mt-lg">
          <div>
            备注：1、每列 “数字1+数字2” 格式表示 业务需求个数+科技需求个数；
          </div>
          <div class="indent">
            2、待实施列 “数字1(数字2)+数字3(数字4) 格式表示 ”
            业务需求个数(待排队需求个数)+科技需求个数(待排队需求个数)；
          </div>
          <div class="indent">3、合计项不重复统计超期数据；</div>
          <div class="indent">
            4、开发人均负荷 = （开发阶段需求+非本组需求 （开发阶段需求））/
            组内开发资源（保留两位小数）；
          </div>
          <div class="indent">5、∞代表该小组下没有角色为开发人员的人。</div>
        </div>
      </fdev-card>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions } from 'vuex';
import GroupsTree from '@/modules/Dashboard/components/Chart/GroupsTree';
import { deepClone, appendNode } from '@/utils/utils';
import { resetList } from '@/modules/Dashboard/utils/constants';

export default {
  name: 'RequirementAnalysis',
  components: { Loading, GroupsTree },
  data() {
    return {
      loading: false,
      rqrStatisData: [],
      groupType: '0',
      groupTypeOptions: [
        { label: '全部', val: '0' },
        { label: '重要', val: '1' }
      ],
      open: false,
      isParent: false,
      selectedList: deepClone(resetList),
      firstData: deepClone(resetList),
      groupsData: []
    };
  },
  computed: {
    ...mapState('dashboard', ['statis']),
    ...mapState('userForm', {
      groups: 'groups'
    }),
    nodes() {
      const root = this.groups.filter(group => !group.parent);
      const groupList = this.appendNode(
        root,
        this.groups.filter(group => group.id && group.parent)
      );
      return this.addAttribute(groupList);
    }
  },
  watch: {
    async groupType(val) {
      this.loading = true;
      if (val === '1') {
        await this.queryStatis({
          priority: '高',
          groupIds: this.selectedList,
          isParent: this.isParent
        });
      } else {
        await this.queryStatis({
          groupIds: this.selectedList,
          isParent: this.isParent
        });
      }
      this.rqrStatisData = this.sumAnalyze(this.statis);
      this.loading = false;
    },
    async selectedList(val) {
      this.loading = true;
      if (this.groupType === '1') {
        await this.queryStatis({
          priority: '高',
          groupIds: val,
          isParent: this.isParent
        });
      } else {
        await this.queryStatis({
          groupIds: val,
          isParent: this.isParent
        });
      }
      this.rqrStatisData = this.sumAnalyze(this.statis);
      this.loading = false;
    },
    async isParent(val) {
      this.loading = true;
      if (this.groupType === '1') {
        await this.queryStatis({
          priority: '高',
          groupIds: this.selectedList,
          isParent: this.isParent
        });
      } else {
        await this.queryStatis({
          groupIds: this.selectedList,
          isParent: this.isParent
        });
      }
      this.rqrStatisData = this.sumAnalyze(this.statis);
      this.loading = false;
    },
    nodes(val) {
      this.groupsData = deepClone(val);
    }
  },
  methods: {
    ...mapActions('dashboard', ['queryStatis']),
    ...mapActions('userForm', ['fetchGroup']),
    sumAnalyze(data) {
      let num = 1;
      data.forEach(item => {
        item.num = num++;
      });
      return data;
    },
    reset() {
      this.selectedList = deepClone(resetList);
      this.groupsData = deepClone(this.nodes);
      this.$refs.groupsTree.reset();
    },
    close() {
      this.open = this.$refs.groupsTree.open;
      this.$refs.groupsTree.closed();
    },
    addAttribute(data) {
      if (!Array.isArray(data)) {
        return data;
      }
      return data.map(item => {
        return {
          ...item,
          expand: false,
          selected: resetList.indexOf(item.id) > -1 ? true : false,
          children: this.addAttribute(item.children)
        };
      });
    },
    appendNode(parent, set, depth = 2) {
      return appendNode(parent, set, depth);
    }
  },
  async created() {
    this.loading = true;
    await this.fetchGroup();
    await this.queryStatis({
      groupIds: this.selectedList,
      isParent: this.isParent
    });
    this.rqrStatisData = this.sumAnalyze(this.statis);
    this.loading = false;
  }
};
</script>

<style lang="stylus" scoped>
.l-flex{
  display: flex;
  align-items: center;
}
.indent
  text-indent 3em
.table-width
  width 130%
.scroll
  overflow auto
.div-msg
  font-size 12px
  color #000
  line-height 30px
.warn
  margin-right 5px
.select-group
  position absolute
  right 5px
  top 36px
</style>
