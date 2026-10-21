"use client";

import { useEffect, useId, useRef, useState } from "react";

type ShareMenuProps = {
  path: string;
  name: string;
  className?: string;
};

export function ShareMenu({ path, name, className = "button button-line" }: ShareMenuProps) {
  const [open, setOpen] = useState(false);
  const [copied, setCopied] = useState(false);
  const [manual, setManual] = useState("");
  const [canNative, setCanNative] = useState(false);
  const buttonRef = useRef<HTMLButtonElement>(null);
  const panelRef = useRef<HTMLDivElement>(null);
  const titleId = useId();

  const absolute = () => {
    if (typeof window === "undefined") return path;
    return new URL(path, window.location.origin).toString();
  };

  useEffect(() => {
    setCanNative(typeof navigator !== "undefined" && typeof navigator.share === "function");
  }, []);

  useEffect(() => {
    if (!open) return;
    const panel = panelRef.current;
    const focusable = panel?.querySelector<HTMLElement>("a, button, input, textarea");
    focusable?.focus();

    function onKey(event: KeyboardEvent) {
      if (event.key === "Escape") {
        event.preventDefault();
        setOpen(false);
        buttonRef.current?.focus();
      }
    }

    function onPointer(event: MouseEvent) {
      const target = event.target as Node;
      if (panelRef.current?.contains(target) || buttonRef.current?.contains(target)) return;
      setOpen(false);
    }

    document.addEventListener("keydown", onKey);
    document.addEventListener("mousedown", onPointer);
    return () => {
      document.removeEventListener("keydown", onKey);
      document.removeEventListener("mousedown", onPointer);
    };
  }, [open]);

  function close() {
    setOpen(false);
    buttonRef.current?.focus();
  }

  async function copyLink() {
    const url = absolute();
    setCopied(false);
    setManual("");
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
        setManual(url);
      }
    }
  }

  async function more() {
    const url = absolute();
    try {
      await navigator.share({ title: name, text: name, url });
    } catch (error) {
      if (error instanceof DOMException && error.name === "AbortError") return;
    }
  }

  const whatsapp = `https://wa.me/?text=${encodeURIComponent(`${name}\n${typeof window === "undefined" ? path : absolute()}`)}`;

  return (
    <div className="share-menu">
      <button
        ref={buttonRef}
        type="button"
        className={className}
        aria-expanded={open}
        aria-controls={open ? titleId : undefined}
        onClick={() => {
          setCopied(false);
          setManual("");
          setOpen((value) => !value);
        }}
      >
        Share
      </button>
      {open ? (
        <div
          ref={panelRef}
          className="share-panel"
          role="dialog"
          aria-modal="false"
          aria-labelledby={titleId}
        >
          <p id={titleId} className="share-panel-title">
            Share {name}
          </p>
          <a className="button" href={whatsapp} target="_blank" rel="noopener noreferrer">
            Share on WhatsApp
          </a>
          <button type="button" className="button button-line" onClick={copyLink}>
            Copy link
          </button>
          {copied ? <p className="share-status">Link copied</p> : null}
          {manual ? (
            <label className="share-manual">
              Copy this link
              <input readOnly value={manual} onFocus={(event) => event.currentTarget.select()} />
            </label>
          ) : null}
          {canNative ? (
            <button type="button" className="button button-line" onClick={more}>
              More sharing options
            </button>
          ) : null}
          <button type="button" className="share-close" onClick={close}>
            Close
          </button>
        </div>
      ) : null}
    </div>
  );
}
