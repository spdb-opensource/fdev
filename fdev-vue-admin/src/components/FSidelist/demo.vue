<template>
  <div class="row no-wrap">
    <f-sidelist
      :dataSource="groupList"
      title="标题"
      label="simpleName"
      class="q-ma-xl"
      @clickItem="selectedItem($event)"
    />
    <f-sidelist
      class="q-ma-xl"
      :dataSource="groupList"
      searchLabel="teamName"
      tip="teamName"
      label="simpleName"
      @clickItem="selectedItem($event)"
    />
    <f-sidelist class="q-ma-xl" :dataSource="noData" title="无数据" />
    <div class="q-ma-xl" style="width:15vw">
      <strong>点击后selectedItem返回数据：</strong><br />{{ selectItem }}
    </div>
  </div>
</template>
<script>
export default {
  name: 'FSidelistDemo',
  data() {
    return {
      searchWord: '',
      groupList: [
        {
          id: '10',
          simpleName: '公共组',
          teamName: '浦发银行-上海分部-开发中心-板块5-公共组'
        },
        {
          id: '11',
          simpleName: '公共组1公共组1公共组1公共组1公共组1',
          teamName: '浦发银行-上海分部-开发中心-板块五-公共组1'
        },
        {
          id: '12',
          simpleName: '公共组2',
          teamName: '浦发银行-上海分部-开发中心-板块五-公共组2'
        },
        {
          id: '13',
          simpleName: '公共组3',
          teamName: '浦发银行-上海分部-开发中心-板块五-公共组3'
        },
        {
          id: '10',
          simpleName: '公共组',
          teamName: '浦发银行-上海分部-开发中心-板块五-公共组'
        },
        {
          id: '11',
          simpleName: '公共组1',
          teamName: '浦发银行-上海分部-开发中心-板块五-公共组1'
        },
        {
          id: '13',
          simpleName: '公共组2',
          teamName: '浦发银行-上海分部-开发中心-板块五-公共组2'
        },
        {
          id: '13',
          simpleName: '公共组3',
          teamName: '浦发银行-上海分部-开发中心-板块五-公共组3'
        }
      ], //列表
      noData: [],
      selectItem: {}
    };
  },
  computed: {},
  watch: {},
  filters: {},
  methods: {
    //选中item
    selectedItem(evt) {
      evt;
      this.selectItem = evt;
    }
  }
};
</script>
