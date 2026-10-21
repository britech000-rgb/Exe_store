import Link from "next/link";
import { creditFor } from "@/content/media";

type ArticlePhotoProps = {
  src: string;
  alt: string;
  href: string;
  label: string;
};

export function ArticlePhoto({ src, alt, href, label }: ArticlePhotoProps) {
  const credit = creditFor(src);
  const sourceName = credit?.licence.startsWith("CC ") ? "Wikimedia Commons" : "Unsplash";

  return (
    <figure className="article-photo">
      <Link href={href}>
        <img src={src} alt={alt} width={1200} height={630} fetchPriority="high" />
      </Link>
      <figcaption>
        Reference photograph. It does not show EXE STORE premises, staff, or a finished job.{" "}
        <Link href={href}>Open {label}</Link>
        {credit ? (
          <>
            . Photograph source:{" "}
            <a href={credit.source} rel="noreferrer">
              {sourceName}
            </a>
          </>
        ) : null}
        .
      </figcaption>
    </figure>
  );
}
