import type { Metadata } from "next";
import Link from "next/link";
import { Advertised } from "@/components/Advertised";
import { getGuides } from "@/lib/content";

export const revalidate = 60;

export const metadata: Metadata = {
  title: "Blog",
  description:
    "Longer guides from EXERIA. One is advertised on the front. The rest sit in the index.",
  alternates: { canonical: "/blog" },
};

export default async function BlogPage() {
  const guides = await getGuides();
  const lead = guides.find((guide) => guide.advertised) ?? guides[0];

  return (
    <main id="content">
      <section className="sheet sheet-black page-intro">
        <div className="wrap">
          <p className="kicker">Blog</p>
          <h1>One guide on the front. The rest in the index.</h1>
          <p className="lede">
            Read the advertised guide before you send the brief. It says what
            is needed so the check can happen, and the price can be confirmed
            on WhatsApp.
          </p>
        </div>
      </section>
      {lead ? (
        <section className="sheet advert-slot">
          <div className="wrap">
            <Advertised
              eyebrow="Guide"
              title={lead.title}
              dek={lead.excerpt}
              href={`/blog/${lead.slug}`}
              index="01"
            />
          </div>
        </section>
      ) : null}
      <section className="sheet" style={{ paddingTop: 0 }}>
        <div className="wrap">
          <h2>Index</h2>
          <ol className="archive">
            {guides.map((guide, index) => (
              <li key={guide.slug}>
                <span>{String(index + 1).padStart(2, "0")}</span>
                <div>
                  {guide.advertised ? <em>Advertised</em> : <em>Guide</em>}
                  <h3>
                    <Link href={`/blog/${guide.slug}`}>{guide.title}</Link>
                  </h3>
                  <p>{guide.excerpt}</p>
                </div>
              </li>
            ))}
          </ol>
        </div>
      </section>
    </main>
  );
}
