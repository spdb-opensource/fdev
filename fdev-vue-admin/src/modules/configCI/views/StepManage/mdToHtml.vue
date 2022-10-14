<template>
  <f-dialog
    right
    :value="value"
    @input="$emit('input', false)"
    @before-show="initDialogData"
    :title="pluginInfo.pluginName + '插件详情'"
  >
    <div v-html="mdToHtml" v-if="mdToHtml" />
    <div v-else>
      <h5 class="text-center">
        该插件无markdown说明
      </h5>
    </div>
  </f-dialog>
</template>
<script>
import showdown from 'showdown';
import { mapState, mapActions } from 'vuex';

export default {
  name: 'MdToHtml',
  data() {
    return {
      mdToHtml: ''
    };
  },
  props: {
    pluginInfo: {
      type: Object,
      default: () => {}
    },
    value: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    ...mapState('configCIForm', ['markDownInfo'])
  },
  methods: {
    ...mapActions('configCIForm', ['queryMarkDown']),
    async initDialogData() {
      await this.queryMarkDown({
        pluginCode: this.pluginInfo.pluginCode
      });
      const converter = new showdown.Converter();
      converter.setOption('tables', true);
      this.mdToHtml = converter.makeHtml(this.markDownInfo.markdown);
    }
  }
};
</script>
<style lang="stylus" scoped>
@import '~#/css/variables.styl';
/deep/:root {
    --side-bar-bg-color: #fafafa;
    --control-text-color: #777;
}

/deep/html {
    font-size: 16px;
}

/deep/body {
    font-family: "Open Sans","Clear Sans","Helvetica Neue",Helvetica,Arial,sans-serif;
    color: rgb(51, 51, 51);
    line-height: 1.6;
}
/deep/#write{
    max-width: 860px;
  	margin: 0 auto;
  	padding: 20px 30px 40px 30px;
	padding-top: 20px;
    padding-bottom: 100px;
}
/deep/#write > ul:first-child,
/deep/#write > ol:first-child{
    margin-top: 30px;
}

/deep/body > *:first-child {
    margin-top: 0 !important;
}
/deep/body > *:last-child {
    margin-bottom: 0 !important;
}
/deep/a {
    color: #4183C4;
}
/deep/h1:hover a.anchor,
/deep/h2:hover a.anchor,
/deep/h3:hover a.anchor,
/deep/h4:hover a.anchor,
/deep/h5:hover a.anchor,
/deep/h6:hover a.anchor {
    /*background: url("../../images/modules/styleguide/para.png") no-repeat 10px center;*/
    text-decoration: none;
}
/deep/h1 tt,
/deep/h1 code {
    font-size: inherit;
}
/deep/h2 tt,
/deep/h2 code {
    font-size: inherit;
}
/deep/h3 tt,
/deep/h3 code {
    font-size: inherit;
}
/deep/h4 tt,
/deep/h4 code {
    font-size: inherit;
}
/deep/h5 tt,
/deep/h5 code {
    font-size: inherit;
}
/deep/h6 tt,
/deep/h6 code {
    font-size: inherit;
}
/deep/p,
/deep/blockquote,
/deep/ul,
/deep/ol,
/deep/dl,
/deep/table{
    margin: 0.8em 0;
}
/deep/li>ol,
/deep/li>ul {
    margin: 0 0;
}
/deep/hr {
    height: 4px;
    padding: 0;
    margin: 16px 0;
    background-color: #e7e7e7;
    border: 0 none;
    overflow: hidden;
    box-sizing: content-box;
    border-bottom: 1px solid #ddd;
}

/deep/body > h2:first-child {
    margin-top: 0;
    padding-top: 0;
}
/deep/body > h1:first-child {
    margin-top: 0;
    padding-top: 0;
}
/deep/body > h1:first-child + h2 {
    margin-top: 0;
    padding-top: 0;
}
/deep/body > h3:first-child,
/deep/body > h4:first-child,
/deep/body > h5:first-child,
/deep/body > h6:first-child {
    margin-top: 0;
    padding-top: 0;
}
/deep/a:first-child h1,
/deep/a:first-child h2,
/deep/a:first-child h3,
/deep/a:first-child h4,
/deep/a:first-child h5,
/deep/a:first-child h6 {
    margin-top: 0;
    padding-top: 0;
}
/deep/h1 p,
/deep/h2 p,
/deep/h3 p,
/deep/h4 p,
/deep/h5 p,
/deep/h6 p {
    margin-top: 0;
}
/deep/li p.first {
    display: inline-block;
}
/deep/ul,
/deep/ol {
    padding-left: 30px;
}
/deep/ul:first-child,
/deep/ol:first-child {
    margin-top: 0;
}
/deep/ul:last-child,
/deep/ol:last-child {
    margin-bottom: 0;
}
/deep/blockquote {
    border-left: 4px solid #dddddd;
    padding: 0 15px;
    color: #777777;
}
/deep/blockquote blockquote {
    padding-right: 0;
}
/deep/table {
    padding: 0;
    word-break: initial;
}
/deep/table tr {
    border-top: 1px solid #cccccc;
    margin: 0;
    padding: 0;
}
/deep/table tr:nth-child(2n) {
    background-color: #f8f8f8;
}
/deep/table tr th {
    font-weight: bold;
    border: 1px solid #cccccc;
    border-bottom: 0;
    text-align: left;
    margin: 0;
    padding: 6px 13px;
}
/deep/table tr td {
    max-width: 300px;
    border: 1px solid #cccccc;
    text-align: left;
    margin: 0;
    padding: 6px 13px;
}
/deep/table tr th:first-child,
/deep/table tr td:first-child {
    margin-top: 0;
}
/deep/table tr th:last-child,
/deep/table tr td:last-child {
    margin-bottom: 0;
}

/deep/.CodeMirror-gutters {
    border-right: 1px solid #ddd;
}

/deep/.md-fences,
/deep/code,
/deep/tt {
    border: 1px solid #ddd;
    background-color: #f8f8f8;
    border-radius: 3px;
    padding: 0;
    font-family: Consolas, "Liberation Mono", Courier, monospace;
    padding: 2px 4px 0px 4px;
    font-size: 0.9em;
}

/deep/.md-fences {
    margin-bottom: 15px;
    margin-top: 15px;
    padding: 0.2em 1em;
    padding-top: 8px;
    padding-bottom: 6px;
}

/deep/.md-task-list-item > input {
  margin-left: -1.3em;
}
@media print {
    html {
        font-size: 13px;
    }
    table,
    pre {
        page-break-inside: avoid;
    }
    pre {
        word-wrap: break-word;
    }
}

/deep/.md-fences {
	background-color: #f8f8f8;
}
/deep/#write pre.md-meta-block {
	padding: 1rem;
    font-size: 85%;
    line-height: 1.45;
    background-color: #F7F7F7;
    border: 0;
    border-radius: 3px;
    color: #777777;
    margin-top: 0 !important;
}

/deep/.mathjax-block>.code-tooltip {
	bottom: .375rem;
}

/deep/#write>h3.md-focus:before{
	left: -1.5625rem;
	top: .375rem;
}
/deep/#write>h4.md-focus:before{
	left: -1.5625rem;
	top: .285714286rem;
}
/deep/#write>h5.md-focus:before{
	left: -1.5625rem;
	top: .285714286rem;
}
/deep/#write>h6.md-focus:before{
	left: -1.5625rem;
	top: .285714286rem;
}
/deep/.md-image>.md-meta {
    /*border: 1px solid #ddd;*/
    border-radius: 3px;
    font-family: Consolas, "Liberation Mono", Courier, monospace;
    padding: 2px 0px 0px 4px;
    font-size: 0.9em;
    color: inherit;
}

/deep/.md-tag{
	color: inherit;
}

/deep/.md-toc {
    margin-top:20px;
    padding-bottom:20px;
}

/deep/.sidebar-tabs {
    border-bottom: none;
}

/deep/#typora-quick-open {
    border: 1px solid #ddd;
    background-color: #f8f8f8;
}

/deep/#typora-quick-open-item {
    background-color: #FAFAFA;
    border-color: #FEFEFE #e5e5e5 #e5e5e5 #eee;
    border-style: solid;
    border-width: 1px;
}

/deep/#md-notification:before {
    top: 10px;
}

/** focus mode */
/deep/.on-focus-mode blockquote {
    border-left-color: rgba(85, 85, 85, 0.12);
}

/deep/header, .context-menu, .megamenu-content, footer{
    font-family: "Segoe UI", "Arial", sans-serif;
}

/deep/.file-node-content:hover .file-node-icon,
/deep/.file-node-content:hover .file-node-open-state{
    visibility: visible;
}

/deep/.mac-seamless-mode #typora-sidebar {
    background-color: #fafafa;
    background-color: var(--side-bar-bg-color);
}

/deep/.md-lang {
    color: #b4654d;
}

/deep/.html-for-mac .context-menu {
    --item-hover-bg-color: #E6F0FE;
}
/deep/a {
  text-decoration: none;
  color: #065588;
}
/deep/a:hover,
/deep/a:active {
  text-decoration: underline;
}
/deep/table {
	border-collapse: collapse;
	border-spacing: 0;
	margin-bottom: 1.5em;
	font-size: 1em;
}
/deep/thead th,
/deep/tfoot th {
	padding: .25em .25em .25em .4em;
	text-transform: uppercase;
}
/deep/th {
	text-align: left;
}
/deep/td {
	vertical-align: top;
	padding: .25em .25em .25em .4em;
}
/deep/thead.md-table-edit {
	background-color: transparent;
}
/deep/thead {
	background-color: #dadada;
}
/deep/tr:nth-child(even) {
	background: #e8e7e7;
}
/deep/code {
  display: block;
  width: 540px;
  white-space: normal;
  line-height: 40px;
  padding: 10px 0 10px 0;
  border: none;
  background: none;
}
/deep/h2 {
   padding-bottom: .3em;
    font-size: 16px;
    line-height: 1.225;
    border-bottom: 1px solid #eee;
}
/deep/h3 {
    font-size: 14px;
    line-height: 1.43;
}
/deep/h4 {
    font-size: 1.25em;
}
/deep/h5 {
    font-size: 1em;
}
/deep/h6 {
   font-size: 1em;
    color: #777;
}
/deep/h1, /deep/h2, /deep/h3, /deep/h4, /deep/h5, /deep/h6
  font-weight: bold;
  position: relative;
  color: #000;
  line-height: 1.4;
  cursor: text;
</style>
