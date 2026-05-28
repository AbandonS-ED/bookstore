<template>
  <footer class="footer">
    <div class="footer-main">
      <div class="footer-brand">
        <div class="logo">
          <div class="logo-icon">📖</div>
          <div>
            书斋
          </div>
        </div>
        <p>用阅读点亮时光。书斋精选万册好书，从经典文学到前沿新知，让好书触手可及。</p>
      </div>
      <div class="footer-col">
        <h4>书籍分类</h4>
        <ul>
          <li><router-link to="/categories">文学小说</router-link></li>
          <li><router-link to="/categories">社科人文</router-link></li>
          <li><router-link to="/categories">科技新知</router-link></li>
          <li><router-link to="/categories">商业财经</router-link></li>
        </ul>
      </div>
      <div class="footer-col">
        <h4>帮助中心</h4>
        <ul>
          <li><a href="#">配送说明</a></li>
          <li><a href="#">退换政策</a></li>
          <li><a href="#">支付方式</a></li>
          <li><a href="#">联系客服</a></li>
        </ul>
      </div>
      <div class="footer-col">
        <h4>关于我们</h4>
        <ul>
          <li><a href="#">品牌故事</a></li>
          <li><a href="#">隐私政策</a></li>
          <li><a href="#">用户协议</a></li>
        </ul>
      </div>
    </div>
    <div class="footer-bottom">
      <span>© 2026 书斋 · 课程设计项目</span>
      <span>用心选书，温暖每一个阅读时刻</span>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: var(--color-primary-abyss);
  color: rgba(237,230,214,0.45);
  margin-top: var(--space-16);
}

.footer-main {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 56px 40px 40px;
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr 1fr;
  gap: 44px;
}

.logo {
  font-family: var(--font-display);
  font-size: 1.3rem;
  font-weight: 900;
  color: var(--color-bg-warm);
  letter-spacing: 0.06em;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.logo-icon {
  width: 30px;
  height: 30px;
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted));
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  transform: rotate(-2deg);
  box-shadow: 0 2px 6px rgba(192,154,75,0.2);
}

.logo-text-sub {
  font-size: 0.55rem;
  color: var(--color-text-light);
  font-weight: 400;
  letter-spacing: 0.15em;
  display: block;
  margin-top: -2px;
}

.footer-brand p {
  font-size: 0.85rem;
  line-height: 1.9;
  max-width: 280px;
}

.footer-col h4 {
  font-size: 0.88rem;
  color: rgba(237,230,214,0.7);
  font-weight: 600;
  margin-bottom: 20px;
  letter-spacing: 0.04em;
  font-family: var(--font-body);
}

.footer-col ul {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 11px;
}

.footer-col a,
.footer-col router-link {
  font-size: 0.82rem;
  color: rgba(237,230,214,0.35);
  transition: color 0.2s ease;
}

.footer-col a:hover,
.footer-col router-link:hover {
  color: var(--color-accent);
}

.footer-bottom {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 20px 40px;
  border-top: 1px solid rgba(237,230,214,0.04);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
  color: rgba(237,230,214,0.2);
}

@media (max-width: 1024px) {
  .footer-main { grid-template-columns: 1fr 1fr; }
}

@media (max-width: 640px) {
  .footer-main { grid-template-columns: 1fr; }
  .footer-bottom { flex-direction: column; gap: 8px; }
}
</style>
