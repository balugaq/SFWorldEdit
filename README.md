# SFWorldEdit

一个粘液科技附属，功能与创世神类似，但是新增了编辑粘液方块的能力

# 使用说明

> 你可以在粘液指南中找到`SFworldedit 物品组`  
> 在里面可以找到对应工具  
> 或者直接使用`/sf give <name> SFW_WORLDEDITOR`  
> 工具选区方式与创世神原版无异

| 全名 | 简写 | 参数 | 作用 |
| --- | --- | --- | --- |
| `/sfwhelp` | `//shelp` | `[子命令key]` | 显示命令列表 / 单条命令用法 |
| `/sfwversion` | `//sversion` | — | 显示插件名与版本 |
| `/sfwreload` | `//sreload` | — | 重载插件 |
| `/sfwpos1` | `//spos1` | — | 以**脚下**方块设为选区点 1 |
| `/sfwpos2` | `//spos2` | — | 以**脚下**方块设为选区点 2 |
| `/sfwclearpos` | `//sclearpos` | — | 清除自己的选区 |
| `/sfwpaste` | `//spaste` | `[sfid] [flags]` | 把指定 Slimefun 方块铺满选区 |
| `/sfwclone` | `//sclone` | `[flags]` | 把选区克隆到**你当前所站位置** |
| `/sfwclear` | `//sclear` | `[flags]` | 清空选区方块 |
| `/sfwblockinfoadd` | `//sblockinfoadd` | `<key> <value>` | 批量写 Slimefun 方块数据 |
| `/sfwblockinforemove` | `//sblockinforemove` | `<key>` | 批量删 Slimefun 方块数据 |
| `/sfwblockmenuslotset` | `//sblockmenuslotset` | `<slot>` | 把主手物品写入选区所有 BlockMenu 的指定槽 |
| `/sfwblockmenuslotfind` | `//sblockmenuslotfind` | `<slot>` | 统计该槽与主手物品相同的方块，导出不符坐标 |
| `/sfwundo` | `//sundo` | — | 撤销上一次区域操作 |
| `/sfwrule` | `//srule` | `<rule> <value>` | 修改两项性能规则并写入 config.yml |
| `/sfwsavefile` | `//ssavefile` | — | 把选区存成 `clones/clone<时间戳>.yml` |
| `/sfwloadfile` | `//sloadfile` | `<file_name> [-p]` | 读取存档并落到你当前位置 |
| `/sfwmove` | `//smove` | `[距离\|cancel]` | 移动 / 取消投影预览 |
| `/sfwclearprojectile` | `//sclearprojectile` | — | 清除投影实体并重置显示管理器 |

# 参数详解

## `//spaste` - 批量放置

| 参数 | 行为 |
| --- | --- | 
| `<sfid>` | 要填充的sf机器id |
| `-override/-o` | 覆盖目标位置已有的 Slimefun 方块数据 |
| `-force/-f` | 跳过 `NotPlaceable` 检查（可能生成无法正常工作的机器） |
| `-withoutblock/-w` | **只写入sf数据，不更改原版方块材质**
| `-prepare/-p` | 仅投影预览 |

示例：把选区铺满某机器并强制覆盖：

```text
//spaste <sfid> -o -f
```

## `//sclear` - 清空选区

```text
//sclear [-callhandler|-c] [-skipvanilla|-v] [-skipslimefun|-s]
```

| 参数 | 行为 |
| --- | --- |
| `-callhandler/-c` | 删除前调用该 Slimefun 方块的 `BlockBreakHandler.onPlayerBreak`（让机器自行掉落 / 清理能量等） |
| `-skipvanilla/-v` | 只清理 sf 数据 |
| `-skipslimefun/-s` | 保留 sf 数据，只清原版方块 |

## 方块数据管理

```text
//sblockinfoadd <key> <value>     # 对选区内所有 SF 方块 setData(key, value)
//sblockinforemove <key>          # removeData(key)
//sblockmenuslotset <slot>        # 把主手物品写进所有 BlockMenu 的指定槽
//sblockmenuslotfind <slot>       # 统计该槽与主手物品相同的方块
```

- `key` 的 Tab 补全来自你**准星 8 格内指向的方块**已有数据键，外加固定的 `energy-charge`。一般用于改动机器电量、进度等。
- `<slot>` 范围 `0~53`。
- `//sblockmenuslotfind` **不匹配**的坐标会生成 `failed-locations` YAML，通过一个「点击复制到剪贴板」的可点击消息发给你。适合排查「哪台机器的输入槽不对」。

## 选区保存与加载

> 类似投影

```text
//ssavefile                       # 无需参数，自动生成文件名
//sloadfile <file_name> [-p]
```

- 保存路径：`plugins/SFWorldEdit/clones/clone<System.currentTimeMillis()>.yml`。**文件名是时间戳**
- `<file_name>` 不带 `.yml` 后缀，例：`//sloadfile clone1730000000000`。
- `-p` 预览目前**只显示 `BukkitContent`**。

## 管理与配置

```text
//srule <rule> <value>            # 立即写入 config.yml
//sreload                         # 重载插件
//sconfirm                        # 执行待确认请求
//sclearprojectile                # 清理投影实体
```


# 环境要求

| 项 | 要求 |
| --- | --- |
| 服务端 | Paper / Spigot，`api-version: 1.20`；CI 在 **1.20.1** 与 **26.2** 上验证 |
| Java | 源码最低 Java 16，运行时要求 **Java ≥ 17**；本项目按 Java 21 编译 |
| 必需前置 | [Slimefun4](https://github.com/SlimefunGuguProject/Slimefun4) |
| 可选前置 | GuizhanLibPlugin（自动更新；仅当版本号以 `Build` 开头时启用） |

`MinecraftVersion` 采用 NetworksExpansion 的 record 实现，直接解析 `Bukkit.getMinecraftVersion()`，不枚举版本常量，因此 1.20.1 与 26.2 等新旧版本号都能正确识别。

# 配置

`config.yml` 默认值：

```yaml
auto-update: true
language: zh-CN
worldedit:
  modification-block-limit: 32768
  modification-chunk-limit-per-second: 16
  allow-undo: true
  max-backups: 20
config-version: 1
debug: false
```

语言文件在 `lang/zh-CN.yml` 与 `lang/en-US.yml`；缺失键会在启动时自动补写回磁盘，但**不会覆盖**已有值。`debug: true` 时调试日志才会输出。

# 构建

```bash
./gradlew clean build
```

产物 `build/libs/SFWorldEdit-51.jar`（fat jar）。依赖统一声明在 `gradle/libs.versions.toml`。

> MetaLib / DisplayModelLib 的原 Maven 仓库 `repo.metamechanists.org` 已下线，这两个库改由 `lib/*.jar` 提供并随 shadow 打包；仓库恢复后可改回坐标依赖。

CI 见 `.github/workflows/`：push 到 `main` 触发构建与兼容性测试；`gradle.properties` 里的版本号变更会自动创建 Release。

# 反馈与许可

问题反馈：[GitHub Issues](https://github.com/balugaq/SFWorldEdit/issues)。许可证见 [LICENSE](LICENSE)。
