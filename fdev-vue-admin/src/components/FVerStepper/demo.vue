<template>
  <div>
    <f-ver-stepper
      :words="[
        { value: 1, inteval: 36 },
        { value: 2 },
        { value: 3 },
        { value: 4 },
        { value: 5 }
      ]"
    >
      <template #content-1>
        <div class="content-area">
          <div class="bg-cyan full-height">
            123
          </div>
        </div>
      </template>

      <template #content-2>
        <div class="content-area">
          <div class="bg-cyan full-height">
            123
          </div>
        </div>
      </template>

      <template #content-3>
        <div class="content-area">
          <div class="bg-cyan full-height">
            123
          </div>
        </div>
      </template>

      <template #content-4>
        <div class="content-area">
          <div class="bg-cyan full-height">
            123
          </div>
        </div>
      </template>

      <template #content-5>
        <div class="content-area">
          <div class="bg-cyan full-height">
            123
          </div>
        </div>
      </template>
    </f-ver-stepper>
  </div>
</template>

<script>
export default {
  name: 'FVerStepperDemo'
};
</script>
<style scoped lang="stylus">
.content-area
  width 400px
  height 200px
  padding-bottom 20px
</style>
