# A2c 工程接檔

**狀態：** 可組裝包已在 `docs/art/a2c/`（main）。  
**對齊：** 錨點 `fx/fy` 同 `docs/art/a2/hotspot_map.json`；本目錄另有 `hotspot_map.json`＋`manifest.json`。

## 必讀

1. `README.md` — Compose 叠層用法  
2. `manifest.json` — 全檔清單  
3. `hotspot_map.json` — 九熱區 id／座標／sprite 路徑  

## 對照工程需求

| 需求 | 路徑 |
|------|------|
| 九熱區 idle／pressed／active | `hotspots/{sprite}_{idle\|pressed\|active}.png`（`active`＝今日有記；亦有 `*_hasEntry.png` 同內容） |
| 木牌浮標框 | `sheets/floating_wood_sign.png`（分類名＋摘要用 Compose Text 叠） |
| 頂欄季節／天氣／連續／種子幣 | `topbar/*` |
| 底欄四 Tab＋選中 | `tabbar/tab_*_{selected\|unselected}.png` |
| 漏記草分層 1–N | `overlays/grass_layer_1.png` … `_3.png`（N>3 可重複叠 `_3` 或加 alpha） |
| 牧場底（唔嵌 UI） | `ranch/spring_ranch_base.png` |
| 夾板／帳簿／日記紙 | `sheets/entry_clipboard.png` 等 |

## id 對照

| eng id | sprite stem |
|--------|-------------|
| food | food_table |
| transit | transport_bike |
| home | housing_porch |
| daily | daily_crates |
| fun | fun_garden_pond |
| health | health_herbs |
| income | income_mail_basket |
| save | savings_piggy |
| other | other_notice |

預覽：`previews/a2c_assembled_ranch.jpg`、`a2c_kit_preview.jpg`。

---

## 橫屏＋Label（用戶 2026-09-26）

**必讀：** [`A2c_landscape_label_spec.md`](./A2c_landscape_label_spec.md)

- 主牧場 **landscape 16:9**
- 熱區 **唔掛常駐中文木牌**；字少／長按 `sheets/whisper_label.png`
- `floating_wood_sign` 唔再做九熱區常駐牌


---

## 第1日疏場

見 `unlock/DAY1_SPARSE.md` ＋ `unlock/day1_sparse.json` ＋ `previews/a2c_day1_sparse_ranch.jpg`。


---

## Day1 淨空農地

開局用 `unlock/DAY1_BARE.md`／`previews/a2c_day1_bare_ranch.jpg`（零物件）。sparse 係買起步件後。
