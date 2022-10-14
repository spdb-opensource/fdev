<template>
  <div class="q-pa-md main">
    <fdev-table
      title="版本记录"
      :data="runnerTableData"
      :columns="columns"
      row-key="id"
      :pagination.sync="pagination"
      noExport
    >
      <template v-slot:title-icon>
        <fdev-img :src="require('../assets/version_record.svg')" />
      </template>
      <template v-slot:body-cell-version="props">
        <fdev-td>
          <router-link
            :to="`/configCI/historyPanorama/${props.row.id}`"
            class="link"
          >
            <!-- params: { id: props.row.diffEntity.sourcePipelineId } -->
            {{ props.row.version }}
          </router-link>
        </fdev-td>
      </template>
      <template v-slot:body-cell-diffEntity="props">
        <fdev-td>
          <div class="link" @click="getDiffInfo($event, props.row)">查看</div>
        </fdev-td>
      </template>
    </fdev-table>
    <div v-show="!!currentCard" class="card">
      <div v-if="diff" class="q-mb-md"><span class="label">修改动态</span></div>
      <div align="center" class="q-mt-lg no-data" v-if="!diff">
        <span class="no-data-span">暂无数据</span>
      </div>
      <div v-else v-for="(record, i) in diff" :key="i" class="record">
        <span>{{ record.diffLabel }}</span
        ><span>{{ !!record.diffInfo ? '：' : '' }}</span
        ><span>{{ record.diffInfo }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';

export default {
  name: 'HistoryVersion',
  props: {
    id: {
      type: String
    }
  },
  data() {
    return {
      runnerData: [],
      columns: [
        {
          name: 'version',
          field: 'version',
          label: '版本详情',
          align: 'center'
        },
        {
          name: 'author',
          field: row => row.author.nameCn,
          label: '更新人',
          align: 'center'
        },
        {
          name: 'updateTime',
          field: row => row.updateTime.split(' ')[0],
          label: '更新时间',
          align: 'center'
        },
        {
          name: 'diffEntity',
          field: 'diffEntity',
          label: '改动记录',
          align: 'center'
        }
      ],
      pagination: {
        rowsPerPage: 5
      },
      runnerTableData: [],
      diff: [],
      currentCard: ''
    };
  },

  computed: {
    ...mapState('configCIForm', ['pipeHistoryVersion'])
  },
  methods: {
    ...mapActions('configCIForm', ['getPipelineHistoryVersion']),
    getDiffInfo(e, { diffEntity, id }) {
      e.stopPropagation();
      this.currentCard = id;
      this.diff = diffEntity && diffEntity.diff;
    },
    showCard(id) {
      return this.currentCard === id;
    },
    async getTableData() {
      await this.getPipelineHistoryVersion({
        pipelineId: this.id
      });
      this.runnerTableData = this.pipeHistoryVersion;
      this.pagination.rowsNumber = this.pipeHistoryVersion.length;
    }
  },
  created() {
    document.onclick = e => {
      this.currentCard = '';
    };
    this.getTableData();
  }
};
</script>

<style lang="stylus" scoped>
.align-right
  position: absolute;
  right: 1%;
.link
  color: #027be3;
  cursor: pointer;
/deep/ .q-table__card .q-table__middle
  overflow visible
.card
  width 263px
  height 340px
  position absolute
  z-index 9999
  padding 32px
  background #fff
  top: 30%;
  right: -240px;
.main
  position relative
.label
  font-size: 16px;
  color: #333333;
  letter-spacing: 0;
  line-height: 28px;
.record
  margin-bottom 8px
.no-data
  display: flex;
  align-items: center;
  align-content: center;
  text-align: center;
  height: 200px;
  width: 100%;
.no-data-span
  font-size: 22px;
  color: #b0bec5;
  margin: auto;
</style>
