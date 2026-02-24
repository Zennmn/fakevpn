# FakeVPN

一个基于 LSPosed/Xposed 的 Android 模块示例，用于在指定作用域应用内模拟“设备正在使用 VPN”的网络状态。

## 项目简介

本项目通过 Hook 系统网络相关 API，在目标应用进程中返回 VPN 相关结果，适用于调试、研究和兼容性验证场景。

## 主要功能

- 伪装 `NetworkCapabilities.hasTransport(TRANSPORT_VPN) = true`
- 伪装 `NetworkCapabilities.hasCapability(NET_CAPABILITY_NOT_VPN) = false`
- 修改 `ConnectivityManager.getActiveNetworkInfo()` 的网络类型为 `TYPE_VPN`
- 在 `NetworkInterface.getNetworkInterfaces()` 中注入 `tun0` 风格网卡信息
- 提供模块状态页面与配置引导

## 环境要求

- Android Studio / Gradle
- Android SDK（已配置 `local.properties`）
- 支持 Xposed/LSPosed 的设备环境
- Android 8.0+（`minSdk 26`）

## 构建方式

### Debug 包

```bash
./gradlew assembleDebug
```

Windows:

```powershell
.\gradlew.bat assembleDebug
```

### Release 包

```bash
./gradlew assembleRelease
```

构建输出目录：

- `app/build/outputs/apk/debug/`
- `app/build/outputs/apk/release/`

## 使用说明

1. 安装模块 APK。
2. 在 LSPosed 中启用模块。
3. 在作用域中勾选需要生效的目标应用（如需模块页面显示“已激活”，建议同时勾选模块自身包名）。
4. 重启目标应用进程（必要时重启设备）后生效。

## 注意事项

- 本项目仅用于学习与测试，请勿用于违反服务条款或法律法规的用途。
- 不同 Android 版本、ROM 与应用实现差异较大，实际效果可能不同。

## License

MIT
