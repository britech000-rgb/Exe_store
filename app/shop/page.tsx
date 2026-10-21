import type { Metadata } from "next";
import { Suspense } from "react";
import { ShopBrowser } from "@/components/ShopBrowser";
import { categories } from "@/content/shop";
import { jsonLdScript } from "@/lib/seo";
import { absoluteUrl } from "@/lib/site-url";

export const metadata: Metadata = {
  title: "Shop",
  description:
    "Laptops, desktops, chargers, networking, CCTV, power, phones, and software. Each category is quoted on WhatsApp before you pay.",
  alternates: { canonical: "/shop" },
};

export default function ShopPage() {
  const list = {
    "@context": "https://schema.org",
    "@type": "ItemList",
    name: "EXE STORE catalogue",
    itemListElement: categories.map((category, index) => ({
      "@type": "ListItem",
      position: index + 1,
      name: category.title,
      url: absoluteUrl(`/shop/${category.slug}`),
    })),
  };

  return (
    <main id="content">
      <section className="sheet page-intro">
        <div className="wrap">
          <p className="kicker">Shop</p>
          <h1>The catalogue.</h1>
          <p className="lede">
            Eight categories. Search, filter, and sort what is actually here.
            There is no price list on this page. The price is confirmed on
            WhatsApp before you pay.
          </p>
          <Suspense fallback={<p>Loading the catalogue.</p>}>
            <ShopBrowser categories={categories} />
          </Suspense>
        </div>
      </section>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: jsonLdScript(list) }} />
    </main>
  );
}
