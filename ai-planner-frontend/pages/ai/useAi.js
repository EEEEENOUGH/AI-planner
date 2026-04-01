import { nextTick, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getAiHistory, saveAiHistory, sendAiMessage, streamAiMessage } from '@/api/ai'

const THINKING_PLACEHOLDER = 'AI 正在思考中...'
const ERROR_PLACEHOLDER = '抱歉，网络连接失败，请重试。'
const MAX_CONTEXT_TURNS = 100
const MAX_CONTEXT_CHARS = 2000000
const OPENING_MESSAGE = {
  msgId: 1,
  role: 'assistant',
  content: '你好，我是你的 AI 学习助手。\n告诉我你的目标考试、当前阶段和每日可学习时长，我来帮你规划。'
}

export function useAi() {
  const messages = ref([{ ...OPENING_MESSAGE }])
  const inputText = ref('')
  const sending = ref(false)
  const scrollIntoViewId = ref('')
  const historyPageNum = ref(1)
  const historyPageSize = 20
  const historyFinished = ref(false)
  const loadingMoreHistory = ref(false)
  const loadedHistoryKeys = ref(new Set())

  // ── ID 生成器 ──
  const nextMsgId = (() => { let c = Date.now(); return () => { c += 1; return c } })()

  // ── 滚动到底部 ──
  const scrollToBottom = () => {
    nextTick(() => {
      scrollIntoViewId.value = ''
      nextTick(() => { scrollIntoViewId.value = 'chat-bottom' })
    })
  }

  // ── 历史去重 key ──
  const buildHistoryKey = (q, a) => `${q}__${a}`

  // ── 历史记录映射为消息列表 ──
  const mapHistoryRecordsToMessages = (records = [], skipDuplicate = false) => {
    const normalized = Array.isArray(records) ? [...records].reverse() : []
    const mapped = []
    for (const item of normalized) {
      const q = (item?.questionContent || '').trim()
      const a = (item?.answerContent || '').trim()
      if (!q && !a) continue
      const key = buildHistoryKey(q, a)
      if (skipDuplicate && loadedHistoryKeys.value.has(key)) continue
      loadedHistoryKeys.value.add(key)
      if (q) mapped.push({ msgId: nextMsgId(), role: 'user', content: q })
      if (a) mapped.push({ msgId: nextMsgId(), role: 'assistant', content: a })
    }
    return mapped
  }

  // ── 初始加载历史 ──
  const loadHistory = async () => {
    try {
      historyPageNum.value = 1
      historyFinished.value = false
      loadedHistoryKeys.value = new Set()
      const pageData = await getAiHistory(historyPageNum.value, historyPageSize)
      const records = Array.isArray(pageData?.records) ? pageData.records : []
      messages.value = [{ ...OPENING_MESSAGE }, ...mapHistoryRecordsToMessages(records)]
      if (records.length < historyPageSize) historyFinished.value = true
    } catch (e) {
      console.warn('加载AI历史记录失败:', e)
      messages.value = [{ ...OPENING_MESSAGE }]
      historyFinished.value = true
    } finally {
      scrollToBottom()
    }
  }

  // ── 上滑加载更多历史 ──
  const loadMoreHistory = async () => {
    if (loadingMoreHistory.value || historyFinished.value) return
    loadingMoreHistory.value = true
    try {
      const nextPage = historyPageNum.value + 1
      const pageData = await getAiHistory(nextPage, historyPageSize)
      const records = Array.isArray(pageData?.records) ? pageData.records : []
      if (!records.length) { historyFinished.value = true; return }
      const olderMessages = mapHistoryRecordsToMessages(records, true)
      if (!olderMessages.length) {
        historyPageNum.value = nextPage
        if (records.length < historyPageSize) historyFinished.value = true
        return
      }
      messages.value = [{ ...OPENING_MESSAGE }, ...olderMessages, ...messages.value.slice(1)]
      historyPageNum.value = nextPage
      if (records.length < historyPageSize) historyFinished.value = true
    } catch (e) {
      console.warn('加载更多AI历史记录失败:', e)
    } finally {
      loadingMoreHistory.value = false
    }
  }

  onLoad(async (query) => {
    await loadHistory()
    if (query && query.initMsg) {
      inputText.value = decodeURIComponent(query.initMsg)
      await send()
    }
  })

  // ── Markdown 简单格式化 ──
  const formatMarkdown = (text) => {
    if (!text) return ''
    return text
      .replace(/\r\n/g, '\n')
      .replace(/^#{1,6}\s?/gm, '')
      .replace(/\*\*(.*?)\*\*/g, '$1')
      .replace(/__(.*?)__/g, '$1')
      .replace(/`([^`]+)`/g, '$1')
      .replace(/^[-*+]\s+/gm, '• ')
  }

  // ── 上下文构建工具 ──
  const isValidChatMessage = (msg) => {
    if (!msg || !msg.role || !msg.content) return false
    if (!['user', 'assistant'].includes(msg.role)) return false
    if (msg.content === THINKING_PLACEHOLDER || msg.content === ERROR_PLACEHOLDER) return false
    return true
  }
  const extractResponseText = (res) => {
    if (typeof res === 'string') return res
    if (res && typeof res === 'object') {
      if (typeof res.data === 'string') return res.data
      if (typeof res.result === 'string') return res.result
      if (typeof res.message === 'string') return res.message
    }
    return ''
  }
  const normalizeContent = (c) => {
    if (!c) return ''
    let v = String(c).trim()
    if (v.length > MAX_CONTEXT_CHARS) v = v.slice(-MAX_CONTEXT_CHARS)
    return v
  }
  const buildContextMessages = () =>
    messages.value
      .filter(isValidChatMessage)
      .map(m => ({ role: m.role, content: normalizeContent(m.content) }))
      .filter(m => m.content.length > 0)
      .slice(-(MAX_CONTEXT_TURNS * 2))

  // ── 流式追加 chunk ──
  const appendStreamChunk = (msgId, chunk) => {
    const idx = messages.value.findIndex(i => i.msgId === msgId)
    if (idx === -1) return
    if (messages.value[idx].content === THINKING_PLACEHOLDER) messages.value[idx].content = ''
    messages.value[idx].content += chunk
    scrollToBottom()
  }

  // ── 持久化历史 ──
  const persistHistoryIfNeeded = async (ctx, ans) => {
    const a = (ans || '').trim()
    if (!a || a === ERROR_PLACEHOLDER || a === THINKING_PLACEHOLDER) return
    try { await saveAiHistory(ctx, a) } catch (e) { console.warn('保存AI答疑历史失败:', e) }
  }

  // ── 发送消息 ──
  const send = async () => {
    if (sending.value) return
    const content = inputText.value.trim()
    if (!content) return

    messages.value.push({ msgId: nextMsgId(), role: 'user', content })
    inputText.value = ''
    const aiMsgId = nextMsgId()
    messages.value.push({ msgId: aiMsgId, role: 'assistant', content: THINKING_PLACEHOLDER })
    scrollToBottom()
    sending.value = true

    const contextArray = buildContextMessages()
    let streamClosed = false

    const closeStream = streamAiMessage(contextArray, {
      onMessage: (chunk) => { appendStreamChunk(aiMsgId, chunk) },
      onError: async () => {
        if (streamClosed) return
        streamClosed = true
        try {
          const reply = extractResponseText(await sendAiMessage(contextArray))
          const idx = messages.value.findIndex(i => i.msgId === aiMsgId)
          if (idx !== -1) {
            messages.value[idx].content = reply || 'AI 导师正在休息，请稍后再试。'
            await persistHistoryIfNeeded(contextArray, messages.value[idx].content)
          }
        } catch (e) {
          const idx = messages.value.findIndex(i => i.msgId === aiMsgId)
          if (idx !== -1) messages.value[idx].content = ERROR_PLACEHOLDER
        } finally {
          sending.value = false
          scrollToBottom()
        }
      },
      onDone: async () => {
        if (streamClosed) return
        streamClosed = true
        const idx = messages.value.findIndex(i => i.msgId === aiMsgId)
        if (idx !== -1) await persistHistoryIfNeeded(contextArray, messages.value[idx].content)
        sending.value = false
        scrollToBottom()
      }
    })

    setTimeout(() => { if (!streamClosed && closeStream) closeStream() }, 130000)
  }

  return {
    messages, inputText, sending, scrollIntoViewId,
    loadingMoreHistory, historyFinished,
    loadMoreHistory, formatMarkdown, send
  }
}
