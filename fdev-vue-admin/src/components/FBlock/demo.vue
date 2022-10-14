<template>
  <div class="column fit q-gutter-y-md">
    <div class="fit bg-indigogrey-0 q-pa-md full-width">
      <f-block
        >开箱样式，页面背景，整个页面同一个背景，适用于大部分页面。<br />包在页面最外层，不需要额外添加<code>padding</code>、<code>margin</code>等。<br />
        <span class="text-h1">页面内容</span></f-block
      >
    </div>
    <div class="fit bg-indigogrey-0 q-pa-md full-width">
      <div class="row no-wrap q-mb-md">
        <f-block block class="col-8 q-mr-md"
          >对于一个页面有多个块的情况（如任务详情页）:<br />每一个块的背景包一个<code>fdev-block</code>,<strong>加上block属性</strong>。<br />块与块之间的间距为<strong>
            16px</strong
          >，可以用<code>q-m(x/y/a/l/r/t/b)-md</code>等公共类</f-block
        >
        <f-block block class="col-grow">模板页右侧块</f-block>
      </div>
      <f-block block class="q-my-md">模板页中部块</f-block>
      <div class="row no-wrap q-mb-md">
        <f-block block class="col-4 q-mr-md">模板页下左块</f-block>
        <f-block block class="col-grow">模板页下右块</f-block>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FBlockDemo'
};
</script>
