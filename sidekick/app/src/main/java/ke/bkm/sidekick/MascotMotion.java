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
    private float flickX;
    private float flickY;
    private float lastFingerX;
    private float lastFingerY;
    private long lastFingerAt;
    private float airLimit = 3f;
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
        lastFingerX = fingerX;
        lastFingerY = fingerY;
        lastFingerAt = System.nanoTime();
        flickX = 0f;
        flickY = 0f;
        vx = 0f;
        vy = 0f;
    }

    void dragTo(float fingerX, float fingerY) {
        long now = System.nanoTime();
        float dt = (now - lastFingerAt) / 1_000_000_000f;
        if (dt > 0.008f && dt < 0.25f) {
            flickX = (fingerX - lastFingerX) / dt;
            flickY = (fingerY - lastFingerY) / dt;
        }
        lastFingerX = fingerX;
        lastFingerY = fingerY;
        lastFingerAt = now;
        x = fingerX - dragDx;
        y = fingerY - dragDy;
    }

    void endGesture(boolean tap) {
        if (tap) {
            act = beforeDrag == Act.DRAG || beforeDrag == Act.FALL ? Act.WALK : beforeDrag;
            return;
        }
        act = Act.FALL;
        actTime = 0f;
        webOn = false;
        ceiling = false;
        float cap = speed(1400);
        vx = Math.max(-cap, Math.min(cap, flickX));
        vy = Math.max(-cap, Math.min(cap, flickY));
        if (Math.hypot(vx, vy) < speed(120)) {
            vx = 0f;
            vy = speed(60);
        }
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
        if (act == Act.RUN) return 0.06f;
        // Walk art is an 8-frame step. Hold each pose long enough to see the foot leave the ground.
        if (act == Act.WALK || act == Act.CEILING || act == Act.DRAG) return 0.12f;
        if (act == Act.CLIMB) return 0.11f;
        if (act == Act.DANCE || act == Act.PLAY || act == Act.SWIM) return 0.12f;
        if (act == Act.WEB) return 0.14f;
        return 0f;
    }

    /** Next action depends on the one that just finished, the way Shimeji chains behaviors. */
    private void chooseNext() {
        Act done = act;
        if (done == Act.SIT) {
            pick(Act.LOOK, 40, Act.SLEEP, 35, Act.STAND, 15, Act.WALK, 10);
        } else if (done == Act.SLEEP) {
            pick(Act.STAND, 45, Act.SIT, 25, Act.LOOK, 20, Act.WALK, 10);
        } else if (done == Act.LOOK) {
            pick(Act.WALK, 40, Act.STAND, 25, Act.SIT, 20, Act.RUN, 15);
        } else if (done == Act.CLIMB || done == Act.CEILING) {
            pick(Act.WALK, 45, Act.RUN, 20, Act.STAND, 20, Act.SIT, 15);
        } else {
            pick(Act.WALK, 26, Act.RUN, 10, Act.STAND, 14, Act.SIT, 16,
                    Act.LOOK, 8, Act.SLEEP, 6, Act.DANCE, 8, Act.PLAY, 5, Act.SWIM, 4, Act.WEB, 3);
        }
    }

    private void pick(Act a, int wa, Act b, int wb, Act c, int wc, Act d, int wd) {
        pick(a, wa, b, wb, c, wc, d, wd, null, 0, null, 0, null, 0, null, 0, null, 0, null, 0);
    }

    private void pick(Act a, int wa, Act b, int wb, Act c, int wc, Act d, int wd,
                      Act e, int we, Act f, int wf, Act g, int wg, Act h, int wh, Act i, int wi, Act j, int wj) {
        int total = wa + wb + wc + wd + we + wf + wg + wh + wi + wj;
        int roll = random.nextInt(Math.max(1, total));
        Act chosen = a;
        int cursor = wa;
        if (roll >= cursor) { chosen = b; cursor += wb; }
        if (roll >= cursor) { chosen = c; cursor += wc; }
        if (roll >= cursor) { chosen = d; cursor += wd; }
        if (roll >= cursor && e != null) { chosen = e; cursor += we; }
        if (roll >= cursor && f != null) { chosen = f; cursor += wf; }
        if (roll >= cursor && g != null) { chosen = g; cursor += wg; }
        if (roll >= cursor && h != null) { chosen = h; cursor += wh; }
        if (roll >= cursor && i != null) { chosen = i; cursor += wi; }
        if (roll >= cursor && j != null) chosen = j;
        startChosen(chosen);
    }

    private void startChosen(Act chosen) {
        if (chosen == Act.RUN) startRun();
        else if (chosen == Act.STAND) startStand();
        else if (chosen == Act.SIT) startSit();
        else if (chosen == Act.LOOK) startLook();
        else if (chosen == Act.SLEEP) startSleep();
        else if (chosen == Act.DANCE) startDance();
        else if (chosen == Act.PLAY) startPlay();
        else if (chosen == Act.SWIM) startSwim();
        else if (chosen == Act.WEB) startWeb();
        else startWalk();
    }

    private void roam(float dt, float dpPerSec, float left, float right, float floor) {
        ceiling = false;
        webOn = false;
        y = floor;
        float pace = act == Act.RUN ? 18f : 14f;
        lean = (float) Math.sin(actTime * pace) * (act == Act.RUN ? 6f : 3.5f);
        // Frames 2-3 and 6-7 are the airborne foot. Rise with that step, in dp so it shows on a phone.
        int phase = Math.floorMod(frame, 8);
        float hopDp = (phase == 2 || phase == 3 || phase == 6 || phase == 7) ? 6f
                : (phase == 1 || phase == 5) ? 3f : 0f;
        bob = -hopDp * density;
        x += (faceRight ? 1f : -1f) * speed(dpPerSec) * dt;
        if (x <= left) {
            x = left;
            meetEdge(Side.LEFT);
            return;
        }
        if (x >= right) {
            x = right;
            meetEdge(Side.RIGHT);
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

    /** Most arrivals at a wall turn him around. Sometimes he grabs it, like Shimeji. */
    private void meetEdge(Side side) {
        if (random.nextFloat() < 0.30f) {
            startClimb(side, true);
            return;
        }
        faceRight = side == Side.LEFT;
        actTime = 0f;
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
        lean = wall == Side.LEFT ? -8f : 8f;
        if (actTime < 0.28f) return;
        if (actTime > 0.7f && y < floor - 90f * density && random.nextFloat() < dt * 0.12f) {
            act = Act.FALL;
            vx = wall == Side.LEFT ? speed(180) : -speed(180);
            vy = speed(20);
            actTime = 0f;
            ceiling = false;
            return;
        }
        y += (climbUp ? -1f : 1f) * speed(95) * dt;
        if (climbUp && y <= top) {
            y = top;
            act = Act.CEILING;
            ceiling = true;
            faceRight = wall == Side.LEFT;
            actTime = 0f;
            frame = 0;
            airLimit = 1.5f + random.nextFloat() * 2.6f;
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
        if (actTime > airLimit) {
            if (random.nextFloat() < 0.45f) {
                act = Act.FALL;
                vy = speed(30);
                vx = (faceRight ? 1f : -1f) * speed(80);
                ceiling = false;
                actTime = 0f;
                return;
            }
            airLimit = actTime + 1.4f + random.nextFloat() * 2f;
        }
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
