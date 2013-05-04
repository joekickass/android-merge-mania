package se.otaino2.megemania.model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.graphics.Paint;

public class Circle {
    
    private static final float MAX_SPEED = 50.0f;
    private static final long MAX_TIME_BETWEEN_SPEED_CHANGES_IN_SECONDS = 30;
    
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> activeTask;
    private float cx;
    private float cy;
    private float radius;
    private Paint paint;
    private float vx;
    private float vy;
    
    public Circle(float cx, float cy, float radius, Paint paint) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
        this.paint = paint;
    }
    
    public float getCx() {
        return cx;
    }
    
    public void setCx(float x) {
        cx = x;
    }

    public float getCy() {
        return cy;
    }
    
    public void setCy(float y) {
        cy = y;
    }
    
    public synchronized float getVx() {
        return vx;
    }

    public synchronized void setVx(float vx) {
        this.vx = vx;
    }

    public synchronized float getVy() {
        return vy;
    }

    public synchronized void setVy(float vy) {
        this.vy = vy;
    }

    public float getRadius() {
        return radius;
    }

    public Paint getPaint() {
        return paint;
    }
    
    public void startMoving() {
        periodicallyChangeSpeed();
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
        float newVx = (float) (MAX_SPEED * Math.cos(seed));
        float newVy = (float) (MAX_SPEED * Math.sin(seed));
        setVx(newVx);
        setVy(newVy);
        long randomDelay = (long) (Math.random() * MAX_TIME_BETWEEN_SPEED_CHANGES_IN_SECONDS);
        activeTask = executor.schedule(new Runnable() {
            @Override
            public void run() {
                periodicallyChangeSpeed();
            }
        }, randomDelay, TimeUnit.SECONDS);
    }

    public boolean isSame(Circle other) {
        if (paint == null) {
            if (other.paint != null)
                return false;
        } else if (paint.getColor() != other.paint.getColor())
            return false;
        return true;
    }
}
