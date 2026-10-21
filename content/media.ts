/**
 * Photographs used on the site, and the licence they were taken under.
 * None of these pictures show EXE STORE premises, staff, or a finished job.
 */
export const mediaCredits = [
  {
    file: "/images/laptops.jpg",
    source: "https://unsplash.com/photos/1496181133206-80ce9b88a853",
    licence: "Unsplash Licence",
  },
  {
    file: "/images/desktops.jpg",
    source: "https://unsplash.com/photos/1593640408182-31c70c8268f5",
    licence: "Unsplash Licence",
  },
  {
    file: "/images/chargers.jpg",
    source: "https://unsplash.com/photos/1583863788434-e58a36330cf0",
    licence: "Unsplash Licence",
  },
  {
    file: "/images/networking.jpg",
    source: "https://unsplash.com/photos/1544197150-b99a580bb7a8",
    licence: "Unsplash Licence",
  },
  {
    file: "/images/cctv.jpg",
    source: "https://unsplash.com/photos/1557597774-9d273605dfa9",
    licence: "Unsplash Licence",
  },
  {
    file: "/images/power.jpg",
    source:
      "https://commons.wikimedia.org/wiki/File:Schuko_power_strip_with_extension_cord_for_6_sockets.JPG",
    licence: "CC BY-SA 4.0",
  },
  {
    file: "/images/phones.jpg",
    source: "https://unsplash.com/photos/1511707171634-5f897ff02aa9",
    licence: "Unsplash Licence",
  },
  {
    file: "/images/software.jpg",
    source: "https://unsplash.com/photos/1555066931-4365d14bab8c",
    licence: "Unsplash Licence",
  },
] as const;

export function creditFor(file: string) {
  return mediaCredits.find((item) => item.file === file);
}
