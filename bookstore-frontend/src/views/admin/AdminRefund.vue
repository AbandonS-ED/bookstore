<template>
  <div class="admin-refund">
    <!-- Stats -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-top">
          <div class="stat-icon warn">↩️</div>
        </div>
        <div class="stat-value">{{ stats.refunding }}</div>
        <div class="stat-label">退款审核中</div>
      </div>
      <div class="stat-card">
        <div class="stat-top">
          <div class="stat-icon warn">📦</div>
        </div>
        <div class="stat-value">{{ stats.afterSale }}</div>
        <div class="stat-label">售后审核中</div>
      </div>
      <div class="stat-card">
        <div class="stat-top">
          <div class="stat-icon danger">↩️</div>
        </div>
        <div class="stat-value">{{ stats.refunded }}</div>
        <div class="stat-label">已退款</div>
      </div>
      <div class="stat-card">
        <div class="stat-top">
          <div class="stat-icon muted">🧾</div>
        </div>
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">全部申请</div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="status-tabs">
      <el-radio-group v-model="statusFilter" @change="loadList">
        <el-radio-button label="all">全部申请</el-radio-button>
        <el-radio-button label="refunding">退款中</el-radio-button>
        <el-radio-button label="after_sale">售后中</el-radio-button>
        <el-radio-button label="refunded">已退款</el-radio-button>
      </el-radio-group>
    </div>

    <!-- Table -->
    <div class="refund-table">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="订单号" min-width="180">
          <template #default="{ row }">
            <span class="mono">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="客户" width="120" />
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ Number(row.totalAmount).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'refunding' ? 'warning' : row.status === 'after_sale' ? 'warning' : 'danger'" size="small">
              {{ row.status === 'refunding' ? '退款' : row.status === 'after_sale' ? '售后' : '已退款' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
              {{ statusMap[row.status]?.text || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="170">
          <template #default="{ row }">
            <span class="time">{{ row.updateTime?.slice(0, 16).replace('T', ' ') || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'refunding'">
              <el-button type="success" size="small" @click="handleApproveRefund(row)">同意退款</el-button>
              <el-button type="danger" size="small" @click="handleRejectRefund(row)">拒绝</el-button>
            </template>
            <template v-else-if="row.status === 'after_sale'">
              <el-button type="success" size="small" @click="handleApproveAfterSale(row)">通过售后</el-button>
              <el-button type="danger" size="small" @click="handleRejectAfterSale(row)">拒绝</el-button>
            </template>
            <template v-else>
              <span class="done-label">已处理</span>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Pagination -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadList"
        @current-change="loadList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const statusFilter = ref('refunding')
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const stats = reactive({ refunding: 0, afterSale: 0, refunded: 0, total: 0 })

const statusMap = {
  refunding:  { text: '退款审核', type: 'warning' },
  after_sale: { text: '售后审核', type: 'warning' },
  refunded:   { text: '已退款',   type: 'danger' }
}

async function loadStats() {
  try {
    const [refundingRes, afterSaleRes, refundedRes] = await Promise.all([
      adminApi.getOrderList({ pageNum: 1, pageSize: 1, status: 'refunding' }),
      adminApi.getOrderList({ pageNum: 1, pageSize: 1, status: 'after_sale' }),
      adminApi.getOrderList({ pageNum: 1, pageSize: 1, status: 'refunded' })
    ])
    stats.refunding = refundingRes.data?.total || 0
    stats.afterSale = afterSaleRes.data?.total || 0
    stats.refunded = refundedRes.data?.total || 0
    stats.total = stats.refunding + stats.afterSale + stats.refunded
  } catch { /* ignore */ }
}

async function loadList() {
  loading.value = true
  try {
    if (statusFilter.value === 'all') {
      const [r1, r2, r3] = await Promise.all([
        adminApi.getOrderList({ pageNum: 1, pageSize: 9999, status: 'refunding' }),
        adminApi.getOrderList({ pageNum: 1, pageSize: 9999, status: 'after_sale' }),
        adminApi.getOrderList({ pageNum: 1, pageSize: 9999, status: 'refunded' })
      ])
      const merged = [
        ...(r1.data?.records || []),
        ...(r2.data?.records || []),
        ...(r3.data?.records || [])
      ]
      merged.sort((a, b) => (b.updateTime || '').localeCompare(a.updateTime || ''))
      total.value = merged.length
      const start = (pageNum.value - 1) * pageSize.value
      list.value = merged.slice(start, start + pageSize.value)
    } else {
      const res = await adminApi.getOrderList({
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        status: statusFilter.value
      })
      list.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function handleApproveRefund(row) {
  try {
    await ElMessageBox.confirm(`确认同意订单 ${row.orderNo} 的退款申请？`, '确认操作', {
      confirmButtonText: '同意退款',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.approveRefund(row.id)
    ElMessage.success('退款已批准')
    loadList()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

async function handleRejectRefund(row) {
  try {
    await ElMessageBox.confirm(`确认拒绝订单 ${row.orderNo} 的退款申请？`, '确认操作', {
      confirmButtonText: '拒绝退款',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.rejectRefund(row.id)
    ElMessage.success('退款已拒绝')
    loadList()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

async function handleApproveAfterSale(row) {
  try {
    await ElMessageBox.confirm(`确认通过订单 ${row.orderNo} 的售后申请？`, '确认操作', {
      confirmButtonText: '通过售后',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.approveAfterSale(row.id)
    ElMessage.success('售后已通过')
    loadList()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

async function handleRejectAfterSale(row) {
  try {
    await ElMessageBox.confirm(`确认拒绝订单 ${row.orderNo} 的售后申请？`, '确认操作', {
      confirmButtonText: '拒绝售后',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.rejectAfterSale(row.id)
    ElMessage.success('售后已拒绝')
    loadList()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadStats()
  loadList()
})
</script>

<style scoped>
.admin-refund {
  max-width: 1400px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.stat-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider);
  border-radius: 12px;
  padding: 22px 24px;
  transition: all .3s;
  cursor: default;
}

.stat-card:hover {
  border-color: rgba(192,154,75,0.15);
  box-shadow: 0 8px 24px var(--color-shadow);
}

.stat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.15rem;
}

.stat-icon.warn {
  background: var(--color-accent-glow);
  color: var(--color-accent);
}

.stat-icon.danger {
  background: var(--color-red-bg);
  color: var(--color-red);
}

.stat-icon.muted {
  background: rgba(74,53,38,0.06);
  color: var(--color-primary-mid);
}

.stat-value {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.8rem;
  font-weight: 900;
  color: var(--color-text);
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: .8rem;
  color: var(--color-text-light);
}

.status-tabs {
  margin-bottom: var(--space-6);
}

.refund-table {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
}

.mono {
  font-family: 'DM Mono', monospace;
  font-size: .82rem;
  color: var(--color-text-light);
}

.amount {
  font-family: var(--font-display);
  font-weight: 600;
  color: var(--color-primary);
}

.time {
  font-size: .82rem;
  color: var(--color-text-light);
}

.done-label {
  font-size: .78rem;
  color: var(--color-text-light);
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-6);
}
</style>
