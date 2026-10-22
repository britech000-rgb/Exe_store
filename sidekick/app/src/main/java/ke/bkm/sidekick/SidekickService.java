package ke.bkm.sidekick;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SidekickService extends Service {
    public static final String ACTION_STOP = "ke.bkm.sidekick.STOP";
    public static final String ACTION_COUNT = "ke.bkm.sidekick.COUNT";
    static final String EXTRA_COUNT = "count";

    private static final int NOTICE = 7;
    private static final int[] POSES = {
            R.drawable.bkm_idle,
            R.drawable.bkm_swing,
            R.drawable.bkm_land,
            R.drawable.bkm_dance,
            R.drawable.bkm_think,
            R.drawable.bkm_remind,
            R.drawable.bkm_sleep,
            R.drawable.bkm_celebrate,
            R.drawable.bkm_reaction
    };

    private final Handler handler = new Handler(Looper.getMainLooper());
    private WindowManager windows;
    private View root;
    private ImageView mascot;
    private TextView bubble;
    private LinearLayout chat;
    private EditText input;
    private TextView transcript;
    private WindowManager.LayoutParams params;
    private int poseIndex;
    private long holdUntil;
    private int lastCount = -1;
    private int lastBatteryBand = -1;
    private boolean chatOpen;
    private float downRawX;
    private float downRawY;
    private int downX;
    private int downY;

    private final Runnable animate = new Runnable() {
        @Override
        public void run() {
            if (SystemClock.uptimeMillis() >= holdUntil) {
                poseIndex = (poseIndex + 1) % POSES.length;
                mascot.setImageResource(POSES[poseIndex]);
            }
            handler.postDelayed(this, 2600);
        }
    };

    private final BroadcastReceiver commands = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            if (ACTION_STOP.equals(intent.getAction())) {
                SidekickPrefs.setEnabled(context, false);
                stopSelf();
                return;
            }
            if (ACTION_COUNT.equals(intent.getAction())) {
                int count = intent.getIntExtra(EXTRA_COUNT, 0);
                if (lastCount >= 0 && count > lastCount) {
                    say("You got " + (count - lastCount) + " new notifications.", R.drawable.bkm_reaction, 5000);
                }
                lastCount = count;
            }
        }
    };

    private final BroadcastReceiver battery = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            if (level < 0 || scale <= 0) return;
            int percent = Math.round(level * 100f / scale);
            int band = percent <= 15 ? 15 : percent <= 20 ? 20 : 100;
            if (band < 100 && band != lastBatteryBand) {
                say("Battery at " + percent + "%. Charge soon.", R.drawable.bkm_remind, 6000);
            }
            lastBatteryBand = band;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            SidekickPrefs.setEnabled(this, false);
            stopSelf();
            return START_NOT_STICKY;
        }
        startLiving();
        if (root == null) attach();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        try {
            unregisterReceiver(commands);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            unregisterReceiver(battery);
        } catch (IllegalArgumentException ignored) {
        }
        if (root != null) {
            windows.removeView(root);
            root = null;
        }
        super.onDestroy();
    }

    private void startLiving() {
        String channelId = "bkm-living";
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager.getNotificationChannel(channelId) == null) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, getString(R.string.channel), NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }
        PendingIntent open = PendingIntent.getActivity(
                this, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        PendingIntent stop = PendingIntent.getService(
                this,
                2,
                new Intent(this, SidekickService.class).setAction(ACTION_STOP),
                PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notice)
                .setContentTitle(getString(R.string.living))
                .setContentText(getString(R.string.living_detail))
                .setContentIntent(open)
                .addAction(new Notification.Action.Builder(null, getString(R.string.stop), stop).build())
                .setOngoing(true)
                .build();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTICE, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTICE, notification);
        }
    }

    private void attach() {
        if (!Settings.canDrawOverlays(this)) {
            SidekickPrefs.setEnabled(this, false);
            stopSelf();
            return;
        }
        windows = getSystemService(WindowManager.class);
        root = buildOverlay();
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = SidekickPrefs.x(this);
        params.y = SidekickPrefs.y(this);
        windows.addView(root, params);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_COUNT);
        filter.addAction(ACTION_STOP);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(commands, filter, RECEIVER_NOT_EXPORTED);
            registerReceiver(battery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED), RECEIVER_EXPORTED);
        } else {
            registerReceiver(commands, filter);
            registerReceiver(battery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
        mascot.animate().translationY(-16f).setDuration(900)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(this::bobBack)
                .start();
        handler.postDelayed(animate, 2600);
        say(getString(R.string.greeting), R.drawable.bkm_idle, 4200);
    }

    private void bobBack() {
        if (mascot == null) return;
        mascot.animate().translationY(0f).setDuration(900)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    if (mascot != null) {
                        mascot.animate().translationY(-16f).setDuration(900)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .withEndAction(this::bobBack)
                                .start();
                    }
                })
                .start();
    }

    private View buildOverlay() {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        bubble = new TextView(this);
        styleBubble(bubble);
        bubble.setVisibility(View.GONE);
        column.addView(bubble);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.BOTTOM);
        mascot = new ImageView(this);
        mascot.setImageResource(R.drawable.bkm_swing);
        mascot.setAdjustViewBounds(true);
        int width = dp(132);
        mascot.setLayoutParams(new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT));
        mascot.setOnTouchListener(this::onMascotTouch);
        row.addView(mascot);

        chat = new LinearLayout(this);
        chat.setOrientation(LinearLayout.VERTICAL);
        chat.setPadding(dp(12), dp(12), dp(12), dp(12));
        GradientDrawable card = new GradientDrawable();
        card.setColor(0xF21C1914);
        card.setCornerRadius(dp(16));
        chat.setBackground(card);
        LinearLayout.LayoutParams chatParams = new LinearLayout.LayoutParams(dp(220), LinearLayout.LayoutParams.WRAP_CONTENT);
        chatParams.leftMargin = dp(8);
        chat.setLayoutParams(chatParams);
        chat.setVisibility(View.GONE);

        transcript = new TextView(this);
        transcript.setTextColor(0xFFF7F3EA);
        transcript.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        transcript.setText(R.string.greeting);
        chat.addView(transcript);

        input = new EditText(this);
        input.setHint(R.string.ask_hint);
        input.setHintTextColor(0xFFC8BBA4);
        input.setTextColor(0xFFF7F3EA);
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_SEND);
        input.setBackgroundColor(0x00000000);
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submit();
                return true;
            }
            return false;
        });
        chat.addView(input);

        TextView send = new TextView(this);
        send.setText(R.string.send);
        send.setTextColor(0xFF12100C);
        send.setBackground(pill());
        send.setPadding(dp(12), dp(8), dp(12), dp(8));
        send.setOnClickListener(v -> submit());
        chat.addView(send);
        row.addView(chat);
        column.addView(row);
        return column;
    }

    private boolean onMascotTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downRawX = event.getRawX();
                downRawY = event.getRawY();
                downX = params.x;
                downY = params.y;
                return true;
            case MotionEvent.ACTION_MOVE:
                params.x = downX + Math.round(event.getRawX() - downRawX);
                params.y = downY + Math.round(event.getRawY() - downRawY);
                windows.updateViewLayout(root, params);
                return true;
            case MotionEvent.ACTION_UP:
                float moved = Math.abs(event.getRawX() - downRawX) + Math.abs(event.getRawY() - downRawY);
                if (moved < dp(8)) toggleChat();
                else SidekickPrefs.place(this, params.x, params.y);
                return true;
            default:
                return false;
        }
    }

    private void toggleChat() {
        chatOpen = !chatOpen;
        chat.setVisibility(chatOpen ? View.VISIBLE : View.GONE);
        if (chatOpen) {
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            transcript.setText(R.string.greeting);
            hold(R.drawable.bkm_idle, 8000);
        } else {
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        }
        windows.updateViewLayout(root, params);
    }

    private void submit() {
        String text = input.getText().toString();
        input.setText("");
        String answer = replyTo(text);
        transcript.setText(answer);
        int pose = poseFor(answer);
        say(answer, pose, 7000);
    }

    private String replyTo(String raw) {
        String q = raw.toLowerCase(Locale.US).trim();
        if (q.isEmpty() || q.matches(".*(hi|hey|yo|hello).*")) {
            return getString(R.string.greeting);
        }
        if (q.contains("battery") || q.contains("charge")) return batteryLine();
        if (q.contains("notification") || q.contains("alert")) {
            if (lastCount < 0) {
                return "Allow alert count, and I will tell you how many. I will not read the messages.";
            }
            return "You got " + lastCount + " notifications.";
        }
        if (q.contains("break") || q.contains("tired") || q.contains("rest")) {
            return "Time to take a break bro.";
        }
        if (q.contains("sleep") || q.contains("good night")) {
            return "I will rest here. Tap me if you need me.";
        }
        if (q.contains("thank")) return "Anytime.";
        Matcher minutes = Pattern.compile("(\\d+)").matcher(q);
        if ((q.contains("remind") || q.contains("meeting")) && minutes.find()) {
            int mins = Integer.parseInt(minutes.group(1));
            handler.postDelayed(() -> say("Time for that reminder.", R.drawable.bkm_remind, 8000), mins * 60_000L);
            return "Meeting in " + mins + " minutes. I will tap you.";
        }
        return "Tell me the battery, a break, or remind me in 30.";
    }

    private int poseFor(String answer) {
        String q = answer.toLowerCase(Locale.US);
        if (q.contains("break")) return R.drawable.bkm_sleep;
        if (q.contains("rest here")) return R.drawable.bkm_sleep;
        if (q.contains("battery") || q.contains("meeting") || q.contains("reminder")) return R.drawable.bkm_remind;
        if (q.contains("notification")) return R.drawable.bkm_reaction;
        if (q.contains("anytime")) return R.drawable.bkm_celebrate;
        if (q.contains("how can i help")) return R.drawable.bkm_idle;
        return R.drawable.bkm_think;
    }

    private String batteryLine() {
        Intent sticky = Build.VERSION.SDK_INT >= 33
                ? registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED), RECEIVER_EXPORTED)
                : registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (sticky == null) return "I cannot see the battery yet.";
        int level = sticky.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = sticky.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int percent = scale <= 0 ? level : Math.round(level * 100f / scale);
        return "Battery at " + percent + "%." + (percent <= 20 ? " Charge soon." : "");
    }

    private void say(String text, int pose, long ms) {
        if (bubble == null) return;
        bubble.setText(text);
        bubble.setVisibility(View.VISIBLE);
        hold(pose, ms);
        handler.postDelayed(() -> {
            if (bubble != null && text.contentEquals(bubble.getText())) bubble.setVisibility(View.GONE);
        }, ms);
    }

    private void hold(int pose, long ms) {
        holdUntil = SystemClock.uptimeMillis() + ms;
        mascot.setImageResource(pose);
    }

    private void styleBubble(TextView view) {
        view.setTextColor(0xFF12100C);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        view.setPadding(dp(12), dp(8), dp(12), dp(8));
        view.setBackground(pill());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(6);
        view.setLayoutParams(lp);
    }

    private GradientDrawable pill() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(0xFFE2B657);
        drawable.setCornerRadius(dp(18));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
