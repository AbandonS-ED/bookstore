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
        <span class="ti-icon">🍾</span>
        <span>书的漂流瓶</span>
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
            <div class="three-cards">
              <div
                v-for="(f, idx) in threeF"
                :key="f.id"
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

      <!-- ═══ OCEAN ═══ -->
      <div v-show="activeTab === 'ocean'" class="tab-panel">
        <div class="ocean-scene">
          <div class="os-sun-glow"></div>
          <div class="os-sun"></div>
          <div class="os-horizon"></div>
          <div class="os-reflection"></div>
          <div class="os-shimmer"></div>
          <div class="os-rays"></div>
          <div class="os-gulls"><span class="gull g1">🕊️</span><span class="gull g2">🕊️</span><span class="gull g3">🕊️</span></div>
          <div v-for="s in oceanShips" :key="s.id"
            class="os-boat"
            :class="s.dir === 1 ? 'sail-r' : 'sail-l'"
            :style="{ top: s.top + '%', '--sd': s.speed + 's' }"
            @animationend="removeShip(s.id)">⛵</div>
          <div class="os-mist m1"></div>
          <div class="os-mist m2"></div>
          <div class="os-mist m3"></div>
          <div v-for="b in oceanBottleItems" :key="b.id"
            class="os-bottle"
            :class="b.dir === 1 ? 'swim-r' : 'swim-l'"
            :style="{ top: b.top + '%', '--sd': b.speed + 's' }"
            @animationend="removeBottle(b.id)">🍾</div>
          <div class="os-bubble b1"></div>
          <div class="os-bubble b2"></div>
          <div class="os-bubble b3"></div>
          <div class="os-bubble b4"></div>
          <div class="os-waves">
            <div class="os-wave" style="--wd:8s;--wdd:0s;"><svg viewBox="0 0 1440 100" preserveAspectRatio="none"><path d="M0,35 C240,10 480,60 720,35 C960,10 1200,55 1440,30 L1440,100 L0,100 Z" fill="rgba(16,48,64,0.7)"/></svg></div>
            <div class="os-wave" style="bottom:6px;opacity:.5;--wd:11s;--wdd:-3s;"><svg viewBox="0 0 1440 100" preserveAspectRatio="none"><path d="M0,45 C360,20 720,70 1080,40 C1260,30 1380,50 1440,40 L1440,100 L0,100 Z" fill="rgba(26,72,88,0.5)"/></svg></div>
            <div class="os-wave" style="bottom:12px;opacity:.25;--wd:14s;--wdd:-6s;"><svg viewBox="0 0 1440 100" preserveAspectRatio="none"><path d="M0,50 C180,70 360,30 540,50 C720,70 900,35 1080,55 C1260,65 1380,40 1440,50 L1440,100 L0,100 Z" fill="rgba(16,48,64,0.4)"/></svg></div>
          </div>
          <div class="os-actions">
            <button class="os-btn throw" @click="openThrow">🍾 扔一个瓶子</button>
            <button class="os-btn catch" @click="catchBottle">🎣 捞一个瓶子</button>
          </div>
          <div class="os-catch" v-if="catchAnim" :key="catchI">
            <div class="oca-ripple" style="left:calc(50% - 30px);top:52%;"></div>
            <div class="oca-ripple" style="left:calc(50% - 30px);top:56%;animation-delay:.9s;"></div>
            <div class="oca-drip d1"></div>
            <div class="oca-drip d2"></div>
            <div class="oca-drip d3"></div>
            <div class="oca-bottle">🍾</div>
          </div>
        </div>
        <div class="ocean-results">
          <div :class="['or-card', { show: throwResult }]" v-if="throwResult">
            <div class="or-header">
              <span class="or-mood">{{ moodEmoji[throwResult.mood] || '🍾' }}</span>
              <span class="or-mood-label">{{ throwResult.mood }} · 你的漂流瓶已入海</span>
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
              <span class="or-mood">{{ moodEmoji[catchResult.mood] || '🍾' }}</span>
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
              <button class="or-btn next" @click="catchResult = null; catchBottle()">🍾 再捞一个</button>
            </div>
          </div>
          <div class="or-stats">
            <div class="or-stat">📤 已扔 <span class="ors-n">{{ thrown }}</span></div>
            <div class="or-stat">📥 已捞 <span class="ors-n">{{ fished }}</span></div>
            <div class="or-stat">🌊 海中 <span class="ors-n">{{ oceanBottles.length }}</span></div>
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
            <h3>🍾 装一个漂流瓶</h3>
            <button class="tm-close" @click="closeThrow">✕</button>
          </div>
          <div class="tm-body">
            <div class="field">
              <div class="field-label">现在的心情 <span class="fl-r">*</span></div>
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
              <div class="field-label">写一段话 <span class="fl-r">*</span></div>
              <textarea class="tm-textarea" v-model="tMsg" placeholder="写下此刻的心情..." maxlength="200"></textarea>
              <div class="tm-char">{{ tMsg.length }}/200</div>
            </div>
            <div class="field">
              <div class="field-label">推荐一本书 <span class="fl-r">*</span></div>
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
            <button class="tm-submit" :disabled="!canThrow" @click="doThrow">🍾 扔进大海</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Throw arc animation -->
    <div :class="['throw-arc', { show: throwArc }]">
      <div class="ta-bottle" ref="taBottle">🍾</div>
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
const hintText = ref('点击「换一批」抽取三张命运卡牌')
const hintActive = ref(false)
const justGotId = ref(null)
const fortuneInited = ref(false)

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
  // pick 12 random books from allBooks
  if (!allBooks.value.length) return
  const shuffled = [...allBooks.value].sort(() => Math.random() - 0.5)
  pool.forEach((f, i) => {
    f.book = shuffled[i % shuffled.length]
  })
}

let allBooks = ref([])

const FALLBACK_BOOKS = [
  { id: 1, title: '三体', author: '刘慈欣', price: 93.50, avgRating: 9.5 },
  { id: 2, title: '百年孤独', author: '马尔克斯', price: 45.60, avgRating: 9.4 },
  { id: 3, title: '活着', author: '余华', price: 28.00, avgRating: 9.6 },
  { id: 4, title: '小王子', author: '圣埃克苏佩里', price: 22.50, avgRating: 9.7 },
  { id: 5, title: '人类简史', author: '赫拉利', price: 52.80, avgRating: 9.1 },
  { id: 6, title: '1984', author: '奥威尔', price: 32.00, avgRating: 9.3 },
  { id: 7, title: '月亮与六便士', author: '毛姆', price: 32.00, avgRating: 9.0 },
  { id: 8, title: '围城', author: '钱钟书', price: 29.80, avgRating: 9.1 },
  { id: 9, title: '我与地坛', author: '史铁生', price: 36.00, avgRating: 9.6 },
  { id: 10, title: '边城', author: '沈从文', price: 24.00, avgRating: 9.0 },
  { id: 11, title: '额尔古纳河右岸', author: '迟子建', price: 35.00, avgRating: 9.2 },
  { id: 12, title: '认知觉醒', author: '周岭', price: 49.80, avgRating: 8.6 },
]

// Load books for the exploration features
async function loadBooks() {
  try {
    const res = await bookApi.getList({ pageNum: 1, pageSize: 50, status: 1 })
    allBooks.value = res.data?.records || FALLBACK_BOOKS
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
  if (flippedIdx.value === -1) return {}
  if (flippedIdx.value === idx) {
    const zoomed = zoomedIdx.value === idx
    const scale = zoomed ? 1.18 : 1
    return {
      transform: `translate(${flipDx.value}px, ${flipDy.value}px) rotateY(180deg) scale(${scale})`,
      zIndex: 10,
      transition: zoomed ? 'transform 0.35s cubic-bezier(0.25,0.8,0.25,1)' : 'transform 0.8s cubic-bezier(0.25,0.8,0.25,1)',
      transformStyle: 'preserve-3d'
    }
  }
  return { opacity: 0, transform: 'scale(0.82)', transition: 'opacity 0.35s ease, transform 0.35s ease' }
}

function pickCard(idx) {
  if (!deckReady.value) return
  deckReady.value = false

  const ct = document.querySelector('.three-cards')
  if (!ct) return
  const cards = Array.from(ct.querySelectorAll('.pick-card'))
  const cRect = ct.getBoundingClientRect()
  const cx = cRect.width / 2
  const cy = cRect.height / 2
  const ch = cards[idx]
  const r = ch.getBoundingClientRect()
  const ccx = r.left - cRect.left + r.width / 2
  const ccy = r.top - cRect.top + r.height / 2
  const dx = cx - ccx
  const dy = cy - ccy

  flipDx.value = dx
  flipDy.value = dy
  flippedIdx.value = idx

  setTimeout(() => {
    zoomedIdx.value = idx
  }, 800)

  setTimeout(() => {
    const f = threeF.value[idx]
    curFort.value = f
    collected.value = new Set([...collected.value, f.id])
    justGotId.value = f.id
    setTimeout(() => { justGotId.value = null }, 700)

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

/* ═══ OCEAN ═══ */
const oceanShips = ref([])
const oceanBottleItems = ref([])
let shipId = 0
let bottleId = 0
let spawnTimer = null

function spawnShip() {
  const dir = Math.random() > 0.5 ? 1 : -1
  const top = 15 + Math.random() * 18
  const speed = (16 + Math.random() * 8).toFixed(1)
  oceanShips.value.push({ id: ++shipId, dir, speed, top })
}

function spawnBottle() {
  const dir = Math.random() > 0.5 ? 1 : -1
  const top = 30 + Math.random() * 22
  const speed = (10 + Math.random() * 6).toFixed(1)
  oceanBottleItems.value.push({ id: ++bottleId, dir, speed, top })
}

function removeShip(id) {
  oceanShips.value = oceanShips.value.filter(s => s.id !== id)
}

function removeBottle(id) {
  oceanBottleItems.value = oceanBottleItems.value.filter(b => b.id !== id)
}

function refreshOceanScene() {
  oceanShips.value = []
  oceanBottleItems.value = []

  for (let i = 0; i < 2; i++) setTimeout(spawnShip, i * 1200)
  for (let i = 0; i < 3; i++) setTimeout(spawnBottle, i * 600)

  if (spawnTimer) clearInterval(spawnTimer)
  spawnTimer = setInterval(() => {
    if (Math.random() > 0.4) spawnShip()
    if (Math.random() > 0.2) spawnBottle()
  }, 2500)
}

const moods = [
  { key: '开心', icon: '😊', label: '开心' },
  { key: '平静', icon: '🍃', label: '平静' },
  { key: '孤独', icon: '🌙', label: '孤独' },
  { key: '好奇', icon: '🔍', label: '好奇' },
  { key: '焦虑', icon: '😰', label: '焦虑' },
  { key: '迷茫', icon: '🌫️', label: '迷茫' }
]
const moodEmoji = { 开心: '😊', 平静: '🍃', 孤独: '🌙', 好奇: '🔍', 焦虑: '😰', 迷茫: '🌫️' }

const oceanBottles = ref([
  { mood: '开心', msg: '今天阳光特别好，想读点轻快的故事', time: '3小时前' },
  { mood: '孤独', msg: '一个人的夜晚，需要一本温暖的书', time: '昨天' },
  { mood: '好奇', msg: '最近对宇宙特别着迷', time: '5小时前' },
  { mood: '迷茫', msg: '不知道未来该怎么办', time: '2天前' },
  { mood: '平静', msg: '泡了杯茶，想找本慢慢读的书', time: '8小时前' },
  { mood: '焦虑', msg: '工作太累了，想逃进另一个世界', time: '1天前' },
  { mood: '开心', msg: '刚升职！想奖励自己一本好书', time: '4小时前' },
  { mood: '孤独', msg: '搬到新城市，谁也不认识...', time: '3天前' },
  { mood: '好奇', msg: '有人推荐过明朝那些事儿？', time: '6小时前' },
  { mood: '平静', msg: '午后阳光下，想读一本诗集', time: '昨天' },
  { mood: '焦虑', msg: '考研压力大，想找点力量', time: '12小时前' },
  { mood: '迷茫', msg: '30岁了还不知道自己想要什么', time: '2天前' }
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
  const moodRecs = { 开心: [], 孤独: [], 好奇: [], 迷茫: [], 平静: [], 焦虑: [] }
  const rec = allBooks.value.length ? allBooks.value[Math.floor(Math.random() * allBooks.value.length)] : null
  if (rec) {
    throwResult.value = { mood: tMood.value, book: rec }
  }
  toast('🍾 漂流瓶已扔进大海')
  confetti(window.innerWidth / 2, window.innerHeight / 2)
}

function catchBottle() {
  if (!oceanBottles.value.length) {
    toast('海面上暂时没有漂流瓶...')
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
    const sc = document.querySelector('.ocean-scene')
    if (sc) {
      const sr = sc.getBoundingClientRect()
      sparkle(sr.left + sr.width / 2, sr.top + sr.height * 0.45)
    }
    confetti(window.innerWidth / 2, window.innerHeight / 2)
  }, 1700)
}

/* ═══ COMMUNITY ═══ */
const communityPosts = ref([
  { id: 1, username: '林小雅', content: '读完《瓦尔登湖》，决定开始极简生活', image: 'https://picsum.photos/seed/walden/400/500', likes: 128, liked: false, time: '2小时前', book: FALLBACK_BOOKS[0] },
  { id: 2, username: '书虫阿东', content: '深夜推理时间，《白夜行》太绝了', image: 'https://picsum.photos/seed/whiteNight/400/300', likes: 89, liked: false, time: '3小时前', book: FALLBACK_BOOKS[2] },
  { id: 3, username: '雨落江南', content: '诗集读完了，整个人都安静下来', image: 'https://picsum.photos/seed/poetry/400/600', likes: 256, liked: false, time: '5小时前', book: FALLBACK_BOOKS[3] },
  { id: 4, username: '北漂青年', content: '打工人的日常，地铁上读完半本《活着》', image: 'https://picsum.photos/seed/toLive/400/350', likes: 67, liked: false, time: '昨天', book: FALLBACK_BOOKS[4] },
  { id: 5, username: '文艺大叔', content: '三体让我对宇宙充满敬畏', image: 'https://picsum.photos/seed/threeBody/400/450', likes: 312, liked: false, time: '1天前', book: FALLBACK_BOOKS[5] },
  { id: 6, username: '小红迷', content: '月下看《围城》，方渐鸿的无奈', image: 'https://picsum.photos/seed/fortress/400/400', likes: 178, liked: false, time: '2天前', book: FALLBACK_BOOKS[6] },
  { id: 7, username: '阅读者', content: '人类简史刷新了我的认知边界', image: 'https://picsum.photos/seed/sapiens/400/550', likes: 234, liked: false, time: '2天前', book: FALLBACK_BOOKS[7] },
  { id: 8, username: '书语者', content: '我与地坛的文字让我泪流满面', image: 'https://picsum.photos/seed/ditian/400/320', likes: 445, liked: false, time: '3天前', book: FALLBACK_BOOKS[8] },
  { id: 9, username: '墨染白', content: '边城的翠翠，那条溪水清澈如她的眼眸', image: 'https://picsum.photos/seed/biancheng/400/480', likes: 189, liked: false, time: '3天前', book: FALLBACK_BOOKS[9] },
  { id: 10, username: '星河书社', content: '认知觉醒，让我开始真正思考', image: 'https://picsum.photos/seed/awaken/400/380', likes: 156, liked: false, time: '4天前', book: FALLBACK_BOOKS[10] },
  { id: 11, username: '豆瓣用户', content: '百年孤独里的魔幻现实让人着迷', image: 'https://picsum.photos/seed/century/400/520', likes: 278, liked: false, time: '4天前', book: FALLBACK_BOOKS[1] },
  { id: 12, username: '读书笔记', content: '小王子的纯真，是我们丢失的美好', image: 'https://picsum.photos/seed/principle/400/280', likes: 367, liked: false, time: '5天前', book: FALLBACK_BOOKS[11] },
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

function doPublish() {
  if (!canPublish.value) return
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const newCommunityPost = {
    id: ++postIdCounter,
    username: userInfo.username || '我',
    content: newPost.value.content.trim(),
    image: newPost.value.imagePreview,
    likes: 0,
    liked: false,
    time: '刚刚',
    book: publishSelectedBook.value,
  }
  communityPosts.value.unshift(newCommunityPost)
  closePublishModal()
  toast('📖 帖子发布成功！')
  confetti(window.innerWidth / 2, window.innerHeight / 2)
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
    await cartApi.add({ bookId: book.id, quantity: 1 })
    await cartStore.getCartList()
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

.three-cards {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 28px;
  height: 340px;
  width: 100%;
  position: relative;
  perspective: 1000px;
}
.pick-card {
  width: 200px;
  height: 300px;
  cursor: pointer;
  position: relative;
  flex-shrink: 0;
  transform-style: preserve-3d;
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
.card-hint.active { color: rgba(232,212,138,0.5); }

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

/* ═══ OCEAN ═══ */
.ocean-scene {
  position: relative;
  width: 100%;
  height: 440px;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(180deg, #5a8eaf 0%, #7aaec4 8%, #a0c8d8 16%, #d8c4a8 22%, #e8b888 26%, #5a9aaa 32%, #3a7a8a 42%, #2a6070 55%, #1a4858 70%, #103040 85%, #081e2c 100%);
  animation: skyBreathe 12s ease-in-out infinite;
}
@keyframes skyBreathe { 0%,100% { filter: brightness(1); } 50% { filter: brightness(1.04); } }
.os-sun {
  position: absolute;
  top: 15%;
  right: 16%;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: radial-gradient(circle at 40% 40%, #fff8e0, #f0c860);
  box-shadow: 0 0 40px rgba(240,200,96,0.3), 0 0 80px rgba(240,200,96,0.1);
}
.os-sun-glow {
  position: absolute;
  top: 11%;
  right: 12%;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(240,200,96,0.06), transparent 70%);
  animation: sunGlowPulse 4s ease-in-out infinite;
}
@keyframes sunGlowPulse { 0%,100% { transform: scale(1); opacity: 0.8; } 50% { transform: scale(1.2); opacity: 1; } }
.os-horizon {
  position: absolute;
  top: 28%;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent 8%, rgba(255,240,210,0.06) 30%, rgba(255,240,210,0.1) 50%, rgba(255,240,210,0.06) 70%, transparent 92%);
}
.os-reflection {
  position: absolute;
  top: 28%;
  left: 0;
  right: 0;
  height: 80px;
  background: linear-gradient(180deg, rgba(240,200,96,0.04), transparent);
  pointer-events: none;
  animation: refPulse 5s ease-in-out infinite;
}
@keyframes refPulse { 0%,100% { opacity: 0.5; } 50% { opacity: 1; } }
.os-shimmer {
  position: absolute;
  top: 32%;
  left: 0;
  right: 0;
  height: 100px;
  background: repeating-linear-gradient(90deg, transparent, transparent 50px, rgba(255,255,255,0.015) 50px, rgba(255,255,255,0.015) 52px);
  animation: osSh 5s linear infinite;
  pointer-events: none;
  mask-image: linear-gradient(180deg, transparent 10%, rgba(0,0,0,0.15) 40%, rgba(0,0,0,0.12) 60%, transparent 90%);
  -webkit-mask-image: linear-gradient(180deg, transparent 10%, rgba(0,0,0,0.15) 40%, rgba(0,0,0,0.12) 60%, transparent 90%);
}
@keyframes osSh { 0% { transform: translateX(0); } 100% { transform: translateX(52px); } }

/* Ocean atmosphere */
.os-mist {
  position: absolute;
  bottom: 22%;
  border-radius: 50%;
  background: radial-gradient(ellipse, rgba(255,255,255,0.06) 0%, transparent 70%);
  pointer-events: none;
  z-index: 2;
  animation: mistRise var(--d, 14s) ease-in-out var(--dd, 0s) infinite;
}
.m1 { width: 220px; height: 40px; left: 5%; --d: 16s; --dd: 0s; }
.m2 { width: 180px; height: 30px; left: 40%; --d: 13s; --dd: 4s; }
.m3 { width: 260px; height: 45px; left: 65%; --d: 18s; --dd: 8s; }
@keyframes mistRise {
  0% { transform: translateY(0) scaleX(1); opacity: 0; }
  15% { opacity: 0.5; }
  50% { transform: translateY(-40px) scaleX(1.15); opacity: 0.3; }
  85% { opacity: 0.15; }
  100% { transform: translateY(-90px) scaleX(1.3); opacity: 0; }
}

.os-rays {
  position: absolute;
  top: 14%;
  right: 14%;
  width: 240px;
  height: 340px;
  background: linear-gradient(135deg, rgba(240,200,96,0.05) 0%, transparent 60%);
  transform-origin: top right;
  pointer-events: none;
  z-index: 1;
  animation: rayPulse 7s ease-in-out infinite;
}
@keyframes rayPulse {
  0%,100% { opacity: 0.3; transform: rotate(-3deg) scaleX(1); }
  50% { opacity: 0.7; transform: rotate(4deg) scaleX(1.08); }
}

.os-gulls {
  position: absolute;
  top: 8%;
  left: 0;
  right: 0;
  height: 0;
  z-index: 3;
  pointer-events: none;
}
.gull {
  position: absolute;
  font-size: 0.7rem;
  opacity: 0.25;
  animation: gullFly var(--gd, 18s) linear var(--gdd, 0s) infinite;
  filter: drop-shadow(0 1px 2px rgba(0,0,0,0.08));
}
.g1 { --gd: 22s; --gdd: 0s; top: -8px; }
.g2 { --gd: 18s; --gdd: 6s; top: 2px; font-size: 0.55rem; opacity: 0.15; }
.g3 { --gd: 26s; --gdd: 13s; top: -4px; font-size: 0.6rem; opacity: 0.2; }
@keyframes gullFly {
  0% { transform: translateX(-60px) scaleX(1); }
  48% { transform: translateX(1400px) scaleX(1); }
  50% { transform: translateX(1400px) scaleX(-1); }
  98% { transform: translateX(-60px) scaleX(-1); }
  100% { transform: translateX(-60px) scaleX(1); }
}

.os-boat {
  position: absolute;
  font-size: 2rem;
  z-index: 3;
  filter: drop-shadow(0 2px 8px rgba(0,0,0,0.18));
  will-change: transform, left;
}
.os-boat.sail-r { animation: sailRight var(--sd, 20s) linear forwards; }
.os-boat.sail-l { animation: sailLeft var(--sd, 20s) linear forwards; }
.os-boat::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 4px;
  background: radial-gradient(ellipse, rgba(0,0,0,0.08) 0%, transparent 70%);
  border-radius: 50%;
  animation: boatShadow 3s ease-in-out infinite;
}
@keyframes boatShadow { 0%,100% { transform: translateX(-50%) scaleX(1); opacity: 0.3; } 50% { transform: translateX(-50%) scaleX(1.4); opacity: 0.1; } }
@keyframes sailRight {
  0% { left: -80px; transform: translateY(0) rotate(-1deg); }
  25% { transform: translateY(-4px) rotate(1deg); }
  50% { transform: translateY(2px) rotate(0deg); }
  75% { transform: translateY(-3px) rotate(-0.5deg); }
  100% { left: calc(100% + 80px); transform: translateY(0) rotate(0.5deg); }
}
@keyframes sailLeft {
  0% { left: calc(100% + 80px); transform: translateY(0) rotate(1deg); }
  25% { transform: translateY(-4px) rotate(-1deg); }
  50% { transform: translateY(2px) rotate(0deg); }
  75% { transform: translateY(-3px) rotate(0.5deg); }
  100% { left: -80px; transform: translateY(0) rotate(-0.5deg); }
}
@keyframes bottleSwimRight {
  0% { left: -50px; transform: translateY(0) rotate(-3deg); }
  25% { transform: translateY(-6px) rotate(2deg); }
  50% { transform: translateY(3px) rotate(-1deg); }
  75% { transform: translateY(-4px) rotate(2deg); }
  100% { left: calc(100% + 50px); transform: translateY(0) rotate(-2deg); }
}
@keyframes bottleSwimLeft {
  0% { left: calc(100% + 50px); transform: translateY(0) rotate(3deg); }
  25% { transform: translateY(-6px) rotate(-2deg); }
  50% { transform: translateY(3px) rotate(1deg); }
  75% { transform: translateY(-4px) rotate(-2deg); }
  100% { left: -50px; transform: translateY(0) rotate(2deg); }
}
.os-bottle.swim-r { animation: bottleSwimRight var(--sd, 13s) linear forwards; }
.os-bottle.swim-l { animation: bottleSwimLeft var(--sd, 13s) linear forwards; }
.os-bottle {
  position: absolute;
  font-size: 1.5rem;
  z-index: 3;
  cursor: pointer;
  filter: drop-shadow(0 1px 3px rgba(0,0,0,0.12));
}
.os-bubble {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle at 35% 35%, rgba(255,255,255,0.08), transparent);
  pointer-events: none;
  z-index: 2;
  animation: bubbleRise var(--bd, 6s) ease-in-out var(--bdd, 0s) infinite;
}
.b1 { width: 6px; height: 6px; bottom: 24%; left: 15%; --bd: 7s; --bdd: 0s; }
.b2 { width: 4px; height: 4px; bottom: 28%; left: 55%; --bd: 9s; --bdd: 2s; }
.b3 { width: 5px; height: 5px; bottom: 22%; left: 75%; --bd: 6s; --bdd: 4s; }
.b4 { width: 3px; height: 3px; bottom: 30%; left: 35%; --bd: 8s; --bdd: 1s; }
@keyframes bubbleRise {
  0% { transform: translateY(0) translateX(0) scale(0.5); opacity: 0; }
  15% { opacity: 0.5; }
  50% { transform: translateY(-50px) translateX(10px) scale(1); opacity: 0.3; }
  100% { transform: translateY(-110px) translateX(-8px) scale(0.3); opacity: 0; }
}
.os-waves { position: absolute; bottom: 0; left: 0; right: 0; height: 110px; z-index: 4; overflow: hidden; }
.os-wave {
  position: absolute;
  bottom: 0;
  left: -5%;
  width: 110%;
  height: 100%;
  animation: waveDrift var(--wd, 8s) ease-in-out var(--wdd, 0s) infinite;
}
.os-wave svg { width: 100%; height: 100%; display: block; }
@keyframes waveDrift {
  0%,100% { transform: translateX(-3%) scaleY(1); }
  50% { transform: translateX(3%) scaleY(1.03); }
}
.os-actions {
  position: absolute;
  bottom: 32px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 14px;
  z-index: 10;
}
.os-btn {
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
.os-btn.throw { background: rgba(255,255,255,0.12); color: rgba(255,255,255,0.9); border: 1px solid rgba(255,255,255,0.1); }
.os-btn.throw:hover { background: rgba(255,255,255,0.2); transform: translateY(-2px); }
.os-btn.catch { background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted)); color: var(--color-primary-abyss); box-shadow: 0 4px 16px rgba(192,154,75,0.3); }
.os-btn.catch:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(192,154,75,0.4); }
.os-catch {
  position: absolute;
  inset: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}
.oca-bottle {
  font-size: 2.8rem;
  position: relative;
  z-index: 2;
  animation: bottleRise 1.1s cubic-bezier(0.2,0.8,0.3,1) 0.3s both;
  filter: drop-shadow(0 4px 12px rgba(192,154,75,0.15));
}
@keyframes bottleRise {
  0% { transform: translateY(100px) rotate(-8deg); opacity: 0; }
  25% { transform: translateY(20px) rotate(2deg); opacity: 0.6; }
  50% { transform: translateY(-8px) rotate(-1deg); opacity: 1; }
  75% { transform: translateY(4px) rotate(2deg); opacity: 1; }
  100% { transform: translateY(-30px) rotate(4deg); opacity: 1; }
}
.oca-drip {
  position: absolute;
  width: 3px;
  height: 8px;
  border-radius: 0 0 3px 3px;
  background: linear-gradient(180deg, transparent, rgba(255,255,255,0.2));
  opacity: 0;
  z-index: 1;
  animation: dripFall var(--dd, 0.7s) ease-in var(--ddd, 0s) forwards;
}
.d1 { --dd: 0.7s; --ddd: 0.1s; left: calc(50% - 18px); top: 48%; }
.d2 { --dd: 0.9s; --ddd: 0.35s; left: calc(50% + 12px); top: 45%; }
.d3 { --dd: 0.6s; --ddd: 0.55s; left: calc(50% - 6px); top: 50%; }
@keyframes dripFall {
  0% { transform: translateY(0) scaleY(1); opacity: 0; }
  10% { opacity: 0.7; }
  100% { transform: translateY(80px) scaleY(0.5); opacity: 0; }
}
.oca-ripple {
  position: absolute;
  width: 60px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.15);
  border-radius: 50%;
  animation: ripEx 0.8s ease-out 0.7s both;
}
@keyframes ripEx { 0% { transform: scale(0); opacity: 0.6; } 100% { transform: scale(3); opacity: 0; } }

/* Throw arc */
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
  filter: drop-shadow(0 2px 6px rgba(0,0,0,0.2));
  animation: taFly 1.2s cubic-bezier(0.2,0.8,0.3,1) forwards;
}
@keyframes taFly {
  0% { bottom: 22%; left: 48%; transform: translateX(-50%) rotate(0) scale(1); opacity: 1; }
  45% { bottom: 58%; left: 52%; transform: translateX(-50%) rotate(-12deg) scale(1.1); opacity: 1; }
  100% { bottom: 36%; left: 56%; transform: translateX(-50%) rotate(20deg) scale(0.3); opacity: 0; }
}
.ta-splash {
  position: absolute;
  bottom: 32%;
  left: 52%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: radial-gradient(ellipse, rgba(255,255,255,0.2), transparent);
  transform: translateX(-50%);
  transition: all 0.5s ease-out;
}
.ta-splash.burst { width: 100px; height: 28px; opacity: 0; }
.ta-splash .tas-rings {
  position: absolute;
  inset: -10px;
  border: 2px solid rgba(255,255,255,0.08);
  border-radius: 50%;
  opacity: 0;
  transition: none;
}
.ta-splash.burst .tas-rings {
  animation: splashRing 0.7s ease-out 0.15s forwards;
}
@keyframes splashRing {
  0% { transform: scale(0.2); opacity: 0.6; }
  100% { transform: scale(2.5); opacity: 0; }
}
.tas-droplet {
  position: absolute;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(255,255,255,0.25);
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
  10% { opacity: 0.8; transform: translate(0, 0) scale(1); }
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
  background: linear-gradient(90deg, var(--color-accent), #4A6E8A, var(--color-accent));
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
  .three-cards { gap: 16px; }
  .pick-card { width: 170px; height: 260px; }
  .pc-back-inner { width: 140px; height: 220px; }
}
@media (max-width: 768px) {
  .ph-inner { padding: 28px 20px; flex-direction: column; align-items: flex-start; }
  .ph-left h1 { font-size: 1.4rem; }
  .tab-bar { padding: 14px 20px; overflow-x: auto; }
  .section-wrap { padding: 24px 20px 40px; }
  .fortune-stage { padding: 40px 12px 36px; }
  .three-cards { height: 280px; gap: 10px; }
  .pick-card { width: 140px; height: 230px; }
  .pc-back-inner { width: 118px; height: 198px; }
  .pc-symbol { font-size: 1.4rem; }
  .pc-title { font-size: 0.85rem; }
  .pc-front { padding: 10px; }
  .pc-front-icon { font-size: 1.2rem; }
  .pc-front-fortune { font-size: 0.72rem; }
  .ocean-scene { height: 360px; }
  .os-actions { flex-direction: column; width: calc(100% - 32px); }
  .os-btn { width: 100%; text-align: center; }
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
