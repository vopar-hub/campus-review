import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'
import './styles/main.css'

console.log('1. 所有模块导入成功')

const app = createApp(App)
const pinia = createPinia()

console.log('2. Vue 应用和 Pinia 创建成功')

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

console.log('3. 图标注册成功')

app.use(pinia)
app.use(router)
app.use(ElementPlus)

console.log('4. 插件安装成功')

// 初始化用户状态
const userStore = useUserStore()
userStore.init()

console.log('5. 用户状态初始化成功')

app.mount('#app')

console.log('6. 应用已挂载')
console.log('#app 内容:', document.getElementById('app')?.innerHTML.substring(0, 100))
