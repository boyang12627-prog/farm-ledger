# 俯視農田場景素材（自繪 32px）

暖色像素農田下一階場景感用圖，全部以 `scripts/gen_farm_scene_assets.py`（PIL nearest-neighbor）自繪，**非**星露谷或其他第三方 tileset。

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
| `tile_grass.png` | 草地底 |
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

### 合成預覽

| 檔名 | 尺寸 |
|------|------|
| `farm_scene_preview.png` | 160×96 |

色板：奶油底、泥啡、鼠尾草／綠、磚紅、金黃、描邊 `#6B4A2E`。

## 建議 Compose 接法（三層）

唔改獎勵 domain；只係 UI 場景層。

```kotlin
// 偽碼：FarmSceneCanvas
Box(Modifier.fillMaxWidth().aspectRatio(160f / 96f)) {
    // 1) 底圖：Grid 或 Canvas 畫 tiles
    //    painterResource(R.drawable.tile_grass) / tile_dirt / tile_path / tile_dirt_edge_*
    Canvas(Modifier.matchParentSize()) { /* drawImage per cell */ }

    // 2) 圍欄層（同座標，z 較高）
    //    fence_h / fence_v / fence_corner_*

    // 3) 寵物 Sprite + 心心 FX
    //    Image(painterResource(R.drawable.pet_idle)) 或 pet_happy
    //    互動時短暫顯示 fx_heart（offset 喺寵物右上）
}
```

要點：

1. **底圖一層**：`LazyVerticalGrid`／自訂 `Grid`／`Canvas` 依地圖資料貼 tile。
2. **圍欄一層**：獨立 overlay，唔同底圖 bake 死，方便之後改佈局。
3. **寵物一層**：`Image`／`Sprite`；狀態切 `pet_idle` ↔ `pet_happy`，心心用 `AnimatedVisibility` 或短動畫。

現有 `FarmScreen` 仍可用 `tile_soil_empty`＋作物圖；本批係下一階俯視場景預留素材。
