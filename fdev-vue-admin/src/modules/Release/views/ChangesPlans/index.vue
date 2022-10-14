<template>
  <f-block>
    <Loading class="bg-white" :visible="loading">
      <CreateUpdate
        :changes="changes"
        :data="changesRecordSort"
        :filterValue="filterValue"
        @click="init"
      >
        <f-formitem
          class="col-4 q-pr-md"
          label-style="width:60px"
          bottom-page
          label="名称"
        >
          <fdev-select
            :value="selectValue"
            multiple
            use-input
            hide-dropdown-icon
            ref="select"
            @new-value="addSelect"
            @input="updateSelectValue($event)"
          >
            <template v-slot:append>
              <f-icon
                name="search"
                class="cursor-pointer"
                @click="setSelect($refs.select)"
              />
            </template>
          </fdev-select>
        </f-formitem>

        <f-formitem
          class="col-4 q-pr-md"
          label-style="width:60px"
          bottom-page
          label="开始日期"
        >
          <f-date
            mask="YYYY/MM/DD"
            :options="sitOptions"
            v-model="changesModel.startDate"
          />
        </f-formitem>

        <f-formitem
          class="col-4 q-pr-md"
          label-style="width:60px"
          bottom-page
          label="结束日期"
        >
          <f-date
            mask="YYYY/MM/DD"
            :options="relOptions"
            v-model="changesModel.endDate"
          />
        </f-formitem>
        <f-formitem
          class="col-4 q-pr-md"
          label-style="width:60px"
          bottom-page
          label="变更类型"
        >
          <fdev-select
            :value="changes"
            :options="changesOptions"
            options-dense
            @input="updateChanges($event)"
          />
        </f-formitem>
      </CreateUpdate>
    </Loading>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import CreateUpdate from '../Manage/createUpdate';
import { mapActions, mapGetters, mapMutations, mapState } from 'vuex';

export default {
  name: 'ChangesPlans',
  components: { Loading, CreateUpdate },
  data() {
    return {
      loading: false,
      filterValue: '',
      data: [],
      changesOptions: ['全部']
    };
  },
  watch: {
    selectValue(val) {
      this.filterValue = val.toString();
      if (val == '') {
        this.filterValue = ',';
      }
      this.updateTerms(this.filterValue);
    },
    changes(val) {
      this.filterValue += ',table,';
      this.updateTerms(this.filterValue);
    },
    'changesModel.endDate'(val) {
      this.init();
    },
    'changesModel.startDate'(val) {
      this.init();
    }
  },
  computed: {
    ...mapState('userActionSaveRelease/changesPlans', [
      'selectValue',
      'changesModel',
      'changes',
      'selector',
      'terms'
    ]),
    ...mapGetters('releaseForm', ['changesRecordSort'])
  },
  methods: {
    ...mapMutations('userActionSaveRelease/changesPlans', [
      'updateSelectValue',
      'updateStartDate',
      'updateEndDate',
      'updateChanges',
      'updateSelector',
      'updateTerms'
    ]),
    ...mapActions('releaseForm', ['queryPlan']),
    ...mapActions('userForm', ['fetchGroup']),
    addSelect(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    // 点击搜索按钮
    setSelect(el) {
      if (el.inputValue.length) {
        el.add(el.inputValue);
        el.inputValue = '';
      }
    },
    sitOptions(date) {
      if (this.changesModel.endDate) {
        return date < this.changesModel.endDate;
      }
      return true;
    },
    relOptions(date) {
      this.changesModel.startDate = this.changesModel.startDate
        ? this.changesModel.startDate
        : '';
      return date > this.changesModel.startDate;
    },
    async init() {
      this.loading = true;
      await this.queryPlan({
        start_date: this.changesModel.startDate,
        end_date: this.changesModel.endDate
      });
      this.getOptions();
      this.loading = false;
    },
    getOptions() {
      this.changesOptions = [
        '全部',
        ...new Set(
          this.changesRecordSort.map(item => {
            return item.type === 'gray' ? '灰度' : '生产';
          })
        )
      ];
    }
  },
  created() {
    this.fetchGroup();
    this.init();
    if (this.terms) {
      this.filterValue = this.terms;
    }
  }
};
</script>

<style lang="stylus" scoped></style>
