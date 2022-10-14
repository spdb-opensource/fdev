<template>
  <Loading :visible="loading['releaseForm/queryContactInfo']">
    <fdev-table
      titleIcon="list_s_f"
      :data="tableData"
      :columns="columns"
      title="投产联系人/变更会同人员列表"
      :pagination.sync="pagination"
    >
      <template v-slot:body-cell-release_spdb_manager="props">
        <fdev-td class="text-ellipsis">
          <router-link
            :title="props.value.user_name_cn"
            v-if="props.value.id"
            :to="{ path: `/user/list/${props.value.id}` }"
            class="link"
          >
            {{ props.value.user_name_cn }}
          </router-link>

          <span v-else> {{ props.value.user_name_en || '-' }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-release_spdb_manager_telephone="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.release_spdb_manager.telephone"
        >
          {{ props.row.release_spdb_manager.telephone }}
        </fdev-td>
      </template>

      <template v-slot:body-cell-release_manager="props">
        <fdev-td class="text-ellipsis">
          <router-link
            :title="props.value.user_name_cn"
            v-if="props.value.id"
            :to="{ path: `/user/list/${props.value.id}` }"
            class="link"
          >
            {{ props.value.user_name_cn }}
          </router-link>

          <span :title="props.value.user_name_en" v-else>
            {{ props.value.user_name_en || '-' }}</span
          >
        </fdev-td>
      </template>

      <template v-slot:body-cell-release_manager_telephone="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.release_manager.telephone"
        >
          {{ props.row.release_manager.telephone || '-' }}
        </fdev-td>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import { mapState, mapActions } from 'vuex';
import {
  getBigReleaseContactPagination,
  setBigReleaseContactPagination
} from '../../utils/setting';
import { watchRouteParams } from '../../utils/mixin';
import { userListColumns } from '../../utils/constants';
export default {
  name: 'Contact',
  mixins: [watchRouteParams],
  components: { Loading },
  data() {
    return {
      release_date: '',
      tableData: [],
      columns: userListColumns,
      pagination: getBigReleaseContactPagination()
    };
  },
  computed: {
    ...mapState('global', ['loading']),
    ...mapState('user', ['currentUser']),
    ...mapState('releaseForm', ['contactInfo'])
  },
  watch: {
    pagination(val) {
      setBigReleaseContactPagination({
        rowsPerPage: val.rowsPerPage
      });
    }
  },
  methods: {
    ...mapActions('releaseForm', ['queryContactInfo']),
    async init() {
      await this.queryContactInfo({
        release_date: this.release_date
      });
      this.tableData = this.contactInfo;
    }
  },
  created() {
    this.release_date = this.$route.params.release_date;
    this.init();
  }
};
</script>
