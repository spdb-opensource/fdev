<template>
  <div>
    <div class="row justify-between q-mb-lg">
      <fdev-btn-toggle
        v-model="type"
        :options="[
          { label: '灰度', value: 'gray' },
          { label: '生产', value: 'proc' }
        ]"
      />
      <!-- <fdev-btn normal label="导出" ficon="download" /> -->
    </div>

    <!-- 灰度-手机-发布说明列表 -->
    <fdev-table
      title="手机-发布说明列表"
      :columns="columns"
      :data="greyMobileData"
      titleIcon="list_s_f"
      noExport
      no-select-cols
      v-if="type === 'gray'"
    >
      <template v-slot:body-cell-release_note_name="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.release_note_name | filterReleaseNoteName"
        >
          <a
            v-if="props.row.lock_flag === '1'"
            class="link cursor-pointer"
            target="_blank"
            @click="edit(props.row)"
          >
            {{ props.row.release_note_name | filterReleaseNoteName }}
          </a>
          <span v-else>{{
            props.row.release_note_name | filterReleaseNoteName
          }}</span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.release_note_name | filterReleaseNoteName }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
      </template>

      <template v-slot:body-cell-image_deliver_type="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.image_deliver_type | imageDeliverTypeFilter"
        >
          <span>{{
            props.row.image_deliver_type | imageDeliverTypeFilter
          }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-launcher_name_cn="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.image_deliver_type | imageDeliverTypeFilter"
        >
          <router-link
            :to="{ path: `/user/list/${props.row.launcher}` }"
            class="link"
            v-if="props.row.launcher"
          >
            <span>{{ props.row.launcher_name_cn }}</span>
          </router-link>
          <span v-else>{{ props.row.launcher_name_cn || '-' }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-lock_name_cn="props">
        <fdev-td class="text-ellipsis" :title="props.row.lock_name_cn">
          <router-link
            :to="{ path: `/user/list/${props.row.lock_people}` }"
            class="link"
            v-if="props.row.lock_people"
          >
            <span>{{ props.row.lock_name_cn }}</span>
          </router-link>
          <span v-else>{{ props.row.lock_name_cn || '-' }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-lock_flag="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.lock_flag | lockFlagFilter"
        >
          <span>{{ props.row.lock_flag | lockFlagFilter }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-btn="props">
        <fdev-td auto-width>
          <div class="q-gutter-x-sm row no-wrap">
            <div class="inline-block">
              <fdev-btn
                :label="props.row.lock_flag === '1' ? '锁定' : '解锁'"
                flat
                :disable="
                  !releaseDetail.can_operation ||
                    (props.row.lock_flag !== '1' &&
                      props.row.lock_people !== currentUser.id)
                "
                @click="lock(props.row)"
              />
              <fdev-tooltip
                position="top"
                v-if="
                  !releaseDetail.can_operation ||
                    (props.row.lock_flag !== '1' &&
                      props.row.lock_people !== currentUser.id)
                "
              >
                {{ tooltipsTitle(props.row) }}
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="下载"
                :disable="!props.row.note_download_url"
                flat
                @click="download(props.row)"
              />
              <fdev-tooltip v-if="!props.row.note_download_url">
                未生成发布说明，请生成发布说明
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="生成发布说明"
                flat
                :disable="props.row.lock_flag !== '1'"
                @click="generateNote(props.row)"
              />
              <fdev-tooltip v-if="props.row.lock_flag !== '1'">
                锁定状态下不能执行此操作
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="删除"
                flat
                :disable="
                  props.row.lock_flag !== '1' || !releaseDetail.can_operation
                "
                @click="deleteNoteFun(props.row)"
              />
              <fdev-tooltip
                v-if="
                  props.row.lock_flag !== '1' || !releaseDetail.can_operation
                "
              >
                <span v-if="props.row.lock_flag !== '1'">
                  锁定状态下不能执行此操作
                </span>
                <span
                  v-if="
                    props.row.lock_flag === '1' && !releaseDetail.can_operation
                  "
                >
                  请联系当前投产窗口所属组成员的第三层级组及其子组的投产管理员
                </span>
              </fdev-tooltip>
            </div>
          </div>
        </fdev-td>
      </template>
    </fdev-table>

    <!-- 灰度-网银-发布说明列表 -->
    <fdev-table
      title="网银-发布说明列表"
      :columns="columns"
      titleIcon="list_s_f"
      :data="greyNetworkData"
      noExport
      no-select-cols
      v-if="type === 'gray'"
    >
      <template v-slot:body-cell-release_note_name="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.release_note_name | filterReleaseNoteName"
        >
          <a
            v-if="props.row.lock_flag === '1'"
            class="link cursor-pointer"
            target="_blank"
            @click="edit(props.row)"
          >
            {{ props.row.release_note_name | filterReleaseNoteName }}
          </a>
          <span v-else>{{
            props.row.release_note_name | filterReleaseNoteName
          }}</span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.release_note_name | filterReleaseNoteName }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
      </template>

      <template v-slot:body-cell-image_deliver_type="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.image_deliver_type | imageDeliverTypeFilter"
        >
          <span>{{
            props.row.image_deliver_type | imageDeliverTypeFilter
          }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-launcher_name_cn="props">
        <fdev-td class="text-ellipsis" :title="props.row.launcher_name_cn">
          <router-link
            :to="{ path: `/user/list/${props.row.launcher}` }"
            class="link"
            v-if="props.row.launcher"
          >
            <span>{{ props.row.launcher_name_cn }}</span>
          </router-link>
          <span v-else>{{ props.row.launcher_name_cn }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-lock_name_cn="props">
        <fdev-td class="text-ellipsis" :title="props.row.lock_name_cn">
          <router-link
            :to="{ path: `/user/list/${props.row.lock_people}` }"
            class="link"
            v-if="props.row.lock_people"
          >
            <span>{{ props.row.lock_name_cn }}</span>
          </router-link>
          <span v-else>{{ props.row.lock_name_cn || '-' }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-lock_flag="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.lock_flag | lockFlagFilter"
        >
          <span>{{ props.row.lock_flag | lockFlagFilter }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-btn="props">
        <fdev-td auto-width>
          <div class="q-gutter-x-sm row no-wrap">
            <div class="inline-block">
              <fdev-btn
                :label="props.row.lock_flag === '1' ? '锁定' : '解锁'"
                flat
                :disable="
                  !releaseDetail.can_operation ||
                    (props.row.lock_flag !== '1' &&
                      props.row.lock_people !== currentUser.id)
                "
                @click="lock(props.row)"
              />
              <fdev-tooltip
                position="top"
                v-if="
                  !releaseDetail.can_operation ||
                    (props.row.lock_flag !== '1' &&
                      props.row.lock_people !== currentUser.id)
                "
              >
                {{ tooltipsTitle(props.row) }}
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="下载"
                :disable="!props.row.note_download_url"
                flat
                @click="download(props.row)"
              />
              <fdev-tooltip v-if="!props.row.note_download_url">
                未生成发布说明，请生成发布说明
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="生成发布说明"
                flat
                :disable="props.row.lock_flag !== '1'"
                @click="generateNote(props.row)"
              />
              <fdev-tooltip v-if="props.row.lock_flag !== '1'">
                锁定状态下不能执行此操作
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="删除"
                flat
                :disable="
                  props.row.lock_flag !== '1' || !releaseDetail.can_operation
                "
                @click="deleteNoteFun(props.row)"
              />
              <fdev-tooltip
                v-if="
                  props.row.lock_flag !== '1' || !releaseDetail.can_operation
                "
              >
                <span v-if="props.row.lock_flag !== '1'">
                  锁定状态下不能执行此操作
                </span>
                <span
                  v-if="
                    props.row.lock_flag === '1' && !releaseDetail.can_operation
                  "
                >
                  请联系当前投产窗口所属组成员的第三层级组及其子组的投产管理员
                </span>
              </fdev-tooltip>
            </div>
          </div>
        </fdev-td>
      </template>
    </fdev-table>

    <!-- 生产-手机-发布说明列表 -->
    <fdev-table
      title="手机-发布说明列表"
      :columns="columns"
      titleIcon="list_s_f"
      :data="prodMobileData"
      noExport
      no-select-cols
      v-if="type === 'proc'"
    >
      <template v-slot:body-cell-release_note_name="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.release_note_name | filterReleaseNoteName"
        >
          <a
            v-if="props.row.lock_flag === '1'"
            class="link cursor-pointer"
            target="_blank"
            @click="edit(props.row)"
          >
            {{ props.row.release_note_name | filterReleaseNoteName }}
          </a>
          <span v-else>{{
            props.row.release_note_name | filterReleaseNoteName
          }}</span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.release_note_name | filterReleaseNoteName }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
      </template>

      <template v-slot:body-cell-image_deliver_type="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.image_deliver_type | imageDeliverTypeFilter"
        >
          <span>{{
            props.row.image_deliver_type | imageDeliverTypeFilter
          }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-launcher_name_cn="props">
        <fdev-td class="text-ellipsis" :title="props.row.launcher_name_cn">
          <router-link
            :to="{ path: `/user/list/${props.row.launcher}` }"
            class="link"
            v-if="props.row.launcher"
          >
            <span>{{ props.row.launcher_name_cn }}</span>
          </router-link>
          <span v-else>{{ props.row.launcher_name_cn || '-' }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-lock_name_cn="props">
        <fdev-td class="text-ellipsis" :title="props.row.lock_name_cn">
          <router-link
            :to="{ path: `/user/list/${props.row.lock_people}` }"
            class="link"
            v-if="props.row.lock_people"
          >
            <span>{{ props.row.lock_name_cn }}</span>
          </router-link>
          <span v-else>{{ props.row.lock_name_cn || '-' }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-lock_flag="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.lock_flag | lockFlagFilter"
        >
          <span>{{ props.row.lock_flag | lockFlagFilter }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-btn="props">
        <fdev-td auto-width>
          <div class="q-gutter-x-sm row no-wrap">
            <div class="inline-block">
              <fdev-btn
                :label="props.row.lock_flag === '1' ? '锁定' : '解锁'"
                flat
                :disable="
                  !releaseDetail.can_operation ||
                    (props.row.lock_flag !== '1' &&
                      props.row.lock_people !== currentUser.id)
                "
                @click="lock(props.row)"
              />
              <fdev-tooltip
                position="top"
                v-if="
                  !releaseDetail.can_operation ||
                    (props.row.lock_flag !== '1' &&
                      props.row.lock_people !== currentUser.id)
                "
              >
                {{ tooltipsTitle(props.row) }}
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="下载"
                :disable="!props.row.note_download_url"
                flat
                @click="download(props.row)"
              />
              <fdev-tooltip v-if="!props.row.note_download_url">
                未生成发布说明，请生成发布说明
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="生成发布说明"
                flat
                :disable="props.row.lock_flag !== '1'"
                @click="generateNote(props.row)"
              />
              <fdev-tooltip v-if="props.row.lock_flag !== '1'">
                锁定状态下不能执行此操作
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="删除"
                flat
                :disable="
                  props.row.lock_flag !== '1' || !releaseDetail.can_operation
                "
                @click="deleteNoteFun(props.row)"
              />
              <fdev-tooltip
                v-if="
                  props.row.lock_flag !== '1' || !releaseDetail.can_operation
                "
              >
                <span v-if="props.row.lock_flag !== '1'">
                  锁定状态下不能执行此操作
                </span>
                <span
                  v-if="
                    props.row.lock_flag === '1' && !releaseDetail.can_operation
                  "
                >
                  请联系当前投产窗口所属组成员的第三层级组及其子组的投产管理员
                </span>
              </fdev-tooltip>
            </div>
          </div>
        </fdev-td>
      </template>
    </fdev-table>

    <!-- 生产-网银-发布说明列表 -->
    <fdev-table
      title="网银-发布说明列表"
      :columns="columns"
      :data="prodNetworkData"
      titleIcon="list_s_f"
      noExport
      no-select-cols
      v-if="type === 'proc'"
    >
      <template v-slot:body-cell-release_note_name="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.release_note_name | filterReleaseNoteName"
        >
          <a
            v-if="props.row.lock_flag === '1'"
            class="link cursor-pointer"
            target="_blank"
            @click="edit(props.row)"
          >
            {{ props.row.release_note_name | filterReleaseNoteName }}
          </a>
          <span v-else>{{
            props.row.release_note_name | filterReleaseNoteName
          }}</span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.release_note_name | filterReleaseNoteName }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
      </template>

      <template v-slot:body-cell-image_deliver_type="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.image_deliver_type | imageDeliverTypeFilter"
        >
          <span>{{
            props.row.image_deliver_type | imageDeliverTypeFilter
          }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-launcher_name_cn="props">
        <fdev-td class="text-ellipsis" :title="props.row.launcher_name_cn">
          <router-link
            :to="{ path: `/user/list/${props.row.launcher}` }"
            class="link"
            v-if="props.row.launcher"
          >
            <span>{{ props.row.launcher_name_cn }}</span>
          </router-link>
          <span v-else>{{ props.row.launcher_name_cn || '-' }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-lock_name_cn="props">
        <fdev-td class="text-ellipsis" :title="props.row.lock_name_cn">
          <router-link
            :to="{ path: `/user/list/${props.row.lock_people}` }"
            class="link"
            v-if="props.row.lock_people"
          >
            <span>{{ props.row.lock_name_cn }}</span>
          </router-link>
          <span v-else>{{ props.row.lock_name_cn || '-' }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-lock_flag="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.lock_flag | lockFlagFilter"
        >
          <span>{{ props.row.lock_flag | lockFlagFilter }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-btn="props">
        <fdev-td auto-width>
          <div class="q-gutter-x-sm row no-wrap">
            <div class="inline-block">
              <fdev-btn
                :label="props.row.lock_flag === '1' ? '锁定' : '解锁'"
                flat
                :disable="
                  !releaseDetail.can_operation ||
                    (props.row.lock_flag !== '1' &&
                      props.row.lock_people !== currentUser.id)
                "
                @click="lock(props.row)"
              />
              <fdev-tooltip
                position="top"
                v-if="
                  !releaseDetail.can_operation ||
                    (props.row.lock_flag !== '1' &&
                      props.row.lock_people !== currentUser.id)
                "
              >
                {{ tooltipsTitle(props.row) }}
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="下载"
                :disable="!props.row.note_download_url"
                flat
                @click="download(props.row)"
              />
              <fdev-tooltip v-if="!props.row.note_download_url">
                未生成发布说明，请生成发布说明
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="生成发布说明"
                flat
                :disable="props.row.lock_flag !== '1'"
                @click="generateNote(props.row)"
              />
              <fdev-tooltip v-if="props.row.lock_flag !== '1'">
                锁定状态下不能执行此操作
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="删除"
                flat
                :disable="
                  props.row.lock_flag !== '1' || !releaseDetail.can_operation
                "
                @click="deleteNoteFun(props.row)"
              />
              <fdev-tooltip
                v-if="
                  props.row.lock_flag !== '1' || !releaseDetail.can_operation
                "
              >
                <span v-if="props.row.lock_flag !== '1'">
                  锁定状态下不能执行此操作
                </span>
                <span
                  v-if="
                    props.row.lock_flag === '1' && !releaseDetail.can_operation
                  "
                >
                  请联系当前投产窗口所属组成员的第三层级组及其子组的投产管理员
                </span>
              </fdev-tooltip>
            </div>
          </div>
        </fdev-td>
      </template>
    </fdev-table>

    <!-- 生产-批量-发布说明列表 -->
    <fdev-table
      title="批量-发布说明列表"
      :columns="columns"
      :data="prodBatchData"
      titleIcon="list_s_f"
      noExport
      no-select-cols
      v-if="type === 'proc'"
    >
      <template v-slot:body-cell-release_note_name="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.release_note_name | filterReleaseNoteName"
        >
          <a
            v-if="props.row.lock_flag === '1'"
            class="link cursor-pointer"
            target="_blank"
            @click="edit(props.row)"
          >
            {{ props.row.release_note_name | filterReleaseNoteName }}
          </a>
          <span v-else>{{
            props.row.release_note_name | filterReleaseNoteName
          }}</span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.release_note_name | filterReleaseNoteName }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
      </template>

      <template v-slot:body-cell-image_deliver_type="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.image_deliver_type | imageDeliverTypeFilter"
        >
          <span>{{
            props.row.image_deliver_type | imageDeliverTypeFilter
          }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-launcher_name_cn="props">
        <fdev-td class="text-ellipsis" :title="props.row.launcher_name_cn">
          <router-link
            :to="{ path: `/user/list/${props.row.launcher}` }"
            class="link"
            v-if="props.row.launcher"
          >
            <span>{{ props.row.launcher_name_cn }}</span>
          </router-link>
          <span v-else>{{ props.row.launcher_name_cn || '-' }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-lock_name_cn="props">
        <fdev-td class="text-ellipsis" :title="props.row.lock_name_cn">
          <router-link
            :to="{ path: `/user/list/${props.row.lock_people}` }"
            class="link"
            v-if="props.row.lock_people"
          >
            <span>{{ props.row.lock_name_cn }}</span>
          </router-link>
          <span v-else>{{ props.row.lock_name_cn || '-' }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-lock_flag="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.lock_flag | lockFlagFilter"
        >
          <span>{{ props.row.lock_flag | lockFlagFilter }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-btn="props">
        <fdev-td auto-width>
          <div class="q-gutter-x-sm row no-wrap">
            <div class="inline-block">
              <fdev-btn
                :label="props.row.lock_flag === '1' ? '锁定' : '解锁'"
                flat
                :disable="
                  !releaseDetail.can_operation ||
                    (props.row.lock_flag !== '1' &&
                      props.row.lock_people !== currentUser.id)
                "
                @click="lock(props.row)"
              />
              <fdev-tooltip
                position="top"
                v-if="
                  !releaseDetail.can_operation ||
                    (props.row.lock_flag !== '1' &&
                      props.row.lock_people !== currentUser.id)
                "
              >
                {{ tooltipsTitle(props.row) }}
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="下载"
                :disable="!props.row.note_download_url"
                flat
                @click="download(props.row)"
              />
              <fdev-tooltip v-if="!props.row.note_download_url">
                未生成发布说明，请生成发布说明
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="生成发布说明"
                flat
                :disable="props.row.lock_flag !== '1'"
                @click="generateNote(props.row)"
              />
              <fdev-tooltip v-if="props.row.lock_flag !== '1'">
                锁定状态下不能执行此操作
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="删除"
                flat
                :disable="
                  props.row.lock_flag !== '1' || !releaseDetail.can_operation
                "
                @click="deleteNoteFun(props.row)"
              />
              <fdev-tooltip
                v-if="
                  props.row.lock_flag !== '1' || !releaseDetail.can_operation
                "
              >
                <span v-if="props.row.lock_flag !== '1'">
                  锁定状态下不能执行此操作
                </span>
                <span
                  v-if="
                    props.row.lock_flag === '1' && !releaseDetail.can_operation
                  "
                >
                  请联系当前投产窗口所属组成员的第三层级组及其子组的投产管理员
                </span>
              </fdev-tooltip>
            </div>
          </div>
        </fdev-td>
      </template>
    </fdev-table>

    <!-- 生产-柜面-发布说明列表 -->
    <fdev-table
      title="柜面-发布说明列表"
      :columns="columns"
      :data="prodOtcData"
      titleIcon="list_s_f"
      noExport
      no-select-cols
      v-if="type === 'proc'"
    >
      <template v-slot:body-cell-release_note_name="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.release_note_name | filterReleaseNoteName"
        >
          <a
            v-if="props.row.lock_flag === '1'"
            class="link cursor-pointer"
            target="_blank"
            @click="edit(props.row)"
          >
            {{ props.row.release_note_name | filterReleaseNoteName }}
          </a>
          <span v-else>{{
            props.row.release_note_name | filterReleaseNoteName
          }}</span>
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ props.row.release_note_name | filterReleaseNoteName }}
            </fdev-banner>
          </fdev-popup-proxy>
        </fdev-td>
      </template>

      <template v-slot:body-cell-image_deliver_type="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.image_deliver_type | imageDeliverTypeFilter"
        >
          <span>{{
            props.row.image_deliver_type | imageDeliverTypeFilter
          }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-launcher_name_cn="props">
        <fdev-td class="text-ellipsis" :title="props.row.launcher_name_cn">
          <router-link
            :to="{ path: `/user/list/${props.row.launcher}` }"
            class="link"
            v-if="props.row.launcher"
          >
            <span>{{ props.row.launcher_name_cn }}</span>
          </router-link>
          <span v-else>{{ props.row.launcher_name_cn || '-' }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-lock_name_cn="props">
        <fdev-td class="text-ellipsis" :title="props.row.lock_name_cn">
          <router-link
            :to="{ path: `/user/list/${props.row.lock_people}` }"
            class="link"
            v-if="props.row.lock_people"
          >
            <span>{{ props.row.lock_name_cn }}</span>
          </router-link>
          <span v-else>{{ props.row.lock_name_cn || '-' }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-lock_flag="props">
        <fdev-td
          class="text-ellipsis"
          :title="props.row.lock_flag | lockFlagFilter"
        >
          <span>{{ props.row.lock_flag | lockFlagFilter }}</span>
        </fdev-td>
      </template>

      <template v-slot:body-cell-btn="props">
        <fdev-td auto-width>
          <div class="q-gutter-x-sm row no-wrap">
            <div class="inline-block">
              <fdev-btn
                :label="props.row.lock_flag === '1' ? '锁定' : '解锁'"
                flat
                :disable="
                  !releaseDetail.can_operation ||
                    (props.row.lock_flag !== '1' &&
                      props.row.lock_people !== currentUser.id)
                "
                @click="lock(props.row)"
              />
              <fdev-tooltip
                position="top"
                v-if="
                  !releaseDetail.can_operation ||
                    (props.row.lock_flag !== '1' &&
                      props.row.lock_people !== currentUser.id)
                "
              >
                {{ tooltipsTitle(props.row) }}
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="下载"
                :disable="!props.row.note_download_url"
                flat
                @click="download(props.row)"
              />
              <fdev-tooltip v-if="!props.row.note_download_url">
                未生成发布说明，请生成发布说明
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="生成发布说明"
                flat
                :disable="props.row.lock_flag !== '1'"
                @click="generateNote(props.row)"
              />
              <fdev-tooltip v-if="props.row.lock_flag !== '1'">
                锁定状态下不能执行此操作
              </fdev-tooltip>
            </div>
            <div class="text-grey-1 sepreator">|</div>
            <div class="inline-block">
              <fdev-btn
                label="删除"
                flat
                :disable="
                  props.row.lock_flag !== '1' || !releaseDetail.can_operation
                "
                @click="deleteNoteFun(props.row)"
              />
              <fdev-tooltip
                v-if="
                  props.row.lock_flag !== '1' || !releaseDetail.can_operation
                "
              >
                <span v-if="props.row.lock_flag !== '1'">
                  锁定状态下不能执行此操作
                </span>
                <span
                  v-if="
                    props.row.lock_flag === '1' && !releaseDetail.can_operation
                  "
                >
                  请联系当前投产窗口所属组成员的第三层级组及其子组的投产管理员
                </span>
              </fdev-tooltip>
            </div>
          </div>
        </fdev-td>
      </template>
    </fdev-table>

    <!-- 预览/编辑/新增发布说明弹窗 -->
    <f-dialog
      @before-close="cleanForm"
      dense
      v-model="showDialogModel"
      right
      :title="title"
      persistent
    >
      <f-formitem
        style="width:1000px"
        label-style="width:112px; margin-right:0;"
        label="发布说明内容"
        required
        bottom-page
      >
        <textarea
          type="textarea"
          style="width:888px; min-height:460px"
          ref="releaseNoteContent"
          v-model="releaseNoteContent"
          :readonly="!isEdit"
          :class="{ 'no-border': !isEdit }"
          @keydown.9.prevent="replaceSpacing"
        />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn dialog label="取消" outline @click="cleanForm" />
        <fdev-btn dialog :label="btnTitle" @click="editDetail" />
        <fdev-btn dialog label="保存" @click="saveDetail" />
      </template>
    </f-dialog>
  </div>
</template>

<script>
import { autoReleaseNoteListColumns } from '../../utils/constants';
import { mapActions, mapState } from 'vuex';
import { isValidReleaseDate, successNotify, errorNotify } from '@/utils/utils';
export default {
  name: 'AutoReleaseNote',
  data() {
    return {
      noteId: '',
      releaseNoteContent: null,
      manualNoteId: '',
      isEdit: false,
      showDialogModel: false,
      type: 'gray',
      greyMobileData: [], // 灰度手机列表
      prodMobileData: [], // 生产手机列表
      greyNetworkData: [], // 灰度网银列表
      prodNetworkData: [], // 生产网银列表
      prodBatchData: [], // 生产批量列表
      prodOtcData: [], // 生产柜面列表
      columns: autoReleaseNoteListColumns
    };
  },
  computed: {
    ...mapState('releaseForm', {
      releaseNoteList: 'releaseNoteList',
      manualNoteInfo: 'manualNoteInfo',
      releaseDetail: 'releaseDetail'
    }),
    ...mapState('global', {
      globalLoading: 'loading'
    }),
    ...mapState('user', {
      currentUser: 'currentUser'
    }),
    // 标题内容
    title() {
      return !this.isEdit ? '预览' : this.manualNoteId ? '编辑' : '新增';
    },
    // 按钮内容
    btnTitle() {
      return !this.isEdit ? '编辑' : '预览';
    }
  },
  filters: {
    // 锁定状态filter
    lockFlagFilter(val) {
      return val === '1' ? '未锁定' : '已锁定';
    },
    // 手动-自动filter
    imageDeliverTypeFilter(val) {
      return val === '1' ? '自动' : '手动';
    },
    // 发布说明名称filter
    filterReleaseNoteName(val) {
      if (val) {
        const indexName = val.lastIndexOf('.txt');
        return val.substring(0, indexName);
      } else {
        return '-';
      }
    }
  },
  watch: {
    releaseNoteList: {
      deep: true,
      handler(val) {
        this.greyMobileData = [];
        this.prodMobileData = [];
        this.greyNetworkData = [];
        this.prodNetworkData = [];
        this.prodBatchData = [];
        this.prodOtcData = [];
        if (val && val.length > 0) {
          val.forEach(element => {
            if (
              element.owner_system_name &&
              element.type === 'gray' &&
              element.owner_system_name.includes('个人手机')
            ) {
              this.greyMobileData.push(element);
            }
            if (
              element.owner_system_name &&
              element.type === 'proc' &&
              element.owner_system_name.includes('个人手机')
            ) {
              this.prodMobileData.push(element);
            }
            if (
              element.owner_system_name &&
              element.type === 'gray' &&
              element.owner_system_name.includes('个人网银')
            ) {
              this.greyNetworkData.push(element);
            }
            if (
              element.owner_system_name &&
              element.type === 'proc' &&
              element.owner_system_name.includes('个人网银')
            ) {
              this.prodNetworkData.push(element);
            }
            if (
              element.owner_system_name &&
              element.type === 'proc' &&
              element.owner_system_name.includes('批量')
            ) {
              this.prodBatchData.push(element);
            }
            if (
              element.owner_system_name &&
              element.type === 'proc' &&
              element.owner_system_name.includes('柜面')
            ) {
              this.prodOtcData.push(element);
            }
          });
        }
      }
    }
  },
  methods: {
    ...mapActions('jobForm', ['queryRqrmntFileUri', 'downExcel']),
    ...mapActions('releaseForm', {
      queryReleaseNote: 'queryReleaseNote',
      generateReleaseNotes: 'generateReleaseNotes',
      lockNote: 'lockNote',
      deleteNote: 'deleteNote',
      addManualNoteInfo: 'addManualNoteInfo',
      queryManualNoteInfo: 'queryManualNoteInfo',
      updateManualNoteInfo: 'updateManualNoteInfo'
    }),
    editDetail() {
      this.isEdit = !this.isEdit;
    },
    forbidTab(e) {
      e.preventDefault();
    },
    replaceSpacing() {
      let inputElement = this.$refs['releaseNoteContent'];
      let startPos = inputElement.selectionStart;
      let endPos = inputElement.selectionEnd;
      if (startPos === undefined || endPos === undefined) {
        return;
      }
      let text = inputElement.value;
      let res = text.substring(0, startPos) + '\t' + text.substring(endPos);
      inputElement.value = res;
      this.releaseNoteContent = res;
      inputElement.focus();
      inputElement.selectionStart = startPos + 1;
      inputElement.selectionEnd = startPos + 1;
    },
    async saveDetail() {
      if (!this.releaseNoteContent) {
        errorNotify('请填写发布说明内容');
        return;
      }
      const params = {
        note_id: this.noteId,
        note_info: this.releaseNoteContent
      };
      if (this.manualNoteId) {
        await this.updateManualNoteInfo(params);
      } else {
        await this.addManualNoteInfo(params);
      }
      successNotify('添加成功');
      this.cleanForm();
      this.init();
    },
    cleanForm() {
      this.releaseNoteContent = '';
      this.isEdit = false;
      this.manualNoteId = '';
      this.noteId = '';
      this.showDialogModel = false;
    },
    // 获取变更记录
    compareTime(date) {
      return isValidReleaseDate(date);
    },
    tooltipsTitle(val) {
      return !this.releaseDetail.can_operation
        ? '请联系当前投产窗口所属组成员的第三层级组及其子组的投产管理员'
        : val.lock_flag !== '1' && val.lock_people !== this.currentUser.id
        ? '请联系' + val.lock_name_cn + '进行解锁'
        : '';
    },
    async generateNote(item) {
      if (item.image_deliver_type !== '1') {
        const params = {
          note_id: item.note_id
        };
        await this.queryManualNoteInfo(params);
        if (!this.manualNoteInfo || !this.manualNoteInfo.note_info) {
          errorNotify('请填写发布说明内容后再生成发布说明');
          return;
        }
      }
      await this.generateReleaseNotes({ id: item.note_id });
      successNotify('生成发布说明成功');
      this.init();
    },
    edit(item) {
      if (item.image_deliver_type === '1') {
        this.$router.push({
          path: '/release/autoReleaseNoteDetail/' + item.note_id + '/applist'
        });
      } else {
        this.getManualNoteDetail(item);
        this.showDialogModel = true;
      }
    },
    async getManualNoteDetail(item) {
      this.noteId = item.note_id;
      const params = {
        note_id: item.note_id
      };
      await this.queryManualNoteInfo(params);
      if (this.manualNoteInfo) {
        if (this.manualNoteInfo.id) {
          this.manualNoteId = this.manualNoteInfo.id;
        } else {
          this.manualNoteId = '';
          this.isEdit = true;
        }
        this.releaseNoteContent = this.manualNoteInfo.note_info || '';
      } else {
        this.releaseNoteContent = '';
      }
    },
    async download(item) {
      let param = {
        path: item.note_download_url,
        moduleName: 'fdev-release'
      };
      await this.downExcel(param);
    },
    async deleteNoteFun(item) {
      this.$q
        .dialog({
          title: '删除发布说明',
          message: '确定删除该发布说明吗?',
          cancel: true
        })
        .onOk(async () => {
          await this.deleteNote({
            note_id: item.note_id,
            image_deliver_type: item.image_deliver_type
          });
          successNotify('删除成功');
          this.init();
        });
    },
    // 锁定
    lock(item) {
      this.$q
        .dialog({
          title: item.lock_flag !== '1' ? '解锁' : '锁定',
          message:
            item.lock_flag !== '1'
              ? '确定解除锁定状态？'
              : `锁定状态下仅支持下载，非锁定人无法解锁。`,
          cancel: true,
          persistent: true
        })
        .onOk(async () => {
          await this.lockNote({
            note_id: item.note_id,
            lock_flag: item.lock_flag === '1' ? '0' : '1'
          });
          successNotify('操作成功');
          this.init();
        });
    },
    init() {
      this.queryReleaseNote({
        release_node_name: this.release_node_name
      });
    }
  },
  created() {
    this.release_node_name = this.$route.params.id;
    this.init();
  }
};
</script>

<style lang="stylus" scoped>
.no-border
  border none
  color #0663be
  margin-top -2px
.sepreator
  line-height 32px
</style>
