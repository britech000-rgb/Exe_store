package ke.bkm.sidekick;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SidekickService extends Service {
    public static final String ACTION_STOP = "ke.bkm.sidekick.STOP";
    public static final String ACTION_COUNT = "ke.bkm.sidekick.COUNT";
    public static final String ACTION_SAY = "ke.bkm.sidekick.SAY";
    static final String EXTRA_COUNT = "count";
    static final String EXTRA_LINE = "line";

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
    private Bitmap idle;
    private Bitmap think;
    private Bitmap asleep;
    private TextToSpeech tts;
    private SpeechRecognizer speech;
    private int listenGen;
    private int lastCount = -1;
    private int lastBatteryBand = -1;
    private boolean chatOpen;
    private boolean moved;
    private float downRawX;
    private float downRawY;
    private long lastFrame;
    private long saveAt;
    private long tapToken;
    private boolean webWasOn;

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
            int nx = Math.round(motion.x);
            int ny = Math.round(motion.y);
            if (nx != params.x || ny != params.y) {
                params.x = nx;
                params.y = ny;
                try {
                    windows.updateViewLayout(body, params);
                } catch (IllegalArgumentException ignored) {
                    return;
                }
            }
            if (webs != null && (motion.webOn || webWasOn)) {
                webs.handX = params.x + bw * 0.5f;
                webs.handY = params.y + bh * 0.32f;
                webs.invalidate();
            }
            webWasOn = motion.webOn;
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
        if (intent != null && ACTION_SAY.equals(intent.getAction())) {
            String line = intent.getStringExtra(EXTRA_LINE);
            say(line == null || line.isEmpty() ? "Time for that reminder." : line);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Choreographer.getInstance().removeFrameCallback(loop);
        handler.removeCallbacksAndMessages(null);
        listenGen++;
        if (speech != null) {
            speech.destroy();
            speech = null;
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
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
            startForeground(NOTICE, notification, foregroundTypes());
        } else {
            startForeground(NOTICE, notification);
        }
    }

    private int foregroundTypes() {
        int type = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            type |= ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;
        }
        return type;
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
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS && tts != null) tts.setLanguage(Locale.US);
        });

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
        chatParams = overlay(dp(250), WindowManager.LayoutParams.WRAP_CONTENT, false);
        chatParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        chatParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
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
                if (!moved) onTap();
                return true;
            default:
                return false;
        }
    }

    private void onTap() {
        long now = SystemClock.uptimeMillis();
        if (tapToken != 0 && now - tapToken < 320) {
            tapToken = 0;
            if (!chatOpen) toggleChat();
            startListening();
            return;
        }
        tapToken = now;
        long token = now;
        handler.postDelayed(() -> {
            if (tapToken == token) {
                tapToken = 0;
                toggleChat();
            }
        }, 280);
    }

    private void paintBody() {
        String clip = motion.clip();
        Bitmap bitmap = frameFor(clip);
        boolean feet = !"swim".equals(clip);
        body.show(bitmap, feet, motion.faceRight, motion.ceiling, motion.lean, motion.bob, motion.squash);
    }

    private Bitmap frameFor(String clip) {
        if ("idle".equals(clip)) return idle != null ? idle : swing;
        if ("think".equals(clip)) return think != null ? think : swing;
        if ("sleep".equals(clip)) return asleep != null ? asleep : swing;
        if ("swing".equals(clip)) return swing;
        if ("fall".equals(clip)) return fall;
        Bitmap[] set;
        if ("climb".equals(clip)) set = climb;
        else if ("swim".equals(clip)) set = swim;
        else if ("dance".equals(clip)) set = dance;
        else if ("web".equals(clip)) set = web;
        else set = walk;
        if (set == null || set.length == 0) return swing;
        return set[Math.floorMod(motion.frame, set.length)];
    }

    private void toggleChat() {
        chatOpen = !chatOpen;
        chat.setVisibility(chatOpen ? View.VISIBLE : View.GONE);
        if (chatOpen) {
            chatParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            transcript.setText(R.string.greeting);
            placeChat();
        } else {
            chatParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        }
        try {
            windows.updateViewLayout(chat, chatParams);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void placeChat() {
        int bw = Math.max(body.getWidth(), params.width);
        int gap = dp(8);
        int chatW = dp(250);
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
        say(replyTo(text));
    }

    private void startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            say("Open BKM and allow the microphone.");
            return;
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            say("Speech is not available on this phone.");
            return;
        }
        final int gen = ++listenGen;
        if (speech != null) {
            speech.destroy();
            speech = null;
        }
        try {
            speech = SpeechRecognizer.createSpeechRecognizer(this);
        } catch (SecurityException ex) {
            say("Open BKM again so I can use the microphone.");
            return;
        }
        speech.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}

            @Override
            public void onError(int error) {
                if (gen != listenGen) return;
                say("I didn't catch that.");
            }

            @Override
            public void onResults(Bundle results) {
                if (gen != listenGen) return;
                listenGen++;
                ArrayList<String> list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (list == null || list.isEmpty()) {
                    say("I didn't catch that.");
                    return;
                }
                say(replyTo(list.get(0)));
            }
        });
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        try {
            speech.startListening(intent);
        } catch (SecurityException ex) {
            say("Open BKM again so I can use the microphone.");
            return;
        }
        if (body != null) body.setLine("Listening.");
    }

    private String replyTo(String raw) {
        String q = raw.toLowerCase(Locale.US).trim();
        int bw = Math.max(body == null ? 0 : body.getWidth(), params == null ? 0 : params.width);
        if (q.equals("bigger") || q.startsWith("bigger") || q.contains("make you bigger")) {
            motion.command("bigger", screenW(), bw);
            applySize();
            SidekickPrefs.setScale(this, motion.scale);
            return "Bigger.";
        }
        if (q.equals("smaller") || q.startsWith("smaller") || q.contains("make you smaller")) {
            motion.command("smaller", screenW(), bw);
            applySize();
            SidekickPrefs.setScale(this, motion.scale);
            return "Smaller.";
        }
        if (wantsMusic(q)) {
            String query = musicQuery(q);
            if (startPlayback(query)) return "Playing " + query + ".";
            return "Install YouTube Music, then say play " + query + " again. I start it myself.";
        }
        if (wantsInbox(q)) return Inbox.summary(this);
        if (asks(q, "swim")) {
            motion.command("swim", screenW(), bw);
            return "Swimming across.";
        }
        if (asks(q, "climb")) {
            motion.command("climb", screenW(), bw);
            return "Climbing.";
        }
        if (asks(q, "dance")) {
            motion.command("dance", screenW(), bw);
            return "Dancing.";
        }
        if (q.equals("play") || asks(q, "jump") || asks(q, "hop")) {
            motion.command("play", screenW(), bw);
            return "Playing.";
        }
        if (q.equals("web") || q.contains("a web") || q.contains("throw a web") || asks(q, "swing")) {
            motion.command("web", screenW(), bw);
            return "Throwing a web.";
        }
        if (asks(q, "run")) {
            motion.command("run", screenW(), bw);
            return "Running.";
        }
        if (asks(q, "walk")) {
            motion.command("walk", screenW(), bw);
            return "Walking the floor.";
        }
        if (asks(q, "sit") || q.contains("sit down")) {
            motion.command("sit", screenW(), bw);
            return "Sitting for a bit.";
        }
        if (q.isEmpty() || q.matches("^(hi|hey|yo|yooh|hello)[.! ]*$")) return getString(R.string.greeting);
        if (q.contains("battery") || q.contains("charge")) return batteryLine();
        if (q.contains("notification") || q.contains("alert")) {
            if (lastCount < 0) return "Allow alert reading, and I will count them and read what they say on this phone.";
            return "You got " + lastCount + " notifications.";
        }
        if (q.contains("break") || q.contains("tired") || q.contains("rest")) {
            motion.command("dance", screenW(), bw);
            return "Time to take a break bro.";
        }
        if (q.contains("sleep") || q.contains("good night")) {
            motion.command("sleep", screenW(), bw);
            return "I will rest here. Tap me if you need me.";
        }
        if (q.contains("thank")) return "Anytime.";
        if (q.contains("what time") || q.contains("the time") || q.equals("time")) return PhoneAssist.timeLine();
        if (q.contains("what day") || q.contains("the date") || q.contains("today's date")) return PhoneAssist.dateLine();
        if (q.startsWith("open ")) return PhoneAssist.openApp(this, raw.trim().substring(5));
        if (q.startsWith("call ") || q.startsWith("dial ") || q.startsWith("phone ")) {
            return PhoneAssist.dial(this, raw.trim().replaceFirst("(?i)^(call|dial|phone)\\s+", ""));
        }
        if (q.contains("timer") && (q.contains("set") || q.contains(" for") || q.startsWith("timer"))) {
            return PhoneAssist.timer(this, timerSeconds(q));
        }
        if (q.contains("alarm")) return clockAlarm(q);
        Matcher minutes = Pattern.compile("(\\d+)").matcher(q);
        if ((q.contains("remind") || q.contains("meeting")) && minutes.find()) {
            int mins = Integer.parseInt(minutes.group(1));
            return PhoneAssist.remind(this, mins, "Time for that reminder.");
        }
        if (wantsResearch(q)) return research(q);
        return "I move on my own. Ask me to play a song, who texted you, open an app, set a timer, or look something up.";
    }

    private int timerSeconds(String q) {
        Matcher clock = Pattern.compile("(\\d+)\\s*(hour|minute|second|min|sec|hr)").matcher(q);
        if (clock.find()) {
            int amount = Integer.parseInt(clock.group(1));
            String unit = clock.group(2);
            if (unit.startsWith("hour") || unit.startsWith("hr")) return amount * 3600;
            if (unit.startsWith("sec")) return amount;
            return amount * 60;
        }
        Matcher number = Pattern.compile("(\\d+)").matcher(q);
        if (number.find()) return Integer.parseInt(number.group(1)) * 60;
        return 60;
    }

    private String clockAlarm(String q) {
        Matcher clock = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?").matcher(q);
        if (!clock.find()) return "Say alarm for 7:30.";
        int hour = Integer.parseInt(clock.group(1));
        int minute = clock.group(2) == null ? 0 : Integer.parseInt(clock.group(2));
        String half = clock.group(3);
        if ("pm".equals(half) && hour < 12) hour += 12;
        if ("am".equals(half) && hour == 12) hour = 0;
        if (hour > 23 || minute > 59) return "Say alarm for 7:30.";
        return PhoneAssist.alarm(this, hour, minute);
    }

    private boolean asks(String q, String verb) {
        return q.equals(verb) || q.startsWith(verb + " ") || q.endsWith(" " + verb) || q.contains(" " + verb + " ");
    }

    private boolean wantsMusic(String q) {
        return q.contains("song") || q.contains("music") || q.contains("play me")
                || (q.startsWith("play ") && q.length() > 5);
    }

    private String musicQuery(String q) {
        String query = q.replace("play me", "")
                .replace("a song", "")
                .replace("song", "")
                .replace("music", "")
                .replace("play", "")
                .replaceAll("\\s+", " ")
                .trim();
        if (query.isEmpty()) return "music";
        return query.replace("kingvon", "king von").replaceAll("\\s+", " ").trim();
    }

    private boolean wantsInbox(String q) {
        return q.contains("texted") || q.contains("texts") || q.contains("text me")
                || q.contains("messag") || q.contains("sms") || q.contains("who wrote")
                || q.contains("inbox") || q.contains("read my")
                || (q.contains("text") && (q.contains("who") || q.contains("today") || q.contains("read")));
    }

    private boolean wantsResearch(String q) {
        if (q.contains("research") || q.contains("idea") || q.contains("explain")
                || q.contains("what is") || q.contains("who is") || q.contains("how do")
                || q.contains("how to") || q.contains("why ")) return true;
        return q.split("\\s+").length >= 5;
    }

    /** The question string only. Inbox text is never passed in. */
    private String research(String question) {
        if (!SidekickPrefs.hasAiKey(this)) {
            openSearch(question);
            return "Add a research key on the BKM screen and I can answer here. I opened a search.";
        }
        new Thread(() -> {
            String answer = Research.answer(SidekickService.this, question);
            handler.post(() -> say(answer));
        }, "bkm-research").start();
        return "Looking that up.";
    }

    /** Starts playback in a music app the phone already has. The song stays in that app. */
    private boolean startPlayback(String query) {
        if (playIn("com.google.android.apps.youtube.music", query)) return true;
        if (playIn("com.spotify.music", query)) return true;
        String url = "https://music.youtube.com/search?q=" + Uri.encode(query);
        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        browser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(browser);
            return true;
        } catch (ActivityNotFoundException ignored) {
            return false;
        }
    }

    private boolean playIn(String pkg, String query) {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        intent.setPackage(pkg);
        intent.putExtra(SearchManager.QUERY, query);
        intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/audio");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException ignored) {
            return false;
        }
    }

    private void openSearch(String query) {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, query);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException primary) {
            Intent browser = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)));
            browser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(browser);
            } catch (ActivityNotFoundException ignored) {
            }
        }
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
        if (text == null || text.isEmpty() || body == null) return;
        if (transcript != null && chatOpen) transcript.setText(text);
        String spoken = text.length() > 350 ? text.substring(0, 350) : text;
        if (tts != null) tts.speak(spoken, TextToSpeech.QUEUE_FLUSH, null, "bkm");
        String bubble = spoken.length() > 48 ? spoken.substring(0, 46) + "…" : spoken;
        body.setLine(bubble);
        handler.postDelayed(() -> {
            if (body != null) body.setLine(null);
        }, 5200);
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
        row.addView(chip(getString(R.string.talk), v -> startListening()));
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
        idle = decode(R.drawable.bkm_idle);
        think = decode(R.drawable.bkm_think);
        asleep = decode(R.drawable.bkm_sleep);
    }

    private Bitmap[] loadSet(String prefix) {
        List<Bitmap> found = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            int id = getResources().getIdentifier(prefix + i, "drawable", getPackageName());
            if (id == 0) break;
            found.add(decode(id));
        }
        return found.toArray(new Bitmap[0]);
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
        if (windows == null) return getResources().getDisplayMetrics().widthPixels;
        if (Build.VERSION.SDK_INT >= 30) return windows.getCurrentWindowMetrics().getBounds().width();
        return getResources().getDisplayMetrics().widthPixels;
    }

    private int screenH() {
        if (windows == null) return getResources().getDisplayMetrics().heightPixels;
        if (Build.VERSION.SDK_INT >= 30) return windows.getCurrentWindowMetrics().getBounds().height();
        return getResources().getDisplayMetrics().heightPixels;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
