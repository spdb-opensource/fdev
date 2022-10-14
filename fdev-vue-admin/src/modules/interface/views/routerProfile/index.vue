<template>
  <f-block class="wrapper">
    <Page>
      <Loading :visible="loading">
        <div class="text-h6 q-pa-md">路由详情</div>
        <fdev-card-section>
          <div class="row q-col-gutter-x-md q-col-gutter-y-sm q-ml-md q-mb-md">
            <description label="场景名称( stageId )" :col="2" class="q-mb-sm">
              {{ version.name }}
            </description>
            <description label="加载容器( module )" :col="2" class="q-mb-sm">
              {{ version.module }}
            </description>
            <description label="项目id" :col="2" class="q-mb-sm">
              {{ version.projectName }}
            </description>
            <description label="分支" :col="2" class="q-mb-sm">
              {{ version.branch }}
            </description>
            <description label="调用路径" :col="2" class="q-mb-sm">
              {{ version.path }}
            </description>
            <description label="版本号" :col="2" class="q-mb-sm">
              {{ version.ver }}
            </description>
            <description label="路由访问权限" :col="2" class="q-mb-sm">
              <fdev-chip
                v-for="(label, index) in version.authCheck"
                :key="index"
                square
                dense
                color="teal"
                text-color="white"
                class="q-mt-none"
              >
                {{ label }}
              </fdev-chip>
            </description>
            <description label="创建时间" :col="2" class="q-mb-sm">
              {{ version.createTime }}
            </description>
            <description label="更新时间" :col="2" class="q-mb-sm">
              {{ version.updateTime }}
            </description>
          </div>
          <div class>
            <fdev-separator inset />
            <div class>
              <fdev-card-section>
                <div class="row q-ml-xs">
                  <f-formitem
                    diaS
                    label="选择版本"
                    v-if="routesDetailVer.length > 0"
                  >
                    <fdev-select
                      input-debounce="0"
                      ref="version"
                      v-model="version"
                      :options="routesDetailVer"
                      map-options
                      emit-value
                      :option-value="opt => opt"
                      :option-label="
                        opt => opt.branch + '分支-' + opt.ver + '版本'
                      "
                    />
                  </f-formitem>
                </div>
              </fdev-card-section>
            </div>
            <fdev-card-section>
              <div>
                <fdev-chip color="primary" text-color="white" icon="directions">
                  跳转传参(query)
                </fdev-chip>
              </div>
              <fdev-markup-table flat v-if="this.version.query">
                <thead>
                  <tr>
                    <th class="text-left">参数名</th>
                    <th class="text-left">参数类型</th>
                    <th
                      v-for="(item, index) in query"
                      :key="index"
                      class="text-left"
                    >
                      参数{{ index + 1 }}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>query</td>
                    <td>object</td>
                    <td
                      v-for="(key, value) of this.version.query"
                      :key="value.id"
                    >
                      {{ value }}: {{ key }}
                    </td>
                  </tr>
                </tbody>
              </fdev-markup-table>
            </fdev-card-section>
          </div>
          <div class>
            <fdev-card-section>
              <div>
                <fdev-chip color="primary" text-color="white" icon="directions">
                  自定义参数(params)
                </fdev-chip>
              </div>
              <fdev-markup-table flat v-if="this.version.params">
                <thead>
                  <tr>
                    <th class="text-left">参数名</th>
                    <th class="text-left">参数类型</th>
                    <th
                      v-for="(item, index) in params"
                      :key="index"
                      class="text-left"
                    >
                      参数{{ index + 1 }}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>params</td>
                    <td>object</td>
                    <td
                      v-for="(key, value) of this.version.params"
                      :key="value.id"
                    >
                      {{ value }}: {{ key }}
                    </td>
                  </tr>
                </tbody>
              </fdev-markup-table>
            </fdev-card-section>
          </div>

          <div class>
            <fdev-card-section>
              <div>
                <fdev-chip color="primary" text-color="white" icon="directions">
                  额外信息(extra)
                </fdev-chip>
              </div>
              <fdev-markup-table flat v-if="this.version.extra">
                <thead>
                  <tr>
                    <th class="text-left">参数名</th>
                    <th class="text-left">参数类型</th>
                    <th
                      v-for="(item, index) in this.version.extra"
                      :key="index"
                      class="text-left"
                    >
                      参数{{ index + 1 }}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>extra</td>
                    <td>object | Array（object）</td>
                    <td
                      v-for="(key, index) in this.version.extra"
                      :key="index.id"
                      class="td2"
                    >
                      <ul>
                        <li v-for="(value, extraKey) of key" :key="extraKey.id">
                          {{ extraKey }} : {{ value }}
                        </li>
                      </ul>
                    </td>
                  </tr>
                </tbody>
              </fdev-markup-table>
            </fdev-card-section>
          </div>
          <div class="text-center">
            <fdev-btn dialog label="返回" @click="goBack" />
          </div>
        </fdev-card-section>
      </Loading>
    </Page>
  </f-block>
</template>

<script>
import Page from '@/components/Page';
import Loading from '@/components/Loading';
import { mapActions, mapState } from 'vuex';
import Description from '@/components/Description';

export default {
  name: 'routerProfile',
  components: {
    Page,
    Loading,
    Description
  },
  data() {
    return {
      loading: false,
      query: [],
      params: [],
      version: {}
    };
  },
  computed: {
    ...mapState('interfaceForm', ['routesDetailVer'])
  },
  methods: {
    ...mapActions('interfaceForm', ['queryRoutesDetailVer']),
    async init() {
      this.loading = true;
      try {
        //默认展示最新版本,isNew = 1时版本为最新,若只有一条数据时,展示当前数据
        await this.queryRoutesDetailVer({ id: this.$route.params.id });
        if (this.routesDetailVer.length === 1) {
          this.version = this.routesDetailVer[0];
          this.routesDetailVer[0].ver = 0;
        } else {
          this.version = this.routesDetailVer.find(item => {
            if (item.isNew == 1) return item;
          });
        }
        for (let [key, value] of Object.entries(this.version.params)) {
          this.params.push(`${key}: ${value}`);
        }
        for (let [key, value] of Object.entries(this.version.query)) {
          this.query.push(`${key} : ${value}`);
        }
      } finally {
        this.loading = false;
      }
    }
  },
  created() {
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.td2 ul{
  margin-left: -20px;
}
</style>
