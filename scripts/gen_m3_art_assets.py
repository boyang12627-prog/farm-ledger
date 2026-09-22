#!/usr/bin/env python3
"""M3 art: night settle ritual + pet feed.

Self-drawn 32px warm pixel, deep outlines (#6B4A2E / #3D2A18), nearest-neighbor.
Does not touch reward / settle formulas — assets + docs (+ optional UI icon wire).

  /tmp/pilvenv/bin/python scripts/gen_m3_art_assets.py
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "app/src/main/res/drawable-nodpi"
PREVIEW = ROOT / "scripts/preview_assets"

# Warm mud palette (FarmMud / roadmap)
CREAM = (0xFF, 0xF8, 0xE7, 255)
SAND = (0xF2, 0xE2, 0xC4, 255)
SOIL_L = (0xD9, 0xB4, 0x8C, 255)
SOIL_M = (0xC4, 0x96, 0x6A, 255)
SOIL_D = (0xA6, 0x7C, 0x52, 255)
OUTLINE = (0x6B, 0x4A, 0x2E, 255)
DECOR_OUTLINE = (0x3D, 0x2A, 0x18, 255)
STONE = (0x8A, 0x7A, 0x68, 255)
STONE_L = (0xC8, 0xBC, 0xA8, 255)
ORANGE = (0xE8, 0x8A, 0x3A, 255)
GOLD = (0xE8, 0xA8, 0x38, 255)
GOLD_L = (0xF4, 0xD0, 0x78, 255)
LGREEN = (0xB8, 0xD9, 0x7A, 255)
SAGE = (0x8F, 0xBF, 0x6A, 255)
DGREEN = (0x4F, 0x7A, 0x45, 255)
SKY = (0x7E, 0xB8, 0xC9, 255)
NIGHT = (0x3A, 0x3A, 0x6A, 255)
NIGHT_L = (0x5A, 0x5A, 0x8A, 255)
MOON = (0xF4, 0xF0, 0xD0, 255)
MOON_L = (0xFF, 0xFC, 0xEE, 255)
MOON_SHADE = (0xD8, 0xD0, 0xA8, 255)
INK = (0x5C, 0x53, 0x46, 255)
RED = (0xD4, 0x3A, 0x2F, 255)
RED_DK = (0xA8, 0x28, 0x22, 255)
BRICK = (0xC7, 0x5B, 0x39, 255)
PINK = (0xF2, 0xB8, 0xA8, 255)
PINK_D = (0xE0, 0x8A, 0x7A, 255)
PINK_DK = (0xC4, 0x6A, 0x5A, 255)
SNORT = (0xF0, 0xA0, 0x90, 255)
HEART = (0xE8, 0x5A, 0x6A, 255)
HEART_L = (0xF4, 0x9A, 0xA4, 255)
NEAR_BLACK = (0x2A, 0x1E, 0x14, 255)
WHITE = (0xFF, 0xFF, 0xFF, 255)
PAPER = (0xED, 0xE6, 0xD9, 255)
WAX = (0xC7, 0x5B, 0x39, 255)
WAX_L = (0xE8, 0x8A, 0x3A, 255)
SPARKLE = (0xFF, 0xF0, 0xA0, 255)
TRANSPARENT = (0, 0, 0, 0)


def new_img(w=32, h=32):
    return Image.new("RGBA", (w, h), TRANSPARENT)


def px(img, x, y, c):
    w, h = img.size
    if 0 <= x < w and 0 <= y < h:
        img.putpixel((x, y), c)


def fill_rect(img, x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px(img, x, y, c)


def hline(img, x0, x1, y, c):
    for x in range(x0, x1 + 1):
        px(img, x, y, c)


def vline(img, x, y0, y1, c):
    for y in range(y0, y1 + 1):
        px(img, x, y, c)


def rect_outline(img, x0, y0, x1, y1, c):
    hline(img, x0, x1, y0, c)
    hline(img, x0, x1, y1, c)
    vline(img, x0, y0, y1, c)
    vline(img, x1, y0, y1, c)


def deep_outline(img, x0, y0, x1, y1):
    rect_outline(img, x0, y0, x1, y1, OUTLINE)
    rect_outline(img, x0 - 1, y0 - 1, x1 + 1, y1 + 1, DECOR_OUTLINE)


def disk(img, cx, cy, r, c):
    for y in range(cy - r, cy + r + 1):
        for x in range(cx - r, cx + r + 1):
            if (x - cx) * (x - cx) + (y - cy) * (y - cy) <= r * r:
                px(img, x, y, c)


def ring_outline(img, cx, cy, r, c):
    for y in range(cy - r - 1, cy + r + 2):
        for x in range(cx - r - 1, cx + r + 2):
            d2 = (x - cx) * (x - cx) + (y - cy) * (y - cy)
            if r * r - r <= d2 <= r * r + r + 2 and d2 > (r - 1) * (r - 1):
                # approximate rim
                inner = (r - 1) * (r - 1)
                outer = (r + 1) * (r + 1)
                if inner < d2 <= outer:
                    px(img, x, y, c)


def moon_disk(img, cx, cy, r, with_crater=True):
    disk(img, cx, cy, r, MOON)
    disk(img, cx - 1, cy - 1, max(1, r - 2), MOON_L)
    if with_crater and r >= 5:
        disk(img, cx + 2, cy + 1, max(1, r // 4), MOON_SHADE)
        disk(img, cx - 2, cy + 2, max(1, r // 5), MOON_SHADE)
    # deep rim
    for y in range(cy - r - 1, cy + r + 2):
        for x in range(cx - r - 1, cx + r + 2):
            d2 = (x - cx) * (x - cx) + (y - cy) * (y - cy)
            if (r - 0.5) ** 2 < d2 <= (r + 1.2) ** 2:
                # only empty or near edge
                if 0 <= x < 32 and 0 <= y < 32:
                    cur = img.getpixel((x, y))
                    if cur[3] == 0 or d2 > r * r:
                        px(img, x, y, OUTLINE if d2 <= (r + 0.8) ** 2 else DECOR_OUTLINE)


# ─── Settle ritual ─────────────────────────────────────────────────────────

def ic_moon_settle():
    """Night settle CTA: crescent / full warm moon with soft glow tip."""
    img = new_img()
    # soft outer glow (sparse)
    for ox, oy in ((15, 15), (16, 14), (14, 16), (17, 16), (13, 14)):
        px(img, ox, oy, GOLD_L)
    moon_disk(img, 16, 16, 9, with_crater=True)
    # crescent bite (night blue cut) for ritual feel
    disk(img, 20, 14, 6, TRANSPARENT)
    # re-draw left bright crescent rim after bite: fill crescent manually
    # simpler: full moon with ledger tick
    # stamp a tiny check / sprout under moon
    fill_rect(img, 14, 24, 17, 27, SAGE)
    fill_rect(img, 15, 23, 16, 23, LGREEN)
    deep_outline(img, 14, 23, 17, 27)
    # star sparkles
    for sx, sy in ((5, 6), (26, 8), (7, 22), (25, 24)):
        px(img, sx, sy, GOLD_L)
        px(img, sx + 1, sy, GOLD)
        px(img, sx, sy - 1, GOLD)
    return img


def ic_moon_settle_clean():
    """Cleaner moon settle icon (full moon + stars) — preferred CTA."""
    img = new_img()
    # night vignette dots
    for sx, sy in ((4, 5), (27, 6), (6, 26), (25, 25), (28, 18), (3, 16)):
        px(img, sx, sy, GOLD_L)
        px(img, sx, sy - 1, WHITE)
    moon_disk(img, 15, 15, 10, with_crater=True)
    # warm highlight crescent strip
    for y in range(8, 18):
        for x in range(8, 13):
            d2 = (x - 15) ** 2 + (y - 15) ** 2
            if d2 <= 81:
                px(img, x, y, MOON_L)
    # tiny growth gem under moon (settle = +GP)
    fill_rect(img, 13, 26, 18, 29, GOLD)
    fill_rect(img, 14, 25, 17, 25, GOLD_L)
    deep_outline(img, 13, 25, 18, 29)
    px(img, 15, 27, CREAM)
    px(img, 16, 27, CREAM)
    return img


def ic_settle_stamp():
    """Wax seal stamp — '今日結清' ritual mark."""
    img = new_img()
    # wax blob
    disk(img, 16, 17, 11, WAX)
    disk(img, 16, 17, 9, WAX_L)
    disk(img, 14, 14, 3, GOLD_L)
    # outer deep rim
    for y in range(4, 30):
        for x in range(4, 30):
            d2 = (x - 16) ** 2 + (y - 17) ** 2
            if 110 < d2 <= 144:
                px(img, x, y, OUTLINE)
            elif 144 < d2 <= 170:
                px(img, x, y, DECOR_OUTLINE)
    # ledger check mark in wax
    for x, y in [
        (10, 17), (11, 18), (12, 19), (13, 20), (14, 21),
        (15, 20), (16, 19), (17, 18), (18, 17), (19, 16), (20, 15), (21, 14),
    ]:
        px(img, x, y, CREAM)
        px(img, x, y - 1, PAPER)
    # ribbon tails
    fill_rect(img, 8, 26, 12, 29, RED)
    fill_rect(img, 19, 26, 23, 29, RED)
    deep_outline(img, 8, 26, 12, 29)
    deep_outline(img, 19, 26, 23, 29)
    px(img, 10, 28, RED_DK)
    px(img, 21, 28, RED_DK)
    return img


def settle_banner():
    """Wide-feel banner compressed to 32px: moon + '結' plate."""
    img = new_img()
    # night strip
    fill_rect(img, 1, 8, 30, 24, NIGHT)
    fill_rect(img, 2, 9, 29, 23, NIGHT_L)
    deep_outline(img, 1, 8, 30, 24)
    # hanging ropes
    vline(img, 4, 4, 8, SOIL_D)
    vline(img, 27, 4, 8, SOIL_D)
    fill_rect(img, 3, 3, 5, 4, GOLD)
    fill_rect(img, 26, 3, 28, 4, GOLD)
    # moon left
    moon_disk(img, 9, 16, 5, with_crater=False)
    # plate / stamp right
    fill_rect(img, 16, 11, 27, 21, WAX)
    fill_rect(img, 17, 12, 26, 20, WAX_L)
    deep_outline(img, 16, 11, 27, 21)
    # check
    for x, y in [(18, 16), (19, 17), (20, 18), (21, 17), (22, 16), (23, 15), (24, 14)]:
        px(img, x, y, CREAM)
    # stars
    px(img, 14, 11, GOLD_L)
    px(img, 13, 20, GOLD)
    return img


def moon_rise_frame(frame: int):
    """2–3 frame moon rise (0=low, 1=mid, 2=high)."""
    img = new_img()
    # night sky wash (sparse)
    fill_rect(img, 0, 0, 31, 22, (0x2A, 0x2A, 0x4A, 180))
    for sx, sy in ((3, 4), (28, 3), (6, 10), (24, 8), (12, 2)):
        px(img, sx, sy, GOLD_L)
    # ground silhouette
    fill_rect(img, 0, 24, 31, 31, SOIL_D)
    fill_rect(img, 0, 24, 31, 25, OUTLINE)
    # hut stub
    fill_rect(img, 2, 18, 10, 24, SAND)
    fill_rect(img, 3, 14, 9, 18, BRICK)
    deep_outline(img, 2, 18, 10, 24)
    deep_outline(img, 3, 14, 9, 18)
    # moon positions
    cy = {0: 22, 1: 14, 2: 8}[frame]
    r = {0: 5, 1: 7, 2: 8}[frame]
    moon_disk(img, 20, cy, r, with_crater=True)
    if frame >= 1:
        px(img, 12, 6, WHITE)
        px(img, 26, 12, GOLD_L)
    if frame == 2:
        # settle glow
        fill_rect(img, 17, 26, 22, 29, GOLD)
        deep_outline(img, 17, 26, 22, 29)
    return img


# ─── Pet (sprout-pig upgrade) ──────────────────────────────────────────────

def _draw_sprout(img, cx, cy):
    vline(img, cx, cy, cy + 4, DGREEN)
    vline(img, cx + 1, cy + 1, cy + 4, SAGE)
    # left leaf
    fill_rect(img, cx - 3, cy, cx - 1, cy + 2, SAGE)
    px(img, cx - 3, cy, OUTLINE)
    px(img, cx - 1, cy + 2, OUTLINE)
    px(img, cx - 4, cy + 1, OUTLINE)
    # right leaf
    fill_rect(img, cx + 2, cy - 1, cx + 4, cy + 1, LGREEN)
    px(img, cx + 2, cy - 1, OUTLINE)
    px(img, cx + 4, cy + 1, OUTLINE)
    px(img, cx + 5, cy, OUTLINE)
    px(img, cx, cy - 1, OUTLINE)


def _draw_pig_body(img, happy: bool, eating: bool = False) -> None:
    """Upgraded sprout-pig: deeper outline, clearer snout, optional eat pose."""
    # body oval
    fill_rect(img, 8, 12, 23, 26, PINK)
    fill_rect(img, 10, 10, 21, 12, PINK)
    fill_rect(img, 10, 26, 21, 28, PINK)
    fill_rect(img, 6, 14, 8, 24, PINK_D)
    fill_rect(img, 23, 14, 25, 24, PINK_D)
    fill_rect(img, 12, 16, 19, 24, PINK)
    fill_rect(img, 13, 18, 18, 23, CREAM)
    # deep body outline
    hline(img, 10, 21, 9, OUTLINE)
    hline(img, 10, 21, 29, OUTLINE)
    vline(img, 5, 14, 24, OUTLINE)
    vline(img, 26, 14, 24, OUTLINE)
    hline(img, 9, 22, 8, DECOR_OUTLINE)
    hline(img, 9, 22, 30, DECOR_OUTLINE)
    vline(img, 4, 13, 25, DECOR_OUTLINE)
    vline(img, 27, 13, 25, DECOR_OUTLINE)
    px(img, 6, 12, OUTLINE); px(img, 7, 11, OUTLINE); px(img, 8, 10, OUTLINE)
    px(img, 25, 12, OUTLINE); px(img, 24, 11, OUTLINE); px(img, 23, 10, OUTLINE)
    px(img, 6, 26, OUTLINE); px(img, 7, 27, OUTLINE); px(img, 8, 28, OUTLINE)
    px(img, 25, 26, OUTLINE); px(img, 24, 27, OUTLINE); px(img, 23, 28, OUTLINE)
    # ears
    fill_rect(img, 7, 8, 11, 12, PINK_D)
    fill_rect(img, 20, 8, 24, 12, PINK_D)
    fill_rect(img, 8, 9, 10, 11, PINK)
    fill_rect(img, 21, 9, 23, 11, PINK)
    deep_outline(img, 7, 8, 11, 12)
    deep_outline(img, 20, 8, 24, 12)
    # snout
    fill_rect(img, 12, 20, 19, 25, SNORT)
    fill_rect(img, 13, 19, 18, 19, SNORT)
    deep_outline(img, 12, 19, 19, 25)
    px(img, 14, 22, PINK_DK)
    px(img, 17, 22, PINK_DK)
    px(img, 14, 23, OUTLINE)
    px(img, 17, 23, OUTLINE)
    # eyes / eat
    if eating:
        # content slits
        fill_rect(img, 11, 14, 13, 16, NEAR_BLACK)
        fill_rect(img, 18, 14, 20, 16, NEAR_BLACK)
        # open mouth with morsel
        fill_rect(img, 13, 24, 18, 27, PINK_DK)
        deep_outline(img, 13, 24, 18, 27)
        fill_rect(img, 14, 22, 17, 24, GOLD)  # food
        px(img, 15, 23, GOLD_L)
    elif happy:
        px(img, 11, 15, OUTLINE); px(img, 12, 14, OUTLINE); px(img, 13, 15, OUTLINE)
        px(img, 18, 15, OUTLINE); px(img, 19, 14, OUTLINE); px(img, 20, 15, OUTLINE)
        px(img, 9, 18, HEART_L); px(img, 10, 18, HEART_L)
        px(img, 21, 18, HEART_L); px(img, 22, 18, HEART_L)
    else:
        fill_rect(img, 11, 14, 12, 16, NEAR_BLACK)
        fill_rect(img, 19, 14, 20, 16, NEAR_BLACK)
        px(img, 11, 14, WHITE)
        px(img, 19, 14, WHITE)
    # feet
    for fx in (9, 14, 18, 22):
        fill_rect(img, fx, 28, fx + 2, 30, PINK_D)
        hline(img, fx, fx + 2, 31, OUTLINE)
        vline(img, fx - 1, 28, 30, OUTLINE)
        vline(img, fx + 3, 28, 30, OUTLINE)
    _draw_sprout(img, 15, 3)


def pet_idle():
    img = new_img()
    _draw_pig_body(img, happy=False)
    return img


def pet_happy():
    img = new_img()
    _draw_pig_body(img, happy=True)
    px(img, 18, 2, GOLD)
    px(img, 12, 4, GOLD)
    px(img, 16, 1, GOLD_L)
    return img


def pet_eat():
    img = new_img()
    _draw_pig_body(img, happy=True, eating=True)
    # crumbs
    px(img, 11, 26, GOLD)
    px(img, 20, 27, GOLD_L)
    return img


# ─── FX ────────────────────────────────────────────────────────────────────

def fx_heart():
    """Refresh interaction heart — deeper outline."""
    img = new_img()
    fill_rect(img, 8, 10, 14, 16, HEART)
    fill_rect(img, 17, 10, 23, 16, HEART)
    fill_rect(img, 10, 8, 13, 9, HEART)
    fill_rect(img, 18, 8, 21, 9, HEART)
    fill_rect(img, 10, 17, 21, 20, HEART)
    fill_rect(img, 12, 21, 19, 23, HEART)
    fill_rect(img, 14, 24, 17, 25, HEART)
    fill_rect(img, 10, 10, 12, 12, HEART_L)
    px(img, 11, 9, WHITE)
    # deep outline
    hline(img, 10, 13, 7, OUTLINE)
    hline(img, 18, 21, 7, OUTLINE)
    hline(img, 9, 14, 6, DECOR_OUTLINE)
    hline(img, 17, 22, 6, DECOR_OUTLINE)
    vline(img, 7, 10, 16, OUTLINE)
    vline(img, 24, 10, 16, OUTLINE)
    vline(img, 6, 10, 16, DECOR_OUTLINE)
    vline(img, 25, 10, 16, DECOR_OUTLINE)
    px(img, 8, 9, OUTLINE); px(img, 9, 8, OUTLINE)
    px(img, 14, 8, OUTLINE); px(img, 15, 9, OUTLINE); px(img, 16, 9, OUTLINE)
    px(img, 17, 8, OUTLINE)
    px(img, 22, 8, OUTLINE); px(img, 23, 9, OUTLINE)
    px(img, 8, 17, OUTLINE); px(img, 9, 18, OUTLINE)
    px(img, 23, 17, OUTLINE); px(img, 22, 18, OUTLINE)
    px(img, 10, 19, OUTLINE); px(img, 11, 20, OUTLINE)
    px(img, 21, 19, OUTLINE); px(img, 20, 20, OUTLINE)
    px(img, 12, 22, OUTLINE); px(img, 13, 23, OUTLINE)
    px(img, 19, 22, OUTLINE); px(img, 18, 23, OUTLINE)
    px(img, 14, 26, OUTLINE); px(img, 15, 26, OUTLINE)
    px(img, 16, 26, OUTLINE); px(img, 17, 26, OUTLINE)
    hline(img, 14, 17, 25, OUTLINE)
    hline(img, 13, 18, 27, DECOR_OUTLINE)
    return img


def fx_sparkle_ready():
    """Maturity highlight sparkle for READY crops."""
    img = new_img()
    # four-point star center
    cx, cy = 15, 15
    fill_rect(img, cx - 1, cy - 6, cx + 1, cy + 6, SPARKLE)
    fill_rect(img, cx - 6, cy - 1, cx + 6, cy + 1, SPARKLE)
    fill_rect(img, cx - 2, cy - 2, cx + 2, cy + 2, GOLD_L)
    fill_rect(img, cx - 1, cy - 1, cx + 1, cy + 1, WHITE)
    # diagonal tips
    for dx, dy in ((-4, -4), (4, -4), (-4, 4), (4, 4), (-3, 0), (3, 0), (0, -3), (0, 3)):
        px(img, cx + dx, cy + dy, GOLD)
        px(img, cx + dx, cy + dy - 1, OUTLINE)
    # corner mini sparkles
    for sx, sy in ((6, 6), (25, 7), (7, 24), (24, 25)):
        px(img, sx, sy, GOLD_L)
        px(img, sx + 1, sy, WHITE)
        px(img, sx, sy - 1, GOLD)
        px(img, sx - 1, sy, OUTLINE)
    # soft ring hint
    rect_outline(img, 8, 8, 22, 22, (0xE8, 0xA8, 0x38, 120))
    return img


def item_feed():
    """Feed sack with grain spill."""
    img = new_img()
    fill_rect(img, 8, 10, 23, 26, SOIL_L)
    fill_rect(img, 10, 12, 21, 24, SOIL_D)
    fill_rect(img, 12, 14, 19, 22, GOLD)
    fill_rect(img, 12, 6, 19, 10, BRICK)
    fill_rect(img, 13, 4, 18, 6, BRICK)
    hline(img, 11, 20, 9, OUTLINE)
    for y in (14, 18, 22):
        hline(img, 11, 20, y, BRICK)
    for x, y in ((7, 24), (8, 25), (24, 24), (25, 25), (15, 27), (17, 28)):
        px(img, x, y, GOLD_L)
        px(img, x + 1, y, GOLD)
    deep_outline(img, 8, 10, 23, 26)
    rect_outline(img, 12, 4, 19, 9, OUTLINE)
    return img


def fx_eat():
    """Bite crumbs + mini heart FX (readable at 32px)."""
    img = new_img()
    # food morsel cluster
    fill_rect(img, 11, 14, 16, 18, GOLD)
    fill_rect(img, 12, 13, 15, 13, GOLD_L)
    fill_rect(img, 12, 15, 14, 16, SOIL_D)
    deep_outline(img, 11, 13, 16, 18)
    # crumbs
    for x, y in ((8, 20), (18, 19), (20, 22), (7, 15), (19, 12)):
        px(img, x, y, GOLD)
        px(img, x + 1, y, GOLD_L)
        px(img, x, y + 1, SOIL_D)
    # mini heart
    fill_rect(img, 22, 8, 24, 10, HEART)
    fill_rect(img, 25, 8, 27, 10, HEART)
    fill_rect(img, 23, 11, 26, 12, HEART)
    px(img, 24, 13, HEART)
    px(img, 22, 7, OUTLINE); px(img, 27, 7, OUTLINE)
    px(img, 24, 14, OUTLINE)
    return img


def fx_settle_stamp():
    """Alias compact stamp for overlay FX (kept beside ic_settle_stamp)."""
    return ic_settle_stamp()



def save(name: str, img: Image.Image) -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    PREVIEW.mkdir(parents=True, exist_ok=True)
    assert img.size == (32, 32), f"{name} {img.size}"
    img.save(OUT / name, "PNG")
    img.resize((256, 256), Image.NEAREST).save(PREVIEW / name)
    print(f"wrote {name}")


def main() -> None:
    # Prefer clean moon for CTA
    gens = {
        "ic_moon_settle.png": ic_moon_settle_clean,
        "ic_settle_stamp.png": ic_settle_stamp,
        "settle_banner.png": settle_banner,
        "moon_rise_f0.png": lambda: moon_rise_frame(0),
        "moon_rise_f1.png": lambda: moon_rise_frame(1),
        "moon_rise_f2.png": lambda: moon_rise_frame(2),
        "pet_idle.png": pet_idle,
        "pet_happy.png": pet_happy,
        "pet_eat.png": pet_eat,
        "fx_heart.png": fx_heart,
        "fx_sparkle_ready.png": fx_sparkle_ready,
        "item_feed.png": item_feed,
        "fx_eat.png": fx_eat,
        "fx_settle_stamp.png": fx_settle_stamp,
    }
    for name, gen in gens.items():
        save(name, gen())
    print(f"M3 art done: {len(gens)} files")


if __name__ == "__main__":
    main()
