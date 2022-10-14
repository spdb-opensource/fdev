import Vue from 'vue';
import { Row, Col } from 'element-ui';
import './index.css';

export default Vue.extend({
  name: 'Description',

  props: {
    label: String,
    span: {
      type: Number,
      default: 8
    },
    align: {
      type: String,
      default: 'left'
    }
  },

  render(h) {
    return (
      <Row class={this.align}>
        <Col class="label" span={this.span}>
          {this.label}：
        </Col>
        <Col class="desc" span={24 - this.span}>
          <div slot="default">{this.$slots.default}</div>
        </Col>
      </Row>
    );
  }
});
