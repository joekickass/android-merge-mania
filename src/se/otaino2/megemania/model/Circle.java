package se.otaino2.megemania.model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.graphics.Paint;

/**
 * A circle represents a dot on the board. It keeps track of type (color), position, velocity, size, etc.
 * 
 * It also has a random functionality for changing its speed and direction.
 * 
 * @author otaino-2
 *
 */
public class Circle {
    
    private static final float ORIG_SPEED = 50.0f;
    private static final long MAX_TIME_BETWEEN_SPEED_CHANGES_IN_SECONDS = 30;
    
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> activeTask;
    private float cx;
    private float cy;
    private float radius;
    private Paint paint;
    private float vx;
    private float vy;
    private float speed;
    
    public Circle(float cx, float cy, float radius, Paint paint) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
        this.paint = paint;
        this.speed = ORIG_SPEED;
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
    
    public void changeOrigSpeed(float speed) {
        this.speed = speed;
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
        float seed = (float) Math.random();
        float newVx = (float) (speed * seed);
        float newVy = (float) (speed * (1-seed));
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

    public float getSpeed() {
        return speed;
    }
}
