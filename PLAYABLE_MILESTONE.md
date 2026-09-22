# Playable Milestone — 農帳養成（Farm Ledger）M1

可邀請試玩的離線 Debug 里程碑。獎勵規則**未改動**（固定 1 成長點／本地日一次；同日編輯／重結算不重複發獎）。

## 基準提交

| 項目 | 值 |
|------|-----|
| Commit SHA | `a04d216e4bf0512fe99a44d2d88f794e4bc9a133`（短：`a04d216`） |
| 說明 | M1 day-phase：晨／晝／昏／夜＋睡覺過日；Room 存檔；農場根世界 HUD |
| 分支 | `main` |

## M1 已落地

- **日狀態機**：晨 → 晝 → 昏 → 夜（等待）；夜晚睡覺 → 翌日晨；`clockPaused`／牆鐘倒退暫停推進
- **Room**：`game_day_state`（日／時段／階段提示）；M2 stub `inventory_items`
- **單一農場主世界**：農場為導航根；記帳／結算仍為 overlay；無獨立記帳首頁
- **HUD**：時段／遊戲日＋成長點；場景時段 tint（可選 overlay PNG）
- **反濫用**：結算仍固定 1 點／日；睡覺／等待／編輯不發獎

## 驗證結果

### 1. 離線權限

`AndroidManifest.xml` **沒有**宣告 `INTERNET`。

### 2. 建置與測試

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

- **單元測試**：29 項全過（含既有結算 7 + 新增 `DayPhaseLogicTest` 6）
- **Debug APK**：`app/build/outputs/apk/debug/app-debug.apk`

### 3. 獎勵規則（不變）

- 每日結算：固定 1 成長點／本地日一次
- 同日再結算／編輯：不重複發獎
- 金額／筆數：不影響
- 時鐘倒退：暫停獎勵，不刪帳本

## 安裝

見 [README.md](README.md)。建議：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
