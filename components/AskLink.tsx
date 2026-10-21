"use client";

import { askHref, askLabel } from "@/lib/links";

type AskLinkProps = {
  message: string;
  className?: string;
  compact?: boolean;
};

export function AskLink({
  message,
  className = "button",
  compact = false,
}: AskLinkProps) {
  const label = askLabel();
  return (
    <a
      className={className}
      href={askHref(message)}
      target="_blank"
      rel="noopener noreferrer"
      aria-label={label}
    >
      {compact ? "Ask" : label}
    </a>
  );
}
