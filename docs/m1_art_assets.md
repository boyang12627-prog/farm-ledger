# M1 美術素材清單（日狀態機＋空農場可過日）

> 2026-09-22 · 自繪 32px 暖像素、深描邊 `#6B4A2E`／`#3D2A18`、nearest-neighbor  
> 產生：`python3 scripts/gen_m1_art_assets.py`（需 Pillow）  
> **唔改**獎勵 domain／日結公式；時段染色只係 UI。

對齊產品 M1：日狀態機（晨→晝→昏→夜→睡覺過日）＋離線存檔；荒地場景可「等待／睡覺」過日。

---

## 實際檔名（`app/src/main/res/drawable-nodpi/`）

### 地形（已齊／刷新）

| 檔名 | 用途 |
|------|------|
| `tile_grass.png` | 通用草地 |
| `tile_dirt.png` | 泥／耕地 |
| `tile_path.png` | 沙石小徑 |
| `tile_dirt_edge.png` | 通用泥草邊 |
| `tile_dirt_edge_n.png` | 北緣 |
| `tile_dirt_edge_s.png` | 南緣 |
| `tile_dirt_edge_e.png` | 東緣 |
| `tile_dirt_edge_w.png` | 西緣 |
| `tile_grass_barren.png` | 荒地感草（稀疏泥斑；荒地場景角落） |

荒地場景：大面積 `tile_dirt`＋四角 `tile_grass_barren`（見 `FarmSceneLayer`）。

### 圍欄（已齊／刷新）

| 檔名 | 用途 |
|------|------|
| `fence_h.png` | 水平 |
| `fence_v.png` | 垂直 |
| `fence_corner_nw.png` | 西北 |
| `fence_corner_ne.png` | 東北 |
| `fence_corner_sw.png` | 西南 |
| `fence_corner_se.png` | 東南 |

### HUD

| 檔名 | 用途 |
|------|------|
| `ic_clock.png` | **新**・時段／日循環 HUD |
| `ic_growth_point.png` | **刷新**・成長點寶石（加深描邊） |

### 時段 overlay PNG（可選）

| 檔名 | 對應 `DayPhase` | ARGB（同 `DayPhaseLogic.phaseTintArgb`） |
|------|-----------------|------------------------------------------|
| `overlay_dawn.png` | `MORNING` 晨 | `0x33FFD27A` 金黃輕 |
| `overlay_day.png` | `NOON` 晝 | `0x00000000` 全透明 |
| `overlay_dusk.png` | `EVENING` 昏 | `0x44C46B3A` 橙紫 |
| `overlay_night.png` | `NIGHT` 夜 | `0x66202A5A` 藍紫 |

**預設接法（推薦）**：Compose `Color` tint，唔拉伸 PNG。

```kotlin
val tint = Color(DayPhaseLogic.phaseTintArgb(dayPhase))
// FarmSceneLayer：useOverlayDrawable = false（預設）→ Box.background(tint)
// 若要 drawable：useOverlayDrawable = true → Image(overlay_*)
```

| 時段 | Compose Color 建議寫法 |
|------|------------------------|
| 晨 | `Color(0x33FFD27A)` |
| 晝 | `Color.Transparent`／`Color(0x00000000)` |
| 昏 | `Color(0x44C46B3A)` |
| 夜 | `Color(0x66202A5A)` |

UI 浮層（記帳／結算 sheet、主 HUD）**唔跟**夜色變暗。

### Compose 接線

- `FarmSceneLayer(dayPhase, growthPoints, useOverlayDrawable)`
- 角落 mini HUD：`ic_clock`＋時段名；可選 `ic_growth_point`
- `FarmScreen` 頂欄：`ic_clock`／`ic_growth_point`／`ic_seed`

---

## 同 roadmap 對齊

產品語意 M1（日循環可玩）對應美術路線圖 **M0 最低可辨**＋時段 tint；美術里程碑「M1 日課可讀」嘅商店／玩家全身仍屬後續。

重新產生：

```bash
# venv 或系統 Pillow
python3 scripts/gen_m1_art_assets.py
# 全套場景（含 hut／樹／階段預覽）
python3 scripts/gen_farm_scene_assets.py
```

*文件：`docs/m1_art_assets.md`*
