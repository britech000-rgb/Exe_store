package ke.bkm.sidekick;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class AlertWatcher extends NotificationListenerService {
    @Override
    public void onListenerConnected() {
        publish();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        publish();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        publish();
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
