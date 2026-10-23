<div align="center">

# Nodyssey iOS 改版

[NodeSeek](https://www.nodeseek.com/) 的非官方客户端 [Nodyssey](https://github.com/5151561/nodyssey) 的 iOS 改版，在原项目基础上修了 iPhone 上的问题，并加了几个小功能。

[![Release](https://img.shields.io/github/v/release/youyou0214OK/nodyssey-wa?include_prereleases&label=Release&color=2C6E7F)](https://github.com/youyou0214OK/nodyssey-wa/releases)
[![iOS](https://img.shields.io/badge/iOS-16.0%2B-000000?logo=apple&logoColor=white)](https://github.com/youyou0214OK/nodyssey-wa/releases)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue)](LICENSE)

<img src="docs/screenshots/post-list-light.png" width="200" alt="帖子列表" />
<img src="docs/screenshots/post-detail-dark.png" width="200" alt="帖子详情" />
<img src="docs/screenshots/notifications-light.png" width="200" alt="消息通知" />
<img src="docs/screenshots/profile-light.png" width="200" alt="我的" />

</div>

> 这是个人维护的改版，**不是原作者发布的版本**。改版的问题请在本仓库反馈，不要去打扰原作者。

## 下载与安装

到 [Releases](https://github.com/youyou0214OK/nodyssey-wa/releases) 下载 `.ipa`。

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

**新增**

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

- [5151561/nodyssey](https://github.com/5151561/nodyssey)：原项目，本改版的全部基础
- [tyrad/nodeseek](https://github.com/tyrad/nodeseek)、[mrzhiin/seekmate](https://github.com/mrzhiin/seekmate)：原项目致谢的参考实现

## 说明

本项目与 NodeSeek 官方无关，是社区自制客户端。仓库中不保存任何 Cookie 或凭据。

## 许可

与原项目相同，[GPL-3.0](LICENSE)。本仓库就是对应 ipa 的完整源码。
