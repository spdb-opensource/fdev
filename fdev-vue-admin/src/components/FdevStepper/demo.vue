<template>
  <div>
    <fdev-stepper v-model="step" ref="stepper" alternative-labels>
      <fdev-step
        :name="1"
        prefix="1"
        :done="step > 1"
        active-prefix="1"
        title="test-1"
      >
        Page1
      </fdev-step>
      <fdev-step
        :name="2"
        prefix="2"
        :done="step > 2"
        active-prefix="2"
        done-prefix="2"
        title="test-2"
      >
        Page2
      </fdev-step>
      <fdev-step
        :name="3"
        prefix="3"
        active-prefix="3"
        done-prefix="3"
        title="test-3"
      >
        Page3
      </fdev-step>

      <template #navigation>
        <fdev-stepper-navigation>
          <fdev-btn @click="$refs.stepper.next()" label="下一步" normal />
          <fdev-btn
            v-if="step > 1"
            flat
            @click="$refs.stepper.previous()"
            label="上一步"
            class="q-ml-md"
          />
        </fdev-stepper-navigation>
      </template>
    </fdev-stepper>
  </div>
</template>

<script>
export default {
  name: 'FdevStepperDemo',
  data() {
    return {
      step: 1
    };
  }
};
</script>
<style scoped></style>
