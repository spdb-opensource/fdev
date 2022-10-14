<template>
  <div class="q-gutter-md">
    {{ model }}
    <f-formitem row label="开箱">
      <fdev-select
        v-model="model"
        :rules="[val => !!val || 'Field is required']"
        :options="options"
        placeholder="自定义placeholder"
      />
    </f-formitem>
    <f-formitem row label="menu最大高度300px，滚动条">
      <fdev-select
        v-model="model3"
        :rules="[val => !!val || 'Field is required']"
        :options="options2"
        placeholder="自定义placeholder"
      />
    </f-formitem>
    <f-formitem row label="option超长label自动展示两行title">
      <fdev-select
        v-model="model1"
        :rules="[val => !!val || 'Field is required']"
        :options="options1"
        placeholder="自定义placeholder"
      />
    </f-formitem>
    <f-formitem row label="option超长label手动展示两行title">
      <fdev-select
        v-model="model2"
        :rules="[val => !!val || 'Field is required']"
        :options="options1"
        placeholder="自定义placeholder"
      >
        <template #option="scope">
          <fdev-item v-bind="scope.itemProps" v-on="scope.itemEvents">
            <fdev-item-section>
              <fdev-item-label
                title="AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                >AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA</fdev-item-label
              >
              <fdev-item-label
                title="BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"
                caption
              >
                BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
              </fdev-item-label>
            </fdev-item-section>
          </fdev-item>
        </template>
      </fdev-select>
    </f-formitem>
    <f-formitem row label="readonly">
      <fdev-select v-model="model" readonly :options="options" />
    </f-formitem>
    <f-formitem row label="use-chips">
      <fdev-select v-model="model" use-input multiple :options="options" />
    </f-formitem>

    <f-formitem row label="测试use-chips">
      <fdev-select v-model="model5" use-input multiple :options="options">
        <template #selected-item="scope">
          <fdev-chip
            removable
            :tabindex="scope.tabindex"
            @remove="scope.removeAtIndex(scope.index)"
            label="1232131312321313123213138765876712321313"
          />
        </template>
      </fdev-select>
    </f-formitem>
    {{ objModel }}
    <f-formitem required alert="提示信息" row label="disable">
      <fdev-select disable v-model="model" :options="options" />
    </f-formitem>
    <f-formitem required row label="多选，单个disabel">
      <fdev-select
        v-model="objModel"
        :options="objOpts"
        option-value="id"
        option-label="desc"
        option-disable="inactive"
        multiple
      />
    </f-formitem>

    {{ modelf }}
    <f-formitem label="use-input，filter" required alert="提示信息">
      <fdev-select
        v-model="modelf"
        use-input
        :options="optionsf"
        @filter="filterFn"
      />
    </f-formitem>
  </div>
</template>

<script>
const stringOptions = [
  'GoogleGoogleGoogleGoogleGoogle',
  'Facebook Facebook Facebook Facebook Facebook ',
  'Twitter',
  'Apple',
  'Oracle'
];

export default {
  name: 'FdevSelectDemo',
  data() {
    return {
      modelf: null,
      optionsf: stringOptions,
      stringOptions,
      objModel: null,
      options: [
        'GoogleGoogleGoogleGoogleGoogle',
        'Facebook Facebook Facebook Facebook Facebook ',
        'Twitter',
        'Apple',
        'Oracle'
      ],
      options1: [
        'GoogleGoogleGoogleGoogleGoogle',
        'Facebook Facebook Facebook Facebook Facebook Facebook Facebook Facebook Facebook Facebook Facebook Facebook Facebook Facebook Facebook',
        'Twitter',
        'Apple',
        'Oracle'
      ],
      options2: [
        '标签一',
        '标签二',
        '标签三',
        '标签四',
        '标签五',
        '标签六',
        '标签七',
        '标签八',
        '标签九',
        '标签十',
        '标签十一',
        '标签十二'
      ],
      objOpts: [
        {
          id: 'goog',
          desc: 'Google'
        },
        {
          id: 'fb',
          desc: 'Facebook'
        },
        {
          id: 'twt',
          desc: 'Twitter'
        },
        {
          id: 'app',
          desc: 'Apple'
        },
        {
          id: 'ora',
          desc: 'Oracle',
          inactive: true
        }
      ],
      model: null,
      model1: '',
      model2: '',
      model3: '',
      model5: []
    };
  },
  methods: {
    filterFn(val, update) {
      if (val === '') {
        update(() => {
          this.optionsf = stringOptions;
        });
        return;
      }
      update(() => {
        const needle = val.toLowerCase();
        this.optionsf = stringOptions.filter(
          v => v.toLowerCase().indexOf(needle) > -1
        );
      });
    }
  }
};
</script>

<style></style>
