# Playable Milestone — 農帳養成（Farm Ledger）M4

可邀請試玩的離線 Debug 里程碑。獎勵規則**未改動**（固定 1 成長點／本地日一次；同日編輯／重結算不重複發獎）。

## 基準提交

| 項目 | 值 |
|------|-----|
| Commit SHA |（組裝推送後填入） |
| 說明 | M4：連續一日生活（起床→勞作→黃昏商店→夜結→瞓覺）＋商店攤位／佈置 |
| 前身 M3 | `6f0a859`／記錄 `7716944` |
| 分支 | `main` |

## M4 已落地

- **一日生活閉環**：起床→勞作（種澆收餵）→黃昏商店買賣入帳→夜晚日結→瞓覺過日（`DayLoopLogic` 指引）
- **商店攤位**：場景 `shop_stall`＋攤主熱區開商店 overlay；黃昏 CTA 強化；UI 格子／標價
- **佈置**：安家起耗成長點佈置；燈籠／花箱／告示／稻草人 sprite
- **保留**：夜結儀式、背包飼料餵食、種／澆／收、固定 1 點／日
- **反濫用**：結算仍固定 1 點／日；睡覺／等待／買賣／編輯／餵食／佈置不發獎

## 驗證結果

### 1. 離線權限

`AndroidManifest.xml` **沒有**宣告 `INTERNET`。

### 2. 建置與測試

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

- **單元測試**：結算 + 日循環 + 一日指引 + 背包／買賣／飼料 + 種澆收＋餵食＋階段佈置
- **Debug APK**：`app/build/outputs/apk/debug/app-debug.apk`

### 3. 獎勵規則（不變）

- 每日結算：固定 1 成長點／本地日一次
- 同日再結算／編輯：不重複發獎
- 金額／筆數／買賣／餵食／佈置／睡覺：不影響成長點發放
- 時鐘倒退：暫停獎勵，不刪帳本

## 安裝

見 [README.md](README.md)。建議：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 基準提交記錄

- M4 day-loop + shop pole：（推送後）
- M3 night-settle + inventory feed：`6f0a859`（2026-09-22）
- M2 art+inventory：`393f781`（2026-09-22）
