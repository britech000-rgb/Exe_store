package ke.bkm.sidekick;

import java.util.Random;

/**
 * Shimeji-style screen pet. He picks his own actions. Nothing on screen
 * is a button for walk, climb, or swim. Voice requests can still ask.
 * Position is the top-left of his window.
 */
final class MascotMotion {
    enum Act { WALK, RUN, STAND, SIT, LOOK, SLEEP, CLIMB, CEILING, SWIM, DANCE, PLAY, WEB, FALL, DRAG }
    enum Side { LEFT, RIGHT }

    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 2.4f;

    Act act = Act.FALL;
    Side wall = Side.RIGHT;
    float x = 48f;
    float y = 48f;
    float vx;
    float vy;
    float scale = 1f;
    boolean faceRight = true;
    boolean ceiling;
    boolean climbUp = true;
    int frame;
    float bob;
    float lean;
    float squash = 1f;
    boolean webOn;
    float webAx;
    float webAy;
    float webGrow;

    private final Random random = new Random();
    private float density = 3f;
    private float frameTime;
    private float actTime;
    private float limit = 2f;
    private float swingAngle;
    private float swingVel;
    private float webLen;
    private int hops = 3;
    private float dragDx;
    private float dragDy;
    private Act beforeDrag = Act.WALK;
    private int screenW = 1080;
    private int bodyW = 300;

    void setDensity(float density) {
        this.density = density;
    }

    void resize(float factor) {
        scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale * factor));
    }

    void beginDrag(float fingerX, float fingerY) {
        beforeDrag = act == Act.DRAG ? beforeDrag : act;
        act = Act.DRAG;
        actTime = 0f;
        webOn = false;
        ceiling = false;
        dragDx = fingerX - x;
        dragDy = fingerY - y;
        vx = 0f;
        vy = 0f;
    }

    void dragTo(float fingerX, float fingerY) {
        x = fingerX - dragDx;
        y = fingerY - dragDy;
    }

    void endGesture(boolean tap) {
        if (tap) {
            act = beforeDrag == Act.DRAG || beforeDrag == Act.FALL ? Act.WALK : beforeDrag;
            return;
        }
        act = Act.FALL;
        vy = 80f;
        actTime = 0f;
        webOn = false;
        ceiling = false;
    }

    void command(String name, int screenW, int bodyW) {
        this.screenW = screenW;
        this.bodyW = bodyW;
        if ("walk".equals(name)) startWalk();
        else if ("run".equals(name)) startRun();
        else if ("climb".equals(name)) startClimb(faceRight ? Side.RIGHT : Side.LEFT, true);
        else if ("swim".equals(name)) startSwim();
        else if ("dance".equals(name)) startDance();
        else if ("play".equals(name)) startPlay();
        else if ("web".equals(name)) startWeb();
        else if ("sleep".equals(name)) startSleep();
        else if ("sit".equals(name)) startSit();
        else if ("bigger".equals(name)) resize(1.18f);
        else if ("smaller".equals(name)) resize(0.85f);
    }

    String clip() {
        if (act == Act.FALL) return "fall";
        if (act == Act.SWIM) return "swim";
        if (act == Act.DANCE || act == Act.PLAY) return "dance";
        if (act == Act.WEB) return webGrow < 1f ? "web" : "swing";
        if (act == Act.CLIMB) return "climb";
        if (act == Act.STAND || act == Act.SIT) return "idle";
        if (act == Act.LOOK) return "think";
        if (act == Act.SLEEP) return "sleep";
        return "walk";
    }

    void tick(float dt, int screenW, int screenH, int bodyW, int bodyH) {
        this.screenW = screenW;
        this.bodyW = bodyW;
        float step = frameStep();
        if (step > 0f) {
            frameTime += dt;
            if (frameTime >= step) {
                frameTime -= step;
                frame++;
            }
        }
        actTime += dt;
        float squat = act == Act.SIT ? 0.9f : 1f;
        squash += (squat - squash) * Math.min(1f, dt * 8f);
        bob = 0f;
        lean = 0f;

        float margin = 10f * density;
        float floor = screenH - bodyH - 24f * density;
        float top = 8f * density;
        float left = margin;
        float right = screenW - bodyW - margin;
        if (right < left) right = left;

        switch (act) {
            case DRAG:
                lean = (float) Math.sin(actTime * 16.0) * 12f;
                bob = (float) Math.sin(actTime * 11.0) * 3f;
                break;
            case WALK:
                roam(dt, 120f, left, right, floor);
                break;
            case RUN:
                roam(dt, 230f, left, right, floor);
                break;
            case STAND:
                pose(floor, 3f);
                break;
            case SIT:
                pose(floor, 1f);
                break;
            case LOOK:
                faceRight = ((int) (actTime / 0.9f)) % 2 == 0;
                pose(floor, 2f);
                break;
            case SLEEP:
                pose(floor, 0f);
                break;
            case CLIMB:
                climb(dt, left, right, top, floor);
                break;
            case CEILING:
                ceiling(dt, left, right, top);
                break;
            case SWIM:
                swim(dt, left, right, top, floor);
                break;
            case DANCE:
                dance(floor);
                break;
            case PLAY:
                play(dt, left, right, floor);
                break;
            case WEB:
                web(dt, floor, bodyW, bodyH);
                break;
            case FALL:
            default:
                fall(dt, left, right, top, floor);
                break;
        }
    }

    private float frameStep() {
        if (act == Act.RUN) return 0.055f;
        if (act == Act.WALK || act == Act.CEILING || act == Act.DRAG) return 0.08f;
        if (act == Act.CLIMB) return 0.11f;
        if (act == Act.DANCE || act == Act.PLAY || act == Act.SWIM) return 0.12f;
        if (act == Act.WEB) return 0.14f;
        return 0f;
    }

    private void chooseNext() {
        int roll = random.nextInt(100);
        if (roll < 34) startWalk();
        else if (roll < 46) startRun();
        else if (roll < 58) startStand();
        else if (roll < 66) startSit();
        else if (roll < 74) startLook();
        else if (roll < 81) startSleep();
        else if (roll < 88) startDance();
        else if (roll < 93) startPlay();
        else if (roll < 97) startSwim();
        else startWeb();
    }

    private void roam(float dt, float dpPerSec, float left, float right, float floor) {
        ceiling = false;
        webOn = false;
        y = floor;
        float pace = act == Act.RUN ? 18f : 14f;
        lean = (float) Math.sin(actTime * pace) * (act == Act.RUN ? 6f : 3.5f);
        bob = (float) Math.abs(Math.sin(actTime * pace)) * -4f;
        x += (faceRight ? 1f : -1f) * speed(dpPerSec) * dt;
        if (x <= left) {
            x = left;
            startClimb(Side.LEFT, true);
            return;
        }
        if (x >= right) {
            x = right;
            startClimb(Side.RIGHT, true);
            return;
        }
        if (actTime > 2.4f && random.nextFloat() < dt * 0.07f) faceRight = !faceRight;
        if (actTime > limit) chooseNext();
    }

    private void pose(float floor, float breath) {
        ceiling = false;
        webOn = false;
        y = floor;
        bob = breath == 0f ? 0f : (float) Math.sin(actTime * 2.0) * breath;
        if (actTime > limit) chooseNext();
    }

    private void startWalk() {
        begin(Act.WALK, 3.2f + random.nextFloat() * 4.8f);
    }

    private void startRun() {
        begin(Act.RUN, 1.5f + random.nextFloat() * 2.0f);
    }

    private void startStand() {
        begin(Act.STAND, 1.2f + random.nextFloat() * 2.4f);
    }

    private void startSit() {
        begin(Act.SIT, 2.4f + random.nextFloat() * 4.0f);
    }

    private void startLook() {
        begin(Act.LOOK, 1.6f + random.nextFloat() * 2.2f);
    }

    private void startSleep() {
        begin(Act.SLEEP, 5f + random.nextFloat() * 7f);
    }

    private void begin(Act next, float seconds) {
        act = next;
        actTime = 0f;
        frame = 0;
        ceiling = false;
        webOn = false;
        vx = 0f;
        vy = 0f;
        limit = seconds;
    }

    private void startClimb(Side side, boolean up) {
        act = Act.CLIMB;
        wall = side;
        climbUp = up;
        faceRight = side == Side.LEFT;
        actTime = 0f;
        frame = 0;
        ceiling = false;
        webOn = false;
    }

    private void climb(float dt, float left, float right, float top, float floor) {
        x = wall == Side.LEFT ? left : right;
        y += (climbUp ? -1f : 1f) * speed(95) * dt;
        lean = wall == Side.LEFT ? -8f : 8f;
        if (climbUp && y <= top) {
            y = top;
            act = Act.CEILING;
            ceiling = true;
            faceRight = wall == Side.LEFT;
            actTime = 0f;
            frame = 0;
            return;
        }
        if (!climbUp && y >= floor) {
            y = floor;
            faceRight = wall == Side.LEFT;
            squash = 0.86f;
            chooseNext();
        }
    }

    private void ceiling(float dt, float left, float right, float top) {
        y = top;
        ceiling = true;
        x += (faceRight ? 1f : -1f) * speed(100) * dt;
        lean = (float) Math.sin(actTime * 12.0) * 4f;
        if (faceRight && x >= right) {
            x = right;
            startClimb(Side.RIGHT, false);
        } else if (!faceRight && x <= left) {
            x = left;
            startClimb(Side.LEFT, false);
        }
    }

    private void startSwim() {
        begin(Act.SWIM, 4.8f);
        faceRight = x < screenW * 0.5f;
        vx = (faceRight ? 1f : -1f) * speed(170);
        vy = speed(30) * (random.nextBoolean() ? 1f : -1f);
    }

    private void swim(float dt, float left, float right, float top, float floor) {
        x += vx * dt;
        y += vy * dt;
        bob = (float) Math.sin(actTime * 7.0) * 10f;
        lean = (float) Math.sin(actTime * 7.0) * 6f;
        if (actTime > limit || x < left - 4 || x > right + 4 || y < top || y > floor) {
            act = Act.FALL;
            actTime = 0f;
            vy = 40f;
            webOn = false;
        }
    }

    private void startDance() {
        begin(Act.DANCE, 3.4f);
    }

    private void dance(float floor) {
        y = floor;
        bob = (float) Math.sin(actTime * 11.0) * 16f;
        lean = (float) Math.sin(actTime * 8.0) * 8f;
        faceRight = ((int) (actTime * 2f)) % 2 == 0;
        if (actTime > limit) chooseNext();
    }

    private void startPlay() {
        begin(Act.PLAY, 4f);
        hops = 3;
        vy = -speed(620);
        vx = (faceRight ? 1f : -1f) * speed(80);
    }

    private void play(float dt, float left, float right, float floor) {
        vy += speed(1700) * dt;
        x += vx * dt;
        y += vy * dt;
        if (x < left || x > right) vx = -vx;
        if (y >= floor) {
            y = floor;
            squash = 0.78f;
            hops--;
            if (hops <= 0) {
                chooseNext();
                return;
            }
            vy = -speed(560);
        }
    }

    private void startWeb() {
        begin(Act.WEB, 4.2f);
        webOn = true;
        webGrow = 0f;
        float reach = 160f * density + random.nextFloat() * 140f * density;
        webAx = Math.max(12f, Math.min(screenW - 12f, x + bodyW * 0.5f + (faceRight ? reach : -reach)));
        webAy = Math.max(8f, y - (120f + random.nextFloat() * 180f) * density);
        swingAngle = faceRight ? 0.55f : -0.55f;
        swingVel = faceRight ? -1.4f : 1.4f;
        webLen = 0f;
    }

    private void web(float dt, float floor, int bodyW, int bodyH) {
        if (webGrow < 1f) {
            y = floor;
            webGrow = Math.min(1f, webGrow + dt / 0.38f);
            frame = Math.min(3, (int) (webGrow * 4f));
            if (webGrow >= 1f) {
                float hx = x + bodyW * 0.5f;
                float hy = y + bodyH * 0.28f;
                webLen = (float) Math.hypot(hx - webAx, hy - webAy);
                if (webLen < 80f) webLen = 180f * density;
            }
            return;
        }
        swingVel += (float) (-Math.sin(swingAngle) * 7.5 * dt);
        swingVel *= (float) Math.pow(0.35, dt);
        swingAngle += swingVel * dt;
        float handX = webAx + (float) Math.sin(swingAngle) * webLen;
        float handY = webAy + (float) Math.cos(swingAngle) * webLen;
        x = handX - bodyW * 0.5f;
        y = handY - bodyH * 0.22f;
        faceRight = swingVel >= 0f;
        lean = (float) Math.toDegrees(swingAngle) * 0.25f;
        if (actTime > limit) {
            webOn = false;
            act = Act.FALL;
            actTime = 0f;
            vx = (float) Math.cos(swingAngle) * swingVel * webLen * 0.35f;
            vy = speed(80);
        }
    }

    private void fall(float dt, float left, float right, float top, float floor) {
        ceiling = false;
        vy += speed(1900) * dt;
        vx *= (float) Math.pow(0.15, dt);
        x += vx * dt;
        y += vy * dt;
        lean = Math.max(-18f, Math.min(18f, vx / density * 0.04f));
        if (x <= left) {
            x = left;
            vx = Math.abs(vx) * 0.45f;
        } else if (x >= right) {
            x = right;
            vx = -Math.abs(vx) * 0.45f;
        }
        if (y < top && vy < 0f) vy = 0f;
        if (y >= floor) {
            y = floor;
            squash = 0.8f;
            chooseNext();
        }
    }

    private float speed(float dpPerSec) {
        return dpPerSec * density;
    }
}
