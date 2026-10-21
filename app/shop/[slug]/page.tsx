import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { RelatedLinks } from "@/components/RelatedLinks";
import { Rule } from "@/components/Rule";
import { ShareRow } from "@/components/ShareRow";
import { categories, getCategory } from "@/content/shop";

type Props = { params: Promise<{ slug: string }> };

export function generateStaticParams() {
  return categories.map((category) => ({ slug: category.slug }));
}

export const dynamicParams = false;

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { slug } = await params;
  const category = getCategory(slug);
  if (!category) return { title: "Not found" };
  return {
    title: category.title,
    description: category.summary,
    alternates: { canonical: `/shop/${category.slug}` },
  };
}

export default async function CategoryPage({ params }: Props) {
  const { slug } = await params;
  const category = getCategory(slug);
  if (!category) notFound();
  const path = `/shop/${category.slug}`;
  const others = categories.filter((item) => item.slug !== category.slug);

  return (
    <main id="content" className="sheet">
      <div className="wrap">
        <p className="kicker">Shop</p>
        <h1 style={{ marginTop: 10 }}>{category.title}</h1>
        <p className="lede" style={{ color: "var(--muted)" }}>
          {category.summary}
        </p>
        <figure className="preview">
          <img
            src={category.image}
            alt={category.imageAlt}
            width={1600}
            height={1067}
          />
        </figure>
        <p className="product-copy">{category.intro}</p>
        <ShareRow
          message={category.askMessage}
          path={path}
          name={category.title}
        />
        <div className="split">
          <section>
            <h2>What I can sort</h2>
            <ul className="list">
              {category.covers.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </section>
          <section>
            <h2>Tell me this</h2>
            <ul className="list">
              {category.tellMe.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </section>
        </div>
        <RelatedLinks links={category.related ? [category.related] : undefined} />
        <div style={{ marginTop: 36 }}>
          <h2>The rule</h2>
          <Rule />
        </div>
        <div style={{ marginTop: 36 }}>
          <h2>Other categories</h2>
          <p className="related">
            {others.map((item) => (
              <Link key={item.slug} href={`/shop/${item.slug}`}>
                {item.title}
              </Link>
            ))}
          </p>
        </div>
      </div>
    </main>
  );
}
