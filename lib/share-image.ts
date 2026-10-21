import "server-only";
import { existsSync } from "node:fs";
import path from "node:path";
import type { Metadata } from "next";

export function shareImage(section: "blog" | "trends" | "shop", slug?: string) {
  const named = slug ? `/social/${section}-${slug}.jpg` : `/social/${section}-index.jpg`;
  const disk = path.join(process.cwd(), "public", named.replace(/^\//, ""));
  if (existsSync(disk)) return named;
  return `/social/${section}-default.jpg`;
}

export function articleShareMeta(input: {
  title: string;
  description: string;
  path: string;
  image: string;
  alt: string;
}): Metadata {
  return {
    title: input.title,
    description: input.description,
    alternates: { canonical: input.path },
    openGraph: {
      type: "article",
      url: input.path,
      title: input.title,
      description: input.description,
      images: [
        {
          url: input.image,
          width: 1200,
          height: 630,
          alt: input.alt,
          type: "image/jpeg",
        },
      ],
    },
    twitter: {
      card: "summary_large_image",
      title: input.title,
      description: input.description,
      images: [
        {
          url: input.image,
          width: 1200,
          height: 630,
          alt: input.alt,
        },
      ],
    },
  };
}
