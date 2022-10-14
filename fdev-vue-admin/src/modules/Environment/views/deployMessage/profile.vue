<template>
  <f-block>
    <Loading :visible="loading" class="bg-white q-pa-md">
      <StepFour ref="StepFour" isAppProfile />
    </Loading>
  </f-block>
</template>

<script>
import { mapActions } from 'vuex';
import { columns } from '../../utils/constants';
import Loading from '@/components/Loading';
import StepFour from './StepFour';
export default {
  name: 'DeployMessageProfile',
  components: { Loading, StepFour },
  data() {
    return {
      loading: false,
      appId: '',
      tableData: [],
      columns: columns.slice(0, 2),
      pagination: {
        rowsPerPage: 0,
        page: 1
      }
    };
  },
  methods: {
    ...mapActions('environmentForm', ['queryDeploy']),
    back() {
      window.history.back(-1);
    },
    async init() {
      this.loading = true;
      await this.queryDeploy({
        app_id: this.appId
      });
      this.$refs.StepFour.appProfileinit();
      this.loading = false;
    }
  },
  created() {
    this.appId = this.$route.params.appId;
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.font
  font-weight: 700
.desc
  word-break: break-all;
  font-size: 14px;
  color: #616161;
  margin-left 16px
</style>
