<div align="center">

# Nodyssey iOS 改版

[Nodyssey](https://github.com/5151561/nodyssey) 是 [NodeSeek](https://www.nodeseek.com/) 的非官方客户端，这是它的 iOS 改版：修了 iPhone 上的问题，适配了 iPad，加了几个小功能。

[![Release](https://img.shields.io/github/v/release/eddielee214/nodyssey-wa?include_prereleases&label=Release&color=2C6E7F)](https://github.com/eddielee214/nodyssey-wa/releases)
[![iOS](https://img.shields.io/badge/iOS-16.0%2B-000000?logo=apple&logoColor=white)](https://github.com/eddielee214/nodyssey-wa/releases)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue)](LICENSE)

基于原版 v1.2.22 · 个人维护，不保证跟进原版更新

<table>
  <tr>
    <td align="center"><img src="docs/screenshots/ios-home-hot-button.png" width="200" alt="首页热榜按钮" /></td>
    <td align="center"><img src="docs/screenshots/ios-hot-list.png" width="200" alt="热榜" /></td>
    <td align="center"><img src="docs/screenshots/ios-svg-author-badges.png" width="200" alt="SVG 图片和作者标签" /></td>
    <td align="center"><img src="docs/screenshots/ios-callout.png" width="200" alt="提示框" /></td>
  </tr>
  <tr>
    <td align="center"><b>新增</b> 头像旁的火苗按钮</td>
    <td align="center"><b>新增</b> 热榜：实时 / 日榜 / 周榜</td>
    <td align="center"><b>修复</b> IP 报告 SVG 不再是黑块<br><b>新增</b> 注册天数 · 等级 · 风险</td>
    <td align="center"><b>新增</b> 和网页一样的彩色提示框</td>
  </tr>
</table>

<table>
  <tr>
    <td align="center"><img src="docs/screenshots/ipad-home.png" width="400" alt="iPad 首页：左边列表，右边帖子" /></td>
    <td align="center"><img src="docs/screenshots/ipad-hot.png" width="400" alt="iPad 热榜：左边热榜，右边帖子" /></td>
  </tr>
  <tr>
    <td align="center"><b>iPad</b> 左边列表，右边帖子，正在看的那条高亮</td>
    <td align="center"><b>iPad</b> 热榜里点开的帖子也在旁边显示</td>
  </tr>
</table>

<sub>截图都在 iOS 模拟器里拍的</sub>

</div>

> 这个改版**不是原作者发布的**，有问题请在本仓库提 issue，别去找原作者。只做 iOS，安卓请直接用[原版](https://github.com/5151561/nodyssey)。

## 下载与安装

到 [Releases](https://github.com/eddielee214/nodyssey-wa/releases) 下载 `.ipa`。

ipa **没有签名**，需要用你自己的 Apple ID 或证书签名后安装，例如：

- [Sideloadly](https://sideloadly.io/)：电脑连上 iPhone，拖入 ipa，填 Apple ID 即可安装（免费 Apple ID 签的 App 7 天后要重签）
- [AltStore](https://altstore.io/)
- 有开发者证书的话，可以用自己的证书和描述文件重签

## 相对原版的改动

**修复**

- 用语音输入（听写）时闪退
- 发帖、编辑个人简介时界面卡死后闪退
- 从通知打开帖子时，看不到刚发的新回复
- 上传的图片插在光标处，不再加到正文最后
- 编辑资料点保存后自动收起键盘，简介栏的回车键改为「完成」
- IP 质量报告这类 SVG 图片显示成黑块
- 私信对话页收不到新消息，要退出重进才看得到
- 快速滑到顶部后继续下拉不会刷新，要等列表停稳再拉一次

**新增**

- 适配 iPad：横屏时左边列表、右边帖子，点列表直接换右边的内容；「我的」分两栏，顶栏和内容对齐。同一个 ipa，装在 iPhone 上还是原来的样子
- 帖子和每层回复显示作者的注册天数、等级和风险提示
- 首页热榜（实时 / 日榜 / 周榜，数据来自第三方 bimg.eu.org），按钮放在头像旁边
- 切换 NodeSeek / DeepFlood 立即生效，不用重启 App
- 帖子里的提示框（`> [!note] 标题`）显示成和网页一样的彩色框

## 原版功能

- **浏览与互动**：发帖、评论、编辑；点赞 / 反对 / 投喂鸡腿、收藏、签到；投票帖阅读与投票
- **消息与社交**：通知与未读数、私信、关注 / 粉丝、用户空间
- **账号**：鸡腿与星辰流水、星辰转账与收款码、等级进度、账号设置
- **阅读体验**：离线缓存、已读标记、浏览历史与阅读位置、图片预览 / 保存 / 分享、六种图床上传
- **界面**：Material 3 Expressive；简体 / 繁體 / English 三语


## 致谢

- [5151561/nodyssey](https://github.com/5151561/nodyssey)：原项目，本改版就是在它上面改的
- [tyrad/nodeseek](https://github.com/tyrad/nodeseek)、[mrzhiin/seekmate](https://github.com/mrzhiin/seekmate)：原项目在致谢里列出的参考实现

## 说明

本项目与 NodeSeek 官方无关，是社区自制客户端。仓库中不保存任何 Cookie 或凭据。

## 许可

与原项目相同，[GPL-3.0](LICENSE)。本仓库就是对应 ipa 的完整源码。
