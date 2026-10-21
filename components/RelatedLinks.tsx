import Link from "next/link";

export function RelatedLinks({
  links,
}: {
  links?: { href: string; label: string }[];
}) {
  if (!links?.length) return null;
  return (
    <p className="related">
      {links.map((link) => (
        <Link key={link.href} href={link.href}>
          {link.label}
        </Link>
      ))}
    </p>
  );
}
