<template>
  <div class="row items-start div-bottom" :class="dense ? 'dense' : ''">
    <div
      class="row"
      :class="stackLabel ? 'col-12' : `${responsive[col]} ${justify[align]}`"
    >
      <div class="input-field row items-center">
        <f-image :name="icon" :size="size" v-if="icon" />
        <span v-if="!contracted">
          {{ label }}<span v-if="optional" class="text-grey-5">（选填）</span>
        </span>
      </div>
      <div class="col text-right" v-if="iconAppend">
        <fdev-btn
          :icon="iconAppend"
          @click="$emit('iconClick')"
          flat
        ></fdev-btn>
      </div>
    </div>
    <div
      class="input-value row items-center"
      :class="stackLabel ? 'col-12' : 'col'"
    >
      <div class="col">
        <slot />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Field',
  props: {
    icon: String,
    label: String,
    labelCol: {
      default: 6,
      type: Number
    },
    size: String,
    align: {
      default: 'left',
      type: String
    },
    contracted: {
      default: false,
      type: Boolean
    },
    dense: {
      default: false,
      type: Boolean
    },
    optional: {
      default: false,
      type: Boolean
    },
    stackLabel: {
      default: false,
      type: Boolean
    },
    iconAppend: String
  },
  data() {
    return {
      responsive: {
        1: 'col-sm-1',
        2: 'col-sm-2',
        3: 'col-sm-3',
        4: 'col-sm-4',
        5: 'col-sm-5',
        6: 'col-sm-6'
      },
      justify: {
        left: 'justify-start',
        center: 'justify-center',
        right: 'justify-end'
      }
    };
  },
  computed: {
    col() {
      if (this.responsive.hasOwnProperty(this.labelCol)) {
        return this.labelCol;
      }
      return 3;
    }
  },
  methods: {}
};
</script>

<style lang="stylus" scoped>

.input-field {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.54);
  padding-right: 6px;
  height: 40px;

  i {
    font-size: 24px;
    margin-right: 14px;
  }
}

.input-value {
  min-height: 40px;
  color: rgba(0, 0, 0, 0.87);
}

.dense {
  .input-field, .input-value {
    height: auto;
    min-height: auto;
  }
}

.div-bottom {
  margin-bottom: 12px;
}
</style>
