import loglevel from 'loglevel'

export type LogLevel = 'trace' | 'debug' | 'info' | 'warn' | 'error' | 'silent'

export interface LoggerOptions {
  level?: LogLevel
  prefix?: string
  sendToServer?: boolean
}

export interface LogEntry {
  level: string
  message: string
  timestamp: string
  module: string
  args?: any[]
}

// 日志模块缓存
const loggers = new Map<string, ReturnType<typeof createLogger>>()

// 默认配置
const defaultOptions: Required<LoggerOptions> = {
  level: import.meta.env.DEV ? 'debug' : 'info',
  prefix: '',
  sendToServer: import.meta.env.DEV,
}

/**
 * 创建带前缀的日志记录器
 */
function createLogger(moduleName: string, options: LoggerOptions = {}) {
  const opts = { ...defaultOptions, ...options }
  const logger = loglevel.getLogger(moduleName)
  logger.setLevel(loglevel.levels[opts.level.toUpperCase() as keyof typeof loglevel.levels] || loglevel.levels.DEBUG)

  const originalMethods = {
    debug: logger.debug,
    info: logger.info,
    warn: logger.warn,
    error: logger.error,
    trace: logger.trace,
  }

  // 重写日志方法，添加时间戳和前缀
  const wrapMethod = (method: keyof typeof originalMethods, levelName: string) => {
    return (message: string, ...args: any[]) => {
      const timestamp = new Date().toISOString()
      const prefix = opts.prefix ? `[${opts.prefix}] ` : ''
      const fullMessage = `${prefix}[${moduleName}] ${message}`

      // 发送到服务器（开发环境）
      if (opts.sendToServer && import.meta.env.DEV) {
        sendLogToServer({
          level: levelName,
          message: fullMessage,
          timestamp,
          module: moduleName,
          args,
        }).catch(() => {
          // 发送失败不重复记录，避免死循环
        })
      }

      // 调用原始方法
      ;(originalMethods[method] as any).call(logger, fullMessage, ...args)
    }
  }

  return {
    debug: wrapMethod('debug', 'DEBUG'),
    info: wrapMethod('info', 'INFO'),
    warn: wrapMethod('warn', 'WARN'),
    error: wrapMethod('error', 'ERROR'),
    trace: wrapMethod('trace', 'TRACE'),
    setLevel: (level: LogLevel) => {
      logger.setLevel(loglevel.levels[level.toUpperCase() as keyof typeof loglevel.levels])
    },
    getDefaultLevel: () => opts.level,
  }
}

/**
 * 发送日志到开发服务器
 */
async function sendLogToServer(entry: LogEntry) {
  try {
    await fetch('/__vite_log__', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(entry),
    })
  } catch {
    // 忽略发送错误
  }
}

/**
 * 获取或创建模块日志记录器
 * @param module 模块名称（支持路径形式，如 'api/auth'）
 * @param options 日志配置选项
 */
export function getLogger(module: string, options: LoggerOptions = {}) {
  // 支持点号或斜杠分隔的模块名
  const moduleName = module.replace(/[./]/g, '-')

  if (!loggers.has(moduleName)) {
    loggers.set(moduleName, createLogger(moduleName, options))
  }
  return loggers.get(moduleName)!
}

/**
 * 全局日志记录器
 */
export const logger = getLogger('app')

/**
 * 设置所有日志记录器的级别
 */
export function setAllLogLevel(level: LogLevel) {
  loggers.forEach((log) => log.setLevel(level))
  logger.setLevel(level)
}

export default logger
