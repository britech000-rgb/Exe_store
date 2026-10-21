import { ogContentType, ogImage, ogSize } from "@/lib/og";

export const alt = "EXE STORE. Say what you need. I check it. Then I confirm the price.";
export const size = ogSize;
export const contentType = ogContentType;

export default function Image() {
  return ogImage("EXE STORE", "Say what you need.");
}
