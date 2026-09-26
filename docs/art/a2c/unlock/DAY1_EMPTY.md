# Day1 淨農地（開局驗收準繩）

橫屏牧場＝草／田／天空／遠景；**零** food／home／income／任何熱區物件／灰桩／木牌。

| 檔 | 用途 |
|----|------|
| `day1_empty.json` | 工程 gating 主檔 |
| `ranch/spring_ranch_bare_day1.png` | bare 底圖 1280×720 |
| `previews/a2c_day1_bare_ranch.jpg` | 開局預覽 |
| `previews/a2c_first_building_home.jpg` | 買咗第一件 home 之後 |

## 渲染規則

```
show = shop_day_reached AND owned
else  completely hidden (no gray stake)
```

## 建造／上架順序

1. **home**（第1件可買）
2. food、income（home 後）
3. daily D3 → transit D5 → save D7 → fun D10 → health D12 → other D14

價位見策劃表；商店 UI 可後做，先保證 bare＋home 層。
