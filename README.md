# Custom Menu - 自定义悬浮菜单栏

一款专为瀑布屏设计的自定义悬浮菜单栏应用，让您在手机屏幕边缘快速访问常用应用。

---

## ✨ 功能特性

### 🎯 核心功能
- **多菜单栏支持**：最多可添加 10 个独立的悬浮菜单栏
- **应用快速启动**：点击图标即可打开对应的应用程序

### ⚙️ 个性化配置
每个菜单栏均可独立配置：

| 配置项 | 说明 | 默认值        |
| :--- | :--- |:-----------|
| **菜单宽度** | 菜单栏整体宽度 | 60dp       |
| **菜单高度** | 菜单栏整体高度 | 200dp      |
| **图标宽度** | 应用图标的宽度 | 50dp       |
| **图标高度** | 应用图标的高度 | 50dp       |
| **图标间距** | 图标之间的垂直间距 | 2dp        |
| **位置 X/Y** | 菜单栏在屏幕上的坐标位置 | 50px/200px |

### 📱 适配特性
- **瀑布屏优化**：专为曲面屏设计，菜单栏固定为垂直模式
- **灵活尺寸**：支持小尺寸显示，图标自动缩放适配

---

## 🚀 快速开始

### 安装要求
- Android 版本：Android 11 (API 30) 及以上
- 权限需求：悬浮窗权限 (SYSTEM_ALERT_WINDOW)

### 首次使用
1. 打开应用，系统会自动请求悬浮窗权限
2. 点击「添加菜单栏」按钮创建新的菜单栏
3. 在「选择应用」对话框中选择要添加的应用
4. 点击「配置」按钮调整菜单栏的尺寸和位置

---

## 📖 使用说明

### 添加菜单栏
1. 点击主界面的「添加菜单栏」按钮
2. 在弹出的应用列表中勾选要添加的应用
3. 点击「确定」完成添加

### 配置菜单栏
1. 在菜单栏列表中找到目标菜单栏
2. 点击「配置」按钮打开配置对话框
3. 根据需要调整各项参数
4. 点击「确定」保存配置，更改会立即生效

### 删除菜单栏
1. 在菜单栏列表中找到目标菜单栏
2. 点击「删除」按钮
3. 确认删除操作

### 编辑应用列表
1. 在菜单栏列表中找到目标菜单栏
2. 点击「选择应用」按钮
3. 修改应用选择后点击「确定」

---

## 🏗️ 技术栈

- **语言**：Kotlin
- **框架**：AndroidX
- **悬浮窗**：WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
- **数据存储**：SharedPreferences + Gson

---

## 📁 项目结构

```
app/
├── src/main/
│   ├── java/com/example/custommenu/
│   │   ├── MainActivity.kt          # 主界面
│   │   ├── FloatMenuService.kt      # 悬浮窗服务
│   │   ├── FloatMenuView.kt         # 悬浮菜单栏视图
│   │   ├── MenuConfig.kt            # 配置数据类
│   │   ├── ConfigManager.kt         # 配置管理
│   │   ├── AppListAdapter.kt        # 应用列表适配器
│   │   ├── MenuListAdapter.kt       # 菜单栏列表适配器
│   │   └── PermissionHelper.kt      # 权限帮助类
│   └── res/
│       ├── layout/                  # 布局文件
│       ├── drawable/                # 资源文件
│       └── values/                  # 配置值
└── build.gradle                     # 构建配置
```

---

## 📋 许可证

```
MIT License

Copyright (c) 2024 Custom Menu

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

*Made with ❤️ for Android users*
