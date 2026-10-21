import Link from "next/link";
import { ShareMenu } from "@/components/ShareMenu";
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
      <Link href={path} className="card-media" aria-label={category.title}>
        <img src={category.image} alt={category.imageAlt} width={1600} height={1067} />
      </Link>
      <div className="card-body">
        {label ? <p className="index-label">{label}</p> : null}
        <h2>
          <Link href={path}>{category.title}</Link>
        </h2>
        <p>{category.summary}</p>
        <p className="price-note">Quote. Confirmed on WhatsApp before you pay.</p>
        <div className="card-actions">
          <Link className="button" href={path}>
            View
          </Link>
          <ShareMenu path={path} name={category.title} />
        </div>
      </div>
    </article>
  );
}
