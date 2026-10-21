import Link from "next/link";

type StoryCardProps = {
  href: string;
  title: string;
  summary: string;
  category: string;
  meta?: string;
  image?: string;
  imageAlt?: string;
  lead?: boolean;
  heading?: "h2" | "h3";
};

export function StoryCard({
  href,
  title,
  summary,
  category,
  meta,
  image,
  imageAlt,
  lead = false,
  heading = "h3",
}: StoryCardProps) {
  const Title = heading;
  return (
    <article className={lead ? "story story-lead" : "story"}>
      {image ? (
        <Link className="story-photo" href={href} aria-label={title}>
          <img
            src={image}
            alt={imageAlt ?? ""}
            width={1200}
            height={630}
            loading={lead ? "eager" : "lazy"}
            fetchPriority={lead ? "high" : "auto"}
          />
        </Link>
      ) : null}
      <p className="index-label">{category}</p>
      <Title>
        <Link href={href}>{title}</Link>
      </Title>
      <p>{summary}</p>
      {meta ? <p className="meta">{meta}</p> : null}
    </article>
  );
}
