#!/usr/bin/env python3
"""Generate A0 palette swatch + sample sheet for 牧場手帳 art bible."""
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os

OUT = "/workspace/farm-ledger/docs/art/a0"
FONT_REG = "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"
FONT_BOLD = "/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc"
TC = 3  # Traditional Chinese

def font(size, bold=False):
    return ImageFont.truetype(FONT_BOLD if bold else FONT_REG, size, index=TC)

WOOD, CREAM, INK, STAMP = "#6B4A2E", "#F5E6C8", "#3D2A1A", "#C45C4A"
SPRING = {"sky": "#A8D4E6", "grass": "#7CB86A", "blossom": "#F2A7B8", "soil": "#A67C52"}
SUMMER = {"sky": "#6EB5D9", "grass": "#5A9E4B", "sun": "#F5C542", "leaf": "#3F7A38"}
AUTUMN = {"sky": "#D4B896", "grass": "#C4A35A", "leaf": "#C45C2A", "soil": "#8B5A3C"}
WINTER = {"sky": "#C5D5E0", "snow": "#F2F5F7", "pine": "#4A6B55", "wood": "#6B4A2E"}
SPRING_ACC = {"leaf_deep": "#4E8A45", "cloud": "#E8F4F8"}
SUMMER_ACC = {"warm_edge": "#E8A838"}
AUTUMN_ACC = {"amber": "#E09A3E"}
WINTER_ACC = {"frost": "#A8BCC8"}

LOCKED_MAP = [
    ("飲食", "餐桌灶"), ("交通", "小路單車"), ("住屋", "門廊"),
    ("日用", "木箱"), ("娛樂", "花圃魚塘"), ("健康", "藥草"),
    ("收入", "郵箱收成籃"), ("儲蓄", "撲滿"), ("其他", "告示牌"),
]
NAV = ["牧場", "入帳", "帳簿", "日記"]


def hex_to_rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))


def soft_ellipse(base, bbox, fill, blur=1):
    layer = Image.new("RGBA", base.size, (0, 0, 0, 0))
    ImageDraw.Draw(layer).ellipse(bbox, fill=fill)
    if blur:
        layer = layer.filter(ImageFilter.GaussianBlur(blur))
    return Image.alpha_composite(base, layer)


def draw_q_building(img, cx, cy, w, h, roof, wall):
    d = ImageDraw.Draw(img)
    x0, y0 = cx - w // 2, cy - h // 2
    d.rectangle([x0, y0 + h // 3, x0 + w, y0 + h], fill=hex_to_rgb(wall),
                outline=hex_to_rgb(INK), width=2)
    d.polygon([(cx, y0), (x0 - 4, y0 + h // 3 + 4), (x0 + w + 4, y0 + h // 3 + 4)],
              fill=hex_to_rgb(roof), outline=hex_to_rgb(INK))
    dw, dh = w // 4, h // 3
    d.rectangle([cx - dw // 2, y0 + h - dh, cx + dw // 2, y0 + h],
                fill=hex_to_rgb(WOOD), outline=hex_to_rgb(INK), width=1)
    ww = w // 5
    d.rectangle([cx - w // 3, y0 + h // 2, cx - w // 3 + ww, y0 + h // 2 + ww],
                fill=hex_to_rgb("#A8D4E6"), outline=hex_to_rgb(INK), width=1)


def draw_hotspot_icon(d, x, y, size, kind):
    ink, stamp, cream, wood = map(hex_to_rgb, (INK, STAMP, CREAM, WOOD))
    grass = hex_to_rgb(SPRING["grass"])
    blossom = hex_to_rgb(SPRING["blossom"])
    soil = hex_to_rgb(SPRING["soil"])
    r = size // 2
    d.ellipse([x - r, y - r, x + r, y + r], fill=cream, outline=stamp, width=3)
    s = size * 0.35
    if kind == "飲食":
        d.rectangle([x - s, y - s * 0.2, x + s, y + s * 0.5], fill=wood, outline=ink, width=1)
        d.ellipse([x - s * 0.5, y - s * 0.9, x + s * 0.5, y - s * 0.2], fill=stamp, outline=ink, width=1)
        d.rectangle([x - s * 0.15, y + s * 0.5, x + s * 0.15, y + s], fill=wood, outline=ink, width=1)
    elif kind == "交通":
        d.ellipse([x - s, y - s * 0.3, x - s * 0.2, y + s * 0.5], outline=ink, width=2)
        d.ellipse([x + s * 0.2, y - s * 0.3, x + s, y + s * 0.5], outline=ink, width=2)
        d.line([(x - s * 0.6, y), (x + s * 0.6, y - s * 0.4), (x + s * 0.6, y)], fill=ink, width=2)
        d.line([(x - s * 1.2, y + s * 0.7), (x + s * 1.2, y + s * 0.7)], fill=soil, width=3)
    elif kind == "住屋":
        d.polygon([(x, y - s), (x - s, y - s * 0.2), (x + s, y - s * 0.2)],
                  fill=hex_to_rgb("#C45C2A"), outline=ink)
        d.rectangle([x - s * 0.7, y - s * 0.2, x + s * 0.7, y + s * 0.7],
                    fill=hex_to_rgb("#E8D4B0"), outline=ink, width=1)
        d.rectangle([x - s * 0.25, y + s * 0.1, x + s * 0.25, y + s * 0.7], fill=wood, outline=ink, width=1)
    elif kind == "日用":
        d.rectangle([x - s, y - s * 0.6, x + s, y + s * 0.7], fill=hex_to_rgb("#C4A35A"), outline=ink, width=2)
        d.line([(x - s, y), (x + s, y)], fill=ink, width=1)
        d.line([(x, y - s * 0.6), (x, y + s * 0.7)], fill=ink, width=1)
    elif kind == "娛樂":
        d.ellipse([x - s * 0.9, y, x + s * 0.2, y + s * 0.8], fill=hex_to_rgb("#6EB5D9"), outline=ink, width=1)
        d.ellipse([x + s * 0.1, y - s * 0.7, x + s * 0.9, y + s * 0.1], fill=blossom, outline=ink, width=1)
        d.ellipse([x + s * 0.35, y - s * 0.45, x + s * 0.65, y - s * 0.15],
                  fill=hex_to_rgb("#F5C542"), outline=ink, width=1)
    elif kind == "健康":
        d.ellipse([x - s * 0.6, y - s * 0.3, x + s * 0.6, y + s * 0.8], fill=grass, outline=ink, width=1)
        d.line([(x, y + s * 0.8), (x, y - s * 0.8)], fill=hex_to_rgb("#3F7A38"), width=2)
        d.ellipse([x - s * 0.8, y - s * 0.9, x - s * 0.1, y - s * 0.2],
                  fill=hex_to_rgb("#7CB86A"), outline=ink, width=1)
        d.ellipse([x + s * 0.1, y - s * 0.9, x + s * 0.8, y - s * 0.2],
                  fill=hex_to_rgb("#5A9E4B"), outline=ink, width=1)
    elif kind == "收入":
        d.rectangle([x - s * 0.9, y - s * 0.5, x - s * 0.1, y + s * 0.4], fill=stamp, outline=ink, width=1)
        d.rectangle([x - s * 0.95, y - s * 0.55, x - s * 0.05, y - s * 0.35], fill=ink)
        d.line([(x - s * 0.5, y + s * 0.4), (x - s * 0.5, y + s)], fill=wood, width=2)
        d.ellipse([x + s * 0.05, y - s * 0.1, x + s * 0.95, y + s * 0.7],
                  fill=hex_to_rgb("#C4A35A"), outline=ink, width=1)
        d.ellipse([x + s * 0.25, y - s * 0.4, x + s * 0.55, y - s * 0.05],
                  fill=hex_to_rgb("#F5C542"), outline=ink, width=1)
    elif kind == "儲蓄":
        d.ellipse([x - s, y - s * 0.5, x + s, y + s * 0.7], fill=blossom, outline=ink, width=2)
        d.ellipse([x + s * 0.4, y - s * 0.7, x + s * 0.85, y - s * 0.25], fill=blossom, outline=ink, width=1)
        d.rectangle([x - s * 0.3, y - s * 0.55, x + s * 0.3, y - s * 0.35], fill=ink)
        d.ellipse([x - s * 0.45, y - s * 0.15, x - s * 0.15, y + s * 0.15], fill=ink)
    else:  # 其他
        d.rectangle([x - s * 0.1, y - s * 0.2, x + s * 0.1, y + s], fill=wood, outline=ink, width=1)
        d.rectangle([x - s, y - s * 0.9, x + s, y - s * 0.1], fill=hex_to_rgb("#E8D4B0"), outline=ink, width=2)
        d.line([(x - s * 0.6, y - s * 0.6), (x + s * 0.6, y - s * 0.6)], fill=ink, width=1)
        d.line([(x - s * 0.5, y - s * 0.4), (x + s * 0.5, y - s * 0.4)], fill=ink, width=1)


def draw_nav_icon(d, cx, cy, name):
    """Hand-drawn nav glyphs inside cream circle."""
    ink, cream, wood = map(hex_to_rgb, (INK, CREAM, WOOD))
    d.ellipse([cx - 24, cy - 24, cx + 24, cy + 24], fill=cream, outline=ink, width=2)
    if name == "牧場":  # little house
        d.polygon([(cx, cy - 14), (cx - 14, cy - 2), (cx + 14, cy - 2)], fill=hex_to_rgb("#C45C2A"), outline=ink)
        d.rectangle([cx - 10, cy - 2, cx + 10, cy + 14], fill=hex_to_rgb("#E8D4B0"), outline=ink, width=1)
        d.rectangle([cx - 3, cy + 4, cx + 3, cy + 14], fill=wood, outline=ink, width=1)
    elif name == "入帳":  # plus in stamp circle
        d.ellipse([cx - 12, cy - 12, cx + 12, cy + 12], outline=hex_to_rgb(STAMP), width=3)
        d.line([(cx - 8, cy), (cx + 8, cy)], fill=hex_to_rgb(STAMP), width=3)
        d.line([(cx, cy - 8), (cx, cy + 8)], fill=hex_to_rgb(STAMP), width=3)
    elif name == "帳簿":  # open book
        d.polygon([(cx, cy - 12), (cx - 14, cy - 8), (cx - 14, cy + 12), (cx, cy + 8)],
                  fill=cream, outline=ink)
        d.polygon([(cx, cy - 12), (cx + 14, cy - 8), (cx + 14, cy + 12), (cx, cy + 8)],
                  fill=hex_to_rgb("#E8D4B0"), outline=ink)
        d.line([(cx, cy - 12), (cx, cy + 8)], fill=ink, width=2)
    else:  # 日記 — pen + lines
        d.line([(cx - 10, cy - 8), (cx + 10, cy - 8)], fill=ink, width=2)
        d.line([(cx - 10, cy), (cx + 6, cy)], fill=ink, width=2)
        d.line([(cx - 10, cy + 8), (cx + 2, cy + 8)], fill=ink, width=2)
        d.polygon([(cx + 8, cy + 2), (cx + 14, cy + 14), (cx + 4, cy + 10)],
                  fill=hex_to_rgb(STAMP), outline=ink)


def make_palette():
    W, H = 1400, 1100
    img = Image.new("RGB", (W, H), hex_to_rgb(CREAM))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, W - 1, H - 1], outline=hex_to_rgb(WOOD), width=16)
    d.rectangle([12, 12, W - 13, H - 13], outline=hex_to_rgb(INK), width=2)

    label, hexf, small = font(18), font(16), font(14)
    d.text((W // 2, 48), "牧場手帳 · A0 色票", fill=hex_to_rgb(INK), font=font(42, bold=True), anchor="mt")
    d.text((W // 2, 95), "Art Bible Palette · Exact Hex", fill=hex_to_rgb(WOOD), font=font(22), anchor="mt")

    y0 = 140
    d.text((60, y0), "基礎 UI", fill=hex_to_rgb(INK), font=font(26, bold=True))
    base = [("木框 Wood", WOOD), ("奶油紙 Cream", CREAM), ("墨線 Ink", INK), ("印章紅 Stamp", STAMP)]
    bx, by, sw, sh, gap = 60, y0 + 45, 280, 90, 20
    for i, (name, hx) in enumerate(base):
        x = bx + i * (sw + gap)
        fill = hex_to_rgb(hx)
        d.rounded_rectangle([x, by, x + sw, by + sh], radius=8, fill=fill, outline=hex_to_rgb(INK), width=2)
        if hx.upper() == CREAM.upper():
            d.rounded_rectangle([x + 4, by + 4, x + sw - 4, by + sh - 4], radius=6, outline=hex_to_rgb(WOOD), width=1)
        lum = 0.299 * fill[0] + 0.587 * fill[1] + 0.114 * fill[2]
        tc = hex_to_rgb(INK) if lum > 140 else hex_to_rgb(CREAM)
        d.text((x + sw // 2, by + 28), name, fill=tc, font=label, anchor="mt")
        d.text((x + sw // 2, by + 58), hx.upper(), fill=tc, font=hexf, anchor="mt")

    seasons = [
        ("春 Spring", SPRING, SPRING_ACC), ("夏 Summer", SUMMER, SUMMER_ACC),
        ("秋 Autumn", AUTUMN, AUTUMN_ACC), ("冬 Winter", WINTER, WINTER_ACC),
    ]
    zh_keys = {
        "sky": "天", "grass": "草", "blossom": "花", "soil": "土", "sun": "陽",
        "leaf": "葉", "snow": "雪", "pine": "松", "wood": "木",
        "leaf_deep": "深葉", "cloud": "雲", "warm_edge": "暖邊", "amber": "琥珀", "frost": "霜",
    }
    sy = by + sh + 40
    for si, (sname, cols, acc) in enumerate(seasons):
        y = sy + si * 175
        d.text((60, y), sname, fill=hex_to_rgb(INK), font=font(26, bold=True))
        items = list(cols.items()) + list(acc.items())
        cell_w = min(200, (W - 120) // len(items) - 10)
        for j, (k, hx) in enumerate(items):
            x = 60 + j * (cell_w + 12)
            fill = hex_to_rgb(hx)
            d.rounded_rectangle([x, y + 40, x + cell_w, y + 40 + 100], radius=6,
                                fill=fill, outline=hex_to_rgb(INK), width=2)
            lum = 0.299 * fill[0] + 0.587 * fill[1] + 0.114 * fill[2]
            tc = hex_to_rgb(INK) if lum > 145 else hex_to_rgb(CREAM)
            mark = "★" if k in acc else ""
            d.text((x + cell_w // 2, y + 55), f"{zh_keys.get(k, k)} {k}{mark}", fill=tc, font=small, anchor="mt")
            d.text((x + cell_w // 2, y + 95), hx.upper(), fill=tc, font=hexf, anchor="mt")

    d.text((W // 2, H - 36), "★ = 季節輔助色（可選）· Hex 必須精確使用",
           fill=hex_to_rgb(WOOD), font=small, anchor="mt")
    path = os.path.join(OUT, "a0_palette.png")
    img.save(path, "PNG", optimize=True)
    print("Wrote", path)
    return path


def make_sample_sheet():
    W, H = 1080, 1920
    img = Image.new("RGBA", (W, H), hex_to_rgb(CREAM) + (255,))
    d = ImageDraw.Draw(img)

    sky = hex_to_rgb(SPRING["sky"])
    for yi in range(0, 720):
        t = yi / 720
        c = tuple(int(sky[i] + (245 - sky[i]) * t * 0.25) for i in range(3))
        d.line([(0, yi), (W, yi)], fill=c + (255,))

    img = soft_ellipse(img, (80, 80, 320, 180), (232, 244, 248, 180), blur=3)
    img = soft_ellipse(img, (700, 50, 1000, 160), (232, 244, 248, 160), blur=4)
    img = soft_ellipse(img, (400, 120, 580, 200), (255, 255, 255, 140), blur=2)
    d = ImageDraw.Draw(img)

    hill = hex_to_rgb(SPRING_ACC["leaf_deep"])
    d.ellipse([-100, 480, 500, 780], fill=hill + (255,))
    d.ellipse([350, 500, 950, 800], fill=hex_to_rgb("#5A9E4B") + (255,))
    d.ellipse([750, 490, 1200, 780], fill=hill + (255,))

    grass = hex_to_rgb(SPRING["grass"])
    d.rectangle([0, 680, W, 1180], fill=grass + (255,))
    for gx, gy, gw in [(40, 720, 120), (200, 760, 90), (500, 710, 140), (800, 740, 100),
                       (100, 900, 80), (600, 880, 110), (900, 920, 70)]:
        d.ellipse([gx, gy, gx + gw, gy + 40], fill=hex_to_rgb(SPRING_ACC["leaf_deep"]) + (90,))

    soil = hex_to_rgb(SPRING["soil"])
    path_pts = [(W // 2 - 40, 1180), (W // 2 - 60, 1000), (W // 2 + 20, 880),
                (W // 2 - 30, 780), (W // 2 + 10, 700)]
    for i in range(len(path_pts) - 1):
        d.line([path_pts[i], path_pts[i + 1]], fill=soil + (255,), width=48)

    blossom = hex_to_rgb(SPRING["blossom"])
    for tx, ty in [(160, 640), (920, 660), (280, 820)]:
        d.rectangle([tx - 8, ty, tx + 8, ty + 90], fill=hex_to_rgb(WOOD) + (255,),
                    outline=hex_to_rgb(INK), width=1)
        for ox, oy, r in [(-35, -20, 40), (25, -15, 38), (0, -45, 42), (-20, 10, 30), (30, 5, 28)]:
            d.ellipse([tx + ox - r, ty + oy - r, tx + ox + r, ty + oy + r],
                      fill=blossom + (255,), outline=hex_to_rgb(INK), width=1)

    for fx in range(30, W, 55):
        d.rectangle([fx, 1140, fx + 10, 1180], fill=hex_to_rgb(WOOD) + (255,),
                    outline=hex_to_rgb(INK), width=1)
    d.line([(20, 1155), (W - 20, 1155)], fill=hex_to_rgb(WOOD) + (255,), width=4)

    # Props with hotspot centers (cat, obj, cx, cy)
    props = [
        ("飲食", "餐桌灶", 205, 1005), ("交通", "小路單車", 545, 1055),
        ("住屋", "門廊", 780, 860), ("日用", "木箱", 122, 1095),
        ("娛樂", "花圃魚塘", 890, 1020), ("健康", "藥草", 368, 925),
        ("收入", "郵箱收成籃", 670, 955), ("儲蓄", "撲滿", 425, 1090),
        ("其他", "告示牌", 950, 900),
    ]

    draw_q_building(img, 780, 820, 140, 110, "#C45C2A", "#E8D4B0")
    draw_q_building(img, 150, 1000, 90, 70, WOOD, "#C4A35A")
    d = ImageDraw.Draw(img)

    # 飲食 — table + stove
    d.rectangle([160, 990, 250, 1020], fill=hex_to_rgb(WOOD), outline=hex_to_rgb(INK), width=2)
    d.rectangle([185, 1020, 195, 1050], fill=hex_to_rgb(WOOD), outline=hex_to_rgb(INK), width=1)
    d.rectangle([215, 1020, 225, 1050], fill=hex_to_rgb(WOOD), outline=hex_to_rgb(INK), width=1)
    d.ellipse([195, 960, 230, 995], fill=hex_to_rgb(STAMP), outline=hex_to_rgb(INK), width=2)

    # 交通 — bike
    d.ellipse([500, 1035, 535, 1070], outline=hex_to_rgb(INK), width=3)
    d.ellipse([555, 1035, 590, 1070], outline=hex_to_rgb(INK), width=3)
    d.line([(517, 1052), (572, 1052), (560, 1020), (530, 1040)], fill=hex_to_rgb(INK), width=2)

    # 日用 — crates
    d.rectangle([90, 1085, 145, 1130], fill=hex_to_rgb("#C4A35A"), outline=hex_to_rgb(INK), width=2)
    d.line([(90, 1107), (145, 1107)], fill=hex_to_rgb(INK), width=1)
    d.rectangle([100, 1065, 155, 1095], fill=hex_to_rgb("#A67C52"), outline=hex_to_rgb(INK), width=2)

    # 娛樂 — pond + flowers
    d.ellipse([840, 1000, 940, 1060], fill=hex_to_rgb("#6EB5D9"), outline=hex_to_rgb(INK), width=2)
    for fx, fy in [(860, 980), (890, 975), (920, 985)]:
        d.ellipse([fx, fy, fx + 18, fy + 18], fill=blossom, outline=hex_to_rgb(INK), width=1)

    # 健康 — herbs
    for hx, hy in [(340, 910), (360, 905), (380, 915), (350, 930)]:
        d.ellipse([hx, hy, hx + 22, hy + 28], fill=hex_to_rgb(SPRING_ACC["leaf_deep"]),
                  outline=hex_to_rgb(INK), width=1)
    d.rectangle([335, 940, 400, 955], fill=soil, outline=hex_to_rgb(INK), width=1)

    # 收入 — mailbox + basket
    d.rectangle([630, 920, 665, 960], fill=hex_to_rgb(STAMP), outline=hex_to_rgb(INK), width=2)
    d.rectangle([625, 915, 670, 925], fill=hex_to_rgb(INK))
    d.line([(647, 960), (647, 990)], fill=hex_to_rgb(WOOD), width=3)
    d.ellipse([670, 955, 720, 995], fill=hex_to_rgb("#C4A35A"), outline=hex_to_rgb(INK), width=2)
    d.ellipse([680, 945, 700, 965], fill=hex_to_rgb("#F5C542"), outline=hex_to_rgb(INK), width=1)
    d.ellipse([698, 948, 715, 968], fill=blossom, outline=hex_to_rgb(INK), width=1)

    # 儲蓄 — piggy
    d.ellipse([395, 1065, 455, 1115], fill=blossom, outline=hex_to_rgb(INK), width=2)
    d.ellipse([440, 1055, 465, 1080], fill=blossom, outline=hex_to_rgb(INK), width=1)
    d.rectangle([415, 1060, 435, 1068], fill=hex_to_rgb(INK))

    # 其他 — signboard
    d.rectangle([945, 900, 955, 970], fill=hex_to_rgb(WOOD), outline=hex_to_rgb(INK), width=1)
    d.rectangle([915, 860, 985, 910], fill=hex_to_rgb("#E8D4B0"), outline=hex_to_rgb(INK), width=2)
    d.text((950, 885), "?", fill=hex_to_rgb(INK), font=font(20, bold=True), anchor="mm")

    # Clickable rings + floating labels on vignette (subset for clarity)
    for cat, obj, cx, cy in props:
        d.ellipse([cx - 30, cy - 30, cx + 30, cy + 30], outline=hex_to_rgb(STAMP), width=2)
        # tiny cat chip above hotspot
        tw = font(12, bold=True)
        bbox = d.textbbox((0, 0), cat, font=tw)
        twid = bbox[2] - bbox[0]
        d.rounded_rectangle([cx - twid // 2 - 6, cy - 52, cx + twid // 2 + 6, cy - 34],
                            radius=4, fill=hex_to_rgb(CREAM), outline=hex_to_rgb(STAMP), width=1)
        d.text((cx, cy - 43), cat, fill=hex_to_rgb(INK), font=tw, anchor="mm")

    d.rounded_rectangle([W // 2 - 220, 24, W // 2 + 220, 78], radius=8,
                        fill=hex_to_rgb(CREAM), outline=hex_to_rgb(WOOD), width=3)
    d.text((W // 2, 51), "牧場手帳 · 春季牧場", fill=hex_to_rgb(INK),
           font=font(28, bold=True), anchor="mm")

    # Ledger panel
    panel_top = 1200
    d.rounded_rectangle([40, panel_top, W - 40, 1655], radius=16,
                        fill=hex_to_rgb(WOOD), outline=hex_to_rgb(INK), width=3)
    d.rounded_rectangle([58, panel_top + 18, W - 58, 1637], radius=10,
                        fill=hex_to_rgb(CREAM), outline=hex_to_rgb(INK), width=2)
    for ly in range(panel_top + 80, 1550, 36):
        d.line([(90, ly), (W - 90, ly)], fill=hex_to_rgb(WOOD) + (70,), width=1)

    d.text((W // 2, panel_top + 50), "帳簿樣本 · 今日一筆", fill=hex_to_rgb(INK),
           font=font(26, bold=True), anchor="mm")

    # Column headers: HKD book amounts + separate 種子幣 (not interchangeable)
    hdr_y = panel_top + 88
    d.text((100, hdr_y), "分類／物件", fill=hex_to_rgb(WOOD), font=font(14, bold=True))
    d.text((430, hdr_y), "備註", fill=hex_to_rgb(WOOD), font=font(14, bold=True))
    d.text((700, hdr_y), "港幣 HKD", fill=hex_to_rgb(WOOD), font=font(14, bold=True), anchor="rm")
    d.text((W - 100, hdr_y), "種子幣", fill=hex_to_rgb(WOOD), font=font(14, bold=True), anchor="rm")

    # Real ledger = HKD; farm reward = 種子幣 (separate column, no bare「幣」as HKD)
    rows = [
        ("飲食", "餐桌灶", "−HK$48", "+2", "早餐食材"),
        ("收入", "郵箱收成籃", "+HK$320", "+12", "週末市集"),
        ("日用", "木箱", "−HK$26", "+1", "肥皂毛巾"),
    ]
    ry = panel_top + 118
    for cat, obj, hkd, seed, note in rows:
        d.text((100, ry), f"【{cat}】{obj}", fill=hex_to_rgb(INK), font=font(18))
        d.text((430, ry), note, fill=hex_to_rgb(WOOD), font=font(16))
        hc = hex_to_rgb("#3F7A38") if hkd.startswith("+") else hex_to_rgb(STAMP)
        d.text((700, ry), hkd, fill=hc, font=font(18, bold=True), anchor="rm")
        d.text((W - 100, ry), seed, fill=hex_to_rgb("#6B4A2E"), font=font(18, bold=True), anchor="rm")
        ry += 42

    stamp_box = [W - 280, panel_top + 270, W - 100, panel_top + 390]
    d.rounded_rectangle(stamp_box, radius=6, outline=hex_to_rgb(STAMP), width=5)
    d.rounded_rectangle([stamp_box[0] + 8, stamp_box[1] + 8, stamp_box[2] - 8, stamp_box[3] - 8],
                        radius=4, outline=hex_to_rgb(STAMP), width=2)
    d.text(((stamp_box[0] + stamp_box[2]) // 2, (stamp_box[1] + stamp_box[3]) // 2 - 4),
           "有記", fill=hex_to_rgb(STAMP), font=font(40, bold=True), anchor="mm")
    d.text(((stamp_box[0] + stamp_box[2]) // 2, stamp_box[3] - 22),
           "STAMP", fill=hex_to_rgb(STAMP), font=font(12), anchor="mm")

    d.text((100, 1600), "※ 帳簿金額＝港幣 HKD｜種子幣＝獨立獎勵欄（≠ HKD，無線性換算）",
           fill=hex_to_rgb(WOOD), font=font(14))

    # Hotspot stamp row — all 9 locked mappings with Chinese labels
    d.text((W // 2, 1685), "分類熱點（已鎖定）Category = Ranch Object",
           fill=hex_to_rgb(INK), font=font(17, bold=True), anchor="mm")

    n = len(LOCKED_MAP)
    gap = (W - 40) / n
    for i, (cat, obj) in enumerate(LOCKED_MAP):
        cx = int(20 + gap * (i + 0.5))
        cy = 1745
        draw_hotspot_icon(d, cx, cy, 48, cat)
        d.text((cx, cy + 34), cat, fill=hex_to_rgb(INK), font=font(13, bold=True), anchor="mt")
        d.text((cx, cy + 50), obj, fill=hex_to_rgb(WOOD), font=font(11), anchor="mt")

    # Bottom nav
    nav_y = 1840
    d.rectangle([0, nav_y, W, H], fill=hex_to_rgb(WOOD))
    d.rectangle([0, nav_y, W, nav_y + 3], fill=hex_to_rgb(INK))
    nw = W // 4
    for i, name in enumerate(NAV):
        cx = nw * i + nw // 2
        draw_nav_icon(d, cx, nav_y + 38, name)
        d.text((cx, nav_y + 72), name, fill=hex_to_rgb(CREAM), font=font(16, bold=True), anchor="mt")

    out = img.convert("RGB")
    path = os.path.join(OUT, "a0_sample_sheet.png")
    out.save(path, "PNG", optimize=True)
    print("Wrote", path, out.size)
    return path


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    make_palette()
    make_sample_sheet()
    print("Done")
