import api from './index'

export const aiApi = {
  chat(messages) {
    return api.post('/ai/chat', { messages })
  },

  getHistory() {
    return api.get('/chat/history')
  },

  clearHistory() {
    return api.delete('/chat/clear')
  },

  async chatStream(messages, onText, onBooks, onDone) {
    const token = localStorage.getItem('token')
    const headers = { 'Content-Type': 'application/json' }
    if (token) headers['Authorization'] = `Bearer ${token}`

    const response = await fetch('/api/ai/chat/stream', {
      method: 'POST',
      headers,
      body: JSON.stringify({ messages })
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    if (!response.body) {
      const text = await response.text()
      throw new Error(text || 'Empty response body')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let completed = false

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      while (true) {
        const idx = buffer.indexOf('\n\n')
        if (idx === -1) break

        const part = buffer.slice(0, idx)
        buffer = buffer.slice(idx + 2)

        const lines = part.split('\n')
        let currentEvent = ''
        let dataParts = []

        for (const line of lines) {
          if (line.startsWith('event:')) {
            let val = line.slice(6)
            if (val.startsWith(' ')) val = val.slice(1)
            currentEvent = val
          } else if (line.startsWith('data:')) {
            let val = line.slice(5)
            if (val.startsWith(' ')) val = val.slice(1)
            dataParts.push(val)
          }
        }

        const data = dataParts.join('\n')

        if (currentEvent === 'done') {
          completed = true
          onDone?.()
        } else if (currentEvent === 'books') {
          try {
            onBooks?.(JSON.parse(data))
          } catch { }
        } else if (data) {
          onText?.(data)
        }

        await new Promise(r => setTimeout(r, 0))
      }
    }

    if (!completed) onDone?.()
  }
}
