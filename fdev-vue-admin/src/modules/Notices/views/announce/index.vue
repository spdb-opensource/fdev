<template>
  <f-block>
    <Loading :visible="loading">
      <fdev-table
        :data="tableAnnounceList"
        :columns="columns"
        :filter="filter"
        :filter-method="tableFilter"
        row-key="id"
        noSelectCols
        @row-click="popAnnounce"
        titleIcon="list_s_f"
        title="公告列表"
        noExport
      >
        <template v-slot:top-right>
          <fdev-select
            :value="announceFilter"
            @input="updateAnnounceFilter($event)"
            :options="announceTypeOptions"
            option-value="val"
            option-label="label"
            map-options
            emit-value
          />
        </template>
        <template v-slot:top-bottom>
          <fdev-select
            use-input
            hide-dropdown-icon
            :value="terms"
            @input="updateTerms($event)"
            @new-value="addTerm"
            ref="terms"
            placeholder="请输入"
          >
            <template v-slot:append>
              <f-icon
                name="search"
                class="cursor-pointer"
                :value="filter"
                @click="addNewValue()"
              />
            </template>
          </fdev-select>
        </template>
        <!-- 内容 -->
        <template v-slot:body-cell-content="props">
          <fdev-td>
            <div class="ellipsis" :title="props.row.content">
              {{ props.row.content }}
            </div>
          </fdev-td>
        </template>
        <!-- 创建时间 -->
        <template v-slot:body-cell-create_time="props">
          <fdev-td>
            <div class="ellipsis" :title="props.row.create_time">
              {{ props.row.create_time }}
            </div>
          </fdev-td>
        </template>
        <!-- 类型 -->
        <template v-slot:body-cell-announce_type="props">
          <fdev-td>
            <div class="ellipsis" :title="props.row.type | typeFilter">
              {{ props.row.type | typeFilter }}
            </div>
          </fdev-td>
        </template>
      </fdev-table>
      <div class="text-center" v-if="isManagerRole">
        <fdev-btn
          label="发起公告"
          v-if="isAdmin"
          dialog
          class="q-mb-lg"
          @click="handleOpenSendDialog"
        />
      </div>
      <!-- 发起公告 -->
      <f-dialog v-model="sendAnnounceDialog" title="发起公告">
        <f-formitem required label="公告类型">
          <fdev-select
            ref="announceModel.type"
            v-model="$v.announceModel.type.$model"
            :options="announceType"
            option-label="name"
            option-value="type"
            emit-value
            map-options
            :rules="[() => !$v.announceModel.type.$error || '请选择公告类型']"
        /></f-formitem>
        <f-formitem required label="公告过期时间">
          <fdev-select
            ref="announceModel.date"
            hide-dropdown-icon
            v-model="$v.announceModel.expiry_time.$model"
            :rules="[
              () =>
                !$v.announceModel.expiry_time.$error ||
                '公告过期时间应大于等于当前时间'
            ]"
          >
            <template v-slot:prepend>
              <fdev-icon name="event" class="cursor-pointer">
                <fdev-popup-proxy
                  ref="qDate"
                  transition-show="scale"
                  transition-hide="scale"
                >
                  <fdev-date
                    :options="expiryDateOptions"
                    @input="() => $refs.qDate.hide()"
                    v-model="$v.announceModel.expiry_time.$model"
                    mask="YYYY-MM-DD HH:mm:ss"
                  />
                </fdev-popup-proxy>
              </fdev-icon>
            </template>
            <template v-slot:append>
              <fdev-icon
                name="access_time"
                class="cursor-pointer qicon"
                :disabled="!announceModel.expiry_time"
              >
                <el-time-picker
                  v-model="time"
                  format="HH:mm:ss"
                  value-format="HH:mm:ss"
                  :clearable="false"
                >
                </el-time-picker>
              </fdev-icon>
              <fdev-icon
                name="access_time"
                class="cursor-pointer"
                :disabled="!announceModel.expiry_time"
              >
              </fdev-icon>
            </template>
          </fdev-select>
        </f-formitem>
        <f-formitem required label="公告内容">
          <fdev-input
            placeholder="请输入公告内容"
            ref="announceModel.content"
            v-model="$v.announceModel.content.$model"
            type="textarea"
            :dense="$q.platform.is.desktop"
            :rules="[
              () => $v.announceModel.content.required || '请输入公告内容'
            ]"
        /></f-formitem>
        <template v-slot:btnSlot
          ><fdev-btn
            :loading="globalLoading['noticesForm/sendAnnounce']"
            @click="handleSendAnnounce"
            dialog
            label="发起公告"
        /></template>
      </f-dialog>
    </Loading>
    <!-- 公告详情 -->
    <f-dialog v-model="openAnnounceDialog" :title="getTypeName(type)">
      <div
        class="text-body1 q-px-md text-content scroll-thin"
        v-html="descFilter(message)"
      />
      <div v-if="expiry_time" class="expiration-time">
        过期时间：{{ expiry_time }}
      </div>
      <template v-slot:btnSlot
        ><fdev-btn label="知道啦" dialog @click="openAnnounceDialog = false"
      /></template>
    </f-dialog>
  </f-block>
</template>

<script>
import Loading from '@/components/Loading';
import { mapActions, mapState, mapMutations } from 'vuex';
import { validate, successNotify } from '@/utils/utils';
import { required } from 'vuelidate/lib/validators';
import { createAnnounceModel } from '@/modules/Notices/utils/model';
import moment from 'moment';
import { findAuthority } from '@/modules/User/utils/model';

export default {
  name: 'Announce',
  components: {
    Loading
  },
  data() {
    return {
      loading: false,
      announceModel: createAnnounceModel(),
      columns: [
        {
          name: 'content',
          label: '公告内容',
          field: 'content',
          align: 'left'
        },
        {
          name: 'create_time',
          label: '创建时间',
          field: 'create_time',
          align: 'left'
        },
        {
          name: 'announce_type',
          label: '公告类型',
          field: 'type',
          align: 'left'
        }
      ],
      isManagerRole: false,
      sendAnnounceDialog: false,
      message: '',
      expiry_time: '',
      openAnnounceDialog: false,
      announceType: [
        { type: 'announce-halt', name: '停机公告' },
        { type: 'announce-update', name: '更新公告' }
      ],
      type: '',
      announceTypeOptions: [
        { label: '全部', val: 'all' },
        { label: '停机公告', val: 'announce-halt' },
        { label: '更新公告', val: 'announce-update' }
      ],
      tableAnnounceList: [],
      time: ''
    };
  },
  validations: {
    announceModel: {
      content: {
        required
      },
      type: {
        required
      },
      expiry_time: {
        expiryTimeRule(val) {
          if (!val) {
            return true;
          }
          let currentDate = moment(new Date()).format('YYYY-MM-DD HH:mm:ss');
          return val >= currentDate;
        }
      }
    }
  },
  computed: {
    ...mapState('userActionSaveNotice/noticeAnnounce', {
      filter: 'filter',
      terms: 'terms',
      announceFilter: 'announceFilter'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('noticesForm', {
      announceList: 'announceList' //实体列表
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    //当前账号是否是超级管理员
    isAdmin() {
      return this.currentUser.user_name_en === 'admin';
    }
  },
  filters: {
    typeFilter(type) {
      if (type === 'announce-halt') {
        return '停机公告';
      } else {
        return '更新公告';
      }
    }
  },
  watch: {
    terms(val) {
      this.updateFilter(val.toString());
    },
    announceFilter(val) {
      if (val === 'all') {
        this.tableAnnounceList = this.announceList;
      } else {
        this.tableAnnounceList = this.announceList.filter(item => {
          return item.type === val;
        });
      }
    },
    time(val) {
      this.announceModel.expiry_time =
        this.announceModel.expiry_time.split(' ')[0] + ' ' + val;
    }
  },
  methods: {
    ...mapMutations('userActionSaveNotice/noticeAnnounce', {
      updateFilter: 'updateFilter',
      updateTerms: 'updateTerms',
      updateAnnounceFilter: 'updateAnnounceFilter'
    }),
    ...mapActions('noticesForm', {
      sendAnnounce: 'sendAnnounce',
      queryAnnounce: 'queryAnnounce'
    }),
    getTypeName(type) {
      if (type === 'announce-halt') {
        return '停机公告';
      } else {
        return '更新公告';
      }
    },
    addTerm(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    // 表格 输入搜索功能
    tableFilter(rows, terms, cols, cellValue) {
      const lowerTerms = terms ? terms.toLowerCase().split(',') : [];
      return rows.filter(row => {
        return lowerTerms.every(term => {
          if (term.startsWith('__') || term === '') {
            return true;
          }
          return cols.some(col => {
            return (cellValue(col, row) + '').toLowerCase().indexOf(term) > -1;
          });
        });
      });
    },
    handelDelete(id) {
      this.$q
        .dialog({
          title: '确认删除',
          message: '您确定删除该公告吗?',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          successNotify('删除成功');
        });
    },
    handleOpenSendDialog() {
      this.announceModel.type = '';
      this.announceModel.content = '';
      this.announceModel.expiry_time = moment(new Date()).format(
        'YYYY-MM-DD HH:mm:ss'
      );
      this.sendAnnounceDialog = true;
    },
    async handleSendAnnounce() {
      this.$v.announceModel.$touch();
      let announceKeys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('announceModel') > -1;
      });
      validate(announceKeys.map(key => this.$refs[key]));
      if (this.$v.announceModel.$invalid) {
        return;
      }
      // 增加二次确认弹窗
      this.$q
        .dialog({
          title: '发送公告确认',
          message: '点击确认后数据将会发送到所有fdev用户，确认仍要发送？',
          ok: '确认发送',
          cancel: '取消',
          persistent: true
        })
        .onOk(async () => {
          await this.sendAnnounce(this.announceModel);
          this.sendAnnounceDialog = false;
          successNotify('发起公告成功');
        });
    },
    addNewValue() {
      if (this.$refs.terms.inputValue.length) {
        this.$refs.terms.add(this.$refs.terms.inputValue);
        this.$refs.terms.inputValue = '';
      }
    },
    popAnnounce(event, row, index) {
      this.type = row.type;
      this.message = row.content;
      this.expiry_time = row.expiry_time;
      this.openAnnounceDialog = true;
    },
    descFilter(wsMsg) {
      if (!wsMsg) {
        return wsMsg;
      }
      let reg = new RegExp(/\n/g);
      let reg1 = new RegExp(/\s/);
      let reg2 = new RegExp(/</g);
      let reg3 = new RegExp(/>/g);
      let str1 = wsMsg
        .replace(reg2, '&lt;')
        .replace(reg3, '&gt;')
        .replace(reg, '</br>');
      let str2 = '';
      while (reg1.test(str1)) {
        str2 = str1.replace(reg1, '&nbsp;');
        str1 = str2;
      }
      return str2 ? str2 : str1;
    },
    expiryDateOptions(date) {
      let newDate = date.replace(/\//g, '-');
      let currentDate = moment(new Date()).format('YYYY-MM-DD');
      let currentTime = moment(new Date()).format('HH:mm:ss');
      let expiry_time = this.announceModel.expiry_time;
      let timeModel = expiry_time ? expiry_time.split(' ')[1] : '';
      if (!!timeModel && timeModel < currentTime) {
        return newDate > currentDate;
      } else {
        return newDate >= currentDate;
      }
    },
    expiryTimeOptions(hour, minute, second) {
      let currentDate = moment(new Date()).format('YYYY-MM-DD');
      let currentHour = moment(new Date()).format('HH');
      let currentMinute = moment(new Date()).format('mm');
      let currentSecond = moment(new Date()).format('ss');
      let dateModel = this.announceModel.expiry_time.split(' ')[0];
      if (hour && !minute) {
        return dateModel > currentDate ? true : hour >= currentHour;
      } else if (minute && !second) {
        return hour > currentHour ? true : minute >= currentMinute;
      } else {
        return minute > currentMinute ? true : second > currentSecond;
      }
    }
  },
  async created() {
    this.loading = true;
    await this.queryAnnounce();
    //前端分页条件announceFilter
    this.tableAnnounceList =
      this.announceFilter === 'all'
        ? this.announceList
        : this.announceList.filter(item => {
            return item.type === this.announceFilter;
          });
    if (
      findAuthority(this.currentUser) === 'admin' ||
      this.currentUser.user_name_en === 'admin'
    ) {
      this.isManagerRole = true;
    }
    this.loading = false;
  },
  mounted() {}
};
</script>
<style lang="stylus" scoped>
.expiration-time
  color #999999
  font-size 14px
  text-align right
  position relative
  top 20px
.fdev-table--col-auto-width
  padding-right 10px!important
.q-layout-container
  height 550px!important
.text-content
  padding-top 24px !important
  max-height 45vh
  margin-bottom 30px
  min-height 220px
  padding-bottom 0
.q-dialog__inner > div
  overflow hidden
.text-body1
  max-width:500px
  overflow-wrap break-word!important

.qicon
  z-index: 999;
  opacity: 0;
  position: absolute;
  float: right;
</style>
<style>
.el-time-panel {
  z-index: 9999 !important;
}
</style>
