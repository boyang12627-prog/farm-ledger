#!/usr/bin/env python3
"""Generate A1 spring ranch base, hotspots, nav icons, and preview for 牧場手帳."""
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os

OUT = "/workspace/farm-ledger/docs/art/a1"
FONT_REG = "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"
FONT_BOLD = "/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc"
TC = 3

WOOD, CREAM, INK, STAMP = "#6B4A2E", "#F5E6C8", "#3D2A1A", "#C45C4A"
SKY, GRASS, BLOSSOM, SOIL = "#A8D4E6", "#7CB86A", "#F2A7B8", "#A67C52"
LEAF_DEEP, CLOUD = "#4E8A45", "#E8F4F8"

# Base map size (drawable-friendly)
BASE_W, BASE_H = 1080, 1200

# Hotspot placements on base map (cx, cy) — locked category → ranch object
HOTSPOTS = [
    ("hotspot_food_table_stove", "飲食", "餐桌灶", 220, 780),
    ("hotspot_transit_path_bike", "交通", "小路單車", 540, 860),
    ("hotspot_home_porch", "住屋", "門廊", 780, 620),
    ("hotspot_daily_crate", "日用", "木箱", 130, 920),
    ("hotspot_fun_garden_pond", "娛樂", "花圃魚塘", 900, 820),
    ("hotspot_health_herbs", "健康", "藥草", 380, 700),
    ("hotspot_income_mail_basket", "收入", "郵箱收成籃", 650, 740),
    ("hotspot_save_piggy", "儲蓄", "撲滿", 430, 900),
    ("hotspot_other_sign", "其他", "告示牌", 960, 680),
]

NAV = [
    ("nav_ranch", "牧場"),
    ("nav_entry", "入帳"),
    ("nav_ledger", "帳簿"),
    ("nav_diary", "日記"),
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


def draw_q_building(img, cx, cy, w, h, roof, wall):
    d = ImageDraw.Draw(img)
    x0, y0 = cx - w // 2, cy - h // 2
    d.rectangle(
        [x0, y0 + h // 3, x0 + w, y0 + h],
        fill=hex_to_rgb(wall),
        outline=hex_to_rgb(INK),
        width=2,
    )
    d.polygon(
        [(cx, y0), (x0 - 4, y0 + h // 3 + 4), (x0 + w + 4, y0 + h // 3 + 4)],
        fill=hex_to_rgb(roof),
        outline=hex_to_rgb(INK),
    )
    dw, dh = w // 4, h // 3
    d.rectangle(
        [cx - dw // 2, y0 + h - dh, cx + dw // 2, y0 + h],
        fill=hex_to_rgb(WOOD),
        outline=hex_to_rgb(INK),
        width=1,
    )
    ww = w // 5
    d.rectangle(
        [cx - w // 3, y0 + h // 2, cx - w // 3 + ww, y0 + h // 2 + ww],
        fill=hex_to_rgb(SKY),
        outline=hex_to_rgb(INK),
        width=1,
    )


# --- Hotspot silhouette drawers (transparent canvas) ---

def silhouette_size():
    return 160  # transparent PNG canvas


def draw_sil_food(d, cx, cy, s):
    """餐桌灶"""
    ink, wood, stamp = map(hex_to_rgb, (INK, WOOD, STAMP))
    d.rectangle([cx - s, cy - s * 0.15, cx + s, cy + s * 0.45], fill=wood, outline=ink, width=2)
    d.rectangle([cx - s * 0.7, cy + s * 0.45, cx - s * 0.5, cy + s], fill=wood, outline=ink, width=1)
    d.rectangle([cx + s * 0.5, cy + s * 0.45, cx + s * 0.7, cy + s], fill=wood, outline=ink, width=1)
    d.ellipse([cx - s * 0.35, cy - s * 0.85, cx + s * 0.35, cy - s * 0.15], fill=stamp, outline=ink, width=2)
    d.ellipse([cx - s * 0.15, cy - s * 1.05, cx + s * 0.15, cy - s * 0.75], fill=hex_to_rgb("#E8D4B0"), outline=ink, width=1)


def draw_sil_transit(d, cx, cy, s):
    """小路單車"""
    ink, soil = map(hex_to_rgb, (INK, SOIL))
    d.ellipse([cx - s, cy - s * 0.2, cx - s * 0.15, cy + s * 0.65], outline=ink, width=3)
    d.ellipse([cx + s * 0.15, cy - s * 0.2, cx + s, cy + s * 0.65], outline=ink, width=3)
    d.line([(cx - s * 0.55, cy + s * 0.2), (cx + s * 0.55, cy - s * 0.25)], fill=ink, width=3)
    d.line([(cx + s * 0.55, cy - s * 0.25), (cx + s * 0.55, cy + s * 0.2)], fill=ink, width=3)
    d.line([(cx - s * 0.55, cy + s * 0.2), (cx + s * 0.2, cy + s * 0.2)], fill=ink, width=2)
    d.line([(cx - s * 1.15, cy + s * 0.85), (cx + s * 1.15, cy + s * 0.85)], fill=soil, width=5)


def draw_sil_home(d, cx, cy, s):
    """門廊"""
    ink, wood = map(hex_to_rgb, (INK, WOOD))
    wall = hex_to_rgb("#E8D4B0")
    roof = hex_to_rgb("#C45C2A")
    d.polygon([(cx, cy - s), (cx - s, cy - s * 0.2), (cx + s, cy - s * 0.2)], fill=roof, outline=ink)
    d.rectangle([cx - s * 0.75, cy - s * 0.2, cx + s * 0.75, cy + s * 0.75], fill=wall, outline=ink, width=2)
    # porch posts + floor
    d.rectangle([cx - s * 0.95, cy + s * 0.35, cx - s * 0.8, cy + s * 0.75], fill=wood, outline=ink, width=1)
    d.rectangle([cx + s * 0.8, cy + s * 0.35, cx + s * 0.95, cy + s * 0.75], fill=wood, outline=ink, width=1)
    d.rectangle([cx - s * 1.0, cy + s * 0.7, cx + s * 1.0, cy + s * 0.9], fill=wood, outline=ink, width=1)
    d.rectangle([cx - s * 0.25, cy + s * 0.1, cx + s * 0.25, cy + s * 0.75], fill=wood, outline=ink, width=1)


def draw_sil_daily(d, cx, cy, s):
    """木箱"""
    ink = hex_to_rgb(INK)
    c1, c2 = hex_to_rgb("#C4A35A"), hex_to_rgb(SOIL)
    d.rectangle([cx - s, cy - s * 0.2, cx + s, cy + s * 0.85], fill=c1, outline=ink, width=2)
    d.line([(cx - s, cy + s * 0.3), (cx + s, cy + s * 0.3)], fill=ink, width=2)
    d.line([(cx, cy - s * 0.2), (cx, cy + s * 0.85)], fill=ink, width=2)
    d.rectangle([cx - s * 0.85, cy - s * 0.75, cx + s * 0.85, cy - s * 0.15], fill=c2, outline=ink, width=2)
    d.line([(cx - s * 0.85, cy - s * 0.45), (cx + s * 0.85, cy - s * 0.45)], fill=ink, width=1)


def draw_sil_fun(d, cx, cy, s):
    """花圃魚塘"""
    ink, blossom = map(hex_to_rgb, (INK, BLOSSOM))
    pond = hex_to_rgb("#6EB5D9")
    d.ellipse([cx - s, cy - s * 0.1, cx + s * 0.35, cy + s * 0.9], fill=pond, outline=ink, width=2)
    d.ellipse([cx - s * 0.55, cy + s * 0.15, cx - s * 0.15, cy + s * 0.45], fill=(255, 255, 255, 120), outline=None)
    for ox, oy in [(0.3, -0.7), (0.7, -0.55), (0.55, -0.95), (0.9, -0.3)]:
        d.ellipse(
            [cx + s * ox - s * 0.28, cy + s * oy - s * 0.28, cx + s * ox + s * 0.28, cy + s * oy + s * 0.28],
            fill=blossom,
            outline=ink,
            width=1,
        )
    d.ellipse([cx + s * 0.55 - 8, cy - s * 0.55 - 8, cx + s * 0.55 + 8, cy - s * 0.55 + 8], fill=hex_to_rgb("#F5C542"), outline=ink, width=1)


def draw_sil_health(d, cx, cy, s):
    """藥草"""
    ink = hex_to_rgb(INK)
    g1, g2 = hex_to_rgb(GRASS), hex_to_rgb(LEAF_DEEP)
    d.rectangle([cx - s * 0.9, cy + s * 0.55, cx + s * 0.9, cy + s * 0.85], fill=hex_to_rgb(SOIL), outline=ink, width=1)
    d.line([(cx, cy + s * 0.55), (cx, cy - s * 0.7)], fill=g2, width=3)
    for ox, oy, col in [(-0.55, -0.2, g1), (0.5, -0.15, g2), (-0.25, -0.55, g2), (0.3, -0.6, g1), (0.0, 0.1, g1)]:
        d.ellipse(
            [cx + s * ox - s * 0.4, cy + s * oy - s * 0.35, cx + s * ox + s * 0.4, cy + s * oy + s * 0.35],
            fill=col,
            outline=ink,
            width=1,
        )


def draw_sil_income(d, cx, cy, s):
    """郵箱收成籃"""
    ink, wood, stamp = map(hex_to_rgb, (INK, WOOD, STAMP))
    d.rectangle([cx - s * 0.95, cy - s * 0.35, cx - s * 0.15, cy + s * 0.45], fill=stamp, outline=ink, width=2)
    d.rectangle([cx - s, cy - s * 0.45, cx - s * 0.1, cy - s * 0.25], fill=ink)
    d.line([(cx - s * 0.55, cy + s * 0.45), (cx - s * 0.55, cy + s * 0.95)], fill=wood, width=4)
    d.ellipse([cx - s * 0.05, cy + s * 0.05, cx + s * 0.95, cy + s * 0.85], fill=hex_to_rgb("#C4A35A"), outline=ink, width=2)
    d.ellipse([cx + s * 0.15, cy - s * 0.25, cx + s * 0.5, cy + s * 0.1], fill=hex_to_rgb("#F5C542"), outline=ink, width=1)
    d.ellipse([cx + s * 0.4, cy - s * 0.15, cx + s * 0.75, cy + s * 0.2], fill=hex_to_rgb(BLOSSOM), outline=ink, width=1)


def draw_sil_save(d, cx, cy, s):
    """撲滿"""
    ink, blossom = map(hex_to_rgb, (INK, BLOSSOM))
    d.ellipse([cx - s, cy - s * 0.35, cx + s, cy + s * 0.85], fill=blossom, outline=ink, width=3)
    d.ellipse([cx + s * 0.45, cy - s * 0.7, cx + s * 0.95, cy - s * 0.15], fill=blossom, outline=ink, width=2)
    d.rectangle([cx - s * 0.35, cy - s * 0.45, cx + s * 0.35, cy - s * 0.25], fill=ink)
    d.ellipse([cx - s * 0.5, cy - s * 0.05, cx - s * 0.15, cy + s * 0.3], fill=ink)
    d.ellipse([cx + s * 0.55, cy + s * 0.55, cx + s * 0.85, cy + s * 0.85], fill=ink)  # leg hint


def draw_sil_other(d, cx, cy, s):
    """告示牌"""
    ink, wood = map(hex_to_rgb, (INK, WOOD))
    board = hex_to_rgb("#E8D4B0")
    d.rectangle([cx - s * 0.12, cy - s * 0.1, cx + s * 0.12, cy + s], fill=wood, outline=ink, width=2)
    d.rectangle([cx - s, cy - s * 0.95, cx + s, cy - s * 0.05], fill=board, outline=ink, width=3)
    d.line([(cx - s * 0.65, cy - s * 0.65), (cx + s * 0.65, cy - s * 0.65)], fill=ink, width=2)
    d.line([(cx - s * 0.55, cy - s * 0.4), (cx + s * 0.55, cy - s * 0.4)], fill=ink, width=2)
    d.line([(cx - s * 0.4, cy - s * 0.15), (cx + s * 0.25, cy - s * 0.15)], fill=ink, width=2)


SIL_DRAW = {
    "hotspot_food_table_stove": draw_sil_food,
    "hotspot_transit_path_bike": draw_sil_transit,
    "hotspot_home_porch": draw_sil_home,
    "hotspot_daily_crate": draw_sil_daily,
    "hotspot_fun_garden_pond": draw_sil_fun,
    "hotspot_health_herbs": draw_sil_health,
    "hotspot_income_mail_basket": draw_sil_income,
    "hotspot_save_piggy": draw_sil_save,
    "hotspot_other_sign": draw_sil_other,
}


def make_hotspot_png(file_id):
    S = silhouette_size()
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    SIL_DRAW[file_id](d, S // 2, S // 2, S * 0.32)
    path = os.path.join(OUT, f"{file_id}.png")
    img.save(path, "PNG")
    return path, img


def make_spring_base():
    W, H = BASE_W, BASE_H
    img = Image.new("RGBA", (W, H), rgba(SKY))
    d = ImageDraw.Draw(img)

    # soft sky gradient
    sky = hex_to_rgb(SKY)
    for yi in range(0, 420):
        t = yi / 420
        c = tuple(int(sky[i] + (245 - sky[i]) * t * 0.2) for i in range(3))
        d.line([(0, yi), (W, yi)], fill=c + (255,))

    img = soft_ellipse(img, (60, 40, 300, 150), rgba(CLOUD, 200), blur=4)
    img = soft_ellipse(img, (700, 30, 1020, 140), rgba(CLOUD, 180), blur=5)
    img = soft_ellipse(img, (380, 90, 560, 180), (255, 255, 255, 150), blur=3)
    d = ImageDraw.Draw(img)

    # distant hills
    d.ellipse([-80, 300, 480, 560], fill=rgba(LEAF_DEEP))
    d.ellipse([320, 320, 900, 580], fill=rgba("#5A9E4B"))
    d.ellipse([720, 310, 1180, 560], fill=rgba(LEAF_DEEP))

    # grass field
    d.rectangle([0, 480, W, H], fill=rgba(GRASS))
    for gx, gy, gw in [
        (40, 520, 140), (220, 560, 100), (500, 510, 160), (780, 540, 120),
        (100, 700, 90), (600, 680, 130), (880, 720, 80), (350, 850, 110),
    ]:
        d.ellipse([gx, gy, gx + gw, gy + 45], fill=rgba(LEAF_DEEP, 90))

    # soil path
    soil = hex_to_rgb(SOIL)
    path_pts = [
        (W // 2 - 30, H - 20),
        (W // 2 - 50, 950),
        (W // 2 + 30, 820),
        (W // 2 - 20, 700),
        (W // 2 + 20, 580),
    ]
    for i in range(len(path_pts) - 1):
        d.line([path_pts[i], path_pts[i + 1]], fill=soil + (255,), width=52)

    # blossom trees
    blossom = hex_to_rgb(BLOSSOM)
    for tx, ty in [(150, 460), (940, 480), (260, 640)]:
        d.rectangle([tx - 8, ty, tx + 8, ty + 100], fill=rgba(WOOD), outline=hex_to_rgb(INK), width=1)
        for ox, oy, r in [(-40, -25, 44), (28, -18, 40), (0, -50, 46), (-22, 12, 32), (32, 8, 30)]:
            d.ellipse(
                [tx + ox - r, ty + oy - r, tx + ox + r, ty + oy + r],
                fill=blossom + (255,),
                outline=hex_to_rgb(INK),
                width=1,
            )

    # fence along bottom-ish
    for fx in range(20, W, 50):
        d.rectangle([fx, H - 70, fx + 10, H - 20], fill=rgba(WOOD), outline=hex_to_rgb(INK), width=1)
    d.line([(10, H - 50), (W - 10, H - 50)], fill=rgba(WOOD), width=4)

    # cozy empty ranch buildings (no trademark characters)
    draw_q_building(img, 780, 560, 160, 130, "#C45C2A", "#E8D4B0")
    draw_q_building(img, 160, 760, 100, 80, WOOD, "#C4A35A")

    # subtle title chip
    d = ImageDraw.Draw(img)
    d.rounded_rectangle(
        [W // 2 - 200, 18, W // 2 + 200, 68],
        radius=8,
        fill=hex_to_rgb(CREAM),
        outline=hex_to_rgb(WOOD),
        width=3,
    )
    d.text(
        (W // 2, 43),
        "春季牧場 · Spring Ranch",
        fill=hex_to_rgb(INK),
        font=font(24, bold=True),
        anchor="mm",
    )

    path = os.path.join(OUT, "spring_ranch_base.png")
    img.convert("RGB").save(path, "PNG", optimize=True)
    print("Wrote", path, (W, H))
    return path, img


def place_props_on_base(img):
    """Draw the 9 hotspot props onto the base (for preview / combined overlay source)."""
    d = ImageDraw.Draw(img)
    blossom = hex_to_rgb(BLOSSOM)
    soil = hex_to_rgb(SOIL)

    # 飲食 — table + stove @ 220,780
    d.rectangle([175, 765, 265, 800], fill=hex_to_rgb(WOOD), outline=hex_to_rgb(INK), width=2)
    d.rectangle([195, 800, 205, 830], fill=hex_to_rgb(WOOD), outline=hex_to_rgb(INK), width=1)
    d.rectangle([235, 800, 245, 830], fill=hex_to_rgb(WOOD), outline=hex_to_rgb(INK), width=1)
    d.ellipse([205, 730, 245, 770], fill=hex_to_rgb(STAMP), outline=hex_to_rgb(INK), width=2)

    # 交通 — bike @ 540,860
    d.ellipse([495, 840, 535, 880], outline=hex_to_rgb(INK), width=3)
    d.ellipse([550, 840, 590, 880], outline=hex_to_rgb(INK), width=3)
    d.line([(515, 860), (570, 860), (560, 825), (525, 850)], fill=hex_to_rgb(INK), width=2)

    # 住屋 porch hint already on building; porch posts near 780,620
    d.rectangle([700, 640, 710, 680], fill=hex_to_rgb(WOOD), outline=hex_to_rgb(INK), width=1)
    d.rectangle([850, 640, 860, 680], fill=hex_to_rgb(WOOD), outline=hex_to_rgb(INK), width=1)
    d.rectangle([690, 670, 870, 690], fill=hex_to_rgb(WOOD), outline=hex_to_rgb(INK), width=1)

    # 日用 crates @ 130,920
    d.rectangle([95, 905, 155, 955], fill=hex_to_rgb("#C4A35A"), outline=hex_to_rgb(INK), width=2)
    d.line([(95, 930), (155, 930)], fill=hex_to_rgb(INK), width=1)
    d.rectangle([105, 880, 165, 915], fill=hex_to_rgb(SOIL), outline=hex_to_rgb(INK), width=2)

    # 娛樂 pond + flowers @ 900,820
    d.ellipse([850, 795, 950, 860], fill=hex_to_rgb("#6EB5D9"), outline=hex_to_rgb(INK), width=2)
    for fx, fy in [(870, 770), (900, 765), (930, 775)]:
        d.ellipse([fx, fy, fx + 20, fy + 20], fill=blossom, outline=hex_to_rgb(INK), width=1)

    # 健康 herbs @ 380,700
    for hx, hy in [(350, 680), (370, 675), (390, 685), (360, 700)]:
        d.ellipse([hx, hy, hx + 24, hy + 30], fill=hex_to_rgb(LEAF_DEEP), outline=hex_to_rgb(INK), width=1)
    d.rectangle([345, 715, 415, 735], fill=soil, outline=hex_to_rgb(INK), width=1)

    # 收入 mailbox + basket @ 650,740
    d.rectangle([610, 710, 650, 755], fill=hex_to_rgb(STAMP), outline=hex_to_rgb(INK), width=2)
    d.rectangle([605, 705, 655, 715], fill=hex_to_rgb(INK))
    d.line([(630, 755), (630, 790)], fill=hex_to_rgb(WOOD), width=3)
    d.ellipse([655, 745, 710, 790], fill=hex_to_rgb("#C4A35A"), outline=hex_to_rgb(INK), width=2)
    d.ellipse([665, 730, 688, 755], fill=hex_to_rgb("#F5C542"), outline=hex_to_rgb(INK), width=1)

    # 儲蓄 piggy @ 430,900
    d.ellipse([400, 875, 460, 930], fill=blossom, outline=hex_to_rgb(INK), width=2)
    d.ellipse([445, 860, 472, 888], fill=blossom, outline=hex_to_rgb(INK), width=1)
    d.rectangle([418, 868, 442, 878], fill=hex_to_rgb(INK))

    # 其他 sign @ 960,680
    d.rectangle([955, 680, 965, 760], fill=hex_to_rgb(WOOD), outline=hex_to_rgb(INK), width=1)
    d.rectangle([920, 635, 1000, 690], fill=hex_to_rgb("#E8D4B0"), outline=hex_to_rgb(INK), width=2)
    d.text((960, 662), "?", fill=hex_to_rgb(INK), font=font(22, bold=True), anchor="mm")

    return img


def make_hotspots_overlay(base_img):
    """Combined overlay: transparent layer with 9 silhouettes + stamp rings."""
    W, H = base_img.size
    overlay = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    for file_id, cat, obj, cx, cy in HOTSPOTS:
        sil = Image.new("RGBA", (silhouette_size(), silhouette_size()), (0, 0, 0, 0))
        SIL_DRAW[file_id](ImageDraw.Draw(sil), silhouette_size() // 2, silhouette_size() // 2, silhouette_size() * 0.28)
        # scale down a bit for map
        sil_s = sil.resize((96, 96), Image.Resampling.LANCZOS)
        overlay.alpha_composite(sil_s, (cx - 48, cy - 48))

    d = ImageDraw.Draw(overlay)
    for file_id, cat, obj, cx, cy in HOTSPOTS:
        d.ellipse([cx - 42, cy - 42, cx + 42, cy + 42], outline=hex_to_rgb(STAMP), width=3)
        tw = font(13, bold=True)
        bbox = d.textbbox((0, 0), cat, font=tw)
        twid = bbox[2] - bbox[0]
        d.rounded_rectangle(
            [cx - twid // 2 - 6, cy - 68, cx + twid // 2 + 6, cy - 48],
            radius=4,
            fill=hex_to_rgb(CREAM),
            outline=hex_to_rgb(STAMP),
            width=1,
        )
        d.text((cx, cy - 58), cat, fill=hex_to_rgb(INK), font=tw, anchor="mm")

    path = os.path.join(OUT, "hotspots_overlay.png")
    overlay.save(path, "PNG")
    print("Wrote", path)
    return path, overlay


def draw_nav_glyph(d, cx, cy, name, scale=1.0):
    ink, cream, wood = map(hex_to_rgb, (INK, CREAM, WOOD))
    stamp = hex_to_rgb(STAMP)
    r = int(22 * scale)
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=cream, outline=ink, width=max(1, int(2 * scale)))
    s = scale
    if name == "牧場":
        d.polygon(
            [(cx, cy - int(14 * s)), (cx - int(14 * s), cy - int(2 * s)), (cx + int(14 * s), cy - int(2 * s))],
            fill=hex_to_rgb("#C45C2A"),
            outline=ink,
        )
        d.rectangle(
            [cx - int(10 * s), cy - int(2 * s), cx + int(10 * s), cy + int(14 * s)],
            fill=hex_to_rgb("#E8D4B0"),
            outline=ink,
            width=1,
        )
        d.rectangle(
            [cx - int(3 * s), cy + int(4 * s), cx + int(3 * s), cy + int(14 * s)],
            fill=wood,
            outline=ink,
            width=1,
        )
    elif name == "入帳":
        d.ellipse([cx - int(12 * s), cy - int(12 * s), cx + int(12 * s), cy + int(12 * s)], outline=stamp, width=max(2, int(3 * s)))
        d.line([(cx - int(8 * s), cy), (cx + int(8 * s), cy)], fill=stamp, width=max(2, int(3 * s)))
        d.line([(cx, cy - int(8 * s)), (cx, cy + int(8 * s))], fill=stamp, width=max(2, int(3 * s)))
    elif name == "帳簿":
        d.polygon(
            [(cx, cy - int(12 * s)), (cx - int(14 * s), cy - int(8 * s)), (cx - int(14 * s), cy + int(12 * s)), (cx, cy + int(8 * s))],
            fill=cream,
            outline=ink,
        )
        d.polygon(
            [(cx, cy - int(12 * s)), (cx + int(14 * s), cy - int(8 * s)), (cx + int(14 * s), cy + int(12 * s)), (cx, cy + int(8 * s))],
            fill=hex_to_rgb("#E8D4B0"),
            outline=ink,
        )
        d.line([(cx, cy - int(12 * s)), (cx, cy + int(8 * s))], fill=ink, width=max(1, int(2 * s)))
    else:  # 日記
        d.line([(cx - int(10 * s), cy - int(8 * s)), (cx + int(10 * s), cy - int(8 * s))], fill=ink, width=max(1, int(2 * s)))
        d.line([(cx - int(10 * s), cy), (cx + int(6 * s), cy)], fill=ink, width=max(1, int(2 * s)))
        d.line([(cx - int(10 * s), cy + int(8 * s)), (cx + int(2 * s), cy + int(8 * s))], fill=ink, width=max(1, int(2 * s)))
        d.polygon(
            [(cx + int(8 * s), cy + int(2 * s)), (cx + int(14 * s), cy + int(14 * s)), (cx + int(4 * s), cy + int(10 * s))],
            fill=stamp,
            outline=ink,
        )


def make_nav_icons():
    paths = []
    for file_id, label in NAV:
        for size, suffix in [(48, ""), (96, "@2x")]:
            img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
            d = ImageDraw.Draw(img)
            scale = size / 48.0
            draw_nav_glyph(d, size // 2, size // 2, label, scale=scale * 0.95)
            name = f"{file_id}{suffix}.png" if suffix else f"{file_id}.png"
            # For @2x use explicit name nav_ranch@2x.png style
            if suffix:
                name = f"{file_id}@2x.png"
            path = os.path.join(OUT, name)
            img.save(path, "PNG")
            paths.append(path)
            print("Wrote", path, img.size)
    return paths


def make_preview(base_img, overlay):
    """a1_preview.png: spring base + placed props + hotspot rings + bottom nav row."""
    W = BASE_W
    # taller sheet: base + nav strip + caption
    nav_h = 140
    caption_h = 80
    H = BASE_H + nav_h + caption_h
    sheet = Image.new("RGBA", (W, H), rgba(CREAM))

    # with props painted
    scene = base_img.copy()
    place_props_on_base(scene)
    # composite overlay rings/sils
    scene = Image.alpha_composite(scene.convert("RGBA"), overlay)
    sheet.paste(scene.convert("RGB"), (0, 0))

    d = ImageDraw.Draw(sheet)
    # caption under base
    d.rectangle([0, BASE_H, W, BASE_H + caption_h], fill=hex_to_rgb(CREAM))
    d.text(
        (W // 2, BASE_H + 28),
        "A1 Preview · 春季牧場底圖＋九熱點（印章紅光環）＋底欄",
        fill=hex_to_rgb(INK),
        font=font(20, bold=True),
        anchor="mm",
    )
    d.text(
        (W // 2, BASE_H + 55),
        "帳簿金額＝港幣 HKD｜種子幣＝獨立獎勵欄（本預覽為牧場場景）",
        fill=hex_to_rgb(WOOD),
        font=font(14),
        anchor="mm",
    )

    # bottom nav bar
    nav_y = BASE_H + caption_h
    d.rectangle([0, nav_y, W, H], fill=hex_to_rgb(WOOD))
    d.rectangle([0, nav_y, W, nav_y + 3], fill=hex_to_rgb(INK))
    nw = W // 4
    for i, (file_id, label) in enumerate(NAV):
        cx = nw * i + nw // 2
        draw_nav_glyph(d, cx, nav_y + 48, label, scale=1.3)
        d.text((cx, nav_y + 95), label, fill=hex_to_rgb(CREAM), font=font(18, bold=True), anchor="mt")

    path = os.path.join(OUT, "a1_preview.png")
    sheet.convert("RGB").save(path, "PNG", optimize=True)
    print("Wrote", path, sheet.size)
    return path


def write_docs():
    assets_md = f"""# A1 Art Assets · 牧場手帳（春季牧場）

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
| `spring_ranch_base.png` | {BASE_W}×{BASE_H} | 春季牧場底圖（空曠舒適牧場，drawable-friendly） |
| `hotspot_food_table_stove.png` | 160×160 | 飲食／餐桌灶（透明底剪影） |
| `hotspot_transit_path_bike.png` | 160×160 | 交通／小路單車 |
| `hotspot_home_porch.png` | 160×160 | 住屋／門廊 |
| `hotspot_daily_crate.png` | 160×160 | 日用／木箱 |
| `hotspot_fun_garden_pond.png` | 160×160 | 娛樂／花圃魚塘 |
| `hotspot_health_herbs.png` | 160×160 | 健康／藥草 |
| `hotspot_income_mail_basket.png` | 160×160 | 收入／郵箱收成籃 |
| `hotspot_save_piggy.png` | 160×160 | 儲蓄／撲滿 |
| `hotspot_other_sign.png` | 160×160 | 其他／告示牌 |
| `hotspots_overlay.png` | {BASE_W}×{BASE_H} | 九熱點＋印章紅光環 combined overlay（透明底） |
| `nav_ranch.png` / `nav_ranch@2x.png` | 48×48 / 96×96 | 底欄・牧場 |
| `nav_entry.png` / `nav_entry@2x.png` | 48×48 / 96×96 | 底欄・入帳 |
| `nav_ledger.png` / `nav_ledger@2x.png` | 48×48 / 96×96 | 底欄・帳簿 |
| `nav_diary.png` / `nav_diary@2x.png` | 48×48 / 96×96 | 底欄・日記 |
| `a1_preview.png` | {BASE_W}×{BASE_H + 220} | 合成預覽：底圖＋熱點＋底欄 |
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

座標相對 `spring_ranch_base.png`（{BASE_W}×{BASE_H}）。

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
"""
    path = os.path.join(OUT, "A1_assets.md")
    with open(path, "w", encoding="utf-8") as f:
        f.write(assets_md)
    print("Wrote", path)

    readme = """# A1 Art · 牧場手帳

春季牧場底圖、九個可點熱點剪影、底欄四圖標，以及合成預覽。

| 重點檔 | 用途 |
|--------|------|
| `spring_ranch_base.png` | 春牧場底圖 1080×1200 |
| `hotspot_*.png` | 九分類熱點（透明底） |
| `hotspots_overlay.png` | 熱點＋印章紅光環 overlay |
| `nav_*.png` / `@2x` | 底欄 牧場｜入帳｜帳簿｜日記 |
| `a1_preview.png` | 單頁合成預覽 |
| `A1_assets.md` | 完整清單／映射／尺寸 |
| `gen_a1_assets.py` | Pillow 重生成 |

**貨幣**：帳簿＝港幣 HKD；種子幣＝獨立獎勵欄（≠ HKD）。

重生成：`/workspace/.venv/bin/python gen_a1_assets.py`
"""
    rpath = os.path.join(OUT, "README.md")
    with open(rpath, "w", encoding="utf-8") as f:
        f.write(readme)
    print("Wrote", rpath)


def main():
    os.makedirs(OUT, exist_ok=True)
    base_path, base_img = make_spring_base()
    for file_id, *_ in HOTSPOTS:
        make_hotspot_png(file_id)
    overlay_path, overlay = make_hotspots_overlay(base_img)
    make_nav_icons()
    make_preview(base_img, overlay)
    write_docs()
    print("A1 Done")


if __name__ == "__main__":
    main()
