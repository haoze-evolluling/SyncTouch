# SyncTouch 功能增强计划

> 基于 4 轮代码审查：结构分析 → 规格对照 → 缺陷扫描 → 竞品对比

## 项目现状

| 维度 | 状态 |
|------|------|
| 已实现功能 | 键盘(521行)、触控板(374行)、TV遥控(127行)、Agent页面、宏管理、设置页、暗色主题、重连机制、页面切换动画 |
| 蓝牙协议 | Keyboard + Mouse + Consumer Control 三合一 HID 描述符 |
| 架构 | 单 Activity + 多 Fragment, MVVM (ViewModel + LiveData), SharedPreferences 持久化 |
| 已实现规格 | 6 份设计文档中 4 份已落地（设置增强、浮动标题卡片、视图切换动画、Hero 渐变修复） |
| 未实现规格 | 设置页 UI 美化（卡片圆角 22dp→16dp、阴影、涟漪效果） |
| 代码缺陷 | 键盘修饰键仅支持 3 个同时激活；触摸板无中键手势；开源许可 InfoItem 为空白占位 |
| 竞品缺失 | 无体感鼠标、无媒体播放控制、无游戏手柄、无剪贴板同步、无快捷设置磁贴、无桌面小组件 |

---

## 第 1 层：快速收益

> 实现成本低，体验提升明显，适合作为下个迭代

### 1.1 媒体播放控制

**现状：** TV Remote 已有音量+/-、静音、电源，Consumer Control 报告基础设施完整。

**方案：** 在 TV Remote 页面添加 4 个按钮（播放/暂停、上一首、下一首、停止），绑定 Consumer Control usage code。

**理由：** `ConsumerReport` 已实现全 16-bit usage 范围，`TvRemoteSender` 已封装 `sendConsumer()`。纯 UI 绑定。

**改动量：** ~30 行 Kotlin + 4 个按钮 XML

**涉及文件：** `TvRemoteSender.kt`, `TvRemoteFragment.kt`, `fragment_tvremote.xml`, `ConsumerReport.kt`

---

### 1.2 触摸板中键点击

**现状：** `MouseSender` 已支持 `BUTTON_MIDDLE`，`MouseReport` 已定义 middle button bitfield。但 `TouchpadFragment` 无三指手势。

**方案：** 在 `handleTouchEvent()` 中检测三指点击（`pointerCount == 3`，duration < 200ms，distance < 20px），发送 `sendMouseClick(BUTTON_MIDDLE)`。

**理由：** 中键在浏览器（新标签页打开链接）、IDE（跳转定义）等场景高频使用。

**改动量：** ~20 行

**涉及文件：** `TouchpadFragment.kt`

---

### 1.3 快捷设置磁贴 (Quick Settings Tile)

**现状：** 用户必须打开 App 才能连接/断开设备。

**方案：** 实现 `TileService`，在下拉快捷菜单中提供一键连接/断开。点击时：
- 未连接 → 自动连接上次设备
- 已连接 → 断开

**理由：** 原生 Android API，无需新权限，高频使用场景。

**改动量：** ~80 行新文件

**涉及文件：** `BluetoothTileService.kt` (新建), `AndroidManifest.xml`

---

### 1.4 键盘修饰键状态指示器

**现状：** `KeyboardFragment` 中 Ctrl/Alt/Win 的激活状态仅通过按钮颜色变化体现，用户容易忽略。没有全局状态栏。

**方案：** 在键盘顶部添加一行小指示条（3 个胶囊形标签），分别显示 Ctrl/Alt/Win 的激活状态，绿色=激活，灰色=关闭。

**理由：** 触摸打字时用户视线在目标设备屏幕，不在手机键盘上，需要一个显眼的指示器确认修饰键状态，避免误操作。

**改动量：** ~15 行 Kotlin + 1 个 LinearLayout

**涉及文件：** `KeyboardFragment.kt`, `fragment_keyboard.xml`

---

### 1.5 主屏小组件 (App Widget)

**现状：** 无。

**方案：** 4x1 小组件，显示蓝牙连接状态 + 一键连接/断开按钮。点击打开 App。

**理由：** 桌面小组件比 Tile 更直观，适合不熟悉下拉菜单的用户。

**改动量：** ~100 行新文件

**涉及文件：** `SyncTouchWidget.kt` (新建), `widget_layout.xml` (新建), `AndroidManifest.xml`

---

## 第 2 层：中等投入，显著扩展使用场景

### 2.1 体感鼠标（陀螺仪）

**现状：** `AndroidManifest.xml` 已声明 `android.hardware.sensor.gyroscope` 可选，但未使用。

**方案：** 在 Touchpad 页面添加"体感模式"切换按钮。激活后使用 `SensorManager` 监听 `TYPE_GYROSCOPE`，将角速度映射为鼠标移动增量。手机平放时自动回中。

**理由：** 竞品（Remote Mouse、Unified Remote）标配功能。无需触摸屏幕即可控制光标，适合演示场景。

**改动量：** ~150 行

**涉及文件：** `TouchpadFragment.kt`, `fragment_touchpad.xml`

---

### 2.2 演示遥控器模式

**现状：** 无。

**方案：** 新增独立页面"演示"，包含：
- 上一页/下一页（键盘左右箭头）
- 激光笔模拟（触摸板移动 + 红色高亮圆点叠加层）
- 黑屏/恢复（发送 `B` 键或 Consumer Control display toggle）
- 计时器（可选）

**理由：** 会议场景高频需求，键盘和触摸板都不适合单手操作投影。

**改动量：** ~200 行新页面

**涉及文件：** `PresentationFragment.kt` (新建), `fragment_presentation.xml` (新建)

---

### 2.3 游戏手柄模式

**现状：** HID 描述符仅包含 Keyboard + Mouse + Consumer Control。

**方案：** 在 `DescriptorCollection` 中添加 Gamepad TLC（Report ID 4），实现标准 16 按钮 + 双摇杆 + D-pad 的 HID 游戏手柄描述符。新增游戏手柄页面，左半屏为虚拟摇杆，右半屏为 ABXY 按钮 + 肩键。

**理由：** 覆盖 Android TV / PC 游戏场景，HID 描述符扩展是纯数据改动。

**改动量：** ~300 行新描述符 + 新页面

**涉及文件：** `DescriptorCollection.kt`, `GamepadSender.kt` (新建), `GamepadFragment.kt` (新建)

---

### 2.4 宏导入/导出

**现状：** `MacroRepository` 使用 SharedPreferences + JSON 存储，无备份机制。

**方案：** 添加"导出宏"按钮（生成 JSON 复制到剪贴板）、"导入宏"按钮（从剪贴板解析 JSON 或通过文件选择器）。添加二维码分享（将 JSON 编码为二维码，另一台手机扫描导入）。

**理由：** 用户自定义宏是重要数据资产，换手机或分享给同事时无法迁移。

**改动量：** ~100 行

**涉及文件：** `MacroRepository.kt`, 设置页新按钮

---

### 2.5 多设备配置

**现状：** 只能记住最后连接的一台设备地址。切换设备需重新配对。

**方案：** 记住多台已配对设备，在设备列表中标记"常用设备"。连接时根据当前设备自动切换。支持为不同设备设置不同的偏好（如 PC 用键盘模式、TV 用遥控模式）。

**理由：** 用户常在家（TV）和办公室（PC）之间切换。

**改动量：** ~80 行修改

**涉及文件：** `BluetoothHidService.kt`, `DeviceListBottomSheetFragment.kt`

---

## 第 3 层：大功能，增加核心竞争力

### 3.1 剪贴板同步

**现状：** 无。

**方案：** 使用 `ClipboardManager.OnPrimaryClipChangedListener` 监听手机剪贴板变化。当检测到新文本时，通过键盘模拟粘贴（Ctrl+V 或 Cmd+V）发送到连接设备。在 Agent 页面添加"同步剪贴板"开关。

**注意事项：** Android 10+ 限制后台读取剪贴板，需前台服务或用户可见时操作。

**理由：** 跨设备复制粘贴是用户呼声最高的缺失功能。竞品 Remote Mouse 的付费功能。

**改动量：** ~120 行

**涉及文件：** `ClipboardSyncService.kt` (新建), `MainActivity.kt`

---

### 3.2 语音输入转文字

**现状：** 无。

**方案：** 在键盘页面添加麦克风按钮。点击后启动 `SpeechRecognizer`，识别结果通过 `KeyboardSender.sendText()` 逐字发送到设备。支持实时识别（边说边打）和整句识别（说完发送）。

**理由：** 手机键盘输入长文本效率低，语音输入弥补这一短板。

**改动量：** ~150 行

**涉及文件：** `VoiceInputHelper.kt` (新建), `KeyboardFragment.kt`

---

### 3.3 自定义键盘布局

**现状：** 键盘布局硬编码在 `KeyboardFragment.kt` 的 `rowXKeys` 列表中，仅支持 QWERTY。

**方案：** 允许用户在设置页选择布局模板（QWERTY/AZERTY/QWERTZ/数字小键盘），或进入编辑模式拖拽按键调整位置/大小。布局保存为 JSON 到 SharedPreferences。

**理由：** 国际用户需要 AZERTY/QWERTZ，会计/数据录入需要数字小键盘。

**改动量：** ~400 行

**涉及文件：** `KeyboardFragment.kt`, `KeyboardLayoutManager.kt` (新建), 设置页

---

## 推荐实施路线

```
第 1 层（2-3 天）
├── 1.1 媒体播放控制       ← 最先做，改动最小
├── 1.2 触摸板中键         ← 顺手做
├── 1.4 键盘修饰键指示器    ← 顺手做
├── 1.3 快捷设置磁贴       ← 独立模块
└── 1.5 主屏小组件         ← 独立模块
        ↓
第 2 层（3-5 天）
├── 2.1 体感鼠标           ← 用户最期待
├── 2.2 演示遥控器         ← 会议场景刚需
└── 2.4 宏导入导出         ← 数据安全
        ↓
第 3 层（按需启动）
├── 3.1 剪贴板同步         ← 竞品付费功能
├── 3.2 语音输入           ← 差异化竞争力
└── 3.3 自定义键盘布局     ← 国际化需求
```

---

## 已排除的功能（及理由）

| 功能 | 排除理由 |
|------|----------|
| 文件传输 | HID 协议不支持文件传输，需额外实现 OBEX 或自建 TCP 通道，投入产出比极低 |
| 游戏手柄模式（第 2 层标记） | 大多数人使用专用蓝牙手柄，手机虚拟摇杆体验差。如果用户明确需要再加 |
| 多设备同时连接 | Android HID 框架限制，一个 `BluetoothHidDevice` 实例只能连接一个 host |
| AI 辅助输入 | 需要网络权限 + 后端服务，超出当前纯本地 App 定位 |
| NFC 快速配对 | 需要硬件支持 + 额外的配对逻辑，用户基数小 |