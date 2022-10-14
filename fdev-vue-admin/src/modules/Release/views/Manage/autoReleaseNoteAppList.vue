<template>
  <div>
    <fdev-table
      title="应用列表"
      titleIcon="list_s_f"
      :data="tableData"
      :columns="columns"
      class="my-sticky-column-table"
      noExport
      no-select-cols
    >
      <template v-slot:top-right>
        <fdev-btn normal label="新增应用" ficon="add" @click="openAddDialog">
        </fdev-btn>
      </template>

      <template v-slot:body-cell-application_name_en="props">
        <fdev-td auto-width class="text-ellipsis">
          <router-link
            :title="props.row.application_name_en"
            :to="{ path: `/app/list/${props.row.application_id}` }"
            class="link"
          >
            <span>{{ props.row.application_name_en }}</span>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.application_name_en }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
        </fdev-td>
      </template>

      <template v-slot:body-cell-application_name_cn="props">
        <fdev-td auto-width class="text-ellipsis">
          <router-link
            :title="props.row.application_name_cn"
            :to="{ path: `/app/list/${props.row.application_id}` }"
            class="link"
          >
            <span>{{ props.row.application_name_cn }}</span>
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.row.application_name_cn }}
              </fdev-banner>
            </fdev-popup-proxy>
          </router-link>
        </fdev-td>
      </template>
      <template v-slot:body-cell-tag_name="props">
        <fdev-td class="text-ellipsis">
          <span
            v-if="props.row.application_type !== 'docker_scale'"
            :title="props.row.tag_name || '-'"
            >{{ props.row.tag_name || '-' }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ props.value }}
              </fdev-banner>
            </fdev-popup-proxy></span
          >
          <span v-else title="-">-</span>
        </fdev-td>
      </template>
      <template v-slot:body-cell-jobGroup="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.jobGroup | jobGroupFilter"
        >
          <span>{{ props.row.jobGroup | jobGroupFilter }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-application_type="props">
        <fdev-td auto-width class="text-ellipsis">
          <span>{{ props.row.application_type | filterApplicationType }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-dev_managers_info="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.value.map(v => v.user_name_cn).join(',')"
        >
          <span
            v-for="(item, index) in props.row.dev_managers_info"
            :key="index"
          >
            <router-link
              :to="{ path: `/user/list/${item.id}` }"
              class="link"
              v-if="item.id"
            >
              <span>{{ item.user_name_cn }}</span>
            </router-link>
            <span v-else>{{ item.user_name_cn }}</span>
            <span v-if="index !== props.row.dev_managers_info.length - 1"
              >,</span
            >
          </span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.value.map(v => v.user_name_cn).join(',') }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
      </template>

      <template v-slot:body-cell-btn="props">
        <fdev-td>
          <div class="q-gutter-x-sm row no-wrap">
            <fdev-btn label="编辑" flat @click="edit(props.row)" />
            <div class="text-grey-1 sepreator">|</div>
            <fdev-btn label="删除" flat @click="deleteApp(props.row)" />
          </div>
        </fdev-td>
      </template>
    </fdev-table>

    <f-dialog v-model="addDialogModel" right :title="title" persistent>
      <f-formitem
        label-style="width:112px; margin-right:0;"
        bottom-page
        label="类型"
        required
      >
        <fdev-select
          v-model="dialogModel.application_type"
          :options="typeOptions"
          map-options
          emit-value
          option-label="label"
          option-value="value"
          ref="dialogModel.application_type"
          :rules="[val => !!val || '请选择类型']"
        >
        </fdev-select>
      </f-formitem>
      <f-formitem
        label-style="width:112px; margin-right:0;"
        bottom-page
        label="应用"
        required
      >
        <fdev-select
          input-debounce="0"
          option-label="app_name_zh"
          v-model="selectApp"
          ref="dialogModel.selectApp"
          :options="serviceOptions"
          use-input
          :loading="globalLoading['appForm/queryPagination']"
          @filter="appFilter"
          :rules="[val => !!val || '请选择应用']"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.app_name_zh">{{
                  scope.opt.app_name_zh
                }}</fdev-item-label>
                <fdev-item-label caption :title="scope.opt.app_name_en">
                  {{ scope.opt.app_name_en }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <f-formitem
        label="Tag"
        label-style="width:112px; margin-right:0;"
        bottom-page
        required
        v-if="
          dialogModel.application_type !== 'docker_restart' &&
            dialogModel.application_type !== 'docker_scale' &&
            dialogModel.application_type !== 'stop_all'
        "
      >
        <fdev-select
          v-model="dialogModel.tag_name"
          emit-value
          ref="dialogModel.tag_name"
          :options="tagOptions"
          :disable="tagOptions && tagOptions.length === 0"
          :rules="[val => !!val || '请选择Tag']"
        >
        </fdev-select>
      </f-formitem>
      <f-formitem
        label="责任人"
        label-style="width:112px; margin-right:0;"
        bottom-page
        required
      >
        <fdev-select
          v-model="dialogModel.dev_managers_info"
          use-input
          ref="dialogModel.dev_managers_info"
          :options="userOptions"
          @filter="filterUser"
          multiple
          option-label="user_name_cn"
          :rules="[val => !!val || '请选择责任人']"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.user_name_en">{{
                  scope.opt.user_name_en
                }}</fdev-item-label>
                <fdev-item-label caption :title="scope.opt.user_name_cn">
                  {{ scope.opt.user_name_cn }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>

      <f-formitem
        v-if="dialogModel.application_type === 'docker_scale'"
        label-style="width:112px; margin-right:0;"
        label="SHK1扩展至数量"
        bottom-page
        required
      >
        <fdev-input
          v-model="dialogModel.expand_info['SHK1']"
          ref="dialogModel.expand_info1"
          :rules="[
            val => !!val || '请输入SHK1扩展至数量',
            val => validNumber(val) || '请输入0-99之间的数字'
          ]"
        />
      </f-formitem>

      <f-formitem
        label-style="width:112px; margin-right:0;"
        v-if="dialogModel.application_type === 'docker_scale'"
        label="SHK2扩展至数量"
        bottom-page
        required
      >
        <fdev-input
          v-model="dialogModel.expand_info['SHK2']"
          ref="dialogModel.expand_info2                                                                                                                                                             "
          :rules="[
            val => !!val || '请输入SHK2扩展至数量',
            val => validNumber(val) || '请输入0-99之间的数字'
          ]"
        />
      </f-formitem>

      <f-formitem
        label-style="width:112px; margin-right:0;"
        v-if="dialogModel.application_type === 'docker_scale'"
        label="HFK1扩展至数量"
        bottom-page
        required
      >
        <fdev-input
          v-model="dialogModel.expand_info['HFK1']"
          ref="dialogModel.expand_info3"
          :rules="[
            val => !!val || '请输入HFK1扩展至数量',
            val => validNumber(val) || '请输入0-99之间的数字'
          ]"
        />
      </f-formitem>

      <f-formitem
        label-style="width:112px; margin-right:0;"
        v-if="dialogModel.application_type === 'docker_scale'"
        label="HFK2扩展至数量"
        bottom-page
        required
      >
        <fdev-input
          v-model="dialogModel.expand_info['HFK2']"
          ref="dialogModel.expand_info4"
          :rules="[
            val => !!val || '请输入HFK2扩展至数量',
            val => validNumber(val) || '请输入0-99之间的数字'
          ]"
        />
      </f-formitem>

      <template v-slot:btnSlot>
        <fdev-btn dialog label="确定" @click="add" />
      </template>
    </f-dialog>
  </div>
</template>
<script>
import { successNotify, deepClone } from '@/utils/utils';
import { mapActions, mapState, mapGetters } from 'vuex';
import { releaseNoteAppModel, typeNoteOptions } from '../../utils/model';
import { releaseNoteAppListColumn } from '../../utils/constants';
export default {
  name: 'AutoReleaseNoteAppList',
  computed: {
    ...mapGetters('user', {
      userList: 'isLoginUserList'
    }),
    ...mapState('releaseForm', {
      releaseNoteAppList: 'releaseNoteAppList',
      applyList: 'applyList'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('appForm', ['appData']),
    title() {
      return this.isEdit ? '编辑应用' : '新建应用';
    }
  },
  watch: {
    'dialogModel.application_type': {
      immediate: true,
      async handler(val) {
        this.handleAppType(val);
      }
    },
    selectApp: {
      immediate: true,
      async handler(val, oldVal) {
        if (
          val &&
          (val.system.id === this.ownerSystem ||
            this.ownerSystemName.includes('批量'))
        ) {
          this.handleAppSystem(val);
        } else if (
          val &&
          val.system.id !== this.ownerSystem &&
          !this.ownerSystemName.includes('批量')
        ) {
          this.$q
            .dialog({
              title: '风险提示',
              message: `该应用不属于${this.ownerSystemName},确认选择吗?`,
              ok: '确认',
              cancel: '取消'
            })
            .onOk(() => {
              this.handleAppSystem(val);
            })
            .onCancel(() => {
              this.selectApp = '';
            });
        }
      }
    }
  },
  filters: {
    filterApplicationType(val) {
      return typeNoteOptions.find(v => v.value === val).label;
    }
  },
  methods: {
    ...mapActions('releaseForm', {
      addNoteService: 'addNoteService',
      queryNoteService: 'queryNoteService',
      deleteNoteService: 'deleteNoteService',
      updateNoteService: 'updateNoteService',
      queryApply: 'queryApply'
    }),
    ...mapActions('appForm', {
      queryPagination: 'queryPagination'
    }),
    ...mapActions('user', ['fetch']),
    openAddDialog() {
      this.isEdit = false;
      this.clearForm();
      this.dialogModel.dev_managers_info = [this.currentUser];
      this.addDialogModel = true;
    },
    async handleAppType(val) {
      this.selectApp = '';
      if (val) {
        const params = {
          descending: false,
          groupId: '',
          index: 1,
          keyword: '',
          label: [],
          size: 0,
          sortBy: '',
          status: '',
          system: '',
          typeId: '',
          user_id: ''
        };
        await this.queryPagination(params);
        this.appData.forEach(v => {
          v.app_name_en = v.name_en;
          v.app_name_zh = v.name_zh;
          v.application_id = v.id;
          v.app_spdb_managers = v.spdb_managers;
          v.app_dev_managers = v.dev_managers;
        });
        let tempArray = this.appData.filter(v => {
          return !this.tableData.find(
            item =>
              item.application_id === v.application_id &&
              val === item.application_type
          );
        });

        if (this.isEdit && this.dialogModel.application_id) {
          const selfApp = this.appData.find(
            v => v.application_id === this.dialogModel.application_id
          );
          if (selfApp) {
            this.serviceOptions = tempArray.push(selfApp);
          }
        }
        this.serviceOptions = tempArray;
      }
      // else if (
      //   val &&
      //   this.applyList.length === 0 &&
      //   (val !== 'docker_restart' &&
      //     val !== 'docker_scale' &&
      //     val !== 'stop_all')
      // ) {
      //   await this.queryApply(this.release_node_name);
      //   const tempArray = this.applyList.filter(v => {
      //     return !this.tableData.find(
      //       val =>
      //         val.application_id === v.application_id &&
      //         val.application_type === this.dialogModel.application_type
      //     );
      //   });
      //   if (this.isEdit && this.dialogModel.application_id) {
      //     const selfApp = this.applyList.find(
      //       v => v.application_id === this.dialogModel.application_id
      //     );
      //     if (selfApp) {
      //       tempArray.push(selfApp);
      //     }
      //   }
      //   this.serviceOptions = tempArray;
      // }
    },
    validNumber(val) {
      const reg = /^([1-9]\d|\d)$/;
      return reg.test(val);
    },
    handleAppSystem(val) {
      if (val) {
        this.tagOptions = [];
        if (val && val.product_tag && val.product_tag.length > 0) {
          val.product_tag.forEach(element => {
            this.tagOptions.push({ label: element, value: element });
          });
        }
        if (this.isEdit) {
          if (this.tagOptions.length > 0 && this.dialogModel.tag_name) {
            const temp = this.tagOptions.find(
              v => v.value === this.dialogModel.tag_name
            );
            if (!temp) {
              this.dialogModel.tag_name =
                'pro-' + this.release_node_name + '-001';
            }
          } else {
            this.dialogModel.tag_name =
              'pro-' + this.release_node_name + '-001';
          }
        } else {
          if (this.tagOptions.length > 0) {
            this.dialogModel.tag_name = this.tagOptions[0].value;
          } else {
            this.dialogModel.tag_name =
              'pro-' + this.release_node_name + '-001';
          }
        }
      }
    },
    async init() {
      await this.queryNoteService({
        note_id: this.node_id,
        catalog_type: '1'
      });
      this.tableData = this.releaseNoteAppList;
    },
    appFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase().trim();
        const tempArray = deepClone(this.appData);
        this.serviceOptions = tempArray.filter(v => {
          const flag =
            v.app_name_zh.toLowerCase().indexOf(needle) > -1 ||
            v.app_name_en.toLowerCase().indexOf(needle) > -1;
          const withoutFlag = !this.tableData.find(
            val =>
              val.application_id === v.application_id &&
              val.application_type === this.dialogModel.application_type
          );
          return flag && withoutFlag;
        });

        if (this.isEdit && this.dialogModel.application_id) {
          const selfApp = this.appData.find(
            v => v.application_id === this.dialogModel.application_id
          );
          if (selfApp) {
            this.serviceOptions.push(selfApp);
          }
        }
      });
    },
    add() {
      let formKeys = Object.keys(this.$refs).filter(key => {
        return this.$refs[key] && key.indexOf('dialogModel') > -1;
      });
      return Promise.all(
        formKeys.map(ele => this.$refs[ele].validate() || Promise.reject(ele))
      ).then(
        async v => {
          this.dialogModel.application_id = this.selectApp.application_id;
          this.dialogModel.application_name_en = this.selectApp.app_name_en;
          this.dialogModel.application_name_cn = this.selectApp.app_name_zh;

          if (this.isEdit) {
            await this.updateNoteService({
              catalog_type: '1',
              release_node_name: this.release_node_name,
              note_id: this.node_id,
              ...this.dialogModel
            });
            successNotify('编辑成功');
            this.init();
            this.addDialogModel = false;
          } else {
            await this.addNoteService({
              leaseholder: this.leaseholder,
              catalog_type: '1',
              release_node_name: this.release_node_name,
              note_id: this.node_id,
              ...this.dialogModel
            });
            successNotify('添加成功');
            this.init();
            this.addDialogModel = false;
          }
          this.dialogModel = releaseNoteAppModel();
        },
        reason => {
          this.$refs[reason].focus();
        }
      );
    },
    clearForm() {
      this.selectApp = '';
      this.dialogModel = releaseNoteAppModel();
    },
    async edit(val) {
      this.clearForm();
      this.isEdit = true;
      this.addDialogModel = true;
      this.dialogModel = deepClone(val);
      await this.handleAppType(val.application_type);
      const temp = this.serviceOptions.find(
        v => v.application_id === val.application_id
      );
      if (temp) {
        this.selectApp = temp;
      } else {
        this.selectApp = '';
      }
      this.tagOptions = [];
      if (temp && temp.product_tag && temp.product_tag.length > 0) {
        temp.product_tag.forEach(element => {
          this.tagOptions.push({ label: element, value: element });
        });
      }
    },
    deleteApp(val) {
      this.$q
        .dialog({
          title: '删除应用',
          message: `请确认是否删除${val.application_name_cn}应用`,
          cancel: true
        })
        .onOk(async () => {
          await this.deleteNoteService({
            id: val.id
          });
          successNotify('删除成功');
          this.init();
        });
    },
    serviceOptionsFilter(val, update, abort) {
      update(() => {
        this.serviceOptions = this.configModelList.filter(
          model =>
            model.name_cn.indexOf(val) > -1 ||
            model.name_en.toLowerCase().includes(val.toLowerCase())
        );
      });
    },
    filterUser(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.userOptions = this.userList.filter(
          v =>
            v.user_name_cn.toLowerCase().indexOf(needle) > -1 ||
            v.user_name_en.toLowerCase().indexOf(needle) > -1
        );
      });
    }
  },
  data() {
    return {
      leaseholder: '',
      ownerSystem: '',
      ownerSystemName: '',
      release_node_name: '',
      selectApp: '',
      isEdit: false,
      typeOptions: typeNoteOptions,
      addDialogModel: false,
      columns: releaseNoteAppListColumn,
      dialogModel: releaseNoteAppModel(),
      tableData: [],
      userOptions: [],
      serviceOptions: [],
      tagOptions: []
    };
  },
  async created() {
    this.node_id = this.$route.params.id;
    this.release_node_name = this.$q.sessionStorage.getItem(
      'release_node_name'
    );
    this.ownerSystem = this.$q.sessionStorage.getItem('owner_system');
    this.ownerSystemName = this.$q.sessionStorage.getItem('owner_system_name');
    this.leaseholder = this.$q.sessionStorage.getItem('leaseholder');
    await this.init();
    await this.fetch();
    this.userOptions = this.userList;
  }
};
</script>
<style lang="stylus" scoped>
.sepreator
  line-height 32px
.right-gap
  padding-right 20px
.left-gap
  padding-left 20px
.dialog-content
  max-width 836px
  min-width 836px
/deep/ .content
    padding-right 0
</style>
