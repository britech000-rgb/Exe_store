import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { AskLink } from "@/components/AskLink";
import { getServicePage, servicePages } from "@/content/service-pages";
import { categories } from "@/content/shop";
import { getService, services } from "@/content/work";
import { breadcrumbLd, jsonLdScript } from "@/lib/seo";
import { absoluteUrl } from "@/lib/site-url";

type Props = { params: Promise<{ slug: string }> };

export function generateStaticParams() {
  return services.map((service) => ({ slug: service.slug }));
}

export const dynamicParams = false;

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { slug } = await params;
  const service = getService(slug);
  if (!service) return { title: "Not found" };
  const page = getServicePage(slug);
  const title = categories.some((category) => category.title === service.title)
    ? `${service.title} work`
    : service.title;
  return {
    title,
    description: service.summary,
    alternates: { canonical: `/work/${service.slug}` },
    openGraph: {
      title,
      description: service.summary,
      url: `/work/${service.slug}`,
      images: page ? [{ url: page.image, alt: page.imageAlt }] : undefined,
    },
  };
}

export default async function ServicePage({ params }: Props) {
  const { slug } = await params;
  const service = getService(slug);
  const page = getServicePage(slug);
  if (!service || !page) notFound();
  const path = `/work/${service.slug}`;
  const jsonLd = [
    {
      "@context": "https://schema.org",
      "@type": "Service",
      name: service.title,
      description: service.summary,
      url: absoluteUrl(path),
      image: absoluteUrl(page.image),
      provider: { "@type": "Organization", name: "EXE STORE", url: absoluteUrl("/") },
    },
    {
      "@context": "https://schema.org",
      "@type": "FAQPage",
      mainEntity: page.faqs.map((faq) => ({
        "@type": "Question",
        name: faq.question,
        acceptedAnswer: { "@type": "Answer", text: faq.answer },
      })),
    },
    breadcrumbLd([
      { name: "Home", path: "/" },
      { name: "Services", path: "/work" },
      { name: service.title, path },
    ]),
  ];

  return (
    <main id="content" className="sheet">
      <div className="wrap service-page">
        <p className="crumbs">
          <Link href="/">Home</Link>
          <span aria-hidden="true">/</span>
          <Link href="/work">Services</Link>
          <span aria-hidden="true">/</span>
          {service.title}
        </p>
        <div className="product">
          <figure className="preview">
            <img src={page.image} alt={page.imageAlt} width={1600} height={1067} />
            <figcaption>Reference photograph. Not a record of an EXE STORE job.</figcaption>
          </figure>
          <div className="product-buy">
            <p className="kicker">Service</p>
            <h1>{service.title}</h1>
            <p className="lede">{service.summary}</p>
            <p>{service.detail}</p>
            <AskLink message={service.askMessage} />
          </div>
        </div>
        <div className="split product-detail">
          <section>
            <h2>Who it is for</h2>
            <p>{page.suitableFor}</p>
          </section>
          <section>
            <h2>The problem</h2>
            <p>{page.solves}</p>
          </section>
          <section>
            <h2>Included</h2>
            <ul className="list">
              {page.included.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </section>
          <section>
            <h2>What you send</h2>
            <ul className="list">
              {service.tellMe.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </section>
          <section>
            <h2>How it works</h2>
            <ol className="list">
              {page.process.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ol>
          </section>
          <section>
            <h2>Price, timing, coverage</h2>
            <p>The price is confirmed on WhatsApp before you pay. A price list is not published.</p>
            <p>No turnaround is published. It is confirmed with the quote.</p>
            <p>No coverage list is published. Say the place in the brief.</p>
          </section>
        </div>
        <section className="faq">
          <h2>Questions</h2>
          {page.faqs.map((item) => (
            <details key={item.question}>
              <summary>{item.question}</summary>
              <p>{item.answer}</p>
            </details>
          ))}
        </section>
        {service.related ? (
          <p className="related">
            <Link href={service.related.href}>{service.related.label}</Link>
          </p>
        ) : null}
        <p className="related">
          {servicePages
            .filter((item) => item.slug !== service.slug)
            .slice(0, 3)
            .map((item) => (
              <Link key={item.slug} href={`/work/${item.slug}`}>
                {services.find((serviceItem) => serviceItem.slug === item.slug)?.title}
              </Link>
            ))}
        </p>
      </div>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: jsonLdScript(jsonLd) }} />
    </main>
  );
}
