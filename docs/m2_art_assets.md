# M2 美術素材清單（場內種收＋物品欄＋買賣入帳視覺）

> 2026-09-22 · 自繪 32px 暖像素、深描邊 `#6B4A2E`／`#3D2A18`、nearest-neighbor  
> 產生：`/tmp/pilvenv/bin/python scripts/gen_m2_art_assets.py`（需 Pillow）  
> **唔改**獎勵 domain／日結公式／防刷；只加 drawable＋可選 UI icon 接線。

對齊產品開 M2 美術：田格種收可讀、物品欄 stub 有圖、記帳本／買袋／賣籃有獨特 icon。

---

## 實際檔名（`app/src/main/res/drawable-nodpi/`）

### 作物（刷新・田格層）

| 檔名 | 用途 |
|------|------|
| `crop_wheat_seed.png` | 小麥・剛種 |
| `crop_wheat_grow.png` | 小麥・成長中 |
| `crop_wheat_ready.png` | 小麥・可收 |
| `crop_carrot_seed.png` | 蘿蔔・剛種 |
| `crop_carrot_grow.png` | 蘿蔔・成長中 |
| `crop_carrot_ready.png` | 蘿蔔・可收 |
| `crop_tomato_seed.png` | 番茄・剛種（加種） |
| `crop_tomato_grow.png` | 番茄・成長中 |
| `crop_tomato_ready.png` | 番茄・可收 |

### 工具／手

| 檔名 | 用途 |
|------|------|
| `ic_tool_hoe.png` | 鋤頭（種植／空地操作） |
| `cursor_hand.png` | 簡易手光標（選格／點擊） |
| `ic_tool_water.png` | 澆水壺（簡） |

### 記帳本

| 檔名 | 用途 |
|------|------|
| `ic_ledger_book.png` | 皮封面帳簿（關閉・主入口） |
| `ic_ledger_book_f1.png` | 翻頁 frame 1（開本左／右頁） |
| `ic_ledger_book_f2.png` | 翻頁 frame 2（頁略移） |

靜態用 `ic_ledger_book` 即可；短開本動畫可循環 f1→f2→book。

### 買賣／Toast

| 檔名 | 用途 |
|------|------|
| `ic_buy_bag.png` | 買料袋（支出入帳視覺） |
| `ic_sell_basket.png` | 賣菜籃（收入入帳視覺） |
| `ic_coin_plus.png` | ＋金額 toast（綠加號金幣） |
| `ic_coin_minus.png` | −金額 toast（紅減號金幣） |

### 物品欄（對應作物）

| 檔名 | 用途 | `InventoryItemKind` |
|------|------|---------------------|
| `item_seed_wheat.png` | 小麥種子包 | `SEED_WHEAT` |
| `item_seed_carrot.png` | 蘿蔔種子包 | `SEED_CARROT` |
| `item_seed_tomato.png` | 番茄種子包 | `SEED_TOMATO` |
| `item_harvest_wheat.png` | 收成小麥 | `CROP_WHEAT` |
| `item_harvest_carrot.png` | 收成蘿蔔 | `CROP_CARROT` |
| `item_harvest_tomato.png` | 收成番茄 | `CROP_TOMATO` |
| `item_material.png` | 材料箱 stub | `MATERIAL` |
| `ic_buy_bag.png` | 商店買種子 CTA | （買賣 UI） |
| `ic_sell_basket.png` | 商店賣收成 CTA | （買賣 UI） |

---

## Compose 接線（本里程碑）

- `FarmScreen`「記帳」：`ic_ledger_book`；「商店」：`ic_buy_bag`
- HUD 收成數：`ic_sell_basket`；物品欄橫列：`item_seed_*`／`item_harvest_*`
- 作物 `FilterChip` leading：`item_seed_*`
- 田格：種植 `ic_tool_hoe`、澆水 `ic_tool_water`、收成 `item_harvest_*`
- 商店 sheet：買列＋`ic_coin_minus`／`ic_buy_bag`；賣列＋`ic_coin_plus`／`ic_sell_basket`

**唔動**：`DailySettlementLogic`／`RewardRules` 結算一日一點／防刷時鐘（買賣只寫帳唔發獎）。

---

## 同 roadmap 對齊

產品「開 M2 美術・種收＋欄＋買賣視覺」對應路線圖 **M1 日課可讀**（作物階＋買袋／賣籃＋記帳本）＋物品欄 icon；路線圖「M2 擴建感」（樹花燈／第二寵物）仍屬後續。

重新產生：

```bash
/tmp/pilvenv/bin/python scripts/gen_m2_art_assets.py
```

*文件：`docs/m2_art_assets.md`*
