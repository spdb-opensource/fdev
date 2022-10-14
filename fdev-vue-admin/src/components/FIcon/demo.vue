<template>
  <div class="column q-gutter-y-xl">
    <div class="row items-center q-gutter-md">
      <div class="column items-center" v-for="icon in iconames" :key="icon">
        <f-icon :name="icon" />
        {{ icon }}
      </div>
    </div>
    <div>
      <div class="text-subtitle1 q-mb-md">通过color改变颜色：</div>
      <div class="row items-center q-gutter-md">
        <div class="column items-center" v-for="icon in iconames" :key="icon">
          <f-icon :name="icon" class="text-primary" />
          {{ icon }}
        </div>
        <div class="column items-center" v-for="(icon, i) in iconames" :key="i">
          <f-icon :name="icon" style="color:red" />
          {{ icon }}
        </div>
      </div>
    </div>
    <div>
      <div class="text-subtitle1 q-mb-md">
        通过width\height改变大小（默认宽、高均为18px）：
      </div>
      <div class="row items-center q-gutter-md">
        <div class="column items-center" v-for="icon in iconames" :key="icon">
          <f-icon :name="icon" :width="36" height="36px" class="text-green-5" />
          {{ icon }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ICONS from './icons';
export default {
  name: 'FIconDemo',
  data() {
    return { iconames: Object.keys(ICONS) };
  }
};
</script>
