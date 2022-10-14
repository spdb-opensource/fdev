<template>
  <div class="full-width content-stretch">
    <fdev-splitter v-model="splitterModel">
      <template v-slot:before>
        <div class="relative-position">
          <div
            class="property-prompt scroll"
            ref="propertyPrompt"
            v-show="promptVisible"
            :style="{ left: `${selectOffsetX}px`, top: `${selectOffsetY}px` }"
            v-if="filteredProperties.length > 0"
          >
            <fdev-list>
              <fdev-item
                clickable
                class="property-item"
                v-for="(property, index) in filteredProperties"
                :key="index"
                @click="e => onConfirmProperty(e, property)"
                :focused="optionIndex === index"
              >
                <fdev-item-section class="block">
                  <fdev-item-label class="item-label">
                    {{ property.desc }}
                  </fdev-item-label>
                  <fdev-item-label class="q-mt-none">
                    {{ property.label }}
                  </fdev-item-label>
                </fdev-item-section>
              </fdev-item>
            </fdev-list>
          </div>
          <fdev-editor
            v-model="editor"
            ref="editor"
            @paste.native="evt => pasteCapture(evt)"
            @keyup="onKeyUp"
            @keydown="onKeyDown"
            @click="onClick"
            square
            class="editor"
            :disabled="disable"
            :disable="disable"
            content-class="line-number"
            :toolbar="[]"
          />
        </div>
      </template>
      <template v-slot:after>
        <div class="hljs-wrapper">
          <pre
            class="hljs q-mb-lg"
          ><code class="properties line-number" v-html="code" ref="code"></code></pre>
        </div>
      </template>
    </fdev-splitter>
  </div>
</template>

<script>
import hljs from 'highlight.js/lib/highlight';
import properties from 'highlight.js/lib/languages/properties';
import 'highlight.js/styles/github.css';
hljs.registerLanguage('properties', properties);
import LocalStorage from '#/plugins/LocalStorage';
import { getScrollTarget, setScrollPosition } from '#/utils/scroll';

export default {
  props: {
    properties: {
      type: Array
    },
    value: {
      type: String,
      default: ''
    },
    disable: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      splitterModel: 50,
      editor: '',
      code: '',
      promptVisible: false,
      anchorOffset: 0,
      selectOffsetX: 0,
      selectOffsetY: 0,
      optionIndex: -1,
      range: null,
      filteredProperties: []
    };
  },
  watch: {
    editor() {
      this.$emit('noPermission');
      this.renderCode();
    },
    properties: {
      deep: true,
      handler() {
        this.renderCode();
      }
    }
  },
  methods: {
    onKeyDown(e) {
      const keyCode = e.keyCode;
      let caret = this.$refs.editor.caret;

      // up, down
      if ((keyCode === 38 || keyCode === 40) && this.promptVisible) {
        this.stopAndPrevent(e);

        this.optionIndex = this.normalizeToInterval(
          this.optionIndex + (e.keyCode === 38 ? -1 : 1),
          0,
          this.filteredProperties.length - 1
        );

        let target = this.$refs.propertyPrompt;
        if (target === undefined) {
          return;
        }
        let content = target.querySelector('.q-list');
        let scrollTop = target.scrollTop,
          viewHeight = target.clientHeight,
          child = content.children[this.optionIndex],
          childPosTop = content.offsetTop + child.offsetTop,
          childPosBottom = childPosTop + child.clientHeight;

        // destination option is not in view
        if (
          childPosTop < scrollTop ||
          childPosBottom > scrollTop + viewHeight
        ) {
          this.scrollToElement(child);
        }
      }
      // enter
      if (keyCode === 13 && this.promptVisible) {
        this.stopAndPrevent(e);
        if (this.optionIndex >= 0) {
          this.confirmProperty(this.filteredProperties[this.optionIndex]);
        }
      }
      // backspace
      if (keyCode === 8) {
        if (
          caret.el.childNodes.length === 1 &&
          caret.el.firstChild.textContent === ''
        ) {
          this.stopAndPrevent(e);
        }
      }
    },
    onKeyUp(e) {
      const keyCode = e.keyCode;
      this.setupRange();
      let textEle = this.getTextEle();

      this.highlightEditor();

      // $
      if (e.shiftKey && keyCode === 52) {
        textEle.textContent = this.insertText(
          textEle.textContent,
          '<>',
          this.anchorOffset
        );
        this.restore(this.anchorOffset + 1);
      }

      this.togglePropertyPrompt();
    },
    onClick(e) {
      this.setupRange();
      this.highlightEditor();
      this.togglePropertyPrompt();
    },
    onConfirmProperty(e, property) {
      this.stopAndPrevent(e);

      this.confirmProperty(property);
      this.promptVisible = false;
    },
    confirmProperty(property) {
      let textWrapper = this.getTextEle().parentNode;
      let preTextWrapper = textWrapper.previousElementSibling;
      let symbol = this.getSymbol();
      if (symbol === null) {
        return;
      }
      let text =
        textWrapper.textContent.substring(0, symbol.startOffset) +
        textWrapper.textContent.substring(symbol.endOffset);
      let preText = property.desc;
      preText = preText.startsWith('#') ? preText.substring(1) : preText;
      textWrapper.textContent = this.insertText(
        text,
        property.label,
        symbol.startOffset
      );
      if (preTextWrapper.innerText.includes('#')) {
        preTextWrapper.innerText = '# ' + preText;
      }
      // force update
      this.$refs.editor.__onInput();
      this.restore(symbol.startOffset + property.label.length + 1);
    },
    // plaintext pasting
    pasteCapture(evt) {
      let text;
      // onPasteStripFormattingIEPaste;
      evt.preventDefault();
      if (evt.originalEvent && evt.originalEvent.clipboardData.getData) {
        text = evt.originalEvent.clipboardData.getData('text/plain');
        this.runCmd('insertText', text);
      } else if (evt.clipboardData && evt.clipboardData.getData) {
        text = evt.clipboardData.getData('text/plain');
        this.runCmd('insertText', text);
      } else if (window.clipboardData && window.clipboardData.getData) {
        // if (!onPasteStripFormattingIEPaste) {
        // onPasteStripFormattingIEPaste = true;
        this.runCmd('ms-pasteTextOnly', text);
        // }
        // onPasteStripFormattingIEPaste = false;
      }

      this.setupRange();
      this.highlightEditor();
    },
    insertText(origin, text, offset = 0) {
      return origin.substring(0, offset) + text + origin.substring(offset);
    },
    stopAndPrevent(e) {
      e.cancelable !== false && e.preventDefault();
      e.stopPropagation();
    },
    normalizeToInterval(v, min, max) {
      if (max <= min) {
        return min;
      }

      const size = max - min + 1;

      let index = min + ((v - min) % size);
      if (index < min) {
        index = size + index;
      }

      return index === 0 ? 0 : index; // fix for (-a % a) => -0
    },
    getTextWidth(text) {
      let canvas = document.createElement('canvas');
      let context = canvas.getContext('2d');
      context.font =
        '16px "Chinese Quote", -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", ' +
        '"Hiragino Sans GB", "Microsoft YaHei", "Helvetica Neue", Helvetica, Arial, sans-serif,' +
        ' "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol"';
      return context.measureText(text).width;
    },
    // move cursor to the end
    restore(offset) {
      if (this.range && offset !== undefined) {
        let textEle = this.range.endContainer;
        // get text node
        while (textEle.nodeType === 1) {
          textEle = textEle.firstChild;
        }
        // focus editor when clicking the propertyPrompt
        this.selection.removeAllRanges();
        this.selection.addRange(this.range);
        return this.range.setStart(textEle, offset);
      }
      let sel = document.getSelection();
      sel.selectAllChildren(this.$refs.editor.caret.el);
      sel.collapseToEnd();
      this.highlightEditor();
    },
    getTextEle() {
      let textEle =
        this.range !== undefined && this.range !== null
          ? this.range.endContainer
          : document.getSelection().baseNode;

      while (
        textEle !== null &&
        textEle.nodeType === 1 &&
        textEle.nodeName !== 'BR'
      ) {
        textEle = textEle.lastChild;
      }
      return textEle;
    },
    getSymbol(textEle = this.getTextEle(), anchorOffset = this.anchorOffset) {
      let prefix = '$<';
      let suffix = '>';

      if (textEle === null) {
        return null;
      }
      let text = textEle.textContent;

      let leftText = text.substring(0, anchorOffset);
      let rightText = text.substring(anchorOffset);
      let leftTextIndex = leftText.lastIndexOf(prefix);
      let rightTextIndex = rightText.indexOf(suffix);
      if (
        leftTextIndex < 0 ||
        rightTextIndex < 0 ||
        leftTextIndex < leftText.lastIndexOf(suffix)
      ) {
        return null;
      }
      if (
        rightText.indexOf(prefix) > 0 &&
        rightTextIndex > rightText.indexOf(prefix)
      ) {
        return null;
      }
      let leftMatchWord = leftText.substring(leftTextIndex + 2);
      let rightMatchWord = rightText.substring(0, rightTextIndex);

      return {
        text: leftMatchWord + rightMatchWord,
        startOffset: leftTextIndex + 2,
        endOffset: leftTextIndex + (leftMatchWord + rightMatchWord).length + 2
      };
    },
    runCmd() {
      this.$refs.editor.runCmd.call(this.$refs.editor, ...arguments);
      let initEditor = arguments[2];
      // 保存初始化的值
      if (initEditor && initEditor === 'initEditor') {
        let text = this.$refs.editor.caret.el.innerText;
        LocalStorage.set('initEditor', text);
      }
      this.setupRange();
      this.restore();
    },
    highlightEditor() {
      let caret = this.$refs.editor.caret;
      let nodes = caret.el.childNodes;
      let textEle = this.getTextEle();

      nodes.forEach(node => {
        // remove highlight
        node.classList.remove('editing');
      });
      // highlight
      if (textEle) {
        textEle.parentNode.classList.add('editing');
      } else {
        caret.el.lastChild.classList.add('editing');
      }
    },
    togglePropertyPrompt() {
      let textEle = this.getTextEle();
      let symbol = this.getSymbol();
      // toggle propertyPrompt when moving cursor
      this.promptVisible = symbol !== null;
      // popupPropertyPrompt
      if (this.promptVisible) {
        this.filteredProperties = this.properties.filter(property => {
          return property.label.indexOf(symbol.text) > -1;
        });

        this.selectOffsetX =
          this.getTextWidth(
            textEle.textContent.substring(0, symbol.startOffset)
          ) +
          40 -
          textEle.parentNode.scrollLeft;
        this.selectOffsetY = textEle.parentElement.offsetTop + 21;
      } else {
        this.optionIndex = -1;
        this.filteredProperties = this.properties;
      }
    },
    scrollToElement(el) {
      let target = getScrollTarget(el);
      let offset = el.offsetTop;
      setScrollPosition(target, offset);
    },
    // wrap textNode with div
    wrapDiv() {
      let caret = this.$refs.editor.caret;
      let nodes = caret.el.childNodes;
      if (nodes.length !== 0) {
        nodes.forEach(node => {
          if (node.nodeType === 3) {
            let wrapper = document.createElement('div');
            node.parentNode.insertBefore(wrapper, node);
            wrapper.appendChild(node);
          }
        });
      } else {
        let wrapper = document.createElement('div');
        caret.el.appendChild(wrapper);
      }
      this.restore();
    },
    setupRange() {
      let caret = this.$refs.editor.caret;
      let range = caret.range;
      let anchorOffset = range !== undefined ? range.startOffset : 0;

      this.range = range;
      this.selection = caret.selection;
      this.anchorOffset = anchorOffset;
    },
    renderCode() {
      let caret = this.$refs.editor.caret;
      let text = caret.el.innerText;
      const textArr = text.split('\n');
      const textResult = [];
      textArr.forEach(item => {
        if (!item.startsWith('#') && item.includes('=')) {
          this.properties.forEach(property => {
            let replaceWord = '';
            // 是变量
            if (property.isVarivale) {
              replaceWord = new RegExp('\\$\\<' + property.label + '\\>', 'g');
              item = item.replace(replaceWord, property.value);
            } else {
              replaceWord = new RegExp(`${property.label}=((.+)|)`, 'g');
              item = item.replace(replaceWord, ($1, $2) => {
                return property.label + '=' + property.value;
              });
            }
          });
        }
        textResult.push(item);
      });
      text = textResult.join('\n');
      let code = hljs.highlight('properties', text).value;
      // chrome 版本兼容
      let isChrome = window.navigator.userAgent.split('Chrome/').length > 1;
      let chromeVersion = 0;
      let codeStr = '';
      if (isChrome) {
        chromeVersion = window.navigator.userAgent
          .split('Chrome/')[1]
          .split('.')[0];
      }
      if (chromeVersion && chromeVersion < 72) {
        let codeList = code.split('\n');
        for (var j = 0; j < codeList.length; j++) {
          let rowCodeStr = codeList[j].trim();
          if (rowCodeStr === '<span class="hljs-comment">') {
            rowCodeStr += '</span>';
          } else if (rowCodeStr.startsWith('#')) {
            rowCodeStr = '<span class="hljs-comment">' + rowCodeStr;
          } else if (rowCodeStr === '') {
            rowCodeStr = `<span class="hljs-comment"></span>`;
          }
          codeStr += rowCodeStr;
        }
        if (
          codeStr.includes(
            '<span class="hljs-attr">\r</span><span class="hljs-attr">\r</span>'
          )
        ) {
          codeStr = codeStr.replace(
            /<span class="hljs-attr">\r<\/span><span class="hljs-attr">\r<\/span>/g,
            '<span class="hljs-comment"></span>'
          );
        }
        if (codeStr.includes('<span class="hljs-attr">\r</span>')) {
          codeStr = codeStr.replace(
            /<span class="hljs-attr">\r<\/span>/g,
            '\r'
          );
          if (
            codeStr.includes(
              '<span class="hljs-comment"><span class="hljs-comment">'
            )
          ) {
            codeStr = codeStr.replace(
              /<span class="hljs-comment"><span class="hljs-comment">/g,
              '<span class="hljs-comment">'
            );
          }
        }
        codeStr = codeStr.substring(0, codeStr.length - 34);
      } else {
        let codeList = code.split('\n\n');
        // 回车换行 编辑和预览 保持换行一致
        for (var i = 0; i < codeList.length; i++) {
          let rowCodeStr = codeList[i].trim();
          if (rowCodeStr.endsWith('<span class="hljs-comment">')) {
            if (rowCodeStr.startsWith('#')) {
              rowCodeStr =
                '<span class="hljs-comment">' + rowCodeStr + `</span>`;
            } else {
              rowCodeStr = rowCodeStr + '</span>';
            }
          } else if (rowCodeStr === '') {
            rowCodeStr = `<span class="hljs-comment"></span>`;
          } else if (
            rowCodeStr.startsWith('#') &&
            rowCodeStr.endsWith('</span>')
          ) {
            rowCodeStr =
              '<span class="hljs-comment">' +
              rowCodeStr +
              `<span class="hljs-comment"></span>`;
          } else if (
            rowCodeStr.startsWith('<span') &&
            rowCodeStr.endsWith('</span>')
          ) {
            rowCodeStr = rowCodeStr + '<span class="hljs-comment"></span>';
          } else if (
            rowCodeStr.startsWith('<span class="hljs-meta">') &&
            rowCodeStr.endsWith('</span>')
          ) {
            rowCodeStr = rowCodeStr + '<span class="hljs-comment"></span>';
          }
          codeStr += rowCodeStr;
        }
        codeStr = codeStr.substring(0, codeStr.length - 34);
        // replace double '\n' if possible
        // this.code = codeStr.replace(/<\/span><span/g, '</span>\n<span');
      }
      // 预览文件中 配置实体属性没有匹配值 红色字体
      let rowList = codeStr
        .replace(/<\/span><span/g, '</span>\n<span')
        .split('\n');
      let str = '';
      for (let m = 0; m < rowList.length; m++) {
        if (rowList[m].includes('hljs-string')) {
          let rightCode = rowList[m].split('hljs-string')[1];
          if (rightCode.includes('#ERROR#')) {
            rowList[m] = rowList[m].replace(
              /hljs-string/g,
              'hljs-string text-negative'
            );
          }
        }
        str += rowList[m];
      }
      this.code = str.replace(/<\/span><span/g, '</span>\n<span');
      if (this.code.includes('#ERROR#')) {
        this.$emit('error');
      }
      /* 
        初始，注释上面换行 预览处显示有问题
        code = code.replace(/\n\n/g, `<span class="hljs-attr">&nbsp;</span>`);
        this.code = code.replace(/<\/span><span/g, '</span>\n<span');
      */
      // remove .squiggly-error nodes
      let _this = this;
      caret.el.parentNode
        .querySelectorAll('.squiggly-error')
        .forEach(error => error.remove());
      caret.el.childNodes.forEach((node, index) => {
        let text = node.textContent;
        let prefix = '$<';
        let anchorOffset = text.lastIndexOf(prefix);

        while (anchorOffset > -1) {
          let symbol = this.getSymbol(node, anchorOffset + 2);
          if (symbol !== null) {
            anchorOffset = text
              .substring(0, symbol.startOffset - 2)
              .lastIndexOf(prefix);

            // match no property
            if (!this.properties.some(({ label }) => label === symbol.text)) {
              let textWidth = this.getTextWidth(symbol.text);
              let offsetX =
                this.getTextWidth(text.substring(0, symbol.startOffset)) +
                40 -
                node.scrollLeft;
              let offsetY = (index + 1) * 24 + 6;
              let errorWrapper = document.createElement('div');
              errorWrapper.classList.add('squiggly-error');
              errorWrapper.style.left = offsetX + 'px';
              errorWrapper.style.top = offsetY + 'px';
              errorWrapper.style.width = textWidth + 'px';
              caret.el.parentNode.appendChild(errorWrapper);
              /* code */
              let rowCodeList = _this.code.split('\n');
              rowCodeList[index] = rowCodeList[index].replace(
                'hljs-string',
                'hljs-string text-negative'
              );
              _this.code = rowCodeList.join('------').replace(/------/g, '\n');
            }
          } else {
            anchorOffset = text.substring(0, anchorOffset).lastIndexOf(prefix);
          }
        }
      });
      /* 
        this.$emit('input', {
          src: caret.el.innerText, 输入框中的
          text  格式化后的
        });
      */
      this.$emit('input', caret.el.innerText);
    }
  },
  mounted() {
    this.wrapDiv();
    /* this.runCmd(
      'insertText',
      '# fdev property editor v1.0\n' +
        '# please contact shaoyonghua if there are any problems\n'
    ); */
  }
};
</script>

<style lang="stylus">
@import '~#/css/variables.styl';
.editor
  border-top: 0
  position: relative
pre
  margin: 0
  height: 100%
.line-number
  counter-reset: line
  >.hljs-comment
    font-style normal
  >.hljs-meta
    color black
  >div,
  >.hljs-attr,
  >.hljs-meta,
  >.hljs-comment
    counter-increment: line
    position: relative
    &:before
      background: #f8f8f8
      content: counter(line)
      width: 30px
      white-space: nowrap
      display: inline-block
      position: sticky
      left: 0
      padding-right: 18px
      text-align: right
      color: $grey-5
  >div
    white-space: nowrap
    height: 21px
    line-height: 17px
    position: relative
    width: 100%
    overflow: scroll
    border-top: 2px solid transparent
    border-bottom: 2px solid transparent
    overflow-y: hidden;
    &:before
      background: white
    &::-webkit-scrollbar
      width: 0
      height: 0
    &.editing
      border-color: #eeeeee
.property-prompt
  position: absolute
  width: 100%
  max-width: 330px
  max-height: 220px
  overflow: scroll
  border: 1px solid #c8c8c8
  z-index: 2
  background: #F3F3F3;
  .property-item
    padding: 0 4px
    background-color: #f3f3f3
    // height: 40px
    min-height: 40px
    line-height: 20px
    font-size: 14px
    border-bottom: 1px solid #e0e0e0;
.hljs-wrapper
  padding: 10px
  background: #f8f8f8
  height: 100%
  min-height: 10rem
.hljs
  padding: 0
.squiggly-error
  pointer-events: none
  height: 3px
  position: absolute
  background: url(./error.svg) repeat-x bottom left
.no-match-row
  color: black !important;
.item-label
  position: relative;
  top: 2px;
.q-mt-none
  margin-top: 0 !important;
.hljs-string, .hljs-doctag
  color: #2962ff ;
.hljs-comment, .hljs-quote
  color: rgb(46, 125, 50)
.line-number >div
  height: 24px;
  line-height: 21px;
  font-size: 16px;
.properties
  font-size: 16px;
.hljs-attr
  color: #000
</style>
