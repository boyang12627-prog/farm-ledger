#!/usr/bin/env python3
"""M2 art: field crops + tools + ledger + buy/sell + inventory icons.

Self-drawn 32px warm pixel, deep outlines (#6B4A2E / #3D2A18), nearest-neighbor.
Does not touch reward / settle formulas — assets + docs (+ optional UI icon wire).

  /tmp/pilvenv/bin/python scripts/gen_m2_art_assets.py
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
WHEAT_DK = (0xC4, 0x86, 0x28, 255)
LGREEN = (0xB8, 0xD9, 0x7A, 255)
RED = (0xD4, 0x3A, 0x2F, 255)
RED_DK = (0xA8, 0x28, 0x22, 255)
BRICK = (0xC7, 0x5B, 0x39, 255)
GOLD = (0xE8, 0xA8, 0x38, 255)
GOLD_L = (0xF4, 0xD0, 0x78, 255)
SAGE = (0x8F, 0xBF, 0x6A, 255)
DGREEN = (0x4F, 0x7A, 0x45, 255)
SKY = (0x7E, 0xB8, 0xC9, 255)
SKIN_L = (0xF5, 0xC6, 0xA8, 255)
SKIN_D = (0xB5, 0x7A, 0x5A, 255)
PAPER = (0xED, 0xE6, 0xD9, 255)
TEXT = (0x5C, 0x53, 0x46, 255)
WHITE = (0xFF, 0xFF, 0xFF, 255)
LEATHER = (0x8B, 0x4E, 0x2E, 255)
LEATHER_L = (0xB8, 0x72, 0x48, 255)
WATER = (0x5A, 0xA8, 0xD4, 255)
WATER_L = (0xA8, 0xD8, 0xF0, 255)
METAL = (0x9A, 0xA0, 0xA8, 255)
METAL_L = (0xD0, 0xD4, 0xDA, 255)
GREEN_P = (0x3A, 0xA8, 0x5A, 255)
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
    """Double outline: warm OUTLINE inner, DECOR_OUTLINE outer."""
    rect_outline(img, x0, y0, x1, y1, OUTLINE)
    rect_outline(img, x0 - 1, y0 - 1, x1 + 1, y1 + 1, DECOR_OUTLINE)


def draw_soil_mound(img, y_top=22):
    fill_rect(img, 7, y_top, 24, 30, SOIL_D)
    fill_rect(img, 9, y_top + 1, 22, 29, SOIL_L)
    for x in range(10, 23, 3):
        vline(img, x, y_top + 2, 28, SOIL_D)
    px(img, 11, y_top + 3, STONE)
    px(img, 12, y_top + 3, STONE_L)
    px(img, 20, y_top + 4, STONE)
    px(img, 16, 29, STONE_L)
    deep_outline(img, 7, y_top, 24, 30)


# ─── Crops (refresh seed / grow / ready) ───────────────────────────────────

def crop_wheat_seed():
    img = new_img()
    draw_soil_mound(img, 22)
    fill_rect(img, 13, 17, 18, 21, GOLD)
    fill_rect(img, 14, 16, 17, 16, GOLD)
    fill_rect(img, 14, 22, 17, 22, WHEAT_DK)
    px(img, 14, 17, CREAM)
    px(img, 15, 17, CREAM)
    deep_outline(img, 13, 16, 18, 22)
    px(img, 15, 14, DGREEN)
    px(img, 16, 14, SAGE)
    px(img, 15, 15, DGREEN)
    px(img, 16, 13, LGREEN)
    return img


def crop_wheat_grow():
    img = new_img()
    draw_soil_mound(img, 24)
    for sx in (12, 15, 18):
        fill_rect(img, sx, 12, sx + 1, 24, DGREEN)
        vline(img, sx - 1, 14, 23, DECOR_OUTLINE)
        vline(img, sx + 2, 14, 23, DECOR_OUTLINE)
        fill_rect(img, sx - 1, 8, sx + 2, 12, SAGE)
        fill_rect(img, sx, 7, sx + 1, 7, LGREEN)
        deep_outline(img, sx - 1, 7, sx + 2, 12)
        px(img, sx, 9, GOLD)
    return img


def crop_wheat_ready():
    img = new_img()
    draw_soil_mound(img, 26)
    for sx in (10, 15, 20):
        fill_rect(img, sx, 14, sx + 1, 26, DGREEN)
        vline(img, sx - 1, 16, 25, DECOR_OUTLINE)
        vline(img, sx + 2, 16, 25, DECOR_OUTLINE)
    heads = [(8, 2, 13, 14), (13, 1, 18, 15), (18, 2, 23, 14)]
    for x0, y0, x1, y1 in heads:
        fill_rect(img, x0, y0, x1, y1, GOLD)
        for y in range(y0 + 1, y1, 2):
            for x in range(x0 + 1, x1):
                px(img, x, y, WHEAT_DK if (x + y) % 2 else CREAM)
        deep_outline(img, x0, y0, x1, y1)
        fill_rect(img, x0 + 2, y0 - 1, x1 - 2, y0 - 1, GOLD)
        hline(img, x0 + 2, x1 - 2, y0 - 1, OUTLINE)
    return img


def crop_carrot_seed():
    img = new_img()
    draw_soil_mound(img, 22)
    fill_rect(img, 14, 18, 17, 21, ORANGE)
    fill_rect(img, 15, 17, 16, 17, BRICK)
    px(img, 15, 18, GOLD)
    deep_outline(img, 14, 17, 17, 21)
    px(img, 14, 15, SAGE)
    px(img, 15, 14, DGREEN)
    px(img, 16, 15, SAGE)
    px(img, 15, 16, DGREEN)
    return img


def crop_carrot_grow():
    img = new_img()
    draw_soil_mound(img, 24)
    fill_rect(img, 13, 16, 18, 23, ORANGE)
    fill_rect(img, 14, 15, 17, 15, BRICK)
    hline(img, 14, 17, 18, GOLD)
    hline(img, 14, 17, 21, BRICK)
    deep_outline(img, 13, 15, 18, 23)
    fill_rect(img, 14, 8, 17, 15, DGREEN)
    fill_rect(img, 11, 10, 13, 14, SAGE)
    fill_rect(img, 18, 10, 20, 14, SAGE)
    deep_outline(img, 14, 8, 17, 15)
    vline(img, 10, 11, 13, DECOR_OUTLINE)
    vline(img, 21, 11, 13, DECOR_OUTLINE)
    hline(img, 11, 13, 10, OUTLINE)
    hline(img, 18, 20, 10, OUTLINE)
    return img


def crop_carrot_ready():
    img = new_img()
    draw_soil_mound(img, 26)
    fill_rect(img, 12, 10, 19, 22, ORANGE)
    fill_rect(img, 13, 23, 18, 25, ORANGE)
    fill_rect(img, 14, 26, 17, 27, BRICK)
    fill_rect(img, 15, 28, 16, 28, BRICK)
    for y in (13, 17, 21):
        hline(img, 13, 18, y, GOLD)
    for y in (15, 19, 24):
        hline(img, 13, 18, y, BRICK)
    deep_outline(img, 12, 10, 19, 22)
    px(img, 12, 23, DECOR_OUTLINE)
    px(img, 19, 23, DECOR_OUTLINE)
    px(img, 13, 26, DECOR_OUTLINE)
    px(img, 18, 26, DECOR_OUTLINE)
    px(img, 14, 28, DECOR_OUTLINE)
    px(img, 17, 28, DECOR_OUTLINE)
    hline(img, 15, 16, 29, DECOR_OUTLINE)
    fill_rect(img, 9, 2, 11, 9, SAGE)
    fill_rect(img, 20, 2, 22, 9, SAGE)
    fill_rect(img, 13, 1, 18, 9, DGREEN)
    fill_rect(img, 11, 4, 12, 8, SAGE)
    fill_rect(img, 19, 4, 20, 8, SAGE)
    fill_rect(img, 14, 0, 17, 0, LGREEN)
    deep_outline(img, 13, 1, 18, 9)
    vline(img, 8, 3, 8, DECOR_OUTLINE)
    vline(img, 23, 3, 8, DECOR_OUTLINE)
    hline(img, 9, 11, 2, OUTLINE)
    hline(img, 20, 22, 2, OUTLINE)
    px(img, 15, 3, SAGE)
    px(img, 16, 5, LGREEN)
    return img


def crop_tomato_seed():
    img = new_img()
    draw_soil_mound(img, 22)
    fill_rect(img, 14, 17, 17, 21, RED_DK)
    fill_rect(img, 15, 16, 16, 16, BRICK)
    px(img, 15, 18, CREAM)
    deep_outline(img, 14, 16, 17, 21)
    px(img, 15, 14, DGREEN)
    px(img, 16, 13, SAGE)
    px(img, 14, 13, SAGE)
    px(img, 15, 15, DGREEN)
    return img


def crop_tomato_grow():
    img = new_img()
    draw_soil_mound(img, 24)
    fill_rect(img, 15, 12, 16, 24, DGREEN)
    vline(img, 14, 14, 23, DECOR_OUTLINE)
    vline(img, 17, 14, 23, DECOR_OUTLINE)
    fill_rect(img, 10, 8, 21, 14, SAGE)
    fill_rect(img, 12, 6, 19, 7, DGREEN)
    deep_outline(img, 10, 6, 21, 14)
    fill_rect(img, 12, 10, 14, 13, LGREEN)
    fill_rect(img, 17, 10, 19, 13, LGREEN)
    deep_outline(img, 12, 10, 14, 13)
    deep_outline(img, 17, 10, 19, 13)
    px(img, 13, 9, DGREEN)
    px(img, 18, 9, DGREEN)
    return img


def crop_tomato_ready():
    img = new_img()
    draw_soil_mound(img, 26)
    fill_rect(img, 15, 16, 16, 26, DGREEN)
    vline(img, 14, 18, 25, DECOR_OUTLINE)
    vline(img, 17, 18, 25, DECOR_OUTLINE)
    fill_rect(img, 12, 2, 19, 6, DGREEN)
    fill_rect(img, 10, 4, 11, 7, SAGE)
    fill_rect(img, 20, 4, 21, 7, SAGE)
    deep_outline(img, 12, 2, 19, 6)
    fruits = [(7, 8, 14, 16), (17, 8, 24, 16), (11, 14, 20, 22)]
    for x0, y0, x1, y1 in fruits:
        fill_rect(img, x0, y0, x1, y1, RED)
        px(img, x0, y0, TRANSPARENT)
        px(img, x1, y0, TRANSPARENT)
        px(img, x0, y1, TRANSPARENT)
        px(img, x1, y1, TRANSPARENT)
        fill_rect(img, x0 + 2, y0 + 2, x0 + 4, y0 + 4, BRICK)
        px(img, x0 + 3, y0 + 3, CREAM)
        hline(img, x0 + 2, x1 - 2, y1 - 1, RED_DK)
        deep_outline(img, x0, y0, x1, y1)
        px(img, x0 + 1, y0, OUTLINE)
        px(img, x1 - 1, y0, OUTLINE)
        px(img, x0 + 1, y1, OUTLINE)
        px(img, x1 - 1, y1, OUTLINE)
    for cx in (10, 20):
        fill_rect(img, cx, 6, cx + 1, 8, DGREEN)
    return img


# ─── Tools / hand ──────────────────────────────────────────────────────────

def ic_tool_hoe():
    img = new_img()
    # wooden handle
    fill_rect(img, 14, 8, 17, 28, SOIL_D)
    fill_rect(img, 15, 8, 16, 27, LEATHER_L)
    deep_outline(img, 14, 8, 17, 28)
    # metal blade (top, angled)
    fill_rect(img, 6, 4, 24, 9, METAL)
    fill_rect(img, 8, 3, 22, 5, METAL_L)
    fill_rect(img, 5, 6, 10, 10, METAL)
    fill_rect(img, 20, 6, 25, 10, METAL)
    deep_outline(img, 6, 4, 24, 9)
    deep_outline(img, 5, 6, 10, 10)
    deep_outline(img, 20, 6, 25, 10)
    # rivet
    fill_rect(img, 14, 6, 17, 8, GOLD)
    rect_outline(img, 14, 6, 17, 8, DECOR_OUTLINE)
    return img


def cursor_hand():
    """Simple pointing / open hand cursor for plot select."""
    img = new_img()
    # palm
    fill_rect(img, 10, 14, 22, 26, SKIN_L)
    fill_rect(img, 12, 16, 20, 24, SKIN_D)
    deep_outline(img, 10, 14, 22, 26)
    # fingers up
    for x0, tip in ((10, 6), (14, 4), (18, 5), (22, 8)):
        fill_rect(img, x0, tip, x0 + 3, 15, SKIN_L)
        fill_rect(img, x0 + 1, tip + 1, x0 + 2, 14, SKIN_D)
        deep_outline(img, x0, tip, x0 + 3, 15)
    # thumb
    fill_rect(img, 6, 16, 10, 22, SKIN_L)
    fill_rect(img, 7, 17, 9, 21, SKIN_D)
    deep_outline(img, 6, 16, 10, 22)
    # cuff
    fill_rect(img, 12, 26, 20, 30, SAGE)
    deep_outline(img, 12, 26, 20, 30)
    return img


def ic_tool_water():
    img = new_img()
    # can body
    fill_rect(img, 8, 10, 22, 26, WATER)
    fill_rect(img, 10, 12, 20, 24, WATER_L)
    deep_outline(img, 8, 10, 22, 26)
    # spout
    fill_rect(img, 22, 12, 28, 16, METAL)
    fill_rect(img, 26, 10, 29, 14, METAL_L)
    deep_outline(img, 22, 12, 28, 16)
    deep_outline(img, 26, 10, 29, 14)
    # handle
    fill_rect(img, 12, 4, 18, 10, SOIL_D)
    fill_rect(img, 13, 5, 17, 9, LEATHER_L)
    deep_outline(img, 12, 4, 18, 10)
    # water drops
    px(img, 28, 18, WATER_L)
    px(img, 29, 20, WATER)
    px(img, 27, 21, WATER_L)
    return img


# ─── Ledger book ───────────────────────────────────────────────────────────

def ic_ledger_book(frame: int = 0):
    """Leather ledger; frame 0 closed, 1–2 open flip."""
    img = new_img()
    if frame == 0:
        # closed book
        fill_rect(img, 6, 4, 25, 28, LEATHER)
        fill_rect(img, 8, 6, 23, 26, LEATHER_L)
        fill_rect(img, 6, 4, 9, 28, SOIL_D)
        deep_outline(img, 6, 4, 25, 28)
        vline(img, 9, 4, 28, DECOR_OUTLINE)
        # gold clasp
        fill_rect(img, 18, 12, 24, 18, GOLD)
        fill_rect(img, 19, 13, 23, 17, GOLD_L)
        deep_outline(img, 18, 12, 24, 18)
        # title lines
        for y in (10, 14, 18):
            hline(img, 11, 16, y, PAPER)
        return img
    # open book — pages
    fill_rect(img, 3, 6, 28, 27, LEATHER)
    deep_outline(img, 3, 6, 28, 27)
    # left page
    fill_rect(img, 5, 8, 15, 25, PAPER)
    # right page (shift with frame for flip)
    rx0 = 16 + (1 if frame == 2 else 0)
    rx1 = 26 + (1 if frame == 2 else 0)
    fill_rect(img, rx0, 8, min(rx1, 28), 25, CREAM if frame == 1 else SAND)
    deep_outline(img, 5, 8, 15, 25)
    deep_outline(img, rx0, 8, min(rx1, 28), 25)
    # spine shadow
    vline(img, 15, 8, 25, OUTLINE)
    vline(img, 16, 8, 25, DECOR_OUTLINE)
    # writing
    for y in (11, 14, 17, 20):
        hline(img, 7, 13, y, TEXT)
        if frame >= 1:
            hline(img, rx0 + 2, min(rx1 - 1, 26), y, TEXT)
    # bookmark
    fill_rect(img, 14, 4, 17, 8, BRICK)
    deep_outline(img, 14, 4, 17, 8)
    return img


# ─── Buy / sell ────────────────────────────────────────────────────────────

def ic_buy_bag():
    img = new_img()
    # sack body
    fill_rect(img, 7, 10, 24, 28, SOIL_M)
    fill_rect(img, 9, 12, 22, 26, SAND)
    deep_outline(img, 7, 10, 24, 28)
    # cinch top
    fill_rect(img, 11, 6, 20, 11, SOIL_D)
    fill_rect(img, 12, 5, 19, 7, LEATHER)
    deep_outline(img, 11, 6, 20, 11)
    # string
    hline(img, 10, 21, 8, DECOR_OUTLINE)
    # seed peek
    fill_rect(img, 13, 16, 18, 20, GOLD)
    px(img, 14, 17, CREAM)
    deep_outline(img, 13, 16, 18, 20)
    # $-ish stitch
    fill_rect(img, 15, 22, 16, 25, BRICK)
    return img


def ic_sell_basket():
    img = new_img()
    # basket weave
    fill_rect(img, 5, 14, 26, 28, SOIL_D)
    fill_rect(img, 7, 16, 24, 26, SOIL_M)
    for y in range(16, 27, 2):
        hline(img, 7, 24, y, LEATHER_L if y % 4 == 0 else SOIL_D)
    deep_outline(img, 5, 14, 26, 28)
    # handle
    fill_rect(img, 10, 6, 12, 15, SOIL_D)
    fill_rect(img, 19, 6, 21, 15, SOIL_D)
    fill_rect(img, 10, 4, 21, 7, LEATHER)
    deep_outline(img, 10, 4, 21, 7)
    vline(img, 10, 6, 15, DECOR_OUTLINE)
    vline(img, 21, 6, 15, DECOR_OUTLINE)
    # veggies peeking
    fill_rect(img, 8, 10, 13, 15, ORANGE)
    fill_rect(img, 14, 9, 19, 15, GOLD)
    fill_rect(img, 20, 11, 24, 15, RED)
    deep_outline(img, 8, 10, 13, 15)
    deep_outline(img, 14, 9, 19, 15)
    deep_outline(img, 20, 11, 24, 15)
    px(img, 10, 9, SAGE)
    px(img, 16, 8, DGREEN)
    return img


def _coin_base(img):
    fill_rect(img, 8, 8, 23, 23, GOLD)
    fill_rect(img, 10, 6, 21, 25, GOLD)
    fill_rect(img, 6, 10, 25, 21, GOLD)
    fill_rect(img, 10, 10, 21, 21, GOLD_L)
    fill_rect(img, 12, 12, 19, 19, CREAM)
    deep_outline(img, 8, 8, 23, 23)
    deep_outline(img, 10, 6, 21, 25)
    deep_outline(img, 6, 10, 25, 21)


def ic_coin_plus():
    img = new_img()
    _coin_base(img)
    # green plus
    fill_rect(img, 14, 10, 17, 21, GREEN_P)
    fill_rect(img, 10, 14, 21, 17, GREEN_P)
    rect_outline(img, 14, 10, 17, 21, DECOR_OUTLINE)
    rect_outline(img, 10, 14, 21, 17, DECOR_OUTLINE)
    return img


def ic_coin_minus():
    img = new_img()
    _coin_base(img)
    # red minus
    fill_rect(img, 10, 14, 21, 17, RED)
    rect_outline(img, 10, 14, 21, 17, DECOR_OUTLINE)
    return img


# ─── Inventory items ───────────────────────────────────────────────────────

def item_seed(kind: str):
    """Seed packet with crop accent color."""
    accent = {"wheat": GOLD, "carrot": ORANGE, "tomato": RED}[kind]
    accent_dk = {"wheat": WHEAT_DK, "carrot": BRICK, "tomato": RED_DK}[kind]
    img = new_img()
    fill_rect(img, 7, 4, 24, 28, PAPER)
    fill_rect(img, 7, 4, 24, 11, accent)
    fill_rect(img, 9, 6, 22, 9, accent_dk)
    deep_outline(img, 7, 4, 24, 28)
    hline(img, 7, 24, 11, DECOR_OUTLINE)
    # seed oval
    fill_rect(img, 12, 15, 19, 23, SOIL_D)
    fill_rect(img, 13, 14, 18, 14, SOIL_D)
    fill_rect(img, 13, 24, 18, 24, SOIL_D)
    px(img, 14, 16, SAND)
    px(img, 15, 16, SAND)
    deep_outline(img, 12, 14, 19, 24)
    # tiny sprout
    px(img, 15, 13, DGREEN)
    px(img, 16, 12, SAGE)
    return img


def item_harvest(kind: str):
    """Harvested produce icon (no soil mound — inventory tile)."""
    img = new_img()
    if kind == "wheat":
        # three heads
        for sx, ox in ((8, 0), (14, -1), (20, 0)):
            fill_rect(img, sx, 14, sx + 1, 28, DGREEN)
            vline(img, sx - 1, 16, 27, DECOR_OUTLINE)
            vline(img, sx + 2, 16, 27, DECOR_OUTLINE)
            fill_rect(img, sx - 2 + ox, 4, sx + 3 + ox, 14, GOLD)
            for y in range(5, 14, 2):
                for x in range(sx - 1 + ox, sx + 3 + ox):
                    px(img, x, y, WHEAT_DK if (x + y) % 2 else CREAM)
            deep_outline(img, sx - 2 + ox, 4, sx + 3 + ox, 14)
        return img
    if kind == "carrot":
        fill_rect(img, 12, 10, 19, 26, ORANGE)
        fill_rect(img, 13, 27, 18, 28, BRICK)
        fill_rect(img, 14, 29, 17, 29, BRICK)
        for y in (13, 17, 21):
            hline(img, 13, 18, y, GOLD)
        deep_outline(img, 12, 10, 19, 26)
        fill_rect(img, 13, 2, 18, 10, DGREEN)
        fill_rect(img, 10, 4, 12, 9, SAGE)
        fill_rect(img, 19, 4, 21, 9, SAGE)
        deep_outline(img, 13, 2, 18, 10)
        return img
    # tomato
    fill_rect(img, 8, 10, 23, 26, RED)
    fill_rect(img, 10, 8, 21, 28, RED)
    fill_rect(img, 12, 12, 15, 15, BRICK)
    px(img, 13, 13, CREAM)
    hline(img, 10, 21, 24, RED_DK)
    deep_outline(img, 8, 10, 23, 26)
    deep_outline(img, 10, 8, 21, 28)
    fill_rect(img, 13, 4, 18, 9, DGREEN)
    fill_rect(img, 11, 6, 12, 8, SAGE)
    fill_rect(img, 19, 6, 20, 8, SAGE)
    deep_outline(img, 13, 4, 18, 9)
    return img


def item_material():
    """Generic material crate (InventoryItemKind.MATERIAL)."""
    img = new_img()
    fill_rect(img, 6, 10, 25, 28, SOIL_D)
    fill_rect(img, 8, 12, 23, 26, SOIL_M)
    fill_rect(img, 8, 12, 23, 14, SAND)
    deep_outline(img, 6, 10, 25, 28)
    # planks
    hline(img, 8, 23, 18, OUTLINE)
    hline(img, 8, 23, 22, OUTLINE)
    vline(img, 15, 12, 26, OUTLINE)
    # nail
    fill_rect(img, 14, 16, 16, 18, METAL)
    return img


def save(name: str, img: Image.Image) -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    PREVIEW.mkdir(parents=True, exist_ok=True)
    assert img.size == (32, 32), f"{name} {img.size}"
    img.save(OUT / name, "PNG")
    img.resize((256, 256), Image.NEAREST).save(PREVIEW / name)
    print(f"wrote {name}")


def main() -> None:
    gens = {
        # crops refresh
        "crop_wheat_seed.png": crop_wheat_seed,
        "crop_wheat_grow.png": crop_wheat_grow,
        "crop_wheat_ready.png": crop_wheat_ready,
        "crop_carrot_seed.png": crop_carrot_seed,
        "crop_carrot_grow.png": crop_carrot_grow,
        "crop_carrot_ready.png": crop_carrot_ready,
        "crop_tomato_seed.png": crop_tomato_seed,
        "crop_tomato_grow.png": crop_tomato_grow,
        "crop_tomato_ready.png": crop_tomato_ready,
        # tools
        "ic_tool_hoe.png": ic_tool_hoe,
        "cursor_hand.png": cursor_hand,
        "ic_tool_water.png": ic_tool_water,
        # ledger
        "ic_ledger_book.png": lambda: ic_ledger_book(0),
        "ic_ledger_book_f1.png": lambda: ic_ledger_book(1),
        "ic_ledger_book_f2.png": lambda: ic_ledger_book(2),
        # buy/sell
        "ic_buy_bag.png": ic_buy_bag,
        "ic_sell_basket.png": ic_sell_basket,
        "ic_coin_plus.png": ic_coin_plus,
        "ic_coin_minus.png": ic_coin_minus,
        # inventory
        "item_seed_wheat.png": lambda: item_seed("wheat"),
        "item_seed_carrot.png": lambda: item_seed("carrot"),
        "item_seed_tomato.png": lambda: item_seed("tomato"),
        "item_harvest_wheat.png": lambda: item_harvest("wheat"),
        "item_harvest_carrot.png": lambda: item_harvest("carrot"),
        "item_harvest_tomato.png": lambda: item_harvest("tomato"),
        "item_material.png": item_material,
    }
    for name, gen in gens.items():
        save(name, gen())
    print(f"M2 art done: {len(gens)} files")


if __name__ == "__main__":
    main()
