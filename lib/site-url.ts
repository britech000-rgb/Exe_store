const PRODUCTION_URL = "https://exestore-red.vercel.app";

/**
 * One public host for canonicals, the sitemap, and structured data.
 * Preview deploys use the same host so they cannot become a second indexed site.
 */
export function getSiteUrl() {
  const configured = process.env.NEXT_PUBLIC_SITE_URL?.trim();
  if (configured) return configured.replace(/\/$/, "");
  if (process.env.VERCEL) return PRODUCTION_URL;
  return "http://localhost:3000";
}

export function absoluteUrl(path: string) {
  if (path.startsWith("http://") || path.startsWith("https://")) return path;
  return `${getSiteUrl()}${path.startsWith("/") ? path : `/${path}`}`;
}
