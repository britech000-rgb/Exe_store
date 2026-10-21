"""Build 1200x630 JPEG cards for blog and trend link previews.

WhatsApp, Facebook, X, and LinkedIn read og:image from the page.
The card has to be an absolute image on this site, at least 300px wide,
and under 600KB. These files are 1200x630 and kept under 300KB.
"""

import re
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "public" / "social"
W, H = 1200, 630
FONT = Path("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf")


def records(path: Path):
    text = path.read_text()
    found = []
    for chunk in re.split(r"\n  \{", text):
        slug = re.search(r'slug: "([^"]+)"', chunk)
        title = re.search(r'title: "([^"]+)"', chunk)
        image = re.search(r'image: "([^"]+)"', chunk)
        summary = re.search(r'summary: "([^"]+)"', chunk)
        if slug and title and image:
            found.append((slug.group(1), title.group(1), image.group(1), summary.group(1) if summary else title.group(1)))
    return found


def cover(path: Path, width: int, height: int) -> Image.Image:
    image = Image.open(path).convert("RGB")
    scale = max(width / image.width, height / image.height)
    resized = image.resize((int(image.width * scale) + 1, int(image.height * scale) + 1), Image.Resampling.LANCZOS)
    left = (resized.width - width) // 2
    top = max(0, int((resized.height - height) * 0.35))
    return resized.crop((left, top, left + width, top + height))


def card(photo: Path, kicker: str, title: str, dest: Path):
    base = cover(photo, W, H).convert("RGBA")
    shade = Image.new("L", (1, H))
    for y in range(H):
        if y < 250:
            alpha = 36
        else:
            alpha = int(36 + (y - 250) / (H - 250) * 200)
        shade.putpixel((0, y), min(alpha, 225))
    shade = shade.resize((W, H))
    ink = Image.new("RGBA", (W, H), (20, 32, 51, 255))
    ink.putalpha(shade)
    framed = Image.alpha_composite(base, ink)
    draw = ImageDraw.Draw(framed)
    brand = ImageFont.truetype(str(FONT), 28)
    kicker_font = ImageFont.truetype(str(FONT), 26)
    title_font = ImageFont.truetype(str(FONT), 58 if len(title) < 32 else 46)
    draw.rounded_rectangle((48, 42, 292, 98), radius=28, fill=(247, 247, 244, 255))
    draw.text((72, 56), "EXE STORE", font=brand, fill=(20, 32, 51, 255))
    draw.rectangle((48, H - 214, 120, H - 206), fill=(8, 108, 112, 255))
    draw.text((48, H - 194), kicker.upper(), font=kicker_font, fill=(215, 232, 232, 255))
    lines = wrap(draw, title, title_font, 1040)
    y = H - 150
    for line in lines[:2]:
        draw.text((48, y), line, font=title_font, fill=(255, 255, 255, 255))
        y += 64 if title_font.size >= 58 else 54
    rgb = framed.convert("RGB")
    dest.parent.mkdir(parents=True, exist_ok=True)
    for quality in (84, 76, 68, 60):
        rgb.save(dest, "JPEG", quality=quality, optimize=True, progressive=True)
        if dest.stat().st_size <= 300_000:
            break
    print(f"{dest.name} {dest.stat().st_size}")


def wrap(draw: ImageDraw.ImageDraw, text: str, font: ImageFont.FreeTypeFont, max_width: int):
    words = text.split()
    lines = []
    current = ""
    for word in words:
        trial = word if not current else f"{current} {word}"
        if draw.textlength(trial, font=font) <= max_width:
            current = trial
        else:
            if current:
                lines.append(current)
            current = word
    if current:
        lines.append(current)
    return lines


def shade_of(width: int, height: int, start: int, low: int, high: int) -> Image.Image:
    shade = Image.new("L", (1, height))
    for y in range(height):
        if y < start:
            alpha = low
        else:
            alpha = int(low + (y - start) / (height - start) * (high - low))
        shade.putpixel((0, y), min(alpha, 230))
    return shade.resize((width, height))


def save_jpeg(image: Image.Image, dest: Path, limit: int):
    rgb = image.convert("RGB")
    dest.parent.mkdir(parents=True, exist_ok=True)
    for quality in (86, 78, 70, 62):
        rgb.save(dest, "JPEG", quality=quality, optimize=True, progressive=True)
        if dest.stat().st_size <= limit:
            break
    print(f"{dest.name} {dest.stat().st_size} {rgb.size}")


def story(photo: Path, kicker: str, title: str, dest: Path):
    width, height = 1080, 1920
    base = cover(photo, width, height).convert("RGBA")
    ink = Image.new("RGBA", (width, height), (20, 32, 51, 255))
    ink.putalpha(shade_of(width, height, 700, 50, 210))
    framed = Image.alpha_composite(base, ink)
    draw = ImageDraw.Draw(framed)
    brand = ImageFont.truetype(str(FONT), 36)
    kicker_font = ImageFont.truetype(str(FONT), 32)
    title_font = ImageFont.truetype(str(FONT), 78 if len(title) < 28 else 64)
    lines = wrap(draw, title, title_font, 920)[:4]
    block = 92 + 36 + 28 + len(lines) * 88
    y = 420 + max(0, (1100 - block) // 2)
    draw.rounded_rectangle((width / 2 - 170, y, width / 2 + 170, y + 78), radius=39, fill=(247, 247, 244, 255))
    draw.text((width / 2, y + 18), "EXE STORE", font=brand, fill=(20, 32, 51, 255), anchor="ma")
    y += 110
    draw.rectangle((width / 2 - 36, y, width / 2 + 36, y + 8), fill=(8, 108, 112, 255))
    y += 28
    draw.text((width / 2, y), kicker.upper(), font=kicker_font, fill=(215, 232, 232, 255), anchor="ma")
    y += 64
    for line in lines:
        draw.text((width / 2, y), line, font=title_font, fill=(255, 255, 255, 255), anchor="ma")
        y += 88
    save_jpeg(framed, dest, 450_000)


def post(photo: Path, kicker: str, title: str, dest: Path):
    width, height = 1080, 1350
    base = cover(photo, width, height).convert("RGBA")
    ink = Image.new("RGBA", (width, height), (20, 32, 51, 255))
    ink.putalpha(shade_of(width, height, 620, 30, 220))
    framed = Image.alpha_composite(base, ink)
    draw = ImageDraw.Draw(framed)
    brand = ImageFont.truetype(str(FONT), 32)
    kicker_font = ImageFont.truetype(str(FONT), 28)
    title_font = ImageFont.truetype(str(FONT), 64 if len(title) < 32 else 52)
    draw.rounded_rectangle((64, 64, 360, 132), radius=34, fill=(247, 247, 244, 255))
    draw.text((84, 80), "EXE STORE", font=brand, fill=(20, 32, 51, 255))
    draw.rectangle((64, height - 360, 148, height - 350), fill=(8, 108, 112, 255))
    draw.text((64, height - 330), kicker.upper(), font=kicker_font, fill=(215, 232, 232, 255))
    y = height - 270
    for line in wrap(draw, title, title_font, 940)[:3]:
        draw.text((64, y), line, font=title_font, fill=(255, 255, 255, 255))
        y += 74
    save_jpeg(framed, dest, 400_000)


def main():
    jobs = []
    for slug, title, image, _summary in records(ROOT / "content" / "guides.ts"):
        jobs.append((f"blog-{slug}.jpg", image, "Blog & Insights", title))
    for slug, title, image, _summary in records(ROOT / "content" / "notes.ts"):
        jobs.append((f"trends-{slug}.jpg", image, "Trends", title))
    for slug, title, image, summary in records(ROOT / "content" / "shop.ts"):
        line = summary.split(". ")[0].rstrip(".")
        jobs.append((f"shop-{slug}.jpg", image, title, line))
    jobs.append(("blog-index.jpg", "/images/laptops.jpg", "Blog & Insights", "Read this before the brief."))
    jobs.append(("blog-default.jpg", "/images/laptops.jpg", "Blog & Insights", "Blog & Insights"))
    jobs.append(("trends-index.jpg", "/images/laptops.jpg", "Trends", "Say the work, not the slogan."))
    jobs.append(("trends-default.jpg", "/images/networking.jpg", "Trends", "Trends"))
    jobs.append(("shop-index.jpg", "/images/laptops.jpg", "Shop", "Tell me what you need."))
    jobs.append(("shop-default.jpg", "/images/desktops.jpg", "Shop", "The catalogue."))
    instagram = ROOT / "public" / "instagram"
    for name, image, kicker, title in jobs:
        photo = ROOT / "public" / image.lstrip("/")
        card(photo, kicker, title, OUT / name)
        stem = name.removesuffix(".jpg")
        story(photo, kicker, title, instagram / f"story-{stem}.jpg")
        post(photo, kicker, title, instagram / f"post-{stem}.jpg")


if __name__ == "__main__":
    main()
