import Link from "next/link";

type AdvertisedProps = {
  eyebrow: string;
  title: string;
  dek: string;
  href: string;
  index: string;
  image?: string;
  imageAlt?: string;
};

export function Advertised({ eyebrow, title, dek, href, index, image, imageAlt }: AdvertisedProps) {
  return (
    <article className={image ? "advertised advertised-photo" : "advertised"}>
      {image ? (
        <Link className="advertised-media" href={href} aria-label={title}>
          <img src={image} alt={imageAlt ?? ""} width={1200} height={630} fetchPriority="high" />
        </Link>
      ) : null}
      <div>
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
      </div>
    </article>
  );
}
