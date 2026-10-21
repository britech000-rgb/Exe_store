import { getNoteLocal } from "@/content/notes";
import { ogContentType, ogImage, ogSize } from "@/lib/og";

export const alt = "Trend note. EXERIA.";
export const size = ogSize;
export const contentType = ogContentType;

export default async function Image({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const note = getNoteLocal(slug);
  return ogImage("Trends", note?.title ?? "Note");
}
