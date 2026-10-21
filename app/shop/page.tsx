import type { Metadata } from "next";
import { CategoryCard } from "@/components/CategoryCard";
import { categories } from "@/content/shop";

export const metadata: Metadata = {
  title: "Shop",
  description:
    "Laptops, desktops, chargers, networking, CCTV, power, phones, and software. Copy a category link for a WhatsApp status.",
  alternates: { canonical: "/shop" },
};

export default function ShopPage() {
  return (
    <main id="content" className="sheet">
      <div className="wrap">
        <p className="kicker">Shop</p>
        <h1 style={{ fontSize: "clamp(3rem, 8vw, 5.4rem)", marginTop: 10 }}>
          Pick the category.
        </h1>
        <p className="lede" style={{ color: "var(--muted)" }}>
          Eight places to start. Each one has its own link and a preview. Copy
          the link, drop it on a WhatsApp status, and a tap opens that
          category. No prices on this page. I confirm the price on WhatsApp
          before you pay.
        </p>
        <div className="grid cards" style={{ marginTop: 28 }}>
          {categories.map((category) => (
            <CategoryCard key={category.slug} category={category} />
          ))}
        </div>
      </div>
    </main>
  );
}
