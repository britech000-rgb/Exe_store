import "server-only";
import { guides, type Guide } from "@/content/guides";
import { notes, type Note } from "@/content/notes";
import { getSupabase } from "@/lib/supabase";

function paragraphs(body: string) {
  return body
    .split(/\n\s*\n/)
    .map((paragraph) => paragraph.trim())
    .filter(Boolean);
}

export async function getNotes(): Promise<Note[]> {
  const supabase = getSupabase();
  if (!supabase) return notes;
  try {
    const { data, error } = await supabase
      .from("trends")
      .select("slug,title,body,published_at")
      .order("published_at", { ascending: false });
    if (error || !data?.length) return notes;
    return data.map((row) => ({
      slug: String(row.slug),
      title: String(row.title),
      paragraphs: paragraphs(String(row.body)),
    }));
  } catch {
    return notes;
  }
}

export async function getNote(slug: string): Promise<Note | undefined> {
  const all = await getNotes();
  return all.find((note) => note.slug === slug);
}

export async function getGuides(): Promise<Guide[]> {
  const supabase = getSupabase();
  if (!supabase) return guides;
  try {
    const { data, error } = await supabase
      .from("posts")
      .select("slug,title,excerpt,body,published_at")
      .order("published_at", { ascending: false });
    if (error || !data?.length) return guides;
    return data.map((row) => ({
      slug: String(row.slug),
      title: String(row.title),
      excerpt: String(row.excerpt),
      paragraphs: paragraphs(String(row.body)),
    }));
  } catch {
    return guides;
  }
}

export async function getGuide(slug: string): Promise<Guide | undefined> {
  const all = await getGuides();
  return all.find((guide) => guide.slug === slug);
}
