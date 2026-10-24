package ke.bkm.sidekick;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.ContactsContract;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Phone tasks that stay on the device. Opens the dialer, the clock, or an
 * app the user named. Does not place a call by itself and does not read
 * message text.
 */
final class PhoneAssist {
    private PhoneAssist() {}

    static String timeLine() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        if (hour == 0) hour = 12;
        int minute = calendar.get(Calendar.MINUTE);
        String half = calendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
        return String.format(Locale.US, "It is %d:%02d %s.", hour, minute, half);
    }

    static String dateLine() {
        Calendar calendar = Calendar.getInstance();
        return String.format(Locale.US, "Today is %1$tA, %1$tB %1$te.", calendar);
    }

    static String remind(Context context, int minutes, String label) {
        int wait = Math.max(1, Math.min(minutes, 24 * 60));
        AlarmManager alarms = context.getSystemService(AlarmManager.class);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(SidekickService.EXTRA_LINE, label);
        int request = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        PendingIntent pending = PendingIntent.getBroadcast(
                context, request, intent, PendingIntent.FLAG_IMMUTABLE);
        long when = System.currentTimeMillis() + wait * 60_000L;
        try {
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pending);
        } catch (SecurityException ex) {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pending);
        }
        return "I will remind you in " + wait + " minutes, even if you leave this screen.";
    }

    static String timer(Context context, int seconds) {
        int length = Math.max(5, Math.min(seconds, 24 * 60 * 60));
        Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER)
                .putExtra(AlarmClock.EXTRA_LENGTH, length)
                .putExtra(AlarmClock.EXTRA_MESSAGE, "BKM")
                .putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            return "This phone has no timer app I can open.";
        }
        context.startActivity(intent);
        int minutes = length / 60;
        int rest = length % 60;
        if (minutes > 0 && rest == 0) return "Timer set for " + minutes + " minutes.";
        if (minutes > 0) return "Timer set for " + minutes + " minutes and " + rest + " seconds.";
        return "Timer set for " + rest + " seconds.";
    }

    static String alarm(Context context, int hour, int minute) {
        int h = Math.max(0, Math.min(hour, 23));
        int m = Math.max(0, Math.min(minute, 59));
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_HOUR, h)
                .putExtra(AlarmClock.EXTRA_MINUTES, m)
                .putExtra(AlarmClock.EXTRA_MESSAGE, "BKM")
                .putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            return "This phone has no clock app I can open.";
        }
        context.startActivity(intent);
        return String.format(Locale.US, "Opening the clock for %d:%02d.", h, m);
    }

    static String dial(Context context, String who) {
        String target = who == null ? "" : who.trim();
        if (target.isEmpty()) return "Say call, then a name or a number.";
        String number = target.replaceAll("[^0-9+]", "");
        boolean digits = number.length() >= 3 && number.length() >= target.replace(" ", "").length() - 1;
        if (!digits) {
            String found = lookup(context, target);
            if (found == null) {
                if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    return "Allow contacts, or say call and the number. I open the dialer. I do not call by myself.";
                }
                return "I cannot find " + target + " in contacts.";
            }
            number = found;
            target = who.trim();
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(number)));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            return "I cannot open the dialer.";
        }
        context.startActivity(intent);
        return "Opening the dialer for " + target + ". You still press call.";
    }

    static String openApp(Context context, String name) {
        String want = name == null ? "" : name.trim().toLowerCase(Locale.US);
        if (want.isEmpty()) return "Say open, then the app name.";
        Intent probe = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(probe, 0);
        ResolveInfo best = null;
        int bestScore = 0;
        if (apps != null) {
            for (ResolveInfo info : apps) {
                if (info.activityInfo == null) continue;
                CharSequence label = info.loadLabel(context.getPackageManager());
                String text = label == null ? "" : label.toString().toLowerCase(Locale.US);
                String pkg = info.activityInfo.packageName.toLowerCase(Locale.US);
                int score = 0;
                if (text.equals(want)) score = 100;
                else if (text.contains(want)) score = 70;
                else if (pkg.contains(want.replace(" ", ""))) score = 40;
                if (score > bestScore) {
                    bestScore = score;
                    best = info;
                }
            }
        }
        if (best == null || best.activityInfo == null) return "I cannot find " + name.trim() + " on this phone.";
        Intent launch = context.getPackageManager().getLaunchIntentForPackage(best.activityInfo.packageName);
        if (launch == null) return "I cannot open " + name.trim() + ".";
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launch);
        CharSequence label = best.loadLabel(context.getPackageManager());
        return "Opening " + (label == null ? name.trim() : label) + ".";
    }

    private static String lookup(Context context, String name) {
        if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Cursor people = null;
        Cursor phones = null;
        try {
            Uri filter = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(name));
            people = context.getContentResolver().query(filter,
                    new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER},
                    null, null, null);
            if (people == null || !people.moveToFirst()) return null;
            if (people.getInt(1) == 0) return null;
            String id = people.getString(0);
            phones = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                    new String[]{id},
                    null);
            if (phones == null || !phones.moveToFirst()) return null;
            return phones.getString(0);
        } catch (SecurityException ex) {
            return null;
        } finally {
            if (people != null) people.close();
            if (phones != null) phones.close();
        }
    }
}
