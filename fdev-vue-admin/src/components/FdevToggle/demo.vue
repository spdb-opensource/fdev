<template>
  <div class="q-gutter-y-xl">
    <div class="q-guitter-y-lg">
      <div class="text-h4">normal(宽36px 高20px)：</div>
      <div class="q-gutter-x-lg">
        <fdev-toggle v-model="value1" label="周一李沁" left-label />
        <fdev-toggle v-model="value2">
          <span class="q-ml-sm">周二宋轶</span>
        </fdev-toggle>
        <fdev-toggle v-model="value3" label="周三陈钰琪" />
        <fdev-toggle v-model="value4" label="周四佟丽娅" disable />
        <fdev-toggle v-model="value5" label="周五古力娜扎" disable />
      </div>
    </div>

    <div class="q-guitter-y-lg">
      <div class="text-h4">large(宽44px 高24px)、Ayyay Model：</div>
      <div class="q-gutter-x-lg">
        <fdev-toggle
          v-model="selection"
          size="lg"
          label="周一李沁"
          val="周一李沁"
        />
        <fdev-toggle
          v-model="selection"
          size="lg"
          label="周二宋轶"
          val="周二宋轶"
        />
        <fdev-toggle
          v-model="selection"
          size="lg"
          label="周三陈钰琪"
          val="周三陈钰琪"
        />
        <fdev-toggle
          v-model="selection"
          size="lg"
          label="周四佟丽娅"
          val="周四佟丽娅"
          disable
        />
        <fdev-toggle
          v-model="selection"
          size="lg"
          label="周五古力娜扎"
          val="周五古力娜扎"
          disable
        />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FdevToggleDemo',
  data() {
    return {
      value1: true,
      value2: false,
      value3: null,
      value4: true,
      value5: false,
      selection: ['周一李沁', '周三陈钰琪']
    };
  }
};
</script>
