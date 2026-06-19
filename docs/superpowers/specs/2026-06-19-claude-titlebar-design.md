# Claude 助手标题栏优化

**日期：** 2026-06-19
**状态：** 已批准

## 目标

将 Claude 助手页面的标题栏从简单的扁平横条升级为与首页 Hero 风格一致的渐变标题栏。

## 设计

### 布局

- 高度：48dp → 72dp
- 结构：`LinearLayout` → `ConstraintLayout`
- 背景：纯色 → `@drawable/bg_home_hero`（蓝紫渐变 135°，32dp 圆角）
- 标题 + 副标题垂直居中 packed，设置按钮右侧居中

### 视觉

- 标题：`textAppearanceTitleMedium`，`@color/home_on_hero`（白色）
- 副标题：`textAppearanceBodySmall`，`@color/home_on_hero_variant`（浅蓝白）
- 设置按钮：`bg_home_icon_button` 背景，白色 tint

### 复用资源

| 资源 | 用途 |
|------|------|
| `bg_home_hero` | 标题栏渐变背景 |
| `bg_home_icon_button` | 设置按钮背景 |
| `home_on_hero` | 标题文字颜色 |
| `home_on_hero_variant` | 副标题文字颜色 |
| `home_claude_subtitle` | 副标题字符串（已有） |

## 实施

**修改文件：** `app/src/main/res/layout/content_claude.xml`

**改动范围：** 替换第 16-44 行 top_bar `LinearLayout` 为 `ConstraintLayout`

**不影响：**
- `btn_settings` id 保持不变，MainActivity 绑定不受影响
- 标题栏下方 Connection Status Card 及其他卡片不变
- 无需新增资源文件