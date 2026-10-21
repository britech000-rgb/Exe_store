/**
 * Brian Kuria site config.
 *
 * WHATSAPP
 * When the number is ready, set whatsappNumber to digits only,
 * with the country code, and without a plus, spaces, or dashes.
 * Example: "2547XXXXXXXX"
 *
 * Leave it as an empty string and every Ask button opens Instagram.
 */
export const site = {
  name: "Brian Kuria",
  description:
    "I supply IT products and I arrange the work. Tell me what you need. I check it, then I confirm the price on WhatsApp before you pay.",
  instagram: "https://instagram.com/briankuria_ke",
  instagramHandle: "@briankuria_ke",
  whatsappNumber: "",
  defaultAskMessage: "Brian, here is what I need: ",
} as const;
