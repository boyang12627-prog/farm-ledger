# Farm Ledger UI screenshots

Generated with **Paparazzi** (JVM Compose screenshots) — no emulator required.

| File | Screen | Notes |
|------|--------|-------|
| `01_home.png` | 主頁 | Sample progress + navigation buttons |
| `02_ledger.png` | 今日帳簿 | Sample income / expense / 無交易日 rows |
| `03_settle.png` | 每日結算 | Settlement rules + CTA |
| `04_farm.png` | 農田 | 6 plots with mixed empty / growing / ready |
| `05_ranch_a2c.png` | 牧場 A2c | Floating chips + hotspots (no permanent labels) |
| `05b_ranch_landscape.png` | 牧場橫屏 | 1280×720 idle; no wood-sign row (spec §6) |
| `05c_topbar_crop.png` | 頂欄裁切 | 季節／天氣＝圖 only；種子袋數字（0.5.4c） |
| `05d_ranch_sparse.png` | 第1日疏場橫屏 | 只 home／food／income 三熱區 |
| `05d_topbar_day.png` | 頂欄「第 N 日」 | ranchDay plaque；唔顯示連續 |

## Regenerate

```bash
./gradlew :app:recordPaparazziDebug
cp app/src/test/snapshots/images/*_01_home.png screenshots/01_home.png
cp app/src/test/snapshots/images/*_02_ledger.png screenshots/02_ledger.png
cp app/src/test/snapshots/images/*_03_settle.png screenshots/03_settle.png
cp app/src/test/snapshots/images/*_04_farm.png screenshots/04_farm.png
cp app/src/test/snapshots/images/*_05_ranch_a2c.png screenshots/05_ranch_a2c.png
cp app/src/test/snapshots/images/*_05b_ranch_landscape.png screenshots/05b_ranch_landscape.png
# Top-bar crop from landscape (new file; do not overwrite playable-v0.1)
ffmpeg -y -i screenshots/05b_ranch_landscape.png -vf "crop=iw:ih*0.14:0:0" screenshots/05c_topbar_crop.png
cp app/src/test/snapshots/images/*_05d_ranch_sparse.png screenshots/05d_ranch_sparse.png
cp app/src/test/snapshots/images/*_05d_topbar_day.png screenshots/05d_topbar_day.png
```

Test source: `app/src/test/java/com/farmledger/app/ui/screenshot/ScreenScreenshotTest.kt`
