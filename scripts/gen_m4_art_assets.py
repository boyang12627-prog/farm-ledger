#!/usr/bin/env python3
"""M4 art: shop / expansion + full-day life playable node visuals.

Self-drawn 32–48px warm pixel, deep outlines (#6B4A2E / #3D2A18), nearest-neighbor.
Does not touch reward / settle / anti-farm formulas — assets + docs (+ Compose wire).

  /tmp/pilvenv/bin/python scripts/gen_m4_art_assets.py
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
WALL = (0xF0, 0xE4, 0xC8, 255)
WALL_D = (0xD8, 0xC4, 0xA0, 255)
SOIL_L = (0xD9, 0xB4, 0x8C, 255)
SOIL_M = (0xC4, 0x96, 0x6A, 255)
SOIL_D = (0xA6, 0x7C, 0x52, 255)
OUTLINE = (0x6B, 0x4A, 0x2E, 255)
DECOR_OUTLINE = (0x3D, 0x2A, 0x18, 255)
WOOD = (0xB8, 0x72, 0x48, 255)
WOOD_D = (0x8B, 0x4E, 0x2E, 255)
WOOD_L = (0xD4, 0x9A, 0x68, 255)
STONE = (0x8A, 0x7A, 0x68, 255)
STONE_L = (0xC8, 0xBC, 0xA8, 255)
ORANGE = (0xE8, 0x8A, 0x3A, 255)
GOLD = (0xE8, 0xA8, 0x38, 255)
GOLD_L = (0xF4, 0xD0, 0x78, 255)
LGREEN = (0xB8, 0xD9, 0x7A, 255)
SAGE = (0x8F, 0xBF, 0x6A, 255)
DGREEN = (0x4F, 0x7A, 0x45, 255)
MGREEN = (0x6A, 0xA8, 0x55, 255)
SKY = (0x7E, 0xB8, 0xC9, 255)
ROOF = (0xC7, 0x5B, 0x39, 255)
ROOF_D = (0xA8, 0x42, 0x28, 255)
ROOF_L = (0xE0, 0x78, 0x48, 255)
BRICK = (0xC7, 0x5B, 0x39, 255)
RED = (0xD4, 0x3A, 0x2F, 255)
RED_DK = (0xA8, 0x28, 0x22, 255)
PINK = (0xF2, 0xB8, 0xA8, 255)
SKIN_L = (0xF5, 0xC6, 0xA8, 255)
SKIN_D = (0xB5, 0x7A, 0x5A, 255)
CLOTH = (0x5A, 0x8A, 0xB8, 255)
CLOTH_D = (0x3A, 0x5E, 0x88, 255)
APRON = (0xED, 0xE6, 0xD9, 255)
INK = (0x5C, 0x53, 0x46, 255)
PAPER = (0xED, 0xE6, 0xD9, 255)
WHITE = (0xFF, 0xFF, 0xFF, 255)
BLUE_GLOW = (0xA8, 0xD0, 0xF0, 255)
BLUEPRINT = (0x5A, 0x9A, 0xC8, 255)
BLUEPRINT_L = (0x8A, 0xC4, 0xE0, 255)
LAMP_GLOW = (0xFF, 0xE0, 0x80, 255)
LAMP_CORE = (0xFF, 0xF0, 0xC0, 255)
FLOWER_R = (0xE8, 0x5A, 0x6A, 255)
FLOWER_Y = (0xF4, 0xD0, 0x58, 255)
FLOWER_P = (0xC8, 0x7A, 0xC8, 255)
TRANSPARENT = (0, 0, 0, 0)
NEAR_BLACK = (0x2A, 0x1E, 0x14, 255)


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


# ─── Shop ───────────────────────────────────────────────────────────────────

def shop_stall():
    """48×48 market stall: wood counter + striped awning + crate goods."""
    img = new_img(48, 48)
    # soft ground shadow
    fill_rect(img, 8, 42, 40, 45, (0x3D, 0x2A, 0x18, 100))

    # posts
    fill_rect(img, 8, 18, 11, 41, WOOD_D)
    fill_rect(img, 36, 18, 39, 41, WOOD_D)
    rect_outline(img, 8, 18, 11, 41, DECOR_OUTLINE)
    rect_outline(img, 36, 18, 39, 41, DECOR_OUTLINE)

    # counter shelf
    fill_rect(img, 6, 30, 41, 36, WOOD)
    fill_rect(img, 6, 30, 41, 31, WOOD_L)
    fill_rect(img, 6, 35, 41, 36, WOOD_D)
    deep_outline(img, 6, 30, 41, 36)

    # front apron / cloth
    fill_rect(img, 8, 37, 39, 41, APRON)
    for x in range(10, 38, 4):
        vline(img, x, 37, 41, SAND)
    rect_outline(img, 8, 37, 39, 41, OUTLINE)

    # striped awning (brick / cream)
    for row in range(0, 12):
        y = 8 + row
        x0 = 5 + row // 3
        x1 = 42 - row // 3
        for x in range(x0, x1 + 1):
            stripe = ((x + row) // 3) % 2 == 0
            px(img, x, y, ROOF if stripe else CREAM)
        px(img, x0 - 1, y, DECOR_OUTLINE)
        px(img, x1 + 1, y, DECOR_OUTLINE)
    hline(img, 4, 43, 7, DECOR_OUTLINE)
    hline(img, 6, 41, 19, DECOR_OUTLINE)
    # awning beam
    fill_rect(img, 6, 18, 41, 20, WOOD_D)
    rect_outline(img, 6, 18, 41, 20, DECOR_OUTLINE)

    # goods on counter: sack, basket, produce
    # sack
    fill_rect(img, 10, 24, 17, 29, SOIL_M)
    fill_rect(img, 11, 23, 16, 24, SOIL_L)
    fill_rect(img, 12, 22, 15, 22, SOIL_D)
    deep_outline(img, 10, 22, 17, 29)
    # carrot tips
    fill_rect(img, 20, 25, 22, 29, ORANGE)
    fill_rect(img, 20, 23, 22, 24, SAGE)
    px(img, 21, 22, LGREEN)
    deep_outline(img, 20, 22, 22, 29)
    # tomato crate
    fill_rect(img, 26, 25, 35, 29, WOOD_D)
    fill_rect(img, 27, 24, 34, 25, WOOD)
    disk(img, 29, 26, 2, RED)
    disk(img, 32, 26, 2, RED_DK)
    px(img, 29, 25, GOLD_L)
    deep_outline(img, 26, 24, 35, 29)

    return img


def npc_vendor_idle():
    """32×40 static vendor: apron + hat, facing south (readable half-top)."""
    img = new_img(32, 40)
    # shadow
    fill_rect(img, 9, 36, 22, 38, (0x3D, 0x2A, 0x18, 110))

    # boots
    fill_rect(img, 11, 33, 14, 36, WOOD_D)
    fill_rect(img, 17, 33, 20, 36, WOOD_D)
    rect_outline(img, 11, 33, 14, 36, DECOR_OUTLINE)
    rect_outline(img, 17, 33, 20, 36, DECOR_OUTLINE)

    # legs
    fill_rect(img, 12, 28, 14, 32, CLOTH_D)
    fill_rect(img, 17, 28, 19, 32, CLOTH_D)

    # body / tunic
    fill_rect(img, 10, 18, 21, 28, CLOTH)
    fill_rect(img, 10, 18, 11, 28, CLOTH_D)
    # apron
    fill_rect(img, 12, 20, 19, 30, APRON)
    fill_rect(img, 13, 21, 18, 22, WHITE)
    hline(img, 12, 19, 24, SAND)
    deep_outline(img, 10, 18, 21, 28)
    rect_outline(img, 12, 20, 19, 30, OUTLINE)

    # arms
    fill_rect(img, 7, 19, 9, 26, CLOTH)
    fill_rect(img, 22, 19, 24, 26, CLOTH)
    # hands
    fill_rect(img, 7, 26, 9, 28, SKIN_L)
    fill_rect(img, 22, 26, 24, 28, SKIN_L)
    deep_outline(img, 7, 19, 9, 28)
    deep_outline(img, 22, 19, 24, 28)

    # head
    fill_rect(img, 12, 10, 19, 17, SKIN_L)
    fill_rect(img, 12, 10, 13, 17, SKIN_D)
    # eyes
    px(img, 14, 13, NEAR_BLACK)
    px(img, 17, 13, NEAR_BLACK)
    px(img, 14, 12, WHITE)
    px(img, 17, 12, WHITE)
    # smile
    hline(img, 14, 17, 15, SKIN_D)
    deep_outline(img, 12, 10, 19, 17)

    # hat (straw / warm)
    fill_rect(img, 10, 7, 21, 10, GOLD)
    fill_rect(img, 11, 5, 20, 7, GOLD_L)
    fill_rect(img, 12, 4, 19, 5, GOLD)
    hline(img, 9, 22, 10, WOOD_D)
    deep_outline(img, 10, 4, 21, 10)

    return img


def shop_sign():
    """32×32 hanging wood sign with coin glyph."""
    img = new_img(32, 32)
    # rope
    vline(img, 15, 2, 6, WOOD_D)
    vline(img, 16, 2, 6, WOOD)
    px(img, 15, 2, OUTLINE)
    # board
    fill_rect(img, 6, 7, 25, 24, WOOD)
    fill_rect(img, 7, 8, 24, 9, WOOD_L)
    fill_rect(img, 6, 22, 25, 24, WOOD_D)
    deep_outline(img, 6, 7, 25, 24)
    # coin circle
    disk(img, 16, 15, 5, GOLD)
    disk(img, 15, 14, 3, GOLD_L)
    for y in range(10, 21):
        for x in range(11, 22):
            d2 = (x - 16) ** 2 + (y - 15) ** 2
            if 25 <= d2 <= 36:
                px(img, x, y, OUTLINE)
    # $ tick
    vline(img, 16, 12, 18, WOOD_D)
    hline(img, 14, 18, 13, WOOD_D)
    hline(img, 14, 18, 17, WOOD_D)
    return img


# ─── Expansion ──────────────────────────────────────────────────────────────

def build_plot_empty():
    """32×32 empty build plot: corner stakes + dashed pad."""
    img = new_img(32, 32)
    # dirt pad
    fill_rect(img, 4, 6, 27, 27, SOIL_D)
    fill_rect(img, 5, 7, 26, 26, SOIL_M)
    fill_rect(img, 7, 9, 24, 24, SOIL_L)
    # dashed border
    for x in range(4, 28, 3):
        hline(img, x, min(x + 1, 27), 6, OUTLINE)
        hline(img, x, min(x + 1, 27), 27, OUTLINE)
    for y in range(6, 28, 3):
        vline(img, 4, y, min(y + 1, 27), OUTLINE)
        vline(img, 27, y, min(y + 1, 27), OUTLINE)
    deep_outline(img, 4, 6, 27, 27)
    # corner stakes
    for cx, cy in ((4, 6), (27, 6), (4, 27), (27, 27)):
        fill_rect(img, cx - 1, cy - 2, cx + 1, cy + 1, WOOD_D)
        px(img, cx, cy - 3, WOOD)
        rect_outline(img, cx - 1, cy - 2, cx + 1, cy + 1, DECOR_OUTLINE)
    # center X hint (light)
    for i in range(4):
        px(img, 14 + i, 14 + i, STONE)
        px(img, 17 - i, 14 + i, STONE)
    return img


def building_hut():
    """48×48 refreshed homestead hut (roof + front readable)."""
    img = new_img(48, 48)
    for y in range(40, 46):
        for x in range(10, 38):
            if abs(x - 24) + abs(y - 42) * 2 <= 16:
                px(img, x, y, (0x3D, 0x2A, 0x18, 120))

    fill_rect(img, 10, 26, 37, 41, WALL)
    fill_rect(img, 10, 26, 11, 41, WALL_D)
    fill_rect(img, 36, 26, 37, 41, WALL_D)
    fill_rect(img, 10, 39, 37, 41, WOOD)
    hline(img, 10, 37, 38, WOOD_D)
    # door
    fill_rect(img, 20, 30, 27, 41, WOOD_D)
    fill_rect(img, 21, 31, 26, 40, WOOD)
    px(img, 25, 35, GOLD)
    rect_outline(img, 20, 30, 27, 41, DECOR_OUTLINE)
    # windows
    fill_rect(img, 12, 30, 17, 35, SKY)
    rect_outline(img, 12, 30, 17, 35, DECOR_OUTLINE)
    hline(img, 12, 17, 32, DECOR_OUTLINE)
    vline(img, 14, 30, 35, DECOR_OUTLINE)
    fill_rect(img, 30, 30, 35, 35, SKY)
    rect_outline(img, 30, 30, 35, 35, DECOR_OUTLINE)
    hline(img, 30, 35, 32, DECOR_OUTLINE)
    vline(img, 32, 30, 35, DECOR_OUTLINE)
    rect_outline(img, 10, 26, 37, 41, DECOR_OUTLINE)

    for row in range(0, 18):
        half = 2 + row
        y = 7 + row
        x0 = 24 - half
        x1 = 24 + half
        c = ROOF_L if row < 4 else (ROOF if (row // 3) % 2 == 0 else ROOF_D)
        fill_rect(img, x0, y, x1, y, c)
        px(img, x0 - 1, y, DECOR_OUTLINE)
        px(img, x1 + 1, y, DECOR_OUTLINE)
    for row in range(5):
        half = 15 + row
        y = 25 + row
        x0 = max(5, 24 - half)
        x1 = min(42, 24 + half)
        c = ROOF if row < 2 else ROOF_D
        fill_rect(img, x0, y, x1, y, c)
        px(img, x0 - 1, y, DECOR_OUTLINE)
        px(img, x1 + 1, y, DECOR_OUTLINE)
    px(img, 24, 6, DECOR_OUTLINE)
    fill_rect(img, 23, 7, 25, 8, ROOF_L)
    fill_rect(img, 8, 29, 39, 30, WOOD_D)
    hline(img, 7, 40, 28, DECOR_OUTLINE)
    hline(img, 7, 40, 31, DECOR_OUTLINE)

    # chimney
    fill_rect(img, 31, 9, 36, 18, STONE)
    fill_rect(img, 31, 9, 36, 11, STONE_L)
    fill_rect(img, 32, 12, 35, 17, WOOD_D)
    rect_outline(img, 31, 9, 36, 18, DECOR_OUTLINE)
    fill_rect(img, 32, 5, 34, 7, STONE_L)
    px(img, 33, 4, STONE_L)

    # flower box under left window (M4 refresh accent)
    fill_rect(img, 11, 36, 18, 38, WOOD_D)
    px(img, 12, 35, FLOWER_R)
    px(img, 14, 35, FLOWER_Y)
    px(img, 16, 35, SAGE)
    rect_outline(img, 11, 36, 18, 38, DECOR_OUTLINE)
    return img


def build_blueprint():
    """32×32 translucent blueprint hut silhouette."""
    img = new_img(32, 32)
    # faint pad
    fill_rect(img, 4, 8, 27, 28, (*BLUEPRINT[:3], 60))
    # hut silhouette
    fill_rect(img, 8, 16, 23, 26, (*BLUEPRINT[:3], 160))
    # roof triangle
    for row in range(10):
        half = 1 + row
        y = 6 + row
        fill_rect(img, 16 - half, y, 16 + half, y, (*BLUEPRINT_L[:3], 180))
    # door gap
    fill_rect(img, 13, 20, 18, 26, (*CREAM[:3], 80))
    # dashed outline
    for x in range(8, 24, 2):
        px(img, x, 16, WHITE)
        px(img, x, 26, WHITE)
    for y in range(16, 27, 2):
        px(img, 8, y, WHITE)
        px(img, 23, y, WHITE)
    for x in range(6, 27, 2):
        # roof edge approx
        pass
    deep_outline(img, 8, 16, 23, 26)
    # corner +
    for cx, cy in ((4, 8), (27, 8), (4, 28), (27, 28)):
        hline(img, cx - 2, cx + 2, cy, BLUEPRINT_L)
        vline(img, cx, cy - 2, cy + 2, BLUEPRINT_L)
    return img


# ─── Decor ──────────────────────────────────────────────────────────────────

def decor_lamp():
    """32×32 warm lantern / path lamp."""
    img = new_img(32, 32)
    fill_rect(img, 12, 28, 19, 30, (0x3D, 0x2A, 0x18, 100))
    # post
    fill_rect(img, 14, 16, 17, 28, WOOD_D)
    fill_rect(img, 14, 16, 15, 28, WOOD)
    deep_outline(img, 14, 16, 17, 28)
    # lamp head
    fill_rect(img, 11, 8, 20, 16, WOOD_D)
    fill_rect(img, 12, 9, 19, 15, LAMP_GLOW)
    fill_rect(img, 13, 10, 18, 14, LAMP_CORE)
    # roof cap
    fill_rect(img, 10, 6, 21, 8, WOOD)
    fill_rect(img, 12, 4, 19, 6, WOOD_D)
    px(img, 15, 3, GOLD)
    px(img, 16, 3, GOLD_L)
    deep_outline(img, 11, 8, 20, 16)
    deep_outline(img, 10, 4, 21, 8)
    # glow sparks
    for x, y in ((8, 11), (23, 12), (9, 18), (22, 9)):
        px(img, x, y, LAMP_GLOW)
    return img


def decor_flowerbox():
    """32×32 wooden flower box with mixed blooms."""
    img = new_img(32, 32)
    fill_rect(img, 6, 26, 25, 29, (0x3D, 0x2A, 0x18, 90))
    # box
    fill_rect(img, 5, 18, 26, 26, WOOD)
    fill_rect(img, 5, 18, 26, 19, WOOD_L)
    fill_rect(img, 5, 24, 26, 26, WOOD_D)
    # soil
    fill_rect(img, 7, 16, 24, 19, SOIL_D)
    fill_rect(img, 8, 15, 23, 17, SOIL_M)
    deep_outline(img, 5, 18, 26, 26)
    # flowers
    blooms = [
        (10, 12, FLOWER_R), (14, 10, FLOWER_Y), (18, 11, FLOWER_P),
        (22, 13, FLOWER_R), (12, 14, FLOWER_Y), (20, 14, SAGE),
    ]
    for cx, cy, col in blooms:
        disk(img, cx, cy, 2, col)
        px(img, cx, cy, GOLD_L if col != SAGE else LGREEN)
        px(img, cx, cy + 3, DGREEN)
        px(img, cx, cy + 4, SAGE)
    deep_outline(img, 7, 15, 24, 19)
    return img


def decor_sign():
    """32×32 farm notice board / way sign."""
    img = new_img(32, 32)
    # post
    fill_rect(img, 14, 14, 17, 29, WOOD_D)
    fill_rect(img, 14, 14, 15, 29, WOOD)
    deep_outline(img, 14, 14, 17, 29)
    # board
    fill_rect(img, 5, 6, 26, 16, WOOD)
    fill_rect(img, 6, 7, 25, 8, WOOD_L)
    fill_rect(img, 5, 14, 26, 16, WOOD_D)
    deep_outline(img, 5, 6, 26, 16)
    # text lines
    hline(img, 8, 23, 9, INK)
    hline(img, 8, 20, 11, INK)
    hline(img, 8, 22, 13, INK)
    # tiny sprout badge
    fill_rect(img, 20, 10, 23, 13, SAGE)
    px(img, 21, 9, LGREEN)
    px(img, 22, 9, LGREEN)
    return img


# ─── UI ─────────────────────────────────────────────────────────────────────

def ui_shop_slot():
    """32×32 shop grid cell frame (cream inset)."""
    img = new_img(32, 32)
    fill_rect(img, 2, 2, 29, 29, WOOD_D)
    fill_rect(img, 3, 3, 28, 28, WOOD)
    fill_rect(img, 5, 5, 26, 26, CREAM)
    fill_rect(img, 6, 6, 25, 7, SAND)
    fill_rect(img, 6, 24, 25, 25, SOIL_L)
    deep_outline(img, 2, 2, 29, 29)
    rect_outline(img, 5, 5, 26, 26, OUTLINE)
    # corner nails
    for x, y in ((4, 4), (27, 4), (4, 27), (27, 27)):
        px(img, x, y, GOLD)
    return img


def ui_price_tag():
    """32×24-ish in 32×32: hanging price tag with coin."""
    img = new_img(32, 32)
    # tag body
    fill_rect(img, 6, 8, 25, 24, PAPER)
    fill_rect(img, 7, 9, 24, 10, WHITE)
    # pointed left
    for i in range(4):
        fill_rect(img, 2 + i, 14 - i, 5, 14 + i, PAPER)
    px(img, 3, 14, OUTLINE)  # hole
    px(img, 4, 14, WOOD_D)
    deep_outline(img, 6, 8, 25, 24)
    # string
    hline(img, 1, 6, 14, WOOD_D)
    # coin
    disk(img, 12, 16, 4, GOLD)
    disk(img, 11, 15, 2, GOLD_L)
    for y in range(12, 21):
        for x in range(8, 17):
            d2 = (x - 12) ** 2 + (y - 16) ** 2
            if 16 <= d2 <= 25:
                px(img, x, y, OUTLINE)
    # price bars
    hline(img, 18, 23, 13, INK)
    hline(img, 18, 22, 16, INK)
    hline(img, 18, 23, 19, RED)
    return img


def ic_day_wake():
    img = new_img(32, 32)
    fill_rect(img, 2, 18, 29, 29, SKY)
    disk(img, 16, 18, 7, GOLD)
    disk(img, 16, 18, 4, GOLD_L)
    fill_rect(img, 2, 24, 29, 29, SAGE)
    deep_outline(img, 2, 10, 29, 29)
    for x, y in [(6, 10), (10, 8), (16, 7), (22, 8), (26, 10)]:
        px(img, x, y, GOLD_L)
        px(img, x, y + 1, GOLD)
    return img


def ic_day_work():
    img = new_img(32, 32)
    fill_rect(img, 4, 18, 27, 28, SOIL_D)
    deep_outline(img, 4, 18, 27, 28)
    fill_rect(img, 15, 10, 16, 18, DGREEN)
    disk(img, 12, 12, 3, SAGE)
    disk(img, 20, 12, 3, LGREEN)
    fill_rect(img, 22, 6, 26, 8, STONE_L)
    fill_rect(img, 23, 8, 24, 16, SOIL_M)
    deep_outline(img, 22, 6, 26, 8)
    return img


def ic_day_shop():
    img = new_img(32, 32)
    fill_rect(img, 5, 10, 26, 16, ROOF)
    deep_outline(img, 5, 10, 26, 16)
    fill_rect(img, 8, 17, 23, 26, SOIL_L)
    deep_outline(img, 8, 17, 23, 26)
    disk(img, 12, 16, 2, LGREEN)
    disk(img, 16, 15, 2, ORANGE)
    disk(img, 20, 16, 2, GOLD)
    return img


def ic_day_sleep():
    img = new_img(32, 32)
    fill_rect(img, 3, 3, 28, 28, (0x3A, 0x3A, 0x6A, 255))
    deep_outline(img, 3, 3, 28, 28)
    disk(img, 20, 12, 6, (0xF4, 0xF0, 0xD0, 255))
    disk(img, 17, 11, 4, (0x3A, 0x3A, 0x6A, 255))
    px(img, 8, 8, CREAM)
    px(img, 9, 7, CREAM)
    px(img, 10, 8, CREAM)
    return img


def ic_shop_pole():
    img = new_img(32, 32)
    fill_rect(img, 6, 8, 25, 24, PAPER)
    deep_outline(img, 6, 8, 25, 24)
    fill_rect(img, 14, 24, 17, 29, SOIL_M)
    fill_rect(img, 6, 8, 25, 11, ROOF)
    disk(img, 16, 17, 4, GOLD)
    disk(img, 15, 16, 2, GOLD_L)
    rect_outline(img, 12, 13, 20, 21, OUTLINE)
    return img


def decor_scarecrow():
    img = new_img(32, 32)
    fill_rect(img, 15, 8, 16, 28, SOIL_M)
    fill_rect(img, 6, 14, 25, 16, SOIL_D)
    deep_outline(img, 6, 14, 25, 16)
    disk(img, 16, 10, 4, SAND)
    rect_outline(img, 12, 6, 20, 14, OUTLINE)
    fill_rect(img, 11, 4, 20, 7, BRICK)
    deep_outline(img, 11, 4, 20, 7)
    fill_rect(img, 12, 17, 19, 24, ROOF)
    deep_outline(img, 12, 17, 19, 24)
    return img


def decor_lantern():
    """Alias — same art as decor_lamp (Decoration id lantern)."""
    return decor_lamp()


def building_shop_pole():
    """Compact pole variant kept for CTA / docs alias."""
    img = new_img(32, 40)
    fill_rect(img, 14, 8, 17, 34, SOIL_M)
    vline(img, 13, 8, 34, OUTLINE)
    vline(img, 18, 8, 34, OUTLINE)
    fill_rect(img, 4, 6, 27, 14, ROOF)
    fill_rect(img, 4, 6, 27, 9, ROOF_D)
    deep_outline(img, 4, 6, 27, 14)
    fill_rect(img, 8, 16, 23, 24, PAPER)
    deep_outline(img, 8, 16, 23, 24)
    fill_rect(img, 6, 28, 25, 35, SOIL_L)
    deep_outline(img, 6, 28, 25, 35)
    disk(img, 11, 27, 2, LGREEN)
    disk(img, 16, 26, 2, ORANGE)
    disk(img, 21, 27, 2, RED)
    return img



def save(name: str, img: Image.Image) -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    PREVIEW.mkdir(parents=True, exist_ok=True)
    w, h = img.size
    assert 32 <= w <= 48 and 32 <= h <= 48, f"{name} {img.size}"
    img.save(OUT / name, "PNG")
    scale = 8 if max(w, h) <= 32 else 6
    img.resize((w * scale, h * scale), Image.NEAREST).save(PREVIEW / name)
    print(f"wrote {name} ({w}x{h})")


def main() -> None:
    gens = {
        "shop_stall.png": shop_stall,
        "npc_vendor_idle.png": npc_vendor_idle,
        "shop_sign.png": shop_sign,
        "build_plot_empty.png": build_plot_empty,
        "building_hut.png": building_hut,
        "build_blueprint.png": build_blueprint,
        "decor_lamp.png": decor_lamp,
        "decor_lantern.png": decor_lantern,
        "decor_flowerbox.png": decor_flowerbox,
        "decor_sign.png": decor_sign,
        "decor_scarecrow.png": decor_scarecrow,
        "ui_shop_slot.png": ui_shop_slot,
        "ui_price_tag.png": ui_price_tag,
        "ic_day_wake.png": ic_day_wake,
        "ic_day_work.png": ic_day_work,
        "ic_day_shop.png": ic_day_shop,
        "ic_day_sleep.png": ic_day_sleep,
        "ic_shop_pole.png": ic_shop_pole,
        "building_shop_pole.png": building_shop_pole,
    }
    for name, gen in gens.items():
        save(name, gen())
    print(f"M4 art done: {len(gens)} files")


if __name__ == "__main__":
    main()
