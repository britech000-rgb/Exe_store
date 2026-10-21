"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import { AskLink } from "@/components/AskLink";
import { site } from "@/config/site";

const links = [
  { href: "/shop", label: "Shop" },
  { href: "/work", label: "Work" },
  { href: "/trends", label: "Trends" },
  { href: "/blog", label: "Blog" },
];

export function SiteHeader() {
  const pathname = usePathname();
  const [open, setOpen] = useState(false);

  useEffect(() => {
    setOpen(false);
  }, [pathname]);

  return (
    <header className="site-header">
      <div className="wrap header-bar">
        <Link href="/" className="wordmark">
          <span className="mark" aria-hidden="true" />
          {site.name}
        </Link>
        <button
          type="button"
          className="menu-button"
          aria-expanded={open}
          aria-controls="site-nav"
          onClick={() => setOpen((value) => !value)}
        >
          {open ? "Close" : "Menu"}
        </button>
        <nav id="site-nav" className={open ? "site-nav is-open" : "site-nav"}>
          {links.map((link) => {
            const active =
              pathname === link.href || pathname.startsWith(`${link.href}/`);
            return (
              <Link
                key={link.href}
                href={link.href}
                aria-current={active ? "page" : undefined}
              >
                {link.label}
              </Link>
            );
          })}
        </nav>
        <AskLink message={site.defaultAskMessage} compact className="button header-ask" />
      </div>
    </header>
  );
}
