# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个 Java/Kotlin 工具库项目，提供了一套用于快速开发的核心组件，特别是 QBean 相关的查询构建和数据处理功能。

## 项目结构

项目采用 Gradle 多模块结构，包含以下核心模块：

- **core-qbean-api**: QBean 核心 API 定义，包括注解、接口和基础类
- **core-qbean-helper**: QBean JPA 集成和辅助工具，提供仓库基类和服务实现
- **core-qbean-processor**: 注解处理器，支持编译时代码生成（使用 KSP）
- **core-qbean-sample**: 使用示例
- **core-base-enums**: 基础枚举定义和工具类
- **core-base-enums-converter**: 枚举转换器实现
- **core-utils**: 通用工具类
- **bom**: Maven BOM（Bill of Materials）定义

## 常用命令

### 构建和测试
```bash
# 清理并构建整个项目
./gradlew clean build

# 发布到本地 Maven 仓库
./gradlew clean build publishToMavenLocal

# 运行测试
./gradlew test

# 构建特定模块
./gradlew :core-qbean-api:build
```

### 开发相关
```bash
# 编译 Java 源码
./gradlew compileJava

# 编译 Kotlin 源码
./gradlew compileKotlin

# 生成 JavaDoc
./gradlew javadoc
```

## 技术架构

### QBean 体系
项目的核心是 QBean 查询构建框架，提供了一套完整的查询、分页和数据处理解决方案：

1. **注解驱动**：通过 `@QBean`、`@QBeanCreator`、`@QBeanUpdater` 等注解定义数据模型
2. **编译时处理**：使用 KSP（Kotlin Symbol Processing）在编译时生成辅助代码
3. **JPA 集成**：通过 `core-qbean-helper` 模块提供与 Spring Data JPA 的深度集成
4. **类型安全**：利用 Kotlin 的类型系统提供类型安全的查询构建

### 关键设计模式
- **Repository 模式**：`BaseRepository` 和 `BaseExtRepository` 提供数据访问抽象
- **Service 层抽象**：`BaseQService` (Kotlin) 和 `AbstractQServiceImpl` 提供业务逻辑基类
- **Builder 模式**：通过 `QBeanCreator` 和 `QBeanUpdater` 实现对象构建

### 依赖管理
- 使用 Gradle Version Catalog（`gradle/libs.versions.toml`）管理依赖版本
- 通过 BOM 模块统一管理发布版本
- 基于 Spring Boot 和自定义 BOM 进行依赖管理

## 开发规范

### Java/Kotlin 混合开发
- Java 代码主要位于 `src/main/java` 目录
- Kotlin 代码位于 `src/main/kotlin` 目录
- 使用 Java 17 作为目标版本
- 所有文件使用 UTF-8 编码

### 版本管理
- 项目版本在根 `build.gradle` 中统一管理
- SNAPSHOT 版本用于开发，正式版本用于发布
- 当前版本：2.6.5

### 发布配置
项目支持发布到 Maven Central 和私有仓库，需要配置以下 Gradle 属性：
- `publishUsername`
- `publishPassword`
- `publishReleasesRepoUrl`
- `publishSnapshotsRepoUrl`

## 注意事项

1. 编译时需要启用注解处理器和 KSP
2. 修改 QBean 相关注解后需要重新编译以生成代码
3. 项目使用 Lombok，需要 IDE 支持
4. Kotlin 日志使用 kotlin-logging-jvm 库