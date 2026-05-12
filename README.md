# music-k27-qwen-code

一款基于 Android 的纯本地音乐播放器，使用 Kotlin + Jetpack Compose 构建，支持本地音乐扫描、播放、歌词显示与歌单管理。

## 功能特性

- **本地音乐扫描**：自动扫描设备存储中的音频文件（过滤时长 >30 秒的铃声/通知）
- **音乐播放**：基于 Media3 ExoPlayer 的高品质播放引擎，支持后台播放与通知栏控制
- **播放详情页**：封面模式与歌词模式切换，支持歌词同步滚动高亮
- **首页浏览**：最近播放、本地歌单、歌手/专辑分类浏览
- **我的页面**：个人资料、收藏统计、最近播放、自建歌单
- **数据持久化**：Room 数据库存储歌曲、歌单、收藏与播放记录
- **日志系统**：运行日志保存在外部存储目录，便于问题定位

## 技术栈

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose
- **架构**：MVVM
- **数据库**：Room
- **音频引擎**：Media3 ExoPlayer
- **构建工具**：Gradle + Kotlin DSL

## 版本历史

### v0.1.2
- 重构优化存量代码，修复架构缺陷与潜在 Bug
- 修复 LazyColumn DSL 误用（ArtistsTab/AlbumsTab）
- 优化 MediaScanner 支持增量扫描，避免全量删除再插入
- 修复 LyricParser 无扩展名歌曲路径兼容性
- 优化 SharedPlayerViewModel 播放列表索引同步与状态管理
- 修复 MiniPlayer 空状态显示与 Scaffold 底部栏布局
- 优化 MusicPlaybackService 通知配置，防止服务被系统杀死
- 优化日志系统：添加文件轮转与单文件大小限制
- 添加 Room 数据库 fallbackToDestructiveMigration 策略

### v0.1.1
- 修复 Room 数据库 KSP 编译错误（补充缺失的实体类）
- 完善数据模型：PlaylistSongMap、Favorite、RecentPlay

### v0.1.0
- 搭建项目框架与 CI/CD 流水线
- 实现本地音乐扫描与数据库持久化
- 实现音乐播放服务与通知栏控制
- 实现首页、播放详情页、我的页面基础 UI
- 实现歌词解析与同步显示

## 构建与运行

由于本地缺少 Android SDK，构建操作统一在 GitHub Actions 上执行。推送 Tag 后会自动触发 Release 构建并发布 APK。

```bash
# 本地开发（需自行配置 Android SDK）
./gradlew assembleDebug
```

## 许可证

MIT License
