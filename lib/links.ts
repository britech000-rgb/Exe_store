import { site } from "@/config/site";

export function digits(value: string) {
  return value.replace(/\D/g, "");
}

export function whatsappReady() {
  return digits(site.whatsappNumber).length >= 8;
}

export function askHref(message: string) {
  const number = digits(site.whatsappNumber);
  if (number.length < 8) return site.instagram;
  return `https://wa.me/${number}?text=${encodeURIComponent(message)}`;
}

export function askLabel() {
  return whatsappReady() ? "Ask on WhatsApp" : "Ask on Instagram";
}
