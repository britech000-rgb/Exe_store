package ke.bkm.sidekick;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.widget.Button;

public class MainActivity extends Activity {
    private static final int POST_NOTIFICATIONS = 21;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button start = findViewById(R.id.start);
        Button alerts = findViewById(R.id.alerts);
        Button stop = findViewById(R.id.stop);

        start.setOnClickListener(v -> putOnScreen());
        alerts.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        stop.setOnClickListener(v -> {
            SidekickPrefs.setEnabled(this, false);
            startService(new Intent(this, SidekickService.class).setAction(SidekickService.ACTION_STOP));
            refresh(start);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh(findViewById(R.id.start));
    }

    private void refresh(Button start) {
        boolean on = SidekickPrefs.enabled(this) && Settings.canDrawOverlays(this);
        start.setText(on ? R.string.already_on : R.string.put_on_screen);
    }

    private void putOnScreen() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, POST_NOTIFICATIONS);
            return;
        }
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }
        SidekickPrefs.setEnabled(this, true);
        startForegroundService(new Intent(this, SidekickService.class));
        refresh(findViewById(R.id.start));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == POST_NOTIFICATIONS) putOnScreen();
    }
}
