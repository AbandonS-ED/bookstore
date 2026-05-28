<template>
  <div class="admin-dashboard">

    <!-- Stats Row -->
    <div class="stats-row">
      <div class="stat-card" v-for="(s, si) in stats" :key="s.label">
        <div class="stat-top">
          <div class="stat-icon" :class="s.color">{{ s.icon }}</div>
          <div v-if="si === 0 && revenueChange" class="stat-change" :class="revenueChange.dir">
            <template v-if="revenueChange.dir === 'up'">↑</template>
            <template v-else>↓</template>
            {{ revenueChange.pct }}
          </div>
        </div>
        <div class="stat-value">{{ s.value }}</div>
        <div class="stat-label">{{ s.label }}</div>
      </div>
    </div>

    <!-- Charts Row -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-header">
          <div>
            <h3>销售趋势</h3>
            <div class="chart-sub">{{ salesRange === 'week' ? '近7天' : '近7个月' }} ¥{{ salesBars.reduce((s, b) => s + Number(b.val.replace(/,/g, '')), 0).toLocaleString() }}</div>
          </div>
          <div class="chart-tabs">
            <div class="chart-tab" :class="{ active: salesRange === 'week' }" @click="salesRange = 'week'">近7天</div>
            <div class="chart-tab" :class="{ active: salesRange === 'month' }" @click="salesRange = 'month'">整月</div>
          </div>
        </div>
        <div class="bar-chart">
          <div v-for="(b, i) in salesBars" :key="i" class="bar-group">
            <div class="bar" :style="{ height: b.pct + '%', background: 'linear-gradient(180deg,var(--color-accent),var(--color-accent-muted))' }">
              <div class="bar-tooltip">¥{{ b.val }}</div>
            </div>
            <div class="bar-label">{{ b.label }}</div>
          </div>
        </div>
      </div>

      <div class="chart-card">
        <div class="chart-header"><h3>品类分布</h3></div>
        <div class="donut-wrap">
          <div class="donut" :style="{ background: donutBg }">
            <div class="donut-center">
              <div class="donut-val">{{ categoryCount }}</div>
              <div class="donut-lbl">个分类</div>
            </div>
          </div>
          <div class="donut-legend">
            <div v-for="(c, i) in categoryStats" :key="c.name" class="legend-item">
              <span class="legend-dot" :style="{ background: catColors[i % catColors.length] }"></span>
              {{ c.name }} {{ c.pct }}%
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Orders -->
    <div class="table-card">
      <div class="table-header">
        <h3>最近订单</h3>
        <router-link to="/admin/orders">查看全部 →</router-link>
      </div>
      <table>
        <thead>
          <tr><th>订单号</th><th>客户</th><th>金额</th><th>状态</th><th>时间</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="order in recentOrders" :key="order.id">
            <td class="cell-mono">{{ order.orderNo }}</td>
            <td>{{ order.username || '—' }}</td>
            <td class="cell-price">¥{{ order.totalAmount }}</td>
            <td><span class="table-badge" :class="statusBadge(order.status)">{{ statusLabel(order.status) }}</span></td>
            <td class="cell-time">{{ formatTime(order.createTime) }}</td>
            <td><router-link :to="'/admin/orders'" class="table-action">详情</router-link></td>
          </tr>
          <tr v-if="!recentOrders.length">
            <td colspan="6" style="text-align:center;color:var(--color-text-light);padding:40px;">暂无订单</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Quick Actions / Pending -->
    <div class="quick-row">
      <div class="quick-card">
        <h3>快捷操作</h3>
        <div class="quick-items">
          <router-link to="/admin/books" class="quick-item">
            <div class="qi-icon" style="background:var(--color-accent-glow);color:var(--color-accent);">📖</div>
            <div><div class="qi-text">添加新书</div><div class="qi-desc">录入新上架书籍信息</div></div>
            <span class="qi-arrow">→</span>
          </router-link>
          <router-link to="/admin/orders" class="quick-item">
            <div class="qi-icon" style="background:var(--color-green-bg);color:var(--color-green);">🧾</div>
            <div><div class="qi-text">处理订单</div><div class="qi-desc">查看待发货、退款申请</div></div>
            <span class="qi-arrow">→</span>
          </router-link>
          <router-link to="/admin/reviews" class="quick-item">
            <div class="qi-icon" style="background:var(--color-blue-bg);color:var(--color-blue);">✍️</div>
            <div><div class="qi-text">评价审核</div><div class="qi-desc">管理用户评价内容</div></div>
            <span class="qi-arrow">→</span>
          </router-link>
        </div>
      </div>
      <div class="quick-card">
        <h3>待处理事项</h3>
        <div class="quick-items">
          <router-link to="/admin/orders" class="quick-item">
            <div class="qi-icon" style="background:var(--color-accent-glow);color:var(--color-accent);">🧾</div>
            <div><div class="qi-text">{{ pendingShip }} 笔待发货订单</div><div class="qi-desc">需要尽快安排发货</div></div>
            <span class="qi-arrow">→</span>
          </router-link>
          <router-link to="/admin/orders" class="quick-item">
            <div class="qi-icon" style="background:var(--color-red-bg);color:var(--color-red);">↩️</div>
            <div><div class="qi-text">{{ pendingRefund }} 笔退款/售后申请</div><div class="qi-desc">需要在 24 小时内处理</div></div>
            <span class="qi-arrow">→</span>
          </router-link>
          <router-link to="/admin/reviews" class="quick-item">
            <div class="qi-icon" style="background:var(--color-primary-mid);color:var(--color-bg-warm);">✍️</div>
            <div><div class="qi-text">{{ pendingReview }} 条隐藏评价</div><div class="qi-desc">管理端可查看及恢复</div></div>
            <span class="qi-arrow">→</span>
          </router-link>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import { categoryApi } from '@/api/category'

const stats = ref([
  { icon: '💰', color: 'orange', label: '本月销售额', value: '—' },
  { icon: '📚', color: 'orange', label: '在售图书', value: '—' },
  { icon: '🧾', color: 'green', label: '总订单数', value: '—' },
  { icon: '👥', color: 'blue', label: '注册用户', value: '—' }
])

const revenueChange = ref(null) // { dir: 'up'|'down', pct: '12.5' }

const salesData = ref({ monthlyRevenue: 0, dailyRevenue: [], monthlyRevenueList: [] })
const salesRange = ref('month')
const recentOrders = ref([])
const categoryStats = ref([])
const catColors = ['#C09A4B', '#7A6252', '#5C8856', '#4A6E8A', '#A04040', '#8B6F4E', '#3D5A5A', '#6B4E71']

const categoryCount = computed(() => categoryStats.value.length)
const donutBg = computed(() => {
  if (!categoryStats.value.length) return 'none'
  const total = categoryStats.value.reduce((s, c) => s + c.count, 0) || 1
  let acc = 0
  const parts = categoryStats.value.map((c, i) => {
    const deg = (c.count / total) * 360
    const start = acc
    acc += deg
    return `${catColors[i % catColors.length]} ${start}deg ${acc}deg`
  })
  return `conic-gradient(${parts.join(',')})`
})

const pendingShip = ref(0)
const pendingRefund = ref(0)
const pendingReview = ref(0)

function barPct(val, max) {
  if (max <= 0) return 5
  return 5 + (val / max) * 85
}

const salesBars = computed(() => {
  const daily = salesData.value.dailyRevenue || []
  const maxVal = Math.max(...daily.map(d => Number(d.revenue)), 0)
  function buildDay(offset) {
    const d = new Date()
    d.setDate(d.getDate() - offset)
    const yyyy = d.getFullYear()
    const mm = String(d.getMonth() + 1).padStart(2, '0')
    const dd = String(d.getDate()).padStart(2, '0')
    const dayStr = `${yyyy}-${mm}-${dd}`
    const found = daily.find(x => x.day === dayStr)
    const val = found ? Number(found.revenue) : 0
    return {
      label: mm + '/' + dd,
      val: val.toLocaleString(),
      pct: barPct(val, maxVal)
    }
  }
  if (salesRange.value === 'week') {
    return [6,5,4,3,2,1,0].map(buildDay)
  }
  const monthly = salesData.value.monthlyRevenueList || []
  const monthlyMax = Math.max(...monthly.map(d => Number(d.revenue)), 0)
  const months = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setMonth(d.getMonth() - i)
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const key = y + '-' + m
    const found = monthly.find(x => x.month === key)
    const val = found ? Number(found.revenue) : 0
    months.push({
      label: m + '月',
      val: val.toLocaleString(),
      pct: barPct(val, monthlyMax)
    })
  }
  return months
})

function statusBadge(s) {
  const m = { created: 'badge-gray', paying: 'badge-orange', paid: 'badge-blue', shipped: 'badge-orange', delivered: 'badge-blue', completed: 'badge-green', cancelled: 'badge-gray', expired: 'badge-gray', refunding: 'badge-red', refunded: 'badge-red', after_sale: 'badge-red' }
  return m[s] || 'badge-gray'
}

function statusLabel(s) {
  const m = { created: '待付款', paying: '支付中', paid: '已付款', shipped: '已发货', delivered: '已收货', completed: '已完成', cancelled: '已取消', expired: '已过期', refunding: '退款中', refunded: '已退款', after_sale: '售后中' }
  return m[s] || s
}

function formatTime(t) {
  if (!t) return '—'
  return t.slice(0, 16).replace('T', ' ')
}

onMounted(async () => {
  try {
    const [bookRes, orderRes, userRes, reviewRes, catRes, revenueRes] = await Promise.all([
      adminApi.getBookList({ pageNum: 1, pageSize: 1 }),
      adminApi.getOrderList({ pageNum: 1, pageSize: 5 }),
      adminApi.getUserList({ pageNum: 1, pageSize: 1 }),
      adminApi.getReviewList({ pageNum: 1, pageSize: 100 }),
      categoryApi.getList(),
      adminApi.getRevenueStats()
    ])
    stats.value[0].value = '¥' + (Number(revenueRes.data?.monthlyRevenue || 0)).toLocaleString()
    stats.value[1].value = (bookRes.data?.total || 0).toLocaleString()
    stats.value[2].value = (orderRes.data?.total || 0).toLocaleString()
    stats.value[3].value = (userRes.data?.total || 0).toLocaleString()

    const revenueData = revenueRes.data || { monthlyRevenue: 0, dailyRevenue: [], lastMonthRevenue: 0 }
    salesData.value = revenueData

    // Revenue change
    const curr = Number(revenueData.monthlyRevenue) || 0
    const prev = Number(revenueData.lastMonthRevenue) || 0
    if (prev > 0) {
      const diff = ((curr - prev) / prev) * 100
      revenueChange.value = { dir: diff >= 0 ? 'up' : 'down', pct: Math.abs(diff).toFixed(1) + '%' }
    } else if (curr > 0) {
      revenueChange.value = { dir: 'up', pct: '100%' }
    }

    recentOrders.value = (orderRes.data?.records || []).slice(0, 5)

    if (catRes.data) {
      const cats = Array.isArray(catRes.data) ? catRes.data : []
      const top = cats.filter(c => c.bookCount > 0).sort((a, b) => b.bookCount - a.bookCount).slice(0, 7)
      const othersCount = cats.filter(c => c.bookCount > 0).sort((a, b) => b.bookCount - a.bookCount).slice(7).reduce((s, c) => s + (c.bookCount || 0), 0)
      const total = cats.reduce((s, c) => s + (c.bookCount || 0), 0) || 1
      const items = top.map(c => ({ name: c.name, count: c.bookCount || 0, pct: Math.round(((c.bookCount || 0) / total) * 100) }))
      if (othersCount > 0) items.push({ name: '其他', count: othersCount, pct: Math.round((othersCount / total) * 100) })
      categoryStats.value = items
    }
  } catch { /* ignore */ }

  // Fetch pending counts
  try {
    const [shipRes, refundRes, reviewRes] = await Promise.all([
      adminApi.getOrderList({ pageNum: 1, pageSize: 1, status: 'paid' }),
      adminApi.getOrderList({ pageNum: 1, pageSize: 1, status: 'refunding' }),
      adminApi.getReviewList({ pageNum: 1, pageSize: 100 })
    ])
    pendingShip.value = (shipRes.data?.total || 0)
    pendingRefund.value = (refundRes.data?.total || 0)
    // Count hidden reviews client-side
    const allReviews = (reviewRes.data?.records || [])
    pendingReview.value = allReviews.filter(r => r.status === 0).length
  } catch { /* ignore */ }
})
</script>

<style scoped>
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 28px; }
.stat-card { background: var(--color-bg-card); border: 1px solid var(--color-divider); border-radius: 12px; padding: 22px 24px; transition: all .3s; }
.stat-card:hover { border-color: rgba(192,154,75,0.15); box-shadow: 0 8px 24px var(--color-shadow); }
.stat-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.stat-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.15rem; }
.stat-icon.orange { background: var(--color-accent-glow); color: var(--color-accent); }
.stat-icon.green { background: var(--color-green-bg); color: var(--color-green); }
.stat-icon.blue { background: var(--color-blue-bg); color: var(--color-blue); }
.stat-icon.brown { background: rgba(74,53,38,0.06); color: var(--color-primary-mid); }
.stat-icon.gold { background: rgba(192,154,75,0.12); color: var(--color-accent); }
.stat-change { font-size: .72rem; font-weight: 500; padding: 2px 8px; border-radius: 10px; }
.stat-change.up { background: var(--color-green-bg); color: var(--color-green); }
.stat-change.down { background: var(--color-red-bg); color: var(--color-red); }
.stat-value { font-family: var(--font-display); font-size: 1.8rem; font-weight: 900; color: var(--color-text); line-height: 1; margin-bottom: 4px; }
.stat-label { font-size: .8rem; color: var(--color-text-light); }

.charts-row { display: grid; grid-template-columns: 1.6fr 1fr; gap: 20px; margin-bottom: 28px; }
.chart-card { background: var(--color-bg-card); border: 1px solid var(--color-divider); border-radius: 12px; padding: 24px; }
.chart-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.chart-header h3 { font-size: 1rem; font-weight: 700; }
.chart-sub { font-size: .75rem; color: var(--color-text-light); margin-top: 2px; }
.chart-tabs { display: flex; gap: 4px; }
.chart-tab { padding: 5px 14px; border-radius: 6px; font-size: .78rem; cursor: pointer; transition: all .2s; color: var(--color-text-light); border: 1px solid transparent; }
.chart-tab:hover { color: var(--color-text-secondary); }
.chart-tab.active { background: var(--color-primary); color: var(--color-bg-warm); }

.bar-chart { display: flex; align-items: flex-end; gap: 12px; height: 180px; padding-top: 10px; }
.bar-group { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4px; height: 100%; justify-content: flex-end; padding-bottom: 0; }
.bar { width: 60%; max-width: 48px; border-radius: 4px 4px 0 0; transition: all .5s cubic-bezier(.25,.46,.45,.94); position: relative; min-height: 4px; }
.bar:hover { opacity: .85; }
.bar-label { font-size: .68rem; color: var(--color-text-light); }
.bar .bar-tooltip { position: absolute; top: -28px; left: 50%; transform: translateX(-50%); background: var(--color-primary-dark); color: var(--color-bg-warm); font-size: .68rem; padding: 3px 8px; border-radius: 4px; white-space: nowrap; opacity: 0; pointer-events: none; transition: opacity .2s; }
.bar:hover .bar-tooltip { opacity: 1; }

.donut-wrap { display: flex; align-items: center; gap: 28px; justify-content: center; }
.donut { width: 150px; height: 150px; border-radius: 50%; position: relative; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.donut-center { position: absolute; width: 90px; height: 90px; border-radius: 50%; background: var(--color-bg-card); display: flex; flex-direction: column; align-items: center; justify-content: center; }
.donut-center .donut-val { font-family: var(--font-display); font-size: 1.2rem; font-weight: 900; }
.donut-center .donut-lbl { font-size: .68rem; color: var(--color-text-light); }
.donut-legend { display: flex; flex-direction: column; gap: 10px; }
.legend-item { display: flex; align-items: center; gap: 8px; font-size: .8rem; color: var(--color-text-secondary); }
.legend-dot { width: 10px; height: 10px; border-radius: 3px; flex-shrink: 0; }

.table-card { background: var(--color-bg-card); border: 1px solid var(--color-divider); border-radius: 12px; overflow: hidden; margin-bottom: 28px; }
.table-header { display: flex; align-items: center; justify-content: space-between; padding: 18px 24px; border-bottom: 1px solid var(--color-divider); }
.table-header h3 { font-size: 1rem; font-weight: 700; }
.table-header a { font-size: .82rem; color: var(--color-text-light); transition: color .2s; text-decoration: none; }
.table-header a:hover { color: var(--color-accent); }
table { width: 100%; border-collapse: collapse; }
thead th { text-align: left; padding: 12px 24px; font-size: .75rem; font-weight: 500; color: var(--color-text-light); letter-spacing: .06em; border-bottom: 1px solid var(--color-divider); background: var(--color-bg-cream); }
tbody td { padding: 14px 24px; font-size: .86rem; border-bottom: 1px solid var(--color-divider); vertical-align: middle; }
tbody tr { transition: background .2s; }
tbody tr:hover { background: rgba(192,154,75,0.03); }
tbody tr:last-child td { border-bottom: none; }
.cell-mono { font-family: 'DM Mono', monospace; font-size: .82rem; color: var(--color-text-light); }
.cell-price { font-weight: 600; }
.cell-time { font-size: .82rem; color: var(--color-text-light); }

.table-badge { display: inline-block; padding: 3px 10px; border-radius: 10px; font-size: .72rem; font-weight: 500; }
.badge-green { background: var(--color-green-bg); color: var(--color-green); }
.badge-orange { background: var(--color-accent-glow); color: var(--color-accent-muted); }
.badge-blue { background: var(--color-blue-bg); color: var(--color-blue); }
.badge-red { background: var(--color-red-bg); color: var(--color-red); }
.badge-gray { background: rgba(74,53,38,0.05); color: var(--color-text-light); }

.table-action { font-size: .78rem; color: var(--color-primary-light); cursor: pointer; padding: 4px 8px; border-radius: 4px; transition: all .2s; text-decoration: none; }
.table-action:hover { background: var(--color-bg-warm); color: var(--color-accent); }

.quick-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 28px; }
.quick-card { background: var(--color-bg-card); border: 1px solid var(--color-divider); border-radius: 12px; padding: 24px; }
.quick-card h3 { font-size: 1rem; font-weight: 700; margin-bottom: 16px; }
.quick-items { display: flex; flex-direction: column; gap: 8px; }
.quick-item { display: flex; align-items: center; gap: 12px; padding: 10px 14px; border-radius: 8px; cursor: pointer; transition: all .2s; border: 1px solid var(--color-divider); text-decoration: none; color: inherit; }
.quick-item:hover { background: var(--color-bg-cream); border-color: var(--color-divider-strong); }
.qi-icon { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: .9rem; flex-shrink: 0; }
.qi-text { font-size: .84rem; font-weight: 500; }
.qi-desc { font-size: .74rem; color: var(--color-text-light); }
.qi-arrow { margin-left: auto; color: var(--color-text-light); font-size: .75rem; }

@media(max-width:1200px) {
  .stats-row { grid-template-columns: repeat(2,1fr); }
  .charts-row { grid-template-columns: 1fr; }
  .quick-row { grid-template-columns: 1fr; }
}
</style>
