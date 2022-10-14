<template>
  <Loading :visible="loading">
    <fdev-table
      :data="foreignInterfaceList"
      :columns="interfaceColumns"
      class="bg-white"
      flat
    >
      <template v-slot:body-cell-transId="props">
        <fdev-td>
          <router-link
            v-if="props.row.transId"
            :to="{
              path: `/interfaceProfile/${props.row.id}`,
              query: { interfaceType: props.row.interfaceType }
            }"
            class="link"
          >
            <span>{{ props.row.transId }}</span>
          </router-link>
          <span v-else>{{ props.row.transId }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-description="props">
        <fdev-td auto-width class="td-width">
          <fdev-tooltip
            anchor="top middle"
            self="bottom middle"
            :offest="[0, 0]"
            v-if="props.row.description"
          >
            <span>{{ props.row.description }}</span>
          </fdev-tooltip>
          <span>{{ props.row.description }}</span>
        </fdev-td>
      </template>
    </fdev-table>
  </Loading>
</template>

<script>
import { createInterfaceModel } from '../../utils/constants';
import { mapActions, mapState } from 'vuex';
import Loading from '@/components/Loading';
import { interfaceColumns } from '@/modules/App/utils/constants';

export default {
  name: 'ForeignInterfaceList',
  components: { Loading },
  data() {
    return {
      interfaceModel: createInterfaceModel(),
      loading: false,
      interfaceColumns: interfaceColumns()
    };
  },
  computed: {
    ...mapState('interfaceForm', ['foreignInterfaceList'])
  },
  methods: {
    ...mapActions('interfaceForm', ['queryInterfacesList'])
  },
  async created() {
    this.id = this.$route.params.id;
    this.loading = true;
    await this.queryInterfacesList({ id: this.id });
    const baseColumns = interfaceColumns();
    let Type = this.foreignInterfaceList.find(
      item => item.interfaceType === 'SOAP'
    );
    if (Type) {
      baseColumns.splice(2, 0, {
        name: 'esbServiceId',
        label: '服务ID',
        field: 'esbServiceId',
        align: 'left'
      });
      baseColumns.splice(3, 0, {
        name: 'esbOperationId',
        label: '操作ID',
        field: 'esbOperationId',
        align: 'left'
      });
    }
    this.interfaceColumns = baseColumns;
    this.loading = false;
  }
};
</script>

<style lang="stylus" scoped>
.td-width
	box-sizing border-box
	max-width 130px
	overflow hidden
	text-overflow ellipsis
</style>
