<template>
  <SvgIcon icon="guide" @click="startGuide" class="guide" />
</template>

<script setup lang="ts">
import { watch } from 'vue'
import { driver } from 'driver.js'
import 'driver.js/dist/driver.css'
import { useI18n } from 'vue-i18n'
const { t, locale } = useI18n()

let driverObj: any
function initDriver() {
  driverObj = driver({
    showProgress: true,
    progressText: t('driver.step') + ' {{current}} ' + t('driver.of') + ' {{total}}',
    showButtons: ['next', 'previous', 'close'],
    nextBtnText: t('driver.nextBtnText'),
    prevBtnText: t('driver.prevBtnText'),
    doneBtnText: t('driver.doneBtnText'),
    steps: [
      { element: '.guide', popover: { title: t('driver.guide'), description: t('driver.guide') } },
      { element: '.hamburger', popover: { title: t('driver.hamburger'), description: t('driver.hamburger') } },
      { element: '.screenfull', popover: { title: t('driver.screenfull'), description: t('driver.screenfull') } },
      { element: '.language', popover: { title: t('driver.language'), description: t('driver.language') } },
      { element: '.avatar', popover: { title: t('driver.avatar'), description: t('driver.avatar') } }
    ]
  })
}

initDriver()

function startGuide() {
  if (driverObj) {
    driverObj.drive()
  }
}

watch(
  () => locale,
  () => {
    initDriver()
  },
  {
    deep: true
  }
)
</script>

<style scoped></style>
