<template>
  <div class="admin-orders">
    <!-- 状态筛选标签 -->
    <div class="status-tabs">
      <el-radio-group v-model="statusFilter" @change="loadOrders">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="created">待付款</el-radio-button>
        <el-radio-button label="paid">待发货</el-radio-button>
        <el-radio-button label="shipped">已发货</el-radio-button>
        <el-radio-button label="delivered">已完成</el-radio-button>
        <el-radio-button label="refunding">退款审核</el-radio-button>
        <el-radio-button label="after_sale">售后审核</el-radio-button>
        <el-radio-button label="cancelled">已取消</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 订单表格 -->
    <el-table :data="orders" v-loading="loading" stripe class="orders-table">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column prop="totalAmount" label="金额" width="120">
        <template #default="{ row }">
          <span class="amount">¥{{ Number(row.totalAmount).toFixed(2) }}</span>
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
      <el-table-column prop="receiverAddress" label="收货地址" min-width="200" show-overflow-tooltip />
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
            @click="openShipDialog(row)"
          >
            发货
          </el-button>
          <el-button
            v-if="row.status === 'shipped'"
            size="small"
            type="success"
            link
            @click="handleDeliver(row)"
          >
            确认送达
          </el-button>
          <el-button
            v-if="row.status === 'paid' || row.status === 'shipped'"
            size="small"
            type="danger"
            link
            @click="handleRefund(row)"
          >
            退款
          </el-button>
          <el-button
            v-if="row.status === 'refunding'"
            size="small"
            type="success"
            link
            @click="handleApproveRefund(row)"
          >
            同意退款
          </el-button>
          <el-button
            v-if="row.status === 'refunding'"
            size="small"
            type="danger"
            link
            @click="handleRejectRefund(row)"
          >
            拒绝退款
          </el-button>
          <el-button
            v-if="row.status === 'after_sale'"
            size="small"
            type="success"
            link
            @click="handleApproveAfterSale(row)"
          >
            通过售后
          </el-button>
          <el-button
            v-if="row.status === 'after_sale'"
            size="small"
            type="danger"
            link
            @click="handleRejectAfterSale(row)"
          >
            拒绝售后
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

    <!-- 发货对话框 -->
    <el-dialog v-model="shipDialogVisible" title="确认发货" width="400px">
      <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules">
        <el-form-item label="快递单号" prop="expressNo">
          <el-input v-model="shipForm.expressNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShip" :loading="shipLoading">确认发货</el-button>
      </template>
    </el-dialog>

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
            <el-descriptions-item label="收货人">{{ currentOrder.receiverName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ currentOrder.receiverPhone }}</el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress }}</el-descriptions-item>
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
          @click="openShipDialog(currentOrder)"
        >
          确认发货
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const orders = ref([])
const statusFilter = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const shipDialogVisible = ref(false)
const shipLoading = ref(false)
const shipFormRef = ref(null)
const shipTargetOrder = ref(null)
const shipForm = reactive({
  expressNo: ''
})
const shipRules = {
  expressNo: [{ required: true, message: '请输入快递单号', trigger: 'blur' }]
}

const detailVisible = ref(false)
const currentOrder = ref(null)

const statusMap = {
  created: { text: '待付款', type: 'warning' },
  pending: { text: '待付款', type: 'warning' },
  paying: { text: '支付中', type: 'warning' },
  paid: { text: '待发货', type: 'primary' },
  shipped: { text: '已发货', type: 'info' },
  delivered: { text: '已收货', type: 'success' },
  completed: { text: '已完成', type: 'success' },
  cancelled: { text: '已取消', type: 'danger' },
  expired: { text: '已过期', type: 'info' },
  refunding: { text: '退款审核', type: 'warning' },
  after_sale: { text: '售后审核', type: 'warning' },
  refunded: { text: '已退款', type: 'danger' }
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
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => {
  currentOrder.value = row
  detailVisible.value = true
}

const openShipDialog = (row) => {
  shipTargetOrder.value = row
  shipForm.expressNo = ''
  shipDialogVisible.value = true
}

const confirmShip = async () => {
  if (!shipFormRef.value) return
  await shipFormRef.value.validate(async (valid) => {
    if (!valid) return
    shipLoading.value = true
    try {
      await adminApi.shipOrder(shipTargetOrder.value.id, shipForm.expressNo)
      ElMessage.success('发货成功')
      shipDialogVisible.value = false
      detailVisible.value = false
      loadOrders()
    } catch (error) {
      ElMessage.error(error.message || '发货失败')
    } finally {
      shipLoading.value = false
    }
  })
}

const handleDeliver = async (row) => {
  try {
    await adminApi.deliverOrder(row.id)
    ElMessage.success('已确认送达')
    loadOrders()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleRefund = async (row) => {
  try {
    await ElMessageBox.confirm('确定要对该订单进行退款吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.refundOrder(row.id)
    ElMessage.success('退款成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '退款失败')
    }
  }
}

const handleApproveRefund = async (row) => {
  try {
    await ElMessageBox.confirm('同意退款后将恢复库存，确定操作？', '同意退款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.approveRefund(row.id)
    ElMessage.success('已同意退款')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleRejectRefund = async (row) => {
  try {
    await ElMessageBox.confirm('拒绝退款后订单将恢复为已支付状态，确定操作？', '拒绝退款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.rejectRefund(row.id)
    ElMessage.success('已拒绝退款')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleApproveAfterSale = async (row) => {
  try {
    await ElMessageBox.confirm('通过售后后将退款并恢复库存，确定操作？', '通过售后', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.approveAfterSale(row.id)
    ElMessage.success('已通过售后申请')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleRejectAfterSale = async (row) => {
  try {
    await ElMessageBox.confirm('拒绝售后后订单将恢复为已完成状态，确定操作？', '拒绝售后', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.rejectAfterSale(row.id)
    ElMessage.success('已拒绝售后申请')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
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