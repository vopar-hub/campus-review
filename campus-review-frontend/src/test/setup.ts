import { config } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach } from 'vitest'

// 在每个测试前设置 Pinia
beforeEach(() => {
  const pinia = createPinia()
  setActivePinia(pinia)
})

// 全局配置 Vue Test Utils
config.global.stubs = {
  RouterLink: true,
}
