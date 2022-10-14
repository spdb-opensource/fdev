<template>
  <div class="column">
    <div class="row q-mb-md justify-between">
      <fdev-btn label="添加" @click="num++" />
      <fdev-btn label="初始化" @click="num = 1" />
    </div>
    <div class="row q-gutter-x-md">
      <f-scrollarea style="height:200px" class="bg-indigogrey-0 q-mr-lg">
        <div
          v-for="i in num"
          :key="i"
          style="height:30px;width:30px"
          class="q-my-md"
          :class="`bg-green-${i}`"
        />
      </f-scrollarea>
      <f-scrollarea normal style="height:200px" class="bg-indigogrey-0">
        <div
          v-for="i in num"
          :key="i"
          style="height:30px;width:30px"
          class="q-my-md"
          :class="`bg-green-${i}`"
        />
      </f-scrollarea>
      <div class="column q-gutter-y-md">
        <f-scrollarea horizontal style="width:200px" class="bg-indigogrey-0">
          <div class="row no-wrap">
            <div
              v-for="i in num"
              :key="i"
              style="height:30px;min-width:30px"
              class="q-mx-md"
              :class="`bg-green-${i}`"
            />
          </div>
        </f-scrollarea>
        <f-scrollarea
          horizontal
          style="width:200px"
          normal
          class="bg-indigogrey-0"
        >
          <div class="row no-wrap">
            <div
              v-for="i in num"
              :key="i"
              style="height:30px;min-width:30px"
              class="q-mx-md"
              :class="`bg-green-${i}`"
            />
          </div>
        </f-scrollarea>
      </div>
      <f-scrollarea
        style="width:200px;height:200px"
        class="bg-indigogrey-0"
        bothXY
        ><div style="max-width:250px;max-height:250px" class="row q-gutter-md">
          <div
            v-for="i in num"
            :key="i"
            style="height:30px;width:30px"
            class="q-my-md"
            :class="`bg-green-${i}`"
          />
        </div>
      </f-scrollarea>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FScrollareaDemo',
  data() {
    return {
      num: 1
    };
  }
};
</script>

<style></style>
