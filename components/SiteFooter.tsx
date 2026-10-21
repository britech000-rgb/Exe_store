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
          <p className="footer-name">{site.name}</p>
          <p>
            I supply IT products and I arrange the work. The price is confirmed
            on WhatsApp before you pay.
          </p>
        </div>
        <div className="footer-links">
          <a href={site.instagram} target="_blank" rel="noopener noreferrer me">
            Instagram {site.instagramHandle}
          </a>
          {ready ? (
            <p className="footer-number">+{digits(site.whatsappNumber)}</p>
          ) : (
            <p>WhatsApp opens here once the number is in. Until then, ask on Instagram.</p>
          )}
          <AskLink message={site.defaultAskMessage} />
          <Link href="/shop">Shop</Link>
          <Link href="/proof">Proof</Link>
        </div>
      </div>
    </footer>
  );
}
