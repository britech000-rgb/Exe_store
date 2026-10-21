import { readFileSync, readdirSync, statSync } from "node:fs";
import { join } from "node:path";

const roots = ["app", "components", "content", "config", "lib", "public", "supabase"];
const files = ["README.md"];

const banned = [
  [/broker/i, "broker"],
  [/middleman/i, "middleman"],
  [/in stock/i, "in stock"],
  [/in-stock/i, "in-stock"],
  [/\breviews?\b/i, "review"],
  [/testimonial/i, "testimonial"],
  [/britech/i, "britech"],
  [/\bksh\b/i, "ksh"],
  [/\$\d/, "a price"],
  [/add to cart/i, "add to cart"],
  [/checkout/i, "checkout"],
  [/our clients/i, "our clients"],
  [/nairobi/i, "nairobi"],
  [/\b\d{10,}\b/, "a long number"],
];

function walk(dir, found) {
  for (const entry of readdirSync(dir)) {
    const path = join(dir, entry);
    const stat = statSync(path);
    if (stat.isDirectory()) walk(path, found);
    else if (/\.(tsx?|svg|sql|md|txt)$/.test(entry)) found.push(path);
  }
}

const paths = [...files];
for (const root of roots) walk(root, paths);

const hits = [];
for (const path of paths) {
  const text = readFileSync(path, "utf8");
  const lines = text.split("\n");
  lines.forEach((line, index) => {
    for (const [pattern, label] of banned) {
      if (label === "a long number" && (path.endsWith("config/site.ts") || path.endsWith("content/media.ts"))) continue;
      if (pattern.test(line)) {
        hits.push(`${path}:${index + 1} ${label}: ${line.trim()}`);
      }
    }
  });
}

if (hits.length) {
  console.error(hits.join("\n"));
  process.exit(1);
}

console.log(`Copy check ok (${paths.length} files).`);
