import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { RelatedLinks } from "@/components/RelatedLinks";
import { ShareRow } from "@/components/ShareRow";
import { notes } from "@/content/notes";
import { getNote, getNotes } from "@/lib/content";

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
  return {
    title: note.title,
    description: note.paragraphs[0],
    alternates: { canonical: `/trends/${note.slug}` },
  };
}

export default async function NotePage({ params }: Props) {
  const { slug } = await params;
  const note = await getNote(slug);
  if (!note) notFound();
  const more = notes.filter((item) => item.slug !== note.slug).slice(0, 3);

  return (
    <main id="content" className="sheet">
      <div className="wrap">
        <p className="kicker">
          <Link href="/trends">Trends</Link>
          {note.advertised ? <span>Advertised</span> : null}
        </p>
        <h1 style={{ fontSize: "clamp(2.4rem, 6vw, 4.4rem)", marginTop: 12 }}>
          {note.title}
        </h1>
        <ShareRow
          message={`EXERIA, I read “${note.title}”. Here is what I need: `}
          path={`/trends/${note.slug}`}
          name={note.title}
        />
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
    </main>
  );
}
