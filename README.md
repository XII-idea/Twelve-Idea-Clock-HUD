# 拾贰光尘时钟HUD (Twelve Idea Clock HUD)

一款面向 Minecraft（NeoForge）的纯客户端时钟 HUD 模组。基于
[Clock HUD](https://github.com/QKninja/ClockHUD) 迁移而来，迁移至 NeoForge 1.21.x 并进行了大量重写。

## 功能

- **昼夜时钟条**：在 HUD 上绘制带圆点滑轨的时钟条，太阳（白天）与月亮（夜晚）沿轨道指示当前时间。
  - 滑轨圆点按矢量绘制，任意 GUI 缩放下都保持正圆。
  - 滑轨带内边距，`dayTime = 0` 时太阳中心与滑轨首点对齐。
- **「第 N 天」计数**：每天清晨开始播放 3 秒动画；进入世界时也会立即显示一次当前天数。
- **按键开关**：默认按 `,` 键开关时钟显示（运行时切换，不写入配置）。
- **调试界面隐藏**：F3 调试界面打开时自动隐藏时钟 HUD（与 Jade 行为一致），可配置关闭。
- **配置界面**：位置、缩放、居中模式、天数显示、调试隐藏等均可在模组配置界面中调整，且随游戏语言本地化显示。

## 配置

配置以 CLIENT 类型注册，可在模组界面中编辑（Mods > 拾贰光尘时钟HUD > Config），
或直接编辑 `config/twelveideaclock-client.toml`。所有布尔选项会在注释中标注默认值。

| 配置项 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `showDayCount` | boolean | `true` | 每天开始时显示「第 N 天」计数 |
| `centeredClock` | boolean | `false` | 忽略 `xCoord`，将时钟锁定在屏幕中央 |
| `xCoord` | int | `2` | 时钟的起始 X 坐标 |
| `yCoord` | int | `2` | 时钟的起始 Y 坐标 |
| `scale` | double | `0.7` | 时钟的缩放比例 |
| `hideInDebug` | boolean | `true` | F3 调试界面打开时隐藏时钟 HUD |

## 安装

这是一个**纯客户端**模组：只需安装在客户端，**无需**安装到专用服务器（在服务器上也不产生任何效果）。

## 许可证

本模组采用 GNU Affero General Public License v3（或其后的任意版本）授权，详见 [LICENSE](LICENSE)。

原版 Clock HUD 代码由 Sam Beckmann 编写，采用 MIT 许可证授权，详见 [NOTICE](NOTICE)。
