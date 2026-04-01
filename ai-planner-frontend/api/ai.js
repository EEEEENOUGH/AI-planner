import request, { baseURL } from '@/utils/request'

// ═══════════════════════════════════════
//  普通对话（非流式）
// ═══════════════════════════════════════

/**
 * 发送 AI 消息（多轮对话，非流式，作为流式降级兜底）
 * @param {{ role: string, content: string }[]} messages
 */
export function sendAiMessage(messages) {
  return request({
    url: '/ai/chat',
    method: 'POST',
    data: messages,
    timeout: 60000
  })
}

// ═══════════════════════════════════════
//  历史记录
// ═══════════════════════════════════════

/**
 * 保存 AI 对话历史
 * @param {{ role: string, content: string }[]} messages  完整上下文
 * @param {string} answerContent  本次 AI 回复内容
 */
export function saveAiHistory(messages, answerContent) {
  return request({
    url: '/ai/history/save',
    method: 'POST',
    data: { messages, answerContent },
    timeout: 30000
  })
}

/**
 * 分页获取 AI 历史记录（按时间倒序）
 * @param {number} pageNum
 * @param {number} pageSize
 */
export function getAiHistory(pageNum = 1, pageSize = 20) {
  return request({
    url: `/ai/history?pageNum=${pageNum}&pageSize=${pageSize}`,
    method: 'GET'
  })
}

// ═══════════════════════════════════════
//  流式对话（SSE）
// ═══════════════════════════════════════

/**
 * 流式发送 AI 消息（SSE），返回关闭流的方法
 *
 * @param {{ role: string, content: string }[]} messages
 * @param {{
 *   onMessage?: (chunk: string) => void,
 *   onError?:   (err: Error) => void,
 *   onDone?:    () => void
 * }} handlers
 * @returns {() => void}  调用此函数可主动关闭流
 */
export function streamAiMessage(messages, handlers = {}) {
  const { onMessage, onError, onDone } = handlers
  const token = uni.getStorageSync('token') || ''

  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), 120000)

  fetch(`${baseURL}/ai/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(messages || []),
    signal: controller.signal
  })
    .then(async (response) => {
      if (!response.ok || !response.body) {
        throw new Error(`STREAM_HTTP_${response.status}`)
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''

      while (true) {
        const { value, done } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })

        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          const trimmed = line.trim()
          if (!trimmed.startsWith('data:')) continue
          const payload = trimmed.slice(5).trim()
          if (!payload) continue
          if (payload === '[DONE]') {
            clearTimeout(timeoutId)
            controller.abort()
            if (onDone) onDone()
            return
          }
          if (onMessage) onMessage(payload)
        }
      }

      clearTimeout(timeoutId)
      if (onDone) onDone()
    })
    .catch((err) => {
      clearTimeout(timeoutId)
      if (onError) onError(err)
    })

  // 返回关闭流的方法，供调用方主动取消
  return () => {
    clearTimeout(timeoutId)
    controller.abort()
    if (onDone) onDone()
  }
} 