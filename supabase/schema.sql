-- Optional tables for notes and guides.
-- The site uses content/ until SUPABASE_URL and SUPABASE_ANON_KEY are set
-- and these tables return at least one row.
-- Separate paragraphs in body with a blank line.

create table if not exists public.trends (
  slug text primary key,
  title text not null,
  body text not null,
  published_at timestamptz not null default now()
);

create table if not exists public.posts (
  slug text primary key,
  title text not null,
  excerpt text not null,
  body text not null,
  published_at timestamptz not null default now()
);

alter table public.trends enable row level security;
alter table public.posts enable row level security;

drop policy if exists "public read trends" on public.trends;
drop policy if exists "public read posts" on public.posts;

create policy "public read trends" on public.trends
  for select using (true);

create policy "public read posts" on public.posts
  for select using (true);

grant select on public.trends to anon, authenticated;
grant select on public.posts to anon, authenticated;
