package ke.bkm.sidekick;

import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Reads message alerts on this phone and speaks them here.
 * The text never leaves the device.
 */
public class AlertWatcher extends NotificationListenerService {
    private String lastSpoken = "";

    @Override
    public void onListenerConnected() {
        publish(null);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        publish(remember(sbn));
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        publish(null);
    }

    private String remember(StatusBarNotification sbn) {
        if (sbn == null || getPackageName().equals(sbn.getPackageName())) return null;
        Notification notification = sbn.getNotification();
        if (notification == null || notification.extras == null) return null;
        if ((notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return null;
        if ((notification.flags & Notification.FLAG_ONGOING_EVENT) != 0) return null;
        if (!worthKeeping(sbn.getPackageName(), notification)) return null;

        String sender = "";
        String body = "";
        String[] parts = messageParts(notification);
        if (parts != null) {
            sender = parts[0];
            body = parts[1];
        } else {
            CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
            if (text == null) text = notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
            body = text == null ? "" : text.toString();
        }
        CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
        String titleText = title == null ? "" : title.toString().trim();
        String who = sender.isEmpty() ? titleText : sender.trim();
        if (who.isEmpty() && body.trim().isEmpty()) return null;
        String app = appName(sbn.getPackageName());
        Inbox.note(this, app, who, body);
        return spokenLine(sbn.getPackageName(), app, who, body);
    }

    /** Last MessagingStyle message. Keys are the platform bundle names "text" and "sender". */
    private String[] messageParts(Notification notification) {
        Parcelable[] messages = notification.extras.getParcelableArray(Notification.EXTRA_MESSAGES);
        if (messages == null || messages.length == 0) return null;
        for (int i = messages.length - 1; i >= 0; i--) {
            if (!(messages[i] instanceof Bundle)) continue;
            Bundle bundle = (Bundle) messages[i];
            CharSequence text = bundle.getCharSequence("text");
            if (text == null || text.toString().trim().isEmpty()) continue;
            CharSequence sender = bundle.getCharSequence("sender");
            return new String[]{sender == null ? "" : sender.toString(), text.toString()};
        }
        return null;
    }

    private String spokenLine(String pkg, String app, String who, String text) {
        String body = text == null ? "" : text.trim().replaceAll("\\s+", " ");
        if (generic(body)) body = "";
        String name = who == null ? "" : who.trim();
        if (body.isEmpty() && (name.isEmpty() || name.equalsIgnoreCase(app))) return null;
        String line = body.isEmpty() ? name + " texted you." : (name.isEmpty() ? body : name + " said " + body);
        if (line.length() > 140) line = line.substring(0, 137) + ".";
        String key = pkg + "\n" + line;
        if (key.equals(lastSpoken)) return null;
        lastSpoken = key;
        return line;
    }

    private boolean generic(String text) {
        String value = text.toLowerCase();
        return value.matches("\\d+ new messages?")
                || value.equals("new message")
                || value.equals("new messages")
                || value.contains("checking for new messages");
    }

    private boolean worthKeeping(String pkg, Notification notification) {
        if (pkg == null) return false;
        if (pkg.equals("android") || pkg.startsWith("com.android.systemui")) return false;
        if (Notification.CATEGORY_MESSAGE.equals(notification.category)) return true;
        String name = pkg.toLowerCase();
        return name.contains("messaging")
                || name.contains("mms")
                || name.contains("sms")
                || name.contains("whatsapp")
                || name.contains("telegram")
                || name.contains("instagram")
                || name.contains("facebook")
                || name.contains("orca")
                || name.contains("snapchat")
                || name.contains("signal")
                || name.contains("viber")
                || name.contains("messenger");
    }

    private String appName(String pkg) {
        try {
            PackageManager manager = getPackageManager();
            CharSequence label = manager.getApplicationLabel(manager.getApplicationInfo(pkg, 0));
            return label == null ? pkg : label.toString();
        } catch (PackageManager.NameNotFoundException ignored) {
            return pkg;
        }
    }

    private void publish(String line) {
        StatusBarNotification[] active = getActiveNotifications();
        int count = 0;
        if (active != null) {
            for (StatusBarNotification item : active) {
                if (!getPackageName().equals(item.getPackageName())) count++;
            }
        }
        Intent intent = new Intent(SidekickService.ACTION_COUNT);
        intent.setPackage(getPackageName());
        intent.putExtra(SidekickService.EXTRA_COUNT, count);
        if (line != null && !line.isEmpty()) intent.putExtra(SidekickService.EXTRA_LINE, line);
        sendBroadcast(intent);
    }
}
