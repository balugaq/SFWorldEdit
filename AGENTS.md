# AGENTS.md — SFWorldEdit 操作宪章

> 本文件是 AI 编程助手与本仓库开发者的**操作契约**。所有改动（代码、构建、文档）均须遵守本文约定；当本文与代码冲突时，以代码现状为准并同步修订本文。

## TL;DR（太长了不看）

1. 本仓库是 **Paper/Spigot + Slimefun4 附属插件**（Gradle Kotlin DSL、Java 21 编译、Java ≥ 17 运行），默认分支 `main`；改动必须通过 `./gradlew clean build` 后才能提交。
2. 玩家可见文案一律走 `lang/*.yml` + `LocalizationService`；日志一律走 `Debug.*`；**禁止** `System.out.println`、**禁止**改动 `SFW_WORLDEDITOR` 等 Slimefun ID、**禁止** `git push -f`、**禁止**绕过构建失败（`-x test` / `--offline`）。
3. 依赖只在 `gradle/libs.versions.toml` 声明；`MetaLib` / `DisplayModelLib` 因源仓库下线由 `lib/*.jar` 提供，不要删。
4. 提交权归开发者本人；助手只把工作区留到可验收状态，不提交、不推送、不改写 git 历史。

---

## 1. 项目身份与边界

| 维度 | 事实 |
| --- | --- |
| 定位 | 面向 Slimefun 方块与 BlockData 的**区域编辑器**：选区、批量放置/克隆/清空、BlockMenu 槽位读写、撤销、YAML 投影与 Display 实体预览 |
| 入口类 | `com.balugaq.sfworldedit.implementation.SFWorldedit` → `extends ISFWorldEdit implements Listener`；`ISFWorldEdit extends JavaPlugin implements SlimefunAddon` |
| 插件名 | `SFWorldEdit`（`plugin.yml`），`api-version: 1.20`，`depend: [Slimefun]`，`softdepend: [GuizhanLibPlugin]` |
| 坐标 | group `com.balugaq.sfworldedit`，version 由 `gradle.properties` 的 `version=` 提供（当前 51） |
| 版本目标 | 源码最低 Java 16（record / `Stream.toList()` / 文本块 / 模式匹配）；运行时门禁 Java ≥ 17；编译与 CI 用 Java 21；兼容矩阵 `1.20.1`、`26.2` |
| 仓库 | `github.com/balugaq/SFWorldEdit`，默认分支 `main` |
| AI 职责 | 功能代码、Bug 修复、构建/CI 维护、文档维护 |
| AI 不负责 | 发布版本、推送远端、改写 git 历史 |

---

## 2. 包地图（改代码前先定位）

| 包 | 职责 | 关键类 |
| --- | --- | --- |
| `api/plugin` | 插件抽象基类 | `ISFWorldEdit`（`getString` / `send` / `sendList`） |
| `api/objects` | 命令与请求抽象 | `SubCommand`(`TabExecutor`)、`CachedRequest`、`enums.MinecraftVersion`（record）、`enums.Facing` |
| `api/data` | 数据模型 | `Content`(抽象 + `action()`)、`BukkitContent`、`SFContent`、`ChunkData`(record)、`Backup`(撤销栈)、`Language` |
| `core/commands` | 20 个命令实现 | `PasteCommand`、`ClearCommand`、`CloneCommand`、`SaveFileCommand`、`LoadFileCommand`、`UndoCommand` 等 |
| `core/managers` | 生命周期管理 | `IManager`(`onLoad`/`onUnload`)、`CommandManager`、`ConfigManager`、`DisplayManager` |
| `core/services` | 本地化 | `LocalizationService` |
| `implementation` | 装配与 Slimefun 物品 | `SFWorldedit`、`SFWItemsSetup`、`slimefun.SFWItemGroups` / `SFWItems` / `SFWRecipes` / `items.SFWorleditor` |
| `utils` | 工具类 | `WorldUtils`(编辑核心)、`Debug`、`Constants`、`ReflectionUtil`、`ClipboardUtil`、`YamlWriter`、`TextUtil` |

物品注册入口：`SFWItemsSetup.setup()`；工具 `SFW_WORLDEDITOR` 的 `ToolUseHandler` 左键设 pos1、`ItemUseHandler` 右键设 pos2，配方类型为 `RecipeType.NULL`。

---

## 3. 技术栈与构建

- **版本识别**：`MinecraftVersion` 采用 NetworksExpansion 的 `record (major, minor, patch)` 实现，由 `Bukkit.getMinecraftVersion()` 字符串解析，不枚举版本常量，26.x 天然可识别；约定常量为 `V1_20` / `V1_20_5` / `V1_21` / `V1_21_3` / `V1_21_4`，`UNKNOWN` = `(999,999,999)`。调用 `current()`、`isAtLeast(...)` / `isBefore(...)`、`humanize()`；判断 UNKNOWN 用 `equals(UNKNOWN)`（record 不能用 `==`）。
- **构建**：`build.gradle.kts`（Gradle Kotlin DSL）+ `com.gradleup.shadow`；`defaultTasks("clean", "build")`；产物 `build/libs/SFWorldEdit-<version>.jar`（fat jar，relocation `org.bstats` → `com.balugaq.sfworldedit.bstats`，`minimize()`，排除 `META-INF/*`）。
- **依赖**：全部声明在 `gradle/libs.versions.toml`，`build.gradle.kts` 只用 `libs.xxx` 引用。`compileOnly` 对应原 Maven `provided`，`implementation` 对应会被 shade 的 `compile`。
- **version 注入**：`processResources { filesMatching("plugin.yml") { expand("version" to project.version.toString()) } }`，因此 `plugin.yml` 里必须写 `version: ${version}`。**不要改回 Maven 的 `${project.version}`**，否则 jar 内版本号是字面量，Bukkit 加载失败，且兼容性 workflow 从 jar 取版本号的检查会失效。
- **本地 JAR**：`org.metamechanists:MetaLib:46` 与 `DisplayModelLib:3` 的原仓库 `repo.metamechanists.org` 已全站 404，改由 `implementation(fileTree("lib"))` 提供。`.gitignore` 有 `!lib/*.jar` 例外，勿删这两个文件。
- **仓库**：`mavenCentral()`、`repo.papermc.io`、`jitpack.io`、`hub.spigotmc.org`。`net.guizhanss:GuizhanLibPlugin` 在 **Maven Central**，不需要额外仓库。
- **CI**：`.github/workflows/build.yml`（JDK 21，`./gradlew test` + `clean build`）、`release.yml`（`gradle.properties` 版本变更自动发 Release）、`test-compatibility.yml`（矩阵 `1.20.1,26.2`，预装 JDK 21/25，按 Paper API 的 `java.version.minimum` 切换）。提交信息以 `[ci skip]` 开头可跳过 build。
- **Dependabot**：`yearly`（GitHub Docs 标注该值仅 GHES 3.19+ 明确支持；若 github.com 不识别，等价写法为 `cron` + `cronjob: "0 0 1 1 *"`）。

---

## 4. 启动顺序（不可打乱）

`SFWorldedit.onEnable()`：

1. `Preconditions.checkArgument(instance == null)` → `instance = this`
2. `saveDefaultConfig()` → `new ConfigManager(this)` → `configManager.onLoad()`
3. `new LocalizationService(this)` → `addLanguage(configManager.getLanguage())` → `addLanguage(DEFAULT_LANGUAGE="zh-CN")`
4. `environmentCheck()`：`MinecraftVersion.current()` 解析 `Bukkit.getMinecraftVersion()`；MC 低于 1.16 或 `javaVersion < 17` → `disablePlugin(this)` 并 `return`；解析异常降级为 `UNKNOWN` 后只告警（`UNKNOWN` 被定义为最大版本，不阻止启动）
5. `tryUpdate()`：`configManager.isAutoUpdate()` 且 `getDescription().getVersion().startsWith("Build")` 时才调用 `GuizhanUpdater.start(...)`（当前版本 `51` 不触发）
6. `SFWItemsSetup.setup(this)` —— 会触发 `Constants` 静态初始化，依赖 `getInstance()` 与 localizationService 已就绪
7. `new CommandManager(this)` → `onLoad()` → `registerCommands()`
8. `new DisplayManager(this)` → `onLoad()`
9. `Debug.info(getString("messages.startup.done"))`

`onDisable()`：`SlimefunItemUtil.unregisterAllItems()` → `displayManager.onUnload()` → `commandManager.onUnload()` → **先取告别语文案** → `localizationService = null` → 清理版本字段 → `configManager.onUnload()` → `Debug.info(message)` → `instance = null`。`Debug.*` 依赖 `getInstance()`，告别日志必须在 `instance = null` 之前打印。

---

## 5. 决策树：当遇到 A 时，执行 B

### 5.1 新增 / 修改命令

- 新建 `SubCommand` 子类放在 `core/commands/`，`getKey()` 返回与 `plugin.yml` 中 `sfw<key>` 对应的 key。
- 在 `CommandManager.onLoad()` 注册；若需按配置开关（如 `UndoCommand` 仅在 `allow-undo: true` 时注册），照现有条件分支写。
- 在 `plugin.yml` 补命令、别名（带前导 `/`）、权限节点 `sfworldedit.command.<key>`。
- **注意**：`SaveFileCommand` / `LoadFileCommand` 的 KEY 是 `saveFile` / `loadFile`（含大写 F），而 `plugin.yml` 声明为全小写；`PermissionUtil` 用 `key.toLowerCase()` 拼权限，别"顺手统一大小写"。

### 5.2 文案与语言文件

- 玩家可见文案写入 `src/main/resources/lang/zh-CN.yml` 与 `en-US.yml`，通过 `LocalizationService` 取。**禁止**在 Java 里硬编码中文（现状仅 `ClipboardUtil` 有 2 处，属历史债务）。
- `send()` / `sendMessage()` / `sendList()` 会**自动加 `messages.` 前缀**，调用方写相对键（如 `"error.no-permission"`）；`getString(path)` **不加前缀**，调用方必须写全路径（如 `"messages.startup.done"`）。
- 占位符是 `MessageFormat` 的 `{0}` / `{1}`，颜色用 `&a` 等 `ChatColor` 码；`${...}` 是 Gradle 占位符，只应出现在 `plugin.yml`。
- `Language` 加载时会把 jar 内默认配置的缺失键**补写**进用户文件，但不会覆盖已有值；新增键要同时改两份语言文件。

### 5.3 配置

- 新配置项加进 `src/main/resources/config.yml` 并在 `ConfigManager` 加带默认值的 getter（如 `getInt(path, default)`）；**禁止**在业务代码里散落 `getConfig().getXxx(...)` 魔法字符串。
- `ConfigManager.setupDefaultConfig()` 只补缺失键、不覆盖已有值；`config-version` 目前只写不读。
- `RuleCommand` 通过 `setConfig(path, value)` 热改限额，改动需保持其整数校验。

### 5.4 日志与异常

- 常规日志一律 `Debug.info/warning/severe`（底层走 `SFWorldedit.getInstance().getLogger()`）。
- 调试输出用 `Debug.debug(...)`，受 `config.yml` 的 `debug` 开关控制。
- 异常用 `Debug.trace(e)`；现状有 9 处裸 `e.printStackTrace()`，属于待收敛的历史债务，新增代码不得沿用。
- `Debug.*` 依赖静态 `instance`，不要在插件未启用 / 已禁用后调用。

### 5.5 世界编辑核心

- `WorldUtils.doWorldEdit()` 按 `ChunkData` 分组、按 `modification-chunk-limit-per-second` 分批 `runTaskLater`、按 `allow-undo` 收集 `Content` 备份；修改分批逻辑时先读懂这三段。
- 访问已注册 Slimefun 方块的持久化数据走 `StorageCacheUtils` / `SlimefunBlockData`，不要绕过存储层。
- `WorldUtils` 静态块通过 `ReflectionUtil` 按名反射 `CraftBlockState` 的 `data` / `position` / `world` / `weakWorld` 私有字段，并有 `success` 标志兜底。这是**运行时对 CraftBukkit 实现细节的耦合**，改 MC 版本支持时必须验证反射是否仍命中；源码没有编译期 NMS 依赖。
- `ClipboardUtil` 用 `player.spigot().sendMessage(...)`（BungeeChat），未使用 Adventure；换 API 时留意。
- 投影实体用 `org.bukkit.entity.Display` / `BlockDisplay`（1.19.4+）。

### 5.6 依赖与构建

- 新增依赖：先确认坐标可解析（POM/metadata 返回 200），再加进 `libs.versions.toml`，`build.gradle.kts` 用 `libs.xxx` 引用，跑 `./gradlew clean build`。
- 升级第三方库前用 `javap` 核对实际调用的 API 签名（已有先例：`GuizhanUpdater.start(Plugin, File, String, String, String)` 在 1.7.6 与 2.5.0 一致）。
- 区分 `compileOnly`（服务端提供）与 `implementation`（要打进 jar）。`bstats` 目前源码零引用、shade 后被 `minimize()` 剔除，可安全移除但不要擅自改。
- 构建失败时先读报错定位依赖/编译/shadow 冲突，**禁止** `-x test` / `--offline` 硬跳过。

---

## 6. 验收

1. `./gradlew clean build` 通过；产物在 `build/libs/`。
2. 检查 jar 内 `plugin.yml` 的 `version` 是真实版本号，不是 `${...}`。
3. CI 的兼容性测试（`1.20.1` 与 `26.2`）日志中出现 `Enabling SFWorldEdit`，且 `Disabling` 不出现在 `Done (` 之前。
4. `git status --short` 无意外改动；不提交、不推送。
5. 样式 / 视觉类改动由开发者目视确认，助手不得自行启动浏览器或渲染工具做验证。

---

## 7. 红线

- 不改 Slimefun 物品 ID（尤其 `SFW_WORLDEDITOR`）与 `plugin.yml` 的 `depend` / `api-version`，除非开发者明确要求。
- 不用 `System.out.println` 输出日志；不吞异常。
- 不擅自改 `onEnable` / `onDisable` 的初始化与销毁顺序。
- 不删 `lib/*.jar`、不删 Gradle wrapper、不把本机 `org.gradle.java.home` 写进 `gradle.properties`。
- 不提交、不推送、不 `git push -f`、不改写 git 历史。

---

## 8. 已知债务（改动时留意，不要当成"笔误"顺手改）

- `SFWorldedit` 构造器的自动更新 `branch = "master"`，而仓库默认分支是 `main`；当前版本号 `51` 不以 `Build` 开头，`tryUpdate()` 不会触发，暂无害。
- 若干本地化 key 在两份 lang 中都不存在（如 `messages.save-all`、`commands.clearprojectile.success`、`error.file-not-found`），会触发 `severe("No localization found for path: ...")` 并原样返回路径。
- `zh-CN.yml` 与 `en-US.yml` 不同步：中文版多出 `//srule`、`//sundo`、`//sconfirm` 等帮助行与若干 error 键；`-skipvanilla` 的短参数一个是 `-v` 一个是 `-s`。
- `bstats` 依赖源码零引用；`MetaLib` 无直接 import（作为 DisplayModelLib 的传递依赖被打进 jar）。
- `e.printStackTrace()` 尚存 9 处；Java 源码硬编码中文 2 处（`ClipboardUtil`）。
