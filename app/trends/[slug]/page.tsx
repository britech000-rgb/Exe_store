import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { ArticlePhoto } from "@/components/ArticlePhoto";
import { RelatedLinks } from "@/components/RelatedLinks";
import { ShareMenu } from "@/components/ShareMenu";
import { AskLink } from "@/components/AskLink";
import { notes } from "@/content/notes";
import { getNote, getNotes } from "@/lib/content";
import { articleShareMeta, shareImage } from "@/lib/share-image";
import { getSiteUrl } from "@/lib/site-url";

type Props = { params: Promise<{ slug: string }> };

export const revalidate = 60;

export async function generateStaticParams() {
  const items = await getNotes();
  return items.map((note) => ({ slug: note.slug }));
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { slug } = await params;
  const note = await getNote(slug);
  if (!note) return { title: "Not found" };
  return articleShareMeta({
    title: note.title,
    description: note.paragraphs[0],
    path: `/trends/${note.slug}`,
    image: shareImage("trends", note.slug),
    alt: `${note.title}. EXE STORE Trends.`,
  });
}

export default async function NotePage({ params }: Props) {
  const { slug } = await params;
  const note = await getNote(slug);
  if (!note) notFound();
  const more = notes.filter((item) => item.slug !== note.slug).slice(0, 3);
  const card = shareImage("trends", note.slug);
  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "BlogPosting",
    headline: note.title,
    description: note.paragraphs[0],
    image: `${getSiteUrl()}${card}`,
    mainEntityOfPage: `${getSiteUrl()}/trends/${note.slug}`,
    publisher: { "@type": "Organization", name: "EXE STORE", url: getSiteUrl() },
  };

  return (
    <main id="content" className="sheet">
      <div className="wrap article">
        <p className="crumbs">
          <Link href="/">Home</Link>
          <span aria-hidden="true">/</span>
          <Link href="/blog">Blog & Insights</Link>
          <span aria-hidden="true">/</span>
          <Link href="/trends">Trends</Link>
        </p>
        <p className="kicker">
          <Link href="/trends">Trends</Link>
          {note.advertised ? <span>Advertised</span> : null}
        </p>
        <h1>{note.title}</h1>
        <div className="share-actions">
          <AskLink message={`EXE STORE, I read “${note.title}”. Here is what I need: `} />
          <ShareMenu path={`/trends/${note.slug}`} name={note.title} social />
        </div>
        {note.image && note.imageAlt && note.photoHref && note.photoLabel ? (
          <ArticlePhoto
            src={note.image}
            alt={note.imageAlt}
            href={note.photoHref}
            label={note.photoLabel}
          />
        ) : null}
        <div className="prose">
          {note.paragraphs.map((paragraph) => (
            <p key={paragraph}>{paragraph}</p>
          ))}
        </div>
        <RelatedLinks links={note.related ? [note.related] : undefined} />
        <div style={{ marginTop: 40 }}>
          <h2>More notes</h2>
          <p className="related">
            {more.map((item) => (
              <Link key={item.slug} href={`/trends/${item.slug}`}>
                {item.title}
              </Link>
            ))}
          </p>
        </div>
      </div>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />
    </main>
  );
}
