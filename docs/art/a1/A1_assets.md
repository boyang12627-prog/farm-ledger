# A1 Art Assets · 牧場手帳（春季牧場）

> 版本：A1　日期：2026-09-26  
> 風格：跟 A0 色票／熱點鎖定；Q 版半像素半手繪佔位（Pillow）

---

## 貨幣顯示規則（繼承 A0）

| 層 | 顯示 | 說明 |
|----|------|------|
| 真實帳簿金額 | **港幣 HKD**（例 −HK$48、+HK$320） | 帳戶流水 |
| 牧場獎勵 | **種子幣**（獨立欄／欄位） | ≠ HKD，**無線性換算**；禁止用裸「幣」當帳戶金額 |

本 A1 預覽以牧場場景為主；若日後在預覽加帳簿 mock，必須分欄顯示 HKD 與種子幣。

---

## 檔案清單與尺寸

| 檔案 | 尺寸 | 說明 |
|------|------|------|
| `spring_ranch_base.png` | 1080×1200 | 春季牧場底圖（空曠舒適牧場，drawable-friendly） |
| `hotspot_food_table_stove.png` | 160×160 | 飲食／餐桌灶（透明底剪影） |
| `hotspot_transit_path_bike.png` | 160×160 | 交通／小路單車 |
| `hotspot_home_porch.png` | 160×160 | 住屋／門廊 |
| `hotspot_daily_crate.png` | 160×160 | 日用／木箱 |
| `hotspot_fun_garden_pond.png` | 160×160 | 娛樂／花圃魚塘 |
| `hotspot_health_herbs.png` | 160×160 | 健康／藥草 |
| `hotspot_income_mail_basket.png` | 160×160 | 收入／郵箱收成籃 |
| `hotspot_save_piggy.png` | 160×160 | 儲蓄／撲滿 |
| `hotspot_other_sign.png` | 160×160 | 其他／告示牌 |
| `hotspots_overlay.png` | 1080×1200 | 九熱點＋印章紅光環 combined overlay（透明底） |
| `nav_ranch.png` / `nav_ranch@2x.png` | 48×48 / 96×96 | 底欄・牧場 |
| `nav_entry.png` / `nav_entry@2x.png` | 48×48 / 96×96 | 底欄・入帳 |
| `nav_ledger.png` / `nav_ledger@2x.png` | 48×48 / 96×96 | 底欄・帳簿 |
| `nav_diary.png` / `nav_diary@2x.png` | 48×48 / 96×96 | 底欄・日記 |
| `a1_preview.png` | 1080×1420 | 合成預覽：底圖＋熱點＋底欄 |
| `gen_a1_assets.py` | — | 重生成腳本 |
| `A1_assets.md` | — | 本文件 |
| `README.md` | — | 短說明 |

---

## 熱點映射表（已鎖定，同 A0）

| file id | 分類 | 牧場物件 | 底圖大約座標 (cx, cy) |
|---------|------|----------|------------------------|
| `hotspot_food_table_stove` | 飲食 | 餐桌灶 | 220, 780 |
| `hotspot_transit_path_bike` | 交通 | 小路單車 | 540, 860 |
| `hotspot_home_porch` | 住屋 | 門廊 | 780, 620 |
| `hotspot_daily_crate` | 日用 | 木箱 | 130, 920 |
| `hotspot_fun_garden_pond` | 娛樂 | 花圃魚塘 | 900, 820 |
| `hotspot_health_herbs` | 健康 | 藥草 | 380, 700 |
| `hotspot_income_mail_basket` | 收入 | 郵箱收成籃 | 650, 740 |
| `hotspot_save_piggy` | 儲蓄 | 撲滿 | 430, 900 |
| `hotspot_other_sign` | 其他 | 告示牌 | 960, 680 |

座標相對 `spring_ranch_base.png`（1080×1200）。

---

## 色票（A0 春＋基礎 UI）

- Sky `#A8D4E6`　Grass `#7CB86A`　Blossom `#F2A7B8`　Soil `#A67C52`
- Wood `#6B4A2E`　Cream `#F5E6C8`　Ink `#3D2A1A`　Stamp `#C45C4A`

禁止官方商標角色／Logo。

---

## 重生成

```bash
/workspace/.venv/bin/python /workspace/farm-ledger/docs/art/a1/gen_a1_assets.py
```
