import { ogContentType, ogImage, ogSize } from "@/lib/og";

export const alt = "Work. EXERIA.";
export const size = ogSize;
export const contentType = ogContentType;

export default function Image() {
  return ogImage("Work", "The work I do.");
}
