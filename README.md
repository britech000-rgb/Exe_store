# EXERIA

Website for EXERIA. IT products and arranged work. The customer says what they need, it is checked, then the price is confirmed on WhatsApp before anyone pays.

Instagram: https://instagram.com/briankuria_ke

## WhatsApp number

The number lives in one file: `config/site.ts`.

Set `whatsappNumber` to digits only, with the country code, and without a plus. Example shape: `2547XXXXXXXX`.

Leave it as an empty string and every Ask button opens Instagram.

## Supabase

Notes and guides in `content/` are what the site shows until Supabase is connected.

When the keys are ready, set these on the host. Do not commit them.

- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`

The tables are in `supabase/schema.sql`. If `trends` or `posts` returns rows, those rows replace the built-in notes or guides. Shop categories stay in `content/shop.ts`.

Separate paragraphs in a `body` field with a blank line.

`NEXT_PUBLIC_SITE_URL` is the public site address, with no trailing slash. Set it when the domain is known so shared previews use that address.

## Local

```sh
npm install
npm run dev
```

## Deploy

The Vercel token is not in this repository. When it is decided:

```sh
VERCEL_TOKEN=your-token npm run deploy
```
