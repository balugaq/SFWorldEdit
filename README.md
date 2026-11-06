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

