# Claude 标题栏优化实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 Claude 助手页面标题栏从扁平 LinearLayout 升级为与首页 Hero 风格一致的渐变标题栏

**Architecture:** 仅修改 `content_claude.xml` 中 top_bar 部分，替换为 ConstraintLayout + 渐变背景，复用现有 drawable 和颜色资源

**Tech Stack:** Android XML Layout, Material 3

---

### Task 1: 替换标题栏布局

**Files:**
- Modify: `app/src/main/res/layout/content_claude.xml:16-44`

- [ ] **Step 1: 替换 top_bar 的 LinearLayout 为 ConstraintLayout**

将第 16-44 行：

```xml
        <!-- Top Bar -->
        <LinearLayout
            android:id="@+id/top_bar"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:gravity="center_vertical"
            android:orientation="horizontal">

            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:ellipsize="end"
                android:maxLines="1"
                android:text="@string/home_claude_title"
                android:textAppearance="?attr/textAppearanceTitleLarge"
                android:textColor="?attr/colorOnSurface" />

            <ImageButton
                android:id="@+id/btn_settings"
                android:layout_width="32dp"
                android:layout_height="32dp"
                android:background="@drawable/ripple_circle"
                android:contentDescription="@string/home_settings_title"
                android:scaleType="centerInside"
                android:src="@drawable/baseline_settings_24"
                app:tint="?attr/colorOnSurfaceVariant" />

        </LinearLayout>
```

替换为：

```xml
        <!-- Top Bar - Hero 风格渐变 -->
        <androidx.constraintlayout.widget.ConstraintLayout
            android:id="@+id/top_bar"
            android:layout_width="match_parent"
            android:layout_height="72dp"
            android:background="@drawable/bg_home_hero"
            android:paddingHorizontal="20dp"
            android:paddingVertical="12dp">

            <TextView
                android:id="@+id/tv_claude_title"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:ellipsize="end"
                android:maxLines="1"
                android:text="@string/home_claude_title"
                android:textAppearance="?attr/textAppearanceTitleMedium"
                android:textColor="@color/home_on_hero"
                app:layout_constraintBottom_toTopOf="@id/tv_claude_subtitle"
                app:layout_constraintEnd_toStartOf="@id/btn_settings"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toTopOf="parent"
                app:layout_constraintVertical_chainStyle="packed" />

            <TextView
                android:id="@+id/tv_claude_subtitle"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:ellipsize="end"
                android:maxLines="1"
                android:text="@string/home_claude_subtitle"
                android:textAppearance="?attr/textAppearanceBodySmall"
                android:textColor="@color/home_on_hero_variant"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintEnd_toStartOf="@id/btn_settings"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toBottomOf="@id/tv_claude_title" />

            <ImageButton
                android:id="@+id/btn_settings"
                android:layout_width="36dp"
                android:layout_height="36dp"
                android:background="@drawable/bg_home_icon_button"
                android:contentDescription="@string/home_settings_title"
                android:scaleType="centerInside"
                android:src="@drawable/baseline_settings_24"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintTop_toTopOf="parent"
                app:tint="@color/home_on_hero" />

        </androidx.constraintlayout.widget.ConstraintLayout>
```

- [ ] **Step 2: 构建 debug APK 验证编译通过**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/layout/content_claude.xml
git commit -m "feat: upgrade Claude title bar to hero-style gradient
header"
```