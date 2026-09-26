# 牧場手帳 A2c｜可組裝 UI 元件包

對齊 `docs/art/ref_user/` 概念冊＋`牧場UI代币`。**唔再交單張 wallpaper**；工程用 Compose 疊層組裝成完整介面。

風格：暖色 SoS／半手繪自繪。禁止官方商標角色。真帳金額用 **HKD**；種子幣另欄（見 `topbar/seed_coin_pouch.png`）。

色票：木框 `#6B4A2E`｜奶油紙 `#F5E6C8`｜墨線 `#3D2A1A`｜印章紅 `#C45C4A`。

完整清單位於 `manifest.json`。預覽：`previews/a2c_kit_preview.jpg`。

---

## 目錄

| 路徑 | 用途 |
|------|------|
| `ranch/spring_ranch_base.png` | 春牧場底圖（空地留给熱區 sprite）1280×720 |
| `ranch/spring_ranch_iso_filled_alt.png` | A2b 填滿版底圖（可選，唔當唯一 UI） |
| `hotspots/*_{idle,pressed,hasEntry}.png` | 九鎖定熱區獨立 sprite×三態 256×256 |
| `topbar/` | 季節章、天氣、連續日記牌、種子幣袋 |
| `tabbar/tab_*_{selected,unselected}.png` | 底欄四 Tab 木鈕 |
| `sheets/` | 入帳夾板、帳簿紙、日記紙、空態插畫、浮標木牌 |
| `overlays/grass_layer_{1,2,3}.png` | 漏記草可疊層 |
| `overlays/stamp_sprout.png` | hasEntry／記入章小標 |
| `source_sheets/` | 生成原表（備查，App 唔使直接用） |

### 九熱區對應

| id | 分類 | 物件 |
|----|------|------|
| `food_table` | 飲食 | 餐桌灶 |
| `transport_bike` | 交通 | 單車 |
| `housing_porch` | 住屋 | 門廊 |
| `daily_crates` | 日用 | 木箱 |
| `fun_garden_pond` | 娛樂 | 花圃魚塘 |
| `health_herbs` | 健康 | 藥草 |
| `income_mail_basket` | 收入 | 郵箱收成籃 |
| `savings_piggy` | 儲蓄 | 撲滿 |
| `other_notice` | 其他 | 告示牌 |

三態：`idle` 靜置｜`pressed` 按下（略暗下壓）｜`hasEntry` 今日已記（印章紅圈＋芽章）。

漏記草：依連續漏記日數疊 `grass_layer_1` → `+2` → `+3`（可半透明）。

---

## 建議 Compose 用法（示意）

```kotlin
// 牧場頁：底圖 + 熱區 + 頂欄 + 底欄 + 可選漏記草
Box(Modifier.fillMaxSize()) {
  Image(painterResource(R.drawable.spring_ranch_base), null, Modifier.fillMaxSize(), ContentScale.Crop)

  // 每個熱區：依今日該分類有無入帳揀 state
  HotspotSprite(
    id = HotspotId.FoodTable,
    state = when {
      pressedId == FoodTable -> HotspotState.Pressed
      hasEntryToday(Category.Food) -> HotspotState.HasEntry
      else -> HotspotState.Idle
    },
    anchorFx = 0.22f, anchorFy = 0.55f, // 見 manifest.json hotspots[].anchor
    onClick = { openEntry(Category.Food) },
  )

  if (missedDays >= 1) Image(grassLayer1, ..., alpha = 0.85f)
  if (missedDays >= 2) Image(grassLayer2, ..., alpha = 0.9f)
  if (missedDays >= 3) Image(grassLayer3, ..., alpha = 1f)

  TopBar(seasonStamp, weatherIcon, streakPlaque with Text("$streak"), seedPouch with Text(seedAmount))
  BottomBar(selected = Tab.Ranch) // selected / unselected drawables
}

// 入帳：半透明牧場底 + 夾板浮層（內容用 Compose 疊，唔燒字入圖）
ModalBottomSheet / Dialog {
  Box {
    Image(entry_clipboard, ...)
    Column(Modifier.padding(...)) { /* 支出｜金額 HKD｜分類｜記入牧場 */ }
  }
}
```

### drawable 建議命名

把 PNG 拷入 `res/drawable`（或 `drawable-nodpi`）時可去掉資料夾前綴，例如：

- `hotspot_food_table_idle.png`
- `tab_ranch_selected.png`
- `overlay_grass_layer_1.png`

`manifest.json` 的 `anchor.fx/fy` 係相對底圖正規化中心點；實機 hitbox 可再微調（A2b 回饋路徑）。

### 注意

- 紙頁／夾板係**材質底**，日期、金額、列表用 Compose `Text` 疊上；真帳一定標 **HK$ / HKD**，種子幣只用幣袋零件。
- Tab 圖已含中文標籤；若要動態字型可裁 icon 區或改用純 icon 版（可再開 A2d）。
- `source_sheets/` 唔進 release APK。

---

## 授權

本包為專案自繪／生成美術，作「牧場手帳」免費 App 商用。無第三方付費素材、無官方 IP。
