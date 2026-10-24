package ke.bkm.sidekick;

import android.content.Context;
import android.content.SharedPreferences;

final class SidekickPrefs {
    private static final String FILE = "bkm";

    private SidekickPrefs() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    static boolean enabled(Context context) {
        return prefs(context).getBoolean("on", false);
    }

    static void setEnabled(Context context, boolean on) {
        prefs(context).edit().putBoolean("on", on).apply();
    }

    static int x(Context context) {
        return prefs(context).getInt("x", 24);
    }

    static int y(Context context) {
        return prefs(context).getInt("y", 180);
    }

    static void place(Context context, int x, int y) {
        prefs(context).edit().putInt("x", x).putInt("y", y).apply();
    }

    static void setScale(Context context, float scale) {
        prefs(context).edit().putFloat("scale", scale).apply();
    }

    static float scale(Context context) {
        return prefs(context).getFloat("scale", 1f);
    }

    static void setAiKey(Context context, String key) {
        String clean = key == null ? "" : key.trim();
        prefs(context).edit().putString("ai", clean).apply();
    }

    static String aiKey(Context context) {
        return prefs(context).getString("ai", "");
    }

    static boolean hasAiKey(Context context) {
        String key = aiKey(context);
        return key != null && key.length() > 10;
    }
}
