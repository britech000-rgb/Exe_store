import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { AskLink } from "@/components/AskLink";
import { ShareMenu } from "@/components/ShareMenu";
import { ZoomImage } from "@/components/ZoomImage";
import { categories, getCategory } from "@/content/shop";
import { breadcrumbLd, jsonLdScript } from "@/lib/seo";
import { shareImage } from "@/lib/share-image";
import { absoluteUrl } from "@/lib/site-url";

type Props = { params: Promise<{ slug: string }> };

export function generateStaticParams() {
  return categories.map((category) => ({ slug: category.slug }));
}

export const dynamicParams = false;

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { slug } = await params;
  const category = getCategory(slug);
  if (!category) return { title: "Not found" };
  const image = shareImage("shop", category.slug);
  return {
    title: category.title,
    description: category.summary,
    alternates: { canonical: `/shop/${category.slug}` },
    openGraph: {
      url: `/shop/${category.slug}`,
      title: category.title,
      description: category.summary,
      images: [
        {
          url: image,
          width: 1200,
          height: 630,
          alt: category.imageAlt,
          type: "image/jpeg",
        },
      ],
    },
    twitter: {
      card: "summary_large_image",
      title: category.title,
      description: category.summary,
      images: [image],
    },
  };
}

export default async function CategoryPage({ params }: Props) {
  const { slug } = await params;
  const category = getCategory(slug);
  if (!category) notFound();
  const path = `/shop/${category.slug}`;
  const others = categories.filter((item) => item.slug !== category.slug).slice(0, 3);

  const jsonLd = [
    {
      "@context": "https://schema.org",
      "@type": "CollectionPage",
      name: category.title,
      description: category.summary,
      url: absoluteUrl(path),
      image: absoluteUrl(category.image),
      isPartOf: { "@type": "WebSite", name: "EXE STORE", url: absoluteUrl("/") },
    },
    breadcrumbLd([
      { name: "Home", path: "/" },
      { name: "Shop", path: "/shop" },
      { name: category.title, path },
    ]),
  ];

  return (
    <main id="content" className="sheet">
      <div className="wrap">
        <p className="crumbs">
          <Link href="/">Home</Link>
          <span aria-hidden="true">/</span>
          <Link href="/shop">Shop</Link>
          <span aria-hidden="true">/</span>
          {category.title}
        </p>
        <div className="product">
          <ZoomImage src={category.image} alt={category.imageAlt} />
          <div className="product-buy">
            <p className="kicker">Shop</p>
            <h1>{category.title}</h1>
            <p className="lede">{category.summary}</p>
            <p className="price-note">Quote. A published price is not on file.</p>
            <p className="availability">Availability is confirmed when the brief is checked. Nothing is reserved on this page.</p>
            <div className="share-actions">
              <AskLink message={category.askMessage} />
              <ShareMenu path={path} name={category.title} social />
            </div>
          </div>
        </div>
        <div className="split product-detail">
          <section>
            <h2>What this covers</h2>
            <ul className="list">
              {category.covers.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </section>
          <section>
            <h2>What to send</h2>
            <ul className="list">
              {category.tellMe.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </section>
          <section>
            <h2>How it is bought</h2>
            <p className="product-copy">{category.intro}</p>
            <p className="product-copy">
              Delivery and collection are confirmed with the quote. A fee, a warranty window, and a returns window are not published.
            </p>
          </section>
          {category.related ? (
            <section>
              <h2>Related</h2>
              <p className="related">
                <Link href={category.related.href}>{category.related.label}</Link>
              </p>
            </section>
          ) : null}
        </div>
        <div className="section-head" style={{ marginTop: 48 }}>
          <h2>Other categories</h2>
        </div>
        <p className="related">
          {others.map((item) => (
            <Link key={item.slug} href={`/shop/${item.slug}`}>
              {item.title}
            </Link>
          ))}
        </p>
        <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: jsonLdScript(jsonLd) }} />
      </div>
    </main>
  );
}
