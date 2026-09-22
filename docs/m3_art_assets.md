# M3 美術素材清單（夜結儀式＋寵物進食＋飼料）

> 2026-09-22 · 自繪 32px 暖像素、深描邊 `#6B4A2E`／`#3D2A18`、nearest-neighbor  
> 產生：`/tmp/pilvenv/bin/python scripts/gen_m3_art_assets.py`（需 Pillow）  
> **唔改**獎勵 domain／日結公式／防刷（固定 1 成長點／日；編輯唔再發獎）。

## 實際檔名（`app/src/main/res/drawable-nodpi/`）

### 夜結儀式

| 檔名 | 用途 |
|------|------|
| `ic_moon_settle.png` | 日結月亮 CTA／HUD |
| `ic_settle_stamp.png` | 結清印章 |
| `fx_settle_stamp.png` | 結算成功蓋章 FX（同章） |
| `settle_banner.png` | 「今日結清」橫幅 |
| `moon_rise_f0.png`～`moon_rise_f2.png` | 月亮升起 3 frame |

### 寵物／餵食

| 檔名 | 用途 |
|------|------|
| `pet_idle.png`／`pet_happy.png`／`pet_eat.png` | 閒置／開心／進食 |
| `fx_heart.png`／`fx_eat.png` | 互動心心／進食碎屑 |
| `item_feed.png` | 背包飼料袋 |

### 其他

| 檔名 | 用途 |
|------|------|
| `fx_sparkle_ready.png` | 可收成高亮 |

## Compose 接線

- `SettleOverlayContent`／`SettleScreen`：月亮升起 frame＋蓋章／橫幅／成長點
- `FarmSceneLayer`：`petFeedbackUntilMs` → `pet_eat`／`pet_happy`＋`fx_eat`／`fx_heart`
- 商店：買飼料＝支出入帳；`FarmLogic.feedPet` 扣 `InventoryItemKind.FEED`（唔扣成長點）

**唔動**：`DailySettlementLogic`／`RewardRules.DAILY_GROWTH_POINTS = 1`。
