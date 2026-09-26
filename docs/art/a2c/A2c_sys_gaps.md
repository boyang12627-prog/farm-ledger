# A2c_sys_gaps — 0.5.3-sys 美術缺口（給像素美術管家）

工程版本：`0.5.3-sys`。A2c 目錄（`hotspots/` `overlays/` `ranch/` `sheets/` `tabbar/` `topbar/`）而家**空**；船用 A1 精靈＋A2 紙感／iso。

## 已用（可暫用）

| 用途 | 現況素材 |
|------|----------|
| 牧場底圖 | `docs/art/a2/spring_ranch_iso.png` → `res/drawable/spring_ranch_iso` |
| 九熱區精靈 | `docs/art/a1/hotspot_*.png` → `res/drawable/hotspot_*` |
| 漏記草 | `docs/art/a2/overlay_missed_grass.png`（堆疊用同一張＋偏移；理想係分層草 tile） |
| 入帳夾板 | `entry_clipboard.png` |
| 帳簿／日記紙 | `ledger_paper.png` / `diary_cover.png` / `diary_page.png` |
| 底欄 icon | `nav_ranch/entry/ledger/diary.png` |

## 缺口（A2c 優先補）

1. **熱區四態精靈**（每分類 idle／pressed／active-badge／disabled）— 而家 Compose 縮放＋色矩陣代替。  
2. **獨立草層 tile**（1／2／3 疊）— 而家同一 `overlay_missed_grass` 偏移堆疊。  
3. **木名牌／浮標** 9-slice 或像素牌 — 而家 Compose 奶油木框。  
4. **頂欄木框 chip**（季節／天氣／連續／種子幣）— 而家 Compose `RanchTopBar`。  
5. **底欄木框選中態** — 而家 Compose `RanchBottomBar`＋nav icon。  
6. **入帳數字鍵鍵帽**、確認印章「記入牧場」— 而家 Compose 木鈕。  
7. **帳簿／日記空態插畫**（牧場靜物）— 而家紙底＋文案。  
8. 若 A2c 有更高清 iso／熱區分離層：請放 `docs/art/a2c/ranch/`、`hotspots/`，工程優先切換。

## 唔好做

- 官方牧場物語／SoS 商標角色  
- 把港幣金額畫成「農場幣」圖示混欄  
