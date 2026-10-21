import type { Metadata } from "next";
import Link from "next/link";
import { getServicePage } from "@/content/service-pages";
import { services } from "@/content/work";

export const metadata: Metadata = {
  title: "Services",
  description:
    "Websites, Wi-Fi and cabling, CCTV, repairs, office setup, and business systems. The price is confirmed on WhatsApp before you pay.",
  alternates: { canonical: "/work" },
};

export default function WorkPage() {
  return (
    <main id="content">
      <section className="sheet page-intro">
        <div className="wrap">
          <p className="kicker">Services</p>
          <h1>Work, arranged.</h1>
          <p className="lede">
            You say what you need. The job is checked. The price is confirmed
            on WhatsApp before you pay. Gear is in the shop.
          </p>
          <div className="grid cards">
            {services.map((service) => {
              const page = getServicePage(service.slug);
              return (
                <article key={service.slug} id={service.slug} className="card">
                  {page ? (
                    <Link href={`/work/${service.slug}`} className="card-media" aria-label={service.title}>
                      <img src={page.image} alt={page.imageAlt} width={1600} height={1067} />
                    </Link>
                  ) : null}
                  <div className="card-body">
                    <h2>
                      <Link href={`/work/${service.slug}`}>{service.title}</Link>
                    </h2>
                    <p>{service.summary}</p>
                    <div className="card-actions">
                      <Link className="button" href={`/work/${service.slug}`}>
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
    </main>
  );
}
