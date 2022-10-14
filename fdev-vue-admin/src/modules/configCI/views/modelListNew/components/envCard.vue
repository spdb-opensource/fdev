<template>
  <f-scrollarea horizontal>
    <div class="row container">
      <div
        v-if="entries.length > 0"
        class="row content scroll-thin no-wrap"
        :class="`${entries.length > 3 ? 'q-pb-md' : ''}`"
      >
        <div v-for="(en, index) in entries" :key="index">
          <div
            class="card-container"
            :class="`${index === entries.length - 1 ? '' : 'q-mr-md'}`"
          >
            <div class="card-header row items-center">
              <div
                class="tip text-center text-white"
                :title="en[0]"
                :style="{ backgroundColor: '#0378EA' }"
              >
                {{ en[0] }}
              </div>
              <div class="icons row" v-if="isManagerRole">
                <div class="row items-center">
                  <f-icon
                    name="copy"
                    class="text-blue-8 cursor-pointer icon-size-md icon-padding"
                    @click="confirmRiskFirst ? () => {} : copyMap(en[0])"
                  />
                  <fdev-tooltip position="top">
                    复制
                  </fdev-tooltip>
                </div>
                <div class="row items-center">
                  <f-icon
                    name="compile"
                    class="text-blue-8 cursor-pointer icon-size-md icon-padding"
                    @click="confirmRiskFirst ? () => {} : editMap(en[0])"
                  />
                  <fdev-tooltip position="top">
                    编辑
                  </fdev-tooltip>
                </div>
                <div class="row items-center">
                  <f-icon
                    name="delete"
                    class="text-blue-8 cursor-pointer icon-size-md icon-padding"
                    @click="confirmRiskFirst ? () => {} : deleteMap(en[0])"
                  />
                  <fdev-tooltip position="top">
                    删除
                  </fdev-tooltip>
                </div>
                <fdev-tooltip v-if="confirmRiskFirst" position="top">
                  请先完成风险确认！
                </fdev-tooltip>
              </div>
            </div>
            <div class="card-body">
              <div
                v-for="property in properties"
                :key="property.nameEn"
                class="row row-info"
              >
                <div class="label">
                  <div
                    :title="`${property.nameEn}\n${property.nameCn}`"
                    class="label-text q-mr-sm"
                  >
                    {{ property.nameCn + ': ' }}
                  </div>
                </div>
                <div class="value text-right">
                  <div v-if="property.type === 'string'">
                    <div
                      class="value-string"
                      :title="property[`value${en[0]}`]"
                    >
                      {{ property[`value${en[0]}`] }}
                    </div>
                  </div>
                  <div
                    v-else
                    style="width:80px"
                    class="text-right"
                    @click="
                      confirmRiskFirst
                        ? () => {}
                        : viewAdvancedProperties(
                            en[0],
                            en[1][property.nameEn],
                            property.nameEn,
                            property.nameCn
                          )
                    "
                  >
                    ......
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div
        class="add-card"
        v-if="entries.length < typeEnvLength && isManagerRole"
      >
        <f-icon
          name="add_s_o"
          class="text-blue-8 cursor-pointer icon-size-ld"
          @click="confirmRiskFirst ? () => {} : addMap()"
        />
        <fdev-tooltip v-if="confirmRiskFirst" position="top">
          请先完成风险确认！
        </fdev-tooltip>
      </div>
    </div>

    <map-add
      v-model="mapAddDialogOpen"
      :type="operateType"
      :entity-info="entityInfo"
      :env="env"
      @refresh="refresh"
    />

    <advanced-props-card
      v-model="addPropsDialogOpen"
      :env="env"
      :arr="arr"
      :pro-name-en="proNameEn"
      :pro-name-cn="proNameCn"
      :entity-info="entityInfo"
    />
  </f-scrollarea>
</template>

<script>
import mapAdd from '../mapAdd';
import advancedPropsCard from './advancedPropsCard';
import { mapActions, mapState } from 'vuex';
import { successNotify } from '@/utils/utils';
import { createEnvTypeColorMap } from '../../../utils/constants';

export default {
  name: 'envCard',
  components: { mapAdd, advancedPropsCard },
  props: {
    title: {
      type: String,
      default: ''
    },
    entityInfo: {
      type: Object,
      default: () => ({})
    },
    envAll: {
      type: Array,
      default: () => []
    },
    confirmRiskFirst: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      isManagerRole: false,
      envObj: null,
      entries: [],
      mapAddDialogOpen: false,
      env: {},
      operateType: 'add',
      addPropsDialogOpen: false,
      arr: [],
      proNameEn: '',
      proNameCn: '',
      envTypeColorMap: createEnvTypeColorMap(),
      deleteEnvName: ''
    };
  },
  computed: {
    ...mapState('user', {
      user: 'currentUser'
    }),
    properties() {
      const { properties } = this.entityInfo;
      properties.forEach((property, index) => {
        this.entries.forEach((en, ind) => {
          const envName = en[0];
          const envValue = en[1];
          const value = envValue[property.nameEn];
          this.$set(property, `value${envName}`, value);
        });
      });
      return properties;
    },
    typeEnvLength() {
      if (!Array.isArray(this.envAll) || this.envAll.length === 0) {
        return 0;
      }
      return this.envAll.filter(env => env.type === this.title).length;
    }
  },
  watch: {
    entityInfo(val) {
      this.creatProperties(this.creatProperties(this.title));
    },
    title(val) {
      this.creatProperties(this.creatProperties(this.title));
    }
  },
  methods: {
    ...mapActions('entityManageConfigForm', ['deleteEntityClass']),
    viewAdvancedProperties(nameEn, arr, proNameEn, proNameCn) {
      this.env = {
        type: this.title,
        nameEn,
        exist: []
      };
      this.proNameEn = proNameEn;
      this.proNameCn = proNameCn;
      this.arr = arr;
      this.addPropsDialogOpen = true;
    },
    addMap() {
      this.env = {
        type: this.title,
        nameEn: '',
        exist: this.entries.map(en => en[0])
        // exist: this.entries
      };
      this.operateType = 'add';
      this.mapAddDialogOpen = true;
    },
    editMap(nameEn) {
      this.env = {
        type: this.title,
        nameEn,
        exist: []
      };
      this.operateType = 'edit';
      this.mapAddDialogOpen = true;
    },
    copyMap(nameEn) {
      this.env = {
        type: this.title,
        nameEn,
        exist: this.entries.map(en => en[0])
      };
      this.operateType = 'copy';
      this.mapAddDialogOpen = true;
    },
    deleteMap(envName) {
      this.deleteEnvName = envName;
      this.$emit('risk-confirm', this.title);
    },
    async deleteMapStep2(operate) {
      if (operate === 'continue') {
        const params = {
          id: this.entityInfo.id,
          envType: this.title,
          envName: this.deleteEnvName
        };
        await this.deleteEntityClass(params);
        successNotify('删除成功！');
        this.$emit('refresh');
      } else {
        this.$q
          .dialog({
            title: '温馨提示',
            message: '您所选择的实体没有应用在使用，可以删除，确认要删除吗？',
            cancel: true
          })
          .onOk(async () => {
            const params = {
              id: this.entityInfo.id,
              envType: this.title,
              envName: this.deleteEnvName
            };
            await this.deleteEntityClass(params);
            successNotify('删除成功！');
            this.$emit('refresh');
          });
      }
    },
    refresh() {
      this.$emit('refresh');
    },
    creatProperties(title) {
      if (Object.keys(this.entityInfo).length > 0) {
        this.envObj = this.entityInfo.propertiesValue[this.title];
        if (this.envObj) {
          this.entries = Object.entries(this.envObj);
        }
      }
      if (!this.envObj) {
        this.entries = [];
      }
    }
  },
  async created() {
    let managerUser = this.user.role.find(item => {
      return item.label === '环境配置管理员';
    });
    if (managerUser) {
      this.isManagerRole = true;
    }
    this.creatProperties(this.title);
  }
};
</script>
<style lang="stylus" scoped>
@import '../../../styles/common.styl';

.title
  width 100%
  height 54px
.container
  min-height 32px
  .content
    position relative
    margin-right 48px
    .card-container
      width 212px
      border 1px solid #0378EA
      border-radius 6px
      .card-header
        height 42px
        border-bottom 1px solid #0378EA
        position relative
        .tip
          font-size 12px
          width 100px
          height 22px
          line-height 22px
          border-radius 0 11px 11px 0
          word-wrap break-word
          white-space nowrap
          overflow hidden
          text-overflow ellipsis
        .icons
          position absolute
          right 10px
      .card-body
        min-height 140px
        padding 16px
        .row-info
          font-size 12px
          margin-bottom 6px
          .label
            color #333333
            .label-text
              white-space nowrap
              width 83px
              overflow hidden
              text-overflow ellipsis
          .value
            color #666666
            .value-string
              width 80px
              white-space nowrap
              overflow hidden
              text-overflow ellipsis
  .add-card
    position absolute
    top 0
    right 0
</style>
