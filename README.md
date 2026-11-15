# CircleIcons · 圆形图标适配

把旧式 App 图标接入 Android 自适应图标体系，修复部分通知在状态栏里显示成白色方块的问题。桌面和系统列表保留彩色图标，状态栏保留随背景变色的单色图案。

**需要 Root 和支持传统 Xposed API 82 的框架。普通、未 Root 的原生系统不能直接使用。** 项目最初针对 Pixel 4 XL / LineageOS 开发，3.0 开始按系统接口检测功能，面向 Android 8.1–17 的 AOSP、Pixel 和 LineageOS 衍生系统。不同 ROM 的支持程度见 [兼容性与验证](docs/COMPATIBILITY.md)。

[下载 APK](https://github.com/Vickylines/CircleIcons/releases) · [English](README.en.md) · [问题反馈](https://github.com/Vickylines/CircleIcons/issues)


![Android rendering preview](docs/preview.png)

## 能处理哪些位置

- Launcher3 与 Pixel Launcher 的应用图标；保留原本的自适应图标。
- 设置、权限管理、安装器、文件管理器中的应用图标。
- 分享/打开方式面板，以及具备相应接口的启动画面。
- 状态栏里的不透明通知图标：提取图案，让 SystemUI 继续控制明暗色、壁纸切换和动画。
- 新版 SystemUI 的通知图标缓存：App 安装、更新或移除后，清理该 App 的旧图标。

没有逐个 App 的白名单。以后安装的 App 也会经过相同的处理入口。动态通知图标、会话头像、系统状态符号和用户头像不做静态图案替换；工作资料徽标仍由系统叠加。

圆形来自系统的自适应图标蒙版。请在支持形状选择的 ROM 中选中圆形；本模块不会把快捷开关、按钮等其它界面元素一起改成圆形。

## 安装

1. 安装适合当前 Android 版本的 Xposed 框架，例如兼容传统 API 的 LSPosed 分支。框架本身支持哪些系统，以其项目说明为准。
2. 安装 Releases 中的 `CircleIcons-3.0.0-beta.1.apk`，在框架管理器里启用模块。
3. 勾选推荐范围中实际存在的系统应用，至少包括**系统界面、当前桌面、设置**。Pixel Launcher 包名为 `com.google.android.apps.nexuslauncher`。
4. 重启手机，让所有目标进程重新加载。升级后也需重新加载目标进程。

Android 的旧版分享面板可能运行在 `system:ui`。只有需要适配这个面板时，才额外选择框架管理器里的 Android 系统范围。代码明确排除 `system_server`。

如果桌面仍保留旧图标，先重启桌面进程；模块会给支持的桌面缓存附加版本标记。无需清除桌面数据，清除数据会丢失桌面布局。

## 支持范围

3.0.0-beta.1 是跨版本适配的首个公开测试版，**不是“所有原生系统全功能通用”**。系统接口缺失时，只跳过对应功能，其它入口继续工作。Pixel Launcher 已加入包名与 AOSP 图标接口兼容路径，Google 的私有或混淆实现仍需要对应版本实机验证。

- 编译与运行下限：Android 8.1 / API 27。
- 按接口适配：Android 8.1–17 / API 27–37。
- 原生图形测试、系统接口检查、真实 Xposed Hook 验证是不同层次；逐项结果记录在 [兼容性文档](docs/COMPATIBILITY.md)。
- 未对 MIUI / HyperOS、One UI、ColorOS 等深度改造界面作全面适配，也不修改第三方桌面、App 内部的任意图片或通知内容缩略图。

反馈时附上 ROM、Android 版本、桌面包名、模块和框架版本，以及框架日志中的 `CircleIcons` 行。发布截图和日志前请去掉通知正文等个人信息。[排查说明](docs/TROUBLESHOOTING.md)

## 构建与检查

需要 JDK 17、Android SDK 36。Gradle Wrapper 与依赖版本已经固定。

```sh
./gradlew :app:assemblePhoneRelease :app:lintPhoneRelease
./gradlew :app:assembleInspectionDebug :app:assembleInspectionDebugAndroidTest
python scripts/run_inspection.py --serial emulator-5554
```

Windows 使用 `gradlew.bat`。如果工作路径包含中文，可把工程放到英文路径，或给 Gradle 加上 `"-Pandroid.overridePathCheck=true"`。在 `local.properties` 中配置本机 `sdk.dir`，此文件不提交。

默认 Release 构建不含签名。签名由本机环境变量提供：`CIRCLE_KEYSTORE`、`CIRCLE_STORE_PASSWORD`、`CIRCLE_KEY_ALIAS`、`CIRCLE_KEY_PASSWORD`。私钥不放在仓库里。检查版本使用独立包名 `com.yusd.circleicons.inspection`，也不会作为 Xposed 模块启用。

GitHub Actions 构建 APK、执行 Lint，并在 Android 8.1、11、15 上运行实际 Android 图形测试和系统 APK 接口检查。CI 不安装 Xposed，因此通过 CI 不等于已经验证每个 ROM 的 Hook 效果。

## 开源许可

[Apache License 2.0](LICENSE)。单色图案生成器来自 AOSP / LineageOS，保留上游版权与许可，修改说明见 [NOTICE](NOTICE.md)。模块不申请网络权限，不上传图标或使用记录。
