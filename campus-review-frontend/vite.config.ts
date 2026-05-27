/// <reference types="vitest" />
import vue from '@vitejs/plugin-vue'
import { dirname, resolve } from 'path'
import { fileURLToPath } from 'url'
import { defineConfig } from 'vite'
import { logPlugin } from './plugins/log-plugin'

// ESM 模式下使用 fileURLToPath 替代 __dirname
const __dirname = dirname(fileURLToPath(import.meta.url))

export default defineConfig({
  plugins: [
    vue(),
    logPlugin({
      logDir: 'logs',
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      'vue': 'vue/dist/vue.esm-bundler.js'
    }
  },
  server: {
    port: 3000,
    // 开发环境下，前端请求 /api 路径，由 nginx 统一代理
    // 如果本地开发没有 nginx，可以临时启用以下代理配置：
    // proxy: {
    //   '/api': {
    //     target: 'http://localhost:8001',
    //     changeOrigin: true
    //   }
    // }
  },
  css: {
    postcss: './postcss.config.js'
  },
  build: {
    target: ['es2020', 'chrome80']
  },
  test: {
    globals: true,
    environment: 'jsdom',
    include: ['src/**/*.test.{ts,tsx}'],
    setupFiles: ['./src/test/setup.ts'],
  },
})
