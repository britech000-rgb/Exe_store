import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { RelatedLinks } from "@/components/RelatedLinks";
import { ShareRow } from "@/components/ShareRow";
import { guides } from "@/content/guides";
import { getGuide, getGuides } from "@/lib/content";

type Props = { params: Promise<{ slug: string }> };

export const revalidate = 60;

export async function generateStaticParams() {
  const items = await getGuides();
  return items.map((guide) => ({ slug: guide.slug }));
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { slug } = await params;
  const guide = await getGuide(slug);
  if (!guide) return { title: "Not found" };
  return {
    title: guide.title,
    description: guide.excerpt,
    alternates: { canonical: `/blog/${guide.slug}` },
  };
}

export default async function GuidePage({ params }: Props) {
  const { slug } = await params;
  const guide = await getGuide(slug);
  if (!guide) notFound();
  const more = guides.filter((item) => item.slug !== guide.slug);

  return (
    <main id="content" className="sheet">
      <div className="wrap">
        <p className="kicker">
          <Link href="/blog">Blog</Link>
        </p>
        <h1 style={{ fontSize: "clamp(2.4rem, 6vw, 4.6rem)", marginTop: 12, maxWidth: "16ch" }}>
          {guide.title}
        </h1>
        <p className="lede" style={{ color: "var(--muted)" }}>{guide.excerpt}</p>
        <ShareRow
          message={`Brian, I read “${guide.title}”. Here is what I need: `}
          path={`/blog/${guide.slug}`}
          name={guide.title}
        />
        <div className="prose">
          {guide.paragraphs.map((paragraph) => (
            <p key={paragraph}>{paragraph}</p>
          ))}
        </div>
        <RelatedLinks links={guide.related} />
        <div style={{ marginTop: 40 }}>
          <h2>More guides</h2>
          <p className="related">
            {more.map((item) => (
              <Link key={item.slug} href={`/blog/${item.slug}`}>
                {item.title}
              </Link>
            ))}
          </p>
        </div>
      </div>
    </main>
  );
}
