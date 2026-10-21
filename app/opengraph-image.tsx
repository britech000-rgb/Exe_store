import { ogContentType, ogImage, ogSize } from "@/lib/og";

export const alt = "Brian Kuria. Say what you need. I check it. Then I confirm the price.";
export const size = ogSize;
export const contentType = ogContentType;

export default function Image() {
  return ogImage("Brian Kuria", "Say what you need.");
}
