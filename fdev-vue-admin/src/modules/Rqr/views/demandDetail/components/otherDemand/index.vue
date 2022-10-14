<template>
  <f-block block>
    <other-dm-task
      v-if="params && params.demand_type === 'daily'"
      :demandModel="params"
      :isDemandManager="isDemandManager"
      :isDemandLeader="isDemandLeader()"
      :isIncludeCurrentUser="isIncludeCurrentUser()"
    ></other-dm-task>
    <div v-else>
      <dm-unit
        v-if="params.oa_contact_name"
        :demandModel="params"
        :isDemandManager="isDemandManager"
        :isDemandLeader="isDemandLeader()"
        :isIncludeCurrentUser="isIncludeCurrentUser()"
      ></dm-unit>
    </div>
  </f-block>
</template>
<script>
import { mapState, mapGetters } from 'vuex';
import DmUnit from '@/modules/Rqr/Components/UnitList';
import OtherDmTask from './otherDm';
export default {
  name: 'otherDemand',
  components: { DmUnit, OtherDmTask },
  props: {
    params: {
      type: Object
    }
  },
  computed: {
    ...mapState('user', ['currentUser']),
    ...mapGetters('user', ['isDemandManager'])
  },
  data() {
    return {
      demandType: null // 需求类型 tech--科技需求；business--业务需求；daily--日常需求；
    };
  },
  methods: {
    isIncludeCurrentUser() {
      if (
        this.params.relate_part_detail &&
        Array.isArray(this.params.relate_part_detail)
      ) {
        return this.params.relate_part_detail.some(part => {
          return (
            part.assess_user &&
            part.assess_user.some(id => {
              return id === this.currentUser.id;
            })
          );
        });
      }
    },
    //判断用户集合中是否包含当前用户，当前用户是否是需求牵头人
    isDemandLeader() {
      if (
        this.params.demand_leader_all &&
        Array.isArray(this.params.demand_leader_all)
      ) {
        return this.params.demand_leader_all.some(user => {
          return user.id === this.currentUser.id;
        });
      }
    }
  }
};
</script>
<style lang="stylus" scoped></style>
