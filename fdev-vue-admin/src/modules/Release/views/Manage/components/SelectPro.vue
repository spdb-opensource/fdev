<template>
  <f-dialog :value="value" title="选择镜像版本" @input="$emit('input', $event)">
    <div v-if="!fdev_config_changed">
      <div v-if="imageTagsVersion.length > 0">
        <f-formitem diaS label="镜像版本">
          <fdev-select
            v-model="pro_image_uri_v"
            :options="imageTagsVersion"
            @input="changeUriVersion"
            :rules="[() => $v.pro_image_uri_v.required || '请选择镜像版本']"
          />
        </f-formitem>
        <f-formitem
          diaS
          label="CASS镜像标签"
          v-if="appDetail && appDetail.deploy_type.includes('CAAS')"
        >
          <fdev-input
            readonly
            :value="pro_image_uri"
            :title="pro_image_uri"
            hint=""
          />
        </f-formitem>
        <f-formitem
          diaS
          label="SCC镜像标签"
          v-if="appDetail && appDetail.deploy_type.includes('SCC')"
        >
          <fdev-input
            readonly
            :value="scc_pro_image_uri"
            :title="scc_pro_image_uri"
          />
        </f-formitem>
      </div>
      <div v-else>
        暂无可选镜像版本，请至投产应用列表页面打包后选择!
        <router-link class="link" :to="`/release/list/${release_name}/applist`"
          >前往打包</router-link
        >
      </div>
    </div>
    <div class="text-red" v-if="fdev_config_changed">
      请应用负责人、投产管理员核对相关变动是否本次投产涉及的内容（dmz网银网段biz业务网段）。
      当前应用投产版本涉及fdev配置文件更新，详细内容差异详见:<a
        class="link"
        target="_blank"
        :href="imageTagResult.compare_url"
        >{{ imageTagResult.compare_url }}</a
      >
    </div>
    <template v-slot:btnSlot>
      <fdev-btn
        @click="submit"
        dialog
        label="确定"
        :loading="loading"
        :disable="!pro_image_uri_v"
        v-if="!fdev_config_changed"
      />
      <fdev-btn
        v-else
        @click="submit"
        label="检查通过"
        dialog
        :loading="loading"
        :disable="pro_image_uri === ''"
      />
    </template>
  </f-dialog>
</template>

<script>
import { mapState } from 'vuex';
import { required } from 'vuelidate/lib/validators';
import { errorNotify } from '@/utils/utils';
export default {
  name: 'SelectPro',
  data() {
    return {
      pro_image_uri_v: '',
      pro_image_uri: '',
      scc_pro_image_uri: ''
    };
  },
  props: {
    value: {
      type: Boolean
    },
    release_name: {
      type: String
    },
    loading: {},
    fdev_config_changed: Boolean,
    appDetail: {
      type: Object
    }
  },
  computed: {
    ...mapState('releaseForm', {
      image_tags: 'image_tags',
      imageTagResult: 'imageTagResult'
    }),
    imageTagsVersion() {
      let res = [];
      let allImageTags = this.image_tags
        ? this.image_tags.images.concat(this.image_tags.sccImages)
        : [];
      allImageTags.forEach(item => {
        res.push(item.split(':')[1]);
      });
      res = Array.from(new Set(res));
      res.sort((oldTag, newTag) => {
        const oldTagNumber = Number(oldTag.match(/\d+/g).join(''));
        const newTagNumber = Number(newTag.match(/\d+/g).join(''));
        return newTagNumber - oldTagNumber;
      });
      return res;
    }
  },
  validations: {
    pro_image_uri_v: {
      required
    }
  },
  methods: {
    // 修改镜像版本，给CASS镜像标签和SCC镜像标签赋值
    changeUriVersion() {
      if (this.pro_image_uri_v) {
        this.pro_image_uri = this.image_tags.images.find(
          item => item.split(':')[1] === this.pro_image_uri_v
        );
        this.scc_pro_image_uri = this.image_tags.sccImages.find(
          item => item.split(':')[1] === this.pro_image_uri_v
        );
      } else {
        this.pro_image_uri = '';
        this.scc_pro_image_uri = '';
      }
    },

    submit() {
      if (this.appDetail.deploy_type.includes('CASS') && !this.pro_image_uri) {
        errorNotify('当前镜像版本没有CAAS镜像标签');
        return;
      }
      if (
        this.appDetail.deploy_type.includes('SCC') &&
        !this.scc_pro_image_uri
      ) {
        errorNotify('当前镜像版本没有SCC镜像标签');
        return;
      }
      this.$emit('click', {
        pro_image_uri:
          this.pro_image_uri && this.appDetail.deploy_type.includes('CAAS')
            ? this.pro_image_uri
            : '',
        pro_scc_image_uri:
          this.scc_pro_image_uri && this.appDetail.deploy_type.includes('SCC')
            ? this.scc_pro_image_uri
            : ''
      });
    }
  },
  watch: {
    value(val) {
      if (val === false) {
        this.pro_image_uri_v = '';
        this.pro_image_uri = '';
        this.scc_pro_image_uri = '';
      }
    }
  }
};
</script>

<style lang="stylus" scoped></style>
