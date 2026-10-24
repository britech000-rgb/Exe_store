package ke.bkm.sidekick;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Fires a reminder the user asked for. The text is their reminder, not a message from someone else. */
public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String line = intent == null ? null : intent.getStringExtra(SidekickService.EXTRA_LINE);
        if (line == null || line.trim().isEmpty()) line = "Time for that reminder.";
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager.getNotificationChannel("bkm-remind") == null) {
            manager.createNotificationChannel(new NotificationChannel(
                    "bkm-remind", "BKM reminders", NotificationManager.IMPORTANCE_HIGH));
        }
        Notification notification = new Notification.Builder(context, "bkm-remind")
                .setSmallIcon(R.drawable.ic_notice)
                .setContentTitle("BKM")
                .setContentText(line)
                .setAutoCancel(true)
                .build();
        manager.notify((int) (System.currentTimeMillis() % 100000), notification);
        if (!SidekickPrefs.enabled(context)) return;
        try {
            Intent say = new Intent(context, SidekickService.class).setAction(SidekickService.ACTION_SAY);
            say.putExtra(SidekickService.EXTRA_LINE, line);
            context.startForegroundService(say);
        } catch (RuntimeException ignored) {
        }
    }
}
