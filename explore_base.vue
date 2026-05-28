<template>
  <div class="explore-page">
    <section class="page-header">
      <div class="ph-inner">
        <div class="ph-left">
          <h1>书笺传情</h1>
          <p>翻开命运之书 · 在漂流瓶中遇见有缘人</p>
        </div>
        <div class="ph-stats">
          <div class="phs">
            <div class="phs-num">12</div>
            <div class="phs-label">命运卡牌</div>
          </div>
          <div class="phs">
            <div class="phs-num">12</div>
            <div class="phs-label">海中漂流瓶</div>
          </div>
        </div>
      </div>
    </section>
    <div class="tab-bar">
      <div class="tab-item active">
        <span class="ti-icon">📖</span>
        <span>书斋社区</span>
      </div>
      <div class="tab-item">
        <span class="ti-icon">🔮</span>
        <span>命运之书</span>
      </div>
      <div class="tab-item">
        <span class="ti-icon">🍾</span>
        <span>漂流瓶</span>
      </div>
    </div>
    <div class="section-wrap">
      <div class="placeholder-text">书笺传情 · 以书会友</div>
    </div>
  </div>
</template>

<style scoped>
.explore-page { min-height: 100vh; background: var(--color-bg); }
.page-header {
  background: var(--color-primary-dark);
  position: relative;
  overflow: hidden;
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
.tab-item.active {
  background: var(--color-primary);
  color: var(--color-bg-warm);
  border-color: var(--color-primary);
}
.section-wrap {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 60px 40px;
}
.placeholder-text {
  text-align: center;
  font-size: 1.2rem;
  color: var(--color-text-light);
}
</style>
