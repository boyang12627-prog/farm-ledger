# M3 美術素材清單（夜晚日結儀式＋餵養）

> 2026-09-22 · 自繪 32px 暖像素、深描邊 `#6B4A2E`／`#3D2A18`、nearest-neighbor  
> 產生：`/tmp/pilvenv/bin/python scripts/gen_m3_art_assets.py`（需 Pillow）  
> **唔改**一日一結／改帳唔重派公式（固定 1 成長點／本地日一次）。

對齊產品開 M3 美術：夜晚日結月亮儀式（升起短條＋蓋章／橫幅）、寵物餵食可讀（idle／happy／eat＋飼料袋）、成熟高亮 FX。

---

## 實際檔名（`app/src/main/res/drawable-nodpi/`）

### 日結儀式

| 檔名 | 用途 |
|------|------|
| `ic_moon_settle.png` | 日結 CTA／浮層月亮 |
| `ic_settle_stamp.png` | 蠟封印章（今日結清） |
| `fx_settle_stamp.png` | 蓋章 FX（同章 alias） |
| `settle_banner.png` | 浮層橫幅（夜條＋月＋印章格） |
| `moon_rise_f0.png`～`moon_rise_f2.png` | 月亮升起 3 frame |

### 寵物／餵養

| 檔名 | 用途 |
|------|------|
| `pet_idle.png`／`pet_happy.png`／`pet_eat.png` | 小芽豬待機／開心／進食（升級深描邊） |
| `fx_heart.png`／`fx_eat.png` | 互動心心／進食碎屑 |
| `item_feed.png` | 背包飼料袋（商店買＝支出） |

### FX

| 檔名 | 用途 |
|------|------|
| `fx_sparkle_ready.png` | 作物成熟／收成高亮 |

---

## Compose 接線（本里程碑）

- `FarmScreen`「每日結算」鈕：`ic_moon_settle`
- `SettleOverlayContent`／`SettleScreen`：`moon_rise_f*` 升起＋`settle_banner`／`ic_settle_stamp` 蓋章儀式
- `FarmSceneLayer`：`petFeedbackUntilMs` → `pet_eat`／`pet_happy`＋`fx_eat`／`fx_heart`
- `PetScreen`：大圖 pet 態＋飼料餵食；商店買飼料＝支出入帳
- 田格 READY 收成鈕：`fx_sparkle_ready`

**唔動**：`DailySettlementLogic`／`RewardRules.DAILY_GROWTH_POINTS = 1`（餵食改耗飼料、唔扣成長點、唔發獎）。

重新產生：

```bash
/tmp/pilvenv/bin/python scripts/gen_m3_art_assets.py
```

*文件：`docs/m3_art_assets.md`*
