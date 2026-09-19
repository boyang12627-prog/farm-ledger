# 第三方授權（佔位）

本文件為 MVP 佔位。正式發行前請以各依賴實際授權全文取代。

本專案建置可能使用（不完全列表）：

| 元件 | 典型授權 |
|------|----------|
| AndroidX / Jetpack Compose / Material | Apache License 2.0 |
| Kotlin / kotlinx.coroutines / kotlinx.serialization | Apache License 2.0 |
| Room / DataStore | Apache License 2.0 |
| JUnit 4 | Eclipse Public License 1.0 |
| Google Truth | Apache License 2.0 |
| Android Gradle Plugin | Apache License 2.0 |

請執行依賴授權彙總工具（例如 `./gradlew :app:dependencies` 後對照 Maven POM）並貼上完整 NOTICE／LICENSE 文字。

暖色像素風美術（含農田 tiles、圍欄、作物、CTA 圖示、寵物「小芽」、簡易屋、樹叢／陰影等 PNG）**全部自繪 © 專案**，以 `scripts/gen_pixel_assets.py`／`scripts/gen_farm_scene_assets.py`（PIL nearest-neighbor）產生，無外部 tileset／第三方素材。
