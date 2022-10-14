import Vue from 'vue';
import { Select, FormItem, Option } from 'element-ui';

export default Vue.extend({
  name: 'TSelect',

  props: {
    value: {
      required: true
    },
    prop: String,
    options: {
      type: Array,
      default: () => []
    },
    optionLabel: {
      type: String,
      default: 'label'
    },
    optionValue: {
      type: String,
      default: 'value'
    },
    placeholder: String,
    disabled: Boolean,
    multiple: Boolean,
    filterable: Boolean,
    remote: Boolean,
    reserveKeyword: Boolean,
    clearable: Boolean,
    loading: Boolean,
    allowCreate: Boolean,
    remoteMethod: Function,
    defaultFirstOption: Boolean,
    label: String,
    labelWidth: String,
    filterMethod: Function,
    fullWidth: {
      type: Boolean,
      default: true
    }
  },

  methods: {
    change(val) {
      this.$emit('input', val);
    },
    optionsList() {
      return this.options.map(item => {
        return (
          <Option label={item[this.optionLabel]} value={item[this.optionValue]}>
            {this.$scopedSlots.options ? this.$scopedSlots.options(item) : null}
          </Option>
        );
      });
    }
  },

  render(h) {
    const on = {
      change: val => this.change(val)
    };
    const select = (
      <Select
        value={this.value}
        disabled={this.disabled}
        placeholder={this.placeholder}
        clearable={this.clearable}
        multiple={this.multiple}
        remote={this.remote}
        reserve-keyword={this.reserveKeyword}
        filterable={this.filterable}
        remote-method={this.remoteMethod}
        loading={this.loading}
        defaultFirstOption={this.defaultFirstOption}
        allow-create={this.allowCreate}
        class={this.fullWidth ? 'full-width' : ''}
        filter-method={this.filterMethod}
        {...{ on }}
      >
        {this.optionsList}
      </Select>
    );

    if (this.prop) {
      return (
        <FormItem
          prop={this.prop}
          label={this.label ? this.label + ':' : ''}
          label-width={this.labelWidth}
        >
          {select}
        </FormItem>
      );
    } else {
      return select;
    }
  }
});
