import type { Metadata } from "next";
import Link from "next/link";
import { getGuides } from "@/lib/content";

export const revalidate = 60;

export const metadata: Metadata = {
  title: "Blog",
  description:
    "Longer guides from Brian Kuria. How to send a brief, what to sort before CCTV, and the order for a small office.",
  alternates: { canonical: "/blog" },
};

export default async function BlogPage() {
  const guides = await getGuides();
  return (
    <main id="content" className="sheet">
      <div className="wrap">
        <p className="kicker">Blog</p>
        <h1 style={{ marginTop: 10 }}>Longer guides.</h1>
        <p className="lede" style={{ color: "var(--muted)" }}>
          Read this before you send the brief. It tells you what I need so I
          can check, then confirm the price on WhatsApp.
        </p>
        <div style={{ marginTop: 18 }}>
          {guides.map((guide) => (
            <article key={guide.slug} className="guide-item">
              <h2>
                <Link href={`/blog/${guide.slug}`}>{guide.title}</Link>
              </h2>
              <p>{guide.excerpt}</p>
            </article>
          ))}
        </div>
      </div>
    </main>
  );
}
