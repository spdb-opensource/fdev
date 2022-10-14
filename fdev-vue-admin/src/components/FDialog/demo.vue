<template>
  <div>
    <div class="q-gutter-md">
      <fdev-btn label="standard" @click="openA = true" />
      <fdev-btn label="right" @click="openB = true" />
      <fdev-btn label="中部弹窗头部插槽" @click="openC = true" />
      <fdev-btn label="右侧弹窗头部插槽" @click="openD = true" />
      <fdev-btn label="右侧弹窗带子弹窗" @click="openE = true" />
    </div>
    <f-dialog
      v-model="openA"
      @show="onShow"
      @hide="onHide"
      @before-show="beforeShow"
      @before-hide="beforeHide"
      @before-close="beforeClose"
      title="页面中部弹窗"
    >
      <f-formitem label="内容区域最小高度152px" diaS>
        <fdev-input />
      </f-formitem>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="openA = false"/>
        <fdev-btn label="确定" dialog @click="openA = false"
      /></template>
    </f-dialog>
    <f-dialog
      v-model="openB"
      right
      title="页面右侧弹窗"
      tip="这是一段提示语！！！"
    >
      <h1 v-for="i in n" :key="i">
        AAAAA
      </h1>
      <template v-slot:btnSlot>
        <fdev-btn outline dialog label="plus" @click="() => n++"/>
        <fdev-btn outline dialog label="reduce" @click="() => n--"/><fdev-btn
          label="取消"
          dialog
          outline
          @click="openB = false"/>
        <fdev-btn label="确定" dialog @click="openB = false"
      /></template>
    </f-dialog>
    <f-dialog title="标题" v-model="openC">
      <template v-slot:titleSlot>
        <div class="full-width bg-blue-0 text-subtitle1">我是头部插槽</div>
      </template>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="openC = false"/>
        <fdev-btn label="确定" dialog @click="openC = false"
      /></template>
    </f-dialog>
    <f-dialog title="标题" right v-model="openD">
      <template v-slot:titleSlot>
        <div class="full-width bg-blue-0 text-subtitle1">我是头部插槽</div>
      </template>
      <div class="q-gutter-y-xl">
        <f-formitem required label="label" diaS
          ><fdev-input></fdev-input
        ></f-formitem>
        <f-formitem required label="label" diaS
          ><fdev-input></fdev-input></f-formitem
        ><f-formitem required label="label" diaS
          ><fdev-input></fdev-input></f-formitem
        ><f-formitem required label="label" diaS
          ><fdev-input></fdev-input></f-formitem
        ><f-formitem required label="label" diaS
          ><fdev-input></fdev-input></f-formitem
        ><f-formitem required label="label" diaS
          ><fdev-input></fdev-input></f-formitem
        ><f-formitem required label="label" diaS
          ><fdev-input></fdev-input></f-formitem
        ><f-formitem required label="label" diaS
          ><fdev-input></fdev-input></f-formitem
        ><f-formitem required label="label" diaS
          ><fdev-input></fdev-input></f-formitem
        ><f-formitem required label="label" diaS
          ><fdev-input></fdev-input></f-formitem
        ><f-formitem required label="label11" diaS
          ><fdev-input></fdev-input
        ></f-formitem>
      </div>
      <template v-slot:btnSlot>
        <fdev-btn label="取消" outline dialog @click="openD = false"/>
        <fdev-btn label="确定" dialog @click="openD = false"
      /></template>
    </f-dialog>
    <f-dialog
      childTitle="子弹窗"
      title="带子弹窗"
      @closeLeftDia="leftDia = false"
      right
      :leftDia="leftDia"
      v-model="openE"
      @before-close="beforeClose"
    >
      <fdev-btn label="子弹窗开关" @click="leftDia = !leftDia" />
      <h1>AAAA</h1>
      <h1>AAAA</h1>
      <h1>AAAA</h1>
      <h1>AAAA</h1>
      <h1>AAAA</h1>
      <template v-slot:leftDiaContent>
        <f-scrollarea horizontal style="width:150px">
          <p class="text-h1" v-for="i in 50" :key="i">21313123</p>
        </f-scrollarea></template
      >
      <template v-slot:btnSlot
        ><fdev-btn label="取消" dialog outline @click="openE = false"/>
        <fdev-btn label="确定" dialog @click="openE = false"
      /></template>
      <template v-slot:leftDiaBtnSlot
        ><fdev-btn label="取消child" dialog outline @click="leftDia = false"/>
        <fdev-btn label="确定child" dialog @click="leftDia = false"
      /></template>
    </f-dialog>
    <h5 class="text-red">弹窗表单模板及使用见fdev-formitem的demo</h5>
  </div>
</template>

<script>
export default {
  name: 'FDialogDemo',
  data() {
    return {
      openA: false,
      openB: false,
      openC: false,
      openD: false,
      openE: false,
      leftDia: false,
      n: 5
    };
  },
  methods: {
    beforeClose() {
      window.alert('点击右上角关闭icon的回调函数');
    },
    onShow() {
      window.alert('onShow');
    },
    onHide() {
      window.alert('onHide');
    },
    beforeShow() {
      window.alert('before-show');
    },
    beforeHide() {
      window.alert('before-hide');
    }
  }
};
</script>

<style></style>
