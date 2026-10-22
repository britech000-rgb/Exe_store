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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MascotMotion motion = new MascotMotion();
    private WindowManager windows;
    private MascotView body;
    private WebLayer webs;
    private LinearLayout chat;
    private EditText input;
    private TextView transcript;
    private WindowManager.LayoutParams params;
    private WindowManager.LayoutParams chatParams;
    private WindowManager.LayoutParams webParams;
    private ScaleGestureDetector scaleDetector;
    private Bitmap[] walk;
    private Bitmap[] climb;
    private Bitmap[] swim;
    private Bitmap[] dance;
    private Bitmap[] web;
    private Bitmap swing;
    private Bitmap fall;
    private int lastCount = -1;
    private int lastBatteryBand = -1;
    private boolean chatOpen;
    private boolean moved;
    private float downRawX;
    private float downRawY;
    private long lastFrame;
    private long saveAt;

    private final Choreographer.FrameCallback loop = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long now) {
            if (body == null) return;
            float dt = lastFrame == 0L ? 0.016f : Math.min(0.05f, (now - lastFrame) / 1_000_000_000f);
            lastFrame = now;
            int bw = Math.max(body.getWidth(), params.width);
            int bh = Math.max(body.getHeight(), params.height);
            motion.tick(dt, screenW(), screenH(), bw, bh);
            paintBody();
            params.x = Math.round(motion.x);
            params.y = Math.round(motion.y);
            try {
                windows.updateViewLayout(body, params);
            } catch (IllegalArgumentException ignored) {
                return;
            }
            if (webs != null) {
                webs.handX = params.x + bw * 0.5f;
                webs.handY = params.y + bh * 0.32f;
                webs.invalidate();
            }
            if (chatOpen) placeChat();
            if (now > saveAt) {
                saveAt = now + 1_000_000_000L;
                SidekickPrefs.place(SidekickService.this, params.x, params.y);
            }
            Choreographer.getInstance().postFrameCallback(this);
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
                    say("You got " + (count - lastCount) + " new notifications.");
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
                say("Battery at " + percent + "%. Charge soon.");
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
        if (body == null) attach();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Choreographer.getInstance().removeFrameCallback(loop);
        handler.removeCallbacksAndMessages(null);
        try {
            unregisterReceiver(commands);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            unregisterReceiver(battery);
        } catch (IllegalArgumentException ignored) {
        }
        removeWindow(chat);
        removeWindow(body);
        removeWindow(webs);
        body = null;
        chat = null;
        webs = null;
        super.onDestroy();
    }

    private void startLiving() {
        String channelId = "bkm-living";
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(new NotificationChannel(
                    channelId, getString(R.string.channel), NotificationManager.IMPORTANCE_LOW));
        }
        PendingIntent open = PendingIntent.getActivity(
                this, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        PendingIntent stop = PendingIntent.getService(
                this, 2, new Intent(this, SidekickService.class).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE);
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
        motion.setDensity(getResources().getDisplayMetrics().density);
        motion.scale = SidekickPrefs.scale(this);
        motion.x = SidekickPrefs.x(this);
        motion.y = SidekickPrefs.y(this);
        loadFrames();

        webs = new WebLayer(this);
        webs.bind(motion);
        webParams = overlay(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, true);
        windows.addView(webs, webParams);

        body = new MascotView(this);
        params = overlay(paramsWidth(), paramsHeight(), false);
        params.x = Math.round(motion.x);
        params.y = Math.round(motion.y);
        scaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                motion.resize(detector.getScaleFactor());
                applySize();
                SidekickPrefs.setScale(SidekickService.this, motion.scale);
                return true;
            }
        });
        body.setOnTouchListener(this::onBodyTouch);
        windows.addView(body, params);

        chat = buildChat();
        chatParams = overlay(dp(230), WindowManager.LayoutParams.WRAP_CONTENT, false);
        chatParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        chat.setVisibility(View.GONE);
        windows.addView(chat, chatParams);

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
        say(getString(R.string.greeting));
        Choreographer.getInstance().postFrameCallback(loop);
    }

    private boolean onBodyTouch(View view, MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        if (scaleDetector.isInProgress()) return true;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                moved = false;
                downRawX = event.getRawX();
                downRawY = event.getRawY();
                motion.beginDrag(event.getRawX(), event.getRawY());
                return true;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getRawX() - downRawX) + Math.abs(event.getRawY() - downRawY) > dp(10)) {
                    moved = true;
                }
                motion.dragTo(event.getRawX(), event.getRawY());
                return true;
            case MotionEvent.ACTION_UP:
                motion.endGesture(!moved);
                if (!moved) toggleChat();
                return true;
            default:
                return false;
        }
    }

    private void paintBody() {
        String clip = motion.clip();
        Bitmap bitmap = frameFor(clip);
        boolean feet = !"swim".equals(clip);
        body.show(bitmap, feet, motion.faceRight, motion.ceiling, motion.lean, motion.bob, motion.squash);
    }

    private Bitmap frameFor(String clip) {
        Bitmap[] set;
        if ("climb".equals(clip)) set = climb;
        else if ("swim".equals(clip)) set = swim;
        else if ("dance".equals(clip)) set = dance;
        else if ("web".equals(clip)) set = web;
        else if ("swing".equals(clip)) return swing;
        else if ("fall".equals(clip)) return fall;
        else set = walk;
        if (set == null || set.length == 0) return swing;
        return set[Math.floorMod(motion.frame, set.length)];
    }

    private void toggleChat() {
        chatOpen = !chatOpen;
        chat.setVisibility(chatOpen ? View.VISIBLE : View.GONE);
        motion.pin(chatOpen);
        if (chatOpen) {
            transcript.setText(R.string.greeting);
            placeChat();
        }
    }

    private void placeChat() {
        int bw = Math.max(body.getWidth(), params.width);
        int gap = dp(8);
        int chatW = dp(230);
        int x = params.x + bw + gap;
        if (x + chatW > screenW()) x = params.x - chatW - gap;
        if (x < 0) x = dp(8);
        chatParams.x = x;
        chatParams.y = Math.max(dp(8), params.y);
        try {
            windows.updateViewLayout(chat, chatParams);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void submit() {
        String text = input.getText().toString();
        input.setText("");
        transcript.setText(replyTo(text));
    }

    private String replyTo(String raw) {
        String q = raw.toLowerCase(Locale.US).trim();
        int bw = Math.max(body.getWidth(), params.width);
        if (q.contains("bigger") || q.contains("large")) {
            motion.command("bigger", screenW(), bw);
            applySize();
            SidekickPrefs.setScale(this, motion.scale);
            return "Bigger.";
        }
        if (q.contains("smaller") || q.contains("small")) {
            motion.command("smaller", screenW(), bw);
            applySize();
            SidekickPrefs.setScale(this, motion.scale);
            return "Smaller.";
        }
        if (q.contains("swim")) {
            motion.command("swim", screenW(), bw);
            return "Swimming across.";
        }
        if (q.contains("climb")) {
            motion.command("climb", screenW(), bw);
            return "Climbing.";
        }
        if (q.contains("dance")) {
            motion.command("dance", screenW(), bw);
            return "Dancing.";
        }
        if (q.contains("play") || q.contains("jump")) {
            motion.command("play", screenW(), bw);
            return "Playing.";
        }
        if (q.contains("web") || q.contains("swing")) {
            motion.command("web", screenW(), bw);
            return "Throwing a web.";
        }
        if (q.contains("walk")) {
            motion.command("walk", screenW(), bw);
            return "Walking the floor.";
        }
        if (q.isEmpty() || q.matches(".*(hi|hey|yo|hello).*")) return getString(R.string.greeting);
        if (q.contains("battery") || q.contains("charge")) return batteryLine();
        if (q.contains("notification") || q.contains("alert")) {
            if (lastCount < 0) return "Allow alert count, and I will tell you how many. I will not read the messages.";
            return "You got " + lastCount + " notifications.";
        }
        if (q.contains("break") || q.contains("tired") || q.contains("rest")) {
            motion.command("dance", screenW(), bw);
            return "Time to take a break bro.";
        }
        if (q.contains("sleep") || q.contains("good night")) return "I will rest here. Tap me if you need me.";
        if (q.contains("thank")) return "Anytime.";
        Matcher minutes = Pattern.compile("(\\d+)").matcher(q);
        if ((q.contains("remind") || q.contains("meeting")) && minutes.find()) {
            int mins = Integer.parseInt(minutes.group(1));
            handler.postDelayed(() -> say("Time for that reminder."), mins * 60_000L);
            return "Meeting in " + mins + " minutes. I will tap you.";
        }
        return "Tell me to walk, climb, swim, dance, play, or throw a web. Or ask the battery.";
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

    private void say(String text) {
        if (body == null) return;
        body.setLine(text);
        handler.postDelayed(() -> {
            if (body != null) body.setLine(null);
        }, 4200);
    }

    private LinearLayout buildChat() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(12), dp(12), dp(12), dp(12));
        GradientDrawable card = new GradientDrawable();
        card.setColor(0xF21C1914);
        card.setCornerRadius(dp(16));
        panel.setBackground(card);

        transcript = new TextView(this);
        transcript.setTextColor(0xFFF7F3EA);
        transcript.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        transcript.setText(R.string.greeting);
        panel.addView(transcript);

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
        panel.addView(input);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(chip(getString(R.string.send), v -> submit()));
        row.addView(chip(getString(R.string.smaller), v -> resizeChip(0.85f)));
        row.addView(chip(getString(R.string.bigger), v -> resizeChip(1.18f)));
        panel.addView(row);
        return panel;
    }

    private TextView chip(String label, View.OnClickListener click) {
        TextView view = new TextView(this);
        view.setText(label);
        view.setTextColor(0xFF12100C);
        view.setPadding(dp(10), dp(8), dp(10), dp(8));
        GradientDrawable pill = new GradientDrawable();
        pill.setColor(0xFFE2B657);
        pill.setCornerRadius(dp(16));
        view.setBackground(pill);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = dp(6);
        lp.topMargin = dp(8);
        view.setLayoutParams(lp);
        view.setOnClickListener(click);
        return view;
    }

    private void resizeChip(float factor) {
        motion.resize(factor);
        applySize();
        SidekickPrefs.setScale(this, motion.scale);
    }

    private void applySize() {
        int w = paramsWidth();
        int h = paramsHeight();
        if (params == null || body == null) return;
        params.width = w;
        params.height = h;
        try {
            windows.updateViewLayout(body, params);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private int paramsWidth() {
        return Math.round(dp(138) * motion.scale);
    }

    private int paramsHeight() {
        return Math.round(dp(220) * motion.scale);
    }

    private WindowManager.LayoutParams overlay(int width, int height, boolean passThrough) {
        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        if (passThrough) flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        WindowManager.LayoutParams layout = new WindowManager.LayoutParams(
                width,
                height,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                flags,
                PixelFormat.TRANSLUCENT);
        layout.gravity = Gravity.TOP | Gravity.START;
        return layout;
    }

    private void loadFrames() {
        walk = loadSet("bkm_walk_");
        climb = loadSet("bkm_climb_");
        swim = loadSet("bkm_swim_");
        dance = loadSet("bkm_dance_");
        web = loadSet("bkm_web_");
        swing = decode(R.drawable.bkm_swing);
        fall = decode(R.drawable.bkm_land);
    }

    private Bitmap[] loadSet(String prefix) {
        Bitmap[] found = new Bitmap[4];
        int count = 0;
        for (int i = 0; i < 4; i++) {
            int id = getResources().getIdentifier(prefix + i, "drawable", getPackageName());
            if (id == 0) break;
            found[count++] = decode(id);
        }
        Bitmap[] trimmed = new Bitmap[count];
        System.arraycopy(found, 0, trimmed, 0, count);
        return trimmed;
    }

    private Bitmap decode(int id) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(getResources(), id, options);
    }

    private void removeWindow(View view) {
        if (view == null) return;
        try {
            windows.removeView(view);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private int screenW() {
        if (Build.VERSION.SDK_INT >= 30) return windows.getCurrentWindowMetrics().getBounds().width();
        return getResources().getDisplayMetrics().widthPixels;
    }

    private int screenH() {
        if (Build.VERSION.SDK_INT >= 30) return windows.getCurrentWindowMetrics().getBounds().height();
        return getResources().getDisplayMetrics().heightPixels;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
