# A2c 橫屏場景＋Label 規範（工程必讀）

用戶鎖定：**主牧場橫屏**；熱區跟牧場物語／SoS 語感——**物件本身可辨，字少而細，或長按先出**。  
禁止：九個熱區同時掛一排誇張中文木牌／核突浮標。

對齊素材：`docs/art/a2c/`｜座標：`hotspot_map.json`（同 a2 fx/fy）｜線框預覽：`previews/a2c_landscape_label_wire.png`

---

## 1. 方向與安全區

| 項 | 規範 |
|----|------|
| 主牧場 Activity／畫面 | **強制橫屏** `landscape`（sensorLandscape 可） |
| 設計基準 | **16:9**，素材 `ranch/spring_ranch_base.png`＝**1280×720** |
| 其他 Tab（入帳／帳簿／日記） | 可用直向浮層／Sheet；由牧場點入時可 overlay，唔一定整 App 鎖橫 |
| 頂欄 | 浮空 chip，左右分佈，**唔做通欄實心條**遮場面 |
| 底欄 | 矮木條＋四 Tab；橫屏高度約 **56–64dp**，唔遮熱區中下帶 |
| 安全邊距 | 左右 ≥16dp；避開挖孔／手勢區 |

Compose：`RanchScreen` 用 `BoxWithConstraints`，熱區位置＝`fx * width`、`fy * height`（見 `hotspot_map.json`）。

---

## 2. 熱區視覺（SoS）

1. **默認只顯示物件 sprite**（`idle`／`pressed`／`active`），靠造型認分類（餐桌灶、單車、門廊…）。  
2. **唔**在每個熱區上方常駐中文木牌、分類全名、金額摘要條。  
3. `active`（今日有記）用素材自帶小章／柔邊即可；**唔**再叠大字「已記」。  
4. 可選極淡 hint：直徑 ≤6dp 半透明點或微光，唔附文字。  
5. 點擊＝開入帳（預填分類）；語感係「點農場物件」，唔係「點選單按鈕」。

### 狀態檔

| 狀態 | 檔 | 何時 |
|------|-----|------|
| idle | `*_idle.png` | 默認 |
| pressed | `*_pressed.png` | 按下 |
| active | `*_active.png`（＝hasEntry） | 該分類今日已有記 |

---

## 3. Label 規則（字少）

| 模式 | 行為 | 素材 |
|------|------|------|
| **A 默認（建議）** | 全程無文字 label | — |
| **B 長按 whisper** | 長按 ≥400ms 先在指尖旁出**細字** tip，鬆開／移開即消 | `sheets/whisper_label.png` 做底；Compose `Text` 叠 **≤4 字**（如「飲食」「儲蓄」） |
| **C 首次教學** | 僅首日／設定「顯示提示」時短暫出示 whisper，可關 | 同上 |
| **禁止** | 九牌同時常駐；大號 `floating_wood_sign` 掛滿場；核突對比／粗黑描邊大字 | `floating_wood_sign.png` **唔再做熱區常駐牌** |

### Whisper 字級

- 字：約 **10–11sp**，墨線 `#3D2A1A`，可 90% alpha  
- 牌：高約 **24–28dp**，寬跟字；左右 padding 8dp  
- 位置：熱區錨點**上方或指尖偏移**，避擋物件中心；同時最多 **1** 個 tip  
- 內容：只分類短名；**唔**放金額、HKD、種子幣數字（數字只在頂欄／入帳夾板）

`sheets/floating_wood_sign.png`：留作**系統 toast／空態說明／設定頁**，唔用於九熱區常駐。

---

## 4. 頂／底欄（橫屏）

**頂（可組裝，唔燒進底圖）**

- 左：季節章 `season_stamp_spring`＋天氣 icon  
- 中或右旁：連續日記數牌（數字用 Compose）  
- 右：種子幣袋（只種子幣；真帳 HKD 唔出現喺牧場 HUD）

**底**

- `tab_*_selected`／`unselected`  
- 橫屏可只顯示 icon、或 icon＋細字；選中態靠素材，唔靠大色塊銀行底

---

## 5. 漏記草

`overlays/grass_layer_1..3` 按漏記日數叠；半透明、**唔**附「你漏記了」大字。敘事靠草，唔靠罵人 banner。

---

## 6. 驗收（策劃系統感＋用戶橫屏語感）

- [ ] 主牧場橫屏 16:9，場面以牧場為主  
- [ ] 九熱區無常駐中文木牌排  
- [ ] 物件 idle／pressed／active 可辨  
- [ ] Label 要麼無，要麼長按細 tip  
- [ ] 頂底欄獨立件，唔嵌死底圖  
- [ ] 漏記草分層可叠  

---

## 7. 工程切換摘要

```text
強制：Ranch 橫屏 + 隱藏常駐 hotspot labels
長按：可選 WhisperLabel(categoryShortName) 用 whisper_label.png
浮標大木牌：移出熱區；改用於非牧場提示
座標：繼續 docs/art/a2c/hotspot_map.json
```
