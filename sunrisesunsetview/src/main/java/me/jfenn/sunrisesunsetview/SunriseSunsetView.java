package me.jfenn.sunrisesunsetview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;

import androidx.annotation.Nullable;
import me.jfenn.androidutils.anim.AnimatedFloat;

public class SunriseSunsetView extends View implements View.OnTouchListener {

    private static final long DAY_LENGTH = 86400000L;

    private Paint paint;
    private Paint sunsetPaint;
    private Paint linePaint;

    private AnimatedFloat dayStart;
    private AnimatedFloat dayEnd;

    private Float moveBeginStart;
    private Float moveBeginEnd;

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

        dayStart = new AnimatedFloat(0.25f);
        dayEnd = new AnimatedFloat(0.75f);
    }

    public void setDayStart(long dayStartMillis) {
        setDayStart(dayStartMillis, false);
    }

    public void setDayStart(long dayStartMillis, boolean animate) {
        dayStartMillis %= DAY_LENGTH;
        if (animate)
            dayStart.to((float) dayStartMillis / DAY_LENGTH);
        else dayStart.setCurrent((float) dayStartMillis / DAY_LENGTH);
    }

    public void setDayEnd(long dayEndMillis) {
        setDayEnd(dayEndMillis, false);
    }

    public void setDayEnd(long dayEndMillis, boolean animate) {
        dayEndMillis %= DAY_LENGTH;
        if (animate)
            dayEnd.to((float) dayEndMillis / DAY_LENGTH);
        else dayEnd.setCurrent((float) dayEndMillis / DAY_LENGTH);
    }

    public void setListener(SunriseListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        dayStart.next(true);
        dayEnd.next(true);

        float scaleX = getWidth() / 23f;
        float scaleY = getHeight() / 2f;
        float interval = (dayEnd.val() - dayStart.val()) / 2;
        float interval2 = (1 - dayEnd.val() + dayStart.val()) / 2;
        float start = dayStart.val() - (1 - dayEnd.val() + dayStart.val());
        interval *= 24 * scaleX;
        interval2 *= 24 * scaleX;
        start *= 24 * scaleX;

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

        if (!dayStart.isTarget() || !dayEnd.isTarget())
            postInvalidate();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float horizontalDistance = event.getX() / getWidth();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                moveBeginStart = null;
                moveBeginEnd = null;
                if (!dayStart.isTarget() || !dayEnd.isTarget())
                    break;

                if (Math.abs(horizontalDistance - dayStart.val()) < Math.abs(horizontalDistance - dayEnd.val()))
                    moveBeginStart = dayStart.val() - horizontalDistance;
                else moveBeginEnd = dayEnd.val() - horizontalDistance;

                break;
            case MotionEvent.ACTION_MOVE:
                if (moveBeginStart != null && horizontalDistance < dayEnd.getTarget()) {
                    dayStart.to(Math.min(1, Math.max(0, moveBeginStart + horizontalDistance)));
                } else if (moveBeginEnd != null && horizontalDistance > dayStart.getTarget()) {
                    dayEnd.to(Math.min(1, Math.max(0, moveBeginEnd + horizontalDistance)));
                }

                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (listener != null) {
                    if (moveBeginStart != null)
                        listener.onSunriseChanged((long) (dayStart.getTarget() * DAY_LENGTH));
                    else if (moveBeginEnd != null)
                        listener.onSunsetChanged((long) (dayEnd.getTarget() * DAY_LENGTH));
                }

                moveBeginStart = null;
                moveBeginEnd = null;
                break;
        }
        return false;
    }

    public interface SunriseListener {
        void onSunriseChanged(long sunriseMillis);
        void onSunsetChanged(long sunsetMillis);
    }
}
