# WeekToDo | 开源极简主义周计划应用

![GitHub all releases](https://img.shields.io/github/downloads/zuntek/weektodoweb/total) 
[![vue3](https://img.shields.io/badge/vue-3.x-brightgreen.svg)](https://vuejs.org/)

WeekToDo 是一款免费开源的极简主义周计划应用，专注于隐私保护。通过待办事项列表和日历来安排您的任务和项目。支持 Windows、Mac、Linux 和在线使用。

![Logo](https://weektodo.me/weektodo-preview.webp)

## 🌟 主要功能

- **跨平台支持** - 支持 Windows、Mac、Linux 和 Web 版本
- **深色/浅色模式** - 支持主题切换
- **自定义待办列表** - 创建个性化任务列表
- **拖拽功能** - 支持任务拖拽排序
- **多语言支持** - 支持 15+ 种语言
- **子任务** - 支持任务分解
- **Markdown 支持** - 支持 Markdown 格式描述
- **可定制界面** - 灵活的界面配置
- **本地存储** - 数据完全存储在本地
- **任务颜色标记** - 支持任务颜色分类
- **任务时间设置** - 支持任务时间安排
- **重复任务** - 支持周期性任务
- **通知提醒** - 支持系统通知和提醒

## 🏗️ 项目架构

### 技术栈

- **前端框架**: Vue.js 3.x
- **状态管理**: Vuex 4.x
- **UI 框架**: Bootstrap 5 + Bootstrap Icons
- **日期处理**: Moment.js
- **国际化**: Vue i18n
- **构建工具**: Vue CLI + Webpack
- **桌面应用**: Electron 25.x
- **数据存储**: IndexedDB (浏览器本地数据库)

### 项目结构

```
src/
├── assets/                    # 静态资源
│   ├── languages/            # 多语言文件
│   ├── style/                # 全局样式
│   └── img/                  # 图片资源
├── components/               # Vue 组件
│   ├── layout/              # 布局组件
│   ├── comfirmModals/       # 确认对话框
│   └── *.vue                # 其他组件
├── views/                    # 页面视图
│   ├── toDoModal/           # 待办事项模态框
│   ├── welcome/             # 欢迎页面
│   └── *.vue                # 其他页面
├── store/                    # Vuex 状态管理
│   ├── modules/             # 状态模块
│   └── store.js             # 主存储文件
├── repositories/             # 数据访问层
│   ├── dbRepository.js        # IndexedDB 操作
│   ├── taskRepository.js      # 任务数据操作
│   └── *.js                   # 其他数据仓库
├── helpers/                  # 工具函数
│   ├── tasksHelper.js        # 任务相关工具
│   ├── notifications.js      # 通知功能
│   └── *.js                  # 其他工具
├── migrations/               # 数据迁移
└── background.js             # Electron 主进程
```

### 核心模块

#### 1. 任务管理系统
- **任务存储**: 使用 IndexedDB 存储任务数据
- **任务分类**: 支持创建和管理任务分类
- **任务看板**: 提供看板视图管理任务
- **任务关联**: 支持任务与待办事项关联

#### 2. 待办事项系统
- **日历视图**: 按日期组织的待办事项
- **自定义列表**: 用户可创建自定义待办列表
- **重复事件**: 支持周期性待办事项
- **数据同步**: 本地数据持久化存储

#### 3. 配置管理
- **主题设置**: 深色/浅色主题切换
- **界面布局**: 可调整的界面布局
- **语言设置**: 多语言支持
- **通知设置**: 灵活的通知配置

#### 4. Electron 集成
- **系统托盘**: 支持最小化到系统托盘
- **自动启动**: 开机自启动功能
- **本地通知**: 系统级通知提醒
- **窗口管理**: 多窗口和状态管理

## 🚀 快速开始

### 环境要求

- Node.js 16.x 或更高版本
- Yarn 包管理器

### 安装依赖

```bash
git clone https://github.com/manuelernestog/weektodo.git
cd weektodo
yarn install
```

### 开发模式

```bash
# 运行 Web 版本
yarn serve

# 运行 Electron 桌面版本
yarn electron:serve
```

### 构建项目

```bash
# 构建 Web 版本
yarn build

# 构建 Electron 应用
yarn electron:build

# 创建发布版本
yarn release
```

### Docker 支持

```bash
# 使用 Docker 运行开发环境
docker-compose up
```

## 📦 构建配置

### Electron 构建选项

项目在 `vue.config.js` 中配置了 Electron 构建选项：

- **应用ID**: `weektodo-app.netlify.app`
- **产品名称**: `WeekToDo`
- **发布平台**: GitHub Releases
- **Linux 支持**: deb、rpm、pacman、AppImage 格式
- **Windows 支持**: NSIS 安装程序
- **macOS 支持**: dmg、pkg 格式

### 数据库结构

应用使用 IndexedDB 存储数据，包含以下对象存储：

- **todo_lists**: 待办事项列表数据
- **repeating_events**: 重复事件配置
- **repeating_events_by_date**: 按日期索引的重复事件
- **tasks**: 任务数据
- **task_categories**: 任务分类数据

## 🔧 开发指南

### 代码规范

- 使用 ESLint 进行代码检查
- 遵循 Vue.js 3 组合式 API 规范
- 使用 SCSS 进行样式开发
- 组件化开发模式

### 国际化

支持语言文件位于 `src/assets/languages/`，新增语言的步骤：

1. 复制 `en.json` 文件
2. 重命名为对应语言代码（如 `zh-CN.json`）
3. 翻译内容并在 `languages.js` 中注册

### 数据存储

所有数据操作通过 `repositories` 层进行，提供统一的 API：

```javascript
// 示例：任务数据操作
import taskRepository from './repositories/taskRepository';

// 获取任务
taskRepository.get(taskId);

// 更新任务
taskRepository.update(taskId, taskData);

// 删除任务
taskRepository.remove(taskId);
```

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 项目仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

详细的贡献指南请查看 [CONTRIBUTING.md](./CONTRIBUTING.md)。

## 📝 许可证

本项目采用 GPL 许可证开源 - 查看 [LICENSE](./LICENSE) 文件了解详情。

## 👥 作者和贡献者

- **作者**: Manuel Ernesto Garcia
- **Logo设计**: hallgraph
- **翻译贡献者**: 查看 [关于页面](https://weektodo.me/about/)

## 💖 支持项目

WeekToDo 是 GPL 许可的开源项目，其持续开发完全依赖于用户的支持和赞助。如果您想支持我们，请考虑：

- [成为赞助商](https://weektodo.me/sponsor-us/)
- [进行捐赠](https://weektodo.me/support-us/)

## 🔗 相关链接

- **官方网站**: https://weektodo.me
- **GitHub 仓库**: https://github.com/manuelernestog/weektodo
- **问题反馈**: https://github.com/manuelernestog/weektodo/issues
- **更新日志**: https://weektodo.me/changelog

---

**⭐ 如果这个项目对您有帮助，请给我们一个 Star！**