#!/usr/bin/env python3
"""M1 minimum art refresh: terrain/fence/barren + HUD + phase overlays.

Self-drawn 32px warm pixel, deep outlines (#6B4A2E / #3D2A18), nearest-neighbor.
Does not touch reward / settle formulas — assets + docs only.

  /tmp/pilvenv/bin/python scripts/gen_m1_art_assets.py
"""
from __future__ import annotations

import importlib.util
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "app/src/main/res/drawable-nodpi"
PREVIEW = ROOT / "scripts/preview_assets"

# Phase tint ARGB (must match DayPhaseLogic.phaseTintArgb)
PHASE_TINTS = {
    "dawn": (0xFF, 0xD2, 0x7A, 0x33),   # MORNING 0x33FFD27A
    "day": (0x00, 0x00, 0x00, 0x00),    # NOON transparent
    "dusk": (0xC4, 0x6B, 0x3A, 0x44),   # EVENING 0x44C46B3A
    "night": (0x20, 0x2A, 0x5A, 0x66),  # NIGHT 0x66202A5A
}

OUTLINE = (0x6B, 0x4A, 0x2E, 255)
DECOR_OUTLINE = (0x3D, 0x2A, 0x18, 255)
GOLD = (0xE8, 0xA8, 0x38, 255)
GOLD_L = (0xF4, 0xD0, 0x78, 255)
CREAM = (0xFF, 0xF8, 0xE7, 255)
SKY = (0xA8, 0xD4, 0xE0, 255)
MOON = (0xE8, 0xEC, 0xF4, 255)
MOON_D = (0xB0, 0xB8, 0xD0, 255)
SUN = (0xFF, 0xC8, 0x4A, 255)
WOOD = (0xB8, 0x7A, 0x48, 255)
TRANSPARENT = (0, 0, 0, 0)


def _load_farm_scene():
    path = ROOT / "scripts/gen_farm_scene_assets.py"
    spec = importlib.util.spec_from_file_location("gen_farm_scene_assets", path)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return mod


def _load_pixel():
    path = ROOT / "scripts/gen_pixel_assets.py"
    spec = importlib.util.spec_from_file_location("gen_pixel_assets", path)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return mod


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


def ic_clock() -> Image.Image:
    """Warm pixel clock / period HUD icon (32×32)."""
    img = new_img()
    # wood bezel
    fill_rect(img, 6, 6, 25, 25, WOOD)
    fill_rect(img, 8, 8, 23, 23, CREAM)
    rect_outline(img, 6, 6, 25, 25, DECOR_OUTLINE)
    rect_outline(img, 7, 7, 24, 24, OUTLINE)
    # face ring
    rect_outline(img, 9, 9, 22, 22, OUTLINE)
    # tick marks 12/3/6/9
    fill_rect(img, 15, 10, 16, 11, OUTLINE)
    fill_rect(img, 15, 20, 16, 21, OUTLINE)
    fill_rect(img, 10, 15, 11, 16, OUTLINE)
    fill_rect(img, 20, 15, 21, 16, OUTLINE)
    # hands (point ~2 o'clock afternoon farm vibe)
    # hour → 14:00-ish
    for x, y in [(15, 16), (16, 15), (17, 14), (18, 13)]:
        px(img, x, y, DECOR_OUTLINE)
    # minute up to 12
    for y in range(11, 17):
        px(img, 15, y, OUTLINE)
        px(img, 16, y, OUTLINE)
    # center
    fill_rect(img, 14, 14, 17, 17, GOLD)
    rect_outline(img, 14, 14, 17, 17, DECOR_OUTLINE)
    # tiny sun accent top-right (period readable)
    fill_rect(img, 24, 3, 28, 7, SUN)
    rect_outline(img, 24, 3, 28, 7, OUTLINE)
    px(img, 26, 2, GOLD_L)
    px(img, 29, 5, GOLD_L)
    px(img, 26, 8, GOLD_L)
    px(img, 23, 5, GOLD_L)
    return img


def ic_growth_point_refresh() -> Image.Image:
    """Refreshed growth gem — chunkier gold + deep double outline."""
    img = new_img()
    # diamond body
    body = []
    for y in range(4, 28):
        half = 12 - abs(y - 16) // 1
        # taper diamond
        t = abs(y - 16)
        half = max(2, 11 - t)
        for x in range(16 - half, 16 + half + 1):
            body.append((x, y))
            px(img, x, y, GOLD if (x + y) % 3 else GOLD_L)
    # cream facet
    fill_rect(img, 13, 12, 18, 18, CREAM)
    fill_rect(img, 14, 10, 17, 20, GOLD_L)
    fill_rect(img, 15, 8, 16, 22, CREAM)
    # deep outline around diamond silhouette
    filled = set(body)
    for x, y in list(filled):
        for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            nx, ny = x + dx, y + dy
            if (nx, ny) not in filled:
                px(img, nx, ny, DECOR_OUTLINE)
    # inner warm outline
    for x, y in list(filled):
        edge = False
        for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            if (x + dx, y + dy) not in filled:
                edge = True
                break
        if edge:
            px(img, x, y, OUTLINE)
    # cardinal spikes
    fill_rect(img, 15, 1, 16, 4, GOLD)
    fill_rect(img, 15, 27, 16, 30, GOLD)
    fill_rect(img, 1, 15, 4, 16, GOLD)
    fill_rect(img, 27, 15, 30, 16, GOLD)
    for x0, y0, x1, y1 in [(15, 1, 16, 4), (15, 27, 16, 30), (1, 15, 4, 16), (27, 15, 30, 16)]:
        rect_outline(img, x0 - 1, y0 - 1, x1 + 1, y1 + 1, DECOR_OUTLINE)
    return img


def overlay_phase(name: str) -> Image.Image:
    """32×32 solid tint tile (stretchable) matching DayPhaseLogic ARGB."""
    r, g, b, a = PHASE_TINTS[name]
    img = new_img()
    if a == 0:
        return img  # fully transparent day
    fill_rect(img, 0, 0, 31, 31, (r, g, b, a))
    return img


def save(name: str, img: Image.Image) -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    PREVIEW.mkdir(parents=True, exist_ok=True)
    img.save(OUT / name, "PNG")
    scale = max(1, 256 // max(img.size))
    img.resize((img.size[0] * scale, img.size[1] * scale), Image.NEAREST).save(
        PREVIEW / name
    )
    print(f"wrote {name} {img.size[0]}x{img.size[1]}")


def main() -> None:
    farm = _load_farm_scene()

    # --- Terrain / fence / barren (refresh from existing warm generators) ---
    terrain = {
        "tile_grass.png": farm.tile_grass,
        "tile_dirt.png": farm.tile_dirt,
        "tile_path.png": farm.tile_path,
        "tile_dirt_edge.png": farm.tile_dirt_edge_generic,
        "tile_dirt_edge_n.png": lambda: farm.tile_dirt_edge("n"),
        "tile_dirt_edge_s.png": lambda: farm.tile_dirt_edge("s"),
        "tile_dirt_edge_e.png": lambda: farm.tile_dirt_edge("e"),
        "tile_dirt_edge_w.png": lambda: farm.tile_dirt_edge("w"),
        "tile_grass_barren.png": farm.tile_grass_barren,
        "fence_h.png": farm.fence_h,
        "fence_v.png": farm.fence_v,
        "fence_corner_nw.png": lambda: farm.fence_corner("nw"),
        "fence_corner_ne.png": lambda: farm.fence_corner("ne"),
        "fence_corner_sw.png": lambda: farm.fence_corner("sw"),
        "fence_corner_se.png": lambda: farm.fence_corner("se"),
    }
    for name, gen in terrain.items():
        img = gen()
        assert img.size == (32, 32), name
        save(name, img)

    # --- HUD ---
    save("ic_clock.png", ic_clock())
    save("ic_growth_point.png", ic_growth_point_refresh())

    # --- Phase overlays (optional drawable path; Compose tint preferred) ---
    for key in ("dawn", "day", "dusk", "night"):
        save(f"overlay_{key}.png", overlay_phase(key))

    print("M1 art refresh done.")


if __name__ == "__main__":
    main()
