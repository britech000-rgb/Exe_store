import Link from "next/link";
import { CopyLink } from "@/components/CopyLink";
import type { Category } from "@/content/shop";

export function CategoryCard({
  category,
  index,
}: {
  category: Category;
  index?: number;
}) {
  const path = `/shop/${category.slug}`;
  const label = index === undefined ? null : String(index + 1).padStart(2, "0");

  return (
    <article className="card">
      <Link href={path} className="card-media">
        <img src={category.image} alt={category.imageAlt} width={1600} height={1067} />
      </Link>
      <div className="card-body">
        {label ? <p className="index-label">{label}</p> : null}
        <h2>
          <Link href={path}>{category.title}</Link>
        </h2>
        <p>{category.summary}</p>
        <div className="card-actions">
          <CopyLink path={path} name={category.title} />
          <Link className="button button-line" href={path}>
            Open
          </Link>
        </div>
      </div>
    </article>
  );
}
