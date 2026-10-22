package ke.bkm.sidekick;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/** A full-screen layer that only draws the web. Touches pass through it. */
public class WebLayer extends View {
    private final Paint strand = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private MascotMotion motion;
    float handX;
    float handY;

    public WebLayer(Context context) {
        super(context);
        strand.setStyle(Paint.Style.STROKE);
        strand.setStrokeCap(Paint.Cap.ROUND);
        strand.setColor(0xF7FFFFFF);
    }

    void bind(MascotMotion motion) {
        this.motion = motion;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (motion == null || !motion.webOn || motion.webGrow <= 0f) return;
        float x1 = motion.webAx;
        float y1 = motion.webAy;
        float x2 = x1 + (handX - x1) * motion.webGrow;
        float y2 = y1 + (handY - y1) * motion.webGrow;
        float density = getResources().getDisplayMetrics().density;
        strand.setStrokeWidth(2.2f * density);
        drawStrand(canvas, x1, y1, x2, y2, 0f);
        strand.setStrokeWidth(1.3f * density);
        strand.setAlpha(210);
        drawStrand(canvas, x1, y1, x2, y2, 10f * density);
        drawStrand(canvas, x1, y1, x2, y2, -9f * density);
        strand.setAlpha(255);
    }

    private void drawStrand(Canvas canvas, float x1, float y1, float x2, float y2, float bend) {
        float mx = (x1 + x2) / 2f + bend;
        float my = (y1 + y2) / 2f + bend * 0.35f;
        path.rewind();
        path.moveTo(x1, y1);
        path.quadTo(mx, my, x2, y2);
        canvas.drawPath(path, strand);
    }
}
