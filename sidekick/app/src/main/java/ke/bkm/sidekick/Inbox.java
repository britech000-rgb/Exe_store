package ke.bkm.sidekick;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Today's messages, stored only in this app's private files.
 * Nothing here is sent to a network or to the research call.
 */
final class Inbox {
    private static final Object LOCK = new Object();
    private static final int KEEP = 40;
    private static final int SPEAK = 4;

    private Inbox() {}

    static void note(Context context, String app, String title, String body) {
        String line = System.currentTimeMillis()
                + "\t" + clean(app)
                + "\t" + clean(title)
                + "\t" + clean(body);
        synchronized (LOCK) {
            List<String> kept = readFresh(context);
            kept.add(line);
            if (kept.size() > KEEP) kept = kept.subList(kept.size() - KEEP, kept.size());
            write(context, kept);
        }
    }

    static String summary(Context context) {
        List<Item> items = new ArrayList<>();
        synchronized (LOCK) {
            for (String line : readFresh(context)) {
                Item item = parse(line);
                if (item != null) items.add(item);
            }
        }
        if (context.checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            readSms(context, items);
        }
        if (items.isEmpty()) {
            boolean alerts = false;
            String enabled = android.provider.Settings.Secure.getString(
                    context.getContentResolver(), "enabled_notification_listeners");
            if (enabled != null && enabled.contains(context.getPackageName())) alerts = true;
            boolean sms = context.checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
            if (!alerts && !sms) {
                return "Allow alerts or texts on the BKM screen. I read them on this phone only, and I do not send them anywhere.";
            }
            return "Nobody has texted you today.";
        }
        StringBuilder spoken = new StringBuilder();
        int count = items.size();
        spoken.append(count == 1 ? "One message today. " : count + " messages today. ");
        int shown = Math.min(SPEAK, count);
        for (int i = 0; i < shown; i++) {
            Item item = items.get(count - 1 - i);
            spoken.append(item.who);
            if (item.app != null && !item.app.isEmpty() && !item.who.toLowerCase().contains(item.app.toLowerCase())) {
                spoken.append(" on ").append(item.app);
            }
            spoken.append(" said ").append(item.text).append(". ");
        }
        if (count > shown) spoken.append("There are more.");
        String text = spoken.toString().trim();
        if (text.length() > 360) text = text.substring(0, 357) + ".";
        return text;
    }

    private static void readSms(Context context, List<Item> items) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    Uri.parse("content://sms/inbox"),
                    new String[]{"address", "body", "date"},
                    "date>=?",
                    new String[]{Long.toString(startOfDay())},
                    "date DESC");
            if (cursor == null) return;
            int added = 0;
            while (cursor.moveToNext() && added < SPEAK) {
                String address = cursor.getString(0);
                String body = cursor.getString(1);
                if (body == null || body.trim().isEmpty()) continue;
                Item item = new Item();
                item.app = "Messages";
                item.who = nameFor(context, address);
                item.text = clip(body);
                if (!duplicate(items, item)) {
                    items.add(item);
                    added++;
                }
            }
        } catch (SecurityException ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private static boolean duplicate(List<Item> items, Item item) {
        for (Item have : items) {
            if (have.text.equalsIgnoreCase(item.text)) return true;
        }
        return false;
    }

    private static String nameFor(Context context, String address) {
        if (address == null || address.trim().isEmpty()) return "Someone";
        if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return address;
        }
        Cursor cursor = null;
        try {
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
            cursor = context.getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(0);
                if (name != null && !name.trim().isEmpty()) return name.trim();
            }
        } catch (SecurityException ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return address;
    }

    private static List<String> readFresh(Context context) {
        List<String> kept = new ArrayList<>();
        File file = file(context);
        if (!file.exists()) return kept;
        long start = startOfDay();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                int tab = line.indexOf('\t');
                if (tab <= 0) continue;
                try {
                    long when = Long.parseLong(line.substring(0, tab));
                    if (when >= start) kept.add(line);
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException ignored) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return kept;
    }

    private static void write(Context context, List<String> lines) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file(context), false);
            for (String line : lines) {
                writer.write(line);
                writer.write('\n');
            }
        } catch (IOException ignored) {
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static Item parse(String line) {
        String[] parts = line.split("\t", 4);
        if (parts.length < 4) return null;
        Item item = new Item();
        item.app = parts[1];
        item.who = parts[2].isEmpty() ? "Someone" : parts[2];
        item.text = parts[3].isEmpty() ? "a message" : parts[3];
        return item;
    }

    private static File file(Context context) {
        return new File(context.getFilesDir(), "today.txt");
    }

    private static long startOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private static String clean(String value) {
        if (value == null) return "";
        return clip(value).replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').trim();
    }

    private static String clip(String value) {
        String text = value.trim();
        if (text.length() > 140) text = text.substring(0, 137) + "...";
        return text;
    }

    private static final class Item {
        String app = "";
        String who = "Someone";
        String text = "";
    }
}
