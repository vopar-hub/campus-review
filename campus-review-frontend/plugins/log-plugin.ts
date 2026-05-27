import * as fs from 'fs'
import * as path from 'path'
import type { Plugin } from 'vite'
import type { LogEntry } from '../src/utils/logger'

interface LogPluginOptions {
  logDir?: string
  formatDate?: (date: Date) => string
  formatFilename?: (date: Date, module: string) => string
}

/**
 * 格式化日期为文件夹名 (YYYY-MM-DD)
 */
function defaultFormatDate(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 格式化日志文件名
 */
function defaultFormatFilename(date: Date, module: string): string {
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  return `${module}-${hour}${minute}${second}.log`
}

/**
 * 确保目录存在
 */
function ensureDir(dirPath: string) {
  if (!fs.existsSync(dirPath)) {
    fs.mkdirSync(dirPath, { recursive: true })
  }
}

/**
 * 格式化日志内容
 */
function formatLogContent(entry: LogEntry): string {
  const argsStr = entry.args && entry.args.length > 0
    ? ' ' + JSON.stringify(entry.args, null, 2)
    : ''

  return `[${entry.timestamp}] [${entry.level}] ${entry.message}${argsStr}\n`
}

/**
 * Vite 日志插件 - 拦截浏览器日志并写入文件
 */
export function logPlugin(options: LogPluginOptions = {}): Plugin {
  const {
    logDir = 'logs',
    formatDate = defaultFormatDate,
    formatFilename = defaultFormatFilename,
  } = options

  // 按模块分组缓存日志，避免频繁写盘
  const logBuffer = new Map<string, LogEntry[]>()
  const FLUSH_INTERVAL = 5000 // 5 秒刷新一次

  let projectRoot = ''

  // 刷新日志到文件
  function flushLogs() {
    if (logBuffer.size === 0) return

    for (const [module, entries] of logBuffer.entries()) {
      if (entries.length === 0) continue

      // 按日期分组
      const logsByDate = new Map<string, LogEntry[]>()
      for (const entry of entries) {
        const date = new Date(entry.timestamp)
        const dateStr = formatDate(date)
        if (!logsByDate.has(dateStr)) {
          logsByDate.set(dateStr, [])
        }
        logsByDate.get(dateStr)!.push(entry)
      }

      // 写入文件
      for (const [dateStr, dateLogs] of logsByDate.entries()) {
        const dateDir = path.join(projectRoot, logDir, dateStr)
        ensureDir(dateDir)

        // 合并同模块日志到同一文件（按小时）
        const logsByHour = new Map<string, LogEntry[]>()
        for (const entry of dateLogs) {
          const date = new Date(entry.timestamp)
          const hour = String(date.getHours()).padStart(2, '0')
          const key = `${module}-${hour}`
          if (!logsByHour.has(key)) {
            logsByHour.set(key, [])
          }
          logsByHour.get(key)!.push(entry)
        }

        for (const [key, hourLogs] of logsByHour.entries()) {
          const filename = `${key}.log`
          const filepath = path.join(dateDir, filename)
          const content = hourLogs.map(formatLogContent).join('')

          // 追加写入
          fs.appendFileSync(filepath, content, 'utf-8')
        }
      }
    }

    logBuffer.clear()
  }

  // 定时刷新
  const flushTimer = setInterval(flushLogs, FLUSH_INTERVAL)

  return {
    name: 'vite-plugin-logger',

    configResolved(config) {
      projectRoot = config.root
    },

    configureServer(server) {
      // 添加日志接收端点
      server.middlewares.use('/__vite_log__', async (req, res) => {
        if (req.method !== 'POST') {
          res.statusCode = 405
          res.end('Method Not Allowed')
          return
        }

        let body = ''
        req.on('data', chunk => {
          body += chunk.toString()
        })

        req.on('end', () => {
          try {
            const entry: LogEntry = JSON.parse(body)

            // 添加到缓冲
            if (!logBuffer.has(entry.module)) {
              logBuffer.set(entry.module, [])
            }
            logBuffer.get(entry.module)!.push(entry)

            res.statusCode = 200
            res.end('OK')
          } catch (error) {
            res.statusCode = 400
            res.end('Invalid JSON')
          }
        })
      })
    },

    buildEnd() {
      // 构建结束时刷新所有日志
      flushLogs()
      clearInterval(flushTimer)
    },

    // 进程退出时刷新
    enforce: 'post',
    closeBundle() {
      flushLogs()
      clearInterval(flushTimer)
    },
  }
}

export default logPlugin
