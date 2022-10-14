<template>
  <div>
    <Loading class="bg-white" style="border-radius: 8px" v-if="!hasAuthority">
      <div class="row header">
        <div
          class="col-12"
          style="padding: 20px 0;font-weight:600;font-size:16px"
        >
          【{{ jobProfile.demand.oa_contact_no }}】 {{ jobProfile.name }}
        </div>
        <f-formitem
          page
          label="所属小组："
          class="col-4"
          label-auto
          profile
          label-style="font-size:16px;"
          value-style="font-size:16px;"
        >
          {{ jobProfile.group ? jobProfile.group.name : '-' }}
          <fdev-popup-proxy context-menu>
            <fdev-banner style="max-width:300px">
              {{ jobProfile.group }}
            </fdev-banner>
          </fdev-popup-proxy>
        </f-formitem>
        <f-formitem
          page
          label="需求编号："
          class="col-4"
          label-auto
          profile
          label-style="font-size:16px;"
          value-style="font-size:16px;"
        >
          <div v-if="jobProfile.demand && jobProfile.demand.oa_contact_no">
            <router-link
              :to="`/rqrmn/rqrProfile/${jobProfile.rqrmnt_no}`"
              class="link a-link"
              :title="jobProfile.demand.oa_contact_no"
            >
              {{ jobProfile.demand.oa_contact_no }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ jobProfile.demand.oa_contact_no }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </div>
          <div v-else>-</div>
        </f-formitem>
        <f-formitem
          page
          label="实施单元编号："
          class="col-4"
          label-auto
          profile
          label-style="font-size:16px;"
          value-style="font-size:16px;"
        >
          <div v-if="jobProfile.ipmp_implement_unit_no">
            <router-link
              :to="
                `/rqrmn/unitDetail/${jobProfile.ipmp_implement_unit_no}/${
                  jobProfile.rqrmnt_no
                }`
              "
              class="link a-link"
              :title="jobProfile.ipmp_implement_unit_no"
            >
              {{ jobProfile.ipmp_implement_unit_no }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ jobProfile.ipmp_implement_unit_no }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </div>
          <div v-else>-</div>
        </f-formitem>
        <f-formitem
          page
          label="研发单元编号："
          class="col-4"
          label-auto
          profile
          label-style="font-size:16px;"
          value-style="font-size:16px;"
        >
          <div
            v-if="jobProfile.fdev_implement_unit_no"
            @click="openUnitDetail(jobProfile)"
            class="a-link"
            :title="jobProfile.fdev_implement_unit_no"
          >
            {{ jobProfile.fdev_implement_unit_no }}
            <fdev-popup-proxy context-menu>
              <fdev-banner style="max-width:300px">
                {{ jobProfile.fdev_implement_unit_no }}
              </fdev-banner>
            </fdev-popup-proxy>
          </div>
          <div v-else>-</div>
        </f-formitem>
        <f-formitem
          page
          label="任务名称："
          class="col-4"
          label-auto
          profile
          label-style="font-size:16px;"
          value-style="font-size:16px;"
        >
          <div v-if="jobProfile.name" :title="jobProfile.name">
            <router-link :to="`/job/list/${jobProfile.id}`" class="link a-link">
              {{ jobProfile.name }}
              <fdev-popup-proxy context-menu>
                <fdev-banner style="max-width:300px">
                  {{ jobProfile.name }}
                </fdev-banner>
              </fdev-popup-proxy>
            </router-link>
          </div>
          <div v-else>-</div>
        </f-formitem>
      </div>
      <div class="borderb"></div>
      <div
        v-if="reviewerObj.finished && reviewerObj.finished.length > 0"
        class="row justify-center items-center q-mb-lg"
      >
        <img class="q-mr-lg" src="@/assets/done.svg" alt="" />
        <span class="doneTile">该审核已完成</span>
      </div>
      <f-ver-stepper :words="stepChildArray">
        <template
          v-for="(item, index) in dataArrCenter"
          :slot="
            `content-${rejectList.length + 2 + dataArrCenter.length - index}`
          "
        >
          <div class="step" :key="item.time">
            <div
              v-if="
                (item.stage === 'load_nopass' &&
                  typeof item.fake === 'undefined') ||
                  (item.stage === 'load_nopass' &&
                    item.fake &&
                    isFinish(reviewerObj, index) === '通过')
              "
            >
              <div class="backgroundC" v-if="stepChild > rejectList.length + 3">
                <div class="row">
                  <div class="title1 q-pr-md">
                    <span>{{
                      index === dataArrCenter.length - 1
                        ? '一轮审核反馈'
                        : item.title
                    }}</span>
                    -
                    <span
                      :class="[
                        isFinish(reviewerObj, index) === '通过'
                          ? 'primary'
                          : 'red'
                      ]"
                      >{{ isFinish(reviewerObj, index) }}</span
                    >
                  </div>
                  <div class="timeStyle">
                    <div v-if="isFinish(reviewerObj, index) === '通过'">
                      {{ reviewerObj.finished[0].time }}
                    </div>
                    <div v-else>
                      {{ item.time }}
                    </div>
                  </div>
                </div>
                <div class="q-pt-lg lh14">
                  <span class="title2 ft600">操作人</span>
                  <span class="timeStyle1">{{
                    isFinish(reviewerObj, index) === '通过'
                      ? reviewerObj.finished[0].name
                      : item.name
                  }}</span>
                </div>
                <div class="q-pt-md row">
                  <div class="title2 ft600">审核反馈</div>
                  <div class="flex1 ellipsis">
                    <div class="row">
                      <span style="width: 68px;line-height: 14px">IOS</span>
                      <a
                        :title="
                          getIosfile(dataArrCenter.length - index)
                            ? getIosfile(dataArrCenter.length - index).fileName
                            : ''
                        "
                        class="link div-7 cursor-pointer q-ml-sm"
                        :disable="disBtnAudit(index)"
                        @click="
                          download(getIosfile(dataArrCenter.length - index))
                        "
                        >{{
                          getIosfile(dataArrCenter.length - index)
                            ? getIosfile(dataArrCenter.length - index).fileName
                            : ''
                        }}</a
                      >
                    </div>
                    <div class="row items-center q-pt-md">
                      <div style="width: 68px;line-height: 14px">Android</div>
                      <div class="ellipsis">
                        <a
                          :title="
                            getAndroidfile(dataArrCenter.length - index)
                              ? getAndroidfile(dataArrCenter.length - index)
                                  .fileName
                              : ''
                          "
                          class="link div-7 cursor-pointer q-ml-sm"
                          @click="
                            download(
                              getAndroidfile(dataArrCenter.length - index)
                            )
                          "
                          v-if="getAndroidfile(dataArrCenter.length - index)"
                          >{{
                            getAndroidfile(dataArrCenter.length - index)
                              ? getAndroidfile(dataArrCenter.length - index)
                                  .fileName
                              : ''
                          }}</a
                        >
                      </div>
                    </div>
                  </div>
                </div>
                <div class="pt12 row">
                  <div class="title2 ft600" style="line-height: 21px;">
                    审核描述
                  </div>
                  <div
                    class="ellipsis-2-lines flex1"
                    style="line-height: 21px;word-break: break-all;"
                    :title="item.remark"
                  >
                    {{
                      isFinish(reviewerObj, index) === '通过'
                        ? reviewerObj.finished[0].remark
                        : item.remark
                    }}
                  </div>
                </div>
              </div>
            </div>
            <div
              v-if="
                item.stage === 'load_upload' && typeof item.fake === 'undefined'
              "
            >
              <div class="backgroundC">
                <div class="row">
                  <div class="title1 q-pr-md">
                    {{ item.title }}
                  </div>
                  <div class="timeStyle">
                    <div>
                      {{ item.time }}
                    </div>
                  </div>
                </div>
                <div class="q-pt-lg">
                  <span class="title2 ft600 lh14">操作人</span>
                  <span class="timeStyle1">{{ item.name }}</span>
                </div>
                <div class="q-pt-md row items-center">
                  <span class="title2 ft600">设计稿</span>
                  <a
                    :title="design0 ? design0.fileName : ''"
                    class="link div-3 cursor-pointer"
                    @click="download(design0)"
                    v-if="design0"
                    >{{ design0 ? design0.fileName : '' }}
                  </a>
                </div>
                <div class="q-pt-md row">
                  <div class="title2 ft600">开发截图</div>
                  <div class="flex1 ellipsis">
                    <div class="row">
                      <span style="width: 68px;line-height: 14px">IOS</span>
                      <a
                        :title="
                          getIosfile(dataArrCenter.length - index)
                            ? getIosfile(dataArrCenter.length - index).fileName
                            : ''
                        "
                        class="link div-3  cursor-pointer"
                        @click="
                          download(getIosfile(dataArrCenter.length - index))
                        "
                        v-if="getIosfile(dataArrCenter.length - index)"
                        >{{
                          getIosfile(dataArrCenter.length - index)
                            ? getIosfile(dataArrCenter.length - index).fileName
                            : ''
                        }}</a
                      >
                    </div>
                    <div class="row items-center q-pt-md">
                      <div style="width: 68px;line-height: 14px">Android</div>
                      <div class="ellipsis">
                        <a
                          :title="
                            getAndroidfile(dataArrCenter.length - index)
                              ? getAndroidfile(dataArrCenter.length - index)
                                  .fileName
                              : ''
                          "
                          class="link div-3 cursor-pointer"
                          @click="
                            download(
                              getAndroidfile(dataArrCenter.length - index)
                            )
                          "
                          v-if="getAndroidfile(dataArrCenter.length - index)"
                          >{{
                            getAndroidfile(dataArrCenter.length - index)
                              ? getAndroidfile(dataArrCenter.length - index)
                                  .fileName
                              : ''
                          }}</a
                        >
                      </div>
                    </div>
                  </div>
                </div>
                <div class="pt12 row">
                  <div class="title2 ft600" style="line-height: 21px;">
                    申请描述
                  </div>
                  <div
                    class="ellipsis-2-lines flex1"
                    style="line-height: 21px;word-break: break-all;"
                    :title="item.remark"
                  >
                    {{ item.remark }}
                  </div>
                </div>
              </div>
            </div>
            <div
              v-if="
                item.stage === 'load_nopass' &&
                  item.fake &&
                  isFinish(reviewerObj, index) !== '通过'
              "
            >
              <div class="flex">
                <div style="min-width:160px;margin-top:36px">
                  <div class="row items-center">
                    <span
                      style="color: #0063C5"
                      class="text-body1 ft600 q-mr-sm"
                      >{{ item.title }}</span
                    >
                    <f-icon class="text-primary" name="arrow_r_f" />
                  </div>
                </div>
                <div class="distribution">
                  <div
                    class="row items-center"
                    style="font-size: 16px; line-height: 16px;"
                  >
                    <div class="row items-center ft600" style="width: 136px">
                      <span class="dian"></span>
                      审核结果
                    </div>
                    <div style="font-size:14px">
                      <fdev-radio
                        class="mr60"
                        v-model="shape"
                        val="0"
                        label="拒绝"
                      />
                      <fdev-radio v-model="shape" val="1" label="通过" />
                    </div>
                  </div>
                  <div class="row items-center q-pt-lg">
                    <f-formitem
                      label="上传开发截图(IOS)"
                      profile
                      full-width
                      label-style="padding-left:14px"
                    >
                      <div class="ellipsis">
                        <fdev-tooltip
                          anchor="top middle"
                          self="center middle"
                          :offest="[0, 0]"
                          v-if="!isRelevant"
                        >
                          您无权操作
                        </fdev-tooltip>
                        <fdev-btn
                          label="上传文件"
                          ficon="upload"
                          roundash
                          :disable="!isRelevant"
                          class="q-mr-md"
                          @click="selBtn(0, dataArrCenter.length)"
                        />
                        <span
                          class="fileNameStyle"
                          :title="
                            getIosfile(dataArrCenter.length)
                              ? getIosfile(dataArrCenter.length).fileName
                              : ''
                          "
                          >{{
                            getIosfile(dataArrCenter.length)
                              ? getIosfile(dataArrCenter.length).fileName
                              : '支持拓展名.zip'
                          }}</span
                        >
                      </div>
                    </f-formitem>
                  </div>
                  <div class="row items-center q-pt-lg">
                    <f-formitem
                      label="上传开发截图(Android)"
                      profile
                      full-width
                      label-style="padding-left:14px"
                    >
                      <div class="ellipsis">
                        <fdev-tooltip
                          anchor="top middle"
                          self="center middle"
                          :offest="[0, 0]"
                          v-if="!isRelevant"
                        >
                          您无权操作
                        </fdev-tooltip>
                        <fdev-btn
                          label="上传文件"
                          ficon="upload"
                          roundash
                          :disable="!isRelevant"
                          class="q-mr-md"
                          @click="selBtn(1, dataArrCenter.length)"
                        />
                        <span
                          class="fileNameStyle"
                          :title="
                            getAndroidfile(dataArrCenter.length)
                              ? getAndroidfile(dataArrCenter.length).fileName
                              : ''
                          "
                          >{{
                            getAndroidfile(dataArrCenter.length)
                              ? getAndroidfile(dataArrCenter.length).fileName
                              : '支持拓展名.zip'
                          }}</span
                        >
                      </div>
                    </f-formitem>
                  </div>
                  <input
                    @change="selFile(0, dataArrCenter.length)"
                    class="none"
                    type="file"
                    value="上传IOS截图"
                    :id="'ios' + dataArrCenter.length"
                  />
                  <input
                    @change="selFile(1, dataArrCenter.length)"
                    class="none"
                    type="file"
                    value="上传安卓截图"
                    :id="'android' + dataArrCenter.length"
                  />
                  <div v-if="shape === '0'" class="row q-pt-lg inputStyle">
                    <f-formitem
                      label="拒绝原因"
                      profile
                      label-style="padding-left:14px"
                    >
                      <fdev-input
                        v-model="text"
                        placeholder="可描述审核文件，或任何需要告知开发人员"
                        clearable
                        type="textarea"
                      />
                    </f-formitem>
                  </div>
                  <div class="footStyle q-pt-lg">
                    <fdev-tooltip
                      anchor="top middle"
                      self="center middle"
                      :offest="[0, 0]"
                      v-if="!isRelevant"
                    >
                      您无权操作
                    </fdev-tooltip>
                    <fdev-btn
                      :disable="!isRelevant"
                      @click="nextStep"
                      :loading="loadingBtn"
                      label="确定"
                    />
                  </div>
                </div>
              </div>
            </div>
            <div v-if="item.stage === 'load_upload' && item.fake">
              <div class="flex">
                <div style="min-width:160px;margin-top:36px">
                  <div class="row items-center">
                    <span
                      style="color: #0063C5"
                      class="text-body1 ft600 q-mr-sm"
                      >{{ item.title }}</span
                    >
                    <f-icon class="text-primary" name="arrow_r_f" />
                  </div>
                </div>
                <div class="distribution">
                  <div>
                    <f-formitem label="上传设计稿" required full-width>
                      <div class="ellipsis">
                        <fdev-tooltip
                          anchor="top middle"
                          self="center middle"
                          :offest="[0, 0]"
                          v-if="!isDeveloper"
                        >
                          您无权操作
                        </fdev-tooltip>
                        <fdev-btn
                          label="上传文件"
                          ficon="upload"
                          roundash
                          class="q-mr-md"
                          :disable="!isDeveloper"
                          @click="selBtn(2, 0)"
                        />
                        <span
                          class="fileNameStyle"
                          :title="design0 ? design0.fileName : ''"
                          >{{
                            design0 ? design0.fileName : '支持拓展名.zip'
                          }}</span
                        >
                      </div>
                    </f-formitem>
                  </div>
                  <div class="q-pt-llg">
                    <f-formitem required label="上传开发截图(IOS)" full-width>
                      <div class="ellipsis">
                        <fdev-tooltip
                          anchor="top middle"
                          self="center middle"
                          :offest="[0, 0]"
                          v-if="!isDeveloper"
                        >
                          您无权操作
                        </fdev-tooltip>
                        <fdev-btn
                          label="上传文件"
                          ficon="upload"
                          :disable="!isDeveloper"
                          roundash
                          class="q-mr-md"
                          @click="selBtn(0, dataArrCenter.length)"
                        />
                        <span
                          class="fileNameStyle"
                          :title="
                            getIosfile(dataArrCenter.length)
                              ? getIosfile(dataArrCenter.length).fileName
                              : ''
                          "
                          >{{
                            getIosfile(dataArrCenter.length)
                              ? getIosfile(dataArrCenter.length).fileName
                              : '支持拓展名.zip'
                          }}</span
                        >
                      </div>
                    </f-formitem>
                  </div>
                  <div class="q-pt-llg">
                    <f-formitem
                      required
                      label="上传开发截图(Android)"
                      full-width
                    >
                      <div class="ellipsis">
                        <fdev-tooltip
                          anchor="top middle"
                          self="center middle"
                          :offest="[0, 0]"
                          v-if="!isDeveloper"
                        >
                          您无权操作
                        </fdev-tooltip>
                        <fdev-btn
                          label="上传文件"
                          ficon="upload"
                          roundash
                          :disable="!isDeveloper"
                          class="q-mr-md"
                          @click="selBtn(1, dataArrCenter.length)"
                        />
                        <span
                          class="fileNameStyle"
                          :title="
                            getAndroidfile(dataArrCenter.length)
                              ? getAndroidfile(dataArrCenter.length).fileName
                              : ''
                          "
                          >{{
                            getAndroidfile(dataArrCenter.length)
                              ? getAndroidfile(dataArrCenter.length).fileName
                              : '支持拓展名.zip'
                          }}</span
                        >
                      </div>
                    </f-formitem>
                  </div>
                  <div class="row q-pt-llg inputStyle">
                    <f-formitem label="申请描述">
                      <fdev-input
                        v-model="text"
                        placeholder="请告知审核范围，否则默认审核全部"
                        clearable
                        type="textarea"
                      />
                    </f-formitem>
                  </div>
                  <input
                    @change="selFile(2, 0)"
                    class="none"
                    type="file"
                    value="上传设计稿"
                    id="design0"
                  />
                  <input
                    @change="selFile(0, dataArrCenter.length)"
                    class="none"
                    type="file"
                    value="上传IOS截图"
                    :id="'ios' + dataArrCenter.length"
                  />
                  <input
                    @change="selFile(1, dataArrCenter.length)"
                    class="none"
                    type="file"
                    value="上传安卓截图"
                    :id="'android' + dataArrCenter.length"
                  />
                  <div class="footStyle q-pt-lg">
                    <fdev-tooltip
                      anchor="top middle"
                      self="center middle"
                      :offest="[0, 0]"
                      v-if="
                        !isDeveloper ||
                          !getAndroidfile(dataArrCenter.length) ||
                          !getIosfile(dataArrCenter.length)
                      "
                    >
                      <div v-if="!isDeveloper">您无权操作</div>
                      <div v-else>请上传开发截图</div>
                    </fdev-tooltip>
                    <fdev-btn
                      :disable="
                        !isDeveloper ||
                          !getAndroidfile(dataArrCenter.length) ||
                          !getIosfile(dataArrCenter.length)
                      "
                      @click="nextStep"
                      :loading="loadingBtn"
                      label="确定"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
        <template :slot="`content-${rejectList.length + 3}`">
          <div
            class="step flex"
            v-if="stepChild === rejectList.length + 3 && !reviewerObj.finished"
          >
            <div style="min-width:160px;margin-top:36px">
              <div class="row items-center">
                <span style="color: #0063C5" class="text-body1 ft600 q-mr-sm">
                  一轮审核-审核反馈
                </span>
                <f-icon class="text-primary" name="arrow_r_f" />
              </div>
            </div>
            <div class="distribution">
              <div
                class="row items-center"
                style="font-size: 16px; line-height: 16px;"
              >
                <div class="row items-center ft600" style="width: 136px">
                  <span class="dian"></span>
                  审核结果
                </div>
                <div style="font-size:14px">
                  <fdev-radio
                    class="mr60"
                    v-model="shape"
                    val="0"
                    label="拒绝"
                  />
                  <fdev-radio v-model="shape" val="1" label="通过" />
                </div>
              </div>
              <div class="row items-center q-pt-lg">
                <f-formitem
                  label="上传开发截图(IOS)"
                  profile
                  full-width
                  label-style="padding-left:14px"
                >
                  <div class="ellipsis">
                    <fdev-tooltip
                      anchor="top middle"
                      self="center middle"
                      :offest="[0, 0]"
                      v-if="!isRelevant"
                    >
                      您无权操作
                    </fdev-tooltip>
                    <fdev-btn
                      label="上传文件"
                      ficon="upload"
                      roundash
                      :disable="!isRelevant"
                      class="q-mr-md"
                      @click="selBtn(0, 1)"
                    />
                    <span
                      class="fileNameStyle"
                      :title="getIosfile(1) ? getIosfile(1).fileName : ''"
                      >{{
                        getIosfile(1)
                          ? getIosfile(1).fileName
                          : '支持拓展名.zip'
                      }}</span
                    >
                  </div>
                </f-formitem>
              </div>
              <div class="row items-center q-pt-lg">
                <f-formitem
                  label="上传开发截图(Android)"
                  profile
                  full-width
                  label-style="padding-left:14px"
                >
                  <div class="ellipsis">
                    <fdev-tooltip
                      anchor="top middle"
                      self="center middle"
                      :offest="[0, 0]"
                      v-if="!isRelevant"
                    >
                      您无权操作
                    </fdev-tooltip>
                    <fdev-btn
                      label="上传文件"
                      ficon="upload"
                      roundash
                      :disable="!isRelevant"
                      class="q-mr-md"
                      @click="selBtn(1, 1)"
                    />
                    <span
                      class="fileNameStyle"
                      :title="
                        getAndroidfile(1) ? getAndroidfile(1).fileName : ''
                      "
                      >{{
                        getAndroidfile(1)
                          ? getAndroidfile(1).fileName
                          : '支持拓展名.zip'
                      }}</span
                    >
                  </div>
                </f-formitem>
              </div>
              <input
                @change="selFile(0, 1)"
                class="none"
                type="file"
                value="上传IOS截图"
                :id="'ios' + 1"
              />
              <input
                @change="selFile(1, 1)"
                class="none"
                type="file"
                value="上传安卓截图"
                :id="'android' + 1"
              />
              <div v-if="shape === '0'" class="row q-pt-lg inputStyle">
                <f-formitem
                  label="拒绝原因"
                  profile
                  label-style="padding-left:14px"
                >
                  <fdev-input
                    v-model="text"
                    placeholder="可描述审核文件，或任何需要告知开发人员"
                    clearable
                    type="textarea"
                  />
                </f-formitem>
              </div>
              <div class="footStyle q-pt-lg">
                <fdev-tooltip
                  anchor="top middle"
                  self="center middle"
                  :offest="[0, 0]"
                  v-if="!isRelevant"
                >
                  您无权操作
                </fdev-tooltip>
                <fdev-btn
                  :disable="!isRelevant"
                  @click="nextStep"
                  :loading="loadingBtn"
                  label="确定"
                />
              </div>
            </div>
          </div>
          <!-- 第三步直接点通过 -->
          <div
            v-if="
              reviewerObj.finished &&
                reviewerObj.finished.length > 0 &&
                !reviewerObj.nopass &&
                reviewerObj.wait_allot
            "
            class="step"
          >
            <div class="backgroundC">
              <div class="row">
                <div class="title1 q-pr-md">
                  <span>{{ !isUnvolved ? '一轮审核反馈' : '审核反馈' }}</span>
                  -
                  <span class="primary">通过</span>
                </div>
                <div class="timeStyle">
                  <div>
                    {{ reviewerObj.finished[0].time }}
                  </div>
                </div>
              </div>
              <div class="q-pt-lg lh14">
                <span class="title2 ft600">操作人</span>
                <span class="timeStyle1">{{
                  reviewerObj.finished[0].name
                }}</span>
              </div>
              <div class="q-pt-md row">
                <div class="title2 ft600">审核反馈</div>
                <div class="flex1 ellipsis">
                  <div class="row">
                    <span style="width: 68px;line-height: 14px">IOS</span>
                    <a
                      :title="getIosfile(1) ? getIosfile(1).fileName : ''"
                      class="link div-7 cursor-pointer q-ml-sm"
                      :disable="disBtnAudit(1)"
                      @click="download(getIosfile(1))"
                      >{{ getIosfile(1) ? getIosfile(1).fileName : '' }}</a
                    >
                  </div>
                  <div class="row items-center q-pt-md">
                    <div style="width: 68px;line-height: 14px">
                      Android
                    </div>
                    <div class="ellipsis">
                      <a
                        :title="
                          getAndroidfile(1) ? getAndroidfile(1).fileName : ''
                        "
                        class="link div-7 cursor-pointer q-ml-sm"
                        @click="download(getAndroidfile(1))"
                        v-if="getAndroidfile(1)"
                        >{{
                          getAndroidfile(1) ? getAndroidfile(1).fileName : ''
                        }}</a
                      >
                    </div>
                  </div>
                </div>
              </div>
              <div class="pt12 row">
                <div class="title2 ft600" style="line-height: 21px;">
                  审核描述
                </div>
                <div
                  class="ellipsis-2-lines flex1"
                  style="line-height: 21px;word-break: break-all;"
                  :title="reviewerObj.finished[0].remark"
                >
                  {{ reviewerObj.finished[0].remark }}
                </div>
              </div>
            </div>
          </div>
        </template>
        <template :slot="`content-${rejectList.length + 2}`">
          <div>
            <div v-if="stepChild > rejectList.length + 2" class="step">
              <div class="backgroundC">
                <div>
                  <span class="title1 q-pr-md">审核分配</span>
                  <span class="timeStyle">{{
                    reviewerObj.fixing && reviewerObj.fixing[0].time
                  }}</span>
                </div>
                <div class="q-pt-lg lh14">
                  <span class="title2 ft600">操作人</span>
                  <span class="timeStyle1">{{
                    reviewerObj.fixing && reviewerObj.fixing[0].name
                  }}</span>
                </div>
                <div class="q-pt-md row items-center">
                  <span class="title2 ft600">指定审核员</span>
                  <span class="timeStyle1">{{
                    jobProfile.reviewer && jobProfile.reviewer.user_name_cn
                  }}</span>
                </div>
                <!-- <div>
                  <span style="display:inline-block;width:300px"
                    >审核分配</span
                  >
                  <span>{{
                    reviewerObj.wait_allot && reviewerObj.wait_allot[0].time
                  }}</span>
                </div>
                <div>
                  <span style="display:inline-block;width:300px"
                    >操作人：{{
                      reviewerObj.wait_allot && reviewerObj.wait_allot[0].name
                    }}</span
                  >
                  <span
                    >指定审核员：{{
                      reviewerObj.fixing && reviewerObj.fixing[0].name
                    }}</span
                  >
                </div> -->
              </div>
            </div>
            <div
              class="step"
              v-else-if="
                stepChild === rejectList.length + 2 && !reviewerObj.finished
              "
            >
              <div v-if="!isUnvolved" class="flex">
                <div style="min-width:160px;margin-top:36px">
                  <div class="row items-center">
                    <span
                      style="color: #0063C5"
                      class="text-body1 ft600 q-mr-sm"
                    >
                      审核分配
                    </span>
                    <f-icon class="text-primary" name="arrow_r_f" />
                  </div>
                </div>
                <div class="distribution">
                  <div
                    class="row items-center"
                    style="font-size: 16px; line-height: 16px"
                  >
                    <span class="dian"></span>
                    审核分配
                  </div>
                  <div style="margin-top: 20px">
                    <f-formitem
                      label="请指定审核人员"
                      label-class="ft600"
                      label-style="padding-left:14px"
                    >
                      <fdev-select
                        use-input
                        v-model="reviewer"
                        :options="users"
                        option-label="user_name_cn"
                        option-value="user_name_en"
                        @filter="userFilter"
                        :disable="!isUIManage"
                      >
                        <template v-slot:option="scope">
                          <fdev-item
                            v-bind="scope.itemProps"
                            v-on="scope.itemEvents"
                          >
                            <fdev-item-section>
                              <fdev-item-label>
                                {{ scope.opt.user_name_cn }}
                              </fdev-item-label>
                              <fdev-item-label caption>
                                {{ scope.opt.user_name_en }}
                              </fdev-item-label>
                            </fdev-item-section>
                          </fdev-item>
                        </template>
                      </fdev-select>
                    </f-formitem>
                  </div>
                  <div class="row justify-center" style="margin-top: 28px">
                    <div>
                      <fdev-tooltip
                        anchor="top middle"
                        self="center middle"
                        :offest="[0, 0]"
                        v-if="!isUIManage"
                      >
                        UI团队负责人指定设计师审核
                      </fdev-tooltip>
                      <fdev-btn
                        :disable="!isUIManage"
                        @click="nextStep"
                        :loading="loadingBtn"
                        label="确定"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
        <template :slot="`content-${rejectList.length + 1}`">
          <div class="step">
            <div
              v-if="
                stepChild === rejectList.length + 1 ||
                  (stepChild > rejectList.length + 1 &&
                    (!(iosfile0 && androidfile0) &&
                      !(
                        reviewerObj.uninvolved &&
                        reviewerObj.uninvolved.length > 0
                      )))
              "
            >
              <div
                v-if="
                  reviewerObj.finished &&
                    reviewerObj.finished.length > 0 &&
                    !reviewerObj.wait_allot
                "
                class="step"
              >
                <div class="backgroundC">
                  <div class="row">
                    <div class="title1 q-pr-md">
                      <span>审核反馈</span>
                      -
                      <span class="primary">通过</span>
                    </div>
                    <div class="timeStyle">
                      <div>
                        {{ reviewerObj.finished[0].time }}
                      </div>
                    </div>
                  </div>
                  <div class="q-pt-lg lh14">
                    <span class="title2 ft600">操作人</span>
                    <span class="timeStyle1">{{
                      reviewerObj.finished[0].name
                    }}</span>
                  </div>
                  <div class="q-pt-md row">
                    <div class="title2 ft600">审核反馈</div>
                    <div class="flex1 ellipsis">
                      <div class="row">
                        <span style="width: 68px;line-height: 14px">IOS</span>
                        <a
                          :title="getIosfile(1) ? getIosfile(1).fileName : ''"
                          class="link div-7 cursor-pointer q-ml-sm"
                          :disable="disBtnAudit(1)"
                          @click="download(getIosfile(1))"
                          >{{ getIosfile(1) ? getIosfile(1).fileName : '' }}</a
                        >
                      </div>
                      <div class="row items-center q-pt-md">
                        <div style="width: 68px;line-height: 14px">
                          Android
                        </div>
                        <div class="ellipsis">
                          <a
                            :title="
                              getAndroidfile(1)
                                ? getAndroidfile(1).fileName
                                : ''
                            "
                            class="link div-7 cursor-pointer q-ml-sm"
                            @click="download(getAndroidfile(1))"
                            v-if="getAndroidfile(1)"
                            >{{
                              getAndroidfile(1)
                                ? getAndroidfile(1).fileName
                                : ''
                            }}</a
                          >
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="pt12 row">
                    <div class="title2 ft600" style="line-height: 21px;">
                      审核描述
                    </div>
                    <div
                      class="ellipsis-2-lines flex1"
                      style="line-height: 21px;word-break: break-all;"
                      :title="reviewerObj.finished[0].remark"
                    >
                      {{ reviewerObj.finished[0].remark }}
                    </div>
                  </div>
                </div>
              </div>
              <div v-else>
                <div class="row" v-if="!isUnvolved && !reviewerObj.finished">
                  <div class="flex">
                    <div style="min-width:160px;margin-top:36px">
                      <div class="row items-center">
                        <span
                          style="color: #0063C5"
                          class="text-body1 ft600 q-mr-sm"
                        >
                          发起审核
                        </span>
                        <f-icon class="text-primary" name="arrow_r_f" />
                      </div>
                    </div>
                    <div class="distribution">
                      <div>
                        <f-formitem label="上传设计稿" required full-width>
                          <div class="ellipsis">
                            <fdev-tooltip
                              anchor="top middle"
                              self="center middle"
                              :offest="[0, 0]"
                              v-if="!isDeveloper"
                            >
                              您无权操作
                            </fdev-tooltip>
                            <fdev-btn
                              label="上传文件"
                              ficon="upload"
                              roundash
                              class="q-mr-md"
                              :disable="!isDeveloper"
                              @click="selBtn(2, 0)"
                            />
                            <span
                              class="fileNameStyle"
                              :title="design0 ? design0.fileName : ''"
                              >{{
                                design0 ? design0.fileName : '支持拓展名.zip'
                              }}</span
                            >
                          </div>
                        </f-formitem>
                      </div>
                      <div class="q-pt-llg">
                        <f-formitem
                          required
                          label="上传开发截图(IOS)"
                          full-width
                        >
                          <div class="ellipsis">
                            <fdev-tooltip
                              anchor="top middle"
                              self="center middle"
                              :offest="[0, 0]"
                              v-if="!isDeveloper"
                            >
                              您无权操作
                            </fdev-tooltip>
                            <fdev-btn
                              label="上传文件"
                              ficon="upload"
                              :disable="!isDeveloper"
                              roundash
                              class="q-mr-md"
                              @click="selBtn(0, 0)"
                            />
                            <span
                              class="fileNameStyle"
                              :title="iosfile0 ? iosfile0.fileName : ''"
                              >{{
                                iosfile0 ? iosfile0.fileName : '支持拓展名.zip'
                              }}</span
                            >
                          </div>
                        </f-formitem>
                      </div>
                      <div class="q-pt-lg">
                        <f-formitem
                          required
                          label="上传开发截图(Android)"
                          full-width
                        >
                          <div class="ellipsis">
                            <fdev-tooltip
                              anchor="top middle"
                              self="center middle"
                              :offest="[0, 0]"
                              v-if="!isDeveloper"
                            >
                              您无权操作
                            </fdev-tooltip>
                            <fdev-btn
                              label="上传文件"
                              ficon="upload"
                              roundash
                              :disable="!isDeveloper"
                              class="q-mr-md"
                              @click="selBtn(1, 0)"
                            />
                            <span
                              class="fileNameStyle"
                              :title="androidfile0 ? androidfile0.fileName : ''"
                              >{{
                                androidfile0
                                  ? androidfile0.fileName
                                  : '支持拓展名.zip'
                              }}</span
                            >
                          </div>
                        </f-formitem>
                      </div>
                      <div class="q-pt-llg inputStyle">
                        <f-formitem label="申请描述">
                          <fdev-input
                            v-model="text"
                            placeholder="请告知审核范围，否则默认审核全部"
                            clearable
                            type="textarea"
                          />
                        </f-formitem>
                      </div>
                      <div class="footStyle q-pt-lg row justify-center">
                        <div>
                          <fdev-tooltip
                            anchor="top middle"
                            self="center middle"
                            :offest="[0, 0]"
                            v-if="
                              !isDeveloper ||
                                !iosfile0 ||
                                !androidfile0 ||
                                !design0
                            "
                          >
                            <span v-if="!isDeveloper">您无权操作</span>
                            <span v-else-if="!design0">请上传设计稿截图</span>
                            <span v-else-if="!iosfile0">请上传ios开发截图</span>
                            <span v-else-if="!androidfile0"
                              >请上传android开发截图</span
                            >
                          </fdev-tooltip>
                          <fdev-btn
                            :disable="
                              !isDeveloper ||
                                !iosfile0 ||
                                !androidfile0 ||
                                !design0
                            "
                            @click="nextStep"
                            :loading="loadingBtn"
                            label="发起审核"
                          />
                        </div>
                      </div>
                      <input
                        @change="selFile(0, 0)"
                        class="none"
                        type="file"
                        value="上传IOS截图"
                        id="ios0"
                      />
                      <input
                        @change="selFile(1, 0)"
                        class="none"
                        type="file"
                        value="上传安卓截图"
                        id="android0"
                      />
                      <input
                        @change="selFile(2, 0)"
                        class="none"
                        type="file"
                        value="上传设计稿"
                        id="design0"
                      />
                    </div>
                  </div>
                  <div class="jumpStyle row items-center">
                    此任务不涉及UI审核？<a href="#" @click="jumpUI"
                      >点击申请跳过</a
                    >
                  </div>
                </div>
                <div v-else class="flex">
                  <div style="min-width:160px;margin-top:36px">
                    <div class="row items-center">
                      <span
                        style="color: #0063C5"
                        class="text-body1 ft600 q-mr-sm"
                      >
                        审核反馈
                      </span>
                      <f-icon class="text-primary" name="arrow_r_f" />
                    </div>
                  </div>
                  <div class="distribution">
                    <div
                      class="row items-center"
                      style="font-size: 16px; line-height: 16px;"
                    >
                      <div class="row items-center ft600" style="width: 136px">
                        <span class="dian"></span>
                        审核结果
                      </div>
                      <div style="font-size:14px">
                        <fdev-radio
                          class="mr60"
                          v-model="shape"
                          val="0"
                          label="拒绝"
                        />
                        <fdev-radio v-model="shape" val="1" label="通过" />
                      </div>
                    </div>
                    <div v-if="shape === '0'" class="q-mt-md inputStyle">
                      <f-formitem
                        required
                        label="拒绝原因"
                        label-style="padding-left:2px"
                      >
                        <fdev-input
                          v-model="text"
                          placeholder="可描述拒绝原因"
                          clearable
                          type="textarea"
                        />
                      </f-formitem>
                    </div>
                    <div class="row justify-center q-mt-lg">
                      <div>
                        <fdev-tooltip
                          anchor="top middle"
                          self="center middle"
                          :offest="[0, 0]"
                          v-if="!isUIManage"
                        >
                          UI团队负责人审核
                        </fdev-tooltip>
                        <fdev-btn
                          :disable="!isUIManage || (shape === '0' && !text)"
                          @click="nextStep"
                          :loading="loadingBtn"
                          label="确定"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div
              class="backgroundC"
              v-else-if="stepChild > rejectList.length + 1"
            >
              <div>
                <div>
                  <span class="title1 q-pr-md">发起审核</span>
                  <span class="timeStyle">{{
                    reviewerObj.wait_allot ? reviewerObj.wait_allot[0].time : ''
                  }}</span>
                </div>
                <div class="q-pt-lg lh14">
                  <span class="title2 ft600">操作人</span>
                  <span class="timeStyle1">{{
                    reviewerObj.uploaded ? reviewerObj.uploaded[0].name : ''
                  }}</span>
                </div>
                <div class="q-pt-md row items-center">
                  <span class="title2 ft600">设计稿</span>
                  <a
                    :title="design0 ? design0.fileName : ''"
                    class="link div-3 cursor-pointer ellipsis"
                    @click="download(design0)"
                    v-if="design0"
                    >{{ design0 ? design0.fileName : '' }}
                  </a>
                </div>
                <div class="q-pt-md row">
                  <div class="title2 ft600">开发截图</div>
                  <div class="flex1 ellipsis">
                    <div class="row">
                      <span style="width: 68px;line-height: 14px">IOS</span>
                      <a
                        :title="iosfile0 ? iosfile0.fileName : ''"
                        class="link div-3 cursor-pointer ellipsis"
                        @click="download(iosfile0)"
                        v-if="iosfile0"
                        >{{ iosfile0 ? iosfile0.fileName : '' }}
                      </a>
                    </div>
                    <div class="row items-center q-pt-md">
                      <div style="width: 68px;line-height: 14px">Android</div>
                      <div class="flex1 ellipsis">
                        <a
                          :title="androidfile0 ? androidfile0.fileName : ''"
                          class="link div-3 cursor-pointer"
                          @click="download(androidfile0)"
                          v-if="androidfile0"
                          >{{ androidfile0 ? androidfile0.fileName : '' }}</a
                        >
                      </div>
                    </div>
                  </div>
                </div>
                <div class="pt12 row">
                  <div class="title2 ft600" style="line-height: 21px;">
                    申请描述
                  </div>
                  <div
                    class="ellipsis-2-lines flex1"
                    style="line-height: 21px;word-break: break-all;"
                    :title="
                      reviewerObj.wait_allot
                        ? reviewerObj.wait_allot[0].remark
                        : ''
                    "
                  >
                    {{
                      reviewerObj.wait_allot
                        ? reviewerObj.wait_allot[0].remark
                        : ''
                    }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
        <template
          v-for="(item, index) in rejectList"
          :slot="`content-${rejectList.length - index}`"
        >
          <div class="step" :key="index">
            <div>
              <div class="backgroundC">
                <div v-if="item.type === 'uninvolved'">
                  <span class="title1 q-pr-md">
                    发起审核-不涉及UI还原审核
                  </span>
                  <span class="timeStyle">{{ item.time }}</span>
                </div>
                <div v-else>
                  <div class="title1 q-pr-md" style="display: inline-block">
                    <span>审核反馈</span>
                    -
                    <span class="red">拒绝</span>
                  </div>
                  <span class="timeStyle">{{ item.time }}</span>
                </div>
                <div class="q-pt-lg lh14">
                  <span class="title2 ft600">操作人</span>
                  <span class="timeStyle1">{{ item.name }}</span>
                </div>
                <div class="pt12 row">
                  <div class="title2 ft600" style="line-height: 21px;">
                    {{ item.type === 'uninvolved' ? '申请描述' : '拒绝理由' }}
                  </div>
                  <div
                    class="ellipsis-2-lines flex1"
                    style="line-height: 21px;word-break: break-all;"
                    :title="item.remark"
                  >
                    {{ item.remark }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </f-ver-stepper>
      <div class="padding-20">
        <div class="div5">
          <fdev-btn label="返回" class="btn-margin q-ml-lg" @click="goBack" />
        </div>
      </div>
    </Loading>
    <Exception
      type="403"
      desc="抱歉，你无权访问该页面"
      backText="返回首页"
      v-if="hasAuthority"
    />
    <f-dialog v-model="openJump" title="申请跳过审核">
      <div class="q-mt-sm">
        <f-icon
          name="alert_c_f"
          style="color: #fd8d00"
          class="q-mr-sm"
        ></f-icon>
        <span style="line-height: 21px;display:inline-block"
          >您将同意跳过本次审核的申请，请附理由，并等待相关审核人员进行审批。</span
        >
      </div>
      <f-formitem
        class="q-mt-lg"
        style="padding-left: 12px"
        label="申请理由"
        required
        full-width
        label-style="font-weight:600"
      >
        <fdev-input v-model="text1" clearable type="textarea" />
      </f-formitem>
      <template v-slot:btnSlot
        ><fdev-btn label="取消" dialog outline @click="closeDialog"/>
        <fdev-btn label="确定" :disable="!text1" dialog @click="onSubmit"
      /></template>
    </f-dialog>
  </div>
</template>

<script>
import { mapState, mapActions, mapGetters } from 'vuex';
import Loading from '@/components/Loading';
import Exception from '@/components/Exception';
import date from '#/utils/date.js';
import {
  errorNotify,
  deepClone,
  getIdsFormList,
  successNotify,
  baseUrl
} from '@/utils/utils';
import axios from 'axios';

let number = 0;
export default {
  name: 'DesignReview',
  components: {
    Loading,
    Exception
  },
  data() {
    return {
      loadingBtn: false,
      reviewer: null,
      id: '',
      users: [],
      hasAuthority: false,
      text: '',
      text1: '',
      files: [],
      stepChild: 0,
      reviewerObj: {},
      changeOptVal: null,
      flag: false,
      choose: null,
      iosfile0: '',
      androidfile0: '',
      design0: '',
      uninvolved: false,
      shape: '0',
      openJump: false,
      rejectList: [],
      stepChildArray: []
    };
  },
  watch: {
    jobProfile: {
      deep: true,
      async handler(val) {
        this.reviewerObj = this.jobProfile.designMap
          ? deepClone(this.jobProfile.designMap)
          : {};
        this.files = val.designDoc || [];
        //描述
        // if (this.reviewerObj) {
        //   if (this.reviewerObj.nopass && !this.reviewerObj.finished) {
        //     this.text = this.reviewerObj.nopass.slice(-1)[0].remark;
        //   } else if (
        //     !this.reviewerObj.nopass &&
        //     this.reviewerObj.fixing &&
        //     !this.reviewerObj.finished
        //   ) {
        //     this.text = this.reviewerObj.fixing.slice(-1)[0].remark;
        //   } else if (!this.reviewerObj.fixing && this.reviewerObj.wait_allot) {
        //     this.text = this.reviewerObj.wait_allot.slice(-1)[0].remark;
        //   } else if (
        //     !this.reviewerObj.wait_allot &&
        //     this.reviewerObj.uploaded
        //   ) {
        //     this.text = this.reviewerObj.uploaded.slice(-1)[0].remark;
        //   } else if (
        //     this.reviewerObj.uninvolved &&
        //     !this.reviewerObj.finished
        //   ) {
        //     this.text = this.reviewerObj.uninvolved.slice(-1)[0].remark;
        //   } else if (this.reviewerObj.finished) {
        //     this.text = this.reviewerObj.finished[0].remark;
        //   }
        // }
        // 计算步骤数量
        if (this.reviewerObj.nopass && Array.isArray(this.reviewerObj.nopass)) {
          let len = this.reviewerObj.nopass.length;
          if (
            this.reviewerObj.nopass[this.reviewerObj.nopass.length - 1] &&
            this.reviewerObj.nopass[this.reviewerObj.nopass.length - 1]
              .stage === 'load_nopass'
          ) {
            this.reviewerObj.nopass.splice(len, 0, {
              stage: 'load_upload',
              name: this.currentUser.name,
              time: date.formatDate(Date.now(), 'YYYY-MM-DD HH:mm:ss'),
              fake: true
            });
          } else if (
            this.reviewerObj.nopass[this.reviewerObj.nopass.length - 1] &&
            this.reviewerObj.nopass[this.reviewerObj.nopass.length - 1]
              .stage === 'load_upload'
          ) {
            this.reviewerObj.nopass.push({
              stage: 'load_nopass',
              name: this.currentUser.name,
              time: date.formatDate(Date.now(), 'YYYY-MM-DD HH:mm:ss'),
              fake: true
            });
          }
          // let obj = deepClone(this.reviewerObj.nopass);
          // let arr = [];
          // obj.forEach((item, index) => {
          //   arr.push(item);
          //   if (
          //     item.stage === 'load_nopass' &&
          //     index !== 0 &&
          //     item.fake !== true
          //   ) {
          //     arr.push({
          //       stage: 'suggest',
          //       name: item.name,
          //       time: item.time
          //     });
          //   }
          // });
          // this.$set(this.reviewerObj, 'nopass', arr);
          number = 0;
          let arr1 = ['二', '三', '四', '五', '六', '七', '八', '九', '十'];
          this.reviewerObj.nopass.forEach((item, index) => {
            if (index === 0 && item.stage === 'load_nopass') {
              item.title = '';
              item.tip = '请上传审核反馈文件';
            } else {
              if (item && item.stage === 'load_upload') {
                item.title = `${arr1[number]}轮审核 - 开发上传`;
                item.tip = '请上传审核反馈文件';
              } else if (item && item.stage === 'load_nopass') {
                item.title = `${arr1[number]}轮审核反馈`;
                item.tip = '如审核未通过，请按反馈修改后再次提交审核';
                number += 1;
              }
            }
          });
        }
        let item = 0;
        let reviewerObjKeys = Object.keys(this.reviewerObj);
        if (this.reviewerObj) {
          reviewerObjKeys.forEach((val, index) => {
            if (
              this.reviewerObj[val] &&
              Array.isArray(this.reviewerObj[val]) &&
              val !== 'finished'
            ) {
              item += this.reviewerObj[val].length;
            }
          });
        }
        this.stepChild = item === 0 ? 1 : item;
        if (this.reviewerObj.nopass && this.reviewerObj.nopass.length > 0) {
          this.stepChild--;
        }
        //获取拒绝流程
        if (
          this.reviewerObj.uninvolved &&
          this.reviewerObj.uninvolved.length > 0
        ) {
          let arr = [];
          this.reviewerObj.uninvolved.map((item, index) => {
            Reflect.set(
              this.reviewerObj.uninvolved[index],
              'type',
              'uninvolved'
            );
            arr.push(this.reviewerObj.uninvolved[index]);
            if (
              this.reviewerObj.refuseUninvolved &&
              this.reviewerObj.refuseUninvolved[index]
            ) {
              Reflect.set(
                this.reviewerObj.refuseUninvolved[index],
                'type',
                'refuseUninvolved'
              );
              arr.push(this.reviewerObj.refuseUninvolved[index]);
            }
          });
          this.rejectList = arr.reverse();
        }
        let reviewKeys = Object.keys(this.reviewerObj);
        //仅有跳过审核 拒绝跳过审核 和 完成状态是 步骤加1
        if (
          (reviewKeys.length == 2 &&
            reviewKeys.includes('uninvolved') &&
            (reviewKeys.includes('refuseUninvolved') ||
              reviewKeys.includes('finished'))) ||
          (reviewKeys.length == 1 && reviewKeys.includes('uninvolved')) ||
          (reviewKeys.length == 3 &&
            reviewKeys.includes('uninvolved') &&
            reviewKeys.includes('refuseUninvolved') &&
            reviewKeys.includes('finished'))
        ) {
          this.stepChild++;
        }
        this.stepChildArray = [];
        for (let i = this.stepChild; i > 0; i--) {
          if (!this.reviewerObj.finished && i === this.stepChild) {
            this.stepChildArray.push({ value: i, inteval: 36 });
          } else {
            this.stepChildArray.push({ value: i });
          }
        }
      }
    },
    async stepChild(val) {
      if (val === this.rejectList.length + 2) {
        // 获取设计师列表
        await this.fetchRole({ name: 'UI团队设计师' });
        await this.fetchUser({
          role_id: [this.roles[0].id],
          status: '0'
        });
        this.users = deepClone(this.list);
      }
    },
    files(val) {
      if (val && Array.isArray(val)) {
        this.iosfile0 = val.find(val => {
          return val.uploadStage === '0' && val.docType === 'ios';
        });
        this.androidfile0 = val.find(val => {
          return val.uploadStage === '0' && val.docType === 'android';
        });
        this.design0 = val.find(val => {
          return val.uploadStage === '0' && val.docType === 'design';
        });
      }
    }
  },
  computed: {
    ...mapState('jobForm', [
      'fileFolder',
      'designState',
      'jobProfile',
      'fileList'
    ]),
    ...mapState('user', ['currentUser', 'list']),
    ...mapGetters('user', ['isKaDianManager']),
    ...mapState('userForm', ['roles']),
    // 判断是否是不涉及的审核
    isUnvolved() {
      if (
        this.jobProfile.designMap &&
        this.jobProfile.designMap.uninvolved &&
        (!this.jobProfile.designMap.refuseUninvolved ||
          (this.jobProfile.designMap.refuseUninvolved &&
            this.jobProfile.designMap.uninvolved.length >
              this.jobProfile.designMap.refuseUninvolved.length))
      ) {
        return true;
      } else {
        return false;
      }
    },
    isManager() {
      return (
        (this.jobProfile.reviewer &&
          this.jobProfile.reviewer.id === this.currentUser.id) ||
        this.isKaDianManager
      );
    },
    showUserSelect() {
      return (
        ((this.stepChild > this.rejectList.length + 1 &&
          this.currentUser.role.some(item => item.name === 'UI团队负责人') &&
          this.androidfile0 &&
          this.iosfile0) ||
          this.stepChild > this.rejectList.length + 2) &&
        !this.isUnvolved
      );
    },
    isUIManage() {
      return this.currentUser.role.some(item => item.name === 'UI团队负责人');
    },
    disableUserSelect() {
      return this.stepChild !== this.rejectList.length + 2;
    },
    disBtn() {
      if (this.reviewerObj.nopass && this.reviewerObj.nopass.length !== 0) {
        return true;
      } else if (
        this.reviewerObj.finished &&
        this.reviewerObj.finished.length > 0
      ) {
        return true;
      }
      return false;
    },
    dataArr() {
      if (
        this.reviewerObj.nopass &&
        typeof this.reviewerObj.nopass !== 'undefined' &&
        Array.isArray(this.reviewerObj.nopass)
      ) {
        if (
          this.reviewerObj.nopass.slice(-1)[0] &&
          this.reviewerObj.nopass.slice(-1)[0].stage === 'load_upload' &&
          this.reviewerObj.nopass.slice(-1)[0].fake
        ) {
          return this.reviewerObj.nopass.slice(0).reverse();
        }
      }
      let arr = this.reviewerObj.nopass
        ? this.reviewerObj.nopass.slice(0).reverse()
        : [];
      return arr;
    },
    dataArrCenter() {
      let res = this.dataArr.filter((item, index) => {
        return (
          (!item.fake &&
            item.fake !== false &&
            index !== 0 &&
            item.stage !== 'suggest') ||
          (this.dataArr[index + 1] && this.dataArr[index + 1].fake === false) ||
          (this.dataArr[index + 1] && !this.dataArr[index + 1].fake)
        );
      });
      return res;
    },
    iosfiles() {
      let res = [];
      if (this.files && Array.isArray(this.files)) {
        res = this.files.filter(item => {
          return item.uploadStage !== '0' && item.docType === 'ios';
        });
        return res;
      }
      return [];
    },
    androidfiles() {
      let res = [];
      if (this.files && Array.isArray(this.files)) {
        res = this.files.filter(item => {
          return item.uploadStage !== '0' && item.docType === 'android';
        });
        return res;
      }
      return [];
    },
    isDeveloper() {
      let role = getIdsFormList([
        this.jobProfile.spdb_master,
        this.jobProfile.master,
        this.jobProfile.developer
      ]);
      if (role.indexOf(this.currentUser.id) !== -1 || this.isKaDianManager) {
        return true;
      }
      return false;
    },
    isRelevant() {
      // 只允许UI审核人点击确定
      let role = getIdsFormList([this.jobProfile.reviewer]);
      if (
        role.indexOf(this.currentUser.id) !== -1 ||
        this.currentUser.role.some(item => item.name === 'UI团队负责人') ||
        this.isKaDianManager
      ) {
        return true;
      }
      return false;
    },
    isInUITeam() {
      return this.currentUser.role.some(role => {
        return role.name === 'UI团队设计师' || role.name === 'UI团队负责人';
      });
    }
  },
  methods: {
    ...mapActions('jobForm', {
      updateState: 'updateState',
      queryJobProfile: 'queryJobProfile',
      downExcel: 'downExcel'
    }),
    ...mapActions('user', {
      fetchUser: 'fetch'
    }),
    ...mapActions('userForm', {
      fetchRole: 'fetchRole'
    }),
    //跳转研发单元详情
    openUnitDetail(info) {
      this.$router.push({
        path: '/rqrmn/devUnitDetails',
        query: { id: info.rqrmnt_no, dev_unit_no: info.fdev_implement_unit_no }
      });
    },
    async download({ minioPath }) {
      let param = {
        path: minioPath,
        moduleName: 'fdev-design'
      };
      await this.downExcel(param);
    },
    async init() {
      this.id = this.$route.params.id;
      // 查询任务详情
      await this.queryJobProfile({ id: this.id });
      // 进入页面的权限控制
      this.reviewer = this.jobProfile.reviewer.user_name_cn
        ? this.jobProfile.reviewer.user_name_cn
        : '';
      this.jobProfile.reviewer = this.jobProfile.reviewer
        ? this.jobProfile.reviewer
        : {};
      let role = getIdsFormList([
        this.jobProfile.spdb_master,
        this.jobProfile.master,
        this.jobProfile.developer,
        this.jobProfile.tester,
        this.jobProfile.reviewer
      ]);
      if (
        role.indexOf(this.currentUser.id) < 0 &&
        this.currentUser.role.every(item => item.name !== 'UI团队负责人') &&
        !this.isKaDianManager
      ) {
        this.hasAuthority = true;
        return;
      }
      if (
        this.reviewerObj.finished &&
        this.iosfile0 === {} &&
        this.androidfile0 === {}
      ) {
        this.iosfile0 = this.files[0] ? this.files[0] : {};
        this.androidfile0 = this.files[1] ? this.files[1] : this.iosfile[0];
      }
      //判断是否有权限操作  来确定是否展示下一步的操作
      if (
        this.reviewerObj.nopass &&
        this.reviewerObj.nopass[this.reviewerObj.nopass.length - 1].fake &&
        this.checkDisable()
      ) {
        this.reviewerObj.nopass[
          this.reviewerObj.nopass.length - 1
        ].fake = false;
        // 判断是修改建议时获取备注
        if (
          this.reviewerObj.nopass[this.reviewerObj.nopass.length - 1].stage ===
          'load_upload'
        ) {
          this.text = this.jobProfile.designRemark || '';
        }
        return;
      }
    },
    userFilter(val, update, abort) {
      update(() => {
        if (
          this.list &&
          Array.isArray(this.list) &&
          this.list.length > 0 &&
          val
        ) {
          this.users = this.list.filter(user => {
            return (
              user.user_name_cn.indexOf(val) > -1 ||
              (user.user_name_en &&
                user.user_name_en.toLowerCase().includes(val.toLowerCase()))
            );
          });
        }
      });
    },
    async updateDesignState(state, isUninvolved) {
      let params = {
        taskId: this.id,
        newStatus: state,
        remark: state === 'uninvolved' ? this.text1 : this.text,
        name: this.currentUser.name,
        time: date.formatDate(Date.now(), 'YYYY-MM-DD HH:mm:ss')
      };
      if (isUninvolved) {
        //不涉及新增通过还是拒绝字段
        params = { ...params, isInvolved: 'uninvolved' };
      } else if (state === 'fixing') {
        params = { ...params, reviewer: this.reviewer.id };
      } else if (state === 'nopass') {
        // if (
        //   this.reviewerObj.nopass &&
        //   this.reviewerObj.nopass[this.reviewerObj.nopass.length - 1].fake &&
        //   this.reviewerObj.nopass[this.reviewerObj.nopass.length - 1].stage !==
        //     'load_nopass'
        // ) {
        //   // 判断是修改建议步骤时保存备注
        //   params = {
        //     taskId: this.id,
        //     designRemark: this.text
        //   };
        //   await this.updateState(params);
        //   // 查看审核详情
        //   await this.queryJobProfile({ id: this.id });
        //   this.reviewerObj.nopass[
        //     this.reviewerObj.nopass.length - 1
        //   ].fake = false;
        //   this.text = this.jobProfile.designRemark || '';
        //   return;
        // }
        //开发上传截图 或  审核拒绝截图
        if (
          !this.reviewerObj.nopass ||
          (this.reviewerObj.nopass && this.reviewerObj.nopass.length === 0)
        ) {
          this.flag = false;
        } else if (
          this.reviewerObj.nopass &&
          this.reviewerObj.nopass.length > 1 &&
          this.reviewerObj.nopass[this.reviewerObj.nopass.length - 1].stage ===
            'load_upload'
        ) {
          this.flag = true;
        } else {
          this.flag = false;
        }
        params = {
          ...params,
          stage: this.flag ? 'load_upload' : 'load_nopass'
        };
      }
      // 状态更新
      await this.updateState(params);
      // 查看审核详情
      await this.queryJobProfile({ id: this.id });
      number = 0;
      this.text = '';
      this.text1 = '';
      this.uninvolved = false;
    },
    selBtn(type, num) {
      let file0;
      if (type === 0) {
        // ios
        file0 = document.querySelector(`#ios${num}`);
      } else if (type === 1) {
        file0 = document.querySelector(`#android${num}`);
      } else {
        file0 = document.querySelector(`#design${num}`);
      }
      file0.value = '';
      file0.click();
    },
    async selFile(type, nums) {
      let file;
      if (type === 0) {
        // ios
        file = document.querySelector(`#ios${nums}`).files[0];
      } else if (type === 1) {
        file = document.querySelector(`#android${nums}`).files[0];
      } else {
        file = document.querySelector(`#design${nums}`).files[0];
      }
      file = file ? file : { size: '', name: '' };
      let length = file.name.split('.').length;
      let finalName = file.name.split('.')[length - 1];
      if (finalName !== 'zip') {
        errorNotify('上传的文件只能为压缩文件zip格式!');
        return;
      }
      let uploadParam = new FormData();
      if (type === 0) {
        uploadParam.append('fileType', 'ios');
      } else if (type === 1) {
        uploadParam.append('fileType', 'android');
      } else {
        uploadParam.append('fileType', 'design');
      }
      uploadParam.append('taskId', this.id);
      uploadParam.append('fileName', file.name);
      uploadParam.append('uploadStage', nums);
      uploadParam.append('remark', this.text);
      uploadParam.append('file', file);
      let config = {
        headers: {
          'Content-Type': 'multipart/form-data',
          Accept: 'application/json',
          Authorization: this.$q.localStorage.getItem('fdev-vue-admin-jwt')
        }
      };
      axios
        .post(
          `${baseUrl}ftask/api/task/doc/uploadDesignDoc`,
          uploadParam,
          config
        )
        .then(async res => {
          if (res.data.code !== 'AAAAAAA') {
            errorNotify('上传失败，请重试!');
            return;
          }
          if (this.stepChild < this.rejectList.length + 2) {
            await this.queryJobProfile({ id: this.id });
          }
          this.files = res.data.data.designDoc || [];
          successNotify('上传成功！');
        })
        .catch(err => {
          errorNotify('上传失败，请重试!');
        });
    },
    async nextStep() {
      this.loadingBtn = true;
      if (this.isUnvolved) {
        //不涉及
        if (this.shape === '0') {
          //拒绝跳过申请
          await this.updateDesignState('refuseUninvolved');
        } else {
          //同意跳过申请
          await this.updateDesignState('finished', true);
        }
      } else {
        if (this.stepChild === this.rejectList.length + 0) {
          if (this.uninvolved) {
            await this.updateDesignState('uninvolved');
          } else {
            //点击确认按钮  触发交易 任务详情下designMap 就会有report字段  待后台确认
            // await this.updateDesignState('nopass');
          }
        } else if (this.stepChild === this.rejectList.length + 1) {
          if (this.uninvolved) {
            await this.updateDesignState('uninvolved');
          } else {
            await this.updateDesignState('wait_allot');
          }
        } else if (this.stepChild === this.rejectList.length + 2) {
          await this.updateDesignState('fixing');
        } else if (this.stepChild > this.rejectList.length + 2) {
          if (this.shape === '1') {
            try {
              await this.updateDesignState('finished');
            } catch (error) {
              this.loadingBtn = false;
            }
          } else {
            try {
              await this.updateDesignState('nopass');
            } catch (e) {
              this.loadingBtn = false;
            }
          }
        }
      }
      this.loadingBtn = false;
    },
    disBtnAudit(index) {
      if (this.reviewerObj.finished && this.reviewerObj.finished.length > 0) {
        return true;
      }
      if (
        (this.reviewerObj.nopass[index].stage === 'load_nopass' ||
          this.reviewerObj.nopass[index].stage === 'suggest') &&
        this.reviewerObj.nopass[index + 1] &&
        this.reviewerObj.nopass[index + 1].fake
      ) {
        return false;
      }
      if (index === this.reviewerObj.nopass.length - 1) {
        return false;
      }
      if (index !== this.reviewerObj.nopass.length - 1) {
        return true;
      }
      if (
        this.reviewerObj.nopass[index].stage === 'load_nopass' &&
        index !== 0
      ) {
        return false;
      }
      return true;
    },
    done(index) {
      return index < this.reviewerObj.nopass.length;
    },
    getAndroidfile(index) {
      // let idx = this.dataArrCenter.length - index;
      if (this.androidfiles && this.androidfiles.length > 0) {
        let res = this.androidfiles.find(item => {
          return item.uploadStage == index;
        });
        return res;
      }
    },
    getIosfile(index) {
      // let idx = this.dataArrCenter.length - index;
      if (this.iosfiles) {
        return this.iosfiles.find(item => {
          return item.uploadStage == index;
        });
      }
    },
    // 判断下一步按钮是否可以点击
    checkDisable() {
      return false;
    },
    isFinish(val, ind) {
      return val && val.finished && val.finished.length > 0 && ind === 0
        ? '通过'
        : '拒绝';
    },
    //跳过UI审核
    jumpUI() {
      this.openJump = true;
    },
    closeDialog() {
      this.text = '';
      this.openJump = false;
    },
    onSubmit() {
      this.openJump = false;
      this.uninvolved = true;
      this.nextStep();
    }
  },
  async created() {
    await this.init();
  }
};
</script>

<style lang="stylus" scoped>
.btn-margin {
  margin-top: 40px;
}

.inputStyle {
  >>> .q-field__native {
    height: 63px;
    width: 280px;
    min-height: 0;
    max-height: 100px;
  }
  >>> .q-textarea .q-field__control{
    min-height: 0;
  }
}

.q-stepper--vertical .q-stepper__step {
  min-height: 130px;
}

.upload-align >>> .q-uploader__file-header-content .q-uploader__title {
  text-align: left;
}

.div1 {
  display: flex;
  border-style: solid;
  border-width: 10px 0;
  border-color: rgb(245, 247, 253);
  height: 60px;
  align-items: center;
  padding: 10px 20px;
}

.div2 {
  flex: 1;
}

.page-header {
  padding: 0;
}

.div3 {
  display: flex;
}

.div4 {
  flex: 2.5;
}

.span2 {
  font-size: 16px;
  color: #000;
}

.div5 {
  text-align: center;
  margin-bottom: 50px;
}

.div6 {
  padding: 16px 0;
  margin-top: 10px;
}

.step {
  margin-bottom 32px
  width 930px
}

.check {
  background-color: var(--q-color-primary) !important;
  color: #fff !important;
}

.name {
  margin: 32px 0 6px;
}

.time {
  color: #9e9e9e;
  font-size: 0.8em;
}

div >>>.q-stepper__step-inner {
  text-align: left !important;
  color: #9e9e9e !important;
}

.bottomDiv {
  padding-bottom: 5px;
}

.leftDiv {
  flex: 2.5;
}

.div-1 {
  width: 100%;
}

.div-2 {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.div-3 {
  flex: 2;
  display: flex;
}

.div-4 {
  margin-bottom: 10px;
}

.div-5 {
  margin-bottom: 10px;
  flex: 1;
}

.div-6 {
  display: flex;
  padding-top: 10px;
}

.div-7 {
  flex: 2;
}

.div-8 {
  flex: 1;
}

.none {
  display: none;
}

.div-9 {
  display: flex;
  flex-direction: column;
  width: 150px;
}

.div-10 {
  margin-bottom: 5px;
}

a {
  word-break: break-all;
  -webkit-line-clamp: 2;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  display: -webkit-box !important;
  overflow: hidden;
  line-height: 25px;
  max-height: 50px;
}

.rightItem {
  margin-left: -20px;
  flex: 3;
}

.mainDiv
  >>> .q-stepper--vertical .q-stepper__step
      min-height 250px


.leftItem {
  flex: 3;
}

.checkbox1 {
  align-items: center;
  color: var(--q-color-primary);
  margin-left: -10px;
}

.padding-20 {
  padding: 20px;
}

.a-link {
  line-height: 36px;
  font-size 16px
  max-height: none;
}

.distribution {
  border: 1px #0063C5 dashed;
  padding: 32px 20px;
  border-radius: 6px;
  width: 564px;
}

.backgroundC {
  background: #F7F7F7;
  border-radius: 4px;
  width: 930px;
  // height: 238px;
  padding: 20px 32px;
}
.red
  color #ff4147

.primary
  color #00bf4a
.footStyle
  text-align center
.title1
  display inline-block
  min-width 64px
  font-size 16px
  color #333
  line-height 16px
  font-family PingFangSC-Medium
  font-weight 600
.title2
  display inline-block
  width 80px
  font-size 14px
  color #333
  line-height 14px
  font-family PingFangSC-Medium
.timeStyle
  font-size 14px
  color #666
.timeStyle1
  font-size 14px
  color #333
.flex1
  flex 1
a
  font-size 14px
  line-height 14px
.dian
  width 6px
  height 6px
  background #0663BE
  border-radius 3px
  margin-right 8px
.mr60
  margin-right 60px
.hg350
  height 300px
.jumpStyle
  position absolute
  right 32px
  top 160px
  a
    text-decoration underline
.borderb
  background #ddd
  height 1px
  margin 0 32px 32px
.doneTile
  display inline-block
  line-height 22px
  font-size 18px
  font-weight 600
.pd0
  padding 0
.ft600
  font-weight 600
.pt12
  padding-top 12px
.hg310
  height 310px
.header
  padding 10px 32px
.header
  >>>.label-font
    font-size 16px
.order
  width 32px
  margin-right 16px
  .circle
    width 24px
    height 24px
    line-height 22px
    border 1px solid #0063C5
    border-radius 50%
    color #0063C5
  .vertical-line
    height 100%
    border-right 4px dotted #D8E0EC
    margin-top 12px
.lh14
  line-height 14px
.fileNameStyle
  font-size 12px
  color #999999
  line-height 12px
.pdl14
  padding-left 14px
</style>
