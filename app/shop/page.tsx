import type { Metadata } from "next";
import { CategoryCard } from "@/components/CategoryCard";
import { categories } from "@/content/shop";

export const metadata: Metadata = {
  title: "Shop",
  description:
    "Laptops, desktops, chargers, networking, CCTV, power, phones, and software. Each category has a photograph, a description, and its own link.",
  alternates: { canonical: "/shop" },
};

export default function ShopPage() {
  return (
    <main id="content">
      <section className="sheet sheet-black page-intro">
        <div className="wrap">
          <p className="kicker">Shop</p>
          <h1>Eight categories. A photograph and a description for each.</h1>
          <p className="lede">
            Copy a category link onto a WhatsApp status. A tap opens that
            category. No prices on this page. The price is confirmed on
            WhatsApp before you pay.
          </p>
        </div>
      </section>
      <section className="sheet">
        <div className="wrap">
          <div className="grid cards">
            {categories.map((category, index) => (
              <CategoryCard key={category.slug} category={category} index={index} />
            ))}
          </div>
        </div>
      </section>
    </main>
  );
}
