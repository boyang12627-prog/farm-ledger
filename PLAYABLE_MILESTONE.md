# Playable Milestone — 農帳養成（Farm Ledger）M3

可邀請試玩的離線 Debug 里程碑。獎勵規則**未改動**（固定 1 成長點／本地日一次；同日編輯／重結算不重複發獎）。

## 基準提交

| 項目 | 值 |
|------|-----|
| Commit SHA | `6f0a8598948eaa02d737679b93f95c1eafc31633`（短：`6f0a859`） |
| 說明 | M3：夜結儀式 UX＋背包飼料餵食（開心／進食反饋） |
| 前身 M2 | `393f781`／記錄 `7636a66` |
| 分支 | `main` |

## M3 已落地

- **夜結儀式**：月亮 icon、蓋章「今日結清」、成長點入袋短反饋；規則仍固定 1 點／日
- **寵物餵食**：消耗背包飼料（商店可買＝支出）；唔扣成長點；場景 `pet_eat`／開心反饋
- **保留**：日循環晨晝昏夜、單一農場主世界、商店買種子／賣收成→帳本、種／澆／收
- **反濫用**：結算仍固定 1 點／日；睡覺／等待／買賣／編輯／餵食不發獎

## 驗證結果

### 1. 離線權限

`AndroidManifest.xml` **沒有**宣告 `INTERNET`。

### 2. 建置與測試

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

- **單元測試**：結算 + 日循環 + 背包／買賣／飼料 + 種澆收＋餵食
- **Debug APK**：`app/build/outputs/apk/debug/app-debug.apk`

### 3. 獎勵規則（不變）

- 每日結算：固定 1 成長點／本地日一次
- 同日再結算／編輯：不重複發獎
- 金額／筆數／買賣／餵食：不影響成長點發放
- 時鐘倒退：暫停獎勵，不刪帳本

## 安裝

見 [README.md](README.md)。建議：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 基準提交記錄

- M3 night-settle + inventory feed：`6f0a859`（2026-09-22）
- M2 art+inventory：`393f781`（2026-09-22）
