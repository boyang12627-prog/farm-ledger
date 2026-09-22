# M4 美術素材清單（商店／擴建＋全日生活可玩節點）

> 2026-09-22 · 自繪 32–48px 暖像素、深描邊 `#6B4A2E`／`#3D2A18`、nearest-neighbor  
> 產生：`/tmp/pilvenv/bin/python scripts/gen_m4_art_assets.py`（需 Pillow）  
> **唔改**一日一結／改帳唔重派公式（固定 1 成長點／本地日一次）。

對齊產品開 M4 美術：場景商店攤位＋攤主、擴建地／藍圖／小屋刷新、少量 decor、商店 UI 格、一日節奏 icon。

---

## 實際檔名（`app/src/main/res/drawable-nodpi/`）

### 商店

| 檔名 | 尺寸 | 用途 |
|------|------|------|
| `shop_stall.png` | 48×48 | 條紋遮棚攤位（主場景／overlay 頭圖） |
| `npc_vendor_idle.png` | 32×40 | 靜態攤主 |
| `shop_sign.png` | 32×32 | 掛牌（金幣標） |
| `building_shop_pole.png` | 32×40 | 精簡竿攤（CTA／文件別名） |
| `ic_shop_pole.png` | 32×32 | 商店 CTA 徽章 |

### 擴建

| 檔名 | 尺寸 | 用途 |
|------|------|------|
| `build_plot_empty.png` | 32×32 | 空擴建地（角樁＋虛線） |
| `building_hut.png` | 48×48 | 安家小屋（刷新＋窗下花箱） |
| `build_blueprint.png` | 32×32 | 藍圖剪影（佈置／擴建提示） |

### decor（少量）

| 檔名 | 用途 | `Decoration.id` |
|------|------|-----------------|
| `decor_lamp.png`／`decor_lantern.png` | 暖路燈／燈籠 | `lantern` |
| `decor_flowerbox.png` | 花箱 | `flowerbed` |
| `decor_sign.png` | 告示牌（通用 fallback） | well／bench／… |
| `decor_scarecrow.png` | 稻草人 | `scarecrow` |

### UI

| 檔名 | 用途 |
|------|------|
| `ui_shop_slot.png` | 商店商品格框 |
| `ui_price_tag.png` | 標價吊牌 |

### 一日節奏 icon

| 檔名 | 用途 |
|------|------|
| `ic_day_wake.png` | 起床／晨 |
| `ic_day_work.png` | 勞作 |
| `ic_day_shop.png` | 黃昏商店 |
| `ic_day_sleep.png` | 瞓覺過日 |

（夜結沿用 M3 `ic_moon_settle`。）

---

## Compose 接線（本里程碑）

- `DayLoopLogic`：起床→勞作→黃昏商店→夜結→瞓覺（純指引；唔改發獎）
- `FarmScreen`：一日生活橫幅＋時段 CTA；商店 overlay 用 `shop_stall`／`npc_vendor_idle`／`ui_shop_slot`／`ui_price_tag`
- `FarmSceneLayer`：`shop_stall`＋攤主熱區 → `onShopTap`；萌芽擴建地／藍圖；placed decor 對應 lamp／flower／sign／scarecrow
- `DecorScreen`：列表 icon
- 佈置鈕：`build_blueprint`／`build_plot_empty`

**唔動**：`DailySettlementLogic`／`RewardRules.DAILY_GROWTH_POINTS = 1`；睡覺／等待／買賣／佈置唔發獎。

重新產生：

```bash
/tmp/pilvenv/bin/python scripts/gen_m4_art_assets.py
```

*文件：`docs/m4_art_assets.md`*
