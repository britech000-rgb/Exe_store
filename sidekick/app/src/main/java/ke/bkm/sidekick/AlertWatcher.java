package ke.bkm.sidekick;

import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class AlertWatcher extends NotificationListenerService {
    @Override
    public void onListenerConnected() {
        publish();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        remember(sbn);
        publish();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        publish();
    }

    private void remember(StatusBarNotification sbn) {
        if (sbn == null || getPackageName().equals(sbn.getPackageName())) return;
        Notification notification = sbn.getNotification();
        if (notification == null || notification.extras == null) return;
        if ((notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return;
        if ((notification.flags & Notification.FLAG_ONGOING_EVENT) != 0) return;
        if (!worthKeeping(sbn.getPackageName(), notification)) return;
        CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
        if (text == null) text = notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        if ((title == null || title.length() == 0) && (text == null || text.length() == 0)) return;
        Inbox.note(this, appName(sbn.getPackageName()),
                title == null ? "" : title.toString(),
                text == null ? "" : text.toString());
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

    private void publish() {
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
        sendBroadcast(intent);
    }
}
