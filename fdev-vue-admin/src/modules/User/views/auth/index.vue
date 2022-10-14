<template>
  <f-block>
    <fdev-card-section>
      <fdev-tabs v-model="auth" active-color="primary">
        <fdev-tab
          v-for="each in auths"
          :key="each.id"
          :label="each.label"
          :name="each.value"
        />
      </fdev-tabs>
    </fdev-card-section>

    <fdev-card-section class="row q-col-gutter-md">
      <div
        class="col-xs-12 col-sm-6 col-md-4 col-lg-3"
        v-for="item in formattedAuths"
        :key="item.name"
      >
        <fdev-card v-show="!isMaster || 'auth' != item.name">
          <fdev-list>
            <fdev-item-label header>{{ item.label }}</fdev-item-label>
            <fdev-item
              tag="label"
              v-for="each in item.auth"
              :key="each.label"
              v-ripple
            >
              <fdev-item-section>
                <fdev-item-label>{{ each.label }}</fdev-item-label>
              </fdev-item-section>
              <fdev-item-section side>
                <fdev-toggle v-model="each.isTicked" @input="updateAuth" />
              </fdev-item-section>
            </fdev-item>
          </fdev-list>
        </fdev-card>
      </div>
    </fdev-card-section>
  </f-block>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import authSet from '@/modules/User/utils/authSet';

export default {
  name: 'Auth',
  data() {
    return {
      auth: '',
      auth_admin_id: '',
      formattedAuths: [],
      authIcons: ['chili-hot', 'chili-medium', 'chili-mild'],
      isMaster: false
    };
  },
  watch: {
    auth(val) {
      this.isMaster = false;
      if (val === this.auth_admin_id) {
        this.isMaster = true;
      }
      let permissions = this.auths.find(auth => auth.value === val).permissions;
      this.formattedAuths = authSet.map(each => {
        const auth = each.auth.map(item => ({
          ...item,
          isTicked: permissions.indexOf(item.value) >= 0
        }));
        return {
          ...each,
          auth
        };
      });
    }
  },
  computed: {
    ...mapState('authorized', {
      auths: 'list'
    })
  },
  methods: {
    ...mapActions('authorized', {
      fetch: 'fetch',
      update: 'update'
    }),
    async updateAuth(val) {
      let auth = this.auths.find(auth => auth.id === this.auth);
      auth.permissions = this.formattedAuths
        .reduce(
          (pre, next) =>
            pre.concat(
              next.auth.map(auth => (auth.isTicked ? auth.value : ''))
            ),
          []
        )
        .filter(each => !!each);
      if (auth.name_en === 'admin') {
        if (auth.permissions.indexOf('/api/permission/update') === -1) {
          auth.permissions.push('/api/permission/update');
        }
      }
      await this.update(auth);
    }
  },
  async created() {
    await this.fetch();
    this.auth = this.auths[0].value;
    this.auth_admin_id = this.auths.find(each => each.name_en === 'admin').id;
  },
  mounted() {}
};
</script>

<style lang="stylus" scoped></style>
