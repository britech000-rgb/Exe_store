import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { ArticlePhoto } from "@/components/ArticlePhoto";
import { RelatedLinks } from "@/components/RelatedLinks";
import { ShareMenu } from "@/components/ShareMenu";
import { guides } from "@/content/guides";
import { categories } from "@/content/shop";
import { getGuide, getGuides } from "@/lib/content";
import { readingMinutes } from "@/lib/reading";
import { articleShareMeta, shareImage } from "@/lib/share-image";
import { getSiteUrl } from "@/lib/site-url";

type Props = { params: Promise<{ slug: string }> };

export const revalidate = 60;

export async function generateStaticParams() {
  const items = await getGuides();
  return items.map((guide) => ({ slug: guide.slug }));
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { slug } = await params;
  const guide = await getGuide(slug);
  if (!guide) return { title: "Not found" };
  return articleShareMeta({
    title: guide.title,
    description: guide.excerpt,
    path: `/blog/${guide.slug}`,
    image: shareImage("blog", guide.slug),
    alt: `${guide.title}. EXE STORE Blog & Insights.`,
  });
}

export default async function GuidePage({ params }: Props) {
  const { slug } = await params;
  const guide = await getGuide(slug);
  if (!guide) notFound();
  const more = guides.filter((item) => item.slug !== guide.slug);

  const products = (guide.related ?? [])
    .map((item) => categories.find((category) => item.href === `/shop/${category.slug}`))
    .filter((item) => item !== undefined);
  const published = guide.publishedAt
    ? new Intl.DateTimeFormat("en", { dateStyle: "long" }).format(new Date(guide.publishedAt))
    : null;
  const card = shareImage("blog", guide.slug);
  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "BlogPosting",
    headline: guide.title,
    description: guide.excerpt,
    image: `${getSiteUrl()}${card}`,
    mainEntityOfPage: `${getSiteUrl()}/blog/${guide.slug}`,
    publisher: { "@type": "Organization", name: "EXE STORE", url: getSiteUrl() },
  };

  return (
    <main id="content" className="sheet">
      <div className="wrap article">
        <p className="crumbs">
          <Link href="/">Home</Link>
          <span aria-hidden="true">/</span>
          <Link href="/blog">Blog & Insights</Link>
          <span aria-hidden="true">/</span>
          {guide.category}
        </p>
        <p className="kicker">{guide.advertised ? "Featured" : guide.category}</p>
        <h1>{guide.title}</h1>
        <p className="lede">{guide.excerpt}</p>
        <p className="meta">
          {readingMinutes([guide.excerpt, ...guide.paragraphs])} min read
          {published ? ` · ${published}` : " · No publication date on file"}
        </p>
        <ShareMenu path={`/blog/${guide.slug}`} name={guide.title} social />
        {guide.image && guide.imageAlt && guide.photoHref && guide.photoLabel ? (
          <ArticlePhoto
            src={guide.image}
            alt={guide.imageAlt}
            href={guide.photoHref}
            label={guide.photoLabel}
          />
        ) : null}
        <div className="prose">
          {guide.paragraphs.map((paragraph) => (
            <p key={paragraph}>{paragraph}</p>
          ))}
        </div>
        {products.length ? (
          <section>
            <h2>From the catalogue</h2>
            <p className="related">
              {products.map((product) => (
                <Link key={product.slug} href={`/shop/${product.slug}`}>
                  {product.title}
                </Link>
              ))}
            </p>
          </section>
        ) : null}
        <RelatedLinks links={guide.related} />
        <div style={{ marginTop: 40 }}>
          <h2>More guides</h2>
          <p className="related">
            {more.map((item) => (
              <Link key={item.slug} href={`/blog/${item.slug}`}>
                {item.title}
              </Link>
            ))}
          </p>
        </div>
      </div>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />
    </main>
  );
}
