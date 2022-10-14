<template>
  <ol
    class="Draggable"
    @dragstart="dragstart"
    @dragover="dragover"
    @dragend="dragend"
    id="Draggable"
  >
    <li
      :draggable="draggable"
      v-for="(item, index) in data"
      :key="index"
      class="Draggable_child"
      :class="{ move: draggable }"
    >
      {{ item[label] }}
    </li>
  </ol>
</template>

<script>
export default {
  name: 'Draggable',
  data() {
    return {
      target: null,
      dragging: null,
      draggingNode: null,
      targetNode: null
    };
  },
  props: {
    data: Array,
    value: Array,
    label: String,
    draggable: {
      type: Boolean,
      default: true
    }
  },
  methods: {
    dragstart(e) {
      const target = e.target;
      e.dataTransfer.setData('te', target.innerText); // 兼容火狐
      this.dragging = this.target = target.innerText; // 获取拖动的值，防止重复触发

      this.draggingNode = this.targetNode = target; // 获取拖动的dom
    },
    dragover(e) {
      const target = e.target;
      if (this.target === target.innerText || target.nodeName !== 'LI') {
        // 防止经过同一个target重复触发；防止经过ol时触发
        return;
      }
      const targetRect = target.getBoundingClientRect(); // 获取交换前target的位置
      const draggingRect = this.draggingNode.getBoundingClientRect(); // 获取交换前dragging的位置
      this.target = target.innerText;

      if (target.animated) {
        return; // 防止动画重发触发
      }

      if (this.index(this.draggingNode) < this.index(target)) {
        target.parentNode.insertBefore(this.draggingNode, target.nextSibling); // dragging在target前面，插入target下一个元素前面
      } else {
        target.parentNode.insertBefore(this.draggingNode, target); // dragging在target后面，插入target前面
      }

      this.animate(targetRect, target);
      this.animate(draggingRect, this.draggingNode);
    },
    dragend(e) {
      const Li = Array.prototype.slice.call(
        document.querySelector('#Draggable').children
      );

      const result = [];
      Li.forEach(li => {
        const item = this.data.find(v => v[this.label] === li.innerText);
        result.push(item);
      });

      this.$emit('input', result);
    },
    index(el) {
      let index = 0;
      while (el && (el = el.previousElementSibling)) {
        index++;
      }
      return index;
    },
    animate(prevRect, target) {
      const currentRect = target.getBoundingClientRect();

      this.css(target, 'transition', 'none');
      this.css(
        target,
        'transform',
        'translate3d(' +
          (prevRect.left - currentRect.left) +
          'px,' +
          (prevRect.top - currentRect.top) +
          'px,0)'
      );

      target.offsetWidth; // 触发重绘
      this.css(target, 'transition', 'all ' + 300 + 'ms');
      this.css(target, 'transform', 'translate3d(0,0,0)');

      clearTimeout(target.animated);
      const _this = this;
      target.animated = setTimeout(function() {
        _this.css(target, 'transition', '');
        _this.css(target, 'transform', '');
        target.animated = false;
      }, 300);
    },
    //给元素添加style
    css(el, prop, val) {
      const style = el && el.style;

      if (!style) {
        return;
      }

      if (val) {
        style[prop] = val + (typeof val === 'string' ? '' : 'px');
      }
    }
  }
};
</script>

<style lang="stylus" scoped>

.Draggable
  padding-left 0
  width 100%
  box-sizing border-box
  list-style-position inside
li.Draggable_child
  font-size: 16px;
  background white
  padding 10px
  margin-top 10px
  box-shadow 0 0 3px rgba(0, 0, 0, .5)
  border-left 5px solid #009688
  &.move
    cursor: move;
</style>
