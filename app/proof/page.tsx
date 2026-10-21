import type { Metadata } from "next";
import { ProofList } from "@/components/ProofList";
import { proof } from "@/content/proof";

export const metadata: Metadata = {
  title: "Proof",
  description:
    "Sites and systems Brian Kuria has shipped. Separate from the shop. Names, one sentence, and a link.",
  alternates: { canonical: "/proof" },
};

export default function ProofPage() {
  return (
    <main id="content" className="sheet">
      <div className="wrap proof-page">
        <p className="kicker">Proof</p>
        <h1 style={{ marginTop: 10, maxWidth: "14ch" }}>Work you can open.</h1>
        <p className="lede" style={{ color: "var(--muted)" }}>
          Sites and systems I have shipped. This page is not the shop. A name,
          one sentence, and a link. That is all.
        </p>
        <ProofList items={proof} />
      </div>
    </main>
  );
}
