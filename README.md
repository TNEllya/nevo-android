# NEVO Android VoIP Client

<p align="center">
  <strong>🇨🇳 中文</strong> &nbsp;|&nbsp; <a href="#english">🇬🇧 English</a>
</p>

NEVO VoIP 的 Android 原生客户端，基于 Kotlin + Jetpack Compose + Hilt + JNI 构建。完整保留并优化 NEVO 桌面端全部功能模块，支持服务器连接、频道管理、实时文字聊天、语音通话、屏幕共享等功能。

---

## 功能特性

- 🔌 **服务器连接管理** — 支持多服务器配置、TLS 加密、心跳保活、自动重连
- 📢 **频道管理** — 树形频道列表、语音/文字频道分类、子频道展开折叠、拖拽排序
- 💬 **实时文字聊天** — 频道聊天、表情面板、消息持久化（Room）、滚动加载
- 🎙️ **语音通话** — 基于 Opus 编码的 PTT/VAD 语音引擎、mute/deafen 控制、libsodium XChaCha20-Poly1305 加密
- 📺 **屏幕共享** — H.264 编码 + NAL 分片 UDP 传输
- 🌐 **多语言支持** — English / 简体中文 / 繁體中文
- 🎨 **Material Design 3** — 亮色/暗色/系统跟随主题、自适应图标
- 🔄 **在线更新** — 通过 GitHub Releases API 检查更新、下载 APK 并自动安装

## 技术栈

| 层级 | 技术 |
|------|------|
| **UI** | Jetpack Compose + Material Design 3 |
| **架构** | Clean Architecture (UI → ViewModel → UseCase → Repository) |
| **DI** | Hilt (Dagger) |
| **数据库** | Room (SQLite) |
| **网络** | OkHttp + 自研 TCP/UDP 协议栈 |
| **加密** | libsodium (XChaCha20-Poly1305) via JNI |
| **音频** | Opus 编解码 via JNI |
| **视频** | MediaCodec (H.264) |
| **导航** | Compose Navigation |
| **状态管理** | StateFlow + collectAsState |
| **构建** | Gradle 8.11.1 + AGP 8.7.3 + KSP |

## 项目结构

```
nevo-android/
├── app/src/main/java/com/nevo/voip/
│   ├── MainActivity.kt                  # 应用入口
│   ├── NevoApplication.kt               # Hilt Application
│   ├── core/
│   │   ├── di/                          # Hilt 模块（AppModule, DatabaseModule, NetworkModule）
│   │   ├── network/                     # TCP/UDP 连接管理、网络监控
│   │   ├── protocol/                    # NevoBuffer 二进制协议序列化
│   │   ├── crypto/                      # JNI 加密（CryptoManager, jni_crypto.cpp）
│   │   ├── database/                    # Room 数据库、DAO、实体
│   │   ├── datastore/                   # DataStore 偏好设置
│   │   └── update/                      # 在线更新管理器
│   ├── feature/
│   │   ├── connection/                  # 服务器连接功能
│   │   ├── channel/                     # 频道管理功能
│   │   ├── chat/                        # 文字聊天功能
│   │   ├── voice/                       # 语音通话功能
│   │   ├── screen_share/                # 屏幕共享功能
│   │   └── settings/                    # 设置功能
│   ├── service/
│   │   └── NevoAudioService.kt         # 音频前台服务
│   └── ui/
│       ├── navigation/                  # 导航图
│       ├── theme/                       # 主题系统（颜色/字体/形状）
│       └── components/                  # 通用组件
├── native/
│   └── src/main/cpp/                    # C++ JNI（opus, libsodium）
├── app/src/main/res/                    # 资源（图标/多语言字符串/XML）
├── gradle/
│   ├── libs.versions.toml               # 版本目录
│   └── wrapper/
└── build.gradle.kts                     # 根构建脚本
```

## 系统要求

- **最低版本**: Android 8.0 (API 26)
- **编译 SDK**: 35
- **推荐设备**: 4 GB RAM 以上

## 构建指南

### 前置条件

1. **JDK 17** (推荐 Amazon Corretto)
2. **Android SDK** (platform 35, build-tools 35.0.0)
3. **NDK 27.0.12077973** + **CMake 3.22.1**
4. **Gradle 8.11.1** (使用 wrapper 自动下载)

### 配置

创建 `local.properties`：

```properties
sdk.dir=C\:\\Android
```

### 编译

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (需配置签名)
./gradlew assembleRelease
```

输出路径：`app/build/outputs/apk/debug/app-debug.apk`

## 协议兼容性

与 NEVO C++ 服务端和 Python 客户端字节级兼容：

- **TCP 帧**: `payloadLength(4) + messageType(4) + requestId(4)` 12 字节头
- **序列化**: `NevoBuffer` (ByteBuffer 小端序) 与 Python `nevo_wire.py` 逐字节一致
- **加密**: XChaCha20-Poly1305 AEAD 语音加密，与 `voice_crypto.py` 一致

## 更新机制

应用内置在线更新功能，从 GitHub Releases 自动拉取最新 APK：

```
Settings → About → Check Updates
     ↓
GitHub API /repos/TNEllya/nevo-android/releases/latest
     ↓
版本比对 → 下载 APK → FileProvider 安装
```

---

<br>

<a name="english"></a>

# 🇬🇧 English

NEVO VoIP Android native client, built with Kotlin + Jetpack Compose + Hilt + JNI. A complete port of the NEVO desktop client preserving all functionality — server connections, channel management, real-time chat, voice calls, and screen sharing.

## Features

- 🔌 **Server Connection** — Multi-server config, TLS encryption, heartbeat keep-alive, auto-reconnect
- 📢 **Channel Management** — Tree channel list, voice/text channel separation, collapsible sub-channels
- 💬 **Real-time Chat** — Channel chat, emoji picker, message persistence (Room), scroll loading
- 🎙️ **Voice Calls** — Opus-based PTT/VAD engine, mute/deafen controls, libsodium XChaCha20-Poly1305 encryption
- 📺 **Screen Sharing** — H.264 encoding + NAL fragmentation over UDP
- 🌐 **Multi-language** — English / 简体中文 / 繁體中文
- 🎨 **Material Design 3** — Light/Dark/System theme with adaptive icons
- 🔄 **OTA Updates** — GitHub Releases API check → download → install via FileProvider

## Tech Stack

| Layer | Technology |
|-------|------------|
| **UI** | Jetpack Compose + Material Design 3 |
| **Architecture** | Clean Architecture (UI → ViewModel → UseCase → Repository) |
| **DI** | Hilt (Dagger) |
| **Database** | Room (SQLite) |
| **Network** | OkHttp + custom TCP/UDP protocol |
| **Encryption** | libsodium (XChaCha20-Poly1305) via JNI |
| **Audio** | Opus codec via JNI |
| **Video** | MediaCodec (H.264) |
| **Navigation** | Compose Navigation |
| **State** | StateFlow + collectAsState |
| **Build** | Gradle 8.11.1 + AGP 8.7.3 + KSP |

## Requirements

- **Min SDK**: Android 8.0 (API 26)
- **Compile SDK**: 35
- **Recommended**: 4 GB+ RAM

## Build

### Prerequisites

1. **JDK 17** (Amazon Corretto recommended)
2. **Android SDK** (platform 35, build-tools 35.0.0)
3. **NDK 27.0.12077973** + **CMake 3.22.1**

### Configure

Create `local.properties`:

```properties
sdk.dir=C\:\\Android
```

### Compile

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (signing config required)
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Protocol Compatibility

Byte-level compatible with NEVO C++ server and Python client:

- **TCP Frame**: 12-byte header — `payloadLength(4) + messageType(4) + requestId(4)`
- **Serialization**: `NevoBuffer` (ByteBuffer little-endian) matches `nevo_wire.py` byte-for-byte
- **Encryption**: XChaCha20-Poly1305 AEAD voice encryption matching `voice_crypto.py`

## OTA Updates

Built-in update mechanism via GitHub Releases:

```
Settings → About → Check Updates
     ↓
GitHub API /repos/TNEllya/nevo-android/releases/latest
     ↓
Version compare → Download APK → FileProvider install
```