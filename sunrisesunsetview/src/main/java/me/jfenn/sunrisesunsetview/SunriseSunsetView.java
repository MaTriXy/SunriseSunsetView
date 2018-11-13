package me.jfenn.sunrisesunsetview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.Calendar;

import androidx.annotation.Nullable;

public class SunriseSunsetView extends View implements View.OnTouchListener {

    private Paint paint;
    private Paint sunsetPaint;
    private Paint linePaint;

    private float dayStart;
    private float dayEnd;
    private float displayDayStart;
    private float displayDayEnd;

    private boolean movingStart;
    private boolean movingEnd;
    private ValueAnimator animator1;
    private ValueAnimator animator2;

    private SunriseListener listener;

    public SunriseSunsetView(Context context) {
        this(context, null, 0);
    }

    public SunriseSunsetView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SunriseSunsetView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);

        sunsetPaint = new Paint();
        sunsetPaint.setAntiAlias(true);
        sunsetPaint.setStyle(Paint.Style.FILL);
        sunsetPaint.setColor(Color.BLACK);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(Color.BLACK);
        linePaint.setAlpha(20);

        setOnTouchListener(this);
        setClickable(true);
        setFocusable(true);
    }

    public void setListener(SunriseListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //float dayStart = (float) alarmio.getDayStart();
        //float dayEnd = (float) alarmio.getDayEnd();
        if (dayStart != this.dayStart) {
            if (animator1 != null && animator1.isStarted())
                animator1.end();

            animator1 = ValueAnimator.ofFloat(this.dayStart, dayStart);
            animator1.setInterpolator(new DecelerateInterpolator());
            animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    displayDayStart = (float) valueAnimator.getAnimatedValue();
                    invalidate();
                }
            });
            animator1.start();
            this.dayStart = dayStart;
        }
        if (dayEnd != this.dayEnd) {
            if (animator2 != null && animator2.isStarted())
                animator2.end();

            animator2 = ValueAnimator.ofFloat(this.dayEnd, dayEnd);
            animator2.setInterpolator(new DecelerateInterpolator());
            animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    displayDayEnd = (float) valueAnimator.getAnimatedValue();
                    invalidate();
                }
            });
            animator2.start();
            this.dayEnd = dayEnd;
        }

        float scaleX = canvas.getWidth() / 23f;
        float scaleY = canvas.getHeight() / 2f;
        float interval = (displayDayEnd - displayDayStart) / 2;
        float interval2 = (24 - displayDayEnd + displayDayStart) / 2;
        float start = displayDayStart - (24 - displayDayEnd + displayDayStart);
        interval *= scaleX;
        interval2 *= scaleX;
        start *= scaleX;

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        Path path = new Path();
        path.moveTo(start, scaleY);
        path.rQuadTo(interval2, scaleY * ((interval2 / interval + 1) / 2), interval2 * 2, 0);
        path.rQuadTo(interval, -scaleY * ((interval / interval2 + 1) / 2), interval * 2, 0);
        path.rQuadTo(interval2, scaleY * ((interval2 / interval + 1) / 2), interval2 * 2, 0);
        path.rQuadTo(interval, -scaleY * ((interval / interval2 + 1) / 2), interval * 2, 0);

        canvas.clipPath(path);
        canvas.drawRect(0, 0, (int) scaleX * hour, (int) scaleY, paint);
        canvas.drawRect(0, (int) scaleY, (int) scaleX * hour, canvas.getHeight(), sunsetPaint);
        canvas.drawRect((int) scaleX * hour, 0, canvas.getWidth(), canvas.getHeight(), linePaint);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float horizontalDistance = event.getX() / getWidth();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (Math.abs(horizontalDistance - (dayStart / 24f)) < Math.abs(horizontalDistance - (dayEnd / 24f))) {
                    movingStart = true;
                    movingEnd = false;
                } else {
                    movingStart = false;
                    movingEnd = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (movingStart) {
                    movingStart = false;
                    float dayStart = Math.min((float) Math.round(horizontalDistance * 24), dayEnd - 1);
                    invalidate();
                    if (listener != null)
                        listener.onSunriseChanged(Math.round(dayStart), Math.round(dayEnd));
                } else if (movingEnd) {
                    movingEnd = false;
                    float dayEnd = Math.max((float) Math.round(horizontalDistance * 24), dayStart + 1);
                    invalidate();
                    if (listener != null)
                        listener.onSunriseChanged(Math.round(dayStart), Math.round(dayEnd));
                }
                break;
        }
        return false;
    }

    public interface SunriseListener {
        void onSunriseChanged(int sunrise, int sunset);
    }
}
