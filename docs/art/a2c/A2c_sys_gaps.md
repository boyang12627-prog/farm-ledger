# A2c_sys_gaps — 0.5.3-sys（工程已接入）

**狀態（2026-09-26）：** A2c 可組裝包已 wire 入 `0.5.3-sys`。  
底圖 `spring_ranch_base`、九熱區 idle／pressed／active、grass_layer_1..3、topbar／tabbar chrome、sheets（夾板／紙／浮標／空態）均已拷入 `res/drawable/`。

## 已接入

| 用途 | drawable |
|------|----------|
| 牧場底 | `spring_ranch_base` |
| 熱區×3 | `hotspot_{stem}_{idle\|pressed\|active}` |
| 漏記草 | `overlay_grass_layer_1..3` |
| 浮標木牌 | `floating_wood_sign` |
| 頂欄 | `topbar_season_stamp_spring`／`weather_*`／`streak_plaque`／`seed_coin_pouch` |
| 底欄 | `tab_*_{selected\|unselected}` |
| 紙材 | `entry_clipboard`／`ledger_paper`／`diary_paper`／`empty_state_illustration` |

錨點：`docs/art/a2c/hotspot_map.json`（同 a2 fx/fy）。

## 殘餘可選（非 blocker）

1. 各天氣依真實季節／氣象切換（而家預設 sunny＋時段字）。  
2. Tab 圖若要純 icon 無燒字 → A2d。  
3. 禁用態專用灰圖（而家 Compose 色矩陣＋alpha）。  
4. `source_sheets/` 唔進 APK（正確）。

## 唔好做

- 官方牧場物語／SoS 商標角色  
- 港幣同種子幣混圖欄  
