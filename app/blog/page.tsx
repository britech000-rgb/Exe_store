import type { Metadata } from "next";
import Link from "next/link";
import { guides } from "@/content/guides";
import { notes } from "@/content/notes";
import { readingMinutes } from "@/lib/reading";

export const revalidate = 60;

export const metadata: Metadata = {
  title: "Blog & Insights",
  description:
    "Buying guides, how-to notes, and product advice from EXE STORE. Read them before you send a brief.",
  alternates: { canonical: "/blog" },
};

const categories = ["Buying Guides", "How-To Guides", "Product Advice"];

type Article = {
  href: string;
  title: string;
  summary: string;
  category: string;
  minutes: number;
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
      lead: guide.slug === lead.slug,
    })),
    ...notes.map((note) => ({
      href: `/trends/${note.slug}`,
      title: note.title,
      summary: note.paragraphs[0],
      category: note.category,
      minutes: readingMinutes(note.paragraphs),
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
            only when a record has one. These local articles do not.
          </p>
          {!query && !cat && lead ? (
            <div className="insight-top">
              <article className="story story-lead">
                <p className="index-label">{lead.category}</p>
                <h2>
                  <Link href={`/blog/${lead.slug}`}>{lead.title}</Link>
                </h2>
                <p>{lead.excerpt}</p>
                <p className="meta">{readingMinutes([lead.excerpt, ...lead.paragraphs])} min read</p>
              </article>
              <div className="insight-side">
                {support.map((guide) => (
                  <article key={guide.slug} className="story">
                    <p className="index-label">{guide.category}</p>
                    <h3>
                      <Link href={`/blog/${guide.slug}`}>{guide.title}</Link>
                    </h3>
                    <p>{guide.excerpt}</p>
                  </article>
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
              {filtered.map((article) => (
                <article key={article.href} className="story">
                  <p className="index-label">{article.category}</p>
                  <h3>
                    <Link href={article.href}>{article.title}</Link>
                  </h3>
                  <p>{article.summary}</p>
                  <p className="meta">{article.minutes} min read</p>
                </article>
              ))}
            </div>
          )}
        </div>
      </section>
    </main>
  );
}
