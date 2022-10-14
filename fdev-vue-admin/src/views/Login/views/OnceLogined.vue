<template>
  <Loading :visible="loading">
    <fdev-dialog v-model="alert" persistent>
      <div class="q-pa-md banner-div" ref="banner" v-if="show">
        <fdev-carousel
          animated
          v-model="slide"
          arrows
          navigation
          infinite
          control-color="blue"
        >
          <fdev-carousel-slide :name="1" :img-src="img1" class="slide-item" />
          <fdev-carousel-slide :name="2" :img-src="img2" class="slide-item" />
          <fdev-carousel-slide :name="3" :img-src="img3" class="slide-item" />
          <fdev-carousel-slide :name="4" :img-src="img4" class="slide-item" />
          <fdev-carousel-slide :name="5" :img-src="img5" class="slide-item" />
          <fdev-carousel-slide :name="6" :img-src="img6" class="slide-item" />
          <fdev-carousel-slide :name="7" :img-src="img7" class="slide-item" />
        </fdev-carousel>
        <fdev-btn
          icon="close"
          class="close-container"
          @click="closeBanner"
          v-show="lastShow"
        ></fdev-btn>
      </div>
    </fdev-dialog>
  </Loading>
</template>

<script>
import Loading from '@/components/Loading';
import img1 from '@/assets/banner-1.jpg';
import img2 from '@/assets/banner-2.jpg';
import img3 from '@/assets/banner-3.jpg';
import img4 from '@/assets/banner-4.jpg';
import img5 from '@/assets/banner-5.jpg';
import img6 from '@/assets/banner-6.jpg';
import img7 from '@/assets/banner-7.jpg';
import { mapState } from 'vuex';

export default {
  name: 'OnceLogined',
  components: { Loading },
  data() {
    return {
      loading: false,
      slide: 1,
      alert: true,
      img1: img1,
      img2: img2,
      img3: img3,
      img4: img4,
      img5: img5,
      img6: img6,
      img7: img7,
      show: true,
      imgList: []
    };
  },
  validations: {},
  computed: {
    lastShow: function() {
      if (this.slide === 7) {
        return true;
      }
      return false;
    },
    ...mapState('user', ['currentUser'])
  },
  methods: {
    async closeBanner() {
      this.show = !this.show;
      if (
        this.currentUser.is_once_login == 1 ||
        this.currentUser.is_once_login == 3
      ) {
        let redirect = this.$route.query.currentUrl;
        if (redirect) {
          this.$router.push(redirect);
        } else {
          this.$router.push('/account/center');
        }
      } else {
        await this.$store.dispatch('user/queryOnceLogin', {});
      }
    }
  },
  created() {},
  mounted() {}
};
</script>

<style lang="stylus" scoped>
.banner-div{
  width: 100%;
  position: relative;
}
@media (min-width:600px){
  .q-dialog__inner--minimized > div{
    max-width: 100%;
  }
}
 .slide-item{
  background-size: contain!important;
  background-repeat: no-repeat;
}
.close-container{
  display: flex;
  align-items: center;
  justify-content: center;
  height: 50px;
  width: 50px;
  background: #2196f3;
  opacity: .6;
  border-radius: 50%;
  position: absolute;
  top: 16px;
  right: 16px;
  cursor: pointer;
}
.q-carousel{
  height: 600px;
}
</style>
