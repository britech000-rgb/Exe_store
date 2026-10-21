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
    image: "/images/laptops.jpg",
    imageAlt: "A laptop open on a light wood desk",
    summary: "A machine you carry between rooms, classes, and calls. The brief decides it.",
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
    askMessage: "EXE STORE, I need a laptop. Here is what I will use it for: ",
  },
  {
    slug: "desktops",
    title: "Desktops",
    image: "/images/desktops.jpg",
    imageAlt: "A desktop tower, monitor, and keyboard on a desk",
    summary: "A desk that stays switched on. The tower stays. The work stays with it.",
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
    askMessage: "EXE STORE, I need a desktop. Here is the work it has to do: ",
  },
  {
    slug: "chargers",
    title: "Chargers",
    image: "/images/chargers.jpg",
    imageAlt: "A laptop charger and its cable on a white surface",
    summary: "The brick has to match the model. A plug that fits is not the right charger.",
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
    askMessage: "EXE STORE, I need a charger. The model is: ",
  },
  {
    slug: "networking",
    title: "Networking",
    image: "/images/networking.jpg",
    imageAlt: "Network cables plugged into a patch panel",
    summary: "Routers, switches, access points, and the cable the back room has been missing.",
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
    askMessage: "EXE STORE, I need networking gear. The problem is: ",
    related: {
      href: "/work#wifi",
      label: "Wi-Fi and cabling work",
    },
  },
  {
    slug: "cctv",
    title: "CCTV",
    image: "/images/cctv.jpg",
    imageAlt: "Rows of security cameras on a wall",
    summary: "Cameras and recorders for a gate, a counter, or a passage. Fitting them sits under Work.",
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
    askMessage: "EXE STORE, I need CCTV gear. I need to see: ",
    related: {
      href: "/work#cctv",
      label: "CCTV fitting is on the Work page",
    },
  },
  {
    slug: "power",
    title: "Power",
    image: "/images/power.jpg",
    imageAlt: "A six-socket power strip with its cord",
    summary: "Backup and clean power for the router, the recorder, and the desk that cannot go dark.",
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
    askMessage: "EXE STORE, I need power backup. What must stay on: ",
  },
  {
    slug: "phones",
    title: "Phones",
    image: "/images/phones.jpg",
    imageAlt: "A smartphone lying on a white desk beside a keyboard",
    summary: "Calls, WhatsApp, and a day away from a socket. The work on the screen decides the phone.",
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
    askMessage: "EXE STORE, I need a phone. I will use it for: ",
  },
  {
    slug: "software",
    title: "Software",
    image: "/images/software.jpg",
    imageAlt: "A laptop screen filled with software code",
    summary: "A genuine licence for the job, counted for the people who actually use it.",
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
      "EXE STORE, I need software. The job and the number of people: ",
  },
];

export function getCategory(slug: string) {
  return categories.find((category) => category.slug === slug);
}
