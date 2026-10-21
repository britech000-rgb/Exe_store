"use client";

import { useEffect, useState } from "react";

type CopyLinkProps = {
  path: string;
  name: string;
};

export function CopyLink({ path, name }: CopyLinkProps) {
  const [copied, setCopied] = useState(false);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    if (!copied) return;
    const timer = window.setTimeout(() => setCopied(false), 2000);
    return () => window.clearTimeout(timer);
  }, [copied]);

  async function onCopy() {
    const url = `${window.location.origin}${path}`;
    setFailed(false);
    try {
      await navigator.clipboard.writeText(url);
      setCopied(true);
    } catch {
      try {
        const field = document.createElement("textarea");
        field.value = url;
        field.setAttribute("readonly", "");
        field.style.position = "fixed";
        field.style.left = "-9999px";
        document.body.appendChild(field);
        field.select();
        const ok = document.execCommand("copy");
        field.remove();
        if (!ok) throw new Error("copy failed");
        setCopied(true);
      } catch {
        setCopied(false);
        setFailed(true);
      }
    }
  }

  return (
    <button
      type="button"
      className={copied ? "copy is-copied" : "copy"}
      onClick={onCopy}
      aria-label={`Copy link to ${name}`}
    >
      {copied ? "Copied" : failed ? "Select the link" : "Copy link"}
    </button>
  );
}

export function UrlLine({ path }: { path: string }) {
  const [href, setHref] = useState(path);

  useEffect(() => {
    setHref(`${window.location.origin}${path}`);
  }, [path]);

  return (
    <p className="url-line">
      <span className="url-label">Link</span>
      <a href={path}>{href}</a>
    </p>
  );
}
