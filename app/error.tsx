"use client";

export default function ErrorPage({
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <main id="content" className="sheet missing">
      <div className="wrap">
        <p className="kicker">Hold on</p>
        <h1 style={{ marginTop: 12, fontSize: "clamp(2.4rem, 7vw, 4.5rem)" }}>
          That did not load.
        </h1>
        <p className="lede" style={{ color: "var(--muted)" }}>
          Try it once more. If it still fails, ask me on Instagram.
        </p>
        <button type="button" className="button" onClick={reset} style={{ marginTop: 18 }}>
          Try again
        </button>
      </div>
    </main>
  );
}
