import Link from "next/link";

type AdvertisedProps = {
  eyebrow: string;
  title: string;
  dek: string;
  href: string;
  index: string;
};

export function Advertised({ eyebrow, title, dek, href, index }: AdvertisedProps) {
  return (
    <article className="advertised">
      <div className="advertised-top">
        <span>Advertised</span>
        <span>{eyebrow}</span>
        <span>{index}</span>
      </div>
      <h2>
        <Link href={href}>{title}</Link>
      </h2>
      <p>{dek}</p>
      <Link className="button" href={href}>
        Read it
      </Link>
    </article>
  );
}
