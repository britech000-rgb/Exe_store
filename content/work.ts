export type Service = {
  slug: string;
  title: string;
  summary: string;
  detail: string;
  tellMe: string[];
  askMessage: string;
  related?: { href: string; label: string };
};

export const services: Service[] = [
  {
    slug: "websites",
    title: "Websites",
    summary: "A site that opens on a phone and says what you do.",
    detail:
      "I build the site. Your customers are already on a phone, so that is the screen I build for. Tell me what the business does and what a visitor must understand in one minute. I check the brief, then I confirm the price on WhatsApp before you pay.",
    tellMe: [
      "What you do, in plain words",
      "What a visitor should do next",
      "Pages you know you need",
      "Anything that must be on the first screen",
    ],
    askMessage: "EXERIA, I need a website. Here is what it must say: ",
  },
  {
    slug: "wifi",
    title: "Wi-Fi and cabling",
    summary: "Signal in the back room, and cable that is not a trip hazard.",
    detail:
      "I run the cable and I set the Wi-Fi. The front room being fine does not mean the job is done. Tell me where people sit and where the signal dies. I check the place, then I confirm the price on WhatsApp before you pay.",
    tellMe: [
      "Which rooms must work",
      "How many people",
      "Where the router sits today",
      "Thick walls, upstairs rooms, or a long corridor",
    ],
    askMessage: "EXERIA, I need Wi-Fi and cabling. The place is: ",
    related: {
      href: "/shop/networking",
      label: "Networking gear is in the shop",
    },
  },
  {
    slug: "cctv",
    title: "CCTV",
    summary: "Cameras placed for the view you asked for, and a way to open them.",
    detail:
      "I fit the cameras. A dome on the wall is not the job. The job is the view you asked for, and you being able to open it. Tell me the gate, the counter, the passage, the room. I check the place and the gear, then I confirm the price on WhatsApp before you pay.",
    tellMe: [
      "What you need to see",
      "How many views",
      "Who opens the picture, and from where",
      "Where power and the recorder can sit",
    ],
    askMessage: "EXERIA, I need CCTV fitted. I need to see: ",
    related: {
      href: "/shop/cctv",
      label: "Cameras and recorders are in the shop",
    },
  },
  {
    slug: "repairs",
    title: "Repairs",
    summary: "Machines that stopped, ports that died, systems that crawl.",
    detail:
      "I repair the machine. Tell me the symptom, not a guess about the part. It will not start. A port does nothing. It crawls with two apps open. I check it, then I confirm the price on WhatsApp before any repair is paid for.",
    tellMe: [
      "What it is doing, or not doing",
      "Laptop, desktop, or something else",
      "The model, if you can read it",
      "When it started",
    ],
    askMessage: "EXERIA, I need a repair. The symptom is: ",
  },
  {
    slug: "office",
    title: "Office setup",
    summary: "Desks, network, power, and machines, ready for people to work.",
    detail:
      "I set the office up. Power, cable, Wi-Fi, machines, then the software those machines need. Tell me how many people and what they do all day. I check the whole brief, then I confirm the price on WhatsApp before you pay.",
    tellMe: [
      "How many people",
      "What they do",
      "What is already in the room",
      "What must stay on when the lights blink",
    ],
    askMessage: "EXERIA, I need an office set up. The room is: ",
    related: {
      href: "/blog/small-office-in-the-right-order",
      label: "Read the order I set an office up in",
    },
  },
  {
    slug: "systems",
    title: "Business systems",
    summary: "A simple way to keep enquiries and records, and for the team to use it.",
    detail:
      "I set the system up. How an enquiry comes in. How a record is kept. How a new person finds it on a Tuesday. Tell me the mess you are actually in. I check a setup your team can run, then I confirm the price on WhatsApp before you pay.",
    tellMe: [
      "What keeps getting lost",
      "How many people will use it",
      "What you are doing today, even if it is a notebook",
      "What a good Tuesday should look like",
    ],
    askMessage: "EXERIA, I need a business system. What keeps getting lost: ",
  },
];

export function getService(slug: string) {
  return services.find((service) => service.slug === slug);
}
