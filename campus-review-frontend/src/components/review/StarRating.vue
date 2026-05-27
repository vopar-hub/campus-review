<template>
  <div class="star-rating" :class="{ 'read-only': readOnly }">
    <span
      v-for="star in 5"
      :key="star"
      class="star"
      :class="[
        star <= modelValue ? 'star-filled' : 'star-empty',
        star <= hoverValue && !readOnly ? 'star-hover' : ''
      ]"
      @mouseenter="handleMouseEnter(star)"
      @mouseleave="handleMouseLeave"
      @click="handleClick(star)"
    >
      <!-- 实心星星 - 选中状态显示 -->
      <span v-if="star <= modelValue" class="star-solid-wrapper">
        <svg width="28" height="28" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="starGradient" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#FFE66D" />
              <stop offset="50%" style="stop-color:#FBBF24" />
              <stop offset="100%" style="stop-color:#F59E0B" />
            </linearGradient>
          </defs>
          <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="url(#starGradient)"/>
        </svg>
      </span>
      <!-- 空心星星 - 未选中状态显示 -->
      <span v-else class="star-outlined-wrapper">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="url(#starGradient)" stroke-width="1.5" fill="none"/>
        </svg>
      </span>
    </span>
    <span v-if="showScore" class="score-text">{{ modelValue.toFixed(1) }}分</span>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = withDefaults(defineProps<{
  modelValue: number
  readOnly?: boolean
  showScore?: boolean
}>(), {
  readOnly: false,
  showScore: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: number]
}>()

const hoverValue = ref<number>(0)

const handleMouseEnter = (star: number) => {
  if (!props.readOnly) {
    hoverValue.value = star
  }
}

const handleMouseLeave = () => {
  hoverValue.value = 0
}

const handleClick = (star: number) => {
  if (!props.readOnly) {
    emit('update:modelValue', star)
  }
}
</script>

<style scoped>
.star-rating {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.star {
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-bounce);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.star-solid-wrapper,
.star-outlined-wrapper {
  display: inline-flex;
  transition: all var(--duration-normal) var(--ease-bounce);
}

/* 实心星星状态 */
.star-filled .star-solid-wrapper {
  filter: drop-shadow(0 2px 8px rgba(255, 230, 109, 0.6));
  transform: scale(1.05);
}

/* 空心星星状态 */
.star-empty .star-outlined-wrapper {
  transform: scale(0.95);
  opacity: 0.6;
}

/* 悬停状态 */
.star-hover .star-outlined-wrapper,
.star-hover .star-solid-wrapper {
  filter: drop-shadow(0 4px 12px rgba(255, 230, 109, 0.8));
  transform: scale(1.2);
}

.read-only .star {
  cursor: default;
}

.score-text {
  margin-left: 12px;
  font-size: 16px;
  font-weight: 700;
  background: var(--gradient-warm);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* ========== 暗夜模式样式 ========== */
html.dark .star-filled .star-solid-wrapper {
  filter: drop-shadow(0 2px 10px rgba(255, 230, 109, 0.7));
}

html.dark .star-empty .star-outlined-wrapper {
  opacity: 0.5;
}

html.dark .star-hover .star-outlined-wrapper,
html.dark .star-hover .star-solid-wrapper {
  filter: drop-shadow(0 4px 16px rgba(255, 230, 109, 0.9));
}

html.dark .score-text {
  background: var(--gradient-warm);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
</style>
