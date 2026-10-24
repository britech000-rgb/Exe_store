package ke.bkm.sidekick;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/** Draws one animation frame, feet on the floor of the view, with a shadow. */
public class MascotView extends View {
    private final Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubble = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubbleText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF dest = new RectF();
    private final RectF bubbleRect = new RectF();
    private Bitmap frame;
    private boolean feetDown = true;
    private boolean faceRight = true;
    private boolean upsideDown;
    private float lean;
    private float bob;
    private float squash = 1f;
    private String line;

    public MascotView(Context context) {
        this(context, null);
    }

    public MascotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        shadow.setColor(0x55000000);
        bubble.setColor(0xFFE2B657);
        bubbleText.setColor(0xFF12100C);
        bubbleText.setTextSize(13f * getResources().getDisplayMetrics().scaledDensity);
    }

    void setLine(String line) {
        this.line = line;
        invalidate();
    }

    void show(Bitmap bitmap, boolean feetDown, boolean faceRight, boolean upsideDown, float lean, float bob, float squash) {
        this.frame = bitmap;
        this.feetDown = feetDown;
        this.faceRight = faceRight;
        this.upsideDown = upsideDown;
        this.lean = lean;
        this.bob = bob;
        this.squash = squash;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();
        float shadowW = w * 0.42f;
        canvas.drawOval(w * 0.5f - shadowW, h - h * 0.06f, w * 0.5f + shadowW, h - 2f, shadow);
        if (frame == null) return;
        float bw = frame.getWidth();
        float bh = frame.getHeight();
        float boxW = w * 0.94f;
        float boxH = h * (feetDown ? 0.9f : 0.78f);
        float fit = Math.min(boxW / bw, boxH / bh);
        float dw = bw * fit;
        float dh = bh * fit * squash;
        float left = (w - dw) / 2f;
        float top = feetDown ? h - dh - h * 0.04f + bob : (h - dh) / 2f + bob;
        canvas.save();
        canvas.scale(faceRight ? 1f : -1f, upsideDown ? -1f : 1f, w / 2f, h / 2f);
        canvas.rotate(lean, w / 2f, h * 0.72f);
        dest.set(left, top, left + dw, top + dh);
        canvas.drawBitmap(frame, null, dest, null);
        canvas.restore();
        if (line == null || line.isEmpty()) return;
        float textW = Math.min(w - 16f, bubbleText.measureText(line) + 28f);
        bubbleRect.set((w - textW) / 2f, 4f, (w + textW) / 2f, 4f + bubbleText.getTextSize() + 18f);
        canvas.drawRoundRect(bubbleRect, 18f, 18f, bubble);
        canvas.drawText(line, bubbleRect.left + 14f, bubbleRect.bottom - 12f, bubbleText);
    }
}
