<template>
  <div>
    <form @submit.prevent="handleAuthorize" class="form">
      <Loading :visible="loading">
        <fdev-card class="q-pa-sm">
          <fdev-item>
            <fdev-item-section avatar top>
              <fdev-avatar>
                <img :src="currentUser.avatar" />
              </fdev-avatar>
            </fdev-item-section>
            <fdev-item-section>
              <fdev-item-label>{{ currentUser.name }}</fdev-item-label>
              <fdev-item-label caption>{{ currentUser.email }}</fdev-item-label>
            </fdev-item-section>
            <fdev-item-section side top>
              <fdev-icon name="new_releases" color="warning" />
            </fdev-item-section>
          </fdev-item>
          <fdev-item>
            <fdev-item-section>
              <fdev-item-label caption>
                <a class="link" :href="preAuth.host" target="_blank">{{
                  preAuth.name
                }}</a>
                将获得以下权限：
              </fdev-item-label>
              <fdev-item-label caption class="">
                <fdev-icon
                  name="mdi-set mdi-checkbox-marked-outline"
                  class="q-mr-xs"
                />获得你的公开信息（昵称，头像等）
              </fdev-item-label>
            </fdev-item-section>
          </fdev-item>
          <fdev-item>
            <fdev-item-section>
              <fdev-btn
                type="submit"
                color="primary"
                class="full-width"
                label="授权"
              />
            </fdev-item-section>
          </fdev-item>
        </fdev-card>
      </Loading>
    </form>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import Loading from '@/components/Loading';
import { queryOAuth, authorize } from '@/services/api';
import { resolveResponseError } from '@/utils/utils';

export default {
  name: 'OAuth',
  components: { Loading },
  data() {
    return {
      loading: true,
      preAuth: {}
    };
  },
  computed: {
    ...mapState('user', {
      currentUser: 'currentUser'
    })
  },
  methods: {
    ...mapActions('user', ['fetchCurrent']),
    async handleAuthorize() {
      this.loading = true;
      const result = await resolveResponseError(() => authorize(this.preAuth));
      window.location.href = result.redirectUrl;
      this.loading = false;
    }
  },
  async created() {
    const oAuthId = this.$route.query.OAuthId;
    let preAuth = await resolveResponseError(() => queryOAuth({ id: oAuthId }));
    this.preAuth = preAuth[0];
    await this.fetchCurrent();
    this.loading = false;
  },
  mounted() {}
};
</script>

<style lang="stylus" scoped>
@import '~#/css/variables.styl';

.form
  margin: 0 auto;
  width: 368px;

@media screen and (max-width: $sizes.sm)
  .form
    width: 95%;
</style>
