import Link from "next/link";
import { AskLink } from "@/components/AskLink";
import { site } from "@/config/site";
import { digits, whatsappReady } from "@/lib/links";

export function SiteFooter() {
  const ready = whatsappReady();
  return (
    <footer className="site-footer">
      <div className="wrap footer-grid">
        <div>
          <p className="wordmark footer-mark">
            <span className="mark" aria-hidden="true">
              <i />
              <i />
              <i />
            </span>
            {site.name}
          </p>
          <p className="footer-name">{site.name}</p>
          <p>
            IT products and arranged work. The price is confirmed on WhatsApp
            before you pay.
          </p>
        </div>
        <div className="footer-links">
          <Link href="/shop">Shop</Link>
          <Link href="/work">Work</Link>
          <Link href="/proof">Proof</Link>
          <Link href="/trends">Trends</Link>
          <Link href="/blog">Blog</Link>
          <Link href="/contact">Contact</Link>
        </div>
        <div className="footer-links">
          <a href={site.instagram} target="_blank" rel="noopener noreferrer me">
            Instagram {site.instagramHandle}
          </a>
          {ready ? (
            <p className="footer-number">+{digits(site.whatsappNumber)}</p>
          ) : (
            <p>WhatsApp opens here once the number is in.</p>
          )}
          <AskLink message={site.defaultAskMessage} />
        </div>
      </div>
    </footer>
  );
}
