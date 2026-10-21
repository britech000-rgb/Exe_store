import { ogContentType, ogImage, ogSize } from "@/lib/og";

export const alt = "Trends. EXE STORE.";
export const size = ogSize;
export const contentType = ogContentType;

export default function Image() {
  return ogImage("Trends", "Short notes.");
}
