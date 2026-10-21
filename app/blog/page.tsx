import type { Metadata } from "next";
import Link from "next/link";
import { ShareMenu } from "@/components/ShareMenu";
import { StoryCard } from "@/components/StoryCard";
import { guides } from "@/content/guides";
import { notes } from "@/content/notes";
import { readingMinutes } from "@/lib/reading";

export const revalidate = 60;

export const metadata: Metadata = {
  title: "Blog & Insights",
  description:
    "Buying guides, how-to notes, and product advice from EXE STORE. Read them before you send a brief.",
  alternates: { canonical: "/blog" },
  openGraph: {
    title: "Blog & Insights",
    description:
      "Buying guides, how-to notes, and product advice from EXE STORE. Read them before you send a brief.",
    url: "/blog",
    images: [
      {
        url: "/social/blog-index.jpg",
        width: 1200,
        height: 630,
        alt: "EXE STORE Blog & Insights.",
        type: "image/jpeg",
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    title: "Blog & Insights",
    description:
      "Buying guides, how-to notes, and product advice from EXE STORE. Read them before you send a brief.",
    images: ["/social/blog-index.jpg"],
  },
};

const categories = ["Buying Guides", "How-To Guides", "Product Advice"];

type Article = {
  href: string;
  title: string;
  summary: string;
  category: string;
  minutes: number;
  image?: string;
  imageAlt?: string;
  lead?: boolean;
};

export default async function BlogPage({
  searchParams,
}: {
  searchParams: Promise<{ q?: string; cat?: string }>;
}) {
  const { q = "", cat = "" } = await searchParams;
  const query = q.trim().toLowerCase();
  const lead = guides.find((guide) => guide.advertised) ?? guides[0];
  const support = guides.filter((guide) => guide.slug !== lead.slug).slice(0, 2);
  const articles: Article[] = [
    ...guides.map((guide) => ({
      href: `/blog/${guide.slug}`,
      title: guide.title,
      summary: guide.excerpt,
      category: guide.category,
      minutes: readingMinutes([guide.excerpt, ...guide.paragraphs]),
      image: guide.image,
      imageAlt: guide.imageAlt,
      lead: guide.slug === lead.slug,
    })),
    ...notes.map((note) => ({
      href: `/trends/${note.slug}`,
      title: note.title,
      summary: note.paragraphs[0],
      category: note.category,
      minutes: readingMinutes(note.paragraphs),
      image: note.image,
      imageAlt: note.imageAlt,
    })),
  ];
  const filtered = articles.filter((article) => {
    const matchesCat = !cat || article.category === cat;
    const matchesQuery = !query || `${article.title} ${article.summary}`.toLowerCase().includes(query);
    return matchesCat && matchesQuery;
  });

  return (
    <main id="content">
      <section className="sheet page-intro">
        <div className="wrap">
          <p className="kicker">Blog & Insights</p>
          <h1>Read this before the brief.</h1>
          <p className="lede">
            Guides and notes for buying and setting up the work. Dates are shown
            only when a record has one.             These local articles do not.
          </p>
          <div className="share-actions">
            <ShareMenu path="/blog" name="EXE STORE Blog & Insights" social />
          </div>
          {!query && !cat && lead ? (
            <div className="insight-top">
              <StoryCard
                heading="h2"
                lead
                href={`/blog/${lead.slug}`}
                title={lead.title}
                summary={lead.excerpt}
                category={lead.category}
                image={lead.image}
                imageAlt={lead.imageAlt}
                meta={`${readingMinutes([lead.excerpt, ...lead.paragraphs])} min read`}
              />
              <div className="insight-side">
                {support.map((guide) => (
                  <StoryCard
                    key={guide.slug}
                    href={`/blog/${guide.slug}`}
                    title={guide.title}
                    summary={guide.excerpt}
                    category={guide.category}
                    image={guide.image}
                    imageAlt={guide.imageAlt}
                  />
                ))}
              </div>
            </div>
          ) : null}
          <form className="catalogue-tools" action="/blog" method="get" role="search">
            <label htmlFor="blog-q">Search articles</label>
            <div className="catalogue-search">
              <input id="blog-q" name="q" defaultValue={q} placeholder="Laptop, CCTV, office" />
              {cat ? <input type="hidden" name="cat" value={cat} /> : null}
              <button className="button" type="submit">
                Search
              </button>
            </div>
          </form>
          <div className="filter-row">
            <Link className={cat ? "chip" : "chip is-on"} href={q ? `/blog?q=${encodeURIComponent(q)}` : "/blog"}>
              All
            </Link>
            {categories.map((name) => (
              <Link
                key={name}
                className={cat === name ? "chip is-on" : "chip"}
                href={`/blog?cat=${encodeURIComponent(name)}${q ? `&q=${encodeURIComponent(q)}` : ""}`}
              >
                {name}
              </Link>
            ))}
          </div>
          {filtered.length === 0 ? (
            <div className="empty">
              <h2>No articles match.</h2>
              <p>Clear the search or pick another category.</p>
              <Link className="button" href="/blog">
                Clear
              </Link>
            </div>
          ) : (
            <div className="article-grid">
              {(query || cat ? filtered : filtered.filter((article) => !article.lead && !support.some((guide) => article.href === `/blog/${guide.slug}`))).map((article) => (
                <StoryCard
                  key={article.href}
                  href={article.href}
                  title={article.title}
                  summary={article.summary}
                  category={article.category}
                  image={article.image}
                  imageAlt={article.imageAlt}
                  meta={`${article.minutes} min read`}
                />
              ))}
            </div>
          )}
        </div>
      </section>
    </main>
  );
}
