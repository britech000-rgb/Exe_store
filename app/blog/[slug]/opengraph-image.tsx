import { getGuideLocal } from "@/content/guides";
import { ogContentType, ogImage, ogSize } from "@/lib/og";

export const alt = "Guide. EXERIA.";
export const size = ogSize;
export const contentType = ogContentType;

export default async function Image({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const guide = getGuideLocal(slug);
  return ogImage("Blog", guide?.title ?? "Guide");
}
