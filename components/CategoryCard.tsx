import Link from "next/link";
import { CopyLink } from "@/components/CopyLink";
import type { Category } from "@/content/shop";

export function CategoryCard({ category }: { category: Category }) {
  const path = `/shop/${category.slug}`;
  return (
    <article className="card">
      <Link href={path} className="card-media">
        <img
          src={category.image}
          alt={category.imageAlt}
          width={1200}
          height={750}
        />
      </Link>
      <div className="card-body">
        <h2>
          <Link href={path}>{category.title}</Link>
        </h2>
        <p>{category.summary}</p>
        <div className="card-actions">
          <CopyLink path={path} name={category.title} />
          <Link className="button button-line" href={path}>
            Open {category.title}
          </Link>
        </div>
      </div>
    </article>
  );
}
