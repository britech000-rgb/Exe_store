import type { ProofItem } from "@/content/proof";

export function ProofList({ items }: { items: ProofItem[] }) {
  return (
    <ul className="proof-list">
      {items.map((item) => (
        <li key={item.href}>
          <h3>{item.name}</h3>
          <p>{item.sentence}</p>
          <a href={item.href} target="_blank" rel="noopener noreferrer">
            {item.href.replace(/^https:\/\//, "")}
          </a>
        </li>
      ))}
    </ul>
  );
}
