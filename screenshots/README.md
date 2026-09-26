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

## Regenerate

```bash
./gradlew :app:recordPaparazziDebug
cp app/src/test/snapshots/images/*_01_home.png screenshots/01_home.png
cp app/src/test/snapshots/images/*_02_ledger.png screenshots/02_ledger.png
cp app/src/test/snapshots/images/*_03_settle.png screenshots/03_settle.png
cp app/src/test/snapshots/images/*_04_farm.png screenshots/04_farm.png
cp app/src/test/snapshots/images/*_05_ranch_a2c.png screenshots/05_ranch_a2c.png
cp app/src/test/snapshots/images/*_05b_ranch_landscape.png screenshots/05b_ranch_landscape.png
```

Test source: `app/src/test/java/com/farmledger/app/ui/screenshot/ScreenScreenshotTest.kt`
