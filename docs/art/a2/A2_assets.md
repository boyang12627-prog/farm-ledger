# A2 Art Assets · 牧場手帳

北極星＝用戶概念冊 `docs/art/ref_user/`（page-03/04 牧場密度、page-05 木夾板）。  
技能對齊：`牧場UI代币` + `牧場手帳聖經`。  
A0／A1＝過渡扁平（Pillow Q 塊／頂視），**唔再**作為牧場主視覺。

## 檔案清單

| 檔案 | 尺寸（約） | 說明 |
|------|------------|------|
| `spring_ranch_iso.png` | 1080×1174 | 春季等距高密度主牧場（內容區）；可點物件、無搖桿 |
| `entry_clipboard.png` | 900×1354 RGBA | 木夾板入帳浮層：今日入帳、支出／收入／轉帳、HK$、分類格、帳戶、「記入牧場」＋印章 |
| `ledger_paper.png` | 1080×720 | 奶油木框帳簿紙；**HK$** 欄＋**種子幣**獨立欄 |
| `diary_cover.png` | 720×960 | 日記封面（奶油＋木） |
| `diary_page.png` | 900×1100 | 日記內頁罫線紙 |
| `overlay_missed_grass.png` | 同牧場 | RGBA 長草／黯淡；漏記層，**唔死／唔重置** |
| `a2_preview.png` | 合成預覽 | 左舊 A1 扁平｜中新 A2 牧場｜右跟 page-03/04；下：夾板＋帳簿＋日記 |
| `a2_vs_ref.png` | 對照 | 新牧場 vs page-04 概念裁切（開發大哥對照） |
| `A2_assets.md` | 本文件 | |
| `README.md` | 短說明 | |
| `compose_a2_sheets.py` | 合成備註腳本 | **唔**再生成扁平牧場 |

## UI 代币（牧場UI代币）

- 木框 `#6B4A2E`　奶油紙 `#F5E6C8`　墨線 `#3D2A1A`　印章紅 `#C45C4A`
- 底欄四 Tab：**牧場｜入帳｜帳簿｜日記**（可有中央＋）
- 入帳＝木夾板浮層；大金額／大鍵盤體驗；分類圖示 8–12；主按鈕 **「記入牧場」**＋橡皮章
- 目標入一筆 ≤8 秒；禁止銀行藍白表單

## 鎖定熱區（牧場手帳聖經）

| 分類 | 物件 | 預設類型 |
|------|------|----------|
| 飲食 | 餐桌／灶 | 支出 |
| 交通 | 小路／單車架 | 支出 |
| 住屋 | 門廊 | 支出 |
| 日用／購物 | 倉庫木箱 | 支出 |
| 娛樂 | 花圃／魚塘 | 支出 |
| 健康 | 藥草圃 | 支出 |
| 收入 | 郵箱／收成籃 | 收入 |
| 儲蓄 | 撲滿 | **轉帳**（唔預填收入） |
| 其他 | 告示牌 | 支出 |

概念冊 page-04 文案映射可不同；**產品以本表為準**。  
A2 `spring_ranch_iso` 已跟 page-04 密度（屋、路、郵箱、田、塘、動物、告示等）；單車／餐桌灶／藥草／撲滿等若未單獨入畫，實作仍按熱區座標對應本表，後續 GenerateImage 可補齊道具可讀性。

## 貨幣鐵則

- 真帳金額＝**港幣 HKD**（畫面顯示 `HK$`）
- **種子幣 ≠ HKD**；唔線性跟金額換算；獎「有記／記全／對帳／儲蓄／預算內」，唔獎有錢
- **禁止**把真帳金額顯示成「農場幣」
- 商店買賣只動種子幣／背包，唔寫真港幣帳

## 漏記

長草／等待 overlay（`overlay_missed_grass.png`）。作物動物唔死、農場唔重置。

## 禁止

官方牧場物語／Story of Seasons 商標角色或 Logo。原創作草帽＋藍吊帶農夫 OK。

## 來源說明（本批 A2）

Executor 子代理無 Cursor `GenerateImage`（無 cursor namespace／無 parent tool parity）。  
`spring_ranch_iso`／`entry_clipboard` 由 `ref_user/page-04`、`page-05` 電話牧場／夾板帶高品質裁切＋放大；帳簿／日記／長草／對照表用 Pillow **合成**（符合「Pillow 只合成、唔做主牧場」）。

## Regen（有 GenerateImage 時）

```
# Ranch — aspect 3:4 or 9:16, refs: page-03.png + page-04.png
Warm half-hand-painted isometric spring ranch, Story-of-Seasons vibe but ORIGINAL only.
HIGH density: blue-roof house+porch(住屋), dining table+stove(飲食), bike on dirt path(交通),
wooden crates(日用), flower garden+fish pond(娛樂), herb planters(健康), red mailbox+harvest basket(收入),
pink piggy bank(儲蓄), wooden signboard(其他), cow/chickens/crops/flowers/fence. Static original
straw-hat blue-overalls farmer OK. NO joystick, NO UI chrome, NO trademarks. ~1080 wide content.

# Clipboard — aspect 3:4, ref: page-05.png
Wooden clipboard floating overlay, cream paper, 「今日入帳」, 支出/收入/轉帳, large HK$ amount,
8–12 category icons mapping locked table, account pills, 「記入牧場」 + red ranch stamp.
Transparent/cropped panel OK. Big keypad feel. Warm wood/cream/ink/stamp tokens.
```

合成對照表可跑：`python compose_a2_sheets.py`（僅說明；重產請跟本表 prompts）。
