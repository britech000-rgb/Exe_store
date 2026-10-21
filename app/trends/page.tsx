import type { Metadata } from "next";
import Link from "next/link";
import { Advertised } from "@/components/Advertised";
import { getNotes } from "@/lib/content";

export const revalidate = 60;

export const metadata: Metadata = {
  title: "Trends",
  description:
    "Short notes from EXE STORE. One is advertised on the front. The rest sit in the index.",
  alternates: { canonical: "/trends" },
};

export default async function TrendsPage() {
  const notes = await getNotes();
  const lead = notes.find((note) => note.advertised) ?? notes[0];

  return (
    <main id="content">
      <section className="sheet sheet-black page-intro">
        <div className="wrap">
          <p className="kicker">Trends</p>
          <h1>One note on the front. The rest in the index.</h1>
          <p className="lede">
            The advertised note is the one to read before the brief. Each note
            still has its own link.
          </p>
        </div>
      </section>
      {lead ? (
        <section className="sheet advert-slot">
          <div className="wrap">
            <Advertised
              eyebrow="Trend"
              title={lead.title}
              dek={lead.paragraphs[0]}
              href={`/trends/${lead.slug}`}
              index="01"
            />
          </div>
        </section>
      ) : null}
      <section className="sheet" style={{ paddingTop: 0 }}>
        <div className="wrap">
          <h2>Index</h2>
          <ol className="archive">
            {notes.map((note, index) => (
              <li key={note.slug}>
                <span>{String(index + 1).padStart(2, "0")}</span>
                <div>
                  {note.advertised ? <em>Advertised</em> : <em>Note</em>}
                  <h3>
                    <Link href={`/trends/${note.slug}`}>{note.title}</Link>
                  </h3>
                  <p>{note.paragraphs[0]}</p>
                </div>
              </li>
            ))}
          </ol>
        </div>
      </section>
    </main>
  );
}
