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
    // oaRealNoRegRule(val) {
    //   if (!val) {
    //     return true;
    //   }
    //   const value = val.replace(/(^\s*)|(\s*$)/g, '');
    //   const arr = value.split('-');
    //   if (arr.length > 1) {
    //     const arr1 = arr.slice(1);
    //     const arr2 = ['M001', 'N001', '001'];
    //     const rule = arr1.some(item => {
    //       return arr2.indexOf(item) > 0;
    //     });
    //     if (rule) {
    //       return false;
    //     }
    //   }
    //   if (arr.length > 2 && arr.length < 6) {
    //     // UT-ZZZZ-YYYY-XXXX
    //     if (arr.length === 4) {
    //       if (arr[0] !== 'UT') {
    //         return false;
    //       } else {
    //         let re = new RegExp(/^\d+$/);
    //         let re1 = new RegExp(/^[a-zA-Z]+$/);
    //         if (!re.test(arr[2]) || !re.test(arr[3])) {
    //           return false;
    //         } else {
    //           return re1.test(arr[1]);
    //         }
    //       }
    //     } else if (arr.length === 5) {
    //       // 0001-YYYY-L-ZZZ-XXXXXX
    //       if (arr[0] !== '0001') {
    //         return false;
    //       } else {
    //         let re = new RegExp(/^\d+$/);
    //         if (!re.test(arr[1]) || !re.test(arr[3]) || !re.test(arr[4])) {
    //           return false;
    //         } else {
    //           return arr[2] === 'L';
    //         }
    //       }
    //     } else {
    //       return false;
    //     }
    //   } else {
    //     return false;
    //   }
    // },
    async checkOANum() {
      if (!this.demandModel.oa_real_no.trim()) return;
      await this.queryDemandByOaContactNo({
        oa_contact_no: this.demandModel.oa_real_no
      });
      this.useRuled = this.oaNOStatus ? false : true;
      this.$refs['demandModel.oa_real_no'].validate();
    },
    oaRealNoUseRule(val) {
      return val ? this.useRuled : true;
    },
    techTypeDescRule(val) {
      if (
        this.demandType !== '1' &&
        this.isOtherType(this.demandModel.tech_type)
      ) {
        return val ? true : false;
      } else return true;
    },
    demandLeaderIsEmpty(val) {
      return val.length === 0;
    },
    leaderGroupIsEmpty(val) {
      let isArray = Object.prototype.toString.call(val).slice(8, -1);
      if (isArray === 'Array' || isArray === 'Null') return true;
      return false;
    },
    relatePartisEmpty(val) {
      return val.length === 0;
    },
    isExitEmpty(val) {
      return val.length === 0;
    },
    formValidateFun(demandType) {
      let refArr = Object.keys(this.$refs);
      if (this.demandTypeFun(demandType) !== 'daily') this.removeRefFun(refArr);
      let error = [];
      for (let v of refArr) {
        if (v !== 'stepper') {
          if (Array.isArray(this.$refs[v])) {
            if (
              this.$refs[v] &&
              this.$refs[v][0] &&
              !this.$refs[v][0].validate()
            )
              error.push('e');
          }
          if (!Array.isArray(this.$refs[v])) {
            if (this.$refs[v] && !this.$refs[v].validate()) error.push('e');
          }
        }
      }
      if (error.includes('e')) return 'e';
    },
    demandTypeFun(demandType) {
      const type = {
        0: 'tech',
        1: 'business',
        2: 'daily'
      };
      return type[demandType];
    },
    removeRefFun(refArr) {
      let i = refArr.findIndex(u => u === 'demandModel.demand_desc');
      refArr.splice(i, 1);
    }
  }
};
