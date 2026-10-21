import type { MetadataRoute } from "next";
import { guides } from "@/content/guides";
import { notes } from "@/content/notes";
import { categories } from "@/content/shop";
import { services } from "@/content/work";
import { getSiteUrl } from "@/lib/site-url";

export default function sitemap(): MetadataRoute.Sitemap {
  const origin = getSiteUrl();
  const dated = new Map<string, Date>();
  for (const guide of guides) {
    if (guide.publishedAt) dated.set(`/blog/${guide.slug}`, new Date(guide.publishedAt));
  }
  for (const note of notes) {
    if (note.publishedAt) dated.set(`/trends/${note.slug}`, new Date(note.publishedAt));
  }

  const entries: { path: string; priority: number; changeFrequency: "weekly" | "monthly" }[] = [
    { path: "", priority: 1, changeFrequency: "weekly" },
    { path: "/shop", priority: 0.9, changeFrequency: "weekly" },
    { path: "/work", priority: 0.8, changeFrequency: "monthly" },
    { path: "/blog", priority: 0.8, changeFrequency: "weekly" },
    { path: "/trends", priority: 0.6, changeFrequency: "weekly" },
    { path: "/contact", priority: 0.5, changeFrequency: "monthly" },
    { path: "/proof", priority: 0.4, changeFrequency: "monthly" },
    ...categories.map((category) => ({
      path: `/shop/${category.slug}`,
      priority: 0.8,
      changeFrequency: "monthly" as const,
    })),
    ...services.map((service) => ({
      path: `/work/${service.slug}`,
      priority: 0.7,
      changeFrequency: "monthly" as const,
    })),
    ...guides.map((guide) => ({
      path: `/blog/${guide.slug}`,
      priority: 0.6,
      changeFrequency: "monthly" as const,
    })),
    ...notes.map((note) => ({
      path: `/trends/${note.slug}`,
      priority: 0.5,
      changeFrequency: "monthly" as const,
    })),
  ];

  return entries.map((entry) => {
    const lastModified = dated.get(entry.path);
    return {
      url: `${origin}${entry.path}`,
      changeFrequency: entry.changeFrequency,
      priority: entry.priority,
      ...(lastModified ? { lastModified } : {}),
    };
  });
}
