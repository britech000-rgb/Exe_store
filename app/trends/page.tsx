import type { Metadata } from "next";
import Link from "next/link";
import { ShareRow } from "@/components/ShareRow";
import { getNotes } from "@/lib/content";

export const revalidate = 60;

export const metadata: Metadata = {
  title: "Trends",
  description: "Short notes from Brian Kuria on gear, briefs, and the work.",
  alternates: { canonical: "/trends" },
};

export default async function TrendsPage() {
  const notes = await getNotes();
  return (
    <main id="content" className="sheet">
      <div className="wrap">
        <p className="kicker">Trends</p>
        <h1 style={{ marginTop: 10 }}>Short notes.</h1>
        <p className="lede" style={{ color: "var(--muted)" }}>
          The thing I would tell you before you send the brief. Each note has
          its own link.
        </p>
        <div style={{ marginTop: 12 }}>
          {notes.map((note) => (
            <article key={note.slug} className="note">
              <h2>
                <Link href={`/trends/${note.slug}`}>{note.title}</Link>
              </h2>
              {note.paragraphs.map((paragraph) => (
                <p key={paragraph} style={{ marginTop: 8 }}>
                  {paragraph}
                </p>
              ))}
              <ShareRow
                message={`Brian, I read “${note.title}”. Here is what I need: `}
                path={`/trends/${note.slug}`}
                name={note.title}
              />
            </article>
          ))}
        </div>
      </div>
    </main>
  );
}
