<template>
  <div>
    <fdev-btn-dropdown label="空间">
      <fdev-list>
        <fdev-item clickable v-close-popup>
          <fdev-item-section>
            <fdev-item-label>效能</fdev-item-label>
          </fdev-item-section>
        </fdev-item>

        <fdev-item clickable v-close-popup>
          <fdev-item-section>
            <fdev-item-label>虚拟团队1 </fdev-item-label>
          </fdev-item-section>
        </fdev-item>

        <fdev-item clickable v-close-popup>
          <fdev-item-section>
            <fdev-item-label>虚拟团队2</fdev-item-label>
          </fdev-item-section>
        </fdev-item>
      </fdev-list>
    </fdev-btn-dropdown>
  </div>
</template>

<script>
export default {
  name: 'FdevBtnDropdownDemo'
};
</script>

<style lang="stylus" scoped></style>
