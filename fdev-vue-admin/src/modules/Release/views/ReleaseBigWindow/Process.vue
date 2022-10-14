<template>
  <Loading :visible="loading['releaseForm/']">
    <div class="row">
      <div>
        <fdev-date
          v-model="date"
          flat
          bordered
          :events="events"
          v-show="!isEditing"
          mask="YYYY-MM-DD"
        />

        <fdev-date
          v-show="isEditing"
          v-model="selectDate"
          :options="timeOptions"
          mask="YYYY-MM-DD"
          multiple
        />

        <div class="text-center q-mt-md">
          <div v-show="isEditing">
            <fdev-btn label="取消" @click="cancel" />
            <div class="inline-block">
              <fdev-btn
                label="确定"
                @click="confirm"
                :disable="selectDate && selectDate.length < 8"
                :loading="loading['releaseForm/updateBigReleaseDate']"
              />
              <fdev-tooltip v-if="selectDate && selectDate.length < 8">
                投产周期为8天，目前已选{{ selectDate.length }}天!
              </fdev-tooltip>
            </div>
          </div>
          <div v-show="!isEditing">
            <fdev-btn
              label="设置投产周期"
              @click="handleEdit"
              :disable="!isReleaseContact()"
              :loading="loading['releaseForm/queryBigReleaseNodes']"
            />
            <fdev-tooltip v-if="!isReleaseContact()">
              请联系牵头联系人!
            </fdev-tooltip>
          </div>
        </div>
      </div>

      <ul
        :class="{ 'is-editing': isEditing }"
        class="text-wrapper col bordered"
        v-if="todo"
      >
        <li class="text-h6">{{ isToday }}完成事项：</li>
        <li v-for="(item, i) in todo" :key="item">{{ i + 1 }}. {{ item }}</li>
        <li v-for="(item, i) in 8 - todo.length" :key="i" />
      </ul>
    </div>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import axios from 'axios';
import date from '#/utils/date.js';
import { watchRouteParams } from '../../utils/mixin';
import { mapState, mapActions, mapGetters } from 'vuex';
import { successNotify } from '@/utils/utils';
export default {
  name: 'Process',
  mixins: [watchRouteParams],
  components: { Loading },
  data() {
    return {
      date: '',
      selectDate: [],
      release_date: '',
      isEditing: false,
      releaseDateLog: {}
    };
  },
  computed: {
    ...mapState('global', ['loading']),
    ...mapState('user', ['currentUser']),
    ...mapState('releaseForm', ['releaseDate', 'bigRelease']),
    ...mapGetters('releaseForm', ['existingDate', 'isReleaseContact']),
    events() {
      const date = { ...this.releaseDate };
      delete date.release_node_name;
      return Object.values(date)
        .toString()
        .replace(/-/g, '/')
        .split(',');
    },
    dateAndLog() {
      const date = { ...this.releaseDate };
      delete date.release_node_name;
      const dateT = Object.keys(date);
      const logObj = {};
      dateT.forEach(key => {
        logObj[date[key]] = this.releaseDateLog[key];
      });
      return logObj;
    },
    todo() {
      const selected = date.formatDate(this.date, 'X');
      const firstDate = date.formatDate(this.releaseDate['T-8'], 'X');
      const lastDate = date.formatDate(this.releaseDate['T'], 'X');
      if (selected < firstDate) {
        return this.releaseDateLog['T-8之前'];
      }
      if (selected > lastDate) {
        return [];
      }
      if (this.dateAndLog[this.date]) return this.dateAndLog[this.date];

      let dateRange = [...this.events];
      dateRange = dateRange.sort().slice(0, 7);
      let i = 0;
      let log = [];
      while (i < 7) {
        if (date.formatDate(dateRange[i], 'X') > selected) {
          const result = dateRange[i - 1].replace(/\//g, '-');
          log = this.dateAndLog[result];
          break;
        } else {
          i++;
        }
      }
      return log;
    },
    isToday() {
      const today = date.formatDate(new Date(), 'YYYY-MM-DD');
      return today === this.date ? '今天' : this.date;
    },
    existingDateDeleteReleaseDate() {
      const existingDate = [...this.existingDate];
      const index = existingDate.indexOf(this.release_date);
      if (index > -1) {
        existingDate.splice(index, 1);
      }
      return existingDate;
    }
  },
  methods: {
    ...mapActions('releaseForm', [
      'queryByReleaseDate',
      'updateBigReleaseDate',
      'queryBigReleaseNodes'
    ]),
    timeOptions(day) {
      return this.selectDate && this.selectDate.length === 8
        ? this.selectDate.includes(day.replace(/\//g, '-'))
        : day < this.release_date.replace(/-/g, '/');
    },
    async confirm() {
      let dateRange = {};
      this.selectDate.sort().forEach((item, i) => {
        dateRange[`T-${8 - i}`] = item;
      });
      await this.updateBigReleaseDate({
        release_date: this.release_date,
        release_date_list: dateRange
      });
      successNotify('修改投产周期成功！');
      this.init();
      this.cancel();
    },
    cancel() {
      this.isEditing = !this.isEditing;
    },
    async handleEdit() {
      await this.queryBigReleaseNodes({
        start_date: date.formatDate(new Date(), 'YYYY-MM-DD')
      });
      const dateObj = { ...this.releaseDate };
      delete dateObj.release_node_name;
      delete dateObj.T;
      this.selectDate = Object.values(dateObj);
      this.cancel();
    },
    async init() {
      this.release_date = this.$route.params.release_date;
      this.queryByReleaseDate({
        release_date: this.release_date
      });
    }
  },
  async created() {
    this.init();
    this.date = date.formatDate(new Date(), 'YYYY-MM-DD');
    if (process.env.NODE_ENV === 'development') {
      const log = await import('./release-date-log.js');
      this.releaseDateLog = log.default;
    } else {
      await axios
        .get(`fdev-configserver/myapp/default/master/release-date-log.json`)
        .then(res => {
          this.releaseDateLog = res.data;
        });
    }
  }
};
</script>

<style lang="stylus" scoped>

.text-wrapper
  height 430px
  margin 0
  margin-left 16px
  border-radius: 5px;
  overflow hidden
  border 1px solid #666666
  padding 0
  &.is-editing
    opacity 0.1
  li
    list-style none
    border-bottom 1px dashed #666666
    height calc(430px / 9)
    align-items: flex-end;
    display flex
    padding-left 1em
    &:nth-of-type(n+2)
      padding-left 3em
</style>
