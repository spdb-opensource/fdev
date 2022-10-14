<template>
  <div>
    <fdev-btn
      menu
      class="fst-menu-btn fst-menu-hover"
      ficon="comment"
      label="通知"
      @click="showing = !showing"
    >
      <fdev-badge color="red" floating v-if="newNotices.length > 0">
        {{ newNotices.length }}
      </fdev-badge></fdev-btn
    >
    <fdev-menu ref="menu" isMenu fit>
      <div class="text-subtitle3 q-mb-md row items-center justify-between">
        <span>通知（{{ newNotices.length }}条未读）</span>
        <fdev-btn
          v-close-popup
          class="self-center"
          flat
          label="查看所有"
          @click="more"
        />
      </div>
      <f-scrollarea
        v-if="newNotices.length && newNotices.length > 0"
        style="max-height:220px"
      >
        <div
          v-for="item in notices"
          :key="item.id"
          class="row no-wrap q-py-xs items-center notice-item q-mb-sm hover-bg"
          @click.prevent="lookMessage(item)"
          :class="{ 'cursor-pointer': item.hyperlink }"
          :title="item.content"
        >
          <div class="notice-circle bg-red-7 q-mr-sm self-start q-mt-sm" />
          <div class="column col">
            <div class="row no-wrap">
              <span class="text-body2 ellipsis-2-lines" style="width:220px">
                【未读】{{ item.content }}
              </span>
              <f-icon
                name="arrow_r_o"
                class="text-primary"
                :height="16"
                v-if="item.hyperlink !== null && item.hyperlink !== ''"
              />
            </div>
            <span class="text-grey-3 text-caption">
              {{ item.create_time }}</span
            >
          </div>
        </div>
      </f-scrollarea>
      <div v-else class="column flex-center" style="width:220px">
        <f-image name="no_data" />
      </div>
    </fdev-menu>
    <f-dialog v-model="announcement" title="公告">
      <div class="dialog">
        <fdev-card-section class="text-body1 text-content">
          <fdev-list bordered separator class="list-title scroll-y">
            <fdev-item
              clickable
              v-ripple
              v-for="msg in newMsgList"
              :key="msg.id"
              @click="showContent(msg.id)"
              :active="link === msg.id"
              active-class="menu-link"
            >
              <fdev-item-section>
                <p class="item-title">{{ msg.type | typeFilter }}公告</p>
              </fdev-item-section>
            </fdev-item>
          </fdev-list>
          <fdev-card-section class="card-content scroll">
            <div
              v-for="msg in newMsgList"
              :key="msg.id"
              v-show="link === msg.id"
              class="div-container"
            >
              <div v-html="descFilter(msg.content)" />
              <span v-if="msg.expiry_time" class="span-position">
                过期时间：{{ msg.expiry_time }}
              </span>
            </div>
          </fdev-card-section>
        </fdev-card-section>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn label="知道啦" dialog @click="announcement = false"
      /></template>
    </f-dialog>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import LocalStorage from '#/plugins/LocalStorage';
import websocket from '@/utils/socket';
import moment from 'moment';
import { setShownNotice, getShownNotice } from './setting.js';

export default {
  name: 'NoticeIcon',
  data() {
    return {
      showing: false,
      announcement: false,
      notices: [],
      msgList: [],
      newMsgList: [],
      link: '',
      ws: '',
      updateShow: false,
      dismiss: null,
      processList: [
        '配置文件更新待确认',
        '任务投产待确认',
        '系统内部错误',
        'process'
      ]
    };
  },
  computed: {
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('global', {
      newNotices: 'newNotices',
      oldNotices: 'oldNotices',
      updateNotices: 'updateNotices'
    })
  },
  filters: {
    typeFilter(type) {
      if (type === 'announce-halt') {
        return '停机';
      } else {
        return '更新';
      }
    }
  },
  watch: {
    newNotices() {
      this.notices = this.newNotices.slice(0, 5);
    }
  },
  methods: {
    ...mapActions('global', {
      updateNotifyStatus: 'updateNotifyStatus',
      fetchNotices: 'fetchNotices'
    }),
    ...mapActions('noticesForm', {
      queryAnnounce: 'queryAnnounce'
    }),
    more() {
      this.$router.push('/notices/message');
    },
    async getWsMsg(event) {
      let msg = JSON.parse(event.data);
      if (
        !Array.isArray(msg) &&
        msg.type !== 'announce-halt' &&
        msg.type !== 'announce-update'
      ) {
        const notifyOptions = {
          message: `
              <div style="max-width: 450px;">
              <i 
                  aria-hidden="true" 
                  class="material-icons q-icon q-notification__icon col-auto float-left q-pl-xs" 
                  style="font-size: 30px;position: relative;top: -4px;"
                >
                  volume_up
                </i> 
                <div style="margin-left: 30px;">
                  <div>${msg.desc}</div>
                  <div style="max-width: 200px;overflow: hidden;text-overflow: ellipsis;">
                    ${msg.content}
                  </div>
                  <div class="text-right">${msg.create_time}</div>
                </div>
              </div>
            `,
          position: 'top-right',
          timeout: 3000,
          color: 'positive',
          textColor: 'white',
          html: true
        };
        if (msg.type === '3') {
          const shownNotice = getShownNotice(this.currentUser.user_name_en);
          shownNotice.push(msg.id);
          setShownNotice(this.currentUser.user_name_en, shownNotice);
          const updateNotifyOptions = {
            ...notifyOptions,
            timeout: 0,
            actions: [
              {
                label: '刷新',
                color: 'white',
                handler: () => {
                  location.reload();
                }
              }
            ]
          };
          if (!this.updateShow) {
            this.updateShow = true;
            this.dismiss = this.$q.notify(updateNotifyOptions);
          } else {
            //关闭已有弹窗
            this.dismiss();
            this.updateShow = true;
            this.dismiss = this.$q.notify(updateNotifyOptions);
          }
        } else if (
          this.processList.includes(msg.desc) ||
          msg.desc === '缺陷通知'
        ) {
          this.$q.notify({
            ...notifyOptions,
            actions: [
              {
                label: '查看',
                color: 'white',
                handler: () => {
                  this.removeShownNotice(msg);
                  this.lookMessage(msg);
                }
              }
            ]
          });
        } else {
          this.$q.notify(notifyOptions);
        }
        //重新调一下接口，查询消息，实时更新消息
        await this.fetchNotices({ target: this.currentUser.user_name_en });
      } else {
        if (!Array.isArray(msg)) {
          this.msgList.push(msg);
        } else {
          this.msgList = msg;
        }
        const user_name_en = this.currentUser.user_name_en;
        let ids = LocalStorage.getItem(user_name_en + '/announceIds')
          ? LocalStorage.getItem(user_name_en + '/announceIds')
          : [];
        // 要显示的公告列表
        let copyMsgList = [];
        let newIds = [];
        this.msgList.forEach(item => {
          newIds.push(item.id);
          let announceIds = LocalStorage.getItem(user_name_en + '/announceIds');
          ids = announceIds ? announceIds : [];
          if (!announceIds || announceIds.indexOf(item.id) < 0) {
            ids.push(item.id);
            LocalStorage.set(user_name_en + '/announceIds', ids);
            copyMsgList.push(item);
          }
        });
        let currentDate = moment(new Date()).format('YYYY-MM-DD HH:mm:ss');
        let noOverTimeList = copyMsgList
          .filter(item => {
            return (
              item.expiry_time > currentDate || !!item.expiry_time === false
            );
          })
          .filter(item => {
            //版本更新不走公告模式
            //type为3代表一直不动，必须刷新的
            return item.type !== '3';
          });
        if (noOverTimeList.length > 0) {
          this.newMsgList = noOverTimeList;
          this.link = this.newMsgList[0].id;
          this.announcement = true;
        }
        // //重新调一下接口，查询通知，实时更新通知列表
        await this.queryAnnounce();
        // // 筛选出新公告以后，将新的id列表存在本地。
        // LocalStorage.set('notices/announceIds', newIds);
      }
    },
    async lookMessage(notice) {
      await this.updateNotifyStatus({ id: [notice.id] });
      this.$refs.menu.updatePosition();
      //为2代表无链接，为3代表需要刷新，为1代表外链接，为0代表内链接
      if (notice.type !== '2' && notice.type !== '3') {
        if (notice.type === '0') {
          window.open(notice.hyperlink, '_self');
        } else {
          window.open(notice.hyperlink);
        }
      }
    },
    removeShownNotice(msg) {
      const latestArr = getShownNotice(this.currentUser.user_name_en);
      const index = latestArr.indexOf(msg.id);
      latestArr.splice(index, 1);
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
    showContent(id) {
      this.link = id;
    },
    handleUnshownNotice(msg) {
      const shownNotice = getShownNotice(this.currentUser.user_name_en);
      shownNotice.push(msg.id);
      setShownNotice(this.currentUser.user_name_en, shownNotice);
      let notifyOptions = {
        position: 'top-right',
        color: 'positive',
        textColor: 'white',
        html: true,
        message: `
          <div style="max-width: 450px;">
            <i 
              aria-hidden="true" 
              class="material-icons q-icon q-notification__icon col-auto float-left q-pl-xs" 
              style="font-size: 30px;position: relative;top: -4px;"
            >
              volume_up
            </i>
            <div style="margin-left: 30px;">
              <div>${msg.desc}</div>
              <div style="max-width: 200px;overflow: hidden;text-overflow: ellipsis;">
                ${msg.content}
              </div>
              <div class="text-right">${msg.create_time}</div>
            </div>
          </div>
        `,
        timeout: 3000
      };
      if (msg.type === '3') {
        this.updateShow = true;
        this.dismiss = this.$q.notify({
          ...notifyOptions,
          timeout: 0,
          actions: [
            {
              label: '刷新',
              color: 'white',
              handler: () => {
                this.removeShownNotice(msg);
                location.reload();
              }
            }
          ]
        });
      }
    }
  },
  async created() {
    await this.fetchNotices({ target: this.currentUser.user_name_en });
    const shownNotice = getShownNotice(this.currentUser.user_name_en);
    if (shownNotice.length > 0) {
      const arr = this.updateNotices.slice(0).reverse();
      arr.forEach(msg => {
        if (shownNotice.indexOf(msg.id) < 0) {
          this.handleUnshownNotice(msg);
        }
      });
    }
    this.ws = new websocket(
      `fuser-${this.currentUser.user_name_en}/${new Date().valueOf()}`,
      this.getWsMsg
    );
  },
  beforeDestroy() {
    this.ws.onclose();
  }
};
</script>

<style lang="stylus" scoped>
.notice-item
  border-radius 2px
.fst-menu-btn
  min-width 0px !important
.fst-menu-hover:hover
  background rgba(0,0,0,0.10)
.notice-list
  max-width 260px
.notify-wrapper
  max-width 400px
.q-notification
  max-width 400px !important
.volume_up
  font-size 30px
  position absolute
  left 10px
.myCard
  min-height 200px
.q-notification__message
  background red
  max-width 400px !important
.text-white
  color #FFF
.text-content
  max-height 50vh
  display flex
  padding 0
.text-indent
  text-indent 2em
.dialog
  width 600px
  height 300px
.title-letter-space
  letter-spacing 10px
.q-dialog__inner > div
  overflow hidden
.list-title
  flex 1
  min-height 300px
  margin-bottom 50px!important
.card-content
  flex 3
  margin-bottom 50px!important
.menu-link
  color white
  background #3185d8
.div-container > div
  overflow-wrap break-word
.span-position
  position absolute
  right 30px
  margin 10px 0 20px 0
.scroll-y
  overflow-x hidden!important
.item-title
  white-space nowrap
  text-overflow ellipsis
  overflow hidden
  margin 0
.text-content > .scroll-y::-webkit-scrollbar
  width 2px
.text-content > .scroll-y::-webkit-scrollbar-track
  background #d4cfcf
.text-content > .scroll-y::-webkit-scrollbar-thumb
  background #027be3
.q-list--bordered
  border-left none
.span-color
  color #027be3
.notice-circle
  width: 6px;
  height: 6px;
  border-radius: 50%;
</style>
