<template>
  <div class="q-gutter-md">
    <span>v-model:{{ demo }},mask:{{ maskDemo }}</span>
    <f-formitem label="搜索">
      <fdev-input v-model="demo" clearable
        ><template v-slot:append>
          <f-icon name="search" class="cursor-pointer" />
        </template>
      </fdev-input>
    </f-formitem>
    <f-formitem label="开箱：">
      <fdev-input placeholder="自定义placeholder" v-model="demo" />
    </f-formitem>
    <f-formitem label="带有后缀：">
      <fdev-input v-model="demo" suffix="@suffix" />
    </f-formitem>
    <f-formitem label="disable">
      <fdev-input disable v-model="demo" />
    </f-formitem>
    <f-formitem label="readonly">
      <fdev-input readonly v-model="demo" />
    </f-formitem>
    <f-formitem label="autofocus(large)">
      <fdev-input large autofocus v-model="demo" />
    </f-formitem>
    <f-formitem label="mask(###/##)">
      <fdev-input mask="###/##" v-model="maskDemo" />
    </f-formitem>
    <f-formitem label="带有校验">
      <fdev-input
        v-model="demo"
        :rules="[val => !!val || 'Field is required']"
      />
    </f-formitem>
    <f-formitem label="带有校验,lazy-rules">
      <fdev-input
        clearable
        v-model="demo"
        :rules="[
          val => !!val || '* Required',
          val => val.length < 2 || 'Please use maximum 1 character'
        ]"
        lazy-rules
      />
    </f-formitem>
    <f-formitem label="textarea 不要加clearable属性！！！">
      <fdev-input v-model="textarea" type="textarea" />
    </f-formitem>
  </div>
</template>

<script>
export default {
  name: 'FdevInputDemo',
  data() {
    return { demo: '', maskDemo: '', textarea: '' };
  }
};
</script>

<style></style>
