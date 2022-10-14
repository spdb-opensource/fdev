import Vue from 'vue';
import { Card } from 'element-ui';
import './index.css';

export default Vue.extend({
  name: 'OrderCard',

  props: {
    shadow: {
      type: String,
      default: 'hover'
    },
    divider: Boolean,
    clickable: Boolean
  },

  render(h) {
    const slot = {
      header: this.$scopedSlots.header ? this.$scopedSlots.header : undefined
    };
    const on = {
      click: () => {
        if (this.clickable) {
          this.$emit('click');
        }
      }
    };
    return (
      <div {...{ on }} class={{ clickable: this.clickable }}>
        <Card
          class="card"
          scopedSlots={slot}
          body-style="padding: 0"
          shadow={this.shadow}
        >
          <div slot="header" class="clearfix">
            {this.$slots.header}
          </div>

          <div slot="default">
            <div class="card-body">{this.$slots.default}</div>

            {this.$slots.footer && this.divider ? (
              <div class="divider" />
            ) : null}
            <div slot="footer" class="card-footer">
              {this.$slots.footer}
            </div>
          </div>
        </Card>
      </div>
    );
  }
});
