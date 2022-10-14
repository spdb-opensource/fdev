<template>
  <fdev-markup-table>
    <tbody>
      <fdev-tr>
        <fdev-td>{{ liHeader }}</fdev-td>
        <fdev-td>{{ secondLiHeader }}</fdev-td>
      </fdev-tr>
      <fdev-tr v-for="(item, index) in ranking[rankType]" :key="index">
        <fdev-td>{{ item.name.substring(item.name.indexOf(':') + 1) }}</fdev-td>
        <fdev-td v-if="rankType !== 'duplicated_lines_density'">
          {{ rankType.includes('radio') ? item.value + '%' : item.value }}
        </fdev-td>
        <fdev-td v-else>
          {{ item.value / 100 + '%' }}
        </fdev-td>
      </fdev-tr>
    </tbody>
  </fdev-markup-table>
</template>

<script>
import { mapState } from 'vuex';
export default {
  name: 'RankingForm',
  props: {
    rankType: {
      type: String,
      required: true
    },
    liHeader: {
      type: String,
      required: true
    },
    secondLiHeader: {
      type: String,
      required: true
    }
  },
  computed: {
    ...mapState('dashboard', ['ranking'])
  }
};
</script>
