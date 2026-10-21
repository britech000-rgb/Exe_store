import { ogContentType, ogImage, ogSize } from "@/lib/og";

export const alt = "Proof. Brian Kuria.";
export const size = ogSize;
export const contentType = ogContentType;

export default function Image() {
  return ogImage("Proof", "Work you can open.");
}
