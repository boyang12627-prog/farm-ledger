#!/usr/bin/env python3
"""Generate A2 ledger/diary paper + missed-entry overlays for 牧場手帳."""
from PIL import Image, ImageDraw, ImageFont, ImageFilter, ImageEnhance
import os
import math
import random

OUT = "/workspace/farm-ledger/docs/art/a2"
A1_BASE = "/workspace/farm-ledger/docs/art/a1/spring_ranch_base.png"
FONT_REG = "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"
FONT_BOLD = "/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc"
TC = 3  # Traditional Chinese

WOOD, CREAM, INK, STAMP = "#6B4A2E", "#F5E6C8", "#3D2A1A", "#C45C4A"
SKY, GRASS, BLOSSOM, SOIL = "#A8D4E6", "#7CB86A", "#F2A7B8", "#A67C52"
LEAF_DEEP, CLOUD = "#4E8A45", "#E8F4F8"
BASE_W, BASE_H = 1080, 1200

LOCKED_MAP = [
    ("飲食", "餐桌灶"), ("交通", "小路單車"), ("住屋", "門廊"),
    ("日用", "木箱"), ("娛樂", "花圃魚塘"), ("健康", "藥草"),
    ("收入", "郵箱收成籃"), ("儲蓄", "撲滿"), ("其他", "告示牌"),
]


def font(size, bold=False):
    return ImageFont.truetype(FONT_BOLD if bold else FONT_REG, size, index=TC)


def hex_to_rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i : i + 2], 16) for i in (0, 2, 4))


def rgba(h, a=255):
    return hex_to_rgb(h) + (a,)


def soft_ellipse(base, bbox, fill, blur=2):
    layer = Image.new("RGBA", base.size, (0, 0, 0, 0))
    ImageDraw.Draw(layer).ellipse(bbox, fill=fill)
    if blur:
        layer = layer.filter(ImageFilter.GaussianBlur(blur))
    return Image.alpha_composite(base, layer)


def paper_texture(img, strength=8):
    """Light noise on cream paper for half-painted paper feel."""
    w, h = img.size
    noise = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    nd = ImageDraw.Draw(noise)
    rng = random.Random(42)
    for _ in range(w * h // 180):
        x, y = rng.randint(0, w - 1), rng.randint(0, h - 1)
        a = rng.randint(4, strength)
        c = rng.choice([(61, 42, 26, a), (107, 74, 46, a), (255, 255, 255, a)])
        nd.point((x, y), fill=c)
    return Image.alpha_composite(img.convert("RGBA"), noise)


def draw_wood_frame(d, box, outer=14, inner=2):
    """Outer wood + ink trim for panels."""
    x0, y0, x1, y1 = box
    d.rounded_rectangle([x0, y0, x1, y1], radius=14, fill=hex_to_rgb(WOOD),
                        outline=hex_to_rgb(INK), width=3)
    d.rounded_rectangle(
        [x0 + outer, y0 + outer, x1 - outer, y1 - outer],
        radius=10,
        fill=hex_to_rgb(CREAM),
        outline=hex_to_rgb(INK),
        width=inner,
    )


def draw_ruled_lines(d, x0, y0, x1, y1, step=36, alpha=70):
    wood = hex_to_rgb(WOOD) + (alpha,)
    for ly in range(y0, y1, step):
        d.line([(x0, ly), (x1, ly)], fill=wood, width=1)


def draw_stamp(d, cx, cy, text="有記", size=56):
    """Rubber stamp look in stamp red."""
    stamp = hex_to_rgb(STAMP)
    w, h = size * 1.6, size * 1.1
    box = [cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2]
    d.rounded_rectangle(box, radius=6, outline=stamp, width=5)
    d.rounded_rectangle(
        [box[0] + 7, box[1] + 7, box[2] - 7, box[3] - 7],
        radius=4, outline=stamp, width=2,
    )
    d.text((cx, cy - 2), text, fill=stamp, font=font(int(size * 0.55), bold=True), anchor="mm")


def draw_mini_hotspot(d, cx, cy, cat, size=36):
    """Tiny category stamp icon (same vocabulary as A0/A1)."""
    ink, stamp, cream, wood = map(hex_to_rgb, (INK, STAMP, CREAM, WOOD))
    grass, blossom, soil = map(hex_to_rgb, (GRASS, BLOSSOM, SOIL))
    r = size // 2
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=cream, outline=stamp, width=2)
    s = size * 0.32
    if cat == "飲食":
        d.rectangle([cx - s, cy - s * 0.2, cx + s, cy + s * 0.5], fill=wood, outline=ink, width=1)
        d.ellipse([cx - s * 0.45, cy - s * 0.85, cx + s * 0.45, cy - s * 0.15], fill=stamp, outline=ink, width=1)
    elif cat == "交通":
        d.ellipse([cx - s, cy - s * 0.25, cx - s * 0.2, cy + s * 0.5], outline=ink, width=2)
        d.ellipse([cx + s * 0.2, cy - s * 0.25, cx + s, cy + s * 0.5], outline=ink, width=2)
        d.line([(cx - s * 0.55, cy), (cx + s * 0.55, cy - s * 0.3)], fill=ink, width=2)
    elif cat == "住屋":
        d.polygon([(cx, cy - s), (cx - s, cy - s * 0.15), (cx + s, cy - s * 0.15)],
                  fill=hex_to_rgb("#C45C2A"), outline=ink)
        d.rectangle([cx - s * 0.65, cy - s * 0.15, cx + s * 0.65, cy + s * 0.65],
                    fill=hex_to_rgb("#E8D4B0"), outline=ink, width=1)
    elif cat == "日用":
        d.rectangle([cx - s, cy - s * 0.5, cx + s, cy + s * 0.65], fill=hex_to_rgb("#C4A35A"), outline=ink, width=2)
        d.line([(cx - s, cy), (cx + s, cy)], fill=ink, width=1)
        d.line([(cx, cy - s * 0.5), (cx, cy + s * 0.65)], fill=ink, width=1)
    elif cat == "娛樂":
        d.ellipse([cx - s * 0.9, cy, cx + s * 0.15, cy + s * 0.75], fill=hex_to_rgb("#6EB5D9"), outline=ink, width=1)
        d.ellipse([cx + s * 0.05, cy - s * 0.65, cx + s * 0.85, cy + s * 0.05], fill=blossom, outline=ink, width=1)
    elif cat == "健康":
        d.ellipse([cx - s * 0.55, cy - s * 0.25, cx + s * 0.55, cy + s * 0.7], fill=grass, outline=ink, width=1)
        d.line([(cx, cy + s * 0.7), (cx, cy - s * 0.75)], fill=hex_to_rgb(LEAF_DEEP), width=2)
    elif cat == "收入":
        d.rectangle([cx - s * 0.85, cy - s * 0.45, cx - s * 0.1, cy + s * 0.35], fill=stamp, outline=ink, width=1)
        d.ellipse([cx + s * 0.05, cy - s * 0.05, cx + s * 0.9, cy + s * 0.65],
                  fill=hex_to_rgb("#C4A35A"), outline=ink, width=1)
    elif cat == "儲蓄":
        d.ellipse([cx - s, cy - s * 0.4, cx + s, cy + s * 0.65], fill=blossom, outline=ink, width=2)
        d.rectangle([cx - s * 0.28, cy - s * 0.5, cx + s * 0.28, cy - s * 0.3], fill=ink)
    else:
        d.rectangle([cx - s * 0.1, cy - s * 0.15, cx + s * 0.1, cy + s], fill=wood, outline=ink, width=1)
        d.rectangle([cx - s, cy - s * 0.85, cx + s, cy - s * 0.05],
                    fill=hex_to_rgb("#E8D4B0"), outline=ink, width=2)


def make_ledger_paper():
    """Cream ledger panel ~1080×720, wood frame, HKD + 種子幣 columns."""
    W, H = 1080, 720
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    draw_wood_frame(d, [8, 8, W - 9, H - 9], outer=16, inner=2)

    # Inner paper margin
    mx0, my0, mx1, my1 = 30, 30, W - 31, H - 31
    img = paper_texture(img, strength=10)
    d = ImageDraw.Draw(img)

    # Header bar
    d.rounded_rectangle([mx0 + 12, my0 + 10, mx1 - 12, my0 + 78], radius=8,
                        fill=hex_to_rgb("#E8D4B0"), outline=hex_to_rgb(WOOD), width=2)
    d.text(((mx0 + mx1) // 2, my0 + 32), "牧場手帳 · 帳簿", fill=hex_to_rgb(INK),
           font=font(28, bold=True), anchor="mm")
    d.text(((mx0 + mx1) // 2, my0 + 60), "真實金額＝港幣 HKD　｜　種子幣＝獨立獎勵欄",
           fill=hex_to_rgb(WOOD), font=font(14), anchor="mm")

    # Category stamp row
    stamp_y = my0 + 118
    d.text((mx0 + 28, stamp_y - 22), "分類印章", fill=hex_to_rgb(WOOD), font=font(13, bold=True))
    n = len(LOCKED_MAP)
    gap = (mx1 - mx0 - 40) / n
    for i, (cat, _obj) in enumerate(LOCKED_MAP):
        cx = int(mx0 + 20 + gap * (i + 0.5))
        draw_mini_hotspot(d, cx, stamp_y + 8, cat, size=34)
        d.text((cx, stamp_y + 32), cat, fill=hex_to_rgb(INK), font=font(11, bold=True), anchor="mt")

    # Ruled ledger area
    rule_top = stamp_y + 58
    rule_bot = my1 - 70
    draw_ruled_lines(d, mx0 + 28, rule_top, mx1 - 28, rule_bot, step=34, alpha=80)

    # Column headers
    hdr_y = rule_top + 6
    d.text((mx0 + 36, hdr_y), "分類／物件", fill=hex_to_rgb(WOOD), font=font(15, bold=True))
    d.text((420, hdr_y), "備註", fill=hex_to_rgb(WOOD), font=font(15, bold=True))
    d.text((720, hdr_y), "港幣 HKD", fill=hex_to_rgb(WOOD), font=font(15, bold=True), anchor="rm")
    d.text((mx1 - 40, hdr_y), "種子幣", fill=hex_to_rgb(WOOD), font=font(15, bold=True), anchor="rm")
    d.line([(mx0 + 28, hdr_y + 22), (mx1 - 28, hdr_y + 22)], fill=hex_to_rgb(INK), width=2)

    rows = [
        ("飲食", "餐桌灶", "−HK$48", "+2", "早餐食材"),
        ("收入", "郵箱收成籃", "+HK$320", "+12", "週末市集"),
        ("日用", "木箱", "−HK$26", "+1", "肥皂毛巾"),
        ("交通", "小路單車", "−HK$18", "+1", "單車維修"),
        ("健康", "藥草", "−HK$65", "+3", "藥草茶"),
    ]
    ry = hdr_y + 36
    for cat, obj, hkd, seed, note in rows:
        d.text((mx0 + 36, ry), f"【{cat}】{obj}", fill=hex_to_rgb(INK), font=font(17))
        d.text((420, ry), note, fill=hex_to_rgb(WOOD), font=font(15))
        hc = hex_to_rgb("#3F7A38") if hkd.startswith("+") else hex_to_rgb(STAMP)
        d.text((720, ry), hkd, fill=hc, font=font(17, bold=True), anchor="rm")
        d.text((mx1 - 40, ry), seed, fill=hex_to_rgb(WOOD), font=font(17, bold=True), anchor="rm")
        ry += 34

    # Stamp
    draw_stamp(d, mx1 - 130, rule_bot - 40, "有記", size=52)

    # Footnote
    d.text(((mx0 + mx1) // 2, my1 - 28),
           "※ 帳簿金額＝港幣 HKD｜種子幣 ≠ HKD，無線性換算｜禁止裸「幣」作帳戶金額",
           fill=hex_to_rgb(WOOD), font=font(13), anchor="mm")

    path = os.path.join(OUT, "ledger_paper.png")
    img.convert("RGBA").save(path, "PNG", optimize=True)
    print("Wrote", path, img.size)
    return path


def make_diary_cover():
    """Diary cover: cream + wood + soft ranch motif, title「日記」."""
    W, H = 720, 960
    img = Image.new("RGBA", (W, H), rgba(CREAM))
    d = ImageDraw.Draw(img)

    # Outer wood board
    d.rounded_rectangle([12, 12, W - 13, H - 13], radius=18,
                        fill=hex_to_rgb(WOOD), outline=hex_to_rgb(INK), width=4)
    # Inner cream cover
    d.rounded_rectangle([36, 36, W - 37, H - 37], radius=12,
                        fill=hex_to_rgb(CREAM), outline=hex_to_rgb(INK), width=2)
    # Decorative inner frame
    d.rounded_rectangle([52, 52, W - 53, H - 53], radius=8,
                        outline=hex_to_rgb(WOOD), width=2)

    img = paper_texture(img, strength=12)
    d = ImageDraw.Draw(img)

    # Soft ranch motif — sky band + hills + tiny house + blossom tree
    motif = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    md = ImageDraw.Draw(motif)
    # sky wash
    for yi in range(120, 380):
        t = (yi - 120) / 260
        a = int(90 * (1 - t * 0.4))
        c = hex_to_rgb(SKY) + (a,)
        md.line([(70, yi), (W - 70, yi)], fill=c)
    # hills
    md.ellipse([40, 280, 380, 460], fill=rgba(LEAF_DEEP, 140))
    md.ellipse([280, 300, 680, 480], fill=rgba(GRASS, 150))
    # grass band
    md.rectangle([70, 400, W - 70, 520], fill=rgba(GRASS, 100))
    # tiny house
    hx, hy = 480, 420
    md.polygon([(hx, hy - 50), (hx - 55, hy - 10), (hx + 55, hy - 10)],
               fill=rgba("#C45C2A", 200), outline=rgba(INK, 220))
    md.rectangle([hx - 42, hy - 10, hx + 42, hy + 55], fill=rgba("#E8D4B0", 210), outline=rgba(INK, 220), width=2)
    md.rectangle([hx - 12, hy + 15, hx + 12, hy + 55], fill=rgba(WOOD, 220), outline=rgba(INK, 220), width=1)
    # blossom tree
    tx, ty = 200, 400
    md.rectangle([tx - 6, ty, tx + 6, ty + 70], fill=rgba(WOOD, 210), outline=rgba(INK, 200), width=1)
    for ox, oy, r in [(-30, -15, 32), (28, -10, 30), (0, -38, 34), (-18, 12, 24)]:
        md.ellipse([tx + ox - r, ty + oy - r, tx + ox + r, ty + oy + r],
                   fill=rgba(BLOSSOM, 200), outline=rgba(INK, 180), width=1)
    # tiny fence
    for fx in range(100, 620, 40):
        md.rectangle([fx, 500, fx + 8, 530], fill=rgba(WOOD, 160), outline=rgba(INK, 140), width=1)
    md.line([(100, 512), (620, 512)], fill=rgba(WOOD, 160), width=3)

    motif = motif.filter(ImageFilter.GaussianBlur(0.6))
    img = Image.alpha_composite(img, motif)
    d = ImageDraw.Draw(img)

    # Title plate
    d.rounded_rectangle([W // 2 - 160, 560, W // 2 + 160, 680], radius=10,
                        fill=hex_to_rgb(CREAM), outline=hex_to_rgb(WOOD), width=4)
    d.rounded_rectangle([W // 2 - 148, 572, W // 2 + 148, 668], radius=6,
                        outline=hex_to_rgb(INK), width=2)
    d.text((W // 2, 620), "日記", fill=hex_to_rgb(INK), font=font(64, bold=True), anchor="mm")

    # Subtitle
    d.text((W // 2, 720), "牧場手帳", fill=hex_to_rgb(WOOD), font=font(24, bold=True), anchor="mm")
    d.text((W // 2, 760), "Ranch Diary", fill=hex_to_rgb(WOOD), font=font(16), anchor="mm")

    # Corner blossoms
    for cx, cy in [(90, 90), (W - 90, 90), (90, H - 90), (W - 90, H - 90)]:
        d.ellipse([cx - 18, cy - 18, cx + 18, cy + 18], fill=hex_to_rgb(BLOSSOM), outline=hex_to_rgb(INK), width=2)
        d.ellipse([cx - 6, cy - 6, cx + 6, cy + 6], fill=hex_to_rgb("#F5C542"), outline=hex_to_rgb(INK), width=1)

    # Spine hint (left)
    d.rectangle([36, 36, 48, H - 37], fill=rgba(WOOD, 180))

    path = os.path.join(OUT, "diary_cover.png")
    img.convert("RGBA").save(path, "PNG", optimize=True)
    print("Wrote", path, img.size)
    return path


def make_diary_page():
    """Open diary inner page — lined cream, space for day note; no fake HKD-as-coin."""
    W, H = 900, 1100
    img = Image.new("RGBA", (W, H), rgba(CREAM))
    d = ImageDraw.Draw(img)

    # Soft wood outer (open book feel — left binding)
    d.rectangle([0, 0, W - 1, H - 1], outline=hex_to_rgb(WOOD), width=10)
    d.rectangle([8, 8, W - 9, H - 9], outline=hex_to_rgb(INK), width=2)

    img = paper_texture(img, strength=10)
    d = ImageDraw.Draw(img)

    # Binding gutter
    for gx in range(28, 52, 4):
        d.line([(gx, 40), (gx, H - 40)], fill=rgba(WOOD, 40), width=1)
    d.ellipse([34, 80, 48, 100], fill=rgba(WOOD, 120), outline=rgba(INK, 160), width=1)
    d.ellipse([34, H // 2 - 10, 48, H // 2 + 10], fill=rgba(WOOD, 120), outline=rgba(INK, 160), width=1)
    d.ellipse([34, H - 100, 48, H - 80], fill=rgba(WOOD, 120), outline=rgba(INK, 160), width=1)

    # Red margin line (classic diary)
    margin_x = 110
    d.line([(margin_x, 60), (margin_x, H - 60)], fill=rgba(STAMP, 90), width=2)

    # Header
    d.text((W // 2 + 20, 55), "牧場日記", fill=hex_to_rgb(INK), font=font(28, bold=True), anchor="mm")
    d.rounded_rectangle([margin_x + 20, 85, W - 50, 130], radius=6,
                        fill=hex_to_rgb("#E8D4B0"), outline=hex_to_rgb(WOOD), width=1)
    d.text((margin_x + 40, 107), "日期：____年____月____日　天氣：　　　心情：",
           fill=hex_to_rgb(WOOD), font=font(16), anchor="lm")

    # Ruled lines for writing
    line_top = 160
    line_bot = H - 160
    step = 40
    for ly in range(line_top, line_bot, step):
        d.line([(margin_x + 10, ly), (W - 50, ly)], fill=rgba(WOOD, 75), width=1)

    # Soft prompt hints (not money)
    prompts = [
        "今日牧場小事…",
        "",
        "收成／照料／心情…",
        "",
        "",
        "（此頁只記日記，金額請見帳簿・港幣 HKD）",
    ]
    py = line_top + 8
    for p in prompts:
        if p:
            d.text((margin_x + 24, py), p, fill=rgba(WOOD, 110), font=font(18))
        py += step

    # Small ranch doodle corner (not currency)
    doodle_x, doodle_y = W - 160, H - 200
    d.ellipse([doodle_x, doodle_y, doodle_x + 70, doodle_y + 40],
              fill=rgba(SKY, 100), outline=rgba(INK, 100), width=1)
    d.ellipse([doodle_x + 40, doodle_y - 30, doodle_x + 100, doodle_y + 30],
              fill=rgba(BLOSSOM, 120), outline=rgba(INK, 100), width=1)
    d.rectangle([doodle_x + 62, doodle_y + 20, doodle_x + 72, doodle_y + 55],
                fill=rgba(WOOD, 140), outline=rgba(INK, 100), width=1)

    # Footnote — clarify currency separation
    d.text((W // 2, H - 40),
           "日記頁｜帳簿金額請用港幣 HKD　種子幣另欄（≠ HKD）",
           fill=hex_to_rgb(WOOD), font=font(13), anchor="mm")

    # Tiny stamp corner
    draw_stamp(d, W - 120, 90, "日記", size=40)

    path = os.path.join(OUT, "diary_page.png")
    img.convert("RGBA").save(path, "PNG", optimize=True)
    print("Wrote", path, img.size)
    return path


def make_overlay_missed_grass():
    """Transparent RGBA overgrown grass / weeds / dull tint — NOT death/reset.
    Same size as A1 base (1080×1200). Suitable to layer ON spring ranch base.
    """
    W, H = BASE_W, BASE_H
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    rng = random.Random(20260926)

    # Soft dull green wash over ground area (lower 2/3) — waiting / neglected feel
    wash = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    wd = ImageDraw.Draw(wash)
    # ground dulling
    for yi in range(420, H):
        t = (yi - 420) / (H - 420)
        a = int(28 + 35 * t)
        # gray-green dull
        wd.line([(0, yi), (W, yi)], fill=(90, 110, 70, a))
    # slight overall cool desat veil on upper
    for yi in range(0, 450):
        a = int(12 + 8 * (yi / 450))
        wd.line([(0, yi), (W, yi)], fill=(120, 125, 115, a))
    wash = wash.filter(ImageFilter.GaussianBlur(6))
    img = Image.alpha_composite(img, wash)

    layer = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)

    # Tall grass clumps / weeds — organic strokes
    weed_colors = [
        (78, 110, 55, 180),
        (95, 125, 60, 160),
        (70, 95, 48, 170),
        (110, 130, 75, 140),
        (85, 100, 55, 150),
    ]
    # denser on lower field and near hotspot zones
    clump_centers = []
    for _ in range(55):
        cx = rng.randint(40, W - 40)
        cy = rng.randint(500, H - 40)
        clump_centers.append((cx, cy))
    # extra near path / props area
    for cx, cy in [(220, 780), (540, 860), (130, 920), (430, 900), (900, 820),
                   (380, 700), (650, 740), (780, 620), (960, 680)]:
        for _ in range(4):
            clump_centers.append((cx + rng.randint(-80, 80), cy + rng.randint(-40, 60)))

    for cx, cy in clump_centers:
        blades = rng.randint(5, 12)
        for _ in range(blades):
            col = weed_colors[rng.randint(0, len(weed_colors) - 1)]
            hgt = rng.randint(18, 55)
            lean = rng.randint(-12, 12)
            wdt = rng.randint(2, 4)
            # blade as thin polygon
            tip_x = cx + lean
            tip_y = cy - hgt
            d.polygon(
                [(cx - wdt, cy), (cx + wdt, cy), (tip_x + 1, tip_y), (tip_x - 1, tip_y)],
                fill=col,
            )
        # small seed head / weed flower dull
        if rng.random() < 0.25:
            d.ellipse([cx - 4, cy - 50, cx + 4, cy - 42],
                      fill=(140, 130, 70, 150), outline=(61, 42, 26, 100))

    # Sparse taller weeds / overgrown tufts
    for _ in range(30):
        x = rng.randint(20, W - 20)
        y = rng.randint(550, H - 30)
        col = (65, 90, 45, 190)
        for i in range(3):
            ox = (i - 1) * 6
            d.polygon(
                [(x + ox - 3, y), (x + ox + 3, y), (x + ox + rng.randint(-8, 8), y - rng.randint(40, 70))],
                fill=col,
            )

    # Soft "waiting" gray patches (not death)
    for _ in range(12):
        bx = rng.randint(50, W - 100)
        by = rng.randint(600, H - 80)
        bw, bh = rng.randint(60, 160), rng.randint(20, 50)
        d.ellipse([bx, by, bx + bw, by + bh], fill=(130, 125, 110, 50))

    layer = layer.filter(ImageFilter.GaussianBlur(0.4))
    img = Image.alpha_composite(img, layer)

    # Caption strip (transparent area with hint text — optional small label bottom)
    d2 = ImageDraw.Draw(img)
    d2.rounded_rectangle([W // 2 - 200, H - 48, W // 2 + 200, H - 12], radius=6,
                         fill=(245, 230, 200, 200), outline=rgba(WOOD, 180), width=1)
    d2.text((W // 2, H - 30), "漏記・長草等待（非死亡）", fill=hex_to_rgb(INK),
            font=font(16, bold=True), anchor="mm")

    path = os.path.join(OUT, "overlay_missed_grass.png")
    img.save(path, "PNG", optimize=True)
    print("Wrote", path, img.size, img.mode)
    return path


def make_overlay_missed_gray():
    """Lighter gray / wait dull overlay (RGBA), same size as base."""
    W, H = BASE_W, BASE_H
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Soft gray veil — waiting / paused, not harsh
    for yi in range(H):
        t = yi / H
        # slightly stronger on ground
        a = int(35 + 25 * t)
        d.line([(0, yi), (W, yi)], fill=(140, 138, 132, a))

    img = img.filter(ImageFilter.GaussianBlur(8))

    # Soft vignette corners
    vig = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    vd = ImageDraw.Draw(vig)
    for r, a in [(80, 40), (140, 25)]:
        vd.ellipse([-r, -r, r * 2, r * 2], fill=(100, 100, 95, a))
        vd.ellipse([W - r * 2, -r, W + r, r * 2], fill=(100, 100, 95, a))
        vd.ellipse([-r, H - r * 2, r * 2, H + r], fill=(100, 100, 95, a))
        vd.ellipse([W - r * 2, H - r * 2, W + r, H + r], fill=(100, 100, 95, a))
    vig = vig.filter(ImageFilter.GaussianBlur(20))
    img = Image.alpha_composite(img, vig)

    # Small wait chip
    d2 = ImageDraw.Draw(img)
    d2.rounded_rectangle([W // 2 - 120, H - 48, W // 2 + 120, H - 12], radius=6,
                         fill=(245, 230, 200, 190), outline=rgba(WOOD, 160), width=1)
    d2.text((W // 2, H - 30), "等待・灰調", fill=hex_to_rgb(INK),
            font=font(16, bold=True), anchor="mm")

    path = os.path.join(OUT, "overlay_missed_gray.png")
    img.save(path, "PNG", optimize=True)
    print("Wrote", path, img.size, img.mode)
    return path


def make_preview(ledger_path, cover_path, page_path, grass_path, gray_path):
    """One sheet: ranch+grass vs clean; bottom ledger + diary; HKD vs 種子幣 footnote."""
    base = Image.open(A1_BASE).convert("RGBA")
    grass = Image.open(grass_path).convert("RGBA")
    gray = Image.open(gray_path).convert("RGBA")
    ledger = Image.open(ledger_path).convert("RGBA")
    cover = Image.open(cover_path).convert("RGBA")
    page = Image.open(page_path).convert("RGBA")

    # Layout: 1080 wide mobile-friendly sheet
    # Top: two ranch thumbs side by side (scaled)
    thumb_w = 500
    scale = thumb_w / BASE_W
    thumb_h = int(BASE_H * scale)

    clean = base.resize((thumb_w, thumb_h), Image.Resampling.LANCZOS)
    grassy = Image.alpha_composite(base, grass).resize((thumb_w, thumb_h), Image.Resampling.LANCZOS)
    # also show gray briefly as tiny badge — or use grassy as primary missed

    pad = 24
    header_h = 70
    label_h = 36
    gap = 20
    bottom_h = 520
    footnote_h = 70

    W = 1080
    H = header_h + label_h + thumb_h + gap + bottom_h + footnote_h + pad * 2

    sheet = Image.new("RGBA", (W, H), rgba(CREAM))
    d = ImageDraw.Draw(sheet)

    # Wood border
    d.rectangle([0, 0, W - 1, H - 1], outline=hex_to_rgb(WOOD), width=12)
    d.rectangle([10, 10, W - 11, H - 11], outline=hex_to_rgb(INK), width=2)

    sheet = paper_texture(sheet, strength=6)
    d = ImageDraw.Draw(sheet)

    d.text((W // 2, 40), "牧場手帳 · A2 美術預覽", fill=hex_to_rgb(INK),
           font=font(30, bold=True), anchor="mm")
    d.text((W // 2, 62), "Ledger / Diary paper + missed-entry overlays",
           fill=hex_to_rgb(WOOD), font=font(14), anchor="mm")

    # Labels
    y0 = header_h + 8
    left_x = (W // 2 - gap // 2 - thumb_w) // 2 + pad // 2
    # better centering
    left_x = (W - gap) // 2 - thumb_w
    right_x = (W + gap) // 2
    # adjust to center the pair
    total_pair = thumb_w * 2 + gap
    left_x = (W - total_pair) // 2
    right_x = left_x + thumb_w + gap

    d.text((left_x + thumb_w // 2, y0 + 14), "春季牧場（清潔）", fill=hex_to_rgb(INK),
           font=font(16, bold=True), anchor="mm")
    d.text((right_x + thumb_w // 2, y0 + 14), "漏記・長草／等待 overlay", fill=hex_to_rgb(INK),
           font=font(16, bold=True), anchor="mm")

    y_thumb = y0 + label_h
    # frames
    for x in (left_x, right_x):
        d.rectangle([x - 4, y_thumb - 4, x + thumb_w + 4, y_thumb + thumb_h + 4],
                    outline=hex_to_rgb(WOOD), width=3)

    sheet.paste(clean, (left_x, y_thumb), clean)
    sheet.paste(grassy, (right_x, y_thumb), grassy)

    # Bottom section: ledger + diary cover + diary page
    y_bot = y_thumb + thumb_h + gap + 8
    d.text((W // 2, y_bot), "帳簿紙／日記封面／日記內頁", fill=hex_to_rgb(INK),
           font=font(18, bold=True), anchor="mm")

    # Scale bottom assets to fit
    avail_h = bottom_h - 50
    # ledger takes more width
    led_h = avail_h - 10
    led_w = int(ledger.width * led_h / ledger.height)
    if led_w > 520:
        led_w = 520
        led_h = int(ledger.height * led_w / ledger.width)
    ledger_s = ledger.resize((led_w, led_h), Image.Resampling.LANCZOS)

    cov_h = avail_h - 10
    cov_w = int(cover.width * cov_h / cover.height)
    cover_s = cover.resize((cov_w, cov_h), Image.Resampling.LANCZOS)

    pg_h = avail_h - 10
    pg_w = int(page.width * pg_h / page.height)
    page_s = page.resize((pg_w, pg_h), Image.Resampling.LANCZOS)

    # layout three items
    spacing = 16
    total_w = led_w + cov_w + pg_w + spacing * 2
    # if too wide, shrink
    if total_w > W - 40:
        scale2 = (W - 40) / total_w
        led_w, led_h = int(led_w * scale2), int(led_h * scale2)
        cov_w, cov_h = int(cov_w * scale2), int(cov_h * scale2)
        pg_w, pg_h = int(pg_w * scale2), int(pg_h * scale2)
        ledger_s = ledger.resize((led_w, led_h), Image.Resampling.LANCZOS)
        cover_s = cover.resize((cov_w, cov_h), Image.Resampling.LANCZOS)
        page_s = page.resize((pg_w, pg_h), Image.Resampling.LANCZOS)
        total_w = led_w + cov_w + pg_w + spacing * 2

    bx = (W - total_w) // 2
    by = y_bot + 28
    sheet.paste(ledger_s, (bx, by + (avail_h - led_h) // 2), ledger_s)
    bx2 = bx + led_w + spacing
    sheet.paste(cover_s, (bx2, by + (avail_h - cov_h) // 2), cover_s)
    bx3 = bx2 + cov_w + spacing
    sheet.paste(page_s, (bx3, by + (avail_h - pg_h) // 2), page_s)

    # Footnote
    fy = H - footnote_h // 2 - 8
    d.rounded_rectangle([40, H - footnote_h + 8, W - 40, H - 18], radius=8,
                        fill=hex_to_rgb("#E8D4B0"), outline=hex_to_rgb(WOOD), width=2)
    d.text((W // 2, H - footnote_h // 2 - 4),
           "※ 帳簿真實金額＝港幣 HKD　｜　種子幣＝獨立欄（≠ HKD，無線性換算）　｜　漏記＝長草／灰／等待（非死亡）",
           fill=hex_to_rgb(INK), font=font(14, bold=True), anchor="mm")

    out = sheet.convert("RGB")
    path = os.path.join(OUT, "a2_preview.png")
    out.save(path, "PNG", optimize=True)
    print("Wrote", path, out.size)
    return path


def write_docs():
    assets_md = """# A2 Art Assets · 牧場手帳（帳簿紙／日記／漏記 overlay）

> 版本：A2　日期：2026-09-26  
> 風格：跟 A0 色票／A1 牧場底；Q 版半像素半手繪（Pillow）

---

## 貨幣顯示規則（繼承 A0）

| 層 | 顯示 | 說明 |
|----|------|------|
| 真實帳簿金額 | **港幣 HKD**（例 −HK$48、+HK$320） | 帳戶流水 |
| 牧場獎勵 | **種子幣**（獨立欄／欄位） | ≠ HKD，**無線性換算**；禁止用裸「幣」當帳戶金額 |

日記內頁**不**把 HKD 偽裝成農場幣。

---

## 漏記視覺（Missed entries）

| 表現 | 說明 |
|------|------|
| 長草／雜草 overlay | `overlay_missed_grass.png` — 可疊在 `spring_ranch_base.png` 上 |
| 灰／等待 overlay | `overlay_missed_gray.png` — 較淡灰調 |
| **禁止** | 死亡、重置、枯骨、遊戲結束感 |

---

## 檔案清單與尺寸

| 檔案 | 尺寸 | 說明 |
|------|------|------|
| `ledger_paper.png` | 1080×720 | 奶油紙帳簿面板（木框）；樣例 HKD＋種子幣欄；分類印章列 |
| `diary_cover.png` | 720×960 | 日記封面（奶油＋木＋柔牧場母題；標題「日記」） |
| `diary_page.png` | 900×1100 | 日記內頁（罫線奶油紙；日註空間；無假農場幣金額） |
| `overlay_missed_grass.png` | 1080×1200 | 透明 RGBA 長草／等待 overlay（同 A1 底圖尺寸） |
| `overlay_missed_gray.png` | 1080×1200 | 透明 RGBA 灰調等待 overlay |
| `a2_preview.png` | ~1080×H | 合成預覽：牧場清潔 vs 長草；帳簿＋日記 |
| `gen_a2_assets.py` | — | 重生成腳本 |
| `A2_assets.md` | — | 本文件 |
| `README.md` | — | 短說明 |

---

## 熱點映射（已鎖定，同 A0／A1）

飲食＝餐桌灶｜交通＝小路單車｜住屋＝門廊｜日用＝木箱｜娛樂＝花圃魚塘｜健康＝藥草｜收入＝郵箱收成籃｜儲蓄＝撲滿｜其他＝告示牌

帳簿面板頂部有九分類印章列作視覺提示。

---

## 色票（A0 春＋基礎 UI）

- Sky `#A8D4E6`　Grass `#7CB86A`　Blossom `#F2A7B8`　Soil `#A67C52`
- Wood `#6B4A2E`　Cream `#F5E6C8`　Ink `#3D2A1A`　Stamp `#C45C4A`

禁止官方商標角色／Logo。

---

## 重生成

```bash
/workspace/.venv/bin/python /workspace/farm-ledger/docs/art/a2/gen_a2_assets.py
```

需已有 A1 `spring_ranch_base.png`（預覽合成用）。
"""
    readme = """# A2 Art · 牧場手帳

帳簿奶油紙、日記封面／內頁，以及漏記時的長草／灰調透明 overlay。

| 重點檔 | 用途 |
|--------|------|
| `ledger_paper.png` | 帳簿面板（HKD＋種子幣分欄） |
| `diary_cover.png` | 日記封面「日記」 |
| `diary_page.png` | 日記內頁罫線紙 |
| `overlay_missed_grass.png` | 漏記・長草 overlay（1080×1200） |
| `overlay_missed_gray.png` | 漏記・灰／等待 overlay |
| `a2_preview.png` | 單頁合成預覽 |
| `A2_assets.md` | 完整清單／規則 |
| `gen_a2_assets.py` | Pillow 重生成 |

**貨幣**：帳簿＝港幣 HKD；種子幣＝獨立獎勵欄（≠ HKD）。  
**漏記**：長草／灰／等待 — 非死亡／重置。

重生成：`/workspace/.venv/bin/python gen_a2_assets.py`
"""
    with open(os.path.join(OUT, "A2_assets.md"), "w", encoding="utf-8") as f:
        f.write(assets_md)
    with open(os.path.join(OUT, "README.md"), "w", encoding="utf-8") as f:
        f.write(readme)
    print("Wrote A2_assets.md + README.md")


def main():
    os.makedirs(OUT, exist_ok=True)
    ledger = make_ledger_paper()
    cover = make_diary_cover()
    page = make_diary_page()
    grass = make_overlay_missed_grass()
    gray = make_overlay_missed_gray()
    make_preview(ledger, cover, page, grass, gray)
    write_docs()
    print("A2 assets done →", OUT)


if __name__ == "__main__":
    main()
