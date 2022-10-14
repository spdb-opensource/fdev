<template>
  <div class="column full-width q-pa-lg">
    <fdev-table
      title="表头(原则上必须要有)"
      style="width:80vw"
      :style="sticky === 'my-sticky-head-table' ? `max-height:500px` : ''"
      :class="sticky"
      :columns="columns"
      :data="data"
      titleIcon="list_s_f"
      :onSearch="onSearch"
    >
      <template v-slot:top-right>
        <div class="q-gutter-md">
          <fdev-btn
            label="固定首尾两列"
            @click="sticky = 'my-sticky-column-table'"
            normal
          />
          <fdev-btn
            label="固定尾列"
            @click="sticky = 'my-sticky-last-column-table'"
            normal
          />
          <fdev-btn
            label="固定头部"
            @click="sticky = 'my-sticky-head-table'"
            normal
          />
          <fdev-btn label="不固定任何列" @click="sticky = ''" normal />
          <fdev-btn label="操作按钮" normal />
          <fdev-btn label="操作按钮" normal />
        </div>
      </template>
      <template v-slot:top-bottom>
        <f-formitem page label="页面内部">
          <fdev-input v-model="name"
        /></f-formitem>
        <f-formitem page label="页jkjjk面内部">
          <fdev-select v-model="model" :options="options" />
        </f-formitem>
        <f-formitem
          page
          label="页面内部页面内部页面内部页面内部页面内部页面内部页面内部页面内部页面内部"
        >
          <fdev-input v-model="name"
        /></f-formitem>
        <f-formitem page label="页面内部">
          <fdev-select v-model="model" :options="options" />
        </f-formitem>
        <f-formitem page label="页面内部">
          <fdev-input clearable v-model="name"
        /></f-formitem>
        <f-formitem page label="页面内部">
          <fdev-select
            use-chips
            multiple
            use-input
            v-model="chips"
            :options="options"
          />
        </f-formitem>
        <f-formitem page label="页面内部">
          <fdev-input readonly clearable v-model="name"
        /></f-formitem>
        <f-formitem page label="页面内部">
          <fdev-select readonly clearable v-model="model" :options="options" />
        </f-formitem>
      </template>
      <template v-slot:top-bottom-opt>
        <div class="self-start">top-bottom-opt插槽猪猪猪</div>
      </template>
      <template v-slot:body-cell-manage="props">
        <fdev-td :props="props">
          <div class="q-gutter-x-sm row no-wrap">
            <fdev-btn flat label="操作A" />
            <fdev-btn flat label="操作B" />
          </div>
        </fdev-td>
      </template>
    </fdev-table>
    <div class="text-red">
      <p>*必须有表头</p>
      <p>
        *不允许使用top-right、top-bottom、top-bottom-opt、title-icon(用fdevIcon属性替换)之外的所有插槽，尤其不允许覆盖表头等（后续会删掉这些插槽，不对外暴露）
      </p>
      <p>*无数据、表头等均使用开箱样式，不用自定义样式覆盖</p>
      <p>
        *操作按钮放在top-right插槽中,除重置、查询外，均使用normal样式btn（原则上无换行，若出现，反馈给谢凝玉，讨论具体解决方案）
      </p>
      <p>*筛选、输入等表单组件放在top-bottom插槽中</p>
      <p>*筛选、输入等表单组件用formitem包裹，输入page属性</p>
      <p>*列选择器放在top-bottom插槽中的最后一个</p>
      <p>
        *表格首尾固定，直接使用公共类class="my-sticky-column-table"，不需要自己写(按场景需求使用)
      </p>
      <p>
        *表格宽度定好，不要出现表格过长，唤起页面滚动条的情况
      </p>
      <p>
        *操作列固定，除UI新版切图中的特殊场景外，默认使用文字按钮，按钮间距为sm，写法见以上demo
      </p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FdevTableDemo',
  data() {
    return {
      chips: [],
      sticky: '',
      options: [
        'Google',
        'FacebookFacebookFacebookFacebookFacebook',
        'Twitter',
        'Apple',
        'Oracle'
      ],
      model: null,
      name: '',
      columns: [
        {
          name: 'name',
          required: true,
          style: 'width:200px',
          label: '名称类(需求、任务、流水线等）',
          field: row => row.name,
          format: val => `${val}`,
          sortable: true
        },
        {
          name: 'calories',
          align: 'left',
          label: 'Calories',
          field: 'calories',
          sortable: true
        },
        { name: 'fat', label: 'Fat (g)', field: 'fat', sortable: true },
        { name: 'carbs', label: 'Carbs (g)', field: 'carbs' },
        { name: 'protein', label: 'Protein (g)', field: 'protein' },
        { name: 'sodium', label: 'Sodium (mg)', field: 'sodium' },
        {
          name: 'calcium',
          label: 'Calcium (%)',
          field: 'calcium',
          sortable: true,
          sort: (a, b) => parseInt(a, 10) - parseInt(b, 10)
        },
        {
          name: 'iron',
          label: 'Iron (%)',
          field: 'iron',
          sortable: true,
          sort: (a, b) => parseInt(a, 10) - parseInt(b, 10)
        },
        {
          name: 'manage',
          label: '操作',
          field: 'iron',
          sortable: true,
          sort: (a, b) => parseInt(a, 10) - parseInt(b, 10)
        }
      ],
      data: [
        {
          name: '我是需求小名称',
          calories:
            'Frozen YogurtFrozen YogurtFrozen YogurtFrozen YogurtFrozen Yogurt',
          fat: 6.0,
          protein: 4.0,
          sodium: 87,
          calcium: '14%',
          iron: '1%'
        },
        {
          name: '我是任务小名称',
          fat: 9.0,
          carbs: 37,
          protein: 4.3,
          sodium: 129,
          calcium: '8%',
          iron: '1%'
        },
        {
          name: '我是流水线小名称',
          calories: 262,
          fat: 16.0,
          carbs: 23,
          protein: 6.0,
          sodium: 337,
          calcium: '6%',
          iron: '7%'
        },
        {
          name: '我是日志小名称',
          calories:
            'Frozen YogurtFrozen YogurtFrozen YogurtFrozen YogurtFrozen Yogurt',
          fat: 6.0,
          carbs: 24,
          protein: 4.0,
          sodium: 87,
          calcium: '14%',
          iron: '1%'
        },
        {
          name: '我是任务的名称',
          calories: 237,
          fat: 9.0,
          carbs: 37,
          protein: 4.3,
          sodium: 129,
          calcium: '8%',
          iron: '1%'
        },
        {
          name: '我是流水线小小的可爱的名称',
          calories: 262,
          fat: 16.0,
          carbs: 23,
          protein: 6.0,
          sodium: 337,
          calcium: '6%',
          iron: '7%'
        },
        {
          name: '我是需求小小的可爱小名称',
          calories:
            'Frozen YogurtFrozen YogurtFrozen YogurtFrozen YogurtFrozen Yogurt',
          fat: 6.0,
          carbs: 24,
          protein: 4.0,
          sodium: 87,
          calcium: '14%',
          iron: '1%'
        },
        {
          name: '我是任务小小的可爱小名称',
          calories: 237,
          fat: 9.0,
          carbs: 37,
          protein: 4.3,
          sodium: 129,
          calcium: '8%',
          iron: '1%'
        },
        {
          name: '我是流水线小小小的可爱名称',
          calories: 262,
          fat: 16.0,
          carbs: 23,
          protein: 6.0,
          sodium: 337,
          calcium: '6%',
          iron: '7%'
        }
      ]
    };
  },
  methods: {
    onSearch() {
      alert('onSearch');
    }
  }
};
</script>

<style scoped lang="stylus"></style>
