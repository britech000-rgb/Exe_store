import { getCategory } from "@/content/shop";
import { ogContentType, ogImage, ogSize } from "@/lib/og";

export const alt = "Shop category. EXERIA.";
export const size = ogSize;
export const contentType = ogContentType;

export default async function Image({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const category = getCategory(slug);
  return ogImage("Shop", category?.title ?? "Shop");
}
