package ke.bkm.sidekick;

import java.util.Random;

/**
 * Screen-pet motion. The character walks the floor, climbs the walls,
 * crosses the ceiling, swims the open screen, dances, hops, and swings
 * from a web. Position is the top-left of his window.
 */
final class MascotMotion {
    enum Act { WALK, CLIMB, CEILING, SWIM, DANCE, PLAY, WEB, FALL, DRAG, ASSIST }
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
    boolean pinned;

    private final Random random = new Random();
    private float density = 3f;
    private float frameTime;
    private float actTime;
    private float swingAngle;
    private float swingVel;
    private float webLen;
    private int hops = 3;
    private float dragDx;
    private float dragDy;
    private Act beforeDrag = Act.WALK;

    void setDensity(float density) {
        this.density = density;
    }

    void resize(float factor) {
        scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale * factor));
    }

    void beginDrag(float fingerX, float fingerY) {
        beforeDrag = act == Act.DRAG ? beforeDrag : act;
        act = Act.DRAG;
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
            act = beforeDrag == Act.DRAG ? Act.WALK : beforeDrag;
            return;
        }
        act = Act.FALL;
        vy = 80f;
        actTime = 0f;
        webOn = false;
        ceiling = false;
    }

    void pin(boolean on) {
        pinned = on;
        if (on) {
            webOn = false;
            ceiling = false;
            if (act == Act.SWIM || act == Act.WEB || act == Act.PLAY || act == Act.CLIMB || act == Act.CEILING) {
                act = Act.FALL;
            }
        }
    }

    void command(String name, int screenW, int bodyW) {
        pinned = false;
        if ("walk".equals(name)) startWalk();
        else if ("climb".equals(name)) startClimb(faceRight ? Side.RIGHT : Side.LEFT, true);
        else if ("swim".equals(name)) startSwim(screenW);
        else if ("dance".equals(name)) startDance();
        else if ("play".equals(name)) startPlay();
        else if ("web".equals(name)) startWeb(screenW, bodyW);
        else if ("bigger".equals(name)) resize(1.18f);
        else if ("smaller".equals(name)) resize(0.85f);
    }

    String clip() {
        if (act == Act.DRAG) return "climb";
        if (act == Act.FALL) return "fall";
        if (act == Act.SWIM) return "swim";
        if (act == Act.DANCE || act == Act.PLAY || act == Act.ASSIST) return "dance";
        if (act == Act.WEB) return webGrow < 1f ? "web" : "swing";
        if (act == Act.CLIMB) return "climb";
        return "walk";
    }

    void tick(float dt, int screenW, int screenH, int bodyW, int bodyH) {
        float step = act == Act.WALK || act == Act.CEILING ? 0.11f : 0.15f;
        frameTime += dt;
        if (frameTime >= step) {
            frameTime -= step;
            frame = (frame + 1) % 4;
        }
        actTime += dt;
        squash += (1f - squash) * Math.min(1f, dt * 10f);
        bob = 0f;
        lean = 0f;

        float margin = 10f * density;
        float floor = screenH - bodyH - 24f * density;
        float top = 8f * density;
        float left = margin;
        float right = screenW - bodyW - margin;
        if (right < left) right = left;

        if (pinned && act != Act.DRAG) {
            assist(dt, left, floor);
            return;
        }

        switch (act) {
            case DRAG:
                break;
            case WALK:
                walk(dt, left, right, floor, screenW, bodyW);
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
                dance(dt, floor);
                break;
            case PLAY:
                play(dt, left, right, floor);
                break;
            case WEB:
                web(dt, floor, bodyW, bodyH);
                break;
            case FALL:
            case ASSIST:
            default:
                fall(dt, left, right, top, floor);
                break;
        }
    }

    private void assist(float dt, float left, float floor) {
        act = Act.ASSIST;
        ceiling = false;
        webOn = false;
        float home = left + 8f * density;
        if (Math.abs(x - home) > 6f) {
            faceRight = home > x;
            x += (faceRight ? 1f : -1f) * speed(140) * dt;
        }
        y = floor;
        bob = (float) Math.sin(actTime * 8.0) * 8f * density * 0.25f;
        lean = (float) Math.sin(actTime * 6.0) * 4f;
    }

    private void walk(float dt, float left, float right, float floor, int screenW, int bodyW) {
        ceiling = false;
        webOn = false;
        y = floor;
        lean = (float) Math.sin(actTime * 14.0) * 3.5f;
        bob = (float) Math.abs(Math.sin(actTime * 14.0)) * -3f;
        x += (faceRight ? 1f : -1f) * speed(125) * dt;
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
        if (actTime > 1.6f && random.nextFloat() < dt * 0.12f) {
            faceRight = !faceRight;
        }
        if (actTime > 2.4f && random.nextFloat() < dt * 0.28f) {
            int roll = random.nextInt(4);
            if (roll == 0) startDance();
            else if (roll == 1) startPlay();
            else if (roll == 2) startSwim(screenW);
            else startWeb(screenW, bodyW);
        }
    }

    private void startWalk() {
        act = Act.WALK;
        actTime = 0f;
        frame = 0;
        ceiling = false;
        webOn = false;
        vx = 0f;
        vy = 0f;
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
            startWalk();
        }
    }

    private void ceiling(float dt, float left, float right, float top) {
        y = top;
        ceiling = true;
        x += (faceRight ? 1f : -1f) * speed(100) * dt;
        if (faceRight && x >= right) {
            x = right;
            startClimb(Side.RIGHT, false);
        } else if (!faceRight && x <= left) {
            x = left;
            startClimb(Side.LEFT, false);
        }
    }

    private void startSwim(int screenW) {
        act = Act.SWIM;
        actTime = 0f;
        frame = 0;
        ceiling = false;
        webOn = false;
        faceRight = x < screenW * 0.5f;
        vx = (faceRight ? 1f : -1f) * speed(170);
        vy = speed(30) * (random.nextBoolean() ? 1f : -1f);
    }

    private void swim(float dt, float left, float right, float top, float floor) {
        x += vx * dt;
        y += vy * dt;
        bob = (float) Math.sin(actTime * 7.0) * 10f;
        lean = (float) Math.sin(actTime * 7.0) * 6f;
        if (actTime > 4.8f || x < left - 4 || x > right + 4 || y < top || y > floor) {
            act = Act.FALL;
            actTime = 0f;
            vy = 40f;
            webOn = false;
        }
    }

    private void startDance() {
        act = Act.DANCE;
        actTime = 0f;
        frame = 0;
        ceiling = false;
        webOn = false;
    }

    private void dance(float dt, float floor) {
        y = floor;
        bob = (float) Math.sin(actTime * 11.0) * 16f;
        lean = (float) Math.sin(actTime * 8.0) * 8f;
        if (((int) (actTime * 2f)) % 2 == 0) faceRight = true;
        else faceRight = actTime % 0.5f > 0.25f;
        if (actTime > 3.4f) startWalk();
    }

    private void startPlay() {
        act = Act.PLAY;
        actTime = 0f;
        hops = 3;
        vy = -speed(620);
        vx = (faceRight ? 1f : -1f) * speed(80);
        ceiling = false;
        webOn = false;
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
                startWalk();
                return;
            }
            vy = -speed(560);
        }
    }

    private void startWeb(int screenW, int bodyW) {
        act = Act.WEB;
        actTime = 0f;
        frame = 0;
        webOn = true;
        webGrow = 0f;
        ceiling = false;
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
        if (actTime > 3.6f) {
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
            startWalk();
        }
    }

    private float speed(float dpPerSec) {
        return dpPerSec * density;
    }
}
