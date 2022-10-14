<template>
  <fdev-layout>
    <div class="glass">
      <Authorized
        :authority="$route.meta.authority"
        :noMatch="$route.meta.noMatch"
      >
        <transition name="slideInDown">
          <div class="logo" v-if="show">
            <img src="@/assets/logo.gif" />
          </div>
        </transition>
        <transition name="slideInUp">
          <router-view v-if="show" />
        </transition>
        <div class="cd" v-if="cd >= 0">
          00:00:0<span>{{ this.cd }}</span>
        </div>
      </Authorized>
    </div>
    <div
      class="bg"
      :style="{
        backgroundImage: background
      }"
    >
      <template v-for="i in 100">
        <div :key="i" class="snow" />
      </template>
    </div>
  </fdev-layout>
</template>
<script>
import Authorized from '@/components/Authorized';
export default {
  name: 'LoginLayoutWelcome',

  components: { Authorized },

  data() {
    return {
      show: false,
      background: `url(${require('../assets/2021-1.jpeg')})`,
      cd: 2
    };
  },

  mounted() {
    const interval = setInterval(() => {
      this.cd -= 1;
      if (this.cd < 0) {
        this.background = `url(${require('../assets/2021-2.jpeg')})`;
        this.$nextTick(() => {
          // 背景图切换动画之后再显示弹窗
          const timeout = setTimeout(() => {
            this.show = true;
            clearTimeout(timeout);
          }, 1000);
          clearInterval(interval);
        });
      }
    }, 1000);
  }
};
</script>
<style lang="scss">
.bg {
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  height: 100vh;
  transition: 0.5s;
  overflow: hidden;
  filter: drop-shadow(0 0 10px white);
}

/* 毛玻璃，失效 */
.glass {
  position: absolute;
  top: 45%;
  left: 50%;
  z-index: 1;
  transform: translate(-50%, -50%);

  &:hover ~ .bg {
    filter: blur(20px);
  }
}

.cd {
  font-size: 6em;
  color: white;
  margin-top: 80vh;
  text-shadow: 1px;
  animation: flash 1s 3;
}

@keyframes flash {
  from,
  to {
    opacity: 0;
  }

  50% {
    opacity: 1;
  }
}

/* fdev图标 */
.logo {
  text-align: center;
  img {
    width: 200px;
  }
}

/* 雪花 */
@function random_range($min, $max) {
  $rand: random();
  $random_range: $min + floor($rand * (($max - $min) + 1));
  @return $random_range;
}

.snow {
  $total: 100;
  position: absolute;
  width: 12px;
  height: 12px;
  background: white;
  border-radius: 50%;

  @for $i from 1 through $total {
    $random-x: random(1000000) * 0.0001vw;
    $random-time: random_range(30000, 80000) / 100000;
    $random-y: $random-time * 100vh;
    $random-scale: random(10000) * 0.0001;
    $random-offset: random_range(-100000, 100000) * 0.001vw;
    $random-x-end: $random-x + $random-offset;
    $random-x-end-drop: $random-x + ($random-offset / 2);
    $fall_duration: random_range(10, 30) * 1s;
    $fall-delay: random(30) * -1s;
    &:nth-child(#{$i}) {
      opacity: random(10000) * 0.0001;
      transform: translate($random-x, -10px) scale($random-scale);
      animation: fall-#{$i} $fall-duration $fall-delay linear infinite;
    }

    @keyframes fall-#{$i} {
      #{percentage($random-time)} {
        transform: translate($random-x-end, $random-y) scale($random-scale);
      }

      to {
        transform: translate($random-x-end-drop, 100vh) scale($random-scale);
      }
    }
  }
}

.fade-leave-active {
  transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}

.fade-leave-to {
  opacity: 0;
}

.slideInDown-enter-active {
  animation: slideInDown 1s;
}
.slideInUp-enter-active {
  animation: slideInUp 1s;
}
@keyframes slideInDown {
  from {
    transform: translate3d((0, -100%, 0));
    visibility: visible;
  }

  50% {
    transform: translate3d(0, 0, 0);
  }
}

@keyframes slideInUp {
  from {
    transform: translate3d((0, 100%, 0));
    visibility: visible;
  }

  to {
    transform: translate3d(0, 0, 0);
  }
}
</style>
