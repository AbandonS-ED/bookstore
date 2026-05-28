<template>
  <div ref="pageEl" class="about-page">
    <!-- ═══ 1. HERO BRAND STATEMENT ═══ -->
    <section class="hero-brand">
      <div class="hero-brand-content">
        <div class="hb-quote">—— 书斋 ——</div>
        <h1>在快时代，做一家<em>慢书店</em></h1>
        <p class="hb-sub">我们相信，每一本书都值得被认真对待。<br>每一位读者，都值得被温柔以待。</p>
        <div class="hb-divider"></div>
        <div class="hb-signature">— 书斋，2018 年创立于北京</div>
      </div>
    </section>

    <!-- ═══ 2. BRAND STORY ═══ -->
    <section class="story-section">
      <div class="story-image reveal-left">
        <div class="story-img-frame">
          <div class="story-img-inner">
            <div class="si-book">📖</div>
            <div class="si-text">书 斋</div>
            <div class="si-year">EST. 2018 · BEIJING</div>
          </div>
        </div>
        <div class="story-img-caption">📷 创始团队在胡同里的第一间办公室</div>
      </div>
      <div class="story-text reveal-right">
        <h2>从一间胡同小屋开始</h2>
        <span class="story-year">2018 · 秋 · 北京</span>
        <p>2018 年的秋天，三位爱书人在北京东四胡同里的一间 20 平米小屋里，决定做一件事：开一家不一样的书店。</p>
        <p>那时候，算法推荐越来越精准，热门榜单千篇一律，但真正打动人心的好书，反而被淹没在信息洪流里。我们发现，读者与一本好书之间的距离，从来没有因为技术进步而变近。</p>
        <div class="highlight">
          「我们不追求最全，而是追求最对。<br>
          每一本上架的书，都经过编辑团队的阅读和讨论。」
        </div>
        <p>于是书斋诞生了。我们坚持人工选书，坚持写真实的书评，坚持在每一本书的背后，放上一个活生生的推荐理由。</p>
        <p>七年过去，从三个人到三十人，从 500 本书到 128,000 本书，从胡同小屋到服务全国 56,000 位读者。变的是规模，不变的是初心。</p>
      </div>
    </section>

    <!-- ═══ 3. CORE VALUES ═══ -->
    <section class="values-bg">
      <div class="section-center">
        <h2 class="section-title reveal">我们相信的事</h2>
        <p class="section-desc center reveal" style="transition-delay:.1s;">四个简单的信念，支撑着我们走过的每一天</p>
        <div class="values-grid">
          <div v-for="(v, i) in values" :key="v.title" class="value-card reveal" :style="{ transitionDelay: (0.1 * (i + 1) + 0.05) + 's' }">
            <div class="value-icon">{{ v.icon }}</div>
            <h3>{{ v.title }}</h3>
            <p>{{ v.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ═══ 4. NUMBERS ═══ -->
    <section class="numbers-section">
      <h2 class="section-title left reveal">用数字说话</h2>
      <p class="section-desc reveal" style="transition-delay:.1s;">七年时间，书斋用数据证明了慢的力量</p>
      <div ref="numbersGridEl" class="numbers-grid">
        <div v-for="(n, i) in numbers" :key="n.label" class="number-card reveal" :style="{ transitionDelay: (0.05 * (i + 2)) + 's' }">
          <div class="number-value" :data-target="n.target" :data-suffix="n.suffix || ''" :data-decimal="n.decimal || 0">{{ n.prefix || '' }}0{{ n.suffix || '' }}</div>
          <div class="number-unit">{{ n.unit }}</div>
          <div class="number-label">{{ n.label }}</div>
        </div>
      </div>
    </section>

    <!-- ═══ 5. TEAM ═══ -->
    <section>
      <div class="section">
        <h2 class="section-title left reveal">我们是一群相信文字力量的人</h2>
        <p class="section-desc reveal" style="transition-delay:.1s;">每一位团队成员，都是深度阅读的践行者</p>
        <div class="team-grid">
          <div v-for="(t, i) in team" :key="t.name" class="team-card reveal" :style="{ transitionDelay: ((i % 3) * 0.1 + 0.1 + Math.floor(i / 3) * 0.05) + 's' }">
            <div class="team-avatar" :style="t.avatarStyle">{{ t.initial }}</div>
            <div class="tc-name">{{ t.name }}</div>
            <div class="tc-role">{{ t.role }}</div>
            <div class="tc-bio">{{ t.bio }}</div>
            <div class="tc-fav">📖 最爱的书：<strong>{{ t.favBook }}</strong></div>
          </div>
        </div>
      </div>
    </section>

    <!-- ═══ 6. TIMELINE ═══ -->
    <section class="timeline-section">
      <div class="section-center">
        <h2 class="section-title reveal">我们的故事</h2>
        <p class="section-desc center reveal" style="transition-delay:.1s;">七年时间，一步一步走来</p>
        <div class="timeline-wrap">
          <div v-for="(tl, i) in timeline" :key="tl.year" class="tl-item reveal" :style="{ transitionDelay: (0.08 * (i + 1)) + 's' }">
            <div class="tl-dot"></div>
            <div class="tl-year">{{ tl.year }}</div>
            <div class="tl-title">{{ tl.title }}</div>
            <div class="tl-desc">
              {{ tl.desc }}
              <span v-if="tl.stat" class="tl-stat">✦ {{ tl.stat }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ═══ 7. CONTACT ═══ -->
    <section class="contact-section">
      <h2 class="section-title left reveal">联系我们</h2>
      <p class="section-desc reveal" style="transition-delay:.1s;">无论是合作、建议还是只是想聊一聊最近读的好书，我们都期待你的来信</p>
      <div class="contact-grid">
        <div class="contact-form-wrap reveal-left" style="transition-delay:.15s;">
          <h3>发送消息</h3>
          <form v-if="!formSubmitted" @submit.prevent="handleSubmit">
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">姓名 <span class="req">*</span></label>
                <input class="form-input" :class="{ error: formErrors.name }" v-model="form.name" placeholder="你的名字" @blur="validateField('name')">
                <div class="form-error" :class="{ show: formErrors.name }">请输入姓名</div>
              </div>
              <div class="form-group">
                <label class="form-label">邮箱 <span class="req">*</span></label>
                <input class="form-input" :class="{ error: formErrors.email }" v-model="form.email" type="email" placeholder="your@email.com" @blur="validateField('email')">
                <div class="form-error" :class="{ show: formErrors.email }">{{ formErrors.email === 'format' ? '请输入有效的邮箱地址' : '请输入邮箱' }}</div>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">主题 <span class="req">*</span></label>
              <select class="form-input" :class="{ error: formErrors.subject }" v-model="form.subject" @blur="validateField('subject')">
                <option value="">请选择主题</option>
                <option>合作咨询</option>
                <option>建议反馈</option>
                <option>售后问题</option>
                <option>媒体合作</option>
                <option>加入我们</option>
                <option>其他</option>
              </select>
              <div class="form-error" :class="{ show: formErrors.subject }">请选择主题</div>
            </div>
            <div class="form-group">
              <label class="form-label">内容 <span class="req">*</span></label>
              <textarea class="form-input" :class="{ error: formErrors.content }" v-model="form.content" placeholder="请写下你想说的..." @input="validateField('content')"></textarea>
              <div class="form-error" :class="{ show: formErrors.content }">内容至少需要 20 个字符</div>
            </div>
            <button type="submit" class="btn-submit">发送消息</button>
          </form>
          <div v-else class="form-success show">
            <div class="fs-icon">✉️</div>
            <h4>消息已发送</h4>
            <p>感谢你的来信，我们会在 24 小时内回复。</p>
            <button class="btn-back" @click="resetForm">再写一封</button>
          </div>
        </div>
        <div class="contact-info-wrap reveal-right" style="transition-delay:.25s;">
          <h3>联系方式</h3>
          <div class="ci-list">
            <div v-for="c in contactInfo" :key="c.label" class="ci-item">
              <div class="ci-icon">{{ c.icon }}</div>
              <div>
                <div class="ci-label">{{ c.label }}</div>
                <div class="ci-value">{{ c.value }}</div>
                <div v-if="c.sub" class="ci-sub">{{ c.sub }}</div>
              </div>
            </div>
          </div>
          <div class="ci-social">
            <div class="ci-social-title">关注我们</div>
            <div class="social-links">
              <div v-for="s in socialLinks" :key="s.title" class="social-link" :title="s.title">{{ s.icon }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { reviewApi } from '@/api/review'

const pageEl = ref(null)
const numbersGridEl = ref(null)
let countDone = false

const values = [
  { icon: '📖', title: '人工选书', desc: '每一本上架的书都经过编辑团队的亲自阅读和讨论，不是算法决定什么值得读，而是真正爱书的人。' },
  { icon: '🔍', title: '深度阅读', desc: '我们不只是卖书，更陪伴阅读。提供专业的书评、作者导读和主题书单，帮你找到那本对的书。' },
  { icon: '🤝', title: '读者社区', desc: '阅读从来不是孤独的事。线上讨论、线下读书会、阅读打卡——让每一本书连接一群人。' },
  { icon: '📦', title: '品质服务', desc: '每本书用无酸纸包裹寄出，支持次日达配送、无忧退换。我们对待包裹的态度，和对待书一样认真。' }
]

const numbers = ref([
  { target: 128000, suffix: '+', unit: '本', label: '在售图书' },
  { target: 56000, suffix: '+', unit: '位', label: '注册读者' },
  { target: 2800, suffix: '+', unit: '家', label: '合作出版社' },
  { target: 365, suffix: '', unit: '天', label: '全年无休运营' },
  { target: 98.6, suffix: '%', unit: '', label: '好评率', decimal: 1 },
  { target: 4.8, suffix: '', unit: '分', label: '平均评分', decimal: 1 },
  { target: 24, suffix: 'h', unit: '', label: '客服响应' },
  { target: 7, suffix: ' 年', unit: '', label: '品牌历程' }
])

const team = [
  { initial: '陈', name: '陈拾光', role: '创始人 · CEO', bio: '10 年出版行业经验，曾任某知名出版社文学主编。坚信一本好书可以改变一个人的一生。', favBook: '《百年孤独》', avatarStyle: { background: 'var(--color-accent-glow)', color: 'var(--color-accent-muted)' } },
  { initial: '林', name: '林书语', role: '主编 · 内容总监', bio: '8 年文学编辑经验，负责所有书籍的筛选和书评审核。文字是她的信仰，阅读是她的呼吸。', favBook: '《红楼梦》', avatarStyle: { background: 'var(--color-blue-bg)', color: 'var(--color-blue)' } },
  { initial: '张', name: '张墨白', role: '技术负责人 · CTO', bio: '6 年电商开发经验，负责整个技术架构。用代码为好书搭建一座通往读者的桥。', favBook: '《三体》', avatarStyle: { background: 'var(--color-green-bg)', color: 'var(--color-green)' } },
  { initial: '王', name: '王小灯', role: '选书编辑', bio: '专注社科人文领域，每年阅读 200+ 本书。她的书单是读者最信赖的推荐来源。', favBook: '《人类简史》', avatarStyle: { background: 'var(--color-red-bg)', color: 'var(--color-red)' } },
  { initial: '李', name: '李页页', role: '视觉设计总监', bio: '8 年品牌设计经验，书斋整套视觉系统的创造者。相信好的设计让阅读更有仪式感。', favBook: '《设计中的设计》', avatarStyle: { background: 'rgba(74,53,38,0.06)', color: 'var(--color-primary-mid)' } },
  { initial: '赵', name: '赵卷卷', role: '社区运营负责人', bio: '5 年社区运营经验，负责线上读书会和线下活动。致力于让阅读成为一种社交方式。', favBook: '《小王子》', avatarStyle: { background: 'var(--color-accent-glow)', color: 'var(--color-accent-muted)' } }
]

const timeline = [
  { year: '2018', title: '书斋诞生', desc: '三位爱书人在北京胡同里创立书斋，开始手工选书、亲自打包、骑车送书的日子。' },
  { year: '2019', title: '线上商城上线', desc: '网站正式上线运营，首批上架 3,000 本精选好书。用户突破 5,000 人。' },
  { year: '2020', title: '「编辑推荐」专栏启动', desc: '推出每周编辑推荐专栏，合作出版社突破 500 家。开始被读者称为「最值得信赖的选书人」。' },
  { year: '2022', title: '读者突破三万', desc: '注册用户突破 30,000 人。启动线下读书会计划，在全国 12 个城市举办了 48 场读书活动。' },
  { year: '2024', title: '十万册图书里程碑', desc: '在售图书突破 100,000 册，推出电子书和有声书服务，合作出版社超过 2,000 家。' },
  { year: '2026', title: '书斋全新改版', desc: '全新网站上线，128,000+ 图书，56,000+ 读者。', stat: '✦ 故事还在继续' }
]

const contactInfo = [
  { icon: '📍', label: '地址', value: '北京市朝阳区建国路 88 号', sub: 'SOHO 现代城 A 座 1208 室' },
  { icon: '📞', label: '电话', value: '400-888-6688', sub: '工作日 9:00 - 21:00' },
  { icon: '✉️', label: '邮箱', value: 'hi@shiguangbooks.com', sub: '合作 / 建议 / 投稿' },
  { icon: '🕘', label: '工作时间', value: '9:00 - 21:00，全年无休', sub: '客服响应时间 24 小时内' }
]

const socialLinks = [
  { icon: '💬', title: '微信公众号' },
  { icon: '📢', title: '微博' },
  { icon: '📕', title: '小红书' },
  { icon: '🎵', title: '抖音' },
  { icon: '📺', title: 'B站' }
]

const form = reactive({ name: '', email: '', subject: '', content: '' })
const formErrors = reactive({ name: false, email: false, subject: false, content: false })
const formSubmitted = ref(false)

function validateField(field) {
  if (field === 'name') { formErrors.name = !form.name.trim() }
  if (field === 'email') {
    const v = form.email.trim()
    formErrors.email = !v || !v.includes('@')
  }
  if (field === 'subject') { formErrors.subject = !form.subject }
  if (field === 'content') {
    const v = form.content.trim()
    formErrors.content = v.length > 0 && v.length < 20
  }
}

function handleSubmit() {
  validateField('name')
  validateField('email')
  validateField('subject')
  validateField('content')
  if (formErrors.name || formErrors.email || formErrors.subject || formErrors.content) return
  formSubmitted.value = true
}

function resetForm() {
  form.name = ''; form.email = ''; form.subject = ''; form.content = ''
  formErrors.name = false; formErrors.email = false; formErrors.subject = false; formErrors.content = false
  formSubmitted.value = false
}

function initScrollReveal() {
  const el = pageEl.value
  if (!el) return
  const items = el.querySelectorAll('.reveal, .reveal-left, .reveal-right')
  if (!items.length) return
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible')
        observer.unobserve(entry.target)
      }
    })
  }, { threshold: 0.15, rootMargin: '0px 0px -40px 0px' })
  items.forEach(el => observer.observe(el))
}

function initCountUp() {
  const grid = numbersGridEl.value
  if (!grid) return
  const items = grid.querySelectorAll('.number-value[data-target]')
  if (!items.length) return
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting && !countDone) {
        countDone = true
        items.forEach(el => {
          const target = parseFloat(el.dataset.target)
          const suffix = el.dataset.suffix || ''
          const decimal = parseInt(el.dataset.decimal) || 0
          const duration = 1800
          const start = performance.now()
          function update(now) {
            const elapsed = now - start
            const progress = Math.min(elapsed / duration, 1)
            const eased = 1 - Math.pow(2, -10 * progress)
            const current = target * eased
            el.textContent = (decimal > 0 ? current.toFixed(decimal) : Math.floor(current).toLocaleString()) + suffix
            if (progress < 1) requestAnimationFrame(update)
            else { el.textContent = (decimal > 0 ? target.toFixed(decimal) : target.toLocaleString()) + suffix }
          }
          requestAnimationFrame(update)
        })
        observer.unobserve(entry.target)
      }
    })
  }, { threshold: 0.3 })
  observer.observe(grid)
}

onMounted(async () => {
  try {
    const statsRes = await reviewApi.getStats()
    if (statsRes.code === 200 && statsRes.data) {
      numbers.value[0].target = statsRes.data.totalBooks
      numbers.value[1].target = statsRes.data.totalUsers
      const pct = parseFloat((statsRes.data.goodRate * 100).toFixed(1))
      numbers.value[4].target = pct
    }
  } catch (error) {
    console.error('Failed to load review stats:', error)
  }
  initScrollReveal()
  initCountUp()
})
</script>

<style scoped>
.about-page {
  background: var(--color-bg, #EDE6D6);
  min-height: calc(100vh - var(--header-height, 68px));
}

/* reveal animations */
.reveal { opacity: 0; transform: translateY(28px); transition: all .7s cubic-bezier(.25,.46,.45,.94); }
.reveal.visible { opacity: 1; transform: translateY(0); }
.reveal-left { opacity: 0; transform: translateX(-40px); transition: all .7s cubic-bezier(.25,.46,.45,.94); }
.reveal-left.visible { opacity: 1; transform: translateX(0); }
.reveal-right { opacity: 0; transform: translateX(40px); transition: all .7s cubic-bezier(.25,.46,.45,.94); }
.reveal-right.visible { opacity: 1; transform: translateX(0); }

/* ══ 1. HERO ══ */
.hero-brand { min-height: 520px; display: flex; align-items: center; justify-content: center; text-align: center; position: relative; overflow: hidden; background: var(--color-primary-abyss, #1C120C); }
.hero-brand::before { content: ''; position: absolute; inset: 0; background: radial-gradient(ellipse at 50% 60%, rgba(192,154,75,0.06) 0%, transparent 55%), radial-gradient(ellipse at 20% 30%, rgba(92,68,52,0.15) 0%, transparent 40%), radial-gradient(ellipse at 80% 80%, rgba(46,31,21,0.3) 0%, transparent 45%); pointer-events: none; }
.hero-brand::after { content: ''; position: absolute; inset: 0; background: repeating-linear-gradient(90deg, transparent, transparent 100px, rgba(192,154,75,0.015) 100px, rgba(192,154,75,0.015) 101px), repeating-linear-gradient(0deg, transparent, transparent 120px, rgba(192,154,75,0.01) 120px, rgba(192,154,75,0.01) 121px); pointer-events: none; }
.hero-brand-content { position: relative; z-index: 1; padding: 80px 40px; max-width: 800px; }
.hero-brand .hb-quote { font-family: var(--font-display, 'Noto Serif SC', serif); font-size: .85rem; color: var(--color-accent, #C09A4B); letter-spacing: .2em; margin-bottom: 28px; opacity: 0; animation: fadeUp .8s ease .2s forwards; }
.hero-brand h1 { font-size: 3.2rem; font-weight: 900; color: var(--color-bg-warm, #E5DDD0); margin-bottom: 28px; letter-spacing: .04em; line-height: 1.35; font-family: var(--font-display, 'Noto Serif SC', serif); opacity: 0; animation: fadeUp .8s ease .4s forwards; }
.hero-brand h1 em { font-style: normal; color: var(--color-accent, #C09A4B); position: relative; }
.hero-brand h1 em::after { content: ''; position: absolute; bottom: 2px; left: 0; right: 0; height: 2px; background: linear-gradient(90deg, transparent, var(--color-accent, #C09A4B), transparent); opacity: .4; }
.hero-brand .hb-sub { font-size: 1.08rem; color: rgba(237,230,214,0.45); line-height: 2; max-width: 560px; margin: 0 auto; opacity: 0; animation: fadeUp .8s ease .6s forwards; }
.hero-brand .hb-divider { width: 48px; height: 1px; background: var(--color-accent, #C09A4B); margin: 32px auto; opacity: 0; animation: fadeUp .8s ease .8s forwards; }
.hero-brand .hb-signature { font-family: var(--font-display, 'Noto Serif SC', serif); font-size: .88rem; color: rgba(237,230,214,0.3); font-style: italic; opacity: 0; animation: fadeUp .8s ease 1s forwards; }

@keyframes fadeUp { from { opacity: 0; transform: translateY(24px); } to { opacity: 1; transform: translateY(0); } }

/* ══ 2. STORY ══ */
.story-section { max-width: var(--max-width, 1280px); margin: 0 auto; padding: 80px 40px; display: grid; grid-template-columns: 1fr 1.2fr; gap: 64px; align-items: center; }
.story-img-frame { width: 100%; aspect-ratio: 4/3; border-radius: var(--radius-lg, 12px); background: linear-gradient(145deg, var(--color-primary-mid, #5C4434), var(--color-primary-dark, #2E1F15)); overflow: hidden; position: relative; box-shadow: 0 24px 48px var(--color-shadow-deep, rgba(30,18,10,0.25)); }
.story-img-frame::before { content: ''; position: absolute; inset: 0; background: radial-gradient(ellipse at 30% 40%, rgba(192,154,75,0.1) 0%, transparent 50%), radial-gradient(ellipse at 70% 70%, rgba(92,68,52,0.2) 0%, transparent 50%); }
.story-img-inner { position: absolute; inset: 20px; border: 1px solid rgba(192,154,75,0.08); border-radius: var(--radius-md, 8px); display: flex; align-items: center; justify-content: center; flex-direction: column; }
.story-img-inner .si-book { font-size: 3.5rem; margin-bottom: 12px; opacity: .4; }
.story-img-inner .si-text { font-family: var(--font-display, 'Noto Serif SC', serif); font-size: 1.1rem; color: rgba(237,230,214,0.25); letter-spacing: .15em; }
.story-img-inner .si-year { font-size: .75rem; color: rgba(192,154,75,0.2); margin-top: 6px; letter-spacing: .2em; }
.story-img-caption { position: absolute; bottom: -12px; right: 20px; background: var(--color-bg-card, #F5F0E8); border: 1px solid var(--color-divider, rgba(74,53,38,0.1)); padding: 8px 16px; border-radius: var(--radius-md, 8px); font-size: .76rem; color: var(--color-text-light, #9B8B7E); box-shadow: 0 4px 12px var(--color-shadow, rgba(44,30,20,0.06)); }
.story-text h2 { font-size: 1.8rem; color: var(--color-text, #2B1D14); margin-bottom: 8px; font-family: var(--font-display, 'Noto Serif SC', serif); }
.story-text .story-year { font-size: .82rem; color: var(--color-accent-muted, #A68638); margin-bottom: 20px; display: block; }
.story-text p { font-size: .95rem; color: var(--color-text-secondary, #6B5B4E); line-height: 2; margin-bottom: 16px; }
.story-text .highlight { color: var(--color-text, #2B1D14); font-weight: 500; border-left: 3px solid var(--color-accent, #C09A4B); padding-left: 14px; margin: 24px 0; font-size: .95rem; line-height: 1.9; }

/* ══ 3. VALUES ══ */
.values-bg { background: var(--color-bg-warm, #E5DDD0); border-top: 1px solid var(--color-divider, rgba(74,53,38,0.1)); border-bottom: 1px solid var(--color-divider, rgba(74,53,38,0.1)); }
.section-center { max-width: 900px; margin: 0 auto; padding: 80px 40px; text-align: center; }
.section-title { font-size: 1.7rem; color: var(--color-text, #2B1D14); margin-bottom: 12px; font-family: var(--font-display, 'Noto Serif SC', serif); }
.section-title.left { padding-left: 20px; display: block; text-align: left; position: relative; }
.section-title.left::before { content: ''; position: absolute; left: 0; top: 5px; bottom: 5px; width: 3px; background: linear-gradient(180deg, var(--color-accent, #C09A4B), var(--color-accent-muted, #A68638)); border-radius: 2px; }
.section-desc { font-size: .93rem; color: var(--color-text-light, #9B8B7E); line-height: 1.8; max-width: 560px; }
.section-desc.center { margin: 0 auto; }
.values-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 22px; margin-top: 40px; }
.value-card { background: var(--color-bg-card, #F5F0E8); border: 1px solid var(--color-divider, rgba(74,53,38,0.1)); border-radius: var(--radius-lg, 12px); padding: 32px 24px; text-align: center; transition: all .35s; cursor: default; }
.value-card:hover { transform: translateY(-5px); box-shadow: 0 16px 36px var(--color-shadow-heavy, rgba(44,30,20,0.16)); border-color: rgba(192,154,75,0.2); }
.value-icon { width: 56px; height: 56px; border-radius: var(--radius-lg, 12px); margin: 0 auto 18px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; background: var(--color-accent-glow, rgba(192,154,75,0.12)); color: var(--color-accent, #C09A4B); }
.value-card h3 { font-family: var(--font-display, 'Noto Serif SC', serif); font-size: 1.05rem; color: var(--color-text, #2B1D14); margin-bottom: 10px; }
.value-card p { font-size: .85rem; color: var(--color-text-light, #9B8B7E); line-height: 1.8; }

/* ══ 4. NUMBERS ══ */
.numbers-section { max-width: var(--max-width, 1280px); margin: 0 auto; padding: 80px 40px; }
.numbers-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-top: 40px; }
.number-card { background: var(--color-bg-card, #F5F0E8); border: 1px solid var(--color-divider, rgba(74,53,38,0.1)); border-radius: var(--radius-lg, 12px); padding: 28px 24px; text-align: center; transition: all .3s; }
.number-card:hover { border-color: rgba(192,154,75,0.15); box-shadow: 0 8px 24px var(--color-shadow, rgba(44,30,20,0.06)); }
.number-value { font-family: var(--font-display, 'Noto Serif SC', serif); font-size: 2.2rem; font-weight: 900; color: var(--color-accent, #C09A4B); line-height: 1; margin-bottom: 6px; }
.number-unit { font-size: .82rem; color: var(--color-text-light, #9B8B7E); }
.number-label { font-size: .88rem; color: var(--color-text-secondary, #6B5B4E); margin-top: 2px; }

/* ══ 5. TEAM ══ */
.section { max-width: var(--max-width, 1280px); margin: 0 auto; padding: 80px 40px; }
.team-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; margin-top: 40px; }
.team-card { background: var(--color-bg-card, #F5F0E8); border: 1px solid var(--color-divider, rgba(74,53,38,0.1)); border-radius: var(--radius-lg, 12px); padding: 32px 24px; text-align: center; transition: all .35s; cursor: default; }
.team-card:hover { transform: translateY(-5px); box-shadow: 0 16px 36px var(--color-shadow-heavy, rgba(44,30,20,0.16)); border-color: rgba(192,154,75,0.15); }
.team-avatar { width: 80px; height: 80px; border-radius: 50%; margin: 0 auto 16px; display: flex; align-items: center; justify-content: center; font-family: var(--font-display, 'Noto Serif SC', serif); font-size: 1.5rem; font-weight: 900; border: 3px solid rgba(192,154,75,0.15); transition: transform .3s; }
.team-card:hover .team-avatar { transform: scale(1.05); }
.team-card .tc-name { font-family: var(--font-display, 'Noto Serif SC', serif); font-size: 1.1rem; font-weight: 700; color: var(--color-text, #2B1D14); margin-bottom: 4px; }
.team-card .tc-role { display: inline-block; font-size: .72rem; font-weight: 600; padding: 3px 12px; border-radius: 12px; background: var(--color-accent-glow, rgba(192,154,75,0.12)); color: var(--color-accent-muted, #A68638); letter-spacing: .04em; margin-bottom: 14px; }
.team-card .tc-bio { font-size: .84rem; color: var(--color-text-light, #9B8B7E); line-height: 1.8; margin-bottom: 14px; }
.team-card .tc-fav { font-size: .78rem; color: var(--color-text-light, #9B8B7E); padding-top: 12px; border-top: 1px solid var(--color-divider, rgba(74,53,38,0.1)); }
.team-card .tc-fav strong { color: var(--color-text-secondary, #6B5B4E); font-weight: 500; }

/* ══ 6. TIMELINE ══ */
.timeline-section { background: var(--color-bg-warm, #E5DDD0); border-top: 1px solid var(--color-divider, rgba(74,53,38,0.1)); border-bottom: 1px solid var(--color-divider, rgba(74,53,38,0.1)); }
.timeline-wrap { max-width: 700px; margin: 40px auto 0; position: relative; padding-left: 40px; }
.timeline-wrap::before { content: ''; position: absolute; left: 13px; top: 0; bottom: 0; width: 2px; background: linear-gradient(180deg, var(--color-accent, #C09A4B), var(--color-divider-strong, rgba(74,53,38,0.18))); }
.tl-item { position: relative; margin-bottom: 40px; }
.tl-item:last-child { margin-bottom: 0; }
.tl-dot { position: absolute; left: -33px; top: 4px; width: 14px; height: 14px; border-radius: 50%; background: var(--color-bg-card, #F5F0E8); border: 3px solid var(--color-accent, #C09A4B); z-index: 1; transition: all .3s; }
.tl-item:last-child .tl-dot { width: 18px; height: 18px; left: -35px; top: 2px; box-shadow: 0 0 0 6px rgba(192,154,75,0.15); background: var(--color-accent, #C09A4B); border-color: var(--color-accent, #C09A4B); }
.tl-year { font-size: 1.1rem; font-weight: 500; color: var(--color-accent, #C09A4B); margin-bottom: 6px; }
.tl-title { font-family: var(--font-display, 'Noto Serif SC', serif); font-size: 1.05rem; font-weight: 700; color: var(--color-text, #2B1D14); margin-bottom: 4px; }
.tl-desc { font-size: .86rem; color: var(--color-text-light, #9B8B7E); line-height: 1.8; }
.tl-desc .tl-stat { display: inline-flex; align-items: center; gap: 4px; background: var(--color-accent-glow, rgba(192,154,75,0.12)); color: var(--color-accent-muted, #A68638); padding: 2px 10px; border-radius: 10px; font-size: .76rem; font-weight: 500; margin-top: 4px; }

/* ══ 7. CONTACT ══ */
.contact-section { max-width: var(--max-width, 1280px); margin: 0 auto; padding: 80px 40px; }
.contact-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 48px; margin-top: 40px; }
.contact-form-wrap { background: var(--color-bg-card, #F5F0E8); border: 1px solid var(--color-divider, rgba(74,53,38,0.1)); border-radius: var(--radius-lg, 12px); padding: 36px; }
.contact-form-wrap h3 { font-family: var(--font-display, 'Noto Serif SC', serif); font-size: 1.2rem; margin-bottom: 24px; color: var(--color-text, #2B1D14); }
.form-group { margin-bottom: 18px; }
.form-label { display: block; font-size: .82rem; font-weight: 500; color: var(--color-text-secondary, #6B5B4E); margin-bottom: 6px; }
.form-label .req { color: var(--color-red, #A04040); margin-left: 2px; }
.form-input { width: 100%; padding: 11px 14px; border: 1px solid var(--color-divider-strong, rgba(74,53,38,0.18)); border-radius: var(--radius-md, 8px); font-size: .88rem; color: var(--color-text, #2B1D14); background: var(--color-bg-cream, #F8F4ED); outline: none; transition: all .2s; box-sizing: border-box; }
.form-input:focus { border-color: var(--color-primary-mid, #5C4434); box-shadow: 0 0 0 3px rgba(74,53,38,0.06); }
.form-input::placeholder { color: var(--color-text-light, #9B8B7E); }
textarea.form-input { resize: vertical; min-height: 120px; }
.form-input.error { border-color: var(--color-red, #A04040); }
.form-error { font-size: .75rem; color: var(--color-red, #A04040); margin-top: 4px; display: none; }
.form-error.show { display: block; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.btn-submit { width: 100%; padding: 13px; background: linear-gradient(135deg, var(--color-accent, #C09A4B), var(--color-accent-muted, #A68638)); color: var(--color-primary-abyss, #1C120C); font-weight: 700; font-size: .92rem; border: none; border-radius: var(--radius-md, 8px); cursor: pointer; transition: all .3s; box-shadow: 0 2px 12px rgba(192,154,75,0.2); margin-top: 6px; }
.btn-submit:hover { transform: translateY(-2px); box-shadow: 0 6px 24px rgba(192,154,75,0.3); }
.contact-info-wrap h3 { font-family: var(--font-display, 'Noto Serif SC', serif); font-size: 1.2rem; margin-bottom: 24px; color: var(--color-text, #2B1D14); }
.ci-list { display: flex; flex-direction: column; gap: 20px; }
.ci-item { display: flex; gap: 16px; align-items: flex-start; }
.ci-icon { width: 44px; height: 44px; border-radius: var(--radius-md, 8px); flex-shrink: 0; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; background: var(--color-bg-card, #F5F0E8); border: 1px solid var(--color-divider, rgba(74,53,38,0.1)); }
.ci-item .ci-label { font-size: .78rem; color: var(--color-text-light, #9B8B7E); margin-bottom: 2px; }
.ci-item .ci-value { font-size: .92rem; color: var(--color-text, #2B1D14); font-weight: 500; }
.ci-item .ci-sub { font-size: .8rem; color: var(--color-text-light, #9B8B7E); margin-top: 1px; }
.ci-social { margin-top: 32px; padding-top: 24px; border-top: 1px solid var(--color-divider, rgba(74,53,38,0.1)); }
.ci-social .ci-social-title { font-size: .82rem; color: var(--color-text-light, #9B8B7E); margin-bottom: 14px; }
.social-links { display: flex; gap: 10px; }
.social-link { width: 42px; height: 42px; border-radius: var(--radius-md, 8px); display: flex; align-items: center; justify-content: center; background: var(--color-bg-card, #F5F0E8); border: 1px solid var(--color-divider, rgba(74,53,38,0.1)); font-size: 1.1rem; cursor: pointer; transition: all .25s; }
.social-link:hover { border-color: var(--color-accent, #C09A4B); background: var(--color-accent-glow, rgba(192,154,75,0.12)); transform: translateY(-2px); }
.form-success { text-align: center; padding: 40px 20px; }
.form-success .fs-icon { font-size: 2.5rem; margin-bottom: 16px; }
.form-success h4 { font-family: var(--font-display, 'Noto Serif SC', serif); font-size: 1.2rem; color: var(--color-text, #2B1D14); margin-bottom: 8px; }
.form-success p { font-size: .9rem; color: var(--color-text-light, #9B8B7E); }
.form-success .btn-back { margin-top: 20px; padding: 10px 28px; background: var(--color-bg-warm, #E5DDD0); border: 1px solid var(--color-divider-strong, rgba(74,53,38,0.18)); border-radius: var(--radius-md, 8px); font-size: .88rem; color: var(--color-text-secondary, #6B5B4E); cursor: pointer; transition: all .2s; }
.form-success .btn-back:hover { border-color: var(--color-primary-mid, #5C4434); color: var(--color-text, #2B1D14); }

/* ══ RESPONSIVE ══ */
@media (max-width: 1024px) {
  .story-section { grid-template-columns: 1fr; gap: 40px; }
  .values-grid { grid-template-columns: repeat(2, 1fr); }
  .numbers-grid { grid-template-columns: repeat(2, 1fr); }
  .team-grid { grid-template-columns: repeat(2, 1fr); }
  .contact-grid { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .hero-brand h1 { font-size: 2rem; }
  .hero-brand-content { padding: 50px 20px; }
  .story-section { padding: 50px 20px; }
  .section { padding: 50px 20px; }
  .section-center { padding: 50px 20px; }
  .values-grid { grid-template-columns: 1fr; }
  .numbers-grid { grid-template-columns: 1fr 1fr; }
  .numbers-section { padding: 50px 20px; }
  .team-grid { grid-template-columns: 1fr; }
  .contact-section { padding: 50px 20px; }
  .contact-form-wrap { padding: 24px; }
  .form-row { grid-template-columns: 1fr; }
}
</style>
