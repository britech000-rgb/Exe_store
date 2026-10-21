import Link from "next/link";
import { AskLink } from "@/components/AskLink";
import { CategoryCard } from "@/components/CategoryCard";
import { Rule } from "@/components/Rule";
import { site } from "@/config/site";
import { proof } from "@/content/proof";
import { categories } from "@/content/shop";
import { services } from "@/content/work";
import { ProofList } from "@/components/ProofList";
import { getGuides, getNotes } from "@/lib/content";

export default async function HomePage() {
  const notes = await getNotes();
  const guides = await getGuides();
  const leadNotes = notes.slice(0, 2);
  const leadGuide = guides[0];

  return (
    <main id="content">
      <section className="hero">
        <div className="wrap">
          <p className="kicker">{site.name}</p>
          <h1>
            Say what you need.
            <span>I check it.</span>
            <em>Then I confirm the price.</em>
          </h1>
          <p className="lede">
            I supply the IT products and I arrange the work. Laptops through to
            CCTV. Websites through to office setup. You pay after I have
            confirmed the price on WhatsApp. Not before.
          </p>
          <div className="share-actions" style={{ marginTop: 24 }}>
            <AskLink message={site.defaultAskMessage} />
            <Link className="button button-line" href="/shop">
              Open the shop
            </Link>
          </div>
        </div>
      </section>
      <section className="band">
        <div className="wrap">
          <Rule />
        </div>
      </section>
      <section className="sheet" style={{ paddingTop: 12 }}>
        <div className="wrap">
          <div className="section-head">
            <h2>Shop</h2>
            <Link href="/shop">All categories</Link>
          </div>
          <p className="muted" style={{ color: "var(--muted)", marginBottom: 18 }}>
            Each category has its own link. Copy it, put it on a WhatsApp
            status, and a tap opens that category.
          </p>
          <div className="grid cards">
            {categories.slice(0, 4).map((category) => (
              <CategoryCard key={category.slug} category={category} />
            ))}
          </div>
        </div>
      </section>
      <section className="sheet sheet-black">
        <div className="wrap">
          <div className="section-head">
            <h2>Work</h2>
            <Link href="/work">The work I do</Link>
          </div>
          <div className="grid">
            {services.map((service) => (
              <Link key={service.slug} href={`/work#${service.slug}`} className="service" style={{ paddingTop: 18, paddingBottom: 0 }}>
                <h3 style={{ fontSize: "1.45rem", letterSpacing: "-0.04em" }}>{service.title}</h3>
                <p className="muted">{service.summary}</p>
              </Link>
            ))}
          </div>
        </div>
      </section>
      <section className="sheet">
        <div className="wrap">
          <div className="section-head">
            <h2>Proof</h2>
            <Link href="/proof">Open the list</Link>
          </div>
          <p className="muted" style={{ color: "var(--muted)", marginBottom: 8 }}>
            Sites and systems I have shipped. Kept off the shop.
          </p>
          <ProofList items={proof} />
        </div>
      </section>
      <section className="sheet" style={{ paddingTop: 0 }}>
        <div className="wrap home-notes">
          <div>
            <div className="section-head">
              <h2>Trends</h2>
              <Link href="/trends">All notes</Link>
            </div>
            {leadNotes.map((note) => (
              <article key={note.slug} className="note">
                <h3>
                  <Link href={`/trends/${note.slug}`}>{note.title}</Link>
                </h3>
                <p>{note.paragraphs[0]}</p>
              </article>
            ))}
          </div>
          {leadGuide ? (
            <div>
              <div className="section-head">
                <h2>Blog</h2>
                <Link href="/blog">All guides</Link>
              </div>
              <article className="guide-item">
                <h3>
                  <Link href={`/blog/${leadGuide.slug}`}>{leadGuide.title}</Link>
                </h3>
                <p>{leadGuide.excerpt}</p>
              </article>
            </div>
          ) : null}
        </div>
      </section>
    </main>
  );
}
