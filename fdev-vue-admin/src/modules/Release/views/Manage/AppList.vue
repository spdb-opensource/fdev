<template>
  <div>
    <Loading :visible="loading">
      <div class="row q-pa-md items-center">
        <f-formitem class="col-4" bottom-page label="搜索条件">
          <fdev-select
            :value="selectValue"
            multiple
            use-input
            hide-dropdown-icon
            ref="select"
            @new-value="addSelect"
            class="inline-block"
            @input="updateSelectValue($event)"
          >
            <template v-slot:append>
              <f-icon name="search" flat @click="setSelect($refs.select)" />
            </template>
          </fdev-select>
        </f-formitem>

        <f-formitem class="col-4" label="" bottom-page>
          <span class="text-grey-6"> 检索到{{ filterLength }}个应用 </span>
        </f-formitem>

        <f-formitem class="col-4" label="" bottom-page>
          <span class="text-grey-6"> 共{{ applyList.length }}个应用 </span>
        </f-formitem>
      </div>
      <fdev-list class="q-pa-sm" v-if="applyList.length > 0">
        <fdev-separator />

        <template v-for="(item, i) in filterData">
          <fdev-expansion-item
            default-opened
            :key="item.type"
            v-show="item.children.length > 0"
          >
            <fdev-table
              noExport
              :title="item.type"
              :data="item.children"
              :pagination.sync="paginationTable"
              :columns="tableColumns"
              hide-bottom
              grid
              no-select-cols
              no-top-bottom
            >
              <template v-slot:item="props">
                <div class="col-4 q-pa-sm">
                  <fdev-card
                    class="shadow-1"
                    square
                    :class="props.row.new_add_sign === '0' ? 'div-border' : ''"
                  >
                    <fdev-card-section class="row no-wrap items-start">
                      <div class="col">
                        <router-link
                          :to="`/app/list/${props.row.application_id}`"
                          class="link"
                        >
                          <div class="text-h6 text-title inline-block">
                            {{ props.row.app_name_en }}
                          </div>
                        </router-link>
                        <fdev-btn
                          outline
                          no-caps
                          label="faketime镜像"
                          @click="openFakeDiolag(props.row)"
                          class="float-right"
                          v-if="
                            isBankMaster(
                              props.row.app_dev_managers.concat(
                                props.row.app_spdb_managers
                              )
                            ) && compareTime
                          "
                        />
                      </div>
                    </fdev-card-section>
                    <fdev-card-section>
                      <div class="text-grey-9">
                        <div class="q-mb-sm row">
                          <div class="col-md-4 col-sm-5">
                            应用名称
                          </div>
                          <div
                            v-if="props.row.app_name_zh"
                            class="col-md-8 col-sm-7 text-grey-8 ellipsis"
                            :title="props.row.app_name_zh"
                          >
                            <router-link
                              :to="`/app/list/${props.row.application_id}`"
                              class="link"
                            >
                              {{ props.row.app_name_zh }}
                              <fdev-popup-proxy context-menu>
                                <fdev-banner style="max-width:300px">
                                  {{ props.row.app_name_zh }}
                                </fdev-banner>
                              </fdev-popup-proxy>
                            </router-link>
                          </div>
                        </div>
                        <div class="q-mb-sm row">
                          <div class="col-md-4 col-sm-5">
                            行内项目负责人
                          </div>
                          <div
                            class="col-md-8 col-sm-7 ellipsis"
                            v-if="props.row.app_name_zh"
                            :title="
                              props.row.app_spdb_managers
                                .map(v => v.user_name_cn)
                                .join(',')
                            "
                          >
                            <span
                              v-for="(user, index) in props.row
                                .app_spdb_managers"
                              :key="index"
                            >
                              <router-link :to="`/user/list/${user.user_id}`">
                                {{ user.user_name_cn }}
                              </router-link>
                              <fdev-popup-proxy context-menu>
                                <fdev-banner>
                                  {{
                                    props.row.app_spdb_managers
                                      .map(v => v.user_name_cn)
                                      .join(',')
                                  }}
                                </fdev-banner>
                              </fdev-popup-proxy>
                            </span>
                          </div>
                        </div>
                        <div class="q-mb-sm row">
                          <div class="col-md-4 col-sm-5">
                            应用负责人
                          </div>
                          <div
                            class="col-md-8 col-sm-7 ellipsis"
                            v-if="props.row.app_name_zh"
                            :title="
                              props.row.app_dev_managers
                                .map(v => v.user_name_cn)
                                .join(',')
                            "
                          >
                            <span
                              v-for="(user, index) in props.row
                                .app_dev_managers"
                              :key="index"
                            >
                              <router-link :to="`/user/list/${user.user_id}`">
                                {{ user.user_name_cn }}
                              </router-link>
                            </span>
                            <fdev-popup-proxy context-menu>
                              <fdev-banner>
                                {{
                                  props.row.app_dev_managers
                                    .map(v => v.user_name_cn)
                                    .join(',')
                                }}
                              </fdev-banner>
                            </fdev-popup-proxy>
                          </div>
                        </div>
                        <div class="q-mb-sm row">
                          <div class="col-md-4 col-sm-5">
                            应用类型
                          </div>
                          <div
                            class="col-md-8 col-sm-7 text-grey-8 ellipsis"
                            :title="props.row.type_name || '-'"
                          >
                            {{ props.row.type_name || '-' }}
                          </div>
                        </div>
                        <div class="q-mb-sm row">
                          <div class="col-md-4 col-sm-5">
                            release分支
                          </div>
                          <div
                            class="col-md-8 col-sm-7 text-grey-8 ellipsis"
                            :title="props.row.release_branch || '-'"
                          >
                            {{ props.row.release_branch || '-' }}
                          </div>
                        </div>
                        <div class="q-mb-sm row">
                          <div class="col-md-4 col-sm-5">
                            TAG列表
                          </div>
                          <div
                            class="col-md-8 col-sm-7 text-grey-8 ellipsis cursor-pointer"
                            v-if="props.row.product_tag.length > 0"
                          >
                            {{ props.row.product_tag[0] }}
                            <f-icon name="arrow_d_f" />
                            <fdev-popup-proxy class="fixheight">
                              <div
                                v-for="data in props.row.product_tag"
                                :key="data"
                              >
                                <p class="q-mx-md q-my-sm">{{ data }}</p>
                                <fdev-separator />
                              </div>
                            </fdev-popup-proxy>
                          </div>
                        </div>
                        <div
                          class="q-mb-sm row"
                          v-if="
                            !(
                              props.row.type_name &&
                              (props.row.type_name
                                .toLowerCase()
                                .includes('ios') ||
                                props.row.type_name
                                  .toLowerCase()
                                  .includes('android'))
                            )
                          "
                        >
                          <div class="col-md-4 col-sm-5">
                            CAAS镜像列表
                          </div>
                          <div
                            class="col-md-8 col-sm-7 text-grey-8 cursor-pointer"
                            v-if="props.row.pro_image_uri.length > 0"
                          >
                            <span class="imageUrl">
                              {{ props.row.pro_image_uri[0] }}
                            </span>
                            <f-icon name="arrow_d_f" />
                            <fdev-popup-proxy class="fixheight">
                              <div
                                v-for="data in props.row.pro_image_uri"
                                :key="data"
                              >
                                <p class="q-mx-md q-my-sm">{{ data }}</p>
                                <fdev-separator />
                              </div>
                            </fdev-popup-proxy>
                          </div>
                        </div>
                        <div
                          class="q-mb-sm row"
                          v-if="
                            !(
                              props.row.type_name &&
                              (props.row.type_name
                                .toLowerCase()
                                .includes('ios') ||
                                props.row.type_name
                                  .toLowerCase()
                                  .includes('android'))
                            )
                          "
                        >
                          <div class="col-md-4 col-sm-5">
                            SCC镜像列表
                          </div>
                          <div
                            class="col-md-8 col-sm-7 text-grey-8 cursor-pointer"
                            v-if="props.row.pro_scc_image_uri.length > 0"
                          >
                            <span class="imageUrl">
                              {{ props.row.pro_scc_image_uri[0] }}
                            </span>
                            <f-icon name="arrow_d_f" />
                            <fdev-popup-proxy class="fixheight">
                              <div
                                v-for="data in props.row.pro_scc_image_uri"
                                :key="data"
                              >
                                <p class="q-mx-md q-my-sm">{{ data }}</p>
                                <fdev-separator />
                              </div>
                            </fdev-popup-proxy>
                          </div>
                        </div>
                        <div
                          class="q-mb-sm row"
                          v-if="
                            !(
                              props.row.type_name &&
                              (props.row.type_name
                                .toLowerCase()
                                .includes('ios') ||
                                props.row.type_name
                                  .toLowerCase()
                                  .includes('android'))
                            )
                          "
                        >
                          <div class="col-md-4 col-sm-5">
                            UAT环境
                          </div>
                          <div
                            class="col-md-8 col-sm-7 text-grey-8 ellipsis"
                            :title="props.row.uat_env_name"
                          >
                            {{ props.row.uat_env_name }}
                          </div>
                        </div>
                        <div
                          class="q-mb-sm row"
                          v-if="
                            !(
                              props.row.type_name &&
                              (props.row.type_name
                                .toLowerCase()
                                .includes('ios') ||
                                props.row.type_name
                                  .toLowerCase()
                                  .includes('android'))
                            )
                          "
                        >
                          <div class="col-md-4 col-sm-5">
                            REL环境
                          </div>
                          <div
                            class="col-md-8 col-sm-7 text-grey-8 ellipsis"
                            :title="props.row.rel_env_name"
                          >
                            {{ props.row.rel_env_name }}
                          </div>
                        </div>
                        <div
                          class="q-mb-sm row"
                          v-if="props.row.fdev_config_changed === true"
                        >
                          <div class="col-md-4 col-sm-5">
                            配置文件审核
                          </div>
                          <div
                            class="col-md-8 col-sm-7 text-grey-8 cursor-pointer ellipsis"
                            v-if="props.row.compare_url"
                          >
                            <span class="imageUrl">
                              {{
                                props.row.fdev_config_confirm | filterComfirm
                              }}
                            </span>
                            <f-icon name="arrow_d_f" />
                            <fdev-popup-proxy class="fixheight">
                              <p class="q-mx-md q-my-sm">
                                {{ props.row.compare_url }}
                              </p>
                            </fdev-popup-proxy>
                          </div>
                          <div
                            v-else
                            class="col-md-8 col-sm-7 text-grey-8 ellipsis"
                          >
                            {{ props.row.fdev_config_confirm | filterComfirm }}
                          </div>
                        </div>
                      </div>
                    </fdev-card-section>

                    <fdev-separator
                      v-if="
                        (isBankMaster(
                          props.row.app_dev_managers.concat(
                            props.row.app_spdb_managers
                          )
                        ) ||
                          isKaDianManager) &&
                          compareTime
                      "
                    />
                    <fdev-card-section
                      v-if="
                        (isBankMaster(
                          props.row.app_dev_managers.concat(
                            props.row.app_spdb_managers
                          )
                        ) ||
                          isKaDianManager) &&
                          compareTime
                      "
                    >
                      <div class="q-gutter-x-md">
                        <div
                          class="inline-block"
                          v-if="
                            !(
                              props.row.type_name &&
                              (props.row.type_name
                                .toLowerCase()
                                .includes('ios') ||
                                props.row.type_name
                                  .toLowerCase()
                                  .includes('android'))
                            )
                          "
                        >
                          <fdev-tooltip
                            anchor="top middle"
                            v-if="!props.row.rel_env_name"
                          >
                            请选择REL测试环境
                          </fdev-tooltip>
                          <fdev-btn
                            label="提交发布"
                            flat
                            :disable="
                              props.row.devops_status == '1' ||
                                !props.row.rel_env_name
                            "
                            @click="handleConfirmModalOpen('0', props.row)"
                          />
                        </div>
                        <fdev-btn
                          v-if="
                            !(
                              props.row.type_name &&
                              (props.row.type_name
                                .toLowerCase()
                                .includes('ios') ||
                                props.row.type_name
                                  .toLowerCase()
                                  .includes('android'))
                            )
                          "
                          label="测试环境"
                          flat
                          @click="openSelectEnvDialog(props.row)"
                        />
                        <div
                          class="inline-block"
                          v-if="
                            props.row.type_name &&
                              (props.row.type_name
                                .toLowerCase()
                                .includes('ios') ||
                                props.row.type_name
                                  .toLowerCase()
                                  .includes('android'))
                          "
                        >
                          <fdev-btn
                            v-for="item in handleBtns"
                            :key="item.flag"
                            class="q-mr-md"
                            flat
                            :label="`${item.btnName}发布`"
                            @click="
                              handleIOSOrAndroidPublish(props.row, item.flag)
                            "
                          />
                        </div>
                        <fdev-btn
                          label="APPSTORE发布"
                          flat
                          v-if="
                            props.row.type_name &&
                              props.row.type_name
                                .toLowerCase()
                                .includes('ios') &&
                              props.row.product_tag.length > 0
                          "
                          @click="handleIOSOrAndroidPublish(props.row, '4')"
                        />
                        <fdev-btn
                          label="CI/CD"
                          flat
                          @click="handlePipelineOpen(props.row)"
                        />
                        <div
                          class="inline-block"
                          v-if="
                            !(
                              props.row.type_name &&
                              (props.row.type_name
                                .toLowerCase()
                                .includes('ios') ||
                                props.row.type_name
                                  .toLowerCase()
                                  .includes('android'))
                            ) && props.row.product_tag[0]
                          "
                        >
                          <fdev-tooltip
                            anchor="top middle"
                            v-if="!props.row.rel_env_name"
                          >
                            请选择REL测试环境
                          </fdev-tooltip>
                          <fdev-btn
                            label="重新打包"
                            flat
                            :disable="
                              !props.row.product_tag || !props.row.rel_env_name
                            "
                            @click="handleConfirmModalOpen('1', props.row)"
                            color="primary"
                          />
                        </div>
                        <fdev-btn
                          label="审核"
                          flat
                          @click="handleCheck(props.row)"
                          :color="
                            props.row.fdev_config_confirm === '1'
                              ? 'primary'
                              : 'red'
                          "
                          v-if="
                            props.row.fdev_config_changed === true &&
                              !(
                                props.row.type_name &&
                                (props.row.type_name
                                  .toLowerCase()
                                  .includes('ios') ||
                                  props.row.type_name
                                    .toLowerCase()
                                    .includes('android'))
                              )
                          "
                        />

                        <fdev-btn
                          flat
                          label="修改批次"
                          @click="handleChangeBatchDialogOpen(props.row)"
                        />
                      </div>
                    </fdev-card-section>
                  </fdev-card>
                </div>
              </template>
            </fdev-table>
          </fdev-expansion-item>

          <fdev-separator :key="i" v-show="item.children.length > 0" />
        </template>
      </fdev-list>

      <div class="text-center q-mt-md" v-else>
        <f-image name="no_data" />
      </div>
    </Loading>

    <f-dialog v-model="confirmModalOpen" right :title="tipMsg">
      <div v-show="operateType == '0'">
        <f-formitem diaS label="版本号" v-if="isClient">
          <fdev-input
            v-if="isClient"
            label="版本号"
            autofocus
            v-model="$v.publishModel.tag_version.$model"
            ref="publishModel.tag_version"
            :rules="[
              () => $v.publishModel.tag_version.require || '请输入版本号',
              () =>
                $v.publishModel.tag_version.format ||
                '只能输入字母、数组和“.”,不能以“.”开头'
            ]"
          />
        </f-formitem>
        <!-- 是否加固 -->
        <f-formitem
          diaS
          label="是否加固"
          v-if="
            rowData.type_name &&
              (rowData.type_name.toLowerCase().includes('ios') ||
                rowData.type_name.toLowerCase().includes('android'))
          "
        >
          <fdev-radio val="1" v-model="publishModel.is_reinforce" label="是" />
          <fdev-radio val="0" v-model="publishModel.is_reinforce" label="否" />
        </f-formitem>
        <f-formitem label="描述信息" diaS>
          <fdev-input
            v-model="$v.publishModel.description.$model"
            ref="publishModel.description"
            type="textarea"
            :rules="[
              () => !$v.publishModel.description.$error || '请输入描述信息'
            ]"
          />
        </f-formitem>
        功能说明：
        <ol>
          <li>提交后将发起此release分支至master分支的合并请求</li>
          <li>
            合并完毕后将从master分支拉取tag，tag名称：pro-{窗口名称}-{3位序号}
          </li>
          <li>
            tag分支拉取后将触发gitlab
            CI/CD,打包生成docker镜像并发布至准生产测试环境
          </li>
          <li>
            投产相关文件请至gitlab项目获取，通过变更详情页上传
          </li>
        </ol>
      </div>
      <div v-show="operateType == '1'">
        <f-formitem label="tag分支" diaS>
          <fdev-select
            v-model="product_tag"
            :options="options"
            @filter="filterFn"
          />
        </f-formitem>
        功能说明：
        <p>
          本功能适用于镜像仓库误删除情境，从选择tag分支直接打出生产docker镜像
        </p>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          label="确定"
          :disable="disabled"
          @click="submit(operateType)"
        />
      </template>
    </f-dialog>

    <f-dialog v-model="examine" title="待审核任务关联" right>
      <p>
        该应用有任务未完成关联项审核，请联系DBA审核人、对应的任务负责人或行内项目负责人审核，点击
        <fdev-btn flat ficon="board_s_f" @click="sendEmail" />
        发送邮件通知
      </p>

      <fdev-table
        fdev-table
        title="待审核任务关联项"
        :data="examineData"
        :columns="columns"
        class="bg-white"
        titleIcon="list_s_f"
        noExport
      >
      </fdev-table>
    </f-dialog>

    <f-dialog v-model="selectEnvDialogOpen" right title="请选择测试环境">
      <div class="q-gutter-md">
        <f-formitem label="UAT测试环境" diaS>
          <fdev-select
            input-debounce="0"
            use-input
            emit-value
            map-options
            :options="labelsFuzzy"
            @filter="queryUATEnv"
            option-label="name_en"
            option-value="name_en"
            ref="testEnv.uat_env_name"
            v-model="testEnv.uat_env_name"
          />
        </f-formitem>
        <f-formitem label="REL测试环境" diaS>
          <fdev-select
            input-debounce="0"
            use-input
            emit-value
            map-options
            :options="labelsFuzzy"
            @filter="queryRELEnv"
            option-label="name_en"
            option-value="name_en"
            ref="testEnv.rel_env_name"
            v-model="testEnv.rel_env_name"
          />
        </f-formitem>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          label="确定"
          @click="handleSelectEnv"
          :disable="!testEnv.rel_env_name && !testEnv.uat_env_name"
        />
      </template>
    </f-dialog>

    <f-dialog v-model="pipelinesOpen" v-if="pipelinesOpen" title="CI/CD" right>
      <Pipelines
        v-show="!jobOpen"
        :rowNumber="5"
        class="pipline-width"
        :application_id="application_id"
        :release_node_name="release_node_name"
        @job="handleJobOpen"
      />
      <Job class="q-mt-lg" v-if="jobOpen" :project_id="params" />
      <template v-slot:btnSlot>
        <fdev-btn label="返回" dialog @click="jobOpen = false" />
      </template>
    </f-dialog>

    <f-dialog v-model="confirmConfigOpen" title="确认审核" persistent>
      该应用的配置文件有变化，请点击
      <a :href="checkData.compare_url" target="_blank"> 审核地址 </a
      >，前往审核。
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          outline
          label="取消"
          @click="confirmConfigOpen = false"
        />
        <fdev-btn dialog label="审核通过" @click="checkOk" />
      </template>
    </f-dialog>
    <f-dialog v-model="confirmEnv" title="提示">
      您的应用尚未绑定过部署信息，请点击
      <a @click="goCheckEnv" target="_blank"> 去绑定 </a>
    </f-dialog>
    <!-- faketime弹窗 -->
    <f-dialog v-model="fakeTimeDialog" title="选择基础镜像">
      <f-formitem diaS label="设置faketime镜像版本">
        <fdev-toggle
          false-value="0"
          true-value="1"
          :rules="[() => true]"
          v-model="imageModel.fake_flag"
        />
      </f-formitem>
      <f-formitem diaS label="镜像名称" v-if="imageModel.fake_flag === '1'">
        <fdev-select
          input-debounce="0"
          use-input
          :options="imageNameOptions"
          @filter="imageNameFilter"
          @blur="imageNameTouch"
          v-model="$v.imageModel.fakeImageName.$model"
          ref="imageModel.fakeImageName"
          option-label="name_cn"
          option-value="name"
          :rules="[
            () => !$v.imageModel.fakeImageName.$error || '请输入描述信息'
          ]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.name_cn">
                  {{ scope.opt.name_cn }}
                </fdev-item-label>
                <fdev-item-label caption :title="scope.opt.name">
                  {{ scope.opt.name }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <f-formitem
        diaS
        label="镜像版本"
        v-if="imageModel.fakeImageName && imageModel.fake_flag === '1'"
      >
        <fdev-select
          input-debounce="0"
          filled
          use-input
          v-model="$v.imageModel.fakeImageVersion.$model"
          ref="imageModel.fakeImageVersion"
          :options="imageVersionOptions"
          @filter="imageVersionFilter"
          option-value="image_tag"
          option-label="image_tag"
          :rules="[
            () => !$v.imageModel.fakeImageVersion.$error || '请输入描述信息'
          ]"
        >
          <template v-slot:option="scope">
            <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
              <fdev-item-section>
                <fdev-item-label :title="scope.opt.image_tag">
                  {{ scope.opt.image_tag }}
                </fdev-item-label>
              </fdev-item-section>
            </fdev-item>
          </template>
        </fdev-select>
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn dialog label="确认" @click="handleFakeFlagAllTip" />
      </template>
    </f-dialog>
    <!-- 点击准生产发布的时候弹框选择是否为特殊测试包，以及是否加固 -->
    <f-dialog
      v-if="handleFlag !== '4'"
      v-model="zhunCreatePublish"
      persist
      title="准生产发布"
    >
      <f-formitem diaS label="是否为特殊测试包？" v-if="handleFlag === '1'">
        <fdev-select
          emit-value
          map-options
          v-model="zhunCreatePublishType"
          option-value="id"
          option-label="zhunCreatePublishType"
          :options="zhunCreatePublishTypeOptions"
        />
      </f-formitem>
      <f-formitem diaS label="是否加固？">
        <fdev-radio val="1" v-model="reinforce" label="是" class="q-mr-lg" />
        <fdev-radio val="0" v-model="reinforce" label="否" />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn
          dialog
          outline
          label="取消"
          @click="zhunCreatePublish = false"
        />
        <fdev-btn
          dialog
          label="确定"
          @click="goZhunCreatePublish"
          :loading="globalLoading['releaseForm/iOSOrAndroidAppPublish']"
        />
      </template>
    </f-dialog>
    <ChangeBatchDialog
      :data="changeBatchDialogModel"
      v-model="changeBatchDialogOpened"
      title="修改变更批次"
      @submit="handleChangeBatchDialog"
      isEdit
    />

    <f-dialog
      right
      v-model="publishFailureMessageDialogOpened"
      title="提交发布失败"
    >
      <div class="message-wrapper" v-if="publishFailureMessage.stage_task_name">
        <div class="header q-mb-sm">以下任务尚未进入 UAT 阶段：</div>
        <router-link
          class="q-mb-sm q-ml-md link block"
          v-for="(name, id) in publishFailureMessage.stage_task_name"
          :key="name"
          :to="`/job/list/${id}`"
          target="_blank"
        >
          {{ name }}
        </router-link>
      </div>

      <div
        class="message-wrapper classq-mt-md"
        v-if="publishFailureMessage.report_task_name"
      >
        <div class="header q-mb-sm">
          以下任务业测报告未到达，请前往需求文档库上传业测报告！
        </div>
        <router-link
          class="q-mb-sm q-ml-md link block"
          v-for="(name, id) in publishFailureMessage.report_task_name"
          :key="name"
          :to="`/job/list/${id}`"
          target="_blank"
        >
          {{ name }}
        </router-link>
      </div>

      <div
        class="message-wrapper classq-mt-md"
        v-if="publishFailureMessage.confirm_task_name"
      >
        <div class="header q-mb-sm">
          以下任务确认书未到达，请前往需求文档库上传上线确认书、任务详情打开上线确认书开关！
        </div>
        <router-link
          class="q-mb-sm q-ml-md link block"
          v-for="(name, id) in publishFailureMessage.confirm_task_name"
          :key="name"
          :to="`/job/list/${id}`"
          target="_blank"
        >
          {{ name }}
        </router-link>
      </div>

      <div
        class="message-wrapper classq-mt-md"
        v-if="publishFailureMessage.safety_task_name"
      >
        <div class="header q-mb-sm">以下任务未通过安全测试：</div>
        <router-link
          class="q-mb-sm q-ml-md link block"
          v-for="(name, id) in publishFailureMessage.safety_task_name"
          :key="name"
          :to="`/job/list/${id}`"
          target="_blank"
        >
          {{ name }}
        </router-link>
      </div>

      <div
        class="message-wrapper classq-mt-md"
        v-if="
          publishFailureMessage.system &&
            publishFailureMessage.system.length > 0
        "
      >
        <div class="header q-mb-md">
          当前应用未绑定所属系统，请绑定:
          <fdev-form @submit.prevent="handleBindSystem" ref="bindModel">
            <div class="row">
              <fdev-select
                use-input
                @filter="systemInputFilter"
                ref="bindModel.systemId"
                filled
                label="系统名称"
                v-model="$v.bindModel.systemId.$model"
                :options="filterSystem"
                class="col-3 bind-width q-pb-md q-mt-md"
                option-label="name"
                :rules="[
                  () => $v.bindModel.systemId.required || '请选择系统名称'
                ]"
              >
              </fdev-select>
              <div class="q-pb-md q-mt-md q-ml-lg">
                <fdev-btn
                  :loading="globalLoading['appForm/bindSystem']"
                  color="primary"
                  label="绑定"
                  type="submit"
                  @click="handleAddUserAllTip"
                />
              </div>
            </div>
          </fdev-form>
        </div>
      </div>
    </f-dialog>

    <AddBatchMessage
      :data="appUserInfo"
      v-model="AddBatchMessageDialogOpened"
    />
  </div>
</template>

<script>
import {
  taskListColumn,
  appListColumn,
  taskColumns,
  runTaskColumns
} from '../../utils/constants';
import { testEnv, batchArr } from '../../utils/model';
import ChangeBatchDialog from '../../Components/ChangeBatchDialog';
import AddBatchMessage from '../../Components/AddBatchMessage.vue';
import { mapState, mapGetters, mapActions, mapMutations } from 'vuex';
import Loading from '@/components/Loading';
import Pipelines from '@/modules/App/components/pipelines';
import Job from '@/modules/App/views/pipelines/jobs';
import { successNotify, validate, deepClone, errorNotify } from '@/utils/utils';
import {
  setSelector,
  getSelector,
  setPagination,
  getPagination
} from '../../utils/setting';
import { required } from 'vuelidate/lib/validators';

export default {
  name: 'JobList',
  components: {
    AddBatchMessage,
    Loading,
    Pipelines,
    Job,
    ChangeBatchDialog
  },
  data() {
    return {
      filterSystem: [],
      loading: true,
      idInfo: {},
      pagination: getPagination(),
      userOptions: [],
      rowData: {},
      filter: '',
      rowSelected: [],
      tableGroup: '',
      tableGroups: [],
      selector: getSelector() || [],
      selection: 'multiple',
      terms: [],
      tableData: [],
      filterData: [],
      tipMsg: '拒绝原因',
      operateType: '0',
      confirmModalOpen: false,
      detail: {},
      disabled: false,
      product_tag: '',
      application_id: '',
      options: [],
      publishModel: {
        description: '',
        tag_version: '',
        is_reinforce: '0'
      },
      examine: false, // 审核窗口
      examineData: [],
      confirmEnv: false,
      columns: taskListColumn,
      cloneData: {},
      testReason: [
        { label: '正常', value: '1' },
        { label: '需求变更', value: '3' }
      ],
      testData: {
        id: [],
        desc: '',
        stage: '2', // 1.SIT  2.REL
        reason: '1' // 1.正常  2.缺陷  3. 需求变更
      },
      selectEnvDialogOpen: false,
      testEnv: testEnv(),
      release_branch: '',
      releaseNodeName: '',
      pipelinesOpen: false,
      jobOpen: false,
      params: {},
      tableColumns: appListColumn,
      filterValue: '',
      paginationTable: {
        rowsPerPage: 0
      },
      testRuningDialogOpened: false,
      selectedTask: [],
      appTaskListLoading: false,
      taskColumns: taskColumns,
      runTaskColumns: runTaskColumns,
      testrun_applications: [],
      testrunApplicationsOptions: [],
      isClient: false,
      confirmConfigOpen: false,
      checkData: '',
      network: '',
      fakeTimeDialog: false,
      imageNameOptions: [],
      imageModel: { fakeImageName: '', fakeImageVersion: '', fake_flag: '0' },
      imageVersionOptions: [],
      appSelected: {},
      zhunCreatePublish: false,
      zhunCreatePublishTypeOptions: [
        { zhunCreatePublishType: '否', id: 'release' },
        { zhunCreatePublishType: '兼容性测试包', id: 'jrcs' },
        { zhunCreatePublishType: '自动化测试包', id: 'auto' },
        { zhunCreatePublishType: '压测包', id: 'yace ' }
      ],
      zhunCreatePublishType: 'release',
      reinforce: '0',
      handleBtns: [
        { btnName: '准生产', flag: '1' },
        { btnName: '灰度', flag: '2' },
        { btnName: '生产', flag: '3' }
      ],
      handleFlag: '',
      changeBatchDialogOpened: false,
      changeBatchDialogModel: {},
      publishFailureMessageDialogOpened: false,
      AddBatchMessageDialogOpened: false,
      bindModel: {
        systemId: ''
      },
      systemOptions: []
    };
  },
  validations: {
    publishModel: {
      description: {
        required
      },
      tag_version: {
        require(val) {
          if (this.isClient) {
            return val.trim();
          }
          return true;
        },
        format(val) {
          if (!val) return true;
          const reg = /^(?!\.)([a-zA-Z0-9.]*)$/;
          return reg.test(val);
        }
      }
    },
    testData: {
      reason: {
        required
      },
      desc: {
        required
      }
    },
    imageModel: {
      fakeImageName: {
        required
      },
      fakeImageVersion: {
        required
      }
    },
    bindModel: {
      systemId: {
        required
      }
    }
  },
  filters: {
    devops_status_Filter: function(input) {
      let json = {
        '0': '未合并',
        '1': '已发起merge request,待合并',
        '2': 'merge request发起失败',
        '3': '合并完成,待拉取product tag',
        '4': '拒绝合并',
        '5': '已拉取product tag',
        '6': '已打包'
      };
      return json[input];
    },
    filterComfirm(val) {
      return val === '1' ? '已确认' : '未确认';
    }
  },
  watch: {
    pagination(val) {
      setPagination({
        rowsPerPage: val.rowsPerPage
      });
    },
    selector(val) {
      setSelector(val);
    },
    terms(val, oldVal) {
      this.filter = val.toString();
    },
    selectEnvDialogOpen(val) {
      if (val === false) {
        this.testEnv = testEnv();
      }
    },
    pipelinesOpen(val) {
      this.jobOpen = false;
    },
    selectValue(val) {
      this.filterMethods(val);
    }
  },
  computed: {
    ...mapState('userActionSaveRelease/releaseAppList', ['selectValue']),
    ...mapState('userForm', [
      'groups',
      'companies',
      'roles',
      'isLeaves',
      'tags'
    ]),
    ...mapState('authorized', {
      auths: 'list'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),
    ...mapState('releaseForm', [
      'compareTime',
      'labelsFuzzy',
      'applyList',
      'examineList',
      'tagsList',
      'tasksOfAppType',
      'testRunBranch',
      'appTaskList',
      'taskByTestRunId',
      'appUserInfo'
    ]),
    ...mapGetters('userForm', ['wrapTotal']),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('appForm', ['vueAppData']),
    // ...mapState('environmentForm', ['selectAppEnv']),
    ...mapState('componentForm', ['imageManageList', 'imageRecordList']),
    ...mapState('releaseForm', ['FakeInfoData', 'releaseDetail']),
    filterLength() {
      return this.filterData.reduce((sum, i) => {
        return sum + i.children.length;
      }, 0);
    },
    publishFailureMessage() {
      if (!this.tasksOfAppType) {
        return {};
      }
      // this.systemOptions = this.tasksOfAppType.system;
      const {
        stage_task_name,
        report_task_name,
        confirm_task_name,
        safety_task_name,
        system,
        application_id
      } = this.tasksOfAppType;
      return {
        stage_task_name:
          Object.keys(stage_task_name).length === 0 ? '' : stage_task_name,
        report_task_name:
          Object.keys(report_task_name).length === 0 ? '' : report_task_name,
        confirm_task_name:
          Object.keys(confirm_task_name).length === 0 ? '' : confirm_task_name,
        safety_task_name:
          Object.keys(safety_task_name).length === 0 ? '' : safety_task_name,
        system: system ? system : [],
        application_id: application_id
      };
    }
  },
  methods: {
    ...mapMutations('userActionSaveRelease/releaseAppList', [
      'updateSelectValue'
    ]),
    ...mapActions('user', {
      fetchUser: 'fetch'
    }),
    ...mapActions('appForm', ['queryApps', 'bindSystem']),
    ...mapActions('releaseForm', {
      queryByLabelsFuzzy: 'queryByLabelsFuzzy',
      setTestEnvs: 'setTestEnvs',
      queryApply: 'queryApply',
      packageFromTag: 'packageFromTag',
      relDevops: 'relDevops',
      queryTasksReviews: 'queryTasksReviews',
      queryApplicationTags: 'queryApplicationTags',
      sendEmailForTaskManagers: 'sendEmailForTaskManagers',
      tasksInSitStage: 'tasksInSitStage',
      createTestrunBranch: 'createTestrunBranch',
      mergeTaskBranch: 'mergeTaskBranch',
      queryNotinlineTasksByAppId: 'queryNotinlineTasksByAppId',
      changeConfirmConf: 'changeConfirmConf',
      iOSOrAndroidAppPublish: 'iOSOrAndroidAppPublish',
      addBatch: 'addBatch',
      queryBatchInfoByAppId: 'queryBatchInfoByAppId'
    }),
    // ...mapActions('environmentForm', ['queryProEnvByAppId']),
    ...mapActions('componentForm', ['queryBaseImage', 'queryBaseImageRecord']),
    ...mapActions('releaseForm', ['editFakeInfo']),
    systemInputFilter(val, update) {
      update(() => {
        this.filterSystem = this.publishFailureMessage.system.filter(
          tag => tag.name.indexOf(val) > -1
        );
      });
    },
    // 点击相关发布按钮
    async handleIOSOrAndroidPublish(row, flag) {
      this.rowData = row;
      if (flag === '1' || flag === '2') {
        if (this.tasksOfAppType && !this.tasksOfAppType.flag) {
          errorNotify(
            `"${this.tasksOfAppType.task_name}"任务尚未进入UAT阶段，无法进行${
              flag === '1' ? '准生产' : '灰度'
            }发布`
          );
          return;
        }
        this.zhunCreatePublish = true;
        this.handleFlag = flag;
      } else if (flag === '4') {
        // APPSTORE发布
        await this.iOSOrAndroidAppPublish({
          release_node_name: this.releaseDetail.release_node_name,
          is_reinforce: this.reinforce,
          env: 'appstore',
          id: row.application_id,
          type: ''
        });
        successNotify('APPSTORE发布成功！');
      } else if (flag === '3') {
        // 生产发布
        this.handleConfirmModalOpen('0', row);
      }
    },

    handleAddUserAllTip() {
      this.$v.bindModel.$touch();
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('bindModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      if (this.$v.bindModel.$invalid) {
        return;
      }
    },
    async handleBindSystem() {
      await this.bindSystem({
        systemId: this.bindModel.systemId.id,
        appId: this.publishFailureMessage.application_id
      }),
        successNotify('绑定成功');
      this.publishFailureMessageDialogOpened = false;
    },

    // 点击dialog内的确定按钮
    async goZhunCreatePublish() {
      if (this.handleFlag === '1') {
        // 准生产发布
        await this.iOSOrAndroidAppPublish({
          release_node_name: this.releaseDetail.release_node_name,
          is_reinforce: this.reinforce,
          env: this.zhunCreatePublishType,
          id: this.rowData.application_id,
          type: 'release'
        });
        successNotify('准生产发布成功！');
      } else if (this.handleFlag === '2') {
        // 灰度发布
        await this.iOSOrAndroidAppPublish({
          release_node_name: this.releaseDetail.release_node_name,
          is_reinforce: this.reinforce,
          env: 'gray',
          id: this.rowData.application_id,
          type: ''
        });
        successNotify('灰度发布成功！');
      }
      this.zhunCreatePublish = false;
    },
    // 打开faketime弹窗,映射之前的基础镜像值（如有）
    async openFakeDiolag(val) {
      this.appSelected = val;
      this.fakeTimeDialog = true;
      this.imageModel.fake_flag = val.fake_flag ? val.fake_flag : '0';
      (this.imageModel.fakeImageVersion = val.fake_image_version),
        await this.queryBaseImage();
      this.imageNameOptions = this.imageManageList;
      let ImageName = this.imageManageList.filter(item => {
        if (item.name === val.fake_image_name) {
          return item.name_cn;
        }
      });
      this.imageModel.fakeImageName = ImageName[0];
    },
    imageNameFilter(val, update) {
      update(() => {
        this.imageNameOptions = this.imageManageList.filter(
          tag =>
            tag.name_cn.indexOf(val) > -1 ||
            tag.name.includes(val.toLowerCase())
        );
      });
    },
    imageVersionFilter(val, update) {
      update(() => {
        this.imageVersionOptions = this.imageRecordList.filter(tag =>
          tag.image_tag.includes(val.toLowerCase())
        );
      });
    },

    async imageNameTouch(val) {
      await this.queryBaseImageRecord({
        name: this.imageModel.fakeImageName.name
      });
      this.imageModel.fakeImageVersion = '';
      this.imageVersionOptions = this.imageRecordList;
    },
    // faketime 必输校验
    handleFakeFlagAllTip() {
      this.$v.imageModel.$touch();
      // 获取要校验的dom并传入validate，此步骤要求每个ref唯一
      const Keys = Object.keys(this.$refs).filter(key => {
        return key.indexOf('imageModel') > -1;
      });
      validate(
        Keys.map(key => {
          return this.$refs[key];
        })
      );
      // 只要有校验未通过的，$invalid会被validate设为true
      if (this.$v.imageModel.$invalid) {
        return;
      }
      this.handleFakeFlag();
    },
    async handleFakeFlag() {
      await this.editFakeInfo({
        release_node_name: this.appSelected.release_node_name,
        application_id: this.appSelected.application_id,
        fake_flag: this.imageModel.fake_flag,
        fake_image_name: this.imageModel.fakeImageName.name || '',
        fake_image_version: this.imageModel.fakeImageVersion.image_tag
          ? this.imageModel.fakeImageVersion.image_tag
          : this.imageModel.fakeImageVersion
      });
      successNotify(this.FakeInfoData);
      this.init();
      this.fakeTimeDialog = false;
    },
    // 清理弹窗信息
    clear() {
      this.publishModel.description = '';
      this.publishModel.tag_version = '';
    },
    /* 打开确认操作modal */
    async handleConfirmModalOpen(num, data) {
      this.application_id = data.application_id;
      const { app_name_en } = data;
      await this.queryApps({ name_en: app_name_en });
      // if (
      //   this.vueAppData[0] &&
      //   this.vueAppData[0].label.indexOf('不涉及环境部署') < 0
      // ) {
      // 包含 '不涉及环境部署'字段,正常流程,不包含字段 继续判断
      //   await this.queryProEnvByAppId({ app_id: this.application_id });
      //   if (this.selectAppEnv.length === 0) {
      //     this.confirmEnv = true;
      //     return;
      //   }
      // }
      this.isClient = false;
      this.clear();
      const params = {
        application_id: data.application_id,
        release_node_name: data.release_node_name
      };
      if (data.type_name) {
        const type_name = data.type_name.toLowerCase();
        this.isClient =
          type_name.includes('ios') || type_name.includes('android');
      }
      await this.queryTasksReviews(params);
      if (this.examineList.length > 0) {
        this.examineData = this.examineList;
        this.examine = true;
        this.cloneData = deepClone(data);
        return;
      }
      /* 重新打包前，发送接口查询该应用下是否有任务未进入rel阶段 */
      if (num === '0') {
        await this.tasksInSitStage(params);
        const { status } = this.tasksOfAppType;
        if (status !== '0') {
          this.publishFailureMessageDialogOpened = true;

          // errorNotify(
          //   `"${
          //     this.tasksOfAppType.task_name
          //   }"任务尚未进入UAT阶段，无法进行生产发布`
          // );
          return;
        }
      }
      this.confirmModalOpen = true;
      this.product_tag = data.product_tag[0];
      this.detail = data;
      this.operateType = num;
      this.tipMsg =
        num == '0'
          ? '输入本次合并请求提交信息'
          : '请选择已有tag分支重新进行打包操作';
    },
    /* confirmModal 确认 并调交易 */
    async submit(type) {
      this.disabled = true;
      if (type == '0') {
        this.$v.publishModel.$touch();
        let jobKeys = Object.keys(this.$refs).filter(
          key => key.indexOf('publishModel') > -1
        );
        validate(jobKeys.map(key => this.$refs[key]));
        this.disabled = false;
        if (this.$v.publishModel.$invalid) {
          return;
        }
        this.operate('0', this.detail);
      } else {
        this.operate('1', this.detail);
      }
    },
    async operate(msg, status) {
      if (msg === '0') {
        let params = {
          application_id: status.application_id,
          release_node_name: this.$route.params.id,
          description: this.publishModel.description,
          tag_version: this.publishModel.tag_version,
          is_reinforce: this.publishModel.is_reinforce
        };
        this.confirmModalOpen = false;
        this.disabled = false;
        await this.relDevops(params);
        this.publishModel.description = '';
        successNotify('已发起release分支至master分支的合并请求');
        this.init();
      }
      if (msg === '1') {
        let params = {
          application_id: status.application_id,
          release_node_name: this.$route.params.id,
          product_tag: this.product_tag
        };
        this.confirmModalOpen = false;
        this.disabled = false;
        await this.packageFromTag(params);
        successNotify('正在进行tag打包,请等待镜像打包完成。');
        this.init();
      }
    },
    isBankMaster(bankMasterList) {
      let isBankMasterFlag = false;
      for (var i = 0; i < bankMasterList.length; i++) {
        if (bankMasterList[i].user_name_en == this.currentUser.user_name_en) {
          isBankMasterFlag = true;
          break;
        }
      }
      return isBankMasterFlag;
    },
    addTerm(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    userFilter(rows, terms, cols, cellValue) {
      const lowerTerms = terms ? terms.toLowerCase().split(',') : [];
      return rows.filter(row => {
        let isNum = true;
        let isGroup = true;
        let companyId = (row.company + '').toLowerCase();
        let groupId = (row.group + '').toLowerCase();
        cols.some(col => {
          if (col.name === 'num') {
            isNum = companyId;
          }
          if (col.name === 'group') {
            isGroup = groupId;
          }
        });
        return (
          isNum &&
          isGroup &&
          lowerTerms.every(term => {
            return cols.some(
              col => (cellValue(col, row) + '').toLowerCase().indexOf(term) > -1
            );
          })
        );
      });
    },
    async init() {
      this.loading = true;
      await this.queryApply(this.$route.params.id);
      this.handleTableData();
      this.loading = false;
    },
    async sendEmail() {
      // 控制当前应用不能连续点击邮件发送
      if (!this.cloneData.sendEmailFlag) {
        this.cloneData.sendEmailFlag = true;
      } else {
        return;
      }
      let params = {
        release_tasks: this.examineData
      };
      await this.sendEmailForTaskManagers(params);
      successNotify('发送邮件成功');
      this.examine = false;
    },
    // tag下拉懒加载
    filterFn(val, update, abort) {
      update(async () => {
        await this.queryApplicationTags({
          application_id: this.application_id,
          release_node_name: this.$route.params.id
        });
        this.options = this.tagsList;
      });
    },
    openSelectEnvDialog(item) {
      this.application_id = item.application_id;
      this.testEnv = {
        uat_env_name: item.uat_env_name,
        rel_env_name: item.rel_env_name
      };
      this.network = item.network;
      this.selectEnvDialogOpen = true;
    },
    async handleSelectEnv() {
      const params = {
        ...this.testEnv,
        application_id: this.application_id,
        release_node_name: this.$route.params.id
      };
      await this.setTestEnvs(params);
      successNotify('设置成功');
      this.init();
      this.selectEnvDialogOpen = false;
    },
    async queryUATEnv(val, update, abort) {
      let labels = this.network.split(',');
      labels.push('uat');
      await this.queryByLabelsFuzzy({ labels });
      update();
    },
    async queryRELEnv(val, update, abort) {
      let labels = this.network.split(',');
      labels.push('rel');
      await this.queryByLabelsFuzzy({ labels });
      update();
    },
    handlePipelineOpen(item) {
      this.application_id = item.application_id;
      this.pipelinesOpen = true;
    },
    handleJobOpen(params) {
      this.params = params;
      this.jobOpen = true;
    },
    addSelect(val, done) {
      if (val.length > 0) {
        done(val);
      }
    },
    handleTableData() {
      this.tableData = this.applyList.reduce((sum, i) => {
        const batch = i.batch_id ? Number(i.batch_id) : 4;
        sum[batch - 1].children.push(i);
        return sum;
      }, batchArr());
      this.filterMethods();
    },
    filterMethods() {
      if (this.selectValue.length === 0) {
        this.filterData = this.tableData;
        return;
      }
      this.filterData = this.tableData.map(item => {
        const itemData = item.children.filter(data => {
          return this.selectValue.some(selected =>
            this.handleSearchData(Object.values(data), selected)
          );
        });
        return { ...item, children: itemData };
      });
    },
    handleSearchData(data, search) {
      return data.some(item => {
        if (typeof item === 'object' && item !== null) {
          const isArray = Array.isArray(item);
          const arr = isArray ? item : Object.values(item);
          return this.handleSearchData(arr, search);
        }
        return typeof item === 'string' && item.includes(search);
      });
    },
    // 点击搜索按钮
    setSelect(el) {
      if (el.inputValue.length) {
        el.add(el.inputValue);
        el.inputValue = '';
      }
    },
    async handleTestRuningDialog() {
      const params = this.selectedTask.map(task => {
        return {
          task_id: task.id,
          task_name: task.name,
          task_branch: task.feature_branch
        };
      });
      await this.mergeTaskBranch({
        testrun_task: params,
        application_id: this.application_id,
        release_node_name: this.release_node_name,
        transition_branch: this.testRunBranch.transition_branch,
        testrun_branch: this.testRunBranch.testrun_branch
      });
      successNotify('成功');
      this.testRuningDialogOpened = false;
      this.init();
      this.selectedTask = [];
    },
    testrunApplicationsFilter(val, update, abort) {
      update(() => {
        const needle = val.toLowerCase();
        this.testrunApplicationsOptions = this.testrun_applications.filter(v =>
          v.testrun_branch.toLowerCase().includes(needle)
        );
      });
    },
    handleCheck(data) {
      this.confirmConfigOpen = true;
      this.checkData = data;
    },
    async checkOk() {
      let params = {
        application_id: this.checkData.application_id,
        release_node_name: this.checkData.release_node_name
      };
      await this.changeConfirmConf(params);
      this.init();
      this.confirmConfigOpen = false;
    },
    goCheckEnv() {
      this.$router.push({
        name: 'DeployMessageHandlePage',
        query: { appId: this.application_id }
      });
    },
    confirmToClose(key) {
      this.$q
        .dialog({
          title: '关闭弹窗',
          message: '关闭弹窗后数据将会丢失，确认要关闭？',
          cancel: true,
          persistent: true
        })
        .onOk(() => {
          this[key] = false;
        });
    },
    handleChangeBatchDialogOpen(data) {
      this.changeBatchDialogOpened = true;
      this.changeBatchDialogModel = data;
    },
    async handleChangeBatchDialog(data) {
      await this.addBatch(data);
      successNotify('修改成功!');
      this.init();
      this.changeBatchDialogOpened = false;

      await this.queryBatchInfoByAppId({
        release_node_name: this.release_node_name,
        application_id: data.applications
      });

      if (this.appUserInfo.length > 0) {
        this.AddBatchMessageDialogOpened = true;
      }
    }
  },
  async created() {
    this.release_node_name = this.$route.params.id;
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.fixheight
  max-height 800px
  overflow auto
.imageUrl
  width calc(100% - 20px)
  overflow hidden
  white-space nowrap
  text-overflow ellipsis
  display inline-block
  vertical-align top
.div-border
  border-top solid 4px #21BA45
.header
  font-weight 700
  color #616161
.bind-width
 width 70%
.pipline-width
  width 900px
</style>
