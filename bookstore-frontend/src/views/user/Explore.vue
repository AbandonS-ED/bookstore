<template>
  <div class="explore-page">
    <section class="page-header">
      <div class="ph-inner">
        <div class="ph-left">
          <h1>🔮 探索好书</h1>
          <p>翻开命运之书，在漂流瓶中遇见有缘之书</p>
        </div>
        <div class="ph-stats">
          <div class="phs">
            <div class="phs-num">{{ fortunePool.length }}</div>
            <div class="phs-label">命运卡牌</div>
          </div>
          <div class="phs">
            <div class="phs-num">{{ oceanBottles.length }}</div>
            <div class="phs-label">海中漂流瓶</div>
          </div>
        </div>
      </div>
    </section>

    <div class="tab-bar">
      <div :class="['tab-item', { active: activeTab === 'community' }]" @click="activeTab = 'community'">
        <span class="ti-icon">📖</span>
        <span>书斋社区</span>
      </div>
      <div :class="['tab-item', { active: activeTab === 'fortune' }]" @click="activeTab = 'fortune'">
        <span class="ti-icon">🔮</span>
        <span>命运之书</span>
      </div>
      <div :class="['tab-item', { active: activeTab === 'ocean' }]" @click="activeTab = 'ocean'">
        <span class="ti-icon">📜</span>
        <span>书笺传情</span>
      </div>
    </div>

    <!-- Community header with publish button -->
    <div v-show="activeTab === 'community'" class="community-header">
      <div class="ch-inner">
        <div class="ch-title">📖 书斋社区</div>
        <button class="ch-publish-btn" @click="openPublishModal">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 5v14M5 12h14"/>
          </svg>
          发布帖子
        </button>
      </div>
    </div>

    <div class="section-wrap">
      <!-- ═══ FORTUNE ═══ -->
      <div v-show="activeTab === 'fortune'" class="tab-panel">
        <div class="fortune-stage">
          <div class="fortune-particles">
            <div v-for="p in particles" :key="p.id" class="fp" :style="p.style"></div>
          </div>
          <div class="fortune-stars">
            <div v-for="s in stars" :key="s.id" class="fs" :style="s.style"></div>
          </div>
          <div class="card-stage">
            <div class="fortune-progress">
              <div class="fp-bar-wrap">
                <div class="fp-bar" :style="{ width: collectedPercent + '%' }"></div>
              </div>
              <div class="fp-text"><strong>{{ collected.size }}</strong> / {{ fortunePool.length }} 张</div>
            </div>
            <div class="cards-area">
              <div :class="['three-cards', { collapsed: hideOtherCards }]">
                <div
                  v-for="(f, idx) in threeF"
                  :key="f.id"
                  v-show="!hideOtherCards || flippedIdx === idx"
                  :class="['pick-card', { selectable: deckReady, enter: cardsEntering }]"
                  :style="[cardStyle(idx), { animationDelay: (idx * 0.15) + 's' }]"
                  @click="pickCard(idx)"
                >
                  <div class="pc-face pc-back">
                    <div class="pc-back-inner">
                      <div class="pc-cd tl"></div>
                      <div class="pc-cd tr"></div>
                      <div class="pc-cd bl"></div>
                      <div class="pc-cd br"></div>
                      <div class="pc-shimmer"></div>
                      <div class="pc-symbol">✦</div>
                      <div class="pc-title">命运之书</div>
                      <div class="pc-sub">选择这张牌</div>
                      <div class="pc-number">{{ idx + 1 }}</div>
                    </div>
                  </div>
                  <div class="pc-face pc-front">
                    <div class="pc-front-icon">{{ f.symbol }}</div>
                    <div class="pc-front-fortune">{{ f.fortune }}</div>
                    <div class="pc-front-tag">{{ f.tag }}</div>
                    <div class="pc-front-book" v-if="f.book">
                      <div class="pfb-title">{{ f.book.title }}</div>
                      <div class="pfb-author">{{ f.book.author }}</div>
                      <div class="pfb-price">¥{{ f.book.onDiscount ? f.book.discountPrice : f.book.price }} <span v-if="f.book.avgRating">★ {{ f.book.avgRating.toFixed(1) }}</span></div>
                    </div>
                  </div>
                </div>
              </div>
              <!-- Book Detail Panel with Excerpt -->
              <div :class="['book-detail-panel', { show: curFort }]">
                <div class="bdp-inner" v-if="curFort?.book">
                  <div class="bdp-top">
                    <h2 class="bdp-title">{{ curFort.book.title }}</h2>
                    <p class="bdp-author">{{ curFort.book.author }}</p>
                  </div>
                  <div class="bdp-body">
                    <p class="bdp-quote" v-if="curExcerpt">"{{ curExcerpt }}"</p>
                    <p class="bdp-quote" v-else>暂无精彩文段</p>
                  </div>
                  <div class="bdp-foot">
                    <span class="bdp-divider"></span>
                    <p class="bdp-fortune-label">命运之书说</p>
                    <p class="bdp-fortune-msg">{{ curFort.fortune }}</p>
                  </div>
                </div>
              </div>
            </div>
            <div :class="['card-hint', { active: hintActive }]">{{ hintText }}</div>
            <button v-if="!curFort" class="btn-draw secondary" @click="drawFortune">🔄 换一批</button>
            <div v-else :class="['card-actions', { show: !!curFort }]">
              <button class="ca-btn primary" @click="favFortune">♡ 收藏这本书</button>
              <button class="ca-btn accent" @click="cartFortune">🛒 加入购物车</button>
              <button class="ca-btn ghost" @click="drawFortune">🔮 再来一次</button>
            </div>
            <div class="fortune-collection">
              <div class="fc-title">我的命运卡牌收藏</div>
              <div class="fc-grid">
                <div
                  v-for="f in fortunePool"
                  :key="f.id"
                  :class="['fc-card', { collected: collected.has(f.id), 'just-got': justGotId === f.id }]"
                  @click="collected.has(f.id) && toast(f.tag)"
                >
                  <template v-if="collected.has(f.id)">
                    {{ f.symbol }}
                    <span class="fcc-num">{{ f.id }}</span>
                  </template>
                  <span v-else style="opacity:.3;font-size:.7rem">?</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ═══ INK LETTER ═══ -->
      <div v-show="activeTab === 'ocean'" class="tab-panel">
        <div class="ink-scene">
          <!-- 天空与远山 -->
          <div class="ink-sky"></div>
          <div class="ink-moon"></div>
          <div class="ink-moon-glow"></div>
          <div class="ink-mountains">
            <div class="ink-mt mt1"></div>
            <div class="ink-mt mt2"></div>
            <div class="ink-mt mt3"></div>
          </div>
          <div class="ink-fog fog1"></div>
          <div class="ink-fog fog2"></div>
          <!-- 湖面 -->
          <div class="ink-lake"></div>
          <div class="ink-ripples">
            <div class="ink-rip r1"></div>
            <div class="ink-rip r2"></div>
            <div class="ink-rip r3"></div>
          </div>
          <!-- 萤火虫 -->
          <div class="ink-fireflies">
            <div class="ink-ff ff1"></div>
            <div class="ink-ff ff2"></div>
            <div class="ink-ff ff3"></div>
            <div class="ink-ff ff4"></div>
            <div class="ink-ff ff5"></div>
            <div class="ink-ff ff6"></div>
          </div>
          <!-- 柳枝 -->
          <div class="ink-willow"></div>
          <!-- 漂浮信笺 -->
          <div v-for="b in oceanBottleItems" :key="b.id"
            class="ink-scroll"
            :class="b.dir === 1 ? 'float-r' : 'float-l'"
            :style="{ top: b.top + '%', '--sd': b.speed + 's' }"
            @animationend="removeBottle(b.id)">📜</div>
          <!-- 操作按钮 -->
          <div class="ink-actions">
            <button class="ink-btn send" @click="openThrow">📜 寄一封书笺</button>
            <button class="ink-btn recv" @click="catchBottle">🎐 收一封书笺</button>
          </div>
          <!-- 收信动画 -->
          <div class="ink-catch" v-if="catchAnim" :key="catchI">
            <div class="ick-ripple" style="left:calc(50% - 30px);top:52%;"></div>
            <div class="ick-ripple" style="left:calc(50% - 30px);top:56%;animation-delay:.7s;"></div>
            <div class="ick-scroll">📜</div>
          </div>
        </div>
        <div class="ocean-results">
          <div :class="['or-card', { show: throwResult }]" v-if="throwResult">
            <div class="or-header">
              <span class="or-mood">{{ moodEmoji[throwResult.mood] || '📜' }}</span>
              <span class="or-mood-label">{{ throwResult.mood }} · 书笺已随风寄出</span>
              <span class="or-time">刚刚</span>
            </div>
            <div class="or-msg">作为回礼，这里有本书送给你</div>
            <div class="or-divider"></div>
            <div class="or-book" @click="throwResult.book && goToBook(throwResult.book.id)">
              <div v-if="throwResult.book" class="orb-c" :style="getCoverStyle(throwResult.book.id)">{{ throwResult.book.title?.substring(0, 2) }}</div>
              <div class="orb-info" v-if="throwResult.book">
                <div class="orb-title">{{ throwResult.book.title }}</div>
                <div class="orb-author">{{ throwResult.book.author }}</div>
                <div class="orb-price">¥{{ throwResult.book.onDiscount ? throwResult.book.discountPrice : throwResult.book.price }} <span v-if="throwResult.book.avgRating">★ {{ throwResult.book.avgRating.toFixed(1) }}</span></div>
              </div>
              <span class="orb-arrow">→</span>
            </div>
            <div class="or-actions">
              <button class="or-btn primary" @click="favBook(throwResult.book)">♡ 收藏</button>
              <button class="or-btn ghost" @click="cartBook(throwResult.book)">🛒 加入购物车</button>
            </div>
          </div>
          <div :class="['or-card', { show: catchResult }]" v-if="catchResult">
            <div class="or-header">
              <span class="or-mood">{{ moodEmoji[catchResult.mood] || '📜' }}</span>
              <span class="or-mood-label">{{ catchResult.mood }} · {{ catchResult.time }}</span>
              <span class="or-time"></span>
            </div>
            <div class="or-msg">{{ catchResult.msg }}</div>
            <div class="or-divider"></div>
            <div class="or-book" @click="catchResult.book && goToBook(catchResult.book.id)">
              <div v-if="catchResult.book" class="orb-c" :style="getCoverStyle(catchResult.book.id)">{{ catchResult.book.title?.substring(0, 2) }}</div>
              <div class="orb-info" v-if="catchResult.book">
                <div class="orb-title">{{ catchResult.book.title }}</div>
                <div class="orb-author">{{ catchResult.book.author }}</div>
                <div class="orb-price">¥{{ catchResult.book.onDiscount ? catchResult.book.discountPrice : catchResult.book.price }} <span v-if="catchResult.book.avgRating">★ {{ catchResult.book.avgRating.toFixed(1) }}</span></div>
              </div>
              <span class="orb-arrow">→</span>
            </div>
            <div class="or-actions">
              <button class="or-btn primary" @click="favBook(catchResult.book)">♡ 收藏</button>
              <button class="or-btn ghost" @click="cartBook(catchResult.book)">🛒 加入购物车</button>
              <button class="or-btn next" @click="catchResult = null; catchBottle()">📜 再收一封</button>
            </div>
          </div>
          <div class="or-stats">
            <div class="or-stat">📤 已寄 <span class="ors-n">{{ thrown }}</span></div>
            <div class="or-stat">📥 已收 <span class="ors-n">{{ fished }}</span></div>
            <div class="or-stat">🌊 湖中 <span class="ors-n">{{ oceanBottles.length }}</span></div>
          </div>
        </div>
      </div>

      <!-- ═══ COMMUNITY ═══ -->
      <div v-show="activeTab === 'community'" class="tab-panel">
        <div class="community-feed" ref="communityFeed">
          <div class="cf-waterfall">
            <div v-for="(col, ci) in communityColumns" :key="ci" class="cf-col">
              <div
                v-for="post in col"
                :key="post.id"
                class="cf-post"
                @click="openCommunityPost(post)"
              >
                <div class="cfp-img-wrap">
                  <img :src="post.image" :alt="post.content" class="cfp-img" loading="lazy" />
                </div>
                <div class="cfp-content">
                  <p class="cfp-text">{{ post.content }}</p>
                  <div class="cfp-bottom">
                    <div class="cfp-user">
                      <span class="cfp-avatar">{{ post.username[0] }}</span>
                      <span class="cfp-name">{{ post.username }}</span>
                    </div>
                    <button class="cfp-like" :class="{ liked: post.liked }" @click.stop="toggleLike(post)">
                      <svg viewBox="0 0 24 24" width="16" height="16" :fill="post.liked ? '#ff4d6a' : 'none'" stroke="currentColor" stroke-width="2">
                        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                      </svg>
                      <span>{{ post.likes }}</span>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- THROW MODAL -->
    <Teleport to="body">
      <div :class="['tm-overlay', { show: throwModalOpen }]">
        <div class="tm-bg" @click="closeThrow"></div>
        <div class="tm-card">
          <div class="tm-head">
            <h3>📜 书笺寄语</h3>
            <button class="tm-close" @click="closeThrow">✕</button>
          </div>
          <div class="tm-body">
            <div class="field">
              <div class="field-label">此刻心绪 <span class="fl-r">*</span></div>
              <div class="mood-grid">
                <div v-for="m in moods" :key="m.key"
                  :class="['mood-item', { sel: tMood === m.key }]"
                  @click="tMood = m.key">
                  <span class="mi-e">{{ m.icon }}</span>
                  <span class="mi-t">{{ m.label }}</span>
                </div>
              </div>
            </div>
            <div class="field">
              <div class="field-label">落笔寄语 <span class="fl-r">*</span></div>
              <textarea class="tm-textarea" v-model="tMsg" placeholder="写下此刻的心情..." maxlength="200"></textarea>
              <div class="tm-char">{{ tMsg.length }}/200</div>
            </div>
            <div class="field">
              <div class="field-label">随笺荐书 <span class="fl-r">*</span></div>
              <div class="bp-search">
                <span class="bps-i">🔍</span>
                <input v-model="bookQuery" placeholder="搜索书名或作者...">
              </div>
              <div class="bp-list">
                <div v-for="b in filteredBooks" :key="b.id"
                  :class="['bp-book', { sel: tBId === b.id }]"
                  @click="tBId = b.id">
                  <div class="bpb-c" :style="getCoverStyle(b.id)">{{ b.title.substring(0, 2) }}</div>
                  <div>
                    <div class="bpb-t">{{ b.title }}</div>
                    <div class="bpb-a">{{ b.author }}</div>
                  </div>
                  <span class="bpb-chk">✓</span>
                </div>
              </div>
            </div>
            <button class="tm-submit" :disabled="!canThrow" @click="doThrow">📜 寄出书笺</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Throw arc animation -->
    <div :class="['throw-arc', { show: throwArc }]">
      <div class="ta-bottle" ref="taBottle">📜</div>
      <div :class="['ta-splash', { burst: splashBurst }]">
        <div class="tas-rings"></div>
        <div class="tas-droplet dr1"></div>
        <div class="tas-droplet dr2"></div>
        <div class="tas-droplet dr3"></div>
        <div class="tas-droplet dr4"></div>
        <div class="tas-droplet dr5"></div>
      </div>
    </div>

    <!-- Community Post Detail Modal -->
    <Teleport to="body">
      <div :class="['cpd-overlay', { show: communityPostOpen }]" @click.self="closeCommunityPost">
        <div class="cpd-modal">
          <button class="cpd-close" @click="closeCommunityPost">✕</button>
          <div class="cpd-content" v-if="selectedPost">
            <div class="cpd-image-section">
              <img :src="selectedPost.image" :alt="selectedPost.content" class="cpd-image" />
            </div>
            <div class="cpd-info-section">
              <div class="cpd-user">
                <div class="cpd-avatar">{{ selectedPost.username[0] }}</div>
                <div class="cpd-user-info">
                  <span class="cpd-username">{{ selectedPost.username }}</span>
                  <span class="cpd-time">{{ selectedPost.time }}</span>
                </div>
              </div>
              <div class="cpd-divider"></div>
              <p class="cpd-text">{{ selectedPost.content }}</p>
              <div class="cpd-divider"></div>
              <div class="cpd-book" v-if="selectedPost.book" @click="goToBook(selectedPost.book.id)">
                <div class="cpd-book-cover" :style="getCoverStyle(selectedPost.book.id)">
                  {{ selectedPost.book.title.substring(0, 2) }}
                </div>
                <div class="cpd-book-info">
                  <div class="cpd-book-title">{{ selectedPost.book.title }}</div>
                  <div class="cpd-book-author">{{ selectedPost.book.author }}</div>
                </div>
                <span class="cpd-book-arrow">→</span>
              </div>
              <div class="cpd-actions">
                <button class="cpd-like-btn" :class="{ liked: selectedPost.liked }" @click="toggleLike(selectedPost)">
                  <svg viewBox="0 0 24 24" width="22" height="22" :fill="selectedPost.liked ? '#ff4d6a' : 'none'" stroke="currentColor" stroke-width="2">
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                  </svg>
                  <span>{{ selectedPost.likes }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Publish Post Modal -->
    <Teleport to="body">
      <div :class="['pm-overlay', { show: publishModalOpen }]">
        <div class="pm-bg" @click="closePublishModal"></div>
        <div class="pm-modal">
          <div class="pm-head">
            <h3>📖 发布帖子</h3>
            <button class="pm-close" @click="closePublishModal">✕</button>
          </div>
          <div class="pm-body">
            <div class="pm-field">
              <div class="pm-field-label">选择图片</div>
              <div class="pm-image-picker" @click="triggerImageUpload">
                <img v-if="newPost.imagePreview" :src="newPost.imagePreview" class="pm-img-preview" />
                <div v-else class="pm-img-placeholder">
                  <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.5">
                    <rect x="3" y="3" width="18" height="18" rx="2"/>
                    <circle cx="8.5" cy="8.5" r="1.5"/>
                    <path d="M21 15l-5-5L5 21"/>
                  </svg>
                  <span>点击上传图片</span>
                </div>
                <input ref="imageInputRef" type="file" accept="image/*" style="display:none" @change="handleImageUpload" />
              </div>
            </div>
            <div class="pm-field">
              <div class="pm-field-label">分享你的阅读感悟</div>
              <textarea class="pm-textarea" v-model="newPost.content" placeholder="写下你读这本书的感受..." maxlength="200"></textarea>
              <div class="pm-char">{{ newPost.content.length }}/200</div>
            </div>
            <div class="pm-field">
              <div class="pm-field-label">关联书籍（可选）</div>
              <div class="pm-book-search">
                <span class="pmbs-i">🔍</span>
                <input v-model="publishBookQuery" placeholder="搜索书名关联书籍..." />
              </div>
              <div v-if="publishSelectedBook" class="pm-book-selected">
                <div class="pmbs-info">
                  <span class="pmbs-title">{{ publishSelectedBook.title }}</span>
                  <span class="pmbs-author">{{ publishSelectedBook.author }}</span>
                </div>
                <button class="pmbs-clear" @click="publishSelectedBook = null">✕</button>
              </div>
              <div v-else class="pm-book-list">
                <div v-for="b in filteredPublishBooks" :key="b.id"
                  class="pm-book-item"
                  @click="publishSelectedBook = b">
                  <div class="pmbi-cover" :style="getCoverStyle(b.id)">{{ b.title.substring(0, 2) }}</div>
                  <div>
                    <div class="pmbi-title">{{ b.title }}</div>
                    <div class="pmbi-author">{{ b.author }}</div>
                  </div>
                </div>
              </div>
            </div>
            <button class="pm-submit" :disabled="!canPublish" @click="doPublish">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
              </svg>
              发布
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Toast container -->
    <div class="toast-wrap" id="toastWrap">
      <div v-for="(t, i) in toasts" :key="i" :class="['toast', { out: t.out }]">
        <span class="t-dot"></span>{{ t.msg }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { bookApi } from '@/api/book'
import { cartApi } from '@/api/cart'
import { favoriteApi } from '@/api/favorite'
import { communityApi } from '@/api/community'
import { useCartStore } from '@/stores/cart'
import { useFavoriteStore } from '@/stores/favorite'
import { getCoverStyle } from '@/utils/cover'

const router = useRouter()
const cartStore = useCartStore()
const favoriteStore = useFavoriteStore()

const activeTab = ref('fortune')

/* ─── TOAST ─── */
const toasts = ref([])
let toastId = 0
function toast(msg) {
  const id = ++toastId
  toasts.value.push({ msg, out: false, id })
  setTimeout(() => {
    const t = toasts.value.find(x => x.id === id)
    if (t) t.out = true
    setTimeout(() => { toasts.value = toasts.value.filter(x => x.id !== id) }, 300)
  }, 2500)
}

/* ─── SPARKLE / CONFETTI ─── */
function sparkle(x, y) {
  const cs = ['#E8D48A','#C09A4B','#D4B06A','#fff']
  for (let i = 0; i < 22; i++) {
    const el = document.createElement('div')
    el.className = 'fx-sparkle'
    el.style.left = x + 'px'
    el.style.top = y + 'px'
    el.style.background = cs[Math.floor(Math.random() * cs.length)]
    const a = Math.random() * Math.PI * 2, d = 50 + Math.random() * 130
    el.style.setProperty('--sx', Math.cos(a) * d + 'px')
    el.style.setProperty('--sy', Math.sin(a) * d + 'px')
    const s = 2 + Math.random() * 3
    el.style.width = s + 'px'
    el.style.height = s + 'px'
    document.body.appendChild(el)
    setTimeout(() => el.remove(), 650)
  }
}

function confetti(x, y) {
  const cs = ['#C09A4B','#D4B06A','#5C8856','#4A6E8A']
  for (let i = 0; i < 14; i++) {
    const el = document.createElement('div')
    el.className = 'fx-confetti'
    el.style.left = x + 'px'
    el.style.top = y + 'px'
    el.style.background = cs[Math.floor(Math.random() * cs.length)]
    el.style.setProperty('--tx', (Math.random() - 0.5) * 180 + 'px')
    el.style.setProperty('--ty', (Math.random() - 0.5) * 180 + 'px')
    el.style.setProperty('--r', Math.random() * 720 - 360 + 'deg')
    document.body.appendChild(el)
    setTimeout(() => el.remove(), 850)
  }
}

/* ═══ FORTUNE ═══ */
const fortunePool = ref([])
const collected = ref(new Set())
const threeF = ref([])
const deckReady = ref(false)
const cardsEntering = ref(false)
const curFort = ref(null)
const flippedIdx = ref(-1)
const zoomedIdx = ref(-1)
const flipDx = ref(0)
const flipDy = ref(0)
const hideOtherCards = ref(false)
const hintText = ref('点击「换一批」抽取三张命运卡牌')
const hintActive = ref(false)
const justGotId = ref(null)
const curExcerpt = ref('')
const fortuneInited = ref(false)
const liftedIdx = ref(-1)

const particles = Array.from({ length: 12 }, (_, i) => {
  const s = 2 + Math.random() * 3
  return {
    id: i,
    style: {
      width: s + 'px',
      height: s + 'px',
      left: Math.random() * 100 + '%',
      top: Math.random() * 100 + '%',
      opacity: 0.08 + Math.random() * 0.12,
      animation: `fp ${6 + Math.random() * 8}s ease-in-out ${Math.random() * 5}s infinite`
    }
  }
})
const stars = Array.from({ length: 30 }, (_, i) => {
  const s = 1 + Math.random() * 1.5
  return {
    id: i,
    style: {
      left: Math.random() * 100 + '%',
      top: Math.random() * 100 + '%',
      opacity: 0.1 + Math.random() * 0.3,
      width: s + 'px',
      height: s + 'px'
    }
  }
})

const collectedPercent = computed(() => fortunePool.value.length ? (collected.value.size / fortunePool.value.length) * 100 : 0)

const fortuneMessages = [
  { symbol: '🌙', fortune: '世间的孤独终将在文字中得到温暖。', tag: '孤独之书' },
  { symbol: '🌟', fortune: '今天适合重拾纯真。像孩子一样看世界。', tag: '纯真之书' },
  { symbol: '🚀', fortune: '你的思维边界即将被打破。', tag: '星际之书' },
  { symbol: '🔥', fortune: '命运有时会给出最沉重的考题，但活着本身就是最好的答案。', tag: '生命之书' },
  { symbol: '🍃', fortune: '地坛的古柏见证过比你更多的风雨。', tag: '力量之书' },
  { symbol: '☽', fortune: '不要忘了抬头看看月亮。', tag: '理想之书' },
  { symbol: '🏛️', fortune: '每一个时代都有人选择了勇敢。', tag: '历史之书' },
  { symbol: '🧠', fortune: '改变从认知开始。觉醒，就是现在。', tag: '觉醒之书' },
  { symbol: '✨', fortune: '十万年的人类历史浓缩在一本书里。', tag: '时空之书' },
  { symbol: '🌿', fortune: '森林里藏着最原始的生活智慧。', tag: '自然之书' },
  { symbol: '🎭', fortune: '城里的人想逃出来，城外的人想冲进去。', tag: '人间之书' },
  { symbol: '🌊', fortune: '翠翠的故事就像溪水，清澈而忧伤。', tag: '溪水之书' }
]

function initFortune() {
  if (fortuneInited.value) return
  fortuneInited.value = true
  const pool = fortuneMessages.map((m, i) => ({ ...m, id: i + 1 }))
  // assign books randomly from loaded list
  assignBooksToPool(pool)
  fortunePool.value = pool
  drawFortune()
}

function assignBooksToPool(pool) {
  const source = allBooks.value.length ? allBooks.value : FALLBACK_BOOKS
  const targetTitles = [
    '百年孤独', '小王子', '三体', '活着', '我与地坛', '月亮与六便士',
    '人类简史', '认知觉醒', '1984', '额尔古纳河', '围城', '边城'
  ]
  pool.forEach((f, i) => {
    const match = source.find(b => b.title.includes(targetTitles[i]))
    f.book = match || source[i % source.length]
  })
}

let allBooks = ref([])

const FALLBACK_BOOKS = [
  { id: 3, title: '三体', author: '刘慈欣', price: 68.00, avgRating: 9.5 },
  { id: 5, title: '百年孤独', author: '马尔克斯', price: 55.00, avgRating: 9.4 },
  { id: 4, title: '活着', author: '余华', price: 35.00, avgRating: 9.6 },
  { id: 6, title: '小王子', author: '圣埃克苏佩里', price: 28.00, avgRating: 9.7 },
  { id: 31, title: '人类简史', author: '赫拉利', price: 68.00, avgRating: 9.1 },
  { id: 26, title: '1984', author: '奥威尔', price: 42.00, avgRating: 9.3 },
  { id: 12, title: '月亮与六便士', author: '毛姆', price: 42.00, avgRating: 9.0 },
  { id: 9, title: '围城', author: '钱钟书', price: 39.00, avgRating: 9.1 },
  { id: 32, title: '我与地坛', author: '史铁生', price: 36.00, avgRating: 9.6 },
  { id: 16, title: '边城', author: '沈从文', price: 29.00, avgRating: 9.0 },
  { id: 30, title: '额尔古纳河右岸', author: '迟子建', price: 48.00, avgRating: 9.2 },
  { id: 33, title: '认知觉醒', author: '周岭', price: 49.80, avgRating: 8.6 },
]

// Load books for the exploration features
async function loadBooks() {
  try {
    const res = await bookApi.getList({ pageNum: 1, pageSize: 50, status: 1 })
    allBooks.value = res.data?.records?.length ? res.data.records : FALLBACK_BOOKS
  } catch (e) {
    console.error('Failed to load books, using fallback:', e)
    allBooks.value = FALLBACK_BOOKS
  }
}

function drawFortune() {
  if (!fortunePool.value.length) return
  curFort.value = null
  deckReady.value = false
  cardsEntering.value = true
  flippedIdx.value = -1
  zoomedIdx.value = -1
  flipDx.value = 0
  flipDy.value = 0
  hideOtherCards.value = false
  liftedIdx.value = -1

  const av = fortunePool.value.filter(f => !collected.value.has(f.id))
  const pool = av.length >= 3 ? av : fortunePool.value
  threeF.value = [...pool].sort(() => Math.random() - 0.5).slice(0, 3)

  hintText.value = '选择一张卡牌，翻开你的命运'
  hintActive.value = false

  setTimeout(() => {
    deckReady.value = true
    cardsEntering.value = false
  }, 700)
}

function cardStyle(idx) {
  if (flippedIdx.value === -1) {
    if (liftedIdx.value === idx) {
      return {
        transform: 'translateY(-18px) scale(1.05)',
        transition: 'transform 0.22s cubic-bezier(0.15,0.85,0.35,1)',
        zIndex: 10
      }
    }
    return {}
  }
  if (flippedIdx.value === idx) {
    const zoomed = zoomedIdx.value === idx
    const scale = zoomed ? 1.18 : 1
    return {
      transform: `rotateY(180deg) scale(${scale})`,
      zIndex: 10,
      transition: zoomed ? 'transform 0.38s cubic-bezier(0.25,0.8,0.25,1)' : 'transform 0.7s cubic-bezier(0.22,0.85,0.35,1)',
      transformStyle: 'preserve-3d'
    }
  }
  const dir = idx - flippedIdx.value > 0 ? 1 : -1
  return {
    opacity: 0,
    transform: `translateX(${dir * 90}px) scale(0.75)`,
    pointerEvents: 'none',
    transition: 'transform 0.5s cubic-bezier(0.55,0,0.1,1), opacity 0.45s ease'
  }
}

function pickCard(idx) {
  if (!deckReady.value) return
  deckReady.value = false

  const ct = document.querySelector('.three-cards')
  const cards = ct ? Array.from(ct.querySelectorAll('.pick-card')) : []
  const ch = cards[idx]

  liftedIdx.value = idx

  setTimeout(() => {
    flippedIdx.value = idx
    liftedIdx.value = -1
  }, 280)

  setTimeout(() => {
    hideOtherCards.value = true
  }, 780)

  setTimeout(() => {
    zoomedIdx.value = idx
  }, 1000)

  setTimeout(() => {
    const f = threeF.value[idx]
    curFort.value = f
    collected.value = new Set([...collected.value, f.id])
    justGotId.value = f.id
    setTimeout(() => { justGotId.value = null }, 700)

    if (f.book?.id) {
      bookApi.getExcerpt(f.book.id).then(res => {
        curExcerpt.value = res.data?.content || ''
      }).catch(() => {
        curExcerpt.value = ''
      })
    } else {
      curExcerpt.value = ''
    }

    if (ch) {
      const cr = ch.getBoundingClientRect()
      sparkle(cr.left + cr.width / 2, cr.top + cr.height / 2)
      confetti(cr.left + cr.width / 2, cr.top + cr.height / 3)
    }

    if (collected.value.size === fortunePool.value.length) {
      hintText.value = '🎉 恭喜集齐全部命运卡牌！'
      toast('🎉 全部 ' + fortunePool.value.length + ' 张命运卡牌已集齐！')
    } else {
      hintText.value = f.tag + ' — 已收集 ' + collected.value.size + '/' + fortunePool.value.length
    }
    hintActive.value = true
  }, 1200)
}

function favFortune() {
  if (!curFort.value || !curFort.value.book) return
  favBook(curFort.value.book)
}

function cartFortune() {
  if (!curFort.value || !curFort.value.book) return
  cartBook(curFort.value.book)
}

/* ═══ INK LETTER ═══ */
const oceanBottleItems = ref([])
let bottleId = 0
let spawnTimer = null

function spawnScroll() {
  const dir = Math.random() > 0.5 ? 1 : -1
  const top = 30 + Math.random() * 22
  const speed = (10 + Math.random() * 6).toFixed(1)
  oceanBottleItems.value.push({ id: ++bottleId, dir, speed, top })
}

function removeBottle(id) {
  oceanBottleItems.value = oceanBottleItems.value.filter(b => b.id !== id)
}

function refreshOceanScene() {
  oceanBottleItems.value = []

  for (let i = 0; i < 3; i++) setTimeout(spawnScroll, i * 600)

  if (spawnTimer) clearInterval(spawnTimer)
  spawnTimer = setInterval(() => {
    if (Math.random() > 0.2) spawnScroll()
  }, 2500)
}

const moods = [
  { key: '心潮', icon: '🌊', label: '心潮' },
  { key: '恬淡', icon: '🍵', label: '恬淡' },
  { key: '独思', icon: '🌙', label: '独思' },
  { key: '探幽', icon: '🔍', label: '探幽' },
  { key: '浮躁', icon: '🍂', label: '浮躁' },
  { key: '迷途', icon: '🌫️', label: '迷途' }
]
const moodEmoji = { 心潮: '🌊', 恬淡: '🍵', 独思: '🌙', 探幽: '🔍', 浮躁: '🍂', 迷途: '🌫️' }

const oceanBottles = ref([
  { mood: '心潮', msg: '今日阳光正好，想读些轻快的故事', time: '3小时前' },
  { mood: '独思', msg: '夜深人静，需一本温暖的书相伴', time: '昨日' },
  { mood: '探幽', msg: '近来对宇宙万象颇为着迷', time: '5小时前' },
  { mood: '迷途', msg: '前路茫茫，不知归处', time: '2天前' },
  { mood: '恬淡', msg: '烹茶焚香，寻一本可慢慢读的书', time: '8小时前' },
  { mood: '浮躁', msg: '案牍劳形，想遁入另一个天地', time: '1天前' },
  { mood: '心潮', msg: '新得佳音，欲以好书贺之', time: '4小时前' },
  { mood: '独思', msg: '独在异乡，四顾无相识', time: '3天前' },
  { mood: '探幽', msg: '有友荐《明朝那些事儿》，可好？', time: '6小时前' },
  { mood: '恬淡', msg: '午后斜阳，正宜读一本诗集', time: '昨日' },
  { mood: '浮躁', msg: '科考在即，压力颇大，欲求力量', time: '12小时前' },
  { mood: '迷途', msg: '而立之年，犹不知心之所向', time: '2天前' }
])

// assign random books to bottles
oceanBottles.value.forEach(b => { b.bookId = null; b.book = null })

const thrown = ref(0)
const fished = ref(0)
const catchI = ref(0)
const catchAnim = ref(false)
const throwResult = ref(null)
const catchResult = ref(null)
const throwArc = ref(false)
const splashBurst = ref(false)
const throwModalOpen = ref(false)
const tMood = ref(null)
const tMsg = ref('')
const tBId = ref(null)
const bookQuery = ref('')

const filteredBooks = computed(() => {
  const q = bookQuery.value.trim().toLowerCase()
  if (!allBooks.value.length) return []
  let list = q ? allBooks.value.filter(b => b.title.toLowerCase().includes(q) || b.author.toLowerCase().includes(q)) : allBooks.value.slice(0, 8)
  return list
})

const canThrow = computed(() => tMsg.value.trim().length >= 3 && tMood.value && tBId.value)

function openThrow() {
  throwModalOpen.value = true
  tMood.value = null
  tBId.value = null
  tMsg.value = ''
  bookQuery.value = ''
}

function closeThrow() {
  throwModalOpen.value = false
}

function doThrow() {
  if (!canThrow.value) return
  const book = allBooks.value.find(b => b.id === tBId.value)
  oceanBottles.value.push({ mood: tMood.value, msg: tMsg.value.trim(), time: '刚刚', bookId: tBId.value, book })
  thrown.value++
  closeThrow()

  // throw animation
  throwArc.value = true
  splashBurst.value = false
  setTimeout(() => { splashBurst.value = true }, 850)
  setTimeout(() => { throwArc.value = false }, 1400)

  // Recommend a book as return gift
  const rec = allBooks.value.length ? allBooks.value[Math.floor(Math.random() * allBooks.value.length)] : null
  if (rec) {
    throwResult.value = { mood: tMood.value, book: rec }
  }
  toast('📜 书笺已随风寄出')
  confetti(window.innerWidth / 2, window.innerHeight / 2)
}

function catchBottle() {
  if (!oceanBottles.value.length) {
    toast('湖面暂无漂来的书笺...')
    return
  }
  const bottle = oceanBottles.value[catchI.value % oceanBottles.value.length]
  catchI.value++
  fished.value++

  catchAnim.value = true
  setTimeout(() => { catchAnim.value = false }, 1600)

  // assign a book if not assigned
  if (!bottle.book && allBooks.value.length) {
    bottle.book = allBooks.value[Math.floor(Math.random() * allBooks.value.length)]
  }

  setTimeout(() => {
    catchResult.value = bottle
    // water sparkle at catch point
    const sc = document.querySelector('.ink-scene')
    if (sc) {
      const sr = sc.getBoundingClientRect()
      sparkle(sr.left + sr.width / 2, sr.top + sr.height * 0.45)
    }
    confetti(window.innerWidth / 2, window.innerHeight / 2)
  }, 1700)
}

/* ═══ COMMUNITY ═══ */
const communityPosts = ref([
  { id: 1, username: '林小雅', content: '读完《瓦尔登湖》，决定开始极简生活', image: 'https://picsum.photos/seed/walden/400/500', likes: 128, liked: false, time: '2小时前', book: null },
  { id: 2, username: '书虫阿东', content: '深夜推理时间，《白夜行》太绝了', image: 'https://picsum.photos/seed/whiteNight/400/300', likes: 89, liked: false, time: '3小时前', book: null },
  { id: 3, username: '雨落江南', content: '诗集读完了，整个人都安静下来', image: 'https://picsum.photos/seed/poetry/400/600', likes: 256, liked: false, time: '5小时前', book: FALLBACK_BOOKS[3] },
  { id: 4, username: '北漂青年', content: '打工人的日常，地铁上读完半本《活着》', image: 'https://picsum.photos/seed/toLive/400/350', likes: 67, liked: false, time: '昨天', book: FALLBACK_BOOKS[2] },
  { id: 5, username: '文艺大叔', content: '三体让我对宇宙充满敬畏', image: 'https://picsum.photos/seed/threeBody/400/450', likes: 312, liked: false, time: '1天前', book: FALLBACK_BOOKS[0] },
  { id: 6, username: '小红迷', content: '月下看《围城》，方渐鸿的无奈', image: 'https://picsum.photos/seed/fortress/400/400', likes: 178, liked: false, time: '2天前', book: FALLBACK_BOOKS[7] },
  { id: 7, username: '阅读者', content: '人类简史刷新了我的认知边界', image: 'https://picsum.photos/seed/sapiens/400/550', likes: 234, liked: false, time: '2天前', book: FALLBACK_BOOKS[4] },
  { id: 8, username: '书语者', content: '我与地坛的文字让我泪流满面', image: 'https://picsum.photos/seed/ditian/400/320', likes: 445, liked: false, time: '3天前', book: FALLBACK_BOOKS[8] },
  { id: 9, username: '墨染白', content: '边城的翠翠，那条溪水清澈如她的眼眸', image: 'https://picsum.photos/seed/biancheng/400/480', likes: 189, liked: false, time: '3天前', book: FALLBACK_BOOKS[9] },
  { id: 10, username: '星河书社', content: '认知觉醒，让我开始真正思考', image: 'https://picsum.photos/seed/awaken/400/380', likes: 156, liked: false, time: '4天前', book: FALLBACK_BOOKS[11] },
  { id: 11, username: '豆瓣用户', content: '百年孤独里的魔幻现实让人着迷', image: 'https://picsum.photos/seed/century/400/520', likes: 278, liked: false, time: '4天前', book: FALLBACK_BOOKS[1] },
  { id: 12, username: '读书笔记', content: '小王子的纯真，是我们丢失的美好', image: 'https://picsum.photos/seed/principle/400/280', likes: 367, liked: false, time: '5天前', book: FALLBACK_BOOKS[3] },
])

let postIdCounter = 100

const communityColumns = computed(() => {
  const cols = [[], [], [], []]
  communityPosts.value.forEach((post, i) => {
    cols[i % 4].push(post)
  })
  return cols
})

const communityPostOpen = ref(false)
const selectedPost = ref(null)

function openCommunityPost(post) {
  selectedPost.value = post
  communityPostOpen.value = true
}

function closeCommunityPost() {
  communityPostOpen.value = false
}

function toggleLike(post) {
  post.liked = !post.liked
  post.likes += post.liked ? 1 : -1
}

/* ═══ COMMUNITY PUBLISH ═══ */
const publishModalOpen = ref(false)
const publishBookQuery = ref('')
const publishSelectedBook = ref(null)
const imageInputRef = ref(null)

const newPost = ref({
  imagePreview: '',
  content: '',
  imageData: null,
})

const filteredPublishBooks = computed(() => {
  const q = publishBookQuery.value.trim().toLowerCase()
  if (!allBooks.value.length) return []
  let list = q ? allBooks.value.filter(b => b.title.toLowerCase().includes(q) || b.author.toLowerCase().includes(q)) : allBooks.value.slice(0, 8)
  return list
})

const canPublish = computed(() => newPost.value.imagePreview && newPost.value.content.trim().length >= 3)

function openPublishModal() {
  publishModalOpen.value = true
  newPost.value = { imagePreview: '', content: '', imageData: null }
  publishSelectedBook.value = null
  publishBookQuery.value = ''
}

function closePublishModal() {
  publishModalOpen.value = false
}

function triggerImageUpload() {
  imageInputRef.value?.click()
}

function handleImageUpload(e) {
  const file = e.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => {
    newPost.value.imagePreview = ev.target.result
    newPost.value.imageData = file
  }
  reader.readAsDataURL(file)
}

async function doPublish() {
  if (!canPublish.value) return
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    await communityApi.add({
      username: userInfo.username || '我',
      content: newPost.value.content.trim(),
      imageUrl: newPost.value.imagePreview,
      bookId: publishSelectedBook.value?.id || null,
    })
    await loadCommunityPosts()
    closePublishModal()
    toast('📖 帖子发布成功！')
    confetti(window.innerWidth / 2, window.innerHeight / 2)
  } catch (e) {
    ElMessage.error('发布失败')
  }
}

async function loadCommunityPosts() {
  try {
    const res = await communityApi.list()
    communityPosts.value = (res.data || []).map(p => ({
      ...p,
      image: p.imageUrl,
      liked: p.liked === 1,
      book: p.bookId ? { id: p.bookId, title: p.bookTitle } : null,
    }))
  } catch (e) {
    console.error('loadCommunityPosts error', e)
  }
}

/* ═══ SHARED ACTIONS ═══ */
function goToBook(id) {
  router.push(`/book/${id}`)
}

async function favBook(book) {
  if (!book) return
  try {
    await favoriteApi.add(String(book.id))
    favoriteStore.fetchFavoriteIds()
    toast('已收藏「' + book.title + '」')
  } catch {
    ElMessage.error('收藏失败')
  }
}

async function cartBook(book) {
  if (!book) return
  try {
    await cartStore.addToCart(book.id, 1)
    toast('「' + book.title + '」已加入购物车')
  } catch {
    ElMessage.error('加入购物车失败')
  }
}

/* ═══ WATCHERS ═══ */
watch(activeTab, (val) => {
  if (val === 'fortune' && !fortuneInited.value) {
    initFortune()
  }
  if (val === 'ocean') {
    refreshOceanScene()
  } else {
    if (spawnTimer) { clearInterval(spawnTimer); spawnTimer = null }
  }
  if (val === 'community') {
    loadCommunityPosts()
  }
})

/* ═══ INIT ═══ */
onUnmounted(() => {
  if (spawnTimer) clearInterval(spawnTimer)
})

onMounted(async () => {
  await loadBooks()
  // assign books to ocean bottles
  oceanBottles.value.forEach(b => {
    if (allBooks.value.length) {
      b.book = allBooks.value[Math.floor(Math.random() * allBooks.value.length)]
    }
  })
  // re-assign fortune pool books
  if (fortunePool.value.length) assignBooksToPool(fortunePool.value)
  initFortune()
  refreshOceanScene()
})
</script>

<style scoped>
.explore-page {
  min-height: 100vh;
  background: var(--color-bg);
}

.page-header {
  background: var(--color-primary-dark);
  position: relative;
  overflow: hidden;
}
.page-header::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 75% 35%, rgba(192,154,75,0.06) 0%, transparent 50%),
              radial-gradient(ellipse at 20% 80%, rgba(92,68,52,0.15) 0%, transparent 45%);
  pointer-events: none;
}
.ph-inner {
  position: relative;
  z-index: 1;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 44px 40px 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 24px;
}
.ph-left h1 {
  font-size: 2rem;
  color: var(--color-bg-warm);
  font-weight: 900;
  margin-bottom: 6px;
}
.ph-left p {
  font-size: 0.9rem;
  color: rgba(237,230,214,0.4);
}
.ph-stats { display: flex; gap: 16px; }
.phs {
  background: rgba(237,230,214,0.04);
  border: 1px solid rgba(237,230,214,0.06);
  border-radius: 10px;
  padding: 14px 22px;
  text-align: center;
  min-width: 80px;
}
.phs-num {
  font-size: 1.35rem;
  font-weight: 900;
  color: var(--color-accent-light);
  line-height: 1;
}
.phs-label {
  font-size: 0.72rem;
  color: rgba(237,230,214,0.35);
  margin-top: 4px;
}

/* ── TAB BAR ── */
.tab-bar {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 16px 40px;
  display: flex;
  gap: 12px;
  border-bottom: 1px solid var(--color-divider);
}
.tab-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  border-radius: 24px;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.3s;
  border: 1.5px solid transparent;
}
.tab-item:hover {
  border-color: var(--color-divider-strong);
}
.tab-item.active {
  background: var(--color-primary);
  color: var(--color-bg-warm);
  border-color: var(--color-primary);
}
.ti-icon { font-size: 1rem; }

/* ── SECTION WRAP ── */
.section-wrap {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 36px 40px 60px;
}
.tab-panel { animation: panelIn 0.45s ease; }
@keyframes panelIn { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }

/* ═══ FORTUNE STAGE ═══ */
.fortune-stage {
  position: relative;
  background: linear-gradient(180deg, var(--color-primary-abyss) 0%, #2a1a3a 30%, #3d2a54 60%, var(--color-primary-dark) 100%);
  border-radius: 16px;
  padding: 56px 32px 48px;
  overflow: hidden;
  min-height: 560px;
}
.fortune-stage::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 50% 30%, rgba(192,154,75,0.06) 0%, transparent 50%),
              radial-gradient(ellipse at 20% 70%, rgba(90,61,122,0.1) 0%, transparent 40%);
  pointer-events: none;
}
.fortune-particles { position: absolute; inset: 0; pointer-events: none; overflow: hidden; }
.fortune-stars { position: absolute; inset: 0; pointer-events: none; }

.fortune-progress {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 28px;
}
.fp-bar-wrap {
  width: 200px;
  height: 6px;
  background: rgba(237,230,214,0.06);
  border-radius: 3px;
  overflow: hidden;
}
.fp-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--color-gold-dark, #8B6914), var(--color-accent-light));
  border-radius: 3px;
  transition: width 0.6s cubic-bezier(0.4,0.2,0.2,1);
}
.fp-text {
  font-size: 0.78rem;
  color: rgba(232,212,138,0.4);
  font-family: monospace;
}
.fp-text strong { color: var(--color-accent-light); }

.card-stage {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.cards-area {
  display: flex;
  justify-content: center;
  align-items: stretch;
  gap: 40px;
  min-height: 354px;
  width: 100%;
}
.three-cards {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  height: 354px;
  position: relative;
  perspective: 1000px;
  overflow: visible;
  flex-shrink: 0;
  transition: gap 0.35s ease;
}
.three-cards.collapsed {
  gap: 0;
}
.pick-card {
  width: 200px;
  height: 300px;
  cursor: pointer;
  position: relative;
  flex-shrink: 0;
  transform-style: preserve-3d;
  transition: opacity 0.35s ease, transform 0.35s ease;
}
.pick-card.enter { opacity:0; animation: cardEnter 0.55s cubic-bezier(0.2,0.8,0.3,1) forwards; }
@keyframes cardEnter { from { opacity: 0; transform: translateY(40px) scale(0.88); } to { opacity: 1; transform: translateY(0) scale(1); } }
.pick-card.selectable { transition: transform 0.25s ease; }
.pick-card.selectable:hover { transform: translateY(-6px); }
.pick-card.selectable:hover .pc-back { border-color: rgba(192,154,75,0.4); box-shadow: 0 14px 40px rgba(0,0,0,0.5), 0 0 30px rgba(192,154,75,0.08); }

.pc-face {
  position: absolute;
  inset: 0;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
  border-radius: 12px;
  overflow: hidden;
  box-sizing: border-box;
}
.pc-back {
  background: linear-gradient(160deg, #1a1030, #2a1a40 30%, #3d2a54 60%, #1a1030);
  border: 2px solid rgba(192,154,75,0.2);
  box-shadow: 0 8px 30px rgba(0,0,0,0.4), inset 0 0 30px rgba(192,154,75,0.04);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.3s, box-shadow 0.3s;
}
.pc-back-inner {
  width: 168px;
  height: 268px;
  border: 1.5px solid rgba(192,154,75,0.12);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}
.pc-back-inner::before {
  content: '';
  position: absolute;
  inset: 6px;
  border: 1px solid rgba(192,154,75,0.06);
  border-radius: 6px;
}
.pc-symbol { font-size: 2rem; margin-bottom: 12px; filter: drop-shadow(0 0 10px rgba(192,154,75,0.3)); }
.pc-title { font-size: 1.1rem; color: var(--color-accent-light); letter-spacing: 0.1em; text-shadow: 0 0 16px rgba(192,154,75,0.15); }
.pc-sub { font-size: 0.58rem; color: rgba(232,212,138,0.25); letter-spacing: 0.18em; margin-top: 3px; }
.pc-cd {
  position: absolute;
  width: 5px;
  height: 5px;
  background: var(--color-accent);
  transform: rotate(45deg);
  opacity: 0.15;
}
.pc-cd.tl { top: 14px; left: 14px; }
.pc-cd.tr { top: 14px; right: 14px; }
.pc-cd.bl { bottom: 14px; left: 14px; }
.pc-cd.br { bottom: 14px; right: 14px; }
.pc-shimmer {
  position: absolute;
  inset: 0;
  background: linear-gradient(105deg, transparent 40%, rgba(232,212,138,0.05) 45%, rgba(232,212,138,0.1) 50%, rgba(232,212,138,0.05) 55%, transparent 60%);
  animation: shimmerSlide 3s ease-in-out infinite;
  pointer-events: none;
}
@keyframes shimmerSlide { 0%,100% { transform: translateX(-80%); } 50% { transform: translateX(80%); } }
.pc-number {
  position: absolute;
  bottom: 10px;
  right: 14px;
  font-size: 0.55rem;
  color: rgba(232,212,138,0.15);
}
.pc-front {
  transform: rotateY(180deg);
  background: var(--color-bg-card);
  border: 2px solid rgba(192,154,75,0.3);
  box-shadow: 0 8px 30px rgba(0,0,0,0.25);
  display: flex;
  flex-direction: column;
  padding: 16px;
  z-index: 1;
}
.pc-front-icon { font-size: 1.6rem; text-align: center; margin-bottom: 8px; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.12)); }
.pc-front-fortune {
  font-size: 0.88rem;
  color: var(--color-text);
  line-height: 1.7;
  text-align: center;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}
.pc-front-tag {
  font-size: 0.62rem;
  padding: 3px 10px;
  border-radius: 10px;
  background: var(--color-accent-glow);
  color: var(--color-accent-muted);
  font-weight: 600;
  text-align: center;
  margin-bottom: 8px;
  align-self: center;
}
.pc-front-book { text-align: center; padding-top: 8px; border-top: 1px solid var(--color-divider); }
.pfb-title { font-size: 0.82rem; font-weight: 700; color: var(--color-text); }
.pfb-author { font-size: 0.66rem; color: var(--color-text-light); }
.pfb-price { font-weight: 700; color: var(--color-accent-muted); font-size: 0.78rem; margin-top: 2px; }

.card-hint {
  font-size: 0.8rem;
  color: rgba(237,230,214,0.25);
  text-align: center;
  margin-bottom: 20px;
  position: relative;
  z-index: 2;
  transition: color 0.3s;
}
.card-hint.active { color: rgba(232,212,138,0.5); animation: hintPop 0.5s cubic-bezier(0.34,1.56,0.64,1); }
@keyframes hintPop {
  from { opacity: 0.3; transform: scale(0.92); }
  to { opacity: 1; transform: scale(1); }
}

.btn-draw {
  padding: 14px 48px;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 700;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  z-index: 2;
  font-family: var(--font-body);
}
.btn-draw.secondary {
  background: rgba(237,230,214,0.08);
  color: var(--color-accent-light);
  box-shadow: none;
  border: 1px solid rgba(192,154,75,0.15);
  font-size: 0.88rem;
  padding: 12px 36px;
}
.btn-draw.secondary:hover { background: rgba(192,154,75,0.1); border-color: rgba(192,154,75,0.3); }

.card-actions { display: none; gap: 10px; position: relative; z-index: 2; flex-wrap: wrap; justify-content: center; }
.card-actions.show { display: flex; animation: fadeUp 0.4s ease; }
@keyframes fadeUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

.ca-btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 0.84rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  font-family: var(--font-body);
}
.ca-btn.primary { background: var(--color-primary); color: var(--color-bg-warm); }
.ca-btn.primary:hover { background: var(--color-primary-mid); }
.ca-btn.accent {
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted));
  color: var(--color-primary-abyss);
}
.ca-btn.accent:hover { transform: translateY(-2px); box-shadow: 0 4px 14px rgba(192,154,75,0.3); }
.ca-btn.ghost { background: rgba(237,230,214,0.06); border: 1px solid rgba(237,230,214,0.1); color: rgba(237,230,214,0.5); }
.ca-btn.ghost:hover { border-color: rgba(192,154,75,0.2); color: var(--color-accent-light); }

/* Book Detail Panel */
.book-detail-panel {
  width: 500px;
  height: 354px;
  flex-shrink: 0;
  background: rgba(10, 8, 20, 0.95);
  border: 1px solid rgba(192, 154, 75, 0.15);
  border-radius: 12px;
  opacity: 0;
  transform: translateX(20px);
  transition: opacity 0.5s ease 0.3s, transform 0.5s cubic-bezier(0.25, 0.8, 0.25, 1) 0.3s;
  overflow: hidden;
  display: none;
  pointer-events: none;
}
.book-detail-panel.show {
  opacity: 1;
  transform: translateX(0);
  pointer-events: auto;
  display: flex;
}
.book-detail-panel.show .bdp-inner > * {
  animation: bdpFadeUp 0.5s cubic-bezier(0.25,0.8,0.25,1) both;
}
.book-detail-panel.show .bdp-inner > *:nth-child(1) { animation-delay: 0.15s; }
.book-detail-panel.show .bdp-inner > *:nth-child(2) { animation-delay: 0.3s; }
.book-detail-panel.show .bdp-inner > *:nth-child(3) { animation-delay: 0.45s; }
@keyframes bdpFadeUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
.bdp-inner {
  display: flex;
  flex-direction: column;
  padding: 28px 32px;
}
.bdp-top {
  flex-shrink: 0;
  margin-bottom: 16px;
}
.bdp-title {
  font-size: 1.25rem;
  font-weight: 800;
  color: #ede6d6;
  line-height: 1.4;
  letter-spacing: 0.02em;
  font-family: 'Noto Serif SC', 'Songti SC', serif;
}
.bdp-author {
  font-size: 0.78rem;
  color: rgba(237, 230, 214, 0.55);
  margin-top: 6px;
  letter-spacing: 0.08em;
}
.bdp-body {
  padding: 12px 0;
}
.bdp-quote {
  font-size: 0.95rem;
  color: rgba(237, 230, 214, 0.88);
  line-height: 1.9;
  font-style: italic;
  letter-spacing: 0.02em;
  text-align: justify;
  font-family: 'Crimson Pro', 'Georgia', serif;
  position: relative;
  padding: 0 4px;
}
.bdp-quote::before {
  content: '"';
  font-size: 3rem;
  color: rgba(192, 154, 75, 0.12);
  position: absolute;
  top: -16px;
  left: -8px;
  font-family: 'Georgia', serif;
  pointer-events: none;
}
.bdp-foot {
  text-align: center;
  padding-top: 12px;
}
.bdp-divider {
  display: block;
  width: 32px;
  height: 1px;
  background: rgba(192, 154, 75, 0.25);
  margin: 0 auto 10px;
}
.bdp-fortune-label {
  font-size: 0.6rem;
  color: rgba(192, 154, 75, 0.55);
  letter-spacing: 0.2em;
  text-transform: uppercase;
  margin-bottom: 4px;
}
.bdp-fortune-msg {
  font-size: 0.85rem;
  color: var(--color-accent-light);
  line-height: 1.7;
  font-weight: 500;
}

/* Collection */
.fortune-collection {
  position: relative;
  z-index: 2;
  margin-top: 36px;
  padding-top: 24px;
  border-top: 1px solid rgba(237,230,214,0.04);
}
.fc-title {
  font-size: 0.82rem;
  color: rgba(237,230,214,0.3);
  text-align: center;
  margin-bottom: 14px;
  letter-spacing: 0.08em;
}
.fc-grid {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
  max-width: 600px;
  margin: 0 auto;
}
.fc-card {
  width: 52px;
  height: 72px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.15rem;
  border: 1.5px solid rgba(192,154,75,0.1);
  background: rgba(192,154,75,0.03);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}
.fc-card:hover { transform: translateY(-3px) scale(1.05); border-color: rgba(192,154,75,0.3); }
.fc-card.collected { border-color: rgba(192,154,75,0.2); background: rgba(192,154,75,0.06); }
.fc-card.just-got { animation: justGot 0.6s ease; }
@keyframes justGot { 0% { transform: scale(1); } 30% { transform: scale(1.3); } 100% { transform: scale(1); } }
.fcc-num {
  position: absolute;
  bottom: 3px;
  right: 5px;
  font-size: 0.45rem;
  color: rgba(232,212,138,0.3);
  font-family: monospace;
}

/* ═══ INK WASH SCENE ═══ */
.ink-scene {
  position: relative;
  width: 100%;
  height: 440px;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(180deg,
    #0a0e1a 0%, #0f1628 15%, #141e35 28%,
    #162a3a 40%, #1a3348 52%, #1e3a50 60%,
    #162a3a 72%, #0f1e2e 85%, #0a1520 100%);
}

/* 宣纸纹理叠加 */
.ink-scene::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.015'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  pointer-events: none;
  z-index: 0;
  opacity: 0.6;
}

/* 月亮 */
.ink-moon {
  position: absolute;
  top: 10%;
  right: 18%;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: radial-gradient(circle at 35% 35%, #f5ecd0, #d4c088);
  box-shadow: 0 0 30px rgba(212,192,136,0.2), 0 0 60px rgba(212,192,136,0.08);
  z-index: 1;
}
.ink-moon-glow {
  position: absolute;
  top: 7%;
  right: 14%;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(212,192,136,0.06), transparent 70%);
  z-index: 0;
  animation: moonPulse 6s ease-in-out infinite;
}
@keyframes moonPulse {
  0%,100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.15); opacity: 1; }
}

/* 远山 */
.ink-mountains {
  position: absolute;
  bottom: 35%;
  left: 0;
  right: 0;
  height: 180px;
  z-index: 1;
  pointer-events: none;
}
.ink-mt {
  position: absolute;
  bottom: 0;
  border-radius: 50% 50% 0 0;
  opacity: 0.6;
}
.mt1 {
  left: -5%;
  width: 45%;
  height: 120px;
  background: linear-gradient(180deg, rgba(30,58,80,0.4), rgba(22,42,58,0.2));
}
.mt2 {
  left: 25%;
  width: 50%;
  height: 160px;
  background: linear-gradient(180deg, rgba(26,51,72,0.5), rgba(18,36,52,0.3));
}
.mt3 {
  right: -8%;
  width: 40%;
  height: 100px;
  background: linear-gradient(180deg, rgba(34,64,88,0.35), rgba(22,42,58,0.15));
}

/* 雾气 */
.ink-fog {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(ellipse, rgba(255,255,255,0.04) 0%, transparent 70%);
  pointer-events: none;
  z-index: 2;
  animation: fogDrift var(--fd, 18s) ease-in-out var(--fdd, 0s) infinite;
}
.fog1 { width: 300px; height: 50px; bottom: 38%; left: 5%; --fd: 20s; --fdd: 0s; }
.fog2 { width: 240px; height: 40px; bottom: 42%; left: 50%; --fd: 16s; --fdd: 6s; }
@keyframes fogDrift {
  0% { transform: translateX(0) scaleX(1); opacity: 0; }
  20% { opacity: 0.4; }
  50% { transform: translateX(30px) scaleX(1.1); opacity: 0.25; }
  80% { opacity: 0.1; }
  100% { transform: translateX(60px) scaleX(1.2); opacity: 0; }
}

/* 湖面 */
.ink-lake {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 55%;
  background: linear-gradient(180deg,
    rgba(22,42,58,0.1) 0%,
    rgba(15,30,46,0.4) 20%,
    rgba(10,21,32,0.7) 50%,
    rgba(8,18,26,0.9) 100%);
  z-index: 2;
}

/* 波光 */
.ink-ripples {
  position: absolute;
  bottom: 8%;
  left: 0;
  right: 0;
  height: 60px;
  z-index: 3;
  pointer-events: none;
}
.ink-rip {
  position: absolute;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg,
    transparent 5%,
    rgba(212,192,136,0.06) 20%,
    rgba(212,192,136,0.1) 40%,
    rgba(212,192,136,0.06) 60%,
    transparent 95%);
  animation: rippleShimmer var(--rd, 4s) ease-in-out var(--rdd, 0s) infinite;
}
.r1 { top: 0; --rd: 5s; --rdd: 0s; }
.r2 { top: 20px; --rd: 7s; --rdd: 1.5s; opacity: 0.6; }
.r3 { top: 40px; --rd: 6s; --rdd: 3s; opacity: 0.3; }
@keyframes rippleShimmer {
  0%,100% { opacity: 0.3; transform: scaleX(0.95); }
  50% { opacity: 0.8; transform: scaleX(1.05); }
}

/* 萤火虫 */
.ink-fireflies {
  position: absolute;
  inset: 0;
  z-index: 4;
  pointer-events: none;
}
.ink-ff {
  position: absolute;
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: rgba(212,192,136,0.6);
  box-shadow: 0 0 6px rgba(212,192,136,0.3), 0 0 12px rgba(212,192,136,0.1);
  animation: fireflyFloat var(--ffd, 8s) ease-in-out var(--ffdd, 0s) infinite;
}
.ff1 { top: 15%; left: 20%; --ffd: 9s; --ffdd: 0s; }
.ff2 { top: 25%; left: 60%; --ffd: 11s; --ffdd: 2s; width: 2px; height: 2px; }
.ff3 { top: 35%; left: 35%; --ffd: 7s; --ffdd: 4s; }
.ff4 { top: 20%; left: 80%; --ffd: 10s; --ffdd: 1s; width: 2px; height: 2px; }
.ff5 { top: 40%; left: 15%; --ffd: 12s; --ffdd: 5s; }
.ff6 { top: 30%; left: 70%; --ffd: 8s; --ffdd: 3s; width: 2px; height: 2px; }
@keyframes fireflyFloat {
  0% { transform: translate(0, 0); opacity: 0; }
  15% { opacity: 0.8; }
  30% { transform: translate(15px, -10px); opacity: 0.4; }
  50% { transform: translate(-8px, -20px); opacity: 0.9; }
  70% { transform: translate(12px, -5px); opacity: 0.3; }
  85% { opacity: 0.6; }
  100% { transform: translate(0, 0); opacity: 0; }
}

/* 柳枝 */
.ink-willow {
  position: absolute;
  bottom: 30%;
  left: -10px;
  width: 120px;
  height: 180px;
  z-index: 5;
  pointer-events: none;
  opacity: 0.25;
  background: radial-gradient(ellipse at 0% 30%, transparent 55%, rgba(80,120,80,0.15) 60%, transparent 75%);
  animation: willowSway 8s ease-in-out infinite;
  transform-origin: top left;
}
.ink-willow::before {
  content: '';
  position: absolute;
  top: 0;
  left: 30px;
  width: 80px;
  height: 160px;
  background: repeating-linear-gradient(160deg,
    transparent 0px, transparent 8px,
    rgba(80,120,80,0.12) 8px, rgba(80,120,80,0.12) 9px);
  border-radius: 0 0 40% 0;
}
@keyframes willowSway {
  0%,100% { transform: rotate(0deg); }
  50% { transform: rotate(2deg); }
}

/* 漂浮信笺 */
.ink-scroll {
  position: absolute;
  font-size: 1.5rem;
  z-index: 6;
  cursor: pointer;
  filter: drop-shadow(0 1px 4px rgba(212,192,136,0.15));
}
.ink-scroll.float-r { animation: scrollFloatRight var(--sd, 13s) linear forwards; }
.ink-scroll.float-l { animation: scrollFloatLeft var(--sd, 13s) linear forwards; }
@keyframes scrollFloatRight {
  0% { left: -50px; transform: translateY(0) rotate(-2deg); }
  25% { transform: translateY(-5px) rotate(1deg); }
  50% { transform: translateY(2px) rotate(-1deg); }
  75% { transform: translateY(-4px) rotate(1.5deg); }
  100% { left: calc(100% + 50px); transform: translateY(0) rotate(-1deg); }
}
@keyframes scrollFloatLeft {
  0% { left: calc(100% + 50px); transform: translateY(0) rotate(2deg); }
  25% { transform: translateY(-5px) rotate(-1deg); }
  50% { transform: translateY(2px) rotate(1deg); }
  75% { transform: translateY(-4px) rotate(-1.5deg); }
  100% { left: -50px; transform: translateY(0) rotate(1deg); }
}

/* 操作按钮 */
.ink-actions {
  position: absolute;
  bottom: 32px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 14px;
  z-index: 10;
}
.ink-btn {
  padding: 13px 36px;
  border-radius: 12px;
  font-size: 0.92rem;
  font-weight: 700;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(10px);
  font-family: var(--font-body);
}
.ink-btn.send {
  background: rgba(212,192,136,0.1);
  color: rgba(245,236,208,0.9);
  border: 1px solid rgba(212,192,136,0.15);
}
.ink-btn.send:hover {
  background: rgba(212,192,136,0.18);
  transform: translateY(-2px);
}
.ink-btn.recv {
  background: linear-gradient(135deg, rgba(212,192,136,0.25), rgba(192,154,75,0.2));
  color: #f5ecd0;
  box-shadow: 0 4px 16px rgba(212,192,136,0.15);
}
.ink-btn.recv:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(212,192,136,0.25);
}

/* 收信动画 */
.ink-catch {
  position: absolute;
  inset: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}
.ick-scroll {
  font-size: 2.8rem;
  position: relative;
  z-index: 2;
  animation: scrollRise 1.1s cubic-bezier(0.2,0.8,0.3,1) 0.3s both;
  filter: drop-shadow(0 4px 12px rgba(212,192,136,0.15));
}
@keyframes scrollRise {
  0% { transform: translateY(80px) rotate(-5deg); opacity: 0; }
  30% { transform: translateY(10px) rotate(2deg); opacity: 0.7; }
  60% { transform: translateY(-6px) rotate(-1deg); opacity: 1; }
  100% { transform: translateY(-20px) rotate(3deg); opacity: 1; }
}
.ick-ripple {
  position: absolute;
  width: 60px;
  height: 14px;
  border: 1.5px solid rgba(212,192,136,0.12);
  border-radius: 50%;
  animation: inkRipple 0.8s ease-out 0.6s both;
}
@keyframes inkRipple {
  0% { transform: scale(0); opacity: 0.5; }
  100% { transform: scale(3); opacity: 0; }
}

/* 抛出动画 */
.throw-arc {
  position: fixed;
  inset: 0;
  z-index: 220;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.2s;
}
.throw-arc.show { opacity: 1; }
.ta-bottle {
  position: absolute;
  font-size: 2.2rem;
  bottom: 22%;
  left: 48%;
  transform: translateX(-50%);
  filter: drop-shadow(0 2px 8px rgba(212,192,136,0.2));
  animation: taFly 1.2s cubic-bezier(0.2,0.8,0.3,1) forwards;
}
@keyframes taFly {
  0% { bottom: 22%; left: 48%; transform: translateX(-50%) rotate(0) scale(1); opacity: 1; }
  45% { bottom: 58%; left: 52%; transform: translateX(-50%) rotate(-8deg) scale(1.05); opacity: 1; }
  100% { bottom: 36%; left: 56%; transform: translateX(-50%) rotate(12deg) scale(0.3); opacity: 0; }
}
.ta-splash {
  position: absolute;
  bottom: 32%;
  left: 52%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: radial-gradient(ellipse, rgba(212,192,136,0.12), transparent);
  transform: translateX(-50%);
  transition: all 0.5s ease-out;
}
.ta-splash.burst { width: 100px; height: 28px; opacity: 0; }
.ta-splash .tas-rings {
  position: absolute;
  inset: -10px;
  border: 1.5px solid rgba(212,192,136,0.08);
  border-radius: 50%;
  opacity: 0;
  transition: none;
}
.ta-splash.burst .tas-rings {
  animation: splashRing 0.7s ease-out 0.15s forwards;
}
@keyframes splashRing {
  0% { transform: scale(0.2); opacity: 0.5; }
  100% { transform: scale(2.5); opacity: 0; }
}
.tas-droplet {
  position: absolute;
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: rgba(212,192,136,0.2);
  opacity: 0;
  transition: none;
}
.ta-splash.burst .tas-droplet {
  animation: dropletFly var(--dd, 0.6s) ease-out var(--ddd, 0s) forwards;
}
.dr1 { --dd: 0.6s; --ddd: 0s; --dx: -30px; --dy: -25px; }
.dr2 { --dd: 0.7s; --ddd: 0.05s; --dx: 25px; --dy: -30px; }
.dr3 { --dd: 0.55s; --ddd: 0.1s; --dx: -18px; --dy: -18px; }
.dr4 { --dd: 0.65s; --ddd: 0s; --dx: 35px; --dy: -22px; }
.dr5 { --dd: 0.5s; --ddd: 0.08s; --dx: -40px; --dy: -15px; }
@keyframes dropletFly {
  0% { transform: translate(0, 0) scale(0.5); opacity: 0; }
  10% { opacity: 0.6; transform: translate(0, 0) scale(1); }
  100% { transform: translate(var(--dx), var(--dy)) scale(0.2); opacity: 0; }
}

/* ═══ COMMUNITY ═══ */
.community-feed {
  min-height: 400px;
}
.cf-waterfall {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  align-items: start;
}
.cf-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.cf-post {
  background: var(--color-bg-card);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid var(--color-divider);
}
.cf-post:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px var(--color-shadow-medium);
  border-color: var(--color-accent);
}
.cfp-img-wrap {
  overflow: hidden;
  background: var(--color-bg);
}
.cfp-img {
  width: 100%;
  display: block;
  transition: transform 0.4s ease;
}
.cf-post:hover .cfp-img {
  transform: scale(1.05);
}
.cfp-content {
  padding: 10px 12px 12px;
}
.cfp-text {
  font-size: 0.8rem;
  color: var(--color-text);
  line-height: 1.5;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.cfp-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.cfp-user {
  display: flex;
  align-items: center;
  gap: 6px;
}
.cfp-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-bg-warm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.6rem;
  font-weight: 700;
}
.cfp-name {
  font-size: 0.68rem;
  color: var(--color-text-light);
}
.cfp-like {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 12px;
  border: none;
  background: var(--color-bg);
  color: var(--color-text-light);
  font-size: 0.7rem;
  cursor: pointer;
  transition: all 0.25s;
}
.cfp-like:hover {
  background: rgba(255,77,106,0.08);
  color: #ff4d6a;
}
.cfp-like.liked {
  color: #ff4d6a;
}

/* Community Post Detail Modal */
.cpd-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition: all 0.35s;
}
.cpd-overlay.show {
  opacity: 1;
  visibility: visible;
}
.cpd-overlay::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(10,20,40,0.75);
  backdrop-filter: blur(10px);
}
.cpd-modal {
  position: relative;
  z-index: 1;
  width: 820px;
  max-width: 92vw;
  max-height: 88vh;
  background: var(--color-bg-card);
  border-radius: 16px;
  box-shadow: 0 32px 80px rgba(0,0,0,0.5);
  transform: scale(0.92) translateY(20px);
  transition: transform 0.4s cubic-bezier(0.34,1.56,0.64,1);
  overflow: hidden;
  display: flex;
}
.cpd-overlay.show .cpd-modal {
  transform: scale(1) translateY(0);
}
.cpd-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: rgba(255,255,255,0.15);
  color: rgba(255,255,255,0.8);
  font-size: 1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  z-index: 10;
  backdrop-filter: blur(4px);
}
.cpd-close:hover {
  background: rgba(255,255,255,0.25);
  color: #fff;
}
.cpd-content {
  display: flex;
  width: 100%;
  height: 100%;
  max-height: 88vh;
}
.cpd-image-section {
  width: 50%;
  flex-shrink: 0;
  background: var(--color-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.cpd-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.cpd-overlay.show .cpd-image {
  animation: cpdImgIn 0.5s ease 0.1s both;
}
@keyframes cpdImgIn {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
.cpd-info-section {
  flex: 1;
  padding: 32px 28px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0;
}
.cpd-info-section::-webkit-scrollbar { width: 4px; }
.cpd-info-section::-webkit-scrollbar-thumb { background: var(--color-divider-strong); border-radius: 2px; }
.cpd-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.cpd-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-bg-warm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  font-weight: 700;
  flex-shrink: 0;
}
.cpd-user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.cpd-username {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--color-text);
}
.cpd-time {
  font-size: 0.72rem;
  color: var(--color-text-light);
}
.cpd-divider {
  width: 36px;
  height: 1px;
  background: var(--color-divider-strong);
  margin: 14px 0;
}
.cpd-text {
  font-size: 0.9rem;
  color: var(--color-text);
  line-height: 1.8;
}
.cpd-book {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--color-bg-cream);
  border: 1px solid var(--color-divider);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s;
  margin-top: 12px;
}
.cpd-book:hover {
  border-color: var(--color-accent);
  transform: translateX(4px);
}
.cpd-book-cover {
  width: 36px;
  height: 48px;
  border-radius: 4px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.28rem;
  color: rgba(237,230,214,0.7);
}
.cpd-book-info {
  flex: 1;
}
.cpd-book-title {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--color-text);
}
.cpd-book-author {
  font-size: 0.7rem;
  color: var(--color-text-light);
  margin-top: 2px;
}
.cpd-book-arrow {
  font-size: 0.85rem;
  color: var(--color-text-light);
}
.cpd-actions {
  margin-top: auto;
  padding-top: 20px;
  display: flex;
  gap: 10px;
}
.cpd-like-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 10px;
  border: 1.5px solid var(--color-divider-strong);
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  font-size: 0.84rem;
  cursor: pointer;
  transition: all 0.25s;
}
.cpd-like-btn:hover {
  border-color: #ff4d6a;
  color: #ff4d6a;
  background: rgba(255,77,106,0.06);
}
.cpd-like-btn.liked {
  border-color: #ff4d6a;
  color: #ff4d6a;
  background: rgba(255,77,106,0.08);
}

/* Community header */
.community-header {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 40px;
}
.ch-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0 0;
}
.ch-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text-secondary);
}
.ch-publish-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 20px;
  border: none;
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted));
  color: var(--color-primary-abyss);
  font-size: 0.84rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 14px rgba(192,154,75,0.25);
}
.ch-publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(192,154,75,0.35);
}

/* Publish Modal */
.pm-overlay {
  position: fixed;
  inset: 0;
  z-index: 210;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition: all 0.35s;
}
.pm-overlay.show { opacity: 1; visibility: visible; }
.pm-bg {
  position: absolute;
  inset: 0;
  background: rgba(10,20,40,0.72);
  backdrop-filter: blur(8px);
}
.pm-modal {
  position: relative;
  z-index: 1;
  width: 480px;
  max-width: 92vw;
  max-height: 88vh;
  overflow-y: auto;
  background: var(--color-bg-card);
  border-radius: 16px;
  box-shadow: 0 24px 60px rgba(0,0,0,0.45);
  transform: translateY(24px) scale(0.96);
  transition: transform 0.35s cubic-bezier(0.34,1.2,0.64,1);
}
.pm-overlay.show .pm-modal { transform: translateY(0) scale(1); }
.pm-modal::-webkit-scrollbar { width: 4px; }
.pm-modal::-webkit-scrollbar-thumb { background: var(--color-divider-strong); border-radius: 2px; }
.pm-head {
  padding: 24px 28px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--color-divider);
}
.pm-head h3 { font-size: 1.1rem; }
.pm-close {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: var(--color-bg);
  color: var(--color-text-light);
  font-size: 0.95rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.pm-close:hover { background: var(--color-divider); color: var(--color-text); }
.pm-body { padding: 20px 28px 28px; }
.pm-field { margin-bottom: 18px; }
.pm-field-label { font-size: 0.82rem; font-weight: 600; color: var(--color-text-secondary); margin-bottom: 8px; }
.pm-image-picker {
  border: 2px dashed var(--color-divider-strong);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s;
  min-height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.pm-image-picker:hover { border-color: var(--color-accent); }
.pm-img-preview {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}
.pm-img-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 30px;
  color: var(--color-text-light);
  font-size: 0.82rem;
}
.pm-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1.5px solid var(--color-divider-strong);
  border-radius: 10px;
  font-size: 0.88rem;
  color: var(--color-text);
  background: var(--color-bg-cream);
  outline: none;
  resize: vertical;
  min-height: 90px;
  line-height: 1.7;
  transition: border-color 0.2s;
  font-family: var(--font-body);
}
.pm-textarea:focus { border-color: var(--color-accent); box-shadow: 0 0 0 3px rgba(192,154,75,0.06); }
.pm-textarea::placeholder { color: var(--color-text-light); }
.pm-char { text-align: right; font-size: 0.7rem; color: var(--color-text-light); margin-top: 4px; }
.pm-book-search {
  display: flex;
  align-items: center;
  border: 1.5px solid var(--color-divider-strong);
  border-radius: 10px;
  padding: 4px;
  background: var(--color-bg-cream);
  transition: border-color 0.2s;
  margin-bottom: 8px;
}
.pm-book-search:focus-within { border-color: var(--color-accent); }
.pm-book-search input { flex: 1; border: none; background: transparent; outline: none; padding: 8px; font-size: 0.84rem; color: var(--color-text); }
.pm-book-search input::placeholder { color: var(--color-text-light); }
.pmbs-i { padding: 6px; font-size: 0.85rem; color: var(--color-text-light); }
.pm-book-list { display: flex; flex-direction: column; gap: 6px; max-height: 160px; overflow-y: auto; }
.pm-book-list::-webkit-scrollbar { width: 3px; }
.pm-book-list::-webkit-scrollbar-thumb { background: var(--color-divider-strong); border-radius: 2px; }
.pm-book-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border: 1.5px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.pm-book-item:hover { background: var(--color-bg-cream); border-color: var(--color-divider); }
.pmbi-cover {
  width: 28px;
  height: 38px;
  border-radius: 3px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.26rem;
  color: rgba(237,230,214,0.7);
}
.pmbi-title { font-size: 0.8rem; font-weight: 600; color: var(--color-text); }
.pmbi-author { font-size: 0.68rem; color: var(--color-text-light); }
.pm-book-selected {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: var(--color-accent-glow);
  border: 1.5px solid var(--color-accent);
  border-radius: 10px;
  margin-top: 4px;
}
.pmbs-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.pmbs-title { font-size: 0.84rem; font-weight: 600; color: var(--color-accent-muted); }
.pmbs-author { font-size: 0.7rem; color: var(--color-text-light); }
.pmbs-clear {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: rgba(192,154,75,0.15);
  color: var(--color-accent-muted);
  font-size: 0.75rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.pmbs-clear:hover { background: rgba(192,154,75,0.3); }
.pm-submit {
  width: 100%;
  padding: 14px;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 700;
  border: none;
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted));
  color: var(--color-primary-abyss);
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 16px rgba(192,154,75,0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
}
.pm-submit:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(192,154,75,0.35); }
.pm-submit:disabled { opacity: 0.4; cursor: not-allowed; transform: none; box-shadow: none; }

/* Ocean results */
.ocean-results { margin-top: 24px; }
.or-card {
  display: none;
  background: var(--color-bg-cream);
  border: 1px solid var(--color-divider);
  border-radius: 12px;
  padding: 24px;
  animation: fadeUp 0.45s ease;
  position: relative;
  overflow: hidden;
  margin-bottom: 20px;
}
.or-card.show { display: block; }
.or-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--color-accent), rgba(212,192,136,0.4), var(--color-accent));
}
.or-header { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.or-mood { font-size: 1.4rem; }
.or-mood-label { font-size: 0.82rem; font-weight: 600; color: var(--color-text); }
.or-time { font-size: 0.72rem; color: var(--color-text-light); margin-left: auto; }
.or-msg {
  font-size: 1rem;
  color: var(--color-text);
  line-height: 2;
  font-style: italic;
  margin-bottom: 18px;
  padding: 0 8px;
  position: relative;
}
.or-msg::before { content: '\201C'; font-size: 2.5rem; color: var(--color-accent); opacity: 0.2; position: absolute; top: -14px; left: -6px; }
.or-msg::after { content: '\201D'; font-size: 2.5rem; color: var(--color-accent); opacity: 0.2; position: absolute; bottom: -28px; right: -6px; }
.or-divider { width: 36px; height: 1px; background: var(--color-divider-strong); margin: 0 auto 16px; }
.or-book {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s;
  margin-bottom: 16px;
}
.or-book:hover { border-color: var(--color-accent); transform: translateY(-2px); box-shadow: 0 6px 18px var(--color-shadow-heavy); }
.orb-c {
  width: 48px;
  height: 64px;
  border-radius: 6px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.42rem;
  color: rgba(237,230,214,0.7);
}
.orb-info { flex: 1; }
.orb-title { font-size: 0.95rem; font-weight: 600; color: var(--color-text); }
.orb-author { font-size: 0.76rem; color: var(--color-text-light); }
.orb-price { font-weight: 700; color: var(--color-accent-muted); font-size: 0.88rem; margin-top: 2px; }
.orb-arrow { color: var(--color-text-light); font-size: 0.85rem; }
.or-book:hover .orb-arrow { color: var(--color-accent); }
.or-actions { display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; }
.or-btn {
  padding: 9px 22px;
  border-radius: 8px;
  font-size: 0.82rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  font-family: var(--font-body);
}
.or-btn.primary { background: var(--color-primary); color: var(--color-bg-warm); }
.or-btn.primary:hover { background: var(--color-primary-mid); }
.or-btn.ghost { background: var(--color-bg-card); border: 1px solid var(--color-divider-strong); color: var(--color-text-secondary); }
.or-btn.ghost:hover { border-color: var(--color-primary-mid); color: var(--color-text); }
.or-btn.next { background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted)); color: var(--color-primary-abyss); font-weight: 600; }
.or-btn.next:hover { transform: translateY(-1px); }
.or-stats { display: flex; gap: 24px; justify-content: center; margin-top: 16px; }
.or-stat { display: flex; align-items: center; gap: 6px; font-size: 0.8rem; color: var(--color-text-light); }
.ors-n { color: var(--color-accent-muted); font-weight: 500; }

/* ═══ MODAL ═══ */
.tm-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition: all 0.35s;
}
.tm-overlay.show { opacity: 1; visibility: visible; }
.tm-bg {
  position: absolute;
  inset: 0;
  background: rgba(10,20,40,0.7);
  backdrop-filter: blur(8px);
}
.tm-card {
  position: relative;
  z-index: 1;
  width: 460px;
  max-width: 92vw;
  max-height: 85vh;
  overflow-y: auto;
  background: var(--color-bg-card);
  border-radius: 14px;
  box-shadow: 0 24px 60px rgba(0,0,0,0.4);
  transform: translateY(24px) scale(0.96);
  transition: transform 0.35s;
}
.tm-overlay.show .tm-card { transform: translateY(0) scale(1); }
.tm-card::-webkit-scrollbar { width: 4px; }
.tm-card::-webkit-scrollbar-thumb { background: var(--color-divider-strong); border-radius: 2px; }
.tm-head { padding: 24px 28px 16px; display: flex; align-items: center; justify-content: space-between; }
.tm-head h3 { font-size: 1.15rem; }
.tm-close {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: var(--color-bg-warm);
  color: var(--color-text-light);
  font-size: 0.95rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.tm-close:hover { background: var(--color-bg); color: var(--color-text); }
.tm-body { padding: 0 28px 28px; }
.field { margin-bottom: 18px; }
.field-label { font-size: 0.82rem; font-weight: 600; color: var(--color-text-secondary); margin-bottom: 8px; display: flex; align-items: center; gap: 4px; }
.fl-r { color: var(--color-red); font-size: 0.68rem; }
.mood-grid { display: flex; gap: 8px; flex-wrap: wrap; }
.mood-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 10px 14px;
  border: 1.5px solid var(--color-divider);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s;
  min-width: 68px;
  background: var(--color-bg-cream);
}
.mood-item:hover { border-color: var(--color-primary-mid); transform: translateY(-2px); }
.mood-item.sel { border-color: var(--color-accent); background: var(--color-accent-glow); }
.mi-e { font-size: 1.3rem; }
.mi-t { font-size: 0.72rem; color: var(--color-text-light); }
.mood-item.sel .mi-t { color: var(--color-accent-muted); }
.tm-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1.5px solid var(--color-divider-strong);
  border-radius: 10px;
  font-size: 0.88rem;
  color: var(--color-text);
  background: var(--color-bg-cream);
  outline: none;
  resize: vertical;
  min-height: 80px;
  line-height: 1.7;
  transition: border-color 0.2s;
}
.tm-textarea:focus { border-color: var(--color-accent); box-shadow: 0 0 0 3px rgba(192,154,75,0.06); }
.tm-textarea::placeholder { color: var(--color-text-light); }
.tm-char { text-align: right; font-size: 0.7rem; color: var(--color-text-light); margin-top: 4px; }
.bp-search {
  display: flex;
  align-items: center;
  border: 1.5px solid var(--color-divider-strong);
  border-radius: 10px;
  padding: 4px;
  background: var(--color-bg-cream);
  transition: border-color 0.2s;
  margin-bottom: 8px;
}
.bp-search:focus-within { border-color: var(--color-accent); }
.bp-search input { flex: 1; border: none; background: transparent; outline: none; padding: 10px; font-size: 0.84rem; color: var(--color-text); }
.bp-search input::placeholder { color: var(--color-text-light); }
.bps-i { padding: 8px; font-size: 0.85rem; color: var(--color-text-light); }
.bp-list { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; max-height: 180px; overflow-y: auto; }
.bp-list::-webkit-scrollbar { width: 3px; }
.bp-list::-webkit-scrollbar-thumb { background: var(--color-divider-strong); border-radius: 2px; }
.bp-book {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  border: 1.5px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.bp-book:hover { background: var(--color-bg-cream); border-color: var(--color-divider); }
.bp-book.sel { border-color: var(--color-accent); background: var(--color-accent-glow); }
.bpb-c {
  width: 28px;
  height: 38px;
  border-radius: 3px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.28rem;
  color: rgba(237,230,214,0.7);
}
.bpb-t { font-size: 0.78rem; font-weight: 600; color: var(--color-text); }
.bpb-a { font-size: 0.66rem; color: var(--color-text-light); }
.bpb-chk { margin-left: auto; font-size: 0.8rem; color: var(--color-accent); opacity: 0; transition: opacity 0.2s; }
.bp-book.sel .bpb-chk { opacity: 1; }
.tm-submit {
  width: 100%;
  padding: 14px;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 700;
  border: none;
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted));
  color: var(--color-primary-abyss);
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 16px rgba(192,154,75,0.25);
}
.tm-submit:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(192,154,75,0.35); }
.tm-submit:disabled { opacity: 0.4; cursor: not-allowed; transform: none; box-shadow: none; }

/* ── TOAST ── */
.toast-wrap {
  position: fixed;
  top: 80px;
  right: 32px;
  z-index: 300;
  display: flex;
  flex-direction: column;
  gap: 8px;
  pointer-events: none;
}
.toast {
  background: var(--color-primary-dark);
  color: var(--color-bg-warm);
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 0.84rem;
  box-shadow: 0 8px 24px var(--color-shadow-dark);
  display: flex;
  align-items: center;
  gap: 10px;
  animation: toastIn 0.35s ease;
}
.toast.out { animation: toastOut 0.3s ease forwards; }
.t-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; background: var(--color-green); }
@keyframes toastIn { from { opacity: 0; transform: translateX(20px); } to { opacity: 1; transform: translateX(0); } }
@keyframes toastOut { to { opacity: 0; transform: translateX(20px); } }

/* ── FX particles ── */
:global(.fx-sparkle) {
  position: fixed;
  pointer-events: none;
  z-index: 250;
  border-radius: 50%;
  animation: sparklePop 0.6s ease-out forwards;
}
.fp { position: absolute; border-radius: 50%; background: var(--color-accent-light); }
.fs { position: absolute; width: 2px; height: 2px; background: rgba(232,212,138,0.5); border-radius: 50%; }
@keyframes fp { 0%,100% { transform: translate(0); } 25% { transform: translate(10px,-15px); } 50% { transform: translate(-5px,10px); } 75% { transform: translate(15px,5px); } }

:global(.fx-confetti) {
  position: fixed;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  pointer-events: none;
  z-index: 999;
  animation: confettiB 0.8s ease-out forwards;
}
@keyframes confettiB { 0% { transform: translate(0,0) rotate(0); opacity: 1; } 100% { transform: translate(var(--tx),var(--ty)) rotate(var(--r)); opacity: 0; } }

/* ── Responsive ── */
@media (max-width: 1200px) {
  .cards-area { min-height: 307px; }
  .three-cards { height: 307px; gap: 16px; }
  .three-cards.collapsed { gap: 0; }
  .pick-card { width: 170px; height: 260px; }
  .pc-back-inner { width: 140px; height: 220px; }
  .book-detail-panel { width: 440px; height: 307px; }
}
@media (max-width: 768px) {
  .ph-inner { padding: 28px 20px; flex-direction: column; align-items: flex-start; }
  .ph-left h1 { font-size: 1.4rem; }
  .tab-bar { padding: 14px 20px; overflow-x: auto; }
  .section-wrap { padding: 24px 20px 40px; }
  .fortune-stage { padding: 40px 12px 36px; }
  .cards-area { min-height: 271px; gap: 16px; }
  .three-cards { height: 271px; gap: 10px; }
  .three-cards.collapsed { gap: 0; }
  .pick-card { width: 140px; height: 230px; }
  .pc-back-inner { width: 118px; height: 198px; }
  .book-detail-panel { width: 300px; height: 271px; }
  .pc-symbol { font-size: 1.4rem; }
  .pc-title { font-size: 0.85rem; }
  .pc-front { padding: 10px; }
  .pc-front-icon { font-size: 1.2rem; }
  .pc-front-fortune { font-size: 0.72rem; }
  .ink-scene { height: 360px; }
  .ink-actions { flex-direction: column; width: calc(100% - 32px); }
  .ink-btn { width: 100%; text-align: center; }
  .bp-list { grid-template-columns: 1fr; }
  .tm-card { max-width: 96vw; }
  .cf-waterfall { grid-template-columns: repeat(2, 1fr); }
  .cpd-content { flex-direction: column; }
  .cpd-image-section { width: 100%; height: 45vh; }
  .cpd-info-section { padding: 20px; }
  .community-header { padding: 0 20px; }
  .ch-inner { padding: 16px 0 0; }
  .ch-publish-btn { padding: 8px 16px; font-size: 0.8rem; }
  .pm-modal { max-width: 96vw; }
}
</style>

<!-- Global keyframes for dynamically created DOM elements (sparkle/confetti) -->
<style>
@keyframes sparklePop {
  0% { transform: translate(0,0) scale(1); opacity: 1; }
  100% { transform: translate(var(--sx),var(--sy)) scale(0); opacity: 0; }
}
@keyframes confettiB {
  0% { transform: translate(0,0) rotate(0); opacity: 1; }
  100% { transform: translate(var(--tx),var(--ty)) rotate(var(--r)); opacity: 0; }
}
</style>
