# 俯視農田場景素材（自繪 32px＋decor）

暖色像素農田下一階場景感用圖，全部以 `scripts/gen_farm_scene_assets.py`（PIL nearest-neighbor）自繪，**非**星露谷或其他第三方 tileset。

**Style note:** Minecraft-inspired readability, 2D top-down — big flat color blocks, clear dark outlines, few mid-tones; **not** a 3D voxel/block world.

重新產生：

```bash
python3 scripts/gen_farm_scene_assets.py
```

輸出目錄：`app/src/main/res/drawable-nodpi/`  
放大對圖：`scripts/preview_assets/`（256px / 預覽 640×384）  
合成預覽：`docs/farm_scene_preview.png`（同 `farm_scene_preview.png`）

## 檔名清單

### 底圖 tiles（32×32）

| 檔名 | 用途 |
|------|------|
| `tile_grass.png` | 草地底（通用） |
| `tile_grass_barren.png` | 荒地：泥為主、稀疏草斑 |
| `tile_grass_sprout.png` | 萌芽：田格感強（邊框＋泥屑） |
| `tile_grass_home.png` | 安家：穩草地＋小花點 |
| `tile_grass_thrive.png` | 旺場：更綠更密 |
| `tile_dirt.png` | 耕地／泥地 |
| `tile_dirt_edge_n.png` | 泥地北緣（接草） |
| `tile_dirt_edge_s.png` | 泥地南緣 |
| `tile_dirt_edge_e.png` | 泥地東緣 |
| `tile_dirt_edge_w.png` | 泥地西緣 |
| `tile_dirt_edge.png` | 通用邊緣（等同 N，可旋轉／鏡像） |
| `tile_path.png` | 沙石小徑（可選） |

### 圍欄（32×32，透明底）

| 檔名 | 用途 |
|------|------|
| `fence_h.png` | 水平欄杆 |
| `fence_v.png` | 垂直欄杆 |
| `fence_corner_nw.png` | 西北轉角 |
| `fence_corner_ne.png` | 東北轉角 |
| `fence_corner_sw.png` | 西南轉角 |
| `fence_corner_se.png` | 東南轉角 |

### 寵物「小芽」與互動 FX（32×32，透明底）

| 檔名 | 用途 |
|------|------|
| `pet_idle.png` | 小芽閒置 |
| `pet_happy.png` | 小芽開心（互動後） |
| `fx_heart.png` | 互動心心 |

### Decor：簡易屋＋樹叢（透明底，尺寸不一）

| 檔名 | 尺寸 | 用途 |
|------|------|------|
| `building_hut.png` | 48×48 | 簡易屋（2D 俯視可讀正面＋屋頂；大方塊色＋深輪廓） |
| `building_hut_ruin.png` | 48×48 | 荒地殘破屋 stub（純視覺） |
| `tree_oak.png` | 32×40 | 圓冠橡樹（chunky canopy＋dark outline） |
| `tree_pine.png` | 32×40 | 層疊松樹（flat tiers＋dark outline） |
| `bush.png` | 24×20 | 小樹叢（可選） |
| `tree_shadow.png` | 32×16 | 樹腳硬像素陰影（可選，疊喺 decor 下） |

### 合成預覽

| 檔名 | 尺寸 |
|------|------|
| `farm_scene_preview.png` | 160×96（草泥＋圍欄＋小芽＋屋＋樹） |
| `stage_barren_preview.png` | 160×96 荒地場景 |
| `stage_sprout_preview.png` | 160×96 萌芽場景 |
| `stage_home_preview.png` | 160×96 安家場景 |
| `stage_thrive_preview.png` | 160×96 旺場場景 |
| `farm_stages_preview.png` | 344×244 四階對比（docs／drawable） |

對比圖亦見：`docs/farm_stages_preview.png`。

色板：奶油底、泥啡 `#A67C52`、鼠尾草／綠、磚紅／深橙屋頂、木色、描邊 `#6B4A2E`；decor 加深輪廓 `#3D2A18`（Minecraft-inspired readability, 2D top-down）。

## 階段 → drawable 對應（FarmStage）

由 `FarmStageLogic.capabilities(totalSettleDays)` 驅動；**唔改獎勵公式／防刷**，只係 UI 顯示／隱藏。

| 階段 | 門檻（結算日） | 地面 tile | Hut | Fence | Trees / bush | 寵物 |
|------|----------------|-----------|-----|-------|--------------|------|
| **荒地** `BARREN` | 0–2 | 大面積 `tile_dirt`＋四角 `tile_grass_barren` | `building_hut_ruin` | 唔顯示 | 無 | 0 |
| **萌芽** `SPROUT` | ≥3 | `tile_grass_sprout`＋泥床／`tile_dirt_edge_*` | 唔顯示 | 唔顯示 | 無（或極少） | 1（小芽） |
| **安家** `HOMESTEAD` | ≥7 | `tile_grass_home`＋泥床／edge | `building_hut` | 全套 fence_* | `tree_pine`＋可選 `bush` | 1 |
| **旺場** `THRIVING` | ≥14（二寵 ≥21） | `tile_grass_thrive` | `building_hut` | 全套 fence_* | pine＋oak＋多 `bush` | 1（≥21 → 2） |

### 建議 Compose 按 FarmStage 開關

`FarmSceneLayer` 已接 `FarmStageCapabilities`：

```kotlin
val stageGrass = when {
    capabilities.greenerDenser -> R.drawable.tile_grass_thrive
    capabilities.showHut -> R.drawable.tile_grass_home
    capabilities.showGrassEdges -> R.drawable.tile_grass_sprout
    else -> R.drawable.tile_grass_barren
}
// showHut / showFence / greenerDenser / animalSlots 直接控制 hut、trees、bush、第二寵物位
```

重新產生含階段預覽：

```bash
python3 scripts/gen_farm_scene_assets.py
```

## 暖泥 token（場景層＋六格田）

Compose：`Theme.kt`（`FarmMud*`／`FarmStroke`／`FarmPlot*`）。PNG：`gen_farm_scene_assets.py`＋`gen_pixel_assets.py` 共用同一填泥。

| Token | Hex | 用途 |
|------|------|------|
| `FarmMudHighlight` / `SOIL_L` | `#D9B48C` | 泥高光／場景→田格泥帶 |
| `FarmMudMid` / `SOIL_M` | `#C4966A` | 泥中調 |
| `FarmMudDark` / `SOIL_D` / `FarmSoil` | `#A67C52` | 泥底／種植鈕 |
| `FarmStroke` / `OUTLINE` | `#6B4A2E` | 場景＋田格卡描邊 |
| `FarmSand` / `FarmPlotEmpty` | `#F2E2C4` | 田格淺奶油卡底 |
| `FarmPlotGrowing` | `#E8DCC4` | 成長中卡底（暖奶油） |
| `FarmPlotReady` | `#F5E8D0` | 可收成卡底（暖高光奶油） |
| `FarmBg` / `CREAM` | `#FFF8E7` | 畫面奶油底 |

唔改獎勵／domain；只統一泥色同描邊令場景層同下面六格視覺黏合。

## 建議 Compose 接法（四層）

唔改獎勵 domain；只係 UI 場景層。

```kotlin
// 偽碼：FarmSceneCanvas
Box(Modifier.fillMaxWidth().aspectRatio(160f / 96f)) {
    // 1) 底圖：Grid 或 Canvas 畫 tiles
    //    painterResource(R.drawable.tile_grass) / tile_dirt / tile_path / tile_dirt_edge_*
    Canvas(Modifier.matchParentSize()) { /* drawImage per cell */ }

    // 2) 圍欄層（同座標，z 較高）
    //    fence_h / fence_v / fence_corner_*

    // 3) Decor 層（屋／樹／叢；可選 tree_shadow 先畫再畫主體）
    //    Image(painterResource(R.drawable.building_hut))
    //    Image(painterResource(R.drawable.tree_oak)) / tree_pine / bush

    // 4) 寵物 Sprite + 心心 FX
    //    Image(painterResource(R.drawable.pet_idle)) 或 pet_happy
    //    互動時短暫顯示 fx_heart（offset 喺寵物右上）
}
```

要點：

1. **底圖一層**：`LazyVerticalGrid`／自訂 `Grid`／`Canvas` 依地圖資料貼 tile。
2. **圍欄一層**：獨立 overlay，唔同底圖 bake 死，方便之後改佈局。
3. **Decor 一層**：屋／樹獨立擺位；`tree_shadow` 可選墊底；唔改獎勵／domain。
4. **寵物一層**：`Image`／`Sprite`；狀態切 `pet_idle` ↔ `pet_happy`，心心用 `AnimatedVisibility` 或短動畫。

現有 `FarmScreen` 仍可用 `tile_soil_empty`＋作物圖；本批係下一階俯視場景預留素材。
