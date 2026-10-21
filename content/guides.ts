export type Guide = {
  slug: string;
  title: string;
  excerpt: string;
  paragraphs: string[];
  advertised?: boolean;
  related?: { href: string; label: string }[];
};

export const guides: Guide[] = [
  {
    slug: "brief-me-for-a-laptop",
    title: "How to brief me for a laptop",
    advertised: true,
    excerpt:
      "Send the work the laptop has to do. I check against that, then I confirm the price on WhatsApp before you pay.",
    paragraphs: [
      "Do not start with a name you saw on a poster. Start with the day the laptop has to survive.",
      "Tell me the work. Classes and documents. Office mail and calls. Photos, design, or a shop that lives in the browser. A machine that stays on a desk is a different check from a machine that goes in a bag every morning.",
      "Tell me what failed last time. The battery died at noon. The machine choked when two apps were open. The screen was too small. The keyboard started missing letters. That list is more useful than a rumour about which name is good this month.",
      "Tell me if you carry it, and roughly how big a screen you can live with. If you know the model you have now, send that too. Read it off the bottom of the machine. A photo of the lid is not a model number.",
      "Then I go and check. I come back on WhatsApp with what I found and the price. You pay only after that price is confirmed. If I have not confirmed it, you have not bought a laptop.",
      "A clear message looks like this: I teach, I carry the laptop daily, the last battery died by lunch, I want a bigger screen. Send that. I can check from there.",
    ],
    related: [{ href: "/shop/laptops", label: "Laptops" }],
  },
  {
    slug: "before-the-cameras-go-up",
    title: "Before the cameras go up",
    excerpt:
      "A camera in the wrong place films the sky and misses the gate. Tell me the view, and who must open it.",
    paragraphs: [
      "This is the work, not the gadget. A camera in the wrong place films the sky and misses the gate.",
      "Tell me what you need to see. The gate. The counter. The passage. The store room. Day, night, or both. One view is a different check from six.",
      "Tell me who opens the picture. You, from the phone. Someone in the office. Someone who is not on the premises. If the person who must see it cannot open it, the job is not done. A camera you cannot open is decoration.",
      "Tell me about power, and a place for the recorder. Cameras need power they can trust. The recorder needs a spot that is not in the rain and not where anyone can walk off with it.",
      "The gear sits on the CCTV page in the shop. Fitting it is the work. I check both. The price for the gear and the price for the work are confirmed on WhatsApp before you pay.",
      "Send the place in plain words, and how many views you need. If you have photos of the corners, send those too. I will tell you what I still need before I confirm any price.",
    ],
    related: [
      { href: "/shop/cctv", label: "CCTV gear" },
      { href: "/work#cctv", label: "CCTV work" },
    ],
  },
  {
    slug: "small-office-in-the-right-order",
    title: "A small office, in the right order",
    excerpt:
      "Do not start with a pile of laptops. Start with power, then the network, then the machines.",
    paragraphs: [
      "Do not start with eight laptops. Start with the room.",
      "Power first. What must stay on when the lights blink. The router, the recorder, the one desk that cannot go dark. I check the backup against that list, not against a bigger box for the sake of it.",
      "Then the cable and the Wi-Fi. The back room has to work on the first Monday, not after everyone has complained for a week. Tell me where people sit and where the walls are thick.",
      "Then the machines. Laptops if people move. Desktops if they stay. Chargers that match those exact machines. I check the set as one brief.",
      "Then genuine software for the work those machines do, for the number of people who use them. A licence that arrived from nowhere becomes a Monday problem. I do not bring you a Monday problem.",
      "Then the system the team will actually run. How an enquiry comes in. How a record is kept. How a new person finds it on Tuesday. I set that up so someone other than me can use it.",
      "Each of those can be its own job, or one setup. Either way the rule holds. I check, I confirm the price on WhatsApp, and only then does anyone pay.",
    ],
    related: [
      { href: "/work#office", label: "Office setup" },
      { href: "/shop/power", label: "Power" },
      { href: "/shop/networking", label: "Networking" },
    ],
  },
];

export function getGuideLocal(slug: string) {
  return guides.find((guide) => guide.slug === slug);
}
