<template>
  <div class="admin-orders">
    <!-- 状态筛选标签 -->
    <div class="status-tabs">
      <el-radio-group v-model="statusFilter" @change="loadOrders">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="pending">待付款</el-radio-button>
        <el-radio-button label="paid">待发货</el-radio-button>
        <el-radio-button label="shipped">已发货</el-radio-button>
        <el-radio-button label="delivered">已完成</el-radio-button>
        <el-radio-button label="cancelled">已取消</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 订单表格 -->
    <el-table :data="orders" v-loading="loading" stripe class="orders-table">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column prop="totalAmount" label="金额" width="120">
        <template #default="{ row }">
          <span class="amount">¥{{ row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="address" label="收货地址" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="viewDetail(row)">
            详情
          </el-button>
          <el-button
            v-if="row.status === 'paid'"
            size="small"
            type="success"
            link
            @click="handleShip(row)"
          >
            发货
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadOrders"
        @current-change="loadOrders"
      />
    </div>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <div v-if="currentOrder" class="order-detail">
        <div class="detail-section">
          <h4>订单信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(currentOrder.status)" size="small">
                {{ getStatusText(currentOrder.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ currentOrder.createTime }}</el-descriptions-item>
            <el-descriptions-item label="总金额">
              <span class="amount">¥{{ currentOrder.totalAmount }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h4>收货信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="收货人">{{ currentOrder.consignee }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ currentOrder.phone }}</el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.address }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h4>商品清单</h4>
          <el-table :data="currentOrder.items" size="small">
            <el-table-column prop="bookTitle" label="商品" />
            <el-table-column prop="price" label="单价" width="100">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="subtotal" label="小计" width="100">
              <template #default="{ row }">¥{{ row.subtotal }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="currentOrder?.status === 'paid'"
          type="primary"
          @click="handleShip(currentOrder)"
        >
          确认发货
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const orders = ref([])
const statusFilter = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detailVisible = ref(false)
const currentOrder = ref(null)

const statusMap = {
  pending: { text: '待付款', type: 'warning' },
  paid: { text: '待发货', type: 'primary' },
  shipped: { text: '已发货', type: 'info' },
  delivered: { text: '已完成', type: 'success' },
  cancelled: { text: '已取消', type: 'danger' }
}

const getStatusText = (status) => statusMap[status]?.text || status
const getStatusType = (status) => statusMap[status]?.type || 'info'

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: statusFilter.value
    }
    const res = await adminApi.getOrderList(params)
    orders.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to load orders:', error)
    // Mock data
    orders.value = [
      {
        id: 1,
        orderNo: 'ORD202401010001',
        username: '张三',
        totalAmount: 128.50,
        status: 'paid',
        createTime: '2024-01-15 10:30:00',
        consignee: '张三',
        phone: '13800138000',
        address: '北京市朝阳区建国路88号',
        items: [
          { bookTitle: '活着', price: 49.00, quantity: 2, subtotal: 98.00 },
          { bookTitle: '围城', price: 30.50, quantity: 1, subtotal: 30.50 }
        ]
      }
    ]
    total.value = 1
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => {
  currentOrder.value = row
  detailVisible.value = true
}

const handleShip = async (row) => {
  try {
    await adminApi.shipOrder(row.id)
    ElMessage.success('发货成功')
    detailVisible.value = false
    loadOrders()
  } catch (error) {
    ElMessage.error(error.message || '发货失败')
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.admin-orders {
  max-width: 1400px;
}

.status-tabs {
  margin-bottom: var(--space-6);
}

.orders-table {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
}

.amount {
  font-family: var(--font-display);
  font-weight: 600;
  color: var(--color-primary);
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-6);
}

.order-detail {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: var(--space-6);
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section h4 {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-2);
  border-bottom: 1px solid var(--color-divider);
}
</style>