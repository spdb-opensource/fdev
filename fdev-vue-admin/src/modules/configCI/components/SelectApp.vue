<template>
  <div v-if="apps">
    <fdev-select
      style="width: 200px"
      :readonly="readonly"
      :value="value"
      :disable="disabled"
      @input="selectedApp"
      option-label="nameEn"
      :options="apps"
    >
      <template v-slot:option="scope">
        <fdev-item
          v-bind="scope.itemProps"
          class="column"
          v-on="scope.itemEvents"
        >
          <fdev-item-section avatar>
            <fdev-item-label>{{ scope.opt.nameEn }}</fdev-item-label>
          </fdev-item-section>
          <fdev-item-section>
            <fdev-item-label caption>{{ scope.opt.nameCn }}</fdev-item-label>
          </fdev-item-section>
        </fdev-item>
      </template>
    </fdev-select>
  </div>
</template>
<script>
import { queryDutyAppBaseInfo } from '../services/method';
export default {
  name: 'SelectApp',
  data() {
    return {
      apps: []
    };
  },
  props: {
    value: Object,
    readonly: Boolean,
    disabled: Boolean
  },

  methods: {
    selectedApp(evt) {
      this.$emit('input', evt);
    }
  },
  async mounted() {
    let apps = (await queryDutyAppBaseInfo()) || {};
    this.apps = apps.data.map(x => {
      return {
        projectId: x.id,
        nameEn: x.name_en,
        nameCn: x.name_zh,
        gitlabProjectId: x.gitlab_project_id
      };
    });
  }
};
</script>

<style lang="stylus" scoped>
.select-app
  margin-top 90px
</style>
