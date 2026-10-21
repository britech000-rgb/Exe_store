export type Category = {
  slug: string;
  title: string;
  image: string;
  imageAlt: string;
  summary: string;
  intro: string;
  covers: string[];
  tellMe: string[];
  askMessage: string;
  related?: { href: string; label: string };
};

export const categories: Category[] = [
  {
    slug: "laptops",
    title: "Laptops",
    image: "/preview/laptops.svg",
    imageAlt: "Preview graphic for the laptops category",
    summary: "For work, school, and the days you carry it.",
    intro:
      "Tell me what the laptop has to survive. Classes, office, calls, or a bag on your shoulder all day. I check against that work, then I confirm the price on WhatsApp before you pay.",
    covers: [
      "Everyday work and school",
      "Heavier days: design, photos, too many tabs",
      "A machine you carry",
      "A replacement when the current one is done",
    ],
    tellMe: [
      "What you will actually do on it",
      "Whether you carry it every day",
      "The screen size you can live with, if you know it",
      "What the last one failed at",
    ],
    askMessage: "Brian, I need a laptop. Here is what I will use it for: ",
  },
  {
    slug: "desktops",
    title: "Desktops",
    image: "/preview/desktops.svg",
    imageAlt: "Preview graphic for the desktops category",
    summary: "A machine that stays put and does the job.",
    intro:
      "A desk, a counter, a setup that does not travel. Tell me the work it has to do in that spot. I check the machine against the room, then I confirm the price on WhatsApp before you pay.",
    covers: [
      "Office desks that stay switched on",
      "A counter or a workshop bench",
      "A home setup that does not leave the room",
      "A tower when a laptop is the wrong tool",
    ],
    tellMe: [
      "The work it must do",
      "Where it will sit",
      "Whether a monitor is part of the brief",
      "What is already on that desk",
    ],
    askMessage: "Brian, I need a desktop. Here is the work it has to do: ",
  },
  {
    slug: "chargers",
    title: "Chargers",
    image: "/preview/chargers.svg",
    imageAlt: "Preview graphic for the chargers category",
    summary: "The right power for that exact model.",
    intro:
      "If it slides in, that still does not mean it is the right charger. Send the laptop or phone model. I check the match, then I confirm the price on WhatsApp before you pay.",
    covers: [
      "Laptop chargers matched to the model",
      "Phone chargers and cables",
      "A spare for the bag",
      "A replacement when the old brick runs hot or does nothing",
    ],
    tellMe: [
      "The exact model, read off the machine",
      "Laptop or phone",
      "What the current charger is doing wrong",
      "Whether you need one or a spare as well",
    ],
    askMessage: "Brian, I need a charger. The model is: ",
  },
  {
    slug: "networking",
    title: "Networking",
    image: "/preview/networking.svg",
    imageAlt: "Preview graphic for the networking category",
    summary: "Routers, switches, access points, and cable.",
    intro:
      "The front room can be fine while the back room is dead. Tell me which room fails and what you are trying to connect. I check the gear, then I confirm the price on WhatsApp before you pay.",
    covers: [
      "Routers",
      "Switches",
      "Access points",
      "Cable for a run that should have been there",
    ],
    tellMe: [
      "Which room dies, or what you need to connect",
      "How many people or desks",
      "Whether the walls are thick",
      "What is already installed",
    ],
    askMessage: "Brian, I need networking gear. The problem is: ",
    related: {
      href: "/work#wifi",
      label: "Wi-Fi and cabling work",
    },
  },
  {
    slug: "cctv",
    title: "CCTV",
    image: "/preview/cctv.svg",
    imageAlt: "Preview graphic for the CCTV category",
    summary: "Cameras and recorders. Fitting them is under Work.",
    intro:
      "This page is the gear: cameras and recorders. Tell me what you need to see. I check the kit against that view, then I confirm the price on WhatsApp before you pay.",
    covers: [
      "Cameras for a gate, counter, passage, or room",
      "Recorders",
      "Gear for day and night views",
      "A setup you can actually open",
    ],
    tellMe: [
      "What you need to see",
      "How many views",
      "Day, night, or both",
      "Who must open the picture, and from where",
    ],
    askMessage: "Brian, I need CCTV gear. I need to see: ",
    related: {
      href: "/work#cctv",
      label: "CCTV fitting is on the Work page",
    },
  },
  {
    slug: "power",
    title: "Power",
    image: "/preview/power.svg",
    imageAlt: "Preview graphic for the power category",
    summary: "Backup for the kit that cannot just die.",
    intro:
      "Tell me what must stay on when the lights blink. The router, the recorder, the one desk that cannot go dark. I check the backup against that list, then I confirm the price on WhatsApp before you pay.",
    covers: [
      "UPS backup",
      "Surge protection",
      "Power for the kit that has to stay up",
      "A match for what is actually plugged in",
    ],
    tellMe: [
      "What must stay on",
      "Roughly how long it must stay on",
      "What is plugged in today",
      "Whether this is one desk or a whole room",
    ],
    askMessage: "Brian, I need power backup. What must stay on: ",
  },
  {
    slug: "phones",
    title: "Phones",
    image: "/preview/phones.svg",
    imageAlt: "Preview graphic for the phones category",
    summary: "For calls, WhatsApp, and the work on the screen.",
    intro:
      "Tell me the work the phone has to do. Calls, WhatsApp, photos, a long day away from a socket. I check against that, then I confirm the price on WhatsApp before you pay.",
    covers: [
      "A phone for calls and WhatsApp",
      "A phone that has to last the day",
      "A second line for work",
      "A replacement when the current one is finished",
    ],
    tellMe: [
      "What you will use it for",
      "Whether the battery has to last a full day out",
      "The phone you have now, if you are replacing it",
      "Anything the current one fails at",
    ],
    askMessage: "Brian, I need a phone. I will use it for: ",
  },
  {
    slug: "software",
    title: "Software",
    image: "/preview/software.svg",
    imageAlt: "Preview graphic for the software category",
    summary: "Genuine licences for the work the machines do.",
    intro:
      "Tell me what the computers must do, and how many people use them. I check a genuine licence against that, then I confirm the price on WhatsApp before you pay.",
    covers: [
      "Genuine licences",
      "Software for the daily work",
      "A licence for each person who needs one",
      "A clean setup when a machine is new",
    ],
    tellMe: [
      "What the computer must do",
      "How many people",
      "Whether the machines are new or already in use",
      "What you are using today",
    ],
    askMessage:
      "Brian, I need software. The job and the number of people: ",
  },
];

export function getCategory(slug: string) {
  return categories.find((category) => category.slug === slug);
}
