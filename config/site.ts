/**
 * EXERIA site config.
 *
 * WHATSAPP
 * When the number is ready, set whatsappNumber to digits only,
 * with the country code, and without a plus, spaces, or dashes.
 * Example: "2547XXXXXXXX"
 *
 * Leave it as an empty string and every Ask button opens Instagram.
 */
export const site = {
  name: "EXERIA",
  description:
    "EXERIA supplies IT products and arranges the work. Send what you need. It is checked, then the price is confirmed on WhatsApp before you pay.",
  instagram: "https://instagram.com/briankuria_ke",
  instagramHandle: "@briankuria_ke",
  whatsappNumber: "254700030314",
  defaultAskMessage: "EXERIA, here is what I need: ",
} as const;
