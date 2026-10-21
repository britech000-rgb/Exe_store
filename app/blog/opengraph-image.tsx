import { ogContentType, ogImage, ogSize } from "@/lib/og";

export const alt = "Blog. EXE STORE.";
export const size = ogSize;
export const contentType = ogContentType;

export default function Image() {
  return ogImage("Blog", "Longer guides.");
}
