<template>
  <fdev-card class="flex row no-wrap" square flat>
    <div>
      <span class="title">
        零售组实施中需求总量
      </span>
      <PlatePie
        class="pie"
        v-if="dataThr.length > 0"
        :ids="ids"
        :names="names"
        :partdemandAmt="partdemandAmt"
        @getGroup="getGroup"
      />
    </div>
    <RequireDetails
      v-if="dataThr.length > 0"
      :group="group"
      :dataDetail="propData"
      :style="{ 'margin-left': '80px' }"
    />
  </fdev-card>
</template>
<script>
import PlatePie from './PlatePie';
import RequireDetails from './RequireDetails';
export default {
  name: 'LineThree',
  components: {
    PlatePie,
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
    dataThr: {
      type: Array
    },
    ids: {
      type: Array
    },
    names: {
      type: Array
    },
    partdemandAmt: {
      type: Array
    }
  },
  watch: {
    dataThr: {
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
        this.propData = this.dataThr.filter(item => {
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
