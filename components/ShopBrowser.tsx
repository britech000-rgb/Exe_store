"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useMemo } from "react";
import { CategoryCard } from "@/components/CategoryCard";
import type { Category } from "@/content/shop";

export function ShopBrowser({ categories }: { categories: Category[] }) {
  const params = useSearchParams();
  const router = useRouter();
  const query = params.get("q")?.trim() ?? "";
  const sort = params.get("sort") === "name" ? "name" : "catalogue";
  const selected = (params.get("cat") ?? "")
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);

  const results = useMemo(() => {
    const q = query.toLowerCase();
    let list = categories.filter((category) => {
      const haystack = `${category.title} ${category.summary}`.toLowerCase();
      const matchesQuery = !q || haystack.includes(q);
      const matchesCat = selected.length === 0 || selected.includes(category.slug);
      return matchesQuery && matchesCat;
    });
    if (sort === "name") {
      list = [...list].sort((a, b) => a.title.localeCompare(b.title));
    }
    return list;
  }, [categories, query, selected, sort]);

  function write(next: { q?: string; sort?: string; cat?: string[] }) {
    const search = new URLSearchParams();
    const q = next.q ?? query;
    const nextSort = next.sort ?? sort;
    const cat = next.cat ?? selected;
    if (q) search.set("q", q);
    if (nextSort === "name") search.set("sort", "name");
    if (cat.length) search.set("cat", cat.join(","));
    const href = search.size ? `/shop?${search.toString()}` : "/shop";
    router.replace(href, { scroll: false });
  }

  function toggle(slug: string) {
    const cat = selected.includes(slug)
      ? selected.filter((item) => item !== slug)
      : [...selected, slug];
    write({ cat });
  }

  return (
    <div className="catalogue">
      <form
        className="catalogue-tools"
        role="search"
        onSubmit={(event) => {
          event.preventDefault();
          const data = new FormData(event.currentTarget);
          write({ q: String(data.get("q") ?? "") });
        }}
      >
        <label htmlFor="shop-search">Search the catalogue</label>
        <div className="catalogue-search">
          <input
            id="shop-search"
            name="q"
            defaultValue={query}
            key={query}
            placeholder="Laptops, CCTV, chargers"
            autoComplete="off"
          />
          <button className="button" type="submit">
            Search
          </button>
        </div>
        <label htmlFor="shop-sort">Sort</label>
        <select
          id="shop-sort"
          value={sort}
          onChange={(event) => write({ sort: event.target.value })}
        >
          <option value="catalogue">Catalogue order</option>
          <option value="name">Name</option>
        </select>
      </form>
      <div className="filter-row" aria-label="Categories">
        {categories.map((category) => {
          const on = selected.includes(category.slug);
          return (
            <button
              key={category.slug}
              type="button"
              className={on ? "chip is-on" : "chip"}
              aria-pressed={on}
              onClick={() => toggle(category.slug)}
            >
              {category.title}
            </button>
          );
        })}
      </div>
      {selected.length || query ? (
        <p className="active-filters">
          {query ? <span>Search: {query}</span> : null}
          {selected.map((slug) => {
            const category = categories.find((item) => item.slug === slug);
            return (
              <button key={slug} type="button" onClick={() => toggle(slug)}>
                Remove {category?.title ?? slug}
              </button>
            );
          })}
          <button type="button" onClick={() => write({ q: "", cat: [], sort: "catalogue" })}>
            Clear all
          </button>
        </p>
      ) : null}
      {results.length === 0 ? (
        <div className="empty">
          <h2>Nothing matches.</h2>
          <p>Try another word, or clear the filters. The catalogue is the eight categories on this page.</p>
          <button type="button" className="button" onClick={() => write({ q: "", cat: [] })}>
            Clear filters
          </button>
        </div>
      ) : (
        <div className="grid cards">
          {results.map((category) => (
            <CategoryCard
              key={category.slug}
              category={category}
              index={categories.findIndex((item) => item.slug === category.slug)}
            />
          ))}
        </div>
      )}
    </div>
  );
}
