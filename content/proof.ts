export type ProofItem = {
  name: string;
  sentence: string;
  href: string;
};

/**
 * Only live sites and systems.
 * One sentence each. No ratings and no outcome claims.
 */
export const proof: ProofItem[] = [
  {
    name: "FuchaTech",
    sentence:
      "The live site for fiber, wireless, switching, firewalls, and server-room work.",
    href: "https://fuchatech.co.ke",
  },
];
