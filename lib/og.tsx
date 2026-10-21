import { ImageResponse } from "next/og";

export const ogSize = { width: 1200, height: 630 };
export const ogContentType = "image/png";

export function ogImage(kicker: string, title: string) {
  return new ImageResponse(
    (
      <div
        style={{
          width: "100%",
          height: "100%",
          display: "flex",
          flexDirection: "column",
          justifyContent: "space-between",
          background: "#0b0b0b",
          color: "#f4f1ea",
          padding: "68px",
        }}
      >
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            width: "100%",
          }}
        >
          <div style={{ display: "flex", fontSize: 28, color: "#d6ff3a" }}>
            {kicker.toUpperCase()}
          </div>
          <div style={{ display: "flex", fontSize: 28 }}>Brian Kuria</div>
        </div>
        <div style={{ display: "flex", flexDirection: "column", width: "100%" }}>
          <div
            style={{
              display: "flex",
              fontSize: title.length > 28 ? 68 : 92,
              fontWeight: 700,
              lineHeight: 0.95,
              letterSpacing: -2,
              maxWidth: 1000,
            }}
          >
            {title}
          </div>
          <div
            style={{
              display: "flex",
              marginTop: 28,
              fontSize: 32,
              color: "#d6ff3a",
            }}
          >
            I check it. Then I confirm the price.
          </div>
        </div>
      </div>
    ),
    { ...ogSize },
  );
}
