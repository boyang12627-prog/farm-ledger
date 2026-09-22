# Playable Milestone — 農帳養成（Farm Ledger）M2

可邀請試玩的離線 Debug 里程碑。獎勵規則**未改動**（固定 1 成長點／本地日一次；同日編輯／重結算不重複發獎）。

## 基準提交

| 項目 | 值 |
|------|-----|
| Commit SHA | `393f7813244dc1123fabc2a8defbac52f375cfc5`（短：`393f781`） |
| 說明 | M2：場內種／澆／收＋背包種子與收成堆疊；買種子／賣收成寫帳（唔發成長點） |
| 前身 M1 | `a04d216`／記錄 `618a14e` |
| 分支 | `main` |

## M2 已落地

- **種／澆／收**：種植消耗背包種子；澆水後才開始成長計時；收成入背包作物堆疊（唔發成長點）
- **背包**：種子（小麥／紅蘿蔔／番茄）＋收成堆疊；Room `inventory_items`
- **商店 overlay**：買種子＝支出、賣收成＝收入；**必須經背包**；金額／筆數唔發成長點
- **日循環**：晨／晝／昏／夜＋睡覺過日保留；黃昏可開商店 stub
- **反濫用**：結算仍固定 1 點／日；睡覺／等待／買賣／編輯不發獎
- **單一農場主世界**：記帳／結算／商店皆為 overlay

## 驗證結果

### 1. 離線權限

`AndroidManifest.xml` **沒有**宣告 `INTERNET`。

### 2. 建置與測試

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

- **單元測試**：結算 + 日循環 + 背包／買賣 + 種澆收
- **Debug APK**：`app/build/outputs/apk/debug/app-debug.apk`

### 3. 獎勵規則（不變）

- 每日結算：固定 1 成長點／本地日一次
- 同日再結算／編輯：不重複發獎
- 金額／筆數／買賣：不影響成長點
- 時鐘倒退：暫停獎勵，不刪帳本

## 安裝

見 [README.md](README.md)。建議：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 基準提交記錄

- M2 art+inventory：`393f781`（2026-09-22）
