# Playable Milestone — 農帳養成（Farm Ledger）

可邀請試玩的離線 Debug 里程碑紀錄。獎勵規則**未改動**（固定 1 成長點／本地日一次；同日編輯／重結算不重複發獎）。

## 基準提交

| 項目 | 值 |
|------|-----|
| Commit SHA | `f1cdffb0d677707a739333cb514bbb5091e4fc83`（短：`f1cdffb`） |
| 說明 | Wire hut and tree decor overlays into FarmSceneLayer |
| 分支 | `main` |

## 驗證結果（2026-09-19，Africa/Lagos）

### 1. 離線權限

`app/src/main/AndroidManifest.xml` **沒有**宣告 `INTERNET` 或任何網絡權限（僅離線註解說明）。

### 2. 建置與測試

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

- **結果**：`BUILD SUCCESSFUL`
- **單元測試**：13 項全過（failures=0, errors=0）
  - `DailySettlementLogicTest`：7
  - `WeeklyReviewLogicTest`：2
  - `ScreenScreenshotTest`：4

### 3. Debug APK

| 項目 | 值 |
|------|-----|
| 絕對路徑 | `/workspace/farm-ledger/app/build/outputs/apk/debug/app-debug.apk` |
| 相對路徑 | `app/build/outputs/apk/debug/app-debug.apk` |
| 大小 | 17 073 035 bytes（約 **17 MB**） |

## 獎勵規則（驗收，保持不變）

- 每日結算：**固定 1 成長點／本地日一次**
- 同日再次結算／編輯帳目：**不會再次發獎**
- 金額、筆數：**不影響**每日成長點
- 週回顧：按本週結算天數發獎，**上限 3 點**
- 時鐘倒退：暫停每日／作物獎勵，**不刪除帳本**

## 安裝步驟（繁體中文）

詳見 [README.md](README.md)「安裝方式」。摘要：

**Android Studio**

1. `File → Open` 開啟專案根目錄
2. Gradle Sync（JDK 17+）
3. API 26+ 模擬器或實機
4. Run ▶ `app`

**adb**

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

全程離線：無網絡權限、無登入、無廣告。

## 已知限制

- 共用開發機上的模擬器可能卡在**鎖屏／無法自動解鎖**，影響無頭點擊驗證；請用實機或本機模擬器試玩
- 作物成長為示範用短倒數（約 1～3 分鐘）
- 美術仍多為佔位／自繪場景裝飾（小屋、樹木已接入 `FarmSceneLayer`）
- 未做多語系切換（介面固定繁中）

## 試玩就緒

**可以邀請使用者試玩**：Debug APK 已組裝、單元測試全綠、無 INTERNET 權限、獎勵規則維持固定 1 點＋不重複發獎。建議優先以實機 `adb install` 或本機 Android Studio Run 體驗。
