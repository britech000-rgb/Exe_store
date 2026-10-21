import type { Metadata } from "next";
import { Suspense } from "react";
import { ShopBrowser } from "@/components/ShopBrowser";
import { ShareMenu } from "@/components/ShareMenu";
import { categories } from "@/content/shop";
import { jsonLdScript } from "@/lib/seo";
import { absoluteUrl } from "@/lib/site-url";

const shopDescription =
  "Laptops, desktops, chargers, networking, CCTV, power, phones, and software. Each category is quoted on WhatsApp before you pay.";

export const metadata: Metadata = {
  title: "Shop",
  description: shopDescription,
  alternates: { canonical: "/shop" },
  openGraph: {
    title: "Shop",
    description: shopDescription,
    url: "/shop",
    images: [
      {
        url: "/social/shop-index.jpg",
        width: 1200,
        height: 630,
        alt: "EXE STORE shop.",
        type: "image/jpeg",
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    title: "Shop",
    description: shopDescription,
    images: ["/social/shop-index.jpg"],
  },
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
          <div className="share-actions">
            <ShareMenu path="/shop" name="EXE STORE Shop" social />
          </div>
          <Suspense fallback={<p>Loading the catalogue.</p>}>
            <ShopBrowser categories={categories} />
          </Suspense>
        </div>
      </section>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: jsonLdScript(list) }} />
    </main>
  );
}
