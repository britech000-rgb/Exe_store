import type { Service } from "@/content/work";

export type ServicePage = {
  slug: string;
  image: string;
  imageAlt: string;
  suitableFor: string;
  solves: string;
  included: string[];
  process: string[];
  faqs: { question: string; answer: string }[];
};

export const servicePages: ServicePage[] = [
  {
    slug: "websites",
    image: "/images/laptops.jpg",
    imageAlt: "A laptop open on a desk, used here as a reference for a site people open on a phone or a computer",
    suitableFor: "A business whose customers already look on a phone, and who need the first screen to say what they do.",
    solves: "A visitor who cannot tell, in one minute, what the business does or what to do next.",
    included: [
      "A site built for the phone first",
      "The pages you already know you need",
      "The first screen, written from the brief",
    ],
    process: [
      "You say what the business does, and what a visitor should do next.",
      "The brief is checked.",
      "The price is confirmed on WhatsApp before you pay.",
    ],
    faqs: [
      {
        question: "Do I pay before the site is scoped?",
        answer: "No. The price is confirmed on WhatsApp before you pay.",
      },
      {
        question: "What should I send?",
        answer: "What you do, in plain words. What a visitor should do next. The pages you know you need. Anything that must be on the first screen.",
      },
    ],
  },
  {
    slug: "wifi",
    image: "/images/networking.jpg",
    imageAlt: "Network cables in a patch panel, a reference for cabling and Wi-Fi work",
    suitableFor: "A place where the front room is fine and another room is not.",
    solves: "Signal that dies in a back room, upstairs, or at the end of a corridor, and cable that is in the way.",
    included: [
      "A check of where people sit and where the signal dies",
      "Cable for the run that should be there",
      "Wi-Fi set for the rooms that must work",
    ],
    process: [
      "You name the rooms that fail, and how many people.",
      "The place is checked.",
      "The price is confirmed on WhatsApp before you pay.",
    ],
    faqs: [
      {
        question: "Is the front room being fine enough?",
        answer: "No. The job is the rooms where people actually sit.",
      },
      {
        question: "Is there a published coverage list?",
        answer: "No. Say the place in the brief. Coverage is confirmed with the quote.",
      },
    ],
  },
  {
    slug: "cctv",
    image: "/images/cctv.jpg",
    imageAlt: "Security cameras on a wall, a reference for CCTV fitting and not a record of a finished job",
    suitableFor: "Someone who needs a specific view, and a person who can open that view.",
    solves: "A camera that films the wrong place, or a picture nobody can open.",
    included: [
      "Placement for the views you named",
      "A way for the right person to open the picture",
      "A check of where power and the recorder can sit",
    ],
    process: [
      "You say what must be seen, how many views, and who opens the picture.",
      "The place and the gear are checked together.",
      "The price is confirmed on WhatsApp before you pay.",
    ],
    faqs: [
      {
        question: "Is a camera on the wall the finished job?",
        answer: "No. The job is the view you asked for, and you being able to open it.",
      },
      {
        question: "Where is the gear sold?",
        answer: "Cameras and recorders are on the CCTV page in the shop. This page is the fitting.",
      },
    ],
  },
  {
    slug: "repairs",
    image: "/images/laptops.jpg",
    imageAlt: "A laptop on a desk, a reference for repair work",
    suitableFor: "A laptop or desktop that stopped, lost a port, or crawls with ordinary work open.",
    solves: "A machine that cannot do the day it is supposed to do.",
    included: [
      "A check of the symptom you describe",
      "The model, when you can read it off the machine",
      "A price confirmed before any repair is paid for",
    ],
    process: [
      "You describe what it is doing, or not doing.",
      "It is checked. A guess about the part is not the brief.",
      "The price is confirmed on WhatsApp before any repair is paid for.",
    ],
    faqs: [
      {
        question: "Should I name the part I think failed?",
        answer: "Name the symptom. The check comes before a part is assumed.",
      },
      {
        question: "Is a turnaround published?",
        answer: "No. Timing is confirmed with the quote.",
      },
    ],
  },
  {
    slug: "office",
    image: "/images/desktops.jpg",
    imageAlt: "A desktop, monitor, and keyboard on a desk, a reference for office setup",
    suitableFor: "A room that needs power, network, and machines before people start work.",
    solves: "A pile of machines with no order: power, cable, then the desks.",
    included: [
      "Power for what must stay on",
      "Cable and Wi-Fi for where people sit",
      "Machines and the software those machines need",
    ],
    process: [
      "You say how many people, what they do, and what is already in the room.",
      "The whole brief is checked as one job.",
      "The price is confirmed on WhatsApp before you pay.",
    ],
    faqs: [
      {
        question: "Should I start by asking for laptops?",
        answer: "Start with the room. Power, then the network, then the machines.",
      },
      {
        question: "Is there a published price for an office?",
        answer: "No. The price is confirmed on WhatsApp after the brief is checked.",
      },
    ],
  },
  {
    slug: "systems",
    image: "/images/software.jpg",
    imageAlt: "A laptop screen with software open, a reference for a business system",
    suitableFor: "A team that loses enquiries or records, including a team that still uses a notebook.",
    solves: "Work that only one person can find, and a Tuesday that depends on memory.",
    included: [
      "A path for how an enquiry comes in",
      "A place a record is kept",
      "A setup a new person can use",
    ],
    process: [
      "You describe what keeps getting lost.",
      "A setup the team can run is checked.",
      "The price is confirmed on WhatsApp before you pay.",
    ],
    faqs: [
      {
        question: "Does the team have to already use software?",
        answer: "No. Say what you do today, even if it is a notebook.",
      },
      {
        question: "Who is this for?",
        answer: "The people who will use it. Say how many, and what a good Tuesday should look like.",
      },
    ],
  },
];

export function getServicePage(slug: string) {
  return servicePages.find((page) => page.slug === slug);
}

export function serviceAsk(service: Service) {
  return service.askMessage;
}
