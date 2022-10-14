<template>
  <div class="updateList">
    <div v-if="changeApllication.length > 0">
      <f-formitem
        class="q-mb-md"
        label="变更类型"
        v-if="
          changeApllication.length > 0 &&
            changesDetail.image_deliver_type === '1'
        "
      >
        <fdev-select
          v-model="type"
          :options="typeOptions"
          options-dense
          @input="appFilter"
        />
      </f-formitem>
      <div class="q-mt-md" v-else />
      <div class="row q-col-gutter-md">
        <div
          class="col-md-4 col-sm-6 col-xs-12"
          v-for="(item, index) in tableData"
          :key="index"
        >
          <fdev-card flat square class="shadow-1">
            <span class="delete">
              <fdev-btn
                round
                color="red"
                ficon="delete"
                @click="deleted(item)"
                flat
                v-if="
                  compareTime() &&
                    (role(item) ||
                      changesDetail.can_operation ||
                      isKaDianManager)
                "
                :disable="item.esf_flag === '1'"
              />
              <fdev-tooltip v-if="item.esf_flag === '1'">
                请先删除该应用esf注册信息
              </fdev-tooltip>
            </span>
            <fdev-card-section class="row no-wrap items-start">
              <div class="col">
                <router-link :to="`/app/list/${item.application_id}`">
                  <span class="text-h6 text-title">{{ item.app_name_en }}</span>
                </router-link>
                <span
                  class="text-h6 text-orange-6 text-title cursor-pointer"
                  v-if="item.isAppRisk === '1'"
                  @click="handleSingleTip(item)"
                  >（含风险）</span
                >
                <!-- <div
                  v-if="
                    item.network.includes('dmz') && item.network.includes('biz')
                  "
                >
                  该应用需双网段部署，FDEV默认出网银网段介质，业务网段介质请手动准备。
                </div> -->
              </div>
            </fdev-card-section>
            <fdev-card-section>
              <div class="text-grey-9">
                <div class="q-mb-sm row">
                  <div class="col-md-4 col-sm-4">
                    应用名称
                  </div>
                  <div
                    class="col-md-8 col-sm-8 text-grey-8 ellipsis"
                    :title="item.app_name_zh"
                  >
                    <router-link
                      :to="`/app/list/${item.application_id}`"
                      class="link"
                    >
                      {{ item.app_name_zh }}
                    </router-link>
                  </div>
                </div>
                <div class="q-mb-sm row">
                  <div class="col-md-4 col-sm-4">
                    行内项目负责人
                  </div>
                  <div
                    class="col-md-8 col-sm-8 text-grey-8"
                    v-if="item.app_spdb_managers"
                  >
                    <router-link
                      class="link"
                      :to="`/user/list/${person.user_id}`"
                      v-for="person in item.app_spdb_managers"
                      :key="person.user_id"
                    >
                      {{ person.user_name_cn }}
                      <span>,</span>
                    </router-link>
                  </div>
                </div>
                <div class="q-mb-sm row">
                  <div class="col-md-4 col-sm-4">
                    应用负责人
                  </div>
                  <div
                    class="col-md-8 col-sm-8 text-grey-8"
                    v-if="item.app_dev_managers"
                  >
                    <router-link
                      class="link"
                      :to="`/user/list/${person.user_id}`"
                      v-for="person in item.app_dev_managers"
                      :key="person.user_id"
                    >
                      {{ person.user_name_cn }}
                      <span>,</span>
                    </router-link>
                  </div>
                </div>
                <div
                  class="q-mb-sm row"
                  v-if="changesDetail.image_deliver_type === '1'"
                >
                  <div class="col-md-4 col-sm-4">
                    变更类型
                  </div>
                  <div class="col-md-8 col-sm-8 text-grey-8">
                    {{ item.release_type | typeOptionsObj }}
                  </div>
                </div>
                <div
                  class="q-mb-sm row items-center"
                  v-if="changesDetail.image_deliver_type === '1'"
                >
                  <div class="col-md-4 col-sm-4">
                    部署平台
                  </div>
                  <div class="text-grey-8 row items-center">
                    <fdev-checkbox
                      v-model="item.deploy_type"
                      class="q-mr-md"
                      style="height:21px"
                      val="CAAS"
                      label="CAAS"
                      :disable="
                        !compareTime() ||
                          !(
                            role(item) ||
                            changesDetail.can_operation ||
                            isKaDianManager
                          ) ||
                          (item.esf_flag === '1' &&
                            item.esf_platform.includes('CAAS')) ||
                          (item.caas_status !== '1' &&
                            !item.deploy_type.includes('CAAS'))
                      "
                      @input="updateDeployType(item.deploy_type, item)"
                    >
                      <fdev-tooltip
                        v-if="
                          !compareTime() ||
                            !(
                              role(item) ||
                              changesDetail.can_operation ||
                              isKaDianManager
                            ) ||
                            (item.esf_flag === '1' &&
                              item.esf_platform.includes('CAAS')) ||
                            (item.caas_status !== '1' &&
                              !item.deploy_type.includes('CAAS'))
                        "
                      >
                        <span v-if="!compareTime()">
                          当前变更已过期
                        </span>
                        <span
                          v-if="
                            compareTime() &&
                              !(
                                role(item) ||
                                changesDetail.can_operation ||
                                isKaDianManager
                              )
                          "
                        >
                          当前用户无权执行此操作
                        </span>
                        <span
                          v-if="
                            compareTime() &&
                              (role(item) ||
                                changesDetail.can_operation ||
                                isKaDianManager) &&
                              item.esf_flag === '1' &&
                              item.esf_platform.includes('CAAS')
                          "
                        >
                          当前应用在esf介质目录已添加CAAS平台
                        </span>
                        <span
                          v-if="
                            compareTime() &&
                              (role(item) ||
                                changesDetail.can_operation ||
                                isKaDianManager) &&
                              !(
                                item.esf_flag === '1' &&
                                item.esf_platform.includes('CAAS')
                              ) &&
                              item.caas_status !== '1' &&
                              !item.deploy_type.includes('CAAS')
                          "
                        >
                          该应用没有CAAS平台部署信息
                        </span>
                      </fdev-tooltip>
                    </fdev-checkbox>
                    <fdev-checkbox
                      v-model="item.deploy_type"
                      val="SCC"
                      label="SCC"
                      style="height:21px"
                      :disable="
                        !compareTime() ||
                          !(
                            role(item) ||
                            changesDetail.can_operation ||
                            isKaDianManager
                          ) ||
                          changesDetail.scc_prod !== '1' ||
                          (item.esf_flag === '1' &&
                            item.esf_platform.includes('SCC')) ||
                          (item.scc_status !== '1' &&
                            !item.deploy_type.includes('SCC')) ||
                          item.app_name_en.startsWith('mspmk-cli')
                      "
                      @input="updateDeployType(item.deploy_type, item)"
                    >
                      <fdev-tooltip
                        v-if="
                          !compareTime() ||
                            !(
                              role(item) ||
                              changesDetail.can_operation ||
                              isKaDianManager
                            ) ||
                            changesDetail.scc_prod !== '1' ||
                            (item.esf_flag === '1' &&
                              item.esf_platform.includes('SCC')) ||
                            (item.scc_status !== '1' &&
                              !item.deploy_type.includes('SCC')) ||
                            item.app_name_en.startsWith('mspmk-cli')
                        "
                      >
                        <span v-if="!compareTime()">
                          当前变更已过期
                        </span>
                        <span
                          v-if="
                            compareTime() &&
                              !(
                                role(item) ||
                                changesDetail.can_operation ||
                                isKaDianManager
                              )
                          "
                        >
                          当前用户无权执行此操作
                        </span>
                        <span
                          v-if="
                            compareTime() &&
                              (role(item) ||
                                changesDetail.can_operation ||
                                isKaDianManager) &&
                              changesDetail.scc_prod !== '1'
                          "
                        >
                          新建变更时未选择SCC新版excel模板
                        </span>
                        <span
                          v-if="
                            compareTime() &&
                              (role(item) ||
                                changesDetail.can_operation ||
                                isKaDianManager) &&
                              changesDetail.scc_prod === '1' &&
                              (item.esf_flag === '1' &&
                                item.esf_platform.includes('SCC'))
                          "
                        >
                          当前应用在esf介质目录已添加SCC平台
                        </span>
                        <span
                          v-if="
                            compareTime() &&
                              (role(item) ||
                                changesDetail.can_operation ||
                                isKaDianManager) &&
                              changesDetail.scc_prod === '1' &&
                              !(
                                item.esf_flag === '1' &&
                                item.esf_platform.includes('SCC')
                              ) &&
                              (item.scc_status !== '1' &&
                                !item.deploy_type.includes('SCC'))
                          "
                        >
                          该应用没有SCC平台部署信息
                        </span>
                        <span
                          v-if="
                            compareTime() &&
                              (role(item) ||
                                changesDetail.can_operation ||
                                isKaDianManager) &&
                              changesDetail.scc_prod === '1' &&
                              !(
                                item.esf_flag === '1' &&
                                item.esf_platform.includes('SCC')
                              ) &&
                              !(
                                item.scc_status !== '1' &&
                                !item.deploy_type.includes('SCC')
                              ) &&
                              item.app_name_en.startsWith('mspmk-cli')
                          "
                        >
                          前端应用不能部署SCC平台
                        </span>
                      </fdev-tooltip>
                    </fdev-checkbox>
                    <span
                      class="text-red q-ml-md"
                      v-if="item.deploy_type.length === 0"
                    >
                      请选择
                    </span>
                  </div>
                </div>
                <!-- 自动化发布 -->
                <div
                  class="q-mb-sm row"
                  v-if="
                    changesDetail.image_deliver_type === '1' &&
                      item.deploy_type.includes('CAAS')
                  "
                >
                  <div class="col-md-4 col-sm-4">
                    CAAS镜像标签
                  </div>
                  <div
                    class="col-md-8 col-sm-8 text-grey-8 ellipsis"
                    :title="item.pro_image_uri"
                  >
                    {{ item.pro_image_uri }}
                  </div>
                </div>
                <div
                  class="q-mb-sm row"
                  v-if="
                    changesDetail.image_deliver_type === '1' &&
                      item.deploy_type.includes('SCC')
                  "
                >
                  <div class="col-md-4 col-sm-4">
                    SCC镜像标签
                  </div>
                  <div
                    class="col-md-8 col-sm-8 text-grey-8 ellipsis"
                    :title="item.pro_scc_image_uri"
                  >
                    {{ item.pro_scc_image_uri }}
                  </div>
                </div>
                <div
                  class="q-mb-sm row"
                  v-if="changesDetail.image_deliver_type !== '1'"
                >
                  <div class="col-md-4 col-sm-5">
                    TAG
                  </div>
                  <div class="col-md-8 col-sm-7 text-grey-8 ellipsis clickable">
                    {{ item.product_tag }}
                    <fdev-popup-proxy class="fixheight">
                      <p class="q-mx-md q-my-sm">{{ item.product_tag }}</p>
                      <fdev-separator />
                    </fdev-popup-proxy>
                  </div>
                </div>
                <div
                  class="q-mb-sm row"
                  v-if="
                    changesDetail.image_deliver_type === '1' &&
                      item.esf_flag === '1'
                  "
                >
                  <div class="col-md-4 col-sm-4">
                    TAG
                  </div>
                  <div
                    class="col-md-8 col-sm-8 text-grey-8 ellipsis"
                    :title="item.tag"
                  >
                    {{ item.tag }}
                  </div>
                </div>
                <div
                  class="q-mb-sm row items-center"
                  v-if="changesDetail.image_deliver_type === '1'"
                >
                  <div class="col-md-4 col-sm-4">
                    介质目录
                  </div>
                  <div
                    class="col-md-7 col-sm-8 text-grey-8 ellipsis"
                    :title="item.prod_dir ? item.prod_dir.toString() : ''"
                  >
                    {{ item.prod_dir ? item.prod_dir.toString() : '' }}
                  </div>
                  <div style="height:21px">
                    <fdev-btn
                      flat
                      ficon="edit"
                      style="margin-top:-6px"
                      @click="updateCatalog(item)"
                      v-if="
                        compareTime() &&
                          (role(item) ||
                            changesDetail.can_operation ||
                            isKaDianManager)
                      "
                    />
                  </div>
                </div>
                <div
                  class="q-mb-sm row"
                  v-if="item.fdev_config_changed === true"
                >
                  <div class="col-md-4 col-sm-4">
                    配置文件检查
                  </div>
                  <div
                    class="col-md-8 col-sm-7 text-grey-8 clickable"
                    v-if="item.compare_url"
                  >
                    <span class="imageUrl">
                      {{
                        item.fdev_config_confirm === '1' &&
                        (item.pro_image_uri || item.pro_scc_image_uri)
                          ? '已检查'
                          : '未检查'
                      }}
                    </span>
                    <fdev-icon name="arrow_drop_down" class="icon" />
                    <fdev-popup-proxy class="fixheight">
                      <p class="q-mx-md q-my-sm">{{ item.compare_url }}</p>
                    </fdev-popup-proxy>
                  </div>
                  <div v-else class="col-md-8 col-sm-7 text-grey-8">
                    {{
                      item.fdev_config_confirm === '1' && item.pro_image_uri
                        ? '已检查'
                        : '未检查'
                    }}
                  </div>
                </div>
              </div>
            </fdev-card-section>

            <fdev-separator />
            <!-- 自动化发布 -->
            <fdev-card-actions
              v-if="changesDetail.image_deliver_type === '1'"
              class="q-gutter-xs"
            >
              <fdev-btn
                label="变更文件管理"
                flat
                v-if="(role(item) || isKaDianManager) && compareTime()"
                @click="
                  $router.push({
                    name: 'UpdateFileManage',
                    params: { id: id },
                    query: {
                      template_id: item.template_id,
                      application_id: item.application_id
                    }
                  })
                "
              />
              <div>
                <fdev-btn
                  flat
                  label="选择镜像版本"
                  v-if="
                    compareTime() &&
                      (role(item) ||
                        changesDetail.can_operation ||
                        isKaDianManager)
                  "
                  @click="openDia(item)"
                  :disable="needDocker(item)"
                />
                <fdev-tooltip v-if="needDocker(item)">
                  介质目录不含docker、docker_yaml、scc、scc_yaml中之一，无法选择镜像标签，请修改介质目录。
                </fdev-tooltip>
              </div>
              <fdev-btn
                label="fdev配置文件"
                flat
                @click="openUrl(item.compare_url)"
                v-if="item.fdev_config_changed"
              />
              <fdev-btn
                label="审核"
                flat
                @click="handleCheck(item)"
                :color="item.fdev_config_confirm === '1' ? 'primary' : 'red'"
                v-if="
                  (changesDetail.can_operation || isKaDianManager) &&
                    item.fdev_config_changed === true &&
                    !(
                      item.type_name &&
                      (item.type_name.toLowerCase().includes('ios') ||
                        item.type_name.toLowerCase().includes('android'))
                    )
                "
              />
              <span>
                <fdev-btn
                  v-if="
                    compareTime() &&
                      (role(item) ||
                        changesDetail.can_operation ||
                        isKaDianManager)
                  "
                  :disable="
                    !(
                      item.prod_dir.includes('docker_startall') ||
                      item.prod_dir.includes('scc_startall')
                    )
                  "
                  flat
                  label="重启副本数"
                  @click="
                    editReplicas(
                      item.change,
                      item.application_id,
                      item.deploy_type
                    )
                  "
                />
                <fdev-tooltip
                  v-if="
                    !(
                      item.prod_dir.includes('docker_startall') ||
                      item.prod_dir.includes('scc_startall')
                    )
                  "
                >
                  介质目录含docker_startall或scc_startall才能重启副本数
                </fdev-tooltip>
              </span>
            </fdev-card-actions>
          </fdev-card>
        </div>
      </div>
    </div>
    <div class="text-center q-mt-md" v-else>
      <f-image name="no_data" />
    </div>
    <SelectPro
      :loading="globalLoading['releaseForm/setImageTag']"
      v-model="proDia"
      @click="handleImageTag"
      :release_name="release_name"
      :fdev_config_changed="fdev_config_changed"
      :appDetail="appDetail"
    />
    <f-dialog right v-model="riskAppTipOpened" title="风险提示">
      以下应用已添加至其他变更版本中，请联系相关应用负责人进行风险评估
      <fdev-markup-table separator="vertical" flat bordered>
        <thead>
          <tr>
            <th class="text-left">应用英文名</th>
            <th class="text-left">应用负责人</th>
            <th class="text-left">变更日期</th>
            <th class="text-left">变更版本</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(app, index) in riskAppList" :key="index">
            <td class="text-left ellipsis" :title="app.app_name_en">
              {{ app.app_name_en }}
            </td>
            <td
              class="text-left ellipsis"
              :title="
                app.app_dev_managers
                  .map(manager => manager.user_name_cn)
                  .join(',')
              "
            >
              {{
                app.app_dev_managers
                  .map(manager => manager.user_name_cn)
                  .join(',')
              }}
            </td>
            <td class="text-left ellipsis" :title="app.riskProd.date">
              {{ app.riskProd.date }}
            </td>
            <td class="text-left ellipsis" :title="app.riskProd.version">
              {{ app.riskProd.version }}
            </td>
          </tr>
        </tbody>
      </fdev-markup-table>
      <template v-slot:btnSlot>
        <fdev-btn
          label="我已知晓"
          dialog
          @click="handleHasKnown"
          v-if="!singleTip"
        />
      </template>
    </f-dialog>
    <f-dialog v-model="confirmConfigOpen" title="确认审核" persistent>
      该应用的配置文件有变化，请点击
      <a class="span-link" :href="checkData.compare_url" target="_blank">
        审核地址 </a
      >，前往审核。
      <template v-slot:btnSlot>
        <fdev-btn dialog label="审核通过" @click="checkOk" />
      </template>
    </f-dialog>

    <f-dialog v-model="assetCatalogOpened" title="修改介质目录">
      <f-formitem label="介质目录" diaS>
        <fdev-select
          multiple
          ref="prod_dir"
          v-model="prod_dir"
          :options="prodDirListFilter"
          :option-disable="
            esfFlag
              ? opt =>
                  (opt === 'docker_yaml' &&
                    esfProdDir.includes('docker_yaml')) ||
                  (opt === 'scc_yaml' && esfProdDir.includes('scc_yaml')) ||
                  (opt === 'docker_startall' && caasSelectItemFlag) ||
                  (opt === 'scc_startall' && sccSelectItemFlag)
                    ? true
                    : false
              : opt =>
                  (opt === 'docker_startall' && caasSelectItemFlag) ||
                  (opt === 'scc_startall' && sccSelectItemFlag)
                    ? true
                    : false
          "
          :rules="[() => $v.prod_dir.required || '请选择介质目录']"
        />
      </f-formitem>
      <f-formitem label="CASS停止的集群" diaS v-if="isCAAS">
        <div class="row">
          <fdev-checkbox v-model="CAASlist" val="SHK1" label="SHK1">
          </fdev-checkbox>
          <fdev-checkbox
            v-if="application_type !== 'gray'"
            v-model="CAASlist"
            val="SHK2"
            label="SHK2"
          >
          </fdev-checkbox>
          <fdev-checkbox v-model="CAASlist" val="HFK1" label="HFK1">
          </fdev-checkbox>
          <fdev-checkbox
            v-if="application_type !== 'gray'"
            v-model="CAASlist"
            val="HFK2"
            label="HFK2"
          >
          </fdev-checkbox>
        </div>
      </f-formitem>

      <f-formitem label="SCC停止的集群" diaS v-if="isSCC">
        <div class="row">
          <fdev-checkbox v-model="SCClist" val="SHK1" label="SHK1">
          </fdev-checkbox>
          <fdev-checkbox
            v-if="application_type !== 'gray'"
            v-model="SCClist"
            val="SHK2"
            label="SHK2"
          >
          </fdev-checkbox>
          <fdev-checkbox v-model="SCClist" val="HFK1" label="HFK1">
          </fdev-checkbox>
          <fdev-checkbox
            v-if="application_type !== 'gray'"
            v-model="SCClist"
            val="HFK2"
            label="HFK2"
          >
          </fdev-checkbox>
        </div>
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          label="取消"
          outline
          @click="assetCatalogOpened = false"
        />
        <fdev-btn
          dialog
          label="确定"
          :loading="globalLoading['releaseForm/updateProdDir']"
          @click="handleUpdate"
        />
      </template>
    </f-dialog>

    <UpdateReplicas
      v-model="replicasDialog"
      :replicasObj="replicasObj"
      :source="updateReplicasSource"
      :applicationId="application_id"
      :deployPlatform="deployPlatform"
      @confirm="confirmUpdateReplicas"
    />
  </div>
</template>

<script>
import { mapActions, mapMutations, mapState, mapGetters } from 'vuex';
import SelectPro from './components/SelectPro';
import UpdateReplicas from './components/updateReplicas';
import { successNotify, isValidReleaseDate, deepClone } from '@/utils/utils';
import { typeOptionsObj } from '../../utils/model';
import { required } from 'vuelidate/lib/validators';

export default {
  name: 'updateList',
  components: {
    SelectPro,
    UpdateReplicas
  },
  props: {
    application_type: {
      type: String
    }
  },
  data() {
    return {
      id: '',
      UpdateTemplate: false, // 变更模板设置弹窗开关
      updateList: [],
      application_id: '',
      release_name: '',
      proDia: false, // 选镜像标签开关
      fdev_config_changed: false,
      typeOptions: [],
      type: '全部',
      tableData: [],
      riskAppList: [],
      riskAppTipOpened: false,
      checkData: '',
      confirmConfigOpen: false,
      singleTip: false,
      prod_dir: [],
      noVmodelProdDir: [],
      assetCatalogOpened: false,
      prod_id: '',
      release_type: '',
      replicasDialog: false, // 修改副本数弹窗开关
      replicasObj: {}, // 各发布环境对应的副本数-传给子组件
      deployPlatform: [], // 应用卡片上已勾选的部署平台-传给子组件
      updateReplicasSource: '',
      queryReplicasnuCode: '',
      esfFlag: false,
      esfProdDir: null,
      caasSelectItemFlag: false,
      sccSelectItemFlag: false,
      appDetail: null,
      appNameEn: '', // 当前应用的英文名，如果是以mspmk-cli开头的前端应用，修改介质目录时不可选scc相关介质目录
      CAASlist: [], //选择caas禁止的集群结合
      SCClist: [] //选择scc禁止的集群结合
    };
  },
  validations: {
    prod_dir: {
      required
    }
  },
  filters: {
    typeOptionsObj(val) {
      if (val) {
        return typeOptionsObj[val];
      }
    }
  },
  watch: {
    changeApllication(newVal, oldVal) {
      this.init();
    },
    prod_dir(newVal, oldVal) {
      this.changeProdDir(newVal, oldVal);
      if (!newVal.includes('docker_stop')) this.CAASlist = [];
      if (!newVal.includes('scc_stop')) this.SCClist = [];
    },
    assetCatalogOpened(newVal, oldVal) {
      if (!newVal) {
        // 修改介质目录的弹窗关闭
        this.queryReplicasnuCode = '';
        this.appNameEn = '';
        this.replicasObj = {};
        this.SCClist = [];
        this.CAASlist = [];
      }
    },
    replicasDialog(newVal, oldVal) {
      if (!newVal && this.updateReplicasSource === 'restartBtn') {
        // 重启副本数弹窗关闭
        this.replicasObj = {};
      }
    }
  },
  computed: {
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapState('releaseForm', {
      changeApllication: 'changeApllication',
      imageTagResult: 'imageTagResult',
      changesDetail: 'changesDetail',
      packageTags: 'packageTags',
      hasKnown: 'hasKnown',
      prodDirList: 'prodDirList',
      replicasNum: 'replicasNum'
    }),
    // 判断当前用户是否为卡点管理员角色
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),
    // 介质目录可选项过滤，变更未选择scc新版excel模板时，不可选scc相关介质目录
    // 当前应用是mspmk-cli开头的前端应用时，不可选scc相关介质目录
    prodDirListFilter() {
      let result = [];
      if (
        this.changesDetail.scc_prod !== '1' ||
        this.appNameEn.startsWith('mspmk-cli')
      ) {
        result = this.prodDirList.filter(str => str.includes('docker'));
      } else {
        result = this.prodDirList.slice();
      }
      return result;
    },
    //是否选择的CASS平台
    isCAAS() {
      return this.prod_dir.includes('docker_stop');
    },
    //是否选择的SCC平台
    isSCC() {
      return this.prod_dir.includes('scc_stop');
    }
  },
  methods: {
    ...mapMutations('releaseForm', ['saveHasKnown']),
    ...mapActions('releaseForm', {
      getChangeApplications: 'getChangeApplications',
      changeName: 'changeName',
      queryImageTags: 'queryImageTags',
      setImageTag: 'setImageTag',
      changeReleaseConf: 'changeReleaseConf',
      queryPackageTags: 'queryPackageTags',
      setPackageTag: 'setPackageTag',
      confirmChanges: 'confirmChanges',
      queryProdDir: 'queryProdDir',
      updateProdDir: 'updateProdDir',
      updateProdDeploy: 'updateProdDeploy',
      queryReplicasnu: 'queryReplicasnu'
    }),

    // 修改应用卡片上的部署平台
    async updateDeployType(deployType, app) {
      try {
        await this.updateProdDeploy({
          prod_id: app.prod_id,
          application_id: app.application_id,
          deploy_type: deployType,
          release_type: app.release_type
        });
        successNotify('更新成功');
        this.getTableData();
      } catch (err) {
        this.getTableData();
      }
    },

    // 点击应用卡片上的编辑介质目录图标按钮
    async updateCatalog(data) {
      await this.queryProdDir();
      this.prod_dir = data.prod_dir;
      this.noVmodelProdDir = data.prod_dir;
      this.prod_id = data.prod_id;
      this.application_id = data.application_id;
      this.release_type = data.release_type;
      this.deployPlatform = data.deploy_type;
      this.appNameEn = data.app_name_en;
      this.updateReplicasSource = 'updateProdDir';
      this.caasSelectItemFlag = data.caas_add_sign === '1' ? true : false;
      this.sccSelectItemFlag = data.scc_add_sign === '1' ? true : false;
      this.SCClist = data.scc_stop_env ? data.scc_stop_env : [];
      this.CAASlist = data.scc_stop_env ? data.caas_stop_env : [];
      this.queryReplicasnuCode = '';
      if (data.esf_flag === '1') {
        this.esfFlag = true;
        this.esfProdDir = data.prod_dir;
      } else {
        this.esfFlag = false;
        this.esfProdDir = null;
      }
      this.assetCatalogOpened = true;
    },

    // 点击“修改介质目录”弹窗的“确定”按钮
    async handleUpdate() {
      this.$v.prod_dir.$touch();
      this.$refs['prod_dir'].validate();
      if (this.$v.prod_dir.$invalid) return;
      if (this.queryReplicasnuCode !== 'error') {
        if (
          !this.prod_dir.includes('docker_startall') &&
          !this.prod_dir.includes('scc_startall')
        ) {
          this.replicasObj = {};
        }
        await this.updateProdDir({
          prod_id: this.prod_id,
          application_id: this.application_id,
          prod_dir: this.prod_dir,
          release_type: this.release_type,
          change: this.replicasObj,
          caas_stop_env: this.isCAAS ? this.CAASlist : [],
          scc_stop_env: this.isSCC ? this.SCClist : []
        });
        successNotify('更新成功');
        this.getTableData();
        this.queryReplicasnuCode = '';
        this.appNameEn = '';
        this.assetCatalogOpened = false;
      }
    },

    // 获取列表数据
    async getTableData() {
      await this.getChangeApplications({ prod_id: this.id });
      this.tableData = this.changeApllication;
      if (Array.isArray(this.riskAppList) && this.riskAppList.length === 0) {
        this.changeApllication.forEach(app => {
          if (app.isAppRisk === '1') {
            this.riskAppList.push(app);
          }
        });
      }
      this.init();
    },
    init() {
      const typeList = [];
      this.changeApllication.forEach(app => {
        if (app.release_type) {
          typeList.push(typeOptionsObj[app.release_type]);
        }
      });
      this.typeOptions = ['全部', ...new Set(typeList)];
      this.appFilter();
      if (this.riskAppList.length > 0 && !this.hasKnown) {
        this.riskAppTipOpened = true;
      }
    },
    handleSingleTip(item) {
      this.riskAppList = [item];
      this.singleTip = true;
      this.riskAppTipOpened = true;
    },
    handleHasKnown() {
      if (!this.hasKnown) {
        this.saveHasKnown(true);
      }
      this.riskAppTipOpened = false;
    },
    async confirm(data) {
      const params = {
        template_id: data,
        prod_id: this.id,
        application_id: this.application_id
      };
      await this.changeName(params);
      this.UpdateTemplate = false;
      this.getTableData();
    },
    // 删除
    async deleted(data) {
      const params = {
        prod_id: data.prod_id,
        application_id: data.application_id
      };
      this.$q
        .dialog({
          title: '确认删除',
          message: '您确定删除该变更应用吗?',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.$store.dispatch('releaseForm/deleteApplication', params);
          successNotify('删除成功');
          this.getTableData();
        });
    },
    // 判断变更日期是否过期，返回true为没过期，返回false为已过期
    compareTime() {
      return isValidReleaseDate(this.$q.sessionStorage.getItem('changeTime'));
    },
    handleCheck(item) {
      this.confirmConfigOpen = true;
      this.checkData = item;
    },
    async checkOk() {
      await this.confirmChanges({
        application_id: this.checkData.application_id,
        prod_id: this.checkData.prod_id
      });
      this.riskAppList = [];
      this.getTableData();
      this.confirmConfigOpen = false;
    },
    // 判断当前用户是否为应用负责人或行内项目负责人
    role(person) {
      const dev_managers = person.app_dev_managers.find(item => {
        return item.user_name_en === this.currentUser.user_name_en;
      });
      const spdb_managers = person.app_spdb_managers.find(item => {
        return item.user_name_en === this.currentUser.user_name_en;
      });
      return Boolean(spdb_managers || dev_managers);
    },

    // 点击应用卡片上的“选择镜像版本”按钮，打开“选择镜像版本”弹窗
    async openDia(data) {
      this.appDetail = data;
      this.fdev_config_changed = false;
      this.release_name = this.$q.sessionStorage.getItem('release_node_name');
      const params = {
        application_id: data.application_id,
        release_node_name: this.$q.sessionStorage.getItem('release_node_name')
      };
      this.application_id = data.application_id;
      await this.queryImageTags(params);
      this.proDia = true;
    },

    async handleImageTag(data) {
      const params = {
        application_id: this.application_id,
        prod_id: this.id,
        pro_image_uri: data.pro_image_uri,
        pro_scc_image_uri: data.pro_scc_image_uri
      };
      if (this.fdev_config_changed) {
        this.proDia = false;
        await this.changeReleaseConf(params);
        successNotify('已通过');
        this.getTableData();
        return;
      }
      await this.setImageTag(params);
      this.imageTagResult.application_tips
        ? successNotify(this.imageTagResult.application_tips)
        : successNotify('选择镜像版本成功');
      this.getTableData();
      if (this.imageTagResult.fdev_config_changed) {
        this.fdev_config_changed = true;
        return;
      }
      this.proDia = false;
    },

    openUrl(url) {
      window.open(url);
    },
    appFilter(val = this.type) {
      if (val === '全部') {
        this.tableData = this.changeApllication;
        return;
      }
      const data = this.changeApllication.filter(
        app => typeOptionsObj[app.release_type] === val
      );
      if (data.length === 0) {
        this.tableData = this.changeApllication;
        this.type = '全部';
        return;
      }
      this.tableData = data;
    },
    needDocker(item) {
      let dockerList = ['docker', 'docker_yaml', 'scc', 'scc_yaml'];
      let include = false;
      if (item.prod_dir) {
        include = item.prod_dir.some(item => {
          return dockerList.includes(item);
        });
      }
      return !include;
    },

    // 点击“重启副本数”按钮，将子组件所需属性参数赋值传递过去
    editReplicas(replicasObj, appId, deployPlatform) {
      this.updateReplicasSource = 'restartBtn';
      if (replicasObj) {
        this.replicasObj = replicasObj;
      } else {
        this.replicasObj = {};
      }
      this.application_id = appId;
      this.deployPlatform = deployPlatform;
      this.replicasDialog = true;
    },

    // 修改副本数完成
    confirmUpdateReplicas(replicasObj) {
      if (replicasObj && this.updateReplicasSource === 'updateProdDir') {
        this.replicasObj = deepClone(replicasObj);
        this.replicasDialog = false;
      } else {
        this.replicasDialog = false;
        this.getTableData();
      }
    },

    // 修改介质目录
    async changeProdDir(newVal, oldVal) {
      this.queryReplicasnuCode = '';
      /* 
      应用卡片的介质目录不含docker_startall或scc_startall 且
      修改介质目录弹窗在勾选介质目录之前本身不包含docker_startall或scc_startall 且
      操作勾选的介质目录是docker_startall或scc_startall时,
      发接口弹窗展示/修改副本数
      */
      if (
        (!this.noVmodelProdDir.includes('docker_startall') &&
          !oldVal.includes('docker_startall') &&
          newVal[newVal.length - 1] === 'docker_startall') ||
        (!this.noVmodelProdDir.includes('scc_startall') &&
          !oldVal.includes('scc_startall') &&
          newVal[newVal.length - 1] === 'scc_startall')
      ) {
        try {
          let deployPlatform = [];
          if (newVal.includes('docker_startall')) {
            deployPlatform.push('CAAS');
          }
          if (newVal.includes('scc_startall')) {
            deployPlatform.push('SCC');
          }
          // 传递给子组件及查询副本数上送的部署平台依据选择的介质目录而定
          // 不是拿取应用卡片上的部署平台
          this.deployPlatform = deployPlatform;
          await this.queryReplicasnu({
            prod_id: this.$route.params.id,
            application_id: this.application_id,
            release_type: this.release_type,
            deploy_type: this.deployPlatform
          });
          this.replicasObj = deepClone(this.replicasNum.change);
          this.replicasDialog = true;
        } catch (e) {
          this.queryReplicasnuCode = 'error';
        }
      }
    }
  },
  async created() {
    this.id = this.$route.params.id;
    this.getTableData();
  }
};
</script>

<style lang="stylus" scoped>
.delete
  position absolute
  top 0
  right 0
  z-index 50
.link
  &:last-child
    span
      display none
</style>
