<template>
  <div class="q-gutter-lg q-ma-lg">
    <span class="text-subtitle1"
      >按钮中加入图标：ficon属性，将ficon中的图标引入按钮。不允许使用icon属性或引用外部icon:</span
    ><br />
    <fdev-btn label="主按钮" />
    <fdev-btn disable label="主按钮" />
    <fdev-btn outline label="线性（次要）按钮" />
    <fdev-btn outline disable label="线性（次要）按钮" />
    <fdev-btn ficon="add" normal label="常显操作" />
    <fdev-btn ficon="run" normal disable label="常显操作" />
    <fdev-btn flat ficon="uncollect" label="文字按钮" />
    <fdev-btn flat ficon="uncollect" disable label="文字按钮" />
    <fdev-btn flat label="文字按钮" />
    <fdev-btn dash label="方形虚线按钮" />
    <fdev-btn dash disable label="方形虚线按钮" />
    <fdev-btn roundash label="圆形虚线按钮" />
    <fdev-btn ficon="delete" roundash label="圆形虚线按钮" />
    <fdev-btn ficon="run" roundash disable label="圆形虚线按钮" />
    <fdev-btn dialog ficon="collect" label="弹窗主" />
    <fdev-btn dialog disable label="弹窗主" />
    <fdev-btn dialog outline label="弹窗次" />
    <fdev-btn dialog outline disable label="弹窗次" />
    <fdev-btn plain label="纯素按钮" />
    <fdev-btn flat flat-colorless label="文字按钮，无特殊颜色" />
  </div>
</template>

<script>
export default {
  name: 'FdevBtnDemo'
};
</script>

<style lang="stylus" scoped></style>
