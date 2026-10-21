import type { Metadata } from "next";
import { RelatedLinks } from "@/components/RelatedLinks";
import { ShareRow } from "@/components/ShareRow";
import { services } from "@/content/work";

export const metadata: Metadata = {
  title: "Work",
  description:
    "Websites, Wi-Fi and cabling, CCTV, repairs, office setup, and business systems. I check the job, then I confirm the price on WhatsApp before you pay.",
  alternates: { canonical: "/work" },
};

export default function WorkPage() {
  return (
    <main id="content">
      <section className="sheet sheet-black" style={{ paddingBottom: 20 }}>
        <div className="wrap">
          <p className="kicker">Work</p>
          <h1 style={{ marginTop: 10, maxWidth: "14ch" }}>Work, arranged.</h1>
          <p className="lede">
            You say what you need. I check the job. I confirm the price on
            WhatsApp before you pay. Gear is in the shop. This page is the work.
          </p>
          <nav className="jump" aria-label="Work">
            {services.map((service) => (
              <a key={service.slug} href={`#${service.slug}`}>
                {service.title}
              </a>
            ))}
          </nav>
        </div>
      </section>
      <section className="sheet sheet-black" style={{ paddingTop: 0 }}>
        <div className="wrap">
          {services.map((service) => (
            <article key={service.slug} id={service.slug} className="service">
              <h2>{service.title}</h2>
              <p className="lede" style={{ marginTop: 8 }}>{service.summary}</p>
              <p style={{ marginTop: 14, maxWidth: "42rem" }}>{service.detail}</p>
              <h3 style={{ marginTop: 22, fontSize: "1.2rem" }}>Tell me this</h3>
              <ul className="list">
                {service.tellMe.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
              <ShareRow
                message={service.askMessage}
                path={`/work#${service.slug}`}
                name={service.title}
              />
              <RelatedLinks
                links={service.related ? [service.related] : undefined}
              />
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}
