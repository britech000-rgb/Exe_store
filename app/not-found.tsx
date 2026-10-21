import Link from "next/link";

export default function NotFound() {
  return (
    <main id="content" className="sheet missing">
      <div className="wrap">
        <p className="kicker">Missing</p>
        <h1 style={{ marginTop: 12 }}>That page is not here.</h1>
        <p className="lede" style={{ color: "var(--muted)" }}>
          The link missed. Go back to the shop, or ask me what you need.
        </p>
        <p className="related">
          <Link href="/">Home</Link>
          <Link href="/shop">Shop</Link>
          <Link href="/work">Work</Link>
        </p>
      </div>
    </main>
  );
}
