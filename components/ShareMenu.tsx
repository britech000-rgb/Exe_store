"use client";

import { useEffect, useId, useRef, useState } from "react";

type ShareMenuProps = {
  path: string;
  name: string;
  className?: string;
  social?: boolean;
};

type InstagramKind = "story" | "post";

const files = new Map<string, Promise<File>>();

function instagramPaths(path: string) {
  if (path === "/blog") {
    return { story: "/instagram/story-blog-index.jpg", post: "/instagram/post-blog-index.jpg", fallback: "blog" };
  }
  if (path === "/trends") {
    return { story: "/instagram/story-trends-index.jpg", post: "/instagram/post-trends-index.jpg", fallback: "trends" };
  }
  const guide = path.match(/^\/blog\/([^/]+)$/);
  if (guide) {
    return {
      story: `/instagram/story-blog-${guide[1]}.jpg`,
      post: `/instagram/post-blog-${guide[1]}.jpg`,
      fallback: "blog",
    };
  }
  const note = path.match(/^\/trends\/([^/]+)$/);
  if (note) {
    return {
      story: `/instagram/story-trends-${note[1]}.jpg`,
      post: `/instagram/post-trends-${note[1]}.jpg`,
      fallback: "trends",
    };
  }
  return null;
}

function loadImage(url: string, filename: string) {
  const cached = files.get(url);
  if (cached) return cached;
  const pending = fetch(url)
    .then((response) => {
      if (!response.ok) throw new Error("missing image");
      return response.blob();
    })
    .then((blob) => new File([blob], filename, { type: "image/jpeg" }));
  files.set(url, pending);
  pending.catch(() => files.delete(url));
  return pending;
}

export function ShareMenu({ path, name, className = "button button-line", social = false }: ShareMenuProps) {
  const [open, setOpen] = useState(false);
  const [copied, setCopied] = useState(false);
  const [manual, setManual] = useState("");
  const [notice, setNotice] = useState("");
  const [canNative, setCanNative] = useState(false);
  const buttonRef = useRef<HTMLButtonElement>(null);
  const panelRef = useRef<HTMLDivElement>(null);
  const titleId = useId();
  const pictures = social ? instagramPaths(path) : null;

  const absolute = () => {
    if (typeof window === "undefined") return path;
    return new URL(path, window.location.origin).toString();
  };

  const caption = () => `${name}\n${absolute()}\nEXE STORE`;

  useEffect(() => {
    setCanNative(typeof navigator !== "undefined" && typeof navigator.share === "function");
  }, []);

  useEffect(() => {
    if (!open || !social) return;
    const ready = instagramPaths(path);
    if (!ready) return;
    loadImage(ready.story, "exe-store-story.jpg").catch(() => undefined);
    loadImage(ready.post, "exe-store-post.jpg").catch(() => undefined);
  }, [open, social, path]);

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

  function reset() {
    setCopied(false);
    setManual("");
    setNotice("");
  }

  async function copyText(value: string) {
    try {
      await navigator.clipboard.writeText(value);
      return true;
    } catch {
      try {
        const field = document.createElement("textarea");
        field.value = value;
        field.setAttribute("readonly", "");
        field.style.position = "fixed";
        field.style.left = "-9999px";
        document.body.appendChild(field);
        field.select();
        const ok = document.execCommand("copy");
        field.remove();
        return ok;
      } catch {
        return false;
      }
    }
  }

  async function copyLink() {
    const url = absolute();
    reset();
    const ok = await copyText(url);
    if (ok) setCopied(true);
    else setManual(url);
  }

  async function more() {
    const url = absolute();
    try {
      await navigator.share({ title: name, text: name, url });
    } catch (error) {
      if (error instanceof DOMException && error.name === "AbortError") return;
    }
  }

  function download(file: File) {
    const objectUrl = URL.createObjectURL(file);
    const link = document.createElement("a");
    link.href = objectUrl;
    link.download = file.name;
    link.click();
    URL.revokeObjectURL(objectUrl);
  }

  async function instagram(kind: InstagramKind) {
    if (!pictures) return;
    reset();
    const filename = kind === "story" ? "exe-store-story.jpg" : "exe-store-post.jpg";
    const preferred = kind === "story" ? pictures.story : pictures.post;
    const reserve =
      pictures.fallback === "blog"
        ? kind === "story"
          ? "/instagram/story-blog-index.jpg"
          : "/instagram/post-blog-index.jpg"
        : kind === "story"
          ? "/instagram/story-trends-index.jpg"
          : "/instagram/post-trends-index.jpg";
    let file: File;
    try {
      file = await loadImage(preferred, filename);
    } catch {
      try {
        file = await loadImage(reserve, filename);
      } catch {
        setNotice("The image could not be prepared.");
        return;
      }
    }

    const text = caption();
    const payload = { files: [file], title: name, text };
    if (typeof navigator.canShare === "function" && navigator.canShare(payload)) {
      try {
        await navigator.share(payload);
        setNotice(kind === "story" ? "Choose Instagram, then Story." : "Choose Instagram, then Post.");
        return;
      } catch (error) {
        if (error instanceof DOMException && error.name === "AbortError") return;
      }
    }

    download(file);
    const copiedCaption = await copyText(text);
    if (copiedCaption) {
      setNotice(
        kind === "story"
          ? "Story image downloaded. Caption copied. Upload it in Instagram."
          : "Post image downloaded. Caption copied. Upload it in Instagram.",
      );
    } else {
      setManual(text);
      setNotice(kind === "story" ? "Story image downloaded." : "Post image downloaded.");
    }
  }

  const pageUrl = typeof window === "undefined" ? path : absolute();
  const encodedUrl = encodeURIComponent(pageUrl);
  const whatsapp = `https://wa.me/?text=${encodeURIComponent(`${name}\n${pageUrl}`)}`;
  const facebook = `https://www.facebook.com/sharer/sharer.php?u=${encodedUrl}`;
  const xShare = `https://x.com/intent/tweet?url=${encodedUrl}&text=${encodeURIComponent(name)}`;
  const linkedin = `https://www.linkedin.com/sharing/share-offsite/?url=${encodedUrl}`;

  return (
    <div className="share-menu">
      <button
        ref={buttonRef}
        type="button"
        className={className}
        aria-expanded={open}
        aria-controls={open ? titleId : undefined}
        onClick={() => {
          reset();
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
          {social ? (
            <>
              <a className="button button-line" href={facebook} target="_blank" rel="noopener noreferrer">
                Share on Facebook
              </a>
              <a className="button button-line" href={xShare} target="_blank" rel="noopener noreferrer">
                Share on X
              </a>
              <a className="button button-line" href={linkedin} target="_blank" rel="noopener noreferrer">
                Share on LinkedIn
              </a>
              {pictures ? (
                <>
                  <button type="button" className="button button-line" onClick={() => instagram("story")}>
                    Instagram Story
                  </button>
                  <button type="button" className="button button-line" onClick={() => instagram("post")}>
                    Instagram Post
                  </button>
                </>
              ) : null}
            </>
          ) : null}
          <button type="button" className="button button-line" onClick={copyLink}>
            Copy link
          </button>
          {copied ? <p className="share-status">Link copied</p> : null}
          {notice ? <p className="share-status">{notice}</p> : null}
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
          {pictures ? (
            <p className="share-note">
              Story is a full-screen image. Post is a feed image. On a phone, choose Instagram.
              The caption includes the page link.
            </p>
          ) : null}
          <button type="button" className="share-close" onClick={close}>
            Close
          </button>
        </div>
      ) : null}
    </div>
  );
}
