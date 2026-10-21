export type Note = {
  slug: string;
  title: string;
  paragraphs: string[];
  advertised?: boolean;
  related?: { href: string; label: string };
};

export const notes: Note[] = [
  {
    slug: "bring-the-model",
    title: "Bring the model number",
    paragraphs: [
      "A photo of the cracked corner is not a model number. Flip the laptop and read the line on the bottom. Send me that.",
      "With the model, I can check the right charger, the right part, or the right replacement. Without it, I am guessing, and I do not guess on your money.",
      "I confirm the price on WhatsApp before you pay.",
    ],
    related: { href: "/shop/laptops", label: "Laptops" },
  },
  {
    slug: "a-charger-has-a-job",
    title: "A charger has a job",
    paragraphs: [
      "If the plug slides in, that does not mean the charger is the right one. The wrong brick runs hot, charges slowly, or does nothing.",
      "Tell me the exact laptop or phone. I check the match. Then I confirm the price on WhatsApp before you pay.",
    ],
    related: { href: "/shop/chargers", label: "Chargers" },
  },
  {
    slug: "the-back-room",
    title: "The back room is where Wi-Fi goes to die",
    paragraphs: [
      "The router is happy in the front room. The back office, the store room, the rooms upstairs: that is where it gives up.",
      "Tell me which room dies and what the walls are like. I check the gear and the cable. The price is confirmed on WhatsApp before you pay.",
    ],
    related: { href: "/work#wifi", label: "Wi-Fi and cabling" },
  },
  {
    slug: "open-the-camera",
    title: "A camera you cannot open is decoration",
    paragraphs: [
      "The camera on the wall is the easy part. If you cannot open the view on your phone, you do not have CCTV. You have a dome.",
      "When you ask me, say who must open it and from where. I check that with the gear. Then I confirm the price on WhatsApp before you pay.",
    ],
    related: { href: "/work#cctv", label: "CCTV work" },
  },
  {
    slug: "say-the-work",
    title: "Say the work, not the slogan",
    advertised: true,
    paragraphs: [
      "“A fast laptop” is a slogan. “I teach all day and I carry it between two rooms” is a brief.",
      "Send me the work. I check what fits that work, and I confirm the price on WhatsApp before you pay.",
    ],
    related: {
      href: "/blog/brief-me-for-a-laptop",
      label: "How to brief me for a laptop",
    },
  },
  {
    slug: "genuine-software",
    title: "Genuine software, then peace",
    paragraphs: [
      "A licence that arrived from nowhere will ask to be paid again, or it will lock the machine on a Monday morning.",
      "Tell me what the computer must do and how many people use it. I check a genuine licence. Then I confirm the price on WhatsApp before you pay.",
    ],
    related: { href: "/shop/software", label: "Software" },
  },
];

export function getNoteLocal(slug: string) {
  return notes.find((note) => note.slug === slug);
}
