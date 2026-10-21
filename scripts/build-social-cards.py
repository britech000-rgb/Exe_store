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
        if slug and title and image:
            found.append((slug.group(1), title.group(1), image.group(1)))
    return found


def cover(path: Path) -> Image.Image:
    image = Image.open(path).convert("RGB")
    scale = max(W / image.width, H / image.height)
    resized = image.resize((int(image.width * scale) + 1, int(image.height * scale) + 1), Image.Resampling.LANCZOS)
    left = (resized.width - W) // 2
    top = max(0, int((resized.height - H) * 0.35))
    return resized.crop((left, top, left + W, top + H))


def card(photo: Path, kicker: str, title: str, dest: Path):
    base = cover(photo).convert("RGBA")
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


def main():
    jobs = []
    for slug, title, image in records(ROOT / "content" / "guides.ts"):
        jobs.append((f"blog-{slug}.jpg", image, "Blog & Insights", title))
    for slug, title, image in records(ROOT / "content" / "notes.ts"):
        jobs.append((f"trends-{slug}.jpg", image, "Trends", title))
    jobs.append(("blog-index.jpg", "/images/laptops.jpg", "Blog & Insights", "Read this before the brief."))
    jobs.append(("blog-default.jpg", "/images/laptops.jpg", "Blog & Insights", "Blog & Insights"))
    jobs.append(("trends-index.jpg", "/images/laptops.jpg", "Trends", "Say the work, not the slogan."))
    jobs.append(("trends-default.jpg", "/images/networking.jpg", "Trends", "Trends"))
    for name, image, kicker, title in jobs:
        card(ROOT / "public" / image.lstrip("/"), kicker, title, OUT / name)


if __name__ == "__main__":
    main()
