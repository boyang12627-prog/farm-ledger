#!/usr/bin/env python3
"""Generate self-drawn warm-farm top-down scene PNGs (32px tiles/sprites).

Commercial-use original pixel art for the next-stage overhead farm scene.
Minecraft-inspired readability (big color blocks, dark outlines, few mid-tones),
still 2D top-down — not a 3D voxel world.
Uses PIL nearest-neighbor only — no external tilesets.

Outputs land in app/src/main/res/drawable-nodpi/ plus an optional scene preview.
See docs/farm_scene_assets.md for filenames and suggested Compose layering.
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path("/workspace/farm-ledger")
OUT = ROOT / "app/src/main/res/drawable-nodpi"
PREVIEW = ROOT / "scripts/preview_assets"
DOCS_PREVIEW = ROOT / "docs"

# Warm farm palette (aligned with gen_pixel_assets.py + Theme.kt)
# Warm mud tokens (scene dirt bed + plot tiles):
#   SOIL_L / FarmMudHighlight #D9B48C
#   SOIL_M / FarmMudMid       #C4966A
#   SOIL_D / FarmMudDark      #A67C52
#   OUTLINE / FarmStroke      #6B4A2E
#   SAND / FarmSand           #F2E2C4  (cream plot card board)
CREAM = (0xFF, 0xF8, 0xE7, 255)
SAND = (0xF2, 0xE2, 0xC4, 255)
SOIL_L = (0xD9, 0xB4, 0x8C, 255)
SOIL_M = (0xC4, 0x96, 0x6A, 255)
SOIL_D = (0xA6, 0x7C, 0x52, 255)
OUTLINE = (0x6B, 0x4A, 0x2E, 255)  # #6B4A2E
DECOR_OUTLINE = (0x3D, 0x2A, 0x18, 255)  # darker rim for hut/trees (MC readability)
STONE = (0x8A, 0x7A, 0x68, 255)
STONE_L = (0xC8, 0xBC, 0xA8, 255)
LGREEN = (0xB8, 0xD9, 0x7A, 255)
SAGE = (0x8F, 0xBF, 0x6A, 255)
MGREEN = (0x6F, 0xA8, 0x55, 255)
DGREEN = (0x4F, 0x7A, 0x45, 255)
BRICK = (0xC7, 0x5B, 0x39, 255)
GOLD = (0xE8, 0xA8, 0x38, 255)
PINK = (0xF2, 0xB8, 0xA8, 255)
PINK_D = (0xE0, 0x8A, 0x7A, 255)
PINK_DK = (0xC4, 0x6A, 0x5A, 255)
SNORT = (0xE8, 0x9A, 0x8A, 255)
WHITE = (0xFF, 0xFF, 0xFF, 255)
NEAR_BLACK = (0x2F, 0x2A, 0x24, 255)
HEART = (0xE8, 0x5A, 0x6A, 255)
HEART_L = (0xF4, 0x9A, 0xA4, 255)
PATH = (0xC8, 0xA8, 0x78, 255)
PATH_D = (0xA8, 0x88, 0x58, 255)
WOOD = (0xB8, 0x7A, 0x48, 255)
WOOD_L = (0xD4, 0xA0, 0x68, 255)
WOOD_D = (0x8A, 0x5A, 0x32, 255)
ROOF = (0xC7, 0x5B, 0x39, 255)      # brick-red roof
ROOF_D = (0x9A, 0x3E, 0x28, 255)    # deep roof shade
ROOF_L = (0xE0, 0x7A, 0x48, 255)    # warm orange highlight
WALL = (0xF2, 0xE2, 0xC4, 255)      # cream plaster
WALL_D = (0xD9, 0xC4, 0x9A, 255)
LEAF_D = (0x2E, 0x52, 0x2C, 255)    # deep foliage (high contrast)
SHADOW = (0x6B, 0x4A, 0x2E, 90)     # soft brown translucent
SKY = (0xA8, 0xD4, 0xE0, 255)
TRANSPARENT = (0, 0, 0, 0)


def new_img(w: int = 32, h: int = 32) -> Image.Image:
    return Image.new("RGBA", (w, h), TRANSPARENT)


def px(img: Image.Image, x: int, y: int, c) -> None:
    w, h = img.size
    if 0 <= x < w and 0 <= y < h:
        img.putpixel((x, y), c)


def fill_rect(img, x0, y0, x1, y1, c) -> None:
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px(img, x, y, c)


def hline(img, x0, x1, y, c) -> None:
    for x in range(x0, x1 + 1):
        px(img, x, y, c)


def vline(img, x, y0, y1, c) -> None:
    for y in range(y0, y1 + 1):
        px(img, x, y, c)


def rect_outline(img, x0, y0, x1, y1, c) -> None:
    hline(img, x0, x1, y0, c)
    hline(img, x0, x1, y1, c)
    vline(img, x0, y0, y1, c)
    vline(img, x1, y0, y1, c)


# --- Ground tiles -----------------------------------------------------------

def tile_grass() -> Image.Image:
    """Soft sage grass with speckles — seamless-ish warm lawn."""
    img = new_img()
    for y in range(32):
        for x in range(32):
            n = (x * 3 + y * 5) % 7
            if n == 0:
                c = LGREEN
            elif n == 1 or n == 2:
                c = SAGE
            elif n == 3:
                c = MGREEN
            else:
                c = SAGE if ((x + y) % 2 == 0) else LGREEN
            # gentle row variation
            if (y // 4) % 2 == 0 and n == 4:
                c = MGREEN
            px(img, x, y, c)
    # tiny blade hints
    blades = [
        (4, 6, DGREEN), (5, 5, LGREEN), (12, 10, DGREEN), (13, 9, SAGE),
        (20, 4, DGREEN), (21, 3, LGREEN), (7, 18, DGREEN), (8, 17, SAGE),
        (16, 22, DGREEN), (17, 21, LGREEN), (25, 14, DGREEN), (26, 13, SAGE),
        (28, 26, DGREEN), (10, 28, LGREEN), (22, 28, DGREEN), (3, 24, SAGE),
    ]
    for x, y, c in blades:
        px(img, x, y, c)
        if y > 0:
            px(img, x, y - 1, LGREEN if c == DGREEN else SAGE)
    # soft cream highlight dots
    for x, y in [(9, 8), (18, 16), (27, 7), (14, 27), (2, 14)]:
        px(img, x, y, SAND)
    return img


def fill_warm_mud(img: Image.Image, x0: int = 0, y0: int = 0, x1: int = 31, y1: int = 31) -> None:
    """Shared warm-mud fill (same tokens/furrows as tile_soil_empty)."""
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            band = ((x + y // 2) // 3) % 2
            c = SOIL_D if band == 0 else SOIL_L
            if (x * 7 + y * 13) % 11 == 0:
                c = SOIL_M
            if (x * 5 + y * 3) % 17 == 0:
                c = SOIL_L if c == SOIL_D else SOIL_D
            px(img, x, y, c)
    # Furrow rows — OUTLINE #6B4A2E ticks match plot empty tile
    for y in (6, 12, 18, 24):
        if not (y0 <= y <= y1):
            continue
        for x in range(max(x0, 0), min(x1, 31) + 1):
            px(img, x, y, OUTLINE if x % 4 == 0 else SOIL_D)
            if y + 1 <= y1:
                px(img, x, y + 1, SOIL_L)
    stones = [
        (5, 8), (6, 8), (14, 5), (15, 5), (23, 10), (24, 10),
        (9, 16), (10, 16), (18, 15), (19, 15), (4, 22), (5, 22),
        (26, 20), (27, 20), (12, 26), (13, 26), (21, 27), (16, 20),
    ]
    for i, (x, y) in enumerate(stones):
        if x0 <= x <= x1 and y0 <= y <= y1:
            px(img, x, y, STONE if i % 3 else STONE_L)
    for x, y in [(8, 7), (17, 13), (25, 19), (11, 25), (20, 7), (7, 19)]:
        if x0 <= x <= x1 and y0 <= y <= y1:
            px(img, x, y, SAND)


def tile_dirt() -> Image.Image:
    """Tilled warm mud patch for crop beds (shared mud tokens with plot tiles)."""
    img = new_img()
    fill_warm_mud(img)
    return img


def _blend_edge(dirt: Image.Image, grass: Image.Image, side: str) -> Image.Image:
    """Dirt tile with grass bleeding in from one side (edge transition)."""
    img = dirt.copy()
    g = grass
    # width of grass fringe
    fringe = 8
    for y in range(32):
        for x in range(32):
            if side == "n":
                depth = y
            elif side == "s":
                depth = 31 - y
            elif side == "w":
                depth = x
            else:  # e
                depth = 31 - x
            if depth >= fringe:
                continue
            # noise so edge isn't a hard bar
            jitter = (x * 5 + y * 9) % 5
            if depth + jitter // 2 < fringe - 1:
                px(img, x, y, g.getpixel((x, y)))
            elif depth + jitter // 2 == fringe - 1:
                # mixed soil/grass crumbs
                px(img, x, y, SOIL_M if (x + y) % 2 else SAGE)
    return img


def tile_dirt_edge(side: str) -> Image.Image:
    return _blend_edge(tile_dirt(), tile_grass(), side)


def tile_dirt_edge_generic() -> Image.Image:
    """Single generic dirt↔grass edge (usable rotated / mirrored)."""
    return tile_dirt_edge("n")


def tile_path() -> Image.Image:
    """Warm sandy path with stone chips."""
    img = new_img()
    for y in range(32):
        for x in range(32):
            n = (x * 2 + y * 3) % 5
            c = PATH if n < 3 else PATH_D
            if (x + y) % 7 == 0:
                c = SAND
            px(img, x, y, c)
    # side borders suggesting packed earth
    for y in range(32):
        px(img, 0, y, SOIL_D)
        px(img, 1, y, WOOD_D if y % 4 == 0 else SOIL_M)
        px(img, 31, y, SOIL_D)
        px(img, 30, y, WOOD_D if y % 4 == 2 else SOIL_M)
    stones = [(6, 8), (7, 8), (14, 14), (15, 14), (14, 15),
              (22, 6), (23, 6), (10, 22), (11, 22), (25, 20), (26, 20)]
    for i, (x, y) in enumerate(stones):
        px(img, x, y, STONE if i % 2 else STONE_L)
    for x, y in [(9, 5), (18, 18), (12, 28), (20, 12)]:
        px(img, x, y, CREAM)
    return img


# --- Fences -----------------------------------------------------------------

def fence_h() -> Image.Image:
    """Horizontal fence rail (transparent bg)."""
    img = new_img()
    # posts
    for px_x in (4, 16, 27):
        fill_rect(img, px_x, 10, px_x + 2, 28, WOOD)
        fill_rect(img, px_x, 10, px_x + 2, 12, WOOD_L)
        vline(img, px_x - 1, 10, 28, OUTLINE)
        vline(img, px_x + 3, 10, 28, OUTLINE)
        hline(img, px_x - 1, px_x + 3, 9, OUTLINE)
        hline(img, px_x - 1, px_x + 3, 29, OUTLINE)
        px(img, px_x + 1, 27, WOOD_D)
    # rails
    for y0, y1 in ((14, 16), (22, 24)):
        fill_rect(img, 2, y0, 29, y1, WOOD_L)
        hline(img, 2, 29, y0 - 1, OUTLINE)
        hline(img, 2, 29, y1 + 1, OUTLINE)
        for x in range(3, 29, 4):
            px(img, x, y0 + 1, WOOD_D)
    return img


def fence_v() -> Image.Image:
    """Vertical fence rail (transparent bg)."""
    img = new_img()
    # posts stacked
    for py in (2, 14, 24):
        fill_rect(img, 13, py, 18, py + 8, WOOD)
        fill_rect(img, 13, py, 18, py + 1, WOOD_L)
        rect_outline(img, 13, py, 18, py + 8, OUTLINE)
        px(img, 15, py + 6, WOOD_D)
    # side rails
    for x0, x1 in ((8, 12), (19, 23)):
        fill_rect(img, x0, 6, x1, 26, WOOD_L)
        vline(img, x0 - 1, 6, 26, OUTLINE)
        vline(img, x1 + 1, 6, 26, OUTLINE)
        for y in range(8, 26, 5):
            px(img, x0 + 1, y, WOOD_D)
    return img


def fence_corner(kind: str) -> Image.Image:
    """Corner post with two rail stubs. kind: nw/ne/sw/se."""
    img = new_img()
    # thick corner post
    fill_rect(img, 12, 10, 19, 28, WOOD)
    fill_rect(img, 12, 10, 19, 13, WOOD_L)
    rect_outline(img, 12, 10, 19, 28, OUTLINE)
    px(img, 15, 26, WOOD_D)
    px(img, 16, 25, WOOD_D)
    # cap
    fill_rect(img, 11, 8, 20, 10, WOOD_D)
    rect_outline(img, 11, 8, 20, 10, OUTLINE)

    def rail_h(x0, x1, y0=15):
        fill_rect(img, x0, y0, x1, y0 + 2, WOOD_L)
        hline(img, x0, x1, y0 - 1, OUTLINE)
        hline(img, x0, x1, y0 + 3, OUTLINE)
        fill_rect(img, x0, y0 + 7, x1, y0 + 9, WOOD_L)
        hline(img, x0, x1, y0 + 6, OUTLINE)
        hline(img, x0, x1, y0 + 10, OUTLINE)

    def rail_v(y0, y1, x0=14):
        fill_rect(img, x0, y0, x0 + 3, y1, WOOD_L)
        vline(img, x0 - 1, y0, y1, OUTLINE)
        vline(img, x0 + 4, y0, y1, OUTLINE)

    if kind in ("nw", "ne", "sw", "se"):
        pass
    if kind in ("nw", "sw"):
        rail_h(1, 12)
    if kind in ("ne", "se"):
        rail_h(19, 30)
    if kind in ("nw", "ne"):
        rail_v(1, 10)
    if kind in ("sw", "se"):
        rail_v(28, 31)
    return img


# --- Pet 小芽 (sprout pig) --------------------------------------------------

def _draw_sprout(img, cx: int, cy: int) -> None:
    """Tiny sprout on head."""
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


def _draw_pig_body(img, happy: bool) -> None:
    """Centered top-down-ish cute pig facing slightly down."""
    # body oval
    fill_rect(img, 8, 12, 23, 26, PINK)
    fill_rect(img, 10, 10, 21, 12, PINK)
    fill_rect(img, 10, 26, 21, 28, PINK)
    fill_rect(img, 6, 14, 8, 24, PINK_D)
    fill_rect(img, 23, 14, 25, 24, PINK_D)
    # belly highlight
    fill_rect(img, 12, 16, 19, 24, PINK)
    fill_rect(img, 13, 18, 18, 23, CREAM)
    # outline body
    hline(img, 10, 21, 9, OUTLINE)
    hline(img, 10, 21, 29, OUTLINE)
    vline(img, 5, 14, 24, OUTLINE)
    vline(img, 26, 14, 24, OUTLINE)
    px(img, 6, 12, OUTLINE); px(img, 7, 11, OUTLINE); px(img, 8, 10, OUTLINE)
    px(img, 25, 12, OUTLINE); px(img, 24, 11, OUTLINE); px(img, 23, 10, OUTLINE)
    px(img, 6, 26, OUTLINE); px(img, 7, 27, OUTLINE); px(img, 8, 28, OUTLINE)
    px(img, 25, 26, OUTLINE); px(img, 24, 27, OUTLINE); px(img, 23, 28, OUTLINE)
    # ears
    fill_rect(img, 7, 8, 11, 12, PINK_D)
    fill_rect(img, 20, 8, 24, 12, PINK_D)
    fill_rect(img, 8, 9, 10, 11, PINK)
    fill_rect(img, 21, 9, 23, 11, PINK)
    rect_outline(img, 7, 8, 11, 12, OUTLINE)
    rect_outline(img, 20, 8, 24, 12, OUTLINE)
    # snout
    fill_rect(img, 12, 20, 19, 25, SNORT)
    fill_rect(img, 13, 19, 18, 19, SNORT)
    rect_outline(img, 12, 19, 19, 25, OUTLINE)
    # nostrils
    px(img, 14, 22, PINK_DK)
    px(img, 17, 22, PINK_DK)
    px(img, 14, 23, OUTLINE)
    px(img, 17, 23, OUTLINE)
    # eyes
    if happy:
        # closed happy curves
        px(img, 11, 15, OUTLINE); px(img, 12, 14, OUTLINE); px(img, 13, 15, OUTLINE)
        px(img, 18, 15, OUTLINE); px(img, 19, 14, OUTLINE); px(img, 20, 15, OUTLINE)
        # blush
        px(img, 9, 18, HEART_L); px(img, 10, 18, HEART_L)
        px(img, 21, 18, HEART_L); px(img, 22, 18, HEART_L)
    else:
        fill_rect(img, 11, 14, 12, 16, NEAR_BLACK)
        fill_rect(img, 19, 14, 20, 16, NEAR_BLACK)
        px(img, 11, 14, WHITE)
        px(img, 19, 14, WHITE)
    # tiny feet
    for fx in (9, 14, 18, 22):
        fill_rect(img, fx, 28, fx + 2, 30, PINK_D)
        hline(img, fx, fx + 2, 31, OUTLINE)
        vline(img, fx - 1, 28, 30, OUTLINE)
        vline(img, fx + 3, 28, 30, OUTLINE)
    # sprout on head
    _draw_sprout(img, 15, 3)


def pet_idle() -> Image.Image:
    img = new_img()
    _draw_pig_body(img, happy=False)
    return img


def pet_happy() -> Image.Image:
    img = new_img()
    _draw_pig_body(img, happy=True)
    # small bounce offset: shift sprout glitter
    px(img, 18, 2, GOLD)
    px(img, 12, 4, GOLD)
    return img


def fx_heart() -> Image.Image:
    """Interaction heart VFX (transparent bg)."""
    img = new_img()
    # classic pixel heart centered
    fill_rect(img, 8, 10, 14, 16, HEART)
    fill_rect(img, 17, 10, 23, 16, HEART)
    fill_rect(img, 10, 8, 13, 9, HEART)
    fill_rect(img, 18, 8, 21, 9, HEART)
    fill_rect(img, 10, 17, 21, 20, HEART)
    fill_rect(img, 12, 21, 19, 23, HEART)
    fill_rect(img, 14, 24, 17, 25, HEART)
    # highlight
    fill_rect(img, 10, 10, 12, 12, HEART_L)
    px(img, 11, 9, WHITE)
    # outline
    hline(img, 10, 13, 7, OUTLINE)
    hline(img, 18, 21, 7, OUTLINE)
    vline(img, 7, 10, 16, OUTLINE)
    vline(img, 24, 10, 16, OUTLINE)
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
    return img


# --- Decor: hut + trees (Minecraft-inspired readability, 2D top-down) -----
# Big flat color blocks, clear dark outlines, few mid-tones.
# Still 2D overhead farm sprites — NOT a 3D voxel/block world.

def _outline_blob(img, filled_coords, outline_c=DECOR_OUTLINE) -> None:
    """Draw a 1px dark outline around an opaque silhouette."""
    pts = set(filled_coords)
    for x, y in pts:
        for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            nx, ny = x + dx, y + dy
            if (nx, ny) not in pts:
                px(img, nx, ny, outline_c)


def building_hut() -> Image.Image:
    """Blocky warm hut, 48×48 — readable 2D top-down (roof + front), MC contrast."""
    img = new_img(48, 48)
    # hard pixel shadow (no soft gradient)
    for y in range(40, 46):
        for x in range(10, 38):
            if abs(x - 24) + abs(y - 42) * 2 <= 16:
                px(img, x, y, (0x3D, 0x2A, 0x18, 120))

    # --- walls: big cream block ---
    fill_rect(img, 10, 26, 37, 41, WALL)
    # single shade strip (not mid-gray wash)
    fill_rect(img, 10, 26, 11, 41, WALL_D)
    fill_rect(img, 36, 26, 37, 41, WALL_D)
    # wood baseboard — thick
    fill_rect(img, 10, 39, 37, 41, WOOD)
    hline(img, 10, 37, 38, WOOD_D)
    # door — solid wood block
    fill_rect(img, 20, 30, 27, 41, WOOD_D)
    fill_rect(img, 21, 31, 26, 40, WOOD)
    px(img, 25, 35, GOLD)
    rect_outline(img, 20, 30, 27, 41, DECOR_OUTLINE)
    # windows — flat sky panes, thick cross
    fill_rect(img, 12, 30, 17, 35, SKY)
    rect_outline(img, 12, 30, 17, 35, DECOR_OUTLINE)
    hline(img, 12, 17, 32, DECOR_OUTLINE)
    vline(img, 14, 30, 35, DECOR_OUTLINE)
    fill_rect(img, 30, 30, 35, 35, SKY)
    rect_outline(img, 30, 30, 35, 35, DECOR_OUTLINE)
    hline(img, 30, 35, 32, DECOR_OUTLINE)
    vline(img, 32, 30, 35, DECOR_OUTLINE)
    rect_outline(img, 10, 26, 37, 41, DECOR_OUTLINE)

    # --- pitched roof: large flat brick-red slabs (few tones) ---
    # upper triangle — solid ROOF / ROOF_D bands only
    for row in range(0, 18):
        half = 2 + row
        y = 7 + row
        x0 = 24 - half
        x1 = 24 + half
        c = ROOF_L if row < 4 else (ROOF if (row // 3) % 2 == 0 else ROOF_D)
        fill_rect(img, x0, y, x1, y, c)
        px(img, x0 - 1, y, DECOR_OUTLINE)
        px(img, x1 + 1, y, DECOR_OUTLINE)
    # eaves overhang — one dark band
    for row in range(5):
        half = 15 + row
        y = 25 + row
        x0 = max(5, 24 - half)
        x1 = min(42, 24 + half)
        c = ROOF if row < 2 else ROOF_D
        fill_rect(img, x0, y, x1, y, c)
        px(img, x0 - 1, y, DECOR_OUTLINE)
        px(img, x1 + 1, y, DECOR_OUTLINE)
    # ridge
    px(img, 24, 6, DECOR_OUTLINE)
    fill_rect(img, 23, 7, 25, 8, ROOF_L)
    # eave beam
    fill_rect(img, 8, 29, 39, 30, WOOD_D)
    hline(img, 7, 40, 28, DECOR_OUTLINE)
    hline(img, 7, 40, 31, DECOR_OUTLINE)

    # chimney — chunky stone block
    fill_rect(img, 31, 9, 36, 18, STONE)
    fill_rect(img, 31, 9, 36, 11, STONE_L)
    fill_rect(img, 32, 12, 35, 17, WOOD_D)
    rect_outline(img, 31, 9, 36, 18, DECOR_OUTLINE)
    # blocky smoke (no soft puffs)
    fill_rect(img, 32, 5, 34, 7, STONE_L)
    px(img, 33, 4, STONE_L)
    return img


def tree_oak() -> Image.Image:
    """Chunky round oak, 32×40 — big canopy blocks + dark rim, 2D top-down readable."""
    img = new_img(32, 40)
    # hard shadow under trunk
    fill_rect(img, 10, 35, 21, 38, (0x3D, 0x2A, 0x18, 110))
    hline(img, 12, 19, 34, (0x3D, 0x2A, 0x18, 90))

    # trunk — thick wood block
    fill_rect(img, 13, 24, 18, 36, WOOD)
    fill_rect(img, 13, 24, 14, 36, WOOD_D)
    rect_outline(img, 13, 24, 18, 36, DECOR_OUTLINE)

    # canopy: few large rect/blob regions (MGREEN / SAGE / DGREEN only)
    filled = []

    def put_leaf(x, y, c):
        px(img, x, y, c)
        filled.append((x, y))

    def blob(cx, cy, rx, ry, c):
        for y in range(cy - ry, cy + ry + 1):
            for x in range(cx - rx, cx + rx + 1):
                if ((x - cx) / max(rx, 1)) ** 2 + ((y - cy) / max(ry, 1)) ** 2 <= 1.05:
                    put_leaf(x, y, c)

    # base dark mass
    blob(16, 16, 11, 10, MGREEN)
    # side blocks
    blob(8, 17, 6, 6, DGREEN)
    blob(24, 17, 6, 6, DGREEN)
    # top highlight block (one light tone only)
    blob(16, 10, 7, 6, SAGE)
    # tiny LGREEN patch — not speckled
    fill_rect(img, 13, 8, 18, 11, LGREEN)
    for y in range(8, 12):
        for x in range(13, 19):
            filled.append((x, y))
    # clear dark outline around canopy
    _outline_blob(img, filled, DECOR_OUTLINE)
    return img


def tree_pine() -> Image.Image:
    """Layered pine, 32×40 — flat tier triangles, thick outline, few greens."""
    img = new_img(32, 40)
    fill_rect(img, 11, 35, 20, 38, (0x3D, 0x2A, 0x18, 110))

    # trunk
    fill_rect(img, 14, 29, 17, 37, WOOD_D)
    fill_rect(img, 15, 29, 16, 37, WOOD)
    rect_outline(img, 14, 29, 17, 37, DECOR_OUTLINE)

    filled = []

    def put(x, y, c):
        px(img, x, y, c)
        filled.append((x, y))

    # three chunky tiers (wide → narrow), 2 colors each
    tiers = [
        # (top_y, rows, max_half, fill, shade)
        (20, 10, 11, MGREEN, DGREEN),
        (12, 10, 8, SAGE, MGREEN),
        (4, 9, 5, LGREEN, SAGE),
    ]
    for top, rows, max_half, fill_c, shade_c in tiers:
        for i in range(rows):
            # step half every 2 rows → blocky silhouette
            half = max_half - (i // 2)
            if half < 1:
                half = 1
            y = top + i
            c = fill_c if i < rows // 2 else shade_c
            for x in range(16 - half, 16 + half + 1):
                put(x, y, c)
    # tip block
    put(16, 2, LGREEN)
    put(15, 3, LGREEN)
    put(16, 3, LGREEN)
    put(17, 3, LGREEN)
    put(16, 1, DECOR_OUTLINE)
    _outline_blob(img, filled, DECOR_OUTLINE)
    return img


def bush() -> Image.Image:
    """Small chunky bush, 24×20 — big green blocks + dark rim."""
    img = new_img(24, 20)
    fill_rect(img, 5, 16, 18, 18, (0x3D, 0x2A, 0x18, 100))

    filled = []

    def put(x, y, c):
        px(img, x, y, c)
        filled.append((x, y))

    def blob(cx, cy, rx, ry, c):
        for y in range(cy - ry, cy + ry + 1):
            for x in range(cx - rx, cx + rx + 1):
                if ((x - cx) / max(rx, 1)) ** 2 + ((y - cy) / max(ry, 1)) ** 2 <= 1.0:
                    put(x, y, c)

    blob(12, 10, 9, 7, SAGE)
    blob(6, 11, 5, 5, DGREEN)
    blob(18, 11, 5, 5, MGREEN)
    # one highlight block
    fill_rect(img, 9, 5, 14, 8, LGREEN)
    for y in range(5, 9):
        for x in range(9, 15):
            filled.append((x, y))
    _outline_blob(img, filled, DECOR_OUTLINE)
    return img


def tree_shadow() -> Image.Image:
    """Hard pixel ellipse shadow, 32×16 — stepped alpha, no soft blur."""
    img = new_img(32, 16)
    for y in range(16):
        for x in range(32):
            nx = abs(x - 15.5) / 14.0
            ny = abs(y - 7.5) / 5.0
            d = nx * nx + ny * ny
            if d <= 0.45:
                a = 130
            elif d <= 0.75:
                a = 90
            elif d <= 1.0:
                a = 55
            else:
                continue
            px(img, x, y, (0x3D, 0x2A, 0x18, a))
    return img



# --- Stage ground differentials (barren → thrive) ---------------------------

def tile_grass_barren() -> Image.Image:
    """Mostly warm mud with sparse sage patches — 荒地 feel."""
    img = new_img()
    fill_warm_mud(img)
    # sparse grass islands (not a full lawn)
    patches = [
        (3, 4, 5, 3), (18, 6, 4, 3), (8, 20, 5, 3),
        (22, 22, 4, 3), (12, 12, 3, 2), (26, 3, 3, 2),
    ]
    for x0, y0, w, h in patches:
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                if (x * 3 + y * 5) % 4 == 0:
                    continue  # leave mud holes
                n = (x + y) % 3
                c = SAGE if n == 0 else (LGREEN if n == 1 else MGREEN)
                px(img, x, y, c)
    # cracked / dry ticks
    for x, y in [(10, 9), (11, 9), (20, 16), (21, 16), (6, 27), (15, 3)]:
        px(img, x, y, OUTLINE)
    return img


def tile_grass_sprout() -> Image.Image:
    """Mud + strong grass fringe feel — 萌芽田格感 (edge-heavy lawn)."""
    img = tile_grass()
    # strengthen plot-grid feel: lighter furrow-like lines
    for y in (0, 31):
        for x in range(32):
            if x % 3 != 1:
                px(img, x, y, MGREEN if y == 0 else DGREEN)
    for x in (0, 31):
        for y in range(32):
            if y % 3 != 1:
                px(img, x, y, MGREEN if x == 0 else DGREEN)
    # mud corner crumbs so beds feel tilled nearby
    for x, y in [(2, 2), (3, 2), (28, 28), (29, 29), (2, 29), (29, 2)]:
        px(img, x, y, SOIL_M)
    return img


def tile_grass_home() -> Image.Image:
    """Settled lawn — same as base grass with a few cream flower dots."""
    img = tile_grass()
    for x, y in [(6, 12), (15, 5), (24, 18), (11, 24), (20, 10)]:
        px(img, x, y, CREAM)
        if y > 0:
            px(img, x, y - 1, GOLD if (x + y) % 2 else PINK)
    return img


def tile_grass_thrive() -> Image.Image:
    """Denser / greener canopy lawn — 旺場."""
    img = new_img()
    for y in range(32):
        for x in range(32):
            n = (x * 3 + y * 5) % 7
            if n <= 1:
                c = MGREEN
            elif n <= 3:
                c = SAGE
            elif n == 4:
                c = DGREEN
            else:
                c = LGREEN if ((x + y) % 2 == 0) else SAGE
            if (y // 3) % 2 == 0 and n == 5:
                c = DGREEN
            px(img, x, y, c)
    blades = [
        (3, 5, DGREEN), (4, 4, LGREEN), (11, 9, DGREEN), (12, 8, SAGE),
        (19, 3, DGREEN), (20, 2, LGREEN), (6, 17, DGREEN), (7, 16, SAGE),
        (15, 21, DGREEN), (16, 20, LGREEN), (24, 13, DGREEN), (25, 12, SAGE),
        (27, 25, DGREEN), (9, 27, LGREEN), (21, 27, DGREEN), (2, 23, SAGE),
        (14, 14, DGREEN), (28, 8, SAGE), (8, 11, LGREEN), (17, 6, DGREEN),
    ]
    for x, y, c in blades:
        px(img, x, y, c)
        if y > 0:
            px(img, x, y - 1, LGREEN if c == DGREEN else SAGE)
    for x, y in [(8, 7), (17, 15), (26, 6), (13, 26), (4, 13), (22, 22)]:
        px(img, x, y, SAND)
    return img


def building_hut_ruin() -> Image.Image:
    """Broken hut stub 48×48 — 荒地殘破感 (optional decor)."""
    img = new_img(48, 48)
    # rubble shadow
    for y in range(38, 44):
        for x in range(12, 36):
            if abs(x - 24) + abs(y - 40) * 2 <= 14:
                px(img, x, y, (0x3D, 0x2A, 0x18, 100))
    # crumbled wall stubs
    fill_rect(img, 14, 30, 33, 40, WALL_D)
    fill_rect(img, 14, 30, 15, 40, WOOD_D)
    fill_rect(img, 32, 30, 33, 40, WOOD_D)
    fill_rect(img, 14, 38, 33, 40, WOOD)
    # broken roof remnant (asymmetric)
    for row in range(0, 10):
        half = 1 + row
        y = 18 + row
        x0 = 24 - half
        x1 = 20 + half  # collapsed right side
        if x1 < x0:
            continue
        c = ROOF_D if row > 5 else ROOF
        fill_rect(img, x0, y, min(x1, 30), y, c)
        px(img, x0 - 1, y, DECOR_OUTLINE)
    # scattered bricks / stones
    for x, y in [(18, 36), (19, 36), (28, 34), (29, 35), (22, 41), (12, 39)]:
        px(img, x, y, STONE if (x + y) % 2 else BRICK)
    rect_outline(img, 14, 30, 33, 40, DECOR_OUTLINE)
    # gap in wall (ruin hole)
    fill_rect(img, 22, 32, 26, 37, SOIL_M)
    return img


def _paste(canvas, sprite, xy):
    canvas.paste(sprite, xy, sprite)


def stage_scene(stage: str) -> Image.Image:
    """160×96 full scene matching FarmSceneLayer stage gating."""
    W, H = 160, 96
    canvas = Image.new("RGBA", (W, H), SKY)
    dirt = tile_dirt()
    path = tile_path()
    edge_n = tile_dirt_edge("n")
    edge_w = tile_dirt_edge("w")
    edge_e = tile_dirt_edge("e")
    fh = fence_h()
    fv = fence_v()
    fc_nw, fc_ne = fence_corner("nw"), fence_corner("ne")
    fc_sw, fc_se = fence_corner("sw"), fence_corner("se")
    hut = building_hut()
    ruin = building_hut_ruin()
    oak, pine = tree_oak(), tree_pine()
    bush_img = bush()
    shadow = tree_shadow()
    pet = pet_idle()
    pet2 = pet_happy()

    grass_map = {
        "barren": tile_grass_barren(),
        "sprout": tile_grass_sprout(),
        "home": tile_grass_home(),
        "thrive": tile_grass_thrive(),
    }
    ground = grass_map[stage]

    # Layer 0 — ground
    if stage == "barren":
        for ty in range(3):
            for tx in range(5):
                # mostly dirt; sprinkle barren grass on corners
                if (tx, ty) in ((0, 0), (4, 0), (0, 2), (4, 2)):
                    _paste(canvas, ground, (tx * 32, ty * 32))
                else:
                    _paste(canvas, dirt, (tx * 32, ty * 32))
    else:
        for ty in range(3):
            for tx in range(5):
                _paste(canvas, ground, (tx * 32, ty * 32))

    _paste(canvas, path, (64, 0))
    _paste(canvas, path, (64, 64))
    _paste(canvas, edge_w, (32, 32))
    _paste(canvas, dirt, (64, 32))
    _paste(canvas, edge_e, (96, 32))
    if stage != "barren":
        for tx in (1, 2, 3):
            for y in range(8):
                for x in range(32):
                    p = edge_n.getpixel((x, y))
                    if p[3] > 0:
                        canvas.putpixel((tx * 32 + x, 32 + y), p)

    # Fence — home+
    if stage in ("home", "thrive"):
        _paste(canvas, fc_nw, (20, 18))
        _paste(canvas, fh, (48, 18))
        _paste(canvas, fh, (80, 18))
        _paste(canvas, fc_ne, (108, 18))
        _paste(canvas, fv, (20, 34))
        _paste(canvas, fv, (116, 34))
        _paste(canvas, fc_sw, (20, 50))
        _paste(canvas, fh, (48, 54))
        _paste(canvas, fh, (80, 54))
        _paste(canvas, fc_se, (108, 50))

    # Trees / hut
    if stage == "barren":
        _paste(canvas, ruin, (2, 48))
    if stage in ("home", "thrive"):
        _paste(canvas, shadow, (-4, 28))
        _paste(canvas, pine, (-4, 0))
        _paste(canvas, hut, (2, 48))
    if stage == "thrive":
        _paste(canvas, shadow, (128, 24))
        _paste(canvas, oak, (128, -4))
        _paste(canvas, bush_img, (4, 4))
        _paste(canvas, bush_img, (100, 8))
        _paste(canvas, bush_img, (118, 70))
    elif stage == "home":
        _paste(canvas, bush_img, (118, 70))

    # Pets — sprout+ 1; thrive animal2 hint at day21 shown as thrive with 2
    if stage in ("sprout", "home", "thrive"):
        _paste(canvas, pet if stage != "thrive" else pet2, (64, 62))
    if stage == "thrive":
        # second animal slot placeholder (slightly smaller feel via crop offset)
        _paste(canvas, pet, (100, 66))

    return canvas


def farm_stages_preview() -> Image.Image:
    """2×2 comparison: barren / sprout / home / thrive — docs review."""
    labels = [("barren", "荒地"), ("sprout", "萌芽"), ("home", "安家"), ("thrive", "旺場")]
    cell_w, cell_h = 160, 96
    pad = 8
    label_h = 14
    cols, rows = 2, 2
    W = cols * cell_w + (cols + 1) * pad
    H = rows * (cell_h + label_h) + (rows + 1) * pad
    out = Image.new("RGBA", (W, H), CREAM)
    for i, (key, zh) in enumerate(labels):
        col, row = i % 2, i // 2
        x = pad + col * (cell_w + pad)
        y = pad + row * (cell_h + label_h + pad)
        scene = stage_scene(key)
        out.paste(scene, (x, y + label_h), scene)
        # tiny pixel label bar
        fill_rect(out, x, y, x + cell_w - 1, y + label_h - 1, SAND)
        # draw simple 5x7-ish text as colored blocks spelling via pattern — use outline ticks
        # instead: colored stage stripe
        colors = {
            "barren": SOIL_D,
            "sprout": SAGE,
            "home": WOOD,
            "thrive": MGREEN,
        }
        fill_rect(out, x, y, x + 6, y + label_h - 1, colors[key])
        rect_outline(out, x, y, x + cell_w - 1, y + label_h - 1, OUTLINE)
        # encode stage id as pixel Morse-like dots for review (readable in docs caption)
        for j, ch in enumerate(key[:6]):
            px(out, x + 10 + j * 4, y + 4, OUTLINE)
            px(out, x + 10 + j * 4, y + 5, colors[key])
            px(out, x + 11 + j * 4, y + 4, colors[key])
    return out



# --- Composite preview ------------------------------------------------------

def farm_scene_preview() -> Image.Image:
    """160×96 overhead scene for design review (nearest-neighbor paste)."""
    W, H = 160, 96
    canvas = Image.new("RGBA", (W, H), SKY)
    grass = tile_grass()
    dirt = tile_dirt()
    path = tile_path()
    edge_n = tile_dirt_edge("n")
    edge_w = tile_dirt_edge("w")
    edge_e = tile_dirt_edge("e")
    fh = fence_h()
    fv = fence_v()
    fc_nw = fence_corner("nw")
    fc_ne = fence_corner("ne")
    fc_sw = fence_corner("sw")
    fc_se = fence_corner("se")
    pet = pet_happy()
    heart = fx_heart()
    hut = building_hut()
    oak = tree_oak()
    pine = tree_pine()
    bush_img = bush()

    # Layer 0 — ground grid (5×3)
    for ty in range(3):
        for tx in range(5):
            canvas.paste(grass, (tx * 32, ty * 32), grass)
    canvas.paste(path, (64, 0), path)
    canvas.paste(path, (64, 64), path)
    canvas.paste(edge_w, (32, 32), edge_w)
    canvas.paste(dirt, (64, 32), dirt)
    canvas.paste(edge_e, (96, 32), edge_e)
    for tx in (1, 2, 3):
        for y in range(8):
            for x in range(32):
                p = edge_n.getpixel((x, y))
                if p[3] > 0:
                    canvas.putpixel((tx * 32 + x, 32 + y), p)

    # Layer 1 — fence around dirt bed
    canvas.paste(fc_nw, (20, 18), fc_nw)
    canvas.paste(fh, (48, 18), fh)
    canvas.paste(fh, (80, 18), fh)
    canvas.paste(fc_ne, (108, 18), fc_ne)
    canvas.paste(fv, (20, 34), fv)
    canvas.paste(fv, (116, 34), fv)
    canvas.paste(fc_sw, (20, 50), fc_sw)
    canvas.paste(fh, (48, 54), fh)
    canvas.paste(fh, (80, 54), fh)
    canvas.paste(fc_se, (108, 50), fc_se)

    # Layer 2 — decor (hut + trees / bush)
    canvas.paste(pine, (-4, 0), pine)
    canvas.paste(oak, (128, -4), oak)
    canvas.paste(bush_img, (118, 70), bush_img)
    canvas.paste(hut, (2, 48), hut)

    # Layer 3 — pet sprite + heart FX
    canvas.paste(pet, (64, 62), pet)
    canvas.paste(heart, (92, 50), heart)
    return canvas


GENERATORS_32 = {
    "tile_grass.png": tile_grass,
    "tile_grass_barren.png": tile_grass_barren,
    "tile_grass_sprout.png": tile_grass_sprout,
    "tile_grass_home.png": tile_grass_home,
    "tile_grass_thrive.png": tile_grass_thrive,
    "tile_dirt.png": tile_dirt,
    "tile_dirt_edge_n.png": lambda: tile_dirt_edge("n"),
    "tile_dirt_edge_s.png": lambda: tile_dirt_edge("s"),
    "tile_dirt_edge_e.png": lambda: tile_dirt_edge("e"),
    "tile_dirt_edge_w.png": lambda: tile_dirt_edge("w"),
    "tile_dirt_edge.png": tile_dirt_edge_generic,
    "tile_path.png": tile_path,
    "fence_h.png": fence_h,
    "fence_v.png": fence_v,
    "fence_corner_nw.png": lambda: fence_corner("nw"),
    "fence_corner_ne.png": lambda: fence_corner("ne"),
    "fence_corner_sw.png": lambda: fence_corner("sw"),
    "fence_corner_se.png": lambda: fence_corner("se"),
    "pet_idle.png": pet_idle,
    "pet_happy.png": pet_happy,
    "fx_heart.png": fx_heart,
}

# Decor sprites (variable size; still PIL nearest-neighbor)
GENERATORS_DECOR = {
    "building_hut.png": building_hut,       # 48×48
    "building_hut_ruin.png": building_hut_ruin,  # 48×48 barren stub
    "tree_oak.png": tree_oak,               # 32×40
    "tree_pine.png": tree_pine,             # 32×40
    "bush.png": bush,                       # 24×20
    "tree_shadow.png": tree_shadow,         # 32×16
}

GENERATORS_STAGE_SCENES = {
    "stage_barren_preview.png": lambda: stage_scene("barren"),
    "stage_sprout_preview.png": lambda: stage_scene("sprout"),
    "stage_home_preview.png": lambda: stage_scene("home"),
    "stage_thrive_preview.png": lambda: stage_scene("thrive"),
}


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    PREVIEW.mkdir(parents=True, exist_ok=True)
    DOCS_PREVIEW.mkdir(parents=True, exist_ok=True)

    written = []
    for name, gen in GENERATORS_32.items():
        img = gen()
        assert img.size == (32, 32), f"{name} size {img.size}"
        path = OUT / name
        img.save(path, "PNG")
        img.resize((256, 256), Image.NEAREST).save(PREVIEW / name)
        written.append(name)
        print(f"wrote {name}")

    for name, gen in GENERATORS_DECOR.items():
        img = gen()
        path = OUT / name
        img.save(path, "PNG")
        # preview: scale ~8× keeping aspect
        scale = max(1, 256 // max(img.size))
        img.resize((img.size[0] * scale, img.size[1] * scale), Image.NEAREST).save(
            PREVIEW / name
        )
        written.append(name)
        print(f"wrote {name} {img.size[0]}x{img.size[1]}")

    preview = farm_scene_preview()
    assert preview.size == (160, 96)
    preview_path = OUT / "farm_scene_preview.png"
    preview.save(preview_path, "PNG")
    preview.resize((640, 384), Image.NEAREST).save(PREVIEW / "farm_scene_preview.png")
    # also keep a copy under docs for easy review
    preview.save(DOCS_PREVIEW / "farm_scene_preview.png", "PNG")
    written.append("farm_scene_preview.png")
    print("wrote farm_scene_preview.png (160x96)")

    for name, gen in GENERATORS_STAGE_SCENES.items():
        img = gen()
        assert img.size == (160, 96), f"{name} size {img.size}"
        img.save(OUT / name, "PNG")
        img.resize((640, 384), Image.NEAREST).save(PREVIEW / name)
        img.save(DOCS_PREVIEW / name, "PNG")
        written.append(name)
        print(f"wrote {name} (160x96)")

    stages = farm_stages_preview()
    stages.save(OUT / "farm_stages_preview.png", "PNG")
    stages.resize((stages.size[0] * 4, stages.size[1] * 4), Image.NEAREST).save(
        PREVIEW / "farm_stages_preview.png"
    )
    stages.save(DOCS_PREVIEW / "farm_stages_preview.png", "PNG")
    written.append("farm_stages_preview.png")
    print(f"wrote farm_stages_preview.png {stages.size[0]}x{stages.size[1]}")
    print(f"total: {len(written)}")


if __name__ == "__main__":
    main()
