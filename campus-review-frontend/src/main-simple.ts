import { createApp } from 'vue'
import App from './App.vue'

// 直接挂载，不加载其他模块
const app = createApp(App)
app.mount('#app')

console.log('Vue app mounted successfully!')
