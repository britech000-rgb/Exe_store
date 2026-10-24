package ke.bkm.sidekick;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int POST_NOTIFICATIONS = 21;
    private static final int MIC = 22;
    private static final int TEXTS = 23;
    private static final int CONTACTS = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button start = findViewById(R.id.start);
        Button alerts = findViewById(R.id.alerts);
        Button mic = findViewById(R.id.mic);
        Button texts = findViewById(R.id.texts);
        Button contacts = findViewById(R.id.contacts);
        Button saveKey = findViewById(R.id.save_key);
        Button stop = findViewById(R.id.stop);
        EditText key = findViewById(R.id.ai_key);

        start.setOnClickListener(v -> putOnScreen());
        alerts.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        mic.setOnClickListener(v -> requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC));
        texts.setOnClickListener(v -> requestPermissions(new String[]{Manifest.permission.READ_SMS}, TEXTS));
        contacts.setOnClickListener(v -> requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS));
        saveKey.setOnClickListener(v -> {
            SidekickPrefs.setAiKey(this, key.getText().toString());
            key.setText("");
            refreshKey();
        });
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
        refreshKey();
    }

    private void refreshKey() {
        TextView state = findViewById(R.id.key_state);
        state.setText(SidekickPrefs.hasAiKey(this) ? R.string.key_saved : R.string.key_missing);
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
        if (requestCode == MIC && SidekickPrefs.enabled(this) && Settings.canDrawOverlays(this)) {
            startForegroundService(new Intent(this, SidekickService.class));
        }
    }
}
