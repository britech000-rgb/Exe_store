import type { Metadata } from "next";
import { AskLink } from "@/components/AskLink";
import { site } from "@/config/site";
import { digits, whatsappReady } from "@/lib/links";

export const metadata: Metadata = {
  title: "Contact",
  description:
    "Send EXERIA the brief on WhatsApp. The price is confirmed there before you pay.",
  alternates: { canonical: "/contact" },
};

const brief = [
  "The work, in plain words",
  "The model, read off the machine",
  "The room, if the job is a place",
  "What failed last time",
];

export default function ContactPage() {
  const ready = whatsappReady();

  return (
    <main id="content">
      <section className="sheet sheet-black contact-hero">
        <div className="wrap">
          <p className="kicker">Contact</p>
          <h1>Send the brief. The price comes back confirmed.</h1>
          <p className="lede">
            EXERIA checks what you send, then confirms the price on WhatsApp
            before you pay. Nothing is bought before that confirmation.
          </p>
        </div>
      </section>
      <section className="sheet contact-sheet">
        <div className="wrap contact-board">
          <article className="contact-card contact-card-lead">
            <p className="index-label">01 · WhatsApp</p>
            <h2>The desk</h2>
            {ready ? (
              <p className="contact-number">+{digits(site.whatsappNumber)}</p>
            ) : (
              <p>The number is not in yet. Use Instagram until it is.</p>
            )}
            <p>
              Write what you need in one message. A reply comes back with what
              was checked, and the price, before you pay.
            </p>
            <AskLink message={site.defaultAskMessage} />
          </article>
          <article className="contact-card">
            <p className="index-label">02 · Instagram</p>
            <h2>The other door</h2>
            <p>
              Status links for a category open that category. The profile is
              there when WhatsApp is not the way you start.
            </p>
            <a
              className="button button-line"
              href={site.instagram}
              target="_blank"
              rel="noopener noreferrer me"
            >
              {site.instagramHandle}
            </a>
          </article>
          <article className="contact-card contact-card-wide">
            <p className="index-label">03 · What to send</p>
            <h2>A short brief is enough.</h2>
            <ul className="list">
              {brief.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </article>
        </div>
      </section>
    </main>
  );
}
