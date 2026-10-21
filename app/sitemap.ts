import type { MetadataRoute } from "next";
import { guides } from "@/content/guides";
import { notes } from "@/content/notes";
import { categories } from "@/content/shop";
import { services } from "@/content/work";
import { getSiteUrl } from "@/lib/site-url";

export default function sitemap(): MetadataRoute.Sitemap {
  const origin = getSiteUrl();
  const paths = [
    "",
    "/shop",
    "/work",
    ...services.map((service) => `/work/${service.slug}`),
    "/proof",
    "/trends",
    "/blog",
    "/contact",
    ...categories.map((category) => `/shop/${category.slug}`),
    ...notes.map((note) => `/trends/${note.slug}`),
    ...guides.map((guide) => `/blog/${guide.slug}`),
  ];
  return paths.map((path) => ({
    url: `${origin}${path}`,
    changeFrequency: path === "" ? "weekly" : "monthly",
    priority: path === "" ? 1 : 0.7,
  }));
}
