# Playable Milestone — 農帳養成（Farm Ledger）M5

可邀請試玩的離線 Debug 里程碑。獎勵規則核心不變（固定成長點／本地日一次；同日編輯／重結算不重複發獎）；M5 另發固定種子幣，且**唔跟金額／筆數**。

## 基準提交

| 項目 | 值 |
|------|-----|
| Commit SHA | `1e29902aa4c0ab5c6d61097bc4aa8ef51f23b1d5`（短：`1e29902`） |
| 說明 | M5：真港幣記帳核——拆混帳、四 Tab、入帳／帳簿／日記殼、category／account migration |
| 前身 M4 | `7f91786`／gate `c5432a7` |
| 分支 | `main` |

## M5.1／A2b 已落地（多帳戶＋紙感＋等距牧場）

- 多帳戶新增／改名；入帳轉帳（單筆 TRANSFER：由→到）；儲蓄熱區預填轉帳
- A2b `spring_ranch_iso`（1280×720）＋`hotspot_map.json` 正規化九熱區；漏記＝`overlay_missed_grass`
- 入帳／帳簿／日記：A2 `entry_clipboard`／`ledger_paper`／`diary_*` 紙感（木／奶油／印章紅）
- versionName `0.5.2-m5`

## M5 已落地

- **拆混帳**：商店買種子／賣收成／買飼料只動種子幣＋背包，**唔再寫** `ledger_entries`
- **Schema v3**：`category`、`accountId`、`transferAccountId`；`TRANSFER` 類型；帳戶表 stub「現金」；Room `MIGRATION_2_3`
- **底欄**：牧場｜入帳｜帳簿｜日記（設定改為次入口）
- **入帳**：預設支出＋分類 chips→金額→儲存（港幣 `amountMinor`）
- **帳簿**：跨日列表＋備註／金額搜尋；編輯／作廢
- **日記殼**：今日有記／未記、漏記分類長草標記、週回顧入口
- **日結**：有記→固定 1 成長點＋1 種子幣；改帳唔重派

## 驗證

```bash
./gradlew :app:testDebugUnitTest
# 可選
./gradlew :app:assembleDebug
```

## 獎勵規則

- 每日結算：固定 1 成長點＋1 種子幣／本地日一次（有記／記全）
- 同日再結算／編輯：不重複發獎
- 金額／筆數／商店買賣／餵食／佈置／睡覺：不影響成長點發放量
- 商店＝牧場內經濟（種子幣 ≠ 港幣）

## 基準提交記錄

- M5 真港幣記帳核：`1e29902`（2026-09-26）
- M4 day-loop + shop stall：`7f91786`（2026-09-22）
- M3 night-settle + inventory feed：`6f0a859`（2026-09-22）
