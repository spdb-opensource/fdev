<template>
  <div class="q-gutter-md">
    <div style="width:300px" class="q-gutter-y-md bg-blue-1 q-pa-sm">
      <f-formitem full-width label="表单双列dfdf"
        ><fdev-input v-model="inputVal"
      /></f-formitem>
      <f-formitem full-width label="表单双列ddf"
        ><fdev-select v-model="model" readonly :options="options"
      /></f-formitem>
      <f-formitem full-width label="表单双列df"
        ><fdev-input v-model="inputVal"
      /></f-formitem>
      <f-formitem full-width label="表单双列dff"
        ><fdev-input v-model="inputVal"
      /></f-formitem>
    </div>
    <f-formitem required label="表单双列dfsdfdf"
      ><fdev-input
        v-model="inputVal"
        :rules="[val => !!val || 'Field is required']"
    /></f-formitem>
    <f-formitem label="表单单列" diaS
      ><fdev-input v-model="inputVal"
    /></f-formitem>
    <f-formitem required label="页面内部页面内部页面内部页面内部页面内部dd" page
      ><fdev-input
        v-model="inputVal"
        :rules="[val => !!val || 'Field is required']"
    /></f-formitem>
    <f-formitem help="我是一头小猪猪" label="页面内部页面" page
      ><fdev-input v-model="inputVal"
    /></f-formitem>
    <f-formitem
      label="适应外部容器宽度"
      full-width
      help="见-右侧弹窗、双列最大弹窗模板"
      ><fdev-input v-model="inputVal"
    /></f-formitem>

    <f-formitem label="内嵌文字" class="text-no-wrap"
      >我是文字文字文字我是文字文字文字我是文字文字文字我是文字文字文字我是文字文字文字我是文字文字文字我是文字文字文字我是文字文字文字</f-formitem
    >
    <fdev-btn label="单列单行弹窗表单模板" @click="slScDia = true" />
    <fdev-btn label="单列最大弹窗模板" @click="slDia = true" />
    <fdev-btn label="双列最大弹窗模板" v-model="dcDia" @click="dcDia = true" />
    <fdev-btn
      label="双列弹窗表单带有自定义元素模板"
      v-model="dcCusDia"
      @click="dcCusDia = true"
    />

    <fdev-btn
      label="双列最小弹窗模板"
      v-model="dcSlDia"
      @click="dcSlDia = true"
    />
    <fdev-btn label="右侧弹窗模板" v-model="rDia" @click="rDia = true" />

    <f-dialog v-model="slScDia" f-sl-sc title="单列单行弹窗模板">
      <f-formitem label="表单单列单行" diaS
        ><fdev-input v-model="inputVal"
      /></f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="slScDia = false"/>
        <fdev-btn label="确定" dialog @click="slScDia = false"
      /></template>
    </f-dialog>
    <f-dialog v-model="slDia" f-sc title="单列最大弹窗模板（5行）">
      <f-formitem label="表单单列" diaS
        ><fdev-input v-model="inputVal"
      /></f-formitem>
      <f-formitem label="表单单列" diaS
        ><fdev-input v-model="inputVal"
      /></f-formitem>
      <f-formitem label="表单单列" diaS
        ><fdev-input v-model="inputVal"
      /></f-formitem>
      <f-formitem label="表单单列" diaS
        ><fdev-input v-model="inputVal"
      /></f-formitem>
      <f-formitem label="表单单列" diaS
        ><fdev-input v-model="inputVal"
      /></f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="slDia = false"/>
        <fdev-btn label="确定" dialog @click="slDia = false"
      /></template>
    </f-dialog>
    <f-dialog v-model="dcDia" f-dc title="双列最大弹窗模板（6行）">
      <f-formitem label="单行输入" full-width
        ><fdev-input v-model="inputVal" type="textarea"
      /></f-formitem>
      <f-formitem label="表单双列"><fdev-input v-model="inputVal"/></f-formitem>
      <f-formitem label="表单双列"><fdev-input v-model="inputVal"/></f-formitem>
      <f-formitem label="表单双列"><fdev-input v-model="inputVal"/></f-formitem>
      <f-formitem label="表单双列" required
        ><fdev-input v-model="inputVal"
      /></f-formitem>
      <f-formitem label="表单双列"><fdev-input v-model="inputVal"/></f-formitem>
      <f-formitem label="表单双列"><fdev-input v-model="inputVal"/></f-formitem>
      <f-formitem label="表单双列"><fdev-input v-model="inputVal"/></f-formitem>
      <f-formitem label="表单双列" required
        ><fdev-input v-model="inputVal"
      /></f-formitem>
      <f-formitem label="表单双列"><fdev-input v-model="inputVal"/></f-formitem>
      <f-formitem label="表单双列"><fdev-input v-model="inputVal"/></f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="dcDia = false"/>
        <fdev-btn label="确定" dialog @click="dcDia = false"
      /></template>
    </f-dialog>
    <f-dialog v-model="dcCusDia" title="双列弹窗表单带有自定义元素模板">
      <div class="dia-dc-w">
        <div class="bg-blue-1 full-width q-mb-lg q-py-sm">自定义小块块</div>
        <div class="row q-gutter-diaLine justify-between">
          <f-formitem label="表单双列"
            ><fdev-input v-model="inputVal"
          /></f-formitem>
          <f-formitem label="表单双列" required
            ><fdev-input v-model="inputVal"
          /></f-formitem>
          <f-formitem label="表单双列"
            ><fdev-input v-model="inputVal"
          /></f-formitem>
          <f-formitem label="表单双列"
            ><fdev-input v-model="inputVal"
          /></f-formitem>
        </div>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="dcCusDia = false"/>
        <fdev-btn label="确定" dialog @click="dcCusDia = false"
      /></template>
    </f-dialog>
    <f-dialog v-model="dcSlDia" f-dc title="双列最小弹窗模板（3行）">
      <f-formitem label="表单双列"><fdev-input v-model="inputVal"/></f-formitem>
      <f-formitem label="表单单行双列" required
        ><fdev-input v-model="inputVal"
      /></f-formitem>
      <f-formitem label="表单双列"><fdev-input v-model="inputVal"/></f-formitem>
      <f-formitem label="表单单行双列" required
        ><fdev-input v-model="inputVal"
      /></f-formitem>
      <f-formitem label="表单双列"><fdev-input v-model="inputVal"/></f-formitem>
      <f-formitem label="表单单行双列" required
        ><fdev-input v-model="inputVal"
      /></f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="dcSlDia = false"/>
        <fdev-btn label="确定" dialog @click="dcSlDia = false"
      /></template>
    </f-dialog>
    <f-dialog v-model="rDia" right title="右侧弹窗模板">
      <div class="q-gutter-y-diaLine rdia-dc-w row justify-between">
        <f-formitem label="单行输入" full-width
          ><fdev-input v-model="inputVal" type="textarea"
        /></f-formitem>
        <f-formitem
          label="表单双列表单双列表单双列表单双列表单双列表单双列表单双列表单双列表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列表单双列表单双列" required
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列" required
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列" required
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列" required
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列" required
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列" required
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列" required
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双jjkjjljl列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="jkjkjkjkjk"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列" required
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列" required
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列" required
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列" required
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
        <f-formitem label="表单双列"
          ><fdev-input v-model="inputVal"
        /></f-formitem>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="rDia = false"/>
        <fdev-btn label="确定" dialog @click="rDia = false"
      /></template>
    </f-dialog>
    <h5 class="text-red">
      *单列表单：最大5个表单元素（5行）；
      <br />
      *双列表单：最少6个表单元素（3行），最多12个表单元素（6行）
      <br />
      *超过12个表单元素，统一右侧弹窗
    </h5>

    <div class="row border-bottom" style="width:1184px">
      <div class="row full-width border-top">
        <f-formitem
          class="col-4"
          label="需求信息单编号"
          profile
          label-auto
          label-class="bg-indigogrey-0 q-px-lg"
          label-style="height:43px;width:160px;line-height:14px"
          value-style="height:43px;width:234px;"
          value-class="text-center"
        >
          20200002
        </f-formitem>
        <f-formitem
          class="col-4"
          label="需求信息单标题"
          profile
          label-auto
          label-class="bg-indigogrey-0 q-px-lg"
          label-style="height:42px;width:160px;line-height:14px"
          value-style="height:42px;width:234px;"
          value-class="text-center"
        >
          无
        </f-formitem>
        <f-formitem
          class="col-4"
          label="需求书名称"
          profile
          label-auto
          label-class="bg-indigogrey-0 q-px-lg"
          label-style="height:42px;width:160px;line-height:14px"
          value-style="height:42px;width:234px;"
          value-class="text-center"
        >
          无
        </f-formitem>
      </div>
      <div class="row border-top">
        <f-formitem
          class="col-4"
          label="需求信息单编号"
          profile
          label-auto
          label-class="bg-indigogrey-0 q-px-lg"
          label-style="height:42px;width:160px;line-height:14px"
          value-style="height:42px;width:234px;"
          value-class="text-center"
        >
          20200002
        </f-formitem>
        <f-formitem
          class="col-4"
          label="需求信息单标题"
          profile
          label-auto
          label-class="bg-indigogrey-0 q-px-lg"
          label-style="height:42px;width:160px;"
          value-style="height:42px;width:234px;"
          value-class="text-center"
        >
          无
        </f-formitem>
        <f-formitem
          class="col-4"
          label="需求信息单我部收件日期"
          profile
          label-auto
          label-class="bg-indigogrey-0 q-px-lg"
          label-style="height:43px;width:160px;line-height:14px"
          value-style="height:43px;width:234px;"
          value-class="text-center"
        >
          2021-08-10
        </f-formitem>
      </div>
    </div>

    <div class="border" style="width:1184px">
      <f-formitem
        class="full-width"
        label="涉及小组"
        profile
        label-auto
        label-class="bg-indigogrey-0 q-px-lg"
        label-style="height:43px;width:160px;line-height:14px"
        value-class="full-width row items-center q-gutter-x-sm"
        value-style="height:43px;padding-left:12px;line-height:0"
      >
        <div class="row items-center mini-card">
          板块1-财富组——潇潇
        </div>
        <div class="row items-center mini-card">
          板块1-财富组——潇潇
        </div>
      </f-formitem>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FFormitemDemo',
  data() {
    return {
      inputVal: '',
      slDia: false,
      slScDia: false,
      dcSlDia: false,
      dcDia: false,
      rDia: false,
      dcCusDia: false,
      options: [
        'GoogleGoogleGoogleGoogleGoogle',
        'Facebook Facebook Facebook Facebook Facebook ',
        'Twitter',
        'Apple',
        'Oracle'
      ],
      model: null
    };
  }
};
</script>

<style scoped lang="stylus">
border(align='all')
  border-top 1px solid #ddd if align == 'top' || align == 'all'
  border-bottom 1px solid #ddd if align == 'bottom' || align == 'all'
.border-top
  border('top')
.border-bottom
  border('bottom')
.border
  border()
.mini-card
  height 26px
  padding 6px 8px
  background-color #f1f1f1
</style>
