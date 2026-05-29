<template>
  <div class="admin-community">
    <div class="toolbar">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名、内容..."
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <span>🔍</span>
          </template>
        </el-input>
      </div>
      <span class="toolbar-title">共 {{ total }} 条帖子</span>
    </div>

    <el-table :data="posts" v-loading="loading" stripe class="posts-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <img v-if="row.imageUrl" :src="row.imageUrl" class="post-thumb" @error="handleImgError" />
          <span v-else class="no-img">无图</span>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="likes" label="点赞" width="80" align="center" />
      <el-table-column prop="bookTitle" label="关联书籍" min-width="140" show-overflow-tooltip />
      <el-table-column prop="createTime" label="发布时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadPosts"
        @current-change="loadPosts"
      />
    </div>

    <el-dialog v-model="editDialogOpen" title="编辑帖子" width="560px" :close-on-click-modal="false">
      <div class="edit-form" v-if="editingPost">
        <div class="edit-field">
          <div class="ef-label">帖子内容</div>
          <el-input
            v-model="editingPost.content"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="请输入帖子内容"
          />
        </div>
        <div class="edit-field">
          <div class="ef-label">更换图片</div>
          <div class="ef-img-picker" @click="triggerImgUpload">
            <img v-if="editingPost.imagePreview" :src="editingPost.imagePreview" class="ef-img-preview" />
            <div v-else-if="editingPost.imageUrl" class="ef-img-current">
              <img :src="editingPost.imageUrl" class="ef-img-current-src" />
              <span class="ef-img-overlay">点击更换</span>
            </div>
            <div v-else class="ef-img-placeholder">
              <span>点击上传图片</span>
            </div>
            <input ref="editImgRef" type="file" accept="image/*" style="display:none" @change="handleEditImgUpload" />
          </div>
        </div>
        <div class="edit-field">
          <div class="ef-label">关联书籍（可选）</div>
          <div class="ef-book-search">
            <el-input v-model="editBookQuery" placeholder="搜索书名..." clearable @input="filterEditBooks">
              <template #prefix><span>🔍</span></template>
            </el-input>
          </div>
          <div v-if="editSelectedBook" class="ef-book-selected">
            <div class="efbs-info">
              <span class="efbs-title">{{ editSelectedBook.title }}</span>
              <span class="efbs-author">{{ editSelectedBook.author }}</span>
            </div>
            <el-button size="small" @click="editSelectedBook = null">清除</el-button>
          </div>
          <div v-else-if="editBookResults.length" class="ef-book-results">
            <div
              v-for="b in editBookResults"
              :key="b.id"
              class="ef-book-item"
              @click="editSelectedBook = b"
            >
              <div class="efbi-cover" :style="getCoverStyle(b.id)">{{ b.title.substring(0, 2) }}</div>
              <div>
                <div class="efbi-title">{{ b.title }}</div>
                <div class="efbi-author">{{ b.author }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="editDialogOpen = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCoverStyle } from '@/utils/cover'
import { adminApi } from '@/api/admin'

const posts = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

const editDialogOpen = ref(false)
const editingPost = ref(null)
const editImgRef = ref(null)
const editBookQuery = ref('')
const editSelectedBook = ref(null)
const editBookResults = ref([])
const allBooks = ref([])

function handleSearch() {
  pageNum.value = 1
  loadPosts()
}

async function loadPosts() {
  loading.value = true
  try {
    const res = await adminApi.getCommunityList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined
    })
    posts.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}

function handleImgError(e) {
  e.target.style.display = 'none'
}

function formatTime(time) {
  if (!time) return '-'
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function openEdit(post) {
  if (!allBooks.value.length) {
    try {
      const res = await adminApi.getBookList({ pageNum: 1, pageSize: 100 })
      allBooks.value = res.data?.records || []
    } catch {}
  }
  editingPost.value = { ...post, imagePreview: post.imageUrl }
  editBookQuery.value = ''
  editSelectedBook.value = null
  editBookResults.value = []
  editDialogOpen.value = true
}

function triggerImgUpload() {
  editImgRef.value?.click()
}

function handleEditImgUpload(e) {
  const file = e.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => {
    editingPost.value.imagePreview = ev.target.result
  }
  reader.readAsDataURL(file)
}

function filterEditBooks() {
  const q = editBookQuery.value.trim().toLowerCase()
  if (!q) {
    editBookResults.value = []
    return
  }
  editBookResults.value = allBooks.value.filter(b =>
    b.title.toLowerCase().includes(q) || b.author.toLowerCase().includes(q)
  ).slice(0, 6)
}

async function saveEdit() {
  if (!editingPost.value) return
  try {
    await adminApi.updateCommunity({
      id: editingPost.value.id,
      content: editingPost.value.content,
      imageUrl: editingPost.value.imagePreview || editingPost.value.imageUrl,
      bookId: editSelectedBook.value?.id || null,
    })
    editDialogOpen.value = false
    ElMessage.success('帖子更新成功')
    await loadPosts()
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该帖子吗？删除后无法恢复。', '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminApi.deleteCommunity(id)
    ElMessage.success('删除成功')
    await loadPosts()
  } catch {
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.admin-community { }
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 16px;
}
.search-box { display: flex; gap: 10px; }
.toolbar-title { font-size: 0.82rem; color: var(--color-text-light); white-space: nowrap; }
.posts-table { border-radius: 8px; overflow: hidden; }
.post-thumb {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
}
.no-img { font-size: 0.7rem; color: var(--color-text-light); }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
.edit-form { display: flex; flex-direction: column; gap: 16px; }
.edit-field { display: flex; flex-direction: column; gap: 6px; }
.ef-label { font-size: 0.82rem; font-weight: 600; color: var(--color-text-secondary); }
.ef-img-picker {
  border: 2px dashed var(--color-divider-strong);
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.25s;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.ef-img-picker:hover { border-color: var(--color-accent); }
.ef-img-preview, .ef-img-current-src {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
}
.ef-img-current { position: relative; width: 100%; }
.ef-img-current-src { opacity: 0.8; }
.ef-img-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0,0,0,0.4);
  color: #fff;
  font-size: 0.8rem;
  opacity: 0;
  transition: opacity 0.2s;
}
.ef-img-current:hover .ef-img-overlay { opacity: 1; }
.ef-img-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 24px;
  color: var(--color-text-light);
  font-size: 0.8rem;
}
.ef-book-search .el-input { width: 100%; }
.ef-book-selected {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: var(--color-accent-glow);
  border: 1.5px solid var(--color-accent);
  border-radius: 8px;
  margin-top: 4px;
}
.efbs-info { flex: 1; }
.efbs-title { font-size: 0.84rem; font-weight: 600; color: var(--color-accent-muted); display: block; }
.efbs-author { font-size: 0.7rem; color: var(--color-text-light); }
.ef-book-results {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 180px;
  overflow-y: auto;
  margin-top: 4px;
  border: 1px solid var(--color-divider);
  border-radius: 8px;
  padding: 6px;
}
.ef-book-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}
.ef-book-item:hover { background: var(--color-bg-warm); }
.efbi-cover {
  width: 28px;
  height: 36px;
  border-radius: 3px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.24rem;
  color: rgba(237,230,214,0.7);
}
.efbi-title { font-size: 0.8rem; font-weight: 600; color: var(--color-text); }
.efbi-author { font-size: 0.68rem; color: var(--color-text-light); }
</style>
