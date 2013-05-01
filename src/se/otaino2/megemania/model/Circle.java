package se.otaino2.megemania.model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.graphics.Paint;

public class Circle {
    
    private static final float MAX_SPEED = 500.0f;
    private static final long MAX_TIME_BETWEEN_SPEED_CHANGES_IN_MILLIS = 10000;
    
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> activeTask;
    private float cx;
    private float cy;
    private float radius;
    private Paint paint;
    private volatile float vx;
    private volatile float vy;
    
    public Circle(float cx, float cy, float radius, Paint paint) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
        this.paint = paint;
        periodicallyChangeSpeed();
    }
    
    public float getCx() {
        return cx;
    }

    public float getCy() {
        return cy;
    }

    public float getRadius() {
        return radius;
    }

    public Paint getPaint() {
        return paint;
    }
    
    public int getType() {
        return paint.getColor();
    }
    
    public void destroy() {
        if (activeTask != null) {
            activeTask.cancel(true);
        }
        executor.shutdownNow();
        executor = null;
    }
    
    private void periodicallyChangeSpeed() {
        double seed = 2 * Math.PI * Math.random();
        vx = (float) (MAX_SPEED * Math.cos(seed));
        vy = (float) (MAX_SPEED * Math.sin(seed));
        long randomDelay = (long) seed * MAX_TIME_BETWEEN_SPEED_CHANGES_IN_MILLIS;
        activeTask = executor.schedule(new Runnable() {
            @Override
            public void run() {
                periodicallyChangeSpeed();
            }
        }, randomDelay, TimeUnit.MILLISECONDS);
    }
}
