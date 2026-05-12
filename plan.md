# 项目规划

## 项目概述
- **App名称**: music-k27-qwen-code
- **包名**: app.music_k27_qwen_code
- **类型**: Android 本地音乐播放器
- **技术栈**: Kotlin + Jetpack Compose + Room + Media3 ExoPlayer

## 长期规划 (Long-term)
- 支持多种音频格式 (MP3, FLAC, AAC, WAV, OGG)
- 完善的播放列表管理与智能推荐
- 均衡器与音效调节
- 桌面小部件 (Widget)
- 睡眠定时器
- 多端数据同步 (可选)

## 中期规划 (Mid-term)
- 支持本地歌词搜索与下载
- 歌曲标签编辑 (ID3)
- 文件夹浏览模式
- 播放统计与听歌报告
- 主题换肤功能
- 耳机线控与蓝牙设备支持优化

## 短期规划 (Short-term)

### 版本 0.1.0 (已完成)
**目标**: 搭建项目框架，实现核心播放功能与基础UI

- [x] 创建 plan.md 项目规划
- [x] 搭建 Android 项目框架 (Gradle, Manifest, 包结构)
- [x] 实现数据层 (Room 数据库、数据模型、DAO)
- [x] 实现本地音乐扫描功能 (MediaStore)
- [x] 实现音乐播放服务 (MediaSessionService + ExoPlayer)
- [x] 实现首页UI (Jetpack Compose: 最近播放、歌单、歌曲列表)
- [x] 实现播放详情页UI (封面模式 + 歌词模式)
- [x] 实现"我的"页面UI
- [x] 实现歌词解析与同步显示
- [x] 配置 GitHub Actions CI/CD 流水线
- [x] 更新 README.md
- [x] 创建 Git Tag v0.1.0 发布

### 版本 0.1.1 (已完成)
**目标**: 修复GitHub Actions构建错误
- [x] 修复 Room 数据库缺少实体类导致的 KSP 编译错误
- [x] 创建 PlaylistSongMap、Favorite、RecentPlay 实体类
- [x] 更新版本号至 0.1.1
- [x] 通过 GitHub Actions CI 流水线

### 版本 0.1.2 (已完成)
**目标**: 重构优化存量代码，修复架构缺陷与潜在Bug
- [x] 修复 LazyColumn DSL 误用（ArtistsTab/AlbumsTab 的 forEach 改为 items）
- [x] 优化 MediaScanner 支持增量扫描，避免全量删除再插入
- [x] 修复 LyricParser 文件路径处理（无扩展名歌曲路径兼容性）
- [x] 优化 SharedPlayerViewModel 播放列表索引同步与状态管理
- [x] 修复 MiniPlayer 空状态显示与 Scaffold 底部栏布局顺序
- [x] 优化 MusicPlaybackService 通知配置，防止服务被系统杀死
- [x] 优化日志系统：添加文件轮转与单文件大小限制
- [x] 添加 Room 数据库 fallbackToDestructiveMigration 策略
- [x] 权限授予后自动触发音乐扫描
- [x] 代码清理：统一导入格式，移除魔法数字

### 版本 0.2.0 (已完成)
**目标**: 完善歌单管理与收藏功能

- [x] 自建歌单 CRUD（创建、重命名、删除歌单）
- [x] 歌单详情页（展示歌单内歌曲、播放歌单）
- [x] 收藏功能（收藏歌曲列表页、从收藏移除）
- [x] 最近播放记录（首页最近播放关联真实数据）
- [x] 播放队列管理（展示当前队列、从队列移除歌曲）
- [x] 添加歌曲到歌单（歌曲列表 More 菜单）
- [x] 更新版本号至 0.2.0
- [x] 通过 GitHub Actions CI 流水线
- [x] 创建 Git Tag v0.2.0 发布

### 版本 0.2.1 (已完成)
**目标**: 重构优化存量代码，修复架构缺陷，补充单元测试

- [x] 修复 SharedPlayerViewModel Flow 泄漏与生命周期问题
- [x] 提取可复用 UI 组件（统一歌曲列表项、最近播放项）
- [x] 移动 formatDuration 到 utils 模块，提取导航路由常量
- [x] 添加基础单元测试（ViewModel、Repository、工具类）
- [x] 为大 Composable 添加错误处理与日志
- [x] 修复单元测试编译与运行时错误（coVerify、Flow mock 时序）
- [x] 更新版本号至 0.2.1
- [x] 通过 GitHub Actions CI 流水线
- [x] 创建 Git Tag v0.2.1 发布

### 版本 0.3.0 (待规划)
**目标**: 优化播放体验与UI细节
- 通知栏播放器
- 锁屏控制
- 音频焦点处理
- 随机播放与循环模式
