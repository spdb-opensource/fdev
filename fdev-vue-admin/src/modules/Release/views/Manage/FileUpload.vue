<template>
  <div class="FileUpload">
    <!-- 自动化发布 -->
    <div v-if="changesDetail.image_deliver_type === '1'">
      <div class="bg-white" v-if="filterData.length > 0">
        <div v-for="(table, index) in filterData" :key="index">
          <div
            v-show="table.catalog_type === '4'"
            class="table q-mt-xs q-mb-md"
          >
            <fdev-bar class="bg-blue-grey-13 shadow-2">
              <div class="q-table__title text-white">
                <span class="q-mr-lg">介质目录：{{ table.catalog_name }}</span>
                <span>步骤：{{ table.description }}</span>
              </div>
              <fdev-space />
              <!-- 若配置文件地址,隐藏从gitlb拉取配置文件按钮 -->
              <span class="q-mr-md">
                <fdev-btn
                  flat
                  ficon="gitlab"
                  :disable="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager) ||
                      !systemDetailByProdId.resource_giturl
                  "
                  @click="openDialog(table.catalog_name)"
                />
                <fdev-tooltip
                  v-if="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager) ||
                      !systemDetailByProdId.resource_giturl
                  "
                >
                  <span v-if="!compareTime()">当前变更已过期</span>
                  <span
                    v-if="
                      compareTime() &&
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                  <span
                    v-if="
                      compareTime() &&
                        (changesDetail.can_operation || isKaDianManager) &&
                        !systemDetailByProdId.resource_giturl
                    "
                  >
                    没有gitlab地址
                  </span>
                </fdev-tooltip>
              </span>
              <span>
                <fdev-fab
                  direction="left"
                  class="q-my-md fabPosition"
                  icon="cloud_upload"
                  color="bg-blue-grey-13"
                  :disable="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <fdev-uploader
                    :url="`${url}frelease/api/release/uploadAssets`"
                    :headers="[
                      {
                        name: 'Authorization',
                        value: $q.localStorage.getItem('fdev-vue-admin-jwt')
                      }
                    ]"
                    :form-fields="[
                      { name: 'asset_catalog_name', value: table.catalog_name },
                      { name: 'prod_id', value: id },
                      { name: 'source_application', value: application }
                    ]"
                    field-name="file"
                    style="max-width: 400px"
                    :multiple="false"
                    @uploaded="uploader"
                    ref="upload"
                    @failed="failed"
                  />
                </fdev-fab>
                <fdev-tooltip
                  v-if="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <span v-if="!compareTime()">当前变更已过期</span>
                  <span
                    v-if="
                      compareTime() &&
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                </fdev-tooltip>
              </span>
            </fdev-bar>
            <fdev-table
              :data="table.assets"
              :columns="columnsWithout"
              :pagination.sync="pagination"
              row-key="filename"
              no-export
              no-select-cols
            >
              <template v-slot:body-cell-filename="props">
                <fdev-td class="text-ellipsis">
                  <a
                    class="text-blue-7 cursor-pointer"
                    target="_blank"
                    @click.stop="download(props.row)"
                    :title="props.row.fileName"
                  >
                    {{ props.row.fileName }}
                  </a>
                </fdev-td>
              </template>

              <template v-slot:body-cell-upload_user="props">
                <fdev-td>
                  <router-link
                    v-if="props.row.upload_user"
                    :to="{ path: `/user/list/${props.row.upload_user}` }"
                    class="link"
                  >
                    <span>{{ props.row.upload_username_cn }}</span>
                  </router-link>
                </fdev-td>
              </template>

              <template v-slot:body-cell-source="props">
                <fdev-td>
                  {{ props.row.source | source }}
                </fdev-td>
              </template>

              <template v-slot:body-cell-btn="props">
                <fdev-td>
                  <span>
                    <fdev-btn
                      :disable="
                        !compareTime() ||
                          !(changesDetail.can_operation || isKaDianManager)
                      "
                      label="删除"
                      @click="deleteFile(props.row.id)"
                      flat
                    />
                    <fdev-tooltip
                      v-if="
                        !compareTime() ||
                          !(changesDetail.can_operation || isKaDianManager)
                      "
                    >
                      <span v-if="!compareTime()">当前变更已过期</span>
                      <span
                        v-if="
                          compareTime() &&
                            !(changesDetail.can_operation || isKaDianManager)
                        "
                      >
                        请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                      </span>
                    </fdev-tooltip>
                  </span>
                </fdev-td>
              </template>
            </fdev-table>
            <fdev-separator />
          </div>

          <div
            v-show="
              table.catalog_type === '5' &&
                table.catalog_name === 'esfcommonconfig'
            "
            class="table q-mt-xs q-mb-md"
          >
            <fdev-bar class="bg-blue-grey-13 shadow-2">
              <div class="q-table__title text-white">
                <span class="q-mr-lg">介质目录：{{ table.catalog_name }}</span>
                <span>步骤：{{ table.description }}</span>
              </div>
              <fdev-space />
              <span>
                <fdev-btn
                  flat
                  ficon="add"
                  :disable="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                  class="cursor-pointer"
                  @click="addcommonconfig"
                />
                <fdev-tooltip
                  v-if="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <span v-if="!compareTime()">当前变更已过期</span>
                  <span
                    v-if="
                      compareTime() &&
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                </fdev-tooltip>
              </span>
            </fdev-bar>
            <fdev-table
              :data="table.list"
              :columns="columnsEsfCommonConfig"
              :pagination.sync="pagination"
              no-export
              no-select-cols
            >
              <template v-slot:body="props">
                <fdev-tr :props="props">
                  <fdev-td auto-width>
                    <fdev-btn
                      dense
                      round
                      flat
                      :icon="
                        props.row.expand
                          ? 'expand_more'
                          : 'keyboard_arrow_right'
                      "
                      @click="props.row.expand = !props.row.expand"
                    />
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.source_application_name || '-' }}
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.assets ? props.row.assets[0].sid : '-' }}
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.upload_username_cn || '-' }}
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.upload_time || '-' }}
                  </fdev-td>
                  <fdev-td>
                    <div class="q-gutter-x-sm row no-wrap">
                      <span>
                        <fdev-btn
                          flat
                          label="删除"
                          :disable="
                            !compareTime() ||
                              !(changesDetail.can_operation || isKaDianManager)
                          "
                          @click="deleteFiles(props.row.ids)"
                        />
                        <fdev-tooltip
                          v-if="
                            !compareTime() ||
                              !(changesDetail.can_operation || isKaDianManager)
                          "
                        >
                          <span v-if="!compareTime()">当前变更已过期</span>
                          <span
                            v-if="
                              compareTime() &&
                                !(
                                  changesDetail.can_operation || isKaDianManager
                                )
                            "
                          >
                            请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                          </span>
                        </fdev-tooltip>
                      </span>
                      <!-- <fdev-btn
                        flat
                        label="编辑"
                        v-if="compareTime() && (role || isKaDianManager)"
                        @click="editEsfCommonConfig(props.row)"
                      /> -->
                    </div>
                  </fdev-td>
                </fdev-tr>
                <fdev-tr v-show="props.row.expand" :props="props">
                  <fdev-td colspan="100%">
                    <fdev-table
                      :data="props.row.assets"
                      :columns="columnsEsfCommonConfigInfo"
                      hide-pagination
                      no-export
                      no-select-cols
                      :rows-per-page-options="['6']"
                    >
                      <template v-slot:body-cell-fileName="child">
                        <fdev-td>
                          <div
                            class="q-gutter-x-sm text-ellipsis"
                            :title="child.row.fileName.join('，')"
                          >
                            <a
                              v-for="(item, index) in child.row.fileName"
                              :key="index"
                              class="text-blue-7 cursor-pointer"
                              target="_blank"
                              @click.stop="download(child.row, index)"
                            >
                              {{ item }}
                            </a>
                          </div>
                        </fdev-td>
                      </template>
                    </fdev-table>
                  </fdev-td>
                </fdev-tr>
              </template>
            </fdev-table>
            <fdev-separator />
          </div>

          <div
            v-show="
              (table.catalog_type === '5' || table.catalog_type === '10') &&
                table.catalog_name !== 'esfcommonconfig'
            "
            class="q-mt-xs"
          >
            <fdev-bar class="bg-blue-grey-13 shadow-2">
              <div class="q-table__title text-white">
                <span class="q-mr-lg">介质目录：{{ table.catalog_name }}</span>
                <span>步骤：{{ table.description }}</span>
              </div>
              <fdev-space />
              <span class="q-mr-md" v-if="table.catalog_type === '5'">
                <fdev-btn
                  flat
                  ficon="add"
                  class="cursor-pointer"
                  @click="openDataDialog"
                  :disable="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                />
                <fdev-tooltip
                  v-if="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <span v-if="!compareTime()">当前变更已过期</span>
                  <span
                    v-if="
                      compareTime() &&
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                </fdev-tooltip>
                <fdev-tooltip v-else>获取配置文件列表</fdev-tooltip>
              </span>
              <span class="q-mr-md">
                <fdev-btn
                  flat
                  ficon="gitlab"
                  :disable="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager) ||
                      !systemDetailByProdId.resource_giturl
                  "
                  @click="
                    openRuntimeDia(table.catalog_name, table.catalog_type)
                  "
                />
                <fdev-tooltip
                  v-if="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager) ||
                      !systemDetailByProdId.resource_giturl
                  "
                >
                  <span v-if="!compareTime()">当前变更已过期</span>
                  <span
                    v-if="
                      compareTime() &&
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                  <span
                    v-if="
                      compareTime() &&
                        (changesDetail.can_operation || isKaDianManager) &&
                        !systemDetailByProdId.resource_giturl
                    "
                  >
                    没有gitlab地址
                  </span>
                </fdev-tooltip>
              </span>
              <span>
                <fdev-fab
                  direction="left"
                  icon="cloud_upload"
                  @click="clickFab(table, index)"
                  @hide="hideUploader(table, index)"
                  color="bg-blue-grey-13"
                  :disable="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <fdev-uploader
                    :url="uploadUrl"
                    :headers="[
                      {
                        name: 'Authorization',
                        value: $q.localStorage.getItem('fdev-vue-admin-jwt')
                      }
                    ]"
                    :form-fields="[
                      { name: 'asset_catalog_name', value: table.catalog_name },
                      { name: 'prod_id', value: id },
                      { name: 'source_application', value: application },
                      { name: 'test', value: [] },
                      { name: 'child_catalog', value: '' }
                    ]"
                    field-name="file"
                    style="max-width: 400px"
                    multiple
                    batch
                    @uploaded="uploader"
                    :ref="`upload${index}`"
                    @failed="failed"
                    @added="addFiles"
                    @finish="clearChildCatalog(table)"
                  >
                    <template v-slot:header="scope">
                      <div class="row no-wrap items-center q-pa-sm q-gutter-xs">
                        <fdev-btn
                          v-if="scope.queuedFiles.length > 0"
                          icon="clear_all"
                          round
                          dense
                          flat
                        />
                        <fdev-btn
                          v-if="scope.uploadedFiles.length > 0"
                          icon="done_all"
                          @click="finish(scope)"
                          round
                          dense
                          flat
                        />
                        <fdev-spinner
                          v-if="scope.isUploading"
                          class="q-uploader__spinner"
                        />
                        <div class="col">
                          <div class="q-uploader__title">
                            上传同名文档(需选择所有环境)
                          </div>
                        </div>
                        <fdev-btn
                          v-if="scope.canAddFiles"
                          icon="add_box"
                          round
                          dense
                          flat
                        >
                          <fdev-uploader-add-trigger class="hidden" />
                          <fdev-menu>
                            <fdev-list>
                              <fdev-item
                                v-for="run in commonconfigEnv"
                                :key="run"
                                @click="
                                  pickFile(run, index, table.catalog_type)
                                "
                                v-show="
                                  hadSelect(run, scope, table.catalog_type)
                                "
                                clickable
                                v-close-popup
                              >
                                <fdev-item-section>{{ run }}</fdev-item-section>
                              </fdev-item>
                            </fdev-list>
                          </fdev-menu>
                        </fdev-btn>
                        <fdev-btn
                          v-if="
                            scope.editable &&
                              uploadIcon(scope.formFields[3].value)
                          "
                          icon="cloud_upload"
                          @click="uploadFile(scope, table)"
                          round
                          dense
                          flat
                        />
                        <fdev-btn
                          v-if="scope.editable && scope.isUploading"
                          icon="clear"
                          @click="scope.abort"
                          round
                          dense
                          flat
                        />
                      </div>
                    </template>

                    <template v-slot:list="scope">
                      <f-formitem
                        v-if="table.catalog_type !== '10'"
                        label="部署平台"
                        diaS
                        label-style="color:rgba(0,0,0,0.6)"
                      >
                        <div
                          class="row items-center"
                          style="color:rgba(0,0,0,0.6)"
                        >
                          <fdev-radio
                            v-model="commonconfigPlatform"
                            class="q-mr-lg"
                            val="CAAS"
                            label="CAAS"
                            @input="inputPlatform($event, index)"
                          />
                          <fdev-radio
                            v-model="commonconfigPlatform"
                            val="SCC"
                            label="SCC"
                            @input="inputPlatform($event, index)"
                          />
                        </div>
                      </f-formitem>
                      <fdev-input
                        type="text"
                        flat
                        dense
                        label="子目录名称"
                        v-model="childCatalog"
                      />
                      <div
                        class="uploader-content row justify-center"
                        v-if="
                          scope.formFields[3].value.length ===
                            scope.files.length && scope.files.length > 0
                        "
                      >
                        <fdev-icon
                          name="cloud_upload"
                          color="grey-5"
                          size="64px"
                          v-if="scope.formFields[3].value.length === 0"
                        />
                        <div class="col" v-else>
                          <fdev-list separator>
                            <fdev-item
                              v-for="(file, num) in scope.files"
                              :key="num"
                              bordered
                            >
                              <fdev-item-section side top>
                                <fdev-badge>{{ file.runtime_env }}</fdev-badge>
                                <fdev-icon
                                  name="warning"
                                  color="red"
                                  size="22px"
                                  class="warning"
                                  v-if="file.__status == 'failed'"
                                />
                              </fdev-item-section>
                              <fdev-item-section>
                                <fdev-item-label class="full-width ellipsis">
                                  {{ file.name }}
                                </fdev-item-label>
                                <fdev-item-label caption>
                                  {{ file.__sizeLabel }} /{{
                                    file.__progressLabel
                                  }}
                                </fdev-item-label>
                              </fdev-item-section>
                              <fdev-item-section
                                v-if="file.__img"
                                thumbnail
                                class="gt-xs"
                              >
                                <img :src="file.__img.src" />
                              </fdev-item-section>
                              <fdev-item-section top side>
                                <fdev-btn
                                  class="gt-xs delete"
                                  size="12px"
                                  flat
                                  dense
                                  round
                                  :icon="
                                    file.__status == 'uploaded'
                                      ? 'done'
                                      : 'delete'
                                  "
                                  @click="deleteUploadFile(scope, file)"
                                />
                              </fdev-item-section>
                            </fdev-item>
                          </fdev-list>
                        </div>
                      </div>
                      <div class="uploader-content row justify-center" v-else>
                        <fdev-icon
                          name="cloud_upload"
                          color="grey-5"
                          size="64px"
                          v-if="scope.formFields[3].value.length === 0"
                        />
                        <div class="col" v-else>
                          <fdev-list separator>
                            <fdev-item
                              v-for="(file, num) in scope.formFields[3].value"
                              :key="num"
                              bordered
                            >
                              <fdev-item-section side top>
                                <fdev-badge>{{ file.runtime_env }}</fdev-badge>
                                <fdev-icon
                                  name="warning"
                                  color="red"
                                  size="22px"
                                  class="warning"
                                  v-if="file.__status == 'failed'"
                                />
                              </fdev-item-section>
                              <fdev-item-section>
                                <fdev-item-label class="full-width ellipsis">
                                  {{ file.name }}
                                </fdev-item-label>
                                <fdev-item-label caption>
                                  {{ file.__sizeLabel }} /{{
                                    file.__progressLabel
                                  }}
                                </fdev-item-label>
                              </fdev-item-section>
                              <fdev-item-section
                                v-if="file.__img"
                                thumbnail
                                class="gt-xs"
                              >
                                <img :src="file.__img.src" />
                              </fdev-item-section>
                              <fdev-item-section top side>
                                <fdev-btn
                                  class="gt-xs delete"
                                  size="12px"
                                  flat
                                  dense
                                  round
                                  :icon="
                                    file.__status == 'uploaded'
                                      ? 'done'
                                      : 'delete'
                                  "
                                  @click="deleteUploadFile(scope, file)"
                                />
                              </fdev-item-section>
                            </fdev-item>
                          </fdev-list>
                        </div>
                      </div>
                    </template>
                  </fdev-uploader>
                </fdev-fab>
                <fdev-tooltip
                  v-if="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <span v-if="!compareTime()">当前变更已过期</span>
                  <span
                    v-if="
                      compareTime() &&
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                </fdev-tooltip>
              </span>
            </fdev-bar>
            <fdev-table
              :data="table.assets"
              :columns="columnsOutSide"
              :pagination.sync="pagination"
              no-export
              no-select-cols
            >
              <template v-slot:body="props">
                <fdev-tr :props="props">
                  <fdev-td auto-width>
                    <fdev-btn
                      dense
                      round
                      flat
                      :icon="
                        props.row.expand
                          ? 'expand_more'
                          : 'keyboard_arrow_right'
                      "
                      @click="props.row.expand = !props.row.expand"
                    />
                  </fdev-td>
                  <fdev-td class="text-ellipsis" :title="props.row.fileName">
                    {{ props.row.fileName }}
                  </fdev-td>
                  <fdev-td>
                    <router-link
                      v-if="props.row.upload_user"
                      :to="{ path: `/user/list/${props.row.upload_user}` }"
                      class="link"
                    >
                      <span>{{ props.row.upload_username_cn }}</span>
                    </router-link>
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.upload_time }}
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.source_application_name }}
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.source | source }}
                  </fdev-td>
                  <fdev-td>
                    <span>
                      <fdev-btn
                        flat
                        label="删除"
                        :disable="
                          !compareTime() ||
                            !(changesDetail.can_operation || isKaDianManager)
                        "
                        @click="deleteFiles(props.row.id)"
                      />
                      <fdev-tooltip
                        v-if="
                          !compareTime() ||
                            !(changesDetail.can_operation || isKaDianManager)
                        "
                      >
                        <span v-if="!compareTime()">当前变更已过期</span>
                        <span
                          v-if="
                            compareTime() &&
                              !(changesDetail.can_operation || isKaDianManager)
                          "
                        >
                          请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                        </span>
                      </fdev-tooltip>
                    </span>
                  </fdev-td>
                </fdev-tr>
                <fdev-tr v-show="props.row.expand" :props="props">
                  <fdev-td colspan="100%">
                    <fdev-table
                      v-if="table.catalog_type !== '10'"
                      :data="props.row.assets"
                      :columns="columns"
                      hide-pagination
                      :rows-per-page-options="['12']"
                      no-export
                      no-select-cols
                    >
                      <template v-slot:body-cell-filename="child">
                        <fdev-td class="text-ellipsis">
                          <a
                            class="text-blue-7 cursor-pointer"
                            target="_blank"
                            @click.stop="download(child.row)"
                            :title="props.row.fileName"
                            >{{ props.row.fileName }}</a
                          >
                        </fdev-td>
                      </template>
                    </fdev-table>
                    <fdev-table
                      v-else
                      :data="props.row.assets"
                      :columns="bastionColumns"
                      hide-pagination
                      :rows-per-page-options="['12']"
                      no-export
                      no-select-cols
                    >
                      <template v-slot:body-cell-filename="child">
                        <fdev-td class="text-ellipsis">
                          <a
                            class="text-blue-7 cursor-pointer"
                            target="_blank"
                            @click.stop="download(child.row)"
                            :title="props.row.fileName"
                            >{{ props.row.fileName }}</a
                          >
                        </fdev-td>
                      </template>
                    </fdev-table>
                  </fdev-td>
                </fdev-tr>
              </template>
            </fdev-table>
          </div>

          <div v-show="table.catalog_type === '6'" class="q-mt-xs">
            <fdev-bar class="bg-blue-grey-13 shadow-2">
              <div class="q-table__title text-white">
                <span class="q-mr-lg">介质目录：{{ table.catalog_name }}</span>
                <span>步骤：{{ table.description }}</span>
              </div>
              <fdev-space />
              <span>
                <fdev-fab
                  direction="left"
                  icon="cloud_upload"
                  color="bg-blue-grey-13"
                  :disable="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <fdev-uploader
                    :url="`${url}frelease/api/release/uploadAssets`"
                    :headers="[
                      {
                        name: 'Authorization',
                        value: $q.localStorage.getItem('fdev-vue-admin-jwt')
                      }
                    ]"
                    :form-fields="[
                      { name: 'asset_catalog_name', value: table.catalog_name },
                      { name: 'prod_id', value: id },
                      {
                        name: 'seq_no',
                        value:
                          table.assets.filter(data => {
                            return data.seq_no !== null;
                          }).length + 1
                      }
                    ]"
                    field-name="file"
                    style="max-width: 400px"
                    :multiple="false"
                    @uploaded="uploader"
                    ref="upload"
                    @failed="failed"
                  />
                </fdev-fab>
                <fdev-tooltip
                  v-if="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <span v-if="!compareTime()">当前变更已过期</span>
                  <span
                    v-if="
                      compareTime() &&
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                </fdev-tooltip>
              </span>
            </fdev-bar>
            <fdev-table
              :data="order(table.assets)"
              :columns="columnsWithIndex"
              :pagination.sync="pagination"
              no-export
              no-select-cols
            >
              <template v-slot:body-cell-index="props">
                <fdev-td>
                  {{ props.row.seq_no }}
                </fdev-td>
              </template>

              <template v-slot:body-cell-filename="props">
                <fdev-td class="text-ellipsis">
                  <a
                    class="text-blue-7 cursor-pointer"
                    target="_blank"
                    @click.stop="download(props.row)"
                    :title="props.row.fileName"
                    >{{ props.row.fileName }}</a
                  >
                </fdev-td>
              </template>

              <template v-slot:body-cell-source="props">
                <fdev-td>
                  {{ props.row.source | source }}
                </fdev-td>
              </template>

              <template v-slot:body-cell-upload_user="props">
                <fdev-td>
                  <router-link
                    v-if="props.row.upload_user"
                    :to="{ path: `/user/list/${props.row.upload_user}` }"
                    class="link"
                  >
                    <span>{{ props.row.upload_username_cn }}</span>
                  </router-link>
                </fdev-td>
              </template>

              <template v-slot:body-cell-btn="props">
                <fdev-td>
                  <span>
                    <fdev-btn
                      label="删除"
                      :disable="
                        !compareTime() ||
                          !(changesDetail.can_operation || isKaDianManager)
                      "
                      @click="deleteFile(props.row.id)"
                      flat
                    />
                    <fdev-tooltip
                      v-if="
                        !compareTime() ||
                          !(changesDetail.can_operation || isKaDianManager)
                      "
                    >
                      <span v-if="!compareTime()">当前变更已过期</span>
                      <span
                        v-if="
                          compareTime() &&
                            !(changesDetail.can_operation || isKaDianManager)
                        "
                      >
                        请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                      </span>
                    </fdev-tooltip>
                  </span>
                </fdev-td>
              </template>
            </fdev-table>
          </div>

          <div v-show="table.catalog_type === '7'" class="q-mt-xs">
            <fdev-bar class="bg-blue-grey-13 shadow-2">
              <div class="q-table__title text-white">
                <span class="q-mr-lg">介质目录：{{ table.catalog_name }}</span>
                <span>步骤：{{ table.description }}</span>
                <span
                  class="q-ml-lg"
                  v-if="
                    table.assets.length > 0 &&
                      compareTime() &&
                      (role || isKaDianManager)
                  "
                >
                  对象存储用户所属组：{{ table.group_name }}
                </span>
                <span
                  class="text-blue-9 cursor-pointer"
                  v-if="
                    table.assets.length > 0 &&
                      table.group_name === '' &&
                      compareTime() &&
                      (role || isKaDianManager)
                  "
                  @click="editGroupDialog(table)"
                >
                  选择小组
                </span>
              </div>
              <fdev-space />
              <span class="q-mr-md">
                <fdev-btn
                  flat
                  class="q-mr-sm"
                  ficon="edit"
                  :disable="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager) ||
                      table.assets.length === 0
                  "
                  @click="editGroupDialog(table)"
                />
                <fdev-tooltip
                  v-if="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager) ||
                      table.assets.length === 0
                  "
                >
                  <span v-if="!compareTime()">当前变更已过期</span>
                  <span
                    v-if="
                      compareTime() &&
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                  <span
                    v-if="
                      compareTime() &&
                        (changesDetail.can_operation || isKaDianManager) &&
                        table.assets.length === 0
                    "
                  >
                    当前介质目录暂无数据
                  </span>
                </fdev-tooltip>
              </span>
              <span class="q-mr-md">
                <fdev-fab
                  direction="left"
                  icon="cloud_upload"
                  ref="awsQfab"
                  @click="clickFab(table, index)"
                  @hide="hideUploader(table, index)"
                  @show="closeAwsZipQfab(index)"
                  color="bg-blue-grey-13"
                  :disable="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <template
                    #tooltip
                    v-if="
                      isShowAwsTooltip &&
                        compareTime() &&
                        (changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    <fdev-tooltip>上传普通文件</fdev-tooltip>
                  </template>
                  <fdev-uploader
                    :url="uploadUrl"
                    :headers="[
                      {
                        name: 'Authorization',
                        value: $q.localStorage.getItem('fdev-vue-admin-jwt')
                      }
                    ]"
                    :form-fields="[
                      { name: 'asset_catalog_name', value: table.catalog_name },
                      { name: 'prod_id', value: id },
                      { name: 'source_application', value: application },
                      { name: 'test', value: [] },
                      { name: 'bucket_name', value: '' },
                      { name: 'bucket_path', value: '' },
                      { name: 'type', value: '1' }
                    ]"
                    field-name="file"
                    style="max-width: 400px"
                    multiple
                    batch
                    @uploaded="uploader"
                    :ref="`upload${index}`"
                    @failed="failed"
                    @added="addFiles"
                    @finish="clearChildCatalog(table)"
                  >
                    <template v-slot:header="scope">
                      <div class="row no-wrap items-center q-pa-sm q-gutter-xs">
                        <fdev-btn
                          v-if="scope.queuedFiles.length > 0"
                          icon="clear_all"
                          round
                          dense
                          flat
                        />
                        <fdev-btn
                          v-if="scope.uploadedFiles.length > 0"
                          icon="done_all"
                          @click="finish(scope)"
                          round
                          dense
                          flat
                        />
                        <fdev-spinner
                          v-if="scope.isUploading"
                          class="q-uploader__spinner"
                        />
                        <div class="col">
                          <div class="q-uploader__title">
                            上传同名文档(需选择所有环境)
                          </div>
                        </div>
                        <fdev-btn
                          v-if="scope.canAddFiles"
                          icon="add_box"
                          round
                          dense
                          flat
                        >
                          <fdev-uploader-add-trigger class="hidden" />
                          <fdev-menu>
                            <fdev-list>
                              <fdev-item
                                v-for="run in objSaveEnv"
                                :key="run"
                                @click="
                                  pickFile(run, index, table.catalog_type)
                                "
                                v-show="
                                  hadSelect(run, scope, table.catalog_type)
                                "
                                clickable
                                v-close-popup
                              >
                                <fdev-item-section>{{ run }}</fdev-item-section>
                              </fdev-item>
                            </fdev-list>
                          </fdev-menu>
                        </fdev-btn>
                        <fdev-btn
                          v-if="
                            scope.editable &&
                              uploadIcon(scope.formFields[3].value)
                          "
                          icon="cloud_upload"
                          @click="uploadFile(scope, table)"
                          round
                          dense
                          flat
                        />
                        <fdev-btn
                          v-if="scope.editable && scope.isUploading"
                          icon="clear"
                          @click="scope.abort"
                          round
                          dense
                          flat
                        />
                      </div>
                    </template>

                    <template v-slot:list="scope">
                      <fdev-input
                        type="text"
                        flat
                        dense
                        ref="bucket_name"
                        label="桶名称"
                        v-model="bucketName"
                        :rules="[val => !!val || '请输入桶名称']"
                      />
                      <fdev-input
                        type="text"
                        flat
                        dense
                        label="桶内路径，如：dir/test"
                        ref="bucket_path"
                        v-model="bucketPath"
                      />
                      <div
                        class="uploader-content row justify-center"
                        v-if="
                          scope.formFields[3].value.length ===
                            scope.files.length && scope.files.length > 0
                        "
                      >
                        <fdev-icon
                          name="cloud_upload"
                          color="grey-5"
                          size="64px"
                          v-if="scope.formFields[3].value.length === 0"
                        />
                        <div class="col" v-else>
                          <fdev-list separator>
                            <fdev-item
                              v-for="(file, num) in scope.files"
                              :key="num"
                              bordered
                            >
                              <fdev-item-section side top>
                                <fdev-badge>{{ file.runtime_env }}</fdev-badge>
                                <fdev-icon
                                  name="warning"
                                  color="red"
                                  size="22px"
                                  class="warning"
                                  v-if="file.__status == 'failed'"
                                />
                              </fdev-item-section>
                              <fdev-item-section>
                                <fdev-item-label class="full-width ellipsis">
                                  {{ file.name }}
                                </fdev-item-label>
                                <fdev-item-label caption>
                                  {{ file.__sizeLabel }} /{{
                                    file.__progressLabel
                                  }}
                                </fdev-item-label>
                              </fdev-item-section>
                              <fdev-item-section
                                v-if="file.__img"
                                thumbnail
                                class="gt-xs"
                              >
                                <img :src="file.__img.src" />
                              </fdev-item-section>
                              <fdev-item-section top side>
                                <fdev-btn
                                  class="gt-xs delete"
                                  size="12px"
                                  flat
                                  dense
                                  round
                                  :icon="
                                    file.__status == 'uploaded'
                                      ? 'done'
                                      : 'delete'
                                  "
                                  @click="deleteUploadFile(scope, file)"
                                />
                              </fdev-item-section>
                            </fdev-item>
                          </fdev-list>
                        </div>
                      </div>
                      <div class="uploader-content row justify-center" v-else>
                        <fdev-icon
                          name="cloud_upload"
                          color="grey-5"
                          size="64px"
                          v-if="scope.formFields[3].value.length === 0"
                        />
                        <div class="col" v-else>
                          <fdev-list separator>
                            <fdev-item
                              v-for="(file, num) in scope.formFields[3].value"
                              :key="num"
                              bordered
                            >
                              <fdev-item-section side top>
                                <fdev-badge>{{ file.runtime_env }}</fdev-badge>
                                <fdev-icon
                                  name="warning"
                                  color="red"
                                  size="22px"
                                  class="warning"
                                  v-if="file.__status == 'failed'"
                                />
                              </fdev-item-section>
                              <fdev-item-section>
                                <fdev-item-label class="full-width ellipsis">
                                  {{ file.name }}
                                </fdev-item-label>
                                <fdev-item-label caption>
                                  {{ file.__sizeLabel }} /{{
                                    file.__progressLabel
                                  }}
                                </fdev-item-label>
                              </fdev-item-section>
                              <fdev-item-section
                                v-if="file.__img"
                                thumbnail
                                class="gt-xs"
                              >
                                <img :src="file.__img.src" />
                              </fdev-item-section>
                              <fdev-item-section top side>
                                <fdev-btn
                                  class="gt-xs delete"
                                  size="12px"
                                  flat
                                  dense
                                  round
                                  :icon="
                                    file.__status == 'uploaded'
                                      ? 'done'
                                      : 'delete'
                                  "
                                  @click="deleteUploadFile(scope, file)"
                                />
                              </fdev-item-section>
                            </fdev-item>
                          </fdev-list>
                        </div>
                      </div>
                    </template>
                  </fdev-uploader>
                </fdev-fab>
                <fdev-tooltip
                  v-if="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <span v-if="!compareTime()">当前变更已过期</span>
                  <span
                    v-if="
                      compareTime() &&
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                </fdev-tooltip>
              </span>

              <!-- AWS上传zip -->
              <span>
                <fdev-fab
                  direction="left"
                  use-ficon
                  icon="zip"
                  ref="awsZipQfab"
                  @click="clickFab(table, index)"
                  @hide="hideUploader(table, index, 'zip')"
                  @show="closeAwsQfab(index)"
                  color="bg-blue-grey-13"
                  :disable="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <template
                    #tooltip
                    v-if="
                      isShowAwsZipTooltip &&
                        compareTime() &&
                        (changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    <fdev-tooltip>上传文件夹</fdev-tooltip>
                  </template>
                  <fdev-uploader
                    :url="uploadUrl"
                    :headers="[
                      {
                        name: 'Authorization',
                        value: $q.localStorage.getItem('fdev-vue-admin-jwt')
                      }
                    ]"
                    :form-fields="[
                      { name: 'asset_catalog_name', value: table.catalog_name },
                      { name: 'prod_id', value: id },
                      { name: 'source_application', value: application },
                      { name: 'test', value: [] },
                      { name: 'bucket_name', value: '' },
                      { name: 'bucket_path', value: '' },
                      { name: 'type', value: '2' }
                    ]"
                    field-name="file"
                    style="max-width: 400px"
                    multiple
                    batch
                    accept=".zip"
                    @uploaded="uploader"
                    :ref="`uploadzip`"
                    @failed="failed"
                    @added="addZipFiles"
                    @finish="clearChildCatalog(table)"
                  >
                    <template v-slot:header="scope">
                      <div class="row no-wrap items-center q-pa-sm q-gutter-xs">
                        <fdev-spinner
                          v-if="scope.isUploading"
                          class="q-uploader__spinner"
                        />
                        <div class="col">
                          <div class="q-uploader__title">
                            必须以zip包格式上传文件夹，介质生成时，会将zip包解压后放入介质中(需选择所有环境且各环境zip包名相同)
                          </div>
                        </div>
                        <fdev-btn
                          v-if="
                            !(
                              scope.editable &&
                              uploadIcon(scope.formFields[3].value)
                            )
                          "
                          icon="add_box"
                          round
                          dense
                          flat
                        >
                          <fdev-uploader-add-trigger class="hidden" />
                          <fdev-menu>
                            <fdev-list>
                              <fdev-item
                                v-for="run in objSaveEnv"
                                :key="run"
                                @click="
                                  pickFile(
                                    run,
                                    index,
                                    table.catalog_type,
                                    'zip'
                                  )
                                "
                                v-show="
                                  hadSelect(run, scope, table.catalog_type)
                                "
                                clickable
                                v-close-popup
                              >
                                <fdev-item-section>{{ run }}</fdev-item-section>
                              </fdev-item>
                            </fdev-list>
                          </fdev-menu>
                        </fdev-btn>
                        <fdev-btn
                          v-if="
                            scope.editable &&
                              uploadIcon(scope.formFields[3].value)
                          "
                          icon="cloud_upload"
                          @click="uploadFile(scope, table, 'zip')"
                          round
                          dense
                          flat
                        />
                        <fdev-btn
                          v-if="scope.editable && scope.isUploading"
                          icon="clear"
                          @click="scope.abort"
                          round
                          dense
                          flat
                        />
                      </div>
                    </template>

                    <template v-slot:list="scope">
                      <fdev-input
                        type="text"
                        flat
                        dense
                        ref="bucket_name"
                        label="桶名称"
                        v-model="bucketName"
                        :rules="[val => !!val || '请输入桶名称']"
                      />
                      <fdev-input
                        type="text"
                        flat
                        dense
                        label="桶内路径，如：dir/test"
                        ref="bucket_path"
                        v-model="bucketPath"
                      />
                      <div
                        class="uploader-content row justify-center"
                        v-if="
                          scope.formFields[3].value.length ===
                            scope.files.length && scope.files.length > 0
                        "
                      >
                        <fdev-icon
                          name="cloud_upload"
                          color="grey-5"
                          size="64px"
                          v-if="scope.formFields[3].value.length === 0"
                        />
                        <div class="col" v-else>
                          <fdev-list separator>
                            <fdev-item
                              v-for="(file, num) in scope.files"
                              :key="num"
                              bordered
                            >
                              <fdev-item-section side top>
                                <fdev-badge>{{ file.runtime_env }}</fdev-badge>
                                <fdev-icon
                                  name="warning"
                                  color="red"
                                  size="22px"
                                  class="warning"
                                  v-if="file.__status == 'failed'"
                                />
                              </fdev-item-section>
                              <fdev-item-section>
                                <fdev-item-label class="full-width ellipsis">
                                  {{ file.name }}
                                </fdev-item-label>
                                <fdev-item-label caption>
                                  {{ file.__sizeLabel }} /{{
                                    file.__progressLabel
                                  }}
                                </fdev-item-label>
                              </fdev-item-section>
                              <fdev-item-section
                                v-if="file.__img"
                                thumbnail
                                class="gt-xs"
                              >
                                <img :src="file.__img.src" />
                              </fdev-item-section>
                              <fdev-item-section top side>
                                <fdev-btn
                                  class="gt-xs delete"
                                  size="12px"
                                  flat
                                  dense
                                  round
                                  :icon="
                                    file.__status == 'uploaded'
                                      ? 'done'
                                      : 'delete'
                                  "
                                  @click="deleteUploadFile(scope, file, 'zip')"
                                />
                              </fdev-item-section>
                            </fdev-item>
                          </fdev-list>
                        </div>
                      </div>
                      <div class="uploader-content row justify-center" v-else>
                        <fdev-icon
                          name="cloud_upload"
                          color="grey-5"
                          size="64px"
                          v-if="scope.formFields[3].value.length === 0"
                        />
                        <div class="col" v-else>
                          <fdev-list separator>
                            <fdev-item
                              v-for="(file, num) in scope.formFields[3].value"
                              :key="num"
                              bordered
                            >
                              <fdev-item-section side top>
                                <fdev-badge>{{ file.runtime_env }}</fdev-badge>
                                <fdev-icon
                                  name="warning"
                                  color="red"
                                  size="22px"
                                  class="warning"
                                  v-if="file.__status == 'failed'"
                                />
                              </fdev-item-section>
                              <fdev-item-section>
                                <fdev-item-label class="full-width ellipsis">
                                  {{ file.name }}
                                </fdev-item-label>
                                <fdev-item-label caption>
                                  {{ file.__sizeLabel }} /{{
                                    file.__progressLabel
                                  }}
                                </fdev-item-label>
                              </fdev-item-section>
                              <fdev-item-section
                                v-if="file.__img"
                                thumbnail
                                class="gt-xs"
                              >
                                <img :src="file.__img.src" />
                              </fdev-item-section>
                              <fdev-item-section top side>
                                <fdev-btn
                                  class="gt-xs delete"
                                  size="12px"
                                  flat
                                  dense
                                  round
                                  :icon="
                                    file.__status == 'uploaded'
                                      ? 'done'
                                      : 'delete'
                                  "
                                  @click="deleteUploadFile(scope, file, 'zip')"
                                />
                              </fdev-item-section>
                            </fdev-item>
                          </fdev-list>
                        </div>
                      </div>
                    </template>
                  </fdev-uploader>
                </fdev-fab>
                <fdev-tooltip
                  v-if="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <span v-if="!compareTime()">当前变更已过期</span>
                  <span
                    v-if="
                      compareTime() &&
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                </fdev-tooltip>
              </span>
            </fdev-bar>
            <fdev-table
              :data="table.assets"
              :columns="columnsObjSave"
              :pagination.sync="pagination"
              no-export
              no-select-cols
            >
              <template v-slot:body="props">
                <fdev-tr :props="props">
                  <fdev-td auto-width>
                    <fdev-btn
                      dense
                      round
                      flat
                      :icon="
                        props.row.expand
                          ? 'expand_more'
                          : 'keyboard_arrow_right'
                      "
                      @click="props.row.expand = !props.row.expand"
                    />
                  </fdev-td>
                  <fdev-td class="text-ellipsis" :title="props.row.fileName">
                    {{ props.row.fileName }}
                  </fdev-td>
                  <fdev-td>
                    <router-link
                      v-if="props.row.upload_user"
                      :to="{ path: `/user/list/${props.row.upload_user}` }"
                      class="link"
                    >
                      <span>{{ props.row.upload_username_cn }}</span>
                    </router-link>
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.upload_time }}
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.aws_type === '1' ? '文件' : '文件夹' }}
                  </fdev-td>
                  <fdev-td class="text-ellipsis" :title="props.row.bucket_name">
                    {{ props.row.bucket_name }}
                  </fdev-td>
                  <fdev-td class="text-ellipsis" :title="props.row.bucket_path">
                    {{ props.row.bucket_path || '-' }}
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.source | source }}
                  </fdev-td>
                  <fdev-td>
                    <span>
                      <fdev-btn
                        flat
                        label="删除"
                        :disable="
                          !compareTime() ||
                            !(changesDetail.can_operation || isKaDianManager)
                        "
                        @click="deleteFiles(props.row.id)"
                      />
                      <fdev-tooltip
                        v-if="
                          !compareTime() ||
                            !(changesDetail.can_operation || isKaDianManager)
                        "
                      >
                        <span v-if="!compareTime()">当前变更已过期</span>
                        <span
                          v-if="
                            compareTime() &&
                              !(changesDetail.can_operation || isKaDianManager)
                          "
                        >
                          请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                        </span>
                      </fdev-tooltip>
                    </span>
                  </fdev-td>
                </fdev-tr>
                <fdev-tr v-show="props.row.expand" :props="props">
                  <fdev-td colspan="100%">
                    <fdev-table
                      :data="props.row.assets"
                      :columns="columns"
                      hide-pagination
                      no-export
                      no-select-cols
                    >
                      <template v-slot:body-cell-filename="child">
                        <fdev-td class="text-ellipsis">
                          <a
                            class="text-blue-7 cursor-pointer"
                            target="_blank"
                            @click.stop="download(child.row)"
                            :title="props.row.fileName"
                            >{{ props.row.fileName }}</a
                          >
                        </fdev-td>
                      </template>
                    </fdev-table>
                  </fdev-td>
                </fdev-tr>
              </template>
            </fdev-table>
          </div>

          <div v-show="table.catalog_type === '8'" class="q-mt-xs">
            <fdev-bar class="bg-blue-grey-13 shadow-2">
              <div class="q-table__title text-white">
                <span class="q-mr-lg">介质目录：{{ table.catalog_name }}</span>
                <span>步骤：{{ table.description }}</span>
              </div>
              <fdev-space />
              <span>
                <fdev-btn
                  flat
                  ficon="add"
                  :disable="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                  class="cursor-pointer"
                  @click="addEsfUserInfo"
                />
                <fdev-tooltip
                  v-if="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <span v-if="!compareTime()">当前变更已过期</span>
                  <span
                    v-if="
                      compareTime() &&
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                </fdev-tooltip>
              </span>
            </fdev-bar>
            <fdev-table
              :data="table.assets"
              :columns="columnsEsf"
              :pagination.sync="pagination"
              no-export
              no-select-cols
            >
              <template v-slot:body="props">
                <fdev-tr :props="props">
                  <fdev-td auto-width>
                    <fdev-btn
                      dense
                      round
                      flat
                      :icon="
                        props.row.expand
                          ? 'expand_more'
                          : 'keyboard_arrow_right'
                      "
                      @click="props.row.expand = !props.row.expand"
                    />
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.application_cn || '-' }}
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.sid || '-' }}
                  </fdev-td>
                  <fdev-td>
                    {{
                      props.row.platform ? props.row.platform.join('，') : '-'
                    }}
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.caas_network_area || '-' }}
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.scc_network_area || '-' }}
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.upload_username_cn || '-' }}
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.update_time || '-' }}
                  </fdev-td>
                  <fdev-td>
                    <div class="q-gutter-x-sm row no-wrap">
                      <span>
                        <fdev-btn
                          flat
                          label="删除"
                          :disable="
                            !compareTime() ||
                              !(changesDetail.can_operation || isKaDianManager)
                          "
                          @click="deleteEsf(props.row)"
                        />
                        <fdev-tooltip
                          v-if="
                            !compareTime() ||
                              !(changesDetail.can_operation || isKaDianManager)
                          "
                        >
                          <span v-if="!compareTime()">当前变更已过期</span>
                          <span
                            v-if="
                              compareTime() &&
                                !(
                                  changesDetail.can_operation || isKaDianManager
                                )
                            "
                          >
                            请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                          </span>
                        </fdev-tooltip>
                      </span>
                      <span>
                        <fdev-btn
                          flat
                          label="编辑"
                          :disable="
                            !compareTime() ||
                              !(changesDetail.can_operation || isKaDianManager)
                          "
                          @click="editEsfUserInfo(props.row)"
                        />
                        <fdev-tooltip
                          v-if="
                            !compareTime() ||
                              !(changesDetail.can_operation || isKaDianManager)
                          "
                        >
                          <span v-if="!compareTime()">当前变更已过期</span>
                          <span
                            v-if="
                              compareTime() &&
                                !(
                                  changesDetail.can_operation || isKaDianManager
                                )
                            "
                          >
                            请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                          </span>
                        </fdev-tooltip>
                      </span>
                    </div>
                  </fdev-td>
                </fdev-tr>
                <fdev-tr v-show="props.row.expand" :props="props">
                  <fdev-td colspan="100%">
                    <fdev-table
                      :data="props.row.esfInfo"
                      :columns="columnsEsfInfo"
                      hide-pagination
                      no-export
                      no-select-cols
                    >
                      <template #header="thprops">
                        <fdev-tr :props="thprops">
                          <template v-for="col in thprops.cols">
                            <fdev-th
                              :key="col.name"
                              :props="thprops"
                              v-if="
                                props.row.esfInfo &&
                                  props.row.esfInfo[0][col.name]
                              "
                            >
                              {{ col.label }}
                            </fdev-th>
                          </template>
                        </fdev-tr>
                      </template>
                      <template v-slot:body-cell-clusterList="props">
                        <fdev-td :props="props" v-if="props.row.clusterList">
                          {{ props.row.clusterList.join(',') }}
                        </fdev-td>
                      </template>
                      <template v-slot:body-cell-config_area="props">
                        <fdev-td :props="props" v-if="props.row.config_area">
                          <div class="column full-width">
                            <div
                              class="full-width overflow ellipsis"
                              v-if="props.row.config_area.k1"
                              :title="
                                `${props.row.config_area.k1.partition}(${
                                  props.row.config_area.k1.config_area
                                })`
                              "
                            >
                              K1:{{
                                `${props.row.config_area.k1.partition}(${
                                  props.row.config_area.k1.config_area
                                })`
                              }}
                            </div>
                            <div
                              v-if="props.row.config_area.k2"
                              class="full-width overflow ellipsis"
                              :title="
                                `${props.row.config_area.k2.partition}(${
                                  props.row.config_area.k2.config_area
                                })`
                              "
                            >
                              K2:{{
                                `${props.row.config_area.k2.partition}(${
                                  props.row.config_area.k2.config_area
                                })`
                              }}
                            </div>
                          </div>
                        </fdev-td>
                      </template>
                    </fdev-table>
                  </fdev-td>
                </fdev-tr>
              </template>
            </fdev-table>
          </div>

          <div
            v-show="table.catalog_type === '9'"
            class="table q-mt-xs q-mb-md"
          >
            <fdev-bar class="bg-blue-grey-13 shadow-2">
              <div class="q-table__title text-white">
                <span class="q-mr-lg">介质目录：{{ table.catalog_name }}</span>
                <span>步骤：{{ table.description }}</span>
              </div>
              <fdev-space />
              <span class="q-mr-md">
                <fdev-btn
                  flat
                  ficon="gitlab"
                  :disable="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager) ||
                      !systemDetailByProdId.resource_giturl
                  "
                  @click="
                    openRuntimeDia(table.catalog_name, table.catalog_type)
                  "
                />
                <fdev-tooltip
                  v-if="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager) ||
                      !systemDetailByProdId.resource_giturl
                  "
                >
                  <span v-if="!compareTime()">当前变更已过期</span>
                  <span
                    v-if="
                      compareTime() &&
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                  <span
                    v-if="
                      compareTime() &&
                        (changesDetail.can_operation || isKaDianManager) &&
                        !systemDetailByProdId.resource_giturl
                    "
                  >
                    没有gitlab地址
                  </span>
                </fdev-tooltip>
              </span>
              <span>
                <fdev-fab
                  direction="left"
                  icon="cloud_upload"
                  @click="clickFab(table, index)"
                  @hide="hideUploader(table, index)"
                  color="bg-blue-grey-13"
                  :disable="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <fdev-uploader
                    :url="uploadUrl"
                    :headers="[
                      {
                        name: 'Authorization',
                        value: $q.localStorage.getItem('fdev-vue-admin-jwt')
                      }
                    ]"
                    :form-fields="[
                      { name: 'asset_catalog_name', value: table.catalog_name },
                      { name: 'prod_id', value: id },
                      { name: 'source_application', value: application },
                      { name: 'test', value: [] },
                      { name: 'child_catalog', value: '' }
                    ]"
                    field-name="file"
                    style="max-width: 400px"
                    multiple
                    batch
                    @uploaded="uploader"
                    :ref="`upload${index}`"
                    @failed="failed"
                    @added="addFiles"
                    @finish="clearChildCatalog(table)"
                  >
                    <template v-slot:header="scope">
                      <div class="row no-wrap items-center q-pa-sm q-gutter-xs">
                        <fdev-btn
                          v-if="scope.queuedFiles.length > 0"
                          icon="clear_all"
                          round
                          dense
                          flat
                        />
                        <fdev-btn
                          v-if="scope.uploadedFiles.length > 0"
                          icon="done_all"
                          @click="finish(scope)"
                          round
                          dense
                          flat
                        />
                        <fdev-spinner
                          v-if="scope.isUploading"
                          class="q-uploader__spinner"
                        />
                        <div class="col">
                          <div class="q-uploader__title">
                            上传同名文档(需选择所有环境)
                          </div>
                        </div>
                        <fdev-btn
                          v-if="scope.canAddFiles"
                          icon="add_box"
                          round
                          dense
                          flat
                        >
                          <fdev-uploader-add-trigger class="hidden" />
                          <fdev-menu>
                            <fdev-list>
                              <fdev-item
                                v-for="run in dynamicConfigEnv"
                                :key="run"
                                @click="
                                  pickFile(run, index, table.catalog_type)
                                "
                                v-show="
                                  hadSelect(run, scope, table.catalog_type)
                                "
                                clickable
                                v-close-popup
                              >
                                <fdev-item-section>{{ run }}</fdev-item-section>
                              </fdev-item>
                            </fdev-list>
                          </fdev-menu>
                        </fdev-btn>
                        <fdev-btn
                          v-if="
                            scope.editable &&
                              uploadIcon(scope.formFields[3].value)
                          "
                          icon="cloud_upload"
                          @click="uploadFile(scope, table)"
                          round
                          dense
                          flat
                        />
                        <fdev-btn
                          v-if="scope.editable && scope.isUploading"
                          icon="clear"
                          @click="scope.abort"
                          round
                          dense
                          flat
                        />
                      </div>
                    </template>
                    <template v-slot:list="scope">
                      <fdev-input
                        type="text"
                        flat
                        dense
                        label="子目录名称"
                        v-model="childCatalog"
                      />
                      <div
                        class="uploader-content row justify-center"
                        v-if="
                          scope.formFields[3].value.length ===
                            scope.files.length && scope.files.length > 0
                        "
                      >
                        <fdev-icon
                          name="cloud_upload"
                          color="grey-5"
                          size="64px"
                          v-if="scope.formFields[3].value.length === 0"
                        />
                        <div class="col" v-else>
                          <fdev-list separator>
                            <fdev-item
                              v-for="(file, num) in scope.files"
                              :key="num"
                              bordered
                            >
                              <fdev-item-section side top>
                                <fdev-badge>{{ file.runtime_env }}</fdev-badge>
                                <fdev-icon
                                  name="warning"
                                  color="red"
                                  size="22px"
                                  class="warning"
                                  v-if="file.__status == 'failed'"
                                />
                              </fdev-item-section>
                              <fdev-item-section>
                                <fdev-item-label class="full-width ellipsis">
                                  {{ file.name }}
                                </fdev-item-label>
                                <fdev-item-label caption>
                                  {{ file.__sizeLabel }} /{{
                                    file.__progressLabel
                                  }}
                                </fdev-item-label>
                              </fdev-item-section>
                              <fdev-item-section
                                v-if="file.__img"
                                thumbnail
                                class="gt-xs"
                              >
                                <img :src="file.__img.src" />
                              </fdev-item-section>
                              <fdev-item-section top side>
                                <fdev-btn
                                  class="gt-xs delete"
                                  size="12px"
                                  flat
                                  dense
                                  round
                                  :icon="
                                    file.__status == 'uploaded'
                                      ? 'done'
                                      : 'delete'
                                  "
                                  @click="deleteUploadFile(scope, file)"
                                />
                              </fdev-item-section>
                            </fdev-item>
                          </fdev-list>
                        </div>
                      </div>
                      <div class="uploader-content row justify-center" v-else>
                        <fdev-icon
                          name="cloud_upload"
                          color="grey-5"
                          size="64px"
                          v-if="scope.formFields[3].value.length === 0"
                        />
                        <div class="col" v-else>
                          <fdev-list separator>
                            <fdev-item
                              v-for="(file, num) in scope.formFields[3].value"
                              :key="num"
                              bordered
                            >
                              <fdev-item-section side top>
                                <fdev-badge>{{ file.runtime_env }}</fdev-badge>
                                <fdev-icon
                                  name="warning"
                                  color="red"
                                  size="22px"
                                  class="warning"
                                  v-if="file.__status == 'failed'"
                                />
                              </fdev-item-section>
                              <fdev-item-section>
                                <fdev-item-label class="full-width ellipsis">
                                  {{ file.name }}
                                </fdev-item-label>
                                <fdev-item-label caption>
                                  {{ file.__sizeLabel }} /{{
                                    file.__progressLabel
                                  }}
                                </fdev-item-label>
                              </fdev-item-section>
                              <fdev-item-section
                                v-if="file.__img"
                                thumbnail
                                class="gt-xs"
                              >
                                <img :src="file.__img.src" />
                              </fdev-item-section>
                              <fdev-item-section top side>
                                <fdev-btn
                                  class="gt-xs delete"
                                  size="12px"
                                  flat
                                  dense
                                  round
                                  :icon="
                                    file.__status == 'uploaded'
                                      ? 'done'
                                      : 'delete'
                                  "
                                  @click="deleteUploadFile(scope, file)"
                                />
                              </fdev-item-section>
                            </fdev-item>
                          </fdev-list>
                        </div>
                      </div>
                    </template>
                  </fdev-uploader>
                </fdev-fab>
                <fdev-tooltip
                  v-if="
                    !compareTime() ||
                      !(changesDetail.can_operation || isKaDianManager)
                  "
                >
                  <span v-if="!compareTime()">当前变更已过期</span>
                  <span
                    v-if="
                      compareTime() &&
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                  </span>
                </fdev-tooltip>
              </span>
            </fdev-bar>
            <fdev-table
              :data="table.assets"
              :columns="columnsOutSide"
              :pagination.sync="pagination"
              no-export
              no-select-cols
            >
              <template v-slot:body="props">
                <fdev-tr :props="props">
                  <fdev-td auto-width>
                    <fdev-btn
                      dense
                      round
                      flat
                      :icon="
                        props.row.expand
                          ? 'expand_more'
                          : 'keyboard_arrow_right'
                      "
                      @click="props.row.expand = !props.row.expand"
                    />
                  </fdev-td>
                  <fdev-td class="text-ellipsis" :title="props.row.fileName">
                    {{ props.row.fileName }}
                  </fdev-td>
                  <fdev-td>
                    <router-link
                      v-if="props.row.upload_user"
                      :to="{ path: `/user/list/${props.row.upload_user}` }"
                      class="link"
                    >
                      <span>{{ props.row.upload_username_cn }}</span>
                    </router-link>
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.upload_time }}
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.source_application_name }}
                  </fdev-td>
                  <fdev-td>
                    {{ props.row.source | source }}
                  </fdev-td>
                  <fdev-td>
                    <span>
                      <fdev-btn
                        flat
                        label="删除"
                        :disable="
                          !compareTime() ||
                            !(changesDetail.can_operation || isKaDianManager)
                        "
                        @click="deleteFiles(props.row.id)"
                      />
                      <fdev-tooltip
                        v-if="
                          !compareTime() ||
                            !(changesDetail.can_operation || isKaDianManager)
                        "
                      >
                        <span v-if="!compareTime()">当前变更已过期</span>
                        <span
                          v-if="
                            compareTime() &&
                              !(changesDetail.can_operation || isKaDianManager)
                          "
                        >
                          请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                        </span>
                      </fdev-tooltip>
                    </span>
                  </fdev-td>
                </fdev-tr>
                <fdev-tr v-show="props.row.expand" :props="props">
                  <fdev-td colspan="100%">
                    <fdev-table
                      :data="props.row.assets"
                      :columns="columns"
                      hide-pagination
                      :rows-per-page-options="['12']"
                      no-export
                      no-select-cols
                    >
                      <template v-slot:body-cell-filename="child">
                        <fdev-td class="text-ellipsis">
                          <a
                            class="text-blue-7 cursor-pointer"
                            target="_blank"
                            @click.stop="download(child.row)"
                            :title="props.row.fileName"
                            >{{ props.row.fileName }}</a
                          >
                        </fdev-td>
                      </template>
                    </fdev-table>
                  </fdev-td>
                </fdev-tr>
              </template>
            </fdev-table>
          </div>
        </div>
      </div>
      <div class="text-center q-mt-md" v-else>
        <f-image name="no_data" />
      </div>
    </div>
    <!-- 非自动化发布 -->
    <div class="bg-white" v-else>
      <div class="q-mb-md">
        <fdev-table
          :data="deAutoAssetsData"
          :columns="columnDeAuto"
          :pagination.sync="deAutoPagination"
          no-export
          no-select-cols
        >
          <template v-slot:body="props">
            <fdev-tr :props="props">
              <fdev-td class="text-ellipsis" :title="props.row.fileName">
                {{ props.row.fileName }}
              </fdev-td>
              <fdev-td>
                <router-link
                  v-if="props.row.upload_user"
                  :to="{ path: `/user/list/${props.row.upload_user}` }"
                  class="link"
                >
                  <span>{{ props.row.upload_username_cn }}</span>
                </router-link>
              </fdev-td>
              <fdev-td>
                {{ props.row.upload_time }}
              </fdev-td>
              <fdev-td>
                {{ props.row.source | source }}
              </fdev-td>
              <fdev-td>
                <span>
                  <fdev-btn
                    label="删除"
                    :disable="
                      !compareTime() ||
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                    @click="deleteFiles(props.row.id.split(','))"
                    flat
                  />
                  <fdev-tooltip
                    v-if="
                      !compareTime() ||
                        !(changesDetail.can_operation || isKaDianManager)
                    "
                  >
                    <span v-if="!compareTime()">当前变更已过期</span>
                    <span
                      v-if="
                        compareTime() &&
                          !(changesDetail.can_operation || isKaDianManager)
                      "
                    >
                      请联系当前变更所属组成员的第三层级组及其子组的投产管理员
                    </span>
                  </fdev-tooltip>
                </span>
              </fdev-td>
            </fdev-tr>
          </template>
          <template v-slot:top-right>
            <fdev-fab
              direction="left"
              icon="cloud_upload"
              color="primary"
              flat
              dense
              v-if="compareTime() && (role || isKaDianManager)"
            >
              <fdev-uploader
                :url="`${url}frelease/api/release/deAutoUpload`"
                :headers="[
                  {
                    name: 'Authorization',
                    value: $q.localStorage.getItem('fdev-vue-admin-jwt')
                  }
                ]"
                :form-fields="[{ name: 'prod_id', value: deAutoProdId }]"
                field-name="file"
                style="max-width: 400px"
                :multiple="false"
                @uploaded="uploader"
                ref="upload"
                @failed="failed"
              >
              </fdev-uploader>
            </fdev-fab>
          </template>
        </fdev-table>
        <fdev-separator />
      </div>
    </div>

    <!-- 修改对象存储用户所属组弹窗 -->
    <f-dialog v-model="showGroupDialog" f-sl-sc title="修改对象存储用户所属组">
      <f-formitem label="选择小组" diaS>
        <fdev-select
          v-model="editedGroup"
          ref="objSaveGroupName"
          :options="awsGroups"
          option-label="group_name"
          :rules="[val => !!val || '请选择小组']"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn label="确定" dialog @click="updateObjSaveGroup" />
      </template>
    </f-dialog>

    <SelectForRuntime
      v-model="runtimeDia"
      :btnLoading="globalLoading['releaseForm/addFile']"
      :source_application="application"
      :prod_id="id"
      :prodType="changesDetail.type"
      :catalogType="catalogType"
      :catalogName="asset_catalog_name"
      @click="handleGitlabFile"
    />
    <SearchBranch
      title="添加gitlab配置文件"
      v-model="branchDia"
      :btnLoading="globalLoading['releaseForm/addFile']"
      :source_application="application"
      :prod_id="id"
      @click="handleGitlabFile"
    />

    <!-- esf信息新增/编辑弹窗 -->
    <EsfUserInfo
      v-model="esfUserInfoDia"
      :type="esfUserInfoDiaType"
      :curEditData="curEditEsfUserInfoData"
      @confirm="submitEsfUserInfo"
    />

    <!-- esfcommonconfig信息新增/编辑弹窗 -->
    <EsfCommonConfig
      v-model="esfCommonConfigDia"
      :type="esfCommonConfigDiaType"
      :curEditData="curEditEsfCommonConfigData"
      @confirm="submitEsfCommonConfigInfo"
    />

    <DataDialogConfig
      v-model="dataDialog"
      :prod_id="id"
      @click="handleQueryProfile"
    />
  </div>
</template>

<script>
import {
  fileConfigColumns,
  fileConfigBastionColumns,
  fileConfigOutSideColunms,
  objSaveColumns,
  withoutFileColumns,
  deAutoColumns,
  esfColumns,
  esfCommonConfigColumns,
  esfInfoColumns,
  esfCommonConfigInfoColumns
} from '../../utils/constants';
import DataDialogConfig from './components/dataDialogConfig';
import SearchBranch from './components/searchBranch';
import SelectForRuntime from './components/selectForRuntime';
import EsfUserInfo from './components/esfUserInfo';
import EsfCommonConfig from './components/esfCommonConfigDialog';
import {
  baseUrl,
  successNotify,
  errorNotify,
  isValidReleaseDate
} from '@/utils/utils';
import { mapActions, mapState, mapGetters } from 'vuex';
import { ImageDeliverType } from '../../utils/model.js';

export default {
  name: 'FileUpload',
  components: {
    SearchBranch,
    SelectForRuntime,
    EsfUserInfo,
    EsfCommonConfig,
    DataDialogConfig
  },
  data() {
    return {
      id: '',
      url: baseUrl,
      tableData: [],
      branchDia: false,
      dataDialog: false,
      runtimeDia: false,
      index: 0,
      asset_catalog_name: '',
      pickFileTypeName: '',
      catalogType: '',
      filterData: [],
      runEvn: [],
      commonconfigPlatform: 'CAAS', // commonconfig介质目录部署平台
      child_catalog: '',
      runtime_env: ['DEV', 'TEST', 'PROCSH', 'PROCHF'],
      objSaveEnv: ['DEV', 'PROC'],
      showGroupDialog: false,
      editedGroup: null,
      awsObj: null,
      bucketName: '', // 桶名称
      bucketPath: '', // 桶路径
      childCatalog: '', // 子目录名称
      isShowAwsUploafer: false, // AWS介质目录的上传器是否显示
      isShowAwsZipTooltip: true,
      isShowAwsTooltip: true,
      deAutoAssetsData: [],
      ImageDeliverType,
      deAutoProdId: this.$route.params.id,
      columns: fileConfigColumns,
      bastionColumns: fileConfigBastionColumns,
      columnsOutSide: fileConfigOutSideColunms,
      columnsObjSave: objSaveColumns,
      columnsWithout: withoutFileColumns,
      columnDeAuto: deAutoColumns,
      columnsEsf: esfColumns,
      columnsEsfInfo: esfInfoColumns,
      columnsEsfCommonConfig: esfCommonConfigColumns,
      columnsEsfCommonConfigInfo: esfCommonConfigInfoColumns,
      pagination: {
        rowsPerPage: 0
      },
      deAutoPagination: {},
      esfUserInfoDia: false, // esf用户信息弹窗
      esfUserInfoDiaType: '', // esf用户信息弹窗（添加或修改-add/edit）
      curEditEsfUserInfoData: null, // 记录编辑的是哪条esf用户信息数据
      esfCommonConfigDia: false, // esfCommonConfig弹窗
      esfCommonConfigDiaType: '', // esfCommonConfig弹窗（添加或修改-add/edit）
      curEditEsfCommonConfigData: null // 记录编辑的是哪条esfCommonConfig数据
    };
  },
  watch: {
    configureData(val) {
      this.tableData = val;
    },
    '$route.query.template_id': {
      deep: true,
      handler(val, oldVal) {
        this.getAssets();
      }
    }
  },
  computed: {
    ...mapState('releaseForm', {
      configureData: 'assetsList',
      deAutoAssets: 'deAutoAssets',
      changesDetail: 'changesDetail',
      systemDetailByProdId: 'systemDetailByProdId',
      awsGroups: 'awsGroups'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    ...mapGetters('user', {
      isKaDianManager: 'isKaDianManager'
    }),

    application() {
      return this.$route.query.application_id
        ? this.$route.query.application_id
        : null;
    },

    role() {
      return this.$q.sessionStorage.getItem('releaseRole');
    },

    columnsWithIndex() {
      const columns = [...this.columnsWithout];
      columns.splice(0, 0, {
        name: 'index',
        label: '序号',
        field: 'index'
      });
      return columns;
    },

    // esf介质目录子表加“集群”列（当用户上送的网络区域为“overlay”时）
    computedColumnsEsfInfo() {
      let columnsEsfInfo = this.columnsEsfInfo.slice();
      columnsEsfInfo.splice(3, 0, {
        name: 'clusterList',
        label: '集群',
        field: row => row.clusterList.join(',')
      });
      return columnsEsfInfo;
    },

    // dynamicconfig介质目录上传文件的环境，区分变更类型（生产/灰度）
    dynamicConfigEnv() {
      let result;
      if (this.changesDetail.type === 'gray') {
        result = ['DEV', 'TEST', 'shanghaiHd', 'hefeiHd'];
      } else {
        result = [
          'DEV',
          'TEST',
          'shanghaiK1',
          'shanghaiK2',
          'hefeiK1',
          'hefeiK2'
        ];
      }
      return result;
    },
    commonconfigEnv() {
      let result;
      if (this.catalogType === '10') {
        result = ['DEV', 'TEST', 'PROCSH', 'PROCHF'];
      } else {
        if (this.commonconfigPlatform === 'CAAS') {
          result = ['DEV', 'TEST', 'PROCSH', 'PROCHF'];
        } else if (this.commonconfigPlatform === 'SCC') {
          if (this.changesDetail.type === 'gray') {
            result = ['DEVSCC', 'TESTSCC', 'PROCSHSCCHD', 'PROCHFSCCHD'];
          } else {
            result = [
              'DEVSCC',
              'TESTSCC',
              'PROCSHSCCK1',
              'PROCSHSCCK2',
              'PROCHFSCCK1',
              'PROCHFSCCK2'
            ];
          }
        }
      }
      return result;
    }
  },
  filters: {
    source(val) {
      if (val === '3') return 'fdev生成';
      return val === '1' ? '上传' : 'gitlab选择';
    }
  },
  methods: {
    ...mapActions('releaseForm', {
      deleteAFile: 'deleteFile',
      addFile: 'addFile',
      deleteFileArr: 'deleteFileArr',
      getAssetsList: 'getAssetsList',
      queryDeAutoAssets: 'queryDeAutoAssets',
      queryResourceProjects: 'queryResourceProjects',
      getTemplateDetail: 'getTemplateDetail',
      querySystemDetailByProdId: 'querySystemDetailByProdId',
      queryChangesDetail: 'queryChangesDetail',
      updateAwsAssetGroupId: 'updateAwsAssetGroupId',
      queryAwsGroups: 'queryAwsGroups',
      delEsf: 'delEsf',
      getChangeApplications: 'getChangeApplications',
      queryProfile: 'queryProfile' //获取配置文件
    }),
    ...mapActions('jobForm', ['downExcel']),

    // 下载文件
    async download(data, index) {
      if (data.source !== '1') {
        window.open(data.file_giturl, '_blank');
      } else {
        let params = {
          path:
            index === undefined ? data.file_giturl : data.files_giturl[index],
          moduleName: 'fdev-release'
        };
        await this.downExcel(params);
      }
    },

    // 获取并封装接口返回数据，用于渲染页面
    async getAssets() {
      await this.getAssetsList({ prod_id: this.id });
      // 所有下拉列表的数据
      this.tableData = this.configureData.reverse();
      this.filterData = this.tableData.map(item => {
        if (
          (item.catalog_type === '5' &&
            item.catalog_name !== 'commonconfig' &&
            item.catalog_name !== 'esfcommonconfig') ||
          item.catalog_type === '9'
        ) {
          return {
            ...item,
            runEvn: [],
            assets: this.classify(item.assets, item.catalog_type),
            child_catalog: ''
          };
        } else if (
          item.catalog_type === '5' &&
          item.catalog_name === 'commonconfig'
        ) {
          return {
            ...item,
            runEvn: [],
            assets: this.classify(item.assets, 'commonconfig'),
            child_catalog: ''
          };
        } else if (item.catalog_type === '10') {
          return {
            ...item,
            runEvn: [],
            assets: this.classify(item.assets, 'bastioncommonconfig'),
            child_catalog: ''
          };
        } else if (
          item.catalog_type === '5' &&
          item.catalog_name === 'esfcommonconfig'
        ) {
          return {
            ...item,
            list: this.classify(item.assets, 'esfcommonconfig')
          };
        } else if (item.catalog_type === '7') {
          return {
            ...item,
            runEvn: [],
            assets: this.classify(item.assets),
            bucket_name: '',
            bucket_path: ''
          };
        } else if (item.catalog_type === '8') {
          return {
            ...item,
            assets: this.classify(item.esfUserList, '8')
          };
        } else {
          return { ...item };
        }
      });
    },

    async getDeAutoAssets() {
      await this.queryDeAutoAssets({ prod_id: this.id });
      this.deAutoAssetsData = this.deAutoAssets[0]['assets'];
    },

    deleteFile(params) {
      this.$q
        .dialog({
          title: '确认删除',
          message: '您确定删除该文件吗?',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteAFile({ id: params });
          successNotify('删除成功');
          this.getAssets();
        });
    },

    // 删除介质目录数据
    deleteFiles(params) {
      this.$q
        .dialog({
          title: '确认删除',
          message: '您确定删除该文件吗?',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.deleteFileArr({ ids: params });
          successNotify('删除成功');
          this.changesDetail.image_deliver_type === '1'
            ? this.getAssets()
            : this.getDeAutoAssets();
        });
    },

    // 触发上传器的uploaded事件
    uploader(info) {
      const respense = JSON.parse(info.xhr.response);
      if (respense.code === 'AAAAAAA') {
        successNotify('上传成功');
        this.changesDetail.image_deliver_type === '1'
          ? this.getAssets()
          : this.getDeAutoAssets();
      } else {
        errorNotify(respense.msg);
        this.$refs.upload.forEach(item => {
          item.reset();
        });
      }
      this.$emit('submit', false);
    },

    // 触发上传器的failed事件
    failed(info) {
      const response = JSON.parse(info.xhr.response);
      if (response.status === 401) {
        this.$router.push('/login');
      }
      errorNotify(response.message);
    },

    // 判断变更是否过期
    compareTime() {
      return isValidReleaseDate(this.$q.sessionStorage.getItem('changeTime'));
    },

    async handleQueryProfile(data) {
      const params = {
        ...data,
        prod_id: this.id
      };
      await this.queryProfile(params);
      this.dataDialog = false;
      successNotify('更新成功');
      this.getAssets();
    },

    async openDataDialog() {
      // 获取变更应用列表
      await this.getChangeApplications({
        prod_id: this.id
      });
      this.dataDialog = true;
    },

    // 打开config介质目录的“添加gitlab配置文件”弹窗
    async openDialog(name) {
      this.asset_catalog_name = name;
      // 获取配置文件地址
      await this.queryResourceProjects({
        prod_id: this.id
      });
      this.branchDia = true;
    },

    // 打开“添加gitlab配置文件”弹窗
    async openRuntimeDia(name, type) {
      this.asset_catalog_name = name;
      this.catalogType = type;
      if (type === '9') {
        await this.queryResourceProjects({
          prod_id: this.id,
          configType: 'dynamicconfig'
        });
      } else {
        await this.queryResourceProjects({
          prod_id: this.id
        });
      }
      this.runtimeDia = true;
    },

    async handleGitlabFile(data) {
      const params = {
        ...data,
        asset_catalog_name: this.asset_catalog_name
      };
      await this.addFile(params);
      this.branchDia = false;
      this.runtimeDia = false;
      this.getAssets();
    },

    // 打开文件资源管理器，完成文件选择
    pickFile(type, index, catalogType, fileType) {
      const params = `upload${index}`;
      this.index = index;
      this.pickFileTypeName = type;
      this.catalogType = catalogType;
      if (catalogType === '7') {
        if (fileType === 'zip') {
          this.$refs.uploadzip[index].files = [];
          this.$refs.uploadzip[index].pickFiles();
        } else {
          this.$refs[params][1].files = [];
          this.$refs[params][1].pickFiles();
        }
      } else if (catalogType === '9') {
        this.$refs[params][2].files = [];
        this.$refs[params][2].pickFiles();
      } else {
        this.$refs[params][0].files = [];
        this.$refs[params][0].pickFiles();
      }
    },

    // 触发上传器的added事件，添加待上传文件数据
    addFiles(file) {
      const params = `upload${this.index}`;
      file[0].runtime_env = this.pickFileTypeName;
      const index =
        this.catalogType === '7' ? 1 : this.catalogType === '9' ? 2 : 0;
      if (this.$refs[params][index].formFields[3].value.length > 0) {
        if (
          this.$refs[params][index].formFields[3].value[0].name ===
            file[0].name &&
          this.catalogType !== '10'
        ) {
          this.$refs[params][index].formFields[3].value.push(file[0]);
        } else if (
          !this.$refs[params][index].formFields[3].value.find(
            v =>
              v.name === file[0].name && v.runtime_env === file[0].runtime_env
          ) &&
          this.catalogType === '10'
        ) {
          this.$refs[params][index].formFields[3].value.push(file[0]);
        }
      } else {
        this.$refs[params][index].formFields[3].value.push(file[0]);
      }
      this.$refs[params][index].files = this.$refs[params][
        index
      ].formFields[3].value;
    },

    // 触发上传器的added事件，AWS添加待上传zip压缩包文件数据
    addZipFiles(file) {
      file[0].runtime_env = this.pickFileTypeName;
      if (this.$refs.uploadzip[this.index].formFields[3].value.length > 0) {
        if (
          this.$refs.uploadzip[this.index].formFields[3].value[0].name ===
          file[0].name
        ) {
          this.$refs.uploadzip[this.index].formFields[3].value.push(file[0]);
        }
      } else {
        this.$refs.uploadzip[this.index].formFields[3].value.push(file[0]);
      }
      this.$refs.uploadzip[this.index].files = this.$refs.uploadzip[
        this.index
      ].formFields[3].value;
    },

    // 点击上传器上传图标按钮，执行上传动作
    uploadFile(scope, table, fileType) {
      scope.queuedFiles = scope.files.map(item => {
        let newFile = new File([item], `${item.name}/${item.runtime_env}`, {
          type: item.type
        });
        newFile.__progress = item.__progress;
        newFile.__progressLabel = item.__progressLabel;
        newFile.__sizeLabel = item.__sizeLabel;
        newFile.__status = item.__status;
        newFile.__uploaded = item.__uploaded;
        return newFile;
      });
      if (table.catalog_type === '7') {
        scope.formFields[4].value = this.bucketName;
        scope.formFields[5].value = this.bucketPath;
        const targetVm = scope.$children.filter(vm => vm.$refs.input)[0];
        targetVm.value ? scope.upload() : targetVm.validate();
      } else {
        scope.formFields[4].value = this.childCatalog;
        scope.upload();
      }
    },

    // 点击上传器内删除图标按钮，删除对应已选文件
    deleteUploadFile(scope, file, fileType) {
      const params = `upload${this.index}`;
      if (scope.files.length === 0) {
        scope.files = scope.formFields[3].value;
      }
      scope.files = scope.files.filter(item => {
        return item !== file;
      });
      if (this.catalogType === '7') {
        if (fileType === 'zip') {
          scope.formFields[3].value = this.$refs.uploadzip[this.index].files;
        } else {
          scope.formFields[3].value = this.$refs[params][1].files;
        }
      } else if (this.catalogType === '9') {
        scope.formFields[3].value = this.$refs[params][2].files;
      } else {
        scope.formFields[3].value = this.$refs[params][0].files;
      }
    },

    uploadIcon(data) {
      const type = data.map(item => {
        return item.runtime_env;
      });
      if (this.catalogType === '7') {
        return Array.from(new Set(type)).length === this.objSaveEnv.length;
      } else if (this.catalogType === '9') {
        return (
          Array.from(new Set(type)).length === this.dynamicConfigEnv.length
        );
      } else if (this.catalogType === '5') {
        return Array.from(new Set(type)).length === this.commonconfigEnv.length;
      } else if (this.catalogType === '10') {
        return Array.from(new Set(type)).length > 0;
      } else {
        return Array.from(new Set(type)).length === this.runtime_env.length;
      }
    },

    hadSelect(data, scope, catalogType) {
      if (catalogType === '10') {
        return true;
      }
      const arr = scope.formFields[3].value.map(item => {
        return item.runtime_env;
      });
      if (scope.formFields[3].value.length > 0) {
        return arr.indexOf(data) === -1;
      } else {
        return true;
      }
    },

    uploadUrl(file) {
      return `${this.url}frelease/api/release/uploadAssets?runtime_env=${
        file[0].runtime_env
      }`;
    },

    finish(scope) {
      scope.removeUploadedFiles();
      scope.files = [];
      scope.formFields[3].value = scope.files;
    },

    // 处理接口返回的数据，用于页面展示
    classify(arr, catalogType) {
      if (catalogType === '8') {
        let applications = [];
        let classifyArr = arr.map(item => {
          return item.application_id;
        });
        classifyArr = Array.from(new Set(classifyArr));
        classifyArr.forEach(item => {
          const obj = {
            application_cn: '',
            application_en: '',
            application_id: item,
            esf_id: '',
            esfInfo: [],
            caas_network_area: '',
            scc_network_area: '',
            sid: '',
            platform: '',
            prod_id: '',
            update_time: '',
            upload_user: '',
            upload_username_cn: '',
            expand: false
          };
          arr.forEach(app => {
            if (item === app.application_id) {
              obj.application_cn = app.application_cn;
              obj.application_en = app.application_en;
              obj.application_id = app.application_id;
              obj.esf_id = app.esf_id;
              for (let env in app.esf_info) {
                obj.esfInfo.push({
                  runtime_env: env,
                  ...app.esf_info[env]
                });
              }
              obj.caas_network_area = app.caas_network_area
                ? app.caas_network_area
                : '';
              obj.scc_network_area = app.scc_network_area
                ? app.scc_network_area
                : '';
              obj.sid = app.sid;
              obj.platform = app.platform;
              obj.prod_id = app.prod_id;
              obj.update_time = app.update_time;
              obj.upload_user = app.upload_user;
              obj.upload_username_cn = app.upload_username_cn;
            }
          });
          applications.push(obj);
        });
        return applications;
      } else if (catalogType === 'esfcommonconfig') {
        let resList = [];
        let appIdArr = arr.map(app => {
          return app.source_application;
        });
        appIdArr = Array.from(new Set(appIdArr));
        let envArr = arr.map(app => {
          return app.runtime_env;
        });
        envArr = Array.from(new Set(envArr));
        appIdArr.forEach(item => {
          const obj = {
            source_application: item,
            source_application_name: '',
            asset_catalog_name: '',
            ids: [],
            prod_id: '',
            release_node_name: '',
            upload_time: '',
            upload_user: '',
            upload_username_cn: '',
            expand: false,
            assets: []
          };
          arr.forEach(app => {
            if (item === app.source_application) {
              obj.source_application_name = app.source_application_name;
              obj.asset_catalog_name = app.asset_catalog_name;
              obj.ids.push(app.id);
              obj.prod_id = app.prod_id;
              obj.release_node_name = app.release_node_name;
              obj.upload_time = app.upload_time;
              obj.upload_user = app.upload_user;
              obj.upload_username_cn = app.upload_username_cn;
            }
          });
          const tempAppId = item;
          envArr.forEach((item, index) => {
            obj.assets.push({
              runtime_env: item,
              fileName: [],
              bucket_name: '',
              sid: '',
              source: '',
              files_giturl: []
            });
            arr.forEach(app => {
              if (
                app.runtime_env === item &&
                app.source_application === tempAppId
              ) {
                obj.assets[index].fileName.push(app.fileName);
                obj.assets[index].files_giturl.push(app.file_giturl);
                if (!obj.assets[index].bucket_name) {
                  obj.assets[index].bucket_name = app.bucket_name;
                }
                if (!obj.assets[index].sid) {
                  obj.assets[index].sid = app.sid;
                }
                if (!obj.assets[index].source) {
                  obj.assets[index].source = app.source;
                }
              }
            });
          });
          resList.push(obj);
        });
        return resList;
      } else {
        let files = [];
        let classifyArr = arr.map(file => {
          return (
            file.fileName +
            '|' +
            file.bucket_name +
            '|' +
            file.bucket_path +
            '|' +
            file.aws_type
          );
        });
        classifyArr = Array.from(new Set(classifyArr));
        classifyArr.forEach(item => {
          const obj = {
            fileName: item.split('|')[0],
            assets: [],
            upload_user: '',
            expand: false,
            id: [],
            upload_username_cn: '',
            upload_time: '',
            source_application: ''
          };
          arr.forEach((file, index) => {
            if (
              item ===
              file.fileName +
                '|' +
                file.bucket_name +
                '|' +
                file.bucket_path +
                '|' +
                file.aws_type
            ) {
              obj.assets.push(file);
              obj.upload_user = file.upload_user;
              obj.upload_user = file.upload_user;
              obj.upload_username_cn = file.upload_username_cn;
              obj.source_application = file.source_application;
              obj.source_application_name = file.source_application_name;
              obj.aws_type = file.aws_type;
              obj.upload_time = file.upload_time;
              obj.bucket_name = file.bucket_name;
              obj.bucket_path = file.bucket_path;
              obj.source = file.source;
              obj.id.push(file.id);
            }
          });
          files.push(obj);
        });
        return files;
      }
    },

    // 触发上传器QUploader的finish事件，重置数据
    clearChildCatalog(table) {
      if (table.catalog_type === '7') {
        this.bucketName = '';
        this.bucketPath = '';
      } else {
        this.childCatalog = '';
      }
    },

    order(table) {
      table = table.filter(data => {
        return data.seq_no !== null;
      });
      let arr = [];
      table.forEach((item, index) => {
        arr[item.seq_no - 1] = item;
      });
      return arr;
    },

    // 获取配置文件更新数据
    async getAssetsData() {
      if (
        this.changesDetail.image_deliver_type === this.ImageDeliverType.auto
      ) {
        this.getAssets();
        await this.querySystemDetailByProdId({
          prod_id: this.id
        });
      } else {
        this.getDeAutoAssets();
      }
    },

    // 修改对象存储用户所属组
    async updateObjSaveGroup() {
      if (this.editedGroup) {
        await this.updateAwsAssetGroupId({
          // id: this.awsObj.id,
          prod_id: this.id,
          group_id: this.editedGroup.group_id
        });
        this.closeDialog();
        this.getAssetsData();
      } else {
        this.$refs.objSaveGroupName.validate();
      }
    },

    // 修改对象存储所属组弹窗
    async editGroupDialog(table) {
      this.awsObj = table;
      this.editedGroup = this.awsGroups.filter(
        group => group.group_name === table.group_name
      )[0];
      this.editedGroup === undefined ? (this.editedGroup = null) : '';
      this.showGroupDialog = true;
    },

    // 关闭对象存储所属组弹窗
    closeDialog() {
      this.editedGroup = null;
      this.showGroupDialog = false;
    },

    // 上传zip的QFab开启，关闭aws普通上传QFab
    closeAwsQfab(index) {
      this.$refs['awsQfab'][index].hide();
      this.isShowAwsZipTooltip = false;
    },

    // aws上传的QFab开启，关闭zip上传的QFab
    closeAwsZipQfab(index) {
      this.$refs['awsZipQfab'][index].hide();
      this.isShowAwsTooltip = false;
    },

    // 点击QFab按钮
    clickFab(table, index) {
      this.catalogType = table.catalog_type;
    },

    // 触发QFab的hide事件，清除上传器数据
    hideUploader(table, index, fileType) {
      const params = `upload${index}`;
      if (table.catalog_type === '7') {
        this.bucketName = '';
        if (fileType === 'zip') {
          this.$refs.uploadzip[index].files = [];
          this.$refs.uploadzip[index].formFields[3].value = [];
          this.isShowAwsZipTooltip = true;
        } else {
          this.bucketPath = '';
          this.$refs[params][1].files = [];
          this.$refs[params][1].formFields[3].value = [];
          this.isShowAwsTooltip = true;
        }
      } else if (table.catalog_type === '5' || table.catalog_type === '10') {
        this.childCatalog = '';
        this.$refs[params][0].files = [];
        this.$refs[params][0].formFields[3].value = [];
      } else if (table.catalog_type === '9') {
        this.childCatalog = '';
        this.$refs[params][2].files = [];
        this.$refs[params][2].formFields[3].value = [];
      }
    },

    // 点击添加esf用户信息按钮
    addEsfUserInfo() {
      this.esfUserInfoDiaType = 'add';
      this.esfUserInfoDia = true;
    },

    // 提交esf用户信息表单之后
    submitEsfUserInfo() {
      this.esfUserInfoDia = false;
      this.changesDetail.image_deliver_type === '1'
        ? this.getAssets()
        : this.getDeAutoAssets();
    },

    // 编辑esfUserInfo
    editEsfUserInfo(curRow) {
      this.esfUserInfoDiaType = 'edit';
      this.curEditEsfUserInfoData = curRow;
      this.esfUserInfoDia = true;
    },

    // 删除esf用户信息
    deleteEsf(curEsf) {
      this.$q
        .dialog({
          title: '确认删除',
          message: '您确定删除该条信息吗?',
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.delEsf({
            id: curEsf.esf_id,
            prod_id: curEsf.prod_id,
            application_id: curEsf.application_id
          });
          successNotify('删除成功');
          this.changesDetail.image_deliver_type === '1'
            ? this.getAssets()
            : this.getDeAutoAssets();
        });
    },

    // 新增commonconfig
    addcommonconfig() {
      this.esfCommonConfigDiaType = 'add';
      this.esfCommonConfigDia = true;
    },

    // 提交esfCommonConfig表单之后
    submitEsfCommonConfigInfo() {
      this.esfCommonConfigDia = false;
      this.changesDetail.image_deliver_type === '1'
        ? this.getAssets()
        : this.getDeAutoAssets();
    },

    // 编辑esfCommonConfig
    editEsfCommonConfig(curRow) {
      this.esfCommonConfigDiaType = 'edit';
      this.curEditEsfCommonConfigData = curRow;
      this.esfCommonConfigDia = true;
    },

    // commonconfig介质目录修改部署平台
    inputPlatform(event, index) {
      const params = `upload${index}`;
      this.$refs[params][0].files = [];
      this.$refs[params][0].formFields[3].value = [];
    }
  },
  async created() {
    this.id = this.$route.params.id;
    await this.queryChangesDetail({
      prod_id: this.id
    });
    await this.queryAwsGroups();
    this.getAssetsData();
  }
};
</script>

<style lang="stylus" scoped>
.shadow-2
  height 40px
.q-fab >>> .q-btn--fab,
.q-fab >>> .q-btn__wrapper
  width 36px!important
  height 36px!important
  min-width 36px!important
  min-height 36px!important
  max-width 36px!important
  max-height 36px!important
  overflow hide
  padding 0px!important
</style>
