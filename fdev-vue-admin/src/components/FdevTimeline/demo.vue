<template>
  <fdev-timeline style="width:360px">
    <fdev-timeline-entry title="2021-01-25">
      <div v-for="i in 4" :key="i" class="row">
        <span class="q-mr-sm text-captain">12:30</span><span>文本内容</span>
      </div>
    </fdev-timeline-entry>

    <fdev-timeline-entry title="2021-01-24">
      <template v-slot:title>
        <div class="row justify-between">
          <span class="text-subtitle2">杨冬竹(看过来）</span>
          <span class="text-caption text-grey-3">2021-07-23 17:00</span>
        </div>
      </template>
      <div v-for="i in 4" :key="i" class="row">
        <span class="q-mr-sm text-captain">12:30</span><span>文本内容</span>
      </div>
    </fdev-timeline-entry>
    <fdev-timeline-entry>
      <template v-slot:title>
        <div class="text-subtitle2">
          特殊样式用title插槽
        </div>
      </template>
      <div v-for="i in 4" :key="i" class="row">
        <span class="q-mr-sm text-captain">12:30</span><span>文本内容</span>
      </div>
    </fdev-timeline-entry>

    <fdev-timeline-entry title="2021-01-22">
      <div v-for="i in 4" :key="i" class="row">
        <span class="q-mr-sm text-captain">12:30</span><span>文本内容</span>
      </div>
    </fdev-timeline-entry>

    <fdev-timeline-entry title="2021-01-21">
      <div v-for="i in 4" :key="i" class="row">
        <span class="q-mr-sm text-captain">12:30</span><span>文本内容</span>
      </div>
    </fdev-timeline-entry>
  </fdev-timeline>
</template>

<script>
export default {
  name: 'FdevTimelineDemo'
};
</script>

<style></style>
