import type { Metadata } from "next";
import Link from "next/link";
import { AskLink } from "@/components/AskLink";
import { CategoryCard } from "@/components/CategoryCard";
import { StoryCard } from "@/components/StoryCard";
import { site } from "@/config/site";
import { guides } from "@/content/guides";
import { categories } from "@/content/shop";
import { services } from "@/content/work";
import { getServicePage } from "@/content/service-pages";
import { readingMinutes } from "@/lib/reading";

export const metadata: Metadata = {
  title: { absolute: "EXE STORE · IT products and the work around them" },
  description:
    "EXE STORE supplies laptops, networking, CCTV, and the work around them. Send the brief. The price is confirmed on WhatsApp before you pay.",
  alternates: { canonical: "/" },
  openGraph: {
    title: "EXE STORE · IT products and the work around them",
    description:
      "EXE STORE supplies laptops, networking, CCTV, and the work around them. Send the brief. The price is confirmed on WhatsApp before you pay.",
    url: "/",
  },
};

const reasons = [
  "You say what you need.",
  "It is checked against that brief.",
  "The price is confirmed on WhatsApp before you pay.",
];

export default function HomePage() {
  const featured = categories.slice(0, 4);
  const editorial = categories.find((category) => category.slug === "chargers") ?? categories[0];
  const articles = [...guides].sort((a, b) => Number(Boolean(b.advertised)) - Number(Boolean(a.advertised)));

  return (
    <main id="content">
      <section className="hero">
        <div className="wrap mast">
          <div>
            <p className="kicker">{site.name}</p>
            <h1>Gear for the work you actually do.</h1>
            <p className="lede">
              Laptops through to CCTV, and the work around them. Tell {site.name} what you need.
              It is checked, then the price is confirmed on WhatsApp before you pay.
            </p>
            <div className="share-actions">
              <Link className="button" href="/shop">
                Shop products
              </Link>
              <AskLink message={site.defaultAskMessage} className="button button-line" />
            </div>
          </div>
          <Link href="/shop/laptops" className="hero-photo">
            <img
              src="/images/laptops.jpg"
              alt="A laptop open on a light wood desk"
              width={1600}
              height={1067}
            />
            <span>Laptops</span>
          </Link>
        </div>
      </section>

      <section className="sheet">
        <div className="wrap">
          <div className="section-head">
            <div>
              <p className="index-label">Catalogue</p>
              <h2>Shop by category</h2>
            </div>
            <Link href="/shop">All categories</Link>
          </div>
          <div className="category-rail">
            {categories.map((category) => (
              <Link key={category.slug} href={`/shop/${category.slug}`}>
                <img src={category.image} alt="" width={640} height={480} />
                <span>{category.title}</span>
              </Link>
            ))}
          </div>
        </div>
      </section>

      <section className="sheet" style={{ paddingTop: 0 }}>
        <div className="wrap">
          <div className="section-head">
            <div>
              <p className="index-label">From the catalogue</p>
              <h2>Selected</h2>
            </div>
          </div>
          <p className="standfirst">
            Four places to start. These are not ranked by sales. A published bestseller list is not on file.
          </p>
          <div className="grid cards">
            {featured.map((category, index) => (
              <CategoryCard key={category.slug} category={category} index={index} />
            ))}
          </div>
        </div>
      </section>

      <section className="sheet sheet-ink">
        <div className="wrap feature">
          <img src={editorial.image} alt={editorial.imageAlt} width={1600} height={1200} />
          <div>
            <p className="index-label">Why it matters</p>
            <h2>{editorial.title}</h2>
            <p>{editorial.intro}</p>
            <Link className="button" href={`/shop/${editorial.slug}`}>
              View {editorial.title}
            </Link>
          </div>
        </div>
      </section>

      <section className="sheet">
        <div className="wrap">
          <div className="section-head">
            <div>
              <p className="index-label">Arranged work</p>
              <h2>Services</h2>
            </div>
            <Link href="/work">All services</Link>
          </div>
          <div className="grid cards">
            {services.map((service) => {
              const page = getServicePage(service.slug);
              return (
                <article key={service.slug} className="card">
                  {page ? (
                    <Link href={`/work/${service.slug}`} className="card-media" aria-label={service.title}>
                      <img src={page.image} alt="" width={1600} height={1067} />
                    </Link>
                  ) : null}
                  <div className="card-body">
                    <h2>
                      <Link href={`/work/${service.slug}`}>{service.title}</Link>
                    </h2>
                    <p>{service.summary}</p>
                    <div className="card-actions">
                      <Link className="button button-line" href={`/work/${service.slug}`}>
                        Explore service
                      </Link>
                    </div>
                  </div>
                </article>
              );
            })}
          </div>
        </div>
      </section>

      <section className="sheet" style={{ paddingTop: 0 }}>
        <div className="wrap">
          <p className="index-label">Why shop here</p>
          <h2>The rule, and nothing invented around it.</h2>
          <ol className="rule">
            {reasons.map((step, index) => (
              <li key={step}>
                <span>0{index + 1}</span>
                <p>{step}</p>
              </li>
            ))}
          </ol>
        </div>
      </section>

      <section className="sheet sheet-white">
        <div className="wrap">
          <div className="section-head">
            <div>
              <p className="index-label">Blog & Insights</p>
              <h2>Latest</h2>
            </div>
            <Link href="/blog">All articles</Link>
          </div>
          <div className="article-grid">
            {articles.map((guide) => (
              <StoryCard
                key={guide.slug}
                lead={Boolean(guide.advertised)}
                href={`/blog/${guide.slug}`}
                title={guide.title}
                summary={guide.excerpt}
                category={guide.category}
                image={guide.image}
                imageAlt={guide.imageAlt}
                meta={`${readingMinutes([guide.excerpt, ...guide.paragraphs])} min read`}
              />
            ))}
          </div>
        </div>
      </section>

      <section className="sheet">
        <div className="wrap contact-strip">
          <div>
            <p className="index-label">Contact</p>
            <h2>Send the brief.</h2>
            <p>
              WhatsApp is the desk. Delivery and collection are part of the quote.
              A street address and a coverage list are not published.
            </p>
          </div>
          <Link className="button" href="/contact">
            Contact
          </Link>
        </div>
      </section>
    </main>
  );
}
