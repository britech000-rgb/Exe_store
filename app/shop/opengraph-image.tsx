import { ogContentType, ogImage, ogSize } from "@/lib/og";

export const alt = "Shop. EXE STORE.";
export const size = ogSize;
export const contentType = ogContentType;

export default function Image() {
  return ogImage("Shop", "Pick the category.");
}
