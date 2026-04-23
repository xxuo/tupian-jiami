# 图片加密

一款基于 Fisher-Yates 洗牌算法的 Android 图片加密工具。

## 功能特点

- 🔐 **像素级加密**：使用 Fisher-Yates 算法对图片像素位置进行置乱
- 🔑 **密码保护**：支持自定义密码加密
- 🖼️ **即时预览**：加密/解密结果实时显示
- 📐 **分辨率显示**：自动显示图片分辨率信息
- 💾 **本地保存**：加密/解密后的图片保存到 Download 目录

## 加密原理

### 算法流程

1. **密码转种子**：将用户输入的密码转换为随机数种子
   ```
   seed = 0
   for char in password:
       seed = seed * 31 + char
   ```

2. **生成置换索引**：使用 Fisher-Yates 洗牌算法生成像素位置置换表
   ```
   for i from n-1 down to 1:
       j = random(0, i)
       swap(idx[i], idx[j])
   ```

3. **像素置乱**：根据置换索引重新排列像素位置
   - 加密：`out[idx[i]] = px[i]`
   - 解密：`out[i] = px[idx[i]]`

### 加密标记

加密后的文件会在末尾附加 `"ENCv1"` 标记，用于识别加密状态。

## 界面截图

| 加密前 | 加密后 |
|--------|--------|
| ![加密前](https://github.com/xxuo/tupian-jiami/raw/main/加密.png) | ![加密后](https://github.com/xxuo/tupian-jiami/raw/main/解密.png) |

## 使用方法

1. 点击"选择图片"按钮选取要处理的图片
2. 在输入框中输入加密密码
3. 按回车键执行加密/解密
4. 处理后的图片自动保存到 `/sdcard/Download/` 目录

## 文件说明

```
图片加密/
├── app/
│   ├── build.gradle          # 构建配置
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/img/enc/
│       │   └── MainActivity.java   # 主界面代码
│       └── res/
│           └── layout/
│               └── activity_main.xml   # 界面布局
├── 加密.png                  # 截图：加密界面
├── 解密.png                  # 截图：解密界面
└── README.md                 # 项目说明
```

## 技术细节

- **最低 SDK**: 19 (Android 4.4)
- **目标 SDK**: 29 (Android 10)
- **编译 SDK**: 29
- **Java 版本**: 1.8

## 安全性说明

⚠️ **注意**：本应用仅用于学习交流，加密强度有限：

- 仅对像素位置进行置乱，不改变像素值
- 统计特征保留（颜色分布不变）
- 建议使用更专业的加密工具保护重要图片

## 下载安装

从 [Releases](../../releases) 页面下载最新版本 APK。

## 开源协议

MIT License
