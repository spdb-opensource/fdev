<template>
  <div>
    <tabs-block :data="tabData" :default-tab="tab" @change="changeTab">
      <fdev-tab-panel name="UATDefect">
        <Loading :visible="loading">
          <UATDefect
            :defectList="UATdefectList"
            :isLoginUserList="isLoginUserList"
            @update-status="init"
            :userNameLoading="globalLoading['user/updateAssignUser']"
            :statusLoading="globalLoading['user/updateFdevMantis']"
          />
        </Loading>
      </fdev-tab-panel>

      <fdev-tab-panel name="SITDefect">
        <Loading :visible="loading">
          <Defect
            :defectList="defectList"
            :isLoginUserList="isLoginUserList"
            @update-status="updateStatus"
            @update-sourcestatus="updateSourceStatus"
            @assignUser="assignUser"
            @init="init"
            :userNameLoading="globalLoading['user/updateAssignUser']"
            :statusLoading="globalLoading['user/updateFdevMantis']"
            @changeShow="changeShow"
          />
        </Loading>
      </fdev-tab-panel>
    </tabs-block>
  </div>
</template>

<script>
import { mapState, mapActions, mapGetters } from 'vuex';
import TabsBlock from '@/components/TabsBlock';
import Loading from '@/components/Loading';
import Defect from '@/components/Defect';
import UATDefect from '@/components/Defect/UATDefect';
import { successNotify } from '@/utils/utils';

export default {
  name: 'myDefect',
  components: { Loading, Defect, UATDefect, TabsBlock },
  data() {
    return {
      loading: false,
      includeCloseFlag: '1',
      tab: 'UATDefect',
      tabData: [
        {
          name: 'UATDefect',
          label: 'UAT测试缺陷'
        },
        {
          name: 'SITDefect',
          label: 'SIT测试缺陷'
        }
      ]
    };
  },
  computed: {
    ...mapState('user', {
      currentUser: 'currentUser',
      defectList: 'defectList'
    }),
    ...mapState('jobForm', ['UATdefectList']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapGetters('user', ['isLoginUserList']),
    ...mapState('userActionSaveHomePage/myDefect', ['termsApp', 'UATtermsApp'])
  },
  methods: {
    ...mapActions('jobForm', ['queryJiraIssues']),
    ...mapActions('user', {
      queryFuserMantis: 'queryFuserMantis',
      updateFdevMantis: 'updateFdevMantis',
      updateAssignUser: 'updateAssignUser',
      fetch: 'fetch'
    }),
    async updateStatus(defect) {
      await this.updateFdevMantis(defect);
      successNotify('修改成功');
      this.init();
    },
    async updateSourceStatus(defect) {
      await this.updateFdevMantis(defect);
      successNotify('修改缺陷来源成功');
      this.init();
    },
    async assignUser(defect) {
      await this.updateAssignUser(defect);
      successNotify('分配成功');
      this.init();
    },
    changeTab(tab) {
      this.tab = tab;
    },
    async init() {
      this.loading = true;
      if (this.tab == 'UATDefect') {
        await this.queryJiraIssues({
          jql: `assignee=${this.currentUser.user_name_en}`
        });
      } else if (this.tab == 'SITDefect') {
        let params = {
          userList: [
            {
              user_name_cn: this.currentUser.user_name_cn,
              user_name_en: this.currentUser.user_name_en
            }
          ],
          includeCloseFlag: this.includeCloseFlag
        };
        await this.queryFuserMantis(params);
      }
      this.loading = false;
    },
    async changeShow() {
      this.includeCloseFlag = this.termsApp.showDownFile ? '0' : '1';
      await this.init();
    },
    async UATchangeShow() {
      this.includeCloseFlag = this.UATtermsApp.showDownFile ? '0' : '1';
      await this.init();
    }
  },
  async created() {
    await this.UATchangeShow();
    this.fetch();
  },
  watch: {
    tab(val) {
      if (val == 'UATDefect') {
        this.init();
      } else if (val == 'SITDefect') {
        this.changeShow();
      }
    }
  }
};
</script>

<style scoped lang="stylus">
.text-grey-7 {
  padding: 20px 50px;
}
>>> .dialog-fdev-btn
  min-width:74px
  max-width:74px
  width:74px;
  height:36px
  float:right
  margin-top:20px
>>> label.q-field--with-bottom
  padding-bottom:0px
</style>
