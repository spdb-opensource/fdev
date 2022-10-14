<template>
  <fdev-card class="flex row no-wrap" square flat>
    <div>
      <span class="title">
        各组实施中需求总量
      </span>
      <FourGroups
        v-if="dataTwo.length > 0"
        class="pie"
        :ids="ids"
        :names="names"
        :groupdemandAmt="groupdemandAmt"
        @getGroup="getGroup"
      />
    </div>
    <!-- <RequireDetails :group="group" :style="{ 'margin-left': '91px' }" /> -->
    <RequireDetails
      v-if="dataTwo.length > 0"
      :group="group"
      :dataDetail="propData"
      :style="{ 'margin-left': '80px' }"
    />
  </fdev-card>
</template>
<script>
import FourGroups from './FourGroups';
import RequireDetails from './RequireDetails';
export default {
  name: 'LineTwo',
  components: {
    FourGroups,
    RequireDetails
  },
  data() {
    return {
      propData: {},
      groupId: this.ids[0],
      group: this.names[0]
    };
  },
  props: {
    dataTwo: {
      type: Array
    },
    ids: {
      type: Array
    },
    names: {
      type: Array
    },
    groupdemandAmt: {
      type: Array
    }
  },
  watch: {
    dataTwo: {
      handler(newV) {
        if (newV.length > 0) {
          this.propData = newV.filter(item => {
            return item.groupId == this.ids[0];
          })[0];
        }
      },
      immediate: true,
      deep: true
    },
    groupId: {
      handler(newVal) {
        this.propData = this.dataTwo.filter(item => {
          return item.groupId == newVal;
        })[0];
      }
    }
  },
  methods: {
    getGroup(data) {
      this.group = data.name;
      this.groupId = data.id;
    }
  }
};
</script>
<style scoped lang="stylus">
.title
  margin-left 38px
  font-family PingFangSC
  font-weight 500
  font-size 24px
  color #394853
  letter-spacing 0
.pie
  margin-top 107px
  margin-left 19px
  margin-bottom 38px
</style>
