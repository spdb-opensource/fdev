<template>
  <f-dialog :value="value" title="选择tag" @input="$emit('input', $event)">
    <f-formitem
      dias
      label="镜像标签"
      v-if="packageTags.packages && packageTags.packages.length > 0"
    >
      <fdev-select
        v-model="pro_image_uri"
        :options="packageTags.packages"
        :rules="[() => !$v.pro_image_uri.$error || '请选择镜像标签']"
      />
    </f-formitem>
    <div v-else>
      暂无可选tag，请至投产应用列表页面打包后选择!
      <router-link class="link" :to="`/release/list/${release_name}/applist`"
        >前往打包</router-link
      >
    </div>
    <template v-slot:btnSlot>
      <fdev-btn
        dialog
        @click="submit"
        label="确定"
        :loading="loading"
        :disable="pro_image_uri === ''"
      />
    </template>
  </f-dialog>
</template>

<script>
import { mapState } from 'vuex';
import { required } from 'vuelidate/lib/validators';
export default {
  name: 'SelectTag',
  data() {
    return {
      pro_image_uri: ''
    };
  },
  props: {
    value: {
      type: Boolean
    },
    release_name: {
      type: String
    },
    loading: {}
  },
  computed: {
    ...mapState('releaseForm', {
      packageTags: 'packageTags'
    })
  },
  validations: {
    pro_image_uri: {
      required
    }
  },
  methods: {
    submit() {
      this.$emit('click', this.pro_image_uri);
    }
  },
  watch: {
    value(val) {
      if (val === false) {
        this.pro_image_uri = '';
      }
    }
  }
};
</script>

<style lang="stylus" scoped></style>
