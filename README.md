# 農帳養成（Farm Ledger）MVP

離線專用的 Android 記帳 × 養成小遊戲。每日記帳結算可獲固定成長點，種田、養寵物、佈置小屋。**無網絡權限、無登入、無廣告、無分析。**

> 介面為繁體中文，用語盡量兼顧粵語地區閱讀習慣（「唔會」「去結算」等）。

## 功能摘要

- **每日帳簿**：收入／支出／無交易日；金額以 `amountMinor`（Long，分）儲存；可編輯、作廢
- **每日結算**：有帳本活動即可結算，**固定 1 成長點／本地日一次**；編輯唔會再次發獎；金額／筆數唔影響獎勵
- **累計結算日解鎖場景**：3／7／14／21 → 萌芽／安家／旺場／第二動物欄（非記帳筆數）
- **連續結算 3／5／7 日**：另解鎖種子加成與裝飾（偏外觀）
- **週回顧**：按本週結算天數發獎，**上限 3 點**
- **時鐘倒退**：暫停每日／作物獎勵，**不刪除帳本**
- **單一農場主世界**：記帳／結算／商店為 overlay；成長點於場景內花費餵食／佈置（種植改耗種子）
- **農田／背包（M2）**：種／澆／收用背包種子與收成堆疊；商店買種子＝支出、賣收成＝收入（金額唔發成長點）
- **農田**：階段解鎖田格（荒地 2 格→萌芽 6 格）、3 種作物種植→澆水→收成入庫（收成不發成長點）
- **寵物**：餵食、互動、改名
- **家居裝飾**：7 件佔位裝飾（暖色像素風；含小屋／樹木場景裝飾）
- **匯出／匯入**：JSON 全量備份、CSV 帳目，經系統 SAF 檔案選擇器

## 安裝方式（離線 Debug APK）

本 App **完全離線**：`AndroidManifest.xml` 不宣告 `INTERNET`／網絡權限。以下兩種安裝方式皆適用本機／實機／模擬器。

### 方法一：用 Android Studio 開啟並執行

1. 安裝 [Android Studio](https://developer.android.com/studio)（建議 Hedgehog／Iguana 或更新）
2. 選 **File → Open**，開啟本專案根目錄（須含 `settings.gradle.kts`）
3. 等待 Gradle Sync 完成；若提示 JDK，選 **Temurin／JDK 17+**
4. 建立或啟動一個 **API 26+** 模擬器，或接上實機並開啟 USB 偵錯
5. 上方工具列選 `app` 模組，按 **Run ▶** 安裝並啟動

### 方法二：指令列組裝後用 adb 安裝

本機已裝 Android SDK／`adb` 時，於專案根目錄執行：

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Debug APK 路徑（相對於專案根目錄）：

```text
app/build/outputs/apk/debug/app-debug.apk
```

若已組裝完成，只需：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

可玩里程碑說明見 [`PLAYABLE_MILESTONE.md`](PLAYABLE_MILESTONE.md)。

## 測試

核心領域邏輯單元測試（防雙重發獎、金額不影響獎勵、時鐘倒退等）：

```bash
./gradlew :app:testDebugUnitTest
```

重點用例見：

- `app/src/test/.../DailySettlementLogicTest.kt`
- `app/src/test/.../WeeklyReviewLogicTest.kt`
- `app/src/test/.../FarmStageLogicTest.kt`
- `app/src/test/.../DayPhaseLogicTest.kt`
- `app/src/test/.../InventoryLogicTest.kt`
- `app/src/test/.../FarmInventoryLogicTest.kt`

## 技術棧

Kotlin · Jetpack Compose · Material 3 · Navigation · ViewModel / StateFlow · Room · DataStore · kotlinx.serialization

單模組 `app`，套件大致分為 `domain` / `data` / `ui`。

## 已知限制（MVP）

- 作物成長時間為示範用短倒數（1～3 分鐘），非真實農作節奏
- 像素美術為色票／向量佔位，非最終素材
- 匯入 CSV 只覆蓋帳目列，完整進度請用 JSON
- 未做多語系切換（目前固定繁中）
- 共用開發機／模擬器可能遇到鎖屏或無法自動解鎖，實機安裝較穩
- `THIRD_PARTY_LICENSES.md` 為佔位，上架前請補齊依賴授權全文

## 權限說明

`AndroidManifest.xml` **故意不宣告** `INTERNET`／網絡相關權限。檔案匯出匯入走 Storage Access Framework，唔使申請儲存權限（API 26+ 目標行為）。

## 授權

應用程式碼以專案倉庫為準；第三方函式庫見 `THIRD_PARTY_LICENSES.md`。
