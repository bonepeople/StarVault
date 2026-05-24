# StarVault

StarVault 是一款面向 Android 的本地密码与敏感信息管理应用，用于安全地保存账号、密码、2FA 密钥等凭据，并支持字段历史快照（例如密码变更记录）。

> 当前版本：**0.1.0**（早期开发阶段，部分界面仍为占位实现）

## 功能概览

- **加密存储**：凭据数据本地加密落盘，由主密码保护；解锁前不以明文暴露内容。
- **标签检索**：为记录设置标签，通过搜索或筛选标签快速定位目标条目。
- **变更记录**：字段修改保留历史快照，可查看过往取值及变更时间。

## 技术栈

- **语言**：Kotlin
- **UI**：Jetpack Compose（Material 3）+ Fragment 混用
- **架构**：`ViewModel` + `StateFlow` 页面状态
- **构建**：Gradle Kotlin DSL、Version Catalog（`gradle/libs.versions.toml`）
- **最低系统**：Android 7.0（API 24）
- **目标 SDK**：35

## 相关链接

- 仓库：[github.com/bonepeople/StarVault](https://github.com/bonepeople/StarVault)

## 许可证

尚未在仓库中附带 LICENSE 文件；使用前请以仓库最新说明为准。
