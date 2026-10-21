import Link from "next/link";
import { Advertised } from "@/components/Advertised";
import { AskLink } from "@/components/AskLink";
import { CategoryCard } from "@/components/CategoryCard";
import { ProofList } from "@/components/ProofList";
import { Rule } from "@/components/Rule";
import { site } from "@/config/site";
import { proof } from "@/content/proof";
import { categories } from "@/content/shop";
import { services } from "@/content/work";
import { getGuides, getNotes } from "@/lib/content";

export default async function HomePage() {
  const notes = await getNotes();
  const guides = await getGuides();
  const leadNote = notes.find((note) => note.advertised) ?? notes[0];
  const leadGuide = guides.find((guide) => guide.advertised) ?? guides[0];

  return (
    <main id="content">
      <section className="hero">
        <div className="wrap mast">
          <div>
            <p className="kicker">{site.name}</p>
            <h1>
              The brief comes in.
              <span>The price goes out confirmed.</span>
            </h1>
            <p className="lede">
              EXERIA supplies the IT products and arranges the work. Laptops
              through to CCTV. Websites through to office setup. You pay after
              the price is confirmed on WhatsApp. Not before.
            </p>
            <div className="share-actions" style={{ marginTop: 28 }}>
              <AskLink message={site.defaultAskMessage} />
              <Link className="button button-line" href="/shop">
                Open the shop
              </Link>
            </div>
          </div>
          <aside className="mast-panel" aria-label="How it works">
            <p className="index-label">The rule</p>
            <Rule />
          </aside>
        </div>
      </section>
      <section className="sheet">
        <div className="wrap">
          <div className="section-head">
            <div>
              <p className="index-label">Supply</p>
              <h2>Shop</h2>
            </div>
            <Link href="/shop">All eight categories</Link>
          </div>
          <p className="muted standfirst">
            Each category has a photograph, a description, and its own link.
            Copy it onto a WhatsApp status. A tap opens that category.
          </p>
          <div className="grid cards">
            {categories.map((category, index) => (
              <CategoryCard key={category.slug} category={category} index={index} />
            ))}
          </div>
        </div>
      </section>
      <section className="sheet sheet-black">
        <div className="wrap">
          <div className="section-head">
            <div>
              <p className="index-label">On the front</p>
              <h2>Advertised</h2>
            </div>
          </div>
          <p className="muted standfirst standfirst-light">
            One note and one guide sit in front of the archive. The rest of
            the desk is behind them.
          </p>
          <div className="advertised-grid">
            {leadNote ? (
              <Advertised
                eyebrow="Trends"
                title={leadNote.title}
                dek={leadNote.paragraphs[0]}
                href={`/trends/${leadNote.slug}`}
                index="01"
              />
            ) : null}
            {leadGuide ? (
              <Advertised
                eyebrow="Blog"
                title={leadGuide.title}
                dek={leadGuide.excerpt}
                href={`/blog/${leadGuide.slug}`}
                index="01"
              />
            ) : null}
          </div>
        </div>
      </section>
      <section className="sheet sheet-black" style={{ paddingTop: 0 }}>
        <div className="wrap">
          <div className="section-head">
            <h2>Work</h2>
            <Link href="/work">The full list</Link>
          </div>
          <ol className="work-index">
            {services.map((service, index) => (
              <li key={service.slug}>
                <Link href={`/work#${service.slug}`}>
                  <span>0{index + 1}</span>
                  <strong>{service.title}</strong>
                  <em>{service.summary}</em>
                </Link>
              </li>
            ))}
          </ol>
        </div>
      </section>
      <section className="sheet">
        <div className="wrap">
          <div className="section-head">
            <h2>Proof</h2>
            <Link href="/proof">Open the list</Link>
          </div>
          <p className="muted standfirst">
            Sites and systems that have gone live. Kept off the shop.
          </p>
          <ProofList items={proof} />
        </div>
      </section>
    </main>
  );
}
