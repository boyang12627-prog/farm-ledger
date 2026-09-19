
#!/usr/bin/env python3
"""Generate warm-farm 32x32 PNG pixel assets for farm-ledger."""
from pathlib import Path
from PIL import Image

OUT = Path("/workspace/farm-ledger/app/src/main/res/drawable-nodpi")
PREVIEW = Path("/workspace/farm-ledger/scripts/preview_assets")

CREAM = (0xFF, 0xF8, 0xE7, 255)
SAND = (0xF2, 0xE2, 0xC4, 255)
SOIL_L = (0xD9, 0xB4, 0x8C, 255)
SOIL_D = (0xA6, 0x7C, 0x52, 255)
OUTLINE = (0x6B, 0x4A, 0x2E, 255)
STONE = (0x8A, 0x7A, 0x68, 255)
STONE_L = (0xC8, 0xBC, 0xA8, 255)
SOIL_M = (0xC4, 0x96, 0x6A, 255)
ORANGE = (0xE8, 0x8A, 0x3A, 255)
WHEAT_DK = (0xC4, 0x86, 0x28, 255)
LGREEN = (0xB8, 0xD9, 0x7A, 255)
RED = (0xD4, 0x3A, 0x2F, 255)
RED_DK = (0xA8, 0x28, 0x22, 255)
BRICK = (0xC7, 0x5B, 0x39, 255)
GOLD = (0xE8, 0xA8, 0x38, 255)
SAGE = (0x8F, 0xBF, 0x6A, 255)
DGREEN = (0x4F, 0x7A, 0x45, 255)
SKY = (0x7E, 0xB8, 0xC9, 255)
SKIN_L = (0xF5, 0xC6, 0xA8, 255)
SKIN_D = (0xB5, 0x7A, 0x5A, 255)
PAPER = (0xED, 0xE6, 0xD9, 255)
TEXT = (0x5C, 0x53, 0x46, 255)
WHITE = (0xFF, 0xFF, 0xFF, 255)
NEAR_BLACK = (0x2F, 0x2A, 0x24, 255)
TRANSPARENT = (0, 0, 0, 0)

def new_img():
    return Image.new("RGBA", (32, 32), TRANSPARENT)

def px(img, x, y, c):
    if 0 <= x < 32 and 0 <= y < 32:
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

def draw_soil_mound(img, y_top=22):
    """Compact soil base under crops."""
    fill_rect(img, 7, y_top, 24, 30, SOIL_D)
    fill_rect(img, 9, y_top + 1, 22, 29, SOIL_L)
    for x in range(10, 23, 3):
        vline(img, x, y_top + 2, 28, SOIL_D)
    px(img, 11, y_top + 3, STONE)
    px(img, 12, y_top + 3, STONE_L)
    px(img, 20, y_top + 4, STONE)
    px(img, 16, 29, STONE_L)
    rect_outline(img, 7, y_top, 24, 30, OUTLINE)

def draw_seed_generic(img, body=SOIL_D, highlight=SAND):
    draw_soil_mound(img, 22)
    fill_rect(img, 13, 16, 18, 21, body)
    fill_rect(img, 14, 15, 17, 15, body)
    fill_rect(img, 14, 22, 17, 22, body)
    px(img, 14, 16, highlight)
    px(img, 15, 16, highlight)
    hline(img, 14, 17, 14, OUTLINE)
    hline(img, 14, 17, 23, OUTLINE)
    vline(img, 12, 16, 21, OUTLINE)
    vline(img, 19, 16, 21, OUTLINE)
    px(img, 13, 15, OUTLINE)
    px(img, 18, 15, OUTLINE)
    px(img, 13, 22, OUTLINE)
    px(img, 18, 22, OUTLINE)

def crop_wheat_seed():
    img = new_img()
    draw_soil_mound(img, 22)
    fill_rect(img, 13, 17, 18, 21, GOLD)
    fill_rect(img, 14, 16, 17, 16, GOLD)
    fill_rect(img, 14, 22, 17, 22, WHEAT_DK)
    px(img, 14, 17, CREAM)
    px(img, 15, 17, CREAM)
    rect_outline(img, 13, 16, 18, 22, OUTLINE)
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
        vline(img, sx - 1, 14, 23, OUTLINE)
        vline(img, sx + 2, 14, 23, OUTLINE)
        fill_rect(img, sx - 1, 8, sx + 2, 12, SAGE)
        fill_rect(img, sx, 7, sx + 1, 7, LGREEN)
        rect_outline(img, sx - 1, 7, sx + 2, 12, OUTLINE)
        px(img, sx, 9, GOLD)
    return img

def crop_wheat_ready():
    img = new_img()
    draw_soil_mound(img, 26)
    for sx in (10, 15, 20):
        fill_rect(img, sx, 14, sx + 1, 26, DGREEN)
        vline(img, sx - 1, 16, 25, OUTLINE)
        vline(img, sx + 2, 16, 25, OUTLINE)
    heads = [(8, 2, 13, 14), (13, 1, 18, 15), (18, 2, 23, 14)]
    for x0, y0, x1, y1 in heads:
        fill_rect(img, x0, y0, x1, y1, GOLD)
        for y in range(y0 + 1, y1, 2):
            for x in range(x0 + 1, x1):
                px(img, x, y, WHEAT_DK if (x + y) % 2 else CREAM)
        rect_outline(img, x0, y0, x1, y1, OUTLINE)
        fill_rect(img, x0 + 2, y0 - 1, x1 - 2, y0 - 1, GOLD)
        hline(img, x0 + 2, x1 - 2, y0 - 1, OUTLINE)
    return img

def crop_carrot_seed():
    img = new_img()
    draw_soil_mound(img, 22)
    fill_rect(img, 14, 18, 17, 21, ORANGE)
    fill_rect(img, 15, 17, 16, 17, BRICK)
    px(img, 15, 18, GOLD)
    rect_outline(img, 14, 17, 17, 21, OUTLINE)
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
    rect_outline(img, 13, 15, 18, 23, OUTLINE)
    fill_rect(img, 14, 8, 17, 15, DGREEN)
    fill_rect(img, 11, 10, 13, 14, SAGE)
    fill_rect(img, 18, 10, 20, 14, SAGE)
    rect_outline(img, 14, 8, 17, 15, OUTLINE)
    vline(img, 10, 11, 13, OUTLINE)
    vline(img, 21, 11, 13, OUTLINE)
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
    vline(img, 11, 10, 22, OUTLINE)
    vline(img, 20, 10, 22, OUTLINE)
    hline(img, 12, 19, 9, OUTLINE)
    px(img, 12, 23, OUTLINE); px(img, 19, 23, OUTLINE)
    px(img, 13, 26, OUTLINE); px(img, 18, 26, OUTLINE)
    px(img, 14, 28, OUTLINE); px(img, 17, 28, OUTLINE)
    hline(img, 15, 16, 29, OUTLINE)
    fill_rect(img, 9, 2, 11, 9, SAGE)
    fill_rect(img, 20, 2, 22, 9, SAGE)
    fill_rect(img, 13, 1, 18, 9, DGREEN)
    fill_rect(img, 11, 4, 12, 8, SAGE)
    fill_rect(img, 19, 4, 20, 8, SAGE)
    fill_rect(img, 14, 0, 17, 0, LGREEN)
    rect_outline(img, 13, 1, 18, 9, OUTLINE)
    vline(img, 8, 3, 8, OUTLINE)
    vline(img, 23, 3, 8, OUTLINE)
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
    rect_outline(img, 14, 16, 17, 21, OUTLINE)
    px(img, 15, 14, DGREEN)
    px(img, 16, 13, SAGE)
    px(img, 14, 13, SAGE)
    px(img, 15, 15, DGREEN)
    return img

def crop_tomato_grow():
    img = new_img()
    draw_soil_mound(img, 24)
    fill_rect(img, 15, 12, 16, 24, DGREEN)
    vline(img, 14, 14, 23, OUTLINE)
    vline(img, 17, 14, 23, OUTLINE)
    fill_rect(img, 10, 8, 21, 14, SAGE)
    fill_rect(img, 12, 6, 19, 7, DGREEN)
    rect_outline(img, 10, 6, 21, 14, OUTLINE)
    fill_rect(img, 12, 10, 14, 13, LGREEN)
    fill_rect(img, 17, 10, 19, 13, LGREEN)
    rect_outline(img, 12, 10, 14, 13, OUTLINE)
    rect_outline(img, 17, 10, 19, 13, OUTLINE)
    px(img, 13, 9, DGREEN)
    px(img, 18, 9, DGREEN)
    return img

def crop_tomato_ready():
    img = new_img()
    draw_soil_mound(img, 26)
    fill_rect(img, 15, 16, 16, 26, DGREEN)
    vline(img, 14, 18, 25, OUTLINE)
    vline(img, 17, 18, 25, OUTLINE)
    fill_rect(img, 12, 2, 19, 6, DGREEN)
    fill_rect(img, 10, 4, 11, 7, SAGE)
    fill_rect(img, 20, 4, 21, 7, SAGE)
    rect_outline(img, 12, 2, 19, 6, OUTLINE)
    fruits = [(7, 8, 14, 16), (17, 8, 24, 16), (11, 14, 20, 22)]
    for x0, y0, x1, y1 in fruits:
        fill_rect(img, x0, y0, x1, y1, RED)
        px(img, x0, y0, TRANSPARENT); px(img, x1, y0, TRANSPARENT)
        px(img, x0, y1, TRANSPARENT); px(img, x1, y1, TRANSPARENT)
        fill_rect(img, x0 + 2, y0 + 2, x0 + 4, y0 + 4, BRICK)
        px(img, x0 + 3, y0 + 3, CREAM)
        hline(img, x0 + 2, x1 - 2, y1 - 1, RED_DK)
        rect_outline(img, x0, y0, x1, y1, OUTLINE)
        px(img, x0 + 1, y0, OUTLINE); px(img, x1 - 1, y0, OUTLINE)
        px(img, x0 + 1, y1, OUTLINE); px(img, x1 - 1, y1, OUTLINE)
    for cx in (10, 20):
        fill_rect(img, cx, 6, cx + 1, 8, DGREEN)
    return img

def tile_soil_empty():
    """Full-tile mud with clear furrows, stones, dark outline."""
    img = new_img()
    for y in range(1, 31):
        for x in range(1, 31):
            band = ((x + y // 2) // 3) % 2
            c = SOIL_D if band == 0 else SOIL_L
            if (x * 7 + y * 13) % 11 == 0:
                c = SOIL_M
            if (x * 5 + y * 3) % 17 == 0:
                c = SOIL_L if c == SOIL_D else SOIL_D
            px(img, x, y, c)
    for y in (6, 12, 18, 24):
        for x in range(2, 30):
            px(img, x, y, OUTLINE if x % 4 == 0 else SOIL_D)
            if y + 1 < 31:
                px(img, x, y + 1, SOIL_L)
    stones = [
        (5, 8), (6, 8), (5, 9), (14, 5), (15, 5),
        (23, 10), (24, 10), (23, 11), (9, 16), (10, 16),
        (18, 15), (19, 15), (18, 16), (4, 22), (5, 22),
        (26, 20), (27, 20), (26, 21), (12, 26), (13, 26),
        (21, 27), (22, 27), (21, 28), (16, 20),
    ]
    for i, (x, y) in enumerate(stones):
        px(img, x, y, STONE if i % 3 else STONE_L)
    for x, y in [(8, 7), (17, 13), (25, 19), (11, 25), (20, 7), (7, 19)]:
        px(img, x, y, SAND)
    rect_outline(img, 0, 0, 31, 31, OUTLINE)
    for i in range(1, 31):
        px(img, i, 1, SOIL_L)
        px(img, 1, i, SOIL_L)
    return img


def ic_cta_ledger():
    img = new_img()
    fill_rect(img, 6, 4, 25, 27, BRICK)
    fill_rect(img, 8, 6, 23, 25, PAPER)
    fill_rect(img, 6, 4, 9, 27, SOIL_D)
    for y in (10, 14, 18, 22):
        hline(img, 11, 21, y, TEXT)
    rect_outline(img, 6, 4, 25, 27, OUTLINE)
    vline(img, 9, 4, 27, OUTLINE)
    fill_rect(img, 20, 4, 22, 12, GOLD)
    vline(img, 20, 4, 12, OUTLINE)
    vline(img, 22, 4, 12, OUTLINE)
    px(img, 21, 13, OUTLINE)
    return img

def ic_cta_settle():
    img = new_img()
    fill_rect(img, 8, 6, 23, 25, GOLD)
    fill_rect(img, 6, 8, 25, 23, GOLD)
    fill_rect(img, 10, 4, 21, 27, GOLD)
    fill_rect(img, 10, 8, 21, 23, CREAM)
    fill_rect(img, 8, 10, 23, 21, CREAM)
    hline(img, 10, 21, 4, OUTLINE)
    hline(img, 10, 21, 27, OUTLINE)
    vline(img, 6, 8, 23, OUTLINE)
    vline(img, 25, 8, 23, OUTLINE)
    px(img, 8, 6, OUTLINE)
    px(img, 23, 6, OUTLINE)
    px(img, 8, 25, OUTLINE)
    px(img, 23, 25, OUTLINE)
    px(img, 7, 7, OUTLINE)
    px(img, 24, 7, OUTLINE)
    px(img, 7, 24, OUTLINE)
    px(img, 24, 24, OUTLINE)
    for x, y in [(10, 15), (11, 16), (12, 17), (13, 18), (14, 19),
                 (15, 18), (16, 17), (17, 16), (18, 15), (19, 14), (20, 13), (21, 12)]:
        px(img, x, y, DGREEN)
        px(img, x, y - 1, SAGE)
    return img

def ic_preview_farm():
    img = new_img()
    fill_rect(img, 2, 2, 29, 16, SKY)
    fill_rect(img, 22, 4, 27, 9, GOLD)
    rect_outline(img, 22, 4, 27, 9, OUTLINE)
    fill_rect(img, 2, 17, 29, 29, SOIL_L)
    fill_rect(img, 2, 20, 29, 29, SOIL_D)
    for cx in (8, 16, 24):
        fill_rect(img, cx - 1, 12, cx, 20, DGREEN)
        fill_rect(img, cx - 2, 8, cx + 1, 12, GOLD if cx != 16 else BRICK)
        rect_outline(img, cx - 2, 8, cx + 1, 12, OUTLINE)
    rect_outline(img, 1, 1, 30, 30, OUTLINE)
    hline(img, 2, 29, 17, OUTLINE)
    return img

def ic_preview_pet():
    img = new_img()
    fill_rect(img, 8, 8, 23, 24, SKIN_L)
    fill_rect(img, 6, 10, 25, 22, SKIN_L)
    fill_rect(img, 10, 6, 21, 26, SKIN_L)
    fill_rect(img, 6, 4, 11, 10, SKIN_D)
    fill_rect(img, 20, 4, 25, 10, SKIN_D)
    fill_rect(img, 7, 5, 10, 9, SKIN_L)
    fill_rect(img, 21, 5, 24, 9, SKIN_L)
    fill_rect(img, 12, 16, 19, 22, SKIN_D)
    fill_rect(img, 13, 17, 18, 21, SAND)
    px(img, 14, 19, OUTLINE)
    px(img, 17, 19, OUTLINE)
    fill_rect(img, 10, 12, 12, 14, NEAR_BLACK)
    fill_rect(img, 19, 12, 21, 14, NEAR_BLACK)
    px(img, 11, 12, WHITE)
    px(img, 20, 12, WHITE)
    hline(img, 10, 21, 5, OUTLINE)
    hline(img, 10, 21, 27, OUTLINE)
    vline(img, 5, 10, 22, OUTLINE)
    vline(img, 26, 10, 22, OUTLINE)
    rect_outline(img, 6, 4, 11, 10, OUTLINE)
    rect_outline(img, 20, 4, 25, 10, OUTLINE)
    rect_outline(img, 12, 16, 19, 22, OUTLINE)
    px(img, 8, 16, BRICK)
    px(img, 23, 16, BRICK)
    return img

def _flame_base(img):
    fill_rect(img, 12, 6, 19, 26, BRICK)
    fill_rect(img, 10, 10, 21, 24, BRICK)
    fill_rect(img, 14, 3, 17, 6, BRICK)
    fill_rect(img, 15, 1, 16, 3, BRICK)
    fill_rect(img, 13, 12, 18, 24, GOLD)
    fill_rect(img, 14, 8, 17, 12, GOLD)
    fill_rect(img, 15, 5, 16, 8, GOLD)
    fill_rect(img, 14, 16, 17, 23, CREAM)
    fill_rect(img, 15, 12, 16, 16, CREAM)
    hline(img, 15, 16, 0, OUTLINE)
    vline(img, 14, 1, 3, OUTLINE)
    vline(img, 17, 1, 3, OUTLINE)
    vline(img, 12, 4, 8, OUTLINE)
    vline(img, 19, 4, 8, OUTLINE)
    vline(img, 10, 10, 24, OUTLINE)
    vline(img, 21, 10, 24, OUTLINE)
    vline(img, 11, 8, 10, OUTLINE)
    vline(img, 20, 8, 10, OUTLINE)
    hline(img, 12, 19, 27, OUTLINE)
    px(img, 11, 25, OUTLINE)
    px(img, 20, 25, OUTLINE)
    px(img, 13, 3, OUTLINE)
    px(img, 18, 3, OUTLINE)

def _digit_pixels(n):
    digits = {
        3: ["111", "001", "111", "001", "111"],
        5: ["111", "100", "111", "001", "111"],
        7: ["111", "001", "001", "001", "001"],
    }
    return digits[n]

def ic_streak(n):
    img = new_img()
    _flame_base(img)
    fill_rect(img, 20, 20, 30, 30, PAPER)
    rect_outline(img, 20, 20, 30, 30, OUTLINE)
    pattern = _digit_pixels(n)
    ox, oy = 24, 23
    for dy, row in enumerate(pattern):
        for dx, ch in enumerate(row):
            if ch == "1":
                px(img, ox + dx, oy + dy, TEXT)
    return img

def ic_income():
    img = new_img()
    fill_rect(img, 8, 10, 23, 25, GOLD)
    fill_rect(img, 6, 12, 25, 23, GOLD)
    fill_rect(img, 10, 8, 21, 27, GOLD)
    fill_rect(img, 10, 12, 21, 23, CREAM)
    fill_rect(img, 8, 14, 23, 21, CREAM)
    hline(img, 10, 21, 8, OUTLINE)
    hline(img, 10, 21, 27, OUTLINE)
    vline(img, 6, 12, 23, OUTLINE)
    vline(img, 25, 12, 23, OUTLINE)
    px(img, 8, 10, OUTLINE)
    px(img, 23, 10, OUTLINE)
    px(img, 8, 25, OUTLINE)
    px(img, 23, 25, OUTLINE)
    fill_rect(img, 14, 14, 17, 22, DGREEN)
    fill_rect(img, 12, 14, 19, 15, DGREEN)
    fill_rect(img, 13, 12, 18, 13, DGREEN)
    fill_rect(img, 14, 10, 17, 11, DGREEN)
    fill_rect(img, 15, 8, 16, 9, DGREEN)
    for x, y in [(15, 7), (14, 9), (17, 9), (13, 11), (18, 11), (12, 13), (19, 13)]:
        px(img, x, y, OUTLINE)
    return img

def ic_expense():
    img = new_img()
    fill_rect(img, 8, 10, 23, 25, GOLD)
    fill_rect(img, 6, 12, 25, 23, GOLD)
    fill_rect(img, 10, 8, 21, 27, GOLD)
    fill_rect(img, 10, 12, 21, 23, CREAM)
    fill_rect(img, 8, 14, 23, 21, CREAM)
    hline(img, 10, 21, 8, OUTLINE)
    hline(img, 10, 21, 27, OUTLINE)
    vline(img, 6, 12, 23, OUTLINE)
    vline(img, 25, 12, 23, OUTLINE)
    px(img, 8, 10, OUTLINE)
    px(img, 23, 10, OUTLINE)
    px(img, 8, 25, OUTLINE)
    px(img, 23, 25, OUTLINE)
    fill_rect(img, 14, 10, 17, 18, BRICK)
    fill_rect(img, 12, 16, 19, 17, BRICK)
    fill_rect(img, 13, 18, 18, 19, BRICK)
    fill_rect(img, 14, 20, 17, 21, BRICK)
    fill_rect(img, 15, 22, 16, 23, BRICK)
    for x, y in [(15, 24), (14, 22), (17, 22), (13, 20), (18, 20), (12, 18), (19, 18)]:
        px(img, x, y, OUTLINE)
    return img

def ic_no_trade():
    img = new_img()
    fill_rect(img, 5, 6, 26, 27, PAPER)
    fill_rect(img, 5, 6, 26, 11, SAGE)
    fill_rect(img, 9, 3, 11, 8, OUTLINE)
    fill_rect(img, 20, 3, 22, 8, OUTLINE)
    fill_rect(img, 9, 4, 11, 6, SKY)
    fill_rect(img, 20, 4, 22, 6, SKY)
    rect_outline(img, 5, 6, 26, 27, OUTLINE)
    hline(img, 5, 26, 11, OUTLINE)
    fill_rect(img, 12, 15, 15, 16, TEXT)
    fill_rect(img, 14, 17, 17, 18, TEXT)
    fill_rect(img, 13, 19, 16, 20, TEXT)
    for i in range(12):
        px(img, 8 + i, 24 - i, BRICK)
        px(img, 9 + i, 24 - i, BRICK)
    return img

def ic_growth_point():
    img = new_img()
    fill_rect(img, 14, 2, 17, 29, GOLD)
    fill_rect(img, 2, 14, 29, 17, GOLD)
    fill_rect(img, 10, 10, 21, 21, GOLD)
    fill_rect(img, 12, 8, 19, 23, GOLD)
    fill_rect(img, 8, 12, 23, 19, GOLD)
    fill_rect(img, 13, 13, 18, 18, CREAM)
    fill_rect(img, 15, 6, 16, 25, CREAM)
    fill_rect(img, 6, 15, 25, 16, CREAM)
    hline(img, 14, 17, 1, OUTLINE)
    hline(img, 14, 17, 30, OUTLINE)
    vline(img, 1, 14, 17, OUTLINE)
    vline(img, 30, 14, 17, OUTLINE)
    for x, y in [
        (12, 8), (13, 7), (18, 7), (19, 8),
        (21, 10), (22, 11), (23, 12), (24, 13),
        (24, 18), (23, 19), (22, 20), (21, 21),
        (19, 23), (18, 24), (13, 24), (12, 23),
        (10, 21), (9, 20), (8, 19), (7, 18),
        (7, 13), (8, 12), (9, 11), (10, 10),
    ]:
        px(img, x, y, OUTLINE)
    vline(img, 14, 2, 6, OUTLINE)
    vline(img, 17, 2, 6, OUTLINE)
    vline(img, 14, 25, 29, OUTLINE)
    vline(img, 17, 25, 29, OUTLINE)
    hline(img, 2, 6, 14, OUTLINE)
    hline(img, 2, 6, 17, OUTLINE)
    hline(img, 25, 29, 14, OUTLINE)
    hline(img, 25, 29, 17, OUTLINE)
    return img

def ic_seed():
    img = new_img()
    fill_rect(img, 8, 4, 23, 28, PAPER)
    fill_rect(img, 8, 4, 23, 10, SAGE)
    rect_outline(img, 8, 4, 23, 28, OUTLINE)
    hline(img, 8, 23, 10, OUTLINE)
    fill_rect(img, 13, 14, 18, 21, SOIL_D)
    fill_rect(img, 14, 13, 17, 13, SOIL_D)
    fill_rect(img, 14, 22, 17, 22, SOIL_D)
    px(img, 14, 15, SAND)
    px(img, 15, 15, SAND)
    rect_outline(img, 13, 13, 18, 22, OUTLINE)
    px(img, 15, 12, DGREEN)
    px(img, 16, 11, SAGE)
    return img

def btn_plant():
    img = new_img()
    fill_rect(img, 2, 2, 29, 29, SAGE)
    fill_rect(img, 4, 4, 27, 27, CREAM)
    rect_outline(img, 2, 2, 29, 29, OUTLINE)
    rect_outline(img, 3, 3, 28, 28, DGREEN)
    fill_rect(img, 8, 20, 23, 26, SOIL_D)
    fill_rect(img, 8, 20, 23, 21, SOIL_L)
    rect_outline(img, 8, 20, 23, 26, OUTLINE)
    fill_rect(img, 15, 12, 16, 20, DGREEN)
    fill_rect(img, 9, 10, 14, 14, SAGE)
    fill_rect(img, 17, 8, 22, 12, SAGE)
    rect_outline(img, 9, 10, 14, 14, OUTLINE)
    rect_outline(img, 17, 8, 22, 12, OUTLINE)
    vline(img, 14, 12, 19, OUTLINE)
    vline(img, 17, 12, 19, OUTLINE)
    return img

def btn_harvest():
    img = new_img()
    fill_rect(img, 2, 2, 29, 29, GOLD)
    fill_rect(img, 4, 4, 27, 27, CREAM)
    rect_outline(img, 2, 2, 29, 29, OUTLINE)
    rect_outline(img, 3, 3, 28, 28, SOIL_D)
    fill_rect(img, 7, 14, 24, 25, SOIL_D)
    fill_rect(img, 8, 15, 23, 24, SAND)
    for y in (17, 20, 23):
        hline(img, 8, 23, y, SOIL_D)
    rect_outline(img, 7, 14, 24, 25, OUTLINE)
    hline(img, 10, 21, 10, OUTLINE)
    vline(img, 10, 10, 14, OUTLINE)
    vline(img, 21, 10, 14, OUTLINE)
    fill_rect(img, 10, 11, 13, 15, BRICK)
    fill_rect(img, 14, 10, 17, 15, GOLD)
    fill_rect(img, 18, 11, 21, 15, SAGE)
    rect_outline(img, 10, 11, 13, 15, OUTLINE)
    rect_outline(img, 14, 10, 17, 15, OUTLINE)
    rect_outline(img, 18, 11, 21, 15, OUTLINE)
    return img

GENERATORS = {
    "crop_wheat_seed.png": crop_wheat_seed,
    "crop_wheat_grow.png": crop_wheat_grow,
    "crop_wheat_ready.png": crop_wheat_ready,
    "crop_carrot_seed.png": crop_carrot_seed,
    "crop_carrot_grow.png": crop_carrot_grow,
    "crop_carrot_ready.png": crop_carrot_ready,
    "crop_tomato_seed.png": crop_tomato_seed,
    "crop_tomato_grow.png": crop_tomato_grow,
    "crop_tomato_ready.png": crop_tomato_ready,
    "tile_soil_empty.png": tile_soil_empty,
    "ic_cta_ledger.png": ic_cta_ledger,
    "ic_cta_settle.png": ic_cta_settle,
    "ic_preview_farm.png": ic_preview_farm,
    "ic_preview_pet.png": ic_preview_pet,
    "ic_streak_3.png": lambda: ic_streak(3),
    "ic_streak_5.png": lambda: ic_streak(5),
    "ic_streak_7.png": lambda: ic_streak(7),
    "ic_income.png": ic_income,
    "ic_expense.png": ic_expense,
    "ic_no_trade.png": ic_no_trade,
    "ic_growth_point.png": ic_growth_point,
    "ic_seed.png": ic_seed,
    "btn_plant.png": btn_plant,
    "btn_harvest.png": btn_harvest,
}

def main():
    OUT.mkdir(parents=True, exist_ok=True)
    PREVIEW.mkdir(parents=True, exist_ok=True)
    written = []
    for name, gen in GENERATORS.items():
        img = gen()
        assert img.size == (32, 32), name
        path = OUT / name
        img.save(path, "PNG")
        preview = img.resize((256, 256), Image.NEAREST)
        preview.save(PREVIEW / name)
        written.append(name)
        xml = OUT / name.replace(".png", ".xml")
        if xml.exists():
            xml.unlink()
            print(f"replaced {xml.name} -> {name}")
        else:
            print(f"wrote {name}")
    print(f"total: {len(written)}")
    remaining_xml = list(OUT.glob("*.xml"))
    remaining_png = sorted(p.name for p in OUT.glob("*.png"))
    print("remaining xml:", remaining_xml)
    print("pngs:", remaining_png)

if __name__ == "__main__":
    main()
