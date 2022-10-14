import { mapState, mapActions } from 'vuex';
export const formValidate = {
  data() {
    return {
      useRuled: true
    };
  },
  computed: {
    ...mapState('demandsForm', {
      oaNOStatus: 'oaNOStatus'
    })
  },
  methods: {
    ...mapActions('demandsForm', {
      queryDemandByOaContactNo: 'queryDemandByOaContactNo'
    }),
    // OA联系单校验
    regRule(val) {
      if (!val) {
        return true;
      }
      const value = val.replace(/(^\s*)|(\s*$)/g, '');
      const arr = value.split('-');
      if (arr.length > 1) {
        const arr1 = arr.slice(1);
        const arr2 = ['M001', 'N001', '001'];
        const rule = arr1.some(item => {
          return arr2.indexOf(item) > 0;
        });
        if (rule) {
          return false;
        }
      }
      if (arr.length > 2 && arr.length < 6) {
        // UT-ZZZZ-YYYY-XXXX
        if (arr.length === 4) {
          if (arr[0] !== 'UT') {
            return false;
          } else {
            let re = new RegExp(/^\d+$/);
            let re1 = new RegExp(/^[a-zA-Z]+$/);
            if (!re.test(arr[2]) || !re.test(arr[3])) {
              return false;
            } else {
              return re1.test(arr[1]);
            }
          }
        } else if (arr.length === 5) {
          // 0001-YYYY-L-ZZZ-XXXXXX
          if (arr[0] !== '0001') {
            return false;
          } else {
            let re = new RegExp(/^\d+$/);
            if (!re.test(arr[1]) || !re.test(arr[3]) || !re.test(arr[4])) {
              return false;
            } else {
              return arr[2] === 'L';
            }
          }
        } else {
          return false;
        }
      } else {
        return false;
      }
    },
    addRule(val) {
      let arr = val.split('-');
      if (arr[0].length === 4) {
        if (arr[1] === 'QTXM') {
          if (arr[2].length === 6) return true;
          return false;
        }
        return false;
      }
      return false;
    },
    useRule(val) {
      return val ? this.useRuled : true;
    },
    //校验oa联系单号的唯一性。
    async checkOANum() {
      if (!this.editModel.oa_contact_no.trim()) return;
      await this.queryDemandByOaContactNo({
        oa_contact_no: this.editModel.oa_contact_no
      });
      this.useRuled = this.oaNOStatus ? false : true;
      this.$refs['editModel.oa_contact_no'].validate();
    },
    demandPlanNoRegRule(val) {
      if (val) {
        const value = val.replace(/(^\s*)|(\s*$)/g, '');
        const reg = new RegExp(/^[0-9]{4}[\u4e00-\u9fa5]{2,6}[0-9]{3,4}$/);
        return reg.test(value);
      } else {
        return true;
      }
    },
    leaderGroupIsEmpty(val) {
      let isArray = Object.prototype.toString.call(val).slice(8, -1);
      if (isArray === 'Array' || isArray === 'Null') return true;
      return false;
    },
    demandLeaderIsEmpty(val) {
      return val.length === 0;
    },
    relatePartisEmpty(val) {
      return val.length === 0;
    },
    isExitEmpty(val) {
      return val.length === 0;
    },
    isExitproperty(val) {
      return !val;
    },
    formValidateFun() {
      for (let v of Object.keys(this.$refs)) {
        if (Array.isArray(this.$refs[v])) {
          if (this.$refs[v] && this.$refs[v][0] && !this.$refs[v][0].validate())
            return 'e';
        }
        if (!Array.isArray(this.$refs[v])) {
          if (this.$refs[v] && !this.$refs[v].validate()) return 'e';
        }
      }
    }
  }
};
