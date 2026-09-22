# 像素版「牧場物語」美術方向＋素材分階草案

> farm-ledger（農帳養成）重開專案用 · 可交群組 · 2026-09-22  
> 目標：像素 OK；日循環／種餵收／商店／擴建 LOGIC 足；**記帳融入日課**（買＝支出、賣＝收入、夜晚日結過日）；**單一農場主世界**。  
> 現況對齊：`docs/farm_scene_assets.md`（32px 自繪暖泥場景）、`PLAYABLE_MILESTONE.md`。

---

## 0. 一句定位

**俯視／半俯視暖色像素農場**：Minecraft 式「大方塊＋深輪廓」可讀，星露／牧場物語節奏感，但**唔抄地圖密度**；記帳本同日結月亮係視覺主角之一，唔係外掛 UI。

---

## 1. 視覺支柱（Visual Pillars）

| 支柱 | 說明 |
|------|------|
| **視角** | 2D **俯視～半俯視**（頂＋一點正面可讀），唔做真 3D／體素世界 |
| **像素規格** | 底 tile **32×32**；建築／角色 **32～48** 高；UI icon **24～32**；nearest-neighbor 放大 |
| **輪廓可讀** | 少中間調、大色塊、深描邊（現有 `#6B4A2E`／`#3D2A18`）；遠看都認得出作物／按鈕 |
| **暖色板** | 奶油底 `#FFF8E7`、泥 `#A67C52`、鼠尾草綠、磚紅／深橙屋頂、木色——延續現有 FarmMud token |
| **時段染色** | **晨**（金黃 overlay 輕）、**晝**（標準）、**昏**（橙紫）、**夜**（藍紫＋燈點）——同一套 sprite，用 multiply／tint 換時段，唔為每時段重繪全圖 |
| **單一主世界** | 一個農場場景承載全部玩法；記帳／商店／日結＝**浮層／Modal**，唔跳大地圖 |
| **情緒** | 溫暖、慢活、帳本有儀式感；成功反饋用短 FX（心、金幣、月亮），唔閃光彈 |

**參考語氣（非素材來源）**：牧場物語日常節奏、星露谷暖色、Minecraft 2D 可讀輪廓。實際像素一律自繪或核實授權後先用。

---

## 2. 場景層級（由底到頂）

與現有四層 Compose 接法兼容，擴成七層概念：

| Z | 層 | 內容 | 備註 |
|---|-----|------|------|
| 0 | **地形 Terrain** | 草／泥／徑／田格 edge；階段變體 barren→thrive | 已有一批 tile |
| 1 | **作物 Crops** | 種／芽／成／熟；可收成高亮 | 對齊田格 LOGIC |
| 2 | **建築 Buildings** | 小屋、倉庫 stub、圍欄、擴建地基 | hut 已有；商店攤位可後補 |
| 3 | **動物 Animals** | 寵物 idle／happy；之後雞／牛等 | 現有小芽 |
| 4 | **NPC／商店** | 攤主或靜態攤位＋氣泡「買／賣」 | 第一期可靜態＋點擊開 sheet |
| 5 | **玩家 Player** | 農場主 4 向 walk／hoe／water／harvest（可極簡） | 可先「光標／手」代替全身 |
| 6 | **UI 浮層** | 記帳本、日結月亮、成長點、商店 sheet、toast | **產品核心交互** |

時段 tint 建議套喺 0–5；UI 層保持高對比唔跟夜色變暗到睇唔清。

---

## 3. 美術里程碑 M0–M3

原則：**先可玩閉環**（種餵收＋買支出／賣收入＋夜日結），再四季／室內。

### M0 — 可玩骨架（約 40–60 張／格）

最低可辨、對齊現有 drawable 思路。

| 類 | 最低清單 | 估數量 |
|----|----------|--------|
| 地形 | grass／dirt／path／dirt_edge×4；階段草 2～4 款 | ~12 |
| 圍欄 | h／v／4 corner | 6 |
| 建築 | hut（完整）＋可選 ruin | 1–2 |
| 作物 | 1 種作物 × 3～4 成長階＋可收高亮 | 4–5 |
| 動物 | pet idle／happy | 2 |
| FX | heart、短「＋錢／−錢」粒子可程式畫 | 1–3 |
| 玩家 | 可選：簡易手／工具 cursor（未做全身 OK） | 0–4 |
| **產品 UI** | **記帳本 icon**、**日結月亮 icon**、買／賣鈕、成長點 gem | **6–8** |
| 時段 | 4 色 overlay（程式 tint 即可，唔一定出 PNG） | 0–4 |

**M0 完成標準**：單一農場畫面能完成「記一筆→種／餵→賣→夜日結過日」，視覺唔再純色塊佔位。

### M1 — 日課可讀（約＋30–50）

| 類 | 清單 | 估＋ |
|----|------|------|
| 作物 | 再＋2 種作物成長階 | ~8–12 |
| 商店 | 攤位／櫃台 1、貨架 1、買料袋／賣菜籃 icon | ~6 |
| 玩家 | 4 向 idle＋1 耕作 pose（或工具 glow） | ~5–8 |
| 建築 | 倉庫／擴建地基 stub | 2–3 |
| 日循環 FX | 日出淡入、月亮升起短條、結算印章 | 3–5 |
| NPC | 1 靜態攤主（可無 walk） | 1–2 |

**M1 完成標準**：買＝支出、賣＝收入喺畫面上有**獨特 icon／短動畫**；夜晚日結有月亮儀式。

### M2 — 擴建感（約＋40–70）

| 類 | 清單 | 估＋ |
|----|------|------|
| 階段場景 | barren／sprout／home／thrive 差異加深（樹、花、燈） | ~15–25 |
| 動物 | 第二寵物位＋1 新種 idle／eat | ~4–6 |
| 裝飾 | 路燈、花箱、告示牌等可解鎖 decor | ~8–12 |
| 商店 UI | 商品格子框、標價條、缺貨灰態 | ~5 |
| 音訊可選 | 唔屬美術，但可並列 SFX 清單 | — |

**M2 完成標準**：結算日解鎖「睇得出農場變旺」；仍係單圖主世界。

### M3 — 四季／室內（延後）

| 類 | 清單 | 估＋ |
|----|------|------|
| 四季 tint／少量季節 decor（落葉、雪頂） | 每季 5–10 decor | ~20–40 |
| 室內小屋 1 房（地板／牆／傢俱 6–10） | | ~15–25 |
| 更多作物／動物種 | | 視 LOGIC |

**明確延後**：大地圖、多村莊、星露級物件密度、全角色 walk cycle 精品化。

---

## 4. 同產品日循環對齊（要獨特動畫／icon）

記帳唔係設定頁，係**日課動作**。以下必須同一般「種地／餵食」視覺有區隔：

| 動作 | LOGIC | 美術要求 | 建議表現 |
|------|--------|----------|----------|
| **開記帳本** | 記收入／支出／無交易 | **獨特 icon＋開本短動畫** | 皮封面帳簿、翻頁 2～3 frame；入口永遠喺主世界可見 |
| **買料** | 支出入帳 | **買料袋／錢飛出** | 商店確認時：袋＋紅色 −金額 toast |
| **賣菜／產物** | 收入入帳 | **菜籃／金幣飛入** | 綠色 ＋金額；可同收成動作銜接 |
| **餵食／互動** | 花費成長點等 | 沿用 heart／eat（可共用 FX） | 唔同買賣搶視覺權重 |
| **夜晚日結** | 結算過日、發成長點 | **日結月亮（主角級）** | 月升起＋帳簿蓋章／「今日結清」；過場後晨 tint |
| **過日** | 日＋1、作物推進 | 短 fade 晨→或月→晨 | 2 秒內；唔切大地圖 |

實作提示：M0 可用 **靜態 icon ＋ Compose 動畫**；M1 再補 2～4 frame sprite。

---

## 5. 授權方針

| 優先 | 做法 |
|------|------|
| **1. 自繪** | 延續 `scripts/gen_*_assets.py`／手繪像素；風格註明自有 |
| **2. CC0／明確可商用** | 僅作**輔助參考或核實後**填洞；每份記入 `THIRD_PARTY_LICENSES.md`（作者、連結、授權全文／摘要、使用範圍） |
| **3. 唔做** | 未核實授權嘅 itch／Pinterest／星露／牧場物語／Minecraft 官方贴圖；「看起來像 CC0」唔算數 |

可列作**搜尋方向**（使用前必須逐項核實，本文**不假定**已授權）：

- 標明 **CC0** 嘅像素 tileset／UI pack（OpenGameArt、部分 itch CC0 專頁、Kenney 類明確 CC0 包）
- 自有色板重繪：只借「格數／視角」唔直接貼原檔

上架前：美術＋依賴授權表要齊；唔齊就唔好當最終素材。

---

## 6. 明確唔做（第一期）

- **3D**／真體素／Unity 風農場  
- **星露谷級大地圖密度**、多區域無縫、百種物件搶工期  
- 多村莊社交、結婚線、節日大地圖（可當遙遠願望）  
- 為每個時段重繪全套地圖（用 tint）  
- 改 Kotlin 獎勵公式／防刷（美術文檔範圍外）  
- 未核實授權素材直接進 `res/`

---

## 7. 同現有 repo 嘅銜接

| 已有 | 下一步美術 |
|------|------------|
| `tile_*`／`fence_*`／`building_hut*`／`tree_*`／`pet_*` | 歸入 M0 地形／建築／動物 |
| FarmMud／FarmStroke token | 新素材跟同一色板 |
| `FarmSceneLayer` 分層 | 加作物層、商店熱區、UI 浮層 |
| 記帳 ModalBottomSheet | 換記帳本視覺殼，唔改 domain |
| 階段 BARREN→THRIVING | M2 加深差異，門檻邏輯不動 |

---

## 7b. M1 實際素材（2026-09-22 開工）

產品 M1＝日狀態機＋離線存檔＋空農場可過日。美術最低包已落檔，詳見 **`docs/m1_art_assets.md`**。

| 類 | 實際檔名 |
|----|----------|
| 地形 | `tile_grass`／`tile_dirt`／`tile_path`／`tile_dirt_edge`＋`_n/_s/_e/_w`／`tile_grass_barren` |
| 圍欄 | `fence_h`／`fence_v`／`fence_corner_{nw,ne,sw,se}` |
| HUD | `ic_clock`（新）、`ic_growth_point`（刷新） |
| 時段 | **Compose tint**（`DayPhaseLogic.phaseTintArgb`）；可選 `overlay_{dawn,day,dusk,night}.png` |
| 接線 | `FarmSceneLayer(dayPhase, growthPoints)`；預設 Color tint |

產生腳本：`scripts/gen_m1_art_assets.py`。

---

## 7c. M2 實際素材（2026-09-22 開工・種收＋欄＋買賣視覺）

產品開 M2 美術＝場內種收可讀＋物品欄 stub 有圖＋記帳本／買袋／賣籃獨特 icon。詳見 **`docs/m2_art_assets.md`**。

| 類 | 實際檔名 |
|----|----------|
| 作物 | `crop_{wheat,carrot,tomato}_{seed,grow,ready}`（刷新深描邊） |
| 工具／手 | `ic_tool_hoe`／`cursor_hand`／`ic_tool_water` |
| 記帳 | `ic_ledger_book`＋`_f1`／`_f2` 翻頁 |
| 買賣 | `ic_buy_bag`／`ic_sell_basket`／`ic_coin_plus`／`ic_coin_minus` |
| 物品欄 | `item_seed_*`／`item_harvest_*`／`item_material` |
| 接線 | `FarmScreen`：記帳鈕、選種 chip、種植／收成、inventory 橫列 |

產生腳本：`scripts/gen_m2_art_assets.py`。**唔改**獎勵／防刷。

---

## 7d. M3 實際素材（2026-09-22 開工・夜日結儀式＋餵養）

產品開 M3 美術＝夜晚日結月亮儀式＋寵物餵食可讀＋成熟高亮。詳見 **`docs/m3_art_assets.md`**。

| 類 | 實際檔名 |
|----|----------|
| 日結 | `ic_moon_settle`／`ic_settle_stamp`／`fx_settle_stamp`／`settle_banner`／`moon_rise_f0..f2` |
| 寵物 | `pet_idle`／`pet_happy`／`pet_eat`（小芽豬升級）＋`item_feed`／`fx_eat` |
| FX | `fx_heart`（刷新）／`fx_sparkle_ready` |
| 接線 | `FarmScreen` 結算鈕＋Settle 浮層升起／蓋章；`FarmSceneLayer`／`PetScreen` 餵食；READY sparkle |

產生腳本：`scripts/gen_m3_art_assets.py`。**唔改**一日一結／改帳唔重派。

---

## 7e. M4 實際素材（2026-09-22 開工・商店／擴建＋全日生活節點）

產品開 M4 美術＝場景商店攤＋攤主、擴建地／藍圖／小屋刷新、少量 decor、商店 UI 格、一日節奏 icon。詳見 **`docs/m4_art_assets.md`**。

| 類 | 實際檔名 |
|----|----------|
| 商店 | `shop_stall`／`npc_vendor_idle`／`shop_sign`（＋`building_shop_pole`／`ic_shop_pole`） |
| 擴建 | `build_plot_empty`／`building_hut`（刷新）／`build_blueprint` |
| decor | `decor_lamp`／`decor_flowerbox`／`decor_sign`（＋`decor_lantern`／`decor_scarecrow`） |
| UI | `ui_shop_slot`／`ui_price_tag` |
| 日課 | `ic_day_wake`／`ic_day_work`／`ic_day_shop`／`ic_day_sleep` |
| 接線 | `DayLoopLogic`＋`FarmScreen`／`FarmSceneLayer` 攤位熱區＋decor；佈置耗成長點唔發獎 |

產生腳本：`scripts/gen_m4_art_assets.py`。**唔改**一日一結／改帳唔重派。

---

## 8. 群組 5 行重點（可直接貼）

1. **方向**：俯視暖色像素＋大方塊深輪廓；單一農場；晨昏夜用 tint。  
2. **記帳＝日課**：記帳本、買料（支出）、賣菜（收入）、**月亮日結**要有獨特 icon／短動畫。  
3. **分階**：M0 可玩閉環 → M1 日課可讀 → M2 擴建感 → M3 先四季／室內。  
4. **授權**：自繪優先；CC0 只係核實後輔助，未核實唔進包。  
5. **唔做**：3D、第一期星露級大地圖密度。

---

*文件路徑：`docs/pixel_sos_art_roadmap.md` · 唔改獎勵／Kotlin domain。*
